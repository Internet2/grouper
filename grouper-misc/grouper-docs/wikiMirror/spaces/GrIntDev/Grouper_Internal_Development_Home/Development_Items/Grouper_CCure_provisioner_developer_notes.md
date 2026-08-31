---
title: "Grouper CCure provisioner developer notes"
space: GrIntDev
pageId: 131956793
version: 1
lastUpdated: 2026-08-15T18:34:35.275Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/131956793/Grouper+CCure+provisioner+developer+notes
---

## API documentation

 The API is the **victor Web Service** from Software House / Johnson Controls. There is no public reference: the API is gated behind the "Victor Web Service for End-Users" license, and the developer documentation is available only through Johnson Controls support. Everything below was established against a real CCure system and is reproduced by the mock service.

 

## External system

 [Grouper CCure external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/132022298/Grouper+CCure+external+system)

 

## Authentication

 Session-based, not a bearer token. Login is form-encoded, not JSON.

 
```
POST {endpoint}/api/Authenticate/Login
Content-Type: application/x-www-form-urlencoded

UserName=ccureUser&Password=*******&ClientName=Internet2 - Grouper - Integration
  &ClientID=ffffffff-ffff-ffff-ffff-ffffffffffff&ClientVersion=2.9

200
response header: session-id: 7d1f...
body: the access token
```

 Every later request carries both:

 
```
header:        session-id: {sessionId}
url parameter: token={accessToken}
```

 
```
POST {endpoint}/api/Authenticate/Logout
200
```

 A bad username, password, or client name all return the same response, so the error does not tell you which one is wrong:

 
```
401
{"response":{"comment":"Error Message:  User not in system"}}
```

 Sessions are limited on the CCure side, so a run logs in once and logs out at the end.

 

## Object model

 Three CCure object types, addressed by their full .NET type names:

 

| Grouper concept | CCure TypeFullName | Key |
| --- | --- | --- |
| group | `SoftwareHouse.NextGen.Common.SecurityObjects.Clearance` | ObjectID |
| entity | `SoftwareHouse.NextGen.Common.SecurityObjects.Personnel` | ObjectID, returned as PersonnelID on a pair |
| membership | `SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair` | ObjectID |

 A pair's `ClearanceID` is the Clearance's `ObjectID` and its `PersonnelID` is the Personnel record's `ObjectID`, so the ids line up across the three axes with no translation.

 The JSON is flat with PascalCase keys - no envelope, unlike SCIM or JSON:API targets.

 

## Query: GetAllWithCriteria

 The workhorse read. It is **projection based**: the response contains only the fields named in `DisplayProperties`. A field that is not asked for comes back missing rather than null, which is why sync back has to widen this list.

 
```
POST {endpoint}/api/Objects/GetAllWithCriteria
Content-Type: application/json
{
  "TypeFullName": "SoftwareHouse.NextGen.Common.SecurityObjects.Personnel",
  "WhereClause": "Int1 = 'jsmith'",
  "pagesize": 2000,
  "pagenumber": 1,
  "DisplayProperties": ["ObjectID", "GUID", "Name", "Int1"]
}

200
[ { "ObjectID": 5001, "GUID": "1111...", "Name": "Smith, Dave", "Int1": "jsmith" } ]
```

 **Paging.** Start at `pagenumber=1` and increment until a page comes back with no rows. An empty result set answers `404` rather than an empty array, which the read loops treat as "no more rows", not as an error.

 

## Read all clearances

 Unlike the query endpoint, this returns whole objects and needs no DisplayProperties or paging.

 
```
GET {endpoint}/api/Objects/GetAll/Clearance

200
[ { "ObjectID": 9001, "GUID": "aaaa...", "Name": "Library After Hours", "PartitionID": 1 } ]
```

 

## Read one object by id

 
```
GET {endpoint}/api/Objects/Get/Clearance/{objectId}
GET {endpoint}/api/Objects/Get/Personnel/{objectId}

200   the object, either bare or as a single-element array
404   no such object
```

 A clearance id that cannot exist makes a good connection test, since a healthy system answers 404: `/api/Objects/Get/Clearance/0`.

 

## Insert a clearance pair

 Form-encoded, not JSON. The parent is the Personnel record and the pair is sent as a child with parallel `PropertyNames` and `PropertyValues` arrays.

 
```
POST {endpoint}/api/Objects/PersistToContainer
Content-Type: application/x-www-form-urlencoded

Type=SoftwareHouse.NextGen.Common.SecurityObjects.Personnel
ID=5001
Children[0][Type]=SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair
Children[0][PropertyNames][0]=PersonnelID
Children[0][PropertyNames][1]=ClearanceID
Children[0][PropertyValues][0]=5001
Children[0][PropertyValues][1]=9001

200 / 201   returns the Personnel object
404         no such Personnel or no such Clearance
```

 **One child per call.** CCure does not accept bundled clearance ids, so the DAO loops and makes one call per membership rather than batching. The declared insert and delete batch sizes therefore describe how many memberships the framework hands over at once, not how many go in a single request.

 

## Delete a clearance pair

 Keyed on the **pair's own ObjectID**, not the clearance id.

 
```
POST {endpoint}/api/Objects/RemoveFromContainer
Content-Type: application/x-www-form-urlencoded

Type=SoftwareHouse.NextGen.Common.SecurityObjects.Personnel
ID=5001
Children[0][Type]=SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair
Children[0][ID]=70001

200
```

 A full sync has the pair ObjectID from the read pass. An incremental may not, so the DAO looks it up first with a GetAllWithCriteria on PersonnelID plus ClearanceID.

 

## Progress and logging

 The four paged read loops report progress via `assignProgressLabelTarget`, so an operator sees counts move on the daemon log screen during a long read. Write progress comes from the framework, once per batch.

 

## Sync back

 Supported on all three axes, handled by `CCureProvisioningTargetNativeSync`.

 

- **Groups and entities** are captured from the raw JSON at the `CCureApiCommands` read seams, so a CCure field the typed records do not model is still reachable via `nativeAttributesGroups` / `nativeAttributesEntities`. Pointers are top level, e.g. `/GUID`, because the JSON is flat.
- **DisplayProperties widening** is the CCure-specific piece: because the query endpoint only returns projected fields, the effective capture list is added to the outbound `DisplayProperties` before the request. Without it a configured native attribute would silently resolve to nothing.
- **Memberships** are captured on read and write-tracked on insert and delete, and never re-read.
- There are no object write hooks, because the provisioner never writes Clearances or Personnel.

 

## Mock service

 URL path for testing mock service: `/grouper/mockServices/ccure`. Set `grouper.is.mockServices = true` in grouper.hibernate.properties.

 

| Table | Holds |
| --- | --- |
| `mock_ccure_auth` | issued sessions |
| `mock_ccure_personnel` | personnel_id, guid, name, int1 |
| `mock_ccure_clearance` | object_id, guid, name, partition_id |
| `mock_ccure_clearance_pair` | object_id, personnel_id, clearance_id |

 The mock reproduces the real behaviors above: projection by DisplayProperties, paging with a 404 on an empty page, one child per persist call, assigning the pair ObjectID on insert, and 404 for an unknown Personnel or Clearance.

 It validates the posted credentials against a real external system config rather than hardcoding them, reading `grouperTest.ccure.mock.configId` from grouper.properties and falling back to the placeholder config id `myCCure`.

 

## Notes for test authors

 The mock runs in the Tomcat JVM while the test runs in its own, and they read the same config from the database at different refresh intervals. Two consequences:

 

- Writing external system config and then immediately authenticating races Tomcat's config reload. When it loses, the mock reads no username and answers `401 "User not in system"`. Write config once, up front, and wait for the refresh - do not rewrite it per test.
- The mock validates against the same config the client posts from, so changing a password moves both sides together and the login still succeeds. To test a credential failure, post from a **different** config id whose password does not match.
