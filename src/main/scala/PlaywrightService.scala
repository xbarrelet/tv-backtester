package ch.xavier

import com.microsoft.playwright.*
import com.microsoft.playwright.options.Cookie

class PlaywrightService {
  
//  private val browserContext: BrowserContext = InitialiseBrowserContext()


  def getEmptyPage: Page = this.synchronized {
    InitialiseBrowserContext().newPage()
//    browserContext.newPage()
  }


  private def InitialiseBrowserContext(): BrowserContext = {
//    val chromiumBrowserType: BrowserType = Playwright.create().chromium()
//    val browser: Browser = chromiumBrowserType.launch()
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

    browserContext.setDefaultTimeout(60000)

    browserContext
  }
}
