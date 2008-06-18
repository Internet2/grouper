/**
 * @author mchyzer
 * $Id: HooksMembershipPostAddMemberBean.java,v 1.1.2.1 2008-06-11 06:19:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import edu.internet2.middleware.grouper.internal.dto.MembershipDTO;


/**
 * post update bean for high level membership change (the main change, not
 * the side effects like adding the member to the groups where the group
 * to be added to is a member)
 */
public class HooksMembershipPostAddMemberBean extends HooksBean {

  /** object being inserted */
  private DefaultMemberOf defaultMemberOf = null;
  /**
   * composite if applicable 
   */
  private Composite composite;
  /**
   * field for membership 
   */
  private Field field;
  /**
   * group for membership 
   */
  private Group group;
  /**
   * member being assigned 
   */
  private MemberDTO memberDTO;
  /**
   * membership dto 
   */
  private MembershipDTO membershipDTO;
  /**
   * stem 
   */
  private Stem stem;
  
  /**
   * @param theHooksContext
   * @param theDefaultMemberOf 
   */
  public HooksMembershipPostAddMemberBean(HooksContext theHooksContext, 
      DefaultMemberOf theDefaultMemberOf) {
    super(theHooksContext);
    this.defaultMemberOf = theDefaultMemberOf;
    this.composite = theDefaultMemberOf.getComposite();
    this.field = theDefaultMemberOf.getField();
    this.group = theDefaultMemberOf.getGroup();
    this.memberDTO = theDefaultMemberOf.getMemberDTO();
    this.membershipDTO = theDefaultMemberOf.getMembershipDTO();
    this.stem = theDefaultMemberOf.getStem();
  }
  
  /**
   * @return the defaultMemberOf
   */
  public DefaultMemberOf getDefaultMemberOf() {
    return this.defaultMemberOf;
  }

  /**
   * composite if applicable
   * @return the composite
   */
  public Composite getComposite() {
    return this.composite;
  }

  /**
   * field for membership
   * @return the field
   */
  public Field getField() {
    return this.field;
  }

  /**
   * group for membership
   * @return the group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * member being assigned
   * @return the memberDTO
   */
  public MemberDTO getMemberDTO() {
    return this.memberDTO;
  }

  /**
   * membership dto
   * @return the membershipDTO
   */
  public MembershipDTO getMembershipDTO() {
    return this.membershipDTO;
  }

  /**
   * stem
   * @return the stem
   */
  public Stem getStem() {
    return this.stem;
  }
  
}
