/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import java.util.List;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class PocLoadRule {

  /**
   * 
   */
  public PocLoadRule() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
//    GrouperSession grouperSession = GrouperSession.startRootSession();
//    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
//      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
//      .assignAttributeDefType(AttributeDefType.perm)
//      .save();
//    
//    permissionDef.setAssignToEffMembership(true);
//    permissionDef.setAssignToGroup(true);
//    permissionDef.store();
//    
//    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
//
//    //make a role
//    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
//    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
//
//    //assign a user to a role
//    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
//    payrollUser.addMember(subject0, false);
//    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
//    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
//    payrollGuest.addMember(subject1, false);
//    
//    //create a permission, assign to role
//    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
//    
//    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
//    
//    //assign the permission to another user directly, not due to a role
//    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
//        
//    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee);

    int count = HibernateSession.bySqlStatic()
        .select(int.class, "SELECT count(*) FROM grouper_rules_v WHERE rule_check_type LIKE 'membershipRemove%'");

    
    List<String> ids = HibernateSession.bySqlStatic()
      .listSelect(String.class, "SELECT attribute_assign_id FROM grouper_rules_v WHERE rule_check_type LIKE 'membershipRemove%'", null);
    
    System.out.println(GrouperUtil.toStringForLog(ids));
    
  }

}
