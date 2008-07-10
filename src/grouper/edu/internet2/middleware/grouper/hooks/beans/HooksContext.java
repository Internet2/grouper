/*
 * @author mchyzer
 * $Id: HooksContext.java,v 1.6 2008-07-10 05:55:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSessionException;
import edu.internet2.middleware.grouper.GrouperSessionHandler;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperCache;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * context in which hooks are running
 */
public class HooksContext {
  
  /** global attributes, threadsafe */
  private static Map<String, Object> attributeGlobal = 
    new HashMap<String, Object>();
  
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
   * current acting subject in app (if applicable)
   * @return the subject acting as (or null if not available)
   */
  public Subject getSubjectActAs() {
    return (Subject)this.getAttribute(HOOKS_KEY_SUBJECT_ACT_AS);
  }

  /**
   * get the grouper session from the grouper session threadlocal
   * @return the grouper session (might be null)
   */
  public GrouperSession getGrouperSession() {
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
              group = GroupFinder.findByName(grouperSession, groupName);
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
  
//  /**
//   * return the grouper session, or null if none in context
//   * @return the grouper session
//   */
//  public GrouperSession getGrouperSession() {
//    List<WeakReference<GrouperSession>> sessions = (List<WeakReference<GrouperSession>>)threadLocalAttribute()
//      .get(HooksContext.HOOKS_KEY_GROUPER_SESSION);
//    if (sessions != null) {
//      //get the last non-null one, remove if not there
//      for (int i=sessions.size()-1;i>=0;i--) {
//        WeakReference<GrouperSession> reference = sessions.get(i);
//        GrouperSession session = reference.get();
//        if (session == null) {
//          sessions.remove(i);
//        } else {
//          return session;
//        }
//      }
//    }
//    //cant find
//    return null;
//  }
  
  /**
   * constructor
   * @param theAsynchronous
   * @param synchronousContext 
   */
  public HooksContext(boolean theAsynchronous, HooksContext synchronousContext) {
    this.asynchronous = theAsynchronous;
    if (this.asynchronous) {
      //if its asynchronous, then remove the thread local ones
      //TODO which thread is this happening in, I think it is wrong, dont clear
      threadLocalAttribute().clear();
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
      if (grouperSession != null) {
        this.asynchronousGrouperSessionSubject = grouperSession.getSubject();
      }
      
    }
    if (synchronousContext != null) {
      //carry over the attributes, which are threadsafe
      if (synchronousContext.attributeLocal != null) {
        for (String key : synchronousContext.attributeLocal.keySet()) {
          
          HooksAttribute hooksAttribute = synchronousContext.attributeLocal.get(key);
          if (hooksAttribute.isThreadSafe()) {
            threadLocalAttribute().put(key, hooksAttribute);
          }
        }
      }
      //copy over the threadlocal to the local attributes
      for (String key : threadLocalAttribute().keySet()) {
        
        HooksAttribute hooksAttribute = threadLocalAttribute().get(key);
        if (hooksAttribute.isThreadSafe()) {
          threadLocalAttribute().put(key, hooksAttribute);
        }
      }
    }
  }
  
  /** keep track of grouper session subject if needed */
  private Subject asynchronousGrouperSessionSubject;
  
  /** if we started one, we should stop it */
  private boolean asynchronousGrouperSessionStarted;
  
  /**
   * set a global attribute
   * @param key
   * @param value
   */
  public static void setAttributeGlobal(String key, Object value) {
    attributeGlobal.put(key, value);
  }
  
  /**
   * get the context in which the hooks are running
   * @return the context
   */
  public GrouperContextType getGrouperContextType() {
    return GrouperBuiltinContextType.currentGrouperContext();
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
   * @param threadSafe if this should be set for hooks spawned in new thread
   */
  public static void setAttributeThreadLocal(String key, Object value, boolean threadSafe) {
    if (value == null) {
      threadLocalAttribute().remove(key);
    } else {
      threadLocalAttribute().put(key, new HooksAttribute(threadSafe, value));
    }
  }
  
  /**
   * clear out the threadlocal attributes at a point when everything should be clear
   */
  public static void clearThreadLocal() {
    threadLocalAttribute().clear();
  }

//  /**
//   * keep sessions in a list so they fall off when done
//   * @param grouperSession
//   */
//  public static void removeGrouperSessionThreadLocal(GrouperSession grouperSession) {
//    
//    List<WeakReference<GrouperSession>> sessions = (List<WeakReference<GrouperSession>>)threadLocalAttribute()
//      .get(HooksContext.HOOKS_KEY_GROUPER_SESSION);
//    
//    if (sessions != null) {
//      //lets find and remove this one or removed ones
//      Iterator<WeakReference<GrouperSession>> iterator = sessions.iterator();
//      while (iterator.hasNext()) {
//        WeakReference<GrouperSession> currentReference = iterator.next();
//        
//        //if not there, or the one to remove, then remove
//        GrouperSession currentSession = currentReference.get();
//        if (currentSession == null || currentSession == grouperSession) {
//          iterator.remove();
//        }
//        
//      }
//      
//    }
//  }
  
//  /**
//   * keep sessions in a list so they fall off when done
//   * @param grouperSession
//   */
//  public static void addGrouperSessionThreadLocal(GrouperSession grouperSession) {
//    
//     HooksAttribute hooksAttribute = threadLocalAttribute()
//      .get(HooksContext.HOOKS_KEY_GROUPER_SESSION);
//     
//    List<WeakReference<GrouperSession>> sessions;
//     
//    if (hooksAttribute == null) {
//      sessions = new ArrayList<WeakReference<GrouperSession>>();
//      setAttributeThreadLocal(HooksContext.HOOKS_KEY_GROUPER_SESSION, sessions, false);
//      hooksAttribute = threadLocalAttribute()
//        .get(HooksContext.HOOKS_KEY_GROUPER_SESSION);
//    }
//    
//    sessions = (List<WeakReference<GrouperSession>>)hooksAttribute.getValue();
//    
//    //lets clear out removed ones
//    Iterator<WeakReference<GrouperSession>> iterator = sessions.iterator();
//    while (iterator.hasNext()) {
//      WeakReference<GrouperSession> currentReference = iterator.next();
//      
//      //if not there, remove
//      GrouperSession currentSession = currentReference.get();
//      if (currentSession == null) {
//        iterator.remove();
//      }
//      
//    }
//    
//    //add to end
//    sessions.add(new WeakReference<GrouperSession>(grouperSession));
//  }
  
  /**
   * local attributes just for this context
   */
  private Map<String, HooksAttribute> attributeLocal = new HashMap<String, HooksAttribute>();

//  /**
//   * hooks internal attribute key for grouper session
//   */
//  public static final String HOOKS_KEY_GROUPER_SESSION = "_grouperSession";
  
  /**
   * keys of attributes (all put together, global, threadlocal, local
   * @return the key
   */
  public Set<String> attributeKeySet() {
    Set<String> keySet = new HashSet<String>();
    keySet.addAll(attributeGlobal.keySet());
    //if not asynchronous, then use threadlocals, else copied into local
    if (!this.asynchronous) {
      keySet.addAll(threadLocalAttribute().keySet());
    }
    keySet.addAll(attributeLocal.keySet());
    return keySet;
  }
  
  /**
   * get an attribute 
   * @param key
   * @return the object or null if not found
   */
  public Object getAttribute(String key) {
    HooksAttribute hooksAttribute = attributeLocal.get(key);
    Object value = null;
    
    //dont check thread local if asynchronous 
    if (hooksAttribute == null && !this.asynchronous) {
      hooksAttribute = threadLocalAttribute().get(key);
    }
    if (hooksAttribute == null) {
      value = attributeGlobal.get(key);
      if (value != null) {
        return value;
      }
    }
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
