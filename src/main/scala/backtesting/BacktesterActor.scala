package ch.xavier
package backtesting

import LocatorsXPaths.*

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import com.microsoft.playwright.*
import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.AriaRole

import java.util
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.*
import ch.xavier.PlaywrightService

import java.nio.file.Paths
import scala.util.Random


object BacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new BacktesterActor(context))
}

class BacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 15.seconds
  private val chartId = sys.env("CHART_ID")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message]) =>
        context.log.debug(s"Starting backtesting actor for chart $chartId")

        val page: Page = PlaywrightService.preparePage()

        enterParameters(parametersToTest, page)

        waitForBacktestingResults(page)
        actorRef ! getBacktestingResults(page, parametersToTest)

        page.close()

      case SaveParametersMessage(parametersToSave: List[ParametersToTest]) =>
        context.log.info(s"Now saving the best parameters for chart id:$chartId. Parameters:$parametersToSave")

        val page: Page = PlaywrightService.preparePage()

        enterParameters(parametersToSave, page)

        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Ok")).click()
        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).waitFor()
        page.keyboard.press("Control+S")

        context.log.info("Parameters saved!")
        page.close()
d
      case _ =>
        context.log.error("Received unknown message in BacktesterActor")

      Behaviors.stopped


  private def getBacktestingResults(page: Page, parametersToTest: List[ParametersToTest]): BacktestingResultMessage = {
    val netProfitsPercentage: Double = page.locator(s"xpath=/$netProfitsPercentageValueXPath").innerText().replace("%", "").toDouble
    val closedTradesNumber: Int = page.locator(s"xpath=/$closedTradesNumberXPath").innerText().toInt
    val profitabilityPercentage: Double = page.locator(s"xpath=/$profitabilityPercentageValueXPath").innerText().replace("%", "").toDouble
    val profitFactor: Double = page.locator(s"xpath=/$profitFactorValueXPath").innerText().toDouble
    val maxDrawdownPercentage: Double = page.locator(s"xpath=/$maxDrawdownPercentValueXPath").innerText().replace("%", "").toDouble

    BacktestingResultMessage(netProfitsPercentage, closedTradesNumber, profitabilityPercentage, profitFactor,
      maxDrawdownPercentage, parametersToTest)
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
    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).waitFor()

    val generateReportButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Generate report")).first()
    if generateReportButton.isEnabled then
      generateReportButton.click()

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
}
