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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.grouper.SubjectFinder;
import junit.textui.TestRunner;

/**
 * Security advisory C1 (uiClientSideCsrfSysadminTakeover), browser check of the client side URL
 * operation guard: grouperUiUrlOperationGuard.js, loaded by commonBottom.jsp, must refuse an
 * action operation that comes from the browser URL (a crafted link), while screen deep links and
 * in-page buttons keep working.  GrouperUiUrlOperationGuardJsTest only checks the source text; this
 * test runs the real JS in a real browser against the running UI.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch52 with one group, deleted at the end.  The victim is GrouperSystem (form
 * login GrouperSystem/pass, local dev) and the subject the link tries to add is test.subject.0.</p>
 *
 * <p>Uses the raw Playwright API (not GrouperPage) so the same file works on releases without the
 * app.browser helpers.</p>
 */
public class UrlOperationGuardBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch52";

  /** group the crafted link tries to add a member to */
  private static final String GROUP_NAME = STEM_NAME + ":group";

  /** subject the crafted link tries to add */
  private static final String SUBJECT_ID = "test.subject.0";

  /** start of the guard's refusal message (grouperUiUrlOperationGuard.js NOT_ALLOWED_MESSAGE) */
  private static final String NOT_ALLOWED_TEXT = "This action cannot be run from a link";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UrlOperationGuardBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UrlOperationGuardBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public UrlOperationGuardBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- data helpers

  /**
   * create the folder and group (the parent folder test may already exist; it is left alone)
   */
  private static void createGroup() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new StemSave().assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
      new GroupSave().assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the group and folder this test created
   */
  private static void deleteGroup() {
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
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @return true if test.subject.0 is a member of the group (read from the database)
   */
  private static boolean subjectIsMember() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, true);
      Subject subject = SubjectFinder.findByIdAndSource(SUBJECT_ID, "jdbc", true);
      return group.hasMember(subject);
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

  // ---------------------------------------------------------------- test

  /**
   * a crafted link to UiV2Group.addMemberSubmit is refused (message shown, nobody added); a deep
   * link to the UiV2Group.viewGroup screen still loads; the screen's own Add member button still
   * adds the member
   * @throws Exception
   */
  public void testCraftedActionLinkRefusedScreenAndButtonWork() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    createGroup();
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newContext().newPage();
      // fail in seconds, not the 30 second Playwright default, when a screen does not render
      page.setDefaultTimeout(15000);

      // page script errors would mean the guard broke the page
      final List<String> pageErrors = new ArrayList<String>();
      page.onPageError(error -> pageErrors.add(error));
      final List<String> consoleWarnings = new ArrayList<String>();
      page.onConsoleMessage(message -> {
        if ("warning".equals(message.type())) {
          consoleWarnings.add(message.text());
        }
      });

      String baseUrl = uiBaseUrl();
      login(page, baseUrl);
      assertFalse("precondition: " + SUBJECT_ID + " not in " + GROUP_NAME, subjectIsMember());

      // 1. the crafted link (md Reproduction shape) must be refused and add nobody
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Group.addMemberSubmit"
          + "&groupName=" + GROUP_NAME.replace(":", "%3A")
          + "&groupAddMemberComboName=jdbc%7C%7C" + SUBJECT_ID + "&privilege-options%5B%5D=default");
      page.getByText(NOT_ALLOWED_TEXT).first().waitFor();
      // give any request the link might have started time to finish before checking the database
      Thread.sleep(2000);
      assertFalse("crafted link must not add " + SUBJECT_ID, subjectIsMember());
      boolean warned = false;
      for (String warning : consoleWarnings) {
        warned = warned || warning.contains("UiV2Group.addMemberSubmit");
      }
      assertTrue("guard must log a console warning naming the operation: " + consoleWarnings, warned);

      // 2. a deep link to a screen loads normally (no refusal)
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup"
          + "&groupName=" + GROUP_NAME.replace(":", "%3A"));
      page.locator("#add-members-submit").waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
          .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
      assertEquals("screen deep link must not be refused", 0, page.getByText(NOT_ALLOWED_TEXT).count());

      // 3. the screen's own Add member button (an action, not from the URL) still works
      page.locator("#show-add-block").click();
      page.locator("#add-members-submit").waitFor();
      // set the combobox value the way a pick from its list does (the box submits groupAddMemberComboName)
      page.evaluate("v => { var e = document.getElementsByName('groupAddMemberComboName');"
          + " for (var i = 0; i < e.length; i++) { e[i].value = v; } }", "jdbc||" + SUBJECT_ID);
      page.locator("#add-members-submit").click();
      boolean added = false;
      for (int i = 0; i < 20 && !added; i++) {
        Thread.sleep(500);
        added = subjectIsMember();
      }
      assertTrue("in-page Add member button must still add " + SUBJECT_ID, added);
      assertEquals("in-page button must not be refused", 0, page.getByText(NOT_ALLOWED_TEXT).count());

      assertTrue("no page script errors: " + pageErrors, pageErrors.isEmpty());

      browser.close();
    } finally {
      deleteGroup();
    }
  }

  /**
   * 2026-11-11: screens whose handler acts when a request parameter selects another branch are no longer allowed from
   * the browser URL.  A crafted daemon jobs link with action=runNow and a crafted provisioner diagnostics link with
   * provisionerInitted=true are refused (message shown, console warning names the operation).  The job and provisioner
   * names do not exist, so even without the guard nothing would run (the handler would just error), but the refusal
   * message only comes from the guard.  The daemon jobs screen still opens from an in-page link (guiV2link, what the
   * menus call).
   * @throws Exception
   */
  public void testActingScreenLinksRefusedMenuStillWorks() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newContext().newPage();
      page.setDefaultTimeout(15000);
      final List<String> pageErrors = new ArrayList<String>();
      page.onPageError(error -> pageErrors.add(error));
      final List<String> consoleWarnings = new ArrayList<String>();
      page.onConsoleMessage(message -> {
        if ("warning".equals(message.type())) {
          consoleWarnings.add(message.text());
        }
      });

      String baseUrl = uiBaseUrl();
      login(page, baseUrl);

      String[][] craftedLinks = new String[][] {
          {"UiV2Admin.daemonJobs", "&action=runNow&jobName=secPatch52NoSuchJob"},
          {"UiV2ProvisionerConfiguration.diagnostics", "&provisionerConfigId=secPatch52NoSuchProvisioner"
              + "&provisionerInitted=true&diagnosticsGroupNameName=test%3AsecPatch52Diagnostics"
              + "&diagnosticsSubjectIdOrIdentifierName=" + SUBJECT_ID},
      };
      for (String[] craftedLink : craftedLinks) {
        consoleWarnings.clear();
        page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=" + craftedLink[0] + craftedLink[1]);
        page.getByText(NOT_ALLOWED_TEXT).first().waitFor();
        boolean warned = false;
        for (String warning : consoleWarnings) {
          warned = warned || warning.contains(craftedLink[0]);
        }
        assertTrue("guard must refuse " + craftedLink[0] + " from the URL: " + consoleWarnings, warned);
      }

      // the menu way in: an in-page link (guiV2link) to the daemon jobs screen still loads it
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
      page.locator("#grouperMainContentDivId").waitFor();
      Thread.sleep(1000);
      consoleWarnings.clear();
      page.evaluate("() => guiV2link('operation=UiV2Admin.daemonJobs')");
      page.locator("#daemonJobsResultsId").waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
          .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
      assertEquals("in-page link to daemon jobs must not be refused", 0, page.getByText(NOT_ALLOWED_TEXT).count());
      assertTrue("no page script errors: " + pageErrors, pageErrors.isEmpty());
      browser.close();
    }
  }
}
