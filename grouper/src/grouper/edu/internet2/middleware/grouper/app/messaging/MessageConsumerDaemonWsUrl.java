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

package edu.internet2.middleware.grouper.app.messaging;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Builds the web service URL the messaging-to-WS bridge (MessageConsumerDaemon) posts to,
 * from the configured base URL (grouper.messaging.&lt;configId&gt;.ws.url) and the
 * grouperHeader.httpPath of a queue message.
 * <p>
 * The message is not trusted: whoever can publish to the bridge queue chooses httpPath.
 * Before this class the two were concatenated, so a path like "@evil:8081/x" turned the
 * configured host into user-info and sent the bridge's Basic credentials and act-as headers
 * to another host.  Here the path must be a plain absolute path under the configured base
 * path and under a "servicesRest" segment (the bridge only sends WsRest*Request bodies), and
 * the joined URL must keep the configured scheme and authority (host and port).
 * </p>
 * <p>
 * Uses only java.net.URI and plain string checks so the same class works in every Grouper
 * release that has the bridge.  Error messages are fixed text: they go into the reply message
 * and the log, so they never echo the path, the URL or any credential.
 * </p>
 */
public class MessageConsumerDaemonWsUrl {

  /**
   * path segment the joined URL must contain: Grouper WS REST requests live under it
   */
  public static final String SERVICES_REST_SEGMENT = "servicesRest";

  /**
   * no instances
   */
  private MessageConsumerDaemonWsUrl() {
  }

  /**
   * join the configured base URL and the message path, or throw if the path could send the
   * request (and the bridge credentials) anywhere other than the configured Grouper WS REST
   * endpoint
   * @param wsBaseUrl configured grouper.messaging.&lt;configId&gt;.ws.url
   * @param httpPath grouperHeader.httpPath from the queue message (untrusted)
   * @return the URL to post to
   * @throws IllegalArgumentException with a fixed-text reason if the base URL or path is not allowed
   */
  public static String buildWsUrl(String wsBaseUrl, String httpPath) {

    // ---- the configured base URL (trusted, but check it so a bad config fails closed)
    if (wsBaseUrl == null || wsBaseUrl.trim().length() == 0) {
      throw new IllegalArgumentException("Messaging bridge ws.url is not configured");
    }
    URI base = null;
    try {
      base = new URI(wsBaseUrl.trim());
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Messaging bridge ws.url is not a valid URL");
    }
    String scheme = base.getScheme();
    if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
      throw new IllegalArgumentException("Messaging bridge ws.url must be an http or https URL");
    }
    // compare the raw authority, not getHost(): getHost() is null for host names with an
    // underscore (common for docker service names), which must keep working
    String baseAuthority = base.getRawAuthority();
    if (baseAuthority == null || baseAuthority.length() == 0) {
      throw new IllegalArgumentException("Messaging bridge ws.url must have a host");
    }
    // credentials belong in ws.username / ws.password, never in the URL
    if (base.getRawUserInfo() != null || baseAuthority.indexOf('@') >= 0) {
      throw new IllegalArgumentException("Messaging bridge ws.url must not contain user info");
    }
    if (base.getRawQuery() != null || base.getRawFragment() != null) {
      throw new IllegalArgumentException("Messaging bridge ws.url must not have a query or fragment");
    }
    String basePath = base.getRawPath() == null ? "" : base.getRawPath();
    while (basePath.endsWith("/")) {
      basePath = basePath.substring(0, basePath.length() - 1);
    }

    // ---- the message path (untrusted)
    validateHttpPath(httpPath);

    // ---- join from the parsed parts (not the raw config string) and re-check the result
    String joined = base.getScheme() + "://" + baseAuthority + basePath + httpPath;
    URI joinedUri = null;
    try {
      joinedUri = new URI(joined);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Messaging bridge httpPath is not a valid URL path");
    }
    if (!scheme.equalsIgnoreCase(joinedUri.getScheme())
        || !baseAuthority.equalsIgnoreCase(joinedUri.getRawAuthority())
        || joinedUri.getRawUserInfo() != null
        || joinedUri.getRawQuery() != null
        || joinedUri.getRawFragment() != null) {
      throw new IllegalArgumentException("Messaging bridge httpPath must not change the ws.url host");
    }
    String joinedPath = joinedUri.getRawPath();
    if (joinedPath == null || !joinedPath.startsWith(basePath + "/")) {
      throw new IllegalArgumentException("Messaging bridge httpPath must stay under the ws.url path");
    }
    // normalize() would remove dot segments; the path was checked to have none, so it must not change
    if (!joinedPath.equals(joinedUri.normalize().getRawPath())) {
      throw new IllegalArgumentException("Messaging bridge httpPath must not contain dot segments");
    }
    return joined;
  }

  /**
   * check the shape of the untrusted path
   * @param httpPath
   */
  static void validateHttpPath(String httpPath) {
    if (httpPath == null || httpPath.length() == 0) {
      throw new IllegalArgumentException("Messaging bridge httpPath is required");
    }
    // exactly one leading slash: "//host/x" would be a network-path reference
    if (httpPath.charAt(0) != '/' || (httpPath.length() > 1 && httpPath.charAt(1) == '/')) {
      throw new IllegalArgumentException("Messaging bridge httpPath must start with a single /");
    }
    for (int i = 0; i < httpPath.length(); i++) {
      char c = httpPath.charAt(i);
      // printable ASCII only: no whitespace, CR/LF, control or non-ASCII characters
      if (c <= 0x20 || c >= 0x7f) {
        throw new IllegalArgumentException("Messaging bridge httpPath has a character that is not allowed");
      }
      // @ switches the authority, \ is a separator to some parsers, ? and # end the path,
      // ; adds path parameters (servlet containers treat "..;" as "..")
      if (c == '@' || c == '\\' || c == '?' || c == '#' || c == ';') {
        throw new IllegalArgumentException("Messaging bridge httpPath has a character that is not allowed");
      }
      if (c == '%') {
        // must be a full %XX escape, and must not decode to a character that changes structure
        if (i + 2 >= httpPath.length() || hexValue(httpPath.charAt(i + 1)) < 0 || hexValue(httpPath.charAt(i + 2)) < 0) {
          throw new IllegalArgumentException("Messaging bridge httpPath has an invalid % escape");
        }
        int decoded = hexValue(httpPath.charAt(i + 1)) * 16 + hexValue(httpPath.charAt(i + 2));
        if (decoded < 0x20 || decoded == 0x7f || decoded == '/' || decoded == '\\' || decoded == '.'
            || decoded == '@' || decoded == '?' || decoded == '#' || decoded == '%' || decoded == ';') {
          throw new IllegalArgumentException("Messaging bridge httpPath has an encoded character that is not allowed");
        }
      }
    }
    // no . or .. segments, and the path must be a WS REST path
    boolean hasServicesRest = false;
    for (String segment : httpPath.substring(1).split("/", -1)) {
      if (".".equals(segment) || "..".equals(segment)) {
        throw new IllegalArgumentException("Messaging bridge httpPath must not contain dot segments");
      }
      if (SERVICES_REST_SEGMENT.equals(segment)) {
        hasServicesRest = true;
      }
    }
    if (!hasServicesRest) {
      throw new IllegalArgumentException("Messaging bridge httpPath must be a Grouper WS servicesRest path");
    }
  }

  /**
   * @param c
   * @return the hex digit value, or -1 if not a hex digit
   */
  private static int hexValue(char c) {
    if (c >= '0' && c <= '9') {
      return c - '0';
    }
    if (c >= 'a' && c <= 'f') {
      return c - 'a' + 10;
    }
    if (c >= 'A' && c <= 'F') {
      return c - 'A' + 10;
    }
    return -1;
  }
}
