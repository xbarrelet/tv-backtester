package ch.xavier
package backtesting.actors.takeProfit

import backtesting.TVLocatorsXpath.{atrTLMultiplierXPath, trailingLossCheckboxXPath, trailingTPCheckboxXPath, whenToActivateTrailingXPath}
import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import ch.xavier.backtesting.{BacktestSpecificPartMessage, Message}
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
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForMultiTP()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for mnulti TP optimization")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in MultiTPBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForMultiTP(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()


    parametersList.toList
}
