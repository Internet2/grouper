---
title: "Grouper GSH script daemon loader for attributes and values"
space: Grouper
pageId: 28549697
version: 5
lastUpdated: 2026-07-01T05:41:21.575Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549697/Grouper+GSH+script+daemon+loader+for+attributes+and+values
---

This example has a SQL query to sync attribute definition assignments and values to groups

## Create the definition

## Create the attribute

## Make a query

This is a sample table but it could be a view

## Make a GSH script daemon

1. Choose "Script daemon" as daemon type
2. Choose "GSH" as script type

## Script

```
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class Test98attributeLoader {

  public static void runLoader() {
    
    // some constants
    final String attributeName = "testAttr:myAttr";
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    try {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeName, true);
          
          // get data from SQL
          List<Object[]> groupNameAttributeValueDbArray = new GcDbAccess().connectionName("grouper").
            sql("select group_name, attribute_value from attributes_and_values where attribute_value is not null").
            selectList(Object[].class);
      
          hib3GrouperLoaderLog.addTotalCount(GrouperUtil.length(groupNameAttributeValueDbArray));
          debugMap.put("rowsFromDb", GrouperUtil.length(groupNameAttributeValueDbArray));

          // convert this to multikey which has .equals, so we can do set math
          Set<MultiKey> groupNameAttributeValuesFromDb = new HashSet<>();
          Set<String> groupNamesFromDb = new HashSet<>();
          for (Object[] groupNameAttributeValue: GrouperUtil.nonNull(groupNameAttributeValueDbArray)) {
            groupNameAttributeValuesFromDb.add(new MultiKey(groupNameAttributeValue[0], groupNameAttributeValue[1]));
            String groupName = (String)groupNameAttributeValue[0];
            
            // this is single assign only
            if (!groupNamesFromDb.add(groupName)) {
              debugMap.put("errorMultipleFromSource", groupName);
              throw new RuntimeException("Multiple assignments from source for group: " + groupName);
            }
          }
      
          // get data from grouper
          List<Object[]> groupNameAttributeValueGrouperArray = new GcDbAccess().connectionName("grouper").
              sql("select gaagv.group_name, gaagv.value_string from grouper_aval_asn_group_v gaagv where gaagv.attribute_def_name_name = ?").
              addBindVar(attributeName).selectList(Object[].class);

          debugMap.put("rowsFromGrouper", GrouperUtil.length(groupNameAttributeValueGrouperArray));

          // convert this to multikey which has .equals, so we can do set math
          Set<MultiKey> groupNameAttributeValuesFromGrouper = new HashSet<>();
          Set<String> groupNamesFromGrouper = new HashSet<>();

          for (Object[] groupNameAttributeValue: GrouperUtil.nonNull(groupNameAttributeValueGrouperArray)) {
            groupNameAttributeValuesFromGrouper.add(new MultiKey(groupNameAttributeValue[0], groupNameAttributeValue[1]));
            groupNamesFromGrouper.add((String)groupNameAttributeValue[0]);
          }
      
          // inserts
          Set<String> insertGroupNames = new HashSet<String>(groupNamesFromDb);
          insertGroupNames.removeAll(groupNamesFromGrouper);
          
          int logCount = 0;
          
          for (MultiKey groupNameAttributeValue : groupNameAttributeValuesFromDb) {
            String groupName = (String)groupNameAttributeValue.getKey(0);
            if (!insertGroupNames.contains(groupName)) {
              continue;
            }
            String attributeValue = (String)groupNameAttributeValue.getKey(1);
            
            Group group = GroupFinder.findByName(groupName, false);
            if (group == null) {
              hib3GrouperLoaderLog.addUnresolvableSubjectCount(1);
              debugMap.put("groupNotFound_" + groupName, true);
              continue;
            }
            hib3GrouperLoaderLog.addInsertCount(1);
            group.getAttributeValueDelegate().assignValue(attributeName, attributeValue);
            
            if (logCount++ < 10) {
              debugMap.put("insert_" + logCount, groupName + ": " + attributeValue);
              
            }
          }
          
          // deletes
          Set<String> deleteGroupNames = new HashSet<String>(groupNamesFromGrouper);
          deleteGroupNames.removeAll(groupNamesFromDb);

          logCount = 0;
          for (MultiKey groupNameAttributeValue : groupNameAttributeValuesFromGrouper) {
            String groupName = (String)groupNameAttributeValue.getKey(0);
            if (!deleteGroupNames.contains(groupName)) {
              continue;
            }
            String attributeValue = (String)groupNameAttributeValue.getKey(1);
            
            Group group = GroupFinder.findByName(groupName, true);
            hib3GrouperLoaderLog.addDeleteCount(1);
            group.getAttributeDelegate().removeAttribute(attributeDefName);
            if (logCount++ < 10) {
              debugMap.put("delete_" + logCount, groupName + ": " + attributeValue);
              
            }
          }

          // updates
          logCount = 0;

          for (MultiKey groupNameAttributeValue : groupNameAttributeValuesFromDb) {
            if (groupNameAttributeValuesFromGrouper.contains(groupNameAttributeValue)) {
              continue;
            }
            String groupName = (String)groupNameAttributeValue.getKey(0);
            if (insertGroupNames.contains(groupName)) {
              continue;
            }
            String attributeValue = (String)groupNameAttributeValue.getKey(1);
            
            Group group = GroupFinder.findByName(groupName, true);
            hib3GrouperLoaderLog.addUpdateCount(1);
            group.getAttributeValueDelegate().assignValue(attributeName, attributeValue);
            
            if (logCount++ < 10) {
              debugMap.put("update_" + logCount, groupName + ": " + attributeValue);
              
            }

          }

          return null;
        }
      });
      
      
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      String debugMapForLog = GrouperUtil.toStringForLog(debugMap);
      hib3GrouperLoaderLog.setJobMessage(debugMapForLog);
      
      // if we are running this locally
      if (OtherJobScript.retrieveFromThreadLocal() == null) {
        System.out.println(debugMapForLog);
        System.exit(0);
      }
    }
    
  }

  public static void main(String[] args) {
    runLoader();
  }

}

// comment this out to run locally
Test98attributeLoader.runLoader();
```
