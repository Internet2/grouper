/**
 * Copyright 2019 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * @author shilen
 */
public enum UpgradeTasks {
  

  /**
   * add groupAttrRead/groupAttrUpdate group sets for entities
   */
  V1 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV1();
    }
    
  },
  
  /**
   * move subject resolution status attributes to member table
   */
  V2 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV2();
    }
    
  },
  V3{
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV3();
    }
    
  },
  V4{

    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV4();
    }
    
  },
  V5 {

    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV5();
    }
    
  },
  V6 {

    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV6();
    }
    
  },
  V7 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV7();
    }

  },
 V8 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV8();
    }

  }
  ,
  V9{
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV9();
    }

  }, 
  V14{
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV14();
    }

  }, 
  
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V10 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV10();
    }

  }      
  ,
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V11 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV11();
    }

  }
  , 
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V12 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV12();
    }    
    
  }
  ,
  /**
   * make sure source_internal_id is populated in pit tables (fields/members/groups)
   */
  V13 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV13();
    }

  },  
  V29{
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV29();
    }
    
  },
  V33{
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV33();
    }
  }
  ;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTasks.class);

  private static int currentVersion = -1;
  
  /**
   * keep the current version here, increment as things change
   * @return the current version
   */
  public static int currentVersion() {
    if (currentVersion == -1) {
      int max = -1;
      for (UpgradeTasks task : UpgradeTasks.values()) {
        String number = task.name().substring(1);
        int theInt = Integer.parseInt(number);
        max = Math.max(max, theInt);
      }
      currentVersion = max;
    }
    return currentVersion;
  }
  
  public abstract UpgradeTasksInterface upgradeTask();
  

  public static void bulkAssignAllUpgradeTasksDone() {
    // get group name
    String upgradeTasksRootStemName = UpgradeTasksJob.grouperUpgradeTasksStemName();

    String groupName = upgradeTasksRootStemName + ":" + UpgradeTasksJob.UPGRADE_TASKS_METADATA_GROUP;
    
    // TODO move these to a DAO in v7
    
    // get group id
    String groupId = new GcDbAccess().sql("select id from grouper_groups where name = ?")
        .addBindVar(groupName).select(String.class);

    String nameOfAttributeDefName = upgradeTasksRootStemName + ":" + UpgradeTasksJob.UPGRADE_TASKS_VERSION_ATTR;
    
    // get the attribute def name id
    String attributeDefNameId = new GcDbAccess()
        .sql("select id from grouper_attribute_def_name where name = ?")
        .addBindVar(nameOfAttributeDefName)
        .select(String.class);
    
    // get the attribute def id
    String attributeDefId = new GcDbAccess()
        .sql("select attribute_def_id from grouper_attribute_def_name where id = ?")
        .addBindVar(attributeDefNameId)
        .select(String.class);
    
    // get the assign action id
    String attributeAssignActionId = new GcDbAccess()
        .sql("select id from grouper_attr_assign_action where name = 'assign' and attribute_def_id = ?")
        .addBindVar(attributeDefId)
        .select(String.class);
    
    // get the change log type for addAttributeAssign
    String addAttributeAssignChangeLogType = new GcDbAccess()
        .sql("select id from grouper_change_log_type where change_log_category='attributeAssign' and action_name='addAttributeAssign'")
        .select(String.class);
    
    // get the change log type for addAttributeAssignValue
    String addAttributeAssignValueChangeLogType = new GcDbAccess()
        .sql("select id from grouper_change_log_type where change_log_category='attributeAssignValue' and action_name='addAttributeAssignValue'")
        .select(String.class);

    // insert an attribute assign for the group
    long now = System.currentTimeMillis();
    String attributeAssignId = GrouperUuid.getUuid();
    new GcDbAccess().sql(
        "insert into grouper_attribute_assign (owner_group_id, attribute_assign_action_id, created_on, "
        + "enabled, attribute_def_name_id, disallowed, attribute_assign_type, attribute_assign_delegatable,"
        + "last_updated, id, hibernate_version_number) "
        + "values (?, ?, ?, 'T', ?, 'F', 'group', 'FALSE', ?, ?, 0)")
        .addBindVar(groupId)
        .addBindVar(attributeAssignActionId)
        .addBindVar(now)
        .addBindVar(attributeDefNameId)
        .addBindVar(now)
        .addBindVar(attributeAssignId)
        .executeSql();
    
    new GcDbAccess().sql(
        "insert into grouper_change_log_entry_temp (id, change_log_type_id, created_on, string01, string02, string03, string04, string05, string07, string08, string09) "
        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid())
        .addBindVar(addAttributeAssignChangeLogType)
        .addBindVar(now * 1000)
        .addBindVar(attributeAssignId)
        .addBindVar(attributeDefNameId)
        .addBindVar(attributeAssignActionId)
        .addBindVar("group")
        .addBindVar(groupId)
        .addBindVar("etc:attribute:upgradeTasks:upgradeTasksVersion")
        .addBindVar("assign")
        .addBindVar("F")
        .executeSql();
    
    now++;

    // bulk insert all the upgrade tasks
    List<List<Object>> batchBindVarsAttributeAssignValues = new ArrayList<List<Object>>();
    List<List<Object>> batchBindVarsChangeLogTemp = new ArrayList<List<Object>>();
    for (UpgradeTasks upgradeTask : UpgradeTasks.values()) {
      String attributeAssignValueId = GrouperUuid.getUuid();
      List<Object> bindVarsAttributeAssignValue = new ArrayList<Object>();
      bindVarsAttributeAssignValue.add(attributeAssignId);
      bindVarsAttributeAssignValue.add(now);
      bindVarsAttributeAssignValue.add(attributeAssignValueId);
      bindVarsAttributeAssignValue.add(now);
      bindVarsAttributeAssignValue.add(upgradeTask.name().substring(1));
      batchBindVarsAttributeAssignValues.add(bindVarsAttributeAssignValue);
      
      List<Object> bindVarsChangeLogTemp = new ArrayList<Object>();
      bindVarsChangeLogTemp.add(GrouperUuid.getUuid());
      bindVarsChangeLogTemp.add(addAttributeAssignValueChangeLogType);
      bindVarsChangeLogTemp.add(now * 1000);
      bindVarsChangeLogTemp.add(attributeAssignValueId);
      bindVarsChangeLogTemp.add(attributeAssignId);
      bindVarsChangeLogTemp.add(attributeDefNameId);
      bindVarsChangeLogTemp.add("etc:attribute:upgradeTasks:upgradeTasksVersion");
      bindVarsChangeLogTemp.add(upgradeTask.name().substring(1));
      bindVarsChangeLogTemp.add("string");
      batchBindVarsChangeLogTemp.add(bindVarsChangeLogTemp);
    }

    new GcDbAccess().sql(
        "insert into grouper_attribute_assign_value (attribute_assign_id, created_on, id, last_updated, value_string, hibernate_version_number) "
            + "values (?, ?, ?, ?, ?, 0)")
        .batchBindVars(batchBindVarsAttributeAssignValues).executeBatchSql();
    
    new GcDbAccess().sql(
        "insert into grouper_change_log_entry_temp (id, change_log_type_id, created_on, string01, string02, string03, string04, string05, string06) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)")
        .batchBindVars(batchBindVarsChangeLogTemp).executeBatchSql();
  }
}
