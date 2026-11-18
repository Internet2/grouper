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
package edu.internet2.middleware.grouper.j2ee.status;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * <p>/status details (hostname, version, uptime, diagnostic task names, failure text) only go to
 * callers on ws.diagnostic.sourceIpAddresses; when that list is blank (the default) everyone gets
 * the heading and HTTP code only, unless ws.diagnostic.sendDetailsOutsideSourceIpAddresses is
 * true.  Text written into the page is HTML-escaped.</p>
 *
 * <p>Drives GrouperStatusServlet.doGet with java.lang.reflect.Proxy request/response stand-ins and
 * diagnosticType=trivial (memory only, no loader jobs).  Needs the Grouper database (doGet opens
 * a root session).  Before the fix the test does not compile (GrouperStatusServletDetails does not
 * exist); the pre-fix servlet sends "Server: host, grouperVersion: ..." to any caller when the
 * allow list is blank.</p>
 *
 * <p>Goes in grouper/src/test/edu/internet2/middleware/grouper/j2ee/status/.  Same file for every
 * release (Java 8 syntax for 2.6.19).</p>
 */
public class GrouperStatusServletDetailsTest extends TestCase {

  /** config keys this test overrides */
  private static final String[] KEYS = new String[] {
      "ws.diagnostic.sourceIpAddresses",
      "ws.diagnostic.sendDetailsOutsideSourceIpAddresses",
      "ws.diagnostic.sendDetailsInResponse",
      "ws.diagnostic.sendHttpCodeOnly"};

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperStatusServletDetailsTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GrouperStatusServletDetailsTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperStatusServletDetailsTest(String name) {
    super(name);
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    clearOverrides();
    // the defaults this test assumes, whatever the local grouper.properties says
    override("ws.diagnostic.sourceIpAddresses", "");
    override("ws.diagnostic.sendDetailsOutsideSourceIpAddresses", "false");
    override("ws.diagnostic.sendDetailsInResponse", "true");
    override("ws.diagnostic.sendHttpCodeOnly", "false");
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    clearOverrides();
    super.tearDown();
  }

  /**
   * remove this test's overrides
   */
  private static void clearOverrides() {
    for (String key : KEYS) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(key);
    }
  }

  /**
   * @param key
   * @param value
   */
  private static void override(String key, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(key, value);
  }

  // ---------------------------------------------------------------- tests

  /**
   * default config (blank allow list): status still answers, but without the details
   */
  public void testNoDetailsWhenAllowListBlank() {
    StatusCall call = callStatus("trivial", "203.0.113.7");
    assertEquals(call.toString(), 0, call.sendErrorCode);
    assertTrue(call.toString(), call.body.contains("Grouper status"));
    assertFalse("hostname/version must not be sent: " + call, call.body.contains("Server: "));
    assertFalse("hostname/version must not be sent: " + call, call.body.contains("grouperVersion"));
  }

  /**
   * a caller on the allow list gets the details (the trivial check succeeds)
   */
  public void testDetailsForCallerOnAllowList() {
    override("ws.diagnostic.sourceIpAddresses", "10.1.2.0/24");
    StatusCall call = callStatus("trivial", "10.1.2.3");
    assertEquals(call.toString(), 200, call.status);
    assertTrue("details expected: " + call, call.body.contains("Server: "));
  }

  /**
   * a caller off the allow list is still refused (unchanged), control
   */
  public void testCallerOffAllowListRefused() {
    override("ws.diagnostic.sourceIpAddresses", "10.1.2.0/24");
    StatusCall call = callStatus("trivial", "10.9.9.9");
    assertEquals(call.toString(), 401, call.sendErrorCode);
    assertFalse(call.toString(), call.body.contains("Server: "));
  }

  /**
   * the switch that restores the old behavior: details to everyone allowed to call /status
   */
  public void testSendDetailsOutsideSourceIpAddresses() {
    override("ws.diagnostic.sendDetailsOutsideSourceIpAddresses", "true");
    StatusCall call = callStatus("trivial", "203.0.113.7");
    assertEquals(call.toString(), 200, call.status);
    assertTrue("details expected: " + call, call.body.contains("Server: "));
  }

  /**
   * a diagnosticType value is echoed in the error message: it must come back escaped
   */
  public void testInvalidDiagnosticTypeEscaped() {
    override("ws.diagnostic.sourceIpAddresses", "10.1.2.0/24");
    StatusCall call = callStatus("<b id=secPatch195>x</b>", "10.1.2.3");
    assertFalse("markup must not be echoed: " + call, call.body.contains("<b id=secPatch195>"));
    if (call.body.contains("secPatch195")) {
      assertTrue("escaped echo expected: " + call, call.body.contains("&lt;b id=secPatch195&gt;"));
    }
  }

  /**
   * the helper's decision and escaping
   */
  public void testHelper() {
    assertFalse(GrouperStatusServletDetails.detailsAllowedForCaller(fakeRequest(null, "10.1.2.3")));
    override("ws.diagnostic.sourceIpAddresses", "10.1.2.0/24");
    assertTrue(GrouperStatusServletDetails.detailsAllowedForCaller(fakeRequest(null, "10.1.2.3")));
    assertFalse(GrouperStatusServletDetails.detailsAllowedForCaller(fakeRequest(null, "10.1.3.3")));
    assertFalse(GrouperStatusServletDetails.detailsAllowedForCaller(fakeRequest(null, "")));
    assertFalse(GrouperStatusServletDetails.detailsAllowedForCaller(null));
    assertEquals("&lt;a href=&quot;x&quot;&gt;&amp;&#39;", GrouperStatusServletDetails.escapeHtml("<a href=\"x\">&'"));
    assertEquals("", GrouperStatusServletDetails.escapeHtml(null));
  }

  // ---------------------------------------------------------------- helpers

  /**
   * result of one doGet
   */
  static class StatusCall {
    /** setStatus value (0 if never set) */
    int status;
    /** sendError code (0 if never called) */
    int sendErrorCode;
    /** the written body */
    String body = "";

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "status=" + this.status + ", sendError=" + this.sendErrorCode + ", body=" + this.body;
    }
  }

  /**
   * call GrouperStatusServlet.doGet
   * @param diagnosticType
   * @param remoteAddr
   * @return the result
   */
  static StatusCall callStatus(String diagnosticType, String remoteAddr) {
    final StatusCall result = new StatusCall();
    final StringWriter stringWriter = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(stringWriter);
    Map<String, String> params = new HashMap<String, String>();
    params.put("diagnosticType", diagnosticType);
    HttpServletRequest request = fakeRequest(params, remoteAddr);
    HttpServletResponse response = (HttpServletResponse)Proxy.newProxyInstance(
        GrouperStatusServletDetailsTest.class.getClassLoader(), new Class[] {HttpServletResponse.class},
        new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("setStatus".equals(name)) {
              result.status = ((Integer)args[0]).intValue();
              return null;
            }
            if ("sendError".equals(name)) {
              result.sendErrorCode = ((Integer)args[0]).intValue();
              return null;
            }
            if ("getWriter".equals(name)) {
              return printWriter;
            }
            return defaultValue(method.getReturnType());
          }
        });
    new GrouperStatusServlet().doGet(request, response);
    printWriter.flush();
    result.body = stringWriter.toString();
    return result;
  }

  /**
   * @param params request parameters (null for none)
   * @param remoteAddr
   * @return a proxied request
   */
  static HttpServletRequest fakeRequest(final Map<String, String> params, final String remoteAddr) {
    return (HttpServletRequest)Proxy.newProxyInstance(
        GrouperStatusServletDetailsTest.class.getClassLoader(), new Class[] {HttpServletRequest.class},
        new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("getParameter".equals(name)) {
              return params == null ? null : params.get((String)args[0]);
            }
            if ("getRemoteAddr".equals(name)) {
              return remoteAddr;
            }
            return defaultValue(method.getReturnType());
          }
        });
  }

  /**
   * @param type
   * @return null, false or zero
   */
  static Object defaultValue(Class<?> type) {
    if (type == boolean.class) {
      return Boolean.FALSE;
    }
    if (type == int.class) {
      return Integer.valueOf(0);
    }
    if (type == long.class) {
      return Long.valueOf(0);
    }
    return null;
  }
}
