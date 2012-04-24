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
import java.io.StringWriter;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.AttributeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttribute;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;

/**
 * Basic Hibernate <code>Attribute</code> DTO interface.
 * @author  blair christensen.
 * @version $Id: Attribute.java,v 1.26 2009-04-13 16:53:08 mchyzer Exp $
 * @since   @HEAD@
 */
@SuppressWarnings("serial")
public class Attribute extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, XmlImportable<Attribute> {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";

  /** constant for field name for: group */
  public static final String FIELD_GROUP = "group";

  /** constant for field name for: groupUUID */
  public static final String FIELD_GROUP_UUID = "groupUUID";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: value */
  public static final String FIELD_VALUE = "value";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_FIELD_ID, FIELD_GROUP, FIELD_GROUP_UUID, FIELD_ID, 
      FIELD_VALUE);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_FIELD_ID, FIELD_GROUP, FIELD_GROUP_UUID, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_ID, FIELD_VALUE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: groupUUID */
  public static final String PROPERTY_GROUP_UUID = "groupUuid";
  
  // PRIVATE INSTANCE VARIABLES //
  
  /** id of the field which is the attribute name */
  private String  fieldId;

  /** */
  private String  groupUUID;
  /** */
  private String  id;
  /** */
  private String  value;

  /** store a reference to the group for hooks or whatnot */
  private Group group;

  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersion()
   */
  @Override
  public Attribute dbVersion() {
    return (Attribute)this.dbVersion;
  }

  /**
   * @param failIfNull 
   * @return the set of different fields
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  public Set<String> dbVersionDifferentFields(boolean failIfNull) {
    if (this.dbVersion == null) {
      if (failIfNull) {
        throw new RuntimeException("State was never stored from db");
      }
      return null;
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);

    return result;
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

  /** context id of the transaction */
  @GrouperIgnoreDbVersion
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
   * store this object to the DB.
   */
  public void store() {    
    GrouperDAOFactory.getFactory().getAttribute().createOrUpdate(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(Attribute existingRecord) {
    
    existingRecord.setFieldId(existingRecord.getFieldId());
    existingRecord.setGroupUuid(existingRecord.getGroupUuid());
    existingRecord.setId(existingRecord.getId());
    existingRecord.setValue(existingRecord.getValue());
    
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(Attribute other) {
    if (!StringUtils.equals(this.fieldId, other.fieldId)) {
      return true;
    }
    if (!StringUtils.equals(this.groupUUID, other.groupUUID)) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!StringUtils.equals(this.value, other.value)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(Attribute other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public Attribute xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getAttribute().findByUuidOrName(this.id, this.groupUUID, this.fieldId, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public Attribute xmlSaveBusinessProperties(Attribute existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.groupUUID, true);
      Field field = FieldFinder.findById(this.fieldId, true);
      existingRecord = group.internal_setAttribute(field.getName(), this.value, false, this.id);
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.store();
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAttribute().saveUpdateProperties(this);
  }

  /**
   * 
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getAttribute().delete(this);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAttribute xmlToExportAttribute(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportAttribute xmlExportAttribute = new XmlExportAttribute();
    
    xmlExportAttribute.setContextId(this.getContextId());
    xmlExportAttribute.setFieldId(this.getFieldId());
    xmlExportAttribute.setGroupId(this.getGroupUuid());
    xmlExportAttribute.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportAttribute.setUuid(this.getId());
    xmlExportAttribute.setValue(this.getValue());
    
    return xmlExportAttribute;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlGetId()
   */
  public String xmlGetId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setId(theId);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    stringWriter.write("Attribute: " + this.id + ", ");
    
//    XmlExportUtils.toStringField(stringWriter, this.fieldId, false);
    
    return stringWriter.toString();
    
  }

} 

