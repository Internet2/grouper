/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeNameUpdate;

import edu.internet2.middleware.grouper.ui.tags.TagUtils;


/**
 * get text in external URL (if applicable), and from nav.properties
 */
public class AttributeNameUpdateText {

  /** singleton */
  private static AttributeNameUpdateText attributeNameUpdateText = new AttributeNameUpdateText();

  /**
   * title of main screen
   * @return title of main screen
   */
  public String getCreateEditIndexTitle() {
    return text("simpleAttributeNameUpdate.createEditIndexTitle");
  }
  
  /**
   * infodot of title of main screen
   * @return infodot of title of main screen
   */
  public String getCreateEditIndexTitleInfodot() {
    return text("simpleAttributeNameUpdate.createEditIndexTitleInfodot");
  }
  
  /**
   * get singleton
   * @return singleton
   */
  public static AttributeNameUpdateText retrieveSingleton() {
    return attributeNameUpdateText;
  }

  
  /**
   * get text based on key
   * @param key
   * @return text
   */
  public String text(String key) {
    
    //finally, just go to nav.propreties
    return TagUtils.navResourceString(key);
    
  }
  
  
}
