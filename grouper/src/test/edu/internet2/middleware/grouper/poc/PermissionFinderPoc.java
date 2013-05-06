/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;


/**
 *
 */
public class PermissionFinderPoc {

  /**
   * @param args
   */
  public static void main(String[] args) {

    for (PermissionEntry permissionEntry : new PermissionFinder().assignPermissionType(PermissionType.role)
        .assignImmediateOnly(true).addPermissionName("a:b").findPermissions()) {
      System.out.println(permissionEntry.getRoleName());
    }
    
    for (PermissionEntry permissionEntry : new PermissionFinder().assignPermissionType(PermissionType.role)
        .assignImmediateOnly(true).addRole("a:b").findPermissions()) {
      System.out.println(permissionEntry.getAttributeDefNameName());
    }
    
  }

}
