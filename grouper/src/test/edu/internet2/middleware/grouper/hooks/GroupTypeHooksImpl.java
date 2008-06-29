/*
 * @author mchyzer
 * $Id: GroupTypeHooksImpl.java,v 1.1 2008-06-29 17:42:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of groupType hooks for test
 */
public class GroupTypeHooksImpl extends GroupTypeHooks {

  /** most recent extension for testing */
  static String mostRecentPreInsertGroupTypeName;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeHooks#groupTypePreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean)
   */
  @Override
  public void groupTypePreInsert(HooksContext hooksContext, HooksGroupTypeBean preInsertBean) {
    
    GroupType groupType = preInsertBean.getGroupType();
    String name = groupType.getName();
    mostRecentPreInsertGroupTypeName = name;
    if (StringUtils.equals("test2", name)) {
      throw new HookVeto("hook.veto.groupType.insert.name.not.test2", "name cannot be test2");
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeHooks#groupTypePreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean)
   */
  @Override
  public void groupTypePostInsert(HooksContext hooksContext, HooksGroupTypeBean postInsertBean) {
    
    GroupType groupType = postInsertBean.getGroupType();
    String name = groupType.getName();
    mostRecentPostInsertGroupTypeName = name;
    if (StringUtils.equals("test4", name)) {
      throw new HookVeto("hook.veto.groupType.insert.name.not.test4", "name cannot be test4");
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeHooks#groupTypePreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean)
   */
  @Override
  public void groupTypePreDelete(HooksContext hooksContext, HooksGroupTypeBean preDeleteBean) {
    
    GroupType groupType = preDeleteBean.getGroupType();
    String name = groupType.getName();
    mostRecentPreDeleteGroupTypeName = name;
    if (StringUtils.equals("test6", name)) {
      throw new HookVeto("hook.veto.groupType.delete.name.not.test6", "name cannot be test6");
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeHooks#groupTypePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean)
   */
  @Override
  public void groupTypePostDelete(HooksContext hooksContext, HooksGroupTypeBean preDeleteBean) {
    
    GroupType groupType = preDeleteBean.getGroupType();
    String name = groupType.getName();
    mostRecentPostDeleteGroupTypeName = name;
    if (StringUtils.equals("test8", name)) {
      throw new HookVeto("hook.veto.groupType.delete.name.not.test8", "name cannot be test8");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteGroupTypeName;

  /** most recent extension for testing */
  static String mostRecentPostUpdateGroupTypeName;

  /** most recent extension for testing */
  static String mostRecentPreDeleteGroupTypeName;

  /** most recent extension for testing */
  static String mostRecentPostInsertGroupTypeName;

  /** most recent extension for testing */
  static String mostRecentPreUpdateGroupTypeName;

}
