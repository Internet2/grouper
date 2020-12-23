package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

public class ProvisionerConfigurationContainer {
  
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
   * @return true if can view provisioner configurations
   */
  public boolean isCanViewProvisionerConfiguration() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }
  
  public List<ProvisionerConfiguration> getAllProvisionerConfigurationTypes() {
    return ProvisionerConfiguration.retrieveAllProvisionerConfigurationTypes();
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

  public String getSubSectionNameOrIndex() {
    
    // targetGroupAttribute.0.select
    String nameLabel = null;
    boolean isFieldElseAttribute = false;
    if (this.currentConfigSuffix != null) {
      int lastIndexOfDot = this.currentConfigSuffix.lastIndexOf('.');
      if (lastIndexOfDot > 0) {
        String indexKey = this.currentConfigSuffix.substring(0, lastIndexOfDot);
        String isFieldElseAttributeKey = indexKey + ".isFieldElseAttribute";
        isFieldElseAttribute = GrouperUtil.booleanValue(this.getGuiProvisionerConfiguration().getProvisionerConfiguration().retrieveAttributes().get(isFieldElseAttributeKey).getValue(), false);
        if (isFieldElseAttribute) {
          String fieldNameKey = indexKey + ".fieldName";
          nameLabel = this.getGuiProvisionerConfiguration().getProvisionerConfiguration().retrieveAttributes().get(fieldNameKey).getValue();
        } else {
          String nameKey = indexKey + ".name";
          nameLabel = this.getGuiProvisionerConfiguration().getProvisionerConfiguration().retrieveAttributes().get(nameKey).getValue();
        }
      }
    }
    
    if (StringUtils.isBlank(nameLabel)) {
      if (this.currentConfigSuffix != null && this.currentConfigSuffix.endsWith(".header")) {
        String prefix = GrouperUtil.prefixOrSuffix(this.currentConfigSuffix, ".header", true);
        String suffix = GrouperUtil.prefixOrSuffix(prefix, ".", false);
        // might be integer
        try {
          int suffixInt = GrouperUtil.intValue(suffix);
          this.setIndex(suffixInt);
        } catch (Exception e) {
          // ignore this
        }
      }
      
      return TextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.attributePrefix") + " " + (this.index+1);
    }

    return (isFieldElseAttribute ? 
        TextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.fieldPrefix") :
        TextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.attributePrefix")
          ) + " '" + nameLabel + "'";
  }
  
  private String currentConfigSuffix;
  
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
  
  
  
}
