/**
 * Copyright 2017 Internet2
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
package edu.internet2.middleware.grouper.instrumentation;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.audit.GrouperEngineIdentifier;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class InstrumentationThread {
  
  private static final Log LOG = GrouperUtil.getLog(InstrumentationThread.class);

  private static ExecutorService executorService = Executors.newSingleThreadExecutor();
  
  private static Map<String, InstrumentationDataCounts> instrumentationDataCounts = new HashMap<String, InstrumentationDataCounts>();

  /**
   * start thread
   * @param grouperEngineIdentifier 
   * @param customTypes
   */
  public static void startThread(final GrouperEngineIdentifier grouperEngineIdentifier, final Set<String> customTypes) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("instrumentation.thread.enabled", true)) {
      return;
    }
    
    executorService.execute(new Runnable() {
      public void run() {
        GrouperSession rootSession = GrouperSession.startRootSession(true);

        try {
          File instanceFile = getInstanceFile(grouperEngineIdentifier);
          
          if (instanceFile == null) {
            LOG.warn("Unable to save an instance id for this instance.");
            return;
          }
          
          String uuid;
          try {
            uuid = FileUtils.readFileToString(instanceFile, null).trim();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          
          AttributeAssign parentAssignment = InstrumentationDataUtils.grouperInstrumentationInstanceParentAttributeAssignment(grouperEngineIdentifier, uuid);

          long increment = GrouperConfig.retrieveConfig().propertyValueInt("instrumentation.updateIncrements", 3600) * 1000L;
          if (3600000 % increment != 0) {
            LOG.warn("instrumentation.updateIncrements must be divisible by 3600.  Using 3600 (1 hour) instead");
            increment = 3600000;
          }
          
          initCounts(customTypes);

          InstrumentationDataUtils.setCollectingStats(true);

          while (true) {
                        
            if (Thread.currentThread().isInterrupted()) {
              return;
            }
            
            try {
              Thread.sleep(GrouperConfig.retrieveConfig().propertyValueInt("instrumentation.updateGrouperIntervalInSeconds", 3600) * 1000L);
            } catch (InterruptedException e) {
              LOG.info("Received interrupt to shutdown instrumentation thread.");
              Thread.currentThread().interrupt();
            }
            
            long daemonStartTime = System.currentTimeMillis();
            
            Map<MultiKey, Long> allCounts = new HashMap<MultiKey, Long>();
            Set<Long> startTimes = new TreeSet<Long>();
            
            for (String key : instrumentationDataCounts.keySet()) {
              InstrumentationDataCounts counts = instrumentationDataCounts.get(key);
              List<Long> timestamps = counts.clearCounts();
              
              if (timestamps.size() > 0) {
                for (Long timestamp : timestamps) {
                  long startTime = (timestamp / increment) * increment;  // top of the increment (e.g. top of the hour)
                  if ((startTime + increment) > daemonStartTime) {
                    // add it back to be processed next time
                    counts.addCount(timestamp);
                  } else {
                    MultiKey multiKey = new MultiKey(key, startTime);
                    if (allCounts.get(multiKey) == null) {
                      allCounts.put(multiKey, 0L);
                    }
                    
                    allCounts.put(multiKey, allCounts.get(multiKey) + 1);
                    startTimes.add(startTime);
                  }
                }
              }      
            }
            
            //System.out.println(allCounts);
            
            for (long startTime : startTimes) {
              Map<String, Long> data = new LinkedHashMap<String, Long>();
              data.put("startTime", startTime);
              data.put("duration", increment);

              for (MultiKey multiKey : allCounts.keySet()) {
                String key = (String)multiKey.getKey(0);
                Long thisStartTime = (Long)multiKey.getKey(1);
                
                if (startTime == thisStartTime) {
                  data.put(key, allCounts.get(multiKey));
                }
              }
              
              String dataJson = GrouperUtil.jsonConvertTo(data, false);
              parentAssignment.getAttributeValueDelegate().addValue(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_COUNTS_ATTR, dataJson);
            }
            
            parentAssignment.getAttributeValueDelegate().assignValue(InstrumentationDataUtils.grouperInstrumentationDataStemName() + ":" + InstrumentationDataUtils.INSTRUMENTATION_DATA_INSTANCE_LAST_UPDATE_ATTR, "" + daemonStartTime);
            
            try {
              FileUtils.touch(instanceFile);
            } catch (IOException e) {
              LOG.warn("Non fatal error while touching file " + instanceFile.getAbsolutePath() + " for the purposes of making sure the file doesn't get cleaned up by the system.");
            }
          }
        } finally {
          GrouperSession.stopQuietly(rootSession);
        }
      }
    });
  }
  
  private static void initCounts(Set<String> customTypes) {
    for (InstrumentationDataBuiltinTypes type: InstrumentationDataBuiltinTypes.values()) {
      instrumentationDataCounts.put(type.name(), new InstrumentationDataCounts());
    }
    
    if (customTypes != null) {
      for (String customCount : customTypes) {
        instrumentationDataCounts.put(customCount, new InstrumentationDataCounts());
      }
    }
  }
  
  /**
   * stop thread
   */
  public static void shutdownThread() {
    executorService.shutdownNow();
  }
  
  private static File getInstanceFile(GrouperEngineIdentifier grouperEngineIdentifier) {
    if (grouperEngineIdentifier == null) {
      throw new RuntimeException("No grouper engine identifier.");
    }
    
    if (grouperEngineIdentifier.getGrouperEngine() == null) {
      throw new RuntimeException("Grouper engine is null");
    }
    
    String instrumentationFileName = grouperEngineIdentifier.getGrouperEngine() + "_instrumentation.dat";
    
    Logger rootLogger = Logger.getRootLogger();

    Set<File> parentFiles = new LinkedHashSet<File>();
    
    if (rootLogger != null) {
      Enumeration appenders = rootLogger.getAllAppenders();
      while (appenders.hasMoreElements()) {
        Appender appender = (Appender) appenders.nextElement();
        
        if (appender instanceof FileAppender) {
          String filename = ((FileAppender) appender).getFile();
          if (!GrouperUtil.isEmpty(filename)) {
            File file = new File(filename);
            if (file.getParentFile().exists()) {
              parentFiles.add(file.getParentFile());
            }
          }
        }
      }
    }
    
    if (parentFiles.size() == 0) {
      return null;
    }
        
    try {
      for (File parentFile : parentFiles) {
        File childFile = new File(parentFile, instrumentationFileName);
        if (childFile.exists()) {
          String uuid = FileUtils.readFileToString(childFile, null);
          if (!GrouperUtil.isBlank(uuid)) {
            return childFile;
          }
        }
      }
  
      File childFile = new File(parentFiles.iterator().next(), instrumentationFileName);
      String uuid = GrouperUuid.getUuid();
      FileUtils.writeStringToFile(childFile, uuid);
      
      return childFile;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @param type
   */
  public static void addCount(String type) {
    if (InstrumentationDataUtils.isCollectingStats()) {
      long timestamp = System.currentTimeMillis();
      
      if (instrumentationDataCounts.get(type) == null) {
        // hmm
        LOG.error("Unknown type: " + type);
        return;
      }
      
      instrumentationDataCounts.get(type).addCount(timestamp);
    }
  }
}
