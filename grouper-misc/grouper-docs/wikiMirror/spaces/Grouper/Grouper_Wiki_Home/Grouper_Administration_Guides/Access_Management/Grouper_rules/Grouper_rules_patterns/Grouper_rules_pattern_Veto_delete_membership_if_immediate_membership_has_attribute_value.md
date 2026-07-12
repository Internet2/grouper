---
title: "Grouper rules pattern - Veto delete membership if immediate membership has attribute value"
space: Grouper
pageId: 28554213
version: 3
lastUpdated: 2026-07-01T05:40:55.326Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554213/Grouper+rules+pattern+-+Veto+delete+membership+if+immediate+membership+has+attribute+value
---

This is an example of an EL condition on memberships which is an advanced use case. Only admins can set these up.

## Setup EL in rules

in grouper.properties

Key: **rules.accessToApiInEl.group**

Value: Some group name, e.g. **etc:rulesAccessToApiInEl**

Add GrouperSystem to that group

## Attribute definition

(note attribute is immediate memberships. if you want "any membership", the query needs to change slightly:

## Attribute name

## Rule

| Attr | Value |
| --- | --- |
| ruleActAsSubjectId | GrouperSystem |
| ruleActAsSubjectSourceId | g:isa |
| ruleCheckType | membershipRemove |
| ruleIfConditionEl | (adjust attribute name test:testAttr)   ``` ${ "1" == new("edu.internet2.middleware.grouperClient.jdbc.GcDbAccess").sql("select value_string from grouper_aval_asn_mship_v gaamv where attribute_def_name_name = 'test:testAttr' and membership_id = ?").addBindVar(membership.getImmediateMembershipId()).select(''.class.forName('java.lang.String')) }  ```  Notes    \| Concept \| Java \| Jexl \| \| --- \| --- \| --- \| \| new instance \| new GcDbAccess() \| new("edu.internet2.middleware.grouperClient.jdbc.GcDbAccess") \| \| class object \| String.class \| ''.class.forName('java.lang.String') \| | Concept | Java | Jexl | new instance | new GcDbAccess() | new("edu.internet2.middleware.grouperClient.jdbc.GcDbAccess") | class object | String.class | ''.class.forName('java.lang.String') |
| Concept | Java | Jexl |
| new instance | new GcDbAccess() | new("edu.internet2.middleware.grouperClient.jdbc.GcDbAccess") |
| class object | String.class | ''.class.forName('java.lang.String') |
| ruleThenEnum | veto |
| ruleThenEnumArg0 | mySchoolVetoIfAttributeThere (some random externalized text key you can ignore) |
| ruleThenEnumArg1 | This membership cannot be removed since attr is there with val 1 (what is shown on screen, see below for caveats) |

Note that the way rules are implemented with membership deletes, the rule is fired in the transaction after the membership (and attributes) are deleted. So if we do a query like this which will not use the same transaction (as opposed to a hibernate call which uses the same transaction), then we get the desired result. The hibernate call which does not work correctly is:

```
${ '1' == membership.getAttributeValueDelegate().retrieveValueString('test:testAttr') }
```

## When you multi-select memberships (or bulk remove via import), you get a generic error

## When you revoke a single membership from the drop down, you see the veto error
