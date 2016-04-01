/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class RuleFinder {

  /**
   * 
   */
  public RuleFinder() {
  }

  /**
   * find group inherit rules by stem name
   * @param stem
   * @return the rules
   */
  public static Set<RuleDefinition> findGroupPrivilegeInheritRules(Stem stem) {
    
    RuleEngine ruleEngine = RuleEngine.ruleEngine();
    
    //handle root stem... hmmm
    //we need to simulate a child object here
    String groupName = stem.isRootStem() ? "qwertyuiopasdfghjkl:b" : (stem.getName() + ":b");
    
    RuleCheck ruleCheck = new RuleCheck(RuleCheckType.groupCreate.name(), null, groupName, null, null, null);
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
    
    Iterator<RuleDefinition> iterator = ruleDefinitions.iterator();
    
    while (iterator.hasNext()) {
      
      RuleDefinition ruleDefinition = iterator.next();
      RuleThen ruleThen = ruleDefinition.getThen();
      RuleThenEnum ruleThenEnum = ruleThen.thenEnum();
      if (ruleThenEnum != RuleThenEnum.assignGroupPrivilegeToGroupId) {
        iterator.remove();
      }
    }
    
    return ruleDefinitions;

  }

  /**
   * find subject inherit rules by stem name.  Note, the calling subject must be able to see the rules
   * @param secure
   * @return the rules
   */
  public static Set<RuleDefinition> findPrivilegeInheritRules(boolean secure) {
    RuleEngine ruleEngine = RuleEngine.ruleEngine();

    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //go through rules and see which are about creating objects, and assigning privileges

    Subject grouperSessionSubject = GrouperSession.staticGrouperSession().getSubject();

    boolean wheelOrRoot = PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject());

    for (RuleDefinition ruleDefinition : ruleEngine.getRuleDefinitions()) {
      switch (ruleDefinition.getCheck().checkTypeEnum()) {
        case attributeDefCreate:
        case groupCreate:
        case stemCreate:
          switch (ruleDefinition.getThen().thenEnum()) {
            case assignAttributeDefPrivilegeToAttributeDefId:
            case assignGroupPrivilegeToGroupId:
            case assignStemPrivilegeToStemId:

              //get the stem
              Stem stem = ruleDefinition.getAttributeAssignType().getOwnerStem();
              if (stem == null) {
                //why?
                continue;
              }

              boolean privilegeOk = false;
              if (wheelOrRoot || !secure) {
                privilegeOk = true;
              }
              if (!privilegeOk) {

                if (!stem.canHavePrivilege(grouperSessionSubject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
                  privilegeOk = true;
                }
              }
              if (!privilegeOk) {
                Scope scope = Scope.valueOfIgnoreCase(ruleDefinition.getCheck().getCheckStemScope(), false);
                
                if (scope == null) {
                  continue;
                }
                
                switch (ruleDefinition.getCheck().checkTypeEnum()) {
                  case attributeDefCreate:

                    //is there a membership for attribute def?
                    Membership membership = new MembershipFinder().assignScope(stem.getName()).assignStemScope(scope)
                      .assignField(AttributeDefPrivilege.ATTR_ADMIN.getField())
                      .assignQueryOptionsForAttributeDef(new QueryOptions().paging(1, 1, false)).findMembership(false);
                    
                    privilegeOk = membership != null;
                    
                    break;
                  case groupCreate:

                    //is there a membership for group?
                    membership = new MembershipFinder().assignScope(stem.getName()).assignStemScope(scope)
                      .assignField(AccessPrivilege.ADMIN.getField())
                      .assignQueryOptionsForGroup(new QueryOptions().paging(1, 1, false)).findMembership(false);
                    
                    privilegeOk = membership != null;

                    break;
                  case stemCreate:
                    
                    //is there a membership for stem?
                    membership = new MembershipFinder().assignScope(stem.getName()).assignStemScope(scope)
                      .assignField(NamingPrivilege.STEM_ADMIN.getField())
                      .assignQueryOptionsForStem(new QueryOptions().paging(1, 1, false)).findMembership(false);
                    
                    privilegeOk = membership != null;

                    break;
                  default:
                    
                }
              }
              if (!privilegeOk) {
                continue;
              }
              ruleDefinitions.add(ruleDefinition);
              break;
            default: 
                
          }
          default:
      }
    }
    
    return ruleDefinitions;

  }
    

  /**
   * find subject inherit rules by stem name.  Note, the calling subject must be able to see the rules
   * @param subject
   * @param secure
   * @return the rules
   */
  public static Set<RuleDefinition> findSubjectPrivilegeInheritRules(Subject subject, boolean secure) {
    
    RuleEngine ruleEngine = RuleEngine.ruleEngine();

    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //go through rules and see which are about creating objects, and assigning privileges

    Subject grouperSessionSubject = GrouperSession.staticGrouperSession().getSubject();

    //if checking security
    boolean wheelOrRoot = PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject());

    for (RuleDefinition ruleDefinition : ruleEngine.getRuleDefinitions()) {
      switch (ruleDefinition.getCheck().checkTypeEnum()) {
        case attributeDefCreate:
        case groupCreate:
        case stemCreate:
          switch (ruleDefinition.getThen().thenEnum()) {
            case assignAttributeDefPrivilegeToAttributeDefId:
            case assignGroupPrivilegeToGroupId:
            case assignStemPrivilegeToStemId:
              
              //see if the subject matches
              String subjectPackedString = ruleDefinition.getThen().getThenEnumArg0();
              Subject currentSubject = SubjectFinder.findByPackedSubjectString(subjectPackedString, false);
              if (subject != null && SubjectHelper.eq(currentSubject, subject)) {
                
                //get the stem
                Stem stem = ruleDefinition.getAttributeAssignType().getOwnerStem();
                if (stem == null) {
                  //why?
                  continue;
                }

                boolean privilegeOk = false;
                if (wheelOrRoot || !secure) {
                  privilegeOk = true;
                }
                if (!privilegeOk) {

                  if (!stem.canHavePrivilege(grouperSessionSubject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
                    privilegeOk = true;
                  }
                }
                if (!privilegeOk) {
                  Scope scope = Scope.valueOfIgnoreCase(ruleDefinition.getCheck().getCheckStemScope(), false);
                  
                  if (scope == null) {
                    continue;
                  }
                  
                  switch (ruleDefinition.getCheck().checkTypeEnum()) {
                    case attributeDefCreate:

                      //is there a membership for attribute def?
                      Membership membership = new MembershipFinder().assignScope(stem.getName()).assignStemScope(scope)
                        .assignField(AttributeDefPrivilege.ATTR_ADMIN.getField())
                        .assignQueryOptionsForAttributeDef(new QueryOptions().paging(1, 1, false)).findMembership(false);
                      
                      privilegeOk = membership != null;
                      
                      break;
                    case groupCreate:

                      //is there a membership for group?
                      membership = new MembershipFinder().assignScope(stem.getName()).assignStemScope(scope)
                        .assignField(AccessPrivilege.ADMIN.getField())
                        .assignQueryOptionsForGroup(new QueryOptions().paging(1, 1, false)).findMembership(false);
                      
                      privilegeOk = membership != null;

                      break;
                    case stemCreate:
                      
                      //is there a membership for stem?
                      membership = new MembershipFinder().assignScope(stem.getName()).assignStemScope(scope)
                        .assignField(NamingPrivilege.STEM_ADMIN.getField())
                        .assignQueryOptionsForStem(new QueryOptions().paging(1, 1, false)).findMembership(false);
                      
                      privilegeOk = membership != null;

                      break;
                    default:
                      
                  }
                  
                }
                if (!privilegeOk) {
                  continue;
                }
                ruleDefinitions.add(ruleDefinition);
                
              }
              default:
          }
          default:
      }
    }
    
    return ruleDefinitions;

  }
  
  /**
   * find folder inherit rules by stem name
   * @param stem
   * @return the rules
   */
  public static Set<RuleDefinition> findFolderPrivilegeInheritRules(Stem stem) {
    
    RuleEngine ruleEngine = RuleEngine.ruleEngine();

    //handle root stem... hmmm
    //we need to simulate a child object here
    String stemName = stem.isRootStem() ? "qwertyuiopasdfghjkl:b" : (stem.getName() + ":b");

    RuleCheck ruleCheck = new RuleCheck(RuleCheckType.stemCreate.name(), null, stemName, null, null, null);
    Set<RuleDefinition> ruleDefinitions = ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck);

    Iterator<RuleDefinition> iterator = ruleDefinitions.iterator();
    
    while (iterator.hasNext()) {
      
      RuleDefinition ruleDefinition = iterator.next();
      RuleThen ruleThen = ruleDefinition.getThen();
      RuleThenEnum ruleThenEnum = ruleThen.thenEnum();
      if (ruleThenEnum != RuleThenEnum.assignStemPrivilegeToStemId) {
        iterator.remove();
      }
    }
    
    return ruleDefinitions;

  }
  
  
  /**
   * find attribute def inherit rules by stem name
   * @param stem
   * @return the rules
   */
  public static Set<RuleDefinition> findAttributeDefPrivilegeInheritRules(Stem stem) {
    
    RuleEngine ruleEngine = RuleEngine.ruleEngine();

    //handle root stem... hmmm
    //we need to simulate a child object here
    String attributeDefName = stem.isRootStem() ? "qwertyuiopasdfghjkl:b" : (stem.getName() + ":b");

    RuleCheck ruleCheck = new RuleCheck(RuleCheckType.attributeDefCreate.name(), null, attributeDefName, null, null, null);
    Set<RuleDefinition> ruleDefinitions = ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck);
    
    Iterator<RuleDefinition> iterator = ruleDefinitions.iterator();
    
    while (iterator.hasNext()) {
      
      RuleDefinition ruleDefinition = iterator.next();
      RuleThen ruleThen = ruleDefinition.getThen();
      RuleThenEnum ruleThenEnum = ruleThen.thenEnum();
      if (ruleThenEnum != RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId) {
        iterator.remove();
      }
    }
    
    return ruleDefinitions;

  }
  
  
  
}
