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
 * $Id: AttributeAssignResult.java,v 1.1 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * simple wrapper bean around result of attribute assignment
 */
public class AttributeAssignResult {

  /** if this attribute assignment took place or already existed */
  private boolean changed = false;
  
  /**
   * the attribute assignment
   */
  private Set<AttributeAssign> attributeAssigns = null;

  /**
   * default constructor
   */
  public AttributeAssignResult() {
    //empty
  }
  
  /**
   * @param newlyAssigned1 if this attribute assignment took place or already existed
   * @param attributeAssign1 the attribute assignment
   */
  public AttributeAssignResult(boolean newlyAssigned1, AttributeAssign attributeAssign1) {
    super();
    this.changed = newlyAssigned1;
    this.attributeAssigns = GrouperUtil.toSet(attributeAssign1);
  }


  /**
   * if this attribute assignment took place or already existed
   * @return the newlyAssigned
   */
  public boolean isChanged() {
    return this.changed;
  }

  
  /**
   * if this attribute assignment took place or already existed
   * @param newlyAssigned the newlyAssigned to set
   */
  public void setChanged(boolean newlyAssigned) {
    this.changed = newlyAssigned;
  }

  
  /**
   * the attribute assignment
   * @return the attributeAssign
   */
  public AttributeAssign getAttributeAssign() {
    return GrouperUtil.setPopOne(this.attributeAssigns);
  }

  
  /**
   * the attribute assignment
   * @param attributeAssign the attributeAssign to set
   */
  public void setAttributeAssign(AttributeAssign attributeAssign) {
    this.attributeAssigns = GrouperUtil.toSet(attributeAssign);
  }


  /**
   * get all assignments (useful in deletions)
   * @return assignments
   */
  public Set<AttributeAssign> getAttributeAssigns() {
    return this.attributeAssigns;
  }


  /**
   * set assignments (useful in deletions)
   * @param attributeAssigns1
   */
  public void setAttributeAssigns(Set<AttributeAssign> attributeAssigns1) {
    this.attributeAssigns = attributeAssigns1;
  }
}
