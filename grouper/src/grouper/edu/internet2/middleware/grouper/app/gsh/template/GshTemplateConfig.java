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
  
  private String gshTemplate;
  
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

  public void populateConfiguration() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    String configPrefix = "grouperGshTemplate."+configId+".";
    
    enabled = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"enabled", true);
    
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
      
      gshTemplateRequireGroupPrivilege =  GshTemplateRequireGroupPrivilege.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"requireGroupPrivilege"), true);
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
      
      gshTemplateRequireFolderPrivilege =  GshTemplateRequireFolderPrivilege.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"requireFolderPrivilege"), true);
    }
    
    gshTemplateSecurityRunType = GshTemplateSecurityRunType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"securityRunType"), true);
    
    if (gshTemplateSecurityRunType == GshTemplateSecurityRunType.specifiedGroup) {
      String groupUuidCanRun = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"groupUuidCanRun");
      groupThatCanRun = GroupFinder.findByUuid(grouperSession, groupUuidCanRun, false);
      GrouperUtil.assertion(groupThatCanRun != null, "could not find group for groupUuidCanRun: "+groupUuidCanRun);
    }
    
    gshTemplate = GrouperConfig.retrieveConfig().propertyValueStringRequired(configPrefix+"gshTemplate");

    gshLightweight = GrouperConfig.retrieveConfig().propertyValueBoolean(configPrefix+"gshLightweight", false);

    int numberOfInputs = GrouperConfig.retrieveConfig().propertyValueInt(configPrefix+"numberOfInputs", 0);
    
    for (int i=0; i<numberOfInputs; i++) {
      
      String inputPrefix = configPrefix + "input." + i + ".";
      
      String inputName = GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "name");
      
      GshTemplateInputConfig gshTemplateInputConfig = new GshTemplateInputConfig();
      gshTemplateInputConfig.setName(inputName);
      
      String valueType = GrouperConfig.retrieveConfig().propertyValueString(inputPrefix + "type", "string");
      GshTemplateInputType gshTemplateInputType = GshTemplateInputType.valueOfIgnoreCase(valueType, true);
      
      gshTemplateInputConfig.setGshTemplateInputType(gshTemplateInputType);
      
      GshTemplateInputValidationType gshTemplateInputValidationType = GshTemplateInputValidationType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueStringRequired(inputPrefix + "validationType"), true);
      gshTemplateInputConfig.setGshTemplateInputValidationType(gshTemplateInputValidationType);
      
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
