/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ******************************************************************************/

package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * lite bean that will be the data from rest request
 */
public class WsRestAttributeDefDeleteLiteRequest implements WsRequestBean {

  /** field */
  private String nameOfAttributeDef;

  /** field */
  private String idOfAttributeDef;

  /** field */
  private String idIndexOfAttributeDef;

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
   * @return nameOfAttributeDef
   */
  public String getNameOfAttributeDef() {
    return this.nameOfAttributeDef;
  }

  /**
   * @param nameOfAttributeDef1
   */
  public void setNameOfAttributeDef(String nameOfAttributeDef1) {
    this.nameOfAttributeDef = nameOfAttributeDef1;
  }

  /**
   * @return idOfAttributeDef
   */
  public String getIdOfAttributeDef() {
    return this.idOfAttributeDef;
  }

  /**
   * @param idOfAttributeDef1
   */
  public void setIdOfAttributeDef(String idOfAttributeDef1) {
    this.idOfAttributeDef = idOfAttributeDef1;
  }

  /**
   * @return idIndexOfAttributeDef
   */
  public String getIdIndexOfAttributeDef() {
    return this.idIndexOfAttributeDef;
  }

  /**
   * @param idIndexOfAttributeDef1
   */
  public void setIdIndexOfAttributeDef(String idIndexOfAttributeDef1) {
    this.idIndexOfAttributeDef = idIndexOfAttributeDef1;
  }

  /**
   * field
   * @return field
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /**
   * @return actAsSubjectId
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * @return actAsSubjectSourceId
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }

  /**
   * @param actAsSubjectSource1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSource1) {
    this.actAsSubjectSourceId = actAsSubjectSource1;
  }

  /**
   * @return actAsSubjectIdentifier
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }

  /**
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }

  /**
   * @return paramName0
   */
  public String getParamName0() {
    return this.paramName0;
  }

  /**
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }

  /**
   * @return paramValue0
   */
  public String getParamValue0() {
    return this.paramValue0;
  }

  /**
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }

  /**
   * @return paramName1
   */
  public String getParamName1() {
    return this.paramName1;
  }

  /**
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }

  /**
   * @return paramValue1
   */
  public String getParamValue1() {
    return this.paramValue1;
  }

  /**
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.DELETE;
  }

}
