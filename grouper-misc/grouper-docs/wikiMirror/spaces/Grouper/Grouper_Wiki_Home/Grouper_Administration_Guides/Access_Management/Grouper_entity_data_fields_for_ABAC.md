---
title: "Grouper entity data fields for ABAC"
space: Grouper
pageId: 28545275
version: 57
lastUpdated: 2026-07-12T05:33:56.288Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545275/Grouper+entity+data+fields+for+ABAC
---

Entity data fields store user attribute data in Grouper so it can be used to build attribute-based access control (ABAC) policies, provision data to other systems, and run reports. This is in Grouper v5+.

Sub pages:

Contents:

## Terminology

- "data field" is a user attribute. This is named "data field" since "attribute" is used in many other places, e.g. the attribute framework

## Description

- Data field values are assigned to users, groups, or globally available
- The data can be single or multi-valued
- The data can be structured in a row (the data in a cell can still be multi-valued)
- The data is stored in Grouper, updated in real time and full syncs
- Point in time history will be maintained
- Security on data fields will ensure that private data remains private
- The data will be stored efficiently so it does not take a lot of space and queries are efficient
- Data fields are documented with examples so users can easily request access, see what data fields represent and how to use them
- Data fields can be configured in the UI
- The data can be used:
  
  
  
  - To construct ABAC policies on groups (scripted group based on data field values)
  - Subject sources can be replaced by a data field source (this is the future direction, and all subject sources will eventually need to be migrated)
  - Provisioning data about users to other systems
  - Reports about access and users
  - etc.

## Data field flow

- In this diagram, the green data field resolvers are either cached (e.g. source systems), or not (one-off which doesn't need the overhead of PIT etc)
  
  
  
  - A provisioner target could have entity data field values for users

## Previous state

## Data field and row diagram

Example usages:

- Provision any identifier to a target without having to "resolve the subject"
- Make a JEXL scripted group: People who have a payroll data row where org is "MATH" and have an affiliation data row where affiliation name is Staff or Faculty with an End date in the next month. Put a rule on that group for the Business Analyst to review people who might need to be renewed.
- Load users from Zoom and match accounts to users in Grouper by any of their email addresses
- A staff member creates a report where a column represents if the user in the service is an Employee
- A help desk worker can see the history of affiliations and troubleshoots access by seeing that the user's payroll org recently changed

## Configuration

Configuring data fields, rows, privacy realms, providers, and provider queries requires Grouper sysadmin access (a member of the wheel group, or running as root).

### Configure data field privacy realms

A privacy realm is a configuration for privacy of one or many data fields. Re-use them as much as you can to reduce the number of configurations

### Configure data fields

Each data field or row column is configured as a data field.

### Configure data rows

Rows are configured as a "table". The columns are data fields.

### Configure data providers

A data provider is a set of queries that load data into Grouper in real time or full sync

### Configure data provider queries

These select data from the target to populate Grouper with data field values. A single provider can have multiple queries. Each query has one provider.

### Configure data provider real time query

This helps the change log know which data to update

### Configure daemon to run data provider

Make a daemon and link to data provider. Note there are fulls and incrementals (if application in the data provider query). The incrementals should probably run every minute, the fulls should run daily or hourly depending on how much work there is to do, how often source data is updated, and how often data changes are needed.
