/**
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
 */
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

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Basic Hibernate <code>Attribute</code> DTO interface.
 * @author  blair christensen.
 * @version $Id: Attribute.java,v 1.26 2009-04-13 16:53:08 mchyzer Exp $
 * @since   @HEAD@
 */
@SuppressWarnings("serial")
public class Attribute extends GrouperAPI {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: group */
  public static final String FIELD_GROUP = "group";

  /** constant for field name for: groupUUID */
  public static final String FIELD_GROUP_UUID = "groupUUID";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: value */
  public static final String FIELD_VALUE = "value";


  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_GROUP, FIELD_GROUP_UUID, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_ID, FIELD_VALUE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: groupUUID */
  public static final String PROPERTY_GROUP_UUID = "groupUuid";

  /**
   * 
   */
  public static final String TABLE_OLD_GROUPER_ATTRIBUTES = "grouper_attributes";
  
  /** column field_id col in db */
  public static final String COLUMN_OLD_FIELD_ID = "field_id";
  
  /** column field_name col in db */
  public static final String COLUMN_OLD_FIELD_NAME = "field_name";

  /** column old_field_name col in db */
  public static final String COLUMN_OLDER_FIELD_NAME = "old_field_name";
  
  // PRIVATE INSTANCE VARIABLES //

  /** */
  private String  groupUUID;
  /** */
  private String  id;
  /** */
  private String  value;

  /** store a reference to the group for hooks or whatnot */
  private Group group;
  
  /** */
  private AttributeAssignValue attributeAssignValue;

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
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Attribute)) {
      return false;
    }
    Attribute that = (Attribute) other;
    return new EqualsBuilder()
      .append( this.id, that.id )
      .isEquals();
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.id )
      .toHashCode();
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "attrName",  this.getAttrName()  )
      .append( "groupUuid", this.getGroupUuid() )
      .append( "id",        this.getId()        )
      .append( "value",     this.getValue()     )
      .toString();
  }

  /**
   * 
   * @return attr name
   */
  public String getAttrName() {
    AttributeDefName attributeDefName = this.internal_getAttributeAssignValue().getAttributeAssign().getAttributeDefName();

    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");
    
    if (attributeDefName.getName().startsWith(stemName) && attributeDefName.getExtension().startsWith(attributePrefix)) {
      return attributeDefName.getExtension().substring(attributePrefix.length());
    }

    return null;
  }
  
  /**
   * 
   * @return group type
   */
  @SuppressWarnings("deprecation")
  public GroupType internal_getGroupType() {
    AttributeAssignValue attributeAssignValue = this.internal_getAttributeAssignValue();
    AttributeAssign attributeAssign = attributeAssignValue.getAttributeAssign();
    AttributeDef attributeDef = attributeAssign.getAttributeDef();

    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    String attributeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attributeDef.prefix");
    
    if (attributeDef.getName().startsWith(stemName) && attributeDef.getExtension().startsWith(attributeDefPrefix)) {
      String groupTypeName = attributeDef.getExtension().substring(attributeDefPrefix.length());
      return GroupTypeFinder.find(groupTypeName, true);
    }

    throw new SchemaException("Unable to find group type for attribute: " + this.getId());
  }
  
  /**
   * 
   * @return group uuid
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
   * 
   * @return value
   */
  public String getValue() {
    return this.value;
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
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /**
   * 
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public Attribute clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /** context id of the transaction */
  @GrouperIgnoreClone
  private String contextId;

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
   * 
   */
  public void delete() {
    AttributeAssignFinder.findById(id, true).delete();
  }
  
  /**
   * used for caching
   * @param attributeAssignValue1
   */
  public void internal_setAttributeAssignValue(AttributeAssignValue attributeAssignValue1) {
    this.attributeAssignValue = attributeAssignValue1;
  }
  
  /**
   * @return attributeAssignValue
   */
  public AttributeAssignValue internal_getAttributeAssignValue() {
    if (this.attributeAssignValue == null) {
      AttributeAssign assign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(id, true);
      this.attributeAssignValue = assign.getValueDelegate().retrieveValue();
    }
    
    return this.attributeAssignValue;
  }
  
  /**
   * @param value
   * @param group this is optional if the group is known
   * @param exceptionIfNotLegacyAttribute 
   * @return attribute
   */
  public static Attribute internal_getAttribute(AttributeAssignValue value, Group group, boolean exceptionIfNotLegacyAttribute) {
    
    if (value.getValueString() != null) {
      String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
      String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");
      
      AttributeAssign assignment = value.getAttributeAssign();
      AttributeDefName name = assignment.getAttributeDefName();
      
      if (name.getName().startsWith(stemName) && name.getExtension().startsWith(attributePrefix)) {
        
        if (group == null) {
          group = assignment.getOwnerAttributeAssign().getOwnerGroup();
        }
        
        Attribute attribute = new Attribute();
        attribute.setContextId(value.getContextId());
        attribute.setHibernateVersionNumber(value.getHibernateVersionNumber());
        attribute.assignGroupUuid(group.getUuid(), group);
        attribute.setId(value.getAttributeAssignId());
        attribute.setValue(value.getValueString());
        attribute.internal_setAttributeAssignValue(value);
        
        return attribute;
      }
    }
    
    if (exceptionIfNotLegacyAttribute) {
      throw new RuntimeException("AttributeAssignValue " + value.getId() + " is not for a legacy attribute.");
    }
    
    return null;
  }
} 

