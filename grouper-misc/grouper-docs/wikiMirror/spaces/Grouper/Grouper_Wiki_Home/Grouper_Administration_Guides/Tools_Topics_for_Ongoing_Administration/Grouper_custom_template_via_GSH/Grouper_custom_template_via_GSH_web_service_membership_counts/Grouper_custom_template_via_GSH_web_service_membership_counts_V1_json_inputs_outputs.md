---
title: "Grouper custom template via GSH web service membership counts - V1 - json inputs / outputs"
space: Grouper
pageId: 28555697
version: 7
lastUpdated: 2023-12-25T22:51:16.085Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555697/Grouper+custom+template+via+GSH+web+service+membership+counts+-+V1+-+json+inputs+outputs
---

This example is for v4.10.0+ and v5.7.0+.

Get the immediate and total membership counts for a group. Use template inputs, but use a more sensible JSON input structure. JSON output

## Config

```
grouperGshTemplate.membershipCountV1jsonInputs.allowWsFromNoOwner = true
grouperGshTemplate.membershipCountV1jsonInputs.displayErrorOutput = true
grouperGshTemplate.membershipCountV1jsonInputs.groupUuidCanRun = test\u003AtemplateRunners
grouperGshTemplate.membershipCountV1jsonInputs.gshTemplate = //
grouperGshTemplate.membershipCountV1jsonInputs.input.0.defaultValue = test\u003AtestGroup
grouperGshTemplate.membershipCountV1jsonInputs.input.0.description = fully qualified group name (ID path)
grouperGshTemplate.membershipCountV1jsonInputs.input.0.formElementType = text
grouperGshTemplate.membershipCountV1jsonInputs.input.0.label = Group name
grouperGshTemplate.membershipCountV1jsonInputs.input.0.name = gsh_input_groupName
grouperGshTemplate.membershipCountV1jsonInputs.input.0.type = string
grouperGshTemplate.membershipCountV1jsonInputs.input.0.validationRegex = ^[a-zA-Z0-9_.\u003A-]+\u0024
grouperGshTemplate.membershipCountV1jsonInputs.input.0.validationType = regex
grouperGshTemplate.membershipCountV1jsonInputs.numberOfInputs = 1
grouperGshTemplate.membershipCountV1jsonInputs.runAsType = GrouperSystem
grouperGshTemplate.membershipCountV1jsonInputs.securityRunType = specifiedGroup
grouperGshTemplate.membershipCountV1jsonInputs.templateDescription = count membership immediate and total of group
grouperGshTemplate.membershipCountV1jsonInputs.templateName = membershipCountV1 json inputs
 
```

## GSH script

```
import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class Test25membershipCountV2jsonInputs extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    String gsh_input_groupName = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_groupName");
    
    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
    
    String effectiveQuery = "select count(*) from grouper_memberships_lw_v where group_name = ? and list_name = 'members'";
    int effectiveCount = new GcDbAccess().sql(effectiveQuery).addBindVar(gsh_input_groupName).selectList(Integer.class).get(0);
 
    String immediateQuery = "select count(*) from grouper_memberships_v where group_name = ? and list_name = 'members' and membership_type = 'immediate'";
    int immediateCount = new GcDbAccess().sql(immediateQuery).addBindVar(gsh_input_groupName).selectList(Integer.class).get(0);
 
    Map<String, Object> jsonOutput = new LinkedHashMap<>();
    
    
    jsonOutput.put("totalMembershipCount", GrouperUtil.longValue(effectiveCount));
    jsonOutput.put("immediateMembershipCount", GrouperUtil.longValue(immediateCount));
     
    gsh_builtin_gshTemplateOutput.setWsOutput(jsonOutput);
  }

}
```

## Sample WS call

Request

```
POST https://grouperWs.school.edu/grouper-ws/servicesRest/2.6.0/gshTemplateExec
Content-Type: application/json
Authorization: sas9f8d7sa9df87asd98f

{
  "WsRestGshTemplateExecRequest":{
    "configId":"membershipCountV1jsonInputs",
    "wsInput":{
        "gsh_input_groupName":"test:testGroup"
    }
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
      "resultMessage": "Success for: clientVersion: 4.0.0, configId: membershipCountV1jsonInputs, ownerType: null , inputs: null\n, actAsSubject: null, paramNames: \n, params: null",
      "success": "T"
    },
    "responseMetadata": {
      "millis": "1308",
      "serverVersion": "4.0.0"
    },
    "transaction": true,
    "gshValidationLines": [],
    "gshOutputLines": [],
    "gshExitCode": 0,
    "wsOutput": {
      "totalMembershipCount": 12,
      "immediateMembershipCount": 10
    }
  }
}
```
