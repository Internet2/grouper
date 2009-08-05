/*
 * @author mchyzer
 * $Id: Misc.java,v 1.2 2009-08-05 00:57:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiCustomizer;
import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.json.AppState;
import edu.internet2.middleware.grouper.grouperUi.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.json.GuiSettings;
import edu.internet2.middleware.grouper.grouperUi.json.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.json.LogoutObject;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class Misc {

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
