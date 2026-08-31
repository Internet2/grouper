---
title: "Penn GitLab integration"
space: Grouper
pageId: 28544059
version: 5
lastUpdated: 2026-07-01T05:48:55.882Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544059/Penn+GitLab+integration
---

## Overview

Penn GitLab is deprovisioned by Grouper.

Originally we explored using SCIM but it was not sufficient for various reasons. The APIs have access to do more things and what we needed to do could not be done by SCIM at the time (details not known).

If a user does not use GitLab for a while, they will be a delicensed user.

## Get access to GitLab

There are two types of users who need new access to GitLab

1. **New user:** is a user who has never been in Penn GitLab or a user who was deprovisioned due to eligibility (e.g. left ISC or Penn), and then returned.
2. **Delicensed user:** is a user who had a license and lost their license due to inactivity. If they do not use GitLab for 60 days they will lose your license. Privileges will be stored so they can be restored when they return.

Instructions:

1. (**New user**): You need a [https://gitlab.com](https://gitlab.com/) account. You can use an existing one or use a new one. Note your email address in your account.
2. (**All users**): Penn GitLab users must be eligible. You must be in the IT department or explicitly allowed in Grouper by a GitLab admin. If you are in the gitlabEligibilityGroup, you will be able to see it
3. (**All users**): Go to the GitLab license app (custom template) and claim a license
4. (**All users**): Go to [https://gitlab.com](https://gitlab.com/) (not the Penn url), and sign in with your gitlab username and password.
5. (**All users**): Wait a minute, and go to https://gitlab.com/tenant-name, sign in with SAML. If it says page not found, try signing out of GitLab and signing back in, and going to that URL.  
    
    
   
  
  1. (**Delicensed user):**Usually you do not need to do this, but if you have a problem, you can go to gitlab, preferences, account, "Disconnect SAML for tenant-name", then link it again by logging in: https://gitlab.com/tenant-name. Note, if you do this, you need to wait 10 minutes before re-linking.
6. (**New user**): Let a privileged ISC GitLab user grant privileges to your GitLab email address
7. (**Delicensed user**): Go to [GitLab license app](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.indexGshSimplifiedUi?operation=UiV2Template.newTemplateSimplifiedUi&templateType=gitlabMembershipFixer&stemId=ed3c972760c6469e887c013dc9d50031) (custom template) and populate privileges. This will copy our privileges back that were lost when you were delicensed.
8. (**Delicensed user**): If you have OpenShift roles which are not restored in the above step, an OpenShift admin needs to "refresh those roles" for the user so they get assigned in GitLab

## Description

The rest of this document describes the technical details of how self-claimed licenses work in PennGroups.

1. The PennGroups loader pulls authorizations from GitLab
2. Syncs the data to the PennGroups database
3. The PennGroups deprovisioner determines which authorizations are invalid
4. Removes authorizations from GitLab

## Groups

| **Group name** | **Description** |
| --- | --- |
| penn:isc:ait:apps:gitlab:service:ref:gitlabEligibilityGroup | People must be in this group to be able   to be in ISC GitLab. Include all ISC people, and    others who are explicitly put in this group.    The eligibility criteria for this group is: memberOfPenn,    the user should work at Penn or a related organization. |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabFrontDoor | Can log in to GitLab with Penn SSO. If not in this group,   will redirect to the error page. Consists of eligible and   recent users to GitLab, or the user could add themselves   to the "claimed license" group |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabRecentAndEligibleUsers | Has used GitLab in the last 60 days (iscGitlabUsersRecentSamlOrActivity),    and is eligible for GitLab (gitlabEligibilityGroup) |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabClaimedAndEligibleUsers | People who have claimed a license    and are eligible for GitLab. Any membership   added to this group will get a default disabled   date 7 days in the future (since they are GitLab   users by then). |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabUsersRecentSamlOrActivity | Has authenticated through SSO or has used   GitLab recently |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabSamlLast60days | Loaded group (configured via recent WebLogin provisioner)   of users who have authenticated to this entity ID in last 60 days:   https://gitlab.com/groups/tenant-name |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabUsersRecentSigninOrActivity | Loaded group from the user table loaded from GitLab.   Consists of matched users who have logged in or   have activity in GitLab in last 60 days |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabUsersWithPrivileges | Loaded group from the user table loaded from GitLab.   Consists of matched users who have some role in some   group or folder in GitLab. |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabUsersNotRelinked | Users who have lost their license due to inactivity |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabUsers | Users in GitLab |
| penn:isc:ait:apps:gitlab:service:policy:iscGitlabUserClaimLicense | Users in this group have claimed a license so they can get through the front door |
| penn:community:employeeOrContractorIncludingUphsInTwoStep | Workforce in Two-Step. Front door reference group for this service. |

## Deprovisioning

User accounts are deprovisioned if they are no longer eligible, or if they have not used GitLab recently. In all cases the privileges that are removed are stored in reports for 30 days and emailed to the GitLab admins.

After 60 days of inactivity (have not logged in to GitLab or used GitLab), if the user is still eligible, their privileges are backed up (until the user is ineligible), and removed from GitLab. When the user returns, they can reinstate their privileges. If a user becomes ineligible while their privileges were delicensed, the privileges are deleted.

If a user is no longer eligible for GitLab, they have a 3 day grace period, and their privileges will be removed from GitLab.

## Load data to PennGroups database

There is a feed using the GitLab REST API that populates tables in the PennGroups database

Memberships in GitLab are from groups and projects. This is the table of groups and projects.

Users from GitLab

Memberships from GitLab

GitLab view (joins the tables)

## Allow usernames

Only usernames which are linked to Penn IDs are eligible for deprovisioning. So you shouldn't need to add any to the allow list (i.e. will not be deprovisioned). But if you want to:

1. Go to iscGitlabUsers attribute assignments page
2. For attribute name gitlabNonPerson → Actions → Add value
3. Add the non-person username (example username: jsmith)

## Privilege report

There is a report (CSV) that shows all current and inactive GitLab privileges. This is a nightly report and a month's worth of reports are stored.

GitLab admins have access to the report: penn:isc:ait:apps:gitlab:security:gitlabAdmins

| Column | Example | Description |
| --- | --- | --- |
| current | T \| F | If this is a current privilege, or was removed due to user inactivity |
| penn_id | 12345678 | Penn ID |
| username | john-smith | GitLab username linked up to Penn identity with SAML |
| email | [jsmith@gmail.com](mailto:jsmith@gmail.com) | Email account associated with GitLab account |
| access_level | 30 | Numeric access level in GitLab which ties to role / access_label |
| access_label | minimal \| developer \| owner \| etc | Natural language representation of access_level |
| full_path | tenant-name/openshift | Path of the group or project |
| object_type | group \| project | If this is a group or project |
| object_id | 12436587 | Unique ID in GitLab for this group/folder |
| state | active | Attribute in the GitLab object model, not really used by this process |

## GitLab deprovisioner GSH daemon

```
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * daemon twice a day
 * @author mchyzer
 *
 */
public class Test94GitlabDeprovisioner {

  public static void main(String[] args) {

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();
    String configId = "gitlabIscPenn";
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      
      
      String baseUrl = GrouperLoaderConfig.retrieveConfig()
          .propertyValueStringRequired(
              "grouper.wsBearerToken." + configId + ".endpoint");
      baseUrl = GrouperUtil.stripLastSlashIfExists(baseUrl);

      debugMap.put("baseUrl", baseUrl);

      List<Object[]> mshipsToDeprovision = new GcDbAccess().sql("""
          SELECT 
          object_id,
          object_type,
          access_level,
          access_label,
          user_id,
          penn_id,
          extern_uid,
          username,
          full_path,
          user_created_on,
          mship_created_on,
          email,
          saml_provider_id,
          person,
          name,
          state,
          CASE 
              WHEN EXISTS (
                  SELECT 1
                  FROM penn_isc_gitlab_user_depro_v pigudv
                  WHERE pigudv.username = pigmv.username
              ) THEN 'T'
              ELSE 'F'
          END AS deprovision,
          CASE 
              WHEN NOT EXISTS (
                  SELECT 1
                  FROM penn_isc_gitlab_user_depro_v pigudv
                  WHERE pigudv.username = pigmv.username
              )
              AND EXISTS (
                  SELECT 1
                  FROM penn_isc_gitlab_user_delicense_v pigudv
                  WHERE pigudv.username = pigmv.username
              ) THEN 'T'
              ELSE 'F'
          END AS delicense
      FROM 
          penn_isc_gitlab_mship_v pigmv
      WHERE
          EXISTS (
              SELECT 1
              FROM penn_isc_gitlab_user_depro_v pigudv
              WHERE pigudv.username = pigmv.username
          )
          OR EXISTS (
              SELECT 1
              FROM penn_isc_gitlab_user_delicense_v pigudv
              WHERE pigudv.username = pigmv.username
          )
      ORDER BY 
          username,
          full_path,
          access_label
          """).selectList(Object[].class);

      hib3GrouperLoaderLog.addTotalCount(GrouperUtil.length(mshipsToDeprovision));
      debugMap.put("mshipsToDeprovision", GrouperUtil.length(mshipsToDeprovision));
      
      if (GrouperUtil.length(mshipsToDeprovision) > 0) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<html>Hello,<br />These memberships were (or will be) deprovisioned in gitlab"
            + " <a href=\"https://penngroups.isc.upenn.edu/deprovisioning/gitlab\">(See documentation)</a><br /><br /><ol>");
        String currentRecord = null;
        try {
          for (Object[] mshipToDeprovision : GrouperUtil.nonNull(mshipsToDeprovision)) {
            
            currentRecord = "";
            
            long objectId = GrouperUtil.longValue(mshipToDeprovision[0]);
            String objectType = (String)mshipToDeprovision[1];
            long accessLevel = GrouperUtil.longValue(mshipToDeprovision[2]);
            String accessLabel = (String)mshipToDeprovision[3];
            long userId = GrouperUtil.longValue(mshipToDeprovision[4]);
            String pennId = (String)mshipToDeprovision[5];
            String externUid = (String)mshipToDeprovision[6];
            String username = (String)mshipToDeprovision[7];
            String fullPath = (String)mshipToDeprovision[8];
            Timestamp userCreatedOn = GrouperUtil.toTimestamp(mshipToDeprovision[9]);
            Timestamp mshipCreatedOn = GrouperUtil.toTimestamp(mshipToDeprovision[10]);
            String email = (String)mshipToDeprovision[11];
            long samlProviderId = GrouperUtil.longValue(mshipToDeprovision[12], -1);
            boolean person = GrouperUtil.booleanValue(mshipToDeprovision[13], false);
            String name = (String)mshipToDeprovision[14];
            String state = (String)mshipToDeprovision[15];
            String deprovision = (String) mshipToDeprovision[16];
            String delicense = (String) mshipToDeprovision[17];

            currentRecord = "Object id: " + objectId + ", objectType: " + objectType + ", access level: " + accessLevel + ", access label: " + accessLabel +
                ", userId: " + userId + ", pennId: " + pennId + ", externUid: " + externUid + ", username: " + username +
                ", fullPath: " + fullPath + ", mshipCreated: " + mshipCreatedOn + ", person: " + person + ", email: " + email +
                ", state: " + state + ", samlProviderId: " + samlProviderId + ", name: " + name + ", userCreatedOn: " + userCreatedOn + 
                ", deprovision: " + deprovision + ", delicense: " + delicense;

            boolean membershipOlderThan3days = mshipCreatedOn.getTime() < System.currentTimeMillis() - (1000 * 60 * 60L * 24 * 3);
            if (!StringUtils.isBlank(pennId) && StringUtils.equals(pennId, externUid) && membershipOlderThan3days) {
              
              GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
              grouperHttpClient.assignGrouperHttpMethod("DELETE");

              WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId);

              // https://gitlab.com/api/v4/groups/9305937/members/5324525
              grouperHttpClient.assignDebugMap(debugMap).
                assignUrl(baseUrl + "/api/v4/" + (StringUtils.equals(objectType, "group") ? "groups" : "projects") + "/" + objectId + "/members/" + userId).executeRequest();
              
              currentRecord += ", httpResponseCode: " + grouperHttpClient.getResponseCode();
              
              if (204 != grouperHttpClient.getResponseCode() && 404 != grouperHttpClient.getResponseCode()) {
                throw new RuntimeException("Response code: " + grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody());
              }

              if (StringUtils.equals(delicense, "T")) {
                int existingRows = new GcDbAccess().
                  sql("select count(1) from penn_isc_gitlab_mship_delicense where user_id = ? and object_id = ? and access_level = ?").
                  addBindVar(userId).addBindVar(objectId).addBindVar(accessLevel).select(int.class);

                if (existingRows == 0) {
                  new GcDbAccess().
                    sql("""
                      insert into penn_isc_gitlab_mship_delicense 
                      (user_id, username, email, penn_id, object_id, access_level, membership_state, object_type) 
                      values (?, ?, ?, ?, ?, ?, ?, ?)
                    """).
                    addBindVar(userId).addBindVar(username).addBindVar(email).addBindVar(pennId).addBindVar(objectId).addBindVar(accessLevel).
                    addBindVar(state).addBindVar(objectType).executeSql();

                  hib3GrouperLoaderLog.addInsertCount(1);
                }

                emailBody.append("<li>Membership delicensed: " + GrouperUtil.escapeHtml(currentRecord, true) + "</li>\n");
              } else {
                emailBody.append("<li>Membership deleted: " + GrouperUtil.escapeHtml(currentRecord, true) + "</li>\n");
              }

              // delete from data loaded so it wont be attempted to be deleted again
              new GcDbAccess().sql("delete from penn_isc_gitlab_mship where user_id = ? and object_id = ? and access_level = ?").
                addBindVar(userId).addBindVar(objectId).addBindVar(accessLevel).executeSql();
              
              hib3GrouperLoaderLog.addDeleteCount(1);
              
            } else {

              emailBody.append("<li>Membership will be deleted soon: " + GrouperUtil.escapeHtml(currentRecord, true) + "</li>\n");

            }
            
          }
          emailBody.append("</ol>\n");
        } catch (RuntimeException e) {
          GrouperUtil.injectInException(e, currentRecord);
          emailBody.append("</ol>\n<pre>" + GrouperUtil.escapeHtml(GrouperUtil.getFullStackTrace(e), true) + "</pre>\n");
          throw e;
        } finally {
          emailBody.append("<br />Thanks\n</html>");
          debugMap.put("email", emailBody.toString());
          new GrouperEmail().setBody(emailBody.toString()).addEmailAddressToSendTo("penn:isc:ait:apps:gitlab:security:gitlabAdminsToEmail@grouper").
            setSubject("ISC GitLab deprovisioning error notification").send();
        }
        
      }
      
      int count = new GcDbAccess().sql("""
          delete from penn_isc_gitlab_mship_delicense pigmd
          where not exists (select 1 from grouper_memberships_lw_v gmlv
          where gmlv.subject_id = pigmd.penn_id and gmlv.subject_source = 'pennperson' and gmlv.list_name = 'members'
          and gmlv.group_name = 'penn:isc:ait:apps:gitlab:service:ref:gitlabEligibilityGroup')
          """).executeSql();
      
      debugMap.put("delicensedMembershipsDeletedIneligible", count);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      String debugMapForLog = GrouperUtil.toStringForLog(debugMap);
      hib3GrouperLoaderLog.setJobMessage(debugMapForLog);
      GrouperSession.stopQuietly(grouperSession);
      if (OtherJobScript.retrieveFromThreadLocal() == null) {
        System.out.println(debugMapForLog);
        System.exit(0);
      }
    }
  }
}
```

## GitLab access loader GSH daemon

```
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncFromData;

public class Test94GitlabLoader {

  /**
   * TODO remove after next upgrade
   * start the sub field with JSON_NODE_ROOT if you want the pointer from root node instead of array node
   * @param rootJsonNode
   * @param jsonPointerOfArrayNode json path that returns a list e.g. /a/b
   * @param jsonPointersOfSubFields from the perspective of the array node, e.g. /c/d
   * @return the list of object arrays not null
   */
  public static List<Object[]> jsonJacksonListObjectArrayFromJsonPointers(JsonNode rootJsonNode, String jsonPointerOfArrayNode,
      List<String> jsonPointersOfSubFields) {
    
    List<Object[]> results = new ArrayList<Object[]>();
    
    if (rootJsonNode != null) {
      
      JsonNode arrayNodeJsonNode = rootJsonNode;
      // traverse down a tad
      if (!StringUtils.isBlank(jsonPointerOfArrayNode)) {
        arrayNodeJsonNode = rootJsonNode.at(jsonPointerOfArrayNode);
      }
      
      if (arrayNodeJsonNode != null) {
        if (!(arrayNodeJsonNode instanceof ArrayNode)) {
          throw new RuntimeException("ArrayNode not found at '" + jsonPointerOfArrayNode + "'");
        }
        ArrayNode arrayNode = (ArrayNode)arrayNodeJsonNode;
        
        for (int i=0;i<arrayNode.size();i++) {
          JsonNode objectIterated = arrayNode.get(i);
          Object[] row = new Object[GrouperUtil.length(jsonPointersOfSubFields)];
          results.add(row);
          int j=0;
          for (String jsonPointerOfSubField : GrouperUtil.nonNull(jsonPointersOfSubFields)) {
            JsonNode dataValueNode = null;
            if (jsonPointerOfSubField.startsWith("JSON_NODE_ROOT")) {
              jsonPointerOfSubField = GrouperUtil.prefixOrSuffix(jsonPointerOfSubField, "JSON_NODE_ROOT", false);
              dataValueNode = rootJsonNode.at(jsonPointerOfSubField);
            } else {
              dataValueNode = objectIterated.at(jsonPointerOfSubField);
            }
            
            if (dataValueNode != null && !(dataValueNode instanceof NullNode)) {

              if (dataValueNode.isInt() || dataValueNode.isLong()) {
                row[j] = dataValueNode.asLong();
              } else if (dataValueNode.isDouble() || dataValueNode.isFloat()) {
                row[j] = dataValueNode.asDouble();
              } else if (dataValueNode.isBoolean()) {
                row[j] = dataValueNode.asBoolean();
              } else {
                row[j] = dataValueNode.asText();
              }
            }        
            j++;
          }
        }
      }
    }
    
    return results;
  }

  public static void convertTypes(Object[] inputArray) {
    if (inputArray == null) {
      return;
    }
    for (int j=0;j<inputArray.length;j++) {
      inputArray[j] = convertType(inputArray[j]);
    }

  }
  
  public static void convertTypesList(List<Object[]> inputList) {
    if (inputList == null) {
      return;
    }
    for (Object[] inputArray: inputList) {
      for (int j=0;j<inputArray.length;j++) {
        inputArray[j] = convertType(inputArray[j]);
      }
    }
  }
  
  public static Object convertType(Object input) {
    if (input instanceof Long) {
      return new BigDecimal((Long)input);
    }
    if (input instanceof Integer) {
      return new BigDecimal((Integer)input);
    }
    return input;
  }
  public static void main(String[] args) {

    // 10169960 upenn-oag
    Set<Long> userIdsWhichAreBots = GrouperUtil.toSet(10169960L, 4028924L, 907655L);
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();
    String configId = "gitlabIscPenn";
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    try {
      
      
      String baseUrl = GrouperLoaderConfig.retrieveConfig()
          .propertyValueStringRequired(
              "grouper.wsBearerToken." + configId + ".endpoint");
      baseUrl = GrouperUtil.stripLastSlashIfExists(baseUrl);

      debugMap.put("baseUrl", baseUrl);

      int groupPageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.wsBearerToken." + configId + ".groupPageSize", 100);

      //  penn_isc_gitlab_group
      //  id
      //  path
      //  full_path
      //  object_type
      
      // get the group ids from database
      Set<Long> groupIds = new LinkedHashSet<>();
      Set<Long> projectIds = new LinkedHashSet<>();

      // id, path, full_path
      List<Object[]> wsRows = new ArrayList<Object[]>();
      
      Map<String, Long> groupFullPathToGroupId = new HashMap<>();

      for (int i=0;i<10000;i++) {

        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
        grouperHttpClient.assignGrouperHttpMethod("GET");

        WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId);

        grouperHttpClient.assignDebugMap(debugMap).assignAssertResponseCode(200).
          assignUrl(baseUrl + "/api/v4/groups?per_page=" + groupPageSize + "&page="+(i+1)).executeRequest();
        
        // convert the response into a list of rows
        List<Object[]> rowsFromWs = jsonJacksonListObjectArrayFromJsonPointers(grouperHttpClient.retrieveJsonNode(), 
            null, GrouperUtil.toList("/id", "/path", "/full_path"));
        if (GrouperUtil.length(rowsFromWs) == 0) {
          break;
        }
        
        Iterator<Object[]> iterator = rowsFromWs.iterator();
        while (iterator.hasNext()) {
          Object[] row = iterator.next();
          String fullPath = (String)row[2];
          if (!fullPath.startsWith("tenant-name")) {
            continue;
          }
          List<Object> rowList = GrouperUtil.toList(row);
          rowList.add("group");
          row = GrouperUtil.toArray(rowList, Object.class);
          wsRows.add(row);
          
          groupIds.add(GrouperUtil.longValue(row[0]));
          groupFullPathToGroupId.put((String)row[2], GrouperUtil.longValue(row[0]));

        }
        
        
        int totalPages = GrouperUtil.intValue(grouperHttpClient.getResponseHeaders().get("x-total-pages"), -1);
        if (totalPages-1 <= i) {
          break;
        }
        
      }
      
      for (int i=0;i<10000;i++) {

        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
        grouperHttpClient.assignGrouperHttpMethod("GET");

        WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId);

        grouperHttpClient.assignDebugMap(debugMap).assignAssertResponseCode(200).
            assignUrl(baseUrl + "/api/v4/projects?owned=true&per_page=" + groupPageSize + "&page="+(i+1)).executeRequest();
        
        // convert the response into a list of rows
        List<Object[]> rowsFromWs = jsonJacksonListObjectArrayFromJsonPointers(grouperHttpClient.retrieveJsonNode(), 
            null, GrouperUtil.toList("/id", "/path", "/path_with_namespace"));
        if (GrouperUtil.length(rowsFromWs) == 0) {
          break;
        }
        
        Iterator<Object[]> iterator = rowsFromWs.iterator();
        while (iterator.hasNext()) {
          Object[] row = iterator.next();
          String fullPath = (String)row[2];
          if (!fullPath.startsWith("tenant-name")) {
            continue;
          }
          List<Object> rowList = GrouperUtil.toList(row);
          rowList.add("project");
          row = GrouperUtil.toArray(rowList, Object.class);
          
          wsRows.add(row);
          
          projectIds.add(GrouperUtil.longValue(row[0]));

        }
        
        int totalPages = GrouperUtil.intValue(grouperHttpClient.getResponseHeaders().get("x-total-pages"), -1);
        if (totalPages-1 <= i) {
          break;
        }
        
      }
      
      debugMap.put("groupsSize", GrouperUtil.length(groupIds));
      debugMap.put("projectsSize", GrouperUtil.length(projectIds));
      hib3GrouperLoaderLog.addTotalCount(GrouperUtil.length(wsRows));
      
      // sync that to the table
      String tableName = "penn_isc_gitlab_group";
      List<String> columnNames = GrouperUtil.toList("id", "path", "full_path", "object_type");
      List<String> columnNamesPrimaryKey = GrouperUtil.toList("id", "object_type");

      convertTypesList(wsRows);
      new GcTableSyncFromData().assignDebugMap(debugMap).assignDebugMapPrefix("group_").assignTableName(tableName).
        assignColumnNames(columnNames).assignColumnNamesPrimaryKey(columnNamesPrimaryKey).assignData(wsRows).sync();

      // lets get users
      int userPageSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.wsBearerToken." + configId + ".userPageSize", 100);

      //  penn_isc_gitlab_user
      //  id
      //  username
      //  name
      //  state
      //  email
      //  extern_uid
      //  saml_provider_id
      //  person
      //  penn_id
      
      // get the user ids from database
      Set<Long> userIds = new HashSet<>();

      // id, path, full_path
      List<Object[]> wsUserRows = new ArrayList<Object[]>();
      
      Long iscPennId = groupFullPathToGroupId.get("tenant-name");

      GrouperUtil.assertion(iscPennId != null, "Cant find tenant-name group");
      
      for (int i=0;i<10000;i++) {

        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
        grouperHttpClient.assignGrouperHttpMethod("GET");

        WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId);

        // https://gitlab.com/api/v4/groups/69205688/saml_users?per_page=100&page=1
        grouperHttpClient.assignDebugMap(debugMap).assignAssertResponseCode(200).
          assignUrl(baseUrl + "/api/v4/groups/" + iscPennId + "/saml_users?per_page=" + userPageSize + "&page="+(i+1)).executeRequest();
        

        
        ArrayNode arrayNode = (ArrayNode)grouperHttpClient.retrieveJsonNode();
        
        if (arrayNode == null || arrayNode.size() == 0) {
          break;
        }

        for (int userIndex=0;userIndex<arrayNode.size();userIndex++) {

          JsonNode userNode = arrayNode.get(userIndex);
          List<Object> rowFromWsList = GrouperUtil.toList(new Object[11]);

          String email = GrouperUtil.jsonJacksonGetString(userNode, "email");
          rowFromWsList.set(4, email);

          Long userId = GrouperUtil.jsonJacksonGetLong(userNode, "id");
          rowFromWsList.set(0, userId);
          
          boolean bot = GrouperUtil.jsonJacksonGetBoolean(userNode, "bot", false);
          rowFromWsList.set(7, (bot || userIdsWhichAreBots.contains(userId)) ? "F" : "T");

          rowFromWsList.set(1, GrouperUtil.jsonJacksonGetString(userNode, "username"));
          rowFromWsList.set(2, GrouperUtil.jsonJacksonGetString(userNode, "name"));
          rowFromWsList.set(3, GrouperUtil.jsonJacksonGetString(userNode, "state"));

          ArrayNode identitiesNode = GrouperUtil.jsonJacksonGetArrayNode(userNode, "identities");
          
          for (int identititiesIndex = 0; identitiesNode != null && identititiesIndex < identitiesNode.size(); identititiesIndex++) {
            JsonNode identityNode = identitiesNode.get(identititiesIndex);
            long samlProviderId = GrouperUtil.jsonJacksonGetLong(identityNode, "saml_provider_id", -1L);
            if (samlProviderId == 5190L) {
              rowFromWsList.set(5, GrouperUtil.jsonJacksonGetString(identityNode, "extern_uid"));
              rowFromWsList.set(6, samlProviderId);
              break;
            }
          }
          
          
          String externUid = (String)rowFromWsList.get(5);
          String pennId = null;
          if (externUid != null && externUid.matches("[0-9]{8}")) {
            pennId = externUid;
          }
          
          if (StringUtils.isBlank(pennId) && !StringUtils.isBlank(email)) {
            
            pennId = new GcDbAccess().connectionName("pennCommunity").sql(
                "SELECT penn_id FROM diradmin.EMAIL_TO_PERSON_COMPOSITE WHERE email = ?").addBindVar(email).select(String.class);
            
          }

          if (!StringUtils.isBlank(pennId)) {
            rowFromWsList.set(8, pennId);
          }
          
          if (!StringUtils.isBlank(pennId)) {
            
            rowFromWsList.set(7, "T");
            
          }

          // 9: current_sign_in_at: 2025-06-10T15:47:26.297Z
          rowFromWsList.set(9, GrouperUtil.timestampIsoUtcSecondsConvertFromString(GrouperUtil.jsonJacksonGetString(userNode, "current_sign_in_at")));

          // 10: last_activity_on: 2025-06-24
          rowFromWsList.set(10, GrouperUtil.stringToTimestamp(GrouperUtil.jsonJacksonGetString(userNode, "last_activity_on")));
          
          Object[] row = GrouperUtil.toArray(rowFromWsList, Object.class);
          
          wsUserRows.add(row);

        }
        
        int totalPages = GrouperUtil.intValue(grouperHttpClient.getResponseHeaders().get("x-total-pages"), -1);
        if (totalPages-1 <= i) {
          break;
        }

      }
      
      for (Object[] wsRow : wsUserRows) {
        userIds.add(GrouperUtil.longValue(wsRow[0]));
      }
      
      // memberships
      
      // penn_isc_gitlab_mship
      // user_id
      // access_level
      // membership_state
      // group_id
      // object_type

      // id, path, full_path
      List<Object[]> wsMshipRows = new ArrayList<Object[]>();
      
      for (String objectType : GrouperUtil.toList("group", "project")) {

        Set<Long> objectIds = StringUtils.equals(objectType, "group") ? groupIds : projectIds;
        
        for (Long objectId : objectIds) {
        
          for (int i=0;i<10000;i++) {
  
            GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
            grouperHttpClient.assignGrouperHttpMethod("GET");
  
            WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId);
  
            // https://gitlab.com/api/v4/groups/69205688/members?per_page=1000&page=1
            grouperHttpClient.assignDebugMap(debugMap).assignAssertResponseCode(200).
                assignUrl(baseUrl + "/api/v4/" + (StringUtils.equals(objectType, "group") ? "groups" : "projects") + "/" + objectId + "/members?per_page=" + groupPageSize + "&page="+(i+1)).executeRequest();
            
            // convert the response into a list of rows
            List<Object[]> rowsFromWs = jsonJacksonListObjectArrayFromJsonPointers(grouperHttpClient.retrieveJsonNode(), 
                null, GrouperUtil.toList("/id", "/access_level", "/membership_state"));
            
            if (GrouperUtil.length(rowsFromWs) == 0) {
              break;
            }
  
            for (Object[] rowFromWs: rowsFromWs) {
  
              List<Object> rowFromWsList = GrouperUtil.toList(rowFromWs);
              
              Long userId = GrouperUtil.longValue(rowFromWsList.get(0));
              
              // User IDs doesnt contain: 3262945, from group: 7945173
              if (!userIds.contains(userId)) {
                grouperHttpClient = new GrouperHttpClient();
                grouperHttpClient.assignGrouperHttpMethod("GET");
  
                WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId);
  
                // https://gitlab.com/api/v4/users/69205688
                grouperHttpClient.assignDebugMap(debugMap).assignAssertResponseCode(200).
                    assignUrl(baseUrl + "/api/v4/users/" + userId).executeRequest();
                
                // convert the response into a list of rows
                JsonNode userNode = grouperHttpClient.retrieveJsonNode();
                
                List<Object> userRow = new ArrayList<>();
  
                userRow.add(GrouperUtil.jsonJacksonGetLong(userNode, "id"));
                userRow.add(GrouperUtil.jsonJacksonGetString(userNode, "username"));
                userRow.add(GrouperUtil.jsonJacksonGetString(userNode, "name"));
                userRow.add(GrouperUtil.jsonJacksonGetString(userNode, "state"));
                userRow.add(GrouperUtil.jsonJacksonGetString(userNode, "public_email"));
                userRow.add(null);
                userRow.add(null);
                Boolean botBoolean = GrouperUtil.jsonJacksonGetBoolean(userNode, "bot");
                userRow.add(botBoolean == null ? null : ((botBoolean || userIdsWhichAreBots.contains(userId)) ? "F" : "T"));
  
                String email = (String)userRow.get(4);
                
                String pennId = null;
                
                if (!StringUtils.isBlank(email)) {
                  
                  pennId = new GcDbAccess().connectionName("pennCommunity").sql(
                      "SELECT penn_id FROM diradmin.EMAIL_TO_PERSON_COMPOSITE WHERE email = ?").addBindVar(email).select(String.class);
                  
                }
  
                userRow.add(pennId);
                userRow.add(null);
                userRow.add(null);

  
                if (!StringUtils.isBlank(pennId)) {
                  
                  userRow.set(7, "T");
                  
                }
                Object[] row = GrouperUtil.toArray(userRow, Object.class);
                wsUserRows.add(row);
                userIds.add(userId);
              }
              
              rowFromWsList.add(objectId);
              rowFromWsList.add(objectType);
              
              Object[] row = GrouperUtil.toArray(rowFromWsList, Object.class);
              
              wsMshipRows.add(row);
            }
            
            int totalPages = GrouperUtil.intValue(grouperHttpClient.getResponseHeaders().get("x-total-pages"), -1);
            if (totalPages-1 <= i) {
              break;
            }
  
          }
  
        }
      }
      
      debugMap.put("usersSize", GrouperUtil.length(wsUserRows));
      hib3GrouperLoaderLog.addTotalCount(GrouperUtil.length(wsUserRows));
            
      // sync that to the table
      tableName = "penn_isc_gitlab_user";
      columnNames = GrouperUtil.toList("id", "username", "name", "state", "email", "extern_uid", "saml_provider_id", "person", "penn_id" 
          , "current_sign_in_at", "last_activity_on");
      columnNamesPrimaryKey = GrouperUtil.toList("id");

      convertTypesList(wsUserRows);
      new GcTableSyncFromData().assignDebugMapPrefix("user_").assignDebugMap(debugMap).assignTableName(tableName).
        assignColumnNames(columnNames).assignColumnNamesPrimaryKey(columnNamesPrimaryKey).assignData(wsUserRows).sync();

      debugMap.put("mshipsSize", GrouperUtil.length(wsMshipRows));
      hib3GrouperLoaderLog.addTotalCount(GrouperUtil.length(wsMshipRows));
      
      // sync that to the table
      tableName = "penn_isc_gitlab_mship";
      columnNames = GrouperUtil.toList("user_id", "access_level", "membership_state", "object_id", "object_type");
      columnNamesPrimaryKey = GrouperUtil.toList("user_id", "object_id", "object_type");
  
      convertTypesList(wsMshipRows);
      new GcTableSyncFromData().assignDebugMapPrefix("mship_").assignDebugMap(debugMap).assignTableName(tableName).
        assignColumnNames(columnNames).assignColumnNamesPrimaryKey(columnNamesPrimaryKey).assignData(wsMshipRows).sync();

      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      
      try {
        new GrouperEmail().setBody(GrouperUtil.getFullStackTrace(re)).addEmailAddressToSendTo("penn:isc:ait:apps:gitlab:security:gitlabAdminsToEmail@grouper").
          setSubject("ISC GitLab loader error").send();

      } catch (Exception e) {
        // ignore
      }

      
      throw re;
    } finally {
      hib3GrouperLoaderLog.setInsertCount(GrouperUtil.intObjectValue(debugMap.get("insertsCount"), true));
      hib3GrouperLoaderLog.setUpdateCount(GrouperUtil.intObjectValue(debugMap.get("updatesCount"), true));
      hib3GrouperLoaderLog.setDeleteCount(GrouperUtil.intObjectValue(debugMap.get("deletesCount"), true));
      String debugMapForLog = GrouperUtil.toStringForLog(debugMap);
      hib3GrouperLoaderLog.setJobMessage(debugMapForLog);
      if (OtherJobScript.retrieveFromThreadLocal() == null) {
        System.out.println(debugMapForLog);
      }
    }
  }
}

// uncomment this to run in grouper daemon
// Test94GitlabLoader.main(null);

```

## GitLab GSH template for license claim and access restore

```
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class Test94GitlabMembershipFixer extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {
    
    Subject currentUserSubject = gshTemplateV2input.getGsh_builtin_subject();

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

    String gsh_input_action = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_action");

    boolean isEligible = new GcDbAccess().sql("""
        select count(1) from grouper_memberships_lw_v
        where group_name = 'penn:isc:ait:apps:gitlab:service:ref:gitlabEligibilityGroup'
        and subject_id = ? and subject_source = ?
    """).addBindVar(currentUserSubject.getId()).addBindVar(currentUserSubject.getSourceId()).select(int.class) > 0;

    if (!isEligible) {
      gsh_builtin_gshTemplateOutput.addOutputLine("You are not eligible for GitLab access. Please contact the GitLab admins.");
      return;
    }

    // claimLicense, populatePrivileges
    boolean claimLicense = StringUtils.equals(gsh_input_action, "claimLicense");
    boolean populatePrivileges = StringUtils.equals(gsh_input_action, "populatePrivileges");
    
    if (claimLicense) {
      Group claimLicenseGroup = GroupFinder.findByName("penn:isc:ait:apps:gitlab:service:policy:iscGitlabUserClaimLicense", true);
      claimLicenseGroup.addMember(currentUserSubject, false);
      gsh_builtin_gshTemplateOutput.addOutputLine("Wait a minute and then link your account");
    } else if (populatePrivileges) {
      String configId = "gitlabIscPenn";
      
      Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
        
      String baseUrl = GrouperLoaderConfig.retrieveConfig()
          .propertyValueStringRequired(
              "grouper.wsBearerToken." + configId + ".endpoint");
      baseUrl = GrouperUtil.stripLastSlashIfExists(baseUrl);

      debugMap.put("baseUrl", baseUrl);

      // lets see if externUid is set correctly
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

      WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId);

      List<Object[]> objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPaths = new GcDbAccess().sql(
          "select object_id, pigmd.object_type, access_level, full_path, user_id, username "
          + "from penn_isc_gitlab_mship_delicense pigmd, penn_isc_gitlab_group pigg where pigg.id = pigmd.object_id and penn_id = ? "
          ).addBindVar(currentUserSubject.getId()).selectList(Object[].class);

      if (GrouperUtil.length(objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPaths) == 0) {
        gsh_builtin_gshTemplateOutput.addOutputLine("You do not have any memberships in gitlab");
        return;
      }

      Long userId = GrouperUtil.longValue(objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPaths.get(0)[4]);
      String username = (String)objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPaths.get(0)[5];

      grouperHttpClient.assignGrouperHttpMethod("GET").assignDebugMap(debugMap).
        assignUrl(baseUrl + "/api/v4/groups/69205688/saml_users?search=" + GrouperUtil.escapeUrlEncode(username)).executeRequest();

      if (grouperHttpClient.getResponseCode() != 200) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Cannot find your user in GitLab. Please make sure you have linked your account.");
        return;
      }

      //  [{
      //    "id": 3079259,
      //    "username": "mchyzer",
      //    "name": "Chris Hyzer",
      //    "identities": [{
      //      "provider": "group_saml",
      //      "extern_uid": "1c6a125f8654a16eb96966ffdb64db34",
      //      "saml_provider_id": 5190
      //    }],
      //  }]

      // https://gitlab.com/api/v4/groups/69205688/users?include_saml_users=true&per_page=100&page=1
      JsonNode responseBodyJson = grouperHttpClient.retrieveJsonNode();
      
      
      if (responseBodyJson == null || responseBodyJson instanceof NullNode || !(responseBodyJson instanceof ArrayNode)) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Cannot find your user in GitLab. Please make sure you have linked your account.");
        return;
      }
      
      
      if (GrouperUtil.length(responseBodyJson) == 0) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Cannot find your user in GitLab. Please make sure you have linked your account.");
        return;
      }
      
      // lets see if the externUid is set correctly
      // [{"id":3079259,"username":"mchyzer","name":"Chris Hyzer","identities":[{"provider":"group_saml","extern_uid":"1c6a125f8654a16eb96966ffdb64db34","saml_provider_id":5190}]}]
      
      ArrayNode identities = (ArrayNode)responseBodyJson.get(0).get("identities");
      if (identities == null || identities.size() == 0) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Cannot find your user in GitLab. Please make sure you have linked your account.");
        return;
      }
      String externUid = null;
      for (JsonNode identity : identities) {
        if (identity.get("saml_provider_id") != null && identity.get("saml_provider_id").asLong() == 5190L) {
          externUid = identity.get("extern_uid").asText();
        }
      }
      if (StringUtils.isBlank(externUid)) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Cannot find your user in GitLab. Please make sure you have linked your account.");
        return;
      }
      
      if (!StringUtils.equals(currentUserSubject.getId(), externUid)) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Your linked ID is not your PennID: '" + externUid + "'.  You need to unlink your account and relink your account.");
        return;
      }
            
      Map<Long, String> accessLevelToLabel = new HashMap<>();

      accessLevelToLabel.put(0L, "No_access");
      accessLevelToLabel.put(5L, "Minimal_access");
      accessLevelToLabel.put(10L, "Guest");
      accessLevelToLabel.put(20L, "Reporter");
      accessLevelToLabel.put(30L, "Developer");
      accessLevelToLabel.put(40L, "Maintainer");
      accessLevelToLabel.put(50L, "Owner");
      
      // lets do projects first, then groups
      for (int i=0;i<3;i++) {
        
        for (Object[] objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPath : objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPaths) {
          
          Long objectId = GrouperUtil.longValue(objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPath[0]);
          String objectType = (String)objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPath[1];
          Long accessLevel = GrouperUtil.longValue(objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPath[2]);
          String fullPath = (String)objectIdObjectTypeAccessLevelUserIdPennIdExternUidUsernameFullPath[3];
          
          boolean mainGroup = StringUtils.equals(objectType, "group") && 69205688L == objectId.longValue();
          
          if (i==0 && !mainGroup) {
            continue;
          } else if (i==1 && (mainGroup || StringUtils.equals(objectType, "group"))) {
            continue;
          } else if (i==2 && (mainGroup || !StringUtils.equals(objectType, "group"))) {
            continue;
          }
          
          grouperHttpClient = new GrouperHttpClient();
          WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId);
          
          // PUT https://gitlab.com/api/v4/groups/69205688/members/3079259?access_level=50
          try {
            grouperHttpClient.assignGrouperHttpMethod("PUT").assignDebugMap(debugMap).
              assignUrl(baseUrl + "/api/v4/" + objectType + "s/" + objectId + "/members/" + userId + "?access_level=" + accessLevel).executeRequest(); 
            if (grouperHttpClient.getResponseCode() == 404) {
              // POST https://gitlab.com/api/v4/groups/69205688/members?user_id=3079259&access_level=50
              grouperHttpClient = new GrouperHttpClient();
              WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId);
              grouperHttpClient.assignGrouperHttpMethod("POST").assignDebugMap(debugMap).assignAssertResponseCode(201).
                assignUrl(baseUrl + "/api/v4/" + objectType + "s/" + objectId + "/members?user_id=" + userId + "&access_level=" + accessLevel).executeRequest(); 
            } else if (grouperHttpClient.getResponseCode() != 200) {
              throw new RuntimeException("Cant update membership! " + objectType + ", " + fullPath + ", " + " with access level: " + accessLevelToLabel.get(accessLevel));
            }
            gsh_builtin_gshTemplateOutput.addOutputLine("Success adding you to " + objectType + " " + fullPath + " with access level: " + accessLevelToLabel.get(accessLevel));
          } catch (Exception e) {
            if (grouperHttpClient.getResponseBody().contains("should be greater than or equal to")) {
              gsh_builtin_gshTemplateOutput.addOutputLine("Warning adding you to " + objectType + " " + fullPath + " with access level: " + accessLevelToLabel.get(accessLevel) +
                  ", note: this is probably ok since has higher rights than the enclosing group");
            } else {
              gsh_builtin_gshTemplateOutput.addOutputLine("Error adding you to " + objectType + " " + fullPath + " with access level: " + accessLevelToLabel.get(accessLevel));
            }
          }
        }

      }
      
      gsh_builtin_gshTemplateOutput.addOutputLine("Gitlab membership sync complete.");

      // Remove user from penn_isc_gitlab_mship_delicense after adding memberships back
      new GcDbAccess().sql("""
        delete from penn_isc_gitlab_mship_delicense where penn_id = ?
      """).addBindVar(currentUserSubject.getId()).executeSql();

    } else {
      gsh_builtin_gshTemplateOutput.addOutputLine("You must specify an action of claimLicense or populatePrivileges: '" + gsh_input_action + "'");
      return;
    }
        
  }
  
  public static void main(String[] args) {

    GrouperStartup.startup();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        // pgtest1
        Subject subject = SubjectFinder.findByIdAndSource("87080839", "pennperson", true);

        Test94GitlabMembershipFixer test94GitlabMembershipFixer = new Test94GitlabMembershipFixer();
        GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();
        
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_action", "populatePrivileges");
        
        gshTemplateV2input.setGsh_builtin_subject(subject);
        GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
        gshTemplateRuntime.setTemplateConfigId("gitlabMembershipFixer");
        gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(gshTemplateRuntime);

        GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();
        
        test94GitlabMembershipFixer.gshRunLogic(gshTemplateV2input, gshTemplateV2output);
        
        System.out.println(gshTemplateV2output.getGsh_builtin_gshTemplateOutput());

        return null;
      }
    });
    
    System.exit(0);

  }

}
```
