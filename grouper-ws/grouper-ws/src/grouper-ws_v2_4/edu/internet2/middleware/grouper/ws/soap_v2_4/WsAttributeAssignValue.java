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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap_v2_4;



/**
 * value of an attribute assign
 */
public class WsAttributeAssignValue {

  /** id of this attribute assignment */
  private String id;
  
  /** internal value */
  private String valueSystem;

  /** formatted value */
  private String valueFormatted;
  
  /**
   * internal value
   * @return internal value
   */
  public String getValueSystem() {
    return this.valueSystem;
  }

  /**
   * internal value
   * @param valueSystem1
   */
  public void setValueSystem(String valueSystem1) {
    this.valueSystem = valueSystem1;
  }

  /**
   * value formatted
   * @return value formatted
   */
  public String getValueFormatted() {
    return this.valueFormatted;
  }

  /**
   * value formatted
   * @param valueFormatted1
   */
  public void setValueFormatted(String valueFormatted1) {
    this.valueFormatted = valueFormatted1;
  }

  /**
   * id of this attribute assignment
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id of this attribute assignment
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * 
   */
  public WsAttributeAssignValue() {
    //default constructor
  }
}
