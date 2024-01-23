//package ch.xavier
//package backtesting.actors.strats.deadzonev5
//
//import ch.xavier.backtesting.parameters.TVLocator.DEADZONE_FAST_EMA
//import backtesting.actors.AbstractBacktesterBehavior
//import backtesting.parameters.StrategyParameter
//import backtesting.{OptimizeParametersListsMessage, Message}
//
//import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
//import akka.actor.typed.{ActorRef, Behavior}
//import org.slf4j.{Logger, LoggerFactory}
//
//import scala.collection.mutable.ListBuffer
//
//object DeadZoneV5FastEMAActor {
//  def apply(): Behavior[Message] =
//    Behaviors.setup(context => new DeadZoneV5FastEMAActor(context))
//}
//
//private class DeadZoneV5FastEMAActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
//  val logger: Logger = LoggerFactory.getLogger("DeadZoneV5FastEMAActor")
//
//
//  override def onMessage(message: Message): Behavior[Message] =
//    message match
//      case OptimizeParametersListsMessage(mainActorRef: ActorRef[Message], chartId: String) =>
//        val parametersTuplesToTest: List[List[StrategyParameter]] =
//          addParametersForFastEMA()
//
//        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for DeadzoneV5 fast EMA")
//
//        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
//      case _ =>
//        context.log.warn("Received unknown message in DeadZoneV5SensitivityActor of type: " + message.getClass)
//
//    this
//
//
//  private def addParametersForFastEMA(): List[List[StrategyParameter]] =
//    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()
//
//    (10 to 80 by 2).map(i => {
//      parametersList.addOne(List(
//        StrategyParameter(DEADZONE_FAST_EMA, i.toString)
//      ))
//    })
//
//    parametersList.toList
//}
//  