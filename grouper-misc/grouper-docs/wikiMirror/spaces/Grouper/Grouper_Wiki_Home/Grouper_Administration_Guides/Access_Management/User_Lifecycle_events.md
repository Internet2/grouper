---
title: "User Lifecycle events"
space: Grouper
pageId: 28544885
version: 44
lastUpdated: 2026-07-12T15:26:30.488Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544885/User+Lifecycle+events
---

In Grouper v6+, the User Lifecycle events feature allows Grouper to **track lifecycle events and act on them**. Generally this is for manual groups that are used in a policy.

> The User Lifecycle events feature allows life cycle rules to be centralized in one place.  
> Grouper [Hooks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545347/Grouper+Hooks) or [Rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules) can also be used to accomplish much of what User Lifecycle Events provides.

**Where to find it:** from the Grouper UI top nav, *Miscellaneous → User lifecycle admin*. From there the four sub-screens are *User lifecycle events*, *User lifecycle actions*, *User lifecycle policies*, and *User lifecycle policy parts*.

**Privilege required to configure:** Grouper sysadmin (typically members of `etc:sysadmingroup`). Non-sysadmins do not see the User lifecycle admin menu entry.

**Lifecycle event:** a change in a user’s relationship to the institution. Each institution defines its own events based on group membership or data field/row changes. Examples:

- The user no longer works at the institution
- An affiliation changes (faculty, student, staff)
- A supervisor changes
- A work status changes (full time vs. part time)
- An organization, department, or school changes

**Key concepts:**

### **Membership lifecycle event** definition (configure as many as you need)

- **Config id** — String, required. Alphanumeric key.
- **Name** — String, required. Short human-readable name shown in dropdowns.
- **Description** — String, textarea, required. Longer explanation of when this event fires.
- **Change magnitude** — Float, required, default `0`. Ranks how significant this event is when multiple events fire for the same user. Suggested scale: `100` leaves institution, `50` leaves department, `10` changes title.
- **Trigger** — Dropdown, required. Picks which change in Grouper fires this event. The fields shown below it change based on the selected value:
  
  - `groupUserAdd` — user was added to a specific group. Shows: *Group id/name* (path or UUID).
  - `groupUserRemove` — user was removed from a specific group (e.g. an Active-employees group). Shows: *Group id/name*.
  - `groupUserRemoveFromFolder` — user was removed from *any* group inside a folder (recursively). Shows: *Folder id/name*.
  - `dataFieldRemove` — a value on a data field (or data-row column) was removed. Shows: *Data field config* (dropdown of `GrouperDataField`).
  - `dataRowRemove` — an entire data row was removed. Shows: *Data row config* (dropdown of `GrouperDataRow`).
- **Description shown to privileged viewers** — Template, textarea, required. Rendered when the viewer is a member of the privileged-viewers group below. Plain text passes through verbatim; each `${...}` block is evaluated as a JEXL script. Available variables depend on the trigger:
  
  - `groupUserAdd` / `groupUserRemove`: `groupName`, `groupDisplayName`, `groupExtension`, `groupDisplayExtension`, `groupDescription`
  - `groupUserRemoveFromFolder`: the five `group*` variables above (for the specific child group) plus `stemName`, `stemDisplayName`, `stemExtension`, `stemDisplayExtension`, `stemDescription`
  - `dataFieldRemove`: `configId`, `value`
  - `dataRowRemove`: `configId`
  - Always available: `grouperUtil` (e.g. `grouperUtil.escapeHtml(value, true)`)
  
  Examples:
  
  - `Job loss` — plain text passthrough
  - `Job loss from ${groupDisplayExtension}`
  - `Job loss from ${grouperUtil.escapeHtml(groupDisplayExtension, true)}` — HTML-escape variables that may contain user-supplied text
  - `Job loss from ${groupDisplayExtension}${groupDescription.contains('Restricted') ? ' (sensitive)' : ''}`
- **Group of privileged viewers** — String, required. Full group name (path) or UUID. Members of this group see the privileged template above; other viewers see the unprivileged template below.
- **Description shown to other viewers** — Template, textarea, required. Same template rules as above, but typically kept generic to avoid leaking sensitive group/folder/attribute names. Example: `Job loss`.

**Save-time validation**: when the event config is saved, both templates are evaluated against stub `Group`/`Stem`/`GrouperDataField`/`GrouperDataRow` objects that match the chosen trigger, in strict mode (`lenient=false`). A typo like `${groupDisplayExtensoin}` fails at save time with a field-level error naming the missing variable, so it cannot ship to the daemon.

**Storage**: the configs and rendered descriptions are cached in two tables with a full daemon and an incremental daemon (described below):

- `grouper_lifecycle_event_config` — columns: `internal_id`, `config_id`, `group_internal_id`, `data_field_internal_id`, `data_row_internal_id`, `stem_id_index`, `created_on_micros`
- `grouper_lifecycle_event` — columns (all ≤ 30 chars):
  
  - `internal_id` — PK
  - `grpr_lcycl_evnt_cnfg_intrnl_id` — FK to `grouper_lifecycle_event_config.internal_id`
  - `member_internal_id` — FK to `grouper_members.internal_id`
  - `event_micros` — micros since 1970, sourced from the relevant `*_hst` table
  - `ntrl_lng_priv_dic_intrnl_id` — FK to `grouper_dictionary` holding the rendered privileged text
  - `ntrl_lng_unpriv_dic_intrnl_id` — FK to `grouper_dictionary` holding the rendered unprivileged text

**Daemons**: enabling User Lifecycle events activates two daemons, full and incremental. Job names (look for these in the daemon log / job status screen):

- `OTHER_JOB_userLifecycleFullDaemon` — full sync, looks back roughly one year of history
- `OTHER_JOB_userLifecycleIncrementalDaemon` — incremental sync, picks up new events since the last run
- `OTHER_JOB_groupPolicyUserLifecycleFullDaemon` — runs the configured actions (email, remove user, end-date membership) for the events the full/incremental daemon recorded

Each daemon looks at all event configs and queries the right history table for the chosen trigger:

- `groupUserAdd` / `groupUserRemove` → `grouper_sql_cache_mship_hst`
- `groupUserRemoveFromFolder` → `grouper_sql_cache_mship_hst` (joined to all child groups of the folder)
- `dataFieldRemove` with `fieldDataStructure = attribute` → `grouper_data_field_assign_hst`
- `dataFieldRemove` with `fieldDataStructure = rowColumn` → `grouper_data_row_field_asn_hst`
- `dataRowRemove` → `grouper_data_row_assign_hst`

For each matching history row, the daemon evaluates the privileged and unprivileged templates against the variables for the trigger and stores the rendered strings in `grouper_dictionary`. If the underlying object (group, field, row) is later removed, the rendered text in the dictionary is preserved — the daemon does not rewrite stored descriptions when the source is gone.

**JEXL tester**: the Grouper JEXL tester has a `USER_LIFECYCLE_EVENT` script type with built-in examples per trigger (plain text, simple interpolation, conditional output, HTML-escape, group + stem, data field, data row). Use it to develop or troubleshoot a template against sample data before saving the event config.

**Deferred**:

- Free-form JEXL script trigger (in addition to the five trigger types above)
- Hierarchy/precedence when two events apply to the same user
- Time-buffer / debouncing across related events (e.g. position + job + relationship lost together)

### **Membership lifecycle action** definitions (the steps a policy can take)

- **Name** — String, required
- **Description** — String, textarea, required
- **Action type** — Dropdown, required:
  
  - `emailManager`
  - `emailUser`
  - `emailGroupAdmin`
  - `removeUserFromGroup`
  - `addEndDateOnMembership` — adds an attribute with an end date
- **`emailManager` options**:
  
  - Data field config id — dropdown, required (the field that names the manager)
  - Subject id / subject identifier / subject id_or_identifier — dropdown, required
  - Subject source — dropdown, optional
  - Email subject line — String, required. Supports `${...}` with `groupName`, `groupURL`, etc.
  - Email body — textarea, required. See *Email body details* below.
- **`emailUser` options**: email subject line + email body (same variables as above)
- **`emailGroupAdmin` options**: email subject line + email body (same variables as above)
- **`removeUserFromGroup` options**: none
- **`addEndDateOnMembership` options**:
  
  - Number of days in the future — int, required

#### **Email body details**

When the action type is `emailUser`, `emailManager`, or `emailGroupAdmin`, the daemon sends one email per recipient with bodies batched by event. The body is a template; `${...}` blocks evaluate as JEXL scripts and the following variable is available:

- `listOfRecordMaps` — a list of Java maps, one entry per event being batched. Each map contains:
  
  - `safeSubjectLifecycleUser` — the user whose lifecycle changed ([`SafeSubject`](https://github.com/Internet2/grouper/blob/GROUPER_5_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/subj/SafeSubject.java))
  - `safeSubjectRecipient` — the user receiving the email ([`SafeSubject`](https://github.com/Internet2/grouper/blob/GROUPER_5_BRANCH/grouper/src/grouper/edu/internet2/middleware/grouper/subj/SafeSubject.java))
  - `groupId`, `groupName`, `groupDisplayName`, `groupExtension`, `groupDisplayExtension`, `groupDescription`

Example body using GSH-style script directives outside `${...}`:

`$$ for (var recordMap : listOfRecordMaps) { Recipient: ${recordMap.get('safeSubjectRecipient').getName()} Subject of action: ${recordMap.get('safeSubjectLifecycleUser').getName()} $$ }`

### **Membership lifecycle policy** definitions (which actions run for which events)

- **Config id** — String, required
- **Name** — String, required
- **Description** — String, textarea, required
- **Is public** — Boolean, required, default `false`. Radio buttons.
- **Who can use this** — String, required (when `isPublic` is false or null). Group id or name. Viewers with READ on the group see the policy; viewers with EDIT cannot change the assignment if they can’t see the group.
- **Support instructions** — String, textarea, optional. How a removed user can request privileges back, otherwise the group owners are contacted.

### **Membership lifecycle policy part** definitions

- **Config id** — String, required
- **Lifecycle policy** — Dropdown, required
- **Number of lifecycle events** — Int 1–10, required, dropdown. Shows that many event-config dropdowns; each must be unique.
- **Number of lifecycle actions** — Int 1–10, required, dropdown. Shows that many action-config dropdowns; each must be unique.

Examples:

- *Medium security group*: if a user leaves the institution, assign a 3-day grace period and notify their manager. If a user did not leave the institution but lost a department (e.g. job or affiliation change), assign a 7-day grace period and notify the user and their old and new managers.
- *Department loss or leaves organization*: if a user loses a relevant affiliation (leaves a certain "Active" group) or leaves an organization (leaves any group in the org folder), notify the user’s managers and add a 7-day grace period.

### **Lifecycle change requirements** (assigned to groups or folders)

Lifecycle change requirements are assigned to groups or folders on the group edit screen as a radio button (default: none).

- Stored as an attribute on the group.
- One notification per user’s lifecycle event.
- If a user manually adds a direct membership and the target group has no lifecycle change requirement set, a note is shown asking them to set one (or explicitly choose “none”).
- Memberships carry an attribute capturing the lifecycle event and the previous end date (if applicable).
- A bulk-edit screen lets group managers add users back (optionally with the previous end date) or remove them.
- The attestation screen and email call out users with recent lifecycle events.
- Emails are batched per user per lifecycle event for the incremental run, and can be batched daily by recipient.

**Open questions:**

- What happens when a user is not in a required lifecycle-defining group (e.g. the UI tries to add a non-employee to an employees-only group)? The lifecycle requirement should veto the membership (like the existing membership-requirement mechanism), and the veto message should help the UI user add the person to a temporary employee-service-eligible group somewhere.
- Membership requirement on a group acting as a grace-period group.
- Veto vs. requirement of auto-remove.

**Example policy tiers** (illustration only — each institution defines its own policies and assigns them to groups via lifecycle change requirements):

| **Group security level (policy)** | **Employee leaves the institution**   (lifecycle event) | **Employee leaves dept**   (but stays at the institution) | **Employee leaves a position**   (but stays in same dept) |
| --- | --- | --- | --- |
| Low security   (lifecycle policy) | Membership set to expire in 5 days   (former) manager is alerted   (lifecycle action) | No actions | No actions |
| Medium security | 3-day grace period starts   (former) manager is alerted | Membership set to expire in 21 days   Employee is alerted | No actions |
| High security | Immediate membership removal   (former) manager is alerted | Membership set to expire in 7 days   Employee is alerted | Membership set to expire in 14 days   Employee is alerted |

### **Worked example: 7-day grace period when an employee leaves**

This walks through wiring up an event, an action, a policy, and a policy part for the scenario “when a user leaves the active-employee group, give them a 7-day grace period on their `adobeUsers` membership and email their manager.”

**1. Lifecycle event** (*User lifecycle events → Add user lifecycle event*)

- Config id: `leaveInstitution`
- Name: *Leave institution*
- Description: *User has left the institution (removed from the active-employee group)*
- Change magnitude: `100`
- Trigger: `groupUserRemove`
- Group id/name: `ref:affiliation:active_employee`
- Description shown to privileged viewers: `Job loss from ${groupDisplayExtension}`
- Group of privileged viewers: `etc:hrViewers`
- Description shown to other viewers: `Job loss`

**2. Lifecycle action** (*User lifecycle actions → Add user lifecycle action*)

- Config id: `gracePeriodSevenDays`
- Name: *7-day grace period*
- Description: *End-date the membership 7 days from the event*
- Action type: `addEndDateOnMembership`
- Number of days in the future: `7`

And a second action for the manager email:

- Config id: `emailFormerManager`
- Name: *Email former manager*
- Action type: `emailManager`
- Data field config id: `managerPennkey` (whichever data field on the lifecycle user names their manager)
- Subject id/identifier: `subjectIdentifier`
- Email subject line: `${listOfRecordMaps.get(0).get('safeSubjectLifecycleUser').getName()} left the institution`
- Email body: see *Email body details* above for the `$$ for ... $$ }` pattern.

**3. Lifecycle policy** (*User lifecycle policies → Add user lifecycle policy*)

- Config id: `mediumSecurity`
- Name: *Medium security*
- Description: *7-day grace + email manager when the user leaves the institution*
- Is public: `true`
- Support instructions: *To restore access during the grace period, contact the group owner.*

**4. Lifecycle policy part** (*User lifecycle policy parts → Add user lifecycle policy part*)

- Config id: `leaveInstitutionMedium`
- Lifecycle policy: `mediumSecurity`
- Number of lifecycle events: `1` → event 0: `leaveInstitution`
- Number of lifecycle actions: `2` → action 0: `gracePeriodSevenDays`, action 1: `emailFormerManager`

**5. Assign the policy to the target group**

On the `app:adobe:adobeUsers` group’s edit screen, set the lifecycle change requirement to `mediumSecurity`.

**What happens at runtime**

1. A user is removed from `ref:affiliation:active_employee` — either by a daemon or manually.
2. On its next run, `OTHER_JOB_userLifecycleFullDaemon` sees the removal in `grouper_sql_cache_mship_hst`, evaluates the privileged and unprivileged templates, and writes a row in `grouper_lifecycle_event` linking the user to the rendered text in `grouper_dictionary`.
3. `OTHER_JOB_groupPolicyUserLifecycleFullDaemon` picks up that event and applies the policy parts:
  
  - Adds an end-date attribute (7 days out) to the user’s `adobeUsers` membership.
  - Resolves the user’s manager via the `managerPennkey` data field and queues a batched email to that manager.
4. An admin viewing the `adobeUsers` group sees the lifecycle marker on the membership. Authorized viewers in `etc:hrViewers` see *Job loss from Active Employees*; everyone else sees *Job loss*.
5. If the manager doesn’t reapprove, the membership ends after 7 days.

### **On-disk config key patterns**

The UI is the supported way to configure these, but the values are stored in the database config table (`grouper_config`) and can also be set in `grouper.properties`. Property name patterns:

- **Event:** `grouperUserLifecycleEvent.<configId>.<property>`  
  where `<property>` is one of `name`, `description`, `changeMagnitude`, `trigger`, `groupUserAddGroup`, `groupUserRemoveGroup`, `groupUserRemoveFolder`, `groupUserRemoveDataFieldConfigId`, `groupUserRemoveDataRowConfigId`, `naturalLanguageDescriptionJexlPrivileged`, `naturalLanguageDescriptionJexlPrivilegedGroupIdOrName`, `naturalLanguageDescriptionJexlUnprivileged`.
- **Action:** `grouperUserLifecycleAction.<configId>.<property>`  
  where `<property>` is one of `name`, `description`, `actionType`, plus type-specific keys (`dataFieldConfigId`, `subjectIdIdentifier`, `subjectSource`, `emailSubjectLine`, `emailBody`, `numberOfDaysInTheFuture`).
- **Policy:** `grouperUserLifecyclePolicy.<configId>.<property>` — `name`, `description`, `isPublic`, `groupIdOrName`, `supportInstructions`.
- **Policy part:** `grouperUserLifecyclePolicyPart.<configId>.<property>` — `policy`, `numberOfLifecycleEvents`, `numberOfLifecycleActions`, plus indexed children `lifeCycleEvents.<n>.lifeCycleEventConfig` and `lifeCycleActions.<n>.lifeCycleActionConfig`.

If you’re grepping the database for an existing config, the “is this defined?” check is the `trigger` / `actionType` / `policy` key for events / actions / policy parts respectively.

### **Example policies**

These could be a drop down select on manual include groups:

| **Group Security Level (policy)** | **Employee Leaves institution**(A Membership Lifecycle Event)**** | **Employee Leaves Dept**   **(But stays at institution)** | **Employee Leaves a Position**   **(But stays in same Dept)** | Added to Group |
| --- | --- | --- | --- | --- |
| Low Security   (A Membership Lifecycle Policy) | Membership set to expire in 5days   (Fmr) Manager is alerted   (A Membership Lifecycle Action) | No Actions | No Actions | Not mvp |
| Med Security | 3-day Grace Period Starts   (Fmr) Manager is alerted | Membership set to expire in 21days   Employee is alerted | No Actions | Not mvp |
| High Security | Immediate membership removal   (Fmr) Manager is alerted | Membership set to expire in 7days   Employee is alerted | Membership set to expire in 14days   Employee is alerted | Not mvp |
