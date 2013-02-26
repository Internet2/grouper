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
package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * built in if condition
 * @author mchyzer
 *
 */
public enum RuleThenEnum {

  /** assign a disabled date if there is a permission assignment to the owner attribute def
   */
  assignDisabledDaysToOwnerPermissionDefAssignments {
    /**
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {

      String days = ruleDefinition.getThen().getThenEnumArg0();
      int daysInteger = GrouperUtil.intValue(days);

      String ownerAttributeDefId = ruleDefinition.getAttributeAssignType().getOwnerAttributeDefId();
      
      return RuleElUtils.assignPermissionDisabledDaysForAttributeDefId(ownerAttributeDefId, rulesBean.getMemberId(), daysInteger);
    }
    
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg1())) {
        return "ruleThenEnumArg1 should not be entered for this ruleThenEnum: " + this.name();
      }

      String days = ruleDefinition.getThen().getThenEnumArg0();

      try {
        GrouperUtil.intValue(days);
      } catch (Exception e) {
        return "ruleThenEnumArg0 should be the number of days in the future that the disabled date should be set: " + e.getMessage();
      }
      
      return null;
      
    }

  },
  
  /** assign a disabled date if there is a membership in this group to the owner group
   * ${ruleElUtils.assignMembershipDisabledDaysForGroupId(ownerGroupId, memberId, 7)}
   */
  assignMembershipDisabledDaysForOwnerGroupId {
    
    /**
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {

      String days = ruleDefinition.getThen().getThenEnumArg0();
      int daysInteger = GrouperUtil.intValue(days);

      String addIfNotThere = ruleDefinition.getThen().getThenEnumArg1();
      boolean addIfNotThereBoolean = GrouperUtil.booleanValue(addIfNotThere);
      
      String ownerGroupId = ruleDefinition.getAttributeAssignType().getOwnerGroupId();
      return RuleElUtils.assignMembershipDisabledDaysForGroupId(ownerGroupId, rulesBean.getMemberId(), daysInteger, addIfNotThereBoolean);
    }
    
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      String days = ruleDefinition.getThen().getThenEnumArg0();

      try {
        GrouperUtil.intValue(days);
      } catch (Exception e) {
        return "ruleThenEnumArg0 should be the number of days in the future that the disabled date should be set: " + e.getMessage();
      }
      
      String addIfNotThere = ruleDefinition.getThen().getThenEnumArg1();
      
      try {
        GrouperUtil.booleanValue(addIfNotThere);
      } catch (Exception e) {
        return "ruleThenEnumArg1 should be T or F for if the membership in the owner group should be created if not there: " + e.getMessage();
      }

      return null;
    }
    
  },
  
  /** veto the operation (note, must be a transactional check for this to work) */
  veto {

    /**
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {

      //${ruleElUtils.veto('rule.entity.must.be.a.member.of.stem.b', 'Entity cannot be a member of stem:a if not a member of stem:b')}
      String key = ruleDefinition.getThen().getThenEnumArg0();
      String message = ruleDefinition.getThen().getThenEnumArg1();
      throw new RuleVeto(key, message);
    }
    
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      String key = ruleDefinition.getThen().getThenEnumArg0();
      String message = ruleDefinition.getThen().getThenEnumArg1();
      
      if (StringUtils.isBlank(key)) {
        return "ruleThenEnumArg0 is the message key in the UI and is required, e.g. some.key.for.ui.messages.file";
      }
      
      if (StringUtils.isBlank(message)) {
        return "ruleThenEnumArg1 is the error message";
      }
      return null;
    }
  },
  
  /** remove the member (the current one being acted on) from the roles and assignments associated with 
   * the owner attribute definition */
  removeMemberFromOwnerPermissionDefAssignments {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {
      
      Set<PermissionEntry> permissionEntries = RuleUtils.permissionsForUser(ruleDefinition
          .getAttributeAssignType().getOwnerAttributeDefId(), rulesBean, false);
      
      RuntimeException runtimeException = null;
      
      Member member = null;
      boolean result = false;
      
      //first remove individual assignments
      for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {
        if (permissionEntry.isImmediatePermission() && permissionEntry.getPermissionType() == PermissionType.role_subject) {
          
          try {
            Role role = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), permissionEntry.getRoleId(), true);
            
            //get this once
            if (member == null) {
              member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), permissionEntry.getMemberId(), true);
            }
            
            AttributeDefName attributeDefName = AttributeDefNameFinder.findById(permissionEntry.getAttributeDefNameId(), true);
            
            role.getPermissionRoleDelegate().removeSubjectRolePermission(attributeDefName, member);
            
            result = true;
            
          } catch (RuntimeException re) {
            if (runtimeException == null) {
              runtimeException = re;
            }
            LOG.error("error removing permission assignments: " + permissionEntry, re);
          }
        }
      }
      
      Set<String> roleIdsRemoved = new HashSet<String>();
      
      //then remove immediate role assignment
      for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {

        //CH: 20110707: changed from immediate membership to immediate permission
        if (permissionEntry.getPermissionType() == PermissionType.role) {
          
          try {
            String roleId = permissionEntry.getRoleId();
            
            if (roleIdsRemoved.contains(roleId)) {
              continue;
            }
            
            Role role = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), roleId, true);
            
            //get this once
            if (member == null) {
              member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), permissionEntry.getMemberId(), true);
            }
            
            if (role.deleteMember(member.getSubject(), false)) {
              result = true;
            }
            
            //dont try again
            roleIdsRemoved.add(roleId);
            
          } catch (RuntimeException re) {
            if (runtimeException == null) {
              runtimeException = re;
            }
            LOG.error("error removing role assignments: " + permissionEntry, re);
          }
        }
      }
      
      if (runtimeException != null) {
        throw runtimeException;
      }
      return result;
    }
    
  },
  
  /** remove the member (the current one being acted on) from the owner group */
  removeMemberFromOwnerGroup {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {
      
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), 
          ruleDefinition.getAttributeAssignType().getOwnerGroupId(), true);
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), rulesBean.getMemberId(), true);
      return group.deleteMember(member, false);
    }
    
  }, 

  reassignGroupPrivilegesIfFromGroup {

    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg1())) {
        return "ruleThenEnumArg1 should not be entered for this ruleThenEnum: " + this.name();
      }

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg1())) {
        return "ruleThenEnumArg0 should not be entered for this ruleThenEnum: " + this.name();
      }

      return null;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {

      Group group = rulesBean.getGroup();
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("reassignGroupPrivilegesIfFromGroup: from group: " + group);
      }
      
      //get the subject that did this
      Subject subjectUnderlyingSession = rulesBean.getSubjectUnderlyingSession();
      
      //this shouldnt ever be null
      if (subjectUnderlyingSession == null) {
        throw new NullPointerException("Why is there no subject in grouper session???");
      }

      //get the stem of the parent of this group
      Stem stem = group.getParentStem();
      
      Set<Subject> creators = stem.getCreators();
      
      Set<Subject> creatorsAreNonWheelGroups = new HashSet<Subject>();

      Group wheelGroup = null;
      boolean calculatedWheelGroup = false;
      
      //lets see which ones are groups
      for (Subject creator : GrouperUtil.nonNull(creators)) {
        
        if (!StringUtils.equals("g:gsa", creator.getSourceId())) {
          continue;
        }
        
        //ok, we have a group, is the session user a member of the group?
        Group creatorGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), creator.getId(), false);
        if (!creatorGroup.hasMember(subjectUnderlyingSession)) {
          continue;
        }
        
        //lets see if this is the wheel group
        if (!calculatedWheelGroup) {
          if (GrouperConfig.getPropertyBoolean(GrouperConfig.PROP_USE_WHEEL_GROUP, false)) {
            String wheelGroupName = GrouperConfig.getProperty( GrouperConfig.PROP_WHEEL_GROUP );
            if (!StringUtils.isBlank(wheelGroupName)) {
              wheelGroup = GroupFinder.findByName( GrouperSession.staticGrouperSession(), wheelGroupName, true );
            }
          }
          calculatedWheelGroup = true;
        }
        
        //if wheel
        if (wheelGroup != null) {

          //dont worry about wheel groups
          if (StringUtils.equals(wheelGroup.getId(), creator.getId())) {
            continue;
          }
          
          //dont worry if group is a member of the wheel gropu
          if (wheelGroup.hasMember(creator)) {
            continue;
          }
          
        }
        
        //ok, we have a group to do this with
        creatorsAreNonWheelGroups.add(creator);
        
      }
      
      boolean result = false;
      
      //if we found a group to use, or if user is wheel or root
      if (creatorsAreNonWheelGroups.size() > 0 || PrivilegeHelper.isWheelOrRoot(subjectUnderlyingSession)) {
        
        //unassign the subject as admin
        group.revokePriv(subjectUnderlyingSession, AccessPrivilege.ADMIN, false);
        
        result = true;
        
      }
      
      //assign admin for the groups which have create
      for (Subject creatorNonWheelGroup : creatorsAreNonWheelGroups) {
        group.grantPriv(creatorNonWheelGroup, AccessPrivilege.ADMIN, false);
      }
      
      return result;
    }

  },
  
  /** assign privilege(s) to subject on the group being acted on (groupId) */
  assignGroupPrivilegeToGroupId {
  
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      String privileges = ruleDefinition.getThen().getThenEnumArg1();
      
      if (StringUtils.isBlank(subjectString)) {
        return "ruleThenEnumArg0 is the subject string to assign to and is required, e.g. g:gsa::::::someFolder:someGroup";
      }
      
      if (StringUtils.isBlank(privileges)) {
        return "ruleThenEnumArg1 are the access privileges, e.g. read,update,admin";
      }
      
      try {
        SubjectFinder.findByPackedSubjectString(subjectString, true);
      } catch (Exception e) {
        return e.getMessage();
      }

      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
      for (String privilegeString : privilegesSet) {
        Privilege privilege = null;
        
        try {
          privilege = Privilege.getInstance(privilegeString);
        } catch (Exception e) {
          return e.getMessage();
        }
        if (!Privilege.isAccess(privilege)) {
          return "Privilege '" + privilegeString + "' must be an access privilege, e.g. admin, read, update, view, optin, optout";
        }
      }
      return null;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {

      Group group = rulesBean.getGroup();
      
      Subject subject = SubjectFinder.findByPackedSubjectString(ruleDefinition.getThen().getThenEnumArg0(), true);

      String privileges = ruleDefinition.getThen().getThenEnumArg1();

      if (LOG.isDebugEnabled()) {
        LOG.debug("assignGroupPrivilege: from group: " + group 
            + ", subject: " + GrouperUtil.subjectToString(subject) 
            + " privilegeNamesCommaSeparated: " + privileges);
      }

      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");

      boolean result = false;

      for (String privilegeString : privilegesSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        if (group.grantPriv(subject, privilege, true)) {
          result = true;
        }
      }
      
      return result;
    }
    
  }, 
  
  /** assign privilege(s) to subject on the stem being acted on (stemId) */
  assignStemPrivilegeToStemId{
  
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      String privileges = ruleDefinition.getThen().getThenEnumArg1();
      
      if (StringUtils.isBlank(subjectString)) {
        return "ruleThenEnumArg0 is the subject string to assign to and is required, e.g. g:gsa::::::someFolder:someGroup";
      }
      
      if (StringUtils.isBlank(privileges)) {
        return "ruleThenEnumArg1 are the naming privileges, e.g. stem,create";
      }
      
      try {
        SubjectFinder.findByPackedSubjectString(subjectString, true);
      } catch (Exception e) {
        return e.getMessage();
      }
  
      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
      for (String privilegeString : privilegesSet) {
        Privilege privilege = null;
        
        try {
          privilege = Privilege.getInstance(privilegeString);
        } catch (Exception e) {
          return e.getMessage();
        }
        if (!Privilege.isNaming(privilege)) {
          return "Privilege '" + privilegeString + "' must be a naming privilege, e.g. stem, create";
        }
      }
      return null;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {
  
      Stem stem = rulesBean.getStem();
      
      Subject subject = SubjectFinder.findByPackedSubjectString(ruleDefinition.getThen().getThenEnumArg0(), true);
  
      String privileges = ruleDefinition.getThen().getThenEnumArg1();
  
      if (LOG.isDebugEnabled()) {
        LOG.debug("assignStemPrivilege: from stem: " + stem 
            + ", subject: " + GrouperUtil.subjectToString(subject) 
            + " privilegeNamesCommaSeparated: " + privileges);
      }
  
      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
  
      boolean result = false;
  
      for (String privilegeString : privilegesSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        if (stem.grantPriv(subject, privilege, true)) {
          result = true;
        }
      }
      
      return result;
    }
    
  }, 
  
  /** assign privilege(s) to subject on the attributeDef being acted on (attributeDefId) */
  assignAttributeDefPrivilegeToAttributeDefId {
  
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      String privileges = ruleDefinition.getThen().getThenEnumArg1();
      
      if (StringUtils.isBlank(subjectString)) {
        return "ruleThenEnumArg0 is the subject string to assign to and is required, e.g. g:gsa::::::someFolder:someGroup";
      }
      
      if (StringUtils.isBlank(privileges)) {
        return "ruleThenEnumArg1 are the attrDef privileges, e.g. attrRead,attrUpdate,attrAdmin";
      }
      
      try {
        SubjectFinder.findByPackedSubjectString(subjectString, true);
      } catch (Exception e) {
        return e.getMessage();
      }
  
      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
      for (String privilegeString : privilegesSet) {
        Privilege privilege = null;
        
        try {
          privilege = Privilege.getInstance(privilegeString);
        } catch (Exception e) {
          return e.getMessage();
        }
        if (!Privilege.isAttributeDef(privilege)) {
          return "Privilege '" + privilegeString + "' must be an attributeDef privilege, e.g. attrAdmin, attrRead, attrUpdate, attrView, attrOptin, attrOptout";
        }
      }
      return null;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {
  
      AttributeDef attributeDef = rulesBean.getAttributeDef();
      
      Subject subject = SubjectFinder.findByPackedSubjectString(ruleDefinition.getThen().getThenEnumArg0(), true);
  
      String privileges = ruleDefinition.getThen().getThenEnumArg1();

      if (LOG.isDebugEnabled()) {
        LOG.debug("assignAttributeDefPrivilege: from attributeDef: " + attributeDef 
            + ", subject: " + GrouperUtil.subjectToString(subject) 
            + " privilegeNamesCommaSeparated: " + privileges);
      }

      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
  
      boolean result = false;
  
      for (String privilegeString : privilegesSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        if (attributeDef.getPrivilegeDelegate().grantPriv(subject, privilege, true)) {
          result = true;
        }
      }
      
      return result;
    }
    
  }, 
  
  /** <pre>
   * send an email about this action.
   * arg0: comma separated email addresses to send to.  ${subjectEmail} is a variable which evaluates to the email of the subject (if applicable)
   * arg1: subject (some text/EL), or template: templateName 
   * arg2: body (some text/EL), or template: templateName
   * The template name comes from the directory in grouper.properties: rules.emailTemplatesFolder
   * </pre>
   */
  sendEmail {
  
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {
      String toAddressesString = ruleDefinition.getThen().getThenEnumArg0();
      String subjectString = ruleDefinition.getThen().getThenEnumArg1();
      String bodyString = ruleDefinition.getThen().getThenEnumArg2();
      
      if (StringUtils.isBlank(toAddressesString)) {
        return "sendEmail ruleThenEnum requires ruleThenArg0 to be the comma separated addresses to send the email to";
      }
        
      if (StringUtils.isBlank(subjectString)) {
        return "sendEmail ruleThenEnum requires ruleThenArg1 to be the subject EL script or template: templateName ";
      }
        
      if (StringUtils.isBlank(bodyString)) {
        return "sendEmail ruleThenEnum requires ruleThenArg2 to be the body EL script or template: templateName ";
      }

      //see if these are templated, and if so, see if they exist and stuff
      try {
        RuleUtils.emailTemplate(subjectString);
        RuleUtils.emailTemplate(bodyString);
      } catch (Exception e) {
        return e.getMessage();
      }
      
      
      return null;
    }
    

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {

      String toAddressesString = ruleDefinition.getThen().getThenEnumArg0();
      
      
      String subjectString = ruleDefinition.getThen().getThenEnumArg1();
      String bodyString = ruleDefinition.getThen().getThenEnumArg2();
      
      String subjectTemplate = RuleUtils.emailTemplate(subjectString);
      String bodyTemplate = RuleUtils.emailTemplate(bodyString);
      
      Map<String, Object> variableMap =  new HashMap<String, Object>();

      Subject actAsSubject = ruleDefinition.getActAs().subject(true);
      boolean hasAccessToEl = RuleEngine.hasAccessToElApi(actAsSubject);

      ruleDefinition.addElVariables(variableMap, rulesBean, hasAccessToEl);
      
      if (logDataForThisDefinition != null) {
        logDataForThisDefinition.append(", EL variables: ");
        for (String varName : variableMap.keySet()) {
          logDataForThisDefinition.append(varName);
          Object value = variableMap.get(varName);
          if (value instanceof String) {
            logDataForThisDefinition.append("(").append(value).append(")");
          }
          logDataForThisDefinition.append(",");
        }
      }
      
      toAddressesString = GrouperUtil.substituteExpressionLanguage(toAddressesString, variableMap);

      String subject = GrouperUtil.substituteExpressionLanguage(subjectTemplate, variableMap);
      
      String body = GrouperUtil.substituteExpressionLanguage(bodyTemplate, variableMap);

      if (logDataForThisDefinition != null) {
        logDataForThisDefinition.append(", toAddressesString: ").append(toAddressesString);
        logDataForThisDefinition.append(", subject: ").append(subject);
        logDataForThisDefinition.append(", body: ").append(body);
      }
      
      new GrouperEmail().setTo(toAddressesString).setBody(body).setSubject(subject).send();
      
      return true;
    }
    
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static RuleThenEnum valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(RuleThenEnum.class, 
        string, exceptionOnNull);

  }

  /**
   * fire this rule
   * @param ruleDefinition
   * @param ruleEngine
   * @param rulesBean
   * @param logDataForThisDefinition is null if not logging, and non null if things should be appended
   * @return something for log
   */
  public abstract Object fireRule(RuleDefinition ruleDefinition, 
      RuleEngine ruleEngine, RulesBean rulesBean, StringBuilder logDataForThisDefinition);
  
  /**
   * validate the rule
   * @param ruleDefinition
   * @return the validation reason
   */
  public String validate(RuleDefinition ruleDefinition) {
    if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg0()) 
        || !StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg1())) {
      return "This ruleThenEnum does not take any arguments: " + this;
    }
    return null;
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleThenEnum.class);

}
