package ch.xavier
package backtesting

import LocatorsXPaths.*

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import com.microsoft.playwright.*
import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.AriaRole

import java.nio.file.Paths
import java.util
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.*


object BacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new BacktesterActor(context))
}

class BacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 15.seconds


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestMessage(parametersToTest: List[ParametersToTest], browserContext: BrowserContext, actorRef: ActorRef[Message]) =>
        val chartId = sys.env("CHART_ID")

        context.log.info(s"Starting backtesting of chart id:$chartId with ${parametersToTest.size} parameters to enter")

        val page: Page = preparePage(chartId, browserContext)

        enterParameters(parametersToTest, page)
        waitForBacktestingResults(page)

        actorRef ! getBacktestingResults(page)


      case _ =>
        context.log.warn("Received unknown message in BacktesterActor")
        
      
      this


  private def getBacktestingResults(page: Page): BacktestingResultMessage = {
    val netProfitsPercentage: Double = page.locator(s"xpath=/$netProfitsPercentageValueXPath").innerText().replace("%", "").toDouble
    val closedTradesNumber: Int = page.locator(s"xpath=/$closedTradesNumberXPath").innerText().toInt
    val profitabilityPercentage: Double = page.locator(s"xpath=/$profitabilityPercentageValueXPath").innerText().replace("%", "").toDouble
    val profitFactor: Double = page.locator(s"xpath=/$profitFactorValueXPath").innerText().toDouble
    val maxDrawdownPercentage: Double = page.locator(s"xpath=/$maxDrawdownPercentValueXPath").innerText().replace("%", "").toDouble

    BacktestingResultMessage(netProfitsPercentage, closedTradesNumber, profitabilityPercentage, profitFactor, maxDrawdownPercentage)
  }

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
  }

  private def waitForBacktestingResults(page: Page): Unit = {
    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Ok")).click()
//    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).waitFor()
//    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).click()

    page.getByLabel("Net Profit").waitFor()
  }

  private def displayAllLocators(page: Page): Unit = {
//    val locators: util.List[Locator] = page.locator("xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div").all()
//    val locators: util.List[Locator] = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).all()
    val locators: util.List[Locator] = page.getByLabel("Net Profit").all()

    context.log.info(s"locators size: ${locators.size()}")

    var counter = 0
    for locator <- locators.asScala do
      context.log.info(s"counter: $counter, locator: ${locator.innerHTML()}")
      counter += 1
  }

  private def preparePage(chartId: String, browserContext: BrowserContext) = {
    val page: Page = browserContext.newPage()
    page.navigate(s"https://www.tradingview.com/chart/$chartId/")
    page.waitForSelector(s"xpath=/$chartDeepBacktestingScalerXPath").isVisible

    page.getByRole(AriaRole.SWITCH).click()

    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Settings").setExact(true)).click()
    page.waitForSelector(s"xpath=/$welcomeLabelParametersModalXPath").isVisible

    page
  }

}
