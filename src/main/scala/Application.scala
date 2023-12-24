package ch.xavier

import backtesting.MainBacktesterActor

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import com.microsoft.playwright.*
import com.microsoft.playwright.options.Cookie

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

object Application extends App {
  implicit val system: ActorSystem[Message] = ActorSystem(Main(), "System")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
}


object Main {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new Main(context))
}

private class Main(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 3600.seconds

  private val sessionId: String = "bug0i5znij5vdax705j9gjo54owxeqzi"
  private val chartIdToOptimize: String = "WyK0KuOH"

  context.log.info("The backtester is starting")

  private val backtesterRef: ActorRef[Message] = context.spawn(MainBacktesterActor(), "backtester-actor")
  private val browserContext: BrowserContext = InitialiseBrowserContext(context, sessionId)

  context.log.info("Browser prepared for crawling")

  backtesterRef ! BacktestChartMessage(chartIdToOptimize, browserContext)


  override def onMessage(message: Message): Behavior[Message] =
    this

  private def InitialiseBrowserContext(context: ActorContext[Message], sessionId: String): BrowserContext = {
    val chromiumBrowserType: BrowserType = Playwright.create().chromium()
    val browser: Browser = chromiumBrowserType.launch()

    val browserContext: BrowserContext = browser.newContext(
      Browser.NewContextOptions()
        .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36")
        .setViewportSize(1920, 1080)
    )

    val cookies: java.util.List[Cookie] = new java.util.ArrayList[Cookie]()
    cookies.add(new Cookie("sessionid", sessionId).setDomain(".tradingview.com").setPath("/"))
    browserContext.addCookies(cookies)
    browserContext
  }
}