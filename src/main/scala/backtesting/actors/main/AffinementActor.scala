package ch.xavier
package backtesting.actors.main

import Application.{executionContext, system}
import backtesting.TVLocatorsXpath.*
import backtesting.actors.affinement.{HurstExponentBacktesterActor, MasAffinementBacktesterActor, RangeFilterBacktesterActor, VWAPCrossoverBacktesterActor}
import backtesting.actors.takeProfit.TPLeverageBacktesterActor

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object AffinementActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new AffinementActor(context))
}

private class AffinementActor(context: ActorContext[Message]) extends AbstractBehavior(context) {
  implicit val timeout: Timeout = 7200.seconds
  private val logger: Logger = LoggerFactory.getLogger("AffinementActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val backtesters: List[ActorRef[Message]] = List(
          context.spawn(MasAffinementBacktesterActor(), "ma-affinement-backtester"),
          context.spawn(HurstExponentBacktesterActor(), "hurst-exponent-backtester"),
          context.spawn(RangeFilterBacktesterActor(), "range-filtering-backtester"),
          context.spawn(VWAPCrossoverBacktesterActor(), "vwap-crossover-backtester"),
          context.spawn(TPLeverageBacktesterActor(), "tp-leverage-backtester")
        )

        Source(backtesters)
          .mapAsync(1)(backtesterRef => {
            backtesterRef ? (myRef => BacktestSpecificPartMessage(myRef, chartId))
          })
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              logger.info("Affinement now complete.")
              mainActorRef ! BacktestChartResponseMessage()
              Behaviors.stopped

            case Failure(e) =>
              logger.error("Exception received during affinement:" + e)
          }

      case _ =>
        context.log.warn("Received unknown message in AffinementActor of type: " + message.getClass)

    this
}
