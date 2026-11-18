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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (localEntityJwtAllowedCidrsStoredXss), browser check: the "Allowed from
 * networks" value of a local entity WS JWT key is stored as typed, and
 * localEntityWsJwtKeyContents.jsp must render it escaped.  LocalEntityWsJwtKeyContentsEscapingTest
 * checks the JSP source; this test checks what the JUnit test cannot: that the patched JSP
 * compiles in the running UI and that a stored payload shows as literal text on the key detail
 * screen, with no script run and no injected element.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * a local entity test:secPatch79:secPatch79entity with a WS JWT key, deleted at the end.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class LocalEntityWsJwtKeyContentsBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch79";

  /** local entity this test creates and deletes */
  private static final String ENTITY_NAME = STEM_NAME + ":secPatch79entity";

  /** payload stored as "allowed from networks"; if it is not escaped it adds an img */
  private static final String PAYLOAD = "\"><img id=secPatch79xss src=x onerror=alert(79)>";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(LocalEntityWsJwtKeyContentsBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public LocalEntityWsJwtKeyContentsBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public LocalEntityWsJwtKeyContentsBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * create the local entity and its WS JWT key with the payload as allowed networks
   * @return member id of the local entity (the key screen takes it as memberId)
   */
  private static String createEntityWithKey() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group entity = new GroupSave(rootSession).assignName(ENTITY_NAME)
          .assignTypeOfGroup(TypeOfGroup.entity).assignCreateParentStemsIfNotExist(true).save();
      Member member = MemberFinder.findBySubject(rootSession, entity.toSubject(), true);
      Member rootMember = MemberFinder.findBySubject(rootSession, rootSession.getSubject(), true);
      // same calls the UI create screen makes (UiV2LocalEntity.createNewWsJwtKeySubmit)
      String[] publicPrivateKey = GrouperUtil.generateRsaKeypair(2048);
      new GrouperPasswordSave()
        .assignAllowedFromCidrs(PAYLOAD)
        .assignApplication(GrouperPassword.Application.WS)
        .assignEncryptionType(GrouperPassword.EncryptionType.RS_2048)
        .assignEntityType("localEntity")
        .assignMemberIdWhoSetPassword(rootMember.getId())
        .assignPublicKey(publicPrivateKey[0])
        .assignUsername(member.getId())
        .assignMemberId(member.getId())
        .save();
      return member.getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the key, the local entity and the folder this test created
   */
  private static void deleteEntityWithKey() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group entity = GroupFinder.findByName(rootSession, ENTITY_NAME, false);
      if (entity != null) {
        Member member = MemberFinder.findBySubject(rootSession, entity.toSubject(), false);
        if (member != null) {
          GrouperPassword grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword()
              .findByUsernameApplication(member.getId(), GrouperPassword.Application.WS.name());
          if (grouperPassword != null) {
            try {
              new GrouperPasswordSave().assignUuid(grouperPassword.getId())
                .assignMemberId(member.getId()).assignSaveMode(SaveMode.DELETE).save();
            } catch (RuntimeException re) {
              // keep cleaning up the rest
              re.printStackTrace();
            }
          }
        }
        entity.delete();
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

  // ---------------------------------------------------------------- test

  /**
   * the stored payload shows as literal text on the WS JWT key detail screen; no dialog opens and
   * no element from the payload lands in the page
   * @throws Exception
   */
  public void testAllowedFromCidrsRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    deleteEntityWithKey();
    try (Playwright playwright = Playwright.create()) {
      String memberId = createEntityWithKey();
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

      // the key detail screen (an allowed screen operation)
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation="
          + "UiV2LocalEntity.viewLocalEntityWSJwtKeys&memberId=" + memberId);
      page.locator("#viewWsJwtKey td").first().waitFor();
      assertEquals("payload must not add an element", 0, page.locator("#secPatch79xss").count());

      Locator cell = page.locator("#viewWsJwtKey td").filter(new Locator.FilterOptions().setHasText("secPatch79xss"));
      assertEquals("allowed from networks must show the payload as literal text", 1, cell.count());
      assertEquals("allowed from networks must show the payload as literal text", PAYLOAD,
          cell.first().textContent().trim());

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteEntityWithKey();
    }
  }

}
