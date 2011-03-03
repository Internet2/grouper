/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap;

import java.util.Set;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * value of an attribute assign
 */
public class WsAttributeAssignValue implements Comparable<WsAttributeAssignValue> {

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
    this.id = attributeAssignValue.getId();
    this.valueSystem = attributeAssignValue.valueString();
  }
}
