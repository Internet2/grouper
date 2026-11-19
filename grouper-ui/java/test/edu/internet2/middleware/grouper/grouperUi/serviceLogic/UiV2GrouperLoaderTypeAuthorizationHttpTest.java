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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory M3 (loaderTypeAuthorization, merged with groupAdminsCanSeeLoaderConfigs), HTTP
 * check against the running local UI: UiV2GrouperLoader.editGrouperLoaderSave calls
 * UiV2GrouperLoaderTypeAuthorization.checkSave before any root-session change and skips the
 * loader-metadata cleanup on a group with no loader marker, and editGrouperLoader /
 * exportLoaderConfig call canOpenOrExport with the group's real loader type.
 * UiV2GrouperLoaderTypeAuthorizationTest covers only the pure decision logic; this covers the
 * handler wiring.
 *
 * <ul>
 * <li>Trigger 1: a VIEW-only user saves the SQL loader group with type INVALID: "Source type is
 * required", the SQL loader marker and query are unchanged (SQL).  Also an ABAC-allowed group ADMIN
 * switching it to JEXL_SCRIPT: refused, unchanged.</li>
 * <li>Trigger 2: a VIEW-only user turns the loader "off" on a group managed by the SQL loader (loader
 * metadata, no marker of its own): the fix makes this a no-op (the security report's Fix decision), so the
 * loader metadata must still be there.</li>
 * <li>Open/export: the ABAC-allowed group ADMIN (not a loader editor) gets neither the edit screen
 * nor the export, and the SQL query is in neither response.</li>
 * <li>Positive control, GrouperSystem: creates the SQL loader through the same save, opens and
 * exports it (query shown), saves it unchanged, turns it off (marker gone, and the managed group's
 * metadata cleaned up, the legitimate cleanup path), and on again.</li>
 * </ul>
 *
 * <p>Setup (this test's own, removed at the end): folder secPatch19 with sqlGroup and managedGroup
 * (loaderMetadata with grouperLoaderMetadataGroupId = sqlGroup); test.subject.0 VIEW on both;
 * test.subject.1 ADMIN on sqlGroup (allowed to edit JEXL loaders by default:
 * grouper.abac.edit.if.in.group is blank) and not in the loader editors group
 * uiV2.loader.edit.if.in.group (etc:loaderEditors).  No loader job is scheduled: the save only
 * schedules with editLoaderScheduleJobName=true or an existing job.  uiV2.loader.view.by.group.admins
 * is not touched: the fixed open/export handlers no longer consult it.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class UiV2GrouperLoaderTypeAuthorizationHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch19";

  /** VIEW only */
  private static final String VIEWER = "test.subject.0";

  /** ADMIN on the SQL loader group, may edit JEXL loaders, not a loader editor */
  private static final String GROUP_ADMIN = "test.subject.1";

  /** marker query string, distinctive so it can be looked for in responses */
  private static final String QUERY = "select 'secPatch19Query' as subject_id from grouper_groups where 1 = 0";

  /** grouperLoaderEditTypeRequired */
  private static final String TYPE_REQUIRED = "Source type is required";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2GrouperLoaderTypeAuthorizationHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2GrouperLoaderTypeAuthorizationHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2GrouperLoaderTypeAuthorizationHttpTest(String name) {
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

      SimpleHttpResponse csrfJs = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperExternal/public/OwaspJavaScriptServlet")).GET());
      Matcher matcher = Pattern.compile("masterTokenValue = '([^']+)'").matcher(csrfJs.body());
      assertTrue("CSRF token for " + username, matcher.find());
      this.csrfToken = matcher.group(1);
    }

    /**
     * ajax POST with the CSRF header and a form body, like the UI's own requests
     * @param operation e.g. UiV2GroupPermission.limitMenu
     * @param formBody urlencoded body
     * @return the response
     * @throws Exception
     */
    SimpleHttpResponse postAjax(String operation, String formBody) throws Exception {
      return send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/" + operation))
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

  /**
   * @param response
   * @return true if the response shows a success message
   */
  private static boolean isSuccess(SimpleHttpResponse response) {
    return response.body() != null && response.body().contains("\"messageType\":\"success\"");
  }

  /**
   * form body of a full SQL_SIMPLE loader save (what the edit screen posts)
   * @param groupId
   * @return the body
   */
  private static String sqlLoaderBody(String groupId) {
    return "groupId=" + enc(groupId) + "&grouperLoaderHasLoaderName=true&grouperLoaderTypeName=SQL"
        + "&grouperLoaderSqlTypeName=SQL_SIMPLE&grouperLoaderSqlDatabaseNameName=grouper"
        + "&grouperLoaderSqlQueryName=" + enc(QUERY)
        + "&editLoaderScheduleTypeName=CRON&editLoaderCronName=" + enc("0 0 5 1 1 ?");
  }

  // ---------------------------------------------------------------- db helpers

  /**
   * @param groupId
   * @param attributeDefNameName
   * @return number of assignments of this attribute directly on the group
   */
  private static int groupAssignCount(String groupId, String attributeDefNameName) {
    return new GcDbAccess().sql("select count(*) from grouper_attribute_assign aa, grouper_attribute_def_name adn "
        + "where adn.id = aa.attribute_def_name_id and aa.owner_group_id = ? and adn.name = ?")
        .addBindVar(groupId).addBindVar(attributeDefNameName).select(int.class);
  }

  /**
   * @param groupId
   * @return the SQL loader marker count on the group (the UI JVM wrote it; read the database)
   */
  private static int sqlLoaderMarkerCount(String groupId) {
    return groupAssignCount(groupId, GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc")
        + ":legacy:attribute:legacyGroupType_grouperLoader");
  }

  /**
   * @param groupId
   * @return the grouperLoaderQuery value(s) on the group's loader marker
   */
  private static List<String> sqlLoaderQueryInDb(String groupId) {
    return new GcDbAccess().sql("select aav.value_string from grouper_attribute_assign owner_aa, grouper_attribute_assign aa, "
        + "grouper_attribute_def_name adn, grouper_attribute_assign_value aav "
        + "where owner_aa.owner_group_id = ? and aa.owner_attribute_assign_id = owner_aa.id "
        + "and adn.id = aa.attribute_def_name_id and adn.name like '%:legacyAttribute_grouperLoaderQuery' "
        + "and aav.attribute_assign_id = aa.id").addBindVar(groupId).selectList(String.class);
  }

  /**
   * @param groupId
   * @return the group's attribute assignments and their values, for failure messages
   */
  private static String dumpAssigns(String groupId) {
    List<Object[]> rows = new GcDbAccess().sql("select adn.name, aa.id, aa.owner_attribute_assign_id, aav.value_string "
        + "from grouper_attribute_assign aa join grouper_attribute_def_name adn on adn.id = aa.attribute_def_name_id "
        + "left join grouper_attribute_assign_value aav on aav.attribute_assign_id = aa.id "
        + "where aa.owner_group_id = ? or aa.owner_attribute_assign_id in (select id from grouper_attribute_assign where owner_group_id = ?)")
        .addBindVar(groupId).addBindVar(groupId).selectList(Object[].class);
    StringBuilder result = new StringBuilder();
    for (Object[] row : rows) {
      result.append(java.util.Arrays.toString(row)).append("; ");
    }
    return result.toString();
  }

  /**
   * @param groupId
   * @return loaderMetadata marker count on the group
   */
  private static int loaderMetadataCount(String groupId) {
    return groupAssignCount(groupId, GrouperCheckConfig.loaderMetadataStemName() + ":" + GrouperLoader.LOADER_METADATA_VALUE_DEF);
  }

  /**
   * delete the folder and everything in it
   */
  private static void cleanUp19() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.obliterate(false, false);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * forged saves refused or no-op with no change, open/export refused; GrouperSystem saves work
   * @throws Exception
   */
  public void testLoaderSaveAndOpenAuthorizedByLoaderType() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUp19();
    try {
      String sqlGroupId;
      String managedGroupId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Subject viewer = SubjectFinder.findById(VIEWER, true);
        Subject groupAdmin = SubjectFinder.findById(GROUP_ADMIN, true);
        Group sqlGroup = new GroupSave(rootSession).assignName(STEM_NAME + ":sqlGroup").assignCreateParentStemsIfNotExist(true).save();
        Group managedGroup = new GroupSave(rootSession).assignName(STEM_NAME + ":managedGroup").save();
        sqlGroupId = sqlGroup.getId();
        managedGroupId = managedGroup.getId();

        // trigger 2 fixture: loader-management metadata pointing at the SQL loader group, no marker
        String metadataStem = GrouperCheckConfig.loaderMetadataStemName();
        AttributeDefName loaderMetadata = AttributeDefNameFinder.findByName(metadataStem + ":" + GrouperLoader.LOADER_METADATA_VALUE_DEF, true);
        AttributeAssign metadataAssign = managedGroup.getAttributeDelegate().assignAttribute(loaderMetadata).getAttributeAssign();
        metadataAssign.getAttributeValueDelegate().assignValue(metadataStem + ":" + GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_GROUP_ID, sqlGroupId);
        metadataAssign.getAttributeValueDelegate().assignValue(metadataStem + ":" + GrouperLoader.ATTRIBUTE_GROUPER_LOADER_METADATA_LOADED, "true");

        sqlGroup.grantPriv(viewer, AccessPrivilege.VIEW, false);
        managedGroup.grantPriv(viewer, AccessPrivilege.VIEW, false);
        sqlGroup.grantPriv(groupAdmin, AccessPrivilege.ADMIN, false);
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      // precondition: the group admin is not a loader editor
      String loaderEditors = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":loaderEditors";
      int editorRows = new GcDbAccess().sql("select count(*) from grouper_memberships_lw_v where group_name = ? "
          + "and list_name = 'members' and subject_id = ?").addBindVar(loaderEditors).addBindVar(GROUP_ADMIN).select(int.class);
      assertEquals(GROUP_ADMIN + " must not be in " + loaderEditors, 0, editorRows);
      assertEquals("managed group has loader metadata", 1, loaderMetadataCount(managedGroupId));

      // ---- positive control, step 4 part 1: GrouperSystem creates the SQL loader through the UI save
      UiSession root = new UiSession(baseUrl);
      root.login("GrouperSystem", "pass");
      SimpleHttpResponse create = root.postAjax("UiV2GrouperLoader.editGrouperLoaderSave", sqlLoaderBody(sqlGroupId));
      assertTrue("GrouperSystem creates the SQL loader: " + describe(create), isSuccess(create));
      assertEquals("SQL loader marker on sqlGroup", 1, sqlLoaderMarkerCount(sqlGroupId));
      assertEquals("query saved: " + dumpAssigns(sqlGroupId), "[" + QUERY + "]", sqlLoaderQueryInDb(sqlGroupId).toString());

      SimpleHttpResponse rootOpen = root.postAjax("UiV2GrouperLoader.editGrouperLoader", "groupId=" + enc(sqlGroupId));
      assertTrue("GrouperSystem opens the loader edit screen with the query: " + describe(rootOpen),
          rootOpen.body().contains("grouperMainContentDivId") && rootOpen.body().contains("secPatch19Query"));
      SimpleHttpResponse rootExport = root.postAjax("UiV2GrouperLoader.exportLoaderConfig", "groupId=" + enc(sqlGroupId));
      assertTrue("GrouperSystem exports the loader config with the query: " + describe(rootExport),
          rootExport.statusCode() == 200 && rootExport.body().contains("secPatch19Query"));

      // ---- step 1: VIEW-only user, unknown loader type on the SQL loader group
      UiSession viewer = new UiSession(baseUrl);
      viewer.login(VIEWER, "pass");
      SimpleHttpResponse invalidType = viewer.postAjax("UiV2GrouperLoader.editGrouperLoaderSave",
          "groupId=" + enc(sqlGroupId) + "&grouperLoaderHasLoaderName=true&grouperLoaderTypeName=INVALID");
      assertTrue("INVALID type refused with 'source type is required': " + describe(invalidType),
          invalidType.body().contains("\"messageType\":\"error\"") && invalidType.body().contains(TYPE_REQUIRED));
      assertFalse("INVALID type not a success: " + describe(invalidType), isSuccess(invalidType));
      assertEquals("SQL loader marker unchanged after INVALID", 1, sqlLoaderMarkerCount(sqlGroupId));
      assertEquals("query unchanged after INVALID", QUERY, sqlLoaderQueryInDb(sqlGroupId).get(0));

      // ---- step 2: VIEW-only user turns "off" the loader of the managed group (no marker of its own)
      SimpleHttpResponse managedOff = viewer.postAjax("UiV2GrouperLoader.editGrouperLoaderSave",
          "groupId=" + enc(managedGroupId) + "&grouperLoaderHasLoaderName=false");
      assertEquals("managed group's loader metadata unchanged (" + describe(managedOff) + ")", 1, loaderMetadataCount(managedGroupId));
      assertEquals("SQL loader untouched by the managed-group request", 1, sqlLoaderMarkerCount(sqlGroupId));

      // ---- step 3: group admin allowed only JEXL loaders: no edit screen, no export, no type switch
      UiSession groupAdmin = new UiSession(baseUrl);
      groupAdmin.login(GROUP_ADMIN, "pass");
      SimpleHttpResponse adminOpen = groupAdmin.postAjax("UiV2GrouperLoader.editGrouperLoader", "groupId=" + enc(sqlGroupId));
      assertFalse("group admin does not get the SQL loader edit screen: " + describe(adminOpen),
          adminOpen.body().contains("secPatch19Query") || adminOpen.body().contains("grouperMainContentDivId"));
      SimpleHttpResponse adminExport = groupAdmin.postAjax("UiV2GrouperLoader.exportLoaderConfig", "groupId=" + enc(sqlGroupId));
      assertFalse("group admin does not get the SQL loader export: " + describe(adminExport),
          adminExport.body().contains("secPatch19Query"));
      SimpleHttpResponse adminSwitch = groupAdmin.postAjax("UiV2GrouperLoader.editGrouperLoaderSave",
          "groupId=" + enc(sqlGroupId) + "&grouperLoaderHasLoaderName=true&grouperLoaderTypeName=JEXL_SCRIPT"
          + "&grouperLoaderJexlScriptName=" + enc("${entity.memberOf('" + STEM_NAME + ":managedGroup')}"));
      assertFalse("group admin's switch SQL -> JEXL refused: " + describe(adminSwitch), isSuccess(adminSwitch));
      assertEquals("SQL loader marker unchanged after the switch attempt", 1, sqlLoaderMarkerCount(sqlGroupId));
      assertEquals("query unchanged after the switch attempt", QUERY, sqlLoaderQueryInDb(sqlGroupId).get(0));
      // the refusals are the check, not a dead session: the same session still renders a screen
      SimpleHttpResponse adminView = groupAdmin.postAjax("UiV2Group.viewGroup", "groupId=" + enc(sqlGroupId));
      assertTrue("group admin's session is alive: " + describe(adminView), adminView.body().contains("grouperMainContentDivId"));

      // ---- step 4: GrouperSystem saves unchanged, turns off, turns on
      SimpleHttpResponse resave = root.postAjax("UiV2GrouperLoader.editGrouperLoaderSave", sqlLoaderBody(sqlGroupId));
      assertTrue("GrouperSystem re-saves unchanged: " + describe(resave), isSuccess(resave));
      assertEquals("one SQL loader marker after re-save", 1, sqlLoaderMarkerCount(sqlGroupId));
      assertEquals("query after re-save", QUERY, sqlLoaderQueryInDb(sqlGroupId).get(0));

      SimpleHttpResponse off = root.postAjax("UiV2GrouperLoader.editGrouperLoaderSave",
          "groupId=" + enc(sqlGroupId) + "&grouperLoaderHasLoaderName=false");
      assertTrue("GrouperSystem turns the loader off: " + describe(off), isSuccess(off));
      assertEquals("SQL loader marker removed", 0, sqlLoaderMarkerCount(sqlGroupId));
      assertEquals("real removal cleans the managed group's metadata", 0, loaderMetadataCount(managedGroupId));

      // turning it back on right away re-adds the marker but not the loader attributes: the UI JVM
      // still has the group's old legacy attribute map cached (Group.setAttribute skips a value it
      // thinks is already set).  Not part of this fix (the save code below the checks is unchanged);
      // re-save until the cache has expired
      long onStarted = System.currentTimeMillis();
      int onAttempts = 0;
      while (true) {
        onAttempts++;
        SimpleHttpResponse on = root.postAjax("UiV2GrouperLoader.editGrouperLoaderSave", sqlLoaderBody(sqlGroupId));
        assertTrue("GrouperSystem turns the loader back on: " + describe(on), isSuccess(on));
        assertEquals("SQL loader marker back", 1, sqlLoaderMarkerCount(sqlGroupId));
        if (sqlLoaderQueryInDb(sqlGroupId).size() > 0 || System.currentTimeMillis() - onStarted > 330000) {
          break;
        }
        Thread.sleep(15000);
      }
      System.out.println("secPatch19: loader back on with its query after " + onAttempts + " save(s), "
          + ((System.currentTimeMillis() - onStarted) / 1000) + " s");
      assertEquals("query back: " + dumpAssigns(sqlGroupId), "[" + QUERY + "]", sqlLoaderQueryInDb(sqlGroupId).toString());

      System.out.println("secPatch19: INVALID type and type switch refused, managed-group off is a no-op, "
          + "open/export refused for the group admin; GrouperSystem create/resave/off/on ok");
    } finally {
      cleanUp19();
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
