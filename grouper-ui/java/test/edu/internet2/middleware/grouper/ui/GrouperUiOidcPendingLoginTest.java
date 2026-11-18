package edu.internet2.middleware.grouper.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.nimbusds.openid.connect.sdk.Nonce;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security fix (uiOidcCallbackCorrelation): the UI OIDC callback is only accepted in the
 * session that started the login, with the same config and state, within the max age, and
 * (5.11 and later) with a saved nonce.  Before the fix the state was never checked and a
 * callback in a session that never started a login was exchanged (login CSRF).
 * No container or database: the session is a small map backed proxy.
 */
public class GrouperUiOidcPendingLoginTest extends TestCase {

  /** a login url like GrouperOidc.generateLoginUrl makes */
  private static final String LOGIN_URL = "https://idp.example.edu/authorize?response_type=code&client_id=grouper"
      + "&redirect_uri=https%3A%2F%2Fgrouper.example.edu%2Fgrouper%2FgrouperUi%2Fapp%2FUiV2Main.oidc"
      + "&scope=openid&state=abc%2D123&nonce=n1";

  /** now */
  private static final long NOW = 1000000000L;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperUiOidcPendingLoginTest.class);
  }

  /**
   * @param name
   */
  public GrouperUiOidcPendingLoginTest(String name) {
    super(name);
  }

  /**
   * @return an http session backed by a map (attribute methods only)
   */
  private static HttpSession newHttpSession() {
    final Map<String, Object> attributes = new HashMap<String, Object>();
    return (HttpSession)Proxy.newProxyInstance(GrouperUiOidcPendingLoginTest.class.getClassLoader(),
        new Class<?>[] {HttpSession.class}, new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getAttribute".equals(method.getName())) {
              return attributes.get(args[0]);
            }
            if ("setAttribute".equals(method.getName())) {
              attributes.put((String)args[0], args[1]);
              return null;
            }
            if ("removeAttribute".equals(method.getName())) {
              attributes.remove(args[0]);
              return null;
            }
            throw new UnsupportedOperationException(method.getName());
          }
        });
  }

  /**
   * a session where GrouperOidc saved a nonce and the UI recorded the login
   * @param nonce
   * @return the session
   */
  private static HttpSession startedLogin(Nonce nonce) {
    HttpSession httpSession = newHttpSession();
    if (nonce != null) {
      httpSession.setAttribute(GrouperUiOidcPendingLogin.NONCE_SESSION_ATTRIBUTE, nonce);
    }
    GrouperUiOidcPendingLogin.recordPendingLogin(httpSession, "oidcA", LOGIN_URL, NOW);
    return httpSession;
  }

  /**
   * @param httpSession
   * @param configId
   * @param state
   * @param requireNonce
   * @param now
   */
  private static void assertRejected(HttpSession httpSession, String configId, String state, boolean requireNonce, long now) {
    try {
      GrouperUiOidcPendingLogin.consumePendingLogin(httpSession, configId, state, requireNonce, now);
      fail("callback should be rejected");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("could not be verified"));
    }
  }

  /**
   * the normal login in one session works, and only once
   */
  public void testSameSessionOnce() {
    Nonce nonce = new Nonce();
    HttpSession httpSession = startedLogin(nonce);
    assertNotNull(httpSession.getAttribute(GrouperUiOidcPendingLogin.SESSION_ATTRIBUTE));
    assertSame(nonce, GrouperUiOidcPendingLogin.consumePendingLogin(httpSession, "oidcA", "abc-123", true, NOW + 5000));
    assertNull(httpSession.getAttribute(GrouperUiOidcPendingLogin.SESSION_ATTRIBUTE));
    // replay in the same session
    assertRejected(httpSession, "oidcA", "abc-123", true, NOW + 6000);
  }

  /**
   * a callback in a session that never started a login (the attack), or with no session
   */
  public void testOtherSession() {
    startedLogin(new Nonce());
    assertRejected(newHttpSession(), "oidcA", "abc-123", true, NOW + 5000);
    assertRejected(null, "oidcA", "abc-123", true, NOW + 5000);
    // even without nonce checking (5.0-5.10)
    assertRejected(newHttpSession(), "oidcA", "abc-123", false, NOW + 5000);
  }

  /**
   * wrong, blank or missing state; a failed attempt also uses up the pending login
   */
  public void testState() {
    HttpSession httpSession = startedLogin(new Nonce());
    assertRejected(httpSession, "oidcA", "abc-124", true, NOW + 5000);
    // the pending login is gone, so the right state no longer works either
    assertRejected(httpSession, "oidcA", "abc-123", true, NOW + 6000);
    assertRejected(startedLogin(new Nonce()), "oidcA", "", true, NOW + 5000);
    assertRejected(startedLogin(new Nonce()), "oidcA", null, true, NOW + 5000);
  }

  /**
   * a login started for another config
   */
  public void testOtherConfig() {
    assertRejected(startedLogin(new Nonce()), "oidcB", "abc-123", true, NOW + 5000);
  }

  /**
   * older than the max age
   */
  public void testExpired() {
    assertRejected(startedLogin(new Nonce()), "oidcA", "abc-123", true, NOW + GrouperUiOidcPendingLogin.MAX_AGE_MILLIS + 1);
    assertNotNull(GrouperUiOidcPendingLogin.consumePendingLogin(startedLogin(new Nonce()), "oidcA", "abc-123", true,
        NOW + GrouperUiOidcPendingLogin.MAX_AGE_MILLIS));
  }

  /**
   * 5.11 and later need the saved nonce; 5.0-5.10 (no nonce) only check the state
   */
  public void testNonce() {
    assertRejected(startedLogin(null), "oidcA", "abc-123", true, NOW + 5000);
    assertNull(GrouperUiOidcPendingLogin.consumePendingLogin(startedLogin(null), "oidcA", "abc-123", false, NOW + 5000));
  }

  /**
   * the state is read from the login url; a url without one cannot start a login
   */
  public void testStateFromLoginUrl() {
    assertEquals("abc-123", GrouperUiOidcPendingLogin.queryParameter(LOGIN_URL, "state"));
    assertEquals("n1", GrouperUiOidcPendingLogin.queryParameter(LOGIN_URL, "nonce"));
    assertNull(GrouperUiOidcPendingLogin.queryParameter(LOGIN_URL, "missing"));
    try {
      GrouperUiOidcPendingLogin.recordPendingLogin(newHttpSession(), "oidcA", "https://idp.example.edu/authorize?client_id=x", NOW);
      fail("login url without state");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("no state"));
    }
  }

  /**
   * a second login in the same session replaces the first
   */
  public void testSecondLoginReplacesFirst() {
    HttpSession httpSession = startedLogin(new Nonce());
    Nonce nonce2 = new Nonce();
    httpSession.setAttribute(GrouperUiOidcPendingLogin.NONCE_SESSION_ATTRIBUTE, nonce2);
    GrouperUiOidcPendingLogin.recordPendingLogin(httpSession, "oidcA", LOGIN_URL.replace("abc%2D123", "def"), NOW + 1000);
    assertSame(nonce2, GrouperUiOidcPendingLogin.consumePendingLogin(httpSession, "oidcA", "def", true, NOW + 2000));
  }

}
