/*
 * @author mchyzer
 * $Id: ChangeLogEntry.java,v 1.2 2009-05-12 06:35:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * represents a user change log record.  This is a change to a record in the DB (insert/update/delete).
 */
@SuppressWarnings("serial")
public class ChangeLogEntry extends GrouperAPI {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: changeLogTypeId */
  public static final String FIELD_CHANGE_LOG_TYPE_ID = "changeLogTypeId";

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
   * save (insert) this object
   */
  public void save() {
    GrouperDAOFactory.getFactory().getChangeLogEntry().save(this);
  }
  
  /**
   * construct, assign an id
   * @param changeLogTypeIdentifier points to changeLog type
   * @param labelNamesAndValues alternate label name and value
   */
  public ChangeLogEntry(ChangeLogTypeIdentifier changeLogTypeIdentifier, 
      String... labelNamesAndValues) {
    
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

  /** foreign key to the type of changeLog entry this is */
  private String changeLogTypeId;

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

  /**
   * context id ties multiple db changes
   * @return id
   */
  public String getContextId() {
    return this.contextId;
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
    return this.string01;
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
    return this.string02;
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
    return this.string03;
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
    return this.string04;
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
    return this.string05;
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
    return this.string06;
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
    return this.string07;
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
    return this.string08;
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
    if (this.createdOnDb == null) {
      this.createdOnDb = System.currentTimeMillis();
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
  }

  /**
   * when created
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
    return this.string09;
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
    return this.string10;
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
    return this.string11;
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
    return this.string12;
  }

  /**
   * set string 12
   * @param _string12
   */
  public void setString12(String _string12) {
    this.string12 = _string12;
  }

}
