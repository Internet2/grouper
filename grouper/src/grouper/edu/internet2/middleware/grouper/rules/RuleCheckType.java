package edu.internet2.middleware.grouper.rules;

import java.util.HashSet;
import java.util.Map;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.beans.RulesAttributeDefBean;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.rules.beans.RulesGroupBean;
import edu.internet2.middleware.grouper.rules.beans.RulesMembershipBean;
import edu.internet2.middleware.grouper.rules.beans.RulesStemBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * type of checking for rules
 * @author mchyzer
 *
 */
public enum RuleCheckType {

  /** if there is a membership remove flattened */
  flattenedMembershipRemove {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembership(this, ruleEngine, rulesBean);
    }

    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {

      RulesMembershipBean rulesMembershipBean = (RulesMembershipBean)rulesBean;
      if (rulesMembershipBean != null) {
        Group group = rulesMembershipBean.getGroup();
        variableMap.put("groupId", group.getId());
        variableMap.put("groupName", group.getName());
        if (hasAccessToElApi) {
          variableMap.put("group", group);
        }
      }
      if (!StringUtils.isBlank(rulesMembershipBean.getMemberId())) {
        variableMap.put("memberId", rulesMembershipBean.getMemberId());
        if (hasAccessToElApi) {
          Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession().internal_getRootSession(), 
              rulesMembershipBean.getMemberId(), false);
          if (member != null) {
            variableMap.put("member", member);
          }
        }
      }
      Membership membership = rulesMembershipBean.getMembership();
      if (membership != null) {
        if (hasAccessToElApi) {
          variableMap.put("membership", membership);
        }
        variableMap.put("membershipId", membership.getUuid());
      }
      Subject subject = rulesMembershipBean.getSubject();
      if (subject != null) {
        if (hasAccessToElApi) {
          variableMap.put("subject", subject);
        }
        variableMap.put("subjectId", subject.getId());
        variableMap.put("sourceId", subject.getSourceId());
      }
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(ruleDefinition, ruleCheck, false, true, false);
    }

    
  },
  
  /** if there is a membership remove in transaction of remove */
  membershipRemove {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembership(this, ruleEngine, rulesBean);
      
    }

    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(ruleDefinition, ruleCheck, false, true, false);
    }

    /**
     * 
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      RuleEngine ruleEngine = RuleEngine.ruleEngine();
      
      //lets get the if enum
      RuleIfConditionEnum ruleIfConditionEnum = ruleDefinition.getIfCondition().ifConditionEnum();
      
      switch (ruleIfConditionEnum) {
        
        case thisGroupHasImmediateEnabledMembership:
          
          //so basically, for the memberships in this group, where there is none in the other group, process them
          String thisGroupId = ruleDefinition.getAttributeAssignType().getOwnerGroupId();
          
          GrouperSession rootSession = GrouperSession.startRootSession(false);
          try {
            
            Group group = null;
            if (!StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerId())) {
              group = GroupFinder.findByUuid(rootSession, ruleDefinition.getCheck().getCheckOwnerId(), false);
            } else if (!StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerName())) {
              group = GroupFinder.findByName(rootSession, ruleDefinition.getCheck().getCheckOwnerName(), false);
            }

            if (group == null) {
              throw new RuntimeException("Group doesnt exist in rule! " + ruleDefinition);
            }

            //find the members which apply
            Set<Member> memberOrphans = GrouperDAOFactory.getFactory().getMembership().findAllMembersInOneGroupNotOtherAndType(thisGroupId, group.getUuid(), 
                MembershipType.IMMEDIATE.name(), null, null, true);

            for (Member member : GrouperUtil.nonNull(memberOrphans)) {

              RulesMembershipBean rulesMembershipBean = new RulesMembershipBean(member, group, member.getSubject());
              
              //fire the rule then clause
              RuleEngine.ruleFirings++;
              
              ruleDefinition.getThen().fireRule(ruleDefinition, ruleEngine, rulesMembershipBean, null);
              
            }
            
          } finally {
            GrouperSession.stopQuietly(rootSession);
          }

          break;
        default:
          if (!StringUtils.isBlank(ruleDefinition.getRunDaemon()) && ruleDefinition.isRunDaemonBoolean()) {
            throw new RuntimeException("This rule is explicitly set to run a daemon, but it is not implemented");
          }
      }
      
      
    }


  },
  
  /** if there is a membership remove in transaction of remove of a gropu in a stem */
  membershipRemoveInFolder {

    /**
     * validate this check type
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(ruleDefinition, ruleCheck, true, false, true);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembershipInFolder(this, ruleEngine, rulesBean);
      
    }

    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

  },
  
  /** if a group is created */
  groupCreate {
    
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a stem
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerStemId())) {

          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this stem
          Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ruleDefinition.getAttributeAssignType().getOwnerStemId(), true);
          ruleCheck.setCheckOwnerName(stem.getName());
        }
      }
      return ruleCheck;
    }

    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(ruleDefinition, ruleCheck, true, false, true);
    }

    /**
     * 
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      RuleThenEnum ruleThenEnum = ruleDefinition.getThen().thenEnum();
      
      if (ruleThenEnum != RuleThenEnum.assignGroupPrivilegeToGroupId) {
        throw new RuntimeException("RuleThenEnum needs to be " + RuleThenEnum.assignGroupPrivilegeToGroupId);
      }
      
      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      Subject subject = SubjectFinder.findByPackedSubjectString(subjectString, true);
      String privilegesString = ruleDefinition.getThen().getThenEnumArg1();
      
      Set<String> privilegesStringSet = GrouperUtil.splitTrimToSet(privilegesString, ",");
      
      Set<Privilege> privilegeSet = new HashSet<Privilege>();
      for (String privilegeString: privilegesStringSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        privilegeSet.add(privilege);
      }
      
      //so basically, for the memberships in this group, where there is none in the other group, process them
      String stemId = ruleDefinition.getAttributeAssignType().getOwnerStemId();
      
      Scope scope = ruleDefinition.getCheck().stemScopeEnum();
      
      for (Privilege privilege : privilegeSet) {
      
        Set<Group> groupsWhichNeedPrivs = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectDoesntHavePrivilege(
            stemId, scope, subject, privilege, false);
        
        for (Group group : GrouperUtil.nonNull(groupsWhichNeedPrivs)) {
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Daemon granting privilege: " + privilege + " to subject: " + GrouperUtil.subjectToString(subject) + " to group: " + group);
          }
          group.grantPriv(subject, privilege, false);
          
        }
        
      }
      
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      RulesGroupBean rulesGroupBean = (RulesGroupBean)rulesBean;
      
      Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
      
      //by id
      RuleCheck ruleCheck = new RuleCheck(this.name(), 
          rulesGroupBean.getGroup().getId(), rulesGroupBean.getGroup().getName(), null);
    
      ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
      
      return ruleDefinitions;
    }

    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      RulesGroupBean rulesGroupBean = (RulesGroupBean)rulesBean;
      if (rulesGroupBean != null) {
        Group group = rulesGroupBean.getGroup();
        variableMap.put("groupId", group.getId());
        variableMap.put("groupName", group.getName());
        if (hasAccessToElApi) {
          variableMap.put("group", group);
        }
      }
    }
   
  },
  
  /** if a stem is created */
  stemCreate {
    
    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(ruleDefinition, ruleCheck, true, false, true);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      RulesStemBean rulesStemBean = (RulesStemBean)rulesBean;
      
      Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
      
      //by id
      RuleCheck ruleCheck = new RuleCheck(this.name(), 
          rulesStemBean.getStem().getUuid(), rulesStemBean.getStem().getName(), null);
    
      ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
      
      return ruleDefinitions;
    }

    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a stem
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerStemId())) {

          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this stem
          Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ruleDefinition.getAttributeAssignType().getOwnerStemId(), true);
          ruleCheck.setCheckOwnerName(stem.getName());
        }
      }
      return ruleCheck;
    }

    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      RulesStemBean rulesStemBean = (RulesStemBean)rulesBean;
      if (rulesStemBean != null) {
        Stem stem = rulesStemBean.getStem();
        variableMap.put("stemId", stem.getUuid());
        variableMap.put("stemName", stem.getName());
        if (hasAccessToElApi) {
          variableMap.put("stem", stem);
        }
      }
    }
   
    /**
     * 
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      RuleThenEnum ruleThenEnum = ruleDefinition.getThen().thenEnum();
      
      if (ruleThenEnum != RuleThenEnum.assignStemPrivilegeToStemId) {
        throw new RuntimeException("RuleThenEnum needs to be " + RuleThenEnum.assignStemPrivilegeToStemId);
      }
      
      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      Subject subject = SubjectFinder.findByPackedSubjectString(subjectString, true);
      String privilegesString = ruleDefinition.getThen().getThenEnumArg1();
      
      Set<String> privilegesStringSet = GrouperUtil.splitTrimToSet(privilegesString, ",");
      
      Set<Privilege> privilegeSet = new HashSet<Privilege>();
      for (String privilegeString: privilegesStringSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        privilegeSet.add(privilege);
      }
      
      //so basically, for the memberships in this group, where there is none in the other group, process them
      String stemId = ruleDefinition.getAttributeAssignType().getOwnerStemId();
      
      Scope scope = ruleDefinition.getCheck().stemScopeEnum();
      
      for (Privilege privilege : privilegeSet) {
      
        Set<Stem> stemsWhichNeedPrivs = GrouperSession.staticGrouperSession().getNamingResolver().getStemsWhereSubjectDoesntHavePrivilege(
            stemId, scope, subject, privilege, false);
        
        for (Stem stem : GrouperUtil.nonNull(stemsWhichNeedPrivs)) {
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Daemon granting privilege: " + privilege + " to subject: " + GrouperUtil.subjectToString(subject) + " to stem: " + stem);
          }
          stem.grantPriv(subject, privilege, false);
          
        }
        
      }
      
    }

  }, 
  
  /** if there is a membership add in transaction of remove */
  membershipAdd{
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembership(this, ruleEngine, rulesBean);
      
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipAdd.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(ruleDefinition, ruleCheck, false, true, false);
    }

  }, 
  
  /** if there is a membership remove flattened */
  flattenedMembershipAdd{
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembership(this, ruleEngine, rulesBean);
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
  
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(ruleDefinition, ruleCheck, false, true, false);
    }

  }, 
  
  /** if there is a membership remove in transaction of remove of a group in a stem */
  membershipAddInFolder{
  
    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(ruleDefinition, ruleCheck, true, false, true);
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembershipInFolder(this, ruleEngine, rulesBean);
      
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }
  }, 
  
  /** if a group is created */
  attributeDefCreate{
    
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {

      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a stem
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerStemId())) {
  
          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this stem
          Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ruleDefinition.getAttributeAssignType().getOwnerStemId(), true);
          ruleCheck.setCheckOwnerName(stem.getName());
        }
      }
      return ruleCheck;
    }
  
    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(ruleDefinition, ruleCheck, true, false, true);
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      RulesAttributeDefBean rulesAttributeDefBean = (RulesAttributeDefBean)rulesBean;
      
      Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
      
      //by id
      RuleCheck ruleCheck = new RuleCheck(this.name(), 
          rulesAttributeDefBean.getAttributeDef().getId(), rulesAttributeDefBean.getAttributeDef().getName(), null);
    
      ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
      
      return ruleDefinitions;
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      RulesAttributeDefBean rulesAttributeDefBean = (RulesAttributeDefBean)rulesBean;
      if (rulesAttributeDefBean != null) {
        AttributeDef attributeDef = rulesAttributeDefBean.getAttributeDef();
        variableMap.put("attributeDefId", attributeDef.getId());
        variableMap.put("attributeDefName", attributeDef.getName());
        if (hasAccessToElApi) {
          variableMap.put("attributeDef", attributeDef);
        }
      }
    }
   
    /**
     * 
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      RuleThenEnum ruleThenEnum = ruleDefinition.getThen().thenEnum();
      
      if (ruleThenEnum != RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId) {
        throw new RuntimeException("RuleThenEnum needs to be " + RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId);
      }
      
      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      Subject subject = SubjectFinder.findByPackedSubjectString(subjectString, true);
      String privilegesString = ruleDefinition.getThen().getThenEnumArg1();
      
      Set<String> privilegesStringSet = GrouperUtil.splitTrimToSet(privilegesString, ",");
      
      Set<Privilege> privilegeSet = new HashSet<Privilege>();
      for (String privilegeString: privilegesStringSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        privilegeSet.add(privilege);
      }
      
      //so basically, for the memberships in this group, where there is none in the other group, process them
      String stemId = ruleDefinition.getAttributeAssignType().getOwnerStemId();
      
      Scope scope = ruleDefinition.getCheck().stemScopeEnum();
      
      for (Privilege privilege : privilegeSet) {
      
        Set<AttributeDef> attributeDefsWhichNeedPrivs = GrouperSession.staticGrouperSession().getAttributeDefResolver()
          .getAttributeDefsWhereSubjectDoesntHavePrivilege(
            stemId, scope, subject, privilege, false);
        
        for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefsWhichNeedPrivs)) {
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Daemon granting privilege: " + privilege 
                + " to subject: " + GrouperUtil.subjectToString(subject) + " to attributeDef: " + attributeDef);
          }
          attributeDef.getPrivilegeDelegate().grantPriv(subject, privilege, false);
          
        }
        
      }
      
    }
  };

  /**
   * get the check key for the index
   * @param ruleDefinition
   * @return the rule check for the index
   */
  public RuleCheck checkKey(RuleDefinition ruleDefinition) {
    return ruleDefinition.getCheck();
  }
  
  /**
   * validate this check type
   * @param ruleDefinition
   * @param ruleCheck 
   * @return the error or null if valid
   */
  public abstract String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck);

  /**
   * get the check object from the rules bean
   * @param ruleEngine
   * @param rulesBean
   * @return the rules
   */
  public abstract Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean);

  /**
   * add EL variables to the substitute map
   * @param ruleDefinition 
   * @param variableMap
   * @param rulesBean 
   * @param hasAccessToElApi 
   */
  public abstract void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
      RulesBean rulesBean, boolean hasAccessToElApi);

  
  /**
   * validate this check type
   * @param ruleDefinition
   * @param ruleCheck 
   * @param requireStemScope true to require, false to require blank
   * @param ownerIsGroup 
   * @param ownerIsStem 
   * @return the error or null if valid
   */
  public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck, boolean requireStemScope, boolean ownerIsGroup, boolean ownerIsStem) {
    if (!StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && !StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
      return "Enter one and only one of checkOwnerId and checkOwnerName!";
    }
    //if owner is not stem and doesnt have an owner, then that is bad 
    if (!ownerIsStem && StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
      return "Enter one and only one of checkOwnerId and checkOwnerName!";
    }
    if (requireStemScope) {
      if (StringUtils.isBlank(ruleCheck.getCheckStemScope())) {
        return "Enter the checkStemScope of ALL or SUB for the folder based rule!";
      }
      try {
        Stem.Scope.valueOfIgnoreCase(ruleCheck.getCheckStemScope(), true);
      } catch (Exception e) {
        return e.getMessage();
      }
    } else {
      if (!StringUtils.isBlank(ruleCheck.getCheckStemScope())) {
        return "Cant put checkStemScope in this ruleCheckType, not allowed";
      }
    }
    
    if (ownerIsGroup) {
      String result = ruleCheck.validateOwnerGroup();
      if (!StringUtils.isBlank(result)) {
        return result;
      }
    }
    
    if (ownerIsStem) {
      String result = ruleCheck.validateOwnerStem(ruleDefinition);
      if (!StringUtils.isBlank(result)) {
        return result;
      }
    }
    
    //if there is a stem scope, then owner is stem
    return null;
  }
  
  /**
   * for a membership remove, get the rules
   * @param ruleCheckType 
   * @param ruleEngine 
   * @param rulesBean 
   * @return rule definitions
   */
  private static Set<RuleDefinition> ruleDefinitionsMembership(RuleCheckType ruleCheckType, RuleEngine ruleEngine, RulesBean rulesBean) {
    
    RulesMembershipBean rulesMembershipBean = (RulesMembershipBean)rulesBean;
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //by id
    RuleCheck ruleCheck = new RuleCheck(ruleCheckType.name(), 
        rulesMembershipBean.getGroup().getId(), rulesMembershipBean.getGroup().getName(), null);
  
    ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrId(ruleCheck)));
    
    return ruleDefinitions;
  }

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static RuleCheckType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(RuleCheckType.class, 
        string, exceptionOnNull);

  }
  
  /**
   * for a membership remove, get the rules
   * @param ruleCheckType 
   * @param ruleEngine 
   * @param rulesBean 
   * @return rule definitions
   */
  private static Set<RuleDefinition> ruleDefinitionsMembershipInFolder(RuleCheckType ruleCheckType, RuleEngine ruleEngine, RulesBean rulesBean) {
    
    RulesMembershipBean rulesMembershipBean = (RulesMembershipBean)rulesBean;
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //by id
    RuleCheck ruleCheck = new RuleCheck(ruleCheckType.name(), 
        rulesMembershipBean.getGroup().getId(), rulesMembershipBean.getGroup().getName(), null);

    ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
    
    return ruleDefinitions;
  }

  /**
   * run the daemon to sync up the state
   * @param ruleDefinition
   */
  public void runDaemon(RuleDefinition ruleDefinition) {
    throw new RuntimeException("Not implemented daemon: " + ruleDefinition);
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleCheckType.class);

}
