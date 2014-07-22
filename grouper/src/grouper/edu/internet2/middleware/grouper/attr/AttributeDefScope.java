/**
 * Copyright 2014 Internet2
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
/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeDefScope;
import edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple;


/**
 * clamp down an attribute def to a set of scopes which are like strings in the DB.
 * could be a group/stem name, or 
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class AttributeDefScope extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, XmlImportableMultiple<AttributeDefScope> {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeAssign.class);

  /** name of the groups attribute def table in the db */
  public static final String TABLE_GROUPER_ATTRIBUTE_DEF_SCOPE = "grouper_attribute_def_scope";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_ID = "attribute_def_id";

  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_SCOPE_TYPE = "attribute_def_scope_type";

  /** column */
  public static final String COLUMN_SCOPE_STRING = "scope_string";

  /** column */
  public static final String COLUMN_SCOPE_STRING2 = "scope_string2";

  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeDefId */
  public static final String FIELD_ATTRIBUTE_DEF_ID = "attributeDefId";

  /** constant for field name for: attributeDefScopeType */
  public static final String FIELD_ATTRIBUTE_DEF_SCOPE_TYPE = "attributeDefScopeType";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: scopeString */
  public static final String FIELD_SCOPE_STRING = "scopeString";

  /** constant for field name for: scopeString2 */
  public static final String FIELD_SCOPE_STRING2 = "scopeString2";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_ATTRIBUTE_DEF_SCOPE_TYPE, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, 
      FIELD_ID, FIELD_LAST_UPDATED_DB, FIELD_SCOPE_STRING, FIELD_SCOPE_STRING2);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_ATTRIBUTE_DEF_SCOPE_TYPE, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_LAST_UPDATED_DB, FIELD_SCOPE_STRING, 
      FIELD_SCOPE_STRING2);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** id of this scope */
  private String id;

  /** id of the attribute def */
  private String attributeDefId;
  
  /** scope string, either a group or stem name or like string or something */
  private String scopeString;
  
  /** scope string information 2 (whatever it is used for) */
  private String scopeString2;
  
  /** type of scope */
  private AttributeDefScopeType attributeDefScopeType;

  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

  /**
   * context id of the transaction 
   */
  private String contextId;

  /**
   * id of this scope
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * id of this scope
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * scope string, either a group or stem name or like string or something
   * @return scope string
   */
  public String getScopeString() {
    return scopeString;
  }

  /**
   * scope string, either a group or stem name or like string or something
   * @param scopeString1
   */
  public void setScopeString(String scopeString1) {
    this.scopeString = scopeString1;
  }

  /**
   * id of the attribute def
   * @return the id
   */
  public String getAttributeDefId() {
    return attributeDefId;
  }

  /**
   * id of the attribute def
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * type of scope
   * @return the type of scope
   */
  public AttributeDefScopeType getAttributeDefScopeType() {
    return this.attributeDefScopeType;
  }

  /**
   * type of scope
   * @param attributeDefScopeType1
   * 
   */
  public void setAttributeDefScopeType(AttributeDefScopeType attributeDefScopeType1) {
    this.attributeDefScopeType = attributeDefScopeType1;
  }
  
  /**
   * type of scope
   * @return the type of scope
   */
  public String getAttributeDefScopeTypeDb() {
    return this.attributeDefScopeType == null ? null : this.attributeDefScopeType.name();
  }

  /**
   * type of scope
   * @param theAttributeDefScopeType1
   * 
   */
  public void setAttributeDefScopeTypeDb(String theAttributeDefScopeType1) {
    this.attributeDefScopeType = AttributeDefScopeType.valueOfIgnoreCase(theAttributeDefScopeType1, false);
  }

  
  /**
   * scope string information 2 (whatever it is used for)
   * @return the scopeString2
   */
  public String getScopeString2() {
    return scopeString2;
  }

  
  /**
   * scope string information 2 (whatever it is used for)
   * @param scopeString2 the scopeString2 to set
   */
  public void setScopeString2(String scopeString2) {
    this.scopeString2 = scopeString2;
  }
  
  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getAttributeDefScope().saveOrUpdate(this);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeDefScope clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(AttributeDefScope existingRecord) {
    existingRecord.setAttributeDefId(this.attributeDefId);
    existingRecord.setAttributeDefScopeType(this.attributeDefScopeType);
    existingRecord.setId(this.getId());
    existingRecord.setScopeString(this.scopeString);
    existingRecord.setScopeString2(this.scopeString2);

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(AttributeDefScope other) {
    if (!StringUtils.equals(this.attributeDefId, other.attributeDefId)) {
      return true;
    }
    if (this.attributeDefScopeType != other.attributeDefScopeType) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!GrouperUtil.equals(this.scopeString, other.scopeString)) {
      return true;
    }
    if (!StringUtils.equals(this.scopeString2, other.scopeString2)) {
      return true;
    }

    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(AttributeDefScope other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.createdOnDb, other.createdOnDb)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    if (!GrouperUtil.equals(this.lastUpdatedDb, other.lastUpdatedDb)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple#xmlSaveBusinessProperties(java.lang.Object)
   */
  public AttributeDefScope xmlSaveBusinessProperties(AttributeDefScope existingRecord) {

    //if its an insert, call the business method
    if (existingRecord == null) {
      //TODO user business method once it exists
      existingRecord = new AttributeDefScope();
      existingRecord.setId(this.id);
      existingRecord.setAttributeDefId(this.getAttributeDefId());
      existingRecord.setAttributeDefScopeType(this.attributeDefScopeType);
      existingRecord.setScopeString(this.scopeString);
      existingRecord.setScopeString2(this.scopeString2);
      existingRecord.saveOrUpdate();
    }

    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.saveOrUpdate();
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAttributeDefScope().saveUpdateProperties(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple#xmlRetrieveByIdOrKey(java.util.Collection)
   */
  public AttributeDefScope xmlRetrieveByIdOrKey(Collection<String> idsToIgnore) {
    return GrouperDAOFactory.getFactory().getAttributeDefScope().findByUuidOrKey(idsToIgnore,
        this.id, this.attributeDefId, this.getAttributeDefScopeTypeDb(), false, this.getScopeString());
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAttributeDefScope xmlToExportAttributeDefScope(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportAttributeDefScope xmlExportAttributeDefScope = new XmlExportAttributeDefScope();
    
    xmlExportAttributeDefScope.setAttributeDefId(this.getAttributeDefId());
    xmlExportAttributeDefScope.setAttributeDefScopeType(this.getAttributeDefScopeTypeDb());
    xmlExportAttributeDefScope.setContextId(this.getContextId());
    xmlExportAttributeDefScope.setCreateTime(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportAttributeDefScope.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportAttributeDefScope.setModifierTime(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    xmlExportAttributeDefScope.setScopeString(this.getScopeString());
    xmlExportAttributeDefScope.setScopeString2(this.getScopeString2());
    xmlExportAttributeDefScope.setUuid(this.getId());
    
    return xmlExportAttributeDefScope;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlGetId()
   */
  public String xmlGetId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setId(theId);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("AttributeDefScope: " + this.getId() + ", scopeString: " + this.scopeString + ", ");

//    XmlExportUtils.toStringAttributeDef(null, stringWriter, this.getAttributeDefId(), false);
    
    return stringWriter.toString();
    
  }

  /**
   * Delete this group from the Groups Registry.
   * <pre class="eg">
   * try {
   *   g.delete();
   * }
   * catch (GroupDeleteException e0) {
   *   // Unable to delete group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this group
   * }
   * </pre>
   * @throws  GroupDeleteException
   * @throws  InsufficientPrivilegeException
   */
  public void delete() throws GroupDeleteException, InsufficientPrivilegeException {
    GrouperDAOFactory.getFactory().getAttributeDefScope().delete(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    if (this.createdOnDb == null) {
      this.setCreatedOnDb(System.currentTimeMillis());
    }
    this.setLastUpdatedDb(System.currentTimeMillis());
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.setLastUpdatedDb(System.currentTimeMillis());
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "type", this.attributeDefScopeType)
      .append( "scopeString", this.scopeString )
      .append( "scopeString2", this.scopeString2 )
      .toString();
    
  }
  
  
}
