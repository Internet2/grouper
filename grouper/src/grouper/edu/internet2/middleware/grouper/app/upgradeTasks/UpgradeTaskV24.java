package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.sql.Types;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV24 implements UpgradeTasksInterface {
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.13.3");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        // convert grouper_sql_cache_mship.flattened_add_timestamp from timestamp/date to bigint/number

        if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "flattened_add_timestamp")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_v")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship3_idx")) {
          return true;
        }
        
        if (GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship1_idx")) {       
          return true;
        }
        return false;
      }
    });
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        // convert grouper_sql_cache_mship.flattened_add_timestamp from timestamp/date to bigint/number

        if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "flattened_add_timestamp")) {

          int columnType = GrouperDdlUtils.getColumnType("grouper_sql_cache_mship", "flattened_add_timestamp");
          if (columnType == Types.TIMESTAMP) {
            
            // drop dependency
            if (GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_v")) {
              new GcDbAccess().sql("DROP VIEW grouper_sql_cache_mship_v").executeSql();
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped view grouper_sql_cache_mship_v");
            }
            
            // add new temporary column if it doesn't exist
            if (!GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "flattened_add_timestamp_temp")) {
              if (GrouperDdlUtils.isOracle()) {
                new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_mship ADD flattened_add_timestamp_temp NUMBER(38)").executeSql();
              } else {
                new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_mship ADD COLUMN flattened_add_timestamp_temp BIGINT").executeSql();
              }
              
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_sql_cache_mship.flattened_add_timestamp_temp");
            }
            
            // populate temporary column if needed - i think we're assuming that the session timezone is correct on the database otherwise the micros might be off (but would be corrected later)
            {
              String sql;
              if (GrouperDdlUtils.isOracle()) {
                sql = "update grouper_sql_cache_mship set flattened_add_timestamp_temp = ((to_number(CAST(FROM_TZ(CAST(flattened_add_timestamp AS TIMESTAMP), SESSIONTIMEZONE) AT TIME ZONE 'UTC' AS DATE) - to_date('01-JAN-1970','DD-MON-YYYY')) * (24 * 60 * 60 * 1000)) * 1000) where flattened_add_timestamp_temp is null and rownum <= 1000000";
              } else if (GrouperDdlUtils.isMysql()) {
                sql = "update grouper_sql_cache_mship gscm JOIN (select member_internal_id, sql_cache_group_internal_id from grouper_sql_cache_mship where flattened_add_timestamp_temp is null limit 1000000) as subquery ON gscm.member_internal_id=subquery.member_internal_id and gscm.sql_cache_group_internal_id=subquery.sql_cache_group_internal_id SET flattened_add_timestamp_temp = (UNIX_TIMESTAMP(flattened_add_timestamp) * 1000000)";
              } else {
                sql = "update grouper_sql_cache_mship set flattened_add_timestamp_temp = (extract(epoch from flattened_add_timestamp AT TIME ZONE current_setting('TIMEZONE')) * 1000000::BIGINT) where ctid IN (select ctid from grouper_sql_cache_mship where flattened_add_timestamp_temp is null limit 1000000)";
              }
              while (true) {
                int count = new GcDbAccess().sql(sql).executeSql();
                if (count == 0) {
                  break;
                }
                
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", set " + count + " flattened_add_timestamp_temp values");
              }
            }
            
            // make flattened_add_timestamp_temp not null
            if (GrouperDdlUtils.isColumnNullable("grouper_sql_cache_mship", "flattened_add_timestamp_temp", "sql_cache_group_internal_id", -1)) {
              String sql;
              
              if (GrouperDdlUtils.isOracle()) {
                sql = "ALTER TABLE grouper_sql_cache_mship MODIFY (flattened_add_timestamp_temp NOT NULL)";
              } else if (GrouperDdlUtils.isMysql()) {
                sql = "ALTER TABLE grouper_sql_cache_mship MODIFY flattened_add_timestamp_temp BIGINT NOT NULL";
              } else {
                sql = "ALTER TABLE grouper_sql_cache_mship ALTER COLUMN flattened_add_timestamp_temp SET NOT NULL";
              }
              
              new GcDbAccess().sql(sql).executeSql();
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", set not null for column grouper_sql_cache_mship.flattened_add_timestamp_temp");
            }
            
            // drop flattened_add_timestamp
            {
              new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_mship DROP COLUMN flattened_add_timestamp").executeSql();
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped column grouper_sql_cache_mship.flattened_add_timestamp");
            }
          }
        }
        
        if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "flattened_add_timestamp_temp")) {
          // need to rename the temp column
          new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_mship RENAME COLUMN flattened_add_timestamp_temp TO flattened_add_timestamp").executeSql();
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", renamed column grouper_sql_cache_mship.flattened_add_timestamp_temp to flattened_add_timestamp");
        }
        
        // replacing the index in mysql gets tricky because the constraints have to be dropped.  so we'll just create a new one.
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship3_idx")) {
          new GcDbAccess().sql("CREATE INDEX grouper_sql_cache_mship3_idx ON grouper_sql_cache_mship (sql_cache_group_internal_id, flattened_add_timestamp)").executeSql();
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_mship3_idx");
        }
        
        if (GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship1_idx")) {                
          if (GrouperDdlUtils.isMysql()) {
            new GcDbAccess().sql("DROP INDEX grouper_sql_cache_mship1_idx ON grouper_sql_cache_mship").executeSql();
          } else {
            new GcDbAccess().sql("DROP INDEX grouper_sql_cache_mship1_idx").executeSql();
          }
          
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index grouper_sql_cache_mship1_idx");
        }
        
        if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship.flattened_add_timestamp IS 'when this member was last added to this group after not being a member before.  How long this member has been in this group'").executeSql();
        }
        
        // create view
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_v")) {
          new GcDbAccess().sql("CREATE VIEW grouper_sql_cache_mship_v (group_name, list_name, subject_id, subject_identifier0, subject_identifier1, subject_identifier2, subject_source, flattened_add_timestamp, group_id, field_id, member_internal_id, group_internal_id, field_internal_id) AS SELECT gg.name AS group_name, gf.name AS list_name, gm.subject_id, gm.subject_identifier0,  gm.subject_identifier1, gm.subject_identifier2, gm.subject_source, gscm.flattened_add_timestamp,  gg.id AS group_id, gf.id AS field_id, gm.internal_id AS member_internal_id,  gg.internal_id AS group_internal_id, gf.internal_id AS field_internal_id  FROM grouper_sql_cache_group gscg, grouper_sql_cache_mship gscm, grouper_fields gf,  grouper_groups gg, grouper_members gm  WHERE gscg.group_internal_id = gg.internal_id AND gscg.field_internal_id = gf.internal_id  AND gscm.sql_cache_group_internal_id = gscg.internal_id AND gscm.member_internal_id = gm.internal_id").executeSql();
          
          if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
            if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("COMMENT ON TABLE grouper_sql_cache_mship_v IS 'SQL cache mship view'").executeSql();
            } else {
              new GcDbAccess().sql("COMMENT ON VIEW grouper_sql_cache_mship_v IS 'SQL cache mship view'").executeSql();
            }
            
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.group_name IS 'group_name: name of group'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.list_name IS 'list_name: name of list e.g. members or admins'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_id IS 'subject_id: subject id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_identifier0 IS 'subject_identifier0: subject identifier0 from subject source and members table'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_identifier1 IS 'subject_identifier1: subject identifier1 from subject source and members table'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_identifier2 IS 'subject_identifier2: subject identifier2 from subject source and members table'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.subject_source IS 'subject_source: subject source id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.flattened_add_timestamp IS 'flattened_add_timestamp: when this membership started'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.group_id IS 'group_id: uuid of group'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.field_id IS 'field_id: uuid of field'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.member_internal_id IS 'member_internal_id: member internal id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.group_internal_id IS 'group_internal_id: group internal id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_v.field_internal_id IS 'field_internal_id: field internal id'").executeSql();
          }
          
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created view grouper_sql_cache_mship_v");
        }
        
        return null;
      }
    });
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

}
