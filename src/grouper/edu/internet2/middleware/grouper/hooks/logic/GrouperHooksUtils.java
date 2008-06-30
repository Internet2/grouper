/*
 * @author mchyzer
 * $Id: GrouperHooksUtils.java,v 1.5 2008-06-30 04:01:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.util.List;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hooks.beans.HooksBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperHooksUtils {

  /**
   * 
   * @param object that the hook is about
   * @param grouperHookType e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObject are the intances to pass to bean constructor.  e.g. group
   * @param businessClass are the types passed to bean constructor.  e.g. Group.class
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @param resetDbVersion if the db version should be saved, assigned, reset, etc (for low level hooks)
   * @param clearDbVersion if the db version should be cleared (e.g. on delete) (for low level hooks)
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void callHooksIfRegistered(Object object, GrouperHookType grouperHookType, String hookMethodName,
      Class<? extends HooksBean> hooksBeanClass, Object businessObject, Class businessClass,
      VetoType vetoType, boolean resetDbVersion, boolean clearDbVersion) throws HookVeto {
    callHooksIfRegistered(object, grouperHookType, hookMethodName, hooksBeanClass, new Object[]{businessObject},
        new Class[]{businessClass}, vetoType, resetDbVersion, clearDbVersion);
  }
  
  /**
   * 
   * @param grouperHookType e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObjects are the intances to pass to bean constructor.  e.g. group
   * @param businessClasses are the types passed to bean constructor.  e.g. Group.class
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void callHooksIfRegistered(GrouperHookType grouperHookType, String hookMethodName,
      Class<? extends HooksBean> hooksBeanClass, Object[] businessObjects, Class[] businessClasses,
      VetoType vetoType) throws HookVeto {
    
    callHooksIfRegistered(null, grouperHookType, hookMethodName, 
        hooksBeanClass, businessObjects, businessClasses, vetoType, false, false);

  }

  /**
   * 
   * @param grouperHookType e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObject are the intances to pass to bean constructor.  e.g. group
   * @param businessClass are the types passed to bean constructor.  e.g. Group.class
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void callHooksIfRegistered(GrouperHookType grouperHookType, String hookMethodName,
      Class<? extends HooksBean> hooksBeanClass, Object businessObject, Class businessClass,
      VetoType vetoType) throws HookVeto {
    callHooksIfRegistered(null, grouperHookType, hookMethodName, hooksBeanClass, new Object[]{businessObject},
        new Class[]{businessClass}, vetoType, false, false);
  }

  /**
   * 
   * @param object that the hook is about
   * @param grouperHookType e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObjects are the intances to pass to bean constructor.  e.g. group
   * @param businessClasses are the types passed to bean constructor.  e.g. Group.class
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @param resetDbVersion if the db version should be saved, assigned, reset, etc (for low level hooks)
   * @param clearDbVersion if the db version should be cleared (e.g. on delete) (for low level hooks)
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void callHooksIfRegistered(Object object, GrouperHookType grouperHookType, String hookMethodName,
      Class<? extends HooksBean> hooksBeanClass, Object[] businessObjects, Class[] businessClasses,
      VetoType vetoType, boolean resetDbVersion, boolean clearDbVersion) throws HookVeto {
    
    Object dbVersion = null;
    Object objectVersion = null;
    GrouperAPI grouperAPI = object instanceof GrouperAPI ? (GrouperAPI)object : null;
    if (resetDbVersion) {
      //current state in db
      dbVersion = grouperAPI.dbVersion();
      
      //take a snapshot of current object
      grouperAPI.dbVersionReset();
      objectVersion = grouperAPI.dbVersion();
      
      //put the dbVersion back temporarily
      GrouperUtil.assignField(grouperAPI, GrouperAPI.FIELD_DB_VERSION, dbVersion);
     
    }
    
    //see if there is a hook class
    List<Object> hooks = grouperHookType.hooksInstances();
    
    if (hooks != null && hooks.size() > 0) {
      
      //loop through each hook
      for (Object hook : hooks) {
        //instantiate bean
        HooksBean hooksBean = GrouperUtil.construct(hooksBeanClass, businessClasses, businessObjects);
        try {
          
          HooksContext hooksContext = new HooksContext();
          
          //groupHooks.groupPreInsert(hooksContext, hooksGroupPreInsertBean);
          GrouperUtil.callMethod(hook.getClass(), hook, hookMethodName, new Class[]{HooksContext.class, hooksBeanClass},
              new Object[]{hooksContext, hooksBean});
        
        } catch (HookVeto hv) {
          hv.assignVetoType(vetoType, false);
          throw hv;
        }
      }
      
    }
    //if not vetoed, put the object version as db version
    if (resetDbVersion) {
      GrouperUtil.assignField(grouperAPI, GrouperAPI.FIELD_DB_VERSION, objectVersion);
    }
  
    if (clearDbVersion) {
      grouperAPI.dbVersionClear();
    }
  
  }

}
