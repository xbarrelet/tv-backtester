package ch.xavier
package backtesting.actors.strats.deadzonev5

import backtesting.TVLocators.*
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object DeadZoneV5AllMainParametersActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new DeadZoneV5AllMainParametersActor(context))
}

private class DeadZoneV5AllMainParametersActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("DeadZoneV5AllMainParametersActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForAllMainParameters()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadzoneV5 all main parameters optimization")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5AllMainParametersActor of type: " + message.getClass)

    this


  private def addParametersForAllMainParameters(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (0 to 600 by 100).map(sensitivity => {
      (0 to 80 by 10).map(fastEma => {
        (50 to 300 by 50).map(slowEma => {
          (5 to 45 by 10).map(bbChannelLength => {
            (0 to 10 by 5).map(bbStdDeviation => {
              (0 to 15 by 5).map(deadZone => {
                parametersList.addOne(List(
                  StrategyParameter(DEADZONE_SENSITIVITY, sensitivity.toString),
                  StrategyParameter(DEADZONE_FAST_EMA, fastEma.toString),
                  StrategyParameter(DEADZONE_SLOW_EMA, slowEma.toString),
                  StrategyParameter(DEADZONE_BB_CHANNEL_LENGTH, bbChannelLength.toString),
                  StrategyParameter(DEADZONE_BB_STDEV_MULTIPLIER, bbStdDeviation.toString),
                  StrategyParameter(DEADZONE_DEADZONE_PARAMETER, deadZone.toString),
                ))
              })
            })
          })
        })
      })
    })

    parametersList.toList
}
  