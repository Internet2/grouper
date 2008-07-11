/*
 * @author mchyzer
 * $Id: HooksLifecycleHooksInitBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * bean to hold objects for when hooks init (register your hooks here)
 */
@GrouperIgnoreDbVersion
public class HooksLifecycleHooksInitBean extends HooksBean {
  
  /**
   */
  public HooksLifecycleHooksInitBean() {
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksLifecycleHooksInitBean clone() {
    return GrouperUtil.clone(this, null);
  }
}
