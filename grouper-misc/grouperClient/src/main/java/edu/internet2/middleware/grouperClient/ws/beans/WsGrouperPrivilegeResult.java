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
/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * Query for one privilege.
 * 
 * @author mchyzer
 */
public class WsGrouperPrivilegeResult {

  /** whether this privilege is allowed T/F */
  private String allowed;
  
  /** owner subject of privilege */
  private WsSubject ownerSubject;
  
  /** privilege name, e.g. read, update, stem */
  private String privilegeName;
  
  /** privilege type, e.g. naming, or access */
  private String privilegeType;
  
  /** If this privilege is revokable (maybe due to if effective privilege not immediate)  */
  private String revokable;
  
  /** group querying */
  private WsGroup wsGroup;
  
  /** stem querying */
  private WsStem wsStem;
  
  /**
   * subject to switch to
   */
  private WsSubject wsSubject;

  /**
   * whether this privilege is allowed T/F
   * @return if allowed
   */
  public String getAllowed() {
    return this.allowed;
  }

  /**
   * owner subject of privilege
   * @return the owner
   */
  public WsSubject getOwnerSubject() {
    return this.ownerSubject;
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
   * owner subject of privilege
   * @param ownerSubject1
   */
  public void setOwnerSubject(WsSubject ownerSubject1) {
    this.ownerSubject = ownerSubject1;
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

  /**
   * If this privilege is revokable (maybe due to if effective privilege not immediate)
   * @return if revokable
   */
  public String getRevokable() {
    return this.revokable;
  }

  /**
   * If this privilege is revokable (maybe due to if effective privilege not immediate)
   * @param revokable1
   */
  public void setRevokable(String revokable1) {
    this.revokable = revokable1;
  }
  
  
  
}
