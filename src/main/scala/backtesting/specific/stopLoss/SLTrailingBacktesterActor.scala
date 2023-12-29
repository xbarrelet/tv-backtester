package ch.xavier
package backtesting.specific.stopLoss

import TVLocators.{atrTLMultiplierXPath, trailingLossCheckboxXPath, trailingTPCheckboxXPath, whenToActivateTrailingXPath}
import backtesting.AbstractBacktesterBehavior
import backtesting.parameters.ParametersToTest

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

object SLTrailingBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new SLTrailingBacktesterActor(context))
}

private class SLTrailingBacktesterActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
  val logger: Logger = LoggerFactory.getLogger("SLTrailingBacktesterActor")


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestSpecificPartMessage(mainActorRef: ActorRef[Message]) =>
        val parametersTuplesToTest: List[List[ParametersToTest]] =
          addParametersForSLTrailing()

        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for SL trailing optimisation")

        optimizeParameters(parametersTuplesToTest, mainActorRef)
      case _ =>
        context.log.warn("Received unknown message in SLTrailingBacktesterActor of type: " + message.getClass)

    this


  private def addParametersForSLTrailing(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    parametersList.addOne(List(
      ParametersToTest(trailingLossCheckboxXPath, "false", "check"),
      ParametersToTest(trailingTPCheckboxXPath, "false", "check"))
    )

    List("Instant", "After Hit TP 1", "After Hit TP 2").map(condition => {
      (1 to 75).map(i => {
        parametersList.addOne(List(
          ParametersToTest(trailingLossCheckboxXPath, "true", "check"),
          ParametersToTest(trailingTPCheckboxXPath, "false", "check"),
          ParametersToTest(whenToActivateTrailingXPath, condition, "selectOption"),
          ParametersToTest(atrTLMultiplierXPath, (i / 10.0).toString, "fill"))
        )
        parametersList.addOne(List(
          ParametersToTest(trailingLossCheckboxXPath, "true", "check"),
          ParametersToTest(trailingTPCheckboxXPath, "true", "check"),
          ParametersToTest(whenToActivateTrailingXPath, condition, "selectOption"),
          ParametersToTest(atrTLMultiplierXPath, (i / 10.0).toString, "fill"))
        )
      })
    })


    parametersList.toList
}
