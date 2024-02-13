package ch.xavier

import backtesting.BacktestingResultMessage

import cats.effect.IO
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

object BacktesterConfig {
    var strategyName: String = ""
    var botifyVersion: String = ""
    var bestResult: BacktestingResultMessage = BacktestingResultMessage(0, 0, 0, 0, 0, List.empty)
    var distanceBetweenLabelAndField = 0

    val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
        driver = "org.postgresql.Driver",
        url = "jdbc:postgresql:backtester",
        user = "postgres",
        password = "postgres123",
        logHandler = None
    )
}
