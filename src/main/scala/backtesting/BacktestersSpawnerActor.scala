package ch.xavier
package backtesting

import Application.{executionContext, system}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.Queue
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}



object BacktestersSpawnerActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new BacktestersSpawnerActor(context))
}

class BacktestersSpawnerActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 300.seconds
  private val logger: Logger = LoggerFactory.getLogger("BacktestersSpawnerActor")

  private var backtesterActorsQueue: mutable.Queue[Int] = mutable.Queue()
  private val backtesterActorsArray: Vector[ActorRef[Message]] = spawnBacktesters


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message]) =>
        val backtesterActorIndex: Int = backtesterActorsQueue.dequeue()
        val response: Future[Message] =  backtesterActorsArray(backtesterActorIndex) ? (myRef => EnrichedBacktestMessage(parametersToTest, myRef, backtesterActorIndex))

        response.onComplete {
          case Success(result: EnrichedBacktestingResultMessage) =>
            backtesterActorsQueue.enqueue(result.backtesterIndex)
            actorRef ! BacktestingResultMessage(result.netProfitsPercentage, result.closedTradesNumber, result.profitabilityPercentage,
              result.profitFactor, result.maxDrawdownPercentage, result.parameters)

          case Failure(ex) =>
            logger.error(s"Problem encountered in SpawnerActor when backtesting:${ex.getMessage}")
            actorRef ! BacktestingResultMessage(0, 0, 0, 0, 0, parametersToTest)
        }

      case SaveParametersMessage(parametersToSave: List[ParametersToTest]) =>
        val backtesterActorIndex: Int = backtesterActorsQueue.head
        backtesterActorsArray(backtesterActorIndex) ! message

      case _ =>
        context.log.warn("Received unknown message in BacktestersSpawnerActor of type: " + message.getClass)

      this

  private def spawnBacktesters = {
    val vector = Vector.fill(sys.env("CRAWLERS_NUMBER").toInt)(context.spawn(BacktesterActor(), "BacktesterActor_for_" + Random.nextInt()))
    (0 until sys.env("CRAWLERS_NUMBER").toInt).foreach(backtesterActorsQueue.enqueue(_))

    vector
  }
}
