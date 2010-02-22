/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.util.GrouperHtmlFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * get text in external URL (if applicable), and from nav.properties
 */
public class SimpleMembershipUpdateText {

  /** singleton */
  private static SimpleMembershipUpdateText simpleMembershipUpdateText = new SimpleMembershipUpdateText();
  
  /**
   * get singleton
   * @return singleton
   */
  public static SimpleMembershipUpdateText retrieveSingleton() {
    return simpleMembershipUpdateText;
  }

  /** grouper html filter */
  private static GrouperHtmlFilter grouperHtmlFilter;
  
  /** if found grouper html filter found */
  private static boolean grouperHtmlFilterFound = false;
  
  /**
   * cache this
   * @return grouper html filter
   */
  @SuppressWarnings("unchecked")
  private static GrouperHtmlFilter grouperHtmlFilter() {
    if (!grouperHtmlFilterFound) {
      String grouperHtmlFilterString = TagUtils.mediaResourceString("simpleMembershipUpdate.externalUrlTextProperties.grouperHtmlFilter");
      Class<GrouperHtmlFilter> grouperHtmlFilterClass = GrouperUtil.forName(grouperHtmlFilterString);
      grouperHtmlFilter = GrouperUtil.newInstance(grouperHtmlFilterClass);
      grouperHtmlFilterFound = true;
    }
    return grouperHtmlFilter;
  }
  
  /**
   * get text based on key
   * @param key
   * @return text
   */
  public String text(String key) {
    
    //first try from URL external
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    String textUrl = simpleMembershipUpdateContainer.configValue("simpleMembershipUpdate.textFromUrl", false);
    
    if (!StringUtils.isBlank(textUrl)) {
      
      Properties properties = GrouperUtil.propertiesFromUrl(textUrl, true, true, grouperHtmlFilter());
      
      if (properties.containsKey(key)) {
        return properties.getProperty(key);
      }
      
    }
    
    //then try from name of simplemembership updater
    String name = simpleMembershipUpdateContainer.getMembershipLiteName();
    if (!StringUtils.isBlank(name)) {
      String namedKey = "membershipLiteName." + name + "." + key;
      if (TagUtils.navResourceContainsKey(namedKey)) {
        return TagUtils.navResourceString(namedKey);
      }
    }

    //finally, just go to nav.propreties
    return TagUtils.navResourceString(key);
    
  }
  
  /**
   * title of update screen
   * @return title
   */
  public String getUpdateTitle() {
    return text("simpleMembershipUpdate.updateTitle");
  }
  
  /**
   * infodot of update screen
   * @return infordot
   */
  public String getUpdateTitleInfodot() {
    return text("infodot.title.simpleMembershipUpdate.updateTitle");
  }
  
  
  
}
