---
title: "Grouper data field subject source"
space: GrIntDev
pageId: 48793067
version: 3
lastUpdated: 2026-07-12T06:46:02.356Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793067/Grouper+data+field+subject+source
---

## Map information and identifier data fields

- Create data fields to represent subject attributes
- Directly assigned to an entity, not a "row"
- Could be multi-valued, but this is not recommended
- Does not need to have the same alias as the subject attribute name
- Need dynamic "description" examples with jexl

## Web service for data field updates

- Web service takes in data provider ids and a subject lookups
- Processes updates for those users from the data providers

## Configure subject source

- Wizard like SQL and LDAP
- Same subject source id as previous
- Configure subject attributes and map those to data fields
- Identify which fields are used in the lower case search description
- Identify the name, description, subject id, identifer 0, 1, 2, identifiers, search attributes, sort attributes

## Subject source search

- Have new tables with columns for subject internal id, subject source internal id, lower case indexed search term
- Search for prefixes of the lower search term (not full table scan)
- How many tables?
