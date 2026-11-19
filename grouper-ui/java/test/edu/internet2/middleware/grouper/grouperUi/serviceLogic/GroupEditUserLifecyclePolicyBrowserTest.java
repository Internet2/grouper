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

import java.sql.Timestamp;
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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleService;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory M3 (userLifecycleRestrictedPolicyRemoval), browser check of the UI half of the
 * fix: groupEdit.jsp shows a policy assigned to the group that the logged in group ADMIN cannot
 * use as a checked read-only choice, and UiV2Group.groupEditSubmit validates the policy before it
 * saves anything.  UserLifecyclePolicyAuthorizationTest covers only UserLifecycleService; this test
 * checks what it cannot: the patched JSP compiles and renders the read-only choice, a save that
 * only changes the description keeps the policy, a tampered radio value for a restricted policy
 * shows the validation message and saves nothing, and None removes the policy with an audit entry
 * naming the admin.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch30 (policyUsers with test.subject.1, protectedGroup with test.subject.0 as
 * ADMIN) and two restricted policies secPatch30restricted / secPatch30restricted2 in the database
 * config, all deleted at the end (the audit entries stay).</p>
 *
 * <p>Login is the UI's form login as test.subject.0/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class GroupEditUserLifecyclePolicyBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch30";

  /** group whose members may use the restricted policies */
  private static final String POLICY_USERS_NAME = STEM_NAME + ":policyUsers";

  /** group test.subject.0 administers */
  private static final String PROTECTED_NAME = STEM_NAME + ":protectedGroup";

  /** restricted policy assigned to the protected group */
  private static final String RESTRICTED = "secPatch30restricted";

  /** restricted policy name */
  private static final String RESTRICTED_NAME = "secPatch30 restricted policy";

  /** second restricted policy, never assigned */
  private static final String RESTRICTED2 = "secPatch30restricted2";

  /** policy config keys this test stores, so cleanup deletes exactly those */
  private static final String[] POLICY_KEY_SUFFIXES = {"name", "description", "isPublic", "groupIdOrName"};

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GroupEditUserLifecyclePolicyBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GroupEditUserLifecyclePolicyBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public GroupEditUserLifecyclePolicyBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * store a restricted (not public) policy usable by members of policyUsers
   * @param configId
   * @param name
   */
  private static void configPolicy(String configId, String name) {
    String[] values = {name, name, "false", POLICY_USERS_NAME};
    for (int i = 0; i < POLICY_KEY_SUFFIXES.length; i++) {
      new GrouperDbConfig().configFileName("grouper.properties")
          .propertyName("grouperUserLifecyclePolicy." + configId + "." + POLICY_KEY_SUFFIXES[i]).value(values[i]).store();
    }
  }

  /**
   * groups, policies, and the restricted policy assigned to the protected group as root
   * @return the protected group id
   */
  private static String createData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group policyUsers = new GroupSave(rootSession).assignName(POLICY_USERS_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      policyUsers.addMember(SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true), false);
      Group protectedGroup = new GroupSave(rootSession).assignName(PROTECTED_NAME)
          .assignDescription("secPatch30 start").assignCreateParentStemsIfNotExist(true).save();
      protectedGroup.grantPriv(SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true), AccessPrivilege.ADMIN, false);
      configPolicy(RESTRICTED, RESTRICTED_NAME);
      configPolicy(RESTRICTED2, "secPatch30 restricted policy 2");
      UserLifecycleService.savePolicyConfigOnGroup(protectedGroup, RESTRICTED, SubjectFinder.findRootSubject());
      assertEquals(RESTRICTED, UserLifecycleService.retrieveExistingPolicyConfigId(protectedGroup));
      return protectedGroup.getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the policy config keys, the groups and the folder
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String configId : new String[] {RESTRICTED, RESTRICTED2}) {
        for (String suffix : POLICY_KEY_SUFFIXES) {
          try {
            new GrouperDbConfig().configFileName("grouper.properties")
                .propertyName("grouperUserLifecyclePolicy." + configId + "." + suffix).delete();
          } catch (RuntimeException re) {
            // already gone; keep cleaning up the rest
          }
        }
      }
      for (String groupName : new String[] {PROTECTED_NAME, POLICY_USERS_NAME}) {
        Group group = GroupFinder.findByName(rootSession, groupName, false);
        if (group != null) {
          group.delete();
        }
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
   * protected group as stored now (fresh from the database)
   * @return the group
   */
  private static Group protectedGroup() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return GroupFinder.findByName(rootSession, PROTECTED_NAME, true);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * policy on the protected group now
   * @return the config id or null
   */
  private static String assignedPolicy() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return UserLifecycleService.retrieveExistingPolicyConfigId(protectedGroup());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * wait until the UI has saved the description (the save is an ajax call followed by a link)
   * @param description
   * @throws InterruptedException
   */
  private static void waitForDescription(String description) throws InterruptedException {
    for (int i = 0; i < 30; i++) {
      if (description.equals(protectedGroup().getDescription())) {
        return;
      }
      Thread.sleep(500);
    }
    fail("description was not saved: " + description + ", is: " + protectedGroup().getDescription());
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
   * @param user
   */
  private static void login(Page page, String baseUrl, String user) {
    com.microsoft.playwright.Response loginResponse = page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    if (loginResponse != null && loginResponse.status() == 401) {
      // this UI uses HTTP basic authentication (grouper.is.ui.basicAuthn), not the form login
      page.setExtraHTTPHeaders(java.util.Collections.singletonMap("Authorization", "Basic "
          + java.util.Base64.getEncoder().encodeToString((user + ":pass").getBytes(java.nio.charset.StandardCharsets.UTF_8))));
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    }
    if (page.locator("input[type=password]").count() > 0) {
      page.locator("input[type=text]").first().fill(user);
      page.locator("input[type=password]").first().fill("pass");
      page.locator("input[type=password]").first().press("Enter");
      // logged in when the login form is gone (waitForURL races a fast login on Playwright 1.46, whose
      // waitForURL always waits for a new navigation); 60 s for the first login after a Tomcat start
      page.locator("input[type=password]").first().waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
          .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED).setTimeout(60000));
    }
  }

  /**
   * open the edit group screen
   * @param page
   * @param baseUrl
   * @param groupId
   */
  private static void openEdit(Page page, String baseUrl, String groupId) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Group.groupEdit&groupId=" + groupId);
    page.locator("#editGroupSaveButton").waitFor();
  }

  // ---------------------------------------------------------------- test

  /**
   * the block with the policy radios: div.controls.user-lifecycle-policy-options from 6.5.0 / 7.4.0,
   * a plain div.controls before that (7.2.1, 6.x), so find it by the None radio it always holds
   */
  private static final String POLICY_OPTIONS = "div.controls:has(#ulp_none)";

  /**
   * read-only assigned policy, description-only save keeps it, tampered restricted policy is
   * refused before anything is saved, None removes it with an audit entry naming the admin
   * @throws Exception
   */
  public void testAssignedRestrictedPolicyReadOnly() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // a previous aborted run may have left data behind
    deleteData();
    Timestamp start = new Timestamp(System.currentTimeMillis() - 1000);
    String groupId = createData();
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newContext().newPage();
      // fail in seconds, not the 30 second Playwright default, when a screen does not render
      page.setDefaultTimeout(15000);

      String baseUrl = uiBaseUrl();
      login(page, baseUrl, "test.subject.0");

      // 1. the assigned policy shows checked and read-only; the running UI reads database config
      // through a cache, so retry until it sees the new policies.  The group's policy assignment is
      // read live, so the read-only radio can show before the cached config has the policy keys
      // (the name then falls back to the config id): wait for the name too
      Locator assigned = page.locator("#ulp_assigned");
      for (int i = 0; i < 10; i++) {
        openEdit(page, baseUrl, groupId);
        if (assigned.count() > 0
            && GrouperUtil.defaultString(page.locator(POLICY_OPTIONS).textContent()).contains(RESTRICTED_NAME)) {
          break;
        }
        Thread.sleep(5000);
      }
      assertEquals("read-only assigned policy radio", 1, assigned.count());
      assertEquals(RESTRICTED, assigned.getAttribute("value"));
      assertTrue("assigned policy is checked", assigned.isChecked());
      assertFalse("None is not checked", page.locator("#ulp_none").isChecked());
      String policyText = page.locator(POLICY_OPTIONS).textContent();
      assertTrue("policy name shown: " + policyText, policyText.contains(RESTRICTED_NAME));
      assertTrue("help text shown: " + policyText, policyText.contains("not allowed to assign it"));
      assertEquals("the other restricted policy is not offered", 0,
          page.locator("input[name=userLifecyclePolicy][value=" + RESTRICTED2 + "]").count());

      // 2. change only the description: the policy stays
      page.locator("#groupDescription").fill("secPatch30 desc 1");
      page.locator("#editGroupSaveButton").click();
      waitForDescription("secPatch30 desc 1");
      assertEquals("description-only save keeps the policy", RESTRICTED, assignedPolicy());

      // 3. tamper: the None radio submits the other restricted policy; refused, nothing saved
      openEdit(page, baseUrl, groupId);
      page.evaluate("() => { var r = document.getElementById('ulp_none'); r.value = '" + RESTRICTED2 + "'; r.checked = true; }");
      page.locator("#groupDescription").fill("secPatch30 desc 2");
      page.locator("#editGroupSaveButton").click();
      page.getByText("not allowed to assign that user lifecycle policy").first().waitFor();
      // give a (wrong) save time to land before checking nothing changed
      Thread.sleep(2000);
      assertEquals("refused save keeps the description", "secPatch30 desc 1", protectedGroup().getDescription());
      assertEquals("refused save keeps the policy", RESTRICTED, assignedPolicy());

      // 4. None removes the policy, audited with test.subject.0
      openEdit(page, baseUrl, groupId);
      page.locator("#ulp_none").check();
      page.locator("#groupDescription").fill("secPatch30 desc 3");
      page.locator("#editGroupSaveButton").click();
      waitForDescription("secPatch30 desc 3");
      assertTrue("None removes the policy", GrouperUtil.isBlank(assignedPolicy()));

      Member subject0Member = GrouperDAOFactory.getFactory().getMember().findBySubject("test.subject.0", "jdbc", true);
      List<String> loggedInMemberIds = HibernateSession.bySqlStatic().listSelect(String.class,
          "select logged_in_member_id from grouper_audit_entry where description = ? and created_on >= ?",
          GrouperUtil.toListObject("Removed user lifecycle policy '" + RESTRICTED + "'", start.getTime()));
      assertTrue("removal audit entry naming test.subject.0: " + loggedInMemberIds,
          loggedInMemberIds.contains(subject0Member.getId()));

      browser.close();
    } finally {
      deleteData();
    }
  }

}
