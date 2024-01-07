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

object RangeFilterBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new RangeFilterBacktesterActor(context))
}

private class RangeFilterBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("RangeFilterBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForRangeFiltering()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for range filtering affinement")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId, evaluationParameter = "profitability")
      case _ =>
        context.log.warn("Received unknown message in RangeFilterBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForRangeFiltering(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    parametersList.addOne(List(ParametersToTest(useRangeFilteringCheckboxXPath, "false", "check")))

    (5 to 75).map(multiplier => {
      (5 to 150).map(period => {
        if multiplier % 5 == 0 && period % 5 == 0 then
          parametersList.addOne(List(
            ParametersToTest(useRangeFilteringCheckboxXPath, "true", "check"),
            ParametersToTest(rangeFilteringPeriodXPath, period.toString, "fill"),
            ParametersToTest(rangeFilteringMultiplierXPath, (multiplier / 10.0).toString, "fill")
          ))
      })
    })

    parametersList.toList
}
