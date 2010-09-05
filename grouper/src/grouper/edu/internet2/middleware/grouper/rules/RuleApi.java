package edu.internet2.middleware.grouper.rules;

import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * helper methods to assign rules to objects without having to deal with attributes
 * note, you can use this from gsh too
 * @author mchyzer
 */
public class RuleApi {

  /**
   * put a rule on the rule group which says that if the user is not in the mustBeInGroup, 
   * then remove from ruleGroup
   * @param actAs
   * @param ruleGroup
   * @param mustBeInGroup
   */
  public static void groupIntersection(Subject actAs, Group ruleGroup, Group mustBeInGroup) {
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(),
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(),
        RuleThenEnum.removeMemberFromOwnerGroup.name());

  }
  
  /**
   * put a rule on the rule group which says that if the user is not in the mustBeInGroup, 
   * then add an end date to the membership in the rule group X days in the future
   * @param actAs
   * @param ruleGroup
   * @param mustBeInGroup
   * @param daysInFutureForDisabledDate
   */
  public static void groupIntersection(Subject actAs, Group ruleGroup, Group mustBeInGroup, int daysInFutureForDisabledDate) {
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(),
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(),
        "${ruleElUtils.assignMembershipDisabledDaysForGroupId(ownerGroupId, memberId, " + daysInFutureForDisabledDate + ")}");

  }

  /**
   * 
   * @param attributeAssignable
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
    
    Set<AttributeAssign> attributeAssigns = attributeAssignable.getAttributeDelegate().retrieveAssignments(RuleUtils.ruleAttributeDefName());

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

    Set<AttributeAssign> attributeAssigns = attributeAssignable.getAttributeDelegate().retrieveAssignments(RuleUtils.ruleAttributeDefName());

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
  
}
