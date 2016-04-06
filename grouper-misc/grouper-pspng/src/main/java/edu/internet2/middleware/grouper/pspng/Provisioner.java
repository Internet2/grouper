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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


/**
 * Top-Level provisioner class of PSP. 
 * 
 * We expect that all subclasses would override the following two methods:
 *     addMembership & deleteMembership
 *   
 * And that some subclasses would override these two methods:
 *     createGroup & deleteGroup
 *   
 *   
 * The Provisioner lifecycle is as follows:
 * -Constructed(Name and ProvisionerProperties subclass)
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
 * @author Bert Bee-Lindgren
 *
 */
public abstract class Provisioner {
  static final Logger STATIC_LOG = LoggerFactory.getLogger(Provisioner.class);
  
  protected final Logger LOG;
  final public String provisionerName;
  
  // Cache groups by groupName key
  final GrouperCache<String, Group> grouperGroupCache;
  final GrouperCache<String, Map<String, Object>> grouperGroupJexlMapCache;
  
  // Cache subjects by sourceId__subjectId key
  final GrouperCache<String, Subject> grouperSubjectCache;

  // Cache TargetSystemUsers by Subject. This is typically a long-lived cache
  // used across provisioning batches. These are only fetched and cached
  // if our config has needsTargetSystemUsers=true.
  final GrouperCache<Subject, TargetSystemUser> targetSystemUserCache;
  
  // This stores TargetSystemUserss during the provisioning batch. This Map might seem
  // redundant to the targetSystemUserCache, but it is needed for
  // two reasons: 1) To make sure items are not flushed during the provisioning batch
  // like they could be within a GrouperCache; 2) To know if a TargetSystemUser really
  // doesn't exist (getTargetSystemUser might do a lookup every time a nonexistent
  // user is requested. By using a map populated once per provisioning batch, we don't
  // have to do extra lookups during the batch).
  // This map is populated by startProvisioningBatch and emptied by finishProvisioningBatch
  // if our config has needsTargetSystemUsers=true.
  private Map<Subject, TargetSystemUser> tsUserCache_shortTerm = new HashMap<Subject, TargetSystemUser>();
  
  
  // Cache TargetSystemGroups by Group. This is a long-lived cache, typically used across
  // several provisioning batches. These are only fetched and cached if our config
  // has needsTargetSystemGroups=true.
  final GrouperCache<Group, TargetSystemGroup> targetSystemGroupCache;
  
  // This stores TargetSystemGroups during the provisioning batch. This Map might seem
  // redundant to the targetSystemGroupCache, but it is needed for
  // two reasons: 1) To make sure items are not flushed during the provisioning batch
  // like they could be within a GrouperCache; 2) To know if a TargetSystemGroup really
  // doesn't exist (a GrouperCache doesn't differentiate between an uncached value and
  // a nonexistent group. By using a map populated once per provisioning batch, we don't
  // do futile lookups during the batch).
  //
  // This map is populated by startProvisioningBatch and emptied by finishProvisioningBatch
  // if our config has needsTargetSystemGroups=true.
  private Map<Group, TargetSystemGroup> tsGroupCache_shortTerm = new HashMap<Group, TargetSystemGroup>();

  // This is used during provisioning so everyone can get access to the current
  // work item while we're looping through the work items of a batch
  private ThreadLocal<ProvisioningWorkItem> currentWorkItem = new ThreadLocal<ProvisioningWorkItem>();

  /**
   * Should this provisioner operate in Full-Sync mode? This might mean fetching all members of a group
   * which can be expensive in an incremental-sync, but is worth the trouble in a full-sync.
   */
  protected boolean fullSyncMode = false;
  
  final ProvisionerProperties config;
  
  
  /**
   * What class holds what is necessary for our configuration
   * @return
   */
  public static Class<? extends ProvisionerProperties> getPropertyClass() {
    return ProvisionerProperties.class;
  }
  
  
  public Provisioner(String provisionerName, ProvisionerProperties config) {
    LOG = LoggerFactory.getLogger(String.format("%s.%s", getClass().getName(), provisionerName ));
    
    this.config = config;
    this.provisionerName = provisionerName;
    
    grouperGroupCache 
      = new GrouperCache<String, Group>(String.format("PSP-%s-GrouperGroupCache", getName()),
          config.getGrouperGroupCacheSize(),
          false,
          config.getGrouperDataCacheTime_secs(),
          config.getGrouperDataCacheTime_secs(),
          false);
    
    grouperGroupJexlMapCache
      = new GrouperCache<String, Map<String, Object>>(String.format("PSP-%s-GrouperGroupJexlCache", getName()),
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
      = new GrouperCache<Subject, TargetSystemUser>(String.format("PSP-%s-TargetSystemUserCache", getName()),
          config.getGrouperSubjectCacheSize(),
          false,
          config.getGrouperDataCacheTime_secs(),
          config.getGrouperDataCacheTime_secs(),
          false);
    
    targetSystemGroupCache 
      = new GrouperCache<Group, TargetSystemGroup>(String.format("PSP-%s-TargetSystemGroupCache", getName()),
          config.getGrouperGroupCacheSize(),
          false,
          config.getGrouperDataCacheTime_secs(),
          config.getGrouperDataCacheTime_secs(),
          false);
  }
  
  // Get ready for a provisioning batch. If this is overridden, make sure you call super()
  // at the beginning of your overridden version.
  public void startProvisioningBatch(List<ProvisioningWorkItem> workItems) throws PspException {
    LOG.info("Starting provisioning batch of {} items", workItems);
    Set<Group> groups = new HashSet<Group>();
    Set<Subject> subjects = new HashSet<Subject>();
    
    for ( ProvisioningWorkItem workItem : workItems) {
      Group group = workItem.getGroup(this);
      if ( group == null )
        continue;
      
      if ( !shouldGroupBeProvisioned(group) )
        workItem.markAsSuccess("Group is not selected for provisioning by groupSelectionExpression");
      
      groups.add(group);
      
      Subject s = workItem.getSubject(this);
      
      if ( s != null )
        subjects.add(s);
    }
    
    prepareGroupCache(groups);
    prepareUserCache(subjects);
  }
  
  // Finish and/or clean up after a provisioning batch. If this is overridden, make sure you 
  // call super() at the END of your overridden version
  public void finishProvisioningBatch(List<ProvisioningWorkItem> workItems) throws PspException {
    tsUserCache_shortTerm.clear();
    tsGroupCache_shortTerm.clear();
    
    LOG.info("Done with provisining batch");
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
  protected final String evaluateJexlExpression(String expression, Subject subject, Group group,
      Object... keysAndValues) {
    
    LOG.debug("Evaluating Jexl expression: {}", expression);
    
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
        group, 
        group==null ? null : tsGroupCache_shortTerm.get(group));
    
    // Give our config a chance to add information
    config.populateElMap(variableMap);
    
    String result = GrouperUtil.substituteExpressionLanguage(expression, variableMap, true, false, false);

    return result;
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
      TargetSystemUser tsUser, Group group, TargetSystemGroup tsGroup) {
    variableMap.put("provisionerType", getClass().getSimpleName());
    variableMap.put("provisionerName", getName());

    if ( subject != null ) 
      variableMap.put("subject", subject);
      
    if ( tsUser != null )
        variableMap.put("tsUser",  tsUser.getJexlMap());
    
    if ( group != null ) {
      Map<String, Object> groupMap = getGroupJexlMap(group);
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
    LOG.info("Starting to cache user information for {} items", subjects.size());
    tsUserCache_shortTerm.clear();
    
    // Nothing to do if TargetSystemUsers are not used by this provisioner
    if ( ! config.needsTargetSystemUsers() )
      return;
    Collection<Subject> subjectsToFetch = new ArrayList<Subject>();
    
    for (Subject s : subjects) {
      // See if the subject is already cached.
      TargetSystemUser cachedTSU = targetSystemUserCache.get(s);
      if ( cachedTSU != null )
        // Cache user in shortTerm cache as well as refresh it in longterm cache
        cacheUser(s, cachedTSU);
      else
        subjectsToFetch.add(s);
    }
    
    List<List<Subject>> batchesOfSubjectsToFetch = PspUtils.chopped(subjectsToFetch, config.getUserSearch_batchSize());
    
    for (List<Subject> batchOfSubjectsToFetch : batchesOfSubjectsToFetch ) {
      Map<Subject, TargetSystemUser> fetchedData;
      
      try {
        fetchedData = fetchTargetSystemUsers(batchOfSubjectsToFetch);
        // Save the fetched data in our cache
        for ( Entry<Subject, TargetSystemUser> subjectInfo : fetchedData.entrySet() )
          cacheUser(subjectInfo.getKey(), subjectInfo.getValue());
      }
      catch (PspException e1) {
        // Batch-fetching failed. Let's see if we can narrow it down to a single
        // Subject
          for ( Subject subject : batchOfSubjectsToFetch ) {
            try {
              TargetSystemUser tsUser = fetchTargetSystemUser(subject);
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
          TargetSystemUser newTSUser = createUser(subj);
          if ( newTSUser != null )
            cacheUser(subj, newTSUser);
        }
        else
          LOG.warn("{}: User not found in target system: {}", getName(), subj.getId());
      }
    }
  }

 
  /**
   * This makes sure all the Groups referenced by workItems are in groupMap_shortTerm. Of
   * course if our config says needsTargetSystemGroups is False, then the groupMap will
   * be empty.
   * 
   * @param workItems
   * @throws PspException
   */
  private void prepareGroupCache(Collection<Group> groups) throws PspException {
    LOG.info("Starting to cache group information for {} items", groups.size());
    tsGroupCache_shortTerm.clear();
    
    // If the target system doesn't need groups, then we obviously don't need to 
    // fetch and cache them
    if ( ! config.needsTargetSystemGroups() )
      return;
    
    // Use a set to deduplicate subjects (that might be mentioned in multiple workItems)
    Collection<Group> groupsToFetch = new ArrayList<Group>();
    
    for (Group g : groups) {
      // See if the group is already cached.
      TargetSystemGroup cachedTSG = targetSystemGroupCache.get(g);
      if ( cachedTSG != null )
        // Cache group in shortTerm cache as well as refresh it in longterm cache
        cacheGroup(g, cachedTSG);
      else
        groupsToFetch.add(g);
    }
    
    List<List<Group>> batchesOfGroupsToFetch = PspUtils.chopped(groupsToFetch, config.getGroupSearch_batchSize());
    
    for ( List<Group> batchOfGroupsToFetch : batchesOfGroupsToFetch ) {
      Map<Group, TargetSystemGroup> fetchedData;
      
      try {
        fetchedData = fetchTargetSystemGroups(batchOfGroupsToFetch);
        // Save the data that was fetched in our cache
        for ( Entry<Group, TargetSystemGroup> groupInfo : fetchedData.entrySet() )
          cacheGroup(groupInfo.getKey(), groupInfo.getValue());
      }
      catch (PspException e1) {
        // Batch-fetching failed. Let's see if we can narrow it down to a single
        // Subject
          for ( Group group : batchOfGroupsToFetch ) {
            try {
              TargetSystemGroup tsGroup = fetchTargetSystemGroup(group);
              cacheGroup(group, tsGroup);
            }
            catch (PspException e2) {
              throw new RuntimeException("Problem fetching information on subject " + group);
            }
          }
      }
    }
    
    for ( Group group : groupsToFetch )
      if ( ! tsGroupCache_shortTerm.containsKey(group) )
        if ( config.areEmptyGroupsSupported() ) {
          TargetSystemGroup tsGroup = createGroup(group);
          cacheGroup(group, tsGroup);
        }
        else
          LOG.warn("{}: Group was not found in target system: {}", getName(), group.getName());
  }

  
  public TargetSystemUser getTargetSystemUser(Subject subject) throws PspException {
    if ( !config.needsTargetSystemUsers() ) 
      throw new IllegalStateException(String.format("%s: system that doesn't need target-system users, but one was requested", getName()));
    
    TargetSystemUser result = tsUserCache_shortTerm.get(subject);
    
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
   * Store Subject-->TargetSystemUser mapping in long-term and short-term caches
   * @param subject
   * @param newTSUser
   */
  private void cacheUser(Subject subject, TargetSystemUser newTSUser) {
    LOG.debug("Adding user to cache: {}", subject);
    targetSystemUserCache.put(subject, newTSUser);
    tsUserCache_shortTerm.put(subject, newTSUser);
  }

  protected void uncacheUser(Subject subject, TargetSystemUser oldTSUser) {
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
   * Store Group-->TargetSystemGroup mapping in long-term and short-term caches
   * @param group
   * @param newTSGroup
   */
  private void cacheGroup(Group group, TargetSystemGroup newTSGroup) {
    LOG.debug("Adding group to cache: {}", group.getName());
    targetSystemGroupCache.put(group, newTSGroup);
    tsGroupCache_shortTerm.put(group, newTSGroup);
  }
  
  
  /**
   * The specified Grouper and TargetSystem groups have changed, remove 
   * them from various caches. 
   * @param group
   * @param oldTSGroup
   */
  protected void uncacheGroup(Group group, TargetSystemGroup oldTSGroup) {
    // If the caller didn't know what Group to flush, let's see if we can find it
    if ( group == null )
      for ( Group g : targetSystemGroupCache.keySet() )
        if ( targetSystemGroupCache.get(g) == oldTSGroup ) {
          group = g;
          break;
        }
    
    if ( group == null ) {
    	LOG.warn("Can't find Grouper Group to uncache from tsGroup {}", oldTSGroup);
    	return;
    }
    
    LOG.debug("Flushing group from cache: {}", group.getName());
    targetSystemGroupCache.remove(group);
    
    grouperGroupCache.remove(group.getName());
    grouperGroupJexlMapCache.remove(group.getName());
  }
  
  /**
   * This fetches user information from the target system. Subclasses that have such TargetSystemUser 
   * information need to override this. 
   * 
   * Notes:
   * 1) The signature of this method is designed for batch fetching. If you cannot fetch batches of
   * information, then loop through the provided users and build a resulting map.
   * 
  * 2) Subclasses SHOULD NOT call the super.fetchTargetSystemUsers version of this
   * @param personSubjects 
   * @param grouperGroups
   * @return
   * @throws PspException
   */
  protected Map<Subject, TargetSystemUser> 
  fetchTargetSystemUsers(Collection<Subject> personSubjects) throws PspException {
    throw new RuntimeException(String.format("fetchTargetSystemUsers( ) is not implemented by %s or the implementation is incorrectly calling the superclass version.",
        getClass().getName()));
  }
  

  /**
   * This fetches group information from the target system. Subclasses that have such TargetSystemGroup 
   * information need to override this. 
   * 
   * Notes:
   * 1) The signature of this method is designed for batch fetching. If you cannot fetch batches of
   * information, then loop through the provided groups and build a resulting map.
   * 
   * 2) Subclasses SHOULD NOT call the super.fetchTargetSystemGroups version of this
   * @param grouperGroups
   * @return
   * @throws PspException
   */
  protected Map<Group, TargetSystemGroup> 
  fetchTargetSystemGroups(Collection<Group> grouperGroups) throws PspException  {
    throw new RuntimeException(String.format("fetchTargetSystemGroups( ) is not implemented by %s or the implementation is incorrectly calling the superclass version.",
        getClass().getName()));
  }


  /**
   * Lookup a single TargetSystemUser for a single Subject. If you have several such mappings to look up,
   * you should use the (plural version) fetchTargetSystemUsers( ) instead, as that will have an opportunity 
   * to do faster batch fetching.
   * 
   * Note: This is final. Systems that have/need TargetSystemUsers need to override fetchTargetSystemUsers.
   * 
   * @param personSubject
   * @return
   * @throws PspException
   */
  protected final TargetSystemUser fetchTargetSystemUser(Subject personSubject) throws PspException {
    // Forward this singluar subject to the multi-subject version.
    Map<Subject, TargetSystemUser> result = fetchTargetSystemUsers(Arrays.asList(personSubject));
    return result.get(personSubject);
  }
  
  /**
   * Lookup a single TargetSystemGroup for a single (grouper) Group. If you have several such mappings to look up,
   * you should use the (plural version) fetchTargetSystemGroups( ) instead, as that will have an opportunity to do 
   * faster batch fetching.
   * 
   * Note: This is final. Systems that have/need TargetSystemGroups need to override fetchTargetSystemGroups.
   * 
   * @param grouperGroup
   * @return
   * @throws PspException
   */
  protected final TargetSystemGroup fetchTargetSystemGroup(Group grouperGroup) throws PspException {
    // Forward this singluar Group to the multi-subject version.
    Map<Group, TargetSystemGroup> result = fetchTargetSystemGroups(Arrays.asList(grouperGroup));
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
  protected TargetSystemUser createUser(Subject personSubject) throws PspException {
    return null;
  }
  
  /**
   * Provisioning a new Group in the target system. This must be overridden in provisioner
   * subclasses that support creating groups. 
   * 
   * @param grouperGroup
   * @return
   * @throws PspException
   */
  protected TargetSystemGroup createGroup(Group grouperGroup) throws PspException {
    return null;
  }
  
  /**
   * Dispatches an event to the right provisionItem_* method, with generally
   * useful parameters. 
   * 
   * There is no need to override this method when implementing basic provisioning 
   * (add/delete groups and members), but a Provisioner subclasses might override this 
   * to capture other CategoryAndActions combinations. 
   * 
   * @param workItem
   */
  protected void provisionItem(ProvisioningWorkItem workItem) throws PspException {
    LOG.info("Starting provisioning of item: {}", workItem);
    
    currentWorkItem.set(workItem);
    ChangeLogEntry entry = workItem.getChangelogEntry();
    
    try {
      if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD ))
      {
        Group group = workItem.getGroup(this);
        
        if ( tsGroupCache_shortTerm.containsKey(group) )
          workItem.markAsSuccess("Group %s already exists", group.getName());
        else
          createGroup(group);
      }
      else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE ))
      {
        Group group = workItem.getGroup(this);
        TargetSystemGroup tsGroup = tsGroupCache_shortTerm.get(group);
        
        if ( tsGroupCache_shortTerm.containsKey(group) )
          deleteGroup(group, tsGroup);
        else
          workItem.markAsSuccess("Group %s had already been deleted", group.getName());
      }
      else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD))
      {
        Group group = workItem.getGroup(this);
        TargetSystemGroup tsGroup = tsGroupCache_shortTerm.get(group);
        Subject subject = workItem.getSubject(this);
        TargetSystemUser tsUser = tsUserCache_shortTerm.get(subject);
        
        addMembership(group, tsGroup, subject, tsUser);
      }
      else if ( entry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE))
      {
        Group group = workItem.getGroup(this);
        TargetSystemGroup tsGroup = tsGroupCache_shortTerm.get(group);
        Subject subject = workItem.getSubject(this);
        TargetSystemUser tsUser = tsUserCache_shortTerm.get(subject);
  
        deleteMembership(group, tsGroup, subject, tsUser);
      }
      else
      {
        LOG.info("Not a supported change: {}", entry.toString());
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
		MDC.put("step", "setup");
		Set<Group> groupsForThisProvisioner = new HashSet<Group>();
		
	    Collection<Group> allGroups = GrouperDAOFactory.getFactory().getGroup().getAllGroups();
	    
	    for ( Group group : allGroups ) 
	      if ( shouldGroupBeProvisioned(group) )
	        groupsForThisProvisioner.add(group);
	    
	    Map<Group, TargetSystemGroup> tsGroups = fetchTargetSystemGroups(groupsForThisProvisioner);
	    
	    MDC.put("step", "clean");
	    duFullSync_cleanupExtraGroups(groupsForThisProvisioner, tsGroups);
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
   * @param group
   * @throws PspException
   */
  final void doFullSync(Group group) throws PspException {
	  
	if ( !config.isEnabled() ) {
		LOG.warn("{} is diabled. Full-sync not being done.", getName());
		return;
	}
    tsUserCache_shortTerm.clear();
    tsGroupCache_shortTerm.clear();
    
    MDC.put("step", "setup/");
    Set<Member> groupMembers = group.getMembers();
    Set<Subject> correctSubjects = new HashSet<Subject>();
    
    for (Member member : groupMembers) {
      Subject subject = member.getSubject();
      if ( subject.getTypeName().equalsIgnoreCase(SubjectTypeEnum.PERSON.getName()) ) {
        correctSubjects.add(subject);
      }
    }
    ProvisioningWorkItem workItemForWholeFullSync = new ProvisioningWorkItem("FullSync: " + group.getName(), group, null);
    currentWorkItem.set(workItemForWholeFullSync);
    startProvisioningBatch(Arrays.asList(workItemForWholeFullSync));

    prepareGroupCache(Arrays.asList(group));
    prepareUserCache(correctSubjects);
    
    TargetSystemGroup tsGroup = tsGroupCache_shortTerm.get(group);
    Set<TargetSystemUser> correctTargetSystemUsers = new HashSet<TargetSystemUser>();
    
    for ( Subject correctSubject: correctSubjects ) {
      TargetSystemUser tsUser = tsUserCache_shortTerm.get(correctSubject);
      if ( tsUser != null )
        correctTargetSystemUsers.add(tsUser);
    }

    try {
      MDC.put("step", "prov/");
      doFullSync(group, tsGroup, correctSubjects, correctTargetSystemUsers);
      MDC.put("step", "finish/");
      finishProvisioningBatch(Arrays.asList(workItemForWholeFullSync));
    }
    finally {
      MDC.remove("step");
      tsUserCache_shortTerm.clear();
      tsGroupCache_shortTerm.clear();
    }
  }

  /**
   * This method's responsibility is to make sure that group's only provisioned memberships are those
   * of correctSubjects. Extra subjects should be removed. 
   * 
   * Before this is called, the following have occurred:
   *   -a ProvisioningWorkItem was created representing the whole Full Sync, and it was marked
   *    as the current provisioning item
   *   -StartProvisioningBatch was called
   *   -TargetSystemGroup- and TargetSystemUser-caches are populated with the group and CORRECT Subjects
   *   
   * Also, remember that fullSyncMode=true for provisioners doing full-sync, so TargetSystemUsers and
   * TargetSystemGroups should have the extra information needed to facilitate full syncs.
   * 
   * @param group Grouper group to fully synchronize with target system
   * @param tsGroup TargetSystemGroup that maps to group.
   * @param correctSubjects What subjects are members in the Grouper Registry
   * @param correctTSUsers Collection of TargetSystemUsers which map to the correctSubjects. This will be empty
   * for provisioners that do not use TargetSystemUsers.
   */
  protected abstract void doFullSync(
      Group group, TargetSystemGroup tsGroup, 
      Set<Subject> correctSubjects, Set<? extends TargetSystemUser> correctTSUsers) throws PspException;

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
  protected abstract void duFullSync_cleanupExtraGroups(
		  Set<Group> groupsForThisProvisioner, 
		  Map<Group, TargetSystemGroup> tsGroups) throws PspException;
  

  /**
   * Action method that handles membership additions where a person-subject is added to a 
   * group. The top-level Provisioner class implementation is abstract, and, of course, 
   * this method is expected to be overridden by every provisioner subclass to accomplish 
   * something useful. 
   * 
   * @param group The group to which the subject needs to be added as a member
   * @param tsGroup A TargetSystemGroup created for group by fetchTargetSystemGroup. This will
   * be null for systems that do not need target system groups.
   * @param subject The (person) subject that needs to be provisioned as a member of 'group'
   * @param tsUser A TargetSystemUser created for the subject by fetchTargetSystemUser. This will
   * be null for systems that do not need target system users.
   */

  protected abstract void addMembership(Group group, TargetSystemGroup tsGroup,
      Subject subject, TargetSystemUser tsUser) throws PspException;
  
  
  /**
   * Abstract action method that handles membership removals. 
   * 
   * Note: This method is called for MembershipDelete events for a non-group member.
   * 
   * @param group The group to which the subject needs to be removed as a member
   * @param tsGroup TargetSystemGroup for the 'group.' This is null for systems that do not need
   * target-system group info
   * @param subject The subject that needs to be deprovisioned as a member of 'group'
   * @param tsUser TargetSystemUser for the 'subject.' This is null for systems that do not need
   * target-system user info
   */

  protected abstract void deleteMembership(Group group, TargetSystemGroup tsGroup,
      Subject subject, TargetSystemUser tsUser) throws PspException;

  
  /**
   * Get the ProvisioningWorkItem that this provisioner is currently processing
   * @return
   */
  public ProvisioningWorkItem getCurrentWorkItem() {
    return currentWorkItem.get();
  }
  
  /**
   * Action method that handles group removal. The top-level Provisioner class implementation
   * does nothing. This is expected to be overridden by subclasses to accomplish something useful.
   * @param group
   * @param tsGroup 
   * @param subject
   */
  protected void 
  deleteGroup(Group group, TargetSystemGroup tsGroup) throws PspException {
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

  protected Group getGroup(String groupName) {
    Group group = grouperGroupCache.get(groupName);
    if (group != null) 
      return group;
    
    group = GroupFinder.findByName(GrouperSession.staticGrouperSession(false), groupName, false);
    
    if ( group != null )
      grouperGroupCache.put(groupName, group);
    
    return group;
  }

  
  protected Map<String, Object> getGroupJexlMap(Group group) {
    final String groupName = group.getName();
    Map<String, Object> result = grouperGroupJexlMapCache.get(groupName);
    //if ( result != null )
    //  return result;
    
    result = new HashMap<String, Object>();
    
    result.put("group", group);
    
    Map<String, Object> stemAttributes = PspUtils.getStemAttributes(group);
    result.put("stemAttributes", stemAttributes);

    Map<String, Object> groupAttributes = PspUtils.getGroupAttributes(group);
    result.put("groupAttributes", groupAttributes);

    grouperGroupJexlMapCache.put(groupName, result);
    return result;
  }
  
  /**
   * Evaluate the GroupSelectionExpression to see if group should be processed by this
   * provisioner.
   * 
   * @param group
   * @return
   */
  protected boolean shouldGroupBeProvisioned(Group group) {
    String resultString = evaluateJexlExpression(config.getGroupSelectionExpression(), null, group);
    
    boolean result = BooleanUtils.toBoolean(resultString);
    
    LOG.info("{}: Group {} {} group-selection filter.", 
        getName(), group.getName(), result ? "matches" : "does not match");
    
    return result;
  }

  public String getName() {
    return provisionerName;
  }

  public void provisionBatchOfItems(List<ProvisioningWorkItem> workItems) {
	  
	// Mark the items as successful if we are not enabled.
	// Note: They are being marked as successful so that there is an easy mechanism
	// to get a provisioner up to date with the changelog (or other event system). 
	// Additionally, if you just don't want to process events, then you can remove
	// this provisioner from the configuration.
	if ( ! config.isEnabled() ) {
		LOG.warn("{} is disabled. Provisioning not being done, and marking requested items as complete.", getName());
		for ( ProvisioningWorkItem workItem : workItems ) 
			workItem.markAsSuccess("Provisioner %s is not enabled", getName());
		return;
	}
	
    // Tell the provisioner about this batch of workItems
    MDC.put("step", "start/");
    try {
      startProvisioningBatch(workItems);
    }
    catch (PspException e) {
      LOG.error("Unable to begin the provisioning batch", e);
      MDC.remove("step");
      throw new RuntimeException("No entries provisioned. Batch-Start failed: " + e.getMessage(), e);
    }
    
    // Go through the workItems that were not marked as processed by the startProvisioningBatch
    // and provision them
    MDC.put("step", "prov/");
    for ( ProvisioningWorkItem workItem : workItems ) {
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
      for ( ProvisioningWorkItem workItem : workItems ) {
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


  
  public boolean isFullSyncMode() {
    return fullSyncMode;
  }


  
  public void setFullSyncMode(boolean fullSyncMode) {
    this.fullSyncMode = fullSyncMode;
  }

}
