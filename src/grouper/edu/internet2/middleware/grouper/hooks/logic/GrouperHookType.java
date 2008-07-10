/*
 * @author mchyzer
 * $Id: GrouperHookType.java,v 1.5 2008-07-10 00:46:53 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.CompositeHooks;
import edu.internet2.middleware.grouper.hooks.FieldHooks;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks;
import edu.internet2.middleware.grouper.hooks.GrouperSessionHooks;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.MemberHooks;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.StemHooks;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * type of hook, and manages the classes and instances
 */
public enum GrouperHookType {

  /**
   * group hooks
   */
  GROUP("hooks.group.class", GroupHooks.class),
  
  /**
   * group hooks
   */
  LIFECYCLE("hooks.lifecycle.class", LifecycleHooks.class),
  
  /**
   * stem hooks
   */
  STEM("hooks.stem.class", StemHooks.class),
  
  /**
   * member hooks
   */
  MEMBER("hooks.member.class", MemberHooks.class),
  
  /**
   * composite hooks
   */
  COMPOSITE("hooks.composite.class", CompositeHooks.class),
  
  /**
   * field hooks
   */
  FIELD("hooks.field.class", FieldHooks.class),
  
  /**
   * grouper session hooks
   */
  GROUPER_SESSION("hooks.grouperSession.class", GrouperSessionHooks.class),
  
  /**
   * group type hooks
   */
  GROUP_TYPE("hooks.groupType.class", GroupTypeHooks.class),
  
  /**
   * stem hooks
   */
  GROUP_TYPE_TUPLE("hooks.groupTypeTuple.class", GroupTypeTupleHooks.class),
  
  /**
   * membership hooks
   */
  MEMBERSHIP("hooks.membership.class", MembershipHooks.class);

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
  public List<Object> hooksInstances() {
    return (List<Object>)hooksInstances(this.getPropertyFileKey(), this.getBaseClass());
  }
  
  /** keep track of all the types and their configured classes.  key is the grouper.properties
   * property file key, class is the configured class (or Object.class 
   * as placeholder if none configured).  Note, never purge this since hooks could be
   * added manually */
  private static Map<String, List<Class<?>>> hookTypes = new HashMap<String, List<Class<?>>>();
  
  /** for testing, you can override a hook (dont forget to remove later) */
  private static Map<String, List<Class<?>>> hookTypesOverride = new HashMap<String, List<Class<?>>>();

  /**
   * for testing, you can override a hook (dont forget to remove later).  this temporarily hides
   * existing hooks
   * @param propertyFileKey
   * @param hookClass
   */
  public static void addHookOverride(String propertyFileKey, Class<?> hookClass) {
    addHookOverride(propertyFileKey, hookClass == null ? null : (List<Class<?>>)(Object)GrouperUtil.toList(hookClass));
  }
  
  /**
   * for testing, you can override a hook (dont forget to remove later).  this temporarily hides
   * existing hooks
   * @param propertyFileKey
   * @param hookClasses
   */
  public static void addHookOverride(String propertyFileKey, List<Class<?>> hookClasses) {
    if (hookClasses == null || hookClasses.size() == 0) {
      hookTypesOverride.remove(propertyFileKey);
    } else {
      hookTypesOverride.put(propertyFileKey, Collections.unmodifiableList(hookClasses));
    }
  }
  
  /** empty list */
  private static final List<Class<?>> EMPTY_LIST = 
    Collections.unmodifiableList(new ArrayList<Class<?>>());
  
  /**
   * get the configured group hooks
   * @param propertyFileKey 
   * @return the groups hooks class
   */
  private static List<Class<?>> hooksClasses(String propertyFileKey) {
    
    //see if override
    List<Class<?>> hooksClasses = hookTypesOverride.get(propertyFileKey);
    
    if (hooksClasses != null) {
      return hooksClasses;
    }
    
    hooksClasses = retrieveHooksFromConfig(propertyFileKey);
    
    //check reference, see if same
    if (EMPTY_LIST == hooksClasses) {
      return null;
    }
    return hooksClasses;
  }

  /**
   * init hooks classes if not initted yet, return the list of hooks
   * @param propertyFileKey
   * @return the list of hooks
   */
  private static List<Class<?>> retrieveHooksFromConfig(String propertyFileKey) {
    List<Class<?>> hooksClasses = hookTypes.get(propertyFileKey);
    if (hooksClasses == null) {
      String hooksClassString = GrouperConfig.getProperty(propertyFileKey);
      if (StringUtils.isBlank(hooksClassString)) {
        hooksClasses = EMPTY_LIST;
      } else {
        String[] hooksClassesArray = GrouperUtil.splitTrim(hooksClassString, ",");
        hooksClasses = new ArrayList<Class<?>>();
        //loop through each hooks class
        for (String hooksClass : hooksClassesArray) {
          hooksClasses.add(GrouperUtil.forName(hooksClass));
        }
        hooksClasses = Collections.unmodifiableList(hooksClasses);
      }
      hookTypes.put(propertyFileKey, hooksClasses);
    }
    return hooksClasses;
  }
  
  /**
   * add a hook to the list of configured hooks for this type
   * note if the class already exists it will not be added again.
   * This method is available publicly through {@link GrouperHooksUtils}
   * method addHookManual
   * @param propertyFileKey
   * @param hooksClass
   */
  static void addHookManual(String propertyFileKey, Class<?> hooksClass) {
    
    List<Class<?>> hooksClasses = retrieveHooksFromConfig(propertyFileKey);
    
    if (hooksClasses == EMPTY_LIST) {

      hooksClasses = new ArrayList<Class<?>>();
      
    } else {

      //do another since was unmodifiable
      hooksClasses = new ArrayList<Class<?>>(hooksClasses);
    }
    if (!hooksClasses.contains(hooksClass)) {
      hooksClasses.add(hooksClass);
    }
    hookTypes.put(propertyFileKey, Collections.unmodifiableList(hooksClasses));
    
  }
  
  /**
   * get an instance of group hooks
   * @param propertyFileKey key in property file of the hooks config
   * @param baseClass that the class must extend
   * @param <T> template type
   * @return the instances or empty list if none configured.  Dont edit this list!
   */
  private static <T> List<T> hooksInstances(String propertyFileKey, Class<T> baseClass) {
    
    //dont step on toes here, but if the hooks havent been hooked yet, do that
    GrouperHooksUtils.fireHooksInitHooksIfNotFiredAlready();
    
    List<Class<?>> theHooksClasses = hooksClasses(propertyFileKey);
    if (theHooksClasses == null) {
      return null;
    }
    List<T> results = new ArrayList<T>();
    for (Class<?> theHooksClass : theHooksClasses) {

      //we have a class, make sure it is the right one
      if (!baseClass.isAssignableFrom(theHooksClass)) {
        throw new RuntimeException("Class configured in grouper config: '" + propertyFileKey 
            + "' does not extend " + baseClass.getName());
      }
      results.add((T)GrouperUtil.newInstance(theHooksClass));
    }
    
    return results;
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
