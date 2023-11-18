/**
 * Copyright 2014 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
/*
 */

package edu.internet2.middleware.grouper.app.loader;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh.GrouperGroovyResult;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 * Publishes Grouper events to messaging
 *
 */
public class EsbPublisherChangeLogScript extends EsbListenerBase {

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(EsbPublisherChangeLogScript.class);

  /**
   * debug map
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
  
  public Map<String, Object> getDebugMap() {
    return debugMap;
  }

  private List<EsbEventContainer> esbEventContainers;
  
  
  public List<EsbEventContainer> getEsbEventContainers() {
    return esbEventContainers;
  }


  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> theEsbEventContainers) {
 
    this.esbEventContainers = theEsbEventContainers;
    
    debugMap.put("method", "dispatchEventList");
    
    debugMap.put("eventCount", GrouperUtil.length(esbEventContainers));
    
    Long startNanos = System.nanoTime();

    threadLocalEsbPublisherChangeLogScript.set(this);

    try {

      debugMap.put("lastSequenceAvailable", esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
      

      //  # file type, you can run a script in config, or run a file in your container
      //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.changeLogFileType$", formElement: "dropdown", optionValues: ["script", "file"]}
      //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogFileType = 
      //
      //  # source of script
      //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.changeLogScriptSource$", formElement: "textarea", showEl: "${changeLogFileType == 'script'}"}
      //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogScriptSource = 
      //
      //  # file name in container to run
      //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.changeLogFileName$", showEl: "${changeLogFileType == 'file'}"}
      //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogFileName = 

      ChangeLogProcessorMetadata changeLogProcessorMetadata = this.getChangeLogProcessorMetadata();
      String consumerName = changeLogProcessorMetadata.getConsumerName();
      
      String changeLogFileType = GrouperLoaderConfig.retrieveConfig()
          .propertyValueStringRequired("changeLog.consumer." + consumerName + ".changeLogFileType");
      debugMap.put("changeLogFileType", changeLogFileType);
      
      String changeLogScriptSource = GrouperLoaderConfig.retrieveConfig()
          .propertyValueString("changeLog.consumer." + consumerName + ".changeLogScriptSource");

      if (StringUtils.isBlank(changeLogScriptSource)) {
        String changeLogFileName = GrouperLoaderConfig.retrieveConfig()
            .propertyValueStringRequired("changeLog.consumer." + consumerName + ".changeLogFileName");
        debugMap.put("changeLogFileName", changeLogFileName);
        changeLogScriptSource = GrouperUtil.readFileIntoString(new File(changeLogFileName));
        
      }

      GrouperUtil.assertion(!StringUtils.isBlank(changeLogScriptSource), "source is blank!");
      
      StringBuilder scriptToRun = new StringBuilder();
      
      scriptToRun.append("import edu.internet2.middleware.grouper.changeLog.*;\n");
      scriptToRun.append("import edu.internet2.middleware.grouper.esb.listener.*;\n");
      scriptToRun.append("import edu.internet2.middleware.grouper.changeLog.esb.consumer.*;\n");
      scriptToRun.append("import edu.internet2.middleware.grouper.app.loader.*;\n");
      scriptToRun.append("import edu.internet2.middleware.grouper.app.loader.db.*;\n");
      
      scriptToRun.append("EsbPublisherChangeLogScript esbPublisherChangeLogScript = EsbPublisherChangeLogScript.retrieveFromThreadLocal();\n");
      scriptToRun.append("HashMap gsh_builtin_debugMap = esbPublisherChangeLogScript.getDebugMap();\n");
      scriptToRun.append("ChangeLogProcessorMetadata gsh_builtin_changeLogProcessorMetadata = esbPublisherChangeLogScript.getChangeLogProcessorMetadata();\n");
      scriptToRun.append("ProvisioningSyncConsumerResult gsh_builtin_provisioningSyncConsumerResult = esbPublisherChangeLogScript.getProvisioningSyncConsumerResult();\n");
      scriptToRun.append("List gsh_builtin_esbEventContainers = esbPublisherChangeLogScript.getEsbEventContainers();\n");
      scriptToRun.append("GrouperSession gsh_builtin_grouperSession = GrouperSession.staticGrouperSession();\n");
      scriptToRun.append("Hib3GrouperLoaderLog gsh_builtin_hib3GrouperLoaderLog = gsh_builtin_changeLogProcessorMetadata.getHib3GrouperLoaderLog();\n");

      GrouperGroovyResult grouperGroovyResult = GrouperUtil.gshRunScriptReturnResult(scriptToRun + changeLogScriptSource, false);
      Long lastSequenceProcessed = GrouperUtil.longObjectValue(grouperGroovyResult.getResult(), true);  
      
      if (lastSequenceProcessed == null) {
        lastSequenceProcessed = this.provisioningSyncConsumerResult.getLastProcessedSequenceNumber();
      } else {
        GrouperUtil.assertion(this.provisioningSyncConsumerResult.getLastProcessedSequenceNumber() == null 
            || this.provisioningSyncConsumerResult.getLastProcessedSequenceNumber().longValue() == lastSequenceProcessed, 
            "Setting lastSequence processed " + this.provisioningSyncConsumerResult.getLastProcessedSequenceNumber() 
            + " does not match return value " + lastSequenceProcessed);
        this.provisioningSyncConsumerResult.setLastProcessedSequenceNumber(lastSequenceProcessed);
      }
      debugMap.put("lastSequenceProcessed", lastSequenceProcessed);
      if (lastSequenceProcessed == null) {
        throw new RuntimeException("Script did not return or set the lastSequenceProcessed!");
      }
      
      if (lastSequenceProcessed.longValue() != esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber()) {
        int eventsSkipped = 0;
        for (int i = esbEventContainers.size()-1; i>=0; i--) {
          if (lastSequenceProcessed.longValue() == esbEventContainers.get(i).getSequenceNumber().longValue()) {
            break;
          }
          eventsSkipped++;
        }
        debugMap.put("eventsSkipped", GrouperUtil.length(eventsSkipped));
      }

      return provisioningSyncConsumerResult;
  
    } finally {
      
      threadLocalEsbPublisherChangeLogScript.remove();
    
      debugMap.put("tookMillis", ((System.nanoTime() - startNanos)/1000000L));
      this.getEsbConsumer().getChangeLogProcessorMetadata().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));
    }

  }


  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#disconnect()
   */
  @Override
  public void disconnect() {
    // Unused, client does not maintain a persistent connection in this version

  }

  private ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
  
  public ProvisioningSyncConsumerResult getProvisioningSyncConsumerResult() {
    return provisioningSyncConsumerResult;
  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#dispatchEvent(java.lang.String, java.lang.String)
   */
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new UnsupportedOperationException("Not implemented");
  }

  private static ThreadLocal<EsbPublisherChangeLogScript> threadLocalEsbPublisherChangeLogScript = new InheritableThreadLocal();
  
  public static EsbPublisherChangeLogScript retrieveFromThreadLocal() {
    return threadLocalEsbPublisherChangeLogScript.get();
  }

}
