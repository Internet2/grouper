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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAttrAssignDelegate;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
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
    AttributeAssign attributeAssign = null;
    
    String checkOwnerName = null;
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
      if (StringUtils.isNotBlank(checkOwner)) {
        
        //value must be thisStem, anotherStem
        if (StringUtils.equals(checkOwner, "thisGroup")) {
          checkOwnerName = group.getName();
        } else if (StringUtils.equals(checkOwner, "anotherGroup")) {
          String groupIdOrName = ruleConfig.getCheckOwnerUuidOrName();
          Group checkOwnerGroup = GroupFinder.findByName(groupIdOrName, false);
          if (checkOwnerGroup == null) {
            checkOwnerGroup = GroupFinder.findByUuid(groupIdOrName, false);
          }
          
          if (checkOwnerGroup != null) {
            checkOwnerName = checkOwnerGroup.getName();
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
          Stem stem = StemFinder.findByName(stemIdOrName, false);
          if (stem == null) {
            stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemIdOrName, false);
          }
          
          if (stem != null) {
            checkOwnerName = stem.getName();
          } else {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidFolder");
            error = error.replace("##folderUuidOrName##", stemIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
        }
        
      }
      
      String ifConditionOwner = ruleConfig.getIfConditionOwner();
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
          Stem stem = StemFinder.findByName(stemIdOrName, false);
          if (stem == null) {
            stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemIdOrName, false);
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
      
      if (StringUtils.isBlank(checkOwner) && StringUtils.isNotBlank(ruleConfig.getCheckOwnerUuidOrName())) {
        checkOwner = "anotherStem";
      } else if (StringUtils.isBlank(checkOwner) && StringUtils.isBlank(ruleConfig.getCheckOwnerUuidOrName())){
        checkOwner = "thisStem";
      }
      
      if (StringUtils.isNotBlank(checkOwner)) {
        
        checkOwnerStemScope = ruleConfig.getCheckOwnerStemScope();
        
        //value must be thisStem, anotherStem
        if (StringUtils.equals(checkOwner, "thisStem")) {
          checkOwnerName = stem.getName();
        } else if (StringUtils.equals(checkOwner, "anotherStem")) {
          String stemIdOrName = ruleConfig.getCheckOwnerUuidOrName();
          Stem checkOwnerStem = StemFinder.findByName(stemIdOrName, false);
          if (checkOwnerStem == null) {
            checkOwnerStem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemIdOrName, false);
          }
          
          if (checkOwnerStem != null) {
            checkOwnerName = checkOwnerStem.getName();
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
          Stem ifConditionOwnerStem = StemFinder.findByName(stemIdOrName, false);
          if (ifConditionOwnerStem == null) {
            ifConditionOwnerStem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemIdOrName, false);
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
      attributeValueDelegate.assignValue(RuleUtils.ruleCheckOwnerNameName(), checkOwnerName);
      ruleCheck.setCheckOwnerName(checkOwnerName);
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
    attributeAssign2.setOwnerStemId(grouperObject.getId());
    ruleDefinition.setAttributeAssignType(attributeAssign2);
    ruleDefinition.setCheck(ruleCheck);
    ruleDefinition.setIfCondition(ruleIfCondition);
    ruleDefinition.setThen(ruleThen);
    
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
   * @param targetName
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
        
        ruleCheckType = RuleCheckType.valueOfIgnoreCase(value, true);
        
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



  public static void deleteRuleAttributes(Stem stem, String attributeAssignId) {
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
    attributeAssign.delete();
  }
  
  public static void deleteRuleAttributes(Group group, String attributeAssignId) {
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
    attributeAssign.delete();
  }

}
