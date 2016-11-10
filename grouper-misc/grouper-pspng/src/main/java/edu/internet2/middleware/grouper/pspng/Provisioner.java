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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


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
  static final Logger STATIC_LOG = LoggerFactory.getLogger(Provisioner.class);
  
  protected final Logger LOG;
  final public String provisionerName;
  
  // Cache groups by groupInfo key
  final GrouperCache<String, GrouperGroupInfo> grouperGroupInfoCache;
  
  // Cache subjects by sourceId__subjectId key
  final GrouperCache<String, Subject> grouperSubjectCache;

  // Cache TargetSystemUsers by Subject. This is typically a long-lived cache
  // used across provisioning batches. These are only fetched and cached
  // if our config has needsTargetSystemUsers=true.
  final GrouperCache<Subject, TSUserClass> targetSystemUserCache;
  
  // This stores TargetSystemUserss during the provisioning batch. This Map might seem
  // redundant to the targetSystemUserCache, but it is needed for
  // two reasons: 1) To make sure items are not flushed during the provisioning batch
  // like they could be within a GrouperCache; 2) To know if a TSUserClass really
  // doesn't exist (getTargetSystemUser might do a lookup every time a nonexistent
  // user is requested. By using a map populated once per provisioning batch, we don't
  // have to do extra lookups during the batch).
  // This map is populated by startProvisioningBatch and emptied by finishProvisioningBatch
  // if our config has needsTargetSystemUsers=true.
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

  /**
   * Should this provisioner operate in Full-Sync mode? This might mean fetching all members of a group
   * which can be expensive in an incremental-sync, but is worth the trouble in a full-sync.
   */
  protected boolean fullSyncMode = false;
  
  final protected ConfigurationClass config;
  
  
  public Provisioner(String provisionerName, ConfigurationClass config) {
    LOG = LoggerFactory.getLogger(String.format("%s.%s", getClass().getName(), provisionerName ));
    
    this.config = config;
    this.provisionerName = provisionerName;

    checkAttributeDefinitions();
    
    grouperGroupInfoCache 
      = new GrouperCache<String, GrouperGroupInfo>(String.format("PSP-%s-GrouperGroupInfoCache", getName()),
          config.getGrouperGroupCacheSize(),
          false,
          config.getGrouperDataCacheTime_secs(),
          config.getGrouperDataCacheTime_secs(),
          false);
    
    grouperSubjectCache 
      = new GrouperCache<String, Subject>(String.format("PSP-%s-GrouperSubjectCache", getName()),
          config.getGrouperSubjectCacheSize(),
          false,
          config.getGrouperDataCacheTime_secs(),
          config.getGrouperDataCacheTime_secs(),
          false);

    targetSystemUserCache 
      = new GrouperCache<Subject, TSUserClass>(String.format("PSP-%s-TargetSystemUserCache", getName()),
          config.getGrouperSubjectCacheSize(),
          false,
          config.getGrouperDataCacheTime_secs(),
          config.getGrouperDataCacheTime_secs(),
          false);
    
    targetSystemGroupCache 
      = new GrouperCache<GrouperGroupInfo, TSGroupClass>(String.format("PSP-%s-TargetSystemGroupCache", getName()),
          config.getGrouperGroupCacheSize(),
          false,
          config.getGrouperDataCacheTime_secs(),
          config.getGrouperDataCacheTime_secs(),
          false);
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
    String provisionToDefName = pspngManagementStemName + ":provision_to_def";
    AttributeDef provisionToDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
        provisionToDefName, false, new QueryOptions().secondLevelCache(false));
    if (provisionToDef == null) {
      provisionToDef = pspngManagementStem.addChildAttributeDef("provision_to_def", AttributeDefType.type);
      provisionToDef.setAssignToGroup(true);
      provisionToDef.setAssignToStem(true);
      provisionToDef.setMultiAssignable(true);
      provisionToDef.setValueType(AttributeDefValueType.string);
      provisionToDef.store();
    }
    
    //see if do_not_provision_to_def attributeDef is there
    String doNotProvisionToDefName = pspngManagementStemName + ":do_not_provision_to_def";
    AttributeDef doNotProvisionToDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
        doNotProvisionToDefName, false, new QueryOptions().secondLevelCache(false));
    if (doNotProvisionToDef == null) {
      doNotProvisionToDef = pspngManagementStem.addChildAttributeDef("do_not_provision_to_def", AttributeDefType.type);
      doNotProvisionToDef.setAssignToGroup(true);
      doNotProvisionToDef.setAssignToStem(true);
      doNotProvisionToDef.setMultiAssignable(true);
      doNotProvisionToDef.setValueType(AttributeDefValueType.string);
      doNotProvisionToDef.store();
    }
    
    GrouperCheckConfig.checkAttribute(pspngManagementStem, provisionToDef, "provision_to", "provision_to", "Defines what provisioners should process a group or groups within a folder", true);
    GrouperCheckConfig.checkAttribute(pspngManagementStem, doNotProvisionToDef, "do_not_provision_to", "do_not_provision_to", "Defines what provisioners should not process a group or groups within a folder. Since the default is already for provisioners to not provision any groups, this attribute is to override a provision_to attribute set on an ancestor folder. ", true);
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
   * subclasses that support creating groups. 
   * 
   * @param grouperGroup
   * @return
   * @throws PspException
   */
  protected abstract TSGroupClass createGroup(GrouperGroupInfo grouperGroup) throws PspException;

  /**
   * Action method that handles group removal. The top-level Provisioner class implementation
   * does nothing except log an error if the target system needs groups.
   * 
   * This is expected to be overridden by subclasses if the target system needs groups, and
   * do not call the super.deleteGroup version of this when you override it this
   * @param group
   * @param tsGroup 
   * @param subject
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
   * @param correctTSUsers Collection of TargetSystemUsers which map to the correctSubjects. This will be empty
   * for provisioners that do not use TargetSystemUsers.
   */
  protected abstract void doFullSync(
      GrouperGroupInfo grouperGroupInfo, TSGroupClass tsGroup, 
      Set<Subject> correctSubjects, Set<TSUserClass> correctTSUsers) throws PspException;

  /**
   * This method's responsibility is find extra groups within Grouper's responsibility that
   * exist in the target system. These extra groups should be removed.
   * 
   * Note: This is only called when grouperIsAuthoritative=true in the provisioner's properties.
   * 
   * The groups that should exist are passed in as a parameter.
   * 
   * @param groupsForThisProvisioner The correct list of groups for this provisioner
   * @param tsGroups The correct list of Target System groups for this provisioner.
   * for provisioners that do not use TargetSystemUsers.
   */
  protected abstract void doFullSync_cleanupExtraGroups(
          Set<GrouperGroupInfo> groupsForThisProvisioner, 
          Map<GrouperGroupInfo, TSGroupClass> tsGroups) throws PspException;
  

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
   * @param grouperGroups
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
    
    LOG.debug("Filtering provisioning batch of {} items");
    
    for ( ProvisioningWorkItem workItem : workItems ) {
      GrouperGroupInfo g = workItem.getGroupInfo(this);
      if ( g == null ) {
        result.add(workItem);
      }
      else if ( shouldGroupBeProvisioned(g) ) {
        result.add(workItem);
      }
      else {
        // Not going to process this item, so mark it as a success and don't add it to result
        workItem.markAsSuccess("Ignoring work item %s because group %s is not provisioned", 
            workItem, g.name);
      }
    }
    
    return result;
  }

  /**
   * Get ready for a provisioning batch. If this is overridden, make sure you call super()
   * at the beginning of your overridden version.
   * 
   * @param workItems
   * @throws PspException
   */
  public void startProvisioningBatch(List<ProvisioningWorkItem> workItems) throws PspException {
    LOG.debug("Starting provisioning batch of {} items", workItems.size());
    Set<Subject> subjects = new HashSet<Subject>();
    
    // Use this Set to remove duplicate group names that are referenced in multiple workItems
    Set<GrouperGroupInfo> grouperGroupInfos = new HashSet<GrouperGroupInfo>();
    
    for ( ProvisioningWorkItem workItem : workItems) {
      String groupName = workItem.getGroupName();
      if ( groupName == null )
    	  // Nothing to do before batch is processed
    	  continue;
      GrouperGroupInfo grouperGroupInfo = getGroupInfo(groupName);
      if ( grouperGroupInfo != null )
        grouperGroupInfos.add(grouperGroupInfo);
    
      Subject s = workItem.getSubject(this);
      if ( s != null )
        subjects.add(s);
    }
    
    prepareGroupCache(grouperGroupInfos);
    prepareUserCache(subjects);
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
   * @param expression
   * @param subject
   * @param group
   * @param keysAndValues Key/Value pairs that will also be available within the Jexl's variable map
   * @return
   */
  protected final String evaluateJexlExpression(String expression, Subject subject, GrouperGroupInfo grouperGroupInfo,
      Object... keysAndValues) {
    
    LOG.trace("Evaluating Jexl expression: {}", expression);
    
    Map<String, Object> variableMap = new HashMap<String, Object>();

    variableMap.put("utils", new PspJexlUtils());

    // Copy any provided keys and values into the map
    GrouperUtil.assertion(keysAndValues.length % 2 == 0, "KeysAndValues must be paired evenly");
    for (int i=0; i<keysAndValues.length; i+=2)
      variableMap.put(keysAndValues[i].toString(), keysAndValues[i+1]);

    // Give provisioner subclasses to add information
    populateJexlMap(variableMap, 
        subject, 
        subject==null ? null : tsUserCache_shortTerm.get(subject), 
        grouperGroupInfo, 
        grouperGroupInfo==null ? null : tsGroupCache_shortTerm.get(grouperGroupInfo));
    
    // Give our config a chance to add information
    config.populateElMap(variableMap);
    
    try {
      String result = GrouperUtil.substituteExpressionLanguage(expression, variableMap, true, false, false);
      LOG.debug("Evaluated Jexl expression: {} FROM {} WITH variables {}", new Object[] {result, expression, variableMap});

      return result;
    }
    catch (RuntimeException e) {
      LOG.error("Jexl Expression {} could not be evaluated for subject '{}/{}' and group '{}/{}' which used variableMap '{}'",
          new Object[] {expression, 
              subject, 
              subject==null ? null : tsUserCache_shortTerm.get(subject), 
              grouperGroupInfo, 
              grouperGroupInfo==null ? null : tsGroupCache_shortTerm.get(grouperGroupInfo),
              variableMap});
      throw e;
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
   * @param group
   * @param tsGroup
   */
  protected void populateJexlMap(Map<String, Object> variableMap, Subject subject, 
      TSUserClass tsUser, GrouperGroupInfo grouperGroupInfo, TSGroupClass tsGroup) {
    variableMap.put("provisionerType", getClass().getSimpleName());
    variableMap.put("provisionerName", getName());

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
  private void prepareUserCache(Set<Subject> subjects) throws PspException {
    LOG.debug("Starting to cache user information for {} items", subjects.size());
    tsUserCache_shortTerm.clear();
    
    // Nothing to do if TargetSystemUsers are not used by this provisioner
    if ( ! config.needsTargetSystemUsers() )
      return;
    Collection<Subject> subjectsToFetch = new ArrayList<Subject>();
    
    for (Subject s : subjects) {
      // Skip group subjects (source=g:gsa)
      if ( s.getSourceId().equals("g:gsa") )
        continue;
      
      // See if the subject is already cached.
      TSUserClass cachedTSU = targetSystemUserCache.get(s);
      if ( cachedTSU != null )
        // Cache user in shortTerm cache as well as refresh it in longterm cache
        cacheUser(s, cachedTSU);
      else
        subjectsToFetch.add(s);
    }
    
    if ( subjectsToFetch.size() == 0 )
      return;
    
    List<List<Subject>> batchesOfSubjectsToFetch = PspUtils.chopped(subjectsToFetch, config.getUserSearch_batchSize());
    
    for (List<Subject> batchOfSubjectsToFetch : batchesOfSubjectsToFetch ) {
      Map<Subject, TSUserClass> fetchedData;
      
      try {
        fetchedData = fetchTargetSystemUsers(batchOfSubjectsToFetch);
        // Save the fetched data in our cache
        for ( Entry<Subject, TSUserClass> subjectInfo : fetchedData.entrySet() )
          cacheUser(subjectInfo.getKey(), subjectInfo.getValue());
      }
      catch (PspException e1) {
        // Batch-fetching failed. Let's see if we can narrow it down to a single
        // Subject
          for ( Subject subject : batchOfSubjectsToFetch ) {
            try {
              TSUserClass tsUser = fetchTargetSystemUser(subject);
              cacheUser(subject, tsUser);
            }
            catch (PspException e2) {
              throw new RuntimeException("Problem fetching information on subject " + subject + ": " + e2.getMessage());
            }
          }
      }
    }
    
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
          LOG.warn("{}: User not found in target system: {}", getName(), subj.getId());
      }
    }
  }

 
  /**
   * This makes sure all the Groups referenced by groupInfoSet are in groupMap_shortTerm. 
   * If our config says needsTargetSystemGroups is False, then the groupMap will
   * be empty.
   * 
   * @param groupInfoSet
   * @throws PspException
   */
  private void prepareGroupCache(Collection<GrouperGroupInfo> grouperGroupInfos) throws PspException {
	// Remove any duplicate group info objects
	Set<GrouperGroupInfo> groupInfoSet = new HashSet<GrouperGroupInfo>(grouperGroupInfos);
	
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
      if ( cachedTSG != null )
        // Cache group in shortTerm cache as well as refresh it in longterm cache
        cacheGroup(grouperGroupInfo, cachedTSG);
      else
        groupsToFetch.add(grouperGroupInfo);
    }
    
    if ( groupsToFetch.size() == 0 )
      return;
    
    List<List<GrouperGroupInfo>> batchesOfGroupsToFetch = PspUtils.chopped(groupsToFetch, config.getGroupSearch_batchSize());
    
    for ( List<GrouperGroupInfo> batchOfGroupsToFetch : batchesOfGroupsToFetch ) {
      Map<GrouperGroupInfo, TSGroupClass> fetchedData;
      
      try {
        fetchedData = fetchTargetSystemGroups(batchOfGroupsToFetch);
        // Save the data that was fetched in our cache
        for ( Entry<GrouperGroupInfo, TSGroupClass> grouperGroupInfo : fetchedData.entrySet() )
          cacheGroup(grouperGroupInfo.getKey(), grouperGroupInfo.getValue());
      }
      catch (PspException e1) {
        // Batch-fetching failed. Let's see if we can narrow it down to a single
        // Group
          for ( GrouperGroupInfo grouperGroupInfo : batchOfGroupsToFetch ) {
            try {
              TSGroupClass tsGroup = fetchTargetSystemGroup(grouperGroupInfo);
              cacheGroup(grouperGroupInfo, tsGroup);
            }
            catch (PspException e2) {
              throw new RuntimeException("Problem fetching information on group " + grouperGroupInfo);
            }
          }
      }
    }
    
    for ( GrouperGroupInfo grouperGroupInfo : groupsToFetch )
      if ( ! tsGroupCache_shortTerm.containsKey(grouperGroupInfo) )
        // Group does not already exist. Create it if we need to.
        if ( shouldGroupBeProvisioned(grouperGroupInfo) ) {
          if ( config.areEmptyGroupsSupported() ) {
            TSGroupClass tsGroup = createGroup(grouperGroupInfo);
            cacheGroup(grouperGroupInfo, tsGroup);
          }
          else
            LOG.warn("{}: Group was not found in target system: {}", getName(), grouperGroupInfo);
        }
  }

  
  public TSUserClass getTargetSystemUser(Subject subject) throws PspException {
    if ( !config.needsTargetSystemUsers() ) 
      throw new IllegalStateException(String.format("%s: system that doesn't need target-system users, but one was requested", getName()));
    
    TSUserClass result = tsUserCache_shortTerm.get(subject);
    
    if ( result == null ) {
      if ( config.isCreatingMissingUsersEnabled() ) {
        result=createUser(subject);
        cacheUser(subject, result);
      }
      else 
        LOG.warn("{}: user is missing and user-creation is not enabled ({})", getName(), subject.getId());
    }
    
    return result;
  }
  
  
  /**
   * Store Subject-->TSUserClass mapping in long-term and short-term caches
   * @param subject
   * @param newTSUser
   */
  private void cacheUser(Subject subject, TSUserClass newTSUser) {
    LOG.debug("Adding user to cache: {}", subject);
    targetSystemUserCache.put(subject, newTSUser);
    tsUserCache_shortTerm.put(subject, newTSUser);
  }

  protected void uncacheUser(Subject subject, TSUserClass oldTSUser) {
    // If the caller didn't know what Subject to flush, let's see if we can find it
    if ( subject == null )
      for ( Subject s : targetSystemUserCache.keySet() )
        if ( targetSystemUserCache.get(s) == oldTSUser ) {
          subject = s;
          break;
        }
    
    // If we didn't find a match
    if ( subject == null ) {
      LOG.warn("Cache-flush failed: Could not find Subject that matches Target System User {}", oldTSUser );
      return;
    }
    
    LOG.debug("Flushing user from cache: {}", subject.getName());
    targetSystemUserCache.remove(subject);
    grouperSubjectCache.remove(getSubjectCacheKey(subject));
  }

  /**
   * Store Group-->TSGroupClass mapping in long-term and short-term caches
   * @param group
   * @param newTSGroup
   */
  private void cacheGroup(GrouperGroupInfo grouperGroupInfo, TSGroupClass newTSGroup) {
    LOG.debug("Adding group to cache: {}", grouperGroupInfo);
    targetSystemGroupCache.put(grouperGroupInfo, newTSGroup);
    tsGroupCache_shortTerm.put(grouperGroupInfo, newTSGroup);
  }
  
  
  /**
   * The specified Grouper and TargetSystem groups have changed, remove 
   * them from various caches. 
   * @param group
   * @param oldTSGroup
   */
  protected void uncacheGroup(GrouperGroupInfo grouperGroupInfo, TSGroupClass oldTSGroup) {
    // If the caller didn't know what Group to flush, let's see if we can find it
    if ( grouperGroupInfo == null )
      for ( GrouperGroupInfo gi : targetSystemGroupCache.keySet() )
        if ( targetSystemGroupCache.get(gi) == oldTSGroup ) {
          grouperGroupInfo = gi;
          break;
        }
    
    if ( grouperGroupInfo == null ) {
    	LOG.warn("Can't find Grouper Group to uncache from tsGroup {}", oldTSGroup);
    	return;
    }
    
    LOG.debug("Flushing group from cache: {}", grouperGroupInfo);
    targetSystemGroupCache.remove(grouperGroupInfo);
    
    grouperGroupInfoCache.remove(grouperGroupInfo.getName());
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
    
    try {
      if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD ))
      {
        GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);
        
        if ( tsGroupCache_shortTerm.containsKey(grouperGroupInfo) ) {
          workItem.markAsSuccess("Group %s already exists", grouperGroupInfo);
          return;
        }
        else
          createGroup(grouperGroupInfo);
      }
      else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE ))
      {
        GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);
        TSGroupClass tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);
        
        deleteGroup(grouperGroupInfo, tsGroup);
      }
      else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD))
      {
        GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);
        
        if ( grouperGroupInfo.hasGroupBeenDeleted() ) {
          workItem.markAsSuccess("Ignoring membership-add event for group that was deleted: %s", grouperGroupInfo);
          return;
        }
        TSGroupClass tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);
        Subject subject = workItem.getSubject(this);
        
        if ( subject.getTypeName().equalsIgnoreCase("group") ) {
          LOG.info("{}: Skipping nested-group membership. Individual subjects will be processed ({})", getName(), subject.getName());
          workItem.markAsSuccess("Nested-group membership skipped");
          return;
        }

        TSUserClass tsUser = tsUserCache_shortTerm.get(subject);
        
        if ( config.needsTargetSystemUsers() && tsUser==null ) {
          LOG.warn("{}: Skipping adding membership to {} for subject that doesn't exist in target system: {}", 
              new Object[]{getName(), grouperGroupInfo.getName(), subject.getName()});
          workItem.markAsSuccess("Skipped: subject '%s' doesn't exist in target system", subject.getName());
          return;
        }
        
        addMembership(grouperGroupInfo, tsGroup, subject, tsUser);
      }
      else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE))
      {
        GrouperGroupInfo grouperGroupInfo = workItem.getGroupInfo(this);
        if ( grouperGroupInfo.hasGroupBeenDeleted() ) {
          workItem.markAsSuccess("Ignoring membership-delete event for group that was deleted: %s", grouperGroupInfo);
          return;
        }
        TSGroupClass tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);
        Subject subject = workItem.getSubject(this);
        TSUserClass tsUser = tsUserCache_shortTerm.get(subject);
  
        if ( config.needsTargetSystemUsers() && tsUser==null ) {
          LOG.warn("{}: Skipping removing membership of subject from {}: Subject doesn't already exist in target system: {}",
              new Object[]{getName(), grouperGroupInfo.getName(), subject.getName()});
          workItem.markAsSuccess("Skipped: subject '%s' doesn't exist in target system", subject.getName());
          return;
        }
        deleteMembership(grouperGroupInfo, tsGroup, subject, tsUser);
      }
      else
      {
        LOG.info("Not a supported change: {}", workItem);
        workItem.markAsSuccess("Nothing to do (not a supported change)");
      }
    } catch (PspException e) {
      LOG.error("Problem provisioning item {}", workItem, e);
      workItem.markAsFailure("Provisioning failure: %s", e.getMessage());
    } finally {
      currentWorkItem.set(null);
    }
  }
  
  final void doFullSync_cleanupExtraGroups() throws PspException {
	  if ( !config.isGrouperAuthoritative() ) {
		  LOG.warn("{}: Not doing group cleanup because grouper is not marked as authoritative in provisioner configuration", getName());
		  return;
	  }
	  
	  tsUserCache_shortTerm.clear();
	  tsGroupCache_shortTerm.clear();
	  try {
		MDC.put("step", "setup/");
		Set<GrouperGroupInfo> groupsForThisProvisioner = new HashSet<GrouperGroupInfo>();
		
	    Collection<Group> allGroups = getAllGroupsForProvisioner();
	    
	    for ( Group group : allGroups ) {
	      GrouperGroupInfo grouperGroupInfo = getGroupInfo(group);
	      groupsForThisProvisioner.add(grouperGroupInfo);
	    }
	    
        Map<GrouperGroupInfo, TSGroupClass> tsGroups;
	    if ( groupsForThisProvisioner.size() == 0 ) 
	      tsGroups = Collections.EMPTY_MAP;
	    else
	      tsGroups = fetchTargetSystemGroups(groupsForThisProvisioner);
	    
	    MDC.put("step", "clean/");
	    doFullSync_cleanupExtraGroups(groupsForThisProvisioner, tsGroups);
	  }
	  catch (PspException e) {
		  LOG.error("Problem while looking for and removing extra groups: {}", e.getMessage());
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
   * This then calls (abstract) doFullSync(group, tsGroup, correctSubjects, correctTSUsers).
   * 
   * This method is final to make it clear that the abstract signature should be
   * overridden and to prevent subclasses from overriding this one by mistake.
   * 
   * @param grouperGroupInfo
   * @throws PspException
   */
  final void doFullSync(GrouperGroupInfo grouperGroupInfo) throws PspException {
    Set<Member> groupMembers = grouperGroupInfo.getMembers();
    Set<Subject> correctSubjects = new HashSet<Subject>();
    
    for (Member member : groupMembers) {
      Subject subject = member.getSubject();
      if ( subject.getTypeName().equalsIgnoreCase(SubjectTypeEnum.PERSON.getName()) ) {
        correctSubjects.add(subject);
      }
    }

    if ( correctSubjects.size() > 0 )
      prepareUserCache(correctSubjects);
    
    TSGroupClass tsGroup = tsGroupCache_shortTerm.get(grouperGroupInfo);
    Set<TSUserClass> correctTargetSystemUsers = new HashSet<TSUserClass>();
    
    for ( Subject correctSubject: correctSubjects ) {
      TSUserClass tsUser = tsUserCache_shortTerm.get(correctSubject);
      if ( tsUser != null )
        correctTargetSystemUsers.add(tsUser);
    }

    try {
      MDC.put("step", "prov/");
      doFullSync(grouperGroupInfo, tsGroup, correctSubjects, correctTargetSystemUsers);
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

  protected GrouperGroupInfo getGroupInfo(Group group) {
    String groupName = group.getName();
    GrouperGroupInfo result = grouperGroupInfoCache.get(groupName);
    if ( result == null ) {
      result = new GrouperGroupInfo(group);
      grouperGroupInfoCache.put(groupName, result);
    }
    return result;
  }
  
  
  protected GrouperGroupInfo getGroupInfo(String groupName) {
    GrouperGroupInfo grouperGroupInfo = grouperGroupInfoCache.get(groupName);
    
    // Return group if it was cached
    if ( grouperGroupInfo != null )
      return grouperGroupInfo;
    
    try {
      // Look for an existing grouper group
	    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(false), groupName, false);
	    
	    if ( group != null ) {
	      return getGroupInfo(group);
	    }
    }
    catch (GroupNotFoundException e) {
      LOG.error("Unable to find existing group '{}'", groupName);
    }
    
    try {
      // If an existing grouper group wasn't found, look for a PITGroup
        PITGroup pitGroup = PITGroupFinder.findMostRecentByName(groupName, false);
  	    
  	    if ( pitGroup != null ) {
  	    	grouperGroupInfo = new GrouperGroupInfo(pitGroup);
  	    	grouperGroupInfoCache.put(groupName, grouperGroupInfo);
  	    	return grouperGroupInfo;
  	    }
    }
    catch (GroupNotFoundException e) {
      LOG.error("Unable to find PIT group '{}'", groupName);
    }
    
    return null;
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
  public Set<Group> getAllGroupsForProvisioner() {
    Set<Group> result = new HashSet<Group>();
    
    Set<Group> interestingGroups = new HashSet<Group>();
    for ( String attribute : getConfig().getAttributesUsedInGroupSelectionExpression() ) {
      Set<Stem> foldersReferencingAttribute;
      Set<Group> groupsReferencingAttribute;
      
      if ( getConfig().isAttributesUsedInGroupSelectionExpressionAreComparedToProvisionerName() ) {
        foldersReferencingAttribute = new StemFinder().assignNameOfAttributeDefName(attribute).assignAttributeValue(getName()).findStems();
        groupsReferencingAttribute = new GroupFinder().assignNameOfAttributeDefName(attribute).assignAttributeValue(getName()).findGroups();
      }
      else {
        foldersReferencingAttribute = new StemFinder().assignNameOfAttributeDefName(attribute).findStems();
        groupsReferencingAttribute = new GroupFinder().assignNameOfAttributeDefName(attribute).findGroups();
      }
    
      LOG.info("{}: There are {} folders that match {} attribute", new Object[]{getName(), foldersReferencingAttribute.size(), attribute});
      LOG.info("{}: There are {} groups that match {} attribute", new Object[]{getName(), groupsReferencingAttribute.size(), attribute});
      
      interestingGroups.addAll(groupsReferencingAttribute);
      for ( Stem folder : foldersReferencingAttribute ) {
        Set<Group> groupsUnderFolder;
        
        groupsUnderFolder = new GroupFinder().assignParentStemId(folder.getId()).assignStemScope(Scope.SUB).findGroups();
        
        LOG.info("{}: There are {} groups underneath folder {}", new Object[]{getName(), groupsUnderFolder.size(), folder.getName()});
        interestingGroups.addAll(groupsUnderFolder);
      }
    }
    
    for ( Group group : interestingGroups ) {
      GrouperGroupInfo grouperGroupInfo = new GrouperGroupInfo(group);
      if ( shouldGroupBeProvisioned(grouperGroupInfo) )
        result.add(group);
    }

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
   * @param group
   * @return
   */
  protected boolean shouldGroupBeProvisioned(GrouperGroupInfo grouperGroupInfo) {
    if ( grouperGroupInfo.hasGroupBeenDeleted() ) {
      return false;
    }
    
    String resultString = evaluateJexlExpression(config.getGroupSelectionExpression(), null, grouperGroupInfo);
    
    boolean result = BooleanUtils.toBoolean(resultString);
    
    if ( result )
      LOG.debug("{}: Group {} matches group-selection filter.", getName(), grouperGroupInfo);
    else
      LOG.trace("{}: Group {} does not match group-selection filter.", getName(), grouperGroupInfo);
    
    return result;
  }

  public String getName() {
    return provisionerName;
  }

  public void provisionBatchOfItems(List<ProvisioningWorkItem> allWorkItems) {
	  
	// Mark the items as successful if we are not enabled.
	// Note: They are being marked as successful so that there is an easy mechanism
	// to get a provisioner up to date with the changelog (or other event system). 
	// Additionally, if you just don't want to process events, then you can remove
	// this provisioner from the configuration.
	if ( ! config.isEnabled() ) {
		LOG.warn("{} is disabled. Provisioning not being done, and marking requested items as complete.", getName());
		for ( ProvisioningWorkItem workItem : allWorkItems ) 
			workItem.markAsSuccess("Provisioner %s is not enabled", getName());
		return;
	}
	
	// Let's see if there is a reason to flush our group cache. In particular, 
	// we flush group information when groups and their attributes are edited
	// because we don't know what attributes of groups could be used in various
	// JEXL expressions
	MDC.put("step", "cache_eval");
	try {
	  flushCachesIfNecessary(allWorkItems);
	}
    catch (PspException e) {
      LOG.error("Unable to evaluate our caches", e);
      MDC.remove("step");
      throw new RuntimeException("No entries provisioned. Cache evaluation failed: " + e.getMessage(), e);
    }
	
	// Let the provisioner filter out any unnecessary work items.
	// This particularly filters out groups that we're not supposed to provision
	List<ProvisioningWorkItem> filteredWorkItems;
    MDC.put("step", "filter/");
    try {
      filteredWorkItems = filterWorkItems(allWorkItems);
    }
    catch (PspException e) {
      LOG.error("Unable to filter the provisioning batch", e);
      MDC.remove("step");
      throw new RuntimeException("No entries provisioned. Batch-filtering failed: " + e.getMessage(), e);
    }
    
    // Tell the provisioner about this batch of workItems
    MDC.put("step", "start/");
    try {
      startProvisioningBatch(allWorkItems);
    }
    catch (PspException e) {
      LOG.error("Unable to begin the provisioning batch", e);
      MDC.remove("step");
      throw new RuntimeException("No entries provisioned. Batch-Start failed: " + e.getMessage(), e);
    }
    
    // Go through the workItems that were not marked as processed by the startProvisioningBatch
    // and provision them
    for ( ProvisioningWorkItem workItem : allWorkItems ) {
      MDC.put("step", String.format("prov/%s/", workItem.getMdcLabel()));
      if ( !workItem.hasBeenProcessed() ) {
        try {
          provisionItem(workItem);
        }
        catch (PspException e) {
          LOG.error( String.format("Problem provisioning %s: %s", workItem), e);
          workItem.markAsFailure(e.getMessage());
        }
      }
    }
    
    // Do 'finish' task for workItems that are not marked as processed before now
    
    MDC.put("step", "fin/");
    List<ProvisioningWorkItem> workItemsToFinish = new ArrayList<ProvisioningWorkItem>();
    try {
      for ( ProvisioningWorkItem workItem : allWorkItems ) {
        if ( !workItem.hasBeenProcessed() )
          workItemsToFinish.add(workItem);
      }
      finishProvisioningBatch(workItemsToFinish);
    }
    catch (PspException e) {
      LOG.error("Problem completing provisioning batch", e);
      for ( ProvisioningWorkItem workItem : workItemsToFinish ) {
        if ( !workItem.hasBeenProcessed() )
          workItem.markAsFailure("Unable to finish provisioning (%s)", e.getMessage());
      }
    }
    MDC.remove("step");
  }


  /**
   * Look at the batch of workItems and flush caches necessary to process the entries
   * properly. For instance, if we're being notified that an ATTRIBUTE-ASSIGNMENT has
   * occurred, we can flush our cache to get the best information possible.
   * 
   * @param allWorkItems
   * @throws PspException
   */
  protected boolean flushCachesIfNecessary(List<ProvisioningWorkItem> allWorkItems)  throws PspException{
    for (ProvisioningWorkItem workItem : allWorkItems ) {
      if ( workItemMightAffectCachedData(workItem) ) {
        LOG.info("{}: Flushing group cache because of possible side effects of {}", getName(), workItem);
        grouperGroupInfoCache.clear();
        return true;
      }
    }
    LOG.info("{}: Keeping caches in tact for provisioning batch", getName());
    return false;
  }


  /**
   * Evaluate whether a workItem might change cached information and, therefore, be
   * a reason to flush our group cache before processing this batch of events.
   * 
   * @param workItem
   * @return
   */
  protected boolean workItemMightAffectCachedData(ProvisioningWorkItem workItem) {
    if ( workItem.isChangingGroupOrStemInformation() )
      return true;
    else
      return false;
  }


  public boolean isFullSyncMode() {
    return fullSyncMode;
  }


  
  public void setFullSyncMode(boolean fullSyncMode) {
    this.fullSyncMode = fullSyncMode;
  }


  @Override
  public String toString() {
    return getClass().getSimpleName() +"[" + provisionerName + "]";
  }

}
