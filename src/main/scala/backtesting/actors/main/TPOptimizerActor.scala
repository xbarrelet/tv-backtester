package ch.xavier
package backtesting.actors.main

import Application.{executionContext, system}
import backtesting.TVLocatorsXpath.*
import backtesting.actors.takeProfit.{MultiTPBacktesterActor, SLAndTPTrailingBacktesterActor, TPLongBacktesterActor, TPShortBacktesterActor}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import ch.xavier.backtesting.{BacktestChartResponseMessage, BacktestSpecificPartMessage, Message}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object TPOptimizerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new TPOptimizerActor(context))
}

private class TPOptimizerActor(context: ActorContext[Message]) extends AbstractBehavior(context) {
  implicit val timeout: Timeout = 7200.seconds
  private val logger: Logger = LoggerFactory.getLogger("TPOptimizerActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val backtesters: List[ActorRef[Message]] = List(
          context.spawn(TPShortBacktesterActor(), "tp-short-backtester"),
          context.spawn(TPLongBacktesterActor(), "tp-long-backtester"),
//          context.spawn(MultiTPBacktesterActor(), "multi-tp-backtester"),
          context.spawn(SLAndTPTrailingBacktesterActor(), "sl-tp-trailing-backtester")
        )

        Source(backtesters)
          .mapAsync(1)(backtesterRef => {
            backtesterRef ? (myRef => BacktestSpecificPartMessage(myRef, chartId))
          })
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              logger.info("TP Optimization now complete.")
              mainActorRef ! BacktestChartResponseMessage()
              Behaviors.stopped

            case Failure(e) =>
              logger.error("Exception received during TP optimization:" + e)
          }

      case _ =>
        context.log.warn("Received unknown message in TPOptimizerActor of type: " + message.getClass)

    this
}
