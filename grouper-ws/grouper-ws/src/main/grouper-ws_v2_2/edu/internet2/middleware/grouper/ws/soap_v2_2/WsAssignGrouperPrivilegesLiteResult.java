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
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_2;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Result of assigning or removing a privilege
 * 
 * @author mchyzer
 */
public class WsAssignGrouperPrivilegesLiteResult {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * empty
   */
  public WsAssignGrouperPrivilegesLiteResult() {
    //empty
  }

  /**
    * metadata about the result
    */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  
  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  /**
   * field 
   */
  private WsParam[] params;

  /**
   * whether this privilege is allowed T/F 
   */
  private String allowed;

  /**
   * privilege name, e.g. read, update, stem 
   */
  private String privilegeName;

  /**
   * privilege type, e.g. naming, or access 
   */
  private String privilegeType;

  /**
   * group querying 
   */
  private WsGroup wsGroup;

  /**
   * stem querying 
   */
  private WsStem wsStem;

  /**
   * subject to switch to
   */
  private WsSubject wsSubject;


  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * whether this privilege is allowed T/F
   * @return if allowed
   */
  public String getAllowed() {
    return this.allowed;
  }

  /**
   * privilege type, e.g. naming, or access
   * @return the name
   */
  public String getPrivilegeName() {
    return this.privilegeName;
  }

  /**
   * privilege type, e.g. naming, or access
   * @return the type
   */
  public String getPrivilegeType() {
    return this.privilegeType;
  }

  /**
   * group querying
   * @return the group
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * stem querying
   * @return the stem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * subject that was changed to
   * @return the subjectId
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * whether this privilege is allowed T/F
   * @param allowed1
   */
  public void setAllowed(String allowed1) {
    this.allowed = allowed1;
  }

  /**
   * privilege type, e.g. naming, or access
   * @param privilegeName1
   */
  public void setPrivilegeName(String privilegeName1) {
    this.privilegeName = privilegeName1;
  }

  /**
   * privilege type, e.g. naming, or access
   * @param privilegeType1
   */
  public void setPrivilegeType(String privilegeType1) {
    this.privilegeType = privilegeType1;
  }

  /**
   * group querying
   * @param wsGroup1
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * stem querying
   * @param wsStem1
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }

  /**
   * subject that was changed to
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }
}
