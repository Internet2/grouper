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
 * @author mchyzer
 * $Id: AttributeAssignResult.java,v 1.1 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.value;



/**
 * simple wrapper bean around result of attribute assignment
 */
public class AttributeAssignValueResult {

  /** if this attribute assignment/deletion took place or already existed */
  private boolean changed = false;
  
  /** if this attribute deletion took place or already existed */
  private boolean deleted = false;
  
  /**
   * the attribute assignment
   */
  private AttributeAssignValue attributeAssignValue = null;

  /**
   * @param newlyAssigned1 if this attribute assignment took place or already existed
   * @param deleted1 
   * @param attributeAssignValue1 the attribute assignment
   */
  public AttributeAssignValueResult(boolean newlyAssigned1, boolean deleted1, AttributeAssignValue attributeAssignValue1) {
    super();
    this.changed = newlyAssigned1;
    this.deleted = deleted1;
    this.attributeAssignValue = attributeAssignValue1;
  }


  /**
   * deleted
   * @return deleted
   */
  public boolean isDeleted() {
    return this.deleted;
  }


  /**
   * if this attribute assignment/deletion took place or already existed
   * @return the newlyAssigned
   */
  public boolean isChanged() {
    return this.changed;
  }

  
  /**
   * if this attribute assignment/deletion took place or already existed
   * @param newlyAssigned the newlyAssigned to set
   */
  public void setChanged(boolean newlyAssigned) {
    this.changed = newlyAssigned;
  }

  
  /**
   * the attribute assignment
   * @return the attributeAssign
   */
  public AttributeAssignValue getAttributeAssignValue() {
    return this.attributeAssignValue;
  }

  
  /**
   * the attribute assignment
   * @param attributeAssignValue the attributeAssign to set
   */
  public void setAttributeAssign(AttributeAssignValue attributeAssignValue) {
    this.attributeAssignValue = attributeAssignValue;
  }
}
