package ch.xavier
package backtesting.actors.strats.deadzonev5

import ch.xavier.backtesting.parameters.TVLocators.DEADZONE_SLOW_EMA
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object DeadZoneV5SlowEMAActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new DeadZoneV5SlowEMAActor(context))
}

private class DeadZoneV5SlowEMAActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("DeadZoneV5SlowEMAActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForSlowEMA()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadZoneV5 slow EMA")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5SensitivityActor of type: " + message.getClass)

    this


  private def addParametersForSlowEMA(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (50 to 300 by 5).map(i => {
      parametersList.addOne(List(
        StrategyParameter(DEADZONE_SLOW_EMA, i.toString)
      ))
    })

    parametersList.toList
}
  