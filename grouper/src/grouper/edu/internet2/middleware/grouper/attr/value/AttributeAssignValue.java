/**
 * 
 */
package edu.internet2.middleware.grouper.attr.value;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
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
  public static final String COLUMN_VALUE_FLOATING = "value_floating";

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

  /** constant for field name for: valueFloating */
  public static final String FIELD_VALUE_FLOATING = "valueFloating";

  /** constant for field name for: valueInteger */
  public static final String FIELD_VALUE_INTEGER = "valueInteger";

  /** constant for field name for: valueMemberId */
  public static final String FIELD_VALUE_MEMBER_ID = "valueMemberId";

  /** constant for field name for: valueString */
  public static final String FIELD_VALUE_STRING = "valueString";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_ASSIGN_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_ID, 
      FIELD_LAST_UPDATED_DB, FIELD_VALUE_INTEGER, FIELD_VALUE_MEMBER_ID, FIELD_VALUE_STRING,
      FIELD_VALUE_FLOATING);

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

  /** floating point value */
  private Double valueFloating;

  /** integer value */
  private Long valueInteger;

  /**
   * floating point value
   * @return floating point value
   */
  public Double getValueFloating() {
    return this.valueFloating;
  }

  /**
   * floating point value
   * @param valueFloating1
   */
  public void setValueFloating(Double valueFloating1) {
    this.valueFloating = valueFloating1;
  }

  /**
   * assign a value to any type
   * @param value
   */
  public void assignValue(String value) {
    
    AttributeAssign attributeAssign = this.getAttributeAssign();
    AttributeDef attributeDef = attributeAssign.getAttributeDef();
    
    AttributeDefValueType attributeDefValueType = attributeDef.getValueType();
    
    this.clearValue();
    
    if (StringUtils.isBlank(value)) {
      return;
    }
    
    switch(attributeDefValueType) {
      case floating:
        this.valueFloating = GrouperUtil.doubleValue(value);
        break;
      case integer:
        this.valueInteger = GrouperUtil.longValue(value);
        break;
      case marker:
        throw new RuntimeException("Cant assign a value to a marker attribute: " 
            + value + ", " + this.attributeAssignId); 
      case memberId:
        this.valueMemberId = value;
        break;
      case string:
        this.valueString = value;
        break;
      default:
        throw new RuntimeException("Not expecting type: " + attributeDefValueType);
    }
  }

  /**
   * whatever the type, return the string value
   * @return value
   */
  public String valueString() {
    return valueString(false);
  }
  
  /**
   * whatever the type, return the string value
   * @param convertTimestampToFriendly true to convert timestamps to yyyy/MM/dd HH:mm:ss.SSS 
   * as opposed to numbers of millis since 1970
   * @return value
   */
  public String valueString(boolean convertTimestampToFriendly) {
    
    AttributeAssign attributeAssign = this.getAttributeAssign();
    AttributeDef attributeDef = attributeAssign.getAttributeDef();
    
    AttributeDefValueType attributeDefValueType = attributeDef.getValueType();
    
    switch(attributeDefValueType) {
      case floating:
        return this.valueFloating == null ? null : this.valueFloating.toString();
      case integer:
        return this.valueInteger == null ? null : this.valueInteger.toString();
      case marker:
        throw new RuntimeException("Why would a marker attribute have a value? " + this);
      case memberId:
        return this.valueMemberId;
      case string:
        return this.valueString;
      case timestamp:
        if (this.valueInteger == null) {
          return null;
        }
        if (convertTimestampToFriendly) {
          return dateToString(new Timestamp(this.valueInteger));
        }
        return this.valueInteger.toString();
      default:
        throw new RuntimeException("Not expecting type: " + attributeDefValueType);
    }
  }

  /**
   * Note, this is 
   * web service format string
   */
  private static final String WS_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

  /**
   * Note, this is 
   * web service format string
   */
  private static final String WS_DATE_FORMAT2 = "yyyy/MM/dd_HH:mm:ss.SSS";

  /**
   * convert a date to a string using the standard web service pattern
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * 
   * @param date
   * @return the string, or null if the date is null
   */
  public static String dateToString(Date date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(WS_DATE_FORMAT);
    return simpleDateFormat.format(date);
  }

  /**
   * convert a string to a date using the standard web service pattern Note
   * that HH is 0-23
   * 
   * @param dateString
   * @return the string, or null if the date was null
   */
  public static Date stringToDate(String dateString) {
    if (StringUtils.isBlank(dateString)) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(WS_DATE_FORMAT);
    try {
      return simpleDateFormat.parse(dateString);
    } catch (ParseException e) {
      SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(WS_DATE_FORMAT2);
      try {
        return simpleDateFormat2.parse(dateString);
      } catch (ParseException e2) {
        throw new RuntimeException("Cannot convert '" + dateString
            + "' to a date based on format: " + WS_DATE_FORMAT, e);
      }
    }
  }

  

  
  /**
   * clear all the values
   */
  public void clearValue() {
    this.valueFloating = null;
    this.valueInteger = null;
    this.valueMemberId = null;
    this.valueString = null;
    
  }
  
  /**
   * clear all the values
   * @param attributeAssignValue 
   */
  public void assignValue(AttributeAssignValue attributeAssignValue) {
    this.valueFloating = attributeAssignValue.valueFloating;
    this.valueInteger = attributeAssignValue.valueInteger;
    this.valueMemberId = attributeAssignValue.valueMemberId;
    this.valueString = attributeAssignValue.valueString;
  }
  
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
    
    if (StringUtils.isBlank(this.id)) {
      this.id = GrouperUuid.getUuid();
    }
    
    final boolean isInsert = ObjectUtils.equals(this.getHibernateVersionNumber(), GrouperAPI.INITIAL_VERSION_NUMBER);

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            String differences = null;
            if (!hibernateHandlerBean.isCallerWillCreateAudit() && !isInsert) {
              differences = GrouperUtil.dbVersionDescribeDifferences(AttributeAssignValue.this.dbVersion(), 
                  AttributeAssignValue.this, AttributeAssignValue.this.dbVersion() != null ? AttributeAssignValue.this.dbVersionDifferentFields() : AttributeAssignValue.CLONE_FIELDS);
            }
            
            if (!isInsert) {
              // delete and re-add the row if values change
              if (AttributeAssignValue.this.dbVersionDifferentFields().contains(FIELD_VALUE_INTEGER) ||
                  AttributeAssignValue.this.dbVersionDifferentFields().contains(FIELD_VALUE_FLOATING) ||
                  AttributeAssignValue.this.dbVersionDifferentFields().contains(FIELD_VALUE_STRING) ||
                  AttributeAssignValue.this.dbVersionDifferentFields().contains(FIELD_VALUE_MEMBER_ID)) {
               
                GrouperDAOFactory.getFactory().getAttributeAssignValue().delete(AttributeAssignValue.this);
                AttributeAssignValue.this.id = GrouperUuid.getUuid();
                AttributeAssignValue.this.createdOnDb = null;
                AttributeAssignValue.this.lastUpdatedDb = null;
                AttributeAssignValue.this.setHibernateVersionNumber(-1L);
              }
            }
    
            GrouperDAOFactory.getFactory().getAttributeAssignValue().saveOrUpdate(AttributeAssignValue.this);
            
            AttributeAssign attributeAssign = AttributeAssignValue.this.getAttributeAssign();
            AttributeDefName attributeDefName = attributeAssign.getAttributeDefName();
            
            AuditEntry auditEntry = new AuditEntry(
                isInsert ? AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD : AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_UPDATE, 
                    "id", 
                AttributeAssignValue.this.getId(), "attributeAssignId", AttributeAssignValue.this.getAttributeAssignId(), 
                "attributeDefNameId", attributeAssign.getAttributeDefNameId(), 
                "value", AttributeAssignValue.this.valueString(), "attributeDefNameName", attributeDefName.getName());

            if (isInsert) {
              
              auditEntry.setDescription("Added attribute assignment value");

            } else {

              auditEntry.setDescription("Updated attribute assignment value: " + differences);
              
            }
            auditEntry.saveOrUpdate(true);
            return null;
          }
        });
            
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
    return this.id;
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
   * get the attribute assign
   * @return the attribute assign
   */
  public AttributeAssign getAttributeAssign() {
    if (StringUtils.isBlank(this.attributeAssignId)) {
      return null;
    }
    //hopefully this is cached
    return GrouperDAOFactory.getFactory().getAttributeAssign().findById(this.attributeAssignId, true);
  }
  
  /**
   * attribute assignment in this value assignment
   * @param attributeAssignId1 the attributeNameId to set
   */
  public void setAttributeAssignId(String attributeAssignId1) {
    this.attributeAssignId = attributeAssignId1;
  }
  
  /**
   * string value
   * @return the valueString
   */
  public String getValueString() {
    return this.valueString;
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
    return this.valueMemberId;
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
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
  
              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
  
                GrouperDAOFactory.getFactory().getAttributeAssignValue().delete(AttributeAssignValue.this);
  
                if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                  AttributeAssign attributeAssign = AttributeAssignValue.this.getAttributeAssign();
                  AttributeDefName attributeDefName = attributeAssign.getAttributeDefName();
                  AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE, 
                          "id", 
                      AttributeAssignValue.this.getId(), "attributeAssignId", AttributeAssignValue.this.getAttributeAssignId(), 
                      "attributeDefNameId", attributeAssign.getAttributeDefNameId(), 
                      "value", AttributeAssignValue.this.valueString(), "attributeDefNameName", attributeDefName.getName());
                  auditEntry.setDescription("Deleted attributeAssignValue: " + AttributeAssignValue.this.getId());
                  auditEntry.saveOrUpdate(true);
                }
                return null;
          }});
    } catch (RuntimeException e) {
      GrouperUtil.injectInException(e, " Problem deleting attribute assignValue: " + this + " ");
      throw e;
    }
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "AttributeAssignValue.id#" + this.getId();
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
    
    stringWriter.write("AttributeAssignValue: " + this.getId());

//    XmlExportUtils.toStringAttributeAssignValue(stringWriter, this, false);
    
    return stringWriter.toString();
    
  }

  /**
   * if the argument has the same value as this
   * @param attributeAssignValue
   * @return if the argument has the same value as this
   */
  public boolean sameValue(AttributeAssignValue attributeAssignValue) {
    if (attributeAssignValue == null) {
      return false;
    }
    if (!GrouperUtil.equals(this.valueMemberId, attributeAssignValue.valueMemberId)) {
      return false;
    }
    if (!GrouperUtil.equals(this.valueString, attributeAssignValue.valueString)) {
      return false;
    }
    if (!GrouperUtil.equals(this.valueFloating, attributeAssignValue.valueFloating)) {
      return false;
    }
    if (!GrouperUtil.equals(this.valueInteger, attributeAssignValue.valueInteger)) {
      return false;
    }
    return true;
  }
 
  /**
   * 
   */
  public static enum AttributeAssignValueType {
    
    /** has an integer value */
    integerValue {

      /**
       * 
       * @see edu.internet2.middleware.grouper.attr.value.AttributeAssignValue.AttributeAssignValueType#compatibleWith(edu.internet2.middleware.grouper.attr.AttributeDefValueType)
       */
      @Override
      public boolean compatibleWith(AttributeDefValueType attributeDefValueType) {
        return attributeDefValueType == AttributeDefValueType.integer 
          || attributeDefValueType == AttributeDefValueType.timestamp;
      }
      
    },
    
    /** has a floating value */
    floating {

      /**
       * 
       * @see edu.internet2.middleware.grouper.attr.value.AttributeAssignValue.AttributeAssignValueType#compatibleWith(edu.internet2.middleware.grouper.attr.AttributeDefValueType)
       */
      @Override
      public boolean compatibleWith(AttributeDefValueType attributeDefValueType) {
        return attributeDefValueType == AttributeDefValueType.floating;
      }
      
    },
    
    /** has a string value */
    string {

      /**
       * 
       * @see edu.internet2.middleware.grouper.attr.value.AttributeAssignValue.AttributeAssignValueType#compatibleWith(edu.internet2.middleware.grouper.attr.AttributeDefValueType)
       */
      @Override
      public boolean compatibleWith(AttributeDefValueType attributeDefValueType) {
        return attributeDefValueType == AttributeDefValueType.string;
      }
      
    },
    
    /** has a member id */
    memberId {

      /**
       * 
       * @see edu.internet2.middleware.grouper.attr.value.AttributeAssignValue.AttributeAssignValueType#compatibleWith(edu.internet2.middleware.grouper.attr.AttributeDefValueType)
       */
      @Override
      public boolean compatibleWith(AttributeDefValueType attributeDefValueType) {
        return attributeDefValueType == AttributeDefValueType.memberId;
      }
      
    },
    
    /** doesnt have a value */
    nullValue {

      /**
       * 
       * @see edu.internet2.middleware.grouper.attr.value.AttributeAssignValue.AttributeAssignValueType#compatibleWith(edu.internet2.middleware.grouper.attr.AttributeDefValueType)
       */
      @Override
      public boolean compatibleWith(AttributeDefValueType attributeDefValueType) {
        //this is ok for all types
        return true;
      }
      
    },
    
    /** has multi values, thats bad */
    multiValueError {

      /**
       * 
       * @see edu.internet2.middleware.grouper.attr.value.AttributeAssignValue.AttributeAssignValueType#compatibleWith(edu.internet2.middleware.grouper.attr.AttributeDefValueType)
       */
      @Override
      public boolean compatibleWith(AttributeDefValueType attributeDefValueType) {
        //this is bad
        return false;
      }
      
    };
    
    /**
     * 
     * @param attributeDefValueType
     * @return true if the value type is compatible with the def type
     */
    public abstract boolean compatibleWith(AttributeDefValueType attributeDefValueType);
    
  }

  /**
   * get the type of this value
   * @return the type of this value
   */
  public AttributeAssignValueType getCurrentAssignValueType() {
    int valueCount = 0;
    valueCount += this.valueFloating != null ? 1 : 0;
    valueCount += this.valueInteger != null ? 1 : 0;
    valueCount += this.valueMemberId != null ? 1 : 0;
    valueCount += !StringUtils.isEmpty(this.valueString) ? 1 : 0;
    if (valueCount > 1) {
      return AttributeAssignValueType.multiValueError;
    }
    if (valueCount == 0) {
      return AttributeAssignValueType.nullValue;
    }
    if (this.valueFloating != null) {
      return AttributeAssignValueType.floating;
    }
    if (this.valueInteger != null) {
      return AttributeAssignValueType.integerValue;
    }
    if (this.valueMemberId != null) {
      return AttributeAssignValueType.memberId;
    }
    if (!StringUtils.isEmpty(this.valueString)) {
      return AttributeAssignValueType.string;
    }
    throw new RuntimeException("Why are we here? " + this);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);
  
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE_ASSIGN_VALUE, 
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_COMMIT_DELETE, HooksAttributeAssignValueBean.class, 
        this, AttributeAssignValue.class);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN_VALUE, 
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_DELETE, HooksAttributeAssignValueBean.class, 
        this, AttributeAssignValue.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_POST_DELETE, false, true);
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
  
    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN_VALUE, 
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_INSERT, HooksAttributeAssignValueBean.class, 
        this, AttributeAssignValue.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_POST_INSERT, true, false);
  
    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE_ASSIGN_VALUE, 
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_COMMIT_INSERT, HooksAttributeAssignValueBean.class, 
        this, AttributeAssignValue.class);
  
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
    
    super.onPostUpdate(hibernateSession);
    
    this.setLastUpdatedDb(System.currentTimeMillis());

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE_ASSIGN_VALUE, 
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_COMMIT_UPDATE, HooksAttributeAssignValueBean.class, 
        this, AttributeAssignValue.class);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN_VALUE, 
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_UPDATE, HooksAttributeAssignValueBean.class, 
        this, AttributeAssignValue.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_POST_UPDATE, true, false);
  
  
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
  
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE, 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id.name(), this.getId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeAssignId.name(), this.getAttributeAssignId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameId.name(), this.getAttributeAssign().getAttributeDefNameId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName.name(), this.getAttributeAssign().getAttributeDefName().getName(),
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.value.name(), this.dbVersion().valueString(),
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.valueType.name(), this.getAttributeAssign().getAttributeDef().getValueType().name()).save();
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN_VALUE, 
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_PRE_DELETE, HooksAttributeAssignValueBean.class, 
        this, AttributeAssignValue.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_PRE_DELETE, false, false);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    long now = System.currentTimeMillis();
    if (this.createdOnDb == null) {
      this.setCreatedOnDb(now);
    }
    this.setLastUpdatedDb(now);
    
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD, 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id.name(), this.getId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId.name(), this.getAttributeAssignId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameId.name(), this.getAttributeAssign().getAttributeDefNameId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName.name(), this.getAttributeAssign().getAttributeDefName().getName(),
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value.name(), this.valueString(),
        ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType.name(), this.getAttributeAssign().getAttributeDef().getValueType().name()).save();
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN_VALUE, 
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_PRE_INSERT, HooksAttributeAssignValueBean.class, 
        this, AttributeAssignValue.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_PRE_INSERT, false, false);
    
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    this.setLastUpdatedDb(System.currentTimeMillis());
    
    if (AttributeAssignValue.this.dbVersionDifferentFields().contains(FIELD_VALUE_INTEGER) ||
        AttributeAssignValue.this.dbVersionDifferentFields().contains(FIELD_VALUE_FLOATING) ||
        AttributeAssignValue.this.dbVersionDifferentFields().contains(FIELD_VALUE_STRING) ||
        AttributeAssignValue.this.dbVersionDifferentFields().contains(FIELD_VALUE_MEMBER_ID)) {
      throw new RuntimeException("Cannot update values.  Must delete and re-add db rows.");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_ATTRIBUTE_ASSIGN_ID)) {
      throw new RuntimeException("cannot update attributeAssignId");
    }

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN_VALUE, 
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_PRE_UPDATE, HooksAttributeAssignValueBean.class, 
        this, AttributeAssignValue.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_PRE_UPDATE, false, false);
  
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public AttributeAssignValue dbVersion() {
    return (AttributeAssignValue)this.dbVersion;
  }
  
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }


  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);
    return result;
  }
  
}
