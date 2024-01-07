package ch.xavier
package backtesting.actors.main

import Application.{executionContext, system}
import backtesting.TVLocatorsXpath.*
import backtesting.actors.strats.*
import backtesting.actors.strats.deadzonev5.*

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import ch.xavier.backtesting.{BacktestChartResponseMessage, BacktestSpecificPartMessage, BacktestingResultMessage, Message}
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

  private var mainActorRef: ActorRef[Message] = _
  private var bestProfitabilityPercentageResult: Double = -1
  private var actorsCounter = 1


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRefFromMessage: ActorRef[Message], chartId: String) =>
        mainActorRef = mainActorRefFromMessage

        val backtesters: List[ActorRef[Message]] = List(
          context.spawn(DeadZoneV5SensitivityActor(), s"dead-zone-v5-sensitivity-backtester-$actorsCounter"),
          context.spawn(DeadZoneV5FastEMAActor(), s"dead-zone-v5-fast-ema-backtester-$actorsCounter"),
          context.spawn(DeadZoneV5SlowEMAActor(), s"dead-zone-v5-slow-ema-backtester-$actorsCounter"),
          context.spawn(DeadZoneV5BBChannelActor(), s"dead-zone-v5-bb-channel-backtester-$actorsCounter"),
          context.spawn(DeadZoneV5BBStdDeviationActor(), s"dead-zone-v5-bb-std-dev-backtester-$actorsCounter"),
          context.spawn(DeadZoneV5DeadzoneActor(), s"dead-zone-v5-deadzone-backtester-$actorsCounter")
        )

        Source(backtesters)
          //        Source(Random.shuffle(backtesters))
          .mapAsync(1)(backtesterRef => {
            backtesterRef ? (myRef => BacktestSpecificPartMessage(myRef, chartId))
          })
          .map(_.asInstanceOf[BacktestingResultMessage])
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              if bestProfitabilityPercentageResult >= result.profitabilityPercentage then
                logger.info("No better result found, finishing main strat optimization with profitability: " + result.profitabilityPercentage)
                mainActorRef ! BacktestChartResponseMessage()
                Behaviors.stopped
              else
                logger.info(s"Better result found:${result.profitabilityPercentage}, old:$bestProfitabilityPercentageResult")
                bestProfitabilityPercentageResult = result.profitabilityPercentage
                actorsCounter += 1

                logger.info("Continuing main strat optimization")
                context.self ! BacktestSpecificPartMessage(mainActorRef, chartId)
                this

            case Failure(e) =>
              logger.error("Exception received during Strat optimization, aborting:" + e)
              mainActorRef ! BacktestChartResponseMessage()
              Behaviors.stopped
          }

      case _ =>
        context.log.warn("Received unknown message in StratOptimizerActor of type: " + message.getClass)

    this
}
