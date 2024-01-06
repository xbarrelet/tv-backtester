package ch.xavier
package backtesting.actors.strats.deadzonev5

import backtesting.TVLocatorsXpath.*
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

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
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForDeadzone()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadzoneV5 deadzone")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5DeadzoneActor of type: " + message.getClass)

    this


  private def addParametersForDeadzone(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (1 to 150).map(i => {
      if i % 2 == 0 then
        parametersList.addOne(List(
          ParametersToTest(deadMultiplierXPath, (i / 10.0).toString, "fill")
        ))
    })

    parametersList.toList
}
  