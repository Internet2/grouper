package edu.internet2.middleware.grouper.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManagerOptionValueDriver;

public enum RulePattern {
  
  VetoInFolderIfNotEligibleDueToGroup {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String groupName = patternPropertiesValues.get("VetoInFolderIfNotEligibleDueToGroup.groupName");
      String folderScope = patternPropertiesValues.get("VetoInFolderIfNotEligibleDueToGroup.stemScope");
    
      ruleConfig.setCheckType(RuleCheckType.subjectAssignInStem.name());
      ruleConfig.setCheckOwnerStemScope(folderScope);
      ruleConfig.setCheckOwner("thisStem");
    
      ruleConfig.setIfConditionOption(RuleIfConditionEnum.groupHasNoEnabledMembership.name());
      ruleConfig.setIfConditionOwnerUuidOrName(groupName);
//      ruleConfig.setIfConditionOwner("anotherGroup");
      
      ruleConfig.setThenOption(RuleThenEnum.veto.name());

      ruleConfig.setThenArg0("rule.entity.must.be.a.member.of.etc.employee");
      
      String arg1Message = GrouperTextContainer.textOrNull("VetoInFolderIfNotEligibleDueToGroupArg1Message");
      arg1Message = arg1Message.replace("##targetGroup##", groupName);
      ruleConfig.setThenArg1(arg1Message);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String groupName = patternPropertiesValues.get("VetoInFolderIfNotEligibleDueToGroup.groupName");
      String folderScope = patternPropertiesValues.get("VetoInFolderIfNotEligibleDueToGroup.stemScope");
      
      Group group = GroupFinder.findByName(groupName, false);
      if (group == null) {
        group = GroupFinder.findByUuid(groupName, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
        error = error.replace("##groupUuidOrName##", groupName);
        errorMessages.add(error);
      }
      
      if (group != null && loggedInSubject != null) {
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      if (group != null && loggedInSubject != null) {
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      if (StringUtils.isBlank(folderScope)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeRequired");
        errorMessages.add(error);
      } else {
        if (!StringUtils.equals(folderScope, "SUB") && !StringUtils.equals(folderScope, "ONE")) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeInvalid");
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("VetoInFolderIfNotEligibleDueToGroupUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("VetoInFolderIfNotEligibleDueToGroup.groupName");
        attribute.setLabel(GrouperTextContainer.textOrNull("VetoInFolderIfNotEligibleDueToGroup.groupName.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("VetoInFolderIfNotEligibleDueToGroup.groupName.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getIfCondition() != null) {
          attribute.setValue(ruleDefinition.getIfCondition().getIfOwnerName());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("VetoInFolderIfNotEligibleDueToGroup.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("VetoInFolderIfNotEligibleDueToGroup.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("VetoInFolderIfNotEligibleDueToGroup.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckStemScope());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return true;
    }

    @Override
    public boolean isApplicableForGroups() {
      return false;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.subjectAssignInStem &&
          ruleDefinition.getIfCondition() != null && ruleDefinition.getIfCondition().ifConditionEnum() == RuleIfConditionEnum.groupHasNoEnabledMembership &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.veto &&
          StringUtils.equals(ruleDefinition.getThen().getThenEnumArg0(), "rule.entity.must.be.a.member.of.etc.employee") &&
          StringUtils.startsWith(ruleDefinition.getThen().getThenEnumArg1(), GrouperTextContainer.textOrNull("VetoInFolderIfNotEligibleDueToGroupArg1MessagePrefix"))) {
        return true;
      }
          
      return false;
    }

  },
  
  InheritedPrivilegesOnFolders {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String subjectIdOrIdentifier = patternPropertiesValues.get("InheritedPrivilegesOnFolders.subjectIdOrIdentifier");
      String privilegesToAdd = patternPropertiesValues.get("InheritedPrivilegesOnFolders.privilegesToAdd");
      String folderScope = patternPropertiesValues.get("InheritedPrivilegesOnFolders.stemScope");
      String subjectSource = patternPropertiesValues.get("InheritedPrivilegesOnFolders.subjectSource");
    
      ruleConfig.setCheckType(RuleCheckType.stemCreate.name());
      ruleConfig.setCheckOwnerStemScope(folderScope);
      ruleConfig.setCheckOwner("thisStem");
    
      ruleConfig.setThenOption(RuleThenEnum.assignStemPrivilegeToStemId.name());
      Subject subject = SubjectFinder.findByIdAndSource(subjectIdOrIdentifier, subjectSource, false);
      String colonSeparator = null;
      if (subject != null) {
        colonSeparator = "::::";
      } else {
        subject = SubjectFinder.findByIdentifierAndSource(subjectIdOrIdentifier, subjectSource, false);
        if (subject != null) {
          colonSeparator = "::::::";
        } else {
          throw new RuntimeException("Cannot find subject '"+subjectIdOrIdentifier +"' by source '"+subjectSource+"'");
        }
      }
      
      ruleConfig.setThenArg0(subjectSource+" "+ colonSeparator+" "+subjectIdOrIdentifier);// "g:gsa :::::: stem1:admins"
      ruleConfig.setThenArg1(privilegesToAdd);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
      
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String subjectIdOrIdentifier = patternPropertiesValues.get("InheritedPrivilegesOnFolders.subjectIdOrIdentifier");
      String privilegesToAdd = patternPropertiesValues.get("InheritedPrivilegesOnFolders.privilegesToAdd");
      String folderScope = patternPropertiesValues.get("InheritedPrivilegesOnFolders.stemScope");
      String subjectSource = patternPropertiesValues.get("InheritedPrivilegesOnFolders.subjectSource");
    
    
      Subject subject = SubjectFinder.findByIdAndSource(subjectIdOrIdentifier, subjectSource, false);
      if (subject == null) {
        subject = SubjectFinder.findByIdentifierAndSource(subjectIdOrIdentifier, subjectSource, false);
      }
      
      if (subject == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidSubjectOrSubjectSource");
        error = error.replace("##subjectIdOrIdentifier##", subjectIdOrIdentifier);
        error = error.replace("##subjectSource##", subjectSource);
        errorMessages.add(error);
      }
      
      if (subject != null && loggedInSubject != null && StringUtils.equals(subjectSource, "g:gsa")) {
        Group group = GroupFinder.findByUuid(subject.getId(), false);
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      if(StringUtils.isBlank(privilegesToAdd)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditPrivilegesRequired");
        errorMessages.add(error);
      } else {
        Set<String> correctPrivs = new HashSet<>();
        correctPrivs.add("stem");
        correctPrivs.add("create");
        String[] privileges = GrouperUtil.splitTrim(privilegesToAdd, ",");
        for (String privilege: privileges) {
           if (!correctPrivs.contains(privilege.toLowerCase())) {
             String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidPrivilegeFound");
             error = error.replace("##invalidPrivilege##", privilege);
             errorMessages.add(error);
           }
        }
        
      }
      
      if (StringUtils.isBlank(folderScope)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeRequired");
        errorMessages.add(error);
      } else {
        if (!StringUtils.equals(folderScope, "SUB") && !StringUtils.equals(folderScope, "ONE")) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeInvalid");
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("InheritedPrivilegesOnFoldersUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnFolders.subjectIdOrIdentifier");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnFolders.subjectIdOrIdentifier.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnFolders.subjectIdOrIdentifier.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg0())) {
          String arg0 = ruleDefinition.getThen().getThenEnumArg0();
          if (arg0.contains("::::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::::");
            attribute.setValue(subjectSourceAndIdentifier[1]);
          } else if (arg0.contains("::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::");
            attribute.setValue(subjectSourceAndIdentifier[1]);
          }
        }
        elements.add(attribute);
      }
    
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnFolders.privilegesToAdd");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnFolders.privilegesToAdd.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnFolders.privilegesToAdd.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg1())) {
          String privileges = ruleDefinition.getThen().getThenEnumArg1();
          attribute.setValue(privileges);
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnFolders.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnFolders.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnFolders.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null && StringUtils.isNotBlank(ruleDefinition.getCheck().getCheckStemScope())) {
          attribute.setValue(ruleDefinition.getCheck().getCheckStemScope());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        
        SourceManagerOptionValueDriver driver = new SourceManagerOptionValueDriver();
        
        attribute.setDropdownValuesAndLabels(driver.retrieveKeysAndLabels());
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnFolders.subjectSource");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnFolders.subjectSource.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnFolders.subjectSource.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg0())) {
          String arg0 = ruleDefinition.getThen().getThenEnumArg0();
          if (arg0.contains("::::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::::");
            attribute.setValue(subjectSourceAndIdentifier[0]);
          } else if (arg0.contains("::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::");
            attribute.setValue(subjectSourceAndIdentifier[0]);
          }
        }
        
        
        elements.add(attribute);
      }
     
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return true;
    }

    @Override
    public boolean isApplicableForGroups() {
      return false;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.stemCreate &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.assignStemPrivilegeToStemId) {
        return true;
      }
      
      return false;
    }

    @Override
    public boolean isDaemonApplicable() {
      return true;
    }
    
  },
  
  InheritedPrivilegesOnAttributeDefinitions {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String subjectIdOrIdentifier = patternPropertiesValues.get("InheritedPrivilegesOnAttributeDefinitions.subjectIdOrIdentifier");
      String privilegesToAdd = patternPropertiesValues.get("InheritedPrivilegesOnAttributeDefinitions.privilegesToAdd");
      String folderScope = patternPropertiesValues.get("InheritedPrivilegesOnAttributeDefinitions.stemScope");
      String subjectSource = patternPropertiesValues.get("InheritedPrivilegesOnAttributeDefinitions.subjectSource");
    
      ruleConfig.setCheckType(RuleCheckType.attributeDefCreate.name());
      ruleConfig.setCheckOwnerStemScope(folderScope);
      ruleConfig.setCheckOwner("thisStem");
    
      ruleConfig.setThenOption(RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId.name());
      Subject subject = SubjectFinder.findByIdAndSource(subjectIdOrIdentifier, subjectSource, false);
      String colonSeparator = null;
      if (subject != null) {
        colonSeparator = "::::";
      } else {
        subject = SubjectFinder.findByIdentifierAndSource(subjectIdOrIdentifier, subjectSource, false);
        if (subject != null) {
          colonSeparator = "::::::";
        } else {
          throw new RuntimeException("Cannot find subject '"+subjectIdOrIdentifier +"' by source '"+subjectSource+"'");
        }
      }
      
      ruleConfig.setThenArg0(subjectSource+" "+ colonSeparator+" "+subjectIdOrIdentifier);// "g:gsa :::::: stem1:admins"
      ruleConfig.setThenArg1(privilegesToAdd);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String subjectIdOrIdentifier = patternPropertiesValues.get("InheritedPrivilegesOnAttributeDefinitions.subjectIdOrIdentifier");
      String privilegesToAdd = patternPropertiesValues.get("InheritedPrivilegesOnAttributeDefinitions.privilegesToAdd");
      String folderScope = patternPropertiesValues.get("InheritedPrivilegesOnAttributeDefinitions.stemScope");
      String subjectSource = patternPropertiesValues.get("InheritedPrivilegesOnAttributeDefinitions.subjectSource");
    
      Subject subject = SubjectFinder.findByIdAndSource(subjectIdOrIdentifier, subjectSource, false);
      if (subject == null) {
        subject = SubjectFinder.findByIdentifierAndSource(subjectIdOrIdentifier, subjectSource, false);
      }
      
      if (subject == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidSubjectOrSubjectSource");
        error = error.replace("##subjectIdOrIdentifier##", subjectIdOrIdentifier);
        error = error.replace("##subjectSource##", subjectSource);
        errorMessages.add(error);
      }
      
      if (subject != null && loggedInSubject != null && StringUtils.equals(subjectSource, "g:gsa")) {
        Group group = GroupFinder.findByUuid(subject.getId(), false);
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      if(StringUtils.isBlank(privilegesToAdd)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditPrivilegesRequired");
        errorMessages.add(error);
      } else {
        Set<String> correctPrivs = GrouperUtil.splitTrimToSet("attrRead, attrUpdate, attrView, attrAdmin, attrOptin, attrOptout", ",");
        String[] privileges = GrouperUtil.splitTrim(privilegesToAdd, ",");
        for (String privilege: privileges) {
           if (!correctPrivs.contains(privilege.toLowerCase())) {
             String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidPrivilegeFound");
             error = error.replace("##invalidPrivilege##", privilege);
             errorMessages.add(error);
           }
        }
        
      }
      
      if (StringUtils.isBlank(folderScope)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeRequired");
        errorMessages.add(error);
      } else {
        if (!StringUtils.equals(folderScope, "SUB") && !StringUtils.equals(folderScope, "ONE")) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeInvalid");
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("InheritedPrivilegesOnAttributeDefinitionsUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnAttributeDefinitions.subjectIdOrIdentifier");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnAttributeDefinitions.subjectIdOrIdentifier.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnAttributeDefinitions.subjectIdOrIdentifier.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg0())) {
          String arg0 = ruleDefinition.getThen().getThenEnumArg0();
          if (arg0.contains("::::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::::");
            attribute.setValue(subjectSourceAndIdentifier[1]);
          } else if (arg0.contains("::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::");
            attribute.setValue(subjectSourceAndIdentifier[1]);
          }
        }
        
        elements.add(attribute);
      }
    
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnAttributeDefinitions.privilegesToAdd");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnAttributeDefinitions.privilegesToAdd.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnAttributeDefinitions.privilegesToAdd.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg1())) {
          String privileges = ruleDefinition.getThen().getThenEnumArg1();
          attribute.setValue(privileges);
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnAttributeDefinitions.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnAttributeDefinitions.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnAttributeDefinitions.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null && StringUtils.isNotBlank(ruleDefinition.getCheck().getCheckStemScope())) {
          attribute.setValue(ruleDefinition.getCheck().getCheckStemScope());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        
        SourceManagerOptionValueDriver driver = new SourceManagerOptionValueDriver();
        
        attribute.setDropdownValuesAndLabels(driver.retrieveKeysAndLabels());
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnAttributeDefinitions.subjectSource");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnAttributeDefinitions.subjectSource.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnAttributeDefinitions.subjectSource.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg0())) {
          String arg0 = ruleDefinition.getThen().getThenEnumArg0();
          if (arg0.contains("::::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::::");
            attribute.setValue(subjectSourceAndIdentifier[0]);
          } else if (arg0.contains("::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::");
            attribute.setValue(subjectSourceAndIdentifier[0]);
          }
        }
        
        elements.add(attribute);
      }
     
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return true;
    }

    @Override
    public boolean isApplicableForGroups() {
      return false;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.attributeDefCreate &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId) {
        return true;
      }
      
      return false;
    }

    @Override
    public boolean isDaemonApplicable() {
      return true;
    }
    
    
    
  },
  
  InheritedPrivilegesOnGroups {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig,
        String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String subjectIdOrIdentifier = patternPropertiesValues.get("InheritedPrivilegesOnGroups.subjectIdOrIdentifier");
      String privilegesToAdd = patternPropertiesValues.get("InheritedPrivilegesOnGroups.privilegesToAdd");
      String folderScope = patternPropertiesValues.get("InheritedPrivilegesOnGroups.stemScope");
      String subjectSource = patternPropertiesValues.get("InheritedPrivilegesOnGroups.subjectSource");
    
      ruleConfig.setCheckType(RuleCheckType.groupCreate.name());
      ruleConfig.setCheckOwnerStemScope(folderScope);
      ruleConfig.setCheckOwner("thisStem");
    
      ruleConfig.setThenOption(RuleThenEnum.assignGroupPrivilegeToGroupId.name());
      Subject subject = SubjectFinder.findByIdAndSource(subjectIdOrIdentifier, subjectSource, false);
      String colonSeparator = null;
      if (subject != null) {
        colonSeparator = "::::";
      } else {
        subject = SubjectFinder.findByIdentifierAndSource(subjectIdOrIdentifier, subjectSource, false);
        if (subject != null) {
          colonSeparator = "::::::";
        } else {
          throw new RuntimeException("Cannot find subject '"+subjectIdOrIdentifier +"' by source '"+subjectSource+"'");
        }
      }
      
      ruleConfig.setThenArg0(subjectSource+" "+ colonSeparator+" "+subjectIdOrIdentifier);// "g:gsa :::::: stem1:admins"
      ruleConfig.setThenArg1(privilegesToAdd);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
      
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String subjectIdOrIdentifier = patternPropertiesValues.get("InheritedPrivilegesOnGroups.subjectIdOrIdentifier");
      String privilegesToAdd = patternPropertiesValues.get("InheritedPrivilegesOnGroups.privilegesToAdd");
      String folderScope = patternPropertiesValues.get("InheritedPrivilegesOnGroups.stemScope");
      String subjectSource = patternPropertiesValues.get("InheritedPrivilegesOnGroups.subjectSource");
    
      Subject subject = SubjectFinder.findByIdAndSource(subjectIdOrIdentifier, subjectSource, false);
      if (subject == null) {
        subject = SubjectFinder.findByIdentifierAndSource(subjectIdOrIdentifier, subjectSource, false);
      }
      
      if (subject == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidSubjectOrSubjectSource");
        error = error.replace("##subjectIdOrIdentifier##", subjectIdOrIdentifier);
        error = error.replace("##subjectSource##", subjectSource);
        errorMessages.add(error);
      }
      
      if (subject != null && loggedInSubject != null && StringUtils.equals(subjectSource, "g:gsa")) {
        Group group = GroupFinder.findByUuid(subject.getId(), false);
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      if(StringUtils.isBlank(privilegesToAdd)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditPrivilegesRequired");
        errorMessages.add(error);
      } else {
        Set<String> correctPrivs = GrouperUtil.splitTrimToSet("read, admin, update", ",");
        String[] privileges = GrouperUtil.splitTrim(privilegesToAdd, ",");
        for (String privilege: privileges) {
           if (!correctPrivs.contains(privilege.toLowerCase())) {
             String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidPrivilegeFound");
             error = error.replace("##invalidPrivilege##", privilege);
             errorMessages.add(error);
           }
        }
        
      }
      
      if (StringUtils.isBlank(folderScope)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeRequired");
        errorMessages.add(error);
      } else {
        if (!StringUtils.equals(folderScope, "SUB") && !StringUtils.equals(folderScope, "ONE")) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeInvalid");
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("InheritedPrivilegesOnGroupsUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnGroups.subjectIdOrIdentifier");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnGroups.subjectIdOrIdentifier.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnGroups.subjectIdOrIdentifier.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg0())) {
          String arg0 = ruleDefinition.getThen().getThenEnumArg0();
          if (arg0.contains("::::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::::");
            attribute.setValue(subjectSourceAndIdentifier[1]);
          } else if (arg0.contains("::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::");
            attribute.setValue(subjectSourceAndIdentifier[1]);
          }
        }
        
        elements.add(attribute);
      }
    
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnGroups.privilegesToAdd");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnGroups.privilegesToAdd.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnGroups.privilegesToAdd.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg1())) {
          String privileges = ruleDefinition.getThen().getThenEnumArg1();
          attribute.setValue(privileges);
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnGroups.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnGroups.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnGroups.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null && StringUtils.isNotBlank(ruleDefinition.getCheck().getCheckStemScope())) {
          attribute.setValue(ruleDefinition.getCheck().getCheckStemScope());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        
        SourceManagerOptionValueDriver driver = new SourceManagerOptionValueDriver();
        
        attribute.setDropdownValuesAndLabels(driver.retrieveKeysAndLabels());
        attribute.setShow(true);
        attribute.setConfigSuffix("InheritedPrivilegesOnGroups.subjectSource");
        attribute.setLabel(GrouperTextContainer.textOrNull("InheritedPrivilegesOnGroups.subjectSource.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("InheritedPrivilegesOnGroups.subjectSource.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg0())) {
          String arg0 = ruleDefinition.getThen().getThenEnumArg0();
          if (arg0.contains("::::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::::");
            attribute.setValue(subjectSourceAndIdentifier[0]);
          } else if (arg0.contains("::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::");
            attribute.setValue(subjectSourceAndIdentifier[0]);
          }
        }
        
        elements.add(attribute);
      }
     
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return true;
    }

    @Override
    public boolean isApplicableForGroups() {
      return false;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.groupCreate &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.assignGroupPrivilegeToGroupId) {
        return true;
      }
      
      return false;
    }
    
    @Override
    public boolean isDaemonApplicable() {
      return true;
    }
    
  },
  
  AddSelfPrivilegesToNewGroups {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig,String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String privilegesToAdd = patternPropertiesValues.get("AddSelfPrivilegesToNewGroups.privilegesToAdd");
      String folderScope = patternPropertiesValues.get("AddSelfPrivilegesToNewGroups.stemScope");
    
      ruleConfig.setCheckType(RuleCheckType.groupCreate.name());
      ruleConfig.setCheckOwnerStemScope(folderScope);
      ruleConfig.setCheckOwner("thisStem");
    
      ruleConfig.setThenOption(RuleThenEnum.assignSelfGroupPrivilege.name());
      
      ruleConfig.setThenArg0(privilegesToAdd);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String privilegesToAdd = patternPropertiesValues.get("AddSelfPrivilegesToNewGroups.privilegesToAdd");
      String folderScope = patternPropertiesValues.get("AddSelfPrivilegesToNewGroups.stemScope");
    
      if(StringUtils.isBlank(privilegesToAdd)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditPrivilegesRequired");
        errorMessages.add(error);
      } else {
        
        Set<String> correctPrivs = GrouperUtil.splitTrimToSet("admin, optin, read, system, update", ",");
        String[] privileges = GrouperUtil.splitTrim(privilegesToAdd, ",");
        for (String privilege: privileges) {
           if (!correctPrivs.contains(privilege.toLowerCase())) {
             String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidPrivilegeFound");
             error = error.replace("##invalidPrivilege##", privilege);
             errorMessages.add(error);
           }
        }
        
      }
      
      if (StringUtils.isBlank(folderScope)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeRequired");
        errorMessages.add(error);
      } else {
        if (!StringUtils.equals(folderScope, "SUB") && !StringUtils.equals(folderScope, "ONE")) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeInvalid");
          errorMessages.add(error);
        }
      }
      
      return errorMessages; 
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("AddSelfPrivilegesToNewGroupsUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddSelfPrivilegesToNewGroups.privilegesToAdd");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddSelfPrivilegesToNewGroups.privilegesToAdd.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddSelfPrivilegesToNewGroups.privilegesToAdd.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg0())) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg0());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddSelfPrivilegesToNewGroups.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddSelfPrivilegesToNewGroups.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddSelfPrivilegesToNewGroups.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null && StringUtils.isNotBlank(ruleDefinition.getCheck().getCheckStemScope())) {
          attribute.setValue(ruleDefinition.getCheck().getCheckStemScope());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return true;
    }

    @Override
    public boolean isApplicableForGroups() {
      return false;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.groupCreate &&
          StringUtils.isNotBlank(ruleDefinition.getCheck().getCheckStemScope()) &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.assignSelfGroupPrivilege) {
        return true;
      }
      
      return false;
    }
    
  },
  
  AddDisabledDateOnInvalidMembership {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String mustBeInGroup = patternPropertiesValues.get("AddDisabledDateOnInvalidMembership.groupName");
      String gracePeriod = patternPropertiesValues.get("AddDisabledDateOnInvalidMembership.gracePeriod");
      String addIfNotThere = patternPropertiesValues.get("AddDisabledDateOnInvalidMembership.addIfNotThere");
      String checkIfRemovedFromGroup = patternPropertiesValues.get("AddDisabledDateOnInvalidMembership.checkIfRemovedFromGroup");
      
      if (StringUtils.equals(checkIfRemovedFromGroup, "T")) {
        ruleConfig.setCheckType(RuleCheckType.flattenedMembershipRemove.name());
      } else if (StringUtils.equals(checkIfRemovedFromGroup, "F")) {
        ruleConfig.setCheckType(RuleCheckType.flattenedMembershipAdd.name());
      }
      
      ruleConfig.setCheckOwnerUuidOrName(mustBeInGroup);
      ruleConfig.setCheckOwner("anotherGroup");

      ruleConfig.setThenOption(RuleThenEnum.assignMembershipDisabledDaysForOwnerGroupId.name());
      ruleConfig.setThenArg0(gracePeriod);
      ruleConfig.setThenArg1(addIfNotThere);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String mustBeInGroup = patternPropertiesValues.get("AddDisabledDateOnInvalidMembership.groupName");
      String gracePeriod = patternPropertiesValues.get("AddDisabledDateOnInvalidMembership.gracePeriod");
      
      Group group = GroupFinder.findByName(mustBeInGroup, false);
      if (group == null) {
        group = GroupFinder.findByUuid(mustBeInGroup, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
        error = error.replace("##groupUuidOrName##", mustBeInGroup);
        errorMessages.add(error);
      }
      
      if (group != null && loggedInSubject != null) {
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      try {
        Integer.valueOf(gracePeriod);
      } catch (Exception e) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGracePeriod");
        error = error.replace("##gracePeriod##", gracePeriod);
        errorMessages.add(error);
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembershipUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("T", GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.checkIfRemovedFromGroup"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("F", GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.checkIfAddedToGroup"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddDisabledDateOnInvalidMembership.checkIfRemovedFromGroup");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.checkIfRemovedFromGroup.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.checkIfRemovedFromGroup.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          if (ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipRemove) {            
            attribute.setValue("T");
          } else if (ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAdd) {
            attribute.setValue("F");
          }
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddDisabledDateOnInvalidMembership.groupName");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.groupName.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.groupName.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckOwnerName());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddDisabledDateOnInvalidMembership.gracePeriod");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.gracePeriod.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.gracePeriod.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg0());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("F", GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.addIfNotThere.no"));
        valuesAndLabels.add(valueAndLabel);
        
        valueAndLabel = new MultiKey("T", GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.addIfNotThere.yes"));
        valuesAndLabels.add(valueAndLabel);
      
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddDisabledDateOnInvalidMembership.addIfNotThere");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.addIfNotThere.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddDisabledDateOnInvalidMembership.addIfNotThere.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg1());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && (ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipRemove 
            || ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAdd) &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.assignMembershipDisabledDaysForOwnerGroupId) {
        return true;
      }
      
      return false;
    }

    @Override
    public boolean isDaemonApplicable() {
      return true;
    }
    
    
  },
  
  AddDisabledDateOnMembership {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String expireAfterDays = patternPropertiesValues.get("AddDisabledDateOnMembership.expireAfterDays");
      
      ruleConfig.setCheckType(RuleCheckType.membershipAdd.name());
      ruleConfig.setCheckOwner("thisGroup");

      ruleConfig.setThenOption(RuleThenEnum.assignMembershipDisabledDaysForOwnerGroupId.name());
      ruleConfig.setThenArg0(expireAfterDays);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String expireAfterDays = patternPropertiesValues.get("AddDisabledDateOnMembership.expireAfterDays");
      
      try {
        Integer.valueOf(expireAfterDays);
      } catch (Exception e) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidNumberOfDays");
        error = error.replace("##numberOfDays##", expireAfterDays);
        errorMessages.add(error);
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("AddDisabledDateOnMembershipUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddDisabledDateOnMembership.expireAfterDays");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddDisabledDateOnMembership.expireAfterDays.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddDisabledDateOnMembership.expireAfterDays.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg0());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.membershipAdd &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.assignMembershipDisabledDaysForOwnerGroupId) {
        return true;
      }
      
      return false;
    }
    
  },
  
  AddMemberToGroupIfAddedToAnotherGroup {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String targetGroupName = patternPropertiesValues.get("AddMemberToGroupIfAddedToAnotherGroup.groupName");
      
      ruleConfig.setCheckType(RuleCheckType.flattenedMembershipAdd.name());
      ruleConfig.setCheckOwnerUuidOrName(targetGroupName);
      ruleConfig.setCheckOwner("anotherGroup");

      ruleConfig.setThenOption(RuleThenEnum.addMemberToOwnerGroup.name());
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String targetGroupName = patternPropertiesValues.get("AddMemberToGroupIfAddedToAnotherGroup.groupName");
      
      Group group = GroupFinder.findByName(targetGroupName, false);
      if (group == null) {
        group = GroupFinder.findByUuid(targetGroupName, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
        error = error.replace("##groupUuidOrName##", targetGroupName);
        errorMessages.add(error);
      }
      
      if (group != null && loggedInSubject != null) {
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("AddMemberToGroupIfAddedToAnotherGroupUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {

      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddMemberToGroupIfAddedToAnotherGroup.groupName");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddMemberToGroupIfAddedToAnotherGroup.groupName.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddMemberToGroupIfAddedToAnotherGroup.groupName.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckOwnerName());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {

      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAdd &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.addMemberToOwnerGroup) {
        return true;
      }
      
      return false;
    }
    
  },
  
  AddMemberToGroupIfRemovedFromAnotherGroup {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String targetGroupName = patternPropertiesValues.get("AddMemberToGroupIfRemovedFromAnotherGroup.groupName");
      
      ruleConfig.setCheckType(RuleCheckType.flattenedMembershipRemove.name());
      ruleConfig.setCheckOwnerUuidOrName(targetGroupName);
      ruleConfig.setCheckOwner("anotherGroup");

      ruleConfig.setThenOption(RuleThenEnum.addMemberToOwnerGroup.name());
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String targetGroupName = patternPropertiesValues.get("AddMemberToGroupIfRemovedFromAnotherGroup.groupName");
      
      Group group = GroupFinder.findByName(targetGroupName, false);
      if (group == null) {
        group = GroupFinder.findByUuid(targetGroupName, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
        error = error.replace("##groupUuidOrName##", targetGroupName);
        errorMessages.add(error);
      }
      
      if (group != null && loggedInSubject != null) {
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("AddMemberToGroupIfRemovedFromAnotherGroupUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {

      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddMemberToGroupIfRemovedFromAnotherGroup.groupName");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddMemberToGroupIfRemovedFromAnotherGroup.groupName.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddMemberToGroupIfRemovedFromAnotherGroup.groupName.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckOwnerName());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {

      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipRemove &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.addMemberToOwnerGroup) {
        return true;
      }
      
      return false;
    }
    
  },
  
  AddCreatedGroupsToAnotherGroup {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String folder = patternPropertiesValues.get("AddCreatedGroupsToAnotherGroup.folder");
      String folderScope = patternPropertiesValues.get("AddCreatedGroupsToAnotherGroup.stemScope");
      
      ruleConfig.setCheckType(RuleCheckType.groupCreate.name());
      ruleConfig.setCheckOwnerStemScope(folderScope);
      ruleConfig.setCheckOwnerUuidOrName(folder);
      
      ruleConfig.setThenOption(RuleThenEnum.addGroupToOwnerGroup.name());
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
      
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String folder = patternPropertiesValues.get("AddCreatedGroupsToAnotherGroup.folder");
      String folderScope = patternPropertiesValues.get("AddCreatedGroupsToAnotherGroup.stemScope");
      
      Stem stem = StemFinder.findByName(folder, false);
      if (stem == null) {
        stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folder, false);
      }
      
      if (stem == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidFolder");
        error = error.replace("##folderUuidOrName##", folder);
        errorMessages.add(error);
      }
      
      if (stem != null && loggedInSubject != null) {
        if (!stem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotAdminStem");
          error = error.replace("##stemName##", stem.getName());
          errorMessages.add(error);
        }
      }
      
      if (StringUtils.isBlank(folderScope)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeRequired");
        errorMessages.add(error);
      } else {
        if (!StringUtils.equals(folderScope, "SUB") && !StringUtils.equals(folderScope, "ONE")) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeInvalid");
          errorMessages.add(error);
        }
      }
      
      return errorMessages; 
    }
    
    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
//      String folder = patternPropertiesValues.get("AddCreatedGroupsToAnotherGroup.folder");
//      String folderScope = patternPropertiesValues.get("AddCreatedGroupsToAnotherGroup.stemScope");
//      
//      ruleConfig.setCheckType(RuleCheckType.groupCreate.name());
//      ruleConfig.setCheckOwnerStemScope(folderScope);
//
//      
//      ruleConfig.setThenOption(RuleThenEnum.addGroupToOwnerGroup.name());
//      ruleConfig.setCheckOwnerUuidOrName(folder);
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddCreatedGroupsToAnotherGroup.folder");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddCreatedGroupsToAnotherGroup.folder.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddCreatedGroupsToAnotherGroup.folder.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckOwnerName());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("AddCreatedGroupsToAnotherGroup.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("AddCreatedGroupsToAnotherGroup.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("AddCreatedGroupsToAnotherGroup.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckStemScope());
        }
        elements.add(attribute);
      }
     
      return elements;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("AddCreatedGroupsToAnotherGroupUserFriendlyText");
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.groupCreate &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.addGroupToOwnerGroup) {
        return true;
      }
      
      return false;
    }
     
  },
  
  RemoveInvalidMembershipDueToGroup {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String group = patternPropertiesValues.get("RemoveInvalidMembershipDueToGroup.group");
      String checkIfRemovedFromGroup = patternPropertiesValues.get("RemoveInvalidMembershipDueToGroup.checkIfRemovedFromGroup");
      
      if (StringUtils.equals(checkIfRemovedFromGroup, "T")) {
        ruleConfig.setCheckType(RuleCheckType.flattenedMembershipRemove.name());
      } else if (StringUtils.equals(checkIfRemovedFromGroup, "F")) {
        ruleConfig.setCheckType(RuleCheckType.flattenedMembershipAdd.name());
      }
      
      ruleConfig.setCheckOwnerUuidOrName(group);
      ruleConfig.setCheckOwner("anotherGroup");
      
      //test
      ruleConfig.setIfConditionOption(RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());

      ruleConfig.setThenOption(RuleThenEnum.removeMemberFromOwnerGroup.name());
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String targetGroupName = patternPropertiesValues.get("RemoveInvalidMembershipDueToGroup.group");
      
      Group group = GroupFinder.findByName(targetGroupName, false);
      if (group == null) {
        group = GroupFinder.findByUuid(targetGroupName, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
        error = error.replace("##groupUuidOrName##", targetGroupName);
        errorMessages.add(error);
      }
      
      if (group != null && loggedInSubject != null) {
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToGroupUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {

      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("T", GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToGroup.checkIfRemovedFromGroup"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("F", GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToGroup.checkIfAddedToGroup"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("RemoveInvalidMembershipDueToGroup.checkIfRemovedFromGroup");
        attribute.setLabel(GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToGroup.checkIfRemovedFromGroup.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToGroup.checkIfRemovedFromGroup.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          if (ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipRemove) {            
            attribute.setValue("T");
          } else if (ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAdd) {
            attribute.setValue("F");
          }
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("RemoveInvalidMembershipDueToGroup.group");
        attribute.setLabel(GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToGroup.group.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToGroup.group.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckOwnerName());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && 
          (ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipRemove || ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAdd) &&
          ruleDefinition.getIfCondition() != null && ruleDefinition.getIfCondition().ifConditionEnum() == RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.removeMemberFromOwnerGroup) {
        return true;
      }
      return false;
    }
    
    @Override
    public boolean isDaemonApplicable() {
      return true;
    }

    @Override
    public boolean isDaemonAssignableByNonAdmin() {
      return true;
    }
     
  },
  
  SendEmailWhenGroupMemberInvalidDueToFolder {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String folder = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.folder");
      String folderScope = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.stemScope");
      
      String emailTo = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.emailBody");
      
      ruleConfig.setCheckType(RuleCheckType.membershipRemoveInFolder.name());
      ruleConfig.setCheckOwnerUuidOrName(folder);
      ruleConfig.setCheckOwnerStemScope(folderScope);
      
      ruleConfig.setIfConditionOption(RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
      ruleConfig.setIfConditionOwnerUuidOrName(ruleConfig.getGrouperObject().getName());
      ruleConfig.setIfConditionOwner("thisGroup");
      
      ruleConfig.setThenOption(RuleThenEnum.sendEmail.name());
      ruleConfig.setThenArg0(emailTo);
      ruleConfig.setThenArg1(emailSubject);
      ruleConfig.setThenArg2(emailBody);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      boolean skipEmailSenderCheck = false;
      if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        skipEmailSenderCheck = true;
      }
      
      if (!skipEmailSenderCheck) {
        Boolean subjectInCache = emailSenders.get(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()));
        if (subjectInCache != null) {
          if (subjectInCache == false) {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
            errorMessages.add(error);
            return errorMessages;
          }
        } else {
          String emailSenderGroupName = GrouperConfig.retrieveConfig().propertyValueString("rules.restrictRulesEmailSendersToMembersOfThisGroupName", "");
          if (StringUtils.isNotBlank(emailSenderGroupName)) {
            Group emailSenderGroup = GroupFinder.findByName(emailSenderGroupName, false);
            if (emailSenderGroup != null) {
              if (emailSenderGroup.hasMember(loggedInSubject)) {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), true);
              } else {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), false);
                String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
                errorMessages.add(error);
                return errorMessages;
              }
              
            } else {
              LOG.warn("rules.restrictRulesEmailSendersToMembersOfThisGroupName is set to '"+emailSenderGroupName+"' and it does not exist.");
            }
          }
        }
      }
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String emailTo = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.emailBody");

      String folderName = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.folder");
      String stemScope = patternPropertiesValues.get("SendEmailWhenGroupMemberInvalidDueToFolder.stemScope");
      
      Stem stem = StemFinder.findByName(folderName, false);
      if (stem == null) {
        stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folderName, false);
      }
      
      if (stem == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidFolder");
        error = error.replace("##folderUuidOrName##", folderName);
        errorMessages.add(error);
      }
      
      if (stem != null && loggedInSubject != null) {
        if (!stem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotAdminStem");
          error = error.replace("##stemName##", stem.getName());
          errorMessages.add(error);
        }
      }
      
      if (StringUtils.isBlank(stemScope)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeRequired");
        errorMessages.add(error);
      } else {
        if (!StringUtils.equals(stemScope, "SUB") && !StringUtils.equals(stemScope, "ONE")) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeInvalid");
          errorMessages.add(error);
        }
      }
      
      
      if (StringUtils.isBlank(emailTo)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailToRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailSubject)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailSubjectRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailBody)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailBodyRequired");
        errorMessages.add(error);
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolderUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailWhenGroupMemberInvalidDueToFolder.folder");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.folder.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.folder.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getIfCondition() != null) {
          attribute.setValue(ruleDefinition.getIfCondition().getIfOwnerName());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailWhenGroupMemberInvalidDueToFolder.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getIfCondition() != null) {
          attribute.setValue(ruleDefinition.getIfCondition().getIfStemScope());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailWhenGroupMemberInvalidDueToFolder.emailTo");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.emailTo.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.emailTo.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg0());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailWhenGroupMemberInvalidDueToFolder.emailSubject");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.emailSubject.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.emailSubject.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg1());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXTAREA);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailWhenGroupMemberInvalidDueToFolder.emailBody");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.emailBody.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailWhenGroupMemberInvalidDueToFolder.emailBody.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg2());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.membershipRemoveInFolder &&
          ruleDefinition.getIfCondition().ifConditionEnum() ==  RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.sendEmail) {
        return true;
      }
      
      return false;
    }
    
  },
  
  SendEmailMembershipAddDueToFolder {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig,String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String emailTo = patternPropertiesValues.get("SendEmailMembershipAddDueToFolder.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailMembershipAddDueToFolder.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailMembershipAddDueToFolder.emailBody");
      String folderScope = patternPropertiesValues.get("SendEmailMembershipAddDueToFolder.stemScope");
      
      ruleConfig.setCheckType(RuleCheckType.flattenedMembershipAddInFolder.name());
      ruleConfig.setCheckOwner("thisStem");
      ruleConfig.setCheckOwnerStemScope(folderScope);
      
      ruleConfig.setThenOption(RuleThenEnum.sendEmail.name());
      ruleConfig.setThenArg0(emailTo);
      ruleConfig.setThenArg1(emailSubject);
      ruleConfig.setThenArg2(emailBody);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      boolean skipEmailSenderCheck = false;
      if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        skipEmailSenderCheck = true;
      }
      
      if (!skipEmailSenderCheck) {
        Boolean subjectInCache = emailSenders.get(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()));
        if (subjectInCache != null) {
          if (subjectInCache == false) {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
            errorMessages.add(error);
            return errorMessages;
          }
        } else {
          String emailSenderGroupName = GrouperConfig.retrieveConfig().propertyValueString("rules.restrictRulesEmailSendersToMembersOfThisGroupName", "");
          if (StringUtils.isNotBlank(emailSenderGroupName)) {
            Group emailSenderGroup = GroupFinder.findByName(emailSenderGroupName, false);
            if (emailSenderGroup != null) {
              if (emailSenderGroup.hasMember(loggedInSubject)) {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), true);
              } else {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), false);
                String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
                errorMessages.add(error);
                return errorMessages;
              }
              
            } else {
              LOG.warn("rules.restrictRulesEmailSendersToMembersOfThisGroupName is set to '"+emailSenderGroupName+"' and it does not exist.");
            }
          }
        }
      }
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String emailTo = patternPropertiesValues.get("SendEmailMembershipAddDueToFolder.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailMembershipAddDueToFolder.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailMembershipAddDueToFolder.emailBody");
      
      if (StringUtils.isBlank(emailTo)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailToRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailSubject)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailSubjectRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailBody)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailBodyRequired");
        errorMessages.add(error);
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("SendEmailMembershipAddDueToFolderUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailMembershipAddDueToFolder.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailMembershipAddDueToFolder.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailMembershipAddDueToFolder.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getIfCondition() != null) {
          attribute.setValue(ruleDefinition.getIfCondition().getIfStemScope());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailMembershipAddDueToFolder.emailTo");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailMembershipAddDueToFolder.emailTo.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailMembershipAddDueToFolder.emailTo.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg0());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailMembershipAddDueToFolder.emailSubject");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailMembershipAddDueToFolder.emailSubject.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailMembershipAddDueToFolder.emailSubject.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg1());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXTAREA);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailMembershipAddDueToFolder.emailBody");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailMembershipAddDueToFolder.emailBody.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailMembershipAddDueToFolder.emailBody.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg2());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return true;
    }

    @Override
    public boolean isApplicableForGroups() {
      return false;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAddInFolder &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.sendEmail) {
        return true;
      }
      
      return false;
    }
    
  },
  
  SendEmailDueToDisabledDate {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String minDays = patternPropertiesValues.get("SendEmailDueToDisabledDate.minDays");
      String maxDays = patternPropertiesValues.get("SendEmailDueToDisabledDate.maxDays");
      
      String emailTo = patternPropertiesValues.get("SendEmailDueToDisabledDate.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailDueToDisabledDate.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailDueToDisabledDate.emailBody");
      
      ruleConfig.setCheckType(RuleCheckType.membershipDisabledDate.name());
      ruleConfig.setCheckArg0(minDays);
      ruleConfig.setCheckArg1(maxDays);
      ruleConfig.setCheckOwner("thisGroup");
      
      ruleConfig.setThenOption(RuleThenEnum.sendEmail.name());
      ruleConfig.setThenArg0(emailTo);
      ruleConfig.setThenArg1(emailSubject);
      ruleConfig.setThenArg2(emailBody);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      boolean skipEmailSenderCheck = false;
      if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        skipEmailSenderCheck = true;
      }
      
      if (!skipEmailSenderCheck) {
        Boolean subjectInCache = emailSenders.get(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()));
        if (subjectInCache != null) {
          if (subjectInCache == false) {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
            errorMessages.add(error);
            return errorMessages;
          }
        } else {
          String emailSenderGroupName = GrouperConfig.retrieveConfig().propertyValueString("rules.restrictRulesEmailSendersToMembersOfThisGroupName", "");
          if (StringUtils.isNotBlank(emailSenderGroupName)) {
            Group emailSenderGroup = GroupFinder.findByName(emailSenderGroupName, false);
            if (emailSenderGroup != null) {
              if (emailSenderGroup.hasMember(loggedInSubject)) {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), true);
              } else {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), false);
                String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
                errorMessages.add(error);
                return errorMessages;
              }
              
            } else {
              LOG.warn("rules.restrictRulesEmailSendersToMembersOfThisGroupName is set to '"+emailSenderGroupName+"' and it does not exist.");
            }
          }
        }
      }
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String emailTo = patternPropertiesValues.get("SendEmailDueToDisabledDate.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailDueToDisabledDate.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailDueToDisabledDate.emailBody");
      
      String minDays = patternPropertiesValues.get("SendEmailDueToDisabledDate.minDays");
      String maxDays = patternPropertiesValues.get("SendEmailDueToDisabledDate.maxDays");
      
      
      if (StringUtils.isBlank(emailTo)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailToRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailSubject)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailSubjectRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailBody)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailBodyRequired");
        errorMessages.add(error);
      }
      
      try {
        Integer.valueOf(minDays);
      } catch (Exception e) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidNumberOfDays");
        error = error.replace("##numberOfDays##", minDays);
        errorMessages.add(error);
      }
      
      try {
        Integer.valueOf(maxDays);
      } catch (Exception e) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidNumberOfDays");
        error = error.replace("##numberOfDays##", maxDays);
        errorMessages.add(error);
      }
      
      return errorMessages;
      
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("SendEmailDueToDisabledDateUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailDueToDisabledDate.minDays");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.minDays.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.minDays.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckArg0());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailDueToDisabledDate.maxDays");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.maxDays.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.maxDays.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckArg1());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailDueToDisabledDate.emailTo");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.emailTo.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.emailTo.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg0());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailDueToDisabledDate.emailSubject");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.emailSubject.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.emailSubject.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg1());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXTAREA);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailDueToDisabledDate.emailBody");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.emailBody.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailDueToDisabledDate.emailBody.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg2());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.membershipDisabledDate &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.sendEmail) {
        return true;
      }
      
      return false;
    }

    @Override
    public boolean isDaemonApplicable() {
      return true;
    }
    
    
  },
  
  VetoIfNewMembershipIsNotAGroup {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String subjectSources = patternPropertiesValues.get("VetoIfNewMembershipIsNotAGroup.subjectSource");
      
      ruleConfig.setCheckType(RuleCheckType.membershipAdd.name());
      ruleConfig.setCheckOwner("thisGroup");
      
      //test
      ruleConfig.setIfConditionOption(RuleIfConditionEnum.subjectNotInSources.name());
      ruleConfig.setIfConditionArg0(subjectSources); //TODO: subjectSources comma separated alphabetical
      
      ruleConfig.setThenOption(RuleThenEnum.veto.name());
      ruleConfig.setThenArg0("rule.entity.must.be.a.group");
      ruleConfig.setThenArg1(GrouperTextContainer.textOrNull("VetoIfNewMembershipIsNotAGroupArg1Message"));
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      return errorMessages;
      
      
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("VetoIfNewMembershipIsNotAGroupUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.CHECKBOX);
        
//        SourceManagerOptionValueDriver driver = new SourceManagerOptionValueDriver();
        
        SubjectFinder finder = new SubjectFinder();
        List<MultiKey> checkboxAttributes = finder.retrieveCheckboxAttributes();
        
        attribute.setCheckboxAttributes(checkboxAttributes);
//        attribute.setDropdownValuesAndLabels(driver.retrieveKeysAndLabels());
        attribute.setShow(true);
        attribute.setConfigSuffix("VetoIfNewMembershipIsNotAGroup.subjectSource");
        attribute.setLabel(GrouperTextContainer.textOrNull("VetoIfNewMembershipIsNotAGroup.subjectSource.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("VetoIfNewMembershipIsNotAGroup.subjectSource.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        
        if (ruleDefinition != null && ruleDefinition.getThen() != null && StringUtils.isNotBlank(ruleDefinition.getThen().getThenEnumArg0())) {
          String arg0 = ruleDefinition.getThen().getThenEnumArg0();
          if (arg0.contains("::::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::::");
            attribute.setValue(subjectSourceAndIdentifier[0]);
          } else if (arg0.contains("::::")) {
            String[] subjectSourceAndIdentifier = GrouperUtil.splitTrim(arg0, "::::");
            attribute.setValue(subjectSourceAndIdentifier[0]);
          }
        }
        
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.membershipAdd &&
          ruleDefinition.getIfCondition() != null && ruleDefinition.getIfCondition().ifConditionEnum() == RuleIfConditionEnum.subjectNotInSources &&
          StringUtils.equals("g:gsa", ruleDefinition.getIfCondition().getIfConditionEnumArg0()) && 
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.veto) {
        return true;
      }
      
      return false;
    }
    
  },
  
  VetoIfNotEligibleDueToFolder {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String folder = patternPropertiesValues.get("VetoIfNotEligibleDueToFolder.folder");
      String stemScope = patternPropertiesValues.get("VetoIfNotEligibleDueToFolder.stemScope");
      
      ruleConfig.setCheckType(RuleCheckType.membershipAdd.name());
      ruleConfig.setCheckOwner("thisGroup");
      
      ruleConfig.setIfConditionOption(RuleIfConditionEnum.noGroupInFolderHasImmediateEnabledMembership.name());
      ruleConfig.setIfConditionOwnerUuidOrName(folder);
      ruleConfig.setIfConditionOwnerStemScope(stemScope);
      
      ruleConfig.setThenOption(RuleThenEnum.veto.name());
      ruleConfig.setThenArg0("rule.entity.must.be.in.IT.employee.to.be.in.group");
      
      
      String arg1Message = GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToFolderArg1Message");
      arg1Message = arg1Message.replace("##targetFolder##", folder);
      ruleConfig.setThenArg1(arg1Message);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String folderName = patternPropertiesValues.get("VetoIfNotEligibleDueToFolder.folder");
      String stemScope = patternPropertiesValues.get("VetoIfNotEligibleDueToFolder.stemScope");
      
      Stem stem = StemFinder.findByName(folderName, false);
      if (stem == null) {
        stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folderName, false);
      }
      
      if (stem == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidFolder");
        error = error.replace("##folderUuidOrName##", folderName);
        errorMessages.add(error);
      }
      
      if (stem != null && loggedInSubject != null) {
        if (!stem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotAdminStem");
          error = error.replace("##stemName##", stem.getName());
          errorMessages.add(error);
        }
      }
      
      if (StringUtils.isBlank(stemScope)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeRequired");
        errorMessages.add(error);
      } else {
        if (!StringUtils.equals(stemScope, "SUB") && !StringUtils.equals(stemScope, "ONE")) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeInvalid");
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
      
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToFolderUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("VetoIfNotEligibleDueToFolder.folder");
        attribute.setLabel(GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToFolder.folder.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToFolder.folder.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getIfCondition() != null) {
          attribute.setValue(ruleDefinition.getIfCondition().getIfOwnerName());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("VetoIfNotEligibleDueToFolder.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToFolder.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToFolder.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getIfCondition() != null) {
          attribute.setValue(ruleDefinition.getIfCondition().getIfStemScope());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.membershipAdd &&
          ruleDefinition.getIfCondition() != null && ruleDefinition.getIfCondition().ifConditionEnum() == RuleIfConditionEnum.noGroupInFolderHasImmediateEnabledMembership && 
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.veto &&
          StringUtils.equals(ruleDefinition.getThen().getThenEnumArg0(), "rule.entity.must.be.in.IT.employee.to.be.in.group") &&
          StringUtils.startsWith(ruleDefinition.getThen().getThenEnumArg1(), GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToFolderArg1MessagePrefix"))) {
        return true;
      }
      
      return false;
    }
    
    @Override
    public boolean isDaemonApplicable() {
      return true;
    }
    
  },
  
  VetoIfNotEligibleDueToGroup {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String group = patternPropertiesValues.get("VetoIfNotEligibleDueToGroup.group");
      
      String checkIfRemovedFromGroup = patternPropertiesValues.get("VetoIfNotEligibleDueToGroup.checkIfRemovedFromGroup");
      
      if (StringUtils.equals(checkIfRemovedFromGroup, "T")) {
        ruleConfig.setCheckType(RuleCheckType.membershipRemove.name());
      } else if (StringUtils.equals(checkIfRemovedFromGroup, "F")) {
        ruleConfig.setCheckType(RuleCheckType.membershipAdd.name());
      }
      
      ruleConfig.setCheckOwnerUuidOrName(ruleConfig.getGrouperObject().getName());
      ruleConfig.setCheckOwner("thisGroup");
      
      ruleConfig.setIfConditionOption(RuleIfConditionEnum.groupHasNoEnabledMembership.name());
      ruleConfig.setIfConditionOwnerUuidOrName(group);
      ruleConfig.setIfConditionOwner("anotherGroup");
      
      ruleConfig.setThenOption(RuleThenEnum.veto.name());
      ruleConfig.setThenArg0("rule.entity.must.be.a.member.of.stem.b");
      
      String arg1Message = GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToGroupArg1Message");
      arg1Message = arg1Message.replace("##targetGroup##", group);
      ruleConfig.setThenArg1(arg1Message);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String groupName = patternPropertiesValues.get("VetoIfNotEligibleDueToGroup.group");
      
      Group group = GroupFinder.findByName(groupName, false);
      if (group == null) {
        group = GroupFinder.findByUuid(groupName, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidGroup");
        error = error.replace("##groupUuidOrName##", groupName);
        errorMessages.add(error);
      }
      
      if (group != null && loggedInSubject != null) {
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotReadGroup");
          error = error.replace("##groupName##", group.getName());
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToGroupUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("T", GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToGroup.checkIfRemovedFromGroup"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("F", GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToGroup.checkIfAddedToGroup"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("VetoIfNotEligibleDueToGroup.checkIfRemovedFromGroup");
        attribute.setLabel(GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToGroup.checkIfRemovedFromGroup.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToGroup.checkIfRemovedFromGroup.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          if (ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipRemove) {            
            attribute.setValue("T");
          } else if (ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAdd) {
            attribute.setValue("F");
          }
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("VetoIfNotEligibleDueToGroup.group");
        attribute.setLabel(GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToGroup.group.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToGroup.group.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckOwnerName());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && 
          (ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.membershipAdd || ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.membershipRemove) &&
          ruleDefinition.getIfCondition() != null && (ruleDefinition.getIfCondition().ifConditionEnum() == RuleIfConditionEnum.groupHasNoEnabledMembership || 
              ruleDefinition.getIfCondition().ifConditionEnum() == RuleIfConditionEnum.groupHasNoImmediateEnabledMembership) && 
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.veto &&
          StringUtils.equals(ruleDefinition.getThen().getThenEnumArg0(), "rule.entity.must.be.a.member.of.stem.b") &&
          StringUtils.startsWith(ruleDefinition.getThen().getThenEnumArg1(), GrouperTextContainer.textOrNull("VetoIfNotEligibleDueToGroupArg1MessagePrefix"))) {
        return true;
      }
      
      return false;
    }
    
    @Override
    public boolean isDaemonApplicable() {
      return true;
    }
    
  },
  
  VetoIfTooManyMembers {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String limit = patternPropertiesValues.get("VetoIfTooManyMembers.limit");
      
      ruleConfig.setCheckType(RuleCheckType.membershipAdd.name());
      ruleConfig.setCheckOwner("thisGroup");
      
      ruleConfig.setIfConditionOption(RuleIfConditionEnum.groupHasTooManyMembers.name());
      ruleConfig.setIfConditionOwnerUuidOrName(ruleConfig.getGrouperObject().getName());
      ruleConfig.setIfConditionArg0(limit);
      ruleConfig.setIfConditionOwner("thisGroup");
      
      ruleConfig.setThenOption(RuleThenEnum.veto.name());
      ruleConfig.setThenArg0("rule.group.has.too.many.members");
      ruleConfig.setThenArg1(GrouperTextContainer.textOrNull("VetoIfTooManyMembersArg1Message"));
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String limit = patternPropertiesValues.get("VetoIfTooManyMembers.limit");
      
      try {
        Integer.valueOf(limit);
      } catch (Exception e) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidNumber");
        error = error.replace("##number##", limit);
        errorMessages.add(error);
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("VetoIfTooManyMembersUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
            
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("VetoIfTooManyMembers.limit");
        attribute.setLabel(GrouperTextContainer.textOrNull("VetoIfTooManyMembers.limit.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("VetoIfTooManyMembers.limit.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getIfCondition() != null) {
          attribute.setValue(ruleDefinition.getIfCondition().getIfConditionEnumArg0());
        }
        elements.add(attribute);
      }
      
      return elements;
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.membershipAdd &&
          ruleDefinition.getIfCondition() != null && ruleDefinition.getIfCondition().ifConditionEnum() == RuleIfConditionEnum.groupHasTooManyMembers && 
          StringUtils.isNotBlank(ruleDefinition.getIfCondition().getIfConditionEnumArg0()) && 
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.veto && 
          StringUtils.equals(ruleDefinition.getThen().getThenEnumArg0(), "rule.group.has.too.many.members") &&
          StringUtils.equals(ruleDefinition.getThen().getThenEnumArg1(), GrouperTextContainer.textOrNull("VetoIfTooManyMembersArg1Message"))) {
        return true;
      }
      
      return false;
    }
    
  },
  
  RemoveInvalidMembershipDueToFolder {

    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String folder = patternPropertiesValues.get("RemoveInvalidMembershipDueToFolder.folder");
      String folderScope = patternPropertiesValues.get("RemoveInvalidMembershipDueToFolder.stemScope");
      
      ruleConfig.setCheckType(RuleCheckType.membershipRemoveInFolder.name());
      ruleConfig.setCheckOwnerUuidOrName(folder);
      ruleConfig.setCheckOwnerStemScope(folderScope);
      
      //test
      ruleConfig.setIfConditionOption(RuleIfConditionEnum.thisGroupAndNotFolderHasImmediateEnabledMembership.name());

      ruleConfig.setThenOption(RuleThenEnum.removeMemberFromOwnerGroup.name());
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }

    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String folder = patternPropertiesValues.get("RemoveInvalidMembershipDueToFolder.folder");
      String folderScope = patternPropertiesValues.get("RemoveInvalidMembershipDueToFolder.stemScope");
      
      Stem stem = StemFinder.findByName(folder, false);
      if (stem == null) {
        stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folder, false);
      }
      
      if (stem == null) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidFolder");
        error = error.replace("##folderUuidOrName##", folder);
        errorMessages.add(error);
      }
      
      if (stem != null && loggedInSubject != null) {
        if (!stem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditCannotAdminStem");
          error = error.replace("##stemName##", stem.getName());
          errorMessages.add(error);
        }
      }
      
      if (StringUtils.isBlank(folderScope)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeRequired");
        errorMessages.add(error);
      } else {
        if (!StringUtils.equals(folderScope, "SUB") && !StringUtils.equals(folderScope, "ONE")) {
          String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditFolderScopeInvalid");
          errorMessages.add(error);
        }
      }
      
      return errorMessages;
    }

    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToFolderUserFriendlyText");
    }

    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("RemoveInvalidMembershipDueToFolder.folder");
        attribute.setLabel(GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToFolder.folder.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToFolder.folder.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckOwnerName());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.DROPDOWN);
        List<MultiKey> valuesAndLabels = new ArrayList<>();
        MultiKey valueAndLabel = new MultiKey("SUB", GrouperTextContainer.textOrNull("grouperRuleOwnerSubStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        valueAndLabel = new MultiKey("ONE", GrouperTextContainer.textOrNull("grouperRuleOwnerOneStemScopeLabel"));
        valuesAndLabels.add(valueAndLabel);
        attribute.setDropdownValuesAndLabels(valuesAndLabels);
        attribute.setShow(true);
        attribute.setConfigSuffix("RemoveInvalidMembershipDueToFolder.stemScope");
        attribute.setLabel(GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToFolder.stemScope.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("RemoveInvalidMembershipDueToFolder.stemScope.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getCheck() != null) {
          attribute.setValue(ruleDefinition.getCheck().getCheckStemScope());
        }
        elements.add(attribute);
      }
     
      return elements;
      
    }

    @Override
    public boolean isApplicableForFolders() {
      return false;
    }

    @Override
    public boolean isApplicableForGroups() {
      return true;
    }

    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.membershipRemoveInFolder &&
          ruleDefinition.getIfCondition() != null && ruleDefinition.getIfCondition().ifConditionEnum() == RuleIfConditionEnum.thisGroupAndNotFolderHasImmediateEnabledMembership && 
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.removeMemberFromOwnerGroup) {
        return true;
      }
      
      
      return false;
    }
    
    @Override
    public boolean isDaemonApplicable() {
      return true;
    }
     
  }, SendEmailAfterNewMembership{
  
    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig,String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String emailTo = patternPropertiesValues.get("SendEmailAfterNewMembership.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailAfterNewMembership.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailAfterNewMembership.emailBody");
      
      ruleConfig.setCheckType(RuleCheckType.flattenedMembershipAdd.name());
      ruleConfig.setCheckOwner("thisGroup");
      
      ruleConfig.setThenOption(RuleThenEnum.sendEmail.name());
      ruleConfig.setThenArg0(emailTo);
      ruleConfig.setThenArg1(emailSubject);
      ruleConfig.setThenArg2(emailBody);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }
  
    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      boolean skipEmailSenderCheck = false;
      if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        skipEmailSenderCheck = true;
      }
      
      if (!skipEmailSenderCheck) {
        Boolean subjectInCache = emailSenders.get(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()));
        if (subjectInCache != null) {
          if (subjectInCache == false) {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
            errorMessages.add(error);
            return errorMessages;
          }
        } else {
          String emailSenderGroupName = GrouperConfig.retrieveConfig().propertyValueString("rules.restrictRulesEmailSendersToMembersOfThisGroupName", "");
          if (StringUtils.isNotBlank(emailSenderGroupName)) {
            Group emailSenderGroup = GroupFinder.findByName(emailSenderGroupName, false);
            if (emailSenderGroup != null) {
              if (emailSenderGroup.hasMember(loggedInSubject)) {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), true);
              } else {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), false);
                String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
                errorMessages.add(error);
                return errorMessages;
              }
              
            } else {
              LOG.warn("rules.restrictRulesEmailSendersToMembersOfThisGroupName is set to '"+emailSenderGroupName+"' and it does not exist.");
            }
          }
        }
      }
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String emailTo = patternPropertiesValues.get("SendEmailAfterNewMembership.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailAfterNewMembership.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailAfterNewMembership.emailBody");
      
      
      if (StringUtils.isBlank(emailTo)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailToRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailSubject)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailSubjectRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailBody)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailBodyRequired");
        errorMessages.add(error);
      }
      
      return errorMessages;
    }
  
    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("SendEmailAfterNewMembershipUserFriendlyText");
    }
  
    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailAfterNewMembership.emailTo");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailAfterNewMembership.emailTo.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailAfterNewMembership.emailTo.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg0());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailAfterNewMembership.emailSubject");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailAfterNewMembership.emailSubject.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailAfterNewMembership.emailSubject.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg1());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXTAREA);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailAfterNewMembership.emailBody");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailAfterNewMembership.emailBody.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailAfterNewMembership.emailBody.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg2());
        }
        elements.add(attribute);
      }
      
      return elements;
    }
  
    @Override
    public boolean isApplicableForFolders() {
      return false;
    }
  
    @Override
    public boolean isApplicableForGroups() {
      return true;
    }
  
    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
      
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipAdd &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.sendEmail) {
        return true;
      }
      
      return false;
    }
    
  }, SendEmailAfterMembershipRemove{
  
    @Override
    public Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId) {
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String emailTo = patternPropertiesValues.get("SendEmailAfterMembershipRemove.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailAfterMembershipRemove.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailAfterMembershipRemove.emailBody");
      
      
      ruleConfig.setCheckOwner("thisStem");
      ruleConfig.setCheckType(RuleCheckType.flattenedMembershipRemove.name());
      
      ruleConfig.setThenOption(RuleThenEnum.sendEmail.name());
      ruleConfig.setThenArg0(emailTo);
      ruleConfig.setThenArg1(emailSubject);
      ruleConfig.setThenArg2(emailBody);
      
      Map<String, List<String>> result = RuleService.saveOrUpdateRuleAttributes(ruleConfig, ruleConfig.getGrouperObject(), attributeAssignId);
      return result;
    }
  
    @Override
    public List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject) {
      
      List<String> errorMessages = new ArrayList<>();
      
      boolean skipEmailSenderCheck = false;
      if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
        skipEmailSenderCheck = true;
      }
      
      if (!skipEmailSenderCheck) {
        Boolean subjectInCache = emailSenders.get(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()));
        if (subjectInCache != null) {
          if (subjectInCache == false) {
            String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
            errorMessages.add(error);
            return errorMessages;
          }
        } else {
          String emailSenderGroupName = GrouperConfig.retrieveConfig().propertyValueString("rules.restrictRulesEmailSendersToMembersOfThisGroupName", "");
          if (StringUtils.isNotBlank(emailSenderGroupName)) {
            Group emailSenderGroup = GroupFinder.findByName(emailSenderGroupName, false);
            if (emailSenderGroup != null) {
              if (emailSenderGroup.hasMember(loggedInSubject)) {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), true);
              } else {
                emailSenders.put(new MultiKey(loggedInSubject.getSource(), loggedInSubject.getId()), false);
                String error = GrouperTextContainer.textOrNull("grouperRuleConfigNotInEmailSenderGroup");
                errorMessages.add(error);
                return errorMessages;
              }
              
            } else {
              LOG.warn("rules.restrictRulesEmailSendersToMembersOfThisGroupName is set to '"+emailSenderGroupName+"' and it does not exist.");
            }
          }
        }
      }
      
      Map<String,String> patternPropertiesValues = ruleConfig.getPatternPropertiesValues();
      
      String emailTo = patternPropertiesValues.get("SendEmailAfterMembershipRemove.emailTo");
      String emailSubject = patternPropertiesValues.get("SendEmailAfterMembershipRemove.emailSubject");
      String emailBody = patternPropertiesValues.get("SendEmailAfterMembershipRemove.emailBody");
      
      
      if (StringUtils.isBlank(emailTo)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailToRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailSubject)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailSubjectRequired");
        errorMessages.add(error);
      }
      if (StringUtils.isBlank(emailBody)) {
        String error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditEmailBodyRequired");
        errorMessages.add(error);
      }
      
      return errorMessages;
    }
  
    @Override
    public String getUserFriendlyText() {
      return GrouperTextContainer.textOrNull("SendEmailAfterMembershipRemoveUserFriendlyText");
    }
  
    @Override
    public List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition) {
      
      List<GrouperConfigurationModuleAttribute> elements = new ArrayList<>();
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailAfterMembershipRemove.emailTo");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailAfterMembershipRemove.emailTo.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailAfterMembershipRemove.emailTo.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg0());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailAfterMembershipRemove.emailSubject");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailAfterMembershipRemove.emailSubject.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailAfterMembershipRemove.emailSubject.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg1());
        }
        elements.add(attribute);
      }
      
      {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        attribute.setFormElement(ConfigItemFormElement.TEXTAREA);
        attribute.setShow(true);
        attribute.setConfigSuffix("SendEmailAfterMembershipRemove.emailBody");
        attribute.setLabel(GrouperTextContainer.textOrNull("SendEmailAfterMembershipRemove.emailBody.label"));
        attribute.setDescription(GrouperTextContainer.textOrNull("SendEmailAfterMembershipRemove.emailBody.description"));
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setRequired(true);
        attribute.setConfigItemMetadata(configItemMetadata);
        if (ruleDefinition != null && ruleDefinition.getThen() != null) {
          attribute.setValue(ruleDefinition.getThen().getThenEnumArg2());
        }
        elements.add(attribute);
      }
      
      return elements;
    }
  
    @Override
    public boolean isApplicableForFolders() {
      return false;
    }
  
    @Override
    public boolean isApplicableForGroups() {
      return true;
    }
  
    @Override
    public boolean isThisThePattern(RuleDefinition ruleDefinition) {
  
      if (ruleDefinition.getCheck() != null && ruleDefinition.getCheck().checkTypeEnum() == RuleCheckType.flattenedMembershipRemove &&
          ruleDefinition.getIfCondition().isBlank() &&
          ruleDefinition.getThen() != null && ruleDefinition.getThen().thenEnum() == RuleThenEnum.sendEmail) {
        return true;
      }
      
      return false;
    }
    
  };
  
  
  public abstract Map<String, List<String>> save(RuleConfig ruleConfig, String attributeAssignId);
  
  /**
   * validate the config that's being saved. If logged in subject is null then don't check if the subject can access all the properties
   * @param ruleConfig
   * @param loggedInSubject
   * @return
   */
  public abstract List<String> validate(RuleConfig ruleConfig, Subject loggedInSubject);
  
  public abstract String getUserFriendlyText();
  
  public abstract List<GrouperConfigurationModuleAttribute> getElementsToShow(GrouperObject grouperObject, RuleDefinition ruleDefinition);
  
  public abstract boolean isApplicableForFolders();

  public abstract boolean isApplicableForGroups();
  
  /**
   * based on rule config attributes, check if the current enum is the pattern
   * @param ruleConfig
   * @return
   */
  public abstract boolean isThisThePattern(RuleDefinition ruleDefinition);
  
  public boolean isDaemonApplicable() {
    return false;
  }
  
  public boolean isDaemonAssignableByNonAdmin() {
    return false;
  }
  
  private static final ExpirableCache<MultiKey, Boolean> emailSenders = new ExpirableCache<MultiKey, Boolean>(5);
  
  /** logger */
  protected static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(RulePattern.class);

}
