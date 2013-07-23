package edu.internet2.middleware.grouper.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * role in a service, admin (update or admin in service) or user (member of group/role or assignee of permissions)
 * @author mchyzer
 *
 */
public enum ServiceRole {
  
  /** admin of a service */
  admin {

    /**
     * @see ServiceRole#fieldsForGroupQuery()
     */
    @Override
    public Collection<Field> fieldsForGroupQuery() {
      return GrouperUtil.toSet(AccessPrivilege.ADMIN.getField(),
          AccessPrivilege.UPDATE.getField());
    }
  },
  
  /** user of a service (might include admins) */
  user {

    /**
     * @see ServiceRole#fieldsForGroupQuery()
     */
    @Override
    public Collection<Field> fieldsForGroupQuery() {
      return GrouperUtil.toSet(Group.getDefaultList());
    }
  };
  
  /**
   * get all fields for any role
   * @return the fields
   */
  public static Collection<Field> allFieldsForGroupQuery() {
    Set<Field> allFields = new HashSet<Field>();
    for (ServiceRole serviceRole : ServiceRole.values()) {
      allFields.addAll(serviceRole.fieldsForGroupQuery());
    }
    return allFields;
  }
  
  /**
   * e.g. return the fields for this service role
   * @return the fields for the query
   */
  public abstract Collection<Field> fieldsForGroupQuery();
  
  /**
   * convert a string to the service role enum
   * @param string
   * @param exceptionOnNull
   * @return service role
   */
  public static ServiceRole valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(ServiceRole.class, string, exceptionOnNull, true);
  }
  
}
