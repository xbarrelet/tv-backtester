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

object AffinementActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new AffinementActor(context))
}

private class AffinementActor(context: ActorContext[Message]) extends AbstractMainOptimizerActor(context) {
  val logger: Logger = LoggerFactory.getLogger("AffinementActor")
  evaluationParameter = "profitFactor"

  
  val parametersLists: List[List[List[StrategyParameter]]] = List(
//    addParametersForFixedMAsOptions(),
    addParametersForHurstExponent(),
    addParametersForRangeFiltering(),
    addParametersForVWAPCrossover()
  )


  private def addParametersForFixedMAsOptions(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    List(
      "Off",
      "Close over/under MA5",
      "Strict Close over/under MA5",
      "5 MA (Ordered)",
      "3 MA (Ordered)",
      "3 MA (Strict)",
      "3 MA (Cross)"
    ).map(masOption => {
      parametersList.addOne(List(
        StrategyParameter(MA_TYPE, masOption)
      ))
    })

    parametersList.toList

  private def addParametersForHurstExponent(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(
      StrategyParameter(USE_HURST_EXP, "false")
    ))

    (1 to 100).map(i => {
      parametersList.addOne(List(
        StrategyParameter(USE_HURST_EXP, "true"),
        StrategyParameter(HURST_EXP_LENGTH, i.toString),
        StrategyParameter(USE_HURST_EXP_MTF, "false")
      ))
      parametersList.addOne(List(
        StrategyParameter(USE_HURST_EXP, "true"),
        StrategyParameter(HURST_EXP_LENGTH, i.toString),
        StrategyParameter(USE_HURST_EXP_MTF, "true")
      ))
    })

    parametersList.toList

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

  private def addParametersForVWAPCrossover(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(StrategyParameter(USE_VWAP_CROSSOVER, "false")))

    (5 to 50).map(length => {
      parametersList.addOne(List(
        StrategyParameter(USE_VWAP_CROSSOVER, "true"),
        StrategyParameter(VWAP_LENGTH, length.toString),
      ))
    })

    parametersList.toList
}
