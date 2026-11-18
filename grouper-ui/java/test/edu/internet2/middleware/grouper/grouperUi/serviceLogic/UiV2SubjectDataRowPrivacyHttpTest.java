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
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory H5 (entityDataRowPrivacyAuthorization), HTTP check against the running local UI:
 * UiV2Subject.viewDataFieldAssignments must not return the values (or the row config id) of a data
 * row whose privacy realm the logged-in user cannot read, even when the operation is called
 * directly.  GrouperDataPrivacyRealmAccessRowAssignTest tests the filter class; this test covers
 * what it cannot: the real UI endpoint wiring.
 *
 * <p>Attack: test.subject.0 (no role in the realm, not wheel) calls the endpoint for subject
 * secPatch26 and must not see the private row value or row id (the public scalar value on the same
 * page shows the screen did render).  Positive control: test.subject.2, a member of the realm's
 * readers group, sees the private row value.</p>
 *
 * <p>Setup (this test's own, removed at the end): subject secPatch26 in the example jdbc subject
 * table; folder secPatch26 with readers group secPatch26:readers (test.subject.2); entity data
 * config secPatch26* (a private realm, a public realm, a public scalar field, a private row column
 * field and row, a provider with two SQL queries) and its full sync job otherJob.secPatch26Sync, run
 * once in this JVM.  The realm access check reads memberships through the sql cache tables, which the
 * change log fills; the local daemon is off, so this test writes the sql cache rows of its own
 * readers group itself and deletes them at the end.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which resets
 * the database the running UI uses).  Plain HTTP requests, dev form login, OWASP CSRF token
 * from OwaspJavaScriptServlet.  The endpoint only reads, so there is no state to check after the
 * attack; the private assignment is confirmed present with SQL so the refusal is not a missing row.</p>
 */
public class UiV2SubjectDataRowPrivacyHttpTest extends TestCase {

  /** subject the provider assigns data to */
  private static final String SUBJECT_ID = "secPatch26";

  /** folder of the readers group */
  private static final String STEM_NAME = "secPatch26";

  /** readers group of the private realm */
  private static final String READERS_GROUP = STEM_NAME + ":readers";

  /** private realm, readers = READERS_GROUP */
  private static final String PRIV_REALM = "secPatch26privRealm";

  /** public realm */
  private static final String PUB_REALM = "secPatch26pubRealm";

  /** public scalar field (shows the page rendered data) */
  private static final String PUB_FIELD = "secPatch26pubField";

  /** private row column field */
  private static final String ROW_FIELD = "secPatch26rowField";

  /** private row */
  private static final String ROW = "secPatch26privRow";

  /** data provider */
  private static final String PROVIDER = "secPatch26provider";

  /** full sync job (otherJob config id) */
  private static final String JOB = "secPatch26Sync";

  /** value of the public scalar field */
  private static final String PUB_VALUE = "SECPATCH26PUBLICVALUE";

  /** value of the private row field */
  private static final String PRIV_VALUE = "SECPATCH26PRIVATEVALUE";

  /** user with no role in the private realm */
  private static final String ATTACKER = "test.subject.0";

  /** member of the private realm's readers group */
  private static final String READER = "test.subject.2";

  /** grouper.properties keys and values this test stores */
  private static final String[][] GROUPER_KEYS = {
      {"grouperPrivacyRealm." + PRIV_REALM + ".privacyRealmName", PRIV_REALM},
      {"grouperPrivacyRealm." + PRIV_REALM + ".privacyRealmPublic", "false"},
      {"grouperPrivacyRealm." + PRIV_REALM + ".privacyRealmAuthenticated", "false"},
      {"grouperPrivacyRealm." + PRIV_REALM + ".privacyRealmReadersGroupName", READERS_GROUP},

      {"grouperPrivacyRealm." + PUB_REALM + ".privacyRealmName", PUB_REALM},
      {"grouperPrivacyRealm." + PUB_REALM + ".privacyRealmPublic", "true"},

      {"grouperDataField." + PUB_FIELD + ".fieldAliases", PUB_FIELD},
      {"grouperDataField." + PUB_FIELD + ".fieldPrivacyRealm", PUB_REALM},
      {"grouperDataField." + PUB_FIELD + ".descriptionHtml", PUB_FIELD},
      {"grouperDataField." + PUB_FIELD + ".fieldDataType", "string"},
      {"grouperDataField." + PUB_FIELD + ".fieldDataStructure", "attribute"},
      {"grouperDataField." + PUB_FIELD + ".fieldDataAssignableTo", "individuals"},

      {"grouperDataField." + ROW_FIELD + ".fieldAliases", ROW_FIELD},
      {"grouperDataField." + ROW_FIELD + ".fieldPrivacyRealm", PRIV_REALM},
      {"grouperDataField." + ROW_FIELD + ".descriptionHtml", ROW_FIELD},
      {"grouperDataField." + ROW_FIELD + ".fieldDataType", "string"},
      {"grouperDataField." + ROW_FIELD + ".fieldDataStructure", "rowColumn"},
      {"grouperDataField." + ROW_FIELD + ".fieldDataAssignableTo", "individuals"},

      {"grouperDataRow." + ROW + ".rowAliases", ROW},
      {"grouperDataRow." + ROW + ".rowPrivacyRealm", PRIV_REALM},
      {"grouperDataRow." + ROW + ".descriptionHtml", ROW},
      {"grouperDataRow." + ROW + ".oneRowPerSubject", "true"},
      {"grouperDataRow." + ROW + ".rowNumberOfDataFields", "1"},
      {"grouperDataRow." + ROW + ".rowDataField.0.colDataFieldConfigId", ROW_FIELD},

      {"grouperDataProvider." + PROVIDER + ".name", PROVIDER},

      {"grouperDataProviderQuery.secPatch26attrQuery.providerConfigId", PROVIDER},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQueryType", "sql"},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQuerySqlConfigId", "grouper"},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQuerySqlQuery",
        "select '" + SUBJECT_ID + "' as subject_id, '" + PUB_VALUE + "' as attr_val"},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQueryDataStructure", "attribute"},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQuerySubjectIdAttribute", "subject_id"},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQuerySubjectIdType", "subjectId"},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQuerySubjectSourceId", "jdbc"},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQueryNumberOfDataFields", "1"},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQueryDataField.0.providerDataFieldConfigId", PUB_FIELD},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQueryDataField.0.providerDataFieldMappingType", "attribute"},
      {"grouperDataProviderQuery.secPatch26attrQuery.providerQueryDataField.0.providerDataFieldAttribute", "attr_val"},

      {"grouperDataProviderQuery.secPatch26rowQuery.providerConfigId", PROVIDER},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQueryType", "sql"},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQuerySqlConfigId", "grouper"},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQuerySqlQuery",
        "select '" + SUBJECT_ID + "' as subject_id, '" + PRIV_VALUE + "' as row_val"},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQueryDataStructure", "row"},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQueryRowConfigId", ROW},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQuerySubjectIdAttribute", "subject_id"},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQuerySubjectIdType", "subjectId"},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQuerySubjectSourceId", "jdbc"},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQueryNumberOfDataFields", "1"},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQueryDataField.0.providerDataFieldConfigId", ROW_FIELD},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQueryDataField.0.providerDataFieldMappingType", "attribute"},
      {"grouperDataProviderQuery.secPatch26rowQuery.providerQueryDataField.0.providerDataFieldAttribute", "row_val"},
  };

  /** grouper-loader.properties keys and values this test stores */
  private static final String[][] LOADER_KEYS = {
      {"otherJob." + JOB + ".class", GrouperDataProviderFullSyncJob.class.getName()},
      // never fires while the test runs (3am); the test runs the job itself
      {"otherJob." + JOB + ".quartzCron", "0 0 3 * * ?"},
      {"otherJob." + JOB + ".dataProviderConfigId", PROVIDER},
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2SubjectDataRowPrivacyHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2SubjectDataRowPrivacyHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2SubjectDataRowPrivacyHttpTest(String name) {
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

    /**
     * Authorization header value when the UI uses basic auth (releases without the dev form login,
     * e.g. 7.0-7.3, 6.0-6.4, 5.x, 2.6.19), else null (form login)
     */
    private String basicAuthorization;

    /** e.g. http://localhost:8080/grouper */
    private final String baseUrl;

    /** OWASP CSRF master token of this session */
    private String csrfToken;

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
      if (response.statusCode() == 401) {
        // no dev form login in this release (GrouperUiFormLogin is in 6.5.0, 7.4.0 and later): the
        // local UI runs with grouper.is.ui.basicAuthn=true, which checks the same UI password on
        // every request
        this.basicAuthorization = "Basic " + java.util.Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        response = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/UiV2Main.index")).GET());
        // only a bad password is 401 (a refused section comes later, not as 401)
        assertTrue("basic auth login of " + username + ": " + response.statusCode(), response.statusCode() != 401);
      } else {
        // a good login is a post/redirect/get; a bad one shows the form again (200)
        assertEquals("form login of " + username + " must redirect (without form login set "
            + "grouper.is.ui.basicAuthn=true): " + response.body(), 302, response.statusCode());
      }

      SimpleHttpResponse csrfJs = send(SimpleHttpRequest.newBuilder(
          URI.create(this.baseUrl + "/grouperExternal/public/OwaspJavaScriptServlet")).GET());
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
        // basic auth is checked on every request, not kept by the session cookie
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
   * @param response
   * @return status and the start of the body, for failure messages
   */
  static String describe(SimpleHttpResponse response) {
    String body = response.body() == null ? "" : response.body();
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

  // ---------------------------------------------------------------- setup helpers

  /**
   * subject, readers group (and its sql cache rows), config, and one run of the full sync job
   */
  private static void createData() {
    HibernateSession.bySqlStatic().executeSql(
        "insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
        GrouperUtil.toListObject(SUBJECT_ID, "secPatch26 entity data row privacy"));
    // GrouperDbConfig checks the caller may edit config, so it needs a (root) session
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group readers = new GroupSave(rootSession).assignName(READERS_GROUP).assignCreateParentStemsIfNotExist(true).save();
      Subject reader = SubjectFinder.findById(READER, true);
      readers.addMember(reader, false);

      // the realm check reads grouper_sql_cache_group / grouper_sql_cache_mship (filled by the change log);
      // write this group's members cache directly
      long membersFieldInternalId = FieldFinder.find("members", true).getInternalId();
      SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
      sqlCacheGroup.setGroupInternalId(readers.getInternalId());
      sqlCacheGroup.setFieldInternalId(membersFieldInternalId);
      sqlCacheGroup.setMembershipSize(1);
      sqlCacheGroup.setMembershipSizeHst(0);
      sqlCacheGroup.setEnabledOn(new Timestamp(System.currentTimeMillis() - 60000L));
      SqlCacheGroupDao.retrieveOrCreateBySqlGroupCache(sqlCacheGroup);
      Long sqlCacheGroupInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group "
          + "where group_internal_id = ? and field_internal_id = ?").addBindVar(readers.getInternalId())
          .addBindVar(membersFieldInternalId).select(Long.class);
      Member readerMember = MemberFinder.findBySubject(rootSession, reader, true);
      new GcDbAccess().sql("insert into grouper_sql_cache_mship (sql_cache_group_internal_id, member_internal_id, "
          + "flattened_add_timestamp) values (?, ?, ?)").addBindVar(sqlCacheGroupInternalId)
          .addBindVar(readerMember.getInternalId()).addBindVar(System.currentTimeMillis() * 1000L).executeSql();

      for (String[] key : GROUPER_KEYS) {
        new GrouperDbConfig().configFileName("grouper.properties").propertyName(key[0]).value(key[1]).store();
      }
      for (String[] key : LOADER_KEYS) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(key[0]).value(key[1]).store();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_" + JOB);
  }

  /**
   * run one cleanup statement, keep going on error
   * @param sql
   * @param param
   */
  private static void sql(String sql, String param) {
    try {
      HibernateSession.bySqlStatic().executeSql(sql, GrouperUtil.toListObject(param));
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }

  /**
   * delete config, synced data, sql cache rows, group, folder, member and subject
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String[] key : GROUPER_KEYS) {
        try {
          new GrouperDbConfig().configFileName("grouper.properties").propertyName(key[0]).delete();
        } catch (RuntimeException re) {
          // already gone; keep cleaning up the rest
        }
      }
      for (String[] key : LOADER_KEYS) {
        try {
          new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(key[0]).delete();
        } catch (RuntimeException re) {
          // already gone; keep cleaning up the rest
        }
      }

      Group readers = GroupFinder.findByName(rootSession, READERS_GROUP, false);
      if (readers != null) {
        String cacheGroups = "(select internal_id from grouper_sql_cache_group where group_internal_id = "
            + readers.getInternalId() + ")";
        sql("delete from grouper_sql_cache_mship where sql_cache_group_internal_id in " + cacheGroups + " and 'x' <> ?", "y");
        sql("delete from grouper_sql_cache_mship_hst where sql_cache_group_internal_id in " + cacheGroups + " and 'x' <> ?", "y");
        sql("delete from grouper_sql_cache_group where group_internal_id = " + readers.getInternalId() + " and 'x' <> ?", "y");
        readers.delete();
      }
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.delete();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // synced data, children first (see the foreign keys on the grouper_data_* tables)
    String fields = "(select internal_id from grouper_data_field where config_id like 'secPatch26%')";
    String rows = "(select internal_id from grouper_data_row where config_id like 'secPatch26%')";
    String like = "secPatch26%";
    sql("delete from grouper_data_row_field_asn_hst where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_row_field_assign where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_row_assign_hst where data_row_internal_id in " + rows + " and 'x' <> ?", like);
    sql("delete from grouper_data_row_assign where data_row_internal_id in " + rows + " and 'x' <> ?", like);
    sql("delete from grouper_data_field_assign_hst where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_field_assign where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_global_assign where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_alias where (data_field_internal_id in " + fields
        + " or data_row_internal_id in " + rows + ") and 'x' <> ?", like);
    sql("delete from grouper_data_field where config_id like ?", like);
    sql("delete from grouper_data_row where config_id like ?", like);
    sql("delete from grouper_data_provider where config_id like ?", like);
    // the sync job's bookkeeping rows (grouper_sync named dataProvider_secPatch26provider)
    for (String table : new String[] {"grouper_sync_log", "grouper_sync_member", "grouper_sync_job"}) {
      sql("delete from " + table + " where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?)",
          "dataProvider_" + PROVIDER);
    }
    sql("delete from grouper_sync where provisioner_name = ?", "dataProvider_" + PROVIDER);
    sql("delete from grouper_loader_log where job_name = ?", "OTHER_JOB_" + JOB);
    sql("delete from grouper_members where subject_id = ? and subject_source = 'jdbc'", SUBJECT_ID);
    sql("delete from subject where subjectid = ?", SUBJECT_ID);
  }

  /**
   * @return the private row value assigned to the subject in the database (null if not synced)
   */
  private static String privateValueInDb() {
    return new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where data_field_config_id = ? "
        + "and subject_id = ?").addBindVar(ROW_FIELD).addBindVar(SUBJECT_ID).select(String.class);
  }

  // ---------------------------------------------------------------- test

  /**
   * the user with no realm role gets the page (public scalar value) without the private row; the
   * realm reader gets the private row
   * @throws Exception
   */
  public void testPrivateRowNotReturnedToNonReader() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    // a previous aborted run may have left data behind
    deleteData();
    try {
      createData();
      assertEquals("private row value synced to the database", PRIV_VALUE, privateValueInDb());

      String baseUrl = uiBaseUrl();
      String params = "subjectId=" + enc(SUBJECT_ID) + "&sourceId=jdbc";
      String path = "/grouperUi/app/UiV2Subject.viewDataFieldAssignments";

      UiSession reader = new UiSession(baseUrl);
      reader.login(READER, "pass");
      UiSession attacker = new UiSession(baseUrl);
      attacker.login(ATTACKER, "pass");

      // positive control first: the running UI sees new DB config within seconds to ~30 s, so retry
      // until the reader gets the private row (this also proves the config is loaded before the attack)
      SimpleHttpResponse readerResponse = null;
      for (int i = 0; i < 12; i++) {
        readerResponse = reader.postAjax(path, params);
        if (readerResponse.body().contains(PRIV_VALUE) && readerResponse.body().contains(PUB_VALUE)) {
          break;
        }
        Thread.sleep(5000);
      }
      assertTrue("reader gets the screen: " + describe(readerResponse), readerResponse.body().contains("grouperMainContentDivId"));
      assertTrue("reader sees the public scalar value: " + describe(readerResponse), readerResponse.body().contains(PUB_VALUE));
      assertTrue("reader sees the private row value: " + describe(readerResponse), readerResponse.body().contains(PRIV_VALUE));
      assertTrue("reader sees the private row id: " + describe(readerResponse), readerResponse.body().contains(ROW));

      // attack: direct call for the same subject by a user with no role in the private realm
      SimpleHttpResponse attack = attacker.postAjax(path, params);
      assertTrue("attacker gets the screen: " + describe(attack), attack.body().contains("grouperMainContentDivId"));
      assertTrue("attacker sees the public scalar value (screen rendered data): " + describe(attack),
          attack.body().contains(PUB_VALUE));
      assertFalse("attacker must not see the private row value: " + describe(attack), attack.body().contains(PRIV_VALUE));
      assertFalse("attacker must not see the private row id: " + describe(attack), attack.body().contains(ROW));
      assertFalse("attacker must not see the private field id: " + describe(attack), attack.body().contains(ROW_FIELD));

      // the endpoint only reads; the private assignment is untouched
      assertEquals("private row value still in the database", PRIV_VALUE, privateValueInDb());
    } finally {
      deleteData();
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
