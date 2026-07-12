---
title: "Analyzing access"
space: GrIntDev
pageId: 48792664
version: 15
lastUpdated: 2026-07-12T17:46:16.192Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792664/Analyzing+access
---

In a future release of Grouper will have a screen which can analyze all aspects of a user's membership in a group

Grouper has a lot of data as far as point in time, user audit, access policy, provisioning action logs, enabled dates, disabled dates.

To see why a user does or does not have access and how it has changed over time takes a lot of work and knowledge about Grouper.

This effort is an attempt to consolidate information needed to troubleshoot access

## Membership audit screen (new)

Add a new screen which either pre-populates or allows input of a user and a group

Put the current "trace membership" information at the top

Group name: _____  
Entity: ____  
Show user audits? [X]  
Show pit? [X]  
Show membership trace? [X]

(submit)

Add functionality to "trace why no longer has membership" information that would be like trace membership but the last event that happened that affected the membership would be described

The rest of the screen is a timeline of applicable groups, audits, point in time, enabled/disabled dates, and provisioning information (will look better than this, just an example)

- 2/5/2021 17:51:17:03 - Jennifer Franklin was in jira-users until this point, then was removed
  
  - Event(s)
    
    - 2/5/2021 17:51:17:03 John Smith removed Jennifer Franklin from twostep group.
    - 2/5/2021 17:51:18:13 Jennifer Franklin effective membership removed from twostepEmployees group.
    - 2/5/2021 17:51:19:04 Provisioning removed Jennifer Franklin as a member of the employeeServiceProvider group in target LDAP
    - (note add enabled / disabled events too)
  - State
    
    - employeeOrServiceProvider (indirect) (since 12/01/2020) (more)
    - twostep (indirect) (until 2/5/2021, will be enabled on 2/12/2021 15:12:24) (more) (more would ajax get PIT for user and group and expand, maybe describe how group fits into the policy if its obvious)
      
      - Added 1/2/2003 – Removed 2/3/2004
      - Added 2/4/2005 – Removed 3/6/2007
    - jira-users (direct) (until 2/5/2021) (more)
    - jira-users in target LDAP (until 2/5/2021) (more)
- 1/2/2021 12:51:12:03 - Jennifer Franklin started membership in jira-users 
  
  - Event(s)
    
    - 1/2/2021 1/2/2021 12:51:12:03 Loader job added Jennifer Franklin as a member of the employeeServiceProvider group.
    - 1/2/2021 1/2/2021 12:51:12:03 Provisioning added Jennifer Franklin as a member of the employeeServiceProvider group in target LDAP
  - State
    
    - employeeOrServiceProvider (indirect) (since 5/12/2020) (more)
    - twostep (direct) (since 5/12/2020) (more)
    - jira-users (direct) (since 12/01/2020, will be disabled on 3/12/2010) (more)
    - jira-users in target LDAP (since 1/2/2021)

## Provisioning errors

If there are provisioning errors, show those

## Custom UI elements

If there are custom UI elements for this group, show those too (in a more friendly way than this)

## Which groups are shown?

Look and see the applicable groups for a policy. Go through point in time and spider through looking for group paths to the overall group.

If that list is a dozen or less groups, so all of them in the state at each event.

If that list if more than a dozen groups, then only show groups that the user is or ever has been a member of (i.e. relevant groups)

## Membership audit

Membership audit for a subject should explicitly describe what it does when clicked from a group (currently confusing)

Membership audit from a subject should include PIT information (optional)

## Privileges

Users using the screen would need to be able to READ groups in the results, or they will be redacted and the user will know not all data is shown

## Visualization (stems)

Allow an input of a subject (not in the settings section)

Groups should have a (y) or (n) for if the user is a member of the group if the user can READ that group

The analyze access screen above should have a button link to visualization and vise versa

## Visualization (groups)

Allow an input of a subject (not in the settings section)

Note the groups display might be different depending on how the access policy has changed (use same logic as above)

Allow a drop down which has all the event horizons when things change (use the same logic as the analyzer memberships screen) where an event is things that happen in the few minutes

- 2022/03/01 2:15 PM - present (is member of group)
- 2022/02/15 6:14 AM - 2022/03/01 2:15 PM (is NOT member of group)
- 2021/11/15 3:45 AM - 2022/02/15 6:14 AM 2:15 PM (is member of group)
- until 2021/11/15 3:45 AM (is NOT member of group)

Note, the time component will display after the user is selected. If there is no user then no time component will be accepted

## Audit buttons

The audit buttons right now are confusing to know where to go. Maybe some options are not even known by users

Have one audit button, and an intermediary screen explaining what each audit button is for

The trace membership button would go to this new screen

Trace membership would be available in PIT views of results
