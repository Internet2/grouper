package edu.internet2.middleware.grouper.ui.util;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

/**
 * do a resource bundle which consults the properties file before 
 * consulting the media.properties resource bundle
 * @author mchyzer
 *
 */
public class ChainedMediaPropertiesResourceBundle extends ResourceBundle {

  /**
   * constructor with the parent resource bundle
   * @param theParentBundle
   */
  public ChainedMediaPropertiesResourceBundle(ResourceBundle theParentBundle) {
    this.setParent(theParentBundle);
  }
  
  /**
   * @see ResourceBundle#getKeys()
   */
  @Override
  public Enumeration<String> getKeys() {
    Vector<String> keys = new Vector<String>();
    
    keys.addAll(GrouperUiConfig.retrieveConfig().propertyNames());
    keys.addAll(this.parent.keySet());
    
    return keys.elements();
    
  }

  /**
   * @see ResourceBundle#handleGetObject()
   */
  @Override
  protected Object handleGetObject(String key) {
    GrouperUiConfig uiConfig = GrouperUiConfig.retrieveConfig();
    if (uiConfig.containsKey(key)) {
      //do a default string since null means go up the chain
      return StringUtils.defaultString(uiConfig.propertyValueString(key));
    }
    return null;
  }

}
