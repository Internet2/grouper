---
title: "Grouper custom template via GSH web service membership counts - V2 - basic"
space: Grouper
pageId: 28555690
version: 9
lastUpdated: 2023-12-25T22:55:16.510Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555690/Grouper+custom+template+via+GSH+web+service+membership+counts+-+V2+-+basic
---

This example is for v4.10.0+ and v5.7.0+.

Get the immediate and total membership counts for a group. This is the V1 template converted to V2

## Config

grouper.properties

```
grouperGshTemplate.membershipCountV2basic.allowWsFromNoOwner = true
grouperGshTemplate.membershipCountV2basic.displayErrorOutput = true
grouperGshTemplate.membershipCountV2basic.groupUuidCanRun = test\u003AtemplateRunners
grouperGshTemplate.membershipCountV2basic.gshTemplate = //
grouperGshTemplate.membershipCountV2basic.input.0.defaultValue = test\u003AtestGroup
grouperGshTemplate.membershipCountV2basic.input.0.description = fully qualified group name (ID path)
grouperGshTemplate.membershipCountV2basic.input.0.formElementType = text
grouperGshTemplate.membershipCountV2basic.input.0.label = Group name
grouperGshTemplate.membershipCountV2basic.input.0.name = gsh_input_groupName
grouperGshTemplate.membershipCountV2basic.input.0.type = string
grouperGshTemplate.membershipCountV2basic.input.0.validationRegex = ^[a-zA-Z0-9_.\u003A-]+\u0024
grouperGshTemplate.membershipCountV2basic.input.0.validationType = regex
grouperGshTemplate.membershipCountV2basic.numberOfInputs = 1
grouperGshTemplate.membershipCountV2basic.runAsType = GrouperSystem
grouperGshTemplate.membershipCountV2basic.securityRunType = specifiedGroup
grouperGshTemplate.membershipCountV2basic.templateDescription = count membership immediate and total of group
grouperGshTemplate.membershipCountV2basic.templateName = membershipCountV2 basic
grouperGshTemplate.membershipCountV2basic.templateVersion = V2
  
```

## GSH script

```
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class Test25membershipCountV2basic extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    String gsh_input_groupName = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_groupName");
    
    GrouperSession gsh_builtin_grouperSession = gshTemplateV2input.getGsh_builtin_grouperSession();
    Subject gsh_builtin_subject = gshTemplateV2input.getGsh_builtin_subject();
    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
    
    String effectiveQuery = "select count(*) from grouper_memberships_lw_v where group_name = ? and list_name = 'members'";
    int effectiveCount = new GcDbAccess().sql(effectiveQuery).addBindVar(gsh_input_groupName).selectList(Integer.class).get(0);
 
    String immediateQuery = "select count(*) from grouper_memberships_v where group_name = ? and list_name = 'members' and membership_type = 'immediate'";
    int immediateCount = new GcDbAccess().sql(immediateQuery).addBindVar(gsh_input_groupName).selectList(Integer.class).get(0);
 
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    
    GrouperUtil.jsonJacksonAssignLong(result, "totalMembershipCount", GrouperUtil.longValue(effectiveCount));
    GrouperUtil.jsonJacksonAssignLong(result, "immediateMembershipCount", GrouperUtil.longValue(immediateCount));
     
    gsh_builtin_gshTemplateOutput.addOutputLine(GrouperUtil.jsonJacksonToString(result));
  }

  public static void main(String[] args) {
   
    GrouperStartup.startup();
    GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();
    GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();
    gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_groupName", "test:testGroup");
    new Test25membershipCountV2basic().gshRunLogic(gshTemplateV2input, gshTemplateV2output);
    System.out.println(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getOutputLines().get(0));
    System.exit(0);
  }

}
```

## Sample WS call

Request

```
PUT https://grouperWs.school.edu/grouper-ws/servicesRest/2.6.0/gshTemplateExec
Content-Type: application/json
Authorization: sas9f8d7sa9df87asd98f

{
  "WsRestGshTemplateExecRequest":{
    "configId":"membershipCountV2basic",
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
  "WsGshTemplateExecResult": {
    "resultMetadata": {
      "resultCode": "SUCCESS",
      "resultMessage": "Success for: clientVersion: 4.0.0, configId: membershipCountV2basic, ownerType: null , inputs: Array size: 1: [0]: edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput@1eb2b170\n\n, actAsSubject: null, paramNames: \n, params: null",
      "success": "T"
    },
    "responseMetadata": {
      "millis": "1551",
      "serverVersion": "4.0.0"
    },
    "transaction": true,
    "gshValidationLines": [],
    "gshOutputLines": [{
      "messageType": "success",
      "text": "{\"totalMembershipCount\":12,\"immediateMembershipCount\":10}"
    }]
  }
}
```
