package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GshTemplateValidationService {
  

  public boolean validate(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec, GshTemplateExecOutput execOutput) {
    
    boolean isValid = validateEnabled(templateConfig, execOutput);
    isValid = isValid && validateOwnerType(templateConfig, gshTemplateExec, execOutput);
    isValid = isValid && validateSecurityRunType(templateConfig, gshTemplateExec, execOutput);
    isValid = isValid && validateInputs(templateConfig, gshTemplateExec, execOutput);
    return isValid;
  }
  
  private boolean validateOwnerType(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec, GshTemplateExecOutput execOutput) {
    if (gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.stem) {
      String ownerStemString = gshTemplateExec.getOwnerStemName();
      if (StringUtils.isBlank(ownerStemString)) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerTypeStem.blank.message");
        execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
        return false;
      }
      
      
      Stem ownerStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), ownerStemString, false);
      if (ownerStem == null) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerStem.notFound.message");
        errorMessage = errorMessage.replace("$$ownerStemName$$", ownerStemString);
        execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
        return false;
      }
      
      if (!templateConfig.canFolderRunTemplate(ownerStem)) {
        throw new RuntimeException("ownerStem "+ownerStem.getName() + " is not allowed to run this gsh template");
      }
      
    } else if (gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.group) {
      String ownerGroupString = gshTemplateExec.getOwnerGroupName();
      if (StringUtils.isBlank(ownerGroupString)) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerTypeGroup.blank.message");
        execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
        return false;
      }
      
      
      Group ownerGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), ownerGroupString, false);
      if (ownerGroup == null) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerGroup.notFound.message");
        errorMessage = errorMessage.replace("$$ownerGroupName$$", ownerGroupString);
        execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
        return false;
      }
      
    } else {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerType.required.message");
      execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
      return false;
    }
    
    return true;
  }
  
  public boolean canSubjectExecuteTemplate(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec) {
    
    if (PrivilegeHelper.isWheelOrRoot(gshTemplateExec.getCurrentUser())) {
      return true;
    }
    return (boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.specifiedGroup) {
          return templateConfig.getGroupThatCanRun().hasMember(gshTemplateExec.getCurrentUser());
        } else if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.wheel) {
          return PrivilegeHelper.isWheelOrRoot(gshTemplateExec.getCurrentUser());
          
        } else if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.privilegeOnObject) {
          
          if (gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.stem) {
            String ownerStemString = gshTemplateExec.getOwnerStemName();
            Stem ownerStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), ownerStemString, true);
            
            GshTemplateRequireFolderPrivilege gshTemplateRequireFolderPrivilege = templateConfig.getGshTemplateRequireFolderPrivilege();
            
            return ownerStem.canHavePrivilege(gshTemplateExec.getCurrentUser(), gshTemplateRequireFolderPrivilege.getPrivilege().getName(), true);
            
          } else if (gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.group) {
            String ownerGroupString = gshTemplateExec.getOwnerGroupName();
            Group ownerGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), ownerGroupString, true);
            
            GshTemplateRequireGroupPrivilege gshTemplateRequireGroupPrivilege = templateConfig.getGshTemplateRequireGroupPrivilege();
            return ownerGroup.canHavePrivilege(gshTemplateExec.getCurrentUser(), gshTemplateRequireGroupPrivilege.getPrivilege().getName(), true);
          } else {
            throw new RuntimeException("Invalid gshTemplateOwnerType");
          }
         
        }
        return true;
      }
    });
    
    
  }
  
  private boolean validateSecurityRunType(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec, GshTemplateExecOutput execOutput) {
    
    if (PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject())) {
      return true;
    }
    
    //Member currentUserMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), currentUser, true);
    // validate current user
    if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.specifiedGroup) {
      if (!templateConfig.getGroupThatCanRun().hasMember(gshTemplateExec.getCurrentUser())) {
        
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.notMemberOfGroupThatCanRunTemplate.message");
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", gshTemplateExec.getCurrentUser().getId());
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$groupName$$", templateConfig.getGroupThatCanRun().getName());
        execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
        return false;
        
      }
    } else if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.wheel) {
      String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
      
      if (StringUtils.isBlank(wheelGroupName)) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.wheelGroupMissing.message");
        execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
        return false;
      } 
      
      if (!PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject())) {
        
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.notMemberOfWheelGroup.message");
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", gshTemplateExec.getCurrentUser().getId());
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$groupName$$", wheelGroupName);
        execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
        
        return false;
      }
       
    } else if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.privilegeOnObject) {
      
      if (gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.stem) {
        String ownerStemString = gshTemplateExec.getOwnerStemName();
        Stem ownerStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), ownerStemString, true);
        
        GshTemplateRequireFolderPrivilege gshTemplateRequireFolderPrivilege = templateConfig.getGshTemplateRequireFolderPrivilege();
        
        if (!ownerStem.canHavePrivilege(gshTemplateExec.getCurrentUser(), gshTemplateRequireFolderPrivilege.getPrivilege().getName(), true)) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.noPrivilegeOnOwnerStem.message");
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", gshTemplateExec.getCurrentUser().getId());
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$ownerStemName$$", ownerStemString);
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$privilege$$", gshTemplateRequireFolderPrivilege.getPrivilege().getName());
          execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
          
          return false;
        }
        
      } else {
        String ownerGroupString = gshTemplateExec.getOwnerGroupName();
        Group ownerGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), ownerGroupString, true);
        
        GshTemplateRequireGroupPrivilege gshTemplateRequireGroupPrivilege = templateConfig.getGshTemplateRequireGroupPrivilege();
        if (!ownerGroup.canHavePrivilege(gshTemplateExec.getCurrentUser(), gshTemplateRequireGroupPrivilege.getPrivilege().getName(), true)) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.noPrivilegeOnOwnerGroup.message");
          
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", gshTemplateExec.getCurrentUser().getId());
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$ownerGroupName$$", ownerGroupString);
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$privilege$$", gshTemplateRequireGroupPrivilege.getPrivilege().getName());
          
          execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
          
          return false;
        }
        
      }
     
    }
    
    return true;
    
  }
  
  private String substituteHtmlInErrorMessage(String errorMessage, String key, String value) {
    errorMessage = errorMessage.replace(key, GrouperUtil.escapeHtml(value, true));
    return errorMessage;
  }
  
  private boolean validateInputs(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec, GshTemplateExecOutput execOutput) {
    
    Map<String, GshTemplateInputConfig> inputConfigs = GrouperUtil.listToMap(templateConfig.getGshTemplateInputConfigs(), String.class, GshTemplateInputConfig.class, "name");
    
    Map<String, Object> variableMap = new HashMap<String, Object>();
    
    variableMap.put("grouperUtil", new GrouperUtil());

    // init stuff to null or default value from config
    for (GshTemplateInputConfig gshTemplateInputConfig: templateConfig.getGshTemplateInputConfigs()) {
      if (StringUtils.isBlank(gshTemplateInputConfig.getDefaultValue())) {
        variableMap.put(gshTemplateInputConfig.getName(), null);
      } else {
        variableMap.put(gshTemplateInputConfig.getName(), gshTemplateInputConfig.getGshTemplateInputType().converToType(gshTemplateInputConfig.getDefaultValue()));
      }
    }

    for (GshTemplateInput gshTemplateInput: gshTemplateExec.getGshTemplateInputs()) {
      
      if (inputConfigs.containsKey(gshTemplateInput.getName())) {
        GshTemplateInputConfig gshTemplateInputConfig = inputConfigs.get(gshTemplateInput.getName());
        
        String valueFromUser = gshTemplateInput.getValueString();
        
        if (StringUtils.isBlank(valueFromUser) && !StringUtils.isBlank(gshTemplateInputConfig.getDefaultValue())) {
          valueFromUser = gshTemplateInputConfig.getDefaultValue();
        }
        
        if (gshTemplateInputConfig.isTrimWhitespace() && gshTemplateInput.getValueString() != null) {
          gshTemplateInput.assignValueString(gshTemplateInput.getValueString().trim());          
        }
        
        // textareas can have various newlines
        if (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXTAREA) {
          gshTemplateInput.assignValueString(GrouperUtil.whitespaceNormalizeNewLines(gshTemplateInput.getValueString()));
        }
        
        if (!gshTemplateInputConfig.getGshTemplateInputType().canConvertToCorrectType(valueFromUser)) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.conversion.message");
          errorMessage = errorMessage.replace("$$valueFromUser$$", GrouperUtil.escapeHtml(valueFromUser, true));
          errorMessage = errorMessage.replace("$$type$$", GrouperUtil.escapeHtml(gshTemplateInputConfig.getGshTemplateInputType().name().toLowerCase(), true));
          execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), errorMessage);
          return false;
        }
        
        variableMap.put(gshTemplateInput.getName(), gshTemplateInputConfig.getGshTemplateInputType().converToType(valueFromUser));
      }
      
      
    }
    
    //remove the ones where showEL is evaluated to false
    for (GshTemplateInputConfig gshTemplateInputConfig: templateConfig.getGshTemplateInputConfigs()) {
      
      if (!inputConfigs.containsKey(gshTemplateInputConfig.getName())) {
        continue;
      }
      
      String showElScript = gshTemplateInputConfig.getShowEl();
      if (StringUtils.isNotBlank(showElScript)) {
        try {
          Object substituteExpressionLanguageScript = GrouperUtil.substituteExpressionLanguageScript(showElScript, variableMap, true, false, false);
          Boolean booleanObjectValue = GrouperUtil.booleanObjectValue(substituteExpressionLanguageScript);
          if (booleanObjectValue == null || !booleanObjectValue) {
            inputConfigs.remove(gshTemplateInputConfig.getName());
          }
        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, ", script: '" + showElScript + "', ");
          GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(variableMap));
          throw re;
        }

      }
      
    }
    
    
    for (GshTemplateInput gshTemplateInput: gshTemplateExec.getGshTemplateInputs()) {
      
      {
        // make sure names passed in the input at runtime do exist in the config input names
        if (!inputConfigs.containsKey(gshTemplateInput.getName())) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.notConfiguredInTemplate.message");
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInput.getName());
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$validInputNames$$", GrouperUtil.collectionToString(inputConfigs.keySet()));
          execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
          
          return false;
        }
      }
      
      GshTemplateInputConfig gshTemplateInputConfig = inputConfigs.get(gshTemplateInput.getName());
      String valueFromUser = gshTemplateInput.getValueString();
      
      {
        // required
        if (gshTemplateInputConfig.isRequired() && StringUtils.isEmpty(valueFromUser)) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.required.message");
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInputConfig.getLabelForUi());
          execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), errorMessage);
          
          return false;
        }
      }
      
      {
        //max length
        if (StringUtils.isNotBlank(valueFromUser) && (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXT || gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXTAREA) && valueFromUser.length() > gshTemplateInputConfig.getMaxLength()) {
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.maxLength.message");
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInputConfig.getLabelForUi());
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$maxLength$$", gshTemplateInputConfig.getMaxLength()+"");
          execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), errorMessage);
          return false;
        }
      }
      
      {
        // validate data type (string, int, boolean)
        GshTemplateInputType gshTemplateInputType = gshTemplateInputConfig.getGshTemplateInputType();
        boolean canBeConverted = gshTemplateInputType.canConvertToCorrectType(valueFromUser);
        if (!canBeConverted) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.conversion.message");
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$valueFromUser$$", valueFromUser);
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$type$$", gshTemplateInputType.name().toLowerCase());
          execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), errorMessage);
          
          return false;
        }
      }
      
      {
        // validate the value provided by the user
        GshTemplateInputValidationType gshTemplateInputValidationType = gshTemplateInputConfig.getGshTemplateInputValidationType();
        if (gshTemplateInputValidationType == GshTemplateInputValidationType.regex && !StringUtils.isBlank(valueFromUser)) {
          boolean valuePasses = gshTemplateInputValidationType.doesValuePassValidation(gshTemplateInputConfig, valueFromUser, gshTemplateExec.getGshTemplateInputs());
          if (!valuePasses) {
            
            if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessage())) {
              execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), gshTemplateInputConfig.getValidationMessage());
              return false;
            } else if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessageExternalizedTextKey())) {
              String validationMessage = GrouperTextContainer.textOrNull(gshTemplateInputConfig.getValidationMessageExternalizedTextKey());
              execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), validationMessage);
              return false;
            } else {
              String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.regex.message");
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$valueFromUser$$", valueFromUser);
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$regex$$", gshTemplateInputConfig.getValidationRegex());
              execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), errorMessage);
              
              return false;
            }            
          }
        } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.jexl && !StringUtils.isBlank(valueFromUser)) {
          boolean valuePasses = gshTemplateInputValidationType.doesValuePassValidation(gshTemplateInputConfig, valueFromUser, gshTemplateExec.getGshTemplateInputs());
          if (!valuePasses) {
            
            if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessage())) {
              execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), gshTemplateInputConfig.getValidationMessage());
              return false;
            } else if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessageExternalizedTextKey())) {
              String validationMessage = GrouperTextContainer.textOrNull(gshTemplateInputConfig.getValidationMessageExternalizedTextKey());
              execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), validationMessage);
              return false;
            } else {
              
              String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.jexl.message");
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInputConfig.getLabelForUi());
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$jexl$$", gshTemplateInputConfig.getValidationJexl());
              execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), errorMessage);
              
              return false;
            }
            
          }
        } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.builtin && !StringUtils.isBlank(valueFromUser)) {
          boolean valuePasses = gshTemplateInputValidationType.doesValuePassValidation(gshTemplateInputConfig, valueFromUser, gshTemplateExec.getGshTemplateInputs());
          if (!valuePasses) {
            
            if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessage())) {
              execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), gshTemplateInputConfig.getValidationMessage());
            } else if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessageExternalizedTextKey())) {
              String validationMessage = GrouperTextContainer.textOrNull(gshTemplateInputConfig.getValidationMessageExternalizedTextKey());
              execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), validationMessage);
            }
            
            String errorMessage = gshTemplateInputConfig.getValidationBuiltinType().getErrorMessage(valueFromUser);
            execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), errorMessage);
            
            return false;
          }
        }
        
        // dropdown value
        if (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.DROPDOWN) {
          List<MultiKey> validKeysValues = gshTemplateInputConfig.getGshTemplateDropdownValueFormatType().retrieveKeysAndLabels(gshTemplateInputConfig);
          
          if (!gshTemplateInputConfig.getGshTemplateDropdownValueFormatType().doesValuePassValidation(valueFromUser, validKeysValues)) {
            // get only keys out of list of multiKeys and convert to comma separated string  
            String validValuesCommaSeparated = GrouperUtil.collectionToString(validKeysValues.stream().map(multikey -> multikey.getKey(0)).collect(Collectors.toSet()));
            String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.invalidDropdownValue.message");
            errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInputConfig.getLabelForUi());
            errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$validValues$$", validValuesCommaSeparated);
            execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), errorMessage);
          }
        }
        
      }
      
    }
    
    //make sure all the required properties are actually there in the inputs provided by user
    Map<String, GshTemplateInput> gshTemplateUserInputs = GrouperUtil.listToMap(gshTemplateExec.getGshTemplateInputs(), String.class, GshTemplateInput.class, "name");
    
    for (String inputNameFromConfig: inputConfigs.keySet()) {
      
      if (inputConfigs.get(inputNameFromConfig).isRequired() && !gshTemplateUserInputs.containsKey(inputNameFromConfig)) {
        
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.requiredNotSent.message");
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", inputNameFromConfig);
        execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
        
        return false;
      }
      
    }
    
    return true;
    
  }
  
  private boolean validateEnabled(GshTemplateConfig templateConfig, GshTemplateExecOutput execOutput) {
    if (!templateConfig.isEnabled()) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.configId.notEnabled.message");
      errorMessage = errorMessage.replace("$$configId$$", templateConfig.getConfigId());
      execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
      return false;
    }
    return true;
  }

}
