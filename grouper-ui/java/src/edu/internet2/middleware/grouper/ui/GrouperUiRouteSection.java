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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter.UiSection;

/**
 * <pre>
 * Decide which UI section (and so which login group, require.group.for.logins) applies to a
 * request, from the same URI path that GrouperUiRestServlet dispatches.
 *
 * Why this class exists (security fix): GrouperUiFilter used to return ANONYMOUS for every
 * request on the alternate servlet mapping /grouperExternal/app/*, before it reached the code
 * that limits that mapping to UiV2ExternalSubjectSelfRegister, and it classified
 * /grouperUi/app/* requests from the caller supplied "operation" parameter
 * (operation=Misc.anything was ANONYMOUS).  The servlet ignores that parameter and runs the
 * class and method in the path, so both routes skipped the configured UI login group.
 *
 * Rules, in order:
 * - the welcome page, index.jsp and the UiV2Public entry points: ANONYMOUS (unchanged)
 * - /grouperExternal/app/UiV2ExternalSubjectSelfRegister.*: EXTERNAL
 * - anything else on /grouperExternal/app/: error, nothing else may run on that mapping
 * - everything else (normal UI handlers, JSPs, appHtml): ADMIN_UI
 * The operation parameter is never used.
 *
 * Plain Java only (no commons-lang), so the same file works in Grouper 5, 6 and 7.
 * </pre>
 */
public class GrouperUiRouteSection {

  /** the only handler class the external servlet mapping may run */
  public static final String EXTERNAL_SELF_REGISTER_CLASS = "UiV2ExternalSubjectSelfRegister";

  /**
   * no instances
   */
  private GrouperUiRouteSection() {
  }

  /**
   * get the ui section for a request
   * @param httpServletRequest
   * @return the section, never null
   * @throws RuntimeException if a non external handler is requested on the external servlet
   */
  public static UiSection uiSectionForRequest(HttpServletRequest httpServletRequest) {
    return uiSectionForRequestUri(httpServletRequest.getRequestURI());
  }

  /**
   * get the ui section for a raw request uri (as from HttpServletRequest.getRequestURI(),
   * not decoded, no query string)
   * @param uri
   * @return the section, never null
   * @throws RuntimeException if a non external handler is requested on the external servlet
   */
  public static UiSection uiSectionForRequestUri(String uri) {

    if (uri == null) {
      // nothing to go on, take the strictest section that still lets the UI work
      return UiSection.ADMIN_UI;
    }

    // exact anonymous entry points, same literal patterns as before the fix.  An encoded
    // spelling of these does not match and falls through to ADMIN_UI, which is stricter
    if (uri.matches("^/[^/]+/$")
        || uri.matches("^/[^/]+/index\\.jsp$")
        || uri.matches("^/[^/]+/grouperExternal/public/UiV2Public\\.index$")
        || uri.matches("^/[^/]+/grouperExternal/public/UiV2Public\\.postIndex$")
        || uri.matches("^/[^/]+/grouperUi/app/UiV2Public\\.postIndex$")) {
      return UiSection.ANONYMOUS;
    }

    // split like GrouperUiRestServlet.extractUrlStrings: non-empty segments, URL decoded.
    // segment 0 is the webapp context, 1 the servlet (grouperUi or grouperExternal),
    // 2 "app" or "public", 3 the Class.method that the servlet runs
    List<String> segments = decodedSegments(uri);

    boolean externalServlet = segments.size() >= 3
        && "grouperExternal".equals(segments.get(1))
        && "app".equals(segments.get(2));

    if (externalServlet) {
      // the external mapping may only run the external subject self register screens.
      // Decide from the path segment the servlet dispatches, exactly one segment after app
      if (segments.size() == 4
          && EXTERNAL_SELF_REGISTER_CLASS.equals(handlerClass(segments.get(3)))) {
        return UiSection.EXTERNAL;
      }
      throw new RuntimeException("Cannot use the external servlet for non external operations! '" + uri + "'");
    }

    // everything else needs the normal UI login group, whatever the operation parameter says
    return UiSection.ADMIN_UI;
  }

  /**
   * class part of a Class.method path segment, the way the servlet reads it: drop anything
   * after a #, then take the part before the first dot
   * @param classAndMethod
   * @return the class name or null if there is no dot
   */
  static String handlerClass(String classAndMethod) {
    if (classAndMethod == null) {
      return null;
    }
    String result = classAndMethod;
    int hashIndex = result.indexOf('#');
    if (hashIndex >= 0) {
      result = result.substring(0, hashIndex);
    }
    int dotIndex = result.indexOf('.');
    if (dotIndex < 0) {
      return null;
    }
    return result.substring(0, dotIndex);
  }

  /**
   * split a raw uri into non-empty path segments and URL decode each one, like
   * GrouperUiRestServlet.extractUrlStrings (StringUtils.split drops empty segments).
   * A segment that cannot be decoded is kept as is (it will not match any allowed name)
   * @param uri
   * @return the segments
   */
  static List<String> decodedSegments(String uri) {
    List<String> result = new ArrayList<String>();
    for (String segment : uri.split("/")) {
      if (segment.length() == 0) {
        continue;
      }
      result.add(urlDecode(segment));
    }
    return result;
  }

  /**
   * URL decode once (UTF-8) like GrouperUtil.escapeUrlDecode; on a malformed escape return the input
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

}
