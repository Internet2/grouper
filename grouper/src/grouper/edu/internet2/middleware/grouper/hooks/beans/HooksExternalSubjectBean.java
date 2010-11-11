/*
 * @author mchyzer
 * $Id: HooksAttributeAssignBean.java 6923 2010-08-11 05:06:01Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * bean to hold objects for attribute def name low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksExternalSubjectBean extends HooksBean {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: externalSubject */
  public static final String FIELD_EXTERNAL_SUBJECT = "externalSubject";

  /** constant for field name for: externalSubjectAttributes */
  public static final String FIELD_EXTERNAL_SUBJECT_ATTRIBUTES = "externalSubjectAttributes";

  /** constant for field name for: externalSubjectInviteName */
  public static final String FIELD_EXTERNAL_SUBJECT_INVITE_NAME = "externalSubjectInviteName";

  /** constant for field name for: insert */
  public static final String FIELD_INSERT = "insert";

  /** constant for field name for: update */
  public static final String FIELD_UPDATE = "update";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_EXTERNAL_SUBJECT, FIELD_EXTERNAL_SUBJECT_ATTRIBUTES, FIELD_EXTERNAL_SUBJECT_INVITE_NAME, FIELD_INSERT, 
      FIELD_UPDATE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private ExternalSubject externalSubject = null;
  
  /** the attributes that will be saved with the subject */
  private Set<ExternalSubjectAttribute> externalSubjectAttributes = null;

  /** if this is an insert */
  private boolean insert = false;
  
  /** if this is an update */
  private boolean update = false;
  
  /** external subject invite name, in url to have different hooks */
  private String externalSubjectInviteName = null;

  
  
  /**
   * object being affected
   * @return external subject
   */
  public ExternalSubject getExternalSubject() {
    return this.externalSubject;
  }

  /**
   * object being affected
   * @param theExternalSubject
   */
  public void setExternalSubject(ExternalSubject theExternalSubject) {
    this.externalSubject = theExternalSubject;
  }

  /**
   * the attributes that will be saved with the subject
   * @return the attributes
   */
  public Set<ExternalSubjectAttribute> getExternalSubjectAttributes() {
    return this.externalSubjectAttributes;
  }

  /**
   * the attributes that will be saved with the subject
   * @param theExternalSubjectAttributes
   */
  public void setExternalSubjectAttributes(
      Set<ExternalSubjectAttribute> theExternalSubjectAttributes) {
    this.externalSubjectAttributes = theExternalSubjectAttributes;
  }

  /**
   * if this is an insert
   * @return if insert
   */
  public boolean isInsert() {
    return this.insert;
  }

  /**
   * if this is an insert
   * @param insert1
   */
  public void setInsert(boolean insert1) {
    this.insert = insert1;
  }

  /**
   * if this is an update
   * @return if udpate
   */
  public boolean isUpdate() {
    return this.update;
  }

  /**
   * if this is an update
   * @param update1
   */
  public void setUpdate(boolean update1) {
    this.update = update1;
  }

  /**
   * external subject invite name, in url to have different hooks
   * @return name
   */
  public String getExternalSubjectInviteName() {
    return this.externalSubjectInviteName;
  }

  /**
   * external subject invite name, in url to have different hooks
   * @param externalSubjectInviteName1
   */
  public void setExternalSubjectInviteName(String externalSubjectInviteName1) {
    this.externalSubjectInviteName = externalSubjectInviteName1;
  }

  /**
   * 
   */
  public HooksExternalSubjectBean() {
    super();
  }

  /**
   * call this when editing the external subject, insert or udpate
   * @param theExternalSubject 
   * @param isInsert 
   * @param isUpdate 
   * @param theExternalSubjectAttributes 
   * @param theExternalSubjectInviteName 
   * @param theAttribute 
   */
  public HooksExternalSubjectBean(ExternalSubject theExternalSubject, boolean isInsert, boolean isUpdate, 
      Set<ExternalSubjectAttribute> theExternalSubjectAttributes, String theExternalSubjectInviteName) {
    this.externalSubject = theExternalSubject;
    this.insert = isInsert;
    this.update = isUpdate;
    this.externalSubjectAttributes = theExternalSubjectAttributes;
    this.externalSubjectInviteName = theExternalSubjectInviteName;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksExternalSubjectBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

}
