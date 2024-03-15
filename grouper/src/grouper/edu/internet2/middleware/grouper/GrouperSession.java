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
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.privs.AccessAdapter;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.AccessResolverFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefResolver;
import edu.internet2.middleware.grouper.privs.AttributeDefResolverFactory;
import edu.internet2.middleware.grouper.privs.GrouperAttributeDefAdapter;
import edu.internet2.middleware.grouper.privs.NamingAdapter;
import edu.internet2.middleware.grouper.privs.NamingResolver;
import edu.internet2.middleware.grouper.privs.NamingResolverFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;


/** 
 * Context for interacting with the Grouper API and Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.101 2009-11-05 20:06:42 isgwb Exp $
 */
@SuppressWarnings("serial")
public class GrouperSession implements Serializable {

  /**
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    stopQuietly(this);
  }

  /**
   * if we should take into consideration that we are a wheel member (or act as self if false)
   */
  private boolean considerIfWheelMember = true;

  /**
   * if we should take into consideration that we are a wheel member (or act as self if false)
   * @return if considering if wheel member
   */
  public boolean isConsiderIfWheelMember() {
    return this.considerIfWheelMember;
  }

  /**
   * if we should take into consideration that we are a wheel member (or act as self if false)
   * @param considerIfWheelMember1
   */
  public void setConsiderIfWheelMember(boolean considerIfWheelMember1) {
    this.considerIfWheelMember = considerIfWheelMember1;
  }

  /**
   * throw illegal state if stopped
   */
  private void internal_ThrowIllegalStateIfStopped() {
    if (this.subject == null) {
      throw new IllegalStateException("Grouper session subject is null, probably since it is stopped.  " +
      		"Dont use it anymore, start another");
    }
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperSession.class);

  /**
   * store the current grouper session here so it is not cleaned up if not assigned to a variable
   */
  private static ThreadLocal<GrouperSession> currentSession = new ThreadLocal<GrouperSession>();
  
  public static GrouperSession internal_testingGetCurrentSession() {
    return currentSession.get();
  }
  
  /**
   * store the grouper connection in thread local so other classes can get it.
   * this is only for inverse of control.  This has priority over the 
   * static session set from start()
   */
  private static ThreadLocal<List<WeakReference<GrouperSession>>> staticSessions = new ThreadLocal<List<WeakReference<GrouperSession>>>();

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone @JsonIgnore
  private transient AccessResolver  accessResolver;

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone @JsonIgnore
  private transient AttributeDefResolver  attributeDefResolver;

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Member          cachedMember;

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone @JsonIgnore
  private transient NamingResolver  namingResolver;

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private transient GrouperSession  rootSession;

  /** have a link back so the parent session doesnt get garbage collected */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private transient GrouperSession  rootSessionParent;

  /** */
  private String          memberUUID;


  /** */
  private long            startTimeLong;

  /** */
  @GrouperIgnoreDbVersion
  private Subject         subject;

  /** */
  private String          uuid;

  private boolean stopped;


  /**
   * Default constructor.  Dont call this, use the factory: start(Subject)
   * <p/>
   * @since   1.2.0
   */
  public GrouperSession() {
    this.cachedMember = null;
    this.rootSession  = null;
  } 

  /**
   * stop a session quietly
   * @param session
   */
  public static void stopQuietly(GrouperSession session) {
    if (session != null) {
      try {
        session.stop();
      } catch (Exception e) {
        LOG.error(e);
      }
    }

  }
  
  /**
   * start a session based on a sourceId and subjectId
   * @param sourceId if null search all sources
   * @param subjectId
   * @return return the GrouperSession
   */
  public static GrouperSession startBySubjectIdAndSource(final String subjectId, final String sourceId) {
    
    return startBySubjectIdAndSource(subjectId, sourceId, true);
    
  }
  
  /**
   * start a session based on a sourceId and subjectId
   * @param sourceId if null search all sources
   * @param subjectId
   * @param addToThreadLocal true if it should be in threadlocal, false if not
   * @return return the GrouperSession
   */
  public static GrouperSession startBySubjectIdAndSource(final String subjectId, final String sourceId, 
      boolean addToThreadLocal) {

    Subject subject = null;
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = false;
    
    try {
      
      if (grouperSession == null) {
        grouperSession = GrouperSession.startRootSession(false);
        startedSession = true;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
        grouperSession = grouperSession.internal_getRootSession();
      }
      
      subject = (Subject)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        /**
         * 
         */
        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {

          if (StringUtils.isBlank(sourceId)) {
            return SubjectFinder.findById(subjectId, true);
          }
          return SubjectFinder.findByIdAndSource(subjectId, sourceId, true);
          
        }
        
      });
      
      
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
    
    return GrouperSession.start(subject, addToThreadLocal);
    
  }
  
  /**
   * start a session based on a sourceId and subjectId
   * @param sourceId if null search all sources
   * @param subjectIdentifier
   * @return return the GrouperSession
   */
  public static GrouperSession startBySubjectIdentifierAndSource(final String subjectIdentifier, final String sourceId) {
    return startBySubjectIdentifierAndSource(subjectIdentifier, sourceId, true);
  }
  
  /**
   * start a session based on a sourceId and subjectId
   * @param subjectIdentifier
   * @param sourceId if null search all sources
   * @param addToThreadLocal 
   * @return return the GrouperSession
   */
  public static GrouperSession startBySubjectIdentifierAndSource(final String subjectIdentifier, final String sourceId, boolean addToThreadLocal) {
    Subject subject = null;
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = false;
    
    try {
      
      if (grouperSession == null) {
        grouperSession = GrouperSession.startRootSession(false);
        startedSession = true;
      }
      
      if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
        grouperSession = grouperSession.internal_getRootSession();
      }
      
      subject = (Subject)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        /**
         * 
         */
        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {

          if (StringUtils.isBlank(sourceId)) {
            return SubjectFinder.findByIdentifier(subjectIdentifier, true);
          }
          return SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId, true);
          
        }
        
      });
      
      
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
    
    return GrouperSession.start(subject, addToThreadLocal);
    
  }
  
  /**
   * Start a session for interacting with the Grouper API.
   * This adds the session to the threadlocal.    This has 
   * threadlocal implications, so start and stop these hierarchically,
   * do not alternate.  If you need to, use the callback inverse of control.
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.subject);
   * </pre>
   * @param   subject   Start session as this {@link Subject}.
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSession start(Subject subject) 
    throws SessionException {
    
    return start(subject, true);
  }

  /**
   * Start a session for interacting with the Grouper API.
   * This adds the session to the threadlocal.    This has 
   * threadlocal implications, so start and stop these hierarchically,
   * do not alternate.  If you need to, use the callback inverse of control.
   * 
   * This will not start a session if it is already started.
   * If it is started as a different user, it will start
   * 
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.subject);
   * </pre>
   * @param   subject   Start session as this {@link Subject}.
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSessionResult startIfNotStarted(Subject subject) 
    throws SessionException {
    
    if (subject == null) {
      throw new NullPointerException("subject is null");
    }
    
    GrouperSessionResult grouperSessionResult = new GrouperSessionResult();
    
    GrouperSession grouperSession = staticGrouperSession(false);
    
    //if there is a session and it is started and same user, use that
    if (grouperSession != null) {
      if (SubjectHelper.eq(subject, grouperSession.getSubject())) {
        grouperSessionResult.setCreated(false);
        grouperSessionResult.setGrouperSession(grouperSession);
        return grouperSessionResult;
      }
    }

    grouperSession = start(subject, true);
    grouperSessionResult.setCreated(true);
    grouperSessionResult.setGrouperSession(grouperSession);
    return grouperSessionResult;
    
  }

  /**
   * Start a session for interacting with the Grouper API.
   * This adds the session to the threadlocal.    This has 
   * threadlocal implications, so start and stop these hierarchically,
   * do not alternate.  If you need to, use the callback inverse of control.
   * This uses 
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.start(subject);
   * </pre>
   * @param addToThreadLocal true to add this to the grouper session
   * threadlocal which replaces the current one
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSession startRootSession(boolean addToThreadLocal) throws SessionException {
    
    return start(SubjectFinder.findRootSubject(), addToThreadLocal);
  }

  /**
   * Start a session for interacting with the Grouper API.
   * This adds the session to the threadlocal.    This has 
   * threadlocal implications, so start and stop these hierarchically,
   * do not alternate.  If you need to, use the callback inverse of control.
   * This uses 
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.start(subject);
   * </pre>
   * @param addToThreadLocal true to add this to the grouper session
   * threadlocal which replaces the current one
   * @param addToCurrentSession if the current threadlocal session is set so the session is not garbage collected
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSession startRootSession(boolean addToThreadLocal, boolean addToCurrentSession) throws SessionException {
    
    return start(SubjectFinder.findRootSubject(), addToThreadLocal, addToCurrentSession);
  }

  /**
   * Start a session for interacting with the Grouper API.
   * This adds the session to the threadlocal.    This has 
   * threadlocal implications, so start and stop these hierarchically,
   * do not alternate.  If you need to, use the callback inverse of control.
   * This uses 
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.start(subject);
   * </pre>
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSession startRootSession()
    throws SessionException {
    return startRootSession(true);
  }

  /**
   * grouper system member uuid
   */
  private static final String GROUPER_SYSTEM_MEMBER_UUID = "41b11bed121c4248bdaa8866b981a5b3";
  
  /**
   * Start a session for interacting with the Grouper API.  This has 
   * threadlocal implications, so start and stop these hierarchically,
   * do not alternate.  If you need to, use the callback inverse of control.
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.start(subject);
   * </pre>
   * @param   subject   Start session as this {@link Subject}.
   * @param addToThreadLocal true to add this to the grouper session
   * threadlocal which replaces the current one.  Though if in the context of a callback,
   * the callback has precedence, and you should use an inner callback to preempt it (callbackGrouperSession)
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSession start(Subject subject, boolean addToThreadLocal) 
    throws SessionException
  {
    return start(subject, addToThreadLocal, addToThreadLocal);
  }
  
  /**
   * Start a session for interacting with the Grouper API.  This has 
   * threadlocal implications, so start and stop these hierarchically,
   * do not alternate.  If you need to, use the callback inverse of control.
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.start(subject);
   * </pre>
   * @param   subject   Start session as this {@link Subject}.
   * @param addToThreadLocal true to add this to the grouper session
   * threadlocal which replaces the current one.  Though if in the context of a callback,
   * the callback has precedence, and you should use an inner callback to preempt it (callbackGrouperSession)
   * @param addToCurrentSession if the current threadlocal session is set so the session is not garbage collected
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSession start(Subject subject, boolean addToThreadLocal, boolean addToCurrentSession) 
    throws SessionException
  {
    if (subject == null) {
      String idLog = "(subject is null)";
      String msg = E.S_START + idLog;
      LOG.fatal(msg);
      throw new SessionException(msg);
    }
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

    if (LOG.isDebugEnabled()) {
      debugMap.put("method", "start(subject,threadLocal)");
      debugMap.put("subjectId", subject.getId());
      debugMap.put("threadLocal", addToThreadLocal);
    }
    GrouperSession s = null;
    try {
      StopWatch sw = new StopWatch();
      sw.start();
      
      s   =  new GrouperSession();

      s.setSubject(subject);
      s.getMember();
      if (addToCurrentSession) {
        currentSession.set(s);
      }
      if (LOG.isDebugEnabled()) {
        debugMap.put("hash", s.hashCode());
      }
        s.setStartTimeLong( new Date().getTime() );
        s.setUuid( GrouperUuid.getUuid() );
      
      sw.stop();
      if (LOG.isInfoEnabled()) {
        LOG.info("[" + s.toString() + "] " + M.S_START + " (" + sw.getTime() + "ms)");
      }

      if (addToThreadLocal) {
        //add to threadlocal
        addStaticHibernateSession(s, true);
      }
    } finally {
      if (LOG.isDebugEnabled()) {
        logAddThreadLocal(debugMap, "");
        LOG.debug("Stack: " + GrouperUtil.stack());
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    return s;
  } 

  /**
   * add thread local info to debug map
   * @param debugMap
   * @param prefix for log message if multiple in one map
   */
  private static void logAddThreadLocal(Map<String, Object> debugMap, String prefix) {
    if (LOG.isDebugEnabled()) {
      clearSessionNulls();
      List<WeakReference<GrouperSession>> staticGrouperSessions = staticSessions.get();
      if (GrouperUtil.length(staticGrouperSessions) == 0) {
        debugMap.put(prefix + "staticSessions", "0");
      } else {
        int i=0;
        for (WeakReference<GrouperSession> grouperSessionReference : staticGrouperSessions) {
          GrouperSession grouperSession = grouperSessionReference.get();
          Subject subject = grouperSession == null ? null : grouperSession.getSubject();
          if (grouperSession == null || subject == null) {
            debugMap.put(prefix + "staticSessions_" + i, "null");
          } else {
            debugMap.put(prefix + "staticSessions_" + i, subject.getId() + "_" + grouperSession.hashCode());
          }
          i++;
        }
      }
    }
  }
  
  /**
   * @param s 
   * @throws  IllegalStateException
   * @since   1.2.0
   */
  public static void validate(GrouperSession s) 
    throws  IllegalStateException
  {
    NotNullValidator v = NotNullValidator.validate(s);
    if (v.isInvalid()) {
      throw new IllegalStateException(E.SV_O);
    }
    s.validate();
  } // public static void validate(s)


  // PUBLIC INSTANCE METHODS //

  /**
   * Get name of class implenting {@link AccessAdapter} privilege interface.
   * <pre class="eg">
   * String klass = s.getAccessClass();
   * </pre>
   * @return access class
   */
  public String getAccessClass() {
    return GrouperAccessAdapter.class.getName(); 
  } 

  /**
   * Get name of class implenting {@link AccessAdapter} privilege interface.
   * <pre class="eg">
   * String klass = s.getAccessClass();
   * </pre>
   * @return access class
   */
  public String getAttributeDefClass() {
    return GrouperAttributeDefAdapter.class.getName();
  } 

  /**
   * @return  <code>AccessResolver</code> used by this session.
   * @since   1.2.1
   */
  public AccessResolver getAccessResolver() {
    this.internal_ThrowIllegalStateIfStopped();
    if (this.accessResolver == null) {
      this.accessResolver = AccessResolverFactory.getInstance(this);
    }
    return this.accessResolver;
  }

   /**
   * Get the {@link Member} associated with this API session.
   * <pre class="eg">
   * Member m = s.getMember(); 
   * </pre>
   * <p>
   * As of 1.2.0, this method throws an {@link IllegalStateException} instead of
   * a {@link NullPointerException} when the member cannot be retrieved.
   * </p>
   * @return  A {@link Member} object.
   * @throws  IllegalStateException if {@link Member} cannot be returned.
   */
  public Member getMember() 
    throws  IllegalStateException
  {
    this.internal_ThrowIllegalStateIfStopped();
    if ( this.cachedMember != null ) {
      return this.cachedMember;
    }
    
    //  this will create the member if it doesn't already exist
    if (GrouperStartup.isFinishedStartupSuccessfully()) {
      if (InternalSourceAdapter.instance().rootSubject(subject)) {
        // if root, then have a default uuid
        this.cachedMember   = MemberFinder.internal_findBySubject(subject, GROUPER_SYSTEM_MEMBER_UUID, true);
      } else {
        
        try {
          this.cachedMember   = MemberFinder.internal_findBySubject(subject, null, true);
        }
        catch (MemberNotFoundException eShouldNeverHappen) {
          throw new IllegalStateException( 
            "this should never happen: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen
          );
        }
      }
    } else {
      //try to get it
      try {
        this.cachedMember   = MemberFinder.internal_findBySubject(subject, null, true);
      } catch (Exception e) {
        // ignore, grouper hasnt started yet, so ignore
        LOG.debug("error finding subject: " + SubjectHelper.getPretty(this.subject), e);
      }
      if (this.cachedMember == null && InternalSourceAdapter.instance().rootSubject(subject)) {
        // if we havent started yet, hard code this...
        this.cachedMember   = new Member();
        this.cachedMember.setSubjectId(subject.getId());
        this.cachedMember.setSubjectSourceId(subject.getSourceId());
        this.cachedMember.setUuid(GROUPER_SYSTEM_MEMBER_UUID);
      }
    }
    
    
    return this.cachedMember;
  } 

  /**
   * Get name of class implenting {@link NamingAdapter} privilege interface.
   * <pre class="eg">
   * String klass = s.getNamingClass();
   * </pre>
   * @return naming class
   */
  public String getNamingClass() {
    return GrouperNamingAdapter.class.getName(); 
  } 

  /**
   * @return  <code>NamingResolver</code> used by this session.
   * @since   1.2.1
   */
  public NamingResolver getNamingResolver() {
    if (this.namingResolver == null) {
      this.namingResolver = NamingResolverFactory.getInstance(this);
    }
    return this.namingResolver;
  }

  /**
   * Get this session's id.
   * <pre class="eg">
   * String id = s.internal_getSessionId();
   * </pre>
   * @return  The session id.
   */
  public String getSessionId() {
    return this.getUuid();
  } // public String getSessionId()

  /**
   * Get this session's start time.
   * <pre class="eg">
   * Date startTime = s.getStartTime();
   * </pre>
   * @return  This session's start time.
   */
  public Date getStartTime() {
    this.internal_ThrowIllegalStateIfStopped();
    return new Date( this.getStartTimeLong() );
  } // public Date getStartTime()

  /**
   * Get the {@link Subject} associated with this API session.
   * <pre class="eg">
   * Subject subj = s.getSubject(); 
   * </pre>
   * @return  A {@link Subject} object.
   * @throws  GrouperException
   */
  public Subject getSubject() 
    throws  GrouperException
  {
    this.internal_ThrowIllegalStateIfStopped();
    return this.subject;
  } // public Subject getSubject()

  /**
   * Get the {@link Subject} associated with this API session.
   * <pre class="eg">
   * Subject subj = s.getSubject(); 
   * </pre>
   * @return  A {@link Subject} object.
   * @throws  GrouperException
   */
  public Subject getSubjectDb() 
    throws  GrouperException
  {
    return this.subject;
  } // public Subject getSubject()

  /**
   * Stop this API session.
   * <pre class="eg">
   * s.stop();
   * </pre>
   * @throws SessionException 
   */
  public void stop()  throws  SessionException
  {
    if (this.stopped) {
      return;
    }
    this.stopped = true;
    
    if (this == currentSession.get()) {
      currentSession.remove();

    }
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    if (LOG.isDebugEnabled()) {
      debugMap.put("method", "stop()");
      debugMap.put("hash", this.hashCode());
      if (this.subject == null) {
        debugMap.put("subject", "null");
      } else {
        debugMap.put("subject", this.subject.getId());
      }
    }
    try {
      //remove from threadlocal if this is the one on threadlocal (might not be due
      //to nesting)
      removeStaticGrouperSession(this);
      
      if (this.accessResolver != null) {
        this.accessResolver.stop();
      }
      if (this.attributeDefResolver != null) {
        this.attributeDefResolver.stop();
      }
      if (this.namingResolver != null) {
        this.namingResolver.stop();
      }
      
      //stop the root
      if (this.rootSession != null && this.rootSession != this) {
        this.rootSession.stop();
        this.rootSession.rootSessionParent = null;
      }
      
      //set some fields to null
      this.subject = null;
      this.accessResolver = null;
      this.attributeDefResolver = null;
      this.cachedMember = null;
      this.memberUUID = null;
      this.namingResolver = null;
      this.rootSession = null;
      this.uuid = null;
    } finally {
      if (LOG.isDebugEnabled()) {
        logAddThreadLocal(debugMap, "");
        LOG.debug("Stack: " + GrouperUtil.stack());
        LOG.debug(debugMap);
      }
    }
  } 

  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append( "session_id",   this.getUuid()                                        )
      .append( "subject_id",   Quote.single( this.getSubject().getId() )             )
      .append( "subject_type", Quote.single( this.getSubject().getType().getName() ) )
      .toString();
  } 

  /**
   * @throws  IllegalStateException
   * @since   1.2.0
   */
  public void validate() 
    throws  IllegalStateException
  {
    GrouperValidator v = NotNullValidator.validate( this.getMemberUuid() );
    if (v.isInvalid()) {
      throw new IllegalStateException(E.SV_M);
    }
    v = NotNullValidator.validate( this.getUuid() );  
    if (v.isInvalid()) {
      throw new IllegalStateException(E.SV_I);
    }
  } 

  /**
   * 
   * @return the grouper session
   * @throws GrouperException
   */
  public GrouperSession internal_getRootSession() 
    throws  GrouperException
  {
    // TODO 20070417 deprecate if possible
    if (this.rootSession == null) {
      
      if (PrivilegeHelper.isWheelOrRoot( this.getSubject())) {
        this.rootSession = this;
      } else {

        GrouperSession rs = new GrouperSession();
        rs.setMemberUuid( MemberFinder.internal_findRootMember().getUuid() );
        rs.setStartTimeLong( new Date().getTime() );
        rs.setSubject( SubjectFinder.findRootSubject() );
        rs.setUuid( GrouperUuid.getUuid() );
        this.rootSession = rs;
        rs.rootSessionParent = this;
      }
    }
    return this.rootSession;
  } 


  /**
   * @return member uuid
   * @since   1.2.0
   */
  public String getMemberUuid() {
    if (StringUtils.isBlank(this.memberUUID)) {
      this.memberUUID = this.getMember().getUuid();
    }
    return this.memberUUID;
  }

  /**
   * @return start time
   * @since   1.2.0
   */
  public long getStartTimeLong() {
    return this.startTimeLong;
  }

  /**
   * @return uuid
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @param memberUUID1 
   * @since   1.2.0
   */
  public void setMemberUuid(String memberUUID1) {
    this.memberUUID = memberUUID1;
  
  }

  /**
   * @param startTime1 
   * @since   1.2.0
   */
  public void setStartTimeLong(long startTime1) {
    this.startTimeLong = startTime1;
  
  }

  /**
   * @param subject1 
   * @since   1.2.0
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  
  }

  /**
   * @param uuid1 
   * @since   1.2.0
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  
  }

  /**
   * @return the string
   * @since   1.2.0
   */
  public String toStringDto() {
    return new ToStringBuilder(this)
      .append( "memberUuid", this.getMemberUuid()  )
      .append( "startTime",  this.getStartTime()   )
      .append( "uuid",       this.getUuid() )
      .toString();
  }

  /**
   * @return  <code>AttributeDefResolver</code> used by this session.
   * @since   1.2.1
   */
  public AttributeDefResolver getAttributeDefResolver() {
    this.internal_ThrowIllegalStateIfStopped();
    if (this.attributeDefResolver == null) {
      this.attributeDefResolver = AttributeDefResolverFactory.getInstance(this);
    }
    return this.attributeDefResolver;
  }

  /**
   * Start a root session for interacting with the Grouper API.
   * This adds the session to the threadlocal.    This has 
   * threadlocal implications, so start and stop these hierarchically,
   * do not alternate.  If you need to, use the callback inverse of control.
   * 
   * This will not start a session if it is already started.
   * If it is started as a different user, it will start
   * 
   * @return  A Grouper API session result.
   * @throws  SessionException
   */
  public static GrouperSessionResult startRootSessionIfNotStarted() throws SessionException {
    
    return startIfNotStarted(SubjectFinder.findRootSubject());

  }

  /**
   * call this to send a callback for the grouper session object. cant use
   * inverse of control for this since it runs it.  Any method in the inverse of
   * control can access the grouper session in a threadlocal
   * 
   * @param grouperSession is the session to do an inverse of control on
   * 
   * @param grouperSessionHandler
   *          will get the callback
   * @return the object returned from the callback
   * @throws GrouperSessionException
   *           if there is a problem, will preserve runtime exceptions so they are
   *           thrown to the caller.  The GrouperSessionException wraps the underlying exception
   */
  public static Object callbackGrouperSession(GrouperSession grouperSession, GrouperSessionHandler grouperSessionHandler)
      throws GrouperSessionException {
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    if (LOG.isDebugEnabled()) {
      debugMap.put("method", "callbackGrouperSession()");
      if (grouperSession == null || grouperSession.getSubject() == null) {
        debugMap.put("subject", "null");
      } else {
        debugMap.put("hash", grouperSession.hashCode());

        debugMap.put("subject", grouperSession.getSubject().getId());
      }
      logAddThreadLocal(debugMap, "start_");
    }
    if (grouperSession != null && grouperSession.stopped) {
      throw new RuntimeException("Cannot callback a grouper session which is stopped!");
    }
    Object ret = null;
    try {
      boolean needsToBeRemoved = false;
      try {
        //add to threadlocal
        needsToBeRemoved = addStaticHibernateSession(grouperSession, false);
        int nullSessions = clearSessionNulls();
        if (LOG.isDebugEnabled()) {
          debugMap.put("needsToBeRemoved", needsToBeRemoved);
          debugMap.put("nullSessionsRemoved", nullSessions);
          logAddThreadLocal(debugMap, "postAdd_");
        }
        ret = grouperSessionHandler.callback(grouperSession);
    
      } finally {
        //remove from threadlocal
        if (needsToBeRemoved) {
          removeStaticGrouperSession(grouperSession);
        }
      }

    } finally {
      if (LOG.isDebugEnabled()) {
        logAddThreadLocal(debugMap, "end_");
        LOG.debug("Stack: " + GrouperUtil.stack());
        LOG.debug(debugMap);
      }
    }

    return ret;
    
  
  }

  /**
   * call this to send a callback for the root grouper session object. 
   * Any method in the inverse of
   * control can access the grouper session in a threadlocal
   * @param runAsRoot true to run as root, false to not run as root
   * @param grouperSessionHandler
   *          will get the callback
   * @return the object returned from the callback
   * @throws GrouperSessionException
   *           if there is a problem, will preserve runtime exceptions so they are
   *           thrown to the caller.  The GrouperSessionException wraps the underlying exception
   */
  public static Object internal_callbackRootGrouperSession(GrouperSessionHandler grouperSessionHandler)
      throws GrouperSessionException {
    return internal_callbackRootGrouperSession(true, grouperSessionHandler);
  }
  /**
   * call this to send a callback for the root grouper session object. 
   * Any method in the inverse of
   * control can access the grouper session in a threadlocal
   * 
   * @param grouperSessionHandler
   *          will get the callback
   * @return the object returned from the callback
   * @throws GrouperSessionException
   *           if there is a problem, will preserve runtime exceptions so they are
   *           thrown to the caller.  The GrouperSessionException wraps the underlying exception
   */
  public static Object internal_callbackRootGrouperSession(boolean runAsRoot, GrouperSessionHandler grouperSessionHandler)
      throws GrouperSessionException {

    // nevermind, dont run as root
    if (!runAsRoot) {
      return callbackGrouperSession(GrouperSession.staticGrouperSession(), grouperSessionHandler);
    }
    
    //this needs to run as root
    boolean startedGrouperSession = false;
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    if (grouperSession == null) {
      grouperSession = GrouperSession.startRootSession(false, false);
      startedGrouperSession = true;
    } else {
      // dont check if wheel or root since can be cached
      grouperSession = grouperSession.internal_getRootSession();
    }
    try {
      return callbackGrouperSession(grouperSession, grouperSessionHandler);
    } finally {
      if (startedGrouperSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * if sessions werent closed but were garbage collected
   * @return how many removed
   */
  private static int clearSessionNulls() {
    int removed = 0;
    List<WeakReference<GrouperSession>> grouperSessionList = grouperSessionList();
    synchronized (grouperSessionList) {

      for (int i=grouperSessionList.size()-1;i>=0;i--) {
        WeakReference<GrouperSession> grouperSessionReference = grouperSessionList.get(i);
        GrouperSession thisOne = grouperSessionReference.get();
        if (thisOne == null) {
          grouperSessionList.remove(i);
          removed++;
        }
      }
    }
    return removed;
  }
  
  /**
   * set the threadlocal hibernate session
   * 
   * @param grouperSession
   * @return if it was added (if already last one, dont add again)
   */
  private static boolean addStaticHibernateSession(GrouperSession grouperSession, boolean addEvenIfSame) {
    List<WeakReference<GrouperSession>> grouperSessionList = grouperSessionList();
    synchronized (grouperSessionList) {

      GrouperSession lastOne = null;
      for (int i=grouperSessionList.size()-1;i>=0;i--) {
        WeakReference<GrouperSession> grouperSessionReference = grouperSessionList.get(i);
        lastOne = grouperSessionReference.get();
        if (lastOne != null) {
          break;
        }
      }
      if (!addEvenIfSame && lastOne == grouperSession) {
        return false;
      }
      grouperSessionList.add(new WeakReference<GrouperSession>(grouperSession));
    }
    // cant have more than 100, something is wrong
    if (grouperSessionList.size() > 100) {
      grouperSessionList.clear();
      throw new RuntimeException(
          "There is probably a problem that there are 100 nested new GrouperSessions called!");
    }
    return true;
  }

  /**
   * get the threadlocal list of hibernate sessions (or create)
   * 
   * @return the set
   */
  private static List<WeakReference<GrouperSession>> grouperSessionList() {
    List<WeakReference<GrouperSession>> grouperSessionSet = staticSessions.get();
    if (grouperSessionSet == null) {
      // note the sessions are in order
      grouperSessionSet = new ArrayList<WeakReference<GrouperSession>>();
      staticSessions.set(grouperSessionSet);
    }
    return grouperSessionSet;
  }

  /**
   * this should remove the last grouper session which should be the same as
   * the one passed in
   * 
   * @param grouperSession should match the last group session
   */
  private static void removeStaticGrouperSession(GrouperSession grouperSession) {
    //this one better be at the end of the list
    List<WeakReference<GrouperSession>> grouperSessionList = grouperSessionList();
    int size = grouperSessionList.size();
    if (size == 0) {
      return;
    }
    synchronized (grouperSessionList) {
      // remove highest index
      for (int i=grouperSessionList.size()-1;i>=0;i--) {
        WeakReference<GrouperSession> thisOneReference = grouperSessionList.get(i);
        GrouperSession thisOne = thisOneReference.get();
        //the reference must be the same
        if (thisOne == grouperSession) {
          grouperSessionList.remove(i);
          return;
        }
      }
    }
  }

  /**
   * get the threadlocal grouper session. access this through inverse of
   * control.  this should be called by internal grouper methods which need the
   * grouper session
   * 
   * @return the grouper session or null if none there
   */
  public static GrouperSession staticGrouperSession() {
    return staticGrouperSession(true);
  }
  
  /**
   * clear the threadlocal grouper sessions (dont really need to call this, just
   * stop the session, but this is here for testing)
   */
  public static void clearGrouperSessions() {
    staticSessions.remove();
  }
  
  /**
   * get the threadlocal grouper session. access this through inverse of
   * control.  this should be called by internal grouper methods which need the
   * grouper session
   * @param exceptionOnNull true if exception when there is none there
   * 
   * @return the grouper session or null if none there
   * @throws IllegalStateException if no sessions available
   */
  public static GrouperSession staticGrouperSession(boolean exceptionOnNull) 
      throws IllegalStateException {

    //first look at the list of threadlocals
    List<WeakReference<GrouperSession>> grouperSessionList = grouperSessionList();
    GrouperSession grouperSession = null;
    synchronized (grouperSessionList) {
      int size = grouperSessionList.size();
      if (size != 0) {
        for (int i=grouperSessionList.size()-1;i>=0;i--) {
          WeakReference<GrouperSession> thisOneReference = grouperSessionList.get(i);
          GrouperSession thisOne = thisOneReference.get();
          //the reference must be the same
          if (thisOne == null) {
            continue;
          }
          // get the last index, return null if session closed
          grouperSession = thisOneReference.get();
          break;
        }
      }
    }

    if (grouperSession != null && grouperSession.subject == null) {
      grouperSession = null;
    }
    
    if (exceptionOnNull && grouperSession == null) {
      String error = "There is no open GrouperSession detected.  Make sure " +
          "to start a grouper session (e.g. GrouperSession.startRootSession() if you want to use a root session ) before calling this method";
      throw new IllegalStateException(error);
    }
    return grouperSession;
  }

  /**
   * get the subject by root session, then callback in a session for that subject
   * @param sourceId
   * @param subjectId
   * @param grouperSessionHandler
   */
  public static void callbackGrouperSessionBySubjectId(String subjectId, String sourceId,
      GrouperSessionHandler grouperSessionHandler) {

    Subject subject = (Subject)internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return SubjectFinder.findByIdAndSource(subjectId, sourceId, true);
      }
    });

    GrouperSession grouperSession = GrouperSession.start(subject, false, false);
    try {
      callbackGrouperSession(grouperSession, grouperSessionHandler);
    } finally {
      stopQuietly(grouperSession);
    }
    
  }
  
}
