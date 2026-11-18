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
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory M5 (attestationAdminAuthorizationBypass), browser check: only group ADMIN may
 * edit, disable or remove a group's attestation settings (GRP-5105); READ+UPDATE may still attest
 * and clear the review date.  UiV2AttestationEditAdminTest covers the handlers; this test checks
 * what it cannot: that the patched groupAttestationMoreActionsButtonContents.jsp (and the new
 * AttestationContainer.canEditAttestation it calls) compiles and renders in the running UI, that
 * an updater's More actions menu has attest and clear but no "Edit attestation settings", that
 * attest and clear still work from that menu, and that a group admin still gets the edit item and
 * the edit form.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch106 with one group with direct attestation, test.subject.0 READ+UPDATE,
 * test.subject.1 ADMIN; group and folder are deleted at the end.</p>
 *
 * <p>Login is the UI's form login as test.subject.0/pass and test.subject.1/pass (local dev),
 * one browser context each.  Uses the raw Playwright API (not GrouperPage) so the same file works
 * on releases without the app.browser helpers.</p>
 */
public class AttestationEditAdminBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch106";

  /** group with attestation settings */
  private static final String GROUP_NAME = STEM_NAME + ":secPatch106grp";

  /** menu item that opens the edit settings form: href holds the operation on 6.x/7.x, but 4.x has
   * href="#" with the operation only in onclick (guiV2link), so match onclick, which every release has */
  private static final String EDIT_ITEM = "#group-attestation-more-options [onclick*='UiV2Attestation.editGroupAttestation']";

  /** menu item that marks the group reviewed */
  private static final String ATTEST_ITEM = "#group-attestation-more-options [onclick*='UiV2Attestation.attestGroup']";

  /** menu item that clears the review date */
  private static final String CLEAR_ITEM = "#group-attestation-more-options [onclick*='UiV2Attestation.clearGroupAttestation']";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AttestationEditAdminBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public AttestationEditAdminBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public AttestationEditAdminBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- data helpers

  /**
   * create the folder and the group with direct attestation, and the two test subjects' privileges
   * (same setup as UiV2AttestationEditAdminTest)
   * @return the group id
   */
  private static String createData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave().assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
      AttributeAssign assign = group.getAttributeDelegate().assignAttribute(
          GrouperAttestationJob.retrieveAttributeDefNameValueDef()).getAttributeAssign();
      assign.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
      assign.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      assign.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "false");
      assign.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName(), "30");
      return group.getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the group and folder this test created (also clears a leftover from an aborted run)
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
   * new browser context (own cookies) logged in through the UI's form login
   * @param browser
   * @param baseUrl
   * @param loginId e.g. test.subject.0
   * @param dialogs collects any alert/confirm/prompt
   * @return the page
   */
  private static Page login(Browser browser, String baseUrl, String loginId, final List<String> dialogs) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    // fail in seconds, not the 30 second Playwright default, when a screen does not render
    page.setDefaultTimeout(15000);
    page.onDialog(dialog -> {
      dialogs.add(dialog.message());
      dialog.dismiss();
    });
    com.microsoft.playwright.Response loginResponse = page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    if (loginResponse != null && loginResponse.status() == 401) {
      // this UI uses HTTP basic authentication (grouper.is.ui.basicAuthn), not the form login
      page.setExtraHTTPHeaders(java.util.Collections.singletonMap("Authorization", "Basic "
          + java.util.Base64.getEncoder().encodeToString((loginId + ":pass").getBytes(java.nio.charset.StandardCharsets.UTF_8))));
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    }
    if (page.locator("input[type=password]").count() > 0) {
      page.locator("input[type=text]").first().fill(loginId);
      page.locator("input[type=password]").first().fill("pass");
      page.locator("input[type=password]").first().press("Enter");
      // logged in when the login form is gone (waitForURL races a fast login on Playwright 1.46, whose
      // waitForURL always waits for a new navigation); 60 s for the first login after a Tomcat start
      page.locator("input[type=password]").first().waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
          .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED).setTimeout(60000));
    }
    return page;
  }

  /**
   * open the group's attestation screen and its More actions menu
   * @param page
   * @param baseUrl
   * @param groupId
   */
  private static void openMenu(Page page, String baseUrl, String groupId) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Attestation.groupAttestation&groupId=" + groupId);
    // if the attestation screen does not render, fail with what the page shows instead of a bare timeout
    try {
      page.locator("#group-attestation-more-options").waitFor(
          new com.microsoft.playwright.Locator.WaitForOptions().setState(
              com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
    } catch (RuntimeException re) {
      String text = page.locator("body").innerText();
      fail("attestation screen did not render for " + page.url() + "; page text: "
          + (text.length() > 1500 ? text.substring(0, 1500) : text));
    }
    // the toggle right before the attestation options list: id group-attestation-more-action-button
    // (a button) on newer releases, the shared id more-action-button (a link) on 6.x and older
    page.locator("#group-attestation-more-options")
        .locator("xpath=preceding-sibling::*[@data-toggle='dropdown'][1]").click();
    page.locator("#group-attestation-more-options").waitFor();
  }

  /**
   * click a menu item and wait for the success message it shows
   * @param page
   * @param selector
   */
  private static void clickForSuccess(Page page, String selector) {
    // clear old messages so the wait sees this action's message
    page.evaluate("() => { var m = document.getElementById('messaging'); if (m) { m.innerHTML = ''; } }");
    page.locator(selector).click();
    // wait for any message, then require success, so a refusal shows its text in the failure
    page.locator("#messaging .alert").first().waitFor();
    assertEquals("expected a success message for " + selector + ", got: "
        + page.locator("#messaging").innerText(), 1, page.locator("#messaging .alert-success").count());
  }

  // ---------------------------------------------------------------- test

  /**
   * an updater's menu has attest and clear (which work) but no edit item; an admin's menu has the
   * edit item and it opens the edit form
   * @throws Exception
   */
  public void testEditItemOnlyForAdmin() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    deleteData();
    String groupId = createData();
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      String baseUrl = uiBaseUrl();
      final List<String> dialogs = new ArrayList<String>();

      // ---- updater (READ+UPDATE, no ADMIN)
      Page updater = login(browser, baseUrl, SubjectTestHelper.SUBJ0_ID, dialogs);
      openMenu(updater, baseUrl, groupId);
      assertEquals("updater menu must have attest", 1, updater.locator(ATTEST_ITEM).count());
      assertEquals("updater menu must have clear", 1, updater.locator(CLEAR_ITEM).count());
      assertEquals("updater menu must not have Edit attestation settings", 0, updater.locator(EDIT_ITEM).count());

      // attest and clear still work for the updater (each redraws the screen with a message)
      clickForSuccess(updater, ATTEST_ITEM);
      openMenu(updater, baseUrl, groupId);
      clickForSuccess(updater, CLEAR_ITEM);

      // ---- admin
      Page admin = login(browser, baseUrl, SubjectTestHelper.SUBJ1_ID, dialogs);
      openMenu(admin, baseUrl, groupId);
      assertEquals("admin menu must have Edit attestation settings", 1, admin.locator(EDIT_ITEM).count());
      admin.locator(EDIT_ITEM).click();
      admin.locator("#editGroupAttestationFormId").waitFor();

      assertTrue("no dialog expected: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteData();
    }
  }

}
