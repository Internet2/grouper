package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningError;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningErrorSummary;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

public class ProvisionerConfigurationContainer {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ProvisionerConfigurationContainer.class);

  /**
   * provisioner configuration user is currently viewing/editing/adding
   */
  private GuiProvisionerConfiguration guiProvisionerConfiguration;
  
  /**
   * all configured provisioning configurations
   */
  private List<GuiProvisionerConfiguration> guiProvisionerConfigurations = new ArrayList<GuiProvisionerConfiguration>();
  
  /**
   * logs for a provisioner
   */
  private List<GuiProvisionerLog> guiProvisionerLogs = new ArrayList<GuiProvisionerLog>();
  
  /**
   * jobs for a provisioner
   */
  private List<GcGrouperSyncJob> provisionerJobs = new ArrayList<GcGrouperSyncJob>();
  
  /**
   * job details for one job 
   */
  private GcGrouperSyncJob grouperSyncJob;
  
  /**
   * provisioner config object type (group, entity or membership)
   */
  private String provisionerConfigObjectType; 
  
  /**
   * activity for group for one provisioner
   */
  private List<GcGrouperSyncGroup> activityForGroup;

  /**
   * activity for member for one provisioner
   */
  private List<GcGrouperSyncMember> activityForMember;
  
  /**
   * activity for membership for one provisioner
   */
  private List<GcGrouperSyncMembership> activityForMembership;
  
  /**
   * current grouped config index we are looping through
   */
  private int index;
  
  /**
   * 
   * @return all configured provisioning configurations
   */
  public List<GuiProvisionerConfiguration> getGuiProvisionerConfigurations() {
    return guiProvisionerConfigurations;
  }

  /**
   * all configured provisioning configurations
   * @param guiProvisionerConfigurations
   */
  public void setGuiProvisionerConfigurations(List<GuiProvisionerConfiguration> guiProvisionerConfigurations) {
    this.guiProvisionerConfigurations = guiProvisionerConfigurations;
  }
  

  /**
   * @return provisioner configuration user is currently viewing/editing/adding
   */
  public GuiProvisionerConfiguration getGuiProvisionerConfiguration() {
    return guiProvisionerConfiguration;
  }

  /**
   * provisioner configuration user is currently viewing/editing/adding
   * @param guiProvisionerConfiguration
   */
  public void setGuiProvisionerConfiguration(GuiProvisionerConfiguration guiProvisionerConfiguration) {
    this.guiProvisionerConfiguration = guiProvisionerConfiguration;
  }
  
  /**
   * @return logs for a provisioner
   */
  public List<GuiProvisionerLog> getGuiProvisionerLogs() {
    return guiProvisionerLogs;
  }

  /**
   * logs for a provisioner
   * @param guiProvisionerLogs
   */
  public void setGuiProvisionerLogs(List<GuiProvisionerLog> guiProvisionerLogs) {
    this.guiProvisionerLogs = guiProvisionerLogs;
  }
  
  /**
   * @return jobs for a provisioner
   */
  public List<GcGrouperSyncJob> getProvisionerJobs() {
    return provisionerJobs;
  }

  /**
   * jobs for a provisioner
   * @param provisionerJobs
   */
  public void setProvisionerJobs(List<GcGrouperSyncJob> provisionerJobs) {
    this.provisionerJobs = provisionerJobs;
  }
  
  /**
   * job details for one job 
   * @return
   */
  public GcGrouperSyncJob getGrouperSyncJob() {
    return grouperSyncJob;
  }

  /**
   * job details for one job 
   * @param grouperSyncJob
   */
  public void setGrouperSyncJob(GcGrouperSyncJob grouperSyncJob) {
    this.grouperSyncJob = grouperSyncJob;
  }

  
  /**
   * @param provisionerConfigId
   * @return true if the logged in subject can view the given provisioner config
   */
  public boolean isCanViewProvisionerConfiguration(String provisionerConfigId) {
    
    if (isCanEditProvisionerConfiguration()) {
      return true;
    }
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    List<ProvisioningConfiguration> allViewableProvisioningConfigurations = ProvisioningConfiguration.retrieveAllViewableProvisioningConfigurations(loggedInSubject);
    
    for (ProvisioningConfiguration provisioningConfiguration: allViewableProvisioningConfigurations) {
      if (StringUtils.equals(provisionerConfigId, provisioningConfiguration.getConfigId())) {
        return true;
      }
    }
    
    return false;
    
  }
  
  /**
   * @return true if the logged in subject can view at least one provisioner configuration
   */
  public boolean isCanViewProvisionerConfiguration() {
    
    if (isCanEditProvisionerConfiguration()) {
      return true;
    }
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    if (ProvisioningConfiguration.retrieveAllViewableProvisioningConfigurations(loggedInSubject).size() > 0) {
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @return true if the logged in user can edit/add/delete provisioner configs
   */
  public boolean isCanEditProvisionerConfiguration() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    return false;
  }
  
  public List<ProvisioningConfiguration> getAllProvisionerConfigurationTypes() {
    return ProvisioningConfiguration.retrieveAllProvisioningConfigurationTypes();
  }
  
  /**
   * keep track of the paging on the config history screen
   */
  private GuiPaging guiPaging = null;

  
  /**
   * keep track of the paging on the config history screen
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  
  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }

  
  public String getProvisionerConfigObjectType() {
    return provisionerConfigObjectType;
  }

  
  public void setProvisionerConfigObjectType(String provisionerConfigObjectType) {
    this.provisionerConfigObjectType = provisionerConfigObjectType;
  }

  
  public List<GcGrouperSyncGroup> getActivityForGroup() {
    return activityForGroup;
  }

  
  public void setActivityForGroup(List<GcGrouperSyncGroup> activityForGroup) {
    this.activityForGroup = activityForGroup;
  }

  
  public List<GcGrouperSyncMember> getActivityForMember() {
    return activityForMember;
  }

  
  public void setActivityForMember(List<GcGrouperSyncMember> activityForMember) {
    this.activityForMember = activityForMember;
  }

  
  public List<GcGrouperSyncMembership> getActivityForMembership() {
    return activityForMembership;
  }

  
  public void setActivityForMembership(List<GcGrouperSyncMembership> activityForMembership) {
    this.activityForMembership = activityForMembership;
  }

  private String cacheGroupAttributePrefix;


  
  
  public String getCacheGroupAttributePrefix() {
    
    if (this.cacheGroupAttributePrefix == null) {
      this.cacheGroupAttributePrefix = TextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.groupAttributePrefix");
    }
    
    return cacheGroupAttributePrefix;
  }

  
  public String getCacheEntityAttributePrefix() {
    if (this.cacheEntityAttributePrefix == null) {
      this.cacheEntityAttributePrefix = TextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetEntityAttribute.i.entityAttributePrefix");
    }
    return cacheEntityAttributePrefix;
  }

  
  public String getCacheAttributePrefix() {
    if (this.cacheAttributePrefix == null) {
      this.cacheAttributePrefix = TextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.attributePrefix");
    }
    return cacheAttributePrefix;
  }

  
  public String getCacheFieldPrefix() {
    if (this.cacheFieldPrefix == null) {
      this.cacheFieldPrefix = TextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.fieldPrefix");
    }
    return cacheFieldPrefix;
  }

  private String cacheEntityAttributePrefix;
  private String cacheAttributePrefix;
  private String cacheFieldPrefix;
  
  private String currentConfigSuffix;

  private List<ProvisionerStartWithBase> startWithConfigClasses;

  private ProvisionerStartWithBase provisionerStartWith;
  
  private boolean blankStartWithSelected;
  
  private boolean showStartWithSection;

  private String startWithSessionId;
  
  private String selectedErrorCode;
  
  private String provisionerConfigErrorDuration;
  
  private GrouperProvisioningErrorSummary grouperProvisioningErrorSummary;
  
  private List<GrouperProvisioningError> grouperProvisioningErrors;
  
  
  public List<GrouperProvisioningError> getGrouperProvisioningErrors() {
    return grouperProvisioningErrors;
  }

  
  public void setGrouperProvisioningErrors(
      List<GrouperProvisioningError> grouperProvisioningErrors) {
    this.grouperProvisioningErrors = grouperProvisioningErrors;
  }

  public GrouperProvisioningErrorSummary getGrouperProvisioningErrorSummary() {
    return grouperProvisioningErrorSummary;
  }

  
  public void setGrouperProvisioningErrorSummary(
      GrouperProvisioningErrorSummary grouperProvisioningErrorSummary) {
    this.grouperProvisioningErrorSummary = grouperProvisioningErrorSummary;
  }

  public String getProvisionerConfigErrorDuration() {
    return provisionerConfigErrorDuration;
  }

  
  public void setProvisionerConfigErrorDuration(String provisionerConfigErrorDuration) {
    this.provisionerConfigErrorDuration = provisionerConfigErrorDuration;
  }

  public String getSelectedErrorCode() {
    return selectedErrorCode;
  }

  
  public void setSelectedErrorCode(String selectedErrorCode) {
    this.selectedErrorCode = selectedErrorCode;
  }

  public List<String> getAllGcGrouperSyncErrorCodes() {
    GcGrouperSyncErrorCode[] values = GcGrouperSyncErrorCode.values();
    List<String> errorCodes = new ArrayList<>();
    for (GcGrouperSyncErrorCode errorCode: values) {
      errorCodes.add(errorCode.name());
    }
    return errorCodes;
  }

  public String getCurrentConfigSuffix() {
    return currentConfigSuffix;
  }

  
  public void setCurrentConfigSuffix(String currentConfigSuffix) {
    this.currentConfigSuffix = currentConfigSuffix;
  }

  public int getIndex() {
    return index;
  }

  
  public void setIndex(int index) {
    this.index = index;
  }

  public void setStartWithConfigClasses(List<ProvisionerStartWithBase> startWithConfigClasses) {
    this.startWithConfigClasses = startWithConfigClasses;
  }

  
  public List<ProvisionerStartWithBase> getStartWithConfigClasses() {
    return startWithConfigClasses;
  }

  
  public void setProvisionerStartWith(ProvisionerStartWithBase provisionerStartWith) {
    this.provisionerStartWith = provisionerStartWith;
  }

  
  public ProvisionerStartWithBase getProvisionerStartWith() {
    return provisionerStartWith;
  }

  
  public boolean isShowStartWithSection() {
    return showStartWithSection;
  }

  
  public void setShowStartWithSection(boolean showStartWithSection) {
    this.showStartWithSection = showStartWithSection;
  }

  public void setStartWithSessionId(String startWithSessionId) {
    this.startWithSessionId = startWithSessionId;
  }

  
  public String getStartWithSessionId() {
    return startWithSessionId;
  }

  
  public boolean isBlankStartWithSelected() {
    return blankStartWithSelected;
  }

  
  public void setBlankStartWithSelected(boolean blankStartWithSelected) {
    this.blankStartWithSelected = blankStartWithSelected;
  }
  
  
  
  
  
  
  
  
  
}
