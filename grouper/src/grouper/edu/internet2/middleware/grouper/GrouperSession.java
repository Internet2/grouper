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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.subject.Subject;


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
   * store the grouper connection in thread local so other classes can get it.
   * this is only for inverse of control.  This has priority over the 
   * static session set from start()
   */
  private static ThreadLocal<List<GrouperSession>> staticSessions = new ThreadLocal<List<GrouperSession>>();

  /**
   * holds a thread local of the current grouper session.
   * this is set from a GrouperSesssion.start().  Note the 
   * inverse of control sessions have priority
   */
  private static ThreadLocal<GrouperSession> staticGrouperSession = new ThreadLocal<GrouperSession>();
  
  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private transient AccessResolver  accessResolver;

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private transient AttributeDefResolver  attributeDefResolver;

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Member          cachedMember;

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private transient NamingResolver  namingResolver;

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private transient GrouperSession  rootSession;

  /** */
  private String          memberUUID;

  /** */
  private long            startTimeLong;

  /** */
  @GrouperIgnoreDbVersion
  private Subject         subject;

  /** */
  private String          uuid;


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
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSession startRootSession()
    throws SessionException {
    return startRootSession(true);
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
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSession start(Subject subject, boolean addToThreadLocal) 
    throws SessionException
  {
    if (subject == null) {
      String idLog = "(subject is null)";
      String msg = E.S_START + idLog;
      LOG.fatal(msg);
      throw new SessionException(msg);
    }
    Member            m = null;
    StopWatch sw = new StopWatch();
    sw.start();

    //  this will create the member if it doesn't already exist
    m   = MemberFinder.internal_findBySubject(subject, null, true); 
    GrouperSession    s   =  new GrouperSession();
      s.setMemberUuid( m.getUuid() );
      s.setStartTimeLong( new Date().getTime() );
      s.setSubject(subject);
      s.setUuid( GrouperUuid.getUuid() );

    sw.stop();
    EventLog.info( s.toString(), M.S_START, sw );
    if (addToThreadLocal) {
      //add to threadlocal
      staticGrouperSession.set(s);
    }
    
    return s;
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
    try {
      Member m = GrouperDAOFactory.getFactory().getMember().findByUuid( this.getMemberUuid(), true );
      this.cachedMember = m;
      return this.cachedMember;
    }
    catch (MemberNotFoundException eShouldNeverHappen) {
      throw new IllegalStateException( 
        "this should never happen: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen
      );
    }
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
    //remove from threadlocal if this is the one on threadlocal (might not be due
    //to nesting)
    if (this == staticGrouperSession.get()) {
      staticGrouperSession.remove();
    }
    
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
    if (this.rootSession != null) {
      this.rootSession.stop();
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
      GrouperSession rs = new GrouperSession();
      rs.setMemberUuid( MemberFinder.internal_findRootMember().getUuid() );
      rs.setStartTimeLong( new Date().getTime() );
      rs.setSubject( SubjectFinder.findRootSubject() );
      rs.setUuid( GrouperUuid.getUuid() );
      this.rootSession = rs;
    }
    return this.rootSession;
  } 


  /**
   * @return member uuid
   * @since   1.2.0
   */
  public String getMemberUuid() {
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
    Object ret = null;
    boolean needsToBeRemoved = false;
    try {
      //add to threadlocal
      needsToBeRemoved = addStaticHibernateSession(grouperSession);
      ret = grouperSessionHandler.callback(grouperSession);
  
    } finally {
      //remove from threadlocal
      if (needsToBeRemoved) {
        removeLastStaticGrouperSession(grouperSession);
      }
    }
    return ret;
  
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
  public static Object internal_callbackRootGrouperSession(GrouperSessionHandler grouperSessionHandler)
      throws GrouperSessionException {

    //this needs to run as root
    boolean startedGrouperSession = false;
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    if (grouperSession == null) {
      grouperSession = GrouperSession.startRootSession(false);
      startedGrouperSession = true;
    }
    if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
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
   * set the threadlocal hibernate session
   * 
   * @param grouperSession
   * @return if it was added (if already last one, dont add again)
   */
  private static boolean addStaticHibernateSession(GrouperSession grouperSession) {
    List<GrouperSession> grouperSessionList = grouperSessionList();
    GrouperSession lastOne = grouperSessionList.size() == 0 ? null : grouperSessionList.get(grouperSessionList.size()-1);
    if (lastOne == grouperSession) {
      return false;
    }
    grouperSessionList.add(grouperSession);
    // cant have more than 60, something is wrong
    if (grouperSessionList.size() > 60) {
      grouperSessionList.clear();
      throw new RuntimeException(
          "There is probably a problem that there are 60 nested new GrouperSessions called!");
    }
    return true;
  }

  /**
   * get the threadlocal list of hibernate sessions (or create)
   * 
   * @return the set
   */
  private static List<GrouperSession> grouperSessionList() {
    List<GrouperSession> grouperSessionSet = staticSessions.get();
    if (grouperSessionSet == null) {
      // note the sessions are in order
      grouperSessionSet = new ArrayList<GrouperSession>();
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
  private static void removeLastStaticGrouperSession(GrouperSession grouperSession) {
    //this one better be at the end of the list
    List<GrouperSession> grouperSessionList = grouperSessionList();
    int size = grouperSessionList.size();
    if (size == 0) {
      throw new RuntimeException("Supposed to remove a session from stack, but stack is empty");
    }
    GrouperSession lastOne = grouperSessionList.get(size-1);
    //the reference must be the same
    if (lastOne != grouperSession) {
      //i guess just clear it out
      grouperSessionList.clear();
      throw new RuntimeException("Illegal state, the grouperSession threadlocal stack is out of sync!");
    }
    grouperSessionList.remove(grouperSession);
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
   * clear the threadlocal grouper session (dont really need to call this, just
   * stop the session, but this is here for testing)
   */
  public static void clearGrouperSession() {
    staticGrouperSession.remove();
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
    List<GrouperSession> grouperSessionList = grouperSessionList();
    int size = grouperSessionList.size();
    String error = "There is no open GrouperSession detected.  Make sure " +
        "to start a grouper session (e.g. GrouperSession.startRootSession() if you want to use a root session ) before calling this method";
    GrouperSession grouperSession = null;
    if (size == 0) {
      //if nothing in the threadlocal list, then use the last one
      //started (and added)
      grouperSession = staticGrouperSession.get();
      
    } else {
      // get the last index, return null if session closed
      grouperSession = grouperSessionList.get(size-1);
    }

    if (grouperSession != null && grouperSession.subject == null) {
      grouperSession = null;
    }
    
    if (exceptionOnNull && grouperSession == null) {
      throw new IllegalStateException(error);
      }
      return grouperSession;
    }
  
}
