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
package edu.internet2.middleware.grouper.ws.soap_v2_0;


/**
 * subject bean for web services
 * 
 * @author mchyzer
 * 
 */
public class WsSubject {

  /** if lookedup by identifier, this is that identifier */
  private String identifierLookup;

  /**
   * identifier used to lookup subject
   * @return the identifier
   */
  public String getIdentifierLookup() {
    return this.identifierLookup;
  }

  /**
   * return the identifier looked up
   * @param identifierLookup1
   */
  public void setIdentifierLookup(String identifierLookup1) {
    this.identifierLookup = identifierLookup1;
  }

  /** can be SUCCESS (T) or UNRESOLVABLE (F) */
  private String resultCode;

  /** T or F */
  private String success;

  /**
   * constructor
   */
  public WsSubject() {
    // blank
  }

  /** id of subject, note if no subject found, and identifier was passed in,
   * that will be placed here */
  private String id;

  /** name of subject */
  private String name;

  /** source of subject */
  private String sourceId;

  /**
   * attribute data of subjects in group (in same order as attributeNames)
   */
  private String[] attributeValues;

  /**
   * subject id, note if no subject found, and identifier was passed in,
   * that will be placed here
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * subject id, note if no subject found, and identifier was passed in,
   * that will be placed here
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @param name1
   *            the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * if attributes are being sent back per config in the grouper.properties,
   * this is attribute0 value, this is extended subject data
   * 
   * @return the attribute0
   */
  public String[] getAttributeValues() {
    return this.attributeValues;
  }

  /**
   * attribute data of subjects in group (in same order as attributeNames)
   * 
   * @param attributesa
   *            the attributes to set
   */
  public void setAttributeValues(String[] attributesa) {
    this.attributeValues = attributesa;
  }

  /**
   * @return the source
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * @param source1 the source to set
   */
  public void setSourceId(String source1) {
    this.sourceId = source1;
  }

  /**
   * @return the resultCode
   */
  public String getResultCode() {
    return this.resultCode;
  }

  /**
   * @param resultCode1 the resultCode to set
   */
  public void setResultCode(String resultCode1) {
    this.resultCode = resultCode1;
  }

  /**
   * T or F for success
   * @return the success
   */
  public String getSuccess() {
    return this.success;
  }

  /**
   * T or F for success
   * @param success1 the success to set
   */
  public void setSuccess(String success1) {
    this.success = success1;
  }

}
