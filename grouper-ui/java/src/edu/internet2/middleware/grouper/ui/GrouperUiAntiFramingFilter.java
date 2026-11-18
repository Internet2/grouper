/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.ui;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>Sends anti-framing (clickjacking) headers on Grouper UI responses:
 * <code>Content-Security-Policy: frame-ancestors 'self'</code> and
 * <code>X-Frame-Options: SAMEORIGIN</code>.  Without them, a page on another site can load a
 * UI screen in an invisible frame and trick a logged-in user into clicking a real button
 * (delete a group, save a pre-filled config form).</p>
 *
 * <p>Config in grouper-ui.properties:</p>
 * <ul>
 *   <li><code>grouperUi.antiFraming.frameAncestors</code>: the sources of the CSP frame-ancestors
 *   directive, default <code>'self'</code>.  A site that embeds the UI in a portal lists the portal
 *   origin too, e.g. <code>'self' https://portal.example.edu</code>.  X-Frame-Options cannot
 *   express a list, so it is sent (SAMEORIGIN) only for <code>'self'</code>, or DENY for
 *   <code>'none'</code>; browsers that know CSP use frame-ancestors either way.</li>
 *   <li><code>grouperUi.antiFraming.enabled</code>: default true; false sends neither header
 *   (e.g. when a proxy in front of Grouper already sets them).</li>
 * </ul>
 *
 * <p>Registration: CommonServletContainerInitializer adds this filter (name
 * {@link #FILTER_NAME}, url patterns {@link #URL_PATTERNS}, REQUEST dispatcher) in its UI block,
 * before GrouperUiFilter, the same way it registers the other UI filters.  It is deliberately not
 * a @WebFilter: an annotation is only seen where the container scans WEB-INF/classes (not, for
 * example, when the classes come from the Eclipse/Tomcat classpath, or with a 2.4 or
 * metadata-complete web.xml), and registering it both ways would clash on the filter name.</p>
 *
 * <p>The headers are set before the rest of the chain runs, so they are on every UI response,
 * including the login page, redirects and error pages.</p>
 */
public class GrouperUiAntiFramingFilter implements Filter {

  /** filter name CommonServletContainerInitializer registers this filter under */
  public static final String FILTER_NAME = "GrouperUiAntiFraming";

  /** url patterns CommonServletContainerInitializer maps this filter to: every UI response */
  public static final String[] URL_PATTERNS = new String[] {"*.jsp", "/grouperUi/*", "/grouperExternal/*"};

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUiAntiFramingFilter.class);

  /** grouper-ui.properties key: false to send no anti-framing headers (default true) */
  public static final String CONFIG_ENABLED = "grouperUi.antiFraming.enabled";

  /** grouper-ui.properties key: the CSP frame-ancestors sources (default 'self') */
  public static final String CONFIG_FRAME_ANCESTORS = "grouperUi.antiFraming.frameAncestors";

  /** only Grouper itself may frame the UI */
  public static final String DEFAULT_FRAME_ANCESTORS = "'self'";

  /** CSP header name */
  public static final String HEADER_CSP = "Content-Security-Policy";

  /** X-Frame-Options header name */
  public static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // config is read per request (it can change at runtime, e.g. from the database)
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (response instanceof HttpServletResponse) {
      addAntiFramingHeaders((HttpServletResponse)response);
    }
    chain.doFilter(request, response);
  }

  /**
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    // nothing to clean up
  }

  /**
   * add the anti-framing headers to a response, according to the config
   * @param response
   */
  public static void addAntiFramingHeaders(HttpServletResponse response) {

    // ---- read the config; if it cannot be read, fail closed with the defaults
    boolean enabled = true;
    String frameAncestors = DEFAULT_FRAME_ANCESTORS;
    try {
      GrouperUiConfigInApi config = GrouperUiConfigInApi.retrieveConfig();
      enabled = config.propertyValueBoolean(CONFIG_ENABLED, true);
      String configured = config.propertyValueString(CONFIG_FRAME_ANCESTORS);
      if (configured != null && configured.trim().length() > 0) {
        frameAncestors = configured;
      }
    } catch (RuntimeException re) {
      LOG.error("Cannot read " + CONFIG_ENABLED + " / " + CONFIG_FRAME_ANCESTORS
          + ", sending the default anti-framing headers", re);
    }

    if (!enabled) {
      return;
    }

    frameAncestors = cleanFrameAncestors(frameAncestors);

    // ---- Content-Security-Policy: frame-ancestors
    // if something earlier in the chain (e.g. a site-registered ContentSecurityPolicyFilter) already
    // set a CSP, add a second header instead of replacing it: browsers enforce every CSP header
    String csp = "frame-ancestors " + frameAncestors;
    if (response.containsHeader(HEADER_CSP)) {
      response.addHeader(HEADER_CSP, csp);
    } else {
      response.setHeader(HEADER_CSP, csp);
    }

    // ---- X-Frame-Options, for browsers that do not know frame-ancestors
    // it has no way to list other origins, so it is only sent when it says the same thing as the CSP
    String xFrameOptions = null;
    if ("'self'".equalsIgnoreCase(frameAncestors)) {
      xFrameOptions = "SAMEORIGIN";
    } else if ("'none'".equalsIgnoreCase(frameAncestors)) {
      xFrameOptions = "DENY";
    }
    if (xFrameOptions != null && !response.containsHeader(HEADER_X_FRAME_OPTIONS)) {
      response.setHeader(HEADER_X_FRAME_OPTIONS, xFrameOptions);
    }
  }

  /**
   * keep the configured value to the one frame-ancestors directive: a ";" would start another
   * directive and a "," another policy, and CR/LF do not belong in a header
   * @param frameAncestors configured value
   * @return the source list, or 'self' if nothing is left
   */
  static String cleanFrameAncestors(String frameAncestors) {
    String result = frameAncestors == null ? "" : frameAncestors.replaceAll("[;,\\r\\n\\t]", " ");
    result = result.replaceAll(" +", " ").trim();
    return result.length() == 0 ? DEFAULT_FRAME_ANCESTORS : result;
  }
}
