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

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersLiteRequest;

/**
 * <pre>
 * Tests that a WS REST write (add member, delete member, save) sent with HTTP GET or HEAD is
 * refused unless ws.rest.allowWritesWithGet is true, and that reads and writes sent with
 * POST / PUT / DELETE still work (security advisory C1).
 *
 * Plain unit test on WsRestHttpMethodValidator, no database or servlet container needed.
 * Before the fix this does not compile (WsRestHttpMethodValidator does not exist).  The servlet
 * wiring (the call right before dispatch in GrouperRestServlet) is covered by reading and the
 * compile checks.
 * </pre>
 */
public class WsRestHttpMethodValidatorTest extends TestCase {

  /**
   * run from eclipse or the command line
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WsRestHttpMethodValidatorTest.class);
  }

  /**
   * @param name
   */
  public WsRestHttpMethodValidatorTest(String name) {
    super(name);
  }

  /**
   * writes sent with GET or HEAD are refused by default, for put and delete type operations,
   * whether built from params (lite) or a body
   */
  public void testWriteWithGetRefused() {
    WsRequestBean[] writes = new WsRequestBean[] {new WsRestAddMemberLiteRequest(),
        new WsRestDeleteMemberLiteRequest(), new WsRestGroupSaveLiteRequest(), new WsRestAddMemberRequest()};
    for (WsRequestBean write : writes) {
      for (String httpMethod : new String[] {"GET", "get", "HEAD"}) {
        assertTrue(httpMethod + " " + write.getClass().getSimpleName(),
            WsRestHttpMethodValidator.isWriteWithGet(httpMethod, write));
        try {
          WsRestHttpMethodValidator.assertHttpMethodAllowed(httpMethod, write, false, "test.subject.0", "127.0.0.1");
          fail("should refuse " + httpMethod + " " + write.getClass().getSimpleName());
        } catch (GrouperRestInvalidRequest grir) {
          // the message names the operation and what to send instead
          assertTrue(grir.getMessage(), grir.getMessage().contains(write.getClass().getSimpleName()));
          assertTrue(grir.getMessage(), grir.getMessage().contains("POST"));
        }
      }
    }
  }

  /**
   * with the override on, a write with GET is allowed (logged at WARN, throttled)
   */
  public void testWriteWithGetAllowedByOverride() {
    // twice: the second one is throttled (no log), but must still be allowed
    for (int i = 0; i < 2; i++) {
      WsRestHttpMethodValidator.assertHttpMethodAllowed("GET", new WsRestAddMemberLiteRequest(), true,
          "test.subject.0", "127.0.0.1");
    }
  }

  /**
   * writes sent with POST, PUT or DELETE are not affected (no PUT/DELETE rule for params, see the
   * advisory C1 decisions): grouperClient always sends POST with a body
   */
  public void testWriteWithOtherMethodsAllowed() {
    for (String httpMethod : new String[] {"POST", "PUT", "DELETE"}) {
      assertFalse(WsRestHttpMethodValidator.isWriteWithGet(httpMethod, new WsRestAddMemberLiteRequest()));
      WsRestHttpMethodValidator.assertHttpMethodAllowed(httpMethod, new WsRestAddMemberLiteRequest(), false, null, null);
      WsRestHttpMethodValidator.assertHttpMethodAllowed(httpMethod, new WsRestDeleteMemberLiteRequest(), false, null, null);
      WsRestHttpMethodValidator.assertHttpMethodAllowed(httpMethod, new WsRestAddMemberRequest(), false, null, null);
    }
  }

  /**
   * reads with GET (and any other method) are not affected, and no request object means the
   * servlet dispatches on the real HTTP method, so nothing to check
   */
  public void testReadsAndNoObjectAllowed() {
    for (String httpMethod : new String[] {"GET", "HEAD", "POST", "PUT"}) {
      assertFalse(WsRestHttpMethodValidator.isWriteWithGet(httpMethod, new WsRestGetMembersLiteRequest()));
      WsRestHttpMethodValidator.assertHttpMethodAllowed(httpMethod, new WsRestGetMembersLiteRequest(), false, null, null);
      assertFalse(WsRestHttpMethodValidator.isWriteWithGet(httpMethod, null));
      WsRestHttpMethodValidator.assertHttpMethodAllowed(httpMethod, null, false, null, null);
    }
    assertFalse(WsRestHttpMethodValidator.isWriteWithGet(null, new WsRestAddMemberLiteRequest()));
  }

  /**
   * request values in the log line cannot forge extra log lines
   */
  public void testSanitizeForLog() {
    assertEquals("a_b_c", WsRestHttpMethodValidator.sanitizeForLog("a\nb\rc"));
    assertNull(WsRestHttpMethodValidator.sanitizeForLog(null));
    StringBuilder longValue = new StringBuilder();
    for (int i = 0; i < 500; i++) {
      longValue.append('x');
    }
    assertEquals(200, WsRestHttpMethodValidator.sanitizeForLog(longValue.toString()).length());
  }

}
