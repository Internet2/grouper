---
title: "Grouper custom template via GSH invoked by daemon - load group attributes"
space: Grouper
pageId: 28548143
version: 8
lastUpdated: 2026-07-01T05:45:12.599Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548143/Grouper+custom+template+via+GSH+invoked+by+daemon+-+load+group+attributes
---

For this POC make a Folder, Groups, and Attribute names. Note the attribute is single-assign, single-valued, with String values

Make a query that assigned some attributes to groups, make a view (note this is postgres , you can adjust for other DBs)

```
create view test_group_attr_loader as
select 'test:testAttributeLoader:testAttrGroup1' as group_name, 'abc123' as attr1val, 'def456' as attr2val, null as attr3val
union
select 'test:testAttributeLoader:testAttrGroup2' as group_name, 'prq789' as attr1val, null as attr2val, 'mno654' as attr3val
union
select 'test:testAttributeLoader:testAttrGroup3' as group_name, null as attr1val, 'def321' as attr2val, 'rst514' as attr3val
;
```

## GSH template for loader

Make a GSH template, input the database, query, group name col, comma separated attr cols, comma separated attr def names (corresponds to cols)

Config (if you want to import instead of UI)

```
grouperGshTemplate.groupAttributeLoader.displayErrorOutput = true
grouperGshTemplate.groupAttributeLoader.folderShowOnDescendants = certainFolder
grouperGshTemplate.groupAttributeLoader.folderShowType = certainFolder
grouperGshTemplate.groupAttributeLoader.folderUuidToShow = d1b48207b30e4f92ab2d469212e2680e
grouperGshTemplate.groupAttributeLoader.input.0.defaultValue = grouper
grouperGshTemplate.groupAttributeLoader.input.0.description = External system config id for database.
grouperGshTemplate.groupAttributeLoader.input.0.label = Database
grouperGshTemplate.groupAttributeLoader.input.0.name = gsh_input_database
grouperGshTemplate.groupAttributeLoader.input.0.type = string
grouperGshTemplate.groupAttributeLoader.input.0.validationBuiltin = alphaNumericUnderscore
grouperGshTemplate.groupAttributeLoader.input.0.validationType = builtin
grouperGshTemplate.groupAttributeLoader.input.1.description = SQL select statement with group name, and attributes as columns.  Values are the data
grouperGshTemplate.groupAttributeLoader.input.1.label = SQL query
grouperGshTemplate.groupAttributeLoader.input.1.name = gsh_input_query
grouperGshTemplate.groupAttributeLoader.input.1.required = true
grouperGshTemplate.groupAttributeLoader.input.1.type = string
grouperGshTemplate.groupAttributeLoader.input.1.validationMessage = Query must start with 'select' (lower case)
grouperGshTemplate.groupAttributeLoader.input.1.validationRegex = ^select .*$
grouperGshTemplate.groupAttributeLoader.input.1.validationType = regex
grouperGshTemplate.groupAttributeLoader.input.2.defaultValue = group_name
grouperGshTemplate.groupAttributeLoader.input.2.description = Column name that has full group system name
grouperGshTemplate.groupAttributeLoader.input.2.label = Group column name
grouperGshTemplate.groupAttributeLoader.input.2.maxLength = 32
grouperGshTemplate.groupAttributeLoader.input.2.name = gsh_input_groupColumnName
grouperGshTemplate.groupAttributeLoader.input.2.type = string
grouperGshTemplate.groupAttributeLoader.input.2.validationBuiltin = alphaNumericUnderscore
grouperGshTemplate.groupAttributeLoader.input.2.validationType = builtin
grouperGshTemplate.groupAttributeLoader.input.3.description = Comma separated ordered column names with attribute values
grouperGshTemplate.groupAttributeLoader.input.3.label = Attribute column names
grouperGshTemplate.groupAttributeLoader.input.3.name = gsh_input_attributeColumnNames
grouperGshTemplate.groupAttributeLoader.input.3.required = true
grouperGshTemplate.groupAttributeLoader.input.3.type = string
grouperGshTemplate.groupAttributeLoader.input.3.validationMessage = Comma separated column names
grouperGshTemplate.groupAttributeLoader.input.3.validationRegex = ^[a-zA-Z0-9_, ]+$
grouperGshTemplate.groupAttributeLoader.input.3.validationType = regex
grouperGshTemplate.groupAttributeLoader.input.4.description = Comma separated system names of attribute def names that match up with the attribute columns
grouperGshTemplate.groupAttributeLoader.input.4.label = Names of attribute def names
grouperGshTemplate.groupAttributeLoader.input.4.name = gsh_input_namesOfAttributeDefNames
grouperGshTemplate.groupAttributeLoader.input.4.required = true
grouperGshTemplate.groupAttributeLoader.input.4.type = string
grouperGshTemplate.groupAttributeLoader.input.4.validationType = none
grouperGshTemplate.groupAttributeLoader.input.5.defaultValue = grouper
grouperGshTemplate.groupAttributeLoader.input.5.description = Database to run group query
grouperGshTemplate.groupAttributeLoader.input.5.label = Group query database
grouperGshTemplate.groupAttributeLoader.input.5.name = gsh_input_groupQueryDatabase
grouperGshTemplate.groupAttributeLoader.input.5.type = string
grouperGshTemplate.groupAttributeLoader.input.5.validationBuiltin = alphaNumericUnderscore
grouperGshTemplate.groupAttributeLoader.input.5.validationType = builtin
grouperGshTemplate.groupAttributeLoader.input.6.description = Enter a SQL query that has the group name column configured above.  This will remove attributes to groups not returned in the attribute query.
grouperGshTemplate.groupAttributeLoader.input.6.label = Group query
grouperGshTemplate.groupAttributeLoader.input.6.name = gsh_input_groupQuery
grouperGshTemplate.groupAttributeLoader.input.6.type = string
grouperGshTemplate.groupAttributeLoader.input.6.validationRegex = ^select .*$
grouperGshTemplate.groupAttributeLoader.input.6.validationType = regex
grouperGshTemplate.groupAttributeLoader.moreActionsLabel = Group attribute loader
grouperGshTemplate.groupAttributeLoader.numberOfInputs = 7
grouperGshTemplate.groupAttributeLoader.runAsType = GrouperSystem
grouperGshTemplate.groupAttributeLoader.runGshInTransaction = false
grouperGshTemplate.groupAttributeLoader.securityRunType = wheel
grouperGshTemplate.groupAttributeLoader.showInMoreActions = true
grouperGshTemplate.groupAttributeLoader.showOnFolders = true
grouperGshTemplate.groupAttributeLoader.templateDescription = Loader attributes and values on groups
grouperGshTemplate.groupAttributeLoader.templateName = Group attribute loader
grouperGshTemplate.groupAttributeLoader.useIndividualAudits = false

```

## GSH loader script

(for 2.5.48+ but can be adjusted for previous versions)

```
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.*;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

//// uncomment here and at bottom to run in eclipse
//public class Test7 {
//
//  public static void main(String[] args) {
//    
//    GrouperGroovyRuntime grouperGroovyRuntime = new GrouperGroovyRuntime();
//    GshTemplateOutput gsh_builtin_gshTemplateOutput = GshTemplateOutput.retrieveGshTemplateOutput();
//    GshTemplateRuntime gsh_builtin_gshTemplateRuntime = GshTemplateRuntime.retrieveGshTemplateRuntime();
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
//
//    String gsh_input_database = "grouper";
//    String gsh_input_query = "select group_name, attr1val, attr2val, attr3val from test_group_attr_loader";
//    String gsh_input_groupColumnName = null;
//    String gsh_input_attributeColumnNames = "attr1val, attr2val, attr3val";
//    String gsh_input_namesOfAttributeDefNames = "test:testAttributeLoader:testAttr1,test:testAttributeLoader:testAttr2,test:testAttributeLoader:testAttr3";
//    String gsh_input_groupQuery = "select name as group_name from grouper_groups gg where gg.name like 'test:testAttributeLoader:%'";
//    String gsh_input_groupQueryDatabase = null;
    
    // default to grouper (should already be done in template)
    gsh_input_database = GrouperUtil.defaultIfBlank(gsh_input_database, "grouper");
    gsh_input_groupQueryDatabase = GrouperUtil.defaultIfBlank(gsh_input_groupQueryDatabase, "grouper");
    gsh_input_groupColumnName = GrouperUtil.defaultIfBlank(gsh_input_groupColumnName, "group_name");

    String[] namesOfAttributeDefNamesArray = GrouperUtil.splitTrim(gsh_input_namesOfAttributeDefNames, ",");
    String[] attributeColumnNamesArray = GrouperUtil.splitTrim(gsh_input_attributeColumnNames, ",");
    
    boolean hasGroupQuery = !StringUtils.isBlank(gsh_input_groupQuery);

    // figure out which column index is which
    GcTableSyncTableMetadata metadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(gsh_input_database, gsh_input_query);
    GcTableSyncTableMetadata groupMetadata = hasGroupQuery ? GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(gsh_input_groupQueryDatabase, gsh_input_groupQuery) : null;
    
    //index target data into groupName, attributeName, attributeValue
    Map<MultiKey, String> targetGroupNameAttributeNameToAttributeValue = new HashMap<MultiKey, String>();
    RETRIEVE_TARGET_DATA: {

      // get target data
      List<Object[]> rows = new GcDbAccess().connectionName(gsh_input_database).sql(gsh_input_query).selectList(Object[].class);
      int groupColumnIndex = metadata.lookupColumn(gsh_input_groupColumnName, true).getColumnIndexZeroIndexed();
      for (int attributeIndex = 0; attributeIndex < namesOfAttributeDefNamesArray.length; attributeIndex++) {
        String attributeName = namesOfAttributeDefNamesArray[attributeIndex];
        String columnName = attributeColumnNamesArray[attributeIndex];
        
        int attributeColumnIndex = metadata.lookupColumn(columnName, true).getColumnIndexZeroIndexed();
        for (Object[] row : rows) {
          String groupName = (String)row[groupColumnIndex];
          String attributeValue = (String)row[attributeColumnIndex];
          if (!StringUtils.isBlank(attributeValue)) {
            targetGroupNameAttributeNameToAttributeValue.put(new MultiKey(groupName, attributeName), attributeValue);
          }
        }
      }
      grouperGroovyRuntime.debugMap("targetAssignments", GrouperUtil.length(targetGroupNameAttributeNameToAttributeValue));
    }
    
    // get a list of group names from results
    Set<String> groupNames = new HashSet<String>();
    for (MultiKey targetGroupNameAttributeNameAttributeValue : targetGroupNameAttributeNameToAttributeValue.keySet()) {
      String groupName = (String)targetGroupNameAttributeNameAttributeValue.getKey(0);
      groupNames.add(groupName);
    }
    // add group names from group query
    if (hasGroupQuery) {
      List<Object[]> rows = new GcDbAccess().connectionName(gsh_input_groupQueryDatabase).sql(gsh_input_groupQuery).selectList(Object[].class);
      grouperGroovyRuntime.debugMap("targetGroupQueryRows", GrouperUtil.length(rows));
      int groupColumnIndex = groupMetadata.lookupColumn(gsh_input_groupColumnName, true).getColumnIndexZeroIndexed();
      for (Object[] row : rows) {
        String groupName = (String)row[groupColumnIndex];
        groupNames.add(groupName);
      }
    }
    grouperGroovyRuntime.debugMap("groupNames", GrouperUtil.length(groupNames));

    // get attribute assignments from grouper
    Map<MultiKey, String> grouperGroupNameAttributeNameToAttributeValue = new HashMap<MultiKey, String>();
    RETRIEVE_GROUPER_DATA: {
      // we need to get groups in batches
      List<String> groupNamesList = new ArrayList<String>(groupNames);
      int batchSize = 200;
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupNamesList, batchSize);
      for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
        List<String> batchListGroupNames = GrouperUtil.batchList(groupNamesList, batchSize, batchIndex);

        // select * from grouper_aval_asn_group_v gaagv where gaagv.attribute_def_name_name in 
        // ('test:testAttributeLoader:testAttr1', 'test:testAttributeLoader:testAttr2', 'test:testAttributeLoader:testAttr3')
        // and gaagv.group_name in ('test:testAttributeLoader:testAttrGroup1', 'test:testAttributeLoader:testAttrGroup2', 
        // 'test:testAttributeLoader:testAttrGroup3') and gaagv.enabled='T'
        String sql = "select gaagv.group_name, gaagv.attribute_def_name_name, value_string from grouper_aval_asn_group_v gaagv where gaagv.attribute_def_name_name in  ( " +
            GrouperClientUtils.appendQuestions(namesOfAttributeDefNamesArray.length) + ") and gaagv.group_name in (" + GrouperClientUtils.appendQuestions(batchListGroupNames.size()) + ")";
        GcDbAccess gcDbAccess = new GcDbAccess().sql(sql);
        for (String nameOfAttributeDefName : namesOfAttributeDefNamesArray) {
          gcDbAccess.addBindVar(nameOfAttributeDefName);
        }
        for (String groupName : batchListGroupNames) {
          gcDbAccess.addBindVar(groupName);
        }
        List<Object[]> rows = gcDbAccess.selectList(Object[].class);
        for (Object[] row : rows) {
          if (!StringUtils.isBlank((String)row[2])) {
            grouperGroupNameAttributeNameToAttributeValue.put(new MultiKey(row[0], row[1]), (String)row[2]);
          }
        }
      }
      grouperGroovyRuntime.debugMap("grouperAssignments", GrouperUtil.length(grouperGroupNameAttributeNameToAttributeValue));
    }
    
    int groupNotFoundCount = 0;
    
    //process inserts
    INSERTS: {
      Set<MultiKey> insertGroupNameAttributeNames = new HashSet<MultiKey>(targetGroupNameAttributeNameToAttributeValue.keySet());
      insertGroupNameAttributeNames.removeAll(grouperGroupNameAttributeNameToAttributeValue.keySet());
      grouperGroovyRuntime.debugMap("inserts", GrouperUtil.length(insertGroupNameAttributeNames));
      for (MultiKey groupNameAttributeName : insertGroupNameAttributeNames) {
        String groupName = (String)groupNameAttributeName.getKey(0);
        String attributeName = (String)groupNameAttributeName.getKey(1);
        String attributeValue = targetGroupNameAttributeNameToAttributeValue.get(groupNameAttributeName);
        Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false);
        // uh... ignore if group not there???
        if (group != null) {
          group.getAttributeValueDelegate().assignValue(attributeName, attributeValue);
        } else {
          groupNotFoundCount++;
        }
      }
    }

    //process updates
    UPDATES: {
      Set<MultiKey> updateGroupNameAttributeNames = new HashSet<MultiKey>();
      for (MultiKey groupNameAttributeName :  targetGroupNameAttributeNameToAttributeValue.keySet()) {
        String targetValue = targetGroupNameAttributeNameToAttributeValue.get(groupNameAttributeName);
        String grouperValue = grouperGroupNameAttributeNameToAttributeValue.get(groupNameAttributeName);
        if (!StringUtils.isBlank(grouperValue) && !StringUtils.equals(targetValue, grouperValue)) {
          updateGroupNameAttributeNames.add(groupNameAttributeName);
        }
      }
      grouperGroovyRuntime.debugMap("updates", GrouperUtil.length(updateGroupNameAttributeNames));
      for (MultiKey groupNameAttributeName : updateGroupNameAttributeNames) {
        String groupName = (String)groupNameAttributeName.getKey(0);
        String attributeName = (String)groupNameAttributeName.getKey(1);
        String attributeValue = targetGroupNameAttributeNameToAttributeValue.get(groupNameAttributeName);
        Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false);
        // uh... ignore if group not there???  but this one should be there, we just queried it!
        if (group != null) {
          group.getAttributeValueDelegate().assignValue(attributeName, attributeValue);
        } else {
          groupNotFoundCount++;
        }
      }
    }

    //process deletes
    DELETES: {
      Set<MultiKey> deleteGroupNameAttributeNames = new HashSet<MultiKey>(grouperGroupNameAttributeNameToAttributeValue.keySet());
      deleteGroupNameAttributeNames.removeAll(targetGroupNameAttributeNameToAttributeValue.keySet());
      grouperGroovyRuntime.debugMap("deletes", GrouperUtil.length(deleteGroupNameAttributeNames));
      for (MultiKey groupNameAttributeName : deleteGroupNameAttributeNames) {
        String groupName = (String)groupNameAttributeName.getKey(0);
        String attributeName = (String)groupNameAttributeName.getKey(1);
        Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false);
        // uh... ignore if group not there???  but this one should be there, we just queried it!
        if (group != null) {
          group.getAttributeDelegate().removeAttributeByName(attributeName);
        } else {
          groupNotFoundCount++;
        }
      }
    }
    grouperGroovyRuntime.debugMap("groupNotFoundCount", groupNotFoundCount);

//    System.out.println(GrouperUtil.mapToString(grouperGroovyRuntime.getDebugMap()));
//  }
//}

```

## Add a script daemon

## GSH script for daemon

```
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOwnerType;
import edu.internet2.middleware.grouper.app.gsh.template.GshValidationLine;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.misc.GrouperStartup;

//// uncomment to run in eclipse
//public class Test8 {
//
//  public static void main(String[] args) {
//
//    GrouperStartup.startup();
//    GrouperSession.startRootSession();
    
    
    GshTemplateExec gshTemplateExec = new GshTemplateExec();
    gshTemplateExec.assignConfigId("groupAttributeLoader");
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_database").assignValueString("grouper"));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_query").assignValueString("select group_name, attr1val, attr2val, attr3val from test_group_attr_loader"));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_groupColumnName").assignValueString("group_name"));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_attributeColumnNames").assignValueString("attr1val, attr2val, attr3val"));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_namesOfAttributeDefNames").assignValueString("test:testAttributeLoader:testAttr1,test:testAttributeLoader:testAttr2,test:testAttributeLoader:testAttr3"));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_groupQuery").assignValueString("select name as group_name from grouper_groups gg where gg.name like 'test:testAttributeLoader:%'"));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_groupQueryDatabase").assignValueString("grouper"));
    gshTemplateExec.assignOwnerStemName("test:testAttributeLoader");
    gshTemplateExec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    gshTemplateExec.assignCurrentUser(SubjectFinder.findRootSubject());
    GshTemplateExecOutput gshTemplateExecOutput = gshTemplateExec.execute();

    String result = gshTemplateExecOutput.getGshScriptOutput();
    
    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(result);

    if (!gshTemplateExecOutput.isValid()) {

      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setStatus("ERROR");
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage("\n");
      
      for (GshValidationLine gshValidationLine : gshTemplateExecOutput.getGshTemplateOutput().getValidationLines()) {
        
        String validationLine = (gshValidationLine.getInputName() == null ? "" : (gshValidationLine.getInputName() + ": ")) + gshValidationLine.getText() + "\n";
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(validationLine);
      }
      
    } else {
    
      if (gshTemplateExecOutput.isSuccess()) {
        int total = (Integer)gshTemplateExecOutput.getGrouperGroovyResult().getDebugMap().get("targetAssignments");
        int inserts = (Integer)gshTemplateExecOutput.getGrouperGroovyResult().getDebugMap().get("inserts");
        int updates = (Integer)gshTemplateExecOutput.getGrouperGroovyResult().getDebugMap().get("updates");
        int deletes = (Integer)gshTemplateExecOutput.getGrouperGroovyResult().getDebugMap().get("deletes");
        
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setStatus("SUCCESS");
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addTotalCount(total);
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(inserts);
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addUpdateCount(updates);
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(deletes);
      } else {
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setStatus("ERROR");
      }
  
      
    }    

    System.out.println(result);
//  }
//
//}

```

## See the attributes loaded
