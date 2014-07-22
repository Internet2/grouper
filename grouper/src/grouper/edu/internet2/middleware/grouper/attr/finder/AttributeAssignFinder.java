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
/**
 * @author mchyzer
 * $Id: AttributeDefFinder.java,v 1.2 2009-09-28 20:30:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;


/**
 * finder methods for attribute assign
 */
public class AttributeAssignFinder {

  /**
   * find an attributeAssign by id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeAssign
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute assign or null
   * @throws AttributeAssignNotFoundException
   */
  public static AttributeAssign findById(String id, boolean exceptionIfNull) {
    
    AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(id, exceptionIfNull);
    
    //at this point no exception should be thrown
    if (attributeAssign == null) {
      return null;
    }
    
    //now we need to check security
    if (PrivilegeHelper.canViewAttributeAssign(GrouperSession.staticGrouperSession(), attributeAssign, true)) {
      return attributeAssign;
    }
    if (exceptionIfNull) {
      throw new AttributeAssignNotFoundException("Not allowed to view attribute assign by id: " + id);
    }
    return null;
  }  

}
