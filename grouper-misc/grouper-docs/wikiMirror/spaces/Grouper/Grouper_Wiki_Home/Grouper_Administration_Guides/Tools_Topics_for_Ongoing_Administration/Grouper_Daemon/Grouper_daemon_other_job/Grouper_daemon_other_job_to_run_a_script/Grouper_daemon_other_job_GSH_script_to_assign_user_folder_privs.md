---
title: "Grouper daemon \"other job\" GSH script to assign user folder privs"
space: Grouper
pageId: 28560177
version: 3
lastUpdated: 2026-07-01T05:36:07.188Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560177/Grouper+daemon+other+job+GSH+script+to+assign+user+folder+privs
---

This daemon will take a folder and assume all subfolders are subject ids. It will make sure the subject has ADMIN on the folder and inherited group ADMIN on all groups directly in the folder.

The folder name and subject source ID need to be adjusted at the top of the script

In this example GrouperSystem is used as a test, but imagine normal users with the folder names

```
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.PrivilegeGroupInheritanceSave;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

//public class Test75assignPrivilegesToFoldersScriptDaemon {
//
//  public static void main(String[] args) {
    
    String folderName = "basis:BCLDAP:groups:personal";
    String subjectSourceId = "g:isa";
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    Stem parentFolder = StemFinder.findByName(folderName, true);
    
    Set<Stem> userFolders = new StemFinder().assignParentStemId(parentFolder.getId()).assignStemScope(Scope.ONE).findStems();
    
    int userCountNotFound = 0;
    int userCountFolderPrivilegesChanged = 0;
    int userIndex = -1;
    
    for (Stem userFolder : userFolders) {
      
      userIndex++;
      
      String username = userFolder.getExtension();
      
      Subject user = SubjectFinder.findByIdAndSource(username, subjectSourceId, false);
      
      if (user == null) {
        userCountNotFound++;
        if (userIndex < 10) {
          debugMap.put("subjectIdNotFound_" + userIndex, username);
        }
        continue;
      }
      
      // assign admin on the folder
      boolean grantedFolderPriv = userFolder.grantPriv(user, NamingPrivilege.STEM_ADMIN, false);

      if (grantedFolderPriv) {
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(1);
      }
      
      SaveResultType saveResultType = new PrivilegeGroupInheritanceSave().assignStem(userFolder).
          assignStemScope(Scope.ONE).assignSubject(user).addPrivilege(AccessPrivilege.ADMIN).save();
      
      if (saveResultType == SaveResultType.INSERT) {
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(1);
      }
      if (saveResultType == SaveResultType.UPDATE) {
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addUpdateCount(1);
      }
      
      if (grantedFolderPriv || saveResultType == SaveResultType.INSERT || saveResultType == SaveResultType.UPDATE) {
        userCountFolderPrivilegesChanged++;
        if (userIndex < 10) {
          debugMap.put("userFolderPrivsChanged_" + userIndex, username);
          continue;
        }
      }
      
    }
    
    debugMap.put("userCountNotFound", userCountNotFound);
    debugMap.put("userCountFolderPrivilegesChanged", userCountFolderPrivilegesChanged);
    
    if (OtherJobScript.retrieveFromThreadLocal() != null) {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));
    }
    
    if (userCountNotFound > 0) {
      throw new RuntimeException("User count not found: " + userCountNotFound);
    }

//  }
//
//}

```
