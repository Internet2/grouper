/*
 * @author mchyzer
 * $Id: Misc.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiCustomizer;
import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
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
  public GuiSettings settings(HttpServletRequest request, HttpServletResponse response) {
    GuiSettings guiSettings = new GuiSettings();
    guiSettings.setAuthnKey(GrouperUuid.getUuid());
    
    Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    guiSettings.setLoggedInSubject(new GuiSubject(loggedInSubject));
    
    //read the properties file
    Properties properties = GuiUtils.propertiesUiTextGui();
    for (String key : (Set<String>)(Object)properties.keySet()) {
      guiSettings.getText().put(key, properties.getProperty(key));
    }
    
    // lets see where the templates are: assume grouperUiText.properties is in WEB-INF/classes,
    // and templates are WEB-INF/templates
    File classesDirFile = GuiUtils.classFileDir(); 
    File templatesDir = new File(GrouperUtil.fileCanonicalPath(classesDirFile.getParentFile()) 
      + File.separator + "grouperUi" + File.separator + "templates");
    
    List<File> templates = GuiUtils.listFilesByExtensionRecursive(templatesDir, ".html");
    
    if (GrouperUtil.length(templates) == 0) {
      throw new RuntimeException("Cant find templates dir, thoguht it was: " + templatesDir.getAbsolutePath());
    }
    //go through templates and add to template map
    for (File templateFile : templates) {
      String template = GrouperUtil.readFileIntoString(templateFile);
      //we need the key
      String key = templateFile.getName();
      
      if (!templatesDir.equals(templateFile.getParentFile() )) {
        //use a dot to separate
        key = templateFile.getParentFile().getName() + "." + key;
        if (!templatesDir.equals(templateFile.getParentFile().getParentFile() )) {
          //use a dot to separate
          key = templateFile.getParentFile().getParentFile().getName() + "." + key;
          if (!templatesDir.equals(templateFile.getParentFile().getParentFile().getParentFile() )) {
            throw new RuntimeException("Cant handle templates more than 2 levels deep: " 
                + GrouperUtil.fileCanonicalPath(templateFile));
          }
        }
      }
      guiSettings.getTemplates().put(key, template);
    }
    
    return guiSettings;

  }

  /**
   * 
   * @param request
   * @param response
   * @return the bean to go to the screen, or null if none
   */
  @SuppressWarnings({ "cast", "unchecked" })
  public LogoutObject logout(HttpServletRequest request, HttpServletResponse response) {
    
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
    
    return logoutObject;
  
  }
  
}
