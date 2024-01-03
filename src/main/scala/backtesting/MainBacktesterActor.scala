package ch.xavier
package backtesting

import Application.{executionContext, system}
import TVLocators.*
import backtesting.specific.{SLOptimizerActor, TPOptimizerActor}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object MainBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new MainBacktesterActor(context))
}

class MainBacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 7200.seconds
  private val logger: Logger = LoggerFactory.getLogger("MainBacktesterActor")

  override def onMessage(message: Message): Behavior[Message] =
    message match
      case StartBacktesting() =>
        val chartId: String = sys.env("CHART_ID")
        context.log.info(s"Starting backtesting for chart $chartId")

        val backtesters: List[ActorRef[Message]] = List(
          context.spawn(SLOptimizerActor(), "sl-optimizer"),
          context.spawn(TPOptimizerActor(), "tp-optimizer"),
        )
        
        Source(backtesters)
          .mapAsync(1)(backtesterRef => {
            backtesterRef ? (myRef => BacktestSpecificPartMessage(myRef, chartId))
          })
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              logger.info("Optimisation now complete, have a nice day :)")
              System.exit(0)

            case Failure(e) =>
              logger.error("Exception received during global backtesting:" + e)
          }

      case _ =>
        context.log.warn("Received unknown message in MainBacktesterActor of type: " + message.getClass)

      this
}
