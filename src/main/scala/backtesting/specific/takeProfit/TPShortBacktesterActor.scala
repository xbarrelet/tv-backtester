package ch.xavier
package backtesting.specific.takeProfit

import TVLocators.*
import backtesting.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

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
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message]) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] = 
          addParametersForTPRRShort() 
            ::: addParametersForTPFixedPercentShort()
//            ::: addParametersForTPPipsShort()
          
        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for TP Short optimisation")
        
        optimizeParameters(parametersTuplesToTest, mainActorRef)
      case _ =>
        context.log.warn("Received unknown message in TPShortBacktesterActor of type: " + message.getClass)

      this
  

  private def addParametersForTPRRShort(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 50).map(i => {
      parametersList.addOne(List(
          ParametersToTest(takeProfitTypeSelectorXPath, "R:R", "selectTakeProfit"),
          ParametersToTest(rrProfitFactorShortXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList

  private def addParametersForTPFixedPercentShort(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 150).map(i => {
      parametersList.addOne(List(
          ParametersToTest(takeProfitTypeSelectorXPath, "Fixed Percent", "selectTakeProfit"),
          ParametersToTest(fixedPercentTPShortXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList


  private def addParametersForTPPipsShort(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (50 to 300).map(i => {
      if i % 5 == 0 then
        parametersList.addOne(List(
          ParametersToTest(takeProfitTypeSelectorXPath, "PIPS", "selectTakeProfit"),
          ParametersToTest(fixedPercentTPShortXPath, i.toString, "fill")))
    })

    parametersList.toList
}
