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
import  java.util.Collection;
import  java.util.Date;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link Membership} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipDTO.java,v 1.4 2007-03-14 19:54:08 blair Exp $
 */
class MembershipDTO extends BaseGrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private long    createTime      = new Date().getTime();           // reasonable default
  private String  creatorUUID;
  private int     depth           = 0;                              // reasonable default
  private String  id;
  private String  listName;
  private String  listType;
  private String  memberUUID;
  private String  ownerUUID;
  private String  parentUUID      = null;                           // reasonable default
  private String  type            = Membership.IMMEDIATE;           // reasonable default
  private String  membershipUUID  = GrouperUuid.internal_getUuid(); // reasonable default
  private String  viaUUID         = null;                           // reasonable default


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */  
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof MembershipDTO)) {
      return false;
    }
    MembershipDTO that = (MembershipDTO) other;
    return new EqualsBuilder()
      .append( this.getDepth(),          that.getDepth()          )
      .append( this.getListName(),       that.getListName()       )
      .append( this.getListType(),       that.getListType()       )
      .append( this.getMemberUuid(),     that.getMemberUuid()     )
      .append( this.getMembershipUuid(), that.getMembershipUuid() )
      .append( this.getOwnerUuid(),      that.getOwnerUuid()      )
      .append( this.getViaUuid(),        that.getViaUuid()        )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getDepth()          )
      .append( this.getListName()       )
      .append( this.getListType()       )
      .append( this.getMemberUuid()     )
      .append( this.getMembershipUuid() )
      .append( this.getOwnerUuid()      )
      .append( this.getViaUuid()        )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "createTime",      this.getCreateTime()      )
      .append( "creatorUuid",     this.getCreatorUuid()     )
      .append( "depth",           this.getDepth()           )
      .append( "id",              this.getId()              )
      .append( "listName",        this.getListName()        )
      .append( "listType",        this.getListType()        )
      .append( "memberUuid",      this.getMemberUuid()      )
      .append( "ownerUuid",       this.getOwnerUuid()       )
      .append( "parentUuid",      this.getParentUuid()      )
      .append( "type",            this.getType()            )
      .append( "membershipUuid",  this.getMembershipUuid()  )
      .append( "viaUuid",         this.getViaUuid()         )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Collection getDTO(Collection c) {
    Set       mships  = new LinkedHashSet();
    Iterator it       = c.iterator();
    while ( it.hasNext() ) {
      mships.add( getDTO( (HibernateMembershipDAO) it.next() ) );
    }
    return mships;
    
  } // protected static Collection getDTO(c)

  // @since   1.2.0
  protected static MembershipDTO getDTO(HibernateMembershipDAO dao) {
    MembershipDTO dto = new MembershipDTO();
    dto.setCreateTime( dao.getCreateTime() );
    dto.setCreatorUuid( dao.getCreatorUuid() );
    dto.setDepth( dao.getDepth() );
    dto.setId( dao.getId() );
    dto.setListName( dao.getListName() );
    dto.setListType( dao.getListType() );
    dto.setMemberUuid( dao.getMemberUuid() );
    dto.setOwnerUuid( dao.getOwnerUuid() );
    dto.setParentUuid( dao.getParentUuid() );
    dto.setType( dao.getType() );
    dto.setMembershipUuid( dao.getMembershipUuid() );
    dto.setViaUuid( dao.getViaUuid() );
    return dto;
  } // protected static MembershipDTO getDTO(dao)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected HibernateMembershipDAO getDAO() {
    HibernateMembershipDAO dao = new HibernateMembershipDAO();
    dao.setCreateTime( this.getCreateTime() );
    dao.setCreatorUuid( this.getCreatorUuid() );
    dao.setDepth( this.getDepth() );
    dao.setId( this.getId() );
    dao.setListName( this.getListName() );
    dao.setListType( this.getListType() );
    dao.setMemberUuid( this.getMemberUuid() );
    dao.setOwnerUuid( this.getOwnerUuid() );
    dao.setParentUuid( this.getParentUuid() );
    dao.setType( this.getType() );
    dao.setMembershipUuid( this.getMembershipUuid() );
    dao.setViaUuid( this.getViaUuid() );
    return dao;
  } // protected HibernateMembershipDAO getDAO()


  // GETTERS //
  
  protected long getCreateTime() {
    return this.createTime;
  }
  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected int getDepth() {
    return this.depth;
  }
  protected String getId() {
    return this.id;
  }
  protected String getListName() {
    return this.listName;
  }
  protected String getListType() {
    return this.listType;
  }
  protected String getMemberUuid() {
    return this.memberUUID;
  }
  protected String getOwnerUuid() {
    return this.ownerUUID;
  }
  protected String getParentUuid() {
    return this.parentUUID;
  }
  protected String getType() {
    return this.type;
  }
  protected String getMembershipUuid() {
    return this.membershipUUID;
  }
  protected String getViaUuid() {
    return this.viaUUID;
  }


  // SETTERS //

  protected void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  protected void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }
  protected void setDepth(int depth) {
    this.depth = depth;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setListName(String listName) {
    this.listName = listName;
  }
  protected void setListType(String listType) {
    this.listType = listType;
  }
  protected void setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
  }
  protected void setOwnerUuid(String ownerUUID) {
    this.ownerUUID = ownerUUID;
  }
  protected void setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
  }
  protected void setType(String type) {
    this.type = type;
  }
  protected void setMembershipUuid(String membershipUUID) {
    this.membershipUUID = membershipUUID;
  }
  protected void setViaUuid(String viaUUID) {
    this.viaUUID = viaUUID;
  }

} // class MembershipDTO extends BaseGrouperDTO

