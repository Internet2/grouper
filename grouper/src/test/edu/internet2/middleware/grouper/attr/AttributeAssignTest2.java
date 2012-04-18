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
/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author mchyzer
 *
 */
public class AttributeAssignTest2 {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    try {

//      Group group = GroupFinder.findByUuid(grouperSession, "444cc54f9c3b4c8aa5bf639312211786", true);
//      
//      group.setDescription("whatever: " + GrouperUtil.uniqueId());
//      
//      group.store();
      
      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById("57b512cbe98648adbfa62244cd4164e0", true, false);
      
      //must be yyyy/mm/dd
      Timestamp enabledTimestamp = GrouperUtil.toTimestamp("2011/01/01");
      attributeAssign.setEnabledTime(enabledTimestamp);
      
      attributeAssign.saveOrUpdate();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

}
