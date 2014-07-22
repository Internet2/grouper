/**
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
 */
/**
 * @author mchyzer
 * $Id: AttributeAssignDelegateOptions.java,v 1.1 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.sql.Timestamp;


/**
 * placeholder for more options on delegate calls
 */
public class AttributeAssignDelegateOptions {

  /** if this should delegate */
  private AttributeAssignDelegatable attributeAssignDelegatable;

  /** if delegatable should be assigned */
  private boolean assignAttributeAssignDelegatable;
  
  /** enabled date */
  private Timestamp enabledTime;
  
  /** if the enabled date should be assigned */
  private boolean assignEnabledDate;
  
  /** disabled date */
  private Timestamp disabledTime;

  
  /**
   * if delegatable should be assigned
   * @return the assignAttributeAssignDelegatable
   */
  public boolean isAssignAttributeAssignDelegatable() {
    return this.assignAttributeAssignDelegatable;
  }


  
  /**
   * if delegatable should be assigned
   * @param assignAttributeAssignDelegatable1 the assignAttributeAssignDelegatable to set
   */
  public void setAssignAttributeAssignDelegatable(boolean assignAttributeAssignDelegatable1) {
    this.assignAttributeAssignDelegatable = assignAttributeAssignDelegatable1;
  }


  
  /**
   * if the enabled date should be assigned
   * @return the assignEnabledDate
   */
  public boolean isAssignEnabledDate() {
    return this.assignEnabledDate;
  }


  
  /**
   * if the enabled date should be assigned
   * @param assignEnabledDate1 the assignEnabledDate to set
   */
  public void setAssignEnabledDate(boolean assignEnabledDate1) {
    this.assignEnabledDate = assignEnabledDate1;
  }


  
  /**
   * if the disabled date should be assigned
   * @return the assignDisabledDate
   */
  public boolean isAssignDisabledDate() {
    return this.assignDisabledDate;
  }


  
  /**
   * if the disabled date should be assigned
   * @param assignDisabledDate1 the assignDisabledDate to set
   */
  public void setAssignDisabledDate(boolean assignDisabledDate1) {
    this.assignDisabledDate = assignDisabledDate1;
  }


  /**
   * if the disabled date should be assigned
   */
  private boolean assignDisabledDate;
  
  /**
   * if this should delegate
   * @return the attributeAssignDelegatable
   */
  public AttributeAssignDelegatable getAttributeAssignDelegatable() {
    return this.attributeAssignDelegatable;
  }

  
  /**
   * if this should delegate
   * @param attributeAssignDelegatable1 the attributeAssignDelegatable to set
   */
  public void setAttributeAssignDelegatable(
      AttributeAssignDelegatable attributeAssignDelegatable1) {
    this.attributeAssignDelegatable = attributeAssignDelegatable1;
  }

  
  /**
   * enabled date
   * @return the enabledDate
   */
  public Timestamp getEnabledTime() {
    return this.enabledTime;
  }

  
  /**
   * enabled date
   * @param enabledDate1 the enabledDate to set
   */
  public void setEnabledTime(Timestamp enabledDate1) {
    this.enabledTime = enabledDate1;
  }

  
  /**
   * disabled date
   * @return the disabledDate
   */
  public Timestamp getDisabledTime() {
    return this.disabledTime;
  }

  
  /**
   * disabled date
   * @param disabledDate1 the disabledDate to set
   */
  public void setDisabledTime(Timestamp disabledDate1) {
    this.disabledTime = disabledDate1;
  }
  
  
}
