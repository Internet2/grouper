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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.daemon.GrouperDaemonOtherJobLoaderIncrementalConfiguration;
import edu.internet2.middleware.grouper.app.daemon.GrouperDaemonOtherJobScriptConfiguration;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Security advisory H4 (daemonAdminScriptAuthority), HTTP check against the running local UI: a
 * member of uiV2.admin.daemonJobs.must.be.in.group who is not wheel uses the real UiV2Admin daemon
 * handlers.  Adding a script daemon and editing a sysadmin's script daemon show "Only a Grouper
 * sysadmin (wheel or root) can add, edit or delete this daemon" and save nothing; deleting it shows
 * the generic error and leaves the config and the scheduled quartz job.  The same user can still
 * run, disable and enable the script daemon, and add and delete an ordinary (incremental loader)
 * daemon; GrouperSystem adds and deletes the script daemon.  GrouperDaemonCodeAuthorizationTest
 * calls insertConfig/editConfig/deleteConfig directly; this test covers the UI screens (error
 * shown, no scheduler change), checked with SQL on grouper_config and the quartz tables.  The
 * add-daemon dropdown check is mainline only and not covered here.
 *
 * <p>Setup (this test's own, removed at the end): folder test:secPatch25 with group
 * delegatedAdmins (member test.subject.0, not wheel), uiV2.admin.daemonJobs.must.be.in.group set to
 * it in the database config (prior value restored), script daemon OTHER_JOB_secPatch25root added
 * by GrouperSystem through the UI (harmless script, cron in 2099; no daemon JVM runs locally, so a
 * run-now trigger never fires).  Daemons this test may leave are removed in finally.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class DaemonCodeAuthorizationHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch25";

  /** the delegated daemon admin group */
  private static final String DELEGATES_GROUP_NAME = STEM_NAME + ":delegatedAdmins";

  /** delegated daemon admin, not wheel */
  private static final String DELEGATE = "test.subject.0";

  /** script daemon a sysadmin made */
  private static final String ROOT_SCRIPT_ID = "secPatch25root";

  /** script daemon the delegate tries to add */
  private static final String PROOF_SCRIPT_ID = "secPatch25proof";

  /** ordinary daemon the delegate adds and deletes */
  private static final String INCREMENTAL_ID = "secPatch25inc";

  /** cron that will not fire */
  private static final String CRON = "0 0 0 1 1 ? 2099";

  /** a different schedule, for the delegate's schedule-only edit */
  private static final String CRON_OTHER = "0 0 0 2 1 ? 2099";

  /** sysadmin's harmless script */
  private static final String ROOT_SOURCE = "edu.internet2.middleware.grouper.app.loader.OtherJobScript"
      + ".retrieveHib3GrouperLoaderLogNotNull().setJobMessage(\"secPatch25 harmless\");";

  /** the md's proof script */
  private static final String PROOF_SOURCE = "def effectiveSubject = edu.internet2.middleware.grouper.GrouperSession"
      + ".staticGrouperSession().getSubject().getId()\n"
      + "edu.internet2.middleware.grouper.app.loader.OtherJobScript.retrieveHib3GrouperLoaderLogNotNull()"
      + ".setJobMessage(\"EFFECTIVE_SUBJECT=\" + effectiveSubject)";

  /** the refusal on add and edit (GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE) */
  private static final String NOT_ALLOWED = "Only a Grouper sysadmin (wheel or root) can add or delete a daemon, or change settings other than its schedule";

  /** UI config key of the delegated daemon admin group */
  private static final String MUST_BE_IN_GROUP_KEY = "uiV2.admin.daemonJobs.must.be.in.group";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(DaemonCodeAuthorizationHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public DaemonCodeAuthorizationHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public DaemonCodeAuthorizationHttpTest(String name) {
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

    /** Authorization header value when the UI uses basic auth (2.6.19), else null (form login) */
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
      if (response.statusCode() == 401
          && response.headers().firstValue("WWW-Authenticate").orElse("").toLowerCase().startsWith("basic")) {
        // 2.6.19 has no form login (GrouperUiFormLogin is 4.x+); its local UI runs with
        // grouper.is.ui.basicAuthn=true, which checks the same UI password on every request
        this.basicAuthorization = "Basic " + java.util.Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(StandardCharsets.UTF_8));
        response = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/UiV2Main.index")).GET());
        // only a bad password is 401 (a refused section comes later, not as 401)
        assertTrue("basic auth login of " + username + ": " + describe(response), response.statusCode() != 401);
      } else {
        // a good login is a post/redirect/get; a bad one shows the form again (200)
        assertEquals("form login of " + username + " must redirect (on 2.6.19 set grouper.is.ui.basicAuthn=true): "
            + response.body(), 302, response.statusCode());
      }

      SimpleHttpResponse csrfJs = send(SimpleHttpRequest.newBuilder(
          URI.create(this.baseUrl + "/grouperExternal/public/OwaspJavaScriptServlet")).GET());
      Matcher matcher = Pattern.compile("masterTokenValue = '([^']+)'").matcher(csrfJs.body());
      assertTrue("CSRF token for " + username, matcher.find());
      this.csrfToken = matcher.group(1);
    }

    /**
     * ajax POST with the CSRF header and a form body, like the UI's own requests
     * @param path
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
        // basic auth (2.6.19) is checked on every request, not kept by the session cookie
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
    // the ajax json starts with the logged in user's settings and ends with screens: show the messages
    List<String> messages = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\"message\":\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(body);
    while (matcher.find()) {
      messages.add(matcher.group(1));
    }
    return response.statusCode() + " messages: " + messages + " body: "
        + (body.length() <= 1000 ? body : body.substring(body.length() - 1000));
  }

  // ---------------------------------------------------------------- ui config

  /** database value of the must-be-in-group key before the test, null if there was no row */
  private String priorMustBeInGroup = null;

  /** true if there was a row before the test */
  private boolean hadMustBeInGroupRow = false;

  /**
   * point the daemon screens at this test's delegates group (database config, grouper-ui.properties)
   */
  private void setMustBeInGroup() {
    List<String> values = new GcDbAccess().sql("select config_value from grouper_config where "
        + "config_file_name = 'grouper-ui.properties' and config_key = ?").addBindVar(MUST_BE_IN_GROUP_KEY).selectList(String.class);
    this.hadMustBeInGroupRow = values.size() > 0;
    this.priorMustBeInGroup = this.hadMustBeInGroupRow ? values.get(0) : null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new GrouperDbConfig().configFileName("grouper-ui.properties").propertyName(MUST_BE_IN_GROUP_KEY)
          .value(DELEGATES_GROUP_NAME).store();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * put the must-be-in-group key back the way it was
   */
  private void restoreMustBeInGroup() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      if (this.hadMustBeInGroupRow) {
        new GrouperDbConfig().configFileName("grouper-ui.properties").propertyName(MUST_BE_IN_GROUP_KEY)
            .value(this.priorMustBeInGroup).store();
      } else {
        new GrouperDbConfig().configFileName("grouper-ui.properties").propertyName(MUST_BE_IN_GROUP_KEY).delete();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- db helpers

  /**
   * @param configId
   * @return the grouper-loader.properties keys stored for otherJob.configId.*
   */
  private static List<String> daemonConfigKeys(String configId) {
    return new GcDbAccess().sql("select config_key from grouper_config where config_file_name = 'grouper-loader.properties' "
        + "and config_key like ?").addBindVar("otherJob." + configId + ".%").selectList(String.class);
  }

  /**
   * @param configId
   * @return the stored script source, or null
   */
  private static String storedScriptSource(String configId) {
    List<String> values = new GcDbAccess().sql("select coalesce(config_value, config_value_clob) from grouper_config "
        + "where config_file_name = 'grouper-loader.properties' and config_key = ?")
        .addBindVar("otherJob." + configId + ".scriptSource").selectList(String.class);
    return values.size() == 0 ? null : values.get(0);
  }

  /**
   * @param jobName
   * @return true if the job is in the quartz store
   */
  private static boolean jobScheduled(String jobName) {
    return new GcDbAccess().sql("select count(*) from grouper_qz_job_details where job_name = ?")
        .addBindVar(jobName).select(int.class) > 0;
  }

  /**
   * @param jobName
   * @return trigger states of the job's triggers
   */
  private static List<String> triggerStates(String jobName) {
    return new GcDbAccess().sql("select trigger_state from grouper_qz_triggers where job_name = ?")
        .addBindVar(jobName).selectList(String.class);
  }

  // ---------------------------------------------------------------- ui helpers

  /**
   * @param configId
   * @param source
   * @return the add/edit form fields of a gsh script daemon
   */
  private static String scriptDaemonFields(String configId, String source) {
    return "daemonConfigType=" + enc(GrouperDaemonOtherJobScriptConfiguration.class.getName())
        + "&daemonConfigId=" + enc(configId)
        + "&daemonConfigEnable=true"
        + "&config_quartzCron=" + enc(CRON)
        + "&config_scriptType=gsh"
        + "&config_fileType=script"
        + "&config_scriptSource=" + enc(source);
  }

  /**
   * @param cron
   * @return the add form fields of an incremental loader daemon
   */
  private static String incrementalDaemonFields(String cron) {
    return "daemonConfigType=" + enc(GrouperDaemonOtherJobLoaderIncrementalConfiguration.class.getName())
        + "&daemonConfigId=" + enc(INCREMENTAL_ID)
        + "&daemonConfigEnable=true"
        + "&config_quartzCron=" + enc(cron)
        + "&config_tableName=secPatch25_incremental";
  }

  /**
   * @param session
   * @param action runNow, disable, enable
   * @param jobName
   * @return the daemon list after the action
   * @throws Exception
   */
  private static SimpleHttpResponse jobAction(UiSession session, String action, String jobName) throws Exception {
    return session.postAjax("/grouperUi/app/UiV2Admin.daemonJobsSubmit", "action=" + enc(action) + "&jobName=" + enc(jobName));
  }

  /**
   * @param response
   * @return true if the response has an error message
   */
  private static boolean hasError(SimpleHttpResponse response) {
    return response.body().contains("\"messageType\":\"error\"");
  }

  /**
   * @param response
   * @param what
   */
  private static void assertSuccessMessage(SimpleHttpResponse response, String what) {
    assertEquals(what + ": " + describe(response), 200, response.statusCode());
    assertTrue(what + " shows a success message: " + describe(response),
        response.body().contains("\"messageType\":\"success\""));
    assertFalse(what + " no error: " + describe(response), hasError(response));
  }

  /**
   * delete a daemon as GrouperSystem through the UI if it is still there (config or quartz job),
   * then through the API for any config left
   * @param root
   * @param configId
   */
  private static void removeDaemon(UiSession root, String configId) {
    String jobName = "OTHER_JOB_" + configId;
    try {
      if (root != null && (daemonConfigKeys(configId).size() > 0 || jobScheduled(jobName))) {
        root.postAjax("/grouperUi/app/UiV2Admin.deleteDaemon", "jobName=" + enc(jobName));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (daemonConfigKeys(configId).size() > 0) {
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        GrouperDaemonOtherJobScriptConfiguration config = new GrouperDaemonOtherJobScriptConfiguration();
        config.setConfigId(configId);
        config.deleteConfig(false);
      } catch (RuntimeException re) {
        re.printStackTrace();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
    }
  }

  /**
   * delete the delegates group and folder
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, DELEGATES_GROUP_NAME, false);
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

  // ---------------------------------------------------------------- test

  /**
   * delegate refused on script daemon add, edit, delete; still runs, disables, enables it and
   * manages an ordinary daemon; GrouperSystem manages the script daemon
   * @throws Exception
   */
  public void testDelegateCannotAuthorScriptDaemon() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    String rootJob = "OTHER_JOB_" + ROOT_SCRIPT_ID;
    String proofJob = "OTHER_JOB_" + PROOF_SCRIPT_ID;
    String incrementalJob = "OTHER_JOB_" + INCREMENTAL_ID;
    UiSession root = null;
    deleteData();
    try {
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group delegates = new GroupSave(rootSession).assignName(DELEGATES_GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        delegates.addMember(SubjectFinder.findById(DELEGATE, true), false);
        String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
        assertFalse("the delegate must not be wheel",
            GroupFinder.findByName(rootSession, wheelGroupName, true).hasMember(SubjectFinder.findById(DELEGATE, true)));
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      setMustBeInGroup();

      root = new UiSession(baseUrl);
      root.login("GrouperSystem", "pass");
      UiSession delegate = new UiSession(baseUrl);
      delegate.login(DELEGATE, "pass");

      // clean up a daemon left by an earlier failed run
      for (String configId : new String[] {ROOT_SCRIPT_ID, PROOF_SCRIPT_ID, INCREMENTAL_ID}) {
        removeDaemon(root, configId);
      }

      // the running UI sees the must-be-in-group key through the database config cache: wait
      // until the delegate gets the daemon screen
      SimpleHttpResponse daemonScreen = null;
      for (int i = 0; i < 20; i++) {
        daemonScreen = delegate.postAjax("/grouperUi/app/UiV2Admin.daemonJobs", "");
        if (daemonScreen.body().contains("daemonJobsResultsId")) {
          break;
        }
        Thread.sleep(3000);
      }
      assertTrue("delegate gets the daemon screen: " + describe(daemonScreen), daemonScreen.body().contains("daemonJobsResultsId"));

      // setup (control): GrouperSystem adds the script daemon through the UI, which schedules it
      SimpleHttpResponse rootAdd = root.postAjax("/grouperUi/app/UiV2Admin.addDaemonSubmit",
          scriptDaemonFields(ROOT_SCRIPT_ID, ROOT_SOURCE));
      assertSuccessMessage(rootAdd, "GrouperSystem adds the script daemon");
      assertEquals("script saved", ROOT_SOURCE, storedScriptSource(ROOT_SCRIPT_ID));
      assertTrue("script daemon scheduled", jobScheduled(rootJob));

      // step 1 (attack): the delegate adds the md's delegatedRootProof script daemon
      SimpleHttpResponse proofAdd = delegate.postAjax("/grouperUi/app/UiV2Admin.addDaemonSubmit",
          scriptDaemonFields(PROOF_SCRIPT_ID, PROOF_SOURCE));
      assertTrue("delegate add refused: " + describe(proofAdd), proofAdd.body().contains(NOT_ALLOWED));
      assertFalse("delegate add no success", proofAdd.body().contains("\"messageType\":\"success\""));
      assertEquals("nothing saved for the proof daemon", 0, daemonConfigKeys(PROOF_SCRIPT_ID).size());
      assertFalse("proof daemon not scheduled", jobScheduled(proofJob));

      // step 2 (attack): the delegate edits the sysadmin's script
      SimpleHttpResponse edit = delegate.postAjax("/grouperUi/app/UiV2Admin.editDaemonSubmit",
          "previousJobName=" + enc(rootJob) + "&" + scriptDaemonFields(ROOT_SCRIPT_ID, PROOF_SOURCE));
      assertTrue("delegate edit refused: " + describe(edit), edit.body().contains(NOT_ALLOWED));
      assertFalse("delegate edit no success", edit.body().contains("\"messageType\":\"success\""));
      assertEquals("script unchanged", ROOT_SOURCE, storedScriptSource(ROOT_SCRIPT_ID));
      assertTrue("still scheduled after the edit", jobScheduled(rootJob));

      // step 3 (attack): the delegate deletes it (generic error, the exception is not caught)
      SimpleHttpResponse delete = delegate.postAjax("/grouperUi/app/UiV2Admin.deleteDaemon", "jobName=" + enc(rootJob));
      assertFalse("delegate delete no success: " + describe(delete), delete.body().contains("\"messageType\":\"success\""));
      assertTrue("delegate delete shows an error: " + describe(delete), hasError(delete));
      assertEquals("script still there", ROOT_SOURCE, storedScriptSource(ROOT_SCRIPT_ID));
      assertTrue("job still scheduled after the delete", jobScheduled(rootJob));

      // step 4 (control): run, disable, enable the script daemon
      SimpleHttpResponse run = jobAction(delegate, "runNow", rootJob);
      assertTrue("run now: " + describe(run), run.statusCode() == 200 && !hasError(run) && run.body().contains("daemonJobsResultsId"));
      SimpleHttpResponse disable = jobAction(delegate, "disable", rootJob);
      assertSuccessMessage(disable, "disable");
      List<String> disabledStates = triggerStates(rootJob);
      assertTrue("triggers paused after disable: " + disabledStates, disabledStates.size() > 0 && disabledStates.contains("PAUSED"));
      SimpleHttpResponse enable = jobAction(delegate, "enable", rootJob);
      assertSuccessMessage(enable, "enable");
      List<String> enabledStates = triggerStates(rootJob);
      assertFalse("no paused trigger after enable: " + enabledStates, enabledStates.contains("PAUSED"));

      // step 4: an ordinary daemon (incremental loader).  The delegate may not add or delete one
      // (only wheel/root can, since daemons run as GrouperSystem) but may change its schedule
      SimpleHttpResponse incAdd = delegate.postAjax("/grouperUi/app/UiV2Admin.addDaemonSubmit", incrementalDaemonFields(CRON));
      assertTrue("delegate add of an incremental loader refused: " + describe(incAdd), incAdd.body().contains(NOT_ALLOWED));
      assertFalse("delegate add no success", incAdd.body().contains("\"messageType\":\"success\""));
      assertEquals("nothing saved for the incremental loader", 0, daemonConfigKeys(INCREMENTAL_ID).size());

      // control: GrouperSystem adds it
      SimpleHttpResponse rootIncAdd = root.postAjax("/grouperUi/app/UiV2Admin.addDaemonSubmit", incrementalDaemonFields(CRON));
      assertSuccessMessage(rootIncAdd, "GrouperSystem adds an incremental loader daemon");
      assertTrue("incremental loader saved", daemonConfigKeys(INCREMENTAL_ID).contains("otherJob." + INCREMENTAL_ID + ".tableName"));
      assertTrue("incremental loader scheduled", jobScheduled(incrementalJob));

      // the delegate changes only its schedule: allowed
      SimpleHttpResponse incSchedule = delegate.postAjax("/grouperUi/app/UiV2Admin.editDaemonSubmit",
          "previousJobName=" + enc(incrementalJob) + "&" + incrementalDaemonFields(CRON_OTHER));
      assertSuccessMessage(incSchedule, "delegate changes the incremental loader's schedule");
      assertTrue("new schedule saved", new GcDbAccess().sql("select count(*) from grouper_config where config_key = ? and config_value = ?")
          .addBindVar("otherJob." + INCREMENTAL_ID + ".quartzCron").addBindVar(CRON_OTHER).select(int.class) > 0);

      // the delegate deletes it: refused, still there
      SimpleHttpResponse incDelete = delegate.postAjax("/grouperUi/app/UiV2Admin.deleteDaemon", "jobName=" + enc(incrementalJob));
      assertFalse("delegate delete of the incremental loader no success: " + describe(incDelete), incDelete.body().contains("\"messageType\":\"success\""));
      assertTrue("incremental loader still saved", daemonConfigKeys(INCREMENTAL_ID).size() > 0);

      // control: GrouperSystem deletes it
      SimpleHttpResponse rootIncDelete = root.postAjax("/grouperUi/app/UiV2Admin.deleteDaemon", "jobName=" + enc(incrementalJob));
      assertSuccessMessage(rootIncDelete, "GrouperSystem deletes the incremental loader daemon");
      assertEquals("incremental loader config gone", 0, daemonConfigKeys(INCREMENTAL_ID).size());
      assertFalse("incremental loader unscheduled", jobScheduled(incrementalJob));

      // control: GrouperSystem deletes the script daemon through the UI
      SimpleHttpResponse rootDelete = root.postAjax("/grouperUi/app/UiV2Admin.deleteDaemon", "jobName=" + enc(rootJob));
      assertSuccessMessage(rootDelete, "GrouperSystem deletes the script daemon");
      assertNull("script config gone", storedScriptSource(ROOT_SCRIPT_ID));
      assertFalse("script daemon unscheduled", jobScheduled(rootJob));

    } finally {
      try {
        for (String configId : new String[] {ROOT_SCRIPT_ID, PROOF_SCRIPT_ID, INCREMENTAL_ID}) {
          removeDaemon(root, configId);
        }
      } finally {
        try {
          restoreMustBeInGroup();
        } finally {
          deleteData();
        }
      }
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
