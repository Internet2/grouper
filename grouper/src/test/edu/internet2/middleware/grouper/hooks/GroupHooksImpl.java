/*
 * @author mchyzer
 * $Id: GroupHooksImpl.java,v 1.1.2.1 2008-06-09 05:52:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean;
import edu.internet2.middleware.grouper.hooks.veto.HookVetoGroupInsert;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;


/**
 * test implementation of group hooks for test
 */
public class GroupHooksImpl extends GroupHooks {

  /** most recent extension for testing */
  private static String mostRecentInsertExtension;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean)
   */
  @Override
  public void groupPreInsert(HooksGroupPreInsertBean preInsertBean) {
    
    GroupDAO groupDAO = preInsertBean.getGroupDao();
    String extension = (String)groupDAO.getAttributes().get(GrouperConfig.ATTR_EXTENSION);
    mostRecentInsertExtension = extension;
    if (StringUtils.equals("test2", extension)) {
      throw new HookVetoGroupInsert("hook.veto.group.insert.name.not.test2", "name cannot be test2");
    }
    
  }

  /**
   * @return the mostRecentExtension
   */
  public static String getMostRecentInsertExtension() {
    return mostRecentInsertExtension;
  }
  
}
