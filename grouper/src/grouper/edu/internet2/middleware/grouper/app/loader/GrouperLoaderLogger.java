/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.loader;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * logger for loader events
 */
public class GrouperLoaderLogger {

  /**
   * is debug enabled
   * @return true if enabled
   */
  public static boolean isLoggerEnabled() {
    return GrouperLoaderLog.isDebugEnabled();
  }
  
  /**
   * done with thread local map
   */
  public static void removeThreadLocalMaps() {
    if (!GrouperLoaderLog.isDebugEnabled()) {
      return;
    }
    
    threadLocalMap.remove();
    overallIdThreadLocal.remove();
    subjobIdThreadLocal.remove();
  }
  
  /**
   * inheritable threadlocal map for logging the maps
   * key is label
   * map will hold logging things
   */
  private static ThreadLocal<Map<String, Map<String, Object>>> threadLocalMap 
    = new ThreadLocal<Map<String, Map<String, Object>>>();

  /**
   * threadlocal map for logging the maps
   * key is label
   * map will hold logging things
   */
  private static ThreadLocal<String> overallIdThreadLocal = new ThreadLocal<String>();

  /**
   * sublog threadlocal map for logging the maps
   * key is label
   * map will hold logging things
   */
  private static ThreadLocal<String> subjobIdThreadLocal = new ThreadLocal<String>();

  /**
   * 
   * @param overallId
   */
  public static void assignOverallId(String overallId) {
    overallIdThreadLocal.set(overallId);
  }
  
  /**
   * 
   * @param subjobId
   */
  public static void assignSubjobId(String subjobId) {
    subjobIdThreadLocal.set(subjobId);
  }
  
  /**
   * 
   * @return the overall id
   */
  public static String retrieveOverallId() {
//    String overallId = (String)GrouperUtil.nonNull(retrieveUberMap().get("overallLog")).get("overallId");
//    
//    if (StringUtils.isBlank(overallId)) {
//      overallId = (String)GrouperUtil.nonNull(retrieveUberMap().get("subjobLog")).get("overallId");
//    }
    String overallId = overallIdThreadLocal.get();
    return overallId;
  }
    
  /**
   * 
   * @return the sub log id
   */
  public static String retrieveSubjobId() {
    //String subjobId = (String)GrouperUtil.nonNull(retrieveUberMap().get("sublogLog")).get("subjobId");
    String subjobId = subjobIdThreadLocal.get();
    return subjobId;
  }
    
  /**
   * retrieve and init uber map
   * @return uber map
   */
  private static Map<String, Map<String, Object>> retrieveUberMap() {
    Map<String, Map<String, Object>> uberMap = threadLocalMap.get();
    
    if (uberMap == null) {
      synchronized (GrouperLoaderLogger.class) {
        
        uberMap = threadLocalMap.get();
        
        if (uberMap == null) {
          uberMap = new HashMap<String, Map<String, Object>>();
          threadLocalMap.set(uberMap);
        }
      }
    }
    return uberMap;
  }
  
  /**
   * when loader thing starts
   * @param label 
   * @return false if already initted
   */
  public static boolean initializeThreadLocalMap(String label) {

    if (!GrouperLoaderLog.isDebugEnabled()) {
      return false;
    }

    Map<String, Object> map = retrieveUberMap().get(label);
    
    if (map != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(Thread.currentThread().getId() + ", initted map already: " + label);
      }
      return false;
    }
    
    map = new LinkedHashMap<String, Object>();
    map.put("logType", label);

    retrieveUberMap().put(label, map);
      
    if (StringUtils.equals("overallLog", label)) {
      String uniqueId = GrouperUtil.uniqueId();
      map.put("overallId", uniqueId);
      assignOverallId(uniqueId);
    } else {
      map.put("overallId", overallIdThreadLocal.get());
    }

    if (StringUtils.equals("subjobLog", label)) {
      String uniqueId = GrouperUtil.uniqueId();
      map.put("subjobId", uniqueId);
      assignSubjobId(uniqueId);
    } else {
      String subjobId = subjobIdThreadLocal.get();
      if (!StringUtils.isBlank(subjobId)) {
        map.put("subjobId", subjobId);
      }
    }
    
    map.put("elapsed", System.nanoTime());
    
    if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("daemon.log.logIdsEnabled", false)) {
      map.put("logId", GrouperUtil.uniqueId());
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(Thread.currentThread().getId() + ", init map: " + label 
          + ", logId: " + GrouperUtil.nonNull(retrieveUberMap().get(label)).get("logId")
          + ", overallId: " + map.get("overallId"));
    }
    
    return true;
  }

  /**
   * get a map or create it
   * @param label 
   * @return the map
   */
  private static Map<String, Object> retrieveMap(String label) {

    if (!GrouperLoaderLog.isDebugEnabled()) {
      return null;
    }

    return retrieveUberMap().get(label);
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderLogger.class);

  /**
   * add log entry
   * @param label the type of log, e.g. overallLog
   * @param key key of log
   * @param value value
   */
  public static void addLogEntry(String label, String key, Object value) {
    if (!GrouperLoaderLog.isDebugEnabled()) {
      return;
    }
    Map<String, Object> logMap = null;

    if (StringUtils.equals(label, "overallOrSubjobLog")) {
      logMap = retrieveMap("subjobLog");
      if (logMap == null) {
        logMap = retrieveMap("overallLog");
      }
    } else {
      logMap = retrieveMap(label);
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug(Thread.currentThread().getId() + ", logId: " + GrouperUtil.nonNull(retrieveUberMap().get(label)).get("logId") + ", " + key + " -> " + value + ", stack: " + GrouperUtil.stack());
    }
    
    if (logMap == null) {
      
      //if this is an overall map, then jsut ignore, the worker is updating, it will be there later
      if (StringUtils.equals("overallLog", label)) {
        return;
      }
      
      throw new RuntimeException("Cant find log map label '" + label + "', thread: " + Thread.currentThread().getId() + ", key: " + key + ", " + value);
    }
    
    //dont move the linked hash map key
    if (logMap.containsKey(key)) {
      Object currentValue = logMap.get(key);
      
      if (GrouperUtil.equals(currentValue, value)) {
        return;
      }
    }
    logMap.put(key, value);
  }
  
  /**
   * @param label is the type of logger
   * log something to the log file
   */
  public static void doTheLogging(String label) {
    if (!GrouperLoaderLog.isDebugEnabled()) {
      return;
    }

    //enable certain logs
    if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("daemon.log.logEnabled_" + label, true)) {
      return;
    }
    //init
    Map<String, Object> logMap = retrieveMap(label);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug(Thread.currentThread().getId() + ", do logging '" + label + "', logId: " + GrouperUtil.nonNull(retrieveUberMap().get(label)).get("logId"));
    }

    logMap.put("threadId", Thread.currentThread().getId());
    
    Long elapsedNanoStart = (Long)logMap.get("elapsed");
    if (elapsedNanoStart != null) {
      logMap.remove("elapsed");
      //overwrite what was already there
      logMap.put("elapsed", ((System.nanoTime() - elapsedNanoStart) / 1000000) + " ms");
    }
                
    String mapToString = GrouperUtil.mapToString(logMap);
    GrouperLoaderLog.logDebug(mapToString);
    logMap.remove("elapsed");

    skipLogging(label);
  }

  /**
   * @param label is the type of logger
   * log something to the log file
   */
  public static void skipLogging(String label) {
    if (!GrouperLoaderLog.isDebugEnabled()) {
      return;
    }

    //enable certain logs
    if (!GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("daemon.log.logEnabled_" + label, true)) {
      return;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(Thread.currentThread().getId() + ", remove logger '" + label + "', logId: " + GrouperUtil.nonNull(retrieveUberMap().get(label)).get("logId") + ", stack: " + GrouperUtil.stack());
    }
    
    retrieveUberMap().remove(label);
    
  }

}
