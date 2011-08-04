/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;



/**
 * bean for simple membership update.  holds all state for this module
 */
public class SimpleMembershipUpdateContainer implements Serializable {

  /** member we are editing */
  private GuiMember enabledDisabledMember = null;
  
  /**
   * 
   * @return the membership lite name
   */
  public String getMembershipLiteName() {
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();
    String membershipLiteName = request.getParameter("membershipLiteName");
    if (!StringUtils.isBlank(membershipLiteName)) {
      if (!membershipLiteName.matches("^[a-zA-Z0-9_]+$")) {
        throw new RuntimeException("Invalid membership lite name, but be alpha numeric or underscore: " + membershipLiteName);
      }
    }
    return membershipLiteName;
  }
  
  /**
   * 
   * @return the css url for this group
   */
  public String getCssUrl() {
    //TODO
    return null;
  }
  
  /**
   * member we are editing
   * @return the enabledDisabledMembers
   */
  public GuiMember getEnabledDisabledMember() {
    return this.enabledDisabledMember;
  }
  
  /**
   * member we are editing
   * @param enabledDisabledMember1 the enabledDisabledMembers to set
   */
  public void setEnabledDisabledMember(GuiMember enabledDisabledMember1) {
    this.enabledDisabledMember = enabledDisabledMember1;
  }
  
  /**
   * store to session scope
   */
  public void storeToSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.getSession().setAttribute("simpleMembershipUpdateContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static SimpleMembershipUpdateContainer retrieveFromSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = (SimpleMembershipUpdateContainer)httpSession
      .getAttribute("simpleMembershipUpdateContainer");
    if (simpleMembershipUpdateContainer == null) {
      throw new NoSessionException(GrouperUiUtils.message("simpleMembershipUpdate.noContainer"));
    }
    return simpleMembershipUpdateContainer;
  }

  /** if can read group */
  private boolean canReadGroup;
  
  /** if can update group */
  private boolean canUpdateGroup;

  /**
   * group object
   */
  private GuiGroup guiGroup;

  /**
   * members in result
   */
  private GuiMember[] guiMembers;

  /**
   * set of name value pairs for screen
   */
  private Map<String, String> subjectDetails = new LinkedHashMap<String, String>();
  
  /**
   * subject for screen
   * TODO make a wrapper where source is not stored in session...
   */
  private Subject subjectForDetails;
  
  /**
   * filter by this, could be the key which is sourceId____subjectId, or just a string literal if nothing is selected
   */
  private String memberFilter;
  
  /**
   * if showing the user what is being filtered, put that here.  This wouldnt be
   * sourceId____subjectId, it would be friendlier
   */
  private String memberFilterForScreen;

  /**
   * cache of properties
   */
  private static GrouperCache<String, Properties> configCache = new GrouperCache<String, Properties>(
      SimpleMembershipUpdateContainer.class.getName() + ".configCache", 1000, true, 120, 120, false);
  
  /**
   * if showing the user what is being filtered, put that here.  This wouldnt be
   * sourceId____subjectId, it would be friendlier
   * @return the memberFilterForScreen
   */
  public String getMemberFilterForScreen() {
    return this.memberFilterForScreen;
  }
  
  /**
   * if showing the user what is being filtered, put that here.  This wouldnt be
   * sourceId____subjectId, it would be friendlier
   * @param memberFilterForScreen1 the memberFilterForScreen to set
   */
  public void setMemberFilterForScreen(String memberFilterForScreen1) {
    this.memberFilterForScreen = memberFilterForScreen1;
  }

  /**
   * filter by this, could be the key which is sourceId____subjectId, or just a string literal if nothing is selected
   * @return the memberFilter
   */
  public String getMemberFilter() {
    return this.memberFilter;
  }

  
  /**
   * filter by this, could be the key which is sourceId____subjectId, or just a string literal if nothing is selected
   * @param memberFilter1 the memberFilter to set
   */
  public void setMemberFilter(String memberFilter1) {
    this.memberFilter = memberFilter1;
  }

  /**
   * subject for screen
   * @return the subjectForDetails
   */
  public Subject getSubjectForDetails() {
    return this.subjectForDetails;
  }
  
  /**
   * subject for screen
   * @param subjectForDetails1 the subjectForDetails to set
   */
  public void setSubjectForDetails(Subject subjectForDetails1) {
    this.subjectForDetails = subjectForDetails1;
  }

  /**
   * set of name value pairs for screen
   * @return the subjectDetails
   */
  public Map<String, String> getSubjectDetails() {
    return this.subjectDetails;
  }

  
  /**
   * set of name value pairs for screen
   * @param subjectDetails1 the subjectDetails to set
   */
  public void setSubjectDetails(Map<String, String> subjectDetails1) {
    this.subjectDetails = subjectDetails1;
  }

  /**
   * 
   * @return the group
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }

  /**
   * group object
   * @param group1
   */
  public void setGuiGroup(GuiGroup group1) {
    this.guiGroup = group1;
  }

  /**
   * 
   * @return if can read group
   */
  public boolean isCanReadGroup() {
    return this.canReadGroup;
  }

  /**
   * if can read group
   * @param canReadGroup1
   */
  public void setCanReadGroup(boolean canReadGroup1) {
    this.canReadGroup = canReadGroup1;
  }

  /**
   * if can update group
   * @return if can update group
   */
  public boolean isCanUpdateGroup() {
    return this.canUpdateGroup;
  }

  /**
   * if can update group
   * @param canUpdateGroup1
   */
  public void setCanUpdateGroup(boolean canUpdateGroup1) {
    this.canUpdateGroup = canUpdateGroup1;
  }

  /**
   * members in result
   * @return members
   */
  public GuiMember[] getGuiMembers() {
    return this.guiMembers;
  }

  /**
   * members in result
   * @param members1
   */
  public void setGuiMembers(GuiMember[] members1) {
    this.guiMembers = members1;
  }

  /**
   * check config file or defaults
   * @param key
   * @return the value
   */
  public String configValue(String key) {
    return configValue(key, true);
  }

  /**
   * check config file or defaults
   * @param key
   * @param exceptionIfNotThere
   * @return the value
   */
  public String configValue(String key, boolean exceptionIfNotThere) {
    
    //lets see if there is an external config
    GuiGroup theGuiGroup = this.getGuiGroup();
    String urlConfig = null;
    if (theGuiGroup != null) {
      
      urlConfig = theGuiGroup.getMembershipConfigUrl();
      
      if (!StringUtils.isBlank(urlConfig)) {
        
        Properties properties = GrouperUtil.propertiesFromUrl(urlConfig, true, true, null);
        if (properties != null && properties.containsKey(key)) {
          return properties.getProperty(key);
        }
        
      }
    }
    
    
    String membershipLiteName = this.getMembershipLiteName();
    
    //lets see if this config file has a value
    String value = null;
    if (!StringUtils.isBlank(membershipLiteName)) {
      try {
        value = SimpleMembershipUpdateContainer.configFileValue(membershipLiteName, key);
        return value;
      } catch (MembershipLiteConfigNotFoundException mlcnfe) {
        //thats ok we will try somewhere else
      }
    }

    //try the default
    try {
      value = TagUtils.mediaResourceString(key);
    } catch (MissingResourceException mre) {
      if (exceptionIfNotThere) {
        throw new  RuntimeException("cant find config for key '" + key + "' in membershipLite config"
            + " (or default in media.properties: " + key + "), and membershipLiteName: " 
            + membershipLiteName + ", urlConfig: " + urlConfig, mre);
      }
    }
    return value;
  }

  /**
   * check config file or defaults
   * @param key 
   * @return true if true, false if false
   */
  public boolean configValueBoolean(
      String key) {
    return configValueBooleanHelper(key, null);
  }

  /**
   * check config file or defaults
   * @param key 
   * @param defaultValue 
   * @return true if true, false if false
   */
  public boolean configValueBoolean(
      String key, boolean defaultValue) {
    return configValueBooleanHelper(key, defaultValue);
  }
  
  /** 
   * text bean
   * @return text bean
   */
  public SimpleMembershipUpdateText getText() {
    return SimpleMembershipUpdateText.retrieveSingleton();
  }
  
  /**
   * check config file or defaults
   * @param key 
   * @param defaultValue or null if no default
   * @return true if true, false if false
   */
  private boolean configValueBooleanHelper(
      String key, Boolean defaultValue) {
    
    String valueString = configValue(key, false);
    
    if (StringUtils.equalsIgnoreCase(valueString, "true") || StringUtils.equalsIgnoreCase(valueString, "t")) {
      return true;
    }
    
    if (StringUtils.equalsIgnoreCase(valueString, "false") || StringUtils.equalsIgnoreCase(valueString, "f")) {
      return false;
    }
    
    if (StringUtils.isBlank(valueString) && defaultValue != null) {
      return defaultValue;
    }
    
    //throw descriptive exception
    throw new RuntimeException("Invalid value: '" + valueString + "' for key '" + key + "' in membershipLite config" +
        " (or default).  Should be true or false: '" + this.getGuiGroup() + "'");
  }

  /**
   * based on request get a media int
   * @param key 
   * @return true if true, false if false
   */
  public int configValueInt(
      String key) {
    
    String valueString = configValue(key);
    
    try {
      return GrouperUtil.intValue(valueString);
    } catch (Exception e) {
      //throw descriptive exception
      throw new RuntimeException("Invalid value: '" + valueString + "' for key '" + key + "' in membershipLite config" +
          " (or default).  Should be an int", e);
    }
  }

  
  
  /**
   * get a config from this finder's config file
   * @param membershipLiteName
   * @param key
   * @return the value
   * @throws MembershipLiteConfigNotFoundException 
   */
  private static String configFileValue(String membershipLiteName, String key) throws MembershipLiteConfigNotFoundException {
    
    Properties properties = configCache.get(membershipLiteName);
    
    String classpathName = "membershipLiteName/" + membershipLiteName + ".properties";
  
    if (properties == null) {
      
      File configFile = null;
      String configFileName = null;
      
      try { 
        configFile = GrouperUtil.fileFromResourceName(classpathName);
      } catch (Exception e) {
        //just ignore
      }
      if (configFile == null) {
        String configDir = TagUtils.mediaResourceString("simpleMembershipUpdate.confDir");
        if (!configDir.endsWith("/") && !configDir.endsWith("\\")) {
          configDir += File.separator;
        }
        configFile = new File(configDir + membershipLiteName + ".properties");
        configFileName = configFile.getAbsolutePath();
        if (!configFile.exists()) {
  
          //you must have a config file for each membership config usage
          throw new RuntimeException("Cant find config for: '" + membershipLiteName + "' in classpath as: " 
              + classpathName + " or on file system in " + configFileName);
  
        }
      }
      properties = GrouperUtil.propertiesFromFile(configFile, true);
      configCache.put(membershipLiteName, properties);
    }
    String value = properties.getProperty(key);
  
    if (value == null) {
      throw new MembershipLiteConfigNotFoundException("Cant find property: " + key + " for config name: " + membershipLiteName
          + " on classpath: " + classpathName 
          + " or in config file: media.properties[\"simpleMembershipUpdate.confDir\"]/" + membershipLiteName + ".properties");
    }
    return value;
  }
  
  /**
   * return true if should show breadcrumb row on default
   * @return true if should show breadcrumb row on default
   */
  public boolean isShowBreadcrumbRowByDefault() {
    return configValueBoolean("simpleMembershipUpdate.showBreadcrumbRowByDefault", true);
  }
  
  /**
   * return true if should show name row on default
   * @return true if should show name row on default
   */
  public boolean isShowNameRowByDefault() {
    return configValueBoolean("simpleMembershipUpdate.showNameRowByDefault", true);
  }
  
  /**
   * return true if should show path row on default
   * @return true if should show path row on default
   */
  public boolean isShowPathRowByDefault() {
    return configValueBoolean("simpleMembershipUpdate.showPathRowByDefault", true);
  }
  
  /**
   * return true if should show description row on default
   * @return true if should show description row on default
   */
  public boolean isShowDescriptionRowByDefault() {
    return configValueBoolean("simpleMembershipUpdate.showDescriptionRowByDefault", true);
  }
  
  /**
   * return true if should show id row on default
   * @return true if should show id row on default
   */
  public boolean isShowIdRowByDefault() {
    return configValueBoolean("simpleMembershipUpdate.showIdRowByDefault", false);
  }
  
  /**
   * return true if should show id path row on default
   * @return true if should show id path row on default
   */
  public boolean isShowIdPathRowByDefault() {
    return configValueBoolean("simpleMembershipUpdate.showIdPathRowByDefault", false);
  }
  
  /**
   * return true if should show alternate id path row on default
   * @return true if should show alternate id path row on default
   */
  public boolean isShowAlternateIdPathRowByDefault() {
    return configValueBoolean("simpleMembershipUpdate.showAlternateIdPathRowByDefault", false);
  }
  
  /**
   * return true if should show uuid row on default
   * @return true if should show uuid row on default
   */
  public boolean isShowUuidRowByDefault() {
    return configValueBoolean("simpleMembershipUpdate.showUuidRowByDefault", false);
  }
  
  
  
}
