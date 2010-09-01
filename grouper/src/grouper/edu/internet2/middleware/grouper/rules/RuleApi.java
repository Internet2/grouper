package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
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
  
}
