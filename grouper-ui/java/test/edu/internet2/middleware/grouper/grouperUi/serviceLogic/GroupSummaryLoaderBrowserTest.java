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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 * Security advisory H6 (loaderGroupSummaryStoredXss), browser check: the group Summary tab
 * (groupSummary.jsp, from 5.20.6) prints the loader's SQL query, LDAP filter and JEXL script in a
 * pre element, and the patched JSP html-escapes them.  This test stores a SQL loader query that
 * closes the pre element and adds an img, opens the Summary tab in the running UI, and checks the
 * query shows as text with no injected element and no dialog.  The LDAP filter and JEXL script
 * sinks are the same one-line change on the same page and are not planted separately.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * the folder test:secPatch125 with one group carrying legacy SQL_SIMPLE loader attributes
 * (cron at midnight, so no daemon runs it during the test); deleted at the end.</p>
 */
public class GroupSummaryLoaderBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch125";

  /** loader group */
  private static final String GROUP_NAME = STEM_NAME + ":secPatch125group";

  /** valid SQL with the payload in a comment; closes the pre and adds an img if not escaped */
  private static final String SQL_QUERY = "select subject_id from grouper_members /* secPatch125sql "
      + "</pre><img id=secPatch125xss src=x onerror=alert(125)> */";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GroupSummaryLoaderBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GroupSummaryLoaderBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public GroupSummaryLoaderBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * create the loader group with the proof query
   * @return the group id
   */
  private static String createData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      // legacy loader attributes, the same ones the loader edit screen saves
      group.addType(GroupTypeFinder.find("grouperLoader", true));
      group.setAttribute(GrouperLoader.GROUPER_LOADER_TYPE, "SQL_SIMPLE");
      group.setAttribute(GrouperLoader.GROUPER_LOADER_DB_NAME, "grouper");
      group.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, SQL_QUERY);
      group.setAttribute(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, "CRON");
      group.setAttribute(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, "0 0 0 * * ?");
      return group.getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the folder and group (the loader attributes go with the group)
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, false);
      if (group != null) {
        group.delete();
      }
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.delete();
      }
    } catch (RuntimeException re) {
      re.printStackTrace();
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
   * log in through the UI's form login (or HTTP basic if the UI asks for it)
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
   * whether a UI logic class has a public screen method (operation) with the usual signature; the
   * test JVM has the same release's classes as the running UI
   * @param theClass
   * @param methodName
   * @return true if the operation exists in this release
   */
  private static boolean hasMethod(Class<?> theClass, String methodName) {
    try {
      theClass.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
      return true;
    } catch (NoSuchMethodException nsme) {
      return false;
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * the Summary tab shows the loader query as text in its pre element, with no injected element
   * and no dialog
   * @throws Exception
   */
  public void testLoaderQueryEscapedOnSummary() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // the Summary tab (groupSummary.jsp) came in 5.20.6; 5.20.6 to 5.21.3 render it from
    // viewGroup, 5.22.0 and later have viewGroupSummary (viewGroup forwards to it by default)
    String summaryOperation = hasMethod(UiV2Group.class, "viewGroupSummary")
        ? "UiV2Group.viewGroupSummary" : "UiV2Group.viewGroup";

    // a previous aborted run may have left data behind
    deleteData();
    try (Playwright playwright = Playwright.create()) {
      String groupId = createData();
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

      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=" + summaryOperation + "&groupId=" + groupId);
      Locator queryPre = page.locator("pre", new Page.LocatorOptions().setHasText("secPatch125sql"));
      queryPre.first().waitFor();

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertEquals("payload must not add an element", 0, page.locator("#secPatch125xss").count());
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());
      // escaped, the whole query (with its </pre><img ...> text) is the pre's text
      assertEquals("pre holds the whole query as text", SQL_QUERY, queryPre.first().textContent().trim());

      browser.close();
      System.out.println("^secPatch125 group summary loader query checked with " + summaryOperation);
    } finally {
      deleteData();
    }
  }

}
