package ch.xavier
package backtesting

import Application.{executionContext, system}
import backtesting.parameters.ParametersToTest

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.slf4j.Logger

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Random, Success}

abstract class AbstractBacktesterBehavior(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 300.seconds
  def logger: Logger

  private val backtestersSpawner: ActorRef[Message] = context.spawn(BacktestersSpawnerActor(), "backtesters-spawner-actor")
  private val results: ListBuffer[BacktestingResultMessage] = ListBuffer()


  def optimizeParameters(parametersCombinationToTest: List[List[ParametersToTest]], mainActorRef: ActorRef[Message], chartId: String, evaluationParameter: String = "profitFactor"): Unit = {
    Source(parametersCombinationToTest)
      .throttle(1, Random.between(2500, 5000).millis)
      .mapAsync(sys.env("CRAWLERS_NUMBER").toInt)(parametersToTest => {
        backtestersSpawner ? (myRef => BacktestMessage(parametersToTest, myRef, chartId: String))
      })
      .map(_.asInstanceOf[BacktestingResultMessage])
//      .map(result => logResults(result))
      .filter(_.closedTradesNumber > 100)
      .filter(_.maxDrawdownPercentage < 30)
      .map(results.append)
      .runWith(Sink.last)
      .onComplete {
        case Success(result) =>
          if results.isEmpty then
            logger.info("No positive result received.")
            mainActorRef ! BacktestChartResponseMessage()
            Behaviors.stopped

          var sortedResults: List[BacktestingResultMessage] = List[BacktestingResultMessage]()
          if evaluationParameter.eq("profitFactor") then
            sortedResults = results.sortWith(_.profitFactor > _.profitFactor).toList
          else
            sortedResults = results.sortWith(_.netProfitsPercentage > _.netProfitsPercentage).toList

          logger.info("")
          logger.info(s"Best 50 on ${sortedResults.size} results sorted by profit factor:")

          for result <- sortedResults.take(50) do
            logger.info(s"Profit factor:${result.profitFactor} - net profit:${result.netProfitsPercentage}% - closed trades:${result.closedTradesNumber} - parameters:${result.parameters.map(_.value)}")
          logger.info("")

          val saveResultFuture: Future[Message] = backtestersSpawner ? (myRef => SaveParametersMessage(sortedResults.head.parameters, myRef))
          saveResultFuture.onComplete {
            case Success(_) =>
              mainActorRef ! BacktestChartResponseMessage()
              Behaviors.stopped
            case Failure(e) =>
              logger.error("Exception received trying to save the best parameters:" + e)
              mainActorRef ! BacktestChartResponseMessage()
              Behaviors.stopped
          }

        case Failure(e) =>
          logger.error("Exception received in RunBacktesting:" + e.printStackTrace())
          mainActorRef ! BacktestChartResponseMessage()
          Behaviors.stopped
      }
  }

  private def logResults(result: BacktestingResultMessage) = {
    if result.closedTradesNumber == 0 then
      logger.info("No positive result received for parameters: " + result.parameters.map(_.value))
    else
      logger.info("Result received for parameters: " + result.parameters.map(_.value) + s" with details: Profit factor:${result.profitFactor} - net profit:${result.netProfitsPercentage}% - closed trades:${result.closedTradesNumber}")
    result
  }
}
