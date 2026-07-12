---
title: "Subject search tuning"
space: Grouper
pageId: 28548779
version: 10
lastUpdated: 2026-07-01T05:43:32.049Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548779/Subject+search+tuning
---

One aspect of setting up a subject source is configuring the search parameters so that Grouper users can find subjects they are looking for, easily and intuitively. This page describes the basics under the hood, and also some suggestions to maximize search usefulness.

Some settings apply to all sources, while others are specific to SQL or LDAP sources.

## Max results size

In both LDAP and SQL, this affects the maximum number of results returned from a general search, per subject source. If the number of results exceed this, there will be an error message and no returned results. In the top left search field, the error is "Please narrow your search. The number of results found is more than what is allowed in the configuration". In the member add combo box the error is"The value entered is not valid".

Although the UI label for this field reads "Default value is '100'", this isn't correct (see [https://todos.internet2.edu/browse/GRP-4439](https://todos.internet2.edu/browse/GRP-4439)). With no set value, there is no default, and all results will be returned. This also applies to the member search combo box, which performs a search for every new character typed. Thus, it is recommended to put an integer value in this field, large enough to show a reasonably sized result set without an error.

## JDBC (GrouperJdbcSourceAdapter2_5) search configuration

Both the UI upper right search form and the Member Add combo box use the configuration field "Lower search column" (subjectApi.source.{source}.param.lowerSearchCol.value). This needs to be an actual column in the source table or view. This should be a concatenated list of all of the subject fields that should be searchable. For example, "800001147|banderson|bob anderson|bob.anderson@example.edu". The search engine will automatically add prefix and suffix wildcards to all of the tokenized search terms (splitting on whitespace). Thus, using a delimiter not normally found in user data (pipe, caret, etc.) effectively finds the right matches without false positives. For the best user experience, adjust the data in the lower search field in the database so that it returns results from keywords users would intuitively expect.

It is imperative that all the data in this column is in lowercase. The search engine will convert your query terms to lowercase, but will not convert the lower search column to lowercase. Thus, if your search column has uppercase letters the subjects may not be findable. If you are using a database table and it doesn't contain a suitable field with lowercase concatenated values, creating a view based on the original table is probably necessary. For example:

```
create table my_subjects_v as
    select last_name, first_name, full_name, netid, employee_id, full_name_reverse,
    lower(concat(emplid, '|', netid, '|', full_name, '|', email)) as lower_search_column
    from my_subjects;

```

Note that the Search Attribute 0, 1, 2, ... in a different section of the UI configuration form are not used for the search in the UI upper right search and Member Add combo box.

## LDAP (GrouperLdapSourceAdapter2_5)

Both the UI upper right search form and the Member Add combo box use the configuration field "Subject free-form search filter" (subjectApi.source.ldap.{source}.search.param.filter.value). This is an LDAP filter expression that can search on any LDAP attributes that you want to be searchable in a general search. The search can include LDAP attributes not explicitly defined as Grouper subject attributes; for example, you may want to filter on object or active status, even if they aren't defined in the Grouper subject attribute section. The keyword(s) entered in the search will replace the placeholder "%TERM%" in the filter expression. The original search term is used verbatim in the placeholder; it does not split on whitespace nor does it add prefix or suffix wildcards. If you want prefix, suffix, or substring matching, add your own wildcards as needed. It is useful to have different wildcards for different attributes. This can improve search result quality as well as performance. Prefix wildcards will negate using an LDAP index, so use only when necessary.

An example LDAP general search filter:

```
(&(|(uid=%TERM%*)(employeeNumber=%TERM%*)(cn=*%TERM%*))(objectclass=eduPerson))

```

In this example, it will match uid or employee number with a suffix wildcard, or substring match on full name. With any matches, only return results where the object class is eduPerson.

Note that although the search term is treated as is, by default it will treat a comma as indicating multiple independent searches returning the combined results. Thus, a search for "smith, john" will perform two searches, one for *smith* and one for *john*, and return the combined results of both searches. if you want the results to be more intuitive (as well as drastically narrowing the results and improving performance) for users that want to search in a "Last name, First name" style, see section  to configure this.

## Search attribute index N / Search attribute name N

In summary, only the search attribute 0 is essential. this property is NOT used in the UI upper right search or in the Member Add combo box. It is used for the Member name filter in a group membership list. It does not need to be internally converted to lowercase.

This field has confused many people, because the intention of the multiple enumerated searches is not what it intuitively seems. Creating multiple search attributes does not combine them into a general unified search. The multiple search attributes are used for different levels of access, where search attribute N is mapped to an access control group N, which is configured elsewhere. The first attribute, Search Attribute 0, is used for the default public search, and is generally the only one most institutions need to set.

Thus, this search attribute 0 should contain a concatenated string of all the fields that should be involved in the general search. Where some people err is in setting only one field as search attribute 0, and don't see results appear when searching for other fields.

These search attributes are actually not the primary source of search criteria. For the search in the UI upper right as well as the Member Add combo box they are not used. Instead, those use either the "Subject free-form search filter" (LDAP) or the "Lower search column" field (JDBC). The only use of the Search Attribute properties is to populate the columns for search_string0, search_string1, etc. for the resolved member or privilege when added to a group. These values are stored in the database (table grouper_members), and the values are then used in the "Filter For" member name field. The string values are converted to lower case in the table, so the subject configuration does not need to do this conversion.

For JDBC, you can simply set "Search attribute index 0" (subjectApi.source.{source}.attribute.0.name) to the same as the lower case search column (although it is not required for this field to be lowercase). For LDAP, you can create an internal attribute using JEXL to compute a value the same as, or similar to, the "Subject free-form search filter" property (subjectApi.source.{source}.search.search.param.filter.value). For example:

- Attribute name: searchAttribute0
- format to lower case: either value works; this is converted to lowercase internally
- translation type: Translation script
- translation script: ${subject_attribute__employeenumber + '|' + subject_attribute__uid + '|' + subject_attribute__cn}
- internal: true

## Comma behavior, multiple queries, LDAP last+first and LDAP affiliation queries

The default behavior of the UI is to treat a comma as an indicator to combine multiple independent searches into a single action, returning combined results. Depending on what your institution desires to search, this may not be what you want. If you want commas to be significant in the search, set (undocumented) grouper.properties parameter grouperQuerySubjectsMultipleQueriesCommaSeparated to false. Once set to false, the search term will be used verbatim. You will now be able to use the search keyword to search on "Last, First" style subject names.

In addition, for LDAP, there are two alternative configurations to treat commas with different behavior. The comma can be used to trigger a special "Last, First" style filter, or a special affiliation filter.

### LDAP last+first filter and affiliation filter

For an LDAP subject source, when both grouper.properties parameter grouperQuerySubjectsMultipleQueriesCommaSeparated is set and subject.properties parameter subjectApi.source.{source}.search.search.param.firstlastfilter.value is set, a special behavior is triggered. It will not use the search term verbatim in the free-form %TERM% placeholder. Instead, it will split the keywords on the comma into two separate placeholders %LAST% (before the comma) and %FIRST% (after the comma). The filter used for the firstlastfilter LDAP search can be crafted to potentially offer results faster than a substring search of the free-form general search. Instead of searching the full name with prefix and suffix wildcards (which won't use any indexes), the firstlast filter can search on the separate firstname and lastname attributes, with or without wildcards. For example:

```
grouperQuerySubjectsMultipleQueriesCommaSeparated = false

```

```
subjectApi.source.mysource.search.search.param.firstlastfilter.value = (&(|(sn=%LAST%)(custompreferredlastname=%LAST%))(|(givenName=%FIRST%*)(eduPersonNickname=%FIRST%*))(objectClass=eduPerson))

```

Note that this is example is constructed to work with reasonable user expectations, that the last name is an exact match on multiple surname fields, while the first name is a match on multiple given name fields with a suffix wildcard. This yields fast responses, since there is no prefix wildcard. With minimal user education and documentation, users can perform searches in this format to return narrow results quickly.

In addition to the firstlast filter, if subject.properties parameter subjectApi.source.{source}.search.search.param.affiliationfilter.value is set, an enhanced behavior is triggered. If the search query contains both a comma and a "[XXX]" bracketed value, the LDAP filter defined for the affiliationfilter is used to do the query. Like the firstlast filter, it splits the query into %LAST% and %FIRST% placeholders. In addition, any [XXX] bracketed keyword after the comma is removed from the query and the string between the brackets is set to the %AFFILIATION% placeholder. The affiliation filter can be tailored to specific fields as needed by the users. For example:

```
grouperQuerySubjectsMultipleQueriesCommaSeparated = false

```

```
subjectApi.source.mysource.search.search.param.affiliationfilter.value = (&(sn=%LAST%)(givenname=%FIRST%*)(edupersonaffiliation=%AFFILIATION%)(objectclass=eduperson))

```

With this setting, a search for "Bentley, a[staff]" would return users matching last name Bentley, first name with wildcard A*, and eduPersonAffiliation of staff. Multiple affiliations can be added, and the results will be entries with all of them. E.g., "Bentley, a[staff][alumni]"

Somewhat flawed, but whitespace before and after the bracket is not trimmed. So a search for "anderson, bob [staff]" would only find results where the first name is "Bob " with the extra space. You should remove the extra space; i.e., "anderson, bob[staff]" for better results.

## Behavior of the Add member combo box with enter key or change focus

When typing characters into an autocomplete combo form field such as the Add member field, there is a timer of 500 milliseconds waiting for the next keypress. If there is no activity during this time, a background query will be performed, doing a free form search against all subject sources. However, if the user types the enter key, or moves off of the field by hitting the tab key or clicking somewhere else on the page, this normal action is circumvented. Instead, the query will be only for the ids and identifiers, using the exact query value without any wildcards. When an exact value is known, typing quickly followed by the enter key or changing focus can be used to resolve to a single user, without waiting for the slower free form query to take place. However, if doing this and the value in the field does not match to any id or identifier, it will return a "The value entered is not valid" error, even if it would have matched as a substring in the free form search.

## GSH scripts to debug searches

In GSH, there are commands to override configured properties, so that search changes can be modified and tested in isolation, without changing behavior outside of GSH. You can verify syntax and test various search configurations for best results, and change the real configuration in the UI once it is correct.

To test the existing search

```groovy
Source source = SourceManager.instance.getSource("eduLDAP")

// do a general search
Set<Subject> subjects = source.search("banderson")

// ===> [Subject id: 800001147, sourceId: eduLDAP, name: Bob Anderson]
```

To override the search properties non-destructively in the GSH sandbox:

```groovy
import edu.internet2.middleware.subject.config.SubjectConfig
import edu.internet2.middleware.grouper.cfg.GrouperConfig

String filter = "(&(employeeNumber=%TERM%*)(objectClass=eduPerson))"
SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.ldap.search.search.param.filter.value", filter)
SourceManager.instance.reloadSource("eduLDAP")
source = SourceManager.instance.getSource("eduLDAP")
source.search("banderson")

// no results, now only looking at employee number
```

When testing multiple comma-separated queries, GSH does not work since the comma splitting in the UI happens at a higher level than the Source.search() call. But it is suitable for testing Last/First and Affiliation filters, since you will be disabling the multiple query option anyway.

```groovy
/*** Last, First filter ***/

// undo override to get back to the original filter
SubjectConfig.retrieveConfig().propertiesOverrideMap().remove("subjectApi.source.ldap.search.search.param.filter.value")
SourceManager.instance.reloadSource("eduLDAP")
source = SourceManager.instance.getSource("eduLDAP")

// back to original query
source.search("bob anderson")
// => [Subject id: 800001147, sourceId: eduLDAP, name: Bob Anderson]

// override the multi-query option in GSH sandbox
GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperQuerySubjectsMultipleQueriesCommaSeparated = false", "false")

// set a new last/first filter
String firstlastfilter = "(&(sn=%LAST%*)(givenname=%FIRST%*)(objectClass=eduPerson))"
SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.ldap.search.search.param.firstlastfilter.value", firstlastfilter)

SourceManager.instance.reloadSource("eduLDAP")
source = SourceManager.instance.getSource("eduLDAP")
source.search("ander, bo")
// ===> [Subject id: 800001147, sourceId: eduLDAP, name: Bob Anderson]
```

You can do a similar test for an affiliation filter

```groovy
/*** Affiliation filter ***/

String affilfilter = "(&(sn=%LAST%*)(givenname=%FIRST%*)(edupersonaffiliation=%AFFILIATION%)(objectClass=eduPerson))"
SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.ldap.search.search.param.affiliationfilter.value", affilfilter)

SourceManager.instance.reloadSource("eduLDAP")
source = SourceManager.instance.getSource("eduLDAP")
source.search("anders, bo[staff]")
// ===> [Subject id: 800001147, sourceId: eduLDAP, name: Bob Anderson]
```

When the GSH terminal is exited, all settings revert back to the live settings. Restarting GSH will not retain any overrides.
