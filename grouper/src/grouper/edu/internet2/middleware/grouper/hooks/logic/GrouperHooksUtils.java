/*
 * @author mchyzer
 * $Id: GrouperHooksUtils.java,v 1.9 2008-07-10 05:55:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleGrouperStartupBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHooksInitBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * utils for grouper hooks
 */
public class GrouperHooksUtils {

  /**
   * switch to know if startup hooks have been called yet
   */
  private static boolean grouperStartupHooksCalled = false;

  /**
   * kick off startup hooks if not done already
   */
  public synchronized static void fireGrouperStartupHooksIfNotFiredAlready() {
    if (!grouperStartupHooksCalled) {
      //even if errors, only call once
      grouperStartupHooksCalled = true;
      
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LIFECYCLE, 
          LifecycleHooks.METHOD_GROUPER_STARTUP, HooksLifecycleGrouperStartupBean.class, 
          (Object)null, null, null);
      
    }
  }
  
  /**
   * switch to know if hooks init hooks have been called yet
   */
  private static boolean hooksInitHooksCalled = false;

  /**
   * kick off hooks init hooks if not done already
   */
  synchronized static void fireHooksInitHooksIfNotFiredAlready() {
    if (!hooksInitHooksCalled) {
      //even if errors, only call once
      hooksInitHooksCalled = true;
      
      //see if we should register test hook:
      try {
        Class testLifecycle = Class.forName("edu.internet2.middleware.grouper.hooks.LifecycleHooksImpl");
        addHookManual("hooks.lifecycle.class", testLifecycle);
      } catch (ClassNotFoundException cnfe) {
        //just ignore, probably not running unit tests
      }
      
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LIFECYCLE, 
          LifecycleHooks.METHOD_HOOKS_INIT, HooksLifecycleHooksInitBean.class, 
          (Object)null, null, null);
      
    }
  }
  
  /**
   * add a hook to the list of configured hooks for this type
   * note if the class already exists it will not be added again
   * @param propertyFileKey
   * @param hooksClass
   */
  public static void addHookManual(String propertyFileKey, Class<?> hooksClass) {
    GrouperHookType.addHookManual(propertyFileKey, hooksClass);
  }

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperHooksUtils.class);

  /**
   * 
   * @param object that the hook is about
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObject are the intances to pass to bean constructor.  e.g. group
   * @param businessClass are the types passed to bean constructor.  e.g. Group.class
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @param resetDbVersion if the db version should be saved, assigned, reset, etc (for low level hooks)
   * @param clearDbVersion if the db version should be cleared (e.g. on delete) (for low level hooks)
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void callHooksIfRegistered(Object object, GrouperHookTypeInterface grouperHookTypeInterface, String hookMethodName,
      Class<? extends HooksBean> hooksBeanClass, Object businessObject, Class businessClass,
      VetoType vetoType, boolean resetDbVersion, boolean clearDbVersion) throws HookVeto {
    callHooksIfRegistered(object, grouperHookTypeInterface, hookMethodName, hooksBeanClass, 
        businessClass == null ? null : new Object[]{businessObject},
        businessClass == null ? null : new Class[]{businessClass}, vetoType, resetDbVersion, clearDbVersion);
  }
  
  /**
   * 
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObjects are the intances to pass to bean constructor.  e.g. group
   * @param businessClasses are the types passed to bean constructor.  e.g. Group.class
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void callHooksIfRegistered(GrouperHookTypeInterface grouperHookTypeInterface, String hookMethodName,
      Class<? extends HooksBean> hooksBeanClass, Object[] businessObjects, Class[] businessClasses,
      VetoType vetoType) throws HookVeto {
    
    callHooksIfRegistered(null, grouperHookTypeInterface, hookMethodName, 
        hooksBeanClass, businessObjects, businessClasses, vetoType, false, false);

  }

  /**
   * 
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObject are the intances to pass to bean constructor.  e.g. group
   * @param businessClass are the types passed to bean constructor.  e.g. Group.class
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void callHooksIfRegistered(GrouperHookTypeInterface grouperHookTypeInterface, String hookMethodName,
      Class<? extends HooksBean> hooksBeanClass, Object businessObject, Class businessClass,
      VetoType vetoType) throws HookVeto {
    callHooksIfRegistered(null, grouperHookTypeInterface, hookMethodName, hooksBeanClass, 
        businessClass == null ? null : new Object[]{businessObject},
            businessClass == null ? null : new Class[]{businessClass}, vetoType, false, false);
  }

  /**
   * 
   * @param object that the hook is about
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObjects are the intances to pass to bean constructor.  e.g. group
   * @param businessClasses are the types passed to bean constructor.  e.g. Group.class
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @param resetDbVersion if the db version should be saved, assigned, reset, etc (for low level hooks)
   * @param clearDbVersion if the db version should be cleared (e.g. on delete) (for low level hooks)
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void callHooksIfRegistered(final Object object, final GrouperHookTypeInterface grouperHookTypeInterface, final String hookMethodName,
      final Class<? extends HooksBean> hooksBeanClass, Object[] businessObjects, Class[] businessClasses,
      final VetoType vetoType, boolean resetDbVersion, boolean clearDbVersion) throws HookVeto {
    
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
    List<GrouperHookMethodAndObject> hooks = GrouperHookType.hooksInstances(grouperHookTypeInterface, hookMethodName, hooksBeanClass);
    
    if (hooks != null && hooks.size() > 0) {
      
      //loop through each hook
      for (final GrouperHookMethodAndObject hookMethodAndObject : hooks) {
        //instantiate bean
        HooksBean hooksBean = GrouperUtil.construct(hooksBeanClass, businessClasses, businessObjects);

        HooksContext hooksContext = new HooksContext();
        
        final Object hook = hookMethodAndObject.getHookLogicInstance();
        final Method method = hookMethodAndObject.getHookMethod();
        
        //if needs to be in another thread
        if (hook instanceof HookAsynchronousMarker) {
          
          HookAsynchronous.callbackAsynchronous(hooksContext, hooksBean, new HookAsynchronousHandler() {

            public void callback(HooksContext hooksContextThread, HooksBean hooksBeanThread) {
              
              executeHook(method, hook, hooksBeanThread, hooksContextThread, vetoType);

            }
          });
          
        } else {
          executeHook(method, hook, hooksBean, hooksContext,vetoType);
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

  /**
   * execute and log hook
   * @param hookMethod
   * @param hook
   * @param hooksBean
   * @param hooksContext
   * @param vetoType 
   */
  private static void executeHook(final Method hookMethod,
      Object hook, HooksBean hooksBean,
      HooksContext hooksContext, VetoType vetoType) {
    String debugLogString = null;
    long start = System.currentTimeMillis();
    if (LOG.isDebugEnabled()) {
      debugLogString = hookLogString(hookMethod.getName(), hook, hooksContext);
      LOG.debug("START: " + debugLogString);
    }
    try {
      //groupHooks.groupPreInsert(hooksContext, hooksGroupPreInsertBean);
      GrouperUtil.invokeMethod(hookMethod, hook, new Object[]{hooksContext, hooksBean});
      if (LOG.isDebugEnabled()) {
        LOG.debug("END (normal): " + debugLogString + " (" + (System.currentTimeMillis() - start) + "ms)");
      }
    } catch (HookVeto hv) {
      hv.assignVetoType(vetoType, false);
      if (LOG.isDebugEnabled()) {
        LOG.debug("END (veto): " + debugLogString + " (" + (System.currentTimeMillis() - start) + "ms)" 
            + ", veto key: " + hv.getReasonKey() + ", veto message: " + StringUtils.abbreviate(hv.getReason(), 50) );
      }
      
      //see if allowed to veto this:
      if (vetoType == null) {
        throw new RuntimeException("You are not allowed to veto this hook! " + debugLogString);
      }
      
      throw hv;
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("END (exception): " + debugLogString + " (" + (System.currentTimeMillis() - start) + "ms)" + ", exception: " + re.getMessage(), re);
      }
      //insert into log message
      if (debugLogString == null) {
        debugLogString = hookLogString(hookMethod.getName(), hook, hooksContext);
      }
      GrouperUtil.injectInException(re, debugLogString);
      throw re;
    }
  }

  /**
   * @param hookMethodName
   * @param hook
   * @param hooksContext
   * @return the log string
   */
  private static String hookLogString(final String hookMethodName, Object hook,
      HooksContext hooksContext) {
    return "Hook " + hook.getClass().getSimpleName() + "." + hookMethodName + " id: " + hooksContext.getHookId();
  }

}
