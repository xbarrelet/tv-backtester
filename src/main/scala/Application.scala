package ch.xavier

import Application.{executionContext, system}
import backtesting.actors.ChartBacktesterActor
import backtesting.{BacktestChartMessage, ChartBacktestedMessage, ChartToProcess, Message}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import doobie.*
import doobie.implicits.*
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}


object Application extends App {
  implicit val system: ActorSystem[Message] = ActorSystem(Main(), "System")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
}


object Main {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new Main(context))
}

private class Main(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  context.log.info("The backtester is starting.\n")
  
  private val logger: Logger = LoggerFactory.getLogger("Application")
  implicit val timeout: Timeout = 24.hours
  private val mainBacktesterRef: ActorRef[Message] = context.spawn(ChartBacktesterActor(), "main-backtester-actor")


  //  private val chartsToProcess: Seq[ChartToProcess] = sql"select chart_id, processing_type from charts_to_process"
  //    .query[ChartToProcess]
  //    .to[List]
  //    .transact(BacktesterConfig.transactor)
  //    .unsafeRunSync()

  //  if chartsToProcess.isEmpty then
  //    context.log.info("No chart to process, exiting")
  //    exit()
  //
  //  for chart <- chartsToProcess do {
  //    context.log.info(s"Processing chart ${chart.chart_id} with processing type ${chart.processing_type}")
  //  }
  //  context.log.info("")

  private val chartsToProcess = List(
    ChartToProcess("CkQUSasu", "full"), //3M
    ChartToProcess("QwzRIQ0k", "full"), //6M
    ChartToProcess("P4oOkco1", "full"), //24M
  )
  
  Source(chartsToProcess)
    .mapAsync(1)(chartToProcess => {
      mainBacktesterRef ? (myRef => BacktestChartMessage(chartToProcess, myRef))
    })
    .map(_.asInstanceOf[ChartBacktestedMessage])
    .map(_.chartId)
    .map(chartId => {
      context.log.info(s"Chart $chartId has been backtested")
      sql"delete from charts_to_process where chart_id = $chartId".update.run
    })
    .runWith(Sink.last)
    .onComplete {
      case Success(result) =>
        logger.info(s"All charts have been backtested, have a nice day :)")
        exit()

      case Failure(e) =>
        logger.error(s"Exception received during global optimization:" + e)
        exit()
    }

  private def exit(): Unit = {
    system.terminate()
    System.exit(0)
  }

  override def onMessage(message: Message): Behavior[Message] =
    this
}


