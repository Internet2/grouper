---
title: "Grouper external subjects from invites"
space: Grouper
pageId: 28549552
version: 7
lastUpdated: 2024-06-13T04:22:55.899Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549552/Grouper+external+subjects+from+invites
---

## URLs and servlets

The self service functions are at a distinct /grouperExternal/public/* URL, so that external users can be protected by a Shibboleth or other separate authentication system, and the rest of the UI can be protected by a local single sign on system. Or the UI can be deployed twice at different URLs. The URL of the external part is e.g.  
  
http://localhost:8090/grouper/grouperExternal/public/UiV2Public.index?operation=UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister&externalSubjectInviteId=<*groupid*>

## Invitations with group provisioning

Invites in the new UI require at least one group to be invited to. The group management page has a menu item under More Actions for the inviting person (with ADMIN or UPDATE privilege) that launches the invitation form, with the current group pre-filled as the provisioned group. In the form, additional groups can be added to the provisioning list. When an invite email is clicked on, all pending invites for that email address are processed.

## Properties relevant to extenal invitations

| config file | property | default value | config_comment |
| --- | --- | --- | --- |
| grouper.properties | externalSubjects.registerRequiresInvite | true | if registrations are only allowed if invited or existing... |
| grouper.properties | externalSubjects.autoaddGroups | None | put some group names comma separated for groups to auto add subjects to |
| grouper.properties | externalSubjects.autoaddGroupExpireAfterDays |  | if a number is here, expire the group assignment after a certain number of days |
| grouper.properties | grouper.ui.url |  | put the URL which will be used e.g. in emails to users. Include the webapp context (e.g. https://hostname/grouper/) |
| grouper.properties | externalSubjects.validateIndentiferLikeEmail | true | make sure the identifier when logging in is like an email address or eppn, e.g. username@example.com |
| grouper-ui.properties | externalMembers.enabledRegistration | false | if the registration screen is enabled |
| grouper-ui.properties | inviteExternalMembers.enableInvitation | false | if the invitation screen is enabled |
| grouper-ui.properties | inviteExternalPeople.link-from-new-ui | false | if link from new UI |
| grouper-ui.properties | grouperUi.autoCreateUserFolderOnLogin | false | if true, when a user logs in, a folder will be created and granted to the user if not already there |
| grouper-ui.properties | grouperUi.autoCreateUserFolderName |  | set a folder for the user.you can use EL here based on the subject, e.g. users:folders:${subject.id} or users:folders:${subject.getAttributeValue('netId')} |

## Group manager invite form

## Onboarding form

The fields below can be customized per institution, as well as the text, look and feel, etc. Some applications might require a lot of user data, and others do not need as much data about the user. It would be nice to have a lot of data, e.g. so the application can use the data (e.g. email address), and so we can have descriptive person pickers, though it is a little risky since the data is user entered and unvetted.
