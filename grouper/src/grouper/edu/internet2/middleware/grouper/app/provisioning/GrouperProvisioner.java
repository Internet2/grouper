package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoAdapter;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.time.DurationFormatUtils;

public abstract class GrouperProvisioner {

  /**
   * this is the controller that makes the editing screen work, this is not the provisioning configuration class: retrieveGrouperProvisioningConfiguration()
   */
  private ProvisionerConfiguration provisionerConfiguration = null;

  /**
   * this is the controller that makes the editing screen work, this is not the provisioning configuration class: retrieveGrouperProvisioningConfiguration()
   * @return provisioner configuration
   */
  public ProvisionerConfiguration getProvisionerConfiguration() {
    return ProvisionerConfiguration.retrieveConfigurationByConfigSuffix(this.getClass().getName());
  }
  
  private String instanceId = GrouperUtil.uniqueId().toLowerCase();
  
  
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    result.append("GrouperProvisioner: ").append(this.getClass().getName());
    result.append(", TargetDao: ").append(this.grouperTargetDaoClass().getName());
    result.append(", Configuration: ").append(this.grouperProvisioningConfigurationClass().getName());
    if (!GrouperProvisioningAttributeManipulation.class.equals(this.grouperProvisioningAttributeManipulationClass())) {
      result.append(", AttributeManipulation: ").append(this.grouperProvisioningAttributeManipulationClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningBehavior().getClass().equals(GrouperProvisioningBehavior.class))) {
      result.append(", Behavior: ").append(this.retrieveGrouperProvisioningBehavior().getClass().getName());
    }
    if (!GrouperProvisioningCompare.class.equals(this.grouperProvisioningCompareClass())) {
      result.append(", Compare: ").append(this.grouperProvisioningCompareClass().getName());
    }
    if (!(this.grouperProvisioningConfigurationValidationClass().equals(GrouperProvisioningConfigurationValidation.class))) {
      result.append(", ConfigurationValidation: ").append(this.grouperProvisioningConfigurationValidationClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningData().getClass().equals(GrouperProvisioningData.class))) {
      result.append(", Data: ").append(this.retrieveGrouperProvisioningData().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningDataChanges().getClass().equals(GrouperProvisioningDataChanges.class))) {
      result.append(", DataChanges: ").append(this.retrieveGrouperProvisioningDataChanges().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningDataGrouper().getClass().equals(GrouperProvisioningDataGrouper.class))) {
      result.append(", DataGrouper: ").append(this.retrieveGrouperProvisioningDataGrouper().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningDataGrouperTarget().getClass().equals(GrouperProvisioningDataGrouperTarget.class))) {
      result.append(", DataGrouperTarget: ").append(this.retrieveGrouperProvisioningDataGrouperTarget().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningDataIncrementalInput().getClass().equals(GrouperProvisioningDataIncrementalInput.class))) {
      result.append(", DataIncrementalInput: ").append(this.retrieveGrouperProvisioningDataIncrementalInput().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningDataIndex().getClass().equals(GrouperProvisioningDataIndex.class))) {
      result.append(", DataIndex: ").append(this.retrieveGrouperProvisioningDataIndex().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningDataSync().getClass().equals(GrouperProvisioningDataSync.class))) {
      result.append(", DataSync: ").append(this.retrieveGrouperProvisioningDataSync().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningDataTarget().getClass().equals(GrouperProvisioningDataTarget.class))) {
      result.append(", DataTarget: ").append(this.retrieveGrouperProvisioningDataTarget().getClass().getName());
    }
    if (!GrouperProvisionerGrouperDao.class.equals(this.grouperDaoClass())) {
      result.append(", GrouperDao: ").append(this.grouperDaoClass().getName());
    }
    if (!GrouperProvisionerGrouperSyncDao.class.equals(this.grouperSyncDaoClass())) {
      result.append(", GrouperSyncDao: ").append(this.grouperSyncDaoClass().getName());
    }
    if (!GrouperProvisioningLinkLogic.class.equals(this.grouperProvisioningLinkLogicClass())) {
      result.append(", LinkLogic: ").append(this.grouperProvisioningLinkLogicClass().getName());
    }
    if (!GrouperProvisioningLogic.class.equals(this.grouperProvisioningLogicClass())) {
      result.append(", Logic: ").append(this.grouperProvisioningLogicClass().getName());
    }
    if (!GrouperProvisioningMatchingIdIndex.class.equals(this.grouperProvisioningMatchingIdIndexClass())) {
      result.append(", MatchingIdIndex: ").append(this.grouperProvisioningMatchingIdIndexClass().getName());
    }
    if (!GrouperProvisioningTranslatorBase.class.equals(this.grouperTranslatorClass())) {
      result.append(", Translator: ").append(this.grouperTranslatorClass().getName());
    }
    if (!GrouperProvisioningValidation.class.equals(this.grouperProvisioningValidationClass())) {
      result.append(", Validation: ").append(this.grouperProvisioningValidationClass().getName());
    }
    
    return result.toString();
  }
  
  private GrouperProvisionerTargetDaoAdapter grouperProvisionerTargetDaoAdapter = null;
  
  private GrouperProvisionerGrouperDao grouperProvisionerGrouperDao = null;

  private GrouperProvisionerGrouperSyncDao grouperProvisionerGrouperSyncDao = null;

  private GrouperProvisioningObjectLog grouperProvisioningObjectLog = null;
  
  private GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata;
  
  /**
   * reference to the consumer which is sending provisioning events to be processed
   */
  private ProvisioningConsumer provisioningConsumer = null;
  
  
  
  /**
   * reference to the consumer which is sending provisioning events to be processed
   * @return
   */
  public ProvisioningConsumer getProvisioningConsumer() {
    return provisioningConsumer;
  }

  /**
   * reference to the consumer which is sending provisioning events to be processed
   * @param provisioningConsumer
   */
  public void setProvisioningConsumer(ProvisioningConsumer provisioningConsumer) {
    this.provisioningConsumer = provisioningConsumer;
  }

  public void setDebugMap(Map<String, Object> debugMap) {
    this.debugMap = debugMap;
  }

  public GrouperProvisioningObjectLog getGrouperProvisioningObjectLog() {
    if (this.grouperProvisioningObjectLog == null) {
      this.grouperProvisioningObjectLog = new GrouperProvisioningObjectLog(this);
    }
    return grouperProvisioningObjectLog;
  }

  private GrouperProvisioningData grouperProvisioningData;

  private GrouperProvisioningDataGrouper grouperProvisioningDataGrouper;

  private GrouperProvisioningDataGrouperTarget grouperProvisioningDataGrouperTarget;

  private GrouperProvisioningDataSync grouperProvisioningDataSync;

  private GrouperProvisioningDataTarget grouperProvisioningDataTarget;

  private GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput ;

  private GrouperProvisioningDataChanges grouperProvisioningDataChanges;

  private GrouperProvisioningDataIndex grouperProvisioningDataIndex;

  
  public Map<String, Object> getDebugMap() {
    return debugMap;
  }

  /**
   * return the class of the DAO for this provisioner
   */
  protected abstract Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass();
  
  
  /**
   * returns the subclass of Data Access Object for this provisioner
   * @return the DAO
   */
  public GrouperProvisionerTargetDaoAdapter retrieveGrouperTargetDaoAdapter() {
    if (this.grouperProvisionerTargetDaoAdapter == null) {
      Class<? extends GrouperProvisionerTargetDaoBase> grouperProvisionerTargetDaoBaseClass = this.grouperTargetDaoClass();
      GrouperProvisionerTargetDaoBase grouperProvisionerTargetDaoBase = GrouperUtil.newInstance(grouperProvisionerTargetDaoBaseClass);
      grouperProvisionerTargetDaoBase.setGrouperProvisioner(this);
      this.grouperProvisionerTargetDaoAdapter = new GrouperProvisionerTargetDaoAdapter(this, grouperProvisionerTargetDaoBase);
    }
    return this.grouperProvisionerTargetDaoAdapter;
    
  }
  
  /**
   * returns the Grouper Data access Object
   * @return the DAO
   */
  public GrouperProvisionerGrouperDao retrieveGrouperDao() {
    if (this.grouperProvisionerGrouperDao == null) {
      Class<? extends GrouperProvisionerGrouperDao> grouperProvisionerGrouperDaoClass = this.grouperDaoClass();
      this.grouperProvisionerGrouperDao = GrouperUtil.newInstance(grouperProvisionerGrouperDaoClass);
      this.grouperProvisionerGrouperDao.setGrouperProvisioner(this);
    }
    return this.grouperProvisionerGrouperDao;
    
  }
  
  protected Class<? extends GrouperProvisionerGrouperDao> grouperDaoClass() {
    return GrouperProvisionerGrouperDao.class;
  }
  
  private GrouperProvisioningConfigurationBase grouperProvisioningConfigurationBase = null;

  private GrouperProvisioningLinkLogic grouperProvisioningLinkLogic = null;

  /**
   * return the class of the DAO for this provisioner
   */
  protected abstract Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationClass();
  
  /**
   * 
   */
  private GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = null;
  
  /**
   * @return the diagnostics
   */
  public GrouperProvisioningDiagnosticsContainer retrieveGrouperProvisioningDiagnosticsContainer() {
    
    if (this.grouperProvisioningDiagnosticsContainer == null) {
      this.grouperProvisioningDiagnosticsContainer = new GrouperProvisioningDiagnosticsContainer();
      this.grouperProvisioningDiagnosticsContainer.setGrouperProvisioner(this);
    }
    
    return this.grouperProvisioningDiagnosticsContainer;
  }
  
  /**
   * returns the subclass of Data Access Object for this provisioner
   * @return the DAO
   */
  public GrouperProvisioningConfigurationBase retrieveGrouperProvisioningConfiguration() {
    if (this.grouperProvisioningConfigurationBase == null) {
      Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationBaseClass = this.grouperProvisioningConfigurationClass();
      this.grouperProvisioningConfigurationBase = GrouperUtil.newInstance(grouperProvisioningConfigurationBaseClass);
      this.grouperProvisioningConfigurationBase.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningConfigurationBase;
    
  }
  
//  public GrouperProvisioningLogicAlgorithmBase retrieveProivisioningLogicAlgorithm() {
//    if (this.grouperProvisioningLogicAlgorithm == null) {
//      Class<? extends GrouperProvisioningLogicAlgorithmBase> grouperProvisioningLogicAlgorithmClass = 
//          this.retrieveProvisioningLogicAlgorithmClass();
//      this.grouperProvisioningLogicAlgorithm = GrouperUtil.newInstance(grouperProvisioningLogicAlgorithmClass);
//      this.grouperProvisioningLogicAlgorithm.setGrouperProvisioner(this);
//    }
//    return this.grouperProvisioningLogicAlgorithm;
//  }
  
  
  private GrouperProvisioningAttributeManipulation grouperProvisioningAttributeManipulation = null;

  /**
   * return the class of the attribute manipulation
   */
  protected Class<? extends GrouperProvisioningAttributeManipulation> grouperProvisioningAttributeManipulationClass() {
    return GrouperProvisioningAttributeManipulation.class;
  }
  
  /**
   * return the instance of the attribute manipulation
   * @return the logic
   */
  public GrouperProvisioningAttributeManipulation retrieveGrouperProvisioningAttributeManipulation() {
    if (this.grouperProvisioningAttributeManipulation == null) {
      Class<? extends GrouperProvisioningAttributeManipulation> grouperProvisioningLogicClass = this.grouperProvisioningAttributeManipulationClass();
      this.grouperProvisioningAttributeManipulation = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningAttributeManipulation.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningAttributeManipulation;
    
  }

  private GrouperProvisioningValidation grouperProvisioningValidation = null;

  /**
   * return the class of the provisioning validation
   */
  protected Class<? extends GrouperProvisioningValidation> grouperProvisioningValidationClass() {
    return GrouperProvisioningValidation.class;
  }

  /**
   * return the instance of the validation
   * @return the logic
   */
  public GrouperProvisioningValidation retrieveGrouperProvisioningValidation() {
    if (this.grouperProvisioningValidation == null) {
      Class<? extends GrouperProvisioningValidation> grouperProvisioningValidationClass = this.grouperProvisioningValidationClass();
      this.grouperProvisioningValidation = GrouperUtil.newInstance(grouperProvisioningValidationClass);
      this.grouperProvisioningValidation.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningValidation;
    
  }

  private GrouperProvisioningLogicIncremental grouperProvisioningLogicIncremental = null;
  
  private GrouperProvisioningLogic grouperProvisioningLogic = null;
  
  /**
   * return the class of the provisioning logic
   */
  protected Class<? extends GrouperProvisioningLogic> grouperProvisioningLogicClass() {
    return GrouperProvisioningLogic.class;
  }
  
  /**
   * return the class of the provisioning logic Incremental
   */
  protected Class<? extends GrouperProvisioningLogicIncremental> grouperProvisioningLogicIncrementalClass() {
    return GrouperProvisioningLogicIncremental.class;
  }
  
  /**
   * return the instance of the provisioning logic
   * @return the logic
   */
  public GrouperProvisioningLogic retrieveGrouperProvisioningLogic() {
    if (this.grouperProvisioningLogic == null) {
      Class<? extends GrouperProvisioningLogic> grouperProvisioningLogicClass = this.grouperProvisioningLogicClass();
      this.grouperProvisioningLogic = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningLogic.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningLogic;
    
  }
  
  /**
   * return the instance of the provisioning logic incremental
   * @return the logic
   */
  public GrouperProvisioningLogicIncremental retrieveGrouperProvisioningLogicIncremental() {
    if (this.grouperProvisioningLogicIncremental == null) {
      Class<? extends GrouperProvisioningLogicIncremental> grouperProvisioningLogicIncrementalClass = this.grouperProvisioningLogicIncrementalClass();
      this.grouperProvisioningLogicIncremental = GrouperUtil.newInstance(grouperProvisioningLogicIncrementalClass);
      this.grouperProvisioningLogicIncremental.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningLogicIncremental;
    
  }
  
  private GrouperProvisioningTranslatorBase grouperProvisioningTranslatorBase = null;

  /**
   * returns the instance of the translator
   * @return the translator
   */
  public GrouperProvisioningTranslatorBase retrieveGrouperTranslator() {
    if (this.grouperProvisioningTranslatorBase == null) {
      Class<? extends GrouperProvisioningTranslatorBase> grouperProvisioningTranslatorBaseClass = this.grouperTranslatorClass();
      this.grouperProvisioningTranslatorBase = GrouperUtil.newInstance(grouperProvisioningTranslatorBaseClass);
      this.grouperProvisioningTranslatorBase.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningTranslatorBase;
    
  }
  
  /**
   * @return the class of the translator for this provisioner (optional)
   */
  protected Class<? extends GrouperProvisioningTranslatorBase> grouperTranslatorClass() {
    return GrouperProvisioningTranslatorBase.class;
  }
  
  /**
   * factory method to get a provisioner by config id
   * @param configId
   * @return the provisioner
   */
  public static GrouperProvisioner retrieveProvisioner(String configId) {
    
    String provisionerClassName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("provisioner." + configId + ".class");
    @SuppressWarnings("unchecked")
    Class<GrouperProvisioner> provisionerClass = GrouperUtil.forName(provisionerClassName);
    GrouperProvisioner provisioner = GrouperUtil.newInstance(provisionerClass);
    provisioner.setConfigId(configId);
    return provisioner;
    
  }
  
  /**
   * dont re-use instances
   */
  private boolean done = false;
  /**
   * debug map for this provisioner
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  /**
   * provisioning table about this provisioner
   */
  private GcGrouperSync gcGrouperSync;
  /**
   * heartbeat thread
   */
  private GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
  /**
   * provisioning table about this job (full, incremental, etc) in this provisioner
   */
  private GcGrouperSyncJob gcGrouperSyncJob;
  /**
   * log for this sync
   */
  private GcGrouperSyncLog gcGrouperSyncLog;
  /**
   * provisioning output
   */
  private GrouperProvisioningOutput grouperProvisioningOutput = new GrouperProvisioningOutput();
  
  /**
   * provisioning output
   * @return output
   */
  public GrouperProvisioningOutput getGrouperProvisioningOutput() {
    return this.grouperProvisioningOutput;
  }

  /**
   * log every minute
   */
  private long lastLog = System.currentTimeMillis();
  /**
   * millis since 1970 when the sync started
   */
  private long millisWhenSyncStarted = -1;

  /**
   * log periodically
   * @param debugMap
   * @param gcTableSyncOutput 
   */
  public void logPeriodically(Map<String, Object> debugMap, GrouperProvisioningOutput grouperProvisioningOutput) {
    
    if (System.currentTimeMillis() - this.lastLog > (1000 * 60) - 10) {
    
      String debugString = GrouperClientUtils.mapToString(debugMap);
      grouperProvisioningOutput.setMessage(debugString);
      GrouperProvisioningLog.debugLog(debugString);
      this.lastLog = System.currentTimeMillis();

    }
    
  }

  private String configId;

  private long startedNanos;

  private boolean initialized = false;

  public GrouperProvisioner initialize(GrouperProvisioningType grouperProvisioningType1) {

    if (!this.initialized) {

      this.debugMap = new LinkedHashMap<String, Object>();

      GcDbAccess.threadLocalQueryCountReset();

      this.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(grouperProvisioningType1);

      this.retrieveGrouperProvisioningConfiguration().configureProvisioner();

      // let the target dao tell the framework what it can do
      this.retrieveGrouperTargetDaoAdapter().getWrappedDao().registerGrouperProvisionerDaoCapabilities(
          this.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities()
          );

      this.retrieveGrouperProvisioningObjectMetadata().initBuiltInMetadata();

      // let the provisioner tell the framework how the provisioner should behave with respect to the target
      this.registerProvisioningBehaviors(this.retrieveGrouperProvisioningBehavior());

    }
    this.initialized = true;
    return this;
  }
  
  /**
   * provision
   * @param grouperProvisioningType
   * @return the output
   */
  public GrouperProvisioningOutput provision(GrouperProvisioningType grouperProvisioningType1) {
    
    if (this.done) {
      throw new RuntimeException("Dont re-use instances of this class: " + GrouperProvisioner.class.getName());
    }

    this.millisWhenSyncStarted = System.currentTimeMillis();
    
    this.startedNanos = System.nanoTime();
    
    try {

      debugMap.put("finalLog", false);
      
      debugMap.put("state", "init");
      this.initialize(grouperProvisioningType1);
      
      this.gcGrouperSyncHeartbeat.setGcGrouperSyncJob(this.gcGrouperSyncJob);
      this.gcGrouperSyncHeartbeat.setFullSync(this.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isFullSync());
      this.gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {

        @Override
        public void run() {

          logPeriodically(debugMap, GrouperProvisioner.this.grouperProvisioningOutput);
          
        }
        
      });
      if (!this.gcGrouperSyncHeartbeat.isStarted()) {
        this.gcGrouperSyncHeartbeat.runHeartbeatThread();
      }

      debugMap.put("provisionerClass", this.getClass().getSimpleName());
      debugMap.put("configId", this.getConfigId());
      debugMap.put("provisioningType", grouperProvisioningType1);
    
      this.retrieveGrouperProvisioningLogic().provision();
      
      return this.grouperProvisioningOutput;
    } catch (RuntimeException re) {
      if (gcGrouperSyncLog != null) {
        gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
      }
      if (debugMap != null) {
        debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      }
      throw re;
    } finally {
      provisionFinallyBlock();
    }
  }

  public void provisionFinallyBlock() {
    // already did this
    if (this.done) {
      return;
    }
    this.done = true;
    
    GcGrouperSyncHeartbeat.endAndWaitForThread(this.gcGrouperSyncHeartbeat);

    debugMap.put("finalLog", true);
    
    synchronized (this) {
      try {
        if (this.gcGrouperSyncJob != null) {
          this.gcGrouperSyncJob.assignHeartbeatAndEndJob();
        }
      } catch (RuntimeException re2) {
        if (this.gcGrouperSyncLog != null) {
          this.gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
        }
        debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
      }
    }

    // TODO sum with dao, hibernate, and client
    this.grouperProvisioningOutput.setQueryCount(GcDbAccess.threadLocalQueryCountRetrieve());
    debugMap.put("queryCount", this.grouperProvisioningOutput.getQueryCount());
    
    int durationMillis = (int)((System.nanoTime()-this.startedNanos)/1000000);
    debugMap.put("tookMillis", durationMillis);
    debugMap.put("took", DurationFormatUtils.formatDurationHMS(durationMillis));
    
    String debugString = GrouperClientUtils.mapToString(debugMap);

    try {
      if (gcGrouperSyncLog != null) {
        gcGrouperSyncLog.setDescriptionToSave(debugString);
        gcGrouperSyncLog.setJobTookMillis(durationMillis);
        gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLog);
      }
    } catch (RuntimeException re3) {
      debugMap.put("exception3", GrouperClientUtils.getFullStackTrace(re3));
      debugString = GrouperClientUtils.mapToString(debugMap);
    }
    
    if (this.retrieveGrouperProvisioningConfiguration().isDebugLog()) {
      GrouperProvisioningLog.debugLog(debugString);
    }
    
    // already set total
    //gcTableSyncOutput.setTotal();
    this.grouperProvisioningOutput.setMessage(debugString);

    this.getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.end);

    // this isnt good
    if (debugMap.containsKey("exception") || debugMap.containsKey("exception2") || debugMap.containsKey("exception3")) {
      throw new RuntimeException(debugString);
    }
  }

  
  public void setGrouperProvisioningOutput(
      GrouperProvisioningOutput grouperProvisioningOutput) {
    this.grouperProvisioningOutput = grouperProvisioningOutput;
  }

  /**
   * provisioning table about this provisioner
   * @return sync
   */
  public GcGrouperSync getGcGrouperSync() {
    return this.gcGrouperSync;
  }

  /**
   * heartbeat thread
   * @return heartbeat
   */
  public GcGrouperSyncHeartbeat getGcGrouperSyncHeartbeat() {
    return this.gcGrouperSyncHeartbeat;
  }

  /**
   * provisioning table about this job (full, incremental, etc) in this provisioner
   * @return job
   */
  public GcGrouperSyncJob getGcGrouperSyncJob() {
    return gcGrouperSyncJob;
  }

  /**
   * log for this sync
   * @return
   */
  public GcGrouperSyncLog getGcGrouperSyncLog() {
    return this.gcGrouperSyncLog;
  }

  /**
   * millis since 1970 when the sync started
   * @return when started
   */
  public long getMillisWhenSyncStarted() {
    return this.millisWhenSyncStarted;
  }

  /**
   * provisioning table about this provisioner
   * @param gcGrouperSync1
   */
  public void setGcGrouperSync(GcGrouperSync gcGrouperSync1) {
    this.gcGrouperSync = gcGrouperSync1;
  }

  /**
   * heartbeat thread
   * @param gcGrouperSyncHeartbeat1
   */
  public void setGcGrouperSyncHeartbeat(GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat1) {
    this.gcGrouperSyncHeartbeat = gcGrouperSyncHeartbeat1;
  }

  /**
   * provisioning table about this job (full, incremental, etc) in this provisioner
   * @param gcGrouperSyncJob1
   */
  public void setGcGrouperSyncJob(GcGrouperSyncJob gcGrouperSyncJob1) {
    this.gcGrouperSyncJob = gcGrouperSyncJob1;
  }

  /**
   * log for this sync
   * @param gcGrouperSyncLog1
   */
  public void setGcGrouperSyncLog(GcGrouperSyncLog gcGrouperSyncLog1) {
    this.gcGrouperSyncLog = gcGrouperSyncLog1;
  }

  public String getConfigId() {
    return configId;
  }

  public void setConfigId(String configId) {
    this.configId = configId;
  }

  
  public GrouperProvisioningDataGrouper retrieveGrouperProvisioningDataGrouper() {
    if (this.grouperProvisioningDataGrouper == null) {
      this.grouperProvisioningDataGrouper = new GrouperProvisioningDataGrouper();
      this.grouperProvisioningDataGrouper.setGrouperProvisioner(this);
    }
    return grouperProvisioningDataGrouper;
  }
  
  public GrouperProvisioningDataSync retrieveGrouperProvisioningDataSync() {
    if (this.grouperProvisioningDataSync == null) {
      this.grouperProvisioningDataSync = new GrouperProvisioningDataSync();
      this.grouperProvisioningDataSync.setGrouperProvisioner(this);
    }
    return grouperProvisioningDataSync;
  }

  public GrouperProvisioningData retrieveGrouperProvisioningData() {
    if (this.grouperProvisioningData == null) {
      this.grouperProvisioningData = new GrouperProvisioningData();
      this.grouperProvisioningData.setGrouperProvisioner(this);
    }
    return grouperProvisioningData;
  }

  public GrouperProvisioningDataGrouperTarget retrieveGrouperProvisioningDataGrouperTarget() {
    if (this.grouperProvisioningDataGrouperTarget == null) {
      this.grouperProvisioningDataGrouperTarget = new GrouperProvisioningDataGrouperTarget();
      this.grouperProvisioningDataGrouperTarget.setGrouperProvisioner(this);
    }
    return grouperProvisioningDataGrouperTarget;
  }

  public GrouperProvisioningDataTarget retrieveGrouperProvisioningDataTarget() {
    if (this.grouperProvisioningDataTarget == null) {
      this.grouperProvisioningDataTarget = new GrouperProvisioningDataTarget();
      this.grouperProvisioningDataTarget.setGrouperProvisioner(this);
    }
    return grouperProvisioningDataTarget;
  }

  
  public GrouperProvisioningDataIncrementalInput retrieveGrouperProvisioningDataIncrementalInput() {
    if (this.grouperProvisioningDataIncrementalInput == null) {
      this.grouperProvisioningDataIncrementalInput = new GrouperProvisioningDataIncrementalInput();
      this.grouperProvisioningDataIncrementalInput.setGrouperProvisioner(this);
    }
    return grouperProvisioningDataIncrementalInput;
  }

  
  public GrouperProvisioningDataChanges retrieveGrouperProvisioningDataChanges() {
    if (this.grouperProvisioningDataChanges == null) {
      this.grouperProvisioningDataChanges = new GrouperProvisioningDataChanges();
      this.grouperProvisioningDataChanges.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningDataChanges;
  }

  public GrouperProvisioningDataIndex retrieveGrouperProvisioningDataIndex() {
    if (this.grouperProvisioningDataIndex == null) {
      this.grouperProvisioningDataIndex = new GrouperProvisioningDataIndex();
      this.grouperProvisioningDataIndex.setGrouperProvisioner(this);
    }
    return grouperProvisioningDataIndex;
  }

  
  
  private ProvisioningSyncResult provisioningSyncResult = new ProvisioningSyncResult();

  
  public ProvisioningSyncResult getProvisioningSyncResult() {
    return provisioningSyncResult;
  }

  
  public void setProvisioningSyncResult(ProvisioningSyncResult provisioningSyncResult) {
    this.provisioningSyncResult = provisioningSyncResult;
  }

  /**
   * return the instance of the compare logic
   * @return the logic
   */
  public GrouperProvisioningCompare retrieveGrouperProvisioningCompare() {
    if (this.grouperProvisioningCompare == null) {
      Class<? extends GrouperProvisioningCompare> grouperProvisioningLogicClass = this.grouperProvisioningCompareClass();
      this.grouperProvisioningCompare = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningCompare.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningCompare;
    
  }

  private GrouperProvisioningCompare grouperProvisioningCompare;
  
  protected Class<? extends GrouperProvisioningCompare> grouperProvisioningCompareClass() {
    return GrouperProvisioningCompare.class;
  }
  
  /**
   * return the instance of the indexing logic
   * @return the logic
   */
  public GrouperProvisioningMatchingIdIndex retrieveGrouperProvisioningMatchingIdIndex() {
    if (this.grouperProvisioningMatchingIdIndex == null) {
      Class<? extends GrouperProvisioningMatchingIdIndex> grouperProvisioningLogicClass = this.grouperProvisioningMatchingIdIndexClass();
      this.grouperProvisioningMatchingIdIndex = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningMatchingIdIndex.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningMatchingIdIndex;
    
  }

  private GrouperProvisioningMatchingIdIndex grouperProvisioningMatchingIdIndex;
  
  protected Class<? extends GrouperProvisioningMatchingIdIndex> grouperProvisioningMatchingIdIndexClass() {
    return GrouperProvisioningMatchingIdIndex.class;
  }

  /**
   * return the instance of the provisioning configuration validation
   * @return the logic
   */
  public GrouperProvisioningConfigurationValidation retrieveGrouperProvisioningConfigurationValidation() {
    if (this.grouperProvisioningConfigurationValidation == null) {
      Class<? extends GrouperProvisioningConfigurationValidation> grouperProvisioningLogicClass = this.grouperProvisioningConfigurationValidationClass();
      this.grouperProvisioningConfigurationValidation = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningConfigurationValidation.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningConfigurationValidation;
    
  }

  private GrouperProvisioningConfigurationValidation grouperProvisioningConfigurationValidation;
  
  protected Class<? extends GrouperProvisioningConfigurationValidation> grouperProvisioningConfigurationValidationClass() {
    return GrouperProvisioningConfigurationValidation.class;
  }

  /**
   * returns the Grouper Sync Data access Object
   * @return the DAO
   */
  public GrouperProvisionerGrouperSyncDao retrieveGrouperSyncDao() {
    if (this.grouperProvisionerGrouperSyncDao == null) {
      Class<? extends GrouperProvisionerGrouperSyncDao> grouperProvisionerGrouperSyncDaoClass = this.grouperSyncDaoClass();
      this.grouperProvisionerGrouperSyncDao = GrouperUtil.newInstance(grouperProvisionerGrouperSyncDaoClass);
      this.grouperProvisionerGrouperSyncDao.setGrouperProvisioner(this);
    }
    return this.grouperProvisionerGrouperSyncDao;
    
  }

  protected Class<? extends GrouperProvisionerGrouperSyncDao> grouperSyncDaoClass() {
    return GrouperProvisionerGrouperSyncDao.class;
  }

  private GrouperProvisioningBehavior grouperProvisioningBehavior = new GrouperProvisioningBehavior(this);



  
  public GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior() {
    return grouperProvisioningBehavior;
  }

  
  public void setGrouperProvisioningBehavior(
      GrouperProvisioningBehavior grouperProvisioningBehavior) {
    this.grouperProvisioningBehavior = grouperProvisioningBehavior;
  }
  
  /**
   * let the provisioner tell the framework how the provisioner should behave with respect to the target
   * @param grouperProvisioningBehavior
   */
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
        
  }

  /**
   * returns the subclass of Data Access Object for this provisioner
   * @return the DAO
   */
  public GrouperProvisioningLinkLogic retrieveGrouperProvisioningLinkLogic() {
    if (this.grouperProvisioningLinkLogic == null) {
      Class<? extends GrouperProvisioningLinkLogic> grouperProvisioningLinkLogicClass = this.grouperProvisioningLinkLogicClass();
      this.grouperProvisioningLinkLogic = GrouperUtil.newInstance(grouperProvisioningLinkLogicClass);
      this.grouperProvisioningLinkLogic.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningLinkLogic;
    
  }
  
  /**
   * returns the object metadata instance
   * @return the object metadata instance
   */
  public GrouperProvisioningObjectMetadata retrieveGrouperProvisioningObjectMetadata() {
    if (this.grouperProvisioningObjectMetadata == null) {
      Class<? extends GrouperProvisioningObjectMetadata> grouperProvisioningObjectMetadataClass = this.grouperProvisioningObjectMetadataClass();
      this.grouperProvisioningObjectMetadata = GrouperUtil.newInstance(grouperProvisioningObjectMetadataClass);
      this.grouperProvisioningObjectMetadata.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningObjectMetadata;
    
  }
  
  /**
   * return the class of the object metadata
   */
  protected Class<? extends GrouperProvisioningObjectMetadata> grouperProvisioningObjectMetadataClass() {
    return GrouperProvisioningObjectMetadata.class;
  }

  /**
   * return the class of the link logic
   */
  protected Class<? extends GrouperProvisioningLinkLogic> grouperProvisioningLinkLogicClass() {
    return GrouperProvisioningLinkLogic.class;
  }

  /**
   * 
   */
  public void propagateProvisioningAttributes() {
    Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningFolderAttributes = this.retrieveGrouperDao().retrieveAllProvisioningFolderAttributes();
    Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningGroupAttributes = this.retrieveGrouperDao().retrieveAllProvisioningGroupAttributes();
    Set<GrouperProvisioningObjectAttributes> grouperProvisioningObjectAttributesToProcess = new HashSet<GrouperProvisioningObjectAttributes>();
    grouperProvisioningObjectAttributesToProcess.addAll(grouperProvisioningFolderAttributes.values());
    grouperProvisioningObjectAttributesToProcess.addAll(grouperProvisioningGroupAttributes.values());
    Set<String> policyGroupIds = this.retrieveGrouperDao().retrieveAllProvisioningGroupIdsThatArePolicyGroups();
    
    GrouperProvisioningService.propagateProvisioningAttributes(this, grouperProvisioningObjectAttributesToProcess, grouperProvisioningFolderAttributes, policyGroupIds);
  }
}
