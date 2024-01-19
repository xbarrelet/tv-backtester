package ch.xavier
package backtesting.actors

import backtesting.*
import backtesting.parameters.TVLocator.MA_TYPE.*
import backtesting.parameters.{StrategyParameter, TYPE}

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.microsoft.playwright.*
import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.{AriaRole, Cookie}

import java.nio.file.Paths
import java.util
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*
import scala.util.Random

object BacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new BacktesterActor(context))
}

class BacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  private var playwright: Playwright = Playwright.create()
  private var chromiumBrowserType: BrowserType = playwright.chromium()
  private var browser: Browser = chromiumBrowserType.launch()
  private val config: BacktesterConfig.type = BacktesterConfig

  private val pageTimeout = 120000
  private var chartId = ""
  private var browserContext: BrowserContext = null
  private var page: Page = null


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case OptimizeParametersMessage(parametersToTest: List[StrategyParameter], actorRef: ActorRef[Message], chartIdFromMessage: String) =>
        chartId = chartIdFromMessage
        resetPage(chartId)

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


      case SaveParametersMessage(parametersToSave: List[StrategyParameter], ref: ActorRef[Message]) =>
        resetPage(chartId)

        try {
          context.log.info(s"Now saving the best parameters of last optimization pass with parameters:${parametersToSave.map(_.value)}")

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
//        context.log.info("Shutting down backtester actor")

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
//    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"timeout_$errorCounter.png")))
    context.log.warn(s"Timeout error id:$errorCounter when trying to backtest in the end actor with parameters:${parametersToTest.map(_.value)}, trying again")

    context.self ! message
  }

  private def resetPage(chartId: String): Unit = {
    if page != null then
      page.close()

    createNewPage(chartId)
  }

  private def saveChart(page: Page): Unit = {
    if page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Save all charts for all symbols and intervals on your layout")).all().size() > 0 then
      page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Save all charts for all symbols and intervals on your layout")).click()
      Thread.sleep(3000)

      context.log.info("Best parameters saved")
      context.log.info("")
    else
      context.log.info("The saved parameters were already the best ones")
      context.log.info("")
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
//      context.log.info(s"Entering for locator type:$locatorType value:${parameterToTest.value}")

      if locatorType.eq(TYPE.CHECKBOX) then
        locator = get_locator(checkboxes, parameterToTest, locatorType)

        val shouldBeClicked = parameterToTest.value.eq("true")
        if locator.isChecked != shouldBeClicked then
          clickOnElement(page, locator)
          page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"tests/${config.botifyVersion}/checkbox_${parameterToTest.tvLocator.toString}.png")))

      else if locatorType.eq(TYPE.INPUT) then
        locator = get_locator(textboxes, parameterToTest, locatorType)
        locator.fill(parameterToTest.value)
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"tests/${config.botifyVersion}/input_${parameterToTest.tvLocator.toString}.png")))

      else if locatorType.eq(TYPE.OPTION) then
        locator = get_locator(buttons, parameterToTest, locatorType)
        clickOnElement(page, locator)
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"tests/${config.botifyVersion}/option_${parameterToTest.tvLocator.toString}.png")))
        page.getByRole(AriaRole.OPTION, new GetByRoleOptions().setName(parameterToTest.value).setExact(true)).click()

      else if locatorType.eq(TYPE.TEST) then
        context.log.info("Testing the current chart")
        context.log.info("")
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("tests/test.png")))

        testCheckboxes(page, checkboxes)
        testButtons(page, buttons)

        fillTextboxesWithIncrementedCounter(page, textboxes)
        context.log.info(s"${buttons.size()} textboxes filled")

        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Ok")).click()
        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).waitFor()
        saveChart(page)
        context.log.info("Tests for current chart done")
  }

  private def testButtons(page: Page, buttons: util.List[Locator]): Unit = {
    var buttonsCounter = 0

    for button <- buttons.asScala do
      if buttonsCounter > 60 then
        button.focus()
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"tests/button_$buttonsCounter.png")))

      buttonsCounter += 1

    context.log.info(s"${buttons.size()} buttons screenshots taken")
  }

  private def testCheckboxes(page: Page, checkboxes: util.List[Locator]): Unit = {
    var checkboxCounter = 0
    for checkbox <- checkboxes.asScala do
      if checkboxCounter > 5 then
        checkbox.focus()
        if !checkbox.isChecked then
          page.mouse().click(checkbox.boundingBox().x + 5, checkbox.boundingBox().y + 5)
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"tests/checkbox_$checkboxCounter.png")))
        checkbox.focus()
        page.mouse().click(checkbox.boundingBox().x + 5, checkbox.boundingBox().y + 5)

      checkboxCounter += 1

    context.log.info(s"${checkboxes.size()} checkboxes screenshots taken")
  }

  private def fillTextboxesWithIncrementedCounter(page: Page, textboxes: util.List[Locator]): Unit = {
    var counter = 0

    for textbox <- textboxes.asScala do
      textbox.fill(counter.toString)
      counter += 1
  }

  private def clickOnElement(page: Page, locator: Locator): Unit = {
    locator.focus()
    page.mouse().click(locator.boundingBox().x + 5, locator.boundingBox().y + 5)
  }

  private def get_locator(locators: util.List[Locator], parameterToTest: StrategyParameter, locatorType: TYPE): Locator = {
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

    var remainingSecondsBeforeTimeout = pageTimeout
    while page.getByText("This strategy did not generate any orders throughout the testing range.").all().isEmpty && page.getByText("Net Profit").all().isEmpty do
      Thread.sleep(5000)
      remainingSecondsBeforeTimeout -= 5000


      if remainingSecondsBeforeTimeout < 0 then
        throw new TimeoutError("Page stuck on waiting for the results, trying again")

  private def createNewPage(chartId: String): Unit =
    if browserContext == null then
      initialiseBrowserContext()

    try {
      openChartAndGoToSettings(chartId)
    }
    catch {
      case e: Exception =>
        if page != null then
          page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"timeout_page_${Random.nextInt()}.png")))
          page.close()

        context.log.warn(s"Exception happened when trying to get a new page, trying again:$e")
        openChartAndGoToSettings(chartId)
    }


  @tailrec
  private def openChartAndGoToSettings(chartId: String): Unit = {
    try {
      page = browserContext.newPage()

      page.navigate(s"https://www.tradingview.com/chart/$chartId/")
      page.getByText("Deep Backtesting").waitFor()
//      page.getByText("Net Profit").waitFor()

      page.getByRole(AriaRole.SWITCH).click()

      page.locator(strategyNameXpath).hover()
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

    browserContext.setDefaultTimeout(pageTimeout)
  }
}
