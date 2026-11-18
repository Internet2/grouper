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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (daemonJobHistoryFilterReflectedXss), browser check: the "names like"
 * filter and the from/to time boxes on the daemon job history chart screen come from the
 * request, and admin/adminJobHistoryChart.jsp must render them escaped.
 * AdminJobHistoryChartJspEscapingTest checks the JSP source; this test checks what that test
 * cannot: that the patched JSP compiles in the running UI and that a typed payload comes back
 * as literal text in the input box, with no script run and no injected element.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  No data or config is created: the
 * screen only reads grouper_loader_log, and an empty result still renders the form.</p>
 *
 * <p>Login is the UI's form login (or HTTP basic) as GrouperSystem/pass (local dev).  Uses the
 * raw Playwright API (not GrouperPage) so the same file works on releases without the
 * app.browser helpers.</p>
 */
public class AdminJobHistoryChartBrowserTest extends TestCase {

  /** attribute-breaking payload; if it is not escaped it closes the value and adds an img */
  private static final String PAYLOAD = "\"><img id=secPatch140xss src=x onerror=alert(140)>";

  /** the refresh button (an input type=submit in every release today; no tag in the selector) */
  private static final String SUBMIT = "#jobHistoryChartSubmitButtonId";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AdminJobHistoryChartBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public AdminJobHistoryChartBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public AdminJobHistoryChartBrowserTest(String name) {
    super(name);
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
   * log in through the UI's form login, or HTTP basic if the UI answers 401
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
   * open the job history chart screen (an allowed screen operation), put a value into one
   * filter box and press Refresh, the way a user would.  The refresh re-renders the whole
   * screen from the request parameters, which is the same JSP output a crafted link reaches.
   * @param page
   * @param baseUrl
   * @param inputId names-like-filter or time-from
   * @param value what to type into the box
   * @return the value the box shows after the refresh re-renders the screen
   */
  private static String submitValue(Page page, String baseUrl, String inputId, String value) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Admin.jobHistoryChart");
    page.locator("#" + inputId).waitFor();
    page.locator("#" + inputId).fill(value);
    // mark the box so we can tell when the refresh has replaced it with a freshly rendered one
    page.evaluate("id => document.getElementById(id).setAttribute('data-secpatch-old', 'true')", inputId);
    page.locator(SUBMIT).click();
    page.locator("#" + inputId + ":not([data-secpatch-old])").waitFor();
    return page.locator("#" + inputId).inputValue();
  }

  // ---------------------------------------------------------------- test

  /**
   * the names-like filter and the from time come back as literal text; no dialog opens and no
   * element from the payload lands in the page
   * @throws Exception
   */
  public void testFilterPayloadRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    try (Playwright playwright = Playwright.create()) {
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

      // positive control: an ordinary wildcard filter is kept as typed
      String plain = "%secPatch140%";
      String plainValue;
      try {
        plainValue = submitValue(page, baseUrl, "names-like-filter", plain);
      } catch (com.microsoft.playwright.TimeoutError te) {
        // one retry for a busy shared Tomcat
        plainValue = submitValue(page, baseUrl, "names-like-filter", plain);
      }
      assertEquals("ordinary filter must be kept", plain, plainValue);

      // reported sink: namesLikeFilter
      String namesValue = submitValue(page, baseUrl, "names-like-filter", PAYLOAD);
      assertEquals("names like filter box must hold the payload as literal text", PAYLOAD, namesValue);
      assertEquals("payload must not add an element", 0, page.locator("#secPatch140xss").count());

      // the time is parsed with SimpleDateFormat.parse, which ignores trailing text, so a valid
      // time followed by the payload reaches the page too
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Admin.jobHistoryChart");
      page.locator("#time-from").waitFor();
      String timeFrom = page.locator("#time-from").inputValue() + PAYLOAD;
      String timeValue = submitValue(page, baseUrl, "time-from", timeFrom);
      assertEquals("from time box must hold the payload as literal text", timeFrom, timeValue);
      assertEquals("payload must not add an element", 0, page.locator("#secPatch140xss").count());

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    }
    System.out.println("^secPatch140 browser test ran");
  }

}
