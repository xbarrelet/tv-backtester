package ch.xavier
package backtesting

import Application.{executionContext, system}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Random, Success}

object MainBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new MainBacktesterActor(context))
}

class MainBacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 300.seconds
  private val logger: Logger = LoggerFactory.getLogger("MainBacktesterActor")

  private val backtestersSpawner: ActorRef[Message] = context.spawn(BacktestersSpawnerActor(), s"backtesters-spawner-actor")
  private val results: ListBuffer[BacktestingResultMessage] = ListBuffer()

  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestChartMessage() =>
        context.log.info(s"Starting backtesting for chart ${sys.env("CHART_ID")}")

        var parametersTuplesToTest: List[List[ParametersToTest]] = List()
        val resultsList: ListBuffer[BacktestingResultMessage] = ListBuffer()

        parametersTuplesToTest ++= addParametersForTPRR()

        RunBacktesting(parametersTuplesToTest)

      case _ =>
        context.log.warn("Received unknown message in MainBacktesterActor of type: " + message.getClass)

      this


  private def RunBacktesting(parametersCombinationToTest: List[List[ParametersToTest]]): Unit = {
    Source(parametersCombinationToTest)
      .throttle(1, Random.between(1500, 2500).millis)
      .mapAsync(4)(parametersToTest => {
        backtestersSpawner ? (myRef => BacktestMessage(parametersToTest, myRef))
      })
      .map(_.asInstanceOf[BacktestingResultMessage])
      .filter(_.closedTradesNumber > 30)
      .map(results.append)
      .runWith(Sink.last)
      .onComplete {
        case Success(result) =>
          val sortedResults = results.sortWith(_.profitFactor > _.profitFactor).toList

          logger.info("")
          logger.info("Best 50 results sorted by profit factor:")

          for result <- sortedResults.take(50) do
            logger.info("Profit factor: " + result.profitFactor + " - " + result.parameters)

          logger.info("")
          backtestersSpawner ! SaveParametersMessage(sortedResults.head.parameters)

        case Failure(e) =>
          logger.error("Exception received in RunBacktesting:" + e)
      }
  }

  private def addParametersForTPRR(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    val profitsSelector = "xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[28]/div/span"
    val profitFactorLongXPath: String = "xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[69]/div/span/span[1]/input"
    val profitFactorShortXPath: String = "xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[70]/div/span/span[1]/input"

    (5 to 15).map(i => {
//    (5 to 50).map(i => {
      parametersList.addOne(
        List(ParametersToTest(profitsSelector, "R:R", "selectTakeProfit"), ParametersToTest(profitFactorLongXPath, (i/10.0).toString, "fill")))
    })

//    (5 to 50).map(i => {
//      parametersList.addOne(
//        List(ParametersToTest(profitsSelector, "R:R", "selectTakeProfit"), ParametersToTest(profitFactorShortXPath, (i / 10.0).toString, "fill")))
//    })

    parametersList.toList
}
