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

package edu.internet2.middleware.grouper.ui.actions;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.GrouperUiCustomizer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Top level Strut's action which invalidates HttpSession and sets cookie 
 * to alllow challenge from whatever is managing authenticated access. 
 * <p/>
 
 <table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">message</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Message instance: text derived 
      from auth.message.logout-success key in nav ResourceBundle</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">loggedOut=true</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates to GrouperCapableAction.execute 
      that user has logged out and invalidated the HttpSession</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td height="28"><font face="Arial, Helvetica, sans-serif">authUser</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used as argument to message</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Strut's Action Parameter</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Cookie</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">_grouper_loggedOut=true</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Out</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates to authentication 
      mechanism that user logged out of application and should re-authenticate</font></td>
  </tr>
</table>

 * @author Gary Brown.
 * @version $Id: LogoutAction.java,v 1.4 2009-08-12 04:52:14 mchyzer Exp $
 */

public class LogoutAction extends GrouperCapableAction {
	protected static Log LOG = LogFactory.getLog(LogoutAction.class);
	private static boolean mediaLogged=false;
	
	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_Index = "Index";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session,GrouperSession grouperSession)
			throws Exception {
	  
		if (!logoutLogic(request, response, session, false)) {
		  return null;
		}
    
		return mapping.findForward(FORWARD_Index);
	}

  /**
   * @param request
   * @param response
   * @param session
   * @param ajax if ajax call
   * @return true if ok, false if redirected
   */
  public static boolean logoutLogic(HttpServletRequest request, HttpServletResponse response,
      HttpSession session, boolean ajax) {
    String user = SessionInitialiser.getAuthUser(session);
		if (user == null)
			user = "";
		if("BASIC".equals(request.getAuthType())) {
			request.setAttribute("message", new Message(
					"auth.message.logout-basic", user,true));
		}else{
			request.setAttribute("message", new Message(
				"auth.message.logout-success", user));
		}
		ResourceBundle media = GrouperUiFilter.retrieveSessionMediaResourceBundle();
		String cookiesToDelete = "none";
		try {
			cookiesToDelete = media.getString("logout.cookies-to-delete");
			if(!mediaLogged) LOG.info("logout.cookies-to-delete=" + cookiesToDelete);
		}catch(MissingResourceException mre) {
			if(!mediaLogged) LOG.info("logout.cookies-to-delete not present in media.properties");
		}
		mediaLogged=true;
		String[] cookieNames = null;
		if (!StringUtils.isBlank(cookiesToDelete)) {
		  if (cookiesToDelete.contains(",")){
		    cookieNames = GrouperUtil.splitTrim(cookiesToDelete, ",");
		  } else if (cookiesToDelete.contains("|")) {
        cookieNames = GrouperUtil.splitTrim(cookiesToDelete, "|");
		  } else {
		    cookieNames = GrouperUtil.splitTrim(cookiesToDelete, " ");
		  }
	    if(cookieNames.length!=1 || !cookieNames[0].equals("none")) {
	      Cookie[] cookies = request.getCookies();
	      if(cookies!=null) {
	        for(Cookie c : cookies) {
	          for(String name : cookieNames) {
	            try {
	              if((cookieNames.length==1 && "all".equals(name)) || c.getName().equals(name) || c.getName().matches(name)) {
	                c.setMaxAge(0);
	                c.setPath("/");
	                c.setValue("");
	                response.addCookie(c);
	                break;
	              }
	            }catch(Exception e) {
	              LOG.error("Error matching " + c.getName() + " with " + name,e);
	            }
	          }
	        }
	      }
	    }
		}

		{
		  //logic from lite ui
		  
      //see if cookies
      String cookiePrefix = GrouperUiConfig.retrieveConfig().propertyValueString("grouperUi.logout.cookie.prefix");
      if (!StringUtils.isBlank(cookiePrefix)) {
        String[] cookiePrefixes = GrouperUtil.splitTrim(cookiePrefix, ",");
        for (String theCookiePrefix : cookiePrefixes) {
          GrouperUiUtils.removeCookiesByPrefix(theCookiePrefix);
        }
      }
  
      //custom logic
      GrouperUiCustomizer.instance().logout();
		}
		
		LOG.info("User logged out");
		if (session != null) {
	    session.invalidate();
		}
		SessionInitialiser.init(request);
		
		//CH: 2014/12/21: I dont know what this is
		Cookie cookie = new Cookie("_grouper_loggedOut", "true");
		response.addCookie(cookie);
		request.setAttribute("loggedOut", Boolean.TRUE);

    //see if redirect
    String logoutRedirect = GrouperUiConfig.retrieveConfig().propertyValueString("grouperUi.logout.redirectToUrl");
    
    if (!StringUtils.isBlank(logoutRedirect)) {
      if (ajax) {
        GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newScript("location.href = '../../logout.do'"));
      } else {
        try {
          response.sendRedirect(logoutRedirect);
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        }
        return false;
      }
    }

    return true;
  }
}
