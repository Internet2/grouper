/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: CompositeHooksImpl.java,v 1.3 2008-07-21 04:43:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
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
   * @see edu.internet2.middleware.grouper.hooks.CompositeHooks#compositePostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean)
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

  /**
   * @see edu.internet2.middleware.grouper.hooks.CompositeHooks#compositePostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean)
   */
  @Override
  public void compositePostCommitInsert(HooksContext hooksContext,
      HooksCompositeBean postCommitInsertBean) {
  
    try {
      Composite composite = postCommitInsertBean.getComposite();
      String extension = composite.getLeftGroup().getExtension();
      mostRecentPostCommitInsertCompositeExtension = extension;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.CompositeHooks#compositePostCommitDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean)
   */
  @Override
  public void compositePostCommitDelete(HooksContext hooksContext,
      HooksCompositeBean postDeleteBean) {
  
    try {
      Composite composite = postDeleteBean.getComposite();
      String extension = composite.getLeftGroup().getExtension();
      mostRecentPostCommitDeleteCompositeExtension = extension;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    
  }

  /** most recent extension for testing */
  static String mostRecentPostInsertCompositeExtension;
  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitInsertCompositeExtension;
  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitDeleteCompositeExtension;

}
