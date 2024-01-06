package ch.xavier
package backtesting

import TVLocators.*
import backtesting.parameters.ParametersToTest

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.microsoft.playwright.*
import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.{AriaRole, Cookie}

import java.nio.file.Paths
import java.util
import scala.annotation.tailrec
import scala.util.Random


object BacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new BacktesterActor(context))
}

class BacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  private var playwright: Playwright = Playwright.create()
  private var chromiumBrowserType: BrowserType = playwright.chromium()
  private var browser: Browser = chromiumBrowserType.launch()
  private var chartId = ""
  private var browserContext: BrowserContext = null
  private var page: Page = null


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message], chartIdFromMessage: String) =>
        chartId = chartIdFromMessage

        if page == null then
          createNewPage()
        else
          resetPage()

        //        context.log.info(s"Backtesting parameters:${parametersToTest.map(_.value)}")

        try {
          enterParameters(parametersToTest, page)

          waitForBacktestingResults(page)
          actorRef ! getBacktestingResults(page, parametersToTest)
        }
        catch
          case timeoutException: TimeoutError =>
            processTimeoutException(message, actorRef, timeoutException, parametersToTest)

          case e: Exception =>
            val errorCounter = Random.nextInt(1000)
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"error_$errorCounter.png")))
            context.log.error(s"Error with id $errorCounter when trying to backtest in the end actor, please check the screenshot to get an idea of what is happening:$e")
            context.self ! message


      case SaveParametersMessage(parametersToSave: List[ParametersToTest], ref: ActorRef[Message]) =>
        resetPage()

        try {
          context.log.info(s"Now saving the best parameters for chart id:$chartId. Parameters:${parametersToSave.map(_.value)}")

          enterParameters(parametersToSave, page)

          page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Ok")).click()
          page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).waitFor()

          saveChart(page)

          ref ! ParametersSavedMessage()
        }
        catch
          case timeoutException: TimeoutError =>
            context.log.error(s"Timeout error when trying to save the parameters, please save it manually:$timeoutException")
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"timeout_save.png")))
            ref ! ParametersSavedMessage()


      case CloseBacktesterMessage() =>
        context.log.info("Shutting down backtester actor")

        closeEverything()
        Behaviors.stopped

      case _ =>
        context.log.error("Received unknown message in BacktesterActor")

    this


  private def closeEverything(): Unit = {
    page.close()
    browserContext.close()
    browser.close()
    playwright.close()
  }

  private def processTimeoutException(message: Message, actorRef: ActorRef[Message], timeoutException: TimeoutError, parametersToTest: List[ParametersToTest]): Unit = {
    val errorCounter = Random.nextInt(1000)
    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"timeout_$errorCounter.png")))
    context.log.warn(s"Timeout error id:$errorCounter when trying to backtest in the end actor, trying again:$timeoutException")

    context.self ! message
  }

  private def resetPage(): Unit = {
    page.close()
    createNewPage()
  }

  private def saveChart(page: Page): Unit = {
    if page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Save all charts for all symbols and intervals on your layout")).all().size() > 0 then
      page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Save all charts for all symbols and intervals on your layout")).click()
      Thread.sleep(3000)

      context.log.info("Best parameters saved")
    else
      context.log.info("The saved parameters were already the best ones")
  }

  private def getBacktestingResults(page: Page, parametersToTest: List[ParametersToTest]): BacktestingResultMessage = {
    if page.getByText("This strategy did not generate any orders throughout the testing range.").all().size() > 0 then
      context.log.debug("Current parameters resulted in no trade.")
      BacktestingResultMessage(0.0, 0, 0.0, 0.0, 0.0, parametersToTest)

    else
      val netProfitsPercentage: Double = getNumberFromResultsFields(page.locator(netProfitsPercentageValueXPath))
      val closedTradesNumber: Int = getNumberFromResultsFields(page.locator(closedTradesNumberXPath)).toInt
      val profitabilityPercentage: Double = getNumberFromResultsFields(page.locator(profitabilityPercentageValueXPath))
      val profitFactor: Double = getNumberFromResultsFields(page.locator(profitFactorValueXPath))
      val maxDrawdownPercentage: Double = getNumberFromResultsFields(page.locator(maxDrawdownPercentValueXPath))

      BacktestingResultMessage(netProfitsPercentage, closedTradesNumber, profitabilityPercentage, profitFactor,
        maxDrawdownPercentage, parametersToTest)
  }

  private def getNumberFromResultsFields(locator: Locator): Double =
    val innerText = locator.innerText()

    if innerText.contains("−") then
      0.0
    else
      locator.innerText().replace("%", "").replace(" ", "").replace("N/A", "0.0").replace(" ", "").toDouble

  private def enterParameters(parametersToTest: List[ParametersToTest], page: Page): Unit = {
    for parameterToTest <- parametersToTest do
      val locator = page.locator(parameterToTest.fullXPath)

      if parameterToTest.action.eq("fill") then
        locator.fill(parameterToTest.value)

      else if parameterToTest.action.eq("selectOption") then
        selectOption(page, parameterToTest, locator)

      else if parameterToTest.action.eq("check") then
        val shouldBeClicked = parameterToTest.value.eq("true")

        if locator.isChecked != shouldBeClicked then
          locator.setChecked(!locator.isChecked())
  }

  private def selectOption(page: Page, parameterToTest: ParametersToTest, locator: Locator): Unit = {
    while page.getByRole(AriaRole.OPTION, new GetByRoleOptions().setName(parameterToTest.value).setExact(true)).all().isEmpty do
      locator.click()
      Thread.sleep(1000)

    page.getByRole(AriaRole.OPTION, new GetByRoleOptions().setName(parameterToTest.value).setExact(true)).click()
  }

  private def waitForBacktestingResults(page: Page): Unit =
    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Ok")).waitFor()
    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Ok")).click()

    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).waitFor()

    val generateReportButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).first()
    if generateReportButton.isEnabled then
      generateReportButton.click()

    while page.getByText("This strategy did not generate any orders throughout the testing range.").all().isEmpty && page.getByText("Net Profit").all().isEmpty do
      Thread.sleep(5000)


  private def createNewPage(): Unit =
    if browserContext == null then
      initialiseBrowserContext()

    try {
      openChartAndGoToSettings()
    }
    catch {
      case e: TimeoutError =>
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"timeout_page_${Random.nextInt()}.png")))
        context.log.warn(s"Timeout error when trying to get a new page, trying again:$e")

        page.close()
        openChartAndGoToSettings()
    }


  @tailrec
  private def openChartAndGoToSettings(): Unit = {
    try {
      page = browserContext.newPage()

      page.navigate(s"https://www.tradingview.com/chart/${sys.env("CHART_ID")}/")
      page.getByText("Deep Backtesting").waitFor()

      page.getByRole(AriaRole.SWITCH).click()

      page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Settings").setExact(true)).click()
      page.getByText("Core Boilerplate Version").waitFor()
    }
    catch {
      case e: Exception =>
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"start_page_${Random.nextInt()}.png")))
        context.log.warn(s"Error when trying to open a new page, restarting Playwright and trying again:${e.getMessage}")

        resetEverything()
        openChartAndGoToSettings()
    }
  }

  private def resetEverything(): Unit = {
    closeEverything()
    Thread.sleep(5000)

    playwright = Playwright.create()
    chromiumBrowserType = playwright.chromium()
    browser = chromiumBrowserType.launch()
    initialiseBrowserContext()
  }

  private def initialiseBrowserContext(): Unit = {
    browserContext = browser.newContext(
      Browser.NewContextOptions()
        .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36")
        .setViewportSize(1920, 1080)
    )

    val cookies: java.util.List[Cookie] = new java.util.ArrayList[Cookie]()
    cookies.add(new Cookie("sessionid", sys.env("SESSION_ID")).setDomain(".tradingview.com").setPath("/"))
    browserContext.addCookies(cookies)

    browserContext.setDefaultTimeout(90000)
  }
}
