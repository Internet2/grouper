/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * Result of one attribute def name being retrieved.  The number of
 * attribute def names will equal the number of attribute def names related to the result
 * 
 * @author mchyzer
 */
public class WsAttributeDef implements Comparable<WsAttributeDef> {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /** extension of attributeDef, the part to the right of last colon in name */
  private String extension;

  /**
   * convert a set of attribute def names to results
   * @param attributeDefSet
   * @return the attributeDefs (null if none or null)
   */
  public static WsAttributeDef[] convertAttributeDefs(Set<AttributeDef> attributeDefSet) {
    if (attributeDefSet == null || attributeDefSet.size() == 0) {
      return null;
    }
    int attributeDefSetSize = attributeDefSet.size();
    WsAttributeDef[] wsAttributeDefResults = new WsAttributeDef[attributeDefSetSize];
    int index = 0;
    for (AttributeDef attributeDef : attributeDefSet) {
      WsAttributeDef wsAttributeDef = new WsAttributeDef(attributeDef, null);
      wsAttributeDefResults[index] = wsAttributeDef;
      index++;
    }
    return wsAttributeDefResults;

  }

  /**
   * friendly description of this attributeDef
   */
  private String description;

  /**
   * Full name of the attributeDef (all extensions of parent stems, separated by colons,  and the extention of this attributeDef
   */
  private String name;

  /**
   * universally unique identifier of this attributeDef
   */
  private String uuid;
  
  /**
   * type of attribute def, from enum AttributeDefType, e.g. attr, domain, type, limit, perm
   */
  private String attributeDefType;

  /**
   * T of F for if can be assigned multiple times to one object
   */
  private String multiAssignable;
  
  /**
   * T or F for if multiple values can be assigned to the attribute assignment
   */
  private String multiValued;
  
  /**
   * what type of value on assignments: AttributeDefValueType: e.g. integer, timestamp, string, floating, marker, memberId
   */
  private String valueType;

  /**
   * can be assigned to these types: ATTRIBUTE_DEF, ATTRIBUTE_DEF_ASSIGNMENT, EFFECTIVE_MEMBERSHIP,
   * EFFECTIVE_MEMBERSHIP_ASSIGNMENT, GROUP, GROUP_ASSIGNMENT, IMMEDIATE_MEMBERSHIP,
   * IMMEDIATE_MEMBERSHIP_ASSIGNMENT, MEMBER, MEMBER_ASSIGNMENT, STEM, STEM_ASSIGNMENT
   */
  private String[] assignableTos;
  
  /**
   * type of attribute def, from enum AttributeDefType, e.g. attr, domain, type, limit, perm
   * @return the type
   */
  public String getAttributeDefType() {
    return this.attributeDefType;
  }

  /**
   * type of attribute def, from enum AttributeDefType, e.g. attr, domain, type, limit, perm
   * @param attributeDefType1
   */
  public void setAttributeDefType(String attributeDefType1) {
    this.attributeDefType = attributeDefType1;
  }

  /**
   *  T of F for if can be assigned multiple times to one object
   * @return if multi assignable
   */
  public String getMultiAssignable() {
    return this.multiAssignable;
  }

  /**
   * T of F for if can be assigned multiple times to one object
   * @param multiAssignable1
   */
  public void setMultiAssignable(String multiAssignable1) {
    this.multiAssignable = multiAssignable1;
  }

  /**
   * T or F, if has values, if can assign multiple values to one assignment
   * @return T or F, if has values, if can assign multiple values to one assignment
   */
  public String getMultiValued() {
    return this.multiValued;
  }

  /**
   * T or F, if has values, if can assign multiple values to one assignment
   * @param multiValued1
   */
  public void setMultiValued(String multiValued1) {
    this.multiValued = multiValued1;
  }

  /**
   * what type of value on assignments: AttributeDefValueType: e.g. integer, timestamp, string, floating, marker, memberId
   * @return value type
   */
  public String getValueType() {
    return this.valueType;
  }

  /**
   * what type of value on assignments: AttributeDefValueType: e.g. integer, timestamp, string, floating, marker, memberId
   * @param valueType1
   */
  public void setValueType(String valueType1) {
    this.valueType = valueType1;
  }

  /**
   * can be assigned to these types
   * @return assignable tos
   */
  public String[] getAssignableTos() {
    return this.assignableTos;
  }

  /**
   * can be assigned to these types
   * @param assignableTos1
   */
  public void setAssignableTos(String[] assignableTos1) {
    this.assignableTos = assignableTos1;
  }

  /**
   * no arg constructor
   */
  public WsAttributeDef() {
    //blank

  }

  /**
   * construct based on attribute def name, assign all fields
   * @param attributeDef 
   * @param wsAttributeDefLookup is the lookup to set looked up values
   */
  public WsAttributeDef(AttributeDef attributeDef, WsAttributeDefLookup wsAttributeDefLookup) {
    if (attributeDef != null) {
      this.setDescription(StringUtils.trimToNull(attributeDef.getDescription()));
      this.setName(attributeDef.getName());
      this.setUuid(attributeDef.getId());
      this.setExtension(attributeDef.getExtension());
      
      this.attributeDefType = attributeDef.getAttributeDefTypeDb();
      this.multiAssignable = GrouperServiceUtils.booleanToStringOneChar(attributeDef.isMultiAssignable());
      this.multiValued = GrouperServiceUtils.booleanToStringOneChar(attributeDef.isMultiValued());
      this.valueType = attributeDef.getValueTypeDb();
      
    } else {
      if (wsAttributeDefLookup != null) {
        //no attributeDef, set the look values so the caller can keep things in sync
        this.setName(wsAttributeDefLookup.getName());
        this.setUuid(wsAttributeDefLookup.getUuid());
        this.setExtension(GrouperUtil.extensionFromName(wsAttributeDefLookup.getName()));
      }
    }
  }
  
  /**
   * construct based on attribute def name, assign all fields
   * @param attributeDef 
   * @param wsAttributeDefLookup is the lookup to set looked up values
   */
  public WsAttributeDef(PITAttributeDef attributeDef, WsAttributeDefLookup wsAttributeDefLookup) {
    if (attributeDef != null) {
      this.setName(attributeDef.getName());
      this.setUuid(attributeDef.getId());
      this.setExtension(GrouperUtil.extensionFromName(attributeDef.getName()));
      
      this.attributeDefType = attributeDef.getAttributeDefTypeDb();      
    } else {
      if (wsAttributeDefLookup != null) {
        //no attributeDef, set the look values so the caller can keep things in sync
        this.setName(wsAttributeDefLookup.getName());
        this.setUuid(wsAttributeDefLookup.getUuid());
        this.setExtension(GrouperUtil.extensionFromName(wsAttributeDefLookup.getName()));
      }
    }
  }

  /**
   * friendly description of this attributeDef
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Full name of the attributeDef (all extensions of parent stems, separated by colons, 
   * and the extention of this attributeDef
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * universally unique identifier of this attributeDef
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * friendly description of this attributeDef
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * Full name of the attributeDef (all extensions of parent stems, separated by colons, 
   * and the extention of this attributeDef
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * universally unique identifier of this attributeDef
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * extension of attributeDef, the part to the right of last colon in name
   * @return the extension
   */
  public String getExtension() {
    return this.extension;
  }

  /**
   * extension of attributeDef, the part to the right of last colon in name
   * @param extension1 the extension to set
   */
  public void setExtension(String extension1) {
    this.extension = extension1;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsAttributeDef o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (this == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }
    return GrouperUtil.compare(this.getName(), o2.getName());
  }
}
