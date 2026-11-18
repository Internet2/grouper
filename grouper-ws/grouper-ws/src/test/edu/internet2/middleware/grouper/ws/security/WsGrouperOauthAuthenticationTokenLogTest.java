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
package edu.internet2.middleware.grouper.ws.security;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import edu.internet2.middleware.grouper.authentication.GrouperTokenLogFingerprint;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import junit.textui.TestRunner;

/**
 * <pre>
 * SECURITY tests for bearer token logging in WsGrouperOauthAuthentication, issue
 * advisory H7.
 *
 * With ws.security.authn.oauth.debugAccessTokens=true and DEBUG on for the class, the whole bearer
 * token was written to the log (a log reader could replay it), and otherwise its first 10 chars
 * were (which also threw StringIndexOutOfBoundsException for a token under 10 chars).  Now only
 * GrouperTokenLogFingerprint.fingerprint (truncated SHA-256 plus length) is logged.
 *
 * Before the fix this does not compile (GrouperTokenLogFingerprint is missing), and the captured
 * log holds the token.
 * </pre>
 */
public class WsGrouperOauthAuthenticationTokenLogTest extends GrouperTest {

  /** distinctive synthetic token, JWT-shaped so it starts like a real one */
  private static final String TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0b2tlbkxvZ1Rlc3QifQ.c3ludGhldGljU2lnbmF0dXJlWFlaMTIz";

  /**
   *
   */
  public WsGrouperOauthAuthenticationTokenLogTest() {
    super();
  }

  /**
   * @param name
   */
  public WsGrouperOauthAuthenticationTokenLogTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsGrouperOauthAuthenticationTokenLogTest("testDebugLogHasFingerprintNotToken"));
  }

  /**
   * captures log events in memory
   */
  private static class CapturingAppender extends AbstractAppender {

    /** formatted messages */
    private final List<String> messages = Collections.synchronizedList(new ArrayList<String>());

    /**
     * @param name
     */
    CapturingAppender(String name) {
      super(name, null, null, true, Property.EMPTY_ARRAY);
    }

    /**
     * @see org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.core.LogEvent)
     */
    public void append(LogEvent event) {
      this.messages.add(event.getMessage().getFormattedMessage());
    }

    /**
     * @return all messages joined
     */
    String all() {
      synchronized (this.messages) {
        return String.join("\n", this.messages);
      }
    }
  }

  /**
   * the fingerprint never contains the token or a prefix of it, is stable, tells tokens apart, and
   * handles null, blank and short tokens
   */
  public void testFingerprint() {
    String fingerprint = GrouperTokenLogFingerprint.fingerprint(TOKEN);
    assertTrue(fingerprint, fingerprint.startsWith("sha256:"));
    assertTrue(fingerprint, fingerprint.endsWith(" len=" + TOKEN.length()));
    assertFalse(fingerprint.contains(TOKEN));
    assertFalse(fingerprint.contains(TOKEN.substring(0, 10)));
    assertFalse(fingerprint.contains(TOKEN.substring(0, 5)));
    // 12 hex chars
    assertEquals("sha256:".length() + 12, fingerprint.indexOf(' '));

    assertEquals(fingerprint, GrouperTokenLogFingerprint.fingerprint(TOKEN));
    assertFalse(fingerprint.equals(GrouperTokenLogFingerprint.fingerprint(TOKEN + "x")));

    assertEquals("null", GrouperTokenLogFingerprint.fingerprint(null));
    assertEquals("blank", GrouperTokenLogFingerprint.fingerprint("  "));
    String shortFingerprint = GrouperTokenLogFingerprint.fingerprint("abc");
    assertTrue(shortFingerprint, shortFingerprint.endsWith(" len=3"));
    assertFalse(shortFingerprint.contains("abc"));
  }

  /**
   * debugAccessTokens=true and DEBUG on: the log has the fingerprint and not the token.  The config
   * id does not exist, so authentication fails after the debug map is filled (failed validation
   * path; with advisory H7 merged it is refused, without it it throws)
   */
  public void testDebugLogHasFingerprintNotToken() {
    assertLogged(TOKEN, "true");
    assertLogged(TOKEN, "false");
  }

  /**
   * a bearer value under 10 chars used to throw StringIndexOutOfBoundsException
   */
  public void testShortTokenDoesNotThrowIndexOutOfBounds() {
    assertLogged("abc", "false");
  }

  /**
   * run the class with DEBUG on and check what it logged
   * @param token
   * @param debugAccessTokens
   */
  private void assertLogged(String token, String debugAccessTokens) {

    String loggerName = WsGrouperOauthAuthentication.class.getName();
    Logger logger = (Logger) LogManager.getLogger(loggerName);
    Level oldLevel = logger.getLevel();
    CapturingAppender appender = new CapturingAppender("tokenLogTest" + System.nanoTime());
    appender.start();
    logger.addAppender(appender);
    logger.setLevel(Level.DEBUG);

    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.security.authn.oauth.debugAccessTokens", debugAccessTokens);
    try {
      try {
        new WsGrouperOauthAuthentication().retrieveLoggedInSubjectId(request(
            "tokenLogTestDoesNotExist" + System.currentTimeMillis(), token));
      } catch (StringIndexOutOfBoundsException sioobe) {
        fail("short token should not throw: " + sioobe);
      } catch (RuntimeException re) {
        // expected without advisory H7: unknown config id
      }
      String log = appender.all();
      assertTrue(log, log.contains(GrouperTokenLogFingerprint.fingerprint(token)));
      assertFalse(log, log.contains(token));
      if (token.length() >= 10) {
        assertFalse(log, log.contains(token.substring(0, 10)));
      }
    } finally {
      GrouperWsConfig.retrieveConfig().propertiesOverrideMap().remove("ws.security.authn.oauth.debugAccessTokens");
      logger.removeAppender(appender);
      logger.setLevel(oldLevel);
      appender.stop();
    }
  }

  /**
   * a WS request with a bearer token and a config id parameter
   * @param configId
   * @param accessToken
   * @return the request
   */
  private static HttpServletRequest request(final String configId, final String accessToken) {
    InvocationHandler handler = new InvocationHandler() {
      public Object invoke(Object proxy, Method method, Object[] args) {
        if ("getParameter".equals(method.getName()) && "grouperOidcConfigId".equals(args[0])) {
          return configId;
        }
        if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
          return "Bearer " + accessToken;
        }
        if ("getRequestURI".equals(method.getName())) {
          return "/grouper-ws/servicesRest/v4_0_000/groups";
        }
        return null;
      }
    };
    return (HttpServletRequest) Proxy.newProxyInstance(WsGrouperOauthAuthenticationTokenLogTest.class.getClassLoader(),
        new Class<?>[] { HttpServletRequest.class }, handler);
  }

}
