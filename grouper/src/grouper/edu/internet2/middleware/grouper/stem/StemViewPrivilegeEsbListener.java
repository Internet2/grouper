package edu.internet2.middleware.grouper.stem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.privs.PrivilegeType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class StemViewPrivilegeEsbListener extends EsbListenerBase {

  /**
   * debug map
   */
  private Map<String, Object> debugMap = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
  
  public static void main(String[] args) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_stemViewPrivileges");
  }
  
  private StemViewPrivilegeLogic stemViewPrivilegeLogic = new StemViewPrivilegeLogic();

  /**
   * events to process
   */
  private List<EsbEventContainer> eventsToProcess;

  private static final Log LOG = GrouperUtil.getLog(StemViewPrivilegeEsbListener.class);

  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void disconnect() {
  }

  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(List<EsbEventContainer> esbEventContainers) {

    this.stemViewPrivilegeLogic.setHib3GrouperLoaderLog(this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog());

    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
    incrementalLogic(esbEventContainers);
    
    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
    
    this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog().appendJobMessage("Finished successfully running stem view privilege incremental sync daemon. \n "+GrouperUtil.mapToString(debugMap));
    
    return provisioningSyncConsumerResult;
    
  }

  public static Map<String, Object> test_debugMapLast;
  
  public void incrementalLogic(List<EsbEventContainer> esbEventContainers) {
    
    test_debugMapLast = debugMap;
    this.stemViewPrivilegeLogic.setDebugMap(this.debugMap);
    
    // dont edit the list given by change log
    eventsToProcess = new ArrayList<EsbEventContainer>(GrouperUtil.nonNull(esbEventContainers));
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("stemViewPrivileges");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.STEM_VIEW_PRIVILEGES);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("incremental");
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(false);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(false);
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
      
      // TODO see if we are doing a full refresh on each view...
      int recalcChangeLogIfNeededInLastSeconds = this.stemViewPrivilegeLogic.recalcChangeLogIfNeededInLastSeconds();
      this.debugMap.put("eventCount", GrouperUtil.length(this.eventsToProcess));
      if (recalcChangeLogIfNeededInLastSeconds != 604800) {
        this.debugMap.put("recalcChangeLogIfNeededInLastSeconds", recalcChangeLogIfNeededInLastSeconds);
      }

      if (recalcChangeLogIfNeededInLastSeconds != 0) { 

        removeNonPrivilegeEvents();
        removePeopleNotCalculatingEvents();
        loadStemIds();
        removeRedundantEvents();
        handleMultipleEventsForOneUser();
        handleEvents();

      }      
    } catch (RuntimeException re) {
      runtimeException = re;
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);
      synchronized (StemViewPrivilegeFullDaemonLogic.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }

      if (LOG.isDebugEnabled() && GrouperConfig.retrieveConfig().propertyValueBoolean("security.folder.view.privileges.changeLogConsumer.log", false)) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
      
    }
  }

  private void handleEvents() {
    long start = System.nanoTime();
    
    try {

      if (GrouperConfig.retrieveConfig().propertyValueBoolean("security.folders.are.viewable.by.all", false)) {
        return;
      }
      
      Map<String, Set<String>> stemIdToMemberIdsForAttributeDelete = new HashMap<String, Set<String>>();
      Map<String, Set<String>> stemIdToMemberIdsForAttributeInsert = new HashMap<String, Set<String>>();
      Map<String, Set<String>> stemIdToMemberIdsForStemDelete = new HashMap<String, Set<String>>();
      Map<String, Set<String>> stemIdToMemberIdsForStemInsert = new HashMap<String, Set<String>>();
      Map<String, Set<String>> stemIdToMemberIdsForGroupDelete = new HashMap<String, Set<String>>();
      Map<String, Set<String>> stemIdToMemberIdsForGroupInsert = new HashMap<String, Set<String>>();
      
      int dontKnowWhatToDoForEvent = 0;
      
      // get distinct subjects
      for (EsbEventContainer esbEventContainer : this.eventsToProcess) {
        String stemName = null;
        if (PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())
            || PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = GrouperUtil.parentStemNameFromName(esbEventContainer.getEsbEvent().getOwnerName());
        } else if (PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = esbEventContainer.getEsbEvent().getOwnerName();
        }
        String stemId = stemNameToStemId.get(stemName);
        
        MultiKey sourceIdSubjectId = new MultiKey(esbEventContainer.getEsbEvent().getSourceId(), esbEventContainer.getEsbEvent().getSubjectId());
        String memberId = sourceIdSubjectIdToMemberId.get(sourceIdSubjectId);

        EsbEventType esbEventType = esbEventContainer.getEsbEventType();
        
        Map<String, Set<String>> stemIdToMemberIdsSetToWorkWith = null;
        
        if ((esbEventType == EsbEventType.PRIVILEGE_ADD || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          
          stemIdToMemberIdsSetToWorkWith = stemIdToMemberIdsForGroupInsert;
          
        } else if ((esbEventType == EsbEventType.PRIVILEGE_ADD || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
              && PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
        
          stemIdToMemberIdsSetToWorkWith = stemIdToMemberIdsForStemInsert;
          
        } else if ((esbEventType == EsbEventType.PRIVILEGE_ADD || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {

          stemIdToMemberIdsSetToWorkWith = stemIdToMemberIdsForAttributeInsert;

        } else if ((esbEventType == EsbEventType.PRIVILEGE_DELETE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
              && PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
            
          stemIdToMemberIdsSetToWorkWith = stemIdToMemberIdsForGroupDelete;

        } else if ((esbEventType == EsbEventType.PRIVILEGE_DELETE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          
          stemIdToMemberIdsSetToWorkWith = stemIdToMemberIdsForStemDelete;
          
        } else if ((esbEventType == EsbEventType.PRIVILEGE_DELETE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          
          stemIdToMemberIdsSetToWorkWith = stemIdToMemberIdsForAttributeDelete;

        }
        
        if (stemIdToMemberIdsSetToWorkWith == null) {
          dontKnowWhatToDoForEvent++;
          continue;
        }
        
        Set<String> memberIds = stemIdToMemberIdsSetToWorkWith.get(stemId);
        
        if (memberIds == null) {
          memberIds = new HashSet<String>();
          stemIdToMemberIdsSetToWorkWith.put(stemId, memberIds);
        }

        memberIds.add(memberId);
        
      }
      
      if (dontKnowWhatToDoForEvent > 0) { 
        debugMap.put("dontKnowWhatToDoForEventCount", dontKnowWhatToDoForEvent);
      }
      
      int fixPrivilegesTypeCount = 0;
      
      for (String stemId : stemIdToMemberIdsForAttributeDelete.keySet()) {
        Set<String> memberIds = stemIdToMemberIdsForAttributeDelete.get(stemId);
        if (GrouperUtil.length(memberIds) > 0) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesAttributeDelete(memberIds, GrouperUtil.toSet(stemId));
          fixPrivilegesTypeCount++;
        }
      }
      
      for (String stemId : stemIdToMemberIdsForAttributeInsert.keySet()) {
        Set<String> memberIds = stemIdToMemberIdsForAttributeInsert.get(stemId);
        if (GrouperUtil.length(memberIds) > 0) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesAttributeInsert(memberIds, GrouperUtil.toSet(stemId));
          fixPrivilegesTypeCount++;
        }
      }
      
      for (String stemId : stemIdToMemberIdsForStemDelete.keySet()) {
        Set<String> memberIds = stemIdToMemberIdsForStemDelete.get(stemId);
        if (GrouperUtil.length(memberIds) > 0) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesStemDelete(memberIds, GrouperUtil.toSet(stemId));
          fixPrivilegesTypeCount++;
        }
      }
      
      for (String stemId : stemIdToMemberIdsForStemInsert.keySet()) {
        Set<String> memberIds = stemIdToMemberIdsForStemInsert.get(stemId);
        if (GrouperUtil.length(memberIds) > 0) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesStemInsert(memberIds, GrouperUtil.toSet(stemId));
          fixPrivilegesTypeCount++;
        }
      }
      
      for (String stemId : stemIdToMemberIdsForGroupDelete.keySet()) {
        Set<String> memberIds = stemIdToMemberIdsForGroupDelete.get(stemId);
        if (GrouperUtil.length(memberIds) > 0) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesGroupDelete(memberIds, GrouperUtil.toSet(stemId));
          fixPrivilegesTypeCount++;
        }
      }
      
      for (String stemId : stemIdToMemberIdsForGroupInsert.keySet()) {
        Set<String> memberIds = stemIdToMemberIdsForGroupInsert.get(stemId);
        if (GrouperUtil.length(memberIds) > 0) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesGroupInsert(memberIds, GrouperUtil.toSet(stemId));
          fixPrivilegesTypeCount++;
        }
      }
      
      debugMap.put("fixPrivilegesTypeCount", fixPrivilegesTypeCount);

    } finally {
      this.debugMap.put("handleEventsMs", (System.nanoTime() - start)/1000000);
    }
  }

  private void handleMultipleEventsForOneUser() {
    long start = System.nanoTime();
    
    try {

      Map<String, Set<String>> memberIdToStemIdsForAttributeDelete = new HashMap<String, Set<String>>();
      Map<String, Set<String>> memberIdToStemIdsForAttributeInsert = new HashMap<String, Set<String>>();
      Map<String, Set<String>> memberIdToStemIdsForStemDelete = new HashMap<String, Set<String>>();
      Map<String, Set<String>> memberIdToStemIdsForStemInsert = new HashMap<String, Set<String>>();
      Map<String, Set<String>> memberIdToStemIdsForGroupDelete = new HashMap<String, Set<String>>();
      Map<String, Set<String>> memberIdToStemIdsForGroupInsert = new HashMap<String, Set<String>>();
      
      // get distinct subjects
      for (EsbEventContainer esbEventContainer : this.eventsToProcess) {
        String stemName = null;
        if (PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())
            || PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = GrouperUtil.parentStemNameFromName(esbEventContainer.getEsbEvent().getOwnerName());
        } else if (PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = esbEventContainer.getEsbEvent().getOwnerName();
        }
        String stemId = stemNameToStemId.get(stemName);
        
        MultiKey sourceIdSubjectId = new MultiKey(esbEventContainer.getEsbEvent().getSourceId(), esbEventContainer.getEsbEvent().getSubjectId());
        String memberId = sourceIdSubjectIdToMemberId.get(sourceIdSubjectId);

        EsbEventType esbEventType = esbEventContainer.getEsbEventType();
        
        Map<String, Set<String>> memberIdToStemIdsSetToWorkWith = null;
        
        if ((esbEventType == EsbEventType.PRIVILEGE_ADD || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForGroupInsert;
          
        } else if ((esbEventType == EsbEventType.PRIVILEGE_ADD || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
              && PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
        
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForStemInsert;
          
        } else if ((esbEventType == EsbEventType.PRIVILEGE_ADD || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {

          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForAttributeInsert;

        } else if ((esbEventType == EsbEventType.PRIVILEGE_DELETE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
              && PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
            
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForGroupDelete;

        } else if ((esbEventType == EsbEventType.PRIVILEGE_DELETE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForStemDelete;
          
        } else if ((esbEventType == EsbEventType.PRIVILEGE_DELETE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForAttributeDelete;

        }
        
        if (memberIdToStemIdsSetToWorkWith == null) {
          continue;
        }
        
        Set<String> stemIds = memberIdToStemIdsSetToWorkWith.get(memberId);
        
        if (stemIds == null) {
          stemIds = new HashSet<String>();
          memberIdToStemIdsSetToWorkWith.put(memberId, stemIds);
        }

        stemIds.add(stemId);
        
      }
      
      int fixPrivilegesForUserTypeCount = 0;
      
      for (String memberId : memberIdToStemIdsForAttributeDelete.keySet()) {
        Set<String> stemIds = memberIdToStemIdsForAttributeDelete.get(memberId);
        if (GrouperUtil.length(stemIds) > 5) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesAttributeDelete(GrouperUtil.toSet(memberId), stemIds);
          fixPrivilegesForUserTypeCount++;
        }
      }
      
      for (String memberId : memberIdToStemIdsForAttributeInsert.keySet()) {
        Set<String> stemIds = memberIdToStemIdsForAttributeInsert.get(memberId);
        if (GrouperUtil.length(stemIds) > 5) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesAttributeInsert(GrouperUtil.toSet(memberId), stemIds);
          fixPrivilegesForUserTypeCount++;
        }
      }
      
      for (String memberId : memberIdToStemIdsForStemDelete.keySet()) {
        Set<String> stemIds = memberIdToStemIdsForStemDelete.get(memberId);
        if (GrouperUtil.length(stemIds) > 5) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesStemDelete(GrouperUtil.toSet(memberId), stemIds);
          fixPrivilegesForUserTypeCount++;
        }
      }
      
      for (String memberId : memberIdToStemIdsForStemInsert.keySet()) {
        Set<String> stemIds = memberIdToStemIdsForStemInsert.get(memberId);
        if (GrouperUtil.length(stemIds) > 5) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesStemInsert(GrouperUtil.toSet(memberId), stemIds);
          fixPrivilegesForUserTypeCount++;
        }
      }
      
      for (String memberId : memberIdToStemIdsForGroupDelete.keySet()) {
        Set<String> stemIds = memberIdToStemIdsForGroupDelete.get(memberId);
        if (GrouperUtil.length(stemIds) > 5) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesGroupDelete(GrouperUtil.toSet(memberId), stemIds);
          fixPrivilegesForUserTypeCount++;
        }
      }
      
      for (String memberId : memberIdToStemIdsForGroupInsert.keySet()) {
        Set<String> stemIds = memberIdToStemIdsForGroupInsert.get(memberId);
        if (GrouperUtil.length(stemIds) > 5) {
          this.stemViewPrivilegeLogic.recalculateStemViewPrivilegesGroupInsert(GrouperUtil.toSet(memberId), stemIds);
          fixPrivilegesForUserTypeCount++;
        }
      }

      debugMap.put("fixPrivilegesForUserTypeCount", fixPrivilegesForUserTypeCount);
      
      // lets get the applicable memberIds
      Iterator<EsbEventContainer> iterator = GrouperUtil.nonNull(this.eventsToProcess).iterator();
      
      int eventsWithManyForOneUser = 0;
      
      // get distinct subjects
      while (iterator.hasNext()) {
        EsbEventContainer esbEventContainer = iterator.next();
        String sourceId = esbEventContainer.getEsbEvent().getSourceId();
        String subjectId = esbEventContainer.getEsbEvent().getSubjectId();
        MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
        String memberId = sourceIdSubjectIdToMemberId.get(sourceIdSubjectId);
        GrouperUtil.assertion(memberId != null, "Cant find memberId! " + sourceIdSubjectId);
        
        String stemName = null;
        if (PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())
            || PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = GrouperUtil.parentStemNameFromName(esbEventContainer.getEsbEvent().getOwnerName());
        } else if (PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = esbEventContainer.getEsbEvent().getOwnerName();
        }
        String stemId = stemNameToStemId.get(stemName);
        
        EsbEventType esbEventType = esbEventContainer.getEsbEventType();
        
        Map<String, Set<String>> memberIdToStemIdsSetToWorkWith = null;
        
        if ((esbEventType == EsbEventType.PRIVILEGE_ADD || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForGroupInsert;
          
        } else if ((esbEventType == EsbEventType.PRIVILEGE_ADD || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
              && PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
        
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForStemInsert;
          
        } else if ((esbEventType == EsbEventType.PRIVILEGE_ADD || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {

          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForAttributeInsert;

        } else if ((esbEventType == EsbEventType.PRIVILEGE_DELETE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
              && PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
            
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForGroupDelete;

        } else if ((esbEventType == EsbEventType.PRIVILEGE_DELETE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForStemDelete;
          
        } else if ((esbEventType == EsbEventType.PRIVILEGE_DELETE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) 
            && PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          
          memberIdToStemIdsSetToWorkWith = memberIdToStemIdsForAttributeDelete;

        }

        Set<String> stemIds = memberIdToStemIdsSetToWorkWith.get(memberId);
        if (stemIds != null && stemIds.contains(stemId) && GrouperUtil.length(stemIds) > 5) {
          
          iterator.remove();
          eventsWithManyForOneUser++;
        }
      }
      this.debugMap.put("eventsWithManyForOneUser", eventsWithManyForOneUser);

    } finally {
      this.debugMap.put("memberIdsWithManyToUpdateMs", (System.nanoTime() - start)/1000000);
    }
  }

  private Map<MultiKey, String> sourceIdSubjectIdToMemberId = new HashMap<MultiKey, String>();
  

  private void removePeopleNotCalculatingEvents() {
    
    int recalcChangeLogIfNeededInLastSeconds = this.stemViewPrivilegeLogic.recalcChangeLogIfNeededInLastSeconds();
    
    long start = System.nanoTime();
    
    try {
      // lets get the applicable memberIds
      Iterator<EsbEventContainer> iterator = GrouperUtil.nonNull(this.eventsToProcess).iterator();
      
      Set<MultiKey> distinctSubjectsBySourceIdSubjectIdSet = new HashSet<MultiKey>();
      
      // get distinct subjects
      while (iterator.hasNext()) {
        EsbEventContainer esbEventContainer = iterator.next();
        String sourceId = esbEventContainer.getEsbEvent().getSourceId();
        String subjectId = esbEventContainer.getEsbEvent().getSubjectId();
        MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
        distinctSubjectsBySourceIdSubjectIdSet.add(sourceIdSubjectId);
      }
      this.debugMap.put("subjects", GrouperUtil.length(distinctSubjectsBySourceIdSubjectIdSet));
      
      List<MultiKey> distinctSubjectsBySourceIdSubjectIdList = new ArrayList<MultiKey>(distinctSubjectsBySourceIdSubjectIdSet);
      
      int batchSize = 450;
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(distinctSubjectsBySourceIdSubjectIdList, batchSize);
      
      for (int i=0;i<numberOfBatches;i++) {
        List<MultiKey> sourceIdSubjectIdBatch = GrouperUtil.batchList(distinctSubjectsBySourceIdSubjectIdList, batchSize, i);
        //not sure how this would be possible but...
        if (GrouperUtil.length(sourceIdSubjectIdBatch) == 0) {
          continue;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select gm.id, gm.subject_source, gm.subject_id from grouper_last_login gll, grouper_members gm where gll.member_uuid = gm.id ");
        GcDbAccess gcDbAccess = new GcDbAccess();
        if (recalcChangeLogIfNeededInLastSeconds > 0) {
          sql.append(" and gll.last_stem_view_need >= ? ");
          gcDbAccess.addBindVar(System.currentTimeMillis() - (recalcChangeLogIfNeededInLastSeconds*1000));
        }
        sql.append(" and ( ");
        boolean first = true;
        for (MultiKey sourceIdSubjectId : sourceIdSubjectIdBatch) {
          
          if (!first) {
            sql.append(" or ");
          }
          
          sql.append(" (gm.subject_source = ? and gm.subject_id = ?) ");
          
          gcDbAccess.addBindVar(sourceIdSubjectId.getKey(0)).addBindVar(sourceIdSubjectId.getKey(1));
          
          first = false;
        }
        sql.append(" ) ");
        
        List<Object[]> results = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
        
        for (Object[] result : results) {
          String memberId = (String)result[0];
          String sourceId = (String)result[1];
          String subjectId = (String)result[2];
          MultiKey multiKey = new MultiKey(sourceId, subjectId);
          
          sourceIdSubjectIdToMemberId.put(multiKey, memberId);
        }
        
      }
      
      Member allMember = MemberFinder.internal_findAllMember();
      sourceIdSubjectIdToMemberId.put(new MultiKey(allMember.getSubjectSourceId(), allMember.getSubjectId()), allMember.getId());
      
      this.debugMap.put("subjectsNeededInChangeLog", GrouperUtil.length(sourceIdSubjectIdToMemberId));

      iterator = GrouperUtil.nonNull(this.eventsToProcess).iterator();
      
      int peopleNotCalculatingEventsCount = 0;

      // get distinct subjects
      while (iterator.hasNext()) {
        EsbEventContainer esbEventContainer = iterator.next();
        String sourceId = esbEventContainer.getEsbEvent().getSourceId();
        String subjectId = esbEventContainer.getEsbEvent().getSubjectId();
        MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
        if (sourceIdSubjectIdToMemberId.containsKey(sourceIdSubjectId)) {
          continue;
        }
        iterator.remove();
        peopleNotCalculatingEventsCount++;
      }

      this.debugMap.put("peopleNotCalculatingEventsCount", peopleNotCalculatingEventsCount);
      
    } finally {
      this.debugMap.put("removePeopleNotCalculatingEventsMs", (System.nanoTime() - start)/1000000);
    }
    
  }

  private void removeNonPrivilegeEvents() {
    int nonPrivilegeEventCount = 0;
    Iterator<EsbEventContainer> iterator = GrouperUtil.nonNull(this.eventsToProcess).iterator();
    while (iterator.hasNext()) {
      EsbEventContainer esbEventContainer = iterator.next();
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      switch (esbEventType) {
        case PRIVILEGE_ADD:
        case PRIVILEGE_DELETE:
        case PRIVILEGE_UPDATE:
          continue;
        default:
          nonPrivilegeEventCount++;
          iterator.remove();
      }
    }
    this.debugMap.put("nonPrivilegeEventCount", nonPrivilegeEventCount);
  }

  private void removeRedundantEvents() {
    
    long start = System.nanoTime();
    
    try {
      // lets go through events and get list of things to query
      
      // eventType is G (group), S (stem), and A (attribute)
      Set<MultiKey> eventTypeMemberIdStemIdSetToQuery = new HashSet<MultiKey>();
      Set<MultiKey> eventTypeMemberIdStemIdSetInDb = new HashSet<MultiKey>();
      
      // get distinct subjects
      for (EsbEventContainer esbEventContainer : this.eventsToProcess) {

        MultiKey sourceIdSubjectId = new MultiKey(esbEventContainer.getEsbEvent().getSourceId(), esbEventContainer.getEsbEvent().getSubjectId());
        String memberId = sourceIdSubjectIdToMemberId.get(sourceIdSubjectId);
        if (StringUtils.isBlank(memberId)) {
          continue;
        }
        
        String stemName = null;
        String eventType = null;

        if (PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = GrouperUtil.parentStemNameFromName(esbEventContainer.getEsbEvent().getOwnerName());
          eventType = "G";
        } else if (PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = GrouperUtil.parentStemNameFromName(esbEventContainer.getEsbEvent().getOwnerName());
          eventType = "A";
        } else if (PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = esbEventContainer.getEsbEvent().getOwnerName();
          eventType = "S";
        }
        
        String stemId = stemNameToStemId.get(stemName);
        
        if (StringUtils.isBlank(stemId) || StringUtils.isBlank(stemName) || StringUtils.isBlank(eventType)) {
          continue;
        }
        eventTypeMemberIdStemIdSetToQuery.add(new MultiKey(eventType, memberId, stemId));
      }

      List<MultiKey> eventTypeMemberIdStemIdToQueryList = new ArrayList<MultiKey>();
      
      this.debugMap.put("eventTypeMemberIdStemIdToQueryCount", GrouperUtil.length(eventTypeMemberIdStemIdToQueryList));
      
      int batchSize = 300;
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(eventTypeMemberIdStemIdToQueryList, batchSize);
      
      for (int i=0;i<numberOfBatches;i++) {
        List<MultiKey> eventTypeMemberIdStemIdBatch = GrouperUtil.batchList(eventTypeMemberIdStemIdToQueryList, batchSize, i);
        //not sure how this would be possible but...
        if (GrouperUtil.length(eventTypeMemberIdStemIdBatch) == 0) {
          continue;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select object_type, member_uuid, stem_uuid from grouper_stem_view_privilege gsvp where ");
        GcDbAccess gcDbAccess = new GcDbAccess();
        
        boolean first = true;
        
        for (MultiKey eventTypeMemberIdStemId : eventTypeMemberIdStemIdBatch) {
          if (!first) {
            sql.append(" or ");
          }
          sql.append(" ( gsvp.object_type = ? and gsvp.member_uuid = ? and gsvp.stem_uuid = ? ) ");
          gcDbAccess.addBindVar(eventTypeMemberIdStemId.getKey(0));
          gcDbAccess.addBindVar(eventTypeMemberIdStemId.getKey(1));
          gcDbAccess.addBindVar(eventTypeMemberIdStemId.getKey(2));
          
        }          
        
        List<Object[]> results = gcDbAccess.selectList(Object[].class);
        
        for (Object[] result : results) {
          String objectType = (String)result[0];
          String memberId = (String)result[1];
          String stemId = (String)result[2];
          MultiKey multiKey = new MultiKey(objectType, memberId, stemId);
          
          eventTypeMemberIdStemIdSetInDb.add(multiKey);
        }
        
      }
      
      this.debugMap.put("eventTypeMemberIdStemIdInDbCount", GrouperUtil.length(eventTypeMemberIdStemIdSetInDb));
  
      Iterator<EsbEventContainer> iterator = GrouperUtil.nonNull(this.eventsToProcess).iterator();
      
      int redundantEventsCount = 0;
  
      // get distinct subjects
      while (iterator.hasNext()) {

        EsbEventContainer esbEventContainer = iterator.next();
        MultiKey sourceIdSubjectId = new MultiKey(esbEventContainer.getEsbEvent().getSourceId(), esbEventContainer.getEsbEvent().getSubjectId());
        String memberId = sourceIdSubjectIdToMemberId.get(sourceIdSubjectId);
        if (StringUtils.isBlank(memberId)) {
          continue;
        }
        
        String stemName = null;
        String eventType = null;

        if (PrivilegeType.ACCESS.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = GrouperUtil.parentStemNameFromName(esbEventContainer.getEsbEvent().getOwnerName());
          eventType = "G";
        } else if (PrivilegeType.ATTRIBUTE_DEF.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = GrouperUtil.parentStemNameFromName(esbEventContainer.getEsbEvent().getOwnerName());
          eventType = "A";
        } else if (PrivilegeType.NAMING.name().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType())) {
          stemName = esbEventContainer.getEsbEvent().getOwnerName();
          eventType = "S";
        }
        
        String stemId = stemNameToStemId.get(stemName);
        
        if (StringUtils.isBlank(stemId) || StringUtils.isBlank(stemName) || StringUtils.isBlank(eventType)) {
          continue;
        }
        MultiKey eventTypeMemberIdStemIdEvent = new MultiKey(eventType, memberId, stemId);

        
        EsbEventType esbEventType = esbEventContainer.getEsbEventType();
        switch (esbEventType) {
          case PRIVILEGE_ADD:
            if (!eventTypeMemberIdStemIdSetInDb.contains(eventTypeMemberIdStemIdEvent)) {
              // we need to process
              continue;
            }
            break;
          case PRIVILEGE_UPDATE:
            // ignore updates I guess... dont know if add or remove
            continue;
          case PRIVILEGE_DELETE:
            if (!eventTypeMemberIdStemIdSetInDb.contains(eventTypeMemberIdStemIdEvent)) {
              
              // we need to process
              continue;
            }
            break;
          default:
            // shouldnt get here
            continue;
        }
        
        iterator.remove();
        redundantEventsCount++;
      }
  
      this.debugMap.put("redundantEventsCount", redundantEventsCount);
      
    } finally {
      this.debugMap.put("redundantEventsCountMs", (System.nanoTime() - start)/1000000);
    }
    
  }

  private Map<String, String> stemNameToStemId = new HashMap<String, String>();

  private void loadStemIds() {

    long start = System.nanoTime();

    try {

      Set<String> stemNamesSet = new HashSet<String>();

      // get distinct subjects
      for (EsbEventContainer esbEventContainer : this.eventsToProcess) {

        if (PrivilegeType.ACCESS.getPrivilegeName().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType().toLowerCase())
            || PrivilegeType.ATTRIBUTE_DEF.getPrivilegeName().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType().toLowerCase())) {
          stemNamesSet.add(GrouperUtil.parentStemNameFromName(esbEventContainer.getEsbEvent().getOwnerName()));
        } else if (PrivilegeType.NAMING.getPrivilegeName().toLowerCase().equals(esbEventContainer.getEsbEvent().getPrivilegeType().toLowerCase())) {
          stemNamesSet.add(esbEventContainer.getEsbEvent().getOwnerName());
        } else {
          throw new RuntimeException("Unexpected privilege type: " + esbEventContainer.getEsbEvent().getPrivilegeType());
        }
        
      }

      List<String> stemNamesList = new ArrayList<String>(stemNamesSet);

      this.debugMap.put("stemCount", GrouperUtil.length(stemNamesSet));
      
      int batchSize = 900;
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemNamesList, batchSize);
            
      for (int i=0;i<numberOfBatches;i++) {
        List<String> stemNameBatch = GrouperUtil.batchList(stemNamesList, batchSize, i);
        //not sure how this would be possible but...
        if (GrouperUtil.length(stemNameBatch) == 0) {
          continue;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select s.id, s.name from grouper_stems s where s.name in ( ");
        GrouperClientUtils.appendQuestions(sql, stemNameBatch.size());
        sql.append(" ) ");
        GcDbAccess gcDbAccess = new GcDbAccess().sql(sql.toString());
        for (String stemName : stemNameBatch) {
          gcDbAccess.addBindVar(stemName);
        }

        List<Object[]> results = gcDbAccess.selectList(Object[].class);
        
        for (Object[] result : results) {
          String stemId = (String)result[0];
          String stemName = (String)result[1];
          
          stemNameToStemId.put(stemName, stemId);
        }
        
      }
      
    } finally {
      this.debugMap.put("loadStemIdsMs", (System.nanoTime() - start)/1000000);
    }
    
  }
  
  

}
