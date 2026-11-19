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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (loaderDiagnosticsReportStoredXss), browser check: a loader setting holding
 * markup (here an SQL comment in the group query) shows as literal text in Loader > Diagnostics >
 * Run, with no element added and no script run, and the report's colored labels still render.
 * GrouperLoaderDiagnosticsReportHtmlTest covers the escaping itself; this covers the wiring in
 * UiV2GrouperLoader.loaderDiagnosticsRun in the running UI.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the database,
 * and the running UI uses the same database.  It creates and deletes its own folder
 * test:secPatch159 with one loader group.  The diagnostics only read the loader settings; the
 * loader itself is not run.</p>
 *
 * <p>Login is the UI's form login (or basic auth) as GrouperSystem/pass (local dev).  Raw
 * Playwright API so the same file works on every release that has Playwright.</p>
 */
public class LoaderDiagnosticsReportBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String FOLDER = "test:secPatch159";

  /** the loader group */
  private static final String LOADER_GROUP = FOLDER + ":loaderGroup";

  /** markup that adds a tagged element (and runs script) if the report is not escaped */
  private static final String PAYLOAD = "<img id=secPatch159xss src=x onerror=alert(159)>";

  /** group query with the payload inside an SQL comment (valid SQL, never run by diagnostics' checks of it) */
  private static final String GROUP_QUERY = "select 'test:secPatch159:a' as group_name /* " + PAYLOAD
      + " */ from grouper_members where 1=0";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(LoaderDiagnosticsReportBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public LoaderDiagnosticsReportBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public LoaderDiagnosticsReportBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- fixtures

  /**
   * delete the loader group and the folder if they exist
   */
  private static void deleteFixtures() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, LOADER_GROUP, false);
      if (group != null) {
        group.delete();
      }
      Stem stem = StemFinder.findByName(rootSession, FOLDER, false);
      if (stem != null) {
        stem.delete();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * make an SQL_GROUP_LIST loader group whose group query holds the payload
   * @return the group id
   */
  @SuppressWarnings("deprecation")
  private static String createLoaderGroup() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group loaderGroup = new GroupSave(rootSession).assignName(LOADER_GROUP)
          .assignCreateParentStemsIfNotExist(true).save();
      // legacy loader attributes: the same API on every release
      loaderGroup.addType(GroupTypeFinder.find("grouperLoader", true));
      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_TYPE, "SQL_GROUP_LIST");
      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_DB_NAME, "grouper");
      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY,
          "select 'test:secPatch159:a' as group_name, subject_id from grouper_members where 1=0");
      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY, GROUP_QUERY);
      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_GROUPS_LIKE, FOLDER + ":%");
      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, "CRON");
      loaderGroup.setAttribute(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, "0 0 4 * * ?");
      return loaderGroup.getId();
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
   * log in through the UI's form login (or basic auth if the UI answers 401)
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
   * open Loader > Diagnostics for the group (an allowed screen operation) and press Run
   * @param page
   * @param baseUrl
   * @param groupId
   * @return the results box once the report text with the payload is in it
   */
  private static Locator runDiagnostics(Page page, String baseUrl, String groupId) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2GrouperLoader.loaderDiagnostics&groupId=" + groupId);
    // the Run control: an <a> or <button> depending on the release, so match its onclick only
    Locator run = page.locator("[onclick*='UiV2GrouperLoader.loaderDiagnosticsRun']").first();
    run.waitFor();
    run.click();
    // escaped, the payload shows up as text; unescaped it becomes an element and this times out
    Locator results = page.locator("#grouperLoaderDiagnosticsResults",
        new Page.LocatorOptions().setHasText("secPatch159xss"));
    results.waitFor();
    return results;
  }

  // ---------------------------------------------------------------- test

  /**
   * the group query with markup is shown as literal text in the diagnostics report
   * @throws Exception
   */
  public void testGroupQueryMarkupShownAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    deleteFixtures();
    try {
      String groupId = createLoaderGroup();

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

        Locator results = null;
        RuntimeException last = null;
        // one retry on a busy shared Tomcat (timeouts only)
        for (int i = 0; i < 2 && results == null; i++) {
          try {
            results = runDiagnostics(page, baseUrl, groupId);
          } catch (com.microsoft.playwright.TimeoutError te) {
            last = te;
          }
        }
        if (results == null) {
          throw new RuntimeException("diagnostics report with the payload as text never showed (unescaped,"
              + " or the screen did not load); tagged elements: " + page.locator("#secPatch159xss").count(), last);
        }

        String text = results.textContent();
        assertTrue("group query must be shown literally: " + text, text.contains(PAYLOAD));
        assertEquals("payload must not add an element", 0, page.locator("#secPatch159xss").count());
        assertTrue("the report's colored labels still render",
            page.locator("#grouperLoaderDiagnosticsResults pre font").count() > 0);

        // give a late onerror a moment to fire before checking
        Thread.sleep(1000);
        assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

        browser.close();
      }
      System.out.println("secPatch159 testGroupQueryMarkupShownAsText done");
    } finally {
      deleteFixtures();
    }
  }

}
