/*******************************************************************************
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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_5;

import org.apache.commons.lang.StringUtils;

/**
 * <pre>
 * Class to lookup an attribute assignment via web service
 * 
 * developers make sure each setter calls this.clearAttributeAssignment();
 * </pre>
 * @author mchyzer
 */
public class WsAttributeAssignLookup {

  /**
   * see if this attributeAssign lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.uuid) || !StringUtils.isBlank(this.batchIndex);
  }
  
  /**
   * uuid of the attributeAssign to find
   */
  private String uuid;

  /**
   * uuid of the attributeAssign to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the attributeAssign to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * 
   */
  public WsAttributeAssignLookup() {
    //blank
  }

  /**
   * if there is a batch request, and this attribute assignment 
   * refers to a previously sent assignment, this is the index (0 indexed)
   */
  private String batchIndex;

  /**
   * if there is a batch request, and this attribute assignment 
   * refers to a previously sent assignment, this is the index (0 indexed)
   * @return the batch index
   */
  public String getBatchIndex() {
    return this.batchIndex;
  }

  /**
   * if there is a batch request, and this attribute assignment 
   * refers to a previously sent assignment, this is the index (0 indexed)
   * @param theIndex the index to set
   */
  public void setBatchIndex(String theIndex) {
    this.batchIndex = theIndex;
  }

}
