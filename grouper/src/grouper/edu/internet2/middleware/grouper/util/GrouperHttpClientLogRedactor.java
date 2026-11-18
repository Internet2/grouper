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
package edu.internet2.middleware.grouper.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * Masks credentials in the HTTP transcript that GrouperHttpClient writes when HTTP command
 * logging is on (provisioner logCommandsAlways / logCommandsOnError, which ends up in
 * grouper_loader_log.job_message) or when the GrouperHttpClient logger is at DEBUG.
 *
 * Why this is central and not per caller: every provisioner and external system that uses
 * GrouperHttpClient goes through the same logging block, and the per caller opt outs
 * (assignDoNotLogRequestBody / assignDoNotLogResponseBody) were missed by several token calls
 * (Dropbox, Box, Remedy, Remedy Digital Marketplace).  The opt outs still win (whole body masked);
 * this class masks the known credential shapes in everything else:
 *
 *  - JSON string pairs whose key is sensitive:      "password":"x"     -> "password":"*******"
 *  - form / query pairs whose name is sensitive:    client_secret=x    -> client_secret=*******
 *  - XML elements whose name is sensitive:          &lt;password&gt;x&lt;/password&gt;
 *  - JWT shaped values anywhere (bare Remedy login response, Box signed assertion): eyJ*******
 *  - Bearer / Basic / AR-JWT authorization values in text
 *
 * Only java.lang and java.util.regex are used so the same class works on every Grouper release.
 * Security issue 40 (provisionerCommandLoggingCredentials).
 * </pre>
 */
public class GrouperHttpClientLogRedactor {

  /**
   * what a masked value is replaced with (same mask GrouperHttpClient already uses)
   */
  public static final String MASK = "*******";

  /**
   * a name containing any of these (lower case, ignoring case of the name) is a credential
   */
  private static final String[] SENSITIVE_NAME_PARTS = new String[] {
    "pass", "secret", "token", "assertion", "apikey", "api_key", "api-key", "credential",
    "authorization", "privatekey", "private_key", "private-key", "jwt", "signature"};

  /**
   * names that contain a sensitive part but are not credentials, so diagnostics keep them
   */
  private static final String[] NOT_SENSITIVE_NAMES = new String[] {
    "token_type", "tokentype", "token-type", "grant_type", "granttype", "expires_in"};

  /**
   * exact names (lower case) that are credentials but have no sensitive part, e.g. the
   * OAuth authorization code
   */
  private static final String[] SENSITIVE_EXACT_NAMES = new String[] {"code", "pwd", "pin"};

  /**
   * header names are a superset: cookies and auth headers (X-Auth-Token, X-Api-Key, ...)
   */
  private static final String[] SENSITIVE_HEADER_PARTS = new String[] {"cookie", "auth", "key"};

  /**
   * "name" : "value" in JSON, value is a JSON string (escaped quotes handled).  The value is an
   * unrolled possessive loop, not (a|b)*, so a long value cannot overflow the regex stack
   */
  private static final Pattern JSON_PAIR = Pattern.compile(
      "(\"([^\"\\\\]{1,200})\"\\s*:\\s*\")([^\"\\\\]*+(?:\\\\.[^\"\\\\]*+)*+)(\")");

  /**
   * name=value in a form body or a query string; starts at the beginning, after ? &amp; or whitespace
   */
  private static final Pattern FORM_PAIR = Pattern.compile(
      "(^|[?&;\\s])([^=&?;\\s\"'<>]{1,200})=([^&;\\s\"'<>]*)");

  /**
   * &lt;name attr="x"&gt;value&lt;/name&gt; (also with a namespace prefix)
   */
  private static final Pattern XML_ELEMENT = Pattern.compile(
      "(<((?:[A-Za-z_][\\w.-]*:)?([A-Za-z_][\\w.-]{0,200}))(?:\\s[^<>]*)?>)([^<]*)(</\\2>)");

  /**
   * a JWT: base64url header starting with {" then payload then (maybe empty) signature
   */
  private static final Pattern JWT = Pattern.compile(
      "eyJ[A-Za-z0-9_-]{4,}\\.[A-Za-z0-9_-]{4,}\\.[A-Za-z0-9_-]*");

  /**
   * authorization scheme followed by its credential
   */
  private static final Pattern AUTH_SCHEME = Pattern.compile(
      "(?i)\\b(Bearer|Basic|AR-JWT|Token)(\\s+)[A-Za-z0-9._~+/=-]{8,}");

  /**
   * see if a parameter, JSON key, or XML element name holds a credential
   * @param name
   * @return true if the value of that name should not be logged
   */
  public static boolean isSensitiveName(String name) {
    if (name == null) {
      return false;
    }
    String lower = name.trim().toLowerCase();
    // url encoded or quoted names: compare on the plain text
    lower = lower.replace("%5f", "_").replace("%2d", "-");
    for (String notSensitive : NOT_SENSITIVE_NAMES) {
      if (lower.equals(notSensitive)) {
        return false;
      }
    }
    for (String exact : SENSITIVE_EXACT_NAMES) {
      if (lower.equals(exact)) {
        return true;
      }
    }
    for (String part : SENSITIVE_NAME_PARTS) {
      if (lower.contains(part)) {
        return true;
      }
    }
    return false;
  }

  /**
   * see if a request or response header holds a credential (Authorization, Cookie, Set-Cookie,
   * X-Api-Key, X-Auth-Token, ...)
   * @param headerName
   * @return true if the header value should not be logged
   */
  public static boolean isSensitiveHeaderName(String headerName) {
    if (headerName == null) {
      return false;
    }
    if (isSensitiveName(headerName)) {
      return true;
    }
    String lower = headerName.trim().toLowerCase();
    for (String part : SENSITIVE_HEADER_PARTS) {
      if (lower.contains(part)) {
        return true;
      }
    }
    return false;
  }

  /**
   * mask credentials in a URL (query string tokens, api keys, user:password@host)
   * @param url
   * @return the url safe to log
   */
  public static String redactUrl(String url) {
    if (url == null || url.length() == 0) {
      return url;
    }
    try {
      String result = url;
      // user info in the authority: https://user:password@host/ -> https://user:*******@host/
      result = result.replaceFirst("^([A-Za-z][A-Za-z0-9+.-]*://[^/@:?#]*):[^/@?#]*@", "$1:" + Matcher.quoteReplacement(MASK) + "@");
      return redactBody(result);
    } catch (RuntimeException re) {
      // never let logging break the call, and never log the raw value if redaction failed
      return MASK;
    }
  }

  /**
   * mask credentials in a request or response body (JSON, form, XML or plain text)
   * @param body
   * @return the body safe to log
   */
  public static String redactBody(String body) {
    if (body == null || body.length() == 0) {
      return body;
    }
    try {
      String result = body;
      result = redactPairs(result, JSON_PAIR, 2, 3);
      result = redactPairs(result, XML_ELEMENT, 3, 4);
      result = redactPairs(result, FORM_PAIR, 2, 3);
      // values that are credentials whatever they are called (or with no name at all)
      result = JWT.matcher(result).replaceAll("eyJ" + Matcher.quoteReplacement(MASK));
      result = AUTH_SCHEME.matcher(result).replaceAll("$1$2" + Matcher.quoteReplacement(MASK));
      return result;
    } catch (RuntimeException re) {
      // never let logging break the call, and never log a body that could not be checked
      return MASK;
    } catch (StackOverflowError soe) {
      // a pathological body that overflows the regex engine: same as above
      return MASK;
    }
  }

  /**
   * replace the value group of each match whose name group is a sensitive name
   * @param text
   * @param pattern
   * @param nameGroup
   * @param valueGroup
   * @return the text with sensitive values masked
   */
  private static String redactPairs(String text, Pattern pattern, int nameGroup, int valueGroup) {
    Matcher matcher = pattern.matcher(text);
    StringBuilder result = null;
    int last = 0;
    while (matcher.find()) {
      String value = matcher.group(valueGroup);
      if (value == null || value.length() == 0 || !isSensitiveName(matcher.group(nameGroup))) {
        continue;
      }
      if (result == null) {
        result = new StringBuilder(text.length());
      }
      // keep everything up to the value, mask the value, keep the rest of the match
      result.append(text, last, matcher.start(valueGroup)).append(MASK);
      last = matcher.end(valueGroup);
    }
    if (result == null) {
      return text;
    }
    result.append(text, last, text.length());
    return result.toString();
  }

}
