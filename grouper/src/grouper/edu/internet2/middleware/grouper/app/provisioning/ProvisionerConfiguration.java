package edu.internet2.middleware.grouper.app.provisioning;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.azure.AzureProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Configuration;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public abstract class ProvisionerConfiguration extends GrouperConfigurationModuleBase {
  
  public static ProvisionerConfiguration retrieveConfigurationByConfigSuffix(String propertyValueThatIdentifiesThisDaemon) {
    for (ProvisionerConfiguration provisionerConfiguration : GrouperUtil.nonNull(retrieveAllProvisionerConfigurationTypes())) {
      if (StringUtils.equals(propertyValueThatIdentifiesThisDaemon, 
          provisionerConfiguration.getPropertyValueThatIdentifiesThisConfig())) {
        return provisionerConfiguration;
      }
    }
    return null;
  }
  
  public final static Set<String> provisionerConfigClassNames = new LinkedHashSet<String>();
  
  static {
    provisionerConfigClassNames.add(AzureProvisionerConfiguration.class.getName());
    provisionerConfigClassNames.add(LdapProvisionerConfiguration.class.getName());
    provisionerConfigClassNames.add(GrouperScim2Configuration.class.getName());
    provisionerConfigClassNames.add(SqlProvisionerConfiguration.class.getName());
//    provisionerConfigClassNames.add("edu.internet2.middleware.grouperBox.BoxProvisionerConfiguration");
  }
  
  /**
   * list of systems that can be configured
   * @return
   */
  public static List<ProvisionerConfiguration> retrieveAllProvisionerConfigurationTypes() {
    return (List<ProvisionerConfiguration>) (Object) retrieveAllConfigurationTypesHelper(provisionerConfigClassNames);
  }
  
  /**
   * list of configured provisioner systems
   * @return
   */
  public static List<ProvisionerConfiguration> retrieveAllProvisionerConfigurations() {
   return (List<ProvisionerConfiguration>) (Object) retrieveAllConfigurations(provisionerConfigClassNames);
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
  public ProvisionerConfigSyncDetails getSyncDetails() {
    
    GcGrouperSync grouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, this.getConfigId());    
    ProvisionerConfigSyncDetails provisionerConfigSyncDetails = null;
    
    if (grouperSync != null) {
      provisionerConfigSyncDetails = new ProvisionerConfigSyncDetails();
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
  
  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    GrouperConfigurationModuleAttribute numberOfMetadataAttribute = attributes.get("numberOfMetadata");
    
    String valueOrExpressionEvaluation = numberOfMetadataAttribute.getValueOrExpressionEvaluation();
    
    int numberOfMetadatas = GrouperUtil.intValue(valueOrExpressionEvaluation, 0);
    
    for (int i=0; i<numberOfMetadatas; i++) {
      GrouperConfigurationModuleAttribute nameAttribute = attributes.get("metadata."+i+".name");
      String nameAttributeValue = nameAttribute.getValueOrExpressionEvaluation();
      if (!nameAttributeValue.startsWith("md_") || !nameAttributeValue.matches("^[a-zA-Z0-9_]+$")) {
        String error = GrouperTextContainer.textOrNull("provisionerConfigurationSaveErrorMetadataNotValidFormat");
        validationErrorsToDisplay.put(nameAttribute.getHtmlForElementIdHandle(), error);
        return;
      }
      
      GrouperConfigurationModuleAttribute defaultValueAttribute = attributes.get("metadata."+i+".defaultValue");
      if (defaultValueAttribute != null && StringUtils.isNotBlank(defaultValueAttribute.getValueOrExpressionEvaluation())) {
        String valueBeforeConversion = defaultValueAttribute.getValueOrExpressionEvaluation();
        GrouperConfigurationModuleAttribute typeAttribute = attributes.get("metadata."+i+".valueType");
        
        GrouperProvisioningObjectMetadataItemValueType valueType = null;
        if (typeAttribute == null || StringUtils.isBlank(typeAttribute.getValueOrExpressionEvaluation())) {
          valueType = GrouperProvisioningObjectMetadataItemValueType.STRING;
        } else {
          valueType = GrouperProvisioningObjectMetadataItemValueType.valueOfIgnoreCase(typeAttribute.getValueOrExpressionEvaluation(), true);
        }
        
        if (!valueType.canConvertToCorrectType(valueBeforeConversion)) { 
          String error = GrouperTextContainer.textOrNull("provisionerConfigurationSaveErrorMetadataDefaultValueNotCorrectType");
          error = GrouperUtil.replace(error, "$$defaultValue$$", valueBeforeConversion);
          error = GrouperUtil.replace(error, "$$selectedType$$", valueType.name().toLowerCase());
          validationErrorsToDisplay.put(defaultValueAttribute.getHtmlForElementIdHandle(), error);
          return;
        }
        
      }
      
      
    }
    
    
    
    
    
  }
  

}
