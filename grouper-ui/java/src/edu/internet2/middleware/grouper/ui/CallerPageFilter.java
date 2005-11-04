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

import java.io.IOException;
import java.net.URLEncoder;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import edu.internet2.middleware.grouper.ui.actions.GrouperCapableAction;


/**
 * Given a request parameter `pageId`, uses methods in GrouperCapableAction to 
 * reconstruct a query string and saved session attributes in order to redirect
 * the user back to a previous page which was previously `saved`.
 * 
 * TODO Need to look at passing messages - only want to apear once
 *  * <p />
 * 
 * @author Gary Brown.
 * @version $Id: CallerPageFilter.java,v 1.1 2005-11-04 11:24:59 isgwb Exp $
 */

public class CallerPageFilter implements Filter {
	

	/**
	 * Constructor
	 */
	public CallerPageFilter() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String id = request.getParameter("pageId");
		Map[] data = GrouperCapableAction.getCallerPageData(request,id);
		String url = (String)data[0].get("_callerPagePath");		
		//if(id.equals(data[0].get("callerPageId"))) data[0].remove("callerPageId");
		Map.Entry entry;
		HttpSession session = request.getSession();
		Iterator it = data[1].entrySet().iterator();
		while(it.hasNext()) {
			entry = (Map.Entry)it.next();
			session.setAttribute((String)entry.getKey(),entry.getValue());
		}
		url = request.getContextPath() + url + ".do?_reinstatePageId=" + id + mapToQueryString(data[0]);
		response.sendRedirect(url);
	}
	
	public static String mapToQueryString(Map map) {
		
	if(map==null || map.size()==0) return "";
	StringBuffer sb = new StringBuffer();
	String amp = "&";
	
	Iterator it = map.keySet().iterator();
	String key;
	Object value;
	Object[] valueArr;
	while(it.hasNext()) {
	  key = (String)it.next();
	  if(!key.startsWith(("_"))) {
		  value =  map.get(key);
		  if(value instanceof Object[]){
			  valueArr = (Object[])value;
			  for(int i=0;i<valueArr.length;i++) {
				  if (valueArr[i]!=null) sb.append(amp + key + "=" + URLEncoder.encode(valueArr[i].toString()));
				  //amp="&";
			  }
		  }else {
			  if(value!=null) sb.append(amp + key + "=" + URLEncoder.encode(value.toString()));
			//amp="&";	
		  }
	  } 
	}
	return sb.toString();
}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

}