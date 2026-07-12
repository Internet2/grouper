---
title: "Grouper GSH script daemon loader from Google sheet"
space: Grouper
pageId: 28548771
version: 2
lastUpdated: 2026-01-15T17:44:12.135Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548771/Grouper+GSH+script+daemon+loader+from+Google+sheet
---

This script daemon will load a group from a google sheet.

You can adjust the authentication, but this simple example will just use an API key to a sheet which can be read from a link

## Setup authentication

Allow google sheet access

Get an API key

## Put the API key in a grouper config

## Make the sheet

## Make a GSH script daemon (adjust code)

Code

```
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

//public class Test84googleSheets {
//  
//  public static void main(String[] args) {
//    
//    GrouperStartup.startup();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Map<String, Object> debugMap = new LinkedHashMap<>();

    String apiKey = GrouperConfig.retrieveConfig().propertyValueStringRequired("someTempGoogleKey");
    
    String jsonResponse = new GrouperHttpClient().assignGrouperHttpMethod(GrouperHttpMethod.get).
      assignUrl("https://sheets.googleapis.com/v4/spreadsheets/1c_abc123/values/Sheet1?key=" + apiKey).
      assignAssertResponseCode(200).executeRequest().getResponseBody();
    
    //  {
    //    "range": "Sheet1!A1:Z1000",
    //    "majorDimension": "ROWS",
    //    "values": [
    //      [
    //        "Users"
    //      ],
    //      [
    //        "mchyzer"
    //      ],
    //      [
    //        "kwilso"
    //      ]
    //    ]
    //  }
    JsonNode jsonNode = GrouperUtil.jsonJacksonNode(jsonResponse);
    ArrayNode arrayNode = GrouperUtil.jsonJacksonGetArrayNode(jsonNode, "values");
    ArrayNode firstRowArray = (ArrayNode)arrayNode.get(0);
    String firstColHeader = GrouperUtil.jsonJacksonGetString(firstRowArray, 0);
    GrouperUtil.assertion(StringUtils.equals(firstColHeader, "Users"), "Header of first col should be 'Users', '" + firstColHeader + "'");
    
    Set<String> subjectIdentifiersGoogleSheet = new HashSet<String>();
    for (int i=1;i<arrayNode.size();i++) {
      ArrayNode rowArray = (ArrayNode)arrayNode.get(i);
      String subjectIdentifier = GrouperUtil.jsonJacksonGetString(rowArray, 0);
      if (!StringUtils.isBlank(subjectIdentifier)) {
        subjectIdentifiersGoogleSheet.add(subjectIdentifier);
      }
    }

    if (OtherJobScript.retrieveFromThreadLocal() != null) {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setTotalCount(subjectIdentifiersGoogleSheet.size());
    }
    
    Map<String, Subject> subjectIdentifierToSubject = GrouperUtil.nonNull(subjectIdentifiersGoogleSheet.size() == 0 ? null : 
      SubjectFinder.findByIdentifiers(subjectIdentifiersGoogleSheet, "pennperson"));
    
    if (subjectIdentifiersGoogleSheet.size() != subjectIdentifierToSubject.size()) {
      if (OtherJobScript.retrieveFromThreadLocal() != null) {
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setUnresolvableSubjectCount(
            subjectIdentifiersGoogleSheet.size() - subjectIdentifierToSubject.size());
      }
      Set<String> unresolvables = new HashSet<String>(subjectIdentifiersGoogleSheet);
      unresolvables.removeAll(subjectIdentifierToSubject.keySet());
      debugMap.put("unresolvables", GrouperUtil.toStringForLog(unresolvables));
    }
    
    Group group = GroupFinder.findByName("test:testGshLoader", true);
    int updates = group.replaceMembers(subjectIdentifierToSubject.values());
    
    if (OtherJobScript.retrieveFromThreadLocal() != null) {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setUpdateCount(updates);
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));
    } else {
      System.exit(0);
    }
    
    GrouperSession.stopQuietly(grouperSession);
    
//  }
//}
```

## See daemon run

## See group memberships

(note make this group before running the daemon)
