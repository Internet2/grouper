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
package edu.internet2.middleware.grouper.pit.finder;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;

/**
 * Find point in time attribute assign values.
 * 
 * @author shilen
 * $Id$
 */
public class PITAttributeAssignValueFinder {

  /**
   * Find point in time attribute assign values by attribute assign and date ranges.
   * This assumes if you have access to the attribute assign, then you have access to the values too.  
   * So there are no security checks here.
   * @param attributeAssign
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @return set of pit attribute assign values
   */
  public static Set<PITAttributeAssignValue> findByPITAttributeAssign(PITAttributeAssign attributeAssign, Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
    
    Set<PITAttributeAssignValue> values = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findByPITAttributeAssignId(attributeAssign.getId(), null);
    Set<PITAttributeAssignValue> valuesInRange = new LinkedHashSet<PITAttributeAssignValue>();

    for (PITAttributeAssignValue value : values) {
      if (pointInTimeFrom != null) {
        if (!value.isActive() && value.getEndTime().before(pointInTimeFrom)) {
          continue;
        }
      }
      
      if (pointInTimeTo != null) {
        if (value.getStartTime().after(pointInTimeTo)) {
          continue;
        }
      }
      
      valuesInRange.add(value);
    }
    
    return valuesInRange;
  }
}
