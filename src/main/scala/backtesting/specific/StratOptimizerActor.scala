package ch.xavier
package backtesting.specific

import Application.{executionContext, system}
import TVLocators.*
import backtesting.specific.strats.*

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
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


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val backtesters: List[ActorRef[Message]] = List(
          context.spawn(DeadZoneV5SensitivityActor(), "dead-zone-v5-sensitivity-backtester"),
          context.spawn(DeadZoneV5FastEMAActor(), "dead-zone-v5-fast-ema-backtester"),
          context.spawn(DeadZoneV5SlowEMAActor(), "dead-zone-v5-slow-ema-backtester"),
          context.spawn(DeadZoneV5BBChannelActor(), "dead-zone-v5-bb-channel-backtester"),
          context.spawn(DeadZoneV5BBStdDeviationActor(), "dead-zone-v5-bb-std-dev-backtester"),
          context.spawn(DeadZoneV5DeadzoneActor(), "dead-zone-v5-deadzone-backtester")
        )

        Source(backtesters)
          .mapAsync(1)(backtesterRef => {
            backtesterRef ? (myRef => BacktestSpecificPartMessage(myRef, chartId))
          })
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              logger.info("Strat optimization now complete.")
              mainActorRef ! BacktestChartResponseMessage()
              Behaviors.stopped

            case Failure(e) =>
              logger.error("Exception received during Strat optimization:" + e)
          }

      case _ =>
        context.log.warn("Received unknown message in StratOptimizerActor of type: " + message.getClass)

    this
}
