---
title: "Grouper ABAC with scripted groups"
space: Grouper
pageId: 28544896
version: 106
lastUpdated: 2026-07-24T12:21:10.760Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups
---

## Grouper ABAC

Grouper does a great job with group relationships and group math. [Basis groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543159) can be loaded into Grouper but that is a single relationship from a user to an attribute (group). For instance:

- You can load groups that represent every affiliation with the users who have that affiliation (e.g. faculty, staff, contractors)
- You can load groups that represent organizations with the users who are in those organizations (e.g. business school, engineering school)
- But you cannot take those groups and use group math to calculate which users have a specific affiliation in a specific organization (both are many to many relationships). (e.g. staff/contractors in the business school)
- You would need to load the cross product of the data which is not scalable as the number of attributes increases. e.g. a loader that has groups for staff in business school, contractors in nursing school, etc

ABAC allows you to model rows of data for a user, and then make an ABAC script to specify criteria in that row of data. You could instantly make a group for users who have certain affiliations in certain org in their primary job in a full time capacity. Previously you needed to make a loader job to load a group with a SQL query that can join various data elements from a data warehouse.

> **Grouper ABAC Blog**
> 
> Check out [this Grouper ABAC blog from November 2025](https://incommon.org/news/abac-to-the-future/) for info on using ABAC to reduce the burden of loader jobs.

## Use case

At U Penn over 15 years we now have 700 loader jobs. Only Grouper sysadmins can manage these loader jobs for security reasons. It takes tickets to create the job, update the job, and troubleshoot the data. This valuable staff time is greatly reduced with ABAC. These loader daemons generally do not have real time updates since that is difficult to configure for every job, so hourly full syncs are scheduled which waste resources. There is no way to do grace periods on the source data unless the source database keeps data history (which likely is not the case).

Enter Grouper ABAC, with a few data providers (identity data, student data, payroll data, training data, etc), we can replace 2/3 of our loader jobs with ABAC. The data will flow to Grouper in real time (with fewer data feeds by two orders of magnitude it is feasible to configure real time updates). Grouper keeps history on all the data so grace periods at the row level are available. Each data provider has its own security policies at at the column level (with three access levels) which can be securely delegated to eligible power users. Users can use ABAC scripts to configure, troubleshoot, and update their own groups.

A **dynamic data dictionary** informs users which attributes are available, what the values mean, and how to use them. The analysis screen shows the numbers of all the parts of the script, and if test users have each attribute or the result. This troubleshooting in SQL is very time consuming. The groups will be updated in real time and the attribute values change. A single ABAC script can use data from any data provider and even Grouper memberships (which cannot be done with loaders without supporting ETL jobs).

Loaders (to build multiple groups at once) can point to Grouper's attribute repository to have consistent data and real time updates. The reduction in loader jobs from external data sources will reduce the network query traffic and database loads. This ABAC data can be used to replace subject sources, provision rich object representations, manage users' lifecycles, and populate reports.

The ability to **manage groups by rows of data** has been requested for years and will revolutionize access management. It further differentiates Grouper from other IAM products.

## Attribute based access control (ABAC) overview

To implement access policies, it has often been necessary to set up intermediate groups, include/exclude, requirement groups, and [allow/deny manual groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547660). Grouper has features to help in this area including: [rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173), [hooks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545347), templates, move/copy, import/export, and GSH scripts.   
  
The ABAC with scripted groups feature is designed to offer increased efficiency in implementing access policies. It's important for the common groups and policy language to be well documented and people to be properly trained.

## Syntax

| **Type** | **Concept** | **Example** | **Description** |
| --- | --- | --- | --- |
| Entity attribute | member of | 'ref:mfaEnrolled'  entity.memberOf('ref:mfaEnrolled') | Users that are members of this group (by system name / ID path) |
| Entity attribute | member of any group | entity.memberOfAny(['ref:staff', 'ref:faculty']) | Users that are members of any of the specified groups (by system name / ID path) |
| Entity attribute | recent member of | entity.recentMemberOf('ref:staff', '30 days')  entity.recentMemberOf('ref:staff', '1 hour') | Users that were recently (but not currently) members of this group (by system name / ID path) |
| Entity attribute | has attribute | org  'org'  entity.hasAttribute(org)  entity.hasAttribute('org') | User has this attribute assigned or true for boolean attribute or has the attribute with any value for other types |
| Entity attribute | has attribute string | org==abc  org=='01234'  'org'=='012#$%45'  entity.hasAttribute(org, abc)  entity.hasAttribute(org, 'abc')  entity.hasAttribute('org', 'abc') | Users that have this string attribute assigned to them |
| Entity attribute | does not have attribute string | org != 'abc' | User that do not have this attribute with this string. Could also write as !(org == 'abc') |
| Entity attribute | has attribute integer | org==123  'org'==123  entity.hasAttribute(org, 123) | Users that have this integer attribute assigned to them |
| Entity attribute | does not have attribute integer | org != 123 | User who do not have this integer attribute assigned to them |
| Entity attribute | comparison operator | entity.hasAttributeLessThan(org, 55)  entity.hasAttributeLessThanOrEqual(org, 55)  entity.hasAttributeGreaterThan(org, 55)  entity.hasAttributeGreaterThanOrEqual(org, 5) | Users have a value compared to the scalar value. In the first example, org must be less than 55. In Grouper v5.17.2+. |
| Entity attribute | has any attribute string in list | jobCode =~ [abc, def]  jobCode =~ ['abc', 'def']  entity.hasAttributeAny('jobCode', ['abc', 'def']) | Users that have any of these values for this attribute |
| Entity attribute | has any attribute integer in list | jobCode =~ [123, 234]  entity.hasAttributeAny('jobNumber', [123, -234]) | Users that have any of these values for this attribute |
| Entity attribute | has attribute value like | entity.hasAttributeLike(org, '%\\_2%') | Users that have an attribute value like the SQL likeString. Note: "like" expressions are more efficient than regex   % (percent) matches any zero or more any characters   _ (underscore) matches exactly one any character   \\ (double backslash) escapes the next percent, underscore, or backslash   \\\\ (quadruple backslash) literal backslash   \' (backslash single quote) literal single quote in single quoted string   \" (backslash double quote) literal double quote in double quoted string |
| Entity attribute | has attribute with value matching regex | org =~ '^.*2.*$'  entity.hasAttributeRegex(org, '^.*2.*$') | Users that have an attribute value that matches the regex. [Recommended regex site](https://regex101.com/) to build and test a regex. Escape quotes and slashes in jexl with backslash. Less efficient than SQL like string. |
| Entity attribute | time from now | entity.hasAttributeLessThan('accessTokenExpiresAt', timeFromNow('now'))  entity.hasAttributeGreaterThan('lastLoginAt', timeFromNow('-30 days')) | Resolves to a millisecond-since-epoch value offset from the current time at script-analysis time. Argument is a single string: 'now' for the current instant, or a signed integer + unit (e.g. '30 days', '-5 minutes', '-1 year'). Negative for past, positive for future. Units accepted (singular or plural, case-insensitive): minutes, hours, days, weeks, months, years. Use anywhere a numeric / timestamp value is expected. In the last example: users who logged in within the last 30 days. |
| Entity row | has row with attribute assignment | entity.hasRow('affiliation', 'active') | Users that have an affiliation row with an attribute assigned or true for boolean, or any value for other types |
| Entity row | has row with attribute value | entity.hasRow('affiliation', 'affiliationCode==staff') | Users with a row of affiliation with a column value of attributeCode or value staff. "staff" is a string that doesnt start with an integer or have special characters in it. |
| Entity row | has row with attribute value | entity.hasRow('affiliation', "affiliationCode=='01234' ") | Users with a row of affiliation with a column value of attributeCode or value 01234. "01234" has quotes around it since it has special chars or starts with an integer |
| Entity row | does not have row with attribute value | entity.hasRow('affiliation',    "affiliationCode != '01234' ") | Users who do not have a row of affiliation with a column value of attributeCode or value 01234. "01234" has quotes around it since it has special chars or starts with an integer |
| Entity row | has row with attribute with integer value | entity.hasRow('affiliation', "affiliationCode == 1234 ") | Users with a row of affiliation with a column value of attributeCode or value 1234. In this case the affiliationCode is an integer type attribute. |
| Entity row | does not have row with attribute with integer value | entity.hasRow('affiliation', "affiliationCode != 1234 ") | Users who do not have a row of affiliation with a column value of attributeCode or value 1234. In this case the affiliationCode is an integer type attribute. |
| Entity row | has row with attribute value using comparison operator | entity.hasRow('affiliation', "affiliationCode > 1234 ") | Users with a row of affiliation with a column value of attributeCode greater than value 1234. Can use <, <=, >, >=. In Grouper v5.17.2+. |
| Entity row | has row with attribute string in list | entity.hasRow('affiliation', 'affiliationCode =~ [staff, fac, alum]') | Users with row of affiliation and has column affiliation code in staff, fac, alum |
| Entity row | has row with attribute string not in list | entity.hasRow('affiliation', 'affiliationCode !~ [staff, fac, alum]') | Users with row of affiliation that does not have a column affiliation code in staff, fac, alum |
| Entity row | has row with attribute value SQL like string | entity.hasRow('affiliation', "hasAttributeLike(affiliationCode, '%f%') ") | Users with row affiliation where a value for column affiliationCode is has an f in it. This is more efficient than regex. |
| Entity row | has row with attribute value that matches a regex | entity.hasRow('affiliation', "hasAttributeRegex(affiliationCode, '^.*f.*$' )" ) | Users with row affiliation where a value for column affiliationCode has an f in it. Users that have an attribute value that matches the regex. [Recommended regex site](https://regex101.com/) to build and test a regex. Escape quotes and slashes in jexl with backslash. Less efficient than SQL like string. |
| Entity row | compare two columns | entity.hasRow('affiliation', 'attributeCompare(affiliationDeptNumber < affiliationDeptNumberPrimary) | Users with row affiliation where column affiliationDeptNumber is less than column affiliationDeptNumberPrimary. Operator can be < > == <= >= !=   In Grouper v6.1.2+, v7.1.0+ |
| Entity row | compare two columns with addition or subtraction | entity.hasRow('affiliation', 'attributeCompare(affiliationDeptNumber + 5 < affiliationDeptNumberPrimary) | Users with row affiliation where column affiliationDeptNumber (plus 5) is less than column affiliationDeptNumberPrimary. Operator can be < > == <= >= !=   Note: the math can be on the left or right or both of the operator. The scalar must be to the right of the column data field (i.e. 5 + affiliationDeptNumber is not allowed. Multiple math operations or parens or other things are not allowed.   In Grouper v6.1.2+, v7.1.0+ |
| Entity row | range of strings or ints | entity.hasRow('affiliation', "   hasAttributeBetween(    '0200' <= affiliationDeptNumber,     affiliationDeptNumber < '0248'   )") | Users with row affiliation where the affiliationDeptNumber between two numbers or strings, inclusive or exclusive. The same column must be used in both arguments, and the order of the scalars and the column must be as shown. Can only user < or <= to denote exclusive and inclusive.   In Grouper v6.1.2+, v7.1.0+ |
| Entity row | time from now | entity.hasRow('enrollment', "startDate <= timeFromNow('now') && endDate >= timeFromNow('now')") | Users with an enrollment row where the start date is on or before now, and the end date is on or after now — i.e. an enrollment that is currently in effect. timeFromNow can be used inside hasRow for row column comparisons (<, <=, >, >=, ==, !=) and inside hasAttributeBetween. |
| Member attribute | subject source id | member.subjectSourceId == 'jdbc' | Members from a particular subject source. |

## Boolean logic

To translate from composite terminology. Note, with composite

| **Grouper composite** | **ABAC** | **Meaning** | **Notes** |
| --- | --- | --- | --- |
| ref:org1 union ref:org2 | "ref:org1" or "ref:org2" | Members who are in either group or both: ref:org1, ref:org2   e.g. you want both populations in the resulting population | Union doesn't really exist in Grouper.   To do this in Grouper you would just add the group "c:d" to be a member of "a:b" |
| app:admins_manual intersect ref:employee | "app:admins_manual" and "ref:employee" | Members who are in both groups (but not in either or neither): app:admins_manual, ref:employee   e.g. you want to make sure members in the first population (a manual group perhaps) are also in the second group (an eligibility group like: employees) |  |
| app:users_allow minus ref:locked_out | "app:users_allow" and !"ref:locked_out" | Members who are in the first group app:users_allow but not in the second group ref:locked_out |  |
| app:admins_manual_employee is app:admins_manual intersect ref:employee  app:admins is app:admins_manual_employee intersect ref:mfa | "app:admins_manual" and "ref:employee" and "ref:mfa" | Members who are in the manual admins group, who are also employees, and also enrolled in mfa | A three part intersection requires an intermediate group in Grouper. In ABAC it can be one script. |
| app:wiki_users is app:wiki_users_allow_emp minus app:wiki_users_deny  app:wiki_users_allow_emp is app:wiki_users_allow intersect ref:employee | ("app:wiki_users_allow" intersect "ref:employee") and !"app:wiki_users_deny" | Members who are not in the deny list. Members must be employees. Members are in the allow list. | A complex user policy requires intermediate groups using Grouper composites. In ABAC it is one script. Use parenthesis. |

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

## JEXL loaded groups

In Grouper v2.6.6+ there is a first pass at JEXL loaded groups using memberships of groups only. In v5+ scripted groups can also be based on [entity data fields](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545275). It is basic and can be built on. Note: this is subject to change as we see a working solution and discuss the optimal path forward.

> **See the blog!**
> 
> For more info, see the February 2022 blog on [Attribute Based Access Control with Grouper](https://incommon.org/news/new-features-with-grouper/).

[Video](https://youtu.be/RLr3mhxU35o)

Expression language (JEXL) scripts facilitate implementing the part of ABAC that defines who is included in a policy based on attributes of those users. Other parts of ABAC such as resource attributes or environment attributes can be taken into consideration with Grouper permissions or by the service which has protected resources.

We want to be able to craft policies by an expression instead of creating loaders or tons of reference groups based on cartesian products of basis/ref groups.

Individual groups can be configured to automatically have their membership managed with individual subjects (or in future groups as members)

Why do we need this feature?

- Reduces pre-loaded rollups that might not be used
- You don't need a loader job for each one of these groups
- Any Grouper user could edit the policies if they can READ underlying groups. The expressions are secure (future state)
- The memberships of the ABAC groups are near real time based on an intelligent change log consumer (future state)
- You can have a UI to help build it and give good error messages
- Could visualize the policies. Perhaps could be integrated into existing visualization (future state)
- This solves the issue of composites with any number of factors

## UI to configure

## Subject source configuration

This section is for Grouper versions: v7.0.4+ and v6.1.2+. By default, ABAC JEXL script evaluation considers all institution-defined subject sources (e.g. `myPerson`), excluding Grouper-internal sources (`g:gsa`, `g:isa`) and Grouper-managed sources (`grouperEntities`, `grouperExternal`). Three configuration properties in `grouper.properties` control this behavior:

**Global default subject sources**

`grouper.abac.globalDefaultSubjectSourceIds`

Comma-separated list of subject source IDs to include in ABAC script evaluation when no per-group override is set. If blank (default), all non-internal subject sources are used, excluding `g:gsa`, `g:isa`, `grouperEntities`, and `grouperExternal`. Imagine you had 100 ABAC groups, which subject source or sources would the majority have? That should be your default(s). Generally this should be your one person subject source.

Example: `jdbc, pennperson`

**Allow per-group override**

`grouper.abac.allowUserOverrideSubjectSourceIds`

Boolean (default `false`). When `true`, users with admin privileges on a JEXL script group can override the default subject sources on a per-group basis via the UI.

**Available subject sources for override**

`grouper.abac.availableSubjectSourceIds`

Comma-separated list of subject source IDs that users can choose from when overriding the defaults. If blank, the per-group picker is not shown even if `allowUserOverrideSubjectSourceIds` is `true`. The list must contain at least two sources for the picker to appear.

Example: `jdbc, grouperEntities`

**How it works**

The view and edit screens for a JEXL script group always display checkboxes for all non-internal subject sources, showing which sources are active (checked) and which are not (unchecked).

- **View screen**: Checkboxes are always disabled. They reflect the effective sources for the group (per-group override if set, otherwise the global defaults).
- **Edit screen, override not allowed**: Checkboxes are disabled, showing the global defaults.
- **Edit screen, override allowed**: A radio toggle lets the user choose between "Use default subject sources" (shows disabled checkboxes with global defaults) and "Customize subject sources" (shows editable checkboxes from the available list).  
    
  Default:   
    
    
  Customized:

When per-group subject sources are customized, only sources from the `availableSubjectSourceIds` list are accepted. If a per-group override contains sources not in the available list, those sources are silently ignored. If all per-group sources are invalid, the global defaults are used.

**Including Grouper entities or external subjects**

To include `grouperEntities` (local entities) or `grouperExternal` (external subjects) in ABAC evaluation, add them explicitly to the configuration. They are never included by default but can be configured as global defaults, available sources, or both.

For example, to allow ABAC scripts to evaluate both regular subjects and local entities:

```
grouper.abac.globalDefaultSubjectSourceIds = jdbc
grouper.abac.allowUserOverrideSubjectSourceIds = true
grouper.abac.availableSubjectSourceIds = jdbc, grouperEntities
```

With this configuration, JEXL script groups default to evaluating only `jdbc` subjects. Group admins can override a specific group to also include `grouperEntities`, for example to grant access to both people and service accounts based on group membership or data attributes.

**Incremental sync**

Changing the subject source IDs on a JEXL script group triggers the incremental ABAC daemon to recalculate that group's membership. There is no need to wait for or manually trigger a full sync.

## Incremental daemon

In Grouper v5.18.0 there is an incremental daemon.

- Runs as a change log consumer
- Looks for changes to groups, data rows, and data fields
- Sees which groups have scripts that use those groups, data rows, and fields
- Skips events that occurred before the last successful full sync started
- Skips events that occurred on scripted groups before the last group sync started
- Tracks group syncs with an attribute in the JEXL group attributes
- If a script is added or is changed, it will perform a group sync
- Group syncs and full syncs will track the dependencies for groups, data fields, and data rows in the database
- If the membership delta is 1500 or more, it will do a group sync on the group
- For incremental actions, the query for those affected users are run against the script and the group, and the adds/deletes are performed
- The incremental and full will not run at the same time

In future:

- Use threads
- Add more tests
- Add more logs

## Daemon screen

Note in Grouper v2.6.6 you need to wait an hour after changing a script, or run the JEXL script loader full job. In v5+ an incremental job will adjust the members quicker. Note: there is one full daemon and one incremental daemon that handles all of the JEXL script ABAC groups. You do not add this, it is built-in

## Scripts

The script can only be written by people who can READ groups in the script and UPDATE the owner group. Since this is actually a JEXL script (not a JEXL expression), so you could have multiple lines, variables, conditionals, etc

In an entity script, the variable 'entity' is an instance of class: edu.internet2.middleware.grouper.abac.GrouperAbacEntity

You can use entity.memberOf('full:group:id:path') exactly like that to see if user is in a group or not.

| **Expression** | **Description** |
| --- | --- |
| ``` ${ entity.memberOf('ref:staff') && entity.memberOf('ref:payroll:fullTime') && entity.memberOf('ref:mfaEnrolled') } ``` | Three part intersection.  Full time staff in MFA |
| ``` ${ ( entity.memberOf('ref:employee')  \|\| entity.memberOf('ref:student')  // employees or students   \|\| (entity.memberOf('ref:guests')      && entity.memberOf('app:vpn:vpnManualOverrides'))) // or guests who are in manual allow   && !entity.memberOf('ref:globalLockout')   && !entity.memberOf('app:vpn:vpnManualLockout') }  // and not in either lockout group ``` | Example policy  That means users who are not in globalLockout and not in vpnManualLockout    and in an eligible population which is faculty, students, or guests who are in the manual app override group |

## How it works in v5+

The script is parsed and converted to SQL. The results represent the members of the group. The diffs will be added or removed from the group.

## Analyze policy

To confirm a policy is correct, a long form translation of the policy can be displayed along with group names and group counts

## Policy patterns

Your institution can make a GSH template that will help users setup policies

Make a GSH template that is of type ABAC

Have inputs as you normally would to gather information from the user about the ABAC script to be generated. When you configure a scripted group, you will see all the GSH templates (you are allowed to see) when you select "pattern"

The script needs to take the inputs, construct an ABAC script, and return it to Grouper

```
    gsh_builtin_gshTemplateOutput.assignAbacScript(script.toString());
```

```
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.subject.*;
import edu.internet2.middleware.grouper.cfg.*;
import edu.internet2.middleware.grouper.misc.*;
import edu.internet2.middleware.grouper.util.*;
import edu.internet2.middleware.grouper.app.attestation.*;
import edu.internet2.middleware.grouper.app.loader.*;
import edu.internet2.middleware.grouper.app.gsh.template.*;
import edu.internet2.middleware.grouper.attr.assign.*;
import edu.internet2.middleware.grouper.attr.finder.*;
import edu.internet2.middleware.grouper.attr.value.*;
import edu.internet2.middleware.grouperClient.jdbc.*;
import org.apache.commons.lang3.*;
import java.util.*;

public class Test127abacAffiliations extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    
    
    
    Boolean gsh_input_hasPennkey = gshTemplateV2input.getGsh_builtin_inputBoolean("gsh_input_hasPennkey");
    String gsh_input_affiliationCode = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_affiliationCode");
    boolean gsh_input_includeLeave = GrouperUtil.booleanValue(gshTemplateV2input.getGsh_builtin_inputBoolean("gsh_input_includeLeave"), true);

    StringBuilder script = new StringBuilder("entity.hasRow('affiliation', \"");

    boolean hasColumnAlready = false;
    if (gsh_input_hasPennkey != null && gsh_input_hasPennkey) {
      if (hasColumnAlready) {
        script.append(" &&");
      }
      script.append(" affiliation_has_pennkey");
      hasColumnAlready = true;
    } else if (gsh_input_hasPennkey != null && !gsh_input_hasPennkey) {
      if (hasColumnAlready) {
        script.append(" &&");
      }
      script.append(" !affiliation_has_pennkey");
      hasColumnAlready = true;
    }

    if (!StringUtils.isBlank(gsh_input_affiliationCode)) {

      if (hasColumnAlready) {
          script.append(" &&");
      }
      script.append(" affiliation_code == '" + gsh_input_affiliationCode + "'");
      hasColumnAlready = true;
    }

    if (!gsh_input_includeLeave) {

      if (hasColumnAlready) {
          script.append(" &&");
      }
      script.append(" affiliation_banner_student_status == 'AS'");
      hasColumnAlready = true;
    }
    
    script.append(" \" )\n");
    gsh_builtin_gshTemplateOutput.assignAbacScript(script.toString());
    
  }
}
```

```
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.app.gsh.template.*;

public class SimpleReportstoAbac extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input input, GshTemplateV2output output) {

    // Get the built-in template output holder
    GshTemplateOutput builtInOutput = output.getGsh_builtin_gshTemplateOutput();

    // Read the input string from the template form
    String gsh_input_reportsto = input.getGsh_builtin_inputString("gsh_input_reportsto");

    // Construct the JEXL expression
    String jexl = "\${entity.hasAttribute('cs_reports_to', '" + gsh_input_reportsto + "')}";

    // Assign it as the ABAC script
    builtInOutput.assignAbacScript(jexl);
  }
}
```

## Access

(v7.0.4+ and v6.1.2+) To restrict who can create or edit scripted (ABAC) groups, set `grouper.abac.edit.if.in.group` in `grouper.base.properties` to a group name (e.g. `etc:abacEditors`). Only members of that group who also have admin privilege on the target group will be able to create or edit scripted groups. If blank (the default), any group admin can create or edit scripted groups. Wheel/root users are always allowed regardless of this setting. Note: To use a group you need READ on the group, to analyze a data field/row you need READ in the privacy realm, to save a new ABAC script, you need UPDATE in the privacy realm for any data field/row.

## Max membership size

To prevent users from configuring large groups that may impact Grouper, users can be restricted based on the membership size of the resulting group.

```
# Max ABAC scripted group membership size limits.
# Configure one or more numbered tiers; each tier is a group whose members may save scripted groups up to maxSize.
# When a user is in multiple tier groups, the highest maxSize wins.  If no tiers are
# configured and no default below is set, there is no limit (the feature is off).
# {valueType: "group", regex: "^grouper\\.abac\\.maxMembershipSizeLimit\\.[^.]+\\.groupName$"}
# grouper.abac.maxMembershipSizeLimit.0.groupName =

# Max size for the associated group.
# {valueType: "integer", regex: "^grouper\\.abac\\.maxMembershipSizeLimit\\.[^.]+\\.maxSize$"}
# grouper.abac.maxMembershipSizeLimit.0.maxSize =

# Base max scripted group membership size for ABAC users who are in none of the tier groups
# above.  Blank means unlimited for those users.
# {valueType: "integer"}
grouper.abac.defaultMaxMembershipSizeLimit =
```

## Features

- (v7.0.4+ and v6.1.2+) Unresolvables and deleted entities are not eligible to be a member in scripted groups

## See also

[Access Management Features Overview](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544689)
