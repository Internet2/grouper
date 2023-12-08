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
 * $Id: AttributeAssignType.java,v 1.1 2009-10-10 18:02:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang3.StringUtils;


/**
 * enum for assignment type
 */
public enum AttributeAssignType {
  
  /** attribute assigned to group */
  group {

    /**
     * @see AttributeAssignType#getAssignmentOnAssignmentType()
     */
    @Override
    public AttributeAssignType getAssignmentOnAssignmentType() {
      return group_asgn;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_DELETE.getAuditType().getId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupId", ((Group)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupName", ((Group)owner).getName());
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_ADD.getAuditType().getId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupId", ((Group)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupName", ((Group)owner).getName());
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_UPDATE.getAuditType().getId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupId", ((Group)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupName", ((Group)owner).getName());
      
    }
  },

  /** attribute assigned to member */
  member {

    /**
     * @see AttributeAssignType#getAssignmentOnAssignmentType()
     */
    @Override
    public AttributeAssignType getAssignmentOnAssignmentType() {
      return mem_asgn;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_DELETE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMemberId", ((Member)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerSourceId", ((Member)owner).getSubjectSourceId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerSubjectId", ((Member)owner).getSubjectId());
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_ADD.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMemberId", ((Member)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerSourceId", ((Member)owner).getSubjectSourceId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerSubjectId", ((Member)owner).getSubjectId());
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_UPDATE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMemberId", ((Member)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerSourceId", ((Member)owner).getSubjectSourceId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerSubjectId", ((Member)owner).getSubjectId());
    }
  },
  
  /** attribute assigned to stem */
  stem {

    /**
     * @see AttributeAssignType#getAssignmentOnAssignmentType()
     */
    @Override
    public AttributeAssignType getAssignmentOnAssignmentType() {
      return stem_asgn;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_STEM_DELETE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerStemId", ((Stem)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerStemName", ((Stem)owner).getName());
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_STEM_ADD.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerStemId", ((Stem)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerStemName", ((Stem)owner).getName());
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_STEM_UPDATE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerStemId", ((Stem)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerStemName", ((Stem)owner).getName());
      
    }
  },

  /** attribute assigned to effective membership */
  any_mem {

    /**
     * @see AttributeAssignType#getAssignmentOnAssignmentType()
     */
    @Override
    public AttributeAssignType getAssignmentOnAssignmentType() {
      return any_mem_asgn;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ANYMSHIP_DELETE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupId", ((GroupMember)owner).getGroup().getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupName", ((GroupMember)owner).getGroup().getName());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMemberId", ((GroupMember)owner).getMember().getUuid());
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ANYMSHIP_ADD.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupId", ((GroupMember)owner).getGroup().getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupName", ((GroupMember)owner).getGroup().getName());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMemberId", ((GroupMember)owner).getMember().getUuid());
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ANYMSHIP_UPDATE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupId", ((GroupMember)owner).getGroup().getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerGroupName", ((GroupMember)owner).getGroup().getName());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMemberId", ((GroupMember)owner).getMember().getUuid());
      
    }
  },
  
  /** attribute assigned to immediate membership */
  imm_mem {

    /**
     * @see AttributeAssignType#getAssignmentOnAssignmentType()
     */
    @Override
    public AttributeAssignType getAssignmentOnAssignmentType() {
      return imm_mem_asgn;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_IMMMSHIP_DELETE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMembershipId", ((Membership)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerOwnerId", ((Membership)owner).getOwnerId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMemberId", ((Membership)owner).getMemberUuid());
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_IMMMSHIP_ADD.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMembershipId", ((Membership)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerOwnerId", ((Membership)owner).getOwnerId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMemberId", ((Membership)owner).getMemberUuid());
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_IMMMSHIP_UPDATE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMembershipId", ((Membership)owner).getUuid());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerOwnerId", ((Membership)owner).getOwnerId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerMemberId", ((Membership)owner).getMemberUuid());
      
    }
  },
  
  /** attribute assigned to attribute def */
  attr_def {

    /**
     * @see AttributeAssignType#getAssignmentOnAssignmentType()
     */
    @Override
    public AttributeAssignType getAssignmentOnAssignmentType() {
      return attr_def_asgn;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ATTRDEF_DELETE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerAttributeDefId", ((AttributeDef)owner).getId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerAttributeDefName", ((AttributeDef)owner).getName());

    }
    
    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ATTRDEF_ADD.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerAttributeDefId", ((AttributeDef)owner).getId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerAttributeDefName", ((AttributeDef)owner).getName());
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ATTRDEF_UPDATE.getAuditType().getId());

      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerAttributeDefId", ((AttributeDef)owner).getId());
      auditEntry.assignStringValue(auditEntry.getAuditType(), 
          "ownerAttributeDefName", ((AttributeDef)owner).getName());
      
    }
  },
  
  /** attribute assigned to group assignment */
  group_asgn {

    /**
     * @see AttributeAssignType#isAssignmentOnAssignment()
     */
    @Override
    public boolean isAssignmentOnAssignment() {
      return true;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignDelete(auditEntry, (AttributeAssign)owner);
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignInsert(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignUpdate(auditEntry, (AttributeAssign)owner);
      
    }
  },
  
  /** attribute assigned to member assignment */
  mem_asgn {

    /**
     * @see AttributeAssignType#isAssignmentOnAssignment()
     */
    @Override
    public boolean isAssignmentOnAssignment() {
      return true;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignDelete(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignInsert(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignUpdate(auditEntry, (AttributeAssign)owner);
      
    }
  },
  
  /** attribute assigned to stem assignment */
  stem_asgn {

    /**
     * @see AttributeAssignType#isAssignmentOnAssignment()
     */
    @Override
    public boolean isAssignmentOnAssignment() {
      return true;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignDelete(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignInsert(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignUpdate(auditEntry, (AttributeAssign)owner);
      
    }
  },
  
  /** attribute assigned to effective membership assignment */
  any_mem_asgn {

    /**
     * @see AttributeAssignType#isAssignmentOnAssignment()
     */
    @Override
    public boolean isAssignmentOnAssignment() {
      return true;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignDelete(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignInsert(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignUpdate(auditEntry, (AttributeAssign)owner);
      
    }
  },
  
  /** attribute assigned to an immediate membership assignment */
  imm_mem_asgn {

    /**
     * @see AttributeAssignType#isAssignmentOnAssignment()
     */
    @Override
    public boolean isAssignmentOnAssignment() {
      return true;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignDelete(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignInsert(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignUpdate(auditEntry, (AttributeAssign)owner);
      
    }
  },
  
  /** attribute assigned to an attribute def assignment */
  attr_def_asgn {

    /**
     * @see AttributeAssignType#isAssignmentOnAssignment()
     */
    @Override
    public boolean isAssignmentOnAssignment() {
      return true;
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryDelete(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignDelete(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryInsert(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignInsert(auditEntry, (AttributeAssign)owner);
      
    }

    /**
     * @see AttributeAssignType#decorateAuditEntryUpdate(AuditEntry, Object)
     */
    @Override
    public void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner) {
      this.decorateAuditEntryAssignUpdate(auditEntry, (AttributeAssign)owner);
      
    }
  };

  /**
   * if assignment is to a group
   * @return true if to group
   */
  public boolean isGroup() {
    return this == group;
  }
  
  /**
   * name for javabean
   * @return the attribute assign type
   */
  public String getName() {
    return this.name();
  }
  
  /**
   * see if assignment on assignment
   * @return if assignment on assignment
   */
  public boolean isAssignmentOnAssignment() {
    return false;
  }
  /**
   * 
   * @param auditEntry
   * @param owner 
   */
  public abstract void decorateAuditEntryInsert(AuditEntry auditEntry, Object owner);
  
  /**
   * 
   * @param auditEntry
   * @param owner 
   */
  public abstract void decorateAuditEntryUpdate(AuditEntry auditEntry, Object owner);
  
  /**
   * 
   * @param auditEntry
   * @param owner 
   */
  public abstract void decorateAuditEntryDelete(AuditEntry auditEntry, Object owner);
  
  /**
   * 
   * @param auditEntry
   * @param owner 
   */
  protected void decorateAuditEntryAssignInsert(AuditEntry auditEntry, AttributeAssign owner) {
    auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ASSIGN_ADD.getAuditType().getId());

    auditEntry.assignStringValue(auditEntry.getAuditType(), 
        "ownerAttributeAssignId", owner.getId());
    
    String groupOrStemId = null;
    if (StringUtils.isNotBlank(owner.getOwnerGroupId())) {
      groupOrStemId = owner.getOwnerGroupId();
    } else if (StringUtils.isNotBlank(owner.getOwnerStemId())) {
      groupOrStemId = owner.getOwnerStemId();
    }
    auditEntry.assignStringValue(auditEntry.getAuditType(),  "groupOrStemId", groupOrStemId);

  }
  
  /**
   * 
   * @param auditEntry
   * @param owner 
   */
  protected void decorateAuditEntryAssignUpdate(AuditEntry auditEntry, AttributeAssign owner) {
    auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ASSIGN_UPDATE.getAuditType().getId());

    auditEntry.assignStringValue(auditEntry.getAuditType(), 
        "ownerAttributeAssignId", owner.getId());
    
    String groupOrStemId = null;
    if (StringUtils.isNotBlank(owner.getOwnerGroupId())) {
      groupOrStemId = owner.getOwnerGroupId();
    } else if (StringUtils.isNotBlank(owner.getOwnerStemId())) {
      groupOrStemId = owner.getOwnerStemId();
    }
    auditEntry.assignStringValue(auditEntry.getAuditType(),  "groupOrStemId", groupOrStemId);
    
  }
  
  /**
   * 
   * @param auditEntry
   * @param owner 
   */
  protected void decorateAuditEntryAssignDelete(AuditEntry auditEntry, AttributeAssign owner) {
    auditEntry.setAuditTypeId(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ASSIGN_DELETE.getAuditType().getId());

    auditEntry.assignStringValue(auditEntry.getAuditType(), 
        "ownerAttributeAssignId", owner.getId());
    
    String groupOrStemId = null;
    if (StringUtils.isNotBlank(owner.getOwnerGroupId())) {
      groupOrStemId = owner.getOwnerGroupId();
    } else if (StringUtils.isNotBlank(owner.getOwnerStemId())) {
      groupOrStemId = owner.getOwnerStemId();
    }
    auditEntry.assignStringValue(auditEntry.getAuditType(),  "groupOrStemId", groupOrStemId);
    
  }
  
  /**
   * if assignment is to a group
   * @return true if to group
   */
  public boolean isEffectiveMembership() {
    return this == any_mem;
  }
  
  /**
   * attribute assignment type of an assignment on this assignment
   * @return the type
   */
  public AttributeAssignType getAssignmentOnAssignmentType() {
    throw new RuntimeException("Not applicable for attributeAssignType: " + this);
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeAssignType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeAssignType.class, 
        string, exceptionOnNull);
  }

}
