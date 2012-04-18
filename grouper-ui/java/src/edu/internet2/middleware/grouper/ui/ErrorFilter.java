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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
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
import edu.internet2.middleware.grouper.ui.actions.LowLevelGrouperCapableAction;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;


/**
 * This should be the first filter. It is responsible for catching errors
 * in any other filter. it also does some UIThreadLocal setup
 * 
 *  * <p />
 * 
 * @author Gary Brown.
 * @version $Id: ErrorFilter.java,v 1.5 2009-10-11 07:32:24 mchyzer Exp $
 */

public class ErrorFilter implements Filter {
	
	protected static Log LOG = LogFactory.getLog(ErrorFilter.class);
	
	/**
	 * Constructor
	 */
	public ErrorFilter() {
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
		HttpSession session=request.getSession();
		String x = request.getRequestURI();
		Integer requestCount = incrementRequestCount(session);
    requestCount = Integer.valueOf(requestCount.intValue()+1);
    session.setAttribute("requestCount",requestCount);

		if(!Boolean.TRUE.equals(session.getAttribute("sessionInited"))) {
		    org.apache.struts.config.ModuleConfig config1 = (org.apache.struts.config.ModuleConfig) request.getAttribute("org.apache.struts.action.MODULE");
		    String module = "";
		    if(config1!=null) module=config1.getPrefix();
		    try {    
				SessionInitialiser.init(module,session);
			}catch(Exception e) {
				Log LOG = LogFactory.getLog(ErrorFilter.class);
				LOG.error("Error initialising session: " + e.getMessage(), e);
			}
		}
		
		initNDC(request);
		UIThreadLocal.clear();
		UIThreadLocal.put("navResource", new LinkedHashSet());
		UIThreadLocal.put("dynamicTiles", new ArrayList());
		Map debugPrefs = (Map) session.getAttribute("debugPrefs");
		if(debugPrefs==null && LowLevelGrouperCapableAction.getCookie("grouperDebugPrefs",request)!=null) {
			try {
				debugPrefs = LowLevelGrouperCapableAction.readDebugPrefs(request);
			}catch(Exception e){}
		}
		
		if (debugPrefs != null) {
			Boolean debugActive = (Boolean)debugPrefs.get("isActive");
			if(debugActive!=null) {
				UIThreadLocal.setDebug(debugActive);
			}
			Boolean doShowResourcesInSitu = (Boolean) debugPrefs
					.get("doShowResourcesInSitu");
			if (Boolean.TRUE.equals(doShowResourcesInSitu)) {
				UIThreadLocal.put("doShowResourcesInSitu", Boolean.TRUE);
			} else {
				UIThreadLocal.put("doShowResourcesInSitu", Boolean.FALSE);
			}
		}
		StringBuffer requestUrl = (StringBuffer) request.getAttribute("_pageUrl");
		if(requestUrl==null || requestUrl.length()==0) { 
			requestUrl=request.getRequestURL();
			request.setAttribute("_pageUrl",requestUrl);
		}
		try {
		chain.doFilter(req, res);
		}catch(Throwable t) {
			Throwable cause=null;
			if(t instanceof UnrecoverableErrorException) {
				cause=t;
			}else{
				cause=t.getCause();
				if(cause==null) cause=t;
			}
			if(!(cause instanceof UnrecoverableErrorException)) {
				LOG.error(NavExceptionHelper.toLog(cause));
				cause=new UnrecoverableErrorException(cause);
			}
			NavExceptionHelper neh=LowLevelGrouperCapableAction.getExceptionHelper(session);
			String msg = neh.getMessage((UnrecoverableErrorException)cause);
			request.setAttribute("seriousError",msg);
			//for some reason this has to be able the getRequestDispatcher...
      boolean committed = response.isCommitted();
			RequestDispatcher rd = request.getRequestDispatcher("/filterError.do");
			try {
        if (!committed) {
			    response.setContentType("text/html");
          rd.forward(request, response);
			  } else {
			    rd.include(request, response);
			  }
			}catch(Throwable tt) {
				LOG.error("Failed to include error page:\n" + NavExceptionHelper.toLog(tt));
				response.sendError(500);
			}
		} finally {
		  try {
		    org.apache.log4j.NDC.remove();
		  } catch (Exception e) {
		    LOG.debug("error", e);
		  }
		}
	}

  /**
   * @param session
   */
  private static Integer incrementRequestCount(HttpSession session) {
    Integer requestCount = (Integer)session.getAttribute("requestCount");
		if(requestCount==null) {
			requestCount=Integer.valueOf(1);
			session.setAttribute("requestCount",requestCount);
		}
		return requestCount;
  }
	
	public static void initNDC(HttpServletRequest request) {
		HttpSession session=request.getSession();
		incrementRequestCount(session);
		String requestCount = session.getAttribute("requestCount").toString();
		String pad="0000";
		requestCount=pad.substring(requestCount.length()) + requestCount;
		GrouperSession s = (GrouperSession)session.getAttribute(
				"edu.intenet2.middleware.grouper.ui.GrouperSession");
		String remoteUser=GrouperUiFilter.remoteUser(request);
				
		String sessionId = session.getId();
		StringBuffer ndc = new StringBuffer("< ");
	    org.apache.log4j.NDC.clear();
	     if(remoteUser==null) {
	      	ndc.append("-");
		 }else {
			ndc.append(remoteUser);
	     }
	     ndc.append(" ");
	     String id = sessionId  + "-" + requestCount;
	     request.setAttribute("uiRequestId",id);
	     ndc.append(id + " ");
	     if(s==null) {
	    	 ndc.append("- - -");
	     }else{
	    	 ndc.append(s.getSessionId() + " " + s.getSubject().getId() + " " + s.getSubject().getSource().getId());
	     }
	     ndc.append(" >");
	     org.apache.log4j.NDC.push(ndc.toString());
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
