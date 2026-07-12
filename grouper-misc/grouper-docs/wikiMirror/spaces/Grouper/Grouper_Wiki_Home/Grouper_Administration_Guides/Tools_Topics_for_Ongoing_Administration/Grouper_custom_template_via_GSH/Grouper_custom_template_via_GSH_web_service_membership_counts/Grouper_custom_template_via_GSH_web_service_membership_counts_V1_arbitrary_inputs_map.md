---
title: "Grouper custom template via GSH web service membership counts - V1 - arbitrary inputs map"
space: Grouper
pageId: 28554860
version: 8
lastUpdated: 2023-12-25T22:52:48.450Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554860/Grouper+custom+template+via+GSH+web+service+membership+counts+-+V1+-+arbitrary+inputs+map
---

This example is for v4.10.0+ and v5.7.0+.

Get the immediate and total membership counts for a group. Does not use template inputs, arbitrary JSON inputs

## Config

```
grouperGshTemplate.membershipCountV1arbitraryInputs.allowWsFromNoOwner = true
grouperGshTemplate.membershipCountV1arbitraryInputs.displayErrorOutput = true
grouperGshTemplate.membershipCountV1arbitraryInputs.groupUuidCanRun = test\u003AtemplateRunners
grouperGshTemplate.membershipCountV1arbitraryInputs.gshTemplate = //
grouperGshTemplate.membershipCountV1arbitraryInputs.runAsType = GrouperSystem
grouperGshTemplate.membershipCountV1arbitraryInputs.securityRunType = specifiedGroup
grouperGshTemplate.membershipCountV1arbitraryInputs.templateDescription = count membership immediate and total of group
grouperGshTemplate.membershipCountV1arbitraryInputs.templateName = membershipCountV1 arbitrary inputs   
```

## GSH script

```
//uncomment to compile in eclipse (and last line)
// these are standard imports, can be commented out in script but needed in eclipse
import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
 
//public class Test25membershipCountV1arbitraryInputs {
//
//  public static void main(String[] args) {
//   
//    GrouperStartup.startup();
//   
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
//    Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("mchyzer", "pennperson", true);
//    GrouperGroovyRuntime grouperGroovyRuntime = new GrouperGroovyRuntime();
//    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();
//    GshTemplateRuntime gsh_builtin_gshTemplateRuntime = new GshTemplateRuntime();
    
    Map<String, Object> wsInput = gsh_builtin_gshTemplateRuntime.getWsInput();
    String groupName = (String)wsInput.get("groupName");
    
    String effectiveQuery = "select count(*) from grouper_memberships_lw_v where group_name = ? and list_name = 'members'";
    int effectiveCount = new GcDbAccess().sql(effectiveQuery).addBindVar(groupName).selectList(Integer.class).get(0);
 
    String immediateQuery = "select count(*) from grouper_memberships_v where group_name = ? and list_name = 'members' and membership_type = 'immediate'";
    int immediateCount = new GcDbAccess().sql(immediateQuery).addBindVar(groupName).selectList(Integer.class).get(0);
 
    Map<String, Object> jsonOutput = new LinkedHashMap<>();
     
    jsonOutput.put("totalMembershipCount", GrouperUtil.longValue(effectiveCount));
    jsonOutput.put("immediateMembershipCount", GrouperUtil.longValue(immediateCount));
     
    gsh_builtin_gshTemplateOutput.setWsOutput(jsonOutput);
         
//  }
//
//}
```

## Sample WS call

Request

```
POST https://grouperWs.school.edu/grouper-ws/servicesRest/2.6.0/gshTemplateExec
Content-Type: application/json
Authorization: sas9f8d7sa9df87asd98f

{
  "WsRestGshTemplateExecRequest":{
    "configId":"membershipCountV1arbitraryInputs",
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
      "resultMessage": "Success for: clientVersion: 4.0.0, configId: membershipCountV1arbitraryInputs, ownerType: null, inputs: null\n, actAsSubject: null, paramNames: \n, params: null",
      "success": "T"
    },
    "responseMetadata": {
      "millis": "723",
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
