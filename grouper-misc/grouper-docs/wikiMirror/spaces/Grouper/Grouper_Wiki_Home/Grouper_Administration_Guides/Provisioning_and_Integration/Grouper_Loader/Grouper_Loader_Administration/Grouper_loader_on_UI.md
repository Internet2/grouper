---
title: "Grouper loader on UI"
space: Grouper
pageId: 28554452
version: 13
lastUpdated: 2026-07-12T15:27:09.370Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554452/Grouper+loader+on+UI
---

The [Grouper loader](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545200/Grouper+Loader) is in the UI in v2.3 (fully patched) and above. Some features were in the UI in previous versions (e.g. editing raw attributes, scheduling the loader job, running the loader job).

### Grouper loader screens

- [View grouper loader settings](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559981/Grouper+loader+on+UI+view)
- [View grouper loader logs](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560114/Grouper+loader+logs)
- [View grouper loader diagnostics](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560110/Grouper+loader+diagnostics)
- [Edit loader settings](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560222/Edit+Grouper+loader+settings)
- [Grouper loader overall](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560370/Grouper+loader+on+UI+overall)

To access the loader settings for a job, pull up the group in the New UI, and click the tab:

These settings control the loader UI

```
###################################
## V2 UI loader settings
###################################

# put in a group here if you want to restrict the loader tab to certin users.  
# note, grouper sysadmins can always see the tab 
uiV2.loader.must.be.in.group = 

# if group admins can see the loader tab
uiV2.loader.view.by.group.admins = true
```

In order to see the Grouper loader menu item one of the following must be true

- User is a grouper sysadmin
- [uiV2.loader.must.be.in](http://uiV2.loader.must.be.in).group is blank and [uiV2.loader.view.by](http://uiV2.loader.view.by).group.admins is true, and the user is an admin of the group
- [uiV2.loader.must.be.in](http://uiv2.loader.must.be.in/).group is set to a group and the user is a member of that group, and the user can view the loader group
