package ch.xavier
package backtesting.actors.affinement

import backtesting.TVLocators.{RANGE_FILTER_MULTIPLIER, RANGE_FILTER_PERIOD, USE_RANGE_FILTER}
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
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
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForRangeFiltering()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for range filtering affinement")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId, evaluationParameter = "profitability")
      case _ =>
        context.log.warn("Received unknown message in RangeFilterBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForRangeFiltering(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(StrategyParameter(USE_RANGE_FILTER, "false")))

    (5 to 75 by 5).map(multiplier => {
      (5 to 150 by 5).map(period => {
        parametersList.addOne(List(
          StrategyParameter(USE_RANGE_FILTER, "true"),
          StrategyParameter(RANGE_FILTER_PERIOD, period.toString),
          StrategyParameter(RANGE_FILTER_MULTIPLIER, (multiplier / 10.0).toString)
        ))
      })
    })

    parametersList.toList
}
