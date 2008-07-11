/*
 * @author mchyzer
 * $Id: HooksGroupTypeBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for GroupType low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksGroupTypeBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: groupType */
  public static final String FIELD_GROUP_TYPE = "groupType";

  /**
   * 
   */
  public HooksGroupTypeBean() {
    super();
  }

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_GROUP_TYPE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private GroupType groupType = null;
  
  /**
   * @param theGroupType
   */
  public HooksGroupTypeBean(GroupType theGroupType) {
    this.groupType = theGroupType;
  }
  
  /**
   * object being inserted
   * @return the GroupType
   */
  public GroupType getGroupType() {
    return this.groupType;
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksGroupTypeBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

}
