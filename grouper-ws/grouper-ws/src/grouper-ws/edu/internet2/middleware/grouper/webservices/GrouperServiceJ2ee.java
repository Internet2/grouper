/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Extend the servlet to get user info
 * @author mchyzer
 *
 */
public class GrouperServiceJ2ee implements Filter {

    /** logger */
    private static final Log LOG = LogFactory.getLog(GrouperService.class);

    /**
	 * retrieve the user principal (who is authenticated) from
	 * the (threadlocal) request object
	 * 
	 * @return the user principal name
	 */
	public static String retrieveUserPrincipalNameFromRequest() {
		
		return "GrouperSystem";
//		HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
//		GrouperServiceUtils.assertTrue(httpServletRequest!=null, 
//				"HttpServletRequest is null, is the GrouperServiceServlet mapped in the web.xml?");
//		Principal principal = httpServletRequest.getUserPrincipal();
//		GrouperServiceUtils.assertTrue(principal != null, 
//				"There is no user logged in, make sure the container requires authentication");
//		return principal.getName();
	}

	/**
	 * retrieve the subject logged in to web service
	 * @return the subject
	 */
	public static Subject retrieveSubjectLoggedIn() {
		//use this to be the user connected, or the user act-as
		String userIdLoggedIn = "GrouperSystem"; //TODO GrouperServiceServlet.retrieveUserPrincipalNameFromRequest();
		Subject caller = null;
		try {
			try {
				caller = SubjectFinder.findById(userIdLoggedIn);
			} catch (SubjectNotFoundException snfe) {
				//if not found, then try any identifier
				caller = SubjectFinder.findByIdentifier(userIdLoggedIn);
			}

		} catch (Exception e) {
			throw new RuntimeException("Cant find subject from login id: " + userIdLoggedIn, e);
		}
		return caller;
	}

	/**
	 * retrieve the subject to act as
	 * @param actAsLookup 
	 * @return the subject
	 */
	public static Subject retrieveSubjectActAs(WsSubjectLookup actAsLookup) {

		Subject loggedInSubject = retrieveSubjectLoggedIn();
		
		//if there is no actAs specified, then just use the logged in user
		if (actAsLookup == null || actAsLookup.isBlank()) {
			return loggedInSubject;
		}
		
		//so there is an actAs specified, lets see if we are allowed to use it
		//first lets get the group you have to be in if you are going to 
		String actAsGroupName = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_ACT_AS_GROUP);
		
		//make sure there is one there
		if (StringUtils.isBlank(actAsGroupName)) {
			
			throw new RuntimeException("A web service is specifying an actAsUser, but there is no '"
					+ GrouperWsConfig.WS_ACT_AS_GROUP + "' specified in the grouper-ws.properties");
			
		}

		GrouperSession session = null;
		//get the group
		try {
			//use this to be the user connected, or the user act-as
			Subject subject = SubjectFinder.findById("GrouperSystem");
			
			session = GrouperSession.start(subject);

			Group actAsGroup = GroupFinder.findByName(session, actAsGroupName);
			
			//if the logged in user is a member of the actAs group, then allow the actAs
			if (actAsGroup.hasMember(loggedInSubject)) {
				//this is the subject the web service wants to use
				return actAsLookup.retrieveSubject();
			}
			
			//if not an effective member
			throw new RuntimeException("A web service is specifying an actAsUser, but the group specified in "
					+ GrouperWsConfig.WS_ACT_AS_GROUP + " in the grouper-ws.properties: '" + actAsGroupName
					+ "' does not have the logged in user as a member: '" + loggedInSubject + "'");
		} catch (SessionException se) {
			throw new RuntimeException(se);
		} catch (SubjectNotFoundException snfe) {
			throw new RuntimeException("Cant find subject 'GrouperSystem'", snfe);
		} catch (SubjectNotUniqueException snue) {
			throw new RuntimeException("Subject 'GrouperSystem' is not unique", snue);
		} catch (GroupNotFoundException gnfe) {
			//TODO this should be a ACT_AS_GROUP_PROBLEM result code
			throw new RuntimeException("A web service is specifying an actAsUser, but the group specified in "
					+ GrouperWsConfig.WS_ACT_AS_GROUP + " in the grouper.properties cant be found: " + actAsGroupName, gnfe);
			
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * thread local for request
	 */
	private static ThreadLocal<HttpServletRequest> threadLocalRequest = 
		new ThreadLocal<HttpServletRequest>();
	
	/**
	 * thread local for response
	 */
	private static ThreadLocal<HttpServletResponse> threadLocalResponse = 
		new ThreadLocal<HttpServletResponse>();

	/**
	 * public method to get the http servlet request
	 * @return the http servlet request
	 */
	public static HttpServletRequest retrieveHttpServletRequest() {
		return threadLocalRequest.get();
	}
	
	/**
	 * public method to get the http servlet request
	 * @return the http servlet request
	 */
	public static HttpServletResponse retrieveHttpServletResponse() {
		return threadLocalResponse.get();
	}
	
	/**
	 * filter method
	 */
	public void destroy() {
		//not needed
		
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		
		threadLocalRequest.set((HttpServletRequest)request);
		threadLocalResponse.set((HttpServletResponse)response);
		try {
			filterChain.doFilter(request, response);
		} finally {
			threadLocalRequest.remove();
			threadLocalResponse.remove();
		}
		
	}

	/**
	 * filter method
	 */
	public void init(FilterConfig arg0) throws ServletException {
		//not needed
	}
	
	
	
}
