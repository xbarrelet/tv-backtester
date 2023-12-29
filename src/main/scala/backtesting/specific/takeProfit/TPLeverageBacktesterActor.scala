package ch.xavier
package backtesting.specific.takeProfit

import TVLocators.*
import backtesting.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object TPLeverageBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new TPLeverageBacktesterActor(context))
}

private class TPLeverageBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("TPLeverageBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message]) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] = addParametersForLeverage()
          
        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for TP leverage optimisation")
        
        optimizeParameters(parametersTuplesToTest, mainActorRef)
      case _ =>
        context.log.warn("Received unknown message in TPLeverageBacktesterActor of type: " + message.getClass)

      this


  private def addParametersForLeverage(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    parametersList.addOne(List(ParametersToTest(dynamicLeverageCheckboxXPath, "true", "check")))

    (1 to 50).map(leverage => {
      parametersList.addOne(List(
        ParametersToTest(dynamicLeverageCheckboxXPath, "false", "check"),
        ParametersToTest(leverageAmountXPath, leverage.toString, "fill")))
    })

    parametersList.toList
}
