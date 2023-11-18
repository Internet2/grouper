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
package edu.internet2.middleware.grouper.service;

import edu.internet2.middleware.grouper.GrouperAPI;

/**
 * 
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class ServiceRoleView extends GrouperAPI {

  /**
   * admin or user is the subject is an admin/updater/reader of a group in 
   * the service folder, or user is the subject is a member of a group in the service folder
   */
  private String serviceRoleDb;

  /**
   * group name in the service that the user is an admin or user of
   */
  private String groupName;

  /**
   * name of the service dev name, this generally is the system name of the service
   */
  private String nameOfServiceDefName; 
  
  /**
   * subject source id of the subject who is the admin or user of the service
   */
  private String subjectSourceId; 

  /**
   * subject id of the subject who is the admin or user of the service
   */
  private String subjectId; 

  /**
   * field of the membership of the subject who is the admin or user of the service
   */
  private String fieldName; 

  /**
   * name of the attribute definition of the service
   */
  private String nameOfServiceDef; 

  /**
   * display name of the group that the subject is an admin or user of the service
   */
  private String groupDisplayName; 

  /**
   * group id of the group that the subject is an admin or user of the service
   */
  private String groupId; 

  /**
   * id of the attribute definition that is related to the service
   */
  private String serviceDefId; 

  /**
   * id of the attribute name that is the service
   */
  private String serviceNameId; 

  /**
   * id in the member table of the subject who is an admin or user of the service
   */
  private String memberId; 

  /**
   * id of the field for the membership of the subject who an admin or user of the service
   */
  private String fieldId;

  /**
   * display name of the service definition name
   */
  private String displayNameOfServiceName;

  /**
   * id of the stem where the service tag is assigned
   */
  private String serviceStemId;
  
  /**
   * admin or user is the subject is an admin/updater/reader of a group in 
   * the service folder, or user is the subject is a member of a group in the service folder
   * @return service role db
   */
  public String getServiceRoleDb() {
    return this.serviceRoleDb;
  }

  /**
   * admin or user is the subject is an admin/updater/reader of a group in 
   * the service folder, or user is the subject is a member of a group in the service folder
   * @param serviceRoleDb1
   */
  public void setServiceRoleDb(String serviceRoleDb1) {
    this.serviceRoleDb = serviceRoleDb1;
  }

  /**
   * 
   * @return service role
   */
  public ServiceRole getServiceRole() {
    return ServiceRole.valueOfIgnoreCase(this.serviceRoleDb, false);
  }
  
  /**
   * 
   * @param theServiceRole
   */
  public void setServiceRole(ServiceRole theServiceRole) {
    this.serviceRoleDb = theServiceRole == null ? null : theServiceRole.name();
  }

  /**
   * group name in the service that the user is an admin or user of
   * @return group name
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * group name in the service that the user is an admin or user of
   * @param groupName1
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * name of the service dev name, this generally is the system name of the service
   * @return the name
   */
  public String getNameOfServiceDefName() {
    return this.nameOfServiceDefName;
  }

  /**
   * name of the service dev name, this generally is the system name of the service
   * @param nameOfserviceDefName1
   */
  public void setNameOfServiceDefName(String nameOfserviceDefName1) {
    this.nameOfServiceDefName = nameOfserviceDefName1;
  }

  /**
   * subject source id of the subject who is the admin or user of the service
   * @return subject source id
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  /**
   * subject source id of the subject who is the admin or user of the service
   * @param subjectSourceId1
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
  }
  
  /**
   * subject id of the subject who is the admin or user of the service
   * @return subject id
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subject id of the subject who is the admin or user of the service
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * field of the membership of the subject who is the admin or user of the service
   * @return field name
   */
  public String getFieldName() {
    return this.fieldName;
  }

  /**
   * field of the membership of the subject who is the admin or user of the service
   * @param fieldName1
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }
  
  /**
   * name of the attribute definition of the service
   * @return name of attribute definition
   */
  public String getNameOfServiceDef() {
    return this.nameOfServiceDef;
  }

  /**
   * name of the attribute definition of the service
   * @param nameOfServiceDef1
   */
  public void setNameOfServiceDef(String nameOfServiceDef1) {
    this.nameOfServiceDef = nameOfServiceDef1;
  }

  /**
   * display name of the group that the subject is an admin or user of the service
   * @return group display name
   */
  public String getGroupDisplayName() {
    return this.groupDisplayName;
  }

  /**
   * display name of the group that the subject is an admin or user of the service
   * @param groupDisplayName1
   */
  public void setGroupDisplayName(String groupDisplayName1) {
    this.groupDisplayName = groupDisplayName1;
  }
  
  /**
   * group id of the group that the subject is an admin or user of the service
   * @return group id
   */
  public String getGroupId() {
    return this.groupId;
  }

  /**
   * group id of the group that the subject is an admin or user of the service
   * @param groupId1
   */
  public void setGroupId(String groupId1) {
    this.groupId = groupId1;
  }

  /**
   * id of the attribute definition that is related to the service
   * @return service def id
   */
  public String getServiceDefId() {
    return this.serviceDefId;
  }

  /**
   * id of the attribute definition that is related to the service
   * @param serviceDefId1
   */
  public void setServiceDefId(String serviceDefId1) {
    this.serviceDefId = serviceDefId1;
  }

  /**
   * id of the attribute name that is the service
   * @return service name id
   */
  public String getServiceNameId() {
    return this.serviceNameId;
  }

  /**
   * id of the attribute name that is the service
   * @param serviceNameId1
   */
  public void setServiceNameId(String serviceNameId1) {
    this.serviceNameId = serviceNameId1;
  }
  
  /**
   * id in the member table of the subject who is an admin or user of the service
   * @return member id
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * id in the member table of the subject who is an admin or user of the service
   * @param memberId1
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }

  /**
   * id of the field for the membership of the subject who an admin or user of the service
   * @return field id
   */
  public String getFieldId() {
    return this.fieldId;
  }

  /**
   * id of the field for the membership of the subject who an admin or user of the service
   * @param fieldId1
   */
  public void setFieldId(String fieldId1) {
    this.fieldId = fieldId1;
  }

  /**
   * display name of the service definition name
   * @return display name
   */
  public String getDisplayNameOfServiceName() {
    return this.displayNameOfServiceName;
  }

  /**
   * display name of the service definition name
   * @param displayNameOfServiceName1
   */
  public void setDisplayNameOfServiceName(String displayNameOfServiceName1) {
    this.displayNameOfServiceName = displayNameOfServiceName1;
  }



  /**
   * id of the stem where the service tag is assigned
   * @return service stem id
   */
  public String getServiceStemId() {
    return serviceStemId;
  }

  /**
   * id of the stem where the service tag is assigned
   * @param serviceStemId1
   */
  public void setServiceStemId(String serviceStemId1) {
    this.serviceStemId = serviceStemId1;
  }

  /**
   * @see GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    throw new RuntimeException("Not implemented in " + this.getClass());
  }

  
  
}
