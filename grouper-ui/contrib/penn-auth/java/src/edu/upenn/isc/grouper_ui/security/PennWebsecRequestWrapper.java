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
 * $Id: PennWebsecRequestWrapper.java,v 1.4 2009-03-15 06:37:51 mchyzer Exp $
 */
package edu.upenn.isc.grouper_ui.security;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.subject.Subject;
import edu.upenn.isc.fast.websec.WebsecClient;
import edu.upenn.isc.fast.websec.WebsecException;
import edu.upenn.isc.fast.websec.WebsecOutput;


/**
 * request wrapper to expose user of webapp
 */
public class PennWebsecRequestWrapper extends HttpServletRequestWrapper {

  /** logger */
  protected static final Log LOG = LogFactory.getLog(PennWebsecRequestWrapper.class);

  /** key in session attribute for penn user */
  private static final String PENN_SESSION_USER_KEY = "pennUser";
  
  /** key in session attribute for penn token */
  private static final String PENN_SESSION_TOKEN_KEY = "pennToken";

  /** reference to response since we might need it for cookies */
  private HttpServletResponse httpServletResponse = null;
  
  /** 
   * get the penn token from request param or cookie
   * @return the token
   */
  private String retrievePennToken() {
    String tokenParam = this.getParameter("websec_token");
    if (StringUtils.isNotBlank(tokenParam)) {
      return tokenParam;
    }
    //not there, check cookie
    return GrouperUiUtils.cookieValue("websec_token", this.getCookies());
  }
  
  /**
   * @see javax.servlet.http.HttpServletRequestWrapper#getRemoteUser()
   */
  @Override
  public String getRemoteUser() {
    
    HttpSession httpSession = this.getSession();
    
    //see if token changed
    String existingToken = (String)httpSession.getAttribute(PENN_SESSION_TOKEN_KEY);
    String newToken = this.retrievePennToken();
    
    if (!StringUtils.isBlank(existingToken)) {
      
      //see new token
      if (!StringUtils.isBlank(newToken)) {
        if (!StringUtils.equals(existingToken, newToken)) {

          //this is a problem, trying to login again, and there is an existing user, 
          //abort by killing session
          httpSession.invalidate();

          //kill the penn cookie
          GrouperUiUtils.killCookie("websec_token", this.getCookies(), this.httpServletResponse);
          
          //this will probably cause an exception or cause the user to login or something
          return null;
        }
      }
    }
    
    //see if stashed in session
    String user = (String)httpSession.getAttribute(PENN_SESSION_USER_KEY);
    
    if (StringUtils.isBlank(user)) {
      
      //see if passed in
      if (StringUtils.isBlank(newToken)) {
        return null;
      }
      
      //decode the token, but dont use media map since it might not exist yet
      //granted this will not take into account local media.properties, but shouldnt
      //be customized much
      Properties mediaProperties = GrouperUiUtils
        .propertiesFromResourceName("resources/custom/media.properties");
      
      String appName = StringUtils.defaultIfEmpty((String)mediaProperties.get("pennWebsecAppName"), "StudentHome");
      String websecBinary = StringUtils.defaultIfEmpty((String)mediaProperties.get("pennWebsecBinary"), 
          System.getProperty("os.name").contains("Windows") ? 
              "c:\\websec\\websec_client.exe" : "/usr/local/lib/websec/websec_client");
      
      StringBuffer logBuffer = new StringBuffer();
      WebsecOutput websecOutput = null;
      
      try {
        websecOutput = WebsecClient.callWebsecClient(newToken, appName, logBuffer, false, websecBinary);
      } catch (WebsecException we) {
        //not sure if this is in exception or not
        LOG.error(logBuffer.toString(), we);
        throw we;
      }
      
      user = websecOutput.getPennid();
      
      //if configured to be in group, make sure
      String uiGroupName = (String)mediaProperties.get("penn.uiGroup");
      if (!StringUtils.isBlank(uiGroupName)) {
        GrouperSession grouperSession = null;
        
        try {
          grouperSession = GrouperSession.start(
            SubjectFinder.findById("GrouperSystem", true)
          );
          Subject subject = SubjectFinder.findByIdentifier(user, true);
          Group group = GroupFinder.findByName(grouperSession, uiGroupName, true);
          if (!group.hasMember(subject)) {
            throw new RuntimeException("User is not authorized");
          }
        } catch (Exception e) {
          LOG.error("user: '" + user + "' is not a member of group: '" + uiGroupName 
              + "', and therefore is not authorized to use the app (configured in local media.properties penn.uiGroup");
          throw new RuntimeException("User is not authorized", e);
        } finally {
          GrouperSession.stopQuietly(grouperSession);
        }
      }
      
      //stash in session
      httpSession.setAttribute(PENN_SESSION_USER_KEY, user);
      httpSession.setAttribute(PENN_SESSION_TOKEN_KEY, newToken);
    }
    
    return user;
    
  }

  /**
   * @param request
   * @param theHttpServletResponse is the response in case we need it for cookies
   */
  public PennWebsecRequestWrapper(HttpServletRequest request, HttpServletResponse theHttpServletResponse) {
    super(request);
    this.httpServletResponse = theHttpServletResponse;
  }

}
