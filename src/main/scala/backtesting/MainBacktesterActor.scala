package ch.xavier
package backtesting

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.util.Timeout
import com.microsoft.playwright.*
import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.AriaRole

import java.nio.file.Paths
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.*

object MainBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new MainBacktesterActor(context))
}

class MainBacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 300.seconds


  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestChartMessage(chartId: String, browserContext: BrowserContext) =>
        context.log.info(s"Starting backtesting of chart id:$chartId")

        //TODO: Tu as trouve les bons fields. Detecte les dynamiquement et ima il faut creer la methode pour faire du backtest. Avec retour.

        optimizeTPRR(chartId, browserContext, context)

    this

  //        val locators: java.util.List[Locator] = page.getByRole(AriaRole.SWITCH).click()


  // Strat options

  //        Thread.sleep(3000)
  //        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")))

  //        Thread.sleep(3000)


  //        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Email")).click()
  //
  //        page.getByLabel("Email or Username").fill(sys.env("TV_USERNAME"))


  private def optimizeTPRR(chartId: String, browserContext: BrowserContext, context: ActorContext[Message]): Unit =
    val parametersList: List[List[ParametersToTest]] = List()

    val profitFactorLongXPath: String = "xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[69]/div/span/span[1]/input"
    //TODO: Fix me and then do all this in a new actor that with the Source.mapAsync and the ask. Make sure you limit the number of parallel requests.
    (5 to 50).map(i => List(ParametersToTest(profitFactorLongXPath, i.toString))) :: parametersList

    context.log.info(s"yo: $parametersList")

    for parameter <- parametersList do
      //      val page: Page = preparePage(chartId, browserContext)
      context.log.info(s"parameter: $parameter")
  //      page.close()

  private def preparePage(chartId: String, browserContext: BrowserContext) = {
    val page: Page = browserContext.newPage()
    page.navigate(s"https://www.tradingview.com/chart/$chartId/")
    Thread.sleep(3000)

    page.getByRole(AriaRole.SWITCH).click()
    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Settings").setExact(true)).click()
    Thread.sleep(3000)
    page
  }
}
