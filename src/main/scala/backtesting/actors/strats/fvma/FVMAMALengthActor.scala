package ch.xavier
package backtesting.actors.strats.fvma

import backtesting.actors.AbstractBacktesterBehavior
import backtesting.parameters.StrategyParameter
import backtesting.parameters.TVLocators.{FVMA_ADX_LENGTH, FVMA_MA_LENGTH}
import backtesting.{BacktestSpecificPartMessage, Message}

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object FVMAMALengthActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new FVMAMALengthActor(context))
}

private class FVMAMALengthActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("FVMAMALengthActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message], chartId: String) =>
        val parametersTuplesToTest: List[List[StrategyParameter]] =
          addParameters()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for FVMA MA length")

        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
      case _ =>
        context.log.warn("Received unknown message in FVMAMALengthActor of type: " + message.getClass)

    this


  private def addParameters(): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (1 to 50).map(i => {
      parametersList.addOne(List(
        StrategyParameter(FVMA_MA_LENGTH, i.toString)
      ))
    })

    parametersList.toList
}
  