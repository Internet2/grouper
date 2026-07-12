---
title: "Grouper custom template via GSH - AI ABAC script writer"
space: Grouper
pageId: 28549888
version: 8
lastUpdated: 2026-07-01T05:41:01.353Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549888/Grouper+custom+template+via+GSH+-+AI+ABAC+script+writer
---

This GSH template is an ABAC "pattern" that helps users write ABAC scripts. This is a first pass which will be improved when there are more ways to interact with the UI from an ABAC GSH template pattern. The ABAC data fields are institution specific so this is not re-usable "as is" unlike that other AI training files. This strategy can use used as an example.

## Architecture

This is an AI architecture where the user is only interacting with Grouper.

1. Browser interacts with Grouper (named PennGroups at Penn)
2. If there is an existing conversation with AI for the person using Grouper on the specific ABAC group, get the conversation thread ID
3. If there is a new conversation with AI for the person using Grouper on the specific ABAC group, start a new OpenAI conversation and store the conversation thread ID
4. Use that conversation thread ID and the input from the user to generate an ABAC script and a description of how it works

There are five web services to make this work. But at a high level, the input is from the user and the output is from AI.

## Using the template

The output from AI (called by web service) can be processed better, but for this proof of concept, the entire response is put in the textarea (the only option for ABAC patterns at this time).

There is a conversation with AI (for this user in the UI for this group they are working on)

This is the new response based on the entire conversation

Take out the non script part

Analyze the script, validate the numbers, save

## Configuring / training the AI

This is an Open AI trained assistant. Trained "GPTs" do not have a web service interface, they are OpenAI UI only. "Assistants" are for web services and not OpenAI UI.

Write an ABAC script for a user. Use the file Test110abacAi.txt which has example user prompts and ABAC scripts.

For instance, if a user sends the message:

abac script for students, which means they have the STU affiliation

Then the script is:

entity.hasRow('affiliation', "affiliation_code == 'STU' && affiliation_active_code == 'A' ")

When displaying abac scripts, do not include the java constant that is in the in file. Do not ask the user if they want the abac script wrapped in a constant, it is not necessary.

Sponsored affiliation means affiliation source is in: PERSUPLOAD, WEB

When checking equality of an attribute always use == and never use =

When checking multiple values (e.g. FAC and STU) use this operator: =~ [ 'FAC', 'STU' ]

Use code interpreter supervisoryGroups.csv to lookup supervisors and the group name of their reports.

User code interpreter org_list.csv to look up 4 character organizational codes at penn. This is usually something like an org code data field (attribute)

Do not have scripts that are too long. You can have a line break before an && or an || for example.

File search:  (bunch of data (exported from SQL) and example scripts (generated using Github Copilot AI in Eclipse)

File search:  (PDF print of the data dictionary from Grouper at Penn)

Code interpreter: two spreadsheets

1. Supervisory groups and names of supervisors
2. Organization list including hierarchy

## GSH template configuration

Here is the code for the GSH template. Note: in the future we will have these OpenAI API calls in the Grouper API so they can just be called from the template.

Note: the [AI GSH agent](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549180/Grouper+AI+public+OpenAI+GSH+agent) is fully trained to be able to write all of this GSH template in sections (or at least give a starting point).

```
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class Test127AbacAi extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    // Retrieve the input prompt
    String gsh_input_prompt = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_prompt");

    // groupname
    String groupName = gshTemplateV2input.getGsh_builtin_ownerGroupName();
    String pennId = gshTemplateV2input.getGsh_builtin_subjectId();
    
    // see if there is a threadId
    String threadId = new GcDbAccess().sql("select thread_id from penn_abac_ai_threads where group_name = ? and penn_id = ?").
        addBindVar(groupName).addBindVar(pennId).select(String.class);

    // if not, create one
    if (StringUtils.isBlank(threadId)) {

      // create a thread
      threadId = retrieveOpenAiThreadId("openaiExploration");

      // insert into db
      new GcDbAccess().sql(
          "insert into penn_abac_ai_threads (group_name, penn_id, thread_id) values (?, ?, ?)").
          addBindVar(groupName).addBindVar(pennId).addBindVar(threadId).executeSql();

    }
    
    // see if there is an existing script on this group
    String sql = """
        select value_string from grouper_aval_asn_asn_group_v gaaagv 
        where group_name = ?
        and attribute_def_name_name1 = 'penn:etc:attribute:abacJexlScript:grouperJexlScriptMarker'
        """;
    String existingScript = new GcDbAccess().sql(sql).
        addBindVar(groupName).select(String.class);
    
    // if there is an existing script, prepend the existing script to the input prompt
    if (!StringUtils.isBlank(existingScript)) {
      gsh_input_prompt = "Existing script is: " + existingScript + "\n" + gsh_input_prompt;
    }
    
    // Send the initial message to OpenAI
    sendMessageToOpenAi("openaiExploration", threadId, gsh_input_prompt);
    
    // Run the thread on an assistant
    String runId = runThreadOnAssistant("openaiExploration", threadId, "asst_assistanthgd643");
    
    // loop to wait for the response
    // this is a polling loop, not the best way to do this, but it works for now
    boolean completed = false;
    for (int i = 0; i < 60; i++) {
      // wait a bit
      GrouperUtil.sleep(1000);

      // check the status of the thread
      completed = retrieveThreadStatus("openaiExploration", threadId, runId);
      if (completed) {
        break;
      }
    }
    // if we didn't complete, throw an error
    if (!completed) {
      throw new RuntimeException(
          "Thread did not complete in time, threadId: " + threadId + ", runId: " + runId);
    }
    
    // Retrieve the output from OpenAI
    String output = retrieveFirstMessageFromThread("openaiExploration", threadId);
    
    
    gsh_builtin_gshTemplateOutput.assignAbacScript(output);
    
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
  
  //  ################# GET MESSAGES: SUBSTITUTE THREAD
  //
  //  curl https://api.openai.com/v1/threads/thread_threadxyz123/messages \
  //  -H "Content-Type: application/json" \
  //  -H "Authorization: Bearer sk-proj-abc123" \
  //  -H "OpenAI-Beta: assistants=v2"
  //
  //
  //  {
  //    "object": "list",
  //    "data": [
  //      {
  //        "id": "msg_messagecvb456",
  //        "object": "thread.message",
  //        "created_at": 1749044781,
  //        "assistant_id": "asst_assistanthgd643",
  //        "thread_id": "thread_threadxyz123",
  //        "run_id": "run_runpoi098",
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
  //        "thread_id": "thread_threadxyz123",
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
  //    "first_id": "msg_messagecvb456",
  //    "last_id": "msg_SoI48EXRJADETWdL21AmeUco",
  //    "has_more": false
  //  }

  
  /**
   * get the status of a thread
   * @return true if the thread is completed, false otherwise
   */
  public boolean retrieveThreadStatus(String externalSystemConfigId, String threadId, String runId) {

    //  curl https://api.openai.com/v1/threads/thread_threadxyz123/runs \
    //    -H "Content-Type: application/json" \
    //    -H "Authorization: Bearer sk-proj-abc123" \
    //    -H "OpenAI-Beta: assistants=v2"
    //
    //
    //
    //    {
    //      "object": "list",
    //      "data": [
    //        {
    //          "id": "run_runpoi098",
    //          "object": "thread.run",
    //          "created_at": 1748817086,
    //          "assistant_id": "asst_assistanthgd643",
    //          "thread_id": "thread_threadfgh456",
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
  
//  /**
//   * run the thread on an assistant
//   */
//  public String runThreadOnAssistant(String externalSystemConfigId, String threadId, String assistantId) {
//    
//    // construct request body JSON with assistant_id
//    ObjectNode requestNode = GrouperUtil.jsonJacksonNode();
//    GrouperUtil.jsonJacksonAssignString(requestNode, "assistant_id", assistantId);
//    String requestBody = requestNode.toString();
//
//    // create HTTP client
//    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
//
//    // add headers
//    grouperHttpClient.addHeader("Content-Type", "application/json");
//    grouperHttpClient.addHeader("OpenAI-Beta", "assistants=v2");
//
//    // authenticate using external system
//    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, externalSystemConfigId);
//
//    // retrieve and prepare endpoint
//    String endpoint = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + externalSystemConfigId + ".endpoint");
//    endpoint = GrouperUtil.stripLastSlashIfExists(endpoint);
//
//    // append path for the thread run
//    String url = endpoint + "/v1/threads/" + threadId + "/runs";
//    grouperHttpClient.assignUrl(url);
//
//    // assign request body
//    grouperHttpClient.assignBody(requestBody);
//
//    // set HTTP method to POST
//    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
//
//    // execute the web service call
//    grouperHttpClient.executeRequest();
//
//    // check for 200 OK response
//    int responseCode = grouperHttpClient.getResponseCode();
//    if (responseCode != 200) {
//      throw new RuntimeException("Unexpected response code from OpenAI: " + responseCode);
//    }
//
//    // parse JSON response
//    JsonNode jsonResponse = grouperHttpClient.retrieveJsonNode();
//
//    // extract "id" from response
//    String runId = GrouperUtil.jsonJacksonGetString(jsonResponse, "id");
//    return runId;
//    
//    
//  }
  
  /**
   * run the thread on an assistant
   * @return the run id
   */
  public String runThreadOnAssistant(String externalSystemConfigId, String threadId, String assistantId) {

    //  POST
    //  curl https://api.openai.com/v1/threads/thread_threadfgh456/runs \
    //    -H "Content-Type: application/json" \
    //    -H "Authorization: Bearer sk-proj-abc123" \
    //    -H "OpenAI-Beta: assistants=v2" \
    //    -d '{
    //      "assistant_id": "asst_assistanthgd643"
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
    //    "id": "thread_threadxyz123",
    //    "object": "thread",
    //    "created_at": 1748824503,
    //    "metadata": {},
    //    "tool_resources": {}
    //  }
    
    // get the id
    String runId = GrouperUtil.jsonJacksonGetString(jsonNode, "id");
    return runId;
    
    
  }
  
//  /**
//   * send a message to OpenAI
//   */
//  public void sendMessageToOpenAi(String externalSystemConfigId, String threadId, String message) {
//  
//    // Create HTTP client
//    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
//  
//    // Attach headers
//    grouperHttpClient.addHeader("Content-Type", "application/json");
//    grouperHttpClient.addHeader("OpenAI-Beta", "assistants=v2");
//  
//    // Attach bearer token from external system
//    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, externalSystemConfigId);
//  
//    // Construct endpoint URL
//    String endpoint = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + externalSystemConfigId + ".endpoint");
//    endpoint = GrouperUtil.stripLastSlashIfExists(endpoint);
//    String url = endpoint + "/v1/threads/" + threadId;
//    grouperHttpClient.assignUrl(url);
//  
//    // Specify POST method
//    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
//  
//    // Create JSON payload
//    ObjectNode jsonPayload = GrouperUtil.jsonJacksonNode();
//    GrouperUtil.jsonJacksonAssignString(jsonPayload, "role", "user");
//    GrouperUtil.jsonJacksonAssignString(jsonPayload, "content", message);
//    String jsonBody = jsonPayload.toString();
//    grouperHttpClient.assignBody(jsonBody);
//  
//    // Execute request
//    grouperHttpClient.executeRequest();
//  
//    // Check response code
//    int responseCode = grouperHttpClient.getResponseCode();
//    if (responseCode != 200) {
//      throw new RuntimeException("Expected HTTP 200 but got: " + responseCode);
//    }
//  }
  /**
   * send a message to OpenAI
   */
  public void sendMessageToOpenAi(String externalSystemConfigId, String threadId, String message) {

    // curl https://api.openai.com/v1/threads/thread_threadfgh456/messages \
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

//  /**
//   * get thread id
//   */
//  public String retrieveOpenAiThreadId(String externalSystemConfigId) {
//    // Initialize HTTP client
//    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
//
//    // Authenticate using bearer token from external system config
//    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, externalSystemConfigId);
//
//    // Add required headers
//    grouperHttpClient.addHeader("Content-Type", "application/json");
//    grouperHttpClient.addHeader("OpenAI-Beta", "assistants=v2");
//
//    // Retrieve and construct endpoint
//    String endpoint = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + externalSystemConfigId + ".endpoint");
//    endpoint = GrouperUtil.stripLastSlashIfExists(endpoint);
//    String path = "/v1/threads";
//    String url = endpoint + path;
//
//    // Set method to POST
//    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
//    
//    // Assign URL
//    grouperHttpClient.assignUrl(url);
//
//    // Execute request
//    grouperHttpClient.executeRequest();
//
//    // Check for successful response
//    int responseCode = grouperHttpClient.getResponseCode();
//    if (responseCode != 200) {
//      throw new RuntimeException("Web service call failed with response code: " + responseCode);
//    }
//
//    // Parse response to JSON
//    JsonNode jsonNode = grouperHttpClient.retrieveJsonNode();
//
//    // Extract thread ID from response
//    String threadId = GrouperUtil.jsonJacksonGetString(jsonNode, "id");
//    return threadId;
//  }
  
  
  /**
   * get a thread id from OpenAI
   * @return
   */
  public String retrieveOpenAiThreadId(String externalSystemConfigId) {
    
    //    curl https://api.openai.com/v1/threads \
    //      -H "Content-Type: application/json" \
    //      -H "Authorization: Bearer sk-proj-abc123" \
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
    //    "id": "thread_threadxyz123",
    //    "object": "thread",
    //    "created_at": 1748824503,
    //    "metadata": {},
    //    "tool_resources": {}
    //  }
    
    // get the id
    String threadId = GrouperUtil.jsonJacksonGetString(jsonNode, "id");
    return threadId;
  }
}
```

Note, the conversation ID needs to be stored, in this case for AI and ABAC for a group and a user editing the group. A database table was created for this:

## Example of using this AI script for complex logic requirement

This is an example that we go over from Grouper Training, but also was used recently in the real world.

This is an experimental feature that can help write a group script. Note: do not trust the output of AI, it must be carefully reviewed and vetted to be correct.

Note: if using other groups, tell AI which groups to use and what they represent since this AI does not currently have that visibility into Grouper.

Submitting that will call an AI web service with that prompt, and the Penn AI ABAC agent will use its training and the prompt to return an ABAC script into the textarea, and explain it.

Take the script part out of the response (dont need the explanation)

Confirm the counts

Script is complete!
