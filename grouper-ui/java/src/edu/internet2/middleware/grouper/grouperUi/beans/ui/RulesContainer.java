/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiRuleDefinition;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * container to show rules on screen
 */
public class RulesContainer {

  /**
   * 
   */
  public RulesContainer() {
  }

  /**
   * rules to show on screen
   */
  private Set<GuiRuleDefinition> guiRuleDefinitions;
  /**
   * if the logged in user can read rules, lazy loaded
   */
  private Boolean canReadRules;
  /**
   * if the logged in user can update rules, lazy loaded
   */
  private Boolean canUpdateRules;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RulesContainer.class);

  
  /**
   * @return the guiRules
   */
  public Set<GuiRuleDefinition> getGuiRuleDefinitions() {
    return this.guiRuleDefinitions;
  }

  
  /**
   * @param guiRules1 the guiRules to set
   */
  public void setGuiRuleDefinitions(Set<GuiRuleDefinition> guiRules1) {
    this.guiRuleDefinitions = guiRules1;
  }


  /**
   * if the logged in user can read rules, lazy loaded
   * @return if can read rules
   */
  public boolean isCanReadRules() {
    if (this.canReadRules == null) {
      try {
        final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
        this.canReadRules = (Boolean)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
              
              @Override
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

                AttributeDef attributeDefType = RuleUtils.ruleTypeAttributeDef();
                boolean canReadType = attributeDefType.getPrivilegeDelegate().canAttrRead(loggedInSubject);
                AttributeDef attributeDefAttr = RuleUtils.ruleAttrAttributeDef();
                boolean canReadAttr = attributeDefAttr.getPrivilegeDelegate().canAttrRead(loggedInSubject);
                return canReadType && canReadAttr;
              
              }
            });
        
  
      } catch (Exception e) {
        //ignore
        if (LOG.isDebugEnabled()) {
          LOG.debug("problem checking rule", e);
        }
        if (this.canReadRules == null) {
          this.canReadRules = false;
        }
      }
    }
    
    return this.canReadRules;
  
  }


  /**
   * if the logged in user can update rules, lazy loaded
   * @return if can update rules
   */
  public boolean isCanUpdateRules() {
    if (this.canUpdateRules == null) {
      try {
        final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
        this.canUpdateRules = (Boolean)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
              
              @Override
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                AttributeDef attributeDefType = RuleUtils.ruleTypeAttributeDef();
                boolean canUpdateType = attributeDefType.getPrivilegeDelegate().canAttrUpdate(loggedInSubject);
                AttributeDef attributeDefAttr = RuleUtils.ruleAttrAttributeDef();
                boolean canUpdateAttr = attributeDefAttr.getPrivilegeDelegate().canAttrUpdate(loggedInSubject);
                return canUpdateType && canUpdateAttr;
              }
            });
        
  
      } catch (Exception e) {
        //ignore
        if (LOG.isDebugEnabled()) {
          LOG.debug("problem checking rule", e);
        }
        if (this.canUpdateRules == null) {
          this.canUpdateRules = false;
        }
      }
    }
    
    return this.canUpdateRules;
  
  }
  
  
  
}
