package ch.xavier
package backtesting

import backtesting.parameters.StrategyParameter

import akka.actor.typed.ActorRef


sealed trait Message

// QUERIES
final case class StartBacktesting() extends Message

final case class BacktestSpecificPartMessage(ref: ActorRef[Message], chartId: String) extends Message

final case class BacktestMessage(parametersToTest: List[StrategyParameter], actorRef: ActorRef[Message], chartId: String) extends Message

final case class SaveParametersMessage(parametersToSave: List[StrategyParameter], ref: ActorRef[Message]) extends Message

final case class CloseBacktesterMessage() extends Message


// RESPONSES
final case class BacktestingResultMessage(netProfitsPercentage: Double, closedTradesNumber: Int, profitabilityPercentage: Double,
                                          profitFactor: Double, maxDrawdownPercentage: Double, parameters: List[StrategyParameter]) extends Message

final case class ParametersSavedMessage() extends Message

final case class BacktestChartResponseMessage() extends Message
