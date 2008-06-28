/*
 * @author mchyzer
 * $Id: GroupHooksImpl.java,v 1.6 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class GroupHooksImpl extends GroupHooks {

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
    if (StringUtils.equals("test2", extension)) {
      throw new HookVeto("hook.veto.group.insert.name.not.test2", "name cannot be test2");
    }
    
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteGroupExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostDelete(HooksContext hooksContext,
      HooksGroupBean postDeleteBean) {
    Group group = postDeleteBean.getGroup();
    String extension = (String)group.getAttributesDb().get(GrouperConfig.ATTR_EXTENSION);
    mostRecentPostDeleteGroupExtension = extension;
    if (StringUtils.equals("test3", extension)) {
      throw new HookVeto("hook.veto.group.delete.name.not.test3", "name cannot be test3");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostInsert(HooksContext hooksContext,
      HooksGroupBean postInsertBean) {

    Group group = postInsertBean.getGroup();
    String extension = (String)group.getAttributesDb().get(GrouperConfig.ATTR_EXTENSION);
    mostRecentPostInsertGroupExtension = extension;
    if (StringUtils.equals("test8", extension)) {
      throw new HookVeto("hook.veto.group.insert.name.not.test8", "name cannot be test8");
    }

  }

  /** most recent extension for testing */
  static String mostRecentPostUpdateGroupExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostUpdate(HooksContext hooksContext,
      HooksGroupBean postUpdateBean) {
    Group group = postUpdateBean.getGroup();
    String extension = (String)group.getAttributesDb().get(GrouperConfig.ATTR_EXTENSION);
    mostRecentPostUpdateGroupExtension = extension;
    if (StringUtils.equals("test12", extension)) {
      throw new HookVeto("hook.veto.group.update.name.not.test12", "name cannot be test12");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPreDeleteGroupExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreDelete(HooksContext hooksContext,
      HooksGroupBean preDeleteBean) {
    Group group = preDeleteBean.getGroup();
    String extension = (String)group.getAttributesDb().get(GrouperConfig.ATTR_EXTENSION);
    mostRecentPreDeleteGroupExtension = extension;
    if (StringUtils.equals("test6", extension)) {
      throw new HookVeto("hook.veto.group.delete.name.not.test6", "name cannot be test6");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostInsertGroupExtension;

  /** most recent extension for testing */
  static String mostRecentPreUpdateGroupExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(HooksContext hooksContext,
      HooksGroupBean preUpdateBean) {
    Group group = preUpdateBean.getGroup();
    String extension = (String)group.getAttributesDb().get(GrouperConfig.ATTR_EXTENSION);
    mostRecentPreUpdateGroupExtension = extension;
    if (StringUtils.equals("test10", extension)) {
      throw new HookVeto("hook.veto.group.update.name.not.test10", "name cannot be test10");
    }
  }

}
