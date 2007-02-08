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
import  org.apache.commons.lang.builder.*;

/** 
 * {@link Member} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberDTO.java,v 1.1 2007-02-08 16:25:25 blair Exp $
 */
class MemberDTO extends BaseGrouperDTO {

  // PRIVATE CLASS CONSTANTS //
  private static final Class KLASS = MemberDTO.class;


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
      .append( this.getMemberUuid(),      that.getMemberUuid()      )
      .append( this.getSubjectId(),       that.getSubjectId()       )
      .append( this.getSubjectSourceId(), that.getSubjectSourceId() )
      .append( this.getSubjectTypeId(),   that.getSubjectTypeId()   )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getMemberUuid()      )
      .append( this.getSubjectId()       )
      .append( this.getSubjectSourceId() )
      .append( this.getSubjectTypeId()   )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "memberUuid",      this.getMemberUuid()      )
      .append( "subjectId",       this.getSubjectId()       )
      .append( "subjectSourceId", this.getSubjectSourceId() )
      .append( "subjectTypeId",   this.getSubjectTypeId()   )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected HibernateMemberDAO getDAO() {
    HibernateMemberDAO dao = new HibernateMemberDAO();
    dao.setId( this.getId() );
    dao.setMemberUuid( this.getMemberUuid() );
    dao.setSubjectId( this.getSubjectId() );
    dao.setSubjectSourceId( this.getSubjectSourceId() );
    dao.setSubjectTypeId( this.getSubjectTypeId() );
    return dao;
  } // protected HibernateMemberDAO getDAO()


  // GETTERS //

  protected String getId() {
    return this.id;
  }
  protected String getMemberUuid() {
    return this.memberUUID;
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
    

  // SETTERS //

  protected void setId(String id) {
    this.id = id;
  }
  protected void setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
  }
  protected void setSubjectId(String subjectID) {
    this.subjectID = subjectID;
  }
  protected void setSubjectSourceId(String subjectSourceID) {
    this.subjectSourceID = subjectSourceID;
  }
  protected void setSubjectTypeId(String subjectTypeID) {
    this.subjectTypeID = subjectTypeID;
  }

} // class MemberDTO extends BaseGrouperDTO

