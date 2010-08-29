package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

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
   
    String ownerId = ruleCheck.getCheckOwnerId();
    String ownerName = ruleCheck.getCheckOwnerName();
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    if (!StringUtils.isBlank(ownerId)) {
      ruleCheck.setCheckOwnerName(null);
      ruleDefinitions.addAll(GrouperUtil.nonNull(this.getRuleCheckIndex().get(ruleCheck)));
      ruleCheck.setCheckOwnerName(ownerName);
    }
    
    if (!StringUtils.isBlank(ownerName)) {
      ruleCheck.setCheckOwnerId(null);
      ruleDefinitions.addAll(GrouperUtil.nonNull(this.getRuleCheckIndex().get(ruleCheck)));
      ruleCheck.setCheckOwnerId(ownerId);
    }
    
    return ruleDefinitions;
  }
  
  /**
   * get rule definitions from cache based on name or id
   * @param ruleCheck
   * @return the definitions
   */
  public Set<RuleDefinition> ruleCheckIndexDefinitionsByNameOrIdInFolder(RuleCheck ruleCheck) {
   
    String ownerName = ruleCheck.getCheckOwnerName();
    
    if (StringUtils.isBlank(ownerName)) {
      throw new RuntimeException("Checking in folder needs an owner name: " + ruleCheck);
    }
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //direct parent
    String parentStemName = GrouperUtil.parentStemNameFromName(ownerName);
    
    RuleCheck tempRuleCheck = new RuleCheck(ruleCheck.getCheckType(), null, parentStemName, Stem.Scope.ONE.name());
    ruleDefinitions.addAll(GrouperUtil.nonNull(this.getRuleCheckIndex().get(tempRuleCheck)));
    
    //direct parent or ancestor
    Set<String> parentStemNames = GrouperUtil.findParentStemNames(ownerName);
    
    for (String ancestorStemName : parentStemNames) {
      
      tempRuleCheck = new RuleCheck(ruleCheck.getCheckType(), null, ancestorStemName, Stem.Scope.SUB.name());
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
  private static RuleEngine ruleEngine() {
    RuleEngine ruleEngine = ruleEngineCache.get(Boolean.TRUE);
    
    if (ruleEngine == null) {
    
      synchronized (RuleEngine.class) {
        ruleEngine = ruleEngineCache.get(Boolean.TRUE);
        if (ruleEngine == null) {
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
          ruleEngine = newEngine;
          ruleEngineCache.put(Boolean.TRUE, ruleEngine);
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
      
      Set<RuleDefinition> checkRuleDefinitions = this.ruleCheckIndex.get(ruleDefinition.getCheck());
      
      //if this list isnt there, then make one
      if (checkRuleDefinitions == null) {
        checkRuleDefinitions = new HashSet<RuleDefinition>();
        this.ruleCheckIndex.put(ruleDefinition.getCheck(), checkRuleDefinitions);
      }
      
      checkRuleDefinitions.add(ruleDefinition);
      
    }
    
  }
  
  /**
   * find rules and fire them
   * @param ruleCheckType
   * @param rulesBean
   */
  public static void fireRule(final RuleCheckType ruleCheckType, final RulesBean rulesBean) {

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
      if (shouldLog) {
        logData.append(", EXCEPTION: ").append(ExceptionUtils.getFullStackTrace(re));
      }
      throw re;
    } finally {
      if (shouldLog) {
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
            
            RuleDefinition ruleDefinition = new RuleDefinition(attributeAssignValueContainersSet);
      
            String validReason = ruleDefinition.validate();
            
            if (StringUtils.isBlank(validReason)) {
              validReason = "T";
            } else {
              validReason = "INVALID: " + validReason;
      
              //throw out invalid rules
              LOG.error("Invalid rule definition: " 
                  + validReason + ", ruleDefinition: " + ruleDefinition);
      
              AttributeAssign typeAttributeAssign = ruleDefinition.getAttributeAssignType();
      
              typeAttributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleValidName(), validReason);
              
              rulesChanged++;
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
   * @param subject
   * @return the cache
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
