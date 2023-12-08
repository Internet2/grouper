/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author vsachdeva
 */
// https://spaces.at.internet2.edu/display/Grouper/Grouper+messaging+to+web+service+API
@DisallowConcurrentExecution
public class MessageConsumerDaemon implements Job {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(MessageConsumerDaemon.class);

  /**
   * @see Job#execute(JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    
    Pattern pattern = Pattern.compile("^grouper\\.messaging\\.([^.]+)\\.messagingSystemName$");
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    
    String configName = null;
    
    String actAsSubjectId = null;
    String actAsSubjectSourceId = null;
    String messagingSystemName = null;
    String queueOrTopicName = null;
    String routingKey = null;
    String exchangeType = null;
    String messageQueueType = null;
    Integer longPollingSeconds = null;
    Map<String, Object> queueArguments = null;
    
    for (String propertyName : grouperLoaderConfig.propertyNames()) {
      Matcher matcher = pattern.matcher(propertyName);
      if (matcher.matches()) {

        configName = matcher.group(1);
        
        messagingSystemName = grouperLoaderConfig.propertyValueString(propertyName);
        if (StringUtils.isBlank(messagingSystemName)) {
          LOG.info("No messaging system name found so not going to connect to any queue or topic.");
          return;
        }
        
        queueOrTopicName = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".queueOrTopicName");
        routingKey = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".routingKey");
        exchangeType = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".exchangeType");
        messageQueueType = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".messageQueueType");
        actAsSubjectSourceId = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".actAsSubjectSourceId");
        actAsSubjectId = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".actAsSubjectId");
        longPollingSeconds = grouperLoaderConfig.propertyValueInt("grouper.messaging."+configName+".longPollingSeconds", 1);

        for (int i=0;i<100;i++) {
          String key = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".queueArgs." + i + ".key");
          if (key == null || "".equals(key)) {
            break;
          }

          String value = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".queueArgs." + i + ".value");
          if (queueArguments == null) {
            queueArguments = new HashMap<>();
          }
          queueArguments.put(key, value);
        }
      }
    }
    
    if (StringUtils.isBlank(configName)) {
      return;
    }
    
    try {
      GrouperSession.startBySubjectIdAndSource(actAsSubjectId, actAsSubjectSourceId);
    } catch (Exception e) {
      LOG.error("Error occurred while starting grouper session for subjectId" + actAsSubjectId +" and source id "+actAsSubjectSourceId, e);
      return;
    }
    
    GrouperMessagingSystem grouperMessageSystem = null;
    
    try {
      grouperMessageSystem = GrouperMessagingEngine.retrieveGrouperMessageSystem(messagingSystemName);
    } catch(Exception e) {
      LOG.error("Error occurred while retrieving grouper message system for "+messagingSystemName, e);
      return;
    }
    
    Collection<GrouperMessage> grouperMessages = null;
    try {
      grouperMessages = receiveMessages(messagingSystemName, queueOrTopicName, routingKey, exchangeType, messageQueueType, longPollingSeconds, queueArguments, grouperMessageSystem);
      LOG.info("Received "+grouperMessages.size() +" message(s) from "+queueOrTopicName +" for message system: "+messagingSystemName);
    } catch (Exception e) {
      LOG.error("Error occurred while receiving messages from "+queueOrTopicName, e);
      return;
    }
    
    processMessages(messagingSystemName, grouperMessageSystem, messageQueueType, queueOrTopicName, grouperMessages, configName);

  }


  /**
   * @param messagingSystemName
   * @param grouperMessageSystem
   * @param messageQueueType
   * @param queueTopicName
   * @param grouperMessages
   * @param configName
   */
  protected void processMessages(String messagingSystemName, GrouperMessagingSystem grouperMessageSystem,
      String messageQueueType, String queueTopicName,
      Collection<GrouperMessage> grouperMessages, String configName) {
    
    List<GrouperMessage> messagesToBeAcknowledged = new ArrayList<GrouperMessage>();
    
    for (GrouperMessage inputMessage: grouperMessages) {
      
      String messageBody = inputMessage.getMessageBody();
      InputMessageGrouperHeader grouperHeader = null;
      
      JsonNode jsonObject = null;
      
      
      try { 
        jsonObject = GrouperUtil.jsonJacksonNode(messageBody);
        
      } catch(Exception e) {
        LOG.error("Error occurred while building json object for "+messageBody);
        continue;
      }
      
      JsonNode grouperHeaderJson = null;
      try {
        grouperHeaderJson = jsonObject.get("grouperHeader");
      } catch (Exception e) {
        LOG.error("Error occurred while retrieving key grouperHeader out of message body: "+messageBody);
        continue;
      }
      
      try {
        grouperHeader = GrouperUtil.jsonConvertFrom(grouperHeaderJson, InputMessageGrouperHeader.class);
      } catch (Exception e) {
        LOG.error("Error occurred while building Json object for "+grouperHeaderJson);
        continue;
      }
      
      Collection<String> errors = validate(grouperHeader);
      
      String replyToQueueOrTopic = grouperHeader.getReplyToQueueOrTopic();
      String replyToQueueOrTopicName = grouperHeader.getReplyToQueueOrTopicName();
      String routingKey = grouperHeader.getReplyToRoutingKey();
      if (errors.size() > 0) {
        if (canReplyToErrorMessages(replyToQueueOrTopicName, replyToQueueOrTopic)) {
          String errorJson = buildErrorResponse(errors, grouperHeader);
          sendReplyMessage(grouperMessageSystem, grouperHeader, messagingSystemName, errorJson, routingKey);
        } else {
          LOG.error("Invalid message received from the queue. Errors: "+GrouperUtil.collectionToString(errors));
        }
        continue;
      }
      
      String endpoint = grouperHeader.getEndpoint();
      
      String wsRequestBody = GrouperUtil.jsonJacksonGetString(jsonObject, endpoint);
      
      String newJson = "{ \"" + endpoint + "\" :" + wsRequestBody + "}";
      
      String wsBaseUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.messaging."+configName+".ws.url");
      
      WsResponse wsReponse = null;
      try {        
        wsReponse = callWebService(newJson, wsBaseUrl + grouperHeader.getHttpPath(), configName);
        messagesToBeAcknowledged.add(inputMessage);
      } catch (Exception e) {
        wsReponse = new WsResponse();
        wsReponse.setHttpStatusCode(400);
        wsReponse.setSuccess("F");
        wsReponse.setResultCode("EXCEPTION");
        wsReponse.setBody("\"errorMessage\": \"" + e.getMessage() + "\"");
        LOG.error("Error occurred while calling web service: " + e.getMessage(), e);
      }
      
      // now send the response to queue/topic if specified in the message
      if (StringUtils.isNotBlank(replyToQueueOrTopic) && StringUtils.isNotBlank(replyToQueueOrTopicName)) {
        String messageToBeSent = buildWsReplyToMessage(wsReponse, grouperHeader);
        sendReplyMessage(grouperMessageSystem, grouperHeader, messagingSystemName, messageToBeSent, routingKey);
      }
            
    }
    
    acknowledge(grouperMessageSystem, messagingSystemName, messageQueueType, queueTopicName, messagesToBeAcknowledged);
    
  }
  
  /**
   * @param grouperMessageSystem
   * @param messagingSystemName
   * @param messageQueueType
   * @param queueTopicName
   * @param messagesToBeAcknowledged
   */
  private void acknowledge(GrouperMessagingSystem grouperMessageSystem,
      String messagingSystemName, String messageQueueType, String queueTopicName, Collection<GrouperMessage> messagesToBeAcknowledged) {
    
    GrouperMessageAcknowledgeParam acknowledgeParam = new GrouperMessageAcknowledgeParam();
    acknowledgeParam.assignQueueName(queueTopicName);
    acknowledgeParam.assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed);
    acknowledgeParam.assignGrouperMessages(messagesToBeAcknowledged);
    acknowledgeParam.assignGrouperMessageSystemName(messagingSystemName);
    
    GrouperMessageSystemParam systemParam = new GrouperMessageSystemParam();
    systemParam.assignMesssageSystemName(messagingSystemName);
    acknowledgeParam.assignGrouperMessageSystemParam(systemParam);
    
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueOrTopicName(queueTopicName);
    queueParam.assignQueueType(GrouperMessageQueueType.valueOfIgnoreCase(messageQueueType, true));
    acknowledgeParam.assignGrouperMessageQueueParam(queueParam);
    
    grouperMessageSystem.acknowledge(acknowledgeParam);
    
  }
  
  /**
   * @param queueOrTopicName
   * @param queueType
   * @return
   */
  private boolean canReplyToErrorMessages(String queueOrTopicName, String queueType) {
    return StringUtils.isNotBlank(queueType) && StringUtils.isNotBlank(queueOrTopicName) && GrouperMessageQueueType.valueOfIgnoreCase(queueType, false) != null;
  }

  /**
   * @param grouperHeader
   * @return
   */
  private List<String> validate(InputMessageGrouperHeader grouperHeader) {
    
    List<String> errors = new ArrayList<String>();
    
    if (StringUtils.isBlank(grouperHeader.getMessageVersion())) {
      errors.add("grouperHeader.messageVersion is required.");
    }
    
    if (StringUtils.isBlank(grouperHeader.getTimestampInput())) {
      errors.add("grouperHeader.timestampInput is required.");
    }
    
    try {
      DateTime.parse(grouperHeader.getTimestampInput(), ISODateTimeFormat.dateTime());
    } catch (Exception e) {
      errors.add("Error converting "+grouperHeader.getTimestampInput() +" to datetime using "+ISODateTimeFormat.dateTime());
    }
    
    if (StringUtils.isBlank(grouperHeader.getType())) {
      errors.add("grouperHeader.type is required.");
    }
    
    if (StringUtils.isBlank(grouperHeader.getEndpoint())) {
      errors.add("grouperHeader.endpoint is required.");
    }
    
    if (StringUtils.isBlank(grouperHeader.getMessageInputUuid())) {
      errors.add("grouperHeader.messageInputUuid is required.");
    }
    
    if (StringUtils.isBlank(grouperHeader.getHttpMethod())) {
      errors.add("grouperHeader.httpMethod is required.");
    }
    
    if (StringUtils.isBlank(grouperHeader.getHttpPath())) {
      errors.add("grouperHeader.httpPath is required.");
    }
    
    if (StringUtils.isNotBlank(grouperHeader.getReplyToQueueOrTopic()) &&
        GrouperMessageQueueType.valueOfIgnoreCase(grouperHeader.getReplyToQueueOrTopic(), false) == null) {
      errors.add("grouperHeader.replyToQueueOrTopic can only be queue or topic.");
    }
  
    return errors;
  }


  private Collection<GrouperMessage> receiveMessages(String messagingSystemName,
      String queueOrTopicName, String routingKey, String exchangeType, String messageQueueType, Integer longPollingSeconds,
      Map<String, Object> queueArguments, GrouperMessagingSystem grouperMessageSystem) {
    
    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messagingSystemName);
    
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueOrTopicName(queueOrTopicName);
    queueParam.assignQueueType(GrouperMessageQueueType.valueOfIgnoreCase(messageQueueType, true));
    receiveParam.assignGrouperMessageQueueParam(queueParam);
    
    GrouperMessageSystemParam systemParam = new GrouperMessageSystemParam();
    systemParam.assignMesssageSystemName(messagingSystemName);
    
    receiveParam.assignQueueName(queueOrTopicName);
    
    receiveParam.assignGrouperMessageSystemParam(systemParam);
    receiveParam.assignLongPollMillis(longPollingSeconds*1000);
    receiveParam.assignAutocreateObjects(true);
    receiveParam.assignRoutingKey(routingKey);
    receiveParam.assignExchangeType(exchangeType);
    receiveParam.assignQueueArguments(queueArguments);
    
    GrouperMessageReceiveResult grouperMessageReceiveResult = grouperMessageSystem.receive(receiveParam);
    return grouperMessageReceiveResult.getGrouperMessages();
  } 
  
  /**
   * @param jsonInput
   * @param url
   * @param configName
   * @return
   * @throws Exception
   */
  private WsResponse callWebService(String jsonInput, String url, String configName) throws Exception {
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);

      GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
      
      String username = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".ws.username");
      String password = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".ws.password");
      if (StringUtils.isNotBlank(password)) {        
        password = GrouperClientUtils.decryptFromFileIfFileExists(password, null);
        grouperHttpClient.assignUser(username);
        grouperHttpClient.assignPassword(password);
      }
      
      String proxyUrl = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".ws.proxyUrl");
      String proxyType = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".ws.proxyType");
      
      grouperHttpClient.assignProxyUrl(proxyUrl);
      grouperHttpClient.assignProxyType(proxyType);

      String actAsSubjectSourceId = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".actAsSubjectSourceId");
      String actAsSubjectId = grouperLoaderConfig.propertyValueString("grouper.messaging."+configName+".actAsSubjectId");
      
      grouperHttpClient.addHeader("X-Grouper-actAsSourceId", actAsSubjectSourceId);
      grouperHttpClient.addHeader("X-Grouper-actAsSubjectId", actAsSubjectId);
      
      grouperHttpClient.addHeader("Connection", "close");
      
      grouperHttpClient.assignBody(jsonInput);

      grouperHttpClient.addHeader("Content-type", "text/x-json; charset=UTF-8");

      grouperHttpClient.executeRequest();
      
      String response = grouperHttpClient.getResponseBody();
      
      WsResponse wsResponse = new WsResponse();
      wsResponse.setBody(response);
      wsResponse.setHttpStatusCode(grouperHttpClient.getResponseCode());
      //make sure a request came back
      String successString = grouperHttpClient.getResponseHeaders().get("X-Grouper-success");
      wsResponse.setSuccess(successString);
      String resultCode = grouperHttpClient.getResponseHeaders().get("X-Grouper-resultCode");
      wsResponse.setResultCode(resultCode);
      
      String resultCode2 = grouperHttpClient.getResponseHeaders().get("X-Grouper-resultCode2");
      wsResponse.setResultCode2(resultCode2);
      
      return wsResponse;
  }
  
  /**
   * @param errors
   * @param inputGrouperHeader
   * @return
   */
  private String buildErrorResponse(Collection<String> errors, InputMessageGrouperHeader inputGrouperHeader) {
    
    String timestampOutput = new DateTime().toString(ISODateTimeFormat.dateTime());
    
    OutputMessageGrouperHeader outputHeader = new OutputMessageGrouperHeader();
    outputHeader.setMessageVersion(inputGrouperHeader.getMessageVersion());
    outputHeader.setTimestampInput(inputGrouperHeader.getTimestampInput());
    outputHeader.setTimestampOutput(timestampOutput);
    outputHeader.setType("grouperMessagingFromWebService");
    outputHeader.setEndpoint(inputGrouperHeader.getEndpoint());
    outputHeader.setMessageInputUuid(inputGrouperHeader.getMessageInputUuid());
    
    outputHeader.setHttpHeaderXGrouperSuccess("F");
    outputHeader.setHttpResponseCode(400);
    outputHeader.setHttpHeaderXGrouperResultCode2("NONE");
    outputHeader.setHttpHeaderXGrouperResultCode("ERROR");

    ObjectNode objectNode = GrouperUtil.jsonConvertFromObjectToObjectNode(outputHeader);
    
    renameKeys(objectNode);

    String header = GrouperUtil.jsonConvertTo(objectNode, false);
    
    String errorMessages = GrouperUtil.jsonConvertTo(errors, false);
    
    String finalOuput = " { \"grouperHeader\":  "+header+", \"errors\": " + errorMessages + " }" ;
        
    return finalOuput;
    
  }
  
  private void renameKeys(ObjectNode jsonObject) {
    
    jsonObject.put("httpHeader_X-Grouper-resultCode", jsonObject.get("httpHeaderXGrouperResultCode"));
    jsonObject.put("httpHeader_X-Grouper-success", jsonObject.get("httpHeaderXGrouperSuccess"));
    jsonObject.put("httpHeader_X-Grouper-resultCode2", jsonObject.get("httpHeaderXGrouperResultCode2"));
    
    jsonObject.remove("httpHeaderXGrouperResultCode");
    jsonObject.remove("httpHeaderXGrouperSuccess");
    jsonObject.remove("httpHeaderXGrouperResultCode2");
    
    
  }
  
  /**
   * @param wsResponse
   * @param inputGrouperHeader
   * @return
   */
  private String buildWsReplyToMessage(WsResponse wsResponse, InputMessageGrouperHeader inputGrouperHeader) {
    
    String timestampOutput = new DateTime().toString(ISODateTimeFormat.dateTime());
    
    OutputMessageGrouperHeader outputHeader = new OutputMessageGrouperHeader();
    outputHeader.setMessageVersion(inputGrouperHeader.getMessageVersion());
    outputHeader.setTimestampInput(inputGrouperHeader.getTimestampInput());
    outputHeader.setTimestampOutput(timestampOutput);
    outputHeader.setType("grouperMessagingFromWebService");
    outputHeader.setEndpoint(inputGrouperHeader.getEndpoint());
    outputHeader.setMessageInputUuid(inputGrouperHeader.getMessageInputUuid());
    outputHeader.setHttpResponseCode(wsResponse.getHttpStatusCode());
    outputHeader.setHttpHeaderXGrouperResultCode(wsResponse.getResultCode());
    outputHeader.setHttpHeaderXGrouperSuccess(wsResponse.getSuccess());
    outputHeader.setHttpHeaderXGrouperResultCode2(wsResponse.getResultCode2());

    ObjectNode objectNode = GrouperUtil.jsonConvertFromObjectToObjectNode(outputHeader);
    renameKeys(objectNode);
    
    String header = GrouperUtil.jsonConvertTo(objectNode, false);
    
    String finalOuput = " { \"grouperHeader\":  "+header+", " + wsResponse.getBody() + " }" ;
        
    return finalOuput;
    
  }

  /**
   * @param grouperMessagingSystem
   * @param inputGrouperHeader
   * @param messagingSystemName
   * @param finalOuput
   */
  private void sendReplyMessage(GrouperMessagingSystem grouperMessagingSystem,
      InputMessageGrouperHeader inputGrouperHeader, String messagingSystemName,
      String finalOuput, String routingKey) {
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    sendParam.assignAutocreateObjects(true);
    
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueOrTopicName(inputGrouperHeader.getReplyToQueueOrTopicName());
    queueParam.assignQueueType(GrouperMessageQueueType.valueOfIgnoreCase(inputGrouperHeader.getReplyToQueueOrTopic(), true));
    sendParam.assignGrouperMessageQueueParam(queueParam);
    
    sendParam.assignGrouperMessageSystemName(messagingSystemName);
    sendParam.assignQueueOrTopicName(inputGrouperHeader.getReplyToQueueOrTopicName());
    sendParam.assignQueueType(GrouperMessageQueueType.valueOfIgnoreCase(inputGrouperHeader.getReplyToQueueOrTopic(), true));
    
    GrouperMessageSystemParam systemParam = new GrouperMessageSystemParam();
    systemParam.assignAutocreateObjects(true);
    systemParam.assignMesssageSystemName(messagingSystemName);
    sendParam.assignGrouperMessageSystemParam(systemParam);
    
    sendParam.assignMessageBodies(Collections.singleton(finalOuput));
    sendParam.assignRoutingKey(routingKey);
    sendParam.assignExchangeType(inputGrouperHeader.getReplyToExchangeType());
    sendParam.assignQueueArguments(inputGrouperHeader.getReplyToQueueArguments());
    try {
      grouperMessagingSystem.send(sendParam);
    } catch (Exception e) {
      LOG.error("Error occurred while sending reply message "+ inputGrouperHeader.getMessageInputUuid()+" to "+inputGrouperHeader.getReplyToQueueOrTopicName(), e);
    }
  }

}
