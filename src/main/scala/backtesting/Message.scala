package ch.xavier
package backtesting

import backtesting.parameters.StrategyParameter

import akka.actor.typed.ActorRef


sealed trait Message

// QUERIES/COMMANDS
final case class BacktestChartMessage(chartToProcess: ChartToProcess, replyTo: ActorRef[Message]) extends Message
final case class ChartBacktestedMessage(chartId: String) extends Message
final case class OptimizePartMessage(ref: ActorRef[Message], chartId: String) extends Message
final case class OptimizeParametersListsMessage(parameters: List[List[StrategyParameter]], ref: ActorRef[Message], chartId: String, evaluationParameter: String) extends Message
final case class OptimizeParametersMessage(parametersToTest: List[StrategyParameter], actorRef: ActorRef[Message], chartId: String) extends Message
final case class SaveParametersMessage(parametersToSave: List[StrategyParameter], ref: ActorRef[Message]) extends Message
final case class CloseBacktesterMessage() extends Message
final case class ShutDownMessage() extends Message


// RESPONSES
final case class BacktestingResultMessage(netProfitsPercentage: Double, closedTradesNumber: Int, profitabilityPercentage: Double,
                                          profitFactor: Double, maxDrawdownPercentage: Double, parameters: List[StrategyParameter]) extends Message
                                          
final case class ParametersSavedMessage() extends Message
final case class BacktestChartResponseMessage() extends Message
