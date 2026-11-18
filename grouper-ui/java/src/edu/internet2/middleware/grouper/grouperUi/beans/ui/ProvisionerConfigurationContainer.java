package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningError;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningErrorSummary;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

public class ProvisionerConfigurationContainer {

  // security fix (provisionerConfigViewsDisclosePrivateGroupsAndMemberships): rows are filtered in
  // the caller's session by ProvisionerConfigViewFilter before the JSPs see them
  
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
   * groups that are provisionable for a provisioner
   */
  private List<GuiGroup> guiGroupsProvisionable = new ArrayList<GuiGroup>();
  
  private int totalProvisionableGroups;

  /**
   * provisionable group rows dropped because the group was not found for the caller
   */
  private int hiddenProvisionableGroups;
  
  private List<GuiProvisioningAssignment> guiProvisioningAssignments = new ArrayList<>();
  private int totalProvisioningAssignments;

  /**
   * assignment rows dropped because the group or folder was not found for the caller
   */
  private int hiddenProvisioningAssignments;
  
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
   * total activity for one provisioner
   */
  private List<GuiProvisionerActivity> provisionerActivity;
  
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
    // drop group-owned log rows whose group the caller cannot VIEW
    this.guiProvisionerLogs = ProvisionerConfigViewFilter.filterLogs(guiProvisionerLogs);
  }
  
  
  public List<GuiGroup> getGuiGroupsProvisionable() {
    return guiGroupsProvisionable;
  }

  
  public void setGuiGroupsProvisionable(List<GuiGroup> guiGroupsProvisionable) {
    // the handler wraps groups the caller cannot VIEW as new GuiGroup(null): drop and count them
    this.guiGroupsProvisionable = ProvisionerConfigViewFilter.filterGuiGroups(guiGroupsProvisionable);
    this.hiddenProvisionableGroups = GrouperUtil.length(guiGroupsProvisionable) - GrouperUtil.length(this.guiGroupsProvisionable);
  }

  
  public List<GuiProvisioningAssignment> getGuiProvisioningAssignments() {
    return guiProvisioningAssignments;
  }

  
  public void setGuiProvisioningAssignments(
      List<GuiProvisioningAssignment> guiProvisioningAssignments) {
    // the handler wraps groups / folders the caller cannot see as GuiGroup(null) / GuiStem(null):
    // drop and count them
    if (guiProvisioningAssignments == null) {
      this.guiProvisioningAssignments = null;
      this.hiddenProvisioningAssignments = 0;
      return;
    }
    List<GuiProvisioningAssignment> visible = new ArrayList<GuiProvisioningAssignment>();
    for (GuiProvisioningAssignment guiProvisioningAssignment : guiProvisioningAssignments) {
      if (guiProvisioningAssignment != null
          && !ProvisionerConfigViewFilter.isEmptyGuiObject(guiProvisioningAssignment.getGuiGroupOrFolder())) {
        visible.add(guiProvisioningAssignment);
      }
    }
    this.guiProvisioningAssignments = visible;
    this.hiddenProvisioningAssignments = guiProvisioningAssignments.size() - visible.size();
  }

  
  public int getTotalProvisioningAssignments() {
    // do not count rows the caller cannot see
    return Math.max(0, totalProvisioningAssignments - hiddenProvisioningAssignments);
  }

  
  public void setTotalProvisioningAssignments(int totalProvisioningAssignments) {
    this.totalProvisioningAssignments = totalProvisioningAssignments;
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
    // keep sync groups the caller can VIEW (filtered copy, the caller's list is not changed)
    this.activityForGroup = ProvisionerConfigViewFilter.filterSyncGroups(activityForGroup);
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
    // keep sync memberships whose group the caller can READ (filtered copy)
    this.activityForMembership = ProvisionerConfigViewFilter.filterSyncMemberships(activityForMembership);
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
  
  private String provisionerConfigErrorGroup;

  private String provisionerConfigErrorEntity;

  private GrouperProvisioningErrorSummary grouperProvisioningErrorSummary;

  /**
   * summary as computed by GrouperProvisioningService, before filtering
   */
  private GrouperProvisioningErrorSummary grouperProvisioningErrorSummaryUnfiltered;
  
  private List<GrouperProvisioningError> grouperProvisioningErrors;
  
  
  public List<GrouperProvisioningError> getGrouperProvisioningErrors() {
    return grouperProvisioningErrors;
  }

  
  public void setGrouperProvisioningErrors(
      List<GrouperProvisioningError> grouperProvisioningErrors) {
    // group errors need VIEW, membership errors need READ on the group
    this.grouperProvisioningErrors = ProvisionerConfigViewFilter.filterErrors(grouperProvisioningErrors);
    // the summary counts must match what the caller may see, whichever setter runs first
    this.grouperProvisioningErrorSummary = ProvisionerConfigViewFilter.filterErrorSummary(
        this.grouperProvisioningErrorSummaryUnfiltered, this.grouperProvisioningErrors);
  }

  public GrouperProvisioningErrorSummary getGrouperProvisioningErrorSummary() {
    return grouperProvisioningErrorSummary;
  }

  
  public void setGrouperProvisioningErrorSummary(
      GrouperProvisioningErrorSummary grouperProvisioningErrorSummary) {
    this.grouperProvisioningErrorSummaryUnfiltered = grouperProvisioningErrorSummary;
    // for a filtered caller the group and membership counts come from the visible error rows
    // (none until setGrouperProvisioningErrors runs), so they are no oracle for hidden groups
    this.grouperProvisioningErrorSummary = ProvisionerConfigViewFilter.filterErrorSummary(
        grouperProvisioningErrorSummary, this.grouperProvisioningErrors);
  }

  public String getProvisionerConfigErrorDuration() {
    return provisionerConfigErrorDuration;
  }

  
  public void setProvisionerConfigErrorDuration(String provisionerConfigErrorDuration) {
    this.provisionerConfigErrorDuration = provisionerConfigErrorDuration;
  }
  
  public String getProvisionerConfigErrorGroup() {
    return provisionerConfigErrorGroup;
  }

  
  public void setProvisionerConfigErrorGroup(String provisionerConfigErrorGroup) {
    this.provisionerConfigErrorGroup = provisionerConfigErrorGroup;
  }
  
  public String getProvisionerConfigErrorEntity() {
    return provisionerConfigErrorEntity;
  }

  
  public void setProvisionerConfigErrorEntity(String provisionerConfigErrorEntity) {
    this.provisionerConfigErrorEntity = provisionerConfigErrorEntity;
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

  
  public int getTotalProvisionableGroups() {
    // do not count rows the caller cannot see
    return Math.max(0, totalProvisionableGroups - hiddenProvisionableGroups);
  }

  
  public void setTotalProvisionableGroups(int totalProvisionableGroups) {
    this.totalProvisionableGroups = totalProvisionableGroups;
  }

  
  public List<GuiProvisionerActivity> getProvisionerActivity() {
    return provisionerActivity;
  }

  
  public void setProvisionerActivity(List<GuiProvisionerActivity> provisionerActivity) {
    if (provisionerActivity == null || ProvisionerConfigViewFilter.isUnfiltered()) {
      this.provisionerActivity = provisionerActivity;
      return;
    }
    // the handler builds these rows from its own unfiltered lists, so rebuild them from the
    // filtered lists the setActivityFor* setters stored (same converter, same order)
    this.provisionerActivity = GuiProvisionerActivity.convertFromGcGrouperSyncObjects(
        this.activityForGroup, this.activityForMember, this.activityForMembership);
  }
  
  
  
}
