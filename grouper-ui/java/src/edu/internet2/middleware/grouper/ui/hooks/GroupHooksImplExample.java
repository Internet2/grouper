/*
 * @author mchyzer
 * $Id: GroupHooksImplExample.java,v 1.1.2.3 2008-06-18 09:22:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.HookVeto;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;


/**
 * test implementation of group hooks for test
 */
public class GroupHooksImplExample extends GroupHooks {

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean)
   */
  @Override
  public void groupPreInsert(HooksGroupPreInsertBean preInsertBean) {
    
    GroupDTO groupDTO = preInsertBean.getGroupDto();
    String name = StringUtils.defaultString((String)groupDTO.getAttributes().get(GrouperConfig.ATTR_NAME));
    if (!name.startsWith("penn:")) {
      throw new HookVeto("hook.veto.group.name.prefix", "group must be in the 'penn' top level folder");
    }
  }

}
