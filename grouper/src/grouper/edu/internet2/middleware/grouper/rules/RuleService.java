package edu.internet2.middleware.grouper.rules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAttrAssignDelegate;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

public class RuleService {
  
  
  /**
   * save or update rule config for a given grouper object (group/stem)
   * @param ruleConfig
   * @param grouperObject
   * @return error messages if any
   */
  public static Map<String, List<String>> saveOrUpdateRuleAttributes(RuleConfig ruleConfig, GrouperObject grouperObject, String attributeAssignId) {

    Map<String, List<String>> result = new HashMap<>();

    // SECURITY (GRP-7359): a rule that uses expression language (EL) in its if-condition or its
    // then runs that arbitrary expression as GrouperSystem when the rule fires.  There is no way
    // to constrain what arbitrary EL does, so only Grouper sysadmins (wheel/root) may add or edit
    // an EL rule.  The rules UI only hides the EL option from non-admins (a display-only control),
    // and this method is invoked inside a root session, so without this server-side check a crafted
    // request could create an EL rule and run arbitrary logic as root (privilege escalation).
    // Enforce it here, before any attribute is created or assigned, so a rejected attempt persists
    // nothing.  The subject configuring the rule is carried on the RuleConfig (the logged in user),
    // not the current (root) session.
    // Fail closed: if there is no configuring subject we cannot establish that the caller is a
    // sysadmin, so treat a null subject the same as a non-wheel user and apply the restrictions
    // below.  Every real caller (the rules UI) sets RuleConfig.subject to the logged in user, so
    // this only blocks a caller that omitted the acting subject -- it never blocks wheel/root, who
    // always have their own subject on the RuleConfig.
    Subject configuringSubject = ruleConfig == null ? null : ruleConfig.getSubject();
    if (configuringSubject == null || !PrivilegeHelper.isWheelOrRoot(configuringSubject)) {

      // SECURITY (GRP-7359): if editing an existing rule, a non-wheel caller may not modify a custom
      // or EL rule (one that matches no predefined pattern) -- the same restriction as delete.  This
      // is checked before the new content below, so a non-admin cannot overwrite a sysadmin-authored
      // custom/EL rule (e.g. downgrade it to a pattern).  attributeAssignId is blank on an add.
      if (StringUtils.isNotBlank(attributeAssignId)) {
        RuleDefinition existingRuleDefinition = null;
        for (RuleDefinition existingCandidate : RuleFinder.retrieveRuleDefinitionsForGrouperObject(grouperObject)) {
          if (existingCandidate.getAttributeAssignType() != null
              && StringUtils.equals(existingCandidate.getAttributeAssignType().getId(), attributeAssignId)) {
            existingRuleDefinition = existingCandidate;
            break;
          }
        }
        if (existingRuleDefinition != null && !allowedToManageRule(configuringSubject, existingRuleDefinition)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCustomRequiresWheel");
          if (StringUtils.isBlank(error)) {
            error = "Only a Grouper administrator can add or edit a custom rule.  "
                + "Non-administrators must use one of the predefined rule patterns.";
          }
          result.put("ERROR", Arrays.asList(error));
          return result;
        }
      }

      // an EL rule runs an arbitrary expression as GrouperSystem; there is no way to constrain what
      // it does, so it is wheel/root only.  Checked on content so it holds regardless of how the
      // request claims the rule was built.
      boolean ruleUsesEl = StringUtils.equals("EL", ruleConfig.getIfConditionOption())
          || StringUtils.isNotBlank(ruleConfig.getIfConditionEl())
          || StringUtils.equals("EL", ruleConfig.getThenOption())
          || StringUtils.isNotBlank(ruleConfig.getThenEl());
      if (ruleUsesEl) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditElRequiresWheel");
        if (StringUtils.isBlank(error)) {
          error = "Only a Grouper administrator can add or edit a rule that uses expression language (EL).";
        }
        result.put("ERROR", Arrays.asList(error));
        return result;
      }

      // a "custom" rule is a hand-authored check/if/then rather than one of the predefined,
      // constrained rule patterns, so it can specify an arbitrary check, if-condition, and then that
      // run as GrouperSystem.  Only wheel/root may add or edit a custom rule; non-admins are limited
      // to the predefined patterns (which fix the check/then and validate the caller's privilege on
      // the objects they reference).  ruleConfig.getPattern() is blank or "custom" for the custom
      // path, and is the pattern name when a pattern's save() calls through here.
      boolean isCustomRule = StringUtils.isBlank(ruleConfig.getPattern())
          || StringUtils.equals("custom", ruleConfig.getPattern());
      if (isCustomRule) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCustomRequiresWheel");
        if (StringUtils.isBlank(error)) {
          error = "Only a Grouper administrator can add or edit a custom rule.  "
              + "Non-administrators must use one of the predefined rule patterns.";
        }
        result.put("ERROR", Arrays.asList(error));
        return result;
      }

      // at this point the (non-wheel) caller named a non-blank, non-custom pattern, so it must be a
      // recognized pattern, and the caller must pass that pattern's own validate() -- which enforces
      // their privilege on the objects the rule references (e.g. the group named in the rule) plus the
      // pattern's other input constraints.  That validation historically ran only in the UI submit;
      // enforce it here so it cannot be skipped by a caller that reaches the service another way.
      RulePattern claimedRulePattern = null;
      try {
        claimedRulePattern = ruleConfig.getRulePattern();
      } catch (RuntimeException re) {
        claimedRulePattern = null;
      }
      if (claimedRulePattern == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditUnknownPattern");
        if (StringUtils.isBlank(error)) {
          error = "Unknown rule pattern.";
        }
        result.put("ERROR", Arrays.asList(error));
        return result;
      }
      List<String> patternValidationErrors = claimedRulePattern.validate(ruleConfig, configuringSubject);
      if (patternValidationErrors != null && !patternValidationErrors.isEmpty()) {
        result.put("ERROR", patternValidationErrors);
        return result;
      }
    }

    AttributeAssign attributeAssign = null;
    
    String checkOwnerName = null;
    // the uuid of whatever checkOwnerName resolved to, captured where we still know if it is a
    // group or a folder, so a rule which stores its check owner as a uuid can keep doing that
    String checkOwnerIdResolved = null;
    String checkOwnerStemScope = null;
    
    String ifConditionOwnerName = null;
    String ifConditionOwnerStemScope= null;
    
    if (StringUtils.isNotBlank(attributeAssignId)) {
      attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
    }
    
    RuleCheck ruleCheck = new RuleCheck();
    RuleIfCondition ruleIfCondition = new RuleIfCondition();
    RuleThen ruleThen = new RuleThen();
    
    if (grouperObject instanceof Group) {
      Group group = (Group) grouperObject;
      attributeAssign = attributeAssign != null ? attributeAssign : group.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
      
      //rule is being assigned on a group 
      String checkOwner = ruleConfig.getCheckOwner();
      
//      if (StringUtils.isBlank(checkOwner) && StringUtils.isNotBlank(ruleConfig.getCheckOwnerUuidOrName())) {
//        checkOwner = "anotherGroup";
//      } else if (StringUtils.isBlank(checkOwner) && StringUtils.isBlank(ruleConfig.getCheckOwnerUuidOrName())){
//        checkOwner = "thisGroup";
//      }
      
      if (StringUtils.isNotBlank(checkOwner)) {
        
        //value must be thisGroup, anotherGroup
        if (StringUtils.equals(checkOwner, "thisGroup")) {
          checkOwnerName = group.getName();
          checkOwnerIdResolved = group.getId();
        } else if (StringUtils.equals(checkOwner, "anotherGroup")) {
          String groupIdOrName = ruleConfig.getCheckOwnerUuidOrName();
          Group checkOwnerGroup = GroupFinder.findByName(groupIdOrName, false);
          if (checkOwnerGroup == null) {
            checkOwnerGroup = GroupFinder.findByUuid(groupIdOrName, false);
          }
          
          if (checkOwnerGroup != null) {
            checkOwnerName = checkOwnerGroup.getName();
            checkOwnerIdResolved = checkOwnerGroup.getId();
          } else {
            //Add error and return
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
            error = error.replace("##groupUuidOrName##", groupIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
          
        }
      } else {
        //must be folder if not blank
        
        checkOwnerStemScope = ruleConfig.getCheckOwnerStemScope();
        
        String stemIdOrName = ruleConfig.getCheckOwnerUuidOrName();
        if (StringUtils.isNotBlank(stemIdOrName)) {
          Stem stem = RuleEngine.findStemByName(stemIdOrName, false);
          if (stem == null) {
            stem = RuleEngine.findStemById(stemIdOrName, false);
          }
          
          if (stem != null) {
            checkOwnerName = stem.getName();
            checkOwnerIdResolved = stem.getUuid();
          } else {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidFolder");
            error = error.replace("##folderUuidOrName##", stemIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
        }
        
      }
      
      String ifConditionOwner = ruleConfig.getIfConditionOwner();
      
//      if (StringUtils.isBlank(ifConditionOwner) && StringUtils.isNotBlank(ruleConfig.getIfConditionOwnerUuidOrName())) {
//        ifConditionOwner = "anotherGroup";
//      } else if (StringUtils.isBlank(ifConditionOwner) && StringUtils.isBlank(ruleConfig.getIfConditionOwnerUuidOrName())){
//        ifConditionOwner = "thisGroup";
//      }
      
      if (StringUtils.isNotBlank(ifConditionOwner)) {
        
        //value must be thisStem, anotherStem
        if (StringUtils.equals(ifConditionOwner, "thisGroup")) {
          ifConditionOwnerName = group.getName();
        } else if (StringUtils.equals(ifConditionOwner, "anotherGroup")) {
          String groupIdOrName = ruleConfig.getIfConditionOwnerUuidOrName();
          Group ifConditionOwnerGroup = GroupFinder.findByName(groupIdOrName, false);
          if (ifConditionOwnerGroup == null) {
            ifConditionOwnerGroup = GroupFinder.findByUuid(groupIdOrName, false);
          }
          
          if (ifConditionOwnerGroup != null) {
            ifConditionOwnerName = ifConditionOwnerGroup.getName();
          } else {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
            error = error.replace("##groupUuidOrName##", groupIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
          
        }
      } else {
        
        ifConditionOwnerStemScope = ruleConfig.getIfConditionOwnerStemScope();
        
        //maybe be group if not blank
        String stemIdOrName = ruleConfig.getIfConditionOwnerUuidOrName();
        if (StringUtils.isNotBlank(stemIdOrName)) {
          Stem stem = RuleEngine.findStemByName(stemIdOrName, false);
          if (stem == null) {
            stem = RuleEngine.findStemById(stemIdOrName, false);
          }
          
          if (stem != null) {
            ifConditionOwnerName = stem.getName();
          } else {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidFolder");
            error = error.replace("##folderUuidOrName##", stemIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
        }
        
      }
      
      
    } else if (grouperObject instanceof Stem) {
      Stem stem = (Stem) grouperObject;
      attributeAssign = attributeAssign != null ? attributeAssign : stem.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
      
      //rule is being assigned on a folder 
      String checkOwner = ruleConfig.getCheckOwner();
      
//      if (StringUtils.isBlank(checkOwner) && StringUtils.isNotBlank(ruleConfig.getCheckOwnerUuidOrName())) {
//        checkOwner = "anotherStem";
//      } else if (StringUtils.isBlank(checkOwner) && StringUtils.isBlank(ruleConfig.getCheckOwnerUuidOrName())){
//        checkOwner = "thisStem";
//      }
      
      if (StringUtils.isNotBlank(checkOwner)) {
        
        checkOwnerStemScope = ruleConfig.getCheckOwnerStemScope();
        
        //value must be thisStem, anotherStem
        if (StringUtils.equals(checkOwner, "thisStem")) {
          checkOwnerName = stem.getName();
          checkOwnerIdResolved = stem.getUuid();
        } else if (StringUtils.equals(checkOwner, "anotherStem")) {
          String stemIdOrName = ruleConfig.getCheckOwnerUuidOrName();
          Stem checkOwnerStem = RuleEngine.findStemByName(stemIdOrName, false);
          if (checkOwnerStem == null) {
            checkOwnerStem = RuleEngine.findStemById(stemIdOrName, false);
          }
          
          if (checkOwnerStem != null) {
            checkOwnerName = checkOwnerStem.getName();
            checkOwnerIdResolved = checkOwnerStem.getUuid();
          } else {
            //Add error and return
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidFolder");
            error = error.replace("##folderUuidOrName##", stemIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
          
        }
      } else {
        //must be group if not blank
        String groupIdOrName = ruleConfig.getCheckOwnerUuidOrName();
        if (StringUtils.isNotBlank(groupIdOrName)) {
          Group group = GroupFinder.findByName(groupIdOrName, false);
          if (group == null) {
            group = GroupFinder.findByUuid(groupIdOrName, false);
          }
          
          if (group != null) {
            checkOwnerName = group.getName();
            checkOwnerIdResolved = group.getId();
          } else {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
            error = error.replace("##groupUuidOrName##", groupIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
        }
        
      }
      
      String ifConditionOwner = ruleConfig.getIfConditionOwner();
      if (StringUtils.isNotBlank(ifConditionOwner)) {
        
        ifConditionOwnerStemScope = ruleConfig.getIfConditionOwnerStemScope();
        
        //value must be thisStem, anotherStem
        if (StringUtils.equals(ifConditionOwner, "thisStem")) {
          ifConditionOwnerName = stem.getName();
        } else if (StringUtils.equals(ifConditionOwner, "anotherStem")) {
          String stemIdOrName = ruleConfig.getIfConditionOwnerUuidOrName();
          Stem ifConditionOwnerStem = RuleEngine.findStemByName(stemIdOrName, false);
          if (ifConditionOwnerStem == null) {
            ifConditionOwnerStem = RuleEngine.findStemById(stemIdOrName, false);
          }
          
          if (ifConditionOwnerStem != null) {
            ifConditionOwnerName = ifConditionOwnerStem.getName();
          } else {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidFolder");
            error = error.replace("##folderUuidOrName##", stemIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
          
        }
      } else {
        //maybe be group if not blank
        String groupIdOrName = ruleConfig.getIfConditionOwnerUuidOrName();
        if (StringUtils.isNotBlank(groupIdOrName)) {
          Group group = GroupFinder.findByName(groupIdOrName, false);
          if (group == null) {
            group = GroupFinder.findByUuid(groupIdOrName, false);
          }
          
          if (group != null) {
            ifConditionOwnerName = group.getName();
          } else {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
            error = error.replace("##groupUuidOrName##", groupIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
        }
        
      }
      
    }
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    AttributeAssignAttrAssignDelegate attributeDelegate = attributeAssign.getAttributeDelegate();
    
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), SubjectFinder.findRootSubject().getId());
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), ruleConfig.getCheckType());
    
    ruleCheck.setCheckType(ruleConfig.getCheckType());
    
    if (StringUtils.isNotBlank(checkOwnerName)) {

      // only one of checkOwnerId and checkOwnerName can be set, so whichever one we store, clear
      // the other one.  if this rule already stores the uuid (e.g. it was created with RuleApi),
      // then keep storing the uuid, even if a different group or folder was picked, so that
      // editing a rule in the UI doesnt change the way that rule stores its check owner
      boolean ruleAlreadyStoresCheckOwnerId = !StringUtils.isBlank(
          attributeValueDelegate.retrieveValueString(RuleUtils.ruleCheckOwnerIdName()));

      if (ruleAlreadyStoresCheckOwnerId && !StringUtils.isBlank(checkOwnerIdResolved)) {
        attributeValueDelegate.assignValue(RuleUtils.ruleCheckOwnerIdName(), checkOwnerIdResolved);
        attributeDelegate.removeAttributeByName(RuleUtils.ruleCheckOwnerNameName());
        ruleCheck.setCheckOwnerId(checkOwnerIdResolved);
      } else {
        attributeValueDelegate.assignValue(RuleUtils.ruleCheckOwnerNameName(), checkOwnerName);
        attributeDelegate.removeAttributeByName(RuleUtils.ruleCheckOwnerIdName());
        ruleCheck.setCheckOwnerName(checkOwnerName);
      }
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleCheckOwnerNameName());
    }
    
    if (StringUtils.isNotBlank(checkOwnerStemScope)) {
      attributeValueDelegate.assignValue(RuleUtils.ruleCheckStemScopeName(), checkOwnerStemScope);
      ruleCheck.setCheckStemScope(checkOwnerStemScope);
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleCheckStemScopeName());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getCheckArg0())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleCheckArg0Name(), ruleConfig.getCheckArg0());
      ruleCheck.setCheckArg0(ruleConfig.getCheckArg0());
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleCheckArg0Name());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getCheckArg1())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleCheckArg1Name(), ruleConfig.getCheckArg1());
      ruleCheck.setCheckArg1(ruleConfig.getCheckArg1());
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleCheckArg1Name());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getIfConditionOption())) {
      
      if (StringUtils.equals(ruleConfig.getIfConditionOption(), "EL")) {
        attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionElName(), ruleConfig.getIfConditionEl());
        ruleIfCondition.setIfConditionEl(ruleConfig.getIfConditionEl());
        attributeDelegate.removeAttributeByName(RuleUtils.ruleIfConditionEnumName());
      } else {
        attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumName(), ruleConfig.getIfConditionOption());
        ruleIfCondition.setIfConditionEnum(ruleConfig.getIfConditionOption());
        attributeDelegate.removeAttributeByName(RuleUtils.ruleIfConditionElName());
      }
      
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleIfConditionElName());
      attributeDelegate.removeAttributeByName(RuleUtils.ruleIfConditionEnumName());
    }
    
    if (StringUtils.isNotBlank(ifConditionOwnerName)) {
      attributeValueDelegate.assignValue(RuleUtils.ruleIfOwnerNameName(), ifConditionOwnerName);
      ruleIfCondition.setIfOwnerName(ifConditionOwnerName);
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleIfOwnerNameName());
    }
    
    if (StringUtils.isNotBlank(ifConditionOwnerStemScope)) {
      attributeValueDelegate.assignValue(RuleUtils.ruleIfStemScopeName(), ifConditionOwnerStemScope);
      ruleIfCondition.setIfStemScope(ifConditionOwnerStemScope);
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleIfStemScopeName());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getIfConditionArg0())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumArg0Name(), ruleConfig.getIfConditionArg0());
      ruleIfCondition.setIfConditionEnumArg0(ruleConfig.getIfConditionArg0());
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleIfConditionEnumArg0Name());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getIfConditionArg1())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumArg1Name(), ruleConfig.getIfConditionArg1());
      ruleIfCondition.setIfConditionEnumArg1(ruleConfig.getIfConditionArg1());
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleIfConditionEnumArg1Name());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getThenOption())) {
      
      if (StringUtils.equals(ruleConfig.getThenOption(), "EL")) {
        attributeValueDelegate.assignValue(RuleUtils.ruleThenElName(), ruleConfig.getThenEl());
        ruleThen.setThenEl(ruleConfig.getThenEl());
        attributeDelegate.removeAttributeByName(RuleUtils.ruleThenEnumName());
      } else {
        attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), ruleConfig.getThenOption());
        ruleThen.setThenEnum(ruleConfig.getThenOption());
        attributeDelegate.removeAttributeByName(RuleUtils.ruleThenElName());
      }
      
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleThenElName());
      attributeDelegate.removeAttributeByName(RuleUtils.ruleThenEnumName());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getThenArg0())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), ruleConfig.getThenArg0());
      ruleThen.setThenEnumArg0(ruleConfig.getThenArg0());
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleThenEnumArg0Name());
    }
    
    
    if (StringUtils.isNotBlank(ruleConfig.getThenArg1())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), ruleConfig.getThenArg1());
      ruleThen.setThenEnumArg1(ruleConfig.getThenArg1());
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleThenEnumArg1Name());
    }
    
    
    if (StringUtils.isNotBlank(ruleConfig.getThenArg2())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg2Name(), ruleConfig.getThenArg2());
      ruleThen.setThenEnumArg2(ruleConfig.getThenArg2());
    } else {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleThenEnumArg2Name());
    }
    
    if (ruleConfig.getRunDaemon() == null) {
      attributeDelegate.removeAttributeByName(RuleUtils.ruleRunDaemonName());
    } else {
      if (!ruleConfig.getRunDaemon()) {
        attributeValueDelegate.assignValue(RuleUtils.ruleRunDaemonName(), "F");
      } else {
        attributeValueDelegate.assignValue(RuleUtils.ruleRunDaemonName(), "T");
      }
    }
   
    
    RuleDefinition ruleDefinition = new RuleDefinition();
    AttributeAssign attributeAssign2 = new AttributeAssign();
    
    if (grouperObject instanceof Group) {
      attributeAssign2.setOwnerGroupId(grouperObject.getId());
    } else if (grouperObject instanceof Stem){
      attributeAssign2.setOwnerStemId(grouperObject.getId());
    }
    
    ruleDefinition.setAttributeAssignType(attributeAssign2);
    
    ruleDefinition.setCheck(ruleCheck);
    ruleDefinition.setIfCondition(ruleIfCondition);
    ruleDefinition.setThen(ruleThen);

    // DESIGN (GRP-7359): rules created through the UI always run as GrouperSystem so they keep working
    // regardless of who created them or whether that person later loses privileges.  Because this runs
    // in a root session, RuleSubjectActAs.allowedToActAs (evaluated during validate() below) always
    // sees the current subject as GrouperSystem and passes -- it is NOT the authorization gate for the
    // UI path.  Authorization for the UI path is enforced above: EL and custom (hand-authored) rules
    // are wheel/root only, and a non-wheel caller is limited to the predefined patterns and must pass
    // the selected pattern's validate() (which checks their privilege on the objects the rule
    // references).  allowedToActAs remains the meaningful gate on the WS/GSH/RuleApi paths, which run
    // under the caller's own session and can specify a non-root actAs.
    RuleSubjectActAs actAs = new RuleSubjectActAs();
    actAs.setSourceId("g:isa");
    actAs.setSubjectId(SubjectFinder.findRootSubject().getId());
    ruleDefinition.setActAs(actAs);
    
    String error = ruleDefinition.validate();
    
    if (StringUtils.isBlank(error)) {
      try {
        
        if (ruleConfig.getRunDaemon() != null) {
          ruleDefinition.getCheck().checkTypeEnum().canRunDeamon(ruleDefinition);
        }
      } catch (Exception e) {
        error = "This rule does not support a daemon so you cannot set run daemon to true";
      }
    } else {
//      String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
//      error = error.replace("##groupUuidOrName##", groupIdOrName);
      result.put("ERROR", Arrays.asList(error));
      return result;
    }

    // SECURITY (GRP-7359): a non-wheel user is limited to the predefined patterns.  The rule is now
    // fully built, so verify it actually matches the pattern the request claimed -- a manipulated
    // request could otherwise set fields that make the persisted rule differ from its pattern.  A
    // blank/"custom" pattern was already rejected for non-wheel at the top.  On a mismatch, remove
    // whatever was assigned and reject so nothing durable remains.
    if (configuringSubject != null && !PrivilegeHelper.isWheelOrRoot(configuringSubject)
        && !ruleMatchesClaimedPattern(ruleDefinition, ruleConfig.getPattern())) {
      attributeAssign.delete();
      String patternError = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditPatternMismatch");
      if (StringUtils.isBlank(patternError)) {
        patternError = "This rule does not match the selected pattern.";
      }
      result.put("ERROR", Arrays.asList(patternError));
      return result;
    }

    attributeAssign.saveOrUpdate();

    String validValue = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    if (!StringUtils.equals(validValue, "T")) {
      String info = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditRuleSavedButNotValid");
      result.put("WARN", Arrays.asList(info));
      return result;
    } 
    
    String info = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditSuccess");
    result.put("SUCCESS", Arrays.asList(info));
    return result;

  }
  
  public static RulePattern calculateRulePattern(RuleConfig ruleConfig) {
    
    
    
    return null;
    
  }
  
  /**
   * retrieve type setting for a given grouper object (group/stem) and target name.
   * @param grouperObject
   * @param attributeAssignId
   * @param loggedInSubject
   * @return
   */
  public static RuleConfig getRuleConfig(GrouperObject grouperObject, String attributeAssignId, Subject loggedInSubject) {
    
    RuleConfig ruleConfig = new RuleConfig(loggedInSubject, grouperObject);
     
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
//    if (attributeAssign != null) {
//      return buildGrouperProvisioningAttributeValue(attributeAssign);
//    }
//    
//    if (!(grouperObject instanceof Group) && !(grouperObject instanceof Stem)) {
//      return null;
//    }
    
    
    Set<AttributeAssign> attributeAssigns = attributeAssign == null ? new HashSet<>() : attributeAssign.getAttributeDelegate().retrieveAssignments();
    
    RuleCheckType ruleCheckType = null;
    String ruleCheckOwnerName = null;
    
    for (AttributeAssign attributeAssignSingle: attributeAssigns) {
      
      String value = attributeAssignSingle.getValueDelegate().retrieveValueString();
      
      if (StringUtils.equals(RuleUtils.ruleCheckTypeName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setCheckType(value);
        
        //an invalid rule can have a check type that isnt an enum, and it needs to be editable so it can be fixed
        ruleCheckType = RuleCheckType.valueOfIgnoreCase(value, true, false);
        
      } else if (StringUtils.equals(RuleUtils.ruleCheckArg0Name(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setCheckArg0(value);
      } else if (StringUtils.equals(RuleUtils.ruleCheckArg1Name(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setCheckArg1(value);
      } else if (StringUtils.equals(RuleUtils.ruleCheckOwnerNameName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        
        ruleCheckOwnerName = value;
//        if (grouperObject instanceof Stem && StringUtils.equals(grouperObject.getName(), value)) {
//          ruleConfig.setCheckOwner("thisStem");
//        } else if (grouperObject instanceof Group && StringUtils.equals(grouperObject.getName(), value)) {
//          ruleConfig.setCheckOwner("thisGroup");
//        }
//        
//        ruleConfig.setCheckOwnerUuidOrName(value);
      } else if (StringUtils.equals(RuleUtils.ruleCheckStemScopeName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setCheckOwnerStemScope(value);
      } else if (StringUtils.equals(RuleUtils.ruleIfConditionElName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setIfConditionEl(value);
        ruleConfig.setIfConditionOption("EL");
      } else if (StringUtils.equals(RuleUtils.ruleIfConditionEnumArg0Name(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setIfConditionArg0(value);
      } else if (StringUtils.equals(RuleUtils.ruleIfConditionEnumArg1Name(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setIfConditionArg1(value);
      } else if (StringUtils.equals(RuleUtils.ruleIfConditionEnumName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setIfConditionOption(value);
      } else if (StringUtils.equals(RuleUtils.ruleIfOwnerNameName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        
        if (grouperObject instanceof Stem && StringUtils.equals(grouperObject.getName(), value)) {
          ruleConfig.setIfConditionOwner("thisStem");
        } else if (grouperObject instanceof Group && StringUtils.equals(grouperObject.getName(), value)) {
          ruleConfig.setIfConditionOwner("thisGroup");
        }
        
        ruleConfig.setIfConditionOwnerUuidOrName(value);
      } else if (StringUtils.equals(RuleUtils.ruleIfStemScopeName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setIfConditionOwnerStemScope(value);
      } else if (StringUtils.equals(RuleUtils.ruleThenElName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setThenEl(value);
        ruleConfig.setThenOption("EL");
      } else if (StringUtils.equals(RuleUtils.ruleThenEnumArg0Name(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setThenArg0(value);
      } else if (StringUtils.equals(RuleUtils.ruleThenEnumArg1Name(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setThenArg1(value);
      } else if (StringUtils.equals(RuleUtils.ruleThenEnumArg2Name(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setThenArg2(value);
      } else if (StringUtils.equals(RuleUtils.ruleThenEnumName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        ruleConfig.setThenOption(value);
      } else if (StringUtils.equals(RuleUtils.ruleRunDaemonName(),  attributeAssignSingle.getAttributeDefName().getName())) {
        if (StringUtils.equals(value, "F")) {
          ruleConfig.setRunDaemon(false);
        }
      }
      
    }
    
    if (ruleCheckType != null) {
      RuleOwnerType ownerType = ruleCheckType.getOwnerType();
      if (ownerType != null && ownerType == RuleOwnerType.FOLDER && grouperObject instanceof Stem) {
        if (grouperObject instanceof Stem && StringUtils.equals(grouperObject.getName(), ruleCheckOwnerName)) {
          ruleConfig.setCheckOwner("thisStem");
        } else {
          ruleConfig.setCheckOwner("anotherStem");
        }
      }
      
      if (ownerType != null && ownerType == RuleOwnerType.GROUP && grouperObject instanceof Group) {
        if (grouperObject instanceof Stem && StringUtils.equals(grouperObject.getName(), ruleCheckOwnerName)) {
          ruleConfig.setCheckOwner("thisGroup");
        } else {
          ruleConfig.setCheckOwner("anotherGroup");
        }
      }
    }
    
    ruleConfig.setCheckOwnerUuidOrName(ruleCheckOwnerName);
    
    return ruleConfig;
  }

  /**
   * SECURITY (GRP-7359): whether the given (real, logged in) subject may edit or delete an existing
   * rule.  A rule runs as GrouperSystem, so managing a "custom" rule -- one that does not match any
   * predefined pattern (this includes every EL rule, since patterns use fixed enums) -- is wheel/root
   * only.  A rule that matches a recognized pattern may be edited/deleted by a non-admin who otherwise
   * has rights on the object (that object-level check is enforced by the caller).  Evaluated on the
   * passed subject, not the current session, because these operations run inside a root session.
   * @param subject the real user requesting the edit or delete
   * @param ruleDefinition the existing rule being edited or deleted
   * @return true if the subject may edit or delete this rule
   */
  public static boolean allowedToManageRule(Subject subject, RuleDefinition ruleDefinition) {
    if (subject == null || ruleDefinition == null) {
      return false;
    }
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    // non-admins may only edit or delete rules that match a recognized (constrained) pattern
    return ruleDefinition.getPattern() != null;
  }

  /**
   * SECURITY (GRP-7381): whether the given (real, logged in) subject may create a sendEmail rule.
   * A sendEmail rule sends email to arbitrary recipients with an arbitrary subject/body that is
   * evaluated as expression language (ruleElUtils) at fire time as GrouperSystem, so this is a
   * default-closed, layered gate rather than the old default-open check:
   * <ul>
   * <li>wheel/root: always allowed;</li>
   * <li>else if rules.restrictRulesEmailSendersToMembersOfThisGroupName is set: only members of it;</li>
   * <li>else if rules.restrictRulesUiToMembersOfThisGroupName is set: only members of it (the vetted
   *     rule-editor group -- a subject who reached the rules UI is already a member);</li>
   * <li>else (both blank): only wheel/root (returned above), so deny.</li>
   * </ul>
   * A configured-but-missing group fails closed.  Evaluated on the passed subject, not the current
   * session, since the save runs inside a root session.
   * @param subject the real user creating the sendEmail rule
   * @return true if the subject may create a sendEmail rule
   */
  public static boolean allowedToCreateEmailRule(Subject subject) {
    if (subject == null) {
      return false;
    }
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    // the email-specific designation wins when it is set
    String emailSenderGroupName = grouperConfig.propertyValueString("rules.restrictRulesEmailSendersToMembersOfThisGroupName", "");
    if (StringUtils.isNotBlank(emailSenderGroupName)) {
      Group emailSenderGroup = GroupFinder.findByName(emailSenderGroupName, false);
      return emailSenderGroup != null && emailSenderGroup.hasMember(subject);
    }
    // otherwise, if the rules UI itself is gated to a group, its members are vetted rule editors
    String restrictRulesUiGroupName = grouperConfig.propertyValueString("rules.restrictRulesUiToMembersOfThisGroupName", "");
    if (StringUtils.isNotBlank(restrictRulesUiGroupName)) {
      Group restrictRulesUiGroup = GroupFinder.findByName(restrictRulesUiGroupName, false);
      return restrictRulesUiGroup != null && restrictRulesUiGroup.hasMember(subject);
    }
    // both gates blank: default-closed (wheel/root only), unless a site has intentionally opted out of
    // the check via rules.allowSendEmailRulesWhenNoGroupConfigured=true (which restores the previous open behavior)
    if (grouperConfig.propertyValueBoolean("rules.allowSendEmailRulesWhenNoGroupConfigured", false)) {
      return true;
    }
    return false;
  }

  /**
   * SECURITY (GRP-7359): whether a built rule actually matches the pattern the request claims it is.
   * Non-admins may only create/edit rules through the predefined patterns, so after building the rule
   * we verify it matches the claimed pattern -- a manipulated request cannot persist a rule that
   * differs from the pattern it names.  A blank or "custom" claimed pattern is handled by the separate
   * custom-rule gate, so it is treated as a match here.
   * @param ruleDefinition the rule that was built
   * @param claimedPatternName the pattern the request claims (RuleConfig.getPattern())
   * @return true if the rule matches the claimed pattern (or no pattern was claimed)
   */
  public static boolean ruleMatchesClaimedPattern(RuleDefinition ruleDefinition, String claimedPatternName) {
    if (StringUtils.isBlank(claimedPatternName) || StringUtils.equals("custom", claimedPatternName)) {
      return true;
    }
    RulePattern claimedPattern = null;
    try {
      claimedPattern = RulePattern.valueOf(claimedPatternName);
    } catch (Exception e) {
      // an unknown pattern name is never a match
      return false;
    }
    RulePattern actualPattern = ruleDefinition == null ? null : ruleDefinition.getPattern();
    return actualPattern == claimedPattern;
  }


  public static void deleteRuleAttributes(String attributeAssignId) {
    // The rule marker attribute assign may already have been deleted before we get here.
    // When a folder/group/attributeDef is deleted, the delete logic first sweeps every
    // attribute assign owned by that object (Stem.delete / Group.delete / AttributeDef.delete),
    // which removes the rule marker assign.  It then loops over rule definitions found in the
    // RuleEngine cache and calls this method.  That cache is stale because the delete path
    // deliberately does not clear it: the value hook (GrouperAttributeAssignValueRulesConfigHook)
    // early-returns when a rule type is being torn down, so it never reaches clearRuleEngineCache().
    // So look the assign up tolerantly (do not throw if it is already gone) and only delete it
    // if it still exists, but always clear the rule engine cache to flush the stale definition.
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, false);
    if (attributeAssign != null) {
      attributeAssign.delete();
    }
    RuleEngine.clearRuleEngineCache();
  }

}
