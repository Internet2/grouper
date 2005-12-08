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

package edu.internet2.middleware.grouper.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.actions.LowLevelGrouperCapableAction;
import edu.internet2.middleware.subject.Source;

/**
 * Protects access to resources not listed in the init parameter 'ignore'.
 * Checks if HttpServletRequest.getRemoteUser() has been set, and ensures that a
 * Grouper session is initialised for the user, and that the HttpSession is
 * setup appropriately. <p/>LoginCheckFilter can work with any authentication
 * scheme which causes the getRemoteUser() method to return an appropriate
 * value. This includes other Filters which precede the LoginCheckFilter. The
 * contributed Yale CAS authentication solution uses this approach <p/>If you
 * use Tomcat's default authentication, or some other implementation of HTTP
 * Basic Authentication, then logging out will not work properly
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: LoginCheckFilter.java,v 1.3 2005-12-08 15:30:19 isgwb Exp $
 */

public class LoginCheckFilter implements Filter {
	protected Log log = LogFactory.getLog("timings");

	private String failureUrl = "/";

	private String ignore = "";

	/**
	 * Constructor
	 */
	public LoginCheckFilter() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		String failureUrl = config.getInitParameter("failureUrl");
		if (failureUrl != null)
			this.failureUrl = failureUrl;
		String ignore = config.getInitParameter("ignore");
		if (ignore != null)
			this.ignore = ignore;
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
		HttpSession session = request.getSession();
		UIThreadLocal.clear();
		UIThreadLocal.put("navResource", new ArrayList());
		UIThreadLocal.put("dynamicTiles", new ArrayList());
		Map debugPrefs = (Map) session.getAttribute("debugPrefs");
		if(debugPrefs==null && LowLevelGrouperCapableAction.getCookie("grouperDebugPrefs",request)!=null) {
			try {
				debugPrefs = LowLevelGrouperCapableAction.readDebugPrefs(request);
			}catch(Exception e){}
		}
		if (debugPrefs != null) {
			Boolean doShowResourcesInSitu = (Boolean) debugPrefs
					.get("doShowResourcesInSitu");
			if (Boolean.TRUE.equals(doShowResourcesInSitu)) {
				UIThreadLocal.put("doShowResourcesInSitu", Boolean.TRUE);
			} else {
				UIThreadLocal.put("doShowResourcesInSitu", Boolean.FALSE);
			}
		}

		Cookie[] cookies = request.getCookies();
		boolean loggedOut = false;
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if ("_grouper_loggedOut".equals(cookies[i].getName())) {
					
					//If pending and RemoteUser set then login
					//was successful - so clear cookie
					if ("pending".equals(cookies[i].getValue())
							&& request.getRemoteUser() != null) {
						cookies[i].setMaxAge(0);
						response.addCookie(cookies[i]);
						response.setHeader("GrouperRelogin", "true");
					} else {
						loggedOut = true;
					}
				}
			}
		}
		String servletPath = request.getServletPath();
		int lastSlash = servletPath.lastIndexOf("/");
		String actionStr = servletPath.substring(lastSlash);
		String moduleStr = "";
		if (lastSlash > 1) {
			moduleStr = servletPath.substring(1, lastSlash);

		}
		String authUser = (String) session.getAttribute("authUser");
		boolean noCheck = ignore.indexOf(":" + actionStr + ":") > -1;
		if (noCheck || authUser != null || loggedOut) {
			if (authUser != null
					&& session.getAttribute("sessionInited") == null) {

				try {
					SessionInitialiser.init(request);
				} catch (Exception e) {
					throw new ServletException(e);
				}
			}
			Date before = new Date();

			chain.doFilter(req, res);
			Date after = new Date();
			if (log != null) {
				String timingsClass = (String) request
						.getAttribute("timingsClass");
				Long ms = (Long) request.getAttribute("timingsMS");
				if (ms != null) {
					long diff = after.getTime() - before.getTime();
					long mss = ms.longValue();
					long renderMs = diff - mss;
					log.debug(request.getServletPath() + "," + timingsClass
							+ "," + diff + "," + mss + "," + renderMs);
				}
			}
			return;
		}

		String remoteUser = request.getRemoteUser();
		if (remoteUser == null || remoteUser.length() == 0) {
			response.sendRedirect(request.getContextPath() + failureUrl
					+ moduleStr);
			return;
		}

		edu.internet2.middleware.subject.Subject subj = null;
		Source source = null;

		try {
			subj = SubjectFinder.findByIdentifier(remoteUser);
		} catch (Exception e) {
			throw new IOException(remoteUser + " is not recognised");
		}
		session.setAttribute("authUser", remoteUser);
		//edu.internet2.middleware.subject.Subject subj =
		// GrouperSubject.load(remoteUser, Grouper.DEF_SUBJ_TYPE);
		GrouperSession s = null;
		try {
			s=GrouperSession.startSession(subj);
		}catch(Exception e) {
			throw new ServletException(e);
		}
		Map subjMap = GrouperHelper.subject2Map(subj);
		request.getSession().setAttribute("AuthSubject", subjMap);
		request.getSession().setAttribute(
				"edu.intenet2.middleware.grouper.ui.GrouperSession", s);
		//request.setAttribute("message",new
		// Message("auth.message.login-welcome",remoteUser));
		try {
			SessionInitialiser.init(request);
		} catch (Exception e) {
			throw new ServletException(e);
		}
		chain.doFilter(req, res);

	}

	private void initSession(ServletRequest request,
			HttpServletResponse response) throws Exception {
		CaptureHttpServletResponse chsr = new CaptureHttpServletResponse(
				response);
		RequestDispatcher rd = request
				.getRequestDispatcher("/WEB-INF/jsp/initSession.jsp");
		rd.include(request, chsr);
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