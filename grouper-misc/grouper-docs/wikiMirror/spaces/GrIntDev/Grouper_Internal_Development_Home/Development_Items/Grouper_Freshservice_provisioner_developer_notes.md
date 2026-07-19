---
title: "Grouper Freshservice provisioner developer notes"
space: GrIntDev
pageId: 48792517
version: 35
lastUpdated: 2026-07-12T06:45:25.823Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792517/Grouper+Freshservice+provisioner+developer+notes
---

## Log in to UI

- (Log in) https://somedomainname.freshservice.com
- (Docs) [https://api.freshservice.com/](https://api.freshservice.com/)

## External system

[Freshservice external system documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547422/Grouper+Freshservice+external+system)

The authentication is a little frustrating. They have basic auth with the password in the user portion of the Authorization header, and X as the password. An enhancement was made to Grouper to swap the user/pass to accommodate this

## Provisioner general

Rate limiting seems standard: [https://api.freshservice.com/#rate_limit](https://api.freshservice.com/#rate_limit)

Paging. Start of ?per_page=100&page=1, and go through until no results (200 all around)

## Group

Mock tables should have

Primary key: id

| mock_freshreq_group |
| --- |
| Column | Type | Description |
| id | bigint | assigned by Freshservice, not null after create, readonly |
| name | varchar(256) | name of group, not null |
| description | varchar(1024) | description, nullable |

Mappable provisionable attributes: id, name, description

## User

Crud: select, insert, update, delete

Primary key: id

| mock_freshreq_user |
| --- |
| Column | Type | Description |
| id | bigint | assigned by Freshservice, not null after create, readonly |
| email | varchar(256) | case insensitive, generally not null |
| active | varchar(1) | T or F |
| first_name | varchar(256) |  |
| last_name | varchar(256) |  |
| address | varchar(512) |  |
| external_id | varchar(256) | put whatever ID you want in there, preferably opaque and unchanging |
| custom_fields | varchar(4000) | json for custom fields |
| department_id | bigint |  |
| job_title | varchar(256) |  |
| reporting_manager_id | bigint |  |
| work_phone_number | varchar(50) |  |

Mappable provisioning attributes: active, address, departmentId, email, firstName, id, isAgent, jobTitle, lastName, reportingManagerId, workPhoneNumber

## Membership

Crud: select, insert, update, delete

Primary key is config_id, group_id, user_id

| mock_freshreq_membership |
| --- |
| Column | Type | Description |
| id | bigint | random integer |
| group_id | bigint | id from group |
| user_id | bigint | id from user |

## Get groups

/api/v2/requester_groups?per_page=100&page=1

Start at page 1, and increment until no results found.

Note, groups of type "rule_based" are tossed, since the API cannot manage them

```
Authorization: Basic abc123
GET $SERVICE_URL$/api/v2/requester_groups?per_page=100&page=1

{
    "requester_groups": [
        {
            "id": 39000032974,
            "name": "Arboretum",
            "description": null,
            "workspace_id": 1,
            "type": "manual"
        },
        {
            "id": 39000021274,
            "name": "Change Requesters",
            "description": null,
            "workspace_id": 1,
            "type": "rule_based"
        }
    ]
}
```

## Get group

/api/v2/requester_groups/1234567

Note, groups of type "rule_based" are tossed, since the API cannot manage them

404 if not found

```
Authorization: Basic abc123
Content-Type: application/json
GET $SERVICE_URL$/api/v2/requester_groups/1234567

{
    "requester_group": {
        "id": 39000032974,
        "name": "Arboretum",
        "description": null,
        "workspace_id": 1,
        "type": "manual"
    }
}
```

## Create group

/api/v2/requester_groups

```
POST /api/v2/requester_groups
Content-Type: application/json

{ "name": "Branch Managers", "description": "Requester group for branch managers across all locations" }

```

Response 200 if created. 409 if already exists

```
{
    "requester_group": {
        "id": 39000128904,
        "name": "testing123",
        "description": "Requester group for testing",
        "workspace_id": 1,
        "type": "manual"
    }
}
```

## Delete group

/api/v2/requester_groups/1234567

```
DELETE /api/v2/requester_groups/1234567
```

Returns a 204 if successful. Returns a 404 if already deleted.

## Update group

/api/v2/requester_groups/1234567

```
PUT /api/v2/requester_groups/1234567
Content-Type: application/json

{ "name":"Human Resources", "description":"Requester group all employees whose department is HR" }

200

{
    "requester_group": {
        "id": 1234567,
        "name": "Human Resources",
        "description": "Requester group all employees whose department is HR",
        "workspace_id": 1,
        "type": "manual"
    }
}
```

## Get all requesters

/api/v2/requesters?per_page=100&page=1

Start at page 1, and increment until no results found.

```
Authorization: Basic abc123
GET $SERVICE_URL$/api/v2/requesters?per_page=100&page=1
200

{
    "requesters": [
        {
            "active": true,
            "address": null,
            "background_information": null,
            "can_see_all_changes_from_associated_departments": false,
            "can_see_all_tickets_from_associated_departments": false,
            "created_at": "2026-02-03T22:20:49Z",
            "custom_fields": {
                "pennkey": "jsmith",
                "penn_id": "12345678"
            },
            "department_ids": [
                39000211201
            ],
            "department_names": [
                "9571 - Residential Operations"
            ],
            "external_id": "12345678",
            "first_name": "John",
            "has_logged_in": false,
            "id": 39002531630,
            "is_agent": false,
            "job_title": "Worker",
            "language": "en",
            "last_name": "Smith",
            "location_id": null,
            "location_name": null,
            "mobile_phone_number": null,
            "primary_email": "jsmith@example.com",
            "reporting_manager_id": null,
            "secondary_emails": [],
            "time_format": "12h",
            "time_zone": "Eastern Time (US & Canada)",
            "updated_at": "2026-02-03T22:20:49Z",
            "vip_user": false,
            "work_phone_number": null,
            "work_schedule_id": null
        }
    ]
}
```

## Get requester by ID

/api/v2/requesters/1234567

200 if found

404 if not found

```
Authorization: Basic abc123
GET $SERVICE_URL$/api/v2/requesters/1234567
200

{
    "requester": {
            "active": true,
            "address": null,
            "background_information": null,
            "can_see_all_changes_from_associated_departments": false,
            "can_see_all_tickets_from_associated_departments": false,
            "created_at": "2026-02-03T22:20:49Z",
            "custom_fields": {
                "pennkey": "jsmith",
                "penn_id": "12345678"
            },
            "department_ids": [
                39000211201
            ],
            "department_names": [
                "9571 - Residential Operations"
            ],
            "external_id": "12345678",
            "first_name": "John",
            "has_logged_in": false,
            "id": 39002531630,
            "is_agent": false,
            "job_title": "Worker",
            "language": "en",
            "last_name": "Smith",
            "location_id": null,
            "location_name": null,
            "mobile_phone_number": null,
            "primary_email": "jsmith@example.com",
            "reporting_manager_id": null,
            "secondary_emails": [],
            "time_format": "12h",
            "time_zone": "Eastern Time (US & Canada)",
            "updated_at": "2026-02-03T22:20:49Z",
            "vip_user": false,
            "work_phone_number": null,
            "work_schedule_id": null
        }
}
```

## Get requester by email

/api/v2/requesters?email=jsmith@example.com

200 if found

```
Authorization: Basic abc123
GET $SERVICE_URL$/api/v2/requesters?email=jsmith@example.com
200

{
    "requesters": [
        {
            "active": true,
            "address": null,
            "background_information": null,
            "can_see_all_changes_from_associated_departments": false,
            "can_see_all_tickets_from_associated_departments": false,
            "created_at": "2026-02-03T22:20:49Z",
            "custom_fields": {
                "pennkey": "jsmith",
                "penn_id": "12345678"
            },
            "department_ids": [
                39000211201
            ],
            "department_names": [
                "9571 - Residential Operations"
            ],
            "external_id": "12345678",
            "first_name": "John",
            "has_logged_in": false,
            "id": 39002531630,
            "is_agent": false,
            "job_title": "Worker",
            "language": "en",
            "last_name": "Smith",
            "location_id": null,
            "location_name": null,
            "mobile_phone_number": null,
            "primary_email": "jsmith@example.com",
            "reporting_manager_id": null,
            "secondary_emails": [],
            "time_format": "12h",
            "time_zone": "Eastern Time (US & Canada)",
            "updated_at": "2026-02-03T22:20:49Z",
            "vip_user": false,
            "work_phone_number": null,
            "work_schedule_id": null
        }
    ]
}
```

200 if not found with no results

```
{
    "requesters": []
}
```

## Get requester by attribute

Can search by external_id or a unique custom attribute

GET /api/v2/requesters?query=pennkey:'jsmith'

If the value is a number:

GET /api/v2/requesters?query=pennid:12345678

Note: for grouper, this is a lookup and should return null or one user. If there are multiple users it should throw a descriptive exception

```
GET /api/v2/requesters?query=pennkey:'jsmith'
200
{
    "requesters": [
        {
            "active": true,
            "address": null,
            "background_information": null,
            "can_see_all_changes_from_associated_departments": false,
            "can_see_all_tickets_from_associated_departments": false,
            "created_at": "2026-02-18T01:24:20Z",
            "custom_fields": {
                "pennkey": "jsmith",
                "penn_id": "12345678"
            },
            "department_ids": [
                39000211201
            ],
            "department_names": [
                "9571 - Residential Operations"
            ],
            "external_id": "12345678",
            "first_name": "John",
            "has_logged_in": false,
            "id": 39003207520,
            "is_agent": false,
            "job_title": "Worker",
            "language": "en",
            "last_name": "Smith",
            "location_id": null,
            "location_name": null,
            "mobile_phone_number": null,
            "primary_email": "jsmith@example.com",
            "reporting_manager_id": null,
            "secondary_emails": [],
            "time_format": "12h",
            "time_zone": "Eastern Time (US & Canada)",
            "updated_at": "2026-02-18T01:24:20Z",
            "vip_user": false,
            "work_phone_number": null,
            "work_schedule_id": null
        }
    ]
}

```

## Create requester

/api/v2/requesters

```
POST /api/v2/requesters
Content-Type: application/json

{
    "requester": {
        "active": true,
        "address": null,
        "background_information": null,
        "can_see_all_changes_from_associated_departments": false,
        "can_see_all_tickets_from_associated_departments": false,
        "created_at": "2026-02-15T07:45:20Z",
        "custom_fields": {
            "pennkey": "jsmith",
            "penn_id": "12345678"
        },
        "department_ids": [
            39000211201
        ],
        "department_names": [
            "9571 - Residential Operations"
        ],
        "external_id": "12345678",
        "first_name": "John",
        "has_logged_in": false,
        "id": 39002927971,
        "is_agent": false,
        "job_title": "Worker",
        "language": "en",
        "last_name": "Smith",
        "location_id": null,
        "location_name": null,
        "mobile_phone_number": null,
        "primary_email": "jsmith@example.com",
        "reporting_manager_id": null,
        "secondary_emails": [],
        "time_format": "12h",
        "time_zone": "Eastern Time (US & Canada)",
        "updated_at": "2026-02-15T07:45:20Z",
        "vip_user": false,
        "work_phone_number": null,
        "work_schedule_id": null
    }
}

```

Response 201 if created. 409 if already exists

```
{
    "requester": {
        "active": true,
        "address": null,
        "background_information": null,
        "can_see_all_changes_from_associated_departments": false,
        "can_see_all_tickets_from_associated_departments": false,
        "created_at": "2026-02-16T01:07:44Z",
        "custom_fields": {
            "pennkey": "jsmith",
            "penn_id": "12345678"
        },
        "department_ids": [
            39000211201
        ],
        "department_names": [
            "9571 - Residential Operations"
        ],
        "external_id": "12345678",
        "first_name": "John",
        "has_logged_in": false,
        "id": 39003000136,
        "is_agent": false,
        "job_title": "Worker",
        "language": "en",
        "last_name": "Smith",
        "location_id": null,
        "location_name": null,
        "mobile_phone_number": null,
        "primary_email": "jsmith@example.com",
        "reporting_manager_id": null,
        "secondary_emails": [],
        "time_format": "12h",
        "time_zone": "Eastern Time (US & Canada)",
        "updated_at": "2026-02-16T01:07:44Z",
        "vip_user": false,
        "work_phone_number": null,
        "work_schedule_id": null
    }
}
```

## Delete requester (deactivate)

/api/v2/requesters/1234567

```
DELETE /api/v2/requesters/1234567
```

Returns a 204 if successful. Returns a 404 if already deleted.

## Reactivate requester

PUT /api/v2/requesters/[id]/reactivate

```
PUT /api/v2/requesters/12345678/reactivate
```

Returns a 200 if successful. 400 with body if already active

```
{
    "code": "contact_already_active",
    "message": "Contact is already active and cannot be restored."
}
```

## Delete requester (forget / permanent)

/api/v2/requesters/1234567

```
DELETE /api/v2/requesters/1234567
```

Returns a 204 if successful. Returns a 404 if already deleted.

## Update requester

/api/v2/requesters/1234567

Take out from GET: id, created_at, has_logged_in, is_agent, updated_at, work_schedule_id, department_names, location_name

```
PUT /api/v2/requesters/1234567
Content-Type: application/json

{
        "address": null,
        "background_information": null,
        "can_see_all_changes_from_associated_departments": false,
        "can_see_all_tickets_from_associated_departments": false,
        "custom_fields": {
            "pennkey": "jsmith2",
            "penn_id": "12345679"
        },
        "department_ids": [
            39000211201
        ],
        "first_name": "John",
        "job_title": "Worker",
        "language": "en",
        "last_name": "Smith",
        "location_id": null,
        "mobile_phone_number": null,
        "primary_email": "jsmith2@example.com",
        "reporting_manager_id": null,
        "secondary_emails": [],
        "time_format": "12h",
        "time_zone": "Eastern Time (US & Canada)",
        "work_phone_number": null
}

200

RESPONSE:

{
    "requester": {
        "active": true,
        "address": null,
        "background_information": null,
        "can_see_all_changes_from_associated_departments": false,
        "can_see_all_tickets_from_associated_departments": false,
        "created_at": "2026-02-16T01:07:44Z",
        "custom_fields": {
            "pennkey": "jsmith2",
            "penn_id": "12345679"
        },
        "department_ids": [
            39000211201
        ],
        "department_names": [
            "9571 - Residential Operations"
        ],
        "external_id": "12345678",
        "first_name": "John",
        "has_logged_in": true,
        "id": 39003000136,
        "is_agent": false,
        "job_title": "Worker",
        "language": "en",
        "last_name": "Smith",
        "location_id": null,
        "location_name": null,
        "mobile_phone_number": null,
        "primary_email": "jsmith2@example.com",
        "reporting_manager_id": null,
        "secondary_emails": [],
        "time_format": "12h",
        "time_zone": "Eastern Time (US & Canada)",
        "updated_at": "2026-02-16T17:12:07Z",
        "vip_user": false,
        "work_phone_number": null,
        "work_schedule_id": null
    }
}
```

## Add group membership

/api/v2/requester_groups/39000128925/members/39003000136

```
POST /api/v2/requester_groups/39000128925/members/39003000136

200 if created
200 if already existed
```

## Remove group membership

/api/v2/requester_groups/39000128925/members/39003000136

```
DELETE /api/v2/requester_groups/39000128925/members/39003000136

204 if removed
404 if membership did not exist
```

## List group memberships

/api/v2/requester_groups/39000128925/members?per_page=100&page=1

```
GET /api/v2/requester_groups/39000128925/members?per_page=100&page=1

200

{
    "requesters": [
        {
            "email": "jsmith@example.com",
            "first_name": "John",
            "id": 39003000136,
            "last_name": "Smith"
        }
    ]
}

```

## List departments (separate from provisioning)

```
GET /api/v2/departments?per_page=100&page=1
200

{
    "departments": [
        {
            "description": null,
            "custom_fields": {},
            "id": 39000213538,
            "name": "0006 - General University Student Fin Service",
            "created_at": "2026-02-03T19:05:39Z",
            "updated_at": "2026-02-03T19:05:39Z",
            "head_name": null,
            "prime_user_id": null,
            "prime_user_name": null,
            "domains": [],
            "head_user_id": null,
            "workspace_id": 1
        }
    ]
}
```

## Create department (separate from provisioning)

```
POST /api/v2/departments

{ 
  "name":"Department for the Regulation and Control of Magical Creatures",
  "description":"Beast, Being and Spirit Divisions, and Pest Advisory Bureau."
}

200

{
    "department": {
        "description": "Beast, Being and Spirit Divisions, and Pest Advisory Bureau.",
        "custom_fields": {},
        "id": 39000380032,
        "name": "Department for the Regulation and Control of Magical Creatures",
        "created_at": "2026-02-17T17:07:03Z",
        "updated_at": "2026-02-17T17:07:03Z",
        "head_name": null,
        "prime_user_id": null,
        "prime_user_name": null,
        "domains": [],
        "head_user_id": null,
        "workspace_id": 1
    }
}
```

## Update department (separate from provisioning)

```
PUT /api/v2/departments/39000380032

{
    "department": {
        "description": "Beast, Being and Spirit Divisions, and Pest Advisory Bureau.",
        "custom_fields": {},
        "name": "Department for the Regulation and Control of Magical Creatures2",
        "prime_user_id": null,
        "domains": [],
        "head_user_id": null,
        "workspace_id": 1
    }
}

200
{
    "department": {
        "description": "Beast, Being and Spirit Divisions, and Pest Advisory Bureau.",
        "custom_fields": {},
        "id": 39000380032,
        "name": "Department for the Regulation and Control of Magical Creatures2",
        "created_at": "2026-02-17T17:07:03Z",
        "updated_at": "2026-02-17T18:00:38Z",
        "head_name": null,
        "prime_user_id": null,
        "prime_user_name": null,
        "domains": [],
        "head_user_id": null,
        "workspace_id": 1
    }
}
```

## Delete department (separate from provisioning)

```
DELETE /api/v2/departments/39000380032

204 if deleted, 404 if not found
```
