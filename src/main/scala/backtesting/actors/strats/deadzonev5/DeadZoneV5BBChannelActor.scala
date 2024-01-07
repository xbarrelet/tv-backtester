package ch.xavier
package backtesting.actors.strats.deadzonev5

import backtesting.TVLocatorsXpath.*
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ch.xavier.backtesting.{BacktestSpecificPartMessage, Message}
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
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForBBChannelLength()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadzoneV5 BB channel length")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5BBChannelActor of type: " + message.getClass)

    this


  private def addParametersForBBChannelLength(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (6 to 40).map(i => {
      if i % 2 == 0 then
        parametersList.addOne(List(
          ParametersToTest(bbChannelLengthXPath, i.toString, "fill")
        ))
    })

    parametersList.toList
}
  