package edu.internet2.middleware.grouper.dataField;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderSync;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderSyncType;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

@DisallowConcurrentExecution
public class GrouperDataProviderFullSyncJob extends OtherJobBase {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperDataProviderFullSyncJob.class);
  
  @Override
  public OtherJobOutput run(final OtherJobInput otherJobInput) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        String jobName = otherJobInput.getJobName();
        String daemonName = jobName.substring(GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX.length(), jobName.length());
        String key = "otherJob."+daemonName+".dataProviderConfigId";
        String dataProviderConfigId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(key);
        
        try {
          Map<String, Object> debugMap = loadFull(dataProviderConfigId, otherJobInput.getHib3GrouperLoaderLog());
          otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished successfully running full sync for dataProviderConfigId=" + dataProviderConfigId + "\n" + GrouperUtil.mapToString(debugMap));
        } catch (Exception e) {
          LOG.warn("Error while running full sync for dataProviderConfigId=" + dataProviderConfigId, e);
          otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished running full sync for dataProviderConfigId=" + dataProviderConfigId + " with an error: " + ExceptionUtils.getFullStackTrace(e));
          throw e;
        } finally {
          otherJobInput.getHib3GrouperLoaderLog().store();
        }

        return null;
      }
    });
    
    return null;
  }
  
  /**
   * 
   * @param dataProviderConfigId
   * @param hib3GrouperLoaderLog
   */
  private Map<String, Object> loadFull(String dataProviderConfigId, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {

    final GrouperDataProviderSync grouperDataProviderSync = GrouperDataProviderSync.retrieveDataProviderSync(dataProviderConfigId);
    grouperDataProviderSync.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.setDebugMap(grouperDataProviderSync.getDebugMap());
    grouperDataProviderSync.setGrouperDataEngine(grouperDataEngine);

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("dataProvider_" + dataProviderConfigId);
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.DATA_PROVIDER);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
    
    grouperDataProviderSync.setGcGrouperSync(gcGrouperSync);
    grouperDataProviderSync.setGcGrouperSyncJob(gcGrouperSyncJob);
    
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(true);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(true);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        hib3GrouperLoaderLog.store();
      }
    });
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }
     
    gcGrouperSyncJob.setLastSyncStart(new Timestamp(System.currentTimeMillis()));
    RuntimeException runtimeException = null;

    try {
      grouperDataProviderSync.runSync(GrouperDataProviderSyncType.fullSyncFull);
      
      // set only if success - used by incremental to determine where the next run should start
      gcGrouperSyncJob.setLastSyncTimestamp(gcGrouperSyncJob.getLastSyncStart());
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      grouperDataProviderSync.getDebugMap().put("finalLog", true);

      synchronized (GrouperDataEngine.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          grouperDataProviderSync.getDebugMap().put("exception2", GrouperClientUtils.getFullStackTrace(re2));

          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }
      
      if (runtimeException != null) {
        throw runtimeException;
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(grouperDataProviderSync.getDebugMap()));
      }
    }
    
    return grouperDataProviderSync.getDebugMap();
  }
  
  /**
   * run standalone
   * @param jobName
   */
  public static void runDaemonStandalone(String jobName) {
    
    if (!jobName.startsWith(GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX)) {
      throw new RuntimeException("Unexpected jobName=" + jobName);
    }
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();

    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();

    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperDataProviderFullSyncJob().run(otherJobInput);
  }
}
