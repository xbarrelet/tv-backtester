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

object DeadZoneV5SlowEMAActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new DeadZoneV5SlowEMAActor(context))
}

private class DeadZoneV5SlowEMAActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("DeadZoneV5SlowEMAActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForSlowEMA()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadZoneV5 slow EMA")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5SensitivityActor of type: " + message.getClass)

    this


  private def addParametersForSlowEMA(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (50 to 300).map(i => {
      if i % 5 == 0 then
        parametersList.addOne(List(
          ParametersToTest(slowEMALengthXPath, i.toString, "fill")
        ))
    })

    parametersList.toList
}
  