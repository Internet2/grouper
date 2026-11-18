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
package edu.internet2.middleware.grouper.j2ee.status;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>Decides who gets the details of the /status page (hostname, version, uptime, the name of
 * every diagnostic task such as loader jobs named after their groups, failure text and stack
 * traces), and escapes that text for the HTML response.</p>
 *
 * <p>/status itself stays open to whoever ws.diagnostic.sourceIpAddresses allows (everyone when
 * it is blank), so load balancer health checks keep working: everyone gets the HTTP code and the
 * SUCCESS or error heading.  The details are only sent to callers whose address is on
 * ws.diagnostic.sourceIpAddresses.  When that list is blank, nobody gets the details in the
 * response (they are still logged), unless ws.diagnostic.sendDetailsOutsideSourceIpAddresses is
 * true, which restores the behavior before this fix.  ws.diagnostic.sendDetailsInResponse
 * (and ws.diagnostic.sendHttpCodeOnly where it exists) still apply on top.</p>
 *
 * <p>A separate class so the same code ships unchanged in every release; GrouperStatusServlet
 * differs from release to release and only gets small calls into this class.</p>
 */
public class GrouperStatusServletDetails {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperStatusServletDetails.class);

  /** grouper.properties key: networks allowed to call /status (blank: everyone) */
  public static final String CONFIG_SOURCE_IP_ADDRESSES = "ws.diagnostic.sourceIpAddresses";

  /** grouper.properties key: if true, callers not on the allowed networks also get details (default false) */
  public static final String CONFIG_SEND_DETAILS_OUTSIDE_SOURCE_IP_ADDRESSES = "ws.diagnostic.sendDetailsOutsideSourceIpAddresses";

  /**
   * if this caller may see the status details
   * @param request
   * @return true if the caller's address is on ws.diagnostic.sourceIpAddresses, or the site
   * turned on ws.diagnostic.sendDetailsOutsideSourceIpAddresses
   */
  public static boolean detailsAllowedForCaller(HttpServletRequest request) {

    GrouperConfig config = GrouperConfig.retrieveConfig();

    // site chose the old behavior: details to everyone who may call /status
    if (config.propertyValueBoolean(CONFIG_SEND_DETAILS_OUTSIDE_SOURCE_IP_ADDRESSES, false)) {
      return true;
    }

    // no allowed networks configured: nobody is "inside", so no details in the response
    String networks = config.propertyValueString(CONFIG_SOURCE_IP_ADDRESSES);
    if (networks == null || networks.trim().length() == 0) {
      return false;
    }

    String sourceIp = request == null ? null : request.getRemoteAddr();
    if (sourceIp == null || sourceIp.trim().length() == 0) {
      return false;
    }

    // fail closed if the address cannot be parsed against the networks
    try {
      return GrouperUtil.ipOnNetworks(sourceIp, networks);
    } catch (RuntimeException re) {
      LOG.warn("Cannot check status caller " + sourceIp + " against " + CONFIG_SOURCE_IP_ADDRESSES, re);
      return false;
    }
  }

  /**
   * escape text for HTML element content (and attributes), so names, failure text and request
   * values are shown as text
   * @param text
   * @return the escaped text, or empty string if null
   */
  public static String escapeHtml(String text) {
    if (text == null) {
      return "";
    }
    StringBuilder result = new StringBuilder(text.length() + 16);
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch (c) {
        case '&':
          result.append("&amp;");
          break;
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        case '"':
          result.append("&quot;");
          break;
        case '\'':
          result.append("&#39;");
          break;
        default:
          result.append(c);
      }
    }
    return result.toString();
  }
}
