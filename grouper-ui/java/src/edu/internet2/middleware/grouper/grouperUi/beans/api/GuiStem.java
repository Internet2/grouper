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

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.misc.GrouperObject;


/**
 * Result of one folder being retrieved.
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GuiStem extends GuiObjectBase implements Serializable {

  /** folder */
  private Stem stem;
  

  /**
   * return the stem
   * @return the stem
   */
  public Stem getStem() {
    return this.stem;
  }

  /**
   * 
   */
  public GuiStem() {
    
  }
  
  /**
   * 
   * @param theStem
   */
  public GuiStem(Stem theStem) {
    this.stem = theStem;
  }
  
  /**
   * @see GuiObjectBase#getObject()
   */
  @Override
  public GrouperObject getGrouperObject() {
    return this.stem;
  }
  
}
