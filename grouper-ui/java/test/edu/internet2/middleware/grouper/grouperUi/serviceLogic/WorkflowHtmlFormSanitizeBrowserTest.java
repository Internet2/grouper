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
import java.util.Iterator;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalStates;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigParam;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigParams;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigService;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstance;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceService;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Security advisory H1 (workflowRawHtmlStoredXssSysadminTakeover), browser check of the render-time
 * sanitize: WorkflowContainer.getHtmlForm() must strip active content from a workflow form (and
 * from a stored instance snapshot) before the JSPs print it, and the cleaned form must still work.
 * GrouperWorkflowHtmlSanitizerTest covers the sanitizer; the render path needs a UI request.
 *
 * <p>A workflow config (secPatch57) is stored directly through the API (as one saved before the
 * fix would be; the API save does not validate) with a form holding an img whose onerror sets the
 * page title, plus a notes textarea.  test.subject.0 opens the initiate form (showJoinGroupForm):
 * no img, title unchanged, notes box present; types notes and submits (the form still works).
 * GrouperSystem then opens that instance (viewInstance, from the stored snapshot, which was built
 * from the raw form): no img, title unchanged, the notes value shows.  The save-time check (an
 * editor saving such a form gets a validation error) is covered by the JUnit test.</p>
 *
 * <p>Workflow is off by default (advisory H1), so the test sets workflow.enable=true in the database
 * config (and in this JVM) and restores the prior value at the end.  Runs against the running local
 * UI (Tomcat), so it is skipped unless grouper.properties has junit.test.tomcat=true.  It extends
 * TestCase, not GrouperTest: GrouperTest resets the database, and the running UI uses the
 * same database.  Setup and cleanup are this test's own: folder test:secPatch57 with one group
 * (its workflow config and instance attributes go with it) and the instance's form snapshot row in
 * grouper_file.  Logins are the UI's form login (GrouperSystem/pass, test.subject.0/pass, local
 * dev).  Uses the raw Playwright API (not GrouperPage) so the same file works on releases without
 * the app.browser helpers.</p>
 */
public class WorkflowHtmlFormSanitizeBrowserTest extends TestCase {

  /** workflow config id (must match ^[a-zA-Z0-9_-]*$) */
  private static final String WORKFLOW_ID = "secPatch57";

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch57";

  /** group that owns the workflow */
  private static final String GROUP_NAME = STEM_NAME + ":group";

  /** what the img onerror would set the title to */
  private static final String XSS_TITLE = "WF_RAW_XSS";

  /** the reporter's form: an img with an onerror handler plus the notes textarea */
  private static final String BAD_FORM = "<p id=\"secPatch57intro\">secPatch57 form</p>"
      + "<img id=\"secPatch57img\" src=\"x\" onerror=\"document.title='" + XSS_TITLE + "'\">"
      + "<textarea name=\"notes\" id=\"secPatch57notes\"></textarea>";

  /** what test.subject.0 types in the notes box */
  private static final String NOTES = "secPatch57 notes";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WorkflowHtmlFormSanitizeBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public WorkflowHtmlFormSanitizeBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public WorkflowHtmlFormSanitizeBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- workflow.enable

  /** database value of workflow.enable before the test */
  private String priorWorkflowEnable = null;

  /** true if there was a workflow.enable row before the test */
  private boolean hadWorkflowEnableRow = false;

  /**
   * turn workflow on for the running UI (database config) and for this JVM
   */
  private void enableWorkflow() {
    List<String> values = new GcDbAccess().sql("select config_value from grouper_config where "
        + "config_file_name = 'grouper.properties' and config_key = 'workflow.enable'").selectList(String.class);
    this.hadWorkflowEnableRow = values.size() > 0;
    this.priorWorkflowEnable = this.hadWorkflowEnableRow ? values.get(0) : null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("workflow.enable").value("true").store();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");
  }

  /**
   * put workflow.enable back the way it was
   */
  private void restoreWorkflowEnable() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("workflow.enable");
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      if (this.hadWorkflowEnableRow) {
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("workflow.enable")
            .value(this.priorWorkflowEnable).store();
      } else {
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("workflow.enable").delete();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- data

  /** owner group uuid, filled by setUpData */
  private String groupId;

  /**
   * group test.subject.0 can VIEW, holding a workflow config with the bad form and one param (notes)
   */
  private void setUpData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new StemSave().assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
      Group group = new GroupSave().assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      group.grantPriv(SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true), AccessPrivilege.VIEW, false);
      this.groupId = group.getId();

      // default params, only notes (the form must have an element for every param)
      GrouperWorkflowConfigParams params = GrouperWorkflowConfigParams.buildParamsFromJsonString(
          GrouperWorkflowConfigParams.getDefaultConfigParamsString());
      Iterator<GrouperWorkflowConfigParam> iterator = params.getParams().iterator();
      while (iterator.hasNext()) {
        if (!"notes".equals(iterator.next().getParamName())) {
          iterator.remove();
        }
      }
      String paramsString;
      try {
        paramsString = workflowObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(params);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      String approvals = GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(this.groupId);
      GrouperWorkflowConfig config = new GrouperWorkflowConfig();
      config.setWorkflowConfigApprovalsString(approvals);
      config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(approvals));
      config.setOwnerGroup(group);
      config.setWorkflowConfigDescription("secPatch57 description");
      config.setWorkflowConfigEnabled("true");
      config.setWorkflowConfigForm(BAD_FORM);
      config.setWorkflowConfigId(WORKFLOW_ID);
      config.setWorkflowConfigName("secPatch57 workflow");
      config.setWorkflowConfigParamsString(paramsString);
      config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(paramsString));
      config.setWorkflowConfigSendEmail(false);
      config.setWorkflowConfigType("grouper");
      config.setWorkflowConfigViewersGroupId(this.groupId);
      GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(config, group);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @return the attribute assign id of the one instance on the group, or null if none yet
   */
  private static String instanceAttributeAssignId() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, true);
      List<GrouperWorkflowInstance> instances = GrouperWorkflowInstanceService.getWorkflowInstances(group);
      return instances.size() == 0 ? null : instances.get(0).getAttributeAssignId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the instance snapshot file rows, the group and the folder
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      // the instance's form snapshot is a grouper_file row (workflow.storage.option=database), named
      // configId_instanceUuid_state_date; deleting the group does not remove it
      new GcDbAccess().sql("delete from grouper_file where file_path like ?")
          .addBindVar("/grouperWorkflow/" + WORKFLOW_ID + "_%").executeSql();
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
   * @param user GrouperSystem or test.subject.0 (local dev password pass)
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
   * @param page
   * @return start of the main content text, for failure messages
   */
  private static String mainText(Page page) {
    String text = page.locator("#grouperMainContentDivId").count() == 0 ? "" : page.locator("#grouperMainContentDivId").innerText();
    return text.substring(0, Math.min(1500, text.length()));
  }

  /**
   * the form html in the page has no img or event handler, and the onerror did not run
   * @param page
   * @param screen label for messages
   * @throws InterruptedException
   */
  private static void assertFormClean(Page page, String screen) throws InterruptedException {
    // give a late onerror a moment to fire before checking
    Thread.sleep(1000);
    // not the combobox busy image: on 4.x the group header's add-member combobox renders
    // <img class="comboThrobber"> inside the main content (6.x/7.x have no img there)
    assertEquals(screen + ": img from the form must be removed", 0,
        page.locator("#grouperMainContentDivId img:not(.comboThrobber)").count());
    assertEquals(screen + ": no onerror attribute may remain", 0, page.locator("#grouperMainContentDivId [onerror]").count());
    assertFalse(screen + ": the onerror must not run", XSS_TITLE.equals(page.title()));
    assertEquals(screen + ": safe form content still shows", 1, page.locator("#secPatch57intro").count());
  }

  // ---------------------------------------------------------------- test

  /**
   * the initiate form and the instance view print the stored form without the img/onerror, the
   * notes box still works, and the submitted notes show on the instance
   * @throws Exception
   */
  public void testWorkflowFormSanitizedAndStillWorks() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    enableWorkflow();
    try {
      setUpData();
      try (Playwright playwright = Playwright.create()) {
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        String baseUrl = uiBaseUrl();

        // any alert/confirm/prompt means something ran
        final List<String> dialogs = new ArrayList<String>();

        BrowserContext userContext = browser.newContext();
        Page userPage = userContext.newPage();
        // fail in seconds, not the 30 second Playwright default, when a screen does not render
        userPage.setDefaultTimeout(15000);
        userPage.onDialog(dialog -> {
          dialogs.add(dialog.message());
          dialog.dismiss();
        });
        login(userPage, baseUrl, "test.subject.0");

        // initiate form as test.subject.0.  The running UI reads database config through a cache;
        // retry until it sees workflow.enable
        boolean loaded = false;
        RuntimeException last = null;
        for (int i = 0; i < 10 && !loaded; i++) {
          try {
            userPage.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2GrouperWorkflow.showJoinGroupForm&groupId="
                + this.groupId);
            userPage.locator("#groupJoinInitiateWorkflowId").waitFor();
            loaded = true;
          } catch (RuntimeException re) {
            last = re;
            Thread.sleep(5000);
          }
        }
        if (!loaded) {
          throw new RuntimeException("initiate form never loaded; main content: " + mainText(userPage), last);
        }
        assertFormClean(userPage, "initiate form");
        assertEquals("initiate form: notes box still shows", 1, userPage.locator("textarea[name=notes]").count());

        // the cleaned form still submits
        userPage.locator("textarea[name=notes]").fill(NOTES);
        userPage.locator("#groupJoinInitiateWorkflowId #submitId").click();
        String attributeAssignId = null;
        for (int i = 0; i < 20 && attributeAssignId == null; i++) {
          Thread.sleep(500);
          attributeAssignId = instanceAttributeAssignId();
        }
        assertNotNull("submitting the cleaned initiate form must create an instance; main content: "
            + mainText(userPage), attributeAssignId);

        // the approver (GrouperSystem) views the instance, from the stored snapshot of the raw form
        BrowserContext rootContext = browser.newContext();
        Page rootPage = rootContext.newPage();
        rootPage.setDefaultTimeout(15000);
        rootPage.onDialog(dialog -> {
          dialogs.add(dialog.message());
          dialog.dismiss();
        });
        login(rootPage, baseUrl, "GrouperSystem");
        rootPage.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2GrouperWorkflow.viewInstance&attributeAssignId="
            + attributeAssignId);
        try {
          rootPage.locator("#secPatch57intro").waitFor();
        } catch (RuntimeException re) {
          throw new RuntimeException("instance view never showed the form; main content: " + mainText(rootPage), re);
        }
        assertFormClean(rootPage, "instance view");
        assertTrue("instance view: submitted notes show", rootPage.locator("#grouperMainContentDivId").innerHTML().contains(NOTES)
            || rootPage.locator("textarea[name=notes]").first().inputValue().contains(NOTES));

        assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

        browser.close();
      }
    } finally {
      try {
        deleteData();
      } finally {
        restoreWorkflowEnable();
      }
    }
  }

  /**
   * the ObjectMapper the workflow code of this release writes its JSON with: GrouperUtil.objectMapper,
   * or GrouperWorkflowSettings.objectMapper in releases where GrouperUtil has none (e.g. 2.6.19).
   * Looked up by reflection so this one file compiles on every release
   * @return the mapper
   */
  private static com.fasterxml.jackson.databind.ObjectMapper workflowObjectMapper() {
    for (String className : new String[] {"edu.internet2.middleware.grouper.util.GrouperUtil",
        "edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings"}) {
      try {
        return (com.fasterxml.jackson.databind.ObjectMapper)Class.forName(className).getField("objectMapper").get(null);
      } catch (Exception e) {
        // not in this release, try the next
      }
    }
    throw new RuntimeException("no workflow ObjectMapper in this release");
  }

}
