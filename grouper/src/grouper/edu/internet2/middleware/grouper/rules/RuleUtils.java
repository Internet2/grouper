/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;


/**
 *
 */
public class RuleUtils {

  /**
   * return the rule attribute def name, assign this to an object to attach a rule.
   * this throws exception if cant find
   * @return the attribute def name
   */
  public static AttributeDefName ruleAttributeDefName() {
    return AttributeDefNameFinder.findByName(attributeRuleStemName() + ":rule", true);
  }

  /**
   * return the rule type attribute def
   * this throws exception if cant find
   * @return the attribute def
   */
  public static AttributeDef ruleTypeAttributeDef() {
    return AttributeDefFinder.findByName(attributeRuleStemName() + ":rulesTypeDef", true);
  }

  /**
   * return the rule attr attribute def
   * this throws exception if cant find
   * @return the attribute def
   */
  public static AttributeDef ruleAttrAttributeDef() {
    return AttributeDefFinder.findByName(attributeRuleStemName() + ":rulesAttrDef", true);
  }

  
  
  /**
   * return the stem name where the rule attributes go, without colon on end
   * @return stem name
   */
  public static String attributeRuleStemName() {
    String rootStemName = GrouperCheckConfig.attributeRootStemName();
    
    //namespace this separate from other builtins
    rootStemName += ":rules";
    return rootStemName;
  }

  /**
   * 
   */
  public static final String RULE_THEN_EL = "ruleThenEl";

  /**
   * rule then el name
   */
  private static String ruleThenElName = null;

  /**
   * full rule then el name
   * @return name
   */
  public static String ruleThenElName() {
    if (ruleThenElName == null) {
      ruleThenElName = RuleUtils.attributeRuleStemName() + ":" + RULE_THEN_EL;
    }
    return ruleThenElName;
  }
  
  /**
   * 
   */
  public static final String RULE_THEN_ENUM = "ruleThenEnum";

  /**
   * rule then enum name
   */
  private static String ruleThenEnumName = null;

  /**
   * full rule then enum name
   * @return name
   */
  public static String ruleThenEnumName() {
    if (ruleThenEnumName == null) {
      ruleThenEnumName = RuleUtils.attributeRuleStemName() + ":" + RULE_THEN_ENUM;
    }
    return ruleThenEnumName;
  }
  
  /**
   * 
   */
  public static final String RULE_IF_CONDITION_ENUM = "ruleIfConditionEnum";
  
  /**
   * rule if condition enum
   */
  private static String ruleIfConditionEnumName = null;

  /**
   * full rule if condition enum name
   * @return name
   */
  public static String ruleIfConditionEnumName() {
    if (ruleIfConditionEnumName == null) {
      ruleIfConditionEnumName = RuleUtils.attributeRuleStemName() + ":" + RULE_IF_CONDITION_ENUM;
    }
    return ruleIfConditionEnumName;
  }
  
  /**
   * 
   */
  public static final String RULE_IF_CONDITION_EL = "ruleIfConditionEl";
  
  /**
   * ruleIfConditionElName
   */
  private static String ruleIfConditionElName = null;

  /**
   * full rule ruleIfConditionElName
   * @return name
   */
  public static String ruleIfConditionElName() {
    if (ruleIfConditionElName == null) {
      ruleIfConditionElName = RuleUtils.attributeRuleStemName() + ":" + RULE_IF_CONDITION_EL;
    }
    return ruleIfConditionElName;
  }
  
  /**
   * 
   */
  public static final String RULE_CHECK_OWNER_NAME = "ruleCheckOwnerName";

  /**
   * rule ruleCheckOwnerName
   */
  private static String ruleCheckOwnerNameName = null;

  /**
   * full ruleCheckOwnerName
   * @return name
   */
  public static String ruleCheckOwnerNameName() {
    if (ruleCheckOwnerNameName == null) {
      ruleCheckOwnerNameName = RuleUtils.attributeRuleStemName() + ":" + RULE_CHECK_OWNER_NAME;
    }
    return ruleCheckOwnerNameName;
  }
  
  /**
   * 
   */
  public static final String RULE_CHECK_STEM_SCOPE = "ruleCheckStemScope";

  /**
   * rule ruleCheckStemScope
   */
  private static String ruleCheckStemScopeName = null;

  /**
   * full ruleCheckStemScope
   * @return name
   */
  public static String ruleCheckStemScopeName() {
    if (ruleCheckStemScopeName == null) {
      ruleCheckStemScopeName = RuleUtils.attributeRuleStemName() + ":" + RULE_CHECK_STEM_SCOPE;
    }
    return ruleCheckStemScopeName;
  }
  
  /**
   * 
   */
  public static final String RULE_CHECK_OWNER_ID = "ruleCheckOwnerId";

  /**
   * ruleCheckOwnerIdName
   */
  private static String ruleCheckOwnerIdName = null;

  /**
   * full ruleCheckOwnerIdName
   * @return name
   */
  public static String ruleCheckOwnerIdName() {
    if (ruleCheckOwnerIdName == null) {
      ruleCheckOwnerIdName = RuleUtils.attributeRuleStemName() + ":" + RULE_CHECK_OWNER_ID;
    }
    return ruleCheckOwnerIdName;
  }
  
  /**
   * 
   */
  public static final String RULE_CHECK_TYPE = "ruleCheckType";

  /**
   * rule ruleCheckTypeName
   */
  private static String ruleCheckTypeName = null;

  /**
   * full ruleCheckTypeName
   * @return name
   */
  public static String ruleCheckTypeName() {
    if (ruleCheckTypeName == null) {
      ruleCheckTypeName = RuleUtils.attributeRuleStemName() + ":" + RULE_CHECK_TYPE;
    }
    return ruleCheckTypeName;
  }
  
  /**
   * 
   */
  public static final String RULE_ACT_AS_SUBJECT_SOURCE_ID = "ruleActAsSubjectSourceId";

  /**
   * rule ruleActAsSubjectSourceIdName
   */
  private static String ruleActAsSubjectSourceIdName = null;

  /**
   * full ruleActAsSubjectSourceIdName
   * @return name
   */
  public static String ruleActAsSubjectSourceIdName() {
    if (ruleActAsSubjectSourceIdName == null) {
      ruleActAsSubjectSourceIdName = RuleUtils.attributeRuleStemName() + ":" + RULE_ACT_AS_SUBJECT_SOURCE_ID;
    }
    return ruleActAsSubjectSourceIdName;
  }
  
  /**
   * 
   */
  public static final String RULE_ACT_AS_SUBJECT_IDENTIFIER = "ruleActAsSubjectIdentifier";

  /**
   * rule then el name
   */
  private static String ruleActAsSubjectIdentifierName = null;

  /**
   * full rule then el name
   * @return name
   */
  public static String ruleActAsSubjectIdentifierName() {
    if (ruleActAsSubjectIdentifierName == null) {
      ruleActAsSubjectIdentifierName = RuleUtils.attributeRuleStemName() + ":" + RULE_ACT_AS_SUBJECT_IDENTIFIER;
    }
    return ruleActAsSubjectIdentifierName;
  }
  
  /**
   * 
   */
  public static final String RULE_ACT_AS_SUBJECT_ID = "ruleActAsSubjectId";
  
  /**
   * rule ruleActAsSubjectIdName
   */
  private static String ruleActAsSubjectIdName = null;

  /**
   * full ruleActAsSubjectIdName
   * @return name
   */
  public static String ruleActAsSubjectIdName() {
    if (ruleActAsSubjectIdName == null) {
      ruleActAsSubjectIdName = RuleUtils.attributeRuleStemName() + ":" + RULE_ACT_AS_SUBJECT_ID;
    }
    return ruleActAsSubjectIdName;
  }
  
  /**
   * remove a member of a group
   * @param groupId
   * @param memberId
   * @return true if removed, false if not
   */
  public static boolean removeMember(String groupId, String memberId) {
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    return group.deleteMember(member, false);
  }
  
  
}
