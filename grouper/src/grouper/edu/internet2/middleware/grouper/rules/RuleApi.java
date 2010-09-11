package edu.internet2.middleware.grouper.rules;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * helper methods to assign rules to objects without having to deal with attributes
 * note, you can use this from gsh too
 * @author mchyzer
 */
public class RuleApi {

  /**
   * 
   * @param actAs
   * @param ruleGroup
   * @param mustBeInGroupInFolder
   * @param stemScope 
   * @param vetoKey
   * @param vetoMessage
   * @return the assignment in case there are edits
   */
  public static AttributeAssign vetoMembershipIfNotInGroupInFolder(Subject actAs, Group ruleGroup, 
      Stem mustBeInGroupInFolder, Stem.Scope stemScope, String vetoKey, String vetoMessage) {
    
    //add a rule on stem:a saying if not in a folder in stem:b, then dont allow add to stem:a

    //add a rule on stem:a saying if not in stem:b, then dont allow add to stem:a
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.noGroupInFolderHasImmediateEnabledMembership.name());
    
    //org folder
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfOwnerIdName(), mustBeInGroupInFolder.getUuid());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfStemScopeName(), stemScope.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    
    //key which would be used in UI messages file if applicable
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), vetoKey);
    
    //error message (if key in UI messages file not there)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), vetoMessage);
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
    
  }
  
  /**
   * 
   * @param actAs
   * @param ruleGroup
   * @param mustBeInGroup
   * @param vetoKey
   * @param vetoMessage
   * @return the assignment in case there are edits
   */
  public static AttributeAssign vetoMembershipIfNotInGroup(Subject actAs, 
      Group ruleGroup, Group mustBeInGroup, String vetoKey, String vetoMessage) {
    //add a rule on stem:a saying if not in stem:b, then dont allow add to stem:a
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.groupHasNoImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfOwnerIdName(), mustBeInGroup.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    
    //key which would be used in UI messages file if applicable
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), vetoKey);
    
    //error message (if key in UI messages file not there)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), vetoMessage);

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;

  }

  /**
   * make sure stem privileges are inherited in a attributeDef
   * @param actAs
   * @param stem
   * @param stemScope ONE or SUB
   * @param subjectToAssign
   * @param privileges can use Privilege.getInstances() to convert from string
   * @return the assignment in case there are edits
   */
  public static AttributeAssign inheritAttributeDefPrivileges(Subject actAs, Stem stem, Scope stemScope, 
      Subject subjectToAssign, Set<Privilege> privileges) {
    

    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.attributeDefCreate.name());

    //can be SUB or ONE for if in this folder, or in this and all subfolders
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), stemScope.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId.name());

    //this is the subject string for the subject to assign to
    //e.g. sourceId :::::: subjectIdentifier
    //or sourceId :::: subjectId
    //or :::: subjectId
    //or sourceId ::::::: subjectIdOrIdentifier
    //etc
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), subjectToAssign.getSourceId()+ " :::: " + subjectToAssign.getId());

    //can be: attrRead, attrUpdate, attrView, attrAdmin, attrOptin, attrOptout
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), Privilege.stringValue(privileges));
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
  }
  
  /**
   * make sure stem privileges are inherited in a stem
   * @param actAs
   * @param stem
   * @param stemScope ONE or SUB
   * @param subjectToAssign
   * @param privileges can use Privilege.getInstances() to convert from string
   * @return the assignment in case there are edits
   */
  public static AttributeAssign inheritFolderPrivileges(Subject actAs, Stem stem, Scope stemScope, 
      Subject subjectToAssign, Set<Privilege> privileges) {
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.stemCreate.name());
    
    //can be SUB or ONE for if should be in all descendants or just on children
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), stemScope.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignStemPrivilegeToStemId.name());
    
    //this is the subject string for the subject to assign to
    //e.g. sourceId :::::: subjectIdentifier
    //or sourceId :::: subjectId
    //or :::: subjectId
    //or sourceId ::::::: subjectIdOrIdentifier
    //etc
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), subjectToAssign.getSourceId() + " :::: " + subjectToAssign.getId());
    
    //possible privileges are stem and create
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), Privilege.stringValue(privileges));
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
  }

  
  /**
   * make sure group privileges are inherited in a stem
   * @param actAs
   * @param stem
   * @param stemScope ONE or SUB
   * @param subjectToAssign
   * @param privileges can use Privilege.getInstances() to convert from string
   * @return the assignment in case there are edits
   */
  public static AttributeAssign inheritGroupPrivileges(Subject actAs, Stem stem, Scope stemScope, 
      Subject subjectToAssign, Set<Privilege> privileges) {
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader and updater group
    AttributeAssign attributeAssign = stem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.groupCreate.name());
    
    //can be SUB or ONE for if in this folder, or in this and all subfolders
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), stemScope.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignGroupPrivilegeToGroupId.name());
    
    //this is the subject string for the subject to assign to
    //e.g. sourceId :::::: subjectIdentifier
    //or sourceId :::: subjectId
    //or :::: subjectId
    //or sourceId ::::::: subjectIdOrIdentifier
    //etc
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), subjectToAssign.getSourceId() + " :::: " + subjectToAssign.getId());
    
    //privileges to assign: read, admin, update, view, optin, optout
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), Privilege.stringValue(privileges));
    
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;

  }

  /**
   * if a member is removed from a folder, and has no more memberships in any group in the folder, then
   * remove from the group
   * @param actAs
   * @param ruleGroup
   * @param folder
   * @param stemScope
   * @return the assignment in case there are edits
   */
  public static AttributeAssign groupIntersectionWithFolder(Subject actAs, 
      Group ruleGroup, Stem folder, Stem.Scope stemScope) {
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    
    //folder where membership was removed
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerIdName(), folder.getUuid());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemoveInFolder.name());

    //SUB for all descendants, ONE for just children
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        stemScope.name());
    
    //if there is no more membership in the folder, and there is a membership in the group
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupAndNotFolderHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(),
        RuleThenEnum.removeMemberFromOwnerGroup.name());

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;

  }
  
  /**
   * put a rule on the rule group which says that if the user is not in the mustBeInGroup, 
   * then remove from ruleGroup
   * @param actAs
   * @param ruleGroup
   * @param mustBeInGroup
   * @return the assignment in case there are edits
   */
  public static AttributeAssign groupIntersection(Subject actAs, Group ruleGroup, Group mustBeInGroup) {
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    
    //note "mustBeInGroup" is the group (e.g. employees)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.membershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(),
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(),
        RuleThenEnum.removeMemberFromOwnerGroup.name());

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
    
  }
  
  /**
   * put a rule on the rule group which says that if the user is not in the mustBeInGroup, 
   * then add an end date to the membership in the rule group X days in the future
   * @param actAs
   * @param ruleGroup
   * @param mustBeInGroup
   * @param daysInFutureForDisabledDate
   * @return the assignment in case there are edits
   */
  public static AttributeAssign groupIntersection(Subject actAs, Group ruleGroup, Group mustBeInGroup, 
      int daysInFutureForDisabledDate) {

    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    
    //if the user falls out of mustBeInGroup, then set a disabled date in this group
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.membershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(),
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignMembershipDisabledDaysForOwnerGroupId.name());
    
    //number of days in future that disabled date should be set
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), Integer.toString(daysInFutureForDisabledDate));
    
    //if the membership in owner group doesnt exist, should it be added?  T|F
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "F");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;

  }

  /**
   * 
   * @return the string
   */
  public static String rulesToString() {
    RuleEngine ruleEngine = RuleEngine.ruleEngine();

    StringBuilder result = new StringBuilder();
    int i=0;

    for (RuleDefinition ruleDefinition : ruleEngine.getRuleDefinitions()) {
      
      result.append("Rule " + i + ": ");
      
      result.append(ruleDefinition.toString()).append("\n");

      i++;
    }
    
    return result.toString();
  }

  /**
   * 
   * @param attributeAssignable
   * @return the string
   */
  public static String rulesToString(AttributeAssignable attributeAssignable) {
    
    Set<AttributeAssign> attributeAssigns = attributeAssignable.getAttributeDelegate()
      .retrieveAssignments(RuleUtils.ruleAttributeDefName());

    //remove disabled
    Iterator<AttributeAssign> iterator = GrouperUtil.nonNull(attributeAssigns).iterator();

    
    while (iterator.hasNext()) {
      
      AttributeAssign attributeAssign = iterator.next();
      if (!attributeAssign.isEnabled()) {
        iterator.remove();
      }
      
    }
    
    if (GrouperUtil.length(attributeAssigns) == 0) {
      return "ro rules assigned";
    }
    
    StringBuilder result = new StringBuilder();
    int i=0;
    
    for (AttributeAssign attributeAssign : attributeAssigns) {
      
      result.append("Rule " + i + ": ");
      
      RuleDefinition ruleDefinition = new RuleDefinition(attributeAssign.getId());
      
      result.append(ruleDefinition.toString());
      
      if (i < (GrouperUtil.length(attributeAssigns) -1)) {
        result.append("\n"); //note, should already have a comma on it
      }
      i++;
    }
    return result.toString();
  }
  
  /**
   * run rules for an attribute assignable
   * @param attributeAssignable
   * @return the number of rules ran (note, if not valid or not daemonable then dont run, then that doesnt count)
   */
  public static int runRulesForOwner(AttributeAssignable attributeAssignable) {

    Set<AttributeAssign> attributeAssigns = attributeAssignable.getAttributeDelegate()
      .retrieveAssignments(RuleUtils.ruleAttributeDefName());

    //remove disabled
    Iterator<AttributeAssign> iterator = GrouperUtil.nonNull(attributeAssigns).iterator();
    
    while (iterator.hasNext()) {
      
      AttributeAssign attributeAssign = iterator.next();
      if (!attributeAssign.isEnabled()) {
        iterator.remove();
      }
      
    }
    
    if (GrouperUtil.length(attributeAssigns) == 0) {
      return 0;
    }
    
    int i=0;
    
    for (AttributeAssign attributeAssign : attributeAssigns) {
      
      RuleDefinition ruleDefinition = new RuleDefinition(attributeAssign.getId());
      
      if (ruleDefinition.validate() == null) {
        if (ruleDefinition.runDaemonOnDefinitionIfShould()) {
          i++;
        }
      }
      
      
    }
    return i;
    
  }
  
  /**
   * put a rule on an attribute def so that if a user comes out of a group, the user will have disabled dates from
   * a role which has permissions or removed assignments directly to the user
   * @param actAs
   * @param permissionToAssignRule
   * @param mustBeInGroup
   * @param daysInFutureToDisable
   * @return the assignment in case there are edits
   */
  public static AttributeAssign permissionGroupIntersection(Subject actAs, 
      AttributeDef permissionToAssignRule, Group mustBeInGroup, int daysInFutureToDisable) {

    //add a rule on stem:permission saying if you are out of stem:employee, 
    //then put disabled date on assignments to permission, or from roles which have the permission
    AttributeAssign attributeAssign = permissionToAssignRule
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisPermissionDefHasAssignment.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.assignDisabledDaysToOwnerPermissionDefAssignments.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), Integer.toString(daysInFutureToDisable));
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
  }
  
  /**
   * put a rule on an attribute def so that if a user comes out of a group, the user will be removed from
   * a role which has permissions or removed assignments directly to the user
   * @param actAs
   * @param permissionToAssignRule
   * @param mustBeInGroup
   * @return the assignment in case there are edits
   */
  public static AttributeAssign permissionGroupIntersection(Subject actAs, 
      AttributeDef permissionToAssignRule, Group mustBeInGroup) {

    //add a rule on stem:permission saying if you are out of stem:employee, 
    //then remove assignments to permission, or from roles which have the permission
    AttributeAssign attributeAssign = permissionToAssignRule
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisPermissionDefHasAssignment.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerPermissionDefAssignments.name());
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
  }
  
  /**
   * 
   * @param actAs
   * @param permissionToAssignRule
   * @param mustBeInGroupInFolder
   * @param stemScope
   * @return the assignment in case there are edits
   */
  public static AttributeAssign permissionFolderIntersection(Subject actAs, AttributeDef permissionToAssignRule, 
      Stem mustBeInGroupInFolder, Stem.Scope stemScope) {
    
    //add a rule on stem:permission saying if you are out of stem:employee, 
    //then remove assignments to permission, or from roles which have the permission
    AttributeAssign attributeAssign = permissionToAssignRule
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());

    //folder where membership was removed
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroupInFolder.getUuid());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemoveInFolder.name());

    //SUB for all descendants, ONE for just children
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        stemScope.name());
    
    //if there is no more membership in the folder, and there is a membership in the group
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisPermissionDefHasAssignmentAndNotFolder.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerPermissionDefAssignments.name());
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
    
  }
  
  /**
   * veto a direct permission assignment if not in group
   * @param actAs
   * @param permissionDef 
   * @param mustBeInGroup
   * @param vetoKey
   * @param vetoMessage
   * @return the assignment in case there are edits
   */
  public static AttributeAssign vetoPermissionIfNotInGroup(Subject actAs, 
      AttributeDef permissionDef, Group mustBeInGroup, String vetoKey, String vetoMessage) {
    //add a rule on stem:a saying if not in stem:b, then dont allow add to stem:a
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.permissionAssignToSubject.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.groupHasNoImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfOwnerIdName(), mustBeInGroup.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    
    //key which would be used in UI messages file if applicable
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), vetoKey);
    
    //error message (if key in UI messages file not there)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), vetoMessage);
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;

  }
  
  /**
   * @param ruleGroup
   * @param actAsSubject
   * @param emailToValue e.g. "a@b.c, ${safeSubject.emailAddress}"
   * @param emailSubjectValue e.g. "You will be removed from group: ${groupDisplayExtension}"
   * @param emailBodyValue e.g. "template: testEmailGroupBodyFlattenedRemove"
   * @return the assignment in case there are edits
   */
  public static AttributeAssign emailOnFlattenedMembershipRemove(Subject actAsSubject, Group ruleGroup, 
      String emailToValue, String emailSubjectValue, String emailBodyValue) {

    //add a rule on stem:a saying if you are out of the group by all paths (flattened), then send an email
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAsSubject.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAsSubject.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.flattenedMembershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), emailToValue);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), emailSubjectValue);
    
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), emailBodyValue);
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
 
  }
  
  /**
   * 
   * @param actAsSubject
   * @param ruleStem
   * @param stemScope
   * @param emailToValue
   * @param emailSubjectValue
   * @param emailBodyValue
   * @return the assignment in case there are edits
   */
  public static AttributeAssign emailOnFlattenedMembershipAddFromStem(Subject actAsSubject, Stem ruleStem,
      Stem.Scope stemScope, String emailToValue, String emailSubjectValue, String emailBodyValue) {
    
    //add a rule on stem:a saying if you are added to a group in the stem by a new paths (flattened), then send an email
    AttributeAssign attributeAssign = ruleStem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(),actAsSubject.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAsSubject.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.flattenedMembershipAddInFolder.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        stemScope.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), emailToValue);

    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), emailSubjectValue);
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), emailBodyValue);
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;

  }

  /**
   * 
   * @param actAsSubject
   * @param ruleStem
   * @param stemScope
   * @param emailToValue
   * @param emailSubjectValue
   * @param emailBodyValue
   * @return the assignment to tweak it
   */
  public static AttributeAssign emailOnFlattenedMembershipRemoveFromStem(Subject actAsSubject, Stem ruleStem,
      Stem.Scope stemScope, String emailToValue, String emailSubjectValue, String emailBodyValue) {
    
    //add a rule on stem:a saying if you are removed from a group in the stem by all paths (flattened), then send an email
    AttributeAssign attributeAssign = ruleStem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(),actAsSubject.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAsSubject.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.flattenedMembershipRemoveInFolder.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        stemScope.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), emailToValue);
  
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), emailSubjectValue);
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), emailBodyValue);
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
  
  }

  /**
   * @param ruleGroup
   * @param actAsSubject
   * @param emailToValue e.g. "a@b.c, ${safeSubject.emailAddress}"
   * @param emailSubjectValue e.g. "You were added to group: ${groupDisplayExtension}"
   * @param emailBodyValue e.g. "template: testEmailGroupBodyFlattenedAdd"
   * @return the assignment to tweak it
   */
  public static AttributeAssign emailOnFlattenedMembershipAdd(Subject actAsSubject, Group ruleGroup, 
      String emailToValue, String emailSubjectValue, String emailBodyValue) {
  
    //add a rule on stem:a saying if you are in a group by a paths (flattened), then send an email
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAsSubject.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAsSubject.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.flattenedMembershipAdd.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), emailToValue);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), emailSubjectValue);
    
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), emailBodyValue);
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;
  
  }
  
  /**
   * send emails via daemon on impending disabled memberships
   * @param actAsSubject 
   * @param ruleGroup 
   * @param daysInFutureDisabledDateMin 
   * @param daysInFutureDisabledDateMax 
   * @param emailToValue 
   * @param emailSubjectValue 
   * @param emailBodyValue 
   * @return the attribute assign for customizing
   */
  public static AttributeAssign emailOnFlattenedDisabledDate(Subject actAsSubject, 
      Group ruleGroup, Integer daysInFutureDisabledDateMin, 
      Integer daysInFutureDisabledDateMax, 
      String emailToValue, String emailSubjectValue, String emailBodyValue) {
    
    //add a rule on stem:a saying if you are about to be out of the group by all paths (flattened), then send an email
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAsSubject.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAsSubject.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipDisabledDate.name());
    
    //will find memberships with a disabled date at least 6 days from now.  blank means no min
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg0Name(), daysInFutureDisabledDateMin == null ? null : daysInFutureDisabledDateMin.toString());

    //will find memberships with a disabled date at most 8 days from now.  blank means no max
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg1Name(), daysInFutureDisabledDateMax == null ? null : daysInFutureDisabledDateMax.toString());

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), emailToValue);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), emailSubjectValue);
 
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), emailBodyValue);
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    return attributeAssign;

  }
}
