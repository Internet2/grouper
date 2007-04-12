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
import  org.apache.commons.lang.builder.*;

/** 
 * {@link Member} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberDTO.java,v 1.5 2007-04-12 15:40:41 blair Exp $
 */
class MemberDTO extends BaseGrouperDTO {

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
  protected GrouperDAO getDAO() {
    return GrouperDAOFactory.getFactory().getMember()
      .setId( this.getId() )
      .setSubjectId( this.getSubjectId() )
      .setSubjectSourceId( this.getSubjectSourceId() )
      .setSubjectTypeId( this.getSubjectTypeId() )
      .setUuid( this.getUuid() )
      ;
  }


  // GETTERS //

  protected String getId() {
    return this.id;
  }
  protected String getSubjectId() {
    return this.subjectID;
  }
  protected String getSubjectSourceId() {
    return this.subjectSourceID;
  }
  protected String getSubjectTypeId() {
    return this.subjectTypeID;
  }
  protected String getUuid() {
    return this.memberUUID;
  }
    

  // SETTERS //

  protected MemberDTO setId(String id) {
    this.id = id;
    return this;
  }
  protected MemberDTO setSubjectId(String subjectID) {
    this.subjectID = subjectID;
    return this;
  }
  protected MemberDTO setSubjectSourceId(String subjectSourceID) {
    this.subjectSourceID = subjectSourceID;
    return this;
  }
  protected MemberDTO setSubjectTypeId(String subjectTypeID) {
    this.subjectTypeID = subjectTypeID;
    return this;
  }
  protected MemberDTO setUuid(String memberUUID) {
    this.memberUUID = memberUUID;
   return this;
  }

} // class MemberDTO extends BaseGrouperDTO

