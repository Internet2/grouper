/*
 * @author mchyzer
 * $Id: ChangeLogEntry.java,v 1.10 2009-11-03 14:18:59 shilen Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * <pre>
 * represents a user change log record.  This is a change to a record in the DB (insert/update/delete).
 * 
 * note: if this object is headed for the temp table, then the getters in the composite key will not be null, will be empty.
 * this is a hibernate constraint
 * 
 * </pre>
 */
@SuppressWarnings("serial")
public class ChangeLogEntry extends GrouperAPI {
  
  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_CHANGE_LOG_TYPE_ID = "change_log_type_id";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_STRING01 = "string01";

  /** column */
  public static final String COLUMN_STRING02 = "string02";

  /** column */
  public static final String COLUMN_STRING03 = "string03";

  /** column */
  public static final String COLUMN_STRING04 = "string04";

  /** column */
  public static final String COLUMN_STRING05 = "string05";

  /** column */
  public static final String COLUMN_STRING06 = "string06";

  /** column */
  public static final String COLUMN_STRING07 = "string07";

  /** column */
  public static final String COLUMN_STRING08 = "string08";

  /** column */
  public static final String COLUMN_STRING09 = "string09";

  /** column */
  public static final String COLUMN_STRING10 = "string10";

  /** column */
  public static final String COLUMN_STRING11 = "string11";

  /** column */
  public static final String COLUMN_STRING12 = "string12";

  /** column */
  public static final String COLUMN_SEQUENCE_NUMBER = "sequence_number";

  /**
   * 
   * @param changeLogTypeIdentifier
   * @param theObject
   * @param dbVersion
   * @param labelNamesAndValues
   * @param objectPropertyNames
   * @param changeLogPropertyNames
   */
  public static void saveTempUpdates(ChangeLogTypeIdentifier changeLogTypeIdentifier, 
      Object theObject, Object dbVersion,
      List<String> labelNamesAndValues,
      List<String> objectPropertyNames,
      List<String> changeLogPropertyNames) {
    
    if (GrouperUtil.length(objectPropertyNames) != GrouperUtil.length(changeLogPropertyNames)) {
      throw new RuntimeException("Object property names length if not equal " +
      		"to changeLog property names length: " + GrouperUtil.length(objectPropertyNames) 
      		+ " != " +  GrouperUtil.length(changeLogPropertyNames));
    }
    
    //since this is an update, why would either be null???
    if (theObject == null || dbVersion == null) {
      throw new RuntimeException("theObject and dbVersion cannot be null: "
          + (theObject == null) + ", " + (dbVersion == null));
    }
    
    int index = 0;
    for (String objectPropertyName: objectPropertyNames) {
      
      //get the values of the property
      Object propertyValue = GrouperUtil.propertyValue(theObject, objectPropertyName);
      Object dbPropertyValue = GrouperUtil.propertyValue(dbVersion, objectPropertyName);
      
      //see if different
      if (!GrouperUtil.equals(propertyValue, dbPropertyValue)) {
        
        String[] labelsAndValuesArray = new String[labelNamesAndValues.size() + 6];
        
        for (int i=0;i<labelNamesAndValues.size();i++) {
          labelsAndValuesArray[i] = labelNamesAndValues.get(i);
        }
        //last two cols are twhats different, and the old value
        labelsAndValuesArray[labelsAndValuesArray.length-6] = "propertyChanged";
        labelsAndValuesArray[labelsAndValuesArray.length-5] = changeLogPropertyNames.get(index);
        labelsAndValuesArray[labelsAndValuesArray.length-4] = "propertyOldValue";
        labelsAndValuesArray[labelsAndValuesArray.length-3] = GrouperUtil.stringValue(dbPropertyValue);
        labelsAndValuesArray[labelsAndValuesArray.length-2] = "propertyNewValue";
        labelsAndValuesArray[labelsAndValuesArray.length-1] = GrouperUtil.stringValue(propertyValue);
        
        //if so, add a change log entry to temp table
        new ChangeLogEntry(true, changeLogTypeIdentifier, labelsAndValuesArray).save();

      }
      
      index++;
    }
    
  }
  
  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    
    if (!(obj instanceof ChangeLogEntry)) {
      return false;
    }
    
    ChangeLogEntry objChangeLogEntry = (ChangeLogEntry)obj;
    
    //if there is a sequence, then it is a ChangeLogEntryEntity
    if (this.sequenceNumber != null || objChangeLogEntry.sequenceNumber != null) {
      return new EqualsBuilder().append(this.sequenceNumber, objChangeLogEntry.sequenceNumber).isEquals();
    } 
    //else it is a ChangeLogEntryTemp
    return new EqualsBuilder()
      .append(this.changeLogTypeId, objChangeLogEntry.changeLogTypeId)
      .append(this.contextId, objChangeLogEntry.contextId)
      .append(this.createdOnDb, objChangeLogEntry.createdOnDb)
      .append(this.string01, objChangeLogEntry.string01)
      .append(this.string02, objChangeLogEntry.string02)
      .append(this.string03, objChangeLogEntry.string03)
      .append(this.string04, objChangeLogEntry.string04)
      .append(this.string05, objChangeLogEntry.string05)
      .append(this.string06, objChangeLogEntry.string06)
      .append(this.string07, objChangeLogEntry.string07)
      .append(this.string08, objChangeLogEntry.string08)
      .append(this.string09, objChangeLogEntry.string09)
      .append(this.string10, objChangeLogEntry.string10)
      .append(this.string11, objChangeLogEntry.string11)
      .append(this.string12, objChangeLogEntry.string12)
      .isEquals();
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    
    //if there is a sequence, then it is a ChangeLogEntryEntity
    if (this.sequenceNumber != null) {
      return new HashCodeBuilder().append(this.sequenceNumber).toHashCode();
    } 
    //else it is a ChangeLogEntryTemp
    return new HashCodeBuilder()
      .append(this.changeLogTypeId)
      .append(this.contextId)
      .append(this.createdOnDb)
      .append(this.string01)
      .append(this.string02)
      .append(this.string03)
      .append(this.string04)
      .append(this.string05)
      .append(this.string06)
      .append(this.string07)
      .append(this.string08)
      .append(this.string09)
      .append(this.string10)
      .append(this.string11)
      .append(this.string12)
      .toHashCode();
  }

  /** entity name for change log temp */
  public static final String CHANGE_LOG_ENTRY_TEMP_ENTITY_NAME = "ChangeLogEntryTemp";
  
  /** entity name for change log */
  public static final String CHANGE_LOG_ENTRY_ENTITY_NAME = "ChangeLogEntryEntity";
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: changeLogTypeId */
  public static final String FIELD_CHANGE_LOG_TYPE_ID = "changeLogTypeId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: string01 */
  public static final String FIELD_STRING01 = "string01";

  /** constant for field name for: string02 */
  public static final String FIELD_STRING02 = "string02";

  /** constant for field name for: string03 */
  public static final String FIELD_STRING03 = "string03";

  /** constant for field name for: string04 */
  public static final String FIELD_STRING04 = "string04";

  /** constant for field name for: string05 */
  public static final String FIELD_STRING05 = "string05";

  /** constant for field name for: string06 */
  public static final String FIELD_STRING06 = "string06";

  /** constant for field name for: string07 */
  public static final String FIELD_STRING07 = "string07";

  /** constant for field name for: string08 */
  public static final String FIELD_STRING08 = "string08";

  /** constant for field name for: string09 */
  public static final String FIELD_STRING09 = "string09";

  /** constant for field name for: string10 */
  public static final String FIELD_STRING10 = "string10";

  /** constant for field name for: string11 */
  public static final String FIELD_STRING11 = "string11";

  /** constant for field name for: string12 */
  public static final String FIELD_STRING12 = "string12";

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** to string deep fields */
  private static final Set<String> TO_STRING_DEEP_FIELDS = GrouperUtil.toSet(
      FIELD_CHANGE_LOG_TYPE_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_STRING01, FIELD_STRING02, FIELD_STRING03, FIELD_STRING04, 
      FIELD_STRING05, FIELD_STRING06, FIELD_STRING07, FIELD_STRING08, 
      FIELD_STRING09, FIELD_STRING10, FIELD_STRING11, FIELD_STRING12);

  /**
   * get the changeLog type, it better be there
   * @return the changeLog type
   */
  public ChangeLogType getChangeLogType() {
    return ChangeLogTypeFinder.find(this.changeLogTypeId, true);
  }
  
  /**
   * 
   * @param extended if all fields should be printed
   * @return the report
   */
  public String toStringReport(boolean extended) {
    StringBuilder result = new StringBuilder();
    ChangeLogType changeLogType = this.getChangeLogType();
    Timestamp createdOn = this.getCreatedOn();
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String createdOnString = simpleDateFormat.format(createdOn);
    
    result.append(createdOnString).append(" ").append(StringUtils.rightPad(changeLogType.getChangeLogCategory(), 12))
      .append(" - ").append(StringUtils.rightPad(changeLogType.getActionName(), 20)).append("\n");
    
    for (String label: changeLogType.labels()) {
      
      //see if there is data
      String fieldName = changeLogType.retrieveChangeLogEntryFieldForLabel(label);
      Object value = GrouperUtil.fieldValue(this, fieldName);
      String valueString = GrouperUtil.stringValue(value);
      
      //abbreviate if not extended
      if (!extended) {
        valueString = StringUtils.abbreviate(valueString, 50);
      }
      
      if (!StringUtils.isBlank(valueString)) {
        
        result.append("  ").append(StringUtils.rightPad(StringUtils.capitalize(label) + ":", 20)).append(value).append("\n");
        
      }
    }
    
    return result.toString();
  }
  
  /**
   * construct
   */
  public ChangeLogEntry() {
    
  }

  /**
   * save this object (insert) to the temp table if configured to do so, and set context id and other things
   * save (insert) this object
   */
  public void save() {
    if (isTempObject() || GrouperConfig.getPropertyBoolean("changeLog.enabled", true)) {
      
      GrouperDAOFactory.getFactory().getChangeLogEntry().save(this);
    }
  }

  /**
   * update this object to the temp or entity table if configured to do so, and set context id and other things
   * save (insert) this object
   */
  public void update() {
    if (isTempObject() || GrouperConfig.getPropertyBoolean("changeLog.enabled", true)) {
      
      GrouperDAOFactory.getFactory().getChangeLogEntry().update(this);
    }
  }
  
  /**
   * delete the change log entry from either the temp table or the entity table
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getChangeLogEntry().delete(this);
  }
  
  /**
   * construct, assign an id
   * @param tempObject1 if this is a temp object, or a normal change log entry
   * @param changeLogTypeIdentifier points to changeLog type
   * @param labelNamesAndValues alternate label name and value
   */
  public ChangeLogEntry(boolean tempObject1, ChangeLogTypeIdentifier changeLogTypeIdentifier, 
      String... labelNamesAndValues) {
    
    this.tempObject = tempObject1;

    ChangeLogType changeLogType = ChangeLogTypeFinder.find(changeLogTypeIdentifier.getChangeLogCategory(),
        changeLogTypeIdentifier.getActionName(), true);
    
    this.changeLogTypeId = changeLogType.getId();
    
    int labelNamesAndValuesLength = GrouperUtil.length(labelNamesAndValues);
    
    if (labelNamesAndValuesLength % 2 != 0) {
      throw new RuntimeException("labelNamesAndValuesLength must be divisible by 2: " 
          + labelNamesAndValuesLength);
    }
    
    for (int i=0;i<labelNamesAndValuesLength;i+=2) {
      String label = labelNamesAndValues[i];
      String value = labelNamesAndValues[i+1];

      assignStringValue(changeLogType, label, value);
    }
  }

  /**
   * reutrn the value based on friendly label.  ChangeLogEntry keeps data in 
   * string01, string02, etc.  But it is more useful when querying by group id.
   * so pass in the fiendly label from the ChangeLogType, and it will look up which field,
   * and return the value of that field
   * @param changeLogLabel is probably from ChangeLogLabels constants
   * @return the value
   */
  public String retrieveValueForLabel(ChangeLogLabel changeLogLabel) {
    return retrieveValueForLabel(changeLogLabel.name());
  }

  /**
   * reutrn the value based on friendly label.  ChangeLogEntry keeps data in 
   * string01, string02, etc.  But it is more useful when querying by group id.
   * so pass in the fiendly label from the ChangeLogType, and it will look up which field,
   * and return the value of that field
   * @param label
   * @return the value
   */
  public String retrieveValueForLabel(String label) {
    ChangeLogType changeLogType = this.getChangeLogType();
    String fieldName = changeLogType.retrieveChangeLogEntryFieldForLabel(label);
    return (String)this.fieldValue(fieldName);
  }
  
  /**
   * @param changeLogType
   * @param label
   * @param value
   */
  public void assignStringValue(ChangeLogType changeLogType, String label, String value) {
    if (StringUtils.equals(label, changeLogType.getLabelString01())) {
      this.string01 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString02())) {
      this.string02 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString03())) {
      this.string03 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString04())) {
      this.string04 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString05())) {
      this.string05 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString06())) {
      this.string06 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString07())) {
      this.string07 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString08())) {
      this.string08 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString09())) {
      this.string09 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString10())) {
      this.string10 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString11())) {
      this.string11 = value;
    } else if (StringUtils.equals(label, changeLogType.getLabelString12())) {
      this.string12 = value;
    } else {
      throw new RuntimeException("Cant find string label: " + label 
          + " in changeLog type: " + changeLogType.getChangeLogCategory() + " - " + changeLogType.getActionName());
    }
  }
  
  /** name of the grouper changeLog entry table in the db */
  public static final String TABLE_GROUPER_CHANGE_LOG_ENTRY = "grouper_change_log_entry";

  /** name of the grouper changeLog entry temp table in the db, where records go first before being moved to the real table */
  public static final String TABLE_GROUPER_CHANGE_LOG_ENTRY_TEMP = "grouper_change_log_entry_temp";

  /** foreign key to the type of changeLog entry this is */
  private String changeLogTypeId;

  /**
   * uuid for temp object
   */
  private String id;
  
  /**
   * context id ties multiple db changes  
   */
  private String contextId;

  /**
   * misc field 1
   */
  private String string01;
  
  /**
   * misc field 2
   */
  private String string02;
  
  /**
   * misc field 3
   */
  private String string03;
  
  /**
   * misc field 4
   */
  private String string04;
  
  /**
   * misc field 5
   */
  private String string05;
  
  /**
   * misc field 6
   */
  private String string06;
  
  /**
   * misc field 7
   */
  private String string07;
  
  /**
   * misc field 8
   */
  private String string08;

  /**
   * misc field 9
   */
  private String string09;

  /**
   * misc field 10
   */
  private String string10;

  /**
   * misc field 11
   */
  private String string11;

  /**
   * misc field 12
   */
  private String string12;

  /**
   * when this record was created 
   */
  private Long createdOnDb;

  /**
   * optional sequence for ordering
   */
  private Long sequenceNumber;

  /**
   * optional sequence for ordering
   * @return sequence number
   */
  public Long getSequenceNumber() {
    return this.sequenceNumber;
  }

  /**
   * optional sequence for ordering
   * @param sequenceNumber1
   */
  public void setSequenceNumber(Long sequenceNumber1) {
    this.sequenceNumber = sequenceNumber1;
  }
  
  /**
   * uuid for temp object
   * @return uuid for temp object
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * set uuid for temp object
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * foreign key to the type of changeLog entry this is
   * @return the changeLog type id
   */
  public String getChangeLogTypeId() {
    return this.changeLogTypeId;
  }

  /**
   * foreign key to the type of changeLog entry this is
   * @param changeLogTypeId1
   */
  public void setChangeLogTypeId(String changeLogTypeId1) {
    this.changeLogTypeId = changeLogTypeId1;
  }

  /** if this object is bound for the temp table, or regular table */
  private boolean tempObject = true;
  
  /**
   * context id ties multiple db changes
   * @return id
   */
  public String getContextId() {
    
    return tempConvert(this.contextId);
  }

  /**
   * context id ties multiple db changes
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * misc field 1
   * @return field
   */
  public String getString01() {
    return tempConvert(this.string01);
  }

  /**
   * if temp object, then with hibernate composite id, cant be null
   * @param theString to convert if temp object
   * @return the string, if temp object, then make sure not null
   */
  private String tempConvert(String theString) {
    if (this.tempObject) {
      return StringUtils.defaultString(theString);
    }
    //why would be have empty string?
    if ("".equals(theString)) {
      return null;
    }
    return theString;
  }

  /**
   * misc field 1
   * @param string01a
   */
  public void setString01(String string01a) {
    this.string01 = string01a;
  }

  /**
   * misc field 2
   * @return field
   */
  public String getString02() {
    return tempConvert(this.string02);
  }

  /**
   * misc field 2
   * @param string02a
   */
  public void setString02(String string02a) {
    this.string02 = string02a;
  }

  /**
   * misc field 3
   * @return field
   */
  public String getString03() {
    return tempConvert(this.string03);
  }

  /**
   * misc field 3
   * @param string03a
   */
  public void setString03(String string03a) {
    this.string03 = string03a;
  }

  /**
   * misc field 4
   * @return field
   */
  public String getString04() {
    return tempConvert(this.string04);
  }

  /**
   * misc field 4
   * @param string04a
   */
  public void setString04(String string04a) {
    this.string04 = string04a;
  }

  /**
   * misc field 5
   * @return field
   */
  public String getString05() {
    return tempConvert(this.string05);
  }

  /**
   * misc field 5
   * @param string05a
   */
  public void setString05(String string05a) {
    this.string05 = string05a;
  }

  /**
   * misc field 6
   * @return field
   */
  public String getString06() {
    return tempConvert(this.string06);
  }

  /**
   * misc field 6
   * @param string06a
   */
  public void setString06(String string06a) {
    this.string06 = string06a;
  }

  /**
   * misc field 7
   * @return field
   */
  public String getString07() {
    return tempConvert(this.string07);
  }

  /**
   * misc field 7
   * @param string07a
   */
  public void setString07(String string07a) {
    this.string07 = string07a;
  }

  /**
   * misc field 8
   * @return field
   */
  public String getString08() {
    return tempConvert(this.string08);
  }

  /**
   * misc field 8
   * @param string08a
   */
  public void setString08(String string08a) {
    this.string08 = string08a;
  }

  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb / 1000);
  }

  /**
   * when created, microseconds since 1970
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
    this.createdOnDb = createdOn1 == null ? null : (createdOn1.getTime() * 1000);
  }

  /**
   * make sure this object will fit in the DB
   */
  public void truncate() {
    this.changeLogTypeId = GrouperUtil.truncateAscii(this.changeLogTypeId, 128);
    this.contextId = GrouperUtil.truncateAscii(this.contextId, 128);
    this.string01 = GrouperUtil.truncateAscii(this.string01, 4000);
    this.string02 = GrouperUtil.truncateAscii(this.string02, 4000);
    this.string03 = GrouperUtil.truncateAscii(this.string03, 4000);
    this.string04 = GrouperUtil.truncateAscii(this.string04, 4000);
    this.string05 = GrouperUtil.truncateAscii(this.string05, 4000);
    this.string06 = GrouperUtil.truncateAscii(this.string06, 4000);
    this.string07 = GrouperUtil.truncateAscii(this.string07, 4000);
    this.string08 = GrouperUtil.truncateAscii(this.string08, 4000);
    this.string09 = GrouperUtil.truncateAscii(this.string09, 4000);
    this.string10 = GrouperUtil.truncateAscii(this.string10, 4000);
    this.string11 = GrouperUtil.truncateAscii(this.string11, 4000);
    this.string12 = GrouperUtil.truncateAscii(this.string12, 4000);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    throw new RuntimeException("not implemented");
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    this.truncate();
    if (this.tempObject) {
      if (this.createdOnDb == null) {
        this.createdOnDb = ChangeLogId.changeLogId();
      }
      if (StringUtils.isBlank(this.contextId)) {
        this.contextId = GrouperContext.retrieveContextId(true);
      }
      
      //assign id if not there
      if (StringUtils.isBlank(this.getId())) {
        this.setId(GrouperUuid.getUuid());
      }
    }
    if (!this.tempObject) {
      if (this.sequenceNumber == null) {
        this.sequenceNumber = nextSequenceNumber();
      }
    }
  }

  /**
   * max sequence number of the entry table
   */
  private static Long nextSequenceNumber = null;
  
  /**
   * find the max sequence number in the entry table
   * @return the max sequence number (plus one)
   */
  private synchronized static long nextSequenceNumber() {
    if (nextSequenceNumber == null) {
      nextSequenceNumber = maxSequenceNumber(true);
      if (nextSequenceNumber == null) {
        nextSequenceNumber = 0l;
      }
    }
    //we can cache this in memory since we are the only process that is inserting into the table
    return ++nextSequenceNumber;
  }

  /**
   * max sequence number in DB
   * @param considerConsumers if the consumers should be considered
   * @return the max sequence number (or null if not there)
   */
  public static Long maxSequenceNumber(boolean considerConsumers) {
    Long result = HibernateSession.byHqlStatic().createQuery(
        "select max(sequenceNumber) from ChangeLogEntryEntity").uniqueResult(Long.class);
    if (considerConsumers) {
      Long resultConsumer = HibernateSession.byHqlStatic().createQuery(
        "select max(lastSequenceProcessed) from ChangeLogConsumer").uniqueResult(Long.class);
      
      //if we have a consumer
      if (resultConsumer != null) {
  
        //if results
        if (result != null) {
          if (result > resultConsumer) {
            return result;
          }
        }
        //return consumer if better than result
        return resultConsumer;
      }
    }
    return result;
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.truncate();
  }

  /**
   * when created, microseconds since 1970
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * the string repre
   * @return string 
   */
  public String toStringDeep() {
    return GrouperUtil.toStringFields(this, TO_STRING_DEEP_FIELDS);
  }

  /**
   * 
   * @return the string 09
   */
  public String getString09() {
    return tempConvert(this.string09);
  }

  /**
   * set the string 09
   * @param theString09
   */
  public void setString09(String theString09) {
    this.string09 = theString09;
  }

  /**
   * get string 10
   * @return string 10
   */
  public String getString10() {
    return tempConvert(this.string10);
  }

  /**
   * set string 10
   * @param theString10
   */
  public void setString10(String theString10) {
    this.string10 = theString10;
  }

  /**
   * 
   * @return string 11
   */
  public String getString11() {
    return tempConvert(this.string11);
  }

  /**
   * set string 11
   * @param _string11
   */
  public void setString11(String _string11) {
    this.string11 = _string11;
  }

  /**
   * get string 12
   * @return string 12
   */
  public String getString12() {
    return tempConvert(this.string12);
  }

  /**
   * set string 12
   * @param _string12
   */
  public void setString12(String _string12) {
    this.string12 = _string12;
  }

  /**
   * if this is a temp object, destined for the temp table
   * @return temp object
   */
  public boolean isTempObject() {
    return this.tempObject;
  }

  /**
   * if this is a temp object headed for the temp table
   * @param tempObject1
   */
  public void setTempObject(boolean tempObject1) {
    this.tempObject = tempObject1;
  }

  /**
   * see if this identifier matches the change log type by category and action
   * @param changeLogTypeIdentifier
   * @return true if matches
   */
  public boolean equalsCategoryAndAction(ChangeLogTypeIdentifier changeLogTypeIdentifier) {
    return this.getChangeLogType() != null 
      && this.getChangeLogType().equalsCategoryAndAction(changeLogTypeIdentifier);
  }

}
