package edu.internet2.middleware.grouper.permissions.limits;


/**
 * the type of permission limit we are referring to
 * @author mchyzer
 *
 */
public enum PermissionLimitBeanType {

  /** a permission limit assigned to an attribute assignment (e.g. assigned to a permission assignment) */
  ATTRIBUTE_ASSIGNMENT,
  
  /** a permission limit assigned to a role (applies to all users in that role for permissions in the context of that role */
  ROLE;
  
}
