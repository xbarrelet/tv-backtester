package ch.xavier

import backtesting.actors.ChartBacktesterActor
import backtesting.{BacktestChartMessage, ChartBacktestedMessage, ChartToProcess, Message}
import Application.{executionContext, system}
import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor.Aux
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
  context.log.info("The backtester is starting")
  context.log.info("")

  implicit val timeout: Timeout = 24.hours
  private val logger: Logger = LoggerFactory.getLogger("Application")
  private val mainBacktesterRef: ActorRef[Message] = context.spawn(ChartBacktesterActor(), "main-backtester-actor")

  
  private val chartsToProcess: Seq[ChartToProcess] = sql"select chart_id, processing_type from charts_to_process"
    .query[ChartToProcess]
    .to[List]
    .transact(BacktesterConfig.transactor)
    .unsafeRunSync()

  for chart <- chartsToProcess do {
    context.log.info(s"Processing chart ${chart.chart_id} with processing type ${chart.processing_type}")
  }
  context.log.info("")
  

  Source(chartsToProcess)
    .mapAsync(1)(chartToProcess => {
      mainBacktesterRef ? (myRef => BacktestChartMessage(chartToProcess, myRef))
    })
    .runWith(Sink.last)
    .onComplete {
      case Success(result) =>
        logger.info(s"All charts have been backtested, have a nice day :)")
        system.terminate()
        System.exit(0)

      case Failure(e) =>
        logger.error(s"Exception received during global optimization:" + e)
        system.terminate()
        System.exit(0)
    }

  override def onMessage(message: Message): Behavior[Message] =
    this
}


