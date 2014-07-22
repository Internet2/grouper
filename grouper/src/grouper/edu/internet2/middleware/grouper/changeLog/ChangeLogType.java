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
 * @author mchyzer
 * $Id: ChangeLogType.java,v 1.3 2009-06-10 05:31:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * type of changeLog
 */
@SuppressWarnings("serial")
public class ChangeLogType extends GrouperAPI implements Hib3GrouperVersioned {

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_ACTION_NAME = "action_name";

  /** column */
  public static final String COLUMN_CHANGE_LOG_CATEGORY = "change_log_category";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";
  
  /** column */
  public static final String COLUMN_LABEL_STRING01 = "label_string01";
  
  /** column */
  public static final String COLUMN_LABEL_STRING02 = "label_string02";

  /** column */
  public static final String COLUMN_LABEL_STRING03 = "label_string03";

  /** column */
  public static final String COLUMN_LABEL_STRING04 = "label_string04";

  /** column */
  public static final String COLUMN_LABEL_STRING05 = "label_string05";

  /** column */
  public static final String COLUMN_LABEL_STRING06 = "label_string06";

  /** column */
  public static final String COLUMN_LABEL_STRING07 = "label_string07";

  /** column */
  public static final String COLUMN_LABEL_STRING08 = "label_string08";

  /** column */
  public static final String COLUMN_LABEL_STRING09 = "label_string09";

  /** column */
  public static final String COLUMN_LABEL_STRING10 = "label_string10";

  /** column */
  public static final String COLUMN_LABEL_STRING11 = "label_string11";

  /** column */
  public static final String COLUMN_LABEL_STRING12 = "label_string12";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: actionName */
  public static final String FIELD_ACTION_NAME = "actionName";

  /** constant for field name for: changeLogCategory */
  public static final String FIELD_CHANGE_LOG_CATEGORY = "changeLogCategory";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: labelString01 */
  public static final String FIELD_LABEL_STRING01 = "labelString01";

  /** constant for field name for: labelString02 */
  public static final String FIELD_LABEL_STRING02 = "labelString02";

  /** constant for field name for: labelString03 */
  public static final String FIELD_LABEL_STRING03 = "labelString03";

  /** constant for field name for: labelString04 */
  public static final String FIELD_LABEL_STRING04 = "labelString04";

  /** constant for field name for: labelString05 */
  public static final String FIELD_LABEL_STRING05 = "labelString05";

  /** constant for field name for: labelString06 */
  public static final String FIELD_LABEL_STRING06 = "labelString06";

  /** constant for field name for: labelString07 */
  public static final String FIELD_LABEL_STRING07 = "labelString07";

  /** constant for field name for: labelString08 */
  public static final String FIELD_LABEL_STRING08 = "labelString08";

  /** constant for field name for: labelString09 */
  public static final String FIELD_LABEL_STRING09 = "labelString09";

  /** constant for field name for: labelString10 */
  public static final String FIELD_LABEL_STRING10 = "labelString10";

  /** constant for field name for: labelString11 */
  public static final String FIELD_LABEL_STRING11 = "labelString11";

  /** constant for field name for: labelString12 */
  public static final String FIELD_LABEL_STRING12 = "labelString12";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * fields in to string deep method
   */
  private static final Set<String> TO_STRING_DEEP_FIELDS = GrouperUtil.toSet(
      FIELD_ACTION_NAME, FIELD_CHANGE_LOG_CATEGORY, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, 
      FIELD_ID, FIELD_LABEL_STRING01, FIELD_LABEL_STRING02, FIELD_LABEL_STRING03, 
      FIELD_LABEL_STRING04, FIELD_LABEL_STRING05, FIELD_LABEL_STRING06, FIELD_LABEL_STRING07, 
      FIELD_LABEL_STRING08, FIELD_LABEL_STRING09, FIELD_LABEL_STRING10, FIELD_LABEL_STRING11, 
      FIELD_LABEL_STRING12, FIELD_LAST_UPDATED_DB);


  
  /**
   * empty constructor
   */
  public ChangeLogType() {
    
  }
  
  /**
   * see if one changeLog type is the same as another (not looking at last update, id, etc)
   * @param changeLogType
   * @return true if equals, false if not
   */
  public boolean equalsDeep(ChangeLogType changeLogType) {
    
    return new EqualsBuilder().append(this.actionName, changeLogType.actionName)
      .append(this.changeLogCategory, changeLogType.changeLogCategory)
      .append(this.labelString01, changeLogType.labelString01)
      .append(this.labelString02, changeLogType.labelString02)
      .append(this.labelString03, changeLogType.labelString03)
      .append(this.labelString04, changeLogType.labelString04)
      .append(this.labelString05, changeLogType.labelString05)
      .append(this.labelString06, changeLogType.labelString06)
      .append(this.labelString07, changeLogType.labelString07)
      .append(this.labelString08, changeLogType.labelString08)
      .append(this.labelString09, changeLogType.labelString09)
      .append(this.labelString10, changeLogType.labelString10)
      .append(this.labelString11, changeLogType.labelString11)
      .append(this.labelString12, changeLogType.labelString12)
      .isEquals();
  }
  
  /**
   * labels for this type
   * @return the labels
   */
  public Set<String> labels() {
    Set<String> labels = new LinkedHashSet<String>();
    if (!StringUtils.isBlank(this.labelString01)) {
      labels.add(this.labelString01);
    }
    if (!StringUtils.isBlank(this.labelString02)) {
      labels.add(this.labelString02);
    }
    if (!StringUtils.isBlank(this.labelString03)) {
      labels.add(this.labelString03);
    }
    if (!StringUtils.isBlank(this.labelString04)) {
      labels.add(this.labelString04);
    }
    if (!StringUtils.isBlank(this.labelString05)) {
      labels.add(this.labelString05);
    }
    if (!StringUtils.isBlank(this.labelString06)) {
      labels.add(this.labelString06);
    }
    if (!StringUtils.isBlank(this.labelString07)) {
      labels.add(this.labelString07);
    }
    if (!StringUtils.isBlank(this.labelString08)) {
      labels.add(this.labelString08);
    }
    if (!StringUtils.isBlank(this.labelString09)) {
      labels.add(this.labelString09);
    }
    if (!StringUtils.isBlank(this.labelString10)) {
      labels.add(this.labelString10);
    }
    if (!StringUtils.isBlank(this.labelString11)) {
      labels.add(this.labelString11);
    }
    if (!StringUtils.isBlank(this.labelString12)) {
      labels.add(this.labelString12);
    }

    return labels;
  }
  
  /**
   * get the field in changeLog entry for this label
   * @param label
   * @return the field
   */
  public String retrieveChangeLogEntryFieldForLabel(String label) {
    if (StringUtils.equals(label, this.labelString01)) {
      return "string01";
    }
    if (StringUtils.equals(label, this.labelString02)) {
      return "string02";
    }
    if (StringUtils.equals(label, this.labelString03)) {
      return "string03";
    }
    if (StringUtils.equals(label, this.labelString04)) {
      return "string04";
    }
    if (StringUtils.equals(label, this.labelString05)) {
      return "string05";
    }
    if (StringUtils.equals(label, this.labelString06)) {
      return "string06";
    }
    if (StringUtils.equals(label, this.labelString07)) {
      return "string07";
    }
    if (StringUtils.equals(label, this.labelString08)) {
      return "string08";
    }
    if (StringUtils.equals(label, this.labelString09)) {
      return "string09";
    }
    if (StringUtils.equals(label, this.labelString10)) {
      return "string10";
    }
    if (StringUtils.equals(label, this.labelString11)) {
      return "string11";
    }
    if (StringUtils.equals(label, this.labelString12)) {
      return "string12";
    }

    throw new RuntimeException("Cant find label '" + label + "' for type: " + this);
  }
  
  /**
   * copy the argument into this
   * @param changeLogType
   */
  public void copyArgFieldIntoThis(ChangeLogType changeLogType) {
    this.actionName = changeLogType.actionName;
    this.changeLogCategory = changeLogType.changeLogCategory;
    this.labelString01 = changeLogType.labelString01;
    this.labelString02 = changeLogType.labelString02;
    this.labelString03 = changeLogType.labelString03;
    this.labelString04 = changeLogType.labelString04;
    this.labelString05 = changeLogType.labelString05;
    this.labelString06 = changeLogType.labelString06;
    this.labelString07 = changeLogType.labelString07;
    this.labelString08 = changeLogType.labelString08;
    this.labelString09 = changeLogType.labelString09;
    this.labelString10 = changeLogType.labelString10;
    this.labelString11 = changeLogType.labelString11;
    this.labelString12 = changeLogType.labelString12;
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
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "ChangeLog type: " + this.changeLogCategory + ": " + this.actionName;
  }

  /**
   * construct with more params
   * @param changeLogCategory1
   * @param actionName1
   * @param enumStrings up to 8 label strings
   */
  public ChangeLogType(String changeLogCategory1, String actionName1, ChangeLogLabel... enumStrings) {
    this(changeLogCategory1, actionName1, enumArrayToStringArray(enumStrings));
  }
  
  /**
   * convert an enum array to a string array
   * @param enumStrings
   * @return the string array
   */
  private static String[] enumArrayToStringArray(ChangeLogLabel... enumStrings) {
    String[] labelStrings = new String[enumStrings.length];
    for (int i=0;i<enumStrings.length;i++) {
      if (enumStrings[i] == null) {
        labelStrings[i] = null;
      } else {
        labelStrings[i] = enumStrings[i].name();
      }
    }
    return labelStrings;
  }
  
  /**
   * construct with more params
   * @param changeLogCategory1
   * @param actionName1
   * @param labelStrings up to 8 label strings
   */
  public ChangeLogType(String changeLogCategory1, String actionName1, String... labelStrings) {
    this.changeLogCategory = changeLogCategory1;
    this.actionName = actionName1;
    

    int index=1;
    for (String labelString : GrouperUtil.nonNull(labelStrings, String.class)) {
      GrouperUtil.assignField(this, "labelString" + (index<10 ? "0" : "") + index, labelString);
      if (index > 12) {
        throw new RuntimeException("Cant send more than 12 labelStrings: " + labelStrings.length);
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
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ChangeLogType)) {
      return false;
    }
    ChangeLogType otherChangeLogType = (ChangeLogType)obj;
    return new EqualsBuilder().append(this.changeLogCategory, otherChangeLogType.changeLogCategory)
      .append(this.actionName, otherChangeLogType.actionName).isEquals();
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.changeLogCategory)
      .append(this.actionName).hashCode();
  }

  /** name of the grouper changeLog type table in the db */
  public static final String TABLE_GROUPER_CHANGE_LOG_TYPE = "grouper_change_log_type";
  
  /** id of this type */
  private String id;

  /** friendly label for the changeLog type */
  private String changeLogCategory;
  
  /** friendly label for the action in the category */
  private String actionName;
  
  /** when this record was last updated */
  private Long lastUpdatedDb;
  
  /** when this record was created */
  private Long createdOnDb;
  
  /** label for the string01 field */
  private String labelString01;
  
  /** label for the string02 field */
  private String labelString02;
  
  /** label for the string03 field */
  private String labelString03;
  
  /** label for the string04 field */
  private String labelString04;
  
  /** label for the string05 field */
  private String labelString05;
  
  /** label for the string06 field */
  private String labelString06;
  
  /** label for the string07 field */
  private String labelString07;
  
  /**
   * label for the string08 field 
   */
  private String labelString08;

  /**
   * label for the string09 field 
   */
  private String labelString09;

  /** label for the string10 field */
  private String labelString10;
  
  /** label for the string11 field */
  private String labelString11;
  
  /** label for the string12 field */
  private String labelString12;
  
  /** context id ties multiple db changes  */
  private String contextId;
  
  /**
   * uuid of row
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * uuid of row
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * category of changeLog
   * @return changeLog type
   */
  public String getChangeLogCategory() {
    return this.changeLogCategory;
  }

  /**
   * category of changeLog
   * @param changeLogCategory1
   */
  public void setChangeLogCategory(String changeLogCategory1) {
    this.changeLogCategory = changeLogCategory1;
  }

  /**
   * action within the changeLog category
   * @return the action name
   */
  public String getActionName() {
    return this.actionName;
  }

  /**
   * action within the changeLog category
   * @param actionName
   */
  public void setActionName(String actionName) {
    this.actionName = actionName;
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
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    if (this.createdOnDb == null) {
      this.createdOnDb = System.currentTimeMillis();
    }
    if (this.lastUpdatedDb == null) {
      this.lastUpdatedDb = System.currentTimeMillis();
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.lastUpdatedDb = System.currentTimeMillis();
  }

  /**
   * label for string01
   * @return label
   */
  public String getLabelString01() {
    return this.labelString01;
  }

  /**
   * label for string01
   * @param labelString01a
   */
  public void setLabelString01(String labelString01a) {
    this.labelString01 = labelString01a;
  }

  /**
   * label for string02
   * @return label
   */
  public String getLabelString02() {
    return this.labelString02;
  }

  /**
   * label for string02
   * @param labelString02a
   */
  public void setLabelString02(String labelString02a) {
    this.labelString02 = labelString02a;
  }

  /**
   * label for string03
   * @return label
   */
  public String getLabelString03() {
    return this.labelString03;
  }

  /**
   * label for string03
   * @param labelString03a
   */
  public void setLabelString03(String labelString03a) {
    this.labelString03 = labelString03a;
  }

  /**
   * label for string04
   * @return label
   */
  public String getLabelString04() {
    return this.labelString04;
  }

  /**
   * label for string04
   * @param labelString04a
   */
  public void setLabelString04(String labelString04a) {
    this.labelString04 = labelString04a;
  }

  /**
   * label for string05
   * @return label
   */
  public String getLabelString05() {
    return this.labelString05;
  }

  /**
   * label for string05
   * @param labelString05a
   */
  public void setLabelString05(String labelString05a) {
    this.labelString05 = labelString05a;
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
   * make sure this object will fit in the DB
   */
  public void truncate() {
    this.actionName = GrouperUtil.truncateAscii(this.actionName, 50);
    this.changeLogCategory = GrouperUtil.truncateAscii(this.changeLogCategory, 50);
    this.contextId = GrouperUtil.truncateAscii(this.id, 128);
    this.id = GrouperUtil.truncateAscii(this.id, 128);
    this.labelString01 = GrouperUtil.truncateAscii(this.labelString01, 50);
    this.labelString02 = GrouperUtil.truncateAscii(this.labelString02, 50);
    this.labelString03 = GrouperUtil.truncateAscii(this.labelString03, 50);
    this.labelString04 = GrouperUtil.truncateAscii(this.labelString04, 50);
    this.labelString05 = GrouperUtil.truncateAscii(this.labelString05, 50);
    this.labelString06 = GrouperUtil.truncateAscii(this.labelString06, 50);
    this.labelString07 = GrouperUtil.truncateAscii(this.labelString07, 50);
    this.labelString08 = GrouperUtil.truncateAscii(this.labelString08, 50);
    this.labelString09 = GrouperUtil.truncateAscii(this.labelString09, 50);
    this.labelString10 = GrouperUtil.truncateAscii(this.labelString10, 50);
    this.labelString11 = GrouperUtil.truncateAscii(this.labelString11, 50);
    this.labelString12 = GrouperUtil.truncateAscii(this.labelString12, 50);
  }

  /**
   * label for the string06 field
   * @return label
   */
  public String getLabelString06() {
    return this.labelString06;
  }

  /**
   * label for the string06 field
   * @param labelString06a
   */
  public void setLabelString06(String labelString06a) {
    this.labelString06 = labelString06a;
  }

  /**
   * label for the string07 field
   * @return label
   */
  public String getLabelString07() {
    return this.labelString07;
  }

  /**
   * label for the string07 field
   * @param labelString07a
   */
  public void setLabelString07(String labelString07a) {
    this.labelString07 = labelString07a;
  }

  /**
   * label for the string08 field
   * @return label
   */
  public String getLabelString08() {
    return this.labelString08;
  }

  /**
   * label for the string08 field
   * @param labelString08a
   */
  public void setLabelString08(String labelString08a) {
    this.labelString08 = labelString08a;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    throw new RuntimeException("not implemented");
  }

  /**
   * label string 9
   * @return label string 9
   */
  public String getLabelString09() {
    return this.labelString09;
  }

  /**
   * label string 9
   * @param _labelString09
   */
  public void setLabelString09(String _labelString09) {
    this.labelString09 = _labelString09;
  }

  /**
   * label string 10
   * @return label string 10
   */
  public String getLabelString10() {
    return this.labelString10;
  }

  /**
   * label string 10
   * @param _labelString10
   */
  public void setLabelString10(String _labelString10) {
    this.labelString10 = _labelString10;
  }

  /**
   * label string 11
   * @return label string 11
   */
  public String getLabelString11() {
    return this.labelString11;
  }

  /**
   * label string 11
   * @param _labelString11
   */
  public void setLabelString11(String _labelString11) {
    this.labelString11 = _labelString11;
  }

  /**
   * label string 12
   * @return label string 12
   */
  public String getLabelString12() {
    return this.labelString12;
  }

  /**
   * label string 12
   * @param _labelString12
   */
  public void setLabelString12(String _labelString12) {
    this.labelString12 = _labelString12;
  }

  /**
   * see if this identifier matches the change log type by category and action
   * @param changeLogTypeIdentifier
   * @return true if matches
   */
  public boolean equalsCategoryAndAction(ChangeLogTypeIdentifier changeLogTypeIdentifier) {
    return this.getChangeLogCategory().equals(
        changeLogTypeIdentifier.getChangeLogCategory() )
      && this.getActionName().equals(
          changeLogTypeIdentifier.getActionName() );
  }

}
