/*
 * @author mchyzer
 * $Id: AuditType.java,v 1.1 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.sql.Timestamp;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * type of audit
 */
@SuppressWarnings("serial")
public class AuditType extends GrouperAPI implements Hib3GrouperVersioned {

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
  private Timestamp lastUpdated;
  
  /** when this record was created */
  private Timestamp createdOn;
  
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
    return this.lastUpdated;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }

  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOn;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOn = createdOn1;
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

}
