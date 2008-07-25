/*
 * @author mchyzer
 * $Id: SubjectDdl.java,v 1.1 2008-07-25 06:17:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;



/**
 * ddl versions and stuff for grouper.  All ddl classes must have a currentVersion method that
 * returns the current version
 */
public enum SubjectDdl implements DdlVersionable {

  /** first version of grouper, make sure the ddl table is there */
  V1 {
    /**
     * add the table grouploader_log for logging and detect and add columns
     * @see SubjectDdl#updateVersionFromPrevious(org.apache.ddlutils.model.Database)
     */
    @Override
    public void updateVersionFromPrevious(Database database) {

      {
        Table subjectTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            "subject", "sample subject table for grouper unit tests");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "subjectId", 
            "subject id of row", Types.VARCHAR, "255", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "subjectTypeId", 
            "subject type e.g. person", 
            Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "name", 
            "name of this subject",
            Types.VARCHAR, "255", false, false);
  
      }
      {
        Table subjectTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            "subjectattribute", "attribute data for each subject");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "subjectId", 
            "subject id of row", Types.VARCHAR, "255", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "name", 
            "name of attribute", 
            Types.VARCHAR, "255", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "value", 
            "value of attribute",
            Types.VARCHAR, "255", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "searchValue", 
            "search value (e.g. all lower)",
            Types.VARCHAR, "255", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, subjectTable.getName(), 
            "searchattribute_value_idx", true, "value");

      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "subjectattribute", 
          "fk_subjectattr_subjectid", "subject", "subjectId", "subjectId");

      
    }
  };

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getVersion()
   */
  public int getVersion() {
    return GrouperDdlUtils.versionIntFromEnum(this);
  }

  /**
   * cache this
   */
  private static int currentVersion = -1;
  
  /**
   * keep the current version here, increment as things change
   * @return the current version
   */
  public static int currentVersion() {
    if (currentVersion == -1) {
      int max = -1;
      for (SubjectDdl grouperDdl : SubjectDdl.values()) {
        String number = grouperDdl.name().substring(1);
        int theInt = Integer.parseInt(number);
        max = Math.max(max, theInt);
      }
      currentVersion = max;
    }
    return currentVersion;
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
    return "SUBJECT%";
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database)
   */
  public abstract void updateVersionFromPrevious(Database database);  
}
