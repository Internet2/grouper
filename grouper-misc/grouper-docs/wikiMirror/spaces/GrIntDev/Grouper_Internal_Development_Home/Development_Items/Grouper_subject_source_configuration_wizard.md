---
title: "Grouper subject source configuration wizard"
space: GrIntDev
pageId: 48792737
version: 14
lastUpdated: 2026-07-12T06:45:39.651Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792737/Grouper+subject+source+configuration+wizard
---

We will build a subject source configuration wizard.

Here is a [video](https://youtu.be/mYWm6uqX-fQ) discussion of converting over a JDBC subject source (legacy) to the configuration wizard format

## Configuration changes

The configuration of a subject source configuration needs a facelift since features were gradually added, and now can use a re-orgnization.

The current format of configuration will not be editable by the wizard. The new format and current format will co-exist until 2.6 where the new format must be used.

Its possible that we can have a conversion utility from old to new.

The configuration changes facilitate leveraging wizard-code from provisioning, and having a similar user experience as provisioning (e.g. how attributes are configured).

We will have new source adapter subclasses to separate the new vs old.

## Configuration

Configuration is in the subject.properties

configs are in the format:

subjectApi.source.<configId>.configItemSuffix = value

If the source is GrouperSourceAdapter, EntitySourceAdapter or InternalSourceAdapter or id = "grouperExternal", then internal and not editable.

| Config item | Config type | Description | Notes |
| --- | --- | --- | --- |
| **General subsection** |
| adapterClass | generic | drop down, ldap or jdbc | Only LdapSourceAdapter2_5 and JDBCSourceAdapter2_5 are supported (new subclasses of LdapSourceAdapter and JDBCSourceAdapter2) |
| id | generic | source id | id of the source (required) |
| name | generic | source name | name of the source (required) |
| enabled | generic | true/false | default true. If false, the source will not be loaded on startup. Either way source is editable and can do diagnostics |
| types | generic | dropdown | person or application, single select (required) |
| ldapExternalSystemConfigId | ldap | dropdown | shown on ldap config screen (required) |
| sqlExternalSystemConfigId | sql | dropdown | shown on sql config screen (required) |
| findResultSize | generic | just get this number of results | if doing a find query (default 100) |
| extraAttributesFromSource | generic | source attribute names to be retrieved, not listed below | if there is a SQL col, or LDAP attribute not listed below in attribute.0.sourceAttribute, to be retrieved, list here, string multiple, comma separated. i.e. these are only used in translations |
| numberOfAttributes | generic | dropdown between 1-30 | free-form. (required) |
| **Attribute subsection (subsection per attribute like provisioning) Configure attributes in order of processing (virtual last)** |
| attribute.0.name | generic | attribute name | textfield (required) |
| attribute.0.formatToLowerCase | generic | true/false |  |
| attribute.0.isTranslation | generic | true/false | if false then simple attribute from source (column or attribute), or a jexl script if true |
| attribute.0.sourceAttribute | generic | textfield  show if not isTranslation | name of a column or attribute from source to use for this subject attribute. e.g. email_address would be the email_address column  mutually exclusive with translation (required) |
| attribute.0.translation | generic | textfield  show if isTranslation | we need variables from source, and variables for other subject attributes. suggestion is (required). Note, all lower case    - ${source_attribute__first_name} - gets an attribute from the source query or filter. in this case 'first_name' column - ${subject_attribute__emailaddress} - references a previously configured subject attribute. in this case "emailaddress" |
| attribute.0.emailAttribute | generic | true/false | only one attribute can be marked true |
| attribute.0.subjectIdentifier | generic | true/false | multiple could be marked true |
| attribute.0.internal | generic | true/false | if should be exposed as subject attribute or kept internal |
| attribute.0.multiValued | generic | true/false |  |
| **Subject field attribute mapping** |
| subject id from attribute name | generic | drop down | the attribute which is the subject id (required) |
| subject name from attribute name | generic | drop down | the attribute which is the subject name (required) |
| subject description from attribute name | generic | drop down | the attribute which is the subject description (required) |
| **Search attribute subsection** |
| searchAttributeCount | generic | drop down 1-5 | (required) |
| searchAttribute.0.attributeName | generic | dropdown with all source attribute names | show if searchAttribute.0.valueType is attribute (required) |
| **Sort attribute subsection** |
| sortAttributeCount | generic | drop down 1-5 | (required) |
| sortAttribute.0.attributeName | generic | dropdown with all source attribute names | show if sortAttribute.0.valueType is attribute (required) |
| **Ldap settings subsection (ldap only)** |
| ldapSearchSubjectByIdFilter | ldap | textfield | %TERM% in filter. Note, this can be automatic if blank |
| ldapSearchSubjectByIdScope | ldap | drop down | default SUBTREE_SCOPE if blank |
| ldapSearchSubjectByIdBaseDn | ldap | textfield | base DN to search (required) |
| ldapSearchSubjectByIdentifierFilter | ldap | textfield | %TERM% in filter. Note, this can be automatic if blank |
| ldapSearchSubjectByIdentifierScope | ldap | drop down | default SUBTREE_SCOPE if blank |
| ldapSearchSubjectByIdentifierBaseDn | ldap | textfield | base DN to search (required) |
| ldapFindSubjectsFilter | ldap | textfield | %TERM% in filter. (required) |
| ldapFindSubjectsScope | ldap | drop down | default SUBTREE_SCOPE if blank |
| ldapFindSubjectsBaseDn | ldap | textfield | base DN to search (required) |
| **SQL settings subsection** |
| dbTableOrView | sql | textfield, table name | could be prefixed by schema is not in connecting schema (required) |
| **Diagnostics subsection** |
| diagnosticsDefaultSubjectId | generic | textfield | subject id that exists in the source. If entered this will be prepopulated in the diagnostics screen (if nothing entered use the current default). default value: someSubjectId |
| diagnosticsDefaultSubjectIdentifier | generic | textfield | subject id that exists in the source. If entered this will be prepopulated in the diagnostics screen (if nothing entered use the current default). default value: someSubjectIdentifier |
| diagnosticsDefaultSearchScreen | generic | textfield | search string that finds some results in the source. If entered this will be prepopulated in the diagnostics screen (if nothing entered use the current default). default value: first last |
| **Status settings subsection** |
| param.findSubjectByIdOnCheckConfig.value | generic | boolean (radio or whatever) | if this source should do a findById when the status of Grouper is checked e.g. in the status servlet (default true) |
| param.subjectIdToFindOnCheckConfig.value | generic | textfield | show if param.findSubjectByIdOnCheckConfig.value is true. This is the subjectId to check on status of Grouper. default value: grouperTestSubjectByIdOnStartupASDFGHJ |
| param.findSubjectByIdentifiedOnCheckConfig.value (note leave this misspelled) | generic | boolean (radio or whatever) | if this source should do a findByIdentifier when the status of Grouper is checked e.g. in the status servlet |
| param.subjectIdentifierToFindOnCheckConfig.value | generic | textfield | show if param.findSubjectByIdentifiedOnCheckConfig.value is true. This is the subjectIdentifier to check on status of Grouper. default value: grouperTestSubjectByIdentifierOnStartupASDFGHJ |
| param.findSubjectByStringOnCheckConfig.value | generic | boolean (radio or whatever) | if this source should do a findByIdentifier when the status of Grouper is checked e.g. in the status servlet |
| param.stringToFindOnCheckConfig.value | generic | textfield | show if param.findSubjectByStringOnCheckConfig.value is true. This is the search string to check on status of Grouper. Shouldnt return too many results for performance reasons. default value: grouperTestStringOnStartupASDFGHJ |

## To do (pre 2.6)

1. Validation when saving
2. View only
3. Breadcrumbs (view only is main for subject source)
4. Generate the new config from the old
5. Look at other configs (grouper.properties, grouper-ui.properties, grouper-ws.properties), collate subject source configuration, and move to this wizard (as override)
6. Overall subject source config
