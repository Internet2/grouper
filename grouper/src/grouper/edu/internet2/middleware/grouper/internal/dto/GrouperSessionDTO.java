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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperDAOFactory;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import edu.internet2.middleware.subject.Subject;

/** 
 * Basic <code>GrouperSession</code> DTO.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: GrouperSessionDTO.java,v 1.10.4.1 2008-03-19 18:46:11 mchyzer Exp $
 * @since   1.2.0
 */
public class GrouperSessionDTO implements GrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String          memberUUID;
  private long            startTime;
  private Subject         subject;
  private String          uuid;
  private long hibernateVersion = -1;


  
  /**
   * @return the hibernateVersion
   */
  public long getHibernateVersion() {
    return this.hibernateVersion;
  }

  
  /**
   * @param hibernateVersion the hibernateSession to set
   */
  public GrouperSessionDTO setHibernateVersion(long hibernateVersion) {
    this.hibernateVersion = hibernateVersion;
    return this;
  }

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
  }
  
  /**
   * @since   1.2.0
   */
  public GrouperDAO getDAO() {
    return GrouperDAOFactory.getFactory().getGrouperSession()
      .setHibernateVersion(this.getHibernateVersion())
      .setMemberUuid( this.getMemberUuid() )
      .setStartTime( this.getStartTime() )
      .setUuid( this.getUuid() )
      ;
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
  public long getStartTime() {
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
  public GrouperSessionDTO setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GrouperSessionDTO setStartTime(long startTime) {
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
  }

} 

