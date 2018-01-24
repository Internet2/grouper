/**
 * Copyright 2014 Internet2
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
 */
package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * helper methods to assign rules to objects without having to deal with attributes
 * note, you can use this from gsh too
 * @author mchyzer
 */
public class RuleApi {

  /**
   * normalize privileges if the user who creates a group is in a group which has create privilegs on the stem
   * @param actAs
   * @param ruleStem
   * @param stemScope 
   * @return the attribute assignment
   */
  public static AttributeAssign reassignGroupPrivilegesIfFromGroup(Subject actAs, Stem ruleStem, Scope stemScope) {
    AttributeAssign attributeAssign = ruleStem
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
        RuleUtils.ruleThenEnumName(), RuleThenEnum.reassignGroupPrivilegesIfFromGroup.name());
    
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    return attributeAssign;
  }
  
  /**
   * normalize privileges if the user who creates a group is in a group which has create privilegs on the stem
   * @param actAs
   * @param ruleStem
   * @param stemScope 
   * @return the attribute assignment
   */
  public static AttributeAssign reassignAttributeDefPrivilegesIfFromGroup(Subject actAs, Stem ruleStem, Scope stemScope) {
    AttributeAssign attributeAssign = ruleStem
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
        RuleUtils.ruleThenEnumName(), RuleThenEnum.reassignAttributeDefPrivilegesIfFromGroup.name());
    
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
   * add a rule on a stem saying that all subject use in the folder must be in a certain group.
   * note, the first rule found will be used
   * @param actAs
   * @param ruleStem
   * @param mustBeInGroup if blank and not allowAll, then restrict all
   * @param allowAll if mustBeIn is blank and allowAll, then allow all (to override a restriction in ancestor folders)
   * @param sourceId optional (recommended), to constraint this to subjects from certain sources
   * @param stemScope 
   * @param vetoKey
   * @param vetoMessage
   * @return the assignment in case there are edits
   */
  public static AttributeAssign vetoSubjectAssignInFolderIfNotInGroup(Subject actAs, Stem ruleStem, 
      Group mustBeInGroup, boolean allowAll, String sourceId, Stem.Scope stemScope, String vetoKey, String vetoMessage) {
    
    if (allowAll && mustBeInGroup != null) {
      throw new RuntimeException("If allowAll, then mustBeInGroup must be false");
    }
    
    //add a rule on stem:a saying if not in a group, then dont allow add member, permission, privilege etc
    AttributeAssign attributeAssign = ruleStem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
  
    //subject use means membership add, privilege assign, permission assign, etc.
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.subjectAssignInStem.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), stemScope != null ? stemScope.name() : Stem.Scope.SUB.name());
    
    //this is optional to restrict to source.  I think you will want to do that, or you
    //would need to have all the usable groups in the allowed group...
    if (!StringUtils.isBlank(sourceId)) {
      attributeValueDelegate.assignValue(
          RuleUtils.ruleCheckArg0Name(), sourceId);
    }  
    
    //if not allow all, and not must be in group, then leave blank if condition
    if (allowAll) {
      
      attributeValueDelegate.assignValue(
          RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.never.name());

    } else if (mustBeInGroup != null) {
      
      attributeValueDelegate.assignValue(
          RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.groupHasNoEnabledMembership.name());
      attributeValueDelegate.assignValue(
          RuleUtils.ruleIfOwnerIdName(), mustBeInGroup.getId());
      
    }

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
    return inheritGroupPrivileges(actAs, stem, stemScope, subjectToAssign, privileges, null);
  }

  /**
   * find rules on a folder for inherited privs, or get from cache
   * @param inheritedRulesCacheByStemIdSubjectPrivilege
   * @param stem
   * @param subjectToAssign
   * @param privilege
   * @return the applicable rules
   */
  private static Set<RuleDefinition> inheritedRulesForFolderOrCache(Map<MultiKey, Set<RuleDefinition>> inheritedRulesCacheByStemIdSubjectPrivilege, 
      Stem stem, Subject subjectToAssign, Privilege privilege) {
    
    MultiKey multiKey = new MultiKey(stem.getId(), subjectToAssign.getSourceId(), subjectToAssign.getId(), privilege.getName());
    Set<RuleDefinition> ruleDefinitions = inheritedRulesCacheByStemIdSubjectPrivilege.get(multiKey);
    
    if (ruleDefinitions == null) {
      
      ruleDefinitions = GrouperUtil.nonNull(inheritedRulesForFolder(stem, subjectToAssign, privilege));
      inheritedRulesCacheByStemIdSubjectPrivilege.put(multiKey, ruleDefinitions);
    }
    
    return ruleDefinitions;
    
  }
  
  /**
   * find rules on a folder for inherited privileges
   * @param stem
   * @param stemScope
   * @param subjectToAssign
   * @param privilege
   * @return the rule definitions
   */
  private static Set<RuleDefinition> inheritedRulesForFolder(Stem stem, Subject subjectToAssign, Privilege privilege) {
    
    Set<RuleDefinition> ruleDefinitions = null;
    
    if (privilege.isAccess()) {
    
      ruleDefinitions = RuleFinder.findGroupPrivilegeInheritRules(stem);
      
    } else if (privilege.isNaming()) {
      
      ruleDefinitions = RuleFinder.findFolderPrivilegeInheritRules(stem);
      
    } else if (privilege.isAttributeDef()) {
      
      ruleDefinitions = RuleFinder.findAttributeDefPrivilegeInheritRules(stem);
      
    } else {
      
      throw new RuntimeException("Not expecting privilege: " + privilege);

    }
    
    if (GrouperUtil.length(ruleDefinitions) == 0) {
      return ruleDefinitions;
    }
    
    
    Iterator<RuleDefinition> iterator = ruleDefinitions.iterator();
    
    //go through privs
    while (iterator.hasNext()) {
      
      RuleDefinition ruleDefinition = iterator.next();
      
      // the subject must match
      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      if (StringUtils.isBlank(subjectString) ) {
        iterator.remove();
        continue;
      }
      
      Subject ruleSubject = SubjectFinder.findByPackedSubjectString(subjectString, true);
      
      if (!SubjectHelper.eq(ruleSubject, subjectToAssign)) {
        
        iterator.remove();
        continue;
        
      }
      
      //check the privilege
      if (!StringUtils.equals(ruleDefinition.getThen().getThenEnumArg1(), privilege.getName())) {
        
        iterator.remove();
        continue;
        
      }
      
    }
    
    return ruleDefinitions;
  }
  
  
  
  /**
   * remove group privileges are inherited in a stem
   * @param actAsRoot
   * @param stem
   * @param stemScope ONE or SUB
   * @param subjectToAssign
   * @param privileges can use Privilege.getInstances() to convert from string
   * @param sqlLikeString 
   * @return the number removed
   */
  public static int removePrivilegesIfNotAssignedByRule(final boolean actAsRoot, final Stem stem, final Scope stemScope, 
      final Subject subjectToAssign, final Set<Privilege> privileges, final String sqlLikeString) {

    return (Integer)GrouperSession.callbackGrouperSession(actAsRoot ? GrouperSession.staticGrouperSession().internal_getRootSession() : GrouperSession.staticGrouperSession(), new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        int removedCount = 0;
        
        // a lot of these will be duplicates so cache it
        Map<MultiKey, Set<RuleDefinition>> inheritedRulesForFolderOrCache = new HashMap<MultiKey, Set<RuleDefinition>>();
        
        for(Privilege privilege : GrouperUtil.nonNull(privileges)) {
          
          Set<GrouperObject> grouperObjectsWithPrivsToBeRemoved = new HashSet<GrouperObject>();
          
          if (privilege.isAccess()) {
            Set<Group> groupsWhichNeedPrivsRemoved = GrouperSession.staticGrouperSession()
                .getAccessResolver().getGroupsWhereSubjectDoesHavePrivilege(
                    stem.getId(), stemScope, subjectToAssign, privilege, false, sqlLikeString);
            grouperObjectsWithPrivsToBeRemoved.addAll(GrouperUtil.nonNull(groupsWhichNeedPrivsRemoved)); 
          } else if (privilege.isNaming()) {
            Set<Stem> stemsWhichNeedPrivsRemoved = GrouperSession.staticGrouperSession()
                .getNamingResolver().getStemsWhereSubjectDoesHavePrivilege(
                    stem.getId(), stemScope, subjectToAssign, privilege, false, sqlLikeString);
            grouperObjectsWithPrivsToBeRemoved.addAll(GrouperUtil.nonNull(stemsWhichNeedPrivsRemoved)); 
            
          } else if (privilege.isAttributeDef()) {
            Set<AttributeDef> attributeDefsWhichNeedPrivsRemoved = GrouperSession.staticGrouperSession()
                .getAttributeDefResolver().getAttributeDefsWhereSubjectDoesHavePrivilege(
                    stem.getId(), stemScope, subjectToAssign, privilege, false, sqlLikeString);
            grouperObjectsWithPrivsToBeRemoved.addAll(GrouperUtil.nonNull(attributeDefsWhichNeedPrivsRemoved)); 
            
          } else {
            throw new RuntimeException("Not expecting privilege: " + privilege); 
          }
          
          
          GROUPER_OBJECT_LOOP:
          for (GrouperObject grouperObject : GrouperUtil.nonNull(grouperObjectsWithPrivsToBeRemoved)) {
           
            boolean immediateStem = true;
            
            Stem currentStem = grouperObject.getParentStem();
            
            //work up to root stem
            while(true) {
              
              //see if there is a SUB or ONE in parent folder
              Set<RuleDefinition> ruleDefinitions = inheritedRulesForFolderOrCache(inheritedRulesForFolderOrCache, currentStem, subjectToAssign, privilege);
              
              for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(ruleDefinitions)) {

                //if directly in the folder or if the scope is SUB
                if (immediateStem || Scope.SUB.name().equalsIgnoreCase(ruleDefinition.getCheck().getCheckStemScope())) {
                  
                  // its ok if no name pattern or if the name pattern matches
                  //see if there is a name pattern
                  //  attributeValueDelegate.assignValue(
                  //      RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.nameMatchesSqlLikeString.name());
                  //  attributeValueDelegate.assignValue(
                  //      RuleUtils.ruleIfConditionEnumArg0Name(), "a:b:%someGroup");
                  if (ruleDefinition.getIfCondition() == null || ruleDefinition.getIfCondition().getIfConditionEnum() == null ||
                      (RuleIfConditionEnum.nameMatchesSqlLikeString.name().equalsIgnoreCase(ruleDefinition.getIfCondition().getIfConditionEnum())
                          && GrouperUtil.matchSqlString(ruleDefinition.getIfCondition().getIfConditionEnumArg0(), grouperObject.getName()))) {
                    continue GROUPER_OBJECT_LOOP;
                  }
                  
                }
              }

              currentStem = currentStem.getParentStemOrNull();
              if (currentStem == null) {
                break;
              }
              immediateStem = false;
            }
            
            removedCount++;
            
            //we need to remove this privilege, since it is not in another rule
            if (privilege.isAccess()) {
              if (((Group)grouperObject).canHavePrivilege(grouperSession.getSubject(), AccessPrivilege.ADMIN.getName(), false)) {
                ((Group)grouperObject).revokePriv(subjectToAssign, privilege, false);

                if (LOG.isDebugEnabled()) {
                  LOG.debug("Revoking privilege (due to inherited priv removed and no other inherited priv assigned): " + privilege + " from subject: " + GrouperUtil.subjectToString(subjectToAssign) + " from group: " + grouperObject.getName());
                }
              }

            } else if (privilege.isNaming()) {

              if (((Stem)grouperObject).canHavePrivilege(grouperSession.getSubject(), NamingPrivilege.STEM_ADMIN.getName(), false)) {
                ((Stem)grouperObject).revokePriv(subjectToAssign, privilege, false);
  
                if (LOG.isDebugEnabled()) {
                  LOG.debug("Revoking privilege (due to inherited priv removed and no other inherited priv assigned): " + privilege + " from subject: " + GrouperUtil.subjectToString(subjectToAssign) + " from folder: " + grouperObject.getName());
                }
              }

            } else if (privilege.isAttributeDef()) {
              
              if (((AttributeDef)grouperObject).getPrivilegeDelegate().canHavePrivilege(grouperSession.getSubject(), AttributeDefPrivilege.ATTR_ADMIN.getName(), false)) {
                ((AttributeDef)grouperObject).getPrivilegeDelegate().revokePriv(subjectToAssign, privilege, false);
  
                if (LOG.isDebugEnabled()) {
                  LOG.debug("Revoking privilege (due to inherited priv removed and no other inherited priv assigned): " + privilege + " from subject: " + GrouperUtil.subjectToString(subjectToAssign) + " from attributeDef: " + grouperObject.getName());
                }
              }

            } else {
              throw new RuntimeException("Not expecting privilege: " + privilege); 
            }
            
          }
          
          
        }
        return removedCount;
      }
    });
    
  }
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleApi.class);

  /**
   * make sure group privileges are inherited in a stem
   * @param actAs
   * @param stem
   * @param stemScope ONE or SUB
   * @param subjectToAssign
   * @param privileges can use Privilege.getInstances() to convert from string
   * @param sqlLikeString 
   * @return the assignment in case there are edits
   */
  public static AttributeAssign inheritGroupPrivileges(Subject actAs, Stem stem, Scope stemScope, 
      Subject subjectToAssign, Set<Privilege> privileges, String sqlLikeString) {
    
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

    //see if pattern to further restrict
    if (!StringUtils.isBlank(sqlLikeString)) {
      attributeValueDelegate.assignValue(
          RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.nameMatchesSqlLikeString.name());
      attributeValueDelegate.assignValue(
          RuleUtils.ruleIfConditionEnumArg0Name(), sqlLikeString);
    }
    
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
        RuleCheckType.flattenedMembershipRemoveInFolder.name()); // changed to effective

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
        RuleCheckType.flattenedMembershipRemove.name()); // changed to effective
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
        RuleCheckType.flattenedMembershipRemove.name()); // changed to effective
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(),
        RuleIfConditionEnum.thisGroupHasImmediateEnabledNoEndDateMembership.name());
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
        RuleCheckType.flattenedMembershipRemove.name()); // changed to effective
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisPermissionDefHasNoEndDateAssignment.name());
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
        RuleCheckType.flattenedMembershipRemove.name()); // changed to effective
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
        RuleCheckType.flattenedMembershipRemoveInFolder.name()); // changed to effective

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

  /**
   * 
   * @param actAsSubject
   * @param permissionDef
   * @param daysInFutureDisabledDateMin
   * @param daysInFutureDisabledDateMax
   * @param emailToValue
   * @param emailSubjectValue
   * @param emailBodyValue
   * @return attribute assign for customizing
   */
  public static AttributeAssign emailOnFlattenedPermissionDisabledDate(Subject actAsSubject, 
      AttributeDef permissionDef, Integer daysInFutureDisabledDateMin, 
      Integer daysInFutureDisabledDateMax, 
      String emailToValue, String emailSubjectValue, String emailBodyValue) {
    //add a rule on the permission definition saying if you are about to lose a permission by all paths (flattened), then send an email
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAsSubject.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAsSubject.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.permissionDisabledDate.name());
    
    //will find permissions with a disabled date at least 6 days from now.  blank means no min
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg0Name(), daysInFutureDisabledDateMin == null ? null : daysInFutureDisabledDateMin.toString());

    //will find permissions with a disabled date at most 8 days from now.  blank means no max
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

  /**
   * normalize privileges if the user who creates a stem is in a group which has create privileges on the stem
   * @param actAs
   * @param ruleStem
   * @param stemScope 
   * @return the attribute assignment
   */
  public static AttributeAssign reassignStemPrivilegesIfFromGroup(Subject actAs, Stem ruleStem, Scope stemScope) {
    //add a rule on stem2 saying if you create a stem underneath, then remove admin if in another group which has create on stem
    AttributeAssign attributeAssign = ruleStem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.stemCreate.name());
    
    //can be SUB or ONE for if in this folder, or in this and all subfolders
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), stemScope.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.reassignStemPrivilegesIfFromGroup.name());
    
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

    return attributeAssign;
  }
}
