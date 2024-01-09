package ch.xavier
package backtesting.actors.takeProfit

import ch.xavier.backtesting.parameters.TVLocators.{LEVERAGE_PERCENT, USE_DYNAMIC_LEVERAGE}
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

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
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] = addParametersForLeverage()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for TP leverage optimisation")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId, evaluationParameter = "netProfitsPercentage")
      case _ =>
        context.log.warn("Received unknown message in TPLeverageBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForLeverage(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(StrategyParameter(USE_DYNAMIC_LEVERAGE, "true")))

    (1 to 50).map(leverage => {
      parametersList.addOne(List(
        StrategyParameter(USE_DYNAMIC_LEVERAGE, "false"),
        StrategyParameter(LEVERAGE_PERCENT, leverage.toString)))
    })

    parametersList.toList
}
