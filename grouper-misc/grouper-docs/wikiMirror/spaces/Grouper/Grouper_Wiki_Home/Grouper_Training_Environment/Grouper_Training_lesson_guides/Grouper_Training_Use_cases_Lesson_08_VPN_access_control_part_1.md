---
title: "Grouper Training - Use cases - Lesson 08: VPN access control part 1"
space: Grouper
pageId: 28544451
version: 24
lastUpdated: 2026-07-12T15:26:21.241Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544451/Grouper+Training+-+Use+cases+-+Lesson+08+VPN+access+control+part+1
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Learning Objectives

- Use group math and reference groups to analyze legacy authorization groups
- Translate natural language policy into Grouper digital policy
- Implement distributed access management
- Use Grouper to answer access management questions such as “who” and “why”

## Hands On

### Analyze legacy VPN authorization group

Gain insight into who has access to VPN based on the vpn_users LDAP group. We’ll do this by using well established reference groups for faculty, staff, and students. First review the legacy VPN authorization group in LDAP.

- Log in to [https://localhost:8443/phpldapadmin](https://localhost:8443/phpldapadmin) with username `cn=root,dc=internet2,dc=edu` and password `password`
- Expand ou=groups, and click on cn=vpn_users. Note the multi-valued "member" field.
- Create a `vpn` folder under the test folder.
- Create a `vpn_legacy` group to load the ldap group
- Add Loader settings to *vpn_legacy* (Group actions-> Loader -> Loader actions -> Edit loader configuration)
  
  - Loader: Yes, has loader configuration
  - Source Type: LDAP
  - Loader type: LDAP_SIMPLE
  - Server ID: demo
  - LDAP filter: `(cn=vpn_users)`
  - Subject attribute name: `member`
  - Search base DN: `ou=groups,dc=internet2,dc=edu`
  - Schedule: `0 15 10 * * ?`
  - Subject source ID: eduLDAP - EDU Ldap
  - Subject lookup type: subjectIdentifier
  - Search scope: SUBTREE_SCOPE
  - Priority:
  - Subject expression: `${loaderLdapElUtils.convertDnToSpecificValue(subjectId)}`
  - Require members in other group(s):
  - Customize failsafe: no
- Run Loader diagnostics (Loader actions -> Loader diagnostics -> Run loader diagnostics)
- Schedule loader (Loader actions → Schedule loader process)
- Run loader (Loader actions -> Run loader process to sync group)
- Review loader logs. How many subjects were added? (Loader actions -> View loader logs)
- Review *vpn_legacy* members

### Analyze legacy VPN members

We will use test composite groups to gain insight into the type of cohorts in vpn_legacy by intersecting it with well known reference groups for faculty and staff.

- Create group `test:vpn:vpn_facstaff` and make it a composite intersection of *ref:role:all_facstaff* and *test:vpn:vpn_legacy*. This represents existing faculty/staff with VPN access
- Create group `test:vpn:vpn_legacy_exceptions` and make it a complement group of *vpn_legacy* minus *all_facstaff*. This shows users who have VPN access but are not faculty or staff

Another way to get the non-Faculty/Staff users is to use a membership filter. Use advanced membership search Group filter on the *vpn_legacy* group to only see subjects that are not faculty/staff.

### Get a list of current exceptions

Before going live with the new group, we want to have the current exceptions looked at

- Grant READ on "test:vpn:vpn_legacy_exceptions" to Networking Staff (basis:hr:employee:dept:10906:staff)
  
  - Go to group
  - Privileges tab
  - Add
- Export the membership of test:vpn:vpn_legacy_exceptions (Group actions -> Export Members)

### Hands on: Get a list of current exceptions (better)

The membership export only retrieves the subject IDs and names. A superior version would add more LDAP attributes, plus would be kept up to date

- In vpn_legacy_exceptions, add a report (Group actions -> Reports -> Add Report)
  
  - Config type: GSH
  - Config format: CSV
  - Report name: `vpnLegacyExceptions`
  - File name: `vpnLegacyExceptions_$$timestamp$$.csv`
  - Description: `Members of the vpn legacy exceptions group, with extra LDAP fields`
  - Viewers group id: `basis:hr:employee:dept:10906:staff`
  - Quartz cron: 0 0 6 * * ?
  - Send email: Yes
  - Email subject: VPN Legacy Exceptions
  - Email body:  
    *`Report: $$reportConfigName$$`*  
    *`Description: $$reportConfigDescription$$`*
    
    *`Dear $$subjectName$$,`*
    
    *`Follow this link to the VPN legacy report: reportLink = $$reportLink$$`*
  - Store report if no results: No
  - GSH script:

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

  

- Actions → Run job now
- View the resulting email
