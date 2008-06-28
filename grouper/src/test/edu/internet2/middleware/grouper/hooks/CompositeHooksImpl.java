/*
 * @author mchyzer
 * $Id: CompositeHooksImpl.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of composite hooks for test
 */
public class CompositeHooksImpl extends CompositeHooks {

  /** most recent extension for testing */
  static String mostRecentPreInsertCompositeExtension;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.CompositeHooks#compositePreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean)
   */
  @Override
  public void compositePreInsert(HooksContext hooksContext, HooksCompositeBean preInsertBean) {
    
    try {
      Composite composite = preInsertBean.getComposite();
      String extension = composite.getLeftGroup().getExtension();
      mostRecentPreInsertCompositeExtension = extension;
      if (StringUtils.equals("test5", extension)) {
        throw new HookVeto("hook.veto.composite.insert.extension.not.test5", "extension cannot be test5");
      }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteCompositeExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.CompositeHooks#compositePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean)
   */
  @Override
  public void compositePostDelete(HooksContext hooksContext,
      HooksCompositeBean postDeleteBean) {

    try {
      Composite composite = postDeleteBean.getComposite();
      String extension = composite.getLeftGroup().getExtension();
      mostRecentPostDeleteCompositeExtension = extension;
      if (StringUtils.equals("test10", extension)) {
        throw new HookVeto("hook.veto.composite.delete.extension.not.test10", "extension cannot be test10");
      }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.CompositeHooks#compositePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean)
   */
  @Override
  public void compositePostInsert(HooksContext hooksContext,
      HooksCompositeBean postInsertBean) {

    try {
      Composite composite = postInsertBean.getComposite();
      String extension = composite.getLeftGroup().getExtension();
      mostRecentPostInsertCompositeExtension = extension;
      if (StringUtils.equals("test11", extension)) {
        throw new HookVeto("hook.veto.composite.insert.extension.not.test11", "extension cannot be test11");
      }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }

  }

  /** most recent extension for testing */
  static String mostRecentPostUpdateCompositeExtension;

  /** most recent extension for testing */
  static String mostRecentPreDeleteCompositeExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.CompositeHooks#compositePreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean)
   */
  @Override
  public void compositePreDelete(HooksContext hooksContext,
      HooksCompositeBean preDeleteBean) {

    try {
      Composite composite = preDeleteBean.getComposite();
      String extension = composite.getLeftGroup().getExtension();
      mostRecentPreDeleteCompositeExtension = extension;
      if (StringUtils.equals("test4", extension)) {
        throw new HookVeto("hook.veto.composite.delete.extension.not.test4", "extension cannot be test4");
      }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    
  }

  /** most recent extension for testing */
  static String mostRecentPostInsertCompositeExtension;

  /** most recent extension for testing */
  static String mostRecentPreUpdateCompositeExtension;

}
