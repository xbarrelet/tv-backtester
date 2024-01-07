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

object DeadZoneV5SensitivityActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new DeadZoneV5SensitivityActor(context))
}

private class DeadZoneV5SensitivityActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("DeadZoneV5SensitivityActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForSensitivity()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadzoneV5 sensitivity")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5SensitivityActor of type: " + message.getClass)

    this


  private def addParametersForSensitivity(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (1 to 600).map(i => {
      if i % 10 == 0 then
        parametersList.addOne(List(
          ParametersToTest(sensitivityXPath, i.toString, "fill")
        ))
    })

    parametersList.toList
}
  