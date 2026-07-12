---
title: "USDU delete subjects after unresolvable for X days"
space: Grouper
pageId: 28554830
version: 15
lastUpdated: 2026-07-01T05:39:32.846Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554830/USDU+delete+subjects+after+unresolvable+for+X+days
---

There are several improvements in [USDU](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548392/Universal+Subject+Daemon+Utility+USDU) for a patch in 2.4.0 API.

## Attest unresolvables

After you get the patches that have better daemon and UI subject resolution, you should setup attestation for an admin to review it

## New attributes

Attributes on members. These will get updated during USDU in readonly or readwrite mode.

**Attribute definitions**

| Definition | Assigned To | Purpose | Value | Cardinality |
| --- | --- | --- | --- | --- |
| subjectResolutionDef | member | marker on member | marker | Single assign |
| subjectResolutionValueDef | member assignment | name/value pairs | string | Single assign, single valued |

****

**Attribute names**

| Name | Definition | Value |
| --- | --- | --- |
| subjectResolutionMarker | subjectResolutionDef | <none> (assigned to unresolvable subjects) |
| subjectResolutionResolvable | subjectResolutionValueDef | false if this subject is currently unresolvable (as of last check).    If the subject is resolvable, remove subjectResolutionMarker and metadata  Note as of 2.5.30, this attribute has been moved to the grouper_members table as a separate column. |
| subjectResolutionDateLastResolved | subjectResolutionValueDef | yyyy/mm/dd If this subject has a date and is unresolveable, leave it. if this subject doesnt have a date, and is unresolvable, then set to currentDate. |
| subjectResolutionDaysUnresolved | subjectResolutionValueDef | 7 - the number of days from current date minus dateLastResolved. |
| subjectResolutionLastChecked | subjectResolutionValueDef | yyyy/mm/dd the date this subject was last checked. When the USDU runs, if this subject is current unresolvable, then set to currentDate |
| subjectResolutionDeleted | subjectResolutionValueDef | true when this subject is marked as deleted. All the memberships are removed at this point.Values from subjectResolutionResolvable, subjectResolutionDaysUnresolved and subjectResolutionDateLastChecked are also cleared at this point.  Note as of 2.5.30, this attribute has been moved to the grouper_members table as a separate column. |
| subjectResolutionDeleteDate | subjectResolutionValueDef | time when this subject is marked as deleted. |

## Change USDU so that instead of deleting unresolvables now, it waits until after X days

```
grouper.base.properties

# global across all sources: Don't do anything if more than this number of unresolvable subjects are found
# {valueType: "integer", required: true}
usdu.failsafe.maxUnresolvableSubjects = 500

# global across all sources: if the first X subjects should be removed but stop after that limit: usdu.failsafe.maxUnresolvableSubjects
usdu.failsafe.removeUpToFailsafe = false

# global across all sources: only delete unresolvables if unresolvable for 30 days.  false or 0 means remove now
# {valueType: "integer", required: false}
usdu.delete.ifAfterDays = 30

# local to one source supersedes the global settings: source ID
# {valueType: "string", required: true, regex: "^usdu\\.source\\.([^.]+)\\.sourceId$"}
# usdu.source.someLabel.sourceId = someSourceId

# local to one source supersedes the global settings: Don't do anything if more than this number of unresolvable subjects are found
# {valueType: "integer", required: true, regex: "^usdu\\.source\\.([^.]+)\\.failsafe\\.maxUnresolvableSubjects$"}
# usdu.source.someLabel.failsafe.maxUnresolvableSubjects = 500

# local to one source supersedes the global settings: if the first X subjects should be removed but stop after that limit: usdu.failsafe.maxUnresolvableSubjects
# {valueType: "integer", required: true, regex: "^usdu\\.source\\.([^.]+)\\.failsafe\\.removeUpToFailsafe$"}
# usdu.source.someLabel.failsafe.removeUpToFailsafe = false

# local to one source supersedes the global settings: only delete unresolvables if unresolvable for 30 days.  false or 0 means remove now
# {valueType: "integer", required: true, regex: "^usdu\\.source\\.([^.]+)\\.delete\\.ifAfterDays$"}
# usdu.source.someLabel.delete.ifAfterDays = 30
```

Currently there is a readonly mode. There is a mode that deletes unresolvables. This new option will adjust the "delete unresolvables" but the unresolvables need to be unresolvable for X days.

Make sure subject deletion is audited. When a subject becomes unresolvable, add an audit. When a subject goes from unresolvable to resolvable, add an audit. When a subject is deleted, add an audit.

## USDU as daemon

Currently USDU is invoked in the Grouper report (readonly), and on the command line. USDU should no longer run during the grouper report. USDU will now run an an "otherjob" configured every saturday at 1am (in grouper-loader.base.properties). The mode will be read/write or will consider the "ifAfterDays"

## USDU on UI

Under Miscellaneous on UI, for grouperSysAdmins, show a "Subject resolution" link.

- On that page, show stats on subject resolution. How many unresolvables per source.
- Allow subjects to be searched. Show if they are resolvable or unresolvable.
- Allow unresolvables to be shown in a paged list of 200. Show a table with when last resolved, when last checked, days unresolved, and when they will be deleted
- Show audits for subject deletion (paged list of 200)
- Have a link to the daemon log screen for this daemon

## Screenshots
