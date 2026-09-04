package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * order that create/update/delete operations are sent to the target in
 * GrouperProvisionerTargetDaoAdapter.sendChangesToTarget().  Note this only applies to daos
 * which do not implement canSendChangesToTarget, since those daos receive all the changes in one
 * call and pick their own order.
 */
public enum GrouperProvisioningCrudOperationOrder {

  /**
   * membership deletes, then group changes, then entity changes, then membership inserts/updates/replaces.
   * This is the default and it is required for targets which need a group emptied before it can be
   * removed, and for capacity limited targets which need room freed up before inserts run.
   */
  deletesFirst,

  /**
   * group inserts/updates, then entity inserts/updates, then membership inserts/updates/replaces,
   * then membership deletes, then group deletes, then entity deletes.  Use this when gaining access
   * late is a more expensive failure than losing access late, e.g. a large full sync where a long
   * delete phase would otherwise block all the inserts.  Note a rename within a single run will
   * collide since the old object is still in the target when the insert runs.
   */
  insertsFirst;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnBlank will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProvisioningCrudOperationOrder valueOfIgnoreCase(String string, boolean exceptionOnBlank) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningCrudOperationOrder.class,
        string, exceptionOnBlank);
  }
}
