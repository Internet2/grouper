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

package uk.ac.bris.is.grouper.ui;

import java.io.IOException;

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
import edu.yale.its.tp.cas.client.filter.CASFilter;

/**
 * Integrates CAS with Grouper ui
 * @author Gary Brown.
 * @version $Id: PreCASFilter.java,v 1.3 2009-11-08 12:27:20 isgwb Exp $
 */
public class PreCASFilter implements Filter {
	private String failureUrl = "/";
	private String ignore="";

	
	public PreCASFilter() {
		super();
	
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		
	}



	/**
	 *  Designed so that container authentication can co-exist with CAS - so if RemoteUser already set
	 *  make CAS think that user is authenticated. Also, if Cookie exists saying that
	 *  user has logged out from Grouper, then force re-login through CAS
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request  = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		HttpSession session = request.getSession();
		CASRenewOnLogoutResponseWrapper renewRespnse= new CASRenewOnLogoutResponseWrapper(response);
		Cookie[] cookies = request.getCookies();
		String ticket = request.getParameter("ticket");
		boolean loggedOut=false;
		String remoteUser = request.getRemoteUser();
	  	if(cookies !=null) {
		  	for(int i=0;i<cookies.length;i++) {
		  		
		  		//Check we have the cookie we are interested in
		  		if("_grouper_loggedOut".equals(cookies[i].getName())) { 
		  			
		  			//
		  			if((remoteUser !=null && "re-enter".equals(cookies[i].getValue()) //Force re-login
		  					||(ticket !=null && ticket.length()!=0))) { //We have a ticket so defer to CAS
		  				cookies[i].setValue("pending");
		  			}
		  			//Read by CASRenewOnLogoutResponseWrapper
		  			response.setHeader("_grouper_loggedOut","true");
		  			break;	
		  		}
		  	}
	  	}
	  	
		String authUser = (String) session.getAttribute("authUser");
		if(authUser==null) {
			authUser=(String)session.getAttribute(CASFilter.CAS_FILTER_USER);
		}
		
		//Convince CAS to work properly with populateIndex.do
		if (!request.getServletPath().endsWith("/populateIndex.do") && session.getAttribute("edu.yale.its.tp.cas.client.filter.didGateway")!=null) {
			session.removeAttribute("edu.yale.its.tp.cas.client.filter.didGateway");
		}
		if(authUser==null) {
			if(remoteUser !=null) {
				//defer to container
				session.setAttribute(CASFilter.CAS_FILTER_USER,remoteUser);
			}else{
				//Our session may have been killed - so make sure CAS does login check
				session.removeAttribute(CASFilter.CAS_FILTER_USER);
			}
			chain.doFilter(req,renewRespnse);
		}else {
			chain.doFilter(req,res);
		}
		
		
	
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
