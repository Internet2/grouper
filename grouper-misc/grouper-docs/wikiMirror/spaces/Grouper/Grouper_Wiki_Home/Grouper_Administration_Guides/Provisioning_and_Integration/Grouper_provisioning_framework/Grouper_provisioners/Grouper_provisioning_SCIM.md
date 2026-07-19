---
title: "Grouper provisioning SCIM"
space: Grouper
pageId: 28555423
version: 45
lastUpdated: 2026-07-19T00:32:44.340Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555423/Grouper+provisioning+SCIM
---

This is for v4+

[https://www.rfc-editor.org/rfc/rfc7643.html#section-4.1](https://www.rfc-editor.org/rfc/rfc7643.html#section-4.1)

Additional functions and options can be found on the [Grouper provisioning - SCIM - Functions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559941/Grouper+provisioning+-+SCIM+-+Functions) page

Note, if you want custom SCIM attributes (extensions) or want to dereference SCIM UUIDs from the target (e.g. load data and join to it), the [Service Now](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564299/Grouper+provisioning+SCIM+ServiceNow) document is an example

## External System

Grouper uses bearer token authentication to connect with SCIM V2 APIs. Create an external system like below.

We have tested SCIM integration for AWS, Github, Atlassian, Robin, and others. Even though they all follow SCIM, there are still many differences, so when you configure a SCIM provisioner, we ask for SCIM type. Based on the SCIM type, the provisioner framework can run extra validations to make integration more robust.

Note: if you get an error on users with special characters in their names or attributes, set `provisioner.<id>.removeAccentedChars = true` in the config to convert special accented characters to unaccented characters before sending to the target.

## Grouper provisioning SCIM examples

## Evaluating a SCIM target

### Accept header

If the SCIM service requires an accept header you can enter that in the configuration

### Content type header

If the SCIM service requires a content-type header that is not the default enter that here

### SCIM type

```
provisioner.<id>.scimType = generic
```

  
SCIM-2.0 endpoints disagree on syntax details. Pick the closest preset; `generic` is the fallback when the target isn't AWS or GitHub.

- `AWS` — AWS Identity Center / SSO. Skips the `Accept` header (AWS rejects it) and enables AWS-specific attribute handling.
- `Github` — uses `Accept: application/vnd.github.v3+json`.
- `generic` — everything else (Anaplan, Atlassian Cloud, Robin, ServiceNow, Tableau, etc.). Use this and tune the strategy knobs below.

### Patch name strategy

```
provisioner.<id>.scimNamePatchStrategy = nonqualified
```

Controls how Grouper addresses `givenName` / `familyName` / `middleName` / `formatted` in a PATCH op.

| Value | Path Grouper sends | When to use |
| --- | --- | --- |
| `nonqualified` (default) | `"path": "givenName"` | Target accepts bare leaf-attribute paths. |
| `qualified` | `"path": "name.givenName"` | Target requires the parent `name` object in the path. Common — used by AWS, Anaplan, Grammarly. |
| `nested` | One op replacing the whole `name` object | Target rejects sub-attribute PATCHes and wants a single `replace` of the entire `name` complex value. Used by Atlassian Cloud SSO. |

How to pick: look at the target's own PATCH example. If it shows `"path": "name.familyName"`, set `qualified`. If `"path": "familyName"`, leave at `nonqualified`. If the docs only show replacing the whole `name` object, set `nested`.

### Patch email strategy

```
provisioner.<id>.scimEmailPatchStrategy = pathEmails
```

  
Controls how Grouper PATCHes the `emails` multi-valued attribute.

| Value | What Grouper sends |
| --- | --- |
| `pathEmails` (default) | `"path": "emails[type eq \"work\"].value"` — SCIM 2.0 standard filter-in-path. |
| `noPath` | No `path`; replaces the entire `emails` array in the `value`. Use for targets that reject filter expressions in PATCH paths. |
| `pathEmailsQualified` | URN-qualified path, e.g. `"urn:ietf:params:scim:schemas:core:2.0:User:emails[type eq \"work\"].value"`. Used by Databricks. |

If creates work but email updates fail with 400, this is the first thing to flip.

### Email filter strategy

```
provisioner.<id>.scimEmailFilterStrategy = email
```

 Controls the GET filter Grouper sends when searching for an existing user by email.

| Value | Filter expression sent |
| --- | --- |
| `email` (default, non-standard but kept for back-compat) | `email eq "x@y"` |
| `emails.value` | `emails.value eq "x@y"` |
| `emails[value]` | `emails[value eq "x@y"]` |
| `emails[typeWork and value]` | `emails[type eq "work" and value eq "x@y"]` |

This only matters if `emailValue` is in your `entityMatchingAttributeN` list. If you match on `userName` or `id`, leave at default.

### Paging issues

If the SCIM service has paging problems, maybe it is not using totalResults

Try this (e.g. curl) <scimUrl>/Users?startIndex=0&count=10

See if there is totalResults. If so, things are probably ok here.

```
{
  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
  "totalResults": 3,
  "itemsPerPage": 3,
  "startIndex": 0,
  "Resources": [{
```

### Retrieve memberships by user

Grouper will try to detect this and respond appropriately, but you can set if memberships are retrieved by user. Try getting a user and see if there are groups inside

Try getting all users:

Try this (e.g. curl) <scimUrl>/Groups?startIndex=0&count=10

In one of the user objects look for "groups". If so there are groups in the user.

```
 "Resources": [{
    "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
    "id": "1234",
    "meta": {
      "resourceType": "User",
      "location": "/Users/1234"
    },
    "userName": "jsmith@example.com",
    "name": {
      "familyName": "Smith",
      "givenName": "John"
    },
    "displayName": "John Smith",
    "active": true,
    "emails": [{
      "value": "jsmith@example.com",
      "type": "work",
      "primary": true
    }],
    "groups": [
      {
        "value": "b5a92fa8-274e-4f51-8e8e-d9c9e5c4a4a1",
        "$ref": "/Groups/b5a92fa8-274e-4f51-8e8e-d9c9e5c4a4a1",
        "display": "Developers",
        "type": "direct"
      }
    ]
  }, 
```

After checking all users, if its not there, check for an individual user: <scimUrl>/Users/1234

If neither of these has "groups", then set this to false:

### Retrieve memberships by user or group

If you can retrieve memberships by user (above), which is preferred, then just set this to false. Note that it is better to have memberships by user since it scales better for large groups.

Grouper will try to detect this and respond appropriately, but you can set if memberships are retrieved by group.

Try getting all groups and see if there are members (users) inside. In this case there are no users

Try (e.g. curl): <scimUrl>/Groups?startIndex=0&count=10

```
{
  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
  "totalResults": 3,
  "itemsPerPage": 3,
  "startIndex": 0,
  "Resources": [{
    "schemas": ["urn:ietf:params:scim:schemas:core:2.0:Group"],
    "id": "6789",
    "meta": {
      "resourceType": "Group",
      "location": "/Groups/6789"
    },
    "displayName": "My Group"
  },
```

Try getting one group and look for users:

Try (e.g. curl): <scimUrl>/Groups/6789

```
{
  "displayName": "My Group",
  "schemas": ["urn:ietf:params:scim:schemas:core:2.0:Group"],
  "members": [{
    "display": "John Smith",
    "value": "1234",
    "$ref": "Users/1234"
  }],
  "id": "6789"
}
```

Priority 1: Retrieves groups when retrieving list of users. If this is the strategy then you should select all users at once.

Priority 2: Retrieves groups when retrieving individual users. Do not select all users at once (until this is resolved: [GRP-6479](https://grouper.atlassian.net/browse/GRP-6479))

Priority 3: Retrieves users when retrieving individual groups. Do not select all groups at once (until this is resolved: [GRP-6479](https://grouper.atlassian.net/browse/GRP-6479))

Priority 4: Retrieves users when retrieving list of groups. If this is the strategy then you should select all groups at once.

### Group add

Try (e.g. in curl)

POST <scimUrl>/Groups

```
Authorization: Bearer abc123
Content-type: application/json
{
  "displayName": "test",
  "schemas": [
    "urn:ietf:params:scim:schemas:core:2.0:Group"
  ]
}
```

Should get 201 and group json.

If you cannot insert groups, set Group CRUD insert to false.

### Group update

Try (e.g. in curl)

PATCH <scimUrl>/Groups/6789

```
Authorization: Bearer abc123
Content-type: application/json
{
  "schemas": [
    "urn:ietf:params:scim:api:messages:2.0:PatchOp"
  ],
  "Operations": [
    {
      "op": "replace",
      "value": "test2",
      "path": "displayName"
    }
  ]
}
```

Should get a 204 and blank body.

If you cannot update groups, set Group CRUD update to false.

### Group delete

Try (e.g. in curl)

DELETE <scimUrl>/Groups/6789

```
Authorization: Bearer abc123
```

Should get a 204 and a blank body

If you cannot delete groups, set Group CRUD delete to false.

### User add

Try (e.g. in curl). Based on retrieving users, set some attributes and try to add.

POST <scimUrl>/Users

```
Authorization: Bearer abc123
Content-type: application/json
{
  "active": true,
  "displayName": "John Smith",
  "emails": [
    {
      "value": "jsmith@example.com",
      "primary": true,
      "type": "work"
    }
  ],
  "userName": "jsmith@example.com",
  "schemas": [
    "urn:ietf:params:scim:schemas:core:2.0:User"
  ]
}

```

Should get 201 and user json. If that works you do not need givenName and familyName. Note, you do not need to map "active", it will be added automatically.

If you cannot insert users, set User CRUD insert to false.

Select the user and make sure all the names and everything look ok.

### User update

Try (e.g. in curl). Try to update various fields

PATCH <scimUrl>/Users/1234

```
Authorization: Bearer abc123
Content-type: application/json
{
  "schemas": [
    "urn:ietf:params:scim:api:messages:2.0:PatchOp"
  ],
  "Operations": [
    {
      "op": "replace",
      "value": "John2 Smith2",
      "path": "displayName"
    }
  ]
}
```

Should get a 204 and blank body.

If you cannot update users, set User CRUD update to false. If you cannot set a property, set that attribute update to false

## Lifecycle and CRUD customization

Beyond the per-operation try-it-with-curl smoke tests above, several knobs control which operations Grouper attempts at all. Turn these on to model targets that don't support the full CRUD surface.

```
provisioner.<id>.customizeEntityCrud = true
provisioner.<id>.customizeGroupCrud = true
provisioner.<id>.customizeMembershipCrud = true
```

  
With customization on, individual operations can be disabled:

- `insertEntities`, `updateEntities`, `deleteEntities` (and the `Groups` / `Memberships` analogs) — turn individual ops off when a target doesn't support them. Tableau is the canonical example: its SCIM endpoint accepts user creates but not user PATCHes, so it runs with `updateEntities = false`, `deleteEntities = false`, `updateGroups = false`, `deleteGroups = false`.
- `selectAllEntities` / `selectAllGroups` — whether to enumerate the full target on a full sync. Off if the target doesn't support paged list endpoints efficiently.

## Disable entities instead of delete

```
provisioner.<id>.disableEntitiesInsteadOfDelete = true
```

When set, a delete becomes a PATCH of `active: false`. On a subsequent create of the same user, Grouper looks up the disabled user and re-enables it instead of inserting. Recommended for any target where users have history that can't be re-created (LogicGate and RandomCoffee both use this at Penn).

When this is on, do **not** map `active` as a target entity attribute — Grouper sets it implicitly.

## Include active on entity create

```
provisioner.<id>.includeActiveOnEntityCreate = true
```

 Default `true`. Adds `"active": true` to the create body. Turn off for targets that reject `active` in POST (rare).

## SCIM membership batch size

```
provisioner.<id>.scimMembershipBatchSize = 100
```

 Number of member add/remove operations packed into a single PATCH `Operations` array per group. Lower it if the target rejects large patch bodies or has per-request limits. Default 100.

## Membership strategy

```
provisioner.<id>.membershipStrategy = fullGroupMembershipsInGroupObjectsWhenRetrievingIndividualGroups
```

 Where memberships live in the target's data model. Decide this after the "Retrieve memberships by user / by group" smoke tests above tell you which side of the relationship the target exposes.

| Value | Use when |
| --- | --- |
| `fullGroupMembershipsInGroupObjectsWhenRetrievingIndividualGroups` | `GET /Groups/{id}` returns a `members` array; `GET /Users/{id}` does **not** return a `groups` array. Used by Databricks. |
| `membershipsInUserObjectsWhenRetrievingIndividualUsers` | `GET /Users/{id}` returns a `groups` array; group objects don't carry `members`. Cheaper for groups with many members. |

## Membership CRUD vs full replace

When a target supports SCIM membership PATCH (`Operations: [{op: add, path: "members", value: [...]}]`), Grouper sends incremental add/remove ops. Some targets — notably Qlik — instead require sending the **entire desired membership list every time** as a single PATCH `replace` op. Configure that with:

```
provisioner.<id>.customizeMembershipCrud = true
provisioner.<id>.insertMemberships = false
provisioner.<id>.deleteMemberships = false
provisioner.<id>.replaceMemberships = true
provisioner.<id>.recalculateAllOperations = true
```

  
`recalculateAllOperations = true` is important here — without it Grouper would compute only the *delta* against its sync table and the replace payload would be incomplete.

Use this pattern when the target's docs say to PUT/PATCH the whole `members` array, or when individual add/remove operations fail but a full replace succeeds.

## Custom target attributes via JSON pointer

Beyond the built-in entity attributes (`active`, `userName`, `givenName`, `familyName`, `displayName`, `emailValue`, `costCenter`, `department`, `employeeNumber`, etc.), you can write **any** field — including deeply nested or array-element fields — into the create payload by defining a custom target entity attribute with a JSON pointer.

```
provisioner.<id>.numberOfEntityAttributes = 10
provisioner.<id>.targetEntityAttribute.7.name = entitlementValue
provisioner.<id>.targetEntityAttribute.7.entityAttributeJsonPointer = /entitlements/0/value
provisioner.<id>.targetEntityAttribute.7.translateExpressionType = staticValues
provisioner.<id>.targetEntityAttribute.7.translateFromStaticValues = mainWorkspace
provisioner.<id>.targetEntityAttribute.8.name = entitlementType
provisioner.<id>.targetEntityAttribute.8.entityAttributeJsonPointer = /entitlements/0/type
provisioner.<id>.targetEntityAttribute.8.translateExpressionType = staticValues
provisioner.<id>.targetEntityAttribute.8.translateFromStaticValues = WORKSPACE
```

  
  
  
That produces a request body containing:

```
"entitlements": [
    { "value": "mainWorkspace", 
      "type": "WORKSPACE" }
]
```

  
Rules:

- The attribute `name` must **not** collide with a built-in attribute name. Anything not in the built-in set flows through the JSON-pointer path.
- The pointer auto-creates nested objects and arrays as it walks. Numeric path segments (e.g. `/0`) produce array elements; non-numeric produce object properties.
- Each attribute value must be a **scalar** (string, boolean, number). To build a multi-element array, define one attribute per leaf — e.g. three attributes pointing at `/entitlements/0/value`, `/entitlements/0/type`, `/entitlements/0/display` together produce one entitlement element.
- Optional: `targetEntityAttribute.<N>.jsonValueType = boolean` (or `number`) coerces the serialized JSON type. Default is `string`.

### URN-namespaced extensions

For SCIM schema extensions (e.g. ServiceNow's enterprise/servicenow namespaces), put the literal URN as the first pointer segment:

```
provisioner.<id>.targetEntityAttribute.7.entityAttributeJsonPointer = /urn:ietf:params:scim:schemas:extension:servicenow:2.0:User/department/value
```

  
When you write into an extension namespace you must also advertise it in the `schemas` array. Configure `schemas` as a multi-valued static attribute:

```
provisioner.<id>.targetEntityAttribute.<N>.name = schemas
provisioner.<id>.targetEntityAttribute.<N>.multiValued = true
provisioner.<id>.targetEntityAttribute.<N>.translateExpressionType = staticValues
provisioner.<id>.targetEntityAttribute.<N>.translateFromStaticValues = urn:ietf:params:scim:schemas:core:2.0:User,urn:ietf:params:scim:schemas:extension:servicenow:2.0:User,urn:ietf:params:scim:schemas:extension:enterprise:2.0:User
```

  

### Sourcing values from subject attributes

To pull a value off the Grouper subject into a target attribute, use the `subjectTranslationScript` cache pattern (idiomatic in the Tableau and ServiceNow provisioners):

```
provisioner.<id>.entityAttributeValueCache2has = true
provisioner.<id>.entityAttributeValueCache2source = grouper
provisioner.<id>.entityAttributeValueCache2type = subjectTranslationScript
provisioner.<id>.entityAttributeValueCache2translationScript = ${subject.getAttributeValue('sn')}
 
provisioner.<id>.targetEntityAttribute.2.name = familyName
provisioner.<id>.targetEntityAttribute.2.translateExpressionType = grouperProvisioningEntityField
provisioner.<id>.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField = entityAttributeValueCache2
```

  
For nullable subject attributes add `nullChecksInScript = true` and a `translationContinueCondition = ${subject != null}` to the cache config.

Note, you can also source entity attributes from data fields as well.

## Vendor notes

Notes from successful Grouper implementations to vendors.

| Target | `scimType` | Known settings |
| --- | --- | --- |
| Anaplan | `generic` | `acceptHeader = application/scim+json`, `scimContentType = application/scim+json`, `scimNamePatchStrategy = qualified`. Requires custom `entitlements` target attributes via JSON pointers — no built-in support. Base URL is `[https://api.anaplan.com/scim/1/0/v2](https://api.anaplan.com/scim/1/0/v2)` (do **not** include `/Users/<workspaceId>` in the external system URL). |
| [Atlassian Cloud](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564203/Grouper+provisioning+SCIM+for+Atlassian) (Jira, Confluence) | `generic` | `acceptHeader = application/json` (overrides the `application/scim+json` default). |
| Atlassian Cloud SSO | `generic` | `scimNamePatchStrategy = nested` (PATCH replaces the entire `name` object). |
| [AWS Identity Center](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564269/Grouper+provisioning+SCIM+for+AWS) | `AWS` | `scimNamePatchStrategy = qualified`. The `AWS` preset suppresses the `Accept` header automatically. |
| [ChatGPT (OpenAI)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564317/Grouper+provisioning+SCIM+for+OpenAI+-+ChatGPT) | `generic` | All SCIM-dialect defaults. |
| Databricks | `generic` | `scimEmailPatchStrategy = pathEmailsQualified`, `scimRetrieveMembershipsByUser = false`, `membershipStrategy = fullGroupMembershipsInGroupObjectsWhenRetrievingIndividualGroups` — memberships live on the group object only. |
| [GitHub](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564232/Grouper+provisioning+SCIM+for+GitHub) ([orgs as groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564280/Grouper+provisioning+SCIM+for+GitHub+with+organizations+as+groups)) | `Github` | Custom `Accept` header set by preset. |
| Grammarly | `generic` | `scimNamePatchStrategy = qualified`, `scimRetrieveMembershipsByUser = false` (group → user direction only). |
| LogicGate | `generic` | `disableEntitiesInsteadOfDelete = true`. |
| Qlik | `generic` | **Whole-list membership replace** — `insertMemberships = false`, `deleteMemberships = false`, `replaceMemberships = true`, `recalculateAllOperations = true`. Matches entities on `emailValue` + `id`; groups on `displayName` + `id`. Sends `emailType = "work"` statically. |
| RandomCoffee | `generic` | `disableEntitiesInsteadOfDelete = true`. |
| [Robin](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564194/Grouper+provisioning+SCIM+for+Robin+workplace+management) | `generic` | `scimRetrieveMembershipsByGroup = false` (memberships only retrievable via user objects, not group objects). |
| [ServiceNow](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564299/Grouper+provisioning+SCIM+ServiceNow) | `generic` | Canonical example for **SCIM schema extensions** (URN-namespaced JSON pointers like `/urn:...:servicenow:2.0:User/department/value`), **multi-valued `schemas`** declaration (`multiValued = true` + comma-separated `translateFromStaticValues`), and **dereferencing target UUIDs via local SQL** through `entityResolver.resolveAttributesWithSQL`. |
| [Tableau](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564262/Grouper+provisioning+SCIM+for+Tableau) | `generic` | **Insert-only target** — `updateEntities = false`, `deleteEntities = false`, `updateGroups = false`, `deleteGroups = false`. Tableau's SCIM endpoint doesn't accept PATCH on users or groups; Grouper only creates them and any later edits happen out-of-band. Memberships still get full CRUD. |
