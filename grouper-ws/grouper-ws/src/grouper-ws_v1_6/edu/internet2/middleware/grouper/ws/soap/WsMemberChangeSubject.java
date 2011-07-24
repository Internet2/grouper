/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

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
