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
import edu.internet2.middleware.grouper.attr.AttributeDefName;
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
   * 
   */
  public static final String RULE_IF_CONDITION_EL = "ruleIfConditionEl";
  /**
   * 
   */
  public static final String RULE_CHECK_OWNER_NAME = "ruleCheckOwnerName";
  /**
   * 
   */
  public static final String RULE_CHECK_OWNER_ID = "ruleCheckOwnerId";
  /**
   * 
   */
  public static final String RULE_CHECK_TYPE = "ruleCheckType";
  /**
   * 
   */
  public static final String RULE_ACT_AS_SUBJECT_SOURCE_ID = "ruleActAsSubjectSourceId";
  /**
   * 
   */
  public static final String RULE_ACT_AS_SUBJECT_IDENTIFIER = "ruleActAsSubjectIdentifier";
  /**
   * 
   */
  public static final String RULE_ACT_AS_SUBJECT_ID = "ruleActAsSubjectId";
  
  /**
   * remove a member of a group
   * @param groupId
   * @param memberId
   * @return true if removed, false if not
   */
  public boolean removeMember(String groupId, String memberId) {
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    return group.deleteMember(member, false);
  }
  
  
}
