/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcResultSetCallback;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class TableSyncTest2 {

  /**
   * 
   */
  public TableSyncTest2() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
//    edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync gcTableSync = new edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync();
//    gcTableSync.setKey("membershipsProd");
//    edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput gcTableSyncOutput = gcTableSync.fullSync();
//    gcTableSyncOutput.getMessage();

    
    // go to database from and look up metadata
    new GcDbAccess().connectionName("awsProd")
    .sql("select * from penn_memberships_feeder_v where 1 != 1")
      .callbackResultSet(new GcResultSetCallback() {

      @Override
      public Object callback(ResultSet resultSet) throws Exception {
    
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        
        System.out.println(resultSetMetaData.getCatalogName(1));
        System.out.println(resultSetMetaData.getSchemaName(1));
        System.out.println(resultSetMetaData.getTableName(1));
        
        for (int i=0;i<resultSetMetaData.getColumnCount();i++) {
          String name = resultSetMetaData.getColumnName(i+1);
          System.out.println(name + "," + resultSetMetaData.getColumnTypeName(i+1));
        }
        return null;
      }
      
      });


  }

}
