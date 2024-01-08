package ch.xavier
package backtesting.parameters

import backtesting.TVLocators

final case class StrategyParameter(tvLocator: TVLocators, value: String)

//TODO: I'll need a factory later :)