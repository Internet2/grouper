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

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * Decides whether a request to the SOAP web services (Axis2, /services/*) may be passed to Axis.
 *
 * Axis2 has "REST" support: unless axis2.xml sets disableREST to true, AxisServlet.doGet turns a
 * plain HTTP GET such as /services/GrouperService_v2_5/addMemberLite?groupName=...&subjectId=...
 * into a call of that operation, with the query parameters as its arguments (HEAD goes through
 * doGet too).  So any SOAP operation, including every write (add member, delete, save, assign
 * privileges, ...), could be run by a GET.  A GET is expected to be safe: links, image tags on
 * another web site, prefetchers and crawlers that hold credentials (for example HTTP Basic
 * credentials cached by a browser) could trigger writes as the user.  The REST servlet
 * (/servicesRest/*) refuses writes with GET since security issue 56; this is the same rule for
 * the SOAP servlet.
 *
 * Rule: a GET or HEAD is passed to Axis only when Axis serves metadata for it and runs no
 * operation, using the same tests as Axis2 1.6 AxisServlet.doGet: a query parameter named wsdl,
 * wsdl2, xsd or policy; a path ending in .wsdl or .xsd; or the listServices /
 * ListFaultyServices pages.  Any other GET or HEAD would be dispatched to an operation, so it is
 * refused with HTTP 400 and logged at ERROR.  Reads are refused too: Axis picks the operation
 * from the URL path, the SOAPAction header or the message, so the servlet cannot safely tell a
 * read from a write before dispatch, and real SOAP clients always POST.  POST, PUT and DELETE are
 * not changed (PUT and DELETE cannot be sent cross site without CORS, and form POSTs are handled
 * like issue 56 decided for REST; axis2.xml disableREST=true closes those as well).
 *
 * A site can turn the old behavior back on with ws.rest.allowWritesWithGet = true in
 * grouper-ws.properties, the same key that re-enables writes with GET on the REST servlet, so one
 * setting covers both.  Allowed requests are then logged at WARN at most once per WS user and
 * operation per hour, so sites can find the clients that need to change.
 *
 * Kept in its own class (instead of inline in the servlet) so it is one version for every
 * release with the SOAP web services (2.6, 4.x) and can be unit tested without a servlet
 * container.  Uses only the servlet API, GrouperWsConfig and GrouperUtil.getLog.  Java 8.
 * (security issue soapAxisRestGetWrites)
 * </pre>
 */
public class WsSoapHttpMethodValidator {

  /**
   * grouper-ws.properties key: true to let Axis2 run operations sent with HTTP GET or HEAD
   * (default false).  Same key as WsRestHttpMethodValidator (REST servlet)
   */
  public static final String CONFIG_ALLOW_WRITES_WITH_GET = "ws.rest.allowWritesWithGet";

  /** query parameter names for which Axis2 1.6 AxisServlet.doGet serves metadata, not an operation */
  private static final String[] METADATA_QUERY_PARAM_NAMES = new String[] {"wsdl2", "wsdl", "xsd", "policy"};

  /** path suffixes for which Axis2 1.6 AxisServlet.doGet serves the service list, not an operation */
  private static final String[] LIST_SERVICES_SUFFIXES = new String[] {"/services/listServices", "/services/ListFaultyServices"};

  /** only log an allowed GET (override on) once per this many millis per user and operation */
  private static final long WARN_THROTTLE_MILLIS = 60L * 60L * 1000L;

  /** if the throttle map gets this big, clear it (bounds memory if there are many users) */
  private static final int WARN_THROTTLE_MAX_KEYS = 10000;

  /** key is wsUser + "|" + operation, value is when the WARN was last logged */
  private static final Map<String, Long> warnLastLoggedMillis = new HashMap<String, Long>();

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(WsSoapHttpMethodValidator.class);

  /**
   * no instances, static utility
   */
  private WsSoapHttpMethodValidator() {
  }

  /**
   * called by GrouperServiceAxisServlet before the request goes to Axis.  If the request is
   * refused, the HTTP 400 response is written here and the caller must not pass it to Axis
   * @param request the servlet request
   * @param response the servlet response
   * @return true if the request may go to Axis, false if it was refused (response written)
   * @throws IOException if the error response cannot be written
   */
  public static boolean allowRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request == null || !isOperationWithGet(request.getMethod(), request.getRequestURI(), request.getQueryString())) {
      // nothing to decide, do not read config or the user on every SOAP POST
      return true;
    }
    boolean allowWritesWithGet = GrouperWsConfig.retrieveConfig().propertyValueBoolean(
        CONFIG_ALLOW_WRITES_WITH_GET, false);
    String refusal = refusalMessage(request.getMethod(), request.getRequestURI(), request.getQueryString(),
        allowWritesWithGet, wsUser(request), request.getRemoteAddr());
    if (refusal == null) {
      return true;
    }
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    if (!"HEAD".equalsIgnoreCase(request.getMethod())) {
      response.getWriter().write(refusal);
    }
    return false;
  }

  /**
   * see if Axis2 would run an operation for this request: a GET or HEAD that is not a metadata
   * (wsdl, xsd, policy) or service list request
   * @param httpMethod the HTTP method used, e.g. GET
   * @param requestUri the request URI as sent (not decoded), without the query string
   * @param queryString the query string as sent, may be null
   * @return true if GET or HEAD and Axis would dispatch it to an operation
   */
  public static boolean isOperationWithGet(String httpMethod, String requestUri, String queryString) {
    if (httpMethod == null) {
      return false;
    }
    if (!"GET".equalsIgnoreCase(httpMethod) && !"HEAD".equalsIgnoreCase(httpMethod)) {
      return false;
    }
    // same order and tests as AxisServlet.doGet (Axis2 1.6.4)
    if (hasMetadataQueryParam(queryString)) {
      return false;
    }
    String uri = requestUri == null ? "" : requestUri;
    if (uri.endsWith(".xsd") || uri.endsWith(".wsdl")) {
      return false;
    }
    for (String suffix : LIST_SERVICES_SUFFIXES) {
      if (uri.endsWith(suffix)) {
        return false;
      }
    }
    return true;
  }

  /**
   * the decision itself, no config or servlet access, so it can be unit tested
   * @param httpMethod the HTTP method used
   * @param requestUri the request URI, without the query string
   * @param queryString the query string, may be null
   * @param allowWritesWithGet value of ws.rest.allowWritesWithGet
   * @param wsUser who is calling, for the log only, may be null
   * @param clientIp client IP, for the log only, may be null
   * @return null if allowed, else the message for the client (the refusal is logged at ERROR)
   */
  public static String refusalMessage(String httpMethod, String requestUri, String queryString,
      boolean allowWritesWithGet, String wsUser, String clientIp) {

    if (!isOperationWithGet(httpMethod, requestUri, queryString)) {
      return null;
    }

    String operation = operationFromUri(requestUri);
    String details = "wsUser: " + sanitizeForLog(wsUser) + ", operation: " + sanitizeForLog(operation)
        + ", httpMethod: " + sanitizeForLog(httpMethod) + ", uri: " + sanitizeForLog(requestUri)
        + ", clientIp: " + sanitizeForLog(clientIp);

    if (allowWritesWithGet) {
      // backwards compatibility on, let it run but tell the site who still does this
      if (shouldWarn(wsUser + "|" + operation)) {
        LOG.warn("Allowed a WS SOAP operation sent with HTTP " + sanitizeForLog(httpMethod)
            + " since " + CONFIG_ALLOW_WRITES_WITH_GET + " is true, the client should send a SOAP"
            + " request with HTTP POST instead (logged once per user and operation per hour), " + details);
      }
      return null;
    }

    LOG.error("Refused a WS SOAP operation sent with HTTP " + sanitizeForLog(httpMethod) + ", " + details
        + ".  The client should send a SOAP request with HTTP POST.  "
        + "To allow operations with GET again (not recommended), set " + CONFIG_ALLOW_WRITES_WITH_GET
        + " = true in grouper-ws.properties");

    // message for the client: what to do, nothing echoed from the request
    return "SOAP web service operations cannot be sent with HTTP "
        + ("HEAD".equalsIgnoreCase(httpMethod) ? "HEAD" : "GET")
        + ", send a SOAP request with HTTP POST instead (or use the REST web services)";
  }

  /**
   * if the query string has a parameter named wsdl, wsdl2, xsd or policy (with or without a
   * value), parsed like Axis2 QueryStringParser: split on ampersand, name up to the equals sign,
   * not decoded, empty parameters skipped
   * @param queryString the query string, may be null
   * @return true if Axis serves metadata for it
   */
  static boolean hasMetadataQueryParam(String queryString) {
    if (queryString == null || queryString.length() == 0) {
      return false;
    }
    for (String param : queryString.split("&")) {
      if (param.length() == 0) {
        continue;
      }
      int equalsIndex = param.indexOf('=');
      String name = equalsIndex == -1 ? param : param.substring(0, equalsIndex);
      for (String metadataName : METADATA_QUERY_PARAM_NAMES) {
        if (metadataName.equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * the last path segment, which is what Axis dispatches on for a URL request.  For the log only
   * @param requestUri the request URI
   * @return the operation or null
   */
  static String operationFromUri(String requestUri) {
    if (requestUri == null) {
      return null;
    }
    int slashIndex = requestUri.lastIndexOf('/');
    return slashIndex == -1 ? requestUri : requestUri.substring(slashIndex + 1);
  }

  /**
   * throttle the WARN for allowed GET operations
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
   * who is calling, from the request (user principal, remote user or the REMOTE_USER attribute
   * the WS filter sets), never throws.  For the log only
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
