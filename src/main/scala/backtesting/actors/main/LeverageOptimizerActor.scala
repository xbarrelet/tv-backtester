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
//    addParametersForSLTrailing(),
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
