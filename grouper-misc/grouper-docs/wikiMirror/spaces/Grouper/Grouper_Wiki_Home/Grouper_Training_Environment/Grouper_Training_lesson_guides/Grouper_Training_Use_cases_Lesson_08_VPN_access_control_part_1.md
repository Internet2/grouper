---
title: "Grouper Training - Use cases - Lesson 08: VPN access control part 1"
space: Grouper
pageId: 28544451
version: 27
lastUpdated: 2026-11-18T20:22:04.607Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544451/Grouper+Training+-+Use+cases+-+Lesson+08+VPN+access+control+part+1
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives

- Use group math and reference groups to analyze legacy authorization groups
- Translate natural language policy into Grouper digital policy
- Implement distributed access management
- Use Grouper to answer access management questions such as “who” and “why”

## Hands on

### Analyze the legacy VPN authorization group

Gain insight into who has access to VPN based on the vpn_users LDAP group. We’ll do this by using well established reference groups for faculty, staff, and students. First review the legacy VPN authorization group in LDAP.

1. Open phpLDAPadmin:
  
  
  ```
  https://localhost:8443/phpldapadmin
  ```
2. Log in. "Login DN":
  
  
  ```
  cn=root,dc=internet2,dc=edu
  ```
3. "Password":
  
  
  ```
  password
  ```
4. Expand "ou=groups" and click "cn=vpn_users". Note the multi-valued "member" field.

### Create the vpn folder and the vpn_legacy group

1. In the left menu, click "Browse folders", click "Root", then click the folder "test".
2. Click "Folder actions" → "Create new folder".
3. "Folder name":
  
  
  ```
  vpn
  ```
4. Click "Save". You land on the new folder page "test:vpn".
5. Click "Folder actions" → "Create new group".
6. "Group name":
  
  
  ```
  vpn_legacy
  ```
7. Click "Save". You land on the new group page "test:vpn:vpn_legacy".

### Load the LDAP group into vpn_legacy

1. On the group "test:vpn:vpn_legacy", click "Group actions" → "Loader".
2. Click "Loader actions" → "Edit loader configuration".
3. "Loader": select "Yes, has loader configuration".
4. "Source type": select "LDAP".
5. "Loader type": select "LDAP_SIMPLE".
6. "Server ID": select "demo".
7. "LDAP filter":
  
  
  ```
  (cn=vpn_users)
  ```
8. "Subject attribute name":
  
  
  ```
  member
  ```
9. "Search base DN":
  
  
  ```
  ou=groups,dc=internet2,dc=edu
  ```
10. "Schedule":
  
  
  ```
  0 15 10 * * ?
  ```
11. "Subject source ID": select "eduLDAP - EDU Ldap".
12. "Subject lookup type": select "subjectIdentifier".
13. "Search scope": select "SUBTREE_SCOPE".
14. "Subject expression":
  
  
  ```
  ${loaderLdapElUtils.convertDnToSpecificValue(subjectId)}
  ```
15. "Customize failsafe": select "No, do not customize the failsafe".
16. Click "Save".

### Run the loader

1. Click "Loader actions" → "Loader diagnostics", then click "Run loader diagnostics".
2. Click "Loader actions" → "Schedule loader process".
3. Click "Loader actions" → "Run loader process to sync group".
4. Click "Loader actions" → "View loader logs". How many subjects were added?
5. Click the "Members" tab and review the members of vpn_legacy.

### Analyze legacy VPN members

We will use test composite groups to gain insight into the type of cohorts in vpn_legacy by intersecting it with well known reference groups for faculty and staff.

#### Faculty and staff with VPN access

1. Go to the folder "test:vpn" (Browse folders → Root → test → vpn).
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  vpn_facstaff
  ```
4. Click "Save". You land on the new group page "test:vpn:vpn_facstaff".
5. Click "Group actions" → "Edit composite".
6. "Composite": select "Yes".
7. "First factor group": paste this and pick the group from the list:
  
  
  ```
  ref:role:all_facstaff
  ```
8. "Operation": select "Intersection".
9. "Second factor group": paste this and pick the group from the list:
  
  
  ```
  test:vpn:vpn_legacy
  ```
10. Click "Save".

#### VPN users who are not faculty or staff

1. Go to the folder "test:vpn" (Browse folders → Root → test → vpn).
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  vpn_legacy_exceptions
  ```
4. Click "Save". You land on the new group page "test:vpn:vpn_legacy_exceptions".
5. Click "Group actions" → "Edit composite".
6. "Composite": select "Yes".
7. "First factor group": paste this and pick the group from the list:
  
  
  ```
  test:vpn:vpn_legacy
  ```
8. "Operation": select "Complement".
9. "Second factor group": paste this and pick the group from the list:
  
  
  ```
  ref:role:all_facstaff
  ```
10. Click "Save".

Another way to get the non-faculty/staff users is a membership filter: on the group "test:vpn:vpn_legacy", "Members" tab, use the "Group filter" in the advanced membership search to only see subjects that are not in ref:role:all_facstaff.

### Get a list of current exceptions

Before going live with the new group, we want to have the current exceptions looked at.

#### Let Networking staff read the exceptions group

1. Go to the group "test:vpn:vpn_legacy_exceptions".
2. Click the "Privileges" tab, then click "Add members".
3. In "Member name or ID", paste this and pick "Networking staff" from the list:
  
  
  ```
  basis:hr:employee:dept:10906:staff
  ```
4. "Assign these privileges": check "READ".
5. Click "Add".

#### Export the members

1. On the group "test:vpn:vpn_legacy_exceptions", click "Group actions" → "Export members".
2. Click "Download now".

### Get a list of current exceptions (better)

The membership export only retrieves the subject IDs and names. A better version adds more LDAP attributes, and is kept up to date.

1. On the group "test:vpn:vpn_legacy_exceptions", click "Group actions" → "Reports".
2. Click "Report actions" → "Add report".
3. "Config type": select "GSH".
4. "Config format": select "CSV".
5. "Report name":
  
  
  ```
  vpnLegacyExceptions
  ```
6. "File name":
  
  
  ```
  vpnLegacyExceptions_$$timestamp$$.csv
  ```
7. "Description":
  
  
  ```
  Members of the vpn legacy exceptions group, with extra LDAP fields
  ```
8. "Viewers group id":
  
  
  ```
  basis:hr:employee:dept:10906:staff
  ```
9. "Quartz cron":
  
  
  ```
  0 0 6 * * ?
  ```
10. "Send email": select "Yes".
11. "Email subject":
  
  
  ```
  VPN Legacy Exceptions
  ```
12. "Email body":
  
  
  ```
  Report: $$reportConfigName$$
  Description: $$reportConfigDescription$$
  
  Dear $$subjectName$$,
  
  Follow this link to the VPN legacy report: reportLink = $$reportLink$$
  ```
13. "Store report if no results": select "No".
14. "GSH script": paste the script below.
  
  
  ```groovy
  Group g = gsh_builtin_gshReportRuntime.ownerGroup
  GrouperReportData grouperReportData = gsh_builtin_gshReportRuntime.grouperReportData
  
  grouperReportData.headers = ['Row', 'ID', 'UID', 'Name', 'Email']
  grouperReportData.data = new ArrayList<String[]>()
  
  g.members.eachWithIndex { it, i ->
      String[] row = [
              i+1,
              it.subject.getAttributeValue('employeenumber'),
              it.subject.getAttributeValue('uid'),
              it.subject.getAttributeValue('cn'),
              it.subject.getAttributeValue('mail'),
      ]
  
      grouperReportData.data << row
  }
  ```
15. Click "Save".
16. On the report row, click "Actions" → "Run job now".
17. Check the resulting email in the mail server (open it from the GTE jump page).
