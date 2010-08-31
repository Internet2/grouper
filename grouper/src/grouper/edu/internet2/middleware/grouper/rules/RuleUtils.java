/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class RuleUtils {

  /**
   * 
   * @param groupId
   * @param memberId
   * @param membershipType @see {@link MembershipType}, null for all
   * @param enabled null for all, T for only enabled, F for only disabled
   * @return true if has immediate enabled membership
   */
  public static boolean hasMembershipByGroupId(String groupId, String memberId, 
      String membershipType, String enabled) {
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Has member: " + memberId + ", from group: " + groupId 
          + ", membershipType: " + membershipType + ", enabled: " + enabled);
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    
    MembershipType membershipTypeEnum = MembershipType.valueOfIgnoreCase(membershipType, false);
    
    Boolean enabledBoolean = GrouperUtil.booleanObjectValue(enabled);
    
    Set<Object[]> membershipSetArray = MembershipFinder.findMemberships(GrouperUtil.toSet(groupId), GrouperUtil.toSet(memberId), 
        null, membershipTypeEnum, Group.getDefaultList(), null, null, null, null, enabledBoolean);
    
    boolean result = GrouperUtil.length(membershipSetArray) > 0;
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Has member: " + member.getSubjectId() + ", from group: " + group.getName() 
          + ", membershipType: " + membershipType + ", enabled: " + enabled + ", result: " + result);
    }
    return result;

  }
  
  /**
   * return the rule attribute def name, assign this to an object to attach a rule.
   * this throws exception if cant find
   * @return the attribute def name
   */
  public static AttributeDefName ruleAttributeDefName() {
    return AttributeDefNameFinder.findByName(attributeRuleStemName() + ":rule", true);
  }

  /**
   * return the rule attribute def name, assign this to an object to attach a rule.
   * this throws exception if cant find
   * @return the attribute def name
   */
  public static AttributeDefName ruleValidAttributeDefName() {
    return AttributeDefNameFinder.findByName(ruleValidName(), true);
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
   * should be T or F
   */
  public static final String RULE_RUN_DAEMON = "ruleRunDaemon";

  /**
   * rule run daemon name
   */
  private static String ruleRunDaemonName = null;

  /**
   * full rule run daemon name
   * @return name
   */
  public static String ruleRunDaemonName() {
    if (ruleRunDaemonName == null) {
      ruleRunDaemonName = RuleUtils.attributeRuleStemName() + ":" + RULE_RUN_DAEMON;
    }
    return ruleRunDaemonName;
  }
  
  /**
   * 
   */
  public static final String RULE_VALID = "ruleValid";

  /**
   * rule valid
   */
  private static String ruleValidName = null;

  /**
   * full rule valid name name
   * @return name
   */
  public static String ruleValidName() {
    if (ruleValidName == null) {
      ruleValidName = RuleUtils.attributeRuleStemName() + ":" + RULE_VALID;
    }
    return ruleValidName;
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
  public static final String RULE_IF_OWNER_NAME = "ruleIfOwnerName";

  /**
   * rule ruleIfOwnerName
   */
  private static String ruleIfOwnerNameName = null;

  /**
   * full ruleIfOwnerName
   * @return name
   */
  public static String ruleIfOwnerNameName() {
    if (ruleIfOwnerNameName == null) {
      ruleIfOwnerNameName = RuleUtils.attributeRuleStemName() + ":" + RULE_IF_OWNER_NAME;
    }
    return ruleIfOwnerNameName;
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
  public static final String RULE_IF_OWNER_ID = "ruleIfOwnerId";

  /**
   * ruleIfOwnerIdName
   */
  private static String ruleIfOwnerIdName = null;

  /**
   * full ruleIfOwnerIdName
   * @return name
   */
  public static String ruleIfOwnerIdName() {
    if (ruleIfOwnerIdName == null) {
      ruleIfOwnerIdName = RuleUtils.attributeRuleStemName() + ":" + RULE_IF_OWNER_ID;
    }
    return ruleIfOwnerIdName;
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
  public static boolean removeMemberFromGroupId(String groupId, String memberId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing member: " + memberId + ", from group: " + groupId);
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing subject: " + member.getSubjectId() + ", from group: " + group.getName());
    }
    return group.deleteMember(member, false);
  }
  /**
   * remove a member of a group
   * @param groupName
   * @param memberId
   * @return true if removed, false if not
   */
  public static boolean removeMemberFromGroupName(String groupName, String memberId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing member: " + memberId + ", from group: " + groupName);
    }
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing subject: " + member.getSubjectId() + ", from group: " + group.getName());
    }
    return group.deleteMember(member, false);
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleUtils.class);
  
  /**
   * veto this for some reason
   * @param reasonKey
   * @param reason
   * @return the exception
   */
  public RuleVeto veto(String reasonKey, String reason) {
    return new RuleVeto(reasonKey, reason);
  }
  
  /**
   * 
   * @param groupId
   * @param groupName
   * @return the error message or null if ok
   */
  public static String validateGroup(String groupId, String groupName) {
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
    try {
      if (!StringUtils.isBlank(groupId)) {
        Group group = GroupFinder.findByUuid(grouperSession, groupId, false);
        if (group == null) {
          return "Cant find group by id: " + groupId;
        }
      }
      if (!StringUtils.isBlank(groupName)) {
        Group group = GroupFinder.findByName(grouperSession, groupName, false);
        if (group == null) {
          return "Cant find group by name: " + groupName;
        }
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return null;
    
  }
  
  /**
   * 
   * @param stemId
   * @param stemName
   * @return the error message or null if ok
   */
  public static String validateStem(String stemId, String stemName) {
    
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
    try {
      if (!StringUtils.isBlank(stemId)) {
        Stem stem = StemFinder.findByUuid(grouperSession, stemId, false);
        if (stem == null) {
          return "Cant find stem by id: " + stemId;
        }
      }
      if (!StringUtils.isBlank(stemName)) {
        Stem stem = StemFinder.findByName(grouperSession, stemName, false);
        if (stem == null) {
          return "Cant find stem by name: " + stemName;
        }
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return null;
    
  }

  /**
   * assign a disabled date in the future by X days
   * @param groupId
   * @param memberId
   * @param daysInFuture
   * @return true if added membership, false if used existing
   */
  public static boolean assignMembershipDisabledDaysForGroupId(String groupId, String memberId, int daysInFuture) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Has member: " + memberId + ", from group: " + groupId 
          + ", daysInFuture: " + daysInFuture);
    }
    boolean result = false;
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    Membership membership = group.getImmediateMembership(Group.getDefaultList(), member, true, false);
    if (membership == null) {
      group.addMember(member.getSubject(), true);
      membership = group.getImmediateMembership(Group.getDefaultList(), member, true, false);
      result = true;
    }
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (daysInFuture * 24 * 60 * 60 * 1000)));
    membership.update();
    return result;
  }
  
}
