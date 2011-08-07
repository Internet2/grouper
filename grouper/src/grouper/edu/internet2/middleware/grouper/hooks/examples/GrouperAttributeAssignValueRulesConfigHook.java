/**
 * 
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.RuleSubjectActAs;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


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
    validateRule(postDeleteBean.getAttributeAssignValue(), true);
  }

  /**
   * 
   */
  @Override
  public void attributeAssignValuePostInsert(HooksContext hooksContext,
      HooksAttributeAssignValueBean postInsertBean) {
    
    validateRule(postInsertBean.getAttributeAssignValue(), false);
  }

  /**
   * 
   */
  @Override
  public void attributeAssignValuePostUpdate(HooksContext hooksContext,
      HooksAttributeAssignValueBean postUpdateBean) {
    validateRule(postUpdateBean.getAttributeAssignValue(), false);
  }

  /** thread local to avoid circular references */
  private static ThreadLocal<Boolean> threadLocalInValidateRule = new ThreadLocal();
  
  /**
   * validate this rule
   * @param attributeAssignValue
   * @param isDelete 
   */
  public void validateRule(final AttributeAssignValue attributeAssignValue, final boolean isDelete) {

    Subject runningSubject = GrouperSession.staticGrouperSession().getSubject();
    
    RuleSubjectActAs.actAsThreadLocalAssign(runningSubject);
    
    try {
      
      //do this as admin
      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
  
        /**
         * 
         */
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          //if deleting the attribute rule type, then dont worry about validations...
          Set<AttributeAssign> attributeAssignDeletes = AttributeAssign.attributeAssignDeletes();
  
          AttributeDefName ruleAttributeDefName = RuleUtils.ruleAttributeDefName();
          if (ruleAttributeDefName != null) {
  
            for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssignDeletes)) {
              if (StringUtils.equals(ruleAttributeDefName.getId(), attributeAssign.getAttributeDefNameId())) {
                //this means we are deleting a rule type, so dont worry about validating stuff
                return null;
              }
            }
          }
          
          //we want to avoid circular references
          Boolean inValidateAlready = threadLocalInValidateRule.get();
          if (inValidateAlready == null) {
            inValidateAlready = false;
          }
          
          //no need to validate if already in validate, dont want a circular reference
          if (inValidateAlready) {
            return null;
          }
          
          threadLocalInValidateRule.set(true);
          
          try {
          
            //see if this is a rule attribute
            AttributeAssign attributeAssign = attributeAssignValue.getAttributeAssign();
            
            AttributeDefName attributeDefName = attributeAssign.getAttributeDefName();
            if (!StringUtils.equals(RuleUtils.ruleAttrAttributeDef().getId(), 
                attributeDefName.getAttributeDefId())) {
              return null;
            }
            
            //we want the rules to refresh since something changed
            RuleEngine.clearRuleEngineCache();
                  
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
            
            if (isDelete && !StringUtils.equals("T", validReason)) {
              //you are allowed to delete the valid attribute (so you can remove the whole thing..)
              //note, the valid attribute will need to be deleted last... will this be a problem on cascade?
              //if this isnt the valid attribute itself, if it is, it will be deleted by whatever fired the hook
              if (!StringUtils.equals(RuleUtils.ruleValidName(), attributeDefName.getName())) {
                //delete the valid attribute...
                typeAttributeAssign.getAttributeDelegate().removeAttribute(RuleUtils.ruleValidAttributeDefName());
              }        
            } else {
  
              typeAttributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleValidName(), validReason);
              
            }      
  
            
            
          } finally {
            threadLocalInValidateRule.set(false);
          }
          return null;
        }
        
      });
    } finally {
      RuleSubjectActAs.actAsThreadLocalClear();
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
