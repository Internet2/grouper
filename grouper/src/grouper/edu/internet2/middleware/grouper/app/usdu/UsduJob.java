package edu.internet2.middleware.grouper.app.usdu;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasksJob;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMessage;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * usdu daemon
 */
@DisallowConcurrentExecution
public class UsduJob extends OtherJobBase {
  
  private static final Log LOG = GrouperUtil.getLog(UsduJob.class);
    
  private static Map<String, UsduSource> usduConfiguredSources = new HashMap<String, UsduSource>();
  
  private static InheritableThreadLocal<Boolean> runningUsduThreadLocal = new InheritableThreadLocal<Boolean>();

  /**
   * @return if in the thread running usdu
   */
  public static boolean isInUsduThread() {
    return GrouperUtil.booleanValue(runningUsduThreadLocal.get(), false);
  }

  static {
    populateUsduConfiguredSources();
  }
  
  private static void populateUsduConfiguredSources() {
    
    Pattern usduSourceIdKey = Pattern.compile("^usdu\\.source\\.(\\w+)\\.sourceId$");
    
    SourceManager.getInstance().getSources();
    
    Map<String, String> propertiesMap = GrouperConfig.retrieveConfig().propertiesMap(usduSourceIdKey);
    
    for (Entry<String, String> entry: propertiesMap.entrySet()) {
          
      String property = entry.getKey();
      String sourceId = entry.getValue();
      
      try {
        SourceManager.getInstance().getSource(sourceId);
      } catch (SourceUnavailableException e) {
        throw new RuntimeException("source id: "+sourceId+" not found in configured subject sources. ");
      }
      
      Matcher matcher = usduSourceIdKey.matcher(property);
      
      if (matcher.matches()) {
        
        String label = matcher.group(1);
        
        int maxUnresolvableSubjects = GrouperConfig.retrieveConfig().propertyValueInt("usdu.source."+label+".failsafe.maxUnresolvableSubjects", 500);
        
        boolean removeUpToFailSafe = GrouperConfig.retrieveConfig().propertyValueBoolean("usdu.source."+label+".failsafe.removeUpToFailsafe", false);
        
        int deleteAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("usdu.source."+label+".delete.ifAfterDays", 30);
        
        UsduSource source = new UsduSource();
        source.setSourceId(sourceId);
        source.setSourceLabel(label);
        source.setMaxUnresolvableSubjects(maxUnresolvableSubjects);
        source.setDeleteAfterDays(deleteAfterDays);
        source.setRemoveUpToFailsafe(removeUpToFailSafe);
        
        usduConfiguredSources.put(sourceId, source);
      }
      
    }
    
  }
  
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    try {
      runningUsduThreadLocal.set(true);

      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      if (!UsduSettings.usduEnabled()) {
        LOG.info("usdu.enable is set to false. not going to run usdu daemon.");
        return null;
      }
      
      AttributeDefName resolvableMembersAttr = AttributeDefNameFinder.findByName(UsduSettings.usduStemName() + ":subjectResolutionResolvable", false);
      if (resolvableMembersAttr != null) {
        if (UpgradeTasksJob.getDBVersion() < 2) {
          // flag for resolvable and deleted moving from attributes to member table.  if this isn't done yet, don't proceed.
          throw new RuntimeException("Migration for subjectResolutionResolvable and subjectResolutionDeleted has not completed.  USDU will not run until that's done.  That migration is done automatically by the job OTHER_JOB_upgradeTasks.");
        }
        
        LOG.warn("Migration for subjectResolutionResolvable and subjectResolutionDeleted was completed but the attributes are still there.  They may have been added back by an old API version after they were deleted.");
      }
          
      int totalProvisioningObjectsUpdated = 0;
      int batchSize = 20000;
      
      LOG.info("Going to mark members as deleted.");
      Set<Member> unresolvableMembers = new LinkedHashSet<Member>();
      List<String> memberIdsToCheck = new ArrayList<String>(GrouperDAOFactory.getFactory().getMember().findAllMemberIdsForUnresolvableCheck());
      LOG.debug("Found " + memberIdsToCheck.size() + " member ids to check.");
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdsToCheck.size(), batchSize);
      for (int i = 0; i < numberOfBatches; i++) {
        LOG.debug("Processing batch: " + i);
        List<String> currentBatch = GrouperUtil.batchList(memberIdsToCheck, batchSize, i);
        Set<Member> currentMembers = GrouperDAOFactory.getFactory().getMember().findByIds(currentBatch, null);
        LOG.debug("Retrieved current members of size: " + currentMembers.size());

        Map<String, Subject> memberIdToSubjectMap = new HashMap<String, Subject>();
  
        for (Member member : currentMembers) {
          
          if (!member.isSubjectResolutionEligible()) {
            member.setSubjectResolutionEligible(true);
            member.store();
          }
          
          if (!USDU.isMemberResolvable(grouperSession, member, memberIdToSubjectMap)) {
            LOG.debug("Found unresolvable member, subjectId=" + member.getSubjectId() + " source=" + member.getSubjectSourceId());
            unresolvableMembers.add(member);
          }
        }
        
        totalProvisioningObjectsUpdated += syncProvisioningData(currentBatch, memberIdToSubjectMap);
      }
  
      long deletedMembers = deleteUnresolvableMembers(grouperSession, unresolvableMembers, otherJobInput.getHib3GrouperLoaderLog());
      otherJobInput.getHib3GrouperLoaderLog().store();
      
      long nowResolvedMembers = clearMetadataFromNowResolvedMembers(grouperSession);
      otherJobInput.getHib3GrouperLoaderLog().store();
      
      int updateBatchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 200);
      if (updateBatchSize <= 0) {
        updateBatchSize = 1;
      }

      List<String> memberIdsNoLongerSubjectResolutionEligible = new ArrayList<String>(GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible());
      numberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdsNoLongerSubjectResolutionEligible.size(), batchSize);
      for (int i = 0; i < numberOfBatches; i++) {
        List<String> currentBatch = GrouperUtil.batchList(memberIdsNoLongerSubjectResolutionEligible, batchSize, i);
        List<Member> currentMembers = new ArrayList<Member>(GrouperDAOFactory.getFactory().getMember().findByIds(currentBatch, null));
        
        for (Member member : currentMembers) {
          member.setSubjectResolutionEligible(false);
        }
        
        int numberOfUpdateBatches = GrouperUtil.batchNumberOfBatches(currentMembers.size(), updateBatchSize);
        for (int j = 0; j < numberOfUpdateBatches; j++) {
          List<Member> currentUpdateBatch = GrouperUtil.batchList(currentMembers, updateBatchSize, j);
          HibernateSession.byObjectStatic().updateBatch(currentUpdateBatch);
        }
      }
          
      otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Marked " + deletedMembers + " members deleted. Cleared subject resolution attributes from "+nowResolvedMembers +" members.  Updated " + totalProvisioningObjectsUpdated + " cached provisioning objects. Marked " + memberIdsNoLongerSubjectResolutionEligible.size() + " members no longer subject resolution eligible.  ");
      
      int duplicateSubjectIdentifierIssuesCount = checkDuplicateSubjectIdentifiers(otherJobInput.getHib3GrouperLoaderLog());
      
      if (duplicateSubjectIdentifierIssuesCount > 0) {
        // force an error
        throw new RuntimeException("There were duplicate subject identifiers in the grouper_members table.  See job message for details.");
      }
      
      LOG.info("UsduJob finished successfully.");
    } finally {
      runningUsduThreadLocal.remove();
    }
    
    return null;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }
  
  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_usduDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new UsduJob().run(otherJobInput);
  }
  
  /**
   * @param memberIdToSubjectMap
   * @return count of changes
   */
  private int syncProvisioningData(List<String> memberIds, Map<String, Subject> memberIdToSubjectMap) {

    int totalObjectsStored = 0;
    
    Pattern provisionerPatternWithMemberInfo = Pattern.compile("^provisioner\\.(\\w+)\\.(entityAttributeValueCache0has|entityAttributeValueCache1has|entityAttributeValueCache2has|entityAttributeValueCache3has)$");    
    Map<String, String> provisionerPropsWithMemberInfo = GrouperLoaderConfig.retrieveConfig().propertiesMap(provisionerPatternWithMemberInfo);
    Set<String> configNames = new HashSet<String>();
    for (String property : provisionerPropsWithMemberInfo.keySet()) {
      Matcher matcher = provisionerPatternWithMemberInfo.matcher(property);
      matcher.matches();
      String configName = matcher.group(1);
      configNames.add(configName);
    }
    
    RuntimeException runtimeException = null;
    
    for (String configName : configNames) {

      boolean entityAttributeValueCache = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".entityAttributeValueCacheHas", false);

      if (!entityAttributeValueCache) {
        continue;
      }
      
      String entityAttributeValueCache0 = null;
      String entityAttributeValueCache1 = null;
      String entityAttributeValueCache2 = null;
      String entityAttributeValueCache3 = null;

      boolean autoEntityAttributeValueCache0 = false;
      boolean autoEntityAttributeValueCache1 = false;
      boolean autoEntityAttributeValueCache2 = false;
      boolean autoEntityAttributeValueCache3 = false;
      
      if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".entityAttributeValueCache0has", false)
          && StringUtils.equals("grouper", GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache0source"))
          && StringUtils.equals("subjectTranslationScript",  GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache0type"))) {
        
        entityAttributeValueCache0 = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache0translationScript");
        autoEntityAttributeValueCache0 = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".entityAttributeValueCache0auto", true);
      }
          
      if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".entityAttributeValueCache1has", false)
          && StringUtils.equals("grouper", GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache1source"))
          && StringUtils.equals("subjectTranslationScript",  GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache1type"))) {
        
        entityAttributeValueCache1 = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache1translationScript");
        autoEntityAttributeValueCache1 = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".entityAttributeValueCache1auto", true);
      }
      if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".entityAttributeValueCache2has", false)
          && StringUtils.equals("grouper", GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache2source"))
          && StringUtils.equals("subjectTranslationScript",  GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache2type"))) {
        
        entityAttributeValueCache2 = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache2translationScript");
        autoEntityAttributeValueCache2 = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".entityAttributeValueCache2auto", true);
      }
      if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".entityAttributeValueCache3has", false)
          && StringUtils.equals("grouper", GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache3source"))
          && StringUtils.equals("subjectTranslationScript",  GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache3type"))) {
        
        entityAttributeValueCache3 = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".entityAttributeValueCache3translationScript");
        autoEntityAttributeValueCache3 = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".entityAttributeValueCache3auto", true);
      }
            
      if ((!autoEntityAttributeValueCache0 || GrouperUtil.isBlank(entityAttributeValueCache0)) &&
          (!autoEntityAttributeValueCache1 || GrouperUtil.isBlank(entityAttributeValueCache1)) &&
          (!autoEntityAttributeValueCache2 || GrouperUtil.isBlank(entityAttributeValueCache2)) &&
          (!autoEntityAttributeValueCache3 || GrouperUtil.isBlank(entityAttributeValueCache3))) {
        // nothing to do
        continue;
      }
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, configName);
      
      GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("usduSubjectCacheUpdater");
      gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(true);

      GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
      gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
      
      //this is not a true full sync, but this means dont defer if another job comes along, finish this until completion
      //other things can wait
      gcGrouperSyncHeartbeat.setFullSync(true);
      if (gcGrouperSyncHeartbeat.isStarted()) {
        gcGrouperSyncHeartbeat.runHeartbeatThread();
      }

      GcGrouperSyncLog gcGrouperSyncLog = null;
          
      long startNanos = System.nanoTime();
      
      try {
      
        gcGrouperSyncLog = gcGrouperSync.getGcGrouperSyncJobDao().jobCreateLog(gcGrouperSyncJob);
        
        gcGrouperSyncLog.setSyncTimestamp(new Timestamp(System.currentTimeMillis()));

        
        Map<String, GcGrouperSyncMember> gcGrouperSyncMembers = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(memberIds);
        
        Set<GcGrouperSyncMember> changedSyncMembers = new HashSet<GcGrouperSyncMember>();
        
        
        for (String memberId : gcGrouperSyncMembers.keySet()) {
          GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSyncMembers.get(memberId);
          Subject subject = memberIdToSubjectMap.get(memberId);
          if (subject == null) {
            // maybe it didn't get resolved, don't mess with the existing cached data.
            continue;
          }
          
          Map<String, Object> variableMap = new HashMap<String, Object>();
          variableMap.put("subject", subject);
          boolean hasChange = false;
          
          if (autoEntityAttributeValueCache0 && !StringUtils.isBlank(entityAttributeValueCache0)) {
            String entityAttributeValueCache0Value = GrouperUtil.substituteExpressionLanguage(entityAttributeValueCache0, variableMap);
            if (!StringUtils.equals(entityAttributeValueCache0Value, gcGrouperSyncMember.getEntityAttributeValueCache0())) {
              gcGrouperSyncMember.setEntityAttributeValueCache0(entityAttributeValueCache0Value);
              hasChange = true;
            }
          }
          
          if (autoEntityAttributeValueCache1 && !StringUtils.isBlank(entityAttributeValueCache1)) {
            String entityAttributeValueCache1Value = GrouperUtil.substituteExpressionLanguage(entityAttributeValueCache1, variableMap);
            if (!StringUtils.equals(entityAttributeValueCache1Value, gcGrouperSyncMember.getEntityAttributeValueCache1())) {
              gcGrouperSyncMember.setEntityAttributeValueCache1(entityAttributeValueCache1Value);
              hasChange = true;
            }
          }
          
          if (autoEntityAttributeValueCache2 && !StringUtils.isBlank(entityAttributeValueCache2)) {
            String entityAttributeValueCache2Value = GrouperUtil.substituteExpressionLanguage(entityAttributeValueCache2, variableMap);
            if (!StringUtils.equals(entityAttributeValueCache2Value, gcGrouperSyncMember.getEntityAttributeValueCache2())) {
              gcGrouperSyncMember.setEntityAttributeValueCache2(entityAttributeValueCache2Value);
              hasChange = true;
            }
          }
          
          if (autoEntityAttributeValueCache3 && !StringUtils.isBlank(entityAttributeValueCache3)) {
            String entityAttributeValueCache3Value = GrouperUtil.substituteExpressionLanguage(entityAttributeValueCache3, variableMap);
            if (!StringUtils.equals(entityAttributeValueCache3Value, gcGrouperSyncMember.getEntityAttributeValueCache3())) {
              gcGrouperSyncMember.setEntityAttributeValueCache3(entityAttributeValueCache3Value);
              hasChange = true;
            }
          }
          if (hasChange) {
            changedSyncMembers.add(gcGrouperSyncMember);
          }
        }
        
        gcGrouperSyncLog.setRecordsProcessed(GrouperUtil.length(gcGrouperSyncMembers));

        int currentObjectsStored = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();

        for (GcGrouperSyncMember gcGrouperSyncMember : changedSyncMembers) {
          if ((gcGrouperSyncMember.getInTarget() != null && gcGrouperSyncMember.getInTarget())) {
            ProvisioningMessage provisioningMessage = new ProvisioningMessage();
            provisioningMessage.setMemberIdsForSync(new String[] {gcGrouperSyncMember.getMemberId()});
            provisioningMessage.setBlocking(true);
            provisioningMessage.send(configName);
          }
        }
        
        gcGrouperSyncLog.setRecordsChanged(currentObjectsStored);
        
        LOG.info("Updated " + currentObjectsStored + " objects for configName=" + configName);
      
        totalObjectsStored += currentObjectsStored;
        
        gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.SUCCESS);
      } catch (RuntimeException e) {
        
        // if one provisioner fails, continue with others
        GrouperUtil.injectInException(e, "Problem in configName: '" + configName + "'");
        LOG.error("error", e);
        runtimeException = e;
        gcGrouperSyncJob.setErrorMessage(GrouperUtil.getFullStackTrace(e));
      } finally {

        // end heartbeat thread
        GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);

        // save the job
        try {
          gcGrouperSyncJob.assignHeartbeatAndEndJob();
        } catch (RuntimeException re2) {
          if (gcGrouperSyncLog != null) {
            if (gcGrouperSyncLog.getStatus() == null || !gcGrouperSyncLog.getStatus().isError()) {
              gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
            }
          }
        }
        
        int durationMillis = (int)((System.nanoTime()-startNanos)/1000000);

        // save the log
        try {
          if (gcGrouperSyncLog != null) {
            gcGrouperSyncLog.setJobTookMillis(durationMillis);
            gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLog);
          }
        } catch (RuntimeException re3) {
        }

      }
    }

    // if any of the parts is a failure, its a failure
    if (runtimeException != null) {
      throw runtimeException;
    }
    
    return totalObjectsStored;
  }
  
  /**
   * clear attributes from members who have become resolvable again.
   * @param grouperSession
   * @return
   */
  private long clearMetadataFromNowResolvedMembers(GrouperSession grouperSession) {
   
    Set<Member> members = GrouperDAOFactory.getFactory().getMember().getUnresolvableMembers(null, null);
    
    long resolvableMembers = 0; 
    
    for (Member member: members) {
      if (USDU.isMemberResolvable(grouperSession, member)) {
        UsduService.deleteAttributeAssign(member);
        
        member.setSubjectResolutionDeleted(false);
        member.setSubjectResolutionResolvable(true);
        member.store();
        
        resolvableMembers++;
      }
    }
    
    return resolvableMembers;
    
  }
  
  
  /**
   * delete unresolvable members
   * @param grouperSession
   * @param unresolvableMembres
   * @return number of members marked as deleted
   */
  public static long deleteUnresolvableMembers(GrouperSession grouperSession, Set<Member> unresolvableMembers, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
        
    // map to store source id to set of members to be deleted
    Map<String, Set<Member>> sourceIdToMembers = new HashMap<String, Set<Member>>();
    
    // store members for which sources have not been configured
    Set<Member> membersWithoutExplicitSourceConfiguration = new HashSet<Member>();
    
    populateUnresolvableMembersConfig(unresolvableMembers, sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
    
    return deleteUnresolvableMembers(sourceIdToMembers, membersWithoutExplicitSourceConfiguration, hib3GrouperLoaderLog);
    
  }
  
  /**
   * delete unresolvable members
   * @param unresolvables
   */
  private static void populateUnresolvableMembersConfig(Set<Member> unresolvables, Map<String, Set<Member>> sourceIdToMembers, 
      Set<Member> membersWithoutExplicitSourceConfiguration) {
        
    if (unresolvables.size() == 0) {
      return;
    }

    AttributeAssignValueFinder attributeAssignValueFinder = new AttributeAssignValueFinder();
    
    for (Member member : unresolvables) {
      
      attributeAssignValueFinder.addOwnerMemberIdOfAssignAssign(member.getId());
    }
    
    attributeAssignValueFinder.addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId());
    AttributeAssignValueFinderResult attributeAssignValueFinderResult = attributeAssignValueFinder.findAttributeAssignValuesResult();
    
    for (Member member : unresolvables) {
      
      SubjectResolutionAttributeValue savedSubjectResolutionAttributeValue = saveSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
      
      addUnresolvedMemberToCorrectSet(member, savedSubjectResolutionAttributeValue, sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
      
    }
    
  }
  
  private static long deleteUnresolvableMembers(Map<String, Set<Member>> sourceIdToMembers, Set<Member> membersWithoutExplicitSourceConfiguration, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    
    int globalMaxAllowed = GrouperConfig.retrieveConfig().propertyValueInt("usdu.failsafe.maxUnresolvableSubjects", 500);
    
    boolean globalRemoveUpToFailSafe = GrouperConfig.retrieveConfig().propertyValueBoolean("usdu.failsafe.removeUpToFailsafe", false);
    
    long deletedCount = 0;
    
    // now we need to decide if we need to delete unresolvable members and how many
    for (String sourceId: sourceIdToMembers.keySet()) {
      
      UsduSource source = usduConfiguredSources.get(sourceId);
      
      Set<Member> unresolvableMembersForASource = sourceIdToMembers.get(sourceId);
      int maxUnresolvableSubjectsAllowed = source.getMaxUnresolvableSubjects();
      boolean removeUpToFailsafe = source.isRemoveUpToFailsafe();
      
      if (unresolvableMembersForASource.size() > maxUnresolvableSubjectsAllowed) {
        
        if (!removeUpToFailsafe) {
          String error = "For source id "+sourceId+" found "+unresolvableMembersForASource.size()+" unresolvable members. max limit is "+maxUnresolvableSubjectsAllowed+". "
              + "removeUpToFailsafe is set to false hence not going to delete any members.  ";
          LOG.error(error);
          hib3GrouperLoaderLog.appendJobMessage(error);
          hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
        } else {
          String error = "For source id "+sourceId+" found "+unresolvableMembersForASource.size()+" unresolvable members. max limit is "+maxUnresolvableSubjectsAllowed+". "
              + "removeUpToFailsafe is set to true hence going to delete "+maxUnresolvableSubjectsAllowed+" members.  ";
          LOG.error(error);
          hib3GrouperLoaderLog.appendJobMessage(error);
          hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.ERROR.name());

          deletedCount += deleteUnresolvableMembers(unresolvableMembersForASource, maxUnresolvableSubjectsAllowed);
          
        }
        
      } else {
        deletedCount += deleteUnresolvableMembers(unresolvableMembersForASource, unresolvableMembersForASource.size());
      }
        
    }
    
    // let's take care of the sources that have not been configured explicitly
    if (membersWithoutExplicitSourceConfiguration.size() > globalMaxAllowed) {
      
      if (!globalRemoveUpToFailSafe) {
        String error = "For global (not explicitly defined sources) found "+membersWithoutExplicitSourceConfiguration.size()+" unresolvable members. max limit is "+globalMaxAllowed+". "
            + "usdu.failsafe.removeUpToFailsafe is set to false hence not going to delete any members.  ";
        LOG.error(error);
        hib3GrouperLoaderLog.appendJobMessage(error);
        hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      } else {
        String error = "For global (not explicitly defined sources) found "+membersWithoutExplicitSourceConfiguration.size()+" unresolvable members. max limit is "+globalMaxAllowed+". "
            + "usdu.failsafe.removeUpToFailsafe is set to true hence going to delete "+globalMaxAllowed+" members.  ";
        LOG.error(error);
        hib3GrouperLoaderLog.appendJobMessage(error);
        hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.ERROR.name());

        deletedCount += deleteUnresolvableMembers(membersWithoutExplicitSourceConfiguration, globalMaxAllowed);
        
      }
      
    } else {
      deletedCount += deleteUnresolvableMembers(membersWithoutExplicitSourceConfiguration, membersWithoutExplicitSourceConfiguration.size());
    }
    
    return deletedCount;
    
  }
  
  private static void addUnresolvedMemberToCorrectSet(Member member, SubjectResolutionAttributeValue memberSubjectResolutionAttributeValue,
      Map<String, Set<Member>> sourceIdToMembers, Set<Member> membersWithoutExplicitSourceConfiguration) {
    
    int globalDeleteAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("usdu.delete.ifAfterDays", 30);
    
    if (usduConfiguredSources.containsKey(member.getSubjectSourceId())) { // this source has been configured explicitly
            
      if ( memberSubjectResolutionAttributeValue.getSubjectResolutionDaysUnresolved() > usduConfiguredSources.get(member.getSubjectSourceId()).getDeleteAfterDays()) {
        Set<Member> membersPerSource = sourceIdToMembers.get(member.getSubjectSourceId());
        if (membersPerSource == null) {
          membersPerSource = new HashSet<Member>();
        }
        
        membersPerSource.add(member);
        sourceIdToMembers.put(member.getSubjectSourceId(), membersPerSource);
      }
      
    } else if (memberSubjectResolutionAttributeValue.getSubjectResolutionDaysUnresolved() > globalDeleteAfterDays) {
      membersWithoutExplicitSourceConfiguration.add(member);
    }
    
  }
  
  private static SubjectResolutionAttributeValue saveSubjectResolutionAttributeValue(Member member, AttributeAssignValueFinderResult attributeAssignValueFinderResult) {
    
    SubjectResolutionAttributeValue existingSubjectResolutionAttributeValue = UsduService.getSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
    
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    Date currentDate = new Date();
    String curentDateString = dateFormat.format(currentDate);
    
    SubjectResolutionAttributeValue newValue = new SubjectResolutionAttributeValue();
    
    AuditEntry auditEntry = null;
    
    if (existingSubjectResolutionAttributeValue == null) { //this member has become unresolvable for the first time only
      
      newValue.setSubjectResolutionResolvableString(BooleanUtils.toStringTrueFalse(false));
      newValue.setSubjectResolutionDateLastResolvedString(curentDateString);
      newValue.setSubjectResolutionDaysUnresolvedString(String.valueOf(0L));
      newValue.setSubjectResolutionDateLastCheckedString(curentDateString);
      newValue.setMember(member);
      
      auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_ADD);
      
      auditEntry.setDescription("Subject with id: " + member.getSubjectId() + " is being marked as unresolvable on "+currentDate);
      
    } else {
      
      String dateLastResolvedString = existingSubjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString();
      Date dateLastResolved = null;
      try {
        dateLastResolved = dateFormat.parse(dateLastResolvedString);
      } catch (ParseException e) {
        throw new RuntimeException(dateLastResolvedString+" is not a valid yyyy/MM/dd format");
      }
      
      long diff = currentDate.getTime() - dateLastResolved.getTime();
      
      long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
      
      newValue.setSubjectResolutionDateLastCheckedString(curentDateString);
      newValue.setSubjectResolutionDaysUnresolvedString(String.valueOf(days));
      newValue.setMember(member);
      
      auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_UPDATE);
      
      auditEntry.setDescription("Subject with id: " + member.getSubjectId() + "; updating subject resolution attributes on "+currentDate);
      
    }
    
    UsduService.markMemberAsUnresolved(newValue, member);
    
    auditEntry.assignStringValue(auditEntry.getAuditType(), "ownerMemberId", member.getUuid());
    auditEntry.assignStringValue(auditEntry.getAuditType(), "ownerSourceId", member.getSubjectSourceId());
    auditEntry.assignStringValue(auditEntry.getAuditType(), "ownerSubjectId", member.getSubjectId());
    
    final AuditEntry AUDIT_ENTRY = auditEntry;
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
        AUDIT_ENTRY.saveOrUpdate(true);
        return null;
      }
    });
    
    return newValue;
    
  }
  
  public static int checkDuplicateSubjectIdentifiers(Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    int issues = 0;
    
    String sqlAll = "select subjectSourceIdDb, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2 from Member where subjectIdentifier0 is not null or subjectIdentifier1 is not null or subjectIdentifier2 is not null";
    Map<String, Set<String>> foundIdentifiers = new HashMap<String, Set<String>>();
    Map<String, Set<String>> duplicateIdentifiers = new HashMap<String, Set<String>>();
    Set<Object[]> resultsAll = HibernateSession.byHqlStatic().createQuery(sqlAll).setCacheable(false).listSet(Object[].class);
    for (Object[] result : resultsAll) {
      Set<String> distinctIdentifiers = new HashSet<String>();
      String sourceId = (String)result[0];
      String subjectIdentifier0 = (String)result[1];
      String subjectIdentifier1 = (String)result[2];
      String subjectIdentifier2 = (String)result[3];
      
      if (!StringUtils.isEmpty(subjectIdentifier0)) {
        distinctIdentifiers.add(subjectIdentifier0);
      }
      
      if (!StringUtils.isEmpty(subjectIdentifier1)) {
        distinctIdentifiers.add(subjectIdentifier1);
      }
      
      if (!StringUtils.isEmpty(subjectIdentifier2)) {
        distinctIdentifiers.add(subjectIdentifier2);
      }

      if (!foundIdentifiers.containsKey(sourceId)) {
        foundIdentifiers.put(sourceId, new HashSet<String>());
      }
      
      for (String identifier : distinctIdentifiers) {
        if (foundIdentifiers.get(sourceId).contains(identifier)) {
          if (!duplicateIdentifiers.containsKey(sourceId)) {
            duplicateIdentifiers.put(sourceId, new HashSet<String>());
          }
          
          duplicateIdentifiers.get(sourceId).add(identifier);
        } else {
          foundIdentifiers.get(sourceId).add(identifier);
        }
      }
    }
    
    for (String sourceId : duplicateIdentifiers.keySet()) {
      for (String subjectIdentifier : duplicateIdentifiers.get(sourceId)) {
  
        Set<Member> members = HibernateSession.byHqlStatic()
          .createQuery("from Member where (subjectIdentifier0 = :subjectIdentifier or subjectIdentifier1 = :subjectIdentifier or subjectIdentifier2 = :subjectIdentifier)  and subjectSourceIdDb = :subjectSourceIdDb")
          .setString("subjectIdentifier", subjectIdentifier)
          .setString("subjectSourceIdDb", sourceId)
          .listSet(Member.class);
              
        Set<Member> resolvableMembersWithSameIdentifier = new HashSet<Member>();
        
        try {
          for (Member member : members) {
            Subject subject = SubjectFinder.findByIdAndSource(member.getSubjectId(), member.getSubjectSourceId(), true, false);
            if (subject == null) {
              member.setSubjectIdentifier0(null);
              member.setSubjectIdentifier1(null);
              member.setSubjectIdentifier2(null);
              member.store();
              LOG.info("Cleared duplicate subject identifier for subjectId=" + member.getSubjectId());
            } else {
              member.updateMemberAttributes(subject, false); // resolving it above should store it.  just need to make sure we have the new data.
    
              if (StringUtils.equals(subjectIdentifier, member.getSubjectIdentifier0()) ||
                  StringUtils.equals(subjectIdentifier, member.getSubjectIdentifier1()) || 
                  StringUtils.equals(subjectIdentifier, member.getSubjectIdentifier2())) {
                resolvableMembersWithSameIdentifier.add(member);
              }
            }
          }
        } catch (SourceUnavailableException e) {
          // might be an old/unused source.  skip.
          LOG.warn("Skipping duplicate fix due to source error", e);
          
          continue;
        }
        
        if (resolvableMembersWithSameIdentifier.size() > 1) {
          issues++;
          
          // ok duplicates still exist after resolving.
          if (hib3GrouperLoaderLog != null) {
            Set<String> subjectIds = new HashSet<String>();
            for (Member member : resolvableMembersWithSameIdentifier) {
              subjectIds.add(member.getSubjectId());
            }
            
            hib3GrouperLoaderLog.appendJobMessage(" There are subjects with the same subject identifier=" + subjectIdentifier + ", subjectIds=" + String.join(",", subjectIds) + ". ");
          }
        }
      }
    }

    return issues;
  }
  
  public static long deleteUnresolvableMembers(Set<Member> unresolvableMembers, int howMany) {
    
    long deletedCount = 0;
    AttributeDefName usduMarkerAttributeDefName = UsduAttributeNames.retrieveAttributeDefNameBase();
    
    for (final Member member: unresolvableMembers) {
      
      if (deletedCount >= howMany) {
        LOG.info("Total: "+unresolvableMembers.size()+" unresolvable members, deleted: "+deletedCount);
        break;
      }
      
      Set<Field> fields = getMemberFields();
      
      Set<Membership> memberships = getAllImmediateMemberships(member, fields);
      
      for (final Membership membership : memberships) {
    
        LOG.info("member_uuid='" + member.getUuid() + "' subject=" + member);
        if (membership.getList().getType().equals(FieldType.LIST)
            || membership.getList().getType().equals(FieldType.ACCESS)) {
          LOG.info(" group='" + membership.getOwnerGroup().getName());
        }
        if (membership.getList().getType().equals(FieldType.NAMING)) {
          LOG.info(" stem='" + membership.getOwnerStem().getName());
        }
        if (membership.getList().getType().equals(FieldType.ATTRIBUTE_DEF)) {
          LOG.info(" attrDef='" + membership.getOwnerAttributeDef().getName());
        }
        LOG.info(" list='" + membership.getList().getName() + "'");
        
        HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
            
            if (membership.getList().getType().equals(FieldType.LIST)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerGroup(), membership.getList());
            }
            
            if (membership.getList().getType().equals(FieldType.ACCESS)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerGroup(), getPrivilege(membership.getList()));
            }
            
            if (membership.getList().getType().equals(FieldType.NAMING)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerStem(), getPrivilege(membership.getList()));
            }
            
            if (membership.getList().getType().equals(FieldType.ATTRIBUTE_DEF)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerAttributeDef(), getPrivilege(membership.getList()));
            }
            
            return null;
          }
        });
                
      }
      
      for (AttributeAssign attributeAssign : GrouperDAOFactory.getFactory().getAttributeAssign().findByOwnerMemberId(member.getId())) {
        
        if (!attributeAssign.getAttributeDefNameId().equals(usduMarkerAttributeDefName.getId())) {
          HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
            public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
              
              LOG.info("member_uuid='" + member.getUuid() + "' subject=" + member + " attributeAssignId=" + attributeAssign.getId());
              attributeAssign.delete();
              
              return null;
            }
          });
        }
      }
      
      UsduService.markMemberAsDeleted(member);
      
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
        public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
          
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.USDU_MEMBER_DELETE);
          auditEntry.assignStringValue(auditEntry.getAuditType(), "memberId", member.getUuid());
          auditEntry.assignStringValue(auditEntry.getAuditType(), "sourceId", member.getSubjectSourceId());
          auditEntry.assignStringValue(auditEntry.getAuditType(), "subjectId", member.getSubjectId());
          auditEntry.setDescription("Deleted source id: " + member.getSubjectSourceId() + ", subject id: "+member.getSubjectId()+", name: "+member.getName()+", description: " + member.getDescription());
          
          auditEntry.saveOrUpdate(true);
          
          return null;
        }
      });
            
      deletedCount++;
      
    }
    
    return deletedCount;
    
  }
  
  /**
   * Get fields of which a subject might be a member. Includes all fields of
   * type FieldType.LIST, FieldType.ACCESS, FieldType.ATTRIBUTE_DEF, and FieldType.NAMING.
   * 
   * @return set of fields
   * @throws SchemaException
   */
  protected static Set<Field> getMemberFields() throws SchemaException {

    Set<Field> listFields = new LinkedHashSet<Field>();
    for (Object field : FieldFinder.findAllByType(FieldType.LIST)) {
      listFields.add((Field) field);
    }
    for (Object field : FieldFinder.findAllByType(FieldType.ACCESS)) {
      listFields.add((Field) field);
    }
    for (Object field : FieldFinder.findAllByType(FieldType.NAMING)) {
      listFields.add((Field) field);
    }
    for (Object field : FieldFinder.findAllByType(FieldType.ATTRIBUTE_DEF)) {
      listFields.add((Field) field);
    }
    return listFields;
  }
  
  /**
   * Get memberships for a member for the given fields.
   * 
   * @param member
   * @param fields
   *          a set of 'list' fields
   * @return a set of memberships
   * @throws SchemaException
   */
  protected static Set<Membership> getAllImmediateMemberships(Member member, Set<Field> fields) throws SchemaException {

    Set<Membership> memberships = new LinkedHashSet<Membership>();
    for (Field field : fields) {
      
      Set<Object[]> rows = new MembershipFinder()
        .addMemberId(member.getId()).addField(field).assignEnabled(null).assignMembershipType(MembershipType.IMMEDIATE)
        .findMembershipsMembers();
      for (Object[] row : GrouperUtil.nonNull(rows)) {
        memberships.add((Membership) row[0]);
      }
    }
    return memberships;
  }
  
  /**
   * Map fields to privileges.
   * 
   * @param field
   * @return the privilege matching the given field or null
   */
  protected static Privilege getPrivilege(Field field) {

    return Privilege.listToPriv(field.getName(), true);
  }

}
