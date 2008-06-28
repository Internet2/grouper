/*
 * @author mchyzer
 * $Id: HooksGroupTypeBean.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.GroupType;


/**
 * bean to hold objects for GroupType low level hooks
 */
public class HooksGroupTypeBean extends HooksBean {
  
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

}
