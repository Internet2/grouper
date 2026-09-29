---
title: "Penn Atlassian licensing front door"
space: Grouper
pageId: 234323970
version: 4
lastUpdated: 2026-09-21T17:30:04.619Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/234323970/Penn+Atlassian+licensing+front+door
---

Penn's central IT (ISC) runs an Atlassian Cloud Jira and Confluence tenant. Licenses used to be granted automatically and held indefinitely. A Grouper front door replaced that: every license is one the person requests for themselves from a GSH template screen, and every license expires after 30 days unless the person is actually using the product.

 It went live on 2026-09-21. Licenses held fell from 726 to 407 in one day, the subscription was cut from 500/600 seats to 300/400, and the annual cost dropped about 28%, or roughly $12,000 a year. Nobody was identified or deactivated by hand.

 This page is the design, the numbers, and the source. It replaces the earlier plan on [Grouper managing cloud licenses to reduce costs](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544367/Grouper+managing+cloud+licenses+to+reduce+costs), which describes the same problem before it was built.

 

## Penn terms used on this page

 

| Term | Means |
| --- | --- |
| PennGroups | Penn's Grouper deployment. |
| WebLogin | Penn's Shibboleth IdP. |
| ISC | Penn's central IT organization, which owns this Atlassian tenant. Wharton, Nursing and the Library share the tenant's SSO but license on their own subscriptions, and are excluded from every figure here. |
| Two-step | Duo MFA enrollment. |

 

## What "front door" means here

 A front door is a Shibboleth intercept driven by group membership: before the IdP releases an assertion to the service provider, it checks whether the person is in a Grouper group, and if not it sends them somewhere else instead of to the application. The general pattern is described on [Grouper and Shibboleth front door authorization](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545521/Grouper+and+Shibboleth+front+door+authorization).

 The usual front door denies: someone who is not eligible gets an error page. This one redirects instead. Someone who is not in the marker group is sent to a Grouper GSH template screen where they can register, claim a license, or give one up. Once they submit, the marker is written and the intercept never fires for them again. So the same mechanism that normally says "no" is being used to say "here is the self-service page, and you arrived at the moment it is relevant".

 That matters for adoption. The screen does not have to be found, advertised or bookmarked: it is served by the IdP at exactly the moment somebody clicks a Jira link they cannot open.

 **If you cannot do the intercept**, the fallback is a static HTML "jump page": a plain landing page linking to the Atlassian product and to the Grouper claim screen, so somebody who is bounced has somewhere to go. It is needed because Atlassian's own access-denied message cannot be customised and its access-request queue collects tickets nobody reads. The jump page has to be found rather than served, so expect to advertise and bookmark it -- but it needs nothing from the IdP.

 

## Results

 

| Product | Purchased before | Purchased after | Held before cutover | Held after cutover | Released | Monthly active users |
| --- | --- | --- | --- | --- | --- | --- |
| Jira | 500 | 300 | 310 | 171 | 139 | 173 |
| Confluence | 600 | 400 | 416 | 236 | 180 | 234 |
| Total | 1,100 | 700 | 726 | 407 | 319 |  |

 Annual cost went from $42,385 to roughly $30,000, a saving of about 28%, or roughly $12,000 a year. Atlassian prices in declining bands, so the seats shed are the cheapest ones and the saving is smaller than a per-seat average would suggest. Licenses are also sold in bands, so 300 and 400 are the available step down rather than numbers chosen to match usage.

 Licenses in use are now the monthly active users by construction: the cutover kept a license only where the product had been used in the last 30 days, and the 30-day term holds it that way. 568 people are eligible to claim a Jira license and 627 a Confluence license, against 171 and 236 held -- eligibility costs nothing, only a claimed license does.

 The chart below is the position *before* the change, and is the argument for doing it. Three counts per product, each well below the one before it: Jira 500 purchased, 309 in use through PennGroups, 160 monthly active users; Confluence 600 purchased, 418 in use through PennGroups, 230 monthly active users. Jira was the larger exposure -- 69% of the cost on 45% of the licenses, and the only one of the two whose usage was declining (roughly 180 down to 160 active users over two years, while Confluence held flat). Neither product was growing, so the held licenses were not headroom for expansion. 191 Jira and 182 Confluence licenses were being paid for and held by nobody before any policy change at all.

 

 

## How the front door works

 

 The diagram shows four steps in a loop between the person, PennGroups and Atlassian Cloud:

 

1. **A person opens an Atlassian link.** If they have not been through the front door, WebLogin sends them to it before they reach Atlassian at all. Atlassian's own access-request queue is turned off, so the front door is the only route and nothing collects in a queue nobody reads.
2. **They choose what they need** on the PennGroups screen: Jira, Confluence, or both, limited to what they are eligible for.
3. **PennGroups grants a 30-day license**, which the SCIM provisioner carries to Atlassian Cloud.
4. **Usage renews it automatically.** Atlassian reports per-product activity back to PennGroups each morning. Anyone who used the product has their term extended without being asked anything; anyone who did not is released at day 30 and the seat returns to the pool.

 Step 4 is what makes a 30-day term workable. Renewing by hand is only ever asked of people who have stopped using the product, and for them it takes seconds.

 

## What people see

 

 The diagram traces the decision: somebody opens an Atlassian link and reaches WebLogin. If they hold the front door marker they pass straight through to Atlassian. If it is their first time, or the marker has been cleared, they get the sign-in screen. That screen always shows one question -- "Sign in with your Penn account" -- and shows two more only if the person is eligible: "ISC Jira license" and "ISC Confluence license". Submitting writes the account and any license changes, then tells them to go back and click the Atlassian link again, because WebLogin does not know where they were headed.

 An eligible person sees three rows. Each row is pre-selected to what they hold right now, and the help text under it says which, so a row left alone changes nothing:

 

 In that screenshot the person holds a Confluence license (row set to True, "you have an ISC Confluence license, choose False to give it up") and not a Jira one (row set to False, "you do not have an ISC Jira license, choose True to claim one"). Somebody whose real work sits elsewhere at Penn can decline the ISC license so ISC stops paying for a seat they never open.

 Somebody with no ISC eligibility sees one row, already set correctly, and one button:

 

 No license rows appear, nothing is declared, and submitting provisions their account into the correct Atlassian authentication policy and returns them to sign in again. No ISC license is consumed and no ticket is opened. If ISC eligibility arrives later, their next visit offers the new products to take or ignore -- nothing is ever granted on their behalf.

 

## Group structure

 ISC Jira is shown. Confluence is the same structure with its own groups: substitute `confluence` for `jira` in every name.

 

 The diagram has four bands, matching the four sections below: signing in; who may claim; the license itself; and keeping it.

 

### 1. Signing in

 WebLogin sends anybody without the marker to the sign-in screen. Submitting writes both groups; the account group then provisions to Atlassian by SCIM.

 

- `penn:isc:ait:apps:atlassian:helperGroups:atlassianCloudSsoManagedUsers` -- **the account**. Add only. Removing somebody deletes their Atlassian account across every Penn instance, not just ISC's, so nothing removes from it.
- `penn:isc:ait:apps:atlassian:helperGroups:atlassianFrontDoorAllowed` -- **the marker** WebLogin checks. Nested nowhere and grants nothing, so clearing it costs somebody one screen and no access.

 Keeping those two apart is the single most important decision on this page. If they were one group, every routine "show this person the screen again" would be an account deletion affecting other schools' tenants.

 

### 2. Who may claim a license

 Two independent tests, intersected. Permission alone is not enough: the license composite would refuse a claim from somebody missing a factor, so the screen does not offer it.

 

- `...:groupsJira:jira-users` -- a loader group holding every other group in `groupsJira`. It contains groups, never people; anybody added directly is removed at the next sync.
- `...:helperGroups:jiraHasGroupAndActive` -- `jira-users` intersected with the active-non-alumni reference group.
- `...:helperGroups:jiraHasGroupAndActiveAndTwoStep` -- the above intersected with the two-step (Duo) reference group.
- `...:helperGroups:jiraAllowedToClaimLicense` -- who is permitted to claim. Maintained by hand, with reference groups nested in. This is the group to add somebody to.
- `...:helperGroups:jiraAllowedToClaimAndEligible` -- the intersection of the previous two, and exactly what the screen reads to decide whether to show the Jira row. Derived, so do not edit it.

 

### 3. The license

 A claim is written to the self-claim group, but only becomes a license once the eligibility factors also hold.

 

- `...:helperGroups:jiraHaveSelfClaimedLicense` -- the claim. Written by the screen, by an admin, or removed by the daemon when it goes stale. Nothing else writes it.
- `...:helperGroups:jiraAutomaticLicense` -- a license by role, with no claim and no expiry. Empty since the changeover but still wired, so a group can be dropped in to grant licenses without claiming.
- `...:helperGroups:jira_has_license_pre_eligibility_check` -- the two above, combined.
- `...:groupsJira:jira_has_license` -- **the license itself**, and what provisions the seat by SCIM. The previous group intersected with the eligibility factors, so losing two-step withdraws the license without touching the claim, and regaining it restores the license on its own.

 

### 4. Keeping it, or losing it

 A license survives on use. Everything here is measured per product, so using Confluence does not keep a Jira license alive.

 

- `...:helperGroups:jiraUsedLast30Days` -- rebuilt each morning by the daemon from Atlassian's per-product last-active dates, counting ISC's site only. Do not edit by hand; it is replaced on every run.
- `...:helperGroups:jiraAllowedToKeepClaimedLicense` -- a SQL loader worklist: holds a claim, and either used the product in the last 30 days or claimed it within the last 30. Automatic license holders are deliberately absent and lose nothing by it, since they have no claim to go stale.

 Holding a claim but being absent from that worklist means the daemon removes the claim, records it in the audit table, and clears the front door marker so the screen offers the license again to anybody still eligible. Reclaiming a license from somebody who wants it and never offering it back is just breaking their access.

 

## Where the usage data comes from

 Two signals, at different levels of detail.

 **IdP sign-ins are useless for this.** Atlassian publishes a single SAML entity for the whole tenant, so a sign-in only shows that somebody entered Atlassian, not which product they used. In a recent 30-day window that entity saw 7,424 sign-ins from 918 distinct people against 407 ISC licenses, because most of that traffic is people on other tenants or holding no ISC license at all.

 **Per-product usage comes from the Atlassian Organizations REST API.** A PennGroups daemon reads it each morning: the API lists each account with the products it can reach, the site for each, and the date it was last active. Two filters make each usage group mean "holds an ISC license AND used ISC's instance recently":

 

- **Site.** Only `product_access` entries whose url is ISC's site count. Without this a Nursing person's activity on Nursing's Confluence would renew an ISC license they never touched.
- **License.** The person must already be in that product's `*_has_license` group. Recent usage by somebody with no ISC license is real but irrelevant, and is counted rather than dropped silently.

 Product keys are collapsed into families -- anything starting with `jira` (jira-software, jira-servicedesk, jira-core, jira-product-discovery) counts as Jira. A product entry with no `last_active` means the license was granted but never used, and is deliberately not counted as usage: that case is the entire point of the exercise.

 One limit worth knowing: Atlassian sets the last-active date on *access*, not on doing work, and reports it by date rather than time. The measure is "opened the product this month", not "was productive in it". That is the right test for whether a seat is worth paying for, but it is not an activity metric.

 

## Reprompting when eligibility changes

 An hourly job clears the front door marker for anyone who has become eligible for a license they do not hold, so the screen appears and offers it. Without this, eligibility gained after somebody's first visit is invisible to them forever: they are already past the front door and would never see it again.

 "Newly" is judged from the audit table plus point-in-time data, not from a new attribute: somebody is newly eligible when their `AllowedToClaimAndEligible` membership started *after* their last "front door submitted" audit row. Decline and your submit post-dates your eligibility, so you are left alone until eligibility actually changes; accept and you hold the license, so you drop out anyway. Self-limiting, and it is why nothing may ever purge old rows from the audit table.

 Every event goes in that table: the screen being shown and submitted, an account being registered, a license being claimed or lost. Each row says whether it came from the screen or from the daemon, which is what distinguishes somebody giving a license up from one being reclaimed for inactivity. History before the front door was backfilled from Grouper's point-in-time data.

 

## External accounts, and why this removed tickets

 ISC has claimed the `@upenn.edu` domain, so every Atlassian account at Penn is managed centrally in ISC's organization -- including accounts of people who only ever use a non-ISC tenant such as Wharton, Nursing or the Library. For those people to authenticate to their own tenant they have to be in the correct Atlassian authentication policy, and only ISC can put them there.

 Each case used to be a ticket: someone in ISC provisioned the account, worked out the right policy, and reported back. The front door automates it, and nobody has to declare themselves external -- somebody with no ISC eligibility simply sees the sign-in question with no license rows, and submitting provisions the account, places them in the right policy, and returns them to their own tenant.

 The daemon also sweeps for this independently, because new Atlassian accounts land in the org's *default* authentication policy, which is not the one Penn wants, and nobody notices until the person cannot sign in and opens a ticket. The sweep is scoped by `endsWith("@upenn.edu")`, which is precisely what excludes school subdomains -- those schools administer their own people, and ISC re-homing their authentication policy would be overreach.

 

## Risks, and what we did about them

 

| Risk | Mitigation |
| --- | --- |
| Losing data when a license lapses | Nothing is lost. The account is never deactivated -- only product access lapses -- and pages, issues, comments, authorship and permissions all survive a revoke and re-claim. Recovery takes seconds, which is what makes a 30-day term safe. |
| Permissions granted to named individuals rather than groups | Group membership is re-asserted automatically when a license returns; a grant made directly to a named person in Atlassian sits outside Grouper and has no record there. Audit Confluence space permissions and Jira project roles for individual grants alongside this change. |
| A scheduled job silently stops | Licenses now depend on a daily daemon, an hourly one, two SQL loaders and three SCIM provisioners. A silent stop shows up slowly -- licenses quietly not renewing -- which is exactly the failure nobody reports for weeks. All eight jobs are monitored and a failure emails a human. A provisioner failure breaks access within minutes; a daemon not running is a degradation measured in days. |
| Accounts behind automations | No exemptions. Anyone running an automation under their own account keeps the license by signing in monthly like everybody else, so an automation whose owner has left stops within 30 days rather than running unattended forever. |
| Running out of licenses | Cutting the purchased count removes the slack that hid demand, and the failure mode -- an eligible person told there is no license to give -- is a worse ticket than the ones this removes. The daily job emails when either product reaches 95% of purchased (285 of 300 Jira, 380 of 400 Confluence) and repeats daily until seats are bought or usage falls back. 95% is deliberately late: seats can be bought in days, and a threshold that fires routinely gets ignored. |
| Usage data regression looks like "nobody used Jira" | Two guards. The usage group sync refuses to run when computed membership is under half the current membership. License reclaim refuses when a run wants to take more than 20% of a self-claim group. Both fail the run without touching anybody, because a broken loader looks identical to a mass abandonment. That loader demonstrably can break -- it carried a hardcoded 2023 timestamp for three years. |
| The worklist is always a day stale | The reclaim step spares anybody the same run measured using the product, not just the worklist. Union only ever spares, so it can never reclaim somebody the worklist would have kept -- without it, a person returning after a 30-day gap would be reclaimed on the morning they came back. |
| Auth-policy sweep goes wrong | A run that wants to move more than 100 accounts fails without moving anyone. That many means the target policy id changed or a policy was recreated, and a human should look before hundreds of accounts are re-homed. |

 

## Implementation notes

 Both the screen and the daemon are **compiled Java** GSH templates (`templateMode = compiled`), not Groovy. The full `.java` source is pasted into the `gshTemplate` property and compiled at runtime by javac.

 Points that cost time and are worth knowing before you build one:

 

- **The simplified UI needs a folder.** `simplifiedUi = true` renders the template on its own page with no folder or group chrome, which is what makes it usable as a front door. But `showOnFolders` must be true and the URL must carry `stemName`: with neither a `stemName` nor a `groupName`, the handler returns silently and the page renders completely **empty**, with no error. EveryEntity needs `stemView` on that folder or the page is blank for everyone but its admins.
- **Security is unusual, and deliberately so.** `securityRunType = everyone` plus `runAsType = GrouperSystem`. Anyone who can authenticate must be able to reach this, including people with no license and no Grouper privileges at all -- gating it would hand an ineligible person Grouper's "not authorized" page, which tells them nothing when they arrived from a Jira link and have never heard of Grouper. Because that combination runs as root for any authenticated person, the run logic re-checks eligibility server side rather than trusting the posted form.
- **Never set `defaultValue` on a boolean input you pre-select per person.** Grouper renders a boolean radio as True/False -- unless a default is set, in which case it silently adds a *third* pre-selected option labelled "Default value (X)" which posts an empty string. That broke this screen in testing: somebody holding a license who agreed with the text and pressed Submit without touching anything sent no value, which read as "I do not want it" and gave their license away. Set the input's *value* from the decorator instead, which is what decides which radio is checked.
- **`templateVersion = V2` is required for the Test button** even though `templateVersion` is otherwise a Groovy-only axis that compiled mode ignores. The test executor refuses outright without it, and the default is V1, so omitting it silently disables every test in the class.
- **`runGshInTransaction` must stay false.** The write path adds the membership then waits for the change log to carry it to the provisioner; inside a transaction the change log would not see the write until after the wait had already timed out.
- **Two schedules, one template body.** The daily job (steps 1-3, about 6 minutes and 257 API calls, nearly all of it paging the org user list) and the hourly reprompt job (no Atlassian calls at all) share one `gshTemplateConfigId`, so the Java is pasted into config exactly once and the two can never drift. The Java tells them apart by the loader-log job name, `OTHER_JOB_<otherJob key>` -- so renaming the hourly job *fails silently*: nothing errors, the hourly run just behaves like the daily one and never runs the reprompt step.
- **SCIM cannot do org administration.** Authentication policies live on a different base path (`https://api.atlassian.com/admin/...`) with a different credential -- an org API key from the admin console -- so this needs a fourth external system beside the three SCIM ones. Do not reuse a SCIM external system even for listing accounts: the SCIM `/Users` resource id is directory-scoped, not the org-level accountId, and the auth-policy endpoints answer HTTP 200 with an empty array when given one. A silent no-match rather than an error.
- **Every step has an enabled switch and a dry-run switch**, both echoed onto the loader-log row, so a run's behaviour is readable from the log alone. A disabled step reports `skippedStepDisabled` so it can never look like a clean run. The read path was verified over four dry runs before the first write.

 

## Attachments

 

| File | What it is |
| --- | --- |
| atlassianFrontDoor.java | The GSH template: the screen, its per-person decoration, and the write path. |
| atlassianDaemon.java | The daemon: auth-policy sweep, usage groups, license reclaim, reprompt, capacity alert. |
| atlassian.grouper.properties | Both GSH template configs. The `gshTemplate` property holds the full Java source of the matching attachment. |
| atlassian.grouper-loader.properties | The two daemon schedules and the Atlassian org-admin external system. |

 Hostnames in the attachments are genericised to `grouper.server.institution.edu`. Group names are left as Penn's real ones so the comments read sensibly -- substitute your own folder.
