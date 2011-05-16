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
