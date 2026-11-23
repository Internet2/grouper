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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * <pre>
 * Decides whether the HTTP method of a WS REST request may run the operation of its request
 * object.
 *
 * GrouperRestServlet dispatches on requestObject.retrieveRestHttpMethod() (e.g. PUT for
 * WsRestAddMemberLiteRequest), not on the HTTP method actually used, and the request object can
 * be built from URL params (wsLiteObjectType=...).  So without this check an HTTP GET (or HEAD)
 * can run add member, delete, save and other writes.  A GET is expected to be safe: links, image
 * tags on another web site, prefetchers and crawlers that hold credentials (e.g. HTTP Basic
 * credentials cached by a browser) could trigger writes as the user.
 *
 * Rule: if the HTTP method is GET or HEAD and the operation is not a GET (read) operation,
 * refuse the request (GrouperRestInvalidRequest, which the servlet turns into HTTP 400).
 * Reads are untouched for every HTTP method, and writes sent with POST, PUT or DELETE (with a
 * body or with params) keep working.
 *
 * A site can re-enable writes with GET for backwards compatibility with
 * ws.rest.allowWritesWithGet = true in grouper-ws.properties.  Refused writes are logged at
 * ERROR, allowed ones (override on) at WARN at most once per WS user and operation per hour, so
 * sites can find the clients that need to change.
 *
 * Kept in its own class (instead of inline in the servlet) so it is one version for every
 * 5.x-7.x release and can be unit tested without a servlet container.  Uses only the servlet
 * API, WsRequestBean, GrouperRestHttpMethod, GrouperRestInvalidRequest, GrouperWsConfig and
 * GrouperUtil.getLog, all the same in every release (no commons-lang, which is lang in 5.x and
 * lang3 in 6.x and later).
 * (GRP-7577)
 * </pre>
 */
public class WsRestHttpMethodValidator {

  /** grouper-ws.properties key: true to allow writes sent with HTTP GET or HEAD (default false) */
  public static final String CONFIG_ALLOW_WRITES_WITH_GET = "ws.rest.allowWritesWithGet";

  /** only log an allowed GET write (override on) once per this many millis per user and operation */
  private static final long WARN_THROTTLE_MILLIS = 60L * 60L * 1000L;

  /** if the throttle map gets this big, clear it (bounds memory if there are many users) */
  private static final int WARN_THROTTLE_MAX_KEYS = 10000;

  /** key is wsUser + "|" + operation, value is when the WARN was last logged */
  private static final Map<String, Long> warnLastLoggedMillis = new HashMap<String, Long>();

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(WsRestHttpMethodValidator.class);

  /**
   * no instances, static utility
   */
  private WsRestHttpMethodValidator() {
  }

  /**
   * called by GrouperRestServlet right before dispatch.  Reads the override from config and
   * the WS user and client IP from the request (for the log only)
   * @param request the servlet request
   * @param requestObject the request object (from the body or the params), may be null
   * @throws GrouperRestInvalidRequest if a write is sent with GET or HEAD and the override is off
   */
  public static void assertHttpMethodAllowed(HttpServletRequest request, WsRequestBean requestObject) {
    if (request == null || !isWriteWithGet(request.getMethod(), requestObject)) {
      // nothing to decide, do not read config or the user on every read request
      return;
    }
    boolean allowWritesWithGet = GrouperWsConfig.retrieveConfig().propertyValueBoolean(
        CONFIG_ALLOW_WRITES_WITH_GET, false);
    assertHttpMethodAllowed(request.getMethod(), requestObject, allowWritesWithGet,
        wsUser(request), request.getRemoteAddr());
  }

  /**
   * see if this is a write operation sent with an HTTP GET or HEAD
   * @param httpMethod the HTTP method used, e.g. GET
   * @param requestObject the request object, may be null (then the servlet dispatches on the
   * real HTTP method, so it is never a write with GET)
   * @return true if GET or HEAD and the operation is not a GET operation
   */
  public static boolean isWriteWithGet(String httpMethod, WsRequestBean requestObject) {
    if (requestObject == null || httpMethod == null) {
      return false;
    }
    if (!"GET".equalsIgnoreCase(httpMethod) && !"HEAD".equalsIgnoreCase(httpMethod)) {
      return false;
    }
    GrouperRestHttpMethod operationMethod = requestObject.retrieveRestHttpMethod();
    return operationMethod != GrouperRestHttpMethod.GET;
  }

  /**
   * the decision itself, no config or servlet access, so it can be unit tested
   * @param httpMethod the HTTP method used
   * @param requestObject the request object, may be null
   * @param allowWritesWithGet value of ws.rest.allowWritesWithGet
   * @param wsUser who is calling, for the log only, may be null
   * @param clientIp client IP, for the log only, may be null
   * @throws GrouperRestInvalidRequest if a write is sent with GET or HEAD and the override is off
   */
  public static void assertHttpMethodAllowed(String httpMethod, WsRequestBean requestObject,
      boolean allowWritesWithGet, String wsUser, String clientIp) {

    if (!isWriteWithGet(httpMethod, requestObject)) {
      return;
    }

    String operation = requestObject.getClass().getSimpleName();
    String requiredMethod = String.valueOf(requestObject.retrieveRestHttpMethod());
    String details = "wsUser: " + sanitizeForLog(wsUser) + ", operation: " + operation
        + ", httpMethod: " + sanitizeForLog(httpMethod) + ", clientIp: " + sanitizeForLog(clientIp);

    if (allowWritesWithGet) {
      // backwards compatibility on, let it run but tell the site who still does this
      if (shouldWarn(wsUser + "|" + operation)) {
        LOG.warn("Allowed a WS REST write sent with HTTP " + sanitizeForLog(httpMethod)
            + " since " + CONFIG_ALLOW_WRITES_WITH_GET + " is true, the client should send "
            + requiredMethod + " or POST instead (logged once per user and operation per hour), " + details);
      }
      return;
    }

    LOG.error("Refused a WS REST write sent with HTTP " + sanitizeForLog(httpMethod) + ", " + details
        + ".  The client should send " + requiredMethod + " or POST (with params or a body).  "
        + "To allow writes with GET again (not recommended), set " + CONFIG_ALLOW_WRITES_WITH_GET
        + " = true in grouper-ws.properties");

    // message for the client: the operation and what to do, nothing else from the request
    throw new GrouperRestInvalidRequest("Operation " + operation + " changes data and cannot be sent with HTTP "
        + ("HEAD".equalsIgnoreCase(httpMethod) ? "HEAD" : "GET") + ", send it with HTTP " + requiredMethod
        + " or POST instead");
  }

  /**
   * throttle the WARN for allowed GET writes
   * @param key user and operation
   * @return true if it should be logged now
   */
  private static boolean shouldWarn(String key) {
    long now = System.currentTimeMillis();
    synchronized (warnLastLoggedMillis) {
      Long last = warnLastLoggedMillis.get(key);
      if (last != null && now - last < WARN_THROTTLE_MILLIS) {
        return false;
      }
      if (warnLastLoggedMillis.size() >= WARN_THROTTLE_MAX_KEYS) {
        warnLastLoggedMillis.clear();
      }
      warnLastLoggedMillis.put(key, now);
      return true;
    }
  }

  /**
   * who is calling, from the request (same sources as
   * GrouperServiceJ2ee.retrieveUserPrincipalNameFromRequest, but never throws).  For the log only
   * @param request the servlet request
   * @return the user or null
   */
  private static String wsUser(HttpServletRequest request) {
    try {
      Principal principal = request.getUserPrincipal();
      if (principal != null) {
        return principal.getName();
      }
      String remoteUser = request.getRemoteUser();
      if (remoteUser != null && remoteUser.length() > 0) {
        return remoteUser;
      }
      Object remoteUserAttribute = request.getAttribute("REMOTE_USER");
      return remoteUserAttribute == null ? null : remoteUserAttribute.toString();
    } catch (RuntimeException re) {
      // the log line is only diagnostic, never fail the request here
      return null;
    }
  }

  /**
   * keep request values from forging log lines: drop control characters and cap the length
   * @param value the value
   * @return the safe value
   */
  static String sanitizeForLog(String value) {
    if (value == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < value.length() && result.length() < 200; i++) {
      char theChar = value.charAt(i);
      result.append(Character.isISOControl(theChar) ? '_' : theChar);
    }
    return result.toString();
  }

}
