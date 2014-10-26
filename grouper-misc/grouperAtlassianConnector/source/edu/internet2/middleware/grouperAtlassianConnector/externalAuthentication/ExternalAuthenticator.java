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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.externalAuthentication;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * custom authenticator for jira
 */
@SuppressWarnings("serial")
public class ExternalAuthenticator extends DefaultAuthenticator implements AtlassianGetUserable  {

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(ExternalAuthenticator.class);

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getUser(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public Principal getUser(HttpServletRequest request, HttpServletResponse response) {
    return getUser(this, request, response, "jira", DefaultAuthenticator.LOGGED_IN_KEY, null);
  }

  /**
   * @param atlassianGetUserable 
   * @param request 
   * @param response 
   * @param authenticatorType 
   * @param userAttributeName 
   * @param logoutAttributeName 
   * @return principal
   */
  public static Principal getUser(AtlassianGetUserable atlassianGetUserable, 
      HttpServletRequest request, HttpServletResponse response, 
      String authenticatorType, String userAttributeName, String logoutAttributeName) {

    Map<String, Object> debugMap = new HashMap<String, Object>();

    debugMap.put("operation", "getUser");
    debugMap.put("authenticatorType", authenticatorType);

    try {
    
      Principal user = null;
  
      String remoteUser = null;
      
      HttpSession session = request.getSession();
      
      //TODO change for 2.2
      //GrouperClientConfig.retrieveConfig().propertyValueBoolean(
      //    "atlassian.authentication.cacheUserToken", true);
      boolean cacheUserToken = GrouperClientUtils.propertiesValueBoolean(
          "atlassian.authentication.cacheUserToken", true, true);
  
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
          
          //TODO in 2.2
          //String attributeName = GrouperClientConfig.retrieveConfig().propertyValueString(
          //    "atlassian.authentication.requestPrincipalAttributeName");
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
          
          //kick in the backdoor
          //TODO in 2.2
          //String allowedBackdoorUsers = GrouperClientConfig.retrieveConfig().propertyValueString("atlassian.authentication.backdoorAllowedUsers");
          String allowedBackdoorUsers = GrouperClientUtils.propertiesValue("atlassian.authentication.backdoorAllowedUsers", false);
          
          //TODO in 2.2
          //String backdoorRequestParam = GrouperClientConfig.retrieveConfig().propertyValueString("atlassian.authentication.backdoorRequestParameterName");
          String backdoorRequestParam = GrouperClientUtils.propertiesValue("atlassian.authentication.backdoorRequestParameterName", false);
  
          if (!GrouperClientUtils.isBlank(backdoorRequestParam)) {
            
            debugMap.put("backdoorRequestParam", backdoorRequestParam);
  
            String newRemoteUser = request.getParameter(backdoorRequestParam);
            if (!GrouperClientUtils.isBlank(newRemoteUser)) {
              debugMap.put("retrievedFromBackdoorRequestParam_" + backdoorRequestParam, true);
              debugMap.put("retrievedFromBackdoorRequestParam_" + backdoorRequestParam + "_value", newRemoteUser);
              
              List<String> usersAllowedToBackdoorList = GrouperClientUtils.splitTrimToList(allowedBackdoorUsers, ",");
              if (usersAllowedToBackdoorList != null
                  && usersAllowedToBackdoorList.contains(remoteUser)) {
                debugMap.put("atlassian.authentication.backdoorAllowedUsers", "contains " + remoteUser);
                remoteUser = newRemoteUser;
                //if getting from param, then cache the user token since it wont be there each time
                cacheUserToken = true;
              } else {
                //might as well throw an exception so the user knows whats going on
                debugMap.put("atlassian.authentication.backdoorAllowedUsers", "doesnt contain " + remoteUser);
                throw new RuntimeException("grouper.client.properties atlassian.authentication.backdoorAllowedUsers doesnt contain " + remoteUser);
              }
            }
          }
          
          
          
          user = atlassianGetUserable.getUser(remoteUser);
        }
      }
      
      if (user == null || GrouperClientUtils.isBlank(user.getName())) {
        debugMap.put("userIsNull", true);
      } else {
        debugMap.put("principalName", user.getName());
      }
  
      //put it back in cache
      if (cacheUserToken && !GrouperClientUtils.isBlank(userAttributeName)) {
        session.setAttribute(userAttributeName, user);
      }
      if (user != null && !GrouperClientUtils.isBlank(user.getName()) && !GrouperClientUtils.isBlank(logoutAttributeName)) {
        session.setAttribute(logoutAttributeName, null);
      }
      
      return user;
    } catch (RuntimeException re) {
      LOG.error("Error authenticating: " + GrouperClientUtils.mapToString(debugMap), re);
      throw re;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperClientUtils.mapToString(debugMap));
      }
    }
  }

  /**
   * @see AtlassianGetUserable#getUser(String)
   */
  @Override
  public Principal getUser(final String username) {
    
    return new Principal() {

      /**
       * 
       * @see java.security.Principal#getName()
       */
      public String getName() {
        return username;
      }

      /**
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode() {
        if (username == null) {
          return -1;
        }
        return username.hashCode();
      }

      /**
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object obj) {
        
        if (obj == null) {
          return false;
        }
        
        if (!(obj instanceof Principal)) {
          return false;
        }
        
        Principal objPrincipal = (Principal)obj;
        
        if (username == objPrincipal.getName()) {
          return true;
        }
        
        if (username == null || objPrincipal.getName() == null) {
          return false;
        }
        
        return username.equals(objPrincipal.getName());
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString() {
        return username;
      }
      
      
      
    };
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#authenticate(java.security.Principal, java.lang.String)
   */
  @Override
  protected boolean authenticate(Principal arg0, String arg1) throws AuthenticatorException {
    return false;
  }
  
}
