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
 * @author mchyzer $Id: WsRestMemberChangeSubjectRequest.java,v 1.1 2008-12-04 07:51:39 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * request bean for rest member change subject lite
 */
public class WsRestMemberChangeSubjectRequest implements WsRequestBean {

  /**
   * field 
   */
  private WsSubjectLookup actAsSubjectLookup;
  /**
   * field 
   */
  private String clientVersion;
  /**
   * field 
   */
  private String includeSubjectDetail;
  /**
   * field 
   */
  private WsParam[] params;
  
  /**
   * members to change subjects
   */
  private WsMemberChangeSubject[] wsMemberChangeSubjects;
  
  /**
   * field 
   */
  private String[] subjectAttributeNames;
  /**
   * field 
   */
  private String txType;

  /**
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * @return the clientVersion
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * @return the includeSubjectDetail
   */
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }

  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * @return the subjectAttributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * @return the txType
   */
  public String getTxType() {
    return this.txType;
  }

  /**
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  /**
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /**
   * @param includeSubjectDetail1 the includeSubjectDetail to set
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }

  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * @param subjectAttributeNames1 the subjectAttributeNames to set
   */
  public void setSubjectAttributeNames(String[] subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }

  /**
   * @param txType1 the txType to set
   */
  public void setTxType(String txType1) {
    this.txType = txType1;
  }

  /**
   * members to change subjects
   * @return the members
   */
  public WsMemberChangeSubject[] getWsMemberChangeSubjects() {
    return this.wsMemberChangeSubjects;
  }

  /**
   * members to change subjects
   * @param wsMemberChangeSubjects1
   */
  public void setWsMemberChangeSubjects(WsMemberChangeSubject[] wsMemberChangeSubjects1) {
    this.wsMemberChangeSubjects = wsMemberChangeSubjects1;
  }
}
