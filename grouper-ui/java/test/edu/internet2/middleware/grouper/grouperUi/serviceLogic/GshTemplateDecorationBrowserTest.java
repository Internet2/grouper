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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H4 (gshTemplateDecorationAuthorization), browser check: UiV2Template.newTemplate
 * must run the template preflight (enabled, owner, securityRunType) before it loads the V2 template
 * and runs decorateTemplateForUiDisplay.  GshTemplateDecorationAuthorizationTest covers the
 * preflight itself; the UI wiring needs the UI filter thread locals, so this test opens the direct
 * URL from the md reproduction in the running UI.
 *
 * <p>The template (secPatch24, interpreted V2, one text input) has a decorator that puts
 * DECORATOR_RAN_FOR_&lt;subject id&gt; in its input.  Checks: GrouperSystem (wheel) gets the form
 * with the marker (the control: the marker check is not vacuous); test.subject.0 (VIEW on the group,
 * not wheel) gets an error and no marker; templateType=&lt;b ...&gt; shows as literal text; disabled
 * (with securityRunType=everyone, so only the disabled check stops it) gives an error and no marker
 * for both users.</p>
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own: the
 * grouperGshTemplate.secPatch24.* keys in the database config and folder test:secPatch24 with one
 * group, deleted at the end.  Logins are the UI's form login (GrouperSystem/pass and
 * test.subject.0/pass, local dev).  Uses the raw Playwright API (not GrouperPage) so the same file
 * works on releases without the app.browser helpers.</p>
 */
public class GshTemplateDecorationBrowserTest extends TestCase {

  /** template config id this test creates and deletes */
  private static final String CONFIG_ID = "secPatch24";

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch24";

  /** owner group of the template form */
  private static final String GROUP_NAME = STEM_NAME + ":group";

  /** the decorator writes this plus the subject id into the input */
  private static final String MARKER = "DECORATOR_RAN_FOR_";

  /** a template id that is html; must come back as text in the error */
  private static final String HTML_TEMPLATE_TYPE = "<b id=secPatch24b>x</b>";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GshTemplateDecorationBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GshTemplateDecorationBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateDecorationBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- config and data

  /**
   * template source: the decorator marks the first input with the current subject id.  It calls
   * setValue(String), not setValueObject(Object): 4.26.0's GshTemplateInputConfigAndValue only has
   * setValue (setValueObject came later; setValue is still there, deprecated, on 6.x and 7.x), and
   * the template is Groovy, so a missing method would only fail at run time in the UI
   */
  private static final String SOURCE =
      "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateDecorateForUiInput;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
      + "public class SecPatch24DecoratorProof extends GshTemplateV2 {\n"
      + "  public void decorateTemplateForUiDisplay(GshTemplateDecorateForUiInput input) {\n"
      + "    if (!input.getGshTemplateInputConfigAndValues().isEmpty()) {\n"
      + "      input.getGshTemplateInputConfigAndValues().values().iterator().next()\n"
      + "          .setValue(\"" + MARKER + "\" + input.getCurrentSubject().getId());\n"
      + "    }\n"
      + "  }\n"
      + "  public void gshRunLogic(GshTemplateV2input input, GshTemplateV2output output) {\n"
      + "    output.getGsh_builtin_gshTemplateOutput().addOutputLine(\"success\", \"not needed\");\n"
      + "  }\n"
      + "}\n";

  /** keys this test stores (suffix after grouperGshTemplate.secPatch24.), so cleanup deletes exactly those */
  private static final String[][] KEYS = {
      {"enabled", "true"},
      {"templateType", "gsh"},
      {"templateVersion", "V2"},
      {"templateName", CONFIG_ID},
      {"templateDescription", CONFIG_ID},
      {"runAsType", "currentUser"},
      {"securityRunType", "wheel"},
      {"showOnGroups", "true"},
      {"groupShowType", "allGroups"},
      {"numberOfInputs", "1"},
      {"input.0.name", "gsh_input_proof"},
      {"input.0.label", "Proof"},
      {"input.0.description", "Decorator execution marker"},
      {"input.0.type", "string"},
      {"input.0.formElementType", "text"},
      {"input.0.required", "false"},
      {"input.0.validationType", "none"},
      {"gshTemplate", SOURCE},
  };

  /**
   * store one template key in the database config
   * @param suffix
   * @param value
   */
  private static void storeKey(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperGshTemplate." + CONFIG_ID + "." + suffix).value(value).store();
  }

  /** owner group uuid, filled by setUpData */
  private String groupId;

  /**
   * template config (enabled, wheel) and a group test.subject.0 can VIEW
   */
  private void setUpData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String[] key : KEYS) {
        storeKey(key[0], key[1]);
      }
      new StemSave().assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
      Group group = new GroupSave().assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      group.grantPriv(SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true), AccessPrivilege.VIEW, false);
      this.groupId = group.getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * disable the template and open security to everyone, so only the disabled check can stop it
   */
  private static void disableTemplate() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      storeKey("enabled", "false");
      storeKey("securityRunType", "everyone");
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the template keys, group and folder this test created
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String[] key : KEYS) {
        try {
          new GrouperDbConfig().configFileName("grouper.properties")
              .propertyName("grouperGshTemplate." + CONFIG_ID + "." + key[0]).delete();
        } catch (RuntimeException re) {
          // already gone; keep cleaning up the rest
        }
      }
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

  /** script: the screen showed a message, or the decorator marker is in the page */
  private static final String SETTLED_JS = "t => { var m = document.getElementById('messaging');"
      + " return (m != null && m.innerText.trim().length > 0) || document.body.innerHTML.indexOf(t) >= 0; }";

  /**
   * open the md reproduction's direct URL for a template on the group, wait for the error message
   * or the form
   * @param page
   * @param baseUrl
   * @param templateType
   * @return the message text ("" if none)
   */
  private String openTemplate(Page page, String baseUrl, String templateType) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Template.newTemplate&groupId="
        + this.groupId + "&templateType=" + java.net.URLEncoder.encode(templateType, java.nio.charset.StandardCharsets.UTF_8));
    page.waitForFunction(SETTLED_JS, MARKER);
    return page.locator("#messaging").count() == 0 ? "" : page.locator("#messaging").innerText().trim();
  }

  /**
   * @param page
   * @return true if the decorator's marker is anywhere in the page (the input value)
   */
  private static boolean markerInPage(Page page) {
    return (Boolean) page.evaluate("t => document.body.innerHTML.indexOf(t) >= 0 "
        + "|| Array.prototype.some.call(document.querySelectorAll('input,textarea'), function (e) { return (e.value || '').indexOf(t) >= 0; })",
        MARKER);
  }

  /**
   * @param page
   * @return start of the main content text, for failure messages
   */
  private static String mainText(Page page) {
    String text = page.locator("#grouperMainContentDivId").count() == 0 ? "" : page.locator("#grouperMainContentDivId").innerText();
    return text.substring(0, Math.min(1000, text.length()));
  }

  // ---------------------------------------------------------------- test

  /**
   * the direct newTemplate URL: refused (no decorator run) for a non-wheel user on a wheel
   * template and for a disabled template; the html template id is shown as text; GrouperSystem gets
   * the decorated form while the template is enabled
   * @throws Exception
   */
  public void testDirectUrlDoesNotDecorateUnauthorized() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    try {
      setUpData();
      try (Playwright playwright = Playwright.create()) {
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        String baseUrl = uiBaseUrl();

        BrowserContext rootContext = browser.newContext();
        Page rootPage = rootContext.newPage();
        // fail in seconds, not the 30 second Playwright default, when a screen does not render
        rootPage.setDefaultTimeout(15000);
        login(rootPage, baseUrl, "GrouperSystem");

        BrowserContext userContext = browser.newContext();
        Page userPage = userContext.newPage();
        userPage.setDefaultTimeout(15000);
        login(userPage, baseUrl, "test.subject.0");

        // control: GrouperSystem (wheel) gets the decorated form.  The running UI reads database
        // config through a cache; retry until it sees secPatch24
        boolean decorated = false;
        String lastMessage = null;
        for (int i = 0; i < 10 && !decorated; i++) {
          try {
            lastMessage = openTemplate(rootPage, baseUrl, CONFIG_ID);
            decorated = rootPage.content().contains(MARKER + "GrouperSystem");
          } catch (RuntimeException re) {
            lastMessage = re.getMessage();
          }
          if (!decorated) {
            Thread.sleep(5000);
          }
        }
        assertTrue("control: GrouperSystem must get the decorated form; last message: " + lastMessage, decorated);

        // test.subject.0, wheel template: error, decorator did not run
        String message = openTemplate(userPage, baseUrl, CONFIG_ID);
        assertTrue("non-wheel user must get an error; main content: " + mainText(userPage), message.length() > 0);
        assertFalse("decorator must not run for test.subject.0", markerInPage(userPage));

        // html template id comes back as text
        message = openTemplate(userPage, baseUrl, HTML_TEMPLATE_TYPE);
        assertEquals("template id must not add an element", 0, userPage.locator("#secPatch24b").count());
        assertTrue("template id must show as literal text: " + message, message.contains(HTML_TEMPLATE_TYPE));

        // disabled (security everyone): wait until the UI sees it (GrouperSystem is refused too)
        disableTemplate();
        boolean refused = false;
        for (int i = 0; i < 10 && !refused; i++) {
          try {
            message = openTemplate(rootPage, baseUrl, CONFIG_ID);
            refused = !markerInPage(rootPage) && message.length() > 0;
          } catch (RuntimeException re) {
            message = re.getMessage();
          }
          if (!refused) {
            Thread.sleep(5000);
          }
        }
        assertTrue("disabled template must be refused for GrouperSystem; last message: " + message, refused);

        message = openTemplate(userPage, baseUrl, CONFIG_ID);
        assertTrue("disabled template must give test.subject.0 an error", message.length() > 0);
        assertFalse("decorator must not run for a disabled template", markerInPage(userPage));

        browser.close();
      }
    } finally {
      deleteData();
    }
  }

}
