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
  implicit val timeout: Timeout = 1800.seconds
  def logger: Logger

  private val backtestersSpawner: ActorRef[Message] = context.spawn(BacktestersSpawnerActor(), "backtesters-spawner-actor")
  private val results: ListBuffer[BacktestingResultMessage] = ListBuffer()


  def optimizeParameters(parametersCombinationToTest: List[List[ParametersToTest]], mainActorRef: ActorRef[Message], chartId: String, evaluationParameter: String = "profitability"): Unit = {
    Source(parametersCombinationToTest)
      .throttle(1, Random.between(2500, 5000).millis)
      .mapAsync(sys.env("CRAWLERS_NUMBER").toInt)(parametersToTest => {
        backtestersSpawner ? (myRef => BacktestMessage(parametersToTest, myRef, chartId: String))
      })
      .map(_.asInstanceOf[BacktestingResultMessage])
      .map(result => logResults(result))
      .filter(_.closedTradesNumber > 100)
      .filter(_.maxDrawdownPercentage < 30)
      .filter(_.netProfitsPercentage > 5)
      .map(results.append)
      .runWith(Sink.last)
      .onComplete {
        case Success(result) =>
          if results.isEmpty then
            logger.info("No positive result received during the last optimization.")
            mainActorRef ! BacktestChartResponseMessage()
            Behaviors.stopped

          var sortedResults: List[BacktestingResultMessage] = List[BacktestingResultMessage]()
          if evaluationParameter.eq("profitFactor") then
            sortedResults = results.sortWith(_.profitFactor > _.profitFactor).toList
          else if evaluationParameter.eq("profitability") then
            sortedResults = results.sortWith(_.profitabilityPercentage > _.profitabilityPercentage).toList
          else
            sortedResults = results.sortWith(_.netProfitsPercentage > _.netProfitsPercentage).toList

          logBestResults(sortedResults)

          val saveResultFuture: Future[Message] = backtestersSpawner ? (myRef => SaveParametersMessage(sortedResults.head.parameters, myRef))
          saveResultFuture.onComplete {
            case Success(_) =>
              backtestersSpawner ! CloseBacktesterMessage()
              mainActorRef ! BacktestChartResponseMessage()
              Behaviors.stopped

            case Failure(e) =>
              logger.error("Exception received trying to save the best parameters:" + e)
              backtestersSpawner ! CloseBacktesterMessage()
              mainActorRef ! BacktestChartResponseMessage()
              Behaviors.stopped
          }

        case Failure(e) =>
          if !e.getClass.getSimpleName.eq("NoSuchElementException") then //No result passed the filters
            logger.error("Exception received in optimizeParameters:" + e.printStackTrace())

          mainActorRef ! BacktestChartResponseMessage()
          Behaviors.stopped
      }
  }

  private def logBestResults(sortedResults: List[BacktestingResultMessage]): Unit = {
    logger.info("")
    logger.info(s"Best 50 on ${sortedResults.size} results sorted by profit factor:")

    for result <- sortedResults.take(50) do
      logger.info(s"Profit factor:${result.profitFactor} - net profit:${result.netProfitsPercentage}% - closed trades:${result.closedTradesNumber} - profitability:${result.profitabilityPercentage} - parameters:${result.parameters.map(_.value)}")
    logger.info("")
  }

  private def logResults(result: BacktestingResultMessage) = {
    if result.closedTradesNumber == 0 then
      logger.info("No positive result received for parameters: " + result.parameters.map(_.value))
    else
      logger.info("Result received for parameters: " + result.parameters.map(_.value) + s" with details: Profit factor:${result.profitFactor} - net profit:${result.netProfitsPercentage}% - closed trades:${result.closedTradesNumber} - profitability:${result.profitabilityPercentage}")

    result
  }
}
