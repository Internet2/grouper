---
title: "Grouper custom template via GSH web service membership counts - V2 - bean inputs"
space: Grouper
pageId: 28555205
version: 4
lastUpdated: 2023-12-25T23:19:26.695Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555205/Grouper+custom+template+via+GSH+web+service+membership+counts+-+V2+-+bean+inputs
---

This example is for v4.10.0+ and v5.7.0+.

For V2, convert the arbitrary input JSON to a bean, convert a bean to the output

## Config

grouper.properties

```
grouperGshTemplate.membershipCountV2beanInputs.allowWsFromNoOwner = true
grouperGshTemplate.membershipCountV2beanInputs.displayErrorOutput = true
grouperGshTemplate.membershipCountV2beanInputs.groupUuidCanRun = test\u003AtemplateRunners
grouperGshTemplate.membershipCountV2beanInputs.gshTemplate = //
arouperGshTemplate.membershipCountV2beanInputs.runAsType = GrouperSystem
grouperGshTemplate.membershipCountV2beanInputs.securityRunType = specifiedGroup
grouperGshTemplate.membershipCountV2beanInputs.templateDescription = count membership immediate and total of group
grouperGshTemplate.membershipCountV2beanInputs.templateName = membershipCountV2 bean inputs
grouperGshTemplate.membershipCountV2beanInputs.templateVersion = V2
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

public class Test25membershipCountV2beanInputs extends GshTemplateV2 {

  public static class WsInputBean {
    
    private String groupName;
 
    public String getGroupName() {
      return groupName;
    }
     
    public void setGroupName(String groupName) {
      this.groupName = groupName;
    }
  }
   
  public static class WsOutputBean {

    private int totalMembershipCount;
    private int immediateMembershipCount;
     
    public int getTotalMembershipCount() {
      return totalMembershipCount;
    }
     
    public void setTotalMembershipCount(int totalMembershipCount) {
      this.totalMembershipCount = totalMembershipCount;
    }
     
    public int getImmediateMembershipCount() {
      return immediateMembershipCount;
    }
     
    public void setImmediateMembershipCount(int immediateMembershipCount) {
      this.immediateMembershipCount = immediateMembershipCount;
    }
     
  }
  
  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    Map<String, Object> wsInput = gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().getWsInput();
    WsInputBean wsInputBean = GrouperUtil.jsonConvertFromMap(wsInput, WsInputBean.class);
    String groupName = wsInputBean.getGroupName();
    
    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
    
    String effectiveQuery = "select count(*) from grouper_memberships_lw_v where group_name = ? and list_name = 'members'";
    int effectiveCount = new GcDbAccess().sql(effectiveQuery).addBindVar(groupName).selectList(Integer.class).get(0);
  
    String immediateQuery = "select count(*) from grouper_memberships_v where group_name = ? and list_name = 'members' and membership_type = 'immediate'";
    int immediateCount = new GcDbAccess().sql(immediateQuery).addBindVar(groupName).selectList(Integer.class).get(0);
 
    WsOutputBean wsOutputBean = new WsOutputBean();
    
    wsOutputBean.setTotalMembershipCount(GrouperUtil.intValue(effectiveCount));
    wsOutputBean.setImmediateMembershipCount(GrouperUtil.intValue(immediateCount));
      
    gsh_builtin_gshTemplateOutput.setWsOutput(wsOutputBean);
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
    "configId":"membershipCountV2beanInputs",
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
      "resultMessage": "Success for: clientVersion: 4.0.0, configId: membershipCountV2beanInputs, ownerType: null , inputs: null\n, actAsSubject: null, paramNames: \n, params: null",
      "success": "T"
    },
    "responseMetadata": {
      "millis": "142",
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
