package ch.xavier
package backtesting.parameters

enum TYPE {
  case INPUT, CHECKBOX, OPTION
}

enum TVLocator(locatorType: TYPE, index: Int, label: String = "", isCloseInput: Boolean = false) {

  def getType: TYPE = locatorType
  def getIndex: Int = index
  def getLabel: String = label
  def getIsCloseInput: Boolean = isCloseInput

  case EMPTY extends TVLocator(null, 0, "")

  // TP
  case TP_TYPE extends TVLocator(TYPE.OPTION, 83, "Type of Take Profit")
  case TP_SHORT_FIXED_PERCENTS extends TVLocator(TYPE.INPUT, 9, "Take Profit Short %")
  case TP_SHORT_RR extends TVLocator(TYPE.INPUT, 17, "Profit factor Short (Risk to Reward)")
  case TP_LONG_FIXED_PERCENTS extends TVLocator(TYPE.INPUT, 7, "Take Profit Long %")
  case TP_LONG_RR extends TVLocator(TYPE.INPUT, 16, "Profit factor Long (Risk to Reward)")

  case USE_MULTI_PROFIT extends TVLocator(TYPE.CHECKBOX, 27)
  case USE_MULTI_PROFIT_TP1 extends TVLocator(TYPE.CHECKBOX, 28)
  case USE_MULTI_PROFIT_TP2 extends TVLocator(TYPE.CHECKBOX, 29)
  case TP1_PERCENTS extends TVLocator(TYPE.INPUT, 51)
  case TP1_LEVEL extends TVLocator(TYPE.INPUT, 52)
  case TP2_PERCENTS extends TVLocator(TYPE.INPUT, 53)
  case TP2_LEVEL extends TVLocator(TYPE.INPUT, 54)
  case TP3_PERCENTS extends TVLocator(TYPE.INPUT, 55)
  case TP3_LEVEL extends TVLocator(TYPE.INPUT, 56)

  // SL
  case SL_TYPE extends TVLocator(TYPE.OPTION, 84, "Type of Stoploss")
  case SL_SHORT_FIXED_PERCENTS extends TVLocator(TYPE.INPUT, 8, "Stop Loss Short %")
  case SL_LONG_FIXED_PERCENTS extends TVLocator(TYPE.INPUT, 6, "Stop Loss Long %")
  case SL_ATR_MULTIPLIER extends TVLocator(TYPE.INPUT, 20, "ATR Multiplier For Stoploss")
  case SL_ATR_SWING_LOOKBACK extends TVLocator(TYPE.INPUT, 21, "ATR Swing lookback")
  case ATR_LENGTH extends TVLocator(TYPE.INPUT, 1, "ATR length")

  // HH
  case HIGHEST_HIGH_LOOKBACK extends TVLocator(TYPE.INPUT, 14, "Highest High lookback")
  case LOWEST_LOW_LOOKBACK extends TVLocator(TYPE.INPUT, 15, "Lowest Low Lookback")

  // TRAILING
  case USE_TRAILING_LOSS extends TVLocator(TYPE.CHECKBOX, 10, "Use Trailing Stop")
  case USE_TRAILING_TP extends TVLocator(TYPE.CHECKBOX, 11, "Use Trailing Take Profit")
  case TRAILING_ACTIVATION extends TVLocator(TYPE.OPTION, 91, "When to Activate Trailing?")
  case TRAILING_LOSS_THRESHOLD extends TVLocator(TYPE.INPUT, 22, "Trigger Trailing Threshold % of Total TP")
  case TRAILING_LOSS_ATR_MULTIPLIER extends TVLocator(TYPE.INPUT, 23, "ATR Trailing Stop Multiplier")

  // LEVERAGE
  case PERCENT_OF_PORTFOLIO_PER_TRADE extends TVLocator(TYPE.INPUT, 30, "% of Portfolio")
  case LEVERAGE_PERCENT extends TVLocator(TYPE.INPUT, 31, "Leverage")
  case USE_DYNAMIC_LEVERAGE extends TVLocator(TYPE.CHECKBOX, 17, "Dynamic Leverage (READ TOOLTIP)")


  // STRATEGIES
  // DEADZONE V5
  case DEADZONE_SENSITIVITY extends TVLocator(TYPE.INPUT, 65)
  case DEADZONE_FAST_EMA extends TVLocator(TYPE.INPUT, 66)
  case DEADZONE_SLOW_EMA extends TVLocator(TYPE.INPUT, 67)
  case DEADZONE_BB_CHANNEL_LENGTH extends TVLocator(TYPE.INPUT, 68)
  case DEADZONE_BB_STDEV_MULTIPLIER extends TVLocator(TYPE.INPUT, 69)
  case DEADZONE_DEADZONE_PARAMETER extends TVLocator(TYPE.INPUT, 70)

  // SQUEEZE IT
  case SQUEEZE_BB_LENGTH extends TVLocator(TYPE.INPUT, 66)
  case SQUEEZE_BB_MULTI_FACTOR extends TVLocator(TYPE.INPUT, 67)
  case SQUEEZE_KC_LENGTH extends TVLocator(TYPE.INPUT, 68)
  case SQUEEZE_KC_MULTI_FACTOR extends TVLocator(TYPE.INPUT, 69)
  case SQUEEZE_WT_FIRST_BEARISH_DIVERGENCE_MIN extends TVLocator(TYPE.INPUT, 70)
  case SQUEEZE_WT_FIRST_BULLISH_DIVERGENCE_MIN extends TVLocator(TYPE.INPUT, 71)

  // FVMA
  case FVMA_FAST_ATR_LENGTH extends TVLocator(TYPE.INPUT, 57)
  case FVMA_FACTOR extends TVLocator(TYPE.INPUT, 58)
  case FVMA_ADX_LENGTH extends TVLocator(TYPE.INPUT, 59)
  case FVMA_WEIGHTING extends TVLocator(TYPE.INPUT, 60)
  case FVMA_MA_LENGTH extends TVLocator(TYPE.INPUT, 61)
  case FVMA_FAST_LENGTH extends TVLocator(TYPE.INPUT, 62)
  case FVMA_SLOW_LENGTH extends TVLocator(TYPE.INPUT, 63)
  case FVMA_SIGNAL_LENGTH extends TVLocator(TYPE.INPUT, 64)



  // AFFINEMENT
  // USE MA
  case MA_TYPE extends TVLocator(TYPE.OPTION, 101, "Use Moving Averages?")

  // VOLUME CONFIRMATION
  case USE_VOLUME_CONFIRMATION extends TVLocator(TYPE.CHECKBOX, 1, "Volume confirm?")
  case TDFI_LOOKBACK_LENGTH extends TVLocator(TYPE.INPUT, 1, "TDFI Lookback", true)
  case MMA_LENGTH extends TVLocator(TYPE.INPUT, 1, "MMA Length v2")
  case N_LENGTH extends TVLocator(TYPE.INPUT, 1, "N Length v2")

  // RANGE FILTER
  case USE_RANGE_FILTER extends TVLocator(TYPE.CHECKBOX, -52, "Use The Range Filter?")
  case RANGE_FILTER_PERIOD extends TVLocator(TYPE.INPUT, -49, "RF Period")
  case RANGE_FILTER_MULTIPLIER extends TVLocator(TYPE.INPUT, -48, "RF Multi")

  //HURST EXPONENT
  case USE_HURST_EXP extends TVLocator(TYPE.CHECKBOX, -49, "Enable  Hurst exponent ?")
  case USE_HURST_EXP_MTF extends TVLocator(TYPE.CHECKBOX, -48, "Enable Hurst MTF ? \uD83D\uDEA8")
  case HURST_EXP_LENGTH extends TVLocator(TYPE.INPUT, -43, "Hurst Exp Length")

  // FLAT MARKET
  case USE_FLAT_MARKET extends TVLocator(TYPE.CHECKBOX, -38, "Enable Flat Market ?")
  case FLAT_MARKET_MA_TYPE extends TVLocator(TYPE.OPTION, -1, "MA Type Flat Market")
  case FLAT_MARKET_MA_LENGTH extends TVLocator(TYPE.INPUT, -26, "MA Length Flat Market")
  case FLAT_MARKET_ABOVE_LINE extends TVLocator(TYPE.INPUT, -25, "Flat Market Above Line")

  // VWAP
  case USE_VWAP_CROSSOVER extends TVLocator(TYPE.CHECKBOX, -35, "Use vWap / ema")
  case VWAP_LENGTH extends TVLocator(TYPE.INPUT, -25, "Vwap Length")

  //MIN TP AND MAX SL
  case MIN_TP_PERCENT extends TVLocator(TYPE.INPUT, 1, "Min Threshold TP %")
  case MAX_SL_PERCENT extends TVLocator(TYPE.INPUT, 1, "Threshold Max % SL")
}

object TVLocator {
  // BACKTESTING RESULTS XPATHS
  val netProfitsPercentageValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[1]/div[2]/div[2]"
  val closedTradesNumberXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[2]/div[2]/div[1]"
  val profitabilityPercentageValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[3]/div[2]/div[1]"
  val profitFactorValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[4]/div[2]/div[1]"
  val maxDrawdownPercentValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[5]/div[2]/div[2]"

  // MISC XPATHS
//  val strategyNameXpath = "//html/body/div[2]/div[5]/div[1]/div[1]/div/div[2]/div[1]/div[2]/div/div[2]/div[2]/div/div[2]/div[1]/div[1]/div[1]/div"
  val strategyNameXpath = "//html/body/div[2]/div[5]/div[1]/div[1]/div/div[2]/div[1]/div[2]/div/div[1]/div[2]/div/div[2]/div[1]/div[1]/div[1]/div"
  val botifyVersionXPath = "//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[3]/div/span/span[1]/span/span"
  val deepBacktestingButtonXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[1]/div[1]/div[2]/div/span/input"
  val backtestingStartDateXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[1]/div[1]/div[3]/div/div[1]/div/div/div/div/span/span[1]/input"
  val backtestingEndDateXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[1]/div[1]/div[3]/div/div[3]/div/div/div/div/span/span[1]/input"
  val strategyTesterXPath = "//html/body/div[2]/div[7]/div[1]/div/div[1]/div[1]/div[3]/button"
}