---
title: "Grouper documentation pages to update"
space: GrIntDev
pageId: 48792957
version: 98
lastUpdated: 2026-07-12T17:02:42.134Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792957/Grouper+documentation+pages+to+update
---

Check out the [February 2025 Blog on the Grouper Documentation effort](https://incommon.org/news/community-collaborates-to-enhance-grouper-documentation/).

[All pages in the Grouper spaces with the needsdocupdate label](https://spaces.at.internet2.edu/dosearchsite.action?cql=space%20in%20(%22Grouper%22)%20AND%20type%20in%20(%22space%22%2C%22user%22%2C%22com.atlassian.confluence.extra.team-calendars%3Acalendar-content-type%22%2C%22attachment%22%2C%22com.atlassian.confluence.extra.team-calendars%3Aspace-calendars-view-content-type%22%2C%22page%22%2C%22blogpost%22%2C%22com.k15t.scroll.scroll-platform%3Ascroll-search-proxy-content-type%22)%20AND%20label%20in%20(%22needsdocupdate%22)&includeArchivedSpaces=false)

|  | **Owner** | **Page** | **Status** | **Time to complete** | **Notes** |
| --- | --- | --- | --- | --- | --- |
| 1 |  | Add the PrivilegeGroupSave and similar classes to GSH wiki |  |  |  |
| 2 | Graham | Update Authentication page |  |  | 1. Take out outdated content in subpages 2. Move high level information from "authentication ui" page to top level 3. Section for UI and WS with the table of methods and some decision criteria |
| 3 | Chris | [Document Penn's institutional specific container](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554290/Grouper+container+institutional+images) | DONE |  |  |
| 4 | Chris | [Document Penn's experience upgrading to v5](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547828/v6+Upgrade+Instructions+from+v4) | DONE |  |  |
| 5 | Chris | Document Penn's experience migrating to ldap provisionin framework from pspng |  |  |  |
| 6 |  | [Grouper external system - Web service - Oauth credential - Okta](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547432/Grouper+external+system+-+Web+service+-+Oauth+credential+-+Okta) |  |  | Make a generic web service doc page that describes all field in a non target specific manner |
| 7 |  | [Grouper container documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation) |  |  | Make another diagram for v5+ in addition to v4 (e.g. take out apache and shib, update ports, etc) |
| 8 |  | All? |  |  | Take out version numbers in page title (e.g. provisioning) |
| 9 | Chris | Grouper Book - Add a member (entity or group) to a group | DONE | 40 min | - I updated the style guide   - I reorganized the pages regarding to searching for a group   - Changed the headers to be H2   - Kept the title though I think we will redo that later   - Added updated screenshots from demo server   - Added a table of contents macro   - Labeled page with "ui"   - Added a Summary and 'Privilege requirements' section |
| 10 | Graham | Grouper Book - Assign someone to be able to create new folders or groups within a parent folder | Done | 20 minutes | - Updated steps and screenshots to reflect current version workflow - Added headers and TOC macro - Removed "Grouper Book" from page title - Labeled page with "ui" |
| 11 | Graham | Grouper Book - Assign someone to be able to manage a group | DONE | 30 minutes | - Updated steps and screenshots to reflect current version workflow - Added headers and TOC macro - Removed "Grouper Book" from page title - Labeled page with "ui" |
| 12 | Jeff W. | Grouper Book - Cardiff University case study | DONE | 5 min | Moved to Community Contributions. |
| 13 | Jeff W. | Grouper book - Connecting to a subject source | DONE | 120 min | - Rewrote document to use the Subject sources GUI - Added screenshots of the major areas of subject source configuration - Removed older content that referenced subjects.xml - Labeled page with "ui" |
| 14 | Jeff W. | Grouper Book - Create a composite group | DONE | 30 min | -Updated the style guide   -Reorganized the page to fit the summary,requirements,process flow.   -Changed headers to H2   -Updated the screenshots   -Added a table of contents macro   -Labeled page with "ui" |
| 15 | Graham | Grouper Book - Create a new folder | DONE   (Stole from Ashek - claimed in November but nothing done) |  | - Updated steps and screenshots to reflect current version workflow - Added headers and TOC macro - Removed "Grouper Book" from page title - Labeled page with "ui" |
| 16 | Graham | Grouper Book - Create a new group | DONE   (Stole from Ashek - claimed in November but nothing done) |  | - Updated steps and screenshots to reflect current version workflow - Added headers and TOC macro - Removed "Grouper Book" from page title - Labeled page with "ui" |
| 17 |  | Grouper book - Deployment and testing in live | UPDATED, but do we need it? |  |  |
| 18 |  | Grouper book - Detailed planning prior to live deployment | REMOVED |  |  |
| 19 |  | Grouper book - Development with a configure-deploy-test-review cycle | REMOVED |  |  |
| 20 |  | Grouper Book - Exploring Grouper | REMOVED |  |  |
| 21 | Graham | Grouper Book - Find a folder or a group by navigation | DONE | ~15 minutes | - Updated screenshots - Updated text to match current feature set - Added headers and TOC macro - Removed "Grouper Book" from title - Labeled page with UI |
| 22 | Ashek | Grouper Book - Find a group | DONE |  |  |
| 23 | Graham | Grouper Book - Find an entity or a group by searching | DONE | ~15-20 min | - Updated screenshots, reworded text to match current feature set - Added headers and TOC macro - Removed "Grouper Book" from title - Labeled page with UI |
| 24 | Gail | Grouper Book - Getting starte | REMOVED |  | totally obsolete! |
| 25 |  | Grouper Book - Getting started with the quickstart | REMOVED |  | page obsolete except for "Note: As of January 2012, the Quickstart has been replaced by an easier installation method.   As of May 2020 and the release of Grouper 2.5+ you should use the procedures here. ".  Just replace with link to "use the procedures here"? |
| 26 |  | Grouper book - Installation of core components | REMOVED |  | Same comments as row 19. |
| 27 |  | Grouper book -Installing an application server and the Grouper UI | REMOVED |  | Should this just point to [Install the Grouper container maturity level -1 quick start v2.6.5+ (quickstart)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555721/Install+the+Grouper+container+maturity+level+-1+quick+start+v2.6.5+quickstart)? How should the Grouper Book approach "getting started"? |
| 28 | hubing | Grouper Book - Installing the quickstart - Linux | REMOVED |  | question is out about switching it to the docker based documentation maturity -1 page |
| 29 | hubing | Grouper Book - Installing the quickstart - Windows | REMOVED |  | question is out about switching it to the docker based documentation maturity -1 page |
| 30 |  | Grouper book - Key questions to consider | REMOVED |  | links fixed. I'm sure we have a more recent version of server requirements. Also needs verbiage for containers. |
| 31 |  | Grouper Book - Management using GSH | REMOVED |  | Looks like an out-of-date version of [GrouperShell (gsh)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh). Should we remove this page, point to the up to date one, or do a tutorial here? |
| 32 |  | Grouper Book - Management using the API | REMOVED |  | No real content here. Do we need something in the Grouper Book on this topic? |
| 33 | Jeff W. | [Monitoring and Reporting](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549413/Monitoring+and+Reporting) | DONE | 60 min | - Added section about using Grouper Diagnostics for monitoring purposes - Added subpage for example zabbix rules ([Example Zabbix monitoring](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555675/Example+Zabbix+monitoring)) - Reworked daily report configuration to direct user to use the configuration UI - Sample report |
| 34 | Jeff W. | Grouper Book - Moving to production | REMOVED |  |  |
| 35 | Chris | Grouper Book - Read a group's membership list | DONE | 20 min | Updated screenshots, features, text, etc |
| 36 | Graham | Grouper Book - Remove a member from a group | DONE | 20 min | Updated screenshots and instructions to current version, added `ui` label |
| 37 |  | Grouper Book - Running in production | REMOVED |  |  |
| 38 | Jeffrey | Grouper book - Setting up the repository database | REMOVED |  | This document was pretty old, most of what is outlined is not automatic when deploying a new Grouper install. Note there was a link to this page I removed from here. |
| 39 |  | Grouper Book - Upgrading Grouper | REMOVED |  | obsolete (except for "first read release notes"). Do we have container-era content elsewhere, or should this just say 'Attend Grouper training"? |
| 40 | Jeffrey | Grouper Book - Web service | DONE | 35 min | Mostly looking for pages to link to, and reverting getting rid of SOAP documentation before we decided it should stay in for now. |
| 41 |  | Grouper Book - What is Grouper and what can it do?   [Look at this page too](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=Grouper&title=Grouper%20Info%20Sheet%20Work%20Area), consolidate, remove it | REMOVED |  |  |
| 42 |  | [Move and copy](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=GrIntDev&title=Move%20and%20Copy&linkCreation=true&fromPageId=48792957) | REMOVED |  |  |
| 43 | Reed | [Subject sources](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544786/Subject+sources) | DONE |  |  |
| 44 |  | [Grouper development team member](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543500/Grouper+Development+Team+Member) |  |  |  |
| 45 |  | [Draft script for interview](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=Grouper&title=Draft%20Script%20for%20Interview) |  |  |  |
| 46 |  | [Grouper community](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=Grouper&title=Grouper%20Community) | REMOVED |  |  |
| 47 |  | [Synchronizing Groups to Active Directory](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=Grouper&title=Synchronizing%20Groups%20to%20Active%20Directory) |  |  |  |
| 48 | Reed | [UI Terminology](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543683/UI+Terminology) | Done |  |  |
| 49 | Reed | [Grouper configuration in the database and UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555638/Grouper+configuration+in+the+database+and+UI) | Done |  |  |
| 50 | Reed | [LDAP Subject API example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549757/LDAP+Subject+API+example) | Done |  |  |
| 51 | Reed | [Migrating from the Grouper JDBC subject source to the JDBC2 subject source](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549883/Migrating+from+the+Grouper+JDBC+subject+source+to+the+JDBC2+subject+source) | Done |  |  |
| 52 | Reed | [Grouper provisioning diagnostics](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555849/Grouper+provisioning+diagnostics) | Done |  |  |
| 53 | Reed | [Grouper provisioning failsafe](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555489/Grouper+provisioning+failsafe) | Done |  |  |
| 54 | Reed | [Grouper SCIM change log client](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548072/Grouper+SCIM+change+log+client) | Done |  |  |
| 55 |  | [Grouper UI Properties](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793144/Grouper+UI+Properties) |  |  |  |
| 56 | Chad | [Grouper Google external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548243/Grouper+Google+external+system) | Done | 1 hour |  |
| 57 | Reed | [Grouper automatic membership removal if not attested](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549775/Grouper+automatic+membership+removal+if+not+attested) | Done |  |  |
| 58 | Reed | [Unique object names in Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549091/Unique+object+names+in+Grouper) | Done |  |  |
| 59 | Reed | [Grouper forms, workflow and approvals](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543753/Grouper+forms+workflow+and+approvals) | Done |  |  |
| 60 |  | [Grouper rules pattern - Add disabled date on invalid permissions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555625/Grouper+rules+pattern+-+Add+disabled+date+on+invalid+permissions) |  |  |  |
| 61 | Reed | [Grouper rules pattern - Add disabled date on invalid membership due to group](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555684/Grouper+rules+pattern+-+Add+disabled+date+on+invalid+membership+due+to+group) | Done |  |  |
| 62 | Reed | [Grouper Client](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545215/Grouper+Client) | Done |  |  |
| 63 | Reed | [Grouper deprovisioning getting started](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549251/Grouper+deprovisioning+getting+started) | Done |  |  |
| 64 | Chad | [Grouper external system mock services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547497/Grouper+external+system+mock+services) | DONE | 7 hours | Moved the original document to the [Internal Development wiki](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792562/Grouper+provisioning+mock+services), and rewrote this page with extensive notes on each system |
| 65 | Reed | [Grouper generic provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560419/Grouper+generic+provisioner) | Done |  |  |
| 66 |  | [Auditing](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=Grouper&title=Auditing) |  |  |  |
| 67 | Reed | [Grouper MidPoint provisioner example with metadata attributes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560271/Grouper+MidPoint+provisioner+example+with+metadata+attributes) | Done |  |  |
| 68 |  | [Grouper database migration utility](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549706/Grouper+database+migration+utility) |  |  |  |
| 69 | Reed | [Grouper attribute framework attributes editable in group edit screen](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548837/Grouper+attribute+framework+attributes+editable+in+group+edit+screen) | Done |  | Better keywords; does not come up in reasonable searches like "show attributes on group page". It's not just about editing, you can also view them. Change wording per comments. Remove excess screenshots |
| 70 |  | [Import-Export](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545561/Import-Export) |  |  |  |
| 71 | Reed | [Grouper automatically delete old data](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548887/Grouper+automatically+delete+old+data) | done |  |  |
| 72 | Reed | [Grouper configuration files and overlays](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549156/Grouper+configuration+files+and+overlays) | Done |  |  |
| 73 | Reed | [SFTP a delimited file and sync to SQL](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548946/SFTP+a+delimited+file+and+sync+to+SQL) | Done |  |  |
| 74 | Reed | [Grouper messaging system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544824/Grouper+messaging+system) | Done |  |  |
| 75 |  | [Grouper entity data fields for ABAC](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545275/Grouper+entity+data+fields+for+ABAC) |  |  |  |
| 76 |  | [Grouper Voot Provisioner](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=Grouper&title=Grouper%20Voot%20Provisioner) |  |  |  |
| 77 | Reed | [Grouper Built-in Basic Authentication to UI and Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549360/Grouper+Built-in+Basic+Authentication+to+UI+and+Web+Services) | DONE |  |  |
| 78 | Reed | [Grouper MidPoint provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555467/Grouper+MidPoint+provisioner) | Done |  |  |
| 79 |  | [Composite group - example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548883/Composite+group+-+example) |  |  |  |
| 80 |  | [Grouper external subjects](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545569/Grouper+external+subjects) |  |  |  |
| 81 | Reed | [Authentication to the Grouper UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548676/Authentication+to+the+Grouper+UI) | Done |  |  |
| 82 |  | [API Building & Configuration](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544006/API+Building+Configuration) |  |  |  |
| 83 |  | [Grouper provisioning technical](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549432/Grouper+provisioning+technical) |  |  | Most of these are internal docs and may be moved |
| 84 |  | Grouper Azure external system |  |  | Need to create this page |
| 85 |  | Document _FILE means read a file from the env variable |  |  | Config documentation page and the container documentation page? |
| 86 |  | Look at container settings which are in v4 and not in v5 and update the container doc page (like GROUPER_LOG_TO_PIPE) |  |  |  |
| 87 |  | Grouper SCIM WS server page. |  |  | There is only a [page in the internal docs](https://grouper.atlassian.net/wiki/pages/createpage.action?spaceKey=GrIntDev&title=Grouper%20TIER%20SCIM%20server%20development&linkCreation=true&fromPageId=48792957) as the development spec. Clean it up for external users and put in the Grouper space |
| 88 | chad | [Subject sources](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544786/Subject+sources) | Rename done; page still needs improvement |  | 2025-07-21 Moved to Installation, renamed from Subject API to Subject sources  Configuring a subject source is one of the top two things to configure in Grouper. It should be more prominent, not under the category "Provisioning and integration" which seems an odd place to put it. |

## From labels
