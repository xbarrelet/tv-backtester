package ch.xavier
package backtesting.parameters

import scala.collection.mutable.ListBuffer
import scala.math.BigDecimal.double2bigDecimal


object StrategyParametersFactory {
  
  def getParameters(locator: TVLocator, min: Double, max: Double, step: Double = 1.0, initialParameter: StrategyParameter = StrategyParameter.empty): List[List[StrategyParameter]] =
    val parametersList: ListBuffer[List[StrategyParameter]] = ListBuffer()

    (min to max by step).map(value => {
      if !initialParameter.eq(StrategyParameter.empty) then
        parametersList.addOne(List(initialParameter, StrategyParameter(locator, value.toString())))
      else
        parametersList.addOne(List(StrategyParameter(locator, value.toString())))
    })
    
    parametersList.toList
}
