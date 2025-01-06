package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV25 implements UpgradeTasksInterface {
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.14.0");
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return (boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_hst")) {
          if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship_hst", "internal_id")) {
            return true;
          }
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_hst")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship_hst", "grouper_sql_cache_mshhst1_idx")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship_hst", "grouper_sql_cache_mshhst2_idx")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_hst_v")) {
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
        
        if (GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_hst")) {
          if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship_hst", "internal_id")) {
            
            // drop old view/table
            if (GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_hst_v")) {
              new GcDbAccess().sql("DROP VIEW grouper_sql_cache_mship_hst_v").executeSql();
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped view grouper_sql_cache_mship_hst_v");
            }
            
            new GcDbAccess().sql("DROP TABLE grouper_sql_cache_mship_hst").executeSql();
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped table grouper_sql_cache_mship_hst");
          }
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_hst")) {
          // create table
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("CREATE TABLE grouper_sql_cache_mship_hst ( sql_cache_group_internal_id NUMBER(38) NOT NULL, member_internal_id NUMBER(38) NOT NULL, start_time NUMBER(38) NOT NULL, end_time NUMBER(38) NOT NULL, PRIMARY KEY (member_internal_id, sql_cache_group_internal_id, start_time))").executeSql();

          } else {
            new GcDbAccess().sql("CREATE TABLE grouper_sql_cache_mship_hst ( sql_cache_group_internal_id bigint NOT NULL, member_internal_id bigint NOT NULL, start_time bigint NOT NULL, end_time bigint NOT NULL, PRIMARY KEY (member_internal_id, sql_cache_group_internal_id, start_time))").executeSql();
          }
          
          if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_sql_cache_mship_hst IS 'Flattened point in time cache table for memberships or privileges'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst.end_time IS 'flattened membership end time'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst.start_time IS 'flattened membership start time'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst.member_internal_id IS 'member internal id of who this membership refers to'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst.sql_cache_group_internal_id IS 'internal id of which object/field this membership refers to'").executeSql();
          }
          
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_sql_cache_mship_hst");
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship_hst", "grouper_sql_cache_mshhst1_idx")) {
          new GcDbAccess().sql("CREATE INDEX grouper_sql_cache_mshhst1_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, end_time)").executeSql();
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_mshhst1_idx");
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship_hst", "grouper_sql_cache_mshhst2_idx")) {
          new GcDbAccess().sql("CREATE INDEX grouper_sql_cache_mshhst2_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, start_time, end_time)").executeSql();
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_mshhst2_idx");
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_hst_v")) {
          
          // drop and add the constraint.  it has nothing to do with the view but this will ensure the constraint is there before the view is added in case it fails in the middle and has to rerun
          // we should really separately check if the constraint exists or not but not sure the best way to make sure we're looking at the right schema when querying information_schema
          // TODO fix ^ later
          
          try {
            if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_mship_hst DROP FOREIGN KEY grouper_sql_cache_msh_hst1_fk").executeSql();
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped constraint grouper_sql_cache_msh_hst1_fk");
            } else {
              new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_mship_hst DROP CONSTRAINT grouper_sql_cache_msh_hst1_fk").executeSql();
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped constraint grouper_sql_cache_msh_hst1_fk");
            }
          } catch (Exception e) {
            // ignore
          }
          
          new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_mship_hst ADD CONSTRAINT grouper_sql_cache_msh_hst1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group (internal_id)").executeSql();
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added constraint grouper_sql_cache_msh_hst1_fk");
          
          new GcDbAccess().sql("CREATE VIEW grouper_sql_cache_mship_hst_v (group_name, list_name, subject_id, subject_identifier0, subject_identifier1, subject_identifier2, subject_source, start_time, end_time, group_id, field_id, member_internal_id, group_internal_id, field_internal_id) AS select  gg.name as group_name, gf.name as list_name, gm.subject_id, gm.subject_identifier0, gm.subject_identifier1,  gm.subject_identifier2, gm.subject_source, gscmh.start_time, gscmh.end_time, gg.id as group_id,  gf.id as field_id, gm.internal_id as member_internal_id,  gg.internal_id as group_internal_id, gf.internal_id as field_internal_id from  grouper_sql_cache_group gscg, grouper_sql_cache_mship_hst gscmh, grouper_fields gf,  grouper_groups gg, grouper_members gm where gscg.group_internal_id = gg.internal_id  and gscg.field_internal_id = gf.internal_id and gscmh.sql_cache_group_internal_id = gscg.internal_id  and gscmh.member_internal_id = gm.internal_id").executeSql();
          
          if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
            if (GrouperDdlUtils.isOracle()) {
              new GcDbAccess().sql("COMMENT ON TABLE grouper_sql_cache_mship_hst_v IS 'SQL cache mship history view'").executeSql();
            } else {
              new GcDbAccess().sql("COMMENT ON VIEW grouper_sql_cache_mship_hst_v IS 'SQL cache mship history view'").executeSql();
            }
            
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.group_name IS 'group_name: name of group'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.list_name IS 'list_name: name of list e.g. members or admins'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_id IS 'subject_id: subject id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_identifier0 IS 'subject_identifier0: subject identifier0 from subject source and members table'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_identifier1 IS 'subject_identifier1: subject identifier1 from subject source and members table'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_identifier2 IS 'subject_identifier2: subject identifier2 from subject source and members table'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.subject_source IS 'subject_source: subject source id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.start_time IS 'start_time: when this membership started'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.end_time IS 'end_time: when this membership ended'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.group_id IS 'group_id: uuid of group'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.field_id IS 'field_id: uuid of field'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.member_internal_id IS 'member_internal_id: member internal id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.group_internal_id IS 'group_internal_id: group internal id'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship_hst_v.field_internal_id IS 'field_internal_id: field internal id'").executeSql();
          }
          
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created view grouper_sql_cache_mship_hst_v");
        }
        
        return null;
      }
    });
  }

}
