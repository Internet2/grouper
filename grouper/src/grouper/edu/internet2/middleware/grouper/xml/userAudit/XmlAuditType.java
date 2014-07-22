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
 * $Id: XmlAuditType.java,v 1.1 2009-03-31 06:58:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.userAudit;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * type of audit
 */
@SuppressWarnings("serial")
public class XmlAuditType {

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
   * fields which are included in clone method
   */
  private static final Set<String> COPY_FIELDS = GrouperUtil.toSet(
      FIELD_ACTION_NAME, FIELD_AUDIT_CATEGORY, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, 
      FIELD_ID, FIELD_LABEL_INT01, FIELD_LABEL_INT02, FIELD_LABEL_INT03, 
      FIELD_LABEL_INT04, FIELD_LABEL_INT05, FIELD_LABEL_STRING01, FIELD_LABEL_STRING02, 
      FIELD_LABEL_STRING03, FIELD_LABEL_STRING04, FIELD_LABEL_STRING05, FIELD_LABEL_STRING06, 
      FIELD_LABEL_STRING07, FIELD_LABEL_STRING08, FIELD_LAST_UPDATED_DB);

  /**
   * 
   */
  public XmlAuditType() {
    super();
  }

  /**
   * construct based on xml audit type
   * @param auditType
   */
  public XmlAuditType(AuditType auditType) {
    
    //go through each field, and copy data over
    
    for (String fieldName : COPY_FIELDS) {
      Object auditTypeValue = GrouperUtil.fieldValue(auditType, fieldName);
      GrouperUtil.assignField(this, fieldName, auditTypeValue);
    }
    
  }

  /**
   * construct based on xml audit type
   * @return the audit type with all this data in there
   */
  public AuditType toAuditType() {
    
    AuditType auditType = new AuditType();
    
    //go through each field, and copy data over
    
    for (String fieldName : COPY_FIELDS) {
      Object auditTypeValue = GrouperUtil.fieldValue(this, fieldName);
      GrouperUtil.assignField(auditType, fieldName, auditTypeValue);
    }
    return auditType;
  }

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

}
