package ch.xavier

import akka.actor.typed.ActorRef
import ch.xavier.backtesting.ParametersToTest
import com.microsoft.playwright.{BrowserContext, Page}


sealed trait Message

final case class BacktestChartMessage(chartId: String, browserContext: BrowserContext) extends Message

final case class BacktestMessage(page: Page, parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message]) extends Message




