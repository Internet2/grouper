/*
 * @author mchyzer
 * $Id: GrouperTestDdl.java,v 1.1 2008-11-08 08:15:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;



/**
 * test tables for junit
 */
public enum GrouperTestDdl implements DdlVersionable {

  /** first version of grouper, make sure the ddl table is there */
  V1 {
    /**
     * add the table testgrouper_loader
     * @see GrouperTestDdl#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, DdlVersionBean ddlVersionBean) {

      {
  
      }
      
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
      for (GrouperTestDdl grouperDdl : GrouperTestDdl.values()) {
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
    return "TESTGROUPER%";
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
   */
  public abstract void updateVersionFromPrevious(Database database, 
      DdlVersionBean ddlVersionBean);  
  
  /**
   * add all foreign keys
   * @param ddlVersionBean 
   */
  public void addAllForeignKeysViewsEtc(DdlVersionBean ddlVersionBean) {

  }
  
  /**
   * an example table name so we can hone in on the exact metadata
   * @return the table name
   */
  public String[] getSampleTablenames() {
    return new String[]{"testgrouper_loader"};
  }


}
