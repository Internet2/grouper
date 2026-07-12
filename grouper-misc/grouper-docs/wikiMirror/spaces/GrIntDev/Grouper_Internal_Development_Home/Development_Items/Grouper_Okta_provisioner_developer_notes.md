---
title: "Grouper Okta provisioner developer notes"
space: GrIntDev
pageId: 48792584
version: 8
lastUpdated: 2026-07-12T06:45:30.532Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792584/Grouper+Okta+provisioner+developer+notes
---

## Log in to UI

- (Log in) [https://trial-8031936-admin.okta.com/admin/getting-started](https://trial-8031936-admin.okta.com/admin/getting-started)
- (Docs) [https://developer.okta.com/docs/api/](https://developer.okta.com/docs/api/), [https://developer.okta.com/docs/reference/rest/](https://developer.okta.com/docs/reference/rest/)

## External system

External system type: Okta

Domain URL: [https://trial-8031936.okta.com/](https://trial-8031936.okta.com/oauth2/v1/token)

Client id: sdf

Private key: sdf

Test by:

1. Generating JWT using [https://www.jsonwebtoken.dev/](https://www.jsonwebtoken.dev/).
  
  
  ```
  {
  "aud": "https://trial-8031936.okta.com/oauth2/v1/token",
  "iss": "clientId",
  "sub": "clientId", 
  "exp": "1738490"
  }
  
  
  ```
  
  
  ```
  {
      "d": "sdf",
      "p": "sdf",
      "q": "sdf",
      "dp": "sdf",
      "dq": "sdf",
      "qi": "sdf",
      "kty": "RSA",
      "e": "AQAB",
      "kid": "B1Dlu1c5yd8TNtvC5x04EkVzoIdFHX5TeHjLoNRzrf4",
      "n": "sdf"
  }
  ```
  
  2.
  
  
  ```
  curl --location --request POST 'https://trial-8031936.okta.com/oauth2/v1/token' \
      --header 'Accept: application/json' \
      --header 'Content-Type: application/x-www-form-urlencoded' \
      --data-urlencode 'grant_type=client_credentials' \
      --data-urlencode 'scope=okta.users.manage okta.groups.manage' \
      --data-urlencode 'client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer' \
      --data-urlencode 'client_assertion=jwt from step one'
  ```

Get the access token (expires is in seconds):

```
{"access_token":"abc123","token_type":"bearer","expires_in":86399}
```

Then execute the API url, put the access token as Authorization: Bearer ACCESS_TOKEN

```
curl --location 'https://trial-8031936.okta.com/api/v1/users' \
--header 'Authorization: Bearer nDjz3HOyP5Z-1bHGmZ9hwJoR039lYdiA5v7vc-KaODdKGYbqRGNLpUrgEb7liZbZUXjzZ2VOxI0lARUtoP6eFvsOMnRPmDMBwjQha6Gik6e-YimE_tVIaIQRqcumrnwEFKqrOn2de-9om9DYgdNZMWyO0cjATmAydrR_mR8cHUmAsKtxhA-VTNDA7Ao9Q'
```

## Provisioner general

Config for Org ID: e.g. ABC101@AdobeOrg

Config for Delete account when delete user: true/false (defaults false)

Config for "User type on create", three drop down options: AdobeID, EnterpriseID, FederatedID. (this is not required, and federated ID is the default). Description is: "AdobeID: An Identity Type that is created, owned, and managed by the end user. Adobe performs authentication, and the end user manages the identity. Users retain complete control over files and data associated with their ID.<br /><br />EnterpriseID (default): An Identity Type that is created, owned, and managed by an organization. Adobe hosts the Enterprise ID and performs authentication, but the organization maintains the Enterprise ID. End-users cannot sign up and create an Enterprise ID, nor can they sign up for additional products and services from Adobe using an Enterprise ID.<br /><br />FederatedID: An Identity Type that is created and owned by an organization, and linked to the enterprise directory through federation. The organization manages credentials and processes Single Sign-On through a SAML2 identity provider. UMAPI clients with email-federated domains must always identity users by email."

Whenever $ORG_ID$ is referenced below, substitute with this config

Whenever $SERVICE_URL$ is referenced below, substitute with

Returns 429 (and Retry-After) if there are too many requests, the Grouper HTTP client should just back off by default. The value is seconds.

```
Retry-After: 38
```

## Group

Mock and prov tables should have

Primary key: config_id, group_id

| mock_okta_group |
| --- |
| Column | Type | Description |
| config_id | varchar(100) | (for prov table only, the provisioner config id) |
| group_id | bigint | increment this, readonly |
| group_name | varchar(2000) | unique group name |
| description | varchar(100) |  |

Mappable provisionable attributes: id, name, description

## User

Crud: select, insert, update, delete

Primary key: config_id, id

| mock_okta_user |
| --- |
| Column | Type | Description |
| config_id | varchar(100) | (for prov table only, the provisioner config id) |
| id | varchar(100) | uuid, readonly |
| email | varchar(256) | case insensitive |
| login | varchar(256) |  |
| status | varchar(30) | e.g. active |
| firstName | varchar(100) |  |
| lastName | varchar(100) |  |

Mappable provisioning attributes: id, email, firstName, lastName, login

## Membership

Crud: select, insert, update, delete

Primary key is config_id, group_id, user_id

| mock_okta_mship |
| --- |
| Column | Type | Description |
| config_id | varchar(100) | (for prov table only, the provisioner config id) |
| group_id | bigint | group_id |
| user_id | varchar(100) | user_id |

## Get groups

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/listGroups](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/listGroups)

Pagination is based on cursor and Link response header is used to retrieve next page cursor. See [https://developer.okta.com/docs/api/#pagination](https://developer.okta.com/docs/api/#pagination)

Should probably expirable cache for 10 minutes all groups by config id in static map

```
GET https://trial-8031936.okta.com/api/v1/groups 

 [
    {
        "id": "00gmvgfan9BtTaT4z697",
        "created": "2024-12-22T17:30:18.000Z",
        "lastUpdated": "2024-12-22T17:30:18.000Z",
        "lastMembershipUpdated": "2024-12-25T00:02:35.000Z",
        "objectClass": [
            "okta:user_group"
        ],
        "type": "BUILT_IN",
        "profile": {
            "name": "Everyone",
            "description": "All users in your organization"
        },
        "_links": {
            "logo": [
                {
                    "name": "medium",
                    "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-medium.30ce6d4085dff29412984e4c191bc874.png",
                    "type": "image/png"
                },
                {
                    "name": "large",
                    "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-large.c3cb8cda8ae0add1b4fe928f5844dbe3.png",
                    "type": "image/png"
                }
            ],
            "users": {
                "href": "https://trial-8031936.okta.com/api/v1/groups/00gmvgfan9BtTaT4z697/users"
            },
            "apps": {
                "href": "https://trial-8031936.okta.com/api/v1/groups/00gmvgfan9BtTaT4z697/apps"
            }
        }
    },
    {
        "id": "00gmvgfao3IzeDs6t697",
        "created": "2024-12-22T17:30:19.000Z",
        "lastUpdated": "2024-12-22T17:30:19.000Z",
        "lastMembershipUpdated": "2024-12-22T17:30:19.000Z",
        "objectClass": [
            "okta:user_group"
        ],
        "type": "BUILT_IN",
        "profile": {
            "name": "Okta Administrators",
            "description": "Okta manages this group, which contains all administrators in your organization."
        },
        "_links": {
            "logo": [
                {
                    "name": "medium",
                    "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-medium.30ce6d4085dff29412984e4c191bc874.png",
                    "type": "image/png"
                },
                {
                    "name": "large",
                    "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-large.c3cb8cda8ae0add1b4fe928f5844dbe3.png",
                    "type": "image/png"
                }
            ],
            "users": {
                "href": "https://trial-8031936.okta.com/api/v1/groups/00gmvgfao3IzeDs6t697/users"
            },
            "apps": {
                "href": "https://trial-8031936.okta.com/api/v1/groups/00gmvgfao3IzeDs6t697/apps"
            }
        }
    },
    {
        "id": "00gmvgcs9mpZKSfAX697",
        "created": "2024-12-22T17:34:45.000Z",
        "lastUpdated": "2024-12-22T17:34:45.000Z",
        "lastMembershipUpdated": "2024-12-22T17:36:12.000Z",
        "objectClass": [
            "okta:user_group"
        ],
        "type": "OKTA_GROUP",
        "profile": {
            "name": "testGroup",
            "description": null
        },
        "_links": {
            "logo": [
                {
                    "name": "medium",
                    "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-medium.30ce6d4085dff29412984e4c191bc874.png",
                    "type": "image/png"
                },
                {
                    "name": "large",
                    "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-large.c3cb8cda8ae0add1b4fe928f5844dbe3.png",
                    "type": "image/png"
                }
            ],
            "users": {
                "href": "https://trial-8031936.okta.com/api/v1/groups/00gmvgcs9mpZKSfAX697/users"
            },
            "apps": {
                "href": "https://trial-8031936.okta.com/api/v1/groups/00gmvgcs9mpZKSfAX697/apps"
            }
        }
    }
]
```

## Create group

[https://trial-8031936.okta.com/api/v1/groups](https://trial-8031936.okta.com/api/v1/groups)

```
POST https://trial-8031936.okta.com/api/v1/groups 

 {
    "profile": {
        "description": "All users West of The Rockies",
        "name": "West Coast users"
    }
}   
```

Response: 200

```
{
    "id": "00gmxp8w6iYQLHtEN697",
    "created": "2024-12-25T00:33:02.000Z",
    "lastUpdated": "2024-12-25T00:33:02.000Z",
    "lastMembershipUpdated": "2024-12-25T00:33:02.000Z",
    "objectClass": [
        "okta:user_group"
    ],
    "type": "OKTA_GROUP",
    "profile": {
        "name": "West Coast users",
        "description": "All users West of The Rockies"
    },
    "_links": {
        "logo": [
            {
                "name": "medium",
                "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-medium.30ce6d4085dff29412984e4c191bc874.png",
                "type": "image/png"
            },
            {
                "name": "large",
                "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-large.c3cb8cda8ae0add1b4fe928f5844dbe3.png",
                "type": "image/png"
            }
        ],
        "users": {
            "href": "https://trial-8031936.okta.com/api/v1/groups/00gmxp8w6iYQLHtEN697/users"
        },
        "apps": {
            "href": "https://trial-8031936.okta.com/api/v1/groups/00gmxp8w6iYQLHtEN697/apps"
        }
    }
}
```

## Get group

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/getGroup](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/getGroup)

```
GET https://trial-8031936.okta.com/api/v1/groups/00gmxp8w6iYQLHtEN697

Response: 200
{
    "id": "00gmxp8w6iYQLHtEN697",
    "created": "2024-12-25T00:33:02.000Z",
    "lastUpdated": "2024-12-25T00:33:02.000Z",
    "lastMembershipUpdated": "2024-12-25T00:33:02.000Z",
    "objectClass": [
        "okta:user_group"
    ],
    "type": "OKTA_GROUP",
    "profile": {
        "name": "West Coast users",
        "description": "All users West of The Rockies"
    },
    "_links": {
        "logo": [
            {
                "name": "medium",
                "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-medium.30ce6d4085dff29412984e4c191bc874.png",
                "type": "image/png"
            },
            {
                "name": "large",
                "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-large.c3cb8cda8ae0add1b4fe928f5844dbe3.png",
                "type": "image/png"
            }
        ],
        "users": {
            "href": "https://trial-8031936.okta.com/api/v1/groups/00gmxp8w6iYQLHtEN697/users"
        },
        "apps": {
            "href": "https://trial-8031936.okta.com/api/v1/groups/00gmxp8w6iYQLHtEN697/apps"
        }
    }
}
```

## Delete group

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/deleteGroup](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/deleteGroup)

```
DELETE https://trial-8031936.okta.com/api/v1/groups/00gmxp8w6iYQLHtEN697   

Response code: 204

```

## Update group

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/usergroupActionCommands.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/usergroupActionCommands.html)

```
POST https://trial-8031936.okta.com/api/v1/groups/00gmxp8w6iYQLHtEN697

 {
    "profile": {
        "description": "All users in CA",
        "name": "California users"
    }
} 

Response 200  {
    "id": "00gmxp8w6iYQLHtEN697",
    "created": "2024-12-25T00:33:02.000Z",
    "lastUpdated": "2024-12-25T00:37:26.000Z",
    "lastMembershipUpdated": "2024-12-25T00:33:02.000Z",
    "objectClass": [
        "okta:user_group"
    ],
    "type": "OKTA_GROUP",
    "profile": {
        "name": "California users",
        "description": "All users in CA"
    },
    "_links": {
        "logo": [
            {
                "name": "medium",
                "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-medium.30ce6d4085dff29412984e4c191bc874.png",
                "type": "image/png"
            },
            {
                "name": "large",
                "href": "https://ok14static.oktacdn.com/assets/img/logos/groups/odyssey/okta-large.c3cb8cda8ae0add1b4fe928f5844dbe3.png",
                "type": "image/png"
            }
        ],
        "users": {
            "href": "https://trial-8031936.okta.com/api/v1/groups/00gmxp8w6iYQLHtEN697/users"
        },
        "apps": {
            "href": "https://trial-8031936.okta.com/api/v1/groups/00gmxp8w6iYQLHtEN697/apps"
        }
    }
}
```

## Get users

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/listUsers](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/listUsers)

Pagination is supported and is based on a cursor. Response header Link has the URL to call to retrieve next page of users. See [https://developer.okta.com/docs/api/#pagination](https://developer.okta.com/docs/api/#pagination)

```
######## GET USERS

GET  https://trial-8031936.okta.com/api/v1/users

 [
    {
        "id": "00umvgfnligMmTnZJ697",
        "status": "PROVISIONED",
        "created": "2024-12-22T17:35:42.000Z",
        "activated": "2024-12-22T17:35:42.000Z",
        "statusChanged": "2024-12-22T17:35:42.000Z",
        "lastLogin": null,
        "lastUpdated": "2024-12-22T17:35:42.000Z",
        "passwordChanged": null,
        "type": {
            "id": "otymvgfaniH2DFk3i697"
        },
        "profile": {
            "firstName": "Chris",
            "lastName": "Hyzer",
            "mobilePhone": null,
            "secondEmail": null,
            "login": "chris.hyzer@gmail.com",
            "email": "chris.hyzer@gmail.com"
        },
        "credentials": {
            "provider": {
                "type": "OKTA",
                "name": "OKTA"
            }
        },
        "_links": {
            "self": {
                "href": "https://trial-8031936.okta.com/api/v1/users/00umvgfnligMmTnZJ697"
            }
        }
    },
    {
        "id": "00umvgfarrDlLp4at697",
        "status": "ACTIVE",
        "created": "2024-12-22T17:30:22.000Z",
        "activated": "2024-12-22T17:30:22.000Z",
        "statusChanged": "2024-12-22T17:30:59.000Z",
        "lastLogin": "2024-12-24T21:19:07.000Z",
        "lastUpdated": "2024-12-22T17:30:59.000Z",
        "passwordChanged": "2024-12-22T17:30:59.000Z",
        "type": {
            "id": "otymvgfaniH2DFk3i697"
        },
        "profile": {
            "firstName": "Vivek",
            "lastName": "Sachdeva",
            "mobilePhone": null,
            "secondEmail": null,
            "login": "contact@viveksachdeva.com",
            "email": "contact@viveksachdeva.com"
        },
        "credentials": {
            "password": {},
            "provider": {
                "type": "OKTA",
                "name": "OKTA"
            }
        },
        "_links": {
            "self": {
                "href": "https://trial-8031936.okta.com/api/v1/users/00umvgfarrDlLp4at697"
            }
        }
    }
] 
```

## Get user

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/getUser](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/getUser)

response code: 200

```
GET https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697 

 {
    "id": "00umxoh7cgDm3zD9v697",
    "status": "PROVISIONED",
    "created": "2024-12-25T00:02:35.000Z",
    "activated": "2024-12-25T00:02:35.000Z",
    "statusChanged": "2024-12-25T00:02:35.000Z",
    "lastLogin": null,
    "lastUpdated": "2024-12-25T00:02:35.000Z",
    "passwordChanged": null,
    "type": {
        "id": "otymvgfaniH2DFk3i697"
    },
    "profile": {
        "firstName": "Test",
        "lastName": "Subject.0",
        "mobilePhone": null,
        "secondEmail": null,
        "login": "test.subject.0@grouper.com",
        "email": "test.subject.0@grouper.com"
    },
    "credentials": {
        "provider": {
            "type": "OKTA",
            "name": "OKTA"
        }
    },
    "_links": {
        "suspend": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/suspend",
            "method": "POST"
        },
        "schema": {
            "href": "https://trial-8031936.okta.com/api/v1/meta/schemas/user/oscmvgfaniH2DFk3i697"
        },
        "resetPassword": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/reset_password",
            "method": "POST"
        },
        "reactivate": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/reactivate",
            "method": "POST"
        },
        "self": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697"
        },
        "resetFactors": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/reset_factors",
            "method": "POST"
        },
        "type": {
            "href": "https://trial-8031936.okta.com/api/v1/meta/types/user/otymvgfaniH2DFk3i697"
        },
        "deactivate": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/deactivate",
            "method": "POST"
        }
    }
}
```

## Create user

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/createUser](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/createUser)

Response code: 200

```
POST https://trial-8031936.okta.com/api/v1/users
Accept: application/json
Content-Type: application/json  

{
  "profile": {
    "firstName": "Isaac",
    "lastName": "Brock",
    "email": "isaac.brock@example.com",
    "login": "isaac.brock@example.com"
  }
} 
```

Response: 200

```
{
    "id": "00umxoh7cgDm3zD9v697",
    "status": "PROVISIONED",
    "created": "2024-12-25T00:02:35.000Z",
    "activated": "2024-12-25T00:02:35.000Z",
    "statusChanged": "2024-12-25T00:02:35.000Z",
    "lastLogin": null,
    "lastUpdated": "2024-12-25T00:02:35.000Z",
    "passwordChanged": null,
    "type": {
        "id": "otymvgfaniH2DFk3i697"
    },
    "profile": {
        "firstName": "Test",
        "lastName": "Subject.0",
        "mobilePhone": null,
        "secondEmail": null,
        "login": "test.subject.0@grouper.com",
        "email": "test.subject.0@grouper.com"
    },
    "credentials": {
        "provider": {
            "type": "OKTA",
            "name": "OKTA"
        }
    },
    "_links": {
        "suspend": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/suspend",
            "method": "POST"
        },
        "schema": {
            "href": "https://trial-8031936.okta.com/api/v1/meta/schemas/user/oscmvgfaniH2DFk3i697"
        },
        "resetPassword": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/reset_password",
            "method": "POST"
        },
        "reactivate": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/reactivate",
            "method": "POST"
        },
        "self": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697"
        },
        "resetFactors": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/reset_factors",
            "method": "POST"
        },
        "type": {
            "href": "https://trial-8031936.okta.com/api/v1/meta/types/user/otymvgfaniH2DFk3i697"
        },
        "deactivate": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/deactivate",
            "method": "POST"
        }
    }
}
```

## Update user

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/updateUser](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/updateUser)

Response code: 200

Can update the firstName, lastName, and email

```
POST https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697 
Accept: application/json
Content-Type: application/json

{
    "profile": {
        "firstName": "Test0",
        "email": "test.subject_0@grouper.com"
    }
}
```

Response: 200

```
{
    "id": "00umxoh7cgDm3zD9v697",
    "status": "PROVISIONED",
    "created": "2024-12-25T00:02:35.000Z",
    "activated": "2024-12-25T00:02:35.000Z",
    "statusChanged": "2024-12-25T00:02:35.000Z",
    "lastLogin": null,
    "lastUpdated": "2024-12-25T00:16:28.000Z",
    "passwordChanged": null,
    "type": {
        "id": "otymvgfaniH2DFk3i697"
    },
    "profile": {
        "firstName": "Test0",
        "lastName": "Subject.0",
        "mobilePhone": null,
        "secondEmail": null,
        "login": "test.subject.0@grouper.com",
        "email": "test.subject_0@grouper.com"
    },
    "credentials": {
        "provider": {
            "type": "OKTA",
            "name": "OKTA"
        }
    },
    "_links": {
        "suspend": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/suspend",
            "method": "POST"
        },
        "schema": {
            "href": "https://trial-8031936.okta.com/api/v1/meta/schemas/user/oscmvgfaniH2DFk3i697"
        },
        "resetPassword": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/reset_password",
            "method": "POST"
        },
        "reactivate": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/reactivate",
            "method": "POST"
        },
        "self": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697"
        },
        "resetFactors": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/reset_factors",
            "method": "POST"
        },
        "type": {
            "href": "https://trial-8031936.okta.com/api/v1/meta/types/user/otymvgfaniH2DFk3i697"
        },
        "deactivate": {
            "href": "https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697/lifecycle/deactivate",
            "method": "POST"
        }
    }
}
```

## Get users of a group

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/listGroupUsers](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/listGroupUsers)

```
GET https://trial-8031936.okta.com/api/v1/groups/00gmvgcs9mpZKSfAX697/users

Response code: 200

[
    {
        "id": "00umvgfnligMmTnZJ697",
        "status": "PROVISIONED",
        "created": "2024-12-22T17:35:42.000Z",
        "activated": "2024-12-22T17:35:42.000Z",
        "statusChanged": "2024-12-22T17:35:42.000Z",
        "lastLogin": null,
        "lastUpdated": "2024-12-22T17:35:42.000Z",
        "passwordChanged": null,
        "type": {
            "id": "otymvgfaniH2DFk3i697"
        },
        "profile": {
            "firstName": "Chris",
            "lastName": "Hyzer",
            "mobilePhone": null,
            "secondEmail": null,
            "login": "chris.hyzer@gmail.com",
            "email": "chris.hyzer@gmail.com"
        },
        "credentials": {
            "provider": {
                "type": "OKTA",
                "name": "OKTA"
            }
        },
        "_links": {
            "self": {
                "href": "https://trial-8031936.okta.com/api/v1/users/00umvgfnligMmTnZJ697"
            }
        }
    }
]
```

## Add user to group

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/assignUserToGroup](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/assignUserToGroup)

Can add one user at a time

```
PUT https://trial-8031936.okta.com/api/v1/groups/00gmvgcs9mpZKSfAX697/users/00umxoh7cgDm3zD9v697

Response: 204  
```

## Remove user from group

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/unassignUserFromGroup](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/Group/#tag/Group/operation/unassignUserFromGroup)

Can remove one user at a time

```
DELETE https://trial-8031936.okta.com/api/v1/groups/00gmvgcs9mpZKSfAX697/users/00umxoh7cgDm3zD9v697

Response: 204 
```

## Delete user

[https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/deleteUser](https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/deleteUser)

```
DELETE https://trial-8031936.okta.com/api/v1/users/00umxoh7cgDm3zD9v697 

Response code: 205
```
