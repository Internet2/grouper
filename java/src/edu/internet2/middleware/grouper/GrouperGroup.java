package edu.internet2.middleware.directory.grouper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** 
 * Class representing a {@link Grouper} group.
 *
 * @author blair christensen.
 * @version $Id: GrouperGroup.java,v 1.8 2004-04-28 15:59:22 blair Exp $
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
   * Returns a list of immediate members of type groupField for
   * a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has VIEW and READ access to the
   *      group for group field groupField.</li>
   *  <li>Fetch rows with appropriate groupID, groupField, and
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
   * Returns a list of effective members of type groupField for
   * a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has VIEW and READ access to the
   *      group for group field groupField.</li>
   *  <li>Fetch rows with appropriate groupID, groupField, and
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

  /**
   * Return a map of nonlist member data for a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has READ access to the group
   *      metadata</li>
   *  <li>Fetch rows with appropriate groupID from the 
   *      <i>grouper_metadata</i> table.</li>
   *  <li>XXX This name sucks.</li>
   * </ul>
   *
   * @return Map of group metadata.
   */
  public Map nonListData() {
    return null;
  }

  /**
   * Return a map of nonlist member data for a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has READ access to the group
   *      metadata</li>
   *  <li>Fetch row(s) with appropriate groupID and groupField from
   *      the <i>grouper_metadata</i> table.</li>
   *  <li>XXX This name sucks.</li>
   * </ul>
   *
   * @param  groupField Type of group field to return.
   * @return Metadata value.
   */
  public String nonListData(String groupField) {
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
   *      <i>groupType</i>.</li>
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
   *      as the <i>groupType</i>.</li>
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
   */
  public void addValue(String groupField, GrouperMember member, Date ttl) {
    // Nothing -- Yet
  }

  /**
   * Remove group metadata.
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has sufficient privileges to ADMIN
   *      (?) the group.</li>
   *  <li>Remove fields from the <i>grouper_metadata</i> table with the
   *      appropriate "groupID", "groupField", and "value".</li>
   *  <li>XXX Should value be required?  What if it was optional?
   *      Requiring the value looks like it may be an extra hoop
   *      that we don't need to jump through.</li>
   * </ul>
   * 
   * @param groupField  The metadata field.
   * @param value       The value to remove.
   */
  public void  removeValue(String groupField, String value) {
    // Nothing -- Yet
  }

  /**
   * Remove group member..
   * <p>
   * <ul>
   *  <li>Verify that "subjectID" has sufficient privileges to ADMIN
   *      (?) the group.</li>
   *  <li>Remove fields from the <i>grouper_metadata</i> table with the
   *      appropriate "groupID", "groupField", and "value".</li>
   *  <li>XXX Should value be required?  What if it was optional?
   *      Requiring the value looks like it may be an extra hoop
   *      that we don't need to jump through.</li>
   *  <li>Verify that "subjectID" has sufficient privileges to VIEW and
   *      either UPDATE or OPTIN the group.</li>
   *  <li>Remove "member" from "groupField" for "groupID" in the 
   *      <i>grouper_membership</i> table.</li>
   *  <li>XXX Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables to reflect any changes in effective memberships that 
   *      may have resulted from this change.</li>
   * </ul>
   * 
   * @param groupField  The field type for this member.
   * @param member      The member to add.
   */
  public void  removeValue(String groupField, GrouperMember member) {
    // Nothing -- Yet
  }

}

