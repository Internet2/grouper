---
title: "Grouper custom template via GSH web service membership counts - V2 - json inputs"
space: Grouper
pageId: 28555230
version: 4
lastUpdated: 2023-12-25T22:57:13.826Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555230/Grouper+custom+template+via+GSH+web+service+membership+counts+-+V2+-+json+inputs
---

This example is for v4.10.0+ and v5.7.0+.

Use template inputs for V2, but use a more sensible JSON input structure. JSON output

## Config

grouper.properties

```
grouperGshTemplate.membershipCountV2jsonInputs.allowWsFromNoOwner = true
grouperGshTemplate.membershipCountV2jsonInputs.displayErrorOutput = true
grouperGshTemplate.membershipCountV2jsonInputs.groupUuidCanRun = test\u003AtemplateRunners
grouperGshTemplate.membershipCountV2jsonInputs.gshTemplate = //
grouperGshTemplate.membershipCountV2jsonInputs.input.0.defaultValue = test\u003AtestGroup
grouperGshTemplate.membershipCountV2jsonInputs.input.0.description = fully qualified group name (ID path)
grouperGshTemplate.membershipCountV2jsonInputs.input.0.formElementType = text
grouperGshTemplate.membershipCountV2jsonInputs.input.0.label = Group name
grouperGshTemplate.membershipCountV2jsonInputs.input.0.name = gsh_input_groupName
grouperGshTemplate.membershipCountV2jsonInputs.input.0.type = string
grouperGshTemplate.membershipCountV2jsonInputs.input.0.validationRegex = ^[a-zA-Z0-9_.\u003A-]+\u0024
grouperGshTemplate.membershipCountV2jsonInputs.input.0.validationType = regex
grouperGshTemplate.membershipCountV2jsonInputs.numberOfInputs = 1
grouperGshTemplate.membershipCountV2jsonInputs.runAsType = GrouperSystem
grouperGshTemplate.membershipCountV2jsonInputs.securityRunType = specifiedGroup
grouperGshTemplate.membershipCountV2jsonInputs.templateDescription = count membership immediate and total of group
grouperGshTemplate.membershipCountV2jsonInputs.templateName = membershipCountV2 json inputs
grouperGshTemplate.membershipCountV2jsonInputs.templateVersion = V2   
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
PUT https://grouperWs.school.edu/grouper-ws/servicesRest/2.6.0/gshTemplateExec
Content-Type: application/json
Authorization: sas9f8d7sa9df87asd98f

{
  "WsRestGshTemplateExecRequest":{
    "configId":"membershipCountV2jsonInputs",
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
      "resultMessage": "Success for: clientVersion: 4.0.0, configId: membershipCountV2jsonInputs, ownerType: null , inputs: null\n, actAsSubject: null, paramNames: \n, params: null",
      "success": "T"
    },
    "responseMetadata": {
      "millis": "1003",
      "serverVersion": "4.0.0"
    },
    "transaction": true,
    "gshValidationLines": [],
    "gshOutputLines": [],
    "wsOutput": {
      "totalMembershipCount": 12,
      "immediateMembershipCount": 10
    }
  }
}
```
