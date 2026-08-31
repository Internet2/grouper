---
title: "Grouper Training - Use cases - Lesson 12: ABAC policies"
space: Grouper
pageId: 28544466
version: 31
lastUpdated: 2026-04-22T02:27:17.332Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544466/Grouper+Training+-+Use+cases+-+Lesson+12+ABAC+policies
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

## Add some lockouts

In group: ref:security:locked_by_ciso

Add users Ronald Abbott, Ashley Adams, Mary Bennett, Deborah Andrews, Ashley Torres

```
800001377
800001808
800002114
800002239
800000618
```

## Create application

- Use the application template on the **app** folder to create an app
  
  - Key: OneDrive
  - Description: Access to OneDrive created 3/15/2024 by Bob Anderson as a request from John Smith in Arts and Sciences referencing ticket number: WO4138212

## Mock up the training requirements

- In the top-level **ref** folder make this folder (do NOT use the AppDrive ref folder!)
  
  - Name: training
  - Description: Lists of people who are required training or who have completed training
- In the **ref:training** folder make this folder
  
  - Name: trainingRequired
  - Description: Lists of people who are required training
- In the **ref:training** folder make this folder
  
  - Name: trainingCompleted
  - Description: Lists of people who have completed training
- In the **ref:training:trainingRequired** folder make the following group:  
  
  
  - Group name (and ID): privacy_cert_required
  - Description: People who are required to complete the privacy training or who are required to recertify. This is mandated by data owners and security departments
  - Import these members (copy and paste)
    
    
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
- In the **ref:training:trainingCompleted** folder make the following group:
  
  - Group name (and ID): privacy_certified
  - Description: People who have completed the privacy training and do not need to recertify. This is fed from the LMS.
  - Import these members:
    
    
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
- Search for these two groups to see if you did this correctly. Compare these ID paths with the ID path of the group, they should match.
  
  - ref:training:trainingCompleted:privacy_certified
  - ref:training:trainingRequired:privacy_cert_required

## HR data provider run

Job: OTHER_JOB_dataProviderHR

## JEXL daemon schedule

View the settings of the daemon jobs to run the loaders. Since v5.18.0 there is both a full sync to update all jexl loader groups, and an incremental to update changed groups.

Job: OTHER_JOB_grouperLoaderJexlScriptFullSync

## Create a policy group

In **app:OneDrive:service:policy** create the group:

- Name: OneDriveUser
- Description: Grants access to OneDrive if the user should have a license and does not have training issues

Group actions → Loader → Edit Loader

- Source type: Scripted group
- Analyze these scripts
  
  - Faculty
    
    
    ```
    /* faculty */
    entity.hasRow('hr_positions', " role == 'faculty' ")
    ```
  - Faculty or staff
    
    
    ```
    /* faculty or staff */
    entity.hasRow('hr_positions', " role == 'faculty' or role == 'staff' ")
    ```
  - Faculty or staff in arts and sciences
    
    
    ```
    /* faculty or staff in arts and sciences */
    entity.hasRow('hr_positions', " (role == 'faculty' or role == 'staff') and org_code == 'AS' ")
    ```
  - Faculty or staff in arts and sciences, and not required to take privacy training
    
    
    ```
    /* faculty or staff in arts and sciences */
    entity.hasRow('hr_positions', " (role == 'faculty' or role == 'staff') and org_code == 'AS' ")
     
    /* either people who are not required to take privacy training */
    and !entity.memberOf('ref:training:trainingRequired:privacy_cert_required')
    ```
  - Faculty or staff in arts and sciences, and either not required to take training or has been trained
    
    
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
  - Faculty or staff in arts and sciences, and either not required to take training or has been trained, and not locked out
    
    
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
  - Analyze for users:
    
    - Jeremy Allen (jallen): faculty in arts and sciences, not required for training
    - Jennifer Malone (jmalone): in arts and sciences but not faculty or staff
    - Jennifer Smith (jsmith4): A&S staff, required and certified
    - Paula Miller (pmiller3): A&S staff, required but not certified
    - Ashley Torres (atorres2): A&S staff, required and certified, global lockout group
  - Save the loader
  - Wait a minute for the incremental loader to run
  - See members in group

## GSH template ABAC pattern

Miscellaneous → Configure → Configuration files → Config actions → Import config file → Copy / paste configuration entries

Configuration file type: grouper.properties

Configuration file contents

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

View GSH template in Miscellaneous → GSH templates

Add a new group in test folder: somePolicy

Group actions → Loader → Loader actions → Edit loader configuration

Loader: Yes, has loader configuration

Source type: scripted group

Construct group: pattern

Patterns: HR role and org

Org: CIS

Role: staff

Analyze → Save
