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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (visualizationObjectIdReflectedXss), browser check: the objectId request
 * parameter of the visualization screen is written into a hidden input by
 * visualization/visualizationMain.jsp, and must be escaped.  VisualizationMainJspEscapingTest
 * checks the JSP source; this test checks what that test cannot: that the patched JSP compiles
 * in the running UI and that a crafted link's payload comes back as the literal input value,
 * with no script run and no injected element.
 *
 * <p>Opens the crafted link the way a victim would (UiV2Main.index with the operation and
 * parameters in the url; UiV2Visualization.subjectView is on advisory C1's deep-link allowlist):
 * subjectIdOrIdentifier=GrouperSystem satisfies the subject lookup, and
 * objectId=g:isa||||GrouperSystem||||&lt;payload&gt; with objectType=subject resolves to
 * GrouperSystem because VisualizationContainer.getGrouperObject only looks at the first two
 * "|" separated parts.  Nothing is created; the screen only reads.</p>
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Login is the UI's form login (or HTTP
 * basic) as GrouperSystem/pass (local dev).  Raw Playwright API (not GrouperPage) so the same
 * file works on every release with Playwright.</p>
 */
public class VisualizationObjectIdBrowserTest extends TestCase {

  /** attribute-breaking payload; if it is not escaped it closes the value and adds an img */
  private static final String PAYLOAD = "\"><img id=secPatch147xss src=x onerror=alert(147)>";

  /** the object id the UI itself uses for GrouperSystem (sourceId||||subjectId) */
  private static final String GROUPER_SYSTEM_OBJECT_ID = "g:isa||||GrouperSystem";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(VisualizationObjectIdBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public VisualizationObjectIdBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public VisualizationObjectIdBrowserTest(String name) {
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
   * url-encode a request parameter value
   * @param value
   * @return the encoded value
   */
  private static String encode(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (java.io.UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
  }

  /**
   * open the crafted subject visualization link with this objectId, wait for the settings form
   * the ajax renders, and read the hidden input back
   * @param page
   * @param baseUrl
   * @param objectId value of the objectId parameter
   * @return the value of the vis-settings-objectid hidden input as the browser parsed it
   */
  private static String openWithObjectId(Page page, String baseUrl, String objectId) {
    // operation first: grouperUi.js builds the ajax url from the operation and appends the rest
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Visualization.subjectView"
        + "&subjectIdOrIdentifier=GrouperSystem"
        + "&objectType=subject"
        + "&objectId=" + encode(objectId));
    // the objectType input follows the objectId input, so it is rendered escaped or not
    page.locator("#vis-settings-objecttype").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    page.locator("#vis-settings-objectid").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    return (String) page.evaluate("() => document.getElementById('vis-settings-objectid').value");
  }

  // ---------------------------------------------------------------- test

  /**
   * an ordinary subject object id is kept; a payload after it comes back as the literal value,
   * no dialog opens and no element from the payload lands in the page
   * @throws Exception
   */
  public void testObjectIdPayloadRenderedAsText() throws Exception {
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

      // positive control: the object id the UI itself uses is echoed as is
      String plainValue;
      try {
        plainValue = openWithObjectId(page, baseUrl, GROUPER_SYSTEM_OBJECT_ID);
      } catch (com.microsoft.playwright.TimeoutError te) {
        // one retry for a busy shared Tomcat
        plainValue = openWithObjectId(page, baseUrl, GROUPER_SYSTEM_OBJECT_ID);
      }
      assertEquals("ordinary object id must be kept", GROUPER_SYSTEM_OBJECT_ID, plainValue);

      // reported sink: a third part after sourceId||||subjectId is ignored by the lookup but echoed
      String crafted = GROUPER_SYSTEM_OBJECT_ID + "||||" + PAYLOAD;
      String payloadValue = openWithObjectId(page, baseUrl, crafted);
      assertEquals("objectId hidden input must hold the payload as literal text", crafted, payloadValue);
      assertEquals("payload must not add an element", 0, page.locator("#secPatch147xss").count());

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    }
    System.out.println("^secPatch147 browser test ran");
  }

}
