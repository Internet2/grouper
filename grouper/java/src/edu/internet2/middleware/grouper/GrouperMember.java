package edu.internet2.middleware.directory.grouper;

import java.util.List;

/** 
 * Provides a GrouperMember.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.3 2004-04-11 03:13:44 blair Exp $
 */
public class GrouperMember {

  public GrouperMember(GrouperSession s, String member) { 
    /*
      - XXX Calls GrouperSession.lookupSubject(member, isMember)
        internally?
      - member == (memberID|presentationID)
      - XXX We need to determine whether it is a member or a group
    */
  }

  public List immediateMemberships(int groupFieldID) {
    return null;
  }

  public List immediateMemberships(String groupFieldID) {
    return null;
  }

  public List effectiveMemberships(int groupFieldID) {
    return null;
  }

  public List effectiveMemberships(String groupFieldID) {
    return null;
  }

  public boolean isGroup() {
    /* 
      Returns true if the GrouperMember object is a group. 
      - XXX How does it know if it is a group or not?
    */
    return false;
  }

}

