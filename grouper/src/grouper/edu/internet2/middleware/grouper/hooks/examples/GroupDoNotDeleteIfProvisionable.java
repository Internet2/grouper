package edu.internet2.middleware.grouper.hooks.examples;

import java.util.Iterator;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class GroupDoNotDeleteIfProvisionable extends GroupHooks {

  public static final String EXTERNALIZED_TEXT_KEY_FOR_DO_NOT_DELETE_GROUP_IF_PROVISIONABLE = "externalized.text.key.for.do.not.delete.group.if.provisionable";
  /**
   * only register once
   */
  private static boolean registered = false;

  /**
   * 
   */
  public static void clearHook() {
    registered = false;
  }

  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   */
  public static void registerHookIfNecessary() {
    
    if (registered) {
      return;
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperHook.GroupDoNotDeleteIfProvisionable.autoRegister", false)) {
      //register this hook
      GrouperHooksUtils.addHookManual(GrouperHookType.GROUP.getPropertyFileKey(), 
          GroupDoNotDeleteIfProvisionable.class);
    }
    
    registered = true;

  }

  @Override
  public void groupPreDelete(HooksContext hooksContext, HooksGroupBean preDeleteBean) {
    
    List<GrouperProvisioningAttributeValue> provisioningAttributeValues = (List<GrouperProvisioningAttributeValue>)
        GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        return GrouperProvisioningService.getProvisioningAttributeValues(preDeleteBean.getGroup());
      }
    });
    
    // remove values which are not provisionable
    Iterator<GrouperProvisioningAttributeValue> iterator = provisioningAttributeValues.iterator();
    while (iterator.hasNext()) {
      GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = iterator.next();
     
      if (!grouperProvisioningAttributeValue.isDoProvision()) {
        iterator.remove();
      }
    }
    
    if (GrouperUtil.length(provisioningAttributeValues) > 0) {
      
      throw new HookVeto(EXTERNALIZED_TEXT_KEY_FOR_DO_NOT_DELETE_GROUP_IF_PROVISIONABLE, 
          "This group is provisionable to at least one provisioner, so it cannot be deleted.  "
          + "Remove the group as provisionable before deleting.");
      
    }
    
  }
  

}
