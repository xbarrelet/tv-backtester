package ch.xavier
package backtesting.actors.affinement

import backtesting.TVLocators.{HURST_EXP_LENGTH, USE_HURST_EXP, USE_HURST_EXP_MTF}
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object HurstExponentBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new HurstExponentBacktesterActor(context))
}

private class HurstExponentBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("HurstExponentBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForHurstExponent()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for Hurst exponent affinement")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId, evaluationParameter = "profitability")
      case _ =>
        context.log.warn("Received unknown message in HurstExponentBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForHurstExponent(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    parametersList.addOne(List(
      StrategyParameter(USE_HURST_EXP, "false")
    ))

    (5 to 150).map(i => {
      parametersList.addOne(List(
        StrategyParameter(USE_HURST_EXP, "true"),
        StrategyParameter(HURST_EXP_LENGTH, (i / 10.0).toString),
        StrategyParameter(USE_HURST_EXP_MTF, "false")
      ))
      parametersList.addOne(List(
        StrategyParameter(USE_HURST_EXP, "true"),
        StrategyParameter(HURST_EXP_LENGTH, (i / 10.0).toString),
        StrategyParameter(USE_HURST_EXP_MTF, "true")
      ))
    })

    parametersList.toList
}
