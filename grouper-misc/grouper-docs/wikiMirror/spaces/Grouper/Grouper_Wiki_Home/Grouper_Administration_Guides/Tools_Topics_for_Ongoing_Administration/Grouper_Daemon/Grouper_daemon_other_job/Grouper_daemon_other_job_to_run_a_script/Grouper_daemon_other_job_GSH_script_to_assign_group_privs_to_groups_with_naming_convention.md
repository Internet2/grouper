---
title: "Grouper daemon \"other job\" GSH script to assign group privs to groups with naming convention"
space: Grouper
pageId: 28560185
version: 2
lastUpdated: 2026-07-01T05:36:06.194Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560185/Grouper+daemon+other+job+GSH+script+to+assign+group+privs+to+groups+with+naming+convention
---

This daemon will take two folders: a folder with groups, and a folder with admin groups. It will make sure the admin groups that match a naming convention will be ADMINS of the group they match

The folder names and admin group suffix need to be adjusted at the top of the script

```
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

//public class Test78assignGroupPrivilegesToGroupsScriptDaemon {
//
//  public static void main(String[] args) {
    
    String folderNameOfGroups = "basis:BCLDAP:groups:admin";
    String folderNameOfAdminGroups = "basis:permissionGroups:admin";
    String adminGroupSuffix = "_admins";
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    // make sure folder exists
    StemFinder.findByName(folderNameOfGroups, true);
    
    Stem parentFolderOfAdminGroups = StemFinder.findByName(folderNameOfAdminGroups, true);

    Set<Group> adminGroups = new GroupFinder().assignParentStemId(parentFolderOfAdminGroups.getId()).assignStemScope(Scope.SUB).findGroups();
    
    debugMap.put("adminGroupsCount", adminGroups.size());
    
    int groupIndex = -1;
    
    // loop through admin groups and find the group
    for (Group adminGroup : adminGroups) {

      groupIndex++;

      String name = adminGroup.getName();
      
      // take off the admin group prefix
      name = GrouperUtil.prefixOrSuffix(name, folderNameOfAdminGroups + ":", false);

      // take off the admin group suffix
      if (name.endsWith(adminGroupSuffix)) {
        name = name.substring(0, name.length() - adminGroupSuffix.length());
      } else {
        GrouperUtil.mapAddValue(debugMap, "adminGroupCountWithoutAdminSuffix", 1);
        if (groupIndex < 10) {
          debugMap.put("adminGroupWithoutSuffix_" + groupIndex, adminGroup.getName());
        }
        continue;
      }
      
      // see if there is a corresponding group
      String groupName = folderNameOfGroups + ":" + name;
      Group group = GroupFinder.findByName(groupName, false);
      
      if (group == null) {
        GrouperUtil.mapAddValue(debugMap, "groupNotFindCount", 1);
        if (groupIndex < 10) {
          debugMap.put("missingGroup_" + groupIndex, groupName);
        }
        continue;
      }
      
      boolean assigned = group.grantPriv(adminGroup.toSubject(), AccessPrivilege.ADMIN, false);
      
      if (assigned) {
        if (groupIndex < 10) {
          debugMap.put("groupAssignedPrivs_" + groupIndex, groupName);
        }
        GrouperUtil.mapAddValue(debugMap, "privilegesAssigned", 1);
        if (OtherJobScript.retrieveFromThreadLocal() != null) {
          OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addUpdateCount(1);
        }
      }
    }
    
    if (OtherJobScript.retrieveFromThreadLocal() != null) {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));
    }

//  }
//
//}

```
