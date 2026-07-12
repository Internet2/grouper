---
title: "Grouper custom template via GSH web service membership counts - V2 - bean inputs with test"
space: Grouper
pageId: 28554853
version: 4
lastUpdated: 2023-12-26T08:07:27.769Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554853/Grouper+custom+template+via+GSH+web+service+membership+counts+-+V2+-+bean+inputs+with+test
---

This example is for v4.10.0+ and v5.7.0+.

For V2, convert the arbitrary input JSON to a bean, convert a bean to the output. Have a test to verify logic

## Test results

## Config

grouper.properties

```
grouperGshTemplate.membershipCountV2beanInputsWithTest.allowWsFromNoOwner = true
grouperGshTemplate.membershipCountV2beanInputsWithTest.displayErrorOutput = true
grouperGshTemplate.membershipCountV2beanInputsWithTest.groupUuidCanRun = test\u003AtemplateRunners
grouperGshTemplate.membershipCountV2beanInputsWithTest.gshTemplate = //
grouperGshTemplate.membershipCountV2beanInputsWithTest.runAsType = GrouperSystem
grouperGshTemplate.membershipCountV2beanInputsWithTest.securityRunType = specifiedGroup
grouperGshTemplate.membershipCountV2beanInputsWithTest.templateDescription = count membership immediate and total of group
grouperGshTemplate.membershipCountV2beanInputsWithTest.templateName = membershipCountV2 json inputs
grouperGshTemplate.membershipCountV2beanInputsWithTest.templateVersion = V2
```

## GSH script

```
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2test;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class Test25membershipCountV2beanInputsWithTest extends GshTemplateV2 {

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
    
    wsOutputBean.setTotalMembershipCount(effectiveCount);
    wsOutputBean.setImmediateMembershipCount(immediateCount);
    
    gsh_builtin_gshTemplateOutput.setWsOutput(wsOutputBean);
  }
  
  public GshTemplateV2test testQueries() {
    return new TestQueries();
  }
  
  public static class TestQueries extends GshTemplateV2test {

    @Override
    protected void setUp() {
      WsInputBean wsInputBean = new WsInputBean();
      wsInputBean.setGroupName("test:testGroup10");
      this.setGshWsInput(wsInputBean);
      this.assignGshSubjectUsingApp("g:isa", "GrouperSystem");
      this.setGshTemplateConfigId("membershipCountV2beanInputsWithTest");

      Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
      Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

      Group testGroup11 = new GroupSave().assignName("test:testGroup11").assignCreateParentStemsIfNotExist(true).save();
      Group testGroup10 = new GroupSave().assignName("test:testGroup10").assignCreateParentStemsIfNotExist(true).save();

      testGroup11.replaceMembers(GrouperUtil.toList(subject0));
      testGroup10.replaceMembers(GrouperUtil.toList(subject1, testGroup11.toSubject()));
      
    }

    @Override
    public void gshCheckResult() {
      WsOutputBean wsOutputBean = (WsOutputBean)this.getGshTemplateOutput().getWsOutput();
      assertEquals(2, wsOutputBean.getImmediateMembershipCount());
      assertEquals(3, wsOutputBean.getTotalMembershipCount());
    }
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
    "configId":"membershipCountV2beanInputsWithTest",
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
      "resultMessage": "Success for: clientVersion: 4.0.0, configId: membershipCountV2beanInputsWithTest, ownerType: null , inputs: null\n, actAsSubject: null, paramNames: \n, params: null",
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
