package edu.internet2.middleware.grouper;

/** 
 * Class representing a type of {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubjectType.java,v 1.1 2004-08-11 22:33:45 blair Exp $
 */
public class GrouperMemberType {

  private String memberType;
  private String displayName;

  /**
   * Create a {@link GrouperMemberType} object.
   */
  public GrouperMemberType() {
    memberType  = null;
    displayName = null;
  }

  public String toString() {
    return "" + this.getMemberType() + ":" + this.getDisplayName();
  }

  /*
   * Below for Hibernate
   */

  protected String getMemberType() {
    return this.memberType;
  }

  // XXX Do we really want to allow write access? 
  private void setMemberType(String memberType) {
    this.memberType = memberType;
  }

  protected String getDisplayName() {
    return this.displayName;
  }

  // XXX Do we really want to allow write access? 
  private void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}

