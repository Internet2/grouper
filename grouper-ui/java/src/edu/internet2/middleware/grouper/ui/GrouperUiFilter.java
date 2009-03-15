/*
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.grouper.ui;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.subject.Subject;

/**
 * Generic filter for ui for grouper (e.g. set hooks context)
 * 
 * @author Chris Hyzer.
 * @version $Id: GrouperUiFilter.java,v 1.2 2009-03-15 06:37:51 mchyzer Exp $
 */

public class GrouperUiFilter implements Filter {

  /** logger */
  private static Log LOG = LogFactory.getLog(GrouperUiFilter.class);

	/**
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
	}

	/**
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

	  HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession();
			
    String remoteUser = request.getRemoteUser();
    if (remoteUser == null || remoteUser.length() == 0) {
      remoteUser = (String)(session == null ? null : session.getAttribute("authUser"));
    }

    HooksContext.clearThreadLocal();
    
    GrouperContextTypeBuiltIn.setDefaultContext(GrouperContextTypeBuiltIn.GROUPER_UI);

    Subject subject = null;

    GrouperSession grouperSession = SessionInitialiser.getGrouperSession(session);
    
    if (grouperSession != null) {
      subject = grouperSession.getSubject();
    }
    
    if (subject == null && !StringUtils.isBlank(remoteUser)) {
      try {
        subject = SubjectFinder.findByIdentifier(remoteUser, true);
      } catch (Exception e) {
        //this is not really ok, but cant do much about it
        LOG.error("Cant find login subject", e);
      }
    }
    
    HooksContext.assignSubjectLoggedIn(subject);
    
    //lets add the request, session, and response
    HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_REQUEST, request, false);
    HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SESSION, session, false);
    HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_RESPONSE, response, false);
    
    try {
      chain.doFilter(request, response);
    } finally {

      HooksContext.clearThreadLocal();
    }



	}

	/**
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {

	}
	
	

}