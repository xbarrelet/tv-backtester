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
import scala.util.{Failure, Success}

object MainBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new MainBacktesterActor(context))
}

class MainBacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 300.seconds
  val logger: Logger = LoggerFactory.getLogger("StrategiesMainActor")

  private val backtestersSpawner: ActorRef[Message] = context.spawn(BacktestersSpawnerActor(), s"backtesters-spawner-actor")
  private var results: ListBuffer[BacktestingResultMessage] = ListBuffer()

  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestChartMessage() =>
        context.log.info(s"Starting backtesting")

        optimizeTPRR()

      case _ =>
        context.log.warn("Received unknown message in MainBacktesterActor of type: " + message.getClass)

      this


  private def optimizeTPRR(): Unit =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()
    val resultsList: ListBuffer[BacktestingResultMessage] = ListBuffer()

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


    Source(parametersList.result())
      .throttle(1, 1.second)
      .mapAsync(16)(parametersToTest => {
        backtestersSpawner ? (myRef => BacktestMessage(parametersToTest, myRef))
        })
      .map(_.asInstanceOf[BacktestingResultMessage])
      .filter(_.closedTradesNumber > 30)
      .map(results.append(_))
      .runWith(Sink.last)
      .onComplete {
        case Success(result) =>
          logger.info("")
          logger.info("Results sorted by profit factor:")

          for result <- results.sortWith(_.profitFactor > _.profitFactor) do
            logger.info("Profit factor: " + result.profitFactor + " - " + result)

        case Failure(e) =>
          logger.error("Exception received in StrategiesMainActor:" + e)
      }
}
