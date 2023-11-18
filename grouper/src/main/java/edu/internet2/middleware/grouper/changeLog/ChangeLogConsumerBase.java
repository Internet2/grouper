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
/**
 * 
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.List;


/**
 * extend this class and register in the grouper-loader.properties to be a change log consumer
 * @author mchyzer
 *
 */
public abstract class ChangeLogConsumerBase {

  /**
   * consumer name in config file
   */
  private String consumerName;
  
  /**
   * consumer name in config file
   * @return
   */
  public String getConsumerName() {
    return consumerName;
  }

  /**
   * consumer name in config file
   * @param consumerName
   */
  public void setConsumerName(String consumerName) {
    this.consumerName = consumerName;
  }

  /**
   * process the change logs
   * @param changeLogEntryList  NOTE, DO NOT CHANGE OR EDIT THE OBJECTS IN THIS LIST, THEY MIGHT BE SHARED!
   * @param changeLogProcessorMetadata
   * @return which sequence number it got up to (which sequence number was the last one processed).  or -1 if none processed
   */
  public abstract long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList, 
      ChangeLogProcessorMetadata changeLogProcessorMetadata);

  /**
   * override the batch size configured in config file
   * @return
   */
  public Integer getBatchSize() {
    return null;
  }

  /**
   * some change log consumers might want to be called even if nothing happened in change log
   * e.g. check messages in provisioners
   * @return
   */
  public boolean callAtLeastOnce() {
    return false;
  }
  
}
