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


package edu.internet2.middleware.grouper.ui;



import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;

import edu.internet2.middleware.grouper.ui.*;
import edu.internet2.middleware.grouper.SubjectFactory;
import edu.internet2.middleware.subject.Source;

/**
 * Dummy, definitely not for production, Filter which looks for a request
 * parameter called 'username' and attempts to create a Subject and GrouperSession
 * based on it - no password needed!
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: EasyLoginFilter.java,v 1.1.1.1 2005-09-16 14:01:58 isgwb Exp $
 */

public class EasyLoginFilter implements Filter {
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}
	private String failureUrl = "/";
	private String ignore="";
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
	
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res,
		FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request  = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		HttpSession session = request.getSession();
	
		edu.internet2.middleware.subject.Subject subj = null;
		Source source=null;
		String remoteUser = request.getRemoteUser();
		if(remoteUser ==null || remoteUser.length()==0) remoteUser=request.getParameter("username");
		if(remoteUser!=null && remoteUser.length()!=0) {
			//we've got a name so try and use it
			try{
				subj = SubjectFactory.getSubjectByIdentifier(remoteUser);
				
				//This is what EasyLoginHttpServletRequest looks for
				session.setAttribute("easyAuthUser",remoteUser);
				
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
	  		
			}catch(Exception e) {
				//Couldn't derive a Subject from the username
				//so effectively log the user out
				e.getMessage();
				session.removeAttribute("easyAuthUser");
	
			}
			
		}
		//Ensure that request.getRemoteUser() returns 'username'
		//if easyAuthUser set
		chain.doFilter(new EasyLoginHttpServletRequest(request),res);
	}
}
