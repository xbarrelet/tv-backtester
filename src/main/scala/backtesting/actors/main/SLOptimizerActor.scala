package ch.xavier
package backtesting.actors.main

import backtesting.Message
import backtesting.actors.AbstractMainOptimizerActor
import backtesting.parameters.StrategyParameter
import backtesting.parameters.TVLocator.*

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.slf4j.{Logger, LoggerFactory}

object SLOptimizerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new SLOptimizerActor(context))
}

private class SLOptimizerActor(context: ActorContext[Message]) extends AbstractMainOptimizerActor(context) {
  val logger: Logger = LoggerFactory.getLogger("SLOptimizerActor")
  evaluationParameter = "profitFactor"

  val parametersLists: List[List[List[StrategyParameter]]] = List(
    // ATR is more representative of what someone would do without any knowledge of the market
//    parametersFactory.getParameters(SL_SHORT_FIXED_PERCENTS, 0.1, 15.0, step = 0.1, initialParameter = StrategyParameter(SL_TYPE, "Fixed Percent")),
//    parametersFactory.getParameters(SL_LONG_FIXED_PERCENTS, 0.1, 15.0, step = 0.1, initialParameter = StrategyParameter(SL_TYPE, "Fixed Percent")),

    parametersFactory.getParameters(SL_ATR_MULTIPLIER, 0.1, 15.0, step = 0.1, initialParameter = StrategyParameter(SL_TYPE, "ATR")),
    parametersFactory.getParameters(SL_ATR_SWING_LOOKBACK, 0.1, 15.0, step = 0.1, initialParameter = StrategyParameter(SL_TYPE, "ATR"))
  )
}
