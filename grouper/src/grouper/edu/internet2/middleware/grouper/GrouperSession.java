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
import  edu.internet2.middleware.subject.*;
import  java.util.Date;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * Context for interacting with the Grouper API and Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.54 2007-03-28 18:12:12 blair Exp $
 */
public class GrouperSession extends GrouperAPI {

  // PRIVATE CLASS CONSTANTS //
  private static final String KEY_MEMBER = "member"; // for state caching


  // PRIVATE INSTANCE VARIABLES //
  private SimpleCache findMemberDTOByUuidCache;
  private SimpleCache stateCache;


  // CONSTRUCTORS //
  
  // @since   1.2.0
  protected GrouperSession() {
    this.findMemberDTOByUuidCache = new SimpleCache();
    this.stateCache               = new SimpleCache();
  } // protected GrouperSession()

  
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
      StopWatch sw = new StopWatch();
      sw.start();

      //  this will create the member if it doesn't already exist
      Member            m   = MemberFinder.internal_findBySubject(subject); 
      GrouperSession    s   = new GrouperSession();
      GrouperSessionDTO _s  = new GrouperSessionDTO()
        .setMemberUuid( m.getUuid() )
        .setStartTime( new Date() )
        .setSessionUuid( GrouperUuid.internal_getUuid() )
        .setSubject(subject);
      s.setDTO( _s.setId( HibernateGrouperSessionDAO.create(_s) ) );

      sw.stop();
      EventLog.info( s.toString(), M.S_START, sw );
      return s;
    }
    catch (MemberNotFoundException eMNF)  {
      String msg = E.S_START + eMNF.getMessage();
      ErrorLog.fatal(GrouperSession.class, msg);
      throw new SessionException(msg, eMNF);
    }
  } // public static GrouperSession start(subject)

  /**
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

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if ( !(other instanceof GrouperSession) ) {
      return false;
    }
    return this.getDTO().equals( ( (GrouperSession) other ).getDTO() );
  } // public boolean equals(other)

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
    if ( this.stateCache.containsKey(KEY_MEMBER) ) {
      return (Member) this.stateCache.get(KEY_MEMBER);
    }
    try {
      Member m = new Member();
      m.setDTO( HibernateMemberDAO.findByUuid( this.getDTO().getMemberUuid() ) );
      m.setSession(this);
      this.stateCache.put(KEY_MEMBER, m);
      return m;
    }
    catch (MemberNotFoundException eShouldNeverHappen) {
      throw new IllegalStateException( 
        "this should never happen: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen
      );
    }
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
   * String id = s.internal_getSessionId();
   * </pre>
   * @return  The session id.
   */
  public String getSessionId() {
    return this.getDTO().getSessionUuid();
  } // public String getSessionId()

  /**
   * Get this session's start time.
   * <pre class="eg">
   * Date startTime = s.getStartTime();
   * </pre>
   * @return  This session's start time.
   */
  public Date getStartTime() {
    return this.getDTO().getStartTime();
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
    if ( this.getDTO().getSubject() == null ) {
      String msg = "unable to get subject associated with session";
      ErrorLog.fatal(GrouperSession.class, msg);
      throw new GrouperRuntimeException(msg);
    }
    return this.getDTO().getSubject();
  } // public Subject getSubject()

  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  /**
   * Stop this API session.
   * <pre class="eg">
   * s.stop();
   * </pre>
   */
  public void stop() 
    throws  SessionException
  {
    if ( this.getDTO().getId() != null ) { // We have a persistent session
      StopWatch sw    = new StopWatch();
      sw.start();
      long      start = this.getStartTime().getTime();
      HibernateGrouperSessionDAO.delete( this.getDTO() );
      sw.stop();
      Date      now   = new Date();
      long      dur   = now.getTime() - start;
      EventLog.info( this.toString(), "session: stop duration=" + + dur + "ms", sw );
    }
    this.setDTO(null);
    this.stateCache.removeAll();
  } // public void stop()

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append( "session_id",   this.getDTO().getSessionUuid()                                 )
      .append( "subject_id",   U.internal_q( this.getDTO().getSubject().getId() )             )
      .append( "subject_type", U.internal_q( this.getDTO().getSubject().getType().getName() ) )
      .toString();
  } // public String toString()

  /**
   * @throws  IllegalStateException
   * @since   1.2.0
   */
  public void validate() 
    throws  IllegalStateException
  {
    GrouperValidator v = NotNullValidator.validate( this.getDTO().getMemberUuid() );
    if (v.isInvalid()) {
      throw new IllegalStateException(E.SV_M);
    }
    v = NotNullValidator.validate( this.getDTO().getSessionUuid() );  
    if (v.isInvalid()) {
      throw new IllegalStateException(E.SV_I);
    }
    v = NotNullValidator.validate( this.getDTO().getStartTime() );
    if (v.isInvalid()) {
      throw new IllegalStateException(E.SV_T);
    }
  } // public void validate(


  // PROTECTED INSTANCE METHODS //

  // proactive caching on a per-session basis
  // @since   1.2.0
  protected MemberDTO cachingFindMemberDTOByUuid(String uuid) 
    throws  GrouperDAOException,      // TODO 20070322 is this appropriate here?
            MemberNotFoundException
  {
    if ( this.findMemberDTOByUuidCache.containsKey(uuid) ) {
      return (MemberDTO) this.findMemberDTOByUuidCache.get(uuid);
    }
    this.findMemberDTOByUuidCache.put( uuid, HibernateMemberDAO.findByUuid(uuid) );
    return (MemberDTO) this.findMemberDTOByUuidCache.get(uuid);
  } // protected MemberDTO cachingFindMemberDTOByUuid(uuid)

  // @since   1.2.0
  protected GrouperSessionDTO getDTO() {
    return (GrouperSessionDTO) super.getDTO();
  } // protected GrouperSessionDTO getDTO()

} // public class GrouperSession extends GrouperAPI

