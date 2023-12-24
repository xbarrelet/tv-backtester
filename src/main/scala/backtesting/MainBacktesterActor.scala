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

        val page: Page = browserContext.newPage()
        page.navigate(s"https://www.tradingview.com/chart/$chartId/")
        Thread.sleep(3000)

        page.getByRole(AriaRole.SWITCH).click()
        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Settings").setExact(true)).click()
        Thread.sleep(3000)

        //TODO: Tu as trouve les bons fields. Detecte les dynamiquement et ima il faut creer la methode pour faire du backtest. Avec retour.

        optimizeTPRR(page, context)

    this

  //        val locators: java.util.List[Locator] = page.getByRole(AriaRole.SWITCH).click()


  // Strat options

  //        Thread.sleep(3000)
  //        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")))

  //        Thread.sleep(3000)


  //        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Email")).click()
  //
  //        page.getByLabel("Email or Username").fill(sys.env("TV_USERNAME"))


  private def optimizeTPRR(page: Page, context: ActorContext[Message]): Unit =
//    val locators: java.util.List[Locator] = page.getByRole(AriaRole.BUTTON).all()
//    val locators: java.util.List[Locator] = page.locator("xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[69]/div/span/span[1]/input")
    page.locator("xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[69]/div/span/span[1]/input").fill("42.42")
    page.locator("xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[71]/div/span/span[1]/input").fill("42.42")
    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")))
//    context.log.info(s"locators size: ${locators.size()}")
//
//    for locator <- locators.asScala do
//      context.log.info(s"locator: ${locator.innerHTML()}")
}
