/**
 * @author mchyzer
 * $Id: PermissionEntry.java,v 1.1 2009-10-02 05:57:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;


/**
 *
 */
@SuppressWarnings("serial")
public class PermissionEntry extends GrouperAPI {
  
  /** role which has the permission or which the subject must be in to have the permission */
  private String roleName;
  
  /** source id of the subject which has the permissions */
  private String subjectSourceId;

  /** subject id of the subject which has the permissions */
  private String subjectId;

  /** action on the perimssion (e.g. read, write, assign (default), etc */
  private String action;

  /** name of the attribute def name which is the permission assigned to the role or subject */
  private String attributeDefNameName;

  /** display name of the attribute def name which is the permission assigned to the role or subject */
  private String attributeDefNameDispName;

  /** display name of the role which the subject is in to have the permission */
  private String roleDisplayName;
  
  /** id of the role which the subject is in to get the permission */
  private String roleId;

  /** id of the attributeDef */
  private String attributeDefId;

  /** id of the member that has the permission */
  private String memberId;

  /** id of the attribute def name which is the permission */
  private String attributeDefNameId;

  
  /**
   * role which has the permission or which the subject must be in to have the permission
   * @return the roleName
   */
  public String getRoleName() {
    return this.roleName;
  }

  
  /**
   * role which has the permission or which the subject must be in to have the permission
   * @param roleName the roleName to set
   */
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  
  /**
   * source id of the subject which has the permissions
   * @return the subjectSourceId
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  
  /**
   * source id of the subject which has the permissions
   * @param subjectSourceId the subjectSourceId to set
   */
  public void setSubjectSourceId(String subjectSourceId) {
    this.subjectSourceId = subjectSourceId;
  }

  
  /**
   * subject id of the subject which has the permissions
   * @return the subjectId
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  
  /**
   * subject id of the subject which has the permissions
   * @param subjectId the subjectId to set
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  
  /**
   * action on the perimssion (e.g. read, write, assign (default), etc
   * @return the action
   */
  public String getAction() {
    return this.action;
  }

  
  /**
   * action on the perimssion (e.g. read, write, assign (default), etc
   * @param action the action to set
   */
  public void setAction(String action) {
    this.action = action;
  }

  
  /**
   * name of the attribute def name which is the permission assigned to the role or subject
   * @return the attributeDefNameName
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  
  /**
   * name of the attribute def name which is the permission assigned to the role or subject
   * @param attributeDefNameName the attributeDefNameName to set
   */
  public void setAttributeDefNameName(String attributeDefNameName) {
    this.attributeDefNameName = attributeDefNameName;
  }

  
  /**
   * display name of the attribute def name which is the permission assigned to the role or subject
   * @return the attributeDefNameDispName
   */
  public String getAttributeDefNameDispName() {
    return this.attributeDefNameDispName;
  }

  
  /**
   * display name of the attribute def name which is the permission assigned to the role or subject
   * @param attributeDefNameDispName the attributeDefNameDispName to set
   */
  public void setAttributeDefNameDispName(String attributeDefNameDispName) {
    this.attributeDefNameDispName = attributeDefNameDispName;
  }

  
  /**
   * display name of the role which the subject is in to have the permission
   * @return the roleDisplayName
   */
  public String getRoleDisplayName() {
    return this.roleDisplayName;
  }

  
  /**
   * display name of the role which the subject is in to have the permission
   * @param roleDisplayName the roleDisplayName to set
   */
  public void setRoleDisplayName(String roleDisplayName) {
    this.roleDisplayName = roleDisplayName;
  }

  
  /**
   * id of the role which the subject is in to get the permission
   * @return the roleId
   */
  public String getRoleId() {
    return this.roleId;
  }

  
  /**
   * id of the role which the subject is in to get the permission
   * @param roleId the roleId to set
   */
  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  
  /**
   * id of the attributeDef
   * @return the attributeDefId
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  
  /**
   * id of the attributeDef
   * @param attributeDefId the attributeDefId to set
   */
  public void setAttributeDefId(String attributeDefId) {
    this.attributeDefId = attributeDefId;
  }

  
  /**
   * id of the member that has the permission
   * @return the memberId
   */
  public String getMemberId() {
    return this.memberId;
  }

  
  /**
   * id of the member that has the permission
   * @param memberId the memberId to set
   */
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  
  /**
   * id of the attribute def name which is the permission
   * @return the attributeDefNameId
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  
  /**
   * id of the attribute def name which is the permission
   * @param attributeDefNameId the attributeDefNameId to set
   */
  public void setAttributeDefNameId(String attributeDefNameId) {
    this.attributeDefNameId = attributeDefNameId;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    throw new RuntimeException("Not implemented");
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "roleName", this.roleName)
      .append( "sourceId", this.subjectSourceId )
      .append( "subjectId", this.subjectId )
      .append( "attributeDefNameName", this.attributeDefNameName )
      .append( "action", this.action )
      .toString();
  }

}
