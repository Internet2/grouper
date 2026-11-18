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
 * Security advisory H6 (attributeActionNameStoredXss), browser check: permission action names are
 * free text, and the action list/edit screens, the role and subject permission matrices and the
 * limit add value / edit limit / edit value screens must render them escaped, and the matrix's
 * onclick and URL strings must still target the right action.  AttributeActionNameEscapingTest checks
 * the JSP source and the limit tooltip; this test checks what it cannot: that the ten patched JSPs
 * compile and render in the running UI with a payload action name as literal text (no script, no
 * injected element), and that the matrix image click and action menu still work for a name with a
 * quote and a backslash.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch70 with a role (test.subject.0 a member), a permission definition with two
 * actions (the payload and a'b\c), role permissions for both, a role/subject permission for the
 * payload, and an expression limit on each payload permission; all deleted at the end.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class AttributeActionNameBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch70";

  /** role this test creates */
  private static final String ROLE_NAME = STEM_NAME + ":secPatch70role";

  /** permission definition this test creates */
  private static final String PERM_DEF_NAME = STEM_NAME + ":secPatch70permDef";

  /** subject added to the role, for the subject permission screen */
  private static final String SUBJECT_ID = "test.subject.0";

  /** source of SUBJECT_ID */
  private static final String SOURCE_ID = "jdbc";

  /**
   * action name payload; if it is not escaped it closes an attribute and adds an svg.  Action
   * names are at most 40 characters (grouper_attr_assign_action.name), hence the short id
   */
  private static final String PAYLOAD = "\"><svg id=sp70x onload=alert(70)>";

  /** action name with a javascript string quote and a backslash */
  private static final String QUOTE_ACTION = "a'b\\c";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AttributeActionNameBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public AttributeActionNameBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public AttributeActionNameBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * ids the screens need
   */
  private static class Fixture {
    /** role group id */
    private String roleId;
    /** permission definition id */
    private String permDefId;
    /** role permission assignment for the payload action */
    private String payloadRoleAssignId;
    /** role permission assignment for the quote action */
    private String quoteRoleAssignId;
    /** limit on the payload role permission */
    private String roleLimitAssignId;
    /** limit on the payload role/subject permission */
    private String subjectLimitAssignId;
  }

  /**
   * put an expression limit (value true) on a permission assignment
   * @param permissionAssign
   * @return the limit assignment id
   */
  private static String addLimit(AttributeAssign permissionAssign) {
    String limitName = PermissionLimitUtils.limitElAttributeDefName().getName();
    permissionAssign.getAttributeValueDelegate().assignValue(limitName, "true");
    return permissionAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(limitName).getAttributeAssignId();
  }

  /**
   * create the role, permission definition, actions, assignments and limits
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
      AttributeDef permDef = stem.addChildAttributeDef("secPatch70permDef", AttributeDefType.perm);
      permDef.setAssignToGroup(true);
      permDef.setAssignToEffMembership(true);
      permDef.store();
      fixture.permDefId = permDef.getId();
      permDef.getAttributeDefActionDelegate().addAction(PAYLOAD);
      permDef.getAttributeDefActionDelegate().addAction(QUOTE_ACTION);
      AttributeDefName permName = stem.addChildAttributeDefName(permDef, "secPatch70perm", "secPatch70perm");

      AttributeAssign payloadRoleAssign = role.getPermissionRoleDelegate()
          .assignRolePermission(PAYLOAD, permName, PermissionAllowed.ALLOWED).getAttributeAssign();
      fixture.payloadRoleAssignId = payloadRoleAssign.getId();
      fixture.roleLimitAssignId = addLimit(payloadRoleAssign);
      fixture.quoteRoleAssignId = role.getPermissionRoleDelegate()
          .assignRolePermission(QUOTE_ACTION, permName, PermissionAllowed.ALLOWED).getAttributeAssign().getId();

      AttributeAssign payloadSubjectAssign = role.getPermissionRoleDelegate()
          .assignSubjectRolePermission(PAYLOAD, permName, subject, PermissionAllowed.ALLOWED).getAttributeAssign();
      fixture.subjectLimitAssignId = addLimit(payloadSubjectAssign);
      return fixture;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete what this test created (permission definition with its names, actions and
   * assignments, the role, the folder); safe to call when some or all of it is gone
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
   * open an allowed screen operation
   * @param page
   * @param baseUrl
   * @param operationAndParams e.g. UiV2GroupPermission.groupPermission&amp;groupId=...
   */
  private static void openScreen(Page page, String baseUrl, String operationAndParams) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=" + operationAndParams);
  }

  /**
   * the payload must not have added an element anywhere on the page
   * @param where
   */
  private static void assertNoInjectedElement(Page page, String where) {
    assertEquals("payload must not add an element: " + where, 0, page.locator("#sp70x").count());
  }

  /**
   * wait for a screen's element, then check it shows the payload action name as literal text and
   * that nothing was injected
   * @param page
   * @param selector element of the screen that holds the action name
   * @param where
   */
  private static void assertScreenShowsPayload(Page page, String selector, String where) {
    page.locator(selector).first().waitFor();
    assertNoInjectedElement(page, where);
    assertTrue("action name must show as literal text: " + where,
        page.locator(selector).first().textContent().contains(PAYLOAD));
  }

  /**
   * index of the first element matching the css selector whose attribute ends with the suffix
   * (ids and names hold action names, which quote and backslash css selectors badly)
   * @param page
   * @param css
   * @param attribute id or name
   * @param suffix
   * @return the locator of that element
   */
  private static Locator byAttributeSuffix(Page page, String css, String attribute, String suffix) {
    page.locator(css).first().waitFor();
    Object index = page.evaluate("args => Array.from(document.querySelectorAll(args[0]))"
        + ".findIndex(e => (e.getAttribute(args[1]) || '').endsWith(args[2]))",
        new Object[] {css, attribute, suffix});
    int i = ((Number)index).intValue();
    assertTrue("no " + css + " with " + attribute + " ending " + suffix, i >= 0);
    return page.locator(css).nth(i);
  }

  /**
   * open a dropdown whose toggle button holds the image, then click the menu item.
   * 6.x/7.x render a bootstrap dropdown (toggle around the image, then a ul of links whose
   * onclick has the menu item id).  4.26.0 has no ul: the image sits in a plain link that a
   * dhtmlx context menu (grouper:menu, built in the browser from a structure XML) is attached
   * to, so there the test sends what that menu's onClick sends:
   * ajax('../app/&lt;uiClass&gt;.&lt;menu operation&gt;', {requestParams: {menuItemId, menuIdOfMenuTarget:
   * the image id}}).  Same server handler, same target id, so the "right action" check still holds
   * @param page
   * @param menuImage
   * @param menuItemId e.g. editAssignment
   * @param uiClass UiV2GroupPermission or UiV2SubjectPermission (which screen the image is on)
   */
  private static void clickMenuItem(Page page, Locator menuImage, String menuItemId, String uiClass) {
    // any element with the menu item id in onclick: menu items are <a> here, but other 7.x menus
    // turned them into <button>, so do not depend on the tag
    Locator menuItem = menuImage.locator("xpath=../../ul//*[contains(@onclick, \"menuItemId: '" + menuItemId + "'\")]");
    if (menuItem.count() > 0) {
      menuImage.locator("xpath=..").click();
      menuItem.click();
      return;
    }
    // 4.26.0 dhtmlx menu: the image id prefix says which menu (see grouper:menu in the JSPs)
    String imageId = menuImage.getAttribute("id");
    String menuOperation = imageId.startsWith("permissionMenuButton_") ? "assignmentMenu"
        : imageId.startsWith("limitMenuButton_") ? "limitMenu" : "limitValueMenu";
    // id is read from the element (not a css string), so quotes and backslashes in the action are fine
    // block body: return nothing, not whatever ajax() returns
    menuImage.evaluate("(img, args) => { ajax('../app/' + args[0], {requestParams: "
        + "{menuItemId: args[1], menuIdOfMenuTarget: img.id, menuEvent: 'onClick'}}); }",
        new Object[] {uiClass + "." + menuOperation, menuItemId});
  }

  /**
   * on a permission screen, open a limit's menu (or its value's menu) and choose an item
   * @param page
   * @param imageIdPrefix limitMenuButton_&lt;id&gt; or limitAssignValueButton_&lt;id&gt;_
   * @param menuItemId
   * @param uiClass UiV2GroupPermission or UiV2SubjectPermission
   */
  private static void clickLimitMenuItem(Page page, String imageIdPrefix, String menuItemId, String uiClass) {
    Locator menuImage = page.locator("[id^='" + imageIdPrefix + "']").first();
    menuImage.waitFor();
    clickMenuItem(page, menuImage, menuItemId, uiClass);
  }

  /**
   * the allow/deny checkbox of an action in the role matrix
   * @param page
   * @param action
   * @return the checkbox
   */
  private static Locator matrixCheckbox(Page page, String action) {
    return byAttributeSuffix(page, "input[name^='permissionCheckbox__']", "name", "__" + action);
  }

  /**
   * click the allow/deny image of an action in the role matrix, then reopen the matrix until the
   * action's checkbox shows the expected state.  The matrix is reopened instead of waiting for the
   * ajax re-render because the click's success message text keys
   * (simplePermissionUpdate.permissionAllowRole / permissionRevokeRole) are missing from the
   * en_us text file, so the click saves and then shows an error instead of re-rendering
   * (unrelated to this issue)
   * @param page
   * @param baseUrl
   * @param screen the role permission screen operation and params
   * @param action
   * @param expectChecked state the checkbox must reach
   * @throws InterruptedException
   */
  private static void clickMatrixImage(Page page, String baseUrl, String screen, String action,
      boolean expectChecked) throws InterruptedException {
    // the allow/deny link right after the checkbox, found by its onclick, not its tag
    matrixCheckbox(page, action).locator(
        "xpath=following-sibling::*[contains(@onclick, 'permissionPanelImageClick')][1]").click();
    for (int i = 0; i < 10; i++) {
      Thread.sleep(1000);
      openScreen(page, baseUrl, screen);
      if (matrixCheckbox(page, action).isChecked() == expectChecked) {
        return;
      }
    }
    fail("image click must " + (expectChecked ? "allow" : "revoke") + " action " + action);
  }

  // ---------------------------------------------------------------- test

  /**
   * every patched screen shows the payload action name as literal text with no injected element
   * and no dialog; the matrix image click and the action menu still target the right action for
   * a name with a quote and a backslash
   * @throws Exception
   */
  public void testActionNameRenderedAsText() throws Exception {
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

      // ---- attribute def actions list (attributeDefActions.jsp)
      openScreen(page, baseUrl, "UiV2AttributeDefAction.attributeDefActions&attributeDefId=" + fixture.permDefId);
      assertScreenShowsPayload(page, "#attributeDefActionsToDeleteFormId", "action list");

      // ---- action edit (attributeDefActionEdit.jsp): the payload action itself, and the other
      // action, whose imply checkboxes list the payload
      // 6.x/7.x: href="?operation=...editAttributeDefAction..."; 4.26.0: href="#" and
      // onclick="return guiV2link('operation=...editAttributeDefAction...')", so match either
      // (no tag, so a later <button> menu item still matches)
      Locator editLinks = page.locator("#attributeDefActionsToDeleteFormId [href*='editAttributeDefAction'], "
          + "#attributeDefActionsToDeleteFormId [onclick*='editAttributeDefAction']");
      int editLinkCount = editLinks.count();
      // the payload, the quote action and the default "assign" action
      assertEquals("one edit link per action", 3, editLinkCount);
      for (int i = 0; i < editLinkCount; i++) {
        String href = editLinks.nth(i).getAttribute("href");
        String params;
        if (href != null && href.indexOf("operation=") >= 0) {
          params = href.substring(href.indexOf("operation=") + "operation=".length());
        } else {
          // 4.26.0: the params are the guiV2link argument, up to its closing quote
          String onclick = editLinks.nth(i).getAttribute("onclick");
          params = onclick.substring(onclick.indexOf("operation=") + "operation=".length());
          params = params.substring(0, params.indexOf('\''));
        }
        openScreen(page, baseUrl, params);
        assertScreenShowsPayload(page, "#editAttributeDefActionForm", "action edit " + i);
        openScreen(page, baseUrl, "UiV2AttributeDefAction.attributeDefActions&attributeDefId=" + fixture.permDefId);
        page.locator("#attributeDefActionsToDeleteFormId").waitFor();
      }

      // ---- role permission matrix (groupViewPermissions.jsp)
      String rolePermissionScreen = "UiV2GroupPermission.groupPermission&groupId=" + fixture.roleId;
      openScreen(page, baseUrl, rolePermissionScreen);
      assertScreenShowsPayload(page, "#attributePermissionsFormId", "role matrix");

      // action menu "edit assignment" must open the assignment of the clicked action
      String[][] actionToAssignId = {{PAYLOAD, fixture.payloadRoleAssignId}, {QUOTE_ACTION, fixture.quoteRoleAssignId}};
      for (String[] actionAndAssignId : actionToAssignId) {
        openScreen(page, baseUrl, rolePermissionScreen);
        Locator menuImage = byAttributeSuffix(page, "[id^='permissionMenuButton_']", "id",
            "__" + actionAndAssignId[0] + "__role");
        clickMenuItem(page, menuImage, "editAssignment", "UiV2GroupPermission");
        page.locator("#simplePermissionEditForm input[name='permissionAssignmentId']").waitFor(
            new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
        assertEquals("edit assignment must target action " + actionAndAssignId[0], actionAndAssignId[1],
            page.locator("#simplePermissionEditForm input[name='permissionAssignmentId']").getAttribute("value"));
        assertNoInjectedElement(page, "edit assignment");
      }

      // allow/deny image of the quote action: revoke, then allow again
      openScreen(page, baseUrl, rolePermissionScreen);
      assertTrue("quote action starts allowed", matrixCheckbox(page, QUOTE_ACTION).isChecked());
      clickMatrixImage(page, baseUrl, rolePermissionScreen, QUOTE_ACTION, false);
      clickMatrixImage(page, baseUrl, rolePermissionScreen, QUOTE_ACTION, true);
      assertNoInjectedElement(page, "role matrix after image clicks");

      // ---- role limit screens (groupPermissionLimitAddValue/LimitEdit/LimitValueEdit.jsp)
      openScreen(page, baseUrl, rolePermissionScreen);
      clickLimitMenuItem(page, "limitMenuButton_" + fixture.roleLimitAssignId, "addValue", "UiV2GroupPermission");
      assertScreenShowsPayload(page, "#simplePermissionLimitAddValueForm", "role limit add value");
      openScreen(page, baseUrl, rolePermissionScreen);
      clickLimitMenuItem(page, "limitMenuButton_" + fixture.roleLimitAssignId, "editLimit", "UiV2GroupPermission");
      assertScreenShowsPayload(page, "#simplePermissionLimitEditForm", "role limit edit");
      openScreen(page, baseUrl, rolePermissionScreen);
      clickLimitMenuItem(page, "limitAssignValueButton_" + fixture.roleLimitAssignId + "_", "editLimitValue", "UiV2GroupPermission");
      assertScreenShowsPayload(page, "#simplePermissionLimitValueEditForm", "role limit value edit");

      // ---- subject permission matrix (subjectViewPermissions.jsp) and its limit screens
      // (permissionLimitAddValue/LimitEdit/LimitValueEdit.jsp)
      String subjectPermissionScreen = "UiV2SubjectPermission.subjectPermission&subjectId=" + SUBJECT_ID
          + "&sourceId=" + SOURCE_ID;
      openScreen(page, baseUrl, subjectPermissionScreen);
      assertScreenShowsPayload(page, "#attributePermissionsFormId", "subject matrix");
      clickLimitMenuItem(page, "limitMenuButton_" + fixture.subjectLimitAssignId, "addValue", "UiV2SubjectPermission");
      assertScreenShowsPayload(page, "#simplePermissionLimitAddValueForm", "subject limit add value");
      openScreen(page, baseUrl, subjectPermissionScreen);
      clickLimitMenuItem(page, "limitMenuButton_" + fixture.subjectLimitAssignId, "editLimit", "UiV2SubjectPermission");
      assertScreenShowsPayload(page, "#simplePermissionLimitEditForm", "subject limit edit");
      openScreen(page, baseUrl, subjectPermissionScreen);
      clickLimitMenuItem(page, "limitAssignValueButton_" + fixture.subjectLimitAssignId + "_", "editLimitValue", "UiV2SubjectPermission");
      assertScreenShowsPayload(page, "#simplePermissionLimitValueEditForm", "subject limit value edit");

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteFixture();
    }
  }

}
