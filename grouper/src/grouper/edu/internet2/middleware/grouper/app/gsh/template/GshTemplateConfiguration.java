package edu.internet2.middleware.grouper.app.gsh.template;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
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

    // GRP-7033: template compile mode (interpreted vs compiled) + compile-on-save.
    GrouperConfigurationModuleAttribute templateModeAttribute = attributes.get("templateMode");
    String templateModeValue = templateModeAttribute == null ? null
        : templateModeAttribute.getValueOrExpressionEvaluationValue();
    boolean compiled = StringUtils.equalsIgnoreCase("compiled", templateModeValue);

    {
      GrouperConfigurationModuleAttribute templateTypeAttribute = attributes.get("templateType");
      String templateTypeValue = templateTypeAttribute == null ? null
          : templateTypeAttribute.getValueOrExpressionEvaluationValue();
      GshTemplateType gshTemplateType = StringUtils.isBlank(templateTypeValue)
          ? GshTemplateType.gsh : GshTemplateType.valueOfIgnoreCase(templateTypeValue, false);

      // the new template types have no legacy interpreted path; they require compiled mode
      boolean legacyType = gshTemplateType == GshTemplateType.gsh
          || gshTemplateType == GshTemplateType.abac
          || gshTemplateType == GshTemplateType.provisioner;

      if (!compiled && !legacyType) {
        String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorTypeRequiresCompiledMode");
        validationErrorsToDisplay.put(templateTypeAttribute.getHtmlForElementIdHandle(), error);
        return;
      }

      if (compiled) {
        // read the source from its configured location (inline textArea or container file)
        GrouperConfigurationModuleAttribute sourceTypeAttribute = attributes.get("gshTemplateSourceType");
        String sourceType = sourceTypeAttribute == null ? "textArea"
            : sourceTypeAttribute.getValueOrExpressionEvaluationValue();

        String javaSource = null;
        GrouperConfigurationModuleAttribute sourceAttributeForError;
        if (StringUtils.equals("file", sourceType)) {
          GrouperConfigurationModuleAttribute fileNameAttribute = attributes.get("gshTemplateFileName");
          sourceAttributeForError = fileNameAttribute;
          String fileName = fileNameAttribute == null ? null : fileNameAttribute.getValueOrExpressionEvaluationValue();
          if (!StringUtils.isBlank(fileName)) {
            File file = new File(fileName);
            if (!file.exists()) {
              String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorSourceFileNotFound");
              error = GrouperUtil.replace(error, "$$fileName$$", fileName);
              validationErrorsToDisplay.put(fileNameAttribute.getHtmlForElementIdHandle(), error);
              return;
            }
            javaSource = GrouperUtil.readFileIntoString(file);
          }
        } else {
          GrouperConfigurationModuleAttribute gshTemplateAttribute = attributes.get("gshTemplate");
          sourceAttributeForError = gshTemplateAttribute;
          javaSource = gshTemplateAttribute == null ? null : gshTemplateAttribute.getValueOrExpressionEvaluationValue();
        }

        // blank source is handled by the required-field validation; only compile-check non-blank source
        if (!StringUtils.isBlank(javaSource) && sourceAttributeForError != null) {
          String diagnostics = compileDiagnosticsOrNull(javaSource);
          if (diagnostics != null) {
            String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorCompile");
            validationErrorsToDisplay.put(sourceAttributeForError.getHtmlForElementIdHandle(),
                error + "<pre>" + GrouperUtil.xmlEscape(diagnostics) + "</pre>");
            return;
          }
        }
      }
    }

    GrouperConfigurationModuleAttribute showOnGroupsAttribute = attributes.get("showOnGroups");
    String showOnGroupsValue = showOnGroupsAttribute.getValueOrExpressionEvaluationValue();
    
    boolean showTemplateOnAllGroups = true;
    Set<Group> groupsWhereTemplateIsAvailable = null;
    Stem stemUnderWhichAnyGroupCanHaveTemplate = null;
    
    if (GrouperUtil.booleanValue(showOnGroupsValue, false)) {
      GrouperConfigurationModuleAttribute groupShowTypeAttribute = attributes.get("groupShowType");
      String groupShowTypeValue = groupShowTypeAttribute.getValueOrExpressionEvaluationValue();
      
      GshTemplateGroupShowType groupShowType = GshTemplateGroupShowType.valueOfIgnoreCase(groupShowTypeValue, true);
      
      if (groupShowType == GshTemplateGroupShowType.certainGroups) {
        showTemplateOnAllGroups = false;
        GrouperConfigurationModuleAttribute groupUuidsToShowAttribute = attributes.get("groupUuidsToShow");
        String groupUuidsToShow = groupUuidsToShowAttribute.getValueOrExpressionEvaluationValue();
        
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
        String folderUuidForGroupsInFolder = folderUuidForGroupsInFolderAttribute.getValueOrExpressionEvaluationValue();
        
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
    String showOnFoldersValue = showOnFoldersAttribute.getValueOrExpressionEvaluationValue();
    
    if (GrouperUtil.booleanValue(showOnFoldersValue, false)) {
      GrouperConfigurationModuleAttribute folderShowTypeAttribute = attributes.get("folderShowType");
      String folderShowTypeValue = folderShowTypeAttribute.getValueOrExpressionEvaluationValue();
      
      GshTemplateFolderShowType folderShowType = GshTemplateFolderShowType.valueOfIgnoreCase(folderShowTypeValue, true);
      
      if (folderShowType == GshTemplateFolderShowType.certainFolders) {
        showTemplateOnAllGroups = false;
        GrouperConfigurationModuleAttribute folderUuidsToShowAttribute = attributes.get("folderUuidToShow");
        String folderUuidsToShow = folderUuidsToShowAttribute.getValueOrExpressionEvaluationValue();
        
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
    String runButtonGroupOrFolderAttributeValue = runButtonGroupOrFolderAttribute.getValueOrExpressionEvaluationValue();
    
    if (StringUtils.equals("group", runButtonGroupOrFolderAttributeValue)) {
      
      GrouperConfigurationModuleAttribute defaultRunButtonGroupUuidOrNameAttribute = attributes.get("defaultRunButtonGroupUuidOrName");
      String groupUuidOrName = defaultRunButtonGroupUuidOrNameAttribute.getValueOrExpressionEvaluationValue();
      
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
            String groupShowOnDescendants = groupShowOnDescendantsAttribute.getValueOrExpressionEvaluationValue();
            
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
      String folderUuidOrName = defaultRunButtonFolderUuidOrNameAttribute.getValueOrExpressionEvaluationValue();
      
      Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folderUuidOrName, false);
      if (stem == null) {
        stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), folderUuidOrName, false);
      }
      
      if (stem == null) {
        String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorFolderNotFound");
        error = GrouperUtil.replace(error, "$$folderUUIDOrName$$", folderUuidOrName);
        validationErrorsToDisplay.put(defaultRunButtonFolderUuidOrNameAttribute.getHtmlForElementIdHandle(), error);
        return;
      }
      // we still need to check if the default run folder is one of the folders where the template can show
      if (!canDefaultRunFolderShowTemplate(stem)) {
        String error = GrouperTextContainer.textOrNull("gshTemplateConfigSaveErrorDefaultRunFolderNotInFoldersToShowList");
        error = GrouperUtil.replace(error, "$$folderUUIDOrName$$", folderUuidOrName);
        validationErrorsToDisplay.put(defaultRunButtonFolderUuidOrNameAttribute.getHtmlForElementIdHandle(), error);
        return;
      }
      
    }
    
    GrouperConfigurationModuleAttribute gshTemplateSourceTypeAttibute = attributes.get("gshTemplateSourceType");
    
    GrouperConfigurationModuleAttribute templateVersion = attributes.get("templateVersion");
    
    if (!compiled && StringUtils.equals(gshTemplateSourceTypeAttibute.getValueOrExpressionEvaluationValue(), "file") && (templateVersion == null || GrouperUtil.isBlank(templateVersion.getValueOrExpressionEvaluation()))) {
      String error = GrouperTextContainer.textOrNull("gshTemplate.error.configId.templateSourceTypeWithNonV2Version.message");
      validationErrorsToDisplay.put(gshTemplateSourceTypeAttibute.getHtmlForElementIdHandle(), error);
      return;
    }
    
    GrouperConfigurationModuleAttribute numberOfInputsAttribute = attributes.get("numberOfInputs");
    
    String valueOrExpressionEvaluation = numberOfInputsAttribute.getValueOrExpressionEvaluationValue();
    
    int numberOfInputs = GrouperUtil.intValue(valueOrExpressionEvaluation, 0);
    boolean hasSeenFileInput = false;
    
    Map<Integer, List<String>> indexToFormElementTypes = new LinkedHashMap<Integer, List<String>>();
    
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
      GrouperConfigurationModuleAttribute validationType = attributes.get("input."+i+".validationType");
      if (validationType != null && StringUtils.equals(validationType.getValueOrExpressionEvaluation(), "regex")) {
        GrouperConfigurationModuleAttribute validationRegexAttribute = attributes.get("input."+i+".validationRegex");
        boolean invalidRegex = false;
        
        String error = null;
        if (validationRegexAttribute == null) {
          // not sure how this could happen
          invalidRegex = true;
          error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorInputInvalidRegex");
        } else {
          String regex = validationRegexAttribute.getValueOrExpressionEvaluation();
          try {
            Pattern.compile(regex);
          } catch (Exception e) {
            invalidRegex = true;
            error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorInputInvalidRegex") + "<pre>" + e.getMessage() + "</pre>";
          }
        }
        
        if (invalidRegex) {
          validationErrorsToDisplay.put(validationRegexAttribute.getHtmlForElementIdHandle(), error);
          return;
          
        }
                
      }
      
      {
        
        GrouperConfigurationModuleAttribute inputType = attributes.get("input."+i+".type");
        if (inputType != null && StringUtils.equals(inputType.getValueOrExpressionEvaluation(), "file")) {
          GrouperConfigurationModuleAttribute templateType = attributes.get("templateType");
          if (templateType != null && !StringUtils.equals(templateType.getValueOrExpressionEvaluation(), "gsh")) {
            String error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorInputTypeFileNotAllowedWithNonGshTemplate");
            validationErrorsToDisplay.put(inputType.getHtmlForElementIdHandle(), error);
            return;
          }
          if (templateVersion == null || GrouperUtil.isBlank(templateVersion.getValueOrExpressionEvaluation())
              || StringUtils.equals(templateVersion.getValueOrExpressionEvaluation(), "V1")) {
            String error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorInputTypeFileNotAllowedWithV1GshTemplate");
            validationErrorsToDisplay.put(inputType.getHtmlForElementIdHandle(), error);
            return;
          }
        }
      }
      
      {
        //when input type is file, form element has to be file
        GrouperConfigurationModuleAttribute inputType = attributes.get("input."+i+".type");
        if (inputType != null && StringUtils.equals(inputType.getValueOrExpressionEvaluation(), "file")) {
          GrouperConfigurationModuleAttribute formElementType = attributes.get("input."+i+".formElementType");
          if (formElementType == null || !StringUtils.equals(formElementType.getValueOrExpressionEvaluation(), "file")) {
            String error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorInputTypeFileButFormElementTypeNotFile");
            validationErrorsToDisplay.put(inputType.getHtmlForElementIdHandle(), error);
            return;
          }
        }
      }
      
      {
        //when form type is file, input type has to be file or string. when it's string, we're going to read the file into a string
        GrouperConfigurationModuleAttribute formElementType = attributes.get("input."+i+".formElementType");
        if (formElementType != null && StringUtils.equals(formElementType.getValueOrExpressionEvaluation(), "file")) {
          hasSeenFileInput = true; // after this input, all the inputs must be of type file only
          GrouperConfigurationModuleAttribute inputType = attributes.get("input."+i+".type");
          if (inputType != null && StringUtils.isNotBlank(inputType.getValueOrExpressionEvaluation())) {
            if (!StringUtils.equals(inputType.getValueOrExpressionEvaluation(), "file") &&
              !StringUtils.equals(inputType.getValueOrExpressionEvaluation(), "string")) {
              String error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorFormElementTypeFileButInputTypeNotNotFileOrString");
              validationErrorsToDisplay.put(formElementType.getHtmlForElementIdHandle(), error);
              return;
            }
          }
        }
      }
      
      GrouperConfigurationModuleAttribute formElementType = attributes.get("input."+i+".formElementType");
      GrouperConfigurationModuleAttribute indexAttribute = attributes.get("input."+i+".index");
      
      String indexValue = GrouperUtil.defaultIfBlank(indexAttribute.getValueOrExpressionEvaluation(),
          indexAttribute.getDefaultValue());
      
      String formElementTypeString = formElementType.getValueOrExpressionEvaluation();
      if (StringUtils.isBlank(formElementTypeString)) {
        formElementTypeString = formElementType.getDefaultValue();
      }
      
      if (indexToFormElementTypes.containsKey(GrouperUtil.intValue(indexValue, 0))) {
        indexToFormElementTypes.get(GrouperUtil.intValue(indexValue, 0)).add(formElementTypeString);
      } else {
        ArrayList<String> formElementTypeStrings = new ArrayList<String>();
        formElementTypeStrings.add(formElementTypeString);
        indexToFormElementTypes.put(GrouperUtil.intValue(indexValue, 0), formElementTypeStrings);
      }
    }
    
    //make sure the form element is the last one, take field index into account
    // 0 -> text, file
    // 1 -> file
    // 2 -> text
    int highestFileIndex = Integer.MAX_VALUE;
    for (Integer index: indexToFormElementTypes.keySet()) {
      List<String> elementTypes = indexToFormElementTypes.get(index);
      boolean fileElementAlreadySeen = false;
      for (String elementType: elementTypes) {
        if (StringUtils.equals(elementType, "file")) {
          fileElementAlreadySeen = true;
          highestFileIndex = index;
        } else {
          if (fileElementAlreadySeen) {
            String error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorFormElementTypeFileMustBeTheLastFormElement");
            errorsToDisplay.add(error);
//            validationErrorsToDisplay.put(formElementType.getHtmlForElementIdHandle(), error);
          }
          if (index > highestFileIndex) {
            String error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorFormElementTypeFileMustBeTheLastFormElement");
            errorsToDisplay.add(error);
          }
          
        }
      }
    }
    
  }
  
  /**
   * GRP-7033: compile-on-save check for a compiled-Java template. Parses the
   * fully-qualified class name from the source, then compiles it against the
   * running JVM classpath via GshTemplateJavaCompiler (no registry swap — this
   * is validation only, before any config is persisted).
   * @param javaSource the Java source body
   * @return null if the source parses and compiles cleanly; otherwise the parse
   *   error or the compiler error diagnostics (one per line, with line/column
   *   and a caret) for inline display on the save screen
   */
  static String compileDiagnosticsOrNull(String javaSource) {
    if (StringUtils.isBlank(javaSource)) {
      return null;
    }

    GshTemplateSourceParser.GshTemplateSourceParseResult parseResult = GshTemplateSourceParser.parse(javaSource);
    if (!parseResult.isSuccess()) {
      return parseResult.getErrorMessage();
    }

    GshTemplateCompileResult compileResult = GshTemplateJavaCompiler.compile(
        parseResult.getFullyQualifiedClassName(), javaSource);
    if (compileResult.isSuccess()) {
      return null;
    }

    StringBuilder diagnostics = new StringBuilder();
    for (GshTemplateCompileDiagnostic diagnostic : compileResult.errorDiagnostics()) {
      if (diagnostics.length() > 0) {
        diagnostics.append("\n");
      }
      diagnostics.append(diagnostic.toString());
    }
    return diagnostics.toString();
  }

  private boolean canDefaultRunFolderShowTemplate(Stem defaultRunFolder) {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    GrouperConfigurationModuleAttribute folderShowTypeAttribute = attributes.get("folderShowType");
    String folderShowTypeValue = folderShowTypeAttribute.getValueOrExpressionEvaluationValue();
    
    GshTemplateFolderShowType folderShowType = GshTemplateFolderShowType.valueOfIgnoreCase(folderShowTypeValue, true);
    if (folderShowType == GshTemplateFolderShowType.allFolders) {
      return true;
    }
    
    GrouperConfigurationModuleAttribute folderUuidsToShowAttribute = attributes.get("folderUuidToShow");
    String folderUuidsToShow = folderUuidsToShowAttribute.getValueOrExpressionEvaluationValue();
    
    String[] folderUuidsOrNamesToShow = GrouperUtil.splitTrim(folderUuidsToShow, ",");
    
    Set<String> folderUuidOrNamesToShowIn = GrouperUtil.toSet(folderUuidsOrNamesToShow);
    
    Set<Stem> stems = new StemFinder().assignStemIds(folderUuidOrNamesToShowIn).findStems();
    stems.addAll(new StemFinder().assignStemNames(folderUuidOrNamesToShowIn).findStems());
    
    GrouperConfigurationModuleAttribute folderShowOnDescendantsAttribute = attributes.get("folderShowOnDescendants");
    String folderShowOnDescendants = folderShowOnDescendantsAttribute.getValueOrExpressionEvaluationValue();
    
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
    GshTemplateConfig.clearGrouperObjectTypeIdOrNameToGrouperObjectCache();

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
      GshTemplateConfig.clearGrouperObjectTypeIdOrNameToGrouperObjectCache();

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
    String folderUuidOrNameString = this.retrieveAttributeValueFromConfig("defaultRunButtonFolderUuidOrName", true);
    try {
      
      Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), folderUuidOrNameString, false);
      if (stem == null) {
        stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), folderUuidOrNameString, true);
      }
      return stem.getId();
    } catch (Exception e) {
      throw new RuntimeException("could not find configured defult run button stem for gsh template configId: " + folderUuidOrNameString + " , "+this.getConfigId());
    }
  }
  
  

}
