package ch.xavier

import backtesting.actors.ChartBacktesterActor
import backtesting.{Message, StartBacktesting}

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.concurrent.ExecutionContextExecutor


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

  private val mainBacktesterRef: ActorRef[Message] = context.spawn(ChartBacktesterActor(), "main-backtester-actor")

  mainBacktesterRef ! StartBacktesting()


  override def onMessage(message: Message): Behavior[Message] =
    this
}