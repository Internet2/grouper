package edu.internet2.middleware.directory.grouper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** 
 * Class representing a {@link Grouper} group.
 *
 * @author blair christensen.
 * @version $Id: GrouperGroup.java,v 1.14 2004-04-30 17:11:05 blair Exp $
 */
public class GrouperGroup {

  /*
   * TODO
   * - GrouperFieldPrivileges
   *  - field level access
   *    - two caches: one for ui, one for api?
   */

  /**
   * Create a new object that represents a single {@link Grouper}
   * group. 
   * <p>
   * <ul>
   *  <li>Cache the <i>subject</i>'s privileges for this group.</li>
   *  <li>XXX Leave the rest for lazy evaluation later?</li>
   * </ul>
   */
  public GrouperGroup(GrouperSession s, String groupName) { 
    // Nothing -- Yet
  }

  /**
   * Returns a list of all {@link GrouperGroup} memberships of type
   * "members".
   * <p>
   * <ul>
   *  <li>Verify that the session's subject has sufficient privileges
   *      to read type "members" for this group.</li>
   *  <li>Fetch and return rows from the <i>grouper_membership</i>
   *      table that represent "members" and have the appropriate
   *      <i>groupId</i> value.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @return  List of group memberships.
   */
  public List getMembership() {
    return null;
  }

  /**
   * Returns a list of all {@link GrouperGroup} memberships of type
   * <i>groupField</li>.
   * <p>
   * <ul>
   *  <li>Verify that the session's subject has sufficient privileges
   *      to read type <i>groupField</i> for this group.</li>
   *  <li>Fetch and return rows from the <i>grouper_membership</i>
   *      table with the appropriate <i>groupID</i> and <i>groupField</i>
   *      values.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @param   groupField  Type of group to return.
   * @return  List of group memberships.
   */
  public List getMembership(String groupField) {
    return null;
  }

  /**
   * Returns a list of all {@link GrouperGroup} memberships of type
   * <i>groupField</li>.
   * <p>
   * <ul>
   *  <li>Verify that the session's subject has sufficient privileges
   *      to read type <i>groupField</i> for this group.</li>
   *  <li>Fetch and return rows from the <i>grouper_membership</i>
   *      table with the appropriate <i>groupID</i>, <i>groupField</i>,
   *      and <i>isImmediate</i> values.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @param   groupField  Type of group to return.
   * @param   isImmediate Return only immediate or non-immediate
   *          memberships.
   * @return  List of group memberships..
   */
  public List getMembership(String groupField, boolean isImmediate) {
    return null;
  }

  /*
   * Returns a map of all the metadata for a {@link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>Fetch and return rows from the <i>grouper_metadata</i>
   *      table that this session's subject has sufficient privileges
   *      to read.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @return  Map of all accessible group metadata.
  */
  public Map getMetadata() {
    return null;
  }

  /*
   * Returns a single item of metadata for a {link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>Verify that this session's subject has sufficient privileges
   *      to read the desired item of metadata.</li>
   *  <li>Fetch and return the desired metadata from the
   *      <i>grouper_metadata</i> table.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @param   groupField Desired metadata for this {@link GrouperGroup}.
   * @return  Metadata value.
   */
  public String getMetadata(String groupField) {
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
   *  <li>XXX If group was automatically created, admin list is
	 *      empty.  If manually created, the creator is the admin.
	 *       But how do we know if the list was created automatically
	 *      or manually?</li>
   *  <li>XXX Granting of "admin" after creating may fail.  So be
   *      it.</li>
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
   * @param   groupField  The field type for this member.
   * @param   value       The new metadata value.
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
   * @param   groupField  The field type for this member.
   * @param   member      The member to add.
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
   * @param   groupField  The field type for this member.
   * @param   member      The member to add.
   * @param   ttl         When the membership expires.
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
   * @param   groupField  The metadata field.
   * @param   value       The value to remove.
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
   * @param   groupField  The field type for this member.
   * @param   member      The member to add.
   */
  public void  removeValue(String groupField, GrouperMember member) {
    // Nothing -- Yet
  }

}

