/**
 * @author mchyzer
 * $Id: AttrAssignDelegatableDelegate.java,v 1.1 2009-10-10 18:02:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * delegate attribute calls from attr assignments when delegating
 */
public class AttrAssignDelegatableDelegate {

  /**
   * reference to the attribute assign in question
   */
  private AttributeAssign attributeAssign = null;
  
  /**
   * make sure member is a member of the group
   */
  private void assertDelegatable() {
    throw new RuntimeException("Not implemented");
  }
  
  /**
   * 
   * @param attributeAssign1
   */
  public AttrAssignDelegatableDelegate(AttributeAssign attributeAssign1) {
    this.attributeAssign = attributeAssign1;
  }

//    return GrouperDAOFactory.getFactory()
//      .getAttributeAssign().findAttributeDefNamesByGroupIdMemberIdAndAttributeDefId(this.group.getUuid(), this.member.getUuid(), attributeDefId);

}
