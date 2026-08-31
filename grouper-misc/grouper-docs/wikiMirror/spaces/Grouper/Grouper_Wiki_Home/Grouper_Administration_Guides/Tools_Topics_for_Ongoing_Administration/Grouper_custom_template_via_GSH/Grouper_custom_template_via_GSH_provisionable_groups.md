---
title: "Grouper custom template via GSH - provisionable groups"
space: Grouper
pageId: 28549828
version: 6
lastUpdated: 2026-07-01T05:41:10.049Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549828/Grouper+custom+template+via+GSH+-+provisionable+groups
---

Input a provisioner config id and get the provisionable groups back. This GSH template has a built in test.

## Configuration

```
grouperGshTemplate.provisionableGroupsWs.allowWsFromNoOwner = true
grouperGshTemplate.provisionableGroupsWs.defaultRunButtonFolderUuidOrName = penn\u003Aetc\u003Atemplates\u003AprovisionableGroups
grouperGshTemplate.provisionableGroupsWs.folderShowOnDescendants = certainFolders
grouperGshTemplate.provisionableGroupsWs.folderShowType = certainFolders
grouperGshTemplate.provisionableGroupsWs.folderUuidToShow = penn\u003Aetc\u003Atemplates\u003AprovisionableGroups
grouperGshTemplate.provisionableGroupsWs.gshTemplate = //
grouperGshTemplate.provisionableGroupsWs.input.0.description = Provisioner config id
grouperGshTemplate.provisionableGroupsWs.input.0.dropdownSqlDatabase = grouper
grouperGshTemplate.provisionableGroupsWs.input.0.dropdownSqlValue = select provisioner_name, provisioner_name from grouper_sync where sync_engine = 'provisioning' order by 1
grouperGshTemplate.provisionableGroupsWs.input.0.dropdownValueFormat = sql
grouperGshTemplate.provisionableGroupsWs.input.0.formElementType = dropdown
grouperGshTemplate.provisionableGroupsWs.input.0.label = Provisioner config id
grouperGshTemplate.provisionableGroupsWs.input.0.name = gsh_input_provisionerConfigId
grouperGshTemplate.provisionableGroupsWs.moreActionsLabel = Provisionable groups
grouperGshTemplate.provisionableGroupsWs.numberOfInputs = 1
grouperGshTemplate.provisionableGroupsWs.runAsType = GrouperSystem
grouperGshTemplate.provisionableGroupsWs.runButtonGroupOrFolder = folder
grouperGshTemplate.provisionableGroupsWs.securityRunType = wheel
grouperGshTemplate.provisionableGroupsWs.showInMoreActions = true
grouperGshTemplate.provisionableGroupsWs.showOnFolders = true
grouperGshTemplate.provisionableGroupsWs.templateDescription = Provisionable groups
grouperGshTemplate.provisionableGroupsWs.templateName = Provisionable groups
grouperGshTemplate.provisionableGroupsWs.templateVersion = V2

```

## GSH template script

```
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecTestOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2test;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2utils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
 
public class Test108provisionableGroupsWs extends GshTemplateV2 {
  
  private static final String TEMPLATE_CONFIG_ID = "provisionableGroupsWs";

  /**
   * output from json is just a list of strings
   *
   */
  public static class WsOutputBean {
 
    private List<String> groupNames = new ArrayList<>();

    
    public List<String> getGroupNames() {
      return groupNames;
    }

    
    public void setGroupNames(List<String> groupNames) {
      this.groupNames = groupNames;
    }

    
  }

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {
 
    
    Map<String, Object> wsInput = gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().getWsInput();
    String provisionerConfigId = null;
    boolean fromWs = false;
    
    // get the input either from UI or WS
    if (GrouperUtil.length(wsInput) > 0) {
      provisionerConfigId = (String)wsInput.get("gsh_input_provisionerConfigId");
      fromWs = true;
    } else {
      provisionerConfigId = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_provisionerConfigId");
    }

    // cannot use UI validations in WS (yet)
    if (StringUtils.isBlank(provisionerConfigId)) {
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addValidationLine("gsh_input_provisionerConfigId", "Provisioner config id is required");
      return;
    }

    // get the provisionable groups
    List<String> groupNames = new GcDbAccess().sql("""
        select gsg.group_name from grouper_sync gs, grouper_sync_group gsg
        where gs.provisioner_name = ?
        and gs.id = gsg.grouper_sync_id
        and gsg.provisionable = 'T' order by 1
        """).addBindVar(provisionerConfigId).selectList(String.class);
    
    WsOutputBean wsOutputBean = new WsOutputBean();
    wsOutputBean.setGroupNames(groupNames);

    if (fromWs) {
    
      // send the outputback in JSON
      GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();     
      
      String json = GrouperUtil.jsonConvertTo(wsOutputBean, false);
      
      Map<String, Object> jsonMap = GrouperUtil.jsonConvertFrom(json, Map.class);
      
      gsh_builtin_gshTemplateOutput.setWsOutput(jsonMap);
    } else {
      
      // for UI just print the first few groups and give a count
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("Found: " + GrouperUtil.length(groupNames) + " groups");
      int count = 0;
      for (String groupName : GrouperUtil.nonNull(groupNames)) {
        gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine(groupName);
        if (++count >= 10) {
          break;
        }
      }
      if (GrouperUtil.length(groupNames) >= 10) {
        gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("...");
      }
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("Provisionable groups GSH template complete");
    }
    

  }

  /**
   * tests allow the test button from the GSH template screen to be used.  
   * start with the work "test", take no args, and return a GshTemplateV2test
   */
  public GshTemplateV2test testProvisionableGroups() {
    return new TestProvisionableGroups();
  }
  
  /**
   * this is the test, just pass in a valid provisioner config id, and get some results
   */
  public class TestProvisionableGroups extends GshTemplateV2test {

    protected void setUp() {
       
      this.setGshTemplateConfigId(TEMPLATE_CONFIG_ID);

      Map<String, String> arguments = new HashMap<String, String>();
      arguments.put("gsh_input_provisionerConfigId", "atlassianCloudConfluence");
      
      this.setGshWsInput(arguments);

      this.assignGshSubjectUsingApp("pennperson", "10021368");
       
      this.setGshExpectValidationError(false);

    }

    @Override
    public void gshCheckResult() {

      WsOutputBean wsOutputBean = GrouperUtil.jsonConvertFromMap((Map)this.getGshTemplateOutput().getWsOutput(), WsOutputBean.class);

      assertTrue(GrouperUtil.length(wsOutputBean.getGroupNames()) > 0);
            
    }

  }
 
  
  public static void main(String[] args) {
    
      
    GrouperStartup.startup();

    // just run a test from the command line to debug
    Test108provisionableGroupsWs test108provisionableGroupsWs = new Test108provisionableGroupsWs();
    GshTemplateExecTestOutput gshTemplateExecTestOutput = null;
  
    
    gshTemplateExecTestOutput = GshTemplateV2utils.gshRunTest(test108provisionableGroupsWs, "testProvisionableGroups");
    
    System.out.println(gshTemplateExecTestOutput.toString());

    System.exit(0);

  }

}
```

## Test the script

## Run from UI

## Run from WS

Request:

PUT [https://server.school.edu/grouper-ws/servicesRest/4.14.0/gshTemplateExec](https://server.school.edu/grouper-ws/servicesRest/4.14.0/gshTemplateExec)

Content-Type: application/json

Authenticate

Body:

```
{
  "WsRestGshTemplateExecRequest":{
    "configId":"provisionableGroupsWs",
    "wsInput":{
        "gsh_input_provisionerConfigId":"atlassianCloudConfluence"
    }
  }
}
```

Response:

200

Body:
