package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GshTemplateConfig {
  
  private String configId;
  
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
  
  private GshTemplateRequireFolderPrivilege gshTemplateRequireFolderPrivilege;
  
  private GshTemplateRequireGroupPrivilege gshTemplateRequireGroupPrivilege;
  
  private String runAsSpecifiedSubjectSourceId;
  
  private String runAsSpecifiedSubjectId;
  
  private boolean runGshInTransaction = true;
  
  private String gshTemplate;
  
  private String actAsGroupUUID;
  
  private List<GshTemplateInputConfig> gshTemplateInputConfigs = new ArrayList<GshTemplateInputConfig>();
  

  public GshTemplateConfig(String configId) {
    this.configId = configId;
  }
  
  
  public String getConfigId() {
    return configId;
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


  public void populateConfiguration() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    String configPrefix = "grouperGshTemplate."+configId+".";
    
    enabled = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"enabled", true);
    useIndividualAudits = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"useIndividualAudits", true);

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
      
      gshTemplateInputConfig.setGshTemplateConfig(this);
      
      gshTemplateInputConfig.setName(inputName);
      
      String valueType = GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "type", "string");
      GshTemplateInputType gshTemplateInputType = GshTemplateInputType.valueOfIgnoreCase(valueType, true);
      
      gshTemplateInputConfig.setGshTemplateInputType(gshTemplateInputType);
      
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
      
      boolean required = GrouperConfig.retrieveConfig().propertyValueBoolean(inputPrefix+"required", false);
      gshTemplateInputConfig.setRequired(required);
      
      if (!required) {
        String defaultValue = GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "defaultValue", null);
        gshTemplateInputConfig.setDefaultValue(defaultValue);
      }
      
      gshTemplateInputConfig.setTrimWhitespace(GrouperConfig.retrieveConfig().propertyValueBoolean(inputPrefix+"trimWhitespace", true));
      
      
      GshTemplateFormElementType gshTemplateFormElementType = GshTemplateFormElementType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "formElementType", "textfield"), true);
      gshTemplateInputConfig.setGshTemplateFormElementType(gshTemplateFormElementType);
      
      if (gshTemplateInputConfig.getGshTemplateFormElementType() == GshTemplateFormElementType.dropdown) {
        
        GshTemplateDropdownValueFormatType gshTemplateDropdownValueFormatType = GshTemplateDropdownValueFormatType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "dropdownValueFormat", "csv"), true);
        gshTemplateInputConfig.setGshTemplateDropdownValueFormatType(gshTemplateDropdownValueFormatType);
        
        if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.csv) {
         String dropdownCsvValue = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "dropdownCsvValue");
         gshTemplateInputConfig.setDropdownCsvValue(dropdownCsvValue);
        } else if (gshTemplateInputConfig.getGshTemplateDropdownValueFormatType() == GshTemplateDropdownValueFormatType.json) {
          String dropdownJsonValue = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "dropdownJsonValue");
          gshTemplateInputConfig.setDropdownJsonValue(dropdownJsonValue);
        } else {
          String dropdownJavaClassValue = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "dropdownJavaClassValue");
          gshTemplateInputConfig.setDropdownJavaClassValue(dropdownJavaClassValue);
        }
      } else {
        int maxLength = GrouperConfig.retrieveConfig().propertyValueInt(inputPrefix + "maxLength", 500);
        maxLength = Math.min(maxLength, 10000);
        gshTemplateInputConfig.setMaxLength(maxLength);
      }
      
      
      gshTemplateInputConfigs.add(gshTemplateInputConfig);
      
    }
    
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
  
  

}
