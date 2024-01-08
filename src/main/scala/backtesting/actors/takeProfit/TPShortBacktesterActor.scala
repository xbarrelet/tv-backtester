package ch.xavier
package backtesting.actors.takeProfit

import backtesting.TVLocators.{TP_SHORT_FIXED_PERCENTS, TP_SHORT_RR, TP_TYPE}
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object TPShortBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new TPShortBacktesterActor(context))
}

private class TPShortBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("TPShortBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForTPRRShort()
            ::: addParametersForTPFixedPercentShort()
        //            ::: addParametersForTPPipsShort()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for TP Short optimisation")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in TPShortBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForTPRRShort(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (5 to 75).map(i => {
      parametersList.addOne(List(
        StrategyParameter(TP_TYPE, "R:R"),
        StrategyParameter(TP_SHORT_RR, (i / 10.0).toString)))
    })

    parametersList.toList

  private def addParametersForTPFixedPercentShort(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (5 to 150).map(i => {
      parametersList.addOne(List(
        StrategyParameter(TP_TYPE, "Fixed Percent"),
        StrategyParameter(TP_SHORT_FIXED_PERCENTS, (i / 10.0).toString)))
    })

    parametersList.toList


  //  private def addParametersForTPPipsShort(): List[List[StrategyParameter]] =
  //    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()
  //
  //    (50 to 300).map(i => {
  //      if i % 5 == 0 then
  //        parametersList.addOne(List(
  //          StrategyParameter(takeProfitTypeSelectorXPath, "PIPS"),
  //          StrategyParameter(fixedPercentTPShortXPath, i.toString)))
  //    })
  //
  //    parametersList.toList
}
