---
title: "Grouper custom template via GSH - loader import / export"
space: Grouper
pageId: 28549874
version: 2
lastUpdated: 2026-07-01T05:41:02.360Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549874/Grouper+custom+template+via+GSH+-+loader+import+export
---

## Summary

Admins should be able to export a loader to JSON, and import into another group or another environment. You can export a loader job from test and import into prod.

## Screens

Export

Import:

Example JSON:

```
{
  "loaderType" : "sql",
  "attributes" : [ {
    "attributeName" : "etc:legacy:attribute:legacyAttribute_grouperLoaderDbName",
    "attributeValue" : "grouper"
  }, {
    "attributeName" : "etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron",
    "attributeValue" : "0 0 6 * * ?"
  }, {
    "attributeName" : "etc:legacy:attribute:legacyAttribute_grouperLoaderQuery",
    "attributeValue" : "select subject_id from grouper_members where subject_id like 'test.%0%' or subject_id like 'test.%1%'"
  }, {
    "attributeName" : "etc:legacy:attribute:legacyAttribute_grouperLoaderScheduleType",
    "attributeValue" : "CRON"
  }, {
    "attributeName" : "etc:legacy:attribute:legacyAttribute_grouperLoaderType",
    "attributeValue" : "SQL_SIMPLE"
  } ]
}
```

## GSH template config

```
grouperGshTemplate.loaderTemplate.defaultRunButtonGroupUuidOrName = chris\u003Atest
grouperGshTemplate.loaderTemplate.displayErrorOutput = true
grouperGshTemplate.loaderTemplate.groupShowType = allGroups
grouperGshTemplate.loaderTemplate.gshTemplate = //
grouperGshTemplate.loaderTemplate.input.0.description = Import or export a template
grouperGshTemplate.loaderTemplate.input.0.dropdownCsvValue = export, import
grouperGshTemplate.loaderTemplate.input.0.formElementType = dropdown
grouperGshTemplate.loaderTemplate.input.0.label = Action
grouperGshTemplate.loaderTemplate.input.0.name = gsh_input_action
grouperGshTemplate.loaderTemplate.input.0.required = true
grouperGshTemplate.loaderTemplate.input.1.description = JSON script to import
grouperGshTemplate.loaderTemplate.input.1.formElementType = textarea
grouperGshTemplate.loaderTemplate.input.1.label = Import script
grouperGshTemplate.loaderTemplate.input.1.maxLength = 10000
grouperGshTemplate.loaderTemplate.input.1.name = gsh_input_importScript
grouperGshTemplate.loaderTemplate.input.1.required = true
grouperGshTemplate.loaderTemplate.input.1.showEl = \u0024{ gsh_input_action == 'import' }
grouperGshTemplate.loaderTemplate.input.1.validationType = none
grouperGshTemplate.loaderTemplate.moreActionsLabel = Loader import / export
grouperGshTemplate.loaderTemplate.numberOfInputs = 2
grouperGshTemplate.loaderTemplate.runAsType = GrouperSystem
grouperGshTemplate.loaderTemplate.runButtonGroupOrFolder = group
grouperGshTemplate.loaderTemplate.securityRunType = wheel
grouperGshTemplate.loaderTemplate.showInMoreActions = true
grouperGshTemplate.loaderTemplate.showOnGroups = true
grouperGshTemplate.loaderTemplate.templateDescription = Export and import a loader job
grouperGshTemplate.loaderTemplate.templateName = Loader import / export
grouperGshTemplate.loaderTemplate.templateVersion = V2

```

Code:

```
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class Test111loaderExportImport extends GshTemplateV2 {
  
  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    // Analyze user access, Analyze user history, Analyze application
    String gsh_input_action = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_action");
    
    String gsh_input_importScript = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_importScript");

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    String groupName = gshTemplateV2input.getGsh_builtin_ownerGroupName();
    
    Group group = GroupFinder.findByName(groupName, true);
    
    boolean actionImport = StringUtils.equals("import", gsh_input_action);
    boolean actionExport = StringUtils.equals("export", gsh_input_action);
    if (!actionImport && !actionExport) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_action", "Action not found");
      return;
    }
    
    boolean isLdapLoader = group.getAttributeDelegate().hasAttributeByName(LoaderLdapUtils.grouperLoaderLdapName());
    
    String legacyAttributeStemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");

    String legacyAttributeGroupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");
    
    String sqlLoaderNameOfAttributeDefName = legacyAttributeStemName + ":" + legacyAttributeGroupTypePrefix + "grouperLoader";
        
    boolean isSqlLoader = group.getAttributeDelegate().hasAttributeByName(sqlLoaderNameOfAttributeDefName);

    String attributeName = null;
    if (isLdapLoader) {
      attributeName = LoaderLdapUtils.grouperLoaderLdapName();
    } else if (isSqlLoader) {
      attributeName = sqlLoaderNameOfAttributeDefName;
    }

    
    if (actionExport) {
      if (!isLdapLoader && !isSqlLoader) {
        gsh_builtin_gshTemplateOutput.addValidationLine("This group is not a SQL or LDAP loader");
        return;
      }
      List<Object[]> attributeNamesValues = new GcDbAccess().sql("""
          select attribute_def_name_name2, value_string from grouper_aval_asn_asn_group_v gaaagv 
          where attribute_def_name_name1 = ?
          and group_name = ? order by 1
          """).addBindVar(attributeName).addBindVar(groupName).selectList(Object[].class);
      ObjectNode containerNode = GrouperUtil.jsonJacksonNode();
      if (isLdapLoader) {
        GrouperUtil.jsonJacksonAssignString(containerNode, "loaderType", "ldap");
      } else if (isSqlLoader) {
        GrouperUtil.jsonJacksonAssignString(containerNode, "loaderType", "sql");
      } else {
        throw new RuntimeException("Not expecting loader type!");
      }
      ArrayNode attributesNode = GrouperUtil.jsonJacksonArrayNode();
      containerNode.set("attributes", attributesNode);
      for (Object[] attributeNameValue : attributeNamesValues) {
        String currentAttributeName = (String)attributeNameValue[0];
        String currentAttributeValue = (String)attributeNameValue[1];

        ObjectNode attributeNameValueNode = GrouperUtil.jsonJacksonNode();
        attributesNode.add(attributeNameValueNode);
        GrouperUtil.jsonJacksonAssignString(attributeNameValueNode, "attributeName", currentAttributeName);
        GrouperUtil.jsonJacksonAssignString(attributeNameValueNode, "attributeValue", currentAttributeValue);
      }
      
      ObjectMapper mapper = new ObjectMapper();
      String indented = null;
      try {
        indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(containerNode);
      } catch (Exception e) {
        throw new RuntimeException("error", e);
      }
      
      gsh_builtin_gshTemplateOutput.addOutputLine("JSON: <pre>" + GrouperUtil.escapeHtml(indented, true) + "</pre>");

      
    } else if (actionImport) {
      if (isLdapLoader || isSqlLoader) {
        gsh_builtin_gshTemplateOutput.addValidationLine("This group is already a loader");
        return;
      }
      //  { "loaderType" : "sql",
      //    "attributes" : [ {
      //      "attributeName" : "etc:legacy:attribute:legacyAttribute_grouperLoaderType",
      //      "attributeValue" : "SQL_SIMPLE"
      //    } ]
      //  }
      JsonNode containerNode = GrouperUtil.jsonJacksonNode(gsh_input_importScript);
      String loaderType = GrouperUtil.jsonJacksonGetString(containerNode, "loaderType");
      AttributeAssign markerAssignment = null;
      if (StringUtils.equals(loaderType, "sql")) {
        markerAssignment = group.getAttributeDelegate().assignAttributeByName(sqlLoaderNameOfAttributeDefName).getAttributeAssign();
      } else if (StringUtils.equals(loaderType, "ldap")) {
        markerAssignment = group.getAttributeDelegate().assignAttributeByName(LoaderLdapUtils.grouperLoaderLdapName()).getAttributeAssign();
      } else {
        throw new RuntimeException("Not expecting loaderType in JSON: '" + loaderType + "'");
      }
      ArrayNode attributesNode = GrouperUtil.jsonJacksonGetArrayNode(containerNode, "attributes");
      for (int i=0; i<attributesNode.size(); i++) {
        JsonNode attributeNode = attributesNode.get(i);
        String currentAttributeName = GrouperUtil.jsonJacksonGetString(attributeNode, "attributeName");
        String currentAttributeValue = GrouperUtil.jsonJacksonGetString(attributeNode, "attributeValue");
        markerAssignment.getAttributeValueDelegate().assignValueString(currentAttributeName, currentAttributeValue);
        
        gsh_builtin_gshTemplateOutput.addOutputLine("Assigned attribute '" + GrouperUtil.extensionFromName(currentAttributeName) + 
            "': '" + GrouperUtil.escapeHtml(currentAttributeValue, true) + "'");
      }
    }
    

    gsh_builtin_gshTemplateOutput.addOutputLine("Success: finished the loader template");

  }

  public static void main(String[] args) {
    
    GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();

    GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Subject subject = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);

        gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(new GshTemplateRuntime());
//        gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().setAuthenticatedSubject(subject);
        gshTemplateV2input.setGsh_builtin_subject(subject);
        gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().setCurrentSubject(subject);
        gshTemplateV2input.setGsh_builtin_ownerGroupName("chris:testLoader");
        GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
        gshTemplateRuntime.setTemplateConfigId("loaderTemplate");
        
        Test111loaderExportImport myGshTemplate = new Test111loaderExportImport();

        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_action", "export");
        
        myGshTemplate.gshRunLogic(gshTemplateV2input, gshTemplateV2output);
        return null;
      }
    });
    System.out.println(GrouperUtil.toStringForLog(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getValidationLines()));
    System.out.println(GrouperUtil.toStringForLog(gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getOutputLines()));
    System.exit(0);
  }

}
```
