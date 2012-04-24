/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueContainer;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.examples.GrouperAttributeAssignValueRulesConfigHook;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * processes rules and kicks off actions
 * @author mchyzer
 *
 */
public class RuleEngine {

  /**
   * used for testing to see how many rule firings there are (that pass the check and if)
   */
  static long ruleFirings = 0;
  
  /** cached rule definitions */
  static GrouperCache<Boolean, RuleEngine> ruleEngineCache = 
    new GrouperCache<Boolean, RuleEngine>(RuleEngine.class.getName() + ".ruleEngine", 100, false, 5*60, 5*60, false);
  
  /** rule definitions */
  private Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
  
  /**
   * rule definitions
   * @return the ruleDefinitions
   */
  public Set<RuleDefinition> getRuleDefinitions() {
    return this.ruleDefinitions;
  }

  /**
   * get rule definitions from cache based on name or id
   * @param ruleCheck
   * @return the definitions
   */
  public Set<RuleDefinition> ruleCheckIndexDefinitionsByNameOrId(RuleCheck ruleCheck) {
   
    if (!GrouperConfig.getPropertyBoolean("rules.enable", true)) {
      return null;
    }

    String ownerId = ruleCheck.getCheckOwnerId();
    String ownerName = ruleCheck.getCheckOwnerName();
    
    Set<RuleDefinition> theRuleDefinitions = new HashSet<RuleDefinition>();
    
    if (!StringUtils.isBlank(ownerId)) {
      ruleCheck.setCheckOwnerName(null);
      Set<RuleDefinition> ruleDefinitionsToAdd = GrouperUtil.nonNull(this.getRuleCheckIndex().get(ruleCheck));
      
      if (LOG.isDebugEnabled() && GrouperConfig.getPropertyBoolean("rules.logWhyRulesDontFire", false)) {
        
        StringBuilder logMessage = new StringBuilder("Checking rules by id: ");
        logMessage.append(ownerId);
        logMessage.append(", ids found: ");
        for (RuleDefinition current : ruleDefinitionsToAdd) {
          logMessage.append(current.getCheck().getCheckOwnerId()).append(",");
        }
        
        logMessage.append("... ids in rules engine: ");
        for (RuleCheck current : this.getRuleCheckIndex().keySet()) {
          if (!StringUtils.isBlank(current.getCheckOwnerId())) {
            logMessage.append(current.getCheckOwnerId()).append(",");
          }
        }
        LOG.debug(logMessage);
      }
      
      theRuleDefinitions.addAll(ruleDefinitionsToAdd);
      ruleCheck.setCheckOwnerName(ownerName);
    }
    
    if (!StringUtils.isBlank(ownerName)) {
      ruleCheck.setCheckOwnerId(null);
      Set<RuleDefinition> ruleDefinitionsToAdd = GrouperUtil.nonNull(this.getRuleCheckIndex().get(ruleCheck));

      if (LOG.isDebugEnabled() && GrouperConfig.getPropertyBoolean("rules.logWhyRulesDontFire", false)) {
        
        StringBuilder logMessage = new StringBuilder("Checking rules by name: ");
        logMessage.append(ownerName);
        logMessage.append(", names found: ");
        for (RuleDefinition current : ruleDefinitionsToAdd) {
          logMessage.append(current.getCheck().getCheckOwnerName()).append(",");
        }
        
        logMessage.append("... names in rules engine: ");
        for (RuleCheck current : this.getRuleCheckIndex().keySet()) {
          if (!StringUtils.isBlank(current.getCheckOwnerName())) {
            logMessage.append(current.getCheckOwnerName()).append(",");
          }
        }
        LOG.debug(logMessage);
      }
      
      theRuleDefinitions.addAll(ruleDefinitionsToAdd);
      ruleCheck.setCheckOwnerId(ownerId);
    }
    
    return theRuleDefinitions;
  }
  
  /**
   * get rule definitions from cache based on name or id
   * @param ruleCheck
   * @return the definitions
   */
  public Set<RuleDefinition> ruleCheckIndexDefinitionsByNameOrIdInFolder(RuleCheck ruleCheck) {
   
    if (!GrouperConfig.getPropertyBoolean("rules.enable", true)) {
      return null;
    }

    String ownerName = ruleCheck.getCheckOwnerName();
    
    if (StringUtils.isBlank(ownerName)) {
      throw new RuntimeException("Checking in folder needs an owner name: " + ruleCheck);
    }
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //direct parent
    String parentStemName = GrouperUtil.parentStemNameFromName(ownerName);
    
    RuleCheck tempRuleCheck = new RuleCheck(ruleCheck.getCheckType(), null, parentStemName, Stem.Scope.ONE.name(), null, null);
    ruleDefinitions.addAll(GrouperUtil.nonNull(this.getRuleCheckIndex().get(tempRuleCheck)));
    
    //direct parent or ancestor
    Set<String> parentStemNames = GrouperUtil.findParentStemNames(ownerName);
    
    for (String ancestorStemName : parentStemNames) {
      
      tempRuleCheck = new RuleCheck(ruleCheck.getCheckType(), null, ancestorStemName, Stem.Scope.SUB.name(), null, null);
      ruleDefinitions.addAll(GrouperUtil.nonNull(this.getRuleCheckIndex().get(tempRuleCheck)));
      
    }
    
    return ruleDefinitions;
  }
  
  /**
   * rule definitions
   * @param ruleDefinitions the ruleDefinitions to set
   */
  public void setRuleDefinitions(Set<RuleDefinition> ruleDefinitions) {
    this.ruleDefinitions = ruleDefinitions;
  }

  /**
   * 
   * @return all the rule definitions, cached
   */
  public static RuleEngine ruleEngine() {
    RuleEngine ruleEngine = ruleEngineCache.get(Boolean.TRUE);
    
    if (ruleEngine == null) {
    
      synchronized (RuleEngine.class) {
        ruleEngine = ruleEngineCache.get(Boolean.TRUE);
        if (ruleEngine == null) {
          
          GrouperSession grouperSession = GrouperSession.staticGrouperSession().internal_getRootSession();
          ruleEngine = (RuleEngine)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
            
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              Map<AttributeAssign, Set<AttributeAssignValueContainer>> attributeAssignValueContainers 
                = allRulesAttributeAssignValueContainers(null);
            
              RuleEngine newEngine = new RuleEngine();
              Set<RuleDefinition> newDefinitions = newEngine.getRuleDefinitions();
              
              for (Set<AttributeAssignValueContainer> attributeAssignValueContainersSet : 
                  GrouperUtil.nonNull(attributeAssignValueContainers).values()) {
                RuleDefinition ruleDefinition = new RuleDefinition(attributeAssignValueContainersSet);
                
                //dont validate, already validated
                newDefinitions.add(ruleDefinition);
                
              }
              
              newEngine.indexData();
              ruleEngineCache.put(Boolean.TRUE, newEngine);
              return newEngine;
            }
          });
          
        }
      }
      
    }
    
    return ruleEngine;
    
  }

  /** map of checks to sets of relevant rules */
  private Map<RuleCheck, Set<RuleDefinition>> ruleCheckIndex = null;

  
  /**
   * map of checks to sets of relevant rules
   * @return the ruleCheckIndex
   */
  public Map<RuleCheck, Set<RuleDefinition>> getRuleCheckIndex() {
    return this.ruleCheckIndex;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleEngine.class);
  
  /**
   * index the rules so they can be searched quickly
   */
  private void indexData() {
    this.ruleCheckIndex = new HashMap<RuleCheck, Set<RuleDefinition>>();
    
    for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(this.ruleDefinitions)) {
      
      RuleCheck originalRuleCheck = ruleDefinition.getCheck();
      RuleCheck ruleCheck = originalRuleCheck.checkTypeEnum().checkKey(ruleDefinition);
      
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId())
          && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        
        LOG.error("Why are ownerId and ownerName blank for this rule: " + ruleDefinition);
        continue;
      }
      
      Set<RuleDefinition> checkRuleDefinitions = this.ruleCheckIndex.get(ruleCheck);
      
      //if this list isnt there, then make one
      if (checkRuleDefinitions == null) {
        checkRuleDefinitions = new HashSet<RuleDefinition>();
        this.ruleCheckIndex.put(ruleCheck, checkRuleDefinitions);
      }
      
      checkRuleDefinitions.add(ruleDefinition);
      
    }
    
  }
  
  /**
   * pop one and log error if multiple
   * @param ruleDefinitions
   * @return the set
   */
  private static Set<RuleDefinition> listPopOneLogError(Set<RuleDefinition> ruleDefinitions) {
    int length = GrouperUtil.length(ruleDefinitions);
    if (length == 1) {
      return ruleDefinitions;
    }
    if (length == 0) {
      throw new RuntimeException("Why is length 0");
    }
    RuleDefinition ruleDefinition = ruleDefinitions.iterator().next();
    LOG.error("Why is there more than 1? " + length + ", " + ruleDefinition);
    return GrouperUtil.toSet(ruleDefinition);
  }
  
  /**
   * get rule definitions from cache based on name or id
   * @param ruleCheck
   * @return the definitions
   */
  public Set<RuleDefinition> ruleCheckIndexDefinitionsByNameOrIdInFolderPickOneArgOptional(RuleCheck ruleCheck) {
  
    if (!GrouperConfig.getPropertyBoolean("rules.enable", true)) {
      return null;
    }

    //owner name is the stem name
    String ownerName = ruleCheck.getCheckOwnerName();
    
    if (StringUtils.isBlank(ownerName)) {
      throw new RuntimeException("Checking in folder needs an owner name: " + ruleCheck);
    }
    
    Set<RuleDefinition> ruleDefinitions = null;
    
    //see if there is a direct that matches the arg
    RuleCheck tempRuleCheck = new RuleCheck(ruleCheck.getCheckType(), null, ownerName, Stem.Scope.ONE.name(), ruleCheck.getCheckArg0(), null);
    
    ruleDefinitions = this.getRuleCheckIndex().get(tempRuleCheck);
    
    if (GrouperUtil.length(ruleDefinitions) > 0) {
      return listPopOneLogError(ruleDefinitions);
    }
    
    tempRuleCheck.setCheckArg0(null);
    
    //see if there is a direct without a matching arg
    ruleDefinitions = this.getRuleCheckIndex().get(tempRuleCheck);
    
    if (GrouperUtil.length(ruleDefinitions) > 0) {
      return listPopOneLogError(ruleDefinitions);
    }
    
    //we need the stems, 
    List<String> stemNameList = null;
    
    {
      Set<String> stemNameSet = GrouperUtil.findParentStemNames(ownerName);
      
      stemNameSet.add(ownerName);

      stemNameList = new ArrayList<String>(stemNameSet);
      
      Collections.reverse(stemNameList);
    }
    
    //loop through all stems from more specific to less specific
    for (String ancestorStemName : stemNameList) {
      
      tempRuleCheck = new RuleCheck(ruleCheck.getCheckType(), null, ancestorStemName, Stem.Scope.SUB.name(), ruleCheck.getCheckArg0(), null);
      
      ruleDefinitions = this.getRuleCheckIndex().get(tempRuleCheck);
      
      if (GrouperUtil.length(ruleDefinitions) > 0) {
        return listPopOneLogError(ruleDefinitions);
      }
      
      tempRuleCheck.setCheckArg0(null);
      
      //see if there is a direct without a matching arg
      ruleDefinitions = this.getRuleCheckIndex().get(tempRuleCheck);
      
      if (GrouperUtil.length(ruleDefinitions) > 0) {
        return listPopOneLogError(ruleDefinitions);
      }
    }
    //didnt find anything, dont return null
    return new HashSet<RuleDefinition>();

  }

  /**
   * find rules and fire them
   * @param ruleCheckType
   * @param rulesBean
   */
  public static void fireRule(final RuleCheckType ruleCheckType, final RulesBean rulesBean) {

    if (GrouperCheckConfig.inCheckConfig) {
      return;
    }

    if (!GrouperConfig.getPropertyBoolean("rules.enable", true)) {
      return;
    }
    
    final RuleEngine ruleEngine = ruleEngine();
    
    if (ruleEngine == null) {
      throw new NullPointerException("ruleEngine cannot be null");
    }
    
    StringBuilder logData = null;
    boolean shouldLog = LOG.isDebugEnabled();
    try {

      Set<RuleDefinition> ruleDefinitions = ruleCheckType.ruleDefinitions(ruleEngine, rulesBean);

      //see if we should log
      for (final RuleDefinition ruleDefinition : GrouperUtil.nonNull(ruleDefinitions)) {
        shouldLog = shouldLog || ruleDefinition.shouldLog();
      }
      
      if (shouldLog) {
        logData = new StringBuilder();
        logData.append("Rules engine processing rulesBean: " + rulesBean);
      }
      
  
      if (shouldLog) {
        logData.append(", found " + GrouperUtil.length(ruleDefinitions) + " matching rule definitions");
      }
      
      int shouldFireCount = 0;
      
      for (final RuleDefinition ruleDefinition : GrouperUtil.nonNull(ruleDefinitions)) {
        
        boolean shouldLogThisDefinition = LOG.isDebugEnabled() || ruleDefinition.shouldLog();
        final StringBuilder logDataForThisDefinition = shouldLogThisDefinition ? logData : null;
        if (ruleDefinition.getIfCondition().shouldFire(ruleDefinition, ruleEngine, rulesBean, logDataForThisDefinition)) {
          
          shouldFireCount++;
          
          if (shouldLogThisDefinition) {
            logDataForThisDefinition.append(", ruleDefinition should fire: ").append(ruleDefinition.toString());
          }
          
          //act as the act as
          RuleSubjectActAs actAs = ruleDefinition.getActAs();
          Subject actAsSubject = actAs.subject(false);
          if (actAsSubject == null) {
            LOG.error("Cant find subject for rule: " + ruleDefinition);
          } else {
            
            GrouperSession.callbackGrouperSession(GrouperSession.start(actAsSubject, false), new GrouperSessionHandler() {
  
              /**
               * 
               */
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                //we are firing, note this isnt synchronized, so might not be exact, but used for testing, so thats ok
                ruleFirings++;
                ruleDefinition.getThen().fireRule(ruleDefinition, ruleEngine, rulesBean, logDataForThisDefinition);
                return null;
                
              }
            });
          }        
        }
        
      }
      
      if (shouldLog) {
        logData.append(", shouldFire count: " + shouldFireCount);
      }
    } catch (RuntimeException re) {
      if (shouldLog && logData != null) {
        logData.append(", EXCEPTION: ").append(ExceptionUtils.getFullStackTrace(re));
      }
      throw re;
    } finally {
      if (shouldLog && logData != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(logData);
        } else if (LOG.isInfoEnabled()) {
          LOG.info(logData);
        }
      }
    }
  }
  
  /**
   * get all rules from the DB in the form of attribute assignments
   * @param queryOptions 
   * @return the assigns of all rules
   */
  public static Map<AttributeAssign, Set<AttributeAssignValueContainer>> allRulesAttributeAssignValueContainers(QueryOptions queryOptions) {
    
    AttributeDefName ruleTypeDefName = RuleUtils.ruleAttributeDefName();
    
    Map<AttributeAssign, Set<AttributeAssignValueContainer>> result = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByAttributeTypeDefNameId(ruleTypeDefName.getId(), queryOptions);
  
    //List<AttributeAssign> keyList = new ArrayList<AttributeAssign>(result.keySet());
    //List<List<AttributeAssignValueContainer>> valuesList = new ArrayList<List<AttributeAssignValueContainer>>();
    //for (AttributeAssign attributeAssign: keyList) {
    //  valuesList.add(new ArrayList<AttributeAssignValueContainer>(result.get(attributeAssign)));
    //}
    
    return result;
    
  }

  /**
   * validate the rules, and run the daemon stuff in rules
   * @return the number of records changed
   */
  public static int daemon() {
    
    //get all enabled rules
    final Map<AttributeAssign, Set<AttributeAssignValueContainer>> attributeAssignValueContainers 
      = allRulesAttributeAssignValueContainers(new QueryOptions().secondLevelCache(false));
  
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
    
    return (Integer)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        GrouperAttributeAssignValueRulesConfigHook.getThreadLocalInValidateRule().set(Boolean.TRUE);
        int rulesChanged = 0;
        try {
          
          for (Set<AttributeAssignValueContainer> attributeAssignValueContainersSet : 
              GrouperUtil.nonNull(attributeAssignValueContainers).values()) {
            
            RuleDefinition ruleDefinition = null;
            try {
              ruleDefinition = new RuleDefinition(attributeAssignValueContainersSet);
        
              String validReason = ruleDefinition.validate();
              
              if (StringUtils.isBlank(validReason)) {
                validReason = "T";
                
                //run daemon on rule if should
                ruleDefinition.runDaemonOnDefinitionIfShould();
                
              } else {
                validReason = "INVALID: " + validReason;
        
                //throw out invalid rules
                LOG.error("Invalid rule definition: " 
                    + validReason + ", ruleDefinition: " + ruleDefinition);
        
                AttributeAssign typeAttributeAssign = ruleDefinition.getAttributeAssignType();
        
                typeAttributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleValidName(), validReason);
                
                rulesChanged++;
              }
            } catch (Exception e) {
              LOG.error("Error with daemon on rule: " + ruleDefinition, e);
            }
          }
        } finally {
          GrouperAttributeAssignValueRulesConfigHook.getThreadLocalInValidateRule().set(Boolean.FALSE);
        }
        return rulesChanged;
      }
    });
  }

  /**
   * multikey is sourceId and subjectId , default is 2.5 minutes
   */
  private static GrouperCache<MultiKey, Boolean> subjectHasAccessToApi = 
    new GrouperCache<MultiKey, Boolean>("RuleEngine.hasAccessToElApi", 1000, false, 150, 150, false);
  
  /**
   * clear this for testing
   */
  static void clearSubjectHasAccessToElApi() {
    subjectHasAccessToApi.clear();
  }
  
  /**
   * clear this for testing
   */
  public static void clearRuleEngineCache() {
    ruleEngineCache.clear();
  }
  
  /**
   * see if a subejct (e.g. act as subject) has access to the EL api
   * @param subject
   * @return true if has access, flase if not, use the cache
   */
  public static boolean hasAccessToElApi(final Subject subject) {

    final String hasAccessToGroupName = GrouperConfig.getProperty("rules.accessToApiInEl.group");
    
    if (StringUtils.isBlank(hasAccessToGroupName)) {
      return false;
    }
    
    final MultiKey subjectKey = new MultiKey(subject.getSourceId(), subject.getId());
    
    Boolean result = subjectHasAccessToApi.get(subjectKey);
    if (result == null) {
      
      //go to root
      GrouperSession grouperSession = GrouperSession.staticGrouperSession().internal_getRootSession();
      
      result = (Boolean)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          Group hasAccessGroup = GroupFinder.findByName(grouperSession, hasAccessToGroupName, true);
          
          boolean result = hasAccessGroup.hasMember(subject);
          
          //add back to cache
          subjectHasAccessToApi.put(subjectKey, result);
          
          return result;
        }
      });
      
    }

    return result;
  }
  
  
}
