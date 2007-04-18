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
import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link Member} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberDTO.java,v 1.2 2007-04-18 14:31:59 blair Exp $
 */
public class MemberDTO implements GrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String  id;
  private String  memberUUID;
  private String  subjectID;
  private String  subjectSourceID;
  private String  subjectTypeID;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */  
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof MemberDTO)) {
      return false;
    }
    MemberDTO that = (MemberDTO) other;
    return new EqualsBuilder()
      .append( this.getSubjectId(),       that.getSubjectId()       )
      .append( this.getSubjectSourceId(), that.getSubjectSourceId() )
      .append( this.getSubjectTypeId(),   that.getSubjectTypeId()   )
      .append( this.getUuid(),            that.getUuid()            )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   1.2.0
   */
  public String getSubjectId() {
    return this.subjectID;
  }

  /**
   * @since   1.2.0
   */
  public String getSubjectSourceId() {
    return this.subjectSourceID;
  }

  /**
   * @since   1.2.0
   */
  public String getSubjectTypeId() {
    return this.subjectTypeID;
  }

  /**
   * @since   1.2.0
   */
  public String getUuid() {
    return this.memberUUID;
  }
    
  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getSubjectId()       )
      .append( this.getSubjectSourceId() )
      .append( this.getSubjectTypeId()   )
      .append( this.getUuid()            )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public MemberDTO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public MemberDTO setSubjectId(String subjectID) {
    this.subjectID = subjectID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public MemberDTO setSubjectSourceId(String subjectSourceID) {
    this.subjectSourceID = subjectSourceID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public MemberDTO setSubjectTypeId(String subjectTypeID) {
    this.subjectTypeID = subjectTypeID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public MemberDTO setUuid(String memberUUID) {
    this.memberUUID = memberUUID;
   return this;
  }

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "subjectId",       this.getSubjectId()       )
      .append( "subjectSourceId", this.getSubjectSourceId() )
      .append( "subjectTypeId",   this.getSubjectTypeId()   )
      .append( "uuid",            this.getUuid()            )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  // FIXME 20070416 visibility
  public GrouperDAO getDAO() {
    return GrouperDAOFactory.getFactory().getMember()
      .setId( this.getId() )
      .setSubjectId( this.getSubjectId() )
      .setSubjectSourceId( this.getSubjectSourceId() )
      .setSubjectTypeId( this.getSubjectTypeId() )
      .setUuid( this.getUuid() )
      ;
  }

} 

