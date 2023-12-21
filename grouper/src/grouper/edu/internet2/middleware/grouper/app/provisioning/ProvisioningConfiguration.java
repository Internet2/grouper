package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.logging.Log;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.azure.AzureProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.boxProvisioner.BoxProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.app.daemon.GrouperDaemonConfiguration;
import edu.internet2.middleware.grouper.app.daemon.GrouperDaemonOtherJobProvisioningFullSyncConfiguration;
import edu.internet2.middleware.grouper.app.daemon.GrouperDaemonProvisioningIncrementalSyncConfiguration;
import edu.internet2.middleware.grouper.app.duo.DuoProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.duo.role.DuoRoleProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.genericProvisioner.GenericProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.google.GoogleProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.messagingProvisioning.MessagingProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.midpointProvisioning.MidPointProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.remedyV2.RemedyProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace.DigitalMarketplaceProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Configuration;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixProvisionerConfiguration;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

public abstract class ProvisioningConfiguration extends GrouperConfigurationModuleBase {
  
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ProvisioningConfiguration.class);
  
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
    
    List<String> configClassNamesList = new ArrayList<>();
    
    configClassNamesList.add(AzureProvisionerConfiguration.class.getName());
    configClassNamesList.add(DigitalMarketplaceProvisionerConfiguration.class.getName());
    configClassNamesList.add(DuoProvisionerConfiguration.class.getName());
    configClassNamesList.add(DuoRoleProvisionerConfiguration.class.getName());
    configClassNamesList.add(GenericProvisionerConfiguration.class.getName());
    configClassNamesList.add(GoogleProvisionerConfiguration.class.getName());
    configClassNamesList.add(LdapProvisionerConfiguration.class.getName());
    configClassNamesList.add(MessagingProvisionerConfiguration.class.getName());
    configClassNamesList.add(MidPointProvisionerConfiguration.class.getName());
    configClassNamesList.add(GrouperScim2Configuration.class.getName());
    configClassNamesList.add(RemedyProvisionerConfiguration.class.getName());
    configClassNamesList.add(SqlProvisionerConfiguration.class.getName());
    configClassNamesList.add(TeamDynamixProvisionerConfiguration.class.getName());
    configClassNamesList.add(BoxProvisionerConfiguration.class.getName());
//    provisionerConfigClassNames.add("edu.internet2.middleware.grouperBox.BoxProvisionerConfiguration");
    
    String extraProvisionerConfigRegex = "^grouperExtraProvisionerConfiguration\\.([^.]+)\\.class$";
    Pattern extraExternalSystemPattern = Pattern.compile(extraProvisionerConfigRegex);
    Map<String, String> extraExternalSystemClasses = GrouperConfig.retrieveConfig().propertiesMap(extraExternalSystemPattern);
    if (GrouperUtil.length(extraExternalSystemClasses) > 0) {
      for (String className : extraExternalSystemClasses.values()) {
        configClassNamesList.add(className);
      }
    }
    
    Collections.sort(configClassNamesList, new Comparator<String>() {

      @Override
      public int compare(String arg0, String arg1) {
        return GrouperUtil.suffixAfterChar(arg0, '.').compareTo(GrouperUtil.suffixAfterChar(arg1, '.'));
      }
    });
    
    provisionerConfigClassNames.addAll(configClassNamesList);
    
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
  
  
  
  /**
   * list of configured provisioner systems that are viewable for the subject
   * @param subject - subject for whom to retrive all viewable provisioning configs
   * @return
   */
  public static List<ProvisioningConfiguration> retrieveAllViewableProvisioningConfigurations(Subject subject) {
    
    List<ProvisioningConfiguration> allProvisioningConfigurations = retrieveAllProvisioningConfigurations();
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return allProvisioningConfigurations;
    }
    
    List<ProvisioningConfiguration> result = new ArrayList<>();
    
    for (ProvisioningConfiguration provisioningConfiguration: allProvisioningConfigurations) {
      if (isSubjectInAssignOrViewGroup("groupAllowedToAssign", subject, provisioningConfiguration)) {
        result.add(provisioningConfiguration);
        continue;
      }
      
      if (isSubjectInAssignOrViewGroup("groupAllowedToView", subject, provisioningConfiguration)) {
        result.add(provisioningConfiguration);
      }
    } 
    
    
    return result;
  }
  
  /**
   * list of configured provisioner systems that are assignable for the subject
   * @param subject - subject for whom to retrive all assignable provisioning configs
   * @return
   */
  public static List<ProvisioningConfiguration> retrieveAllAssignableProvisioningConfigurations(Subject subject) {
    
    List<ProvisioningConfiguration> allProvisioningConfigurations = retrieveAllProvisioningConfigurations();
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return allProvisioningConfigurations;
    }
    
    List<ProvisioningConfiguration> result = new ArrayList<>();
    
    for (ProvisioningConfiguration provisioningConfiguration: allProvisioningConfigurations) {
      if (isSubjectInAssignOrViewGroup("groupAllowedToAssign", subject, provisioningConfiguration)) {
        result.add(provisioningConfiguration);
      }
    } 
    
    return result;
  }
  
  
  /**
   * check if subject is member of allowedToAssign or allowedToView group
   * @param suffix
   * @param subject
   * @param provisioningConfiguration
   * @return
   */
  public static boolean isSubjectInAssignOrViewGroup(String suffix, Subject subject, ProvisioningConfiguration provisioningConfiguration) {
    
    String groupUuidOrNameString = provisioningConfiguration.retrieveAttributeValueFromConfig(suffix, false);
    
    boolean result = false;
    
    if (StringUtils.isNotBlank(groupUuidOrNameString)) {
      
      boolean isSubjectInGroup = (boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Group group = GroupFinder.findByUuid(groupUuidOrNameString, false);
          
          if (group == null) {
            group = GroupFinder.findByName(groupUuidOrNameString, false);
          }
          
          if (group == null) {
            LOG.warn(suffix + " = "+groupUuidOrNameString +" which is not a valid group id or name for provisioner "+provisioningConfiguration.getConfigId());
          }
          
          if (group != null && group.hasMember(subject)) {
            return true;
          }
          
          return false;
        }
      });
      
      result = isSubjectInGroup;
      
    }
    
    return result;
    
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
    
    // delete full sync and incremental sync daemon
    Pattern pattern = Pattern.compile("^otherJob\\.(.*)\\.provisionerConfigId$");
    Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(pattern);
    String jobConfigId = null;
    for (String configId: configIds) {
      String className = "otherJob."+configId+".class";
      String provisionerConfigId = "otherJob."+configId+".provisionerConfigId";
      if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(className), GrouperProvisioningFullSyncJob.class.getName()) && 
          StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(provisionerConfigId), getConfigId() )) {
        jobConfigId = configId;
        break;
      }
    }
    
    if (StringUtils.isNotBlank(jobConfigId)) {
      
      
      GrouperDaemonConfiguration configToDelete = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_"+jobConfigId);

      configToDelete.deleteConfig(true);
      
      try {
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
        
        JobKey jobKey = new JobKey(configToDelete.getDaemonJobPrefix()+configToDelete.getConfigId());
        scheduler.deleteJob(jobKey);
      } catch (Exception e) {
        throw new RuntimeException("Could not delete full sync daemon with job key "+configToDelete.getDaemonJobPrefix()+configToDelete.getConfigId());
      }
      
    }
    
    pattern = Pattern.compile("^changeLog\\.consumer\\.(.*)\\.provisionerConfigId$");
    configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(pattern);
   
    String incrementalJobConfigId = null;
    for (String configId: configIds) {
      String className = "changeLog.consumer."+configId+".publisher.class";
      String provisionerConfigId = "changeLog.consumer."+configId+".provisionerConfigId";
      if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(className), ProvisioningConsumer.class.getName()) && 
          StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(provisionerConfigId), getConfigId() )) {
        incrementalJobConfigId = configId;
        break;
      }
    }
    
    if (StringUtils.isNotBlank(incrementalJobConfigId)) {
      
      GrouperDaemonConfiguration configToDelete = GrouperDaemonConfiguration.retrieveImplementationFromJobName("CHANGE_LOG_consumer_"+incrementalJobConfigId);

      configToDelete.deleteConfig(true);
      
      try {
        Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
        
        JobKey jobKey = new JobKey(configToDelete.getDaemonJobPrefix()+configToDelete.getConfigId());
        scheduler.deleteJob(jobKey);
      } catch (Exception e) {
        throw new RuntimeException("Could not delete incremental sync daemon with job key "+configToDelete.getDaemonJobPrefix()+configToDelete.getConfigId());
      }
    }
    
    
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
      provisionerConfigSyncDetails.setMembershipCount(grouperSync.getRecordsCount() == null ? 0: grouperSync.getRecordsCount());
      
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
  
  
  /**
   * get sync stats for a provisioner config
   * @return
   */
  public ProvisioningConfigSyncStats getSyncStats() {
    
    GcGrouperSync grouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, this.getConfigId());    
    ProvisioningConfigSyncStats provisionerConfigSyncStats = null;
    
    if (grouperSync != null) {
      provisionerConfigSyncStats = new ProvisioningConfigSyncStats();
      if (grouperSync.getLastFullSyncRun() != null) {
        provisionerConfigSyncStats.setLastFullSyncTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastFullSyncRun()));
      }
      
      if (grouperSync.getLastIncrementalSyncRun() != null) {
        provisionerConfigSyncStats.setLastIncrementalSyncTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastIncrementalSyncRun()));
      }
      
      if (grouperSync.getLastFullSyncStart() != null) {
        provisionerConfigSyncStats.setLastFullSyncStartTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastFullSyncStart()));
      }
      
      if (grouperSync.getLastFullMetadataSyncStart() != null) {
        provisionerConfigSyncStats.setLastFullMetadataSyncStartTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastFullMetadataSyncStart()));
      }
      
      if (grouperSync.getLastFullMetadataSyncRun() != null) {
        provisionerConfigSyncStats.setLastFullMetadataSyncTimestamp(GrouperUtil.dateStringValue(grouperSync.getLastFullMetadataSyncRun()));
      }
      
      provisionerConfigSyncStats.setGroupCount(grouperSync.getGroupCount() == null ? 0: grouperSync.getGroupCount());
      provisionerConfigSyncStats.setUserCount(grouperSync.getUserCount() == null ? 0: grouperSync.getUserCount());
      provisionerConfigSyncStats.setMembershipCount(grouperSync.getRecordsCount() == null ? 0: grouperSync.getRecordsCount());
      
    }
    
    return provisionerConfigSyncStats;
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
          String configKey = "provisioner." + this.getConfigId() + "." + suffix;
          if (grouperConfigurationModuleAttribute.isHasValue()) {
            GrouperLoaderConfig.retrieveConfig().propertiesThreadLocalOverrideMap().put(configKey, 
                grouperConfigurationModuleAttribute.getValueOrExpressionEvaluation());
          } else {
            if (!StringUtils.isEmpty(GrouperLoaderConfig.retrieveConfig().propertyValueString(configKey))) {
              GrouperLoaderConfig.retrieveConfig().propertiesThreadLocalOverrideMap().put(configKey, null);
            }
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
        GrouperLoaderConfig.retrieveConfig().propertiesThreadLocalOverrideMap().clear();
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

  @Override
  public void editConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);

    addDaemonsIfNecessary(message, errorsToDisplay, validationErrorsToDisplay);
    
  }

  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay,
      List<String> actionsPerformed) {
    
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
        actionsPerformed);
    
      
    addDaemonsIfNecessary(message, errorsToDisplay, validationErrorsToDisplay);
    
    
  }

  public void addDaemonsIfNecessary(StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
    
      GrouperConfigurationModuleAttribute disabledFullSyncAttribute = this.retrieveAttributes().get("addDisabledFullSyncDaemon");
      
      if (disabledFullSyncAttribute != null && GrouperUtil.booleanValue(disabledFullSyncAttribute.getValueOrExpressionEvaluation(), false)) {
        
        boolean foundConfig = false;
        
        Pattern pattern = Pattern.compile("^otherJob\\.(.*)\\.provisionerConfigId$");
        Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(pattern);
        
        for (String configId: configIds) {
          String className = "otherJob."+configId+".class";
          String provisionerConfigId = "otherJob."+configId+".provisionerConfigId";
          if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(className), GrouperProvisioningFullSyncJob.class.getName()) && 
              StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(provisionerConfigId), getConfigId() )) {
            foundConfig = true;
          }
        }
        
       
        if (!foundConfig) {
          
          GrouperDaemonOtherJobProvisioningFullSyncConfiguration fullSyncConfig = new GrouperDaemonOtherJobProvisioningFullSyncConfiguration();
          fullSyncConfig.setConfigId("provisioner_full_"+getConfigId());
          
          Map<String, GrouperConfigurationModuleAttribute> attributes = fullSyncConfig.retrieveAttributes();
          
          attributes.get("class").setValue("edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob");
          attributes.get("provisionerConfigId").setValue(this.getConfigId());
          
          int hour = RandomUtils.nextInt(3, 8);
          int minute = RandomUtils.nextInt(0, 60);
          
          String cronExpression = "0 "+minute + " " + hour + " * * ?";
          
          attributes.get("quartzCron").setValue(cronExpression);
          
          fullSyncConfig.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
          
          if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
            return;
          }
          
          try {
            GrouperLoader.scheduleJobs();
            Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
            JobKey jobKey = new JobKey(fullSyncConfig.getDaemonJobPrefix()+fullSyncConfig.getConfigId());
            scheduler.pauseJob(jobKey);
          } catch (SchedulerException e) {
            throw new RuntimeException("Could not pause the job successfully");
          }
          
        }
        
      }
      
      GrouperConfigurationModuleAttribute disabledIncrementalSyncAttribute = this.retrieveAttributes().get("addDisabledIncrementalSyncDaemon");
      
      if (disabledIncrementalSyncAttribute != null && GrouperUtil.booleanValue(disabledIncrementalSyncAttribute.getValueOrExpressionEvaluation(), false)) {
        
        
        boolean foundConfig = false;
        
        Pattern pattern = Pattern.compile("^changeLog\\.consumer\\.(.*)\\.provisionerConfigId$");
        Set<String> configIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(pattern);
        
        for (String configId: configIds) {
          String className = "changeLog.consumer."+configId+".publisher.class";
          String provisionerConfigId = "changeLog.consumer."+configId+".provisionerConfigId";
          if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(className), ProvisioningConsumer.class.getName()) && 
              StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(provisionerConfigId), getConfigId() )) {
            foundConfig = true;
          }
        }
        
        
        if (!foundConfig) {
          
          GrouperDaemonProvisioningIncrementalSyncConfiguration incrementalSyncConfig = new GrouperDaemonProvisioningIncrementalSyncConfiguration();
          incrementalSyncConfig.setConfigId("provisioner_incremental_"+getConfigId());
          
          Map<String, GrouperConfigurationModuleAttribute> attributes = incrementalSyncConfig.retrieveAttributes();
          
          attributes.get("class").setValue("edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer");
          attributes.get("provisionerConfigId").setValue(this.getConfigId());
          
          attributes.get("quartzCron").setValue("0 * * * * ?");
          attributes.get("publisher.class").setValue("edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer");
          
          GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              incrementalSyncConfig.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
              return null;
            }
          });
          
          if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
            return;
          }
          
          try {
            GrouperLoader.scheduleJobs();
            Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
            JobKey jobKey = new JobKey(incrementalSyncConfig.getDaemonJobPrefix()+incrementalSyncConfig.getConfigId());
            scheduler.pauseJob(jobKey);
          } catch (SchedulerException e) {
            throw new RuntimeException("Could not pause the job successfully");
          }
          
        }
        
        
      }
    }
  }

}
