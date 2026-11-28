---
title: "Grouper Training - Use cases - Lesson 12: ABAC policies"
space: Grouper
pageId: 28544466
version: 33
lastUpdated: 2026-11-19T19:02:42.204Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544466/Grouper+Training+-+Use+cases+-+Lesson+12+ABAC+policies
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives

- Write attribute-based (ABAC) JEXL scripted groups from HR data and reference groups
- Analyze a script for specific users before saving it
- Use a GSH template ABAC pattern to build a script from a form

## Add some lockouts

Add Ronald Abbott, Ashley Adams, Mary Bennett, Deborah Andrews, and Ashley Torres to the CISO lockout group.

1. In the left menu, click "Browse folders", click "Root", then click "ref" → "security".
2. Click the group "locked_by_ciso".
3. Click "Add members" → "Import".
4. Scroll down to "Add these members" and change the radio button to "Copy/paste a list of member IDs".
5. Paste this whole list into the text area, then click "Submit":
  
  
  ```
  800001377
  800001808
  800002114
  800002239
  800000618
  ```

## Create application

1. In the left menu, click "Browse folders", click "Root", then click "app".
2. Click "Folder actions" → "Run template".
3. "Template to run": select "Application".
4. "Key":
  
  
  ```
  OneDrive
  ```
5. "Description":
  
  
  ```
  Access to OneDrive created 3/15/2024 by Bob Anderson as a request from John Smith in Arts and Sciences referencing ticket number: WO4138212
  ```
6. Click "Next".
7. Review the folders and groups the template will create, then click "Submit".

## Mock up the training requirements

Make these folders in the top-level "ref" folder (not in the OneDrive application ref folder).

### Create the training folders

1. In the left menu, click "Browse folders", click "Root", then click "ref".
2. Click "Folder actions" → "Create new folder".
3. "Folder name":
  
  
  ```
  training
  ```
4. "Description":
  
  
  ```
  Lists of people who are required training or who have completed training
  ```
5. Click "Save".

1. In the left menu, click "Browse folders", click "Root", then click "ref" → "training".
2. Click "Folder actions" → "Create new folder".
3. "Folder name":
  
  
  ```
  trainingRequired
  ```
4. "Description":
  
  
  ```
  Lists of people who are required training
  ```
5. Click "Save".

1. In the left menu, click "Browse folders", click "Root", then click "ref" → "training".
2. Click "Folder actions" → "Create new folder".
3. "Folder name":
  
  
  ```
  trainingCompleted
  ```
4. "Description":
  
  
  ```
  Lists of people who have completed training
  ```
5. Click "Save".

### Create the privacy training required group

1. In the left menu, click "Browse folders", click "Root", then click "ref" → "training" → "trainingRequired".
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  privacy_cert_required
  ```
4. "Description":
  
  
  ```
  People who are required to complete the privacy training or who are required to recertify.  This is mandated by data owners and security departments
  ```
5. Click "Save". You land on the new group page "ref:training:trainingRequired:privacy_cert_required".
6. Click "Add members" → "Import".
7. Scroll down to "Add these members" and change the radio button to "Copy/paste a list of member IDs".
8. Paste this whole list into the text area, then click "Submit":
  
  
  ```
  basis:hr:employee:dept:21300:staff
  basis:hr:employee:dept:21350:staff
  basis:hr:employee:dept:21400:staff
  basis:hr:employee:dept:21450:staff
  basis:hr:employee:dept:21500:staff
  basis:hr:employee:dept:21550:staff
  basis:hr:employee:dept:21600:staff
  basis:hr:employee:dept:21650:staff
  basis:hr:employee:dept:21700:staff
  basis:hr:employee:dept:21750:staff
  basis:hr:employee:dept:21800:staff
  ```

### Create the privacy certified group

1. In the left menu, click "Browse folders", click "Root", then click "ref" → "training" → "trainingCompleted".
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  privacy_certified
  ```
4. "Description":
  
  
  ```
  People who have completed the privacy training and do not need to recertify.  This is fed from the LMS.
  ```
5. Click "Save". You land on the new group page "ref:training:trainingCompleted:privacy_certified".
6. Click "Add members" → "Import".
7. Scroll down to "Add these members" and change the radio button to "Copy/paste a list of member IDs".
8. Paste this whole list into the text area, then click "Submit":
  
  
  ```
  jsmith4
  scastill
  czuniga
  bberry
  aburch
  ttaylor
  broberso
  ewillia5
  rgomez
  dweaver
  gerickso
  dhoffman
  ksimmons
  rhall
  cfowler
  mburnett
  aowens
  smaxwell
  rrodrigu
  mwelch2
  tharriso
  eallen
  dvillanu
  abrewer
  mwaller
  jschmid2
  kali
  ddiaz
  sshaw
  jjohnso3
  mwilson3
  dmcmilla
  atorres2
  ajackso2
  vfreeman
  powens
  dbrown3
  wespinoz
  cbrennan
  ggarcia
  dcoleman
  ```

### Check your work

1. Search for this group and check that its ID path matches exactly:
  
  
  ```
  ref:training:trainingCompleted:privacy_certified
  ```
2. Search for this group and check that its ID path matches exactly:
  
  
  ```
  ref:training:trainingRequired:privacy_cert_required
  ```

## HR data provider run

The HR data provider loads the HR position data (hr_positions) that the scripts below use.

1. In the left menu, click "Miscellaneous", then click "Daemon jobs".
2. Type this in the filter box and click "Apply filter":
  
  
  ```
  OTHER_JOB_dataProviderHR
  ```

## Create a policy group

### Create the group

1. In the left menu, click "Browse folders", click "Root", then click "app" → "OneDrive" → "service" → "policy".
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  OneDriveUser
  ```
4. "Description":
  
  
  ```
  Grants access to OneDrive if the user should have a license and does not have training issues
  ```
5. Click "Save". You land on the new group page "app:OneDrive:service:policy:OneDriveUser".

### Build and analyze the script

Build the script one piece at a time. For each step, replace everything in "Entity JEXL script" with the script, then click "Analyze" and look at the results.

1. Click "Group actions" → "Loader".
2. Click "Loader actions" → "Edit loader configuration".
3. "Loader": select "Yes, has loader configuration".
4. "Source type": select "Scripted group".
5. "Construct script": select "Input script".
6. Faculty:
  
  
  ```
  /* faculty */
  entity.hasRow('hr_positions', " role == 'faculty' ")
  ```
7. Faculty or staff:
  
  
  ```
  /* faculty or staff */
  entity.hasRow('hr_positions', " role == 'faculty' or role == 'staff' ")
  ```
8. Faculty or staff in arts and sciences:
  
  
  ```
  /* faculty or staff in arts and sciences */
  entity.hasRow('hr_positions', " (role == 'faculty' or role == 'staff') and org_code == 'AS' ")
  ```
9. Faculty or staff in arts and sciences, and not required to take privacy training:
  
  
  ```
  /* faculty or staff in arts and sciences */
  entity.hasRow('hr_positions', " (role == 'faculty' or role == 'staff') and org_code == 'AS' ")
   
  /* either people who are not required to take privacy training */
  and !entity.memberOf('ref:training:trainingRequired:privacy_cert_required')
  ```
10. Faculty or staff in arts and sciences, and either not required to take training or has been trained:
  
  
  ```
  /* faculty or staff in arts and sciences */
  entity.hasRow('hr_positions', " (role == 'faculty' or role == 'staff') and org_code == 'AS' ")
  and (
    /* either people who are not required to take privacy training */
    !entity.memberOf('ref:training:trainingRequired:privacy_cert_required')
  
    /* or people who are trained */
    or entity.memberOf('ref:training:trainingCompleted:privacy_certified')
  )
  
  
  ```
11. Faculty or staff in arts and sciences, and either not required to take training or has been trained, and not locked out:
  
  
  ```
  /* faculty or staff in arts and sciences */
  entity.hasRow('hr_positions', " (role == 'faculty' or role == 'staff') and org_code == 'AS' ")
  and (
    /* either people who are not required to take privacy training */
    !entity.memberOf('ref:training:trainingRequired:privacy_cert_required')
  
    /* or people who are trained */
    or entity.memberOf('ref:training:trainingCompleted:privacy_certified')
  )
  /* do not allow people in global deny group */
  and !entity.memberOf('ref:iam:global_deny')
  ```
12. Analyze the script for specific users. In "Member name or ID", paste each user below one at a time, pick the user from the list, and click "Analyze". Jeremy Allen: faculty in arts and sciences, not required for training:
  
  
  ```
  jallen
  ```
13. Jennifer Malone: in arts and sciences but not faculty or staff:
  
  
  ```
  jmalone
  ```
14. Jennifer Smith: A&S staff, required and certified:
  
  
  ```
  jsmith4
  ```
15. Paula Miller: A&S staff, required but not certified:
  
  
  ```
  pmiller3
  ```
16. Ashley Torres: A&S staff, required and certified, in the global lockout group:
  
  
  ```
  atorres2
  ```
17. Click "Save".
18. Wait a minute for the incremental loader to run, then click the "Members" tab to see the members.

## GSH template ABAC pattern

### Import the template

1. In the left menu, click "Miscellaneous", then click "Configure" → "Configuration files".
2. Click "Config actions" → "Import config file".
3. "Import configuration with": select "Copy/paste configuration entries".
4. "Configuration file type": select "grouper.properties".
5. "Configuration file contents":
  
  
  ```
  grouperGshTemplate.hrRole.gshTemplate = StringBuilder script = new StringBuilder('\u0024' + "{ \u005Cn");\n\
      \n\
      script.append(" entity.hasRow('hr_positions', \u005C" role == '" + gsh_input_role + "' and org_code == '" + gsh_input_org + "' \u005C") ");\n\
      \n\
      script.append("\u005Cn} ");\n\
        \n\
      gsh_builtin_gshTemplateOutput.assignAbacScript(script.toString());
  grouperGshTemplate.hrRole.input.0.description = Org
  grouperGshTemplate.hrRole.input.0.dropdownSqlDatabase = hr
  grouperGshTemplate.hrRole.input.0.dropdownSqlValue = select abbrev, abbrev from hr_orgs order by 1
  grouperGshTemplate.hrRole.input.0.dropdownValueFormat = sql
  grouperGshTemplate.hrRole.input.0.formElementType = dropdown
  grouperGshTemplate.hrRole.input.0.label = Org
  grouperGshTemplate.hrRole.input.0.name = gsh_input_org
  grouperGshTemplate.hrRole.input.0.required = true
  grouperGshTemplate.hrRole.input.1.description = Role
  grouperGshTemplate.hrRole.input.1.dropdownSqlDatabase = hr
  grouperGshTemplate.hrRole.input.1.dropdownSqlValue = select distinct role, role from hr_positions order by 1
  grouperGshTemplate.hrRole.input.1.dropdownValueFormat = sql
  grouperGshTemplate.hrRole.input.1.formElementType = dropdown
  grouperGshTemplate.hrRole.input.1.label = Role
  grouperGshTemplate.hrRole.input.1.name = gsh_input_role
  grouperGshTemplate.hrRole.input.1.required = true
  grouperGshTemplate.hrRole.numberOfInputs = 2
  grouperGshTemplate.hrRole.runAsType = GrouperSystem
  grouperGshTemplate.hrRole.securityRunType = wheel
  grouperGshTemplate.hrRole.templateDescription = Script based on HR role and org
  grouperGshTemplate.hrRole.templateName = HR role and org
  grouperGshTemplate.hrRole.templateType = abac
  grouperGshTemplate.hrRole.templateVersion = V1
  ```
6. Click "Submit".
7. In the left menu, click "Miscellaneous", then click "GSH templates" and find "HR role and org".

### Use the pattern on a group

1. In the left menu, click "Browse folders", click "Root", then click "test".
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  somePolicy
  ```
4. Click "Save". You land on the new group page "test:somePolicy".
5. Click "Group actions" → "Loader".
6. Click "Loader actions" → "Edit loader configuration".
7. "Loader": select "Yes, has loader configuration".
8. "Source type": select "Scripted group".
9. "Construct script": select "Pattern".
10. "Patterns": select "HR role and org".
11. "Org": select "CIS".
12. "Role": select "staff".
13. Click "Analyze", then click "Save".
