---
title: "Unicon Grouper Contributions"
space: Grouper
pageId: 28543548
version: 15
lastUpdated: 2026-07-01T05:49:31.001Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543548/Unicon+Grouper+Contributions
---

## GSH Example scripts

### Daemon jobs

#### Loading a group from Duo users matching criteria

[https://github.com/UniconLabs/grouper-gsh-examples/blob/main/daemonJobs/ExampleDuoUserImport.groovy](https://github.com/UniconLabs/grouper-gsh-examples/blob/main/daemonJobs/ExampleDuoUserImport.groovy)

Example of a GSH script daemon job, which uses the Duo API to fetch all users from Duo, applies a filter (e.g., only enabled users, or users with 1+ phones), and synchronizes a Grouper group with the resulting user list. This demo shows usage of the following techniques which can be applied to other daemon scripts:

- logging
- using external systems, to avoid credentials in the script
- Duo API methods
- closures as filter methods
- failsafe triggers and overrides
- setting log counts for total/add/update/delete/unresolvable
- subject resolution
- membership sync

Job output log:

> `START`  
> `Duo users retrieved; total count: 150, filtered: 91`  
> `Start sync of basis:duo:duo_users`  
> `Count before: 150`  
> `Count predicted: 91`  
> `Failsafe triggered (approved for bypass) memberships to remove is more than 30%`  
> `Count after: 91`  
> `End sync of basis:duo:duo_users`

#### Constructing and uploading zip CSV files to the Canvas import API

[https://github.com/UniconLabs/grouper-gsh-examples/blob/main/daemonJobs/ProvisionCanvasCsvFullSync.groovy](https://github.com/UniconLabs/grouper-gsh-examples/blob/main/daemonJobs/ProvisionCanvasCsvFullSync.groovy)

Example daemon job to fetch memberships in Canvas policy groups and construct a zip archive of csv files that gets uploaded to the Canvas SIS import api. Demonstrates the following techniques:

- Reading configuration properties for job control
- Creating a temp directory for generated files
- Constructing random filenames
- Using the Apache Commons CSV library to create files in memory
- Using Java base methods to construct a zip file
- Retrieving the last run time for a job as a baseline for incremental changes
- Grouper API to find all policy groups in a folder plus subfolders
- Setting job status and counts while in progress
- Retrieving connection parameters from a WS bearer token external system
- Using the GrouperHttpClient class as a REST client

The daemon job sends 3 csv files - courses_groups.csv, sections_groups.csv, and enrollments_groups.csv. The folders are mapped by script to the account, subaccount, term, and section. Folder extensions map to SIS IDs, display extensions to short names, and descriptions to long names. Groups are named for the valid role types in Canvas - teacher, student, ta, etc., and marked with the policy tag. Zip files are saved before sending, which won't be saved after container restarts, but can be useful for debugging. This particular implementation is a full sync, returning all current memberships. Depending on the Canvas needs, this script can be modified to just send membership deltas, or include past memberships with deleted status.

**Grouper folder structure:**

**Canvas page for imported course:**

Job log sample output:

> `Starting script`  
> `GROUPER_POLICY_FOLDER = app:canvas:service:policy`  
> `FULL_SYNC_IF_NO_CHANGES = true`  
> `DRY_RUN = false`  
> `Creating import csv for account '1', subAccount 'DynamicGroups', and term '2024_FALL'`  
> `Working on group: app:canvas:service:policy:1:DynamicGroups:2024_FALL:GS101:GS101_SEC100:teacher`  
> `Working on group: app:canvas:service:policy:1:DynamicGroups:2024_FALL:GS101:GS101_SEC100:student`  
> `Uploading zip file instructure-2024_08_04__01_44_04_437_V9OUGBXJ.zip to Canvas API`  
> `URL: http://canvas:3000/api/v1/accounts/1/sis_imports`  
> `Parameter: batch_mode_term_id: sis_term_id:2024_FALL`  
> `Parameter: change_threshold: 50`  
> `Parameter: batch_mode_enrollment_drop_status: deleted`  
> `Parameter: batch_mode: true`  
> `File: attachment: /tmp/grouper_canvas_import/instructure-2024_08_04__01_44_04_437_V9OUGBXJ.zip`  
> `URL (final): http://canvas:3000/api/v1/accounts/1/sis_imports?batch_mode_term_id=sis_term_id%3A2024_FALL&change_threshold=50&batch_mode_enrollment_drop_status=deleted&batch_mode=true`  
> `Api response code: 200`  
> `Api response json: {"id":2,"created_at":"2024-08-04T01:44:04Z","started_at":null,"ended_at":null,"updated_at":"2024-08-04T01:44:04Z","progress":0,"workflow_state":"created","data":{"import_type":"instructure_csv"},"batch_mode":true,"batch_mode_term_id":4,"multi_term_batch_mode":null,"override_sis_stickiness":null,"add_sis_stickiness":null,"clear_sis_stickiness":null,"diffing_data_set_identifier":null,"diffed_against_import_id":null,"diffing_drop_status":null,"skip_deletes":false,"change_threshold":50,"diff_row_count_threshold":null,"user":{"id":1,"name":"admin@example.com","created_at":"2024-08-03T13:12:46-06:00","sortable_name":"admin@example.com","short_name":"admin@example.com","sis_user_id":null,"integration_id":null,"sis_import_id":null,"login_id":"admin@example.com"}}`  
> `policyGroups: 2, courses: 1, sections: 1, members: 11`

## Grouper POC Connectors for Authorization APIs

**Apache Shiro**

Grouper group membership to Shiro hasRole

Grouper permissions to Shiro hasPermission

[https://github.com/UniconLabs/cas-shiro-grouper](https://github.com/UniconLabs/cas-shiro-grouper)

##### Spring Security

Grouper group to GrantedAuthority

[https://github.com/UniconLabs/cas-spring-security-grouper](https://github.com/UniconLabs/cas-spring-security-grouper)

##### .NET

Grouper group to .NET hasRole

[https://github.com/UniconLabs/CASGrouperWebServicesWebApplication](https://github.com/UniconLabs/CASGrouperWebServicesWebApplication)

##### Jasig CAS

Course-grained access control via PersonDirectory Grouper plugin

[https://github.com/Unicon/cas-addons/blob/master/src/main/java/net/unicon/cas/addons/persondir/GrouperPersonAttributeDao.java](https://github.com/Unicon/cas-addons/blob/master/src/main/java/net/unicon/cas/addons/persondir/GrouperPersonAttributeDao.java)

##### Jasig CAS, Grouper and OAUTH

CAS and Grouper can work together with OAuth to provide authorization services

[https://github.com/UniconLabs/cas-grouper-oauth](https://github.com/UniconLabs/cas-grouper-oauth)

## Self Contained Grouper Docker Image

A self contained, fully functioning Grouper implementation distributed as a Docker container. Perfect for demos and using as a base image for development.

[https://registry.hub.docker.com/u/unicon/](https://registry.hub.docker.com/u/unicon/)
