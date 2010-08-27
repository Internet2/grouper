/**
 * 
 */
package edu.internet2.middleware.grouper.hooks.examples;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Fire on attribute assign value changes, validate the rules and 
 * set the answer in "valid" T|F attribute value
 * @author mchyzer
 *
 */
public class GrouperAttributeAssignValueRulesConfigHook extends AttributeAssignValueHooks {

  /**
   * 
   */
  public GrouperAttributeAssignValueRulesConfigHook() {
    // TODO Auto-generated constructor stub
  }

  /**
   * 
   */
  @Override
  public void attributeAssignValuePostDelete(HooksContext hooksContext,
      HooksAttributeAssignValueBean postDeleteBean) {
    validateRule(postDeleteBean.getAttributeAssignValue());
  }

  /**
   * 
   */
  @Override
  public void attributeAssignValuePostInsert(HooksContext hooksContext,
      HooksAttributeAssignValueBean postInsertBean) {
    validateRule(postInsertBean.getAttributeAssignValue());
  }

  /**
   * 
   */
  @Override
  public void attributeAssignValuePostUpdate(HooksContext hooksContext,
      HooksAttributeAssignValueBean postUpdateBean) {
    validateRule(postUpdateBean.getAttributeAssignValue());
  }

  /** thread local to avoid circular references */
  private static ThreadLocal<Boolean> threadLocalInValidateRule = new ThreadLocal();
  
  /**
   * validate this rule
   * @param attributeAssignValue
   */
  public void validateRule(AttributeAssignValue attributeAssignValue) {

    //we want to avoid circular references
    Boolean inValidateAlready = threadLocalInValidateRule.get();
    if (inValidateAlready == null) {
      inValidateAlready = false;
    }
    
    //no need to validate if already in validate, dont want a circular reference
    if (inValidateAlready) {
      return;
    }
    
    threadLocalInValidateRule.set(true);
    
    try {
    
      //see if this is a rule attribute
      AttributeAssign attributeAssign = attributeAssignValue.getAttributeAssign();
      
      if (!StringUtils.equals(RuleUtils.ruleAttrAttributeDef().getId(), 
          attributeAssign.getAttributeDef().getId())) {
        return;
      }
      
      //this is a rule attribute, lets validate
      RuleDefinition ruleDefinition = new RuleDefinition(attributeAssign.getOwnerAttributeAssignId());
  
      String validReason = ruleDefinition.validate();
      
      if (StringUtils.isBlank(validReason)) {
        validReason = "T";
      } else {
        validReason = "INVALID: " + validReason;
      }
      
      //now, we need to assign the valid attribute if not there already
      AttributeAssign typeAttributeAssign = attributeAssign.getOwnerAttributeAssign();
      
      typeAttributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleValidName(), validReason);
      
    } finally {
      threadLocalInValidateRule.set(false);
    }
  }
  
  /**
   * only register once
   */
  private static boolean registered = false;

  /** if the hook was registered and being used */
  private static boolean registeredSuccess = false;
  
  /**
   * register this hook
   * @param tryAgainIfNotBefore 
   */
  public static void registerHookIfNecessary(boolean tryAgainIfNotBefore) {
    
    if (registered && !tryAgainIfNotBefore) {
      return;
    }
    
    //if trying again, but already registered, fine
    if (tryAgainIfNotBefore && registeredSuccess) {
      return;
    }

      
    registeredSuccess = true;
    LOG.debug("Registering hooks GrouperAttributeAssignValueRulesConfigHook");
    //register this hooks
    GrouperHooksUtils.addHookManual(GrouperHookType.ATTRIBUTE_ASSIGN_VALUE.getPropertyFileKey(), 
        GrouperAttributeAssignValueRulesConfigHook.class);
    
    registered = true;

  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperAttributeAssignValueRulesConfigHook.class);

  
  /**
   * @return the threadLocalInValidateRule
   */
  public static ThreadLocal<Boolean> getThreadLocalInValidateRule() {
    return threadLocalInValidateRule;
  }

}
