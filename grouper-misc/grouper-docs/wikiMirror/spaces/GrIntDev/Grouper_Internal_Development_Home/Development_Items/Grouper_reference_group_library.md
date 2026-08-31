---
title: "Grouper reference group library"
space: GrIntDev
pageId: 48792642
version: 6
lastUpdated: 2026-07-12T06:45:36.343Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792642/Grouper+reference+group+library
---

In order to make reference groups more useful, there is a request to add a page in the Grouper UI to list the important reference groups and documentation about them.

## Configure

Add some information (on the "type) about the important reference groups

| Label | Value | Description |
| --- | --- | --- |
| addToLibrary | true | If this reference group should be added to the library |
| showForGroupName | a:b:c | This group can see this in their library. Otherwise everyone sees all groups. |
| showNameIfNotAllowedToView | true | If should list this for everyone allowed to see in their library, with a link for how to request access. If false, then only show the group if they user can VIEW the group |
| friendlyLabel | Active employee | If this is filled out it will display on the library page, otherwise use the group display extension |
| groupDescription | This group consists of active employees based on payroll records. Does not include contractors. | If this is filled out it will display on the library page, otherwise use the group description |
| order | 12.4 | Groups will be ordered by this number |
| groupLabel | Active employee | Organize groups by this label |

## Example reference group library page in UI

### Reference group library

Reference groups are institutionally meaningful and can be used to construct policies, automatically deprovision manual groups, or for other reasons. If you are not sure which group to use, please contact [support](mailto:a@b.c)

| ### **Reference category: General** |
| --- |
| Reference group | Group name | Description | Number of members | Uses |
| Member | ref:activePersonWithNetIdNotIncludingAlum | People actively associated with the university, includes affiliations: abc, staf, hosp, def, etc.  This can be used for very coarse grained deprovisioning when manual groups can include    people loosely affiliated with the University | 123456 | 51 |
| Affiliate | ref:affiliate | You are not allowed to view this group, [request access](http://request access) |  |  |
| ### **Reference category: Employee** |
| **Reference group** | **Group name** | **Description** | **Number of members** | **Uses** |
| Active employee | ref:employee | Active employees based on payroll records. Does not include contractors. | 12345 | 8 |
| Employee including contractors |  | You are not allowed to view this group, [request access](http://request access) |  |  |
| All employees | ref:allEmployees | All people working at the institution in any capacity. Includes health system.  Note: this group includes student workers  This can be used to restrict access to someone who works at the institution in some capacity. | 23456 | 4 |

## Notes

Have a way to add folders (e.g. for basis groups)
