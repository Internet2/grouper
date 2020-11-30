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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
//    int i=14;
//    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
//    RegistryReset._addSubjects(i, i+1);
//    Subject subject = SubjectFinder.findById("test.subject." + i, true);
//    group.addMember(subject);
    
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
      EsbEventType esbEventType = null;
      
      try {
        esbEventType = EsbEventType.valueOfIgnoreCase(changeLogTypeBuiltin.name(), false);
      } catch (Exception e) {
        // ignore
      }
      
      if (esbEventType != null) {
        event.setEventType(esbEventType.name());
        esbEventContainer.setEsbEventType(esbEventType);
        esbEventType.processChangeLogEntry(esbEventContainer, changeLogEntry);
      } else {
        debugMapForEvent.put("unsupportEventType", event.getType());
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
        
        iterator.remove();
      }

    }
    // for logging
    debugMapOverall.put("currentSequenceNumber", null);
    logIntegerIfNotZero(debugMapOverall, "filterInvalidEventTypesSize", filterInvalidEventTypesSize);
    this.internal_esbConsumerTestingData.filterInvalidEventTypesSize = filterInvalidEventTypesSize;

    
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

  public Runnable provisioningHeartbeatLogic() {
    return new Runnable() {

      @Override
      public void run() {
        EsbConsumer.this.changeLogProcessorMetadata.getHib3GrouperLoaderLog().store();
        
        // periodically log
        if (LOG.isDebugEnabled()) {
          String debugMapToString = GrouperUtil.mapToString(debugMapOverall);
          LOG.debug(debugMapToString);
        }
      }
      
    };
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
    public int skippedEventsDueToExpressionLanguageCount;
    public List<EsbEventContainer> skippedEventsDueToExpressionLanguage;
    public int eventsWithAddedSubjectAttributes;


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

        
      // ######### STEP 8: add in subject attributes
      this.debugMapOverall.put("state", "addSubjectAttributes");
      this.addSubjectAttributes(esbEventContainersToProcess);
      
      // ######### STEP 9: filter by EL
      this.debugMapOverall.put("state", "filterByExpressionLanguage");
      this.filterByExpressionLanguage(esbEventContainersToProcess);

      // ######### STEP 15: setup routing key
      this.debugMapOverall.put("state", "setupRoutingKeys");
      this.setupRoutingKeys(esbEventContainersToProcess);
      
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
        ProvisioningSyncConsumerResult provisioningSyncConsumerResult = this.esbPublisherBase.dispatchEventList(esbEventContainersToProcess);
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
      

    } catch (RuntimeException re) {
      hasError = true;
      debugMapOverall.put("exception", GrouperUtil.getFullStackTrace(re));
      
      changeLogProcessorMetadata.registerProblem(re, "Error processing record " + currentId, currentId);
      if (currentId != -1) {
        currentId--;
      }

      
    } finally {

      long tookMillis = (System.nanoTime() - startNanos) / 1000000;
      debugMapOverall.put("tookMillis", tookMillis);
      String debugMapToString = GrouperUtil.mapToString(debugMapOverall);

      if (this.changeLogProcessorMetadata != null && this.changeLogProcessorMetadata.getHib3GrouperLoaderLog() != null) {
        this.changeLogProcessorMetadata.getHib3GrouperLoaderLog().setJobMessage(debugMapToString);
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
