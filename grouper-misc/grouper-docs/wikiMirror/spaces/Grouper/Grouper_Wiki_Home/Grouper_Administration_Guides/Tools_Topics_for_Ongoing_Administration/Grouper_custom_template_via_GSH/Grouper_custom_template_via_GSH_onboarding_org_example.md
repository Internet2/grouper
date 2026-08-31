---
title: "Grouper custom template via GSH onboarding org example"
space: Grouper
pageId: 28548894
version: 3
lastUpdated: 2026-07-01T05:43:18.426Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548894/Grouper+custom+template+via+GSH+onboarding+org+example
---

This video shows this example

[https://www.youtube.com/watch?v=2mYt8l19vas](https://www.youtube.com/watch?v=2mYt8l19vas)

Menu

## Template UI screen

## Config

```
grouperGshTemplate.onboardO365twoStepOrg.folderShowOnDescendants = certainFolderAndDescendants
grouperGshTemplate.onboardO365twoStepOrg.folderShowType = certainFolder
grouperGshTemplate.onboardO365twoStepOrg.folderUuidToShow = 2cc5737d4e2541178e30cd0bda8917fa
grouperGshTemplate.onboardO365twoStepOrg.groupUuidCanRun = dfa31e4e45f94fa1b1edce420f15405b
grouperGshTemplate.onboardO365twoStepOrg.input.0.description = Alpha-numeric (starting with lower case letter) org name, e.g. businessServices
grouperGshTemplate.onboardO365twoStepOrg.input.0.label = Org name
grouperGshTemplate.onboardO365twoStepOrg.input.0.maxLength = 50
grouperGshTemplate.onboardO365twoStepOrg.input.0.name = gsh_input_orgName
grouperGshTemplate.onboardO365twoStepOrg.input.0.required = true
grouperGshTemplate.onboardO365twoStepOrg.input.0.validationMessage = Alpha-numeric (starting with lower case letter) org name, e.g. businessServices
grouperGshTemplate.onboardO365twoStepOrg.input.0.validationRegex = ^[a-z][a-zA-Z0-9]+$
grouperGshTemplate.onboardO365twoStepOrg.input.0.validationType = regex
grouperGshTemplate.onboardO365twoStepOrg.input.1.description = Require date in format: yyyy/mm/dd
grouperGshTemplate.onboardO365twoStepOrg.input.1.label = Require date
grouperGshTemplate.onboardO365twoStepOrg.input.1.name = gsh_input_requireDate
grouperGshTemplate.onboardO365twoStepOrg.input.1.required = true
grouperGshTemplate.onboardO365twoStepOrg.input.1.validationMessage = Require date in format: yyyy/mm/dd
grouperGshTemplate.onboardO365twoStepOrg.input.1.validationRegex = ^[0-9]{4}\/[0-1][0-9]\/[0-3][0-9]$
grouperGshTemplate.onboardO365twoStepOrg.input.1.validationType = regex
grouperGshTemplate.onboardO365twoStepOrg.input.2.description = Comma separated group names (ID paths) to represent the population of the organization to be able to be enroll in O365/TwoStep
grouperGshTemplate.onboardO365twoStepOrg.input.2.label = Reference group names
grouperGshTemplate.onboardO365twoStepOrg.input.2.maxLength = 2000
grouperGshTemplate.onboardO365twoStepOrg.input.2.name = gsh_input_refGroupNames
grouperGshTemplate.onboardO365twoStepOrg.input.2.validationType = none
grouperGshTemplate.onboardO365twoStepOrg.input.3.description = Comma separated list of admin pennkeys
grouperGshTemplate.onboardO365twoStepOrg.input.3.label = Admin pennkeys
grouperGshTemplate.onboardO365twoStepOrg.input.3.maxLength = 500
grouperGshTemplate.onboardO365twoStepOrg.input.3.name = gsh_input_adminPennkeys
grouperGshTemplate.onboardO365twoStepOrg.input.3.validationType = none
grouperGshTemplate.onboardO365twoStepOrg.moreActionsLabel = Onboard O365/TwoStep org
grouperGshTemplate.onboardO365twoStepOrg.numberOfInputs = 4
grouperGshTemplate.onboardO365twoStepOrg.runAsType = GrouperSystem
grouperGshTemplate.onboardO365twoStepOrg.securityRunType = specifiedGroup
grouperGshTemplate.onboardO365twoStepOrg.showInMoreActions = true
grouperGshTemplate.onboardO365twoStepOrg.showOnFolders = true
grouperGshTemplate.onboardO365twoStepOrg.templateDescription = Run this template to onboard a new O365/TwoStep organization.  This will create and link together the to be enrolled, required, excluded, and admin groups.
grouperGshTemplate.onboardO365twoStepOrg.templateName = Onboard O365/TwoStep org

```

## GSH script

```
import java.util.*;
import edu.internet2.middleware.grouper.cfg.*;
import edu.internet2.middleware.grouper.misc.*;
import edu.internet2.middleware.grouper.util.*;
import edu.internet2.middleware.grouper.app.attestation.*;
import edu.internet2.middleware.grouper.attr.assign.*;
import edu.internet2.middleware.grouper.attr.finder.*;
import edu.internet2.middleware.grouper.attr.value.*;
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.subject.*;
import org.apache.commons.lang3.*;

String orgNameCap = StringUtils.capitalize(gsh_input_orgName);

// build email as we do tasks
StringBuilder emailBody = new StringBuilder();
Set<String> emailTos = new HashSet<String>();

// email to the user who performed this so they can support it
String emailOfUserRunningTemplate = GrouperEmail.retrieveEmailAddress(gsh_builtin_subject);
if (!StringUtils.isBlank(emailOfUserRunningTemplate)) {
  emailTos.add(emailOfUserRunningTemplate);
}

emailBody.append("Your org '" + gsh_input_orgName + "' has been onboarded to O365/twoStep.\nYour users can self enroll.\n");
emailBody.append("On " + gsh_input_requireDate + ", when you are ready, you can add your 'toBeRequired' group to be a member of your 'required' group.\n\n");

// validate first
// Check if admins are resolvable
boolean hasAdminPennkeys = !StringUtils.isBlank(gsh_input_adminPennkeys);

// convert pennkeys from comma separate string to an array
String[] adminPennkeys = null;
// convert pennkeys into subjects
Subject[] adminSubjects = null;
if (hasAdminPennkeys) {
  adminPennkeys = GrouperUtil.splitTrim(gsh_input_adminPennkeys, ",");
  adminSubjects = new Subject[adminPennkeys.length];
  for (int i=0;i<adminPennkeys.length;i++) {
    Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(adminPennkeys[i], "pennperson", false);
    if (subject == null) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_adminPennkeys",
        "Error: admin pennkey cannot be found '" + adminPennkeys[i] + "'!");
    }
    adminSubjects[i] = subject;
    if (!new MembershipFinder().addGroup("penn:community:employeeOrContractorInTwoStep").addSubject(adminSubjects[i]).hasMembership()) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_adminPennkeys",
        "Error: admin pennkey '" + adminPennkeys[i] + "' is not in group: penn:community:employeeOrContractorInTwoStep!");
    }
    String emailOfAdmin = GrouperEmail.retrieveEmailAddress(adminSubjects[i]);
    if (!StringUtils.isBlank(emailOfAdmin)) {
      emailTos.add(emailOfAdmin);
    }

  }
}

// convert ref groups from comma separate string to an array
boolean hasRefGroups = !StringUtils.isBlank(gsh_input_refGroupNames);
String[] refGroupNames = null;
Group[] refGroups = null;
if (hasRefGroups) {
  refGroupNames = GrouperUtil.splitTrim(gsh_input_refGroupNames, ",");
  refGroups = new Group[refGroupNames.length];
  for (int i=0;i<refGroupNames.length;i++) {
    Group group = GroupFinder.findByName(gsh_builtin_grouperSession, refGroupNames[i], false);
    if (group == null) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_refGroupNames",
        "Error: ref group cannot be found '" + refGroupNames[i] + "'!");
    }
    refGroups[i] = group;
  }
}
  
// Do not proceed is there is an error
if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
  gsh_builtin_gshTemplateOutput.assignIsError(true);
  GrouperUtil.gshReturn();
}
  
// Create or find admins group, e.g. penn:isc:ait:apps:twoFactor:twoFactorSecurity:twoStepAdmins:twoStepAuditAndComplianceAdmins
GroupSave adminsGroupSave = new GroupSave().assignName("penn:isc:ait:apps:twoFactor:twoFactorSecurity:twoStepAdmins:twoStep" + orgNameCap + "Admins");
Group adminsGroup = adminsGroupSave.save();
if (adminsGroupSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Admins group existed: " + adminsGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Admins group created: " + adminsGroup.getName());
}
emailBody.append("Admins group url:\n" + GrouperConfig.getGrouperUiUrl(false) + "grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + adminsGroup.getId() + "\n\n");

// Add admins to it
if (hasAdminPennkeys) {
  for (int i=0;i<adminSubjects.length;i++) {
    if (adminsGroup.addMember(adminSubjects[i], false)) {
      gsh_builtin_gshTemplateOutput.addOutputLine("Added admin: " + adminSubjects[i].getDescription() + " to group: " + adminsGroup.getName());
    } else {
      gsh_builtin_gshTemplateOutput.addOutputLine("Admin: " + adminSubjects[i].getDescription() + " was already in group: " + adminsGroup.getName());
    }
  }
}

// create require group, e.g. penn:isc:ait:apps:O365:twoStepProd:o365schoolsCenters:o365twoStepAuditAndCompliance
GroupSave requireGroupSave = new GroupSave().assignName("penn:isc:ait:apps:O365:twoStepProd:o365schoolsCenters:o365twoStep" + orgNameCap);
Group requireGroup = requireGroupSave.save();
if (requireGroupSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep org require group existed: " + requireGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep org require group created: " + requireGroup.getName());
}
emailBody.append("O365/twoStep org require group url:\n" + GrouperConfig.getGrouperUiUrl(false) + "grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + requireGroup.getId() + "\n\n");

// add require group to the overall required group
Group overallRequireGroup = GroupFinder.findByName(gsh_builtin_grouperSession, "penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod_required", true);
if (overallRequireGroup.addMember(requireGroup.toSubject(), false)) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Added O365/twoStep org require group to overall require group: " + overallRequireGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep org require group was already in overall require group: " + overallRequireGroup.getName());
}

// create to be required group, e.g. penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepGroupsToBeRequired:o365twoStepToBeRequiredAuditAndCompliance
GroupSave toBeRequiredGroupSave = new GroupSave().assignName("penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepGroupsToBeRequired:o365twoStepToBeRequired" + orgNameCap);
Group toBeRequiredGroup = toBeRequiredGroupSave.save();
if (toBeRequiredGroupSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep to be required group existed: " + toBeRequiredGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep to be required group created: " + toBeRequiredGroup.getName());
}
emailBody.append("O365/twoStep to be required group url:\n" + GrouperConfig.getGrouperUiUrl(false) + "grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + toBeRequiredGroup.getId() + "\n\n");

// add each ref group to the toBeRequired group
if (hasRefGroups) {
  for (int i=0;i<refGroups.length;i++) {
    if (toBeRequiredGroup.addMember(refGroups[i].toSubject(), false)) {
      gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep ref group: " + refGroups[i].getName() + " added to the to be required group: " + toBeRequiredGroup.getName());
    } else {
      gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep ref group: " + refGroups[i].getName() + " was already a member of the to be required group: " + toBeRequiredGroup.getName());
    }
  }
}

// add attribute and value to the toBeRequired group of date to be require
AttributeAssign requireDateAttributeAssign = new AttributeAssignToGroupSave().assignGroup(toBeRequiredGroup).assignNameOfAttributeDefName("penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepRequiredDate").save();
new AttributeAssignValueSave().assignAttributeAssign(requireDateAttributeAssign).assignValue(gsh_input_requireDate).save();
gsh_builtin_gshTemplateOutput.addOutputLine("The to be required group was assigned an attribute: penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepRequiredDate with value: " + gsh_input_requireDate);

// add the new org to be required group to the overall to be required group  
Group overallToBeRequiredGroup = GroupFinder.findByName(gsh_builtin_grouperSession, "penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepToBeRequired", true);
if (overallToBeRequiredGroup.addMember(toBeRequiredGroup.toSubject(), false)) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Added O365/twoStep org to be required group to overall to be required group: " + overallToBeRequiredGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep org to be required group was already in overall to be required group: " + overallToBeRequiredGroup.getName());
}

// add security group as member: penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepAllowedToAdmin
Group overallSecurityGroup = GroupFinder.findByName(gsh_builtin_grouperSession, "penn:isc:ait:apps:O365:twoStepProd:simpleEnrollUnenroll:o365twoStepAllowedToAdmin", true);
if (overallSecurityGroup.addMember(adminsGroup.toSubject(), false)) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Added org admin group to overall O365/twoStep admin group: " + overallSecurityGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep org admin group was already in overall admin group: " + overallSecurityGroup.getName());
}

// create exclude group: penn:isc:ait:apps:O365:twoStepProd:o365schoolCentersExclude:o365twoStepAuditAndComplianceExclude
GroupSave excludeGroupSave = new GroupSave().assignName("penn:isc:ait:apps:O365:twoStepProd:o365schoolCentersExclude:o365twoStep" + orgNameCap + "Exclude");
Group excludeGroup = excludeGroupSave.save();
if (excludeGroupSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep exclude group existed: " + excludeGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep exclude group created: " + excludeGroup.getName());
}
emailBody.append("O365/twoStep exclude group url:\n" + GrouperConfig.getGrouperUiUrl(false) + "grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + excludeGroup.getId() + "\n\n");

// grant UPDATE to penn:isc:ait:apps:twoFactor:twoFactorSecurity:twoStepAdmins:twoStepAuditAndComplianceAdmins on 
// penn:isc:ait:apps:O365:twoStepProd:o365schoolCentersExclude:o365twoStepAuditAndComplianceExclude
PrivilegeGroupSave excludePrivilegeGroupSave = new PrivilegeGroupSave().assignGroup(excludeGroup).assignFieldName("updaters").assignSubject(adminsGroup.toSubject());
excludePrivilegeGroupSave.save();
if (excludePrivilegeGroupSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep exclude group already had: " + adminsGroup.getName() + " as updaters");
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep exclude group added: " + adminsGroup.getName() + " as updaters");
}

// grant READ to penn:isc:ait:apps:twoFactor:twoFactorSecurity:twoStepAdmins:twoStepAuditAndComplianceAdmins on 
// penn:isc:ait:apps:O365:twoStepProd:o365schoolCentersExclude:o365twoStepAuditAndComplianceExclude
excludePrivilegeGroupSave = new PrivilegeGroupSave().assignGroup(excludeGroup).assignFieldName("readers").assignSubject(adminsGroup.toSubject());
excludePrivilegeGroupSave.save();
if (excludePrivilegeGroupSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep exclude group already had: " + adminsGroup.getName() + " as readers");
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep exclude group added: " + adminsGroup.getName() + " as readers");
}

// add exclude group as member of: penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod_policy_deny
Group overallExcludeGroup = GroupFinder.findByName(gsh_builtin_grouperSession, "penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod_policy_deny", true);
if (overallExcludeGroup.addMember(excludeGroup.toSubject(), false)) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Added org exclude group to overall O365/twoStep exclude group: " + overallExcludeGroup.getName());
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("O365/twoStep org exclude group was already in overall exclude group: " + overallExcludeGroup.getName());
}
emailBody.append("Self-serve url:\n" + GrouperConfig.getGrouperUiUrl(false) + "grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=61bcaad67d57438ab1fea11c426c2f64\n\n");
emailBody.append("Open a ticket with ISC client care for support.\nThanks!\nISC");

// send email
if (emailTos.size() > 0) {
  String emailTo = StringUtils.join(emailTos.iterator(), ",");
  new GrouperEmail().setTo(emailTo).setSubject("O365/twoStep org onboard for " + gsh_input_orgName).setBody(emailBody.toString()).send();
  gsh_builtin_gshTemplateOutput.addOutputLine("Sent email with links and instructions to: " + emailTo); 
}

// done!
gsh_builtin_gshTemplateOutput.addOutputLine("Finished running O365/twoStep template for org: " + gsh_input_orgName);
```
