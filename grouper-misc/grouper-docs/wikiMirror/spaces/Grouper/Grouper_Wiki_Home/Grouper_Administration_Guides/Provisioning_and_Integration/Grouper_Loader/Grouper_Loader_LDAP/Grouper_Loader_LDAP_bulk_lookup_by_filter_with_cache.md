---
title: "Grouper Loader LDAP bulk lookup by filter (with cache)"
space: Grouper
pageId: 28554470
version: 3
lastUpdated: 2026-07-01T05:40:22.812Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554470/Grouper+Loader+LDAP+bulk+lookup+by+filter+with+cache
---

Use this if:

1. You are doing a simple or list LDAP loader (any type)
2. If you need to dereference LDAP objects
3. The attribute that looks up user cannot be converted by text manipulation to subjectId or subjectIdentifier
4. There are any number of lookups per day

Group in LDAP

User in LDAP

Ignore the fact that the RDN of the user DN has the subject ID in it in this example. If that is the case for you, you can [simply unpack that](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555319/Grouper+Loader+LDAP+dereference+example+convert+DN+to+RDN+value)

The list loader job will:

1. Filter the groups
2. Return the attribute of the users
3. If the filter has not run recently (in the last day in this case), run the bulk filter, and cache the results
4. Note the dn is the query attribute (though its not really an attribute, thats ok). Its the query attribute since that is what is looked up in the cache
5. The uid is the result attribute of the filter
6. Check the memory cache to lookup the value (do not run a filter if not found)
7. Get the uid attribute of the user from cache
8. Use that as the subjectId
9. Store the groups in the "test:ldap" folder

The important part of this config is the subject expression:

```
 ${ldapLookup.assignLdapConfigId('personLdap').assignAttributeNameQuery('dn').assignTerm(subjectId).assignSearchDn('ou=People,dc=example,dc=edu').assignSearchScope('SUBTREE_SCOPE').assignFilter('(uid=*)').assignAttributeNameResult('uid').assignBulkLookup(true).assignCacheForMinutes(24*60).doLookup()}
```

Each time you run the job it will LDAP filter if it has not done so recently (TTL set in cache timeout)
