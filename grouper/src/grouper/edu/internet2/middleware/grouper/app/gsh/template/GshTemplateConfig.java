package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
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
  
  private Group groupToShow;
  
  private GshTemplateGroupShowOnDescendants gshTemplateGroupShowOnDescendants;
  
  private boolean showOnFolders;
  
  private GshTemplateFolderShowType gshTemplateFolderShowType;
  
  private Stem folderToShow;
  
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


  
  public Group getGroupToShow() {
    return groupToShow;
  }


  
  public GshTemplateGroupShowOnDescendants getGshTemplateGroupShowOnDescendants() {
    return gshTemplateGroupShowOnDescendants;
  }


  
  public boolean isShowOnFolders() {
    return showOnFolders;
  }

  
  
  public boolean isShowInMoreActions() {
    return showInMoreActions;
  }


  public GshTemplateFolderShowType getGshTemplateFolderShowType() {
    return gshTemplateFolderShowType;
  }


  
  public Stem getFolderToShow() {
    return folderToShow;
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
        
        enabled = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"enabled", true);
        useIndividualAudits = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"useIndividualAudits", true);
        
        useExternalizedText = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"externalizedText", false);
        
        showInMoreActions = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"showInMoreActions", false);
        if (showInMoreActions) {
          if (useExternalizedText) {
            moreActionsLabelExternalizedTextKey = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"moreActionsLabelExternalizedTextKey");
            templateNameExternalizedTextKey = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"templateNameExternalizedTextKey");
            templateDescriptionExternalizedTextKey = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"templateDescriptionExternalizedTextKey");
          } else {
            moreActionsLabel = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"moreActionsLabel");
            templateName = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"templateName");
            templateDescription = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"templateDescription");
          }
        }

        displayErrorOutput = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"displayErrorOutput", false);
        
        actAsGroupUUID = GrouperConfig.retrieveConfig().propertyValueString(configPrefix+"actAsGroupUUID", null);
        
        String runAsType = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"runAsType");
        gshTemplateRunAsType = GshTemplateRunAsType.valueOfIgnoreCase(runAsType, true);
        
        if (gshTemplateRunAsType == GshTemplateRunAsType.specifiedSubject) {
          runAsSpecifiedSubjectSourceId = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"runAsSpecifiedSubjectSourceId");
          runAsSpecifiedSubjectId = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"runAsSpecifiedSubjectId");
        }
        
        showOnGroups = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"showOnGroups", false);
        
        if (showOnGroups) {
          gshTemplateGroupShowType = GshTemplateGroupShowType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"groupShowType"), true);
          
          if (gshTemplateGroupShowType == GshTemplateGroupShowType.certainGroup) {
            String groupUuidToShow = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"groupUuidToShow");
            groupToShow = GroupFinder.findByUuid(grouperSession, groupUuidToShow, false);
            GrouperUtil.assertion(groupToShow != null, "could not find group for groupUuidToShow: "+groupUuidToShow);
          } else if (gshTemplateGroupShowType == GshTemplateGroupShowType.groupsInFolder) {
            gshTemplateGroupShowOnDescendants = GshTemplateGroupShowOnDescendants.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"groupShowOnDescendants"), true);
          }
          
        }
        
        showOnFolders = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"showOnFolders", false);
        
        if (showOnFolders) {
          gshTemplateFolderShowType = GshTemplateFolderShowType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"folderShowType"), true);
          
          if(gshTemplateFolderShowType == GshTemplateFolderShowType.certainFolder) {
            String folderUuidToShow = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"folderUuidToShow");
            folderToShow = StemFinder.findByUuid(grouperSession, folderUuidToShow, false);
            GrouperUtil.assertion(folderToShow != null, "could not find group for folderUuidToShow: "+folderUuidToShow);
            
            gshTemplateFolderShowOnDescendants = GshTemplateFolderShowOnDescendants.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"folderShowOnDescendants"), true);
          }
          
        }
        
        gshTemplateSecurityRunType = GshTemplateSecurityRunType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"securityRunType"), true);
        
        if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.specifiedGroup) {
          String groupUuidCanRun = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"groupUuidCanRun");
          groupThatCanRun = GroupFinder.findByUuid(grouperSession, groupUuidCanRun, false);
          GrouperUtil.assertion(groupThatCanRun != null, "could not find group for groupUuidCanRun: "+groupUuidCanRun);
        }
        
        if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.privilegeOnObject && showOnGroups) {
          gshTemplateRequireGroupPrivilege =  GshTemplateRequireGroupPrivilege.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"requireGroupPrivilege"), true);
        }
        
        if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.privilegeOnObject && showOnFolders) {
          gshTemplateRequireFolderPrivilege =  GshTemplateRequireFolderPrivilege.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"requireFolderPrivilege"), true);
        }
        
        gshTemplate = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"gshTemplate");

        gshLightweight = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"gshLightweight", false);

        runGshInTransaction = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"runGshInTransaction", true);

        int numberOfInputs = GrouperConfig.retrieveConfig().propertyValueInt(configPrefix+"numberOfInputs", 0);
        
        for (int i=0; i<numberOfInputs; i++) {
          
          String inputPrefix = configPrefix + "input." + i + ".";
          
          String inputName = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "name");
          
          GshTemplateInputConfig gshTemplateInputConfig = new GshTemplateInputConfig();
          
          gshTemplateInputConfig.setGshTemplateConfig(GshTemplateConfig.this);
          
          gshTemplateInputConfig.setName(inputName);
          
          String valueType = GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "type", "string");
          GshTemplateInputType gshTemplateInputType = GshTemplateInputType.valueOfIgnoreCase(valueType, true);
          
          gshTemplateInputConfig.setUseExternalizedText(GshTemplateConfig.this.useExternalizedText);
          
          if (useExternalizedText) {
            gshTemplateInputConfig.setLabelExternalizedTextKey(GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "labelExternalizedTextKey"));
            gshTemplateInputConfig.setDescriptionExternalizedTextKey(GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "descriptionExternalizedTextKey"));
          } else {
            gshTemplateInputConfig.setLabel(GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "label"));
            gshTemplateInputConfig.setDescription(GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "description"));
          }
          
          gshTemplateInputConfig.setGshTemplateInputType(gshTemplateInputType);
          
          if (gshTemplateInputType == GshTemplateInputType.BOOLEAN) {
            gshTemplateInputConfig.setConfigItemFormElement(ConfigItemFormElement.RADIOBUTTON);
          } else {
            ConfigItemFormElement configItemFormElement = ConfigItemFormElement.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "formElementType", "text"), true);
            gshTemplateInputConfig.setConfigItemFormElement(configItemFormElement);
          }
          
          if (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.TEXT && gshTemplateInputType != GshTemplateInputType.BOOLEAN) {
            GshTemplateInputValidationType gshTemplateInputValidationType = GshTemplateInputValidationType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "validationType"), true);
            gshTemplateInputConfig.setGshTemplateInputValidationType(gshTemplateInputValidationType);
            
            String validationMessage = GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "validationMessage");
            gshTemplateInputConfig.setValidationMessage(validationMessage);
            
            String validationMessageExternalizedTextKey = GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "validationMessageExternalizedTextKey");
            gshTemplateInputConfig.setValidationMessageExternalizedTextKey(validationMessageExternalizedTextKey);
            
            if (gshTemplateInputValidationType == GshTemplateInputValidationType.regex) {
              String validationRegex = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "validationRegex");
              gshTemplateInputConfig.setValidationRegex(validationRegex);
            } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.jexl) {
              String validationJexl = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "validationJexl");
              gshTemplateInputConfig.setValidationJexl(validationJexl);
            } else if (gshTemplateInputValidationType == GshTemplateInputValidationType.builtin) {
              String validationBuiltinTypeString = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "validationBuiltin");
              ValidationBuiltinType validationBuiltinType = ValidationBuiltinType.valueOfIgnoreCase(validationBuiltinTypeString, true);
              gshTemplateInputConfig.setValidationBuiltinType(validationBuiltinType);
            }
          }
          
          boolean required = GrouperConfig.retrieveConfig().propertyValueBoolean(inputPrefix+"required", false);
          gshTemplateInputConfig.setRequired(required);
          
          if (!required) {
            String defaultValue = GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "defaultValue", null);
            gshTemplateInputConfig.setDefaultValue(defaultValue);
          }
          
          gshTemplateInputConfig.setTrimWhitespace(GrouperConfig.retrieveConfig().propertyValueBoolean(inputPrefix+"trimWhitespace", true));
          gshTemplateInputConfig.setShowEl(GrouperConfig.retrieveConfig().propertyValueString(inputPrefix+"showEl", null));
          gshTemplateInputConfig.setIndex(GrouperConfig.retrieveConfig().propertyValueInt(inputPrefix+"index", 0));
          
          if (gshTemplateInputConfig.getConfigItemFormElement() == ConfigItemFormElement.DROPDOWN) {
            
            GshTemplateDropdownValueFormatType gshTemplateDropdownValueFormatType = GshTemplateDropdownValueFormatType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "dropdownValueFormat", "csv"), true);
            gshTemplateInputConfig.setGshTemplateDropdownValueFormatType(gshTemplateDropdownValueFormatType);
            
            if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.csv) {
             String dropdownCsvValue = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "dropdownCsvValue");
             gshTemplateInputConfig.setDropdownCsvValue(dropdownCsvValue);
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.json) {
              String dropdownJsonValue = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "dropdownJsonValue");
              gshTemplateInputConfig.setDropdownJsonValue(dropdownJsonValue);
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.javaclass) {
              String dropdownJavaClassValue = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "dropdownJavaClassValue");
              gshTemplateInputConfig.setDropdownJavaClassValue(dropdownJavaClassValue);
            } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.sql) {
              String dropdownSqlDatabase = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "dropdownSqlDatabase");
              gshTemplateInputConfig.setDropdownSqlDatabase(dropdownSqlDatabase);
              String dropdownSqlValue = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "dropdownSqlValue");
              gshTemplateInputConfig.setDropdownSqlValue(dropdownSqlValue);
              int dropdownSqlCacheForMinutes = GrouperConfig.retrieveConfig().propertyValueInt(inputPrefix + "dropdownSqlCacheForMinutes", 2);
              gshTemplateInputConfig.setDropdownSqlCacheForMinutes(dropdownSqlCacheForMinutes);
            } else {
              throw new RuntimeException("Not expecting drop down value format type: " + gshTemplateInputConfig.getGshTemplateDropdownValueFormatType());
            }
          } else {
            int maxLength = GrouperConfig.retrieveConfig().propertyValueInt(inputPrefix + "maxLength", 500);
            maxLength = Math.min(maxLength, 10000);
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
    
    Stem folderToShow = getFolderToShow();
    if (folderToShow == null) {
      LOG.error("folderToShow is not configured correctly for template with config id: "+getConfigId());
      return false;
    }
    
    GshTemplateFolderShowOnDescendants gshTemplateFolderShowOnDescendants = getGshTemplateFolderShowOnDescendants();
    if (GshTemplateFolderShowOnDescendants.certainFolder == gshTemplateFolderShowOnDescendants && 
        !StringUtils.equals(folderToShow.getUuid(), folder.getUuid())) {
      return false;
    }
    
    if (GshTemplateFolderShowOnDescendants.oneChildLevel == gshTemplateFolderShowOnDescendants && 
        !folder.getName().equals(GrouperUtil.parentStemNameFromName(folderToShow.getName()))) {
      
      return false;
    }
    
    if (GshTemplateFolderShowOnDescendants.certainFolderAndOneChildLevel == gshTemplateFolderShowOnDescendants) {
      
      if (!folder.getName().equals(GrouperUtil.parentStemNameFromName(folderToShow.getName())) &&
          !StringUtils.equals(folderToShow.getUuid(), folder.getUuid())) {            
        return false;
      }
      
    }
    
    if (GshTemplateFolderShowOnDescendants.descendants == gshTemplateFolderShowOnDescendants &&
        !folder.getName().startsWith(folderToShow.getName()+":")) {
      
      return false;
    }
    
    if (GshTemplateFolderShowOnDescendants.certainFolderAndDescendants == gshTemplateFolderShowOnDescendants) {
      
      if (!folder.getName().startsWith(folderToShow.getName()+":") &&
          !StringUtils.equals(folderToShow.getUuid(), folder.getUuid())) {            
        return false;
      }
      
    }
    
    return true;
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

}
