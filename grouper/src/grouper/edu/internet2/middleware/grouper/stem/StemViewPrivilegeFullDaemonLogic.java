package edu.internet2.middleware.grouper.stem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

@DisallowConcurrentExecution
public class StemViewPrivilegeFullDaemonLogic extends OtherJobBase {

  public static Map<String, Object> test_debugMapLast;

  /**
   * debug map
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
  private OtherJobInput otherJobInput; 
  

  public StemViewPrivilegeFullDaemonLogic() {
    super();
    otherJobInput = new OtherJobInput();
    otherJobInput.setHib3GrouperLoaderLog(new Hib3GrouperLoaderLog());
  }

  private static final Log LOG = GrouperUtil.getLog(StemViewPrivilegeFullDaemonLogic.class);
  
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    try {
      this.otherJobInput = otherJobInput;
      fullSyncLogic();
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished successfully running stem view privilege full sync daemon. \n "+GrouperUtil.mapToString(debugMap));
    } catch (Exception e) {
      LOG.warn("Error while running stem view privilege full sync daemon", e);
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished running object stem view privilege full sync logic daemon with an error: " + ExceptionUtils.getFullStackTrace(e));
    } finally {
      otherJobInput.getHib3GrouperLoaderLog().store();
    }
    return null;
  }
  
  /**
   * member ids to recalc
   */
  private Set<String> memberIdsToRecalc = new HashSet<String>();
  
  
  /**
   * 
   */
  public void fullSyncLogic() {
    
    test_debugMapLast = debugMap;

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("stemViewPrivileges");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.STEM_VIEW_PRIVILEGES);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(true);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(true);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        
      }
    });
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }
    
    RuntimeException runtimeException = null;
    
    try {
      
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("security.folders.are.viewable.by.all", false)) {
        
        return;
        
      }

      addFlagForPrecompute();
      deleteUnneededEntries();
      retrieveMemberIdsToRecalc();
      recalcStemPrivileges();
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
      
      if (LOG.isDebugEnabled() && GrouperConfig.retrieveConfig().propertyValueBoolean("security.folder.view.privileges.fullDaemon.log", false)) {
        LOG.debug(GrouperUtil.mapToString(this.debugMap));
      }
      
      if (runtimeException != null) {
        throw runtimeException;
      }

    }
    
  }

  /**
   * add stem view need for people in group
   */
  private void addFlagForPrecompute() {
    String groupName = GrouperConfig.retrieveConfig().propertyValueStringRequired("security.folder.view.privileges.precompute.group");
    
    int recalcChangeLogIfNeededInLastSeconds = StemViewPrivilege.recalcChangeLogIfNeededInLastSeconds();
    long recalcChangeLogIfNeededInLastMillis = System.currentTimeMillis() - (recalcChangeLogIfNeededInLastSeconds*1000);

      
    // get people to update
    List<String> memberIds = new GcDbAccess().sql("select member_uuid from grouper_memberships_lw_v gmlv, grouper_last_login gll "
        + "where gmlv.member_id = gll.member_uuid and gmlv.group_name = ? and gmlv.list_name = 'members' "
        + "and gmlv.subject_source != 'g:gsa' and (gll.last_stem_view_need is null or gll.last_stem_view_need < ?)")
      .addBindVar(groupName).addBindVar(recalcChangeLogIfNeededInLastMillis).selectList(String.class);

    // grouperAll to update
    Member everyEntityMember = MemberFinder.internal_findAllMember();

    memberIds.addAll(new GcDbAccess().sql("select member_uuid from grouper_last_login gll "
        + "where gll.member_uuid = ? "
        + "and (gll.last_stem_view_need is null or gll.last_stem_view_need < ?)")
      .addBindVar(everyEntityMember.getId()).addBindVar(recalcChangeLogIfNeededInLastMillis).selectList(String.class));

    StemViewPrivilege.recalculateStemViewPrivilegesLastStemViewNeedUpdate(this.debugMap, memberIds, "preCompute_", this.otherJobInput.getHib3GrouperLoaderLog());
    
    // get people to insert
    memberIds = new GcDbAccess().sql("select gmlv.member_id from grouper_memberships_lw_v gmlv where gmlv.group_name = ? "
        + " and gmlv.list_name = 'members' and gmlv.subject_source != 'g:gsa' "
        + " and not exists (select 1 from grouper_last_login gll where gmlv.member_id = gll.member_uuid)")
      .addBindVar(groupName).selectList(String.class);

    // grouperAll to insert
    memberIds.addAll(new GcDbAccess().sql("select gm.id from grouper_members gm where "
        + " gm.id = ? "
        + " and not exists (select 1 from grouper_last_login gll where gm.id = gll.member_uuid)")
      .addBindVar(everyEntityMember.getId()).selectList(String.class));

    StemViewPrivilege.recalculateStemViewPrivilegesLastStemViewNeedInsert(this.debugMap, memberIds, "preCompute_", this.otherJobInput.getHib3GrouperLoaderLog());

       
  }

  private void recalcStemPrivileges() {
    
    long start = System.nanoTime();
    
    try {

      StemViewPrivilege.recalculateStemViewPrivilegesForUsers(this.memberIdsToRecalc, this.otherJobInput.getHib3GrouperLoaderLog());
    
    } finally {
      this.debugMap.put("recalcStemPrivilegesMs", (System.nanoTime() - start)/1000000);
    }

  }

  private void retrieveMemberIdsToRecalc() {
    long start = System.nanoTime();
    
    try {

      int recalcChangeLogIfNeededInLastSeconds = StemViewPrivilege.recalcChangeLogIfNeededInLastSeconds();

      GcDbAccess gcDbAccess = new GcDbAccess();
      
      //  # 0 means dont do this for anyone (full recalc each time),
      //  # -1 means do this for everyone who has ever checked stem view,
      //  # other negative values are not valid.
      if (recalcChangeLogIfNeededInLastSeconds == 0) {
        return;

      } else if (recalcChangeLogIfNeededInLastSeconds == -1) {
        gcDbAccess.sql("select gll.member_uuid from grouper_last_login gll where gll.last_stem_view_need is not null");
      } else if (recalcChangeLogIfNeededInLastSeconds < -1) {
        throw new RuntimeException("Invalid ifNeededInLastSeconds setting: " + recalcChangeLogIfNeededInLastSeconds);
      } else {
        gcDbAccess.sql("select gll.member_uuid from grouper_last_login gll where gll.last_stem_view_need is not null and gll.last_stem_view_need >= ? ");
        gcDbAccess.addBindVar(System.currentTimeMillis() - (recalcChangeLogIfNeededInLastSeconds*1000));
      }

      List<String> results = gcDbAccess.selectList(String.class);
      this.memberIdsToRecalc.addAll(results);
      this.debugMap.put("memberIdsToRecalcCount", GrouperUtil.length(results));
      
    } finally {
      this.debugMap.put("retrieveMemberIdsToRecalcMs", (System.nanoTime() - start)/1000000);
    }


  }

  private void deleteUnneededEntries() {
    long start = System.nanoTime();
    
    try {

      int recalcChangeLogIfNeededInLastSeconds = StemViewPrivilege.recalcChangeLogIfNeededInLastSeconds();

      GcDbAccess gcDbAccess = new GcDbAccess();
      
      //  # 0 means dont do this for anyone (full recalc each time),
      //  # -1 means do this for everyone who has ever checked stem view,
      //  # other negative values are not valid.
      if (recalcChangeLogIfNeededInLastSeconds == 0) {
        gcDbAccess.sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege gsvp");

      } else if (recalcChangeLogIfNeededInLastSeconds == -1) {
        gcDbAccess.sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege gsvp where not exists "
            + "(select 1 from grouper_last_login gll where gll.member_uuid = gsvp.member_uuid and gll.last_stem_view_need is not null )");
      } else if (recalcChangeLogIfNeededInLastSeconds < -1) {
        throw new RuntimeException("Invalid ifNeededInLastSeconds setting: " + recalcChangeLogIfNeededInLastSeconds);
      } else {
        gcDbAccess.sql("select member_uuid, stem_uuid, object_type from grouper_stem_view_privilege gsvp where not exists "
            + "(select 1 from grouper_last_login gll where gll.member_uuid = gsvp.member_uuid and gll.last_stem_view_need is not null and gll.last_stem_view_need >= ? )");
        gcDbAccess.addBindVar(System.currentTimeMillis() - (recalcChangeLogIfNeededInLastSeconds*1000));
      }

      List<Object[]> results = gcDbAccess.selectList(Object[].class);
      
      this.debugMap.put("stemPrivsToDeleteCount", GrouperUtil.length(results));
      
      if (GrouperUtil.length(results) > 0) {
        
        gcDbAccess = new GcDbAccess().sql("delete from grouper_stem_view_privilege where member_uuid = ? and stem_uuid = ? and object_type = ?");
        
        List<List<Object>> listBindVars = new ArrayList<List<Object>>();

        for (Object[] result : results) {
          listBindVars.add(GrouperUtil.toList(result));
        }
        gcDbAccess.batchBindVars(listBindVars);
        
        int[] resultCounts = gcDbAccess.executeBatchSql();
        
        int rowsDeleted = 0;
        for (int resultCount : resultCounts) {
          rowsDeleted += resultCount;
        }
        this.debugMap.put("stemPrivRowsDeletedCount", rowsDeleted);
        this.getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(rowsDeleted);
      }
      
    } finally {
      this.debugMap.put("deleteUnneededEntriesMs", (System.nanoTime() - start)/1000000);
    }

  }

  public static void main(String[] args) {
    GrouperSession.startRootSession();
    new StemViewPrivilegeFullDaemonLogic().fullSyncLogic();
  }

}

