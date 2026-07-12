---
title: "Grouper Loader LDAP simple lookup by DN (no cache)"
space: Grouper
pageId: 28555106
version: 4
lastUpdated: 2026-07-01T05:38:55.938Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555106/Grouper+Loader+LDAP+simple+lookup+by+DN+no+cache
---

Use this if:

1. If you need to dereference members of a simple LDAP loader group
2. You have a Group hasMember that has User DN values
3. The users don't have hasMember
4. The RDN in the user DN is not a subject ID or identifier
5. There are not that many lookups per day (e.g. less than a couple thousand)

Group in LDAP

User in LDAP

Ignore the fact that the RDN of the user DN has the subject ID in it in this example. If that is the case for you, you can [simply unpack that](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555319/Grouper+Loader+LDAP+dereference+example+convert+DN+to+RDN+value)

The loader job will:

1. Filter the group
2. Return the DN's of the users
3. Lookup those DN's
4. Get the uid attribute
5. Use that as the subjectId

The important part of this config is the subject expression:

```
${ldapLookup.assignLdapConfigId('personLdap').assignAttributeNameResult('uid').assignSearchDn('%TERM%').assignTerm(subjectId).doLookup()}
```

Each time you run the job it will LDAP lookup each subject
