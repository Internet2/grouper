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
import java.util.HashMap;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState;
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
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Security advisory H1 (workflowConfigStoredXss), browser check: the six workflow JSPs must escape
 * the workflow config name, description, approval state names (also shown as the instance state)
 * and the edit form values.  WorkflowJspEscapingTest only checks the JSP source; this test checks
 * that the patched JSPs compile in the running UI and that a stored payload shows as literal text,
 * with no script run and no injected element, and that the edit form holds the exact stored strings
 * and an unchanged re-save keeps them.
 *
 * <p>Screens (as GrouperSystem, the approver: wheel): config details, the group's workflow list,
 * edit config (and Save), forms I submitted, forms waiting for my approval, the config's
 * instances.  Workflow is off by default (advisory H1), so the test sets workflow.enable=true in the
 * database config (and in this JVM) and restores the prior value at the end.</p>
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch100 with one group that holds workflow config secPatch100 and one instance
 * (deleted with the group), and the instance's form snapshot row in grouper_file.  Note: the UI
 * caches each subject's "forms I submitted" and "waiting for approval" lists for 5
 * minutes, so a rerun within 5 minutes waits on those two screens until the cache expires.
 * Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class WorkflowJspEscapingBrowserTest extends TestCase {

  /** workflow config id (must match ^[a-zA-Z0-9_-]*$) */
  private static final String WORKFLOW_ID = "secPatch100";

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch100";

  /** group that owns the workflow */
  private static final String GROUP_NAME = STEM_NAME + ":group";

  /** element-breaking payload; if it is not escaped it adds an img with this id */
  private static final String PAYLOAD = "\"><img id=secPatch100xss src=x onerror=alert(100)>";

  /** part of the payload that is in the page text only if it was escaped */
  private static final String PAYLOAD_TEXT = "<img id=secPatch100xss";

  /** stored description; shown in a textarea on the edit form, where only a closing tag breaks out */
  private static final String DESCRIPTION = "</textarea>" + PAYLOAD;

  /** stored config name */
  private static final String CONFIG_NAME = "Workflow " + PAYLOAD;

  /** approval state name (replaces the default groupManager state); the instance sits in it */
  private static final String STATE_NAME = "state" + PAYLOAD;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WorkflowJspEscapingBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public WorkflowJspEscapingBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public WorkflowJspEscapingBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- workflow.enable

  /** database value of workflow.enable before the test, null if there was no row */
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

  /** approvals json as stored, filled by setUpData */
  private String approvalsJson;

  /**
   * group, workflow config with the payload in name, description and one state name, and one
   * instance in that state initiated by GrouperSystem
   */
  private void setUpData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new StemSave().assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
      Group group = new GroupSave().assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      this.groupId = group.getId();

      // default approval states with the groupManager state renamed to the payload
      GrouperWorkflowApprovalStates states = GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(
          GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(this.groupId));
      for (GrouperWorkflowApprovalState state : states.getStates()) {
        if ("groupManager".equals(state.getStateName())) {
          state.setStateName(STATE_NAME);
        }
      }
      try {
        this.approvalsJson = workflowObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(states);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      GrouperWorkflowConfig config = new GrouperWorkflowConfig();
      config.setWorkflowConfigApprovalsString(this.approvalsJson);
      config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(this.approvalsJson));
      config.setOwnerGroup(group);
      config.setWorkflowConfigDescription(DESCRIPTION);
      config.setWorkflowConfigEnabled("true");
      // keep the default form (new GrouperWorkflowConfig() sets it)
      config.setWorkflowConfigId(WORKFLOW_ID);
      config.setWorkflowConfigName(CONFIG_NAME);
      // default params; the one editable in groupManager is editable in the renamed state instead
      // (the validator rejects a param editable in a state that does not exist)
      GrouperWorkflowConfigParams params = GrouperWorkflowConfigParams.buildParamsFromJsonString(
          GrouperWorkflowConfigParams.getDefaultConfigParamsString());
      for (GrouperWorkflowConfigParam param : params.getParams()) {
        List<String> editableInStates = new ArrayList<String>();
        for (String state : param.getEditableInStates()) {
          editableInStates.add("groupManager".equals(state) ? STATE_NAME : state);
        }
        param.setEditableInStates(editableInStates);
      }
      String paramsString;
      try {
        paramsString = workflowObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(params);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      config.setWorkflowConfigParamsString(paramsString);
      config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(paramsString));
      config.setWorkflowConfigSendEmail(false);
      config.setWorkflowConfigType("grouper");
      config.setWorkflowConfigViewersGroupId(this.groupId);
      GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(config, group);

      // initiate as GrouperSystem, then move the instance to the payload state
      GrouperWorkflowConfig savedConfig = GrouperWorkflowConfigService.getWorkflowConfig(group, WORKFLOW_ID);
      GrouperWorkflowInstanceService.saveInitiateStateInstance(savedConfig,
          SubjectFinder.findRootSubject(), new HashMap<GrouperWorkflowConfigParam, String>(), group);
      for (GrouperWorkflowInstance instance : GrouperWorkflowInstanceService.getWorkflowInstances(group)) {
        instance.setWorkflowInstanceState(STATE_NAME);
        GrouperWorkflowInstanceService.saveOrUpdateWorkflowInstance(instance, group);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @return the stored config (root session)
   */
  private static GrouperWorkflowConfig storedConfig() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, true);
      return GrouperWorkflowConfigService.getWorkflowConfig(group, WORKFLOW_ID);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the group (its workflow config and instance attributes go with it) and folder
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

  /** script: the main content shows the payload (as text) or the payload's element exists */
  private static final String PAYLOAD_RENDERED_JS = "t => { var m = document.getElementById('grouperMainContentDivId');"
      + " return !!document.getElementById('secPatch100xss') || (m != null && m.innerText.indexOf(t) >= 0); }";

  /**
   * @param page
   * @return start of the main content text, for failure messages
   */
  private static String mainText(Page page) {
    String text = page.locator("#grouperMainContentDivId").count() == 0 ? "" : page.locator("#grouperMainContentDivId").innerText();
    return text.substring(0, Math.min(1500, text.length()));
  }

  /**
   * open a screen by deep link (an allowed screen operation), wait until the payload rendered
   * (escaped or not), then assert it is text and not an element
   * @param page
   * @param baseUrl
   * @param operationAndParams
   * @param screen label for messages
   */
  private static void checkScreen(Page page, String baseUrl, String operationAndParams, String screen) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=" + operationAndParams);
    try {
      page.waitForFunction(PAYLOAD_RENDERED_JS, PAYLOAD_TEXT);
    } catch (RuntimeException re) {
      throw new RuntimeException(screen + ": payload never rendered; main content: " + mainText(page), re);
    }
    assertEquals(screen + ": payload must not add an element", 0, page.locator("#secPatch100xss").count());
    assertTrue(screen + ": payload must show as literal text",
        page.locator("#grouperMainContentDivId").innerText().contains(PAYLOAD_TEXT));
  }

  /**
   * the "forms I submitted" and "waiting for my approval" lists are cached per subject for 5 minutes
   * (GrouperWorkflowInstanceService), so a rerun soon after another run can show the old list: retry
   * until the cache expires
   * @param page
   * @param baseUrl
   * @param operationAndParams
   * @param screen
   * @throws InterruptedException
   */
  private static void checkCachedListScreen(Page page, String baseUrl, String operationAndParams, String screen)
      throws InterruptedException {
    long giveUp = System.currentTimeMillis() + 330 * 1000L;
    while (true) {
      try {
        checkScreen(page, baseUrl, operationAndParams, screen);
        return;
      } catch (RuntimeException re) {
        if (System.currentTimeMillis() > giveUp) {
          throw re;
        }
        System.out.println(screen + ": not shown yet (5 minute per-subject cache?), retrying");
        Thread.sleep(15000);
      }
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * the stored payload shows as literal text on the six workflow screens; the edit form holds the
   * exact strings and an unchanged Save keeps them; no dialog opens
   * @throws Exception
   */
  public void testWorkflowValuesRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    enableWorkflow();
    try {
      setUpData();
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

        String groupParams = "&workflowConfigId=" + WORKFLOW_ID + "&groupId=" + this.groupId;

        // the running UI reads database config through a cache; retry until it sees workflow.enable
        RuntimeException last = null;
        boolean loaded = false;
        for (int i = 0; i < 10 && !loaded; i++) {
          try {
            checkScreen(page, baseUrl, "UiV2GrouperWorkflow.viewWorkflowConfigDetails" + groupParams, "config details");
            loaded = true;
          } catch (RuntimeException re) {
            last = re;
            Thread.sleep(5000);
          }
        }
        if (!loaded) {
          throw new RuntimeException("workflow config details never loaded (workflow.enable not seen?)", last);
        }

        checkScreen(page, baseUrl, "UiV2GrouperWorkflow.viewForms&groupId=" + this.groupId, "group workflow list");
        checkScreen(page, baseUrl, "UiV2GrouperWorkflow.viewInstances" + groupParams, "instances");

        // edit form: exact stored strings, no breakout
        page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2GrouperWorkflow.editWorkflowConfig" + groupParams);
        page.locator("#grouperWorkflowConfigNameId").waitFor();
        assertEquals("edit name box", CONFIG_NAME, page.locator("#grouperWorkflowConfigNameId").inputValue());
        assertEquals("edit description box", DESCRIPTION, page.locator("#grouperWorkflowConfigDescriptionId").inputValue());
        assertEquals("edit approvals box", storedConfig().getWorkflowConfigApprovalsString(),
            page.locator("#grouperWorkflowConfigApprovalsId").inputValue());
        assertEquals("edit form: payload must not add an element", 0, page.locator("#secPatch100xss").count());

        // unchanged Save keeps the values
        GrouperWorkflowConfig before = storedConfig();
        // mark the name box so we can tell when the save has replaced the screen
        page.evaluate("() => document.getElementById('grouperWorkflowConfigNameId').setAttribute('data-secpatch-old', 'true')");
        page.locator("[onclick*='workflowConfigEditSubmit']").first().click();
        // success goes to the group's workflow list (guiV2link to viewForms); an error re-renders the form
        page.waitForFunction("() => { var e = document.getElementById('grouperWorkflowConfigNameId');"
            + " return e == null || !e.hasAttribute('data-secpatch-old'); }");
        String saveMessage = page.locator("#messaging").count() == 0 ? "" : page.locator("#messaging").innerText().trim();
        try {
          // poll the current url: waitForURL on Playwright 1.46 (grouper_v4) only waits for a new
          // navigation and misses one that already happened
          page.waitForFunction("() => location.href.indexOf('operation=UiV2GrouperWorkflow.viewForms') >= 0");
          page.locator("#grouperMainContentDivId").getByText(PAYLOAD_TEXT).first().waitFor();
        } catch (RuntimeException re) {
          throw new RuntimeException("unchanged re-save was not accepted; message: " + saveMessage
              + "; main content: " + mainText(page), re);
        }
        GrouperWorkflowConfig after = storedConfig();
        assertEquals("re-save keeps the name; message: " + saveMessage, before.getWorkflowConfigName(), after.getWorkflowConfigName());
        assertEquals("re-save keeps the description", before.getWorkflowConfigDescription(), after.getWorkflowConfigDescription());
        assertEquals("re-save keeps the approvals", before.getWorkflowConfigApprovalsString(), after.getWorkflowConfigApprovalsString());
        assertEquals("re-save keeps the params", before.getWorkflowConfigParamsString(), after.getWorkflowConfigParamsString());
        assertEquals("after save: payload must not add an element", 0, page.locator("#secPatch100xss").count());

        // last: these two lists are cached per subject for 5 minutes, so a rerun may wait here
        checkCachedListScreen(page, baseUrl, "UiV2GrouperWorkflow.formsUserSubmitted", "forms I submitted");
        checkCachedListScreen(page, baseUrl, "UiV2GrouperWorkflow.formsWaitingForApproval", "waiting for approval");

        // give a late onerror a moment to fire before checking
        Thread.sleep(1000);
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
