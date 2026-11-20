---
title: "Grouper ABAC script syntax"
space: Grouper
pageId: 397738094
version: 1
lastUpdated: 2026-11-20T20:28:25.191Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/397738094/Grouper+ABAC+script+syntax
---

Syntax for the scripts of [Grouper ABAC with scripted groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups).

A scripted group runs a JEXL script (not just an expression), so it can have multiple lines and comments. Wrap the script in `${ }`. The variable `entity` is an instance of `edu.internet2.middleware.grouper.abac.GrouperAbacEntity`. Refer to groups by system name (ID path), e.g. `entity.memberOf('ref:staff')`.

## Entity attributes

| **Concept** | **Example** | **Description** |
| --- | --- | --- |
| member of | 'ref:mfaEnrolled'  entity.memberOf('ref:mfaEnrolled') | Users that are members of this group (by system name / ID path) |
| member of any group | entity.memberOfAny(['ref:staff', 'ref:faculty']) | Users that are members of any of the specified groups (by system name / ID path) |
| recent member of | entity.recentMemberOf('ref:staff', '30 days')  entity.recentMemberOf('ref:staff', '1 hour') | Users that were recently (but not currently) members of this group (by system name / ID path) |
| has attribute | org  'org'  entity.hasAttribute(org)  entity.hasAttribute('org') | User has this attribute assigned or true for boolean attribute or has the attribute with any value for other types |
| has attribute string | org==abc  org=='01234'  'org'=='012#$%45'  entity.hasAttribute(org, abc)  entity.hasAttribute(org, 'abc')  entity.hasAttribute('org', 'abc') | Users that have this string attribute assigned to them |
| does not have attribute string | org != 'abc' | Users that do not have this attribute with this string. Could also write as !(org == 'abc') |
| has attribute integer | org==123  'org'==123  entity.hasAttribute(org, 123) | Users that have this integer attribute assigned to them |
| does not have attribute integer | org != 123 | Users who do not have this integer attribute assigned to them |
| comparison operator | entity.hasAttributeLessThan(org, 55)  entity.hasAttributeLessThanOrEqual(org, 55)  entity.hasAttributeGreaterThan(org, 55)  entity.hasAttributeGreaterThanOrEqual(org, 5) | Users have a value compared to the scalar value. In the first example, org must be less than 55. In Grouper v5.17.2+. |
| has any attribute string in list | jobCode =~ [abc, def]  jobCode =~ ['abc', 'def']  entity.hasAttributeAny('jobCode', ['abc', 'def']) | Users that have any of these values for this attribute |
| has any attribute integer in list | jobCode =~ [123, 234]  entity.hasAttributeAny('jobNumber', [123, -234]) | Users that have any of these values for this attribute |
| has attribute value like | entity.hasAttributeLike(org, '%\\_2%') | Users that have an attribute value like the SQL likeString. Note: "like" expressions are more efficient than regex   % (percent) matches any zero or more any characters   _ (underscore) matches exactly one any character   \\ (double backslash) escapes the next percent, underscore, or backslash   \\\\ (quadruple backslash) literal backslash   \' (backslash single quote) literal single quote in single quoted string   \" (backslash double quote) literal double quote in double quoted string |
| has attribute with value matching regex | org =~ '^.*2.*$'  entity.hasAttributeRegex(org, '^.*2.*$') | Users that have an attribute value that matches the regex. [Recommended regex site](https://regex101.com/) to build and test a regex. Escape quotes and slashes in jexl with backslash. Less efficient than SQL like string. |
| time from now | entity.hasAttributeLessThan('accessTokenExpiresAt', timeFromNow('now'))  entity.hasAttributeGreaterThan('lastLoginAt', timeFromNow('-30 days')) | Resolves to a millisecond-since-epoch value offset from the current time at script-analysis time. Argument is a single string: 'now' for the current instant, or a signed integer + unit (e.g. '30 days', '-5 minutes', '-1 year'). Negative for past, positive for future. Units accepted (singular or plural, case-insensitive): minutes, hours, days, weeks, months, years. Use anywhere a numeric / timestamp value is expected. In the last example: users who logged in within the last 30 days. |

## Entity rows

| **Concept** | **Example** | **Description** |
| --- | --- | --- |
| has row with attribute assignment | entity.hasRow('affiliation', 'active') | Users that have an affiliation row with an attribute assigned or true for boolean, or any value for other types |
| has row with attribute value | entity.hasRow('affiliation', 'affiliationCode==staff') | Users with a row of affiliation with a column value of attributeCode or value staff. "staff" is a string that does not start with an integer or have special characters in it. |
| has row with attribute value | entity.hasRow('affiliation', "affiliationCode=='01234' ") | Users with a row of affiliation with a column value of attributeCode or value 01234. "01234" has quotes around it since it has special chars or starts with an integer |
| does not have row with attribute value | entity.hasRow('affiliation',    "affiliationCode != '01234' ") | Users who do not have a row of affiliation with a column value of attributeCode or value 01234. "01234" has quotes around it since it has special chars or starts with an integer |
| has row with attribute with integer value | entity.hasRow('affiliation', "affiliationCode == 1234 ") | Users with a row of affiliation with a column value of attributeCode or value 1234. In this case the affiliationCode is an integer type attribute. |
| does not have row with attribute with integer value | entity.hasRow('affiliation', "affiliationCode != 1234 ") | Users who do not have a row of affiliation with a column value of attributeCode or value 1234. In this case the affiliationCode is an integer type attribute. |
| has row with attribute value using comparison operator | entity.hasRow('affiliation', "affiliationCode > 1234 ") | Users with a row of affiliation with a column value of attributeCode greater than value 1234. Can use <, <=, >, >=. In Grouper v5.17.2+. |
| has row with attribute string in list | entity.hasRow('affiliation', 'affiliationCode =~ [staff, fac, alum]') | Users with row of affiliation and has column affiliation code in staff, fac, alum |
| has row with attribute string not in list | entity.hasRow('affiliation', 'affiliationCode !~ [staff, fac, alum]') | Users with row of affiliation that does not have a column affiliation code in staff, fac, alum |
| has row with attribute value SQL like string | entity.hasRow('affiliation', "hasAttributeLike(affiliationCode, '%f%') ") | Users with row affiliation where a value for column affiliationCode has an f in it. This is more efficient than regex. |
| has row with attribute value that matches a regex | entity.hasRow('affiliation', "hasAttributeRegex(affiliationCode, '^.*f.*$' )" ) | Users with row affiliation where a value for column affiliationCode has an f in it. Users that have an attribute value that matches the regex. [Recommended regex site](https://regex101.com/) to build and test a regex. Escape quotes and slashes in jexl with backslash. Less efficient than SQL like string. |
| compare two columns | entity.hasRow('affiliation', 'attributeCompare(affiliationDeptNumber < affiliationDeptNumberPrimary)') | Users with row affiliation where column affiliationDeptNumber is less than column affiliationDeptNumberPrimary. Operator can be < > == <= >= !=   In Grouper v6.1.2+, v7.1.0+ |
| compare two columns with addition or subtraction | entity.hasRow('affiliation', 'attributeCompare(affiliationDeptNumber + 5 < affiliationDeptNumberPrimary)') | Users with row affiliation where column affiliationDeptNumber (plus 5) is less than column affiliationDeptNumberPrimary. Operator can be < > == <= >= !=   Note: the math can be on the left or right or both of the operator. The scalar must be to the right of the column data field (i.e. 5 + affiliationDeptNumber is not allowed. Multiple math operations or parens or other things are not allowed.   In Grouper v6.1.2+, v7.1.0+ |
| range of strings or ints | entity.hasRow('affiliation', "   hasAttributeBetween(    '0200' <= affiliationDeptNumber,     affiliationDeptNumber < '0248'   )") | Users with row affiliation where the affiliationDeptNumber between two numbers or strings, inclusive or exclusive. The same column must be used in both arguments, and the order of the scalars and the column must be as shown. Can only use < or <= to denote exclusive and inclusive.   In Grouper v6.1.2+, v7.1.0+ |
| time from now | entity.hasRow('enrollment', "startDate <= timeFromNow('now') && endDate >= timeFromNow('now')") | Users with an enrollment row where the start date is on or before now, and the end date is on or after now, i.e. an enrollment that is currently in effect. timeFromNow can be used inside hasRow for row column comparisons (<, <=, >, >=, ==, !=) and inside hasAttributeBetween. |

## Member attributes

| **Concept** | **Example** | **Description** |
| --- | --- | --- |
| subject source id | member.subjectSourceId == 'jdbc' | Members from a particular subject source. |

## Boolean logic

How Grouper composite terminology translates to ABAC:

| **Grouper composite** | **ABAC** | **Meaning** | **Notes** |
| --- | --- | --- | --- |
| ref:org1 union ref:org2 | "ref:org1" or "ref:org2" | Members who are in either group or both: ref:org1, ref:org2   e.g. you want both populations in the resulting population | Grouper composites have no union. Instead, add both groups as members of one group. |
| app:admins_manual intersect ref:employee | "app:admins_manual" and "ref:employee" | Members who are in both groups (but not in either or neither): app:admins_manual, ref:employee   e.g. you want to make sure members in the first population (a manual group perhaps) are also in the second group (an eligibility group like: employees) |  |
| app:users_allow minus ref:locked_out | "app:users_allow" and !"ref:locked_out" | Members who are in the first group app:users_allow but not in the second group ref:locked_out |  |
| app:admins_manual_employee is app:admins_manual intersect ref:employee  app:admins is app:admins_manual_employee intersect ref:mfa | "app:admins_manual" and "ref:employee" and "ref:mfa" | Members who are in the manual admins group, who are also employees, and also enrolled in mfa | A three part intersection requires an intermediate group in Grouper. In ABAC it can be one script. |
| app:wiki_users is app:wiki_users_allow_emp minus app:wiki_users_deny  app:wiki_users_allow_emp is app:wiki_users_allow intersect ref:employee | ("app:wiki_users_allow" and "ref:employee") and !"app:wiki_users_deny" | Members who are not in the deny list. Members must be employees. Members are in the allow list. | A complex user policy requires intermediate groups using Grouper composites. In ABAC it is one script. Use parentheses. |

## Script examples

| **Expression** | **Description** |
| --- | --- |
| ``` ${ entity.memberOf('ref:staff') && entity.memberOf('ref:payroll:fullTime') && entity.memberOf('ref:mfaEnrolled') } ``` | Three part intersection.  Full time staff in MFA |
| ``` ${ ( entity.memberOf('ref:employee')  \|\| entity.memberOf('ref:student')  // employees or students   \|\| (entity.memberOf('ref:guests')      && entity.memberOf('app:vpn:vpnManualOverrides'))) // or guests who are in manual allow   && !entity.memberOf('ref:globalLockout')   && !entity.memberOf('app:vpn:vpnManualLockout') }  // and not in either lockout group ``` | Example policy  That means users who are not in globalLockout and not in vpnManualLockout    and in an eligible population which is faculty, students, or guests who are in the manual app override group |

## Global variables

In Grouper v7.3.0+, there is a first pass at being able to use single-valued global variables in ABAC scripts. This is done by having a data provider that assigns data field values to the group etc:abacGlobal (the 'etc' folder may be different based on your configuration). In other words, your data provider should treat etc:abacGlobal as a subject and assign data fields to it that would represent global variables. Once assigned, those variables may be used in the following ways by having the function globalAttributeValue in your script.

```
entity.hasAttribute('dataField', globalAttributeValue('globalDataField'))
entity.hasAttributeGreaterThan('dataField', globalAttributeValue('globalDataField'))
entity.hasAttributeGreaterThanOrEqual('dataField', globalAttributeValue('globalDataField'))
entity.hasAttributeLessThan('dataField', globalAttributeValue('globalDataField'))
entity.hasAttributeLessThanOrEqual('dataField', globalAttributeValue('globalDataField'))
entity.hasAttributeBetween(globalAttributeValue('globalDataField') <= dataField, dataField <= 500)
entity.hasAttributeLike('dataField', globalAttributeValue('globalDataField'))
entity.hasAttributeRegex('dataField', globalAttributeValue('globalDataField'))
entity.hasAttribute('dataField', globalAttributeValue('globalDataField')) && entity.hasAttributeLessThan('otherDataField', globalAttributeValue('otherGlobalDataField'))
entity.hasAttribute('dataField', globalAttributeValue('globalDataField')) || entity.hasAttribute('otherDataField', globalAttributeValue('otherGlobalDataField'))
entity.hasRow('dataRow', "dataField < globalAttributeValue('globalDataField')")
```

Multi-valued global variables are also supported.

```
entity.hasAttributeAny('dataField', globalAttributeValues('globalDataField'))
entity.hasRow('dataRow', "dataField =~ globalAttributeValues('globalDataField')")
```

Global attribute values are cached for 2 minutes by default, you can configure it in grouper.properties:

```
# Number of minutes to cache the data field values assigned to the etc:abacGlobal group, which are
# referenced in ABAC scripts via globalAttributeValue('alias').  A changed global value takes effect
# within this many minutes.  Default is 2.
# {valueType: "integer", defaultValue: "2"}
grouper.abac.globalAttributeValuesCacheMinutes =

```
