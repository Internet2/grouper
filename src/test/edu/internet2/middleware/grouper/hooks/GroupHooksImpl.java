/*
 * @author mchyzer
 * $Id: GroupHooksImpl.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;


/**
 * test implementation of group hooks for test
 */
public class GroupHooksImpl extends GroupHooks {

  /** most recent extension for testing */
  private static String mostRecentInsertGroupExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksGroupPreInsertBean)
   */
  @Override
  public void groupPreInsert(HooksGroupPreInsertBean preInsertBean) {
    
    GroupDTO groupDTO = preInsertBean.getGroupDto();
    String extension = (String)groupDTO.getAttributes().get(GrouperConfig.ATTR_EXTENSION);
    mostRecentInsertGroupExtension = extension;
    if (StringUtils.equals("test2", extension)) {
      throw new HookVeto("hook.veto.group.insert.name.not.test2", "name cannot be test2");
    }
    
  }

  /**
   * @return the mostRecentExtension
   */
  public static String getMostRecentInsertGroupExtension() {
    return mostRecentInsertGroupExtension;
  }
  
}
