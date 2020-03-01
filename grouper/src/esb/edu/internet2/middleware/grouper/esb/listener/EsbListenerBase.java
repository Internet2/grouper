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

package edu.internet2.middleware.grouper.esb.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningProcessingResult;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvents;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.encryption.GcEncryptionInterface;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * extend this to make an ESB processor
 */
public abstract class EsbListenerBase {

  /**
   * ref to consumer
   */
  private EsbConsumer esbConsumer;
  
  /**
   * ref to consumer
   * @return consumer
   */
  public EsbConsumer getEsbConsumer() {
    return esbConsumer;
  }

  /**
   * ref to consumer
   * @param esbConsumer1
   */
  public void setEsbConsumer(EsbConsumer esbConsumer1) {
    this.esbConsumer = esbConsumer1;
  }


  /** 
   * get stuff like hib3 loader log 
   */
  private ChangeLogProcessorMetadata changeLogProcessorMetadata;
  
  
  /**
   * get stuff like hib3 loader log 
   * @return metadata
   */
  public ChangeLogProcessorMetadata getChangeLogProcessorMetadata() {
    return this.changeLogProcessorMetadata;
  }

  /**
   * get stuff like hib3 loader log 
   * @param changeLogProcessorMetadata1
   */
  public void setChangeLogProcessorMetadata(
      ChangeLogProcessorMetadata changeLogProcessorMetadata1) {
    this.changeLogProcessorMetadata = changeLogProcessorMetadata1;
  }

  /**
   * 
   * @param eventJsonString
   * @param consumerName
   * @return true if ok, false if not
   */
  public abstract boolean dispatchEvent(String eventJsonString, String consumerName);

  /**
   * implement this instead of dispatchEvent if you want objects instead of json string
   * @param esbEventContainers
   * @param grouperProvisioningProcessingResult
   * @param consumerName
   * @return true if ok, false if not
   */
  public ProvisioningSyncConsumerResult dispatchEventList(List<EsbEventContainer> esbEventContainers, GrouperProvisioningProcessingResult grouperProvisioningProcessingResult) {

    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(-1L);
    
    long currentId = -1;
    
    String consumerName = this.getChangeLogProcessorMetadata().getConsumerName();
    
    boolean noSensitiveData = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.consumer." + consumerName
        + ".noSensitiveData", false);

    // for logging
    if (noSensitiveData) {
      this.getEsbConsumer().getDebugMapOverall().put("noSensitiveData", noSensitiveData);
    }
    
    String encryptionImplName = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." + consumerName
        + ".encryptionImplementation");

    boolean dontSendFirst4 = GrouperLoaderConfig.retrieveConfig().assertPropertyValueBoolean("changeLog.consumer." + consumerName
        + ".dontSendShaBase64secretFirst4", false);
    
    EsbConsumer.logObjectIfNotNull(this.getEsbConsumer().getDebugMapOverall(), "encryptionImplName", encryptionImplName);

    Class<GcEncryptionInterface> encryptionImplClass = StringUtils.isBlank(encryptionImplName) ? null : GrouperUtil.forName(encryptionImplName);
    
    String encryptionKey = null;
    
    if (!StringUtils.isBlank(encryptionImplName)) {
      encryptionKey = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("changeLog.consumer." + consumerName + ".encryptionKey");
    }

    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {

      // for logging
      this.getEsbConsumer().getDebugMapOverall().put("currentSequenceNumber", esbEventContainer.getSequenceNumber());

      EsbEvent event = esbEventContainer.getEsbEvent();
      
      // by default we can process these by strings and send to legacy listeners
      // add event to array, only one event supported for now
      EsbEvents events = new EsbEvents();
      events.addEsbEvent(event);

      events = convertEventsToNoSensitiveData(events, noSensitiveData);
      
      String eventJsonString = this.convertEventsToString(events);

      eventJsonString = this.encryptMessage(eventJsonString, encryptionImplClass, encryptionKey, dontSendFirst4);
      
      boolean dispatched = false;
     
      if (this instanceof EsbMessagingPublisher) {
        EsbMessagingPublisher esbMessagingPublisher = (EsbMessagingPublisher) this;
        dispatched = esbMessagingPublisher.dispatchEvent(eventJsonString, consumerName, esbEventContainer.getRoutingKey());
      } else {
        dispatched = this.dispatchEvent(eventJsonString, consumerName);
      }
      
      esbEventContainer.getDebugMapForEvent().put("processed", dispatched);

      if (!dispatched) {
        // error, need to retry
        changeLogProcessorMetadata.registerProblem(null,
            "Error processing record " + event.getSequenceNumber(), currentId);
        
        //we made it to this -1
        provisioningSyncConsumerResult.setLastProcessedSequenceNumber(currentId-1L);
        return provisioningSyncConsumerResult;
      }
    }

    // for logging
    this.getEsbConsumer().getDebugMapOverall().put("currentSequenceNumber", null);

    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(currentId-1L);
    return provisioningSyncConsumerResult;
  }

  public String encryptMessage(String message, Class<GcEncryptionInterface> encryptionImplClass, String encryptionKey, boolean dontSendFirst4) {
    //lets see if we are encrypting
    //    # if you want to encrypt messages, set this to an implementation of edu.internet2.middleware.grouperClient.encryption.GcEncryptionInterface
    //changeLog.consumer.awsJira.encryptionImplementation = edu.internet2.middleware.grouperClient.encryption.GcSymmetricEncryptAesCbcPkcs5Padding
    //    # this is a key or could be encrypted in a file as well like other passwords
    //    changeLog.consumer.awsJira.encryptionKey = Mdxabc123zouRykg==
    
    if (encryptionImplClass != null) {
      
      encryptionKey = GrouperClientUtils.decryptFromFileIfFileExists(encryptionKey, null);
      
      
      GcEncryptionInterface gcEncryptionInterface = GrouperUtil.newInstance(encryptionImplClass);
      String encryptedPayload = gcEncryptionInterface.encrypt(encryptionKey, message);
      
      EsbEvents events = new EsbEvents();
      events.setEncrypted(true);
      events.setEncryptedPayload(encryptedPayload);
      
      if (!dontSendFirst4) {
        String secretFirst4 = GrouperClientUtils.encryptSha(encryptionKey).substring(0,4);
        events.setEncryptionKeySha1First4(secretFirst4);
      }

      message = convertEventsToString(events);

    }

    return message;
  }
  
  /**
   * convert events to events with no sensitive data
   * @param esbEvents
   * @param noSensitiveData
   * @return
   */
  public EsbEvents convertEventsToNoSensitiveData(EsbEvents events, boolean noSensitiveData) {
    //if no sensitive data, then just send over that a change occurred and the event type and the id
    if (noSensitiveData) {
      EsbEvents tempEsbEvents = new EsbEvents();
      List<EsbEvent> tempEsbEventList = new ArrayList<EsbEvent>();
      for (EsbEvent esbEvent : GrouperUtil.nonNull(events.getEsbEvent(), EsbEvent.class)) {
        EsbEvent tempEvent = new EsbEvent();
        //copy over non sensitive data
        tempEvent.setSequenceNumber(esbEvent.getSequenceNumber());
        tempEvent.setEventType(esbEvent.getEventType());
        tempEvent.setChangeOccurred(true);
        tempEsbEventList.add(tempEvent);
      }
      tempEsbEvents.setEsbEvent(GrouperUtil.toArray(tempEsbEventList, EsbEvent.class));
      events = tempEsbEvents;
    }
    return events;
  }

  /**
   * convert events to json.  optionally indent
   * @param events
   * @return the json
   */
  public String convertEventsToString(EsbEvents events) {
    String eventJsonString = GrouperUtil.jsonConvertToNoWrap(events);
    //String eventJsonString = gson.toJson(event);
    // add indenting for debugging
    // add subject attributes if configured

    if (this.getEsbConsumer().isDebugConsumer()) {
      eventJsonString = GrouperUtil.indent(eventJsonString, false);
    }
    return eventJsonString;
  }
  
  /**
   * disconnect if needed
   */
  public abstract void disconnect();
  
  /**
   * return true if you want objects instead of json
   * @return
   */
  protected boolean isProcessObjectsInsteadOfJson() {
    return false;
  }
  
}
