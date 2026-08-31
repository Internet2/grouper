---
title: "Grouper web service - subject source - local entities"
space: Grouper
pageId: 28555148
version: 6
lastUpdated: 2026-07-12T15:27:10.747Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555148/Grouper+web+service+-+subject+source+-+local+entities
---

You can link up an account with a local entity. Here is an example from the demo server

Add a user via [apache password store](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555359/Grouper+web+services+-+authentication+-+Apache+http+password) ( or any other [Authentication method](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548787/Grouper+Web+Services+Authentication) ) . In this example we will create an account named "test_local_entity" in the authentication source.

```
[mchyzer@i2midev1 ~]$ sudo htpasswd /etc/httpd/conf.d/users.pass test_local_entity
New password: 
Re-type new password: 
Adding password for user test_local_entity
```

You also need to expose this account to grouper via a Subject API.  
Fortunately Grouper has a built in Subject API to expose local entities created in Grouper. You can use that feature if you don't have another Subject API that would return the account name via a Subject Identifier lookup.  
Here are a few screen shots of creating a local entity in a "special folder" (path to the folder is used in the configuration later) created to hold local entities for use as Grouper Web Service accounts.

Note the 'Local entity name': needs to match the name of the account.

Add it to the WS group.

Note: It is best practice to never add a Subject directly to a policy group. ( The webServiceUsers group is a policy group. )

- The reference group can be the Subject that is privileged to other objects Grouper.
- This allows for two or more web service accounts who need to be granted the same access a trivial change. ( Add them to the same set of reference group(s) )

Configure a prefix on logins on WS: in grouper-ws.properties to only select local entities from the folder where you store local entities for use with Grouper Web Services.  
NOTE: Don't forget to KEEP the trailing ":" seperator. This string is used as a prefix string that is concatenated to any "username" that is returned from any authentication source.

```
# prepend to the userid this value (e.g. if using local entities, might be:    etc:servicePrincipals:   )
ws.security.prependToUserIdForSubjectLookup = etc:servicePrincipals:
```

Hit a link: login as test_local_entity and whatever_pass

[https://grouperdemo.internet2.edu/grouper-ws_v2_3/servicesRest/json/v2_3_000/groups/test%3AtestGroup/members](https://grouperdemo.internet2.edu/grouper-ws_v2_3/servicesRest/json/v2_3_000/groups/test%3AtestGroup/members)
