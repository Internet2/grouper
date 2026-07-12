---
title: "Grouper custom template via GSH web service membership counts - V2 - arbitrary inputs"
space: Grouper
pageId: 28554641
version: 6
lastUpdated: 2023-12-25T22:58:59.126Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554641/Grouper+custom+template+via+GSH+web+service+membership+counts+-+V2+-+arbitrary+inputs
---

This example is for v4.10.0+ and v5.7.0+.

For V2, do not use template inputs, arbitrary JSON inputs

## Config

grouper.properties

```
grouperGshTemplate.membershipCountV2arbitraryInputs.allowWsFromNoOwner = true
grouperGshTemplate.membershipCountV2arbitraryInputs.displayErrorOutput = true
grouperGshTemplate.membershipCountV2arbitraryInputs.groupUuidCanRun = test\u003AtemplateRunners
grouperGshTemplate.membershipCountV2arbitraryInputs.gshTemplate = //
grouperGshTemplate.membershipCountV2arbitraryInputs.moreActionsLabel = membershipCountV2 arbitrary inputs
grouperGshTemplate.membershipCountV2arbitraryInputs.runAsType = GrouperSystem
grouperGshTemplate.membershipCountV2arbitraryInputs.securityRunType = specifiedGroup
grouperGshTemplate.membershipCountV2arbitraryInputs.templateDescription = count membership immediate and total of group
grouperGshTemplate.membershipCountV2arbitraryInputs.templateName = membershipCountV2 arbitrary inputs
grouperGshTemplate.membershipCountV2arbitraryInputs.templateVersion = V2
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

public class Test25membershipCountV2arbitraryInputs extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    Map<String, Object> wsInput = gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().getWsInput();
    String groupName = (String)wsInput.get("groupName");
    
    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
    
    String effectiveQuery = "select count(*) from grouper_memberships_lw_v where group_name = ? and list_name = 'members'";
    int effectiveCount = new GcDbAccess().sql(effectiveQuery).addBindVar(groupName).selectList(Integer.class).get(0);
 
    String immediateQuery = "select count(*) from grouper_memberships_v where group_name = ? and list_name = 'members' and membership_type = 'immediate'";
    int immediateCount = new GcDbAccess().sql(immediateQuery).addBindVar(groupName).selectList(Integer.class).get(0);
 
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
    "configId":"membershipCountV2arbitraryInputs",
    "wsInput":{
        "groupName":"test:testGroup"
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
      "resultMessage": "Success for: clientVersion: 4.0.0, configId: membershipCountV2arbitraryInputs, ownerType: null , inputs: null\n, actAsSubject: null, paramNames: \n, params: null",
      "success": "T"
    },
    "responseMetadata": {
      "millis": "36",
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
