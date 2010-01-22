/*
 * @author mchyzer
 * $Id: HooksLifecycleGrouperStartupBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * bean to hold objects for grouper startup hooks
 */
@GrouperIgnoreDbVersion
public class HooksLifecycleGrouperStartupBean extends HooksBean {
  
  /**
   */
  public HooksLifecycleGrouperStartupBean() {
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksLifecycleGrouperStartupBean clone() {
    return GrouperUtil.clone(this, null);
  }
}
