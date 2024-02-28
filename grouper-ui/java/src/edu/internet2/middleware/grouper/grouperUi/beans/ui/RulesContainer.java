/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiRuleDefinition;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleConfig;
import edu.internet2.middleware.grouper.rules.RuleIfConditionEnum;
import edu.internet2.middleware.grouper.rules.RuleOwnerType;
import edu.internet2.middleware.grouper.rules.RulePattern;
import edu.internet2.middleware.grouper.rules.RuleThenEnum;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
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
  
  private GuiRuleDefinition currentGuiRuleDefinition;
  
  /**
   * if can view privilege inheritance
   * @return true if can
   */
  public boolean isCanReadPrivilegeInheritance() {

    boolean privilegeInheritanceReadRequireAdmin = GrouperUiConfig.retrieveConfig()
        .propertyValueBoolean("uiV2.privilegeInheritanceReadRequireAdmin", false);

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    if (privilegeInheritanceReadRequireAdmin && !PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return false;
    }
    
    final String privilegeInheritanceReadRequireGroup = GrouperUiConfig.retrieveConfig()
        .propertyValueString("uiV2.privilegeInheritanceReadRequireGroup");

    if (!StringUtils.isBlank(privilegeInheritanceReadRequireGroup)) {
      
      if (false == (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Group group = GroupFinder.findByName(grouperSession, privilegeInheritanceReadRequireGroup, true);
          if (!group.hasMember(loggedInSubject)) {
            return false;
          }
          return true;
        }
      })) {
        return false;
      }

    }
    
    boolean privilegeInheritanceDoesntRequireRulesPrivileges = GrouperUiConfig.retrieveConfig()
        .propertyValueBoolean("uiV2.privilegeInheritanceDoesntRequireRulesPrivileges", true);
    
    if (privilegeInheritanceDoesntRequireRulesPrivileges) {
      return true;
    }
    
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer().isCanReadRules();
  }

  /**
   * if can update privilege inheritance
   * @return true if can
   */
  public boolean isCanUpdatePrivilegeInheritance() {

    boolean privilegeInheritanceUpdateRequireAdmin = GrouperUiConfig.retrieveConfig()
        .propertyValueBoolean("uiV2.privilegeInheritanceUpdateRequireAdmin", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    if (privilegeInheritanceUpdateRequireAdmin && !PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return false;
    }
    
    final String privilegeInheritanceUpdateRequireGroup = GrouperUiConfig.retrieveConfig()
        .propertyValueString("uiV2.privilegeInheritanceUpdateRequireGroup");

    if (!StringUtils.isBlank(privilegeInheritanceUpdateRequireGroup)) {
      
      if (false == (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Group group = GroupFinder.findByName(grouperSession, privilegeInheritanceUpdateRequireGroup, true);
          if (!group.hasMember(loggedInSubject)) {
            return false;
          }
          return true;
        }
      })) {
        return false;
      }
    }
    boolean privilegeInheritanceDoesntRequireRulesPrivileges = GrouperUiConfig.retrieveConfig()
        .propertyValueBoolean("uiV2.privilegeInheritanceDoesntRequireRulesPrivileges", true);
    
    if (privilegeInheritanceDoesntRequireRulesPrivileges) {
      return true;
    }
    
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer().isCanUpdateRules();
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
  
  
  public GuiRuleDefinition getCurrentGuiRuleDefinition() {
    return currentGuiRuleDefinition;
  }

  
  public void setCurrentGuiRuleDefinition(GuiRuleDefinition currentGuiRuleDefinition) {
    this.currentGuiRuleDefinition = currentGuiRuleDefinition;
  }
  
  public Map<String, String> getAllCheckTypes() {
    RuleCheckType[] values = RuleCheckType.values();
    Map<String, String> checkTypes = new HashMap<>();
    
    
    for (RuleCheckType checkType: values) {
      
      GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
      GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
      
      if (guiStem != null && checkType.getOwnerType() == RuleOwnerType.FOLDER) {
        checkTypes.put(checkType.name(), TextContainer.textOrNull("ruleCheckTypeOptionUserFriendlyLabel_"+checkType.name()));
      } else if (guiGroup != null && checkType.getOwnerType() == RuleOwnerType.GROUP) {
        checkTypes.put(checkType.name(), TextContainer.textOrNull("ruleCheckTypeOptionUserFriendlyLabel_"+checkType.name()));
      }
      
      
    }
    
    List<Entry<String, String>> list = new LinkedList<>(checkTypes.entrySet());
    Collections.sort(list, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

    Map<String, String> sortedMap = new LinkedHashMap<>();
    for (Entry<String, String> entry : list) {
        sortedMap.put(entry.getKey(), entry.getValue());
    }
    
    return sortedMap;
  }
  
  public Map<String, String> getAllPatterns() {
    RulePattern[] values = RulePattern.values();
    Map<String, String> rulePatterns = new HashMap<>();
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    for (RulePattern pattern: values) {
      if (guiStem != null && pattern.isApplicableForFolders()) {
        rulePatterns.put(pattern.name(), pattern.getUserFriendlyText());
      } else if (guiGroup != null && pattern.isApplicableForGroups()) {
        rulePatterns.put(pattern.name(), pattern.getUserFriendlyText());
      }
    }
    
    List<Entry<String, String>> list = new LinkedList<>(rulePatterns.entrySet());
    Collections.sort(list, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

    Map<String, String> sortedMap = new LinkedHashMap<>();
    for (Entry<String, String> entry : list) {
        sortedMap.put(entry.getKey(), entry.getValue());
    }
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      sortedMap.put("custom", TextContainer.textOrNull("rulePatternCustomUserFriendlyText")); //Custom pattern at he the bottom
    }    
    return sortedMap;
  }

  public Map<String, String> getAllIfConditionOptions() {
    RuleIfConditionEnum[] ruleIfConditionEnums = RuleIfConditionEnum.values();
    Map<String, String> result = new HashMap<>();
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    
    for (RuleIfConditionEnum ruleIfConditionEnum: ruleIfConditionEnums) {
      
      if (ruleIfConditionEnum.isAdminOnly()) {
        if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
          
          if (guiStem != null && ruleIfConditionEnum.getOwnerType() == RuleOwnerType.FOLDER) {
            result.put(ruleIfConditionEnum.name(), TextContainer.textOrNull("ruleIfConditionOptionUserFriendlyLabel_"+ruleIfConditionEnum.name()));
          } else if (guiGroup != null && ruleIfConditionEnum.getOwnerType() == RuleOwnerType.GROUP) {
            result.put(ruleIfConditionEnum.name(), TextContainer.textOrNull("ruleIfConditionOptionUserFriendlyLabel_"+ruleIfConditionEnum.name()));
          }
          
        }
      } else {
        
        if (guiStem != null && ruleIfConditionEnum.getOwnerType() == RuleOwnerType.FOLDER) {
          result.put(ruleIfConditionEnum.name(), TextContainer.textOrNull("ruleIfConditionOptionUserFriendlyLabel_"+ruleIfConditionEnum.name()));
        } else if (guiGroup != null && ruleIfConditionEnum.getOwnerType() == RuleOwnerType.GROUP) {
          result.put(ruleIfConditionEnum.name(), TextContainer.textOrNull("ruleIfConditionOptionUserFriendlyLabel_"+ruleIfConditionEnum.name()));
        }
        
      }
      
    }
    
    List<Entry<String, String>> list = new LinkedList<>(result.entrySet());
    Collections.sort(list, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

    Map<String, String> sortedMap = new LinkedHashMap<>();
    for (Entry<String, String> entry : list) {
        sortedMap.put(entry.getKey(), entry.getValue());
    }
    
    sortedMap.put("EL", TextContainer.textOrNull("guiCustomUiUserQueryTypeLabel_expressionlanguage")); //it's at the bottom
    return sortedMap;
  }
  
  public Map<String, String> getAllThenOptions() {
    
    RuleThenEnum[] ruleThenEnums = RuleThenEnum.values();
    Map<String, String> result = new HashMap<>();
    for (RuleThenEnum ruleThenEnum: ruleThenEnums) {
      
      String userFriendlyLabel = TextContainer.textOrNull("ruleThenOptionUserFriendlyLabel_"+ruleThenEnum.name());
      if (StringUtils.isBlank(userFriendlyLabel)) {
        userFriendlyLabel = ruleThenEnum.name();
      }
      result.put(ruleThenEnum.name(), userFriendlyLabel);
    }
    
    List<Entry<String, String>> list = new LinkedList<>(result.entrySet());
    Collections.sort(list, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

    Map<String, String> sortedMap = new LinkedHashMap<>();
    for (Entry<String, String> entry : list) {
        sortedMap.put(entry.getKey(), entry.getValue());
    }
    
    result.put("EL", TextContainer.textOrNull("guiCustomUiUserQueryTypeLabel_expressionlanguage"));
    return sortedMap;
    
  }
  
  private RuleConfig ruleConfig;
  private String attributeAssignId;

  
  public RuleConfig getRuleConfig() {
    return ruleConfig;
  }

  
  public void setRuleConfig(RuleConfig ruleConfig) {
    this.ruleConfig = ruleConfig;
  }

  public void setAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
  }

  
  public String getAttributeAssignId() {
    return attributeAssignId;
  }
  
  public boolean isCanViewAllRules() {
    return true;
  }

  
}
