/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;


import java.io.File;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class RuleUtils {

  
  
  /**
   * 
   * @param rulesBean
   * @param groupId
   * @return
   */
  public static boolean groupHasImmediateEnabledMembership(RulesBean rulesBean, String groupId) {
    String memberId = null;
    try {
      memberId = rulesBean.getMemberId();
    } catch (Exception e) {
      //ignore
    }
    
    GrouperSession rootSession = GrouperSession.startRootSession(false);
    try {
      
      if (StringUtils.isBlank(memberId)) {
        
        Member member = MemberFinder.findBySubject(rootSession, rulesBean.getSubject(), false);
        memberId = member == null ? null : member.getUuid();
        
        if (StringUtils.isBlank(memberId )) {
          throw new RuntimeException("memberId cannot be null");
        }
      }
      
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("groupId cannot be null");
      }
      
      Group group = GroupFinder.findByUuid(rootSession, groupId, false);
      
      if (group == null) {
        LOG.error("Group doesnt exist in rule!");
        return false;
      }
      
      Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
        .findAllByGroupOwnerAndFieldAndMemberIdsAndType(
            group.getId(), Group.getDefaultList(), 
            GrouperUtil.toSet(memberId), "immediate", true);
      
      //if not in this group, forget it
      if (GrouperUtil.length(memberships) > 0) {
        return true;
      }
      
      return false;
      
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

  }
  
  /**
   * see if there is a membership in the folder
   * @param rulesBean
   * @param stemId add either this or stem name
   * @param stemName add either this or stem id
   * @param stemScope
   * @param membershipType null for any
   * @return true if membership, false if not
   */
  public static boolean folderHasMembership(RulesBean rulesBean, String stemId, String stemName, Stem.Scope stemScope, MembershipType membershipType) {

    String memberId = null;
    try {
      memberId = rulesBean.getMemberId();
    } catch (Exception e) {
      //ignore
    }
    
    GrouperSession rootSession = GrouperSession.startRootSession(false);
    try {
      
      if (StringUtils.isBlank(memberId)) {
        
        Member member = MemberFinder.findBySubject(rootSession, rulesBean.getSubject(), false);
        memberId = member == null ? null : member.getUuid();

        if (StringUtils.isBlank(memberId )) {
          return false;
        }
      }
      
      Stem stem = null;
      if (!StringUtils.isBlank(stemId)) {
        stem = StemFinder.findByUuid(rootSession, stemId, false);
      } else if (!StringUtils.isBlank(stemName)) {
        stem = StemFinder.findByName(rootSession, stemName, false);
      }
      
      if (stem == null) {
        throw new RuntimeException("Cant find stem: " + stemName + ", " + stemId);
      }
      
      Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
        .findAllByStemParentOfGroupOwnerAndFieldAndType(stem, stemScope, Group.getDefaultList(), membershipType, true, memberId);
    
      //if not in this group, forget it
      if (GrouperUtil.length(memberships) == 0) {
        return false;
      }
    
      return true;
      
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

  }

  /**
   * 
   * @param attributeDefId
   * @param rulesBean
   * @param noEndDate 
   * @return the set of permissions entries
   */
  public static Set<PermissionEntry> permissionsForUser(final String attributeDefId, final RulesBean rulesBean, final boolean noEndDate) {
    GrouperSession rootSession = GrouperSession.startRootSession(false);
    
    String memberId = null;
    try {
      memberId = (String)GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          String memberId = null;
          try {
            memberId = rulesBean.getMemberId();
          } catch (Exception e) {
            //ignore
          }

          if (StringUtils.isBlank(memberId)) {
            
            Member member = MemberFinder.findBySubject(grouperSession, rulesBean.getSubject(), false);
            memberId = member == null ? null : member.getUuid();

          }
          return memberId;
        }
      });

    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    return permissionsForUser(attributeDefId, memberId, noEndDate);
    

  }

  
  /**
   * 
   * @param attributeDefId
   * @param rulesBean
   * @param memberId
   * @param noEndDate
   * @return the set of permissions entries
   */
  public static Set<PermissionEntry> permissionsForUser(final String attributeDefId, final String memberId, final boolean noEndDate) {
    GrouperSession rootSession = GrouperSession.startRootSession(false);
    
    try {
     return (Set<PermissionEntry>)GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          if (StringUtils.isBlank(memberId)) {
            return false;
          }

          if (StringUtils.isBlank(attributeDefId)) {
            throw new RuntimeException("Expecting an attributeDefId!");
          }

          Set<PermissionEntry> permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry()
            .findPermissions(GrouperUtil.toSet(attributeDefId), null, null, null, true, GrouperUtil.toSet(memberId), noEndDate);
        
          return permissionEntries;
        }
      });

    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

  }
  
  /**
   * if it starts with template: then get the arg from a file.  If it doesnt, then it is the template, return that.
   * if there is a problem retrieving the template, then throw exception.
   * if the template name is invalid, throw exception
   * @param emailTemplateString
   * @return the email template
   */
  public static String emailTemplate(String emailTemplateString) {
    
    if (StringUtils.isBlank(emailTemplateString)) {
      return emailTemplateString;
    }
    
    //lets check the templates
    emailTemplateString = emailTemplateString.trim();
    if (emailTemplateString.startsWith("template:")) {
      String emailTemplateName = emailTemplateString.substring("template:".length(), emailTemplateString.length()).trim();
    
      //make sure valid
      if (!emailTemplateName.matches("^[a-zA-Z0-9-_]+$")) {
        throw new RuntimeException("emailTemplateName must be alphanumeric, dash, or underscore only: '" + emailTemplateName + "'");
      }

      //see if there is a directory
      String emailTemplatesFolder = GrouperConfig.getProperty("rules.emailTemplatesFolder");
      
      //if there is a folder
      if (!StringUtils.isBlank(emailTemplatesFolder)) {
        
        if (!emailTemplatesFolder.endsWith("/") && !emailTemplatesFolder.endsWith("\\")) {
          emailTemplatesFolder += File.separator;
        }
        
        File templateFile = new File(emailTemplatesFolder + emailTemplateName + ".txt");
        if (!templateFile.exists() || !templateFile.isFile()) {
          throw new RuntimeException("Cant find template on file system: " + templateFile.getAbsolutePath());
        }
        
        String template = GrouperUtil.readFileIntoString(templateFile);
        return template;
      }
      
      //else it is on the classpath
      try {
        String template = GrouperUtil.readResourceIntoString("grouperRulesEmailTemplates/" + emailTemplateName + ".txt", false);
        return template;
      } catch (Exception e) {
        throw new RuntimeException("Cant find template: on classpath: grouperRulesEmailTemplates/" + emailTemplateName + ".txt", e);
      }
    }
    
    //just return the string, it is the template0
    return emailTemplateString;
    
    
  }
  
  /**
   * take in a string, e.g. "this", and return it without quotes on the outside
   * @param string
   * @return the string
   */
  public static String removeSurroundingQuotesConvertNull(String string) {
    
    if (string == null) {
      return string;
    }

    if (StringUtils.equals("null", string)) {
      return null;
    }
    
    char startChar = string.charAt(0);
    char endChar = string.charAt(string.length()-1);
    
    if (startChar == endChar && (startChar == '\'' || startChar == '"' )) {
      return string.substring(1, string.length()-1);
    }
    //not sure why there wouldnt be quotes, oh well
    return string;
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
  public static final String RULE_THEN_ENUM_ARG0 = "ruleThenEnumArg0";

  /**
   * rule then enum arg0 name
   */
  private static String ruleThenEnumArg0Name = null;

  /**
   * full rule then enum arg0 name
   * @return name
   */
  public static String ruleThenEnumArg0Name() {
    if (ruleThenEnumArg0Name == null) {
      ruleThenEnumArg0Name = RuleUtils.attributeRuleStemName() + ":" + RULE_THEN_ENUM_ARG0;
    }
    return ruleThenEnumArg0Name;
  }
  
  /**
   * 
   */
  public static final String RULE_THEN_ENUM_ARG1 = "ruleThenEnumArg1";

  /**
   * rule then enum arg1 name
   */
  private static String ruleThenEnumArg1Name = null;

  /**
   * full rule then enum arg1 name
   * @return name
   */
  public static String ruleThenEnumArg1Name() {
    if (ruleThenEnumArg1Name == null) {
      ruleThenEnumArg1Name = RuleUtils.attributeRuleStemName() + ":" + RULE_THEN_ENUM_ARG1;
    }
    return ruleThenEnumArg1Name;
  }
  
  /**
   * 
   */
  public static final String RULE_THEN_ENUM_ARG2 = "ruleThenEnumArg2";

  /**
   * rule then enum arg2 name
   */
  private static String ruleThenEnumArg2Name = null;

  /**
   * full rule then enum arg2 name
   * @return name
   */
  public static String ruleThenEnumArg2Name() {
    if (ruleThenEnumArg2Name == null) {
      ruleThenEnumArg2Name = RuleUtils.attributeRuleStemName() + ":" + RULE_THEN_ENUM_ARG2;
    }
    return ruleThenEnumArg2Name;
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
  public static final String RULE_CHECK_ARG0 = "ruleCheckArg0";

  /**
   * ruleCheckArg0Name
   */
  private static String ruleCheckArg0Name = null;

  /**
   * full ruleCheckArg0Name
   * @return name
   */
  public static String ruleCheckArg0Name() {
    if (ruleCheckArg0Name == null) {
      ruleCheckArg0Name = RuleUtils.attributeRuleStemName() + ":" + RULE_CHECK_ARG0;
    }
    return ruleCheckArg0Name;
  }
  
  /**
   * 
   */
  public static final String RULE_CHECK_ARG1 = "ruleCheckArg1";

  /**
   * ruleCheckArg1Name
   */
  private static String ruleCheckArg1Name = null;

  /**
   * full ruleCheckArg1Name
   * @return name
   */
  public static String ruleCheckArg1Name() {
    if (ruleCheckArg1Name == null) {
      ruleCheckArg1Name = RuleUtils.attributeRuleStemName() + ":" + RULE_CHECK_ARG1;
    }
    return ruleCheckArg1Name;
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
  public static final String RULE_IF_STEM_SCOPE = "ruleIfStemScope";

  /**
   * ruleIfStemScopeName
   */
  private static String ruleIfStemScopeName = null;

  /**
   * full ruleIfStemScopeName
   * @return name
   */
  public static String ruleIfStemScopeName() {
    if (ruleIfStemScopeName == null) {
      ruleIfStemScopeName = RuleUtils.attributeRuleStemName() + ":" + RULE_IF_STEM_SCOPE;
    }
    return ruleIfStemScopeName;
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
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleUtils.class);
  
  /**
   * 
   * @param groupId
   * @param groupName
   * @param alternateGroupId
   * @param useRootSession if we should use root or static session
   * @param throwExceptionIfNotFound
   * @return group or null
   */
  public static Group group(String groupId, String groupName, String alternateGroupId, boolean useRootSession, boolean throwExceptionIfNotFound) {
    GrouperSession grouperSession = useRootSession ? GrouperSession.startRootSession(false) : GrouperSession.staticGrouperSession();
    try {
      Group group = null;
      if (!StringUtils.isBlank(groupId)) {
        group = GroupFinder.findByUuid(grouperSession, groupId, false);
      } else if (!StringUtils.isBlank(groupName)) {
        group = GroupFinder.findByName(grouperSession, groupName, false);
      } else if (!StringUtils.isBlank(alternateGroupId)) {
        group = GroupFinder.findByUuid(grouperSession, alternateGroupId, false);
      }
      
      if (throwExceptionIfNotFound && group == null) {
        throw new RuntimeException("Cant find group: " + groupId + ", " + groupName + ", " + alternateGroupId);
      }

      return group;

     } finally {
       if (useRootSession) {
         GrouperSession.stopQuietly(grouperSession);
       }
    }
    
  }
  
  /**
   * 
   * @param stemId
   * @param stemName
   * @param alternateStemId
   * @param useRootSession if we should use root or static session
   * @param throwExceptionIfNotFound
   * @return stem or null
   */
  public static Stem stem(String stemId, String stemName, String alternateStemId, boolean useRootSession, boolean throwExceptionIfNotFound) {
    GrouperSession grouperSession = useRootSession ? GrouperSession.startRootSession(false) : GrouperSession.staticGrouperSession();
    try {
      Stem stem = null;
      if (!StringUtils.isBlank(stemId)) {
        stem = StemFinder.findByUuid(grouperSession, stemId, false);
      } else if (!StringUtils.isBlank(stemName)) {
        stem = StemFinder.findByName(grouperSession, stemName, false);
      } else if (!StringUtils.isBlank(alternateStemId)) {
        stem = StemFinder.findByUuid(grouperSession, alternateStemId, false);
      }
      
      if (throwExceptionIfNotFound && stem == null) {
        throw new RuntimeException("Cant find stem: " + stemId + ", " + stemName + ", " + alternateStemId);
      }
      
      return stem;
     } finally {
       if (useRootSession) {
         GrouperSession.stopQuietly(grouperSession);
       }
    }
    
  }
  
  /**
   * 
   * @param attributeDefId
   * @param attributeDefName
   * @param alternateAttributeDefId
   * @param useRootSession if we should use root or static session
   * @param throwExceptionIfNotFound
   * @return attributeDef or null
   */
  public static AttributeDef attributeDef(final String attributeDefId, final String attributeDefName, 
      final String alternateAttributeDefId, final boolean useRootSession, final boolean throwExceptionIfNotFound) {
    GrouperSession grouperSession = useRootSession ? GrouperSession.startRootSession(false) : GrouperSession.staticGrouperSession();
    try {
      return (AttributeDef)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          AttributeDef attributeDef = null;
          if (!StringUtils.isBlank(attributeDefId)) {
            attributeDef = AttributeDefFinder.findById(attributeDefId, false);
          } else if (!StringUtils.isBlank(attributeDefName)) {
            attributeDef = AttributeDefFinder.findByName(attributeDefName, false);
          } else if (!StringUtils.isBlank(alternateAttributeDefId)) {
            attributeDef = AttributeDefFinder.findById( alternateAttributeDefId, false);
          }
          
          if (throwExceptionIfNotFound && attributeDef == null) {
            throw new RuntimeException("Cant find attributeDef: " + attributeDefId + ", " + attributeDefName + ", " + alternateAttributeDefId);
          }
          
          return attributeDef;
        }
      });
     } finally {
       if (useRootSession) {
         GrouperSession.stopQuietly(grouperSession);
       }
    }
    
  }
  
  /**
   * 
   * @param groupId
   * @param groupName
   * @param alternateGroupId
   * 
   * @return the error message or null if ok
   */
  public static String validateGroup(String groupId, String groupName, String alternateGroupId) {
    try {
      group(groupId, groupName, alternateGroupId, true, true);
    } catch (Exception e) {
      return e.getMessage();
    }
    return null;
  }
  
  /**
   * 
   * @param stemId
   * @param stemName
   * @param alternateStemId 
   * @return the error message or null if ok
   */
  public static String validateStem(String stemId, String stemName, String alternateStemId) {
    
    try {
      stem(stemId, stemName, alternateStemId, true, true);
    } catch (Exception e) {
      return e.getMessage();
    }
    return null;
  }
  
  /**
   * 
   * @param attributeDefId
   * @param attributeDefName
   * @param alternateAttributeDefId 
   * @return the error message or null if ok
   */
  public static String validateAttributeDef(String attributeDefId, String attributeDefName, String alternateAttributeDefId) {
    
    try {
      attributeDef(attributeDefId, attributeDefName, alternateAttributeDefId, true, true);
    } catch (Exception e) {
      return e.getMessage();
    }
    return null;
  }
  
  
}
