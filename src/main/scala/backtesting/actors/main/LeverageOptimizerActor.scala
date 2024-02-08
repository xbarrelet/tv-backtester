package ch.xavier
package backtesting.actors.main

import backtesting.Message
import backtesting.actors.AbstractMainOptimizerActor
import backtesting.parameters.StrategyParameter
import backtesting.parameters.TVLocator.*

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object LeverageOptimizerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new LeverageOptimizerActor(context))
}


private class LeverageOptimizerActor(context: ActorContext[Message]) extends AbstractMainOptimizerActor(context) {
  val logger: Logger = LoggerFactory.getLogger("LeverageOptimizerActor")
  evaluationParameter = "netProfit"
  

  val parametersLists: List[List[List[StrategyParameter]]] = List(
    addParametersForLeverage()
  )


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
