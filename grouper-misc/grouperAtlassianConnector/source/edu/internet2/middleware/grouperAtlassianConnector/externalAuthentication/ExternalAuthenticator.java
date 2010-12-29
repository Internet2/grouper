/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.externalAuthentication;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.seraph.auth.DefaultAuthenticator;

import edu.internet2.middleware.grouperAtlassianConnector.GrouperAtlassianUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * custom authenticator for jira
 */
@SuppressWarnings("serial")
public class ExternalAuthenticator extends DefaultAuthenticator {

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(ExternalAuthenticator.class);

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getUser(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public Principal getUser(HttpServletRequest request, HttpServletResponse response) {
    return getUser(request, response, "jira", DefaultAuthenticator.LOGGED_IN_KEY, null);
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getUser(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public static Principal getUser(HttpServletRequest request, HttpServletResponse response, 
      String authenticatorType, String userAttributeName, String logoutAttributeName) {

    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new HashMap<String, Object>();

    debugMap.put("operation", "getUser");
    debugMap.put("authenticatorType", authenticatorType);

    Principal user = null;

    String remoteUser = null;
    
    HttpSession session = request.getSession();
    
    String backdoorRequestParam = GrouperClientUtils.propertiesValue("atlassian.authentication.backdoorRequestParameterName", false);
    if (!GrouperClientUtils.isBlank(backdoorRequestParam)) {
      
      debugMap.put("backdoorRequestParam", backdoorRequestParam);

      remoteUser = request.getParameter(backdoorRequestParam);
      if (!GrouperClientUtils.isBlank(remoteUser)) {
        debugMap.put("retrievedFromBackdoorRequestParam_" + backdoorRequestParam, true);
      }
    }
    
    boolean cacheUserToken = GrouperClientUtils.propertiesValueBoolean(
        "atlassian.authentication.cacheUserToken", false, true);

    debugMap.put("cacheUserTokenInSession", cacheUserToken);

    if (cacheUserToken && session != null
        && session.getAttribute(userAttributeName) != null) {

      user = (Principal) session.getAttribute(
          userAttributeName);

      debugMap.put("retrievedFromSession", true);

    } else {

      remoteUser = request.getRemoteUser();

      if (GrouperClientUtils.isBlank(remoteUser)) {
        user = request.getUserPrincipal();
        remoteUser = user != null ? user
            .getName() : null;

        if (user != null) {
          debugMap.put("retrievedFromRequestDotGetUserPrincipal", true);
        }
      }

      if (GrouperClientUtils.isBlank(remoteUser)) {
        remoteUser = (String) request.getAttribute("REMOTE_USER");
        if (!GrouperClientUtils.isBlank(remoteUser)) {
          debugMap.put("retrievedFromRequestGetAttributeRemoteUser", true);
        }
      }

      if (GrouperClientUtils.isBlank(remoteUser)) {
        String attributeName = GrouperClientUtils.propertiesValue(
            "atlassian.authentication.requestPrincipalAttributeName", false);

        debugMap.put("requestAttributeName", attributeName);
        
        if (!GrouperClientUtils.isBlank(attributeName)) {
          remoteUser = (String) request.getAttribute(attributeName);
          if (!GrouperClientUtils.isBlank(remoteUser)) {
            debugMap.put("retrievedFromRequestGetAttribute_" + attributeName, true);
          }
        }
      }
      
      if (user == null && !GrouperClientUtils.isBlank(remoteUser)) {
        final String REMOTE_USER = remoteUser;
        
        user = new Principal() {
          
          /**
           * get name of principal
           */
          @Override
          public String getName() {
            
            return REMOTE_USER;
          }
        };
      }
    }
    
    if (user == null || GrouperClientUtils.isBlank(user.getName())) {
      debugMap.put("userIsNull", true);
    } else {
      debugMap.put("principalName", user.getName());
    }

    if (LOG.isDebugEnabled()) {
      GrouperAtlassianUtils.assignTimingGate(debugMap, startNanos);
      LOG.debug(GrouperAtlassianUtils.mapForLog(debugMap));
    }
    
    //put it back in cache
    if (cacheUserToken && !GrouperClientUtils.isBlank(userAttributeName)) {
      session.setAttribute(userAttributeName, user);
    }
    if (user != null && !GrouperClientUtils.isBlank(user.getName()) && !GrouperClientUtils.isBlank(logoutAttributeName)) {
      session.setAttribute(logoutAttributeName, null);
    }    
    
    return user;
  }

}
