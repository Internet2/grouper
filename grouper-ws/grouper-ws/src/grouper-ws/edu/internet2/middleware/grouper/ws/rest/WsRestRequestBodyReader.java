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

import java.io.IOException;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestRequestContentType;

/**
 * <pre>
 * Reads and parses the body of a WS REST request for GrouperRestServlet, with a size limit and
 * without copying the body into logs, exceptions or the error response.
 *
 * Before this, the servlet read the whole body with IOUtils.toString(request.getReader()) (no
 * limit), and a malformed JSON body was logged at ERROR in full by
 * WsRestRequestContentType.json.parseString, put in full in that exception's message, again in
 * the cause's message (GrouperUtil.jsonConvertFrom), logged again by the servlet, and with the
 * default ws.throwExceptionsToClient sent back to the client as a stack trace (about 3 times the
 * request size).  A low privileged WS client could use that for heap, log and bandwidth pressure.
 *
 * readBody: refuses a declared Content-Length above ws.restMaxRequestBodyBytes before reading,
 * and stops reading as soon as more characters than the limit arrive (chunked requests, no or a
 * wrong Content-Length).  Characters are never more than bytes for the charsets used, so a body
 * within the byte limit is never refused.  Still uses request.getReader() so the request charset
 * handling is unchanged.
 *
 * parseBody: parses json with the configured converter directly (skips the full-body ERROR log
 * in WsRestRequestContentType) and xml with the normal parser; on failure logs one bounded WARN
 * line and throws a short GrouperRestInvalidRequest with NO cause, so the servlet answers HTTP 400
 * with a small message and nothing of the body is logged or returned.  http (form / query string)
 * bodies are parsed as before: their errors are about param names, not the body.
 *
 * Its own class (instead of inline in the servlet, or edits to WsRestRequestContentType and
 * GrouperUtil, which differ a lot across releases) so it is one version for every 5.x-7.x
 * release.  No commons-lang (lang in 5.x, lang3 in 6.x and later).
 * (security issue 114)
 * </pre>
 */
public class WsRestRequestBodyReader {

  /** grouper-ws.properties key: max REST request body size in bytes, zero or less means no limit */
  public static final String CONFIG_MAX_REQUEST_BODY_BYTES = "ws.restMaxRequestBodyBytes";

  /** default limit, 100 MB, large enough for big batch requests (e.g. many member adds) */
  public static final int DEFAULT_MAX_REQUEST_BODY_BYTES = 100000000;

  /** at most this many characters of a parser error message go in the WARN log */
  private static final int MAX_LOGGED_ERROR_CHARS = 200;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(WsRestRequestBodyReader.class);

  /**
   * no instances, static utility
   */
  private WsRestRequestBodyReader() {
  }

  /**
   * limit from config
   * @return the max body size in bytes, zero or less means no limit
   */
  public static int maxRequestBodyBytes() {
    return GrouperWsConfig.retrieveConfig().propertyValueInt(CONFIG_MAX_REQUEST_BODY_BYTES,
        DEFAULT_MAX_REQUEST_BODY_BYTES);
  }

  /**
   * read the request body with the configured limit
   * @param request the servlet request
   * @return the body, empty string if none
   * @throws IOException if reading fails
   * @throws WsRestRequestTooLarge if the body is over the limit
   */
  public static String readBody(HttpServletRequest request) throws IOException {
    return readBody(request, maxRequestBodyBytes());
  }

  /**
   * read the request body with a limit
   * @param request the servlet request
   * @param maxBytes max body size, zero or less means no limit
   * @return the body, empty string if none
   * @throws IOException if reading fails
   * @throws WsRestRequestTooLarge if the body is over the limit
   */
  public static String readBody(HttpServletRequest request, int maxBytes) throws IOException {

    // refuse a declared length over the limit before reading anything
    // (getContentLength is -1 when unknown, or when it does not fit in an int, then the loop below stops it)
    int declaredLength = request.getContentLength();
    if (maxBytes > 0 && declaredLength > maxBytes) {
      throw new WsRestRequestTooLarge("Request body is too large: Content-Length " + declaredLength
          + " is more than the limit of " + maxBytes + " bytes (" + CONFIG_MAX_REQUEST_BODY_BYTES + ")");
    }

    Reader reader = request.getReader();
    if (reader == null) {
      return "";
    }

    // read in chunks and stop as soon as the limit is passed, so an unknown or wrong length
    // cannot make us buffer more than about the limit
    StringBuilder body = new StringBuilder(declaredLength > 0 ? Math.min(declaredLength, 1000000) : 1024);
    char[] buffer = new char[8192];
    int read;
    while ((read = reader.read(buffer)) != -1) {
      if (maxBytes > 0 && (long) body.length() + read > maxBytes) {
        throw new WsRestRequestTooLarge("Request body is too large: more than the limit of " + maxBytes
            + " bytes (" + CONFIG_MAX_REQUEST_BODY_BYTES + ")");
      }
      body.append(buffer, 0, read);
    }
    return body.toString();
  }

  /**
   * parse the body to a request object, without the body ending up in logs, exceptions or the response
   * @param wsRestRequestContentType content type of the request
   * @param body the body, not blank
   * @param warnings where the converter writes warnings
   * @return the request object
   * @throws GrouperRestInvalidRequest (no cause, no body content) if the body cannot be parsed
   */
  public static Object parseBody(WsRestRequestContentType wsRestRequestContentType, String body,
      StringBuilder warnings) {

    // form / query string bodies: unchanged, the errors there are about params and lite object types
    if (wsRestRequestContentType == WsRestRequestContentType.http) {
      return wsRestRequestContentType.parseString(body, warnings);
    }

    try {
      if (wsRestRequestContentType == WsRestRequestContentType.json) {
        // call the converter directly: WsRestRequestContentType.json.parseString logs the whole body at ERROR
        return WsRestRequestContentType.jsonConverter().convertFromJson(body, warnings);
      }
      return wsRestRequestContentType.parseString(body, warnings);
    } catch (RuntimeException re) {

      // one bounded line for the admin: the cause's message can hold the start of the body, so cap and sanitize it
      int length = body == null ? 0 : body.length();
      LOG.warn("Could not parse WS REST request body, content type: " + wsRestRequestContentType
          + ", length: " + length + ", error: " + re.getClass().getName() + ": "
          + boundedForLog(re.getMessage()));

      // no cause: the cause chain holds the whole body, and the servlet logs and returns what it gets
      throw new GrouperRestInvalidRequest("Could not parse the request body as " + wsRestRequestContentType
          + " (length " + length + " characters), check the request syntax");
    }
  }

  /**
   * cap a message and drop control characters so it cannot forge log lines
   * @param message the message, may be null
   * @return the bounded message
   */
  static String boundedForLog(String message) {
    if (message == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < message.length() && i < MAX_LOGGED_ERROR_CHARS; i++) {
      char theChar = message.charAt(i);
      result.append(Character.isISOControl(theChar) ? ' ' : theChar);
    }
    if (message.length() > MAX_LOGGED_ERROR_CHARS) {
      result.append("...");
    }
    return result.toString();
  }

}
