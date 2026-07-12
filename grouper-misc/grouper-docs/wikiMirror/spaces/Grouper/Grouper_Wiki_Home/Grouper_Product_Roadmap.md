---
title: "Grouper Product Roadmap"
space: Grouper
pageId: 28541781
version: 357
lastUpdated: 2026-07-12T15:25:58.928Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541781/Grouper+Product+Roadmap
---

# Grouper Product Roadmap

This roadmap sketches substantial and signal functional enhancements to Grouper, and to align at least some of them with future releases. It is (always!) a work in progress, subject to the considerations and requirements of participants in the Grouper Working Group. It is also a proposition: it represents the default plan that the Grouper core developers will attempt to implement.  
Items that have fallen off of the roadmap appear further below with some explanation as to why.

# Grouper Version Support

Grouper developers offer support to the versions released in the last 3 months or the latest version in the active major versions (currently 4.x, 5.x).

See [Grouper Versioning and Support info here.](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544481/Versioning+Support+Policy)

| **Release** | **Tentative date or time frame** | **Support** | **Notes** |
| --- | --- | --- | --- |
| **v1.6** | [Released June 2010](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=grouper&title=v1.6%20Release%20Notes&linkCreation=true&fromPageId=28541781) | None |  |
| **v2.0** | [Released September 2011](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793436/v2.0+Release+Notes) | None |  |
| **v2.1** | [Released March 2012](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793451/v2.1+Release+Notes) | None |  |
| **v2.2** | Released July 2014 | None |  |
| **v2.3** | [Released April 2016](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793456/v2.3+Release+Notes) | None |  |
| **v2.4** | [Released August 2018](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793480/v2.4+Release+Notes) | None |  |
| **v2.5** | [Released April 2020](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793539/v2.5+Release+Notes) | None |  |
| **v2.6** | [Released September 2021](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793558/v2.6+renamed+to+v4+Release+Notes) | None | Has both new provisioning and subject sources as well as old |
| **v4** | [Released March 2023](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549344/v4+Release+Notes) | Stable release | Is same as v2.6, but using semantic versioning |
| **v5** | [Released October 2023](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549048/v5+Release+Notes) | Stable release | Will only have new provisioners |
| **6.0** | [Released February 2026](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547614/v6+Release+Notes) | Stable release | Stable version of v5    ### **These are gone in v6**  Apache, shib SP  WS SOAP  Non-provisioning-framework provisioners    - googleapps-google-provisioner - grouperAtlassianConnector - grouper-aws-changelog - grouper-azure - grouper-box - grouper-duo - grouperKimConnector - grouper-pspng - grouper-remedy - grouper-remedyDigitalMarketplace - grouperScim - grouper-shib - grouper-tierApiAuthz - grouper-tier-scim - grouper-installer    ### **These will still be in Grouper going forward**  All provisioning framework connectors   Custom change log consumers   Messaging connectors    - grouper-messaging-activemq - grouper-messaging-aws - grouper-messaging-rabbitmq |
| **v7** | Estimated Q4 2026 | Not released | Will redo how data is stored in the database in order to make things faster and use fewer resources  Will only have new data field subject sources |

Work on migrating away from legacy provisioners, SOAP, and Apache in v4.

| **Release** | **Item** | **Description** |
| --- | --- | --- |
| **v7** | Grouper Oauth 2.1 | Originally this is tightly coupled with MCP but could be leveraged for other things in the future. Grouper is an Oauth 2.1 Authorization server which can allow users to approve scopes for access tokens. |
| **v7** | [Grouper MCP](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547487/Grouper+MCP+server) | AI MCP server on top of Grouper Oauth and Grouper WS. |
| **v7** | Provisioning activity and auditing | Write audits to the provisioning audit table and read that from UI. Errors to audit table too? |
| **v7** | External system usage | Report on external systems to see where they are used |
| **v7** | Add more [ABAC / data field features](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups) | Add visualization, attribute resolver, and renames. |
| **v7** | Implement centralized SQL batch sizes | See what the batch size is for each DB vendor and set a default which can be overridden. Adjust hardcoded batch sizes with these defaults |
| **v7** | External system documentation in wizard | For each external system document the specifics in the wizard |
| **v7** | Upgrade JS libraries | JS libraries |
| **v7** | Remove legacy subject source configs | Only new subject source available |
| **v7** | Add bulk operations | Make bulk operations faster, e.g. creating or deleting a list of groups, adding or removing a list of memberships. Add bulk hooks |
| **v7** | Redesign Grouper DDL | Reduce size, improve efficiency, move to single purpose tables/structure. Simple integer foreign keys (sequence or auto increment). Simple integer enums. Compact core tables with external auxiliary tables. |
| **v7** | Performance diagnostics | Administrative function to measure and diagnose the performance of a deployment |
| **v7** | Cache redesign | Analyze and improve how Grouper caches objects in and out of Hibernate. Simply the subject API |
| **v7** | Remove Voot | Remove the Voot provisioner |
| **v7** | Upgrade Groovy | Major version upgrade of Groovy, for new features |
| **v7** | Reorganize Git source | Reorganize Git source directories to be more standard ([GRP-5134](https://todos.internet2.edu/browse/GRP-5134)) |
| **v9** | Revisit Grouper service registry | Identify services in grouper. Make them easy to see, join, manage, document, attest, etc.   [https://docs.google.com/document/d/1zV2kuAKOwoBFIf4GIpiQt6-NFsVkdbYdagDjGcJ7efQ/edit](https://docs.google.com/document/d/1zV2kuAKOwoBFIf4GIpiQt6-NFsVkdbYdagDjGcJ7efQ/edit) |
| **v9** | Re-write Grouper WS | Either use SCIM or more targeted REST/JSON to streamline operations. Proxy from old to new so legacy clients are supported. New operations will not have SOAP or XML. SOAP jars will no longer be in Grouper (proxy to another shim project) |
| **?** | GSH loader | [Allow a loader to be a GSH script to load groups and memberships (like SQL)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547778/Grouper+-+Loader+GSH) |
| **?** | Migrate Grouper git | For consistency, reporting, licensing reasons, Internet2 would like the Grouper git repo to be in its enterprise account instead of public git |
| **?** | Simplify UI | Make UI task oriented and easy to use for various types of users |
| **?** | Integrate connid | midpoint uses connid for provisioning. This is a standard. We would like Grouper to be able to load from and provision to connid connectors. We would also like to migrate our (non-pspng) connectors (e.g. duo, box, etc) to connid (if not there already) and share with midpoint. |
| **?** | Improve notifications | support people, groups, and email lists. Individual email addresses are problematic. Add ability to batch emails. Log emails (temporarily). User can control preferences. Notify configure on groups.   [Grouper email notifications](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=grouper&title=Grouper%20email%20notifications&linkCreation=true&fromPageId=28541781) |
| **?** | Curated groups | Add features to support Duke presentation   [https://meetings.internet2.edu/media/medialibrary/2019/12/05/20191211-mckee-paranoidiam_1.pdf](https://meetings.internet2.edu/media/medialibrary/2019/12/05/20191211-mckee-paranoidiam_1.pdf) |
| **?** | Membership constraints | Allow memberships to be able to be constrained for certain reasons, when those conditions are met, enable the membership, else disable. And keep the existing enabled/disabled dates if applicable |
| **?** | GraphQL WS interface | Implement graphQL on web services |
| **?** | Custom Grouper types | Allow institution specific types to be added. Get requirements from community. |
| **?** | Daily report refactor | Refactor the Grouper "daily" report. make it a dashboard on UI. Keep calculations in attributes if they arent already there with instrumentation. See what features we can use from Michael Gettes dashboard. See what features from Chad Redman email on April 9, 2019 with his daily report features |
| **?** | Changelog improvements | Allow change log consumers or message publishers to process messages before the single threaded "change log temp" processor completes.  Or, not that change log temp is quicker, allow change log consumers to keep track of which messages they have processed so messages can be processed out of order |
| **?** | Register for notifications | Add ability for users to register to be notified of changes to specified objects. Note, [there are rules to email users about changes to memberships](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554715/Grouper+rules+pattern+-+Send+email+after+membership+remove) |
| **?** | Provision lifecycle events | Events (such as admission, enrollment, new hire, etc.) must trigger lifecycle stage transitions, role changes, affiliation changes, etc. Those can then cause other events such as service eligibility. Lifecycle changes or affiliations all precipitate a need for provisioning wherein roles are mapped to services / entitlements. |
| **?** | Workflow state groups | The solution must support high level workflows between states. Group memberships transitioning among workflow state groups |
| **?** | Separation of duties | The solution must anticipate the possibility of conflicting roles in the case of multiple personae. Also allow overrides of separation of duties |
| **?** | Conflicting roles | The solutions must take into consideration that conflicting grants of authority, eg, one source indicating a grant of access and another a denial of access, must be resolvable according to the needs of each application or service context |
| **?** | Handle multiple roles | The solutions must enable individuals to have multiple roles/affiliations/relationships/whatever with the institution, each with its own lifecycle and overlapping set of access privileges needed to undertake each role. Statefulness (persistence and preservation of state) must permeate the design goals of all solution components in order to correctly and efficiently manage their access over the course of these multiple lifecycles |
| **?** | Rules on individual membership | An individual membership could have a rule that it is dependent on memberships in another group for example |
| **?** | Add remaining attribute/permission operations to WS | Add permission hierarchy services for roles, actions. Limits? Any other attribute permission services? |
| **?** | Add dropbox endpoint to provisioning |  |
| **?** | UI warn, restrict, or schedule large operations | If adding a group to another group, maybe warn, restrict, notify user that the operation will take a while to provision. Or schedule this for later? |
| **?** | Copy entitlements to another user | Copy entitlements to another user. Optionally include start and end dates |
| **?** | Automatically clean various things | If a group is marked as a composite ad hoc list (and/or maybe includes / excludes), then if the membership is no longer relevant, then set an end date for some time in the future. Optionally notify. This applies to individual permissions as well. Automatically or manually clean up redundant privs (if assigned to group and individual). Automatically or manually clean up redundant memberships (group and individual) |
| ? | Add high level help or how tos | For admins or users etc |
| ? |  | Direct/indirect should show on policy group |
| ? |  | Security model - documentation and UI opportunities - wizard? |
| ? |  | Can application owners see reference group? via attributes |
| ? | WCAG accessibility certification | Various accessibility analyses have been done by institutions. Changes have been implemented in the UI when reported. There is not yet a formal WCAG compliance certification (e.g. WCAG 2.0, 2.1). Customers needing an RFP to implement Grouper or that have requirements for a WCAG report could benefit from one. |
| ? | Upgrade bootstrap | Major upgrade to bootstrap UI CSS and JS, or migrate to something else |
| ? | Reorganize Git source | Reorganize Git source directories to be more standard ([GRP-5134](https://todos.internet2.edu/browse/GRP-5134)) |
| **On-going** | Rewrite Grouper wiki | Remove old docs and make sure missing docs are added |
| **On-going** | [Update third party libraries](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=grouper&title=Identifying%20and%20Updating%20Grouper%20Libraries%20%282017-2018%29&linkCreation=true&fromPageId=28541781) | Update third party libraries to the latest version |
| **On-going** | Update training videos | Go through training videos and either keep, re-record, annotate, or delete. Identify new training videos to make |
| **On-going** | Grouper Core enhancement | Continue adding capabilities to meet requirements from the field. |
| **On-going** | [Community contributions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541859/Community+Contributions) | Solicit and publicize [community contributions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541859/Community+Contributions) of extensions and complements to Grouper. |
| **Not yet assigned** | More provisioning connectors | Add further connectors to reflect specified group, membership, role, and permission information into external systems and services. Include Google provisioning (from the Unicon contribution to the PSPNG) |
| **Not yet assigned** | Scaling REST webservice | A page in the Administration guide, [Grouper always available web services and client](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=grouper&title=Grouper%20always%20available%20web%20services%20and%20client&linkCreation=true&fromPageId=28541781), demonstrates one way to provide always available services using a specialized client. The CIFER REST web service will need the server-side capability to provide that always-available functionality. In addition the REST API should be able to access multiple, read-only caches so it can efficiently handle any increase in query requests, most of which will not need to directly access the primary database. PSPNG should be able to provision to a database table, and WS should be able to read from that table (or tables) for simple operations. |

## Whatever happened to ... ?

A brief explanation of why some things seem to have disappeared from earlier versions of this roadmap.

| **What Happened?** | **Item** | **Description** |
| --- | --- | --- |
| **v4 (DONE)** | [Freshservice requester provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner) | Manage Freshservice requesters and groups |
| **v5 (DONE)** | [User lifecycle events](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544885/User+Lifecycle+events) | Configure lifecycle events. Capture events efficiently. Configure policies and assign to groups. Take actions when user lifecycle events are affected by policies. |
| **v5 (DONE)** | Remove unneeded externalized text | Remove admin and lite UI externalized text |
| **v5 (DONE)** | Upgrade Java libraries | Update Java libraries to latest stable versions |
| **v5 (DONE)** | Consolidate utils classes | JEXL translations have different utils classes in scope. These should be harmonized. |
| **v5 (DONE)** | Group summary screen | The main group screen should have a "Summary" tab, which does not show actual members, but will show a summary of the group, e.g. how many members (direct or indirect), types, provisioning, attributes, rules, attestation, loading, etc. The summary screen will have links to the details for various things |
| **v5 (DONE)** | Configure subject source from data fields | Subject information should be configured as data fields and configure a subject source based on data fields. The subject source is now in the Grouper database and does not need an external dependency. The performance will be improved and the searching is standardized (instead of different for SQL vs LDAP). |
| **v5 (DONE)** | Explore potential of AI to improve Grouper functions | Explore [AI calling web services with basic aut](http://GPT AI calling Grouper web service with basic auth)h, [using AI GSH Template to call script](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547932/GSH+template+AI+GPT+to+write+script), [set up a training file in git](https://github.com/Internet2/grouper/blob/GROUPER_5_BRANCH/grouper/misc/aiGsh/aiGsh.txt) |
| **v5 (DONE)** | Add more [ABAC / data field features](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups) | Add natural language, self-documentation, diagnostics, and secure editing. |
| **v5 (DONE)** | Incremental scripted groups | Manage dependencies and real time changes for ABAC scripted groups. |
| **v5 (DONE)** | Fix pac4j include with releases | Build Grouper authentication jar (minimal) and assemble the larger plugin jar when the container is built. Include the authentication plugin jar in container for easy use. |
| **v5 (DONE)** | Upgrade http client | Upgrade to supported HTTP client in all Grouper modules |
| **v5 (DONE)** | Installer in docker, remove installer java module | Move the "installer logic" to be in the Dockerfile |
| **v4 (DONE)** | Min group membership size | In loader jobs and just on groups have min group sizes   [https://todos.internet2.edu/browse/GRP-2388](https://todos.internet2.edu/browse/GRP-2388) |
| **v5 (DONE)** | Add group graph | Add group membership information for a user in the visualization. |
| **v5 (DONE)** | Normalize UUIDs, add idIndexes | For core objects which do not have idIndex, add. Normalize UUIDs so they are lower case without dash. |
| **v4 (DONE)** | Playwright in UI to sanity test Grouper | Add Playwright in UI so Grouper can be sanity tested on upgrade (or whenever) |
| **v4 (DONE)** | Make a translation utility on UI | Make a translation utility on UI to test various things... use GSH for this |
| **v4 (DONE)** | Grouper WS OpenAPI | Document the WS API with Swagger JSON. WS will host a "dynamic" and customizable WS API page. Explore client generation. |
| **v5 (DONE)** | Rules UI | Add a rules UI |
| **v4 (DONE)** | Improve grouper startup time | Grouper takes a while to startup in webapp or gsh command line. Reduced (batched) number of queries |
| **v5 (DONE)** | Upgrade libraries | Upgrade java (17), jars, |
| **v4 (DONE)** | [Remove jsonlib](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=grouper&title=Migrate%20from%20json-lib%20to%20jackson&linkCreation=true&fromPageId=28541781) | Migrate to jackson |
| **v5 (DONE)** | Add Grouper data field system | [Manage user attributes and identifiers differently than the legacy subject source system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545275/Grouper+entity+data+fields+for+ABAC) |
| **v5 (DONE)** | Single process container | Only run Tomcat in container, not TomEE, Apache, ShibSP |
| **v5 (DONE)** | Remove pspng and legacy provisioners | Only new provisioning framework, change log consumers, ESB consumers (including messaging) available |
| **v5 (DONE)** | Evaluate which upstream linux container should be used | Rocky linux |
| **v4 (DONE)** | Unicon authn | Add Unicon authn in container which implements SAML in java (and other things, CAS, etc) |
| **v2.6 (DONE)** | Add remedy provisioners |  |
| **v2.6 (DONE)** | Box provisioner |  |
| **v2.6 (DONE)** | Rewrite Grouper SCIM server | Replace the current J2EE SCIM server to only need tomcat |
| **v2.6 (DONE)** | Support JSON in grouper client | grouper client currently does XML but should do JSON (by default with option to switch back) |
| **v2.6 (DONE)** | Add OIDC UI authn | [OIDC UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548296/OIDC+authentication+to+Grouper+UI) |
| **v2.6 (DONE)** | Streamline provisioning configuration | Make it easier to configure before more people start using it (v2.6 change). There would be an upgrade instruction to run a script to help you transition (including script configs). e.g. CRUD and validation. Change docs/tests. |
| **v2.6 (DONE)** | Add provisioning loaders for non generic provisioners | Add loader for provisioners (not SQL or LDAP) like Duo or Zoom |
| **v2.6 (DONE)** | Group attributes on edit screen | [Have some configured group attributes on the group edit screen](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548837/Grouper+attribute+framework+attributes+editable+in+group+edit+screen) |
| **v2.6 (DONE)** | Add provisioning config scaffolding | [Add scaffolding for provisioning configs to generate a starting point](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560139/Grouper+provisioning+configuration+scaffolding+start+with) |
| **v2.6 (DONE)** | Add OSGI to Grouper | Add strategy to have plugins on their own classpath |
| **v2.6 (DONE)** | Entity global attribute resolver | Define a SQL or LDAP generic entity resolver which can be used in Grouper features like ABAC or provisioning |
| **v2.6 (DONE)** | ABAC JEXL scripted groups | JEXL based access policies based on memberships or attributes |
| **v2.6 (DONE)** | Improve folder security performance | Might need an extra table to hold part of the folder security decision |
| **v2.6 (DONE)** | Finalize LDAP provisioner |  |
| **v2.6 (DONE)** | Add Google provisioner |  |
| **v2.6 (DONE)** | Finish provisioning diagnostics |  |
| **v2.6 (DONE)** | Finalize Azure provisoiner |  |
| **v2.6 (DONE)** | [Add SQL provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554228/Grouper+SQL+provisioner) |  |
| **v2.6 (DONE)** | Add box provisioner |  |
| **v2.6 (DONE)** | [Add Duo role provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555002/Grouper+Duo+Administrator+Role+Provisioner) | Admin roles |
| **v2.6 (DONE)** | Add WS authn options | Trusted JWT WS, self-service JWT WS, OIDC WS |
| **v2.5 (DONE)** | Add database columns | Add database columns for group expiry (membership expiry already exists), and membership notes (maybe an attribute instead). Anything else for point-in-time? "visible" flag for UI for groups. password table for revamped WS authn. Service account subject source table? provisioning status. provisioning group status? log table? email batching? config PIT table |
| **v2.5 (DONE)** | Revise build environment and dependency retrieval | Revising code environment to get rid of dependencies and the hybrid builds (Maven and ant builds, hard to keep everything in sync)  Possible options:    1. Ivy: keep existing ant scripts and use Ivy for dependency retrieval 2. Maven: Remove ant build script and let maven drive both the build and dependency retrieval. (create various profiles for each env) 3. Gradle: Remove ant/maven build scripts. Use groovy scripts to retrieve dependencies and drive the build  Need to figure out versions for each dependency. |
| **v2.5 (DONE)** | Real time message based provisioning | Allow messaging to take events to provision new netIds (pspng) |
| **v2.5 (DONE)** | Add unicon azure integration to grouper | Add the unicon azure integration to grouper.  [https://github.com/Unicon/office365-and-azure-ad-grouper-provisioner](https://github.com/Unicon/office365-and-azure-ad-grouper-provisioner) |
| **v2.5 (DONE)** | GSH templates | Look at how the community uses GSH and move those needs into the UI |
| **v2.5 (DONE)** | Subject source adapter configuration wizard | Have grouper subject source adaptor configuration in the UI like the loader config. Explore including Midpoint and Comanage if useful |
| **v2.5 (DONE)** | [LDAP provisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework) | Improve PSPNG so it is more performant and accurate. |
| **v2.5 (DONE)** | [Provisioning in UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework) | Add UI elements to troubleshoot and monitor provisioning. |
| **v2.5 (DONE)** | [Daemon configuration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547876/Daemon+configuration) | UI elements to add/edit/remove Grouper daemons including configuration specific to each type of daemon |
| **v2.5 (DONE)** | [External systems wizards](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544712/Grouper+External+systems+configuration) | Wizards to guide administrators through configuring, managing, testing external systems. External systems and things Grouper connects to and generally have endpoints, credentials, and settings. |
| **v2.5 (DONE)** | Provisioning configuration wizard | UI screens to configure a provisioner and assign provisioning to folders and groups |
| **v2.5 (DONE)** | Provisioning controls on grouper objects | Screens on folders, groups, memberships, and subject to view, troubleshoot, and fix provisioning. Reports of activity, errors, etc. |
| **v2.5 (DONE)** | [Gantt chart for jobs](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547702/Grouper+Daemon+-+job+history+chart) | See when jobs have executed, job overlap, how long jobs take, success or error |
| **v2.5 (DONE)** | Update WS/UI authn | [Basic authn in database. Passwordless WS authn in future](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549360/Grouper+Built-in+Basic+Authentication+to+UI+and+Web+Services) |
| **v2.5 (DONE)** | Grouper installer installs container | REMOVED |
| **v2.5 (DONE)** | Container redesign | [One servlet container, easier mounts, one directory structure, fewer processes, maven build, patchless](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation) |
| **v2.4 patch (DONE)** | attributes on memberships | [allow direct and indirect attributes on memberships in UI](https://todos.internet2.edu/browse/GRP-2434) |
| **v2.5 (DONE)** | Require container | Grouper requires a container to run. No tarballs will be distributed. The grouper installer will install the container easily |
| **v2.5 (DONE)** | Expire dates on groups | [GRP-849: add enable/disable dates on groups like memberships and permisisons](https://bugs.internet2.edu/jira/browse/GRP-849) |
| **v2.4 patch (DONE)** | Custom join/leave/analyze UI | [Simple custom join/leave UI, also analyze access](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549064/Grouper+Custom+UI) |
| **v2.5 (DONE)** | Improve pagination in WS | [Cursor based paging](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544698/Cursor+based+paging+to+download+large+amounts+of+data+without+missing+records+during+inserts+deletes) |
| **v2.5 (DONE)** | Add some web services | [Add GRP-2153: Add audit log functions to the Web Service](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548223/Get+Audit+Entries)  [Add point in time options for WS get members, get groups, group save, get memberships](https://todos.internet2.edu/browse/GRP-2180) |
| **v2.4 patch (DONE)** | Screens to show attribute assignments from attribute def (name) | [GRP-2302: create screen to show attribute assignments from an attribute def](https://todos.internet2.edu/browse/GRP-2302)  [GRP-2303: create screen to show attribute assignments from an attribute def name](https://todos.internet2.edu/browse/GRP-2303) |
| **v2.4 patch (DONE)** | [Allow configuration to be stored in database](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555638/Grouper+configuration+in+the+database+and+UI) | Allow configuration to be stored in the database so common configuration is shared among all JVMs. Of course some configuration wouldnt be eligible for this (e.g. database connection information, passwords, etc) |
| **v2.4 patch (DONE)** | [Templates](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545142/Template+wizard) | Templates can create multiple folders / groups / privileges / etc at once based on a wizard UI. Built in template for a service/application, and [TIER Grouper Deployment Guide](http://doi.org/10.26869/TI.25.1) structure |
| **v2.4 patch (DONE)** | Real time message based loading LDAP by person | Allow messaging to take events to update a user in loader jobs (ldap) |
| **v2.4 patch (DONE)** | Disable loader jobs | Add ability to disable loader jobs |
| **v2.4 patch (DONE)** | Provisioning in UI | Manage and which folders and groups get provisioned in the UI |
| **v2.4 patch (DONE)** | Improve performance | Look at recent Grouper performance issues and make improvements |
| **v2.4 patch (DONE)** | [Tag Grouper Types](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545122/Assigning+types+on+objects) | Add ability to tag Reference / Basis / Authorization groups. Show this information to describe access policy |
| **v2.4 patch (DONE)** | [Visualizing Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548424/Visualization+API) | Allow the ability to show a visual graph representation of group, privilege, and permission relationships |
| **v2.4 patch (DONE)** | [Membership reports](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554409/Grouper+reporting) | See which users in a group or a folder of groups are not active. Add other attributes. Download reports. Schedule reports. |
| **v2.4 patch (DONE)** | [Membership approvals](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543753/Grouper+forms+workflow+and+approvals) | Add simple workflow (approval) for an OPTIN or UPDATE operation on a group |
| **v2.4 patch (DONE)** | Show disabled memberships | Show disabled memberships and privileges on demand and allow the user to configure enabled/disabled dates in more flexible way |
| **v2.4 patch (DONE)** | USDU expiration dates | Allow USDU to clean up unresolvable subjects that have been unresolvable for X days |
| **Completed in v2.3** | Provision to BMC Remedy | Provision memberships into remedy and digital marketplace |
| **Completed in v2.3 patch** | Deprovisioning | User interface to manage deprovisioning of subjects [Grouper deprovisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544732/Grouper+deprovisioning) |
| **Completed in v2.4** | Finish the new UI, replace admin and lite UI | Add features into the new Grouper v2.2 UI so that everything from the admin UI and the lite UI can be performed in the new UI. Remove the admin and lite UIs (redirect outdated links). Add user based auditing and overall auditing. Add new features like the ability to easily configure "rules" in the UI |
| **Completed in v2.3** | Require Java8, Tomcat8 | Standardize and require java8 |
| **Completed in v2.3** | [Add new messaging strategies](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544824/Grouper+messaging+system) | Add new messaging strategies in the Grouper Messaging system for ActiveMQ, AMQP (e.g. RabbitMQ), AWS |
| **Completed in v2.3** | [Attestation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545015/Grouper+attestation) | Groups and folders can be marked to require periodic membership review. Reminders will be emailed to group owners |
| **Completed in v2.3** | TIER API in installer | The TIER API Tomee service is installed with the grouper installer |
| **Completed in v2.3** | [Grouper loader in UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554452/Grouper+loader+on+UI) | User interface to show loader configuration, diagnostics, logs, wizard editor |
| **Completed in v2.3** | [Subject source diagnostics in UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545428/Subject+API+diagnostics) | User interface to analyze, diagnose, and recommend improvements for subject source configuration |
| **Completed in v2.3** | Harmonize configuration | Convert [sources.xml](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555326/Grouper+sources.xml+conversion+to+subject.properties) and [ehcache.xml](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554172/Grouper+ehcache.xml+conversion+to+grouper.cache.properties) to be cascaded properties files |
| **Completed in v2.3** | [Grouper loader real time updates](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555503/Grouper+loader+real+time+updates) | Allow a change log table (SQL triggers) or messages to trigger loader updates for a partial population or single user |
| **Completed in v2.3** | Grouper instrumentation | Improve and standardize Grouper logging to provide centralized metrics at an institution and the ability to upload stats to a central Internet2 server    - Around Dec 2016, make the patch default to on - Add features: Number of loader jobs, Hourly stats of number of users (UI/WS) [rate information not just count], Collect configuration (non sensitive), Performance (e.g. threadcount of loader jobs, heap size), Operations per time period for pspng / ldap server, how many messages, Subject source type - UI so administrators can see local stats |
| **Completed in v2.3** | TIER packaging for v2.4 | [In the TIER packaging for Grouper](https://spaces.at.internet2.edu/display/TPD/TIER+Package+Delivery), create Grouper docker container, integrate Grouper with Shibboleth, configure PSPNG, configure user registration with COmanage |
| **Completed in v2.3** | [UI accessibility](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793113/Grouper+accessibility) | Incorporate recommendations from Colorado UI accessibility review |
| **Completed in v2.3** | Improve GSH | Improve gsh by adding readline like capabilities (line editing, tab completions, history, etc). Use groovysh instead of beanshell. |
| **Completed in v2.3** | [Inbound messages](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548229/Grouper+messaging+to+web+service+API) | Allow Grouper to read a message queue and act on messages (e.g. membership changes etc) |
| **Completed in v2.3** | [Update third party dependencies](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=grouper&title=Identifying%20and%20Updating%20Grouper%20Libraries%20%282017-2018%29&linkCreation=true&fromPageId=28541781) | Update third party dependncies and have strategy to easily do this on each release. Document which libraries are used and licenses. |
| **Completed in v2.3** | [upgrade vt-ldap](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=grouper&title=vt-ldap%20to%20ldaptive%20migration%20for%20LDAP%20access&linkCreation=true&fromPageId=28541781) | to ldaptive (PSPNG to use ldaptive). Use adaptor |
| **Completed in v2.2** | [Unix GID management](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544315/Integer+IDs+on+Grouper+objects) | Built-in [support for managing unix GIDs by assigning a numeric ID to each group and folder](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544315/Integer+IDs+on+Grouper+objects). |
| **Completed in v2.2** | Legacy attribute migration | Migrate from legacy attributes to the new [attribute framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework) in a transparent way. The old API and WS and UI should still work correctly. Plan to migrate lists and hooks as well. |
| **Completed in v2.2** | COmanage integration | Work cooperatively with the COmanage project to integrate Grouper within [COmanage](http://www.internet2.edu/comanage/). [Integer group ID's](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544315/Integer+IDs+on+Grouper+objects), WS operation tweaks |
| **Completed in v2.2** | Subject security realms | Differently users might have different privacy requirements for the Subject API. Security by realm is implemented in the JDBC2 source adapter. Callers pass in which "realm" the search should take place in, and the source can adjust how the search takes place, what attributes look like, etc. |
| **Completed in v2.2** | Grouper user data | Store information about a user in grouper in a generic way. e.g. recently used objects. favorites, etc. |
| **Completed in v2.1** | GrouperWS high availability | In-built load-balancing to enable highly available read-only access to the Groups Registry via web services. |
| **Completed in v1.6-v2.1** | PSP, formerly Ldappc NG | Complete work on the new provisioning connector, built from the Shibboleth Attribute Resolver and SPML components. Integrate with Grouper notifications for asynchronous, incremental updating in addition to periodic batch style updating. Includes specific support for Active Directory. Package a Shibboleth DataConnector for Grouper.       Real-time and incremental provisioning will be added in v2.1.       Consider adding an SPML input to grouper capability. |
| **Completed in v2.1** | Dynamic group membership | Dynamically maintain groups and memberships based on LDAP-resident attributes. |
| **Completed in v2.0** | Point in Time Audit | Query the state of the groups registry at a prior point in time. |
| **Completed in v2.0** | Rules | Declarative triggers that perform changes to the Grouper Registry. |
| **Completed in v2.0** | Federated group membership and privileges | Built-in support for memberships and Grouper privileges to be assigned to federated identities. |
| **Completed in v2.0** | Federated group management | Enable groups from autonomous Grouper instances to be referenced by and incorporated into another Grouper instance. |
| **Completed in v2.0** | PDP | The Grouper permissions web service takes into account allow/disallow and limits to give the decision of access back to the requestor |
| **Completed in v2.0** | Lite UI enhancement | Support easier to use end-user UI components in addition to the existing administrative UI. Initial component, for managing membership of a single group, is in v1.5.       In v2.0, add simple management of attributes, roles, and permissions. |
| **Completed in v2.0** | Integrate with VOOT | Integrate Grouper with VOOT (group protocol for cloud webapps), experimental... |
| **Completed in v1.6-v2.1+** | Notification of changes | In v1.6, build on the initial implementation of incremental group, membership, and folder (or namespace) change notifications in v1.5 to provide notification based on flattened group membership to more efficiently enable relying parties to maintain membership lists. Also in v1.6, partner with a deployment using an asynchronous messaging infrastructure (perhaps an ESB) to drive enhancement of the toolkit for that style of data integration.       For v2.0, add flattened membership notification.    Somewhere along the line, add ability for users to register to be notified of changes to specified objects. |
| **Completed in v1.6** | Attribute framework | Complement the existing ad hoc attribute on groups with the ability to define and associate attributes of various types to groups, memberships, and folders. Initial release was in v1.5, comprising marker attributes. Additional attribute types in v1.6. Expose attribute framework suitably through web services interfaces in v1.6. |
| **Completed in v1.6** | Kuali Identity Management integration | A connector that enables Kuali Rice to delegate group management to Grouper. |
| **Completed in v 1.6** | Subject Web Service | Expose Subject API methods suitably via Grouper Web Services so that clients don't have to build their own way to reference Subjects. |
| **Completed in v 1.6** | External workflow integration | Integrate Grouper with Kuali Enterprise Workflow (v1.6), and maybe other implementations. |
| **Completed in v1.5** | Namespace Transition Support | The hierarchy of folders (or naming stems) in a deployment will change over time. This supports the ability to logically move or copy a group, a selection of groups, or a folder from one folder to another. This complements the capability of the XML Import/Export tool for prune & graft operations for large scale changes. |
| **Completed in v1.5** | User Audit | Report on who took which administrative action when. |
| **Completed in v1.4** | Extension hooks | Implement infrastructure within the Grouper API to enable independent extension of key internal events. Pre- and post-processing hooks will be provided for each "primitive API operation". This would make certain other tasks more feasible, notably "Notification of changes" in this roadmap and incorporation of a site's business rules. |
| **Completed in v1.4** | Enhance Web Services | Solidify the experimental Web Services support released in 1.3.0 based on field experience. |
| **The issue has been resolved with improved Grouper configuration and the cessation of the Signet project.** | Configuration and binding framework for I2MI | Identify and implement a framework in which combinations of I2MI components (currently Grouper API, Grouper UI, Grouper Web Services, Signet API, Signet UI, Ldappc, and Subject source adapters) can be easily integrated (not just in a single JVM). This is largely an issue of managing configuration and 3rd party libraries. The Spring application framework is an example of what might be used to address this need. |
| **This was overtaken by the "Enhance Web Services" item in the roadmap.** | Web service interface facades | Determine which subsets of native API capabilities should be exposed through more focused end points to facilitate access by applications to Grouper- and Signet-provided access management capabilities. Also investigate how facades may be used to manage access to underlying group and privilege management and query capabilities. |
| **Not yet assigned** | Further KIM-Grouper integration | Refine the Kuali KIM services interfaces and extend existing integration beyond group-level into roles & permissions. |
| **Not yet assigned** | Further uPortal-Grouper integration | Complete [Phase II deliverables](https://spaces.at.internet2.edu/download/attachments/10059938/uPortal-grouper-phases.pdf?version=1). Time frame for Phase III deliverables still to be determined in concert with uPortal team. |
| **Not yet assigned** | Security plugins | Spring security, Shiro, .NET plugins for Grouper WS that might be able to be distributed with the plugin itself. Initial proof-of-concept code available: [Unicon Grouper Contributions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543548/Unicon+Grouper+Contributions). |

#
