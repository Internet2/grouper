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
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (attestationEmailAddressesStoredXss), browser check: the attestation email
 * addresses a group updater types are stored and later printed in the attestation settings
 * tables by groupAttestationListOfSettingsHelper.jsp, which must escape them.
 * AttestationListOfSettingsHelperEscapingTest checks the JSP source; this test checks what it cannot:
 * that the patched JSP compiles in the running UI and that both screens using it (the overall
 * settings list and a folder's settings list) show a stored payload as literal text, with no
 * script run and no injected element.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch28 with one group whose attestation email addresses hold the payload
 * (stored through the API, the same attributes the edit screen saves), deleted at the end.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class AttestationListOfSettingsBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch28";

  /** group with attestation settings */
  private static final String GROUP_NAME = STEM_NAME + ":secPatch28grp";

  /** attribute-breaking payload; if it is not escaped it adds an img that runs onerror */
  private static final String PAYLOAD = "\"><img id=secPatch28xss src=x onerror=alert(28)>";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AttestationListOfSettingsBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public AttestationListOfSettingsBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public AttestationListOfSettingsBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- data helpers

  /**
   * create the folder and the group with direct attestation whose email addresses are the payload
   * @return the folder id
   */
  private static String createData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave().assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      // same attributes UiV2Attestation.editGroupAttestationSave stores for custom email addresses
      AttributeAssign assign = group.getAttributeDelegate().assignAttribute(
          GrouperAttestationJob.retrieveAttributeDefNameValueDef()).getAttributeAssign();
      assign.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      assign.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
      assign.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "true");
      assign.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName(), PAYLOAD);
      return StemFinder.findByName(rootSession, STEM_NAME, true).getId();
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
   * open a settings list screen and return the row of the test group
   * @param page
   * @param baseUrl
   * @param operationAndParams e.g. UiV2Attestation.allSettings
   * @return the table row of the test group (waited for)
   */
  private static Locator openSettingsRow(Page page, String baseUrl, String operationAndParams) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=" + operationAndParams);
    // the group link cell shows the group's name; that row also holds the email cell
    Locator row = page.locator("table.footable tbody tr",
        new Page.LocatorOptions().setHasText("secPatch28grp"));
    row.first().waitFor();
    return row.first();
  }

  // ---------------------------------------------------------------- test

  /**
   * the stored payload shows as literal text in the email column of the overall settings list and
   * of the folder settings list; no dialog opens and no element from the payload lands in the page
   * @throws Exception
   */
  public void testEmailAddressesRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    deleteData();
    String stemId = createData();
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

      // folder settings list (stemAttestationSettings.jsp includes the helper)
      Locator row = openSettingsRow(page, baseUrl,
          "UiV2Attestation.viewSettingsInStem&stemId=" + stemId);
      assertTrue("folder list: email cell must show the payload as text: " + row.textContent(),
          row.textContent().contains(PAYLOAD));
      assertEquals("folder list: payload must not add an element", 0, page.locator("#secPatch28xss").count());

      // overall settings list (groupAttestationOverallSettings.jsp includes the helper)
      row = openSettingsRow(page, baseUrl, "UiV2Attestation.allSettings");
      assertTrue("overall list: email cell must show the payload as text: " + row.textContent(),
          row.textContent().contains(PAYLOAD));
      assertEquals("overall list: payload must not add an element", 0, page.locator("#secPatch28xss").count());

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteData();
    }
  }

}
