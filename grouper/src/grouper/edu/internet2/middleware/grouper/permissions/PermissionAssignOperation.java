/**
 * Copyright 2012 Internet2
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
 */
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
      return AttributeAssignOperation.replace_attrs;
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
