---
title: "v2.4 Bug roundup"
space: GrIntDev
pageId: 48795870
version: 95
lastUpdated: 2026-07-19T00:34:14.837Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48795870/v2.4+Bug+roundup
---

For a month starting August 7, 2019 the Grouper team will largely focus on fixing bugs. We are looking for help from the community to identify important bugs. Please make a request to slack or email or edit this page as needed to vote on an issue. Votes will be managed on this page (not in jira). We will release a weekly patch with whatever bugs are fixed.

## Bug votes and progress

Effort estimate: low is trivial, medium is a couple hours, high is more

Status can be "Not started, Working on it, Done"

Fixed in patch date can be: 8/14/19, 8/21/19, 8/28/19, 9/4/19

Fixed in patch label is label of patch released

Chris will make a patch for items that are "Done" and have a "Fixed in patch date"

| Jira number | Description | Voted by whom | Effort estimate by dev team | Developer | Status | Branch | Fixed in patch date | Fixed in patch number |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [JIRA GRP-2343] | LdapSystem.performLdapSearchRequest doesn't return any members when group has more than 1500 memberships | Auckland |  |  |  | 2.4 PSPNG P11 |  |  |
| [JIRA GRP-2342] | pspng full sync trigger configurability | Auckland |  |  |  | 2.4 PSPNG P11 |  |  |
| [JIRA GRP-2349] | Have the FullSync and the Incremental provisioners share the list of ProvisionedGroups | Auckland |  |  |  | 2.4 PSPNG P11 |  |  |
| [JIRA GRP-2348] | Slow evaluations for GroupSelectionExpression in initialize Provisioner | Auckland |  |  |  | 2.4 PSPNG P11 |  |  |
| [GRP-2263](https://grouper.atlassian.net/browse/GRP-2263) | looping in db config when getting config while getting database config | Chris Hyzer at Penn | low | Chris | Done | master | 8/14/2019 | grouper_v2_4_0_api_patch_68 |
| [GRP-2282](https://grouper.atlassian.net/browse/GRP-2282) | deprovisioning cache throws NPE after having been running for a while | Chris Hyzer at Penn | medium | Chris | Done | master | 8/14/2019 | grouper_v2_4_0_api_patch_68 |
| [GRP-2283](https://grouper.atlassian.net/browse/GRP-2283) | move ui and ws example files to api | Chris Hyzer at Penn | low | Chris | Done | master | 8/14/2019 | grouper_v2_4_0_api_patch_68 |
| [GRP-2284](https://grouper.atlassian.net/browse/GRP-2284) | configs in database which are encrypted should not be shown on screen or in audits | Chris Hyzer at Penn | low | Chris | Done | master | 8/14/2019 | grouper_v2_4_0_ui_patch_40 |
| [GRP-2264](https://grouper.atlassian.net/browse/GRP-2264) | Attestation UI doesn't show correct date to recertify and email info | Shilen Patel at Duke | med | Shilen | Done | GRP-2264-attestation-recertify-and-email-incorrect | 9/18/19 | grouper_v2_4_0_ui_patch_44 |
| [GRP-2265](https://grouper.atlassian.net/browse/GRP-2265) | Attestation UI doesn't show "Folder with settings" | Shilen Patel at Duke | low | Shilen | Done | This was fixed in GRP-2202_attestation_reports.  GRP-2202_attestation_reports hasn't been merged to master yet, Chris will look at it. | 8/19/2019 | grouper_v2_4_0_ui_patch_41 |
| [GRP-2268](https://grouper.atlassian.net/browse/GRP-2268) | typo in groups I manage page | Ben Beecher at Columbia | low | Vivek | Done | GRP-2268_typo | 8/14/2019 | grouper_v2_4_0_api_patch_71 |
| GRP-2031 | when you export as gsh, make sure its valid java when multiple objects | Chad Redman at UNC |  | Chad | Done | 201908-cer28-four-bugfixes | 8/14/19 | grouper_v2_4_0_api_patch_71 |
| GRP-2260 | AttributeDefFinder reports "AttributeDef not found" exactly when it was found | Chad Redman at UNC |  | Chad | Done | 201908-cer28-four-bugfixes | 8/14/19 | grouper_v2_4_0_api_patch_71 |
| GRP-2106 | Membership counts inaccurate when member has more than one membership | Chad Redman at UNC |  | Chad | Done | 201908-cer28-four-bugfixes | 8/14/19 | grouper_v2_4_0_api_patch_71 |
| GRP-1924 | grouperClient WsGroup bean missing alternateName property | Chad Redman at UNC |  | Chad | Done | 201908-cer28-four-bugfixes | 8/14/19 | grouper_v2_4_0_api_patch_71 |
| GRP-2244 | Groups list loader sets loaded=false for groups of all other jobs | Chad Redman at UNC |  | Chad | Done | GRP-2244_group-loader-sets-other-groups-false | 8/14/19 (separate patch for this?) | grouper_v2_4_0_api_patch_72 |
| [GRP-2241](https://grouper.atlassian.net/browse/GRP-2241) | Allow suppression of startup error "Error: Cannot properly read UTF string from database" | Chad Redman at UNC |  | Chad | Done | GRP-2241_config-db-utf8-checking | 8/14/19 | grouper_v2_4_0_api_patch_71 |
| [GRP-2249](https://grouper.atlassian.net/browse/GRP-2249) | member batch import incompatible with content security policy blocking iframes | Chad Redman at UNC |  | Chad | Done | GRP-2249_member-file-import-skip-iframe | 8/14/19 | grouper_v2_4_0_ui_patch_44 |
| [GRP-2259](https://grouper.atlassian.net/browse/GRP-2259) | Loader log message bad format "found (object reference) members overall" | Chad Redman at UNC | low | Chad | Done | GRP-2259_loader-log-msg-bad-format | 8/14/19 | grouper_v2_4_0_api_patch_71 |
| [GRP-1762](https://grouper.atlassian.net/browse/GRP-1762) | grouperInstaller should check to see if there is a new version of the grouperInstaller | Chad Redman at UNC | new feature; may need more design work |  |  |  |  |  |
| [GRP-2049](https://grouper.atlassian.net/browse/GRP-2049) | Installer checks for "javac" multiple times, once incorrectly | Chad Redman at UNC | med-high | Chad | Done | GRP-2049_installer-checks-javac-multiple-times | 8/28/2019 | installer updatels |
| [GRP-2170](https://grouper.atlassian.net/browse/GRP-2170) | change logging for EventLog session starts from INFO->DEBUG | Chad Redman at UNC | low | Chad | Done | GRP-2170_change-logging-for-session-start | 8/14/19 | grouper_v2_4_0_api_patch_71 |
| [GRP-2238](https://grouper.atlassian.net/browse/GRP-2238) | enabled/disabled membership screen shows enabled when not | Shilen Patel at Duke | low | Shilen | Done | GRP-2238 | 8/14/19 | grouper_v2_4_0_ui_patch_43 |
| [GRP-2101](https://grouper.atlassian.net/browse/GRP-2101) | Ldaptive exception reports queried attributes as array pointer | Shilen Patel at Duke | low | Shilen | Done | GRP-2101 | 8/14/19 | grouper_v2_4_0_api_patch_71 |
| [GRP-1982](https://grouper.atlassian.net/browse/GRP-1982) | member search and sort strings should trim before change | Shilen Patel at Duke | med | Shilen | Done | GRP-1982 | 8/14/19 | grouper_v2_4_0_api_patch_71 |
| [GRP-2272](https://grouper.atlassian.net/browse/GRP-2272) | PSPNG: FullSync Message warnings | Gettes at UFL | low | Bert | Done | master (oops!) | 8/14/2019 | grouper_v2_4_0_pspng_patch_9 |
| [GRP-2273](https://grouper.atlassian.net/browse/GRP-2273) | Subjects: Null subject_id leads to NPE | Bert at Georgia Tech | low | Bert | Done | GRP-2273 | 8/14/2019 | grouper_v2_4_0_api_patch_68 |
| [GRP-2274](https://grouper.atlassian.net/browse/GRP-2274) | PSPNG: JEXL Variables | Chris at Penn | low | Bert | Done | GRP-2274 | 8/14/2019 | grouper_v2_4_0_pspng_patch_9 |
| [GRP-2275](https://grouper.atlassian.net/browse/GRP-2275) | PSPNG - NPE in LdapProvisioner | Bert at Georgia Tech | low | Bert | Done | GRP-2275 | 8/14/2019 | grouper_v2_4_0_pspng_patch_9 |
| [GRP-2270](https://grouper.atlassian.net/browse/GRP-2270) | Attribute def - Trace privileges | Vivek | low | Vivek | Done | GRP-2270_attributeDef_tracePrivs | 8/14/2019 | grouper_v2_4_0_ui_patch_43 |
| [GRP-2247](https://grouper.atlassian.net/browse/GRP-2247) | Attribute def values not showing properly in the edit text field | Vivek | low | Vivek | Done | GRP-2247-attribute-values | 8/14/2019 | grouper_v2_4_0_ui_patch_43 |
| [GRP-2185](https://grouper.atlassian.net/browse/GRP-2185) | Fix copy stem issue | Vivek | low | Vivek | Done | GRP-2185-copy-stem | 8/14/2019 | grouper_v2_4_0_ui_patch_43 |
| [GRP-2203](https://grouper.atlassian.net/browse/GRP-2203) | stem membership doesnt show right count | Shilen Patel at Duke | low | Shilen | Done | GRP-2203_stem-membership-count | 8/14/19 | grouper_v2_4_0_ui_patch_43 |
| [GRP-2280](https://grouper.atlassian.net/browse/GRP-2280) | grouper warnings on startup about configuration in database | Chris at Penn | low | Chris | Done | master | 8/14/2019 | grouper_v2_4_0_api_patch_68 |
| [GRP-2219](https://grouper.atlassian.net/browse/GRP-2219) | do not submit attribute value edit form on enter key | Jason from Oregon State University | low | Vivek | Done | GRP-2219-attribute-edit-enter-key | 8/14/2019 | grouper_v2_4_0_ui_patch_43 |
| [GRP-2289](https://grouper.atlassian.net/browse/GRP-2289) | Clear out the entire cache when deprovisioning config is edited on root | Chris Hyzer | low | Vivek | Done | Deprovisioning | 8/14/2019 | grouper_v2_4_0_ui_patch_43 |
| [GRP-2281](https://grouper.atlassian.net/browse/GRP-2281) | show something about folder composites perhaps? | Chris Hyzer | med | Shilen | Done | GRP-2281-show-folder-composites | 8/28/19 | grouper_v2_4_0_ui_patch_44 |
| [GRP-2298](https://grouper.atlassian.net/browse/GRP-2298) | Assign provisioning config to folders in a separate thread | Vivek | low | Vivek | Done | GRP-2298-provisioning-separate-thread | 9/15/19 | grouper_v2_4_0_ui_patch_44 |
| [GRP-1980](https://grouper.atlassian.net/browse/GRP-1980) | ws message about versions is backwards | Chad | low | Chad | Done | GRP-1980_ws-message-about-versions | 8/28/2019 | grouper_v2_4_0_ws_patch_8 |
| [GRP-1961](https://grouper.atlassian.net/browse/GRP-1961) | gsh findSubject mixes type and source when passed | Chad | low | Chad | Resolved (updated wiki) | N/A | N/A | N/A |
| [GRP-1904](https://grouper.atlassian.net/browse/GRP-1904) | LDAP loader API validator gives error when GroupTypes set | Chad | low | Chad | Done | GRP-1904_ldap-loader-error-grouptypes-set | 9/18/2019 | grouper_v2_4_0_api_patch_74 |
| [GRP-2299](https://grouper.atlassian.net/browse/GRP-2299) | LDAP loader Updaters and attrUpdaters field using same value | Chad | low | Chad | Done | GRP-2299_ldap-loader-attrUpdaters-wrong | 9/18/2019 | grouper_v2_4_0_ui_patch_44 |
| [GRP-2335](https://grouper.atlassian.net/browse/GRP-2335) | cant find loader log map | Jeffrey Crawford | low | Chris | Done | master | 9/18/2019 | grouper_v2_4_0_api_patch_74 |
| [GRP-2300](https://grouper.atlassian.net/browse/GRP-2300) | LDAP loader in UI shows subject attribute as required differently from wiki | Chad | low | Chad | Done | GRP-2300_ui-ldap_group_list_req_indicator | 9/4/2019 | grouper_v2_4_0_ui_patch_44 |
| [GRP-2301](https://grouper.atlassian.net/browse/GRP-2301) | When uploading members to groups via files, show regular progress | Vivek | Medium | Vivek | In Progress |  |  |  |
| [GRP-2296](https://grouper.atlassian.net/browse/GRP-2296) | IncrementalLoading: Disabled Loader Jobs | Bert | med | Shilen | Done | GRP-2296-incremental-loader-disabled-jobs | 8/28/19 | grouper_v2_4_0_api_patch_74 |
| [GRP-2305](https://grouper.atlassian.net/browse/GRP-2305) | "Stem stemOwner" is repeated in export to gsh | Chad | low | Chad | Done | GRP-2305_var-repeated-in-gsh-export | 9/4/2019 | grouper_v2_4_0_api_patch_74 |
| [GRP-2307](https://grouper.atlassian.net/browse/GRP-2307) | mail.smtp.ssl=true should not also set mail.smtp.starttls.enable=true | Chad | low | Chad | Done | GRP-2307_smtps-sets-starttls-flag | 9/4/2019 | grouper_v2_4_0_api_patch_74 |
| [GRP-1902](https://grouper.atlassian.net/browse/GRP-1902) | PSPNG: Remember Group Selection between events | Bert | med | Bert | Done | GRP-1902-pspng-group-selection | 9/3/19 | grouper_v2_4_0_pspng_patch_11 |
| [GRP-2293](https://grouper.atlassian.net/browse/GRP-2293) | Option for case insensitive searching for Grouper Loader | Shilen | med | Shilen | Done | GRP-2293-loader-case-insensitive | 9/4/2019 | grouper_v2_4_0_api_patch_74 |
| [GRP-2303](https://grouper.atlassian.net/browse/GRP-2303) | Show owners for attribute def names | Vivek | med | Vivek | In Progress | GRP-2303-show-owners-attributeDefName | 10/21/19 | grouper_v2_4_0_ui_patch_49 |
| [GRP-2302](https://grouper.atlassian.net/browse/GRP-2302) | Show owners for attribute defs | Vivek | med | Vivek | In Progress | GRP-2303-show-owners-attributeDefName | 10/21/19 | grouper_v2_4_0_ui_patch_49 |
| [GRP-2318](https://grouper.atlassian.net/browse/GRP-2318) | LDAP subject source doesn't support maxResults; add option to return partial results | Jeffrey Crawford | med | Shilen | Done | GRP-2318-ldap-maxResults-and-errorOnMaxResults | 9/4/2019 | grouper_v2_4_0_api_patch_74 |
| [GRP-2325](https://grouper.atlassian.net/browse/GRP-2325) | GRP-2325: grouper hangs in some cases on startup due to thread lock | Chris | low | Chris | Done | master | 9/4/2019 | grouper_v2_4_0_api_patch_74 |
| [GRP-2333](https://grouper.atlassian.net/browse/GRP-2333) | UI shouldn't be adding wildcards for subject id/identifier queries | Shilen | low | Shilen | Done | GRP-2333-remove-wildcard-from-subject-id-query | 9/4/2019 | grouper_v2_4_0_ui_patch_44 |
| [GRP-2316](https://grouper.atlassian.net/browse/GRP-2316) | Fix Deprovisioning email service | Wenlai Wang | low | Vivek | Done | GRP-2316-fix-deprovisioning-email-bug | 10/21/19 | grouper_v2_4_0_api_patch_74 |
| [GRP-2290](https://grouper.atlassian.net/browse/GRP-2290) | On electronics forms, members tab should be clickable and should show members | Chris | low | Vivek | Done | GRP-2290-electronics-forms-members-tab | 10/21/19 | grouper_v2_4_0_ui_patch_44 |
| [GRP-2288](https://grouper.atlassian.net/browse/GRP-2288) | Electronic forms config screen should use the current group as default for approval states json | Chris | low | Vivek | Done | GRP-2288-electronic-forms-default-groupId | 10/21/19 | grouper_v2_4_0_api_patch_74 |
| [GRP-2247](https://grouper.atlassian.net/browse/GRP-2247) | Convert text to text area for attribute assignment values so that long text can be seen properly | Carey Black | low | Vivek | Done | GRP-2247-attribute-assignment-value-edit-textarea | 10/21/19 | grouper_v2_4_0_api_patch_74 |
| [GRP-2220](https://grouper.atlassian.net/browse/GRP-2220) | Members tab should work when editing types on groups screen | James Babb | low | Vivek | Done | GRP-2220-members-tab-types-screen | 10/21/19 | grouper_v2_4_0_ui_patch_44 |
| [GRP-2218](https://grouper.atlassian.net/browse/GRP-2218) | Members tab should update when joining or leaving a group | Carey Black | low | Vivek | Done | GRP-2218-members-tab-update-join-leave-group | 10/21/19 | grouper_v2_4_0_ui_patch_44 |
| [GRP-1870](https://grouper.atlassian.net/browse/GRP-1870) | Attestation job should only try to retrieve email address if subject is of type person | Chris | low | Vivek | Done | GRP-1870-attestation-job | 10/21/19 | grouper_v2_4_0_api_patch_74 |
| [GRP-1841](https://grouper.atlassian.net/browse/GRP-1841) | Attribute names tab should work when on attribute assignments screen or deprovisioning screens | Chris | low | Vivek | Done | GRP-1841-attribute-names-tab-clickable | 10/21/19 | grouper_v2_4_0_ui_patch_44 |
| [GRP-1709](https://grouper.atlassian.net/browse/GRP-1709) | add alternate name to new ui group/folder edit screens. | Shilen / Chris | med | Shilen | Done | GRP-1709-alternate-name-ui | 10/21/19 | grouper_v2_4_0_ui_patch_44, grouper_v2_4_0_ws_patch_8 |
| [GRP-2338](https://grouper.atlassian.net/browse/GRP-2338) | daemon logs should have a way to get back | Chris | low | Shilen | Done | GRP-2338-daemon-logs-breadcrumb | 10/21/19 | grouper_v2_4_0_ui_patch_44 |
| [GRP-2337](https://grouper.atlassian.net/browse/GRP-2337) | daemon page should show a more accurate status | Shilen / Chris | med | Shilen | Done | GRP-2337-daemon-page-overall-status | 10/21/19 | grouper_v2_4_0_ui_patch_46 |
| [GRP-2362](https://grouper.atlassian.net/browse/GRP-2362) | GrouperSystem cant be found | Jeff | low | Chris | Done | master | 10/21/19 | grouper_v2_4_0_api_patch_75 |
| [GRP-2310](https://grouper.atlassian.net/browse/GRP-2310) | Bulk Subject Lookup: Use grouper_members data first | Bert | med | Bert/Shilen | Done | GRP-2310-lazy-bulk-subject-lookup | 10/21/19 | grouper_v2_4_0_api_patch_76 |
| [GRP-2380](https://grouper.atlassian.net/browse/GRP-2380) | Morph was moved from external in grouperClient, adjust those projects | Chris | low | Chris | Done | master | 10/26/2019 | grouper_v2_4_0_api_patch_81 |
| [GRP-2386](https://grouper.atlassian.net/browse/GRP-2386) | Grouper reports missing from diagnostics | Shilen / Chris | med | Shilen | Done | GRP-2386-grouper-reports-diagnostics |  | grouper_v2_4_0_api_patch_81 |
| [GRP-2188](https://grouper.atlassian.net/browse/GRP-2188) | Add Cc, Bcc and Reply-To to GrouperEmail options | Chad | low | Chad | Done | master |  | grouper_v2_4_0_api_patch_75 |
| [GRP-2481](https://grouper.atlassian.net/browse/GRP-2481) | Remember that new groups are selected for provisioning if they are created in selected folders | Oliver Trieu (Vienna) | med | Bert & Jeff | Developed | GRP-2481-group-selection-for-new-groups |  |  |
