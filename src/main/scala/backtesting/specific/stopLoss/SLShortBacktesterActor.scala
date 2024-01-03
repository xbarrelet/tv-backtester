package ch.xavier
package backtesting.specific.stopLoss

import TVLocators.*
import backtesting.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object SLShortBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new SLShortBacktesterActor(context))
}

private class SLShortBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("SLShortBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] =
            addParametersForSLShortFixedPercent()
//            ::: addParametersForSLShortPips()
          
        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for SL short optimisation")
        
        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in SLShortBacktesterActor of type: " + message.getClass)

      this


  private def addParametersForSLShortFixedPercent(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 125).map(i => {
      parametersList.addOne(List(
        ParametersToTest(stopLossTypeSelectorXPath, "Fixed Percent", "selectOption"),
        ParametersToTest(fixedPercentSLShortXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList


  private def addParametersForSLShortPips(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (50 to 300).map(i => {
      if i % 5 == 0 then
        parametersList.addOne(List(
          ParametersToTest(stopLossTypeSelectorXPath, "PIPS", "selectOption"),
          ParametersToTest(fixedPercentSLShortXPath, i.toString, "fill")))
    })

    parametersList.toList
}
