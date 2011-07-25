/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITPermissionAllView;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * result of permission entry query represents an assignment in the DB
 */
public class WsPermissionAssign implements Comparable<WsPermissionAssign> {

  /** if retrieving limits, these are the limits */
  private WsPermissionLimit[] limits;
  
  /**
   * if retrieving limits, these are the limits
   * @return the limits
   */
  public WsPermissionLimit[] getLimits() {
    return this.limits;
  }
  
  /**
   * if retrieving limits, these are the limits
   * @param limits1 the limits to set
   */
  public void setLimits(WsPermissionLimit[] limits1) {
    this.limits = limits1;
  }


  /** detail on the permission */
  private WsPermissionAssignDetail detail;

  /**
   * detail on the permission
   * @return detail
   */
  public WsPermissionAssignDetail getDetail() {
    return this.detail;
  }

  /**
   * detail on the permission
   * @param detail1
   */
  public void setDetail(WsPermissionAssignDetail detail1) {
    this.detail = detail1;
  }


  /**
   * name of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  private String action;

  /** 
   * Type of owner, from enum PermissionType, e.g.
   * role or role_subject 
   */
  private String permissionType;

  /** attribute name id in this assignment */
  private String attributeDefNameId;
  
  /** attribute name in this assignment */
  private String attributeDefNameName;

  /** id of attribute def in this assignment */
  private String attributeDefId;
  
  /** name of attribute def in this assignment */
  private String attributeDefName;

  /**
   * id of attribute def in this assignment
   * @return id of attribute def in this assignment
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * id of attribute def in this assignment
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * name of attribute def in this assignment
   * @return name of attribute def in this assignment
   */
  public String getAttributeDefName() {
    return this.attributeDefName;
  }


  /**
   * name of attribute def in this assignment
   * @param attributeDefName1
   */
  public void setAttributeDefName(String attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }


  /**
   * T or F for if this assignment is enabled (e.g. might have expired) 
   */
  private String enabled;

  /** if this is an attribute assign attribute, this is the foreign key */
  private String attributeAssignId;
  
  /** if this is a group attribute, this is the foreign key */
  private String roleId;
  
  /** if this is a group attribute, this is the name of the foreign key */
  private String roleName;
  
  /** if this is a member attribute, this is the subject of the foreign key */
  private String subjectId;
  
  /** if this is a member attribute, this is the source of the foreign key */
  private String sourceId;
  
  /** if this is a membership attribute, this is the foreign key */
  private String membershipId;

  /**
   * compare and sort so results are reproducible for tests
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsPermissionAssign o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (o2 == null) {
      return 1;
    }
    int compare;
    
    compare = GrouperUtil.compare(this.permissionType, o2.permissionType);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getAttributeDefName(), o2.getAttributeDefName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getAttributeDefNameName(), o2.getAttributeDefNameName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.action, o2.action);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getRoleName(), o2.getRoleName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getSourceId(), o2.getSourceId());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getSubjectId(), o2.getSubjectId());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getAttributeAssignId(), o2.getAttributeAssignId());
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(this.membershipId, o2.membershipId);
  }

  /**
   *  name of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   * @return  name of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  public String getAction() {
    return this.action;
  }

  /**
   *  name of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   * @param attributeAssignActionName1
   */
  public void setAction(String attributeAssignActionName1) {
    this.action = attributeAssignActionName1;
  }

  /**
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @return type
   */
  public String getPermissionType() {
    return this.permissionType;
  }

  /**
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @param attributeAssignType1
   */
  public void setPermissionType(String attributeAssignType1) {
    this.permissionType = attributeAssignType1;
  }

  /**
   * attribute name id in this assignment
   * @return attribute name id in this assignment
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  /**
   * attribute name id in this assignment
   * @param attributeDefNameId1
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
  }

  /**
   * attribute name in this assignment
   * @return attribute name in this assignment
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  /**
   * attribute name in this assignment
   * @param attributeDefNameName1
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }

  /**
   * T or F for if this assignment is enabled (e.g. might have expired) 
   * @return T or F
   */
  public String getEnabled() {
    return this.enabled;
  }

  /**
   * T or F for if this assignment is enabled (e.g. might have expired) 
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @return attribute assign id
   */
  public String getAttributeAssignId() {
    return this.attributeAssignId;
  }

  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @param ownerAttributeAssignId1
   */
  public void setAttributeAssignId(String ownerAttributeAssignId1) {
    this.attributeAssignId = ownerAttributeAssignId1;
  }

  /**
   * if this is a group attribute, this is the foreign key
   * @return the owner group id
   */
  public String getRoleId() {
    return this.roleId;
  }

  /**
   * if this is a group attribute, this is the foreign key
   * @param ownerGroupId1
   */
  public void setRoleId(String ownerGroupId1) {
    this.roleId = ownerGroupId1;
  }

  /**
   * if this is a group attribute, this is the name of the foreign key
   * @return owner group name
   */
  public String getRoleName() {
    return this.roleName;
  }

  /**
   * if this is a group attribute, this is the name of the foreign key
   * @param ownerGroupName1
   */
  public void setRoleName(String ownerGroupName1) {
    this.roleName = ownerGroupName1;
  }

  /**
   * if this is a member attribute, this is the subject of the foreign key
   * @return owner subject id
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * if this is a member attribute, this is the subject of the foreign key
   * @param ownerMemberSubjectId1
   */
  public void setSubjectId(String ownerMemberSubjectId1) {
    this.subjectId = ownerMemberSubjectId1;
  }

  /**
   * if this is a member attribute, this is the source of the foreign key
   * @return owner member source id
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * if this is a member attribute, this is the source of the foreign key
   * @param ownerMemberSourceId1
   */
  public void setSourceId(String ownerMemberSourceId1) {
    this.sourceId = ownerMemberSourceId1;
  }

  /**
   * if this is a membership attribute, this is the foreign key
   * @return membership attribute
   */
  public String getMembershipId() {
    return this.membershipId;
  }

  /**
   * if this is a membership attribute, this is the foreign key
   * @param ownerMembershipId1
   */
  public void setMembershipId(String ownerMembershipId1) {
    this.membershipId = ownerMembershipId1;
  }
  
  /**
   * T or F, this will be if this permissions is allowed (not in DB/assignment, but overall).  So if we are
   * considering limits, and the limit is false, then this will be false for a permission where
   * the disallow is set to false
   */
  private String allowedOverall;
  
  /**
   * T or F, this will be if this permissions is allowed (not in DB/assignment, but overall).  So if we are
   * considering limits, and the limit is false, then this will be false for a permission where
   * the disallow is set to false
   * @return true if allowed overall
   */
  public String getAllowedOverall() {
    return this.allowedOverall;
  }
  
  /**
   * T or F, this will be if this permissions is allowed (not in DB/assignment, but overall).  So if we are
   * considering limits, and the limit is false, then this will be false for a permission where
   * the disallow is set to false
   * @param allowedOverall1
   */
  public void setAllowedOverall(String allowedOverall1) {
    this.allowedOverall = allowedOverall1;
  }

  /**
   * T or F, if this is a permission, then if this permission assignment is allowed or not 
   * @param disallowed1
   */
  public void setDisallowed(String disallowed1) {
    this.disallowed = disallowed1;
  }


  /**
   * T or F, if this is a permission, then if this permission assignment is allowed or not 
   */
  private String disallowed;
  
  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @return if disallowed
   */
  public String getDisallowed() {
    return this.disallowed;
  }

  /**
   * convert permission assigns
   * @param permissionEntrySet 
   * @param permissionLimitMap limits for the permission
   * @param includePermissionAssignDetail 
   * @param attributeAssignSet should be the membership, group, and member objects in a row
   * @return the subject results
   */
  public static WsPermissionAssign[] convertPermissionEntries(
      Set<PermissionEntry> permissionEntrySet, Map<PermissionEntry, Set<PermissionLimitBean>> permissionLimitMap,
      boolean includePermissionAssignDetail) {
    int permissionEntrySetLength = GrouperUtil.length(permissionEntrySet);
    if (permissionEntrySetLength == 0) {
      return null;
    }
  
    WsPermissionAssign[] wsAttributeAssignResultArray = new WsPermissionAssign[permissionEntrySetLength];
    int index = 0;
    
    permissionLimitMap = GrouperUtil.nonNull(permissionLimitMap);
    
    for (PermissionEntry permissionEntry : permissionEntrySet) {

      Set<PermissionLimitBean> permissionLimitBeans = permissionLimitMap.get(permissionEntry);
      
      wsAttributeAssignResultArray[index++] = new WsPermissionAssign(permissionEntry, permissionLimitBeans, includePermissionAssignDetail);
      
    }
    return wsAttributeAssignResultArray;
  }

  /**
   * 
   */
  public WsPermissionAssign() {
    //default constructor
  }
  
  /**
   * construct with attribute assign to set internal fields
   * 
   * @param permissionEntry
   * @param permissionLimitBeans are the limits on this permission
   * @param includePermissionAssignDetail if detail should be added
   */
  public WsPermissionAssign(PermissionEntry permissionEntry, 
      Set<PermissionLimitBean> permissionLimitBeans, boolean includePermissionAssignDetail) {
    
    this.action =  permissionEntry.getAction();
    this.attributeAssignId = permissionEntry.getAttributeAssignId();
    
    if (permissionEntry instanceof PITPermissionAllView) {
      PITAttributeDef theAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef()
        .findById(permissionEntry.getAttributeDefId());
      this.attributeDefName = theAttributeDef == null ? null : theAttributeDef.getName();
    } else {
      AttributeDef theAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef()
        .findById(permissionEntry.getAttributeDefId(), true);
      this.attributeDefName = theAttributeDef == null ? null : theAttributeDef.getName();
      this.enabled = permissionEntry.isEnabled() ? "T" : "F";
    }

    this.attributeDefId = permissionEntry.getAttributeDefId();
    this.attributeDefNameId = permissionEntry.getAttributeDefNameId();
    this.attributeDefNameName = permissionEntry.getAttributeDefNameName();
    
    if (includePermissionAssignDetail) {
      this.detail = new WsPermissionAssignDetail(permissionEntry);
    }
    
    this.attributeAssignId = permissionEntry.getAttributeAssignId();
    
    this.membershipId = permissionEntry.getMembershipId();
    this.permissionType = permissionEntry.getPermissionTypeDb();
    this.roleId = permissionEntry.getRoleId();
    this.roleName = permissionEntry.getRoleName();
    this.sourceId = permissionEntry.getSubjectSourceId();
    this.subjectId = permissionEntry.getSubjectId();
    
    if (GrouperUtil.length(permissionLimitBeans) > 0) {
      this.limits = new WsPermissionLimit[GrouperUtil.length(permissionLimitBeans)];
      
      for (PermissionLimitBean permissionLimitBean : GrouperUtil.nonNull(permissionLimitBeans)) {
        
        
        
      }
    }    
  }
}
