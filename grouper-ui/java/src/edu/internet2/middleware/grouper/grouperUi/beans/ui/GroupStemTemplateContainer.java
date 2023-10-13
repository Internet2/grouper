/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfiguration;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOwnerType;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateValidationService;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Stem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.util.PerformanceLogger;
import edu.internet2.middleware.subject.Subject;

/**
 * @author vsachdeva
 *
 */
public class GroupStemTemplateContainer {
  
  /**
   * if we are showing on group
   */
  private boolean showOnGroup;

  /**
   * if we are showing on folder
   */
  private boolean showOnFolder;
  
  /**
   * if this is a simplified ui request from browser
   */
  private boolean simplifiedRequest;
  
  
  
  /**
   * if we are showing on group
   * @return
   */
  public boolean isShowOnGroup() {
    return showOnGroup;
  }

  /**
   * if we are showing on group
   * @param showOnGroup
   */
  public void setShowOnGroup(boolean showOnGroup) {
    this.showOnGroup = showOnGroup;
  }

  /**
   * if we are showing on folder
   * @return
   */
  public boolean isShowOnFolder() {
    return showOnFolder;
  }

  /**
   * if we are showing on folder
   * @param showOnFolder
   */
  public void setShowOnFolder(boolean showOnFolder) {
    this.showOnFolder = showOnFolder;
  }

  /**
   * if this is a simplified ui request from browser
   * @return
   */
  public boolean isSimplifiedRequest() {
    return simplifiedRequest;
  }

  /**
   * if this is a simplified ui request from browser
   * @param simplifiedRequest
   */
  public void setSimplifiedRequest(boolean simplifiedRequest) {
    this.simplifiedRequest = simplifiedRequest;
  }

  /**
   * template type eg: service
   */
  private String templateType;
  
  /**
   * user specified template key eg: wiki
   */
  private String templateKey;
  
  /**
   * friendly name of the template. optional
   */
  private String templateFriendlyName;
  
  /**
   * template description. optional
   */
  private String templateDescription;
  
  /**
   * list of service actions for selected template type
   */
  private List<ServiceAction> serviceActions = new ArrayList<ServiceAction>();
  
  /**
   * implementation class for selected template type
   */
  private GrouperTemplateLogicBase templateLogic;
  
  /**
   * current service action
   */
  private ServiceAction currentServiceAction;
  
  /**
   * all the template types to labels
   */
  private Map<String, String> templateOptions = new HashMap<String, String>();
  
  /**
   * create template in current folder or a subfolder
   */
  private boolean createNoSubfolder;
  

  /**
   * template type eg: service
   * @return
   */
  public String getTemplateType() {
    return templateType;
  }

  /**
   * @param templateType: template type eg: service
   */
  public void setTemplateType(String templateType) {
    this.templateType = templateType;
  }

  /**
   * @return user specified template key eg: wiki
   */
  public String getTemplateKey() {
    return templateKey;
  }

  /**
   * @param templateKey: user specified template key eg: wiki
   */
  public void setTemplateKey(String templateKey) {
    this.templateKey = templateKey;
  }

  /**
   * @return friendly name of the template. optional
   */
  public String getTemplateFriendlyName() {
    return templateFriendlyName;
  }

  /**
   * 
   * @return the template key or friendly name
   */
  public String getFriendlyNameOrTemplateKey() {
    return StringUtils.defaultIfBlank(this.templateFriendlyName, this.templateKey);
  }
  
  /**
   * @param templateFriendlyName: friendly name of the template. optional
   */
  public void setTemplateFriendlyName(String templateFriendlyName) {
    this.templateFriendlyName = templateFriendlyName;
  }

  /**
   * @return template description. optional
   */
  public String getTemplateDescription() {
    return templateDescription;
  }

  /**
   * @param templateDescription: template description. optional
   */
  public void setTemplateDescription(String templateDescription) {
    this.templateDescription = templateDescription;
  }


  /**
   * @return implementation class for selected template type
   */
  public GrouperTemplateLogicBase getTemplateLogic() {
    return templateLogic;
  }

  /**
   * @param templateLogic: implementation class for selected template type
   */
  public void setTemplateLogic(GrouperTemplateLogicBase templateLogic) {
    this.templateLogic = templateLogic;
  }

  /**
   * @return all the template types to labels
   */
  public Map<String, String> getTemplateOptions() {
    return templateOptions;
  }

  /**
   * @param templateOptions: all the template types to labels
   */
  public void setTemplateOptions(Map<String, String> templateOptions) {
    this.templateOptions = templateOptions;
  }

  /**
   * @return list of service actions for selected template type
   */
  public List<ServiceAction> getServiceActions() {
    return serviceActions;
  }

  /**
   * @param serviceActions: list of service actions for selected template type
   */
  public void setServiceActions(List<ServiceAction> serviceActions) {
    this.serviceActions = serviceActions;
  }

  /**
   * @return current service action
   */
  public ServiceAction getCurrentServiceAction() {
    return currentServiceAction;
  }

  /**
   * @param currentServiceAction Current service action
   */
  public void setCurrentServiceAction(ServiceAction currentServiceAction) {
    this.currentServiceAction = currentServiceAction;
  }

  /**
   * if the show in this folder checkbox should show
   */
  private boolean showInThisFolderCheckbox = true;

  /**
   * custom template gsh template config user is executing
   */
  private GuiGshTemplateConfig guiGshTemplateConfig;

  /**
   * if the show in this folder checkbox should show
   * @return the showInThisFolderCheckbox
   */
  public boolean isShowInThisFolderCheckbox() {
    return this.showInThisFolderCheckbox;
  }
  
  /**
   * if the show in this folder checkbox should show
   * @param showInThisFolderCheckbox1 the showInThisFolderCheckbox to set
   */
  public void setShowInThisFolderCheckbox(boolean showInThisFolderCheckbox1) {
    this.showInThisFolderCheckbox = showInThisFolderCheckbox1;
  }

  /**
   * @return
   */
  public boolean isCreateNoSubfolder() {
    return createNoSubfolder;
  }

  /**
   * @param createNoSubfolder
   */
  public void setCreateNoSubfolder(boolean createNoSubfolder) {
    this.createNoSubfolder = createNoSubfolder;
  }
  
  /**
   * get custom gsh templates to show in template types dropdown
   * @return
   */
  public Map<String, String> getCustomGshTemplates() {
    
    this.templatesToShowHelper();
    return this.customGshTemplates;
  }

  /**
   * cache this
   */
  private Map<String, String> customGshTemplates = null;

  
  /**
   * cache this
   */
  private Map<String, String> customAbacTemplates = null;
  
  /**
   * get custom abac templates to show in pattern dropdown
   * @return
   */
  public Map<String, String> getCustomAbacTemplates() {
    this.abacTemplatesToShowHelper();
    return this.customAbacTemplates;
  }

  /**
   * cache this
   */
  private Map<String, String> templatesToShowInMoreActions = null;
  
  /**
   * get templates to show in more actions. map of configId to template name
   * @return
   */
  public Map<String, String> getTemplatesToShowInMoreActions() {
    long startNanos = System.nanoTime();
    try {
      this.templatesToShowHelper();
    } finally {
      PerformanceLogger.performanceTimingDuration(UiV2Stem.PERFORMANCE_LOG_LABEL_STEM_UI_VIEW, "getTemplatesToShowInMoreActions", System.nanoTime()-startNanos);
    }
    return this.templatesToShowInMoreActions;

  }

  private static final Log LOG = GrouperUtil.getLog(GroupStemTemplateContainer.class);
  
  private void abacTemplatesToShowHelper() {
    
    Map<String, String> configsToShowInAbacPatternDropdown = new HashMap<String, String>();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
    
    Group group = null;
    if (groupContainer != null && groupContainer.getGuiGroup() != null && groupContainer.getGuiGroup().getGroup() != null) {
      group  = groupContainer.getGuiGroup().getGroup();
    }
    
    if (group == null) {
      throw new RuntimeException("group is null");
    }
    
    List<GshTemplateConfiguration> gshTemplateConfigs = GshTemplateConfiguration.retrieveAllGshTemplateConfigs();
    
    for (GshTemplateConfiguration gshTemplateConfiguration: gshTemplateConfigs) {
      
      String templateTypeString = gshTemplateConfiguration.retrieveAttributeValueFromConfig("templateType", true);
      if (!StringUtils.equals(templateTypeString, "abac")) {
        continue;
      }
      
      if (gshTemplateConfiguration.isEnabled()) {
        
        try {
          GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(gshTemplateConfiguration.getConfigId());
          gshTemplateConfig.populateConfiguration();
          
          if (StringUtils.isBlank(gshTemplateConfiguration.getConfigId())) {
            continue;
          }
          
          GshTemplateExec gshTemplateExec = new GshTemplateExec()
              .assignConfigId(gshTemplateConfiguration.getConfigId())
              .assignCurrentUser(loggedInSubject)
              .assignGshTemplateOwnerType(GshTemplateOwnerType.group)
              .assignOwnerGroupName(group.getName());
          
          if (gshTemplateExec == null || !new GshTemplateValidationService().canSubjectExecuteTemplate(gshTemplateConfig, gshTemplateExec)) {
            continue;
          }

          if (!StringUtils.isBlank(gshTemplateConfig.getTemplateNameForUi())) {
            configsToShowInAbacPatternDropdown.put(gshTemplateConfiguration.getConfigId(), gshTemplateConfig.getTemplateNameForUi());
          }
          
        } catch (Exception e) {
          LOG.error("Cant decide if GSH template should display! " + gshTemplateConfiguration.getConfigId(), e);
        }
      }
    }
    
    {
      List<Map.Entry<String, String>> listToShowInAbacPatternDropdown = new LinkedList<Map.Entry<String, String>>(configsToShowInAbacPatternDropdown.entrySet()); 
  
      // Sort the list 
      Collections.sort(listToShowInAbacPatternDropdown, new Comparator<Map.Entry<String, String> >() { 
       public int compare(Map.Entry<String, String> o1,  
                          Map.Entry<String, String> o2) { 
         if (o1 == o2) {
           return 0;
         }
         if (o1 == null) {
           return -1;
         }
         if (o2 == null) {
           return 1;
         }
         return GrouperUtil.compare(o1.getValue(), o2.getValue()); 
       } 
      }); 
      
      
      Map<String, String> sortedAbacTemplatesByNameToShowInPatternDropdown = new LinkedHashMap<String, String>();
      
      for (Map.Entry<String, String> templateIdAndName : listToShowInAbacPatternDropdown) { 
        sortedAbacTemplatesByNameToShowInPatternDropdown.put(templateIdAndName.getKey(), templateIdAndName.getValue()); 
      } 
      this.customAbacTemplates = sortedAbacTemplatesByNameToShowInPatternDropdown;
    }
  }

  /**
   * get templates to show in more actions. map of configId to template name
   * @return
   */
  private void templatesToShowHelper() {
    
    if (this.templatesToShowInMoreActions != null) {
      return;
    }
    
    Map<String, String> configsToShowInStemMoreActions = new HashMap<String, String>();
    Map<String, String> configsToShowInTemplateTypeDropdown = new HashMap<String, String>();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    StemContainer stemContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer();
    GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
    
    Stem stem = null;
    Group group = null;
    if (stemContainer != null && stemContainer.getGuiStem() != null && stemContainer.getGuiStem().getStem() != null) {
      stem = stemContainer.getGuiStem().getStem();
    } else if (groupContainer != null && groupContainer.getGuiGroup() != null && groupContainer.getGuiGroup().getGroup() != null) {
      group  = groupContainer.getGuiGroup().getGroup();
    }
    
    List<GshTemplateConfiguration> gshTemplateConfigs = GshTemplateConfiguration.retrieveAllGshTemplateConfigs();
    
    for (GshTemplateConfiguration gshTemplateConfiguration: gshTemplateConfigs) {
      if (gshTemplateConfiguration.isEnabled()) {
        
        try {
          GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(gshTemplateConfiguration.getConfigId());
          gshTemplateConfig.populateConfiguration();
          
          if (StringUtils.isBlank(gshTemplateConfiguration.getConfigId())) {
            continue;
          }
          
          GshTemplateExec gshTemplateExec = null;
          
          if (stem != null) {
            if (!gshTemplateConfig.canFolderRunTemplate(stem)) {
              continue;
            }
            gshTemplateExec = new GshTemplateExec()
                .assignConfigId(gshTemplateConfiguration.getConfigId())
                .assignCurrentUser(loggedInSubject)
                .assignGshTemplateOwnerType(GshTemplateOwnerType.stem)
                .assignOwnerStemName(stem.getName());
          } else if (group != null) {
            
            if (!gshTemplateConfig.canGroupRunTemplate(group)) {
              continue;
            }
            
            gshTemplateExec = new GshTemplateExec()
                .assignConfigId(gshTemplateConfiguration.getConfigId())
                .assignCurrentUser(loggedInSubject)
                .assignGshTemplateOwnerType(GshTemplateOwnerType.group)
                .assignOwnerGroupName(group.getName());
          }
          
          if (gshTemplateExec == null || !new GshTemplateValidationService().canSubjectExecuteTemplate(gshTemplateConfig, gshTemplateExec)) {
            continue;
          }

          if (!StringUtils.isBlank(gshTemplateConfig.getTemplateNameForUi())) {
            configsToShowInTemplateTypeDropdown.put(gshTemplateConfiguration.getConfigId(), gshTemplateConfig.getTemplateNameForUi());
          }
          
          if (gshTemplateConfig.isShowInMoreActions() && !StringUtils.isBlank(gshTemplateConfig.getMoreActionsLabelForUi())) {
            configsToShowInStemMoreActions.put(gshTemplateConfiguration.getConfigId(), gshTemplateConfig.getMoreActionsLabelForUi());
          }
        } catch (Exception e) {
          LOG.error("Cant decide if GSH template should display! " + gshTemplateConfiguration.getConfigId(), e);
        }
      }
    }
    
    {
      List<Map.Entry<String, String>> listToShowInStemMoreActions = new LinkedList<Map.Entry<String, String>>(configsToShowInStemMoreActions.entrySet()); 
  
      // Sort the list 
      Collections.sort(listToShowInStemMoreActions, new Comparator<Map.Entry<String, String> >() { 
       public int compare(Map.Entry<String, String> o1,  
                          Map.Entry<String, String> o2) { 
         if (o1 == o2) {
           return 0;
         }
         if (o1 == null) {
           return -1;
         }
         if (o2 == null) {
           return 1;
         }
         return GrouperUtil.compare(o1.getValue(), o2.getValue()); 
       } 
      }); 
      
      
      Map<String, String> sortedTemplatesByNameToShowInStemMoreActions = new LinkedHashMap<String, String>();
      
      for (Map.Entry<String, String> templateIdAndName : listToShowInStemMoreActions) { 
        sortedTemplatesByNameToShowInStemMoreActions.put(templateIdAndName.getKey(), templateIdAndName.getValue()); 
      } 
      this.templatesToShowInMoreActions = sortedTemplatesByNameToShowInStemMoreActions;
    }
    {
      List<Map.Entry<String, String>> listToShowInTemplateTypeDropdown = new LinkedList<Map.Entry<String, String>>(configsToShowInTemplateTypeDropdown.entrySet()); 
  
      // Sort the list 
      Collections.sort(listToShowInTemplateTypeDropdown, new Comparator<Map.Entry<String, String> >() { 
       public int compare(Map.Entry<String, String> o1,  
                          Map.Entry<String, String> o2) { 
         if (o1 == o2) {
           return 0;
         }
         if (o1 == null) {
           return -1;
         }
         if (o2 == null) {
           return 1;
         }
         return GrouperUtil.compare(o1.getValue(), o2.getValue()); 
       } 
      }); 
      
      
      Map<String, String> sortedTemplatesByNameToShowInTemplateTypeDropdown = new LinkedHashMap<String, String>();
      
      for (Map.Entry<String, String> templateIdAndName : listToShowInTemplateTypeDropdown) { 
        sortedTemplatesByNameToShowInTemplateTypeDropdown.put(templateIdAndName.getKey(), templateIdAndName.getValue()); 
      } 
      this.customGshTemplates = sortedTemplatesByNameToShowInTemplateTypeDropdown;
    }
  }

  
  public GuiGshTemplateConfig getGuiGshTemplateConfig() {
    return guiGshTemplateConfig;
  }

  
  public void setGuiGshTemplateConfig(GuiGshTemplateConfig guiGshTemplateConfig) {
    this.guiGshTemplateConfig = guiGshTemplateConfig;
  }

  
}
