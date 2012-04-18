/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.pit;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBase;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitDocumentation;

/**
 * test limit
 * @author mchyzer
 *
 */
public class PITPermissionLimit extends PermissionLimitBase {

  /**
   * 
   */
  public PITPermissionLimit() {
    
  }

  /**
   * @see PermissionLimitBase#allowPermission(PermissionEntry, AttributeAssign, Set, Map, Set)
   */
  public boolean allowPermission(PermissionEntry permissionEntry,
      AttributeAssign limitAssignment, Set<AttributeAssignValue> limitAssignmentValues,
      Map<String, Object> limitEnvVars, Set<PermissionLimitBean> permissionLimitBeans) {
    return true;
  }

  /**
   * @see PermissionLimitBase#documentation()
   */
  public PermissionLimitDocumentation documentation() {
    return null;
  }

  /**
   * @see PermissionLimitBase#validateLimitAssignValue(AttributeAssign, Set)
   */
  public PermissionLimitDocumentation validateLimitAssignValue(
      AttributeAssign limitAssign, Set<AttributeAssignValue> limitAssignmentValues) {
    return null;
  }

}
