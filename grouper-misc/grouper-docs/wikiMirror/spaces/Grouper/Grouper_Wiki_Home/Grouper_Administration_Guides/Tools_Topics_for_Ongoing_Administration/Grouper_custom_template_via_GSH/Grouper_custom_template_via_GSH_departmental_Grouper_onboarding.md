---
title: "Grouper custom template via GSH departmental Grouper onboarding"
space: Grouper
pageId: 28549899
version: 1
lastUpdated: 2024-10-04T18:44:19.846Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549899/Grouper+custom+template+via+GSH+departmental+Grouper+onboarding
---

## Summary

At Simon Fraser University, we onboard individual departments to Grouper after some initial training and needs-assessment. When we onboard a department, we create a standard set of folders and groups, and set privileges on those objects. This template facilitates onboarding departments in a standard way so that the right steps are taken every time.

At a high level, this template does the following:

- Creates a folder for the department's reference groups under `ref:dept` for the department (e.g. `ref:dept:beekeeping` )
- Creates a folder for the department's policy groups under `resource:dept` (e.g. `resource:dept:beekeeping` )
- Optionally (but almost always) creates a folder for the department's Active Directory groups. At SFU, we treat AD as a **resource**, so these live in `resource:app:ADSFU:SFUGroups` (e.g. `resource:app:ADSFU:SFUGroups:beekeeping` ). Anything in this folder will be pushed into our AD under the `SFUGroups/beekeeping` OU. Our sync system assumes that Grouper is the system-of-record for these groups, so the AD groups will always mirror the backing Grouper groups.
- Creates a group under that folder called `grouper-admins` (e.g. `ref:dept:beekeeping:grouper-admins` )
- Assigns ADMIN privileges (both direct and inherited) on the folders and groups created above to the departmental `grouper-admins` group.
- Adds the departmental `grouper-admins` to a couple of other groups:
  
  - `etc:privilege:sfuDeptReaders` - a group that has READ access to all departmental reference groups (to facilitate group sharing)
  - `ref:grouper:grouper-admins` - a rollup reference group of all departmental grouper admins. This group is a member of a policy group that controls access to the Grouper UI (we only allow onboarded departmental technical staff access to Grouper) - `resource:app:grouper:grouper-ui-access`

## Screens

The template is configured so that it is visible in the the **Folder actions** menu on the **Root** folder.

When run, the template collects information, such as the department's human-readable name (e.g. School of Beekeeping), the name to use for the folders (e.g. `beekeeping` ), a comma-separated list of subject IDs that will be added to the department's `grouper-admins` group, and a toggle to create the AD group folder.

The template prints its output to the screen as it proceeds with its tasks:

## GSH Template Config

## grouper.properties

```
grouperGshTemplate.onboardDepartment.actAsGroupUUID = etc\u003Asysadmingroup
grouperGshTemplate.onboardDepartment.enabled = true
grouperGshTemplate.onboardDepartment.folderShowOnDescendants = certainFolders
grouperGshTemplate.onboardDepartment.folderShowType = certainFolders
grouperGshTemplate.onboardDepartment.folderUuidToShow = bb3248f4ba494b02a968da8c936ce9f6,etc\u003Atemplates
grouperGshTemplate.onboardDepartment.groupUuidCanRun = etc\u003Asysadmingroup
grouperGshTemplate.onboardDepartment.gshTemplate = /* 2023-10-24 Graham Ballantyne <grahamb@example.com>\n\
grouperGshTemplate.onboardDepartment.input.0.description = Human-readable department name (e.g. Computing Science)
grouperGshTemplate.onboardDepartment.input.0.formElementType = text
grouperGshTemplate.onboardDepartment.input.0.label = Department name
grouperGshTemplate.onboardDepartment.input.0.name = gsh_input_department_name
grouperGshTemplate.onboardDepartment.input.0.type = string
grouperGshTemplate.onboardDepartment.input.0.validationType = none
grouperGshTemplate.onboardDepartment.input.1.description = Name of the department folder, typically department abbreviation (e.g. cmpt). Lower-case, numbers, underscore, hyphen only.
grouperGshTemplate.onboardDepartment.input.1.formElementType = text
grouperGshTemplate.onboardDepartment.input.1.label = Folder name
grouperGshTemplate.onboardDepartment.input.1.name = gsh_input_stem_name
grouperGshTemplate.onboardDepartment.input.1.type = string
grouperGshTemplate.onboardDepartment.input.1.validationRegex = [a-z0-9\u005C-_]+
grouperGshTemplate.onboardDepartment.input.1.validationType = regex
grouperGshTemplate.onboardDepartment.input.2.description = Subjects or reference groups who will be added to the grouper-admins group for this department. Comma-separated list of SFU Computing IDs.
grouperGshTemplate.onboardDepartment.input.2.formElementType = text
grouperGshTemplate.onboardDepartment.input.2.label = Administrators
grouperGshTemplate.onboardDepartment.input.2.name = gsh_input_admins
grouperGshTemplate.onboardDepartment.input.2.type = string
grouperGshTemplate.onboardDepartment.input.2.validationType = none
grouperGshTemplate.onboardDepartment.input.3.description = Whether to create a folder under ADSFU\u005CSFUGroups for this department. Uses the same folder name.
grouperGshTemplate.onboardDepartment.input.3.label = Create ADSFU folder
grouperGshTemplate.onboardDepartment.input.3.name = gsh_input_create_AD_stem
grouperGshTemplate.onboardDepartment.input.3.required = true
grouperGshTemplate.onboardDepartment.input.3.type = boolean
grouperGshTemplate.onboardDepartment.moreActionsLabel = Onboard department
grouperGshTemplate.onboardDepartment.numberOfInputs = 4
grouperGshTemplate.onboardDepartment.runAsType = GrouperSystem
grouperGshTemplate.onboardDepartment.securityRunType = specifiedGroup
grouperGshTemplate.onboardDepartment.showInMoreActions = true
grouperGshTemplate.onboardDepartment.showOnFolders = true
grouperGshTemplate.onboardDepartment.templateDescription = Onboards a department to Grouper. Creates a standard folder structure\u003A\n\
- ref\u003Adept\u003A\u0024{DEPARTMENT_NAME}\n\
- resource\u003Adept\u003A\u0024{DEPARTMENT_NAME}\n\
- (optionally) resource\u003Aapp\u003AADSFU\u003ASFUGroups\u003A\u0024{DEPARTMENT_NAME}\n\
\n\
Creates a ref\u003Adept\u003A\u0024{DEPARTMENT_NAME}\u003Agrouper-admins group with specified members and grants ADMIN (direct and inherited) to the above folders.
grouperGshTemplate.onboardDepartment.templateName = Onboard department
```

## Code

```java
/* 2023-10-24 Graham Ballantyne <grahamb@example.com>
  Creates a standard set of reference and resource groups for onboarding a department to Grouper
  Inputs:
    - gsh_input_department_name(String): Proper name of the department (used in descriptions)
    - gsh_input_stem_name(String): Lower-case department name used as folder names
    - gsh_input_admins(String): Comma-separated list of SFU computing IDs of people to add to the grouper-admins group
    - gsh_input_create_AD_stem(boolean): Whether to create a corresponding stem in the ADSFU stem
*/

import edu.internet2.middleware.grouper.exception.*

GrouperSession session = GrouperSession.startRootSession();

// Uncomment for testing:
// String gsh_input_department_name = "Widget Studies";
// String gsh_input_stem_name = "widgetstudies";
// String gsh_input_admins = "grahamb, hillman, davidli";
// boolean gsh_input_create_AD_stem = true;

// public class GshTemplateOutput {
//   public void addOutputLine(String string) {
//     System.out.println(string);
//   }  
// }
// GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();
// End test setup

Stem refStem = StemFinder.findByName(session, 'ref:dept');
Stem resourceStem = StemFinder.findByName(session, 'resource:dept');
Stem adsfuStem = StemFinder.findByName(session, "resource:app:ADSFU");

// create the reference stem and groups
gsh_builtin_gshTemplateOutput.addOutputLine("Attempting to create reference stem: ${refStem.getName()}:${gsh_input_stem_name}");
StemSave deptRefStemSave = new StemSave(session).assignName("${refStem.getName()}:${gsh_input_stem_name}").assignDescription("Reference groups for ${gsh_input_department_name}");
Stem deptRefStem = deptRefStemSave.save();
if (deptRefStemSave.saveResultType == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Stem existed: ${deptRefStem.getName()}");
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Stem created ${deptRefStem.getName()}");
}

// create a grouper-admins group in the reference stem
gsh_builtin_gshTemplateOutput.addOutputLine('Creating grouper-admins group');
GroupSave grouperAdminsGroupSave = new GroupSave(session).assignName("${deptRefStem.getName()}:grouper-admins").assignDescription("Grouper admins for ${gsh_input_department_name}");
Group grouperAdminsGroup = grouperAdminsGroupSave.save();
if (grouperAdminsGroupSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Group existed: ${grouperAdminsGroup.getName()}");
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Group created ${grouperAdminsGroup.getName()}");
}

// grant ADMIN on the grouper-admins group to itself
Subject grouperAdminsGroupSubject = grouperAdminsGroup.toSubject();
grantPriv(grouperAdminsGroup.getName(), grouperAdminsGroupSubject.id, Privilege.ADMIN);
gsh_builtin_gshTemplateOutput.addOutputLine("Granted admin privileges on ${grouperAdminsGroup.getName()} to group ${grouperAdminsGroup.getName()}");

// grant VIEW and READ on the grouper-admins group to EveryEntity
grantPriv(grouperAdminsGroup.getName(), 'GrouperAll', Privilege.VIEW);
grantPriv(grouperAdminsGroup.getName(), 'GrouperAll', Privilege.READ);

// add the grouper-admins group to the rollup group (ref:grouper:grouper-admins)
addMember("ref:grouper:grouper-admins", grouperAdminsGroup.name)

// add members to the admins group from gsh_input_admins
Set<String> adminsComputingIds = GrouperUtil.splitTrimToSet(gsh_input_admins, ",");
Set<Subject> adminSubjects = [];
for (id in adminsComputingIds) {
  
  // if `id` contains a `:` character, assume it is a group; try to find the group and if valid, add to the adminSubjects list
  if (id.contains(':')) {
    try {
      Group group = GroupFinder.findByName(id, true);
      adminSubjects.add(group.toSubject());
    } catch (GroupNotFoundException) {
        gsh_builtin_gshTemplateOutput.addValidationLine("${id} is not a valid group");
        gsh_builtin_gshTemplateOutput.assignIsError(true);
        GrouperUtil.gshReturn();
    }
  } else {
    try {
      // see if the id is a subject (e.g. a user) and if so, add to the list
      Subject subject = SubjectFinder.findByIdAndSource(id, 'ldap', true);
      adminSubjects.add(subject);
    } catch (SubjectNotFoundException error) {
      gsh_builtin_gshTemplateOutput.addValidationLine("${id} is not a valid subject");
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      GrouperUtil.gshReturn();
    }
  }

}
grouperAdminsGroup.replaceMembers(adminSubjects);
gsh_builtin_gshTemplateOutput.addOutputLine("Added subjects to grouper-admins group: ${adminsComputingIds.join(", ")}");

// add the grouper-admins group to the group that controls access to the UI
addMember("resource:app:grouper:grouper-ui-access", grouperAdminsGroup.getName());
gsh_builtin_gshTemplateOutput.addOutputLine("Added grouper-admins group to the resource:app:grouper:grouper-ui-access group");

// grant ADMIN to the reference stem
grantPriv(deptRefStem.getName(), grouperAdminsGroupSubject.id, NamingPrivilege.STEM);
gsh_builtin_gshTemplateOutput.addOutputLine("Granted admin privileges on ${deptRefStem.getName()} to group ${grouperAdminsGroup.getName()}");
// set inherited privileges on the reference stem
new PrivilegeStemInheritanceSave().assignStem(deptRefStem).assignSubject(grouperAdminsGroupSubject).addPrivilegeName('stemAdmins').save();
new PrivilegeGroupInheritanceSave().assignStem(deptRefStem).assignSubject(grouperAdminsGroupSubject).addPrivilegeName('admin').save();
new PrivilegeAttributeDefInheritanceSave().assignStem(deptRefStem).assignSubject(grouperAdminsGroupSubject).addPrivilegeName('attrAdmins').save();

// create the resource stem
gsh_builtin_gshTemplateOutput.addOutputLine("Attempting to create resource stem: ${resourceStem.getName()}:${gsh_input_stem_name}");
StemSave deptResourceStemSave = new StemSave(session).assignName("${resourceStem.getName()}:${gsh_input_stem_name}").assignDescription("Resource groups for ${gsh_input_department_name}");
Stem deptResourceStem = deptResourceStemSave.save();
if (deptResourceStemSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Stem existed: ${deptResourceStem.getName()}");
} else {
  gsh_builtin_gshTemplateOutput.addOutputLine("Stem created ${deptResourceStem.getName()}");
}

// grant ADMIN to the resource stem
grantPriv(deptResourceStem.getName(), grouperAdminsGroupSubject.id, NamingPrivilege.STEM);
gsh_builtin_gshTemplateOutput.addOutputLine("Granted admin privileges on ${deptResourceStem.getName()} to group ${grouperAdminsGroup.getName()}");
// set inherited privileges on the reference stem
new PrivilegeStemInheritanceSave().assignStem(deptResourceStem).assignSubject(grouperAdminsGroupSubject).addPrivilegeName('stemAdmins').save();
new PrivilegeGroupInheritanceSave().assignStem(deptResourceStem).assignSubject(grouperAdminsGroupSubject).addPrivilegeName('admin').save();
new PrivilegeAttributeDefInheritanceSave().assignStem(deptResourceStem).assignSubject(grouperAdminsGroupSubject).addPrivilegeName('attrAdmins').save();

// if ADSFU checked, create stem
if (gsh_input_create_AD_stem) {
  gsh_builtin_gshTemplateOutput.addOutputLine("Attempting to create ADSFU stem: resource:app:ADSFU:SFUGroups:${gsh_input_stem_name}");
  StemSave adsfuDeptStemSave = new StemSave(session).assignName("resource:app:ADSFU:SFUGroups:${gsh_input_stem_name}").assignDescription("AD groups for ${gsh_input_department_name}");
  Stem adsfuDeptStem = adsfuDeptStemSave.save();
  if (adsfuDeptStemSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
    gsh_builtin_gshTemplateOutput.addOutputLine("Stem existed: ${adsfuDeptStem.getName()}");
  } else {
    gsh_builtin_gshTemplateOutput.addOutputLine("Stem created ${adsfuDeptStem.getName()}");
  }

  // grant ADMIN to the ADSFU stem
  grantPriv(adsfuDeptStem.getName(), grouperAdminsGroupSubject.id, NamingPrivilege.STEM);
  gsh_builtin_gshTemplateOutput.addOutputLine("Granted admin privileges on ${adsfuDeptStem.getName()} to group ${grouperAdminsGroup.getName()}");
  // set inherited privileges on the reference stem
  new PrivilegeStemInheritanceSave().assignStem(adsfuDeptStem).assignSubject(grouperAdminsGroupSubject).addPrivilegeName('stemAdmins').save();
  new PrivilegeGroupInheritanceSave().assignStem(adsfuDeptStem).assignSubject(grouperAdminsGroupSubject).addPrivilegeName('admin').save();
  new PrivilegeAttributeDefInheritanceSave().assignStem(adsfuDeptStem).assignSubject(grouperAdminsGroupSubject).addPrivilegeName('attrAdmins').save();

  gsh_builtin_gshTemplateOutput.addOutputLine("DONE running department onboarding for ${gsh_input_department_name}");
}
```
