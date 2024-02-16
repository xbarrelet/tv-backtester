package ch.xavier
package backtesting.actors

import Application.{executionContext, system}
import backtesting.*
import backtesting.parameters.{StrategyParameter, StrategyParametersFactory}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.slf4j.Logger

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

abstract class AbstractMainOptimizerActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 2.hours
  def logger: Logger
  def parametersLists: List[List[List[StrategyParameter]]]

  private val optimizerActorRef: ActorRef[Message] = context.spawn(ParametersGroupBacktesterActor(), "parameter-optimizer-actor")
  val parametersFactory: StrategyParametersFactory.type = StrategyParametersFactory
  var evaluationParameter: String = "profitability"

  
  override def onMessage(message: Message): Behavior[Message] =
    message match
      case OptimizePartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        Source(parametersLists)
          .mapAsync(1)(parameters => {
            optimizerActorRef ? (myRef => OptimizeParametersListsMessage(parameters, myRef, chartId, evaluationParameter))
          })
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              logger.info("Current optimization complete.")
              logger.info("")
              mainActorRef ! BacktestChartResponseMessage()
              optimizerActorRef ! CloseBacktesterMessage()
              Behaviors.stopped

            case Failure(e) =>
              logger.error("Exception received during optimization:" + e)
              mainActorRef ! BacktestChartResponseMessage()
              optimizerActorRef ! CloseBacktesterMessage()
              Behaviors.stopped
          }

      case _ =>
        context.log.warn("Received unknown message in AbstractMainOptimizerActor of type: " + message.getClass)

    this
}


