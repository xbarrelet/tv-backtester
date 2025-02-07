package ch.xavier
package backtesting.actors

import Application.{executionContext, system}
import backtesting.*
import backtesting.parameters.StrategyParameter

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}


object BacktestersSpawnerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new BacktestersSpawnerActor(context))
}

class BacktestersSpawnerActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 1800.seconds
  private val logger: Logger = LoggerFactory.getLogger("BacktestersSpawnerActor")
  private val backtestersPool: mutable.Queue[ActorRef[Message]] = instantiateBacktestersPool()
  private var actorsCounter = 0


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case OptimizeParametersMessage(parametersToTest: List[StrategyParameter], actorRef: ActorRef[Message], chartId: String) =>
        while backtestersPool.isEmpty do
          context.log.info("Backtesters not ready yet, waiting a little")
          Thread.sleep(5000)
          
        val ref: ActorRef[Message] = backtestersPool.dequeue()

        val response: Future[Message] = ref ? (myRef => OptimizeParametersMessage(parametersToTest, myRef, chartId))

        response.onComplete {
          case Success(result: Message) =>
            actorRef ! result
            backtestersPool.enqueue(ref)
          case Failure(ex) =>
            logger.error(s"Problem encountered in SpawnerActor when backtesting:${ex.getMessage}")
            actorRef ! BacktestingResultMessage(0, 0, 0, 0, 0, parametersToTest)
            backtestersPool.enqueue(ref)
        }

      case SaveParametersMessage(parametersToSave: List[StrategyParameter], actorRef: ActorRef[Message]) =>
        val ref: ActorRef[Message] = backtestersPool.dequeue()
        val response: Future[Message] = ref ? (myRef => SaveParametersMessage(parametersToSave, myRef))

        response.onComplete {
          case Success(result: Message) =>
            backtestersPool.enqueue(ref)
            actorRef ! ParametersSavedMessage()
          case Failure(ex) =>
            logger.error(s"Problem encountered in SpawnerActor when saving the parameter, sending new message:${ex.getMessage}")
            backtestersPool.enqueue(ref)
            actorRef ! ParametersSavedMessage()
        }

      case CloseBacktesterMessage() =>
        backtestersPool.foreach(ref => ref ! CloseBacktesterMessage())
        Behaviors.stopped

      case _ =>
        context.log.warn("Received unknown message in BacktestersSpawnerActor of type: " + message.getClass)

    this

  private def instantiateBacktestersPool(): mutable.Queue[ActorRef[Message]] =
    val backtestersPool: mutable.Queue[ActorRef[Message]] = mutable.Queue.empty

    for _ <- 1 to sys.env("CRAWLERS_NUMBER").toInt do
      backtestersPool.enqueue(context.spawn(BacktesterActor(), s"BacktesterActor-$actorsCounter"))
      actorsCounter += 1

    backtestersPool
}
