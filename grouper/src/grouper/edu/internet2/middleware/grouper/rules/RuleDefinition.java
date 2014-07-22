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
/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueContainer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;




/**
 * Define a rule, convert to a JSON string for attribute
 * @author mchyzer
 *
 */
public class RuleDefinition {

  /**
   * load rules for one attribute type assign id
   * @param attributeTypeAssignId
   */
  public RuleDefinition(String attributeTypeAssignId) {
    
    Set<AttributeAssignValueContainer> result = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByAssignTypeId(attributeTypeAssignId);
    
    construct(result);
  }
  
  /**
   * rule definitions from attribute assigns
   * @param attributeAssignValueContainers
   */
  public RuleDefinition(
      Set<AttributeAssignValueContainer> attributeAssignValueContainers) {
    
    construct(attributeAssignValueContainers);
  }

  /**
   * @param attributeAssignValueContainers
   */
  private void construct(Set<AttributeAssignValueContainer> attributeAssignValueContainers) {
    
    if (GrouperUtil.length(attributeAssignValueContainers) == 0) {
      return;
    }
    
    //RuleUtils.RULE_ACT_AS_SUBJECT_ID
    //RuleUtils.RULE_ACT_AS_SUBJECT_IDENTIFIER
    //RuleUtils.RULE_ACT_AS_SUBJECT_SOURCE_ID
    //RuleUtils.RULE_CHECK_TYPE
    //RuleUtils.RULE_CHECK_OWNER_ID
    //RuleUtils.RULE_CHECK_OWNER_NAME
    //RuleUtils.RULE_IF_CONDITION_EL
    //RuleUtils.RULE_IF_CONDITION_ENUM
    //RuleUtils.RULE_IF_OWNER_ID
    //RuleUtils.RULE_IF_OWNER_NAME
    //RuleUtils.RULE_THEN_EL
    //RuleUtils.RULE_RUN_DAEMON
    
    String actAsSubjectId = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleActAsSubjectIdName());
    String actAsSubjectIdentifier = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleActAsSubjectIdentifierName());
    String actAsSubjectSourceId = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleActAsSubjectSourceIdName());
    String checkTypeString = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckTypeName());
    String checkOwnerId = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckOwnerIdName());
    String checkOwnerName = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckOwnerNameName());
    
    AttributeAssign attributeAssignType = attributeAssignValueContainers.iterator().next().getAttributeTypeAssign();

    //this is done in the RuleCheckType I believe
    ////if owner id and name are blank, then we get from where the rules are assigned
    //if (StringUtils.isBlank(checkOwnerId) && StringUtils.isBlank(checkOwnerName)) {
    //  //if it is stem, lets go by name, if it is not, then go by id
    //  if (attributeAssignType.getAttributeAssignType() == AttributeAssignType.stem) {
    //    //lets get the stem
    //    Stem stem = attributeAssignType.getOwnerStem();
    //    checkOwnerName = stem.getName();
    //  } else {
    //    checkOwnerId = attributeAssignType.getOwnerSingleId();
    //  }
    //}

    String checkArg0 = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckArg0Name());
    String checkArg1 = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckArg1Name());
    String checkStemScope = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckStemScopeName());
    String ifConditionEl = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfConditionElName());
    String ifConditionEnum = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfConditionEnumName());
    String ifConditionEnumArg0 = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfConditionEnumArg0Name());
    String ifConditionEnumArg1 = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfConditionEnumArg1Name());
    String ifOwnerId = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfOwnerIdName());
    String ifOwnerName = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfOwnerNameName());
    String ifStemScope = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfStemScopeName());
    String thenEl = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleThenElName());
    String thenEnum = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleThenEnumName());
    String thenEnumArg0 = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleThenEnumArg0Name());
    String thenEnumArg1 = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleThenEnumArg1Name());
    String thenEnumArg2 = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleThenEnumArg2Name());
    String runDaemonTf = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleRunDaemonName());
    
    //lets do the subject first
    RuleSubjectActAs ruleSubjectActAs = new RuleSubjectActAs(
        actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier);
    
    RuleCheck ruleCheck = new RuleCheck(checkTypeString, checkOwnerId, 
        checkOwnerName, checkStemScope, checkArg0, checkArg1);
    
    RuleIfCondition ruleIfCondition = new RuleIfCondition(ifConditionEl, ifConditionEnum, 
        ifOwnerId, ifOwnerName, ifStemScope, ifConditionEnumArg0, ifConditionEnumArg1);
    
    RuleThen ruleThen = new RuleThen(thenEl, thenEnum, thenEnumArg0, thenEnumArg1, thenEnumArg2);

    construct(attributeAssignType, 
        ruleSubjectActAs, ruleCheck, ruleIfCondition, ruleThen);
    
    this.runDaemon = runDaemonTf;
    
  }
  

  /**
   * keep a reference to this to get back to the owner etc
   */
  private AttributeAssign attributeAssignType;
  
  
  /**
   * keep a reference to this to get back to the owner etc
   * @return the attributeAssignType
   */
  public AttributeAssign getAttributeAssignType() {
    return this.attributeAssignType;
  }

  
  /**
   * keep a reference to this to get back to the owner etc
   * @param attributeAssignType1 the attributeAssignType to set
   */
  public void setAttributeAssignType(AttributeAssign attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  /**
   * 
   */
  public RuleDefinition() {
    super();
    
  }

  /**
   * @param theAttributeAssignType
   * @param actAs
   * @param check
   * @param ifCondition
   * @param then
   */
  private void construct(AttributeAssign theAttributeAssignType, RuleSubjectActAs actAs,
      RuleCheck check, RuleIfCondition ifCondition, RuleThen then) {
    this.attributeAssignType = theAttributeAssignType;
    this.actAs = actAs;
    this.check = check;
    this.ifCondition = ifCondition;
    this.then = then;
  }

  /** run daemon */
  private String runDaemon;
  
  /** who this rule acts as */
  private RuleSubjectActAs actAs;

  /** when this rules is triggered */
  private RuleCheck check;

  /** only fire if this condition occurs */
  private RuleIfCondition ifCondition;
  
  /** do this when the rule fires */
  private RuleThen then;

  /**
   * who this rule acts as
   * @return who this rule acts as
   */
  public RuleSubjectActAs getActAs() {
    return this.actAs;
  }

  /**
   * who this rule acts as
   * @param actAs1
   */
  public void setActAs(RuleSubjectActAs actAs1) {
    this.actAs = actAs1;
  }

  /**
   * when this rules is triggered
   * @return the check
   */
  public RuleCheck getCheck() {
    return check;
  }

  /**
   * when this rules is triggered
   * @param check1
   */
  public void setCheck(RuleCheck check1) {
    this.check = check1;
  }

  /**
   * only fire if this condition occurs
   * @return the if condition
   */
  public RuleIfCondition getIfCondition() {
    return ifCondition;
  }

  /**
   * only fire if this condition occurs
   * @param ifCondition1
   */
  public void setIfCondition(RuleIfCondition ifCondition1) {
    this.ifCondition = ifCondition1;
  }

  /**
   * do this when the rule fires
   * @return the then part
   */
  public RuleThen getThen() {
    return then;
  }

  /**
   * do this when the rule fires
   * @param then1
   */
  public void setThen(RuleThen then1) {
    this.then = then1;
  }

  /**
   * 
   * @return string for run daemon
   */
  public String getRunDaemon() {
    return this.runDaemon;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleDefinition.class);

  /**
   * if we should run the daemon, then do
   * @return if run
   */
  public boolean runDaemonOnDefinitionIfShould() {
    
    if (!StringUtils.isBlank(this.getIfCondition().getIfConditionEl()) || !this.isRunDaemonBoolean()) {
      return false;
    }
    //setup the actAs
    RuleSubjectActAs actAs = this.getActAs();
    Subject actAsSubject = actAs.subject(false);
    if (actAsSubject == null) {
      LOG.error("Cant find subject for rule: " + this);
    } else {
      
      GrouperSession.callbackGrouperSession(GrouperSession.start(actAsSubject, false), new GrouperSessionHandler() {

        /**
         * 
         */
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          //we are running the daemon, lets go
          RuleDefinition.this.getCheck().checkTypeEnum().runDaemon(RuleDefinition.this);
          return null;
          
        }
      });
    }        
    
    return true;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    try {
      if (this.attributeAssignType != null && !StringUtils.isBlank(this.attributeAssignType.getId())) {
        result.append("attributeAssignTypeId: ").append(this.attributeAssignType.getId()).append(", ");
      }
      if (this.actAs != null) {
        this.actAs.toStringHelper(result);
      }
      if (this.check != null) {
        this.check.toStringHelper(result);
      }
      if (this.ifCondition != null) {
        this.ifCondition.toStringHelper(result);
      }
      if (this.then != null) {
        this.then.toStringHelper(result);
      }
      if (this.runDaemon != null) {
        result.append("runDaemon: ").append(this.runDaemon).append(", ");
      }
    } catch (Exception e) {
      //ignore, we did the best we could
    }
    return result.toString();
  }
  
  /**
   * validate this 
   * @return the error or null if none
   */
  public String validate() {
    String reason = null;
    
    if (this.attributeAssignType == null) {
      return "type attributeAssign is required";
    }

    if (this.actAs != null) {
      reason = this.actAs.validate(this);
      if (!StringUtils.isBlank(reason)) {
        return reason;
      }
    } else {
      return "ActAs is required";
    }
    
    if (this.check != null) {
      reason = this.check.validate(this);
      if (!StringUtils.isBlank(reason)) {
        return reason;
      }
    } else {
      return "Check is required";
    }

    //note, if condition can be null
    if (this.ifCondition != null) {
      reason = this.ifCondition.validate(this);
      if (!StringUtils.isBlank(reason)) {
        return reason;
      }
    } 

    if (this.then != null) {
      reason = this.then.validate(this);
      if (!StringUtils.isBlank(reason)) {
        return reason;
      }
    } else {
      return "then is required";
    }

    if (this.runDaemon != null) {
      try {
        this.isRunDaemonBoolean();

        //if if is not enum, then cant be T
        if (!StringUtils.isBlank(this.getIfCondition().getIfConditionEl()) && this.isRunDaemonBoolean()) {
          return "runDaemon cant be true if the IF EL is set, must be enum";
        }
      } catch (Exception e) {
        return "runDaemon must be T or F";
      }
      
      
    }
    
    return null;
  
  }
  
  /**
   * run daemon, true by default
   * @return if run daemon
   */
  public boolean isRunDaemonBoolean() {
    return GrouperUtil.booleanValue(this.runDaemon, true);
  }
  
  /**
   * if we should log this rule definition
   * @return true if should log
   */
  public boolean shouldLog() {
    if (this.attributeAssignType == null) {
      return false;
    }
    String attributeTypeAssignIdsToLog = GrouperConfig.retrieveConfig().propertyValueString("rules.attributeAssignTypeIdsToLog");

    //since we are working with uuid's, we can just do a contains to see if it is there
    return StringUtils.defaultString(attributeTypeAssignIdsToLog).contains(this.attributeAssignType.getId());
  }
  
  /**
   * add EL variables to the substitute map
   * @param variableMap
   * @param rulesBean 
   * @param hasAccessToEl 
   */
  public void addElVariables(Map<String, Object> variableMap, RulesBean rulesBean, boolean hasAccessToEl) {
    this.getCheck().addElVariables(this, variableMap, rulesBean, hasAccessToEl);
    
    AttributeAssign attributeAssign = this.getAttributeAssignType();

    if (hasAccessToEl) {
      variableMap.put("attributeAssignType", attributeAssign);
      variableMap.put("ruleDefinition", this);
      variableMap.put("grouperUtil", new GrouperUtil());
      variableMap.put("ruleUtils", new RuleUtils());
      
    }

    variableMap.put("ruleElUtils", new RuleElUtils());

    if (attributeAssign != null) {
      
      if (!StringUtils.isBlank(attributeAssign.getOwnerAttributeAssignId())) {
        variableMap.put("ownerAttributeAssignId", attributeAssign.getOwnerAttributeAssignId());
      }
      
      if (!StringUtils.isBlank(attributeAssign.getOwnerAttributeDefId())) {
        variableMap.put("ownerAttributeDefId", attributeAssign.getOwnerAttributeDefId());
      }
      if (!StringUtils.isBlank(attributeAssign.getOwnerGroupId())) {
        variableMap.put("ownerGroupId", attributeAssign.getOwnerGroupId());
      }
      if (!StringUtils.isBlank(attributeAssign.getOwnerMemberId())) {
        variableMap.put("ownerMemberId", attributeAssign.getOwnerMemberId());
      }
      if (!StringUtils.isBlank(attributeAssign.getOwnerMembershipId())) {
        variableMap.put("ownerMembershipId", attributeAssign.getOwnerMembershipId());
      }
      if (!StringUtils.isBlank(attributeAssign.getOwnerStemId())) {
        variableMap.put("ownerStemId", attributeAssign.getOwnerStemId());
      }
      
    }
    
    //middleware.grouper.rules.MyRuleUtils
    String customElClasses = GrouperConfig.retrieveConfig().propertyValueString("rules.customElClasses");
    if (!StringUtils.isBlank(customElClasses)) {
      String[] customElClassesArray = GrouperUtil.splitTrim(customElClasses, ",");
      for (String customElClass : customElClassesArray) {
        Class<?> customClassClass = GrouperUtil.forName(customElClass);
        String simpleName = StringUtils.uncapitalize(customClassClass.getSimpleName());
        variableMap.put(simpleName, GrouperUtil.newInstance(customClassClass));
      }
    }
  }
  
}
