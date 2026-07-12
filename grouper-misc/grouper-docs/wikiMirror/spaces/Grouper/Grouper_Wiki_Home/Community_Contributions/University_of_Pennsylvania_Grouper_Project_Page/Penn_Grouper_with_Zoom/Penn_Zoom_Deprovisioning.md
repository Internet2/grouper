---
title: "Penn Zoom Deprovisioning"
space: Grouper
pageId: 28549445
version: 5
lastUpdated: 2026-07-01T05:42:01.342Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549445/Penn+Zoom+Deprovisioning
---

Requirements

- 30 day grace period from "can log in to zoom" (which means in population or has override)
- Take the loaded "user types", and look for user type "2" which means "licensed"
- Make a group of licensed minus canLogIn, and this is the group of people to deprovision
- Load in Zoom user data into a database table
- Grouper Report will let zoom admin
  
  - Download a CSV with user type set to 1 for users that should be deprovisioned now (email addresses that link to a Penn person, who hasnt had access to zoom for 30 days)
  - Download a CSV with user type set to 1 for users who are not registered as non-human and who arent link to a person (non human). i.e. these are non SSO accounts by personal email address
  - Download a CSV of registered non human email addresses (and some auditing)
  - Download a CSV of email links to users (non EPPN email addresses where the email address is not in the directory but we know who the user is)
- GSH template to allow the zoom admin to manage the non human account registration
- GSH template to allow the zoom admin to manage the email linking to person

See more about the [GSH template here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548913/Grouper+custom+template+via+GSH+zoom+deprovisioning)

1. The Grouper zoom connector loads user types (into groups) and raw user data into a table. This is a scheduled job (runs every hour)
2. The "Zoom can log in group" is users who pass through the SSO IdP. To be a licensed user you need an active affiliation, and sometimes a sponsor, and cant be restricted (i.e. since you are in a subaccount instead)
3. User type "2" (loaded group) is licensed user.
4. The Grouper feature "recent membership" keeps a group in sync with the "can log in group" and also includes people who are not in the "can log in" group but were in the last 30 days
5. For SSO users, they email address in zoom is the EPPN in Grouper, which is a subject identifier, so we can match most of the users
6. There are email addresses in Zoom which are "non-human" and should be allowed to exist. We have an ad hoc (manual) list of non human accounts managed by a GSH template
7. If a user has an email address registered in the directory, and that email address is the email for the zoom account, we can match that email to the user
8. Otherwise we have a generic (can be used for things other than zoom) table of ad hoc (manual) email addresses linked to subjects (subjectId which is pennId i.e. employee number)

## Zoom deprovisioning template

Zoom admins can run this

Use this link: https://grouper.server.institution.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemId=319a44d13d754dfea820571dd2709147

Click More actions → Zoom deprovisioning

## Zoom run reports

This takes a few minutes since it will do a fresh load of zoom data. If you want to read a past report you do not need to do this. Reports are scheduled weekly. You need to run reports if you change the list of non-human accounts or email links

## View zoom reports

There are four reports

1. zoomDeprovisionNow: these are email addresses that match Penn users who have not been in the zoom allowedToLogIn group in the last 30 days. This is in the format of a zoom upload with the status set to 1 (basic account)
2. zoomDeprovisionUnresolvable: these are email addresses that do not match Penn users and are not known non-user emails. This is in the format of a zoom upload with the status set to 1 (basic account)
3. zoomDeprovisioningEmailList: current list of email address → penn person (override since not matched in directory)
4. zoomDeprovisioningNonHuman: list of email addresses which are known to be not human and should not be in the zoomDeprovisionUnresolvable report

The main deprovisioning report looks like this

```
Query: select * from penn_zoom_deprovision_now_v
View:
create or replace
view penngrouper.penn_zoom_deprovision_now_v as
select
  gpzu.email as "Email",
  null::text as "First Name",
  null::text as "Last Name",
  null::text as "Phone Number",
  null::text as "Department",
  null::text as "Manager",
  1 as "User Type",
  null::text as "Large Meeting",
  null::text as "Webinar",
  null::text as "Zoom Events",
  null::text as "Job Title",
  null::text as "Location"
from
  grouper_prov_zoom_user gpzu
where
  (exists (
  select
    1
  from
    grouper_memberships_lw_v gmlv
  where
    gmlv.group_name::text = 'penn:isc:ait:apps:zoom:service:ref:zoomDeprovisioning:zoomDeprovisionNow'::text
    and gmlv.member_id::text = gpzu.member_id::text
    and gmlv.list_name::text = 'members'::text));
```

The unresolvable view looks like this

```
create or replace
view penngrouper.penn_zoom_deprovision_unr_v as
select
  gpzu.email as "Email",
  null::text as "First Name",
  null::text as "Last Name",
  null::text as "Phone Number",
  null::text as "Department",
  null::text as "Manager",
  1 as "User Type",
  null::text as "Large Meeting",
  null::text as "Webinar",
  null::text as "Zoom Events",
  null::text as "Job Title",
  null::text as "Location"
from
  grouper_prov_zoom_user gpzu
where
  gpzu.member_id is null
  and not (exists (
  select
    1
  from
    penn_zoom_non_human pznh
  where
    lower(pznh.email::text) = lower(gpzu.email::text)));
```

## Non-human accounts

You can add or remove non human accounts with the template. Non human accounts will not be included in the unresolvable report

## Email link

If an email address which is not an eppn and not in the directory, is identified to belong to a penn user, you can map those emails to a pennkey or pennid. Linked emails will not be in the unresolvable report and will be in the deprovisionNow report (if not eligible for 30 days)

## Grace period group

We already had a "can log in group". Add a group that includes that population and the recent members by 30 days

## User type loaded groups

The Grouper zoom provisioner loads user types into Grouper

## Users to deprovision

Make a group of all users that have licenses who cannot log in

Make a group of users who have licenses who havent been able to log in for 30 days (deprovision these users)
