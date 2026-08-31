---
title: "Page to view/manage lifecycle events on a group"
space: Grouper
pageId: 28548852
version: 6
lastUpdated: 2026-07-01T05:43:22.491Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548852/Page+to+view+manage+lifecycle+events+on+a+group
---

This is a new page that allows admins of a group (or sysadmins to see all) to view and manage *current* lifecycle events on a group. Just show date, hours and minutes AM/PM

Under Miscellaneous in the top section: User lifecycle events. Move the existing one to Administration in bottom called "User lifecycle admin".

See a table where each row is a user/group pair (only for direct memberships).

Two attribute marker name/value pairs for user lifecycle:

1. in-flight: name value pairs for the event id, timestamp. Only added if theres a user lifecycle grace period and then auto remove
2. history: if they approve user should stay in group:
  
  1. Remove the in-flight
  2. Add history attribute on membership: who approved it, and when

Above the table have a "Keep selected users" (should have a confirm javascript) (remove inflight, add history), "Remove selected users" (should have a confirm javascript) (remove membership)

Columns

1. Checkbox
2. Group (short name with link)
3. Subject
4. Date and time of lifecycle event. Based on grouper_lifecycle_event.event_micros
5. English description of lifecycle event. 
  
  1. Look at config of lifecycle event, two groups for each dictionary item to see if user see it. Batch up and see which privilege group, unprivileged (no group to check) the user is in and cache that result to show the user the best label for each row.
  2. Use privileged if user in group, unprivileged if not
6. When the membership will be removed if that's the action? userLifecycleMshipInFlightMicrosExpire (display timestamp in future). If that attribute is not there this cell would be blank

Table sort: By removal date.

If there are multiple in-flight assignments on a membership, then the removal date should be the soonest. And the event date and description should be based on highest change magnitude. If there are multiple with the highest change magnitude, then it should be based on the one with the soonest removal date.

LATER

The "Group actions" dropdown on a group will have a new option called "Lifecycle events" under Administration. This is visible to updaters/admins of the group.

Clicking that will take the user to a new page that displays all the current lifecycle events on the group based on memberships that currently have userLifecycleMshipInFlightMarker assigned. The data will be displayed in a table with the following columns:

I assume the attribute userLifecycleMshipInFlightLifecycleEventId is linked with the table/column grouper_lifecycle_event.internal_id. Yes

What happens if the row in the table doesn't exist? Just exclude it from the page? yes
