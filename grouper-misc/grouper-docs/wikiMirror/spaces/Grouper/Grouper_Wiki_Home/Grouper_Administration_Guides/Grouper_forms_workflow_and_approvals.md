---
title: "Grouper forms, workflow and approvals"
space: Grouper
pageId: 28543753
version: 32
lastUpdated: 2026-07-01T05:49:06.432Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543753/Grouper+forms+workflow+and+approvals
---

Grouper has a built-in electronic forms, workflow, and approvals capability. A subject submits a form (for example, to join a group), the request routes through one or more approval states, and on completion Grouper can automatically provision the resulting membership. It is intentionally simple; if you need something more complex you would integrate Grouper with a dedicated BPM or workflow system. Grouper can also expose an interface so external workflow services satisfy the request.

> Electronic forms / workflow were introduced in v2.4 (via the `grouper_v2_4_0_api_patch_70` and `grouper_v2_4_0_ui_patch_42` patches) and are available in the currently supported releases. Workflow is enabled by default (`workflow.enable = true` in `grouper.properties`).

> **Required privileges:** Grouper admins, or members of the workflow editors group, can configure workflows on a group. The editors group defaults to `etc:workflowEditors` and is configurable with `workflow.editorsGroup` in `grouper.properties`. Anyone in a state's approver group can act on the instances routed to them, and the optional `workflowConfigViewersGroupId` grants view access to a workflow and its instances.

## High level description

Think of this like a paper form, but electronic. It is simple and if you need something more complicated likely you might need a BPM or workflow system and integrate with it.

- Grouper admins (or members of etc:workflowEditors group) can configure a workflow on a group
  
  - Specify which groups need to approve the request
  - Configure the workflow and form fields with JSON
  - Configure the form itself with HTML
- A workflow daemon sends out notifications. There are two daemons:
  
  1. Runs every 5 minutes, looks for state changes, sends out individual email
  2. Runs nightly, looks for instances that need another reminder, sends out digest email
- Screens to view and manage the requests
- Automatic provisioning into group(s)
- A copy of the form being submitted will be stored at every state of the form to the database, AWS S3, or the file system. That includes the history of the form and who approved it when
- A workflow can be initiated from the UI or by web service. From the UI, Grouper processes the "initiate" state immediately, sends the notification, and advances to the next state. When initiated by web service, the daemon (which runs every 5 minutes) picks up the "initiate" state and advances it.

## Group "More actions" menu

On a group, the "More actions" menu includes an "Electronic forms" action, which opens the "View forms" screen. Joining a group can also kick off a workflow.

## Notifications

Emails are sent to approvers when they have forms to approve. An email goes out when the state changes, and a daily digest (one per user) reminds approvers of pending forms.

## Electronic forms screens

From a group's "Electronic forms" screen, the actions menu offers:

- "View forms"
- "Edit form"

Under "Miscellaneous", the "Forms" area offers:

- "My forms" – forms the current user submitted
- "Forms waiting for my approval" – forms awaiting the current user's approval

## Screens

On a group, the "Electronic forms" action is available in the "More actions" menu.

Clicking on the Electronic forms shows all the workflows configured on the selected group.

When a subject wants to join the group and click on the Join group button in the More actions, they are shown a form configured as per the workflow.

Under Miscellaneous, there is a new link called Forms

Clicking on Miscellaneous → Forms → My forms shows a list of all the forms the logged in subject has initiated.

Clicking on Miscellaneous → Forms → Forms waiting for my approval shows a list of all the forms logged in subject has in its pending queue.

## Future enhancements

- Enable kicking off workflows from other events (e.g. folder create, group create, membership add , membership remove , attribute values add/change/remove, etc…)
- Forms search for form admins – find forms, see their status, search by submitter, and approve on behalf of approvers

## Configuration

The configuration follows the same attribute structure as other Grouper modules such as reporting.

**Attribute definitions for config**

| Definition | Assigned To | Purpose | Value | Cardinality |
| --- | --- | --- | --- | --- |
| workflowConfigDef | group | identify a workflow config | marker | Multi assign |
| workflowConfigValueDef | group assignment | name/value pairs | string | Single assign, single valued |

****

### **Instance values at different states in workflow**

#### When user submits form

| **Attribute** | **Value** |
| --- | --- |
| workflowInstanceState | initiate |
| workflowInstanceLastUpdatedMillisSince1970 | lastUpdated |
| workflowInstanceConfigMarkerAssignmentId | assign id |
| workflowInstanceInitiatedMillisSince1970 | timestamp |
| workflowInstanceUuid | uuid |
| workflowInstanceEncryptionKey | generated an encrypted encryption key |
| workflowInstanceFileInfo | value is:     ``` { fileNamesAndPointers: [ {state: "initiate", fileName: "/something.html", filePointer: "something/something"} ] } ```    File contents have:  HTML  show form just like they saw it  has values in there.  has some auditing at the bottom in div. append to that at each state  subjectsource: subjectID, name clicked approve for state dataOwner on timestamp: 2019/03/05 12:35:47 (standard timestamp format) |
| workflowInstanceLog | ``` { logEntries: [   {subjectSourceId: "jdbc", subjectId: "jsmith", action: "initiate", state: "initiate", millisSince1970: 12345678} ] } ``` |
| workflowInstanceParamValue0 | params should be in order that they are configured   ``` {paramName: "notesForApprovers", paramValue: "He will be collaborating with such and such which is why he needs access",  lastUpdatedMillis: 12324345432434234, editedByMemberId: "abc123", editedInState: "initiate"} ``` |
| workflowInstanceParamValue1 | if there is more than one param, put second one here |

Daemon runs, sees the state initiate, and progresses workflow:

| **Attribute** | **Value** |
| --- | --- |
| workflowInstanceState | supervisor |
| workflowInstanceLastUpdatedMillisSince1970 | lastUpdated |
| workflowInstanceLastEmailedDate | now |
| workflowInstanceLastEmailedState | supervisor |
| workflowInstanceLog | ``` { logEntries: [   {subjectSourceId: "jdbc", subjectId: "jsmith", action: "initiate", state: "initiate", millisSince1970: 12345678},   {action: "workflowStateChange", state: "supervisor", millisSince1970: 12345679} ] } ``` |

**Attribute names for config**

Note: for non-grouper workflows configured in grouper.properties, only the marker and type will be set. For Grouper workflows, all the attributes will be available

| Name | Definition | Required? | Value |
| --- | --- | --- | --- |
| workflowConfigMarker | workflowConfigDef |  | <none> |
| workflowConfigType | workflowConfigValueDef | required | if grouper is the only implementation in grouper.properties, auto populate with "grouper", otherwise list other implementations |
| workflowConfigApprovals | workflowConfigValueDef | required | This is a JSON config of the workflow approvals. must have "initiate" and "complete"   ``` {   states: [     {       stateName: "initiate",        allowedGroupId: "abc123def456"          },          {       stateName: "supervisor",       approverSubjectId: "${initiatorSubject.attribute['supervisorSubjectId']}",       approverSubjectSourceId: "mypeople"     },          {       stateName: "dataOwner",       //who can approve       approverGroupId: "sdgf76gdf87",         //who is notified, if blank, then use approverGroupId       approverNotifyGroupId: "dfkjh234kjb"     },          {       stateName: "complete",       //if blank use the selected groups from form or just the group       //the workflow is assigned to       actions: [            {               actionName: "assignToGroup",               actionArg0: "sgk234kh234"              }         ]     }   ] } ```  This is the default and should pre-populate:   ```  {   states: [     {       stateName: "initiate",      },     {       stateName: "groupManager",       //who can approve       approverManagersOfGroupId: "sdgf76gdf87" <-- groupId of current group                                                <-- updater/admins of group     },     {       stateName: "complete",       //if blank use the selected groups from form or just the group       //the workflow is assigned to       actions: [            {               actionName: "assignToGroup",               actionArg0: "sgk234kh234"   <-- groupId of current group             }         ]     }   ] } ``` |
| workflowConfigName | workflowConfigValueDef | required | Name of workflow. No two workflows in the same owner should have the same name  Default to groupName_managerApproval (e.g. wikiUsers_managerApproval) |
| workflowConfigId | workflowConfigValueDef | required | Camel-case alphanumeric id of workflow. No two workflows in all of Grouper can have the same ID  Default to groupName_managerApproval (e.g. wikiUsers_managerApproval) |
| workflowConfigDescription | workflowConfigValueDef | required | Textarea which describes the information in the workflow. Must be less than 4k  Default to: Group: $groupDisplayPath% approval for membership. The group's managers will be notified about requests and can approve them. |
| workflowConfigParams | workflowConfigValueDef | required | Note: max 10 params, checkbox is true/false   ``` {   params: [     {        paramName: "agreeToTerms",       label: "Agree to terms",       type: "checkbox",       editableInStates: "initiate",       required: "true"     },     {       paramName: "notes",       label: "Notes",       type: "textarea",       editableInStates: "initiate"     },     {       paramName: "notesForApprovers",       label: "Notes for approvers",       type: "textarea",       editableInStates: "supervisor, dataOwner"     },     {       paramName: "reason",       label: "Reason",       type: "text",       editableInStates: "initiate"     }   ] } ```  Default pre-populated:   ``` {   params: [     {       paramName: "notes",       label: "Notes",       type: "textarea",       editableInStates: "initiate"     },     {       paramName: "notesForApprovers",       label: "Notes for approvers",       type: "textarea",       editableInStates: "supervisor, dataOwner"     }   ] } ``` |
| workflowConfigForm | workflowConfigValueDef | optional | Note: Grouper with javascript will enable or disable these form fields, or fill in values, as needed. The form field names need to match the names in the params. The id must match with "Id" appended to the end. If a name or id doesnt exist in the HTML there will be an error   ``` Fill out this form to be added to this group.<br /><br /> Several approvals will take place which usually take less than 2 business days<br /><br /> State the reason you would like this access: <input type="text" name="reason" id="reasonId" /><br /><br > <input type="checkbox" name="agreeToTerms" id="agreeToTermsId" /> I agree this this institutions' <a href="https://whatever.whatever/whatever">terms and conditions</a><br /><br /> Notes: <textarea rows="4" cols="50" name="notes" id="notesId"></textarea><br /><br /> Notes for approvers: <textarea rows="4" cols="50" name="notesForApprovers" id="notesForApproversId"></textarea><br /><br />   ```     This is the default:   ``` Submit this form to be added to this group.<br /><br /> The managers of the group will be notified to approve this request.<br /><br /> Notes (optional): <textarea rows="4" cols="50" name="notes" id="notesId"></textarea><br /><br /> Notes for approvers: <textarea rows="4" cols="50" name="notesForApprovers" id="notesForApproversId"></textarea><br /><br /> ``` |
| workflowConfigViewersGroupId | workflowConfigValueDef | optional | GroupId of people who can view this workflow and instances of this workflow. Grouper admins can view any workflow (blank means admin only). Anyone in an approver group can view the workflow. |
| workflowConfigSendEmail | workflowConfigValueDef | required (default to true, no blank option available) | true/false if email should be sent |
| workflowConfigEnabled | workflowConfigValueDef | default to true (required, no blank option) | Could by "true", "false", or "noNewSubmissions", i.e. let current forms go through |

****

**Attribute definitions for instance (a workflow that was run)**

This attribute is assigned to the same owner as the config attribute (e.g. the same group/folder)

| Definition | Assigned To | Purpose | Value | Cardinality |
| --- | --- | --- | --- | --- |
| workflowInstanceDef | group | identify a workflow that was run | marker | Multi assign |
| workflowInstanceValueDef | folder assignment, group assignment | name/value pairs | string | Single assign, single valued |

****

**Attribute names for instance**

Note: the ID is the attribute assign id of the marker (this is passed in URLs/emails etc)

| Name | Definition | Value |
| --- | --- | --- |
| workflowInstanceMarker | workflowInstanceDef | <none> |
| workflowInstanceState | workflowInstanceValueDef | Any of the states, plus "exception" if there is a problem, workflows must have "initiate", and "complete", plus "rejected" if someone rejects it. |
| workflowInstanceLastUpdatedMillisSince1970 | workflowInstanceValueDef | number of millis since 1970 when this instance was last updated |
| workflowInstanceConfigMarkerAssignmentId | workflowInstanceValueDef | Attribute assign ID of the marker attribute of the config (same owner as this attribute, but there could be many workflows configured on one owner) |
| workflowInstanceInitiatedMillisSince1970 | workflowInstanceValueDef | millis since 1970 that this workflow was submitted |
| workflowInstanceUuid | workflowInstanceValueDef | uuid assigned to this workflow instance |
| workflowInstanceFileInfo | workflowInstanceValueDef | value is:  pointer: depending on storage type, this is a pointer to the workflow in storage, e.g. the S3 address. note the S3 address is .csv suffix, but change to __metadata.json for instance metadata     ``` { fileNamesAndPointers: [ {state: "initiate", fileName: "/something.html", filePointer: "something/something"} ] } ```    File contents have:  HTML  show form just like they saw it  has values in there.  has some auditing at the bottom in div. append to that at each state  subjectsource: subjectID, name clicked approve for state dataOwner on timestamp: 2019/03/05 12:35:47 (standard timestamp format) |
| workflowInstanceEncryptionKey | workflowInstanceValueDef | randomly generated 16 char alphanumeric encryption key (never allow display or edit of this) |
| workflowInstanceLastEmailedDate | workflowInstanceValueDef | yyyy/mm/dd date that this was last emailed so multiple emails dont go out on same day |
| workflowInstanceLastEmailedState | workflowInstanceValueDef | the state of the workflow instance when it was last emailed |
| workflowInstanceLog | workflowInstanceValueDef | has brief info about who did what when on this instance |
| workflowInstanceError | workflowInstanceValueDef | error message including stack of why this instance is in "exception" state |
| workflowInstanceParamValue0 | workflowInstanceValueDef | ``` {paramName: "agreeToTerms", paramValue: "true", lastUpdatedMillis: 12324345432434234, editedByMemberId: "abc123", editedInState: "initiate"} ``` |
| workflowInstanceParamValue1 | workflowInstanceValueDef | ``` {paramName: "notes", paramValue: "Im traveling abroad and need access to such and such", lastUpdatedMillis: 12324345432434234, editedByMemberId: "abc123", editedInState: "initiate"} ``` |
| workflowInstanceParamValue2 | workflowInstanceValueDef | ``` {paramName: "notesForApprovers", paramValue: "He will be collaborating with such and such which is why he needs access", lastUpdatedMillis: 12324345432434234, editedByMemberId: "abc123", editedInState: "initiate"} ``` |
| workflowInstanceParamValue3 | workflowInstanceValueDef | ``` {paramName: "reason", paramValue: "Need access to whatever system", lastUpdatedMillis: 12324345432434234, editedByMemberId: "abc123", editedInState: "initiate"} ``` |
| workflowInstanceParamValue4 | workflowInstanceValueDef |  |
| workflowInstanceParamValue5 | workflowInstanceValueDef |  |
| workflowInstanceParamValue6 | workflowInstanceValueDef |  |
| workflowInstanceParamValue7 | workflowInstanceValueDef |  |
| workflowInstanceParamValue8 | workflowInstanceValueDef |  |
| workflowInstanceParamValue9 | workflowInstanceValueDef |  |
