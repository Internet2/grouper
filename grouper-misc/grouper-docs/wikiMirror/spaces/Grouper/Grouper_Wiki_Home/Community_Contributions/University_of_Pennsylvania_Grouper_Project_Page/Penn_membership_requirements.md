---
title: "Penn membership requirements"
space: Grouper
pageId: 28544127
version: 3
lastUpdated: 2026-07-01T05:48:51.363Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544127/Penn+membership+requirements
---

This is documentation for easily configuring [membership requirements](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549482/Grouper+membership+eligibility+requirements) on groups or folders. This is for immediate memberships. Groups can still be added to groups and effective memberships are not checked.

1. Ineligible members will be vetoed on add in UI or WS
2. People who are no longer eligible (i.e. not an employee anymore) will be remove immediately by change log consumer
3. Nightly a full check will be made to make sure all immediate members are eligible

## Who is allowed to use this

Different membership requirements can be configured by different populations. Note: any admin can assign requirements

| Requirement | Who can assign |
| --- | --- |
| Active at Penn | Power users |
| Penn employee | Power users |
| ISC (IT dept) | ISC employees |
| Pennant team members (Banner team) | Pennant team members |

## Make sure you are ready for the requirement of group

See what will change if you apply the requirement in the membership screen. **Note: ignore groups listed, those will not be removed, only other types of subjects**

Click "Advanced"

## Make sure you are ready for the requirement of folder via UI

This only works if the number of ineligible memberships is not very large

Look at memberships in a folder.

Find which will be removed. **Note: ignore groups listed, those will not be removed, only other types of subjects**

## Make sure you are ready for the requirement of folder via report

Have the Grouper admin run a SQL report and export as CSV or make a Grouper report

```
select
  gmlv.group_name,
  gm.subject_id,
  gm.description
from
  grouper_memberships_lw_v gmlv,
  grouper_members gm
where
  gmlv.list_name = 'members'
  and gmlv.group_name like 'penn:isc:ait:apps:outsystems:groups:%'
  and gmlv.member_id = gm.id
  and gmlv.subject_source = 'pennperson'
  and not exists (
  select
    1
  from
    grouper_memberships_lw_v gmlv2
  where
    gmlv2.list_name = 'members'
    and gmlv2.group_name = 'penn:community:activeNonAlumniWithPennname'
    and gmlv2.member_id = gmlv.member_id)
```

## Assign a requirement to group

Edit the group (need to be a group admin), assign the requirement

## Assign a requirement to a folder

There is currently not a folder edit way to assign this, but can do with attributes (need to be a folder admin)

## Effects of adding an ineligible member by UI

The adds will be vetoed

## Effects of adding an ineligible member by WS

An exception will be thrown and the WS will return an error

```
[mchyzer@flash pennGroupsClient-2.6.0]$ java -jar grouperClient-2.6.13.jar --operation=addMemberWs --groupName=test:testGroup1 --subjectIds=10035755
Error with grouper client, check the logs: Bad response from web service: resultCode: PROBLEM_WITH_ASSIGNMENT, There were 0 successes and 1 failures of users added to the group.
Error 0, result index: 0, code: EXCEPTION, message: edu.internet2.middleware.grouper.hooks.logic.HookVeto: veto.membershipVeto.customComposite.vetoRequireActive: User is not eligible to be in this group since they are not in: penn:community:activeNonAlumniWithPennname,
, group name: test:testGroup1, subject: Subject id: 10035755, sourceId: pennperson, field: members
	at edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireMembershipHook$1.callback(MembershipRequireMembershipHook.java:115)
	at edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:1000)
	at edu.internet2.middleware.grouper.GrouperSession.internal_callbackRootGrouperSession(GrouperSession.java:1069)
	at edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireMembershipHook.checkMembershipEligibility(MembershipRequireMembershipHook.java:95)

```

## (Grouper admin) add a new membership requirement

1. If the new requirement is controlled by the same population, re-use an existing attribute definition, otherwise add another one
2. Configure who can use it with attribute definition privileges
3. Create an attribute name
4. Configure externalized text for this requirement in grouper.text.en.us.properties (configured dynamically in the database)
  
  
  ```
  veto.membershipVeto.customComposite.vetoRequireEmployee = Only Penn active employees can be group members
  ```
5. Configure the membership requirement and group edit screen in grouper.properties (configured dynamically in the database)
  
  
  ```
  grouper.membershipRequirement.requireEmployee.uiKey = vetoRequireEmployee
  grouper.membershipRequirement.requireEmployee.attributeName = penn:etc:attribute:membershipRequirement:membershipRequirementEmployee
  grouper.membershipRequirement.requireEmployee.requireGroupName = penn:community:employeeOrContractorIncludingUphs
  
  groupScreen.attribute.requireEmployee.attributeName = penn:etc:attribute:membershipRequirement:membershipRequirementEmployee
  groupScreen.attribute.requireEmployee.label = Require employee
  groupScreen.attribute.requireEmployee.description = Only Penn employees can be group members.  Ineligible people will be vetoed or removed.
  groupScreen.attribute.requireEmployee.index = 2
  
  
  ```
6. Wait until caches clear (few minutes)
