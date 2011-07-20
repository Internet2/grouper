/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeAssignValueFinder;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefFinder;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefNameFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * result of attribute assign query represents an assignment in the DB
 */
public class WsLimitAssign implements Comparable<WsLimitAssign> {

  /** attribute name id in this assignment */
  private String attributeDefNameId;
  
  /** attribute name in this assignment */
  private String attributeDefNameName;

  /** value(s) in this assignment if any */
  private WsAttributeAssignValue[] wsAttributeAssignValues;

  /**
   * value(s) in this assignment if any
   * @return values
   */
  public WsAttributeAssignValue[] getWsAttributeAssignValues() {
    return this.wsAttributeAssignValues;
  }

  /**
   * value(s) in this assignment if any
   * @param wsAttributeAssignValues1
   */
  public void setWsAttributeAssignValues(WsAttributeAssignValue[] wsAttributeAssignValues1) {
    this.wsAttributeAssignValues = wsAttributeAssignValues1;
  }

  /** id of this attribute assignment */
  private String attributeAssignId;
  
  /**
   * compare and sort so results are reproducible for tests
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsLimitAssign o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (o2 == null) {
      return 1;
    }
    int compare;
    
    compare = GrouperUtil.compare(this.getAttributeDefNameName(), o2.getAttributeDefNameName());
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(this.attributeAssignId, o2.attributeAssignId);
  }

  /**
   * attribute name id in this assignment
   * @return attribute name id in this assignment
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  /**
   * attribute name id in this assignment
   * @param attributeDefNameId1
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
  }

  /**
   * attribute name in this assignment
   * @return attribute name in this assignment
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  /**
   * attribute name in this assignment
   * @param attributeDefNameName1
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }

  /**
   * id of this attribute assignment
   * @return id
   */
  public String getAttributeAssignId() {
    return this.attributeAssignId;
  }

  /**
   * id of this attribute assignment
   * @param id1
   */
  public void setAttributeAssignId(String id1) {
    this.attributeAssignId = id1;
  }

  /**
   * convert attribute assigns
   * @param attributeAssignSet should be the membership, group, and member objects in a row
   * @return the subject results
   */
  public static WsLimitAssign[] convertAttributeAssigns(Set<AttributeAssign> attributeAssignSet) {
    int attributeAssignSetLength = GrouperUtil.length(attributeAssignSet);
    if (attributeAssignSetLength == 0) {
      return null;
    }
  
    WsLimitAssign[] wsAttributeAssignResultArray = new WsLimitAssign[attributeAssignSetLength];
    int index = 0;
    for (AttributeAssign attributeAssign : attributeAssignSet) {
            
      wsAttributeAssignResultArray[index++] = new WsLimitAssign(attributeAssign);
      
    }
    return wsAttributeAssignResultArray;
  }


  /**
   * 
   */
  public WsLimitAssign() {
    //default constructor
  }
  
  /**
   * construct with attribute assign to set internal fields
   * 
   * @param attributeAssign
   */
  public WsLimitAssign(AttributeAssign attributeAssign) {
    
    AttributeDefName theAttributeDefName = attributeAssign.getAttributeDefName();
    AttributeDef theAttributeDef = theAttributeDefName == null ? null : theAttributeDefName.getAttributeDef();

    this.attributeDefNameId = attributeAssign.getAttributeDefNameId();
    this.attributeDefNameName = theAttributeDefName == null ? null : theAttributeDefName.getName();

    this.attributeAssignId = attributeAssign.getId();
    
    //get the values
    if (theAttributeDef != null && !StringUtils.isBlank(this.attributeAssignId) && theAttributeDef.getValueType() != null
        && theAttributeDef.getValueType().hasValue()) {
      
      Set<AttributeAssignValue> attributeAssignValues = GrouperDAOFactory
        .getFactory().getAttributeAssignValue().findByAttributeAssignId(this.attributeAssignId);
      
      if (GrouperUtil.length(attributeAssignValues) > 0) {
        this.wsAttributeAssignValues = WsAttributeAssignValue.convertAttributeAssigns(attributeAssignValues);
      }
      
    }
    
  }

  /**
   * construct with attribute assign to set internal fields
   * 
   * @param pitAttributeAssign
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   */
  public WsLimitAssign(PITAttributeAssign pitAttributeAssign, Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
    
    PITAttributeDefName theAttributeDefName = PITAttributeDefNameFinder.findById(pitAttributeAssign.getAttributeDefNameId(), false);
    PITAttributeDef theAttributeDef = PITAttributeDefFinder.findById(theAttributeDefName.getAttributeDefId(), false);
    
    this.attributeDefNameId = pitAttributeAssign.getAttributeDefNameId();
    this.attributeDefNameName = theAttributeDefName == null ? null : theAttributeDefName.getName();

    this.attributeAssignId = pitAttributeAssign.getId();
    //get the values
    if (theAttributeDef != null && !StringUtils.isBlank(this.attributeAssignId)) {
      
      Set<PITAttributeAssignValue> values = PITAttributeAssignValueFinder.findByPITAttributeAssign(
          pitAttributeAssign, pointInTimeFrom, pointInTimeTo);
      
      if (GrouperUtil.length(values) > 0) {
        this.wsAttributeAssignValues = WsAttributeAssignValue.convertPITAttributeAssignValues(values);
      }
      
    }
  }
}
