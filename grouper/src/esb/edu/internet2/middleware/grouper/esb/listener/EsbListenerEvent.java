/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

/**
 * 
 * Simple class to hold incoming data sent by an ESB. Currently only direct
 * membership add and delete events are supported
 *
 */
public class EsbListenerEvent {

  private String subjectId;

  private String[] addMembershipGroupNames;

  private String[] addMembershipGroupIds;

  private String[] addMembershipGroupExtensions;

  private String[] removeMembershipGroupNames;

  private String[] removeMembershipGroupIds;

  private String[] removeMembershipGroupExtensions;

  /**
   * 
   * @return subjectId
   */
  public String getSubjectId() {
    return subjectId;
  }

  /**
   * 
   * @param subjectId for which memberships are being changed
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * 
   * @return addMembershipGroupNames
   */
  public String[] getAddMembershipGroupNames() {
    return addMembershipGroupNames;
  }

  /**
   * 
   * @param array of group names to which subject should be added as member
   */
  public void setAddMembershipGroupNames(String[] addMembershipGroupNames) {
    this.addMembershipGroupNames = addMembershipGroupNames;
  }

  /**
   * 
   * @return addMembershipGroupIds
   */
  public String[] getAddMembershipGroupIds() {
    return addMembershipGroupIds;
  }

  /**
   * 
   * @param array of group Uuids to which subject should be added as member
   */
  public void setAddMembershipGroupIds(String[] addMembershipGroupIds) {
    this.addMembershipGroupIds = addMembershipGroupIds;
  }

  /**
   * 
   * @return addMembershipGroupExtensions
   */
  public String[] getAddMembershipGroupExtensions() {
    return addMembershipGroupExtensions;
  }

  /**
   * 
   * @param array of group Extensions to which subject should be added as member
   */
  public void setAddMembershipGroupExtensions(
      String[] addMembershipGroupExtensions) {
    this.addMembershipGroupExtensions = addMembershipGroupExtensions;
  }

  /**
   * 
   * @return removeMembershipGroupNames
   */
  public String[] getRemoveMembershipGroupNames() {
    return removeMembershipGroupNames;
  }

  /**
   * 
   * @param array of group names from which subject should be deleted as member
   */
  public void setRemoveMembershipGroupNames(String[] removeMembershipGroupNames) {
    this.removeMembershipGroupNames = removeMembershipGroupNames;
  }

  /**
   * 
   * @return removeMembershipGroupIds
   */
  public String[] getRemoveMembershipGroupIds() {
    return removeMembershipGroupIds;
  }

  /**
   * 
   * @param array of group Uuids from which subject should be deleted as member
   */
  public void setRemoveMembershipGroupIds(String[] removeMembershipGroupIds) {
    this.removeMembershipGroupIds = removeMembershipGroupIds;
  }

  /**
   * 
   * @return removeMembershipGroupExtensions
   */
  public String[] getRemoveMembershipGroupExtensions() {
    return removeMembershipGroupExtensions;
  }

  /**
   * 
   * @param rray of group Extensions from which subject should be deleted as member
   */
  public void setRemoveMembershipGroupExtensions(
      String[] removeMembershipGroupExtensions) {
    this.removeMembershipGroupExtensions = removeMembershipGroupExtensions;
  }
}
