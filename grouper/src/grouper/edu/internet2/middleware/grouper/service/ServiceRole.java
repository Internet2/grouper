/*******************************************************************************
 * Copyright 2014 Internet2
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
