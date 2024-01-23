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
    addParametersForSLTrailing()
  )


  private def addParametersForSLTrailing(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(
      StrategyParameter(USE_TRAILING_LOSS, "false"),
      StrategyParameter(USE_TRAILING_TP, "false"))
    )

    List("Instant", "After Hit TP 1").map(condition => {
      //    List("Instant", "After Hit TP 1", "After Hit TP 2").map(condition => {
      (1 to 3).map(multiplier => {
        (1 to 75).map(threshold => {
          parametersList.addOne(List(
            StrategyParameter(USE_TRAILING_LOSS, "true"),
            StrategyParameter(USE_TRAILING_TP, "false"),
            StrategyParameter(TRAILING_ACTIVATION, condition),
            StrategyParameter(TRAILING_LOSS_THRESHOLD, (threshold / 10.0).toString),
            StrategyParameter(TRAILING_LOSS_ATR_MULTIPLIER, multiplier.toString)
          ))
          parametersList.addOne(List(
            StrategyParameter(USE_TRAILING_LOSS, "true"),
            StrategyParameter(USE_TRAILING_TP, "true"),
            StrategyParameter(TRAILING_ACTIVATION, condition),
            StrategyParameter(TRAILING_LOSS_THRESHOLD, (threshold / 10.0).toString),
            StrategyParameter(TRAILING_LOSS_ATR_MULTIPLIER, multiplier.toString)
          ))
        })
      })
    })

    parametersList.toList
}
