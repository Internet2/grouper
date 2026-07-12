---
title: "Grouper custom template via GSH impersonate delete example"
space: Grouper
pageId: 28549137
version: 3
lastUpdated: 2026-07-01T05:42:45.175Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549137/Grouper+custom+template+via+GSH+impersonate+delete+example
---

## Description

This template removes a project team from the impersonation feature.

## Run the template

Anyone in the group who is allowed to run the template (SSO admins) will see this in the impersonate folder or subfolders

### Input

### Success

## Tasks

The GSH template performs these tasks

1. Remove the project folder (and all subobjects)
2. Remove the configuration that allows Grouper to email the Super Admin group

## Inputs

| Input | Example | Description |
| --- | --- | --- |
| gsh_input_projectName | wiki | Folder extension to delete |

## Config

Regex for camel case label alphanumeric starts with lower

| `^[a-z][a-zA-Z0-``9``]{``1``,``49``}$` |
| --- |

## GSH script

```
import java.util.*;
import edu.internet2.middleware.grouper.cfg.*;
import edu.internet2.middleware.grouper.misc.*;
import edu.internet2.middleware.grouper.util.*;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.attr.assign.*;
import edu.internet2.middleware.grouper.attr.finder.*;
import edu.internet2.middleware.grouper.attr.value.*;
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.subject.*;
import org.apache.commons.lang3.*;

//// uncomment to compile in eclipse
//// input of project name, alphanumeric, start with lower.
//String gsh_input_projectName = null;
//
//Subject gsh_builtin_subject = null;
//GshTemplateOutput gsh_builtin_gshTemplateOutput = null;
//GrouperSession gsh_builtin_grouperSession = null;

// if gsh_input_projectName is ngss, then projectNameCap is Ngss
String projectNameCap = StringUtils.capitalize(gsh_input_projectName);

String projectStemName = "penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:" + gsh_input_projectName;

// 1. Obliterate project folder
StemSave projectFolderSave = new StemSave().assignName(projectStemName).assignSaveMode(SaveMode.DELETE);
projectFolderSave.save();
if (projectFolderSave.getSaveResultType() == SaveResultType.DELETE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Project folder deleted: " + projectStemName);
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Project folder did not exist: " + projectStemName);
}

String superAdminGroupName = projectStemName + ":impersonation" + projectNameCap + "SuperAdmins";

String impersonateAttestationEmailAddress = superAdminGroupName + "@grouper";

// 2. Remove from grouper.properties that the super admins group can receive email
GrouperEmail.removeAllowEmailToGroup(impersonateAttestationEmailAddress);
gsh_builtin_gshTemplateOutput.addOutputLine("Removed the configuration to allow emailing the " + gsh_input_projectName +  " SuperAdmins");

// done!
gsh_builtin_gshTemplateOutput.addOutputLine("Finished removing impersonation project: " + gsh_input_projectName);
```
