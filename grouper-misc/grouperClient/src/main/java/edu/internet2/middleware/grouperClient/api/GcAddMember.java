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
 * $Id: GcAddMember.java,v 1.7 2009-12-13 06:33:06 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAddMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.morphString.Crypto;


/**
 * class to run an add member web service call
 */
public class GcAddMember {


  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   */
  private String wsEndpoint;

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   * @param theWsEndpoint
   * @return this for chaining
   */
  public GcAddMember assignWsEndpoint(String theWsEndpoint) {
    this.wsEndpoint = theWsEndpoint;
    return this;
  }
  
  /**
   * ws user
   */
  private String wsUser;

  /**
   * ws user
   * @param theWsUser
   * @return this for chaining
   */
  public GcAddMember assignWsUser(String theWsUser) {
    this.wsUser = theWsUser;
    return this;
  }
  
  /**
   * ws pass
   */
  private String wsPass;

  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcAddMember assignWsPass(String theWsPass) {
    this.wsPass = theWsPass;
    return this;
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcAddMember assignWsPassEncrypted(String theWsPassEncrypted) {
    String encryptKey = GrouperClientUtils.encryptKey();
    return this.assignWsPass(new Crypto(encryptKey).decrypt(theWsPassEncrypted));
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcAddMember assignWsPassFile(File theFile) {
    return this.assignWsPass(GrouperClientUtils.readFileIntoString(theFile));
  }

  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcAddMember assignWsPassFileEncrypted(File theFile) {
    return this.assignWsPassEncrypted(GrouperClientUtils.readFileIntoString(theFile));
  }

  /** group name to add member to */
  private String groupName;
  
  /** group uuid to add member to */
  private String groupUuid;
  
  /** date this membership will be disabled, yyyy/MM/dd HH:mm:ss.SSS */
  private Timestamp disabledTime;
  
  /**
   * date this membership will be disabled, yyyy/MM/dd HH:mm:ss.SSS
   * @param theDisabledTime
   */
  public void assignDisabledTime(Timestamp theDisabledTime) {
    this.disabledTime = theDisabledTime;
  }
  
  /** date this membership will be enabled (for future provisioning), yyyy/MM/dd HH:mm:ss.SSS */
  private Timestamp enabledTime; 
  
  /**
   * date this membership will be enabled (for future provisioning), yyyy/MM/dd HH:mm:ss.SSS
   * @param theEnabledTime
   */
  public void assignEnabledTime(Timestamp theEnabledTime) {
    this.enabledTime = theEnabledTime;
  }
  
  
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcAddMember assignGroupName(String theGroupName) {
    this.groupName = theGroupName;
    return this;
  }
  
  /**
   * set the group uuid
   * @param theGroupUuid
   * @return this for chaining
   */
  public GcAddMember assignGroupUuid(String theGroupUuid) {
    this.groupUuid = theGroupUuid;
    return this;
  }
  
  /**
   * set the group id index
   * @param theGroupIdIndex
   * @return this for chaining
   */
  public GcAddMember assignGroupIdIndex(Long theGroupIdIndex) {
    this.groupIdIndex = theGroupIdIndex;
    return this;
  }

  /**
   * if referring to the group by id index
   */
  private Long groupIdIndex = null;
  
  /** subject lookups */
  private List<WsSubjectLookup> subjectLookups = new ArrayList<WsSubjectLookup>();

  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcAddMember addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAddMember addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcAddMember addSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectId
   * @return this for chaining
   */
  public GcAddMember addSubjectId(String subjectId) {
    this.subjectLookups.add(new WsSubjectLookup(subjectId, null, null));
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectIdentifier
   * @return this for chaining
   */
  public GcAddMember addSubjectIdentifier(String subjectIdentifier) {
    this.subjectLookups.add(new WsSubjectLookup(null, null, subjectIdentifier));
    return this;
  }
  
  /** if we should replace all existing */
  private Boolean replaceAllExisting = null;

  /**
   * set if we should replace all existing members with new list
   * @param isReplaceAllExisting
   * @return this for chaining
   */
  public GcAddMember assignReplaceAllExisting(Boolean isReplaceAllExisting) {
    this.replaceAllExisting = isReplaceAllExisting;
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcAddMember assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(this.groupName) && GrouperClientUtils.isBlank(this.groupUuid) && GrouperClientUtils.isBlank(this.groupIdIndex)) {
      throw new RuntimeException("Group name or uuid or idIndex is required: " + this);
    }
    //if we arent replacing, we need a subject to add.  if replacing, we might be removing all
    if (GrouperClientUtils.length(this.subjectLookups) == 0 && (this.replaceAllExisting == null || this.replaceAllExisting == false)) {
      throw new RuntimeException("Need at least one subject to add to group: " + this);
    }
  }
  
  /** field name to add member */
  private String fieldName;
  
  /**
   * assign the field name to the request
   * @param theFieldName
   * @return this for chaining
   */
  public GcAddMember assignFieldName(String theFieldName) {
    this.fieldName = theFieldName;
    return this;
  }
  
  /** tx type for request */
  private GcTransactionType txType;

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcAddMember assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcAddMember addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcAddMember assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * addExternalSubjectIfNotFound T or F, if this is a search by id or identifier, with no source, or the external source,
   * and the subject is not found, then add an external subject (if the user is allowed
   */
  private Boolean addExternalSubjectIfNotFound;
  
  /**
   * addExternalSubjectIfNotFound T or F, if this is a search by id or identifier, with no source, or the external source,
   * and the subject is not found, then add an external subject (if the user is allowed
   * @param theAssignAddExternalSubjectIfNotFound
   * @return this for chaining
   */
  public GcAddMember assignAddExternalSubjectIfNotFound(Boolean theAssignAddExternalSubjectIfNotFound) {
    this.addExternalSubjectIfNotFound = theAssignAddExternalSubjectIfNotFound;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcAddMember assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAddMember assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsAddMemberResults execute() {
    this.validate();
    WsAddMemberResults wsAddMemberResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAddMemberRequest addMember = new WsRestAddMemberRequest();

      addMember.setActAsSubjectLookup(this.actAsSubject);

      addMember.setFieldName(this.fieldName);
      
      addMember.setTxType(this.txType == null ? null : this.txType.name());
      
      if (this.replaceAllExisting != null) {
        addMember.setReplaceAllExisting(this.replaceAllExisting ? "T" : "F");
      }

      if (this.includeGroupDetail != null) {
        addMember.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        addMember.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (this.addExternalSubjectIfNotFound != null) {
        addMember.setAddExternalSubjectIfNotFound(this.addExternalSubjectIfNotFound ? "T" : "F");
      }
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      wsGroupLookup.setGroupName(this.groupName);
      wsGroupLookup.setUuid(this.groupUuid);
      wsGroupLookup.setIdIndex(this.groupIdIndex == null ? null : this.groupIdIndex.toString());
      
      addMember.setWsGroupLookup(wsGroupLookup);
      
      if (this.subjectAttributeNames.size() > 0) {
        addMember.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      WsSubjectLookup[] subjectLookupsResults = GrouperClientUtils.toArray(this.subjectLookups, 
          WsSubjectLookup.class);
      addMember.setSubjectLookups(subjectLookupsResults);

      //add params if there are any
      if (this.params.size() > 0) {
        addMember.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      addMember.setDisabledTime(GrouperClientUtils.dateToString(this.disabledTime));
      addMember.setEnabledTime(GrouperClientUtils.dateToString(this.enabledTime));
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      grouperClientWs.assignWsUser(this.wsUser);
      grouperClientWs.assignWsPass(this.wsPass);
      grouperClientWs.assignWsEndpoint(this.wsEndpoint);
      
      //kick off the web service
      //MCH dont fill all this in since could be uuid based
      //String urlSuffix = "groups/" 
      //    + GrouperClientUtils.escapeUrlEncode(this.groupName) + "/members";
      String urlSuffix = "groups";
      wsAddMemberResults = (WsAddMemberResults)
        grouperClientWs.executeService(urlSuffix, addMember, "addMember", this.clientVersion, false);
      
      String resultMessage = wsAddMemberResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsAddMemberResults, wsAddMemberResults.getResults(), resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAddMemberResults;
    
  }
  
}
