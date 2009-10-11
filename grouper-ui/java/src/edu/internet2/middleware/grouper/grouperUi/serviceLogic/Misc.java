/*
 * @author mchyzer
 * $Id: Misc.java,v 1.3 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiCustomizer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * miscellaneous ajax methods
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
   */
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    
    //see if cookies
    String cookiePrefix = TagUtils.mediaResourceString("grouperUi.logout.cookie.prefix");
    if (!StringUtils.isBlank(cookiePrefix)) {
      String[] cookiePrefixes = GrouperUtil.splitTrim(cookiePrefix, ",");
      for (String theCookiePrefix : cookiePrefixes) {
        GrouperUiUtils.removeCookiesByPrefix(theCookiePrefix);
      }
    }

    //custom logic
    GrouperUiCustomizer.instance().logout();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/misc/logout.jsp"));
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#topDiv", ""));
  
  }
  
}
