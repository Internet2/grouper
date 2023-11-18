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
 * $Id: GroupHooksImpl.java,v 1.10 2009-01-02 06:57:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class GroupHooksImpl extends GroupHooks {

  /** most recent extension for testing */
  static String mostRecentPostCommitInsertGroupExtension;

  /** keep reference to the group to make sure it is different */
  static Group mostRecentPostCommitInsertGroup;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostCommitInsert(HooksContext hooksContext,
      HooksGroupBean postInsertCommitBean) {
    
    Group group = postInsertCommitBean.getGroup();
    String extension = group.getExtension();
    mostRecentPostCommitInsertGroupExtension = extension;
    mostRecentPostCommitInsertGroup = group;
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitUpdateGroupExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostCommitUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostCommitUpdate(HooksContext hooksContext,
      HooksGroupBean postUpdateCommitBean) {
    
    Group group = postUpdateCommitBean.getGroup();
    String extension = group.getExtension();
    mostRecentPostCommitUpdateGroupExtension = extension;
    
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitDeleteGroupExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostCommitDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostCommitDelete(HooksContext hooksContext,
      HooksGroupBean postDeleteCommitBean) {
    
    Group group = postDeleteCommitBean.getGroup();
    String extension = group.getExtension();
    mostRecentPostCommitDeleteGroupExtension = extension;
    
  }

  /** most recent extension for testing */
  static String mostRecentPreInsertGroupExtension;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    
    Group group = preInsertBean.getGroup();
    String extension = group.getExtension();
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
    String extension = group.getExtension();
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
    String extension = group.getExtension();
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
    String extension = group.getExtension();
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
    String extension = group.getExtension();
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
    String extension = group.getExtension();
    mostRecentPreUpdateGroupExtension = extension;
    if (StringUtils.equals("test10", extension)) {
      throw new HookVeto("hook.veto.group.update.name.not.test10", "name cannot be test10");
    }
  }

}
