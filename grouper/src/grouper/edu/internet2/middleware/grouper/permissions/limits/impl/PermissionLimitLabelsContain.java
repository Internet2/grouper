/**
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
 */
package edu.internet2.middleware.grouper.permissions.limits.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.LimitElUtils;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBase;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitDocumentation;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * logic for the built in to see if user has a label in a set of labels
 * @author mchyzer
 */
public class PermissionLimitLabelsContain extends PermissionLimitBase {

  /**
   * @see PermissionLimitInterface#allowPermission(PermissionEntry, AttributeAssign, Set, Map, Set)
   */
  public boolean allowPermission(PermissionEntry permissionEntry,
      AttributeAssign limitAssignment, Set<AttributeAssignValue> limitAssignmentValues,
      Map<String, Object> limitEnvVars, Set<PermissionLimitBean> permissionLimitBeans) {
    
    Object userLabels = limitEnvVars.get("labels");
    
    if (!limitEnvVars.containsKey("labels")) {
      throw new RuntimeException("You need to pass in the environment variable 'labels' even if the value is blank");
    }

    //get what the limit is
    String theLimit = null;
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      theLimit = limitAssignmentValues.iterator().next().getValueString();
    }

    if (StringUtils.isBlank(theLimit)) {
      throw new RuntimeException("There is a labelsContain limit assigned (assignment id: " + limitAssignment.getId() 
          + ") which does not have a value associated with it, it must have a value!"); 
    }
    
    return LimitElUtils.labelsContain((String)userLabels, theLimit);
    
  }

  /**
   * @see PermissionLimitInterface#documentation()
   */
  public PermissionLimitDocumentation documentation() {
    PermissionLimitDocumentation permissionLimitDocumentation = new PermissionLimitDocumentation();
    permissionLimitDocumentation.setDocumentationKey("grouperPermissionLabelsContain.doc");
    
    return permissionLimitDocumentation;
  }


  /**
   * @see PermissionLimitInterface#validateLimitAssignValue(AttributeAssign, Set)
   */
  public PermissionLimitDocumentation validateLimitAssignValue(AttributeAssign limitAssign, Set<AttributeAssignValue> limitAssignmentValues) {
    
    //lets see what values we have...
    String value = null;
    
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      value = limitAssignmentValues.iterator().next().getValueString();
    }
    
    if (StringUtils.isBlank(value)) {
      return new PermissionLimitDocumentation("grouperPermissionLabelsContain.required");
    }
    
    return null;
  
  }

}
