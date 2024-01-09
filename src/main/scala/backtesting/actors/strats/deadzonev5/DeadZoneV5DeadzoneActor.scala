package ch.xavier
package backtesting.actors.strats.deadzonev5

import ch.xavier.backtesting.parameters.TVLocators.DEADZONE_DEADZONE_PARAMETER
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object DeadZoneV5DeadzoneActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new DeadZoneV5DeadzoneActor(context))
}

private class DeadZoneV5DeadzoneActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("DeadZoneV5DeadzoneActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForDeadzone()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadzoneV5 deadzone")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5DeadzoneActor of type: " + message.getClass)

    this


  private def addParametersForDeadzone(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (2 to 150 by 2).map(i => {
      parametersList.addOne(List(
        StrategyParameter(DEADZONE_DEADZONE_PARAMETER, (i / 10.0).toString)
      ))
    })

    parametersList.toList
}
  