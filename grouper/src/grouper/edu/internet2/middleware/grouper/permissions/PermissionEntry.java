/**
 * @author mchyzer
 * $Id: PermissionEntry.java,v 1.3 2009-10-26 02:26:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions;

import java.sql.Timestamp;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
@SuppressWarnings("serial")
public class PermissionEntry extends GrouperAPI implements Comparable<PermissionEntry> {

  /** notes on the assignment of privilege */
  private String assignmentNotes;
  
  /**
   * notes on the assignment of privilege
   * @return notes
   */
  public String getAssignmentNotes() {
    return this.assignmentNotes;
  }

  /**
   * notes on the assignment of privilege
   * @param assignmentNotes1
   */
  public void setAssignmentNotes(String assignmentNotes1) {
    this.assignmentNotes = assignmentNotes1;
  }

  /**
   * see if a permission is in the list of entries
   * @param permissionEntries
   * @param roleName
   * @param attributeDefNameName
   * @param action
   * @param subjectSourceId
   * @param subjectId
   * @return true if the item is in the list
   */
  public static boolean collectionContains(Collection<PermissionEntry> permissionEntries, String roleName, 
      String attributeDefNameName, String action, String subjectSourceId, String subjectId) {
    return collectionFindFirst(permissionEntries, roleName, attributeDefNameName, action, subjectSourceId, subjectId, null, false) != null;
  }
    
  /**
   * find the first permission entry in the list of entries
   * @param permissionEntries
   * @param roleName
   * @param attributeDefNameName
   * @param action
   * @param subjectSourceId
   * @param subjectId
   * @param permissionType e.g. role or role_subject
   * @return true if the item is in the list
   */
  public static PermissionEntry collectionFindFirst(Collection<PermissionEntry> permissionEntries, String roleName, 
      String attributeDefNameName, String action, String subjectSourceId, String subjectId, String permissionType) {
    return collectionFindFirst(permissionEntries, roleName, attributeDefNameName, action, subjectSourceId, subjectId, permissionType, true);
  }
  
  /**
   * find the first permission entry in the list of entries
   * @param permissionEntries
   * @param roleName
   * @param attributeDefNameName
   * @param action
   * @param subjectSourceId
   * @param subjectId
   * @param permissionType e.g. role or role_subject
   * @param considerPermissionType 
   * @return true if the item is in the list
   */
  public static PermissionEntry collectionFindFirst(Collection<PermissionEntry> permissionEntries, String roleName, 
      String attributeDefNameName, String action, String subjectSourceId, String subjectId, String permissionType, boolean considerPermissionType) {
    for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {
      if (StringUtils.equals(roleName, permissionEntry.roleName)
          && StringUtils.equals(attributeDefNameName, permissionEntry.attributeDefNameName)
          && StringUtils.equals(action, permissionEntry.action)
          && StringUtils.equals(subjectSourceId, permissionEntry.subjectSourceId)
          && StringUtils.equals(subjectId, permissionEntry.subjectId)
          && (considerPermissionType ? StringUtils.equals(permissionType, permissionEntry.getPermissionTypeDb()) : true)
           ) {
        return permissionEntry;
      }
    }
    return null;
  }
    
  /** role which has the permission or which the subject must be in to have the permission */
  private String roleName;
  
  /** source id of the subject which has the permissions */
  private String subjectSourceId;

  /** subject id of the subject which has the permissions */
  private String subjectId;

  /** action on the perimssion (e.g. read, write, assign (default), etc */
  private String action;

  /**
   * action on the perimssion (e.g. read, write, assign (default), etc
   */
  private String actionId;
  
  /**
   * when the underlying membership was enabled
   */
  private Long immediateMshipEnabledTimeDb;
  
  /**
   * when the underlying membership will be disabled
   */
  private Long immediateMshipDisabledTimeDb;
  
  
  
  /**
   * when the underlying membership was enabled
   * @return time
   */
  public Long getImmediateMshipEnabledTimeDb() {
    return this.immediateMshipEnabledTimeDb;
  }

  /**
   * when the underlying membership was enabled
   * @return time
   */
  public Timestamp getImmediateMshipEnabledTime() {
    return this.immediateMshipEnabledTimeDb == null ? null : new Timestamp(this.immediateMshipEnabledTimeDb);
  }

  /**
   * when the underlying membership was enabled
   * @param immediateMshipEnabledTimeDb1
   */
  public void setImmediateMshipEnabledTimeDb(Long immediateMshipEnabledTimeDb1) {
    this.immediateMshipEnabledTimeDb = immediateMshipEnabledTimeDb1;
  }

  /**
   * when the underlying membership was enabled
   * @param immediateMshipEnabledTimeDb1
   */
  public void setImmediateMshipEnabledTime(Timestamp immediateMshipEnabledTimeDb1) {
    this.immediateMshipEnabledTimeDb = immediateMshipEnabledTimeDb1 == null ? null : immediateMshipEnabledTimeDb1.getTime();
  }

  /**
   * when the underlying membership will be disabled
   * @return time
   */
  public Long getImmediateMshipDisabledTimeDb() {
    return this.immediateMshipDisabledTimeDb;
  }

  /**
   * when the underlying membership was enabled
   * @param immediateMshipDisabledTimeDb1
   */
  public void setImmediateMshipDisabledTimeDb(Long immediateMshipDisabledTimeDb1) {
    this.immediateMshipDisabledTimeDb = immediateMshipDisabledTimeDb1;
  }

  /**
   * when the underlying membership will be disabled
   * @return time
   */
  public Timestamp getImmediateMshipDisabledTime() {
    return this.immediateMshipDisabledTimeDb == null ? null : new Timestamp(this.immediateMshipDisabledTimeDb);
  }

  /**
   * when the underlying membership was enabled
   * @param immediateMshipDisabledTimeDb1
   */
  public void setImmediateMshipDisabledTimeDb(Timestamp immediateMshipDisabledTimeDb1) {
    this.immediateMshipDisabledTimeDb = immediateMshipDisabledTimeDb1 == null ? null : immediateMshipDisabledTimeDb1.getTime();
  }

  /**
   * action on the perimssion (e.g. read, write, assign (default), etc
   * @return action
   */
  public String getActionId() {
    return this.actionId;
  }

  /**
   * action on the perimssion (e.g. read, write, assign (default), etc
   * @param actionId1
   */
  public void setActionId(String actionId1) {
    this.actionId = actionId1;
  }

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

  /** if this assignment is enabled */
  private boolean enabled;
  
  /** the delegatable flag on assignment */
  private AttributeAssignDelegatable attributeAssignDelegatable;
  
  /** the time this assignment was enabled */
  private Long enabledTimeDb;
  
  /** the time this assignment was disabled */
  private Long disabledTimeDb;
  
  /** depth of memberships, 0 means immediate */
  private int membershipDepth = -2;
  
  /** depth of role hierarchy, 0 means immediate, -1 means no role set involved */
  private int roleSetDepth = -2;
  
  /** depth of attributeDefName set hierarchy, 0 means immediate */
  private int attributeDefNameSetDepth = -2;

  /** depth of action hierarchy, 0 means immediate */
  private int attributeAssignActionSetDepth = -2;

  /**
   * depth of memberships, 0 means immediate
   * @return depth
   */
  public int getMembershipDepth() {
    return this.membershipDepth;
  }


  /**
   * depth of memberships, 0 means immediate
   * @param membershipDepth1
   */
  public void setMembershipDepth(int membershipDepth1) {
    this.membershipDepth = membershipDepth1;
  }


  /**
   * depth of role hierarchy, 0 means immediate, -1 means no role set involved
   * @return depth
   */
  public int getRoleSetDepth() {
    return this.roleSetDepth;
  }


  /**
   * depth of role hierarchy, 0 means immediate, -1 means no role set involved
   * @param roleSetDepth1
   */
  public void setRoleSetDepth(int roleSetDepth1) {
    this.roleSetDepth = roleSetDepth1;
  }


  /**
   * depth of attributeDefName set hierarchy, 0 means immediate
   * @return depth
   */
  public int getAttributeDefNameSetDepth() {
    return this.attributeDefNameSetDepth;
  }


  /**
   * depth of attributeDefName set hierarchy, 0 means immediate
   * @param attributeDefNameSetDepth1
   */
  public void setAttributeDefNameSetDepth(int attributeDefNameSetDepth1) {
    this.attributeDefNameSetDepth = attributeDefNameSetDepth1;
  }


  /**
   * depth of action hierarchy, 0 means immediate
   * @return depth
   */
  public int getAttributeAssignActionSetDepth() {
    return this.attributeAssignActionSetDepth;
  }


  /**
   * depth of action hierarchy, 0 means immediate
   * @param attributeAssignActionSetDepth1
   */
  public void setAttributeAssignActionSetDepth(int attributeAssignActionSetDepth1) {
    this.attributeAssignActionSetDepth = attributeAssignActionSetDepth1;
  }


  /**
   * role which has the permission or which the subject must be in to have the permission
   * @return the roleName
   */
  public String getRoleName() {
    return this.roleName;
  }

  
  /**
   * role which has the permission or which the subject must be in to have the permission
   * @param roleName1 the roleName to set
   */
  public void setRoleName(String roleName1) {
    this.roleName = roleName1;
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
   * @param subjectSourceId1 the subjectSourceId to set
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
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
   * @param subjectId1 the subjectId to set
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
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
   * @param action1 the action to set
   */
  public void setAction(String action1) {
    this.action = action1;
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
   * @param attributeDefNameName1 the attributeDefNameName to set
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
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
   * @param attributeDefNameDispName1 the attributeDefNameDispName to set
   */
  public void setAttributeDefNameDispName(String attributeDefNameDispName1) {
    this.attributeDefNameDispName = attributeDefNameDispName1;
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
   * @param roleDisplayName1 the roleDisplayName to set
   */
  public void setRoleDisplayName(String roleDisplayName1) {
    this.roleDisplayName = roleDisplayName1;
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
   * @param roleId1 the roleId to set
   */
  public void setRoleId(String roleId1) {
    this.roleId = roleId1;
  }

  
  /**
   * id of the attributeDef
   * @return the attributeDefId
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * 
   * @return attributeDef
   */
  public AttributeDef getAttributeDef() {
    return this.attributeDefId == null ? null : AttributeDefFinder.findByAttributeDefNameId(this.attributeDefNameId, true);
  }
  
  /**
   * id of the attributeDef
   * @param attributeDefId1 the attributeDefId to set
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  
  /**
   * id of the member that has the permission
   * @return the memberId
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * get the member
   * @return the member
   */
  public Member getMember() {
      
    //I think the current grouper session isnt really relevant here, I think we just need to produce the group without security
    return this.memberId == null ? null : GrouperDAOFactory.getFactory().getMember().findByUuid(this.memberId, true);
  }

  
  /**
   * id of the member that has the permission
   * @param memberId1 the memberId to set
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
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
   * @param attributeDefNameId1 the attributeDefNameId to set
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
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
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "roleName", this.roleName)
      .append( "attributeDefNameName", this.attributeDefNameName )
      .append( "action", this.action )
      .append( "sourceId", this.subjectSourceId )
      .append( "subjectId", this.subjectId )
      .append( "imm_mem", this.isImmediateMembership() )
      .append( "imm_perm", this.isImmediatePermission() )
      .append( "mem_depth", this.membershipDepth )
      .append( "role_depth", this.roleSetDepth )
      .append( "action_depth", this.attributeAssignActionSetDepth )
      .append( "attrDef_depth", this.attributeDefNameSetDepth )
      .append( "perm_type", this.getPermissionTypeDb() )
      .append( "imm_mship_enabled", this.getImmediateMshipEnabledTime() )
      .append( "imm_mship_disabled", this.getImmediateMshipDisabledTime() )
      .toString();
  }


  /**
   * get the enum for delegatable, do not return null
   * @return the attributeAssignDelegatable
   */
  public AttributeAssignDelegatable getAttributeAssignDelegatable() {
    return GrouperUtil.defaultIfNull(this.attributeAssignDelegatable, 
        AttributeAssignDelegatable.FALSE); 
  }


  /**
   * internal method for hibernate to persist this enum
   * @return the string value (enum name)
   */
  public String getAttributeAssignDelegatableDb() {
    return this.getAttributeAssignDelegatable().name();
  }


  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @return the disabledTimeDb
   */
  public Timestamp getDisabledTime() {
    return this.disabledTimeDb == null ? null : new Timestamp(this.disabledTimeDb);
  }


  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @return the disabledTimeDb
   */
  public Long getDisabledTimeDb() {
    return this.disabledTimeDb;
  }


  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @return the enabled
   */
  public String getEnabledDb() {
    return this.enabled ? "T" : "F";
  }


  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @return the enabledTimeDb
   */
  public Timestamp getEnabledTime() {
    return this.enabledTimeDb == null ? null : new Timestamp(this.enabledTimeDb);
  }


  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @return the enabledTimeDb
   */
  public Long getEnabledTimeDb() {
    return this.enabledTimeDb;
  }


  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @return the enabled
   */
  public boolean isEnabled() {
    //currently this is based on timestamp
    long now = System.currentTimeMillis();
    if (this.enabledTimeDb != null && this.enabledTimeDb > now) {
      return false;
    }
    if (this.disabledTimeDb != null && this.disabledTimeDb < now) {
      return false;
    }
    return true;
  }


  /**
   * @param attributeAssignDelegatable1 the attributeAssignDelegatable to set
   */
  public void setAttributeAssignDelegatable(
      AttributeAssignDelegatable attributeAssignDelegatable1) {
    this.attributeAssignDelegatable = attributeAssignDelegatable1;
  }


  /**
   * internal method for hibernate to set if delegatable
   * @param theAttributeAssignDelegatableDb
   */
  public void setAttributeAssignDelegatableDb(String theAttributeAssignDelegatableDb) {
    this.attributeAssignDelegatable = AttributeAssignDelegatable.valueOfIgnoreCase(
        theAttributeAssignDelegatableDb, false);
  }


  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @param disabledTimeDb1 the disabledTimeDb to set
   */
  public void setDisabledTime(Timestamp disabledTimeDb1) {
    this.disabledTimeDb = disabledTimeDb1 == null ? null : disabledTimeDb1.getTime();
  }


  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @param disabledTimeDb1 the disabledTimeDb to set
   */
  public void setDisabledTimeDb(Long disabledTimeDb1) {
    this.disabledTimeDb = disabledTimeDb1;
  }


  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @param enabled1 the enabled to set
   */
  public void setEnabled(boolean enabled1) {
    this.enabled = enabled1;
  }


  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @param enabled1 the enabled to set
   */
  public void setEnabledDb(String enabled1) {
    this.enabled = GrouperUtil.booleanValue(enabled1);
  }


  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @param enabledTimeDb1 the enabledTimeDb to set
   */
  public void setEnabledTime(Timestamp enabledTimeDb1) {
    this.enabledTimeDb = enabledTimeDb1 == null ? null : enabledTimeDb1.getTime();
  }


  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @param enabledTimeDb1 the enabledTimeDb to set
   */
  public void setEnabledTimeDb(Long enabledTimeDb1) {
    this.enabledTimeDb = enabledTimeDb1;
  }


  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null || (!(obj instanceof PermissionEntry))) {
      return false;
    }
    PermissionEntry other = (PermissionEntry)obj;
    return new EqualsBuilder().append(this.roleId, other.roleId)
      .append(this.memberId, other.memberId)
      .append(this.action, other.action)
      .append(this.attributeDefNameId, other.attributeDefNameId)
      .append(this.enabled, other.enabled)
      .append(this.immediateMshipDisabledTimeDb, other.immediateMshipDisabledTimeDb)
      .append(this.immediateMshipEnabledTimeDb, other.immediateMshipEnabledTimeDb)
      .append(this.attributeAssignDelegatable, other.attributeAssignDelegatable)
      .append(this.permissionType, other.permissionType)
      .isEquals();

  }


  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.roleId)
      .append(this.memberId)
      .append(this.action)
      .append(this.attributeDefNameId)
      .append(this.enabled)
      .append(this.attributeAssignDelegatable)
      .append(this.immediateMshipDisabledTimeDb)
      .append(this.immediateMshipEnabledTimeDb)
      .append(this.permissionType)
      .toHashCode();
  }

  /**
   * type of permission, either assigned to role, or assigned to role and user combined
   */
  public static enum PermissionType {

    /** permission assigned to role */
    role {

      @Override
      public AttributeAssignType convertToAttributeAssignType() {
        return AttributeAssignType.group;
      }
    },
    
    /** permission assigned to role and user combined */
    role_subject {

      @Override
      public AttributeAssignType convertToAttributeAssignType() {
        return AttributeAssignType.any_mem;
      }
    };
    
    /**
     * do a case-insensitive matching
     * 
     * @param theString
     * @param exceptionOnNull will not allow null or blank entries
     * @return the enum or null or exception if not found
     */
    public static PermissionType valueOfIgnoreCase(String theString, boolean exceptionOnNull) {
      return GrouperUtil.enumValueOfIgnoreCase(PermissionType.class, 
          theString, exceptionOnNull);

    }

    /**
     * name for javabean
     * @return the attribute assign type
     */
    public String getName() {
      return this.name();
    }
    

    /**
     * convert to attribute assign type
     * @return type
     */
    public abstract AttributeAssignType convertToAttributeAssignType();
    
  }
  
  /** type of permission, either assigned to role, or assigned to role and user combined: role_subject */
  private PermissionType permissionType;

  /**
   * type of permission, either assigned to role, or assigned to role and user combined: role_subject
   * @return type of permission
   */
  public String getPermissionTypeDb() {
    return this.permissionType == null ? null : this.permissionType.name();
  }

  /**
   * type of permission, either assigned to role, or assigned to role and user combined: role_subject
   * @param permissionTypeDb1
   */
  public void setPermissionTypeDb(String permissionTypeDb1) {
    this.permissionType = PermissionType.valueOfIgnoreCase(permissionTypeDb1, false);
  }
  
  /**
   * type of permission, either assigned to role, or assigned to role and user combined: role_subject
   * @return permission type
   */
  public PermissionType getPermissionType() {
    return this.permissionType;
  }
  
  /** id of the membership row */
  private String membershipId;

  /** id of the attribute assign row, either to the role, or to the role member pair */
  private String attributeAssignId;

  /**
   * id of the membership row
   * @return id of the membership row
   */
  public String getMembershipId() {
    return this.membershipId;
  }

  /**
   * see if the membership is unassignable directly
   * @return true if immediate
   */
  public boolean isImmediateMembership() {
    if (this.membershipDepth < 0) {
      throw new RuntimeException("Why is membership depth not initialized??? " + this.membershipDepth );
    }
    return this.membershipDepth == 0;
  }
  
  /**
   * see if the permission is unassignable directly
   * @return true if immediate
   */
  public boolean isImmediatePermission() {
    if (this.attributeAssignActionSetDepth < 0) {
      throw new RuntimeException("Why is action depth not initialized??? " + this.attributeAssignActionSetDepth );
    }
    //role set can be -1, if role subject assignment
    if (this.roleSetDepth < -1) {
      throw new RuntimeException("Why is role depth not initialized??? " + this.membershipDepth );
    }
    if (this.attributeDefNameSetDepth < 0) {
      throw new RuntimeException("Why is attribute def name set depth not initialized??? " + this.attributeDefNameSetDepth );
    }
    return this.attributeAssignActionSetDepth == 0 && (this.roleSetDepth == -1 || this.roleSetDepth == 0) && this.attributeDefNameSetDepth == 0;
  }
  
  /**
   * id of the membership row
   * @param membershipId1
   */
  public void setMembershipId(String membershipId1) {
    this.membershipId = membershipId1;
  }


  /**
   * id of the attribute assign row, either to the role, or to the role member pair
   * @return id
   */
  public String getAttributeAssignId() {
    return this.attributeAssignId;
  }


  /**
   * id of the attribute assign row, either to the role, or to the role member pair
   * @param attributeAssignId1
   */
  public void setAttributeAssignId(String attributeAssignId1) {
    this.attributeAssignId = attributeAssignId1;
  }


  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(PermissionEntry o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (o2 == null) {
      return 1;
    }
    int compare;
    
    compare = GrouperUtil.compare(this.roleName, o2.roleName);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.attributeDefNameName, o2.attributeDefNameName);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.action, o2.action);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.permissionType, o2.permissionType);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.subjectSourceId, o2.subjectSourceId);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.subjectId, o2.subjectId);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.membershipId, o2.membershipId);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.immediateMshipDisabledTimeDb, o2.immediateMshipDisabledTimeDb);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.immediateMshipEnabledTimeDb, o2.immediateMshipEnabledTimeDb);
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(this.attributeAssignId, o2.attributeAssignId);
  }
  
}
