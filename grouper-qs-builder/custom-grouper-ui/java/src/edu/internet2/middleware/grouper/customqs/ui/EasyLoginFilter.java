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
/*
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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


package edu.internet2.middleware.grouper.customqs.ui;



import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.*;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.subject.Source;

/**
 * Dummy, definitely not for production, Filter which looks for a request
 * parameter called 'username' and attempts to create a Subject and GrouperSession
 * based on it - no password needed!
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: EasyLoginFilter.java,v 1.2 2009-11-10 16:41:04 isgwb Exp $
 */

public class EasyLoginFilter implements Filter {
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}
	private String failureUrl = "/";
	private String[] ignore;
	/**
	 * 
	 */
	public EasyLoginFilter() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		ignore=config.getInitParameter("ignore").split(":");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res,
		FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request  = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		HttpSession session = request.getSession();
		String url=	request.getRequestURI();
		boolean ignoreThisRequest=false;
		String reqUsername = request.getParameter("username");
		String authUser = (String)session.getAttribute("authUser");
		if((reqUsername==null || "".equals(reqUsername)) && authUser !=null) {
		}else{
			if(reqUsername==null || "".equals(reqUsername)) {
				for(String toIgnore : ignore) {
					if(!"".equals(toIgnore) && url.endsWith(toIgnore)) {
						ignoreThisRequest=true;
						break;
					}
				}
			}
			
			if(!ignoreThisRequest) {
				edu.internet2.middleware.subject.Subject subj = null;
				Source source=null;
				String remoteUser = request.getRemoteUser();
				if(remoteUser ==null || remoteUser.length()==0) remoteUser=request.getParameter("username");
				if(remoteUser!=null && remoteUser.length()!=0) {
					//we've got a name so try and use it
					try{
						subj = SubjectFinder.findByIdentifier(remoteUser,true);
						
						//This is what EasyLoginHttpServletRequest looks for
						session.setAttribute("easyAuthUser",remoteUser);
						request.setAttribute("forceNewLogin",Boolean.TRUE);
						//We want to initialise a new session later
						session.removeAttribute("sessionInited");
						Cookie[] cookies = request.getCookies();
						
			  			if(cookies !=null) {
				  			for(int i=0;i<cookies.length;i++) {
				  				//we've logged in - but still need to do some initialisation
				  				// and removal of Cookie later on
				  				if(cookies[i].getName().equals("_grouper_loggedOut")) cookies[i].setValue("pending");
				  			}
				  		}
			  			if(remoteUser != null && url.endsWith("/callLogin.do")) {
			  				request.setAttribute("forceRedirect", "/home.do");
							
			  			}
					}catch(Exception e) {
						//Couldn't derive a Subject from the username
						//so effectively log the user out
						e.getMessage();
						session.removeAttribute("easyAuthUser");
						remoteUser=null;
			
					}
					
				}
				if(remoteUser==null) {
					try {
						SessionInitialiser.init(request);
					} catch (Exception e) {
						throw new ServletException(e);
					}
					SessionInitialiser.initThread(session);
					request.getRequestDispatcher("/callLogin.do").forward(request, response);
					return;
				}
			}
		}
		//Ensure that request.getRemoteUser() returns 'username'
		//if easyAuthUser set
		chain.doFilter(new EasyLoginHttpServletRequest(request),res);
	}
}
