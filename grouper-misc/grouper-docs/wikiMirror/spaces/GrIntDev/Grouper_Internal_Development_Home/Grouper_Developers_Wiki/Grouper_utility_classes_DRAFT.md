---
title: "Grouper utility classes (DRAFT)"
space: GrIntDev
pageId: 48792928
version: 4
lastUpdated: 2026-07-12T06:45:54.887Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792928/Grouper+utility+classes+DRAFT
---

In a 2.6.* release we will standardize Grouper utility classes

- Example classes: GrouperUtil, SubjectUtils, GrouperUtilElSafe, etc
- Make sure this assumption is valid: only Grouper admins are writing JEXLs and scripts which use utility classes
- Consolidate logic so that
  
  - Any utility method which can live in GrouperClientUtils has logic moved there
  - Any utility method which cannot live in GrouperClientUtils (due to class dependencies) has logic moved to GrouperUtil
  - No method signatures change (so compiled code still works). i.e. if a method exists, deprecate and delegate to GrouperUtil or GrouperClientUtils
  - Deprecate methods not in GrouperClientUtils and GrouperUtil. Deprecate utility classes not GrouperClientUtils and GrouperUtil
  - GrouperUtil will inherit from GrouperClientUtils
  - "grouperUtil" will be in any Grouper (non-client) jexl as GrouperUtil and "grouperClientUtils" will be in any client jexl by default
  - All GrouperClientUtils methods will have wrapper methods in GrouperUtil so there is one easy Javadoc to refer to
- The first part of a util method name should organize the namespace, e.g. "batch" in "batchNumberOfBatches()" is for batching methods, "ldap" in "ldapEscapeRdn()" is for ldap methods, etc
