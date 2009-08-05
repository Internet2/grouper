/*
 * @author mchyzer
 * $Id: Misc.java,v 1.3 2009-08-05 06:38:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiCustomizer;
import edu.internet2.middleware.grouper.grouperUi.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.json.LogoutObject;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class Misc {

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/misc/index.jsp"));

  }
  
  /**
   * 
   * @param request
   * @param response
   * @return the bean to go to the screen, or null if none
   */
  @SuppressWarnings({ "cast", "unchecked" })
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    
    Properties propertiesSettings = GrouperUtil.propertiesFromResourceName(
      "grouperUiSettings.properties");

    //see if cookies
    String cookiePrefix = GrouperUtil.propertiesValue(propertiesSettings, "grouperUi.logout.cookie.prefix");
    if (!StringUtils.isBlank(cookiePrefix)) {
      String[] cookiePrefixes = GrouperUtil.splitTrim(cookiePrefix, ",");
      for (String theCookiePrefix : cookiePrefixes) {
        GuiUtils.removeCookiesByPrefix(theCookiePrefix);
      }
    }

    //custom logic
    GrouperUiCustomizer.instance().logout();
    
    LogoutObject logoutObject = new LogoutObject();
    logoutObject.setSuccess(true);

//TODO
//    replaceHtmlWithTemplate('#bodyDiv', 'common.logout.html');
//    $("#topDiv").html("");

  
  }
  
}
