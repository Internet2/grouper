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
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.misc.GrouperObject;


/**
 * Result of one attribute def name being retrieved
 * 
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GuiAttributeDefName extends GuiObjectBase implements Serializable {

  /** folder */
  private AttributeDefName attributeDefName;
  

  /**
   * return the attribute def name
   * @return the attribute def name
   */
  public AttributeDefName getAttributeDefName() {
    return this.attributeDefName;
  }

  /**
   * 
   */
  public GuiAttributeDefName() {
    
  }
  
  /**
   * 
   * @param theAttributeDefName
   */
  public GuiAttributeDefName(AttributeDefName theAttributeDefName) {
    this.attributeDefName = theAttributeDefName;
  }
  
  /**
   * @see GuiObjectBase#getObject()
   */
  @Override
  public GrouperObject getGrouperObject() {
    return this.attributeDefName;
  }
  
}
