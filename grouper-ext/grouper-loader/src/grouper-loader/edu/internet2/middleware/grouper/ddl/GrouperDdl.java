/*
 * @author mchyzer
 * $Id: GrouperDdl.java,v 1.4 2008-05-13 19:30:00 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.loader.util.GrouperDdlUtils;


/**
 * ddl versions and stuff for grouper.  All ddl classes must have a currentVersion method that
 * returns the current version
 */
public enum GrouperDdl implements DdlVersionable {

  /** second version of grouper */
  V1 {
    /**
     * add columns for last_edited, and unique constraint on name
     * @see edu.internet2.middleware.grouper.ddl.GrouperLoaderDdl#updateVersionFromPrevious(org.apache.ddlutils.model.Database)
     */
    @Override
    public void updateVersionFromPrevious(Database database) {
      
      //see if the grouper_ext_loader_log table is there
      Table grouperDdlTable = GrouperDdlUtils.ddlutilsFindTable(database,"grouper_ddl");
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "last_updated", 
          "last update timestamp, string so it can easily be used from update statement", 
          Types.VARCHAR, "50", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "history", 
          "history of this object name, with most recent first (truncated after 4k)", 
          Types.VARCHAR, "4000", false, false);
      
      //object name is unique
      GrouperDdlUtils.ddlutilsAddIndex(database, "grouper_ddl", "grouper_ddl_object_name_idx", 
          true, "object_name");
      
    }
  }, 
  /** first version of grouper */
  V0 {
    /**
     * add the table grouploader_log for logging and detect and add columns
     * @see edu.internet2.middleware.grouper.ddl.GrouperLoaderDdl#updateVersionFromPrevious(org.apache.ddlutils.model.Database)
     */
    @Override
    public void updateVersionFromPrevious(Database database) {

      //see if the grouper_ext_loader_log table is there
      Table grouperDdlTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"grouper_ddl", 
          "holds a record for each database object name, and db version, and java version");

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "id", "uuid of this ddl record", 
          Types.VARCHAR, "128", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "object_name", 
          "Corresponds to an enum in grouper.ddl package (with Ddl on end), represents one module, " +
          "so grouper itself is one object", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "db_version", 
          "Version of this object as far as DB knows about", Types.INTEGER, null, false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDdlTable, "java_version", 
          "Version of this object as far as Java knows about (should be greater or equal to db version)", 
          Types.INTEGER, null, false, false);
      
    }
  };

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getVersion()
   */
  public int getVersion() {
    return GrouperDdlUtils.versionIntFromEnum(this);
  }
  
  /**
   * keep the current version here, increment as things change
   * @return the current version
   */
  public static int currentVersion() {
    return 1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getObjectName()
   */
  public String getObjectName() {
    return GrouperDdlUtils.objectName(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getDefaultTablePattern()
   */
  public String getDefaultTablePattern() {
    return "GROUPER%";
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database)
   */
  public abstract void updateVersionFromPrevious(Database database);  
}
