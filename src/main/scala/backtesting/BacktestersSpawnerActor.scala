package ch.xavier
package backtesting

import Application.{executionContext, system}
import backtesting.parameters.ParametersToTest

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
  implicit val timeout: Timeout = 300.seconds
  private val logger: Logger = LoggerFactory.getLogger("BacktestersSpawnerActor")
  private val backtestersPool: mutable.Queue[ActorRef[Message]] = instantiateBacktestersPool()
  private var actorsCounter = 0

  
  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message], chartId: String) =>
        val ref: ActorRef[Message] = backtestersPool.dequeue()

        val response: Future[Message] =  ref ? (myRef => BacktestMessage(parametersToTest, myRef, chartId))

        response.onComplete {
          case Success(result: Message) =>
            actorRef ! result
            backtestersPool.enqueue(ref)
          case Failure(ex) =>
            logger.error(s"Problem encountered in SpawnerActor when backtesting:${ex.getMessage}")
            actorRef ! BacktestingResultMessage(0, 0, 0, 0, 0, parametersToTest)
            backtestersPool.enqueue(ref)
        }

      case SaveParametersMessage(parametersToSave: List[ParametersToTest], actorRef: ActorRef[Message]) =>
        val ref: ActorRef[Message] = backtestersPool.dequeue()

        val response: Future[Message] =  ref ? (myRef => message)

        response.onComplete {
          case Success(result: Message) =>
            actorRef ! ParametersSavedMessage()
            backtestersPool.enqueue(ref)
          case Failure(ex) =>
            logger.error(s"Problem encountered in SpawnerActor when saving the parameter:${ex.getMessage}")
            actorRef ! ParametersSavedMessage()
            backtestersPool.enqueue(ref)
        }

      case _ =>
        context.log.warn("Received unknown message in BacktestersSpawnerActor of type: " + message.getClass)

      this

  private def instantiateBacktestersPool(): mutable.Queue[ActorRef[Message]] =
    val backtestersPool: mutable.Queue[ActorRef[Message]] = mutable.Queue.empty

    for _ <- 1 to sys.env("CRAWLERS_NUMBER").toInt do
      backtestersPool.enqueue(context.spawn(BacktesterActor(), s"BacktesterActor-$actorsCounter"))
      actorsCounter += 1

    logger.info(s"Backtesters pool instantiated with ${backtestersPool.size} actors")
    backtestersPool
}
