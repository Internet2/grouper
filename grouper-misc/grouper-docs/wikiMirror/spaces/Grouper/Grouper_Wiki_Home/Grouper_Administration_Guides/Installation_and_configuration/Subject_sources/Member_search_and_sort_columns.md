---
title: "Member search and sort columns"
space: Grouper
pageId: 28548031
version: 24
lastUpdated: 2026-07-01T05:45:31.220Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548031/Member+search+and+sort+columns
---

> Member search and sort columns have been part of Grouper since v2.0 (2011, GRP-526) and remain current in all supported releases.

  To allow searching for and sorting members of a group without having to resolve every subject, Grouper stores additional columns on the `grouper_members` table:

 

- `name` – holds `subject.getName()`
- `description` – holds `subject.getDescription()`
- `sort_string0` through `sort_string4`
- `search_string0` through `search_string4`

 Search strings allow up to 2K of data and sort strings allow up to 50 bytes of data. Each sort and search string is an attribute configured per subject source. Keeping the attributes consistent across sources (for people sources at least) makes searching and sorting more useful. If a value exceeds the maximum length it is truncated rather than causing an error. Each source must have at least one search string and one sort string configured, otherwise Grouper raises an error during startup.

 

## Configuring search and sort attributes per source

 Each search and sort string maps to a subject attribute, obtainable via `subject.getAttributeValue(attributeName, false)`. For JDBC sources the attribute name is not necessarily the database column name – it must be a subject attribute. A source may combine multiple attributes into one search index (comma separated) and present the user a single search option. The built-in searches query one field per query, so you can populate additional search columns (for example with private attributes) and use the security settings below to control who may search on each.

 Each search and sort attribute is set through an init-param on the source: the param name is `searchAttribute0`–`searchAttribute4` or `sortAttribute0`–`sortAttribute4`, and the param value is the name of the attribute. Virtual (comma-separated) attributes use the `subjectVirtualAttribute_<n>_searchAttribute<m>` form.

 > Subject sources are configured through Grouper's properties-based configuration (the Grouper UI configuration editor, or the subject source config properties). A standalone `sources.xml` file is deprecated – in v5+ Grouper asks you to remove it from the classpath. The init-param names shown below (`searchAttribute0–4`, `sortAttribute0–4`, `subjectVirtualAttribute_…`) are the parameter names Grouper uses internally for every source regardless of how the source is configured.

 
```xml
<init-param>
       <param-name>subjectVirtualAttribute_0_searchAttribute0</param-name>
       <param-value>${subject.name},${subjectUtils.defaultIfBlank(subject.getAttributeValue('LFNAME'), "")},${subjectUtils.defaultIfBlank(subject.getAttributeValue('LOGINID'), "")},${subjectUtils.defaultIfBlank(subject.description, "")},${subjectUtils.defaultIfBlank(subject.getAttributeValue('EMAIL'), "")}</param-value>
     </init-param>
     <init-param>
       <param-name>sortAttribute0</param-name>
       <param-value>LFNAME</param-value>
     </init-param>
     <init-param>
       <param-name>sortAttribute1</param-name>
       <param-value>LOGINID</param-value>
     </init-param>
     <init-param>
       <param-name>searchAttribute0</param-name>
       <param-value>searchAttribute0</param-value>
     </init-param>

```

 

## Internal attributes

 Subjects can have "internal" attributes so that comma-separated virtual attributes are not returned by the Subject API (for example `Subject.getAttributeValue()`) by default unless an overloaded method is used. Specify which attributes are internal in the subject source configuration.

 
```xml
<internal-attribute>internalAttribute0</internal-attribute>
<internal-attribute>internalAttribute1</internal-attribute>
<internal-attribute>internalAttribute2</internal-attribute>

```

 

## Search and sort strings for internal and external subjects

 The sort and search string configuration for the built-in internal and external subject sources is in `grouper.properties`:

 
```
# Search and sort strings for internal users
internalSubjects.searchAttribute0.el = ${subject.name},${subject.id}
internalSubjects.sortAttribute0.el = ${subject.name}

...

#search and sort strings added to member objects
externalSubjects.searchAttribute0.el = ${subject.name},${subjectUtils.defaultIfBlank(subject.getAttributeValue("institution"), "")},${subjectUtils.defaultIfBlank(subject.getAttributeValue("identifier"), "")},${subject.id},${subjectUtils.defaultIfBlank(subject.getAttributeValue("email"), "")}
externalSubjects.sortAttribute0.el = ${subject.name}
externalSubjects.sortAttribute1.el = ${subjectUtils.defaultIfBlank(subject.getAttributeValue("identifier"), "")}
externalSubjects.sortAttribute2.el = ${subjectUtils.defaultIfBlank(subject.getAttributeValue("institution"), "")}

```

 The data in these columns is updated when a subject is resolved by id or identifier, when a new member row is created, and group names are updated when groups are renamed.

 

## How searching works

 The search columns contain lowercase characters, and searches are substring searches of each word in the string. A search for "John Doe" on `search_string0` becomes `... where search_string0 like '%john%' and search_string0 like '%doe%'`.

 

## Restricting who can search and sort

 By default every user can search and sort on any of the search and sort strings in the member table. You can restrict access per index to wheel/sysadmin users only, or to a specific group, in `grouper.properties`:

 
```
# By default, all users have access to sort using any of the sort strings in the member table and search using any of the search strings in the member table.
# You can restrict to wheel only or to a certain group.
#security.member.sort.string0.allowOnlyGroup = etc:someGroup
#security.member.sort.string1.allowOnlyGroup = etc:someGroup
#security.member.sort.string2.wheelOnly = true
#security.member.sort.string3.wheelOnly = true
#security.member.sort.string4.wheelOnly = true
#security.member.search.string0.allowOnlyGroup = etc:someGroup
#security.member.search.string1.allowOnlyGroup = etc:someGroup
#security.member.search.string2.wheelOnly = true
#security.member.search.string3.wheelOnly = true
#security.member.search.string4.wheelOnly = true

```

 > **Privileges:** end users need no special privilege to search or sort by default – access is open unless restricted per index with `allowOnlyGroup` (membership in the named group) or `wheelOnly = true` (wheel/sysadmin users only). Changing any of these settings requires access to edit the Grouper configuration (a Grouper sysadmin / file access).

 

## Default search and sort index

 You can specify the default indexes to use for searching and sorting when one is not specified. The value is comma-separated, so that if the user does not have access to the first index the next is tried, and so forth. All sources should have attributes configured for every default index.

 
```
###################################
## Member sort and search
###################################

# Attributes of members are kept in the grouper_members table to allow easy sorting and searching (for instance when listing group members).
# When performing a sort or search and an index is not specified, then a default index will be used as configured below.  The value is comma-separated,
# so that if the user does not have access to the first index, then next will be tried and so forth.
# Note:  all sources should have attributes configured for all default indexes.
member.search.defaultIndexOrder=0
member.sort.defaultIndexOrder=0

```

 

## Using search and sort in the UI and API

 The Grouper UI uses these columns when displaying a group's membership list, letting users sort and search the membership. The functionality is also available in the API via `Group.getImmediateMembers(Field, Set<Source>, QueryOptions, SortStringEnum, SearchStringEnum, String)`, and in the web service get-members calls (which can sort on `name`, `description`, and `sortString0`–`sortString4`).

 If you have enabled member sorting and disabled default-only sorting, add a label for each default sort index so users see a friendly name. The labels are in the Grouper UI text configuration:

 
```
# If you have enabled member sorting (member.sort.enabled) and disabled default sorting (member.sort.defaultOnly), be sure to add labels for each default sort string configured in grouper.properties (member.sort.defaultIndexOrder).
member.sort.string0=Name
member.sort.string1=Login Id

```

 > The `member.sort.enabled`, `member.sort.defaultOnly`, and `member.search.enabled` toggles (set via `media.properties`) and the "lite" / admin membership screens that read them are part of the legacy Struts UI. That UI was removed in v5, so these toggles apply only to v4 (legacy UI); they have no effect in v6. The sort labels above still apply to the current UI.

 

## Example layout

 Here is one way the data may be stored.

 

|  | sort0 | sort1 | sort2 | search0 | search1 (Name) |
| --- | --- | --- | --- | --- | --- |
| person source | displayName | sn | uid | displayName,uid,ou | uid,ou |
| group source | displayName | null | null | name,displayName | name,displayName |

 sort0 = Sort by name  
sort1 = Sort by last name  
sort2 = Sort by login id  
search0 = default search for privileged users  
search1 = default search for all other users

 

## Syncing member attributes

 If you change the sort or search strings, sync the member attributes. For subjects that are people, use USDU (Unresolvable Subject Deletion Utility): run the `OTHER_JOB_usduDaemon` job from the "Daemon jobs" UI page.

 For subjects that are groups, run the following using [GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh):

 
```java
gsh 0% GrouperSession.startRootSession()
gsh 1% for (String g : HibernateSession.byHqlStatic().createQuery("select uuid from Group").listSet(String.class)) { subj = SubjectFinder.findByIdAndSource(g, "g:gsa", true); GrouperDAOFactory.getFactory().getMember().findBySubject(subj).updateMemberAttributes(subj, true); }

```
