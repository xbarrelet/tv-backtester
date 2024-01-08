package ch.xavier
package backtesting.actors.affinement

import backtesting.TVLocators.MA_TYPE
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object MasAffinementBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new MasAffinementBacktesterActor(context))
}

private class MasAffinementBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("MasAffinementBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForFixedMAsOptions()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for MAs affinement")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId, evaluationParameter = "profitability")
      case _ =>
        context.log.warn("Received unknown message in MasAffinementBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForFixedMAsOptions(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    List(
      "Off",
      "Close over/under MA5",
      "Strict Close over/under MA5",
      "5 MA (Ordered)",
      "3 MA (Ordered)",
      "3 MA (Strict)",
      "3 MA (Cross)"
    ).map(masOption => {
      parametersList.addOne(List(
        StrategyParameter(MA_TYPE, masOption)
      ))
    })
    parametersList.toList
}
