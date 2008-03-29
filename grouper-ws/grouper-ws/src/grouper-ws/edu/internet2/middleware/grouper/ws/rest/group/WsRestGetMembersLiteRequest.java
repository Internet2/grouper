/*
 * @author mchyzer $Id: WsRestGetMembersLiteRequest.java,v 1.3 2008-03-29 10:50:43 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * request bean for rest get members request
 */
public class WsRestGetMembersLiteRequest implements WsRequestBean {

  /** client version */
  private String clientVersion;
  
  /** group name */
  private String groupName;
  
  /** group uuid */
  private String groupUuid;
  
  /** member filter */
  private String memberFilter;

  /** retrieveSubjectDetail */
  private String retrieveSubjectDetail;

  /** actAsSubjectId */
  private String actAsSubjectId;

  /** actAsSubjectSource */
  private String actAsSubjectSourceId;

  /** actAsSubjectIdentifier */
  private String actAsSubjectIdentifier;

  /** fieldName */
  private String fieldName;

  /** subjectAttributeNames */
  private String subjectAttributeNames;

  /** includeGroupDetail */
  private String includeGroupDetail;

  /** paramName0 */
  private String paramName0;

  /** paramValue0 */
  private String paramValue0;

  /** paramName1 */
  private String paramName1;

  /** paramValue1 */
  private String paramValue1;

  /**
   * 
   * @return
   */
  public String getMemberFilter() {
    return this.memberFilter;
  }

  /**
   * memberFilter1
   * @param memberFilter1
   */
  public void setMemberFilter(String memberFilter1) {
    this.memberFilter = memberFilter1;
  }

  /**
   * retrieveSubjectDetail
   * @return retrieveSubjectDetail
   */
  public String getRetrieveSubjectDetail() {
    return this.retrieveSubjectDetail;
  }

  /**
   * retrieveSubjectDetail1
   * @param retrieveSubjectDetail1
   */
  public void setRetrieveSubjectDetail(String retrieveSubjectDetail1) {
    this.retrieveSubjectDetail = retrieveSubjectDetail1;
  }

  /**
   * actAsSubjectId
   * @return actAsSubjectId
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * actAsSubjectId
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * actAsSubjectSource
   * @return actAsSubjectSource
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }

  /**
   * actAsSubjectSource
   * @param actAsSubjectSource1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSource1) {
    this.actAsSubjectSourceId = actAsSubjectSource1;
  }

  /**
   * actAsSubjectIdentifier
   * @return actAsSubjectIdentifier
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }

  /**
   * actAsSubjectIdentifier
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }

  /**
   * fieldName
   * @return fieldName
   */
  public String getFieldName() {
    return this.fieldName;
  }

  /**
   * fieldName
   * @param fieldName1
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }

  /**
   * subjectAttributeNames
   * @return subjectAttributeNames
   */
  public String getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * subjectAttributeNames
   * @param subjectAttributeNames1
   */
  public void setSubjectAttributeNames(String subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }

  /**
   * includeGroupDetail
   * @return includeGroupDetail
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  /**
   * includeGroupDetail
   * @param includeGroupDetail1
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }

  /**
   * paramName0
   * @return paramName0
   */
  public String getParamName0() {
    return this.paramName0;
  }

  /**
   * paramName0
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }

  /**
   * paramValue0
   * @return paramValue0
   */
  public String getParamValue0() {
    return this.paramValue0;
  }

  /**
   * _paramValue0
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }

  /**
   * paramName1
   * @return paramName1
   */
  public String getParamName1() {
    return this.paramName1;
  }

  /**
   * paramName1
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }

  /**
   * paramValue1
   * @return paramValue1
   */
  public String getParamValue1() {
    return this.paramValue1;
  }

  /**
   * paramValue1
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }
  
  /**
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }
  
  /**
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }
  
  /**
   * @return the groupUuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }
  
  /**
   * @param groupUuid1 the groupUuid to set
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }
  
  /**
   * @return the clientVersion
   */
  public String getClientVersion() {
    return this.clientVersion;
  }
  
  /**
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }
}
