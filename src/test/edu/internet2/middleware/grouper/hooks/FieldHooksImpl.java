/*
 * @author mchyzer
 * $Id: FieldHooksImpl.java,v 1.1 2008-06-29 17:42:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksFieldBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of field hooks for test
 */
public class FieldHooksImpl extends FieldHooks {

  /** most recent extension for testing */
  static String mostRecentPreInsertFieldName;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.FieldHooks#fieldPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksFieldBean)
   */
  @Override
  public void fieldPreInsert(HooksContext hooksContext, HooksFieldBean preInsertBean) {
    
    Field field = preInsertBean.getField();
    String name = field.getName();
    mostRecentPreInsertFieldName = name;
    if (StringUtils.equals("test2", name)) {
      throw new HookVeto("hook.veto.field.insert.name.not.test2", "name cannot be test2");
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.FieldHooks#fieldPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksFieldBean)
   */
  @Override
  public void fieldPostInsert(HooksContext hooksContext, HooksFieldBean postInsertBean) {
    
    Field field = postInsertBean.getField();
    String name = field.getName();
    mostRecentPostInsertFieldName = name;
    if (StringUtils.equals("test4", name)) {
      throw new HookVeto("hook.veto.field.insert.name.not.test4", "name cannot be test4");
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.FieldHooks#fieldPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksFieldBean)
   */
  @Override
  public void fieldPreDelete(HooksContext hooksContext, HooksFieldBean preDeleteBean) {
    
    Field field = preDeleteBean.getField();
    String name = field.getName();
    mostRecentPreDeleteFieldName = name;
    if (StringUtils.equals("test6", name)) {
      throw new HookVeto("hook.veto.field.delete.name.not.test6", "name cannot be test6");
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.FieldHooks#fieldPostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksFieldBean)
   */
  @Override
  public void fieldPostDelete(HooksContext hooksContext, HooksFieldBean preDeleteBean) {
    
    Field field = preDeleteBean.getField();
    String name = field.getName();
    mostRecentPostDeleteFieldName = name;
    if (StringUtils.equals("test8", name)) {
      throw new HookVeto("hook.veto.field.delete.name.not.test8", "name cannot be test8");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteFieldName;

  /** most recent extension for testing */
  static String mostRecentPostUpdateFieldName;

  /** most recent extension for testing */
  static String mostRecentPreDeleteFieldName;

  /** most recent extension for testing */
  static String mostRecentPostInsertFieldName;

  /** most recent extension for testing */
  static String mostRecentPreUpdateFieldName;

}
