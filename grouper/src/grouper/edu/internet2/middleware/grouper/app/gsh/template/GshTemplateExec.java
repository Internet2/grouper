package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.text.StringEscapeUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh.GrouperGroovyResult;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateExec {
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    GrouperSession.startRootSession();
     
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId("createNewWorkingGroup");
    Subject subject = SubjectFinder.findRootSubject();
    exec.assignCurrentUser(subject);
     
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName("ref:incommon-collab"); // run the script from test2 folder
     
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_workingGroupExtension");
    input.assignValueString("myGroup");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_workingGroupDisplayExtension");
    input.assignValueString("My group");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_workingGroupDescription");
    input.assignValueString("My working group will do a lot of group work");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_isSympa");
    input.assignValueString("true");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_sympaDomain");
    input.assignValueString("internet2");
    exec.addGshTemplateInput(input);
    input = new GshTemplateInput();
    input.assignName("gsh_input_isSympaModerated");
    input.assignValueString("true");
    exec.addGshTemplateInput(input);
     
    // when
    GshTemplateExecOutput output = exec.execute();
     
    // then
    System.out.println("Success: " + output.isSuccess());
    if (!output.isSuccess() && output.getException() != null) {
      System.out.println(output.getExceptionStack());
    }
    System.out.println("Valid: " + output.isValid());
    System.out.println("Validation:");
    for (GshValidationLine gshValidationLine : output.getGshTemplateOutput().getValidationLines()) {
      System.out.println(gshValidationLine.getInputName() + ": " + gshValidationLine.getText());
    }
    System.out.println("Output from script:");
    for (GshOutputLine gshOutputLine : output.getGshTemplateOutput().getOutputLines()) {
      System.out.println(gshOutputLine.getMessageType() + ": " + gshOutputLine.getText());
    }
    System.out.println("Script output:");
    System.out.println(output.getGshScriptOutput());
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GshTemplateExec.class);
  
  private GshTemplateOwnerType gshTemplateOwnerType;
  
  private String ownerStemName;
  
  private String ownerGroupName;
  
  private String configId; // gsh template config to run
  
  private Subject currentUser; // user using the UI or the webservice
  
  private Subject actAsSubject; // only for WS
  
  private Subject originalCurrentUser; // user is the current user or if there's an actAs, it's the user calling the web service
  
  private List<GshTemplateInput> gshTemplateInputs = new ArrayList<GshTemplateInput>();
  
  public GshTemplateExec assignActAsSubject(Subject actAsSubject) {
    this.actAsSubject = actAsSubject;
    return this;
  }
  
  public GshTemplateExec assignGshTemplateOwnerType(GshTemplateOwnerType gshTemplateOwnerType) {
    this.gshTemplateOwnerType = gshTemplateOwnerType;
    return this;
  }
  
  public GshTemplateExec assignOwnerStemName(String ownerStemName) {
    this.ownerStemName = ownerStemName;
    return this;
  }
  
  public GshTemplateExec assignOwnerGroupName(String ownerGroupName) {
    this.ownerGroupName = ownerGroupName;
    return this;
  }
  
  public GshTemplateExec addGshTemplateInput(GshTemplateInput input) {
    gshTemplateInputs.add(input);
    return this;
  }
  
  public GshTemplateExec assignConfigId(String configId) {
    this.configId = configId;
    return this;
  }
  
  public GshTemplateExec assignCurrentUser(Subject currentUser) {
    this.currentUser = currentUser;
    return this;
  }

  public Subject getCurrentUser() {
    return currentUser;
  }

  public GshTemplateOwnerType getGshTemplateOwnerType() {
    return gshTemplateOwnerType;
  }

  
  public String getOwnerStemName() {
    return ownerStemName;
  }
  
  public String getOwnerGroupName() {
    return ownerGroupName;
  }

  
  public List<GshTemplateInput> getGshTemplateInputs() {
    return gshTemplateInputs;
  }
  
  
  public String getConfigId() {
    return configId;
  }

  
  
  private boolean validateEnabled(GshTemplateConfig templateConfig, GshTemplateExecOutput execOutput) {
    if (!templateConfig.isEnabled()) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.configId.notEnabled.message");
      errorMessage = errorMessage.replace("$$configId$$", configId);
      execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
      return false;
    }
    return true;
  }
  
  
  public Subject getActAsSubject() {
    return actAsSubject;
  }

  private boolean validateOwnerType(GshTemplateConfig templateConfig, GshTemplateExecOutput execOutput) {
    if (this.gshTemplateOwnerType == GshTemplateOwnerType.stem) {
      String ownerStemString = this.getOwnerStemName();
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
      
    } else if (this.gshTemplateOwnerType == GshTemplateOwnerType.group) {
      String ownerGroupString = this.getOwnerGroupName();
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
  
  public boolean canSubjectExecuteTemplate(GshTemplateConfig templateConfig) {
    
    if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.specifiedGroup) {
      Member currentUserMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), currentUser, true);
      return templateConfig.getGroupThatCanRun().getMembers().contains(currentUserMember);
    } else if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.wheel) {
      String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
      if (StringUtils.isBlank(wheelGroupName)) {
        return false;
      } 
      Member currentUserMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), currentUser, true);
      Group wheelGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), wheelGroupName, true);
      return wheelGroup.getMembers().contains(currentUserMember);
      
    } else if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.privilegeOnObject) {
      
      if (this.gshTemplateOwnerType == GshTemplateOwnerType.stem) {
        String ownerStemString = this.getOwnerStemName();
        Stem ownerStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), ownerStemString, true);
        
        GshTemplateRequireFolderPrivilege gshTemplateRequireFolderPrivilege = templateConfig.getGshTemplateRequireFolderPrivilege();
        
        return ownerStem.canHavePrivilege(currentUser, gshTemplateRequireFolderPrivilege.getPrivilege().getName(), true);
        
      } else {
        String ownerGroupString = this.getOwnerGroupName();
        Group ownerGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), ownerGroupString, true);
        
        GshTemplateRequireGroupPrivilege gshTemplateRequireGroupPrivilege = templateConfig.getGshTemplateRequireGroupPrivilege();
        return ownerGroup.canHavePrivilege(currentUser, gshTemplateRequireGroupPrivilege.getPrivilege().getName(), true);
      }
     
    }
    
    
    return true;
  }
  
  private boolean validateSecurityRunType(GshTemplateConfig templateConfig, GshTemplateExecOutput execOutput) {
    
    if (PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject())) {
      return true;
    }
    
    Member currentUserMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), currentUser, true);
    // validate current user
    if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.specifiedGroup) {
      if (!templateConfig.getGroupThatCanRun().getMembers().contains(currentUserMember)) {
        
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.notMemberOfGroupThatCanRunTemplate.message");
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", currentUser.getId());
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
      
      Group wheelGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), wheelGroupName, true);
      if (!wheelGroup.getMembers().contains(currentUserMember)) {
        
        String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.notMemberOfWheelGroup.message");
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", currentUser.getId());
        errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$groupName$$", wheelGroup.getName());
        execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
        
        return false;
      }
    } else if (templateConfig.getGshTemplateSecurityRunType() == GshTemplateSecurityRunType.privilegeOnObject) {
      
      if (this.gshTemplateOwnerType == GshTemplateOwnerType.stem) {
        String ownerStemString = this.getOwnerStemName();
        Stem ownerStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), ownerStemString, true);
        
        GshTemplateRequireFolderPrivilege gshTemplateRequireFolderPrivilege = templateConfig.getGshTemplateRequireFolderPrivilege();
        
        if (!ownerStem.canHavePrivilege(currentUser, gshTemplateRequireFolderPrivilege.getPrivilege().getName(), true)) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.noPrivilegeOnOwnerStem.message");
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", currentUser.getId());
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$ownerStemName$$", ownerStemString);
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$privilege$$", gshTemplateRequireFolderPrivilege.getPrivilege().getName());
          execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
          
          return false;
        }
        
      } else {
        String ownerGroupString = this.getOwnerGroupName();
        Group ownerGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), ownerGroupString, true);
        
        GshTemplateRequireGroupPrivilege gshTemplateRequireGroupPrivilege = templateConfig.getGshTemplateRequireGroupPrivilege();
        if (!ownerGroup.canHavePrivilege(currentUser, gshTemplateRequireGroupPrivilege.getPrivilege().getName(), true)) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.currentUser.noPrivilegeOnOwnerGroup.message");
          
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$subjectId$$", currentUser.getId());
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
  
  private boolean validateInputs(GshTemplateConfig templateConfig, GshTemplateExecOutput execOutput) {
    
    Map<String, GshTemplateInputConfig> inputConfigs = GrouperUtil.listToMap(templateConfig.getGshTemplateInputConfigs(), String.class, GshTemplateInputConfig.class, "name");
    
    Map<String, Object> variableMap = new HashMap<String, Object>();
    
    variableMap.put("grouperUtil", new GrouperUtil());
    
    for (GshTemplateInput gshTemplateInput: gshTemplateInputs) {
      variableMap.put(gshTemplateInput.getName(), StringUtils.isNotBlank(gshTemplateInput.getValueString())? gshTemplateInput.getValueString(): "");
    }
    
    //remove the ones where showEL is evaluated to false
    for (GshTemplateInputConfig gshTemplateInputConfig: templateConfig.getGshTemplateInputConfigs()) {
      
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
    
    
    for (GshTemplateInput gshTemplateInput: gshTemplateInputs) {
      
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
      if (gshTemplateInputConfig.isTrimWhitespace() && StringUtils.isNotBlank(valueFromUser)) {
        valueFromUser = valueFromUser.trim();
      }
      
      {
        // required
        if (gshTemplateInputConfig.isRequired() && StringUtils.isBlank(valueFromUser)) {
          
          String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.input.required.message");
          errorMessage = substituteHtmlInErrorMessage(errorMessage, "$$inputName$$", gshTemplateInputConfig.getLabelForUi());
          execOutput.getGshTemplateOutput().addValidationLine(gshTemplateInput.getName(), errorMessage);
          
          return false;
        }
      }
      
      {
        //max length
        if (StringUtils.isNotBlank(valueFromUser) && gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXT && valueFromUser.length() > gshTemplateInputConfig.getMaxLength()) {
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
        if (gshTemplateInputValidationType == GshTemplateInputValidationType.regex) {
          boolean valuePasses = gshTemplateInputValidationType.doesValuePassValidation(gshTemplateInputConfig, valueFromUser, gshTemplateInputs);
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
        } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.jexl) {
          boolean valuePasses = gshTemplateInputValidationType.doesValuePassValidation(gshTemplateInputConfig, valueFromUser, gshTemplateInputs);
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
        } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.builtin) {
          boolean valuePasses = gshTemplateInputValidationType.doesValuePassValidation(gshTemplateInputConfig, valueFromUser, gshTemplateInputs);
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
          List<MultiKey> validKeysValues = gshTemplateInputConfig.getGshTemplateDropdownValueFormatType().retrieveKeysAndLabels(gshTemplateInputConfig.getDropdownValueBasedOnType());
          
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
    Map<String, GshTemplateInput> gshTemplateUserInputs = GrouperUtil.listToMap(getGshTemplateInputs(), String.class, GshTemplateInput.class, "name");
    
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
  
  private boolean validate(GshTemplateConfig templateConfig, GshTemplateExecOutput execOutput) {
    
    boolean isValid = validateEnabled(templateConfig, execOutput);
    isValid = isValid && validateOwnerType(templateConfig, execOutput);
    isValid = isValid && validateSecurityRunType(templateConfig, execOutput);
    isValid = isValid && validateInputs(templateConfig, execOutput);
    return isValid;
    
  }
  
  /**
   * execute the gsh template
   * @return
   */
  public GshTemplateExecOutput execute() {
    
    GshTemplateExecOutput execOutput = new GshTemplateExecOutput();
    
    final GshTemplateOutput gshTemplateOutput = new GshTemplateOutput();
    
    execOutput.setGshTemplateOutput(gshTemplateOutput);
    
    final GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
    
    if (StringUtils.isBlank(configId)) {
      
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.configIdBlank.message");
      execOutput.getGshTemplateOutput().addValidationLine(errorMessage);
      return execOutput;
    }
    
    if (currentUser == null) {
      throw new RuntimeException("currentUser cannot be null");
    }
    
    GshTemplateConfig templateConfig = new GshTemplateConfig(configId);
    
    Subject grouperSessionSubject = (Subject) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Subject callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        originalCurrentUser = currentUser;
        
        templateConfig.populateConfiguration();
        
        if (actAsSubject != null) {
          String actAsGroupUUID = templateConfig.getActAsGroupUUID();
          if (actAsGroupUUID == null) {
            throw new RuntimeException("actAsGroupUUID has not been configured in the template");
          }
          Group actAsGroup = GroupFinder.findByUuid(grouperSession, actAsGroupUUID, true);
          
          if (!actAsGroup.hasMember(currentUser)) {
            throw new RuntimeException(currentUser.getId()+ " is not a member of "+actAsGroup.getName());
          }
          
          currentUser = actAsSubject;
          
        }
        
        gshTemplateRuntime.setCurrentSubject(currentUser);
        
        if (!validate(templateConfig, execOutput)) {
          return null;
        }
        
        Subject grouperSessionSubject = null;
        
        if (templateConfig.getGshTemplateRunAsType() == GshTemplateRunAsType.GrouperSystem) {
          grouperSessionSubject = SubjectFinder.findRootSubject();
        } else if (templateConfig.getGshTemplateRunAsType() == GshTemplateRunAsType.currentUser) {
          grouperSessionSubject = currentUser;
        } else if (templateConfig.getGshTemplateRunAsType() == GshTemplateRunAsType.specifiedSubject) {
          
          String subjectId = templateConfig.getRunAsSpecifiedSubjectId();
          String sourceId = templateConfig.getRunAsSpecifiedSubjectSourceId();
          
          grouperSessionSubject = SubjectFinder.findByIdAndSource(subjectId, sourceId, true);
          
        }
        
        return grouperSessionSubject;
      }
    });
    
  
    if (execOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
      execOutput.setValid(false);
      return execOutput;
    } else {
      execOutput.setValid(true);
    }
    
    StringBuilder scriptToRun = new StringBuilder();
    
    scriptToRun.append("import edu.internet2.middleware.grouper.app.gsh.template.*;");
    scriptToRun.append("\n");

    scriptToRun.append("import edu.internet2.middleware.grouper.util.*;");
    scriptToRun.append("\n");
    
    scriptToRun.append("GshTemplateOutput gsh_builtin_gshTemplateOutput = GshTemplateOutput.retrieveGshTemplateOutput(); ");
    scriptToRun.append("\n");
    
    scriptToRun.append("GshTemplateRuntime gsh_builtin_gshTemplateRuntime = GshTemplateRuntime.retrieveGshTemplateRuntime(); ");
    scriptToRun.append("\n");
    
    scriptToRun.append("GrouperSession gsh_builtin_grouperSession = gsh_builtin_gshTemplateRuntime.getGrouperSession();");
    scriptToRun.append("\n");
    
    scriptToRun.append("Subject gsh_builtin_subject = gsh_builtin_gshTemplateRuntime.getCurrentSubject();");
    scriptToRun.append("\n");
    
    scriptToRun.append("String gsh_builtin_subjectId = \""+ StringEscapeUtils.escapeJava(currentUser.getId()) + "\";");
    scriptToRun.append("\n");
    
    if (this.gshTemplateOwnerType == GshTemplateOwnerType.stem) {
      scriptToRun.append("String gsh_builtin_ownerStemName = \""+StringEscapeUtils.escapeJava(ownerStemName) + "\";");
      scriptToRun.append("\n");
    } else {
      scriptToRun.append("String gsh_builtin_ownerGroupName = \""+StringEscapeUtils.escapeJava(ownerGroupName) + "\";");
      scriptToRun.append("\n");
    }
    
    Map<String, GshTemplateInput> gshTemplateInputsMap = new HashMap<String, GshTemplateInput>();
    
    for (GshTemplateInput gshTemplateInput: gshTemplateInputs) {
      gshTemplateInputsMap.put(gshTemplateInput.getName(), gshTemplateInput);
    }
    
    for (GshTemplateInputConfig inputConfig: templateConfig.getGshTemplateInputConfigs()) {
      
      GshTemplateInput gshTemplateInput = gshTemplateInputsMap.get(inputConfig.getName());
      
      String valueFromUser = null;
      if (gshTemplateInput != null) {
        valueFromUser = gshTemplateInput.getValueString();
        if (inputConfig.isTrimWhitespace()) {
          valueFromUser = valueFromUser.trim();
        }
      }
      
      String gshVariable = inputConfig.getGshTemplateInputType().generateGshVariable(inputConfig, valueFromUser);
      
      scriptToRun.append(gshVariable);
      
    }
    
    scriptToRun.append(templateConfig.getGshTemplate());
    GrouperSession grouperSession = null;
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(System.currentTimeMillis());
    
    try {     
      GshTemplateOutput.assignThreadLocalGshTemplateOutput(gshTemplateOutput);
      GshTemplateRuntime.assignThreadLocalGshTemplateRuntime(gshTemplateRuntime);
      grouperSession = GrouperSession.start(grouperSessionSubject);
      gshTemplateRuntime.setGrouperSession(grouperSession);

      GrouperGroovyResult grouperGroovyResult = null;
      
      execOutput.setTransaction(templateConfig.isRunGshInTransaction());
      
      StringBuilder inputsStringBuilder = new StringBuilder();
      for (GshTemplateInput input: this.gshTemplateInputs) {
        inputsStringBuilder.append(input.getName() + " = " + GrouperUtil.abbreviate(input.getValueString(), 50) + ";");
      }
      
      final boolean success[] = {true};
      
      grouperGroovyResult = (GrouperGroovyResult) HibernateSession.callbackHibernateSession(
          templateConfig.isRunGshInTransaction() ? GrouperTransactionType.READ_WRITE_OR_USE_EXISTING: GrouperTransactionType.NONE, 
              templateConfig.isUseIndividualAudits() ? AuditControl.WILL_NOT_AUDIT: AuditControl.WILL_AUDIT,
          new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          GrouperGroovyResult grouperGroovyResult = GrouperGroovysh.runScript(scriptToRun.toString(), templateConfig.isGshLightweight(), true);
          
          for (GshOutputLine gshOutputLine: execOutput.getGshTemplateOutput().getOutputLines()) {
            if (StringUtils.equals("error", gshOutputLine.getMessageType())) {
              success[0] = false;
            }
          }
          
          if (execOutput.getGshTemplateOutput().isError() || execOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
            success[0] = false;
          }
          
          if (success[0] == false && templateConfig.isRunGshInTransaction()) {
            hibernateHandlerBean.getHibernateSession().rollback(GrouperRollbackType.ROLLBACK_NOW);
          }
          
          if (!templateConfig.isRunGshInTransaction() || (success[0] == true && templateConfig.isRunGshInTransaction())) {
            
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GSH_TEMPLATE_EXEC,
                "gshTemplateConfigId", configId, "status",
                success[0] ? "success": "error");
            String actAsAdditionalLine = originalCurrentUser == currentUser ? "" : ("(WsUser: "+SubjectHelper.getPretty(originalCurrentUser) + ") ");
            auditEntry.setDescription("Execute gsh template "+actAsAdditionalLine + "with configId: " + configId + ", status: " + (success[0] ? "success": "error") + ", inputs: "+inputsStringBuilder.toString());
            auditEntry.saveOrUpdate(true);
          }
          
          return grouperGroovyResult;
        }
        
      });
      
      
      
      if (execOutput.getGshTemplateOutput().getValidationLines().size() > 0) {
        execOutput.setValid(false);
        
        Set<String> configuredInputNames = new HashSet<String>();
        for (GshTemplateInputConfig inputConfig: templateConfig.getGshTemplateInputConfigs()) {
          configuredInputNames.add(inputConfig.getName());
        }
        
        for (GshValidationLine gshValidationLine: execOutput.getGshTemplateOutput().getValidationLines()) {
          if (StringUtils.isNotBlank(gshValidationLine.getInputName()) && !configuredInputNames.contains(gshValidationLine.getInputName())) {
            LOG.error(gshValidationLine.getInputName() + " is not in list of configured input names");
          }
         }
        
      }
      
      if (success[0] == false || GrouperUtil.intValue(grouperGroovyResult.getResultCode(), -1) != 0) {
        execOutput.setSuccess(false);
      } else {
        if (gshTemplateOutput.isError()) {
          execOutput.setSuccess(false);
        } else {
          execOutput.setSuccess(true);
        }
      }
      
      execOutput.setGshScriptOutput(grouperGroovyResult.getOutString());
      execOutput.setException(grouperGroovyResult.getException());
      
      if (!execOutput.isSuccess()) {
        LOG.error(grouperGroovyResult.getOutString(), grouperGroovyResult.getException());
      } else {
        LOG.debug(grouperGroovyResult.getOutString(), grouperGroovyResult.getException());
      }
      
    } catch (RuntimeException e) {
      execOutput.setSuccess(false);
      execOutput.setException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      GshTemplateOutput.removeThreadLocalGshTemplateOutput();
      GshTemplateRuntime.removeThreadLocalGshTemplateRuntime();
      
    }
    
    return execOutput;
  }

  
}
