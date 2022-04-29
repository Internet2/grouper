package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.azure.AzureProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.app.duo.DuoProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.duo.role.DuoRoleProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.google.GoogleProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.messagingProvisioning.MessagingProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Configuration;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisionerConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public abstract class ProvisioningConfiguration extends GrouperConfigurationModuleBase {
  
  public static ProvisioningConfiguration retrieveConfigurationByConfigSuffix(String propertyValueThatIdentifiesThisDaemon) {
    for (ProvisioningConfiguration provisionerConfiguration : GrouperUtil.nonNull(retrieveAllProvisioningConfigurationTypes())) {
      if (StringUtils.equals(propertyValueThatIdentifiesThisDaemon, 
          provisionerConfiguration.getPropertyValueThatIdentifiesThisConfig())) {
        return provisionerConfiguration;
      }
    }
    return null;
  }
  
  /**
   * Classes that are configured to give a starting point for setting up provisioners. These are spefici to provisioner type
   * e.g. Sql provisioner have SqlProvisioningGroupTableStartWith, SqlProvisioningEntityTableStartWith, SqlProvisioningGroupAndMembershipTableStartWith
   * @return list of start with classes
   */
  public List<ProvisionerStartWithBase> getStartWithConfigClasses() {
    return new ArrayList<>();
  }
  
  public final static Set<String> provisionerConfigClassNames = new LinkedHashSet<String>();
  
  static {
    provisionerConfigClassNames.add(AzureProvisionerConfiguration.class.getName());
    provisionerConfigClassNames.add(DuoProvisionerConfiguration.class.getName());
    provisionerConfigClassNames.add(DuoRoleProvisionerConfiguration.class.getName());
    provisionerConfigClassNames.add(GoogleProvisionerConfiguration.class.getName());
    provisionerConfigClassNames.add(LdapProvisionerConfiguration.class.getName());
    provisionerConfigClassNames.add(MessagingProvisionerConfiguration.class.getName());
    provisionerConfigClassNames.add(GrouperScim2Configuration.class.getName());
    provisionerConfigClassNames.add(SqlProvisionerConfiguration.class.getName());
//    provisionerConfigClassNames.add("edu.internet2.middleware.grouperBox.BoxProvisionerConfiguration");
  }
  
  /**
   * list of systems that can be configured
   * @return
   */
  public static List<ProvisioningConfiguration> retrieveAllProvisioningConfigurationTypes() {
    return (List<ProvisioningConfiguration>) (Object) retrieveAllConfigurationTypesHelper(provisionerConfigClassNames);
  }
  
  /**
   * list of configured provisioner systems
   * @return
   */
  public static List<ProvisioningConfiguration> retrieveAllProvisioningConfigurations() {
   return (List<ProvisioningConfiguration>) (Object) retrieveAllConfigurations(provisionerConfigClassNames);
  }
  
  @Override
  protected String getConfigurationTypePrefix() {
    return "provisionerConfiguration";
  }
  
  @Override
  protected String getGenericConfigId() {
    return "genericProvisioner";
  }
  
  @Override
  public void deleteConfig(boolean fromUi) {
    super.deleteConfig(fromUi);
    GcGrouperSync grouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, this.getConfigId());
    
    if (grouperSync == null) return;
    
    {
      List<GcGrouperSyncGroup> grouperSyncGroups = grouperSync.getGcGrouperSyncGroupDao().groupRetrieveAll();
      grouperSync.getGcGrouperSyncGroupDao().groupDelete(grouperSyncGroups, true, true);
    }
    
    {
      List<GcGrouperSyncMember> grouperSyncMembers = grouperSync.getGcGrouperSyncMemberDao().memberRetrieveAll();
      grouperSync.getGcGrouperSyncMemberDao().memberDelete(grouperSyncMembers, true, true);
    }
    
    {
      List<GcGrouperSyncMembership> grouperSyncMemberships = grouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveAll();
      grouperSync.getGcGrouperSyncMembershipDao().membershipDelete(grouperSyncMemberships, true);
    }
    
    {
      List<GcGrouperSyncJob> grouperSyncJobs = grouperSync.getGcGrouperSyncJobDao().jobRetrieveAll();
      grouperSync.getGcGrouperSyncJobDao().jobDelete(grouperSyncJobs, true);
    }
    
    grouperSync.getGcGrouperSyncDao().delete();
  }

  /**
   * get sync details for a provisioner config
   * @return
   */
  public ProvisioningConfigSyncDetails getSyncDetails() {
    
    GcGrouperSync grouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, this.getConfigId());    
    ProvisioningConfigSyncDetails provisionerConfigSyncDetails = null;
    
    if (grouperSync != null) {
      provisionerConfigSyncDetails = new ProvisioningConfigSyncDetails();
      if (grouperSync.getLastFullSyncRun() != null) {
        provisionerConfigSyncDetails.setLastFullSyncTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastFullSyncRun()));
      }
      
      if (grouperSync.getLastIncrementalSyncRun() != null) {
        provisionerConfigSyncDetails.setLastIncrementalSyncTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastIncrementalSyncRun()));
      }
      
      if (grouperSync.getLastFullSyncStart() != null) {
        provisionerConfigSyncDetails.setLastFullSyncStartTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastFullSyncStart()));
      }
      
      if (grouperSync.getLastFullMetadataSyncStart() != null) {
        provisionerConfigSyncDetails.setLastFullMetadataSyncStartTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastFullMetadataSyncStart()));
      }
      
      if (grouperSync.getLastFullMetadataSyncRun() != null) {
        provisionerConfigSyncDetails.setLastFullMetadataSyncTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastFullMetadataSyncRun()));
      }
      
      provisionerConfigSyncDetails.setGroupCount(grouperSync.getGroupCount() == null ? 0: grouperSync.getGroupCount());
      provisionerConfigSyncDetails.setUserCount(grouperSync.getUserCount() == null ? 0: grouperSync.getUserCount());
      provisionerConfigSyncDetails.setRecordsCount(grouperSync.getRecordsCount() == null ? 0: grouperSync.getRecordsCount());
      
      List<GcGrouperSyncJob> gcGrouperSyncJobs = grouperSync.getGcGrouperSyncJobDao().jobRetrieveAll();
      for (GcGrouperSyncJob gcGrouperSyncJob: gcGrouperSyncJobs) {
        
        GcGrouperSyncLog gcGrouperSyncLog = grouperSync.getGcGrouperSyncLogDao().logRetrieveMostRecent(gcGrouperSyncJob.getId());
        
        GrouperSyncJobWrapper grouperSyncJobWrapper = new GrouperSyncJobWrapper();
        grouperSyncJobWrapper.setGcGrouperSyncJob(gcGrouperSyncJob);
        grouperSyncJobWrapper.setGcGrouperSyncLog(gcGrouperSyncLog);
        provisionerConfigSyncDetails.getSyncJobs().add(grouperSyncJobWrapper);
      }
      
      Map<String, Integer> errorCountByCodeGroup = grouperSync.getGcGrouperSyncGroupDao().retrieveErrorCountByCode();
      
      Map<String, Integer> errorCountByCodeMember = grouperSync.getGcGrouperSyncMemberDao().retrieveErrorCountByCode();
      
      Map<String, Integer> errorCountByCodeMembership = grouperSync.getGcGrouperSyncMembershipDao().retrieveErrorCountByCode();
      
      int[] counts = new int[] {0, 0, 0};
      setErrorCount(errorCountByCodeGroup, counts);
      setErrorCount(errorCountByCodeMember, counts);
      setErrorCount(errorCountByCodeMembership, counts);
      
      provisionerConfigSyncDetails.setExceptionCount(counts[0]);
      provisionerConfigSyncDetails.setTargetErrorCount(counts[1]);
      provisionerConfigSyncDetails.setValidationErrorCount(counts[2]);
      
    }
    
    return provisionerConfigSyncDetails;
  }
  
  private void setErrorCount(Map<String, Integer> errorCountByCode, int[] counts) {
    
    for (String error: GrouperUtil.nonNull(errorCountByCode).keySet()) {
      
      GcGrouperSyncErrorCode gcGrouperSyncErrorCode = GcGrouperSyncErrorCode.valueOf(error);
      if (gcGrouperSyncErrorCode == GcGrouperSyncErrorCode.ERR) {
        counts[0] = counts[0] + errorCountByCode.get(error);
      } else if (gcGrouperSyncErrorCode == GcGrouperSyncErrorCode.DNE) {
        counts[1] = counts[1] + errorCountByCode.get(error);
      } else {
        counts[2] = counts[2] + errorCountByCode.get(error);
      }
      
    }
  }

  /**
   * dont have endless loop
   */
  private static ThreadLocal<Boolean> inValidateThreadLocal = new InheritableThreadLocal<Boolean>();

  public void validatePreSaveNonProvisionerSpecific(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {

    // dont have endless loop
    Boolean inValidate = inValidateThreadLocal.get();
    if (inValidate != null && inValidate) {
      return;
    }
    
    inValidateThreadLocal.set(true);
    try {
      super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);

    } finally {
      inValidateThreadLocal.remove();
    }

  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {

    // dont have endless loop
    Boolean inValidate = inValidateThreadLocal.get();
    if (inValidate != null && inValidate) {
      return;
    }
    
    inValidateThreadLocal.set(true);
    try {
      super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);

      // if we have errors, then we might not even be able to load the provisioner
      if (GrouperUtil.length(errorsToDisplay) > 0 || GrouperUtil.length(validationErrorsToDisplay) > 0) {
        return;
      }
      
      Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
      
      try {
        // put these in a threadlocal config and try to validate
        
        for (String suffix : attributes.keySet()) {
          GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = attributes.get(suffix);
          if (grouperConfigurationModuleAttribute.isHasValue()) {
            String configKey = "provisioner." + this.getConfigId() + "." + suffix;
            GrouperLoaderConfig.retrieveConfig().propertiesThreadLocalOverrideMap().put(configKey, 
                grouperConfigurationModuleAttribute.getValueOrExpressionEvaluation());
          }
        }
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(this.getConfigId());
        grouperProvisioner.initialize(GrouperProvisioningType.fullProvisionFull);
        
        List<ProvisioningValidationIssue> errors = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
        if (errors.size() > 0) {
          for (ProvisioningValidationIssue provisioningValidationIssue : errors) {
            if (!StringUtils.isBlank(provisioningValidationIssue.getJqueryHandle())) {
              validationErrorsToDisplay.put(provisioningValidationIssue.getJqueryHandle(), provisioningValidationIssue.getMessage());
            } else {
              errorsToDisplay.add(provisioningValidationIssue.getMessage());
            }
          }
        }
  
      } finally {
        for (String suffix : attributes.keySet()) {
          GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = attributes.get(suffix);
          if (grouperConfigurationModuleAttribute.isHasValue()) {
            String configKey = "provisioner." + this.getConfigId() + "." + suffix;
            GrouperLoaderConfig.retrieveConfig().propertiesThreadLocalOverrideMap().remove(configKey);
          }
        }
      }
    } finally {
      inValidateThreadLocal.remove();
    }
    
  }

  public void correctFormFieldsForExpressionLanguageValues() {
    
    for (GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute : GrouperUtil.nonNull(this.retrieveAttributes()).values()) {
      
      if (grouperConfigurationModuleAttribute.isExpressionLanguage()) {
        grouperConfigurationModuleAttribute.setFormElement(ConfigItemFormElement.TEXT);
      }
      
    }
    
  }
  

}
