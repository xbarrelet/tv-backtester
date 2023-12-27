package ch.xavier
package backtesting

import TVLocators.*

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.microsoft.playwright.*
import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.{AriaRole, Cookie}

import java.nio.file.Paths
import java.util
import scala.jdk.CollectionConverters.*
import scala.util.Random


object BacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new BacktesterActor(context))
}

class BacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  private val chartId = sys.env("CHART_ID")
  private var ownPlaywrightService: PlaywrightService = _
  private val chromiumBrowserType: BrowserType = Playwright.create().chromium()
  private val browser: Browser = chromiumBrowserType.launch()
  private val browserContext: BrowserContext = InitialiseBrowserContext()


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case EnrichedBacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message], playwrightService: PlaywrightService) =>
        ownPlaywrightService = playwrightService
        context.log.info(s"Starting backtesting actor:${context.self} for chart $chartId")

        val page: Page = getPreparedPage(context.self.toString)

        //        displayAllLocators(page)
//        actorRef ! BacktestingResultMessage(0, 0, 0, 0, 0, parametersToTest)

        try {
          enterParameters(parametersToTest, page)

          waitForBacktestingResults(page)
          actorRef ! getBacktestingResults(page, parametersToTest)
        }
        catch
          case e: Exception =>
            val counter = Random.nextInt(1000)
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"error_$counter.png")))
            context.log.error(s"Error with id $counter when trying to backtest in the end actor, please check error.png screenshot to get an idea of what is happening:$e")
            page.close()
            context.self ! message
            return this

        page.close()

      case SaveParametersMessage(parametersToSave: List[ParametersToTest]) =>
        context.log.info(s"Now saving the best parameters for chart id:$chartId. Parameters:$parametersToSave")

        val page: Page = getPreparedPage(context.self.toString)

        enterParameters(parametersToSave, page)

        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Ok")).click()
        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).waitFor()
        page.keyboard.press("Control+S")

        context.log.info("Parameters saved!")
        page.close()

      case _ =>
        context.log.error("Received unknown message in BacktesterActor")

      browserContext.close()
      browser.close()
      Behaviors.stopped


  private def getPreparedPage(stringActorRef: String) = {
    var isPageReady = false
    var page: Page = null

    while !isPageReady do
      try {
        page = preparePage(browserContext.newPage())
        isPageReady = true
      }
      catch
        case e: Exception =>
          context.log.debug(s"Error when preparing a page in $stringActorRef, trying again")
    page
  }

  private def getBacktestingResults(page: Page, parametersToTest: List[ParametersToTest]): BacktestingResultMessage = {
    val netProfitsPercentage: Double = getNumberFromResultsFields(page.locator(netProfitsPercentageValueXPath))
    val closedTradesNumber: Int = getNumberFromResultsFields(page.locator(closedTradesNumberXPath)).toInt
    val profitabilityPercentage: Double = getNumberFromResultsFields(page.locator(profitabilityPercentageValueXPath))
    val profitFactor: Double = getNumberFromResultsFields(page.locator(profitFactorValueXPath))
    val maxDrawdownPercentage: Double = getNumberFromResultsFields(page.locator(maxDrawdownPercentValueXPath))

    BacktestingResultMessage(netProfitsPercentage, closedTradesNumber, profitabilityPercentage, profitFactor,
      maxDrawdownPercentage, parametersToTest)
  }

  private def getNumberFromResultsFields(locator: Locator): Double =
    locator.innerText().replace("%", "").replace(" ", "").replace("N/A", "0.0").replace(" ", "").toDouble

  private def enterParameters(parametersToTest: List[ParametersToTest], page: Page): Unit = {
    for parameterToTest <- parametersToTest do
      val locator = page.locator(parameterToTest.fullXPath)

      if parameterToTest.action.eq("fill") then
        locator.fill(parameterToTest.value)

      else if parameterToTest.action.eq("selectOption") then
        locator.selectOption(parameterToTest.value)

      else if parameterToTest.action.eq("selectTakeProfit") then
        locator.click()
        page.getByRole(AriaRole.OPTION, new GetByRoleOptions().setName(parameterToTest.value)).click()

      else if parameterToTest.action.eq("selectStopLoss") then
        locator.click()
        page.getByRole(AriaRole.OPTION, new GetByRoleOptions().setName(parameterToTest.value)).click()
  }

  private def waitForBacktestingResults(page: Page): Unit = {
    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Ok")).waitFor()
    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Ok")).click()

    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).waitFor()
    val generateReportButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).first()
    if generateReportButton.isEnabled then
      generateReportButton.click()

    page.getByLabel("Net Profit").waitFor()
  }

  private def displayAllLocators(page: Page): Unit = {
//    val locators: util.List[Locator] = page.locator("//html/body/div[6]/div/div/div[1]/div/div[3]/div/div").all()
//    val locators: util.List[Locator] = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).all()
    val locators: util.List[Locator] = page.getByRole(AriaRole.TEXTBOX).all()
//    val locators: util.List[Locator] = page.getByText("Profit factor Long (Risk to Reward)").all()
    context.log.info(s"locators size: ${locators.size()}")

    var counter = 0
    for locator: Locator <- locators.asScala do
      context.log.info(s"counter: $counter, locator: ${locator.inputValue()}")
      locator.fill(counter.toString)
      counter += 1

//      if counter % 10 == 0 then
//        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(s"screenshot_$counter.png")))
  }

  private def preparePage(page: Page): Page =
    page.navigate(s"https://www.tradingview.com/chart/${sys.env("CHART_ID")}/")
    page.waitForSelector(chartDeepBacktestingScalerXPath).isVisible

    page.getByRole(AriaRole.SWITCH).click()

    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Settings").setExact(true)).click()
    page.waitForSelector(welcomeLabelParametersModalXPath).isVisible
    page

  private def InitialiseBrowserContext(): BrowserContext = {
//    val chromiumBrowserType: BrowserType = Playwright.create().chromium()
//    val browser: Browser = chromiumBrowserType.launch()

    val browserContext: BrowserContext = browser.newContext(
      Browser.NewContextOptions()
        .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36")
        .setViewportSize(1920, 1080)
    )

    val cookies: java.util.List[Cookie] = new java.util.ArrayList[Cookie]()
    cookies.add(new Cookie("sessionid", sys.env("SESSION_ID")).setDomain(".tradingview.com").setPath("/"))
    browserContext.addCookies(cookies)

    browserContext.setDefaultTimeout(30000)

    browserContext
  }
}
