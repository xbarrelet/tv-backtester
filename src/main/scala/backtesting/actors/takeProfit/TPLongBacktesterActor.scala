package ch.xavier
package backtesting.actors.takeProfit

import ch.xavier.backtesting.parameters.TVLocators.{TP_LONG_FIXED_PERCENTS, TP_LONG_RR, TP_TYPE}
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object TPLongBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new TPLongBacktesterActor(context))
}

private class TPLongBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("TPLongBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForTPRRLong()
            ::: addParametersForTPFixedPercentLong()
        //            ::: addParametersForTPPipsLong()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for TP Long optimisation")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in TPLongBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForTPRRLong(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (5 to 75).map(i => {
      parametersList.addOne(List(
        StrategyParameter(TP_TYPE, "R:R"),
        StrategyParameter(TP_LONG_RR, (i / 10.0).toString)))
    })

    parametersList.toList

  private def addParametersForTPFixedPercentLong(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (5 to 150).map(i => {
      parametersList.addOne(List(
        StrategyParameter(TP_TYPE, "Fixed Percent"),
        StrategyParameter(TP_LONG_FIXED_PERCENTS, (i / 10.0).toString)))
    })

    parametersList.toList


  //  private def addParametersForTPPipsLong(): List[List[StrategyParameter]] =
  //    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()
  //
  //    (50 to 300).map(i => {
  //      if i % 5 == 0 then
  //        parametersList.addOne(List(
  //          StrategyParameter(takeProfitTypeSelectorXPath, "PIPS"),
  //          StrategyParameter(fixedPercentTPLongXPath, i.toString)))
  //    })
  //
  //    parametersList.toList
}
