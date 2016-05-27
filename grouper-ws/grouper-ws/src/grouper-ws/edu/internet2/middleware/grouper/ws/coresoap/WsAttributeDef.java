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
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;

/**
 * Result of one attribute def name being retrieved.  The number of
 * attribute def names will equal the number of attribute def names related to the result
 * 
 * @author mchyzer
 */
public class WsAttributeDef implements Comparable<WsAttributeDef> {

  /**
   * integer ID for object
   */
  private String idIndex;
  
  /**
   * integer ID for object
   * @return the id
   */
  public String getIdIndex() {
    return this.idIndex;
  }

  /**
   * integer ID for object
   * @param idIndex1
   */
  public void setIdIndex(String idIndex1) {
    this.idIndex = idIndex1;
  }

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
   * @param attributeDefNameSet
   * @return the attributeDefs (null if none or null)
   */
  public static WsAttributeDef[] convertAttributeDefNames(Set<AttributeDefName> attributeDefNameSet) {
    if (attributeDefNameSet == null || attributeDefNameSet.size() == 0) {
      return null;
    }

    Set<AttributeDef> attributeDefSet = new TreeSet<AttributeDef>();
    Set<String> idsOfAttributeDefs = new LinkedHashSet<String>();
    for (AttributeDefName attributeDefName : attributeDefNameSet) {
      if (!idsOfAttributeDefs.contains(attributeDefName.getAttributeDefId())) {
        idsOfAttributeDefs.add(attributeDefName.getAttributeDefId());
        attributeDefSet.add(attributeDefName.getAttributeDef());
      }
    }
    
    return convertAttributeDefs(attributeDefSet);

  }


  
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
   * assign to Attribute Def T|F 
   */
  private String assignToAttributeDef = "F";
  
  /**
   * assign to Attribute Def assignment T|F
   */
  private String assignToAttributeDefAssignment = "F";
  
  /**
   * assign to effective membership T|F
   */
  private String assignToEffectiveMembership = "F";
  
  /**
   * assign to effective membership assignment T|F
   */
  private String assignToEffectiveMembershipAssignment = "F";
  
  /**
   * assign to group T|F
   */
  private String assignToGroup = "F";
    
  /**
   * assign to group assignment T|F
   */
  private String assignToGroupAssignment = "F";
  
  /**
   * assign to immediate membership T|F
   */
  private String assignToImmediateMembership = "F";
  
  /**
   * assign to immediate membership assignment T|F
   */
  private String assignToImmediateMembershipAssignment = "F";
  
  /**
   * assign to member T|F
   */
  private String assignToMember = "F";
  
  /**
   * assign to member assignment T|F
   */
  private String assignToMemberAssignment = "F";
  
  /**
   * assign to stem T|F
   */
  private String assignToStem = "F";
  
  /**
   * assign to stem assignment T|F
   */
  private String assignToStemAssignment = "F";
  
  
  /**
   * @return true if this attribute def is assigned to  anything
   */
  public boolean areThereAnyAssignables() {
    return this.getAssignToAttributeDef().equalsIgnoreCase("T") || this.getAssignToAttributeDefAssignment().equalsIgnoreCase("T")
        || this.getAssignToEffectiveMembership().equalsIgnoreCase("T") || this.getAssignToEffectiveMembershipAssignment().equalsIgnoreCase("T")
        || this.getAssignToGroup().equalsIgnoreCase("T") || this.getAssignToGroupAssignment().equalsIgnoreCase("T")
        || this.getAssignToImmediateMembership().equalsIgnoreCase("T") || this.getAssignToImmediateMembershipAssignment().equalsIgnoreCase("T")
        || this.getAssignToMember().equalsIgnoreCase("T") || this.getAssignToMemberAssignment().equalsIgnoreCase("T")
        || this.getAssignToStem().equalsIgnoreCase("T") || this.getAssignToStemAssignment().equalsIgnoreCase("T");
     
  }
  
  
  /**
   * @return the assignToAttributeDef
   */
  public String getAssignToAttributeDef() {
    return this.assignToAttributeDef;
  }

  
  /**
   * @param assignToAttributeDef1 the assignToAttributeDef to set
   */
  public void setAssignToAttributeDef(String assignToAttributeDef1) {
    this.assignToAttributeDef = assignToAttributeDef1;
  }

  
  /**
   * @return the assignToAttributeDefAssignment
   */
  public String getAssignToAttributeDefAssignment() {
    return this.assignToAttributeDefAssignment;
  }

  
  /**
   * @param assignToAttributeDefAssignment1 the assignToAttributeDefAssignment to set
   */
  public void setAssignToAttributeDefAssignment(String assignToAttributeDefAssignment1) {
    this.assignToAttributeDefAssignment = assignToAttributeDefAssignment1;
  }

  
  /**
   * @return the assignToEffectiveMembership
   */
  public String getAssignToEffectiveMembership() {
    return this.assignToEffectiveMembership;
  }

  
  /**
   * @param assignToEffectiveMembership1 the assignToEffectiveMembership to set
   */
  public void setAssignToEffectiveMembership(String assignToEffectiveMembership1) {
    this.assignToEffectiveMembership = assignToEffectiveMembership1;
  }

  
  /**
   * @return the assignToEffectiveMembershipAssignment
   */
  public String getAssignToEffectiveMembershipAssignment() {
    return this.assignToEffectiveMembershipAssignment;
  }

  
  /**
   * @param assignToEffectiveMembershipAssignment1 the assignToEffectiveMembershipAssignment to set
   */
  public void setAssignToEffectiveMembershipAssignment(
      String assignToEffectiveMembershipAssignment1) {
    this.assignToEffectiveMembershipAssignment = assignToEffectiveMembershipAssignment1;
  }

  
  /**
   * @return the assignToGroup
   */
  public String getAssignToGroup() {
    return this.assignToGroup;
  }

  
  /**
   * @param assignToGroup1 the assignToGroup to set
   */
  public void setAssignToGroup(String assignToGroup1) {
    this.assignToGroup = assignToGroup1;
  }

  
  /**
   * @return the assignToGroupAssignment
   */
  public String getAssignToGroupAssignment() {
    return this.assignToGroupAssignment;
  }

  
  /**
   * @param assignToGroupAssignment1 the assignToGroupAssignment to set
   */
  public void setAssignToGroupAssignment(String assignToGroupAssignment1) {
    this.assignToGroupAssignment = assignToGroupAssignment1;
  }

  
  /**
   * @return the assignToImmediateMembership
   */
  public String getAssignToImmediateMembership() {
    return this.assignToImmediateMembership;
  }

  
  /**
   * @param assignToImmediateMembership1 the assignToImmediateMembership to set
   */
  public void setAssignToImmediateMembership(String assignToImmediateMembership1) {
    this.assignToImmediateMembership = assignToImmediateMembership1;
  }

  
  /**
   * @return the assignToImmediateMembershipAssignment
   */
  public String getAssignToImmediateMembershipAssignment() {
    return this.assignToImmediateMembershipAssignment;
  }

  
  /**
   * @param assignToImmediateMembershipAssignment1 the assignToImmediateMembershipAssignment to set
   */
  public void setAssignToImmediateMembershipAssignment(
      String assignToImmediateMembershipAssignment1) {
    this.assignToImmediateMembershipAssignment = assignToImmediateMembershipAssignment1;
  }

  
  /**
   * @return the assignToMember
   */
  public String getAssignToMember() {
    return this.assignToMember;
  }

  
  /**
   * @param assignToMember1 the assignToMember to set
   */
  public void setAssignToMember(String assignToMember1) {
    this.assignToMember = assignToMember1;
  }

  
  /**
   * @return the assignToMemberAssignment
   */
  public String getAssignToMemberAssignment() {
    return this.assignToMemberAssignment;
  }

  
  /**
   * @param assignToMemberAssignment1 the assignToMemberAssignment to set
   */
  public void setAssignToMemberAssignment(String assignToMemberAssignment1) {
    this.assignToMemberAssignment = assignToMemberAssignment1;
  }

  
  /**
   * @return the assignToStem
   */
  public String getAssignToStem() {
    return this.assignToStem;
  }

  
  /**
   * @param assignToStem1 the assignToStem to set
   */
  public void setAssignToStem(String assignToStem1) {
    this.assignToStem = assignToStem1;
  }

  
  /**
   * @return the assignToStemAssignment
   */
  public String getAssignToStemAssignment() {
    return this.assignToStemAssignment;
  }

  
  /**
   * @param assignToStemAssignment1 the assignToStemAssignment to set
   */
  public void setAssignToStemAssignment(String assignToStemAssignment1) {
    this.assignToStemAssignment = assignToStemAssignment1;
  }

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

      //if greater then 2.2 then set id index
      if (GrouperWsVersionUtils.retrieveCurrentClientVersion()
          .greaterOrEqualToArg(GrouperVersion.valueOfIgnoreCase("v2_2_000"))) {
        this.setIdIndex(attributeDef.getIdIndex() == null ? null : attributeDef.getIdIndex().toString());
      }

    } else {
      if (wsAttributeDefLookup != null) {
        //no attributeDef, set the look values so the caller can keep things in sync
        this.setName(wsAttributeDefLookup.getName());
        this.setUuid(wsAttributeDefLookup.getUuid());
        this.setExtension(GrouperUtil.extensionFromName(wsAttributeDefLookup.getName()));
        //if greater then 2.2 then set id index
        if (GrouperWsVersionUtils.retrieveCurrentClientVersion()
            .greaterOrEqualToArg(GrouperVersion.valueOfIgnoreCase("v2_2_000"))) {
          this.setIdIndex(wsAttributeDefLookup.getIdIndex() == null ? null : wsAttributeDefLookup.getIdIndex().toString());
        }
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
      this.setUuid(attributeDef.getSourceId());
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
