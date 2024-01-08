package ch.xavier
package backtesting.actors.takeProfit

import backtesting.TVLocators.{TP3_LEVEL, TP3_PERCENTS}
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object MultiTPBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new MultiTPBacktesterActor(context))
}

private class MultiTPBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("MultiTPBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParametersForMultiTP()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for mnulti TP optimization")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in MultiTPBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForMultiTP(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    //TODO: There doesn't seem to be a SL set after TP1 is hit, more profitable with it as it goes down a lot and then comes back to TP2 and TP3?s For now not used

    parametersList.addOne(List(
      StrategyParameter(TP3_PERCENTS, "100"),
      StrategyParameter(TP3_LEVEL, "100")
    ))


    parametersList.toList
}
