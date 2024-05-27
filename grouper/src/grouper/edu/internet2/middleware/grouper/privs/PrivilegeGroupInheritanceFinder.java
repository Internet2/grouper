package edu.internet2.middleware.grouper.privs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleFinder;
import edu.internet2.middleware.grouper.rules.RuleIfCondition;
import edu.internet2.middleware.grouper.rules.RuleSubjectActAs;
import edu.internet2.middleware.grouper.rules.RuleThen;
import edu.internet2.middleware.grouper.rules.RuleThenEnum;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;

public class PrivilegeGroupInheritanceFinder {
  
  private String stemId;
  
  private String stemName;
  
  private Stem stem;
  
  private Subject subject;
  
  private String subjectId;
  
  private String subjectSourceId;
  
  private String subjectIdentifier;
  
  private Privilege privilege;
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public PrivilegeGroupInheritanceFinder assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }

  /**
   * add privilege
   * @param theFieldName
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignPrivilege(Privilege privilege) {
    this.privilege = privilege;
    return this;
  }

  /**
   * add privilege
   * @param theFieldName
   * @return this for chaining
   */
  private PrivilegeGroupInheritanceFinder assignPrivilegeHelper(Privilege privilege, String searchFor) {
    if (privilege == null || !privilege.isAccess()) {
      throw new RuntimeException("Cant find field" + (StringUtils.isBlank(searchFor) ? "" : " '" + searchFor + "',") + " expecting: " 
          + Field.FIELD_NAME_ADMINS + ", "
          + Field.FIELD_NAME_VIEWERS + ", "
          + Field.FIELD_NAME_GROUP_ATTR_READERS + ", "
          + Field.FIELD_NAME_GROUP_ATTR_UPDATERS + ", "
           + Field.FIELD_NAME_READERS + ", "
           + Field.FIELD_NAME_UPDATERS + ", "
           + Field.FIELD_NAME_OPTINS + ", "
          + Field.FIELD_NAME_OPTOUTS);
    }
    this.privilege = privilege;
    return this;
  }

  /**
   * field of privilege (could be privilege name too)
   * @param thePrivilegeName
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignPrivilegeName(String thePrivilegeName) {
    Privilege privilege = Privilege.listToPriv(thePrivilegeName, false);
    if (privilege == null) {
      privilege = Privilege.getInstance(thePrivilegeName, false);
    }
    this.assignPrivilegeHelper(privilege, thePrivilegeName);    
    return this;
  }

  /**
   * field of privilege
   * @param theFieldId
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignField(Field theField) {
    this.assignFieldName(theField.getName());
    return this;
  }

  /**
   * field of privilege
   * @param theFieldId
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignFieldId(String theFieldId) {
    
    Field field = FieldFinder.findById(theFieldId, true);
    
    this.assignField(field);
    return this;
  }

  /**
   * field of privilege (could be privilege name too)
   * @param theFieldName
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignFieldName(String theFieldName) {
    this.assignPrivilegeName(theFieldName);
    return this;
  }

  /**
   * assign a stem
   * @param theStem
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignStem(Stem theStem) {
    this.stem = theStem;
    return this;
  }

  /**
   * stem id to add to, mutually exclusive with stem name
   * @param theStemId
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignStemId(String theStemId) {
    this.stemId = theStemId;
    return this;
  }

  /**
   * stem name to add to, mutually exclusive with stem id
   * @param theStemName
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignStemName(String theStemName) {
    this.stemName = theStemName;
    return this;
  }

  /**
   * subject to add
   * @param theSubject
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignSubject(Subject theSubject) {
    this.subject = theSubject;
    return this;
  }

  /**
   * subject id to add, mutually exclusive and preferable to subject identifier
   * @param theSubjectId
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignSubjectId(String theSubjectId) {
    this.subjectId = theSubjectId;
    return this;
  }

  /**
   * subject identifier to add, mutually exclusive and not preferable to subject id
   * @param thesubjectIdentifier
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignSubjectIdentifier(String theSubjectIdentifier) {
    this.subjectIdentifier = theSubjectIdentifier;
    return this;
  }

  /**
   * subject source id to add
   * @param theSubjectSourceId
   * @return this for chaining
   */
  public PrivilegeGroupInheritanceFinder assignSubjectSourceId(String theSubjectSourceId) {
    this.subjectSourceId = theSubjectSourceId;
    return this;
  }
  
  /**
   * return true if the user has this inherited privilege based on assignment to the user or a group the user is in.
   * The inherited privilege could be assigned on the folder or an ancestor folder.
   * @return
   */
  public boolean hasAssignedPrivilege() {
    GrouperUtil.assertion(PrivilegeGroupInheritanceFinder.this.privilege != null,  "You must pass the privilege to the finder");
    return findAssignedPrivileges().contains(this.privilege);
    
  }
  
  /**
   * return true if the user has this inherited privilege based on assignment to the user or a group the user is in.
   * The inherited privilege could be assigned on the folder or an ancestor folder.
   * The privilege could be the actual privilege or a privilege that the assigned privilege implies. e.g. if searching for view
   * and the subject has admin, it would return true 
   * @return
   */
  public boolean hasEffectivePrivilege() {
    GrouperUtil.assertion(PrivilegeGroupInheritanceFinder.this.privilege != null,  "You must pass the privilege to the finder");
    
    return findEffectivePrivileges().contains(this.privilege);
  }
  
  /**
   * return set of effective inherited privileges based on assignment to the user or a group the user is in.
   * The inherited privileges could be assigned on the folder or an ancestor folder.
   * The privileges could be the actual privileges or privileges that the assigned privilege implies. e.g. if the subject has admin, 
   * it would include view and other privileges as well.
   * @return
   */
  public Set<Privilege> findEffectivePrivileges() {
    
    Set<Privilege> assignedPrivileges = findAssignedPrivileges();
    Set<Privilege> effectivePrivileges = new HashSet<>();
    
    for (Privilege assignedPrivilege: assignedPrivileges) {
      effectivePrivileges.addAll(assignedPrivilege.getImpliedPrivileges());
    }
    return effectivePrivileges;
  }
  
  /**
   * return set of assigned inherited privileges based on assignment to the user or a group the user is in.
   * The inherited privileges could be assigned on the folder or an ancestor folder.
   * @return
   */
  @SuppressWarnings("unchecked")
  public Set<Privilege> findAssignedPrivileges() {
    
    final Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();
    
    return (Set<Privilege>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (stem == null && !StringUtils.isBlank(PrivilegeGroupInheritanceFinder.this.stemId)) {
          stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), PrivilegeGroupInheritanceFinder.this.stemId, false, new QueryOptions().secondLevelCache(false));
        } 
        if (stem == null && !StringUtils.isBlank(PrivilegeGroupInheritanceFinder.this.stemName)) {
          stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), PrivilegeGroupInheritanceFinder.this.stemName, false, new QueryOptions().secondLevelCache(false));
        }
        GrouperUtil.assertion(stem!=null,  "Stem not found");

  
        if (subject == null && !StringUtils.isBlank(subjectId) && !StringUtils.isBlank(subjectSourceId)) {
          subject = SubjectFinder.findByIdAndSource(PrivilegeGroupInheritanceFinder.this.subjectId, PrivilegeGroupInheritanceFinder.this.subjectSourceId, false);
        }            
        if (subject == null && !StringUtils.isBlank(subjectId) && StringUtils.isBlank(subjectSourceId)) {
          subject = SubjectFinder.findById(PrivilegeGroupInheritanceFinder.this.subjectId, false);
        }            
        if (subject == null && !StringUtils.isBlank(subjectIdentifier) && !StringUtils.isBlank(subjectSourceId)) {
          subject = SubjectFinder.findByIdentifierAndSource(PrivilegeGroupInheritanceFinder.this.subjectIdentifier, PrivilegeGroupInheritanceFinder.this.subjectSourceId, false);
        }
        if (subject == null && !StringUtils.isBlank(subjectIdentifier) && StringUtils.isBlank(subjectSourceId)) {
          subject = SubjectFinder.findByIdentifier(PrivilegeGroupInheritanceFinder.this.subjectIdentifier, false);
        }
        GrouperUtil.assertion(subject!=null,  "Subject not found");
          
        subjectId = subject.getId();
        subjectSourceId = subject.getSourceId();
        
        if (!runAsRoot) {
          if (!stem.canHavePrivilege(SUBJECT_IN_SESSION, NamingPrivilege.STEM_ADMIN.getName(), false)) {
            throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION) 
            + "' cannot ADMIN stem '" + stem.getName() + "'");
          }
        }

        String inputSubjectString = subjectSourceId + " :::: " + subjectId;
        Subject rootSubject = SubjectFinder.findRootSubject();
        
        // lets see whats already there
        Set<Privilege> alreadyAssignedPrivileges = new HashSet<Privilege>();
        
        Set<String> groupIds = new HashSet<>();
        
        Set<String> parentStemNamesPlusPassedInStemName = GrouperUtil.findParentStemNames(stem.getName());
        parentStemNamesPlusPassedInStemName.add(stem.getName());
        
        Set<Stem> stems = new StemFinder().assignStemNames(parentStemNamesPlusPassedInStemName).findStems();
        Set<String> stemIds = new HashSet<>();
        
        Set<RuleDefinition> stemRuleDefinitions = new HashSet<>();
        
        for (Stem stemLocal: stems) {
          stemIds.add(stemLocal.getId());
          stemRuleDefinitions.addAll(GrouperUtil.nonNull(RuleFinder.findGroupPrivilegeInheritRules(stem)));
        }
        
        List<RuleDefinition> matchingDefinitions = new ArrayList<>();
        
        //find applicable inheritance rules and collect the list of group ids
        for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(stemRuleDefinitions)) {

          AttributeAssign attributeAssign = ruleDefinition.getAttributeAssignType();
          
          if (ruleDefinition.getCheck() == null) {
            continue;
          }

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
          
          if (!StringUtils.equals(ruleDefinition.getCheck().getCheckType(), RuleCheckType.groupCreate.name())) {
            continue;
          }
         
          RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
            
          if (ruleIfCondition != null && !ruleIfCondition.isBlank()) {
            continue;
          }
              
          RuleThen ruleThen = ruleDefinition.getThen();
          if (ruleThen == null) {
            continue;
          }
          
          if (!StringUtils.equals(ruleThen.getThenEnum(), RuleThenEnum.assignGroupPrivilegeToGroupId.name())) {
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
        
          String thisSubjectString = ruleThen.getThenEnumArg0();
          if (!StringUtils.equals(thisSubjectString, inputSubjectString) && !StringUtils.startsWith(thisSubjectString, "g:gsa :::: ")) {
            continue;
          }
          
          //  //should be valid
          //  String isValidString = attributeValueDelegate.retrieveValueString(
          //      RuleUtils.ruleValidName());
          if (!ruleDefinition.isValidInAttributes()) {
            continue;
          }

          String[] subjectSourceAndSubjectId = thisSubjectString.split("::::");
          String subjectSourceId = StringUtils.trim(subjectSourceAndSubjectId[0]);
          String subjectId = StringUtils.trim(subjectSourceAndSubjectId[1]);
          
          if (StringUtils.equals("g:gsa", subjectSourceId)) {
            groupIds.add(subjectId);
          }
          
          matchingDefinitions.add(ruleDefinition);
          
        }
        
        MembershipResult membershipResult = new MembershipFinder().assignGroupIds(groupIds).addSubject(subject).findMembershipResult();
        Set<String> groupIdsTheUserIsMemberOf = membershipResult.groupIds();
        
        // find assigned privileges to the user or a group the user is in
        for (RuleDefinition ruleDefinition : matchingDefinitions) {

          RuleThen ruleThen = ruleDefinition.getThen();
        
          String thisSubjectString = ruleThen.getThenEnumArg0();

          String[] subjectSourceAndSubjectId = thisSubjectString.split("::::");
          String subjectSourceId = StringUtils.trim(subjectSourceAndSubjectId[0]);
          String subjectId = StringUtils.trim(subjectSourceAndSubjectId[1]);
          
          if (StringUtils.equals("g:gsa", subjectSourceId) && !groupIdsTheUserIsMemberOf.contains(subjectId)) {
            continue;
          }
          
          //  //privileges to assign: read, admin, update, view, optin, optout
          //  attributeValueDelegate.assignValue(
          //      RuleUtils.ruleThenEnumArg1Name(), Privilege.stringValue(privileges));
          for (String privilege : GrouperUtil.nonNull(GrouperUtil.splitTrim(ruleThen.getThenEnumArg1(), ","), String.class)) {
            alreadyAssignedPrivileges.add(Privilege.getInstance(privilege, false));
          }
          
        }
        
        return alreadyAssignedPrivileges;
      }
    });
    
  }

}
