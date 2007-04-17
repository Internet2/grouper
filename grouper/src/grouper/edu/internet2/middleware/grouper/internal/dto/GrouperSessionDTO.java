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

package edu.internet2.middleware.grouper.internal.dto;
import  edu.internet2.middleware.grouper.DebugLog;
import  edu.internet2.middleware.grouper.GrouperConfig;
import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.GrouperRuntimeException;
import  edu.internet2.middleware.grouper.GrouperSession;
import  edu.internet2.middleware.grouper.MemberFinder;
import  edu.internet2.middleware.grouper.SubjectFinder;
import  edu.internet2.middleware.grouper.internal.cache.BasePrivilegeCache;
import  edu.internet2.middleware.grouper.internal.cache.PrivilegeCache;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import  edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import  edu.internet2.middleware.subject.*;
import  java.util.Date;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link GrouperSession} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSessionDTO.java,v 1.3 2007-04-17 18:08:05 blair Exp $
 * @since   1.2.0
 */
public class GrouperSessionDTO extends BaseGrouperDTO {

  // PRIVATE CLASS CONSTANTS //
  private static final Class KLASS = GrouperSessionDTO.class;


  // PRIVATE INSTANCE VARIABLES //
  private PrivilegeCache  accessCache;
  private String          id;
  private String          memberUUID;
  private PrivilegeCache  namingCache;
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
  public PrivilegeCache getAccessCache() {
    // FIXME 20070416 why is this in this class?
    if (this.accessCache == null) {
      this.setAccessCache( BasePrivilegeCache.getCache( GrouperConfig.getProperty(GrouperConfig.PACI) ) );
      DebugLog.info( KLASS, "using access cache: " + this.accessCache.getClass().getName() );
    }
    return this.accessCache;
  }

  /**
   * @since   1.2.0
   */
  public String getId() {
    return this.id;
  } 

  /**
   * @since   1.2.0
   */
  public String getMemberUuid() {
    return this.memberUUID;
  }

  /**
   * @since   1.2.0
   */
  public PrivilegeCache getNamingCache() {
    // FIXME 20070416 why is this in this class?
    if (this.namingCache == null) {
      this.setNamingCache( BasePrivilegeCache.getCache( GrouperConfig.getProperty(GrouperConfig.PNCI) ) );
      DebugLog.info( KLASS, "using naming cache: " + this.namingCache.getClass().getName() );
    }
    return this.namingCache;
  }

  /**
   * @since   1.2.0
   */
  public Date getStartTime() {
    return this.startTime;
  }

  /**
   * @since   1.2.0
   */
  public Subject getSubject() {
    return this.subject;
  }

  /**
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

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
  public GrouperSessionDTO setAccessCache(PrivilegeCache accessCache) {
    this.accessCache = accessCache;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GrouperSessionDTO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GrouperSessionDTO setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GrouperSessionDTO setNamingCache(PrivilegeCache namingCache) {
    this.namingCache = namingCache;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GrouperSessionDTO setStartTime(Date startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GrouperSessionDTO setSubject(Subject subject) {
    this.subject = subject;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GrouperSessionDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

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
  // FIXME 20070416 visibility
  public GrouperDAO getDAO() {
    return GrouperDAOFactory.getFactory().getGrouperSession()
      .setId( this.getId() )
      .setMemberUuid( this.getMemberUuid() )
      .setStartTime( this.getStartTime() )
      .setUuid( this.getUuid() )
      ;
  }

} 

