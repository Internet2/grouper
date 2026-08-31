---
title: "Grouper custom template via GSH Internet2 example delete"
space: Grouper
pageId: 28554437
version: 5
lastUpdated: 2026-07-01T05:40:26.935Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554437/Grouper+custom+template+via+GSH+Internet2+example+delete
---

## Delete a working group

Do this when a working group is deprovisioned or just for testing

| Name | Type | Validation | Required? | Description |
| --- | --- | --- | --- | --- |
| gsh_input_workingGroupExtension | String | alphanumeric and dash | required | will be used in the folder and group system names |

## Screen

## Config

```
grouperGshTemplate.deleteWorkingGroup.actAsGroupUUID = 2f5e010d633344798d8fcbfbf05302f4
grouperGshTemplate.deleteWorkingGroup.displayErrorOutput = true
grouperGshTemplate.deleteWorkingGroup.folderShowOnDescendants = certainFolder
grouperGshTemplate.deleteWorkingGroup.folderShowType = certainFolder
grouperGshTemplate.deleteWorkingGroup.folderUuidToShow = 318d11f5325741b49402ccb5728285d2
grouperGshTemplate.deleteWorkingGroup.groupUuidCanRun = edcb2b237f7f482fa4f12fed73e2883b
grouperGshTemplate.deleteWorkingGroup.gshTemplate = 
// Check if working group folder exists\
Stem workingGroupFolder = StemFinder.findByName(gsh_builtin_grouperSession, "ref:incommon-collab:" + gsh_input_workingGroupExtension, false);\
...
grouperGshTemplate.deleteWorkingGroup.input.0.description = The name of the collaboration group folder and other objects.
grouperGshTemplate.deleteWorkingGroup.input.0.label = Working group extention
grouperGshTemplate.deleteWorkingGroup.input.0.maxLength = 64
grouperGshTemplate.deleteWorkingGroup.input.0.name = gsh_input_workingGroupExtension
grouperGshTemplate.deleteWorkingGroup.input.0.required = true
grouperGshTemplate.deleteWorkingGroup.input.0.validationBuiltin = alphaNumericUnderscoreDash
grouperGshTemplate.deleteWorkingGroup.input.0.validationType = builtin
grouperGshTemplate.deleteWorkingGroup.numberOfInputs = 1
grouperGshTemplate.deleteWorkingGroup.runAsType = currentUser
grouperGshTemplate.deleteWorkingGroup.securityRunType = specifiedGroup
grouperGshTemplate.deleteWorkingGroup.showOnFolders = true
grouperGshTemplate.deleteWorkingGroup.templateDescription = Delete a working group and all related groups e.g. sympa, confluence, jira, etc
grouperGshTemplate.deleteWorkingGroup.templateName = Delete working group
```

## GSH script

```

// Check if working group folder exists
Stem workingGroupFolder = StemFinder.findByName(gsh_builtin_grouperSession, "ref:incommon-collab:" + gsh_input_workingGroupExtension, false);
if (workingGroupFolder != null) {
  workingGroupFolder.obliterate(false, false, false);
  gsh_builtin_gshTemplateOutput.addOutputLine("Working group folder obliterated: " + workingGroupFolder.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Working group folder not found: ref:incommon-collab:" + gsh_input_workingGroupExtension);
}
  
Stem sympaFolder = StemFinder.findByName(gsh_builtin_grouperSession, "app:sympa:incommon:" + gsh_input_workingGroupExtension, false);
if (sympaFolder != null) {
  sympaFolder.obliterate(false, false, false);
  gsh_builtin_gshTemplateOutput.addOutputLine("Sympa folder obliterated: " + sympaFolder.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Sympa folder not found: app:sympa:incommon:" + gsh_input_workingGroupExtension);
}
sympaFolder = StemFinder.findByName(gsh_builtin_grouperSession, "app:sympa:internet2:" + gsh_input_workingGroupExtension, false);
if (sympaFolder != null) {
  sympaFolder.obliterate(false, false, false);
  gsh_builtin_gshTemplateOutput.addOutputLine("Sympa folder obliterated: " + sympaFolder.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Sympa folder not found: app:sympa:internet2:" + gsh_input_workingGroupExtension);
}

Stem confluenceFolder = StemFinder.findByName(gsh_builtin_grouperSession, "app:confluence:" + gsh_input_workingGroupExtension, false);
if (confluenceFolder != null) {
  confluenceFolder.obliterate(false, false, false);
  gsh_builtin_gshTemplateOutput.addOutputLine("Confluence folder obliterated: " + confluenceFolder.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Confluence folder not found: app:confluence:" + gsh_input_workingGroupExtension);
}

Stem jiraFolder = StemFinder.findByName(gsh_builtin_grouperSession, "app:jira:" + gsh_input_workingGroupExtension, false);
if (jiraFolder != null) {
  jiraFolder.obliterate(false, false, false);
  gsh_builtin_gshTemplateOutput.addOutputLine("Jira folder obliterated: " + jiraFolder.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Jira folder not found: app:jira:" + gsh_input_workingGroupExtension);
}
  
gsh_builtin_gshTemplateOutput.addOutputLine("Finished executing template: " + gsh_input_workingGroupExtension);
```

## Web service call

curl -H "Content-Type: text/x-json; charset=UTF-8" -d "@./deleteworkinggroup.json" -X POST -u comanage_provision:XXXXXXX [https://grouper.dev.at.internet2.edu/grouper-ws/servicesRest/v2_5_000/gshTemplateExec](https://grouper.dev.at.internet2.edu/grouper-ws/servicesRest/v2_5_000/gshTemplateExec)  
**deleteworkinggroup.json**

| `{`   ```"WsRestGshTemplateExecRequest"``:{`   ```"gshTemplateActAsSubjectLookup"``: {`   ```"subjectSourceId"``:``"ldap"``,`   ```"subjectId"``:``"eisbruch@[at.internet2.edu](http://at.internet2.edu)"`   ```},`   ```"ownerStemLookup"``:{`   ```"stemName"``:``"ref:incommon-collab"`   ```},`   ```"ownerType"``:``"stem"``,`   ```"configId"``:``"deleteWorkingGroup"``,`   ```"inputs"``:[`   ```{`   ```"name"``:``"gsh_input_workingGroupExtension"``,`   ```"value"``:``"test4"`   ```}`   ```]`   ```}`   `}` |
| --- |
