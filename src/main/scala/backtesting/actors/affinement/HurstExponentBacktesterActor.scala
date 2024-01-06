package ch.xavier
package backtesting.actors.affinement

import backtesting.TVLocatorsXpath.*
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object HurstExponentBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new HurstExponentBacktesterActor(context))
}

private class HurstExponentBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("HurstExponentBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForHurstExponent()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for Hurst exponent affinement")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId, evaluationParameter = "profitability")
      case _ =>
        context.log.warn("Received unknown message in HurstExponentBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForHurstExponent(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    parametersList.addOne(List(ParametersToTest(useHurstExponentCheckboxXPath, "false", "check")))
    parametersList.addOne(List(ParametersToTest(hurstTypeSelectXPath, "Trending Market", "selectOption")))

    (5 to 150).map(i => {
      parametersList.addOne(List(
        ParametersToTest(useHurstExponentCheckboxXPath, "true", "check"),
        ParametersToTest(hurstExponentLengthXPath, (i / 10.0).toString, "fill"),
        ParametersToTest(hurstExponentMTFXCheckboxPath, "false", "check")
      ))
      parametersList.addOne(List(
        ParametersToTest(useHurstExponentCheckboxXPath, "true", "check"),
        ParametersToTest(hurstExponentLengthXPath, (i / 10.0).toString, "fill"),
        ParametersToTest(hurstExponentMTFXCheckboxPath, "true", "check")
      ))
    })

    parametersList.toList
}
