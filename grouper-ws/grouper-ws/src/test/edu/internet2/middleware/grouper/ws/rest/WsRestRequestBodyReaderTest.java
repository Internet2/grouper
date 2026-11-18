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
package edu.internet2.middleware.grouper.ws.rest;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestRequestContentType;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersRequest;

/**
 * <pre>
 * Tests the WS REST request body limit and that a malformed body is not copied into the
 * exception (and so not into the servlet's log or the error response) (security advisory M7).
 *
 * Unit test, no database or servlet container: the request is a java.lang.reflect.Proxy that
 * answers getContentLength and getReader.  The parse tests read grouper-ws config (jsonConverter)
 * so need the grouper-ws config on the classpath, as in Eclipse.  Before the fix this does not
 * compile (WsRestRequestBodyReader and WsRestRequestTooLarge do not exist).
 * </pre>
 */
public class WsRestRequestBodyReaderTest extends TestCase {

  /** marker put in malformed bodies, must never come back in an exception message */
  private static final String MARKER = "zzSecretMarker114zz";

  /**
   * run from eclipse or the command line
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WsRestRequestBodyReaderTest.class);
  }

  /**
   * @param name
   */
  public WsRestRequestBodyReaderTest(String name) {
    super(name);
  }

  /** set when the fake request's getReader is called */
  private boolean readerCalled = false;

  /**
   * fake request with a declared length and a body
   * @param contentLength what getContentLength returns, -1 for unknown
   * @param body the body
   * @return the request
   */
  private HttpServletRequest request(final int contentLength, final String body) {
    return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
        new Class<?>[] {HttpServletRequest.class}, new InvocationHandler() {

          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getContentLength".equals(method.getName())) {
              return contentLength;
            }
            if ("getReader".equals(method.getName())) {
              WsRestRequestBodyReaderTest.this.readerCalled = true;
              return new BufferedReader(new StringReader(body));
            }
            throw new UnsupportedOperationException(method.getName());
          }
        });
  }

  /**
   * @param length
   * @param theChar
   * @return a string of that length
   */
  private static String repeat(int length, char theChar) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < length; i++) {
      result.append(theChar);
    }
    return result.toString();
  }

  /**
   * a declared length over the limit is refused before the body is read
   * @throws Exception
   */
  public void testDeclaredLengthOverLimit() throws Exception {
    try {
      WsRestRequestBodyReader.readBody(request(1001, repeat(1001, 'a')), 1000);
      fail("should refuse");
    } catch (WsRestRequestTooLarge wrrtl) {
      assertTrue(wrrtl instanceof GrouperRestInvalidRequest);
      assertTrue(wrrtl.getMessage(), wrrtl.getMessage().contains("1000"));
    }
    assertFalse("body must not be read", this.readerCalled);
  }

  /**
   * unknown length (chunked) or a wrong declared length is stopped while reading
   * @throws Exception
   */
  public void testUnknownLengthOverLimit() throws Exception {
    for (int declared : new int[] {-1, 10}) {
      try {
        WsRestRequestBodyReader.readBody(request(declared, repeat(20001, 'a')), 20000);
        fail("should refuse, declared: " + declared);
      } catch (WsRestRequestTooLarge wrrtl) {
        // good
      }
    }
  }

  /**
   * exactly the limit, multibyte text, empty bodies and no limit all come back intact
   * @throws Exception
   */
  public void testWithinLimit() throws Exception {
    String body = repeat(20000, 'a');
    assertEquals(body, WsRestRequestBodyReader.readBody(request(20000, body), 20000));
    assertEquals(body, WsRestRequestBodyReader.readBody(request(-1, body), 20000));

    // 4 chars, 9 bytes in utf-8: the char count is what is limited, never more than bytes
    String multibyte = "a\u00e9\u4e2d\u00fc";
    assertEquals(multibyte, WsRestRequestBodyReader.readBody(request(9, multibyte), 9));

    assertEquals("", WsRestRequestBodyReader.readBody(request(0, ""), 20000));

    // zero or less means no limit
    assertEquals(body, WsRestRequestBodyReader.readBody(request(20000, body), 0));
    assertEquals(body, WsRestRequestBodyReader.readBody(request(20000, body), -1));
  }

  /**
   * malformed json (fails the wrapper check in GrouperUtil.jsonConvertFrom, or inside the json
   * parser) is a GrouperRestInvalidRequest without a cause and without the body in the message
   */
  public void testMalformedJsonNotEchoed() {
    String[] malformed = new String[] {
        repeat(5000, 'A') + MARKER,
        "{\"WsRestAddMemberRequest\":{\"x\":\"" + MARKER,
        "{\"" + MARKER + "\":{}}"};
    for (String body : malformed) {
      try {
        WsRestRequestBodyReader.parseBody(WsRestRequestContentType.json, body, new StringBuilder());
        fail("should not parse");
      } catch (GrouperRestInvalidRequest grir) {
        assertNull("no cause, the cause chain holds the body", grir.getCause());
        assertFalse(grir.getMessage(), grir.getMessage().contains(MARKER));
        assertTrue(grir.getMessage(), grir.getMessage().length() < 300);
      }
    }
  }

  /**
   * malformed xml is handled the same way
   */
  public void testMalformedXmlNotEchoed() {
    try {
      WsRestRequestBodyReader.parseBody(WsRestRequestContentType.xml, "<" + MARKER + "><x>", new StringBuilder());
      fail("should not parse");
    } catch (GrouperRestInvalidRequest grir) {
      assertNull(grir.getCause());
      assertFalse(grir.getMessage(), grir.getMessage().contains(MARKER));
    }
  }

  /**
   * a valid json body still parses to the request bean
   */
  public void testValidJsonParses() {
    Object object = WsRestRequestBodyReader.parseBody(WsRestRequestContentType.json,
        "{\"WsRestGetMembersRequest\":{\"memberFilter\":\"All\"}}", new StringBuilder());
    assertTrue(String.valueOf(object), object instanceof WsRestGetMembersRequest);
    assertEquals("All", ((WsRestGetMembersRequest) object).getMemberFilter());
  }

  /**
   * long parser messages in the log line are capped and control characters removed
   */
  public void testBoundedForLog() {
    assertNull(WsRestRequestBodyReader.boundedForLog(null));
    assertEquals("a b", WsRestRequestBodyReader.boundedForLog("a\nb"));
    assertEquals(203, WsRestRequestBodyReader.boundedForLog(repeat(5000, 'x')).length());
  }

}
