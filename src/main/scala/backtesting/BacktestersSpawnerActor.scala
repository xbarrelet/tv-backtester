package ch.xavier
package backtesting

import Application.{executionContext, system}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}



object BacktestersSpawnerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new BacktestersSpawnerActor(context))
}

class BacktestersSpawnerActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 300.seconds
  private var actorCounter: Int = 0

  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message]) =>
        val ref: ActorRef[Message] = context.spawn(BacktesterActor(), "BacktesterActor_for_" + actorCounter)
        actorCounter += 1

        val response: Future[Message] =  ref ? (myRef => message)

        response.onComplete {
          case Success(result: Message) => actorRef ! result
          case Failure(ex) => context.log.error(s"Problem encountered when backtesting strategy")
        }
        
        this
}
