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
 * $Id: GcMemberChangeSubject.java,v 1.1 2008-12-08 02:55:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsMemberChangeSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestMemberChangeSubjectRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a member change subject
 */
public class GcMemberChangeSubject {

  /** subject lookup */
  private WsSubjectLookup oldSubjectLookup;

  /**
   * assign the old subject lookup
   * @param theOldSubjectLookup
   * @return this for chaining
   */
  public GcMemberChangeSubject assignOldSubjectLookup(WsSubjectLookup theOldSubjectLookup) {
    this.oldSubjectLookup = theOldSubjectLookup;
    return this;
  }

  /** subject lookup */
  private WsSubjectLookup newSubjectLookup;

  /**
   * assign the new subject lookup
   * @param theNewSubjectLookup
   * @return this for chaining
   */
  public GcMemberChangeSubject assignNewSubjectLookup(WsSubjectLookup theNewSubjectLookup) {
    this.newSubjectLookup = theNewSubjectLookup;
    return this;
  }

  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcMemberChangeSubject addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcMemberChangeSubject addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcMemberChangeSubject assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (this.oldSubjectLookup == null) {
      throw new RuntimeException("Old subject lookup is required: " + this);
    }
    if (this.newSubjectLookup == null) {
      throw new RuntimeException("New subject lookup is required: " + this);
    }
  }
  
  /** if the old member should be deleted if applicable */
  private Boolean deleteOldMember;
  
  /**
   * if the old member should be deleted if applicable
   * @param isDeleteOldMember
   * @return this for chaining
   */
  public GcMemberChangeSubject assignDeleteOldMember(boolean isDeleteOldMember) {
    this.deleteOldMember = isDeleteOldMember;
    return this;
  }
  
  /** tx type for request */
  private GcTransactionType txType;

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcMemberChangeSubject assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcMemberChangeSubject addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcMemberChangeSubject assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
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
  public GcMemberChangeSubject assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsMemberChangeSubjectResults execute() {
    this.validate();
    WsMemberChangeSubjectResults wsMemberChangeSubjectResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestMemberChangeSubjectRequest memberChangeSubject = new WsRestMemberChangeSubjectRequest();

      memberChangeSubject.setActAsSubjectLookup(this.actAsSubject);
      
      WsMemberChangeSubject wsMemberChangeSubject = new WsMemberChangeSubject();
      if (this.deleteOldMember != null) {
        wsMemberChangeSubject.setDeleteOldMember(this.deleteOldMember ? "T" : "F");
      }
      wsMemberChangeSubject.setOldSubjectLookup(this.oldSubjectLookup);
      wsMemberChangeSubject.setNewSubjectLookup(this.newSubjectLookup);
      memberChangeSubject.setWsMemberChangeSubjects(new WsMemberChangeSubject[]{wsMemberChangeSubject});
      
      memberChangeSubject.setTxType(this.txType == null ? null : this.txType.name());
      
      if (this.includeSubjectDetail != null) {
        memberChangeSubject.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        memberChangeSubject.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      //add params if there are any
      if (this.params.size() > 0) {
        memberChangeSubject.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsMemberChangeSubjectResults = (WsMemberChangeSubjectResults)
        grouperClientWs.executeService("members", memberChangeSubject, "memberChangeSubject", this.clientVersion, false);
      
      String resultMessage = wsMemberChangeSubjectResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsMemberChangeSubjectResults, wsMemberChangeSubjectResults.getResults(),resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsMemberChangeSubjectResults;
    
  }
  
}
