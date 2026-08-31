---
title: "Creating a policy group (run template)"
space: Grouper
pageId: 28545375
version: 4
lastUpdated: 2026-07-01T05:47:18.342Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545375/Creating+a+policy+group+run+template
---

Using the "run template" function of a folder to create a policy group.

1. Navigate to empty target folder
2. Expand “Folder Actions” menu on right
3. Select “Create folder”
4. Fill out form to name folder and click "save"
5. When in the new folder select “Folder actions” and “Run Template” (do not do this in your directory provisioning folders as this will generate multiple groups that should not be populated in the directory)
6. Select the type of template type  
     
    
  **Policy group**creates a composite that with “allow” and “deny” groups to manage final group and is the type most users will need  
  **Application**is for Grouper internal security management  
  **Grouper Deployment Guide structure** is not generally used by the average user
7. After selecting “policy group” you will create a “key” (name) for the root of the group then click “next”
8. Grouper will ask if you want to create “allow” and “deny” ad hoc groups (if you select to create them now you can always delete them later, creating them later is more difficult)
9. Click “Next”
10. Group structure that is created:
11. To have the policy group populated with the final desired group add groups to the “allow” group (if using ad hoc add users to the “allow_manual” group) and the groups you want deny access add to the “deny” (if using ad hoc add users to the “deny_manual” group).
12. You can then use the “policy” group to populate a group that is getting passed to the directories or as a factor in another group if desired.
