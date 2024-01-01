package ch.xavier
package backtesting.specific.stopLoss

import TVLocators.*
import backtesting.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

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
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message]) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForSLATR()
            ::: addParametersForSLLongFixedPercent()
//            ::: addParametersForSLLongPips()
          
        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for SL Long optimisation")
        
        optimizeParameters(parametersTuplesToTest, mainActorRef)
      case _ =>
        context.log.warn("Received unknown message in SLLongBacktesterActor of type: " + message.getClass)

      this


  private def addParametersForSLATR(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 100).map(i => {
      parametersList.addOne(List(
        ParametersToTest(stopLossTypeSelectorXPath, "ATR", "selectOption"),
        ParametersToTest(fixedPercentSLLongXPath, (i / 10.0).toString, "fill")))
    })


    parametersList.toList

  private def addParametersForSLLongFixedPercent(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 150).map(i => {
      parametersList.addOne(List(
        ParametersToTest(stopLossTypeSelectorXPath, "Fixed Percent", "selectOption"),
        ParametersToTest(fixedPercentSLLongXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList


  private def addParametersForSLLongPips(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (50 to 300).map(i => {
      if i % 5 == 0 then
        parametersList.addOne(List(
          ParametersToTest(stopLossTypeSelectorXPath, "PIPS", "selectOption"),
          ParametersToTest(fixedPercentSLLongXPath, i.toString, "fill")))
    })

    parametersList.toList
}
