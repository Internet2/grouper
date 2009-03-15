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

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.AttributeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Basic Hibernate <code>Attribute</code> DTO interface.
 * @author  blair christensen.
 * @version $Id: Attribute.java,v 1.23 2009-03-15 06:37:21 mchyzer Exp $
 * @since   @HEAD@
 */
@SuppressWarnings("serial")
@GrouperIgnoreDbVersion
public class Attribute extends GrouperAPI implements Hib3GrouperVersioned {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";

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
      FIELD_DB_VERSION, FIELD_FIELD_ID, FIELD_GROUP_UUID, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_ID, FIELD_VALUE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  // PRIVATE INSTANCE VARIABLES //
  
  /** id of the field which is the attribute name */
  private String  fieldId;

  /** */
  private String  groupUUID;
  /** */
  private String  id;
  /** */
  private String  value;

  /**
   * 
   */
  public static final String TABLE_GROUPER_ATTRIBUTES = "grouper_attributes";

  /** column field_id col in db */
  public static final String COLUMN_FIELD_ID = "field_id";

  /** column field_name col in db */
  public static final String COLUMN_FIELD_NAME = "field_name";

  /** column old_field_name col in db */
  public static final String COLUMN_OLD_FIELD_NAME = "old_field_name";

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
      .append( this.fieldId,  that.fieldId  )
      .append( this.groupUUID, that.groupUUID )
      .append( this.value,     that.value     )
      .isEquals();
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.fieldId  )
      .append( this.groupUUID )
      .append( this.value    )
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
    if (StringUtils.isBlank(this.fieldId)) {
      return null;
    }
    Field field = FieldFinder.findById(this.fieldId, true);
    if (!field.isAttributeName()) {
      throw new RuntimeException("Field is not an attribute name, id: " + this.fieldId
          + ", instead it is: " + field.getTypeString());
    }
    return field.getName();
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
   * @param groupUUID
   */
  public void setGroupUuid(String groupUUID) {
    this.groupUUID = groupUUID;
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

  /**
   * id of the field which is the attribute name
   * @return the field id
   */
  public String getFieldId() {
    return fieldId;
  }

  /**
   * id of the field which is the attribute name
   * @param fieldId1
   */
  public void setFieldId(String fieldId1) {
    this.fieldId = fieldId1;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);
  
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE, 
        AttributeHooks.METHOD_ATTRIBUTE_POST_COMMIT_DELETE, HooksAttributeBean.class, 
        this, Attribute.class);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE, 
        AttributeHooks.METHOD_ATTRIBUTE_POST_DELETE, HooksAttributeBean.class, 
        this, Attribute.class, VetoTypeGrouper.ATTRIBUTE_POST_DELETE, false, true);
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {

    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE, 
        AttributeHooks.METHOD_ATTRIBUTE_POST_INSERT, HooksAttributeBean.class, 
        this, Attribute.class, VetoTypeGrouper.ATTRIBUTE_POST_INSERT, true, false);
  
    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE, 
        AttributeHooks.METHOD_ATTRIBUTE_POST_COMMIT_INSERT, HooksAttributeBean.class, 
        this, Attribute.class);
  

  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
    
    super.onPostUpdate(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE, 
        AttributeHooks.METHOD_ATTRIBUTE_POST_COMMIT_UPDATE, HooksAttributeBean.class, 
        this, Attribute.class);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE, 
        AttributeHooks.METHOD_ATTRIBUTE_POST_UPDATE, HooksAttributeBean.class, 
        this, Attribute.class, VetoTypeGrouper.ATTRIBUTE_POST_UPDATE, true, false);
  
  
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE, 
        AttributeHooks.METHOD_ATTRIBUTE_PRE_DELETE, HooksAttributeBean.class, 
        this, Attribute.class, VetoTypeGrouper.ATTRIBUTE_PRE_DELETE, false, false);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE, 
        AttributeHooks.METHOD_ATTRIBUTE_PRE_INSERT, HooksAttributeBean.class, 
        this, Attribute.class, VetoTypeGrouper.ATTRIBUTE_PRE_INSERT, false, false);
    
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE, 
        AttributeHooks.METHOD_ATTRIBUTE_PRE_UPDATE, HooksAttributeBean.class, 
        this, Attribute.class, VetoTypeGrouper.ATTRIBUTE_PRE_UPDATE, false, false);
  
  }

} 

