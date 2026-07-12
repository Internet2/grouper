---
title: "Grouper bug - GRP-5107 - authentication bypass remediation"
space: Grouper
pageId: 28555274
version: 5
lastUpdated: 2026-07-01T05:38:37.765Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555274/Grouper+bug+-+GRP-5107+-+authentication+bypass+remediation
---

If you are running an affected version and configuration, this will remediate the issue.

If you make these changes, you do not need to upgrade or patch your environment, but if you are in a position to, it would be best to do that as well when the new release is out.

If you have questions, then send a direct slack message to Chris Hyzer, the project lead ([request to join InCommon slack](https://incommon.org/help/)). If you are not in InCommon slack then email [mchyzer@yahoo.com](mailto:mchyzer@yahoo.com).

## How to protect your Grouper deployment

This procedure does not require an upgrade or patch and does not require downtime. This is a summary, the details are in this document below.

- Make sure your subject source(s) which have Grouper WS authenticated subjects are correct
- Add a group of web service users (if not already exists)
- Load that group with valid users automatically (or have a process for populating the group)
- Add a script daemon with provided source
- The day before we release the CVE we will provide you with confidential instructions on how to test your environments to make sure they are protected.

## Subject source

This fix assumes your subject source for WS credentials is configured correctly. Specifically, you need to make sure that any subject identifier that you are searching or filtering for is also listed as a subject identifier for Grouper to index in the grouper_members table. If you are only using groups or local entities as subjects for WS credentials, then skip to the next section.

Look at your filter or query in your subject source to find subjects by identifier. Here is an example for SQL:

```
subjectApi.source.jdbc.search.searchSubjectByIdentifier.param.sql.value = select    {cols}  where  a.name='loginid' and s.subjectid = a.subjectid and {inclause}
```

Note: if you have case insensitive subject id or identifier searching then contact Chris Hyzer for a fix, this document will not protect you.

The subject attribute is "loginid". See if the columns are populated in the grouper members table:

If the identifier is in one of those columns you are good. Otherwise you need to make a change like this to your subject source (the 0 is the index and should not conflict with another index)

```
subjectApi.source.jdbc.param.subjectIdentifierAttribute0.value = loginid
```

After you change that, run the USDU daemon, and the query above should return results

## WS authentication

If you have these: grouper.is.ws.basicAuthn = true or GROUPER_WS_GROUPER_AUTH=true please proceed with the adjustment ASAP. Note if you have this set in grouper-loader.properties discuss with Chris Hyzer: default.subject.source.id

1. See if you have properties files or environment variable passwords set in grouper.hibernate.properties (this is not common). You need to move these to the database (encrypted).
2. If you do NOT have grouper-ws.properties (key: ws.security.prependToUserIdForSubjectLookup) Use this query in loader in step #4
  
  
  ```
  select username as subject_id_or_identifier from grouper_password gp
  where gp.application = 'WS' and gp.the_password not like 'xXxXx%'
  and not exists (select 1 from grouper_groups gg where gg.id = gp.username)
  union all
  select name as subject_id_or_identifier from grouper_groups gg where id
  in (select username from grouper_password gp where gp.application = 'WS'
  and gp.the_password not like 'xXxXx%')
  ```
3. If you DO have grouper-ws.properties (key: ws.security.prependToUserIdForSubjectLookup) Use this query in loader in step #4
  
  
  ```
  ws.security.prependToUserIdForSubjectLookup = etc:servicePrincipals:
  ```
  
  updated query for #2 (for postgres or oracle) (substitute in config value):
  
  
  ```
  select gm.subject_identifier0 as subject_id_or_identifier from grouper_password gp, grouper_members gm where gp.application = 'WS' and gp.the_password not like 'xXxXx%' and 'etc:servicePrincipals:' || gp.username = gm.subject_identifier0
  ```
4. Make a group of web service users, make an hourly loader with the query from #2 or #3. If you have this set in grouper-ws.properties: ws.client.user.group.name export the users of that group (in case loader messes up you can import again quickly), and configure the loader on that group, otherwise the group name is new and arbitrary. Note, you might want to make a new loaded group of these users so you don’t affect the current group if the query is wrong. Or just to test it out, and once verified, then use that query in the original group loader.
5. Run the diagnostics on that loader and see if you have any unresolvables. If you do, but all your web service users are listed in the group, you can ignore them. Otherwise, to remove them, delete from grouper_password (and the foreign keys in grouper_password_recently_used) any login which is unresolvable. Keep a backup first just in case, and note that if you have security.prependToUserIdForSubjectLookup. (mentioned above), then you need to append the username to that value.
6. Schedule and run the loader and make sure there are users in it. If not all of your WS users are in the group, then your query is wrong or you are not using passwords from the database. If the group previously existed, restore from your export and fix the query.
7. Configure that group name in #5 to be the WS client users group in grouper-ws.properties file or db, if you do not already have it set.
  
  
  ```
  ws.client.user.group.name = etc:webServiceClientUsers
  ```
8. Make sure clients can still use WS. You can remove a user from the group, wait 5 minutes, and see that the user is not allowed to access the WS. Then add them back to the group, wait 5 minutes, see the user allowed again.
9. Make sure to do this in all of your environments (dev/test/prod/etc)
10. Note: from now on, when you add a password in the DB for WS, you need to add the user to the WS loaded group, or you need to wait an hour for the loader to run

## Script daemon

You need to temporarily add a script daemon until you have a Grouper container which is fixed

```
Config id: grouperTempUserDaemon

Schedule: 45 28 * * * ?
```

```
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.Application;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordSave;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

//public class Test61passwordDuplicates {
//
//  public static void main(String[] args) {

    GrouperSession.startRootSession();

    String prefix = GrouperUtil.defaultIfBlank(GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.security.prependToUserIdForSubjectLookup"), "");
    String idOrIdentifierQuery = null;

    if (GrouperDdlUtils.isMysql()) {
      idOrIdentifierQuery = "select gm.subject_id, gp.application " +
          "from grouper_password gp, grouper_members gm where (concat(gp.username, ?) = gm.subject_id " +
          "or concat(gp.username, ?) = gm.subject_identifier0 or concat(gp.username, ?) = gm.subject_identifier1 " +
          "or concat(gp.username, ?) = gm.subject_identifier2) and gm.subject_id is not null " +
          "union select gm.subject_identifier0, gp.application " +
          "from grouper_password gp, grouper_members gm where (concat(gp.username, ?) = gm.subject_id " +
          "or concat(gp.username, ?) = gm.subject_identifier0 or concat(gp.username, ?) = gm.subject_identifier1 or concat(gp.username, ?) = gm.subject_identifier2) " +
          "and gm.subject_identifier0 is not null " +
          "union select gm.subject_identifier1, gp.application " +
          "from grouper_password gp, grouper_members gm where (concat(gp.username, ?) = gm.subject_id " +
          "or concat(gp.username, ?) = gm.subject_identifier0 or concat(gp.username, ?) = gm.subject_identifier1 or concat(gp.username, ?) = gm.subject_identifier2) " +
          "and gm.subject_identifier1 is not null " +
          "union select gm.subject_identifier2, gp.application " +
          "from grouper_password gp, grouper_members gm where (concat(gp.username, ?) = gm.subject_id " +
          "or concat(gp.username, ?) = gm.subject_identifier0 or concat(gp.username, ?) = gm.subject_identifier1 or concat(gp.username, ?) = gm.subject_identifier2) " +
          "and gm.subject_identifier2 is not null " +
          "union select gg.name, gp.application " +
          "from grouper_password gp, grouper_groups gg where (concat(gp.username, ?) = gg.name or concat(gp.username, ?) = gg.id) and gg.name is not null "  +
          "union select gg.id, gp.application " +
          "from grouper_password gp, grouper_groups gg where (concat(gp.username, ?) = gg.name or concat(gp.username, ?) = gg.id) and gg.id is not null ";
    } else {
      idOrIdentifierQuery = "select gm.subject_id, gp.application " +
          "from grouper_password gp, grouper_members gm where (? || gp.username = gm.subject_id " +
          "or ? || gp.username = gm.subject_identifier0 or ? || gp.username = gm.subject_identifier1 " +
          "or ? || gp.username = gm.subject_identifier2) and gm.subject_id is not null " +
          "union select gm.subject_identifier0, gp.application " +
          "from grouper_password gp, grouper_members gm where (? || gp.username = gm.subject_id " +
          "or ? || gp.username = gm.subject_identifier0 or ? || gp.username = gm.subject_identifier1 or ? || gp.username = gm.subject_identifier2) " +
          "and gm.subject_identifier0 is not null " +
          "union select gm.subject_identifier1, gp.application " +
          "from grouper_password gp, grouper_members gm where (? || gp.username = gm.subject_id " +
          "or ? || gp.username = gm.subject_identifier0 or ? || gp.username = gm.subject_identifier1 or ? || gp.username = gm.subject_identifier2) " +
          "and gm.subject_identifier1 is not null " +
          "union select gm.subject_identifier2, gp.application " +
          "from grouper_password gp, grouper_members gm where (? || gp.username = gm.subject_id " +
          "or ? || gp.username = gm.subject_identifier0 or ? || gp.username = gm.subject_identifier1 or ? || gp.username = gm.subject_identifier2) " +
          "and gm.subject_identifier2 is not null " +
          "union select gg.name, gp.application " +
          "from grouper_password gp, grouper_groups gg where (? || gp.username = gg.name or ? || gp.username = gg.id) and gg.name is not null " +
          "union select gg.id, gp.application " +
          "from grouper_password gp, grouper_groups gg where (? || gp.username = gg.name or ? || gp.username = gg.id) and gg.id is not null ";
    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<Object[]> idOrIdentifiersAndApplicationsObjectArray = new GcDbAccess().sql(idOrIdentifierQuery).
        addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).
        addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).
        addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).
        addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).addBindVar(prefix).selectList(Object[].class);

    debugMap.put("idsOrIdentifiers", idOrIdentifiersAndApplicationsObjectArray.size());

    Set<MultiKey> idOrIdentifiersAndApplications = new HashSet<MultiKey>();
    for (Object[] idOrIdentifierAndApplicationObject : idOrIdentifiersAndApplicationsObjectArray) {
      if (GrouperUtil.isBlank(idOrIdentifierAndApplicationObject[0])) {
        continue;
      }
      idOrIdentifiersAndApplications.add(new MultiKey(idOrIdentifierAndApplicationObject[0], idOrIdentifierAndApplicationObject[1]));
    }

    List<Object[]> usernamesAndApplicationsObjectArray = new GcDbAccess().sql("select gp.username, gp.application from grouper_password gp").selectList(Object[].class);

    debugMap.put("usernames", usernamesAndApplicationsObjectArray.size());

    Set<MultiKey> usernamesAndApplications = new HashSet<MultiKey>();
    for (Object[] usernameAndApplicationObject : usernamesAndApplicationsObjectArray) {
      usernamesAndApplications.add(new MultiKey(usernameAndApplicationObject[0], usernameAndApplicationObject[1]));
    }

    idOrIdentifiersAndApplications.removeAll(usernamesAndApplications);

    OtherJobScript otherJobScript = OtherJobScript.retrieveFromThreadLocal();

    if (otherJobScript != null) {
      otherJobScript.getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(idOrIdentifiersAndApplications.size());
      otherJobScript.getOtherJobInput().getHib3GrouperLoaderLog().addTotalCount(usernamesAndApplicationsObjectArray.size() + idOrIdentifiersAndApplications.size());
    }

    for (MultiKey idOrIdentifiersAndApplication : idOrIdentifiersAndApplications) {
      String idOrIdentifier = (String)idOrIdentifiersAndApplication.getKey(0);
      String idOrIdentifierNoColons = StringUtils.replace(idOrIdentifier, ":", "_CoLoN_");
      String application = (String)idOrIdentifiersAndApplication.getKey(1);
      new GrouperPasswordSave().assignApplication(Application.valueOf(application)).
        assignUsername(idOrIdentifierNoColons).assignPassword(GrouperUuid.getUuid()).assignSaveMode(SaveMode.INSERT).save();
      new GcDbAccess().sql("update grouper_password set username = ?, the_password = ? where username = ? and application = ?").
        addBindVar(idOrIdentifier).
        addBindVar("xXxXx___" + GrouperUuid.getUuid()).addBindVar(idOrIdentifierNoColons).addBindVar(application).executeSql();

      debugMap.put("addPasswordFor_" + idOrIdentifier, true);
    }

    debugMap.put("totalPasswords", new GcDbAccess().sql("select count(1) from grouper_password gp").select(int.class));
    debugMap.put("existingPasswords", new GcDbAccess().sql("select count(1) from grouper_password gp where the_password not like 'xXxXx%'").select(int.class));
    debugMap.put("newPasswords", new GcDbAccess().sql("select count(1) from grouper_password gp where the_password like 'xXxXx%'").select(int.class));
    debugMap.put("ws.security.prependToUserIdForSubjectLookup", GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.security.prependToUserIdForSubjectLookup"));
    String wsGroupName = GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.client.user.group.name");
    debugMap.put("ws.client.user.group.name", wsGroupName);
    if (!StringUtils.isBlank(wsGroupName)) {
      Group wsGroup = GroupFinder.findByName(wsGroupName, true);
      debugMap.put("wsGroupCount", wsGroup == null ? 0 : wsGroup.getMembers().size());
    }

    if (otherJobScript != null) {
      otherJobScript.getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));
    } else {
      System.out.println(GrouperUtil.mapToString(debugMap));
      //System.exit(0);
    }

//  }
//
//}

```

Run the daemon. The first run should have some inserts. The second should not

After adding this daemon, and loader, and config the ws user group, you are protected from this issue. On the day before we release the CVE we will provide you with confidential instructions on how to test your environments to make sure they are protected.

## UI authentication

Generally you will be using SSO so this is not an issue.

If you have these: grouper.is.ui.basicAuthn = true or GROUPER_UI_GROUPER_AUTH=true please proceed with the adjustment ASAP.

1. Change that setting to false or turn off your Grouper UI or protect with a VPN or other controls so that only admins or trusted users can access it
2. The Grouper UI is not intended to be run with basic auth; switch this over to OIDC, SAML, or something else that ties into your SSO.
3. If you must run a non prod env with basic auth, wait for the release of the container where this is fixed, upgrade to it, and turn the setting back to true and turn on your Grouper UI.
4. Make sure to do this in all of your environments (dev/test/prod/etc)
