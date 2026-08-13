/*******************************************************************************
 * Copyright 2024 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.mcp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.authentication.GrouperOAuthClient;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthSigningKey;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;

import junit.textui.TestRunner;

/**
 * Tests the MCP servlet's handling of protocol revision 2026-07-28.
 *
 * <p>The rules this covers all sit between a request arriving and a method being
 * dispatched, so they are driven by calling the validation methods directly rather than
 * by going through {@code doPost}. That keeps authentication, the database and tool
 * dispatch out of the way, and it means a failure here names the rule which broke rather
 * than something further down.</p>
 *
 * <p>Requests and responses are stubbed with a proxy over the servlet interfaces rather
 * than a mocking library, since two versions of one are on this classpath and which of
 * them answers is not something a test should depend on.</p>
 *
 * <p>Expected codes and versions are written out as literals rather than read from the
 * servlet's own constants. A test which asks the code what it should say cannot notice
 * the code saying the wrong thing.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpServlet20260728Test extends GrouperTest {

  /** the revision under test */
  private static final String MODERN = "2026-07-28";

  /** the revision this server serves a client which declares no version at all */
  private static final String LEGACY = "2025-03-26";

  private static final int INVALID_REQUEST = -32600;

  private static final int METHOD_NOT_FOUND = -32601;

  private static final int INVALID_PARAMS = -32602;

  private static final int HEADER_MISMATCH = -32020;

  private static final int UNSUPPORTED_PROTOCOL_VERSION = -32022;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** the servlet under test */
  private GrouperMcpServlet servlet;

  /** an OAuth client registered so that authentication can get past its existence check */
  private String clientId;

  /** a bearer token this server will accept, for the subject the tests run as */
  private String bearerToken;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperMcpServlet20260728Test.class);
  }

  /**
   *
   */
  public GrouperMcpServlet20260728Test() {
    super();
  }

  /**
   * @param name
   */
  public GrouperMcpServlet20260728Test(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);
    this.servlet = new GrouperMcpServlet();
    GrouperConfig.retrieveConfig().propertiesOverrideMap()
      .put("grouper.ws.url", "https://server.example.edu/grouper-ws");
  }

  /**
   * make a bearer token this server accepts, and the client registration it insists on.
   *
   * <p>Called only by the tests which go through {@code doPost}, since the ones which
   * drive a validation method do not reach authentication.  The token is minted by the
   * same code which issues one in production, so its issuer and audience come from the
   * same configuration the verification checks them against and cannot drift apart.</p>
   */
  private void setUpAuthentication() {

    this.clientId = "test-client-" + GrouperUtil.uniqueId();

    GrouperOAuthClient client = new GrouperOAuthClient();
    client.setClientId(this.clientId);
    client.setClientName("mcp 2026 test");
    client.setRedirectUris(Collections.singleton("http://localhost:9999/callback"));
    client.setRegisteredMicros(Long.valueOf(System.currentTimeMillis() * 1000L));
    GrouperOAuthStore.registerClient(client);

    this.bearerToken = GrouperOAuthSigningKey.createSignedJwt(
        SubjectTestHelper.SUBJ0_ID, "jdbc", this.clientId,
        "{\"readonly\":true,\"adminReadonly\":true}");
  }

  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.ws.url");
    super.tearDown();
  }

  // ==================== stubs ====================

  /**
   * what a stubbed response recorded
   */
  private static class Recorded {

    /** HTTP status, -1 when the code under test never set one */
    private int status = -1;

    /** headers set on the response */
    private final Map<String, String> headers = new LinkedHashMap<String, String>();

    /** what was written to the response body */
    private final StringWriter body = new StringWriter();

    /**
     * @return the body parsed as JSON, or null when nothing was written
     */
    private JsonNode json() {
      String text = this.body.toString();
      if (text.length() == 0) {
        return null;
      }
      try {
        return objectMapper.readTree(text);
      } catch (Exception e) {
        throw new RuntimeException("response body is not JSON: " + text, e);
      }
    }

    /**
     * @return the JSON-RPC error code in the body
     */
    private int errorCode() {
      JsonNode node = this.json();
      assertNotNull("expected a JSON-RPC error, nothing was written", node);
      assertTrue("expected a JSON-RPC error, got: " + node, node.has("error"));
      return node.get("error").get("code").asInt();
    }

    /**
     * @return the JSON-RPC error message in the body
     */
    private String errorMessage() {
      return this.json().get("error").get("message").asText();
    }
  }

  /**
   * a value for a return type which the stub has no answer for, so that a method the code
   * under test happens to call does not blow up on an unboxed null
   * @param type the return type
   * @return the value
   */
  private static Object defaultFor(Class<?> type) {
    if (!type.isPrimitive()) {
      return null;
    }
    if (type == boolean.class) {
      return Boolean.FALSE;
    }
    if (type == long.class) {
      return Long.valueOf(0);
    }
    if (type == char.class) {
      return Character.valueOf(' ');
    }
    if (type == void.class) {
      return null;
    }
    return Integer.valueOf(0);
  }

  /**
   * a request which answers the headers it was given, and which describes itself as
   * arriving at a host other than the configured one, so that anything reading the
   * request instead of the configuration is visible
   * @param headers header name to value, matched without regard to case
   * @return the request
   */
  private static HttpServletRequest request(final Map<String, String> headers) {
    return request(headers, null);
  }

  /**
   * a request which answers the headers it was given and reads the body it was given
   * @param headers header name to value, matched without regard to case
   * @param body the request body, or null when the code under test will not read one
   * @return the request
   */
  private static HttpServletRequest request(final Map<String, String> headers,
      final String body) {

    InvocationHandler handler = new InvocationHandler() {

      public Object invoke(Object proxy, Method method, Object[] args) {

        String name = method.getName();

        if ("getInputStream".equals(name)) {
          final ByteArrayInputStream bytes = new ByteArrayInputStream(
              (body == null ? "" : body).getBytes(StandardCharsets.UTF_8));
          return new ServletInputStream() {

            @Override
            public int read() {
              return bytes.read();
            }

            @Override
            public boolean isFinished() {
              return bytes.available() == 0;
            }

            @Override
            public boolean isReady() {
              return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
              // nothing reads this asynchronously
            }
          };
        }

        if ("getHeader".equals(name)) {
          String wanted = (String) args[0];
          for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(wanted)) {
              return entry.getValue();
            }
          }
          return null;
        }
        if ("getScheme".equals(name)) {
          return "https";
        }
        if ("getServerName".equals(name)) {
          return "someotherhost.example.edu";
        }
        if ("getServerPort".equals(name)) {
          return Integer.valueOf(443);
        }
        if ("getContextPath".equals(name)) {
          return "/grouper-ws";
        }
        if ("getRemoteAddr".equals(name)) {
          return "10.0.0.1";
        }
        if ("getMethod".equals(name)) {
          return "POST";
        }
        if ("toString".equals(name)) {
          return "stub request " + headers;
        }
        if ("hashCode".equals(name)) {
          return Integer.valueOf(System.identityHashCode(proxy));
        }
        if ("equals".equals(name)) {
          return Boolean.valueOf(proxy == args[0]);
        }
        return defaultFor(method.getReturnType());
      }
    };

    return (HttpServletRequest) Proxy.newProxyInstance(
        GrouperMcpServlet20260728Test.class.getClassLoader(),
        new Class<?>[] { HttpServletRequest.class }, handler);
  }

  /**
   * a request with no headers at all
   * @return the request
   */
  private static HttpServletRequest request() {
    return request(new LinkedHashMap<String, String>());
  }

  /**
   * a response which records what the code under test did to it
   * @param recorded where to record it
   * @return the response
   */
  private static HttpServletResponse response(final Recorded recorded) {

    final PrintWriter writer = new PrintWriter(recorded.body);

    InvocationHandler handler = new InvocationHandler() {

      public Object invoke(Object proxy, Method method, Object[] args) {

        String name = method.getName();

        if ("setStatus".equals(name)) {
          recorded.status = ((Integer) args[0]).intValue();
          return null;
        }
        if ("setHeader".equals(name) || "addHeader".equals(name)) {
          recorded.headers.put((String) args[0], (String) args[1]);
          return null;
        }
        if ("getWriter".equals(name)) {
          return writer;
        }
        if ("toString".equals(name)) {
          return "stub response";
        }
        if ("hashCode".equals(name)) {
          return Integer.valueOf(System.identityHashCode(proxy));
        }
        if ("equals".equals(name)) {
          return Boolean.valueOf(proxy == args[0]);
        }
        return defaultFor(method.getReturnType());
      }
    };

    return (HttpServletResponse) Proxy.newProxyInstance(
        GrouperMcpServlet20260728Test.class.getClassLoader(),
        new Class<?>[] { HttpServletResponse.class }, handler);
  }

  /**
   * headers for a well formed request on the modern revision
   * @param method the JSON-RPC method, mirrored into Mcp-Method
   * @param name the tool or resource name, mirrored into Mcp-Name, may be null
   * @return the headers
   */
  private static Map<String, String> modernHeaders(String method, String name) {
    Map<String, String> headers = new LinkedHashMap<String, String>();
    headers.put("MCP-Protocol-Version", MODERN);
    headers.put("Mcp-Method", method);
    if (name != null) {
      headers.put("Mcp-Name", name);
    }
    return headers;
  }

  /**
   * a well formed request body on the modern revision
   * @param method the JSON-RPC method
   * @param id the JSON-RPC id, as JSON text, or null to leave it out entirely
   * @param extraParams JSON text merged into params, or null
   * @return the parsed body
   */
  private static JsonNode modernBody(String method, String id, String extraParams) {
    StringBuilder json = new StringBuilder("{\"jsonrpc\":\"2.0\"");
    if (id != null) {
      json.append(",\"id\":").append(id);
    }
    json.append(",\"method\":\"").append(method).append("\",\"params\":{\"_meta\":{")
      .append("\"io.modelcontextprotocol/protocolVersion\":\"").append(MODERN).append("\",")
      .append("\"io.modelcontextprotocol/clientCapabilities\":{}}");
    if (extraParams != null) {
      json.append(",").append(extraParams);
    }
    json.append("}}");
    return parse(json.toString());
  }

  /**
   * @param json the JSON text
   * @return the parsed node
   */
  private static JsonNode parse(String json) {
    try {
      return objectMapper.readTree(json);
    } catch (Exception e) {
      throw new RuntimeException("could not parse: " + json, e);
    }
  }

  /**
   * drive the modern request validation
   * @param headers the HTTP headers
   * @param body the parsed request body
   * @return what the response recorded
   * @throws IOException never in practice
   */
  private Recorded validateModern(Map<String, String> headers, JsonNode body)
      throws IOException {

    Recorded recorded = new Recorded();
    JsonNode params = body.get("params");
    JsonNode id = body.get("id");
    String method = body.has("method") && body.get("method").isTextual()
        ? body.get("method").asText() : null;

    boolean rejected = this.servlet.rejectIfModernRequestInvalid(
        request(headers), response(recorded), body, method, params, id);

    return rejected ? recorded : null;
  }

  /**
   * assert that a modern request was accepted
   * @param headers the HTTP headers
   * @param body the parsed request body
   * @throws IOException never in practice
   */
  private void assertModernAccepted(Map<String, String> headers, JsonNode body)
      throws IOException {
    Recorded recorded = this.validateModern(headers, body);
    if (recorded != null) {
      fail("expected the request to be accepted, it was rejected with "
          + recorded.errorCode() + " " + recorded.errorMessage());
    }
  }

  /**
   * assert that a modern request was rejected with a code and status
   * @param headers the HTTP headers
   * @param body the parsed request body
   * @param expectedCode the JSON-RPC error code
   * @param expectedStatus the HTTP status
   * @throws IOException never in practice
   */
  private void assertModernRejected(Map<String, String> headers, JsonNode body,
      int expectedCode, int expectedStatus) throws IOException {

    Recorded recorded = this.validateModern(headers, body);
    assertNotNull("expected the request to be rejected, it was accepted", recorded);
    assertEquals("JSON-RPC error code", expectedCode, recorded.errorCode());
    assertEquals("HTTP status", expectedStatus, recorded.status);
  }

  // ==================== driving the whole servlet ====================

  /**
   * put a request through doPost, authenticated, so that what is under test is the order
   * the checks run in and whether they are reached at all, rather than any one of them on
   * its own
   * @param headers the HTTP headers, an Authorization header is added
   * @param body the request body as JSON text
   * @return what the response recorded
   * @throws Exception if the servlet throws
   */
  private Recorded post(Map<String, String> headers, String body) throws Exception {

    if (this.bearerToken == null) {
      this.setUpAuthentication();
    }

    Map<String, String> withAuth = new LinkedHashMap<String, String>(headers);
    withAuth.put("Authorization", "Bearer " + this.bearerToken);

    Recorded recorded = new Recorded();
    this.servlet.doPost(request(withAuth, body), response(recorded));

    assertFalse("authentication should have succeeded, got 401.  the token, the registered "
        + "client or the subject is not set up as this test expects",
        recorded.status == HttpServletResponse.SC_UNAUTHORIZED);

    return recorded;
  }

  /**
   * a modern request body as JSON text
   * @param method the JSON-RPC method
   * @param id the id as JSON text, or null to leave it out and make it a notification
   * @param extraParams JSON text merged into params, or null
   * @return the JSON text
   */
  private static String modernJson(String method, String id, String extraParams) {
    return modernBody(method, id, extraParams).toString();
  }

  // ==================== which revision a request declares ====================

  /**
   * only the modern revision counts as modern.  the earlier ones are served the way this
   * server always served them, so mistaking one for the other would hold a client to
   * rules its revision never had
   */
  public void testIsModernProtocolVersion() {
    assertTrue(GrouperMcpServlet.isModernProtocolVersion("2026-07-28"));
    assertFalse(GrouperMcpServlet.isModernProtocolVersion("2025-11-25"));
    assertFalse(GrouperMcpServlet.isModernProtocolVersion("2025-06-18"));
    assertFalse(GrouperMcpServlet.isModernProtocolVersion(LEGACY));
    assertFalse(GrouperMcpServlet.isModernProtocolVersion(null));
    assertFalse(GrouperMcpServlet.isModernProtocolVersion(""));
    assertFalse(GrouperMcpServlet.isModernProtocolVersion("2026-07-28 "));
  }

  /**
   * the versions this server accepts.  a client asking for one this server does not
   * implement has to be told so rather than served under this server's own semantics
   */
  public void testIsProtocolVersionSupported() {
    assertTrue(GrouperMcpServlet.isProtocolVersionSupported("2026-07-28"));
    assertTrue(GrouperMcpServlet.isProtocolVersionSupported("2025-11-25"));
    assertTrue(GrouperMcpServlet.isProtocolVersionSupported("2025-06-18"));
    assertTrue(GrouperMcpServlet.isProtocolVersionSupported(LEGACY));
    assertFalse(GrouperMcpServlet.isProtocolVersionSupported("1900-01-01"));
    assertFalse(GrouperMcpServlet.isProtocolVersionSupported("2027-01-01"));
    assertFalse(GrouperMcpServlet.isProtocolVersionSupported(null));
  }

  /**
   * the version in the request body's _meta.  a field which is there but set to null
   * declares no version, the same as leaving it out: the text of a null node is the word
   * "null", so reading it any other way has the request appear to ask for a version by
   * that name
   */
  public void testProtocolVersionFromMeta() {

    assertEquals(MODERN, GrouperMcpServlet.protocolVersionFromMeta(
        parse("{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\"}}")));

    assertNull(GrouperMcpServlet.protocolVersionFromMeta(parse("{\"_meta\":{}}")));
    assertNull(GrouperMcpServlet.protocolVersionFromMeta(parse("{}")));
    assertNull(GrouperMcpServlet.protocolVersionFromMeta(null));

    assertNull("a field set to null declares no version",
        GrouperMcpServlet.protocolVersionFromMeta(
            parse("{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":null}}")));

    assertNull("an object has no version text to read",
        GrouperMcpServlet.protocolVersionFromMeta(
            parse("{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":{}}}")));

    assertNull("_meta which is not an object carries nothing",
        GrouperMcpServlet.protocolVersionFromMeta(parse("{\"_meta\":\"oops\"}")));
  }

  /**
   * the version in the HTTP header
   * @throws IOException never in practice
   */
  public void testProtocolVersionFromHeader() throws IOException {

    Map<String, String> headers = new LinkedHashMap<String, String>();
    headers.put("MCP-Protocol-Version", MODERN);
    assertEquals(MODERN, GrouperMcpServlet.protocolVersionFromHeader(request(headers)));

    assertNull(GrouperMcpServlet.protocolVersionFromHeader(request()));

    headers.put("MCP-Protocol-Version", "   ");
    assertNull("blank is no version", GrouperMcpServlet.protocolVersionFromHeader(request(headers)));
  }

  /**
   * the body is where the modern revision puts the version, so it is preferred, and the
   * header is used only when the body has none
   * @throws IOException never in practice
   */
  public void testDeclaredProtocolVersion() throws IOException {

    Map<String, String> header2025 = new LinkedHashMap<String, String>();
    header2025.put("MCP-Protocol-Version", LEGACY);

    assertEquals("the body wins", MODERN, GrouperMcpServlet.declaredProtocolVersion(
        request(header2025),
        parse("{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\"}}")));

    assertEquals("the header is used when the body has none", LEGACY,
        GrouperMcpServlet.declaredProtocolVersion(request(header2025), parse("{}")));

    assertNull("a request declaring nothing declares nothing",
        GrouperMcpServlet.declaredProtocolVersion(request(), parse("{}")));
  }

  // ==================== methods the revision removed ====================

  /**
   * the handshake went in this revision, and ping went with it.  a request declaring the
   * revision which calls one of them is answered the way any other method this server
   * does not implement would be
   */
  public void testIsMethodRemovedInModernRevision() {
    assertTrue(GrouperMcpServlet.isMethodRemovedInModernRevision("initialize"));
    assertTrue(GrouperMcpServlet.isMethodRemovedInModernRevision("notifications/initialized"));
    assertTrue("ping went with the handshake",
        GrouperMcpServlet.isMethodRemovedInModernRevision("ping"));

    assertFalse(GrouperMcpServlet.isMethodRemovedInModernRevision("tools/list"));
    assertFalse(GrouperMcpServlet.isMethodRemovedInModernRevision("tools/call"));
    assertFalse(GrouperMcpServlet.isMethodRemovedInModernRevision("server/discover"));
    assertFalse(GrouperMcpServlet.isMethodRemovedInModernRevision("Ping"));
    assertFalse(GrouperMcpServlet.isMethodRemovedInModernRevision(null));
  }

  // ==================== version agreement ====================

  /**
   * a request carrying no version at all is from a client on the revision which had none,
   * and is served rather than refused
   * @throws IOException never in practice
   */
  public void testVersionNeitherHeaderNorBodyIsAllowed() throws IOException {
    Recorded recorded = new Recorded();
    assertFalse(this.servlet.rejectIfProtocolVersionNotSupported(
        request(), response(recorded), parse("{}"), parse("1")));
    assertNull("nothing should have been written", recorded.json());
  }

  /**
   * header and body agreeing on a version this server implements is fine
   * @throws IOException never in practice
   */
  public void testVersionHeaderAndBodyAgree() throws IOException {
    Recorded recorded = new Recorded();
    assertFalse(this.servlet.rejectIfProtocolVersionNotSupported(
        request(modernHeaders("tools/list", null)), response(recorded),
        parse("{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\"}}"),
        parse("1")));
    assertNull(recorded.json());
  }

  /**
   * header and body disagreeing is refused, since something in the network could route on
   * one value while this server acts on the other
   * @throws IOException never in practice
   */
  public void testVersionHeaderAndBodyDisagree() throws IOException {
    Recorded recorded = new Recorded();
    assertTrue(this.servlet.rejectIfProtocolVersionNotSupported(
        request(modernHeaders("tools/list", null)), response(recorded),
        parse("{\"_meta\":{\"io.modelcontextprotocol/protocolVersion\":\"2025-06-18\"}}"),
        parse("1")));
    assertEquals(HEADER_MISMATCH, recorded.errorCode());
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, recorded.status);
  }

  /**
   * a version this server does not implement is refused, and the client is told which
   * versions it could ask for instead so it can retry rather than guess
   * @throws IOException never in practice
   */
  public void testVersionUnsupportedNamesTheSupportedOnes() throws IOException {

    Map<String, String> headers = new LinkedHashMap<String, String>();
    headers.put("MCP-Protocol-Version", "1900-01-01");

    Recorded recorded = new Recorded();
    assertTrue(this.servlet.rejectIfProtocolVersionNotSupported(
        request(headers), response(recorded), parse("{}"), parse("1")));

    assertEquals(UNSUPPORTED_PROTOCOL_VERSION, recorded.errorCode());
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, recorded.status);

    JsonNode data = recorded.json().get("error").get("data");
    assertNotNull("the client has to be told what to retry with", data);
    assertEquals("1900-01-01", data.get("requested").asText());
    assertTrue("the supported list should offer the modern revision",
        data.get("supported").toString().contains(MODERN));
    assertTrue("and an earlier one, so a client which cannot go modern has somewhere to go",
        data.get("supported").toString().contains(LEGACY));
  }

  // ==================== a well formed modern request ====================

  /**
   * the shape everything else in this class varies from
   * @throws IOException never in practice
   */
  public void testWellFormedModernRequestIsAccepted() throws IOException {
    this.assertModernAccepted(modernHeaders("tools/list", null),
        modernBody("tools/list", "1", null));
  }

  /**
   * a tool call names what it acts on in a header as well as in the body
   * @throws IOException never in practice
   */
  public void testWellFormedModernToolCallIsAccepted() throws IOException {
    this.assertModernAccepted(modernHeaders("tools/call", "group_get_members"),
        modernBody("tools/call", "1", "\"name\":\"group_get_members\""));
  }

  // ==================== jsonrpc ====================

  /**
   * every message is a JSON-RPC 2.0 message, which means saying so
   * @throws IOException never in practice
   */
  public void testJsonrpcMustBePresentAndTwoPointOh() throws IOException {

    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\","
            + "\"io.modelcontextprotocol/clientCapabilities\":{}}}}"),
        INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);

    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"jsonrpc\":\"1.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\","
            + "\"io.modelcontextprotocol/clientCapabilities\":{}}}}"),
        INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);

    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"jsonrpc\":2.0,\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\","
            + "\"io.modelcontextprotocol/clientCapabilities\":{}}}}"),
        INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
  }

  // ==================== _meta.protocolVersion ====================

  /**
   * the revision requires the version in _meta and not only in the header, so a request
   * which declares it in the header alone is not a well formed request of that revision
   * @throws IOException never in practice
   */
  public void testMetaProtocolVersionIsRequired() throws IOException {
    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/clientCapabilities\":{}}}}"),
        INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * a field set to null is not a declared version
   * @throws IOException never in practice
   */
  public void testMetaProtocolVersionExplicitNullIsRejected() throws IOException {
    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/protocolVersion\":null,"
            + "\"io.modelcontextprotocol/clientCapabilities\":{}}}}"),
        INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * a version is a string.  one which is not cannot be allowed to pass and let the header
   * decide on its own
   * @throws IOException never in practice
   */
  public void testMetaProtocolVersionMustBeAString() throws IOException {
    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/protocolVersion\":{},"
            + "\"io.modelcontextprotocol/clientCapabilities\":{}}}}"),
        INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST);
  }

  // ==================== _meta.clientCapabilities ====================

  /**
   * with no handshake there is nowhere else the client could have said what it supports
   * @throws IOException never in practice
   */
  public void testClientCapabilitiesIsRequired() throws IOException {
    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\"}}}"),
        INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * a field set to null says no more than leaving it out does
   * @throws IOException never in practice
   */
  public void testClientCapabilitiesExplicitNullIsRejected() throws IOException {
    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\","
            + "\"io.modelcontextprotocol/clientCapabilities\":null}}}"),
        INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * what a client supports is a set of named capabilities, so a string or an array cannot
   * be read as one however well formed it is on its own
   * @throws IOException never in practice
   */
  public void testClientCapabilitiesMustBeAnObject() throws IOException {

    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\","
            + "\"io.modelcontextprotocol/clientCapabilities\":\"yes\"}}}"),
        INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST);

    this.assertModernRejected(modernHeaders("tools/list", null),
        parse("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"_meta\":{"
            + "\"io.modelcontextprotocol/protocolVersion\":\"" + MODERN + "\","
            + "\"io.modelcontextprotocol/clientCapabilities\":[]}}}"),
        INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * clientInfo is not required by the revision and is deliberately not asked for
   * @throws IOException never in practice
   */
  public void testClientInfoIsNotRequired() throws IOException {
    this.assertModernAccepted(modernHeaders("tools/list", null),
        modernBody("tools/list", "1", null));
  }

  // ==================== the id ====================

  /**
   * an id is what a client matches a response to a request by, and this revision says
   * unlike plain JSON-RPC it must not be null
   * @throws IOException never in practice
   */
  public void testIdMustNotBeNull() throws IOException {
    this.assertModernRejected(modernHeaders("tools/list", null),
        modernBody("tools/list", "null", null),
        INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * an id which is neither a string nor a number cannot be echoed back without this
   * server emitting something which is not a valid JSON-RPC response
   * @throws IOException never in practice
   */
  public void testIdMustBeAStringOrANumber() throws IOException {

    this.assertModernRejected(modernHeaders("tools/list", null),
        modernBody("tools/list", "{}", null),
        INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);

    this.assertModernRejected(modernHeaders("tools/list", null),
        modernBody("tools/list", "[]", null),
        INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * the schema this revision names as its source of truth defines a request id as a
   * string or a number, so a number with a fractional part is a valid id.  the prose
   * saying "integer" is narrower than the schema and the schema governs
   * @throws IOException never in practice
   */
  public void testIdMayBeAnyNumberOrAString() throws IOException {
    this.assertModernAccepted(modernHeaders("tools/list", null), modernBody("tools/list", "1", null));
    this.assertModernAccepted(modernHeaders("tools/list", null), modernBody("tools/list", "1.5", null));
    this.assertModernAccepted(modernHeaders("tools/list", null), modernBody("tools/list", "-7.25", null));
    this.assertModernAccepted(modernHeaders("tools/list", null), modernBody("tools/list", "\"abc\"", null));
  }

  /**
   * an id which is absent marks a notification rather than a request, and is answered
   * elsewhere, so it is not held to the rules for an id which is there
   * @throws IOException never in practice
   */
  public void testAbsentIdIsNotCheckedHere() throws IOException {
    this.assertModernAccepted(modernHeaders("tools/list", null), modernBody("tools/list", null, null));
  }

  // ==================== the mirrored headers ====================

  /**
   * the revision requires the version header on every request.  a request carrying no
   * version at all is served as the earlier revision, but this one declared the modern
   * revision in its body and so is held to that revision's rules
   * @throws IOException never in practice
   */
  public void testProtocolVersionHeaderIsRequired() throws IOException {
    Map<String, String> headers = modernHeaders("tools/list", null);
    headers.remove("MCP-Protocol-Version");
    this.assertModernRejected(headers, modernBody("tools/list", "1", null),
        HEADER_MISMATCH, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * the method is mirrored into a header so an intermediary can route on it without
   * parsing the body, and the two have to agree
   * @throws IOException never in practice
   */
  public void testMcpMethodHeaderIsRequiredAndMustMatch() throws IOException {

    Map<String, String> missing = modernHeaders("tools/list", null);
    missing.remove("Mcp-Method");
    this.assertModernRejected(missing, modernBody("tools/list", "1", null),
        HEADER_MISMATCH, HttpServletResponse.SC_BAD_REQUEST);

    this.assertModernRejected(modernHeaders("tools/call", null),
        modernBody("tools/list", "1", null),
        HEADER_MISMATCH, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * the name header is required for the methods which name what they act on, and of those
   * this server implements tools/call
   * @throws IOException never in practice
   */
  public void testMcpNameHeaderIsRequiredForToolsCall() throws IOException {

    Map<String, String> missing = modernHeaders("tools/call", null);
    this.assertModernRejected(missing,
        modernBody("tools/call", "1", "\"name\":\"group_get_members\""),
        HEADER_MISMATCH, HttpServletResponse.SC_BAD_REQUEST);

    this.assertModernRejected(modernHeaders("tools/call", "some_other_tool"),
        modernBody("tools/call", "1", "\"name\":\"group_get_members\""),
        HEADER_MISMATCH, HttpServletResponse.SC_BAD_REQUEST);
  }

  /**
   * a name which cannot be sent as plain ASCII is base64 encoded by the client, and has
   * to be decoded before it is compared to the body
   * @throws IOException never in practice
   */
  public void testMcpNameHeaderMayBeBase64Encoded() throws IOException {

    String toolName = "grüppe_lesen";
    String encoded = "=?base64?"
        + Base64.getEncoder().encodeToString(toolName.getBytes(StandardCharsets.UTF_8)) + "?=";

    this.assertModernAccepted(modernHeaders("tools/call", encoded),
        modernBody("tools/call", "1", "\"name\":\"" + toolName + "\""));
  }

  /**
   * the name header is not asked for on methods which do not name anything
   * @throws IOException never in practice
   */
  public void testMcpNameHeaderNotRequiredForToolsList() throws IOException {
    this.assertModernAccepted(modernHeaders("tools/list", null),
        modernBody("tools/list", "1", null));
  }

  // ==================== header value encoding ====================

  /**
   * a value which is not marked as encoded is used as it is, and one which is marked is
   * decoded.  something which claims to be encoded and is not is left alone, where it
   * will not match the body and the request is refused
   */
  public void testDecodeHeaderValue() {

    assertEquals("plain", GrouperMcpServlet.decodeHeaderValue("plain"));
    assertNull(GrouperMcpServlet.decodeHeaderValue(null));

    String encoded = "=?base64?"
        + Base64.getEncoder().encodeToString("hello".getBytes(StandardCharsets.UTF_8)) + "?=";
    assertEquals("hello", GrouperMcpServlet.decodeHeaderValue(encoded));

    assertEquals("=?base64?not valid base64!?=",
        GrouperMcpServlet.decodeHeaderValue("=?base64?not valid base64!?="));

    assertEquals("a sentinel wrapping nothing decodes to nothing, which will not match "
        + "a body value and so is refused further on",
        "", GrouperMcpServlet.decodeHeaderValue("=?base64??="));

    assertEquals("a value only starting like the sentinel is left alone",
        "=?base64?abc", GrouperMcpServlet.decodeHeaderValue("=?base64?abc"));
  }

  // ==================== where clients are sent ====================

  /**
   * the origin this server treats as its own comes from configuration and not from the
   * request.  a request whose Host is some other name must not be able to make this
   * server agree that the name is its own
   */
  public void testConfiguredOriginComesFromConfigurationNotTheRequest() {
    assertEquals("https://server.example.edu", GrouperMcpServlet.configuredOrigin());
  }

  /**
   * with nothing configured there is no origin which can be trusted to be this server's,
   * so none is claimed
   */
  public void testConfiguredOriginIsNullWhenNotConfigured() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.ws.url", "");
    assertNull(GrouperMcpServlet.configuredOrigin());
  }

  /**
   * a client which did not authenticate is told where to find out how, and that address
   * is taken from configuration.  built from the request instead, whoever set the Host
   * header would choose where clients go to be told where to log in
   * @throws IOException never in practice
   */
  public void testResourceMetadataUrlIgnoresTheRequestHost() throws IOException {

    Map<String, String> hostile = new LinkedHashMap<String, String>();
    hostile.put("Host", "attacker.example.com");

    String url = GrouperMcpServlet.resourceMetadataUrl(request(hostile));

    assertEquals("https://server.example.edu/grouper-ws/.well-known/oauth-protected-resource", url);
    assertFalse("the request host must not appear", url.contains("attacker"));
    assertFalse("nor the host the request arrived at", url.contains("someotherhost"));
  }

  // ==================== the Origin check ====================

  /**
   * only a browser sends an Origin, so a request without one is not what this protects
   * against and is served
   * @throws IOException never in practice
   */
  public void testOriginAbsentIsAllowed() throws IOException {
    Recorded recorded = new Recorded();
    assertFalse(this.servlet.rejectIfOriginNotAllowed(request(), response(recorded)));
  }

  /**
   * a page served by this server is not cross origin
   * @throws IOException never in practice
   */
  public void testOriginMatchingTheConfiguredOriginIsAllowed() throws IOException {
    Map<String, String> headers = new LinkedHashMap<String, String>();
    headers.put("Origin", "https://server.example.edu");
    Recorded recorded = new Recorded();
    assertFalse(this.servlet.rejectIfOriginNotAllowed(request(headers), response(recorded)));
  }

  /**
   * an origin which is neither this server's nor configured as allowed is refused
   * @throws IOException never in practice
   */
  public void testOriginNotAllowedIsRefused() throws IOException {
    Map<String, String> headers = new LinkedHashMap<String, String>();
    headers.put("Origin", "https://evil.example.com");
    Recorded recorded = new Recorded();
    assertTrue(this.servlet.rejectIfOriginNotAllowed(request(headers), response(recorded)));
    assertEquals(HttpServletResponse.SC_FORBIDDEN, recorded.status);
    assertEquals(INVALID_REQUEST, recorded.errorCode());
  }

  /**
   * the case this check exists for.  a page which made a name it controls resolve to this
   * server's address sends that name as both the Origin and the Host, so deciding which
   * origin is this server's from the request would compare the attacker's origin against
   * the attacker's own Host and always agree
   * @throws IOException never in practice
   */
  public void testOriginIsNotTrustedJustBecauseItMatchesTheRequestHost() throws IOException {

    Map<String, String> headers = new LinkedHashMap<String, String>();
    // the stub request reports itself as arriving at someotherhost.example.edu
    headers.put("Origin", "https://someotherhost.example.edu");

    Recorded recorded = new Recorded();
    assertTrue("an origin agreeing with the request's own host is still cross origin",
        this.servlet.rejectIfOriginNotAllowed(request(headers), response(recorded)));
    assertEquals(HttpServletResponse.SC_FORBIDDEN, recorded.status);
  }

  /**
   * a deployment opts in to a browser based client by configuring a pattern for it
   * @throws IOException never in practice
   */
  public void testOriginMatchingAConfiguredPatternIsAllowed() throws IOException {

    GrouperConfig.retrieveConfig().propertiesOverrideMap()
      .put("grouper.mcp.allowedOrigin.someApp.regex", "^https://apps\\.example\\.edu$");
    try {
      Map<String, String> headers = new LinkedHashMap<String, String>();
      headers.put("Origin", "https://apps.example.edu");
      Recorded recorded = new Recorded();
      assertFalse(this.servlet.rejectIfOriginNotAllowed(request(headers), response(recorded)));

      headers.put("Origin", "https://apps.example.edu.evil.com");
      Recorded other = new Recorded();
      assertTrue("the pattern is anchored, so a longer name does not match",
          this.servlet.rejectIfOriginNotAllowed(request(headers), response(other)));
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .remove("grouper.mcp.allowedOrigin.someApp.regex");
    }
  }

  // ==================== the checks reached through doPost ====================

  /**
   * the handshake and ping went in this revision, so a request declaring it is answered
   * the way any other method this server does not implement would be.  a request which
   * declares nothing still gets the handshake, which is what the earlier revisions open
   * with
   * @throws Exception if the servlet throws
   */
  public void testRemovedMethodsAreRefusedOnlyForModernRequests() throws Exception {

    for (String removed : new String[] { "ping", "initialize" }) {

      Recorded modern = this.post(modernHeaders(removed, null), modernJson(removed, "1", null));
      assertEquals(removed + " on the modern revision", METHOD_NOT_FOUND, modern.errorCode());
      assertEquals(HttpServletResponse.SC_NOT_FOUND, modern.status);

      Recorded legacy = this.post(new LinkedHashMap<String, String>(),
          "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"" + removed + "\",\"params\":{}}");
      assertNotNull(legacy.json());
      assertTrue(removed + " on the earlier revisions is still served",
          legacy.json().has("result"));
    }
  }

  /**
   * this revision defines no notification a client sends over this transport, so one
   * cannot be accepted.  the earlier revisions do have notifications/initialized, so for
   * them a notification is acknowledged
   * @throws Exception if the servlet throws
   */
  public void testNotificationIsRefusedOnModernAndAcknowledgedOnLegacy() throws Exception {

    Recorded modern = this.post(modernHeaders("tools/list", null),
        modernJson("tools/list", null, null));
    assertEquals("a notification on the modern revision cannot be accepted",
        HttpServletResponse.SC_BAD_REQUEST, modern.status);
    assertEquals(INVALID_REQUEST, modern.errorCode());

    Recorded legacy = this.post(new LinkedHashMap<String, String>(),
        "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\",\"params\":{}}");
    assertEquals("acknowledged with no body", HttpServletResponse.SC_ACCEPTED, legacy.status);
    assertNull("a notification is never answered", legacy.json());
  }

  /**
   * a notification shaped call to a method which returns something would otherwise be run
   * and answered with a result the client never asked for and cannot match to anything
   * @throws Exception if the servlet throws
   */
  public void testNotificationDoesNotRunTheMethod() throws Exception {
    Recorded recorded = this.post(new LinkedHashMap<String, String>(),
        "{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\",\"params\":{}}");
    assertEquals(HttpServletResponse.SC_ACCEPTED, recorded.status);
    assertNull("tools/list must not have run", recorded.json());
  }

  /**
   * this server answers tools/list in one page and never sends a nextCursor, so any
   * cursor which arrives is one it cannot resume from
   * @throws Exception if the servlet throws
   */
  public void testToolsListRejectsACursorItCouldNotHaveIssued() throws Exception {
    Recorded recorded = this.post(new LinkedHashMap<String, String>(),
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{\"cursor\":\"x\"}}");
    assertEquals(INVALID_PARAMS, recorded.errorCode());
  }

  /**
   * a method which is present but is not a string makes this something other than a
   * JSON-RPC request, which is a different thing from naming a method this server does
   * not have
   * @throws Exception if the servlet throws
   */
  public void testNonStringMethodIsAnInvalidRequest() throws Exception {
    Recorded recorded = this.post(new LinkedHashMap<String, String>(),
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":99,\"params\":{}}");
    assertEquals(INVALID_REQUEST, recorded.errorCode());
  }

  /**
   * a notification the server cannot accept is refused with an HTTP error status, since
   * the status is the only thing it can say.  a request is answered with a JSON-RPC error
   * at HTTP 200 as it always has been
   * @throws Exception if the servlet throws
   */
  public void testMissingMethodStatusDependsOnWhetherThereIsAnId() throws Exception {

    Recorded request = this.post(new LinkedHashMap<String, String>(),
        "{\"jsonrpc\":\"2.0\",\"id\":1}");
    assertEquals(INVALID_REQUEST, request.errorCode());
    assertEquals("a request carries its error in the body", HttpServletResponse.SC_OK,
        request.status);

    Recorded notification = this.post(new LinkedHashMap<String, String>(),
        "{\"jsonrpc\":\"2.0\"}");
    assertEquals(INVALID_REQUEST, notification.errorCode());
    assertEquals("a notification has only the status to say it with",
        HttpServletResponse.SC_BAD_REQUEST, notification.status);
  }

  /**
   * every result says what kind of result it is, and identifies the server which produced
   * it, without either having been established beforehand by a handshake
   * @throws Exception if the servlet throws
   */
  public void testResultCarriesResultTypeAndServerInfo() throws Exception {

    Recorded recorded = this.post(modernHeaders("tools/list", null),
        modernJson("tools/list", "1", null));

    JsonNode result = recorded.json().get("result");
    assertNotNull("expected a result, got: " + recorded.json(), result);
    assertEquals("complete", result.get("resultType").asText());

    JsonNode serverInfo = result.get("_meta").get("io.modelcontextprotocol/serverInfo");
    assertNotNull("the server identifies itself in every result", serverInfo);
    assertNotNull(serverInfo.get("name"));

    assertNotNull("tools/list carries caching hints", result.get("ttlMs"));
    assertNotNull(result.get("cacheScope"));
  }

  /**
   * the modern validation has to actually be reached.  a rule which is right but never
   * run protects nothing, so this drives one of them through the whole servlet
   * @throws Exception if the servlet throws
   */
  public void testModernValidationIsReachedFromDoPost() throws Exception {

    Map<String, String> headers = modernHeaders("tools/list", null);
    headers.remove("MCP-Protocol-Version");

    Recorded recorded = this.post(headers, modernJson("tools/list", "1", null));
    assertEquals("the missing header should have been noticed", HEADER_MISMATCH,
        recorded.errorCode());
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, recorded.status);
  }
}

