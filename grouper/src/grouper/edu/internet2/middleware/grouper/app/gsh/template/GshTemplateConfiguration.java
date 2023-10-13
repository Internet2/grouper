package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class GshTemplateConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperGshTemplate." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperGshTemplate)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperGshTemplate";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "testGshTemplate";
  }
  
  /**
   * list of configured gsh template configs
   * @return
   */
  public static List<GshTemplateConfiguration> retrieveAllGshTemplateConfigs() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GshTemplateConfiguration.class.getName());
   return (List<GshTemplateConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    GrouperConfigurationModuleAttribute showOnGroupsAttribute = attributes.get("showOnGroups");
    String showOnGroupsValue = showOnGroupsAttribute.getValueOrExpressionEvaluation();
    
    boolean showTemplateOnAllGroups = true;
    Set<Group> groupsWhereTemplateIsAvailable = null;
    Stem stemUnderWhichAnyGroupCanHaveTemplate = null;
    
    if (GrouperUtil.booleanValue(showOnGroupsValue, false)) {
      GrouperConfigurationModuleAttribute groupShowTypeAttribute = attributes.get("groupShowType");
      String groupShowTypeValue = groupShowTypeAttribute.getValueOrExpressionEvaluation();
      
      GshTemplateGroupShowType groupShowType = GshTemplateGroupShowType.valueOfIgnoreCase(groupShowTypeValue, true);
      
      if (groupShowType == GshTemplateGroupShowType.certainGroups) {
        showTemplateOnAllGroups = false;
        GrouperConfigurationModuleAttribute groupUuidsToShowAttribute = attributes.get("groupUuidsToShow");
        String groupUuidsToShow = groupUuidsToShowAttribute.getValueOrExpressionEvaluation();
        
        String[] groupUuidsOrNamesToShow = GrouperUtil.splitTrim(groupUuidsToShow, ",");
        
        Set<String> groupUuidOrNamesToShowIn = GrouperUtil.toSet(groupUuidsOrNamesToShow);
        
        groupsWhereTemplateIsAvailable = new GroupFinder()
          .assignGroupNames(groupUuidOrNamesToShowIn)
          .findGroups();
        
        groupsWhereTemplateIsAvailable.addAll(new GroupFinder()
          .assignGroupIds(groupUuidOrNamesToShowIn)
          .findGroups());
        
        if (groupsWhereTemplateIsAvailable.size() < groupUuidOrNamesToShowIn.size()) {
          
          Set<String> groupIdsThatWereFound = groupsWhereTemplateIsAvailable.stream().map(group -> group.getId()).collect(Collectors.toSet());
          Set<String> groupNamesThatWereFound = groupsWhereTemplateIsAvailable.stream().map(group -> group.getName()).collect(Collectors.toSet());
         
          for (String groupUuidOrNameOnUi: groupUuidOrNamesToShowIn) {
            if (!groupIdsThatWereFound.contains(groupUuidOrNameOnUi) && !groupNamesThatWereFound.contains(groupUuidOrNameOnUi)) {
              String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorGroupNotFound");
              error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrNameOnUi);
              validationErrorsToDisplay.put(groupUuidsToShowAttribute.getHtmlForElementIdHandle(), error);
            }
          }
        }
      } else if (groupShowType == GshTemplateGroupShowType.groupsInFolder) {
        
        showTemplateOnAllGroups = false;
        
        GrouperConfigurationModuleAttribute folderUuidForGroupsInFolderAttribute = attributes.get("folderUuidForGroupsInFolder");
        String folderUuidForGroupsInFolder = folderUuidForGroupsInFolderAttribute.getValueOrExpressionEvaluation();
        
        stemUnderWhichAnyGroupCanHaveTemplate = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folderUuidForGroupsInFolder, false);
        if (stemUnderWhichAnyGroupCanHaveTemplate == null) {
          stemUnderWhichAnyGroupCanHaveTemplate = StemFinder.findByName(GrouperSession.staticGrouperSession(), folderUuidForGroupsInFolder, false);
        }
        
        if (stemUnderWhichAnyGroupCanHaveTemplate == null) {
          String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorFolderNotFound");
          error = GrouperUtil.replace(error, "$$folderUUIDOrName$$", folderUuidForGroupsInFolder);
          validationErrorsToDisplay.put(folderUuidForGroupsInFolderAttribute.getHtmlForElementIdHandle(), error);
        }
        
      }
    }
    
    GrouperConfigurationModuleAttribute showOnFoldersAttribute = attributes.get("showOnFolders");
    String showOnFoldersValue = showOnFoldersAttribute.getValueOrExpressionEvaluation();
    
    if (GrouperUtil.booleanValue(showOnFoldersValue, false)) {
      GrouperConfigurationModuleAttribute folderShowTypeAttribute = attributes.get("folderShowType");
      String folderShowTypeValue = folderShowTypeAttribute.getValueOrExpressionEvaluation();
      
      GshTemplateFolderShowType folderShowType = GshTemplateFolderShowType.valueOfIgnoreCase(folderShowTypeValue, true);
      
      if (folderShowType == GshTemplateFolderShowType.certainFolders) {
        showTemplateOnAllGroups = false;
        GrouperConfigurationModuleAttribute folderUuidsToShowAttribute = attributes.get("folderUuidToShow");
        String folderUuidsToShow = folderUuidsToShowAttribute.getValueOrExpressionEvaluation();
        
        String[] folderUuidsOrNamesToShow = GrouperUtil.splitTrim(folderUuidsToShow, ",");
        
        Set<String> folderUuidOrNamesToShowIn = GrouperUtil.toSet(folderUuidsOrNamesToShow);
        
        Set<Stem> stemsToShowTemplateIn = new StemFinder()
          .assignStemNames(folderUuidOrNamesToShowIn)
          .findStems();
        
        stemsToShowTemplateIn.addAll(new StemFinder()
            .assignStemIds(folderUuidOrNamesToShowIn)
            .findStems());
        
        if (stemsToShowTemplateIn.size() < folderUuidOrNamesToShowIn.size()) {
          
          Set<String> stemIdsThatWereFound = stemsToShowTemplateIn.stream().map(stem -> stem.getId()).collect(Collectors.toSet());
          Set<String> stemNamesThatWereFound = stemsToShowTemplateIn.stream().map(stem -> stem.getName()).collect(Collectors.toSet());
         
          for (String folderUuidOrNameOnUi: folderUuidOrNamesToShowIn) {
            if (!stemIdsThatWereFound.contains(folderUuidOrNameOnUi) && !stemNamesThatWereFound.contains(folderUuidOrNameOnUi)) {
              String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorFolderNotFound");
              error = GrouperUtil.replace(error, "$$folderUUIDOrName$$", folderUuidOrNameOnUi);
              validationErrorsToDisplay.put(folderUuidsToShowAttribute.getHtmlForElementIdHandle(), error);
            }
          }
        }
      }
    }
    
    GrouperConfigurationModuleAttribute runButtonGroupOrFolderAttribute = attributes.get("runButtonGroupOrFolder");
    String runButtonGroupOrFolderAttributeValue = runButtonGroupOrFolderAttribute.getValueOrExpressionEvaluation();
    
    if (StringUtils.equals("group", runButtonGroupOrFolderAttributeValue)) {
      
      GrouperConfigurationModuleAttribute defaultRunButtonGroupUuidOrNameAttribute = attributes.get("defaultRunButtonGroupUuidOrName");
      String groupUuidOrName = defaultRunButtonGroupUuidOrNameAttribute.getValueOrExpressionEvaluation();
      
      Group group = GroupFinder.findByUuid(groupUuidOrName, false);
      if (group == null) {
        group = GroupFinder.findByName(groupUuidOrName, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorGroupNotFound");
        error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrName);
        validationErrorsToDisplay.put(defaultRunButtonGroupUuidOrNameAttribute.getHtmlForElementIdHandle(), error);
      } else {
        
        // we still need to check if the default run group is one of the groups where the template can show 
        if (!showTemplateOnAllGroups) {
          if (groupsWhereTemplateIsAvailable != null && !groupsWhereTemplateIsAvailable.contains(group)) {
            String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorDefaultRunGroupNotInGroupsToShowList");
            error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrName);
            validationErrorsToDisplay.put(defaultRunButtonGroupUuidOrNameAttribute.getHtmlForElementIdHandle(), error);
          } else if (stemUnderWhichAnyGroupCanHaveTemplate != null) {
            
            GrouperConfigurationModuleAttribute groupShowOnDescendantsAttribute = attributes.get("groupShowOnDescendants");
            String groupShowOnDescendants = groupShowOnDescendantsAttribute.getValueOrExpressionEvaluation();
            
            GshTemplateGroupShowOnDescendants showOnDescendants = GshTemplateGroupShowOnDescendants.valueOfIgnoreCase(groupShowOnDescendants, true);
            if (GshTemplateGroupShowOnDescendants.descendants == showOnDescendants && !stemUnderWhichAnyGroupCanHaveTemplate.isChildGroup(group)) {
              String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorDefaultRunGroupNotUnderFolderToShow");
              error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrName);
              validationErrorsToDisplay.put(defaultRunButtonGroupUuidOrNameAttribute.getHtmlForElementIdHandle(), error);
            } else if (GshTemplateGroupShowOnDescendants.oneChildLevel == showOnDescendants && !stemUnderWhichAnyGroupCanHaveTemplate.getChildGroups(Scope.ONE).contains(group)) {
              String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorDefaultRunGroupNotUnderFolderToShow");
              error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrName);
              error = GrouperUtil.replace(error, "$$folderUUIDOrName$$", stemUnderWhichAnyGroupCanHaveTemplate.getName());
              validationErrorsToDisplay.put(defaultRunButtonGroupUuidOrNameAttribute.getHtmlForElementIdHandle(), error);
            }
          }
        }
      }
      
    } else if (StringUtils.equals("folder", runButtonGroupOrFolderAttributeValue)) {
      
      GrouperConfigurationModuleAttribute defaultRunButtonFolderUuidOrNameAttribute = attributes.get("defaultRunButtonFolderUuidOrName");
      String folderUuidOrName = defaultRunButtonFolderUuidOrNameAttribute.getValueOrExpressionEvaluation();
      
      Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folderUuidOrName, false);
      if (stem == null) {
        stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), folderUuidOrName, false);
      }
      
      if (stem == null) {
        String error = GrouperTextContainer.textOrNull("customUiConfigSaveErrorFolderNotFound");
        error = GrouperUtil.replace(error, "$$folderUUIDOrName$$", folderUuidOrName);
        validationErrorsToDisplay.put(defaultRunButtonFolderUuidOrNameAttribute.getHtmlForElementIdHandle(), error);
      } else {
        // we still need to check if the default run folder is one of the folders where the template can show
        if (!canDefaultRunFolderShowTemplate(stem)) {
          String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorDefaultRunFolderNotInFoldersToShowList");
          error = GrouperUtil.replace(error, "$$folderUUIDOrName$$", folderUuidOrName);
          validationErrorsToDisplay.put(defaultRunButtonFolderUuidOrNameAttribute.getHtmlForElementIdHandle(), error);
        }
      }
      
    }
    
    GrouperConfigurationModuleAttribute numberOfInputsAttribute = attributes.get("numberOfInputs");
    
    String valueOrExpressionEvaluation = numberOfInputsAttribute.getValueOrExpressionEvaluation();
    
    int numberOfInputs = GrouperUtil.intValue(valueOrExpressionEvaluation, 0);
    
    for (int i=0; i<numberOfInputs; i++) {
      GrouperConfigurationModuleAttribute nameAttribute = attributes.get("input."+i+".name");
      String nameAttributeValue = nameAttribute.getValueOrExpressionEvaluation();
      if (!nameAttributeValue.startsWith("gsh_input_") || !nameAttributeValue.matches("^[a-zA-Z0-9_]+$")) {
        String error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorInputNotValidFormat");
        validationErrorsToDisplay.put(nameAttribute.getHtmlForElementIdHandle(), error);
        return;
      }
      GrouperConfigurationModuleAttribute defaultValueAttribute = attributes.get("input."+i+".defaultValue");
      if (defaultValueAttribute != null && StringUtils.isNotBlank(defaultValueAttribute.getValueOrExpressionEvaluation())) {
        String valueBeforeConversion = defaultValueAttribute.getValueOrExpressionEvaluation();
        GrouperConfigurationModuleAttribute typeAttribute = attributes.get("input."+i+".type");
        
        GshTemplateInputType templateInputType = null;
        if (typeAttribute == null || StringUtils.isBlank(typeAttribute.getValueOrExpressionEvaluation())) {
          templateInputType = GshTemplateInputType.STRING;
        } else {
          templateInputType = GshTemplateInputType.valueOfIgnoreCase(typeAttribute.getValueOrExpressionEvaluation(), true);
        }
        
        if (!templateInputType.canConvertToCorrectType(valueBeforeConversion)) {
          String error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorInputDefaultValueNotCorrectType");
          error = GrouperUtil.replace(error, "$$defaultValue$$", valueBeforeConversion);
          error = GrouperUtil.replace(error, "$$selectedType$$", templateInputType.name().toLowerCase());
          validationErrorsToDisplay.put(defaultValueAttribute.getHtmlForElementIdHandle(), error);
          return;
        }
        
      }
    }
    
  }
  
  private boolean canDefaultRunFolderShowTemplate(Stem defaultRunFolder) {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    GrouperConfigurationModuleAttribute folderShowTypeAttribute = attributes.get("folderShowType");
    String folderShowTypeValue = folderShowTypeAttribute.getValueOrExpressionEvaluation();
    
    GshTemplateFolderShowType folderShowType = GshTemplateFolderShowType.valueOfIgnoreCase(folderShowTypeValue, true);
    if (folderShowType == GshTemplateFolderShowType.allFolders) {
      return true;
    }
    
    GrouperConfigurationModuleAttribute folderUuidsToShowAttribute = attributes.get("folderUuidToShow");
    String folderUuidsToShow = folderUuidsToShowAttribute.getValueOrExpressionEvaluation();
    
    String[] folderUuidsOrNamesToShow = GrouperUtil.splitTrim(folderUuidsToShow, ",");
    
    Set<String> folderUuidOrNamesToShowIn = GrouperUtil.toSet(folderUuidsOrNamesToShow);
    
    Set<Stem> stems = new StemFinder().assignStemIds(folderUuidOrNamesToShowIn).findStems();
    stems.addAll(new StemFinder().assignStemNames(folderUuidOrNamesToShowIn).findStems());
    
    GrouperConfigurationModuleAttribute folderShowOnDescendantsAttribute = attributes.get("folderShowOnDescendants");
    String folderShowOnDescendants = folderShowOnDescendantsAttribute.getValueOrExpressionEvaluation();
    
    GshTemplateFolderShowOnDescendants showOnDescendants = GshTemplateFolderShowOnDescendants.valueOfIgnoreCase(folderShowOnDescendants, true);
    if (showOnDescendants == GshTemplateFolderShowOnDescendants.certainFolders && stems.contains(defaultRunFolder)) {
      return true;
    } else if (showOnDescendants == GshTemplateFolderShowOnDescendants.oneChildLevel) {
      
      for (Stem folderToShow: stems) {
        if (StringUtils.equals(GrouperUtil.parentStemNameFromName(defaultRunFolder.getName(), false), folderToShow.getName())) {
          return true;
        }
      }
      
      return false;
    } else if (showOnDescendants == GshTemplateFolderShowOnDescendants.certainFoldersAndOneChildLevel) {
      
      if (stems.contains(defaultRunFolder)) {            
        return true;
      } 
      
      for (Stem folderToShow: stems) {
        if (StringUtils.equals(GrouperUtil.parentStemNameFromName(defaultRunFolder.getName(), false), folderToShow.getName())) {
          return true;
        }
      }
      
      return false;
    } else if (showOnDescendants == GshTemplateFolderShowOnDescendants.descendants) {
      
      for (Stem folderToShow: stems) {
        if (defaultRunFolder.getName().startsWith(folderToShow.getName()+":")) {
          return true;
        }
      }
      
      return false;
    } else if (showOnDescendants == GshTemplateFolderShowOnDescendants.certainFoldersAndDescendants) {
      
      for (Stem folderToShow: stems) {
        if (defaultRunFolder.getName().startsWith(folderToShow.getName()+":")) {
          return true;
        }
      }
      
      if (stems.contains(defaultRunFolder)) {
        return true;
      }
      
      return false;
    }
     
    return false;
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay, final List<String> actionsPerformed) {
    
    final String configId = this.getConfigId();
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
       new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        GshTemplateConfiguration.super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) { 
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GSH_TEMPLATE_ADD,
              "gshTemplateConfigId", configId);
          auditEntry.setDescription("Add gsh template with configId: " + configId); 
          auditEntry.saveOrUpdate(true);
          
        }
        return null;
       
      }
      
    });
    ConfigPropertiesCascadeBase.clearCache();

  }
  
  @Override
  public void editConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    
      final String configId = this.getConfigId();
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
         new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          
          GshTemplateConfiguration.super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
          if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) { 
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GSH_TEMPLATE_UPDATE,
                "gshTemplateConfigId", configId);
            auditEntry.setDescription("Update gsh template with configId: " + configId); 
            auditEntry.saveOrUpdate(true);
            
          }
          return null;
          
        }
        
      });
      ConfigPropertiesCascadeBase.clearCache();

  }
  
  @Override
  public void deleteConfig(boolean fromUi) {
    
    final String configId = this.getConfigId();
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
       new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        GshTemplateConfiguration.super.deleteConfig(fromUi);
        AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GSH_TEMPLATE_DELETE,
            "gshTemplateConfigId", configId);
        auditEntry.setDescription("Delete gsh template with configId: " + configId); 
        auditEntry.saveOrUpdate(true);
        return null;
        
      }
      
    });
    ConfigPropertiesCascadeBase.clearCache();

  }
  
  public String getDefaultRunButtonType() {

    String runButtonGroupOrFolder = this.retrieveAttributeValueFromConfig("runButtonGroupOrFolder", false);
    return runButtonGroupOrFolder;
      
  }
  
  public String getGroupId() {
    try {
      String groupUuidOrNameString = this.retrieveAttributeValueFromConfig("defaultRunButtonGroupUuidOrName", true);
      
      Group group = GroupFinder.findByUuid(groupUuidOrNameString, false);
      if (group == null) {
        group = GroupFinder.findByName(groupUuidOrNameString, true);
      }
      return group.getId();
    } catch (Exception e) {
      throw new RuntimeException("could not find configured default run button group for gsh template configId "+this.getConfigId());
    }
  }
  
  public String getFolderId() {
    try {
      String folderUuidOrNameString = this.retrieveAttributeValueFromConfig("defaultRunButtonFolderUuidOrName", true);
      
      Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folderUuidOrNameString, false);
      if (stem == null) {
        stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), folderUuidOrNameString, true);
      }
      return stem.getId();
    } catch (Exception e) {
      throw new RuntimeException("could not find configured defult run button stem for gsh template configId "+this.getConfigId());
    }
  }
  
  

}
