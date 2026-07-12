---
title: "Princeton University Grouper Page"
space: Grouper
pageId: 28543485
version: 22
lastUpdated: 2026-07-01T05:49:44.074Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543485/Princeton+University+Grouper+Page
---

The purpose of this contribution page will be to share our approach to [deploy](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544772/Azure+Release+Pipeline), [update container images](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544532/Container+update+process), and [monitor](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544682/Application+performance+monitoring)Grouper. We will also share our architecture diagrams and a nifty solution to create a[New Employees Grouper group](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543989/New+employees+group).

## Grouper Architecture

We opted to deploy Grouper into Azure. We have an Azure ExpressRoute back to our Source of Record (Microsoft Identity Manager) and our primary provisioning target (Active Directory).

The Grouper containers run inside an Azure App Service Plan, which basically defines the resources (CPU/ memory) that are available to the App Services (which run the actual containers). Both containers send log messages to our Log Analytics workspace and performance metrics are sent to Application Insights. We have enabled [Smart Detectors](https://docs.microsoft.com/en-us/azure/azure-monitor/app/proactive-diagnostics) within Application Insights to alert us of abnormal activity via an action group; we have received a few alerts and have adjusted settings within Grouper accordingly.

Notes regarding our Azure Resources:

- Container registry is where our container images are stored. We had an issue, 30 days post initial deployment, where the App Services stopped running and could not connect to the ACR. To resolve this, we created a Managed Identity that has permission to connect to the ACR and pull container images.
- Grouper is a noisy application from a logs perspective, and we are seeing lots of log messages to sent to our log analytics workspace. We are considering implement data caps to reduce the cost associated with the log storage.
- KeyVault is used to store sensitive information (DB password) and configuration variable values that are passed to the container during startup

See also:
