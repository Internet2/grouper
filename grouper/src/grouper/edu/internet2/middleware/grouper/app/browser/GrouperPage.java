package edu.internet2.middleware.grouper.app.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class GrouperPage {

  private Page page;
  private Browser browser;
  
  public GrouperPage initializePage() {
    Playwright playwright = Playwright.create();
    BrowserType chromium = playwright.chromium();
    this.browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(true));
    this.page = browser.newPage();
    BrowserContext context = browser.newContext();
    return this;
  }
  
  public void close() {
    if (this.page != null) {
      try {
        this.page.close();
      } catch (Exception e) {
        // ignore
      }
    }
    if (this.browser != null) {
      try {
        this.browser.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }
  
  public Browser getBrowser() {
    return browser;
  }


  
  public void setBrowser(Browser browser) {
    this.browser = browser;
  }


  public Page getPage() {
    return page;
  }

  
  public void setPage(Page page) {
    this.page = page;
  }
  
}
