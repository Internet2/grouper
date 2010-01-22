/*
 * @author mchyzer
 * $Id: HooksGroupTypeTupleBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for group low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksGroupTypeTupleBean extends HooksBean {
  
  /** object being affected */
  private GroupTypeTuple groupTypeTuple = null;
  
  /**
   * @param theGroupTypeTuple
   */
  public HooksGroupTypeTupleBean(GroupTypeTuple theGroupTypeTuple) {
    this.groupTypeTuple = theGroupTypeTuple;
  }
  
  /**
   * 
   */
  public HooksGroupTypeTupleBean() {
    super();
  }

  /**
   * object being inserted
   * @return the Group
   */
  public GroupTypeTuple getGroupTypeTuple() {
    return this.groupTypeTuple;
  }

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: groupTypeTuple */
  public static final String FIELD_GROUP_TYPE_TUPLE = "groupTypeTuple";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_GROUP_TYPE_TUPLE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksGroupTypeTupleBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
}
