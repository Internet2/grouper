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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (groupMembersSubjectIdStoredXss), browser check: the row action links on a
 * group's Members tab put the member's subject id into href and onclick attributes, and
 * groupContents.jsp must url-escape it.  GroupContentsSubjectIdEscapingTest checks the JSP source; this
 * test checks what the JUnit test cannot: that the patched JSP compiles in the running UI, a
 * subject id that breaks out of the attribute adds no element and runs no script, and the escaped
 * row link still carries the exact subject id (so it still opens the right subject).
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own: a
 * subject row in the example JDBC subject table (source jdbc) whose id is the payload, and the
 * folder test:secPatch77 with one group that has that subject as a member; all deleted at the
 * end.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class GroupContentsSubjectIdBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch77";

  /** group with the proof member */
  private static final String GROUP_NAME = STEM_NAME + ":secPatch77group";

  /** subject id that closes the href attribute and adds an img if it is not escaped */
  private static final String SUBJECT_ID = "secPatch77\"><img id=secPatch77xss src=x onerror=alert(77)>";

  /** subject name, a plain value so the row can be found */
  private static final String SUBJECT_NAME = "secPatch77 subject id xss proof";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GroupContentsSubjectIdBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GroupContentsSubjectIdBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public GroupContentsSubjectIdBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * insert the proof subject in the example jdbc subject table and add it to the group
   * @return the group id
   */
  private static String createData() {
    HibernateSession.bySqlStatic().executeSql(
        "insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
        GrouperUtil.toListObject(SUBJECT_ID, SUBJECT_NAME));
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Subject subject = SubjectFinder.findByIdAndSource(SUBJECT_ID, "jdbc", true);
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      group.addMember(subject, false);
      return group.getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the folder (and group and membership), the member row and the subject row
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
    } catch (RuntimeException re) {
      re.printStackTrace();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    // the member row would otherwise show up as an unresolvable subject once the subject is gone
    try {
      HibernateSession.bySqlStatic().executeSql(
          "delete from grouper_members where subject_id = ? and subject_source = 'jdbc'",
          GrouperUtil.toListObject(SUBJECT_ID));
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
    try {
      HibernateSession.bySqlStatic().executeSql("delete from subject where subjectid = ?",
          GrouperUtil.toListObject(SUBJECT_ID));
    } catch (RuntimeException re) {
      re.printStackTrace();
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
   * the subjectId parameter of a link, url-decoded once (what the server receives)
   * @param href
   * @return the subject id
   * @throws Exception
   */
  private static String subjectIdParam(String href) throws Exception {
    int index = href.indexOf("&subjectId=");
    assertTrue("link has a subjectId: " + href, index >= 0);
    String value = href.substring(index + "&subjectId=".length());
    int end = value.indexOf('&');
    if (end >= 0) {
      value = value.substring(0, end);
    }
    return URLDecoder.decode(value, "UTF-8");
  }

  /**
   * whether a UI logic class has a public screen method (operation) with the usual signature; the
   * test JVM has the same release's classes as the running UI
   * @param theClass
   * @param methodName
   * @return true if the operation exists in this release
   */
  private static boolean hasMethod(Class<?> theClass, String methodName) {
    try {
      theClass.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
      return true;
    } catch (NoSuchMethodException nsme) {
      return false;
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * the Members tab renders the proof row with no injected element and no dialog, and the row's
   * links (href and onclick) carry the exact subject id once url-decoded, so they still open the
   * right subject
   * @throws Exception
   */
  public void testSubjectIdLinksEscaped() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // a previous aborted run may have left data behind
    deleteData();
    String groupId = createData();
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

      // the Members tab of the group (groupContents.jsp).  From 5.x/6.x on, viewGroup opens on the
      // summary tab and the members are UiV2Group.viewGroupMembers; 4.26.0 has no viewGroupMembers
      // and viewGroup itself renders groupContents.jsp.  The test JVM runs the same release as the
      // UI, so ask the class which operation exists instead of guessing by version.
      String membersOperation = hasMethod(UiV2Group.class, "viewGroupMembers")
          ? "UiV2Group.viewGroupMembers" : "UiV2Group.viewGroup";
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=" + membersOperation + "&groupId=" + groupId);
      // locate by onclick, which every release has: 6.x/7.x links carry the url in href AND onclick
      // (handleGuiV2LinkClick), 4.26.0 links are href="#" with the url only in onclick (guiV2link).
      // No tag: menu items are <a> here in every release, but other 7.x menus became <button>
      Locator attributeLinks = page.locator(
          "[onclick*='UiV2MembershipAttributeAssignment.viewAttributeAssignments'][onclick*='&subjectId=secPatch77']");
      // the row links sit in a closed dropdown, so wait for them in the DOM, not visible
      attributeLinks.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertEquals("payload must not add an element", 0, page.locator("#secPatch77xss").count());
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      // the escaped links still carry the exact subject id
      assertEquals(1, attributeLinks.count());
      String href = attributeLinks.first().getAttribute("href");
      // 4.26.0 links are href="#" (the url is only in onclick, checked below)
      if (href != null && href.contains("subjectId=")) {
        assertEquals("attribute assignment link subject id", SUBJECT_ID, subjectIdParam(href));
      }
      Locator provisioningLinks = page.locator(
          "[onclick*='UiV2Provisioning.viewProvisioningOnGroupMembership'][onclick*='&subjectId=secPatch77']");
      if (provisioningLinks.count() > 0) {
        String provisioningHref = provisioningLinks.first().getAttribute("href");
        if (provisioningHref != null && provisioningHref.contains("subjectId=")) {
          assertEquals("provisioning link subject id", SUBJECT_ID, subjectIdParam(provisioningHref));
        }
        String provisioningOnclick = provisioningLinks.first().getAttribute("onclick");
        assertEquals("provisioning onclick subject id", SUBJECT_ID, subjectIdParam(provisioningOnclick.substring(
            provisioningOnclick.indexOf("'operation=") + 1, provisioningOnclick.lastIndexOf("'"))));
      }

      // the onclick copy of the link (what a click actually sends) carries the same id
      String onclick = attributeLinks.first().getAttribute("onclick");
      String onclickUrl = onclick.substring(onclick.indexOf("'operation=") + 1, onclick.lastIndexOf("'"));
      assertEquals("attribute assignment onclick subject id", SUBJECT_ID, subjectIdParam(onclickUrl));

      // not followed: the membership attribute screen the link opens still renders the raw subject
      // id (one of the ~38 JSPs of the sweep listed in the issue's open items, out of scope here)

      browser.close();
    } finally {
      deleteData();
    }
  }

}
