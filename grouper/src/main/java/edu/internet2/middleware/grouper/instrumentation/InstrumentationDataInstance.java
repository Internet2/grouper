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

import java.util.Date;
import java.util.List;

/**
 * @author shilen
 */
public class InstrumentationDataInstance {

  private String uuid;
  
  private String engineName;
  
  private String serverLabel;
  
  private Date lastUpdate;
  
  private List<InstrumentationDataInstanceCounts> counts;

  
  /**
   * @return the uuid
   */
  public String getUuid() {
    return uuid;
  }

  
  /**
   * @param uuid the uuid to set
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  
  /**
   * @return the engineName
   */
  public String getEngineName() {
    return engineName;
  }

  
  /**
   * @param engineName the engineName to set
   */
  public void setEngineName(String engineName) {
    this.engineName = engineName;
  }

  
  /**
   * @return the serverLabel
   */
  public String getServerLabel() {
    return serverLabel;
  }

  
  /**
   * @param serverLabel the serverLabel to set
   */
  public void setServerLabel(String serverLabel) {
    this.serverLabel = serverLabel;
  }

  
  /**
   * @return the lastUpdate
   */
  public Date getLastUpdate() {
    return lastUpdate;
  }

  
  /**
   * @param lastUpdate the lastUpdate to set
   */
  public void setLastUpdate(Date lastUpdate) {
    this.lastUpdate = lastUpdate;
  }


  
  /**
   * @return the counts
   */
  public List<InstrumentationDataInstanceCounts> getCounts() {
    return counts;
  }


  
  /**
   * @param counts the counts to set
   */
  public void setCounts(List<InstrumentationDataInstanceCounts> counts) {
    this.counts = counts;
  }
}
