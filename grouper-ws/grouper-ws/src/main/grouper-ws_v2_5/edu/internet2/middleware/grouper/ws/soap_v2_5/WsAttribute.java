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

/**
 * attribute sent back to caller
 * @author mchyzer
 *
 */
public class WsAttribute {

  /**
   * name of attribute 
   */
  private String name;

  /**
   * value of attribute 
   */
  private String value;

  /**
   * name of attribute
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * value of attribute
   * @return the value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * name of attribute
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * @param value1 the value to set
   */
  public void setValue(String value1) {
    this.value = value1;
  }

}
