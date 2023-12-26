package ch.xavier

import TVLocators.{chartDeepBacktestingScalerXPath, welcomeLabelParametersModalXPath}

import com.microsoft.playwright.*
import com.microsoft.playwright.Page.GetByRoleOptions
import com.microsoft.playwright.options.{AriaRole, Cookie}
import org.slf4j.{Logger, LoggerFactory}

import java.nio.file.Paths

object PlaywrightService {
  
  def preparePage(): Page = {
    val page: Page = InitialiseBrowserContext().newPage()
    
    try {
      page.navigate(s"https://www.tradingview.com/chart/${sys.env("CHART_ID")}/")
      page.waitForSelector(chartDeepBacktestingScalerXPath).isVisible

      page.getByRole(AriaRole.SWITCH).click()

      page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Settings").setExact(true)).click()
      page.waitForSelector(welcomeLabelParametersModalXPath).isVisible

      page
    }
    catch
      case e: Exception =>
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("error.png")))
        val logger: Logger = LoggerFactory.getLogger("PlaywrightService")
        logger.error("Error when trying to open the chart, please check error.png screenshot to get an idea of what is happening")
        page.close()
        throw e
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
    
    browserContext.setDefaultTimeout(60000)

    browserContext
  }

}
