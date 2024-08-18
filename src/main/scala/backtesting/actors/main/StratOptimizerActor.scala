package ch.xavier
package backtesting.actors.main

import Application.{executionContext, system}
import backtesting.{BacktestChartResponseMessage, BacktestingResultMessage, Message, OptimizeParametersListsMessage, OptimizePartMessage}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import ch.xavier.backtesting.parameters.TVLocator.strategyNameXpath
import com.microsoft.playwright.options.Cookie
import com.microsoft.playwright.{Browser, BrowserContext, Playwright}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object StratOptimizerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new StratOptimizerActor(context))
}

private class StratOptimizerActor(context: ActorContext[Message]) extends AbstractBehavior(context) {
  implicit val timeout: Timeout = 7200.seconds
  private val logger: Logger = LoggerFactory.getLogger("StratOptimizerActor")
  private val config: BacktesterConfig.type = BacktesterConfig

  private var mainActorRef: ActorRef[Message] = _
  private var bestProfitabilityPercentageResult: Double = -1
  private var actorsCounter = 1


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case OptimizePartMessage(ref: ActorRef[Message], chartId: String) =>
        mainActorRef = ref

        val stratName = getStrategyNameUsedInChart(chartId)

        mainActorRef ! BacktestChartResponseMessage()
        Behaviors.stopped

//              val backtesters: List[ActorRef[Message]] = getStrategyActors(chartId)

      //        Source(backtesters)
      //          //        Source(Random.shuffle(backtesters))
      //          .mapAsync(1)(backtesterRef => {
      //            backtesterRef ? (myRef => BacktestSpecificPartMessage(myRef, chartId))
      //          })
      //          .map(_.asInstanceOf[BacktestingResultMessage])
      //          .runWith(Sink.last)
      //          .onComplete {
      //            case Success(result) =>
      //              if bestProfitabilityPercentageResult >= result.profitabilityPercentage then
      //                logger.info("No better result found, finishing main strat optimization with profitability: " + result.profitabilityPercentage)
      //                mainActorRef ! BacktestChartResponseMessage()
      //                Behaviors.stopped
      //              else
      //                logger.info(s"Better result found:${result.profitabilityPercentage}, old:$bestProfitabilityPercentageResult")
      //                bestProfitabilityPercentageResult = result.profitabilityPercentage
      //                actorsCounter += 1
      //
      //                logger.info("Continuing main strat optimization")
      //                context.self ! BacktestSpecificPartMessage(mainActorRef, chartId)
      //                this
      //
      //            case Failure(e) =>
      //              logger.error("Exception received during Strat optimization, aborting:" + e)
      //              mainActorRef ! BacktestChartResponseMessage()
      //              Behaviors.stopped
      //          }

      case _ =>
        context.log.warn("Received unknown message in StratOptimizerActor of type: " + message.getClass)

    this

  //  private def getStrategyActors(chartId: String): List[ActorRef[Message]] = {
  //    val strategyName = getStrategyNameUsedInChart(chartId)
  //    config.strategyName = strategyName
  //    context.log.info("Strategy name detected:" + strategyName)
  //
  //    //TODO: Refactoring in something that doesn't require so many new actors
  //    if strategyName.contains("DEAD ZONE") then
  //      List(
  ////        context.spawn(DeadZoneV5AllMainParametersActor(), s"dead-zone-v5-all-main-parameters-backtester-$actorsCounter"),
  //        context.spawn(DeadZoneV5SensitivityActor(), s"dead-zone-v5-sensitivity-backtester-$actorsCounter"),
  //        context.spawn(DeadZoneV5FastEMAActor(), s"dead-zone-v5-fast-ema-backtester-$actorsCounter"),
  //        context.spawn(DeadZoneV5SlowEMAActor(), s"dead-zone-v5-slow-ema-backtester-$actorsCounter"),
  //        context.spawn(DeadZoneV5BBChannelActor(), s"dead-zone-v5-bb-channel-backtester-$actorsCounter"),
  //        context.spawn(DeadZoneV5BBStdDeviationActor(), s"dead-zone-v5-bb-std-dev-backtester-$actorsCounter"),
  //        context.spawn(DeadZoneV5DeadzoneActor(), s"dead-zone-v5-deadzone-backtester-$actorsCounter")
  //      )
  //
  //    else if strategyName.contains("Squeeze") then
  //      List(
  //        context.spawn(SqueezeBBLengthActor(), s"squeeze-bb-length-backtester-$actorsCounter"),
  //        context.spawn(SqueezeBBMultiFactorActor(), s"squeeze-bb-multi-factor-backtester-$actorsCounter"),
  //        context.spawn(SqueezeKCLengthActor(), s"squeeze-kc-length-backtester-$actorsCounter"),
  //        context.spawn(SqueezeKCMultiFactorActor(), s"squeeze-kc-multi-factor-backtester-$actorsCounter"),
  //        context.spawn(SqueezeWTFirstDivergencesMinActor(), s"squeeze-wt-divergences-min-backtester-$actorsCounter"),
  //      )
  //
  //    else if strategyName.contains("FVMA") then
  //      List(
  //        context.spawn(FVMAATRLengthActor(), s"fvma-atr-length-backtester-$actorsCounter"),
  //        context.spawn(FVMAFactorActor(), s"fvma-factor-backtester-$actorsCounter"),
  //        context.spawn(FVMAADXLengthActor(), s"fvma-adx-length-backtester-$actorsCounter"),
  //        context.spawn(FVMAWeigthingActor(), s"fvma-weighting-backtester-$actorsCounter"),
  //        context.spawn(FVMAMALengthActor(), s"fvma-ma-length-backtester-$actorsCounter"),
  //        context.spawn(FVMAFastLengthActor(), s"fvma-fast-length-backtester-$actorsCounter"),
  //        context.spawn(FVMASlowLengthActor(), s"fvma-slow-length-backtester-$actorsCounter"),
  //        context.spawn(FVMASignalLengthActor(), s"fvma-signal-backtester-$actorsCounter"),
  //      )
  //
  //    else
  //      context.log.error(s"No recognized strategy detected in the chart:$chartId, aborting backtesting")
  //      List.empty
  //  }

  private def getStrategyNameUsedInChart(chartId: String): String =
    val playwright: Playwright = Playwright.create()
    val browser: Browser = playwright.chromium().launch()
    val browserContext: BrowserContext = initialiseBrowserContext(browser)
    val page = browserContext.newPage()

    page.navigate(s"https://www.tradingview.com/chart/$chartId/")
    page.getByText("Deep Backtesting").waitFor()
    val strategyName = page.locator(strategyNameXpath).innerText()

    page.close()
    browserContext.close()
    browser.close()
    playwright.close()

    strategyName.split("by").head

  //
  private def initialiseBrowserContext(browser: Browser): BrowserContext = {
    val browserContext = browser.newContext(
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
