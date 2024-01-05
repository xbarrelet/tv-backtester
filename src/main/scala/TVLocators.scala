package ch.xavier

object TVLocators {
  // MAIN PAGE
  val chartDeepBacktestingScalerXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[1]/div[1]/div[2]/div/span"

  // PARAMETERS MODAL
  val welcomeLabelParametersModalXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[1]/div"


  // TP/SL
  val takeProfitTypeSelectorXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[28]/div/span"
  val stopLossTypeSelectorXPath =   "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[30]/div/span/span[1]"

  // Trailing parameters
//  val trailingLossCheckboxXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[92]/div/label/span[1]/input"
  val trailingLossCheckboxXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[92]/div/label/span[1]/span"
//  val trailingTPCheckboxXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[99]/div/label/span[1]/input"
  val trailingTPCheckboxXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[99]/div/label/span[1]/span"
  val atrTLMultiplierXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[101]/div/span/span[1]/input"
  val whenToActivateTrailingXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[96]/div/span"

  // RR
  val rrProfitFactorLongXPath: String =  "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[69]/div/span/span[1]/input"
  val rrProfitFactorShortXPath: String = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[71]/div/span/span[1]/input"

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

  // Leverage
  val leverageAmountXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[128]/div/span/span[1]/input"
  val dynamicLeverageCheckboxXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[131]/div/label"


  //// STRATS
  //DEAD ZONE V5
  val sensitivityXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[226]/div/span/span[1]/input"
  val fastEMALengthXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[228]/div/span/span[1]/input"
  val slowEMALengthXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[230]/div/span/span[1]/input"
  val bbChannelLengthXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[230]/div/span/span[1]/input"
  val bbStdDevXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[234]/div/span/span[1]/input"
  val deadMultiplerXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[236]/div/span/span[1]/input"


  // BACKTESTING RESULTS
  //  val resultsPanelXPath =           "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]"
  val netProfitsPercentageValueXPath =    "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[1]/div[2]/div[2]"
//  val netProfitsPercentageValueXPath =    "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[2]/div/div[1]/div[1]/div[2]/div[2]"
  val closedTradesNumberXPath =           "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[2]/div[2]/div[1]"
  val profitabilityPercentageValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[3]/div[2]/div[1]"
  val profitFactorValueXPath =            "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[4]/div[2]/div[1]"
  val maxDrawdownPercentValueXPath =      "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[5]/div[2]/div[2]"
}
