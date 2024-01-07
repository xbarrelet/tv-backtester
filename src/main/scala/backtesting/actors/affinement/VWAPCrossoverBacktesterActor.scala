package ch.xavier
package backtesting.actors.affinement

import backtesting.TVLocatorsXpath.*
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ch.xavier.backtesting.{BacktestSpecificPartMessage, Message}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object VWAPCrossoverBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new VWAPCrossoverBacktesterActor(context))
}

private class VWAPCrossoverBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("VWAPCrossoverBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForVWAPCrossover()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for vWap crossover affinement")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId, evaluationParameter = "profitability")
      case _ =>
        context.log.warn("Received unknown message in VWAPCrossoverBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForVWAPCrossover(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    parametersList.addOne(List(ParametersToTest(useVWapCrossoverCheckboxXPath, "false", "check")))

    (5 to 50).map(length => {
      parametersList.addOne(List(
        ParametersToTest(useVWapCrossoverCheckboxXPath, "true", "check"),
        ParametersToTest(rangeFilteringPeriodXPath, length.toString, "fill"),
      ))
    })

    parametersList.toList
}
