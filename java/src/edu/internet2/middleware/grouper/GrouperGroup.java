package edu.internet2.middleware.directory.grouper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** 
 * Class representing a {@link Grouper} group.
 *
 * @author blair christensen.
 * @version $Id: GrouperGroup.java,v 1.3 2004-04-14 03:05:42 blair Exp $
 */
public class GrouperGroup {

  /**
   * Create a new object that represents a single {@link Grouper}
   * group. 
   */
  public GrouperGroup(GrouperSession s, String groupName) { 
    // Nothing -- Yet
  }

  /**
   * Returns a list of immediate members of type groupFieldID for
   * a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has VIEW and READ access to the
   *      group for group field groupField.</li>
   *  <li>Fetch rows with appropriate groupID, groupFieldID, and
   *      isImmediate values from the <i>grouper_membership</i>
   *      table.</li>
   * </ul>
   *
   * @param  groupField  Type of group field to return.
   * @return List of group members
   */
  public List immediateMembers(String groupField) {
    return null;
  }

  /**
   * Returns a list of effective members of type groupFieldID for
   * a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has VIEW and READ access to the
   *      group for group field groupField.</li>
   *  <li>Fetch rows with appropriate groupID, groupFieldID, and
   *      isImmediate values from the <i>grouper_membership</i>
   *      table.</li>
   * </ul>
   * 
   * @param  groupField  Type of group field to return.
   * @return List of group members
   */
  public List effectiveMembers(String groupField) {
    return null;
  }

  public Map nonListData() {
    return null;
  }

  /**
   * Create a new {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that the "subjectID" has sufficient privileges to
   *      create the group.</li>
   *  <li>Update the <i>grouper_group</i> table.</li>
   *  <li>Update the <i>grouper_schema</i> table using the default
   *      <i>groupTypeID</i>.</li>
   *  <li>Update the <i>grouper_membership</i> table by making
   *      "subjectID" an "admin".</li>
   *  <li>Update the <i>grouper_metadata</i> table</li>
   * </ul>
   */
  public void create() {
    // Nothing -- Yet
  }

  /**
   * Create a new {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that the "subjectID" has sufficient privileges to
   *      create the group.</li>
   *  <li>Update the <i>grouper_group</i> table.</li>
   *  <li>Update the <i>grouper_schema</i> table using "groupType"
   *      as the <i>groupTypeID</i>.</li>
   *  <li>Update the <i>grouper_membership</i> table by making
   *      "subjectID" an "admin".</li>
   *  <li>Update the <i>grouper_metadata</i> table</li>
   * </ul>
   */
  public void create(String groupType) {
    // Nothing -- Yet
  }

  /**
   * Rename a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that the "subjectID" has sufficient privileges to
   *      rename the group.</li>
   *  <li>Update the <i>grouper_group</i> table.</li>
   *  <li>??? Possibly update the <i>grouper_metadata</i> table.</li>
   * </ul>
   */
  public void rename(String newGroupName) {
    // Nothing -- Yet
  }

  /** 
   * Delete a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that the "subjectID" has sufficient privileges to
   *       delete the group.</li>
   *  <li>Atomically:</li>
   *  <ul>
   *   <li>Update the <i>grouper_metadata</i> table</li>
   *   <li>Update the <i>grouper_group</i> table.</li>
   *   <li>Update the <i>grouper_schema</i> table.</li>
   *   <li>XXX Update the <i>grouper_membership</i> table.</li>
   *   <li>XXX Update the <i>grouper_via</i> table.</li>
   *  </ul>
   * </ul>
   */
  public void delete() {
    // Nothing -- Yet
  }

  /**
   * Add metadata "value" to group field "groupField".
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has sufficient privileges to VIEW and
   *      ADMIN the group.</li>
   *  <li>Update the <i>grouper_metadata</i> table with this
   *      information.</li>
   * </ul>
   *
   * @param groupField  The field type for this member.
   * @param value       The new metadata value.
   */
  public void addValue(String groupField, String value) {
    // Nothing -- Yet
  }

  /**
   * Add member "member" to the group field "groupField".
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has sufficient privileges to VIEW and
   *      either UPDATE or OPTIN the group.</li>
   *  <li>Update the <i>grouper_membership</i> table with this
   *      immediate membership information and the default TTL.</li>
   *  <li>XXX Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables with any new effective memberships that have been
   *      created.</li>
   * </ul>
   *
   * @param groupField  The field type for this member.
   * @param member      The member to add.
   */
  public void addValue(String groupField, GrouperMember member) {
    // Nothing -- Yet
  }

  /**
   * Add member "member" to the group field "groupField".
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has sufficient privileges to VIEW and
   *      either UPDATE or OPTIN the group.</li>
   *  <li>Update the <i>grouper_membership</i> table with this
   *      immediate membership information.</li>
   *  <li>XXX Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables with any new effective memberships that have been
   *      created.</li>
   * </ul>
   *
   * @param groupField  The field type for this member.
   * @param member      The member to add.
   * @param ttl         When the membership expires.
   * @param 
   */
  public void addValue(String groupField, GrouperMember member, Date ttl) {
    // Nothing -- Yet
  }

  public void  removeValue(String groupField, String value) {
    // Nothing -- Yet
  }

  public void  removeValue(String groupField, GrouperMember member) {
    // Nothing -- Yet
  }

}

