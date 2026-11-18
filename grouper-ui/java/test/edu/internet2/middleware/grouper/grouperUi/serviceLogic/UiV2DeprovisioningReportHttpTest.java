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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeValue;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningConfiguration;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningOverallConfiguration;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M6 (deprovisioningReportRevokesAdmin), HTTP check against the running local
 * UI: the deprovisioning report submits (UiV2Deprovisioning.deprovisioningOnGroupReportSubmit and
 * deprovisioningOnFolderReportSubmit) only remove access from members the report lists.
 * GrouperDeprovisioningAllowedAccessReportTest calls GrouperDeprovisioningAllowedAccess
 * directly; this test covers what it cannot: the real UI handler wiring (error and success
 * messages, the report refresh, and the folder submit refreshing the FOLDER report), checked
 * against the database rows the running UI wrote.</p>
 *
 * <p>The attack: a user with UPDATE+READ on the group opens the group's deprovisioning report and
 * submits it with the checkbox value changed to the member id of an active ADMIN who is not on
 * the report.  Refused, the ADMIN is kept.  The positive controls: the same user submits the
 * listed deprovisioned subject on the group report (loses membership and ADMIN, report refreshes
 * without them), and on the folder report (loses STEM_ADMIN, the folder report is shown).</p>
 *
 * <p>Setup (this test's own, removed at the end): affiliation secPatch46 (appended to
 * deprovisioning.affiliations in database config, restored in finally) with its managers and
 * deprovisioned users groups under etc:deprovisioning; jdbc subjects secPatch46a (active ADMIN)
 * and secPatch46b (deprovisioned); folder secPatch46 with group g and folder f, both configured
 * to deprovision for the affiliation; test.subject.0 (not wheel locally) UPDATE+READ on g and
 * STEM_ADMIN on f.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class UiV2DeprovisioningReportHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch46";

  /** group with the report */
  private static final String GROUP_NAME = STEM_NAME + ":g";

  /** folder with the report */
  private static final String FOLDER_NAME = STEM_NAME + ":f";

  /** affiliation this test adds to deprovisioning.affiliations */
  private static final String AFFILIATION = "secPatch46";

  /** the report user: UPDATE+READ on g, STEM_ADMIN on f (not wheel locally) */
  private static final String REPORT_USER = "test.subject.0";

  /** active ADMIN of g, not deprovisioned (jdbc row this test inserts) */
  private static final String ACTIVE_ADMIN = "secPatch46a";

  /** deprovisioned member and ADMIN of g, STEM_ADMIN of f (jdbc row this test inserts) */
  private static final String DEPROVISIONED = "secPatch46b";

  /** config key */
  private static final String AFFILIATIONS_KEY = "deprovisioning.affiliations";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2DeprovisioningReportHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2DeprovisioningReportHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2DeprovisioningReportHttpTest(String name) {
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
   * @param fieldName members, admins, ...
   * @param subjectId
   * @return number of enabled immediate rows of the subject in that group field (SQL)
   */
  private static int groupCount(String groupName, String fieldName, String subjectId) {
    return new GcDbAccess().sql("select count(*) from grouper_memberships ms, grouper_fields f, grouper_members m, grouper_groups g "
        + "where ms.field_id = f.id and ms.member_id = m.id and ms.owner_group_id = g.id and g.name = ? and f.name = ? "
        + "and m.subject_id = ? and ms.mship_type = 'immediate' and ms.enabled = 'T'")
        .addBindVar(groupName).addBindVar(fieldName).addBindVar(subjectId).select(Integer.class);
  }

  /**
   * @param stemName
   * @param subjectId
   * @return number of enabled immediate stemAdmins rows of the subject on the folder (SQL)
   */
  private static int stemAdminCount(String stemName, String subjectId) {
    return new GcDbAccess().sql("select count(*) from grouper_memberships ms, grouper_fields f, grouper_members m, grouper_stems s "
        + "where ms.field_id = f.id and ms.member_id = m.id and ms.owner_stem_id = s.id and s.name = ? and f.name = 'stemAdmins' "
        + "and m.subject_id = ? and ms.mship_type = 'immediate' and ms.enabled = 'T'")
        .addBindVar(stemName).addBindVar(subjectId).select(Integer.class);
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
   * turn deprovisioning on for the affiliation directly on this object (same as
   * GrouperDeprovisioningAllowedAccessReportTest)
   * @param owner
   */
  private static void configureDeprovisioning(GrouperObject owner) {
    GrouperDeprovisioningOverallConfiguration overallConfiguration =
        GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(owner, false);
    GrouperDeprovisioningConfiguration configuration = new GrouperDeprovisioningConfiguration();
    configuration.setGrouperDeprovisioningOverallConfiguration(overallConfiguration);
    overallConfiguration.getAffiliationToConfiguration().put(AFFILIATION, configuration);
    GrouperDeprovisioningAttributeValue attributeValue = new GrouperDeprovisioningAttributeValue();
    attributeValue.setGrouperDeprovisioningConfiguration(configuration);
    configuration.setNewConfig(attributeValue);
    attributeValue.setAffiliationString(AFFILIATION);
    attributeValue.setDirectAssignment(true);
    attributeValue.setDeprovisionString("true");
    configuration.storeConfiguration();
    GrouperDeprovisioningOverallConfiguration.cacheClear();
  }

  /**
   * delete everything this test made (not the config, see storeAffiliations).  The affiliation
   * groups must exist whenever any JVM has the affiliation configured (the membership veto hook
   * looks them up on every add and fails the add if they are missing), so they are only deleted
   * once the config is gone (deleteAffiliationGroups true)
   * @param deleteAffiliationGroups
   */
  private static void cleanUp46(boolean deleteAffiliationGroups) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, false);
      if (group != null) {
        group.delete();
      }
      for (String stemName : new String[] {FOLDER_NAME, STEM_NAME}) {
        Stem stem = StemFinder.findByName(rootSession, stemName, false);
        if (stem != null) {
          stem.delete();
        }
      }
      // the affiliation groups exist only for this test's affiliation
      for (String groupName : new String[] {managersGroupName(), deprovisionedGroupName()}) {
        Group affiliationGroup = GroupFinder.findByName(rootSession, groupName, false);
        if (affiliationGroup == null) {
          continue;
        }
        if (deleteAffiliationGroups) {
          affiliationGroup.delete();
        } else {
          for (Member member : affiliationGroup.getImmediateMembers()) {
            affiliationGroup.deleteMember(member, false);
          }
        }
      }
      for (String subjectId : new String[] {ACTIVE_ADMIN, DEPROVISIONED}) {
        HibernateSession.bySqlStatic().executeSql("delete from subjectattribute where subjectid = ?", listOf(subjectId));
        HibernateSession.bySqlStatic().executeSql("delete from subject where subjectid = ?", listOf(subjectId));
        List<String> memberIds = new GcDbAccess().sql("select id from grouper_members where subject_id = ? and subject_source = 'jdbc'")
            .addBindVar(subjectId).selectList(String.class);
        for (String memberId : memberIds) {
          try {
            HibernateSession.bySqlStatic().executeSql("delete from grouper_members where id = ?", listOf(memberId));
          } catch (RuntimeException re) {
            System.out.println("secPatch46: member row " + memberId + " still referenced, left in place: " + re.getMessage());
          }
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * after the config is restored, wait until the UI no longer has the affiliation (up to 90 s),
   * then 30 s more for other JVMs, so no JVM looks up the affiliation groups after they are deleted
   * @param session logged in session, or null if the test failed before login
   * @throws Exception
   */
  private static void waitUntilUiDropsAffiliation(UiSession session) throws Exception {
    if (session != null) {
      String body = "affiliation=" + enc(AFFILIATION) + "&groupAddMemberComboName=" + enc(REPORT_USER);
      for (int i = 0; i < 30; i++) {
        String html = unjson(session.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisionUserSubmit", body).body());
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

  // ---------------------------------------------------------------- page parsing

  /**
   * @param html decoded report page
   * @return checkbox name (memberIds_N) to value (member id), in page order
   */
  private static Map<String, String> memberRows(String html) {
    Map<String, String> result = new LinkedHashMap<String, String>();
    Matcher matcher = Pattern.compile("name=\"(memberIds_\\d+)\"[^>]*?value=\"([^\"]*)\"", Pattern.DOTALL).matcher(html);
    while (matcher.find()) {
      result.put(matcher.group(1), matcher.group(2));
    }
    return result;
  }

  /**
   * @param html
   * @return true if this is the group deprovisioning report
   */
  private static boolean isGroupReport(String html) {
    return html.contains("deprovisioningGroupMoreActionsButtonContentsDivId");
  }

  /**
   * @param html
   * @return true if this is the folder deprovisioning report
   */
  private static boolean isFolderReport(String html) {
    return html.contains("deprovisioningFolderMoreActionsButtonContentsDivId");
  }

  // ---------------------------------------------------------------- test

  /**
   * forged member id refused (ADMIN kept); listed subject removed on the group report and on the
   * folder report, each refreshing its own report
   * @throws Exception
   */
  public void testReportSubmitsBoundToListedMembers() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    String priorAffiliations = dbAffiliations();
    boolean configStored = false;
    assertTrue("a previous run left " + AFFILIATION + " in " + AFFILIATIONS_KEY + ": " + priorAffiliations,
        priorAffiliations == null || !priorAffiliations.contains(AFFILIATION));
    cleanUp46(false);
    UiSession reportUser = null;
    try {
      // ---- setup: the affiliation groups first (they must exist before any JVM sees the config)
      String groupId;
      String stemId;
      String activeAdminMemberId;
      String deprovisionedMemberId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        new GroupSave(rootSession).assignName(managersGroupName()).assignCreateParentStemsIfNotExist(true).save();
        Group deprovisionedGroup = new GroupSave(rootSession).assignName(deprovisionedGroupName())
            .assignCreateParentStemsIfNotExist(true).save();

        String newAffiliations = (priorAffiliations == null || priorAffiliations.trim().length() == 0)
            ? AFFILIATION : priorAffiliations + "," + AFFILIATION;
        storeAffiliations(newAffiliations);
        configStored = true;

        insertSubject(ACTIVE_ADMIN);
        insertSubject(DEPROVISIONED);
        Subject activeAdmin = SubjectFinder.findByIdAndSource(ACTIVE_ADMIN, "jdbc", true);
        Subject deprovisioned = SubjectFinder.findByIdAndSource(DEPROVISIONED, "jdbc", true);
        Subject user = SubjectFinder.findById(REPORT_USER, true);
        activeAdminMemberId = MemberFinder.findBySubject(rootSession, activeAdmin, true).getId();
        deprovisionedMemberId = MemberFinder.findBySubject(rootSession, deprovisioned, true).getId();

        Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        group.grantPriv(user, AccessPrivilege.UPDATE, false);
        group.grantPriv(user, AccessPrivilege.READ, false);
        group.grantPriv(activeAdmin, AccessPrivilege.ADMIN, false);
        group.addMember(deprovisioned, false);
        group.grantPriv(deprovisioned, AccessPrivilege.ADMIN, false);
        Stem folder = new StemSave(rootSession).assignName(FOLDER_NAME).assignCreateParentStemsIfNotExist(true).save();
        folder.grantPriv(user, NamingPrivilege.STEM_ADMIN, false);
        folder.grantPriv(deprovisioned, NamingPrivilege.STEM_ADMIN, false);
        configureDeprovisioning(group);
        configureDeprovisioning(folder);
        // last: once deprovisioned, the veto hook refuses new access for this subject
        deprovisionedGroup.addMember(deprovisioned, false);
        groupId = group.getId();
        stemId = folder.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      reportUser = new UiSession(baseUrl);
      reportUser.login(REPORT_USER, "pass");

      // ---- the group report lists the deprovisioned subject (retry while the UI picks up the config)
      String groupParams = "groupId=" + enc(groupId);
      SimpleHttpResponse report = null;
      Map<String, String> listed = null;
      for (int i = 0; i < 20; i++) {
        report = reportUser.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisioningOnGroupReport", groupParams);
        listed = memberRows(unjson(report.body()));
        if (listed.containsValue(deprovisionedMemberId)) {
          break;
        }
        Thread.sleep(3000);
      }
      String reportHtml = unjson(report.body());
      assertTrue("group report: " + describe(report), isGroupReport(reportHtml));
      assertTrue("deprovisioned subject listed: " + listed, listed.containsValue(deprovisionedMemberId));
      assertFalse("active admin not listed: " + listed, listed.containsValue(activeAdminMemberId));
      String checkboxName = null;
      for (Map.Entry<String, String> row : listed.entrySet()) {
        if (row.getValue().equals(deprovisionedMemberId)) {
          checkboxName = row.getKey();
        }
      }

      // ---- attack: the checkbox value changed to the active ADMIN's member id
      SimpleHttpResponse attack = reportUser.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisioningOnGroupReportSubmit",
          groupParams + "&" + enc(checkboxName + "[]") + "=" + enc(activeAdminMemberId));
      String attackHtml = unjson(attack.body());
      assertTrue("forged submit shows the refusal: " + describe(attack), attackHtml.contains("\"messageType\":\"error\"")
          && attackHtml.contains("Nothing was changed"));
      assertFalse("forged submit shows no success: " + describe(attack), attackHtml.contains("\"messageType\":\"success\""));
      assertEquals("active admin keeps ADMIN", 1, groupCount(GROUP_NAME, "admins", ACTIVE_ADMIN));
      assertEquals("deprovisioned subject untouched (ADMIN)", 1, groupCount(GROUP_NAME, "admins", DEPROVISIONED));
      assertEquals("deprovisioned subject untouched (member)", 1, groupCount(GROUP_NAME, "members", DEPROVISIONED));

      // ---- positive control 1: the listed subject on the group report
      SimpleHttpResponse groupSubmit = reportUser.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisioningOnGroupReportSubmit",
          groupParams + "&" + enc(checkboxName + "[]") + "=" + enc(deprovisionedMemberId));
      String groupSubmitHtml = unjson(groupSubmit.body());
      assertTrue("group submit succeeds: " + describe(groupSubmit), groupSubmitHtml.contains("\"messageType\":\"success\"")
          && groupSubmitHtml.contains("deprovisioned entities had access removed"));
      assertTrue("group report refreshed: " + describe(groupSubmit), isGroupReport(groupSubmitHtml));
      assertFalse("refreshed report no longer lists the subject: " + describe(groupSubmit),
          memberRows(groupSubmitHtml).containsValue(deprovisionedMemberId));
      assertEquals("deprovisioned subject lost ADMIN", 0, groupCount(GROUP_NAME, "admins", DEPROVISIONED));
      assertEquals("deprovisioned subject lost membership", 0, groupCount(GROUP_NAME, "members", DEPROVISIONED));
      assertEquals("active admin keeps ADMIN", 1, groupCount(GROUP_NAME, "admins", ACTIVE_ADMIN));
      assertEquals("report user keeps UPDATE", 1, groupCount(GROUP_NAME, "updaters", REPORT_USER));

      // ---- positive control 2: the folder report, listed subject
      String stemParams = "stemId=" + enc(stemId);
      SimpleHttpResponse folderReport = reportUser.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisioningOnFolderReport", stemParams);
      String folderReportHtml = unjson(folderReport.body());
      assertTrue("folder report: " + describe(folderReport), isFolderReport(folderReportHtml));
      Map<String, String> folderListed = memberRows(folderReportHtml);
      String folderCheckboxName = null;
      for (Map.Entry<String, String> row : folderListed.entrySet()) {
        if (row.getValue().equals(deprovisionedMemberId)) {
          folderCheckboxName = row.getKey();
        }
      }
      assertNotNull("deprovisioned subject on the folder report: " + folderListed, folderCheckboxName);
      SimpleHttpResponse folderSubmit = reportUser.postAjax("/grouperUi/app/UiV2Deprovisioning.deprovisioningOnFolderReportSubmit",
          stemParams + "&" + enc(folderCheckboxName + "[]") + "=" + enc(deprovisionedMemberId));
      String folderSubmitHtml = unjson(folderSubmit.body());
      assertTrue("folder submit succeeds: " + describe(folderSubmit), folderSubmitHtml.contains("\"messageType\":\"success\"")
          && folderSubmitHtml.contains("deprovisioned entities had access removed"));
      assertTrue("the FOLDER report is shown after the folder submit: " + describe(folderSubmit), isFolderReport(folderSubmitHtml));
      assertFalse("not the group report after the folder submit", isGroupReport(folderSubmitHtml));
      assertFalse("refreshed folder report no longer lists the subject",
          memberRows(folderSubmitHtml).containsValue(deprovisionedMemberId));
      assertEquals("deprovisioned subject lost STEM_ADMIN", 0, stemAdminCount(FOLDER_NAME, DEPROVISIONED));
      assertEquals("report user keeps STEM_ADMIN", 1, stemAdminCount(FOLDER_NAME, REPORT_USER));

    } finally {
      // fixtures first, then the config, then (once the UI no longer has the affiliation) the
      // affiliation groups and the subjects
      cleanUp46(false);
      if (configStored) {
        storeAffiliations(priorAffiliations);
        waitUntilUiDropsAffiliation(reportUser);
      }
      cleanUp46(true);
    }
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
