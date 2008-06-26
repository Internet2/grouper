/*
 * @author mchyzer
 * $Id: GroupHooksImplExample.java,v 1.4 2008-06-26 11:16:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class GroupHooksImplExample extends GroupHooks {

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupPreInsertBean preInsertBean) {
    
    Group group = preInsertBean.getGroup();
    String name = StringUtils.defaultString(group.getName());
    if (!name.startsWith("penn:")) {
      throw new HookVeto("hook.veto.group.name.prefix", "group must be in the 'penn' top level folder");
    }
  }

}
