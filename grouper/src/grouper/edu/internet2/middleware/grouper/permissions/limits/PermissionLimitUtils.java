/**
 * @author mchyzer
 * $Id: RuleUtils.java 7037 2010-11-11 08:08:59Z mchyzer $
 */
package edu.internet2.middleware.grouper.permissions.limits;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.util.ExpirableCache;


/**
 *
 */
public class PermissionLimitUtils {

  /**
   * return the limit attribute def name, assign this to an object to attach a rule.
   * this throws exception if cant find
   * @return the attribute def name
   */
  public static AttributeDefName limitAttributeDefName() {
    return AttributeDefNameFinder.findByName(attributeLimitStemName() + ":rule", true);
  }
  
  /**
   * regex limit
   * @return the attribute def name
   */
  public static AttributeDefName limitElAttributeDefName() {
    return AttributeDefNameFinder.findByName(limitElName(), true);
  }

  /**
   * return the limit type attribute def
   * this throws exception if cant find
   * @return the attribute def
   */
  public static AttributeDef limitAttributeDef() {
    return AttributeDefFinder.findByName(attributeLimitStemName() + ":limitsDef", true);
  }

  /**
   * return the stem name where the limit attributes go, without colon on end
   * @return stem name
   */
  public static String attributeLimitStemName() {
    String rootStemName = GrouperCheckConfig.attributeRootStemName();
    
    //namespace this separate from other builtins
    rootStemName += ":permissionLimits";
    return rootStemName;
  }

  /**
   * 
   */
  public static final String LIMIT_EL = "limitExpression";

  /**
   * limit el name
   */
  private static String limitElName = null;

  /**
   * limit el name
   * @return name
   */
  public static String limitElName() {
    if (limitElName == null) {
      limitElName = PermissionLimitUtils.attributeLimitStemName() + ":" + LIMIT_EL;
    }
    return limitElName;
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(PermissionLimitUtils.class);
  
  /**
   * add standard limit variables
   * @param limitEnvVarsObject
   */
  public static void addStandardLimitVariablesIfNotExist(Map<String, Object> limitEnvVarsObject) {
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(System.currentTimeMillis());
    
    //sunday is one
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    
    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
    int minuteOfHour = calendar.get(Calendar.MINUTE);
    int minuteOfDay = (hourOfDay * 60) + minuteOfHour;
    
    //january is 0
    int monthOfYear = calendar.get(Calendar.MONTH);
    
    if (!limitEnvVarsObject.containsKey("dayOfWeek")) {
      limitEnvVarsObject.put("dayOfWeek", dayOfWeek);
    }
    if (!limitEnvVarsObject.containsKey("hourOfDay")) {
      limitEnvVarsObject.put("hourOfDay", hourOfDay);
    }
    if (!limitEnvVarsObject.containsKey("minuteOfHour")) {
      limitEnvVarsObject.put("minuteOfHour", minuteOfHour);
    }
    if (!limitEnvVarsObject.containsKey("minuteOfDay")) {
      limitEnvVarsObject.put("minuteOfDay", minuteOfDay);
    }
    if (!limitEnvVarsObject.containsKey("monthOfYear")) {
      limitEnvVarsObject.put("monthOfYear", monthOfYear);
    }
    
  }
  
  /** limit logic map */
  static ExpirableCache<Boolean, Map<String, Class<PermissionLimitInterface>>> limitLogicMap = new ExpirableCache<Boolean, Map<String, Class<PermissionLimitInterface>>>(5);

  /** 
   * pattern to parse the limit configs
   * grouper.permissions.limits.logic.someName.limitName
   */
  private static Pattern limitNamePattern = Pattern.compile("^grouper\\.permissions\\.limits\\.logic\\.([^.]+)\\.limitName$");
  
  
  /**
   * get the client connection source config beans based on connection id
   * @param limitName name of the attribute def name
   * @return an instance of the interface
   */
  public static PermissionLimitInterface logicInstance(String limitName) {
    
    Map<String, Class<PermissionLimitInterface>> limitMap = limitLogicMap.get(Boolean.TRUE);

    if (limitMap == null) {
      
      synchronized (PermissionLimitUtils.class) {
        
        limitMap = limitLogicMap.get(Boolean.TRUE);
        
        if (limitMap == null) {
          limitMap = new HashMap<String, Class<PermissionLimitInterface>>();
          //lets add standard entries, do this first so they can be overridden
          limitMap.put(PermissionLimitUtils.limitElName(), (Class<PermissionLimitInterface>)(Object)PermissionLimitElLogic.class);
          
          Matcher matcher = null;
          
          //lets get the sources
          for (String sourcePropertyName : GrouperConfig.getPropertyNames()) {
            
            matcher = limitNamePattern.matcher(sourcePropertyName);
            if (matcher.matches()) {
              String limitKey = matcher.group(1);
              String className = GrouperConfig.getProperty("grouper.permissions.limits.logic." + limitKey + ".logicClass");
              String limitAttributeName = GrouperConfig.getProperty(sourcePropertyName);
              
              try {
                Class<PermissionLimitInterface> theClass = GrouperUtil.forName(className);
                
                limitMap.put(limitAttributeName, theClass);
                
              } catch (RuntimeException runtimeException) {
                LOG.error("Error with classname for limit, maybe class not on classpath or it is misconfigured in grouper.properties: " + className, runtimeException);
                //we will need to continue so one problem doesnt ruin it for all
                continue;
              }
            }
          }
          limitLogicMap.put(Boolean.TRUE, limitMap);
          
        }
      }
    }
    Class<PermissionLimitInterface> theClass = limitMap.get(limitName);
    
    if (theClass == null) {
      throw new RuntimeException("Cant find configuration for limit: " + limitName + " in grouper.properties or built-ins");
    }
    
    return GrouperUtil.newInstance(theClass);
    
  }

  /** limit logic map */
  static ExpirableCache<Boolean, Set<Class<?>>> limitElClassesMap = new ExpirableCache<Boolean, Set<Class<?>>>(5);

  /**
   * custom el instances to add to the variable map for limit EL
   * @param limitName 
   * @return the map
   */
  public static Map<String, Object> limitElClasses() {
    
    //grouper.permissions.limits.el.classes
    
    Set<Class<?>> limitElClasses = limitElClassesMap.get(Boolean.TRUE);

    if (limitElClasses == null) {
      
      synchronized (PermissionLimitUtils.class) {
        
        limitElClasses = limitElClassesMap.get(Boolean.TRUE);
        
        if (limitElClasses == null) {

          limitElClasses = new HashSet<Class<?>>();
          
          //middleware.grouper.rules.MyRuleUtils
          String customElClasses = GrouperConfig.getProperty("grouper.permissions.limits.el.classes");

          if (!StringUtils.isBlank(customElClasses)) {
            String[] customElClassesArray = GrouperUtil.splitTrim(customElClasses, ",");
            for (String customElClass : customElClassesArray) {
              Class<?> customClassClass = GrouperUtil.forName(customElClass);
              limitElClasses.add(customClassClass);
            }
          }

          
          //lets add standard entries, do this first so they can be overridden
          limitElClassesMap.put(Boolean.TRUE, limitElClasses);
                    
        }
      }
    }
    
    //get new instances each time
    Map<String,Object> result = new HashMap<String, Object>();
    for (Class<?> customClassClass : limitElClasses) {
      String simpleName = StringUtils.uncapitalize(customClassClass.getSimpleName());
      result.put(simpleName, GrouperUtil.newInstance(customClassClass));
      
    }

    return result;
    
  }
  
}
