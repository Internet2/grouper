---
title: "Subject source using data fields"
space: Grouper
pageId: 28549032
version: 14
lastUpdated: 2026-07-01T05:42:56.147Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549032/Subject+source+using+data+fields
---

In v5.19.0+ subject sources can be created from data fields.

The legacy subject sources and data field subject sources will be supported in v5 and v6. In v7 all subject sources must be from data fields.

## Summary

A subject source based on data fields has two parts:

1. Load data to data fields
  
  1. This can be done with a SQL or LDAP data provider, or in the future with a SCIM interface
  2. The data provider can fetch data in full sync and incremental
  3. There will be a way to notify Grouper to add a new user
2. Resolve subjects based on data field values in the Grouper database

The subjects in this data source are stored in the grouper_members table, and in the data field assignments.

When the subject source is live, the data provider is configured as a subject source data provider. This adds new rows in the grouper_members table for each new subject discovered in the data query.

Searches against the source will search the grouper_members tables and the data field assignments tables.

## Privacy realms

The data field subject source starts with privacy realms. If the subject information is the same for all users of Grouper, just start with making a public privacy realm. If you have different privacy levels for different users, you will still probably need a public realm for default data values.

Public privacy realm:

If you have multiple values for a subject attribute depending on the user using Grouper, define those realms with groups as the reader.

For example: subject attributes might map from data fields from different privacy realms

- pennperson_public: data from the public directory. If there is no attribute value for name and description, it will show the employee ID (opaque).
- pennperson_affiliate: data from the authenticated directory where users must be affiliated with the institution
- pennperson_workforce: data from the institutional directory where users must be primarily in the workforce (i.e. not a student worker)
- pennperson_private: only IAM/infosec staff members who have taken privacy training can see the data

The privacy realms have a priority so if a person using Grouper can see multiple values for a subject attribute, the most private value is be used.

In this case the readers group "pennpersonAffiliateReaders" would contain the "affiliate" reference group. So that any active affiliate using Grouper would see this level of data (e.g. from the authenticated directory). The "updaters" group would be used by service principals who are assigning data via SCIM. The viewers group is not all that useful.

## Data fields

- Model each subject attribute (or each privacy value for each subject attribute) as a data field
- These are data fields assigned to a subject, not a "row"
  
  - The data provider can select all the data fields in one query thoguh
- Note the subject ID is not modeled as a data field
- String value only
- Assigned to entity (not a row)
- Generally these are single-valued
- Multiple data fields can map to the same subject attributes (in a hierarchical fashion) so that some Grouper users can see some values for subject attributes, and other Grouper users will see more private values
  
  - For example, users who are privacy certified, can see attribute values which are otherwise hidden
- Field data use: informational (e.g. name) or identifier (e.g. netId or EPPN)

Here is an example: for the description attribute, have several data fields for different privacy levels of description

- pennperson_description_public: data from the public directory
- pennperson_description_affiliate: data from the authenticated directory
- pennperson_description_workforce: data from the institutional directory
- pennperson_description_private: includes restricted data

Different subject attributes can have different privacy realms, and each attribute can have multiple data fields which map to it. Since the data field names must be unique, they can be prefixed to group them with other data fields in the subject source.

Note: the privacy realms should be hierarchical so that affiliates can see public data, people who can see institutional data can see affiliate data, etc.

Based on the subject attributes (and the many-to-one relationship of privacy levels to subject attribute), this is what the data fields could look like

## Data provider

The data provider has a setting to identify as a subject source data provider. In the initial load of the data (if planning to migrate from an existing subject source), do not mark this as a subject source data provider

Once the subject source is configured and the legacy subject source is removed, set this to a subject source data provider.

This setting will create new rows in the grouper_members table when new subjects are discovered in the data query.

## Data provider query

Query your data provider. This example is not a great one since there should be more columns and different data for the privacy levels

## Data provider change log query

The data provider for the subject source should have a change log query so that it can add new subject as soon as they exist. Otherwise subjects will not be able to be used in Grouper until the full sync runs.
