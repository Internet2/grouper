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
 * $Id: WsRestGroupSaveLiteRequest.java,v 1.1 2008-03-31 07:22:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;



/**
 * lite bean that will be the data from rest request
 * @see GrouperServiceLogic#getGroupsLite(edu.internet2.middleware.grouper.ws.GrouperWsVersion, String, String, String, edu.internet2.middleware.grouper.ws.member.WsMemberFilter, String, String, String, boolean, boolean, String, String, String, String, String)
 * for lite method
 */
public class WsRestGroupSaveLiteRequest implements WsRequestBean {

  /** type of group can be an enum of TypeOfGroup, e.g. group, role, entity */
  private String typeOfGroup;
  
  
  
  /**
   * @return type of group can be an enum of TypeOfGroup, e.g. group, role, entity
   */
  public String getTypeOfGroup() {
    return this.typeOfGroup;
  }
  
  /**
   * @param typeOfGroup1 type of group can be an enum of TypeOfGroup, e.g. group, role, entity
   */
  public void setTypeOfGroup(String typeOfGroup1) {
    this.typeOfGroup = typeOfGroup1;
  }
  
  /** field */
  private String groupLookupUuid;
  
  /** field */
  private String groupLookupName;
  
  /** field */
  private String description;
  
  /** field */
  private String displayExtension;
  
  /** field */
  private String saveMode;
  
  /** field */
  private String groupName; 
  
  /** field */
  private String groupUuid; 
  
  /** field */
  private String alternateName;
  
  /** 
   * field
   */
  private String clientVersion;
  
  /** field */
  private String actAsSubjectId;
  /** field */
  private String actAsSubjectSourceId;
  
  /** field */
  private String actAsSubjectIdentifier;
  /** field */
  private String paramName0;
  
  /** field */
  private String paramValue0;
  /** field */
  private String paramName1;
  /** field */
  private String paramValue1;

  /**
   * field 
   */
  private String includeGroupDetail;
  
  /**
   * field
   * @return field
   */
  public String getClientVersion() {
    return this.clientVersion;
  }
  /**
   * field
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }
  /**
   * field
   * @return field
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }
  
  /**
   * field
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }
  
  /**
   * field
   * @return field
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }
  
  /**
   * field
   * @param actAsSubjectSource1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSource1) {
    this.actAsSubjectSourceId = actAsSubjectSource1;
  }
  
  /**
   * field
   * @return field
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }
  
  /**
   * field
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }
  
  /**
   * field
   * @return field
   */
  public String getParamName0() {
    return this.paramName0;
  }
  /**
   * field
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }
  /**
   * field
   * @return field
   */
  public String getParamValue0() {
    return this.paramValue0;
  }
  /**
   * field
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }
  /**
   * field
   * @return field
   */
  public String getParamName1() {
    return this.paramName1;
  }
  /**
   * field
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }
  
  /**
   * field
   * @return field
   */
  public String getParamValue1() {
    return this.paramValue1;
  }
  
  /**
   * field
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }
  
  /**
   * field
   * @return the stemName
   */
  public String getGroupName() {
    return this.groupName;
  }
  
  /**
   * field
   * @param stemName1 the stemName to set
   */
  public void setGroupName(String stemName1) {
    this.groupName = stemName1;
  }
  
  /**
   * field
   * @return the groupUuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }
  
  /**
   * field
   * @param groupUuid1 the groupUuid to set
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }
  
  /**
   * field
   * @return the stemLookupUuid
   */
  public String getGroupLookupUuid() {
    return this.groupLookupUuid;
  }
  
  /**
   * field
   * @param stemLookupUuid1 the stemLookupUuid to set
   */
  public void setGroupLookupUuid(String stemLookupUuid1) {
    this.groupLookupUuid = stemLookupUuid1;
  }
  
  /**
   * field
   * @return the stemLookupName
   */
  public String getGroupLookupName() {
    return this.groupLookupName;
  }
  
  /**
   * field
   * @param stemLookupName1 the stemLookupName to set
   */
  public void setGroupLookupName(String stemLookupName1) {
    this.groupLookupName = stemLookupName1;
  }
  
  /**
   * field
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }
  
  /**
   * field
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }
  
  /**
   * field
   * @return the displayExtension
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }
  
  /**
   * field
   * @param displayExtension1 the displayExtension to set
   */
  public void setDisplayExtension(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }
  
  /**
   * field
   * @return the saveMode
   */
  public String getSaveMode() {
    return this.saveMode;
  }
  
  /**
   * field
   * @param saveMode1 the saveMode to set
   */
  public void setSaveMode(String saveMode1) {
    this.saveMode = saveMode1;
  }
  /**
   * field
   * @return the includeGroupDetail
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }
  /**
   * field
   * @param includeGroupDetail1 the includeGroupDetail to set
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }

  
  /**
   * @return the alternateName
   */
  public String getAlternateName() {
    return this.alternateName;
  }
  
  /**
   * @param alternateName1 the alternateName to set
   */
  public void setAlternateName(String alternateName1) {
    this.alternateName = alternateName1;
  }
}
