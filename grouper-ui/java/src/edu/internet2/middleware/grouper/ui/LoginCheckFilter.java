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
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

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
 * @version $Id: LoginCheckFilter.java,v 1.20 2009-11-10 16:43:08 isgwb Exp $
 */

public class LoginCheckFilter implements Filter {
	protected Log tLOG = LogFactory.getLog("timings");
	protected Log LOG = LogFactory.getLog(LoginCheckFilter.class);

	private String failureUrl = "/";

	private String ignore = "";
	
	private String grouperRole="*";

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
	  
    GrouperStartup.startup();
	  
		String failureUrl = config.getInitParameter("failureUrl");
		if (failureUrl != null)
			this.failureUrl = failureUrl;
		
		String grouperRole = config.getInitParameter("grouperRole");
		if (grouperRole != null)
			this.grouperRole = grouperRole;
		
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
	public void doFilter(final ServletRequest req, final ServletResponse res,
			final FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession();

			
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
						//loggedOut = true;
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
		if(Boolean.TRUE.equals(request.getAttribute("forceNewLogin"))) {
			noCheck=false;
		}
		if (noCheck || authUser != null || loggedOut) {
			if (authUser != null
					&& (session.getAttribute("sessionInited") == null || session.getAttribute("sessionInited").equals(Boolean.FALSE))) {

				try {
					SessionInitialiser.init(request);
				} catch (Exception e) {
					throw new ServletException(e);
				}
			}
			Date before = new Date();

			
			SessionInitialiser.initThread(session);
			
			GrouperSession grouperSession = SessionInitialiser.getGrouperSession(session);
			
			try {
        GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
  
          public Object callback(GrouperSession innerGrouperSession)
              throws GrouperSessionException {
            try {
              chain.doFilter(req, res);
              return null;
            } catch (IOException ie) {
              throw new GrouperSessionException(ie);
            } catch (ServletException se) {
              throw new GrouperSessionException(se);
            }
          }
          
        });
			} catch (GrouperSessionException re) {
			  if (re.getCause() instanceof IOException) {
			    throw (IOException)re.getCause();
			  }
        if (re.getCause() instanceof ServletException) {
          throw (ServletException)re.getCause();
        }
        throw re;
			}
			
			Date after = new Date();
			if (tLOG != null) {
				String timingsClass = (String) request
						.getAttribute("timingsClass");
				Long ms = (Long) request.getAttribute("timingsMS");
				if (ms != null) {
					long diff = after.getTime() - before.getTime();
					long mss = ms.longValue();
					long renderMs = diff - mss;
					tLOG.debug(request.getServletPath() + "," + timingsClass
							+ "," + diff + "," + mss + "," + renderMs);
				}
			}
			return;
		}

		final String remoteUser = GrouperUiFilter.remoteUser(request);
		if (remoteUser == null || remoteUser.length() == 0 || (!"*".equals(grouperRole) && !request.isUserInRole(grouperRole) && !"y".equals(request.getParameter("badRole")))) {
			response.sendRedirect(request.getContextPath() + failureUrl + "?badRole=y"
					+ moduleStr);
			return;
		}

		Subject subj = null;
		UnrecoverableErrorException unrecov = null;
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
		try {
		  subj = (Subject)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          return SubjectFinder.findByIdOrIdentifier(remoteUser, true);
        }
      });
		} catch (SubjectNotFoundException e) {
			LOG.error(remoteUser + " is not recognised",e);
			unrecov = new UnrecoverableErrorException("error.login.not-recognised",e);		
		} catch (SubjectNotUniqueException e) {
			LOG.error(remoteUser + " is not unique",e);
			unrecov = new UnrecoverableErrorException("error.login.not-unique",e);
		} catch (Exception e) {
			LOG.error("Problem looking up remote user: " + remoteUser,e);
			unrecov = new UnrecoverableErrorException("error.login.serious-error",e);
		} finally {
		  GrouperSession.stopQuietly(grouperSession);
		}
		if(unrecov!=null) {
			throw unrecov;
		}
		session.setAttribute("authUser", remoteUser);
		//edu.internet2.middleware.subject.Subject subj =
		// GrouperSubject.load(remoteUser, Grouper.DEF_SUBJ_TYPE);
		GrouperSession s = null;
		try {
			s=GrouperSession.start(subj);
		}catch(Exception e) {
			throw new ServletException(e);
		}
		
		request.getSession().setAttribute(
				"edu.intenet2.middleware.grouper.ui.GrouperSession", s);
		//request.setAttribute("message",new
		// Message("auth.message.login-welcome",remoteUser));
		ErrorFilter.initNDC(request);
		try {
			SessionInitialiser.init(request);
		} catch (Exception e) {
			throw new ServletException(e);
		}
		SessionInitialiser.initThread(session);
		Map subjMap = GrouperHelper.subject2Map(subj);
		request.getSession().setAttribute("AuthSubject", subjMap);
		
		
    try {
      GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

        public Object callback(GrouperSession innerGrouperSession)
            throws GrouperSessionException {
          try {
            chain.doFilter(req, res);
            return null;
          } catch (IOException ie) {
            throw new GrouperSessionException(ie);
          } catch (ServletException se) {
            throw new GrouperSessionException(se);
          }
        }
        
      });
    } catch (GrouperSessionException re) {
      if (re.getCause() instanceof IOException) {
        throw (IOException)re.getCause();
      }
      if (re.getCause() instanceof ServletException) {
        throw (ServletException)re.getCause();
      }
      throw re;
    }


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