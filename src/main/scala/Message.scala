package ch.xavier

import com.microsoft.playwright.BrowserContext


sealed trait Message

final case class BacktestChartMessage(chartId: String, browserContext: BrowserContext) extends Message




