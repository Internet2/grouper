---
title: "Grouper bug - GRP-5107 - authentication bypass testing"
space: Grouper
pageId: 28554556
version: 3
lastUpdated: 2026-07-01T05:40:11.173Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554556/Grouper+bug+-+GRP-5107+-+authentication+bypass+testing
---

These are the testing steps to make sure your remediation is correct for the Grouper security issue.

If you have questions, then send a direct slack message to Chris Hyzer, the project lead ([request to join InCommon slack](https://incommon.org/help/)). If you are not in InCommon slack then email [mchyzer@example.com](mailto:mchyzer@example.com).

## Vulnerability description

If the environment is affected, the web service (or less likely UI) can be authenticated for an arbitrary user or identifier (which does not have a password set) with no password. If the WS restricts which users can connect (grouper-ws.properties: ws.client.user.group.name), and all subject IDs and identifiers have a password defined, then this cannot be exploited.

The remediation ensures the WS restricts group, and adds the other subject ids or identifiers with unusable passwords in the grouper_password table.

## Test your environments

To test, make a call like this to your WS in all environments (dev/test/prod etc). Adjust the https, domain name, and context (grouper-ws) to fit your env. Adjust the “user” and the “password” per the test case. With curl, if there is no password, just end the username with a colon: -u 'user:'

```
curl -X GET -H 'Content-Type: application/json' -u 'user:password' -i 'https://domain.name/grouper-ws/servicesRest/v2_5_000/subjects?wsLiteObjectType=WsRestGetSubjectsLiteRequest&searchString=GrouperSystem'
```

This is a valid response with 200 code

```
{"WsGetSubjectsResults":{"resultMetadata":
{"success":"T","resultCode":"SUCCESS","resultMessage":"Queried 1 subjects"},"responseMetadata":{"serverVersion":"4.0.0","millis":"400"},"wsSubjects":[{"sourceId":"g:isa","success":"T","name":"GrouperSysAdmin","resultCode":"SUCCESS","id":"GrouperSystem"}]}}
```

**Note**: You do not have to use curl, you can use any rest client. You cannot use the grouper client since it requires a password.

**Note**: Valid user means a WS user which is in the group specified by: [ws.client.user.group.name](http://ws.client.user.group.name), and a password in grouper_password table that doesn’t start with xXxXx in the_password column

**Note**: Invalid user means a valid subject but which is not in the group specified by: [ws.client.user.group.name](http://ws.client.user.group.name)

**Note**: When <none> is specified for password, leave it blank

**Note**: When “Valid user different subject identifier or id” is specified, find a subject id or identifier for the user which is different than the one authenticating. E.g. if you use a local entity UUID to authenticate, use the id path (or system name) instead. if you use a local entity name to authenticate, use the UUID instead. If you have a different subject source than the Grouper group subject source, then use a different subject id or identifier for the same subject. **Note**: the there is no subject identifier for GrouperSystem, there is only the subject ID “GrouperSystem”.

**Note**: Error generally means it will return a 401 status code, but any 4XX or 5XX status code is fine.

| **Username** | **Password** | **Expected result** |
| --- | --- | --- |
| Valid user | Valid password | Valid |
| Valid user | <none> | Error |
| Valid user different subject identifier or id | <none> | Error |
| GrouperSystem | <none> | Error |
| Invalid user | <none> | Error |

## Clear your shell history

If you were using curl via commands line, edit or delete the history file (e.g. ~/.bash_history) and log out and log in and type: history and ensure any passwords are not stored in your OS.

## Final data check

Check the usernames in the grouper_password table to ensure that each real password (the_password doesn’t start with xXxXx), has entries for other subject IDs or identifiers (the_password does start with xXxXx which makes the password not work). For instance if you are using local entities with UUIDs, make sure the group names are listed in the grouper_password table with password that starts with xXxXx. If you are using your own subject source, see what subject identifiers or ids that source allows.
