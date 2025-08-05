package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV15 implements UpgradeTasksInterface {
  
  
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.8.1");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    // adding based on ddl removed from v45 and v46 sql script files
    {
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_members", "internal_id")) {
        return true;
      }
      
      // TODO copying from old script file which I'm not entirely sure I understand why it's doing what it's doing
      if (GrouperDdlUtils.isPostgres()) {
        if (!GrouperDdlUtils.assertIndexExists("grouper_members", "members_internal_id_unique")) {
          return true;
        }
      }
      
      // TODO mysql we're adding two indexes?? Just copying what was there for now.
      if (GrouperDdlUtils.isMysql()) {
        if (!GrouperDdlUtils.assertIndexExists("grouper_members", "grouper_mem_internal_id_idx")) {
          return true;
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_members", "members_internal_id_unique")) {
          return true;
        }
      }
      
      if (GrouperDdlUtils.isOracle()) {
        if (!GrouperDdlUtils.assertIndexExists("grouper_members", "grouper_mem_internal_id_idx")) {
          return true;
        }
        
        if (!GrouperDdlUtils.doesConstraintExistOracle("members_internal_id_unique")) {
          return true;
        }
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_fields", "internal_id")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_fields", "grouper_fie_internal_id_idx")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_groups", "internal_id")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_groups", "grouper_grp_internal_id_idx")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_group")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_group", "grouper_sql_cache_group1_idx")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship")) {
        return true;
      }
      
      // TODO for oracle, this is added somewhere else??
      if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isMysql()) {
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_sql_cache_group", "grouper_sql_cache_group1_fk")) {
          return true;
        }
      }
      
      if (!GrouperDdlUtils.assertForeignKeyExists("grouper_sql_cache_mship", "grouper_sql_cache_mship1_fk")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_members", "source_internal_id")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_fields", "source_internal_id")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_groups", "source_internal_id")) {
        return true;
      }
          
      if (!GrouperDdlUtils.assertIndexExists("grouper_pit_members", "pit_member_source_internal_idx")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_pit_fields", "pit_field_source_internal_idx")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_pit_groups", "pit_group_source_internal_idx")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_group_v")) {
        return true;
      }
    }
    
    boolean groupsNullable = GrouperDdlUtils.isColumnNullable("grouper_groups", "internal_id", "name", GrouperCheckConfig.attributeRootStemName() + ":upgradeTasks:upgradeTasksMetadataGroup");
    boolean fieldsNullable = GrouperDdlUtils.isColumnNullable("grouper_fields", "internal_id", "name", "admins");
    
    if (groupsNullable) {
      return true;
    }
    
    if (fieldsNullable) {
      return true;
    }
   
    if (GrouperDdlUtils.isOracle()) {
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_fie_internal_id_unq")) {
        return true;
      }
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_grp_internal_id_unq")) {
        return true;
      }
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_sql_cache_group1_fk")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    {
      // adding based on ddl removed from v45 and v46 sql script files
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_members", "internal_id")) {
        if (GrouperDdlUtils.isOracle()) {
          new GcDbAccess().sql("ALTER TABLE grouper_members ADD internal_id NUMBER(38)").executeSql();
        } else {
          new GcDbAccess().sql("ALTER TABLE grouper_members ADD COLUMN internal_id BIGINT").executeSql();
        }
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_members.internal_id");
        }
      }
      
      // TODO copying from old script file which I'm not entirely sure I understand why it's doing what it's doing
      if (GrouperDdlUtils.isPostgres()) {
        if (!GrouperDdlUtils.assertIndexExists("grouper_members", "members_internal_id_unique")) {
          if (!GrouperDdlUtils.assertIndexExists("grouper_members", "grouper_mem_internal_id_idx")) {
            new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_mem_internal_id_idx ON grouper_members (internal_id)").executeSql();
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_mem_internal_id_idx");
            }
          }
          
          new GcDbAccess().sql("ALTER TABLE grouper_members ADD CONSTRAINT members_internal_id_unique UNIQUE USING INDEX grouper_mem_internal_id_idx").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added constraint members_internal_id_unique");
          }
        }
      }
      
      // TODO mysql we're adding two indexes?? Just copying what was there for now.
      if (GrouperDdlUtils.isMysql()) {
        if (!GrouperDdlUtils.assertIndexExists("grouper_members", "grouper_mem_internal_id_idx")) {
          new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_mem_internal_id_idx ON grouper_members (internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_mem_internal_id_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_members", "members_internal_id_unique")) {
          new GcDbAccess().sql("ALTER TABLE grouper_members ADD CONSTRAINT members_internal_id_unique UNIQUE (internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added constraint members_internal_id_unique");
          }
        }
      }
      
      if (GrouperDdlUtils.isOracle()) {
        if (!GrouperDdlUtils.assertIndexExists("grouper_members", "grouper_mem_internal_id_idx")) {
          new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_mem_internal_id_idx ON grouper_members (internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_mem_internal_id_idx");
          }
        }
        
        if (!GrouperDdlUtils.doesConstraintExistOracle("members_internal_id_unique")) {
          new GcDbAccess().sql("ALTER TABLE grouper_members ADD CONSTRAINT members_internal_id_unique UNIQUE (internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added constraint members_internal_id_unique");
          }
        }
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_fields", "internal_id")) {
        if (GrouperDdlUtils.isPostgres()) {
          new GcDbAccess().sql("ALTER TABLE grouper_fields ADD COLUMN internal_id BIGINT").executeSql();
        } else if (GrouperDdlUtils.isMysql()) {
          new GcDbAccess().sql("ALTER TABLE grouper_fields ADD COLUMN internal_id BIGINT AFTER context_id").executeSql();
        } else {
          new GcDbAccess().sql("ALTER TABLE GROUPER_FIELDS ADD internal_id NUMBER(38)").executeSql();
        }
        
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_fields.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql(); 
        }
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_fields.internal_id");
        }
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_fields", "grouper_fie_internal_id_idx")) {
        new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_fie_internal_id_idx ON grouper_fields (internal_id)").executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_fie_internal_id_idx");
        }
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_groups", "internal_id")) {
        if (GrouperDdlUtils.isPostgres()) {
          new GcDbAccess().sql("ALTER TABLE grouper_groups ADD COLUMN internal_id BIGINT").executeSql();
        } else if (GrouperDdlUtils.isMysql()) {
          new GcDbAccess().sql("ALTER TABLE grouper_groups ADD COLUMN internal_id BIGINT AFTER id_index").executeSql();
        } else {
          new GcDbAccess().sql("ALTER TABLE GROUPER_GROUPS ADD internal_id NUMBER(38)").executeSql();
        }
        
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_groups.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
        }
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_groups.internal_id");
        }
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_groups", "grouper_grp_internal_id_idx")) {
        new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_grp_internal_id_idx ON grouper_groups (internal_id)").executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_grp_internal_id_idx");
        }
      }
      
      
      if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_group")) {
        if (GrouperDdlUtils.isPostgres()) {
          new GcDbAccess().sql("""
            CREATE TABLE grouper_sql_cache_group (
              internal_id bigint NOT NULL,
              group_internal_id bigint NOT NULL,
              field_internal_id bigint not NULL,
              membership_size bigint not null,
              membership_size_hst bigint NOT NULL,
              created_on timestamp NOT NULL,
              enabled_on timestamp NOT NULL,
              disabled_on timestamp NULL,
              PRIMARY KEY (internal_id)
            )                   
              """).executeSql();
        } else if (GrouperDdlUtils.isMysql()) {
          new GcDbAccess().sql("""
            CREATE TABLE grouper_sql_cache_group
            (
                internal_id BIGINT NOT NULL,
                group_internal_id BIGINT NOT NULL,
                field_internal_id BIGINT NOT NULL,
                membership_size BIGINT NOT NULL,
                membership_size_hst BIGINT NOT NULL,
                created_on DATETIME NOT NULL,
                enabled_on DATETIME NOT NULL,
                disabled_on DATETIME,
                PRIMARY KEY (internal_id)
            )                 
                """).executeSql();
        } else {
          new GcDbAccess().sql("""
            CREATE TABLE grouper_sql_cache_group
            (
                internal_id NUMBER(38) NOT NULL,
                group_internal_id NUMBER(38) NOT NULL,
                field_internal_id NUMBER(38) NOT NULL,
                membership_size NUMBER(38) NOT NULL,
                membership_size_hst NUMBER(38) NOT NULL,
                created_on DATE NOT NULL,
                enabled_on DATE NOT NULL,
                disabled_on DATE,
                PRIMARY KEY (internal_id)
            )                  
                """).executeSql();
        }
        
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          new GcDbAccess().sql("COMMENT ON TABLE grouper_sql_cache_group IS 'Holds groups that are cacheable in SQL'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group.group_internal_id IS 'internal integer id for gruops which are cacheable'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group.field_internal_id IS 'internal integer id for the field which is the members or privilege which is cached'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group.membership_size IS 'approximate number of members of this group, used primarily to optimize batching'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group.membership_size_hst IS 'approximate number of rows of HST data for this group, used primarily to optimize batching'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group.created_on IS 'when this row was created (i.e. when this group started to be cached)'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group.enabled_on IS 'when this cache will be ready to use (do not use it while it is being populated)'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group.disabled_on IS 'when this cache should stop being used'").executeSql();
        }
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_sql_cache_group");
        }
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_group", "grouper_sql_cache_group1_idx")) {
        new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_sql_cache_group1_idx ON grouper_sql_cache_group (group_internal_id, field_internal_id)").executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_group1_idx");
        }
      }
      
      if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship")) {
        if (GrouperDdlUtils.isPostgres()) {
          new GcDbAccess().sql("""
            CREATE TABLE grouper_sql_cache_mship (
              internal_id bigint NOT NULL,
              sql_cache_group_internal_id bigint NOT NULL,
              member_internal_id bigint not NULL,
              flattened_add_timestamp timestamp not null,
              created_on timestamp NOT NULL,
              PRIMARY KEY (internal_id)
            )
              """).executeSql();
        } else if (GrouperDdlUtils.isMysql()) {
          new GcDbAccess().sql("""
            CREATE TABLE grouper_sql_cache_mship
            (
                created_on DATETIME NOT NULL,
                flattened_add_timestamp DATETIME NOT NULL,
                internal_id BIGINT NOT NULL,
                member_internal_id BIGINT NOT NULL,
                sql_cache_group_internal_id BIGINT NOT NULL
            )
                """).executeSql();
        } else {
          new GcDbAccess().sql("""
            CREATE TABLE grouper_sql_cache_mship
            (
                created_on DATE NOT NULL,
                flattened_add_timestamp DATE NOT NULL,
                internal_id NUMBER(38) NOT NULL,
                member_internal_id NUMBER(38) NOT NULL,
                sql_cache_group_internal_id NUMBER(38) NOT NULL
            )
                """).executeSql();
        }
        
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          new GcDbAccess().sql("COMMENT ON TABLE grouper_sql_cache_mship IS 'Cached memberships based on group and list'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship.created_on IS 'when this cache row was created'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship.flattened_add_timestamp IS 'when this member was last added to this group after not being a member before.  How long this member has been in this group'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship.member_internal_id IS 'internal id of the member in this group'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_mship.sql_cache_group_internal_id IS 'internal id of the group/list that this member is in'").executeSql();
        }
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added table grouper_sql_cache_mship");
        }
      }
      
      if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isMysql()) {
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_sql_cache_group", "grouper_sql_cache_group1_fk")) {
          new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_group ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (field_internal_id) REFERENCES grouper_fields(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_sql_cache_group1_fk");
          }
        }
      }
      
      if (!GrouperDdlUtils.assertForeignKeyExists("grouper_sql_cache_mship", "grouper_sql_cache_mship1_fk")) {
        new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_mship ADD CONSTRAINT grouper_sql_cache_mship1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group(internal_id)").executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_sql_cache_mship1_fk");
        }
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_members", "source_internal_id")) {
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isMysql()) {
          new GcDbAccess().sql("ALTER TABLE grouper_pit_members ADD COLUMN source_internal_id BIGINT").executeSql();
        } else {
          new GcDbAccess().sql("ALTER TABLE GROUPER_PIT_MEMBERS ADD source_internal_id NUMBER(38)").executeSql();
        }
        
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_pit_members.source_internal_id IS 'internal integer id from the grouper_members table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
        }
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_pit_members.source_internal_id");
        }
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_fields", "source_internal_id")) {
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isMysql()) {
          new GcDbAccess().sql("ALTER TABLE grouper_pit_fields ADD COLUMN source_internal_id BIGINT").executeSql();
        } else {
          new GcDbAccess().sql("ALTER TABLE GROUPER_PIT_FIELDS ADD source_internal_id NUMBER(38)").executeSql();
        }
        
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_pit_fields.source_internal_id IS 'internal integer id from the grouper_fields table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
        }
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_pit_fields.source_internal_id");
        }
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_groups", "source_internal_id")) {
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isMysql()) {
          new GcDbAccess().sql("ALTER TABLE grouper_pit_groups ADD COLUMN source_internal_id BIGINT").executeSql();
        } else {
          new GcDbAccess().sql("ALTER TABLE GROUPER_PIT_GROUPS ADD source_internal_id NUMBER(38)").executeSql();
        }
        
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_pit_groups.source_internal_id IS 'internal integer id from the grouper_groups table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
        }
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_pit_groups.source_internal_id");
        }
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_pit_members", "pit_member_source_internal_idx")) {
        new GcDbAccess().sql("CREATE INDEX pit_member_source_internal_idx ON grouper_pit_members (source_internal_id)").executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index pit_member_source_internal_idx");
        }
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_pit_fields", "pit_field_source_internal_idx")) {
        new GcDbAccess().sql("CREATE INDEX pit_field_source_internal_idx ON grouper_pit_fields (source_internal_id)").executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index pit_field_source_internal_idx");
        }
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_pit_groups", "pit_group_source_internal_idx")) {
        new GcDbAccess().sql("CREATE INDEX pit_group_source_internal_idx ON grouper_pit_groups (source_internal_id)").executeSql();
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index pit_group_source_internal_idx");
        }
      }
      
      if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_group_v")) {
        new GcDbAccess().sql("CREATE VIEW grouper_sql_cache_group_v (group_name, list_name, membership_size, group_id, field_id, group_internal_id, field_internal_id) AS select gg.name group_name, gf.name list_name, membership_size,  gg.id group_id, gf.id field_id, gg.internal_id group_internal_id, gf.internal_id field_internal_id  from grouper_sql_cache_group gscg, grouper_fields gf, grouper_groups gg  where gscg.group_internal_id = gg.internal_id and gscg.field_internal_id = gf.internal_id").executeSql();
        
        if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
          
          if (GrouperDdlUtils.isPostgres()) {
            new GcDbAccess().sql("COMMENT ON VIEW grouper_sql_cache_group_v IS 'SQL cache group view'").executeSql();
          } else {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_sql_cache_group_v IS 'SQL cache group view'").executeSql();
          }
          
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group_v.group_name IS 'group_name: name of group'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group_v.list_name IS 'list_name: name of list: members or the privilege like admins'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group_v.membership_size IS 'membership_size: approximate number of memberships in the group'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group_v.group_id IS 'group_id: uuid of the group'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group_v.field_id IS 'field_id: uuid of the field'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group_v.group_internal_id IS 'group_internal_id: group internal id'").executeSql();
          new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_group_v.field_internal_id IS 'field_internal_id: field internal id'").executeSql();
        }
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added view grouper_sql_cache_group_v");
        }
      }
    }
    
    
    
    boolean groupsNullable = GrouperDdlUtils.isColumnNullable("grouper_groups", "internal_id", "name", GrouperCheckConfig.attributeRootStemName() + ":upgradeTasks:upgradeTasksMetadataGroup");
    boolean fieldsNullable = GrouperDdlUtils.isColumnNullable("grouper_fields", "internal_id", "name", "admins");
    
    if (groupsNullable || fieldsNullable) {
      // ok nulls are allowed so make the change
      GrouperDaemonDeleteOldRecords.verifyTableIdIndexes(null);
    }
    
    if (groupsNullable) {
      String sql = null;
      
      if (GrouperDdlUtils.isOracle()) {
        sql = "ALTER TABLE grouper_groups MODIFY (internal_id NOT NULL)";
      } else if (GrouperDdlUtils.isMysql()) {
        sql = "ALTER TABLE grouper_groups MODIFY internal_id BIGINT NOT NULL";
      } else if (GrouperDdlUtils.isPostgres()) {
        sql = "ALTER TABLE grouper_groups ALTER COLUMN internal_id SET NOT NULL";
      } else {
        throw new RuntimeException("Which database are we????");
      }
      
      new GcDbAccess().sql(sql).executeSql();
    }
    
    if (fieldsNullable) {
      String sql = null;

      if (GrouperDdlUtils.isOracle()) {
        sql = "ALTER TABLE grouper_fields MODIFY (internal_id NOT NULL)";
      } else if (GrouperDdlUtils.isMysql()) {
        sql = "ALTER TABLE grouper_fields MODIFY internal_id BIGINT NOT NULL";
      } else if (GrouperDdlUtils.isPostgres()) {
        sql = "ALTER TABLE grouper_fields ALTER COLUMN internal_id SET NOT NULL";
      } else {
        throw new RuntimeException("Which database are we????");
      }
      
      new GcDbAccess().sql(sql).executeSql();
    }

    // cant add foreign key until this is there
    if (GrouperDdlUtils.isOracle()) {
      
      String sql = "ALTER TABLE grouper_fields ADD CONSTRAINT grouper_fie_internal_id_unq unique (internal_id)";
      
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_fie_internal_id_unq")) {
        try {
          new GcDbAccess().sql(sql).executeSql();
        } catch (Exception e) {
          if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02261")) {
            // throw if the exception is anything other than the constraint already exists
            throw e;
          }
        }
      }
      
      sql = "ALTER TABLE grouper_groups ADD CONSTRAINT grouper_grp_internal_id_unq unique (internal_id)";
      
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_grp_internal_id_unq")) {
        try {
          new GcDbAccess().sql(sql).executeSql();
        } catch (Exception e) {
          if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02261")) {
            // throw if the exception is anything other than the constraint already exists
            throw e;
          }
        }
      }

      sql = "ALTER TABLE grouper_sql_cache_group ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (field_internal_id) REFERENCES grouper_fields(internal_id)";
      
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_sql_cache_group1_fk")) {
        try {
          new GcDbAccess().sql(sql).executeSql();
        } catch (Exception e) {
          if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
            // throw if the exception is anything other than the constraint already exists
            throw e;
          }
        }
      }
    }
  }

}
