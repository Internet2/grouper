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
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Basic Hibernate <code>Group</code> and <code>GroupType</code> tuple DTO implementation.
 * @author  blair christensen.
 * @version $Id: GroupTypeTuple.java,v 1.12 2009-09-24 18:07:16 shilen Exp $
 * @since   @HEAD@
 */
@SuppressWarnings("serial")
public class GroupTypeTuple extends GrouperAPI {

  /**
   * 
   */
  public static final String TABLE_OLD_GROUPER_GROUPS_TYPES = "grouper_groups_types";

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: groupUUID */
  public static final String FIELD_GROUP_UUID = "groupUUID";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: typeUUID */
  public static final String FIELD_TYPE_UUID = "typeUUID";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DB_VERSION, FIELD_GROUP_UUID, FIELD_ID, FIELD_TYPE_UUID, FIELD_HIBERNATE_VERSION_NUMBER);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** */
  private String  groupUUID;
  
  /** */
  private String  id;

  /** */
  private String  typeUUID;

  /** context id of the transaction */
  private String contextId;

  /** store a reference to the group for hooks or whatnot */
  private Group group;
  
  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * try to get the current group if it is available (if this object
   * is cloned, then it might be null)
   * @param retrieveIfNull true to get from DB if null
   * @return the current group
   */
  public Group retrieveGroup(boolean retrieveIfNull) {
    if (retrieveIfNull && this.group==null) {
      this.group = GroupFinder.findByUuid(
          GrouperSession.staticGrouperSession(), this.groupUUID, true);
    }
    return this.group;
  }


  // PUBLIC CLASS METHODS //

  /**
   * @param other 
   * @return if equals
   * @since   @HEAD@
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GroupTypeTuple)) {
      return false;
    }
    GroupTypeTuple that = (GroupTypeTuple) other;
    return new EqualsBuilder()
      .append( this.groupUUID, that.groupUUID )
      .append( this.typeUUID,  that.typeUUID  )
      .isEquals();
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.groupUUID )
      .append( this.typeUUID  )
      .toHashCode();
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "groupUuid", this.getGroupUuid() )
      .append( "typeUuid",  this.getTypeUuid()  )
      .toString();
  }


  /**
   * 
   * @return uuid
   */
  public String getGroupUuid() {
    return this.groupUUID;
  }
  
  /**
   * 
   * @return id
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * type uuid
   * @return uuid
   */
  public String getTypeUuid() {
    return this.typeUUID;
  }


  /**
   * 
   * @param groupUUID1
   */
  public void setGroupUuid(String groupUUID1) {
    this.assignGroupUuid(groupUUID1, null);
  }

  /**
   * 
   * @param groupUUID1
   * @param group1 
   */
  public void assignGroupUuid(String groupUUID1, Group group1) {
    this.groupUUID = groupUUID1;
    
    //see if we need to wipe out to null
    if (group1 == null && this.group != null 
        && StringUtils.equals(this.group.getUuid(), groupUUID1)) {
      group1 = this.group;
    }

    this.group = group1;
  }

  /**
   * 
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }
  
  /**
   * 
   * @param typeUUID1
   * @return tuple
   */
  public GroupTypeTuple setTypeUuid(String typeUUID1) {
    this.typeUUID = typeUUID1;
    return this;
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public GroupTypeTuple clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
  
  /**
   * @param assignment
   * @param exceptionIfNotLegacyGroupTypeTuple
   * @return groupType
   */
  public static GroupTypeTuple internal_getGroupTypeTuple(AttributeAssign assignment, boolean exceptionIfNotLegacyGroupTypeTuple) {
    
    AttributeAssignType ownerType = assignment.getAttributeAssignType();
    if (AttributeAssignType.group == ownerType) {
      AttributeDefName attributeDefName = assignment.getAttributeDefName();
      GroupType groupType = GroupType.internal_getGroupType(attributeDefName, false);
      
      if (groupType != null) {
        Group group = assignment.getOwnerGroup();

        GroupTypeTuple gtt = new GroupTypeTuple();
        gtt.setGroupUuid(group.getId());
        gtt.assignGroupUuid(group.getId(), group);
        gtt.setTypeUuid(groupType.getUuid());
        gtt.setId(assignment.getId());
        gtt.setContextId(assignment.getContextId());
        gtt.setHibernateVersionNumber(assignment.getHibernateVersionNumber());
        
        return gtt;
      }
    }
    
    if (exceptionIfNotLegacyGroupTypeTuple) {
      throw new RuntimeException("AttributeAssign " + assignment.getId() + " is not a legacy group type assignment.");
    }
    
    return null;
  }
} 

