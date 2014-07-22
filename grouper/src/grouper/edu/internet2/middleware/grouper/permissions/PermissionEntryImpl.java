/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author mchyzer
 */
package edu.internet2.middleware.grouper.permissions;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
@SuppressWarnings("serial")
public class PermissionEntryImpl extends PermissionEntryBase {

  /** 
   * this will be if this permissions is allowed (not in DB/assignment, but overall).  So if we are
   * considering limits, and the limit is false, then this will be false for a permission where
   * the disallow is set to false
   */
  private boolean allowedOverall = true;

  /**
   * this will be if this permissions is allowed (not in DB/assignment, but overall).  So if we are
   * considering limits, and the limit is false, then this will be false for a permission where
   * the disallow is set to false
   * @return true if allowed overall
   */
  public boolean isAllowedOverall() {
    return this.allowedOverall && !this.isDisallowed() && this.isEnabled();
  }

  /**
   * this will be if this permissions is allowed (not in DB/assignment, but overall).  So if we are
   * considering limits, and the limit is false, then this will be false for a permission where
   * the disallow is set to false
   * @param allowedOverall1
   */
  public void setAllowedOverall(boolean allowedOverall1) {
    this.allowedOverall = allowedOverall1;
  }
  
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

  /** display name of the attribute def name which is the permission assigned to the role or subject */
  private String attributeDefNameDispName;

  /** display name of the role which the subject is in to have the permission */
  private String roleDisplayName;

  /** if this assignment is enabled */
  private boolean enabled;
  
  /** the delegatable flag on assignment */
  private AttributeAssignDelegatable attributeAssignDelegatable;
  
  /** the time this assignment was enabled */
  private Long enabledTimeDb;
  
  /** the time this assignment was disabled */
  private Long disabledTimeDb;

  
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
   * owner role
   * @return the ownerRole
   */
  public Role getRole() {
    
    //I think the current grouper session isnt really relevant here, I think we just need to produce the group without security
    return this.getRoleId() == null ? null : GrouperDAOFactory.getFactory().getGroup().findByUuid(this.getRoleId(), true);

  }

  /**
   * get attribute def name
   * @return attributeDefName
   */
  public AttributeDefName getAttributeDefName() {
    return this.getAttributeDefNameId() == null ? null 
      : GrouperDAOFactory.getFactory().getAttributeDefName().findByUuidOrName(this.getAttributeDefNameId(), null, true);
  }
  
  /**
   * get attribute assign
   * @return attributeAssign
   */
  public AttributeAssign getAttributeAssign() {
    return this.getAttributeAssignId() == null ? null 
      : GrouperDAOFactory.getFactory().getAttributeAssign().findById(this.getAttributeAssignId(), true);
  }

  /**
   * 
   * @return attributeDef
   */
  public AttributeDef getAttributeDef() {
    return this.getAttributeDefId() == null ? null : AttributeDefFinder.findByAttributeDefNameId(this.getAttributeDefNameId(), true);
  }

  /**
   * get the member
   * @return the member
   */
  public Member getMember() {
      
    //I think the current grouper session isnt really relevant here, I think we just need to produce the group without security
    return this.getMemberId() == null ? null : GrouperDAOFactory.getFactory().getMember().findByUuid(this.getMemberId(), true);
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "roleName", this.getRoleName())
      .append( "attributeDefNameName", this.getAttributeDefNameName() )
      .append( "action", this.getAction() )
      .append( "sourceId", this.getSubjectSourceId() )
      .append( "subjectId", this.getSubjectId() )
      .append( "imm_mem", this.isImmediateMembership() )
      .append( "imm_perm", this.isImmediatePermission() )
      .append( "mem_depth", this.getMembershipDepth() )
      .append( "role_depth", this.getRoleSetDepth() )
      .append( "action_depth", this.getAttributeAssignActionSetDepth() )
      .append( "attrDef_depth", this.getAttributeDefNameSetDepth() )
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
    if (obj == null || (!(obj instanceof PermissionEntryImpl))) {
      return false;
    }
    PermissionEntryImpl other = (PermissionEntryImpl)obj;
    return new EqualsBuilder().append(this.getRoleId(), other.getRoleId())
      .append(this.getMemberId(), other.getMemberId())
      .append(this.getAction(), other.getAction())
      .append(this.getAttributeDefNameId(), other.getAttributeDefNameId())
      .append(this.enabled, other.enabled)
      .append(this.immediateMshipDisabledTimeDb, other.immediateMshipDisabledTimeDb)
      .append(this.immediateMshipEnabledTimeDb, other.immediateMshipEnabledTimeDb)
      .append(this.attributeAssignDelegatable, other.attributeAssignDelegatable)
      .append(this.getPermissionType(), other.getPermissionType())
      .append(this.getAttributeAssignId(), other.getAttributeAssignId())
      .isEquals();

  }


  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getRoleId())
      .append(this.getMemberId())
      .append(this.getAction())
      .append(this.getAttributeDefNameId())
      .append(this.enabled)
      .append(this.attributeAssignDelegatable)
      .append(this.immediateMshipDisabledTimeDb)
      .append(this.immediateMshipEnabledTimeDb)
      .append(this.getPermissionType())
      .append(this.getAttributeAssignId())
      .toHashCode();
  }

  /**
   * see if the membership is unassignable directly
   * @return true if immediate
   */
  public boolean isImmediateMembership() {
    if (this.getMembershipDepth() < 0) {
      throw new RuntimeException("Why is membership depth not initialized??? " + this.getMembershipDepth() );
    }
    
    return this.getMembershipDepth() == 0;
  }
  
  /**
   * see if the membership is unassignable directly
   * @return true if immediate
   */
  public boolean isAssignedToSubject() {
    return true;
//Note: the membership returned is the same member id as permission one,
//we need to change the view to do a better check...
//    String memberId = this.getMemberId();
//    String membershipId = this.getMembershipId();
//    //get this cached
//    Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(membershipId, true, false);
//    return StringUtils.equals(memberId, membership.getMemberUuid());
  }
  
  /**
   * see if the permission is unassignable directly
   * @return true if immediate
   */
  public boolean isImmediatePermission() {
    if (this.getAttributeAssignActionSetDepth() < 0) {
      throw new RuntimeException("Why is action depth not initialized??? " + this.getAttributeAssignActionSetDepth() );
    }
    //role set can be -1, if role subject assignment
    if (this.getRoleSetDepth() < -1) {
      throw new RuntimeException("Why is role depth not initialized??? " + this.getRoleSetDepth() );
    }
    if (this.getAttributeDefNameSetDepth() < 0) {
      throw new RuntimeException("Why is attribute def name set depth not initialized??? " + this.getAttributeDefNameSetDepth() );
    }
    return this.getAttributeAssignActionSetDepth() == 0 && (this.getRoleSetDepth() == -1 || this.getRoleSetDepth() == 0) && this.getAttributeDefNameSetDepth() == 0;
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
    
    compare = GrouperUtil.compare(this.getRoleName(), o2.getRoleName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getAttributeDefNameName(), o2.getAttributeDefNameName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getAction(), o2.getAction());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getPermissionType(), o2.getPermissionType());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getSubjectSourceId(), o2.getSubjectSourceId());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getSubjectId(), o2.getSubjectId());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getMembershipId(), o2.getMembershipId());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.immediateMshipDisabledTimeDb, o2.getImmediateMshipDisabledTimeDb());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.immediateMshipEnabledTimeDb, o2.getImmediateMshipEnabledTimeDb());
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(this.getAttributeAssignId(), o2.getAttributeAssignId());
  }
  
  /**
   * 
   * @param thePermissionType
   * @return if immediate, considering which permission type we are looking at
   */
  public boolean isImmediate(PermissionType thePermissionType) {
    
    //if inherited from another permission type
    if (this.getPermissionType() != thePermissionType) {
      return false;
    }
    
    if (thePermissionType == PermissionType.role) {
      return this.isImmediatePermission();
    }
    
    if (thePermissionType != PermissionType.role_subject) {
      throw new RuntimeException("Not expecting permissionType: " + thePermissionType);
    }
    
    return this.isAssignedToSubject() && this.isImmediatePermission();
    
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.PermissionEntry#isActive()
   */
  public boolean isActive() {
    return true;
  }
}
