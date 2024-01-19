package ch.xavier
package backtesting.actors.main

import backtesting.Message
import backtesting.actors.AbstractMainOptimizerActor
import backtesting.parameters.StrategyParameter
import backtesting.parameters.TVLocator.TEST

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.slf4j.{Logger, LoggerFactory}

object TestParametersActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new TestParametersActor(context))
}

private class TestParametersActor(context: ActorContext[Message]) extends AbstractMainOptimizerActor(context) {
  val logger: Logger = LoggerFactory.getLogger("TestParametersActor")

  val parametersLists: List[List[List[StrategyParameter]]] = List(
    parametersFactory.getParameters(TEST, 1, 1)
  )
}
