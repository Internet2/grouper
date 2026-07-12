---
title: "Copy of Grouper Training - Administration - Lesson: GSH templates"
space: Grouper
pageId: 28548878
version: 1
lastUpdated: 2025-04-09T00:28:16.471Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548878/Copy+of+Grouper+Training+-+Administration+-+Lesson+GSH+templates
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Create access control group for who can run the template

Go to app folder

Create new app appTemplateRunner (Run template "Application")

Make a policy group (from template) in the policy folder: appTemplateRunner, group name: appTemplateRunner (require fac/staff)

Add kjenkins to allow group and fac/staff

Should see in policy group

In the app folder, grant VIEW to app:appTemplateRunner:service:policy:appTemplateRunner

Once added, you should see that kjenkins has indirect VIEW.

## Create custom application template

As banderson: Miscellaneous → GSH templates → Add GSH template

Config ID: appTemplate

Template: GshTemplateConfiguration

Template type: GSH

Template version: V1

Show on folders: True

Folder show type: Certain folders

Folder uuids or names to show: app

Folder show on descendants: Certain folders

Run template owner type: folder

Run template folder name: app

Security run type: Specified group

Group uuid or name: app:appTemplateRunner:service:policy:appTemplateRunner

Run as type: GrouperSystem

Template name: App template

Template description: Creates app in institution specific way

Show in actions dropdown: true

Template label in actions dropdown: App template

Number of inputs: 2

Input 1: gsh_input_appName

Input 1 label: App name

Input 1 description: App name is alphanumeric app folder display extension

Input 1 validation type: Built-in

Input 1 builtin validation: Alpha-numeric and underscores

Input 2: gsh_input_subjectIds

Input 2 label: Subject IDs

Input 2 description: Comma separated net ID's, e.g. kjenkins, banderson

Input 2 form element type: Textarea

Input 2 validation type: Regex

Input 2 validation regex: ^[a-z0-9, ]+$

Input 2 validation message: Comma separated lower case alphanumeric net IDs only

GSH script

```
//
////uncomment to compile in eclipse (and last line)
//// these are standard imports, can be commented out in script but needed in eclipse
//import edu.internet2.middleware.grouper.*;
//import edu.internet2.middleware.grouper.app.gsh.*;
//import edu.internet2.middleware.grouper.privs.*;
//import edu.internet2.middleware.grouper.misc.*;
//import edu.internet2.middleware.grouper.util.*;
//import edu.internet2.middleware.subject.*;
//
import edu.internet2.middleware.grouper.Stem.Scope;
//
//
//public class Test15 {
//
//  public static void main(String[] args) {
//    
//    GrouperStartup.startup();
//    
//    String gsh_input_appName = "wiki"; 
//    String gsh_input_subjectIds = "jsmith, banderson";
//    
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
//    Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("jsmith", "eduLDAP", true);
//    GrouperGroovyRuntime grouperGroovyRuntime = new GrouperGroovyRuntime();
//    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();

    // 1. app should not already exist
    String appStemName = "app:" + gsh_input_appName;
    Stem appFolder = StemFinder.findByName(gsh_builtin_grouperSession, appStemName, false);
    if (appFolder != null) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_appName",
          "Error: App already exists '" + gsh_input_appName + "'!");
    }
    
    // 2. resolve subjects, they need to resolve
    String[] subjectIdArray = GrouperUtil.nonNull(GrouperUtil.splitTrim(gsh_input_subjectIds, ","), String.class);
    Subject[] subjects = new Subject[GrouperUtil.length(subjectIdArray)];

    for (int i=0;i<GrouperUtil.length(subjectIdArray);i++) {
      Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectIdArray[i], "eduLDAP", false);
      if (subject == null) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_subjectIds",
          "Error: SubjectId cannot be found '" + subjectIdArray[i] + "'!");
      }
      subjects[i] = subject;
      
    }

    // 3. Do not proceed is there is an error
    if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      GrouperUtil.gshReturn();
    }

    // 4. Create stem
    appFolder = new StemSave().assignName(appStemName).save();
    gsh_builtin_gshTemplateOutput.addOutputLine("App folder created: " + appStemName);

    // 5. Create admin group
    Group adminGroup = new GroupSave().assignName(appStemName + ":" + gsh_input_appName + "Admins").save();
    gsh_builtin_gshTemplateOutput.addOutputLine("Admin group created: " + adminGroup.getName());

    // 6. Add users
    for (Subject subject : GrouperUtil.nonNull(subjects, Subject.class)) {
      adminGroup.addMember(subject, false);
      gsh_builtin_gshTemplateOutput.addOutputLine("Added admin: " + subject.getDescription());
    }

    // 7. add inherited privileges
    PrivilegeGroupInheritanceSave privilegeGroupInheritanceSave = new PrivilegeGroupInheritanceSave().assignStem(appFolder).assignStemScope(Scope.SUB);
    privilegeGroupInheritanceSave.assignSubject(adminGroup.toSubject()).addPrivilege(AccessPrivilege.ADMIN).save();
    gsh_builtin_gshTemplateOutput.addOutputLine("Assigned inherited group ADMIN priv on: " + appFolder.getName() + " to group: " + adminGroup.getName());
    
    // 8. add attestation
    AttestationGroupSave attestationGroupSave = new AttestationGroupSave().assignGroup(adminGroup);
    attestationGroupSave.assignAttestationType(AttestationType.group).assignDaysUntilRecertify(30).assignMarkAsAttested(true).assignSendEmail(true).save();
    gsh_builtin_gshTemplateOutput.addOutputLine("Configured attestation on group: " + adminGroup.getName());

    // 9. success message
    gsh_builtin_gshTemplateOutput.addOutputLine("Finished running institution app template");

//  }
//
//}

```

## Run script

Log in as kjenkins

Navigate to app folder

Run "App Template"

App name: wiki2

Subject IDs: jsmith, dbrown

## View audits

As banderson, view audits for *app:wiki2:wiki2Admins*
