/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

/** 
 * {@link GrouperSession} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSessionDTO.java,v 1.7 2007-04-05 14:28:28 blair Exp $
 */
class GrouperSessionDTO extends BaseGrouperDTO {

  // PRIVATE CLASS CONSTANTS //
  private static final Class KLASS = GrouperSessionDTO.class;


  // PRIVATE INSTANCE VARIABLES //
  private PrivilegeCache  accessCache;
  private String          id;
  private String          memberUUID;
  private PrivilegeCache  namingCache;
  private GrouperSession  rootSession;
  private Date            startTime;
  private Subject         subject;
  private String          uuid;


  // PUBLIC INSTANCE METHODS //
  /**
   * @since   1.2.0
   */  
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GrouperSessionDTO)) {
      return false;
    }
    GrouperSessionDTO that = (GrouperSessionDTO) other;
    return new EqualsBuilder()
      .append( this.getMemberUuid(), that.getMemberUuid() )
      .append( this.getStartTime(),  that.getStartTime()  )
      .append( this.getUuid(),       that.getUuid()       )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getMemberUuid() )
      .append( this.getStartTime()  )
      .append( this.getUuid()       )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "memberUuid", this.getMemberUuid()  )
      .append( "startTime",  this.getStartTime()   )
      .append( "uuid",       this.getUuid() )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected HibernateGrouperSessionDAO getDAO() {
    return new HibernateGrouperSessionDAO()
      .setId( this.getId() )
      .setMemberUuid( this.getMemberUuid() )
      .setStartTime( this.getStartTime() )
      .setUuid( this.getUuid() )
      ;
  } // protected HibernateGrouperSessionDAO getDAO()


  // GETTERS //

  protected PrivilegeCache getAccessCache() {
    if (this.accessCache == null) {
      this.setAccessCache( BasePrivilegeCache.getCache( GrouperConfig.getProperty(GrouperConfig.PACI) ) );
      DebugLog.info( KLASS, "using access cache: " + this.accessCache.getClass().getName() );
    }
    return this.accessCache;
  }
  protected String getId() {
    return this.id;
  } 
  protected String getMemberUuid() {
    return this.memberUUID;
  }
  protected PrivilegeCache getNamingCache() {
    if (this.namingCache == null) {
      this.setNamingCache( BasePrivilegeCache.getCache( GrouperConfig.getProperty(GrouperConfig.PNCI) ) );
      DebugLog.info( KLASS, "using naming cache: " + this.namingCache.getClass().getName() );
    }
    return this.namingCache;
  }
  protected GrouperSession getRootSession() 
    throws  GrouperRuntimeException
  {
    // TODO 20070119 should i care if we are fetching a root session from within another root session?
    if (this.rootSession == null) {
      GrouperSession rs = new GrouperSession();
      rs.setDTO(
        new GrouperSessionDTO()
          .setMemberUuid( MemberFinder.internal_findRootMember().getUuid() )
          .setStartTime( new Date() )
          .setSubject( SubjectFinder.findRootSubject() )
          .setUuid( GrouperUuid.internal_getUuid() )
      );
      this.setRootSession(rs);
    }
    return this.rootSession;
  } 
  protected Date getStartTime() {
    return this.startTime;
  }
  protected Subject getSubject() {
    return this.subject;
  }
  protected String getUuid() {
    return this.uuid;
  }


  // SETTERS //

  protected GrouperSessionDTO setAccessCache(PrivilegeCache accessCache) {
    this.accessCache = accessCache;
    return this;
  }
  protected GrouperSessionDTO setId(String id) {
    this.id = id;
    return this;
  }
  protected GrouperSessionDTO setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
    return this;
  }
  protected GrouperSessionDTO setNamingCache(PrivilegeCache namingCache) {
    this.namingCache = namingCache;
    return this;
  }
  protected GrouperSessionDTO setRootSession(GrouperSession rootSession) {
    this.rootSession = rootSession;
    return this;
  }
  protected GrouperSessionDTO setStartTime(Date startTime) {
    this.startTime = startTime;
    return this;
  }
  protected GrouperSessionDTO setSubject(Subject subject) {
    this.subject = subject;
    return this;
  }
  protected GrouperSessionDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

} // class GrouperSessionDTO extends BaseGrouperDTO

