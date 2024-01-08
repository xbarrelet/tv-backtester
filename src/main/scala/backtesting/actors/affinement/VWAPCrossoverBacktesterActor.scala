package ch.xavier
package backtesting.actors.affinement

import backtesting.TVLocators.{USE_VWAP_CROSSOVER, VWAP_LENGTH}
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object VWAPCrossoverBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new VWAPCrossoverBacktesterActor(context))
}

private class VWAPCrossoverBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("VWAPCrossoverBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForVWAPCrossover()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for vWap crossover affinement")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId, evaluationParameter = "profitability")
      case _ =>
        context.log.warn("Received unknown message in VWAPCrossoverBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForVWAPCrossover(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(StrategyParameter(USE_VWAP_CROSSOVER, "false")))

    (5 to 50).map(length => {
      parametersList.addOne(List(
        StrategyParameter(USE_VWAP_CROSSOVER, "true"),
        StrategyParameter(VWAP_LENGTH, length.toString),
      ))
    })

    parametersList.toList
}
