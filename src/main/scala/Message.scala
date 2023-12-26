package ch.xavier

import backtesting.ParametersToTest

import akka.actor.typed.ActorRef


sealed trait Message

// QUERIES
final case class BacktestChartMessage() extends Message
final case class BacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message]) extends Message
final case class EnrichedBacktestMessage(parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message], playwrightService: PlaywrightService ) extends Message
final case class SaveParametersMessage(parametersToSave: List[ParametersToTest]) extends Message


// RESPONSES
final case class BacktestingResultMessage(netProfitsPercentage: Double, closedTradesNumber: Int, profitabilityPercentage: Double,
                                          profitFactor: Double, maxDrawdownPercentage: Double, parameters: List[ParametersToTest]) extends Message