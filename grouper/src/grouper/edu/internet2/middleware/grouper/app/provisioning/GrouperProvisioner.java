package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.amazonaws.endpointdiscovery.DaemonThreadFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.OtherJobException;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoAdapter;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.tableSync.GrouperProvisioningSyncIntegration;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncResult;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperFailsafe;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * 
 * @author mchyzer
 *
 */
public abstract class GrouperProvisioner {
  
  private ProvisioningStateGlobal provisioningStateGlobal = new ProvisioningStateGlobal();
  
  
  public GrouperProvisioner() {
    this.provisioningStateGlobal.setGrouperProvisioner(this);
  }
  
  public ProvisioningStateGlobal getProvisioningStateGlobal() {
    return provisioningStateGlobal;
  }
  
  
  public void setProvisioningStateGlobal(ProvisioningStateGlobal provisioningStateGlobal) {
    this.provisioningStateGlobal = provisioningStateGlobal;
  }

  /**
   * cache the thread pool
   */
  public static Map<String, ExecutorService> executorConfigIdToThreadPool = new HashMap<String, ExecutorService>();
  
  /**
   * if running threads, this is the pool.  if null then dont use threads
   */
  private ExecutorService executorService = null;

  /**
   * cache if has executor service
   */
  private boolean executorServiceInitted = false;

  /**
   * if running threads, this is the pool.  if null then dont use threads
   * @return executor service
   */
  public ExecutorService retrieveExecutorService() {
    if (!this.executorServiceInitted) {

      this.executorServiceInitted = true;

      ThreadPoolExecutor cachedExecutorService = (ThreadPoolExecutor)executorConfigIdToThreadPool.get(this.getConfigId());

      int threadPoolSize = this.retrieveGrouperProvisioningConfiguration().getThreadPoolSize();

      boolean changeExecutorService = false;

      // if this is the first time...
      if (cachedExecutorService == null) {
        if (threadPoolSize > 1) {
          this.getDebugMap().put("initThreadPool", true);
          changeExecutorService = true;
        }
      } else {
        if (cachedExecutorService.getMaximumPoolSize() != threadPoolSize) {
          changeExecutorService = true;
        }
      }

      if (changeExecutorService) {
        
        // shut down old
        if (cachedExecutorService != null) {
          this.getDebugMap().put("shutdownCachedThreadPool", true);
          try {
            cachedExecutorService.shutdown();
          } catch (Exception e) {
            LOG.error("Error shutting down executor service", e);
          }
        }

        if (threadPoolSize > 1) {
          this.getDebugMap().put("createThreadPool", true);
          this.executorService = Executors.newFixedThreadPool(
              threadPoolSize, new DaemonThreadFactory());
        } else {
          this.getDebugMap().put("noThreadPool", true);
          this.executorService = null;
        }
        executorConfigIdToThreadPool.put(this.getConfigId(), this.executorService);
      } else {
        this.executorService = cachedExecutorService;
      }
      
    }
    return this.executorService;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioner.class);

  /**
   * job name from full or incremental
   */
  private String jobName;
  
  /**
   * job name from full or incremental
   * @return
   */
  public String getJobName() {
    return jobName;
  }
  
  /**
   * job name from full or incremental
   * @param jobName1
   */
  public void setJobName(String jobName1) {
    this.jobName = jobName1;
  }

  /**
   * 
   */
  private Set<String> jobNames = null;

  /**
   * get job names configured for this provisioner
   * @return the job name
   */
  public Set<String> getJobNames() {

    if (this.jobNames == null) {
      Set<String> tempJobNames = new LinkedHashSet<String>();
      {
        String fullSyncRegex = "^otherJob\\.([^.]+)\\.provisionerConfigId$";
        Pattern fullSyncPattern = Pattern.compile(fullSyncRegex);
        Map<String, String> fullSyncProvisioningJobs = GrouperLoaderConfig.retrieveConfig().propertiesMap(fullSyncPattern);
        if (GrouperUtil.length(fullSyncProvisioningJobs) > 0) {
          for (Entry<String, String> entry : fullSyncProvisioningJobs.entrySet()) {
            if (StringUtils.equals(this.configId, entry.getValue())) {
              Matcher matcher = fullSyncPattern.matcher(entry.getKey());
              matcher.matches();
              String theConfigId = matcher.group(1);
              tempJobNames.add("OTHER_JOB_" + theConfigId);
            }
          }
        }
      }
      {
        String incrementalSyncRegex = "^changeLog\\.consumer\\.([^.]+)\\.provisionerConfigId$";
        Pattern incrementalSyncPattern = Pattern.compile(incrementalSyncRegex);
        Map<String, String> incrementalSyncProvisioningJobs = GrouperLoaderConfig.retrieveConfig().propertiesMap(incrementalSyncPattern);
        if (GrouperUtil.length(incrementalSyncProvisioningJobs) > 0) {
          for (Entry<String, String> entry : incrementalSyncProvisioningJobs.entrySet()) {
            if (StringUtils.equals(this.configId, entry.getValue())) {
              Matcher matcher = incrementalSyncPattern.matcher(entry.getKey());
              matcher.matches();
              String theConfigId = matcher.group(1);
              tempJobNames.add("CHANGE_LOG_consumer_" + theConfigId);
            }
          }
        }
      }
      
      this.jobNames = tempJobNames;
    }
    
    
    return this.jobNames;
  }
  
  /**
   */
  private ProvisioningConfiguration provisioningConfiguration = null;

  /**
   * this is the controller that makes the editing screen work, this is not the provisioning configuration class: retrieveGrouperProvisioningConfiguration()
   * @return provisioner configuration
   */
  public ProvisioningConfiguration getControllerForProvisioningConfiguration() {
    if (this.provisioningConfiguration == null) {
      this.provisioningConfiguration = ProvisioningConfiguration.retrieveConfigurationByConfigSuffix(this.getClass().getName());
    
      this.provisioningConfiguration.setConfigId(this.getConfigId());
    }
    return this.provisioningConfiguration;
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
    if (!(this.retrieveGrouperProvisioningDataIncrementalInput().getClass().equals(GrouperProvisioningDataIncrementalInput.class))) {
      result.append(", DataIncrementalInput: ").append(this.retrieveGrouperProvisioningDataIncrementalInput().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningDataIndex().getClass().equals(GrouperProvisioningDataIndex.class))) {
      result.append(", DataIndex: ").append(this.retrieveGrouperProvisioningDataIndex().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningDiagnosticsContainer().getClass().equals(GrouperProvisioningDiagnosticsContainer.class))) {
      result.append(", DiagnosticsContainer: ").append(this.retrieveGrouperProvisioningDiagnosticsContainer().getClass().getName());
    }
    if (!(this.retrieveGrouperProvisioningFailsafe().getClass().equals(GrouperProvisioningFailsafe.class))) {
      result.append(", Failsafe: ").append(this.retrieveGrouperProvisioningFailsafe().getClass().getName());
    }
    if (!GrouperProvisioningGrouperDao.class.equals(this.grouperDaoClass())) {
      result.append(", GrouperDao: ").append(this.grouperDaoClass().getName());
    }
    if (!GrouperProvisioningGrouperSyncDao.class.equals(this.grouperSyncDaoClass())) {
      result.append(", GrouperSyncDao: ").append(this.grouperSyncDaoClass().getName());
    }
    if (!GrouperProvisioningLinkLogic.class.equals(this.grouperProvisioningLinkLogicClass())) {
      result.append(", LinkLogic: ").append(this.grouperProvisioningLinkLogicClass().getName());
    }
    if (!GrouperProvisioningLogic.class.equals(this.grouperProvisioningLogicClass())) {
      result.append(", Logic: ").append(this.grouperProvisioningLogicClass().getName());
    }
    if (!GrouperProvisioningLogic.class.equals(this.grouperProvisioningLogicIncrementalClass())) {
      result.append(", LogicIncremental: ").append(this.grouperProvisioningLogicIncrementalClass().getName());
    }
    if (!GrouperProvisioningMatchingIdIndex.class.equals(this.grouperProvisioningMatchingIdIndexClass())) {
      result.append(", MatchingIdIndex: ").append(this.grouperProvisioningMatchingIdIndexClass().getName());
    }
    if (!GrouperProvisioningMatchingIdIndex.class.equals(this.grouperProvisioningObjectMetadataClass())) {
      result.append(", ObjectMetadata: ").append(this.grouperProvisioningObjectMetadataClass().getName());
    }
    if (!GrouperProvisioningTranslator.class.equals(this.grouperTranslatorClass())) {
      result.append(", Translator: ").append(this.grouperTranslatorClass().getName());
    }
    if (!GrouperProvisioningValidation.class.equals(this.grouperProvisioningValidationClass())) {
      result.append(", Validation: ").append(this.grouperProvisioningValidationClass().getName());
    }
    
    return result.toString();
  }
  
  private GrouperProvisionerTargetDaoAdapter grouperProvisioningTargetDaoAdapter = null;
  
  private GrouperProvisioningGrouperDao grouperProvisioningGrouperDao = null;

  private GrouperProvisioningGrouperSyncDao grouperProvisioningGrouperSyncDao = null;

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

  public GrouperProvisioningObjectLog retrieveGrouperProvisioningObjectLog() {
    if (this.grouperProvisioningObjectLog == null) {
      this.grouperProvisioningObjectLog = new GrouperProvisioningObjectLog(this);
    }
    return grouperProvisioningObjectLog;
  }

  private GrouperProvisioningData grouperProvisioningData;

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
  public GrouperProvisionerTargetDaoAdapter retrieveGrouperProvisioningTargetDaoAdapter() {
    if (this.grouperProvisioningTargetDaoAdapter == null) {
      Class<? extends GrouperProvisionerTargetDaoBase> grouperProvisionerTargetDaoBaseClass = this.grouperTargetDaoClass();
      GrouperProvisionerTargetDaoBase grouperProvisionerTargetDaoBase = GrouperUtil.newInstance(grouperProvisionerTargetDaoBaseClass);
      grouperProvisionerTargetDaoBase.setGrouperProvisioner(this);
      this.grouperProvisioningTargetDaoAdapter = new GrouperProvisionerTargetDaoAdapter(this, grouperProvisionerTargetDaoBase);
    }
    return this.grouperProvisioningTargetDaoAdapter;
    
  }
  
  /**
   * returns the Grouper Data access Object
   * @return the DAO
   */
  public GrouperProvisioningGrouperDao retrieveGrouperDao() {
    if (this.grouperProvisioningGrouperDao == null) {
      Class<? extends GrouperProvisioningGrouperDao> grouperProvisionerGrouperDaoClass = this.grouperDaoClass();
      this.grouperProvisioningGrouperDao = GrouperUtil.newInstance(grouperProvisionerGrouperDaoClass);
      this.grouperProvisioningGrouperDao.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningGrouperDao;
    
  }
  
  protected Class<? extends GrouperProvisioningGrouperDao> grouperDaoClass() {
    return GrouperProvisioningGrouperDao.class;
  }
  
  private GrouperProvisioningConfiguration grouperProvisioningConfiguration = null;

  private GrouperProvisioningLinkLogic grouperProvisioningLinkLogic = null;

  /**
   * return the class of the DAO for this provisioner
   */
  protected abstract Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass();
  
  /**
   * 
   */
  private GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = null;
  
  /**
   * @return the diagnostics
   */
  public GrouperProvisioningDiagnosticsContainer retrieveGrouperProvisioningDiagnosticsContainer() {
    
    if (this.grouperProvisioningDiagnosticsContainer == null) {
      Class<? extends GrouperProvisioningDiagnosticsContainer> grouperProvisioningDiagnosticsContainerClass = this.grouperProvisioningDiagnosticsContainerClass();
      this.grouperProvisioningDiagnosticsContainer = GrouperUtil.newInstance(grouperProvisioningDiagnosticsContainerClass);
      this.grouperProvisioningDiagnosticsContainer.setGrouperProvisioner(this);
    }
    
    return this.grouperProvisioningDiagnosticsContainer;
  }
  
  /**
   * return the class of the attribute manipulation
   */
  protected Class<? extends GrouperProvisioningDiagnosticsContainer> grouperProvisioningDiagnosticsContainerClass() {
    return GrouperProvisioningDiagnosticsContainer.class;
  }
  
  /**
   * returns the subclass of Data Access Object for this provisioner
   * @return the DAO
   */
  public GrouperProvisioningConfiguration retrieveGrouperProvisioningConfiguration() {
    if (this.grouperProvisioningConfiguration == null) {
      Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass = this.grouperProvisioningConfigurationClass();
      this.grouperProvisioningConfiguration = GrouperUtil.newInstance(grouperProvisioningConfigurationClass);
      this.grouperProvisioningConfiguration.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningConfiguration;
    
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
  
  

  private GrouperProvisioningSyncIntegration grouperProvisioningSyncIntegration = null;

  /**
   * return the class of the attribute manipulation
   */
  protected Class<? extends GrouperProvisioningSyncIntegration> grouperProvisioningSyncIntegrationClass() {
    return GrouperProvisioningSyncIntegration.class;
  }
  
  /**
   * return the instance of the attribute manipulation
   * @return the logic
   */
  public GrouperProvisioningSyncIntegration retrieveGrouperProvisioningSyncIntegration() {
    if (this.grouperProvisioningSyncIntegration == null) {
      Class<? extends GrouperProvisioningSyncIntegration> grouperProvisioningLogicClass = this.grouperProvisioningSyncIntegrationClass();
      this.grouperProvisioningSyncIntegration = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningSyncIntegration.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningSyncIntegration;
    
  }

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
  
  /**
   * return the instance of the provisioning log
   * @return the logic
   */
  public GrouperProvisioningLog retrieveGrouperProvisioningLog() {
    if (this.grouperProvisioningLog == null) {
      Class<? extends GrouperProvisioningLog> grouperProvisioningLogClass = this.grouperProvisioningLogClass();
      this.grouperProvisioningLog = GrouperUtil.newInstance(grouperProvisioningLogClass);
      this.grouperProvisioningLog.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningLog;
    
  }


  private GrouperProvisioningLog grouperProvisioningLog = null;
  
  /**
   * return the class of the provisioning logic
   */
  protected Class<? extends GrouperProvisioningLog> grouperProvisioningLogClass() {
    return GrouperProvisioningLog.class;
  }
  

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
  
  private GrouperProvisioningTranslator grouperProvisioningTranslator = null;

  /**
   * returns the instance of the translator
   * @return the translator
   */
  public GrouperProvisioningTranslator retrieveGrouperProvisioningTranslator() {
    if (this.grouperProvisioningTranslator == null) {
      Class<? extends GrouperProvisioningTranslator> grouperProvisioningTranslatorBaseClass = this.grouperTranslatorClass();
      this.grouperProvisioningTranslator = GrouperUtil.newInstance(grouperProvisioningTranslatorBaseClass);
      this.grouperProvisioningTranslator.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningTranslator;
    
  }
  
  /**
   * @return the class of the translator for this provisioner (optional)
   */
  protected Class<? extends GrouperProvisioningTranslator> grouperTranslatorClass() {
    return GrouperProvisioningTranslator.class;
  }
  
  /**
   * last provisioner for junit
   */
  private static GrouperProvisioner internalLastProvisioner;
  
  /**
   * last provisioner for junit
   * @return provisioner
   */
  public static GrouperProvisioner retrieveInternalLastProvisioner() {
    return internalLastProvisioner;
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
    GrouperContext grouperContext = GrouperContext.retrieveDefaultContext();
    
    // dont waste memory
    if (grouperContext != null && grouperContext.getGrouperEngine() == GrouperEngineBuiltin.JUNIT) {
      internalLastProvisioner = provisioner;
    }
    return provisioner;
    
  }
  
  /**
   * dont re-use instances
   */
  private boolean done = false;

  /**
   * debug map for this provisioner
   */
  private Map<String, Object> debugMap = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
  
  private static ThreadLocal<GrouperProvisioner> threadLocalGrouperProvisioner = new InheritableThreadLocal<>();
  
  public static GrouperProvisioner retrieveCurrentGrouperProvisioner() {
    return threadLocalGrouperProvisioner.get();
  }
  
  public static void assignCurrentGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    threadLocalGrouperProvisioner.set(grouperProvisioner);
  }
  
  public static void removeCurrentGrouperProvisioner() {
    threadLocalGrouperProvisioner.remove();
  }
  
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
  private GrouperProvisioningOutput grouperProvisioningOutput = null;
  
  /**
   * provisioning output
   * @return output
   */
  public GrouperProvisioningOutput retrieveGrouperProvisioningOutput() {
    if (this.grouperProvisioningOutput == null) {
      this.grouperProvisioningOutput = new GrouperProvisioningOutput();
      this.grouperProvisioningOutput.setGrouperProvisioner(this);
    }
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
      String theMessage = debugString;
      StringBuilder objectLog = this.retrieveGrouperProvisioningObjectLog().getObjectLog();
      if (objectLog.length() > 0) {
        theMessage = objectLog + "\n\n" + debugString;
      }

      grouperProvisioningOutput.setMessage(theMessage);
      GrouperProvisioningLog.debugLog(debugString);
      this.lastLog = System.currentTimeMillis();

    }
    
  }

  private String configId;

  private long startedNanos;

  private boolean initialized = false;

  public GrouperProvisioner initialize(GrouperProvisioningType grouperProvisioningType1) {

    if (!this.initialized) {

      this.debugMap = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
      
      threadLocalGrouperProvisioner.set(this);

      GcDbAccess.threadLocalQueryCountReset();

      this.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(grouperProvisioningType1);

      this.retrieveGrouperProvisioningConfiguration().configureProvisioner();

      // let the target dao tell the framework what it can do
      this.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().registerGrouperProvisionerDaoCapabilities(
          this.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities()
          );

      this.retrieveGrouperProvisioningObjectMetadata().initBuiltInMetadata();
      this.retrieveGrouperProvisioningObjectMetadata().indexBuiltInMetadata();

      this.retrieveGrouperProvisioningConfiguration().configureAfterMetadata();
      
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

      debugMap.put("runId", this.getInstanceId());

      debugMap.put("state", "init");
      this.initialize(grouperProvisioningType1);
      
      this.getGcGrouperSyncJob().waitForRelatedJobsToFinishThenRun(this.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isFullSync());
      
      if (this.getGcGrouperSyncLog() == null) {
        this.setGcGrouperSyncLog(this.getGcGrouperSync().getGcGrouperSyncJobDao().jobCreateLog(this.getGcGrouperSyncJob()));
      }
      
      this.getGcGrouperSyncLog().setSyncTimestamp(new Timestamp(System.currentTimeMillis()));

      this.gcGrouperSyncHeartbeat.setGcGrouperSyncJob(this.gcGrouperSyncJob);
      this.gcGrouperSyncHeartbeat.setFullSync(this.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isFullSync());
      this.gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {

        @Override
        public void run() {

          logPeriodically(debugMap, GrouperProvisioner.this.retrieveGrouperProvisioningOutput());
          
        }
        
      });
      if (!this.gcGrouperSyncHeartbeat.isStarted()) {
        this.gcGrouperSyncHeartbeat.runHeartbeatThread();
      }

      debugMap.put("provisionerClass", this.getClass().getSimpleName());
      debugMap.put("configId", this.getConfigId());
      debugMap.put("provisioningType", grouperProvisioningType1);
    
      this.retrieveGrouperProvisioningLogic().provision();
      
      // assign a success, and if this is a full, then remove failures from incremental if they are there
      if (gcGrouperSyncLog != null && gcGrouperSyncLog.getStatus() != null && !gcGrouperSyncLog.getStatus().isError()) {
        if (!StringUtils.isBlank(this.getJobName())) {
          GrouperFailsafe.assignSuccess(this.getJobName());
          if (this.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isFullSync()) {
            for (String theJobName : GrouperUtil.nonNull(this.getJobNames())) {
              if (theJobName.startsWith("CHANGE_LOG_consumer_")) {
                GrouperFailsafe.removeFailure(theJobName);
              }
            }
          }

        }
      }
          
      return this.retrieveGrouperProvisioningOutput();
    } catch (RuntimeException re) {
      String fullStackTrace = GrouperClientUtils.getFullStackTrace(re);
      LOG.error(this.retrieveGrouperProvisioningLog().prefixLogLinesWithInstanceId(
          "Error, " + fullStackTrace));
      if (gcGrouperSyncLog != null) {
        if (gcGrouperSyncLog.getStatus() == null || !gcGrouperSyncLog.getStatus().isError()) {
          gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
        }
      }
      if (debugMap != null) {
        debugMap.put("exception", fullStackTrace);
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
        String fullStackTrace = GrouperClientUtils.getFullStackTrace(re2);
        LOG.error(this.retrieveGrouperProvisioningLog().prefixLogLinesWithInstanceId(
            "Error2, " + fullStackTrace));
        if (this.gcGrouperSyncLog != null) {
          if (gcGrouperSyncLog.getStatus() == null || !gcGrouperSyncLog.getStatus().isError()) {
            this.gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
          }
        }
        debugMap.put("exception2", fullStackTrace);
      }
    }

    // TODO sum with dao, hibernate, and client
    this.retrieveGrouperProvisioningOutput().setQueryCount(GcDbAccess.threadLocalQueryCountRetrieve());
    debugMap.put("queryCount", this.retrieveGrouperProvisioningOutput().getQueryCount());
    
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
      String fullStackTrace = GrouperClientUtils.getFullStackTrace(re3);
      LOG.error(this.retrieveGrouperProvisioningLog().prefixLogLinesWithInstanceId(
          "Error3, " + fullStackTrace));

      debugMap.put("exception3", fullStackTrace);
      debugString = GrouperClientUtils.mapToString(debugMap);
    }
    
    if (this.retrieveGrouperProvisioningConfiguration().isDebugLog()) {
      GrouperProvisioningLog.debugLog(debugString);
    }
    
    this.retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.end);

    // already set total
    //gcTableSyncOutput.setTotal();
    String theMessage = debugString;
    StringBuilder objectLog = this.retrieveGrouperProvisioningObjectLog().getObjectLog();
    if (objectLog.length() > 0) {
      theMessage = objectLog + "\n\n" + debugString;
    }
    
    this.retrieveGrouperProvisioningOutput().setMessage(theMessage);
    
    threadLocalGrouperProvisioner.remove();

    // this isnt good
    if (debugMap.containsKey("exception") || debugMap.containsKey("exception2") || debugMap.containsKey("exception3")) {
      if (gcGrouperSyncLog != null && gcGrouperSyncLog.getStatus() == GcGrouperSyncLogState.ERROR_FAILSAFE) {
        throw new OtherJobException(GrouperLoaderStatus.ERROR_FAILSAFE, theMessage);
      }
      throw new RuntimeException(theMessage);
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

  
  public GrouperProvisioningData retrieveGrouperProvisioningData() {
    if (this.grouperProvisioningData == null) {
      this.grouperProvisioningData = new GrouperProvisioningData();
      this.grouperProvisioningData.setGrouperProvisioner(this);
    }
    return grouperProvisioningData;
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
  
  protected Class<? extends GrouperProvisioningBehavior> grouperProvisioningBehaviorClass() {
    return GrouperProvisioningBehavior.class;
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
  public GrouperProvisioningGrouperSyncDao retrieveGrouperProvisioningSyncDao() {
    if (this.grouperProvisioningGrouperSyncDao == null) {
      Class<? extends GrouperProvisioningGrouperSyncDao> grouperProvisionerGrouperSyncDaoClass = this.grouperSyncDaoClass();
      this.grouperProvisioningGrouperSyncDao = GrouperUtil.newInstance(grouperProvisionerGrouperSyncDaoClass);
      this.grouperProvisioningGrouperSyncDao.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningGrouperSyncDao;
    
  }

  protected Class<? extends GrouperProvisioningGrouperSyncDao> grouperSyncDaoClass() {
    return GrouperProvisioningGrouperSyncDao.class;
  }

  private GrouperProvisioningBehavior grouperProvisioningBehavior = new GrouperProvisioningBehavior(this);

  private GrouperProvisioningFailsafe grouperProvisioningFailsafe;

  private GrouperProvisioningLogCommands grouperProvisioningLogCommands;


  
  public GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior() {
    if (this.grouperProvisioningBehavior == null) {
      Class<? extends GrouperProvisioningBehavior> grouperProvisioningLogicClass = this.grouperProvisioningBehaviorClass();
      this.grouperProvisioningBehavior = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningBehavior.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningBehavior;

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
    // retrieve all grouper sync data and put in GrouperProvisioningDataSync
    this.retrieveGrouperProvisioningSyncDao().retrieveSyncDataFull();

    Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningFolderAttributes = this.retrieveGrouperDao().retrieveAllProvisioningFolderAttributes();
    Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningGroupAttributes = this.retrieveGrouperDao().retrieveAllProvisioningGroupAttributes();
    Set<GrouperProvisioningObjectAttributes> grouperProvisioningObjectAttributesToProcess = new HashSet<GrouperProvisioningObjectAttributes>();
    grouperProvisioningObjectAttributesToProcess.addAll(grouperProvisioningGroupAttributes.values());
    Set<String> policyGroupIds = this.retrieveGrouperDao().retrieveAllProvisioningGroupIdsThatArePolicyGroups();

    Map<String, GrouperProvisioningObjectAttributes> calculatedProvisioningAttributes = GrouperProvisioningService.calculateProvisioningAttributes(this, grouperProvisioningObjectAttributesToProcess, grouperProvisioningFolderAttributes, policyGroupIds);

    ProvisioningSyncResult provisioningSyncResult = new ProvisioningSyncResult();
    this.setProvisioningSyncResult(provisioningSyncResult);
    
    List<GcGrouperSyncGroup> initialGcGrouperSyncGroups = GrouperUtil.nonNull(this.retrieveGrouperProvisioningData().retrieveGcGrouperSyncGroups());
    
    this.retrieveGrouperProvisioningSyncIntegration().fullSyncGroups(calculatedProvisioningAttributes, new HashSet<GcGrouperSyncGroup>(initialGcGrouperSyncGroups));
    
    //Get the attributes from the attributes framework and store those in grouper sync member table
    Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningMemberAttributes = this.retrieveGrouperDao().retrieveProvisioningMemberAttributes(true, null);
    
    List<GcGrouperSyncMember> initialGcGrouperSyncMembers = GrouperUtil.nonNull(this.retrieveGrouperProvisioningData().retrieveGcGrouperSyncMembers());
    
    this.retrieveGrouperProvisioningSyncIntegration().fullSyncMembers(grouperProvisioningMemberAttributes, new HashSet<GcGrouperSyncMember>(initialGcGrouperSyncMembers));
    
    this.getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();

  }

  protected Class<? extends GrouperProvisioningFailsafe> grouperProvisioningFailsafeClass() {
    return GrouperProvisioningFailsafe.class;
  }

  /**
   * return the instance of the failsafe logic
   * @return the logic
   */
  public GrouperProvisioningFailsafe retrieveGrouperProvisioningFailsafe() {
    if (this.grouperProvisioningFailsafe == null) {
      Class<? extends GrouperProvisioningFailsafe> grouperProvisioningLogicClass = this.grouperProvisioningFailsafeClass();
      this.grouperProvisioningFailsafe = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningFailsafe.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningFailsafe;
    
  }
  
  protected Class<? extends GrouperProvisioningLogCommands> grouperProvisioningLogCommandsClass() {
    return GrouperProvisioningLogCommands.class;
  }

  /**
   * return the instance of the LogCommands logic
   * @return the logic
   */
  public GrouperProvisioningLogCommands retrieveGrouperProvisioningLogCommands() {
    if (this.grouperProvisioningLogCommands == null) {
      Class<? extends GrouperProvisioningLogCommands> grouperProvisioningLogicClass = this.grouperProvisioningLogCommandsClass();
      this.grouperProvisioningLogCommands = GrouperUtil.newInstance(grouperProvisioningLogicClass);
      this.grouperProvisioningLogCommands.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningLogCommands;
    
  }
  
  private GrouperProvisioningLoader grouperProvisioningLoader;
  
  protected Class<? extends GrouperProvisioningLoader> grouperProvisioningLoaderClass() {
    return GrouperProvisioningLoader.class;
  }
  
  public GrouperProvisioningLoader retrieveGrouperProvisioningLoader() {
    if (this.grouperProvisioningLoader == null) {
      Class<? extends GrouperProvisioningLoader> grouperProvisioningLoaderClass = this.grouperProvisioningLoaderClass();
      this.grouperProvisioningLoader = GrouperUtil.newInstance(grouperProvisioningLoaderClass);
      this.grouperProvisioningLoader.setGrouperProvisioner(this);
    }
    return this.grouperProvisioningLoader;
  }
}
