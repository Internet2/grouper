---
title: "Grouper Loader LDAP dereference example convert DN to RDN value"
space: Grouper
pageId: 28555319
version: 7
lastUpdated: 2026-07-01T05:38:32.571Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555319/Grouper+Loader+LDAP+dereference+example+convert+DN+to+RDN+value
---

A common case is the group member attribute is a DN and the first RDN value can be used as subject ID or identifier. In this case its the subject ID but it could be an identifier instead.

In this case the "uid" is the subjectId. So this is the loader configuration. The key is the Subject expression. Note: in v2.5.42+ this will escape special characters like commas correctly.

```
${loaderLdapElUtils.convertDnToSpecificValue(subjectId)}
```
