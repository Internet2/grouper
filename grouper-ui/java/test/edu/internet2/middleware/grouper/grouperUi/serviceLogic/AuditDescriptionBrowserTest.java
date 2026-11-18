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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (auditDescriptionStoredXss), browser check: the audit screens print stored
 * audit values (description, server user, host, ...) in the "extended results" columns, and the
 * patched group, folder and subject audit JSPs must escape them.  GuiAuditEntryEscapingTest checks the
 * JSP source and the GuiAuditEntry getters; this test checks what it cannot: that the patched
 * groupViewAuditsContents.jsp, stemViewAuditsContents.jsp and subjectViewAuditsContents.jsp
 * compile in the running UI, that an audit description holding a payload shows as literal text
 * with no script run and no injected element, and that the ordinary audit text in the action
 * column still renders its links.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch67 with one group, and two audit rows (a membership add on the group for
 * test.subject.0, and a folder privilege delete) whose description is the payload; the audit rows,
 * group and folder are deleted at the end.  The audit rows are written directly (same labels the
 * API uses) so the description is exactly the payload; a real description change would only
 * show the payload inside longer text.</p>
 *
 * <p>Not covered here: groupAttestationViewAuditsContents.jsp and subjectDeleteAuditsContents.jsp
 * (same one-line escaping; need attestation or USDU audit rows).</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class AuditDescriptionBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch67";

  /** group this test creates and deletes */
  private static final String GROUP_NAME = STEM_NAME + ":secPatch67grp";

  /** attribute-breaking payload; if it is not escaped it adds an img that runs onerror */
  private static final String PAYLOAD = "\"><img id=secPatch67xss src=x onerror=alert(67)>";

  /** ids of the audit rows this test wrote, deleted in cleanup */
  private final List<String> auditIds = new ArrayList<String>();

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AuditDescriptionBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public AuditDescriptionBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public AuditDescriptionBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- data helpers

  /**
   * create the folder, the group, and the two audit rows with the payload description
   * @return {groupId, stemId}
   */
  private String[] createData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave().assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, true);
      Member member = MemberFinder.findBySubject(rootSession, SubjectTestHelper.SUBJ0, true);

      // membership add on the group: shows on the group audit log and on test.subject.0's
      // (membership) audit log.  Same labels as Group.addMember
      AuditEntry membershipAudit = new AuditEntry(AuditTypeBuiltin.MEMBERSHIP_GROUP_ADD,
          "id", GrouperUuid.getUuid(), "fieldId", Group.getDefaultList().getUuid(),
          "fieldName", Group.getDefaultList().getName(), "memberId", member.getUuid(),
          "membershipType", "immediate", "groupId", group.getUuid(), "groupName", group.getName());
      membershipAudit.setDescription(PAYLOAD);
      membershipAudit.saveOrUpdate(true);
      this.auditIds.add(membershipAudit.getId());

      // folder privilege delete: shows on the folder audit log on every release (a folder privilege
      // ADD is only mapped to the folder log from 7.6.0, AuditFieldType).  Same labels as Stem.revokePriv
      AuditEntry privilegeAudit = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_STEM_DELETE,
          "privilegeName", NamingPrivilege.STEM_ADMIN.getName(), "memberId", member.getUuid(),
          "privilegeType", "naming", "stemId", stem.getUuid(), "stemName", stem.getName());
      privilegeAudit.setDescription(PAYLOAD);
      privilegeAudit.saveOrUpdate(true);
      this.auditIds.add(privilegeAudit.getId());

      return new String[] {group.getId(), stem.getId()};
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the audit rows, group and folder this test created (the group/folder deletes also
   * clear a leftover from an aborted run)
   */
  private void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String auditId : this.auditIds) {
        HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry where id = ?",
            GrouperUtil.toListObject(auditId));
      }
      this.auditIds.clear();
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
   * open an audit log screen (an allowed screen operation), tick "show extended results" and
   * press the filter button, the way a user would
   * @param page
   * @param baseUrl
   * @param operationAndParams e.g. UiV2Group.viewAudits&groupId=...
   * @return the description cell holding the payload text (waited for)
   */
  private static Locator openExtendedAudits(Page page, String baseUrl, String operationAndParams) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=" + operationAndParams);
    page.locator("input[name=showExtendedResults]").check();
    page.locator("#auditLogSubmitButtonId").click();
    // escaped, the payload is cell text; unescaped, the cell has an img and this never appears
    Locator cell = page.locator("td", new Page.LocatorOptions().setHasText("secPatch67xss"));
    cell.first().waitFor();
    return cell.first();
  }

  /**
   * check one audit screen: description cell holds the payload as text, no injected element, and
   * the action column of that row still has its links
   * @param page
   * @param baseUrl
   * @param label for messages
   * @param operationAndParams
   */
  private static void assertScreen(Page page, String baseUrl, String label, String operationAndParams) {
    Locator cell = openExtendedAudits(page, baseUrl, operationAndParams);
    assertEquals(label + ": description must show the payload as text", PAYLOAD, cell.textContent().trim());
    assertEquals(label + ": payload must not add an element", 0, page.locator("#secPatch67xss").count());
    Locator row = page.locator("tr", new Page.LocatorOptions().setHas(cell));
    assertTrue(label + ": audit text in the row must still render links",
        // any element with an href (the guiSubjectShortLink / guiGroupShortLink text), not a tag
        row.first().locator("[href]").count() > 0);
  }

  // ---------------------------------------------------------------- test

  /**
   * the payload description shows as literal text on the group, folder and subject audit logs
   * with extended results; no dialog opens and no element from the payload lands in the page
   * @throws Exception
   */
  public void testAuditDescriptionRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    deleteData();
    String[] ids = createData();
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

      assertScreen(page, baseUrl, "group", "UiV2Group.viewAudits&groupId=" + ids[0]);
      assertScreen(page, baseUrl, "folder", "UiV2Stem.viewAudits&stemId=" + ids[1]);
      assertScreen(page, baseUrl, "subject", "UiV2Subject.viewAudits&sourceId=jdbc&subjectId="
          + SubjectTestHelper.SUBJ0_ID);

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteData();
    }
  }

}
