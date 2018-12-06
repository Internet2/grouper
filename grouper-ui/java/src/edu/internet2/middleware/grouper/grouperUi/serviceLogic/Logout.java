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
 * $Id: Misc.java,v 1.3 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiCustomizer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * invalidates HttpSession and sets cookie
 * to allow challenge from whatever is managing authenticated access.
 *
 * Ported from admin ui, edu.internet2.middleware.grouper.ui.actions.LogoutAction
 */

public class Logout {
  protected static Log LOG = LogFactory.getLog(Logout.class);

  /**
   * Utility function to perform logout: set message, delete cookies, invalidate session
   *
   * @param request
   * @param response
   * @param session
   * @param ajax if ajax call
   * @return true if ok, false if redirected
   */
  public static boolean logoutLogic(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, boolean ajax) {
    boolean mediaLogged=false;
    //String user = SessionInitialiser.getAuthUser(session);
    String user = (String) request.getSession().getAttribute("grouperLoginId");
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
    //mediaLogged=true;
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
        GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newScript("location.href = '" + logoutRedirect + "'"));
        return false;
      }
    }

    return true;
  }


  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/misc/index.jsp"));

  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void logout(HttpServletRequest request, HttpServletResponse response) {

    if (!logoutLogic(request, response, request.getSession(false), false)) {
      throw new ControllerDone(true);
    }

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
            "/WEB-INF/grouperUi2/public/logout.jsp"));

    Message message = (Message) request.getAttribute("message");
    guiResponseJs.addAction(GuiScreenAction.newMessage(
            message.isError()? GuiScreenAction.GuiMessageType.error : GuiScreenAction.GuiMessageType.success,
            TextContainer.retrieveFromRequest().getText().get(message.getText())));
  }
  
}
