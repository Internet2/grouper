package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GshTemplateValidationService {
  

  public boolean validate(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec, GshTemplateOutput gshTemplateOutput) {
    
    // check enabled, owner, and who can run it BEFORE the template's decorate logic runs.  Validation runs in a root
    // session, and compiled templates are always V2 (GRP-7448), so decorate would otherwise run template code for
    // a caller who is not allowed to run the template
    if (!validateEnabledOwnerAndSecurity(templateConfig, gshTemplateExec, gshTemplateOutput)) {
      return false;
    }
    
    // decorate before validating inputs since decorate can change the input configs
    if (StringUtils.equals("V2", templateConfig.getTemplateVersion())) {
      GshTemplateExec gshTemplateExec2 = new GshTemplateExec();
      gshTemplateExec2.assignConfigId(templateConfig.getConfigId());
      GshTemplateV2 executeForTemplateV2instance = gshTemplateExec.executeForTemplateV2instance();
      
      GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput = new GshTemplateDecorateForUiInput();
      gshTemplateDecorateForUiInput.setCurrentSubject(gshTemplateExec.getCurrentUser());
      gshTemplateDecorateForUiInput.setRemoteAddr(gshTemplateExec.getRemoteAddr());
      gshTemplateDecorateForUiInput.setActAsSubject(gshTemplateExec.getActAsSubject());
      gshTemplateDecorateForUiInput.setTemplateConfigId(gshTemplateExec.getConfigId());
      gshTemplateDecorateForUiInput.setOwnerStemName(gshTemplateExec.getOwnerStemName());
      gshTemplateDecorateForUiInput.setOwnerGroupName(gshTemplateExec.getOwnerGroupName());
      Map<String, GshTemplateInputConfigAndValue> gshTemplateInputConfigAndValues = new LinkedHashMap<>();
      Map<String, GshTemplateInput> nameToGshTemplateInput = new HashMap<>(); 
      for (GshTemplateInput gshTemplateInput : GrouperUtil.nonNull(gshTemplateExec.getGshTemplateInputs())) {
        nameToGshTemplateInput.put(gshTemplateInput.getName(), gshTemplateInput);
      }

      for (GshTemplateInputConfig gshTemplateInputConfig : GrouperUtil.nonNull(templateConfig.getGshTemplateInputConfigs())) {

        GshTemplateInputConfigAndValue gshTemplateInputConfigAndValue = new GshTemplateInputConfigAndValue();
        gshTemplateInputConfigAndValues.put(gshTemplateInputConfig.getName(), gshTemplateInputConfigAndValue);
        gshTemplateInputConfigAndValue.setGshTemplateInputConfig(gshTemplateInputConfig);
        
        GshTemplateInput gshTemplateInput = nameToGshTemplateInput.get(gshTemplateInputConfig.getName());
        if (gshTemplateInput != null) {
          gshTemplateInputConfigAndValue.setValue(gshTemplateInput.getValueString());
        }
        
      }
      gshTemplateDecorateForUiInput.setGshTemplateInputConfigAndValues(gshTemplateInputConfigAndValues);
      
      executeForTemplateV2instance.decorateTemplateForUiDisplay(gshTemplateDecorateForUiInput);
      
    }

    return validateInputs(templateConfig, gshTemplateExec, gshTemplateOutput);
  }
  
  /**
   * The checks that must pass before any template code is loaded or run: the template is enabled, the owner
   * (group or folder) exists and the template is allowed on it, and the current user satisfies the template's
   * securityRunType.  Used by validate() (execution) and validatePreflight() (UI form render).
   * @param templateConfig populated config
   * @param gshTemplateExec has the current user and the owner
   * @param gshTemplateOutput validation lines are added here
   * @return true if the template may be used
   */
  private boolean validateEnabledOwnerAndSecurity(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec, GshTemplateOutput gshTemplateOutput) {
    boolean isValid = validateEnabled(templateConfig, gshTemplateOutput);
    // who may run the template is checked before the owner is looked up (as root), when it does not depend on the
    // owner (wheel, specifiedGroup, everyone): a caller who may not run the template at all learns nothing about
    // whether a group or folder name exists or is in the template's scope.  privilegeOnObject needs the owner first.
    boolean securityDependsOnOwner = templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.privilegeOnObject;
    if (!securityDependsOnOwner) {
      isValid = isValid && validateSecurityRunType(templateConfig, gshTemplateExec, gshTemplateOutput);
    }
    isValid = isValid && validateOwnerType(templateConfig, gshTemplateExec, gshTemplateOutput);
    if (securityDependsOnOwner) {
      isValid = isValid && validateSecurityRunType(templateConfig, gshTemplateExec, gshTemplateOutput);
    }
    return isValid;
  }
  
  /**
   * The owner group is looked up as root, so also require that the current user can VIEW it (the UI requires the
   * same before showing the template): otherwise a WS caller could run an everyone or specifiedGroup template on a
   * group they cannot see, or learn that it exists.  Wheel and root can see everything.
   * @param ownerGroup found as root
   * @param gshTemplateExec has the current user
   * @return true if the current user can VIEW the owner group
   */
  private static boolean currentUserCanViewOwnerGroup(Group ownerGroup, GshTemplateExec gshTemplateExec) {
    if (gshTemplateExec.getCurrentUser() == null) {
      return false;
    }
    if (PrivilegeHelper.isWheelOrRoot(gshTemplateExec.getCurrentUser())) {
      return true;
    }
    return ownerGroup.canHavePrivilege(gshTemplateExec.getCurrentUser(), AccessPrivilege.VIEW.getName(), false);
  }
  
  /**
   * Check, before any template code is loaded or run, that the current user of the exec may use this template
   * on the exec's owner: the template is enabled, the owner exists and the template is allowed on it, and the
   * current user satisfies the securityRunType.  Callers that render a template form (and so load the V2 class
   * and run decorateTemplateForUiDisplay) must call this first: the UI template dropdown only hides templates,
   * it does not stop a direct request for a template id.
   * <p>
   * Runs in a root session, the same context validate() has under GshTemplateExec.execute(), so owner lookups
   * and group membership checks do not depend on what the user can VIEW.  Fails closed: an exception from a
   * check (e.g. the owner is not allowed for this template) counts as not allowed.
   * </p>
   * @param templateConfig populated config of the template
   * @param gshTemplateExec has the current user, owner type and owner name assigned
   * @param gshTemplateOutput validation lines (already html escaped) are added here when not allowed
   * @return true if allowed
   */
  public boolean validatePreflight(final GshTemplateConfig templateConfig, final GshTemplateExec gshTemplateExec, final GshTemplateOutput gshTemplateOutput) {
    
    if (templateConfig == null || gshTemplateExec == null || gshTemplateExec.getCurrentUser() == null) {
      gshTemplateOutput.addValidationLine(GrouperTextContainer.textOrNull("gshTemplate.error.ownerType.required.message"));
      return false;
    }
    
    try {
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          return validateEnabledOwnerAndSecurity(templateConfig, gshTemplateExec, gshTemplateOutput);
        }
      });
    } catch (RuntimeException re) {
      // e.g. "ownerGroup x is not allowed to run this gsh template": deny, do not let it through
      GrouperUtil.getLog(GshTemplateValidationService.class).warn("gsh template preflight denied: " + templateConfig.getConfigId(), re);
      gshTemplateOutput.addValidationLine(GrouperUtil.escapeHtml(StringUtils.defaultIfBlank(re.getMessage(), 
          "Not allowed to use this template"), true));
      return false;
    }
  }
  
  private boolean validateOwnerType(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec, GshTemplateOutput gshTemplateOutput) {
    if (gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.stem) {
      String ownerStemString = gshTemplateExec.getOwnerStemName();
      if (StringUtils.isBlank(ownerStemString)) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerTypeStem.blank.message");
        gshTemplateOutput.addValidationLine(errorMessage);
        return false;
      }
      
      
      Stem ownerStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), ownerStemString, false);
      if (ownerStem == null) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerStem.notFound.message");
        errorMessage = errorMessage.replace("$$ownerStemName$$", ownerStemString);
        gshTemplateOutput.addValidationLine(errorMessage);
        return false;
      }
      
      if (!templateConfig.canFolderRunTemplate(ownerStem)) {
        throw new RuntimeException("ownerStem "+ownerStem.getName() + " is not allowed to run this gsh template");
      }
      
    } else if (gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.group) {
      String ownerGroupString = gshTemplateExec.getOwnerGroupName();
      if (StringUtils.isBlank(ownerGroupString)) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerTypeGroup.blank.message");
        gshTemplateOutput.addValidationLine(errorMessage);
        return false;
      }
      
      
      Group ownerGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), ownerGroupString, false);
      // a group the current user cannot VIEW gets the same message as a group that does not exist
      if (ownerGroup == null || !currentUserCanViewOwnerGroup(ownerGroup, gshTemplateExec)) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerGroup.notFound.message");
        errorMessage = errorMessage.replace("$$ownerGroupName$$", ownerGroupString);
        gshTemplateOutput.addValidationLine(errorMessage);
        return false;
      }
      if (!templateConfig.canGroupRunTemplate(ownerGroup)) {
        throw new RuntimeException("ownerGroup "+ownerGroup.getName() + " is not allowed to run this gsh template");
      }
      
    } else {
      if (!templateConfig.isAllowWsFromNoOwner() && templateConfig.getGshTemplateType() != GshTemplateType.provisioner) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerType.required.message");
        gshTemplateOutput.addValidationLine(errorMessage);
        return false;
      }
    }
    if (!StringUtils.equals("V2", templateConfig.getTemplateVersion())  && templateConfig.getGshTemplateType() == GshTemplateType.provisioner) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.provisioner.mustBe.v2.message");
      gshTemplateOutput.addValidationLine(errorMessage);
      return false;
    }
    if ((gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.group 
        || gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.stem ) 
        && templateConfig.getGshTemplateType() == GshTemplateType.provisioner) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.provisioner.notOnGroupsOrFolders.message");
      gshTemplateOutput.addValidationLine(errorMessage);
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
        
        if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.everyone) {
          return true;
        } else if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.specifiedGroup) {
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
            throw new RuntimeException("Invalid gshTemplateOwnerType: "+gshTemplateExec.getGshTemplateOwnerType());
          }
         
        } else {
          throw new RuntimeException("Invalid gshTemplateSecurityRunType: "+templateConfig.getGshTemplateSecurityRunType());
        } 
      }
    });
    
    
  }
  
  private boolean validateSecurityRunType(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec, GshTemplateOutput gshTemplateOutput) {
    
    if (PrivilegeHelper.isWheelOrRoot(gshTemplateExec.getCurrentUser())) {
      return true;
    }
    
    GshTemplateSecurityRunType gshTemplateSecurityRunType = templateConfig.getGshTemplateSecurityRunType();

    // Fail closed for provisioner templates and for a missing securityRunType.  Provisioner templates run as
    // GrouperSystem and are meant to be invoked only by the provisioning framework, which runs them as the root
    // subject (GshTemplateProvisionerFactory) and so took the early return above.  Their securityRunType is not
    // configurable and is null, and null must never mean "everyone": without this, any caller of
    // GshTemplateExec.execute() (e.g. the WS gshTemplateExec operation, with no owner) could run them as
    // GrouperSystem.  Treat them as wheel only.
    if (templateConfig.getGshTemplateType() == GshTemplateType.provisioner || gshTemplateSecurityRunType == null) {
      String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
      if (StringUtils.isBlank(wheelGroupName)) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.wheelGroupMissing.message");
        gshTemplateOutput.addValidationLine(errorMessage);
        return false;
      }
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.notMemberOfWheelGroup.message");
      errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", gshTemplateExec.getCurrentUser().getId());
      errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$groupName$$", wheelGroupName);
      gshTemplateOutput.addValidationLine(errorMessage);
      return false;
    }
    
    //Member currentUserMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), currentUser, true);
    // validate current user
    if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.specifiedGroup) {
      if (!templateConfig.getGroupThatCanRun().hasMember(gshTemplateExec.getCurrentUser())) {
        
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.notMemberOfGroupThatCanRunTemplate.message");
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", gshTemplateExec.getCurrentUser().getId());
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$groupName$$", templateConfig.getGroupThatCanRun().getName());
        gshTemplateOutput.addValidationLine(errorMessage);
        return false;
        
      }
    } else if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.wheel) {
      String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
      
      if (StringUtils.isBlank(wheelGroupName)) {
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.wheelGroupMissing.message");
        gshTemplateOutput.addValidationLine(errorMessage);
        return false;
      } 
      
      if (!PrivilegeHelper.isWheelOrRoot(gshTemplateExec.getCurrentUser())) {
        
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.notMemberOfWheelGroup.message");
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", gshTemplateExec.getCurrentUser().getId());
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$groupName$$", wheelGroupName);
        gshTemplateOutput.addValidationLine(errorMessage);
        
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
          gshTemplateOutput.addValidationLine(errorMessage);
          
          return false;
        }
        
      } else if (gshTemplateExec.getGshTemplateOwnerType() == GshTemplateOwnerType.group) {
        String ownerGroupString = gshTemplateExec.getOwnerGroupName();
        Group ownerGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), ownerGroupString, true);
        
        GshTemplateRequireGroupPrivilege gshTemplateRequireGroupPrivilege = templateConfig.getGshTemplateRequireGroupPrivilege();
        if (!ownerGroup.canHavePrivilege(gshTemplateExec.getCurrentUser(), gshTemplateRequireGroupPrivilege.getPrivilege().getName(), true)) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.noPrivilegeOnOwnerGroup.message");
          
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", gshTemplateExec.getCurrentUser().getId());
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$ownerGroupName$$", ownerGroupString);
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$privilege$$", gshTemplateRequireGroupPrivilege.getPrivilege().getName());
          
          gshTemplateOutput.addValidationLine(errorMessage);
          
          return false;
        }
        
      } else {

        // passing no owner when the privileges are on owner
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.ownerType.required.message");
        
        gshTemplateOutput.addValidationLine(errorMessage);
        
        return false;
        
      }
     
    } else if (gshTemplateSecurityRunType != GshTemplateSecurityRunType.everyone) {

      // a securityRunType this code does not know: refuse instead of falling through to "allowed"
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.notMemberOfWheelGroup.message");
      errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", gshTemplateExec.getCurrentUser().getId());
      errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$groupName$$", 
          StringUtils.defaultString(GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group")));
      gshTemplateOutput.addValidationLine(errorMessage);
      return false;
    }
    
    // everyone, or one of the checks above passed
    return true;
    
  }
  
  private String substituteHtmlInErrorMessage(String errorMessage, String key, String value) {
    errorMessage = errorMessage.replace(key, GrouperUtil.escapeHtml(value, true));
    return errorMessage;
  }
  
  private boolean validateInputs(GshTemplateConfig templateConfig, GshTemplateExec gshTemplateExec, GshTemplateOutput gshTemplateOutput) {
    
    Map<String, GshTemplateInputConfig> inputConfigs = GrouperUtil.listToMap(templateConfig.getGshTemplateInputConfigs(), String.class, GshTemplateInputConfig.class, "name");
    
    Map<String, Object> variableMap = new HashMap<String, Object>();
    
    variableMap.put("grouperUtil", new GrouperUtil());

    // init stuff to null or default value from config
    for (GshTemplateInputConfig gshTemplateInputConfig: templateConfig.getGshTemplateInputConfigs()) {
      if (StringUtils.isBlank(gshTemplateInputConfig.getDefaultValue())) {
        variableMap.put(gshTemplateInputConfig.getName(), null);
      } else {
        variableMap.put(gshTemplateInputConfig.getName(), gshTemplateInputConfig.getGshTemplateInputType().convertToType(gshTemplateInputConfig.getDefaultValue()));
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
          gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), errorMessage);
          return false;
        }
        
        variableMap.put(gshTemplateInput.getName(), gshTemplateInputConfig.getGshTemplateInputType().convertToType(valueFromUser));
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
          GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(GshTemplateInputMask.variableMapForLog(templateConfig, variableMap)));
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
          gshTemplateOutput.addValidationLine(errorMessage);
          
          return false;
        }
      }
      
      GshTemplateInputConfig gshTemplateInputConfig = inputConfigs.get(gshTemplateInput.getName());
      String valueFromUser = gshTemplateInput.getValueString();
      
      {
        // required
        if (gshTemplateInputConfig.isRequired() && StringUtils.isBlank(valueFromUser)) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.required.message");
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInputConfig.getLabelForUi());
          gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), errorMessage);
          
          return false;
        }
      }
      
      {
        //max length
        if (StringUtils.isNotBlank(valueFromUser) && (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXT || gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXTAREA) && valueFromUser.length() > gshTemplateInputConfig.getMaxLength()) {
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.maxLength.message");
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInputConfig.getLabelForUi());
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$maxLength$$", gshTemplateInputConfig.getMaxLength()+"");
          gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), errorMessage);
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
          gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), errorMessage);
          
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
              gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), gshTemplateInputConfig.getValidationMessage());
              return false;
            } else if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessageExternalizedTextKey())) {
              String validationMessage = GrouperTextContainer.textOrNull(gshTemplateInputConfig.getValidationMessageExternalizedTextKey());
              gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), validationMessage);
              return false;
            } else {
              String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.regex.message");
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$valueFromUser$$", valueFromUser);
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$regex$$", gshTemplateInputConfig.getValidationRegex());
              gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), errorMessage);
              
              return false;
            }            
          }
        } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.jexl && !StringUtils.isBlank(valueFromUser)) {
          boolean valuePasses = gshTemplateInputValidationType.doesValuePassValidation(gshTemplateInputConfig, valueFromUser, gshTemplateExec.getGshTemplateInputs());
          if (!valuePasses) {
            
            if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessage())) {
              gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), gshTemplateInputConfig.getValidationMessage());
              return false;
            } else if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessageExternalizedTextKey())) {
              String validationMessage = GrouperTextContainer.textOrNull(gshTemplateInputConfig.getValidationMessageExternalizedTextKey());
              gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), validationMessage);
              return false;
            } else {
              
              String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.jexl.message");
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInputConfig.getLabelForUi());
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$jexl$$", gshTemplateInputConfig.getValidationJexl());
              gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), errorMessage);
              
              return false;
            }
            
          }
        } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.builtin && !StringUtils.isBlank(valueFromUser)) {
          boolean valuePasses = gshTemplateInputValidationType.doesValuePassValidation(gshTemplateInputConfig, valueFromUser, gshTemplateExec.getGshTemplateInputs());
          if (!valuePasses) {
            
            if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessage())) {
              gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), gshTemplateInputConfig.getValidationMessage());
            } else if (StringUtils.isNotBlank(gshTemplateInputConfig.getValidationMessageExternalizedTextKey())) {
              String validationMessage = GrouperTextContainer.textOrNull(gshTemplateInputConfig.getValidationMessageExternalizedTextKey());
              gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), validationMessage);
            }
            
            String errorMessage = gshTemplateInputConfig.getValidationBuiltinType().getErrorMessage(valueFromUser);
            gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), errorMessage);
            
            return false;
          }
        }
        
        // dropdown value
        if (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.DROPDOWN) {
          List<MultiKey> validKeysValues = gshTemplateInputConfig.getDropdownKeysAndLabels();
          
          if (StringUtils.isNotBlank(valueFromUser)) {
            if (!gshTemplateInputConfig.getGshTemplateDropdownValueFormatType().doesValuePassValidation(valueFromUser, validKeysValues)) {
              // get only keys out of list of multiKeys, drop the empty UI placeholder, and convert to comma separated string
              // (GshTemplateInputConfig.getDropdownKeysAndLabels prepends an empty MultiKey for the UI dropdown's blank initial option;
              // it must not appear in the human-readable validation message)
              LinkedHashSet<String> validKeysSet = new LinkedHashSet<String>();
              for (MultiKey multikey : validKeysValues) {
                String key = (String) multikey.getKey(0);
                if (key != null && key.length() > 0) {
                  validKeysSet.add(key);
                }
              }
              String validValuesCommaSeparated = GrouperUtil.collectionToString(validKeysSet);
              String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.invalidDropdownValue.message");
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInputConfig.getLabelForUi());
              errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$validValues$$", validValuesCommaSeparated);
              gshTemplateOutput.addValidationLine(gshTemplateInput.getName(), errorMessage);
            }
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
        gshTemplateOutput.addValidationLine(errorMessage);
        
        return false;
      }
      
    }
    
    return true;
    
  }
  
  private boolean validateEnabled(GshTemplateConfig templateConfig, GshTemplateOutput gshTemplateOutput) {
    if (!templateConfig.isEnabled()) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.configId.notEnabled.message");
      // the config id can come from the request (UI templateType parameter), so escape it
      errorMessage = errorMessage.replace("$$configId$$", GrouperUtil.escapeHtml(templateConfig.getConfigId(), true));
      gshTemplateOutput.addValidationLine(errorMessage);
      return false;
    }
    return true;
  }

}
