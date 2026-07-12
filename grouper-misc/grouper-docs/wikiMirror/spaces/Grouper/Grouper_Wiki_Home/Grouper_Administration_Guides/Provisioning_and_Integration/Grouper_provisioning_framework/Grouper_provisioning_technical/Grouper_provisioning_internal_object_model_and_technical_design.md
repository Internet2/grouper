---
title: "Grouper provisioning internal object model and technical design"
space: Grouper
pageId: 28555296
version: 2
lastUpdated: 2026-07-01T05:38:35.607Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555296/Grouper+provisioning+internal+object+model+and+technical+design
---

## Workflows

There are three workflows

1. GrouperProvisioningLogic.provisionFull()
2. GrouperProvisioningLogic.provisionIncremental()
3. GrouperProvisioningDiagnosticsContainer.runDiagnostics()

## Data model

The main spot for keep provisioning data is

this.getGrouperProvisioner().retrieveGrouperProvisioningData()

This has wrappers for groups, entities, and memberships

The list of wrappers has all the data from Grouper and the target.

## Indexed data

Groups / entities / memberships are indexed by group id or sync id

this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
