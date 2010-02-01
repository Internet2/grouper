/**
 * 
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeAssignValue;
import edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple;


/**
 * value of an attribute assignment (could be multi-valued based on the attributeDef
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class AttributeAssignValue extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, XmlImportableMultiple<AttributeAssignValue> {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeAssignValue.class);

  /** name of the groups attribute def table in the db */
  public static final String TABLE_GROUPER_ATTRIBUTE_ASSIGN_VALUE = "grouper_attribute_assign_value";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_VALUE_STRING = "value_string";

  /** column */
  public static final String COLUMN_VALUE_INTEGER = "value_integer";

  /** column */
  public static final String COLUMN_VALUE_MEMBER_ID = "value_member_id";

  /** column */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_ID = "attribute_assign_id";

  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeAssignId */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ID = "attributeAssignId";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: valueInteger */
  public static final String FIELD_VALUE_INTEGER = "valueInteger";

  /** constant for field name for: valueMemberId */
  public static final String FIELD_VALUE_MEMBER_ID = "valueMemberId";

  /** constant for field name for: valueString */
  public static final String FIELD_VALUE_STRING = "valueString";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_ASSIGN_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_ID, 
      FIELD_LAST_UPDATED_DB, FIELD_VALUE_INTEGER, FIELD_VALUE_MEMBER_ID, FIELD_VALUE_STRING);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_ASSIGN_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_ID, FIELD_LAST_UPDATED_DB, FIELD_VALUE_INTEGER, FIELD_VALUE_MEMBER_ID, 
      FIELD_VALUE_STRING);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeAssignValue clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /** attribute assignment in this value assignment */
  private String attributeAssignId;

  /** id of this attribute def */
  private String id;

  /** string value */
  private String valueString;

  /** integer value */
  private Long valueInteger;

  /** member id value */
  private String valueMemberId;

  /** context id of the transaction */
  private String contextId;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getAttributeAssignValue().saveOrUpdate(this);
  }
  
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
   * id of this attribute def
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * id of this attribute def
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
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
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
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
   * attribute assignment in this value assignment
   * @return the attributeNameId
   */
  public String getAttributeAssignId() {
    return this.attributeAssignId;
  }

  
  /**
   * attribute assignment in this value assignment
   * @param attributeAssignId1 the attributeNameId to set
   */
  public void setAttributeAssignId(String attributeAssignId1) {
    this.attributeAssignId = attributeAssignId1;
  }

  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    long now = System.currentTimeMillis();
    this.setCreatedOnDb(now);
    this.setLastUpdatedDb(now);
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
   * string value
   * @return the valueString
   */
  public String getValueString() {
    return valueString;
  }

  
  /**
   * string value
   * @param valueString1 the valueString to set
   */
  public void setValueString(String valueString1) {
    this.valueString = valueString1;
  }

  
  /**
   * integer value
   * @return the valueInteger
   */
  public Long getValueInteger() {
    return this.valueInteger;
  }

  
  /**
   * integer value
   * @param valueInteger1 the valueInteger to set
   */
  public void setValueInteger(Long valueInteger1) {
    this.valueInteger = valueInteger1;
  }

  
  /**
   * memberId value (for subjects)
   * @return the valueMemberId
   */
  public String getValueMemberId() {
    return valueMemberId;
  }

  
  /**
   * memberId value (for subjects)
   * @param valueMemberId1 the valueMemberId to set
   */
  public void setValueMemberId(String valueMemberId1) {
    this.valueMemberId = valueMemberId1;
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAttributeAssignValue xmlToExportAttributeAssignValue(GrouperVersion grouperVersion) {
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportAttributeAssignValue xmlExportAttributeAssignValue = new XmlExportAttributeAssignValue(); 
    
    xmlExportAttributeAssignValue.setAttributeAssignId(this.getAttributeAssignId());
    xmlExportAttributeAssignValue.setContextId(this.getContextId());
    xmlExportAttributeAssignValue.setCreateTime(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportAttributeAssignValue.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportAttributeAssignValue.setModifierTime(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    xmlExportAttributeAssignValue.setUuid(this.getId());
    xmlExportAttributeAssignValue.setValueInteger(this.getValueInteger());
    xmlExportAttributeAssignValue.setValueMemberId(this.getValueMemberId());
    xmlExportAttributeAssignValue.setValueString(this.getValueString());
    
    return xmlExportAttributeAssignValue;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple#xmlRetrieveByIdOrKey(java.util.Collection)
   */
  public AttributeAssignValue xmlRetrieveByIdOrKey(Collection<String> idsToIgnore) {
    return GrouperDAOFactory.getFactory().getAttributeAssignValue().findByUuidOrKey(idsToIgnore,
        this.id, this.attributeAssignId, false, this.valueInteger, this.valueMemberId, this.valueString);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(AttributeAssignValue existingRecord) {
    existingRecord.setAttributeAssignId(this.attributeAssignId);
    existingRecord.setId(this.id);
    existingRecord.setValueInteger(this.valueInteger);
    existingRecord.setValueMemberId(this.valueMemberId);
    existingRecord.setValueString(this.valueString);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(AttributeAssignValue other) {
    if (!StringUtils.equals(this.attributeAssignId, other.attributeAssignId)) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!GrouperUtil.equals(this.valueInteger, other.valueInteger)) {
      return true;
    }
    if (!StringUtils.equals(this.valueMemberId, other.valueMemberId)) {
      return true;
    }
    if (!StringUtils.equals(this.valueString, other.valueString)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(AttributeAssignValue other) {
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
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveBusinessProperties(java.lang.Object)
   */
  public AttributeAssignValue xmlSaveBusinessProperties(AttributeAssignValue existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      //TODO user business method once it exists
      existingRecord = new AttributeAssignValue();
      existingRecord.setId(this.id);
      existingRecord.setAttributeAssignId(this.attributeAssignId);
      existingRecord.setValueInteger(this.valueInteger);
      existingRecord.setValueMemberId(this.valueMemberId);
      existingRecord.setValueString(this.valueString);
      existingRecord.saveOrUpdate();
    }

    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.saveOrUpdate();
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAttributeAssignValue().saveUpdateProperties(this);
  }
  
  /**
   * delete this record
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getAttributeAssignValue().delete(this);
  }

}
