package ch.xavier
package backtesting.actors

import Application.{executionContext, system}
import backtesting.*
import backtesting.parameters.StrategyParameter

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Random, Success}

object ParametersGroupBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new ParametersGroupBacktesterActor(context))
}


private class ParametersGroupBacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 10800.seconds
  private def logger: Logger = LoggerFactory.getLogger("ParametersGroupBacktesterActor")
  private val config: BacktesterConfig.type = BacktesterConfig

  private val backtestersSpawner: ActorRef[Message] = context.spawn(BacktestersSpawnerActor(), "backtesters-spawner-actor")

  private val results: ListBuffer[BacktestingResultMessage] = ListBuffer()


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case OptimizeParametersListsMessage(parameters: List[List[StrategyParameter]], mainActorRef: ActorRef[Message], chartId: String, evaluationParameter: String) =>
        Source(parameters)
          .throttle(1, Random.between(2500, 5000).millis)
          .mapAsync(sys.env("CRAWLERS_NUMBER").toInt)(parametersToTest => {
            backtestersSpawner ? (myRef => OptimizeParametersMessage(parametersToTest, myRef, chartId: String))
          })
          .map(_.asInstanceOf[BacktestingResultMessage])
//          .map(result => logResults(result))
          .filter(_.closedTradesNumber > config.backtestingPeriodDays / 7)
          .filter(_.maxDrawdownPercentage < 20)
          .filter(_.netProfitsPercentage > 10)
          //      .filter(_.profitFactor > 1)
          .map(results.append)
          .runWith(Sink.last)
          .onComplete {
            case Success(result) =>
              if results.isEmpty then
                logger.info("No positive result received during the last optimization.")
                mainActorRef ! BacktestingResultMessage(0.0, 0, 0.0, 0.0, 0.0, List.empty)

              else
                var sortedResults: List[BacktestingResultMessage] = List[BacktestingResultMessage]()
                var isNewResultBetterThanCurrentBest = false

                if evaluationParameter.eq("profitFactor") then
                  sortedResults = results.sortBy(p => (p.profitFactor, p.profitabilityPercentage)).reverse.toList
                  if sortedResults.head.profitFactor > config.bestResult.profitFactor then
                    isNewResultBetterThanCurrentBest = true

                else if evaluationParameter.eq("profitability") then
                  sortedResults = results.sortBy(p => (p.profitabilityPercentage, p.profitFactor)).reverse.toList
                  if sortedResults.head.profitabilityPercentage > config.bestResult.profitabilityPercentage then
                    isNewResultBetterThanCurrentBest = true

                else
                  sortedResults = results.sortBy(p => (p.netProfitsPercentage, p.profitabilityPercentage)).reverse.toList

                  if sortedResults.head.netProfitsPercentage > config.bestResult.netProfitsPercentage then
                    isNewResultBetterThanCurrentBest = true

                logBestResults(sortedResults)

                if isNewResultBetterThanCurrentBest then
                  val saveResultFuture: Future[Message] = backtestersSpawner ? (myRef => SaveParametersMessage(sortedResults.head.parameters, myRef))
                  saveResultFuture.onComplete {
                    case Success(_) =>
                      config.bestResult = sortedResults.head
                      mainActorRef ! sortedResults.head

                    case Failure(e) =>
                      logger.error("Exception received trying to save the best parameters:" + e)
                      mainActorRef ! sortedResults.head
                  }
                else
                  logger.info("No better result detected")
                  logger.info("")

                  mainActorRef ! sortedResults.head

            case Failure(e) =>
              if !e.getMessage.eq("last of empty stream") then
                logger.error("Exception received in optimizeParameters:" + e)
                mainActorRef ! BacktestingResultMessage(0.0, 0, 0.0, 0.0, 0.0, List.empty)
              else
                logger.info("No positive result received during the last optimization continuing with the next parameters")
                mainActorRef ! BacktestingResultMessage(0.0, 0, 0.0, 0.0, 0.0, List.empty)
          }
        this

      case CloseBacktesterMessage() =>
        backtestersSpawner ! CloseBacktesterMessage()
        Behaviors.stopped


  private def logBestResults(sortedResults: List[BacktestingResultMessage]): Unit = {
    logger.info("")
    logger.info(s"Best ${Math.min(sortedResults.size, 50)} on ${sortedResults.size} results sorted by % of profitable trades:")

    for result <- sortedResults.take(50) do
      logger.info(s"Profit factor:${result.profitFactor} - net profit:${result.netProfitsPercentage}% - closed trades:${result.closedTradesNumber} - profitability:${result.profitabilityPercentage} - max drawdown:${result.maxDrawdownPercentage} - parameters:${result.parameters.map(_.value)}")
    logger.info("")
  }

  private def logResults(result: BacktestingResultMessage) = {
    if result.closedTradesNumber == 0 then
      logger.info("No positive result received for parameters: " + result.parameters.map(_.value))
    else
      logger.info("Result received for parameters: " + result.parameters.map(_.value) + s" with details: Profit factor:${result.profitFactor} - net profit:${result.netProfitsPercentage}% - closed trades:${result.closedTradesNumber} - profitability:${result.profitabilityPercentage} - max drawdown:${result.maxDrawdownPercentage}")

    result
  }
}
