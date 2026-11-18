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

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.reports.GroupMembersExportRequestLimiter;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigService;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M3 (groupExportEmailReportConfigCreation), HTTP check against the running
 * local UI: UiV2GroupImport.groupExportEmailSubmit (READ on the group is enough) is limited to
 * one pending export per requester per group, and the export job it registers is one shot.
 * GroupMembersExportRequestLimiterTest covers GroupMembersExportRequestLimiter; this test covers
 * what it cannot: the real UI handler wiring (success text, refusal message, no new report
 * config) and the one-shot job running in a scheduler and leaving nothing behind.</p>
 *
 * <p>The attack: a READ user submits the emailed export of the same group again right away.
 * Refused with the pending message; no second report config and no second job.  The positive
 * control: the first submit shows the normal success text, saves one report config and one non
 * durable job, and that job, run once, emails the download link and is then gone.</p>
 *
 * <p>The job is NOT run in the clustered Quartz store shared with Tomcat: the UI registers it there
 * (no scheduler is started against that store locally), the test checks the stored row is not
 * durable and deletes it, then registers the same one-shot job (same name, same code path,
 * GroupMembersExportRequestLimiter.scheduleOneShot) in an in-memory RAMJobStore scheduler in this
 * JVM, with mail.smtp.server=testing in this JVM only so the email can be read.</p>
 *
 * <p>Setup (this test's own, removed at the end): folder secPatch65 with group export65a,
 * test.subject.0 (not wheel locally) READ, test.subject.9 a member.  Reports are on locally
 * (grouperReporting.enable in grouper.properties); the test fails if the UI says they are off.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class UiV2GroupExportEmailHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch65";

  /** exported group */
  private static final String GROUP_NAME = STEM_NAME + ":export65a";

  /** requester with READ (not wheel locally) */
  private static final String REQUESTER = "test.subject.0";

  /** a member, so the export has a row */
  private static final String MEMBER = "test.subject.9";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2GroupExportEmailHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2GroupExportEmailHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2GroupExportEmailHttpTest(String name) {
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
   * @param groupId
   * @return attribute assign ids of the report configs (reportConfigMarker) on the group (SQL)
   */
  static List<String> reportConfigMarkerIds(String groupId) {
    return new GcDbAccess().sql("select aa.id from grouper_attribute_assign aa, grouper_attribute_def_name n "
        + "where aa.attribute_def_name_id = n.id and n.name = ? and aa.owner_group_id = ? and aa.enabled = 'T'")
        .addBindVar(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc")
            + ":reportConfig:reportConfigMarker").addBindVar(groupId).selectList(String.class);
  }

  /**
   * @param groupId
   * @return attribute assign ids of the report instances (reportInstanceMarker) on the group (SQL)
   */
  static List<String> reportInstanceMarkerIds(String groupId) {
    return new GcDbAccess().sql("select aa.id from grouper_attribute_assign aa, grouper_attribute_def_name n "
        + "where aa.attribute_def_name_id = n.id and n.name = ? and aa.owner_group_id = ? and aa.enabled = 'T'")
        .addBindVar(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc")
            + ":reportConfig:reportInstanceMarker").addBindVar(groupId).selectList(String.class);
  }

  /**
   * @param groupId
   * @return {job_name, is_durable} of the export jobs of the group in the shared (clustered) quartz store (SQL)
   */
  static List<Object[]> sharedStoreJobs(String groupId) {
    return new GcDbAccess().sql("select job_name, cast(is_durable as varchar(5)) from grouper_qz_job_details where job_name like ?")
        .addBindVar("grouper_report_" + groupId + "_%").selectList(Object[].class);
  }

  /**
   * delete the group's export jobs from the shared quartz store (the UI registered them there;
   * no scheduler runs them locally).  Only rows of this test's group.
   * @param groupId
   */
  static void deleteSharedStoreJobs(String groupId) {
    String jobPattern = "grouper_report_" + groupId + "_%";
    new GcDbAccess().sql("delete from grouper_qz_simple_triggers where trigger_name like ?")
        .addBindVar("triggerOneShot_" + jobPattern).executeSql();
    new GcDbAccess().sql("delete from grouper_qz_triggers where job_name like ?").addBindVar(jobPattern).executeSql();
    new GcDbAccess().sql("delete from grouper_qz_job_details where job_name like ?").addBindVar(jobPattern).executeSql();
  }

  /**
   * delete the jobs, loader log rows, stored report files, the group and the folder
   */
  static void cleanUpExport(String stemName, String groupName, String groupExtension) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, groupName, false);
      if (group != null) {
        deleteSharedStoreJobs(group.getId());
        new GcDbAccess().sql("delete from grouper_loader_log where job_name like ?")
            .addBindVar("grouper_report_" + group.getId() + "_%").executeSql();
        group.delete();
      }
      new GcDbAccess().sql("delete from grouper_file where system_name = 'report' and file_name like ?")
          .addBindVar("groupMembersExport_" + groupExtension + "_%").executeSql();
      Stem stem = StemFinder.findByName(rootSession, stemName, false);
      if (stem != null) {
        stem.delete();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * <p>Run the export's one-shot job once in an in-memory scheduler in this JVM (never the clustered
   * store shared with Tomcat): swap GrouperLoader.schedulerFactory for a RAMJobStore factory,
   * register the job with GroupMembersExportRequestLimiter.scheduleOneShot (the UI's code path),
   * wait until Quartz has removed it, restore the factory.  Mail goes to the in-memory testing list
   * (mail.smtp.server=testing in this JVM only).</p>
   * @param groupName
   * @param markerId report config marker assign id
   * @return emails sent while the job ran
   * @throws Exception
   */
  static List<GrouperEmail> runExportJobOnce(String groupName, String markerId) throws Exception {
    Field schedulerFactoryField = GrouperLoader.class.getDeclaredField("schedulerFactory");
    schedulerFactoryField.setAccessible(true);
    Object originalSchedulerFactory = schedulerFactoryField.get(null);
    String originalSmtpServer = GrouperConfig.retrieveConfig().propertiesOverrideMap().get("mail.smtp.server");
    Scheduler scheduler = null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("mail.smtp.server", "testing");
      int emailsBefore = GrouperEmail.testingEmails().size();

      Properties quartzProperties = new Properties();
      quartzProperties.setProperty("org.quartz.scheduler.instanceName", "secPatchExportScheduler");
      quartzProperties.setProperty("org.quartz.scheduler.instanceId", "secPatchExport");
      quartzProperties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
      quartzProperties.setProperty("org.quartz.threadPool.threadCount", "1");
      quartzProperties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
      SchedulerFactory testSchedulerFactory = new StdSchedulerFactory(quartzProperties);
      scheduler = testSchedulerFactory.getScheduler();
      schedulerFactoryField.set(null, testSchedulerFactory);

      Group group = GroupFinder.findByName(rootSession, groupName, true);
      GrouperReportConfigurationBean configBean = GrouperReportConfigService.getGrouperReportConfigBean(markerId);
      assertNotNull("export config " + markerId, configBean);
      JobKey jobKey = JobKey.jobKey("grouper_report_" + group.getId() + "_" + markerId);

      GroupMembersExportRequestLimiter.scheduleOneShot(group, configBean);
      assertTrue("job registered", scheduler.checkExists(jobKey));
      scheduler.start();

      // one shot, not durable: Quartz removes the job after it runs
      for (int i = 0; i < 60 && scheduler.checkExists(jobKey); i++) {
        Thread.sleep(1000);
      }
      assertFalse("one-shot job removed after its run (not durable)", scheduler.checkExists(jobKey));
      // the job thread finishes the email and instance save after the trigger completes; wait for the instance
      for (int i = 0; i < 30 && reportInstanceMarkerIds(group.getId()).size() == 0; i++) {
        Thread.sleep(1000);
      }
      Thread.sleep(2000);
      List<GrouperEmail> emails = new ArrayList<GrouperEmail>(GrouperEmail.testingEmails());
      return emails.subList(Math.min(emailsBefore, emails.size()), emails.size());
    } finally {
      if (scheduler != null) {
        scheduler.shutdown(true);
      }
      schedulerFactoryField.set(null, originalSchedulerFactory);
      if (originalSmtpServer == null) {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("mail.smtp.server");
      } else {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put("mail.smtp.server", originalSmtpServer);
      }
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * first emailed export accepted (one config, one non durable job that runs once and is gone);
   * a repeat right away refused with no new config or job
   * @throws Exception
   */
  public void testEmailExportLimitedAndOneShot() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUpExport(STEM_NAME, GROUP_NAME, "export65a");
    try {
      String groupId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        group.grantPriv(SubjectFinder.findById(REQUESTER, true), AccessPrivilege.READ, false);
        group.addMember(SubjectFinder.findById(MEMBER, true), false);
        groupId = group.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      String exportBody = "groupId=" + enc(groupId) + "&group-export-options=ids";

      UiSession requester = new UiSession(baseUrl);
      requester.login(REQUESTER, "pass");

      // ---- positive control: the first emailed export
      SimpleHttpResponse first = requester.postAjax("/grouperUi/app/UiV2GroupImport.groupExportEmailSubmit", exportBody);
      String firstHtml = unjson(first.body());
      assertTrue("first export accepted with the normal text: " + describe(first), firstHtml.contains("\"messageType\":\"success\"")
          && firstHtml.contains("Your members export has started"));
      List<String> markers = reportConfigMarkerIds(groupId);
      assertEquals("one report config on the group", 1, markers.size());
      List<Object[]> jobs = sharedStoreJobs(groupId);
      assertEquals("one export job registered by the UI", 1, jobs.size());
      assertEquals("UI registered job name", "grouper_report_" + groupId + "_" + markers.get(0), jobs.get(0)[0]);
      assertFalse("UI registered job is not durable", "true".equals(jobs.get(0)[1]));

      // ---- attack: the same export again right away
      SimpleHttpResponse second = requester.postAjax("/grouperUi/app/UiV2GroupImport.groupExportEmailSubmit", exportBody);
      String secondHtml = unjson(second.body());
      assertTrue("repeat refused: " + describe(second), secondHtml.contains("\"messageType\":\"error\"")
          && secondHtml.contains("An export of this group was requested in the last hour"));
      assertFalse("repeat shows no success", secondHtml.contains("\"messageType\":\"success\""));
      assertEquals("no new report config", markers, reportConfigMarkerIds(groupId));
      assertEquals("no new job", 1, sharedStoreJobs(groupId).size());

      // ---- the one-shot job: move it out of the shared store and run it once in this JVM
      deleteSharedStoreJobs(groupId);
      List<GrouperEmail> emails = runExportJobOnce(GROUP_NAME, markers.get(0));
      assertEquals("one report instance saved", 1, reportInstanceMarkerIds(groupId).size());
      assertEquals("one email: " + emails.size(), 1, emails.size());
      assertTrue("email to the requester: " + emails.get(0).getTo(), emails.get(0).getTo().contains(REQUESTER));
      assertTrue("email has the download link: " + emails.get(0).getBody(),
          emails.get(0).getBody().contains("UiV2GroupImport.groupExportDownload?attributeAssignId="));
      assertEquals("nothing left in the shared store", 0, sharedStoreJobs(groupId).size());

    } finally {
      cleanUpExport(STEM_NAME, GROUP_NAME, "export65a");
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
