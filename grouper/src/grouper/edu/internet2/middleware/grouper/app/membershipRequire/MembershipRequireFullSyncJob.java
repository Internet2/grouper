package edu.internet2.middleware.grouper.app.membershipRequire;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.stem.StemViewPrivilegeFullDaemonLogic;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

@DisallowConcurrentExecution
public class MembershipRequireFullSyncJob extends OtherJobBase {
  
  /**
   * debug map
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
  private static final Log LOG = GrouperUtil.getLog(MembershipRequireFullSyncJob.class);

  @Override
  public OtherJobOutput run(final OtherJobInput otherJobInput) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        final Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
        
        runFullSync(hib3GrouperLoaderLog);

        return null;
      }
    });
    
    return null;
  }

  public static Hib3GrouperLoaderLog internal_mostRecentHib3GrouperLoaderLog;
  
  public void runFullSync(final Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    internal_mostRecentHib3GrouperLoaderLog = hib3GrouperLoaderLog;
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("membershipRequire");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.MEMBERSHIP_REQUIRE);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
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

      List<MembershipRequireConfigBean> membershipRequireConfigBeans = MembershipRequireEngine.membershipRequireConfigBeans();

      this.debugMap.put("configCount", GrouperUtil.length(membershipRequireConfigBeans));

      for (MembershipRequireConfigBean membershipRequireConfigBean : membershipRequireConfigBeans) {
        
        String attributeName = membershipRequireConfigBean.getAttributeName();
        String requireGroupName = membershipRequireConfigBean.getRequireGroupName();
        Set<String> groupNames = MembershipRequireEngine.attributeDefNameNameToGroupNames(attributeName);
        
        for (String groupName : GrouperUtil.nonNull(groupNames)) {
          int removes = MembershipRequireEngine.removeInvalidMembers(groupName, membershipRequireConfigBean, null, MembershipRequireEngineEnum.fullSync);
          hib3GrouperLoaderLog.addDeleteCount(removes);
        }
        
      }

    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      this.debugMap.put("finalLog", true);
      synchronized (StemViewPrivilegeFullDaemonLogic.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          this.debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(this.debugMap));
      }
      
      if (runtimeException != null) {
        throw runtimeException;
      }

    }
  }

}
