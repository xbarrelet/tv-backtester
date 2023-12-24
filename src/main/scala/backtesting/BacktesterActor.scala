//package ch.xavier
//package backtesting
//
//import akka.actor.typed.{ActorRef, Behavior}
//import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
//import akka.stream.scaladsl.Source
//import akka.util.Timeout
//import com.microsoft.playwright.*
//import com.microsoft.playwright.Page.GetByRoleOptions
//import com.microsoft.playwright.options.AriaRole
//
//import java.nio.file.Paths
//import scala.concurrent.duration.DurationInt
//import scala.jdk.CollectionConverters.*
//
//object BacktesterActor {
//  def apply(): Behavior[Message] =
//    Behaviors.setup(context => new BacktesterActor(context))
//}
//
//class BacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
//  implicit val timeout: Timeout = 300.seconds
//
//  private def preparePage(chartId: String, browserContext: BrowserContext) = {
//    val page: Page = browserContext.newPage()
//    page.navigate(s"https://www.tradingview.com/chart/$chartId/")
//    Thread.sleep(3000)
//
//    page.getByRole(AriaRole.SWITCH).click()
//    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Settings").setExact(true)).click()
//    Thread.sleep(3000)
//    page
//  }
//
//  private def optimizeTPRR(chartId: String, browserContext: BrowserContext, context: ActorContext[Message]): Unit =
//    context.log.info("yo1")
//      val parametersList: List[List[ParametersToTest]] = List()
//
//      val profitFactorLongXPath: String = "xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[69]/div/span/span[1]/input"
//      (5 to 50).map(i => parametersList :+ List(ParametersToTest(profitFactorLongXPath, (i/10.0).toString)))
//
//      context.log.info("yo2")
//
//      for parameter <- parametersList do
//  //      val page: Page = preparePage(chartId, browserContext)
//        context.log.info(s"parameter: $parameter")
//  //      page.close()
//
//  //    val profitFactorShortXPath: String = "xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[71]/div/span/span[1]/input"
//  //    (5 to 50).map(i => parametersList :+ List(ParametersToTest(profitFactorShortXPath, (i/10.0).toString)))
//
//  //    page.locator().fill("42.42")
//  //    page.locator("xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[71]/div/span/span[1]/input").fill("42.42")
//  //    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")))
//  //    context.log.info(s"locators size: ${locators.size()}")
//  //
//  //    for locator <- locators.asScala do
//  //      context.log.info(s"locator: ${locator.innerHTML()}")
//
//  override def onMessage(message: Message): Behavior[Message] =
//    message match
//      case BacktestMessage(page: Page, parametersToTest: List[ParametersToTest], actorRef: ActorRef[Message]) =>
//        context.log.info(s"Starting backtesting of chart id:$chartId")
//
//        //TODO: Tu as trouve les bons fields. Detecte les dynamiquement et ima il faut creer la methode pour faire du backtest. Avec retour.
//
//        optimizeTPRR(chartId, browserContext, context)
//
//    this
//
//  //        val locators: java.util.List[Locator] = page.getByRole(AriaRole.SWITCH).click()
//
//
//  // Strat options
//
//  //        Thread.sleep(3000)
//  //        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")))
//
//  //        Thread.sleep(3000)
//
//
//  //        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Email")).click()
//  //
//  //        page.getByLabel("Email or Username").fill(sys.env("TV_USERNAME"))
//
//}
