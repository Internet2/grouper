---
title: "Grouper Duo provisioning"
space: Grouper
pageId: 28554905
version: 23
lastUpdated: 2026-09-28T02:54:20.503Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554905/Grouper+Duo+provisioning
---

## External System

- [Grouper Duo External System](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548604/Grouper+Duo+External+System)

## Movie

[Demo video of provisioning groups and memberships to Duo](https://youtu.be/WsGod8rOtgE)

This is the script to create users in the video

```
GrouperSession grouperSession = GrouperSession.startRootSession();
RegistrySubject.addOrUpdate(grouperSession, "mchyzer", "person", "Chris Hyzer", "Chris Hyzer", "mchyzer", "Chris Hyzer", "mchyzer@example.com");
RegistrySubject.addOrUpdate(grouperSession, "kwilso", "person", "Kate Wilson", "Kate Wilson", "kwilso", "Kate Wilson", "kwilso@example.com");
```

## Provisioning attributes

Advice

- Provisioning type is membershipObjects
- Use group and entity link (since there are uuids in the target for groups and entities that need to be looked up)

#### Group attributes. [API](https://duo.com/docs/adminapi#groups)

| **Grouper name** | **Type** | **Required?** | **Duo API** | **Duo UI** | **Description** |
| --- | --- | --- | --- | --- | --- |
| id | String | required | group_id | (in URL) | This is the UUID read from Duo. Select only. This should not be translated from Grouper and the target attribute should be cached. |
| name | String | required | name | Group Name | This is the name of the group on the Duo side. |
| description | String | optional | desc | Description | This is the description in Duo |

#### Entity attributes. [API](https://duo.com/docs/adminapi#users)

| **Grouper name** | **Type** | **Required?** | **Duo API** | **Duo UI** | **Description** |
| --- | --- | --- | --- | --- | --- |
| id | String | required | user_id | (in URL) | This is the UUID read from Duo. Select only. This should not be translated from Grouper and the target attribute should be cached. |
| loginId | String | required | username | Username | This is the username in Duo. Note if you have upper case letters in this, you need to set the loginId attribute: advanced → value settings → case sensitive compare: false |
| name | String | optional | realname | Full name | First and last name |
| email | String | optional | email | Email | Email address for user |
| firstname | String | optional | firstname | NA | First name of user. Note, this does not update, the "name" attribute will do the update |
| lastname | String | optional | lastname | NA | Last name of user. Note, this does not update, the "name" attribute will do the update |
| alias1 | String | optional | alias1 | Username alias 1 | Username alias 1 - It cannot be the same as username or any other username aliases |
| alias2 | String | optional | alias2 | Username alias 2 | Username alias 2 - It cannot be the same as username or any other username aliases |
| alias3 | String | optional | alias3 | Username alias 3 | Username alias 3 - It cannot be the same as username or any other username aliases |
| alias4 | String | optional | alias4 | Username alias 4 | Username alias 4 - It cannot be the same as username or any other username aliases |

## Logging

Generally you will have logging setting set to off unless you are troubleshooting something.

If you want to see the HTTP traffic going to and from duo, set one of these options

You will see container logs that look like this

```
2021-11-06 13:54:04,854: [Thread-36] INFO  GrouperProvisioningLogCommands.infoLog(25) -  - Command log for provisioner 'duoTest' - 'u5ydv5lk', retrieveAllData: HTTP method: get
HTTP URL: https://api-84f782e2.duosecurity.com/admin/v1/groups?limit=100&offset=0
HTTP request header: Authorization: *******
HTTP request header: Date: Sat, 06 Nov 2021 17:54:02 +0000
HTTP request header: Content-Type: application/x-www-form-urlencoded
HTTP response code: 200, took ms: 913
HTTP response header: Transfer-Encoding: chunked
HTTP response header: Strict-Transport-Security: max-age=31536000
HTTP response header: Server: Duo/1.0
HTTP response header: Cache-Control: no-store
HTTP response header: Etag: W/"198da276e78d748b76b7123456"
HTTP response header: Content-Security-Policy: default-src 'self'; frame-src 'self' ; img-src 'self'  ; connect-src 'self'
HTTP response header: Connection: keep-alive
HTTP response header: Pragma: no-cache
HTTP response header: Date: Sat, 06 Nov 2021 17:54:03 GMT
HTTP response header: Content-Type: application/json
{
   "metadata":{
      "total_objects":11
   },
   "response":[
      {
         "desc":"",
         "group_id":"DGUCVTMOMM3UK7YHQ7ZE",
         "mobile_otp_enabled":false,
         "name":"duoGroupFromGrouper",
         "push_enabled":false,
         "sms_enabled":false,
         "status":"Active",
         "voice_enabled":false
      },
      {
         "desc":"This is a description",
         "group_id":"DGCVKVG5GQNG0Z4ZF13G",
         "mobile_otp_enabled":false,
         "name":"duoGroupFromGrouper2",
         "push_enabled":false,
         "sms_enabled":false,
         "status":"Active",
         "voice_enabled":false
      }
   ],
   "stat":"OK"
}
```

You can load duo users into grouper database into grouper_prov_duo_user table as shown below.

grouper_prov_duo_user table is shown below

## Sync back auth methods

*Available in Grouper 7.6.0+, 6.6.0+, and 4.27.0+ (GRP-7384).*

When entity [sync back](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555407/Grouper+provisioning+sync+back) is on (`loadEntitiesToGenericGrouperTable`), the Duo provisioner captures `userName`, `email`, `status`, `lastLogin` (epoch seconds), and `isEnrolled` for each Duo user by default. `isEnrolled` is separate from `status`: an active user might not have any auth method registered yet.

Turn on *Sync back auth methods* to also capture a summary of each user's authentication methods. This is useful for finding users who can only authenticate with a method that is being retired, such as SMS or phone callback. These attributes are added to the default or configured native entity attributes; they do not replace them.

```
provisioner.myDuoProvisioner.loadEntitiesToGenericGrouperTable = true
provisioner.myDuoProvisioner.nativeAttributesEntitiesIncludeAuthMethods = true
```

| **Attribute** | **Description** |
| --- | --- |
| `pushPhoneCount` | Activated phones with Duo Mobile push |
| `mobileOtpPhoneCount` | Activated phones with Duo Mobile passcodes |
| `pushPhoneLastSeen` | Latest Duo `last_seen` across activated push phones, as Duo returns it (ISO 8601 string). Not set if there are none. |
| `smsPhoneCount` | Phones that can receive SMS passcodes |
| `voicePhoneCount` | Phones that can receive a phone callback |
| `duoHardwareTokenCount` | Duo D-100 hardware tokens (Duo token type `d1`) |
| `hotpTokenCount` | HOTP tokens (`h6`, `h8`) |
| `totpTokenCount` | TOTP tokens (`t6`, `t8`) |
| `yubikeyOtpTokenCount` | YubiKey OTP tokens (`yk`) |
| `webauthnCount` | WebAuthn credentials. Duo does not say whether each one is a platform or roaming authenticator. |
| `u2fCount` | Legacy U2F security keys |

Every count is written, including 0. Phone numbers, token serials, and credential names are not stored.

Query the values from `grouper_prov_user_attr_v` (counts are in `value_integer`, strings in `value_string`). For example, Duo users who only have SMS or phone callback:

```sql
select target_user_id,
  max(case when attribute_name = 'userName' then value_string end) as user_name,
  max(case when attribute_name = 'lastLogin' then value_integer end) as last_login
from grouper_prov_user_attr_v
where provisioner_name = 'myDuoProvisioner'
group by target_user_id
having max(case when attribute_name = 'status' then value_string end) = 'active'
  and sum(case when attribute_name in ('pushPhoneCount', 'mobileOtpPhoneCount', 'duoHardwareTokenCount',
      'webauthnCount', 'u2fCount') then value_integer else 0 end) = 0
  and sum(case when attribute_name in ('smsPhoneCount', 'voicePhoneCount') then value_integer else 0 end) > 0
```
