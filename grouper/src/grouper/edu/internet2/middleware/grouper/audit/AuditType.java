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
 * @author mchyzer
 * $Id: AuditType.java,v 1.4 2009-04-15 15:56:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAuditType;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;


/**
 * type of audit
 */
@SuppressWarnings("serial")
public class AuditType extends GrouperAPI implements Hib3GrouperVersioned, XmlImportable<AuditType> {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: actionName */
  public static final String FIELD_ACTION_NAME = "actionName";

  /** constant for field name for: auditCategory */
  public static final String FIELD_AUDIT_CATEGORY = "auditCategory";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: labelInt01 */
  public static final String FIELD_LABEL_INT01 = "labelInt01";

  /** constant for field name for: labelInt02 */
  public static final String FIELD_LABEL_INT02 = "labelInt02";

  /** constant for field name for: labelInt03 */
  public static final String FIELD_LABEL_INT03 = "labelInt03";

  /** constant for field name for: labelInt04 */
  public static final String FIELD_LABEL_INT04 = "labelInt04";

  /** constant for field name for: labelInt05 */
  public static final String FIELD_LABEL_INT05 = "labelInt05";

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

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * fields in to string deep method
   */
  private static final Set<String> TO_STRING_DEEP_FIELDS = GrouperUtil.toSet(
      FIELD_ACTION_NAME, FIELD_AUDIT_CATEGORY, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, 
      FIELD_ID, FIELD_LABEL_INT01, FIELD_LABEL_INT02, FIELD_LABEL_INT03, 
      FIELD_LABEL_INT04, FIELD_LABEL_INT05, FIELD_LABEL_STRING01, FIELD_LABEL_STRING02, 
      FIELD_LABEL_STRING03, FIELD_LABEL_STRING04, FIELD_LABEL_STRING05, FIELD_LABEL_STRING06, 
      FIELD_LABEL_STRING07, FIELD_LABEL_STRING08, FIELD_LAST_UPDATED_DB);


  
  /**
   * empty constructor
   */
  public AuditType() {
    
  }
  
  /**
   * see if one audit type is the same as another (not looking at last update, id, etc)
   * @param auditType
   * @return true if equals, false if not
   */
  public boolean equalsDeep(AuditType auditType) {
    
    return new EqualsBuilder().append(this.actionName, auditType.actionName)
      .append(this.auditCategory, auditType.auditCategory)
      .append(this.labelInt01, auditType.labelInt01)
      .append(this.labelInt02, auditType.labelInt02)
      .append(this.labelInt03, auditType.labelInt03)
      .append(this.labelInt04, auditType.labelInt04)
      .append(this.labelInt05, auditType.labelInt05)
      .append(this.labelString01, auditType.labelString01)
      .append(this.labelString02, auditType.labelString02)
      .append(this.labelString03, auditType.labelString03)
      .append(this.labelString04, auditType.labelString04)
      .append(this.labelString05, auditType.labelString05)
      .append(this.labelString06, auditType.labelString06)
      .append(this.labelString07, auditType.labelString07)
      .append(this.labelString08, auditType.labelString08).isEquals();
      
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

    if (!StringUtils.isBlank(this.labelInt01)) {
      labels.add(this.labelInt01);
    }
    if (!StringUtils.isBlank(this.labelInt02)) {
      labels.add(this.labelInt02);
    }
    if (!StringUtils.isBlank(this.labelInt03)) {
      labels.add(this.labelInt03);
    }
    if (!StringUtils.isBlank(this.labelInt04)) {
      labels.add(this.labelInt04);
    }
    if (!StringUtils.isBlank(this.labelInt05)) {
      labels.add(this.labelInt05);
    }
    return labels;
  }
  
  /**
   * get the field in audit entry for this label
   * @param label
   * @return the field
   */
  public String retrieveAuditEntryFieldForLabel(String label) {
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

    if (StringUtils.equals(label, this.labelInt01)) {
      return "int01";
    }
    if (StringUtils.equals(label, this.labelInt02)) {
      return "int02";
    }
    if (StringUtils.equals(label, this.labelInt03)) {
      return "int03";
    }
    if (StringUtils.equals(label, this.labelInt04)) {
      return "int04";
    }
    if (StringUtils.equals(label, this.labelInt05)) {
      return "int05";
    }

    throw new RuntimeException("Cant find label '" + label + "' for type: " + this);
  }
  
  /**
   * copy the argument into this
   * @param auditType
   */
  public void copyArgFieldIntoThis(AuditType auditType) {
    this.actionName = auditType.actionName;
    this.auditCategory = auditType.auditCategory;
    this.labelInt01 = auditType.labelInt01;
    this.labelInt02 = auditType.labelInt02;
    this.labelInt03 = auditType.labelInt03;
    this.labelInt04 = auditType.labelInt04;
    this.labelInt05 = auditType.labelInt05;
    this.labelString01 = auditType.labelString01;
    this.labelString02 = auditType.labelString02;
    this.labelString03 = auditType.labelString03;
    this.labelString04 = auditType.labelString04;
    this.labelString05 = auditType.labelString05;
    this.labelString06 = auditType.labelString06;
    this.labelString07 = auditType.labelString07;
    this.labelString08 = auditType.labelString08;
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
    return "Audit type: " + this.auditCategory + ": " + this.actionName;
  }
  
  /**
   * construct with more params
   * @param auditCategory1
   * @param actionName1
   * @param labelInt01a
   * @param labelStrings up to 8 label strings
   */
  public AuditType(String auditCategory1, String actionName1, String labelInt01a, String... labelStrings) {
    this.auditCategory = auditCategory1;
    this.actionName = actionName1;
    this.labelInt01 = labelInt01a;

    int index=1;
    for (String labelString : GrouperUtil.nonNull(labelStrings, String.class)) {
      GrouperUtil.assignField(this, "labelString" + (index<10 ? "0" : "") + index, labelString);
      if (index > 8) {
        throw new RuntimeException("Cant send more than 8 labelStrings: " + labelStrings.length);
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
    if (!(obj instanceof AuditType)) {
      return false;
    }
    AuditType otherAuditType = (AuditType)obj;
    return new EqualsBuilder().append(this.auditCategory, otherAuditType.auditCategory)
      .append(this.actionName, otherAuditType.actionName).isEquals();
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.auditCategory)
      .append(this.actionName).hashCode();
  }

  /** name of the grouper audit type table in the db */
  public static final String TABLE_GROUPER_AUDIT_TYPE = "grouper_audit_type";
  
  /** id of this type */
  private String id;

  /** friendly label for the audit type */
  private String auditCategory;
  
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
  
  /** label for the string08 field */
  private String labelString08;
  
  /** context id ties multiple db changes  */
  private String contextId;
  
  /** label for the int01 field */
  private String labelInt01;
  
  /** label for the int02 field */
  private String labelInt02;

  /** label for the int03 field */
  private String labelInt03;
  
  /** label for the int04 field */
  private String labelInt04;
  
  /** label for the int05 field */
  private String labelInt05;

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
   * category of audit
   * @return audit type
   */
  public String getAuditCategory() {
    return this.auditCategory;
  }

  /**
   * category of audit
   * @param auditType1
   */
  public void setAuditCategory(String auditType1) {
    this.auditCategory = auditType1;
  }

  /**
   * action within the audit category
   * @return the action name
   */
  public String getActionName() {
    return this.actionName;
  }

  /**
   * action within the audit category
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
   * label for int01
   * @return label
   */
  public String getLabelInt01() {
    return this.labelInt01;
  }

  /**
   * label for int01
   * @param labelInt01a
   */
  public void setLabelInt01(String labelInt01a) {
    this.labelInt01 = labelInt01a;
  }

  /**
   * label for int02
   * @return label
   */
  public String getLabelInt02() {
    return this.labelInt02;
  }

  /**
   * label for int02
   * @param labelInt02a
   */
  public void setLabelInt02(String labelInt02a) {
    this.labelInt02 = labelInt02a;
  }
  
  /**
   * label for int03
   * @return label
   */
  public String getLabelInt03() {
    return this.labelInt03;
  }

  /**
   * label for int03
   * @param labelInt03a
   */
  public void setLabelInt03(String labelInt03a) {
    this.labelInt03 = labelInt03a;
  }

  /**
   * label for int04
   * @return label
   */
  public String getLabelInt04() {
    return this.labelInt04;
  }

  /**
   * label for int04
   * @param labelInt04a
   */
  public void setLabelInt04(String labelInt04a) {
    this.labelInt04 = labelInt04a;
  }

  /**
   * label for int05
   * @return label
   */
  public String getLabelInt05() {
    return this.labelInt05;
  }

  /**
   * label for int05
   * @param labelInt05
   */
  public void setLabelInt05(String labelInt05) {
    this.labelInt05 = labelInt05;
  }
  
  /**
   * make sure this object will fit in the DB
   */
  public void truncate() {
    this.actionName = GrouperUtil.truncateAscii(this.actionName, 50);
    this.auditCategory = GrouperUtil.truncateAscii(this.auditCategory, 50);
    this.contextId = GrouperUtil.truncateAscii(this.id, 128);
    this.id = GrouperUtil.truncateAscii(this.id, 128);
    this.labelInt01 = GrouperUtil.truncateAscii(this.labelInt01, 50);
    this.labelInt02 = GrouperUtil.truncateAscii(this.labelInt02, 50);
    this.labelInt03 = GrouperUtil.truncateAscii(this.labelInt03, 50);
    this.labelInt04 = GrouperUtil.truncateAscii(this.labelInt04, 50);
    this.labelInt05 = GrouperUtil.truncateAscii(this.labelInt05, 50);
    this.labelString01 = GrouperUtil.truncateAscii(this.labelString01, 50);
    this.labelString02 = GrouperUtil.truncateAscii(this.labelString02, 50);
    this.labelString03 = GrouperUtil.truncateAscii(this.labelString03, 50);
    this.labelString04 = GrouperUtil.truncateAscii(this.labelString04, 50);
    this.labelString05 = GrouperUtil.truncateAscii(this.labelString05, 50);
    this.labelString06 = GrouperUtil.truncateAscii(this.labelString06, 50);
    this.labelString07 = GrouperUtil.truncateAscii(this.labelString07, 50);
    this.labelString08 = GrouperUtil.truncateAscii(this.labelString08, 50);
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
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public XmlImportable<AuditType> xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getAuditType().findByUuidOrName(this.id, this.auditCategory, this.actionName, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(AuditType existingRecord) {
    existingRecord.setActionName(this.actionName);
    existingRecord.setAuditCategory(this.auditCategory);
    existingRecord.setId(this.id);
    existingRecord.setLabelInt01(this.labelInt01);
    existingRecord.setLabelInt02(this.labelInt02);
    existingRecord.setLabelInt03(this.labelInt03);
    existingRecord.setLabelInt04(this.labelInt04);
    existingRecord.setLabelInt05(this.labelInt05);
    existingRecord.setLabelString01(this.labelString01);
    existingRecord.setLabelString02(this.labelString02);
    existingRecord.setLabelString03(this.labelString03);
    existingRecord.setLabelString04(this.labelString04);
    existingRecord.setLabelString05(this.labelString05);
    existingRecord.setLabelString06(this.labelString06);
    existingRecord.setLabelString07(this.labelString07);
    existingRecord.setLabelString08(this.labelString08);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(AuditType other) {
    if (!StringUtils.equals(this.actionName, other.actionName)) {
      return true;
    }
    if (!StringUtils.equals(this.auditCategory, other.auditCategory)) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!StringUtils.equals(this.labelInt01, other.labelInt01)) {
      return true;
    }
    if (!StringUtils.equals(this.labelInt02, other.labelInt02)) {
      return true;
    }
    if (!StringUtils.equals(this.labelInt03, other.labelInt03)) {
      return true;
    }
    if (!StringUtils.equals(this.labelInt04, other.labelInt04)) {
      return true;
    }
    if (!StringUtils.equals(this.labelInt05, other.labelInt05)) {
      return true;
    }
    if (!StringUtils.equals(this.labelString01, other.labelString01)) {
      return true;
    }
    if (!StringUtils.equals(this.labelString02, other.labelString02)) {
      return true;
    }
    if (!StringUtils.equals(this.labelString03, other.labelString03)) {
      return true;
    }
    if (!StringUtils.equals(this.labelString04, other.labelString04)) {
      return true;
    }
    if (!StringUtils.equals(this.labelString05, other.labelString05)) {
      return true;
    }
    if (!StringUtils.equals(this.labelString06, other.labelString06)) {
      return true;
    }
    if (!StringUtils.equals(this.labelString07, other.labelString07)) {
      return true;
    }
    if (!StringUtils.equals(this.labelString08, other.labelString08)) {
      return true;
    }

    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(AuditType other) {
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
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlGetId()
   */
  public String xmlGetId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveBusinessProperties(java.lang.Object)
   */
  public AuditType xmlSaveBusinessProperties(AuditType existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      existingRecord = new AuditType();
      existingRecord.setAuditCategory(this.auditCategory);
      existingRecord.setActionName(this.actionName);
      existingRecord.setId(this.id);
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    GrouperDAOFactory.getFactory().getAuditType().saveOrUpdate(existingRecord);
    AuditTypeFinder.clearCache();
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAuditType().saveUpdateProperties(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setId(theId);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAuditType xmlToExportAuditType(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportAuditType xmlExportAuditType = new XmlExportAuditType();
    xmlExportAuditType.setActionName(this.getActionName());
    xmlExportAuditType.setAuditCategory(this.getAuditCategory());
    xmlExportAuditType.setContextId(this.getContextId());
    xmlExportAuditType.setCreatedOn(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportAuditType.setHibernateVersionNumber(GrouperUtil.longValue(this.getHibernateVersionNumber(), 0));
    xmlExportAuditType.setId(this.getId());
    xmlExportAuditType.setLabelInt01(this.getLabelInt01());
    xmlExportAuditType.setLabelInt02(this.getLabelInt02());
    xmlExportAuditType.setLabelInt03(this.getLabelInt03());
    xmlExportAuditType.setLabelInt04(this.getLabelInt04());
    xmlExportAuditType.setLabelInt05(this.getLabelInt05());
    xmlExportAuditType.setLabelString01(this.getLabelString01());
    xmlExportAuditType.setLabelString02(this.getLabelString02());
    xmlExportAuditType.setLabelString03(this.getLabelString03());
    xmlExportAuditType.setLabelString04(this.getLabelString04());
    xmlExportAuditType.setLabelString05(this.getLabelString05());
    xmlExportAuditType.setLabelString06(this.getLabelString06());
    xmlExportAuditType.setLabelString07(this.getLabelString07());
    xmlExportAuditType.setLabelString08(this.getLabelString08());
    xmlExportAuditType.setLastUpdated(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    return xmlExportAuditType;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("AuditType: " + this.getId() + ", " + this.auditCategory + " - " + this.actionName);

    return stringWriter.toString();
    
  }

}
