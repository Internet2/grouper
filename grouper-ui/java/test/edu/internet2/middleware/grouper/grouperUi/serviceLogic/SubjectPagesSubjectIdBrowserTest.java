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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (subjectPagesSubjectIdStoredXss), browser check: the subject pages, the
 * membership edit page and the membership trace page put the subject id and source id into
 * href / onclick urls and hidden inputs, and the membership edit page prints the subject's email.
 * The patched JSPs url-escape the ids in urls and html-escape them in hidden inputs and the email
 * in text.  This test checks, in the running UI, that a subject id that breaks out of the
 * attribute adds no element and runs no script, that the escaped links still carry the exact
 * subject id, and that an email holding markup shows as text.
 *
 * <p>Second test: the subject visualization (grouperVisualization.js) used to write the node id
 * (sourceId||||subjectId) into javascript:followObject(...) links and the DOT source.  The patched
 * script keeps the id as data (an index looked up by one click handler) and uses opaque DOT keys.
 * testVisualizationSubjectIdInert renders the text and graph views for a subject id with quotes,
 * a backslash and markup, and checks nothing is injected, no link is a javascript: url, the DOT
 * source does not hold the id, and clicking follows the exact subject.  It uses its own subject
 * and the folder test:secPatch124vis with one group, deleted at the end.</p>
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own: a
 * subject row in the example JDBC subject table (source jdbc) whose id is the payload, its email
 * row in subjectattribute (also a payload), and the folder test:secPatch124 with one group that
 * has that subject as a member; all deleted at the end.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev), or HTTP basic if the UI
 * answers 401.  Uses the raw Playwright API (not GrouperPage) so the same file works on releases
 * without the app.browser helpers.  Selectors use onclick, not a/button tags, since the tags
 * differ between releases.</p>
 */
public class SubjectPagesSubjectIdBrowserTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch124";

  /** group with the proof member */
  private static final String GROUP_NAME = STEM_NAME + ":secPatch124group";

  /** subject id that closes the attribute and adds an img if it is not escaped */
  private static final String SUBJECT_ID = "secPatch124\"><img id=secPatch124xss src=x onerror=alert(124)>";

  /** subject name, a plain value so the subject can be found */
  private static final String SUBJECT_NAME = "secPatch124 subject id xss proof";

  /** email of the subject, rendered as text on the membership edit page */
  private static final String EMAIL = "secPatch124mail<img id=secPatch124mailxss src=x onerror=alert(1240)>";

  /** folder of the visualization check */
  private static final String VIS_STEM_NAME = "test:secPatch124vis";

  /** group with the visualization proof member */
  private static final String VIS_GROUP_NAME = VIS_STEM_NAME + ":secPatch124visgroup";

  /**
   * subject id for the visualization check: the single quote and the trailing comment end the old
   * javascript:followObject('subject', '...') string and run a statement on click, the backslash
   * and double quote end the old href / DOT string, the img tags itself if it ever becomes markup
   */
  private static final String VIS_SUBJECT_ID = "secPatch124vis');document.title='secPatch124VIS';//\\\"><img id=secPatch124visxss src=x onerror=alert(1241)>";

  /** subject name of the visualization proof subject */
  private static final String VIS_SUBJECT_NAME = "secPatch124 visualization proof";

  /** the visualization node id of the proof subject: sourceId||||subjectId (GrouperObjectSubjectWrapper) */
  private static final String VIS_NODE_ID = "jdbc||||" + VIS_SUBJECT_ID;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(SubjectPagesSubjectIdBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public SubjectPagesSubjectIdBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public SubjectPagesSubjectIdBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * insert the proof subject (and its email) in the example jdbc tables and add it to the group
   * @return the group id
   */
  private static String createData() {
    HibernateSession.bySqlStatic().executeSql(
        "insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
        GrouperUtil.toListObject(SUBJECT_ID, SUBJECT_NAME));
    HibernateSession.bySqlStatic().executeSql(
        "insert into subjectattribute (subjectid, name, value, searchvalue) values (?, 'email', ?, ?)",
        GrouperUtil.toListObject(SUBJECT_ID, EMAIL, EMAIL.toLowerCase()));
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Subject subject = SubjectFinder.findByIdAndSource(SUBJECT_ID, "jdbc", true);
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      group.addMember(subject, false);
      // the membership trace page reads the member's point in time row (and fails without it); the
      // change log daemon normally creates it, but the test Tomcat runs with the daemon off
      edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity.convertRecords();
      return group.getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the folder (and group and membership), the member row and the subject rows
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
    String[] sqls = new String[] {
        "delete from grouper_members where subject_id = ? and subject_source = 'jdbc'",
        "delete from subjectattribute where subjectid = ?",
        "delete from subject where subjectid = ?"};
    for (String sql : sqls) {
      try {
        HibernateSession.bySqlStatic().executeSql(sql, GrouperUtil.toListObject(SUBJECT_ID));
      } catch (RuntimeException re) {
        re.printStackTrace();
      }
    }
  }

  /**
   * insert the visualization proof subject in the example jdbc table and add it to its group, so
   * the subject's graph has a subject node, a group node and an edge
   */
  private static void createVisData() {
    HibernateSession.bySqlStatic().executeSql(
        "insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
        GrouperUtil.toListObject(VIS_SUBJECT_ID, VIS_SUBJECT_NAME));
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Subject subject = SubjectFinder.findByIdAndSource(VIS_SUBJECT_ID, "jdbc", true);
      Group group = new GroupSave(rootSession).assignName(VIS_GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      group.addMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the visualization folder, group, member row and subject row
   */
  private static void deleteVisData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, VIS_GROUP_NAME, false);
      if (group != null) {
        group.delete();
      }
      Stem stem = StemFinder.findByName(rootSession, VIS_STEM_NAME, false);
      if (stem != null) {
        stem.delete();
      }
    } catch (RuntimeException re) {
      re.printStackTrace();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    String[] sqls = new String[] {
        "delete from grouper_members where subject_id = ? and subject_source = 'jdbc'",
        "delete from subject where subjectid = ?"};
    for (String sql : sqls) {
      try {
        HibernateSession.bySqlStatic().executeSql(sql, GrouperUtil.toListObject(VIS_SUBJECT_ID));
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
   * a query parameter of a url, url-decoded once (what the server receives)
   * @param url
   * @param name e.g. subjectId
   * @return the value
   * @throws Exception
   */
  private static String param(String url, String name) throws Exception {
    int index = url.indexOf("?" + name + "=");
    if (index < 0) {
      index = url.indexOf("&" + name + "=");
    }
    assertTrue("url has a " + name + ": " + url, index >= 0);
    String value = url.substring(index + name.length() + 2);
    int end = value.indexOf('&');
    if (end >= 0) {
      value = value.substring(0, end);
    }
    end = value.indexOf('\'');
    if (end >= 0) {
      value = value.substring(0, end);
    }
    return URLDecoder.decode(value, "UTF-8");
  }

  /**
   * after a screen rendered: no injected element and no dialog
   * @param page
   * @param dialogs
   * @param screen for messages
   * @throws Exception
   */
  private static void assertNoInjection(Page page, List<String> dialogs, String screen) throws Exception {
    // give a late onerror a moment to fire before checking
    Thread.sleep(1000);
    assertEquals(screen + ": subject id payload must not add an element", 0, page.locator("#secPatch124xss").count());
    assertEquals(screen + ": email payload must not add an element", 0, page.locator("#secPatch124mailxss").count());
    assertTrue(screen + ": no dialog may open: " + dialogs, dialogs.isEmpty());
  }

  // ---------------------------------------------------------------- test

  /**
   * the subject page, the membership edit page and the membership trace page render the proof
   * subject with no injected element and no dialog; the subject page's search url and the trace
   * page's subject link carry the exact subject id once url-decoded; the email shows as text
   * @throws Exception
   */
  public void testSubjectIdAndEmailEscaped() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // a previous aborted run may have left data behind
    deleteData();
    try (Playwright playwright = Playwright.create()) {
      String groupId = createData();
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
      String subjectParams = "&subjectId=" + URLEncoder.encode(SUBJECT_ID, "UTF-8") + "&sourceId=jdbc";

      // 1. the subject page (subjectHeader.jsp, subjectTabs.jsp, subjectContents.jsp, more actions)
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Subject.viewSubject" + subjectParams);
      // the "search for a group" button of the add-to-group dialog, in the DOM on every release
      Locator searchButton = page.locator("[onclick*='UiV2Subject.addGroupSearch'][onclick*='subjectId=secPatch124']");
      searchButton.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
      assertNoInjection(page, dialogs, "viewSubject");
      assertEquals("add group search subject id", SUBJECT_ID, param(searchButton.first().getAttribute("onclick"), "subjectId"));
      assertEquals("add group search source id", "jdbc", param(searchButton.first().getAttribute("onclick"), "sourceId"));

      // 2. the membership edit page (editMembership.jsp): email as text, the subject link escaped
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Membership.editMembership&groupId="
          + groupId + subjectParams + "&field=members");
      // escaped, the email shows as text; unescaped it becomes an img and this wait times out
      page.locator("#grouperMainContentDivId", new Page.LocatorOptions().setHasText(EMAIL)).waitFor();
      assertNoInjection(page, dialogs, "editMembership");
      Locator editSubjectLink = page.locator("[onclick*='UiV2Subject.viewSubject'][onclick*='subjectId=secPatch124']");
      if (editSubjectLink.count() > 0) {
        assertEquals("edit membership subject link id", SUBJECT_ID, param(editSubjectLink.first().getAttribute("onclick"), "subjectId"));
      }

      // 3. the membership trace page (traceMembership.jsp), opened from the subject (backTo=subject):
      // only then does it render the subject breadcrumb and the "back to subject" link
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Membership.traceMembership&groupId="
          + groupId + subjectParams + "&field=members&backTo=subject");
      Locator traceSubjectLink = page.locator("[onclick*='UiV2Subject.viewSubject'][onclick*='subjectId=secPatch124']");
      traceSubjectLink.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
      assertNoInjection(page, dialogs, "traceMembership");
      assertEquals("trace subject link id", SUBJECT_ID, param(traceSubjectLink.first().getAttribute("onclick"), "subjectId"));
      String traceHref = traceSubjectLink.first().getAttribute("href");
      // 4.26.0 links are href="#" (the url is only in onclick, checked above)
      if (traceHref != null && traceHref.contains("subjectId=")) {
        assertEquals("trace subject href id", SUBJECT_ID, param(traceHref, "subjectId"));
      }

      browser.close();
      System.out.println("^secPatch124 subject pages checked: viewSubject, editMembership, traceMembership");
    } finally {
      deleteData();
    }
  }

  // ---------------------------------------------------------------- visualization

  /**
   * pick the draw module on the visualization form (the radio sits in the collapsed settings
   * panel, so set it directly) and press Generate
   * @param page
   * @param module "text" or "d3"
   */
  private static void drawVisualization(Page page, String module) {
    page.evaluate("m => { document.getElementById('vis-settings-module-' + m).checked = true; fetchGraph(); }", module);
  }

  /**
   * after a visualization rendered: no injected element, no dialog, and the payload statement
   * (document.title=...) did not run
   * @param page
   * @param dialogs
   * @param screen for messages
   * @throws Exception
   */
  private static void assertVisNoInjection(Page page, List<String> dialogs, String screen) throws Exception {
    // give a late onerror a moment to fire before checking
    Thread.sleep(1000);
    assertEquals(screen + ": subject id payload must not add an element", 0, page.locator("#secPatch124visxss").count());
    assertTrue(screen + ": no dialog may open: " + dialogs, dialogs.isEmpty());
    assertFalse(screen + ": subject id payload statement must not run", "secPatch124VIS".equals(page.title()));
    // no link may be a javascript: url (the old code built them from the node id)
    Number javascriptLinks = (Number) page.evaluate("() => Array.prototype.slice.call("
        + "document.querySelectorAll('#vis-graph-text [href], #vis-graph-svg-pane svg a')).filter(function(a) {"
        + " var h = a.getAttribute('href') || a.getAttribute('xlink:href')"
        + " || (a.getAttributeNS ? a.getAttributeNS('http://www.w3.org/1999/xlink', 'href') : null) || '';"
        + " return /^\\s*javascript:/i.test(h); }).length");
    assertEquals(screen + ": no javascript: links", 0, javascriptLinks.intValue());
  }

  /**
   * what the clicks followed (see the followObject stub): at least one subject, and every subject
   * followed with the exact node id (type subject, sourceId||||subjectId)
   * @param page
   * @param screen for messages
   */
  private static void assertVisFollowed(Page page, String screen) {
    @SuppressWarnings("unchecked")
    List<Object> followed = (List<Object>) page.evaluate("() => window.secPatch124Followed");
    boolean subjectFollowed = false;
    for (Object one : GrouperUtil.nonNull(followed)) {
      String entry = String.valueOf(one);
      if (entry.startsWith("subject|")) {
        assertEquals(screen + ": the subject link follows the exact subject", "subject|" + VIS_NODE_ID, entry);
        subjectFollowed = true;
      }
    }
    assertTrue(screen + ": clicking the links followed the subject: " + followed, subjectFollowed);
    assertFalse(screen + ": subject id payload statement must not run on click", "secPatch124VIS".equals(page.title()));
  }

  /**
   * the subject visualization (grouperVisualization.js), text and graph views, for a subject whose
   * id holds quotes, a backslash and markup: nothing is injected, no link is a javascript: url,
   * the DOT source does not contain the id, and clicking the subject's link or graph node follows
   * the exact subject (followObject is stubbed to record instead of leaving the page)
   * @throws Exception
   */
  public void testVisualizationSubjectIdInert() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // a previous aborted run may have left data behind
    deleteVisData();
    try (Playwright playwright = Playwright.create()) {
      createVisData();
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newContext().newPage();
      page.setDefaultTimeout(15000);

      final List<String> dialogs = new ArrayList<String>();
      page.onDialog(dialog -> {
        dialogs.add(dialog.message());
        dialog.dismiss();
      });

      // uncaught script errors on the page (4.26.0's stock text view throws, see below)
      final List<String> pageErrors = new ArrayList<String>();
      page.onPageError(error -> pageErrors.add(String.valueOf(error)));

      String baseUrl = uiBaseUrl();
      login(page, baseUrl);

      // the subject's More actions -> Visualization screen (visualizationMain.jsp)
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Visualization.subjectView&subjectId="
          + URLEncoder.encode(VIS_SUBJECT_ID, "UTF-8") + "&sourceId=jdbc");
      page.locator("#vis-settings-module-text").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));

      // generating a graph saves the draw module as this user's preference: put it back at the end
      boolean wasD3 = Boolean.TRUE.equals(page.evaluate("() => document.getElementById('vis-settings-module-d3').checked"));
      try {
        // record what a click follows instead of submitting the form and leaving the page
        page.evaluate("() => { window.secPatch124Followed = []; "
            + "window.followObject = function(objectType, objectId) { window.secPatch124Followed.push(objectType + '|' + objectId); }; }");

        // 1. text view: the subject and group links
        drawVisualization(page, "text");
        boolean textViewRendered = true;
        try {
          page.locator("#vis-graph-text .visualization-text-node").first().waitFor(
              new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(30000));
        } catch (com.microsoft.playwright.TimeoutError te) {
          // stock 4.26.0: drawGraphModuleText checks node.compositeLeftFactorId !== "" but the
          // server leaves it undefined, so it looks up a missing node and throws reading
          // displayExtension; the text view never renders there, with or without this fix
          if (!pageErrors.toString().contains("displayExtension")) {
            throw te;
          }
          textViewRendered = false;
        }
        if (textViewRendered) {
          assertVisNoInjection(page, dialogs, "visualization text");
          Locator textLinks = page.locator("#vis-graph-text [data-grouper-vis-node]");
          assertTrue("visualization text: the objects are links", textLinks.count() > 0);
          for (int i = 0; i < textLinks.count(); i++) {
            // fire the click on the element itself: some links have no text (zero size), which a
            // real mouse click waits on forever; the test is about what the handler follows
            textLinks.nth(i).dispatchEvent("click");
          }
          assertVisFollowed(page, "visualization text");
        }

        // 2. graph view: the svg nodes, and the DOT source the page keeps for "copy .dot"
        page.evaluate("() => { window.secPatch124Followed = []; }");
        drawVisualization(page, "d3");
        page.locator("#vis-graph-svg-pane svg .node").first().waitFor(
            new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(30000));
        assertVisNoInjection(page, dialogs, "visualization graph");
        String dot = String.valueOf(page.evaluate("() => visualizationDotOutput"));
        assertFalse("visualization graph: the DOT source must not contain the subject id: " + dot,
            dot.contains("secPatch124vis'"));
        Locator svgLinks = page.locator("#vis-graph-svg-pane svg a");
        assertTrue("visualization graph: the nodes are links", svgLinks.count() > 0);
        for (int i = 0; i < svgLinks.count(); i++) {
          // a dispatched click: the zoomed graph can put nodes under each other or off screen
          svgLinks.nth(i).dispatchEvent("click");
        }
        assertVisFollowed(page, "visualization graph");
      } finally {
        try {
          drawVisualization(page, wasD3 ? "d3" : "text");
          Thread.sleep(2000);
        } catch (RuntimeException re) {
          re.printStackTrace();
        }
      }

      browser.close();
      System.out.println("^secPatch124 visualization checked: text and graph views");
    } finally {
      deleteVisData();
    }
  }

}
