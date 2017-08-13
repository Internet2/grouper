package edu.internet2.middleware.grouper.app.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * @author vsachdeva
 */

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
    
    String messagingSystemName = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.messaging.wsMessagingBridge.messagingSystemName");
    if (StringUtils.isBlank(messagingSystemName)) {
      LOG.info("No messaging system name found so not going to connect to any queue or topic.");
      return;
    }
    
    String queueOrTopicName = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.messaging.wsMessagingBridge.queueOrTopicName");
    String messageQueueType = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.messaging.wsMessagingBridge.messageQueueType");
    String actAsSubjectSourceId = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.messaging.wsMessagingBridge.actAsSubjectSourceId");
    String actAsSubjectId = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.messaging.wsMessagingBridge.actAsSubjectId");
    //Integer secondsBetweenChecks = GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.messaging.wsMessagingBridge.secondsBetweenChecks");
    Integer longPollingSeconds = GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.messaging.wsMessagingBridge.longPollingSeconds");
    
    try {
      GrouperSession.startBySubjectIdAndSource(actAsSubjectId, actAsSubjectSourceId);
    } catch (Exception e) {
      LOG.error("Error occurred while starting grouper session for subjectId" + actAsSubjectId +" and source id "+actAsSubjectSourceId, e);
    }
    
    GrouperMessagingSystem grouperMessageSystem = null;
    
    try {
      grouperMessageSystem = GrouperMessagingEngine.retrieveGrouperMessageSystem(messagingSystemName);
    } catch(Exception e) {
      LOG.error("Error occurred while retrieving grouper message system for "+messagingSystemName, e);
    }
    
    Collection<GrouperMessage> grouperMessages = null;
    try {
      grouperMessages = receiveMessages(messagingSystemName, queueOrTopicName, messageQueueType, longPollingSeconds, grouperMessageSystem);
      LOG.info("Received "+grouperMessages.size() +" massages from "+queueOrTopicName +" for message system: "+messagingSystemName);
    } catch (Exception e) {
      LOG.error("Error occurred while receving messages from "+queueOrTopicName, e);
    }
    
    processMessages(messagingSystemName, grouperMessageSystem, grouperMessages);

  }


  /**
   * @param messagingSystemName
   * @param grouperMessageSystem
   * @param grouperMessages
   */
  protected void processMessages(String messagingSystemName, GrouperMessagingSystem grouperMessageSystem,
      Collection<GrouperMessage> grouperMessages) {
    
    for (GrouperMessage inputMessage: grouperMessages) {
      
      String messageBody = inputMessage.getMessageBody();
      JSONObject jsonObject = null;
      
      try { 
        jsonObject = JSONObject.fromObject(messageBody);
      } catch(JSONException e) {
        LOG.error("Error occurred while building json object for "+messageBody);
      }
      
      JSONObject grouperHeaderJson = null;
      try {
        grouperHeaderJson = jsonObject.getJSONObject("grouperHeader");
      } catch (JSONException e) {
        LOG.error("Error occurred while retrieving key grouperHeader out of message body: "+messageBody);
      }
      
      InputMessageGrouperHeader grouperHeader = null;
      try {
        grouperHeader = (InputMessageGrouperHeader)JSONObject.toBean(grouperHeaderJson, InputMessageGrouperHeader.class);
      } catch (JSONException e) {
        LOG.error("Error occurred while building Json object for "+grouperHeaderJson);
      }
      
      Collection<String> errors = validate(grouperHeader);
      
      String replyToQueueOrTopic = grouperHeader.getReplyToQueueOrTopic();
      String replyToQueueOrTopicName = grouperHeader.getReplyToQueueOrTopicName();
      if (errors.size() > 0) {
        if (canReplyToErrorMessages(replyToQueueOrTopicName, replyToQueueOrTopic)) {
          String errorJson = buildErrorResponse(errors, grouperHeader);
          sendReplyMessage(grouperMessageSystem, grouperHeader, messagingSystemName, errorJson);
        } else {
          LOG.error("Invalid message received from the queue. Errors: "+GrouperUtil.collectionToString(errors));
        }
        continue;
      }
      
      String endpoint = grouperHeader.getEndpoint();
      
      String wsRequestBody = jsonObject.getString(endpoint);
      
      String newJson = "{ \"" + endpoint + "\" :" + wsRequestBody + "}";
      
      WsResponse wsReponse = null;
      try {        
        wsReponse = callWebService(newJson, grouperHeader.getHttpPath());
      } catch (Exception e) {
        wsReponse = new WsResponse();
        wsReponse.setHttpStatusCode(400);
        wsReponse.setSuccess("F");
        wsReponse.setResultCode("EXCEPTION");
      }
      
      // now send the response to queue/topic if specified in the message
      if (StringUtils.isNotBlank(replyToQueueOrTopic) && StringUtils.isNotBlank(replyToQueueOrTopicName)) {
        String messageToBeSent = buildWsReplyToMessage(wsReponse, grouperHeader);
        sendReplyMessage(grouperMessageSystem, grouperHeader, messagingSystemName, messageToBeSent);
      }
      
    }
    
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

  /**
   * @param messagingSystemName
   * @param queueOrTopicName
   * @param messageQueueType
   * @param longPollingSeconds
   * @param grouperMessageSystem
   * @return
   */
  private Collection<GrouperMessage> receiveMessages(String messagingSystemName,
      String queueOrTopicName, String messageQueueType, Integer longPollingSeconds,
      GrouperMessagingSystem grouperMessageSystem) {
    
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
    
    GrouperMessageReceiveResult grouperMessageReceiveResult = grouperMessageSystem.receive(receiveParam);
    return grouperMessageReceiveResult.getGrouperMessages();
  } 
  
  /**
   * @param jsonInput
   * @param url
   * @return
   * @throws Exception
   */
  private WsResponse callWebService(String jsonInput, String url) throws Exception {
      
      HttpClient httpClient = new HttpClient();
      
      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      PostMethod method = new PostMethod(url);

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials("GrouperSystem", "admin123");
      
      method.setRequestHeader("Connection", "close");
      
      httpClient.getState().setCredentials(new AuthScope("localhost", 8085), defaultcreds);

      method.setRequestEntity(new StringRequestEntity(jsonInput, "text/x-json", "UTF-8"));
      
      httpClient.executeMethod(method);
      
      String response = method.getResponseBodyAsString();
      
      WsResponse responseHeader = new WsResponse();
      responseHeader.setBody(response);
      responseHeader.setHttpStatusCode(method.getStatusCode());
      //make sure a request came back
      Header successHeader = method.getResponseHeader("X-Grouper-success");
      String successString = successHeader == null ? null : successHeader.getValue();
      responseHeader.setSuccess(successString);
      String resultCode = method.getResponseHeader("X-Grouper-resultCode").getValue();
      responseHeader.setResultCode(resultCode);
      
      String resultCode2 = method.getResponseHeader("X-Grouper-resultCode2").getValue();
      responseHeader.setResultCode2(resultCode2);
      
      return responseHeader;
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

    JSONObject outputHeaderJson = JSONObject.fromObject(outputHeader);
    String header = outputHeaderJson.toString();
    
    String errorMessages = JSONArray.fromObject(errors).toString();
    
    String finalOuput = " { \"grouperHeader\":  "+header+", \"errors\": " + errorMessages + " }" ;
        
    return finalOuput;
    
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

    JSONObject outputHeaderJson = JSONObject.fromObject(outputHeader);
    String header = outputHeaderJson.toString();
    
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
      String finalOuput) {
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
    grouperMessagingSystem.send(sendParam);
  }
  
}
