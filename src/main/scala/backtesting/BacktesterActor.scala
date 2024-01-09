package ch.xavier
package backtesting

import backtesting.parameters.TVLocators.MA_TYPE.*
import backtesting.parameters.{StrategyParameter, TYPE}

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.microsoft.playwright.*
import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.{AriaRole, Cookie}
import scala.jdk.CollectionConverters.*

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
      case BacktestMessage(parametersToTest: List[StrategyParameter], actorRef: ActorRef[Message], chartIdFromMessage: String) =>
        chartId = chartIdFromMessage

        if page == null then
          createNewPage(chartId)
        else
          resetPage(chartId)

        context.log.info(s"Backtesting parameters:${parametersToTest.map(_.value)}")

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


      case SaveParametersMessage(parametersToSave: List[StrategyParameter], ref: ActorRef[Message]) =>
        resetPage(chartId)

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
        context.log.debug("Shutting down backtester actor")

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

  private def processTimeoutException(message: Message, actorRef: ActorRef[Message], timeoutException: TimeoutError, parametersToTest: List[StrategyParameter]): Unit = {
    val errorCounter = Random.nextInt(1000)
    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"timeout_$errorCounter.png")))
    context.log.warn(s"Timeout error id:$errorCounter when trying to backtest in the end actor, trying again:$timeoutException")

    context.self ! message
  }

  private def resetPage(chartId: String): Unit = {
    page.close()
    createNewPage(chartId)
  }

  private def saveChart(page: Page): Unit = {
    if page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Save all charts for all symbols and intervals on your layout")).all().size() > 0 then
      page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Save all charts for all symbols and intervals on your layout")).click()
      Thread.sleep(3000)

      context.log.info("Best parameters saved")
    else
      context.log.info("The saved parameters were already the best ones")
  }

  private def getBacktestingResults(page: Page, parametersToTest: List[StrategyParameter]): BacktestingResultMessage = {
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

  private def enterParameters(parametersToTest: List[StrategyParameter], page: Page): Unit = {
    val checkboxes = page.getByRole(AriaRole.CHECKBOX).all()
    val buttons = page.getByRole(AriaRole.BUTTON).all()
    val textboxes = page.getByRole(AriaRole.TEXTBOX).all()

    for parameterToTest <- parametersToTest do
      val locatorType: TYPE = parameterToTest.tvLocator.getType
      var locator: Locator = null

      if locatorType.eq(TYPE.CHECKBOX) then
        locator = get_locator(checkboxes, parameterToTest)

        val shouldBeClicked = parameterToTest.value.eq("true")
        if locator.isChecked != shouldBeClicked then
          clickOnElement(page, locator)

      else if locatorType.eq(TYPE.INPUT) then
        locator = get_locator(textboxes, parameterToTest)
        locator.fill(parameterToTest.value)

      else if locatorType.eq(TYPE.OPTION) then
        locator = get_locator(buttons, parameterToTest)
        clickOnElement(page, locator)
        page.getByRole(AriaRole.OPTION, new GetByRoleOptions().setName(parameterToTest.value).setExact(true)).click()
  }

  private def clickOnElement(page: Page, locator: Locator): Unit = {
    locator.focus()
    page.mouse().click(locator.boundingBox().x + 1, locator.boundingBox().y + 1)
  }

  private def get_locator(locators: util.List[Locator], parameterToTest: StrategyParameter): Locator = {
    var index = parameterToTest.tvLocator.getIndex
    if index < 0 then
      index = locators.size() + index

    locators.get(index)
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


  private def createNewPage(chartId: String): Unit =
    if browserContext == null then
      initialiseBrowserContext()

    try {
      openChartAndGoToSettings(chartId)
    }
    catch {
      case e: Exception =>
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"timeout_page_${Random.nextInt()}.png")))
        context.log.warn(s"Exception happened when trying to get a new page, trying again:$e")

        page.close()
        openChartAndGoToSettings(chartId)
    }


  @tailrec
  private def openChartAndGoToSettings(chartId: String): Unit = {
    try {
      page = browserContext.newPage()

      page.navigate(s"https://www.tradingview.com/chart/$chartId/")
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
        openChartAndGoToSettings(chartId)
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
