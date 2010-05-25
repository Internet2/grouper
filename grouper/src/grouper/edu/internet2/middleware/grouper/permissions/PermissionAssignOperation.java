/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * can be used for generic method for permission assignments
 */
public enum PermissionAssignOperation {
  
  /** if the attribute is there, leave it alone, else assign it */
  assign_permission {

    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionAssignOperation#convertToAttributeAssignOperation()
     */
    @Override
    public AttributeAssignOperation convertToAttributeAssignOperation() {
      return AttributeAssignOperation.assign_attr;
    }
  },
  
  /** replace permissions of this user/role with these new permissions */
  replace_permissions {

    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionAssignOperation#convertToAttributeAssignOperation()
     */
    @Override
    public AttributeAssignOperation convertToAttributeAssignOperation() {
      throw new RuntimeException("Cant convert this to attribute assign operation");
    }
  },
  
  /** remove all assignments of this attribute name, or just certain attributes if by id */
  remove_permission {

    /**
     * 
     * @see edu.internet2.middleware.grouper.permissions.PermissionAssignOperation#convertToAttributeAssignOperation()
     */
    @Override
    public AttributeAssignOperation convertToAttributeAssignOperation() {
      return AttributeAssignOperation.remove_attr;
    }
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static PermissionAssignOperation valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(PermissionAssignOperation.class, 
        string, exceptionOnNull);
  }

  /**
   * convert to attribute assign operation
   * @return type
   */
  public abstract AttributeAssignOperation convertToAttributeAssignOperation();

}
