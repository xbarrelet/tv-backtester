package ch.xavier
package backtesting.actors.stopLoss

import ch.xavier.backtesting.parameters.TVLocators.{SL_ATR_MULTIPLIER, SL_LONG_FIXED_PERCENTS, SL_TYPE}
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object SLLongBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new SLLongBacktesterActor(context))
}

private class SLLongBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("SLLongBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForSLATR()
            ::: addParametersForSLLongFixedPercent()
        //            ::: addParametersForSLLongPips()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for SL Long optimisation")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in SLLongBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForSLATR(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (5 to 150).map(i => {
      parametersList.addOne(List(
        StrategyParameter(SL_TYPE, "ATR"),
        StrategyParameter(SL_ATR_MULTIPLIER, (i / 10.0).toString)))
    })


    parametersList.toList

  private def addParametersForSLLongFixedPercent(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (5 to 125).map(i => {
      parametersList.addOne(List(
        StrategyParameter(SL_TYPE, "Fixed Percent"),
        StrategyParameter(SL_LONG_FIXED_PERCENTS, (i / 10.0).toString)))
    })

    parametersList.toList


  //  private def addParametersForSLLongPips(): List[List[StrategyParameter]] =
  //    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()
  //
  //    (50 to 300).map(i => {
  //      if i % 5 == 0 then
  //        parametersList.addOne(List(
  //          StrategyParameter(SL_TYPE, "PIPS"),
  //          StrategyParameter(, i.toString, "fill")))
  //    })
  //
  //    parametersList.toList
}
