package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.sql.Types;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV33 implements UpgradeTasksInterface {
  
  @Override
  public boolean runOnNewInstall() {
    return true;
  }
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
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
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship1_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship2_idx")) {
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
    
    // Copying from UpgradeTaskV15 in Grouper v5
    {
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
    }
    
    // Copying from UpgradeTaskV21 in Grouper v5
    {
      if (GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship") && !GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_group", "last_membership_sync")) {
        return true;
      }
      
      if (GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship2_idx")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertPrimaryKeyExists("grouper_sql_cache_mship")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_group", "grouper_sql_cache_group2_idx")) {
        return true;
      }
    }
    
    // Copying from UpgradeTaskV22 in Grouper v5
    {
      if (GrouperDdlUtils.assertTableThere(true, "grouper_pit_stems") && !GrouperDdlUtils.assertColumnThere(true, "grouper_pit_stems", "source_id_index")) {
        return true;
      }
      
      if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_attribute_def", "source_id_index")) {
       return true;
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_pit_stems", "pit_stem_source_idindex_idx")) {
       return true;
      }
      
      if (!GrouperDdlUtils.assertIndexExists("grouper_pit_attribute_def", "pit_attrdef_source_idindex_idx")) {
        return true;
      }
    }
    
    // Copying from UpgradeTaskV24 in Grouper v5
    {
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
    }
    
    return false;
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.19.0");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
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
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship1_idx")) {
          new GcDbAccess().sql("CREATE INDEX grouper_sql_cache_mship1_idx ON grouper_sql_cache_mship (sql_cache_group_internal_id, flattened_add_timestamp)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_mship1_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship2_idx")) {
          new GcDbAccess().sql("CREATE INDEX grouper_sql_cache_mship2_idx ON grouper_sql_cache_mship (member_internal_id, sql_cache_group_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_mship2_idx");
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
        
        // Copying from UpgradeTaskV15 in Grouper v5
        {
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
        
        // Copying from UpgradeTaskV21 in Grouper v5
        {          
          if (GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_mship_v")) {
            if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship_v", "mship_hst_internal_id")) {
              HibernateSession.bySqlStatic().executeSql("DROP VIEW grouper_sql_cache_mship_v");
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped view grouper_sql_cache_mship_v");
            }
          }
          
          if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "internal_id")) {
            HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_mship DROP COLUMN internal_id");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped column grouper_sql_cache_mship.internal_id");
          }
          
          if (GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_mship", "created_on")) {
            HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_mship DROP COLUMN created_on");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped column grouper_sql_cache_mship.created_on");
          }
          
          if (!GrouperDdlUtils.assertColumnThere(true, "grouper_sql_cache_group", "last_membership_sync")) {
            if (GrouperDdlUtils.isOracle()) {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_group ADD last_membership_sync DATE");
            } else if (GrouperDdlUtils.isMysql()) {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_group ADD COLUMN last_membership_sync DATETIME");
            } else {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_group ADD COLUMN last_membership_sync timestamp");
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_sql_cache_group.last_membership_sync");
          }
          
          if (GrouperDdlUtils.assertIndexExists("grouper_sql_cache_mship", "grouper_sql_cache_mship2_idx")) {
            if (GrouperDdlUtils.isMysql()) {
              HibernateSession.bySqlStatic().executeSql("DROP INDEX grouper_sql_cache_mship2_idx ON grouper_sql_cache_mship");
            } else {
              HibernateSession.bySqlStatic().executeSql("DROP INDEX grouper_sql_cache_mship2_idx");
            }
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index grouper_sql_cache_mship2_idx");
          }
          
          if (!GrouperDdlUtils.assertPrimaryKeyExists("grouper_sql_cache_mship")) {
            HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_sql_cache_mship ADD PRIMARY KEY (member_internal_id, sql_cache_group_internal_id)");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added primary key to grouper_sql_cache_mship");
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_group", "grouper_sql_cache_group2_idx")) {
            HibernateSession.bySqlStatic().executeSql("CREATE INDEX grouper_sql_cache_group2_idx ON grouper_sql_cache_group (last_membership_sync)");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_group2_idx");
          }
        }
        
        // Copying from UpgradeTaskV22 in Grouper v5
        {
          if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_stems", "source_id_index")) {
            if (GrouperDdlUtils.isOracle()) {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE GROUPER_PIT_STEMS ADD source_id_index NUMBER(38)");
            } else {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_pit_stems ADD COLUMN source_id_index BIGINT");
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_pit_stems.source_id_index");
          }
         
          if (!GrouperDdlUtils.assertColumnThere(true, "grouper_pit_attribute_def", "source_id_index")) {
            if (GrouperDdlUtils.isOracle()) {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE GROUPER_PIT_ATTRIBUTE_DEF ADD source_id_index NUMBER(38)");
            } else {
              HibernateSession.bySqlStatic().executeSql("ALTER TABLE grouper_pit_attribute_def ADD COLUMN source_id_index BIGINT");
            }
            
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_pit_attribute_def.source_id_index");
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_pit_stems", "pit_stem_source_idindex_idx")) {
            HibernateSession.bySqlStatic().executeSql("CREATE INDEX pit_stem_source_idindex_idx ON grouper_pit_stems (source_id_index)");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index pit_stem_source_idindex_idx");
          }
          
          if (!GrouperDdlUtils.assertIndexExists("grouper_pit_attribute_def", "pit_attrdef_source_idindex_idx")) {
            HibernateSession.bySqlStatic().executeSql("CREATE INDEX pit_attrdef_source_idindex_idx ON grouper_pit_attribute_def (source_id_index)");
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index pit_attrdef_source_idindex_idx");
          }
        }
        
        // Copying from UpgradeTaskV24 in Grouper v5
        {
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
        }
        
        return null;
      }
    });
  }

}
