package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Top-Level provisioner class of PSPNG and is the superclass of Target-System-Specific 
 * subclasses. This class is generic so that User & Group objects that the subclasses
 * need are cached and referenced directly in overridden methods. For example, an
 * LDAP-provisioning subclass will receive both Subject and LdapUser information.
 * 
 * This class is responsible for the following functionalities:
 *   -Mapping the grouper world of Subjects & Groups into User and Group objects specific to the target system
 *   -Caching both Grouper & Target-System information
 *   -Driving Incremental and FullSync operations
 *   -Keep track of what groups need to be full-synced (ie, there is a full-sync queue)
 * 
 * While there are several abstract methods:
 *     fetchTargetSystemUsers & fetchTargetSystemGroups
 *     addMembership & deleteMembership
 *     createGroup & deleteGroup
 *     doFullSync & doFullSync_cleanupExtraGroups
 *   
 * And that some subclasses can override the following method(s):
 *     createUser
 *   
 *   
 * The Provisioner lifecycle is as follows:
 * -Constructed(Name and ProvisionerConfiguration subclass)
 *   (setup empty caches)
 * 
 * Invoked with a batch of changes
 *   -startProvisioningBatch(A collection of change events)
 *      (refresh caches as necessary, do any bulk fetching that will help provisioning go faster)
 *   -provisionItem or addMember/deleMembership/createGroup/deleteGroup (for each event in the batch)
 *      (Determine and/or make the necessary changes)
 *      
 *   -finishProvisioningBatch(A collection of change events)
 *      (Perform any batch updates that were determined, but not performed, by provisionItem step)
 *      (Clear any caches specific to batch)
 *      
 *  FullSync
 *    -Go through all Grouper groups and see which match the provisioner's selection filter
 *    -Run the subclass's doFullSync method for each group that matches
 *    -Run doFullSync_cleanupExtraGruops with the list of correct groups
 *      
 * @author Bert Bee-Lindgren
 *
 */
public abstract class Provisioner
  <ConfigurationClass extends ProvisionerConfiguration, 
   TSUserClass extends TargetSystemUser, 
   TSGroupClass extends TargetSystemGroup> {
  private static final String DO_NOT_PROVISION_TO_ATTRIBUTE = "do_not_provision_to";

  private static final String PROVISION_TO_ATTRIBUTE = "provision_to";

  static final Logger STATIC_LOG = LoggerFactory.getLogger(Provisioner.class);
  
  protected final Logger LOG;

  // What should logs show for this provisioner's name. This differentiates between the
  // Incremental/Normal version of the provisioner and its full-sync version
  final public String provisionerDisplayName;

  // What config elements underpin this provisioner, both in grouper_loader.properties
  // and in Grouper Attributes. This will be the same for both the Incremental and the
  // full-sync provisioners
  final public String provisionerConfigName;
  
  // Cache groups by groupInfo key
  final GrouperCache<String, GrouperGroupInfo> grouperGroupInfoCache;
  
  // Cache subjects by sourceId__subjectId key
  final GrouperCache<String, Subject> grouperSubjectCache;

  // Cache TargetSystemUsers by Subject. This is typically a long-lived cache
  // used across provisioning batches. These are only fetched and cached
  // if our config has needsTargetSystemUsers=true.
  final PspDatedCache<Subject, TSUserClass> targetSystemUserCache;
  
  // This stores TargetSystemUserss during the provisioning batch. This Map might seem
  // redundant to the targetSystemUserCache, but it is needed for
  // two reasons: 1) To make sure items are not flushed during the provisioning batch
  // like they could be within a GrouperCache; 2) To know if a TSUserClass really
  // doesn't exist (getTargetSystemUser might do a lookup every time a nonexistent
  // user is requested. By using a map populated once per provisioning batch, we don't
  // have to do extra lookups during the batch).
  // If our config has needsTargetSystemUsers=true, this map is populated by 
  // startProvisioningBatch and emptied by finishProvisioningBatch
  private Map<Subject, TSUserClass> tsUserCache_shortTerm = new HashMap<Subject, TSUserClass>();
  
  
  // Cache TargetSystemGroups by Group. This is a long-lived cache, typically used across
  // several provisioning batches. These are only fetched and cached if our config
  // has needsTargetSystemGroups=true.
  final GrouperCache<GrouperGroupInfo, TSGroupClass> targetSystemGroupCache;
  
  // This stores TargetSystemGroups during the provisioning batch. This Map might seem
  // redundant to the targetSystemGroupCache, but it is needed for
  // two reasons: 1) To make sure items are not flushed during the provisioning batch
  // like they could be within a GrouperCache; 2) To know if a TSGroupClass really
  // doesn't exist (a GrouperCache doesn't differentiate between an uncached value and
  // a nonexistent group. By using a map populated once per provisioning batch, we don't
  // do futile lookups during the batch).
  //
  // This map is populated by startProvisioningBatch and emptied by finishProvisioningBatch
  // if our config has needsTargetSystemGroups=true.
  private Map<GrouperGroupInfo, TSGroupClass> tsGroupCache_shortTerm = new HashMap<GrouperGroupInfo, TSGroupClass>();

  // This is used during provisioning so everyone can get access to the current
  // work item while we're looping through the work items of a batch
  private ThreadLocal<ProvisioningWorkItem> currentWorkItem = new ThreadLocal<ProvisioningWorkItem>();


  // This is used to track what groups are selected for this provisioner and used
  // when groups are deleted (to know if they were selected before they were deleted)
  // or when attributes change (to know if new groups are selected or if old ones
  // are no longer selected)
  private AtomicReference<Set<GrouperGroupInfo>> selectedGroups = new AtomicReference<>();

  /**
   * Should this provisioner operate in Full-Sync mode? This might mean fetching all members of a group
   * which can be expensive in an incremental-sync, but is worth the trouble in a full-sync.
   */
  protected final boolean fullSyncMode;
  
  final protected ConfigurationClass config;

  final private ExecutorService tsUserFetchingService;

  // This is managed during incremental and full provisioning activities
  public static final ThreadLocal<Provisioner>  activeProvisioner = new ThreadLocal<>();

  
  Provisioner(String provisionerConfigName, ConfigurationClass config, boolean fullSyncMode) {
    this.provisionerConfigName = provisionerConfigName;

    if (fullSyncMode) {
      this.provisionerDisplayName = provisionerConfigName + "-full";
    } else {
      this.provisionerDisplayName = provisionerConfigName;
    }
    this.fullSyncMode = fullSyncMode;
    LOG = LoggerFactory.getLogger(String.format("%s.%s", getClass().getName(), provisionerDisplayName));

    this.config = config;

    checkAttributeDefinitions();

    // NOTE: PSPNG has different size=0 semantics... In PSPNG, size=0 means zero caching, but in
    // GrouperCache/EhCache size=0 means unlimited caching. Therefore, we map size=0 to size=1.
    grouperGroupInfoCache
            = new GrouperCache<String, GrouperGroupInfo>(String.format("PSP-%s-GrouperGroupInfoCache", getConfigName()),
            config.getGrouperGroupCacheSize() > 1 ? config.getGrouperGroupCacheSize() : 1,
            false,
            config.getDataCacheTime_secs(),
            config.getDataCacheTime_secs(),
            false);

    grouperSubjectCache
              = new GrouperCache<String, Subject>(String.format("PSP-%s-GrouperSubjectCache", getConfigName()),
              config.getGrouperSubjectCacheSize() > 1 ? config.getGrouperSubjectCacheSize() : 1,
              false,
              config.getDataCacheTime_secs(),
              config.getDataCacheTime_secs(),
              false);

    targetSystemUserCache
              = new PspDatedCache<Subject, TSUserClass>(String.format("PSP-%s-TargetSystemUserCache", getConfigName()),
              config.getTargetSystemUserCacheSize() > 1 ? config.getTargetSystemUserCacheSize() : 1,
              false,
              config.getDataCacheTime_secs(),
              config.getDataCacheTime_secs(),
              false);

    // This cache is set up with DisplayName of provisioner
    // to keep the caches of the FullSync and Incremental instances separate
    targetSystemGroupCache
      = new GrouperCache<GrouperGroupInfo, TSGroupClass>(String.format("PSP-%s-TargetSystemGroupCache", getDisplayName()),
          config.getTargetSystemGroupCacheSize() > 1 ? config.getTargetSystemGroupCacheSize() : 1,
          false,
          config.getDataCacheTime_secs(),
          config.getDataCacheTime_secs(),
          false);

    if ( config.needsTargetSystemUsers() ) {
      tsUserFetchingService = Executors.newFixedThreadPool(
              config.getNumberOfDataFetchingWorkers(),
              new ThreadFactory() {
                AtomicInteger counter = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                  return new Thread(r, String.format("TSUserFetcher-%s-%d", getDisplayName(), counter.getAndIncrement()));
                }
              });
    } else {
      tsUserFetchingService=null;
    }

    selectedGroups.set(getAllGroupsForProvisioner());
  }
  

  /**
   * This creates any attributes missing within the etc:pspng: folder.
   */
  private void checkAttributeDefinitions() {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    if ( grouperSession == null )
      grouperSession = GrouperSession.startRootSession();
    
    //GRP-1356: pspng should use the default configuration folder
    //String pspngManagementStemName = "etc:pspng";
    String pspngManagementStemName = GrouperConfig.retrieveConfig().propertyValueString(
        "grouper.rootStemForBuiltinObjects", "etc") + ":pspng";
    
    Stem pspngManagementStem = StemFinder.findByName(grouperSession, pspngManagementStemName, false);
    if (pspngManagementStem == null) {
      pspngManagementStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignDescription("Location for pspng-management objects.")
        .assignName(pspngManagementStemName).save();
    }

    //see if provision_to_def attributeDef is there
    String provisionToDefName = pspngManagementStemName + ":" + PROVISION_TO_ATTRIBUTE + "_def";
    AttributeDef provisionToDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
        provisionToDefName, false, new QueryOptions().secondLevelCache(false));
    if (provisionToDef == null) {
      provisionToDef = pspngManagementStem.addChildAttributeDef(PROVISION_TO_ATTRIBUTE + "_def", AttributeDefType.type);
      provisionToDef.setAssignToGroup(true);
      provisionToDef.setAssignToStem(true);
      provisionToDef.setMultiAssignable(true);
      provisionToDef.setValueType(AttributeDefValueType.string);
      provisionToDef.store();
    }
    
    //see if do_not_provision_to_def attributeDef is there
    String doNotProvisionToDefName = pspngManagementStemName + ":" + DO_NOT_PROVISION_TO_ATTRIBUTE + "_def";
    AttributeDef doNotProvisionToDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
        doNotProvisionToDefName, false, new QueryOptions().secondLevelCache(false));
    if (doNotProvisionToDef == null) {
      doNotProvisionToDef = pspngManagementStem.addChildAttributeDef(DO_NOT_PROVISION_TO_ATTRIBUTE+"_def", AttributeDefType.type);
      doNotProvisionToDef.setAssignToGroup(true);
      doNotProvisionToDef.setAssignToStem(true);
      doNotProvisionToDef.setMultiAssignable(true);
      doNotProvisionToDef.setValueType(AttributeDefValueType.string);
      doNotProvisionToDef.store();
    }
    
    GrouperCheckConfig.checkAttribute(pspngManagementStem, provisionToDef, PROVISION_TO_ATTRIBUTE, PROVISION_TO_ATTRIBUTE, "Defines what provisioners should process a group or groups within a folder", true);
    GrouperCheckConfig.checkAttribute(pspngManagementStem, doNotProvisionToDef, DO_NOT_PROVISION_TO_ATTRIBUTE, DO_NOT_PROVISION_TO_ATTRIBUTE, "Defines what provisioners should not process a group or groups within a folder. Since the default is already for provisioners to not provision any groups, this attribute is to override a provision_to attribute set on an ancestor folder. ", true);
  }


  /**
   * Action method that handles membership additions where a person-subject is added to a 
   * group. The top-level Provisioner class implementation is abstract, and, of course, 
   * this method is expected to be overridden by every provisioner subclass to accomplish 
   * something useful. 
   * 
   * @param grouperGroupInfo The group to which the subject needs to be added as a member
   * @param tsGroup A TSGroupClass created for group by fetchTargetSystemGroup. This will
   * be null for systems that do not need target system groups.
   * @param subject The (person) subject that needs to be provisioned as a member of 'group'
   * @param tsUser A TSUserClass created for the subject by fetchTargetSystemUser. This will
   * be null for systems that do not need target system users.
   */

  protected abstract void addMembership(GrouperGroupInfo grouperGroupInfo, TSGroupClass tsGroup,
      Subject subject, TSUserClass tsUser) throws PspException;
  
  
  /**
   * Abstract action method that handles membership removals. 
   * 
   * Note: This method is called for MembershipDelete events for a non-group member.
   * 
   * @param grouperGroupInfo The group to which the subject needs to be removed as a member
   * @param tsGroup TSGroupClass for the 'group.' This is null for systems that do not need
   * target-system group info
   * @param subject The subject that needs to be deprovisioned as a member of 'group'
   * @param tsUser TSUserClass for the 'subject.' This is null for systems that do not need
   * target-system user info
   */

  protected abstract void deleteMembership(GrouperGroupInfo grouperGroupInfo, TSGroupClass tsGroup,
      Subject subject, TSUserClass tsUser) throws PspException;

  
  /**
   * Provisioning a new Group in the target system. This must be overridden in provisioner
   * subclasses that support creating groups. This will normally be called (with an empty
   * members parameter) when provisioning-enabled groups are created in Grouper. However, 
   * when an empty group cannot be created (eg, an ldap group that _requires_ members) or when
   * members are somehow already known, this  may be called with a (non-empty) list of members.
   * 
   * @param grouperGroup
   * @param initialMembers What members should in the provisioned group once the method completes. 
   * This is generally empty during incremental/changelog-based provisioning, but may list users 
   * at other times.
   * @return
   * @throws PspException
   */
  protected abstract TSGroupClass createGroup(GrouperGroupInfo grouperGroup, Collection<Subject> initialMembers) throws PspException;

  /**
   * Action method that handles group removal. The top-level Provisioner class implementation
   * does nothing except log an error if the target system needs groups.
   * 
   * This is expected to be overridden by subclasses if the target system needs groups, and
   * do not call the super.deleteGroup version of this when you override it this
   * @param grouperGroupInfo
   * @param tsGroup 
   */
  protected abstract void 
  deleteGroup(GrouperGroupInfo grouperGroupInfo, TSGroupClass tsGroup) throws PspException;
  
  /**
   * This method's responsibility is to make sure that group's only provisioned memberships are those
   * of correctSubjects. Extra subjects should be removed. 
   * 
   * Before this is called, the following have occurred:
   *   -a ProvisioningWorkItem was created representing the whole Full Sync, and it was marked
   *    as the current provisioning item
   *   -StartProvisioningBatch was called
   *   -TSGroupClass- and TSUserClass-caches are populated with the group and CORRECT Subjects
   *   
   * Also, remember that fullSyncMode=true for provisioners doing full-sync, so TargetSystemUsers and
   * TargetSystemGroups should have the extra information needed to facilitate full syncs.
   * 
   * @param grouperGroupInfo Grouper group to fully synchronize with target system
   * @param tsGroup TSGroupClass that maps to group.
   * @param correctSubjects What subjects are members in the Grouper Registry
   * @param tsUserMap Map of TargetSystemUsers which map to the correctSubjects. This will be empty
   * for provisioners that do not use TargetSystemUsers.
   * @param correctTSUsers A list of the TSUsers that correspond to correctSubjects. This might be a subset
   * of the TSUsers in the tsUserMap.
   * @param stats A holder of the number of changes the fullSync performs
   * @return True when changes were made to target system
   */
  protected abstract boolean doFullSync(
      GrouperGroupInfo grouperGroupInfo, TSGroupClass tsGroup, 
      Set<Subject> correctSubjects, Map<Subject, TSUserClass> tsUserMap, Set<TSUserClass> correctTSUsers,
      JobStatistics stats)
          throws PspException;

  /**
   * This method's responsibility is find extra groups within Grouper's responsibility that
   * exist in the target system. These extra groups should be removed.
   * 
   * Note: This is only called when grouperIsAuthoritative=true in the provisioner's properties.
   * 
   * To avoid deleting newly created groups, implementations should follow the following:
   *   1) Read all the provisioned information
   *   2) Read all the groups that should be provisioned (getAllGroupsForProvisioner())
   *   3) Fetch group information from system (fetchTargetSystemGroupsInBatches(allGroupsForProvisioner)
   *      (if TSGroup objects are relevant)
   *   4) Compare (1) & (3) and delete anything extra in (1)
   * 
   */
  protected abstract void doFullSync_cleanupExtraGroups(JobStatistics stats) throws PspException;
  

  /**
   * This fetches group information from the target system. Subclasses that have TSGroupClass 
   * information need to override this. Subclasses that do not need TSGroupClass information
   * should just return either an empty map (Collections.EMPTY_MAP) or null;. 
   * 
   * Note:
   * The signature of this method is designed for batch fetching. If you cannot fetch batches of
   * information, then loop through the provided groups and build a resulting map.
   * @param grouperGroups
   * @return
   * @throws PspException
   */
  protected abstract Map<GrouperGroupInfo, TSGroupClass> 
  fetchTargetSystemGroups(Collection<GrouperGroupInfo> grouperGroups) throws PspException;


  /**
   * This fetches user information from the target system. Subclasses that have TSUserClass 
   * information need to override this. Subclasses that do not have TSUserClass should implement
   * this so it returns either an empty map (Collections.EMPTY_MAP) or null.
   * 
   * Note:
   * The signature of this method is designed for batch fetching. If you cannot fetch batches of
   * information, then loop through the provided users and build a resulting map.
   * 
   * @param personSubjects 
   * @return
   * @throws PspException
   */
  protected abstract Map<Subject, TSUserClass> 
  fetchTargetSystemUsers(Collection<Subject> personSubjects) throws PspException;


  /**
   * This method returns the work items that are supposed to be provisioned
   * by calling shouldGroupBeProvisioned on each group mentioned
   * by a workItem. If a workItem's group is within the scope of this provisioner
   * or if the workItem is not related to a group, then it is included in the
   * returned list of work items that should be processed further. Otherwise, it
   * is marked as completed and not returned.
   * 
   * If the workItem is not to be processed, 
   * @param workItems WorkItems read from the triggering source (Changelog or messaging).
   * This will include both events that affect groups and those that do not. Generally, we
   * pass on non-group changes in case a provisioner wants to process them. 
   * @return The list of workItems that are to be provisioned
   * @throws PspException
   */
  public List<ProvisioningWorkItem> 
  filterWorkItems(List<ProvisioningWorkItem> workItems) throws PspException {
    List<ProvisioningWorkItem> result = new ArrayList<ProvisioningWorkItem>();
    
    LOG.debug("Filtering provisioning batch of {} items", workItems.size());

    for ( ProvisioningWorkItem workItem : workItems ) {
      GrouperGroupInfo group = workItem.getGroupInfo(this);

      // Groups that haven't been deleted: Skip them if they're not supposed to be provisioned
      if ( group != null ) {
        if ( !group.hasGroupBeenDeleted() && !shouldGroupBeProvisioned(group)) {
          workItem.markAsSkipped("Ignoring work item because (existing) group should not be provisioned");
          continue;
        }

        if ( group.hasGroupBeenDeleted() && !selectedGroups.get().contains(group) ) {
          workItem.markAsSkippedAndWarn("Ignoring work item because (deleted) group was not provisioned before it was deleted");
          continue;
        }
      }

      if ( shouldWorkItemBeProcessed(workItem) ) {
        result.add(workItem);
      } else {
        // Not going to process this item, so mark it as a success and don't add it to result
          workItem.markAsSkipped("Ignoring work item");
      }
    }
    
    return result;
  }

  /**
   * Used to filter workItems. This can be overridden by a subclass that was unhappy
   * with the default filter behaviors. If overriding this, eg, to accept additional
   * changes into a provisioner, then it would probably be useful to look for those
   * additional types of changes and then call the super version of this.
   *
   * Note: In addition to returning a boolean about skipping the work item, it is helpful
   * if this method actually calls markAsSkipped(reason) so that a more detailed
   * reason can be noted for skipping it. In other words, this method has the
   * (optional) side effect of actually marking the item as skipped. Finally, if this
   * method does _not_ mark the item as skipped, it will be marked as skipped
   * with a generic message.
   *
   * @param workItem
   * @return
   */
  protected boolean shouldWorkItemBeProcessed(ProvisioningWorkItem workItem) {
    if ( workItem.isSubjectUnresolvable(this) ) {
      workItem.markAsSkippedAndWarn("Subject is unresolvable");
      return false;
    }

    // Check if we're configured to ignore changes to internal (g:gsa) subjects
    // (default is that we do ignore such changes)
    if ( getConfig().areChangesToInternalGrouperSubjectsIgnored() ) {
      Subject subject = workItem.getSubject(this);
      if ( subject != null && subject.getSourceId().equalsIgnoreCase("g:gsa") ) {
        workItem.markAsSkipped("Ignoring event about a g:gsa subject");
        return false;
      }
    }

    // Only the actual group-deletions are processed for groups that have been deleted
    // This skips all the membership-removal changelog entries that precede the
    // DElETE changelog entry
    if ( workItem.getGroupInfo(this) != null &&
         workItem.getGroupInfo(this).hasGroupBeenDeleted() ) {
      if (workItem.matchesChangelogType(ChangeLogTypeBuiltin.GROUP_DELETE) ) {
        LOG.debug("{}: Group has been deleted within grouper, so we're processing group-deletion event: {}", getDisplayName(), workItem);
      } else {
        workItem.markAsSkipped("Group has been deleted within Grouper. Skipping event because it is not the actual GROUP_DELETE event");
        return false;
      }
    }

    if ( !workItem.matchesChangelogType(ChangelogHandlingConfig.allRelevantChangelogTypes) ) {
      workItem.markAsSkipped("Changelog type is not relevant to PSPNG provisioning (see ChangelogHandlingConfig.allRelevantChangelogTypes)");
      return false;
    }

    return true;
  }


  /**
   * Lock the groups that we are about to process. This will prevent simultaneous
   * activity on them, which reduces the opportunities for duplicate provisioning operations
   * that result in spurious error messages.
   *
   * @param workItems
   */
  public void startCoordination(List<ProvisioningWorkItem> workItems) {
    for (ProvisioningWorkItem workItem : workItems) {
      GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);
      if (grouperGroupInfo == null)
        // Nothing to do before batch is processed
        continue;

      if (isFullSyncMode()) {
        getProvisionerCoordinator().lockForFullSyncIfNoIncrementalIsUnderway(grouperGroupInfo);
      } else {
        getProvisionerCoordinator().lockForIncrementalProvisioningIfNoFullSyncIsUnderway(grouperGroupInfo);
      }
    }
  }

  /**
   * Provisioning is over. Time to unlock in order to allow other full- or incremental-sync to
   * occur on them
   * @param workItems
   * @param wasSuccessful
   */
  public void finishCoordination(List<ProvisioningWorkItem> workItems, boolean wasSuccessful) {

    for ( ProvisioningWorkItem workItem : workItems ) {
      GrouperGroupInfo groupInfo = workItem.getGroupInfo(this);
      if ( groupInfo != null ) {
        if ( isFullSyncMode() ) {
          getProvisionerCoordinator().unlockAfterFullSync(groupInfo, wasSuccessful);
        }
        else {
          getProvisionerCoordinator().unlockAfterIncrementalProvisioning(groupInfo);
        }
      }
    }
  }


  /**
   * Get ready for a provisioning batch. If this is overridden, make sure you call super()
   * at the beginning of your overridden version.
   * 
   * @param workItems
   * @throws PspException
   */
  public void startProvisioningBatch(List<ProvisioningWorkItem> workItems) throws PspException {
    Provisioner.activeProvisioner.set(this);
    LOG.info("Starting provisioning batch of {} items", workItems.size());

    warnAboutCacheSizeConcerns();

    DateTime newestAsOfDate=null;
    for ( ProvisioningWorkItem workItem : workItems) {
      LOG.debug("-->Work item: {}", workItem);

      if ( workItem.asOfDate!=null ) {
        if ( workItem.groupName != null ) {
          DateTime lastFullSync = getFullSyncer().getLastSuccessfulFullSyncDate(workItem.groupName);
          if ( lastFullSync!=null && lastFullSync.isAfter(workItem.asOfDate) ) {
            workItem.markAsSkipped("Change was covered by full sync: {}",
                    PspUtils.formatDate_DateHoursMinutes(lastFullSync,null));
            continue;
          }
        }
        // Is the workItem's asOfDate newer?
        if (newestAsOfDate == null || newestAsOfDate.isBefore(workItem.asOfDate)) {
          newestAsOfDate = workItem.asOfDate;
        }
      }
    }

    LOG.info("Information cached before {} will be ignored", newestAsOfDate);

    Set<Subject> subjects = new HashSet<Subject>();

    // Use this Set to remove duplicate group names that are referenced in multiple workItemsß
    Set<GrouperGroupInfo> grouperGroupInfos = new HashSet<GrouperGroupInfo>();

    for ( ProvisioningWorkItem workItem : workItems) {
      GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);
      if ( grouperGroupInfo == null ) {
        // Nothing to do before batch is processed
        continue;
      }

      grouperGroupInfos.add(grouperGroupInfo);

      Subject s = workItem.getSubject(this);
      if ( s != null )
        subjects.add(s);
    }
    
    prepareGroupCache(grouperGroupInfos);
    prepareUserCache(subjects, newestAsOfDate);
  }

  protected void warnAboutCacheSizeConcerns() {
    warnAboutCacheSizeConcerns(grouperGroupInfoCache.getStats().getObjectCount(), config.getGrouperGroupCacheSize(), "grouper groups", "grouperGroupCacheSize");
    warnAboutCacheSizeConcerns(targetSystemGroupCache.getStats().getObjectCount(), config.getTargetSystemGroupCacheSize(), "provisioned groups", "targetSystemGroupCacheSize");
    warnAboutCacheSizeConcerns(grouperSubjectCache.getStats().getObjectCount(), config.getGrouperSubjectCacheSize(), "grouper subjects", "grouperSubjectCacheSize");
    warnAboutCacheSizeConcerns(targetSystemUserCache.getStats().getObjectCount(), config.getTargetSystemUserCacheSize(), "provisioned subjects", "targetSystemUserCacheSize");
  }

  private void warnAboutCacheSizeConcerns(long cacheObjectCount, int configuredSize, String objectType, String configurationProperty) {
    if ( !config.areCacheSizeWarningsEnabled() )
      return;

    // If caching is disabled, then don't warn about it!!
    if ( configuredSize <= 1 ) {
      return;
    }

    double cacheWarningThreshold_percentage = config.getCacheFullnessWarningThreshold_percentage();
    long cacheWarningTreshold_count = Math.round(cacheWarningThreshold_percentage*configuredSize);

    long cacheFullness_percentage = Math.round(100.0*cacheObjectCount/configuredSize);

    if ( cacheFullness_percentage >= cacheWarningThreshold_percentage ) {
      LOG.warn("{}: Cache of {} is very full ({}%). Provisioning performance is much better if {} is big enough to hold all {}. " +
                      "({} is currently set to {}, and this warning occurs when cache is {}% full)",
              getConfigName(),
              objectType,
              cacheFullness_percentage,
              configurationProperty,
              objectType,
              configurationProperty, configuredSize,
              cacheFullness_percentage);
    } else {
      LOG.debug("{}: Cache of {} is sufficiently sized ({}% full) (property {}={})",
              getConfigName(), objectType, cacheFullness_percentage, configurationProperty, configuredSize);
    }

  }

  private ProvisionerCoordinator getProvisionerCoordinator() {
    return ProvisionerFactory.getProvisionerCoordinator(this);
  }

  // Finish and/or clean up after a provisioning batch. If this is overridden, make sure you 
  // call super() at the END of your overridden version
  public void finishProvisioningBatch(List<ProvisioningWorkItem> workItems) throws PspException {
    tsUserCache_shortTerm.clear();
    tsGroupCache_shortTerm.clear();

    LOG.debug("Done with provisining batch");
  }

  /**
   * Make a JexlMap that contains subject and group information and evaluate the given
   * expression.
   *
   * @param expressionName
   * @param expression
   * @param subject
   * @param tsUser
   * @param grouperGroupInfo
   * @param tsGroup
   * @param keysAndValues Key/Value pairs that will also be available within the Jexl's variable map
   * @return
   */
  protected final String evaluateJexlExpression(String expressionName, String expression,
      Subject subject, TSUserClass tsUser,
      GrouperGroupInfo grouperGroupInfo, TSGroupClass tsGroup,
      Object... keysAndValues) throws PspException {
    
    LOG.trace("Evaluating {} Jexl expression: {}", expressionName, expression);
    
    Map<String, Object> variableMap = new HashMap<>();

    variableMap.put("utils", new PspJexlUtils());

    // Copy any provided keys and values into the map
    GrouperUtil.assertion(keysAndValues.length % 2 == 0, "KeysAndValues must be paired evenly");
    for (int i=0; i<keysAndValues.length; i+=2)
      variableMap.put(keysAndValues[i].toString(), keysAndValues[i+1]);

    // Give provisioner subclasses to add information
    populateJexlMap(variableMap, 
        subject, 
        tsUser,
        grouperGroupInfo, 
        tsGroup);
    
    // Give our config a chance to add information
    config.populateElMap(variableMap);
    
    try {
      // In order to support nested expressions, we're going to repeatedly look for atomic (non-nested) expressions
      // and replace each atomic ${...} with it's evaluation result until there are no more ${ in the string
      // This kind of regular expression problem is discussed here:
      // https://stackoverflow.com/questions/717644/regular-expression-that-doesnt-contain-certain-string

      Pattern atomicExpressionPattern = Pattern.compile("\\$\\{([^$]|\\$[^{])*?\\}" );
      String result=expression;
      Matcher atomicExpressionMatcher = atomicExpressionPattern.matcher(result);

      while ( atomicExpressionMatcher.find()) {
        String atomicExpression = atomicExpressionMatcher.group();
        String atomicExpressionResult;

        // Check to see if expression has a backup expression
        //   xyz:-pdq ==> evaluate pdq if xyz cannot be evaluated
        if ( ! atomicExpression.contains(":-") ) {
          atomicExpressionResult = GrouperUtil.substituteExpressionLanguage(atomicExpression, variableMap, true, false, false);
        }
        else {
          // Split atomicExpression on :- and add } & { to the first and second pieces

          String expressionOne = StringUtils.substringBefore(atomicExpression, ":-") + "}";
          String expressionTwo = "${" + StringUtils.substringAfter(atomicExpression, ":-");

          try {
            atomicExpressionResult = GrouperUtil.substituteExpressionLanguage(expressionOne, variableMap, true, false, false);
          } catch (RuntimeException e) {
            LOG.warn("{}: Problem evaluating '{}'. Will try :- expression '{}': {}",
                    new Object[]{expressionName, expressionOne, expressionTwo, e.getMessage()});


            atomicExpressionResult = GrouperUtil.substituteExpressionLanguage(expressionTwo, variableMap, true, false, false);
          }
        }
        LOG.debug("Evaluated {} Jexl expression: '{}'", expressionName, atomicExpressionResult);
        LOG.trace("Evaluated {} Jexl expression: '{}' FROM {} WITH variables {}",
                new Object[]{expressionName, atomicExpressionResult, atomicExpression, variableMap});

        // replaceFirst unescapes the string it is given (because it isn't a string literal, but can also
        // refer to regex groupings: $1 refers to the first matching group in the Pattern. In order to get a $ in the
        // replacement, you need to use \$. We're not using groups in this stuff, but we need to keep \, exactly
        // as it is; therefore, We need to double-escape atomicExpressionResult... Yuck

        // This is even more confusing because replaceAll takes a regex and a string literal:
        //   the first \\\\ is a regex expression for a single \
        //     (java strings need \\ to make a \ and the regex needs \\ to make a single \)
        //   and the second \\\\\\\\ (8 whacks) is a substitution string resulting in  two \\
        result = atomicExpressionMatcher.replaceFirst(atomicExpressionResult.replaceAll("\\\\", "\\\\\\\\"));
        atomicExpressionMatcher = atomicExpressionPattern.matcher(result);
      }

      if ( expression.equals(result) ) {
        LOG.trace("{} Jexl expression did not include any substitutions: {}", expressionName, expression);
      } else {
        LOG.debug("Evaluated entire {} Jexl expression: '{}'", expressionName, result);
      }

      return result;
    }
    catch (RuntimeException e) {
      LOG.error("Jexl Expression {} '{}' could not be evaluated for subject '{}/{}' and group '{}/{}' which used variableMap '{}'",
          new Object[] {expressionName, expression,
              subject, tsUser,
              grouperGroupInfo, tsGroup,
              variableMap, e});
      throw new PspException("Jexl evaluation failed: %s", e.getMessage());
    }
  }

  
  /**
   * Overridable method to put group and subject information into the Jexl map
   * for use during evaluation.
   * 
   * If you override this, make sure to call super().populateElMap so the standard 
   * mappings will be included in addition to those you're adding.
   * @param variableMap Map that will eventually be provided in Jexl evalutions
   * @param subject
   * @param tsUser
   * @param grouperGroupInfo
   * @param tsGroup
   */
  protected void populateJexlMap(Map<String, Object> variableMap, Subject subject, 
      TSUserClass tsUser, GrouperGroupInfo grouperGroupInfo, TSGroupClass tsGroup) {
    variableMap.put("provisionerType", getClass().getSimpleName());
    variableMap.put("provisionerName", getDisplayName());

    if ( subject != null ) 
      variableMap.put("subject", subject);
      
    if ( tsUser != null )
        variableMap.put("tsUser",  tsUser.getJexlMap());
    
    if ( grouperGroupInfo != null ) {
      Map<String, Object> groupMap = getGroupJexlMap(grouperGroupInfo);
      variableMap.putAll(groupMap);
    }
      
    if ( tsGroup != null ) 
      variableMap.put("tsGroup", tsGroup.getJexlMap());
  }

  
  
  /**
   * This makes sure all the Subjects are in tsUserCache_shortTerm. Of
   * course if our config says needsTargetSystemUsers is False, then the 
   * tsUserCache_shortTerm will always be empty.
   * 
   * @param subjects
   * @throws PspException
   */
  private void prepareUserCache(Set<Subject> subjects, DateTime oldestCacheTimeAllowed) throws PspException {
    LOG.debug("Starting to cache user information for {} items", subjects.size());
    tsUserCache_shortTerm.clear();
    
    // Nothing to do if TargetSystemUsers are not used by this provisioner
    if ( ! config.needsTargetSystemUsers() || subjects.size()==0 )
      return;
    Collection<Subject> subjectsToFetch = new ArrayList<Subject>();
    
    for (Subject s : subjects) {
      // Skip group subjects (source=g:gsa)
      if ( s.getSourceId().equals("g:gsa") )
        continue;
      
      // See if the subject is already cached.
      TSUserClass cachedTSU = targetSystemUserCache.get(s, oldestCacheTimeAllowed);
      if ( cachedTSU != null )
        // Cache user in shortTerm cache as well as refresh it in longterm cache
        cacheUser(s, cachedTSU);
      else
        subjectsToFetch.add(s);
    }

    LOG.info("{} out of {} subjects were found in cache ({}%)",
            tsUserCache_shortTerm.size(), subjects.size(),
            (int) 100.0*tsUserCache_shortTerm.size()/subjects.size());

    if ( subjectsToFetch.size() == 0 )
      return;

    ProgressMonitor fetchingProgress = new ProgressMonitor(subjectsToFetch.size(), LOG, false, 15, "Fetching subjects");

    List<List<Subject>> batchesOfSubjectsToFetch = PspUtils.chopped(subjectsToFetch, config.getUserSearch_batchSize());

    List<Future<Map<Subject, TSUserClass>>> futures = new ArrayList<>();

    // Submit these batches to the Fetching ExecutorService and get Futures back
    for (final List<Subject> batchOfSubjectsToFetch : batchesOfSubjectsToFetch ) {

      // This essentially calls fetchTargetSystemUsers on the batch and returns
      // a Map<Subject, TSUserClass>

      Future<Map<Subject, TSUserClass>> future = tsUserFetchingService.submit(
              new Callable<Map<Subject, TSUserClass>>() {
                @Override
                public Map<Subject, TSUserClass> call() throws Exception {
                  
                  GrouperSession grouperSession = GrouperSession.startRootSession();
                  GrouperContext grouperContext = GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);

                  Provisioner.activeProvisioner.set(Provisioner.this);
                  Map<Subject, TSUserClass> fetchedData;
                  try {
                    fetchedData = fetchTargetSystemUsers(batchOfSubjectsToFetch);
                  } catch (PspException e1) {
                    LOG.warn("Batch-fetching subject information failed. Trying fetching information for each subject individually", e1);
                    // Batch-fetching failed. Let's see if we can narrow it down to a single
                    // Subject
                    fetchedData = new HashMap<>();
                    for (Subject subject : batchOfSubjectsToFetch) {
                      try {
                        TSUserClass tsUser = fetchTargetSystemUser(subject);
                        fetchedData.put(subject, tsUser);
                      } catch (PspException e2) {
                        LOG.error("Problem fetching information about subject '{}'", subject, e2);
                        throw new RuntimeException("Problem fetching information on subject " + subject + ": " + e2.getMessage());
                      }
                    }
                  } finally {
                    GrouperSession.stopQuietly(grouperSession);
                    GrouperContext.deleteDefaultContext();
                  }
                  return fetchedData;
                }
              });

      futures.add(future);
    }

    // Gather up the futures until there are no more
    while (!futures.isEmpty()) {
      Iterator<Future<Map<Subject, TSUserClass>>> futureIterator = futures.iterator();

      while (futureIterator.hasNext()) {
        Future<Map<Subject, TSUserClass>> future = futureIterator.next();

        // Process (and remove) any future that has completed its fetching
        if ( future.isDone() ) {
          Map<Subject, TSUserClass> fetchedData;
          try {
            fetchedData = future.get();
          } catch (InterruptedException e) {
            LOG.error("Problem fetching information on subjects", e);

            throw new RuntimeException("Problem fetching information on subjects: " + e.getMessage());
          } catch (ExecutionException e) {
            LOG.error("Problem fetching information on subjects", e);

            throw new RuntimeException("Problem fetching information on subjects: " + e.getMessage());
          }

          // Save the fetched data in our cache
          for ( Entry<Subject, TSUserClass> subjectInfo : fetchedData.entrySet() )
            cacheUser(subjectInfo.getKey(), subjectInfo.getValue());

          fetchingProgress.workCompleted(fetchedData.size());
          futureIterator.remove();
        }
      }
    }
    fetchingProgress.completelyDone("Success");

    // CREATE MISSING TARGET SYSTEM USERS (IF ENABLED)
    // Go through the subjects and see if any of them were not found above. 
    // If user-creation is enabled, just create the
    // user in the target system and add it to our caches
    for ( Subject subj : subjects ) {
      if ( !tsUserCache_shortTerm.containsKey(subj) ) {
        if ( config.isCreatingMissingUsersEnabled() ) {
          TSUserClass newTSUser = createUser(subj);
          if ( newTSUser != null )
            cacheUser(subj, newTSUser);
        }
        else
          LOG.warn("{}: User not found in target system: {}", getDisplayName(), subj.getId());
      }
    }
  }

 
  /**
   * This makes sure all the Groups referenced by groupInfoSet are in groupMap_shortTerm. 
   * If our config says needsTargetSystemGroups is False, then the groupMap will
   * be empty.
   *
   * Note: This will create missing groups
   * 
   * @param grouperGroupInfos
   * @throws PspException
   */
  private void prepareGroupCache(Collection<GrouperGroupInfo> grouperGroupInfos) throws PspException {
	// Remove any duplicate group info objects
	Set<GrouperGroupInfo> groupInfoSet = new HashSet<>(grouperGroupInfos);
	
    LOG.debug("Starting to cache group information for {} items", groupInfoSet.size());
    tsGroupCache_shortTerm.clear();
    
    // If the target system doesn't need groups, then we obviously don't need to 
    // fetch and cache them
    if ( ! config.needsTargetSystemGroups() )
      return;
    
    Collection<GrouperGroupInfo> groupsToFetch = new ArrayList<GrouperGroupInfo>();

    for (GrouperGroupInfo grouperGroupInfo : groupInfoSet) {
      // See if the group is already cached.
      TSGroupClass cachedTSG = targetSystemGroupCache.get(grouperGroupInfo);
      if (cachedTSG != null) {
        // Cache group in shortTerm cache as well as refresh it in longterm cache
        cacheGroup(grouperGroupInfo, cachedTSG);
      }
      else {
        groupsToFetch.add(grouperGroupInfo);
      }
    }

    Map<GrouperGroupInfo, TSGroupClass> fetchedData = fetchTargetSystemGroupsInBatches(groupsToFetch);
    // Save the data that was fetched in our cache
    for ( Entry<GrouperGroupInfo, TSGroupClass> grouperGroupInfo : fetchedData.entrySet() )
      cacheGroup(grouperGroupInfo.getKey(), grouperGroupInfo.getValue());

    // If empty groups are supported, then look for groups that were not found in target
    // system and create them
    //
    // Note: If empty groups are not supported, they will be created later... when their
    // first membership is provisioned
    if ( config.areEmptyGroupsSupported() ) {
      for (GrouperGroupInfo grouperGroupInfo : groupsToFetch) {
        if (!tsGroupCache_shortTerm.containsKey(grouperGroupInfo) &&
            shouldGroupBeProvisioned(grouperGroupInfo))
        {
          // Group does not already exist so create it
          TSGroupClass tsGroup = createGroup(grouperGroupInfo, new ArrayList<Subject>());
          cacheGroup(grouperGroupInfo, tsGroup);
        }
      }
    }

  }

  /**
   * This method fetches an arbitrary number of groups from the target system. The configuration
   * of the system defines a maximum-batch-fetch size (config.getGroupSearch_batchSize()), and
   * this method breaks the given groups into appropriately-sized batches.
   *
   * @param groupsToFetch
   * @throws PspException
   */
  public Map<GrouperGroupInfo, TSGroupClass> fetchTargetSystemGroupsInBatches(Collection<GrouperGroupInfo> groupsToFetch) throws PspException {
    Map<GrouperGroupInfo, TSGroupClass> result = new HashMap<>(groupsToFetch.size());

    List<List<GrouperGroupInfo>> batchesOfGroupsToFetch = PspUtils.chopped(groupsToFetch, config.getGroupSearch_batchSize());

    for ( List<GrouperGroupInfo> batchOfGroupsToFetch : batchesOfGroupsToFetch ) {
      Map<GrouperGroupInfo, TSGroupClass> fetchedData;

      try {
        fetchedData = fetchTargetSystemGroups(batchOfGroupsToFetch);
        result.putAll(fetchedData);
      }
      catch (PspException e1) {
        LOG.warn("Batch-fetching group information failed. Trying to fetch information for each group individually", e1);
        // Batch-fetching failed. Let's see if we can narrow it down to a single
        // Group
          for ( GrouperGroupInfo grouperGroupInfo : batchOfGroupsToFetch ) {
            try {
              TSGroupClass tsGroup = fetchTargetSystemGroup(grouperGroupInfo);
              cacheGroup(grouperGroupInfo, tsGroup);
            }
            catch (PspException e2) {
              LOG.error("Problem fetching information on group '{}'", grouperGroupInfo, e2);
              throw new RuntimeException("Problem fetching information on group " + grouperGroupInfo);
            }
          }
      }
    }

    return result;
  }


  public TSUserClass getTargetSystemUser(Subject subject) throws PspException {
    GrouperUtil.assertion(config.needsTargetSystemUsers(),
            String.format("%s: system doesn't need target-system users, but one was requested", getDisplayName()));
    
    TSUserClass result = tsUserCache_shortTerm.get(subject);
    
    if ( result == null ) {
      if ( config.isCreatingMissingUsersEnabled() ) {
        result=createUser(subject);
        cacheUser(subject, result);
      }
      else {
        LOG.warn("{}: user is missing and user-creation is not enabled ({})", getDisplayName(), subject.getId());
      }
    }
    
    return result;
  }
  
  
  /**
   * Store Subject-->TSUserClass mapping in long-term and short-term caches
   * @param subject
   * @param newTSUser
   */
  private void cacheUser(Subject subject, TSUserClass newTSUser) {
    LOG.debug("Adding target-system user to cache: {}", subject);
    targetSystemUserCache.put(subject, newTSUser);
    tsUserCache_shortTerm.put(subject, newTSUser);
  }


  /**
   * Store Group-->TSGroupClass mapping in long-term and short-term caches
   * @param grouperGroupInfo
   * @param newTSGroup
   */
  protected void cacheGroup(GrouperGroupInfo grouperGroupInfo, TSGroupClass newTSGroup) {
    if ( newTSGroup != null ) {
      LOG.debug("Adding target-system group to cache: {}", grouperGroupInfo);
      targetSystemGroupCache.put(grouperGroupInfo, newTSGroup);
      tsGroupCache_shortTerm.put(grouperGroupInfo, newTSGroup);
    } else {
      if ( targetSystemGroupCache.containsKey(grouperGroupInfo) ||
           tsGroupCache_shortTerm.containsKey(grouperGroupInfo) ) {
        LOG.debug("Removing target-system group from cache: {}", grouperGroupInfo);
        targetSystemGroupCache.remove(grouperGroupInfo);
        tsGroupCache_shortTerm.remove(grouperGroupInfo);
      } else {
        LOG.debug("No target-system group to cache: {}", grouperGroupInfo);
      }
    }
  }
  
  
  /**
   * The specified Grouper or TargetSystem group has changed, remove
   * them from various caches, including hibernate L2 cache.
   *
   * Only one parameter is needed
   * @param grouperGroupInfo
   * @param tsGroup
   */
  protected void  uncacheGroup(GrouperGroupInfo grouperGroupInfo, TSGroupClass tsGroup) {
    // If the caller only knew the TargetSystem Group and didn't know what Grouper Group to flush,
    // let's see if we can find it
    if ( grouperGroupInfo == null && tsGroup != null ) {
      for (GrouperGroupInfo gi : targetSystemGroupCache.keySet())
        if (targetSystemGroupCache.get(gi) == tsGroup) {
          grouperGroupInfo = gi;
          break;
        }

      if ( grouperGroupInfo == null ) {
        LOG.warn("Can't find Grouper Group to uncache from tsGroup {}", tsGroup);
        return;
      }
    }

    LOG.debug("Flushing group from target-system cache: {}", grouperGroupInfo);
    targetSystemGroupCache.remove(grouperGroupInfo);

    LOG.debug("Flushing group from pspng group-info cache: {}", grouperGroupInfo.getName());
    grouperGroupInfoCache.remove(grouperGroupInfo.getName());

    grouperGroupInfo.hibernateRefresh();
  }


  /**
   * This removes all Group information from our caches
   */
  protected void uncacheAllGroups() {

    for (GrouperGroupInfo g : grouperGroupInfoCache.values()) {
      uncacheGroup(g, null);
    }
  }


  /**
   * Lookup a single TSUserClass for a single Subject. If you have several such mappings to look up,
   * you should use the (plural version) fetchTargetSystemUsers( ) instead, as that will have an opportunity 
   * to do faster batch fetching.
   * 
   * Note: This is final. Systems that have/need TargetSystemUsers need to override fetchTargetSystemUsers.
   * 
   * @param personSubject
   * @return
   * @throws PspException
   */
  protected final TSUserClass fetchTargetSystemUser(Subject personSubject) throws PspException {
    // Forward this singluar subject to the multi-subject version.
    Map<Subject, TSUserClass> result = fetchTargetSystemUsers(Arrays.asList(personSubject));
    return result.get(personSubject);
  }
  
  /**
   * Lookup a single TSGroupClass for a single (grouper) Group. If you have several such mappings to look up,
   * you should use the (plural version) fetchTargetSystemGroups( ) instead, as that will have an opportunity to do 
   * faster batch fetching.
   * 
   * Note: This is final. Systems that have/need TargetSystemGroups need to override fetchTargetSystemGroups.
   * 
   * @param grouperGroup
   * @return
   * @throws PspException
   */
  protected final TSGroupClass fetchTargetSystemGroup(GrouperGroupInfo grouperGroup) throws PspException {
    // Forward this singluar Group to the multi-subject version.
    Map<GrouperGroupInfo, TSGroupClass> result = fetchTargetSystemGroups(Arrays.asList(grouperGroup));
    return result.get(grouperGroup);
  }
  
  /**
   * Provisioning a new User account in the target system. This must be overridden in provisioner
   * subclasses that support creating user accounts. This is more used for provisioning testing, but
   * can be supported in production when provisioning can be done with just the information stored
   * in a grouper Subject.
   * 
   * @param personSubject
   * @return
   * @throws PspException
   */
  protected TSUserClass createUser(Subject personSubject) throws PspException {
    return null;
  }
  
  /**
   * Dispatches an event to the right method, with generally
   * useful parameters. 
   * 
   * There is no need to override this method when implementing basic provisioning 
   * (add/delete groups and members), but a Provisioner subclasses might override this 
   * to capture other CategoryAndActions combinations. 
   * 
   * @param workItem
   */
  protected void provisionItem(ProvisioningWorkItem workItem) throws PspException {
    LOG.debug("Starting provisioning of item: {}", workItem);
    
    currentWorkItem.set(workItem);
    ChangeLogEntry entry = workItem.getChangelogEntry();
    String workItem_identifier = null;
    if ( workItem.getChangelogEntry()!=null ) {
      workItem_identifier = Long.toString(workItem.getChangelogEntry().getSequenceNumber());
    }

    try {
      if ( workItem.matchesChangelogType(ChangelogHandlingConfig.changelogTypesThatAreHandledIncrementally) ) {
        processIncrementalSyncEvent(workItem);
        return;
      }

      if ( workItemMightChangeGroupSelection(workItem) ) {
        processAnyChangesInGroupSelection(workItem);
      }

      // If this is a specific group and is still selected for provisioning,
      // then we need to do a full sync of that group in case an attribute or
      // another non-membership aspect of the group changed
      if ( workItem.getGroupInfo(this) != null &&
                selectedGroups.get().contains(workItem.getGroupInfo(this)) ) {
        // This is a changelog entry that modifies the group. Do a FullSync to see if any
        // provisioned information changed. Unfortunately, this will do a membership sync which
        // might slow down the processing of this changelog entry. However, non-membership
        // changes are expected to be infrequent, so we aren't creating an optimized code path
        // that doesn't sync memberships.

        FullSyncQueueItem fullSyncStatus = getFullSyncer()
                .scheduleGroupForSync(
                        FullSyncProvisioner.QUEUE_TYPE.ASAP,
                        workItem.groupName,
                        workItem_identifier,
                        "Changelog: %s", workItem);

        workItem.markAsSuccess("Handled with scheduled FullSync (qid=%d)", fullSyncStatus.id);
        return;
      }

      // If we've gotten this far without processing it, there's nothing we can do
      if ( !workItem.hasBeenProcessed() ) {
        workItem.markAsSkipped("Nothing to do (not a supported change)");
      }

    } catch (PspException e) {
      LOG.error("Problem provisioning item {}", workItem, e);
      workItem.markAsFailure("Provisioning failure: %s", e.getMessage());
    } finally {
      currentWorkItem.set(null);
    }
  }

  private void processAnyChangesInGroupSelection(ProvisioningWorkItem workItem) throws PspException {
    String workItem_identifier;
    if ( workItem.getChangelogEntry() != null ) {
      workItem_identifier = String.format("chlog #%d",workItem.getChangelogEntry().getSequenceNumber());
    } else {
      workItem_identifier = workItem.toString();
    }

    LOG.info("{}: Checking to see if group selection has changed", getDisplayName());
    Set<GrouperGroupInfo> selectedGroups_before = selectedGroups.get();
    Set<GrouperGroupInfo> selectedGroups_now = getAllGroupsForProvisioner();

    // deselectedGroups = BEFORE GROUPS -minus- NOW GROUPS
    Set<GrouperGroupInfo> deselectedGroups = new HashSet<>(selectedGroups_before);
    deselectedGroups.removeAll(selectedGroups_now);

    // newlySelectedGroups = NOW GROUPS -minus- BEFORE GROUPS
    Set<GrouperGroupInfo> newlySelectedGroups = new HashSet<>(selectedGroups_now);
    newlySelectedGroups.removeAll(selectedGroups_before);

    // Save updated list of groups selected for provisioner
    selectedGroups.set(selectedGroups_now);

    LOG.info("{}: Change deselected {} groups from provisioner", deselectedGroups.size());
    LOG.info("{}: Change selected {} new groups for provisioner", newlySelectedGroups.size());

    for (GrouperGroupInfo group : deselectedGroups) {
      TSGroupClass tsGroup=null;
      if ( getConfig().needsTargetSystemGroups() ) {
        tsGroup = fetchTargetSystemGroup(group);
        if ( tsGroup == null ) {
          LOG.info("{}: Group is already not present in target system: {}", getDisplayName(), group.getName());
          continue;
        }
      }
      LOG.info("{}: Deleting group from target system because it is no longer selected for provisioning: {}", group);
      deleteGroup(group, tsGroup);
    }

    for (GrouperGroupInfo group : newlySelectedGroups) {
      LOG.info("{}: Scheduling full sync of group because it is now selected for provisioning: {}", group);

      getFullSyncer().scheduleGroupForSync(
              FullSyncProvisioner.QUEUE_TYPE.CHANGELOG,
              group.getName(),
              workItem_identifier,
              "group newly selected for provisioning");

    }
    workItem.markAsSuccess("Processed any changes in group selection");
  }


  private void processIncrementalSyncEvent(ProvisioningWorkItem workItem) throws PspException {
    ChangeLogEntry entry = workItem.getChangelogEntry();

    if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD ))
    {
      GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);

      if ( grouperGroupInfo == null || grouperGroupInfo.hasGroupBeenDeleted() ) {
        workItem.markAsSkippedAndWarn("Ignored group-add: group does not exist any more");
        return;
      }

      if ( !shouldGroupBeProvisioned(grouperGroupInfo) ) {
        workItem.markAsSkipped("Group %s is not selected to be provisioned", grouperGroupInfo);
        return;
      }

      if ( tsGroupCache_shortTerm.containsKey(grouperGroupInfo) ) {
        workItem.markAsSuccess("Group %s already exists", grouperGroupInfo);
        return;
      }
      else
        createGroup(grouperGroupInfo, Collections.EMPTY_LIST);
    }
    else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE ))
    {
      // Can't tell right reliably if group was supposed to be provisioned when it existed, so
      // we just delete it if it currently exists in target
      GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);

      if ( grouperGroupInfo == null ) {
        workItem.markAsSkippedAndWarn("Ignoring group-deletion event because group information was not found in grouper");
        return;
      }

      TSGroupClass tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);

      // If system uses targetSystemGroup and one doesn't exist ==> Nothing to do
      if ( config.needsTargetSystemGroups() ) {
          if ( tsGroup == null ) {
              workItem.markAsSuccess("Nothing to do: Group does not exist in target system");
              return;
          } else {
              LOG.info("Deleting provisioned group: {}", grouperGroupInfo);
              deleteGroup(grouperGroupInfo, tsGroup);
          }
      }
      else {
        // System doesn't use targetSystemGroup, so we can't tell if this group
        // was provisioned or not. Therefore call deleteGroup just in case

        LOG.info("{}: Group has been deleted from grouper. Checking to see if it was provisioned to target system", getDisplayName());
        deleteGroup(grouperGroupInfo, tsGroup);
      }
    }
    else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD))
    {
      GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);

      if ( grouperGroupInfo == null || grouperGroupInfo.hasGroupBeenDeleted() ) {
        workItem.markAsSkipped("Ignoring membership-add event for group that was deleted");
        return;
      }

      if ( !shouldGroupBeProvisioned(grouperGroupInfo) ) {
        workItem.markAsSkipped("Group %s is not selected to be provisioned", grouperGroupInfo);
        return;
      }

      TSGroupClass tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);
      Subject subject = workItem.getSubject(this);

      if ( subject == null ) {
        workItem.markAsSkippedAndWarn("Ignoring membership-add event because subject is no longer in grouper");
        return;
      }

      if ( subject.getTypeName().equalsIgnoreCase("group") ) {
        workItem.markAsSkipped("Nested-group membership skipped");
        return;
      }

      TSUserClass tsUser = tsUserCache_shortTerm.get(subject);

      if ( config.needsTargetSystemUsers() && tsUser==null ) {
        workItem.markAsSkippedAndWarn("Skipped: subject doesn't exist in target system");
        return;
      }

      addMembership(grouperGroupInfo, tsGroup, subject, tsUser);
    }
    else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE))
    {
      GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);

      if ( grouperGroupInfo==null || grouperGroupInfo.hasGroupBeenDeleted() ) {
        workItem.markAsSkipped("Ignoring membership-delete event for group that was deleted");
        return;
      }

      if ( !shouldGroupBeProvisioned(grouperGroupInfo) ) {
        workItem.markAsSkipped("Group %s is not selected to be provisioned", grouperGroupInfo);
        return;
      }

      TSGroupClass tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);
      Subject subject = workItem.getSubject(this);

      if ( subject == null ) {
        workItem.markAsSkippedAndWarn("Ignoring membership-delete event because subject is no longer in grouper");
        LOG.warn("Work item ignored: {}", workItem);
        return;
      }

      TSUserClass tsUser = tsUserCache_shortTerm.get(subject);

      if ( config.needsTargetSystemUsers() && tsUser==null ) {
        workItem.markAsSkippedAndWarn("Skipped: subject doesn't exist in target system");
        return;
      }
      deleteMembership(grouperGroupInfo, tsGroup, subject, tsUser);
    }
  }


  /**
   * Method that drives the cleanup operation. This sets things up and then
   * calls the (Abstract) doFullSync_cleanupExtraGroups
   *
   * @param stats
   * @throws PspException
   */
  final void prepareAndRunGroupCleanup(JobStatistics stats) throws PspException {
      // Make sure this is only used within Provisioners set up for full-sync mode
      GrouperUtil.assertion(isFullSyncMode(), "FullSync operations should only be used with provisioners initialized for full-sync");

	  if ( !config.isGrouperAuthoritative() ) {
		  LOG.warn("{}: Not doing group cleanup because grouper is not marked as authoritative in provisioner configuration", getDisplayName());
		  return;
	  }
	  
	  tsUserCache_shortTerm.clear();
	  tsGroupCache_shortTerm.clear();
	  try {
	    MDC.put("step", "clean/");
	    doFullSync_cleanupExtraGroups(stats);
	  }
	  catch (PspException e) {
		  LOG.error("Problem while looking for and removing extra groups", e);
		  throw e;
	  }
	  finally {
		  MDC.remove("step");
	  }
  }
  

/**
   * This is called by the FullSync thread, and is responsible for getting the
   * list of correct subjects together and cached. 
   * 
   * This then calls (abstract) doFullSync(group, tsGroup, correctSubjects, correctTSUsers, stats).
   * 
   * This method is final to make it clear that the abstract signature should be
   * overridden and to prevent subclasses from overriding this one by mistake.
   * 
   * @param grouperGroupInfo
 * @param oldestCacheTimeAllowed only use cached information fresher than this date
 * @param stats
 * @return True when changes were made to target system
 @throws PspException
   */
  final boolean doFullSync(GrouperGroupInfo grouperGroupInfo, DateTime oldestCacheTimeAllowed, JobStatistics stats)
        throws PspException {
    // Make sure this is only used within Provisioners set up for full-sync mode
    GrouperUtil.assertion(isFullSyncMode(),
            "FullSync operations should only be used with provisioners initialized for full-sync");


    TSGroupClass tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);

    // If there is a target-system group, then check to see if it should be deleted
    if ( tsGroup != null ) {
      if (grouperGroupInfo.hasGroupBeenDeleted() && tsGroup != null) {
        LOG.info("{} full sync: Deleting group because it was deleted from grouper: {}/{}",
                new Object[]{getDisplayName(), grouperGroupInfo, tsGroup});

        deleteGroup(grouperGroupInfo, tsGroup);
        return true;
      }

      if (!shouldGroupBeProvisioned(grouperGroupInfo)) {
        LOG.info("{} full sync: Deleting group because it is not selected for this provisioner: {}/{}",
                new Object[]{getDisplayName(), grouperGroupInfo, tsGroup});

        deleteGroup(grouperGroupInfo, tsGroup);
        return true;
      }
    }

    Set<Member> groupMembers = grouperGroupInfo.getMembers();
    Set<Subject> correctSubjects = new HashSet<Subject>();
    
    for (Member member : groupMembers) {
      Subject subject = member.getSubject();
      if ( subject.getTypeName().equalsIgnoreCase(SubjectTypeEnum.PERSON.getName()) ) {
        correctSubjects.add(subject);
      }
    }

    if ( correctSubjects.size() > 0 )
      prepareUserCache(correctSubjects, oldestCacheTimeAllowed);
    
    Set<TSUserClass> correctTSUsers = new HashSet<TSUserClass>();
    
    if ( getConfig().needsTargetSystemUsers() ) {
      // Loop through a copy of the correct subjects so that we can remove 
      // subjects that don't have matching TargetSystem users.
      for ( Subject correctSubject: new ArrayList<Subject>(correctSubjects) ) {
        TSUserClass tsUser = tsUserCache_shortTerm.get(correctSubject);
        if ( tsUser == null ) {
          // User is necessary in target system, but is not present
          LOG.warn("{}: Member in grouper group {} is being ignored because subject is not present in target system: {}",
              getDisplayName(), grouperGroupInfo, correctSubject);
          
          correctSubjects.remove(correctSubject);
        }
        else {
          correctTSUsers.add(tsUser);
        }
      }
    }

    LOG.debug("{}/{}: All correct member subjects: {}",
              new Object[] {getDisplayName(), grouperGroupInfo, correctSubjects});

    LOG.info("{}/{}: {} correct member subjects. Sample: {}...",
      new Object[] {getDisplayName(), grouperGroupInfo, correctSubjects.size(),
              new ArrayList<Subject>(correctSubjects).subList(0, Math.min(10, correctSubjects.size()))});

    try {
      MDC.put("step", "prov/");
      return doFullSync(grouperGroupInfo, tsGroup, correctSubjects, tsUserCache_shortTerm, correctTSUsers, stats);
    }
    finally {
      MDC.remove("step");
    }
  }

  /**
   * Get the ProvisioningWorkItem that this provisioner is currently processing
   * @return
   */
  public ProvisioningWorkItem getCurrentWorkItem() {
    return currentWorkItem.get();
  }
  
  public void setCurrentWorkItem(ProvisioningWorkItem item) {
    currentWorkItem.set(item);
  }
  
  protected static String getSubjectCacheKey(String subjectId, String sourceId) {
    String cacheKey = String.format("%s__%s", sourceId, subjectId);
    return cacheKey;
  }
	  
  protected static String getSubjectCacheKey(Subject subject) {
    String cacheKey = getSubjectCacheKey(subject.getSourceId(), subject.getSourceId());
    return cacheKey;
  }
  
  protected Subject getSubject(String subjectId, String sourceId) {
    String cacheKey = getSubjectCacheKey(subjectId, sourceId);
    
    Subject subject = grouperSubjectCache.get(cacheKey);
    
    if ( subject != null ) 
      return subject;
    
      subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, false);

      if ( subject != null )
        grouperSubjectCache.put(cacheKey, subject);
      
      return subject;
  }

  protected GrouperGroupInfo getGroupInfoOfExistingGroup(Group group) {
    String groupName = group.getName();
    GrouperGroupInfo result = grouperGroupInfoCache.get(groupName);
    if ( result == null ) {
      result = new GrouperGroupInfo(group);
      grouperGroupInfoCache.put(groupName, result);
    }
    return result;
  }
  
  
  protected GrouperGroupInfo getGroupInfoOfExistingGroup(String groupName) {
    GrouperGroupInfo grouperGroupInfo = grouperGroupInfoCache.get(groupName);

    // Return group if it was cached
    if (grouperGroupInfo != null)
      return grouperGroupInfo;

    try {
      // Look for an existing grouper group
      Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(false), groupName, false);

      if (group != null) {
        return getGroupInfoOfExistingGroup(group);
      }
    } catch (GroupNotFoundException e) {
      LOG.warn("Unable to find existing group '{}'", groupName);
    }
    return null;
  }

  protected GrouperGroupInfo getGroupInfo(ProvisioningWorkItem workItem) {
    String groupName = workItem.groupName;

    if ( groupName == null ) {
      return null;
    }

    GrouperGroupInfo result = getGroupInfoOfExistingGroup(groupName);
    if ( result != null ) {
      LOG.debug("{}: Found existing group {}", getDisplayName(), result);
      return result;
    }

    try {
      // If an existing grouper group wasn't found, look for a PITGroup
        PITGroup pitGroup = PITGroupFinder.findMostRecentByName(groupName, false);
  	    
        if ( pitGroup != null ) {
            result = new GrouperGroupInfo(pitGroup);
            result.idIndex = workItem.getGroupIdIndex();

            // Avoiding caching pitGroup-based items because they are workItem dependent
            //grouperGroupInfoCache.put(groupName, result);
            LOG.debug("{}: Found PIT group {}", getDisplayName(), result);

            return result;
        } else {
          LOG.warn("Could not find PIT group: {}", groupName);
          result = new GrouperGroupInfo(groupName, workItem.getGroupIdIndex());
          LOG.debug("{}: Using barebones group info {}", getDisplayName(), result);
          return result;
        }
    }
    catch (GroupNotFoundException e) {
      LOG.warn("Unable to find PIT group '{}'", groupName);
    }
    
    return null;
  }
  
  private FullSyncProvisioner getFullSyncer() throws PspException {
    return FullSyncProvisionerFactory.getFullSyncer(this);
  }


  /**
   * This method looks for groups that are marked for provisioning as determined by 
   * the GroupSelectionExpression. 
   * 
   * Because it can take a very long time to look through a large group registry, this
   * method pre-filters groups and folders before evaluating them with the GroupSelectionExpression:
   * This method looks for Groups and Folders that reference the attributes in 
   * attributesUsedInGroupSelectionExpression
   * 
   * @return A collection of groups that are to be provisioned by this provisioner
   */
  public Set<GrouperGroupInfo> getAllGroupsForProvisioner()  {
    Date start = new Date();

    LOG.debug("{}: Compiling a list of all groups selected for provisioning", getDisplayName());
    Set<GrouperGroupInfo> result = new HashSet<>();

    Set<Group> interestingGroups = new HashSet<Group>();
    for ( String attribute : getConfig().getAttributesUsedInGroupSelectionExpression() ) {
      Set<Stem> foldersReferencingAttribute;
      Set<Group> groupsReferencingAttribute;

      if ( getConfig().areAttributesUsedInGroupSelectionExpressionComparedToProvisionerName() ) {
        LOG.debug("Looking for folders that match attribute {}={}", attribute, getConfigName());
        foldersReferencingAttribute = new StemFinder().assignNameOfAttributeDefName(attribute).assignAttributeValue(getConfigName()).findStems();
        LOG.debug("Looking for groups that match attribute {}={}", attribute, getConfigName());
        groupsReferencingAttribute = new GroupFinder().assignNameOfAttributeDefName(attribute).assignAttributeValue(getConfigName()).findGroups();
      }
      else {
        LOG.debug("Looking for folders that have attribute {}", attribute);
        foldersReferencingAttribute = new StemFinder().assignNameOfAttributeDefName(attribute).findStems();
        LOG.debug("Looking for groups that have attribute {}", attribute);
        groupsReferencingAttribute = new GroupFinder().assignNameOfAttributeDefName(attribute).findGroups();
      }

      LOG.debug("{}: There are {} folders that match {} attribute", new Object[]{getDisplayName(), foldersReferencingAttribute.size(), attribute});
      LOG.debug("{}: There are {} groups that match {} attribute", new Object[]{getDisplayName(), groupsReferencingAttribute.size(), attribute});

      interestingGroups.addAll(groupsReferencingAttribute);
      for ( Stem folder : foldersReferencingAttribute ) {
        Set<Group> groupsUnderFolder;

        groupsUnderFolder = new GroupFinder().assignParentStemId(folder.getId()).assignStemScope(Scope.SUB).findGroups();

        LOG.debug("{}: There are {} groups underneath folder {}", new Object[]{getDisplayName(), groupsUnderFolder.size(), folder.getName()});
        interestingGroups.addAll(groupsUnderFolder);
      }
    }

    for ( Group group : interestingGroups ) {
      GrouperGroupInfo grouperGroupInfo = new GrouperGroupInfo(group);
      if ( shouldGroupBeProvisioned(grouperGroupInfo) )
        result.add(grouperGroupInfo);
    }
    LOG.info("{}: There are {} groups selected for provisioning (found in %s)",
        getDisplayName(), result.size(), PspUtils.formatElapsedTime(start, null));

    return result;
  }
  
  
  public ConfigurationClass getConfig() {
    return config;
  }
  
  /**
   * This returns the configuration class needed by provisioners of this class.
   * Unfortunately, java generics do not allow the generics to be used in static
   * methods.
   * 
   * Therefore, every (concrete (non-abstract)) subclass of Provisioner needs
   * to implement this so the ProvisionerConfiguration subclass it needs can be 
   * returned.
   * 
   * TODO: Maybe this could be done with an annotation?
   * 
   * @return
   */
  public static Class<? extends ProvisionerConfiguration> getPropertyClass() {
    return ProvisionerConfiguration.class;
  }

  
  protected Map<String, Object> getGroupJexlMap(GrouperGroupInfo grouperGroupInfo) {
	return grouperGroupInfo.getJexlMap();
  }
  
  /**
   * Evaluate the GroupSelectionExpression to see if group should be processed by this
   * provisioner.
   * 
   * @param grouperGroupInfo
   * @return
   */
  protected boolean shouldGroupBeProvisioned(GrouperGroupInfo grouperGroupInfo) {
    if ( grouperGroupInfo.hasGroupBeenDeleted() ) {
      return false;
    }

    try {
      String resultString = evaluateJexlExpression("GroupSelection", config.getGroupSelectionExpression(), null, null, grouperGroupInfo, null);

      boolean result = BooleanUtils.toBoolean(resultString);

      if (result)
        LOG.debug("{}: Group {} matches group-selection filter.", getDisplayName(), grouperGroupInfo);
      else
        LOG.trace("{}: Group {} does not match group-selection filter.", getDisplayName(), grouperGroupInfo);

      return result;
    } catch (PspException e) {
      LOG.warn("{}: Error evaluating groupSelection expression for group {}. Assuming group is not selected for provisioner",
              getDisplayName(), grouperGroupInfo, e);
      return false;
    }
  }

  public String getDisplayName() {
    return provisionerDisplayName;
  }

  public String getConfigName() { return provisionerConfigName; }

  public void provisionBatchOfItems(List<ProvisioningWorkItem> allWorkItems) {
    activeProvisioner.set(this);
    List<ProvisioningWorkItem> filteredWorkItems=null;

    // Mark the items as successful if we are not enabled.
    // Note: They are being marked as successful so that there is an easy mechanism
    // to get a provisioner up to date with the changelog (or other event system).
    // Additionally, if you just don't want to process events, then you can remove
    // this provisioner from the configuration.
    if (!config.isEnabled()) {
      for (ProvisioningWorkItem workItem : allWorkItems)
        workItem.markAsSkippedAndWarn("Provisioner %s is not enabled", getDisplayName());
      return;
    }

    // Let's see if there is a reason to flush our group cache. In particular,
    // we flush group information when groups and their attributes are edited
    // because we don't know what attributes of groups could be used in various
    // JEXL expressions
    MDC.put("step", "cache_eval");
    try {
      flushCachesIfNecessary(allWorkItems);
    } catch (PspException e) {
      LOG.error("Unable to evaluate our caches", e);
      MDC.remove("step");
      throw new RuntimeException("No entries provisioned. Cache evaluation failed: " + e.getMessage(), e);
    }

    // Let the provisioner filter out any unnecessary work items.
    // This particularly filters out groups that we're not supposed to provision
    MDC.put("step", "filter/");
    try {
      filteredWorkItems = filterWorkItems(allWorkItems);
      LOG.info("{}: {} work items need to be processed further", getDisplayName(), filteredWorkItems.size());
    } catch (PspException e) {
      LOG.error("Unable to filter the provisioning batch", e);
      MDC.remove("step");
      throw new RuntimeException("No entries provisioned. Batch-filtering failed: " + e.getMessage(), e);
    }

    // Tell the provisioner about this batch of workItems
    MDC.put("step", "start/");
    try {
      try {
        startCoordination(filteredWorkItems);
        startProvisioningBatch(filteredWorkItems);
      } catch (PspException e) {
        LOG.error("Unable to begin the provisioning batch", e);
        MDC.remove("step");
        throw new RuntimeException("No entries provisioned. Batch-Start failed: " + e.getMessage(), e);
      }

      // Go through the workItems that were not marked as processed by the startProvisioningBatch
      // and provision them
      for (ProvisioningWorkItem workItem : allWorkItems) {
        MDC.put("step", String.format("prov/%s/", workItem.getMdcLabel()));
        if (!workItem.hasBeenProcessed()) {
          try {
            provisionItem(workItem);
          } catch (PspException e) {
            LOG.error("Problem provisioning {}", workItem, e);
            workItem.markAsFailure(e.getMessage());
          }
        }
      }

      // Do 'finish' task for workItems that are not marked as processed before now

      MDC.put("step", "fin/");
      List<ProvisioningWorkItem> workItemsToFinish = new ArrayList<ProvisioningWorkItem>();
      try {
        for (ProvisioningWorkItem workItem : allWorkItems) {
          if (!workItem.hasBeenProcessed())
            workItemsToFinish.add(workItem);
        }
        finishProvisioningBatch(workItemsToFinish);
        finishCoordination(filteredWorkItems, true);
      } catch (PspException e) {
        LOG.error("Problem completing provisioning batch", e);
        for (ProvisioningWorkItem workItem : workItemsToFinish) {
          if (!workItem.hasBeenProcessed())
            workItem.markAsFailure("Unable to finish provisioning (%s)", e.getMessage());
        }
      }
      MDC.remove("step");
    }
    finally{
      finishCoordination(filteredWorkItems, false);
      activeProvisioner.remove();
    }
  }


  /**
   * Look at the batch of workItems and flush caches necessary to process the entries
   * properly. For instance, if we're being notified that an ATTRIBUTE-ASSIGNMENT has
   * occurred, we can flush our cache to get the best information possible.
   * 
   * @param allWorkItems
   * @throws PspException
   */
  protected void flushCachesIfNecessary(List<ProvisioningWorkItem> allWorkItems)  throws PspException{
    for (ProvisioningWorkItem workItem : allWorkItems ) {

      // Skip irrelevant changelog entries
      if (!workItem.matchesChangelogType(ChangelogHandlingConfig.allRelevantChangelogTypes)) {
        continue;
      }

      // Skip changelog entries that don't need cache flushing
      if (!workItem.matchesChangelogType(ChangelogHandlingConfig.relevantChangesThatNeedGroupCacheFlushing)) {
        continue;
      }

      // We know we need to flush something from the cache. If the entry is group-specific,
      // we'll only flush that group
      GrouperGroupInfo groupInfo = workItem.getGroupInfo(this);
      if (groupInfo != null) {
        uncacheGroup(groupInfo, null);
      } else {
        // Flush everything and return
        uncacheAllGroups();
        return;
      }
    }
  }


  public boolean isFullSyncMode() {
    return fullSyncMode;
  }


  @Override
  public String toString() {
    return String.format("%s[%s]", getClass().getSimpleName(), getDisplayName());
  }


  /**
   * Some changes (eg, labeling a folder for syncing) can have a large effect and are best handled with
   * a complete sync of all groups.
   * @return true if this work item should initiate a full sync of all groups
   */
  public boolean workItemMightChangeGroupSelection(ProvisioningWorkItem workItem) {
    // Skip if this ChangelogHandlingConfig says this doesn't affect group selection
    if ( !workItem.matchesChangelogType(ChangelogHandlingConfig.changelogTypesThatCanChangeGroupSelection) ) {
      return false;
    }

    String attributeName = workItem.getAttributeName();

    // If we can't figure out what the attributeName being modified is, we need to assume
    // GroupSelection might be changed
    if (StringUtils.isEmpty(attributeName)) {
      LOG.info("{}: Change might change group selection: {}", getDisplayName(), workItem);
      return true;
    }

    if ( getConfig().attributesUsedInGroupSelectionExpression.contains(attributeName) ) {
      LOG.info("{}: Change changes {} which might change group selection: {}",
              new Object[]{getDisplayName(), attributeName, workItem});
      return true;
    }

    return false;
  }

  /**
   * Were enough subjects missing from target system that we should log more information than normal
   * to help track down why they were missing?
   *
   * Subclasses should use this in fetchTargetSystemUsers()
   * @param subjectsToFetch
   * @param subjectsInfoFound
   * @return
   */

  protected boolean shouldLogAboutMissingSubjects(Collection<Subject> subjectsToFetch, Collection<?> subjectsInfoFound) {
    int missingResults_count = subjectsToFetch.size() - subjectsInfoFound.size();
    double missingResults_percentage = 100.0 * missingResults_count / subjectsToFetch.size();

    if (subjectsToFetch.size() > 5 && missingResults_percentage > config.getMissingSubjectsWarningThreshold_percentage()) {
      return true;
    }
    return false;
  }




}
