/*
 * @author mchyzer
 * $Id: OrgDataLoader.java,v 1.2 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.poc;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;

/**
 *
 */
public class OrgDataLoader {

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  /**
   * 
   */
  public static void dropOrgDataTable() {
    
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();

        {
          Table loaderTable = database.findTable("testgrouper_loader");
          
          if (loaderTable != null) {
            database.removeTable(loaderTable);
          }
        }
        {
          Table loaderGroupsTable = database.findTable("testgrouper_loader_groups");
          
          if (loaderGroupsTable != null) {
            database.removeTable(loaderGroupsTable);
          }
          
        }
        
      }
      
    });

  }
  
}
