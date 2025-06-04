package edu.internet2.middleware.grouper.app.upgradeTasks;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV32 implements UpgradeTasksInterface {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV32.class);

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_dependency_group_v")) {
      return true;
    }

    if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_dependency_attr_v")) {
      return true;
    }

    if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_dependency_row_v")) {
      return true;
    }

    return false;
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.18.0");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        try {
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_dependency_group_v")) {
            
            new GcDbAccess().sql("create view grouper_sql_dependency_group_v (dependency_type_name, dependency_type_category, dependency_type_internal_id, dependency_internal_id, owner_cache_group_internal_id, owner_group_name,owner_group_id, owner_group_internal_id, owner_group_id_index, owner_field_name, owner_field_type, owner_field_internal_id, depen_cache_group_internal_id, depen_group_name, depen_group_id, depen_group_internal_id, depen_group_id_index, depen_field_name, depen_field_type, depen_field_internal_id) as select gscdt_group.name as dependency_type_name, gscdt_group.dependency_category as dependency_type_category, gscdt_group.internal_id as dependency_type_internal_id, gscd_group.internal_id as dependency_internal_id, gscg_owner.internal_id as owner_cache_group_internal_id, gg_owner.name as owner_group_name, gg_owner.id as owner_group_id, gg_owner.internal_id as owner_group_internal_id, gg_owner.id_index as owner_group_id_index, gf_owner.name as owner_field_name, gf_owner.type as owner_field_type, gf_owner.internal_id as owner_field_internal_id, gscg_dependent.internal_id as depen_cache_group_internal_id, gg_dependent.name as depen_group_name, gg_dependent.id as depen_group_id, gg_dependent.internal_id as depen_group_internal_id, gg_dependent.id_index as depen_group_id_index, gf_dependent.name as depen_field_name, gf_dependent.type as depen_field_type, gf_dependent.internal_id as depen_field_internal_id from  grouper_sql_cache_depend_type gscdt_group, grouper_sql_cache_dependency gscd_group left join grouper_sql_cache_group gscg_owner on gscd_group.owner_internal_id = gscg_owner.internal_id left join grouper_groups gg_owner on gg_owner.internal_id = gscg_owner.group_internal_id left join grouper_fields gf_owner on gscg_owner.field_internal_id = gf_owner.internal_id  left join grouper_sql_cache_group gscg_dependent on gscd_group.dependent_internal_id = gscg_dependent.internal_id left join grouper_groups gg_dependent on gg_dependent.internal_id = gscg_dependent.group_internal_id left join grouper_fields gf_dependent on gscg_dependent.field_internal_id = gf_dependent.internal_id  where gscdt_group.name in ('mshipHistory_viaAttribute', 'mshipHistory_recentMships','mshipHistory_abac','abac_group')  and gscd_group.dep_type_internal_id = gscdt_group.internal_id").executeSql();
  
            if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
              if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("COMMENT ON table grouper_sql_dependency_group_v IS 'view on dependencies from group to group.  a group is dependent on another group if when the owner changes the dependent group needs to be recalculated'").executeSql();
              } else {
                new GcDbAccess().sql("COMMENT ON VIEW grouper_sql_dependency_group_v IS 'view on dependencies from group to group.  a group is dependent on another group if when the owner changes the dependent group needs to be recalculated'").executeSql();
              }
              
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.dependency_type_name IS 'name of the dependency from the grouper_sql_cache_depend_type table.  e.g. mshipHistory_abac or abac_group'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.dependency_type_category IS 'category of the dependency from the grouper_sql_cache_depend_type table.  e.g. mshipHistory or abac'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.dependency_type_internal_id IS 'internal id of the dependency type from the grouper_sql_cache_depend_type table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.dependency_internal_id IS 'internal id of the dependency from the grouper_sql_cache_dependency table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.owner_cache_group_internal_id IS 'internal id of the owner of the cache group from the grouper_sql_cache_group table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.owner_group_name IS 'group name of the owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.owner_group_id IS 'group uuid of the owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.owner_group_internal_id IS 'group internal id of the owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.owner_group_id_index IS 'group id index of the owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.owner_field_name IS 'field name e.g. members of the owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.owner_field_type IS 'field type e.g. access of the owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.owner_field_internal_id IS 'field internal id of the owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.depen_cache_group_internal_id IS 'internal id of the dependent of the cache group from the grouper_sql_cache_group table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.depen_group_name IS 'group name of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.depen_group_id IS 'group uuid of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.depen_group_internal_id IS 'group internal id of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.depen_group_id_index IS 'group id index of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.depen_field_name IS 'field name e.g. members of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.depen_field_type IS 'field type e.g. access of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_group_v.depen_field_internal_id IS 'field internal id of the dependent group of the dependency'").executeSql();
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created view grouper_sql_dependency_group_v");
  
          }
  
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_dependency_attr_v")) {
  
            new GcDbAccess().sql("create view grouper_sql_dependency_attr_v (dependency_type_name, dependency_type_category,  dependency_type_internal_id, dependency_internal_id, owner_data_field_internal_id,  owner_data_field_config_id, owner_data_alias_name, owner_data_alias_lower_name,  owner_data_alias_internal_id, depen_cache_group_internal_id, depen_group_name,  depen_group_id, depen_group_internal_id, depen_group_id_index, depen_field_name,  depen_field_type, depen_field_internal_id ) as select gscdt.name as dependency_type_name, gscdt.dependency_category as dependency_type_category, gscdt.internal_id as dependency_type_internal_id, gscd.internal_id as dependency_internal_id, dtf_owner.internal_id as owner_data_field_internal_id, dtf_owner.config_id  as owner_data_field_config_id, gda_owner.name as owner_data_alias_name, gda_owner.lower_name as owner_data_alias_lower_name, gda_owner.internal_id as owner_data_alias_internal_id, gscg_dependent.internal_id as depen_cache_group_internal_id, gg_dependent.name as depen_group_name, gg_dependent.id as depen_group_id, gg_dependent.internal_id as depen_group_internal_id, gg_dependent.id_index as depen_group_id_index, gf_dependent.name as depen_field_name, gf_dependent.type as depen_field_type, gf_dependent.internal_id as depen_field_internal_id from  grouper_sql_cache_depend_type gscdt, grouper_sql_cache_dependency gscd left join grouper_data_field dtf_owner on gscd.owner_internal_id = dtf_owner.internal_id left join grouper_data_alias gda_owner on gda_owner.data_field_internal_id = dtf_owner.internal_id left join grouper_sql_cache_group gscg_dependent on gscd.dependent_internal_id = gscg_dependent.internal_id left join grouper_groups gg_dependent on gg_dependent.internal_id = gscg_dependent.group_internal_id left join grouper_fields gf_dependent on gscg_dependent.field_internal_id = gf_dependent.internal_id  where gscdt.name in ('abac_attribute')  and gscd.dep_type_internal_id = gscdt.internal_id").executeSql();
  
            if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
              if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("COMMENT ON table grouper_sql_dependency_attr_v IS 'view on dependencies with group dependent on an owner data field.  a group is dependent on a data field if when the owner data field value changes the dependent group needs to be recalculated'").executeSql();
              } else {
                new GcDbAccess().sql("COMMENT ON VIEW grouper_sql_dependency_attr_v IS 'view on dependencies with group dependent on an owner data field.  a group is dependent on a data field if when the owner data field value changes the dependent group needs to be recalculated'").executeSql();
              }
              
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.dependency_type_name IS 'name of the dependency from the grouper_sql_cache_depend_type table.  e.g. mshipHistory_abac or abac_group'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.dependency_type_category IS 'category of the dependency from the grouper_sql_cache_depend_type table.  e.g. mshipHistory or abac'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.dependency_type_internal_id IS 'internal id of the dependency type from the grouper_sql_cache_depend_type table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.dependency_internal_id IS 'internal id of the dependency from the grouper_sql_cache_dependency table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.owner_data_field_internal_id IS 'internal id of the owner data field from the grouper_data_field table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.owner_data_field_config_id IS 'config id of the ata field owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.owner_data_alias_name IS 'alias of the owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.owner_data_alias_lower_name IS 'alias of the data field owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.owner_data_alias_internal_id IS 'internal if of the alias of the data field owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.depen_cache_group_internal_id IS 'internal id of the dependent of the cache group from the grouper_sql_cache_group table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.depen_group_name IS 'group name of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.depen_group_id IS 'group uuid of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.depen_group_internal_id IS 'group internal id of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.depen_group_id_index IS 'group id index of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.depen_field_name IS 'field name e.g. members of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.depen_field_type IS 'field type e.g. access of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_attr_v.depen_field_internal_id IS 'field internal id of the dependent group of the dependency'").executeSql();
  
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created view grouper_sql_dependency_attr_v");
  
          }
  
          if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_dependency_row_v")) {
  
            new GcDbAccess().sql("create view grouper_sql_dependency_row_v (dependency_type_name, dependency_type_category,  dependency_type_internal_id, dependency_internal_id, owner_data_row_internal_id,  owner_data_row_config_id, owner_data_alias_name, owner_data_alias_lower_name,  owner_data_alias_internal_id, depen_cache_group_internal_id, depen_group_name,  depen_group_id, depen_group_internal_id, depen_group_id_index, depen_field_name,  depen_field_type, depen_field_internal_id ) as select gscdt.name as dependency_type_name, gscdt.dependency_category as dependency_type_category, gscdt.internal_id as dependency_type_internal_id, gscd.internal_id as dependency_internal_id, dtr_owner.internal_id as owner_data_row_internal_id, dtr_owner.config_id  as owner_data_row_config_id, gda_owner.name as owner_data_alias_name, gda_owner.lower_name as owner_data_alias_lower_name, gda_owner.internal_id as owner_data_alias_internal_id, gscg_dependent.internal_id as depen_cache_group_internal_id, gg_dependent.name as depen_group_name, gg_dependent.id as depen_group_id, gg_dependent.internal_id as depen_group_internal_id, gg_dependent.id_index as depen_group_id_index, gf_dependent.name as depen_field_name, gf_dependent.type as depen_field_type, gf_dependent.internal_id as depen_field_internal_id from  grouper_sql_cache_depend_type gscdt, grouper_sql_cache_dependency gscd left join grouper_data_row dtr_owner on gscd.owner_internal_id = dtr_owner.internal_id left join grouper_data_alias gda_owner on gda_owner.data_row_internal_id = dtr_owner.internal_id left join grouper_sql_cache_group gscg_dependent on gscd.dependent_internal_id = gscg_dependent.internal_id left join grouper_groups gg_dependent on gg_dependent.internal_id = gscg_dependent.group_internal_id left join grouper_fields gf_dependent on gscg_dependent.field_internal_id = gf_dependent.internal_id  where gscdt.name in ('abac_row')  and gscd.dep_type_internal_id = gscdt.internal_id").executeSql();
  
            if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
              if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("COMMENT ON table grouper_sql_dependency_row_v IS 'view on dependencies with group dependent on an owner data row.  a group is dependent on a data row if when the owner data row value changes the dependent group needs to be recalculated'").executeSql();
              } else {
                new GcDbAccess().sql("COMMENT ON view grouper_sql_dependency_row_v IS 'view on dependencies with group dependent on an owner data row.  a group is dependent on a data row if when the owner data row value changes the dependent group needs to be recalculated'").executeSql();
              }
              
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.dependency_type_name IS 'name of the dependency from the grouper_sql_cache_depend_type table.  e.g. mshipHistory_abac or abac_group'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.dependency_type_category IS 'category of the dependency from the grouper_sql_cache_depend_type table.  e.g. mshipHistory or abac'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.dependency_type_internal_id IS 'internal id of the dependency type from the grouper_sql_cache_depend_type table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.dependency_internal_id IS 'internal id of the dependency from the grouper_sql_cache_dependency table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.owner_data_row_internal_id IS 'internal id of the owner data row from the grouper_data_row table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.owner_data_row_config_id IS 'config id of the ata row owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.owner_data_alias_name IS 'alias of the owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.owner_data_alias_lower_name IS 'alias of the data row owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.owner_data_alias_internal_id IS 'internal if of the alias of the data row owner of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.depen_cache_group_internal_id IS 'internal id of the dependent of the cache group from the grouper_sql_cache_group table'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.depen_group_name IS 'group name of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.depen_group_id IS 'group uuid of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.depen_group_internal_id IS 'group internal id of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.depen_group_id_index IS 'group id index of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.depen_field_name IS 'field name e.g. members of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.depen_field_type IS 'field type e.g. access of the dependent group of the dependency'").executeSql();
              new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_dependency_row_v.depen_field_internal_id IS 'field internal id of the dependent group of the dependency'").executeSql();
  
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created view grouper_sql_dependency_row_v");
  
          }
        } catch (RuntimeException re) {
          LOG.error("Error creating views grouper_sql_dependency_group_v, grouper_sql_dependency_attr_v, grouper_sql_dependency_row_v", re);
          throw re;
        }

        return null;
      }
    });
  }

}
