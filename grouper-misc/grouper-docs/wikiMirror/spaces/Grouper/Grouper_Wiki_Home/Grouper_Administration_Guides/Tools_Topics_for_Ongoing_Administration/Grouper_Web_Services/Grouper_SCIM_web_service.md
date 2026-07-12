---
title: "Grouper SCIM web service"
space: Grouper
pageId: 28549538
version: 4
lastUpdated: 2025-06-20T18:13:21.376Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549538/Grouper+SCIM+web+service
---

A Grouper web service container (i.e. a container started via `ws` or `quickstart`) is also capable of serving as a SCIM 2 server. To enable it, set either of the following:

- Environment variable GROUPER_SCIM=true
- In hibernate.properties, grouper.is.scim=true

To enable the built-in Basic authentication for SCIM, set either:

- Environment variable GROUPER_SCIM_GROUPER_AUTH=true
- hibernate.properties, grouper.is.scim.basicAuthn=true

The base URL endpoint will be /grouper-ws/scim/. For example, to retrieve a group, the URL would be similar to http://localhost:8080/grouper-ws/scim/v2/Groups/1c890f7b78ab4a18a79be250106fd073.

The content type in the request must be either "application/scim+json" or "application/json".

In addition to the standard SCIM object schemas, data is extended with custom TIER schemas. This allows mapping of more than the basic attributes. For example, for a group, the id path, description, and id index can be retrieved and set, in addition to the standard displayName and members attributes.

## Supported operations

### Groups

The SCIM server supports retrieving, searching, create, update, and delete. It does not support PATCH or the /.search endpoint. To add a member to a group, use the Create membership endpoint.

#### Retrieve group by ID:

Endpoint: GET /grouper-ws/scim/v2/Groups/{id}

The id parameter by itself refers to the uuid of the group. However, if the id starts with "systemName:", it will search the group by name, and if it starts with "idIndex" it will search by the idIndex of the group.

To retrieve a group by other fields, use a filter as below.

#### Retrieve group with filter

Endpoint: GET /grouper-ws/scim/v2/Groups?filter={filter}&startIndex={startIndex}&itemsPerPage={itemsPerPage}

Filter is using the SCIM syntax. For example, `displayName eq "manager"`. The search term must be in double quotes if it is a string. The spaces between terms is required, and must be URL encoded with + or %20 for use in the URL.

Note: startIndex is not currently implemented. If included, the value must be blank

Supported filters:

EQUALS (eq)

- name
- idIndex
- displayName
- extension
- displayExtension
- uuid
- description

CONTAINS (co)

- displayName
- extension
- displayExtension
- description

#### Create group

Endpoint: POST /grouper-ws/scim/v2/Groups

Parameters:

- SCIM standard (schema "urn:ietf:params:scim:schemas:core:2.0:Group"):
  
  - displayName (group display name)
  - members (array of user objects)
- TierGroupExtension (schema "urn:grouper:params:scim:schemas:extension:TierGroupExtension")
  
  - systemName (Grouper id path)
  - description (The group description)
  - idIndex (The group id index)

Example:

Request:

> curl --verbose -X POST [http://localhost:8080/grouper-ws/scim/v2/Groups](http://localhost:8080/grouper-ws/scim/v2/Groups) \  
> -H 'Accept: application/scim+json' \  
> -H 'Content-Type: application/scim+json' \  
> -H 'Authorization: Basic R3JvdXBlclN5c3RlbTpwYXNz' --data '  
> {  
>  "schemas": [  
>  "urn:ietf:params:scim:schemas:core:2.0:Group"  
>  ],  
>  "displayName": "etc:Test Group",  
>  "members": [  
>  {  
>  "value": "800001147"  
>  }  
>  ],  
>  "urn:grouper:params:scim:schemas:extension:TierGroupExtension": {  
>  "systemName": "etc:testGroup",  
>  "description": "TEST",  
>  "idIndex": 1000999  
>  }  
> }  
> '

Response:

> { "schemas": [ "urn:ietf:params:scim:schemas:core:2.0:Group", "urn:tier:params:scim:schemas:extension:TierMetaExtension", "urn:grouper:params:scim:schemas:extension:TierGroupExtension" ], "id": "5daaa0dc2973421eb55acab3d0cf4d91", "externalId": "etc:testGroup2", "meta": { "resourceType": "Group", "created": null, "lastModified": null, "location": null, "version": "78fbb8832bed0df42712319dfe76874ef4a8998a" }, "displayName": "etc:Test Group 2", "members": [ { "value": "c7037048dac749c2bff1425939c03874", "display": "Bob Anderson", "$ref": "../Users/800001147" } ], "urn:grouper:params:scim:schemas:extension:TierGroupExtension": { "schemas": [ "urn:grouper:params:scim:schemas:extension:TierGroupExtension" ], "systemName": "etc:testGroup2", "description": "TEST", "idIndex": 1000042 }, "urn:tier:params:scim:schemas:extension:TierMetaExtension": { "schemas": [ "urn:tier:params:scim:schemas:extension:TierMetaExtension" ], "resultCode": "SUCCESS", "responseDurationMillis": 5 } }

#### Update group

Endpoint: PUT /grouper-ws/scim/v2/Groups/{id}

The id refers to the UUID of the group.

Parameters:

- same as for Create group above

#### Delete group

Endpoint: DELETE /grouper-ws/scim/v2/Groups/{id}

The id refers to the UUID of the group.

### Users

#### retrieve user by subjectId or identifier

Endpoint: GET /grouper-ws/scim/v2/Users/{id}

#### Retrieve user with filter

Endpoint: GET /grouper-ws/scim/v2/Users?filter={filter}&startIndex={startIndex}&itemsPerPage={itemsPerPage}

Filter is using the SCIM syntax. For users, only the id eq "{subjectId}" and identifier eq "{subjectIdentifier}" filters are supported. The search term must be in double quotes if it is a string. The spaces between terms is required, and must be encoded with + or %20 for use in the URL.

Note: startIndex is not currently implemented. If included, the value must be blank

Supported filters:

EQUALS (eq)

- id
- identifier

### Memberships

#### Retrieve membership by membership UUID

Endpoint: GET /grouper-ws/scim/v2/Memberships/{id}

The id value is the membership UUID in Grouper, which is an internal field and not easily found except from a database query. However, they are returned as the reference id for the members attribute of groups.

#### Retrieve membership with filter

Endpoint: GET /grouper-ws/scim/v2/Memberships?filter={filter}&startIndex={startIndex}&itemsPerPage={itemsPerPage}

Filter is using the SCIM syntax. The membership implements both the "eq" and "and" operators. For example: groupName eq "etc:sysadmingroup" and subjectId eq "80001147". The search term must be in double quotes if it is a string. The spaces between terms is required, and must be encoded with + or %20 for use in the URL.

Note: startIndex is not currently implemented. If included, the value must be blank

Supported filters:

EQUALS (eq)

- groupName
- subjectId
- subjectIdentifier

#### Create membership

Endpoint: POST /grouper-ws/scim/v2/Memberships

The payload is of schema "urn:tier:params:scim:schemas:Membership", which includes the following attributes:

- membershipType (Membership Type, allowed values composite, immediate, or effective)
- enabled (is membership enabled, Java boolean)
- id (membership id)
- owner (Owner of this membership, as a Group object)
- member (Member of this membership -- Group or User, should be a value or $ref attribute)
- enabledTime (membership enabled time, Java LocalDateTime format)
- disabledTime (membership disabled time, Java LocalDateTime format)

Request

> curl --verbose -X POST http://localhost:8080/grouper-ws/scim/v2/Memberships \  
>  -H 'Accept: application/scim+json' \  
>  -H 'Content-Type: application/scim+json' \  
>  -H 'Authorization: Basic R3JvdXBlclN5c3RlbTpwYXNz' --data '  
> {  
>  "schemas": [  
>  "urn:tier:params:scim:schemas:Membership"  
>  ],  
>  "owner": {  
>  "value": "3c4a047d384447b28d3f159bc348a77c"  
>  },  
>  "member": {  
>  "value": "800001147"  
>  }  
> }  
> '

Response

> {  
>  "schemas": [  
>  "urn:tier:params:scim:schemas:Membership"  
>  ],  
>  "id": "08000729f5f646f1936d24e0a73a9e0c:8656bb87bea0417093030afeff48758e",  
>  "externalId": null,  
>  "meta": {  
>  "resourceType": "Membership",  
>  "created": null,  
>  "lastModified": null,  
>  "location": null,  
>  "version": "60a50020942daf6d0c4f49cda09c6dc465e40acf"  
>  },  
>  "membershipType": "immediate",  
>  "enabled": true,  
>  "owner": {  
>  "value": "3c4a047d384447b28d3f159bc348a77c",  
>  "display": "test:createMshipTest",  
>  "type": null,  
>  "$ref": "../Groups/3c4a047d384447b28d3f159bc348a77c"  
>  },  
>  "member": {  
>  "value": "800001147",  
>  "display": "Bob Anderson",  
>  "$ref": "../Users/800001147"  
>  },  
>  "enabledTime": null,  
>  "disabledTime": null  
> }

#### Update membership

Endpoint: PUT /grouper-ws/scim/v2/Memberships/{id}

The id value is the membership UUID in Grouper, which is an internal field and not easily found except from a database query. However, they are returned as the reference id for the members attribute of groups.

The payload is of schema "urn:tier:params:scim:schemas:Membership", with attributes describe in "Create membership" above.

The only values to modify for a membership are the enabled data and disabled date.

#### Delete membership

Endpoint: DELETE /grouper-ws/scim/v2/Memberships/{id}

The id value is the membership UUID in Grouper, which is an internal field and not easily found except from a database query. However, they are returned as the reference id for the members attribute of groups.

## Miscellaneous endpoints

Basic server information and documentation link:

- GET /grouper-ws/scim/v2/ServiceProviderConfig

Description of the User, Group, and Membership schemas

- GET /grouper-ws/scim/v2/ResourceTypes

Description of the User, Group, or Membership schema (individually)

- GET /grouper-ws/scim/v2/ResourceTypes/{ResourceType}

## Custom Schemas

The standard SCIM resources are enhanced with custom extensions, to provide more Grouper-related information.

### Tier Group Resource

- Name: TierGroupExtension
- Description: Tier extension for group
- Id: urn:grouper:params:scim:schemas:extension:TierGroupExtension
- Attributes
  
  - systemName (Grouper id path)
  - description (The group description)
  - idIndex (The group id index)

### TierMetaExtension

- Name: TierMetaExtension
- Description: Tier metadata extension for result status
- Id: urn:tier:params:scim:schemas:extension:TierMetaExtension
- Attributes
  
  - resultCode (Grouper result code)
  - responseDurationMillis (Grouper time to respond in milliseconds)

## Source and unit tests

The [source code](https://github.com/Internet2/grouper/tree/GROUPER_4_BRANCH/grouper-ws/grouper-ws/src/grouper-ws/edu/internet2/middleware/grouper/ws/scim) for the SCIM server is available. Source for the [full set of unit tests](https://github.com/Internet2/grouper/tree/GROUPER_4_BRANCH/grouper-ws/grouper-ws/src/test/edu/internet2/middleware/grouper/ws/scim) for all of the endpoints shows the proper format for URLs, query parameters, and json payloads for successful interactions.
