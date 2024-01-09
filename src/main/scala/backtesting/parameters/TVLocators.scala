package ch.xavier
package backtesting.parameters

enum TYPE {
  case INPUT, CHECKBOX, OPTION
}

enum TVLocators(locatorType: TYPE, index: Int) {

  def getType: TYPE = locatorType

  def getIndex: Int = index


  // TP
  case TP_TYPE extends TVLocators(TYPE.OPTION, 83)
  case TP_SHORT_FIXED_PERCENTS extends TVLocators(TYPE.INPUT, 9)
  case TP_SHORT_RR extends TVLocators(TYPE.INPUT, 17)
  case TP_LONG_FIXED_PERCENTS extends TVLocators(TYPE.INPUT, 7)
  case TP_LONG_RR extends TVLocators(TYPE.INPUT, 16)

  case USE_MULTI_PROFIT extends TVLocators(TYPE.CHECKBOX, 27)
  case USE_MULTI_PROFIT_TP1 extends TVLocators(TYPE.CHECKBOX, 28)
  case USE_MULTI_PROFIT_TP2 extends TVLocators(TYPE.CHECKBOX, 29)
  case TP1_PERCENTS extends TVLocators(TYPE.INPUT, 51)
  case TP1_LEVEL extends TVLocators(TYPE.INPUT, 52)
  case TP2_PERCENTS extends TVLocators(TYPE.INPUT, 53)
  case TP2_LEVEL extends TVLocators(TYPE.INPUT, 54)
  case TP3_PERCENTS extends TVLocators(TYPE.INPUT, 55)
  case TP3_LEVEL extends TVLocators(TYPE.INPUT, 56)

  // SL
  case SL_TYPE extends TVLocators(TYPE.OPTION, 84)
  case SL_SHORT_FIXED_PERCENTS extends TVLocators(TYPE.INPUT, 8)
  case SL_LONG_FIXED_PERCENTS extends TVLocators(TYPE.INPUT, 6)
  case SL_ATR_MULTIPLIER extends TVLocators(TYPE.INPUT, 20)

  // HH
  case HIGHEST_HIGH_LOOKBACK extends TVLocators(TYPE.INPUT, 14)
  case LOWEST_LOW_LOOKBACK extends TVLocators(TYPE.INPUT, 15)

  // TRAILING
  case USE_TRAILING_LOSS extends TVLocators(TYPE.CHECKBOX, 10)
  case USE_TRAILING_TP extends TVLocators(TYPE.CHECKBOX, 11)
  case TRAILING_ACTIVATION extends TVLocators(TYPE.OPTION, 91)
  case TRAILING_LOSS_THRESHOLD extends TVLocators(TYPE.INPUT, 22)
  case TRAILING_LOSS_ATR_MULTIPLIER extends TVLocators(TYPE.INPUT, 23)

  // LEVERAGE
  case PERCENT_OF_PORTFOLIO_PER_TRADE extends TVLocators(TYPE.INPUT, 30)
  case LEVERAGE_PERCENT extends TVLocators(TYPE.INPUT, 31)
  case USE_DYNAMIC_LEVERAGE extends TVLocators(TYPE.CHECKBOX, 17)

  // DEADZONE V5
  case DEADZONE_SENSITIVITY extends TVLocators(TYPE.INPUT, 65)
  case DEADZONE_FAST_EMA extends TVLocators(TYPE.INPUT, 66)
  case DEADZONE_SLOW_EMA extends TVLocators(TYPE.INPUT, 67)
  case DEADZONE_BB_CHANNEL_LENGTH extends TVLocators(TYPE.INPUT, 68)
  case DEADZONE_BB_STDEV_MULTIPLIER extends TVLocators(TYPE.INPUT, 69)
  case DEADZONE_DEADZONE_PARAMETER extends TVLocators(TYPE.INPUT, 70)


  // AFFINEMENT
  // USE MA
  case MA_TYPE extends TVLocators(TYPE.OPTION, 101)

  // RANGE FILTER
  case USE_RANGE_FILTER extends TVLocators(TYPE.CHECKBOX, -52)
  case RANGE_FILTER_PERIOD extends TVLocators(TYPE.INPUT, -49)
  case RANGE_FILTER_MULTIPLIER extends TVLocators(TYPE.INPUT, -48)

  //HURST EXPONENT
  case USE_HURST_EXP extends TVLocators(TYPE.CHECKBOX, -49)
  case USE_HURST_EXP_MTF extends TVLocators(TYPE.CHECKBOX, -48)
  case HURST_EXP_LENGTH extends TVLocators(TYPE.INPUT, -43)

  // FLAT MARKET
  case USE_FLAT_MARKET extends TVLocators(TYPE.CHECKBOX, -38)
  case FLAT_MARKET_MA_LENGTH extends TVLocators(TYPE.INPUT, -26)
  case FLAT_MARKET_ABOVE_LINE extends TVLocators(TYPE.INPUT, -25)

  // VWAP
  case USE_VWAP_CROSSOVER extends TVLocators(TYPE.CHECKBOX, -35)
  case VWAP_LENGTH extends TVLocators(TYPE.INPUT, -25)


  // BACKTESTING RESULTS XPATHS
  val netProfitsPercentageValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[1]/div[2]/div[2]"
  val closedTradesNumberXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[2]/div[2]/div[1]"
  val profitabilityPercentageValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[3]/div[2]/div[1]"
  val profitFactorValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[4]/div[2]/div[1]"
  val maxDrawdownPercentValueXPath = "//html/body/div[2]/div[7]/div[2]/div[4]/div/div[3]/div/div[1]/div[5]/div[2]/div[2]"
}