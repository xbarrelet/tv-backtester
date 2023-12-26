package ch.xavier

import LocatorsXPaths.{chartDeepBacktestingScalerXPath, welcomeLabelParametersModalXPath}

import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.{AriaRole, Cookie}
import com.microsoft.playwright.*

object PlaywrightService {

  def preparePage(): Page = {
    val browserContext: BrowserContext = InitialiseBrowserContext()

    val page: Page = browserContext.newPage()
    page.navigate(s"https://www.tradingview.com/chart/${sys.env("CHART_ID")}/")
    page.waitForSelector(s"xpath=/$chartDeepBacktestingScalerXPath").isVisible

    page.getByRole(AriaRole.SWITCH).click()

    page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Settings").setExact(true)).click()
    page.waitForSelector(s"xpath=/$welcomeLabelParametersModalXPath").isVisible

    page
  }


  private def InitialiseBrowserContext(): BrowserContext = {
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
