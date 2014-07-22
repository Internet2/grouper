/**
 * Copyright 2012 Internet2
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
 * $Id: StemHooksImpl.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksStemBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of stem hooks for test
 */
public class StemHooksImpl extends StemHooks {

  /** most recent extension for testing */
  static String mostRecentPreInsertStemExtension;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPreInsert(HooksContext hooksContext, HooksStemBean preInsertBean) {
    
    Stem stem = preInsertBean.getStem();
    String extension = (String)stem.getExtension();
    mostRecentPreInsertStemExtension = extension;
    if (StringUtils.equals("test2", extension)) {
      throw new HookVeto("hook.veto.stem.insert.name.not.test2", "name cannot be test2");
    }
    
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteStemExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPostDelete(HooksContext hooksContext,
      HooksStemBean postDeleteBean) {
    Stem stem = postDeleteBean.getStem();
    String extension = (String)stem.getExtension();
    mostRecentPostDeleteStemExtension = extension;
    if (StringUtils.equals("test3", extension)) {
      throw new HookVeto("hook.veto.stem.delete.name.not.test3", "name cannot be test3");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPostInsert(HooksContext hooksContext,
      HooksStemBean postInsertBean) {

    Stem stem = postInsertBean.getStem();
    String extension = (String)stem.getExtension();
    mostRecentPostInsertStemExtension = extension;
    if (StringUtils.equals("test8", extension)) {
      throw new HookVeto("hook.veto.stem.insert.name.not.test8", "name cannot be test8");
    }

  }

  /** most recent extension for testing */
  static String mostRecentPostUpdateStemExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPostUpdate(HooksContext hooksContext,
      HooksStemBean postUpdateBean) {
    Stem stem = postUpdateBean.getStem();
    String extension = (String)stem.getExtension();
    mostRecentPostUpdateStemExtension = extension;
    if (StringUtils.equals("test12", extension)) {
      throw new HookVeto("hook.veto.stem.update.name.not.test12", "name cannot be test12");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPreDeleteStemExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPreDelete(HooksContext hooksContext,
      HooksStemBean preDeleteBean) {
    Stem stem = preDeleteBean.getStem();
    String extension = (String)stem.getExtension();
    mostRecentPreDeleteStemExtension = extension;
    if (StringUtils.equals("test6", extension)) {
      throw new HookVeto("hook.veto.stem.delete.name.not.test6", "name cannot be test6");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostInsertStemExtension;

  /** most recent extension for testing */
  static String mostRecentPreUpdateStemExtension;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitDeleteStemExtension;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitInsertStemExtension;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitUpdateStemExtension;

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPreUpdate(HooksContext hooksContext,
      HooksStemBean preUpdateBean) {
    Stem stem = preUpdateBean.getStem();
    String extension = (String)stem.getExtension();
    mostRecentPreUpdateStemExtension = extension;
    if (StringUtils.equals("test10", extension)) {
      throw new HookVeto("hook.veto.stem.update.name.not.test10", "name cannot be test10");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPostCommitDelete(HooksContext hooksContext,
      HooksStemBean postDeleteBean) {
    Stem stem = postDeleteBean.getStem();
    String extension = (String)stem.getExtension();
    mostRecentPostCommitDeleteStemExtension = extension;
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPostCommitInsert(HooksContext hooksContext,
      HooksStemBean postInsertBean) {
  
    Stem stem = postInsertBean.getStem();
    String extension = (String)stem.getExtension();
    mostRecentPostCommitInsertStemExtension = extension;
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPostCommitUpdate(HooksContext hooksContext,
      HooksStemBean postUpdateBean) {
    Stem stem = postUpdateBean.getStem();
    String extension = (String)stem.getExtension();
    mostRecentPostCommitUpdateStemExtension = extension;
  }

}
