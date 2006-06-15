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
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  net.sf.ehcache.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * Context for interacting with the Grouper API and Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.25 2006-06-15 04:10:34 blair Exp $
 */
public class GrouperSession {

  // PRIVATE CLASS CONSTANTS //
  private static final EventLog EL        = new EventLog();
  // TODO Move to *E*
  private static final String   ERR_START = "unable to start session: ";
  private static final String   ERR_STOP  = "unable to stop session: ";


  // HIBERNATE PROPERTIES //
  private String  id;
  private Member  member_id;
  private String  session_id;
  private Date    start_time;


  // PRIVATE CLASS VARIABLES //
  private static Subject root = null;


  // PRIVATE TRANSIENT INSTANCE VARIABLES //
  private transient Subject subj  = null;
  private transient String  who;
  private transient String  type;


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
  // TODO Rename: getSession
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
      EL.sessionStart(s.toString(), sw);
      return s;
    }
    catch (Exception e) {
      // @exception HibernateException
      // @MemberNotFoundException
      String msg = ERR_START + e.getMessage();
      ErrorLog.fatal(GrouperSession.class, msg);
      throw new SessionException(msg, e);
    }
  } // public static GrouperSession start(subject)

  protected static GrouperSession startTransient() {
    if (root == null) {
      try {
        root = SubjectFinder.findById(
          GrouperConfig.ROOT, GrouperConfig.IST, InternalSourceAdapter.ID
        );
      }
      catch (Exception e) {
        String msg = E.S_NOSTARTROOT + e.getMessage();
        ErrorLog.fatal(GrouperSession.class, msg);
        throw new RuntimeException(msg, e);
      }
    }
    try {
      return _getSession(root);
    }
    catch (Exception e) {
      String msg = E.S_NOSTARTROOT + e.getMessage();
      ErrorLog.fatal(GrouperSession.class, msg);
      throw new RuntimeException(msg, e);
    }
  } // protected static GrouperSession startTransient()


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
    return GrouperConfig.getInstance().getProperty(GrouperConfig.PAI);
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
    return GrouperConfig.getInstance().getProperty(GrouperConfig.PNI);
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
   * // Get this session's Subject.
   * Subject subj = s.getSubject(); 
   * </pre>
   * @return  A {@link Subject} object.
   */
  public Subject getSubject() {
    if (this.subj == null) {
      try {
        this.subj = this.getMember_id().getSubject();
      }
      catch (Exception e) {
        String msg = E.S_GETSUBJECT + e.getMessage();
        ErrorLog.fatal(GrouperSession.class, msg);
        throw new RuntimeException(msg);
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
        EL.sessionStop(sessionToString, start, sw);
      }
      this.setMember_id(null);
      this.setSession_id(null);
      this.setStart_time(null);
      this.subj = null;
    }
    catch (HibernateException eH) {
      String msg = ERR_STOP + eH.getMessage();
      ErrorLog.error(GrouperSession.class, msg);
      throw new SessionException(msg, eH);
    }
  } // public void stop()

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append("session_id"    , this.getSession_id())
      .append("subject_id"    , this.who            )
      .append("subject_type"  , this.type           )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //
  // TODO Deprecate
  protected static void validate(GrouperSession s) {
    try {
      GrouperSessionValidator.validate(s);
    }
    catch (ModelException eM) {
      eM.printStackTrace();
      throw new RuntimeException(eM.getMessage(), eM);
    }
  } // protected static void validate(s)


  // PRIVATE STATIC METHODS //
  private static GrouperSession _getSession(Subject subj) 
    throws  MemberNotFoundException
  {
    GrouperSession s = new GrouperSession();
    // Transient
    s.subj  = subj;
    s.who   = subj.getId();
    s.type  = subj.getType().getName();
    //if (s.type.equals(SubjectTypeEnum.valueOf("group"))) {
    if (s.type.equals("group")) { // FIXME
      s.who = subj.getName();
    }
    // Persistent
    s.setMember_id(MemberFinder.findBySubject(subj));
    s.setStart_time( new Date() );
    s.setSession_id( GrouperUuid.getUuid() );
    return s;
  } // private GrouperSession(subj) 


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

