package edu.internet2.middleware.directory.grouper;

import java.util.List;
import java.util.Map;

/** 
 * Provides a GrouperGroup.
 *
 * @author blair christensen.
 * @version $Id: GrouperGroup.java,v 1.1 2004-04-02 21:04:11 blair Exp $
 */
public class GrouperGroup {

  public GrouperGroup(GrouperSession s, String groupName) { 
  }

  public List immediateMembers(int groupFieldID) {
    return null;
  }

  public List immediateMembers(String groupFieldID) {
    return null;
  }

  public List effectiveMembers(int groupFieldID) {
    return null;
  }

  public List effectiveMembers(String groupFieldID) {
    return null;
  }

  public Map nonListData() {
    return null;
  }

  public boolean create() {
    return false;
  }

  public boolean create(String groupType) {
    /*
      - groupType is a String in the global Map table representing
        group types
        - What the hell does that mean?
    */
    return false;
  }

  public boolean rename(String newGroupName) {
    return false;
  }

  public boolean delete() {
    return false;
  }

  public boolean addValue(int groupFieldID, String value) {
    /*
      We want/need to be able to reference the groupFieldID by either
      name or number
      - Updates 'metadata' or 'membership' table  
      - XXX Should we really use a GrouperMember object?
      - XXX Is this polymorphism the way to go?
    */
    return false;
  }

  public boolean addValue(String groupFieldID, String value) {
    return false;
  }

  public boolean addValue(int groupFieldID, GrouperMember member) {
    return false;
  }

  public boolean addValue(String groupFieldID, GrouperMember member) {
    return false;
  }

  public boolean addValue(int groupFieldID, GrouperMember member, int TTL) {
    return false;
  }

  public boolean addValue(String groupFieldID, GrouperMember member, int TTL) {
    return false;
  }

  public boolean  removeValue(int groupFieldID, String value) {
    return false;
  }

  public boolean  removeValue(String groupFieldID, String value) {
    /*
      We want/need to be able to reference the groupFieldID by either
      name or number
    */
    return false;
  }

}

