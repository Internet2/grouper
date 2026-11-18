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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (provisionerErrorFilterReflectedXss), browser check: the filter values on the
 * provisioner "view errors" screen come from the request, and provisionerConfigViewErrors.jsp
 * must render them escaped.  ProvisionerConfigViewErrorsJspEscapingTest checks the JSP source; this
 * test checks what the JUnit test cannot: that the patched JSP compiles in the running UI and that
 * a crafted URL shows the payload as literal text in the input box, with no script run and no
 * injected element.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * one SQL provisioner config (secPatch84) in the database config, deleted at the end.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class ProvisionerConfigViewErrorsBrowserTest extends TestCase {

  /** provisioner config id this test creates and deletes */
  private static final String CONFIG_ID = "secPatch84";

  /** attribute-breaking payload; if it is not escaped it closes the value and adds an img */
  private static final String PAYLOAD = "\"><img id=secPatch84xss src=x onerror=alert(84)>";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ProvisionerConfigViewErrorsBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public ProvisionerConfigViewErrorsBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public ProvisionerConfigViewErrorsBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- config helpers

  /**
   * store one grouper-loader.properties key in the database config
   * @param suffix after provisioner.secPatch84.
   * @param value
   */
  private static void storeKey(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("provisioner." + CONFIG_ID + "." + suffix).value(value).store();
  }

  /** keys this test stores, so cleanup deletes exactly those */
  private static final String[][] KEYS = {
      {"class", "edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner"},
      {"dbExternalSystemConfigId", "grouper"},
      {"operateOnGrouperGroups", "true"},
      {"groupTableName", "secpatch84_groups"},
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

  /**
   * create the provisioner config (the screen needs a real provisioner to load)
   */
  private static void createProvisionerConfig() {
    // GrouperDbConfig checks the caller may edit config, so it needs a (root) session
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String[] key : KEYS) {
        storeKey(key[0], key[1]);
      }
      // the UI caches the provisioner list for 5 minutes and only its own save clears it; this
      // clears it here and notifies the running UI (cache database), which drops it in seconds
      edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.clearTargetsCache();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the provisioner config keys this test stored
   */
  private static void deleteProvisionerConfig() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
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

  /**
   * open the view errors screen (an allowed screen operation), type the payload into one filter
   * box and press Submit, the way a user would.  A crafted link straight to
   * viewProvisionerErrorsSubmit is refused by the advisory C1 URL operation guard, so the form is the
   * path that reaches the JSP with a request value
   * @param page
   * @param baseUrl
   * @param inputId provisionerConfigErrorGroupId or provisionerConfigErrorEntityId
   * @return the value the box shows after the submit re-renders the screen
   */
  private static String submitPayload(Page page, String baseUrl, String inputId) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation="
        + "UiV2ProvisionerConfiguration.viewProvisionerErrors&provisionerConfigId=" + CONFIG_ID);
    page.locator("#" + inputId).waitFor();
    page.locator("#" + inputId).fill(PAYLOAD);
    // mark the box so we can tell when the submit has replaced it with a freshly rendered one
    page.evaluate("id => document.getElementById(id).setAttribute('data-secpatch-old', 'true')", inputId);
    // [type=submit] without a tag: an <input> in every release today, a <button> would match too
    page.locator("#provisionerConfigViewErrors [type=submit]").click();
    page.locator("#" + inputId + ":not([data-secpatch-old])").waitFor();
    return page.locator("#" + inputId).inputValue();
  }

  // ---------------------------------------------------------------- test

  /**
   * a crafted URL puts the payload in the group and entity filter boxes as literal text; no
   * dialog opens and no element from the payload lands in the page
   * @throws Exception
   */
  public void testFilterPayloadRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    createProvisionerConfig();
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

      // the running UI reads database config through a cache; retry until it sees secPatch84
      String groupValue = null;
      RuntimeException last = null;
      for (int i = 0; i < 6 && groupValue == null; i++) {
        try {
          groupValue = submitPayload(page, baseUrl, "provisionerConfigErrorGroupId");
        } catch (RuntimeException re) {
          last = re;
          Thread.sleep(5000);
        }
      }
      if (groupValue == null) {
        throw new RuntimeException("view errors screen never loaded for " + CONFIG_ID, last);
      }

      assertEquals("group filter box must hold the payload as literal text", PAYLOAD, groupValue);
      assertEquals("payload must not add an element", 0, page.locator("#secPatch84xss").count());

      String entityValue = submitPayload(page, baseUrl, "provisionerConfigErrorEntityId");
      assertEquals("entity filter box must hold the payload as literal text", PAYLOAD, entityValue);
      assertEquals("payload must not add an element", 0, page.locator("#secPatch84xss").count());

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteProvisionerConfig();
    }
  }

}
