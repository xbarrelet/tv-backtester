package ch.xavier
package backtesting.actors.strats.deadzonev5

import ch.xavier.backtesting.parameters.TVLocators.DEADZONE_BB_STDEV_MULTIPLIER
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object DeadZoneV5BBStdDeviationActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new DeadZoneV5BBStdDeviationActor(context))
}

private class DeadZoneV5BBStdDeviationActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("DeadZoneV5BBStdDeviationActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForBBStdDeviation()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadzoneV5 BB std dev")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in DeadZoneV5BBStdDeviationActor of type: " + message.getClass)

    this


  private def addParametersForBBStdDeviation(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (1 to 100).map(i => {
      parametersList.addOne(List(
        StrategyParameter(DEADZONE_BB_STDEV_MULTIPLIER, (i / 10.0).toString)
      ))
    })

    parametersList.toList
}
  