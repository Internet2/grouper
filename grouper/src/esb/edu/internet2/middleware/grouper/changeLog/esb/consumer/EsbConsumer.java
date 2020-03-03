/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import java.sql.Timestamp;
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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningProcessingResult;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * Class to dispatch individual events to external systems through configured classes.
 * HTTP, HTTTPS and XMPP currently supported.
 * Configure in grouper-loader.properties
 */
public class EsbConsumer extends ChangeLogConsumerBase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //GrouperBuiltinMessagingSystem.createQueue("abc");
    int i=14;
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    RegistryReset._addSubjects(i, i+1);
    Subject subject = SubjectFinder.findById("test.subject." + i, true);
    group.addMember(subject);
  }
  
  /** */
  private EsbListenerBase esbPublisherBase;

  /** */
  private static final Log LOG = GrouperUtil.getLog(EsbConsumer.class);

  /**
   * convert a change log entry to an esb event
   * @param changeLogEntry
   * @param debugMapForEvent
   * @param sendCreatedOnMicros
   * @return the event
   */
  private EsbEventContainer convertChangeLogEntryToEsbEvent(ChangeLogEntry changeLogEntry, Map<String, Object> debugMapForEvent, boolean sendCreatedOnMicros) {
    Long currentId = changeLogEntry.getSequenceNumber();
    debugMapForEvent.put("sequenceNumber", currentId);

    EsbEventContainer esbEventContainer = new EsbEventContainer();
    esbEventContainer.setSequenceNumber(changeLogEntry.getSequenceNumber());
    // for logging
    debugMapOverall.put("currentSequenceNumber", changeLogEntry.getSequenceNumber());

    EsbEvent event = new EsbEvent();
    esbEventContainer.setEsbEvent(event);
    esbEventContainer.setDebugMapForEvent(debugMapForEvent);

    event.setSequenceNumber(Long.toString(currentId));
    
    if (sendCreatedOnMicros) {
      event.setCreatedOnMicros(changeLogEntry.getCreatedOnDb());
    }
    
    ChangeLogTypeBuiltin changeLogTypeBuiltin = ChangeLogTypeBuiltin.retrieveChangeLogTypeByChangeLogEntry(changeLogEntry);

    if (changeLogTypeBuiltin != null) {
      
      // this is a shadow enum
      EsbEventType esbEventType = EsbEventType.valueOfIgnoreCase(changeLogTypeBuiltin.name(), false);
      
      if (esbEventType != null) {
        event.setEventType(esbEventType.name());
        esbEventContainer.setEsbEventType(esbEventType);
        esbEventType.processChangeLogEntry(esbEventContainer, changeLogEntry);
      }
    }

    debugMapForEvent.put("eventType", event.getEventType());

    if (!StringUtils.isBlank(event.getGroupName())) {
      debugMapForEvent.put("groupName", event.getGroupName());
    }
    
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);

    return esbEventContainer;
  }
  
  /**
   * add something to the log if its not zero (so we dont have noise).  Note, if it existed previously, then remove it
   * @param debugMap
   * @param label
   * @param theInt
   */
  public static void logIntegerIfNotZero(Map<String, Object> debugMap, String label, Integer theInt) {
    if (theInt == null || theInt == 0) {
      debugMap.remove(label);
    } else {
      debugMap.put(label, theInt);
    }
  }
  
  /**
   * add something to the log if its not null (so we dont have noise).  Note, if it existed previously, then remove it
   * @param debugMap
   * @param label
   * @param theInt
   */
  public static void logObjectIfNotNull(Map<String, Object> debugMap, String label, Object theObject) {
    if (theObject == null) {
      debugMap.remove(label);
    } else {
      debugMap.put(label, theObject);
    }
  }
  
  /**
   * 
   * @param esbEventContainers
   */
  private void filterInvalidEventTypes(List<EsbEventContainer> esbEventContainers) {
    
    Iterator<EsbEventContainer> iterator = esbEventContainers.iterator();
    int filterInvalidEventTypesSize = 0;
    while (iterator.hasNext()) {
      EsbEventContainer esbEventContainer = iterator.next();

      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

      EsbEvent event = esbEventContainer.getEsbEvent();

      if (event.getEventType() == null) {
        
        filterInvalidEventTypesSize++;
        
        Map<String, Object> debugMapForEvent = esbEventContainer.getDebugMapForEvent();

        String unsupportedEventLabel = "unsupportedEvent_" + event.getType();
        Integer unsupportedEventCount = (Integer)debugMapForEvent.get(unsupportedEventLabel);
        if (unsupportedEventCount == null) {
          unsupportedEventCount = 1;
        } else {
          unsupportedEventCount++;
        }
        debugMapForEvent.put("unsupportEventType", event.getType());
        logIntegerIfNotZero(debugMapOverall, unsupportedEventLabel, unsupportedEventCount);
        iterator.remove();
      }

    }
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);
    logIntegerIfNotZero(debugMapOverall, "filterInvalidEventTypesSize", filterInvalidEventTypesSize);
    this.internal_esbConsumerTestingData.filterInvalidEventTypesSize = filterInvalidEventTypesSize;

    
  }

  /**
   * filter events that happened after the last full sync
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  private void filterByProvisioningFullSync(List<EsbEventContainer> esbEventContainers, GcGrouperSync gcGrouperSync) {

    if (esbEventContainers.size() == 0) {
      return;
    }
    
    long firstChangeLogMicrosSince1970 = esbEventContainers.get(0).getEsbEvent().getCreatedOnMicros();

    // check for full sync
    Timestamp lastFullSync = gcGrouperSync.getLastFullSyncRun();
//    Timestamp lastFullMetadataSync = gcGrouperSync.getLastFullMetadataSyncRun();
    
    // last full sync happened before these records
    long lastFullSyncMicros1970 = lastFullSync == null ? -1 : (1000*lastFullSync.getTime());
//    long lastFullSyncMetadataMicros1970 = lastFullMetadataSync == null ? -1 : (1000*lastFullMetadataSync.getTime());

    // see if any applicable
    if (lastFullSyncMicros1970 < firstChangeLogMicrosSince1970) {
      return;
    }

    Iterator<EsbEventContainer> iterator = esbEventContainers.iterator();
    
    Integer skippedEventsDueToFullSync = GrouperUtil.defaultIfNull((Integer)debugMapOverall.get("skippedEventsDueToFullSync"), 0);

    while (iterator.hasNext()) {
      EsbEventContainer esbEventContainer = iterator.next();

      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

      EsbEvent event = esbEventContainer.getEsbEvent();

//      boolean groupMetadataEvent = esbEventContainer.getEsbEventType() == EsbEventType.GROUP_ADD
//          || esbEventContainer.getEsbEventType() == EsbEventType.GROUP_ADD
//      
      // we can skip anything that happened before the last full sync started
      if (event.getCreatedOnMicros() < lastFullSyncMicros1970) {

        Map<String, Object> debugMapForEvent = esbEventContainer.getDebugMapForEvent();

        debugMapForEvent.put("skippingEventBeforeLastFullSync", lastFullSync);
        skippedEventsDueToFullSync++;
        iterator.remove();
      }

    }
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);

    logIntegerIfNotZero(debugMapOverall, "skippedEventsDueToFullSync", skippedEventsDueToFullSync);
    this.internal_esbConsumerTestingData.skippedEventsDueToFullSync = skippedEventsDueToFullSync;
  }

  /**
   * filter events that dont match a certain EL
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  private void setupRoutingKeys(List<EsbEventContainer> esbEventContainers) {

    String regexRoutingKeyReplacementDefinition = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
        + consumerName + ".regexRoutingKeyReplacementDefinition");
    logObjectIfNotNull(debugMapOverall, "regexRoutingKeyReplacementDefinition", regexRoutingKeyReplacementDefinition);

    if (StringUtils.isBlank(regexRoutingKeyReplacementDefinition)) {
      return;
    }
    
    boolean replaceColonsWithPeriods = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.consumer."
        + consumerName + ".replaceRoutingKeyColonsWithPeriods", true);

    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {

      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

      String routingKey = null;
      String groupName = esbEventContainer.getEsbEvent().getGroupName();
      
      if (StringUtils.isNotBlank(groupName)) {
        
        if (StringUtils.isNotBlank(regexRoutingKeyReplacementDefinition)) {
          
          Map<String, Object> substituteMap = new HashMap<String, Object>();
          substituteMap.put("groupName", groupName);
    
          routingKey = GrouperUtil.substituteExpressionLanguage(regexRoutingKeyReplacementDefinition, substituteMap, true, false, false);;
          
          if (replaceColonsWithPeriods) {
            routingKey = routingKey.replaceAll(":", ".");
          }

          esbEventContainer.setRoutingKey(routingKey);
        }
      }
    }
    
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);

  }  

  /**
   * filter events that dont match a certain EL
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  private void filterByExpressionLanguage(List<EsbEventContainer> esbEventContainers) {

    if (esbEventContainers.size() == 0) {
      return;
    }
    
    String elFilter = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." + consumerName + ".elfilter");

    logObjectIfNotNull(debugMapOverall, "elFilter", elFilter);
    
    if (StringUtils.isBlank(elFilter)) {
      return;
    }
    
    Iterator<EsbEventContainer> iterator = esbEventContainers.iterator();
    
    int skippedEventsDueToExpressionLanguageCount = GrouperUtil.defaultIfNull((Integer)debugMapOverall.get("skippedEventsDueToExpressionLanguageCount"), 0);

    if (this.internal_esbConsumerTestingData.skippedEventsDueToExpressionLanguage == null) {
      this.internal_esbConsumerTestingData.skippedEventsDueToExpressionLanguage = new ArrayList<EsbEventContainer>();
    }
    while (iterator.hasNext()) {
      EsbEventContainer esbEventContainer = iterator.next();

      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

      Map<String, Object> debugMapForEvent = esbEventContainer.getDebugMapForEvent();
      
      EsbEvent event = esbEventContainer.getEsbEvent();

      boolean matchesFilter = matchesFilter(event, elFilter);

      debugMapForEvent.put("matchesFilter", matchesFilter);
      if (!matchesFilter) {
        skippedEventsDueToExpressionLanguageCount++;
        this.internal_esbConsumerTestingData.skippedEventsDueToExpressionLanguage.add(esbEventContainer);
        iterator.remove();
      }

    }
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);

    logIntegerIfNotZero(debugMapOverall, "skippedEventsDueToExpressionLanguageCount", skippedEventsDueToExpressionLanguageCount);
    this.internal_esbConsumerTestingData.skippedEventsDueToExpressionLanguageCount = skippedEventsDueToExpressionLanguageCount;
  }

  /**
   * 
   * @param changeLogEntryList
   * @return
   */
  private List<EsbEventContainer> convertAllChangeLogEventsToEsbEvents(List<ChangeLogEntry> changeLogEntryList, boolean sendCreatedOnMicros) {
    

    List<EsbEventContainer> result = new ArrayList<EsbEventContainer>();
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

      // for logging
      debugMapOverall.put("currentSequenceNumber", changeLogEntry.getSequenceNumber());
      
      Map<String, Object> debugMapForEvent = new LinkedHashMap<String, Object>();

      debugMapForEvent.put("type", "event");
      debugMapForEvent.put("consumerName", consumerName);
      
      EsbEventContainer esbEventContainer = convertChangeLogEntryToEsbEvent(changeLogEntry, debugMapForEvent, sendCreatedOnMicros);

      result.add(esbEventContainer);
      
    }
    
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);

    this.internal_esbConsumerTestingData.convertAllChangeLogEventsToEsbEventsSize = GrouperUtil.length(result);
    
    return result;
  }

  /**
   * 
   * @param esbEventContainersToProcess
   * @param gcGrouperSync
   */
  private GrouperProvisioningProcessingResult processProvisioningMetadata(List<EsbEventContainer> esbEventContainersToProcess, GcGrouperSync gcGrouperSync, GcGrouperSyncJob gcGrouperSyncJob) {
    
    // get group ids which need to be analyzed
    Set<String> groupIds = groupIdsToQueryProvisioningAttributes(esbEventContainersToProcess);
    
    this.grouperProvisioningProcessingResult =
        GrouperProvisioningService.processProvisioningMetadataForGroupIds(gcGrouperSync, groupIds);
    
    grouperProvisioningProcessingResult.setGcGrouperSyncJob(gcGrouperSyncJob);
    
    grouperProvisioningProcessingResult.setGcGrouperSyncLog(gcGrouperSync.getGcGrouperSyncJobDao().jobCreateLog(gcGrouperSyncJob));

    grouperProvisioningProcessingResult.getGcGrouperSyncLog().setStatus(null);
    
    logIntegerIfNotZero(debugMapOverall, "groupIdCountToAddToTarget", GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdsToAddToTarget()));
    this.internal_esbConsumerTestingData.groupIdCountToAddToTarget = GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdsToAddToTarget());
    logIntegerIfNotZero(debugMapOverall, "groupIdCountToRemoveFromTarget", GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdsToRemoveFromTarget()));
    this.internal_esbConsumerTestingData.groupIdCountToRemoveFromTarget = GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdsToRemoveFromTarget());
    logIntegerIfNotZero(debugMapOverall, "gcGrouperSyncGroupsCountInitial", GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdToGcGrouperSyncGroupMap()));
    this.internal_esbConsumerTestingData.gcGrouperSyncGroupsCountInitial = GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdToGcGrouperSyncGroupMap());
    
    // setup heartbeat thread
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {

      @Override
      public void run() {
        EsbConsumer.this.changeLogProcessorMetadata.getHib3GrouperLoaderLog().store();
        
        // periodically log
        if (LOG.isDebugEnabled()) {
          String debugMapToString = GrouperUtil.mapToString(debugMapOverall);
          LOG.debug(debugMapToString);
        }
      }
      
    });
    this.grouperProvisioningProcessingResult.setGcGrouperSyncHeartbeat(gcGrouperSyncHeartbeat);
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }
    
    return grouperProvisioningProcessingResult;
  }

  /**
   * take out events that will be handled by provisioning metadata changes (i.e. group full sync)
   * @param esbEventContainersToProcess
   * @param grouperProvisioningProcessingResult
   */
  private void filterEventsCapturedByProvisioningMetadata(
      List<EsbEventContainer> esbEventContainersToProcess, GrouperProvisioningProcessingResult grouperProvisioningProcessingResult) {
    if (grouperProvisioningProcessingResult == null || GrouperUtil.length(esbEventContainersToProcess) == 0) {
      return;
    }
    Set<String> groupIdsToIgnore = new HashSet<String>();
    
    groupIdsToIgnore.addAll(GrouperUtil.nonNull(grouperProvisioningProcessingResult.getGroupIdsToAddToTarget()));
    groupIdsToIgnore.addAll(GrouperUtil.nonNull(grouperProvisioningProcessingResult.getGroupIdsToRemoveFromTarget()));
    
    if (GrouperUtil.length(groupIdsToIgnore) > 0) {
      
      int eventsFilteredByGroupEvents = 0;
      
      Iterator<EsbEventContainer> iterator = esbEventContainersToProcess.iterator();
      while (iterator.hasNext()) {
        
        EsbEventContainer esbEventContainer = iterator.next();

        // for logging
        debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

        String groupId = esbEventContainer.getEsbEvent().getGroupId();

        if (!StringUtils.isBlank(groupId)  && groupIdsToIgnore.contains(groupId)) {
          esbEventContainer.getDebugMapForEvent().put("removeEventSinceProvisioningChangeOnGroup", groupId);
          iterator.remove();
          eventsFilteredByGroupEvents++;
        }
        
      }

      // for logging
      debugMapOverall.put("currentSequenceNumber", null);

      logIntegerIfNotZero(debugMapOverall, "eventsFilteredByGroupEvents", eventsFilteredByGroupEvents);
      this.internal_esbConsumerTestingData.eventsFilteredByGroupEvents = eventsFilteredByGroupEvents;

    }
    
  }
  
  /**
   * 
   */
  private GrouperProvisioningProcessingResult grouperProvisioningProcessingResult = null;
  
  /**
   * 
   * @return
   */
  public GrouperProvisioningProcessingResult getGrouperProvisioningProcessingResult() {
    return grouperProvisioningProcessingResult;
  }

  /**
   * if we are debugging this consumer
   */
  private boolean debugConsumer = false;
  
  /**
   * 
   * @return
   */
  public boolean isDebugConsumer() {
    return this.debugConsumer;
  }

  /**
   * consumer name
   */
  private String consumerName = null;

  /**
   * debug map
   */
  private Map<String, Object> debugMapOverall = null;

  
  public Map<String, Object> getDebugMapOverall() {
    return debugMapOverall;
  }

  /**
   * change log processor metadata
   */
  private ChangeLogProcessorMetadata changeLogProcessorMetadata;
  
  /**
   * 
   * @return metadata
   */
  public ChangeLogProcessorMetadata getChangeLogProcessorMetadata() {
    return this.changeLogProcessorMetadata;
  }

  /**
   * 
   * @param changeLogProcessorMetadata1
   */
  public void setChangeLogProcessorMetadata(
      ChangeLogProcessorMetadata changeLogProcessorMetadata1) {
    this.changeLogProcessorMetadata = changeLogProcessorMetadata1;
  }

  public static class EsbConsumerTestingData {
    public int changeLogEntryListSize;
    public int convertAllChangeLogEventsToEsbEventsSize;
    public int filterInvalidEventTypesSize;
    public String provisionerTarget;
    public String provisionerJobSyncType;
    public int skippedEventsDueToFullSync;
    public int groupIdCountToAddToTarget;
    public int groupIdCountToRemoveFromTarget;
    public int gcGrouperSyncGroupsCountInitial;
    public int eventsFilteredByGroupEvents;
    public int eventsWithAddedSubjectAttributes;
    public int skippedEventsDueToExpressionLanguageCount;
    public List<EsbEventContainer> skippedEventsDueToExpressionLanguage;
    public int gcGrouperSyncGroupGroupIdsToRetrieveCount;
    public int eventsFilteredByNotProvisionable;
    public int eventsFilteredNotTrackedAtAll;
    public int filterByNotProvisionablePreSize;
    public int filterByNotProvisionablePostSize;
    public int eventsFilteredNotTrackedOrProvisionable;
    public int gcGrouperSyncGroupsRetrievedByEventsSize;
  }

  /**
   * testing data for unit tests
   */
  public EsbConsumerTestingData internal_esbConsumerTestingData = new EsbConsumerTestingData();
  
  
  /**
   * @see ChangeLogConsumerBase#processChangeLogEntries(List, ChangeLogProcessorMetadata)
   */
  @Override
  public long processChangeLogEntries(
      List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata1) {
    
    this.internal_esbConsumerTestingData.changeLogEntryListSize = GrouperUtil.length(changeLogEntryList);
    
    this.changeLogProcessorMetadata = changeLogProcessorMetadata1;
    this.consumerName = changeLogProcessorMetadata.getConsumerName();
    long currentId = -1;

    long startNanos = System.nanoTime();
    
    this.debugMapOverall = new LinkedHashMap<String, Object>();

    this.debugMapOverall.put("type", "consumer");
    this.debugMapOverall.put("finalLog", false);
    this.debugMapOverall.put("state", "init");

    debugMapOverall.put("consumerName", this.consumerName);
    
    boolean hasError = false;

    this.debugConsumer = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.consumer." + consumerName
        + ".publisher.debug", false);

    this.grouperProvisioningProcessingResult = null;
    
    // all containers for logging or whatever
    List<EsbEventContainer> allEsbEventContainers = new ArrayList<EsbEventContainer>();

    //try catch so we can track that we made some progress
    try {
      
      List<EsbEventContainer> esbEventContainersToProcess = new ArrayList<EsbEventContainer>();

      debugMapOverall.put("totalCount", GrouperUtil.length(changeLogEntryList));

      //####### STEP 1: convert to esb events
      boolean sendCreatedOnMicros = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.consumer." + this.consumerName
          + ".publisher.sendCreatedOnMicros", true);

      this.debugMapOverall.put("state", "convertAllChangeLogEventsToEsbEvents");
      esbEventContainersToProcess = this.convertAllChangeLogEventsToEsbEvents(changeLogEntryList, sendCreatedOnMicros);
      allEsbEventContainers = new ArrayList<EsbEventContainer>(esbEventContainersToProcess);
      
      //####### STEP 2: filter out event types not needed
      this.debugMapOverall.put("state", "filterInvalidEventTypes");
      this.filterInvalidEventTypes(esbEventContainersToProcess);

      // lets see if we want to filter by provisioner target
      String filterByProvisionerTarget = GrouperLoaderConfig.retrieveConfig()
          .propertyValueString("changeLog.consumer." + consumerName + ".provisionerTarget");
      String filterByProvisionerJobSyncType = GrouperLoaderConfig.retrieveConfig()
          .propertyValueString("changeLog.consumer." + consumerName + ".provisionerJobSyncType");

      GcGrouperSync gcGrouperSync = null;
      GcGrouperSyncJob gcGrouperSyncJob = null;
      
      if (!StringUtils.isBlank(filterByProvisionerTarget)) {

        this.internal_esbConsumerTestingData.provisionerTarget = filterByProvisionerTarget;
        this.internal_esbConsumerTestingData.provisionerJobSyncType = filterByProvisionerJobSyncType;
        
        logObjectIfNotNull(debugMapOverall, "filterByProvisionerTarget", filterByProvisionerTarget);

        if (StringUtils.isBlank(filterByProvisionerJobSyncType)) {
          throw new RuntimeException("Cant have a provisioner target and not: changeLog.consumer." + consumerName + ".provisionerJobSyncType");
        }
        
        if (!sendCreatedOnMicros) {
          throw new RuntimeException("Must send createdOnMicros for provisioning target!");
        }

        {
          long startNanoTime = System.nanoTime();
          // ######### STEP 3: wait for other jobs, and start thread
          this.debugMapOverall.put("state", "waitForRelatedJobsToFinishThenRun");
          gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, filterByProvisionerTarget);
          gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType(filterByProvisionerJobSyncType);
          gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(false);
          int tookMillis = (int)((System.nanoTime()-startNanoTime) / 1000000);
          if (tookMillis > 10) {
            debugMapOverall.put("waitingForRelatedJobsMillis", tookMillis);
          }
        }
        
        // ######### STEP 4: see if we can skip some based on full sync
        this.debugMapOverall.put("state", "filterByProvisioningFullSync");
        this.filterByProvisioningFullSync(esbEventContainersToProcess, gcGrouperSync);
        
        // ######### STEP 5: update provisioning metadata based on attribute changes
        this.debugMapOverall.put("state", "processProvisioningMetadata");
        this.grouperProvisioningProcessingResult = this.processProvisioningMetadata(esbEventContainersToProcess, gcGrouperSync, gcGrouperSyncJob);
        
        // ######### STEP 6: filter out events that are captured in provisioning metadata changes
        this.debugMapOverall.put("state", "filterEventsCapturedByProvisioningMetadata");
        this.filterEventsCapturedByProvisioningMetadata(esbEventContainersToProcess, this.grouperProvisioningProcessingResult);
        
      }

      // ######### STEP 7: add in subject attributes
      this.debugMapOverall.put("state", "addSubjectAttributes");
      this.addSubjectAttributes(esbEventContainersToProcess);
      
      // ######### STEP 8: filter by EL
      this.debugMapOverall.put("state", "filterByExpressionLanguage");
      this.filterByExpressionLanguage(esbEventContainersToProcess);

      if (!StringUtils.isBlank(filterByProvisionerTarget)) {
        
        // ######### STEP 9: retrieve and create group sync objects
        this.debugMapOverall.put("state", "retrieveProvisioningGroupSyncObjects");
        this.retrieveAndCreateProvisioningGroupSyncObjects(esbEventContainersToProcess, gcGrouperSync);
        
        // ######### STEP 10: filter if not provisionable
        this.debugMapOverall.put("state", "filterByNotProvisionable");
        this.filterByNotProvisionable(esbEventContainersToProcess);
        
        // ######### STEP 11: filter by group sync
        this.debugMapOverall.put("state", "filterByProvisioningGroupSync");
        this.filterByProvisioningGroupSync(esbEventContainersToProcess, gcGrouperSync);

        // ######### STEP 12: retrieve member sync objects
        this.debugMapOverall.put("state", "setupProvisioningMemberSyncObjects");
        this.retrieveProvisioningMemberSyncObjects(esbEventContainersToProcess, gcGrouperSync);

//        // ######### STEP 13: retrieve membership sync objects
//        this.debugMapOverall.put("state", "retrieveProvisioningMembershipSyncObjects");
//        this.retrieveProvisioningMembershipSyncObjects(esbEventContainersToProcess, gcGrouperSync);

//        // ######### STEP 14: filter by membership sync objects
//        this.debugMapOverall.put("state", "filterByProvisioningMembershipSyncObjects");
//        this.filterByProvisioningMembershipSyncObjects(esbEventContainersToProcess, gcGrouperSync);

      }

      // ######### STEP 15: setup routing key
      this.debugMapOverall.put("state", "setupRoutingKeys");
      this.setupRoutingKeys(esbEventContainersToProcess);
      
      // ######### STEP 16: fill in more data, e.g. membership update
      this.fillInMoreData(esbEventContainersToProcess);
      
      // ######### STEP 17: send to the publisher
      this.debugMapOverall.put("state", "sendToPublisher");
      String theClassName = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." + consumerName
          + ".publisher.class");
      debugMapOverall.put("publisherClass", theClassName);
      Class<?> theClass = GrouperUtil.forName(theClassName);
      this.esbPublisherBase = (EsbListenerBase) GrouperUtil.newInstance(theClass);
      
      this.esbPublisherBase.setChangeLogProcessorMetadata(this.changeLogProcessorMetadata);
      this.esbPublisherBase.setEsbConsumer(this);
      long lastEventSequenceOverall = allEsbEventContainers.get(allEsbEventContainers.size()-1).getSequenceNumber();

      if (GrouperUtil.length(esbEventContainersToProcess) > 0) {
        long lastEventSequenceToProcess = esbEventContainersToProcess.get(esbEventContainersToProcess.size()-1).getSequenceNumber();
        ProvisioningSyncConsumerResult provisioningSyncConsumerResult = this.esbPublisherBase.dispatchEventList(esbEventContainersToProcess, this.grouperProvisioningProcessingResult);
        currentId = GrouperUtil.defaultIfNull(provisioningSyncConsumerResult.getLastProcessedSequenceNumber(), -1L);

        // if we processed all the records available, then we processed them all
        if (currentId == lastEventSequenceToProcess) {
          currentId = lastEventSequenceOverall;
        }
      } else {
        
        // no records to process, we good
        currentId = lastEventSequenceOverall;
        
      }
      
      this.debugMapOverall.put("state", "done");
      
      if (grouperProvisioningProcessingResult != null && grouperProvisioningProcessingResult.getGcGrouperSyncLog() != null){
        if (currentId == lastEventSequenceOverall) {
          grouperProvisioningProcessingResult.getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.SUCCESS);        
        } else {
          grouperProvisioningProcessingResult.getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.ERROR);        
        }
      }

    } catch (RuntimeException re) {
      if (grouperProvisioningProcessingResult != null && grouperProvisioningProcessingResult.getGcGrouperSyncLog() != null) {
        grouperProvisioningProcessingResult.getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.ERROR);        
      }
      hasError = true;
      debugMapOverall.put("exception", GrouperUtil.getFullStackTrace(re));
      
      changeLogProcessorMetadata.registerProblem(re, "Error processing record " + currentId, currentId);
      if (currentId != -1) {
        currentId--;
      }

      
    } finally {

      if (this.grouperProvisioningProcessingResult != null) {
        GcGrouperSyncHeartbeat.endAndWaitForThread(this.grouperProvisioningProcessingResult.getGcGrouperSyncHeartbeat());

        synchronized (this) {
          try {
            if (this.grouperProvisioningProcessingResult.getGcGrouperSyncJob() != null) {
              this.grouperProvisioningProcessingResult.getGcGrouperSyncJob().assignHeartbeatAndEndJob();
            }
          } catch (RuntimeException re2) {
            if (this.grouperProvisioningProcessingResult.getGcGrouperSyncLog() != null) {
              this.grouperProvisioningProcessingResult.getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.ERROR);
            }
            this.debugMapOverall.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
          }
        }
      }

      long tookMillis = (System.nanoTime() - startNanos) / 1000000;
      debugMapOverall.put("tookMillis", tookMillis);
      String debugMapToString = GrouperUtil.mapToString(debugMapOverall);

      if (this.changeLogProcessorMetadata != null && this.changeLogProcessorMetadata.getHib3GrouperLoaderLog() != null) {
        this.changeLogProcessorMetadata.getHib3GrouperLoaderLog().setJobMessage(debugMapToString);
      }
      
      try {
        if (this.grouperProvisioningProcessingResult != null && this.grouperProvisioningProcessingResult.getGcGrouperSyncLog() != null) {
          this.grouperProvisioningProcessingResult.getGcGrouperSyncLog().setDescription(debugMapToString);
          this.grouperProvisioningProcessingResult.getGcGrouperSyncLog().setJobTookMillis((int)tookMillis);
          this.grouperProvisioningProcessingResult.getGcGrouperSyncLog().setRecordsProcessed(GrouperUtil.length(allEsbEventContainers));
          this.grouperProvisioningProcessingResult.getGcGrouperSync().getGcGrouperSyncLogDao().internal_logStore(
              this.grouperProvisioningProcessingResult.getGcGrouperSyncLog());
        }
      } catch (RuntimeException re3) {
        debugMapOverall.put("exception3", GrouperClientUtils.getFullStackTrace(re3));
        debugMapToString = GrouperUtil.mapToString(debugMapOverall);
      }

      this.debugMapOverall.put("finalLog", true);

      if (LOG.isDebugEnabled() || hasError) {

        if (hasError) {
          LOG.error(debugMapToString);
        } else {
          LOG.debug(debugMapToString);
        }
      }

      if (hasError) {
        Long sequenceNumber = (Long)debugMapOverall.get("currentSequenceNumber");
        if (sequenceNumber!=null) {
          // find this sequence
          for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(allEsbEventContainers)) {
            if (GrouperUtil.equals(sequenceNumber, esbEventContainer.getSequenceNumber())) {
              esbEventContainer.getDebugMapForEvent().put("event", esbEventContainer.getEsbEvent().toString());
              LOG.error("debugMapForEventForError: " + GrouperUtil.mapToString(esbEventContainer.getDebugMapForEvent()));
            }
          }
        }
            
      }
      
      if (this.debugConsumer && LOG.isDebugEnabled()) {
        for (EsbEventContainer esbEventContainer : allEsbEventContainers) {
          Map<String, Object> eventDebugMap = esbEventContainer.getDebugMapForEvent();
          if (eventDebugMap != null && eventDebugMap.size() > 0) {
            if (esbEventContainer.getEsbEvent() != null) {
              eventDebugMap.put("esbEvent", esbEventContainer.getEsbEvent().toString());
            }
            LOG.debug(GrouperUtil.mapToString(eventDebugMap));
          }
        }
      }
      
      try {
        if (this.esbPublisherBase != null) {
          this.esbPublisherBase.disconnect();
        }

      } catch (RuntimeException re) {
        LOG.error("Error disconnecting", re);
      }
    }

    // not sure why this would happen
    if (currentId == -1) {
      throw new RuntimeException("Couldn't process any records: " + GrouperUtil.mapToString(debugMapOverall));
    }
    return currentId;
  }

  /**
   * might need this method later
   * @param esbEventContainersToProcess
   */
  private void fillInMoreData(List<EsbEventContainer> esbEventContainersToProcess) {
    
    if (esbEventContainersToProcess.size() == 0) {
      return;
    }

//    MemberFind
    
    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainersToProcess)) {

      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());
      
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      
      if (EsbEventType.MEMBERSHIP_UPDATE == esbEventContainer.getEsbEventType()) {
        
        
//        event.setMemberId(retrieveLabelValue(changeLogEntry,
//            ChangeLogLabels.MEMBERSHIP_UPDATE.memberId));
//        event.setFieldId(retrieveLabelValue(changeLogEntry,
//            ChangeLogLabels.MEMBERSHIP_UPDATE.fieldId));
//
        
        
      }
      
    }
    debugMapOverall.put("currentSequenceNumber", null);

    
  }

  /**
   * go through esb event containers and 
   * @param esbEventContainers
   * @return groupIds to investigate
   */
  private Set<String> groupIdsToQueryProvisioningAttributes(List<EsbEventContainer> esbEventContainers) {
    
    Set<String> groupIdsToInvestigate = new HashSet<String>();
    
    Set<String> attributeAssignIdsToInvestigate = new HashSet<String>();
    
    // target name
    String provisioningTargetAttributeDefNameId = GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getId();

    // do provision
    String provisioningDoProvisionAttributeDefNameId = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision().getId();

    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {

      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();

      Map<String, Object> debugMapForEvent = esbEventContainer.getDebugMapForEvent();
      
      switch (esbEventType) {
        
        
        case ATTRIBUTE_ASSIGN_ADD:
        case ATTRIBUTE_ASSIGN_DELETE:
          
          String attributeAssignType = esbEvent.getAttributeAssignType();
          if (!AttributeAssignType.group_asgn.name().equals(attributeAssignType)) {
            
            debugMapForEvent.put("ignoreProvisioningUpdatesDueToAssignType", attributeAssignType);
            
            continue;
          }

          // fall through
          
        case ATTRIBUTE_ASSIGN_VALUE_ADD:
        case ATTRIBUTE_ASSIGN_VALUE_DELETE:
          
          String esbEventAttributeDefNameId = esbEvent.getAttributeDefNameId();
          
          if (!StringUtils.equals(provisioningTargetAttributeDefNameId, esbEventAttributeDefNameId)
              && !StringUtils.equals(provisioningDoProvisionAttributeDefNameId, esbEventAttributeDefNameId)) {
            
            debugMapForEvent.put("ignoreProvisioningUpdatesDueToAttributeDefName", esbEvent.getAttributeDefNameName());
            
            continue;
            
          }

          debugMapForEvent.put("processProvisioningUpdatesForAssignId", esbEvent.getAttributeAssignId());

          //lets look at attributeAssignOnAssignIds
          attributeAssignIdsToInvestigate.add(esbEvent.getAttributeAssignId());
          
          break;
          
        case GROUP_DELETE:
        case GROUP_ADD:
          
          debugMapForEvent.put("processProvisioningUpdatesForGroupId", esbEvent.getGroupId());
          groupIdsToInvestigate.add(esbEvent.getGroupId());
          
          break;
          
      }
      
    }
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);
    logIntegerIfNotZero(debugMapOverall, "groupIdCountAddOrDelete", GrouperUtil.length(groupIdsToInvestigate));

    
    if (GrouperUtil.length(attributeAssignIdsToInvestigate) > 0) {
      logIntegerIfNotZero(debugMapOverall, "attributeAssignIdsToInvestigate", GrouperUtil.length(attributeAssignIdsToInvestigate));
      Set<String> groupIds = GrouperProvisioningService.findAllGroupIdsFromAttributeAssignIdsOnIds(attributeAssignIdsToInvestigate);
      logIntegerIfNotZero(debugMapOverall, "groupIdCountFromAttributeAssignIds", GrouperUtil.length(groupIds));
      groupIdsToInvestigate.addAll(GrouperUtil.nonNull(groupIds));
    }
    
    return groupIdsToInvestigate;
  }
  
  /**
   * add subject attributes to all events
   * @param esbEventContainersToProcess
   */
  private void addSubjectAttributes(List<EsbEventContainer> esbEventContainersToProcess) {
    
    String subjectAttributesToAdd = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "changeLog.consumer." + consumerName + ".publisher.addSubjectAttributes");

    int eventsWithAddedSubjectAttributes = 0;
    
    if (!StringUtils.isBlank(subjectAttributesToAdd)) {
      for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainersToProcess)) {
        
        // for logging
        debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

        // add subject attributes if configured
        boolean addedAttributes = this.addSubjectAttributes(esbEventContainer, subjectAttributesToAdd);
        
        if (addedAttributes) {
          eventsWithAddedSubjectAttributes++;
        }

      }
    }

    this.internal_esbConsumerTestingData.eventsWithAddedSubjectAttributes = eventsWithAddedSubjectAttributes;
    
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);

  }
  
  /**
   * Add subject attributes to event
   * @param esbEvent
   * @param attributes (comma delimited)
   * @return if attributes were added 
   */
  private boolean addSubjectAttributes(EsbEventContainer esbEventContainer, String attributes) {
    EsbEvent esbEvent = esbEventContainer.getEsbEvent();
    Map<String, Object> debugMapForEvent = esbEventContainer.getDebugMapForEvent();
    boolean addedAttributes = false;
    Subject subject = esbEvent.retrieveSubject();
    if (subject != null) {
      String[] attributesArray = GrouperUtil.splitTrim(attributes, ",");
      for (int i = 0; i < attributesArray.length; i++) {
        addedAttributes = true;
        String attributeName = attributesArray[i];
        String attributeValue = subject.getAttributeValueOrCommaSeparated(attributeName);
        if (GrouperUtil.isBlank(attributeValue)) {
          if (StringUtils.equals("name", attributeName)) {
            attributeValue = subject.getName();
          } else if (StringUtils.equals("description", attributeName)) {
            attributeValue = subject.getDescription();
          } 
        }
        if (!StringUtils.isBlank(attributeValue)) {
          debugMapForEvent.put("attr_" + attributeName + "_value", "'" + attributeValue + "'");
          esbEvent.addSubjectAttribute(attributeName, attributeValue);
        }
      }
    }
    return addedAttributes;
  }
  
  /**
   * filter events that happened after the last group sync
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  private void filterByProvisioningGroupSync(List<EsbEventContainer> esbEventContainersToProcess, GcGrouperSync gcGrouperSync) {

    if (GrouperUtil.length(esbEventContainersToProcess) == 0) {
      return;
    }
    int eventsFilteredBeforeGroupSync = 0;
    
    Iterator<EsbEventContainer> iterator = esbEventContainersToProcess.iterator();
    while (iterator.hasNext()) {
      
      EsbEventContainer esbEventContainer = iterator.next();

      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

      GcGrouperSyncGroup gcGrouperSyncGroup = esbEventContainer.getGcGrouperSyncGroup();
      
      String groupId = esbEventContainer.getEsbEvent().getGroupId();

      // if this is a group based event
      if (!StringUtils.isBlank(groupId)) {
        
        // we can ignore these...
        Timestamp lastGroupSync = gcGrouperSyncGroup.getLastGroupSync();
        if (lastGroupSync == null) {
          continue;
        }
        
        if (lastGroupSync.getTime() * 1000 > esbEventContainer.getEsbEvent().getCreatedOnMicros()) {
          
          iterator.remove();
          eventsFilteredBeforeGroupSync++;
          esbEventContainer.getDebugMapForEvent().put("eventFilteredBeforeGroupSync", true);
          continue;
        }
      }
      
    }

    // for logging
    debugMapOverall.put("currentSequenceNumber", null);

    logIntegerIfNotZero(debugMapOverall, "eventsFilteredBeforeGroupSync", eventsFilteredBeforeGroupSync);

  }

  /**
   * retrieve events that happened after the last group sync
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  private void retrieveAndCreateProvisioningGroupSyncObjects(List<EsbEventContainer> esbEventContainers, GcGrouperSync gcGrouperSync) {

    // we need all the groupIds
    Set<String> groupIdsToRetrieve = new HashSet<String>();

    if (this.grouperProvisioningProcessingResult.getGroupIdToGcGrouperSyncGroupMap() == null) {
      this.grouperProvisioningProcessingResult.setGroupIdToGcGrouperSyncGroupMap(new HashMap<String, GcGrouperSyncGroup>());
    }
    
    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {
      
      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

      String groupId = esbEventContainer.getEsbEvent().getGroupId();
      
      // dont worry if there is no group id
      if (StringUtils.isBlank(groupId)) {
        continue;
      }
      
      GcGrouperSyncGroup gcGrouperSyncGroup = this.grouperProvisioningProcessingResult.getGroupIdToGcGrouperSyncGroupMap().get(groupId);
      
      // if this is there, when we done
      if (gcGrouperSyncGroup != null) {
        esbEventContainer.setGcGrouperSyncGroup(gcGrouperSyncGroup);
      } else {
        groupIdsToRetrieve.add(groupId);
      }
      
    }
    this.internal_esbConsumerTestingData.gcGrouperSyncGroupGroupIdsToRetrieveCount = GrouperUtil.length(groupIdsToRetrieve);
    
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);

    // lets retrieve all those
    Map<String, GcGrouperSyncGroup> groupIdToSyncGroupMap = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIdsToRetrieve);
    logIntegerIfNotZero(debugMapOverall, "gcGrouperSyncGroupsRetrievedByEvents", GrouperUtil.length(groupIdToSyncGroupMap));

    this.internal_esbConsumerTestingData.gcGrouperSyncGroupsRetrievedByEventsSize = GrouperUtil.length(GrouperUtil.length(groupIdToSyncGroupMap));
        
    this.grouperProvisioningProcessingResult.getGroupIdToGcGrouperSyncGroupMap().putAll(groupIdToSyncGroupMap);

    //setup in the event objects
    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      
      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());
      
      String groupId = esbEventContainer.getEsbEvent().getGroupId();
      
      // dont worry if there is no group id or if we already have it
      if (esbEventContainer.getGcGrouperSyncGroup() != null || StringUtils.isBlank(groupId)) {
        continue;
      }
      
      GcGrouperSyncGroup gcGrouperSyncGroup = this.grouperProvisioningProcessingResult.getGroupIdToGcGrouperSyncGroupMap().get(groupId);
      
      // if this is there, when we done
      if (gcGrouperSyncGroup != null) {
        esbEventContainer.setGcGrouperSyncGroup(gcGrouperSyncGroup);
      }
      
    }
    
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);
    
    
  }

  /**
   * take out events that are not provisionable to this target
   * @param esbEventContainersToProcess
   * @param grouperProvisioningProcessingResult
   */
  private void filterByNotProvisionable(List<EsbEventContainer> esbEventContainersToProcess) {
    if (GrouperUtil.length(esbEventContainersToProcess) == 0) {
      return;
      
    }

    this.internal_esbConsumerTestingData.filterByNotProvisionablePreSize = GrouperUtil.length(esbEventContainersToProcess);

    int eventsFilteredNotTrackedAtAll = 0;
    int eventsFilteredByNotProvisionable = 0;
    
    Iterator<EsbEventContainer> iterator = esbEventContainersToProcess.iterator();
    while (iterator.hasNext()) {
      
      EsbEventContainer esbEventContainer = iterator.next();

      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

      GcGrouperSyncGroup gcGrouperSyncGroup = esbEventContainer.getGcGrouperSyncGroup();
      
      String groupId = esbEventContainer.getEsbEvent().getGroupId();

      // if this is a group based event
      if (!StringUtils.isBlank(groupId)) {
        
        // this is not even on our radar
        if (gcGrouperSyncGroup == null) {
          iterator.remove();
          eventsFilteredNotTrackedAtAll++;
          esbEventContainer.getDebugMapForEvent().put("eventFilteredNotTrackedAtAll", true);
          continue;
        }
        
        // we can ignore these...
        if (!gcGrouperSyncGroup.isInTarget() && !gcGrouperSyncGroup.isProvisionable()) {
          iterator.remove();
          eventsFilteredByNotProvisionable++;
          esbEventContainer.getDebugMapForEvent().put("eventFilteredByNotProvisionable", true);
          continue;
        }
      }
      
    }

    // for logging
    debugMapOverall.put("currentSequenceNumber", null);

    logIntegerIfNotZero(debugMapOverall, "eventsFilteredByNotProvisionable", eventsFilteredByNotProvisionable);
    logIntegerIfNotZero(debugMapOverall, "eventsFilteredNotTrackedAtAll", eventsFilteredNotTrackedAtAll);

    this.internal_esbConsumerTestingData.eventsFilteredByNotProvisionable = eventsFilteredByNotProvisionable;
    this.internal_esbConsumerTestingData.eventsFilteredNotTrackedAtAll = eventsFilteredNotTrackedAtAll;
    this.internal_esbConsumerTestingData.eventsFilteredNotTrackedOrProvisionable = eventsFilteredNotTrackedAtAll + eventsFilteredByNotProvisionable;
    this.internal_esbConsumerTestingData.filterByNotProvisionablePostSize = GrouperUtil.length(esbEventContainersToProcess);
  }

  /**
   * get the member objects currently in the db
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  private void retrieveProvisioningMemberSyncObjects(List<EsbEventContainer> esbEventContainers, GcGrouperSync gcGrouperSync) {
  
    // we need all the memberIds
    Set<String> memberIdsToRetrieve = new HashSet<String>();

    if (this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap() == null) {
      this.grouperProvisioningProcessingResult.setMemberIdToGcGrouperSyncMemberMap(new HashMap<String, GcGrouperSyncMember>());
    }

    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      
      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());
  
      String memberId = esbEventContainer.getEsbEvent().getMemberId();
      
      // dont worry if there is no group id
      if (StringUtils.isBlank(memberId)) {
        continue;
      }
      
      GcGrouperSyncMember gcGrouperSyncMember = this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap().get(memberId);
      
      // if this is there, when we done
      if (gcGrouperSyncMember != null) {
        esbEventContainer.setGcGrouperSyncMember(gcGrouperSyncMember);
      } else {
        memberIdsToRetrieve.add(memberId);
      }
      
    }
    
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);
  
    // lets retrieve all those
    Map<String, GcGrouperSyncMember> memberIdToSyncMemberMap = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(memberIdsToRetrieve);

    logIntegerIfNotZero(debugMapOverall, "gcGrouperSyncMembersRetrievedByEvents", GrouperUtil.length(memberIdToSyncMemberMap));

    if (this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap() == null) {
      this.grouperProvisioningProcessingResult.setMemberIdToGcGrouperSyncMemberMap(new HashMap<String, GcGrouperSyncMember>());
    }

    this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap().putAll(memberIdToSyncMemberMap);
  
    //setup in the event objects
    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      
      // for logging
      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());
      
      String memberId = esbEventContainer.getEsbEvent().getMemberId();
      
      // dont worry if there is no group id or if we already have it
      if (esbEventContainer.getGcGrouperSyncMember() != null || StringUtils.isBlank(memberId)) {
        continue;
      }
      
      GcGrouperSyncMember gcGrouperSyncMember = this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap().get(memberId);
      
      // if this is there, when we done
      if (gcGrouperSyncMember != null) {
        esbEventContainer.setGcGrouperSyncMember(gcGrouperSyncMember);
      }
      
    }
    
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);
    
    
  }

  /**
   * see if the esb event matches an EL filter.  Note the available objects are
   * event for the EsbEvent, and grouperUtil for the GrouperUtil class which has
   * a lot of utility methods
   * @param filterString
   * @param esbEvent
   * @return true if matches, false if doesnt
   */
  public static boolean matchesFilter(EsbEvent esbEvent, String filterString) {

    Map<String, Object> elVariables = new HashMap<String, Object>();
    elVariables.put("event", esbEvent);
    elVariables.put("grouperUtilElSafe", new GrouperUtil());

    String resultString = GrouperUtil.substituteExpressionLanguage("${" + filterString + "}", elVariables, true, true, true);

    boolean result = GrouperUtil.booleanValue(resultString, false);

    return result;
  }
}
