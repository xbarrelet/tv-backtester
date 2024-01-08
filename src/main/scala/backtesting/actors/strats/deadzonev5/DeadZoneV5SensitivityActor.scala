package ch.xavier
package backtesting.actors.strats.deadzonev5

import backtesting.TVLocators.DEADZONE_SENSITIVITY
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object DeadZoneV5SensitivityActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new DeadZoneV5SensitivityActor(context))
}

private class DeadZoneV5SensitivityActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("DeadZoneV5SensitivityActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForSensitivity()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadzoneV5 sensitivity")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5SensitivityActor of type: " + message.getClass)

    this


  private def addParametersForSensitivity(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (0 to 600 by 10).map(i => {
      parametersList.addOne(List(
        StrategyParameter(DEADZONE_SENSITIVITY, i.toString)
      ))
    })

    parametersList.toList
}
  