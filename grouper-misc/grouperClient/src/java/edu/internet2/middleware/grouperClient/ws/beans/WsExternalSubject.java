/*******************************************************************************
 * Copyright 2016 Internet2
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
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * Result of one external subject being retrieved.
 * 
 * @author mchyzer
 */
public class WsExternalSubject {

  /**
   * universally unique identifier of this external subject
   */
  private String uuid;

  /** 
   * description, which is generated from other attributes 
   */
  private String description;

  /** email address */
  private String email;

  /** is this is currently enabled T or F */
  private String enabled = "T";

  /** the thing that the subject uses to login */
  private String identifier;

  /** institution where the user is from */
  private String institution;

  /** name of subject */
  private String name;

  /** search string to find a subject, in all lower case */
  private String searchStringLower;

  /** comma separated vetted email addresses */
  private String vettedEmailAddresses;
  
  /**
   * no arg constructor
   */
  public WsExternalSubject() {
    //blank

  }

  /**
   * attributes
   */
  private WsExternalSubjectAttribute[] wsExternalSubjectAttributes;

  /**
   * attributes
   * @return the wsExternalSubjectAttributes
   */
  public WsExternalSubjectAttribute[] getWsExternalSubjectAttributes() {
    return this.wsExternalSubjectAttributes;
  }

  
  /**
   * attributes
   * @param wsExternalSubjectAttributes1 the wsExternalSubjectAttributes to set
   */
  public void setWsExternalSubjectAttributes(WsExternalSubjectAttribute[] wsExternalSubjectAttributes1) {
    this.wsExternalSubjectAttributes = wsExternalSubjectAttributes1;
  }

  /**
   * description, which is generated from other attributes 
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * name of subject   
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * universally unique identifier of this external subject
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * description, which is generated from other attributes 
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * name of subject
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * universally unique identifier of this external subject
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return this.email;
  }

  
  /**
   * @param email1 the email to set
   */
  public void setEmail(String email1) {
    this.email = email1;
  }

  
  /**
   * @return the enabled T or F
   */
  public String getEnabled() {
    return this.enabled;
  }

  
  /**
   * T or F
   * @param enabled1 the enabled to set
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  
  /**
   * @return the identifier
   */
  public String getIdentifier() {
    return this.identifier;
  }

  
  /**
   * @param identifier1 the identifier to set
   */
  public void setIdentifier(String identifier1) {
    this.identifier = identifier1;
  }

  
  /**
   * @return the institution
   */
  public String getInstitution() {
    return this.institution;
  }

  
  /**
   * @param institution1 the institution to set
   */
  public void setInstitution(String institution1) {
    this.institution = institution1;
  }

  
  /**
   * @return the searchStringLower
   */
  public String getSearchStringLower() {
    return this.searchStringLower;
  }

  
  /**
   * @param searchStringLower1 the searchStringLower to set
   */
  public void setSearchStringLower(String searchStringLower1) {
    this.searchStringLower = searchStringLower1;
  }

  
  /**
   * @return the vettedEmailAddresses
   */
  public String getVettedEmailAddresses() {
    return this.vettedEmailAddresses;
  }

  
  /**
   * @param vettedEmailAddresses1 the vettedEmailAddresses to set
   */
  public void setVettedEmailAddresses(String vettedEmailAddresses1) {
    this.vettedEmailAddresses = vettedEmailAddresses1;
  }
}
