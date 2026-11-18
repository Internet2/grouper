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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.CompositeType;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M6 (deprovisioningAuthorization), HTTP check against the running local UI:
 * UiV2Deprovisioning.deprovisionUserDeprovisionSubmit is bound to the rows the results page
 * offered.  GrouperDeprovisioningAllowedAccessTest calls GrouperDeprovisioningAllowedAccess
 * directly; this test covers what it cannot: the real UI handler wiring (refusal, skipped and
 * success messages, the navigation to recently deprovisioned users, the add to the deprovisioned
 * users group as a non-wheel manager), checked against the database rows the running UI wrote.</p>
 *
 * <p>The attack: a non-wheel deprovisioning manager opens the results page for user A, then
 * submits it with one checkbox value changed to another person's membership uuid.  The whole
 * request is refused and nothing changes.  The positive control: the same manager submits the
 * offered rows unmodified; the plain row is removed, the excludes-group row is skipped with an
 * info message, and A is listed as recently deprovisioned.</p>
 *
 * <p>Setup (this test's own, removed at the end): affiliation secPatch09 (appended to
 * deprovisioning.affiliations in database config, restored in finally) with its managers group
 * (test.subject.0, not wheel locally) and deprovisioned users group under etc:deprovisioning; jdbc
 * subjects secPatch09u1 (deprovisioned) and secPatch09u2 (bystander); folder secPatch09 with g1
 * (u1), other (u2), and policy = includes minus excludes (u1 in excludes).</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class UiV2DeprovisionUserHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch09";

  /** affiliation this test adds to deprovisioning.affiliations */
  private static final String AFFILIATION = "secPatch09";

  /** the deprovisioning manager (not wheel locally) */
  private static final String MANAGER_USER = "test.subject.0";

  /** subject being deprovisioned (jdbc row this test inserts) */
  private static final String USER1 = "secPatch09u1";

  /** bystander whose membership uuid is forged into the request (jdbc row this test inserts) */
  private static final String USER2 = "secPatch09u2";

  /** config key */
  private static final String AFFILIATIONS_KEY = "deprovisioning.affiliations";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2DeprovisionUserHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2DeprovisionUserHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2DeprovisionUserHttpTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- http helpers

  /**
   * base url of the running UI: grouper.ui.url if set, else the junit.test.tomcat.* keys
   * @return e.g. http://localhost:8080/grouper
   */
  static String uiBaseUrl() {
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

  /**
   * one browser-like session: its own cookie jar, no redirect following, CSRF token after login
   */
  static class UiSession {

    /** this session's cookies */
    private final SimpleCookieJar cookieJar = new SimpleCookieJar();

    /** e.g. http://localhost:8080/grouper */
    private final String baseUrl;

    /** OWASP CSRF master token of this session */
    private String csrfToken;

    /** "Basic ..." header sent on every request when the release has no form login (2.6.19), else null */
    private String basicAuthorization;

    /**
     * @param theBaseUrl
     */
    UiSession(String theBaseUrl) {
      this.baseUrl = theBaseUrl;
    }

    /**
     * form login (dev only), then read the CSRF token
     * @param username
     * @param password
     * @throws Exception
     */
    void login(String username, String password) throws Exception {
      String contextPath = URI.create(this.baseUrl).getPath();
      String body = "grouperFormLoginUsername=" + enc(username)
          + "&grouperFormLoginPassword=" + enc(password)
          + "&grouperFormLoginTarget=" + enc(contextPath + "/grouperUi/app/UiV2Main.index");
      SimpleHttpResponse response = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/UiV2Main.index"))
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(SimpleHttpRequest.ofString(body)));
      // a good login is a post/redirect/get; a bad one shows the form again (200)
      if (response.statusCode() != 302) {
        // 2.6.19 has no dev form login (GrouperUiFormLogin is 4.x+): its local UI uses HTTP basic
        // auth (grouper.is.ui.basicAuthn=true, UI rows in grouper_password), so the POST above got
        // a 401.  Send the credentials on every request of this session instead.
        this.basicAuthorization = "Basic " + java.util.Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        SimpleHttpResponse check = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/UiV2Main.index")).GET());
        assertTrue("login of " + username + " (form login: " + response.statusCode() + " " + response.body()
            + "; basic auth: " + check.statusCode() + ")",
            check.statusCode() == 200 && !check.body().contains("grouperFormLoginUsername"));
      }

      SimpleHttpResponse csrfJs = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperExternal/public/OwaspJavaScriptServlet")).GET());
      Matcher matcher = Pattern.compile("masterTokenValue = '([^']+)'").matcher(csrfJs.body());
      assertTrue("CSRF token for " + username, matcher.find());
      this.csrfToken = matcher.group(1);
    }

    /**
     * ajax POST with the CSRF header and a form body, like the UI's own requests
     * @param path after the base url
     * @param formBody urlencoded body
     * @return the response
     * @throws Exception
     */
    SimpleHttpResponse postAjax(String path, String formBody) throws Exception {
      return send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + path))
          .header("X-Requested-With", "XMLHttpRequest")
          .header("OWASPCSRFTOKEN", this.csrfToken)
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(SimpleHttpRequest.ofString(formBody)));
    }

    /**
     * @param builder
     * @return the response, 15 second timeout
     * @throws Exception
     */
    private SimpleHttpResponse send(SimpleHttpRequest builder) throws Exception {
      if (this.basicAuthorization != null) {
        builder.header("Authorization", this.basicAuthorization);
      }
      return builder.send(this.cookieJar, 15000);
    }
  }

  /**
   * @param value
   * @return url encoded
   */
  static String enc(String value) {
    try {
      // the String charset name overload: URLEncoder.encode(String, Charset) is Java 10+
      return URLEncoder.encode(value, "UTF-8");
    } catch (java.io.UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
  }

  /**
   * the ajax response is json with html inside: undo the json escaping so html can be matched
   * @param body
   * @return body with \" \/ \n and \\uXXXX decoded
   */
  static String unjson(String body) {
    if (body == null) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < body.length(); i++) {
      char c = body.charAt(i);
      if (c == '\\' && i + 1 < body.length()) {
        char next = body.charAt(i + 1);
        if (next == 'u' && i + 5 < body.length()) {
          result.append((char)Integer.parseInt(body.substring(i + 2, i + 6), 16));
          i += 5;
          continue;
        }
        if (next == 'n') {
          result.append('\n');
        } else if (next == 't') {
          result.append('\t');
        } else if (next == 'r') {
          result.append('\r');
        } else {
          result.append(next);
        }
        i++;
        continue;
      }
      result.append(c);
    }
    return result.toString();
  }

  /**
   * @param response
   * @return status and the start of the (decoded) body, for failure messages
   */
  static String describe(SimpleHttpResponse response) {
    String body = unjson(response.body());
    // the ajax json starts with the logged in user's settings and ends with screens: show the
    // messages (errors) and the end of the body, not the settings at the start
    java.util.List<String> messages = new java.util.ArrayList<String>();
    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"message\":\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(body);
    while (matcher.find()) {
      messages.add(matcher.group(1));
    }
    return response.statusCode() + " messages: " + messages + " body: "
        + (body.length() <= 1500 ? body : body.substring(body.length() - 1500));
  }

  // ---------------------------------------------------------------- db helpers

  /**
   * @param values
   * @return list of the values
   */
  static List<Object> listOf(Object... values) {
    return new ArrayList<Object>(Arrays.asList(values));
  }

  /**
   * @param groupName
   * @param subjectId
   * @return number of enabled immediate members-list rows of the subject in the group (SQL)
   */
  private static int immediateMemberCount(String groupName, String subjectId) {
    return new GcDbAccess().sql("select count(*) from grouper_memberships ms, grouper_fields f, grouper_members m, grouper_groups g "
        + "where ms.field_id = f.id and ms.member_id = m.id and ms.owner_group_id = g.id and g.name = ? and f.name = 'members' "
        + "and m.subject_id = ? and ms.mship_type = 'immediate' and ms.enabled = 'T'")
        .addBindVar(groupName).addBindVar(subjectId).select(Integer.class);
  }

  /**
   * @return number of deprovisioning audit rows for the affiliation (SQL)
   */
  private static int deprovisionAuditCount() {
    return new GcDbAccess().sql("select count(*) from grouper_audit_entry where description like ?")
        .addBindVar("Deprovisioned user: %" + USER1 + "% from affiliation: " + AFFILIATION + ",%").select(Integer.class);
  }

  /**
   * @return current database value of deprovisioning.affiliations, or null if there is no row
   */
  private static String dbAffiliations() {
    List<String> values = new GcDbAccess().sql("select config_value from grouper_config where config_file_name = 'grouper.properties' "
        + "and config_key = ?").addBindVar(AFFILIATIONS_KEY).selectList(String.class);
    return values.size() == 0 ? null : values.get(0);
  }

  /**
   * set or remove deprovisioning.affiliations in database config (the UI sees it within seconds)
   * @param value null to delete the row
   */
  private static void storeAffiliations(String value) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      if (value == null) {
        new GrouperDbConfig().configFileName("grouper.properties").propertyName(AFFILIATIONS_KEY).delete();
      } else {
        new GrouperDbConfig().configFileName("grouper.properties").propertyName(AFFILIATIONS_KEY).value(value).store();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @return deprovisioning system folder, e.g. etc:deprovisioning
   */
  private static String deprovisioningStem() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":deprovisioning";
  }

  /**
   * @return managers group name of this test's affiliation
   */
  private static String managersGroupName() {
    return deprovisioningStem() + ":managersWhoCanDeprovision_" + AFFILIATION;
  }

  /**
   * @return deprovisioned users group name of this test's affiliation
   */
  private static String deprovisionedGroupName() {
    return deprovisioningStem() + ":usersWhoHaveBeenDeprovisioned_" + AFFILIATION;
  }

  /**
   * insert a jdbc subject like test.subject.N
   * @param subjectId
   */
  private static void insertSubject(String subjectId) {
    HibernateSession.bySqlStatic().executeSql("insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
        listOf(subjectId, "my name is " + subjectId));
    String[][] attributes = {{"name", "name." + subjectId}, {"loginid", "id." + subjectId},
        {"description", "description." + subjectId}, {"email", subjectId + "@example.edu"}};
    for (String[] attribute : attributes) {
      HibernateSession.bySqlStatic().executeSql("insert into subjectattribute (subjectid, name, value, searchvalue) values (?, ?, ?, ?)",
          listOf(subjectId, attribute[0], attribute[1], attribute[1].toLowerCase()));
    }
  }

  /**
   * delete everything this test made (not the config, see storeAffiliations).  The affiliation
   * groups must exist whenever any JVM has the affiliation configured (the membership veto hook
   * looks them up on every add and fails the add if they are missing), so they are only deleted
   * once the config is gone (deleteAffiliationGroups true)
   * @param deleteAffiliationGroups
   */
  private static void cleanUp09(boolean deleteAffiliationGroups) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      // the composite first, then the other groups and the folder
      for (String extension : new String[] {"policy", "includes", "excludes", "g1", "other"}) {
        Group group = GroupFinder.findByName(rootSession, STEM_NAME + ":" + extension, false);
        if (group != null) {
          group.delete();
        }
      }
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.delete();
      }
      // the affiliation groups exist only for this test's affiliation
      for (String groupName : new String[] {managersGroupName(), deprovisionedGroupName()}) {
        Group group = GroupFinder.findByName(rootSession, groupName, false);
        if (group == null) {
          continue;
        }
        if (deleteAffiliationGroups) {
          group.delete();
        } else {
          // empty it, keep the group
          for (Member member : group.getImmediateMembers()) {
            group.deleteMember(member, false);
          }
        }
      }
      new GcDbAccess().sql("delete from grouper_audit_entry where description like ?")
          .addBindVar("Deprovisioned user: %" + USER1 + "% from affiliation: " + AFFILIATION + ",%").executeSql();
      for (String subjectId : new String[] {USER1, USER2}) {
        HibernateSession.bySqlStatic().executeSql("delete from subjectattribute where subjectid = ?", listOf(subjectId));
        HibernateSession.bySqlStatic().executeSql("delete from subject where subjectid = ?", listOf(subjectId));
        List<String> memberIds = new GcDbAccess().sql("select id from grouper_members where subject_id = ? and subject_source = 'jdbc'")
            .addBindVar(subjectId).selectList(String.class);
        for (String memberId : memberIds) {
          try {
            HibernateSession.bySqlStatic().executeSql("delete from grouper_members where id = ?", listOf(memberId));
          } catch (RuntimeException re) {
            System.out.println("secPatch09: member row " + memberId + " still referenced, left in place: " + re.getMessage());
          }
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- page parsing

  /**
   * @param html decoded results page
   * @return checkbox name (membershipRow_N) to value (membership uuid), in page order
   */
  private static Map<String, String> membershipRows(String html) {
    Map<String, String> result = new LinkedHashMap<String, String>();
    Matcher matcher = Pattern.compile("name=\"(membershipRow_\\d+)\"[^>]*?value=\"([^\"]*)\"", Pattern.DOTALL).matcher(html);
    while (matcher.find()) {
      result.put(matcher.group(1), matcher.group(2));
    }
    return result;
  }

  /**
   * @param html
   * @return the memberId hidden field
   */
  private static String hiddenMemberId(String html) {
    Matcher matcher = Pattern.compile("name=\"memberId\" value=\"([^\"]*)\"").matcher(html);
    assertTrue("memberId hidden field on the results page", matcher.find());
    return matcher.group(1);
  }

  /**
   * submit body: affiliation, memberId and the given checkbox name/value pairs, as the ajax form
   * serializer sends them (checkbox names get [] appended)
   * @param memberId
   * @param rows checkbox name to value
   * @return urlencoded body
   */
  private static String submitBody(String memberId, Map<String, String> rows) {
    StringBuilder body = new StringBuilder("affiliation=" + enc(AFFILIATION) + "&memberId=" + enc(memberId));
    for (Map.Entry<String, String> row : rows.entrySet()) {
      body.append("&").append(enc(row.getKey() + "[]")).append("=").append(enc(row.getValue()));
    }
    return body.toString();
  }

  // ---------------------------------------------------------------- test

  /**
   * forged membership uuid refused (nothing changed); unmodified submit removes the plain row,
   * skips the excludes row, and lists the user as deprovisioned
   * @throws Exception
   */
  public void testDeprovisionUserSubmitBoundToOfferedRows() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    String priorAffiliations = dbAffiliations();
    boolean configStored = false;
    assertTrue("a previous run left " + AFFILIATION + " in " + AFFILIATIONS_KEY + ": " + priorAffiliations,
        priorAffiliations == null || !priorAffiliations.contains(AFFILIATION));
    cleanUp09(false);
    UiSession manager = null;
    try {
      // ---- setup: the affiliation groups first (they must exist before any JVM sees the config)
      String g1MembershipId;
      String excludesMembershipId;
      String otherMembershipId;
      String user1MemberId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group managers = new GroupSave(rootSession).assignName(managersGroupName()).assignCreateParentStemsIfNotExist(true).save();
        new GroupSave(rootSession).assignName(deprovisionedGroupName()).assignCreateParentStemsIfNotExist(true).save();

        String newAffiliations = (priorAffiliations == null || priorAffiliations.trim().length() == 0)
            ? AFFILIATION : priorAffiliations + "," + AFFILIATION;
        storeAffiliations(newAffiliations);
        configStored = true;

        insertSubject(USER1);
        insertSubject(USER2);
        Subject user1 = SubjectFinder.findByIdAndSource(USER1, "jdbc", true);
        Subject user2 = SubjectFinder.findByIdAndSource(USER2, "jdbc", true);
        user1MemberId = MemberFinder.findBySubject(rootSession, user1, true).getId();

        managers.addMember(SubjectFinder.findById(MANAGER_USER, true), false);

        Group g1 = new GroupSave(rootSession).assignName(STEM_NAME + ":g1").assignCreateParentStemsIfNotExist(true).save();
        g1.addMember(user1, false);
        Group other = new GroupSave(rootSession).assignName(STEM_NAME + ":other").assignCreateParentStemsIfNotExist(true).save();
        other.addMember(user2, false);
        Group includes = new GroupSave(rootSession).assignName(STEM_NAME + ":includes").assignCreateParentStemsIfNotExist(true).save();
        Group excludes = new GroupSave(rootSession).assignName(STEM_NAME + ":excludes").assignCreateParentStemsIfNotExist(true).save();
        excludes.addMember(user1, false);
        Group policy = new GroupSave(rootSession).assignName(STEM_NAME + ":policy").assignCreateParentStemsIfNotExist(true).save();
        policy.addCompositeMember(CompositeType.COMPLEMENT, includes, excludes);

        g1MembershipId = MembershipFinder.findImmediateMembership(rootSession, g1, user1, true).getUuid();
        excludesMembershipId = MembershipFinder.findImmediateMembership(rootSession, excludes, user1, true).getUuid();
        otherMembershipId = MembershipFinder.findImmediateMembership(rootSession, other, user2, true).getUuid();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      manager = new UiSession(baseUrl);
      manager.login(MANAGER_USER, "pass");

      // ---- the results page for user1 (retry while the UI picks up the affiliation config)
      String searchBody = "affiliation=" + enc(AFFILIATION) + "&groupAddMemberComboName=" + enc("jdbc||" + USER1);
      String resultsHtml = null;
      SimpleHttpResponse results = null;
      for (int i = 0; i < 20; i++) {
        results = manager.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisionUserSubmit", searchBody);
        resultsHtml = unjson(results.body());
        if (resultsHtml.contains("deprovisionUserFormId")) {
          break;
        }
        Thread.sleep(3000);
      }
      assertTrue("results page for " + USER1 + ": " + describe(results), resultsHtml.contains("deprovisionUserFormId"));
      assertEquals("results page is for user1", user1MemberId, hiddenMemberId(resultsHtml));
      Map<String, String> offered = membershipRows(resultsHtml);
      assertTrue("g1 row offered: " + offered, offered.containsValue(g1MembershipId));
      assertTrue("excludes row offered: " + offered, offered.containsValue(excludesMembershipId));
      assertFalse("bystander row not offered: " + offered, offered.containsValue(otherMembershipId));

      // only this test's two rows are submitted (user1 has no other rows, but keep it exact)
      Map<String, String> ownRows = new LinkedHashMap<String, String>();
      for (Map.Entry<String, String> row : offered.entrySet()) {
        if (row.getValue().equals(g1MembershipId) || row.getValue().equals(excludesMembershipId)) {
          ownRows.put(row.getKey(), row.getValue());
        }
      }
      assertEquals("two own rows: " + offered, 2, ownRows.size());

      // ---- attack: the g1 checkbox kept, the excludes checkbox value changed to the bystander's membership uuid
      Map<String, String> forged = new LinkedHashMap<String, String>();
      for (Map.Entry<String, String> row : ownRows.entrySet()) {
        forged.put(row.getKey(), row.getValue().equals(excludesMembershipId) ? otherMembershipId : row.getValue());
      }
      SimpleHttpResponse attack = manager.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisionUserDeprovisionSubmit",
          submitBody(user1MemberId, forged));
      String attackHtml = unjson(attack.body());
      assertTrue("forged submit shows the refusal: " + describe(attack), attackHtml.contains("\"messageType\":\"error\"")
          && attackHtml.contains("this screen did not offer") && attackHtml.contains("Nothing was changed"));
      assertFalse("forged submit does not navigate: " + describe(attack), attackHtml.contains("viewRecentlyDeprovisionedUsers"));
      assertEquals("bystander still in other", 1, immediateMemberCount(STEM_NAME + ":other", USER2));
      assertEquals("user1 still in g1", 1, immediateMemberCount(STEM_NAME + ":g1", USER1));
      assertEquals("user1 still in excludes", 1, immediateMemberCount(STEM_NAME + ":excludes", USER1));
      assertEquals("user1 not deprovisioned", 0, immediateMemberCount(deprovisionedGroupName(), USER1));
      assertEquals("no deprovisioning audit", 0, deprovisionAuditCount());

      // ---- positive control: the offered rows, unmodified
      SimpleHttpResponse submit = manager.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisionUserDeprovisionSubmit",
          submitBody(user1MemberId, ownRows));
      String submitHtml = unjson(submit.body());
      assertTrue("unmodified submit succeeds: " + describe(submit), submitHtml.contains("\"messageType\":\"success\"")
          && submitHtml.contains("the user was deprovisioned"));
      assertTrue("excludes row reported as skipped: " + describe(submit), submitHtml.contains("\"messageType\":\"info\"")
          && submitHtml.contains(STEM_NAME + ":excludes (excludeOrDenyGroup)"));
      assertTrue("navigates to recently deprovisioned users: " + describe(submit),
          submitHtml.contains("UiV2Deprovisioning.viewRecentlyDeprovisionedUsers&affiliation=" + AFFILIATION));
      assertEquals("user1 removed from g1", 0, immediateMemberCount(STEM_NAME + ":g1", USER1));
      assertEquals("user1 kept in excludes (guardrail)", 1, immediateMemberCount(STEM_NAME + ":excludes", USER1));
      assertEquals("bystander still in other", 1, immediateMemberCount(STEM_NAME + ":other", USER2));
      assertEquals("user1 added to the deprovisioned users group by a non-wheel manager", 1,
          immediateMemberCount(deprovisionedGroupName(), USER1));
      assertEquals("one deprovisioning audit", 1, deprovisionAuditCount());

      // ---- recently deprovisioned users lists user1
      SimpleHttpResponse recent = manager.postAjax("/grouperUi/app/UiV2Deprovisioning.viewRecentlyDeprovisionedUsers",
          "affiliation=" + enc(AFFILIATION));
      String recentHtml = unjson(recent.body());
      assertTrue("recently deprovisioned screen: " + describe(recent), recentHtml.contains("grouperMainContentDivId"));
      assertTrue("user1 listed as recently deprovisioned: " + describe(recent), recentHtml.contains(USER1));

    } finally {
      // fixtures first, then the config, then (once the UI no longer has the affiliation) the
      // affiliation groups, the audit rows and the subjects
      cleanUp09(false);
      if (configStored) {
        storeAffiliations(priorAffiliations);
        waitUntilUiDropsAffiliation(manager);
      }
      cleanUp09(true);
    }
  }

  /**
   * after the config is restored, wait until the UI no longer offers the affiliation (up to 90 s),
   * then 30 s more for other JVMs, so no JVM looks up the affiliation groups after they are deleted
   * @param manager logged in session, or null if the test failed before login
   * @throws Exception
   */
  private static void waitUntilUiDropsAffiliation(UiSession manager) throws Exception {
    if (manager != null) {
      String body = "affiliation=" + enc(AFFILIATION) + "&groupAddMemberComboName=" + enc(MANAGER_USER);
      for (int i = 0; i < 30; i++) {
        String html = unjson(manager.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisionUserSubmit", body).body());
        // an unknown affiliation shows the select-a-valid-affiliation error, or not-configured when no affiliation is
        // left (a known one shows results or a not-manager error).  2.6.19 has no not-configured text: with no
        // affiliation left it throws "Deprovisioning is disabled" (shown in the generic error); with others left
        // it throws not-allowed for a non-manager, so there this wait just runs out
        if (html.contains("Please select a valid affiliation") || html.contains("Deprovisioning is not configured")
            || html.contains("Deprovisioning is disabled")) {
          break;
        }
        Thread.sleep(3000);
      }
    }
    Thread.sleep(30000);
  }

  // ---------------------------------------------------------------- http (Java 8)

  /**
   * Minimal HTTP request on HttpURLConnection, shaped like the java.net.http builder calls this
   * test was first written with.  java.net.http is Java 11+, and this one test file has to compile
   * on every release it ships for, including 2.6.19 (Java 8).  Redirects are not followed; cookies
   * are kept by hand in a per-session jar (like a browser, one jar per logged in user).
   */
  static class SimpleHttpRequest {

    /** where to send it */
    private final java.net.URI uri;

    /** request headers, in order: {name, value} */
    private final java.util.List<String[]> headers = new java.util.ArrayList<String[]>();

    /** GET or POST */
    private String method = "GET";

    /** POST body, null for GET */
    private byte[] body;

    /**
     * @param theUri
     */
    private SimpleHttpRequest(java.net.URI theUri) {
      this.uri = theUri;
    }

    /**
     * @param theUri
     * @return a GET request to the uri until POST() is called
     */
    static SimpleHttpRequest newBuilder(java.net.URI theUri) {
      return new SimpleHttpRequest(theUri);
    }

    /**
     * @param text
     * @return the UTF-8 bytes, for POST()
     */
    static byte[] ofString(String text) {
      return text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * @param bytes
     * @return the same bytes, for POST()
     */
    static byte[] ofByteArray(byte[] bytes) {
      return bytes;
    }

    /**
     * @param name
     * @param value
     * @return this
     */
    SimpleHttpRequest header(String name, String value) {
      this.headers.add(new String[] {name, value});
      return this;
    }

    /**
     * @return this, as a GET
     */
    SimpleHttpRequest GET() {
      this.method = "GET";
      this.body = null;
      return this;
    }

    /**
     * @param theBody
     * @return this, as a POST of the body
     */
    SimpleHttpRequest POST(byte[] theBody) {
      this.method = "POST";
      this.body = theBody;
      return this;
    }

    /**
     * send it with the session's cookies, store the cookies the response sets
     * @param cookieJar
     * @param timeoutMillis connect and read timeout
     * @return the response (any status, the body decoded as UTF-8)
     * @throws java.io.IOException
     */
    SimpleHttpResponse send(SimpleCookieJar cookieJar, int timeoutMillis) throws java.io.IOException {
      java.net.HttpURLConnection connection = (java.net.HttpURLConnection)this.uri.toURL().openConnection();
      try {
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setRequestMethod(this.method);
        // HttpURLConnection sends a browser-ish Accept by default; java.net.http sent none
        connection.setRequestProperty("Accept", "*/*");
        for (String[] header : this.headers) {
          connection.addRequestProperty(header[0], header[1]);
        }
        String path = this.uri.getRawPath() == null || this.uri.getRawPath().isEmpty() ? "/" : this.uri.getRawPath();
        String cookieHeader = cookieJar.cookieHeader(path);
        if (cookieHeader != null) {
          connection.setRequestProperty("Cookie", cookieHeader);
        }
        if (this.body != null) {
          connection.setDoOutput(true);
          java.io.OutputStream outputStream = connection.getOutputStream();
          try {
            outputStream.write(this.body);
          } finally {
            outputStream.close();
          }
        }
        int status = connection.getResponseCode();
        java.util.Map<String, java.util.List<String>> responseHeaders = new java.util.LinkedHashMap<String, java.util.List<String>>();
        for (java.util.Map.Entry<String, java.util.List<String>> entry : connection.getHeaderFields().entrySet()) {
          // the null key is the status line
          if (entry.getKey() != null) {
            responseHeaders.put(entry.getKey(), entry.getValue());
          }
        }
        SimpleHttpHeaders simpleHeaders = new SimpleHttpHeaders(responseHeaders);
        cookieJar.store(path, simpleHeaders.allValues("Set-Cookie"));
        // 4xx/5xx bodies come from the error stream
        java.io.InputStream inputStream = null;
        try {
          inputStream = connection.getInputStream();
        } catch (java.io.IOException ioe) {
          inputStream = connection.getErrorStream();
        }
        String responseBody = "";
        if (inputStream != null) {
          try {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
              bytes.write(buffer, 0, read);
            }
            responseBody = new String(bytes.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
          } finally {
            inputStream.close();
          }
        }
        return new SimpleHttpResponse(status, responseBody, simpleHeaders);
      } finally {
        connection.disconnect();
      }
    }
  }

  /**
   * status, body and headers of one response
   */
  static class SimpleHttpResponse {

    /** http status */
    private final int statusCode;

    /** body as UTF-8 text, never null */
    private final String body;

    /** response headers */
    private final SimpleHttpHeaders headers;

    /**
     * @param theStatusCode
     * @param theBody
     * @param theHeaders
     */
    SimpleHttpResponse(int theStatusCode, String theBody, SimpleHttpHeaders theHeaders) {
      this.statusCode = theStatusCode;
      this.body = theBody;
      this.headers = theHeaders;
    }

    /**
     * @return http status
     */
    int statusCode() {
      return this.statusCode;
    }

    /**
     * @return body text
     */
    String body() {
      return this.body;
    }

    /**
     * @return response headers
     */
    SimpleHttpHeaders headers() {
      return this.headers;
    }
  }

  /**
   * response headers, names matched case-insensitively (like java.net.http.HttpHeaders)
   */
  static class SimpleHttpHeaders {

    /** header name to values, as the server sent them */
    private final java.util.Map<String, java.util.List<String>> map;

    /**
     * @param theMap
     */
    SimpleHttpHeaders(java.util.Map<String, java.util.List<String>> theMap) {
      this.map = theMap;
    }

    /**
     * @return all headers
     */
    java.util.Map<String, java.util.List<String>> map() {
      return this.map;
    }

    /**
     * @param name
     * @return every value of the header, empty if none
     */
    java.util.List<String> allValues(String name) {
      java.util.List<String> result = new java.util.ArrayList<String>();
      for (java.util.Map.Entry<String, java.util.List<String>> entry : this.map.entrySet()) {
        if (entry.getKey().equalsIgnoreCase(name) && entry.getValue() != null) {
          result.addAll(entry.getValue());
        }
      }
      return result;
    }

    /**
     * @param name
     * @return the first value of the header
     */
    java.util.Optional<String> firstValue(String name) {
      java.util.List<String> values = allValues(name);
      return values.isEmpty() ? java.util.Optional.<String>empty() : java.util.Optional.ofNullable(values.get(0));
    }
  }

  /**
   * cookies of one session (one host), by path and name, like java.net.CookieManager does for these
   * requests: a cookie goes back on requests under its path; Max-Age=0 or an empty value removes it
   */
  static class SimpleCookieJar {

    /** path + " " + name (a cookie name has no spaces) to {name, value, path} */
    private final java.util.Map<String, String[]> cookies = new java.util.LinkedHashMap<String, String[]>();

    /**
     * @param requestPath
     * @return the Cookie header for a request to the path, or null if no cookie applies
     */
    synchronized String cookieHeader(String requestPath) {
      StringBuilder result = new StringBuilder();
      for (String[] cookie : this.cookies.values()) {
        String cookiePath = cookie[2];
        boolean matches = requestPath.equals(cookiePath) || (requestPath.startsWith(cookiePath)
            && (cookiePath.endsWith("/") || requestPath.charAt(cookiePath.length()) == '/'));
        if (matches) {
          if (result.length() > 0) {
            result.append("; ");
          }
          result.append(cookie[0]).append("=").append(cookie[1]);
        }
      }
      return result.length() == 0 ? null : result.toString();
    }

    /**
     * @param requestPath path of the request the response belongs to (default cookie path)
     * @param setCookieHeaders values of the Set-Cookie headers
     */
    synchronized void store(String requestPath, java.util.List<String> setCookieHeaders) {
      for (String setCookie : setCookieHeaders) {
        String[] parts = setCookie.split(";");
        int equalsIndex = parts[0].indexOf('=');
        if (equalsIndex <= 0) {
          continue;
        }
        String name = parts[0].substring(0, equalsIndex).trim();
        String value = parts[0].substring(equalsIndex + 1).trim();
        // default path: the request path up to its last slash (RFC 6265)
        int lastSlash = requestPath.lastIndexOf('/');
        String path = lastSlash <= 0 ? "/" : requestPath.substring(0, lastSlash);
        boolean expired = value.isEmpty();
        for (int i = 1; i < parts.length; i++) {
          String attribute = parts[i].trim();
          String lower = attribute.toLowerCase();
          if (lower.startsWith("path=")) {
            path = attribute.substring("path=".length()).trim();
          } else if (lower.startsWith("max-age=")) {
            try {
              expired = expired || Long.parseLong(attribute.substring("max-age=".length()).trim()) <= 0;
            } catch (NumberFormatException nfe) {
              // ignore a bad max-age
            }
          } else if (lower.startsWith("expires=") && lower.contains("1970")) {
            expired = true;
          }
        }
        if (path.isEmpty()) {
          path = "/";
        }
        if (expired) {
          this.cookies.remove(path + " " + name);
        } else {
          this.cookies.put(path + " " + name, new String[] {name, value, path});
        }
      }
    }
  }

}
