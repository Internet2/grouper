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
package edu.internet2.middleware.grouper.attr.value;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;


/**
 * if retrieving all assigns of assigns, and values, and attribute def names, etc, put them in this bean.
 * assumes each attr assign has one and only one value (not zero)
 * @author mchyzer
 *
 */
public class AttributeAssignValueContainer {

  /** attribute assign on owner of attribute */
  private AttributeAssign attributeTypeAssign;

  /** attribute assign on the attributeTypeAssign, which has the value */
  private AttributeAssign attributeValueAssign;
  
  /** value on this attribute */
  private AttributeAssignValue attributeAssignValue;
  
  /** attribute def name of the attribute */
  private AttributeDefName attributeDefName;

  /**
   * attribute assign on owner of attribute
   * @return attribute assign
   */
  public AttributeAssign getAttributeTypeAssign() {
    return this.attributeTypeAssign;
  }

  /**
   * attribute assign on owner of attribute
   * @param attributeTypeAssign1
   */
  public void setAttributeTypeAssign(AttributeAssign attributeTypeAssign1) {
    this.attributeTypeAssign = attributeTypeAssign1;
  }

  /**
   * attribute assign on the attributeTypeAssign, which has the value
   * @return attribute assign
   */
  public AttributeAssign getAttributeValueAssign() {
    return this.attributeValueAssign;
  }

  /**
   * attribute assign on the attributeTypeAssign, which has the value
   * @param attributeValueAssign1
   */
  public void setAttributeValueAssign(AttributeAssign attributeValueAssign1) {
    this.attributeValueAssign = attributeValueAssign1;
  }

  /**
   * value on this attribute
   * @return value on this attribute
   */
  public AttributeAssignValue getAttributeAssignValue() {
    return this.attributeAssignValue;
  }

  /**
   * value on this attribute
   * @param attributeAssignValue1
   */
  public void setAttributeAssignValue(AttributeAssignValue attributeAssignValue1) {
    this.attributeAssignValue = attributeAssignValue1;
  }

  /**
   * attribute def name of the attribute
   * @return attribute def name of the attribute
   */
  public AttributeDefName getAttributeDefName() {
    return this.attributeDefName;
  }

  /**
   * attribute def name of the attribute
   * @param attributeDefName1
   */
  public void setAttributeDefName(AttributeDefName attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }

  /**
   * get the value of an attribute, or null if not there
   * @param attributeAssignValueContainers
   * @param attributeDefNameName
   * @return the string value of the attribute
   */
  public static String attributeValueString(Set<AttributeAssignValueContainer> attributeAssignValueContainers, String attributeDefNameName) {
    for (AttributeAssignValueContainer attributeAssignValueContainer : attributeAssignValueContainers) {
      if (StringUtils.equals(attributeAssignValueContainer.getAttributeDefName().getName(), attributeDefNameName)) {
        return attributeAssignValueContainer.getAttributeAssignValue().getValueString();
      }
    }
    return null;
  }
  
}
