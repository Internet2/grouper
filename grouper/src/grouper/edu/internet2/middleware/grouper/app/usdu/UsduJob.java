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
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasksJob;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
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
  
  /** map list names to corresponding privileges, a better way probably exists */
  private static Map<String, Privilege> list2priv = new HashMap<String, Privilege>();
  
  private static Map<String, UsduSource> usduConfiguredSources = new HashMap<String, UsduSource>();
  
  private static InheritableThreadLocal<Boolean> runningUsduThreadLocal = new InheritableThreadLocal<Boolean>();

  /**
   * @return if in the thread running usdu
   */
  public static boolean isInUsduThread() {
    return GrouperUtil.booleanValue(runningUsduThreadLocal.get(), false);
  }

  static {
    list2priv.put(Field.FIELD_NAME_ADMINS, AccessPrivilege.ADMIN);
    list2priv.put(Field.FIELD_NAME_OPTINS, AccessPrivilege.OPTIN);
    list2priv.put(Field.FIELD_NAME_OPTOUTS, AccessPrivilege.OPTOUT);
    list2priv.put(Field.FIELD_NAME_READERS, AccessPrivilege.READ);
    list2priv.put(Field.FIELD_NAME_UPDATERS, AccessPrivilege.UPDATE);
    list2priv.put(Field.FIELD_NAME_VIEWERS, AccessPrivilege.VIEW);
    list2priv.put(Field.FIELD_NAME_GROUP_ATTR_READERS, AccessPrivilege.GROUP_ATTR_READ);
    list2priv.put(Field.FIELD_NAME_GROUP_ATTR_UPDATERS, AccessPrivilege.GROUP_ATTR_UPDATE);
    list2priv.put(Field.FIELD_NAME_CREATORS, NamingPrivilege.CREATE);
    list2priv.put(Field.FIELD_NAME_STEM_ADMINS, NamingPrivilege.STEM_ADMIN);
    list2priv.put(Field.FIELD_NAME_STEM_ATTR_READERS, NamingPrivilege.STEM_ATTR_READ);
    list2priv.put(Field.FIELD_NAME_STEM_ATTR_UPDATERS, NamingPrivilege.STEM_ATTR_UPDATE);
    
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
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberIdsToCheck.size(), batchSize);
      for (int i = 0; i < numberOfBatches; i++) {
        List<String> currentBatch = GrouperUtil.batchList(memberIdsToCheck, batchSize, i);
        Set<Member> currentMembers = GrouperDAOFactory.getFactory().getMember().findByIds(currentBatch, null);
        
        Map<String, Subject> memberIdToSubjectMap = new HashMap<String, Subject>();
  
        for (Member member : currentMembers) {
          
          if (!USDU.isMemberResolvable(grouperSession, member, memberIdToSubjectMap)) {
            unresolvableMembers.add(member);
          }
        }
        
        totalProvisioningObjectsUpdated += syncProvisioningData(currentBatch, memberIdToSubjectMap);
      }
  
      long deletedMembers = deleteUnresolvableMembers(grouperSession, unresolvableMembers);
      otherJobInput.getHib3GrouperLoaderLog().store();
      
      long nowResolvedMembers = clearMetadataFromNowResolvedMembers(grouperSession);
      otherJobInput.getHib3GrouperLoaderLog().store();
          
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Marked " + deletedMembers + " members deleted. Cleared subject resolution attributes from "+nowResolvedMembers +" members.  Updated " + totalProvisioningObjectsUpdated + " cached provisioning objects.");
      
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
    
    Pattern provisionerPatternWithMemberInfo = Pattern.compile("^provisioner\\.(\\w+)\\.common\\.subjectLink\\.(memberFromId2|memberFromId3|memberToId2|memberToId3)$");    
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
      boolean isEnabled = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".common.enabled", true); // what is the default here??
      if (!isEnabled) {
        continue;
      }
      
      String memberFromId2 = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".common.subjectLink.memberFromId2");
      String memberFromId3 = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".common.subjectLink.memberFromId3");
      String memberToId2 = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".common.subjectLink.memberToId2");
      String memberToId3 = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + configName + ".common.subjectLink.memberToId3");

      boolean autoMemberFromId2 = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".common.subjectLink.autoMemberFromId2", true);
      boolean autoMemberFromId3 = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".common.subjectLink.autoMemberFromId3", true);
      boolean autoMemberToId2 = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".common.subjectLink.autoMemberToId2", true);
      boolean autoMemberToId3 = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + configName + ".common.subjectLink.autoMemberToId3", true);

      if ((!autoMemberFromId2 || GrouperUtil.isBlank(memberFromId2)) &&
          (!autoMemberFromId3 || GrouperUtil.isBlank(memberFromId3)) &&
          (!autoMemberToId2 || GrouperUtil.isBlank(memberToId2)) &&
          (!autoMemberToId3 || GrouperUtil.isBlank(memberToId3))) {
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
        for (String memberId : gcGrouperSyncMembers.keySet()) {
          GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSyncMembers.get(memberId);
          Subject subject = memberIdToSubjectMap.get(memberId);
          if (subject == null) {
            // maybe it didn't get resolved, don't mess with the existing cached data.
            continue;
          }
          
          Map<String, Object> variableMap = new HashMap<String, Object>();
          variableMap.put("subject", subject);
          
          if (autoMemberFromId2 && !StringUtils.isBlank(memberFromId2)) {
            String memberFromId2Value = GrouperUtil.substituteExpressionLanguage(memberFromId2, variableMap);
            gcGrouperSyncMember.setMemberFromId2(memberFromId2Value);
          }
          
          if (autoMemberFromId3 && !StringUtils.isBlank(memberFromId3)) {
            String memberFromId3Value = GrouperUtil.substituteExpressionLanguage(memberFromId3, variableMap);
            gcGrouperSyncMember.setMemberFromId3(memberFromId3Value);
          }
          
          if (autoMemberToId2 && !StringUtils.isBlank(memberToId2)) {
            String memberToId2Value = GrouperUtil.substituteExpressionLanguage(memberToId2, variableMap);
            gcGrouperSyncMember.setMemberToId2(memberToId2Value);
          }
          
          if (autoMemberToId3 && !StringUtils.isBlank(memberToId3)) {
            String memberToId3Value = GrouperUtil.substituteExpressionLanguage(memberToId3, variableMap);
            gcGrouperSyncMember.setMemberToId3(memberToId3Value);
          }
        }
        
        gcGrouperSyncLog.setRecordsProcessed(GrouperUtil.length(gcGrouperSyncMembers));

        int currentObjectsStored = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();

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
            gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
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
  private long deleteUnresolvableMembers(GrouperSession grouperSession, Set<Member> unresolvableMembers) {
        
    // map to store source id to set of members to be deleted
    Map<String, Set<Member>> sourceIdToMembers = new HashMap<String, Set<Member>>();
    
    // store members for which sources have not been configured
    Set<Member> membersWithoutExplicitSourceConfiguration = new HashSet<Member>();
    
    populateUnresolvableMembersConfig(unresolvableMembers, sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
    
    return deleteUnresolvableMembers(sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
    
  }
  
  /**
   * delete unresolvable members
   * @param unresolvables
   */
  private void populateUnresolvableMembersConfig(Set<Member> unresolvables, Map<String, Set<Member>> sourceIdToMembers, 
      Set<Member> membersWithoutExplicitSourceConfiguration) {
    
    Set<Field> fields = getMemberFields();
    Set<Member> unresolvablesWithMemberships = new HashSet<Member>();
    
    for (Member member : unresolvables) {
      
      Set<Membership> memberships = getAllImmediateMemberships(member, fields);
      
      if (memberships.isEmpty()) {
        LOG.info("member_uuid='" + member.getUuid() + "' subject=" + member + " no_memberships");
        continue;
      }
      unresolvablesWithMemberships.add(member);
    }
    
    if (unresolvablesWithMemberships.size() == 0) {
      return;
    }

    AttributeAssignValueFinder attributeAssignValueFinder = new AttributeAssignValueFinder();
    
    for (Member member : unresolvablesWithMemberships) {
      
      attributeAssignValueFinder.addOwnerMemberIdOfAssignAssign(member.getId());
    }
    
    attributeAssignValueFinder.addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId());
    AttributeAssignValueFinderResult attributeAssignValueFinderResult = attributeAssignValueFinder.findAttributeAssignValuesResult();
    
    for (Member member : unresolvablesWithMemberships) {
      
      SubjectResolutionAttributeValue savedSubjectResolutionAttributeValue = saveSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
      
      addUnresolvedMemberToCorrectSet(member, savedSubjectResolutionAttributeValue, sourceIdToMembers, membersWithoutExplicitSourceConfiguration);
      
    }
    
  }
  
  private long deleteUnresolvableMembers(Map<String, Set<Member>> sourceIdToMembers, Set<Member> membersWithoutExplicitSourceConfiguration) {
    
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
          LOG.info("For source id "+sourceId+" found "+unresolvableMembersForASource.size()+"unresolvable members. max limit is "+maxUnresolvableSubjectsAllowed+". "
              + "removeUpToFailsafe is set to false hence not going to delete any members.");
        } else {
          LOG.info("For source id "+sourceId+" found "+unresolvableMembersForASource.size()+"unresolvable members. max limit is "+maxUnresolvableSubjectsAllowed+". "
              + "removeUpToFailsafe is set to true hence going to delete "+maxUnresolvableSubjectsAllowed+" members.");
          
          deletedCount += deleteUnresolvableMembers(unresolvableMembersForASource, maxUnresolvableSubjectsAllowed);
          
        }
        
      } else {
        deletedCount += deleteUnresolvableMembers(unresolvableMembersForASource, unresolvableMembersForASource.size());
      }
        
    }
    
    // let's take care of the sources that have not been configured explicitly
    if (membersWithoutExplicitSourceConfiguration.size() > globalMaxAllowed) {
      
      if (!globalRemoveUpToFailSafe) {
        LOG.info("For global (not explicitly defined sources) found "+membersWithoutExplicitSourceConfiguration.size()+"unresolvable members. max limit is "+globalMaxAllowed+". "
            + "usdu.failsafe.removeUpToFailsafe is set to false hence not going to delete any members.");
      } else {
        LOG.info("For global (not explicitly defined sources) found "+membersWithoutExplicitSourceConfiguration.size()+"unresolvable members. max limit is "+globalMaxAllowed+". "
            + "usdu.failsafe.removeUpToFailsafe is set to true hence going to delete "+globalMaxAllowed+" members.");
        
        deletedCount += deleteUnresolvableMembers(membersWithoutExplicitSourceConfiguration, globalMaxAllowed);
        
      }
      
    } else {
      deletedCount += deleteUnresolvableMembers(membersWithoutExplicitSourceConfiguration, membersWithoutExplicitSourceConfiguration.size());
    }
    
    return deletedCount;
    
  }
  
  private void addUnresolvedMemberToCorrectSet(Member member, SubjectResolutionAttributeValue memberSubjectResolutionAttributeValue,
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
  
  private SubjectResolutionAttributeValue saveSubjectResolutionAttributeValue(Member member, AttributeAssignValueFinderResult attributeAssignValueFinderResult) {
    
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
  
  public static long deleteUnresolvableMembers(Set<Member> unresolvableMembers, int howMany) {
    
    long deletedCount = 0;
    
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
            
            return null;
          }
        });
                
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
   * type FieldType.LIST, FieldType.ACCESS, and FieldType.NAMING.
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

    return list2priv.get(field.getName());
  }

}
