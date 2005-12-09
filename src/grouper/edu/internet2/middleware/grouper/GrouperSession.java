/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
import  java.io.Serializable;
import  java.util.Date;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;


/** 
 * Session for interacting with the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.7 2005-12-09 07:35:38 blair Exp $
 *     
*/
public class GrouperSession implements Serializable {

  // Hibernate Properties
  private String  id;
  private Member  member_id;
  private String  session_id;
  private Date    start_time;


  // Private Transient Instance Variables
  private transient Subject subj  = null;


  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public GrouperSession() { 
    // Nothing
  } // public GrouperSession()


  // Public class methods

  /**
   * Start a session for interacting with the Grouper API.
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.startSession(subject);
   * </pre>
   * @param   subject   Start session as this {@link Subject}.
   * @return  A Grouper API session.
   * @throws  SessionException
   */
  // TODO Rename: getSession
  public static GrouperSession startSession(Subject subject) 
    throws SessionException
  {
    GrouperSession s = _getSession(subject);
    try {
      // Will cascade and save newly created Member if appropriate
      HibernateHelper.save(s);
    }
    catch (HibernateException e) {
      throw new SessionException(
        "Unable to start session: " + e.getMessage()
      );
    }
    return s;
  } // public static GrouperSession startSession(subject)


  // Public instance methods

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
    return GrouperConfig.getInstance().getProperty("interface.access"); 
  } // public String getAccessClass()

   /**
   * Get the {@link Member} associated with this API session.
   * <pre class="eg">
   * // Get this session's Subject as a Member object.
   * Member m = s.getMember(); 
   * </pre>
   * @return  A {@link Member} object.
   */
  public Member getMember() {
    return this.getMember_id();
  } // public Member getMember()

  /**
   * Get name of class being used for naming privileges.
   * <pre class="eg">
   * String klass = s.getNamingClass();
   * </pre>
   * @return  Name of class implementing naming privileges.
   */
  public String getNamingClass() {
    return GrouperConfig.getInstance().getProperty("interface.naming"); 
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
  public Subject  getSubject() {
    // DESIGN What exception?
    // TODO What exception?
    if (this.subj == null) {
      try {
        this.subj = this.getMember_id().getSubject();
      }
      catch (Exception e) {
        // Ignore
      }
    }
    if (this.subj == null) {
      throw new RuntimeException("unable to get subject");
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
  public void stop() {
    try {
      HibernateHelper.delete(this);
      this.setId(null);
      this.setMember_id(null);
      this.setSession_id(null);
      this.setStart_time(null);
      this.subj = null;
    }
    catch (HibernateException eH) {
      throw new RuntimeException("unable to stop session: " + eH.getMessage());
    }
  } // public void stop()

  public String toString() {
    String  who   = this.getSubject().getId();
    String  type  = this.getSubject().getType().getName();
    if (type.equals(SubjectTypeEnum.valueOf("group"))) {
      who = this.getSubject().getName();
    }
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append("session_id"    , this.getSession_id())
      .append("subject_id"    , who                 )
      .append("subject_type"  , type                )
      .toString();
  } // public String toString()


  // Protected Static Methods
  protected static void validate(GrouperSession s) {
    try {
      if (s == null) {
        throw new RuntimeException("null session object");
      }
      if (s.id == null) {
        throw new RuntimeException("null session identity");
      }
      if (s.member_id == null) {
        throw new RuntimeException("null session member");
      }
      if (s.session_id == null) {
        throw new RuntimeException("null session id");
      }
      if (s.start_time == null) {
        throw new RuntimeException("null session start time");
      }
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  } // protected static void validate(s)


  // Private Static Methods
  private static GrouperSession _getSession(Subject subj) {
    try {
      GrouperSession s = new GrouperSession();
      // Transient
      s.subj = subj;
      // Persistent
      s.setMember_id(MemberFinder.findBySubject(subj));
      s.setStart_time( new Date() );
      s.setSession_id( GrouperUuid.getUuid() );
      return s;
    }
    catch (MemberNotFoundException e) {
      // TODO What to do|throw here?
      throw new RuntimeException(
        "member error: " + e.getMessage()
      );
    }
  } // private GrouperSession(subj) 


  // Hibernate Accessors
  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  private Date getStart_time() {
    return this.start_time;
  }

  private void setStart_time(Date start_time) {
    this.start_time = start_time;
  }

  private String getSession_id() {
    return this.session_id;
  }

  private void setSession_id(String session_id) {
    this.session_id = session_id;
  }

  private Member getMember_id() {
    return this.member_id;
  }

  private void setMember_id(Member member_id) {
    this.member_id = member_id;
  }

}

