package edu.internet2.middleware.grouper.ai.openai;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperOpenAiApiCommands {

  public GrouperOpenAiApiCommands() {
    // TODO Auto-generated constructor stub
  }

  /**
   * get the first message from OpenAI
   */
  public static String retrieveFirstMessageFromThread(String externalSystemConfigId, String threadId) {
    
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    // add header content type
    grouperHttpClient.addHeader("Content-Type", "application/json");
    // add open ai beta header
    grouperHttpClient.addHeader("OpenAI-Beta", "assistants=v2");
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient,
        externalSystemConfigId);
    String endpoint = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired(
            "grouper.wsBearerToken." + externalSystemConfigId + ".endpoint");
  
    String url = GrouperUtil.stripLastSlashIfExists(endpoint);
  
    url += "/v1/threads/" + threadId + "/messages";
    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
    
    grouperHttpClient.executeRequest();
    // check response code
    int responseCode = grouperHttpClient.getResponseCode();
    if (responseCode != 200) {
      throw new RuntimeException(
          "Failed to get thread status, response code: " + responseCode);
    }
    // Read the JSON response body
    JsonNode jsonNode = grouperHttpClient.retrieveJsonNode();
    // get data array
    ArrayNode dataArrayNode = GrouperUtil.jsonJacksonGetArrayNode(jsonNode, "data");
    
    // get first data
    if (dataArrayNode.size() == 0) {
      throw new RuntimeException("No messages found in thread: " + threadId);
    }
    JsonNode firstDataNode = dataArrayNode.get(0);
    // get the content
    ArrayNode contentArrayNode = GrouperUtil.jsonJacksonGetArrayNode(firstDataNode, "content");
    if (contentArrayNode.size() == 0) {
      throw new RuntimeException(
          "No content found in first message of thread: " + threadId);
    }
    JsonNode firstContentNode = contentArrayNode.get(0);
    // get the text object
    JsonNode textNode = GrouperUtil.jsonJacksonGetNode(firstContentNode, "text");
    
    String textValue = GrouperUtil.jsonJacksonGetString(textNode, "value");
    
    return textValue;
  
  }
    
  
  /**
   * get a thread id from OpenAI
   * @return
   */
  public static String retrieveOpenAiThreadId(String externalSystemConfigId) {
    
    //    curl https://api.openai.com/v1/threads \
    //      -H "Content-Type: application/json" \
    //      -H "Authorization: Bearer " \
    //      -H "OpenAI-Beta: assistants=v2" \
    //      -d ''

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
    // add header content type
    grouperHttpClient.addHeader("Content-Type", "application/json");
    
    // add open ai beta header
    grouperHttpClient.addHeader("OpenAI-Beta", "assistants=v2");

    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, externalSystemConfigId);
    
    
    String endpoint = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired("grouper.wsBearerToken." + externalSystemConfigId + ".endpoint");
    
    String url = GrouperUtil.stripLastSlashIfExists(endpoint);

    url += "/v1/threads";
    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
    grouperHttpClient.executeRequest();
    // check response code
    int responseCode = grouperHttpClient.getResponseCode();
    if (responseCode != 200) {
      throw new RuntimeException("Failed to create OpenAI thread, response code: " + responseCode);
    }
    // Read the JSON response body
    JsonNode jsonNode = grouperHttpClient.retrieveJsonNode();
    
    //  {
    //    "id": "thread_0OMxsx4gZO1iJia2NDMxsODs",
    //    "object": "thread",
    //    "created_at": 1748824503,
    //    "metadata": {},
    //    "tool_resources": {}
    //  }
    
    // get the id
    String threadId = GrouperUtil.jsonJacksonGetString(jsonNode, "id");
    return threadId;
  }

  //  ################# GET MESSAGES: SUBSTITUTE THREAD
  //
  //  curl https://api.openai.com/v1/threads/thread_0OMxsx4gZO1iJia2NDMxsODs/messages \
  //  -H "Content-Type: application/json" \
  //  -H "Authorization: Bearer " \
  //  -H "OpenAI-Beta: assistants=v2"
  //
  //
  //  {
  //    "object": "list",
  //    "data": [
  //      {
  //        "id": "msg_NPqgQyKReJB02wl5sPQR2dwq",
  //        "object": "thread.message",
  //        "created_at": 1749044781,
  //        "assistant_id": "asst_sDIDZuo7HEJHuO4IKIn9QTwr",
  //        "thread_id": "thread_0OMxsx4gZO1iJia2NDMxsODs",
  //        "run_id": "run_uVzqUgahk3G4Ar3FEmtoeHRS",
  //        "role": "assistant",
  //        "content": [
  //          {
  //            "type": "text",
  //            "text": {
  //              "value": "To write an ABAC script for service providers in the School of Engineering and Applied Science (SEAS), we'll be considering the organization code and affiliation code related to service providers. In the context provided, the school code for SEAS is usually represented by '13'. Let's assume the standard setup used in the document for service providers (contractors or unpaid workers) is to use the code 'SERV' for Service Providers.\n\nHere is the ABAC script for service providers in SEAS:\n\n```plaintext\nentity.hasRow('affiliation', \"affiliation_code == 'SERV' \n                              && affiliation_center == '13' \")\n```\n\nThis script will match individuals who are recognized as service providers (SERV code) within the SEAS (School of Engineering and Applied Science), which is identified by the center code '13'.",
  //              "annotations": []
  //            }
  //          }
  //        ],
  //        "attachments": [],
  //        "metadata": {}
  //      },
  //      {
  //        "id": "msg_SoI48EXRJADETWdL21AmeUco",
  //        "object": "thread.message",
  //        "created_at": 1749044670,
  //        "assistant_id": null,
  //        "thread_id": "thread_0OMxsx4gZO1iJia2NDMxsODs",
  //        "run_id": null,
  //        "role": "user",
  //        "content": [
  //          {
  //            "type": "text",
  //            "text": {
  //              "value": "Write an ABAC script service proviers in seas",
  //              "annotations": []
  //            }
  //          }
  //        ],
  //        "attachments": [],
  //        "metadata": {}
  //      }
  //    ],
  //    "first_id": "msg_NPqgQyKReJB02wl5sPQR2dwq",
  //    "last_id": "msg_SoI48EXRJADETWdL21AmeUco",
  //    "has_more": false
  //  }
  
  
  /**
   * get the status of a thread
   * @return true if the thread is completed, false otherwise
   */
  public static boolean retrieveThreadStatus(String externalSystemConfigId, String threadId, String runId) {
  
    //  curl https://api.openai.com/v1/threads/thread_0OMxsx4gZO1iJia2NDMxsODs/runs \
    //    -H "Content-Type: application/json" \
    //    -H "Authorization: Bearer " \
    //    -H "OpenAI-Beta: assistants=v2"
    //
    //
    //
    //    {
    //      "object": "list",
    //      "data": [
    //        {
    //          "id": "run_wovxWagh8YBiZ9vXyR8Nb836",
    //          "object": "thread.run",
    //          "created_at": 1748817086,
    //          "assistant_id": "asst_T32PZupfX84qOZKuyy6Cmw9R",
    //          "thread_id": "thread_VA8q087u0SWuAtAEhQf30eQn",
    //          "status": "completed",
  
    
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    // add header content type
    grouperHttpClient.addHeader("Content-Type", "application/json");
    // add open ai beta header
    grouperHttpClient.addHeader("OpenAI-Beta", "assistants=v2");
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient,
        externalSystemConfigId);
    String endpoint = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired(
            "grouper.wsBearerToken." + externalSystemConfigId + ".endpoint");
  
    String url = GrouperUtil.stripLastSlashIfExists(endpoint);
  
    url += "/v1/threads/" + threadId + "/runs";
    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
    
    grouperHttpClient.executeRequest();
    // check response code
    int responseCode = grouperHttpClient.getResponseCode();
    if (responseCode != 200) {
      throw new RuntimeException(
          "Failed to get thread status, response code: " + responseCode);
    }
    // Read the JSON response body
    JsonNode jsonNode = grouperHttpClient.retrieveJsonNode();
    // get data array
    ArrayNode dataArrayNode = GrouperUtil.jsonJacksonGetArrayNode(jsonNode, "data");
    
    // loop through the data array
    for (JsonNode dataNode : dataArrayNode) {
      // get the id
      String runIdFromResponse = GrouperUtil.jsonJacksonGetString(dataNode, "id");
      if (StringUtils.equals(runIdFromResponse, runId)) {
        // we found the run, so return the status
        String status = GrouperUtil.jsonJacksonGetString(dataNode, "status");
        return StringUtils.equals(status, "completed");
      }
    }
  
    // if we get here, we didn't find the run id in the response
    throw new RuntimeException(
        "Failed to find run id: " + runId + " in thread: " + threadId
            + ", response code: " + responseCode);
  }

    
  /**
   * run the thread on an assistant
   * @return the run id
   */
  public static String runThreadOnAssistant(String externalSystemConfigId, String threadId, String assistantId) {

    //  POST
    //  curl https://api.openai.com/v1/threads/thread_FPhaCFv7WrRFfVAG9YcEHKMw/runs \
    //    -H "Content-Type: application/json" \
    //    -H "Authorization: Bearer " \
    //    -H "OpenAI-Beta: assistants=v2" \
    //    -d '{
    //      "assistant_id": "asst_sDIDZuo7HEJHuO4IKIn9QTwr"
    //    }'
    
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    // add header content type
    grouperHttpClient.addHeader("Content-Type", "application/json");

    // add open ai beta header
    grouperHttpClient.addHeader("OpenAI-Beta", "assistants=v2");

    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient,
        externalSystemConfigId);

    String endpoint = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired(
            "grouper.wsBearerToken." + externalSystemConfigId + ".endpoint");

    String url = GrouperUtil.stripLastSlashIfExists(endpoint);

    url += "/v1/threads/" + threadId + "/runs";

    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);

    ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
    
    GrouperUtil.jsonJacksonAssignString(objectNode, "assistant_id", assistantId);
    
    grouperHttpClient.assignBody(objectNode.toString());

    grouperHttpClient.executeRequest();

    // check response code
    int responseCode = grouperHttpClient.getResponseCode();
    
    if (responseCode != 200) {
      throw new RuntimeException(
          "Failed to run thread on assistant, response code: " + responseCode);
    }
    
    //  {
    //    "id": "run_4Gm59xazfg36f1XDxdIYUjbx",

    // Read the JSON response body
    JsonNode jsonNode = grouperHttpClient.retrieveJsonNode();
    
    //  {
    //    "id": "thread_0OMxsx4gZO1iJia2NDMxsODs",
    //    "object": "thread",
    //    "created_at": 1748824503,
    //    "metadata": {},
    //    "tool_resources": {}
    //  }
    
    // get the id
    String runId = GrouperUtil.jsonJacksonGetString(jsonNode, "id");
    return runId;
    
    
  }

  /**
   * send a message to OpenAI
   */
  public static void sendMessageToOpenAi(String externalSystemConfigId, String threadId, String message) {

    // curl https://api.openai.com/v1/threads/thread_FPhaCFv7WrRFfVAG9YcEHKMw/messages \
    // -H "Content-Type: application/json" \
    // -H "OpenAI-Beta: assistants=v2" \
    // -d '{
    //     "role": "user",
    //     "content": "Write an ABAC script faculty in the dental school"
    //   }'

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    // add header content type
    grouperHttpClient.addHeader("Content-Type", "application/json");

    // add open ai beta header
    grouperHttpClient.addHeader("OpenAI-Beta", "assistants=v2");

    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient,
        externalSystemConfigId);

    String endpoint = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired(
            "grouper.wsBearerToken." + externalSystemConfigId + ".endpoint");

    String url = GrouperUtil.stripLastSlashIfExists(endpoint);

    url += "/v1/threads/" + threadId + "/messages";

    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);

    ObjectNode jsonNode = GrouperUtil.jsonJacksonNode();
    
    GrouperUtil.jsonJacksonAssignString(jsonNode, "role", "user");
    GrouperUtil.jsonJacksonAssignString(jsonNode, "content", message);    
    grouperHttpClient.assignBody(jsonNode.toString());

    grouperHttpClient.executeRequest();

    // check response code
    int responseCode = grouperHttpClient.getResponseCode();
    if (responseCode != 200) {
      throw new RuntimeException(
          "Failed to send message to OpenAI, response code: " + responseCode);
    }
  }

}
