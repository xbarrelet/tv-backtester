package ch.xavier

import backtesting.BacktestingResultMessage

object BacktesterConfig {
    var strategyName: String = ""
    var botifyVersion: String = ""
    var bestResult: BacktestingResultMessage = BacktestingResultMessage(0, 0, 0, 0, 0, List.empty)
}
