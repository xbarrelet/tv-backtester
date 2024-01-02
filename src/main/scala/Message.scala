package ch.xavier

import backtesting.parameters.ParametersToTest

import akka.actor.typed.ActorRef


sealed trait Message

// QUERIES
final case class StartBacktesting() extends Message
final case class BacktestSpecificPartMessage(ref: ActorRef[Message], chartId: String) extends Message
final case class BacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message], chartId: String) extends Message
final case class SaveParametersMessage(parametersToSave: List[ParametersToTest], ref: ActorRef[Message]) extends Message
final case class CloseBacktesterMessage() extends Message


// RESPONSES
final case class BacktestingResultMessage(netProfitsPercentage: Double, closedTradesNumber: Int, profitabilityPercentage: Double,
                                          profitFactor: Double, maxDrawdownPercentage: Double, parameters: List[ParametersToTest]) extends Message
final case class ParametersSavedMessage() extends Message
final case class BacktestChartResponseMessage() extends Message