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

  /** constant for field name for: autoaddGroups */
  public static final String FIELD_AUTOADD_GROUPS = "autoaddGroups";

  /** constant for field name for: externalSubject */
  public static final String FIELD_EXTERNAL_SUBJECT = "externalSubject";

  /** constant for field name for: externalSubjectAttributes */
  public static final String FIELD_EXTERNAL_SUBJECT_ATTRIBUTES = "externalSubjectAttributes";

  /** constant for field name for: externalSubjectInviteName */
  public static final String FIELD_EXTERNAL_SUBJECT_INVITE_NAME = "externalSubjectInviteName";

  /** constant for field name for: fromRecalcDaemon */
  public static final String FIELD_FROM_RECALC_DAEMON = "fromRecalcDaemon";

  /** constant for field name for: insert */
  public static final String FIELD_INSERT = "insert";

  /** constant for field name for: update */
  public static final String FIELD_UPDATE = "update";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_AUTOADD_GROUPS, FIELD_EXTERNAL_SUBJECT, FIELD_EXTERNAL_SUBJECT_ATTRIBUTES, FIELD_EXTERNAL_SUBJECT_INVITE_NAME, 
      FIELD_FROM_RECALC_DAEMON, FIELD_INSERT, FIELD_UPDATE);

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

  /** if should autoadd groups */
  private boolean autoaddGroups = false;
  
  /** if from daemon */
  private boolean fromRecalcDaemon = false;
  
  
  /**
   * if should autoadd groups
   * @return the autoaddGroups
   */
  public boolean isAutoaddGroups() {
    return this.autoaddGroups;
  }

  
  /**
   * if should autoadd groups
   * @param autoaddGroups1 the autoaddGroups to set
   */
  public void setAutoaddGroups(boolean autoaddGroups1) {
    this.autoaddGroups = autoaddGroups1;
  }

  
  /**
   * if from daemon
   * @return the fromDaemon
   */
  public boolean isFromRecalcDaemon() {
    return this.fromRecalcDaemon;
  }

  
  /**
   * if from daemon
   * @param fromDaemon1 the fromDaemon to set
   */
  public void setFromRecalcDaemon(boolean fromDaemon1) {
    this.fromRecalcDaemon = fromDaemon1;
  }

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
   * @param theAutoAddGroups 
   * @param theFromDaemon 
   */
  public HooksExternalSubjectBean(ExternalSubject theExternalSubject, boolean isInsert, boolean isUpdate, 
      Set<ExternalSubjectAttribute> theExternalSubjectAttributes, String theExternalSubjectInviteName,
      boolean theAutoAddGroups, boolean theFromDaemon) {
    this.externalSubject = theExternalSubject;
    this.insert = isInsert;
    this.update = isUpdate;
    this.externalSubjectAttributes = theExternalSubjectAttributes;
    this.externalSubjectInviteName = theExternalSubjectInviteName;
    this.autoaddGroups = theAutoAddGroups;
    this.fromRecalcDaemon = theFromDaemon;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksExternalSubjectBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

}
