package edu.internet2.middleware.directory.grouper;

import java.util.List;

/** 
 * Provides a GrouperMember.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.1 2004-04-02 21:04:11 blair Exp $
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

  public List allowedStems() {
    return null;
  }

  public boolean allowedStems(String stem) {
    /* - This is an interface (or something like that) */
    return false;
  }

  public boolean hasPriv(String groupName, String priv) {
    /* - XXX s,String groupName,GrouperGroup g,? */
    return false;
  }

  public boolean isGroup() {
    /* 
      Returns true if the GrouperMember object is a group. 
      - XXX How does it know if it is a group or not?
    */
    return false;
  }

}

