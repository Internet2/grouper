/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.util;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.group.TypeOfGroup;


/**
 *
 */
public class LoadSomeGroups {

  /**
   * 
   */
  public LoadSomeGroups() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    for (int i=0;i<540;i++) {
      new GroupSave(grouperSession)
        .assignTypeOfGroup(TypeOfGroup.role)
        .assignCreateParentStemsIfNotExist(true)
        .assignName("someStem3:role_" + i).save();
    }
    
  }

}
