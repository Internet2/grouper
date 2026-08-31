---
title: "Grouper style guide"
space: GrIntDev
pageId: 48792966
version: 34
lastUpdated: 2026-08-28T19:37:19.460Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792966/Grouper+style+guide
---

Note: manage wiki contributors [here](https://comanage.at.internet2.edu)

| Item | Description | Example |
| --- | --- | --- |
| New page template when creating new pages | Use the Grouper standard template | Click the ... next to Create at top, and select Grouper standard template |
| Use sentence case | Title and everything should be sentence case   except for proper nouns, acronyms, etc | Grouper style guide |
| I2 products start with capital | Capitalize Grouper, Shibboleth, etc | Grouper |
| Versions look like: v2.6, v2.6.43, v4, v4.5.5+ | Lower case v, plus means that an above | Do not just say 2.6.32 |
| Do not put versions in page title |  |  |
| Links should open in same target | Right click to open in new tab |  |
| Do not link with URL to same wiki and space. | Use the page finder to link the page, then Confluence manages it | When editing the page in the Wiki, there should be a button at the top for 'insert link'. When you push that, it should provide a UI to search for the page to link. You can enter the title of the page, and hopefully it will find it. When you select the right page, it will insert an internal wiki link instead of a URL.   The advantage here is that if someone moves or renames the linked page, its URL will change, but the wiki links auto-update so that you don't get broken links every time someone moves a page (or need to hunt down and manually update every link). |
| Use confluence "share" button to copy links from wiki | Do not post wiki links in training or slack or email with the default confluence titled link, use the "share" obfuscated link | e.g. this is the share button: [Grouper rules EL variables](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549057/Grouper+rules+EL+variables)  this is the titled link: [Grouper rules EL variables](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549057/Grouper+rules+EL+variables)  Note, if the link is in confluence, do neither of these, use the link to a page. This is for slack, other websites, etc. |
| Where it makes sense use the children macro to show children | Automatically lists all the child pages. Don't manually add links to child pages at the top, because it can get out of date |  |
| Where it makes sense, use outline macros to organize page | Macro will pick up the anchor links based on headers |  |
| Hide long content with expand macro | Change the text on the expand macro to be descriptive about what is hidden |  |
| Subheadings should use Heading 2 | If there is a subheading under an H2, then do H3 and so on.   This affects the outline macro |  |
| Consider the value of a screenshot vs. text | Screenshots are not searchable, and require the user to type values by hand instead of being able to copy/paste | An example of rule attributes for inherited privileges on a group. Set these attribute assignments on the parent folder:  Attribute name: etc:attribute:rules:rule  Metadata assignments and values:    \| **Attribute name** \| **Value** \| \| --- \| --- \| \| ruleActAsSubjectId \| GrouperSystem \| \| ruleActAsSubjectSourceId \| g:isa \| \| ruleCheckStemScope \| SUB \| \| ruleCheckType \| groupCreate \| \| ruleThenEnum \| assignGroupPrivilegeToGroupId \| \| ruleThenEnumArg0 \| eduLDAP :::: 800001147 \| \| ruleThenEnumArg1 \| read \| | **Attribute name** | **Value** | ruleActAsSubjectId | GrouperSystem | ruleActAsSubjectSourceId | g:isa | ruleCheckStemScope | SUB | ruleCheckType | groupCreate | ruleThenEnum | assignGroupPrivilegeToGroupId | ruleThenEnumArg0 | eduLDAP :::: 800001147 | ruleThenEnumArg1 | read |
| **Attribute name** | **Value** |
| ruleActAsSubjectId | GrouperSystem |
| ruleActAsSubjectSourceId | g:isa |
| ruleCheckStemScope | SUB |
| ruleCheckType | groupCreate |
| ruleThenEnum | assignGroupPrivilegeToGroupId |
| ruleThenEnumArg0 | eduLDAP :::: 800001147 |
| ruleThenEnumArg1 | read |
| Screenshots have right size | Make sure by default the width of screenshot is correct. e.g. 1000 | Note, if the screenshot doesnt require a lot of width, make the window narrower so it fits on the doc page well |
| Use large red arrows to highlight screenshots |  |  |
| Screenshots should be relatively up to date |  |  |
| Screenshots should show right information | Crop out sidebar if it makes sense  Crop out top header if that makes sense |  |
| Do not upload large attachments (~10M? max) |  | We export our space for previous versions and do not want too big of an export |
| Do not repeat yourself. Link to other wikis or use includes |  |  |
| Identify buttons or labels in double quotes | Use the exact case in button text | e.g. Click "Advanced" to see the "Group filter" input. |
| Include keywords | In title, or at least in body | Confluence prioritizes searches for words in title. e.g. GrouperShell, Grouper shell, GSH |
| Delete incoming links of removed pages | We do not want broken links | When you remove a page in the wiki, confluence will tell you about the broken incoming links. Make sure to go to all those pages and remove the links so we do not have broken links |
| Use labels for wiki lists of non-children | Use convention of grouper-labelname-wiki | Use the labels list macro, sort by name. |
| Use standard labels | [needsdocupdate](https://spaces.at.internet2.edu/dosearchsite.action?cql=space%20in%20(%22Grouper%22)%20AND%20type%20in%20(%22space%22%2C%22user%22%2C%22com.atlassian.confluence.extra.team-calendars%3Acalendar-content-type%22%2C%22attachment%22%2C%22com.atlassian.confluence.extra.team-calendars%3Aspace-calendars-view-content-type%22%2C%22page%22%2C%22blogpost%22%2C%22com.k15t.scroll.scroll-platform%3Ascroll-search-proxy-content-type%22)%20AND%20label%20in%20(%22needsdocupdate%22)&includeArchivedSpaces=false): wiki page needs updating  [needsmove](https://spaces.at.internet2.edu/dosearchsite.action?cql=space%20in%20(%22Grouper%22)%20AND%20type%20in%20(%22space%22%2C%22user%22%2C%22com.atlassian.confluence.extra.team-calendars%3Acalendar-content-type%22%2C%22attachment%22%2C%22com.atlassian.confluence.extra.team-calendars%3Aspace-calendars-view-content-type%22%2C%22page%22%2C%22blogpost%22%2C%22com.k15t.scroll.scroll-platform%3Ascroll-search-proxy-content-type%22)%20AND%20label%20in%20(%22needsmove%22)&includeArchivedSpaces=false): wiki page needs move, not sure where  [needsarchive](https://spaces.at.internet2.edu/dosearchsite.action?cql=space%20in%20(%22Grouper%22)%20AND%20type%20in%20(%22space%22%2C%22user%22%2C%22com.atlassian.confluence.extra.team-calendars%3Acalendar-content-type%22%2C%22attachment%22%2C%22com.atlassian.confluence.extra.team-calendars%3Aspace-calendars-view-content-type%22%2C%22page%22%2C%22blogpost%22%2C%22com.k15t.scroll.scroll-platform%3Ascroll-search-proxy-content-type%22)%20AND%20label%20in%20(%22needsarchive%22)&includeArchivedSpaces=false): wiki page does not belong, could be deleted | Also add a comment to the page which describes what needs updating specifically |
| Be concise | Pages are read by people and by AI assistants. Cover the topic fully, then stop. Say a thing once, and prefer lists and tight prose over long paragraphs. AI assistants tend to over-write, so trim what they draft. | Full information, minimal words |
| Long code or config goes in an attachment | More than roughly two dozen lines -- a whole properties file, a full GSH script, a complete provisioner config -- should be an attachment rather than a code macro. Keep the few lines that matter inline and describe what the rest does. Short snippets stay inline. | Attach the whole grouper-loader.properties, and show inline only the `provisioner.myProv.*` lines the page is explaining |
| Describe every diagram and image in text | Pages are converted to markdown for AI assistants, and images do not survive that conversion. Every diagram and screenshot needs prose beside it carrying the same information: the boxes, the arrows, the order of the steps, the values shown. If the text alone does not convey the point, the page is unusable for an AI reader. This is in addition to attaching the diagram source (.svg, .drawio) next to the embedded image. | After a provisioning flow diagram, list the steps in order: Grouper loader reads the SQL source, the change log picks up the membership add, the provisioner writes to the target LDAP |
| Label community contributions | If a page describes something an institution built on top of Grouper -- a GSH template, a script, a config pattern, a custom provisioner -- say so near the top of the page. It is not built-in Grouper functionality; it is a way to use the Grouper platform to accomplish a task. The adopting institution implements the configuration or code itself and is responsible for maintaining it. Use an info or note panel so it cannot be missed. | > This is a community contribution, not built-in Grouper functionality. It is a way to use the Grouper platform to accomplish a task. An institution adopting it implements the configuration or code itself and is responsible for maintaining it. |

## Grouper documentation rubric to determine if a wiki page needs to be updated

- Adheres to style guide
- Diagrams and screenshots are explained in text, and long code or config is attached rather than pasted inline
- Is accurate to the advertised version
- Are required privileges described
- Identify which version this feature was introduced (or when it ended)
- Wiki page should be updated (even if trivial update) in the last several years
- Search for the page using various terms and make sure the page is in the results. Adjust the title or body text to fix
