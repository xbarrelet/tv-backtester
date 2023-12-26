package ch.xavier
package backtesting

import Application.{executionContext, system}

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import com.microsoft.playwright.*
import com.microsoft.playwright.options.Cookie

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

object MainBacktesterActor {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new MainBacktesterActor(context))
}

class MainBacktesterActor(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  implicit val timeout: Timeout = 300.seconds

  override def onMessage(message: Message): Behavior[Message] =
    message match
      case BacktestChartMessage() =>
        context.log.info(s"Starting backtesting")

        //TODO: Tu as trouve les bons fields. Detecte les dynamiquement et ima il faut creer la methode pour faire du backtest. Avec retour.

        val browserContext: BrowserContext = InitialiseBrowserContext(context)

        optimizeTPRR(context, browserContext)

      case _ =>
        context.log.warn("Received unknown message in MainBacktesterActor of type: " + message.getClass)

      this


  private def optimizeTPRR(context: ActorContext[Message], browserContext: BrowserContext): Unit =
    val parametersList: ListBuffer[List[ParametersToTest]] = ListBuffer()

    val profitsSelector = "xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[28]/div/span"
    val profitFactorLongXPath: String = "xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[69]/div/span/span[1]/input"
    val profitFactorShortXPath: String = "xpath=//html/body/div[6]/div/div/div[1]/div/div[3]/div/div[70]/div/span/span[1]/input"

    (5 to 50).map(i => {
      parametersList.addOne(
        List(ParametersToTest(profitsSelector, "R:R", "selectTakeProfit"), ParametersToTest(profitFactorLongXPath, (i/10.0).toString, "fill")))
    })

    val backtesterRef: ActorRef[Message] = context.spawn(BacktesterActor(), "backtester-actor")
        val response: Future[Message] =  backtesterRef ? (myRef => BacktestMessage(parametersList.result().head, browserContext, myRef))

    response.onComplete {
      case Success(result: Message) => println("Success:" + result)
      case Failure(ex) => println(s"Problem encountered : ${ex.getMessage}")
    }



  private def InitialiseBrowserContext(context: ActorContext[Message]): BrowserContext = {
    val chromiumBrowserType: BrowserType = Playwright.create().chromium()
    val browser: Browser = chromiumBrowserType.launch()

    val browserContext: BrowserContext = browser.newContext(
      Browser.NewContextOptions()
        .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36")
        .setViewportSize(1920, 1080)
    )

    val cookies: java.util.List[Cookie] = new java.util.ArrayList[Cookie]()
    cookies.add(new Cookie("sessionid", sys.env("SESSION_ID")).setDomain(".tradingview.com").setPath("/"))
    browserContext.addCookies(cookies)

    browserContext
  }
}
