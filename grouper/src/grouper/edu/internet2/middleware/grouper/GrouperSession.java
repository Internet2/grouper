/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * Context for interacting with the Grouper API and Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.37 2006-09-11 14:00:33 blair Exp $
 */
public class GrouperSession {

  // HIBERNATE PROPERTIES //
  private String  id;
  private Member  member_id;
  private String  session_id;
  private Date    start_time;


  // PRIVATE TRANSIENT INSTANCE VARIABLES //
  private transient PrivilegeCache    ac    = getAccessCache();
  private transient PrivilegeCache    nc    = getNamingCache();
  private transient GrouperSession    ps    = null; // parent session of root session
  private transient GrouperSession    rs    = null; // inner root session
  private transient Subject           subj  = null;
  private transient String            type;
  private transient String            who;


  // CONSTRUCTORS //
  private GrouperSession() { 
    // Default constructor for Hibernate
  } // private GrouperSession()


  // PUBLIC CLASS METHODS //

  /**
   * Start a session for interacting with the Grouper API.
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.start(subject);
   * </pre>
   * @param   subject   Start session as this {@link Subject}.
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  public static GrouperSession start(Subject subject) 
    throws SessionException
  {
    try {
      // No cascading so save everything myself
      StopWatch sw = new StopWatch();
      sw.start();
      GrouperSession s = _getSession(subject);
      Set objects = new LinkedHashSet();
      objects.add(s.getMember_id());
      objects.add(s);
      HibernateHelper.save(objects);
      sw.stop();
      EventLog.info(s.toString(), M.S_START, sw);
      return s;
    }
    catch (HibernateException eH)         {
      String msg = E.S_START + eH.getMessage();
      ErrorLog.fatal(GrouperSession.class, msg);
      throw new SessionException(msg, eH);
    } 
    catch (MemberNotFoundException eMNF)  {
      String msg = E.S_START + eMNF.getMessage();
      ErrorLog.fatal(GrouperSession.class, msg);
      throw new SessionException(msg, eMNF);
    }
  } // public static GrouperSession start(subject)


  // PUBLIC INSTANCE METHODS //

  public boolean equals(Object other) {
    if ( (this == other ) ) return true;
    if ( !(other instanceof GrouperSession) ) return false;
    GrouperSession castOther = (GrouperSession) other;
    return new EqualsBuilder()
           .append(this.getSession_id(), castOther.getSession_id())
           .append(this.getMember_id(), castOther.getMember_id())
           .isEquals();
  }

  /**
   * Get name of class being used for access privileges.
   * <pre class="eg">
   * String klass = s.getAccessClass();
   * </pre>
   * @return  Name of class implementing naming privileges.
   */
  public String getAccessClass() {
    return GrouperConfig.getProperty(GrouperConfig.PAI);
  } // public String getAccessClass()

   /**
   * Get the {@link Member} associated with this API session.
   * <pre class="eg">
   * Member m = s.getMember(); 
   * </pre>
   * @return  A {@link Member} object.
   * @throws  NullPointerException if {@link Member} null.
   */
  public Member getMember() 
    throws  NullPointerException
  {
    Member m = this.getMember_id();
    Validator.valueNotNull(m, E.MEMBER_NULL);
    m.setSession(this);
    return m;
  } // public Member getMember()

  /**
   * Get name of class being used for naming privileges.
   * <pre class="eg">
   * String klass = s.getNamingClass();
   * </pre>
   * @return  Name of class implementing naming privileges.
   */
  public String getNamingClass() {
    return GrouperConfig.getProperty(GrouperConfig.PNI);
  } // public String getNamingClass()

  /**
   * Get this session's id.
   * <pre class="eg">
   * String id = s.getSessionId();
   * </pre>
   * @return  The session id.
   */
  public String getSessionId() {
    return this.getSession_id();
  } // public String getSessionId()

  /**
   * Get this session's start time.
   * <pre class="eg">
   * Date startTime = s.getStartTime();
   * </pre>
   * @return  This session's start time.
   */
  public Date getStartTime() {
    return this.getStart_time();
  } // public Date getStartTime()

  /**
   * Get the {@link Subject} associated with this API session.
   * <pre class="eg">
   * Subject subj = s.getSubject(); 
   * </pre>
   * @return  A {@link Subject} object.
   * @throws  GrouperRuntimeException
   */
  public Subject getSubject() 
    throws  GrouperRuntimeException
  {
    if (this.subj == null) {
      try {
        this.subj = this.getMember_id().getSubject();
      }
      catch (Exception e) {
        String msg = E.S_GETSUBJECT + e.getMessage();
        ErrorLog.fatal(GrouperSession.class, msg);
        throw new GrouperRuntimeException(msg);
      }
    }
    return this.subj;
  } // public Subject getSubject()

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getSession_id())
           .append(getMember_id())
           .toHashCode();
  }

  /**
   * Stop this API session.
   * <pre class="eg">
   * s.stop();
   * </pre>
   */
  public void stop() 
    throws  SessionException
  {
    try {
      if (this.getId() != null) {
        StopWatch sw = new StopWatch();
        sw.start();
        // So we don't log transient sessions
        String sessionToString = this.toString();
        // For logging session duration
        long   start  = this.getStart_time().getTime();
        HibernateHelper.delete(this);
        this.setId(null);
        sw.stop();
        Date  now = new Date();
        long  dur = now.getTime() - start;
        EventLog.info(sessionToString, M.S_STOP + dur + "ms", sw);
      }
      this.setMember_id(null);
      this.setSession_id(null);
      this.setStart_time(null);
      this.subj = null;
    }
    catch (HibernateException eH) {
      String msg = E.S_STOP + eH.getMessage();
      ErrorLog.error(GrouperSession.class, msg);
      throw new SessionException(msg, eH);
    }
  } // public void stop()

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append("session_id"    , this.getSession_id()  )
      .append("subject_id"    , U.q(this.who)         )
      .append("subject_type"  , U.q(this.type)        )
      .toString();
  } // public String toString()



  // PROTECTED INSTANCE METHODS //

  // @since   1.1.0
  protected PrivilegeCache getAccessCache() {
    if (this.ac == null) {
      this.ac = BasePrivilegeCache.getCache(GrouperConfig.getProperty(GrouperConfig.PACI));
      DebugLog.info(
        GrouperSession.class, "using access cache: " + ac.getClass().getName()
      );
    }
    return this.ac;
  } // protected PrivilegeCache getAccessCache()

  // @since   1.1.0
  protected PrivilegeCache getNamingCache() {
    if (this.nc == null) {
      this.nc = BasePrivilegeCache.getCache(GrouperConfig.getProperty(GrouperConfig.PNCI));
      DebugLog.info(
        GrouperSession.class, "using naming cache: " + nc.getClass().getName()
      );
    }
    return this.nc;
  } // protected PrivilegeCache getNamingCache()

  // @throws  GrouperRuntimeException
  // @since   1.1.0
  protected GrouperSession getRootSession() 
    throws  GrouperRuntimeException
  {
    if (this._getParentSession() != null) { 
      DebugLog.info(GrouperSession.class, M.GOT_INNER_WITHIN_INNER);
    }
    if (this.rs == null) {
      try {
        this.rs = _getSession( SubjectFinder.findRootSubject() );
        this.rs._setParentSession(this);
      }
      catch (MemberNotFoundException eMNF) {
        String msg = E.S_NOSTARTROOT + eMNF.getMessage();
        ErrorLog.fatal(GrouperSession.class, msg);
        throw new GrouperRuntimeException(msg, eMNF);
      }
    }
    return this.rs;
  } // protected GrouperSession getRootSession()


  // PRIVATE STATIC METHODS //
  private static GrouperSession _getSession(Subject subj) 
    throws  MemberNotFoundException
  {
    GrouperSession s = new GrouperSession();
    // Transient
    s.subj  = subj;
    s.who   = subj.getId();
    s.type  = subj.getType().getName();
    if (s.type.equals("group")) {
      s.who = subj.getName();
    }
    // Persistent
    s.setMember_id(MemberFinder.findBySubject(subj));
    s.setStart_time( new Date() );
    s.setSession_id( GrouperUuid.getUuid() );
    return s;
  } // private GrouperSession(subj) 


  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private GrouperSession _getParentSession() {
    return this.ps;
  } // private GrouperSession getParentSession()

  // @since   1.1.0
  private void _setParentSession(GrouperSession parent) {
    this.ps = parent;
  } // private void setParentSession(parent)


  // GETTERS //
  private String getId() {
    return this.id;
  }
  protected Member getMember_id() {
    return this.member_id;
  }
  protected String getSession_id() {
    return this.session_id;
  }
  protected Date getStart_time() {
    return this.start_time;
  }


  // SETTERS //
  private void setId(String id) {
    this.id = id;
  }
  private void setMember_id(Member member_id) {
    this.member_id = member_id;
  }
  private void setSession_id(String session_id) {
    this.session_id = session_id;
  }
  private void setStart_time(Date start_time) {
    this.start_time = start_time;
  }

}

