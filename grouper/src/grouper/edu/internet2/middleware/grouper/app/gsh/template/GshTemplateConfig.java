package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateConfig {
  
  private static final Log LOG = GrouperUtil.getLog(GshTemplateConfig.class);
  
  private String configId;
  
  private String templateName;
  
  private String templateNameExternalizedTextKey;
  
  private String templateDescription;
  
  private String templateDescriptionExternalizedTextKey;
  
  
  public String getTemplateNameExternalizedTextKey() {
    return templateNameExternalizedTextKey;
  }


  
  public String getTemplateDescriptionExternalizedTextKey() {
    return templateDescriptionExternalizedTextKey;
  }


  public String getTemplateDescription() {
    return templateDescription;
  }

  private GshTemplateRunAsType gshTemplateRunAsType;
  
  private boolean enabled;
  
  private boolean useIndividualAudits;
  
  private boolean showOnGroups;
  
  private GshTemplateGroupShowType gshTemplateGroupShowType;
  
  private Set<Group> groupsToShow = new HashSet<Group>();
  
  private GshTemplateGroupShowOnDescendants gshTemplateGroupShowOnDescendants;
  
  private boolean allowWsFromNoOwner;
  
  
  public boolean isAllowWsFromNoOwner() {
    return allowWsFromNoOwner;
  }

  private boolean showOnFolders;
  
  private GshTemplateFolderShowType gshTemplateFolderShowType;
  
  /** V1 or V2 */
  private String templateVersion;
  
  /**
   * V1 or V2
   * @return
   */
  public String getTemplateVersion() {
    return templateVersion;
  }

  private Set<Stem> foldersToShow = new HashSet<Stem>();
  
  private Stem folderForGroupsInFolder;
  
  private GshTemplateType gshTemplateType;
  
  
  public GshTemplateType getGshTemplateType() {
    return gshTemplateType;
  }

  private GshTemplateFolderShowOnDescendants gshTemplateFolderShowOnDescendants;
  
  private GshTemplateSecurityRunType gshTemplateSecurityRunType;
  
  private Group groupThatCanRun;
  
  private boolean useExternalizedText;
  
  private String moreActionsLabelExternalizedTextKey;
  
  private String moreActionsLabel;
  
  private GshTemplateRequireFolderPrivilege gshTemplateRequireFolderPrivilege;
  
  private GshTemplateRequireGroupPrivilege gshTemplateRequireGroupPrivilege;
  
  private String runAsSpecifiedSubjectSourceId;
  
  private String runAsSpecifiedSubjectId;
  
  private boolean runGshInTransaction = true;
  
  private String gshTemplate;
  
  private String actAsGroupUUID;
  
  private boolean showInMoreActions;
  
  private boolean displayErrorOutput;
  
  private List<GshTemplateInputConfig> gshTemplateInputConfigs = new ArrayList<GshTemplateInputConfig>();
  

  public GshTemplateConfig(String configId) {
    this.configId = configId;
  }
  
  
  public String getConfigId() {
    return configId;
  }
  
  

  
  



  public String getTemplateName() {
    return templateName;
  }

  public String getTemplateNameForUi() {
    if (!useExternalizedText) {
      return this.templateName;
    } else {
      return StringUtils.defaultString(GrouperTextContainer.textOrNull(templateNameExternalizedTextKey), templateNameExternalizedTextKey);
    }
  }

  public String getTemplateDescriptionForUi() {
    if (!useExternalizedText) {
      return this.templateDescription;
    } else {
      return StringUtils.defaultString(GrouperTextContainer.textOrNull(templateDescriptionExternalizedTextKey), templateDescriptionExternalizedTextKey);
    }
  }

  public String getMoreActionsLabelForUi() {
    if (!useExternalizedText) {
      return this.moreActionsLabel;
    } else {
      return StringUtils.defaultString(GrouperTextContainer.textOrNull(moreActionsLabelExternalizedTextKey), moreActionsLabelExternalizedTextKey);
    }
  }

  public boolean isUseExternalizedText() {
    return useExternalizedText;
  }


  public String getMoreActionsLabelExternalizedTextKey() {
    return moreActionsLabelExternalizedTextKey;
  }


  
  public String getMoreActionsLabel() {
    return moreActionsLabel;
  }

  public GshTemplateRunAsType getGshTemplateRunAsType() {
    return gshTemplateRunAsType;
  }


  
  public boolean isEnabled() {
    return enabled;
  }

  
  
  public boolean isUseIndividualAudits() {
    return useIndividualAudits;
  }


  public boolean isShowOnGroups() {
    return showOnGroups;
  }


  
  public GshTemplateGroupShowType getGshTemplateGroupShowType() {
    return gshTemplateGroupShowType;
  }


  
  
  public Set<Group> getGroupsToShow() {
    return groupsToShow;
  }


  
  public GshTemplateGroupShowOnDescendants getGshTemplateGroupShowOnDescendants() {
    return gshTemplateGroupShowOnDescendants;
  }


  
  public boolean isShowOnFolders() {
    return showOnFolders;
  }

  
  
  
  
  public Stem getFolderForGroupsInFolder() {
    return folderForGroupsInFolder;
  }



  
  public void setFolderForGroupsInFolder(Stem folderForGroupsInFolder) {
    this.folderForGroupsInFolder = folderForGroupsInFolder;
  }



  public boolean isShowInMoreActions() {
    return showInMoreActions;
  }


  public GshTemplateFolderShowType getGshTemplateFolderShowType() {
    return gshTemplateFolderShowType;
  }


  
  
  
  public Set<Stem> getFoldersToShow() {
    return foldersToShow;
  }



  public GshTemplateFolderShowOnDescendants getGshTemplateFolderShowOnDescendants() {
    return gshTemplateFolderShowOnDescendants;
  }


  
  public GshTemplateSecurityRunType getGshTemplateSecurityRunType() {
    return gshTemplateSecurityRunType;
  }


  
  public Group getGroupThatCanRun() {
    return groupThatCanRun;
  }


  
  public GshTemplateRequireFolderPrivilege getGshTemplateRequireFolderPrivilege() {
    return gshTemplateRequireFolderPrivilege;
  }


  
  public GshTemplateRequireGroupPrivilege getGshTemplateRequireGroupPrivilege() {
    return gshTemplateRequireGroupPrivilege;
  }


  
  public String getRunAsSpecifiedSubjectSourceId() {
    return runAsSpecifiedSubjectSourceId;
  }


  
  public String getRunAsSpecifiedSubjectId() {
    return runAsSpecifiedSubjectId;
  }

  private boolean simplifiedUi;
  
  public boolean isSimplifiedUi() {
    return simplifiedUi;
  }
  
  public void setSimplifiedUi(boolean simplifiedUi) {
    this.simplifiedUi = simplifiedUi;
  }



  public String getGshTemplate() {
    return gshTemplate;
  }

  public List<GshTemplateInputConfig> getGshTemplateInputConfigs() {
    return gshTemplateInputConfigs;
  }
  
  
  public boolean isRunGshInTransaction() {
    return runGshInTransaction;
  }
  

  
  public String getActAsGroupUUID() {
    return actAsGroupUUID;
  }

  public boolean isDisplayErrorOutput() {
    return displayErrorOutput;
  }

  public void populateConfiguration() {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        String configPrefix = "grouperGshTemplate."+configId+".";
        
        GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
        gshTemplateType = GshTemplateType.valueOfIgnoreCase(GrouperUtil.defaultIfBlank(grouperConfig.propertyValueString(configPrefix+"templateType"), "gsh"), true);
        
        enabled = grouperConfig.propertyValueBoolean(configPrefix+"enabled", true);

        templateVersion = grouperConfig.propertyValueString(configPrefix+"templateVersion", "V1");

        simplifiedUi = grouperConfig.propertyValueBoolean(configPrefix+"simplifiedUi", false);

        useIndividualAudits = grouperConfig.propertyValueBoolean(configPrefix+"useIndividualAudits", true);
        
        useExternalizedText = grouperConfig.propertyValueBoolean(configPrefix+"externalizedText", false);
        
        showInMoreActions = grouperConfig.propertyValueBoolean(configPrefix+"showInMoreActions", false);
        
        if (useExternalizedText) {
          
          if (showInMoreActions) {
            moreActionsLabelExternalizedTextKey = grouperConfig.propertyValueStringRequired(configPrefix+"moreActionsLabelExternalizedTextKey");
          }
          templateNameExternalizedTextKey = grouperConfig.propertyValueStringRequired(configPrefix+"templateNameExternalizedTextKey");
          templateDescriptionExternalizedTextKey = grouperConfig.propertyValueStringRequired(configPrefix+"templateDescriptionExternalizedTextKey");
          
        } else {
          if (showInMoreActions) {
            moreActionsLabel = grouperConfig.propertyValueStringRequired(configPrefix+"moreActionsLabel");
          }
          templateName = grouperConfig.propertyValueStringRequired(configPrefix+"templateName");
          templateDescription = grouperConfig.propertyValueStringRequired(configPrefix+"templateDescription");
        }

        displayErrorOutput = grouperConfig.propertyValueBoolean(configPrefix+"displayErrorOutput", false);
        
        actAsGroupUUID = grouperConfig.propertyValueString(configPrefix+"actAsGroupUUID", null);
        
        String runAsType = grouperConfig.propertyValueStringRequired(configPrefix+"runAsType");
        gshTemplateRunAsType = GshTemplateRunAsType.valueOfIgnoreCase(runAsType, true);
        
        if (gshTemplateRunAsType == GshTemplateRunAsType.specifiedSubject) {
          runAsSpecifiedSubjectSourceId = grouperConfig.propertyValueStringRequired(configPrefix+"runAsSpecifiedSubjectSourceId");
          runAsSpecifiedSubjectId = grouperConfig.propertyValueStringRequired(configPrefix+"runAsSpecifiedSubjectId");
        }
        
        showOnGroups = grouperConfig.propertyValueBoolean(configPrefix+"showOnGroups", false);
        
        if (showOnGroups) {
          gshTemplateGroupShowType = GshTemplateGroupShowType.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"groupShowType"), true);
          
          if (gshTemplateGroupShowType == GshTemplateGroupShowType.certainGroups) {
            String groupUuidsToShow = grouperConfig.propertyValueStringRequired(configPrefix+"groupUuidsToShow");
            
            String[] groupUuidsOrNames = GrouperUtil.splitTrim(groupUuidsToShow, ",");
            for (String groupUuidOrName: groupUuidsOrNames) {
              Group groupToShow = GroupFinder.findByUuid(grouperSession, groupUuidOrName, false);
              if (groupToShow == null) {
                groupToShow = GroupFinder.findByName(grouperSession, groupUuidOrName, false);
              }
              GrouperUtil.assertion(groupToShow != null, "could not find group for groupUuidOrName: "+groupUuidOrName);
              groupsToShow.add(groupToShow);
            }
            
          } else if (gshTemplateGroupShowType == GshTemplateGroupShowType.groupsInFolder) {
            
            String folderUuidForGroupsInFolder = grouperConfig.propertyValueStringRequired(configPrefix+"folderUuidForGroupsInFolder");
            folderForGroupsInFolder = StemFinder.findByUuid(grouperSession, folderUuidForGroupsInFolder, false);
            if (folderForGroupsInFolder == null) {
              folderForGroupsInFolder = StemFinder.findByName(grouperSession, folderUuidForGroupsInFolder, false);
            }
            GrouperUtil.assertion(folderForGroupsInFolder != null, "could not find folder for folderUuidForGroupsInFolder: "+folderUuidForGroupsInFolder);
            gshTemplateGroupShowOnDescendants = GshTemplateGroupShowOnDescendants.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"groupShowOnDescendants"), true);
          }
          
        }
        
        allowWsFromNoOwner = grouperConfig.propertyValueBoolean(configPrefix+"allowWsFromNoOwner", false);
        
        showOnFolders = grouperConfig.propertyValueBoolean(configPrefix+"showOnFolders", false);
        
        if (showOnFolders) {
          gshTemplateFolderShowType = GshTemplateFolderShowType.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"folderShowType"), true);
          
          if(gshTemplateFolderShowType == GshTemplateFolderShowType.certainFolders) {
            String folderUuidsOrNamesToShow = grouperConfig.propertyValueStringRequired(configPrefix+"folderUuidToShow");
            
            String[] folderUuidsOrNames = GrouperUtil.splitTrim(folderUuidsOrNamesToShow, ",");
            for (String folderUuidOrName: folderUuidsOrNames) {
              Stem folderToShow = StemFinder.findByUuid(grouperSession, folderUuidOrName, false);
              if (folderToShow == null) {
                folderToShow = StemFinder.findByName(grouperSession, folderUuidOrName, false);
              }
              GrouperUtil.assertion(folderToShow != null, "could not find folder for folderUuidToShow: "+folderUuidOrName);
              foldersToShow.add(folderToShow);
            }
            
            gshTemplateFolderShowOnDescendants = GshTemplateFolderShowOnDescendants.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"folderShowOnDescendants"), true);
          }
          
        }
        
        gshTemplateSecurityRunType = GshTemplateSecurityRunType.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"securityRunType"), true);
        
        if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.specifiedGroup) {
          String groupUuidOrNameCanRun = grouperConfig.propertyValueStringRequired(configPrefix+"groupUuidCanRun");
          groupThatCanRun = GroupFinder.findByUuid(grouperSession, groupUuidOrNameCanRun, false);
          if (groupThatCanRun == null) {
            groupThatCanRun = GroupFinder.findByName(grouperSession, groupUuidOrNameCanRun, false);
          }
          GrouperUtil.assertion(groupThatCanRun != null, "could not find group for groupUuidOrNameCanRun: "+groupUuidOrNameCanRun);
        }
        
        if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.privilegeOnObject && showOnGroups) {
          gshTemplateRequireGroupPrivilege =  GshTemplateRequireGroupPrivilege.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"requireGroupPrivilege"), true);
        }
        
        if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.privilegeOnObject && showOnFolders) {
          gshTemplateRequireFolderPrivilege =  GshTemplateRequireFolderPrivilege.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(configPrefix+"requireFolderPrivilege"), true);
        }
        
        gshTemplate = grouperConfig.propertyValueStringRequired(configPrefix+"gshTemplate");

        gshLightweight = grouperConfig.propertyValueBoolean(configPrefix+"gshLightweight", false);

        runGshInTransaction = grouperConfig.propertyValueBoolean(configPrefix+"runGshInTransaction", true);

        int numberOfInputs = grouperConfig.propertyValueInt(configPrefix+"numberOfInputs", 0);
        
        for (int i=0; i<numberOfInputs; i++) {
          
          String inputPrefix = configPrefix + "input." + i + ".";
          
          String inputName = grouperConfig.propertyValueStringRequired(inputPrefix + "name");
          
          GshTemplateInputConfig gshTemplateInputConfig = new GshTemplateInputConfig();
          
          gshTemplateInputConfig.setGshTemplateConfig(GshTemplateConfig.this);
          
          gshTemplateInputConfig.setName(inputName);
          
          String valueType = grouperConfig.propertyValueString(inputPrefix + "type", "string");
          GshTemplateInputType gshTemplateInputType = GshTemplateInputType.valueOfIgnoreCase(valueType, true);
          
          gshTemplateInputConfig.setUseExternalizedText(GshTemplateConfig.this.useExternalizedText);
          
          if (useExternalizedText) {
            gshTemplateInputConfig.setLabelExternalizedTextKey(grouperConfig.propertyValueStringRequired(inputPrefix + "labelExternalizedTextKey"));
            gshTemplateInputConfig.setDescriptionExternalizedTextKey(grouperConfig.propertyValueStringRequired(inputPrefix + "descriptionExternalizedTextKey"));
          } else {
            gshTemplateInputConfig.setLabel(grouperConfig.propertyValueStringRequired(inputPrefix + "label"));
            gshTemplateInputConfig.setDescription(grouperConfig.propertyValueStringRequired(inputPrefix + "description"));
          }
          
          gshTemplateInputConfig.setGshTemplateInputType(gshTemplateInputType);
          
          if (gshTemplateInputType == GshTemplateInputType.BOOLEAN) {
            gshTemplateInputConfig.setConfigItemFormElement(ConfigItemFormElement.RADIOBUTTON);
          } else {
            ConfigItemFormElement configItemFormElement = ConfigItemFormElement.valueOfIgnoreCase(grouperConfig.propertyValueString(inputPrefix + "formElementType", "text"), true);
            gshTemplateInputConfig.setConfigItemFormElement(configItemFormElement);
          }
          
          if ((gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXT || gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXTAREA) && gshTemplateInputType != GshTemplateInputType.BOOLEAN) {
            GshTemplateInputValidationType gshTemplateInputValidationType = GshTemplateInputValidationType.valueOfIgnoreCase(grouperConfig.propertyValueStringRequired(inputPrefix + "validationType"), true);
            gshTemplateInputConfig.setGshTemplateInputValidationType(gshTemplateInputValidationType);
            
            String validationMessage = grouperConfig.propertyValueString(inputPrefix + "validationMessage");
            gshTemplateInputConfig.setValidationMessage(validationMessage);
            
            String validationMessageExternalizedTextKey = grouperConfig.propertyValueString(inputPrefix + "validationMessageExternalizedTextKey");
            gshTemplateInputConfig.setValidationMessageExternalizedTextKey(validationMessageExternalizedTextKey);
            
            if (gshTemplateInputValidationType == GshTemplateInputValidationType.regex) {
              String validationRegex = grouperConfig.propertyValueStringRequired(inputPrefix + "validationRegex");
              gshTemplateInputConfig.setValidationRegex(validationRegex);
            } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.jexl) {
              String validationJexl = grouperConfig.propertyValueStringRequired(inputPrefix + "validationJexl");
              gshTemplateInputConfig.setValidationJexl(validationJexl);
            } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.builtin) {
              String validationBuiltinTypeString = grouperConfig.propertyValueStringRequired(inputPrefix + "validationBuiltin");
              ValidationBuiltinType validationBuiltinType = ValidationBuiltinType.valueOfIgnoreCase(validationBuiltinTypeString, true);
              gshTemplateInputConfig.setValidationBuiltinType(validationBuiltinType);
            }
          }
          
          boolean required = grouperConfig.propertyValueBoolean(inputPrefix+"required", false);
          gshTemplateInputConfig.setRequired(required);
          
          if (!required) {
            String defaultValue = grouperConfig.propertyValueString(inputPrefix + "defaultValue", null);
            gshTemplateInputConfig.setDefaultValue(defaultValue);
          }
          
          gshTemplateInputConfig.setTrimWhitespace(grouperConfig.propertyValueBoolean(inputPrefix+"trimWhitespace", true));
          gshTemplateInputConfig.setShowEl(grouperConfig.propertyValueString(inputPrefix+"showEl", null));
          gshTemplateInputConfig.setIndex(grouperConfig.propertyValueInt(inputPrefix+"index", 0));
          
          if (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.DROPDOWN) {
            
            GshTemplateDropdownValueFormatType gshTemplateDropdownValueFormatType = GshTemplateDropdownValueFormatType.valueOfIgnoreCase(grouperConfig.propertyValueString(inputPrefix + "dropdownValueFormat", "csv"), true);
            gshTemplateInputConfig.setGshTemplateDropdownValueFormatType(gshTemplateDropdownValueFormatType);
            
            if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.csv) {
             String dropdownCsvValue = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownCsvValue");
             gshTemplateInputConfig.setDropdownCsvValue(dropdownCsvValue);
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.json) {
              String dropdownJsonValue = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownJsonValue");
              gshTemplateInputConfig.setDropdownJsonValue(dropdownJsonValue);
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.dynamicFromTemplate) {
              // let this happen
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.javaclass) {
              String dropdownJavaClassValue = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownJavaClassValue");
              gshTemplateInputConfig.setDropdownJavaClassValue(dropdownJavaClassValue);
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.sql) {
              String dropdownSqlDatabase = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownSqlDatabase");
              gshTemplateInputConfig.setDropdownSqlDatabase(dropdownSqlDatabase);
              String dropdownSqlValue = grouperConfig.propertyValueStringRequired(inputPrefix + "dropdownSqlValue");
              gshTemplateInputConfig.setDropdownSqlValue(dropdownSqlValue);
              int dropdownSqlCacheForMinutes = grouperConfig.propertyValueInt(inputPrefix + "dropdownSqlCacheForMinutes", 2);
              gshTemplateInputConfig.setDropdownSqlCacheForMinutes(dropdownSqlCacheForMinutes);
            } else {
              throw new RuntimeException("Not expecting drop down value format type: " + gshTemplateInputConfig.getGshTemplateDropdownValueFormatType());
            }
          } else {
            int maxLength = grouperConfig.propertyValueInt(inputPrefix + "maxLength", 500);
            gshTemplateInputConfig.setMaxLength(maxLength);
          }
          
          
          gshTemplateInputConfigs.add(gshTemplateInputConfig);
          
        }
        
        return null;
      }
    });
    
  }

  /**
   * this will not have imports built in, so have imports in script or fully qualify classes.  Saves 3 seconds of execution
   */
  private boolean gshLightweight = false;

  /**
   * this will not have imports built in, so have imports in script or fully qualify classes.  Saves 3 seconds of execution
   * @return
   */
  public boolean isGshLightweight() {
    return gshLightweight;
  }
  
  /**
   * check if the given folder can run this gsh template
   * @param folder
   * @return
   */
  public boolean canFolderRunTemplate(Stem folder) {
    
    if (!isShowOnFolders()) {
      return false;
    }
    
    if (this.getGshTemplateFolderShowType() == GshTemplateFolderShowType.allFolders) {
      return true;
    }
    
    Set<Stem> foldersToShow = getFoldersToShow();
    if (GrouperUtil.nonNull(foldersToShow).size() == 0) {
      LOG.error("foldersToShow is not configured correctly for template with config id: "+getConfigId());
      return false;
    }
    
    Set<String> foldersToShowUuids = new HashSet<String>();

    for (Stem folderToShow: foldersToShow) {
      foldersToShowUuids.add(folderToShow.getUuid());
    }
    
    GshTemplateFolderShowOnDescendants gshTemplateFolderShowOnDescendants = getGshTemplateFolderShowOnDescendants();
    if (GshTemplateFolderShowOnDescendants.certainFolders == gshTemplateFolderShowOnDescendants) {
      
      if (foldersToShowUuids.contains(folder.getUuid())) {
        return true;
      }
      
      return false;
    } else if (GshTemplateFolderShowOnDescendants.oneChildLevel == gshTemplateFolderShowOnDescendants) {
      
      for (Stem folderToShow: foldersToShow) {
        if (StringUtils.equals(GrouperUtil.parentStemNameFromName(folder.getName(), false), folderToShow.getName())) {
          return true;
        }
      }
      
      return false;
    } else if (GshTemplateFolderShowOnDescendants.certainFoldersAndOneChildLevel == gshTemplateFolderShowOnDescendants) {
      
      if (foldersToShowUuids.contains(folder.getUuid())) {            
        return true;
      } 
      
      for (Stem folderToShow: foldersToShow) {
        if (StringUtils.equals(GrouperUtil.parentStemNameFromName(folder.getName(), false), folderToShow.getName())) {
          return true;
        }
      }
      
      return false;
    } else if (GshTemplateFolderShowOnDescendants.descendants == gshTemplateFolderShowOnDescendants) {
      
      for (Stem folderToShow: foldersToShow) {
        if (folder.getName().startsWith(folderToShow.getName()+":")) {
          return true;
        }
      }
      
      return false;
    } else if (GshTemplateFolderShowOnDescendants.certainFoldersAndDescendants == gshTemplateFolderShowOnDescendants) {
      
      for (Stem folderToShow: foldersToShow) {
        if (folder.getName().startsWith(folderToShow.getName()+":")) {
          return true;
        }
      }
      
      if (foldersToShowUuids.contains(folder.getUuid())) {
        return true;
      }
      
      return false;
      
    } else {
      throw new RuntimeException("Invalid gshTemplateFolderShowOnDescendants: "+gshTemplateFolderShowOnDescendants);
    }
    
  }
  
  /**
   * check if the given group can run this gsh template
   * @param folder
   * @return
   */
  public boolean canGroupRunTemplate(Group group) {
    
    if (!isShowOnGroups()) {
      return false;
    }
    
    if (this.getGshTemplateGroupShowType() == GshTemplateGroupShowType.allGroups) {
      return true;
    } else if (this.getGshTemplateGroupShowType() == GshTemplateGroupShowType.certainGroups) {
      Set<Group> groupsToShow = getGroupsToShow();
      if (groupsToShow.contains(group)) {
        return true;
      }
    } else if (this.getGshTemplateGroupShowType() == GshTemplateGroupShowType.groupsInFolder) {
      
      Stem folderForGroupsInFolder = getFolderForGroupsInFolder();
      
      GshTemplateGroupShowOnDescendants templateGroupShowOnDescendants = getGshTemplateGroupShowOnDescendants();
      
      if (GshTemplateGroupShowOnDescendants.descendants == templateGroupShowOnDescendants) {
        return folderForGroupsInFolder.isChildGroup(group);
      } else if (GshTemplateGroupShowOnDescendants.oneChildLevel == templateGroupShowOnDescendants) {
        return folderForGroupsInFolder.getChildGroups(Scope.ONE).contains(group);
      }
      
    }
    
    return false;
    
  }

  /**
   * some controls might depend on the logged in subject
   */
  private Subject currentUser = null;
  
  /**
   * some controls might depend on the logged in subject
   * @param loggedInSubject
   */
  public void setCurrentUser(Subject loggedInSubject) {
    this.currentUser = loggedInSubject;
  }

  /**
   * some controls might depend on the logged in subject
   * @return current user
   */
  public Subject getCurrentUser() {
    return currentUser;
  }



  public GshTemplateInputConfig retrieveGshTemplateInputConfig(String gshInputName) {
    for (GshTemplateInputConfig gshTemplateInputConfig : GrouperUtil.nonNull(this.getGshTemplateInputConfigs())) {
      if (StringUtils.equals(gshInputName, gshTemplateInputConfig.getName())) {
        return gshTemplateInputConfig;
      }
    }
    throw new RuntimeException("Cannot find config for input: '" + gshInputName + "'");
  }

}
