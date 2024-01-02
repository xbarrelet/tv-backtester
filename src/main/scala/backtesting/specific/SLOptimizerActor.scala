package ch.xavier
package backtesting.specific

import Application.{executionContext, system}
import TVLocators.*
import backtesting.specific.stopLoss.{SLLongBacktesterActor, SLShortBacktesterActor, SLTrailingBacktesterActor}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object SLOptimizerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new SLOptimizerActor(context))
}

private class SLOptimizerActor(context: ActorContext[Message]) extends AbstractBehavior(context) {
  implicit val timeout: Timeout = 3600.seconds
  private val logger: Logger = LoggerFactory.getLogger("SLOptimizerActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val backtesters: List[ActorRef[Message]] = List(
          context.spawn(SLShortBacktesterActor(), "sl-short-backtester"),
          context.spawn(SLLongBacktesterActor(), "sl-long-backtester"),
          context.spawn(SLTrailingBacktesterActor(), "sl-trailing-backtester"),
        )

        Source(backtesters)
          .mapAsync(1)(backtesterRef => {
            backtesterRef ? (myRef => BacktestSpecificPartMessage(myRef, chartId))
          })
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              logger.info("SL optimization now complete.")
              mainActorRef ! BacktestChartResponseMessage()
              Behaviors.stopped
            case Failure(e) =>
              logger.error("Exception received during SL optimization:" + e)
          }

      case _ =>
        context.log.warn("Received unknown message in SLOptimizerActor of type: " + message.getClass)

    this
}
