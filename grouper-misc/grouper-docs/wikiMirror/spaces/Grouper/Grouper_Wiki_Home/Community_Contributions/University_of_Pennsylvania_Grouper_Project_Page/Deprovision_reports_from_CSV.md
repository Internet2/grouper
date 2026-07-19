---
title: "Deprovision reports from CSV"
space: Grouper
pageId: 28544582
version: 6
lastUpdated: 2026-07-01T05:48:23.025Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544582/Deprovision+reports+from+CSV
---

Penn uses Slack. We dont have an integration to sync to penngroups. But we want to deprovision. This is how we do it:

## Individual

When the Slack service manager is notified of someone leaving penn, they deactivate the account.

## Periodic reports deprovisioning process

1. Get the CSV of users in slack
2. Copy the emails into the email col e.g. in this sheet
3. Filter by status. Take out "bot" and "Deactivated"
4. Copy the last col of netIds
5. Import into the correct group in grouper[https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemId=8a24828cf70a4a91b85350933702b8fd](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemId=8a24828cf70a4a91b85350933702b8fd)(see below for visualization)
6. Look at the deprovisioning groups to see which accounts to disable (this takes manual discretion)
  
  1. Note that ISC (IT dept) has two groups, one of accounts which arent active, one which arent ISC people (be careful because some are legit, who are allowed to have accounts but are in other depts... e.g. for joint projects)

## Import users into penngroups, replace existing users

Go to slack groups:

[https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=penn:isc:nandt:apps:slack:service:policy](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemName=penn:isc:nandt:apps:slack:service:policy)

Import list of pennkeys

Manually look at entries that dont resolve, add them to the netidLookup sheet

| `Error on row``48``. Problem finding entity:``"something"`   `Error on row``105``. Problem finding entity:``"another` |
| --- |

## Excel sheet

The problem is the account in slack is created by email address, and people dont have to use [netId@example.com.](mailto:netId@example.com.) So...

Setup a new sheet like this:

Make a col called "netId" (col J) with formula:

| `=MID(B2,``1``,FIND(``"@"``, B2)-``1``)` |
| --- |

Make a col called netidTranslated (col K) with formula:

| `=IFERROR(VLOOKUP(J2, netidLookup!$A$``2``:$B$``200``,``2``, FALSE),``"Not Found"``)` |
| --- |

Make a col named netidOrTranslated (col L)

| `=IF(K2 =``"Not Found"``, J2, K2)` |
| --- |

The excel sheet does two things:

1. Gets the email prefix into a col by itself
2. Translates email prefixes to netIds for ones which dont match (90% of them match and this isnt needed)

## Visualization of groups
