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
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.grouperTypes.GdgTypeGroupSave;
import edu.internet2.middleware.grouper.app.grouperTypes.GdgTypeStemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (objectTypeMetadataStoredXss), browser check: the object type view of a
 * group or folder (objectTypeObjectSettingsView.jsp) prints the data owner, member description
 * and service name, and the patched JSP html-escapes them.  This test stores markup in all three
 * (a policy group, an app group, an org folder), opens the type view in the running UI, and checks
 * each value shows as text with no injected element and no dialog.
 *
 * <p>Not covered here: the service-folder option list of the edit screen
 * (grouperObjectTypeObjectSettingsEditHelper.jsp) needs a service folder whose name holds markup;
 * that change is checked by reading the JSP (see the issue md).</p>
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * the folder test:secPatch126 with two groups; deleted at the end (the type assignments go with
 * them).</p>
 */
public class ObjectTypeSettingsViewBrowserTest extends TestCase {

  /** folder this test creates and deletes, also gets the org type */
  private static final String STEM_NAME = "test:secPatch126";

  /** group with the policy type (data owner and member description) */
  private static final String POLICY_GROUP_NAME = STEM_NAME + ":secPatch126policy";

  /** group with the app type (service name) */
  private static final String APP_GROUP_NAME = STEM_NAME + ":secPatch126app";

  /** data owner payload */
  private static final String DATA_OWNER = "secPatch126owner<img id=secPatch126xss1 src=x onerror=alert(1261)>";

  /** member description payload */
  private static final String MEMBER_DESCRIPTION = "secPatch126desc<img id=secPatch126xss2 src=x onerror=alert(1262)>";

  /** service name payload */
  private static final String SERVICE_NAME = "secPatch126svc<img id=secPatch126xss3 src=x onerror=alert(1263)>";

  /** folder data owner payload */
  private static final String FOLDER_DATA_OWNER = "secPatch126folder<img id=secPatch126xss4 src=x onerror=alert(1264)>";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ObjectTypeSettingsViewBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public ObjectTypeSettingsViewBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public ObjectTypeSettingsViewBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * create the groups and folder with the proof type settings
   * @return policy group id, app group id, folder id
   */
  private static String[] createData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Stem stem = new StemSave(rootSession).assignName(STEM_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      Group policyGroup = new GroupSave(rootSession).assignName(POLICY_GROUP_NAME).save();
      Group appGroup = new GroupSave(rootSession).assignName(APP_GROUP_NAME).save();
      // the same save classes the type edit screen uses
      new GdgTypeGroupSave().assignGroup(policyGroup).assignType("policy")
          .assignDataOwner(DATA_OWNER).assignMemberDescription(MEMBER_DESCRIPTION).save();
      new GdgTypeGroupSave().assignGroup(appGroup).assignType("app")
          .assignServiceName(SERVICE_NAME).save();
      new GdgTypeStemSave().assignStem(stem).assignType("org")
          .assignDataOwner(FOLDER_DATA_OWNER).assignMemberDescription("secPatch126 folder members").save();
      return new String[] {policyGroup.getId(), appGroup.getId(), stem.getId()};
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the groups and the folder
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String groupName : new String[] {POLICY_GROUP_NAME, APP_GROUP_NAME}) {
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
   * open a type view and wait until each payload shows as text in a table cell (escaped, the
   * text appears; unescaped, the payload becomes an img and the wait times out), then check no
   * element was injected and no dialog opened
   * @param page
   * @param url
   * @param dialogs
   * @param payloads
   * @throws Exception
   */
  private static void checkView(Page page, String url, List<String> dialogs, String... payloads) throws Exception {
    page.navigate(url);
    for (String payload : payloads) {
      page.locator("td", new Page.LocatorOptions().setHasText(payload)).first().waitFor();
    }
    // give a late onerror a moment to fire before checking
    Thread.sleep(1000);
    for (int i = 1; i <= 4; i++) {
      Locator injected = page.locator("#secPatch126xss" + i);
      if (injected.count() > 0) {
        // say where it landed: the markup around the injected element
        Object where = injected.first().evaluate("e => (e.parentElement && e.parentElement.parentElement ? e.parentElement.parentElement : e.parentElement || e).outerHTML");
        fail(url + ": payload " + i + " added an element, here: " + where);
      }
    }
    assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());
  }

  // ---------------------------------------------------------------- test

  /**
   * the group and folder type views show data owner, member description and service name as
   * text
   * @throws Exception
   */
  public void testObjectTypeSettingsEscaped() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // a previous aborted run may have left data behind
    deleteData();
    try (Playwright playwright = Playwright.create()) {
      String[] ids = createData();
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
      String app = baseUrl + "/grouperUi/app/UiV2Main.index?operation=";

      checkView(page, app + "UiV2GrouperObjectTypes.viewObjectTypesOnGroup&groupId=" + ids[0], dialogs,
          DATA_OWNER, MEMBER_DESCRIPTION);
      checkView(page, app + "UiV2GrouperObjectTypes.viewObjectTypesOnGroup&groupId=" + ids[1], dialogs,
          SERVICE_NAME);
      checkView(page, app + "UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=" + ids[2], dialogs,
          FOLDER_DATA_OWNER);

      browser.close();
      System.out.println("^secPatch126 object type views checked: policy group, app group, org folder");
    } finally {
      deleteData();
    }
  }

}
