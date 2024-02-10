package edu.internet2.middleware.grouper.rules;

import java.util.Arrays;
import java.util.Collections;
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
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectAttributes;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
    
    if (grouperObject instanceof Group) {
      Group group = (Group) grouperObject;
      attributeAssign = attributeAssign != null ? attributeAssign : group.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    } else if (grouperObject instanceof Stem) {
      Stem stem = (Stem) grouperObject;
      attributeAssign = attributeAssign != null ? attributeAssign : stem.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
      
      //rule is being assigned on a folder 
      String checkOwner = ruleConfig.getCheckOwner();
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
            error = error.replace("$$folderUuidOrName$$", stemIdOrName);
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
            error = error.replace("$$groupUuidOrName$$", groupIdOrName);
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
            error = error.replace("$$folderUuidOrName$$", stemIdOrName);
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
            error = error.replace("$$groupUuidOrName$$", groupIdOrName);
            result.put("ERROR", Arrays.asList(error));
            return result;
          }
        }
        
      }
      
    }
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), SubjectFinder.findRootSubject().getId());
    attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), ruleConfig.getCheckType());
    
    if (StringUtils.isNotBlank(checkOwnerName)) {
      attributeValueDelegate.assignValue(RuleUtils.ruleCheckOwnerNameName(), checkOwnerName);
    }
    
    if (StringUtils.isNotBlank(checkOwnerStemScope)) {
      attributeValueDelegate.assignValue(RuleUtils.ruleCheckStemScopeName(), checkOwnerStemScope);
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getCheckArg0())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleCheckArg0Name(), ruleConfig.getCheckArg0());
    }
    if (StringUtils.isNotBlank(ruleConfig.getCheckArg1())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleCheckArg1Name(), ruleConfig.getCheckArg1());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getIfConditionOption())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumName(), ruleConfig.getIfConditionOption());
    }
    
    if (StringUtils.isNotBlank(ifConditionOwnerName)) {
      attributeValueDelegate.assignValue(RuleUtils.ruleIfOwnerNameName(), ifConditionOwnerName);
    }
    
    if (StringUtils.isNotBlank(ifConditionOwnerStemScope)) {
      attributeValueDelegate.assignValue(RuleUtils.ruleIfStemScopeName(), ifConditionOwnerStemScope);
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getIfConditionArg0())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumArg0Name(), ruleConfig.getIfConditionArg0());
    }
    if (StringUtils.isNotBlank(ruleConfig.getIfConditionArg1())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionEnumArg1Name(), ruleConfig.getIfConditionArg1());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getThenOption())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), ruleConfig.getThenOption());
    }
    
    if (StringUtils.isNotBlank(ruleConfig.getThenArg0())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), ruleConfig.getThenArg0());
    }
    if (StringUtils.isNotBlank(ruleConfig.getThenArg1())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), ruleConfig.getThenArg1());
    }
    if (StringUtils.isNotBlank(ruleConfig.getThenArg2())) {
      attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg2Name(), ruleConfig.getThenArg2());
    }
    
    if (!ruleConfig.isRunDaemon()) {
      attributeValueDelegate.assignValue(RuleUtils.ruleRunDaemonName(), "F");
    } else {
      attributeValueDelegate.assignValue(RuleUtils.ruleRunDaemonName(), "T");
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
  
  
  
  /**
   * retrieve type setting for a given grouper object (group/stem) and target name.
   * @param grouperObject
   * @param targetName
   * @return
   */
  public static RuleConfig getRuleConfig(GrouperObject grouperObject, String attributeAssignId) {
    
    RuleConfig ruleConfig = new RuleConfig();
     
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
    
    ruleConfig.setCheckOwnerUuidOrName(ruleCheckOwnerName);
    
    return ruleConfig;
  }



  public static void deleteRuleAttributes(Stem stem, String attributeAssignId) {
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
    attributeAssign.delete();
  }

}
