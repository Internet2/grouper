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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (viewAuditsAuditTypeReflectedXss), browser check: the auditType request
 * parameter of the group and subject audit screens is written into a hidden input by
 * group/groupViewAudits.jsp and subject/subjectViewAudits.jsp, and must be escaped.
 * ViewAuditsJspEscapingTest checks the JSP source; this test checks what that test cannot: that
 * the patched JSPs compile in the running UI and that a crafted link's payload comes back as the
 * literal input value, with no script run and no injected element.
 *
 * <p>Opens the crafted links the way a victim would (UiV2Main.index with the operation and
 * parameters in the url; UiV2Group.viewAudits and UiV2Subject.viewAudits are on advisory C1's
 * deep-link allowlist).  The group screen uses the wheel group (groups.wheel.group, default
 * etc:sysadmingroup); if it does not exist, a group etc:secPatch149group is created and deleted
 * afterwards.  The subject screen uses GrouperSystem itself.  Nothing else is changed: the
 * audit screens only read.</p>
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Login is the UI's form login (or HTTP
 * basic) as GrouperSystem/pass (local dev).  Raw Playwright API (not GrouperPage) so the same
 * file works on every release with Playwright.</p>
 */
public class ViewAuditsAuditTypeBrowserTest extends TestCase {

  /** attribute-breaking payload; if it is not escaped it closes the value and adds an img */
  private static final String PAYLOAD = "\"><img id=secPatch149xss src=x onerror=alert(149)>";

  /** fallback group if the wheel group does not exist */
  private static final String FALLBACK_GROUP_NAME = "etc:secPatch149group";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ViewAuditsAuditTypeBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public ViewAuditsAuditTypeBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public ViewAuditsAuditTypeBrowserTest(String name) {
    super(name);
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
   * log in through the UI's form login, or HTTP basic if the UI answers 401
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
   * url-encode a request parameter value
   * @param value
   * @return the encoded value
   */
  private static String encode(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (java.io.UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
  }

  /**
   * open a crafted audit link, wait for the audit filter form the ajax renders, and read the
   * auditType hidden input back
   * @param page
   * @param url full url of the crafted link
   * @param formId id of the audit filter form (groupFilterAuditFormId / subjectFilterAuditFormId)
   * @return the value of the auditType hidden input as the browser parsed it
   */
  private static String openAndReadAuditType(Page page, String url, String formId) {
    page.navigate(url);
    // the filter form holds the hidden input, escaped or not
    page.locator("#" + formId).waitFor();
    Locator hidden = page.locator("#" + formId + " input[name='auditType']");
    hidden.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    return (String) page.evaluate("formId => document.querySelector('#' + formId + \" input[name='auditType']\").value", formId);
  }

  /**
   * open with one retry for a busy shared Tomcat (never retries an assertion)
   * @param page
   * @param url
   * @param formId
   * @return the hidden input value
   */
  private static String openWithRetry(Page page, String url, String formId) {
    try {
      return openAndReadAuditType(page, url, formId);
    } catch (com.microsoft.playwright.TimeoutError te) {
      return openAndReadAuditType(page, url, formId);
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * the auditType hidden input after a payload request is safe either way: the payload kept as
   * literal attribute text (the JSP escaping), or replaced by the screen's default audit type
   * (upstream GRP-7374 allow-list, in 7.7.0 and later, and in UiV2Subject wherever another fix
   * ships the 7.9.0 file, e.g. 7.3.0 to 7.6.1)
   * @param screen for messages
   * @param defaultAuditType what the allow-list maps an unknown value to
   * @param value the hidden input's value
   */
  private static void assertPayloadSafe(String screen, String defaultAuditType, String value) {
    assertTrue(screen + " auditType hidden input must hold the payload as literal text or the default '"
        + defaultAuditType + "', but was: " + value, PAYLOAD.equals(value) || defaultAuditType.equals(value));
  }

  /**
   * on both audit screens an ordinary audit type is kept; a payload comes back as the literal
   * value, no dialog opens and no element from the payload lands in the page
   * @throws Exception
   */
  public void testAuditTypePayloadRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // a group GrouperSystem can open the audits of: the wheel group, else our own
    String groupId = null;
    boolean createdGroup = false;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      String wheelName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group", "etc:sysadmingroup");
      Group group = GroupFinder.findByName(rootSession, wheelName, false);
      if (group == null) {
        group = GroupFinder.findByName(rootSession, FALLBACK_GROUP_NAME, false);
        if (group == null) {
          group = new GroupSave(rootSession).assignName(FALLBACK_GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
          createdGroup = true;
        }
      }
      groupId = group.getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    try {
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

        // operation first: grouperUi.js builds the ajax url from the operation and appends the rest
        String groupUrl = baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewAudits"
            + "&groupId=" + encode(groupId) + "&auditType=";
        String subjectUrl = baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Subject.viewAudits"
            + "&subjectId=GrouperSystem&sourceId=g:isa&auditType=";

        // group screen: positive control, then the reported sink
        assertEquals("ordinary group audit type must be kept", "membership",
            openWithRetry(page, groupUrl + "membership", "groupFilterAuditFormId"));
        assertPayloadSafe("group", "group", openWithRetry(page, groupUrl + encode(PAYLOAD), "groupFilterAuditFormId"));
        assertEquals("group payload must not add an element", 0, page.locator("#secPatch149xss").count());

        // subject screen: positive control, then the reported sink
        assertEquals("ordinary subject audit type must be kept", "actions",
            openWithRetry(page, subjectUrl + "actions", "subjectFilterAuditFormId"));
        assertPayloadSafe("subject", "membership", openWithRetry(page, subjectUrl + encode(PAYLOAD), "subjectFilterAuditFormId"));
        assertEquals("subject payload must not add an element", 0, page.locator("#secPatch149xss").count());

        // give a late onerror a moment to fire before checking
        Thread.sleep(1000);
        assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

        browser.close();
      }
    } finally {
      if (createdGroup) {
        GrouperSession cleanupSession = GrouperSession.startRootSession();
        try {
          Group group = GroupFinder.findByName(cleanupSession, FALLBACK_GROUP_NAME, false);
          if (group != null) {
            group.delete();
          }
        } finally {
          GrouperSession.stopQuietly(cleanupSession);
        }
      }
    }
    System.out.println("^secPatch149 browser test ran");
  }

}
