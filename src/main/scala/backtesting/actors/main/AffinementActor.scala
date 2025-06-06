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
  val stepName = "Affinement"
  evaluationParameter = "profitFactor"

  
  val parametersLists: List[List[List[StrategyParameter]]] = List(
    addParametersForFixedMAsOptions(),

    addParametersForVolumeConfirmationTFDILookback(),
    addParametersForVolumeConfirmationMMALength(),
    addParametersForVolumeConfirmationNLength(),

    addParametersForHurstExponent(),
    addParametersForRangeFiltering(),
    addParametersForVWAPCrossover(),
    //    addParametersForSLTrailing(),
    addParametersForMaxSL(),
    addParametersForMinTp(),
  )


  private def addParametersForVolumeConfirmationTFDILookback(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(StrategyParameter(USE_VOLUME_CONFIRMATION, "false")))

    (1 to 25).map(length => {
      parametersList.addOne(List(
        StrategyParameter(USE_VOLUME_CONFIRMATION, "true"),
        StrategyParameter(TDFI_LOOKBACK_LENGTH, length.toString),
      ))
    })

    parametersList.toList


  private def addParametersForVolumeConfirmationMMALength(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(StrategyParameter(USE_VOLUME_CONFIRMATION, "false")))

    (1 to 100).map(length => {
      parametersList.addOne(List(
        StrategyParameter(USE_VOLUME_CONFIRMATION, "true"),
        StrategyParameter(MMA_LENGTH, length.toString),
      ))
    })

    parametersList.toList


  private def addParametersForVolumeConfirmationNLength(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(StrategyParameter(USE_VOLUME_CONFIRMATION, "false")))

    (1 to 100).map(length => {
      parametersList.addOne(List(
        StrategyParameter(USE_VOLUME_CONFIRMATION, "true"),
        StrategyParameter(N_LENGTH, length.toString),
      ))
    })

    parametersList.toList



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
      (5 to 125 by 5).map(period => {
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

  private def addParametersForMaxSL(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (10 to 15).map(percent => {
      parametersList.addOne(List(
        StrategyParameter(MAX_SL_PERCENT, (percent / 10.0).toString)
      ))
    })

    parametersList.toList

  
  private def addParametersForMinTp(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (5 to 30).map(percent => {
      parametersList.addOne(List(
        StrategyParameter(MIN_TP_PERCENT, (percent / 10.0).toString)
      ))
    })

    parametersList.toList
}
