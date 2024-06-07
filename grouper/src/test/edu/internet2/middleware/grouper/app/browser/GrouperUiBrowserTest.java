package edu.internet2.middleware.grouper.app.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;


public class GrouperUiBrowserTest extends GrouperTest {

  public GrouperUiBrowserTest() {
    super();
  }

  public GrouperUiBrowserTest(String name) {
    super(name);
  }
  public static void main(String[] args) {
    new GrouperUiBrowserTest().runTestVersion();
  }
  /**
   * We can't delete the grouper database or the ui will get confused, so this is not a real junit test.
   */
  public void runTestVersion() {
    Playwright playwright = Playwright.create();
    BrowserType chromium = playwright.chromium();
    Browser browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(true));
    try {
      Page page = browser.newPage();
      BrowserContext context = browser.newContext();
      page.navigate("http://GrouperSystem:pass@localhost:8080/grouper");
      GrouperUiBrowserGeneralVerifyVersion grouperUiBrowserGeneralVerifyVersion = new GrouperUiBrowserGeneralVerifyVersion(page).browse();
      // get the current ui version
      String uiVersion = grouperUiBrowserGeneralVerifyVersion.getUiVersion().toString();
      
      // Confirm the current ui version
      new GrouperUiBrowserGeneralVerifyVersion(page).assignExpectedVersion("4.0.0").browse();
      
      try {
        new GrouperUiBrowserGeneralVerifyVersion(page).assignExpectedVersion("1.2.3").browse();
        throw new RuntimeException("This should fail because it is the wrong version");
      } catch (Exception e) {
        // This is good
      }
      
    } finally {
      browser.close();
    }
    System.exit(0);
  }
}
