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
/*
 * @author mchyzer
 * $Id: HooksContext.java,v 1.9 2009-03-15 06:37:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.misc.GrouperCloneable;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * context in which hooks are running
 */
public class HooksContext {
  
  /** keep a unique id to keep the logs straight */
  private String hookId = GrouperUtil.uniqueId();
  
  /**
   * if this context is asynchronous
   */
  private boolean asynchronous = false;
  
  /**
   * constructor
   */
  public HooksContext() {
    
  }
  
  /**
   * hooks internal attribute key for grouper session
   */
  public static final String HOOKS_KEY_SUBJECT_LOGGED_IN = "_grouperSubjectLoggedIn";
  
  /**
   * hooks internal attribute key for grouper session
   */
  public static final String HOOKS_KEY_SUBJECT_ACT_AS = "_grouperSubjectActAs";
  
  /**
   * current user logged in to app (e.g. UI or WS)
   * @return the subject logged in (or null if not available)
   */
  public Subject getSubjectLoggedIn() {
    return (Subject)this.getAttribute(HOOKS_KEY_SUBJECT_LOGGED_IN);
  }
  
  /**
   * current user in the grouper session or null if none there
   * @return the subject logged in (or null if not available)
   */
  public Subject getSubjectFromGrouperSession() {
    GrouperSession grouperSession = this.grouperSession();
    return grouperSession == null ? null : grouperSession.getSubject();
  }
  
  /**
   * current acting subject in app (if applicable), or just the current
   * subject
   * @return the subject acting as (or null if not available)
   */
  public Subject getSubjectActAs() {
    Subject subjectActAs = (Subject)this.getAttribute(HOOKS_KEY_SUBJECT_ACT_AS);
    return GrouperUtil.defaultIfNull(subjectActAs, this.getSubjectLoggedIn());
  }

  /**
   * get the grouper session from the grouper session threadlocal
   * @return the grouper session (might be null)
   */
  public GrouperSession grouperSession() {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    if (grouperSession == null && !asynchronousGrouperSessionStarted 
        && asynchronousGrouperSessionSubject != null && asynchronous) {
      try {
        asynchronousGrouperSessionStarted = true;
        grouperSession = GrouperSession.start(asynchronousGrouperSessionSubject);
      } catch (SessionException se) {
        throw new RuntimeException(se);
      }
    }
    return grouperSession;
  }
  
  /**
   * if this is an asynchronous hook, the grouper session subject is passed from
   * the other thread to this thread, this is that subject.  note, you can call
   * HooksContext.grouperSession() to start a session instead.
   * 
   * @return the subject
   */
  public Subject getAsynchronousGrouperSessionSubject() {
    return this.asynchronousGrouperSessionSubject;
  }
  
  /**
   * this will be a threadsafe attribute
   * @param subject or null to clear
   */
  public static void assignSubjectLoggedIn(Subject subject) {
    setAttributeThreadLocal(HOOKS_KEY_SUBJECT_LOGGED_IN, subject, true);
  }
  
  /**
   * this will be a threadsafe attribute
   * @param subject or null to clear
   */
  public static void assignSubjectActAs(Subject subject) {
    setAttributeThreadLocal(HOOKS_KEY_SUBJECT_ACT_AS, subject, true);
  }
  
  /**
   * cache group to uuids for 5 minutes
   */
  private static GrouperCache<String, Group> groupNameToGroupCache = 
    new GrouperCache<String, Group>("edu.internet2.middleware.grouper.hooks.beans.HooksContext.groupNameToGroupCache",
        2000, false, 0, 60*5, false);
  
  /**
   * cache group to uuids for 5 minutes
   */
  private static GrouperCache<MultiKey, Boolean> subjectInGroupCache = 
    new GrouperCache<MultiKey, Boolean>("edu.internet2.middleware.grouper.hooks.beans.HooksContext.subjectInGroupCache",
        2000, false, 0, 60*5, false);
  
  /**
   * see if the current act as subject is in a certain group.  Note, this group uuid will be stored
   * in a cache.  Also the result will be stored in a cache, it is not meant to hold too many items
   * @param groupName
   * @return true if in group, false if not in group, or if the subject is not available
   */
  public boolean isSubjectActAsInGroup(final String groupName) {
    final Subject subject = this.getSubjectActAs();
    
    return isSubjectInGroup(groupName, subject);
  }

  /**
   * see if the current subject in grouper session is in a certain group (e.g. for authorization)
   * @param groupName fully qualified group name to check
   * @return true if the subject is in group, false if subject is null or not in group
   */
  public boolean isSubjectFromGrouperSessionInGroup(final String groupName) {
    final Subject subject = this.getSubjectFromGrouperSession();
    
    return isSubjectInGroup(groupName, subject);
  }

  /**
   * see if a subject is in a group
   * @param groupName
   * @param subject
   * @return true if subject is in group
   */
  private boolean isSubjectInGroup(final String groupName, final Subject subject) {
    if (subject == null) {
      return false;
    }
    //see if answer is cached
    final MultiKey multiKey = new MultiKey(groupName, subject.getId(), subject.getSource());
    Boolean result = subjectInGroupCache.get(multiKey);
    if (Boolean.TRUE.equals(result)) {
      return true;
    }
    if (Boolean.FALSE.equals(result)) {
      return false;
    }
    
    Subject rootSubject = SubjectFinder.findRootSubject();
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(rootSubject, false);

      boolean isMember = (Boolean)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          //see if group cached
          Group group = groupNameToGroupCache.get(groupName);
          
          if (group == null) {
            try {
              group = GroupFinder.findByName(grouperSession, groupName, true);
            } catch (GroupNotFoundException gnfe) {
              //wrap in groupersessionexception to get through inverse of control
              throw new GrouperSessionException(gnfe);
            }
            groupNameToGroupCache.put(groupName, group);
          }
          
          boolean isMember = group.hasMember(subject);
          
          //put it in cache either way
          subjectInGroupCache.put(multiKey, isMember);
          return isMember;
        }
        
      });
      
      return isMember;
    } catch (Throwable throwable) {
      //unwrap grouper sesion exception
      if (throwable instanceof GrouperSessionException && throwable.getCause() != null) {
        throwable = throwable.getCause();
      }
      throw new RuntimeException("Problem seeing if subject: " + subject.getId() + ", " 
          + subject.getSource() + ", is in group: " + groupName, throwable);
    } finally { 
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * look at all threadlocal attributes, and extract the names and values of the threadsafe ones.
   * if the values are cloneable, then clone them
   * @return the map, never null
   */
  public Map<String, Object> _internal_threadSafeAttributes(){
    Map<String, Object> result = new HashMap<String, Object>();
    Map<String, HooksAttribute> theThreadLocalAttribute = threadLocalAttribute();
    for (String key : theThreadLocalAttribute.keySet()) {
      
      HooksAttribute hooksAttribute = theThreadLocalAttribute.get(key);
      if (hooksAttribute.isThreadSafe()) {
        Object value = hooksAttribute.getValue();
        //if cloneable, then clone it
        if (value instanceof GrouperCloneable) {
          value = ((GrouperCloneable)value).clone();
        }
        result.put(key, value);
      }
    }
    return result;
  }
  
  
  /**
   * constructor
   * @param theAsynchronous true if this is in a new thread, false if not
   * @param threadSafeAttributes attributes from another thread if applicable 
   * @param theAsynchronousGrouperSessionSubject if asynchronous, pass in who the grouper subject should be
   * @param theAynchronousHookId if we are asynchronous, pass in what the hook id should be
   */
  public HooksContext(boolean theAsynchronous, Map<String, Object> threadSafeAttributes, 
      Subject theAsynchronousGrouperSessionSubject, String theAynchronousHookId) {

    this.asynchronous = theAsynchronous;
    if (this.asynchronous) {
      //if its asynchronous, then remove the thread local ones
      threadLocalAttribute().clear();
      
      this.asynchronousGrouperSessionSubject = theAsynchronousGrouperSessionSubject;
      
      //assign the id if necessary
      if (!StringUtils.isBlank(theAynchronousHookId)) {
        this.hookId = theAynchronousHookId;
      }
      
    }
    if (threadSafeAttributes != null) {
      for (String key : threadSafeAttributes.keySet()) {
        
        HooksAttribute hooksAttribute = new HooksAttribute(true, threadSafeAttributes.get(key));

        //put in map as threadsafe
        threadLocalAttribute().put(key, hooksAttribute);
      }
    }
  }
  
  /** keep track of grouper session subject if needed */
  private Subject asynchronousGrouperSessionSubject;
  
  /** if we started one, we should stop it */
  private boolean asynchronousGrouperSessionStarted;

  /**
   * if application, key in context for response
   */
  public static final String KEY_HTTP_SERVLET_RESPONSE = "HttpServletResponse";

  /**
   * if application, key in context for session
   */
  public static final String KEY_HTTP_SESSION = "HttpSession";

  /**
   * if application, key in context for request
   */
  public static final String KEY_HTTP_SERVLET_REQUEST = "HttpServletRequest";
  
  /**
   * get the context in which the hooks are running, e.g. UI, GSH, etc
   * @return the context
   */
  public GrouperContextType getGrouperContextType() {
    return GrouperContextTypeBuiltIn.currentGrouperContext();
  }

  /**
   * thread local hooks attribute, access from: threadLocalAttribute()
   */
  private static ThreadLocal<Map<String, HooksAttribute>> threadLocalAttribute = new ThreadLocal<Map<String, HooksAttribute>>();

  /**
   * lazy load the threadlocal attribute
   * @return the attribute map
   */
  private static Map<String, HooksAttribute> threadLocalAttribute() {
    Map<String, HooksAttribute> theMap = threadLocalAttribute.get();
    if (theMap == null) {
      theMap = new HashMap<String, HooksAttribute>();
      threadLocalAttribute.set(theMap);
    }
    return theMap;
    
  }
  
  /**
   * set a threadlocal attribute
   * @param key
   * @param value
   * @param okToCopyToNewThread if this should be set for hooks spawned in new thread
   */
  public static void setAttributeThreadLocal(String key, Object value, boolean okToCopyToNewThread) {
    if (value == null) {
      threadLocalAttribute().remove(key);
    } else {
      threadLocalAttribute().put(key, new HooksAttribute(okToCopyToNewThread, value));
    }
  }
  
  /**
   * clear out the threadlocal attributes at a point when everything should be clear
   */
  public static void clearThreadLocal() {
    threadLocalAttribute().clear();
  }

  /**
   * keys of attributes (all put together, global, threadlocal, local
   * @return the key
   */
  public Set<String> attributeKeySet() {
    Set<String> keySet = new HashSet<String>();

    keySet.addAll(threadLocalAttribute().keySet());

    return keySet;
  }
  
  /**
   * get an attribute 
   * @param key
   * @return the object or null if not found
   */
  public Object getAttribute(String key) {
    HooksAttribute hooksAttribute = threadLocalAttribute().get(key);

    if (hooksAttribute != null) {
      return hooksAttribute.getValue();
    }
    return null;
  }

  
  /**
   * if this context is asynchronous
   * @return the asynchronous
   */
  public boolean isAsynchronous() {
    return this.asynchronous;
  }

  
  /**
   * keep track of grouper session subject if needed
   * @return the asynchronousGrouperSessionSubject
   */
  public Subject _internal_getAsynchronousGrouperSessionSubject() {
    return this.asynchronousGrouperSessionSubject;
  }

  
  /**
   * if we started one, we should stop it
   * @return the asynchronousGrouperSessionStarted
   */
  public boolean _internal_isAsynchronousGrouperSessionStarted() {
    return this.asynchronousGrouperSessionStarted;
  }

  
  /**
   * keep a unique id to keep the logs straight
   * @return the hookId
   */
  public String getHookId() {
    return this.hookId;
  }
  
}
