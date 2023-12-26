package ch.xavier

import backtesting.ParametersToTest

import akka.actor.typed.ActorRef
import com.microsoft.playwright.{BrowserContext, Page}


sealed trait Message

// QUERIES
final case class BacktestChartMessage() extends Message
final case class BacktestMessage(parametersToTest: List[ParametersToTest], browserContext: BrowserContext, actorRef: ActorRef[Message]) extends Message


// RESPONSES
final case class BacktestingResultMessage(netProfitsPercentage: Double, 
                                          closedTradesNumber: Int, 
                                          profitabilityPercentage: Double,
                                          profitFactor: Double,
                                          maxDrawdownPercentage: Double
                                         ) extends Message