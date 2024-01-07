package ch.xavier
package backtesting.actors.takeProfit

import backtesting.TVLocatorsXpath.*
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ch.xavier.backtesting.{BacktestSpecificPartMessage, Message}
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
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForTPRRLong()
            ::: addParametersForTPFixedPercentLong()
        //            ::: addParametersForTPPipsLong()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for TP Long optimisation")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in TPLongBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForTPRRLong(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 75).map(i => {
      parametersList.addOne(List(
        ParametersToTest(takeProfitTypeSelectorXPath, "R:R", "selectOption"),
        ParametersToTest(rrProfitFactorLongXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList

  private def addParametersForTPFixedPercentLong(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 150).map(i => {
      parametersList.addOne(List(
        ParametersToTest(takeProfitTypeSelectorXPath, "Fixed Percent", "selectOption"),
        ParametersToTest(fixedPercentTPLongXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList


  private def addParametersForTPPipsLong(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (50 to 300).map(i => {
      if i % 5 == 0 then
        parametersList.addOne(List(
          ParametersToTest(takeProfitTypeSelectorXPath, "PIPS", "selectOption"),
          ParametersToTest(fixedPercentTPLongXPath, i.toString, "fill")))
    })

    parametersList.toList
}
