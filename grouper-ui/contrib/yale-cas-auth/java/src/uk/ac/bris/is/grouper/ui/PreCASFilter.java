/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Bristol
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Bristol nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Bristol, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Bristol, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * @version $Id: PreCASFilter.java,v 1.1.1.1 2005-08-23 13:03:13 isgwb Exp $
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
