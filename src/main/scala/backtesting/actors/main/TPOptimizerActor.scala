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
import scala.math.BigDecimal.double2bigDecimal

object TPOptimizerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new TPOptimizerActor(context))
}

private class TPOptimizerActor(context: ActorContext[Message]) extends AbstractMainOptimizerActor(context) {
  val logger: Logger = LoggerFactory.getLogger("TPOptimizerActor")
  val stepName = "Take Profit"
    evaluationParameter = "profitFactor"


  var parametersListsBuffer: ListBuffer[List[StrategyParameter]] = ListBuffer()
  // the general consensus is that having a different risk-to-reward ratio for long and short positions can lead to overfitting the strategy,
  // which often results in poor performance during forward testing. It might be worth adjusting the settings to keep the RR more consistent between longs and shorts for better overall results.

  // From what I see in the successful strats they all use RR, don't use the fixed percents

  (0.1 to 7.5 by 0.1).map(value => {
    // TODO: Should I optimize the long first, then the short both being R:R? I could make 2 TPOptimizer in order to save the best result each time
    parametersListsBuffer.addOne(List(StrategyParameter(TP_TYPE, "R:R"), StrategyParameter(TP_SHORT_RR, value.toString()), StrategyParameter(TP_LONG_RR, value.toString())))
//    parametersListsBuffer.addOne(List(StrategyParameter(TP_TYPE, "R:R"), StrategyParameter(TP_LONG_RR, value.toString())))
  })

//  (0.1 to 15.0 by 0.1).map(value => {
//    parametersListsBuffer.addOne(List(List(StrategyParameter(TP_TYPE, "Fixed Percent"), StrategyParameter(TP_SHORT_FIXED_PERCENTS, value.toString()))))
//    parametersListsBuffer.addOne(List(List(StrategyParameter(TP_TYPE, "Fixed Percent"), StrategyParameter(TP_LONG_FIXED_PERCENTS, value.toString()))))
//  })

  val parametersLists: List[List[List[StrategyParameter]]] = List(parametersListsBuffer.toList)
}
