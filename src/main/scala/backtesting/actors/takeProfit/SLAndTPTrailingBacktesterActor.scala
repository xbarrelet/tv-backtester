package ch.xavier
package backtesting.actors.takeProfit

import ch.xavier.backtesting.parameters.TVLocators.*
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object SLAndTPTrailingBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new SLAndTPTrailingBacktesterActor(context))
}

private class SLAndTPTrailingBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("SLAndTPTrailingBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForSLTrailing()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for SL and TP trailing optimization")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in SLAndTPTrailingBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForSLTrailing(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(
      StrategyParameter(USE_TRAILING_LOSS, "false"),
      StrategyParameter(USE_TRAILING_TP, "false"))
    )

    List("Instant", "After Hit TP 1").map(condition => {
//    List("Instant", "After Hit TP 1", "After Hit TP 2").map(condition => {
      (1 to 3).map(multiplier => {
        (1 to 75).map(threshold => {
          parametersList.addOne(List(
            StrategyParameter(USE_TRAILING_LOSS, "true"),
            StrategyParameter(USE_TRAILING_TP, "false"),
            StrategyParameter(TRAILING_ACTIVATION, condition),
            StrategyParameter(TRAILING_LOSS_THRESHOLD, (threshold / 10.0).toString),
            StrategyParameter(TRAILING_LOSS_ATR_MULTIPLIER, multiplier.toString)
          ))
          parametersList.addOne(List(
            StrategyParameter(USE_TRAILING_LOSS, "true"),
            StrategyParameter(USE_TRAILING_TP, "true"),
            StrategyParameter(TRAILING_ACTIVATION, condition),
            StrategyParameter(TRAILING_LOSS_THRESHOLD, (threshold / 10.0).toString),
            StrategyParameter(TRAILING_LOSS_ATR_MULTIPLIER, multiplier.toString)
          ))
        })
      })
    })


    parametersList.toList
}
