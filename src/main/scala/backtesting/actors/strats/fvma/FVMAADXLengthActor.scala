//package ch.xavier
//package backtesting.actors.strats.fvma
//
//import backtesting.actors.AbstractBacktesterBehavior
//import backtesting.parameters.StrategyParameter
//import backtesting.parameters.TVLocator.FVMA_ADX_LENGTH
//import backtesting.{OptimizeParametersListsMessage, Message}
//
//import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
//import akka.actor.typed.{ActorRef, Behavior}
//import org.slf4j.{Logger, LoggerFactory}
//
//import scala.collection.mutable.ListBuffer
//
//object FVMAADXLengthActor {
//  def apply(): Behavior[Message] =
//    Behaviors.setup(context => new FVMAADXLengthActor(context))
//}
//
//private class FVMAADXLengthActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
//  val logger: Logger = LoggerFactory.getLogger("FVMAADXLengthActor")
//
//
//  override def onMessage(message: Message): Behavior[Message] =
//    message match
//      case OptimizeParametersListsMessage(mainActorRef: ActorRef[Message], chartId: String) =>
//        val parametersTuplesToTest: List[List[StrategyParameter]] =
//          addParameters()
//
//        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for FVMA ADX length")
//
//        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
//      case _ =>
//        context.log.warn("Received unknown message in FVMAADXLengthActor of type: " + message.getClass)
//
//    this
//
//
//  private def addParameters(): List[List[StrategyParameter]] =
//    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()
//
//    (1 to 75).map(i => {
//      parametersList.addOne(List(
//        StrategyParameter(FVMA_ADX_LENGTH, i.toString)
//      ))
//    })
//
//    parametersList.toList
//}
//  