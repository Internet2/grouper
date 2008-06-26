/*
 * @author mchyzer
 * $Id: GrouperHooksUtils.java,v 1.3 2008-06-26 11:16:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.util.List;

import edu.internet2.middleware.grouper.hooks.beans.HooksBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperHooksUtils {

  
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
    callHooksIfRegistered(grouperHookType, hookMethodName, hooksBeanClass, new Object[]{businessObject},
        new Class[]{businessClass}, vetoType);
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

  }

}
