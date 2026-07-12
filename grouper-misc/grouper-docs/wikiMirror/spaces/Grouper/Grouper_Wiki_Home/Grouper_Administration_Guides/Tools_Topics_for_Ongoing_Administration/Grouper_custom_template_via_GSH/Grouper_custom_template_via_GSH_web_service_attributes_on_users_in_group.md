---
title: "Grouper custom template via GSH web service attributes on users in group"
space: Grouper
pageId: 28549562
version: 3
lastUpdated: 2026-07-01T05:41:47.292Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549562/Grouper+custom+template+via+GSH+web+service+attributes+on+users+in+group
---

## Requirements

Write a GSH template ws using classname Test132userAttributesOnGroupMembers that takes in two params:

- gsh_input_groupName: group name to check
- gsh_input_nameOfAttributeDef: name of attribute def of attributes on users.
  
  - If this is null just send back all the attribute def names on the user

Make sure the user authenticated to the WS can READ memberships on the group.

Use one SQL query to get all users who are members of the group and all attributes and values directly assigned to those users who are members of the group.  
If gsh_input_nameOfAttributeDef is there then constrain the query to attributes which have that definition by name of attribute def.

Return a json in the ws output with array where the object inside is a user object. the user has subjectId, and name.  
It also has an array of objects which have nameOfAttributeDefName, valueString

## Template config

```
grouperGshTemplate.attributesOnSubjectsInGroup.allowWsFromNoOwner = true
grouperGshTemplate.attributesOnSubjectsInGroup.groupUuidCanRun = penn\u003Aetc\u003Atemplates\u003AattributesOnSubjectsInGroup\u003AattributesOnSubjectsInGroupRunners
grouperGshTemplate.attributesOnSubjectsInGroup.gshTemplate = //
grouperGshTemplate.attributesOnSubjectsInGroup.input.0.description = Group name
grouperGshTemplate.attributesOnSubjectsInGroup.input.0.label = Group name
grouperGshTemplate.attributesOnSubjectsInGroup.input.0.name = gsh_input_groupName
grouperGshTemplate.attributesOnSubjectsInGroup.input.0.required = true
grouperGshTemplate.attributesOnSubjectsInGroup.input.0.validationType = none
grouperGshTemplate.attributesOnSubjectsInGroup.input.1.description = Name of attributeDef.  If not entered include all
grouperGshTemplate.attributesOnSubjectsInGroup.input.1.label = Name of attributeDef
grouperGshTemplate.attributesOnSubjectsInGroup.input.1.name = gsh_input_nameOfAttributeDef
grouperGshTemplate.attributesOnSubjectsInGroup.input.1.validationType = none
grouperGshTemplate.attributesOnSubjectsInGroup.numberOfInputs = 2
grouperGshTemplate.attributesOnSubjectsInGroup.runAsType = GrouperSystem
grouperGshTemplate.attributesOnSubjectsInGroup.securityRunType = specifiedGroup
grouperGshTemplate.attributesOnSubjectsInGroup.templateDescription = See attributes assigned to users in a group
grouperGshTemplate.attributesOnSubjectsInGroup.templateName = Attributes on subjects in group
grouperGshTemplate.attributesOnSubjectsInGroup.templateType = gsh
grouperGshTemplate.attributesOnSubjectsInGroup.templateVersion = V2

```

## AI helped write this GSH template

Using the [GSH agent](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549180/Grouper+AI+public+OpenAI+GSH+agent)

## GSH template source

The output from AI was generally correct but needed to be tweaked a little bit. Note the queries are functional but could also be cleaned up, e.g.

```
select gmlv.subject_id, gm.name, attribute_def_name_name, value_string 
from grouper_aval_asn_member_v gaamv, grouper_memberships_lw_v gmlv, grouper_members gm
where gmlv.group_name = 'test:isc:ait:mchyzer:testAttributes:testAttributeGroup'
and gmlv.list_name = 'members'
and gaamv.member_id = gmlv.member_id and gm.id = gmlv.member_id
-- optional if name of attribute def sent in
and gaamv.name_of_attribute_def = 'a:b'
```

```
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class Test132userAttributesOnGroupMembers extends GshTemplateV2 {

  private static final Log LOG = GrouperUtil.getLog(GshTemplateV2.class);

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    // Get input parameters
    String gsh_input_groupName = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_groupName");
    String gsh_input_nameOfAttributeDef = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_nameOfAttributeDef");

    // Validate group exists
    Group group = GroupFinder.findByName(gsh_input_groupName, false);
    if (group == null) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_groupName", "Group not found: " + gsh_input_groupName);
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      return;
    }

    // Validate READ access
    if (!group.canHavePrivilege(GrouperSession.staticGrouperSession().getSubject(), AccessPrivilege.READ.getName(), false)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_groupName", "User does not have READ access to group: " + gsh_input_groupName);
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      return;
    }

    // Build SQL
    String sql = """
      select gm.subject_id, gm.name, gaamv.attribute_def_name_name, gaamv.value_string
      from grouper_memberships_lw_v gmlv,
           grouper_members gm,
           grouper_aval_asn_member_v gaamv
      where gmlv.group_name = ?
        and gmlv.list_name = 'members'
        and gmlv.subject_source = gm.subject_source
        and gmlv.subject_id = gm.subject_id
        and gm.subject_id = gaamv.subject_id
        and gm.subject_source = gaamv.source_id
    """;

    if (!StringUtils.isBlank(gsh_input_nameOfAttributeDef)) {
      sql += " and gaamv.attribute_def_name_name in (select name from grouper_attribute_def_name where attribute_def_id in (select id from grouper_attribute_def where name = ?))";
    }

    GcDbAccess dbAccess = new GcDbAccess().sql(sql).addBindVar(gsh_input_groupName);
    if (!StringUtils.isBlank(gsh_input_nameOfAttributeDef)) {
      dbAccess.addBindVar(gsh_input_nameOfAttributeDef);
    }

    List<Object[]> results = dbAccess.selectList(Object[].class);

    // Build JSON
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, ObjectNode> userMap = new LinkedHashMap<>();

    for (Object[] row : results) {
      String subjectId = (String) row[0];
      String name = (String) row[1];
      String attrName = (String) row[2];
      String attrValue = (String) row[3];

      ObjectNode userNode = userMap.get(subjectId);
      if (userNode == null) {
        userNode = objectMapper.createObjectNode();
        userNode.put("subjectId", subjectId);
        userNode.put("name", name);
        userNode.putArray("attributes");
        userMap.put(subjectId, userNode);
      }

      ObjectNode attrNode = objectMapper.createObjectNode();
      attrNode.put("nameOfAttributeDefName", attrName);
      attrNode.put("valueString", attrValue);
      ((ArrayNode) userNode.get("attributes")).add(attrNode);
    }

    ArrayNode userArray = objectMapper.createArrayNode();
    for (ObjectNode userNode : userMap.values()) {
      userArray.add(userNode);
    }

    resultNode.set("users", userArray);
    // Return JsonNode directly in WS output
    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().setWsOutput(resultNode);
  }
}

```

## How to call this WS

Request

```
POST https://grouperws.school.edu/grouperWs/servicesRest/2.6.0/gshTemplateExec
Content-Type: application/json
Authorization: abc123

{
  "WsRestGshTemplateExecRequest":{
    "configId":"attributesOnSubjectsInGroup",
    "wsInput":{
        "gsh_input_groupName": "test:isc:ait:mchyzer:testAttributes:testAttributeGroup"
    }
  }
}
```

Response:

```
200
x-grouper-resultcode: SUCCESS
x-grouper-resultcode2: NONE
x-grouper-success: T

{
  "WsGshTemplateExecResult": {
    "resultMetadata": {
      "resultCode": "SUCCESS",
      "resultMessage": "Success for: clientVersion: 2.6.0, configId: attributesOnSubjectsInGroup, ownerType: null , inputs: null\n, actAsSubject: null, paramNames: \n, params: null",
      "success": "T"
    },
    "responseMetadata": {
      "millis": "2828",
      "serverVersion": "5.17.2"
    },
    "transaction": true,
    "gshValidationLines": [],
    "gshOutputLines": [],
    "wsOutput": {
      "users": [{
        "subjectId": "10021368",
        "name": "Chris Hyzer",
        "attributes": [{
          "nameOfAttributeDefName": "test:isc:ait:mchyzer:testAttributes:testAttr1_1",
          "valueString": "firstVal"
        }, {
          "nameOfAttributeDefName": "test:isc:ait:mchyzer:testAttributes:testAttr2_1",
          "valueString": "someVal2"
        }]
      }, {
        "subjectId": "89505485",
        "name": "Katherine R Wilson",
        "attributes": [{
          "nameOfAttributeDefName": "test:isc:ait:mchyzer:testAttributes:testAttr1_2",
          "valueString": "val2a"
        }, {
          "nameOfAttributeDefName": "test:isc:ait:mchyzer:testAttributes:testAttr2_2",
          "valueString": "val2b"
        }]
      }]
    }
  }
}
```
