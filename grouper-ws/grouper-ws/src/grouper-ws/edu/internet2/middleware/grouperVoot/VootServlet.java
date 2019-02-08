/*******************************************************************************
 * Copyright 2012 Internet2
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

package edu.internet2.middleware.grouperVoot;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.util.JsonIndenter;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouperVoot.messages.VootErrorResponse;
import edu.internet2.middleware.grouperVoot.restLogic.VootWsRest;
import edu.internet2.middleware.subject.Subject;

/**
 * VOOT servlet for the voot rest protocol.
 * 
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 */
@SuppressWarnings("serial")
public class VootServlet extends HttpServlet {

  /** logger facility for this class. */
  private static final Log LOG = LogFactory.getLog(VootServlet.class);

  /**
   * Method called when GET method received on the servlet.
   * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    GrouperSession grouperSession = null;
    try {
      GrouperServiceJ2ee.assignHttpServlet(this);

      // get the logged in user
      Subject loggedInSubject = GrouperServiceJ2ee.retrieveSubjectLoggedIn();
      if (loggedInSubject == null) {
        throw new NullPointerException("No logged in user!");
      }
      grouperSession = GrouperSession.start(loggedInSubject);

      List<String> urlStrings = extractUrlStrings(request);

      // url should be: /groups/aStem:aGroup
      String resource = GrouperServiceUtils.popUrlString(urlStrings);
      VootRestHttpMethod vootRestHttpMethod = VootRestHttpMethod.valueOfIgnoreCase(request.getMethod(), true);

      // validate and get the operation
      VootWsRest vootWsRest = VootWsRest.valueOfIgnoreCase(resource, false);

      @SuppressWarnings("unchecked")
      Object resultObject = vootWsRest.service(urlStrings, vootRestHttpMethod,
          (Map<String, String[]>) request.getParameterMap());

      String json = GrouperUtil.jsonConvertToNoWrap(resultObject);

      if (GrouperUtil.booleanValue(request.getParameter("indentResponse"), false)) {
        json = new JsonIndenter(json).result();
      }

      if (resultObject instanceof VootErrorResponse) {
        response.setStatus(500);
      } else {
        response.setStatus(200);
      }

      response.setContentType("application/json");

      Writer writer = null;

      try {
        writer = response.getWriter();
        writer.write(json);
      } catch (Exception e) {
        LOG.error("error", e);
      } finally {
        GrouperUtil.closeQuietly(writer);
      }
    } catch (RuntimeException re) {
      response.setStatus(500);

      LOG.error("Error in voot", re);
      throw new RuntimeException("Error in voot", re);

    } finally {
      try {
        GrouperSession.stopQuietly(grouperSession);

        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
          httpSession.invalidate();
        }
      } catch (Exception e) {
        LOG.error("Error", e);
      }
    }
  }

  /**
   * Take a request and get the list of url strings for the rest web service.
   * @see #extractUrlStrings(String)
   * 
   * @param request is the request to get the url strings out of.
   * @return the list of url strings.
   */
  private static List<String> extractUrlStrings(HttpServletRequest request) {
    String requestResourceFull = request.getRequestURI();
    return extractUrlStrings(requestResourceFull);
  }

  /**
   * Take a request uri and break up the url strings not including the app name or servlet
   * this does not include the url params (if applicable).
   * If the input is: grouper-ws/servicesRest/xhtml/v1_3_000/groups/members
   * then the result is a list of size 2: {"group", "members"}.
   * 
   * @param requestResourceFull the request string.
   * @return the parsed URL strings.
   */
  private static List<String> extractUrlStrings(String requestResourceFull) {
    String[] requestResources = StringUtils.split(requestResourceFull, '/');
    List<String> urlStrings = new ArrayList<String>();
    
    // loop through and decode
    int index = 0;
    for (String requestResource : requestResources) {
      // skip the app name and lite servlet
      if (index++ < 2) {
        continue;
      }
      // unescape the url encoding
      urlStrings.add(GrouperUtil.escapeUrlDecode(requestResource));
    }
    return urlStrings;
  }
}
