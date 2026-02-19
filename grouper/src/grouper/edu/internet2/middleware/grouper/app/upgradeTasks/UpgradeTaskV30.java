package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV30 implements UpgradeTasksInterface {
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_data_field_assign_hst")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign_hst", "data_field_assign_hst1_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign_hst", "data_field_assign_hst2_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign_hst", "data_field_assign_hst3_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign_hst", "data_field_assign_hst4_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_field_assign_hst", "data_field_assign_hst_fk_1")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_field_assign_hst", "data_field_assign_hst_fk_2")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_field_assign_hst", "data_field_assign_hst_fk_3")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_data_row_assign_hst")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_assign_hst", "data_row_assign_hst1_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_assign_hst", "data_row_assign_hst2_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_assign_hst", "data_row_assign_hst3_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_assign_hst", "data_row_assign_hst_fk_1")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_assign_hst", "data_row_assign_hst_fk_2")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_data_row_field_asn_hst")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst1_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst2_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst3_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst4_idx")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst_fk_1")) {
      return true;
    }
    
    if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst_fk_2")) {
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
    return GrouperVersion.valueOfIgnoreCase("5.16.0");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_data_field_assign_hst")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_data_field_assign_hst (
                  internal_id NUMBER(38) NOT NULL,
                  member_internal_id NUMBER(38) NOT NULL,
                  data_field_internal_id NUMBER(38) NOT NULL,
                  start_time NUMBER(38) NOT NULL,
                  end_time NUMBER(38) NOT NULL,
                  value_integer NUMBER(38) NULL,
                  value_dictionary_internal_id NUMBER(38) NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_data_field_assign_hst");
            }
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_data_field_assign_hst (
                  internal_id BIGINT NOT NULL,
                  member_internal_id BIGINT NOT NULL,
                  data_field_internal_id BIGINT NOT NULL,
                  start_time BIGINT NOT NULL,
                  end_time BIGINT NOT NULL,
                  value_integer BIGINT NULL,
                  value_dictionary_internal_id BIGINT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_data_field_assign_hst");
            }
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign_hst", "data_field_assign_hst1_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_field_assign_hst1_idx ON grouper_data_field_assign_hst (data_field_internal_id, value_dictionary_internal_id, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_field_assign_hst1_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign_hst", "data_field_assign_hst2_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_field_assign_hst2_idx ON grouper_data_field_assign_hst (data_field_internal_id, value_integer, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_field_assign_hst2_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign_hst", "data_field_assign_hst3_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_field_assign_hst3_idx ON grouper_data_field_assign_hst (member_internal_id, data_field_internal_id, value_dictionary_internal_id, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_field_assign_hst3_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_field_assign_hst", "data_field_assign_hst4_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_field_assign_hst4_idx ON grouper_data_field_assign_hst (member_internal_id, data_field_internal_id, value_integer, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_field_assign_hst4_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_field_assign_hst", "data_field_assign_hst_fk_1")) {
          try {
            new GcDbAccess().sql("ALTER TABLE grouper_data_field_assign_hst ADD CONSTRAINT data_field_assign_hst_fk_1 FOREIGN KEY (member_internal_id) REFERENCES grouper_members(internal_id)").executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
              throw e;
            }
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_field_assign_hst_fk_1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_field_assign_hst", "data_field_assign_hst_fk_2")) {
          try {
            new GcDbAccess().sql("ALTER TABLE grouper_data_field_assign_hst ADD CONSTRAINT data_field_assign_hst_fk_2 FOREIGN KEY (data_field_internal_id) REFERENCES grouper_data_field(internal_id)").executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
              throw e;
            }
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_field_assign_hst_fk_2");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_field_assign_hst", "data_field_assign_hst_fk_3")) {
          try {
            new GcDbAccess().sql("ALTER TABLE grouper_data_field_assign_hst ADD CONSTRAINT data_field_assign_hst_fk_3 FOREIGN KEY (value_dictionary_internal_id) REFERENCES grouper_dictionary(internal_id)").executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
              throw e;
            }
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_field_assign_hst_fk_3");
          }
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_data_row_assign_hst")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_data_row_assign_hst (
                  internal_id NUMBER(38) NOT NULL,
                  member_internal_id NUMBER(38) NOT NULL,
                  data_row_internal_id NUMBER(38) NOT NULL,
                  data_row_assign_internal_id NUMBER(38) NOT NULL,
                  start_time NUMBER(38) NOT NULL,
                  end_time NUMBER(38) NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_data_row_assign_hst");
            }
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_data_row_assign_hst (
                  internal_id BIGINT NOT NULL,
                  member_internal_id BIGINT NOT NULL,
                  data_row_internal_id BIGINT NOT NULL,
                  data_row_assign_internal_id BIGINT NOT NULL,
                  start_time BIGINT NOT NULL,
                  end_time BIGINT NOT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_data_row_assign_hst");
            }
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_assign_hst", "data_row_assign_hst1_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_row_assign_hst1_idx ON grouper_data_row_assign_hst (data_row_internal_id, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_assign_hst1_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_assign_hst", "data_row_assign_hst2_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_row_assign_hst2_idx ON grouper_data_row_assign_hst (data_row_assign_internal_id, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_assign_hst2_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_assign_hst", "data_row_assign_hst3_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_row_assign_hst3_idx ON grouper_data_row_assign_hst (member_internal_id, data_row_internal_id, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_assign_hst3_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_assign_hst", "data_row_assign_hst_fk_1")) {
          try {
            new GcDbAccess().sql("ALTER TABLE grouper_data_row_assign_hst ADD CONSTRAINT data_row_assign_hst_fk_1 FOREIGN KEY (member_internal_id) REFERENCES grouper_members(internal_id)").executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
              throw e;
            }
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_row_assign_hst_fk_1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_assign_hst", "data_row_assign_hst_fk_2")) {
          try {
            new GcDbAccess().sql("ALTER TABLE grouper_data_row_assign_hst ADD CONSTRAINT data_row_assign_hst_fk_2 FOREIGN KEY (data_row_internal_id) REFERENCES grouper_data_row(internal_id)").executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
              throw e;
            }
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_row_assign_hst_fk_2");
          }
        }
        
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_data_row_field_asn_hst")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_data_row_field_asn_hst (
                  internal_id NUMBER(38) NOT NULL,
                  data_row_assign_internal_id NUMBER(38) NOT NULL,
                  data_field_internal_id NUMBER(38) NOT NULL,
                  start_time NUMBER(38) NOT NULL,
                  end_time NUMBER(38) NOT NULL,
                  value_integer NUMBER(38) NULL,
                  value_dictionary_internal_id NUMBER(38) NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_data_row_field_asn_hst");
            }
          } else {
            new GcDbAccess().sql("""
                CREATE TABLE grouper_data_row_field_asn_hst (
                  internal_id BIGINT NOT NULL,
                  data_row_assign_internal_id BIGINT NOT NULL,
                  data_field_internal_id BIGINT NOT NULL,
                  start_time BIGINT NOT NULL,
                  end_time BIGINT NOT NULL,
                  value_integer BIGINT NULL,
                  value_dictionary_internal_id BIGINT NULL,
                  PRIMARY KEY (internal_id)
                )
              """).executeSql();
            
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_data_row_field_asn_hst");
            }
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst1_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_row_field_asn_hst1_idx ON grouper_data_row_field_asn_hst (data_row_assign_internal_id, data_field_internal_id, value_dictionary_internal_id, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_field_asn_hst1_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst2_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_row_field_asn_hst2_idx ON grouper_data_row_field_asn_hst (data_row_assign_internal_id, data_field_internal_id, value_integer, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_field_asn_hst2_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst3_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_row_field_asn_hst3_idx ON grouper_data_row_field_asn_hst (data_field_internal_id, value_dictionary_internal_id, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_field_asn_hst3_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst4_idx")) {
          new GcDbAccess().sql("CREATE INDEX data_row_field_asn_hst4_idx ON grouper_data_row_field_asn_hst (data_field_internal_id, value_integer, end_time)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index data_row_field_asn_hst4_idx");
          }
        }
        
        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst_fk_1")) {
          try {
            new GcDbAccess().sql("ALTER TABLE grouper_data_row_field_asn_hst ADD CONSTRAINT data_row_field_asn_hst_fk_1 FOREIGN KEY (data_field_internal_id) REFERENCES grouper_data_field(internal_id)").executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
              throw e;
            }
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_row_field_asn_hst_fk_1");
          }
        }

        if (!GrouperDdlUtils.assertForeignKeyExists("grouper_data_row_field_asn_hst", "data_row_field_asn_hst_fk_2")) {
          try {
            new GcDbAccess().sql("ALTER TABLE grouper_data_row_field_asn_hst ADD CONSTRAINT data_row_field_asn_hst_fk_2 FOREIGN KEY (value_dictionary_internal_id) REFERENCES grouper_dictionary(internal_id)").executeSql();
          } catch (Exception e) {
            if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02275")) {
              throw e;
            }
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added foreign key data_row_field_asn_hst_fk_2");
          }
        }
        
        return null;
      }
    });
  }

}
