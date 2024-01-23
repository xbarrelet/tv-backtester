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

object TPOptimizerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new TPOptimizerActor(context))
}

private class TPOptimizerActor(context: ActorContext[Message]) extends AbstractMainOptimizerActor(context) {
  val logger: Logger = LoggerFactory.getLogger("TPOptimizerActor")


  val parametersLists: List[List[List[StrategyParameter]]] = List(
    //TODO: WRONG! You should test both sides with fixed percents and then check if the combined best parameters for both sides is better than the combined best parameters of the R:R.

    parametersFactory.getParameters(TP_SHORT_RR, 0.1, 7.5, step = 0.1, initialParameter = StrategyParameter(TP_TYPE, "R:R")),
    parametersFactory.getParameters(TP_SHORT_FIXED_PERCENTS, 0.1, 15.0, step = 0.1, initialParameter = StrategyParameter(TP_TYPE, "Fixed Percent")),

    parametersFactory.getParameters(TP_LONG_RR, 0.1, 7.5, step = 0.1, initialParameter = StrategyParameter(TP_TYPE, "R:R")),
    parametersFactory.getParameters(TP_LONG_FIXED_PERCENTS, 0.1, 15.0, step = 0.1, initialParameter = StrategyParameter(TP_TYPE, "Fixed Percent")),
  )
}
