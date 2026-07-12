---
title: "(Proposal) AI agent for troubleshooting provisioning issues"
space: GrIntDev
pageId: 48794011
version: 5
lastUpdated: 2026-07-12T06:46:26.628Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794011/Proposal+AI+agent+for+troubleshooting+provisioning+issues
---

## Problem

Setting up and managing provisioning is one of the more challenging tasks in Grouper. There are a large number of ways to configure a provisioner, with a myriad of options and parameters. Getting the right combination of settings tends to be an iterative cycle of testing and improvement until the results match the desired outcome, for all the types of objects it might encounter. With default logging, uncaught errors show up as Java stack traces, but other issues such as unmatched users or incorrect values are unknown. With verbose logging turned on, the amount of information can be overwhelming for untrained users, who don't know which output is the most relevant.

This problem can be tackled using a custom AI that has knowledge of Grouper and the specific information involved in provisioning. Data can come from many sources -- configuration, log files, and per-object result data.

## Data connectors

### Log output

The output from the job run is either sent to the Docker container STDOUT, or stored in a log file if configured to do so. Depending on the deployment, the container output can be: (a) read directly from the container; or (b) pulled from a logging service like AWS CloudWatch, fluentd, Unix journal, splunk, or syslog. When using files to log, the file could potentially be within the ephemeral container, but is more likely mounted to a host location where it is more accessible and persistent.

Connectors for these log sources will need to have knowledge of the format of lines, which usually contain datetime, environment, container. It will also know about the tag IDs attached to specific provisioning runs in order to pick out the relevant data.

### Database

A database task used by the agent will likely need only a few types of information:

- groups that have provisioning set for the target provisioner
- the memberships in those groups at the time the job ran
- information about the specific job in question (the context of the agent will be geared toward specific job runs than in general provisioner health)
- member information: subject ids and identifiers
- information from the sync logs; the last run status (although it may not be for the same run), object status, and cache bucket data

Note that configuration data for a provisioner is better obtained from the log output rather than the database. It is not guaranteed that a provisioner configuration will be stored in the database, as it could be file-base. And even if it is in the database. It may not represent the values that were set at the time the job was run.

There may be gaps between database information and log output. Per Jira GRP-5981 (*Provisioner UI log should show instanceId (at least once) even with debug objects off*), the instance ID associated with a job run is not always available in a reliable way, and to identify the correct log lines in a log file sometimes needs manually guessing at the value based on timestamps.

## Approach

For the type of analysis needed for provisioner troubleshooting, a supervisor pattern may be suitable. A coordinating agent would be able to launch tasks for ingesting logs, retrieving data, and categorizing issues. The agent will have robust information from context documents, which may be enough for decision-making without the need for a custom trained agent. With the given context, the agent would be capable of ranking hypotheses, and iterate to find the correct solution.

With this approach, the bulk of the work would be on informing the context on how to identify specific issues, and suggestions for fixing them.

## Pilot plan

Multiple scenarios would be set up with different combinations of provisioning settings, groups, memberships, and external systems. The agent would run against each test to return an initial analysis of the case. The outputs of each run would be compared against a rubric to determine whether the correct answer was achieved.
