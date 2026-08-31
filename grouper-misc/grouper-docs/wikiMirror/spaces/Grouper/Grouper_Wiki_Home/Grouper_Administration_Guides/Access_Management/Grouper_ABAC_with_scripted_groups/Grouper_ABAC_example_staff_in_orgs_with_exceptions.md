---
title: "Grouper ABAC example staff in orgs with exceptions"
space: Grouper
pageId: 28548501
version: 5
lastUpdated: 2026-07-01T05:44:21.406Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548501/Grouper+ABAC+example+staff+in+orgs+with+exceptions
---

## Use case

```
People who are staff in org AB or CD
Manual allow: ann
Manual deny: greg
```

## Solution with composites

This solution has three extra intermediate groups. It is also not a correct solution. Just because someone is staff, and in orgs AB or BC, does NOT mean they are staff in those orgs. Some could be staff in other orgs and guests in AB or BC. If everyone in an org is staff then you do not need the composite intersection or staff and orgs.

## ABAC scripted group with groups

```
( 'app:litellm_ag:service:policy:service_litellm_rw_allow_manual'
  or
  ( ( 'basis:orgs:org_AB' or 'basis:orgs:org_BC' ) and 'ref:affiliations:affiliation_staff' )
)
and !'app:litellm_ag:service:policy:service_litellm_rw_deny_manual'
```

Note in 7.2.0+ scripted group can be visualized

This solution only has three groups for the app: the policy group, the manual allow, and the manual deny. It does not require the three intermediate group from the first solution. In addition, the policy can be easily changed at any time without having to clear the members out (when you remove a composite). However, the solution is still NOT correct since it does not ensure that people are staff in an org, similar to the first solution. They could be staff in another org and guests in this org.

## ABAC scripted group with data fields and rows

The affiliation rows are modeled as data fields and rows from a table

The ABAC script is now

```
( 'app:litellm_ag:service:policy:service_litellm_rw_allow_manual'
  or
  entity.hasRow('affiliation', "( affiliation_org == 'AB' or affiliation_org == 'BC' )
                                 and affiliation_name == 'staff' ")
)
and !'app:litellm_ag:service:policy:service_litellm_rw_deny_manual'
```

Notice the policy group has fewer (and the correct) number of members. This visualizes as: (visualization for ABAC new in v7.2.0+)
