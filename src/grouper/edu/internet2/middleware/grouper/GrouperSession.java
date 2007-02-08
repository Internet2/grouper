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
 * @version $Id: GrouperSession.java,v 1.46 2007-02-08 16:25:25 blair Exp $
 */
public class GrouperSession extends GrouperAPI {

  // PRIVATE INSTANCE VARIABLES //
  private GrouperSessionDTO dto  = null;


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

      Member m = MemberFinder.internal_findBySubject(subject); // this will create the member if it doesn't already exist

      GrouperSessionDTO dto = new GrouperSessionDTO();
      dto.setMemberUuid( m.getUuid() );
      dto.setStartTime( new Date() );
      dto.setSessionUuid( GrouperUuid.internal_getUuid() );
      dto.setSubject(subject);

      dto.setId( HibernateGrouperSessionDAO.create(dto) );
      GrouperSession s = new GrouperSession();
      s.setDTO(dto);

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
    try {
      Member m = new Member();
      m.setDTO( HibernateMemberDAO.findByUuid( this.getDTO().getMemberUuid() ) );
      m.setSession(this);
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
  } // public void stop()

  public String toString() {
    // TODO 20070125 replace with call to DTO?
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append( "session_id",   this.getDTO().getSessionUuid()                                 )
      .append( "subject_id",   U.internal_q( this.getDTO().getSubject().getId() )             )
      .append( "subject_type", U.internal_q( this.getDTO().getSubject().getType().getName() ) )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected GrouperSessionDTO getDTO() {
    return (GrouperSessionDTO) super.getDTO();
  } // protected GrouperSessionDTO getDTO()

} // public class GrouperSession extends GrouperAPI

