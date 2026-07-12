---
title: "Grouper Loader LDAP simple lookup by filter (no cache)"
space: Grouper
pageId: 28554189
version: 2
lastUpdated: 2026-07-01T05:40:57.329Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554189/Grouper+Loader+LDAP+simple+lookup+by+filter+no+cache
---

Use this if:

1. If you need to dereference members of a simple LDAP loader group
2. You have a Group hasMember that has User lookup values by filter
3. The users don't have memberOf
4. The attribute that looks up user cannot be converted by text manipulation to subjectId or subjectIdentifier
5. There are not that many lookups per day (e.g. less than a couple thousand)

Group in LDAP

User in LDAP

Ignore the fact that the RDN of the user DN has the subject ID in it in this example. If that is the case for you, you can [simply unpack that](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555319/Grouper+Loader+LDAP+dereference+example+convert+DN+to+RDN+value)

Ignore the fact that the uniqueMember is a DN. If that is the case you can [search by DN](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555106/Grouper+Loader+LDAP+simple+lookup+by+DN+no+cache)

The loader job will:

1. Filter the group
2. Return the attribute of the users
3. In this case, massage the attribute value to the RDN value, but you might not need this (using loaderLdapElUtils.convertDnToSpecificValue())
4. Run filters based on that attribute
5. Get the uid attribute of the user
6. Use that as the subjectId

The important part of this config is the subject expression:

```
${ldapLookup.assignLdapConfigId('personLdap').assignTerm(loaderLdapElUtils.convertDnToSpecificValue(subjectId)).assignSearchDn('ou=People,dc=example,dc=edu').assignSearchScope('SUBTREE_SCOPE').assignFilter('(uid=%TERM%)').assignAttributeNameResult('uid').doLookup()}
```

Each time you run the job it will LDAP filter each subject
