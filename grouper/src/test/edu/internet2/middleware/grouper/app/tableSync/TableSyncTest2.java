/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcResultSetCallback;


/**
 *
 */
public class TableSyncTest2 {

  //def etc:provisioning:provisioningValueDef
  
  //name etc:provisioning:provisioningTarget personSourceTest
  
  //name etc:provisioning:provisioningDoProvision true
  
//  SELECT distinct gg.id as group_id, gg.name,
//  gm.SUBJECT_ID, GM.ID as member_id
//FROM grouper_memberships ms, grouper_group_set gs, grouper_groups gg, grouper_fields gfl, grouper_members gm,
//  grouper_attribute_assign gaa_marker, grouper_attribute_def_name gadn_marker,
//  grouper_attribute_assign gaa_do_provision, grouper_attribute_def_name gadn_do_provision, grouper_attribute_assign_value gaav_do_provision,
//  grouper_attribute_assign gaa_target, grouper_attribute_def_name gadn_target, grouper_attribute_assign_value gaav_target         
//WHERE ms.owner_id = gs.member_id AND ms.field_id = gs.member_field_id and ms.enabled = 'T'
//  and gs.OWNER_GROUP_ID = gg.id AND gs.FIELD_ID = gfl.ID and GM.SUBJECT_SOURCE = 'jdbc'
//  and gg.name = 'test:testGroup' and gfl.name = 'members'
//  and gg.id = gaa_marker.owner_group_id and gaa_marker.id = gaa_do_provision.owner_attribute_assign_id and gaa_marker.id = gaa_target.owner_attribute_assign_id
//  AND gaa_marker.attribute_def_name_id = gadn_marker.id and gadn_marker.name = 'etc:provisioning:provisioningMarker'
//     AND gaa_marker.enabled = 'T'
//  AND gaa_do_provision.attribute_def_name_id = gadn_do_provision.id and gadn_do_provision.name = 'etc:provisioning:provisioningDoProvision'
//      AND gaa_do_provision.enabled = 'T' and gaav_do_provision.attribute_assign_id = gaa_do_provision.id and gaav_do_provision.value_string = 'true'
//  AND gaa_target.attribute_def_name_id = gadn_target.id and gadn_target.name = 'etc:provisioning:provisioningTarget'
//      AND gaa_target.enabled = 'T' and gaav_target.attribute_assign_id = gaa_targe  

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
