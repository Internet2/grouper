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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.audit.GrouperEngineIdentifier;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
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

        GrouperSession rootSession = null;
        long increment;
        AttributeAssign parentAssignment;
        File instanceFile;
        
        try {
          GrouperStartup.waitForGrouperStartup();
          
          rootSession = GrouperSession.startRootSession(true);
          
          instanceFile = getInstanceFile(grouperEngineIdentifier);
          
          if (instanceFile == null) {
            LOG.warn("Unable to save an instance id for this instance.");
            return;
          }
          
          String uuid;
          try {
            uuid = FileUtils.readFileToString(instanceFile, (String)null).trim();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          
          parentAssignment = InstrumentationDataUtils.grouperInstrumentationInstanceParentAttributeAssignment(grouperEngineIdentifier, uuid);

          increment = GrouperConfig.retrieveConfig().propertyValueInt("instrumentation.updateIncrements", 3600) * 1000L;
          if (3600000 % increment != 0) {
            LOG.warn("instrumentation.updateIncrements must be divisible by 3600.  Using 3600 (1 hour) instead");
            increment = 3600000;
          }
          
          initCounts(customTypes);

          InstrumentationDataUtils.setCollectingStats(true);
        } catch (RuntimeException re) {
          LOG.error("error in thread", re);
          throw re;
        } finally {
          GrouperSession.stopQuietly(rootSession);
        }
        

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
            
          try {
            rootSession = GrouperSession.startRootSession(true);

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
          } catch (RuntimeException re) {
            LOG.error("error in thread", re);
          } finally {
            GrouperSession.stopQuietly(rootSession);
          }
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

    String grouperEngineName = grouperEngineIdentifier.getGrouperEngine();
    if (grouperEngineName == null) {
      throw new RuntimeException("Grouper engine is null");
    }

    String instrumentationFileName = grouperEngineName + "_instrumentation.dat";
    File parentFile = null;

    String instrumentationDirectoryName = GrouperConfig.retrieveConfig().propertyValueString("instrumentation.instanceFile.directory");
    if (instrumentationDirectoryName != null) {
      /* If a specific writable directory is configured, read or create the uuid from there */
      parentFile = new File(instrumentationDirectoryName);
    } else {
      if (new File("/opt/grouper").exists()) {
        File grouperInstrumentationDirectory = new File("/opt/grouper/instrumentation");
        if (grouperInstrumentationDirectory.exists()) {
          parentFile = grouperInstrumentationDirectory;
        } else {
          if (grouperInstrumentationDirectory.mkdir()) {
            parentFile = grouperInstrumentationDirectory;
          } else {
            LOG.warn("Failed to create directory " + grouperInstrumentationDirectory.getAbsolutePath());
          }
        }
      }
    }

    if (parentFile == null) {
      LOG.warn("Could not determine directory to read or write instrumentation file; define config property instrumentation.instanceFile.directory");
      return null;
    }

    {
      File childFile = new File(parentFile, instrumentationFileName);
      if (childFile.exists()) {
        try {
          LOG.debug("Reading uuid from instrumentation file: " + childFile.getCanonicalPath());
          String uuid = FileUtils.readFileToString(childFile, (String)null);
          if (!GrouperUtil.isBlank(uuid)) {
            LOG.debug("Uuid for " + grouperEngineName + " is " + uuid);
            return childFile;
          } else {
            LOG.warn("Uuid is blank in instrumentation file " + childFile.getCanonicalPath() + ".");
          }
        } catch (IOException e) {
          LOG.error("Failed to read uuid from instrumentation file " + instrumentationFileName, e);
        }
      }
    }

    /* Couldn't find existing file, so create a new one */
    try {
      File childFile = new File(parentFile, instrumentationFileName);
      String uuid = GrouperUuid.getUuid();
      LOG.debug("Writing new uuid " + uuid + " to instrumentation file: " + childFile.getCanonicalPath());
      FileUtils.writeStringToFile(childFile, uuid, Charset.defaultCharset());
      LOG.debug("Uuid for " + grouperEngineName + " is " + uuid);
      return childFile;
    } catch (IOException e) {
      LOG.error("Failed to write uuid to instrumentation file " + instrumentationFileName, e);
      return null;
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
