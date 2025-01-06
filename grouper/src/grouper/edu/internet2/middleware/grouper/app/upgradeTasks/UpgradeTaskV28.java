package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyTypeDao;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV28 implements UpgradeTasksInterface {
  
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
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_depend_type")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_depend_type", "grouper_sql_cache_deptype1_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_dependency")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_dependency", "grouper_sql_cache_dep1_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_dependency", "grouper_sql_cache_dep2_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_dependency", "grouper_sql_cache_dep3_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertForeignKeyExists("grouper_sql_cache_dependency", "grouper_sql_cache_dep_fk")) {
      return true;
    }
    
    return false;
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_depend_type")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_sql_cache_depend_type (
                  internal_id NUMBER(38) NOT NULL,
                  dependency_category varchar2(100) NOT NULL,
                  name varchar2(100) NOT NULL,
                  description varchar2(1024) NOT NULL, 
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_sql_cache_depend_type");
            }
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_sql_cache_depend_type (
                  internal_id bigint NOT NULL,
                  dependency_category varchar(100) NOT NULL,
                  name varchar(100) NOT NULL,
                  description varchar(1024) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_sql_cache_depend_type");
            }
          }
          
          if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_sql_cache_depend_type IS 'table to store types of dependencies'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_depend_type.internal_id IS 'primary key of the table'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_depend_type.dependency_category IS 'category of dependency type'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_depend_type.name IS 'name of dependency type'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_depend_type.description IS 'description of dependency type'").executeSql();
          }
          
          // add rows
          SqlCacheDependencyTypeDao.addDefaultSqlCacheDependencyTypesIfNecessary();
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_depend_type", "grouper_sql_cache_deptype1_idx")) {
          new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_sql_cache_deptype1_idx ON grouper_sql_cache_depend_type (dependency_category, name)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_deptype1_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_sql_cache_dependency")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_sql_cache_dependency (
                  internal_id NUMBER(38) NOT NULL,
                  dep_type_internal_id NUMBER(38) NOT NULL, 
                  owner_internal_id NUMBER(38) NOT NULL,
                  dependent_internal_id NUMBER(38) NOT NULL,
                  created_on NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_sql_cache_dependency");
            }
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_sql_cache_dependency (
                  internal_id bigint NOT NULL,
                  dep_type_internal_id bigint NOT NULL,
                  owner_internal_id bigint NOT NULL,
                  dependent_internal_id bigint NOT NULL,
                  created_on bigint NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_sql_cache_dependency");
            }
          }
          
          if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isPostgres()) {
            new GcDbAccess().sql("COMMENT ON TABLE grouper_sql_cache_dependency IS 'table to store dependencies'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_dependency.internal_id IS 'primary key of the table'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_dependency.dep_type_internal_id IS 'foreign key to grouper_sql_cache_depend_type table'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_dependency.owner_internal_id IS 'Something that something else is dependent on.  If something in the owner changes, then the dependent object might need to change'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_dependency.dependent_internal_id IS 'This is the internal id of the dependent object.  Check all the dependent objects if something changes in owner'").executeSql();
            new GcDbAccess().sql("COMMENT ON COLUMN grouper_sql_cache_dependency.created_on IS 'when this row was created'").executeSql();
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_dependency", "grouper_sql_cache_dep1_idx")) {
          new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_sql_cache_dep1_idx ON grouper_sql_cache_dependency (dep_type_internal_id, owner_internal_id, dependent_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_dep1_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_dependency", "grouper_sql_cache_dep2_idx")) {
          new GcDbAccess().sql("CREATE INDEX grouper_sql_cache_dep2_idx ON grouper_sql_cache_dependency (owner_internal_id, dependent_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_dep2_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sql_cache_dependency", "grouper_sql_cache_dep3_idx")) {
          new GcDbAccess().sql("CREATE INDEX grouper_sql_cache_dep3_idx ON grouper_sql_cache_dependency (dependent_internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sql_cache_dep3_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_sql_cache_dependency", "grouper_sql_cache_dep_fk")) {
          new GcDbAccess().sql("ALTER TABLE grouper_sql_cache_dependency ADD CONSTRAINT grouper_sql_cache_dep_fk FOREIGN KEY (dep_type_internal_id) REFERENCES grouper_sql_cache_depend_type(internal_id)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key grouper_sql_cache_dep_fk");
          }
        }
        
        return null;
      }
    });
  }

}
