---
title: "Grouper Messaging provisioner"
space: Grouper
pageId: 236027906
version: 6
lastUpdated: 2026-09-29T19:20:40.905Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/236027906/Grouper+Messaging+provisioner
---

## What it does

The Messaging provisioner publishes a message to a queue or topic every time a group, an entity (user), or a membership is added, updated, or removed in the scope of the provisioner. Downstream applications consume those messages to keep their own data in step with Grouper.

It is a **membership-objects** provisioner that sends to a message broker instead of writing to a directory, database, or SaaS API.

- Provisioner class: `edu.internet2.middleware.grouper.app.messagingProvisioning.GrouperMessagingProvisioner`
- Provisioning type: `membershipObjects`
- Supported brokers: Grouper built-in messaging, ActiveMQ, Amazon AWS SQS, RabbitMQ
- Message format: `EsbEventJson`, or a JEXL `TranslationScript`

See the [Grouper messaging system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544824/Grouper+messaging+system) page for the broker-specific setup (external system configuration, credentials, queue naming).

## How it differs from other provisioners

Most provisioners (LDAP, SQL, Entra ID, Google, and so on) keep a target system in step with Grouper. They can read the target, compare it to what Grouper expects, and correct differences. The Messaging provisioner cannot do that, because a broker is a one-way pipe: there is nothing to read back. That changes how it behaves in several ways.

|  | Typical provisioner | Messaging provisioner |
| --- | --- | --- |
| **What the target holds** | The current state of groups, users and memberships | Nothing durable from Grouper's point of view. Messages are consumed and removed by the receiver |
| **Reads from the target** | Yes. Retrieves target objects to compare with Grouper | No. The select options are off and hidden in the configuration screen |
| **Reconciliation (full sync)** | Compares Grouper with the target and fixes drift, including deleting extras | Compares Grouper with Grouper's own record of what it already sent (the provisioning sync tables), not with the broker. It can send what was never sent, but it cannot detect a message that was lost after the broker accepted it. See Full sync and incremental sync |
| **What the provisioner sends** | Writes the final state of an object | Sends an **event** describing one change (`GROUP_ADD`, `MEMBERSHIP_DELETE`, ...). The receiver applies it |
| **Who holds state** | The target system | The **consumer**. If it misses or mishandles a message, Grouper does not know and will not repair it |
| **Attribute translation** | Configurable target attributes, translations, and link attributes | Fixed message body. You choose which objects to send, not which fields appear |
| **Delete behavior** | Removes the object from the target | Sends a `*_DELETE` event. Deleting is always on and there is nothing to delete "if not in Grouper" |
| **Ordering** | Not applicable | Each message carries a `sequenceNumber` so consumers can detect gaps and order |
| **Result of a failure** | Retried on a later run from the sync state | The object is marked not provisioned and the error is reported |

The consequences to plan for:

- **Consumers must be idempotent.** Because the provisioner sends events and does not confirm that they were applied, a consumer should handle a repeated message safely, using the object `id` and `sequenceNumber`.
- **A new consumer that needs existing data needs a bootstrap.** Messages are sent once. A consumer that starts listening later, or a queue that was purged, does not receive the earlier events. Load the initial state some other way (for example a web service or SQL export) and then apply messages from that point. See [Grouper Messaging System - full sync messages](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548117/Grouper+Messaging+System+-+full+sync+messages) for the design discussion.
- **Scope selection is the main tool.** As with every provisioner, use provisioning metadata on folders and groups to decide what is sent. Only in-scope objects produce messages.

### Compared with the messaging change log consumer

Older versions of Grouper sent messages with an ESB change log consumer (`EsbConsumer` with `EsbMessagingPublisher`). The Messaging provisioner is the recommended replacement. It uses the same JSON envelope (`esbEvent`), so existing consumers generally keep working, and it adds:

- Granular control over which folders and groups are eligible, using the provisioning framework metadata
- Provisioner sync tracking and diagnostics in the UI
- Message content derived from what the provisioner computed, rather than raw change log entries

## Configuration

Configure it through the provisioner wizard in the UI (choose the Messaging "start with"), or in `grouper-loader.properties` / the database configuration. Properties are `provisioner.<configId>.<property>`.

| Property | Default | Description |
| --- | --- | --- |
| `class` |  | `edu.internet2.middleware.grouper.app.messagingProvisioning.GrouperMessagingProvisioner` |
| `messagingType` | (required) | `ActiveMQ`, `AWS_SQS`, `RabbitMQ`, or `Grouper_Builtin` |
| `messagingActiveMqExternalSystemConfigId` |  | ActiveMQ external system. Required when `messagingType` is `ActiveMQ` |
| `messagingAwsSqsExternalSystemConfigId` |  | AWS SQS external system. Required when `messagingType` is `AWS_SQS` |
| `messagingRabbitMqExternalSystemConfigId` |  | RabbitMQ external system. Required when `messagingType` is `RabbitMQ` |
| `messagingFormatType` | (required) | `EsbEventJson` or `TranslationScript` (see below) |
| `groupFormatTranslationScript` |  | JEXL script returning a map that is the group message body. Only used when `messagingFormatType` is `TranslationScript` |
| `entityFormatTranslationScript` |  | JEXL script returning a map that is the entity message body. Only used when `messagingFormatType` is `TranslationScript` |
| `membershipFormatTranslationScript` |  | JEXL script returning a map that is the membership message body. Only used when `messagingFormatType` is `TranslationScript` |
| `queueOrTopicName` | (required) | Name of the queue or topic to send to |
| `queueType` | (required) | `queue` or `topic`. Not used for AWS SQS, which is always a queue |
| `routingKey` |  | RabbitMQ only |
| `exchangeType` |  | RabbitMQ only: `DIRECT`, `TOPIC`, `HEADERS`, or `FANOUT` |
| `numberOfQueueArguments` | `0` | How many queue arguments to pass when the queue is created |
| `queueArgument.<i>.key` / `queueArgument.<i>.value` |  | Queue argument name and value |

The queue or topic is created automatically if it does not exist when `loader.messaging.settings.autocreate.objects` is `true` (the default).

## Message format

Every message is a JSON envelope with a single event in the `esbEvent` array.

| Field | Description |
| --- | --- |
| `encrypted` | Always `false` |
| `esbEvent` | Array with one event object |
| `eventType` | What happened. See the table below |
| `sequenceNumber` | Increases by one for each change sent by this provisioner. Consumers can use it to order messages and to spot gaps |
| `changeOccurred` | Always `true` |
| `createdOnMicros` | When the message was created, in microseconds since the epoch |

Empty values are left out of the message, which is why the examples below do not show `description` or `parentStemId`.

| Object | Event types |
| --- | --- |
| Group | `GROUP_ADD`, `GROUP_UPDATE`, `GROUP_DELETE` |
| Entity | `MEMBER_ADD`, `MEMBER_UPDATE`, `MEMBER_DELETE` |
| Membership | `MEMBERSHIP_ADD`, `MEMBERSHIP_DELETE` |

Entity messages use the event types `MEMBER_*` because they follow the ESB change log naming, where a Grouper "member" is what the provisioning framework calls an entity. A `*_DELETE` message has the same body as the corresponding `*_ADD` with a different `eventType`.

### Group

Sent when a group comes into scope, changes, or leaves scope.

```json
{"encrypted":false,"esbEvent":[{"id":"15eebb0dc7854a0288f57fbe357f2c8a","displayExtension":"testGroup","displayName":"test:testGroup","groupId":"15eebb0dc7854a0288f57fbe357f2c8a","groupName":"test:testGroup","name":"test:testGroup","eventType":"GROUP_ADD","sequenceNumber":2,"changeOccurred":true,"createdOnMicros":1790015366337000}]}
```

| Field | Description |
| --- | --- |
| `id`, `groupId` | The Grouper group UUID |
| `name`, `groupName` | The group name, for example `test:testGroup` |
| `displayName` | The display name, for example `test:testGroup` |
| `displayExtension` | The last part of the display name |
| `description` | The group description, present only when the group has one |

### Entity

Sent when a user comes into scope, changes, or leaves scope.

```json
{"encrypted":false,"esbEvent":[{"id":"b8c19b0ef67a44d9b0ad60db351e75d5","subjectId":"800001147","subjectSourceId":"eduLDAP","subjectIdentifier0":"banderson","eventType":"MEMBER_ADD","sequenceNumber":3,"changeOccurred":true,"createdOnMicros":1790015366383000}]}
```

| Field | Description |
| --- | --- |
| `id` | The Grouper member UUID. This is the `memberId` in membership messages |
| `subjectId` | The subject id in its source |
| `subjectSourceId` | The subject source id, for example `eduLDAP` |
| `subjectIdentifier0` | The subject's first identifier |

`subjectSourceId` and `subjectIdentifier0` are taken from the entity attributes of the provisioner, so they appear when the entity data is available to the provisioner (from the subject source or the entity resolver).

### Membership

Sent when a user is added to or removed from a group in scope. The membership is always reported as `flattened`, so an indirect member (through a subgroup) produces the same message as a direct member.

```json
{"encrypted":false,"esbEvent":[{"id":"1000956:1000040","fieldId":"9d948d4da8bd4987b6c11f6371850503","fieldName":"members","groupId":"15eebb0dc7854a0288f57fbe357f2c8a","groupName":"test:testGroup","memberId":"b8c19b0ef67a44d9b0ad60db351e75d5","membershipType":"flattened","sourceId":"eduLDAP","subjectId":"800001147","eventType":"MEMBERSHIP_ADD","sequenceNumber":4,"changeOccurred":true,"createdOnMicros":1790015366396000}]}
```

| Field | Description |
| --- | --- |
| `id` | Identifies the membership. Treat it as an opaque key (in the example, two internal ids joined by a colon) |
| `fieldId`, `fieldName` | The Grouper list the membership is in, normally `members` |
| `groupId`, `groupName` | The group. Matches `id` and `name` in the group message |
| `memberId` | The Grouper member UUID. Matches `id` in the entity message |
| `membershipType` | Always `flattened` |
| `sourceId`, `subjectId` | The subject source and subject id. Match `subjectSourceId` and `subjectId` in the entity message |

The three example messages fit together: they add group `test:testGroup`, then user `banderson`, then the membership between them, with `sequenceNumber` 2, 3 and 4. A consumer can join the messages using `groupId` and `memberId`.

### Custom message body with JEXL

If the consumer needs a different body than the standard one, set `messagingFormatType` to `TranslationScript` and give a JEXL script for the object types you want to customize. Each script returns a map, and that map becomes the event object in the `esbEvent` array. An object type with no script keeps the standard `EsbEventJson` body.

| Property | Variables available to the script |
| --- | --- |
| `groupFormatTranslationScript` | `targetGroup`, `eventType` |
| `entityFormatTranslationScript` | `targetEntity`, `eventType` |
| `membershipFormatTranslationScript` | `targetMembership`, `targetGroup`, `targetEntity`, `eventType` |

`eventType` is the event being sent (for example `MEMBER_ADD` or `MEMBER_DELETE`), so one script can return a smaller body for deletes. It is read only: the framework still sets the `eventType` field of the message itself.

Example entity script:

```none
${ {
    "subjectId": targetEntity.getSubjectId(),
    "netid": targetEntity.retrieveAttributeValueString("subjectIdentifier1"),
    "name": targetEntity.name,
    "email": targetEntity.email
  }
}
```

The script is JEXL, not JSON: quoted keys look like JSON but a trailing comma after the last entry, e.g. `..."email": targetEntity.email,\n }`, is a parse error, not a warning. Drop the comma after the last entry.

As with the standard body, keys whose value is null (for example an entity with no email) are left out of the message, and a null property does not cause an error. Because of that, a misspelled variable name is also treated as null rather than reported. The envelope and the framework fields `eventType`, `sequenceNumber`, `changeOccurred` and `createdOnMicros` are always added after the script runs, so a script cannot override them. If the script does not return a map, or fails, the change is not sent and the provisioning run reports the error.

The framework's own `createdOnMicros` is not available to the script (it is computed after the script runs), but a script can add its own current-time value with a JEXL expression. JEXL2 has no `new` operator, so `new java.util.Date()` or `new SimpleDateFormat(...)` will not parse; `java.time`'s static factory methods work instead, for example:

```none
"generatedAt": java.time.Instant.now().toString()
```

For the full list of properties and methods available on `targetGroup`, `targetEntity` and `targetMembership`, and the `eventType` values, see JEXL translation script reference at the end of this page.

## Full sync and incremental sync

Both run the same message-building code, so the messages are identical. What differs is how the provisioner decides what to send. Because the broker cannot be read, both compare against the **provisioning sync tables**, where Grouper records for each group, entity and membership whether it has been sent (`in target`).

|  | Incremental sync | Full sync |
| --- | --- | --- |
| **Trigger** | Change log consumer (`provisioner_incremental_<configId>`), on its schedule | Daemon (`provisioner_full_<configId>`), on its schedule or run manually |
| **Input** | Only the events since the last run (and any objects with errors to retry) | Every group, entity and membership in scope, read from Grouper |
| **What is sent** | Messages for the changed objects only | An add message for each in-scope object not recorded as sent. A delete message for each object recorded as sent that has left scope or been deleted |
| **Typical use** | Normal operation. One message per change, quickly | First run on a new provisioner, catch-up after a long outage, or recovery after clearing the sync state |
| **Nothing changed** | Sends nothing | Sends nothing |
| **Cost** | Proportional to the number of changes | Proportional to the size of the provisioned population |
| **Ordering** | Follows the order of the change events | Follows the order the provisioner builds its inserts and deletes. Do not assume more than the `sequenceNumber` |

A few consequences:

- **A first full sync is a bootstrap.** On a new provisioner nothing is recorded as sent, so a full sync sends an add message for every in-scope group, entity and membership. A large scope produces a large burst of messages, so size the queue and consumer accordingly.
- **Full sync will not repair a consumer.** If a message reached the broker and the consumer lost it, Grouper's record says it was sent, and a later full sync sends nothing. Recovering a consumer means replaying the data (a bootstrap) or clearing the provisioner's sync state so that everything is sent again.
- **Incremental can escalate to a full sync.** When a run has a large number of events (past the `scoreConvertToFullSyncThreshold` for the provisioner) the incremental job hands over to a full sync in the same run rather than process each event.
- **Full sync updates are rare.** Nothing is read back from the broker, so there are no attribute comparisons against the target to produce updates. Expect adds and deletes.

## Sequence numbers and ordering

The provisioner takes the next `sequenceNumber` from its sync job (one counter per provisioner config id), and increments it for every object it sends. Messages are published in the order the provisioner processes them, and `sequenceNumber` reflects that order. Because the counter belongs to one provisioner, do not compare sequence numbers between different Messaging provisioners.

A gap in the sequence numbers can indicate a message that was lost or that has not been delivered yet. The provisioner does not resend a message that the broker accepted and later lost.

## JEXL translation script reference

Properties and methods available to the `groupFormatTranslationScript`, `entityFormatTranslationScript` and `membershipFormatTranslationScript` scripts described under **Custom message body with JEXL** above.

### Event type values

The same event types as the standard body (see the **Message format** section above):

| Object | `eventType` values |
| --- | --- |
| `targetGroup` script | `GROUP_ADD`, `GROUP_UPDATE`, `GROUP_DELETE` |
| `targetEntity` script | `MEMBER_ADD`, `MEMBER_UPDATE`, `MEMBER_DELETE` |
| `targetMembership` script | `MEMBERSHIP_ADD`, `MEMBERSHIP_DELETE` |

### Properties and methods on targetGroup, targetEntity and targetMembership

`targetGroup`, `targetEntity` and `targetMembership` are Java beans, so a JEXL script can use either the dotted property form (`targetEntity.email`) or the getter method directly (`targetEntity.getEmail()`). The ticket example that introduced this feature mixes both styles.

These methods are common to all three objects. What attribute names they accept depends on what the provisioner is configured to load (for example, an entity attribute only resolves if the provisioner's entity attribute resolver is configured to fetch it):

| Method | Description |
| --- | --- |
| `retrieveAttributeValueString('name')` | An attribute's value as a string |
| `retrieveAttributeValue('name')` | An attribute's value in its native type |
| `retrieveAttributeValueLong('name')` | An attribute's value as a `Long` |
| `retrieveAttributeValueInteger('name')` | An attribute's value as an `Integer` |
| `retrieveAttributeValueBoolean('name')` | An attribute's value as a `Boolean` |
| `retrieveAttributeValueSet('name')` | A multi-valued attribute's values as a `Set` |
| `hasAttribute('name')` | Whether the attribute is present |

`targetGroup` (a `ProvisioningGroup`) built-in properties:

| Property | Description |
| --- | --- |
| `id` | The Grouper group UUID |
| `name` | The group name, for example `test:testGroup` |
| `extension` | The last part of `name` |
| `displayName` | The display name |
| `displayExtension` | The last part of `displayName` |
| `idIndex` | Target id index, when the provisioner uses one (optional) |
| `groupInternalId` | `grouper_groups.internal_id` for the matched Grouper group |

`targetEntity` (a `ProvisioningEntity`) built-in properties:

| Property | Description |
| --- | --- |
| `id` | The Grouper member UUID. Matches `memberId` in the standard membership body |
| `subjectId` | The subject id in its source |
| `subjectSourceId` | The subject source id, for example `eduLDAP` |
| `subjectIdentifier0` / `subjectIdentifier1` / `subjectIdentifier2` | The subject's identifiers, when the provisioner resolves them |
| `subjectIdentifier` | Whichever `subjectIdentifierN` the provisioner is configured to treat as the primary one |
| `loginId` | A login id, either a subject identifier or subject id (optional) |
| `name` | The subject's name (optional) |
| `email` | The subject's email (optional) |
| `description` | The entity description (optional) |
| `idIndex` | Target id index, when the provisioner uses one (optional) |
| `internalId` | Grouper member internal id (optional) |

`targetMembership` (a `ProvisioningMembership`) built-in properties:

| Property | Description |
| --- | --- |
| `id` | Identifies the membership. Treat it as an opaque key, like `id` in the standard membership body |
| `provisioningGroupId` | The group's id. Matches `targetGroup.id` |
| `provisioningEntityId` | The entity's id. Matches `targetEntity.id` |
| `provisioningGroup` | The same object as the `targetGroup` variable, for convenience |
| `provisioningEntity` | The same object as the `targetEntity` variable, for convenience |

`targetMembership` does not carry `fieldId`, `fieldName`, `membershipType` or `sourceId` the way the standard membership body does. Those are filled in by the messaging layer, not stored on the object. Use `targetGroup`/`targetEntity` attributes, or `id`, to identify the membership; use `targetGroup.retrieveAttributeValueString(...)` / `targetEntity.retrieveAttributeValueString(...)` for anything else the script needs to look up.

### Provisioning wrappers

`targetEntity` and `targetGroup` also each expose a `provisioningEntityWrapper` / `provisioningGroupWrapper` property, which reaches a few provisioning-framework lookups that are not on the target object itself (`targetMembership.provisioningEntityWrapper` and `targetMembership.provisioningGroupWrapper` reach the same wrappers as the entity and group scripts get directly). The wrapper classes have other methods too, mostly internal bookkeeping (matching Grouper-side and target-side copies, sync table rows, error state); these are the ones useful from a script:

| Method (on `provisioningEntityWrapper`) | Description |
| --- | --- |
| `isInGroup('a:group:name')` | Whether this entity is a member of the named Grouper group |
| `isHasPrivilege('a:group:name', 'admins')` | Whether this entity holds the named privilege (for example `admins`, `updaters`) on the named group |

| Method (on `provisioningGroupWrapper`) | Description |
| --- | --- |
| `thisGroupMembers('subjectId')` | Set of this group's member values, in the given field (`subjectId`, `subjectIdentifier0`, `subjectIdentifier1`, `subjectIdentifier2`, or `email`) |
| `groupMembers('another:group:name', 'subjectId')` | Same, for a different group |
| `thisGroupPrivilegeHolders('admins', 'subjectId')` | Set of subjects holding the named privilege on this group, in the given field |
| `groupPrivilegeHolders('another:group:name', 'admins', 'subjectId')` | Same, for a different group |

Example, adding whether the entity is in an "owners" group to the entity message:

```none
${ {
    "subjectId": targetEntity.getSubjectId(),
    "isOwner": targetEntity.provisioningEntityWrapper.isInGroup("etc:owners")
  }
}
```

## Notes

- Messages contain user identifiers (`subjectId`, `subjectIdentifier0`). Restrict access to the queue or topic accordingly.
- A change can be sent again if a run fails partway and is retried, so consumers should tolerate duplicates.
- The [Grouper messaging system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544824/Grouper+messaging+system) page lists the receive-side options, including the messaging listener daemon and the built-in `grouper_message` table cleanup.
