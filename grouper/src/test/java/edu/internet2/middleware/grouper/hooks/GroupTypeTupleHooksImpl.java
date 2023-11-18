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
 * $Id: GroupTypeTupleHooksImpl.java,v 1.4 2009-03-15 06:37:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of groupType hooks for test
 */
public class GroupTypeTupleHooksImpl extends GroupTypeTupleHooks {

  /** most recent extension for testing */
  static String mostRecentPreInsertGroupTypeTupleName;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks#groupTypeTuplePreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean)
   */
  @Override
  public void groupTypeTuplePreInsert(HooksContext hooksContext, HooksGroupTypeTupleBean preInsertBean) {
    
    GroupTypeTuple groupTypeTuple = preInsertBean.getGroupTypeTuple();
    String name = null;
    try {
      name = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(),groupTypeTuple.getGroupUuid(), true).getExtension();
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    mostRecentPreInsertGroupTypeTupleName = name;
    if (StringUtils.equals("test2", name)) {
      throw new HookVeto("hook.veto.groupTypeTuple.insert.name.not.test2", "name cannot be test2");
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks#groupTypeTuplePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean)
   */
  @Override
  public void groupTypeTuplePostInsert(HooksContext hooksContext, HooksGroupTypeTupleBean postInsertBean) {
    
    GroupTypeTuple groupTypeTuple = postInsertBean.getGroupTypeTuple();
    String name = null;
    try {
      name = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(),groupTypeTuple.getGroupUuid(), true).getExtension();
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    mostRecentPostInsertGroupTypeTupleName = name;
    if (StringUtils.equals("test4", name)) {
      throw new HookVeto("hook.veto.groupTypeTuple.insert.name.not.test4", "name cannot be test4");
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks#groupTypeTuplePreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean)
   */
  @Override
  public void groupTypeTuplePreDelete(HooksContext hooksContext, HooksGroupTypeTupleBean preDeleteBean) {
    
    GroupTypeTuple groupTypeTuple = preDeleteBean.getGroupTypeTuple();
    String name = null;
    try {
      name = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(),groupTypeTuple.getGroupUuid(), true).getExtension();
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    mostRecentPreDeleteGroupTypeTupleName = name;
    if (StringUtils.equals("test6", name)) {
      throw new HookVeto("hook.veto.groupTypeTuple.delete.name.not.test6", "name cannot be test6");
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks#groupTypeTuplePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean)
   */
  @Override
  public void groupTypeTuplePostDelete(HooksContext hooksContext, HooksGroupTypeTupleBean preDeleteBean) {
    
    GroupTypeTuple groupTypeTuple = preDeleteBean.getGroupTypeTuple();
    String name = null;
    try {
      name = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(),groupTypeTuple.getGroupUuid(), true).getExtension();
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    mostRecentPostDeleteGroupTypeTupleName = name;
    if (StringUtils.equals("test8", name)) {
      throw new HookVeto("hook.veto.groupTypeTuple.delete.name.not.test8", "name cannot be test8");
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks#groupTypeTuplePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean)
   */
  @Override
  public void groupTypeTuplePostCommitDelete(HooksContext hooksContext, HooksGroupTypeTupleBean preDeleteBean) {
    
    GroupTypeTuple groupTypeTuple = preDeleteBean.getGroupTypeTuple();
    String name = null;
    try {
      name = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(),groupTypeTuple.getGroupUuid(), true).getExtension();
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    mostRecentPostCommitDeleteGroupTypeTupleName = name;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks#groupTypeTuplePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean)
   */
  @Override
  public void groupTypeTuplePostCommitInsert(HooksContext hooksContext, HooksGroupTypeTupleBean postInsertBean) {
    
    GroupTypeTuple groupTypeTuple = postInsertBean.getGroupTypeTuple();
    String name = null;
    try {
      name = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(),groupTypeTuple.getGroupUuid(), true).getExtension();
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException(gnfe);
    }
    mostRecentPostCommitInsertGroupTypeTupleName = name;
    
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteGroupTypeTupleName;

  /** most recent extension for testing */
  static String mostRecentPreDeleteGroupTypeTupleName;

  /** most recent extension for testing */
  static String mostRecentPostInsertGroupTypeTupleName;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitDeleteGroupTypeTupleName;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitInsertGroupTypeTupleName;

}
