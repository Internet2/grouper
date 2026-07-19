---
title: "Grouper Slate provisioner developer notes"
space: GrIntDev
pageId: 48792578
version: 3
lastUpdated: 2026-07-12T17:46:14.724Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792578/Grouper+Slate+provisioner+developer+notes
---

## Log in to UI

- (Log in) [https://adminconsole.adobe.com/](https://adminconsole.adobe.com/)
- (Docs) [https://adobe-apiplatform.github.io/umapi-documentation/en/](https://adobe-apiplatform.github.io/umapi-documentation/en/)

## External system

External system type: WS

Authentication type: (new) OAUTH client credentials

Token URL: [https://ims-na1.adobelogin.com/ims/token/v2](https://ims-na1.adobelogin.com/ims/token/v2)

Service URL: [https://usermanagement.adobe.io/v2/usermanagement](https://usermanagement.adobe.io/v2/usermanagement)

Client id: sdf

Client secret: sdf

Grant type: (for Adobe, needs to be client_credentials)

Scopes: (for Adobe, needs to be openid,AdobeID,user_management_sdk)

API key header name: (for Adobe: x-api-key)

API key password (encrypted by default): sdf

URL suffix: /groups/ABC101@AdobeOrg/0

Test by:

POST to Token URL with request params specified above:

[https://ims-na1.adobelogin.com/ims/token/v2?grant_type=client_credentials&client_id=sfd&client_secret=sdf&scope=openid,AdobeID,user_management_sdk](https://ims-na1.adobelogin.com/ims/token/v2?grant_type=client_credentials&client_id=sfd&client_secret=sdf&scope=openid,AdobeID,user_management_sdk)

Get the access token (expires is in seconds):

```
{"access_token":"abc123","token_type":"bearer","expires_in":86399}
```

Then execute the service url and test URL suffix (if there is an Adobe API key password, put that in the API key header: e.g. x-api-key), put the access token as Authorization: Bearer ACCESS_TOKEN

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

| mock_adobe_group and group_prov_adobe_group |
| --- |
| Column | Type | Description |
| config_id | varchar(100) | (for prov table only, the provisioner config id) |
| group_id | bigint | increment this, readonly |
| group_name | varchar(2000) | unique group name |
| type | varchar(100) |  |
| product_name | varchar(2000) | product |
| member_count | bigint (nullable) | this is from the get group service, not an actual count of memberships (since includes direct and indirect) |
| license_quota | bigint (nullable) | null if not specified, -1 for UNLIMITED, or the number |

Mappable provisionable attributes: id, name

## User

Crud: select, insert, update, delete

Primary key: config_id, id

| mock_adobe_user and group_prov_adobe_user |
| --- |
| Column | Type | Description |
| config_id | varchar(100) | (for prov table only, the provisioner config id) |
| user_id | varchar(100) | uuid, readonly |
| email | varchar(256) | case insensitive |
| username | varchar(256) |  |
| status | varchar(30) | e.g. active |
| type | varchar(30) | "adobeID", "enterpriseID", "federatedID", "unknown"  default to federatedID in commands |
| firstname | varchar(100) |  |
| lastname | varchar(100) |  |
| domain | varchar(100) |  |
| country | varchar(2) | select/insert only, default to US in commands |

Mappable provisioning attributes: id, email, firstname, lastname, type, country

## Membership

Crud: select, insert, update, delete

Primary key is config_id, group_id, user_id

| mock_adobe_mship and group_prov_adobe_mship |
| --- |
| Column | Type | Description |
| config_id | varchar(100) | (for prov table only, the provisioner config id) |
| group_id | bigint | group_id |
| user_id | varchar(100) | user_id |

## Get groups

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/group.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/group.html)

Start at page 0, and increment until lastPage is true (or max page 100k)

Should probably expirable cache for 10 minutes all groups by config id in static map

```
Authorization: Bearer abc123
x-api-key: def456
GET $SERVICE_URL$/groups/$ORG_ID$/0

{
  "lastPage": true,
  "result": "success",
  "groups": [{
    "groupId": 4147407,
    "groupName": "_org_admin",
    "type": "SYSADMIN_GROUP",
    "memberCount": 23
  }, 
  {
    "groupId": 38324336,
    "groupName": "Default Spark with Premium Features for Higher-Ed - 2 GB configuration",
    "type": "PRODUCT_PROFILE",
    "productName": "Creative Cloud Shared Device Access for Higher Education (ETLA,ETLA - DD211C79AB1DB19CBD0A)",
    "licenseQuota": "UNLIMITED".  (integer or UNLIMITED), put this in Integer and -1 if UNLIMITED (null if not specified)
  }
```

## Create group

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/usergroupActionCommands.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/usergroupActionCommands.html)

```
POST $SERVICE_URL$/action/$ORG_ID$

[
  {
    "usergroup": "myTestGroup",
    "do": [
            {
              "createUserGroup": {
                "name": "myTestGroup",
                "option": "ignoreIfAlreadyExists"
              }
            }
         ]
  }
]

```

After creating a group, select them all, loop through, and get the id.

## Delete group

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/usergroupActionCommands.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/usergroupActionCommands.html)

```
POST $SERVICE_URL$/action/$ORG_ID$

[
  {
    "usergroup": "GROUP_NAME",
    "do": [
            {
              "deleteUserGroup": {
              }
            }
         ]
  }
]

200

{
  "completed": 1,
  "notCompleted": 0,
  "completedInTestMode": 0,
  "result": "success"
}
```

## Update group

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/usergroupActionCommands.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/usergroupActionCommands.html)

```
POST $SERVICE_URL$/action/$ORG_ID$

[
  {
    "usergroup": "OLD_GROUP_NAME",
    "do": [
            {
              "updateUserGroup": {
                "name": "NEW_GROUP_NAME"
              }
            }
         ]
  }
]

200

{
  "completed": 1,
  "notCompleted": 0,
  "completedInTestMode": 0,
  "result": "success"
}
```

## Get users and memberships

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/getUsersWithPage.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/getUsersWithPage.html)

Start at page 0, and increment until lastPage is true (or max page 100k)

```
######## GET USERS IN ORG

GET $SERVICE_URL$/users/$ORG_ID$/0

{
  "lastPage": false,
  "result": "success",
  "users": [{
    "id": "abc123",
    "email": "abc@example.com",
    "status": "active",
    "groups": ["Group name 1", "Group name 2"],
    "username": "ABC@example.com",
    "domain": "upenn.edu",
    "firstname": "Dave",
    "lastname": "Smith",
    "type": "federatedID",
    "country": "US"
  }, {
```

## Get user

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/getUser.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/getUser.html)

with memberships

response code: 200/404

```
GET $SERVICE_URL$/organizations/$ORG_ID$/users/email@example.com

{
  "result": "success",
  "user": {
    "id": "abc123",
    "email": "jsmith@example.com",
    "status": "active",
    "groups": ["Group1", "Group2"],
    "username": "JSMITH@example.com",
    "domain": "upenn.edu",
    "firstname": "John",
    "lastname": "SMITH",
    "country": "US",
    "type": "federatedID"
  }
}
```

## Create user

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html)

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#createEnterpriseID](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#createEnterpriseID)

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#addAdobeID](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#addAdobeID)

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#createFederatedID](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#createFederatedID)

Response code: 200

Depending on user type (default to federated), that is the label in the "do" part: addAdobeID|createFederatedID|createEnterpriseID

```
POST $SERVICE_URL$/action/$ORG_ID$
Accept: application/json
Content-Type: application/json

[
  {
    "user": "abc@example.com",
    "do": [
      {
        "addAdobeID|createFederatedID|createEnterpriseID": {
          "email": "abc@example.com",
          "country": "US",
          "firstname": "AbcTest",
          "lastname": "AbcTest"
        }
      }
    ]
  }
]

```

Response: 200

```
{"completed":1,"notCompleted":0,"completedInTestMode":0,"result":"success"}
```

Note, the ID does not come back, so need to select the user by email address after create to get that

## Update user

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html)

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#update](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#update)

Response code: 200

Can update the firstname, lastname, country, or email, and can do multiple at once

```
POST $SERVICE_URL$/action/$ORG_ID$
Accept: application/json
Content-Type: application/json

[
  {
    "user": "abc@example.com",
    "do": [
      {
        "update": {
          "lastname": "AbcTest1"
        }
      }
    ]
  }
]
```

Response: 200

```
{"completed":1,"notCompleted":0,"completedInTestMode":0,"result":"success"}
```

## Add user to groups

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html)

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#add](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#add)

Can put multiple groups in there

```
POST $SERVICE_URL$/action/$ORG_ID$
Accept: application/json
Content-Type: application/json

[
  {
    "user": "abc@example.com",
    "do": [
      {
        "add": {
          "group": [
            "HireIT ISC - CCE Pro - Acrobat Pro DC"
          ]
        }
      }
    ]
  }
]

```

Response: 200

```
{"completed":1,"notCompleted":0,"completedInTestMode":0,"result":"success"}
```

## Remove user from groups

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html)

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#remove](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#remove)

Can put multiple groups in there

```
POST $SERVICE_URL$/action/$ORG_ID$
Accept: application/json
Content-Type: application/json

[
  {
    "user": "abc@example.com",
    "do": [
      {
        "remove": {
          "group": [
            "HireIT ISC - CCE Pro - Acrobat Pro DC"
          ]
        }
      }
    ]
  }
]

```

Response: 200

```
{"completed":1,"notCompleted":0,"completedInTestMode":0,"result":"success"}
```

## Delete user

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html)

[https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#removeFromOrg](https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsCmds.html#removeFromOrg)

deleteAccount is based on provisioner config above

```
POST $SERVICE_URL$/action/$ORG_ID$
Accept: application/json
Content-Type: application/json

[
  {
    "user": "abc@example.com",
    "do": [
      {
        "removeFromOrg": {
          "deleteAccount": true/false
        }
      }
    ]
  }
]

```

Response: 200

```
{"completed":1,"notCompleted":0,"completedInTestMode":0,"result":"success"}
```
