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
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import java.util.Set;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientCommonUtils;


/**
 * value of an attribute assign
 */
public class WsAttributeAssignValue implements Comparable<WsAttributeAssignValue> {

  /**
   * turn an array to a string
   * @param wsAttributeAssignValues1
   * @param maxSize is the size where this value get larger than, 
   * it should just quit and return what it has
   * @return the string value
   */
  public static String toString(WsAttributeAssignValue[] wsAttributeAssignValues1, int maxSize) {
    if (GrouperClientCommonUtils.length(wsAttributeAssignValues1) == 0) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    
    int index = 0;
    for (WsAttributeAssignValue wsAttributeAssignValue : wsAttributeAssignValues1) {
      if (maxSize > 0 && result.length() > maxSize) {
        break;
      }
      result.append(index).append(". ");
      if (!GrouperClientCommonUtils.isBlank(wsAttributeAssignValue.id)) {
        result.append("id: ").append(wsAttributeAssignValue.id).append(", ");
      }
      if (!GrouperClientCommonUtils.isBlank(wsAttributeAssignValue.valueFormatted)) {
        result.append("valueFormatted: ").append(wsAttributeAssignValue.valueFormatted).append(", ");
      }
      if (!GrouperClientCommonUtils.isBlank(wsAttributeAssignValue.valueSystem)) {
        result.append("valueSystem: ").append(wsAttributeAssignValue.valueSystem).append(", ");
      }
      index++;
    }
    return result.toString();
  }
  
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
   * compare and sort so results are reproducible for tests
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsAttributeAssignValue o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (o2 == null) {
      return 1;
    }
    int compare;
    
    compare = GrouperUtil.compare(this.valueSystem, o2.valueSystem);
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(this.id, o2.id);
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
   * convert attribute assigns
   * @param attributeAssignValueSet should be the value row
   * @return the subject results
   */
  public static WsAttributeAssignValue[] convertAttributeAssigns(Set<AttributeAssignValue> attributeAssignValueSet) {
    int attributeAssignSetLength = GrouperUtil.length(attributeAssignValueSet);
    if (attributeAssignSetLength == 0) {
      return null;
    }
  
    WsAttributeAssignValue[] wsAttributeAssignValueResultArray = new WsAttributeAssignValue[attributeAssignSetLength];
    int index = 0;
    for (AttributeAssignValue attributeAssignValue : attributeAssignValueSet) {
            
      wsAttributeAssignValueResultArray[index++] = new WsAttributeAssignValue(attributeAssignValue);
      
    }
    
    Arrays.sort(wsAttributeAssignValueResultArray);
    
    return wsAttributeAssignValueResultArray;
  }
  
  /** 
   * convert pit attribute assign values
   * @param attributeAssignValueSet should be the value row
   * @return the results
   */
  public static WsAttributeAssignValue[] convertPITAttributeAssignValues(Set<PITAttributeAssignValue> attributeAssignValueSet) {
    int attributeAssignSetLength = GrouperUtil.length(attributeAssignValueSet);
    if (attributeAssignSetLength == 0) {
      return null;
    }
  
    WsAttributeAssignValue[] wsAttributeAssignValueResultArray = new WsAttributeAssignValue[attributeAssignSetLength];
    int index = 0;
    for (PITAttributeAssignValue attributeAssignValue : attributeAssignValueSet) {
      wsAttributeAssignValueResultArray[index++] = new WsAttributeAssignValue(attributeAssignValue);      
    }
    
    Arrays.sort(wsAttributeAssignValueResultArray);
    
    return wsAttributeAssignValueResultArray;
  }


  /**
   * 
   */
  public WsAttributeAssignValue() {
    //default constructor
  }
  
  /**
   * construct with attribute assign to set internal fields
   * 
   * @param attributeAssignValue
   */
  public WsAttributeAssignValue(AttributeAssignValue attributeAssignValue) {

    this.id = attributeAssignValue.getId();
    this.valueSystem = attributeAssignValue.valueString(true);
    
  }

  /**
   * construct with pit attribute assign value to set internal fields
   * 
   * @param attributeAssignValue
   */
  public WsAttributeAssignValue(PITAttributeAssignValue attributeAssignValue) {
    this.id = attributeAssignValue.getSourceId();
    this.valueSystem = attributeAssignValue.valueString();
  }
}
