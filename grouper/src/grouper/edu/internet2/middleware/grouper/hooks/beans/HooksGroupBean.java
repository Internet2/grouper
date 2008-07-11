/*
 * @author mchyzer
 * $Id: HooksGroupBean.java,v 1.3 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for group low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksGroupBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: group */
  public static final String FIELD_GROUP = "group";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_GROUP);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private Group group = null;
  
  /**
   * 
   */
  public HooksGroupBean() {
    super();
  }

  /**
   * @param theGroup
   */
  public HooksGroupBean(Group theGroup) {
    this.group = theGroup;
  }
  
  /**
   * object being inserted
   * @return the Group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksGroupBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

}
