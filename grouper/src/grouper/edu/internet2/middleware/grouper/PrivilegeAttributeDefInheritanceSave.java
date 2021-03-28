/**
 * 
 */
package edu.internet2.middleware.grouper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.RuleApi;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.RuleFinder;
import edu.internet2.middleware.grouper.rules.RuleIfCondition;
import edu.internet2.middleware.grouper.rules.RuleIfConditionEnum;
import edu.internet2.middleware.grouper.rules.RuleSubjectActAs;
import edu.internet2.middleware.grouper.rules.RuleThen;
import edu.internet2.middleware.grouper.rules.RuleThenEnum;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * <p>Use this class to add/edit/delete privileges on attribute defs inside a folder for a subject</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * SaveResultType saveResultType = new PrivilegeAttributeDefInheritanceSave()
      .assignStem(stem)
      .assignStemScope(Scope.SUB)
      .assignSubject(subject)
      .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
      .save();
 * System.out.println(saveResultType); // INSERT, DELETE, NO_CHANGE, or UPDATE
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to delete privileges on attribute defs inside a folder for a subject
 * <blockquote>
 * <pre>
 * new PrivilegeAttributeDefInheritanceSave()
 *    .assignStem(stem)
 *    .assignStemScope(Scope.SUB)
 *    .assignSubject(subject)
 *    .assignSaveMode(SaveMode.DELETE)
 *    .save();
 * </pre>
 * </blockquote>
 * </p>
 */
public class PrivilegeAttributeDefInheritanceSave {
  
  /**
   * rule attribute assign id to delete
   */
  private String attributeAssignId;
  
  /**
   * @param theAttributeAssignId
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignAttributeAssignId(String theAttributeAssignId) {
    this.attributeAssignId = theAttributeAssignId;
    return this;
  }
  
  /**
   * @param theAttributeAssignId
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignAttributeAssign(AttributeAssign theAttributeAssign) {
    this.attributeAssignId = theAttributeAssign.getId();
    return this;
  }
  
  /**
   * privileges
   */
  private Set<Privilege> privileges = new HashSet<Privilege>();

  /**
   * member to add
   */
  private Member member = null;
  
  /**
   * member id to add
   */
  private String memberId;

  /** 
   * save mode.
   * Delete is remove privs.  If you dont specify privs it will remove all.  If you dont specify stem scope it will remove all stem scopes 
   * Insert is add privs (error if they are already there).  
   * Update is replace existing privs with new list. 
   * Insert or update just adds some, and if there no error
   */
  private SaveMode saveMode;
  
  /** save type after the save */
  private SaveResultType saveResultType = null;
  
  /**
   * default to sub
   */
  private Scope stemScope = null;
  
  /**
   * subject to add
   */
  private Subject subject = null;
  /**
   * subject id to add, mutually exclusive and preferable to subject identifier
   */
  private String subjectId;
  /**
   * subject identifier to add, mutually exclusive and not preferable to subject id
   */
  private String subjectIdentifier;
  /**
   * subject source id to add
   */
  private String subjectSourceId;

  /**
   * stem
   */
  private Stem stem = null;

  /**
   * stem id to add to, mutually exclusive with stem name
   */
  private String stemId;

  /**
   * stem name to add to, mutually exclusive with stem id
   */
  private String stemName;

  /**
   * 
   */
  public PrivilegeAttributeDefInheritanceSave() {
    
  }

  /**
   * field of privilege
   * @param theFieldId
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave addField(Field theField) {
    this.addFieldName(theField.getName());
    return this;
  }

  /**
   * field of privilege
   * @param theFieldId
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave addFieldId(String theFieldId) {
    
    Field field = FieldFinder.findById(theFieldId, true);
    
    this.addField(field);
    return this;
  }

  /**
   * field of privilege (could be privilege name too)
   * @param theFieldName
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave addFieldName(String theFieldName) {
    this.addPrivilegeName(theFieldName);
    return this;
  }

  /**
   * add privilege
   * @param theFieldName
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave addPrivilege(Privilege privilege) {
    return this.addPrivilegeHelper(privilege, null);
  }

  /**
   * add privilege
   * @param theFieldName
   * @return this for chaining
   */
  private PrivilegeAttributeDefInheritanceSave addPrivilegeHelper(Privilege privilege, String searchFor) {
    if (privilege == null || !privilege.isAttributeDef()) {
      throw new RuntimeException("Cant find field" + (StringUtils.isBlank(searchFor) ? "" : " '" + searchFor + "',") + " expecting: " 
          + Field.FIELD_NAME_ATTR_ADMINS + ", "
          + Field.FIELD_NAME_ATTR_OPTINS + ", "
          + Field.FIELD_NAME_ATTR_OPTOUTS + ", "
          + Field.FIELD_NAME_ATTR_UPDATERS + ", "
          + Field.FIELD_NAME_ATTR_VIEWERS + ", "
          + Field.FIELD_NAME_ATTR_DEF_ATTR_READERS + ", "
          + Field.FIELD_NAME_ATTR_DEF_ATTR_UPDATERS + ", "
          + Field.FIELD_NAME_ATTR_READERS);
    }
    this.privileges.add(privilege);    
    return this;
  }

  /**
   * field of privilege (could be privilege name too)
   * @param thePrivilegeName
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave addPrivilegeName(String thePrivilegeName) {
    Privilege privilege = Privilege.listToPriv(thePrivilegeName, false);
    if (privilege == null) {
      privilege = Privilege.getInstance(thePrivilegeName, false);
    }
    this.addPrivilegeHelper(privilege, thePrivilegeName);    
    return this;
  }

  /**
   * member to add
   * @param member
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignMember(Member theMember) {
    this.member = theMember;
    return this;
  }

  /**
   * member id to add
   * @param theMemberId
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignMemberId(String theMemberId) {
    this.memberId = theMemberId;
    return this;
  }

  /**
   * save mode.
   * Delete is remove privs.  If you dont specify privs it will remove all.  If you dont specify stem scope it will remove all stem scopes 
   * Insert is add privs (error if they are already there).  
   * Update is replace existing privs with new list. 
   * Insert or update just adds some, and if there no error
   * @param theSaveMode
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /**
   * subject to add
   * @param theSubject
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignSubject(Subject theSubject) {
    this.subject = theSubject;
    return this;
  }

  /**
   * subject id to add, mutually exclusive and preferable to subject identifier
   * @param theSubjectId
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignSubjectId(String theSubjectId) {
    this.subjectId = theSubjectId;
    return this;
  }

  /**
   * subject identifier to add, mutually exclusive and not preferable to subject id
   * @param thesubjectIdentifier
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignSubjectIdentifier(String theSubjectIdentifier) {
    this.subjectIdentifier = theSubjectIdentifier;
    return this;
  }

  /**
   * subject source id to add
   * @param theSubjectSourceId
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignSubjectSourceId(String theSubjectSourceId) {
    this.subjectSourceId = theSubjectSourceId;
    return this;
  }

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }

  /**
   * only do this for certain like strings
   */
  private String nameMatchesSqlLikeString;
  
  /**
   * only do this for certain like strings
   * @param theNameMatchesSqlLikeString
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignNameMatchesSqlLikeString(String theNameMatchesSqlLikeString) {
    this.nameMatchesSqlLikeString = theNameMatchesSqlLikeString;
    return this;
  }
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public PrivilegeAttributeDefInheritanceSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  /**
   * <pre>
   * create or update or delete a composite
   * </pre>
   * @return the composite that was updated or created or deleted
   */
  public SaveResultType save() throws InsufficientPrivilegeException, GroupNotFoundException {
  
    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
  
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
    
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          final Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();
          
          return GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              boolean deletingAttributeAssign = saveMode == SaveMode.DELETE && !StringUtils.isBlank(attributeAssignId);

              if (stem == null && !StringUtils.isBlank(PrivilegeAttributeDefInheritanceSave.this.stemId)) {
                stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), PrivilegeAttributeDefInheritanceSave.this.stemId, false, new QueryOptions().secondLevelCache(false));
              } 
              if (stem == null && !StringUtils.isBlank(PrivilegeAttributeDefInheritanceSave.this.stemName)) {
                stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), PrivilegeAttributeDefInheritanceSave.this.stemName, false, new QueryOptions().secondLevelCache(false));
              }
              GrouperUtil.assertion(stem!=null,  "Stem not found");

              if (!deletingAttributeAssign) {
                if (member == null && !StringUtils.isBlank(PrivilegeAttributeDefInheritanceSave.this.memberId)) {
                  member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), PrivilegeAttributeDefInheritanceSave.this.memberId, false);
                }
        
                if (subject == null && !StringUtils.isBlank(subjectId) && !StringUtils.isBlank(subjectSourceId)) {
                  subject = SubjectFinder.findByIdAndSource(PrivilegeAttributeDefInheritanceSave.this.subjectId, PrivilegeAttributeDefInheritanceSave.this.subjectSourceId, false);
                }            
                if (subject == null && !StringUtils.isBlank(subjectIdentifier) && !StringUtils.isBlank(subjectSourceId)) {
                  subject = SubjectFinder.findByIdentifierAndSource(PrivilegeAttributeDefInheritanceSave.this.subjectIdentifier, PrivilegeAttributeDefInheritanceSave.this.subjectSourceId, false);
                }
                if (subject == null && member != null) {
                  subject = member.getSubject();
                }
                //  if (member == null && subject != null) {
                //    member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, saveMode!=SaveMode.DELETE);
                //  }
                //  GrouperUtil.assertion(member!=null,  "Member not found");
                GrouperUtil.assertion(subject!=null,  "Subject not found");
                  
                subjectId = subject.getId();
                subjectSourceId = subject.getSourceId();
              }
              
              if (!runAsRoot) {
                if (!stem.canHavePrivilege(SUBJECT_IN_SESSION, NamingPrivilege.STEM_ADMIN.getName(), false)) {
                  throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION) 
                  + "' cannot ADMIN stem '" + stem.getName() + "'");
                }
              }

              Set<RuleDefinition> attributeDefRuleDefinitions = RuleFinder.findAttributeDefPrivilegeInheritRules(stem);
              
              String inputSubjectString = subjectSourceId + " :::: " + subjectId;
              Subject rootSubject = SubjectFinder.findRootSubject();
              
              // lets see whats already there
              Set<Privilege> alreadyAssignedPrivileges = new HashSet<Privilege>();
              
              List<RuleDefinition> matchingDefinitions = new ArrayList<RuleDefinition>();

              // default to sub if not deleting
              if (stemScope == null && saveMode != SaveMode.DELETE) {
                stemScope = Scope.SUB;
              }

              Map<RuleDefinition, Set<Privilege>> matchingDefinitionToPrivileges = new HashMap<RuleDefinition, Set<Privilege>>();

              for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(attributeDefRuleDefinitions)) {

                AttributeAssign attributeAssign = ruleDefinition.getAttributeAssignType();
                if (deletingAttributeAssign && StringUtils.equals(attributeAssignId, attributeAssign.getId())) {
                  matchingDefinitions.add(ruleDefinition);
                  break;
                }
                
                if (ruleDefinition.getCheck() == null) {
                  continue;
                }

                //  attributeValueDelegate.assignValue(
                //      RuleUtils.ruleActAsSubjectSourceIdName(), SubjectFinder.findRootSubject().getSourceId());
                //  attributeValueDelegate.assignValue(
                //      RuleUtils.ruleActAsSubjectIdName(), SubjectFinder.findRootSubject().getId());
                RuleSubjectActAs ruleSubjectActAs = ruleDefinition.getActAs();
                if (ruleSubjectActAs == null) {
                  continue;
                }
                if (!StringUtils.equals(ruleSubjectActAs.getSourceId(), rootSubject.getSourceId())) {
                  continue;
                }
                if (!StringUtils.equals(ruleSubjectActAs.getSubjectId(), rootSubject.getId())) {
                  continue;
                }
                
                //  attributeValueDelegate.assignValue(
                //      RuleUtils.ruleCheckTypeName(), RuleCheckType.groupCreate.name());
                if (!StringUtils.equals(ruleDefinition.getCheck().getCheckType(), RuleCheckType.stemCreate.name())) {
                  continue;
                }
                
                //  //can be SUB or ONE for if in this folder, or in this and all subfolders
                //  attributeValueDelegate.assignValue(
                //      RuleUtils.ruleCheckStemScopeName(), stemScope.name());
                Scope thisStemScope = Scope.valueOfIgnoreCase(ruleDefinition.getCheck().getCheckStemScope(), true);
                
                // if we are delete and null then continue
                if (saveMode != SaveMode.DELETE || stemScope != null) {
                  if (PrivilegeAttributeDefInheritanceSave.this.stemScope != thisStemScope) {
                    continue;
                  }
                }
                
                //  if (!StringUtils.isBlank(sqlLikeString)) {
                //    attributeValueDelegate.assignValue(
                //        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.nameMatchesSqlLikeString.name());
                //    attributeValueDelegate.assignValue(
                //        RuleUtils.ruleIfConditionEnumArg0Name(), sqlLikeString);
                //  }
                RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
                String existingNameMatchesSqlLikeString = ruleIfCondition == null ? null : ruleIfCondition.getIfConditionEnumArg0();
                boolean hasIfConditionEnum = !StringUtils.isBlank(ruleIfCondition == null ? null : ruleIfCondition.getIfConditionEnum());
                if (!StringUtils.isBlank(nameMatchesSqlLikeString) || !StringUtils.isBlank(existingNameMatchesSqlLikeString) || hasIfConditionEnum) {
                  
                  boolean ifConditionIsNameMatches = StringUtils.equals(
                      ruleIfCondition == null ? null : ruleIfCondition.getIfConditionEnum(), RuleIfConditionEnum.nameMatchesSqlLikeString.name());
                  
                  if (!ifConditionIsNameMatches) {
                    continue;
                  }
                  if (!StringUtils.equals(nameMatchesSqlLikeString, existingNameMatchesSqlLikeString)) {
                    continue;
                  }
                  
                }
                    
                

                //  attributeValueDelegate.assignValue(
                //      RuleUtils.ruleThenEnumName(), RuleThenEnum.assignGroupPrivilegeToGroupId.name());
                RuleThen ruleThen = ruleDefinition.getThen();
                if (ruleThen == null) {
                  continue;
                }
                
                if (!StringUtils.equals(ruleThen.getThenEnum(), RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId.name())) {
                  continue;
                }

                //  //this is the subject string for the subject to assign to
                //  //e.g. sourceId :::::: subjectIdentifier
                //  //or sourceId :::: subjectId
                //  //or :::: subjectId
                //  //or sourceId ::::::: subjectIdOrIdentifier
                //  //etc
                //  attributeValueDelegate.assignValue(
                //      RuleUtils.ruleThenEnumArg0Name(), subjectToAssign.getSourceId() + " :::: " + subjectToAssign.getId());
                if (ruleDefinition.getCheck() == null) {
                  continue;
                }
                String thisSubjectString = ruleThen.getThenEnumArg0();
                if (!StringUtils.equals(thisSubjectString, inputSubjectString)) {
                  continue;
                }
                
                //  //should be valid
                //  String isValidString = attributeValueDelegate.retrieveValueString(
                //      RuleUtils.ruleValidName());
                if (!ruleDefinition.isValidInAttributes()) {
                  continue;
                }

                //  //privileges to assign: read, admin, update, view, optin, optout
                //  attributeValueDelegate.assignValue(
                //      RuleUtils.ruleThenEnumArg1Name(), Privilege.stringValue(privileges));
                Set<Privilege> thisRulePrivileges = new HashSet<Privilege>();
                matchingDefinitionToPrivileges.put(ruleDefinition, thisRulePrivileges);
                for (String privilege : GrouperUtil.nonNull(GrouperUtil.splitTrim(ruleThen.getThenEnumArg1(), ","), String.class)) {
                  thisRulePrivileges.add(Privilege.getInstance(privilege, false));
                }
                alreadyAssignedPrivileges.addAll(thisRulePrivileges);
                
                matchingDefinitions.add(ruleDefinition);
                
              }
              
              Set<Privilege> privilegesRemoved = new HashSet<Privilege>();
              Set<Privilege> privilegesAdded = new HashSet<Privilege>();
              
              Set<Privilege> privsInGrouperMissingInRequest = new HashSet<Privilege>(alreadyAssignedPrivileges);
              
              privsInGrouperMissingInRequest.removeAll(privileges);

              Set<Privilege> privsInRequestMissingInGrouper = new HashSet<Privilege>(privileges);
              
              privsInRequestMissingInGrouper.removeAll(alreadyAssignedPrivileges);

              Set<Privilege> privsInRequestAndInGrouper = new HashSet<Privilege>(privileges);
              
              privsInRequestAndInGrouper.retainAll(alreadyAssignedPrivileges);

              // lets find all the no change situations
              if (saveMode == SaveMode.DELETE && matchingDefinitions.size() == 0) {
                saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              if (saveMode == SaveMode.DELETE && privsInRequestAndInGrouper.size() == 0) {
                saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              if (saveMode == SaveMode.INSERT && privsInRequestAndInGrouper.size() > 0) {
                throw new RuntimeException("inserting privs and they already exist!");
              }
              // if they are all there
              if (saveMode == SaveMode.UPDATE && privsInRequestAndInGrouper.size() == privileges.size()) {
                saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              if (saveMode == SaveMode.INSERT_OR_UPDATE && privsInRequestMissingInGrouper.size() == 0) {
                saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              
              
              if (saveMode == SaveMode.DELETE) {
                boolean removeSome = false;
                boolean removeAll = false;
                if (privileges.size() > 0) {
                  // we need to remove all privs
                  if (privsInRequestAndInGrouper.size() == privileges.size()) {
                    removeAll = true;
                    saveResultType = SaveResultType.DELETE;
                  } else {
                    //we need to remove some privs
                    removeSome = true;
                  }
                } else {
                  removeAll = true;
                }
                
                Iterator<RuleDefinition> iterator = matchingDefinitions.iterator();
                while (iterator.hasNext()) {
                  RuleDefinition ruleDefinition = iterator.next();
                  boolean remove = removeAll;
                  Set<Privilege> thisRulePrivileges = matchingDefinitionToPrivileges.get(ruleDefinition);
                  if (removeSome) {
                    Set<Privilege> privsInThisRuleNotInRequest = new HashSet<Privilege>(thisRulePrivileges);
                    privsInThisRuleNotInRequest.removeAll(privileges);
                    
                    if (privsInThisRuleNotInRequest.size() == 0) {
                      remove = true;
                    } else {
                      if (privsInThisRuleNotInRequest.size() < thisRulePrivileges.size()) {
                        Set<Privilege> privsToRemoveThisRule = new HashSet<Privilege>(thisRulePrivileges);
                        privsToRemoveThisRule.retainAll(privileges);
                        privilegesRemoved.addAll(privsToRemoveThisRule);

                        // remove some
                        AttributeValueDelegate attributeValueDelegate = ruleDefinition.getAttributeAssignType().getAttributeValueDelegate();
                        attributeValueDelegate.assignValue(
                            RuleUtils.ruleThenEnumArg1Name(), Privilege.stringValue(privsInThisRuleNotInRequest));
                        thisRulePrivileges.clear();
                        thisRulePrivileges.addAll(privsInThisRuleNotInRequest);
                        
                        //should be valid
                        String isValidString = attributeValueDelegate.retrieveValueString(
                            RuleUtils.ruleValidName());
                        GrouperUtil.assertion("T".equals(isValidString), isValidString);
                        saveResultType = SaveResultType.DELETE;
                      }
                    }
                  }
                  if (remove) {
                    privilegesRemoved.addAll(thisRulePrivileges);
                    ruleDefinition.getAttributeAssignType().delete();
                    saveResultType = SaveResultType.DELETE;
                    iterator.remove();
                  }
                }
              } else {
                //otherwise we might be adding some
                if (matchingDefinitions.size() == 0) {
                  privilegesAdded.addAll(privileges);
                  RuleApi.inheritAttributeDefPrivileges(stem, stemScope, subject, privileges);
                  saveResultType = SaveResultType.INSERT;
                } else {
                  // add to existing rule
                  RuleDefinition ruleDefinition = matchingDefinitions.get(0);
                  AttributeValueDelegate attributeValueDelegate = ruleDefinition.getAttributeAssignType().getAttributeValueDelegate();
                  Set<Privilege> thisRulePrivileges = matchingDefinitionToPrivileges.get(ruleDefinition);
                  privilegesAdded.addAll(privsInRequestMissingInGrouper);
                  thisRulePrivileges.addAll(privsInRequestMissingInGrouper);
                  attributeValueDelegate.assignValue(
                      RuleUtils.ruleThenEnumArg1Name(), Privilege.stringValue(thisRulePrivileges));

                  //should be valid
                  String isValidString = attributeValueDelegate.retrieveValueString(
                      RuleUtils.ruleValidName());
                  GrouperUtil.assertion("T".equals(isValidString), isValidString);
                  if (saveResultType == null) {
                    saveResultType = SaveResultType.INSERT;
                  }
                  
                }
                // if we are update we might be removing some
                if (saveMode == SaveMode.UPDATE && privsInGrouperMissingInRequest.size() > 0) {
                  Iterator<RuleDefinition> iterator = matchingDefinitions.iterator();
                  while (iterator.hasNext()) {
                    
                    RuleDefinition ruleDefinition = iterator.next();

                    Set<Privilege> thisRulePrivileges = matchingDefinitionToPrivileges.get(ruleDefinition);
                    Set<Privilege> privsInThisRuleNotInRequest = new HashSet<Privilege>(thisRulePrivileges);
                    privsInThisRuleNotInRequest.removeAll(privileges);
                    if (privsInThisRuleNotInRequest.size() == 0) {
                      continue;
                    } else if (privsInThisRuleNotInRequest.size() == thisRulePrivileges.size()) {
                      privilegesRemoved.addAll(thisRulePrivileges);
                      iterator.remove();
                      ruleDefinition.getAttributeAssignType().delete();
                      saveResultType = SaveResultType.UPDATE;
                    } else {
                      
                      Set<Privilege> privsToRemoveThisRule = new HashSet<Privilege>(thisRulePrivileges);
                      privsToRemoveThisRule.removeAll(privileges);
                      privilegesRemoved.addAll(privsToRemoveThisRule);
                      
                      Set<Privilege> privsInThisRuleAndInRequest = new HashSet<Privilege>(thisRulePrivileges);
                      privsInThisRuleAndInRequest.retainAll(privileges);
                      // remove some
                      AttributeValueDelegate attributeValueDelegate = ruleDefinition.getAttributeAssignType().getAttributeValueDelegate();
                      thisRulePrivileges.clear();
                      thisRulePrivileges.addAll(privsInThisRuleAndInRequest);
                      attributeValueDelegate.assignValue(
                          RuleUtils.ruleThenEnumArg1Name(), Privilege.stringValue(thisRulePrivileges));

                      //should be valid
                      String isValidString = attributeValueDelegate.retrieveValueString(
                          RuleUtils.ruleValidName());
                      GrouperUtil.assertion("T".equals(isValidString), isValidString);
                      saveResultType = SaveResultType.UPDATE;
                    }
                  }
                  
                }
                
              }
                
              if (saveResultType == null) {
                saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              
              if (privilegesAdded.size() > 0 || privilegesRemoved.size() > 0) {
                RuleEngine.clearRuleEngineCache();
              }
              
              // TODO do this in thread optionally?
              if (privilegesAdded.size() > 0) {
                RuleApi.runRulesForOwner(stem);
              }
              
              if (privilegesRemoved.size() > 0) {
                if (GrouperUiConfigInApi.retrieveConfig().propertyValueBoolean("uiV2.grouperRule.removeInheritedPrivileges.whenUnassigned", true)) {
                  
                  boolean actAsRoot = GrouperUiConfigInApi.retrieveConfig().propertyValueBoolean("uiV2.grouperRule.removeInheritedPrivileges.asRoot", true);
                  
                  RuleApi.removePrivilegesIfNotAssignedByRule(actAsRoot, stem, 
                      GrouperUtil.defaultIfNull(stemScope, Scope.SUB), subject, privilegesRemoved, 
                      nameMatchesSqlLikeString);
                }                          
              }
              
              return null;
            }
          });
        }
      });
    return saveResultType;
  }

  /**
   * assign a stem
   * @param theStem
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignStem(Stem theStem) {
    this.stem = theStem;
    return this;
  }
  
  /**
   * assign a stem scope, default to SUB
   * @param theStem
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignStemScope(Scope theScope) {
    this.stemScope = theScope;
    return this;
  }

  /**
   * assign a stemscope, default to SUB
   * @param theStem
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignStemScopeName(String theScope) {
    this.stemScope = Scope.valueOfIgnoreCase(theScope, true);
    return this;
  }


  /**
   * stem id to add to, mutually exclusive with stem name
   * @param theStemId
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignStemId(String theStemId) {
    this.stemId = theStemId;
    return this;
  }

  /**
   * stem name to add to, mutually exclusive with stem id
   * @param theStemName
   * @return this for chaining
   */
  public PrivilegeAttributeDefInheritanceSave assignStemName(String theStemName) {
    this.stemName = theStemName;
    return this;
  }

}
