---
title: "Grouper custom template via GSH impersonate example"
space: Grouper
pageId: 28547944
version: 10
lastUpdated: 2026-07-01T05:45:42.236Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547944/Grouper+custom+template+via+GSH+impersonate+example
---

## Description

This [custom template](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544720/Grouper+custom+template+via+GSH) sets up a structure so a project team can manage who can impersonate others in our test IdP, and who they can impersonate as.

The IdP expects groups in the form of netIds who can be impersonated, and a list of users inside who can impersonate them. A loader job will convert from the manageable form, to the cartesian product form that the IdP expects.

The structure in Grouper that is created by the template

Here is our login screen if the user is in the test IdP, is able to impersonate other users, and is a current employee:

## Run the template

Anyone in the group who is allowed to run the template (SSO admins) will see this in the impersonate folder or subfolders

### Validation

### Success

### Email

## Tasks

The GSH template performs these tasks

1. Build an email for the SuperAdmins so they have instructions of how to do certain tasks
2. Also email the SSO admin who ran the template so they have a record
3. Validate that SuperAdmins are employees
4. Add the SuperAdmins to the email list
5. Create a project folder
6. Create or find super admin group. This is a couple people who manage the admins, and who attest all the groups
7. If the Super Admin group was just created, add a rule to remove members who are no longer employees
8. Add superadmin users to the superadmin group.
9. SuperAdmins group can manage itself
10. Create or find admin group. The admin group can manage the impersonators and who can be impersonated
11. If we just created the admin group, add a rule that removes members if not employees (see #7)
12. Add superadmin users to the admin group. Note we are not adding the superadmin group as a member, just the individual users, so they can be loosely coupled, and so the rules work correctly
13. SuperAdmins group can manage Admins (see #9)
14. Create or find impersonator group. These users can act as certain other users
15. If we just created this, add a rule that removes members if not employees
16. Admins group can manage impersonators
17. Add impersonation group to the overall allowed to impersonate group (see #5)
18. Create or find impersonateAs group (who the impersonators can test as) (see #5)
19. Admins group can manage impersonateAs (see #16)
20. Add to grouper.properties that the super admins group can receive email
21. Add attestation to the super admins group to the project folder
22. Send the email (see email example above)

## Inputs

| Input | Example | Description |
| --- | --- | --- |
| gsh_input_projectName | wiki | Will be the folder name and be used in naming other Grouper objects |
| gsh_input_superAdminPennkeys | jsmith, 12345678 | Comma separated netIds or employeeIds who will be "super admins" who will have delegated control of impersonation for their project |

## Config

Regex for camel case label alphanumeric starts with lower

```
^[a-z][a-zA-Z0-9]{1,49}$
```

Regex for comma separated numbers or lower, spaces allowed

```
^[ ,a-z0-9]{2,100}$
```

## GSH script

```
import java.util.*;
import edu.internet2.middleware.grouper.cfg.*;
import edu.internet2.middleware.grouper.misc.*;
import edu.internet2.middleware.grouper.rules.*;
import edu.internet2.middleware.grouper.util.*;
import edu.internet2.middleware.grouper.app.attestation.*;
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
//// comma separated pennkeys or pennids
//String gsh_input_superAdminPennkeys = null;
//
//Subject gsh_builtin_subject = null;
//GshTemplateOutput gsh_builtin_gshTemplateOutput = null;
//GrouperSession gsh_builtin_grouperSession = null;

// if gsh_input_projectName is ngss, then projectNameCap is Ngss
String projectNameCap = StringUtils.capitalize(gsh_input_projectName);

// 1. build email as we do tasks
StringBuilder emailBody = new StringBuilder();
Set<String> emailTos = new HashSet<String>();

// 2. email to the user who performed this so they can support it
String emailOfUserRunningTemplate = GrouperEmail.retrieveEmailAddress(gsh_builtin_subject);
if (!StringUtils.isBlank(emailOfUserRunningTemplate)) {
  emailTos.add(emailOfUserRunningTemplate);
}

emailBody.append("You are the super admin of " + projectNameCap + " SSO impersonation.\n");
emailBody.append("This feature allows users in non-prod to act as other users\n");
emailBody.append("This is a very serious feature which must be treated with attention\n");
emailBody.append("Auditing is affected during impersonation, so this must be only used for testing\n");
emailBody.append("If you need more than a few dozen impersonators or impersonatees, please get approval from the weblogin team\n");
emailBody.append("The impersonators and imperonsateAs people are manually managed groups by you, and must be reviewed (attested) monthly\n\n");

// validate first
// Check if admins are resolvable
boolean hasSuperAdminPennkeys = !StringUtils.isBlank(gsh_input_superAdminPennkeys);

// for rules
Group employeeGroup = GroupFinder.findByName(gsh_builtin_grouperSession, "penn:community:employeeOrContractorIncludingUphs", true);

// convert pennkeys from comma separate string to an array
String[] superAdminPennkeys = null;
// convert pennkeys into subjects
Subject[] superAdminSubjects = null;
if (hasSuperAdminPennkeys) {
  superAdminPennkeys = GrouperUtil.splitTrim(gsh_input_superAdminPennkeys, ",");
  superAdminSubjects = new Subject[superAdminPennkeys.length];
  for (int i=0;i<superAdminPennkeys.length;i++) {
    Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(superAdminPennkeys[i], "pennperson", false);
    if (subject == null) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_superAdminPennkeys",
        "Error: superAdmin pennkey cannot be found '" + superAdminPennkeys[i] + "'!");
      continue;
    }
    superAdminSubjects[i] = subject;
   
    // 3. validate that all super admins are employees
    if (!new MembershipFinder().addGroup(employeeGroup).addSubject(superAdminSubjects[i]).hasMembership()) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_superAdminPennkeys",
        "Error: superAdmin pennkey '" + superAdminPennkeys[i] + "' is not in group: " + employeeGroup.getName() + "!");
    }
    String emailOfSuperAdmin = GrouperEmail.retrieveEmailAddress(superAdminSubjects[i]);

    // 4. email the super admins a list of links to manage their groups
    if (!StringUtils.isBlank(emailOfSuperAdmin)) {
      emailTos.add(emailOfSuperAdmin);
    }
 
  }
}

// Do not proceed is there is an error
if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
  gsh_builtin_gshTemplateOutput.assignIsError(true);
  GrouperUtil.gshReturn();
}

String projectStemName = "penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:" + gsh_input_projectName;

// 5. Create project folder if not exist
StemSave projectFolderSave = new StemSave().assignName(projectStemName).assignDisplayExtension(gsh_input_projectName + " impersonation").assignDescription("Folder holds groups for " + gsh_input_projectName + " impersonation in WebLogin");
Stem projectFolder = projectFolderSave.save();
if (projectFolderSave.getSaveResultType() == SaveResultType.INSERT) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Project folder created: " + projectStemName);
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Project folder existed: " + projectStemName);
}

// 6. Create or find super admin group, e.g. penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:ngss:impersonationNgssSuperAdmins
GroupSave superAdminsGroupSave = new GroupSave().assignName(projectStemName + ":impersonation" + projectNameCap + "SuperAdmins").assignDescription("SuperAdmins group manages SuperAdmins, Admins, impersonators, and impersonatees for " + gsh_input_projectName + " impersonation in WebLogin");
Group superAdminsGroup = superAdminsGroupSave.save();
if (superAdminsGroupSave.getSaveResultType() == SaveResultType.INSERT) {
  gsh_builtin_gshTemplateOutput.addOutputLine("SuperAdmins group created: " + superAdminsGroup.getName());
  
  // 7. since we just created this, add a rule that removes members if not employees
  RuleApi.groupIntersection(gsh_builtin_grouperSession.getSubject(), superAdminsGroup, employeeGroup);
  gsh_builtin_gshTemplateOutput.addOutputLine("Rule added on SuperAdmins group to require membership in: " + employeeGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("SuperAdmins group existed: " + superAdminsGroup.getName());
}
emailBody.append("SuperAdmins group (manages admins) url:\n" + GrouperConfig.getGrouperUiUrl(false) + "grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + superAdminsGroup.getId() + "\n\n");
emailBody.append("Note: if a member of the SuperAdmin group, Admin group, or impersonator group is no longer an employee at Penn they will be automatically removed.\n\n");

// 8. Add superadmin users to the superadmin group
if (hasSuperAdminPennkeys) {
  for (int i=0;i<superAdminSubjects.length;i++) {
    MembershipSave membershipSave = new MembershipSave().assignGroup(superAdminsGroup).assignSubject(superAdminSubjects[i]);
    membershipSave.save();
    if (membershipSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
      gsh_builtin_gshTemplateOutput.addOutputLine("SuperAdmin: " + superAdminSubjects[i].getDescription() + " was already in SuperAdmin group: " + superAdminsGroup.getName());
    } else {
      gsh_builtin_gshTemplateOutput.addOutputLine("SuperAdmin: " + superAdminSubjects[i].getDescription() + " was added to SuperAdmin group: " + superAdminsGroup.getName());
    }
  }
}

// 9. SuperAdmins group can manage itself
new PrivilegeGroupSave().assignGroup(superAdminsGroup).assignFieldName("updaters").assignSubject(superAdminsGroup.toSubject()).save();
new PrivilegeGroupSave().assignGroup(superAdminsGroup).assignFieldName("readers").assignSubject(superAdminsGroup.toSubject()).save();
gsh_builtin_gshTemplateOutput.addOutputLine("SuperAdmins group can manage its memberships");

// remove from superadmins if no longer employee
Subject grouperSystem = SubjectFinder.findRootSubject();

// 10. Create or find admin group, e.g. penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:ngss:impersonationNgssAdmins
GroupSave adminsGroupSave = new GroupSave().assignName(projectStemName + ":impersonation" + projectNameCap + "Admins").assignDescription("Admins group manages impersonators, and impersonatees for " + gsh_input_projectName + " impersonation in WebLogin");
Group adminsGroup = adminsGroupSave.save();
if (adminsGroupSave.getSaveResultType() == SaveResultType.INSERT) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Admins group existed: " + adminsGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Admins group created: " + adminsGroup.getName());
  
  // 11. since we just created this, add a rule that removes members if not employees
  RuleApi.groupIntersection(grouperSystem, adminsGroup, employeeGroup);
  gsh_builtin_gshTemplateOutput.addOutputLine("Rule added on Admins group to require membership in: " + employeeGroup.getName());

}
emailBody.append("Admins group (manages impersonation) url:\n" + GrouperConfig.getGrouperUiUrl(false) + "grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + adminsGroup.getId() + "\n\n");

// 12. Add superadmin users to the admin group
if (hasSuperAdminPennkeys) {
  for (int i=0;i<superAdminSubjects.length;i++) {
    MembershipSave membershipSave = new MembershipSave().assignGroup(adminsGroup).assignSubject(superAdminSubjects[i]);
    membershipSave.save();
    if (membershipSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
      gsh_builtin_gshTemplateOutput.addOutputLine("SuperAdmin: " + superAdminSubjects[i].getDescription() + " was already in Admin group: " + adminsGroup.getName());
    } else {
      gsh_builtin_gshTemplateOutput.addOutputLine("SuperAdmin: " + superAdminSubjects[i].getDescription() + " was added to Admin group: " + adminsGroup.getName());
    }
  }
}

// 13. SuperAdmins group can manage admins
new PrivilegeGroupSave().assignGroup(adminsGroup).assignFieldName("updaters").assignSubject(superAdminsGroup.toSubject()).save();
new PrivilegeGroupSave().assignGroup(adminsGroup).assignFieldName("readers").assignSubject(superAdminsGroup.toSubject()).save();
gsh_builtin_gshTemplateOutput.addOutputLine("SuperAdmins group can manage admin memberships");

// 14. Create or find impersonator group, e.g. penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:ngss:canImpersonate
GroupSave impersonatorGroupSave = new GroupSave().assignName(projectStemName + ":canImpersonate").assignDisplayExtension(gsh_input_projectName + " canImpersonate").assignDescription("Members in this group (if they are employees) can impersonate test users for " + gsh_input_projectName + " in WebLogin");
Group impersonatorGroup = impersonatorGroupSave.save();
if (impersonatorGroupSave.getSaveResultType() == SaveResultType.INSERT) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Impersonator group created: " + impersonatorGroup.getName());
  
  // 15. since we just created this, add a rule that removes members if not employees
  RuleApi.groupIntersection(grouperSystem, impersonatorGroup, employeeGroup);
  gsh_builtin_gshTemplateOutput.addOutputLine("Rule added on impersonator group to require membership in: " + employeeGroup.getName());
  
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Impersonator group existed: " + impersonatorGroup.getName());
}
emailBody.append("Impersonator group (can impersonate) url:\n" + GrouperConfig.getGrouperUiUrl(false) + "grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + impersonatorGroup.getId() + "\n\n");

// 16. Admins group can manage impersonators
new PrivilegeGroupSave().assignGroup(impersonatorGroup).assignFieldName("updaters").assignSubject(adminsGroup.toSubject()).save();
new PrivilegeGroupSave().assignGroup(impersonatorGroup).assignFieldName("readers").assignSubject(adminsGroup.toSubject()).save();
gsh_builtin_gshTemplateOutput.addOutputLine("Admins group can manage impersonatorGroup memberships");

// 17. Add impersonation group to the overall allowed to impersonate group
Group overallAllowedToImpersonateGroup = GroupFinder.findByName(gsh_builtin_grouperSession, "penn:isc:ts:iam:weblogin:service:policy:impersonation:allowedToImpersonate_allow", true);
MembershipSave overallAllowedToImpersonateMembershipSave = new MembershipSave().assignGroup(overallAllowedToImpersonateGroup).assignSubject(impersonatorGroup.toSubject());
overallAllowedToImpersonateMembershipSave.save();
if (overallAllowedToImpersonateMembershipSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("ImpersonatorGroup was already in OverallAllowedToImpersonate group: " + overallAllowedToImpersonateGroup.getName());
} else {
   gsh_builtin_gshTemplateOutput.addOutputLine("ImpersonatorGroup was added to OverallAllowedToImpersonate group: " + overallAllowedToImpersonateGroup.getName());
}

// 18. Create or find impersonateAs group, e.g. penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:ngss:canImpersonateAs
GroupSave impersonateAsGroupSave = new GroupSave().assignName(projectStemName + ":canImpersonateAs").assignDisplayExtension(gsh_input_projectName + " impersonateAs").assignDescription("Members in this group are who the impersonators can act as for " + gsh_input_projectName + " in WebLogin");
Group impersonateAsGroup = impersonateAsGroupSave.save();
if (impersonateAsGroupSave.getSaveResultType() == SaveResultType.INSERT) {
  gsh_builtin_gshTemplateOutput.addOutputLine("ImpersonateAs group created: " + impersonateAsGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("ImpersonateAs group existed: " + impersonateAsGroup.getName());
}
emailBody.append("ImpersonateAs group (who the impersonators can act as) url:\n" + GrouperConfig.getGrouperUiUrl(false) + "grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + impersonateAsGroup.getId() + "\n\n");

// 19. Admins group can manage impersonateAs
new PrivilegeGroupSave().assignGroup(impersonateAsGroup).assignFieldName("updaters").assignSubject(adminsGroup.toSubject()).save();
new PrivilegeGroupSave().assignGroup(impersonateAsGroup).assignFieldName("readers").assignSubject(adminsGroup.toSubject()).save();
gsh_builtin_gshTemplateOutput.addOutputLine("Admins group can manage impersonateAsGroup memberships");

// SuperAdmins group should attest, whoever in group gets emails
String impersonateAttestationEmailAddress = superAdminsGroup.getName() + "@grouper";

// 20. Add to grouper.properties that the super admins group can receive email
GrouperEmail.addAllowEmailToGroup(impersonateAttestationEmailAddress);

// 21. Add attestation to the super admins group to the project folder
AttestationStemSave attestationStemSave = new AttestationStemSave().assignStem(projectFolder).assignDaysUntilRecertify(30).addEmailAddress(impersonateAttestationEmailAddress).assignSendEmail(true).assignAttestationType(AttestationType.group).assignMarkAsAttested(true);
attestationStemSave.save();
if (attestationStemSave.getSaveResultType() == SaveResultType.INSERT) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Attestation was assigned: " + projectFolder.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Attestation was already assigned: " + projectFolder.getName());
}

// 22. send email
if (emailTos.size() > 0) {
  String emailTo = StringUtils.join(emailTos.iterator(), ",");
  new GrouperEmail().setTo(emailTo).setSubject("WebLogin impersonation for " + gsh_input_projectName).setBody(emailBody.toString()).send();
  gsh_builtin_gshTemplateOutput.addOutputLine("Sent email with links and instructions to: " + emailTo);
}
 
// done!
gsh_builtin_gshTemplateOutput.addOutputLine("Finished running impersonation template for project: " + gsh_input_projectName);
```

## Loader

This has nothing to do with the GSH template, but to get the impersonation working with our Shib IDP, we want a list of groups where the group name is the netId to impersonate as, and the members can impersonate as that user.

The input is a list of project folders with groups of impersonators and who they can impersonate as.

The output is a list of who they can impersonate as

SQL list of groups loader query

```
select group_name, subject_id, subject_source_id from penn_impersonate_v

create or replace
view penngrouper.penn_impersonate_v as
select
distinct gmlv_impersonate.subject_id,
'pennperson' as subject_source_id,
'penn:isc:ts:iam:weblogin:service:policy:impersonation:targets:canImpersonate_' || gm_impersonate_as.subject_identifier0 as group_name
from
grouper_memberships_lw_v gmlv_impersonate,
grouper_memberships_lw_v gmlv_impersonate_as,
grouper_members gm_impersonate_as,
grouper_stems gs,
grouper_stems gs_parent
where
gs_parent.name = 'penn:isc:ts:iam:weblogin:service:policy:impersonation:projects'
and gs.parent_stem = gs_parent.id
and gmlv_impersonate.list_name = 'members'
and gmlv_impersonate_as.list_name = 'members'
and gmlv_impersonate.subject_source = 'pennperson'
and gmlv_impersonate_as.subject_source = 'pennperson'
and gmlv_impersonate_as.member_id = gm_impersonate_as.id
and gmlv_impersonate.group_name = (('penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:' || gs.extension) || ':canImpersonate')
and gmlv_impersonate_as.group_name = (('penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:' || gs.extension) || ':canImpersonateAs')
and gmlv_impersonate.group_name like 'penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:%'
and gmlv_impersonate_as.group_name like 'penn:isc:ts:iam:weblogin:service:policy:impersonation:projects:%'
and gm_impersonate_as.subject_identifier0 is not null
order by
3, 1;
```
