package ch.xavier
package backtesting.parameters

final case class StrategyParameter(tvLocator: TVLocator, value: String)

object StrategyParameter:
  def empty: StrategyParameter = StrategyParameter(TVLocator.EMPTY, "")