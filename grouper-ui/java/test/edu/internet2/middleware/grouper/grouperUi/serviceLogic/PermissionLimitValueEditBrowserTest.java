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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import junit.framework.TestCase;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (permissionLimitValueStoredXss), browser check: a permission limit value is
 * stored as typed, and the "Edit value" screens (groupPermissionLimitValueEdit.jsp for a role,
 * permissionLimitValueEdit.jsp for a subject) put it in an input value attribute, so they must
 * escape it.  PermissionLimitValueEditJspEscapingTest checks the JSP source; this test checks what the
 * JUnit test cannot: that both patched JSPs compile in the running UI, that the edit box shows the
 * stored payload exactly (no script, no injected element), and that saving the box unchanged keeps
 * the value byte-for-byte (no double escaping).
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch83 with a role (test.subject.0 a member), a permission definition and name,
 * a role permission and a role/subject permission, each with an expression limit whose value is
 * the payload; all deleted at the end.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class PermissionLimitValueEditBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch83";

  /** role this test creates */
  private static final String ROLE_NAME = STEM_NAME + ":secPatch83role";

  /** permission definition this test creates */
  private static final String PERM_DEF_NAME = STEM_NAME + ":secPatch83permDef";

  /** subject added to the role, for the subject permission screen */
  private static final String SUBJECT_ID = "test.subject.0";

  /** source of SUBJECT_ID */
  private static final String SOURCE_ID = "jdbc";

  /** payload stored as the limit value; if it is not escaped it closes the value and adds an img */
  private static final String PAYLOAD = "true\"><img id=secPatch83xss src=x onerror=alert(83)>";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(PermissionLimitValueEditBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public PermissionLimitValueEditBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public PermissionLimitValueEditBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * ids the screens need
   */
  private static class Fixture {
    /** role group id */
    private String roleId;
    /** permission assignment on the role, carries a limit */
    private String rolePermissionAssignId;
    /** limit assignment on the role permission */
    private String roleLimitAssignId;
    /** permission assignment on the role/subject (effective membership), carries a limit */
    private String subjectPermissionAssignId;
    /** limit assignment on the role/subject permission */
    private String subjectLimitAssignId;
  }

  /**
   * put an expression limit with the payload value on a permission assignment
   * @param permissionAssign
   * @return the limit assignment id
   */
  private static String addLimit(AttributeAssign permissionAssign) {
    String limitName = PermissionLimitUtils.limitElAttributeDefName().getName();
    permissionAssign.getAttributeValueDelegate().assignValue(limitName, PAYLOAD);
    return permissionAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(limitName).getAttributeAssignId();
  }

  /**
   * create the role, permission and limits
   * @return the ids
   */
  private static Fixture createFixture() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Fixture fixture = new Fixture();
      Group role = new GroupSave(rootSession).assignName(ROLE_NAME)
          .assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
      fixture.roleId = role.getId();
      Subject subject = SubjectFinder.findByIdAndSource(SUBJECT_ID, SOURCE_ID, true);
      role.addMember(subject, false);

      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, true);
      AttributeDef permDef = stem.addChildAttributeDef("secPatch83permDef", AttributeDefType.perm);
      permDef.setAssignToGroup(true);
      permDef.setAssignToEffMembership(true);
      permDef.store();
      AttributeDefName permName = stem.addChildAttributeDefName(permDef, "secPatch83perm", "secPatch83perm");

      AttributeAssign rolePermissionAssign = role.getPermissionRoleDelegate()
          .assignRolePermission(permName, PermissionAllowed.ALLOWED).getAttributeAssign();
      fixture.rolePermissionAssignId = rolePermissionAssign.getId();
      fixture.roleLimitAssignId = addLimit(rolePermissionAssign);

      AttributeAssign subjectPermissionAssign = role.getPermissionRoleDelegate()
          .assignSubjectRolePermission(permName, subject, PermissionAllowed.ALLOWED).getAttributeAssign();
      fixture.subjectPermissionAssignId = subjectPermissionAssign.getId();
      fixture.subjectLimitAssignId = addLimit(subjectPermissionAssign);
      return fixture;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the stored limit value of a permission assignment, read fresh from the database
   * @param permissionAssignId
   * @return the value
   */
  private static String limitValue(String permissionAssignId) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      AttributeAssign permissionAssign = AttributeAssignFinder.findById(permissionAssignId, true);
      return permissionAssign.getAttributeValueDelegate().retrieveValueString(
          PermissionLimitUtils.limitElAttributeDefName().getName());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete what this test created (permission definition with its names and assignments, the
   * role, the folder); safe to call when some or all of it is gone
   */
  private static void deleteFixture() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      AttributeDef permDef = AttributeDefFinder.findByName(PERM_DEF_NAME, false);
      if (permDef != null) {
        permDef.delete();
      }
      Group role = GroupFinder.findByName(rootSession, ROLE_NAME, false);
      if (role != null) {
        role.delete();
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
   * on a permissions screen that is showing, open the limit value's menu and choose Edit value,
   * the way a user would (the edit operation is not an allowed deep link)
   * @param page
   * @param limitAssignId
   * @param logicClass UiV2GroupPermission or UiV2SubjectPermission (the screen's ajax class)
   * @return the value the edit box shows
   */
  private static String openEditValue(Page page, String limitAssignId, String logicClass) {
    Locator menuImage = page.locator("[id^='limitAssignValueButton_" + limitAssignId + "_']");
    menuImage.waitFor();
    // 6.5.0 and 7.x: a bootstrap dropdown whose Edit value item is in the DOM (closed menu).  It is
    // an <a> today, but other 7.x menus turned items into <button>, so match by onclick, not tag
    Locator editItem = page.locator("[onclick*='editLimitValue'][onclick*='limitAssignValueButton_" + limitAssignId + "_']");
    if (editItem.count() > 0) {
      // the image sits in the dropdown toggle button
      menuImage.locator("xpath=..").click();
      editItem.click();
    } else {
      // 4.26.0: a dhtmlx context menu (grouper:menu limitValueMenu) built from an XML structure;
      // clicking the image opens it and records the image id as the menu target.  The dhtmlx item
      // markup is not stable to select, so after the real click send what the menu's onClick sends
      // (grouperUi.js guiInitDhtmlxMenu): an in-page ajax call, not a URL deep link, so the advisory C1
      // guard does not apply
      String menuImageId = menuImage.first().getAttribute("id");
      menuImage.first().click();
      page.evaluate("args => ajax('../app/' + args[0] + '.limitValueMenu', {requestParams: "
          + "{menuItemId: 'editLimitValue', menuIdOfMenuTarget: args[1], menuEvent: 'onClick'}})",
          new String[] {logicClass, menuImageId});
    }
    page.locator("#valueToEditId").waitFor();
    return page.locator("#valueToEditId").inputValue();
  }

  /**
   * press the edit value screen's submit button (box unchanged) and wait for the screen to go
   * back to the permission list
   * @param page
   */
  private static void saveUnchanged(Page page) {
    page.locator("#simplePermissionLimitValueEditForm .btn-primary").click();
    page.locator("#valueToEditId").waitFor(new Locator.WaitForOptions()
        .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
  }

  // ---------------------------------------------------------------- test

  /**
   * role and subject "Edit value" screens show the stored payload exactly, add no element, open
   * no dialog, and an unchanged save keeps the value byte-for-byte
   * @throws Exception
   */
  public void testLimitValueEditRoundTrip() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    deleteFixture();
    try (Playwright playwright = Playwright.create()) {
      Fixture fixture = createFixture();
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

      // role permissions screen (allowed screen operation) -> limit value -> Edit value
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation="
          + "UiV2GroupPermission.groupPermission&groupId=" + fixture.roleId);
      String roleValue = openEditValue(page, fixture.roleLimitAssignId, "UiV2GroupPermission");
      assertEquals("payload must not add an element (role)", 0, page.locator("#secPatch83xss").count());
      assertEquals("role edit box must hold the stored value exactly", PAYLOAD, roleValue);
      saveUnchanged(page);
      assertEquals("unchanged save must keep the role limit value", PAYLOAD, limitValue(fixture.rolePermissionAssignId));

      // subject permissions screen (allowed screen operation) -> limit value -> Edit value
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation="
          + "UiV2SubjectPermission.subjectPermission&subjectId=" + SUBJECT_ID + "&sourceId=" + SOURCE_ID);
      String subjectValue = openEditValue(page, fixture.subjectLimitAssignId, "UiV2SubjectPermission");
      assertEquals("payload must not add an element (subject)", 0, page.locator("#secPatch83xss").count());
      assertEquals("subject edit box must hold the stored value exactly", PAYLOAD, subjectValue);
      saveUnchanged(page);
      assertEquals("unchanged save must keep the subject limit value", PAYLOAD, limitValue(fixture.subjectPermissionAssignId));

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteFixture();
    }
  }

}
