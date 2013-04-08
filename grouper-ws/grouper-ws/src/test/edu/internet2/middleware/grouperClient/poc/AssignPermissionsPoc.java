/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

import edu.internet2.middleware.grouperClient.api.GcAssignPermissions;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignPermissionsResults;


/**
 *
 */
public class AssignPermissionsPoc {

  /**
   * @param args
   */
  public static void main(String[] args) {

    WsAssignPermissionsResults wsAssignPermissionsResults = new GcAssignPermissions().addAction("assign")
      .addPermissionDefNameName("test:poc:pocPerm").addRoleName("test:poc:pocRole")
      .assignPermissionAssignOperation("assign_permission").assignPermissionType("role").execute();
    
    System.out.println(wsAssignPermissionsResults.getResultMetadata().getResultCode());
    
  }

}
