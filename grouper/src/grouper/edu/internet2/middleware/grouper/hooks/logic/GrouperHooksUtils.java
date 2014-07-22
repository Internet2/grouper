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
 * $Id: GrouperHooksUtils.java,v 1.23 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.lang.reflect.Method;
import java.util.List;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Transaction;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleGrouperStartupBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHooksInitBean;
import edu.internet2.middleware.grouper.hooks.examples.GroupAttributeNameValidationHook;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeSecurityHook;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook;
import edu.internet2.middleware.grouper.hooks.examples.GrouperAttributeAssignValueRulesConfigHook;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
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
    
    GrouperStartup.startup();
    
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
        Class grouperTestClass = Class.forName("edu.internet2.middleware.grouper.helper.GrouperTest");
        boolean testing = false;
        
        try {
          testing = (Boolean)GrouperUtil.fieldValue(grouperTestClass, null, "testing", false, true, false);
        } catch (Exception e) {
          LOG.warn("You might have a wrong version of grouper-test.jar... if so, upgrade it", e);
          //might have wrong version of testing jar...
        }
        if (testing) {
        addHookManual(GrouperHookType.LIFECYCLE.getPropertyFileKey(), testLifecycle);
          GroupAttributeNameValidationHook.registerHookIfNecessary(true);
        }
        
        
      } catch (ClassNotFoundException cnfe) {
        //just ignore, probably not running unit tests
        GroupAttributeNameValidationHook.registerHookIfNecessary(false);
      }
      
      GroupTypeTupleIncludeExcludeHook.registerHookIfNecessary(false);

      GroupTypeSecurityHook.registerHookIfNecessary(false);
      
      GrouperAttributeAssignValueRulesConfigHook.registerHookIfNecessary(true);
      
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
  private static final Log LOG = GrouperUtil.getLog(GrouperHooksUtils.class);

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
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBean hooks bean
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void callHooksIfRegistered(GrouperHookTypeInterface grouperHookTypeInterface, String hookMethodName,
      HooksBean hooksBean,
      VetoType vetoType) throws HookVeto {
    callHooksIfRegistered(null, grouperHookTypeInterface, hookMethodName, hooksBean, null, 
        null, null, vetoType, false, false);
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
  public static void callHooksIfRegistered(final Object object, 
      final GrouperHookTypeInterface grouperHookTypeInterface, final String hookMethodName,
      final Class<? extends HooksBean> hooksBeanClass, Object[] businessObjects, Class[] businessClasses,
      final VetoType vetoType, boolean resetDbVersion, boolean clearDbVersion) throws HookVeto {
    callHooksIfRegistered(object, grouperHookTypeInterface, hookMethodName, null, hooksBeanClass, 
        businessObjects, businessClasses, vetoType, resetDbVersion, clearDbVersion);
  }

  /**
   * 
   * @param object that the hook is about
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBean if passing bean, pass it here, otherwise pass the reflection details
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObjects are the intances to pass to bean constructor.  e.g. group
   * @param businessClasses are the types passed to bean constructor.  e.g. Group.class
   * @param vetoType is default vetoType, e.g. VetoTypeGrouper.GROUP_PRE_INSERT
   * @param resetDbVersion if the db version should be saved, assigned, reset, etc (for low level hooks)
   * @param clearDbVersion if the db version should be cleared (e.g. on delete) (for low level hooks)
   * @throws HookVeto if there is a veto (if applicable)
   */
  private static void callHooksIfRegistered(final Object object, 
      final GrouperHookTypeInterface grouperHookTypeInterface, final String hookMethodName, final HooksBean hooksBean,
      final Class<? extends HooksBean> hooksBeanClass, Object[] businessObjects, Class[] businessClasses,
      final VetoType vetoType, boolean resetDbVersion, boolean clearDbVersion) throws HookVeto {
    
    Class<? extends HooksBean> theHooksBeanClass = hooksBeanClass != null ? hooksBeanClass : hooksBean.getClass();
    Object dbVersion = null;
    Object objectVersion = null;
    GrouperAPI grouperAPI = object instanceof GrouperAPI ? (GrouperAPI)object : null;
    if (resetDbVersion) {
      //current state in db
      dbVersion = grouperAPI.dbVersion();
      
      //take a snapshot of current object (WHY NOT JUST CLONE?)
      grouperAPI.dbVersionReset();
      objectVersion = grouperAPI.dbVersion();
      
      //put the dbVersion back temporarily
      GrouperUtil.assignField(grouperAPI, GrouperAPI.FIELD_DB_VERSION, dbVersion);
     
    }
    
    //see if there is a hook class
    List<GrouperHookMethodAndObject> hooks = GrouperHookType.hooksInstances(grouperHookTypeInterface, 
        hookMethodName, theHooksBeanClass);
    
    if (hooks != null && hooks.size() > 0) {
      
      //instantiate bean or use existing
      HooksBean theHooksBean = hooksBean != null ? hooksBean : 
        GrouperUtil.construct(hooksBeanClass, businessClasses, businessObjects);

      //loop through each hook
      for (final GrouperHookMethodAndObject hookMethodAndObject : hooks) {
        
        final Object hook = hookMethodAndObject.getHookLogicInstance();

        boolean asychronous = hook instanceof HookAsynchronousMarker;
        
        HooksContext hooksContext = new HooksContext();
        
        final Method method = hookMethodAndObject.getHookMethod();
        
        executeHook(method, hook, theHooksBean, hooksContext,vetoType, asychronous);
      
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
   * if there are hooks available, schedule the post commit call
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObject are the intances to pass to bean constructor.  e.g. group
   * @param businessClass are the types passed to bean constructor.  e.g. Group.class
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void schedulePostCommitHooksIfRegistered(
      final GrouperHookTypeInterface grouperHookTypeInterface, final String hookMethodName,
      final Class<? extends HooksBean> hooksBeanClass, Object businessObject, Class businessClass) {
    schedulePostCommitHooksIfRegistered(grouperHookTypeInterface, hookMethodName, hooksBeanClass, 
        businessClass == null ? null : new Object[]{businessObject},
            businessClass == null ? null : new Class[]{businessClass});
    
  }

  /**
   * if there are hooks available, schedule the post commit call
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBean as argument to hook
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void schedulePostCommitHooksIfRegistered(
      final GrouperHookTypeInterface grouperHookTypeInterface, final String hookMethodName,
      HooksBean hooksBean) {
    schedulePostCommitHooksIfRegistered(grouperHookTypeInterface, hookMethodName, hooksBean, null, null, null);
    
  }

  /**
   * if there are hooks available, schedule the post commit call
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObjects are the intances to pass to bean constructor.  e.g. group
   * @param businessClasses are the types passed to bean constructor.  e.g. Group.class
   * @throws HookVeto if there is a veto (if applicable)
   */
  public static void schedulePostCommitHooksIfRegistered(
      final GrouperHookTypeInterface grouperHookTypeInterface,
      final String hookMethodName, final Class<? extends HooksBean> hooksBeanClass,
      final Object[] businessObjects, final Class[] businessClasses) {
    schedulePostCommitHooksIfRegistered(grouperHookTypeInterface, hookMethodName, 
        null, hooksBeanClass, businessObjects, businessClasses);
  }
  
  /**
   * if there are hooks available, schedule the post commit call
   * @param grouperHookTypeInterface e.g. GrouperHookType.GROUP
   * @param hookMethodName is method name in hook to call e.g. groupPreInsert
   * @param hooksBean hooks bean
   * @param theHooksBeanClass e.g. HooksGroupPreInsertBean.class
   * @param businessObjects are the intances to pass to bean constructor.  e.g. group
   * @param businessClasses are the types passed to bean constructor.  e.g. Group.class
   * @throws HookVeto if there is a veto (if applicable)
   */
  private static void schedulePostCommitHooksIfRegistered(
      final GrouperHookTypeInterface grouperHookTypeInterface,
      final String hookMethodName, final HooksBean hooksBean, final Class<? extends HooksBean> theHooksBeanClass,
      final Object[] businessObjects, final Class[] businessClasses) {

    final boolean hasBean = hooksBean != null;
    final Class<? extends HooksBean> hooksBeanClass = hasBean ? hooksBean.getClass() : theHooksBeanClass;
    
    //see if there is a hook class
    final List<GrouperHookMethodAndObject> hooks = GrouperHookType.hooksInstances(
        grouperHookTypeInterface, hookMethodName, hooksBeanClass);

    if (hooks != null && hooks.size() > 0) {

      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              if (hibernateSession.isNewHibernateSession()) {
                throw new RuntimeException(
                    "How could this be a new hibernate session???");
              }

              Transaction transaction = hibernateSession.getSession().getTransaction();
              
              HooksBean localHooksBean = hasBean ? hooksBean : GrouperUtil.construct(hooksBeanClass,
                  businessClasses, businessObjects);

              //loop through each hook
              for (final GrouperHookMethodAndObject hookMethodAndObject : hooks) {

                final Object hook = hookMethodAndObject.getHookLogicInstance();
                
                final boolean asychronous = hook instanceof HookAsynchronousMarker;
                
                //clone all the args here since this is a post commit, everything needs
                //a snapshot now...  only do this if not asynchronous, since asynchronous
                //will clone already
                localHooksBean = !asychronous ? localHooksBean.clone() : localHooksBean;
                
                final HooksBean finalHooksBean = localHooksBean;
                
                final HooksContext hooksContext = new HooksContext();
        
                final Method method = hookMethodAndObject.getHookMethod();
        
                //register this on the transaction
                transaction.registerSynchronization(new Synchronization() {

                  public void afterCompletion(int status) {

                    //only do this if committed
                    if (status == Status.STATUS_COMMITTED) {

                      //we need another tx since it needs to be committed...
                      HibernateSession.callbackHibernateSession(
                          GrouperTransactionType.READ_WRITE_NEW,
                          AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

                            public Object callback(
                                HibernateHandlerBean hibernateHandlerBean)
                                throws GrouperDAOException {
                              HibernateSession hibernateSession = hibernateHandlerBean
                                  .getHibernateSession();
                              executeHook(method, hook, finalHooksBean, hooksContext,
                                  null, asychronous);
                              //for some reason there is a tx, but it doesnt commit...
                              hibernateSession.commit(GrouperCommitType.COMMIT_NOW);
                              return null;
                            }

                          });
                    }
                  }

                  public void beforeCompletion() {
                  }

                });
              }
              return null;

            }
        });

    }

  }

  /**
   * execute and log hook
   * @param hookMethod
   * @param hook
   * @param hooksBean
   * @param hooksContext
   * @param vetoType 
   * @param asynchronous 
   */
  private static void executeHook(final Method hookMethod,
      final Object hook, final HooksBean hooksBean,
      final HooksContext hooksContext, final VetoType vetoType, boolean asynchronous) {
    
    if (asynchronous) {
      HookAsynchronous.callbackAsynchronous(hooksContext, hooksBean, new HookAsynchronousHandler() {

        public void callback(HooksContext hooksContextThread, HooksBean hooksBeanThread) {
          
          executeHook(hookMethod, hook, hooksBeanThread, hooksContextThread, vetoType, false);

        }
      });
      return;
    }
    
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
