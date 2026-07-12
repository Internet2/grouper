---
title: "Grouper custom template via GSH inherited privileges WS example"
space: Grouper
pageId: 28548929
version: 3
lastUpdated: 2026-07-01T05:43:10.163Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548929/Grouper+custom+template+via+GSH+inherited+privileges+WS+example
---

As of 2.5.52 there is no inherited privilege web service, so we can expose that functionality securely via GSH template. Eventually Grouper should add this WS.

## Sample WS call

```
POST https://grouperws.server.institution.edu/grouper-ws/servicesRest/v2_5_000/gshTemplateExec
Content-Type: application/json
Authorization: Basic sdf876sdf876sdf87s6df87s6df

{
  "WsRestGshTemplateExecRequest":{
    "ownerStemLookup":{
      "stemName":"penn:etc:templates:inheritedPrivilegeSave"
    },
    "ownerType":"stem",
    "configId":"inheritedPrivilegeSave",
    "inputs":[
      {
        "name":"gsh_input_subjectIdOrIdentifier",
        "value":"mchyzer"   (netid or employeeid or group name)
      },
      {
        "name":"gsh_input_folderIdOrName",
        "value":"test:testFolder:testInheritanceWs"
      },    
      {
        "name":"gsh_input_subjectSourceId",
        "value":"pennperson"   (subject source ids, in Penn's case: pennperson, servPrinc, g:gsa for groups)
      },    
      {
        "name":"gsh_input_privilegeNames",
        "value":"admin"    (e.g. admin, update, read, view, optin, optout, groupAttrRead, groupAttrUpdate, create, stemAdmin, stemAttrRead, stemAttrUpdate, attrAdmin, attrDefAttrRead, attrDefAttrUpdate, attrOptin, attrOptout, attrRead, attrUpdate, attrView)
      }
    ]
  }
}

```

Response

```
    Status Code: 200 OK
    content-length: 717
    content-type: application/json;charset=UTF-8
    date: Tue, 13 Jul 2021 16:42:15 GMT
    server: Apache/2.4.6 (CentOS) OpenSSL/1.0.2k-fips
    strict-transport-security: max-age=15768000
    x-firefox-spdy: h2
    x-grouper-resultcode: SUCCESS
    x-grouper-resultcode2: NONE
    x-grouper-success: T

{
  "WsGshTemplateExecResult":{
    "resultMetadata":{
      "success":"T",
      "resultCode":"SUCCESS",
      "resultMessage":"Success for: clientVersion: 2.5.0, configId: inheritedPrivilegeSave, ownerType: stem , inputs: Array size: 4: [0]: edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput@52ef11a8\n[1]: edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput@1689df2d\n[2]: edu.internet2.middlew...\n, actAsSubject: null, paramNames: \n, params: null"
    },
    "gshOutputLines":[
      {
        "messageType":"success",
        "text":"Success group privileges INSERT"
      },
      {
        "messageType":"success",
        "text":"Finished inherited privilege template"
      }
    ],
    "responseMetadata":{
      "serverVersion":"2.5.52",
      "millis":"4957"
    },
    "gshValidationLines":[
      
    ],
    "transaction":true
  }
}

```

## UI

## Configuration

## Script

```
////uncomment to compile in eclipse (and last line)
//// these are standard imports, can be commented out in script but needed in eclipse
//import java.util.HashSet;
//import java.util.Set;
//
//import org.apache.commons.lang3.StringUtils;
//
//import edu.internet2.middleware.grouper.GrouperSession;
//import edu.internet2.middleware.grouper.PrivilegeAttributeDefInheritanceSave;
//import edu.internet2.middleware.grouper.PrivilegeGroupInheritanceSave;
//import edu.internet2.middleware.grouper.PrivilegeStemInheritanceSave;
//import edu.internet2.middleware.grouper.Stem;
//import edu.internet2.middleware.grouper.StemFinder;
//import edu.internet2.middleware.grouper.SubjectFinder;
//import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
//import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
//import edu.internet2.middleware.grouper.misc.GrouperStartup;
//import edu.internet2.middleware.grouper.misc.SaveResultType;
//import edu.internet2.middleware.grouper.privs.NamingPrivilege;
//import edu.internet2.middleware.grouper.privs.Privilege;
//import edu.internet2.middleware.grouper.util.GrouperUtil;
//import edu.internet2.middleware.subject.Subject;
//
//
//public class Test17 {
//
//  public static void main(String[] args) {
//    
//    GrouperStartup.startup();
//    
//    String gsh_input_subjectIdOrIdentifier = "mchyzer"; 
//    String gsh_input_folderIdOrName = "test:testFolder";
//    String gsh_input_subjectSourceId = "g:gsa";
//    String gsh_input_privilegeNames = "UPDATE, READ, CREATE, STEM_ATTR_READ, ATTR_READ, ATTR_UPDATE";
//    
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
//    Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("jsmith", "ldap", true);
//    GrouperGroovyRuntime grouperGroovyRuntime = new GrouperGroovyRuntime();
//    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();

    // stay on screen
    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

    Stem stem = StemFinder.findByUuid(gsh_builtin_grouperSession, gsh_input_folderIdOrName, false);
    stem = stem != null ? stem : StemFinder.findByName(gsh_builtin_grouperSession, gsh_input_folderIdOrName, false);
    
    // folder must exist
    if (stem == null) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_folderIdOrName",
          "Error: Cannot find folder by ID or name: '" + gsh_input_folderIdOrName + "'!");
    }

    // subject to assign
    Subject subject = null;
    if (StringUtils.isBlank(gsh_input_subjectSourceId)) {
      
      subject = SubjectFinder.findByIdOrIdentifier(gsh_input_subjectIdOrIdentifier, false);
      
    } else {
      
      subject = SubjectFinder.findByIdOrIdentifierAndSource(gsh_input_subjectIdOrIdentifier, gsh_input_subjectSourceId, false);

    }

    // subject to assign must exist
    if (subject == null) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_subjectIdOrIdentifier",
          "Error: Cannot find subject: '" + gsh_input_subjectIdOrIdentifier + "'!");
    }

    // calling user must have admin on folder
    if (stem != null && !stem.canHavePrivilege(gsh_builtin_subject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
      
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_folderIdOrName",
          "Error: User does not have stem ADMIN on folder: '" + gsh_input_folderIdOrName + "'!");
      
    }
    
    Set<String> privilegeNameSet = GrouperUtil.splitTrimToSet(gsh_input_privilegeNames, ",");
    Set<Privilege> groupPrivileges = new HashSet<Privilege>();
    Set<Privilege> stemPrivileges = new HashSet<Privilege>();
    Set<Privilege> attributePrivileges = new HashSet<Privilege>();

    for (String privilegeName : privilegeNameSet) {
      Privilege privilege = Privilege.listToPriv(privilegeName, false);
      if (privilege == null) {
        privilege = Privilege.getInstance(privilegeName, false);
      }
      if (privilege == null) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_privilegeNames",
            "Error: Cannot find privilege name: '" + privilegeName + "'!");
      } else {
        if (privilege.isAccess()) {
          groupPrivileges.add(privilege);
        } else if (privilege.isNaming()) {
          stemPrivileges.add(privilege);
        } else if (privilege.isAttributeDef()) {
          attributePrivileges.add(privilege); 
        } else {
          throw new RuntimeException("Not expecting privilege: '" + privilegeName + "'");
        }
      }
    }
    
    // Do not proceed is there is an error
    if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      GrouperUtil.gshReturn();
    }

    if (groupPrivileges.size() > 0) {
      PrivilegeGroupInheritanceSave privilegeGroupInheritanceSave = new PrivilegeGroupInheritanceSave().assignStem(stem).assignSubject(subject);
      for (Privilege privilege : groupPrivileges) {
        privilegeGroupInheritanceSave.addPrivilege(privilege);
      }
      SaveResultType saveResultType = privilegeGroupInheritanceSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Success group privileges " + saveResultType.name());
    }
    
    if (stemPrivileges.size() > 0) {
      PrivilegeStemInheritanceSave privilegeStemInheritanceSave = new PrivilegeStemInheritanceSave().assignStem(stem).assignSubject(subject);
      for (Privilege privilege : stemPrivileges) {
        privilegeStemInheritanceSave.addPrivilege(privilege);
      }
      SaveResultType saveResultType = privilegeStemInheritanceSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Success stem privileges " + saveResultType.name());
    }
    
    if (attributePrivileges.size() > 0) {
      PrivilegeAttributeDefInheritanceSave privilegeAttributeDefInheritanceSave = new PrivilegeAttributeDefInheritanceSave().assignStem(stem).assignSubject(subject);
      for (Privilege privilege : attributePrivileges) {
        privilegeAttributeDefInheritanceSave.addPrivilege(privilege);
      }
      SaveResultType saveResultType = privilegeAttributeDefInheritanceSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Success attribute def privileges " + saveResultType.name());
    }
    
    // Success message
    gsh_builtin_gshTemplateOutput.addOutputLine("Finished inherited privilege template");

//  }
//
//}
```

## Configuration export (minus script)

```
grouperGshTemplate.inheritedPrivilegeSave.displayErrorOutput = true
grouperGshTemplate.inheritedPrivilegeSave.folderShowOnDescendants = certainFolderAndDescendants
grouperGshTemplate.inheritedPrivilegeSave.folderShowType = certainFolder
grouperGshTemplate.inheritedPrivilegeSave.folderUuidToShow = 611b324f71ad43b5a6d808171e1d39be
grouperGshTemplate.inheritedPrivilegeSave.groupUuidCanRun = d268474cceee45d19b7daa4e4a2c66fb
grouperGshTemplate.inheritedPrivilegeSave.gshTemplate = //
grouperGshTemplate.inheritedPrivilegeSave.input.0.description = Subject ID or identifier assign the inherited privileges to.  Could be a group name (recommended) or uuid, pennid, or pennkey
grouperGshTemplate.inheritedPrivilegeSave.input.0.index = 10
grouperGshTemplate.inheritedPrivilegeSave.input.0.label = Subject ID or identifier
grouperGshTemplate.inheritedPrivilegeSave.input.0.maxLength = 100
grouperGshTemplate.inheritedPrivilegeSave.input.0.name = gsh_input_subjectIdOrIdentifier
grouperGshTemplate.inheritedPrivilegeSave.input.0.required = true
grouperGshTemplate.inheritedPrivilegeSave.input.0.validationType = none
grouperGshTemplate.inheritedPrivilegeSave.input.1.description = Folder ID or name to assign the inherited privileges to.  User must have ADMIN on the folder
grouperGshTemplate.inheritedPrivilegeSave.input.1.index = 30
grouperGshTemplate.inheritedPrivilegeSave.input.1.label = Folder ID or name
grouperGshTemplate.inheritedPrivilegeSave.input.1.maxLength = 100
grouperGshTemplate.inheritedPrivilegeSave.input.1.name = gsh_input_folderIdOrName
grouperGshTemplate.inheritedPrivilegeSave.input.1.required = true
grouperGshTemplate.inheritedPrivilegeSave.input.1.validationType = none
grouperGshTemplate.inheritedPrivilegeSave.input.2.description = Subject source ID is the source for the subject, e.g. pennperson, servPrinc, or g:gsa (groups)
grouperGshTemplate.inheritedPrivilegeSave.input.2.dropdownCsvValue = pennperson, servPrinc, g:gsa
grouperGshTemplate.inheritedPrivilegeSave.input.2.dropdownValueFormat = csv
grouperGshTemplate.inheritedPrivilegeSave.input.2.formElementType = dropdown
grouperGshTemplate.inheritedPrivilegeSave.input.2.index = 20
grouperGshTemplate.inheritedPrivilegeSave.input.2.label = Subject source ID
grouperGshTemplate.inheritedPrivilegeSave.input.2.name = gsh_input_subjectSourceId
grouperGshTemplate.inheritedPrivilegeSave.input.3.description = Privilege names (comma separated) on groups, folders, or attributes.  e.g. admin, update, read, view, optin, optout, groupAttrRead, groupAttrUpdate, create, stemAdmin, stemAttrRead, stemAttrUpdate, attrAdmin, attrDefAttrRead, attrDefAttrUpdate, attrOptin, attrOptout, attrRead, attrUpdate, attrView
grouperGshTemplate.inheritedPrivilegeSave.input.3.index = 40
grouperGshTemplate.inheritedPrivilegeSave.input.3.label = Privilege names
grouperGshTemplate.inheritedPrivilegeSave.input.3.name = gsh_input_privilegeNames
grouperGshTemplate.inheritedPrivilegeSave.input.3.required = true
grouperGshTemplate.inheritedPrivilegeSave.input.3.validationMessage = Letters, comma, whitespace are only allowed characters
grouperGshTemplate.inheritedPrivilegeSave.input.3.validationRegex = ^[A-Z0-9a-z ,]+$
grouperGshTemplate.inheritedPrivilegeSave.input.3.validationType = regex
grouperGshTemplate.inheritedPrivilegeSave.moreActionsLabel = Inherited privilege save
grouperGshTemplate.inheritedPrivilegeSave.numberOfInputs = 4
grouperGshTemplate.inheritedPrivilegeSave.runAsType = currentUser
grouperGshTemplate.inheritedPrivilegeSave.securityRunType = specifiedGroup
grouperGshTemplate.inheritedPrivilegeSave.showInMoreActions = true
grouperGshTemplate.inheritedPrivilegeSave.showOnFolders = true
grouperGshTemplate.inheritedPrivilegeSave.templateDescription = Assign inherited privileges to a folder
grouperGshTemplate.inheritedPrivilegeSave.templateName = Inherited privilege save

```
