package ch.xavier
package backtesting.actors.strats.deadzonev5

import backtesting.TVLocators.DEADZONE_BB_CHANNEL_LENGTH
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object DeadZoneV5BBChannelActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new DeadZoneV5BBChannelActor(context))
}

private class DeadZoneV5BBChannelActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("DeadZoneV5BBChannelActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForBBChannelLength()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadzoneV5 BB channel length")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5BBChannelActor of type: " + message.getClass)

    this


  private def addParametersForBBChannelLength(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (6 to 40 by 2).map(i => {
      parametersList.addOne(List(
        StrategyParameter(DEADZONE_BB_CHANNEL_LENGTH, i.toString)
      ))
    })

    parametersList.toList
}
  