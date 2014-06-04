/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueDelegate;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotAllowed;
import edu.internet2.middleware.grouper.exception.AttributeDefNameAddException;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.AttributeAssignHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.beans.RulesPermissionBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeAssign;
import edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple;


/**
 * assignment of an attribute
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class AttributeAssign extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, 
    XmlImportableMultiple<AttributeAssign>, AttributeAssignable {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeAssign.class);

  /** name of the groups attribute def table in the db */
  public static final String TABLE_GROUPER_ATTRIBUTE_ASSIGN = "grouper_attribute_assign";

  /** allowed col in db */
  public static final String COLUMN_DISALLOWED = "disallowed";

  /** actions col in db */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_ACTION_ID = "attribute_assign_action_id";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_NOTES = "notes";

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_NAME_ID = "attribute_def_name_id";

  /** column */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_DELEGATABLE = "attribute_assign_delegatable";

  /** column */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_TYPE = "attribute_assign_type";

  /** column */
  public static final String COLUMN_OWNER_GROUP_ID = "owner_group_id";

  /** column */
  public static final String COLUMN_OWNER_STEM_ID = "owner_stem_id";

  /** column */
  public static final String COLUMN_OWNER_MEMBER_ID = "owner_member_id";

  /** column */
  public static final String COLUMN_OWNER_MEMBERSHIP_ID = "owner_membership_id";

  /** column */
  public static final String COLUMN_OWNER_ATTRIBUTE_ASSIGN_ID = "owner_attribute_assign_id";

  /** column */
  public static final String COLUMN_OWNER_ATTRIBUTE_DEF_ID = "owner_attribute_def_id";

  /** column */
  public static final String COLUMN_ENABLED = "enabled";

  /** column */
  public static final String COLUMN_ENABLED_TIME = "enabled_time";

  /** column */
  public static final String COLUMN_DISABLED_TIME = "disabled_time";

  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: disallowed */
  public static final String FIELD_DISALLOWED = "disallowed";

  /** constant for field name for: attributeAssignActionId */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ACTION_ID = "attributeAssignActionId";

  /** constant for field name for: attributeAssignDelegatable */
  public static final String FIELD_ATTRIBUTE_ASSIGN_DELEGATABLE = "attributeAssignDelegatable";

  /** constant for field name for: attributeAssignType */
  public static final String FIELD_ATTRIBUTE_ASSIGN_TYPE = "attributeAssignType";

  /** constant for field name for: attributeDefNameId */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_ID = "attributeDefNameId";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: disabledTimeDb */
  public static final String FIELD_DISABLED_TIME_DB = "disabledTimeDb";

  /** constant for field name for: enabledTimeDb */
  public static final String FIELD_ENABLED_TIME_DB = "enabledTimeDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: notes */
  public static final String FIELD_NOTES = "notes";

  /** constant for field name for: ownerAttributeAssignId */
  public static final String FIELD_OWNER_ATTRIBUTE_ASSIGN_ID = "ownerAttributeAssignId";

  /** constant for field name for: ownerAttributeDefId */
  public static final String FIELD_OWNER_ATTRIBUTE_DEF_ID = "ownerAttributeDefId";

  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerMemberId */
  public static final String FIELD_OWNER_MEMBER_ID = "ownerMemberId";

  /** constant for field name for: ownerMembershipId */
  public static final String FIELD_OWNER_MEMBERSHIP_ID = "ownerMembershipId";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";

  /** constant for field name for: valueDelegate */
  public static final String FIELD_VALUE_DELEGATE = "valueDelegate";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_DISALLOWED, FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, 
      FIELD_ATTRIBUTE_ASSIGN_DELEGATABLE, FIELD_ATTRIBUTE_ASSIGN_TYPE, FIELD_ATTRIBUTE_DEF_NAME_ID, 
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DISABLED_TIME_DB, 
      FIELD_ENABLED_TIME_DB, FIELD_ID, FIELD_LAST_UPDATED_DB, FIELD_NOTES, 
      FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, FIELD_OWNER_ATTRIBUTE_DEF_ID, FIELD_OWNER_GROUP_ID, FIELD_OWNER_MEMBER_ID, 
      FIELD_OWNER_MEMBERSHIP_ID, FIELD_OWNER_STEM_ID, FIELD_VALUE_DELEGATE);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DISALLOWED, FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, 
      FIELD_ATTRIBUTE_ASSIGN_DELEGATABLE, FIELD_ATTRIBUTE_ASSIGN_TYPE, FIELD_ATTRIBUTE_DEF_NAME_ID, 
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DISABLED_TIME_DB, 
      FIELD_ENABLED_TIME_DB, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_LAST_UPDATED_DB, 
      FIELD_NOTES, FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, FIELD_OWNER_ATTRIBUTE_DEF_ID, FIELD_OWNER_GROUP_ID, 
      FIELD_OWNER_MEMBER_ID, FIELD_OWNER_MEMBERSHIP_ID, FIELD_OWNER_STEM_ID, FIELD_VALUE_DELEGATE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * 
   */
  public AttributeAssign() {
    //default
    this.attributeAssignDelegatable = AttributeAssignDelegatable.FALSE;
  }
  
  /**
   * create an attribute assign, including a uuid
   * @param ownerStem
   * @param theAction
   * @param theAttributeDefName
   * @param uuid
   */
  public AttributeAssign(Stem ownerStem, String theAction, AttributeDefName theAttributeDefName, String uuid) {

    this();
    this.attributeDefName = theAttributeDefName;
    this.setAttributeAssignType(AttributeAssignType.stem);
    this.setOwnerStemId(ownerStem.getUuid());
    this.attributeAssignAction = theAttributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(this.attributeAssignAction.getId());
    this.setAttributeDefNameId(theAttributeDefName.getId());
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerAttributeDef
   * @param theAction
   * @param theAttributeDefName
   * @param uuid is uuid or null if generated
   */
  public AttributeAssign(AttributeDef ownerAttributeDef, String theAction, AttributeDefName theAttributeDefName, String uuid) {
    
    this();
    this.setAttributeAssignType(AttributeAssignType.attr_def);
    this.setOwnerAttributeDefId(ownerAttributeDef.getId());
    this.attributeAssignAction = theAttributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(this.attributeAssignAction.getId());
    this.setAttributeDefNameId(theAttributeDefName.getId());
    
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerGroup
   * @param theAction
   * @param theAttributeDefName
   * @param uuid is the uuid or null if generated
   */
  public AttributeAssign(Group ownerGroup, String theAction, AttributeDefName theAttributeDefName, String uuid) {
    
    this();
    this.setAttributeAssignType(AttributeAssignType.group);
    this.setOwnerGroupId(ownerGroup.getUuid());
    this.attributeAssignAction = theAttributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(this.attributeAssignAction.getId());
    this.setAttributeDefNameId(theAttributeDefName.getId());
    
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid.  This is for an immediate or effective membership
   * @param ownerGroup
   * @param ownerMember 
   * @param theAction
   * @param theAttributeDefName
   * @param uuid
   */
  public AttributeAssign(Group ownerGroup, Member ownerMember, String theAction, 
      AttributeDefName theAttributeDefName, String uuid) {
    
    this();
    this.setAttributeAssignType(AttributeAssignType.any_mem);

    //this can be any membership
    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllByGroupOwnerAndMemberAndField(ownerGroup.getId(), ownerMember.getUuid(), 
          Group.getDefaultList(), true);

    if (memberships.size() == 0) {
      throw new RuntimeException("'Any' memberships which have attributes must be immediate or effective on the members list: " 
          + ownerGroup + ", " + GrouperUtil.subjectToString(ownerMember.getSubject()));
    }
    
    this.setOwnerGroupId(ownerGroup.getUuid());
    this.setOwnerMemberId(ownerMember.getUuid());
    this.attributeAssignAction = theAttributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(this.attributeAssignAction.getId());
    this.setAttributeDefNameId(theAttributeDefName.getId());
    
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerAttributeAssign
   * @param theAction
   * @param theAttributeDefName
   * @param uuid to use or null for generated
   */
  public AttributeAssign(AttributeAssign ownerAttributeAssign, String theAction, 
      AttributeDefName theAttributeDefName, String uuid) {
    
    this();
    this.attributeDefName = theAttributeDefName;
    
    this.setAttributeDefNameId(theAttributeDefName.getId());
    
    //cant assign to an assignment of an assignment.
    if (!StringUtils.isBlank(ownerAttributeAssign.getOwnerAttributeAssignId())) {
      throw new RuntimeException("You cant assign an attribute to " +
      		"an assignment of an assignment (only to an assignment of a non-assignment): " 
          + theAction + ", " + theAttributeDefName.getName());
    }
    
    this.setOwnerAttributeAssignId(ownerAttributeAssign.getId());
    
    theAction = StringUtils.defaultIfEmpty(theAction, AttributeDef.ACTION_DEFAULT);
    
    this.attributeAssignAction = theAttributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(this.attributeAssignAction.getId());
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerMembership
   * @param theAction
   * @param theAttributeDefName
   * @param uuid
   */
  public AttributeAssign(Membership ownerMembership, String theAction, AttributeDefName theAttributeDefName, String uuid) {
    
    this();
    this.attributeDefName = theAttributeDefName;
    this.setAttributeAssignType(AttributeAssignType.imm_mem);

    //this must be an immediate, list membership
    if (!ownerMembership.isImmediate()) {
      throw new RuntimeException("Memberships which have attributes must be immediate: " 
          + ownerMembership.getType() + ", " + ownerMembership.getUuid());
    }
    
    if (!Group.getDefaultList().equals(ownerMembership.getList())) {
      throw new RuntimeException("Memberships which have attributes must be list type: " 
          + ownerMembership.getList() + ", " + ownerMembership.getImmediateMembershipId());
      
    }
    
    this.setOwnerMembershipId(ownerMembership.getImmediateMembershipId());

    this.attributeAssignAction = theAttributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(this.attributeAssignAction.getId());
    this.setAttributeDefNameId(theAttributeDefName.getId());
    
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerMember
   * @param theActionId
   * @param theAttributeDefName
   * @param uuid is the uuid or null for generated
   */
  public AttributeAssign(Member ownerMember, String theActionId, AttributeDefName theAttributeDefName, String uuid) {
    
    this();
    
    this.attributeDefName = theAttributeDefName;
    
    this.setAttributeAssignType(AttributeAssignType.member);

    this.setOwnerMemberId(ownerMember.getUuid());
    this.setAttributeAssignActionId(theActionId);
    this.setAttributeDefNameId(theAttributeDefName.getId());
    
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }
  
  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    saveOrUpdate(true);
  }
  
  /**
   * save or update this object
   * @param checkSecurity 
   */
  public void saveOrUpdate(boolean checkSecurity) {
    final AttributeDef theAttributeDef = this.getAttributeDef();
    
    final boolean isInsert = ObjectUtils.equals(this.getHibernateVersionNumber(), GrouperAPI.INITIAL_VERSION_NUMBER);
    
    //validate if allowed
    if (StringUtils.isNotEmpty(this.ownerGroupId) && StringUtils.isEmpty(this.ownerMemberId) && !theAttributeDef.isAssignToGroup()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to group: " + theAttributeDef + ", " + this.ownerGroupId + ", to allow this, make sure the attributeDef has setAssignToGroup(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerStemId) && !theAttributeDef.isAssignToStem()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to stem: " + theAttributeDef + ", " + this.ownerStemId + ", to allow this, make sure the attributeDef has setAssignToStem(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerMemberId) && StringUtils.isEmpty(this.ownerGroupId) && !theAttributeDef.isAssignToMember()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to member: " + theAttributeDef + ", " + this.ownerMemberId + ", to allow this, make sure the attributeDef has setAssignToMember(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerMembershipId) && !theAttributeDef.isAssignToImmMembership()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to immediate membership: " + theAttributeDef + ", " + this.ownerMembershipId + ", to allow this, make sure the attributeDef has setAssignToImmMembership(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerAttributeDefId) && !theAttributeDef.isAssignToAttributeDef()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to attribute def: " + theAttributeDef + ", " + this.ownerAttributeDefId + ", to allow this, make sure the attributeDef has setAssignToAttributeDef(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerMemberId) && StringUtils.isNotEmpty(this.ownerGroupId) && !theAttributeDef.isAssignToEffMembership()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to effective membership: " + theAttributeDef + ", " + this.ownerGroupId + ", " + this.ownerMemberId + ", to allow this, make sure the attributeDef has setAssignToEffMembership(true)");
    }
    
    final AttributeDefName ATTRIBUTE_DEF_NAME = this.getAttributeDefName();

    if (StringUtils.isNotEmpty(this.ownerAttributeAssignId)) {
      AttributeAssign ownerAttributeAssign = this.getOwnerAttributeAssign();
      AttributeAssignType ownerType = ownerAttributeAssign.getAttributeAssignType();

      if (AttributeAssignType.group == ownerType) {
        this.attributeAssignType = AttributeAssignType.group_asgn;
        
        if (!theAttributeDef.isAssignToGroupAssn()) {
          throw new RuntimeException("Attribute is not assignable to a group attribute assignment, nameOfAttributeDefName: " 
              + ATTRIBUTE_DEF_NAME.getName() + ", nameOfAttributeDef: " + theAttributeDef.getName());
        }


      } else if (AttributeAssignType.stem == ownerType) {
        this.attributeAssignType = AttributeAssignType.stem_asgn;

        if (!theAttributeDef.isAssignToStemAssn()) {
          throw new RuntimeException("Attribute is not assignable to a stem attribute assignment, nameOfAttributeDefName: " 
              + ATTRIBUTE_DEF_NAME.getName() + ", nameOfAttributeDef: " + theAttributeDef.getName());
        }
      } else if (AttributeAssignType.member == ownerType) {
        this.attributeAssignType = AttributeAssignType.mem_asgn;
        
        if (!theAttributeDef.isAssignToMemberAssn()) {
          throw new RuntimeException("Attribute is not assignable to a member attribute assignment, nameOfAttributeDefName: " 
              + ATTRIBUTE_DEF_NAME.getName() + ", nameOfAttributeDef: " + theAttributeDef.getName());
        }

      } else if (AttributeAssignType.attr_def == ownerType) {
        this.attributeAssignType = AttributeAssignType.attr_def_asgn;
      
        if (!theAttributeDef.isAssignToAttributeDefAssn()) {
          throw new RuntimeException("Attribute is not assignable to a attribute definition attribute assignment, nameOfAttributeDefName: " 
              + ATTRIBUTE_DEF_NAME.getName() + ", nameOfAttributeDef: " + theAttributeDef.getName());
        }

      } else if (AttributeAssignType.any_mem == ownerType) {
        this.attributeAssignType = AttributeAssignType.any_mem_asgn;

        if (!theAttributeDef.isAssignToEffMembershipAssn()) {
          throw new RuntimeException("Attribute is not assignable to an effective membership attribute assignment, nameOfAttributeDefName: " 
              + ATTRIBUTE_DEF_NAME.getName() + ", nameOfAttributeDef: " + theAttributeDef.getName());
        }

      } else if (AttributeAssignType.imm_mem == ownerType) {
        this.attributeAssignType = AttributeAssignType.imm_mem_asgn;
      
        if (!theAttributeDef.isAssignToImmMembershipAssn()) {
          throw new RuntimeException("Attribute is not assignable to a immediate membership attribute assignment, nameOfAttributeDefName: " 
              + ATTRIBUTE_DEF_NAME.getName() + ", nameOfAttributeDef: " + theAttributeDef.getName());
        }

      } else {
        throw new RuntimeException("Not expecting attribute on ownerAttributeType: " + ownerType);
      }

    }
    
    //this is the owner of the assignment
    final AttributeAssignable attributeAssignable = AttributeAssign.this.retrieveAttributeAssignable();
    
    //make sure subject is allowed to do this
    final AttributeAssignAction ATTRIBUTE_ASSIGN_ACTION = AttributeAssign.this.getAttributeAssignAction();

    if (checkSecurity) {
      attributeAssignable.getAttributeDelegate().assertCanUpdateAttributeDefName(ATTRIBUTE_DEF_NAME);
    }
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            try {

              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

              String differences = null;
              if (!hibernateHandlerBean.isCallerWillCreateAudit() && !isInsert) {
                differences = GrouperUtil.dbVersionDescribeDifferences(AttributeAssign.this.dbVersion(), 
                    AttributeAssign.this, AttributeAssign.this.dbVersion() != null ? AttributeAssign.this.dbVersionDifferentFields() : AttributeAssign.CLONE_FIELDS);
              }
              
              GrouperDAOFactory.getFactory().getAttributeAssign().saveOrUpdate(AttributeAssign.this);
              
              //if enabled permission
              if (AttributeAssign.this.isEnabled() && AttributeDefType.perm == theAttributeDef.getAttributeDefType()
                  && AttributeAssign.this.getAttributeAssignType() == AttributeAssignType.any_mem) {
                
                //fire rule
                RulesPermissionBean rulesPermissionBean = new RulesPermissionBean(
                    AttributeAssign.this, 
                    AttributeAssign.this.getOwnerGroup(),
                    AttributeAssign.this.getOwnerMember(),
                    ATTRIBUTE_DEF_NAME,
                    theAttributeDef,
                    ATTRIBUTE_ASSIGN_ACTION.getName());

                //fire rules directly connected to this permission add
                RuleEngine.fireRule(RuleCheckType.permissionAssignToSubject, rulesPermissionBean);
                
                RuleEngine.fireRule(RuleCheckType.subjectAssignInStem, rulesPermissionBean);

              }

              if (AttributeDefType.perm != theAttributeDef.getAttributeDefType()) {
                if (AttributeAssign.this.disallowed) {
                  throw new RuntimeException("You can only have an attribute assignment which is not " +
                      "allowed if the attribute definition is a permission: " + theAttributeDef);
                }
              }
              
              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                AuditEntry auditEntry = new AuditEntry();
                
                if (isInsert) {
                  
                  AttributeAssign.this.getAttributeAssignType().decorateAuditEntryInsert(auditEntry, attributeAssignable);
                  
                  if (AttributeDefType.perm == theAttributeDef.getAttributeDefType() && AttributeAssign.this.disallowed) {
                    auditEntry.setDescription("Added attribute assignment, disallowed");
                  } else {
                    auditEntry.setDescription("Added attribute assignment");
                  }

                } else {

                  AttributeAssign.this.getAttributeAssignType().decorateAuditEntryUpdate(auditEntry, attributeAssignable);
                  auditEntry.setDescription("Updated attribute assignment: " + differences);
                  
                }

                auditEntry.assignStringValue(auditEntry.getAuditType(), "id", AttributeAssign.this.getId());
                auditEntry.assignStringValue(auditEntry.getAuditType(), "attributeDefNameName", ATTRIBUTE_DEF_NAME.getName());
                auditEntry.assignStringValue(auditEntry.getAuditType(), "attributeDefNameId", AttributeAssign.this.getAttributeDefNameId());
                auditEntry.assignStringValue(auditEntry.getAuditType(), "action", ATTRIBUTE_ASSIGN_ACTION.getName());
                auditEntry.assignStringValue(auditEntry.getAuditType(), "attributeDefId", theAttributeDef.getId());

                auditEntry.saveOrUpdate(true);
              }
              return null;
            } catch (HookVeto hv) {
              throw hv;
            } catch (AttributeDefNameAddException adnae) {
              throw adnae;
            } catch (Exception e) {
              throw new AttributeDefNameAddException( "Cannot saveOrUpdate attribute assign: " + e.getMessage(), e );
            }
          }
        });
    
  }
  
  /** in attribute assign delete */
  private static ThreadLocal<Set<AttributeAssign>> attributeAssignDeletes = new ThreadLocal<Set<AttributeAssign>>();

  /**
   * @return if in delete
   */
  @SuppressWarnings("unchecked")
  public static Set<AttributeAssign> attributeAssignDeletes() {
    Set<AttributeAssign> attributeAssignDeletesSet =  attributeAssignDeletes.get();
    return attributeAssignDeletesSet == null ? null : Collections.unmodifiableSet(attributeAssignDeletesSet);
  }
  
  /**
   * delete this object
   */
  public void delete() {
    
    //TODO does this check to see if allowed???
    
    boolean clearInAttributeAssignDelete = false;
    Set<AttributeAssign> attributeAssignDeletesSet =  attributeAssignDeletes.get();
    if (attributeAssignDeletesSet == null || !attributeAssignDeletesSet.contains(this)) {
      if (attributeAssignDeletesSet == null) {
        attributeAssignDeletesSet = new HashSet<AttributeAssign>();
        attributeAssignDeletes.set(attributeAssignDeletesSet);
      }
      attributeAssignDeletesSet.add(this);
      clearInAttributeAssignDelete = true;
    }
    try {
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
          AuditControl.WILL_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
  
          //delete other assignments on this assignment
          Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
            .findByOwnerAttributeAssignId(AttributeAssign.this.getId(), new QueryOptions().secondLevelCache(false));
          
          for (AttributeAssign attributeAssign : attributeAssigns) {
            attributeAssign.delete();
            hibernateHandlerBean.getHibernateSession().getSession().flush();
          }
  
          //delete any values based on this assignment
          Set<AttributeAssignValue> attributeAssignValues = GrouperDAOFactory.getFactory()
            .getAttributeAssignValue().findByAttributeAssignId(AttributeAssign.this.getId(), new QueryOptions().secondLevelCache(false));
  
          for (AttributeAssignValue attributeAssignValue : attributeAssignValues) {
            attributeAssignValue.delete();
            hibernateHandlerBean.getHibernateSession().getSession().flush();
          }
  
  
          //delete the assignment
          GrouperDAOFactory.getFactory().getAttributeAssign().delete(AttributeAssign.this);
  
          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            AuditEntry auditEntry = new AuditEntry();
            AttributeDefName theAttributeDefName = AttributeAssign.this.getAttributeDefName();
            AttributeAssign.this.getAttributeAssignType().decorateAuditEntryInsert(auditEntry, AttributeAssign.this.retrieveAttributeAssignable());

            AttributeDef theAttributeDef = theAttributeDefName.getAttributeDef();
            
            if (AttributeDefType.perm == theAttributeDef.getAttributeDefType() && AttributeAssign.this.disallowed) {
              auditEntry.setDescription("Deleted attribute assignment, disallowed");
            } else {
              auditEntry.setDescription("Deleted attribute assignment");
            }
            
            auditEntry.assignStringValue(auditEntry.getAuditType(), "id", AttributeAssign.this.getId());
            auditEntry.assignStringValue(auditEntry.getAuditType(), "attributeDefNameName", theAttributeDefName.getName());
            auditEntry.assignStringValue(auditEntry.getAuditType(), "attributeDefNameId", AttributeAssign.this.getAttributeDefNameId());
            auditEntry.assignStringValue(auditEntry.getAuditType(), "action", AttributeAssign.this.getAttributeAssignAction().getName());
            auditEntry.assignStringValue(auditEntry.getAuditType(), "attributeDefId", attributeDef.getId());
  
            auditEntry.saveOrUpdate(true);
          }
  
          return null;
        }
      });
    } catch (RuntimeException e) {
      GrouperUtil.injectInException(e, " Problem deleting attribute assign: " + this + " ");
      throw e;
    } finally {
      if (clearInAttributeAssignDelete) {
        attributeAssignDeletesSet.remove(this);
      }
    }
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeAssign clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
  
  /** attribute name in this assignment */
  private String attributeDefNameId;

  /** if the subjects assigned to the attribute can delegate to someone else, or delegate as delegatable */
  private AttributeAssignDelegatable attributeAssignDelegatable;
  
  /** type of assignment */
  private AttributeAssignType attributeAssignType;
  
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
   * internal method for hibernate to set if delegatable
   * @param theAttributeAssignDelegatableDb
   */
  public void setAttributeAssignDelegatableDb(String theAttributeAssignDelegatableDb) {
    this.attributeAssignDelegatable = AttributeAssignDelegatable.valueOfIgnoreCase(
        theAttributeAssignDelegatableDb, false);
  }
  
  /**
   * @param attributeAssignDelegatable1 the attributeAssignDelegatable to set
   */
  public void setAttributeAssignDelegatable(
      AttributeAssignDelegatable attributeAssignDelegatable1) {
    this.attributeAssignDelegatable = attributeAssignDelegatable1;
  }

  /**
   * get the enum for delegatable, should not return null
   * @return the attributeAssignDelegatable
   */
  public AttributeAssignType getAttributeAssignType() {
    return this.attributeAssignType;
  }

  /**
   * internal method for hibernate to persist this enum
   * @return the string value (enum name)
   */
  public String getAttributeAssignTypeDb() {
    return this.getAttributeAssignType().name();
  }

  /**
   * internal method for hibernate to set if delegatable
   * @param theAttributeAssignTypeDb
   */
  public void setAttributeAssignTypeDb(String theAttributeAssignTypeDb) {
    this.attributeAssignType = AttributeAssignType.valueOfIgnoreCase(
        theAttributeAssignTypeDb, false);
  }
  
  /**
   * @param attributeAssignType1 the attributeAssignDelegatable to set
   */
  public void setAttributeAssignType(
      AttributeAssignType attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  
  /** if this is an attribute assign attribute, this is the foreign key */
  private String ownerAttributeAssignId;
  
  /** if this is an attribute def attribute, this is the foreign key */
  private String ownerAttributeDefId;
  
  /** if this is a group attribute, this is the foreign key */
  private String ownerGroupId;
  
  /** if this is a member attribute, this is the foreign key */
  private String ownerMemberId;
  
  /** if this is a membership attribute, this is the foreign key */
  private String ownerMembershipId;
  
  /** if this is a stem attribute, this is the foreign key */
  private String ownerStemId;
  
  /** id of this attribute assign */
  private String id;

  /** context id of the transaction */
  private String contextId;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * notes about this assignment, free-form text
   */
  private String notes;

  /**
   * id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  private String attributeAssignActionId;
  
  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   */
  private boolean disallowed = false;
  
  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @param disallowed1 the allowed to set
   */
  public void setDisallowed(boolean disallowed1) {
    this.disallowed = disallowed1;
  }

  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @param disallowed1 the allowed to set
   */
  public void setDisallowedDb(String disallowed1) {
    this.disallowed = GrouperUtil.booleanValue(disallowed1, false);
  }

  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @return the allowed
   */
  public String getDisallowedDb() {
    return this.disallowed ? "T" : "F";
  }

  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @return if allowed
   */
  public boolean isDisallowed() {
    return this.disallowed;
  }

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   */
  private Long enabledTimeDb;
  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   */
  private Long disabledTimeDb;
  
  /**
   * action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT.
   * action must exist in AttributeDef.actions
   * @return the action
   */
  public String getAttributeAssignActionId() {
    return this.attributeAssignActionId;
  }

  /** cache the attribute def name */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeDefName attributeDefName;

  /**
   * set this for caching
   * @param attributeDefName1
   */
  public void internalSetAttributeDefName(AttributeDefName attributeDefName1) {
    
    if (attributeDefName1 != null) {
      if (!StringUtils.equals(this.attributeDefNameId, attributeDefName1.getId())) {
        throw new RuntimeException("Why does the attributeDefName id " 
            + this.attributeDefNameId + " not equal the param id: " + attributeDefName1.getId());
      }
    }
    
    this.attributeDefName = attributeDefName1;
  }
  

  /**
   * set this for caching
   * @param attributeDef1
   */
  public void internalSetAttributeDef(AttributeDef attributeDef1) {
    
    this.attributeDef = attributeDef1;
  }
  
  /**
   * 
   * @return attributeDefName
   */
  public AttributeDefName getAttributeDefName() {
    if (this.attributeDefName == null ) {
      this.attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByUuidOrName(this.attributeDefNameId, null, true);
    }
    return this.attributeDefName;
  }
  
  /** cache the attribute assign action */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignAction attributeAssignAction;
  
  /**
   * 
   * @return attributeAssignAction
   */
  public AttributeAssignAction getAttributeAssignAction() {
    if (this.attributeAssignAction == null ) {
      this.attributeAssignAction = GrouperDAOFactory.getFactory()
        .getAttributeAssignAction().findById(this.attributeAssignActionId, true);
    }
    return this.attributeAssignAction;
  }
  
  /** cache the attribute def of this attribute def name */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeDef attributeDef;
  
  /**
   * 
   * @return attributeDef
   */
  public AttributeDef getAttributeDef() {
    if (this.attributeDef == null ) {
      this.attributeDef = AttributeDefFinder.findByAttributeDefNameId(this.attributeDefNameId, true);
    }
    return this.attributeDef;
  }
  
  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignAttrAssignDelegate attributeAssignAttrAssignDelegate;
  
  /**
   * 
   * @return the delegate
   */
  public AttributeAssignAttrAssignDelegate getAttributeDelegate() {
    if (this.attributeAssignAttrAssignDelegate == null) {
      this.attributeAssignAttrAssignDelegate = new AttributeAssignAttrAssignDelegate(this);
    }
    return this.attributeAssignAttrAssignDelegate;
  }

  
  /**
   * default is "assign" actions must contain only alphanumeric or underscore, case sensitive
   * e.g. id for read,write,admin
   * @param theActionId
   */
  public void setAttributeAssignActionId(String theActionId) {
    this.attributeAssignActionId = theActionId;
  }

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * id of this attribute assign
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id of this attribute assign
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }
  
  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * notes about this assignment, free-form text
   * @return the notes
   */
  public String getNotes() {
    return this.notes;
  }

  /**
   * notes about this assignment, free-form text
   * @param notes1
   */
  public void setNotes(String notes1) {
    this.notes = notes1;
  }

  
  /**
   * attribute name in this assignment
   * @return the attributeNameId
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  
  /**
   * attribute name in this assignment
   * @param attributeDefNameId1 the attributeNameId to set
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
    //reset cached object
    this.attributeDefName = null;
    this.attributeDef = null;
  }

  
  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @return the ownerAttributeAssignId
   */
  public String getOwnerAttributeAssignId() {
    return this.ownerAttributeAssignId;
  }

  
  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @param ownerAttributeAssignId1 the ownerAttributeAssignId to set
   */
  public void setOwnerAttributeAssignId(String ownerAttributeAssignId1) {
    this.ownerAttributeAssignId = ownerAttributeAssignId1;
  }

  
  /**
   * if this is an attribute def attribute, this is the foreign key
   * @return the ownerAttributeDefId
   */
  public String getOwnerAttributeDefId() {
    return this.ownerAttributeDefId;
  }

  
  /**
   * if this is an attribute def attribute, this is the foreign key
   * @param ownerAttributeDefId1 the ownerAttributeDefId to set
   */
  public void setOwnerAttributeDefId(String ownerAttributeDefId1) {
    this.ownerAttributeDefId = ownerAttributeDefId1;
  }

  
  /**
   * if this is a group attribute, this is the foreign key
   * @return the ownerAttributeGroupId
   */
  public String getOwnerGroupId() {
    return this.ownerGroupId;
  }

  /**
   * if this is a group attribute, this is the foreign key
   * @return the ownerGroup
   */
  public Group getOwnerGroup() {
    
    //I think the current grouper session isnt really relevant here, I think we just need to produce the group without security
    return this.ownerGroupId == null ? null : GrouperDAOFactory.getFactory().getGroup().findByUuid(this.ownerGroupId, true);

  }

  /**
   * if this is a membership attribute, this is the foreign key
   * @return the ownerMembership
   */
  public Membership getOwnerMembership() {
    
    //I think the current grouper session isnt really relevant here, I think we just need to produce the membership without security
    return this.ownerMembershipId == null ? null : GrouperDAOFactory.getFactory().getMembership().findByUuid(this.ownerMembershipId, true, false);

  }

  /**
   * if this is a member attribute, this is the foreign key
   * @return the ownerMember
   */
  public Member getOwnerMember() {
    
    //I think the current grouper session isnt really relevant here, I think we just need to produce the group without security
    return this.ownerMemberId == null ? null : GrouperDAOFactory.getFactory().getMember().findByUuid(this.ownerMemberId, true);

  }

  /**
   * if this is a attribute assign attribute, this is the foreign key
   * @return the ownerAttributeAssign
   */
  public AttributeAssign getOwnerAttributeAssign() {
    
    //I think the current grouper session isnt really relevant here, I think we just need to produce the group without security
    return this.ownerAttributeAssignId == null ? null : GrouperDAOFactory.getFactory().getAttributeAssign().findById(this.ownerAttributeAssignId, true);

  }

  /**
   * if this is a attributeDef attribute, this is the foreign key
   * @return the ownerAttributeDef
   */
  public AttributeDef getOwnerAttributeDef() {
    
    //I think the current grouper session isnt really relevant here, I think we just need to produce the attribute def without security
    return this.ownerAttributeDefId == null ? null : GrouperDAOFactory.getFactory().getAttributeDef().findById(this.ownerAttributeDefId, true);

  }

  /**
   * if this is a membership attribute, this is the foreign key
   * @return the ownerAttributeDef
   */
  public Membership getOwnerImmediateMembership() {
    
    //I think the current grouper session isnt really relevant here, I think we just need to produce the attribute def without security
    return this.ownerMembershipId == null ? null : GrouperDAOFactory.getFactory().getMembership().findByUuid(this.ownerMembershipId, true, false);

  }

  /**
   * if this is a stem attribute, this is the foreign key
   * @return the ownerStem
   */
  public Stem getOwnerStem() {
    
    return this.ownerStemId == null ? null : GrouperDAOFactory.getFactory().getStem().findByUuid(this.ownerStemId, true);

  }

  
  /**
   * if this is a group attribute, this is the foreign key
   * @param ownerAttributeGroupId1 the ownerAttributeGroupId to set
   */
  public void setOwnerGroupId(String ownerAttributeGroupId1) {
    this.ownerGroupId = ownerAttributeGroupId1;
  }

  
  /**
   * if this is a member attribute, this is the foreign key
   * @return the ownerAttributeMemberId
   */
  public String getOwnerMemberId() {
    return this.ownerMemberId;
  }

  
  /**
   * if this is a member attribute, this is the foreign key
   * @param ownerAttributeMemberId1 the ownerAttributeMemberId to set
   */
  public void setOwnerMemberId(String ownerAttributeMemberId1) {
    this.ownerMemberId = ownerAttributeMemberId1;
  }

  
  /**
   * if this is a membership attribute, this is the foreign key
   * @return the ownerAttributeMembershipId
   */
  public String getOwnerMembershipId() {
    return this.ownerMembershipId;
  }

  
  /**
   * if this is a membership attribute, this is the foreign key
   * @param ownerAttributeMembershipId1 the ownerAttributeMembershipId to set
   */
  public void setOwnerMembershipId(String ownerAttributeMembershipId1) {
    this.ownerMembershipId = ownerAttributeMembershipId1;
  }

  
  /**
   * if this is a stem attribute, this is the foreign key
   * @return the ownerAttributeStemId
   */
  public String getOwnerStemId() {
    return this.ownerStemId;
  }

  
  /**
   * if this is a stem attribute, this is the foreign key
   * @param ownerAttributeStemId1 the ownerAttributeStemId to set
   */
  public void setOwnerStemId(String ownerAttributeStemId1) {
    this.ownerStemId = ownerAttributeStemId1;
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
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @return the enabled
   */
  public String getEnabledDb() {
    return this.isEnabled() ? "T" : "F";
  }

  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * dont call this method, its for hibernate
   * @param enabled1 the enabled to set
   */
  public void setEnabledDb(@SuppressWarnings("unused") String enabled1) {
    //note enabled is handled by dates
  }

  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * dont call this method, its for hibernate
   * @param enabled1 the enabled to set
   */
  public void setEnabled(@SuppressWarnings("unused") boolean enabled1) {
    //note enabeld is handled by dates
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
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @param enabledTimeDb1 the enabledTimeDb to set
   */
  public void setEnabledTimeDb(Long enabledTimeDb1) {
    this.enabledTimeDb = enabledTimeDb1;
  }

  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @return the disabledTimeDb
   */
  public Long getDisabledTimeDb() {
    return this.disabledTimeDb;
  }

  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @param disabledTimeDb1 the disabledTimeDb to set
   */
  public void setDisabledTimeDb(Long disabledTimeDb1) {
    this.disabledTimeDb = disabledTimeDb1;
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
   * @param enabledTimeDb1 the enabledTimeDb to set
   */
  public void setEnabledTime(Timestamp enabledTimeDb1) {
    this.enabledTimeDb = enabledTimeDb1 == null ? null : enabledTimeDb1.getTime();
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
   * @param disabledTimeDb1 the disabledTimeDb to set
   */
  public void setDisabledTime(Timestamp disabledTimeDb1) {
    this.disabledTimeDb = disabledTimeDb1 == null ? null : disabledTimeDb1.getTime();
  }

  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this);
    try {
      // Bypass privilege checks.  If the group is loaded it is viewable.
      toStringBuilder
        .append( "id", this.id)
        .append( "action", this.getAttributeAssignAction().getName() )
        .append( "attributeDefName", this.getAttributeDefName().getName() );
      
      if (!StringUtils.isBlank(this.ownerStemId)) {
        toStringBuilder.append("stem", 
            StemFinder.findByUuid(GrouperSession.staticGrouperSession()
                .internal_getRootSession(), this.ownerStemId, true));
      }
      if (!StringUtils.isBlank(this.ownerGroupId)) {
        toStringBuilder.append("group", 
            GroupFinder.findByUuid(GrouperSession.staticGrouperSession()
                .internal_getRootSession(), this.ownerGroupId, true));
      }
      if (!StringUtils.isBlank(this.ownerMemberId)) {
        toStringBuilder.append("subjectId", 
            MemberFinder.findByUuid(GrouperSession.staticGrouperSession()
                .internal_getRootSession(), this.ownerMemberId, true));
      }
      if (!StringUtils.isBlank(this.ownerMembershipId)) {
        toStringBuilder.append("membershipId", 
                this.ownerMembershipId);
      }
      if (!StringUtils.isBlank(this.ownerAttributeDefId)) {
        toStringBuilder.append("attributeDef", 
            GrouperDAOFactory.getFactory().getAttributeDef().findById(
                this.ownerAttributeDefId, true));
      }
      if (!StringUtils.isBlank(this.ownerAttributeAssignId)) {
        toStringBuilder.append("ownerAttributeAssignId", 
            this.ownerAttributeAssignId);
      }
      
    } catch (Exception e) {
      //ignore, did all we could
    }
    return toStringBuilder.toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(AttributeAssign existingRecord) {
    
    existingRecord.setDisallowed(existingRecord.isDisallowed());
    existingRecord.setAttributeAssignActionId(existingRecord.getAttributeAssignActionId());
    existingRecord.setAttributeAssignDelegatable(existingRecord.getAttributeAssignDelegatable());
    existingRecord.setAttributeAssignType(existingRecord.getAttributeAssignType());
    existingRecord.setAttributeDefNameId(existingRecord.getAttributeDefNameId());
    existingRecord.setDisabledTimeDb(existingRecord.getDisabledTimeDb());
    existingRecord.setEnabledTimeDb(existingRecord.getEnabledTimeDb());
    existingRecord.setId(existingRecord.getId());
    existingRecord.setNotes(existingRecord.getNotes());
    existingRecord.setOwnerAttributeAssignId(existingRecord.getOwnerAttributeAssignId());
    existingRecord.setOwnerAttributeDefId(existingRecord.getOwnerAttributeDefId());
    existingRecord.setOwnerGroupId(existingRecord.getOwnerGroupId());
    existingRecord.setOwnerMemberId(existingRecord.getOwnerMemberId());
    existingRecord.setOwnerMembershipId(existingRecord.getOwnerMembershipId());
    existingRecord.setOwnerStemId(existingRecord.getOwnerStemId());

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(AttributeAssign other) {
    if (this.disallowed != other.disallowed) {
      return true;
    }
    if (!StringUtils.equals(this.attributeAssignActionId, other.attributeAssignActionId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.attributeAssignDelegatable, other.attributeAssignDelegatable)) {
      return true;
    }
    if (!GrouperUtil.equals(this.attributeAssignType, other.attributeAssignType)) {
      return true;
    }
    if (!StringUtils.equals(this.attributeDefNameId, other.attributeDefNameId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.disabledTimeDb, other.disabledTimeDb)) {
      return true;
    }
    if (!GrouperUtil.equals(this.enabledTimeDb, other.enabledTimeDb)) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!StringUtils.equals(this.notes, other.notes)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerAttributeAssignId, other.ownerAttributeAssignId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerAttributeDefId, other.ownerAttributeDefId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerGroupId, other.ownerGroupId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerMemberId, other.ownerMemberId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerMembershipId, other.ownerMembershipId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerStemId, other.ownerStemId)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(AttributeAssign other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.createdOnDb, other.createdOnDb)) {
      return true;
    }
    if (!GrouperUtil.equals(this.lastUpdatedDb, other.lastUpdatedDb)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveBusinessProperties(java.lang.Object)
   */
  public AttributeAssign xmlSaveBusinessProperties(AttributeAssign existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      
      AttributeAssignResult attributeAssignResult = null;
      AttributeAssignAction theAttributeAssignAction = this.getAttributeAssignAction();
      AttributeDefName theAttributeDefName = this.getAttributeDefName();
        
      if (!StringUtils.isBlank(this.ownerAttributeAssignId)) {
        
        AttributeAssign ownerAttributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign()
          .findById(this.ownerAttributeAssignId, true);
        attributeAssignResult = ownerAttributeAssign.getAttributeDelegate()
          .internal_assignAttributeHelper(theAttributeAssignAction.getName(), theAttributeDefName, true, this.id, PermissionAllowed.fromDisallowedBoolean(this.disallowed));
        
      } else if (!StringUtils.isBlank(this.ownerAttributeDefId)) {

        AttributeDef ownerAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef()
          .findById(this.ownerAttributeDefId, true);
        attributeAssignResult = ownerAttributeDef.getAttributeDelegate()
          .internal_assignAttributeHelper(theAttributeAssignAction.getName(), theAttributeDefName, true, this.id, PermissionAllowed.fromDisallowedBoolean(this.disallowed));
      
      } else if (!StringUtils.isBlank(this.ownerGroupId)) {

        Group ownerGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerGroupId, true);
        attributeAssignResult = ownerGroup.getAttributeDelegate()
          .internal_assignAttributeHelper(theAttributeAssignAction.getName(), theAttributeDefName, true, this.id, PermissionAllowed.fromDisallowedBoolean(this.disallowed));
        
      } else if (!StringUtils.isBlank(this.ownerMemberId)) {
        
        Member ownerMember = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerMemberId, true);
        attributeAssignResult = ownerMember.getAttributeDelegate()
          .internal_assignAttributeHelper(theAttributeAssignAction.getName(), theAttributeDefName, true, this.id, PermissionAllowed.fromDisallowedBoolean(this.disallowed));

      } else if (!StringUtils.isBlank(this.ownerMembershipId)) {
        
        Membership ownerMembership = GrouperDAOFactory.getFactory().getMembership().findByUuid(this.ownerMembershipId, true, false);
        attributeAssignResult = ownerMembership.getAttributeDelegate()
          .internal_assignAttributeHelper(theAttributeAssignAction.getName(), theAttributeDefName, true, this.id, PermissionAllowed.fromDisallowedBoolean(this.disallowed));

      } else if (!StringUtils.isBlank(this.ownerStemId)) {
        
        Stem ownerStem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), this.id, true);
        attributeAssignResult = ownerStem.getAttributeDelegate()
          .internal_assignAttributeHelper(theAttributeAssignAction.getName(), theAttributeDefName, true, this.id, PermissionAllowed.fromDisallowedBoolean(this.disallowed));

      } else {
        throw new RuntimeException("Cant find owner: " + this);
      }
      
      existingRecord = attributeAssignResult.getAttributeAssign();
      
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.saveOrUpdate(true);
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAttributeAssign().saveUpdateProperties(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple#xmlRetrieveByIdOrKey(java.util.Collection)
   */
  public AttributeAssign xmlRetrieveByIdOrKey(Collection<String> idsToIgnore) {
    return GrouperDAOFactory.getFactory().getAttributeAssign().findByUuidOrKey(idsToIgnore,
        this.id, this.attributeDefNameId, this.attributeAssignActionId, this.ownerAttributeAssignId, this.ownerAttributeDefId, this.ownerGroupId,
        this.ownerMemberId, this.ownerMembershipId, this.ownerStemId,  
        false, this.disabledTimeDb, this.enabledTimeDb, this.notes, this.disallowed);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAttributeAssign xmlToExportAttributeAssign(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    XmlExportAttributeAssign xmlExportAttributeAssign = new XmlExportAttributeAssign();
    xmlExportAttributeAssign.setAttributeAssignActionId(this.getAttributeAssignActionId());
    xmlExportAttributeAssign.setAttributeAssignDelegatable(this.getAttributeAssignDelegatableDb());
    xmlExportAttributeAssign.setAttributeAssignType(this.getAttributeAssignTypeDb());
    xmlExportAttributeAssign.setAttributeDefNameId(this.getAttributeDefNameId());
    xmlExportAttributeAssign.setContextId(this.getContextId());
    xmlExportAttributeAssign.setCreateTime(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportAttributeAssign.setDisabledTime(GrouperUtil.dateStringValue(this.getDisabledTimeDb()));
    xmlExportAttributeAssign.setDisallowed(this.getDisallowedDb());
    xmlExportAttributeAssign.setEnabled(this.getEnabledDb());
    xmlExportAttributeAssign.setEnabledTime(GrouperUtil.dateStringValue(this.getEnabledTimeDb()));
    xmlExportAttributeAssign.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportAttributeAssign.setModifierTime(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    xmlExportAttributeAssign.setNotes(this.getNotes());
    xmlExportAttributeAssign.setOwnerAttributeAssignId(this.getOwnerAttributeAssignId());
    xmlExportAttributeAssign.setOwnerAttributeDefId(this.getOwnerAttributeDefId());
    xmlExportAttributeAssign.setOwnerGroupId(this.getOwnerGroupId());
    xmlExportAttributeAssign.setOwnerMemberId(this.getOwnerMemberId());
    xmlExportAttributeAssign.setOwnerMembershipId(this.getOwnerMembershipId());
    xmlExportAttributeAssign.setOwnerStemId(this.getOwnerStemId());
    xmlExportAttributeAssign.setUuid(this.getId());
     
    return xmlExportAttributeAssign;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlGetId()
   */
  public String xmlGetId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setId(theId);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("AttributeAssign: " + this.getId());

//    XmlExportUtils.toStringAttributeAssign(null, stringWriter, this, false);
    
    return stringWriter.toString();
    
  }

  /**
   * get the delegate that relates the object with the the assignment
   * @return the delegate
   */
  public AttributeAssignable retrieveAttributeAssignable() {
    AttributeDef theAttributeDef = this.getAttributeDef();
    if (theAttributeDef.isAssignToStem() && !StringUtils.isBlank(this.ownerStemId)) {
      Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerStemId, true);
      return stem;
    }
    if (theAttributeDef.isAssignToAttributeDef() && !StringUtils.isBlank(this.ownerAttributeDefId)) {
      AttributeDef attributeDefOwner = AttributeDefFinder.findById(this.ownerAttributeDefId, true);
      return attributeDefOwner;
    }
    if (theAttributeDef.isAssignToEffMembership() && !StringUtils.isBlank(this.ownerMemberId) && !StringUtils.isBlank(this.ownerGroupId)) {
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerGroupId, true);
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerMemberId, true);
      return new GroupMember(group, member);
    }
    if (theAttributeDef.isAssignToGroup() && !StringUtils.isBlank(this.ownerGroupId)) {
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerGroupId, true);
      return group;
    }
    if (theAttributeDef.isAssignToMember() && !StringUtils.isBlank(this.ownerMemberId)) {
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerMemberId, true);
      return member;
    }
    if (theAttributeDef.isAssignToImmMembership() && !StringUtils.isBlank(this.ownerMembershipId)) {
      Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(this.ownerMembershipId, true, false);
      return membership;
    }
    if ((theAttributeDef.isAssignToAttributeDefAssn() || theAttributeDef.isAssignToEffMembershipAssn() 
        || theAttributeDef.isAssignToGroupAssn() || theAttributeDef.isAssignToImmMembershipAssn()
        || theAttributeDef.isAssignToMemberAssn() || theAttributeDef.isAssignToStemAssn()) && !StringUtils.isBlank(this.ownerAttributeAssignId)) {
      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findById(this.ownerAttributeAssignId, true);
      return attributeAssign;
    }
    throw new RuntimeException("Cannot find assign delegate for assignment and attributeDef: " 
        + this.id + ", " + theAttributeDef.getName());
    
  }
  
  /**
   * get the delegate that relates the object with the the assignment
   * @return the delegate
   */
  public AttributeAssignBaseDelegate retrieveAttributeAssignDelegate() {
    return this.retrieveAttributeAssignable().getAttributeDelegate();
  }
  
  /** delegate to manage values on this assignment */
  private AttributeAssignValueDelegate valueDelegate;
  
  /**
   * 
   * @return the value delegate
   */
  public AttributeAssignValueDelegate getValueDelegate() {
    if  (this.valueDelegate == null) {
      this.valueDelegate = new AttributeAssignValueDelegate(this);
    }
    return this.valueDelegate;
  }
  
  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeValueDelegate attributeValueDelegate;
  
  /**
   * this delegate works on attributes and values at the same time
   * @return the delegate
   */
  public AttributeValueDelegate getAttributeValueDelegate() {
    if (this.attributeValueDelegate == null) {
      this.attributeValueDelegate = new AttributeValueDelegate(this.getAttributeDelegate());
    }
    return this.attributeValueDelegate;
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AttributeAssign)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.getId(), ( (AttributeAssign) other ).getId() )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @return hashcode
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getId() )
      .toHashCode();
  } // public int hashCode()
  
  /**
   * retrieve a set of attributeDefs based on some assignments
   * @param attributeAssigns
   * @return the set of attributeDefs
   */
  public static Set<AttributeDef> retrieveAttributeDefs(Collection<AttributeAssign> attributeAssigns) {
      
    Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();

    for (AttributeAssign attributeAssign : attributeAssigns) {
      
      attributeDefs.add(attributeAssign.getAttributeDef());
      
    }
    
    return attributeDefs;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);
  
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE_ASSIGN, 
        AttributeAssignHooks.METHOD_ATTRIBUTE_ASSIGN_POST_COMMIT_DELETE, HooksAttributeAssignBean.class, 
        this, AttributeAssign.class);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN, 
        AttributeAssignHooks.METHOD_ATTRIBUTE_ASSIGN_POST_DELETE, HooksAttributeAssignBean.class, 
        this, AttributeAssign.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_POST_DELETE, false, true);
  
    GroupTypeTuple gtt = GroupTypeTuple.internal_getGroupTypeTuple(this, false);
    if (gtt != null) {
      // this is a group type tuple
      GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE_TUPLE,
          GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_COMMIT_DELETE, HooksGroupTypeTupleBean.class,
          gtt, GroupTypeTuple.class);

      GrouperHooksUtils.callHooksIfRegistered(gtt, GrouperHookType.GROUP_TYPE_TUPLE,
          GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_DELETE, HooksGroupTypeTupleBean.class,
          gtt, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_POST_DELETE, false, true);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
  
    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN, 
        AttributeAssignHooks.METHOD_ATTRIBUTE_ASSIGN_POST_INSERT, HooksAttributeAssignBean.class, 
        this, AttributeAssign.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_POST_INSERT, true, false);
  
    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE_ASSIGN, 
        AttributeAssignHooks.METHOD_ATTRIBUTE_ASSIGN_POST_COMMIT_INSERT, HooksAttributeAssignBean.class, 
        this, AttributeAssign.class);
  
    
    GroupTypeTuple gtt = GroupTypeTuple.internal_getGroupTypeTuple(this, false);
    if (gtt != null) {
      // this is a group type tuple
      
      GrouperHooksUtils.callHooksIfRegistered(gtt, GrouperHookType.GROUP_TYPE_TUPLE,
          GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_INSERT, HooksGroupTypeTupleBean.class,
          gtt, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_POST_INSERT, true, false);

      GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE_TUPLE,
          GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_COMMIT_INSERT, HooksGroupTypeTupleBean.class,
          gtt, GroupTypeTuple.class);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
    
    super.onPostUpdate(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE_ASSIGN, 
        AttributeAssignHooks.METHOD_ATTRIBUTE_ASSIGN_POST_COMMIT_UPDATE, HooksAttributeAssignBean.class, 
        this, AttributeAssign.class);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN, 
        AttributeAssignHooks.METHOD_ATTRIBUTE_ASSIGN_POST_UPDATE, HooksAttributeAssignBean.class, 
        this, AttributeAssign.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_POST_UPDATE, true, false);
  
  
    GroupTypeTuple gtt = GroupTypeTuple.internal_getGroupTypeTuple(this, false);
    if (gtt != null) {
      // this is a group type tuple
      
      GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE_TUPLE,
          GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_COMMIT_UPDATE, HooksGroupTypeTupleBean.class,
          gtt, GroupTypeTuple.class);

      GrouperHooksUtils.callHooksIfRegistered(gtt, GrouperHookType.GROUP_TYPE_TUPLE,
          GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_UPDATE, HooksGroupTypeTupleBean.class,
          gtt, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_POST_UPDATE, true, false);

    }
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @SuppressWarnings("deprecation")
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN, 
        AttributeAssignHooks.METHOD_ATTRIBUTE_ASSIGN_PRE_DELETE, HooksAttributeAssignBean.class, 
        this, AttributeAssign.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_PRE_DELETE, false, false);
    
    GroupTypeTuple gtt = GroupTypeTuple.internal_getGroupTypeTuple(this, false);
    if (gtt != null) {
      // this is a group type tuple
      GrouperHooksUtils.callHooksIfRegistered(gtt, GrouperHookType.GROUP_TYPE_TUPLE,
          GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_PRE_DELETE, HooksGroupTypeTupleBean.class,
          gtt, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_PRE_DELETE, false, false);
    }
    
    String ownerId1 = null;
    String ownerId2 = null;
    
    AttributeAssignType ownerType = this.getAttributeAssignType();
    if (AttributeAssignType.group == ownerType) {
      ownerId1 = this.getOwnerGroupId();
    } else if (AttributeAssignType.stem == ownerType) {
      ownerId1 = this.getOwnerStemId();
    } else if (AttributeAssignType.member == ownerType) {
      ownerId1 = this.getOwnerMemberId();
    } else if (AttributeAssignType.attr_def == ownerType) {
      ownerId1 = this.getOwnerAttributeDefId();
    } else if (AttributeAssignType.any_mem == ownerType) {
      ownerId1 = this.getOwnerGroupId();
      ownerId2 = this.getOwnerMemberId();
    } else if (AttributeAssignType.imm_mem == ownerType) {
      ownerId1 = this.getOwnerMembershipId();
    } else if (this.getOwnerAttributeAssignId() != null) {
      ownerId1 = this.getOwnerAttributeAssignId();
    } else {
      throw new RuntimeException("Unexpected ownerType: " + ownerType);
    }
    
    // may need to delete group sets if we're unassigning a group type.  also change log entry.
    if (gtt != null) {
      // ok this is really a group type unassignment
      
      Group group = gtt.retrieveGroup(true);
      GroupType groupType = GroupTypeFinder.findByUuid(gtt.getTypeUuid(), true);
      Set<Field> fields = FieldFinder.findAllByGroupType(groupType);
      for (Field field : fields) {
        GrouperDAOFactory.getFactory().getGroupSet().deleteSelfByOwnerGroupAndField(group.getUuid(), field.getUuid());
      }
      
      //change log into temp table
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_TYPE_UNASSIGN,
          ChangeLogLabels.GROUP_TYPE_UNASSIGN.id.name(), this.getId(),
          ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupId.name(), group.getUuid(),
          ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupName.name(), group.getName(),
          ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeId.name(), groupType.getUuid(),
          ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeName.name(), groupType.getName()).save();
    }
    
    if (this.dbVersion().isEnabled()) {
      //change log into temp table
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE, 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.id.name(), this.getId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameId.name(), this.getAttributeDefNameId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeAssignActionId.name(), this.getAttributeAssignActionId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.assignType.name(), this.getAttributeAssignTypeDb(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId1.name(), ownerId1,
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId2.name(), ownerId2,
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameName.name(), this.getAttributeDefName().getName(),
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.action.name(), this.getAttributeAssignAction().getName(),
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.disallowed.name(), this.getDisallowedDb()).save();
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @SuppressWarnings("deprecation")
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    long now = System.currentTimeMillis();
    this.setCreatedOnDb(now);
    this.setLastUpdatedDb(now);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN, 
        AttributeAssignHooks.METHOD_ATTRIBUTE_ASSIGN_PRE_INSERT, HooksAttributeAssignBean.class, 
        this, AttributeAssign.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_PRE_INSERT, false, false);
    
    GroupTypeTuple gtt = GroupTypeTuple.internal_getGroupTypeTuple(this, false);
    if (gtt != null) {
      // this is a group type tuple
      GrouperHooksUtils.callHooksIfRegistered(gtt, GrouperHookType.GROUP_TYPE_TUPLE,
          GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_PRE_INSERT, HooksGroupTypeTupleBean.class,
          gtt, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_PRE_INSERT, false, false);
    }
    
    String ownerId1 = null;
    String ownerId2 = null;
    
    AttributeAssignType ownerType = this.getAttributeAssignType();
    if (AttributeAssignType.group == ownerType) {
      ownerId1 = this.getOwnerGroupId();
    } else if (AttributeAssignType.stem == ownerType) {
      ownerId1 = this.getOwnerStemId();
    } else if (AttributeAssignType.member == ownerType) {
      ownerId1 = this.getOwnerMemberId();
    } else if (AttributeAssignType.attr_def == ownerType) {
      ownerId1 = this.getOwnerAttributeDefId();
    } else if (AttributeAssignType.any_mem == ownerType) {
      ownerId1 = this.getOwnerGroupId();
      ownerId2 = this.getOwnerMemberId();
    } else if (AttributeAssignType.imm_mem == ownerType) {
      ownerId1 = this.getOwnerMembershipId();
    } else if (this.getOwnerAttributeAssignId() != null) {
      ownerId1 = this.getOwnerAttributeAssignId();
    } else {
      throw new RuntimeException("Unexpected ownerType: " + ownerType);
    }
    
    // may need to add group sets if we're assigning a group type.  also change log entry
    if (gtt != null) {
      // ok this is really a group type assignment
      Group group = gtt.retrieveGroup(true);
      GroupType groupType = GroupTypeFinder.findByUuid(gtt.getTypeUuid(), true);
    
      Set<Field> fields = FieldFinder.findAllByGroupType(groupType);
      for (Field field : fields) {
        if (group.getTypeOfGroup() != null && group.getTypeOfGroup().supportsField(field)) {
          GroupSet groupSet = new GroupSet();
          groupSet.setId(GrouperUuid.getUuid());
          groupSet.setCreatorId(GrouperSession.staticGrouperSession().getMemberUuid());
          groupSet.setDepth(0);
          groupSet.setMemberGroupId(group.getUuid());
          groupSet.setOwnerGroupId(group.getUuid());
          groupSet.setParentId(groupSet.getId());
          groupSet.setFieldId(field.getUuid());
          GrouperDAOFactory.getFactory().getGroupSet().save(groupSet);
        }
      }
      
      //change log into temp table
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN,
          ChangeLogLabels.GROUP_TYPE_ASSIGN.id.name(), this.getId(),
          ChangeLogLabels.GROUP_TYPE_ASSIGN.groupId.name(), group.getUuid(),
          ChangeLogLabels.GROUP_TYPE_ASSIGN.groupName.name(), group.getName(),
          ChangeLogLabels.GROUP_TYPE_ASSIGN.typeId.name(), groupType.getUuid(),
          ChangeLogLabels.GROUP_TYPE_ASSIGN.typeName.name(), groupType.getName()).save();
    }
    
    if (this.isEnabled()) {
      //change log into temp table
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD, 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id.name(), this.getId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId.name(), this.getAttributeDefNameId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId.name(), this.getAttributeAssignActionId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType.name(), this.getAttributeAssignTypeDb(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1.name(), ownerId1,
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2.name(), ownerId2,
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName.name(), this.getAttributeDefName().getName(),
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.action.name(), this.getAttributeAssignAction().getName(),
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.disallowed.name(), this.getDisallowedDb()).save();
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    this.setLastUpdatedDb(System.currentTimeMillis());
    
    if (this.dbVersionDifferentFields().contains(FIELD_ATTRIBUTE_ASSIGN_ACTION_ID)) {
      throw new RuntimeException("cannot update attributeAssignActionId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_ATTRIBUTE_DEF_NAME_ID)) {
      throw new RuntimeException("cannot update attributeDefNameId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_ATTRIBUTE_ASSIGN_TYPE)) {
      throw new RuntimeException("cannot update attributeAssignType");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_DISALLOWED)) {
      throw new RuntimeException("cannot update disallowed");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_OWNER_STEM_ID) ||
        this.dbVersionDifferentFields().contains(FIELD_OWNER_GROUP_ID) ||
        this.dbVersionDifferentFields().contains(FIELD_OWNER_ATTRIBUTE_DEF_ID) ||
        this.dbVersionDifferentFields().contains(FIELD_OWNER_ATTRIBUTE_ASSIGN_ID) ||
        this.dbVersionDifferentFields().contains(FIELD_OWNER_MEMBER_ID) ||
        this.dbVersionDifferentFields().contains(FIELD_OWNER_MEMBERSHIP_ID)) {
      throw new RuntimeException("cannot update owner columns");
    }

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_ASSIGN, 
        AttributeAssignHooks.METHOD_ATTRIBUTE_ASSIGN_PRE_UPDATE, HooksAttributeAssignBean.class, 
        this, AttributeAssign.class, VetoTypeGrouper.ATTRIBUTE_ASSIGN_PRE_UPDATE, false, false);
  
    GroupTypeTuple gtt = GroupTypeTuple.internal_getGroupTypeTuple(this, false);
    if (gtt != null) {
      // this is a group type tuple
      GrouperHooksUtils.callHooksIfRegistered(gtt, GrouperHookType.GROUP_TYPE_TUPLE,
          GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_PRE_UPDATE, HooksGroupTypeTupleBean.class,
          gtt, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_PRE_UPDATE, false, false);  
    }
    
    String ownerId1 = null;
    String ownerId2 = null;
    
    AttributeAssignType ownerType = this.getAttributeAssignType();
    if (AttributeAssignType.group == ownerType) {
      ownerId1 = this.getOwnerGroupId();
    } else if (AttributeAssignType.stem == ownerType) {
      ownerId1 = this.getOwnerStemId();
    } else if (AttributeAssignType.member == ownerType) {
      ownerId1 = this.getOwnerMemberId();
    } else if (AttributeAssignType.attr_def == ownerType) {
      ownerId1 = this.getOwnerAttributeDefId();
    } else if (AttributeAssignType.any_mem == ownerType) {
      ownerId1 = this.getOwnerGroupId();
      ownerId2 = this.getOwnerMemberId();
    } else if (AttributeAssignType.imm_mem == ownerType) {
      ownerId1 = this.getOwnerMembershipId();
    } else if (this.getOwnerAttributeAssignId() != null) {
      ownerId1 = this.getOwnerAttributeAssignId();
    } else {
      throw new RuntimeException("Unexpected ownerType: " + ownerType);
    }
    
    
    if (this.isEnabled() && !this.dbVersion().isEnabled()) {
      // this is an add
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD, 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id.name(), this.getId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId.name(), this.getAttributeDefNameId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId.name(), this.getAttributeAssignActionId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType.name(), this.getAttributeAssignTypeDb(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1.name(), ownerId1,
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2.name(), ownerId2,
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName.name(), this.getAttributeDefName().getName(),
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.action.name(), this.getAttributeAssignAction().getName(),
          ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.disallowed.name(), this.getDisallowedDb()).save();
    } else if (!this.isEnabled() && this.dbVersion().isEnabled()) {
      // this is a delete
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE, 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.id.name(), this.getId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameId.name(), this.getAttributeDefNameId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeAssignActionId.name(), this.getAttributeAssignActionId(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.assignType.name(), this.getAttributeAssignTypeDb(), 
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId1.name(), ownerId1,
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId2.name(), ownerId2,
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameName.name(), this.getAttributeDefName().getName(),
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.action.name(), this.getAttributeAssignAction().getName(),
          ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.disallowed.name(), this.getDisallowedDb()).save();
    }
  }

  /**
   * if it is possible to get a single ownerid (i.e. not any_mem), then do that here
   * @return the single owner id
   */
  public String getOwnerSingleId() {
    String ownerId = null;
    AttributeAssignType ownerType = this.getAttributeAssignType();
    if (AttributeAssignType.group == ownerType) {
      ownerId = this.getOwnerGroupId();
    } else if (AttributeAssignType.stem == ownerType) {
      ownerId = this.getOwnerStemId();
    } else if (AttributeAssignType.member == ownerType) {
      ownerId = this.getOwnerMemberId();
    } else if (AttributeAssignType.attr_def == ownerType) {
      ownerId = this.getOwnerAttributeDefId();
    } else if (AttributeAssignType.any_mem == ownerType) {
      throw new RuntimeException("Cant get single owner id from any_mem... " + this.getOwnerGroupId() + ", " + this.getOwnerMemberId());
    } else if (AttributeAssignType.imm_mem == ownerType) {
      ownerId = this.getOwnerMembershipId();
    } else if (this.getOwnerAttributeAssignId() != null) {
      ownerId = this.getOwnerAttributeAssignId();
    } else {
      throw new RuntimeException("Unexpected ownerType: " + ownerType);
    }
    return ownerId;
  }
  
  /**
   * e.g. if enabled or disabled is switching, delete this attribute assignment (and child objects)
   * and recommit it (which will not have the child objects or will have this time)
   */
  public void deleteAndStore() {
    //TODO add auditing, maybe try to maintain context id, or create a new one
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            //make sure it is still there, it could be gone since deleted from a parent...
            AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign()
              .findById(AttributeAssign.this.getId(), false);
            
            if (attributeAssign != null) {
              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
              
              AttributeAssign.this.delete();
              //insert this again
              AttributeAssign.this.setHibernateVersionNumber(GrouperAPI.INITIAL_VERSION_NUMBER);
              GrouperDAOFactory.getFactory().getAttributeAssign().saveOrUpdate(AttributeAssign.this);
              AttributeAssign.this.dbVersionReset();            
            }
            return null;
          }
        });
  }
  

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public AttributeAssign dbVersion() {
    return (AttributeAssign)this.dbVersion;
  }
  
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);
    return result;
  }

  /**
   * fix enabled and disabled memberships, and return the count of how many were fixed
   * @return the number of records affected
   */
  public static int internal_fixEnabledDisabled() {
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAllEnabledDisabledMismatch();
    for (AttributeAssign attributeAssign : attributeAssigns) {
      attributeAssign.deleteAndStore();
    }
    return attributeAssigns.size();
  }

}
