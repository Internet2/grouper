---
title: "Grouper custom template via GSH web service membership counts - V1 - basic"
space: Grouper
pageId: 28555237
version: 11
lastUpdated: 2023-12-25T19:55:33.214Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555237/Grouper+custom+template+via+GSH+web+service+membership+counts+-+V1+-+basic
---

Get the immediate and total membership counts for a group. Basic GSH template WS with template inputs and result packed into template output. This is not recommended.

## Config

```
grouperGshTemplate.membershipCountV1basic.defaultRunButtonFolderUuidOrName = test
grouperGshTemplate.membershipCountV1basic.displayErrorOutput = true
grouperGshTemplate.membershipCountV1basic.folderShowOnDescendants = certainFoldersAndDescendants
grouperGshTemplate.membershipCountV1basic.folderShowType = certainFolders
grouperGshTemplate.membershipCountV1basic.folderUuidToShow = test
grouperGshTemplate.membershipCountV1basic.groupUuidCanRun = test\u003AtemplateRunners
grouperGshTemplate.membershipCountV1basic.gshTemplate = //
grouperGshTemplate.membershipCountV1basic.input.0.defaultValue = test\u003AtestGroup
grouperGshTemplate.membershipCountV1basic.input.0.description = fully qualified group name (ID path)
grouperGshTemplate.membershipCountV1basic.input.0.formElementType = text
grouperGshTemplate.membershipCountV1basic.input.0.label = Group name
grouperGshTemplate.membershipCountV1basic.input.0.name = gsh_input_groupName
grouperGshTemplate.membershipCountV1basic.input.0.type = string
grouperGshTemplate.membershipCountV1basic.input.0.validationRegex = ^[a-zA-Z0-9_.\u003A-]+\u0024
grouperGshTemplate.membershipCountV1basic.input.0.validationType = regex
grouperGshTemplate.membershipCountV1basic.moreActionsLabel = membershipCountV1 basic
grouperGshTemplate.membershipCountV1basic.numberOfInputs = 1
grouperGshTemplate.membershipCountV1basic.runAsType = GrouperSystem
grouperGshTemplate.membershipCountV1basic.runButtonGroupOrFolder = folder
grouperGshTemplate.membershipCountV1basic.securityRunType = specifiedGroup
grouperGshTemplate.membershipCountV1basic.showInMoreActions = true
grouperGshTemplate.membershipCountV1basic.showOnFolders = true
grouperGshTemplate.membershipCountV1basic.templateDescription = count membership immediate and total of group
grouperGshTemplate.membershipCountV1basic.templateName = membershipCountV1 basic  
```

## GSH script

```
//uncomment to compile in eclipse (and last line)
// these are standard imports, can be commented out in script but needed in eclipse
import com.fasterxml.jackson.databind.node.ObjectNode;
 
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
 
//public class Test25membershipCountV1basic {
//
//  public static void main(String[] args) {
//   
//    GrouperStartup.startup();
//   
//    String gsh_input_groupName = "test:group1";
//   
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
//    Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("mchyzer", "pennperson", true);
//    GrouperGroovyRuntime grouperGroovyRuntime = new GrouperGroovyRuntime();
//    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();
 
    String effectiveQuery = "select count(*) from grouper_memberships_lw_v where group_name = ? and list_name = 'members'";
    int effectiveCount = new GcDbAccess().sql(effectiveQuery).addBindVar(gsh_input_groupName).selectList(Integer.class).get(0);
 
    String immediateQuery = "select count(*) from grouper_memberships_v where group_name = ? and list_name = 'members' and membership_type = 'immediate'";
    int immediateCount = new GcDbAccess().sql(immediateQuery).addBindVar(gsh_input_groupName).selectList(Integer.class).get(0);
 
    ObjectNode result = GrouperUtil.jsonJacksonNode();
     
    GrouperUtil.jsonJacksonAssignLong(result, "totalMembershipCount", GrouperUtil.longValue(effectiveCount));
    GrouperUtil.jsonJacksonAssignLong(result, "immediateMembershipCount", GrouperUtil.longValue(immediateCount));
     
    gsh_builtin_gshTemplateOutput.addOutputLine(GrouperUtil.jsonJacksonToString(result));
         
//  }
//
//}
```

## Sample web call

## Sample WS call

Request

```
POST https://grouperWs.school.edu/grouper-ws/servicesRest/2.6.0/gshTemplateExec
Content-Type: application/json
Authorization: sas9f8d7sa9df87asd98f
 
{
  "WsRestGshTemplateExecRequest":{
    "ownerStemLookup":{
      "stemName":"penn:etc:templates:membershipCount"
    },
    "ownerType":"stem",
    "configId":"membershipCountV1basic",
    "inputs":[
      {
        "name":"gsh_input_groupName",
        "value":"test:testGroup"
      }
    ]
  }
}
```

Response

```
STATUS: 200
x-grouper-resultcode: SUCCESS
x-grouper-resultcode2: NONE
x-grouper-success: T
{
   "WsGshTemplateExecResult":{
      "resultMetadata":{
         "success":"T",
         "resultCode":"SUCCESS",
         "resultMessage":"Success for: clientVersion: 2.6.0, configId: membershipCountV1basic, ownerType: stem , inputs: Array size: 1: [0]: edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput@106d7e5e\n\n, actAsSubject: null, paramNames: \n, params: null"
      },
      "gshOutputLines":[
         {
            "messageType":"success",
            "text":"{\"totalMembershipCount\":3,\"immediateMembershipCount\":3}"
         }
      ],
      "responseMetadata":{
         "serverVersion":"2.5.55",
         "millis":"4046"
      },
      "gshValidationLines":[],
      "transaction":true
   }
}
```
