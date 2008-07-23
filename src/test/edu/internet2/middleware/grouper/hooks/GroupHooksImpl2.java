/*
 * @author mchyzer
 * $Id: GroupHooksImpl2.java,v 1.4 2008-07-23 06:41:30 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;


/**
 * test implementation of group hooks for test
 */
public class GroupHooksImpl2 extends GroupHooks {

  /** most recent extension for testing */
  static String mostRecentPreInsertGroupExtension;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    
    Group group = preInsertBean.getGroup();
    String extension = (String)group.getAttributesDb().get(GrouperConfig.ATTR_EXTENSION);
    mostRecentPreInsertGroupExtension = extension;
    
  }

}
