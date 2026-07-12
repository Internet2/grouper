---
title: "UW-Madison Stem and Group Naming Standards"
space: Grouper
pageId: 28544556
version: 11
lastUpdated: 2026-07-01T05:48:25.093Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544556/UW-Madison+Stem+and+Group+Naming+Standards
---

Namespaces and Delegation

For background on the issues, see the information from U Washington [here](https://wiki.cac.washington.edu/display/infra/UW+Affiliation+Groups) and [here](https://wiki.cac.washington.edu/display/infra/UW+Group+Naming+Plan)

*These rules were adopted in a team meeting on December 20, 2011. Any changes from here on out will need to follow formal change control process.*

###### Namespace rules:

1. Valid names: any reasonably short string consisting of a sequence of characters chosen from the following classes:
  
  - lower- and upper-case letters a-z, A-Z
  - numerals 0-9,
  - "-" (hyphen)
  - "."
2. Name component separator character: colon, ":"
  
  - Recommended because it makes for more easily readable stem and group names and it is less likely than colon ":" to cause problems in scripts and modules.
3. Root stems: one of uw:* or u:* per specific rules below
4. Institutional (data-derived) groups: `uw:ref:*`
  
  - Note: Institutional groups are never used directly for authorization purposes, but always by inclusion in an appropriate authorization-related group
5. eduPersonAffiliations: `uw:ref:institutional_roles:`{faculty, staff, student, employee, member, alum, affiliate}
  
  - Note: Recommend use of eduPersonEntitlement: common-lib-terms in preference to eduPersonAffiliation: library-walk-in
6. Affiliations: `uw:affiliation:*`
7. (not implemented, yet) Courses (strictly speaking, course offerings): `uw:course:*`
8. Each *.wisc.edu or UW-related domain is welcome to a subdomain-tagged stem name: e.g. `uw:domain:doit.wisc.edu`, `uw:domain:middleware.doit.wisc.edu`, `uw:domain:chtc.cs.wisc.edu`, `discovery.wisc.edu`, `morgridge.org`, `supportuw.org`
9. Departments, Institutes, Centers and Divisions not covered by or overlapping with subdomain assignments: `uw:org:*`
10. (not implemented) NetID-based / personal groups u:netid:*
11. Miscellaneous or "stemless" groups: x:*
12. Mandated exceptions: Any valid name value not equal to any other predefined or pre-existing name
13. Group names are made globally unique by adding the following URN prefix: urn:mace:wisc.edu:group:
