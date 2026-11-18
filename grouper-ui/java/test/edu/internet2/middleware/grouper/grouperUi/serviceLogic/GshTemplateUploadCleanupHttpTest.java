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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory C3 (temporaryUploadFilesNotCleaned), HTTP check against the running local UI: the copies the
 * UI makes of an upload under java.io.tmpdir are deleted again.
 * <ul>
 *   <li>GSH template: a real multipart POST to UiV2Template.customTemplateExecute for a V1 template with one
 *   file input read as a string.  After the run ("Finished") no copy of the upload is left.  When the user
 *   may not run the template (securityRunType=wheel) nothing is left either.</li>
 *   <li>Loader config import: a real multipart POST to UiV2GrouperLoader.importLoaderConfigSubmit (as
 *   GrouperSystem) with a file that is not JSON is rejected ("Invalid json input") and leaves no copy.</li>
 * </ul>
 * GshTemplateUploadCleanupTest checks the cleanup methods themselves (also a failed write, a file input after
 * its run, inputs hidden by showEl); this test covers the wiring through the UI filter chain.
 *
 * <p>Runs only with junit.test.tomcat=true.  Tomcat and this JVM run as the same OS user with no
 * java.io.tmpdir override, so they share java.io.tmpdir.  Extends TestCase (not GrouperTest, which resets the
 * database the running UI uses).</p>
 *
 * <p>Setup (this test's own, removed at the end): folder test:secPatch145 (test.subject.0 CREATE, so the user
 * can find it), group test:secPatch145:loaderImport, and the GSH template config grouperGshTemplate.secPatch145.*
 * (database config, grouper.properties).  Uploads use unique names per run; any copy found is deleted at the
 * end.</p>
 */
public class GshTemplateUploadCleanupHttpTest extends TestCase {

  /** folder this test creates and deletes, the template owner */
  private static final String STEM_NAME = "test:secPatch145";

  /** group whose loader config import is tried */
  private static final String GROUP_NAME = STEM_NAME + ":loaderImport";

  /** template config id */
  private static final String TEMPLATE_ID = "secPatch145";

  /** user running the template: not wheel */
  private static final String USER = "test.subject.0";

  /** file input name */
  private static final String INPUT_NAME = "gsh_input_upload";

  /** template config: a V1 template with one file input read as a string */
  private static final String[][] TEMPLATE_CONFIG = {
    {"enabled", "true"},
    {"templateType", "gsh"},
    {"templateName", "secPatch145 upload cleanup test"},
    {"templateDescription", "security advisory C3 http test"},
    {"runAsType", "currentUser"},
    {"securityRunType", "everyone"},
    {"showOnFolders", "true"},
    {"folderShowType", "allFolders"},
    {"gshTemplate", "gsh_builtin_gshTemplateOutput.addOutputLine(\"Finished\");"},
    {"numberOfInputs", "1"},
    {"input.0.name", INPUT_NAME},
    {"input.0.label", "Upload"},
    {"input.0.description", "Harmless test file"},
    {"input.0.type", "string"},
    {"input.0.formElementType", "file"},
    {"input.0.required", "true"},
    {"input.0.maxLength", "1000"},
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GshTemplateUploadCleanupHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GshTemplateUploadCleanupHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateUploadCleanupHttpTest(String name) {
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
    // the ajax json starts with the logged in user's settings and ends with screens: show the messages
    List<String> messages = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\"message\":\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(body);
    while (matcher.find()) {
      messages.add(matcher.group(1));
    }
    return response.statusCode() + " messages: " + messages + " body: "
        + (body.length() <= 1000 ? body : body.substring(body.length() - 1000));
  }

  // ---------------------------------------------------------------- multipart

  /**
   * POST a multipart form like guiSubmitFileForm (jquery.form, XHR with the CSRF header)
   * @param session
   * @param path
   * @param fields plain fields
   * @param fileField name of the file part
   * @param fileName client file name, sent as is
   * @param content file content
   * @return the response
   * @throws Exception
   */
  private static SimpleHttpResponse postMultipart(UiSession session, String path, Map<String, String> fields,
      String fileField, String fileName, String content) throws Exception {
    String boundary = "----secPatch145" + UUID.randomUUID().toString().replace("-", "");
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    StringBuilder text = new StringBuilder();
    for (Map.Entry<String, String> field : fields.entrySet()) {
      text.append("--").append(boundary).append("\r\n")
          .append("Content-Disposition: form-data; name=\"").append(field.getKey()).append("\"\r\n\r\n")
          .append(field.getValue()).append("\r\n");
    }
    text.append("--").append(boundary).append("\r\n")
        .append("Content-Disposition: form-data; name=\"").append(fileField).append("\"; filename=\"").append(fileName).append("\"\r\n")
        .append("Content-Type: text/plain\r\n\r\n")
        .append(content).append("\r\n")
        .append("--").append(boundary).append("--\r\n");
    body.write(text.toString().getBytes(StandardCharsets.UTF_8));
    return session.send(SimpleHttpRequest.newBuilder(URI.create(session.baseUrl + path))
        .header("X-Requested-With", "XMLHttpRequest")
        .header("OWASPCSRFTOKEN", session.csrfToken)
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .POST(SimpleHttpRequest.ofByteArray(body.toByteArray())));
  }

  // ---------------------------------------------------------------- files

  /**
   * @return java.io.tmpdir of this JVM (the same as Tomcat's locally)
   */
  private static File tmpDir() {
    return new File(System.getProperty("java.io.tmpdir"));
  }

  /**
   * @param name file name
   * @return every file with that name directly in java.io.tmpdir or in any directory directly under it (the
   * template uploads use grouperGshTemplateUpload* directories, the loader import random ones)
   */
  private static List<File> findCopies(String name) {
    List<File> result = new ArrayList<File>();
    File direct = new File(tmpDir(), name);
    if (direct.exists()) {
      result.add(direct);
    }
    File[] children = tmpDir().listFiles();
    if (children != null) {
      for (File child : children) {
        if (child.isDirectory()) {
          File inside = new File(child, name);
          if (inside.exists()) {
            result.add(inside);
          }
        }
      }
    }
    return result;
  }

  /**
   * if a check failed, delete the copies this run left (by their unique names) and their now empty directories
   * @param names
   */
  private static void deleteCopies(List<String> names) {
    for (String name : names) {
      for (File file : findCopies(name)) {
        File parent = file.getParentFile();
        file.delete();
        if (!parent.equals(tmpDir())) {
          String[] left = parent.list();
          if (left != null && left.length == 0) {
            parent.delete();
          }
        }
      }
    }
  }

  /**
   * a copy may be deleted by a task that finishes just after the response: give it a few seconds
   * @param name
   * @return the copies still there after waiting up to 10 seconds
   * @throws InterruptedException
   */
  private static List<File> copiesAfterCleanup(String name) throws InterruptedException {
    List<File> copies = findCopies(name);
    for (int i = 0; i < 20 && copies.size() > 0; i++) {
      Thread.sleep(500);
      copies = findCopies(name);
    }
    return copies;
  }

  // ---------------------------------------------------------------- config and data

  /**
   * @param key after grouperGshTemplate.secPatch145.
   * @param value
   */
  private static void storeTemplateConfig(String key, String value) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate." + TEMPLATE_ID + "." + key)
          .value(value).store();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the template config, the group and the folder
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String[] keyValue : TEMPLATE_CONFIG) {
        new GrouperDbConfig().configFileName("grouper.properties")
            .propertyName("grouperGshTemplate." + TEMPLATE_ID + "." + keyValue[0]).delete();
      }
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, false);
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

  // ---------------------------------------------------------------- tests

  /**
   * a completed template run and a refused one leave no copy of the upload
   * @throws Exception
   */
  public void testTemplateUploadDeleted() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    String runId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    List<String> uploadedNames = new ArrayList<String>();
    deleteData();
    try {
      String stemId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Stem stem = new StemSave(rootSession).assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
        // the user must be able to find the folder (the run itself needs no folder privilege)
        stem.grantPriv(SubjectFinder.findById(USER, true), NamingPrivilege.CREATE, false);
        stemId = stem.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      for (String[] keyValue : TEMPLATE_CONFIG) {
        storeTemplateConfig(keyValue[0], keyValue[1]);
      }

      UiSession user = new UiSession(baseUrl);
      user.login(USER, "pass");

      String executePath = "/grouperUi/app/UiV2Template.customTemplateExecute?stemId=" + enc(stemId);
      Map<String, String> fields = new LinkedHashMap<String, String>();
      fields.put("stemId", stemId);
      fields.put("templateType", TEMPLATE_ID);
      String fileField = "config_" + INPUT_NAME;

      // 1. a completed run.  The UI sees the new database config within seconds; retry until it does
      SimpleHttpResponse ordinary = null;
      for (int i = 0; i < 20; i++) {
        String name = "secPatch145run-" + runId + "-" + i + ".txt";
        uploadedNames.add(name);
        ordinary = postMultipart(user, executePath, fields, fileField, name, "harmless secPatch145 content");
        if (ordinary.body().contains("Finished")) {
          break;
        }
        Thread.sleep(3000);
      }
      assertTrue("upload runs the template: " + describe(ordinary), ordinary.body().contains("Finished"));
      for (String name : uploadedNames) {
        assertEquals("no copy left after the run (or the tries before it): " + name, 0, copiesAfterCleanup(name).size());
      }

      // 2. a refused run: template now wheel only; wait until the UI refuses
      storeTemplateConfig("securityRunType", "wheel");
      SimpleHttpResponse refused = null;
      String refusedName = null;
      for (int i = 0; i < 20; i++) {
        refusedName = "secPatch145refused-" + runId + "-" + i + ".txt";
        uploadedNames.add(refusedName);
        refused = postMultipart(user, executePath, fields, fileField, refusedName, "harmless secPatch145 content");
        if (!refused.body().contains("Finished")) {
          break;
        }
        Thread.sleep(3000);
      }
      assertFalse("wheel-only template refused for " + USER + ": " + describe(refused), refused.body().contains("Finished"));
      assertEquals("no copy left after a refused run", 0, copiesAfterCleanup(refusedName).size());

    } finally {
      try {
        deleteCopies(uploadedNames);
      } finally {
        deleteData();
      }
    }
  }

  /**
   * a loader config import file that is rejected (not JSON) leaves no copy
   * @throws Exception
   */
  public void testLoaderImportFileDeleted() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    String runId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    List<String> uploadedNames = new ArrayList<String>();
    deleteData();
    try {
      String groupId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        groupId = group.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      UiSession admin = new UiSession(baseUrl);
      admin.login("GrouperSystem", "pass");

      String name = "secPatch145loader-" + runId + ".json";
      uploadedNames.add(name);
      Map<String, String> fields = new LinkedHashMap<String, String>();
      fields.put("groupId", groupId);
      fields.put("importLoaderConfigFormat", "file");
      SimpleHttpResponse response = postMultipart(admin,
          "/grouperUi/app/UiV2GrouperLoader.importLoaderConfigSubmit?groupId=" + enc(groupId),
          fields, "importConfigFile", name, "this is not json {");
      assertTrue("import rejected: " + describe(response), response.body().contains("Invalid json input"));
      assertEquals("no copy left after a rejected import", 0, findCopies(name).size());

    } finally {
      try {
        deleteCopies(uploadedNames);
      } finally {
        deleteData();
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
