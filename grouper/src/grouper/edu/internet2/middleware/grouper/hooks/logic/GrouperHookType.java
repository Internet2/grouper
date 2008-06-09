/*
 * @author mchyzer
 * $Id: GrouperHookType.java,v 1.1.2.1 2008-06-09 05:52:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * type of hook, and manages the classes and instances
 */
public enum GrouperHookType {

  /**
   * group hooks
   */
  GROUP("hooks.group.class", GroupHooks.class);
  
  /**
   * construct
   * @param thePropertyFileKey
   * @param theBaseClass
   */
  private GrouperHookType(String thePropertyFileKey, Class<?> theBaseClass) {
    this.propertyFileKey = thePropertyFileKey;
    this.baseClass = theBaseClass;
    
  }
  
  /** property file key for this hook class */
  private String propertyFileKey;

  /** base class for this hook class */
  private Class<?> baseClass;
  
  /**
   * get an instance for this group type
   * @return the instance or null if none configured
   */
  public Object hooksInstance() {
    return hooksInstance(this.getPropertyFileKey(), this.getBaseClass());
  }
  
  /** keep track of all the types and their configured classes.  key is the grouper.properties
   * property file key, class is the configured class (or Object.class 
   * as placeholder if none configured) */
  private static Map<String, Class<?>> hookTypes = new HashMap<String, Class<?>>();
  
  /** for testing, you can override a hook (dont forget to remove later) */
  private static Map<String, Class<?>> hookTypesOverride = new HashMap<String, Class<?>>();

  /**
   * for testing, you can override a hook (dont forget to remove later)
   * @param propertyFileKey
   * @param hookClass
   */
  public static void addHookOverride(String propertyFileKey, Class<?> hookClass) {
    if (hookClass == null) {
      hookTypesOverride.remove(propertyFileKey);
    } else {
      hookTypesOverride.put(propertyFileKey, hookClass);
    }
  }
  
  /**
   * get the configured group hooks
   * @param propertyFileKey 
   * @return the groups hooks class
   */
  private static Class<?> hooksClass(String propertyFileKey) {
    
    //see if override
    Class<?> hooksClass = hookTypesOverride.get(propertyFileKey);
    
    if (hooksClass != null) {
      return hooksClass;
    }
    
    hooksClass = hookTypes.get(propertyFileKey);
    if (hooksClass == null) {
      String hooksClassString = GrouperConfig.getProperty(propertyFileKey);
      if (StringUtils.isBlank(hooksClassString)) {
        hooksClass = Object.class;
      } else {
        hooksClass = GrouperUtil.forName(hooksClassString);
      }
      hookTypes.put(propertyFileKey, hooksClass);
    }
    if (Object.class.equals(hooksClass)) {
      return null;
    }
    return hooksClass;
  }

  /**
   * get an instance of group hooks
   * @param propertyFileKey key in property file of the hooks config
   * @param baseClass that the class must extend
   * @param <T> template type
   * @return the instance or null if none configured
   */
  public static <T> T hooksInstance(String propertyFileKey, Class<T> baseClass) {
    Class<?> theHooksClass = hooksClass(propertyFileKey);
    if (theHooksClass == null) {
      return null;
    }

    //we have a class, make sure it is the right one
    if (!baseClass.isAssignableFrom(theHooksClass)) {
      throw new RuntimeException("Class configured in grouper config: '" + propertyFileKey 
          + "' does not extend " + baseClass.getName());
    }
    
    return (T)GrouperUtil.newInstance(theHooksClass);
  }

  
  /**
   * property file key for this hook class
   * @return the propertyFileKey
   */
  public String getPropertyFileKey() {
    return this.propertyFileKey;
  }

  
  /**
   * base class for this hook class
   * @return the baseClass
   */
  public Class<?> getBaseClass() {
    return this.baseClass;
  }
  
}
