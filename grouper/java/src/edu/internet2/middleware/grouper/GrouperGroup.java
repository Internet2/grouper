package edu.internet2.middleware.directory.grouper;

import  java.sql.*;
import  java.util.ArrayList;
import  java.util.Date;
import  java.util.HashMap;
import  java.util.List;
import  java.util.Map;

/** 
 * Class representing a {@link Grouper} group.
 *
 * @author  blair christensen.
 * @version $Id: GrouperGroup.java,v 1.21 2004-05-28 14:55:02 blair Exp $
 */
public class GrouperGroup {

  private GrouperSession  intSess   = null;
  private String          groupID   = null;
  private String          groupName = null;
  private boolean         exists    = false;

  /**
   * Create a new object that represents a single {@link Grouper}
   * group. 
   * <p>
   * <ul>
   *  <li>Caches the group name.</li>
   *  <li>Checks and caches whether group exists.</li>
   *  <li>If group exists, the privileges of the current subject   
   *      on this group will be cached.</li>
   * </ul>
   * 
   * @param   s         Session context.
   * @param   groupName Name of group.
   */
  public GrouperGroup(GrouperSession s, String groupName) { 
    // Internal reference to the session we are using.
    this.intSess    = s;
    // XXX Hrm...
    this.groupName  = groupName;
    // XXX Also dubious
    this.exists     = true;  
  }

  /**
   * Retrieves all memberships of type "members".
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to view desired information.</li>
   *  <li>Fetch rows from the <i>grouper_membership</i> table
   *      that represent "members" and have the appropriate
   *      <i>groupID</i> value.</li>
   * </ul>
   *
   * @return  List of group memberships.
   */
  public List getMembership() {
    List membership = new ArrayList();

    // XXX Isn't this simplistic!
    // XXX And wrong!
    try { 
      Statement stmt  = intSess.connection().createStatement();
      String    query = "SELECT * FROM grouper_membership WHERE " +
                        "groupID=1"; // AND groupField='members'";
      ResultSet rs    = stmt.executeQuery(query);
      while (rs.next()) {
        String memberRef = rs.getString("memberID");
        GrouperMember m = new GrouperMember(this.intSess, rs.getString("memberID"), false);
        membership.add( (Object) m);
      }
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }

    return membership;
  }

  /**
   * Retrieves all memberships of a specified type.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to view desired information.</li>
   *  <li>Fetch rows from the <i>grouper_membership</i> table
   *      that have the appropriate <i>groupID</i> and 
   *      <i>groupField</i> values.</li>
   * </ul>
   *
   * @param   groupField  Type of group to return.
   * @return  List of group memberships.
   */
  public List getMembership(String groupField) {
    List membership = new ArrayList();
    return membership;
  }

  /**
   * Retrieves all memberships of a specified type and immediacy.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to view desired information..</li>
   *  <li>Fetch rows from the <i>grouper_membership</i> table
   *      that have the appropriate <i>groupID</i>, 
   *      <i>groupField</i>, and <i>isImmediate</i> values.</li>
   * </ul>
   *
   * @param   groupField  Type of group to return.
   * @param   isImmediate Return only immediate or non-immediate
   *          memberships.
   * @return  List of group memberships..
   */
  public List getMembership(String groupField, boolean isImmediate) {
    List membership = new ArrayList();
    return membership;
  }

  /**
   * Retrieves all metadata.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      view desired information.</li>
   *  <li>Fetch rows from the <i>grouper_metadata</i> table that have
   *      the appropriate <i>groupID</i> value.
   * </ul>
   *
   * @return  Map of all accessible group metadata.
  */
  public Map getMetadata() {
    Map metadata = new HashMap();
    return metadata;
  }

  /**
   * Retrieves a single item of metadata.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      view the desired informaton.</li>
   *  <li>Fetch row from the <i>grouper_metadata</i> table that has the 
   *      appropriate <i>groupID</i> and <i>groupField</i> values.</li>
   * </ul>
   *
   * @param   groupField Desired metadata for this {@link GrouperGroup}.
   * @return  Metadata value.
   */
  public String getMetadata(String groupField) {
    return null;
  }

  /**
   * Create a new group of type "base".
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      create the desired group.</li>
   *  <li>If yes:</li>
   *  <ul>
   *   <li>Update the <i>grouper_group</i> table.</li>
   *   <li>Update the <i>grouper_schema</i> table.</li>
   *   <li>Update the <i>grouper_metadata</i> table.</li>
   *   <li>Grant the current subject "admin" privileges on the new
   *       group.</li>
   *   <li>Update the internal <i>exists</i> flag.</li>
   *  </ul>
   * </ul>
   */
  public void create() {
    // Nothing -- Yet
  }

  /**
   * Create a new group of type <i>groupType</i>.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      create the desired group.</li>
   *  <li>If yes:</li>
   *  <ul>
   *   <li>Update the <i>grouper_group</i> table.</li>
   *   <li>Update the <i>grouper_schema</i> table.</li>
   *   <li>Update the <i>grouper_metadata</i> table.</li>
   *   <li>Grant the current subject "admin" privileges on the new
   *       group.</li>
   *   <li>Update the internal <i>exists</i> flag.</li>
   *  </ul>
   * </ul>
   */
  public void create(String groupType) {
    // Nothing -- Yet
  }

  /**
   * Rename a group.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      rename the group as the desired stem and descriptor.</li>
   *  <li>Update the <i>grouper_metadata</i> table.</li>
   * </ul>
   */
  public void rename(String newGroupName) {
    // Nothing -- Yet
  }

  /** 
   * Delete a group.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      delete the group.</li>
   *  <li>Update the <i>grouper_group</i> table.</li>
   *  <li>Update the <i>grouper_schema</i> table.</li>
   *  <li>Update the <i>grouper_metadata</i> table.</li>
   *  <li>Update the <i>grouper_membership</i> table.</li>
   *  <li>Update the <i>grouper_via</i> table.</li>
   *  <li>Update the internal <i>exists</i> flag.</li>
   * </ul>
   */
  public void delete() {
    // Nothing -- Yet
  }

  /**
   * Add metadata to a group.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      update the specified metadata.</li>
   *  <li>Update the <i>grouper_metadata</i> table.</li>
   * </ul>
   *
   * @param   groupField  The field type for this member.
   * @param   value       The new metadata value.
   */
  public void addValue(String groupField, String value) {
    // Nothing -- Yet
  }

  /**
   * Add new "member" membership.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      add a new membership of this type.</li>
   *  <li>Update the <i>grouper_membership</i> table to with this new
   *      membership.</li>
   *  <li>Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables as appropriate to reflect any new effective
   *      memberships.</li>
   * </ul>
   *
   * @param   member      The member to add.
   */
  public void addValue(GrouperMember member) {
    // Nothing -- Yet
  }

  /**
   * Add new membership of type <i>groupField</i>.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      add a new membership of this type.</li>
   *  <li>Update the <i>grouper_membership</i> table to with this new
   *      membership.</li>
   *  <li>Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables as appropriate to reflect any new effective
   *      memberships.</li>
   * </ul>
   *
   * @param   groupField  The field type for this member.
   * @param   member      The member to add.
   */
  public void addValue(String groupField, GrouperMember member) {
    // Nothing -- Yet
  }

  /**
   * Add new membership of type <i>groupField</i> with non-default TTL.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      add a new membership of this type.</li>
   *  <li>Update the <i>grouper_membership</i> table to with this new
   *      membership.</li>
   *  <li>Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables as appropriate to reflect any new effective
   *      memberships.</li>
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
   *  <li>Verify that the current subject has sufficient privileges to
   *      remove the specified metadata.</li>
   *  <li>Update <i>grouper_metadata</i> table.</li>
   * </ul>
   * 
   * @param   groupField  The metadata field.
   */
  public void  removeValue(String groupField) {
    // Nothing -- Yet
  }

  /**
   * Remove specific group metadata.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to
   *      remove the specified metadata.</li>
   *  <li>Update <i>grouper_metadata</i> table.</li>
   * </ul>
   * 
   * @param   groupField  The metadata field.
   * @param   groupValue  The value to remove.
   */
  public void  removeValue(String groupField, String groupValue) {
    // Nothing -- Yet
  }

  /**
   * Remove existing "member" membership.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to 
   *      remove the specified membership.</li>
   *  <li>Update <i>grouper_membership</i> table to reflect the loss of
   *      this membership.</li>
   *  <li>Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables as appropriate to reflect any loss of effective
   *      memberships.</li>
   * </ul>
   *
   * @param   member    The membership to remove.
   */
  public void removeValue(GrouperMember member) {
    // Nothing -- Yet
  }

  /**
   * Remove existing membership of type <i>groupField</i>.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privileges to 
   *      remove the specified membership.</li>
   *  <li>Update <i>grouper_membership</i> table to reflect the loss of
   *      this membership.</li>
   *  <li>Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables as appropriate to reflect any loss of effective
   *      memberships.</li>
   * </ul>
   *
   * @param   member      The membership to remove.
   * @param   groupField  Type of membership to remove.
   */
  public void removeValue(GrouperMember member, String groupField) {
    // Nothing -- Yet
  }

  /**
   * Identify this group object.
   *
   * @return  Group name.
   */
  public String whoAmI() {
    if (this.groupName == null) {
      // XXX Query <i>grouper_metadata</i> table, fetch <i>name</i>,
      //     and cache it in <i>this.groupName</i>.
    }
    return this.groupName;
  }

}

