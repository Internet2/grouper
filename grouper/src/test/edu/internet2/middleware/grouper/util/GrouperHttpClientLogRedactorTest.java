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
package edu.internet2.middleware.grouper.util;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security advisory M1 (provisionerCommandLoggingCredentials): the HTTP transcript that
 * GrouperHttpClient writes for provisioner command logging (and DEBUG) must not contain
 * passwords, client secrets, refresh / access tokens, signed assertions or API keys, even when
 * the caller did not call assignDoNotLogRequestBody / assignDoNotLogResponseBody.
 *
 * The unit tests use the request and response shapes of the affected connectors (Dropbox, Box,
 * Remedy V2, Remedy Digital Marketplace V2).  The transcript tests run real requests against a
 * local JDK HttpServer with the thread local GrouperHttpClientLog started, as
 * GrouperProvisionerTargetDaoAdapter does.  No database is needed.
 *
 * Before the fix this does not compile (GrouperHttpClientLogRedactor does not exist), and the
 * transcript tests would find every dummy secret in the log.
 * </pre>
 */
public class GrouperHttpClientLogRedactorTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperHttpClientLogRedactorTest("testTranscriptRawBodyAndTokenResponse"));
  }

  /**
   * @param name
   */
  public GrouperHttpClientLogRedactorTest(String name) {
    super(name);
  }

  /**
   * assert the text does not contain any of the secrets
   * @param text
   * @param secrets
   */
  private static void assertNoSecrets(String text, String... secrets) {
    assertNotNull(text);
    for (String secret : secrets) {
      assertFalse("should not contain '" + secret + "': " + text, text.contains(secret));
    }
  }

  /**
   * assert the text contains all of the expected strings
   * @param text
   * @param expecteds
   */
  private static void assertContainsAll(String text, String... expecteds) {
    assertNotNull(text);
    for (String expected : expecteds) {
      assertTrue("should contain '" + expected + "': " + text, text.contains(expected));
    }
  }

  // ---------------------------------------------------------------------------------------
  // redactBody on the connector shapes

  /**
   * Dropbox refresh token form body
   */
  public void testDropboxFormBody() {
    String body = "grant_type=refresh_token&refresh_token=RT-SECRET-1&client_id=myClientId&client_secret=CS-SECRET-2";
    String redacted = GrouperHttpClientLogRedactor.redactBody(body);
    assertNoSecrets(redacted, "RT-SECRET-1", "CS-SECRET-2");
    assertContainsAll(redacted, "grant_type=refresh_token", "client_id=myClientId", "refresh_token=*******");
  }

  /**
   * Remedy Digital Marketplace V2 login JSON body, including an escaped quote in the password
   */
  public void testRemedyDigitalMarketplaceJsonBody() {
    String body = "{\"id\":\"svcUser\",\"password\":\"PW-SE\\\"CRET-3\"}";
    String redacted = GrouperHttpClientLogRedactor.redactBody(body);
    assertNoSecrets(redacted, "PW-SE", "CRET-3");
    assertContainsAll(redacted, "\"id\":\"svcUser\"", "\"password\":\"*******\"");
  }

  /**
   * Dropbox / Box token JSON response
   */
  public void testTokenJsonResponse() {
    String body = "{\"access_token\": \"AT-SECRET-4\", \"token_type\": \"bearer\", \"expires_in\": 14400, "
        + "\"refresh_token\":\"RT-SECRET-5\", \"id_token\":\"ID-SECRET-6\"}";
    String redacted = GrouperHttpClientLogRedactor.redactBody(body);
    assertNoSecrets(redacted, "AT-SECRET-4", "RT-SECRET-5", "ID-SECRET-6");
    assertContainsAll(redacted, "\"token_type\": \"bearer\"", "\"expires_in\": 14400");
  }

  /**
   * Remedy V2 login response is a bare JWT with no key name
   */
  public void testBareJwtResponse() {
    String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdmNVc2VyIn0.SIGsig_123-abc";
    String redacted = GrouperHttpClientLogRedactor.redactBody(jwt);
    assertNoSecrets(redacted, "eyJzdWIiOiJzdmNVc2VyIn0", "SIGsig_123-abc");
  }

  /**
   * Box JWT bearer grant with a signed assertion
   */
  public void testBoxAssertionFormBody() {
    String body = "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer"
        + "&assertion=eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJib3gifQ.ASSERTIONsig&client_id=boxClient&client_secret=CS-SECRET-7";
    String redacted = GrouperHttpClientLogRedactor.redactBody(body);
    assertNoSecrets(redacted, "eyJpc3MiOiJib3gifQ", "ASSERTIONsig", "CS-SECRET-7");
    assertContainsAll(redacted, "client_id=boxClient", "grant_type=");
  }

  /**
   * XML / SOAP credentials and authorization values in text
   */
  public void testXmlAndAuthSchemes() {
    String body = "<env><ns1:password>PW-SECRET-8</ns1:password><user>bob</user></env> Authorization: Bearer BEARER-SECRET-9 ok";
    String redacted = GrouperHttpClientLogRedactor.redactBody(body);
    assertNoSecrets(redacted, "PW-SECRET-8", "BEARER-SECRET-9");
    assertContainsAll(redacted, "<user>bob</user>", "Bearer *******");
  }

  /**
   * URL user info and query string credentials
   */
  public void testRedactUrl() {
    String url = "https://svc:PW-SECRET-10@example.edu/api?api_key=KEY-SECRET-11&page=2&access_token=AT-SECRET-12";
    String redacted = GrouperHttpClientLogRedactor.redactUrl(url);
    assertNoSecrets(redacted, "PW-SECRET-10", "KEY-SECRET-11", "AT-SECRET-12");
    assertContainsAll(redacted, "https://svc:*******@example.edu/api?", "page=2");
  }

  /**
   * names that are and are not credentials
   */
  public void testSensitiveNames() {
    for (String name : new String[] {"password", "client_secret", "refresh_token", "access_token", "assertion",
        "client_assertion", "X-Api-Key", "apiKey", "private_key", "code"}) {
      assertTrue(name, GrouperHttpClientLogRedactor.isSensitiveName(name));
    }
    for (String name : new String[] {"grant_type", "token_type", "expires_in", "client_id", "id", "name", "scope"}) {
      assertFalse(name, GrouperHttpClientLogRedactor.isSensitiveName(name));
    }
    for (String name : new String[] {"Authorization", "Cookie", "Set-Cookie", "X-Auth-Token", "X-Api-Key"}) {
      assertTrue(name, GrouperHttpClientLogRedactor.isSensitiveHeaderName(name));
    }
    assertFalse(GrouperHttpClientLogRedactor.isSensitiveHeaderName("Content-Type"));
  }

  /**
   * null, empty, ordinary diagnostics, and a very long value (no regex stack overflow)
   */
  public void testOrdinaryAndLongBodies() {
    assertNull(GrouperHttpClientLogRedactor.redactBody(null));
    assertEquals("", GrouperHttpClientLogRedactor.redactBody(""));
    String ordinary = "{\"displayName\":\"Test group\",\"members\":[\"a\",\"b\"],\"count\":2}";
    assertEquals(ordinary, GrouperHttpClientLogRedactor.redactBody(ordinary));

    StringBuilder longBody = new StringBuilder("{\"password\":\"");
    for (int i = 0; i < 1000000; i++) {
      longBody.append('x');
    }
    longBody.append("\",\"name\":\"ok\"}");
    assertEquals("{\"password\":\"*******\",\"name\":\"ok\"}",
        GrouperHttpClientLogRedactor.redactBody(longBody.toString()));
  }

  // ---------------------------------------------------------------------------------------
  // the real transcript (GrouperHttpClient logging block)

  /**
   * start a local server that answers every request with this body and a credential header
   * @param responseBody
   * @return the server (caller stops it)
   * @throws Exception
   */
  private static HttpServer startServer(final String responseBody) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", new HttpHandler() {

      @Override
      public void handle(HttpExchange exchange) {
        try {
          // drain the request (a read loop: InputStream.readAllBytes is Java 9+, 2.6.19 is Java 8)
          java.io.InputStream requestBody = exchange.getRequestBody();
          byte[] buffer = new byte[8192];
          while (requestBody.read(buffer) != -1) {
            // discard
          }
          byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.getResponseHeaders().add("X-Auth-Token", "HDR-SECRET-20");
          exchange.sendResponseHeaders(200, bytes.length);
          OutputStream outputStream = exchange.getResponseBody();
          outputStream.write(bytes);
          outputStream.close();
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          exchange.close();
        }
      }
    });
    server.start();
    return server;
  }

  /**
   * Dropbox style: raw form body with refresh token and client secret, JSON token response,
   * API key request header, credential response header.  No doNotLog flags set.
   * @throws Exception
   */
  public void testTranscriptRawBodyAndTokenResponse() throws Exception {
    HttpServer server = startServer("{\"access_token\":\"AT-SECRET-21\",\"token_type\":\"bearer\",\"expires_in\":14400}");
    String log = null;
    try {
      GrouperHttpClient.logStart(new GrouperHttpClientLog());
      try {
        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
        grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
        grouperHttpClient.assignUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/oauth2/token?api_key=KEY-SECRET-22");
        grouperHttpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
        grouperHttpClient.addHeader("X-Api-Key", "KEY-SECRET-23");
        grouperHttpClient.assignBody("grant_type=refresh_token&refresh_token=RT-SECRET-24&client_id=myClientId&client_secret=CS-SECRET-25");
        grouperHttpClient.executeRequest();
        assertEquals(200, grouperHttpClient.getResponseCode());
        // the caller still gets the real token
        assertTrue(grouperHttpClient.getResponseBody().contains("AT-SECRET-21"));
      } finally {
        log = GrouperHttpClient.logEnd();
      }
    } finally {
      server.stop(0);
    }
    assertNoSecrets(log, "AT-SECRET-21", "KEY-SECRET-22", "KEY-SECRET-23", "RT-SECRET-24", "CS-SECRET-25", "HDR-SECRET-20");
    // ordinary diagnostics are still there
    assertContainsAll(log, "HTTP method: ", "HTTP URL: http://127.0.0.1:", "/oauth2/token?api_key=*******",
        "HTTP response code: 200", "client_id=myClientId", "\"token_type\":\"bearer\"");
  }

  /**
   * Remedy style: form body parameters with a token name (only pass and secret were masked
   * before) and a bare JWT response
   * @throws Exception
   */
  public void testTranscriptBodyParamsAndJwtResponse() throws Exception {
    HttpServer server = startServer("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJKV1QtU0VDUkVUIn0.JWTsig_SECRET-26");
    String log = null;
    try {
      GrouperHttpClient.logStart(new GrouperHttpClientLog());
      try {
        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
        grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
        grouperHttpClient.assignUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/api/jwt/login");
        grouperHttpClient.addBodyParameter("username", "svcUser");
        grouperHttpClient.addBodyParameter("password", "PW-SECRET-27");
        grouperHttpClient.addBodyParameter("api_token", "TOK-SECRET-28");
        grouperHttpClient.executeRequest();
        assertEquals(200, grouperHttpClient.getResponseCode());
      } finally {
        log = GrouperHttpClient.logEnd();
      }
    } finally {
      server.stop(0);
    }
    assertNoSecrets(log, "PW-SECRET-27", "TOK-SECRET-28", "eyJzdWIiOiJKV1QtU0VDUkVUIn0", "JWTsig_SECRET-26");
    assertContainsAll(log, "HTTP request body param: username:svcUser", "HTTP response code: 200");
  }

}
