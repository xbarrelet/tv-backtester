package ch.xavier

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



object PlaywrightActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new PlaywrightActor(context))
}

class PlaywrightActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 300.seconds
  private val logger: Logger = LoggerFactory.getLogger("PlaywrightActor")
  private var actorCounter: Int = 0

  override def onMessage(message: Message): Behavior[Message] =
    message match
      case _ =>
        context.log.warn("Received unknown message in BacktestersSpawnerActor of type: " + message.getClass)

      this
}
