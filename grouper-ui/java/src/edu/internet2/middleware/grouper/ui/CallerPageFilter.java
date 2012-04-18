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

import edu.internet2.middleware.grouper.misc.GrouperStartup;
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
 * @version $Id: CallerPageFilter.java,v 1.6 2009-08-12 04:52:14 mchyzer Exp $
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
	  GrouperStartup.startup();
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
