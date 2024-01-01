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
//          context.spawn(SLOptimizerActor(), "sl-optimizer"),
          context.spawn(TPOptimizerActor(), "tp-optimizer"),
        )
        //TODO: I'm supposed to use a few years period. And it would be nice to use the latest 6 months for example to test the strat after optimization.
        //Also, after reviewing few 5 mins strtaegies - avoid them. Squeeze It is overoptimised. My goal is to focu on trending strategies (like Supertrend, Pmax, McGinley etc) that can follow a pump, and are not losing on dumps

        Source(backtesters)
          .mapAsync(1)(backtesterRef => {
            backtesterRef ? (myRef => BacktestSpecificPartMessage(myRef, chartId))
          })
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              logger.info("Optimisation now complete, have a nice day :)")

            case Failure(e) =>
              logger.error("Exception received during global backtesting:" + e)
          }

      case _ =>
        context.log.warn("Received unknown message in MainBacktesterActor of type: " + message.getClass)

      this
}
