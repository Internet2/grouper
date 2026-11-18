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
package edu.internet2.middleware.grouper.ui;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * A UI OIDC login this browser session started, kept in the HTTP session so the OIDC callback
 * can be tied to it.
 *
 * Why this class exists (security fix): GrouperOidc.generateLoginUrl puts a random state (and
 * from 5.11 a nonce) in the authorization request, but the UI only saved the nonce, never
 * checked the returned state, and exchanged the authorization code even when the session had
 * no saved nonce.  So a callback url from someone else's login, opened in another browser,
 * logged that browser in as the other person (login CSRF).
 *
 * Now:
 * - recordPendingLogin, when the UI redirects to the OIDC provider: save the config id, the
 *   state (read from the login url), the nonce GrouperOidc put in the session, and the time
 * - consumePendingLogin, at the callback, before the code is exchanged: remove the record (one
 *   attempt only, also when the exchange later fails), then require it to exist, to be for the
 *   same config, the returned state to equal the saved one, the record to be under
 *   MAX_AGE_MILLIS old, and (5.11 and later) a saved nonce.  Else throw, nothing is exchanged
 * - rotateSessionId after a successful login, so a session id known before login is not
 *   the logged in session
 *
 * One pending login per session: a second login started in another tab replaces the first
 * (the nonce was already one per session before this fix).
 *
 * Plain Java only (no commons-lang, the nonce is kept as Object), so the same file works in
 * Grouper 5, 6 and 7.
 * </pre>
 */
public class GrouperUiOidcPendingLogin implements Serializable {

  /** serial version */
  private static final long serialVersionUID = 1L;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUiOidcPendingLogin.class);

  /** session attribute holding the pending login */
  public static final String SESSION_ATTRIBUTE = "grouperUiOidcPendingLogin";

  /** session attribute where GrouperOidc.generateLoginUrl(HttpServletRequest) puts the nonce */
  public static final String NONCE_SESSION_ATTRIBUTE = "oidcNonce";

  /** a login must come back from the OIDC provider within this time (10 minutes) */
  public static final long MAX_AGE_MILLIS = 10L * 60L * 1000L;

  /** message for the user, the reason is only logged */
  private static final String REJECTED_MESSAGE = "The OIDC login could not be verified for this browser session, please go back to Grouper and log in again";

  /** oidc external system config id */
  private String externalSystemConfigId;

  /** state sent in the authorization request */
  private String state;

  /** nonce sent in the authorization request (com.nimbusds.openid.connect.sdk.Nonce) or null */
  private Object nonce;

  /** when the login was started */
  private long createdMillis;

  /**
   * @param externalSystemConfigId
   * @param state
   * @param nonce
   * @param createdMillis
   */
  GrouperUiOidcPendingLogin(String externalSystemConfigId, String state, Object nonce, long createdMillis) {
    this.externalSystemConfigId = externalSystemConfigId;
    this.state = state;
    this.nonce = nonce;
    this.createdMillis = createdMillis;
  }

  /**
   * call after GrouperOidc.generateLoginUrl and before redirecting to it
   * @param httpServletRequest
   * @param externalSystemConfigId
   * @param loginUrl the authorization url from generateLoginUrl
   */
  public static void recordPendingLogin(HttpServletRequest httpServletRequest, String externalSystemConfigId, String loginUrl) {
    recordPendingLogin(httpServletRequest.getSession(), externalSystemConfigId, loginUrl, System.currentTimeMillis());
  }

  /**
   * @param httpSession
   * @param externalSystemConfigId
   * @param loginUrl
   * @param nowMillis
   */
  static void recordPendingLogin(HttpSession httpSession, String externalSystemConfigId, String loginUrl, long nowMillis) {
    String state = queryParameter(loginUrl, "state");
    if (state == null || state.length() == 0) {
      throw new RuntimeException("OIDC login url has no state parameter for config: " + externalSystemConfigId);
    }
    // 5.11 and later: GrouperOidc.generateLoginUrl(request) just put the nonce in the session
    Object nonce = httpSession.getAttribute(NONCE_SESSION_ATTRIBUTE);
    httpSession.setAttribute(SESSION_ATTRIBUTE, new GrouperUiOidcPendingLogin(externalSystemConfigId, state, nonce, nowMillis));
  }

  /**
   * call at the OIDC callback before the authorization code is exchanged.  Removes the
   * pending login and checks the callback belongs to it, else throws
   * @param httpServletRequest
   * @param externalSystemConfigId config the callback is for
   * @param returnedState the state request parameter
   * @param requireNonce true where GrouperOidc saves a nonce (5.11 and later)
   * @return the saved nonce (null only if requireNonce is false)
   */
  public static Object consumePendingLogin(HttpServletRequest httpServletRequest, String externalSystemConfigId,
      String returnedState, boolean requireNonce) {
    return consumePendingLogin(httpServletRequest.getSession(false), externalSystemConfigId, returnedState,
        requireNonce, System.currentTimeMillis());
  }

  /**
   * @param httpSession or null if the request has none
   * @param externalSystemConfigId
   * @param returnedState
   * @param requireNonce
   * @param nowMillis
   * @return the saved nonce
   */
  static Object consumePendingLogin(HttpSession httpSession, String externalSystemConfigId,
      String returnedState, boolean requireNonce, long nowMillis) {

    GrouperUiOidcPendingLogin pendingLogin = null;
    if (httpSession != null) {
      Object attribute = httpSession.getAttribute(SESSION_ATTRIBUTE);
      // one attempt per login: remove before any check so a failed or replayed callback cannot retry
      httpSession.removeAttribute(SESSION_ATTRIBUTE);
      if (attribute instanceof GrouperUiOidcPendingLogin) {
        pendingLogin = (GrouperUiOidcPendingLogin)attribute;
      }
    }

    String reason = null;
    if (pendingLogin == null) {
      reason = "no OIDC login was started in this session";
    } else if (externalSystemConfigId == null || !externalSystemConfigId.equals(pendingLogin.externalSystemConfigId)) {
      reason = "login was started for config '" + pendingLogin.externalSystemConfigId + "' not '" + externalSystemConfigId + "'";
    } else if (returnedState == null || returnedState.length() == 0) {
      reason = "no state returned";
    } else if (!MessageDigest.isEqual(pendingLogin.state.getBytes(StandardCharsets.UTF_8),
        returnedState.getBytes(StandardCharsets.UTF_8))) {
      reason = "state does not match";
    } else if (nowMillis - pendingLogin.createdMillis > MAX_AGE_MILLIS || nowMillis < pendingLogin.createdMillis) {
      reason = "login is older than " + (MAX_AGE_MILLIS / 1000) + " seconds";
    } else if (requireNonce && pendingLogin.nonce == null) {
      reason = "no nonce was saved";
    }

    if (reason != null) {
      LOG.warn("Rejected UI OIDC callback for config '" + externalSystemConfigId + "': " + reason);
      throw new RuntimeException(REJECTED_MESSAGE);
    }
    return pendingLogin.nonce;
  }

  /**
   * forget a pending login (e.g. the OIDC provider returned an error)
   * @param httpServletRequest
   */
  public static void discardPendingLogin(HttpServletRequest httpServletRequest) {
    HttpSession httpSession = httpServletRequest.getSession(false);
    if (httpSession != null) {
      httpSession.removeAttribute(SESSION_ATTRIBUTE);
    }
  }

  /**
   * give the session a new id after login (session fixation).  Attributes are kept.  If the
   * container does not support it (Servlet 3.0), log and go on
   * @param httpServletRequest
   */
  public static void rotateSessionId(HttpServletRequest httpServletRequest) {
    try {
      httpServletRequest.changeSessionId();
    } catch (IllegalStateException ise) {
      LOG.warn("Could not change the session id after OIDC login", ise);
    } catch (UnsupportedOperationException uoe) {
      LOG.warn("Could not change the session id after OIDC login", uoe);
    } catch (LinkageError le) {
      LOG.warn("Could not change the session id after OIDC login (servlet 3.1 needed)", le);
    }
  }

  /**
   * first value of a query parameter in a url, URL decoded
   * @param url
   * @param name
   * @return the value or null
   */
  static String queryParameter(String url, String name) {
    if (url == null) {
      return null;
    }
    int questionIndex = url.indexOf('?');
    if (questionIndex < 0) {
      return null;
    }
    String query = url.substring(questionIndex + 1);
    int hashIndex = query.indexOf('#');
    if (hashIndex >= 0) {
      query = query.substring(0, hashIndex);
    }
    for (String pair : query.split("&")) {
      int equalsIndex = pair.indexOf('=');
      String pairName = equalsIndex < 0 ? pair : pair.substring(0, equalsIndex);
      if (name.equals(urlDecode(pairName))) {
        return equalsIndex < 0 ? "" : urlDecode(pair.substring(equalsIndex + 1));
      }
    }
    return null;
  }

  /**
   * URL decode once (UTF-8); on a malformed escape return the input
   * @param string
   * @return the decoded string
   */
  static String urlDecode(String string) {
    try {
      return URLDecoder.decode(string, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("UTF-8 not supported", uee);
    } catch (IllegalArgumentException iae) {
      return string;
    }
  }

  /**
   * @return the config id
   */
  public String getExternalSystemConfigId() {
    return this.externalSystemConfigId;
  }

  /**
   * @return when the login was started
   */
  public long getCreatedMillis() {
    return this.createdMillis;
  }

}
