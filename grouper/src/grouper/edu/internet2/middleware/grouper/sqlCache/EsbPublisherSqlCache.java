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

package edu.internet2.middleware.grouper.sqlCache;

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
public class EsbPublisherSqlCache extends EsbListenerBase {

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(EsbPublisherSqlCache.class);

  /**
   * debug map
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
  
  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> esbEventContainers) {
 
    debugMap.put("method", "dispatchEventList");
    
    debugMap.put("eventCount", GrouperUtil.length(esbEventContainers));
    
    Long startNanos = System.nanoTime();

//    try {
//
//      ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
//
//      debugMap.put("lastSequenceAvailable", esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
//
//      for (int i = esbEventContainers.size()-1; i>=0; i--) {
//        if (lastSequenceProcessed.longValue() == esbEventContainers.get(i).getSequenceNumber().longValue()) {
//          break;
//        }
//        eventsSkipped++;
//      }
//      return provisioningSyncConsumerResult;
//  
//    } finally {
//      
//      threadLocalEsbPublisherSqlCache.remove();
//    
//      debugMap.put("tookMillis", ((System.nanoTime() - startNanos)/1000000L));
//      this.getEsbConsumer().getChangeLogProcessorMetadata().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));
//    }
return null;
  }


  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#disconnect()
   */
  @Override
  public void disconnect() {
    // Unused, client does not maintain a persistent connection in this version

  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#dispatchEvent(java.lang.String, java.lang.String)
   */
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new UnsupportedOperationException("Not implemented");
  }

}
