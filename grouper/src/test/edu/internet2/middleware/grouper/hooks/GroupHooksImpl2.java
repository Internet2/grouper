/*
 * @author mchyzer
 * $Id: GroupHooksImpl2.java,v 1.1 2008-06-26 16:43:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean;


/**
 * test implementation of group hooks for test
 */
public class GroupHooksImpl2 extends GroupHooks {

  /** most recent extension for testing */
  static String mostRecentPreInsertGroupExtension;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupPreInsertBean preInsertBean) {
    
    Group group = preInsertBean.getGroup();
    String extension = (String)group.getAttributesDb().get(GrouperConfig.ATTR_EXTENSION);
    mostRecentPreInsertGroupExtension = extension;
    
  }

}
