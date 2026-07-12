---
title: "Grouper custom template via GSH membership trace - V2"
space: Grouper
pageId: 28547583
version: 7
lastUpdated: 2026-02-27T20:03:13.133Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547583/Grouper+custom+template+via+GSH+membership+trace+-+V2
---

This example is for v4+, not yet tested on v6. It is still a work-in-progress.

The web services API does not currently provide a method to do a membership trace. This template allows a user to perform a **membership** trace (not privilege membership, yet) for a given subject in a particular group. It is intended to be called via WS (if you're in the UI, you can just to the trace there), so it only returns JSON and not UI output lines.

If a user has more than one path to group membership, there will be multiple entries in the JSON array returned by the template.

This currently works for our (SFU's) needs, but it could be expanded to do privilege traces, customizing the `stringPath` delimiter, displaying composite group information, etc. In our environment this is intended to be used in reporting scripts by Grouper admins who have full root access, so it doesn't consider any privileges on the groups in the path trace.

## Example

```shell
$ curl -X "POST" "https://yourgrouper.yourschool.edu/grouper-ws/servicesRest/v4_0_000/gshTemplateExec" \
     -H 'Content-Type: application/json' \
     -u 'user:pass' \
     -d $'{
  "WsRestGshTemplateExecRequest": {
    "ownerStemLookup": {
      "stemName": "etc:templates"
    },
    "configId": "membershipTrace",
    "ownerType": "stem",
    "inputs": [
      {
        "name": "gsh_input_subjectId",
        "value": "grahamb"
      },
      {
        "name": "gsh_input_groupName",
        "value": "resource:app:vpn:vpn-users"
      }
    ],
    "actAsSubjectLookup": {
      "subjectId": "grahamb"
    }
  }
}' \
| jq -r .WsGshTemplateExecResult.wsOutput

```

```json
{
  "WsGshTemplateExecResult": {
    "resultMetadata": {
      "resultCode": "SUCCESS",
      "resultMessage": "Success for: clientVersion: 4.0.0, configId: membershipTrace, ownerType: stem , inputs: Array size: 2: [0]: edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput@12e96496\n[1]: edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput@7681d6fe\n\n, actAsSubject: WsSubjectLookup[subjectId=grahamb], paramNames: \n, params: null",
      "success": "T"
    },
    "responseMetadata": {
      "millis": "124",
      "serverVersion": "4.20.3"
    },
    "transaction": true,
    "gshValidationLines": [],
    "gshOutputLines": [],
    "wsOutput": "[ {\n  \"type\" : \"effective\",\n  \"path\" : [ \"basis:role:hap-employees\", \"ref:role:employees\", \"ref:role:staff-faculty\", \"resource:app:vpn:role-includes\", \"resource:app:vpn:includes-rollup\", \"resource:app:vpn:vpn-users\" ],\n  \"stringPath\" : \"basis:role:hap-employees → ref:role:employees → ref:role:staff-faculty → resource:app:vpn:role-includes → resource:app:vpn:includes-rollup → resource:app:vpn:vpn-users\"\n} ]"
  }
}
```

```json
[
  {
    "type": "effective",
    "path": [
      "basis:role:hap-employees",
      "ref:role:employees",
      "ref:role:staff-faculty",
      "resource:app:vpn:role-includes",
      "resource:app:vpn:includes-rollup",
      "resource:app:vpn:vpn-users"
    ],
    "stringPath": "basis:role:hap-employees → ref:role:employees → ref:role:staff-faculty → resource:app:vpn:role-includes → resource:app:vpn:includes-rollup → resource:app:vpn:vpn-users"
  }
]

```

## Config

```text
grouperGshTemplate.membershipTrace.folderShowOnDescendants = certainFolders
grouperGshTemplate.membershipTrace.folderShowType = certainFolders
grouperGshTemplate.membershipTrace.folderUuidToShow = etc\u003Atemplates
grouperGshTemplate.membershipTrace.groupUuidCanRun = etc\u003Asysadmingroup
grouperGshTemplate.membershipTrace.input.0.description = Subject ID
grouperGshTemplate.membershipTrace.input.0.formElementType = text
grouperGshTemplate.membershipTrace.input.0.label = Subject ID
grouperGshTemplate.membershipTrace.input.0.name = gsh_input_subjectId
grouperGshTemplate.membershipTrace.input.0.required = true
grouperGshTemplate.membershipTrace.input.0.type = string
grouperGshTemplate.membershipTrace.input.0.validationType = none
grouperGshTemplate.membershipTrace.input.1.description = Group Name
grouperGshTemplate.membershipTrace.input.1.formElementType = text
grouperGshTemplate.membershipTrace.input.1.label = Group Name
grouperGshTemplate.membershipTrace.input.1.name = gsh_input_groupName
grouperGshTemplate.membershipTrace.input.1.required = false
grouperGshTemplate.membershipTrace.input.1.type = string
grouperGshTemplate.membershipTrace.input.1.validationType = none
grouperGshTemplate.membershipTrace.numberOfInputs = 2
grouperGshTemplate.membershipTrace.runAsType = currentUser
grouperGshTemplate.membershipTrace.securityRunType = specifiedGroup
grouperGshTemplate.membershipTrace.showOnFolders = true
grouperGshTemplate.membershipTrace.templateDescription = Runs a membership trace on the specified subject and group
grouperGshTemplate.membershipTrace.templateName = Membership Trace
grouperGshTemplate.membershipTrace.templateType = gsh
grouperGshTemplate.membershipTrace.templateVersion = V2
```

## GSH Script

```java
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.grouper.membership.MembershipPathGroup;
import edu.internet2.middleware.grouper.membership.MembershipPath;
import edu.internet2.middleware.grouper.membership.MembershipPathNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// you need a class that extends GshTemplateV2
public class MembershipTraceTemplate extends GshTemplateV2 {
  
  // implement the gshRunLogic method, this is what is called when the template executes
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {
    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
    String gsh_builtin_ownerStemName = gshTemplateV2input.getGsh_builtin_ownerStemName();
    String gsh_input_subjectId = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_subjectId");
    String gsh_input_groupName = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_groupName");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject subject = SubjectFinder.findById(gsh_input_subjectId);
    Group group = GroupFinder.findByName(grouperSession, gsh_input_groupName, true);
    Field field = FieldFinder.find("members");
    MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyze(group, subject, field);

    List<Map<String, Object>> allPaths = [];

    for (MembershipPath path : membershipPathGroup.getMembershipPaths()) {
      List<String> pathNodes = [];
      List<MembershipPathNode> nodes = path.getMembershipPathNodes();

      for (int i = 0; i < nodes.size(); i++) {
        MembershipPathNode node = nodes.get(i);
        Group nodeGroup = node.getOwnerGroup();
        String nodeName = nodeGroup.getName();
        pathNodes.add(nodeName);
      }

      Map<String, Object> pathMap = new LinkedHashMap<>();
      pathMap.put("type", path.getMembershipType().toString().toLowerCase());
      pathMap.put("path", pathNodes);
      pathMap.put("stringPath", pathNodes.join(" → "));
      allPaths.add(pathMap);
    }

    ObjectMapper mapper = new ObjectMapper();
    String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(allPaths);

    gsh_builtin_gshTemplateOutput.setWsOutput(jsonOutput);

  }
}

gsh_builtin_gshTemplateRuntime.assignGshTemplateV2(new MembershipTraceTemplate());
```
