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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (provisioningSyncCacheStoredXss), browser check: the provisioning screens
 * print grouper_sync* values (attribute value caches, names, membership ids, error messages, log
 * descriptions), and the eleven patched JSPs must escape them.  ProvisioningSyncJspEscapingTest only
 * checks the JSP source; this test checks what it cannot: that the patched JSPs compile in the
 * running UI and that a payload stored in the sync tables shows as literal text, with no script
 * run and no injected element, on each screen.
 *
 * <p>The sync rows are written directly with the GcGrouperSync API (the same rows a full sync
 * writes; the md "Reproduction" got the payload there through a group display name and a real
 * sync), so no provisioning job runs.  Screens: group and subject target details, group and
 * subject membership target details, provisioner Activity (blank, group, entity, membership), View
 * errors, job details and config details.  provisioningMembershipDetailsHelper.jsp is only
 * included by provisioningMembershipDetails.jsp, which no handler renders, so it is not reachable.</p>
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * provisioner config secPatch88 in the database config, its grouper_sync rows, and folder
 * test:secPatch88 with one group (marked provisionable to secPatch88), all deleted at the end.
 * Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class ProvisioningSyncBrowserTest extends TestCase {

  /** provisioner config id (and sync provisioner name) this test creates and deletes */
  private static final String CONFIG_ID = "secPatch88";

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch88";

  /** provisionable group */
  private static final String GROUP_NAME = STEM_NAME + ":group";

  /** subject with a sync member and membership row */
  private static final String SUBJECT_ID = "test.subject.0";

  /** element-breaking payload; if it is not escaped it adds an img with this id */
  private static final String PAYLOAD = "\"><img id=secPatch88xss src=x onerror=alert(88)>";

  /** part of the payload that is in the page text only if it was escaped */
  private static final String PAYLOAD_TEXT = "<img id=secPatch88xss";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ProvisioningSyncBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public ProvisioningSyncBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public ProvisioningSyncBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup and cleanup

  /** keys this test stores, so cleanup deletes exactly those (never run; the table does not exist) */
  private static final String[][] KEYS = {
      {"class", "edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner"},
      {"dbExternalSystemConfigId", "grouper"},
      {"operateOnGrouperGroups", "true"},
      {"groupTableName", "secpatch88_groups"},
      {"groupTableIdColumn", "id"},
      {"hasTargetGroupLink", "false"},
      {"numberOfGroupAttributes", "1"},
      {"targetGroupAttribute.0.name", "id"},
      {"targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField"},
      {"targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "id"},
      {"groupMatchingAttributeCount", "1"},
      {"groupMatchingAttribute0name", "id"},
      {"addDisabledFullSyncDaemon", "false"},
      {"addDisabledIncrementalSyncDaemon", "false"},
  };

  /** ids the browser needs, filled by setUpData */
  private String groupId;
  /** */
  private String memberId;
  /** */
  private String syncMemberId;
  /** */
  private String syncMembershipId;
  /** */
  private String syncJobId;

  /**
   * provisioner config, group marked provisionable, and sync rows holding the payload
   */
  private void setUpData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String[] key : KEYS) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
            .propertyName("provisioner." + CONFIG_ID + "." + key[0]).value(key[1]).store();
      }
      // the UI caches the provisioner list for 5 minutes and only its own save clears it; this
      // clears it here and notifies the running UI (cache database), which drops it in seconds
      edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.clearTargetsCache();
      // pass the session explicitly: the no-arg constructors depend on the thread's static
      // session, which older releases (6.x and earlier) assert on when it is missing
      new StemSave(rootSession).assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      this.groupId = group.getId();
      Subject subject = SubjectFinder.findByIdAndSource(SUBJECT_ID, "jdbc", true);
      group.addMember(subject, false);
      Member member = MemberFinder.findBySubject(rootSession, subject, true);
      this.memberId = member.getId();

      // the group target details screens need the group marked provisionable to the target
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision(CONFIG_ID);
      attributeValue.setTargetName(CONFIG_ID);
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, group);

      Timestamp now = new Timestamp(System.currentTimeMillis());
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, CONFIG_ID);

      GcGrouperSyncGroup syncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId(this.groupId);
      syncGroup.setGroupName(GROUP_NAME + PAYLOAD);
      syncGroup.setGroupAttributeValueCache0(PAYLOAD);
      syncGroup.setGroupAttributeValueCache1(PAYLOAD);
      syncGroup.setGroupAttributeValueCache2(PAYLOAD);
      syncGroup.setGroupAttributeValueCache3(PAYLOAD);
      syncGroup.setProvisionable(true);
      syncGroup.setInTarget(true);
      syncGroup.setInTargetStart(now);
      syncGroup.setErrorCode(GcGrouperSyncErrorCode.ERR);
      syncGroup.setErrorMessage(PAYLOAD);
      syncGroup.setErrorTimestamp(now);

      GcGrouperSyncMember syncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId(this.memberId);
      syncMember.setSubjectId(SUBJECT_ID);
      syncMember.setSourceId("jdbc");
      syncMember.setSubjectIdentifier(PAYLOAD);
      syncMember.setEntityAttributeValueCache0(PAYLOAD);
      syncMember.setEntityAttributeValueCache1(PAYLOAD);
      syncMember.setEntityAttributeValueCache2(PAYLOAD);
      syncMember.setEntityAttributeValueCache3(PAYLOAD);
      syncMember.setProvisionable(true);
      syncMember.setInTarget(true);
      syncMember.setInTargetStart(now);

      gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();

      GcGrouperSyncMembership syncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao()
          .membershipRetrieveOrCreateBySyncGroupIdAndSyncMemberId(syncGroup.getId(), syncMember.getId());
      syncMembership.setMembershipId(PAYLOAD);
      syncMembership.setMembershipId2(PAYLOAD);
      syncMembership.setInTarget(true);
      syncMembership.setInTargetStart(now);

      GcGrouperSyncJob syncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("fullProvisionFull");
      syncJob.setErrorMessage(PAYLOAD);
      syncJob.setErrorTimestamp(now);
      syncJob.setLastUpdated(now);

      gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();

      GcGrouperSyncLog syncLog = gcGrouperSync.getGcGrouperSyncJobDao().jobCreateLog(syncJob);
      syncLog.setDescriptionToSave(PAYLOAD);
      syncLog.setServer(PAYLOAD);
      syncLog.setStatus(GcGrouperSyncLogState.ERROR);
      syncLog.setSyncTimestamp(now);
      gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();

      this.syncMemberId = syncMember.getId();
      this.syncMembershipId = syncMembership.getId();
      this.syncJobId = syncJob.getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the sync rows, group, folder and config keys this test created
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      try {
        GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, CONFIG_ID);
        if (gcGrouperSync != null) {
          gcGrouperSync.getGcGrouperSyncMembershipDao().membershipDeleteAll(true);
          gcGrouperSync.getGcGrouperSyncGroupDao().groupDeleteAll(true, true);
          gcGrouperSync.getGcGrouperSyncMemberDao().memberDeleteAll(true, true);
          gcGrouperSync.getGcGrouperSyncJobDao().jobDeleteAll(true);
          gcGrouperSync.getGcGrouperSyncLogDao().logDeleteAll();
          gcGrouperSync.getGcGrouperSyncDao().delete();
        }
      } catch (RuntimeException re) {
        // keep cleaning up the rest
        re.printStackTrace();
      }
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, false);
      if (group != null) {
        group.delete();
      }
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.delete();
      }
      for (String[] key : KEYS) {
        try {
          new GrouperDbConfig().configFileName("grouper-loader.properties")
              .propertyName("provisioner." + CONFIG_ID + "." + key[0]).delete();
        } catch (RuntimeException re) {
          // already gone; keep cleaning up the rest
        }
      }
      // the UI caches the provisioner list for 5 minutes and only its own save clears it; this
      // clears it here and notifies the running UI (cache database), which drops it in seconds
      edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.clearTargetsCache();
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
      + " return !!document.getElementById('secPatch88xss') || (m != null && m.innerText.indexOf(t) >= 0); }";

  /**
   * wait until the main content has rendered the payload (escaped or not), then assert it is text
   * and not an element
   * @param page
   * @param screen label for messages
   */
  private static void assertPayloadIsText(Page page, String screen) {
    try {
      page.waitForFunction(PAYLOAD_RENDERED_JS, PAYLOAD_TEXT);
    } catch (RuntimeException re) {
      String text = page.locator("#grouperMainContentDivId").count() > 0
          ? page.locator("#grouperMainContentDivId").innerText() : page.content();
      throw new RuntimeException(screen + ": payload never rendered; page text: "
          + text.substring(0, Math.min(2000, text.length())), re);
    }
    assertEquals(screen + ": payload must not add an element", 0, page.locator("#secPatch88xss").count());
    assertTrue(screen + ": payload must show as literal text",
        page.locator("#grouperMainContentDivId").innerText().contains(PAYLOAD_TEXT));
  }

  /**
   * open a screen by deep link (an allowed screen operation) and check the payload
   * @param page
   * @param baseUrl
   * @param operationAndParams e.g. UiV2Provisioning.viewProvisioningTargetDetailsOnGroup&groupId=...
   * @param screen
   */
  private static void checkScreen(Page page, String baseUrl, String operationAndParams, String screen) {
    try {
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=" + operationAndParams);
      assertPayloadIsText(page, screen);
    } catch (RuntimeException re) {
      // one retry: a busy shared Tomcat (other tests recompiling JSPs) once left a screen blank
      // for 15 seconds.  Assertion failures are Errors, not RuntimeExceptions, so they are not retried
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=" + operationAndParams);
      assertPayloadIsText(page, screen);
    }
  }

  /**
   * on the Activity screen pick an object type and press Submit, then check the payload
   * @param page
   * @param baseUrl
   * @param objectType "" (all), group, entity, membership
   */
  private static void checkActivity(Page page, String baseUrl, String objectType) {
    // submit buttons are found by [type=submit] without a tag (an <input> in every release today)
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation="
        + "UiV2ProvisionerConfiguration.viewProvisionerActivity&provisionerConfigId=" + CONFIG_ID);
    page.locator("#provisionerConfigObjectTypeId").waitFor();
    page.selectOption("#provisionerConfigObjectTypeId", objectType);
    // mark the form so we can tell when the submit has replaced the screen
    page.evaluate("() => document.getElementById('provisionerConfigObjectTypeId').setAttribute('data-secpatch-old', 'true')");
    page.locator("#provisionerConfigViewActivity [type=submit]").click();
    page.locator("#provisionerConfigObjectTypeId:not([data-secpatch-old])").waitFor();
    assertPayloadIsText(page, "activity type '" + objectType + "'");
  }

  // ---------------------------------------------------------------- test

  /**
   * a payload stored in the grouper_sync tables shows as literal text on every provisioning
   * screen that prints sync values; no dialog opens and no element from the payload lands
   * @throws Exception
   */
  public void testSyncValuesRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

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

        String groupTargetDetails = "UiV2Provisioning.viewProvisioningTargetDetailsOnGroup&groupId="
            + this.groupId + "&provisioningTargetName=" + CONFIG_ID;

        // the running UI reads database config through a cache; retry until it sees secPatch88
        RuntimeException last = null;
        boolean loaded = false;
        for (int i = 0; i < 8 && !loaded; i++) {
          try {
            checkScreen(page, baseUrl, groupTargetDetails, "group target details");
            loaded = true;
          } catch (RuntimeException re) {
            last = re;
            Thread.sleep(5000);
          }
        }
        if (!loaded) {
          throw new RuntimeException("group target details never loaded for " + CONFIG_ID, last);
        }

        checkScreen(page, baseUrl, "UiV2Provisioning.viewProvisioningTargetDetailsOnSubject&memberId="
            + this.memberId + "&provisioningTargetName=" + CONFIG_ID + "&groupSyncMemberId=" + this.syncMemberId,
            "subject target details");
        checkScreen(page, baseUrl, "UiV2Provisioning.viewProvisioningTargetDetailsOnGroupMembership&groupId="
            + this.groupId + "&memberId=" + this.memberId + "&provisioningTargetName=" + CONFIG_ID
            + "&groupSyncMembershipId=" + this.syncMembershipId, "group membership target details");
        checkScreen(page, baseUrl, "UiV2Provisioning.viewProvisioningTargetDetailsOnSubjectMembership&groupId="
            + this.groupId + "&memberId=" + this.memberId + "&provisioningTargetName=" + CONFIG_ID
            + "&groupSyncMembershipId=" + this.syncMembershipId, "subject membership target details");

        checkActivity(page, baseUrl, "");
        checkActivity(page, baseUrl, "group");
        checkActivity(page, baseUrl, "entity");
        checkActivity(page, baseUrl, "membership");

        // View errors: the stored error table (sync group error) after a plain Submit
        page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation="
            + "UiV2ProvisionerConfiguration.viewProvisionerErrors&provisionerConfigId=" + CONFIG_ID);
        page.locator("#provisionerConfigErrorGroupId").waitFor();
        // mark the form so we can tell when the submit has replaced the screen
        page.evaluate("() => document.getElementById('provisionerConfigErrorGroupId').setAttribute('data-secpatch-old', 'true')");
        page.locator("#provisionerConfigViewErrors [type=submit]").click();
        page.locator("#provisionerConfigErrorGroupId:not([data-secpatch-old])").waitFor();
        assertPayloadIsText(page, "view errors");

        checkScreen(page, baseUrl, "UiV2ProvisionerConfiguration.viewProvisionerJobDetails&provisionerConfigId="
            + CONFIG_ID + "&provisionerJobId=" + this.syncJobId, "job details");
        checkScreen(page, baseUrl, "UiV2ProvisionerConfiguration.viewProvisionerConfigDetails&provisionerConfigId="
            + CONFIG_ID, "config details");

        // give a late onerror a moment to fire before checking
        Thread.sleep(1000);
        assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

        browser.close();
      }
    } finally {
      deleteData();
    }
  }

}
