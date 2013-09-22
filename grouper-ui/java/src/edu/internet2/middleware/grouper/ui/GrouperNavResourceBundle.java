/**
 * 
 */
package edu.internet2.middleware.grouper.ui;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import edu.internet2.middleware.grouperClient.config.GrouperUiTextConfig;


/**
 * @author mchyzer
 *
 */
public class GrouperNavResourceBundle extends ResourceBundle {

  private GrouperUiTextConfig grouperUiTextConfig = null;
  
  /**
   * 
   */
  public GrouperNavResourceBundle(Locale locale) {
    this.grouperUiTextConfig = GrouperUiTextConfig.retrieveText(locale); 
  }

  /**
   * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
   */
  @Override
  protected Object handleGetObject(String key) {
    return this.grouperUiTextConfig.propertyValueString(key);
  }

  /**
   * @see java.util.ResourceBundle#getKeys()
   */
  @Override
  public Enumeration<String> getKeys() {
    return Collections.enumeration(this.grouperUiTextConfig.propertyNames());
  }

}
