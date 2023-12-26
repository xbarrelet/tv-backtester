package ch.xavier
package backtesting

import Application.{executionContext, system}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}



object BacktestersSpawnerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new BacktestersSpawnerActor(context))
}

class BacktestersSpawnerActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 300.seconds
  private val logger: Logger = LoggerFactory.getLogger("MainBacktesterActor")
  private var actorCounter: Int = 0

  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message]) =>
        val ref: ActorRef[Message] = context.spawn(BacktesterActor(), "BacktesterActor_for_" + actorCounter)
        actorCounter += 1

        val response: Future[Message] =  ref ? (myRef => message)

        response.onComplete {
          case Success(result: Message) => actorRef ! result
          case Failure(ex) => logger.error(s"Problem encountered when backtesting strategy:${ex.getMessage}")
        }

      case SaveParametersMessage(parametersToSave: List[ParametersToTest]) =>
        val ref: ActorRef[Message] = context.spawn(BacktesterActor(), "BacktesterActor_for_" + actorCounter)
        actorCounter += 1

        ref ! message

      case _ =>
        context.log.warn("Received unknown message in BacktestersSpawnerActor of type: " + message.getClass)

      this
}
