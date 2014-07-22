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
package edu.internet2.middleware.grouper.pit.finder;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.PITGroup;

/**
 * Find point in time attribute assignments.
 * 
 * @author shilen
 * $Id$
 */
public class PITAttributeAssignFinder {

  /**
   * Find point in time attribute assignments by owner group and attribute def name.
   * No security checks done here.  Assuming retrieval of owner group and attribute def name were done securely.
   * @param ownerGroup
   * @param attributeDefName
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @return set of pit attribute assigns
   */
  public static Set<PITAttributeAssign> findByOwnerPITGroupAndPITAttributeDefName(PITGroup ownerGroup, PITAttributeDefName attributeDefName, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
    
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findByOwnerPITGroupIdAndPITAttributeDefNameId(ownerGroup.getId(), attributeDefName.getId());
    Set<PITAttributeAssign> assignmentsInRange = new LinkedHashSet<PITAttributeAssign>();

    for (PITAttributeAssign assignment : assignments) {
      if (pointInTimeFrom != null) {
        if (!assignment.isActive() && assignment.getEndTime().before(pointInTimeFrom)) {
          continue;
        }
      }
      
      if (pointInTimeTo != null) {
        if (assignment.getStartTime().after(pointInTimeTo)) {
          continue;
        }
      }
      
      assignmentsInRange.add(assignment);
    }
    
    return assignmentsInRange;
  }
}