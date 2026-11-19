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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.usdu.UsduService;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (subjectResolutionStoredXss), browser check: the subject resolution search
 * result (subjectResolutionViewSubject.jsp) and the unresolved subjects list
 * (unresolvedSubjectsHelper.jsp) render subject source / member values, and must escape them.
 * SubjectResolutionJspEscapingTest checks the JSP source; this test checks what the JUnit test cannot:
 * that the patched JSPs compile in the running UI and show the payloads as literal text, with no
 * script run and no injected element, and that the unresolved subject link still carries the exact
 * subject id.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own, in
 * the example JDBC subject table (source jdbc): subject secPatch29a (resolvable, payload name,
 * description and login id) and a member whose subject id is a payload and whose subject row is
 * deleted (unresolvable, payload name and description); both subjects, both members and the
 * subject resolution marker the UI puts on the unresolvable member are deleted at the end.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class SubjectResolutionBrowserTest extends TestCase {

  /** resolvable subject id */
  private static final String SUBJECT_ID_A = "secPatch29a";

  /** name of the resolvable subject */
  private static final String NAME_A = "<img id=secPatch29xssAname src=x onerror=alert(291)>";

  /** description of the resolvable subject */
  private static final String DESCRIPTION_A = "<img id=secPatch29xssAdesc src=x onerror=alert(292)>";

  /** login id (identifier0) of the resolvable subject */
  private static final String LOGIN_ID_A = "<img id=secPatch29xssAlogin src=x onerror=alert(293)>";

  /** unresolvable subject id: closes the attribute and adds an img if the link text is not escaped */
  private static final String SUBJECT_ID_B = "secPatch29b\"><img id=secPatch29xssBid src=x onerror=alert(294)>";

  /** member name of the unresolvable subject */
  private static final String NAME_B = "<img id=secPatch29xssBname src=x onerror=alert(295)>";

  /** member description of the unresolvable subject */
  private static final String DESCRIPTION_B = "<img id=secPatch29xssBdesc src=x onerror=alert(296)>";

  /** any element a payload would add */
  private static final String XSS_SELECTOR = "[id^=secPatch29xss]";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(SubjectResolutionBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public SubjectResolutionBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public SubjectResolutionBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * insert a subject in the example jdbc subject table
   * @param subjectId
   * @param name
   * @param description
   * @param loginId
   */
  private static void insertSubject(String subjectId, String name, String description, String loginId) {
    HibernateSession.bySqlStatic().executeSql(
        "insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
        GrouperUtil.toListObject(subjectId, name));
    HibernateSession.bySqlStatic().executeSql(
        "insert into subjectattribute (subjectid, name, value, searchvalue) values (?, 'description', ?, ?)",
        GrouperUtil.toListObject(subjectId, description, description.toLowerCase()));
    HibernateSession.bySqlStatic().executeSql(
        "insert into subjectattribute (subjectid, name, value, searchvalue) values (?, 'loginid', ?, ?)",
        GrouperUtil.toListObject(subjectId, loginId, loginId.toLowerCase()));
  }

  /**
   * delete a subject from the example jdbc subject table
   * @param subjectId
   */
  private static void deleteSubject(String subjectId) {
    HibernateSession.bySqlStatic().executeSql("delete from subjectattribute where subjectid = ?",
        GrouperUtil.toListObject(subjectId));
    HibernateSession.bySqlStatic().executeSql("delete from subject where subjectid = ?",
        GrouperUtil.toListObject(subjectId));
  }

  /**
   * subject A resolvable with a member; subject B only as a member (its subject row is deleted
   * after the member is made, so it does not resolve)
   */
  private static void createData() {
    insertSubject(SUBJECT_ID_A, NAME_A, DESCRIPTION_A, LOGIN_ID_A);
    insertSubject(SUBJECT_ID_B, NAME_B, DESCRIPTION_B, "secPatch29bLogin");
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      // the member rows copy name, description and identifier0 from the subject
      for (String subjectId : new String[] {SUBJECT_ID_A, SUBJECT_ID_B}) {
        Subject subject = SubjectFinder.findByIdAndSource(subjectId, "jdbc", true);
        Member member = MemberFinder.findBySubject(rootSession, subject, true);
        member.updateMemberAttributes(subject, true);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    deleteSubject(SUBJECT_ID_B);
  }

  /**
   * delete the subject resolution marker, the members and the subjects
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String subjectId : new String[] {SUBJECT_ID_A, SUBJECT_ID_B}) {
        try {
          Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subjectId, "jdbc", false);
          if (member != null) {
            // the search marks an unresolvable member with subject resolution attributes
            UsduService.deleteAttributeAssign(member);
            HibernateSession.bySqlStatic().executeSql("delete from grouper_members where id = ?",
                GrouperUtil.toListObject(member.getId()));
          }
        } catch (RuntimeException re) {
          re.printStackTrace();
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    for (String subjectId : new String[] {SUBJECT_ID_A, SUBJECT_ID_B}) {
      try {
        deleteSubject(subjectId);
      } catch (RuntimeException re) {
        re.printStackTrace();
      }
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
   * search a subject id on the subject resolution search screen, like a user
   * @param page
   * @param baseUrl
   * @param subjectId
   * @param expectedText literal text the result must show (times out if it rendered as markup)
   * @return the result text
   */
  private static String search(Page page, String baseUrl, String subjectId, String expectedText) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2SubjectResolution.searchSubjects");
    // by name, not #subjectSearchQueryId: 4.26.0's text field has no id
    page.locator("#searchPageForm input[name=subjectId]").fill(subjectId);
    // any .btn-primary: a <button> on 7.x, an <a href="#"> on 6.x and 4.26.0
    page.locator("#searchPageForm .btn-primary").first().click();
    Locator result = page.locator("#searchResultId");
    // an unescaped payload is an element, not text, so this times out without the fix
    page.locator("#searchResultId td", new Page.LocatorOptions().setHasText(expectedText)).first().waitFor();
    return result.textContent();
  }

  /**
   * the subjectId parameter of a link, url-decoded once (what the server receives)
   * @param href
   * @return the subject id
   * @throws Exception
   */
  private static String subjectIdParam(String href) throws Exception {
    int index = href == null ? -1 : href.indexOf("&subjectId=");
    assertTrue("link has a subjectId: " + href, index >= 0);
    String value = href.substring(index + "&subjectId=".length());
    int end = value.indexOf('&');
    if (end >= 0) {
      value = value.substring(0, end);
    }
    return URLDecoder.decode(value, "UTF-8");
  }

  // ---------------------------------------------------------------- test

  /**
   * search shows the resolvable and the unresolvable subject's values as text; the unresolved
   * list shows the subject id as link text, and the link carries the exact id
   * @throws Exception
   */
  public void testSubjectValuesRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // a previous aborted run may have left data behind
    deleteData();
    createData();
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newContext().newPage();
      // fail in seconds, not the 30 second Playwright default, when a screen does not render
      page.setDefaultTimeout(15000);

      // any alert/confirm/prompt means a payload ran
      final List<String> dialogs = new ArrayList<String>();
      page.onDialog(dialog -> {
        dialogs.add(dialog.message());
        dialog.dismiss();
      });

      String baseUrl = uiBaseUrl();
      login(page, baseUrl);

      // resolvable subject: subject name, description and the member's identifier0
      String resultA = search(page, baseUrl, SUBJECT_ID_A, NAME_A);
      assertTrue("description as text: " + resultA, resultA.contains(DESCRIPTION_A));
      assertTrue("login id as text: " + resultA, resultA.contains(LOGIN_ID_A));
      assertEquals("payload must not add an element", 0, page.locator(XSS_SELECTOR).count());

      // unresolvable subject: values from the member row (this also marks it unresolved)
      String resultB = search(page, baseUrl, SUBJECT_ID_B, NAME_B);
      assertTrue("description as text: " + resultB, resultB.contains(DESCRIPTION_B));
      assertEquals("payload must not add an element", 0, page.locator(XSS_SELECTOR).count());

      // unresolved list: the subject id is the link text, and the link carries the exact id
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2SubjectResolution.viewUnresolvedSubjects");
      // 6.x/7.x put the operation in href (and onclick); 4.26.0 has href="#" and only
      // onclick="return guiV2link('operation=...')", so match either attribute (no tag: an <a> today
      // in every release, but menu/link tags have changed between releases)
      Locator link = page.locator("[href*='UiV2Subject.viewSubject'][href*='subjectId=secPatch29b'], "
          + "[onclick*='UiV2Subject.viewSubject'][onclick*='subjectId=secPatch29b']");
      link.first().waitFor();
      assertEquals("subject id link text", SUBJECT_ID_B, link.first().textContent().trim());
      String linkHref = link.first().getAttribute("href");
      if (linkHref == null || linkHref.indexOf("&subjectId=") < 0) {
        // 4.26.0: the subject id is only in the onclick guiV2link call
        linkHref = link.first().getAttribute("onclick");
      }
      assertEquals("link subject id", SUBJECT_ID_B, subjectIdParam(linkHref));
      String unresolvedText = page.locator("#grouperMainContentDivId").textContent();
      assertTrue("name as text", unresolvedText.contains(NAME_B));
      assertTrue("description as text", unresolvedText.contains(DESCRIPTION_B));

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertEquals("payload must not add an element", 0, page.locator(XSS_SELECTOR).count());
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteData();
    }
  }

}
