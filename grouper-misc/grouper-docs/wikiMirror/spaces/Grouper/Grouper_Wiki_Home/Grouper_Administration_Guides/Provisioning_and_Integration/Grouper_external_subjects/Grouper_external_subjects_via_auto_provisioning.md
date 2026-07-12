---
title: "Grouper external subjects via auto-provisioning"
space: Grouper
pageId: 28549558
version: 1
lastUpdated: 2024-06-13T04:03:48.009Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549558/Grouper+external+subjects+via+auto-provisioning
---

With certain property settings, external users can self-register their federated account in Grouper via the registration URL and logging in with their institution's SSO. In this case, the users aren't set up from invitations, so they won't receive email notices with registration links. Instead, they must know the registration link through other communications. But similar to the invite link, once logged in they will see the creation form, where they can enter their name, institution, and email. Their SSO principal name will appear as a read-only value. Once submitted, a new subject record will be added to the grouperExternal source. Optionally, newly provisioned users can be added to one or more groups, with or without an expiration date.

## User perspective

The user will first access the registration URL:

https://<hostname>/grouper/grouperExternal/public/UiV2Public.index?operation=UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister

The self service functions are at a distinct /grouper/grouperExternal/public/* URL, so that external users can be authenticated by a separate SSO from the rest of the /grouper/* pages. This will require modification from the standard Shibboleth service provider configuration, in order to handle distinct SP's. Alternatively, the UI can be deployed twice at different URLs.

Once authenticated via their local institution's SSO, the registration page will appear. The name field is required, but institution name and email are optional. Their principal name from their provider's SSO will appear as a read-only value.

By default, the principal name needs to be in an email-like format, such as a eduPersonPrincipalName value scoped to their institution. Thus, each institution needs to release ePPN or a similarly unique value in their SAML assertion. However, this requirement can be turned off via properties, if you are certain that external principals won't overlap with your institution's login ids.

After submitting the form, there isn't much for the user to see, unless some groups are made globally visible. Grouper can be set up to automatically add new users to one or more groups. If you wish to initialize a folder for each new user, set grouper-ui.properties value grouperUi.autoCreateUserFolderOnLogin to true, and grouperUi.autoCreateUserFolderName to a folder pattern.

## Setup

Properties relevant to self-registration

| config file | key | default value | description |
| --- | --- | --- | --- |
| grouper-ui.properties | externalMembers.enabledRegistration | false | if the registration screen is enabled |
| grouper-ui.properties | grouperUi.autoCreateUserFolderOnLogin | false | if true, when a user logs in, a folder will be created and granted to the user if not already there |
| grouper-ui.properties | grouperUi.autoCreateUserFolderName | none | set a folder for the user. you can use EL here based on the subject, e.g. users:folders:${subject.id} or users:folders:${subject.getAttributeValue('identifier')} |
| grouper.properties | externalSubjects.validateIndentiferLikeEmail | true | make sure the identifier when logging in is like an email address or eppn, e.g. username@school.edu |
| grouper.properties | externalSubjects.autoaddGroups |  | put some group names comma separated for groups to auto add subjects to |
