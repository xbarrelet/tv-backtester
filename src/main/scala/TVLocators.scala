package ch.xavier

object TVLocators {
  // MAIN PAGE
  val chartDeepBacktestingScalerXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[1]/div[1]/div[2]/div/span"

  // PARAMETERS MODAL
  val welcomeLabelParametersModalXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[1]/div"


  // TP/SL
  val takeProfitTypeSelectorXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[28]/div/span"
  val stopLossTypeSelectorXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[30]/div/span"

  // RR
  val rrProfitFactorLongXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[69]/div/span/span[1]/input"
  val rrProfitFactorShortXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[70]/div/span/span[1]/input"

  // Fixed Percent
  val fixedPercentTPLongXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[39]/div/span/span[1]/input"
  val fixedPercentTPShortXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[43]/div/span/span[1]/input"
  val fixedPercentSLLongXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[37]/div/span/span[1]/input"
  val fixedPercentSLShortXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[41]/div/span/span[1]/input"

  // PIPS
  val pipsTPLongXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[49]/div/span/span[1]/input"
  val pipsTPShortXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[53]/div/span/span[1]/input"
  val pipsSLLongXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[47]/div/span/span[1]/input"
  val pipsSLShortXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[51]/div/span/span[1]/input"

  // HH - LL
  val highestHighLookbackXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[63]/div/span/span[1]/input"
  val LowestLowLookbackXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[65]/div/span/span[1]/input"

  // ATR
  val atrMultiplierXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[81]/div/span/span[1]/input"


  // BACKTESTING RESULTS
  //  val resultsPanelXPath =           "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]"
  val netProfitsPercentageValueXPath =    "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[1]/div[2]/div[2]"
  val closedTradesNumberXPath =           "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[2]/div[2]/div[1]"
  val profitabilityPercentageValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[3]/div[2]/div[1]"
  val profitFactorValueXPath =            "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[4]/div[2]/div[1]"
  val maxDrawdownPercentValueXPath =      "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[5]/div[2]/div[2]"
}
