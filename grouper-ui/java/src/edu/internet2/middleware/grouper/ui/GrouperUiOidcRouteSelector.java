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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * Pick the OIDC external system that protects a UI request (grouper.oidcExternalSystem.*
 * with useForUi=true), in place of GrouperOidc.externalSystemConfigIdForUi(String).
 *
 * Why this class exists (security fix): the route regexes (uiPathRegexes) were matched against
 * one raw string, request uri + "?" + raw query string, while the UI handler reads the decoded
 * path and decoded parameters from the query string or the POST body.  An extra parameter
 * first, a percent-encoded character, the parameter in the POST body, an encoded path, the
 * alternate /grouperExternal/app/ mapping, or the text ".oidc?" anywhere in the query (which
 * made the request look like the OIDC callback and skipped every regex and the default
 * config) all made the regex miss, and the filter then fell back to a lower assurance login.
 *
 * What this does:
 * - the OIDC callback is recognized only when the request path is exactly the path of a
 *   config's redirectUri, never by a substring of the query
 * - otherwise each config's regexes are matched against several spellings of the request:
 *   the raw string as before (so existing configs keep matching), the decoded path, the
 *   /grouperExternal/app/ path as /grouperUi/app/, the path alone, the query rebuilt from the
 *   decoded parameters (query and form body, decoded and re-encoded), and each single
 *   name=value parameter.  A config is picked if any regex matches any spelling.  Matching
 *   more spellings can only require OIDC more often, never less
 * - no route config matches: the default config (no regexes), else null, as before
 * - two configs at the same level match: error, as before
 *
 * Config is read on every call (like GrouperOidc in 5.20 and later), with only APIs that exist
 * in every release from 5.17.2, and no commons-lang, so the same file works in 5, 6 and 7.
 * </pre>
 */
public class GrouperUiOidcRouteSelector {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUiOidcRouteSelector.class);

  /** finds the oidc external system config ids */
  private static final Pattern CONFIG_ID_PATTERN = Pattern.compile("^grouper\\.oidcExternalSystem\\.(.*)\\.clientId$");

  /**
   * everything after the host of the redirect uri, same pattern as GrouperOidcConfig,
   * e.g. https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.oidc -> /grouper/grouperUi/app/UiV2Main.oidc
   */
  private static final Pattern REDIRECT_CONTEXT_PATTERN = Pattern.compile("^http[^\\/]+\\/\\/[^\\/]+(\\/.*)$");

  /**
   * no instances
   */
  private GrouperUiOidcRouteSelector() {
  }

  /**
   * which oidc external system config id applies to this UI request
   * @param httpServletRequest
   * @return the config id or null if no UI OIDC applies
   */
  @SuppressWarnings("unchecked")
  public static String externalSystemConfigIdForUi(HttpServletRequest httpServletRequest) {
    // the decoded query and form body parameters the handler will see.  Built from getParameterNames and
    // getParameterValues, not getParameterMap: GrouperRequestWrapper adds multipart form fields through
    // those methods only, so a multipart request could otherwise move a routed parameter out of sight
    // (2026-11-10).  If parsing the request fails, the exception propagates and no handler runs.
    Map<String, String[]> parameterMap = new LinkedHashMap<String, String[]>();
    Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
    while (parameterNames != null && parameterNames.hasMoreElements()) {
      String parameterName = parameterNames.nextElement();
      parameterMap.put(parameterName, httpServletRequest.getParameterValues(parameterName));
    }
    return externalSystemConfigIdForUi(httpServletRequest.getRequestURI(),
        httpServletRequest.getQueryString(), parameterMap);
  }

  /**
   * which oidc external system config id applies to this UI request
   * @param rawPath request uri, not decoded, without the query
   * @param rawQuery raw query string or null
   * @param parameterMap decoded parameters (query and body) or null
   * @return the config id or null if no UI OIDC applies
   */
  public static String externalSystemConfigIdForUi(String rawPath, String rawQuery, Map<String, String[]> parameterMap) {

    String path = rawPath == null ? "" : rawPath;
    String decodedPath = decodePath(path);

    // enabled UI configs, sorted so the result does not depend on hash order
    List<String> uiConfigIds = new ArrayList<String>();
    Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(CONFIG_ID_PATTERN);
    for (String configId : new TreeSet<String>(GrouperUtil.nonNull(configIds))) {
      boolean enabled = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oidcExternalSystem."
          + configId + ".enabled", true);
      boolean useForUi = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oidcExternalSystem."
          + configId + ".useForUi", false);
      if (enabled && useForUi) {
        uiConfigIds.add(configId);
      }
    }

    // the OIDC callback: the request path is exactly a config's redirect uri path
    String callbackConfigId = null;
    for (String configId : uiConfigIds) {
      String context = redirectUriContext(GrouperConfig.retrieveConfig().propertyValueString(
          "grouper.oidcExternalSystem." + configId + ".redirectUri"));
      if (context != null && (context.equals(path) || context.equals(decodedPath))) {
        GrouperUtil.assertion(callbackConfigId == null, "Multiple OIDC external systems cannot be enabled for UI at the same time: "
            + callbackConfigId + " ," + configId);
        callbackConfigId = configId;
      }
    }
    if (callbackConfigId != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("oidc callback path: '" + path + "', externalSystemConfigIdForUi: " + callbackConfigId);
      }
      return callbackConfigId;
    }

    List<String> candidates = null;
    String routeConfigId = null;
    String defaultConfigId = null;

    for (String configId : uiConfigIds) {
      List<String> uiPathRegexes = uiPathRegexes(configId);
      if (uiPathRegexes.size() == 0) {
        GrouperUtil.assertion(defaultConfigId == null, "Multiple OIDC external systems cannot be enabled for UI at the same time: "
            + defaultConfigId + " ," + configId);
        defaultConfigId = configId;
        continue;
      }
      if (candidates == null) {
        candidates = candidates(path, decodedPath, rawQuery, parameterMap);
      }
      if (anyMatch(uiPathRegexes, candidates)) {
        GrouperUtil.assertion(routeConfigId == null, "Multiple OIDC external systems cannot be enabled for UI at the same time: "
            + routeConfigId + " ," + configId);
        routeConfigId = configId;
      }
    }

    String result = routeConfigId != null ? routeConfigId : defaultConfigId;
    if (LOG.isDebugEnabled()) {
      LOG.debug("path: '" + path + "', externalSystemConfigIdForUi: " + result);
    }
    return result;
  }

  /**
   * @param regexes
   * @param candidates
   * @return true if any regex matches any candidate
   */
  private static boolean anyMatch(List<String> regexes, List<String> candidates) {
    for (String regex : regexes) {
      Pattern pattern = Pattern.compile(regex);
      for (String candidate : candidates) {
        if (pattern.matcher(candidate).matches()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * the uiPathRegexes of a config, parsed like GrouperOidcConfig: comma separated, trimmed,
   * &amp;#x2c; is a comma
   * @param configId
   * @return the regexes, empty if none
   */
  static List<String> uiPathRegexes(String configId) {
    List<String> result = new ArrayList<String>();
    String uiPathRegexesString = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem."
        + configId + ".uiPathRegexes");
    if (uiPathRegexesString == null || uiPathRegexesString.trim().length() == 0) {
      return result;
    }
    for (String uiPathRegex : GrouperUtil.splitTrimToList(uiPathRegexesString, ",")) {
      result.add(uiPathRegex.replace("&#x2c;", ","));
    }
    return result;
  }

  /**
   * path of the redirect uri, like GrouperOidcConfig.getRedirectUriContext
   * @param redirectUri
   * @return the path or null if blank or not a url
   */
  static String redirectUriContext(String redirectUri) {
    if (redirectUri == null || redirectUri.trim().length() == 0) {
      return null;
    }
    Matcher matcher = REDIRECT_CONTEXT_PATTERN.matcher(redirectUri.trim());
    return matcher.matches() ? matcher.group(1) : null;
  }

  /**
   * every spelling of the request the regexes are matched against
   * @param path raw path
   * @param decodedPath path decoded like the servlet does
   * @param rawQuery
   * @param parameterMap
   * @return the candidate strings
   */
  static List<String> candidates(String path, String decodedPath, String rawQuery, Map<String, String[]> parameterMap) {

    // paths: raw, decoded, and the external servlet alias as the normal servlet (it
    // dispatches the same UiV2 handlers)
    Set<String> paths = new LinkedHashSet<String>();
    paths.add(path);
    paths.add(decodedPath);
    paths.add(normalServletAlias(path));
    paths.add(normalServletAlias(decodedPath));

    // queries: raw, rebuilt from the decoded parameters, and each single parameter
    Set<String> queries = new LinkedHashSet<String>();
    if (rawQuery != null && rawQuery.length() > 0) {
      queries.add(rawQuery);
    }
    StringBuilder rebuiltDecoded = new StringBuilder();
    StringBuilder rebuiltEncoded = new StringBuilder();
    for (Map.Entry<String, String[]> entry : GrouperUtil.nonNull(parameterMap).entrySet()) {
      String name = entry.getKey();
      String[] values = entry.getValue();
      if (name == null || values == null) {
        continue;
      }
      for (String value : values) {
        String decodedPair = name + "=" + (value == null ? "" : value);
        String encodedPair = urlEncode(name) + "=" + urlEncode(value == null ? "" : value);
        queries.add(decodedPair);
        queries.add(encodedPair);
        rebuiltDecoded.append(rebuiltDecoded.length() == 0 ? "" : "&").append(decodedPair);
        rebuiltEncoded.append(rebuiltEncoded.length() == 0 ? "" : "&").append(encodedPair);
      }
    }
    if (rebuiltDecoded.length() > 0) {
      queries.add(rebuiltDecoded.toString());
      queries.add(rebuiltEncoded.toString());
    }

    List<String> result = new ArrayList<String>();
    for (String onePath : paths) {
      // the path alone too, so an anchored regex is not escaped by adding a junk parameter
      result.add(onePath);
      for (String query : queries) {
        result.add(onePath + "?" + query);
      }
    }
    return result;
  }

  /**
   * /ctx/grouperExternal/app/X -> /ctx/grouperUi/app/X
   * @param path
   * @return the path with the normal servlet, or the input if not the external servlet
   */
  static String normalServletAlias(String path) {
    int index = path.indexOf("/grouperExternal/app/");
    if (index < 0) {
      return path;
    }
    return path.substring(0, index) + "/grouperUi/app/" + path.substring(index + "/grouperExternal/app/".length());
  }

  /**
   * decode each path segment once, like GrouperUiRestServlet.extractUrlStrings
   * @param path
   * @return the decoded path
   */
  static String decodePath(String path) {
    String[] segments = path.split("/", -1);
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < segments.length; i++) {
      if (i > 0) {
        result.append('/');
      }
      result.append(urlDecode(segments[i]));
    }
    return result.toString();
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
   * URL encode (UTF-8)
   * @param string
   * @return the encoded string
   */
  static String urlEncode(String string) {
    try {
      return URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("UTF-8 not supported", uee);
    }
  }

}
