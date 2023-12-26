package ch.xavier
package backtesting

import Application.{executionContext, system}
import TVLocators.*

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

        var TPParametersTuplesToTest: List[List[ParametersToTest]] = List()

        //TODO: Faur les processer une apres lautre. Comment faire ca elegamment? Enrober tes source flows?
//        TPParametersTuplesToTest = addParametersForTPRRShort()
//        context.log.info(s"Testing ${TPParametersTuplesToTest.size} different parameters combinations for TP optimisation in TPRRShort")
//        optimizeParameters(TPParametersTuplesToTest)

        TPParametersTuplesToTest = addParametersForTPRRLong()
        context.log.info(s"Testing ${TPParametersTuplesToTest.size} different parameters combinations for TP optimisation in TPRRLong")
        optimizeParameters(TPParametersTuplesToTest)
//        TPParametersTuplesToTest ++= addParametersForTPFixedPercent()
//        TPParametersTuplesToTest ++= addParametersForTPPips()

        //TODO: Begin with a simple final step Trailing loss + TP?
//        context.log.info(s"Testing ${TPParametersTuplesToTest.size} different parameters combinations for TP optimisation")
//        optimizeParameters(TPParametersTuplesToTest)

//        var SLParametersTuplesToTest: List[List[ParametersToTest]] = List()
//
//        SLParametersTuplesToTest ++= addParametersForSLATR()
//        SLParametersTuplesToTest ++= addParametersForSLFixedPercent()
//        SLParametersTuplesToTest ++= addParametersForSLPips()
//
//        context.log.info(s"Testing ${SLParametersTuplesToTest.size} different parameters combinations for SL optimisation")
//        optimizeParameters(SLParametersTuplesToTest)

      case _ =>
        context.log.warn("Received unknown message in MainBacktesterActor of type: " + message.getClass)

      this


  private def optimizeParameters(parametersCombinationToTest: List[List[ParametersToTest]]): Unit = {
    Source(parametersCombinationToTest)
      .throttle(1, Random.between(2500, 5000).millis)
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
          logger.info(s"Best 50 on ${sortedResults.size} results sorted by profit factor:")

          for result <- sortedResults.take(50) do
            logger.info("Profit factor: " + result.profitFactor + " - " + result.parameters)

          logger.info("")
          backtestersSpawner ! SaveParametersMessage(sortedResults.head.parameters)
          logger.info("")

        case Failure(e) =>
          logger.error("Exception received in RunBacktesting:" + e)
      }
  }

  private def addParametersForTPRRShort(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 50).map(i => {
      parametersList.addOne(List(
          ParametersToTest(takeProfitTypeSelectorXPath, "R:R", "selectTakeProfit"),
          ParametersToTest(rrProfitFactorShortXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList

  private def addParametersForTPRRLong(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 50).map(i => {
      parametersList.addOne(List(
        ParametersToTest(takeProfitTypeSelectorXPath, "R:R", "selectTakeProfit"),
        ParametersToTest(rrProfitFactorLongXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList

  private def addParametersForTPFixedPercent(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 150).map(i => {
      parametersList.addOne(List(
          ParametersToTest(takeProfitTypeSelectorXPath, "Fixed Percent", "selectTakeProfit"),
          ParametersToTest(fixedPercentTPLongXPath, (i / 10.0).toString, "fill")))
    })

    (5 to 150).map(i => {
      parametersList.addOne(List(
          ParametersToTest(takeProfitTypeSelectorXPath, "Fixed Percent", "selectTakeProfit"),
          ParametersToTest(fixedPercentTPShortXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList

  private def addParametersForSLFixedPercent(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 150).map(i => {
      parametersList.addOne(List(
        ParametersToTest(stopLossTypeSelectorXPath, "Fixed Percent", "selectStopLoss"),
        ParametersToTest(fixedPercentSLLongXPath, (i / 10.0).toString, "fill")))
    })

    (5 to 150).map(i => {
      parametersList.addOne(List(
        ParametersToTest(stopLossTypeSelectorXPath, "Fixed Percent", "selectStopLoss"),
        ParametersToTest(fixedPercentSLShortXPath, (i / 10.0).toString, "fill")))
    })

    parametersList.toList

  private def addParametersForTPPips(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (50 to 300).map(i => {
      if i % 5 == 0 then
        parametersList.addOne(List(
          ParametersToTest(takeProfitTypeSelectorXPath, "PIPS", "selectTakeProfit"),
          ParametersToTest(fixedPercentTPLongXPath, i.toString, "fill")))
    })

    (50 to 300).map(i => {
      if i % 5 == 0 then
        parametersList.addOne(List(
          ParametersToTest(takeProfitTypeSelectorXPath, "PIPS", "selectTakeProfit"),
          ParametersToTest(fixedPercentTPShortXPath, i.toString, "fill")))
    })

    parametersList.toList

  private def addParametersForSLPips(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (50 to 300).map(i => {
      if i % 5 == 0 then
        parametersList.addOne(List(
          ParametersToTest(stopLossTypeSelectorXPath, "PIPS", "selectStopLoss"),
          ParametersToTest(fixedPercentSLLongXPath, i.toString, "fill")))
    })

    (50 to 300).map(i => {
      if i % 5 == 0 then
        parametersList.addOne(List(
          ParametersToTest(stopLossTypeSelectorXPath, "PIPS", "selectStopLoss"),
          ParametersToTest(fixedPercentSLShortXPath, i.toString, "fill")))
    })

    parametersList.toList

  private def addParametersForSLATR(): List[List[ParametersToTest]] =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    (5 to 100).map(i => {
        parametersList.addOne(List(
          ParametersToTest(stopLossTypeSelectorXPath, "ATR", "selectStopLoss"),
          ParametersToTest(fixedPercentSLLongXPath, (i / 10.0).toString, "fill")))
    })


    parametersList.toList
}
