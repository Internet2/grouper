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
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.attr.AttributeDef;


/**
 * @author mchyzer
 *
 */
public class RulesAttributeDefBean extends RulesBean {

  /**
   * 
   */
  public RulesAttributeDefBean() {
    
  }
  
  /**
   * 
   * @param theAttributeDef
   */
  public RulesAttributeDefBean(AttributeDef theAttributeDef) {
    super();
    this.attributeDef = theAttributeDef;
  }


  /** attributeDef */
  private AttributeDef attributeDef;

  /**
   * attributeDef
   * @return attributeDef
   */
  @Override
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

  /**
   * @see RulesBean#hasAttributeDef()
   */
  @Override
  public boolean hasAttributeDef() {
    return true;
  }

  /**
   * attributeDef
   * @param attributeDef1
   */
  public void setAttributeDef(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }

  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (this.attributeDef != null) {
      result.append("group: ").append(this.attributeDef.getName()).append(", ");
    }
    return result.toString();
  }
  
  
}
