package edu.internet2.middleware.grouper.app.provisioning;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncLog;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.time.DurationFormatUtils;

public abstract class GrouperProvisioner {

  private GrouperProvisionerTargetDaoBase grouperProvisionerTargetDaoBase = null;
  
  private GrouperProvisionerGrouperDao grouperProvisionerGrouperDao = null;

  private GrouperProvisionerGrouperSyncDao grouperProvisionerGrouperSyncDao = null;

  private GrouperProvisioningObjectLog grouperProvisioningObjectLog = null;
  
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

  private GrouperProvisioningData grouperProvisioningData = null;
  
  private GrouperProvisionerOperationSupported grouperProvisionerOperationSupported = new GrouperProvisionerOperationSupported();
  
  public Map<String, Object> getDebugMap() {
    return debugMap;
  }

  /**
   * return the class of the DAO for this provisioner
   */
  protected abstract Class<? extends GrouperProvisionerTargetDaoBase> retrieveTargetDaoClass();
  
  
  /**
   * returns the subclass of Data Access Object for this provisioner
   * @return the DAO
   */
  public GrouperProvisionerTargetDaoBase retrieveTargetDao() {
    if (this.grouperProvisionerTargetDaoBase == null) {
      Class<? extends GrouperProvisionerTargetDaoBase> grouperProvisionerTargetDaoBaseClass = this.retrieveTargetDaoClass();
      this.grouperProvisionerTargetDaoBase = GrouperUtil.newInstance(grouperProvisionerTargetDaoBaseClass);
      this.grouperProvisionerTargetDaoBase.setGrouperProvisioner(this);
    }
    return this.grouperProvisionerTargetDaoBase;
    
  }
  
  /**
   * returns the Grouper Data access Object
   * @return the DAO
   */
  public GrouperProvisionerGrouperDao retrieveGrouperDao() {
    if (this.grouperProvisionerGrouperDao == null) {
      Class<? extends GrouperProvisionerGrouperDao> grouperProvisionerGrouperDaoClass = this.retrieveGrouperDaoClass();
      this.grouperProvisionerGrouperDao = GrouperUtil.newInstance(grouperProvisionerGrouperDaoClass);
      this.grouperProvisionerGrouperDao.setGrouperProvisioner(this);
    }
    return this.grouperProvisionerGrouperDao;
    
  }
  
  protected Class<? extends GrouperProvisionerGrouperDao> retrieveGrouperDaoClass() {
    return GrouperProvisionerGrouperDao.class;
  }
  
  private GrouperProvisioningConfigurationBase grouperProvisioningConfigurationBase = null;
  
  /**
   * return the class of the DAO for this provisioner
   */
  protected abstract Class<? extends GrouperProvisioningConfigurationBase> retrieveProvisioningConfigurationClass();
  
  /**
   * returns the subclass of Data Access Object for this provisioner
   * @return the DAO
   */
  public GrouperProvisioningConfigurationBase retrieveProvisioningConfiguration() {
    if (this.grouperProvisioningConfigurationBase == null) {
      Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationBaseClass = this.retrieveProvisioningConfigurationClass();
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
  
  
  private GrouperProvisioningLogic grouperProvisioningLogic = null;
  
  /**
   * return the class of the provisioning logic
   */
  protected Class<GrouperProvisioningLogic> grouperProvisioningLogicClass() {
    return GrouperProvisioningLogic.class;
  }
  
  /**
   * return the instance of the provisioning logic
   * @return the logic
   */
  public GrouperProvisioningLogic retrieveGrouperProvisioningLogic() {
    if (this.grouperProvisioningLogic == null) {
      Class<GrouperProvisioningLogic> grouperProvisioningLogicClass = this.grouperProvisioningLogicClass();
      this.grouperProvisioningLogic = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningLogic.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningLogic;
    
  }
  
  private GrouperProvisioningTranslatorBase grouperProvisioningTranslatorBase = null;

  /**
   * returns the instance of the translator
   * @return the translator
   */
  public GrouperProvisioningTranslatorBase retrieveTranslator() {
    if (this.grouperProvisioningTranslatorBase == null) {
      Class<? extends GrouperProvisioningTranslatorBase> grouperProvisioningTranslatorBaseClass = this.retrieveTranslatorClass();
      this.grouperProvisioningTranslatorBase = GrouperUtil.newInstance(grouperProvisioningTranslatorBaseClass);
      this.grouperProvisioningTranslatorBase.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningTranslatorBase;
    
  }
  
  /**
   * @return the class of the translator for this provisioner (optional)
   */
  protected Class<? extends GrouperProvisioningTranslatorBase> retrieveTranslatorClass() {
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
  private GrouperProvisioningOutput grouperProvisioningOutput;
  
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
   * 
   */
  private GrouperProvisioningType grouperProvisioningType;
  private String configId;
  
  
  public GrouperProvisioningType getGrouperProvisioningType() {
    return grouperProvisioningType;
  }

  
  public void setGrouperProvisioningType(GrouperProvisioningType grouperProvisioningType) {
    this.grouperProvisioningType = grouperProvisioningType;
  }

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

  

  /**
   * provision
   * @param grouperProvisioningType
   * @return the output
   */
  public GrouperProvisioningOutput provision(GrouperProvisioningType grouperProvisioningType1) {
    
    if (this.done) {
      throw new RuntimeException("Dont re-use instances of this class: " + GcTableSync.class.getName());
    }

    this.grouperProvisioningType = grouperProvisioningType1;
    
    this.millisWhenSyncStarted = System.currentTimeMillis();
    
    this.grouperProvisioningOutput = new GrouperProvisioningOutput();
    
    this.retrieveProvisioningConfiguration().configureProvisioner();
    
    this.debugMap = new LinkedHashMap<String, Object>();
    
    long now = System.nanoTime();
    
    GcDbAccess.threadLocalQueryCountReset();
    
    try {

      debugMap.put("finalLog", false);
      
      debugMap.put("state", "init");

      this.gcGrouperSyncHeartbeat.setGcGrouperSyncJob(this.gcGrouperSyncJob);
      this.gcGrouperSyncHeartbeat.setFullSync(this.grouperProvisioningType.isFullSync());
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
      
      if (GrouperClientUtils.isBlank(gcGrouperSyncLog.getStatus())) {
        gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.SUCCESS);
      }

      return this.grouperProvisioningOutput;
    } catch (RuntimeException re) {
      gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
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
      
      int durationMillis = (int)((System.nanoTime()-now)/1000000);
      debugMap.put("tookMillis", durationMillis);
      debugMap.put("took", DurationFormatUtils.formatDurationHMS(durationMillis));
      
      String debugString = GrouperClientUtils.mapToString(debugMap);

      try {
        if (gcGrouperSyncLog != null) {
          gcGrouperSyncLog.setDescription(debugString);
          gcGrouperSyncLog.setJobTookMillis(durationMillis);
          gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLog);
        }
      } catch (RuntimeException re3) {
        debugMap.put("exception3", GrouperClientUtils.getFullStackTrace(re3));
        debugString = GrouperClientUtils.mapToString(debugMap);
      }
      
      if (this.retrieveProvisioningConfiguration().isDebugLog()) {
        GrouperProvisioningLog.debugLog(debugString);
      }
      
      // already set total
      //gcTableSyncOutput.setTotal();
      this.grouperProvisioningOutput.setMessage(debugString);

      // this isnt good
      if (debugMap.containsKey("exception") || debugMap.containsKey("exception2") || debugMap.containsKey("exception3")) {
        throw new RuntimeException(debugString);
      }
    }
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

  
  public GrouperProvisioningData getGrouperProvisioningData() {
    return grouperProvisioningData;
  }

  
  public void setGrouperProvisioningData(GrouperProvisioningData grouperProvisioningData) {
    this.grouperProvisioningData = grouperProvisioningData;
  }

  
  public GrouperProvisionerOperationSupported getGrouperProvisionerOperationSupported() {
    return grouperProvisionerOperationSupported;
  }

  
  public void setGrouperProvisionerOperationSupported(
      GrouperProvisionerOperationSupported grouperProvisionerOperationSupported) {
    this.grouperProvisionerOperationSupported = grouperProvisionerOperationSupported;
  }

  private ProvisioningSyncResult provisioningSyncResult = null;

  
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
      Class<GrouperProvisioningCompare> grouperProvisioningLogicClass = this.grouperProvisioningCompareClass();
      this.grouperProvisioningCompare = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningCompare.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningCompare;
    
  }

  private GrouperProvisioningCompare grouperProvisioningCompare;
  
  protected Class<GrouperProvisioningCompare> grouperProvisioningCompareClass() {
    return GrouperProvisioningCompare.class;
  }
  
  /**
   * return the instance of the indexing logic
   * @return the logic
   */
  public GrouperProvisioningTargetIdIndex retrieveGrouperProvisioningTargetIdIndex() {
    if (this.grouperProvisioningTargetIdIndex == null) {
      Class<GrouperProvisioningTargetIdIndex> grouperProvisioningLogicClass = this.grouperProvisioningTargetIdIndexClass();
      this.grouperProvisioningTargetIdIndex = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningTargetIdIndex.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningTargetIdIndex;
    
  }

  private GrouperProvisioningTargetIdIndex grouperProvisioningTargetIdIndex;
  
  protected Class<GrouperProvisioningTargetIdIndex> grouperProvisioningTargetIdIndexClass() {
    return GrouperProvisioningTargetIdIndex.class;
  }

  /**
   * returns the Grouper Sync Data access Object
   * @return the DAO
   */
  public GrouperProvisionerGrouperSyncDao retrieveGrouperSyncDao() {
    if (this.grouperProvisionerGrouperSyncDao == null) {
      Class<? extends GrouperProvisionerGrouperSyncDao> grouperProvisionerGrouperSyncDaoClass = this.retrieveGrouperSyncDaoClass();
      this.grouperProvisionerGrouperSyncDao = GrouperUtil.newInstance(grouperProvisionerGrouperSyncDaoClass);
      this.grouperProvisionerGrouperSyncDao.setGrouperProvisioner(this);
    }
    return this.grouperProvisionerGrouperSyncDao;
    
  }

  protected Class<? extends GrouperProvisionerGrouperSyncDao> retrieveGrouperSyncDaoClass() {
    return GrouperProvisionerGrouperSyncDao.class;
  }

  
  
}
