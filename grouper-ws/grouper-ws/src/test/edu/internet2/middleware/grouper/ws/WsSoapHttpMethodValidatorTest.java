/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security issue soapAxisRestGetWrites: the SOAP web services (Axis2, /services/*) ran any
 * operation, including writes such as addMemberLite, from a plain HTTP GET, because Axis2 "REST"
 * support turns the URL path and query parameters into an operation call.
 * GrouperServiceAxisServlet now asks WsSoapHttpMethodValidator first: a GET or HEAD is passed to
 * Axis only for metadata (wsdl, xsd, policy, listServices), unless ws.rest.allowWritesWithGet is
 * true.
 *
 * Plain TestCase, no database or servlet container: tests the decision methods.  The servlet
 * wiring is covered by the manual test steps (real HTTP against the WS).  Java 8 (2.6.19).
 * Before the fix this does not compile (WsSoapHttpMethodValidator is new).
 * </pre>
 */
public class WsSoapHttpMethodValidatorTest extends TestCase {

  /** a SOAP service path as the container sees it */
  private static final String SERVICE = "/grouper-ws/services/GrouperService_v2_5";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsSoapHttpMethodValidatorTest("testOperationWithGetRefused"));
  }

  /**
   * @param name
   */
  public WsSoapHttpMethodValidatorTest(String name) {
    super(name);
  }

  /**
   * a write or a read sent with GET or HEAD (any case) is refused, the message says to POST
   */
  public void testOperationWithGetRefused() {
    String[] uris = new String[] {
        SERVICE + "/addMemberLite",
        SERVICE + "/deleteMemberLite",
        SERVICE + "/groupSaveLite",
        SERVICE + "/getMembersLite",
        SERVICE + ".GrouperService_v2_5HttpSoap11Endpoint/addMemberLite",
        SERVICE,
        "/grouper-ws/services/GrouperService/addMember"};
    for (String uri : uris) {
      for (String method : new String[] {"GET", "get", "HEAD"}) {
        assertTrue(method + " " + uri, WsSoapHttpMethodValidator.isOperationWithGet(method, uri,
            "clientVersion=v2_5_000&groupName=test:a&subjectId=test.subject.1"));
        String message = WsSoapHttpMethodValidator.refusalMessage(method, uri,
            "clientVersion=v2_5_000&groupName=test:a&subjectId=test.subject.1", false, "test.subject.0", "127.0.0.1");
        assertNotNull(method + " " + uri, message);
        assertTrue(message, message.contains("POST"));
        // nothing from the request is echoed to the client
        assertFalse(message, message.contains("test:a"));
      }
    }
    // no query string at all is still an operation call
    assertTrue(WsSoapHttpMethodValidator.isOperationWithGet("GET", SERVICE + "/addMemberLite", null));
  }

  /**
   * with ws.rest.allowWritesWithGet true the old behavior is back (second call: WARN throttled)
   */
  public void testOperationWithGetAllowedByOverride() {
    assertNull(WsSoapHttpMethodValidator.refusalMessage("GET", SERVICE + "/addMemberLite",
        "groupName=test:a", true, "test.subject.0", "127.0.0.1"));
    assertNull(WsSoapHttpMethodValidator.refusalMessage("GET", SERVICE + "/addMemberLite",
        "groupName=test:a", true, "test.subject.0", "127.0.0.1"));
  }

  /**
   * metadata and the service list stay available with GET, the same tests Axis uses
   */
  public void testMetadataAllowed() {
    String[][] cases = new String[][] {
        {SERVICE, "wsdl"},
        {SERVICE, "wsdl2"},
        {SERVICE, "xsd=xsd0.xsd"},
        {SERVICE, "policy"},
        {SERVICE, "a=b&wsdl"},
        {SERVICE + "/addMemberLite", "wsdl"},
        {"/grouper-ws/services/listServices", null},
        {"/grouper-ws/services/ListFaultyServices", null},
        {SERVICE + "/GrouperService_v2_5.wsdl", null},
        {SERVICE + "/xsd0.xsd", null}};
    for (String[] theCase : cases) {
      assertFalse(theCase[0] + "?" + theCase[1], WsSoapHttpMethodValidator.isOperationWithGet("GET", theCase[0], theCase[1]));
      assertNull(theCase[0] + "?" + theCase[1], WsSoapHttpMethodValidator.refusalMessage("GET", theCase[0], theCase[1],
          false, "test.subject.0", "127.0.0.1"));
    }
    // a parameter that only contains or resembles a metadata name is not metadata (Axis matches
    // the exact parameter name)
    assertTrue(WsSoapHttpMethodValidator.isOperationWithGet("GET", SERVICE + "/addMemberLite", "groupName=wsdl"));
    assertTrue(WsSoapHttpMethodValidator.isOperationWithGet("GET", SERVICE + "/addMemberLite", "WSDL"));
    assertTrue(WsSoapHttpMethodValidator.isOperationWithGet("GET", SERVICE + "/addMemberLite", "wsdlx=1"));
    assertTrue(WsSoapHttpMethodValidator.isOperationWithGet("GET", SERVICE + "/addMemberLite", "&&"));
    assertTrue(WsSoapHttpMethodValidator.hasMetadataQueryParam("&&wsdl"));
    assertFalse(WsSoapHttpMethodValidator.hasMetadataQueryParam(""));
  }

  /**
   * POST (the SOAP binding), PUT and DELETE are not changed by this rule, nor a null method
   */
  public void testOtherMethodsAllowed() {
    for (String method : new String[] {"POST", "PUT", "DELETE", "OPTIONS", null}) {
      assertFalse(String.valueOf(method), WsSoapHttpMethodValidator.isOperationWithGet(method, SERVICE + "/addMemberLite", "groupName=test:a"));
      assertNull(String.valueOf(method), WsSoapHttpMethodValidator.refusalMessage(method, SERVICE + "/addMemberLite",
          "groupName=test:a", false, "test.subject.0", "127.0.0.1"));
    }
  }

  /**
   * the operation for the log is the last path segment; log values cannot forge lines
   */
  public void testOperationAndSanitizeForLog() {
    assertEquals("addMemberLite", WsSoapHttpMethodValidator.operationFromUri(SERVICE + "/addMemberLite"));
    assertEquals("GrouperService_v2_5", WsSoapHttpMethodValidator.operationFromUri(SERVICE));
    assertNull(WsSoapHttpMethodValidator.operationFromUri(null));

    assertNull(WsSoapHttpMethodValidator.sanitizeForLog(null));
    assertEquals("a_b_c", WsSoapHttpMethodValidator.sanitizeForLog("a\nb\rc"));
    StringBuilder longValue = new StringBuilder();
    for (int i = 0; i < 500; i++) {
      longValue.append('x');
    }
    assertEquals(200, WsSoapHttpMethodValidator.sanitizeForLog(longValue.toString()).length());
  }

}
