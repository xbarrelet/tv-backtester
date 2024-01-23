//package ch.xavier
//package backtesting.actors.strats.squeeze
//
//import backtesting.actors.AbstractBacktesterBehavior
//import backtesting.parameters.StrategyParameter
//import backtesting.parameters.TVLocator.{SQUEEZE_WT_FIRST_BEARISH_DIVERGENCE_MIN, SQUEEZE_WT_FIRST_BULLISH_DIVERGENCE_MIN}
//import backtesting.{OptimizeParametersListsMessage, Message}
//
//import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
//import akka.actor.typed.{ActorRef, Behavior}
//import org.slf4j.{Logger, LoggerFactory}
//
//import scala.collection.mutable.ListBuffer
//
//object SqueezeWTFirstDivergencesMinActor {
//  def apply(): Behavior[Message] =
//    Behaviors.setup(context => new SqueezeWTFirstDivergencesMinActor(context))
//}
//
//private class SqueezeWTFirstDivergencesMinActor(context: ActorContext[Message]) extends AbstractBacktesterBehavior(context) {
//  val logger: Logger = LoggerFactory.getLogger("SqueezeWTFirstDivergencesMinActor")
//
//
//  override def onMessage(message: Message): Behavior[Message] =
//    message match
//      case OptimizeParametersListsMessage(mainActorRef: ActorRef[Message], chartId: String) =>
//        val parametersTuplesToTest: List[List[StrategyParameter]] =
//          addParametersForBBLength()
//
//        context.log.info(s"Testing ${parametersTuplesToTest.size} different parameters combinations for Squeeze IT WT divergences min")
//
//        optimizeParameters(parametersTuplesToTest, mainActorRef, chartId)
//      case _ =>
//        context.log.warn("Received unknown message in SqueezeWTFirstDivergencesMinActor of type: " + message.getClass)
//
//    this
//
//
//  private def addParametersForBBLength(): List[List[StrategyParameter]] =
//    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()
//
//    (1 to 50).map(i => {
//      parametersList.addOne(List(
//        StrategyParameter(SQUEEZE_WT_FIRST_BEARISH_DIVERGENCE_MIN, i.toString),
//        StrategyParameter(SQUEEZE_WT_FIRST_BULLISH_DIVERGENCE_MIN, (-1 * i).toString)
//      ))
//    })
//
//    parametersList.toList
//}
//  