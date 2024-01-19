//package ch.xavier
//package backtesting.actors.strats.squeeze
//
//import backtesting.actors.AbstractBacktesterBehavior
//import backtesting.parameters.StrategyParameter
//import backtesting.parameters.TVLocator.SQUEEZE_KC_LENGTH
//import backtesting.{OptimizeParametersListsMessage, Message}
//
//import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
//import akka.actor.typed.{ActorRef, Behavior}
//import org.slf4j.{Logger, LoggerFactory}
//
//import scala.collection.mutable.ListBuffer
//
//object SqueezeKCLengthActor {
//  def apply(): Behavior[Message] =
//    Behaviors.setup(context => new SqueezeKCLengthActor(context))
//}
//
//private class SqueezeKCLengthActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
//  val logger: Logger = LoggerFactory.getLogger("SqueezeKCLengthActor")
//
//
//  override def onMessage(message: Message): Behavior[Message] =
//    message match
//      case OptimizeParametersListsMessage(mainActorRef: ActorRef[Message], chartId: String) =>
//        val parametersTuplesToTest: List[List[StrategyParameter]] =
//          addParametersForBBLength()
//
//        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for Squeeze IT KC Length")
//
//        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
//      case _ =>
//        context.log.warn("Received unknown message in SqueezeKCLengthActor of type: " + message.getClass)
//
//    this
//
//
//  private def addParametersForBBLength(): List[List[StrategyParameter]] =
//    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()
//
//    (1 to 50).map(i => {
//      parametersList.addOne(List(
//        StrategyParameter(SQUEEZE_KC_LENGTH, i.toString)
//      ))
//    })
//
//    parametersList.toList
//}
//  