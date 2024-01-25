package ch.xavier
package backtesting.actors

import Application.{executionContext, system}
import backtesting.actors.main.{AffinementActor, LeverageOptimizerActor, SLOptimizerActor, TPOptimizerActor}
import backtesting.parameters.TVLocator.*
import backtesting.{BacktestingResultMessage, Message, OptimizePartMessage, StartBacktesting}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.microsoft.playwright.*
import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.{AriaRole, BoundingBox, Cookie}
import org.slf4j.{Logger, LoggerFactory}

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object ChartBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new ChartBacktesterActor(context))
}

class ChartBacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 6.hours
  private val logger: Logger = LoggerFactory.getLogger("ChartBacktesterActor")
  private val config: BacktesterConfig.type = BacktesterConfig

  override def onMessage(message: Message): Behavior[Message] =
    message match
      case StartBacktesting() =>
        val chartId: String = sys.env("CHART_ID")
        context.log.info(s"Getting config from chart $chartId")

        fillConfigFromChart(chartId)
        //        deleteAllScreenshotsFromLastRun() -> class sun.nio.fs.WindowsPath$WindowsPathWithAttributes cannot be cast to class java.io.File (sun.nio.fs.WindowsPath$WindowsPathWithAttributes and java.io.File are in module java.base of loader 'bootstrap')

        val backtesters: List[ActorRef[Message]] = List(
          //          context.spawn(StratOptimizerActor(), "strat-optimizer-actor"),
          context.spawn(SLOptimizerActor(), "sl-optimizer-actor"),
          context.spawn(TPOptimizerActor(), "tp-optimizer-actor"),
          context.spawn(AffinementActor(), "affinement-actor"),
          context.spawn(LeverageOptimizerActor(), "leverage-optimizer-actor")
        )

        //TODO: Fix getClosestCheckbox, for now it's on the RSI checkbox but it doesn't make a lot of sense

        context.log.info(s"The optimization is starting with the backtesters: ${backtesters.map(_.path.name.split("-actor").head).mkString(", ")}")
        context.log.info("")

        Source(backtesters)
          .mapAsync(1)(backtesterRef => {
            backtesterRef ? (myRef => OptimizePartMessage(myRef, chartId))
          })
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              logger.info(s"Optimisation now complete for chart $chartId, have a nice day :)")
              System.exit(0)

            case Failure(e) =>
              logger.error("Exception received during global backtesting:" + e)
              System.exit(0)
          }

      case _ =>
        context.log.warn("Received unknown message in MainBacktesterActor of type: " + message.getClass)

    this

  private def deleteAllScreenshotsFromLastRun(): Unit = {
    val screenshotsToDeletePath: Path = Paths.get("screenshots_from_last_run")

    if Files.exists(screenshotsToDeletePath) then
      Files.list(screenshotsToDeletePath).toArray.map(_.asInstanceOf[File]).map(file => Files.delete(file.toPath))
  }

  private def fillConfigFromChart(chartId: String): Unit =
    val playwright: Playwright = Playwright.create()
    val browser: Browser = playwright.chromium().launch()
    val browserContext: BrowserContext = initialiseBrowserContext(browser)
    val page = browserContext.newPage()

    page.navigate(s"https://www.tradingview.com/chart/$chartId/")
    page.getByText("Deep Backtesting").waitFor()

    config.strategyName = getStrategyNameUsedInChart(page)
    context.log.info("Strategy name detected:" + config.strategyName)

//    config.bestResult = getCurrentResult(page)

    config.botifyVersion = getBotifyVersion(page)
    context.log.info("Botify version detected:" + config.botifyVersion)

    config.distanceBetweenLabelAndField = getDistanceBetweenLabelsAndFields(page)
    context.log.info("Distance between labels and fields detected:" + config.distanceBetweenLabelAndField)

    context.log.info("")

    page.close()
    browserContext.close()
    browser.close()
    playwright.close()


  private def getDistanceBetweenLabelsAndFields(page: Page): Int = {
    val slTypeLabelLocator = page.getByText("Type of Stoploss", Page.GetByTextOptions().setExact(true)).first()
    slTypeLabelLocator.scrollIntoViewIfNeeded()
    val labelBoundingBox: BoundingBox = slTypeLabelLocator.boundingBox()

    var distance = 100
    while page.getByText("Donchian").all().size() != 1 do
      page.mouse().click(labelBoundingBox.x + distance, labelBoundingBox.y + (labelBoundingBox.height / 2.0))
//      Thread.sleep(100)
      distance += 10

    distance
  }

  private def getCurrentResult(page: Page): BacktestingResultMessage = {
    page.getByRole(AriaRole.SWITCH).click()
    page.getByText("Net Profit").waitFor()

    val netProfitsPercentage: Double = getNumberFromResultsFields(page.locator(netProfitsPercentageValueXPath))
    val closedTradesNumber: Int = getNumberFromResultsFields(page.locator(closedTradesNumberXPath)).toInt
    val profitabilityPercentage: Double = getNumberFromResultsFields(page.locator(profitabilityPercentageValueXPath))
    val profitFactor: Double = getNumberFromResultsFields(page.locator(profitFactorValueXPath))
    val maxDrawdownPercentage: Double = getNumberFromResultsFields(page.locator(maxDrawdownPercentValueXPath))

    context.log.info(s"Current result: netProfitsPercentage: $netProfitsPercentage, closedTradesNumber: $closedTradesNumber, profitabilityPercentage: $profitabilityPercentage, profitFactor: $profitFactor, maxDrawdownPercentage: $maxDrawdownPercentage")

    BacktestingResultMessage(netProfitsPercentage, closedTradesNumber, profitabilityPercentage, profitFactor,
      maxDrawdownPercentage, List.empty)
  }

  private def initialiseBrowserContext(browser: Browser): BrowserContext = {
    val browserContext = browser.newContext(
      Browser.NewContextOptions()
        .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36")
        .setViewportSize(1920, 1080)
    )

    val cookies: java.util.List[Cookie] = new java.util.ArrayList[Cookie]()
    cookies.add(new Cookie("sessionid", sys.env("SESSION_ID")).setDomain(".tradingview.com").setPath("/"))
    browserContext.addCookies(cookies)

    browserContext.setDefaultTimeout(60000)
    browserContext
  }

  private def getStrategyNameUsedInChart(page: Page): String =
    val strategyName = page.locator(strategyNameXpath).innerText()
    strategyName.split("by").head


  private def getBotifyVersion(page: Page): String =
    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Settings").setExact(true)).click()
    page.getByText("Core Boilerplate Version").waitFor()

    page.locator(botifyVersionXPath).innerText().split("@").head.trim

  private def getNumberFromResultsFields(locator: Locator): Double =
    val innerText = locator.innerText()

    if innerText.contains("−") then
      0.0
    else
      locator.innerText().replace("%", "").replace(" ", "").replace("N/A", "0.0").replace(" ", "").toDouble
}
