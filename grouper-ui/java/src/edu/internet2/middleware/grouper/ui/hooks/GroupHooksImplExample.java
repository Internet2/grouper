/*
 * @author mchyzer
 * $Id: GroupHooksImplExample.java,v 1.1.2.1 2008-06-11 06:19:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean;
import edu.internet2.middleware.grouper.hooks.veto.HookVetoGroupInsert;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;


/**
 * test implementation of group hooks for test
 */
public class GroupHooksImplExample extends GroupHooks {

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean)
   */
  @Override
  public void groupPreInsert(HooksGroupPreInsertBean preInsertBean) {
    
    GroupDAO groupDAO = preInsertBean.getGroupDao();
    String name = StringUtils.defaultString((String)groupDAO.getAttributes().get(GrouperConfig.ATTR_NAME));
    if (!name.startsWith("penn:")) {
      throw new HookVetoGroupInsert("hook.veto.group.name.prefix", "group must be in the 'penn' top level folder");
    }
  }

}
