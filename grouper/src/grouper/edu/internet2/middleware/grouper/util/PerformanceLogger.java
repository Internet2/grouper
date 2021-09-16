package edu.internet2.middleware.grouper.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

public class PerformanceLogger {

  public PerformanceLogger() {
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(PerformanceLogger.class);

  /**
   * performance timing of a group create
   */
  private static Map<String, ThreadLocal<Map<String, Object>>> performanceTiming = Collections.synchronizedMap(new HashMap<String, ThreadLocal<Map<String, Object>>>());
    
  /**
   * get performance timing map
   * @param label
   * @param createIfNotThere
   * @return the map
   */
  private static Map<String, Object> performanceTimingMap(String label, boolean createIfNotThere, boolean multiThreaded) {
    ThreadLocal<Map<String, Object>> threadLocal = performanceTimingThreadLocal(label);
    Map<String, Object> result = threadLocal.get();
    if (result == null && createIfNotThere) {
      result = new LinkedHashMap<String, Object>();
      if (multiThreaded) {
        result = Collections.synchronizedMap(result);
      }
      threadLocal.set(result);
    }
    return result;
  }
  
  /**
   * get a reference to this logger so the log config happens in one place
   * @return the logger
   */
  public static Log performanceLog() {
    return LOG;
  }

  /**
   * add a performance timer
   * @param label in config and refer to this
   * @param multiThreaded
   */
  public static void performanceTimingStart(String label, boolean multiThreaded) {
    if (performanceTimingEnabled(label)) {
      performanceTimingMap(label, true, multiThreaded);
      performanceTimingData(label, "performanceLogFor", label);
      performanceTimingData(label, "startNanos", System.nanoTime());
    } else {
      performanceTimingDelete(label);
    }
  }

  /**
   * thread local for this label
   * @param label
   * @return the threadlocal
   */
  private static ThreadLocal<Map<String, Object>> performanceTimingThreadLocal(String label) {
    ThreadLocal<Map<String, Object>> threadLocal = performanceTiming.get(label);
    if (threadLocal == null) {
      synchronized (PerformanceLogger.class) {
        threadLocal = performanceTiming.get(label);
        if (threadLocal == null) {
          threadLocal = new InheritableThreadLocal<Map<String, Object>>();
          performanceTiming.put(label, threadLocal);
        }        
      }
    }
    return threadLocal;
  }
  
  /**
   * add a performance timer
   * @param label in config and refer to this
   */
  public static void performanceTimingDelete(String label) {
    ThreadLocal<Map<String, Object>> threadLocal = performanceTimingThreadLocal(label);
    threadLocal.remove();
  }

  /**
   * add a performance gate.  This measures from start until now, not a duration
   * @param key something that will end up having ElapsedMs on end
   */
  public static void performanceTimingGate(String label, String key) {
    try {
      Map<String, Object> performanceMap = performanceTimingMap(label, false, false);
      if (performanceMap == null) {
        return;
      }
      Long startNanos = (Long)performanceMap.get("startNanos");
      if (startNanos == null) {
        throw new RuntimeException("Performance timeer is not started!");
      }
      String newKey = key + "_elapsedMs";
      String keyCount = key + "_count";
      
      Long originalMillis = (Long)performanceMap.get(newKey);
      Integer count = (Integer)performanceMap.get(keyCount);
      
      if (originalMillis != null) {
        if (count == null) {
          count = 1;
        }
        count++;
        performanceMap.put(keyCount, count);
        // this is just as something passes, so we cant measure duration...  just get a count and use the first one
        return;
        
      }
      originalMillis = (System.nanoTime()-startNanos)/1000000;
      
      performanceMap.put(newKey, originalMillis);
    } catch (Exception e) {
      LOG.error("Error with " + key, e);
    }
      
  }
  
  /**
   * add a performance gate for all performance timers, for a duration from start to finish.  Cannot overlap.  Will count all and keep a count if more than 1
   * @param key something that will end up having _durationMs on end
   * @param durationNanos
   */
  public static void performanceTimingAllDuration(String key, long durationNanos) {
    
    try {
      for (String label : performanceTiming.keySet()) {
        
        performanceTimingDuration(label, key, durationNanos);
        
      }
    } catch (Exception e) {
      LOG.error("Error with " + key, e);
    }
  }

  /**
   * use this for performance log label for sql queries
   */
  public static final String PERFORMANCE_LOG_LABEL_SQL = "sqlQueries";

  
  /**
   * add a performance gate for a duration from start to finish.  Cannot overlap.  Will count all and keep a count if more than 1
   * @param durationNanos something that will end up having ElapsedMs on end
   */
  public static void performanceTimingDuration(String label, String key, long durationNanos) {
    try {
      Map<String, Object> performanceMap = performanceTimingMap(label, false, false);
      if (performanceMap == null) {
        return;
      }
      
      String durationKey = key + "_durationNanos";
      String keyCount = key + "_count";
      
      Long currentDuration = (Long)performanceMap.get(durationKey);
      Integer count = (Integer)performanceMap.get(keyCount);
  
      if (count != null) {
        count++;
        performanceMap.put(keyCount, count);
      } else if (currentDuration != null) {
        count = 2;
        performanceMap.put(keyCount, count);
      }
  
      if (currentDuration != null) {
        durationNanos += currentDuration;
      }
      
      performanceMap.put(durationKey, durationNanos);
    } catch (Exception e) {
      LOG.error("Error with " + key, e);
    }
      
  }
  
  /**
   * add a performance gate
   * @param key
   * @param value
   */
  public static void performanceTimingData(String label, String key, Object value) {
    try {
      Map<String, Object> performanceMap = performanceTimingMap(label, false, false);
      if (performanceMap == null) {
        return;
      }
      performanceMap.put(key, value);
    } catch (Exception e) {
      LOG.error("Error with " + label + ", " +  key, e);
    }
  }

  /**
   * see if enabled
   * @param label
   * @return ture if enabled
   */
  public static boolean performanceTimingEnabled(String label) {
    boolean performanceLog = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.log.performance.info.on." + label, false);
    return performanceLog;
  }
  
  /**
   * add a performance gate
   * @param key
   * @param value
   */
  public static void performanceTimingDataRemoveKey(String label, String key) {
    try {
      Map<String, Object> performanceMap = performanceTimingMap(label, false, false);
      if (performanceMap == null) {
        return;
      }
      performanceMap.remove(key);
    } catch (Exception e) {
      LOG.error("Error with " + label + ", " + key, e);
    }
  }
  
  /**
   * performance string
   * @param label
   */
  public static String performanceTimingDataResult(String label) {
    try {
      Map<String, Object> performanceMap = performanceTimingMap(label, false, false);
      if (performanceMap == null) {
        return null;
      }
      PerformanceLogger.performanceTimingGate(label, "took");
      performanceTimingDataRemoveKey(label, "startNanos");
      
      // take out nanos and put in millis
      Map<String, Object> performanceMapNew = new LinkedHashMap<String, Object>();
      for (String key : performanceMap.keySet()) {
        Object value = performanceMap.get(key);
        if (value instanceof Long && key.endsWith("_durationNanos")) {
          Long valueLong = (Long)value;
          key = GrouperUtil.prefixOrSuffix(key, "_durationNanos", true) + "_durationMs";
          valueLong /= 1000000;
          value = valueLong;
        }
        performanceMapNew.put(key, value);
      }
      
      String result = GrouperUtil.mapToString(performanceMapNew);
      PerformanceLogger.performanceTimingDelete(label);
      return result;
    } catch (Exception e) {
      LOG.error("Error with " + label, e);
      return null;
    }

  }


  
}
