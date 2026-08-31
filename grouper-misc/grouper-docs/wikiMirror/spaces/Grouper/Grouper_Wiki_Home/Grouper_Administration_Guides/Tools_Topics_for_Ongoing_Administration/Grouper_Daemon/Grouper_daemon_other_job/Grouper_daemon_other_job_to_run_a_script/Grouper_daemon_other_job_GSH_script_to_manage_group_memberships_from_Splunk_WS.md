---
title: "Grouper daemon \"other job\" GSH script to manage group memberships from Splunk WS"
space: Grouper
pageId: 28560387
version: 6
lastUpdated: 2026-07-01T05:35:38.646Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560387/Grouper+daemon+other+job+GSH+script+to+manage+group+memberships+from+Splunk+WS
---

This integration will load a group with users who have used a weblogin app recently (number of days back is configurable, max 60)

There are two use case for this, but could be used or other things

1. Generate a list of recent users for an application for notifications when there is maintenance (the Banner team has requested this past)
2. Manage licenses for Atlassian so that users who do not have a perpetual license, and who have not used the app, will lose their license and they can opt in (claim license) again when they need it in the future

## Assign a group to be loaded

This is done via provisioner configuration as a convenience, but this is not a provisioner, and some provisioner features will not be available like other provisioners.

1. You need the entity ID of the SAML service provider. You can find this in Firefox SAML tracer (or ask the weblogin team if you cannot do that). In the example below it is "[https://auth.atlassian.com/saml/fadcfa78-0eec-4ebb-89ca-db7b381e0f41](https://auth.atlassian.com/saml/fadcfa78-0eec-4ebb-89ca-db7b381e0f41)"
2. Go to Group in PennGroups and click Group options → Provisioning → Edit provisioning settings, and fill out the form
3. The daemon runs hourly, so wait for that, or kick it off

## Daemon configuration

The daemon will

1. See which groups are marked to be loaded
2. Get the configuration
3. Call a splunk web service based on entity id and days back, and get the users
4. Replace the membership of the group with the list from splunk (add/remove as needed)
5. Set some numbers in the logs

The credentials are in grouper-loader.properties

Daemon configuration

```
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
import org.apache.commons.lang3.StringUtils;
 
import com.fasterxml.jackson.databind.JsonNode;
 
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.subject.Subject;
 
//public class Test56splunk {
//
// 
//  public static void main(String[] args) {
     
 
    Timestamp start = new Timestamp(System.currentTimeMillis());
    GrouperSession grouperSession = GrouperSession.startRootSession();
 
    String rootStemName = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc");
     
    Map<String, Object> debugMap = new LinkedHashMap<>();
 
    String groupQuery = "SELECT gaaagv.group_name, gaaagv.value_string FROM grouper_aval_asn_asn_group_v gaaagv " +
      " where gaaagv.attribute_def_name_name2 = '" + rootStemName + ":provisioning:provisioningMetadataJson' " +
      " and gaaagv.group_name in ( " +
      " SELECT gaaagv2.group_name FROM grouper_aval_asn_asn_group_v gaaagv2 " +
      " where gaaagv2.attribute_def_name_name2 = '" + rootStemName + ":provisioning:provisioningDoProvision' " +
      " and gaaagv2.value_string = 'webloginServiceProviderRecentUsersToGroup') ";
    List<Object[]> groupNamesMetadata = new GcDbAccess().sql(groupQuery).selectList(Object[].class);
     
    List<MultiKey> splunkGroupsDaysEntities = new ArrayList<>();
     
    for (Object[] groupNameMetadata : GrouperUtil.nonNull(groupNamesMetadata)) {
      String groupName = (String)groupNameMetadata[0];
      String metadata = (String)groupNameMetadata[1];
      JsonNode metadataNode = GrouperUtil.jsonJacksonNode(metadata);
      int days = GrouperUtil.intValue(GrouperUtil.jsonJacksonGetInteger(metadataNode, "md_days"));
      String entityId = GrouperUtil.jsonJacksonGetString(metadataNode, "md_entityId");
      MultiKey splunkGroupsDaysEntity = new MultiKey(groupName, days, entityId);
      splunkGroupsDaysEntities.add(splunkGroupsDaysEntity);
    }
     
    // list the groups, days back, and entity id
    //    MultiKey atlassianCloud = new MultiKey("penn:isc:ait:apps:atlassian:helperGroups:recentAtlassianCloudUsers", 60,
    //        "https://auth.atlassian.com/saml/fadcfa78-0eec-4ebb-89ca-db7b381e0f41");
         
    String splunkBaseUrl = GrouperUtil.stripLastSlashIfExists(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("splunk.url"));
    String splunkUser = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("splunk.user");
    String splunkPass = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("splunk.pass");
 
    debugMap.put("webloginLoadedGroupCount", splunkGroupsDaysEntities.size());
     
    // keep track of all pennkeys for user size of job
    Set<String> allPennkeys = new HashSet<String>();
 
    // keep track of memberships for membership size of job
    int membershipCount = 0;
 
    // loop through the groups that are loaded by splunk recent users logs
    for (MultiKey splunkGroupsDaysEntity : splunkGroupsDaysEntities) {
 
      String groupName = (String)splunkGroupsDaysEntity.getKey(0);
      Group group = GroupFinder.findByName(grouperSession, groupName, false);
      if (group == null) {
        // ignore if group not found, dont fail all other jobs
        debugMap.put("group_" + groupName, "notFound");
        continue;
      }
      int days = GrouperUtil.intValue(splunkGroupsDaysEntity.getKey(1));
      debugMap.put("group_" + groupName + "_days", days);
 
      String entityId = (String)splunkGroupsDaysEntity.getKey(2);
      debugMap.put("group_" + groupName + "_entityId", entityId);
       
      // https://splunk.school.edu/servicesNS/-/isc_attribution/search/jobs/export?search=search%20%60sso_unique_users_by_entity(https%3A%2F%2Fauth.atlassian.com%2Fsaml%2Ffadcfa78-0eec-4ebb-89ca-db7b381e0f41)%60&exec_mode=oneshot&output_mode=json&earliest_time=-60d%40d
      String url = splunkBaseUrl + "/servicesNS/-/isc_attribution/search/jobs/export";
       
      Set<String> pennkeys = new HashSet<String>();
 
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().
          assignGrouperHttpMethod(GrouperHttpMethod.get).
          addHeader("Content-Type", "application/json").
          addHeader("Accept", "application/json").
          assignUser(splunkUser).
          assignPassword(splunkPass).assignUrl(url).
          addUrlParameter("search", "search `sso_unique_users_by_entity(" + entityId + ")`").
          addUrlParameter("output_mode", "json").
          addUrlParameter("earliest_time", "-" + days + "d@d").
          addUrlParameter("exec_mode", "oneshot").
          executeRequest();
 
      // if there is not a 200 (success), then continue to next group
      int resonseCode = grouperHttpClient.getResponseCode();
      if (resonseCode != 200) {
        debugMap.put("group_" + groupName + "_responseCode", resonseCode);
        continue;
      }
 
      String body = grouperHttpClient.getResponseBody();
      //  {"preview":false,"offset":0,"result":{"Entity ID":"https://auth.atlassian.com/saml/fadcfa78-0eec-4ebb-89ca-db7b381e0f41","user_id":"knightp","count":"51"}}
      //  {"preview":false,"offset":1,"result":{"Entity ID":"https://auth.atlassian.com/saml/fadcfa78-0eec-4ebb-89ca-db7b381e0f41","user_id":"chieffo","count":"44"}}
      String[] bodyLines = new String[0];
      if (!StringUtils.isBlank(body)) {
          bodyLines = GrouperUtil.splitTrim(body, "\n");
      }
      for (String bodyLine : bodyLines) {
        if (StringUtils.isBlank(bodyLine)) {
          continue;
        }
        JsonNode mainNode = GrouperUtil.jsonJacksonNode(bodyLine);
        JsonNode resultNode = mainNode == null ? null : GrouperUtil.jsonJacksonGetNode(mainNode, "result");
        String pennkey = resultNode == null ? null : GrouperUtil.jsonJacksonGetString(resultNode, "user_id");
        if (!StringUtils.isBlank(pennkey)) {
          pennkeys.add(pennkey);
        }
      }
       
      allPennkeys.addAll(pennkeys);
      membershipCount+=GrouperUtil.length(pennkeys);
       
      // take all the pennkeys and replace the group
      Map<String, Subject> pennkeyToSubject = SubjectFinder.findByIdentifiers(pennkeys, "pennperson");
      debugMap.put("group_" + groupName + "_newMembersSize", pennkeyToSubject.size());
       
      int changes = group.replaceMembers(pennkeyToSubject.values());
      debugMap.put("group_" + groupName + "_changes", changes);
       
      if (OtherJobScript.retrieveFromThreadLocal() != null) {
        // we dont know if these are inserts or updates
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addUpdateCount(changes);
   
      }
    }
    debugMap.put("groupQuery", groupQuery);
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("webloginServiceProviderRecentUsersToGroup");
    gcGrouperSync.setSyncEngine(GcGrouperSync.PROVISIONING);
    gcGrouperSync.setLastFullSyncStart(start);
    Timestamp end = new Timestamp(System.currentTimeMillis());
    gcGrouperSync.setLastFullSyncRun(end);
    gcGrouperSync.setGroupCount(GrouperUtil.length(splunkGroupsDaysEntities));
    gcGrouperSync.setUserCount(GrouperUtil.length(allPennkeys));
    gcGrouperSync.setRecordsCount(membershipCount);
     
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType(GrouperProvisioningType.fullProvisionFull.name());
    gcGrouperSyncJob.setLastSyncStart(start);
    gcGrouperSyncJob.setLastSyncTimestamp(end);
 
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    if (OtherJobScript.retrieveFromThreadLocal() != null) {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));
    } else {
      System.out.println(GrouperUtil.mapToString(debugMap));
    }
     
//  }
// 
//}
```

## Placeholder provisioning configuration

[Documentation on setting up a provisioner for the purposes of the provisionable screen and metadata only](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554518/Using+a+placeholder+provisioner+configuration+for+the+provisionable+metadata+screen)
