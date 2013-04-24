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

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * definition for the subject act as for a rule
 * @author mchyzer
 *
 */
public class RuleSubjectActAs {

  /**
   * 
   * @param subjectId
   * @param sourceId
   * @param subjectIdentifier
   */
  public RuleSubjectActAs(String subjectId, String sourceId, String subjectIdentifier) {
    super();
    this.subjectId = subjectId;
    this.sourceId = sourceId;
    this.subjectIdentifier = subjectIdentifier;
  }

  /**
   * 
   */
  public RuleSubjectActAs() {

  }
  
  /** subject id to act as */
  private String subjectId;
  
  /** source id to act as */
  private String sourceId;
  
  /** subject identifier to act as */
  private String subjectIdentifier;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleSubjectActAs.class);

  /**
   * subject id to act as
   * @return subject id to act as
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subject id to act as
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * source id to act as
   * @return source id to act as
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * source id to act as
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /**
   * subject identifier to act as
   * @return subject id to act as
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }

  /**
   * subject identifier to act as
   * @param subjectIdentifier1
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    toStringHelper(result);
    return result.toString();
  }
  
  /**
   * 
   * @param result
   */
  void toStringHelper(StringBuilder result) {
    if (!StringUtils.isBlank(this.sourceId)) {
      result.append("actAsSourceId: ").append(this.sourceId).append(", ");
    }
    if (!StringUtils.isBlank(this.subjectId)) {
      result.append("actAsSubjectId: ").append(this.subjectId).append(", ");
    }
    if (!StringUtils.isBlank(this.subjectIdentifier)) {
      result.append("actAsSubjectIdentifier: ").append(this.subjectIdentifier).append(", ");
    }
  }
  
  /**
   * resolve the subject
   * @param exceptionIfNotFound true if exception if not found
   * @return the subject
   */
  public Subject subject(boolean exceptionIfNotFound) {
    return SubjectFinder.findByOptionalArgs(this.sourceId, this.subjectId, this.subjectId, exceptionIfNotFound);
  }
  
  /** act as thread local since it runs as GrouperSystem */
  private static ThreadLocal<Subject> actAsThreadLocal = new ThreadLocal<Subject>();
  
  /**
   * clear act as thread local
   */
  public static void actAsThreadLocalClear() {
    actAsThreadLocal.remove();
  }
  
  /**
   * clear act as thread local
   */
  public static void actAsThreadLocalAssign(Subject subject) {
    actAsThreadLocal.set(subject);
  }
  
  /**
   * validate this 
   * @param ruleDefinition 
   * @return error or null if ok
   */
  public String validate(RuleDefinition ruleDefinition) {
    if (StringUtils.isBlank(this.subjectId) ==  StringUtils.isBlank(this.subjectIdentifier)) {
      return "Enter one and only one of actAsSubjectId and actAsSubjectIdentifier!";
    }
    //lets see what the subject is
    Subject subject = this.subject(false);
    if (subject == null) {
      return "Cant find subject: " + this;
    }
    
    //make sure can act as
    Subject currentSubject = actAsThreadLocal.get();
    if (currentSubject == null) {
      currentSubject = GrouperSession.staticGrouperSession().getSubject();
    }
    
    if (!allowedToActAs(ruleDefinition, currentSubject, subject)) {
      return "Subject: " 
      + GrouperUtil.subjectToString(currentSubject)
      + " cannot act as subject: " + GrouperUtil.subjectToString(subject) + " based on grouper.properties: "
      + " rules.act.as.group";
    }

    return null;
    
  }
  
  /**
   * @return act as cache minutes
   */
  private static int actAsCacheMinutes() {
    int actAsTimeoutMinutes = GrouperConfig.getPropertyInt(
        "rules.act.as.cache.minutes", 30);
    return actAsTimeoutMinutes;
  }


  /** cache the actAs */
  private static GrouperCache<MultiKey, Boolean> subjectAllowedCache = null;
  
  /**
   * get the subjectAllowedCache, and init if not initted
   * @return the subjectAllowedCache
   */
  private static GrouperCache<MultiKey, Boolean> subjectAllowedCache() {
    if (subjectAllowedCache == null) {
      
      subjectAllowedCache = new GrouperCache<MultiKey, Boolean>(
          RuleSubjectActAs.class.getName() + "subjectAllowedCache", 1000, false, 
          60*60*24, actAsCacheMinutes()*60, false);
  
    }
    return subjectAllowedCache;
  }
  
  /** when packing things in a single param, this is the separator */
  public static final String ACT_AS_SEPARATOR = "::::";


  /**
   * see if a subject can act as another subject
   * @param ruleDefinition 
   * @param subject
   * @param subjectToActAs
   * @return true if a subject can act as another subject
   */
  public static boolean allowedToActAs(RuleDefinition ruleDefinition, final Subject subject, final Subject subjectToActAs) {

    if (subject == null || subjectToActAs == null) {
      throw new RuntimeException("Need to pass in subject and subjectToActAs");
    }
  
    //if they equal each other, its ok
    if (SubjectHelper.eq(subject, subjectToActAs)) {
      return true;
    }

    //if we are wheel or GrouperSystem, thats good.
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    //lets see if in cache    
    //cache key to get or set if a user can act as another

    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession();
    GrouperSession internalRootSession = staticGrouperSession.internal_getRootSession();
    boolean allowed = (Boolean)GrouperSession.callbackGrouperSession(internalRootSession, new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        //make sure allowed
        String actAsGroupName = GrouperConfig.getProperty("rules.act.as.group");
        
        if (StringUtils.isBlank(actAsGroupName)) {
          return false;
        }

        MultiKey cacheKey = new MultiKey(subject.getId(), subject.getSourceId(), 
            subjectToActAs.getId(), subjectToActAs.getSourceId());
      
        Boolean inCache = null;
      
        if (actAsCacheMinutes() > 0) {
          inCache = subjectAllowedCache().get(cacheKey);
        } else {
          inCache = false;
        }
      
        if (inCache != null && Boolean.TRUE.equals(inCache)) {
          //if in cache and true, then allow
          return true;
        }

        //first separate by comma
        String[] groupEntries = GrouperUtil.splitTrim(actAsGroupName, ",");
    
        //see if all throw exceptions
        int countNoExceptions = 0;
    
        //we could also cache which entries the user is in...  not sure how many entries will be here
        for (String groupEntry : groupEntries) {
    
          //each entry should be failsafe
          try {
            //now see if it is a multi input
            if (StringUtils.contains(groupEntry, ACT_AS_SEPARATOR)) {
    
              //it is the group the user is in, and the group the act as has to be in
              String[] groupEntryArray = GrouperUtil.splitTrim(groupEntry,
                  ACT_AS_SEPARATOR);
              String userMustBeInGroupName = groupEntryArray[0];
              String actAsMustBeInGroupName = groupEntryArray[1];
    
              Group userMustBeInGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(),
                  userMustBeInGroupName, true);
              Group actAsMustBeInGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(),
                  actAsMustBeInGroupName, true);
    
              if (userMustBeInGroup.hasMember(subject)
                  && actAsMustBeInGroup.hasMember(subjectToActAs)) {
                //its ok, lets add to cache
                subjectAllowedCache().put(cacheKey, Boolean.TRUE);
                return true;
              }
    
            } else {
              //else this is a straightforward rule where the logged in user just has to be in a group and
              //can act as anyone
              Group actAsGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupEntry, true);
    
              // if the logged in user is a member of the actAs group, then allow
              // the actAs
              if (actAsGroup.hasMember(subject)) {
                //its ok, lets add to cache
                subjectAllowedCache().put(cacheKey, Boolean.TRUE);
                // this is the subject the rule wants to use
                return true;
              }
            }
            countNoExceptions++;
          } catch (Exception e) {
            //just log and dont act since other entries could be fine
            LOG.error("Problem with groupEntry: " + groupEntry + ", subject: "
                + subject + ", actAsSubject: " + subjectToActAs, e);
          }

        }

        if (countNoExceptions == 0) {
          return false;
        }
        // if not an effective member
        LOG.error(
            "A rule is specifying an actAsUser, but the groups specified in "
                + " rules.act.as.group in the grouper.properties "
                + " does not have a valid rule for member: '" + GrouperUtil.subjectToString(subject)
                + "', and actAs: '" + GrouperUtil.subjectToString(subjectToActAs) + "'");
        
        return false;
      }
    });
    
    if (allowed) {
      return true;
    }
    
    if (GrouperConfig.getPropertyBoolean("rules.allowActAsGrouperSystemForInheritedStemPrivileges", true)) {

      try {
        RuleCheckType ruleCheckType = ruleDefinition.getCheck().checkTypeEnum();
        
        if (ruleCheckType == RuleCheckType.groupCreate || ruleCheckType == RuleCheckType.stemCreate
                || ruleCheckType == RuleCheckType.attributeDefCreate) {
  
          //check owner name is null and attribute owner name is not null
          Stem ownerStem = ruleDefinition.getAttributeAssignType().getOwnerStem();
          
          //see if this is a special case
          if (SubjectHelper.eq(SubjectFinder.findRootSubject(), subjectToActAs) && ownerStem != null ) {
            if (((StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerId())
                && StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerName()))
                || StringUtils.equals(ruleDefinition.getCheck().getCheckOwnerName(), ownerStem.getName()))) {
              
              RuleThenEnum ruleThenEnum = ruleDefinition.getThen().thenEnum();
              
              if (ruleThenEnum == RuleThenEnum.assignGroupPrivilegeToGroupId
                  || ruleThenEnum == RuleThenEnum.assignStemPrivilegeToStemId
                  || ruleThenEnum == RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId) {
                //everything ok, allowed to act as grouper system
                return true;
              }
            }
          }
          
          
        }
      } catch (Exception e) {
        //ignore, something wrong
        LOG.debug("error figuring out act as grouper system for inherited stem privileges", e);
      }
      
    }
    return false;

    
  }

//  /**
//   * <pre>
//   * pattern.  test: ${ruleElUtils.assignGroupPrivilege(groupId, 'g:gsa', null, 'stem1:a', 'read,update')}
//   * regex: ^\$\{\s*ruleElUtils\.assign(Group|Stem)Privilege\(\s*(group|stem)Id\s*,([^\)]+)\s*\)\s*\}\s*$
//   * ^               start
//   * \$\{            dollar, curly
//   * \s*             optional whitespace
//   * ruleElUtils\.   ruleElUtils and dot
//   * assign          assign
//   * (Group|Stem)    Group or Stem
//   * Privilege       Privilege
//   * \(\s*           paren and optional whitespace
//   * (group|stem)Id  groupId or stemId
//   * \s*,            optional whitespace and comma
//   * ([^\)]+)        a bunch of stuff not a paren, capture this
//   * \s*\)\s*\}      optional space, then paren, optional space, then curly
//   * \s*$            optional space then done
//   * </pre>
//   */
//  public static Pattern actAsGrouperSystemStemInheritPattern = 
//    Pattern.compile("^\\$\\{\\s*ruleElUtils\\.assign(Group|Stem|AttributeDef)Privilege\\(\\s*(group|stem|attributeDef)Id\\s*,([^\\)]+)\\s*\\)\\s*\\}\\s*$");
//  
//  /**
//   * <pre>
//   * pattern: ^\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^,]+)\s*,\s*(.+)\s*$
//   * matches: "g:gsa" , null, 'stem1:a', 'read,update'
//   * 
//   * ^\s*([^,]+)\s*,  start, optional spaces, stuff not a comma, optional whitespace, comma
//   * \s*([^,]+)\s*,   optional spaces, stuff not a comma, optional whitespace, comma
//   * \s*([^,]+)\s*,   optional spaces, stuff not a comma, optional whitespace, comma
//   * \s*(.+)\s*$      optional spaces, stuff not a comma, optional whitespace, end
//   * </pre>
//   */
//  public static Pattern actAsGrouperSystemStemInheritArgsPattern = 
//    Pattern.compile("^\\s*([^,]+)\\s*,\\s*([^,]+)\\s*,\\s*([^,]+)\\s*,\\s*(.+)\\s*$");
  

  
}
