/**
 * Copyright 2014 Internet2
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
/*
 * @author mchyzer
 * $Id: GrouperHookType.java,v 1.9 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateCompiledDispatch;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.AttributeAssignHooks;
import edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks;
import edu.internet2.middleware.grouper.hooks.AttributeDefHooks;
import edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks;
import edu.internet2.middleware.grouper.hooks.AttributeHooks;
import edu.internet2.middleware.grouper.hooks.CompositeHooks;
import edu.internet2.middleware.grouper.hooks.ExternalSubjectHooks;
import edu.internet2.middleware.grouper.hooks.FieldHooks;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks;
import edu.internet2.middleware.grouper.hooks.GrouperSessionHooks;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.LoaderHooks;
import edu.internet2.middleware.grouper.hooks.MemberHooks;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.StemHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * type of hook, and manages the classes and instances
 */
public enum GrouperHookType implements GrouperHookTypeInterface {
  
  /**
   * external subject hooks
   */
  EXTERNAL_SUBJECT("hooks.externalSubject.class", ExternalSubjectHooks.class),
  
  /**
   * loader hooks
   */
  LOADER("hooks.loader.class", LoaderHooks.class),
  
  /**
   * group hooks
   */
  GROUP("hooks.group.class", GroupHooks.class),
  
  /**
   * attribute hooks
   */
  ATTRIBUTE("hooks.attribute.class", AttributeHooks.class),
  
  /**
   * attribute assign hooks
   */
  ATTRIBUTE_ASSIGN("hooks.attributeAssign.class", AttributeAssignHooks.class),
  
  /**
   * attribute assign value hooks
   */
  ATTRIBUTE_ASSIGN_VALUE("hooks.attributeAssignValue.class", AttributeAssignValueHooks.class),
  
  /**
   * attribute hooks
   */
  ATTRIBUTE_DEF("hooks.attributeDef.class", AttributeDefHooks.class),
  
  /**
   * attribute hooks
   */
  ATTRIBUTE_DEF_NAME("hooks.attributeDefName.class", AttributeDefNameHooks.class),
  
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
   * 
   */
  public static void clearHooks() {

    hookTypesOverride.clear();
    hookTypes.clear();
    hookTypeMap.clear();
    compiledHookCache.clear();

  }

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
   * @param grouperHookTypeInterface 
   * @param methodName e.g. groupPreUpdate
   * @param beanClass e.g. HooksGroupBean.class
   * @return the instance / methods or empty list if none configured
   */
  public static List<GrouperHookMethodAndObject> hooksInstances(GrouperHookTypeInterface grouperHookTypeInterface, String methodName, 
      Class<?> beanClass) {
    return (List<GrouperHookMethodAndObject>)hooksInstances(grouperHookTypeInterface, grouperHookTypeInterface.getPropertyFileKey(), 
        grouperHookTypeInterface.getBaseClass(), methodName, beanClass);
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
    
    //remove from method cache so it will be calculated again
    hookTypeMap.remove(propertyFileKey);
    
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
      String hooksClassString = GrouperConfig.retrieveConfig().propertyValueString(propertyFileKey);
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
    
    //remove from method cache so it will be calculated again
    hookTypeMap.remove(propertyFileKey);
    
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
   * this is a map of cached instances.  First the key of the map is the config key (e.g. hooks.member.class)
   * The value of that is a map of multikey to the list of objects (instances of the hook to call, and method object).
   * The multikey is the method name (e.g. groupPreUpdate) combined with the bean arg class (not HooksContext) e.g. 
   * HooksGroupBean.class.  This is a sub-map lookup which can easily be cleared when a new hook is added
   */
  private static Map<String, Map<MultiKey, List<GrouperHookMethodAndObject>>> hookTypeMap = 
      new HashMap<String, Map<MultiKey, List<GrouperHookMethodAndObject>>>();
  
  /**
   * get an instance of group hooks
   * @param propertyFileKey key in property file of the hooks config
   * @param baseClass that the class must extend
   * @param grouperHookTypeInterface 
   * @param methodName e.g. groupPreUpdate
   * @param beanClass e.g. HooksGroupBean.class
   * @return the instances or empty list if none configured.  Dont edit this list!
   */
  private static List<GrouperHookMethodAndObject> hooksInstances(GrouperHookTypeInterface grouperHookTypeInterface,
      String propertyFileKey, Class<?> baseClass,
      String methodName, Class<?> beanClass) {

    //dont step on toes here, but if the hooks havent been hooked yet, do that
    GrouperHooksUtils.fireHooksInitHooksIfNotFiredAlready();

    List<GrouperHookMethodAndObject> classpathResults = classpathHooksInstances(grouperHookTypeInterface,
        propertyFileKey, baseClass, methodName, beanClass);

    // GRP-7032: compiled-Java (DB-stored) hooks coexist with classpath/jar hooks.
    // These hot-reload, so they cannot live in the permanent hookTypeMap cache —
    // they are resolved through a short-TTL cache instead and merged in here.
    List<GrouperHookMethodAndObject> compiledResults = compiledHooksInstances(grouperHookTypeInterface,
        propertyFileKey, baseClass, methodName, beanClass);

    if (compiledResults == null || compiledResults.size() == 0) {
      return classpathResults;
    }
    if (classpathResults == null || classpathResults.size() == 0) {
      return compiledResults;
    }
    List<GrouperHookMethodAndObject> combined = new ArrayList<GrouperHookMethodAndObject>(classpathResults);
    combined.addAll(compiledResults);
    return combined;
  }

  /**
   * resolve the classpath/jar-deployed hooks for this type, method, and bean.
   * These never change at runtime, so the (method,instance) lists are cached
   * permanently in hookTypeMap.
   * @param grouperHookTypeInterface
   * @param propertyFileKey
   * @param baseClass
   * @param methodName
   * @param beanClass
   * @return the instances or empty list if none configured.  Dont edit this list!
   */
  private static List<GrouperHookMethodAndObject> classpathHooksInstances(GrouperHookTypeInterface grouperHookTypeInterface,
      String propertyFileKey, Class<?> baseClass,
      String methodName, Class<?> beanClass) {

    Map<MultiKey, List<GrouperHookMethodAndObject>> methodMap = hookTypeMap.get(propertyFileKey);

    //create if not there
    if (methodMap == null) {
      methodMap = new HashMap<MultiKey, List<GrouperHookMethodAndObject>>();
      hookTypeMap.put(propertyFileKey, methodMap);
    }

    //see if method is in there already
    MultiKey methodMapKey = new MultiKey(methodName, beanClass);
    List<GrouperHookMethodAndObject> results = methodMap.get(methodMapKey);

    //if there we are all good
    if (results != null) {
      return results;
    }

    List<Class<?>> theHooksClasses = hooksClasses(propertyFileKey);
    results = new ArrayList<GrouperHookMethodAndObject>();
    //set in cache
    methodMap.put(methodMapKey, results);

    if (theHooksClasses == null || theHooksClasses.size() == 0) {
      return results;
    }

    for (Class<?> theHooksClass : theHooksClasses) {

      //we have a class, make sure it is the right one
      if (!baseClass.isAssignableFrom(theHooksClass)) {
        throw new RuntimeException((theHooksClass == null ? null : theHooksClass.getName()) + " class configured in grouper config: '" + propertyFileKey
            + "' does not extend " + baseClass.getName());
      }

      //lets see if the method is there
      Method method = GrouperUtil.method(theHooksClass, methodName, new Class[]{HooksContext.class, beanClass},
          grouperHookTypeInterface.getBaseClass(), false, false, null, false);

      if (method != null) {
        results.add(new GrouperHookMethodAndObject(method, GrouperUtil.newInstance(theHooksClass)));
      }

    }

    return results;
  }

  /**
   * default TTL (seconds) for the compiled-hook instance cache
   */
  private static final int COMPILED_HOOK_CACHE_SECONDS_DEFAULT = 30;

  /**
   * per-domain (propertyFileKey) cache of resolved compiled-hook classes plus
   * lazily-built per-(method,beanClass) instance lists, with a creation
   * timestamp for TTL expiry. GRP-7032.
   */
  private static Map<String, CompiledHookCacheEntry> compiledHookCache =
      new ConcurrentHashMap<String, CompiledHookCacheEntry>();

  /**
   * one domain's compiled-hook cache entry — when it was built, the classes
   * resolved from the registry, and the (method,beanClass) -> instances built
   * from them so far.
   */
  private static class CompiledHookCacheEntry {
    /** when this entry was built (for TTL) */
    private long createdMillis;
    /** classes resolved from the gshTemplateConfigIds for this domain */
    private List<Class<?>> resolvedClasses;
    /** lazily-built (methodName,beanClass) -> (method,instance) lists */
    private Map<MultiKey, List<GrouperHookMethodAndObject>> methodMap =
        new HashMap<MultiKey, List<GrouperHookMethodAndObject>>();
  }

  /**
   * resolve the compiled-Java (DB-stored) hooks for this type, method, and bean.
   * Source comes from GSH template configs listed in
   * <code>hooks.&lt;domain&gt;.gshTemplateConfigIds</code>; the resolved classes
   * are cached per domain with a short TTL so the hot path neither re-hashes
   * source nor blocks, while still picking up edits (and JVM peers ride the
   * GrouperConfig cadence). Returns null/empty if none configured.
   * @param grouperHookTypeInterface
   * @param propertyFileKey the classpath key, e.g. hooks.group.class
   * @param baseClass the hook base class the compiled class must extend
   * @param methodName
   * @param beanClass
   * @return the instances, or null if no compiled hooks configured for this domain
   */
  private static List<GrouperHookMethodAndObject> compiledHooksInstances(GrouperHookTypeInterface grouperHookTypeInterface,
      String propertyFileKey, Class<?> baseClass, String methodName, Class<?> beanClass) {

    String gshTemplateConfigIdsKey = StringUtils.removeEnd(propertyFileKey, ".class") + ".gshTemplateConfigIds";
    String configIdsString = GrouperConfig.retrieveConfig().propertyValueString(gshTemplateConfigIdsKey);
    if (StringUtils.isBlank(configIdsString)) {
      return null;
    }

    long ttlMillis = 1000L * GrouperConfig.retrieveConfig().propertyValueInt(
        "hooks.compiledTemplate.cacheSeconds", COMPILED_HOOK_CACHE_SECONDS_DEFAULT);

    CompiledHookCacheEntry entry = compiledHookCache.get(propertyFileKey);
    if (entry == null || (System.currentTimeMillis() - entry.createdMillis) > ttlMillis) {
      entry = rebuildCompiledHookCacheEntry(configIdsString, baseClass);
      compiledHookCache.put(propertyFileKey, entry);
    }

    MultiKey methodMapKey = new MultiKey(methodName, beanClass);
    synchronized (entry) {
      List<GrouperHookMethodAndObject> results = entry.methodMap.get(methodMapKey);
      if (results != null) {
        return results;
      }
      results = new ArrayList<GrouperHookMethodAndObject>();
      for (Class<?> theHooksClass : entry.resolvedClasses) {
        Method method = GrouperUtil.method(theHooksClass, methodName, new Class[]{HooksContext.class, beanClass},
            grouperHookTypeInterface.getBaseClass(), false, false, null, false);
        if (method != null) {
          results.add(new GrouperHookMethodAndObject(method, GrouperUtil.newInstance(theHooksClass)));
        }
      }
      entry.methodMap.put(methodMapKey, results);
      return results;
    }
  }

  /**
   * Rebuild a domain's compiled-hook cache entry: resolve each configured GSH
   * template config id to its compiled class through the registry (which itself
   * recompiles only on source change). The asSubclass in resolveClass enforces
   * that the compiled class extends the hook base.
   * @param configIdsString comma-separated GSH template config ids
   * @param baseClass the hook base class
   * @return a fresh cache entry
   */
  private static CompiledHookCacheEntry rebuildCompiledHookCacheEntry(String configIdsString, Class<?> baseClass) {
    CompiledHookCacheEntry entry = new CompiledHookCacheEntry();
    entry.createdMillis = System.currentTimeMillis();
    entry.resolvedClasses = new ArrayList<Class<?>>();
    String[] configIds = GrouperUtil.splitTrim(configIdsString, ",");
    for (String configId : configIds) {
      GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(configId);
      gshTemplateConfig.populateConfiguration();
      Class<?> resolvedClass = GshTemplateCompiledDispatch.resolveClass(configId, gshTemplateConfig, baseClass);
      entry.resolvedClasses.add(resolvedClass);
    }
    return entry;
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
