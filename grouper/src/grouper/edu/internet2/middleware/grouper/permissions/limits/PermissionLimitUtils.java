/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.TreeSet;
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
import edu.internet2.middleware.grouper.permissions.limits.impl.PermissionLimitAmountLessThan;
import edu.internet2.middleware.grouper.permissions.limits.impl.PermissionLimitAmountLessThanEquals;
import edu.internet2.middleware.grouper.permissions.limits.impl.PermissionLimitElLogic;
import edu.internet2.middleware.grouper.permissions.limits.impl.PermissionLimitIpOnNetworkRealm;
import edu.internet2.middleware.grouper.permissions.limits.impl.PermissionLimitIpOnNetworks;
import edu.internet2.middleware.grouper.permissions.limits.impl.PermissionLimitLabelsContain;
import edu.internet2.middleware.grouper.permissions.limits.impl.PermissionLimitWeekday9to5Logic;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.util.ExpirableCache;


/**
 * utils for permission limits
 */
public class PermissionLimitUtils {

  /**
   * name of variable in the env map
   */
  public static final String MONTH_OF_YEAR = "monthOfYear";

  /**
   * name of variable in the env map
   */
  private static final String MINUTE_OF_DAY = "minuteOfDay";

  /**
   * name of variable in the env map
   */
  public static final String MINUTE_OF_HOUR = "minuteOfHour";

  /**
   * name of variable in the env map
   */
  public static final String HOUR_OF_DAY = "hourOfDay";

  /**
   * name of variable in the env map
   */
  public static final String DAY_OF_WEEK = "dayOfWeek";

  /**
   * name of variable in the env map
   */
  public static final String CALENDAR = "calendar";

  /** limits def extension */
  public static final String LIMIT_DEF = "limitsDef";
  
  /**
   * return the limit type attribute def
   * this throws exception if cant find
   * @return the attribute def
   */
  public static AttributeDef limitAttributeDef() {
    return AttributeDefFinder.findByName(attributeLimitStemName() + ":" + LIMIT_DEF, true);
  }

  /** limits def extension */
  public static final String LIMIT_DEF_INT = "limitsDefInt";
  
  /**
   * return the limit type attribute def
   * this throws exception if cant find
   * @return the attribute def
   */
  public static AttributeDef limitAttributeDefInt() {
    return AttributeDefFinder.findByName(attributeLimitStemName() + ":" + LIMIT_DEF_INT, true);
  }

  /** limits def extension */
  public static final String LIMIT_DEF_MARKER = "limitsDefMarker";
  
  /**
   * return the limit type attribute def
   * this throws exception if cant find
   * @return the attribute def
   */
  public static AttributeDef limitAttributeDefMarker() {
    return AttributeDefFinder.findByName(attributeLimitStemName() + ":" + LIMIT_DEF_MARKER, true);
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

  /**
   * regex limit
   * @return the attribute def name
   */
  public static AttributeDefName limitElAttributeDefName() {
    return AttributeDefNameFinder.findByName(limitElName(), true);
  }

  /**
   * 
   */
  public static final String LIMIT_WEEKDAY_9_TO_5 = "limitWeekday9to5";

  /**
   * limit el name
   */
  private static String limitWeekday9to5Name = null;

  /**
   * limit el name
   * @return name
   */
  public static String limitWeekday9to5Name() {
    if (limitWeekday9to5Name == null) {
      limitWeekday9to5Name = PermissionLimitUtils.attributeLimitStemName() + ":" + LIMIT_WEEKDAY_9_TO_5;
    }
    return limitWeekday9to5Name;
  }

  /**
   * weekday 9 to 5
   * @return the attribute def name
   */
  public static AttributeDefName limitWeekday9to5AttributeDefName() {
    return AttributeDefNameFinder.findByName(limitWeekday9to5Name(), true);
  }

  /**
   * 
   */
  public static final String LIMIT_AMOUNT_LESS_THAN = "limitAmountLessThan";

  /**
   * limit amount less than
   */
  private static String limitAmountLessThanName = null;

  /**
   * limit amount less than
   * @return name
   */
  public static String limitAmountLessThanName() {
    if (limitAmountLessThanName == null) {
      limitAmountLessThanName = PermissionLimitUtils.attributeLimitStemName() + ":" + LIMIT_AMOUNT_LESS_THAN;
    }
    return limitAmountLessThanName;
  }

  /**
   * amount less than
   * @return the attribute def name
   */
  public static AttributeDefName limitAmountLessThanAttributeDefName() {
    return AttributeDefNameFinder.findByName(limitAmountLessThanName(), true);
  }

  /**
   * 
   */
  public static final String LIMIT_AMOUNT_LESS_THAN_OR_EQUAL = "limitAmountLessThanOrEqual";

  /**
   * limit amount less than
   */
  private static String limitAmountLessThanOrEqualName = null;

  /**
   * limit amount less than or equal
   * @return name
   */
  public static String limitAmountLessThanOrEqualName() {
    if (limitAmountLessThanOrEqualName == null) {
      limitAmountLessThanOrEqualName = PermissionLimitUtils.attributeLimitStemName() + ":" + LIMIT_AMOUNT_LESS_THAN_OR_EQUAL;
    }
    return limitAmountLessThanOrEqualName;
  }

  /**
   * amount less than or equal
   * @return the attribute def name
   */
  public static AttributeDefName limitAmountLessThanOrEqualAttributeDefName() {
    return AttributeDefNameFinder.findByName(limitAmountLessThanOrEqualName(), true);
  }

  /**
   * 
   */
  public static final String LIMIT_IP_ON_NETWORKS = "limitIpOnNetworks";

  /**
   * limit ip on networks
   */
  private static String limitIpOnNetworksName = null;

  /**
   * limit ip on networks
   * @return name
   */
  public static String limitIpOnNetworksName() {
    if (limitIpOnNetworksName == null) {
      limitIpOnNetworksName = PermissionLimitUtils.attributeLimitStemName() + ":" + LIMIT_IP_ON_NETWORKS;
    }
    return limitIpOnNetworksName;
  }

  /**
   * amount less than or equal
   * @return the attribute def name
   */
  public static AttributeDefName limitIpOnNetworksAttributeDefName() {
    return AttributeDefNameFinder.findByName(limitIpOnNetworksName(), true);
  }

  /**
   * 
   */
  public static final String LIMIT_IP_ON_NETWORK_REALM = "limitIpOnNetworkRealm";

  /**
   * limit ip on network realm
   */
  private static String limitIpOnNetworkRealmName = null;

  /**
   * limit ip on network realm
   * @return name
   */
  public static String limitIpOnNetworkRealmName() {
    if (limitIpOnNetworkRealmName == null) {
      limitIpOnNetworkRealmName = PermissionLimitUtils.attributeLimitStemName() + ":" + LIMIT_IP_ON_NETWORK_REALM;
    }
    return limitIpOnNetworkRealmName;
  }

  /**
   * amount less than or equal
   * @return the attribute def name
   */
  public static AttributeDefName limitIpOnNetworkRealmAttributeDefName() {
    return AttributeDefNameFinder.findByName(limitIpOnNetworkRealmName(), true);
  }

  /**
   * 
   */
  public static final String LIMIT_LABELS_CONTAIN = "limitLabelsContain";

  /**
   * limit labels contain a label
   */
  private static String limitLabelsContainName = null;

  /**
   * limit labels contain a label
   * @return name
   */
  public static String limitLabelsContainName() {
    if (limitLabelsContainName == null) {
      limitLabelsContainName = PermissionLimitUtils.attributeLimitStemName() + ":" + LIMIT_LABELS_CONTAIN;
    }
    return limitLabelsContainName;
  }

  /**
   * amount labels contain
   * @return the attribute def name
   */
  public static AttributeDefName limitLabelsContainAttributeDefName() {
    return AttributeDefNameFinder.findByName(limitLabelsContainName(), true);
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
    
    if (!limitEnvVarsObject.containsKey(CALENDAR)) {
      limitEnvVarsObject.put(CALENDAR, calendar);
    }
    if (!limitEnvVarsObject.containsKey(DAY_OF_WEEK)) {
      limitEnvVarsObject.put(DAY_OF_WEEK, dayOfWeek);
    }
    if (!limitEnvVarsObject.containsKey(HOUR_OF_DAY)) {
      limitEnvVarsObject.put(HOUR_OF_DAY, hourOfDay);
    }
    if (!limitEnvVarsObject.containsKey(MINUTE_OF_HOUR)) {
      limitEnvVarsObject.put(MINUTE_OF_HOUR, minuteOfHour);
    }
    if (!limitEnvVarsObject.containsKey(MINUTE_OF_DAY)) {
      limitEnvVarsObject.put(MINUTE_OF_DAY, minuteOfDay);
    }
    if (!limitEnvVarsObject.containsKey(MONTH_OF_YEAR)) {
      limitEnvVarsObject.put(MONTH_OF_YEAR, monthOfYear);
    }
    
  }
  
  /** limit logic map */
  static ExpirableCache<Boolean, Set<String>> limitRealms = new ExpirableCache<Boolean, Set<String>>(5);

  /** 
   * pattern to parse the limit realms
   * "grouper.permissions.limits.realm." + realmName
   */
  private static Pattern limitRealmPattern = Pattern.compile("^grouper\\.permissions\\.limits\\.realm\\.([^.]+)$");

  /**
   * get a logic instance based on attributeDefName of the limit
   * @return an instance of the interface
   */
  @SuppressWarnings("unchecked")
  public static Set<String> limitRealms() {
    
    Set<String> limitRealmsSet = limitRealms.get(Boolean.TRUE);

    if (limitRealmsSet == null) {
      
      synchronized (PermissionLimitUtils.class) {
        
        limitRealmsSet = limitRealms.get(Boolean.TRUE);
        
        if (limitRealmsSet == null) {
          limitRealmsSet = new TreeSet<String>();
          
          Matcher matcher = null;
          
          //lets get the sources
          for (String sourcePropertyName : GrouperConfig.retrieveConfig().propertyNames()) {
            
            matcher = limitRealmPattern.matcher(sourcePropertyName);
            if (matcher.matches()) {
              String limitRealm = matcher.group(1);
              limitRealmsSet.add(limitRealm);
            }
          }
          limitRealms.put(Boolean.TRUE, limitRealmsSet);
          
        }
      }
    }
    
    return limitRealmsSet;
    
  }


  
  /** limit logic map */
  static ExpirableCache<Boolean, Map<String, Class<PermissionLimitInterface>>> limitLogicMap = new ExpirableCache<Boolean, Map<String, Class<PermissionLimitInterface>>>(5);

  /**
   * for testing, clear this to put things in the api test config
   */
  public static void clearLimitLogicMap() {
    limitLogicMap.clear();
  }
  
  /** 
   * pattern to parse the limit configs
   * grouper.permissions.limits.logic.someName.limitName
   */
  private static Pattern limitNamePattern = Pattern.compile("^grouper\\.permissions\\.limits\\.logic\\.([^.]+)\\.limitName$");
  
  
  /**
   * get a logic instance based on attributeDefName of the limit
   * @param limitName name of the attribute def name
   * @return an instance of the interface
   */
  @SuppressWarnings("unchecked")
  public static PermissionLimitInterface logicInstance(String limitName) {
    
    Map<String, Class<PermissionLimitInterface>> limitMap = limitLogicMap.get(Boolean.TRUE);

    if (limitMap == null) {
      
      synchronized (PermissionLimitUtils.class) {
        
        limitMap = limitLogicMap.get(Boolean.TRUE);
        
        if (limitMap == null) {
          limitMap = new HashMap<String, Class<PermissionLimitInterface>>();
          //lets add standard entries, do this first so they can be overridden
          limitMap.put(PermissionLimitUtils.limitElName(), (Class<PermissionLimitInterface>)(Object)PermissionLimitElLogic.class);
          limitMap.put(PermissionLimitUtils.limitWeekday9to5Name(), (Class<PermissionLimitInterface>)(Object)PermissionLimitWeekday9to5Logic.class);
          limitMap.put(PermissionLimitUtils.limitAmountLessThanName(), (Class<PermissionLimitInterface>)(Object)PermissionLimitAmountLessThan.class);
          limitMap.put(PermissionLimitUtils.limitAmountLessThanOrEqualName(), (Class<PermissionLimitInterface>)(Object)PermissionLimitAmountLessThanEquals.class);
          limitMap.put(PermissionLimitUtils.limitIpOnNetworksName(), (Class<PermissionLimitInterface>)(Object)PermissionLimitIpOnNetworks.class);
          limitMap.put(PermissionLimitUtils.limitIpOnNetworkRealmName(), (Class<PermissionLimitInterface>)(Object)PermissionLimitIpOnNetworkRealm.class);
          limitMap.put(PermissionLimitUtils.limitLabelsContainName(), (Class<PermissionLimitInterface>)(Object)PermissionLimitLabelsContain.class);
          
          Matcher matcher = null;
          
          //lets get the sources
          for (String sourcePropertyName : GrouperConfig.retrieveConfig().propertyNames()) {
            
            matcher = limitNamePattern.matcher(sourcePropertyName);
            if (matcher.matches()) {
              String limitKey = matcher.group(1);
              String className = GrouperConfig.retrieveConfig().propertyValueString("grouper.permissions.limits.logic." + limitKey + ".logicClass");
              String limitAttributeName = GrouperConfig.retrieveConfig().propertyValueString(sourcePropertyName);
              
              try {
                Class<PermissionLimitInterface> theClass = GrouperUtil.forName(className);
                
                limitMap.put(limitAttributeName, theClass);
                
              } catch (RuntimeException runtimeException) {
                LOG.error("Error with classname for limit, maybe class not on classpath or it is misconfigured in grouper.properties: " + sourcePropertyName + ", classname: " + className, runtimeException);
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
          String customElClasses = GrouperConfig.retrieveConfig().propertyValueString("grouper.permissions.limits.el.classes");

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
