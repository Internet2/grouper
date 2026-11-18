/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (externalSystemConnectionTestStoredXss), browser check: a connection test
 * result can carry data from the external system (here a database row), and
 * UiV2ExternalSystem.testExternalSystemConfigDetails must escape it (ExternalSystemTestErrorHtml)
 * before it goes to the HTML message area.  ExternalSystemTestErrorHtmlTest covers the helper;
 * this test checks what it cannot: that the running UI wires the helper in, so the Test action on
 * the external systems screen shows a payload row as literal text, with no script run and no
 * injected element.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * one Database external system (secPatch37) in the database config, pointing at the Grouper
 * database itself (connection from grouper.hibernate.properties), whose test query returns the
 * payload and whose expected value is SAFE, so the test reports a mismatch that contains the
 * payload; deleted at the end.  No table is needed: the query selects a literal.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class ExternalSystemTestErrorBrowserTest extends TestCase {

  /** external system config id this test creates and deletes */
  private static final String CONFIG_ID = "secPatch37";

  /** payload the test query returns; if it is not escaped it adds an img */
  private static final String PAYLOAD = "\"><img id=secPatch37xss src=x onerror=alert(37)>";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ExternalSystemTestErrorBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public ExternalSystemTestErrorBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public ExternalSystemTestErrorBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- config helpers

  /** suffixes (after db.secPatch37.) of the keys this test stores, so cleanup deletes exactly those */
  private static final String[] KEY_SUFFIXES = {"url", "user", "pass", "driver", "testQuery", "testExpectedValue"};

  /**
   * create the Database external system in the database config
   */
  private static void createExternalSystem() {
    GrouperHibernateConfig hibernateConfig = GrouperHibernateConfig.retrieveConfig();
    String[] values = {
        hibernateConfig.propertyValueStringRequired("hibernate.connection.url"),
        hibernateConfig.propertyValueStringRequired("hibernate.connection.username"),
        hibernateConfig.propertyValueString("hibernate.connection.password"),
        hibernateConfig.propertyValueString("hibernate.connection.driver_class"),
        // single quotes around a literal; the payload has none of its own
        "select '" + PAYLOAD + "'",
        "SAFE",
    };
    // GrouperDbConfig checks the caller may edit config, so it needs a (root) session
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (int i = 0; i < KEY_SUFFIXES.length; i++) {
        if (values[i] == null || values[i].trim().length() == 0) {
          continue;
        }
        new GrouperDbConfig().configFileName("grouper-loader.properties")
            .propertyName("db." + CONFIG_ID + "." + KEY_SUFFIXES[i]).value(values[i]).store();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the config keys this test stored
   */
  private static void deleteExternalSystem() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String suffix : KEY_SUFFIXES) {
        try {
          new GrouperDbConfig().configFileName("grouper-loader.properties")
              .propertyName("db." + CONFIG_ID + "." + suffix).delete();
        } catch (RuntimeException re) {
          // already gone; keep cleaning up the rest
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * base url of the running UI: grouper.ui.url (what GrouperPage uses) if set, else the same
   * junit.test.tomcat.* keys the mock tests use
   * @return e.g. http://localhost:8080/grouper
   */
  private static String uiBaseUrl() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    String uiUrl = config.propertyValueString("grouper.ui.url");
    if (uiUrl != null && uiUrl.trim().length() > 0) {
      return uiUrl.trim().replaceAll("/+$", "");
    }
    boolean ssl = config.propertyValueBoolean("junit.test.tomcat.ssl", false);
    String host = config.propertyValueString("junit.test.tomcat.domainName", "localhost");
    int port = config.propertyValueInt("junit.test.tomcat.port", 8080);
    String context = config.propertyValueString("junit.test.tomcat.uiContext", "grouper");
    return (ssl ? "https" : "http") + "://" + host + ":" + port + "/" + context;
  }

  // ---------------------------------------------------------------- browser helpers

  /**
   * log in through the UI's form login
   * @param page
   * @param baseUrl
   */
  private static void login(Page page, String baseUrl) {
    com.microsoft.playwright.Response loginResponse = page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    if (loginResponse != null && loginResponse.status() == 401) {
      // this UI uses HTTP basic authentication (grouper.is.ui.basicAuthn), not the form login
      page.setExtraHTTPHeaders(java.util.Collections.singletonMap("Authorization", "Basic "
          + java.util.Base64.getEncoder().encodeToString(("GrouperSystem" + ":pass").getBytes(java.nio.charset.StandardCharsets.UTF_8))));
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    }
    if (page.locator("input[type=password]").count() > 0) {
      page.locator("input[type=text]").first().fill("GrouperSystem");
      page.locator("input[type=password]").first().fill("pass");
      page.locator("input[type=password]").first().press("Enter");
      // logged in when the login form is gone (waitForURL races a fast login on Playwright 1.46, whose
      // waitForURL always waits for a new navigation); 60 s for the first login after a Tomcat start
      page.locator("input[type=password]").first().waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
          .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED).setTimeout(60000));
    }
  }

  /**
   * open the external systems screen (an allowed screen operation), open secPatch37's actions
   * menu and click Test, the way a user would (the test operation is not an allowed deep link)
   * @param page
   * @param baseUrl
   * @return the text of the message area after the test
   */
  private static String runConnectionTest(Page page, String baseUrl) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2ExternalSystem.viewExternalSystems");
    // match by onclick only, no tag (menu items are <a> here but became <button> in other 7.x menus)
    Locator testLink = page.locator("[onclick*='testExternalSystemConfigDetails?externalSystemConfigId="
        + CONFIG_ID + "&']");
    testLink.waitFor(new Locator.WaitForOptions()
        .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
    // the link is in a dropdown; its toggle button is the list's previous sibling
    testLink.locator("xpath=ancestor::ul[1]/preceding-sibling::*[1]").click();
    testLink.click();
    Locator message = page.locator("#messaging").filter(new Locator.FilterOptions().setHasText("SAFE"));
    message.waitFor();
    return message.textContent();
  }

  // ---------------------------------------------------------------- test

  /**
   * the connection test mismatch message shows the payload row as literal text; no dialog opens
   * and no element from the payload lands in the page
   * @throws Exception
   */
  public void testConnectionTestPayloadRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    deleteExternalSystem();
    try (Playwright playwright = Playwright.create()) {
      createExternalSystem();
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newContext().newPage();
      // fail in seconds, not the 30 second Playwright default, when a screen does not render
      page.setDefaultTimeout(15000);

      // any alert/confirm/prompt means the payload ran
      final List<String> dialogs = new ArrayList<String>();
      page.onDialog(dialog -> {
        dialogs.add(dialog.message());
        dialog.dismiss();
      });

      String baseUrl = uiBaseUrl();
      login(page, baseUrl);

      // the running UI reads database config through a cache; retry until it sees secPatch37
      String messageText = null;
      RuntimeException last = null;
      for (int i = 0; i < 6 && messageText == null; i++) {
        try {
          messageText = runConnectionTest(page, baseUrl);
        } catch (RuntimeException re) {
          last = re;
          Thread.sleep(5000);
        }
      }
      if (messageText == null) {
        throw new RuntimeException("connection test never ran for " + CONFIG_ID, last);
      }

      assertEquals("payload must not add an element", 0, page.locator("#secPatch37xss").count());
      assertTrue("mismatch message must show the payload as literal text: " + messageText,
          messageText.contains(PAYLOAD));

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteExternalSystem();
    }
  }

}
