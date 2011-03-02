/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;


/**
 *
 */
public class CacheTry {

  /**
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    
    for (int i=0;i<20;i++) {
      SubjectFinder.findById("10021368", true);
    }
    
    for (int i=0;i<20;i++) {
      SubjectFinder.findByIdOrIdentifier("mchyzer", true);
    }
    
    for (int i=0;i<20;i++) {
      GrouperDAOFactory.getFactory().getAttributeAssign().findById("dbfacf21faad4c94b4388b1e8ff54fda", false);
    }
    
  }

}
