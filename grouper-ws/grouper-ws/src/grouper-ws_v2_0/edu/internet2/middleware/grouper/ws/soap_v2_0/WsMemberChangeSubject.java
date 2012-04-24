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
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * <pre>
 * Class with data about a member who's subject needs to change
 * 
 * </pre>
 * @author mchyzer
 */
public class WsMemberChangeSubject {

  /**
   * subject of the member which is going to change
   */
  private WsSubjectLookup oldSubjectLookup = null;
  
  /**
   * subject which should be the new subject of the member
   */
  private WsSubjectLookup newSubjectLookup = null;
  
  /**
   * if the old member should be removed (only an issue if the new subject is already a member,
   * this defaults to T).  Should be either T or F
   */
  private String deleteOldMember;

  /**
   * subject of the member which is going to change
   * @return old subject
   */
  public WsSubjectLookup getOldSubjectLookup() {
    return this.oldSubjectLookup;
  }

  /**
   * subject of the member which is going to change
   * @param oldSubjectLookup1
   */
  public void setOldSubjectLookup(WsSubjectLookup oldSubjectLookup1) {
    this.oldSubjectLookup = oldSubjectLookup1;
  }

  /**
   * subject which should be the new subject of the member
   * @return new subject
   */
  public WsSubjectLookup getNewSubjectLookup() {
    return this.newSubjectLookup;
  }

  
  /**
   * if the old member should be removed (only an issue if the new subject is already a member,
   * this defaults to T).  Should be either T or F
   * @return old member
   */
  public String getDeleteOldMember() {
    return this.deleteOldMember;
  }


  /**
   * if the old member should be removed (only an issue if the new subject is already a member,
   * this defaults to T).  Should be either T or F
   * @param deleteOldMember1
   */
  public void setDeleteOldMember(String deleteOldMember1) {
    this.deleteOldMember = deleteOldMember1;
  }


  /**
   * subject which should be the new subject of the member
   * @param newSubjectLookup1
   */
  public void setNewSubjectLookup(WsSubjectLookup newSubjectLookup1) {
    this.newSubjectLookup = newSubjectLookup1;
  }

  /**
   * convert the delete old member to a boolean
   * @return the boolean
   */
  public boolean retrieveDeleteOldMemberBoolean() {
    return GrouperServiceUtils.booleanValue(
        this.deleteOldMember, true, "deleteOldMember");
  }
  
}
