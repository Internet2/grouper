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
import  edu.internet2.middleware.grouper.cfg.ApiConfig;
import  edu.internet2.middleware.grouper.internal.dto.GrouperSessionDTO;
import  edu.internet2.middleware.grouper.privs.AccessResolver;
import  edu.internet2.middleware.grouper.privs.AccessResolverFactory;
import  edu.internet2.middleware.grouper.privs.NamingResolver;
import  edu.internet2.middleware.grouper.privs.NamingResolverFactory;
import  edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import  edu.internet2.middleware.grouper.internal.util.Quote;
import  edu.internet2.middleware.grouper.internal.util.Realize;
import  edu.internet2.middleware.subject.*;
import  java.util.Date;

import org.apache.commons.lang.StringUtils;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;


/** 
 * Context for interacting with the Grouper API and Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.75 2008-01-14 07:36:49 mchyzer Exp $
 */
public class GrouperSession extends GrouperAPI {

  private AccessAdapter   access;         // TODO 20070816 eliminate
  private AccessResolver  accessResolver;
  private Member          cachedMember;
  private ApiConfig       cfg;
  private NamingAdapter   naming;         // TODO 20070816 eliminate
  private NamingResolver  namingResolver;
  private GrouperSession  rootSession;


  /**
   * Default constructor.
   * <p/>
   * @since   1.2.0
   */
  private GrouperSession() {
    this.cachedMember = null;
    this.cfg          = new ApiConfig();
    this.rootSession  = null;
  } 

  
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
    Member            m = null;
    try {
      StopWatch sw = new StopWatch();
      sw.start();

      //  this will create the member if it doesn't already exist
      m   = MemberFinder.internal_findBySubject(subject); 
      GrouperSession    s   = new GrouperSession();
      GrouperSessionDTO _s  = new GrouperSessionDTO()
        .setMemberUuid( m.getUuid() )
        .setStartTime( new Date().getTime() )
        .setSubject(subject)
        .setUuid( GrouperUuid.getUuid() )
        ;
      s.setDTO( _s.setId( GrouperDAOFactory.getFactory().getGrouperSession().create(_s) ) );

      sw.stop();
      EventLog.info( s.toString(), M.S_START, sw );
      return s;
    }
    catch (MemberNotFoundException eMNF)  {
      String idLog = subject == null ? " (subject is null) " : null;
      if (StringUtils.isBlank(idLog)) {
        //put the id in the error message if possible
        String id = m != null ? m.getSubjectId() : null;
        if (StringUtils.isBlank(id) && subject !=null) {
          id = subject.getId();
        }
        idLog = " (for subject id: " + id + ") ";
      }
      String msg = E.S_START + idLog + eMNF.getMessage();
      ErrorLog.fatal(GrouperSession.class, msg);
      throw new SessionException(msg, eMNF);
    }
  } 

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
   * Get name of class implenting {@link AccessAdapter} privilege interface.
   * <pre class="eg">
   * String klass = s.getAccessClass();
   * </pre>
   * @since   ?
   */
  public String getAccessClass() {
    return this.getConfig(ApiConfig.ACCESS_PRIVILEGE_INTERFACE); // TODO 20070725 is this necessary?
  } 

  /**
   * Get {@link AccessAdapter} implementation.
   * <p/>
   * @since   1.2.1
   */
  public AccessAdapter getAccessImpl() {
    if (this.access == null) {
      this.access = (AccessAdapter) Realize.instantiate(
        new ApiConfig().getProperty( ApiConfig.ACCESS_PRIVILEGE_INTERFACE ) 
      );
    }
    return this.access;
  }

  /**
   * @return  <code>AccessResolver</code> used by this session.
   * @since   1.2.1
   */
  protected AccessResolver getAccessResolver() {
    if (this.accessResolver == null) {
      this.accessResolver = AccessResolverFactory.getInstance(this);
    }
    return this.accessResolver;
  }

  /**
   * Get specified {@link ApiConfig} property.
   * <p/>
   * @return  Value of <i>property</i> or null if not set.
   * @throws  IllegalArgumentException if <i>property</i> is null.
   * @since   1.2.1
   */
  public String getConfig(String property) 
    throws  IllegalArgumentException
  {
    return this.cfg.getProperty(property);
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
    if ( this.cachedMember != null ) {
      return this.cachedMember;
    }
    try {
      Member m = new Member();
      m.setDTO( GrouperDAOFactory.getFactory().getMember().findByUuid( this._getDTO().getMemberUuid() ) );
      m.setSession(this);
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
   * @since   ?
   */
  public String getNamingClass() {
    return this.getConfig(ApiConfig.NAMING_PRIVILEGE_INTERFACE); // TODO 20070725 is this necessary?
  } 

  /**
   * Get {@link NamingAdapter} implementation.
   * <p/>
   * @since   1.2.1
   */
  public NamingAdapter getNamingImpl() {
    if (this.naming == null) {
      this.naming = (NamingAdapter) Realize.instantiate(
        new ApiConfig().getProperty( ApiConfig.NAMING_PRIVILEGE_INTERFACE ) 
      );
    }
    return this.naming;
  }

  /**
   * @return  <code>AccessResolver</code> used by this session.
   * @since   1.2.1
   */
  protected NamingResolver getNamingResolver() {
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
    return this._getDTO().getUuid();
  } // public String getSessionId()

  /**
   * Get this session's start time.
   * <pre class="eg">
   * Date startTime = s.getStartTime();
   * </pre>
   * @return  This session's start time.
   */
  public Date getStartTime() {
    return new Date( this._getDTO().getStartTime() );
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
    if ( this._getDTO().getSubject() == null ) {
      String msg = "unable to get subject associated with session";
      ErrorLog.fatal(GrouperSession.class, msg);
      throw new GrouperRuntimeException(msg);
    }
    return this._getDTO().getSubject();
  } // public Subject getSubject()

  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  /**
   * Currently just a testing hack.
   * <p/>
   * @return  Value of <i>property</i> or null if not set.
   * @throws  IllegalArgumentException if <i>property</i> is null.
   * @since   1.2.1
   */
  protected void setConfig(String property, String value) 
    throws  IllegalArgumentException
  {
    this.cfg.setProperty(property, value);
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
    if ( this._getDTO().getId() != null ) { // We have a persistent session
      StopWatch sw    = new StopWatch();
      sw.start();
      long      start = this.getStartTime().getTime();
      GrouperDAOFactory.getFactory().getGrouperSession().delete( this._getDTO() );
      sw.stop();
      Date      now   = new Date();
      long      dur   = now.getTime() - start;
      EventLog.info( this.toString(), "session: stop duration=" + + dur + "ms", sw );
    }
    this.setDTO(null);
    this.cachedMember = null;
  } 

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append( "session_id",   this._getDTO().getUuid()                                        )
      .append( "subject_id",   Quote.single( this._getDTO().getSubject().getId() )             )
      .append( "subject_type", Quote.single( this._getDTO().getSubject().getType().getName() ) )
      .toString();
  } 

  /**
   * @throws  IllegalStateException
   * @since   1.2.0
   */
  public void validate() 
    throws  IllegalStateException
  {
    GrouperValidator v = NotNullValidator.validate( this._getDTO().getMemberUuid() );
    if (v.isInvalid()) {
      throw new IllegalStateException(E.SV_M);
    }
    v = NotNullValidator.validate( this._getDTO().getUuid() );  
    if (v.isInvalid()) {
      throw new IllegalStateException(E.SV_I);
    }
  } 


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected GrouperSession internal_getRootSession() 
    throws  GrouperRuntimeException
  {
    // TODO 20070417 deprecate if possible
    if (this.rootSession == null) {
      GrouperSession rs = new GrouperSession();
      rs.cfg = this.cfg;
      rs.setDTO(
        new GrouperSessionDTO()
          .setMemberUuid( MemberFinder.internal_findRootMember().getUuid() )
          .setStartTime( new Date().getTime() )
          .setSubject( SubjectFinder.findRootSubject() )
          .setUuid( GrouperUuid.getUuid() )
      );
      this.rootSession = rs;
    }
    return this.rootSession;
  } 


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private GrouperSessionDTO _getDTO() {
    return (GrouperSessionDTO) super.getDTO();
  } 
  
}
