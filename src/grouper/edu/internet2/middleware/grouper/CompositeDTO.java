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
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link Composite} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CompositeDTO.java,v 1.6 2007-04-05 14:28:28 blair Exp $
 */
class CompositeDTO extends BaseGrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private long    createTime;
  private String  creatorUUID;
  private String  factorOwnerUUID;
  private String  id;
  private String  leftFactorUUID;
  private String  rightFactorUUID;
  private String  type;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */  
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof CompositeDTO)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.getUuid(), ( (CompositeDTO) other ).getUuid() )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getUuid() )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "createTime",      this.getCreateTime()        )
      .append( "creatorUuid",     this.getCreatorUuid()       )
      .append( "factorUuid",      this.getFactorOwnerUuid()   )
      .append( "leftFactorUuid",  this.getLeftFactorUuid()    )
      .append( "ownerUuid",       this.getUuid()              )
      .append( "rightFactorUuid", this.getRightFactorUuid()   )
      .append( "type",            this.getType()              )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Collection getDTO(Collection c) {
    Set       composites  = new LinkedHashSet();
    Iterator  it          = c.iterator();
    while ( it.hasNext() ) {
      composites.add( getDTO( (HibernateCompositeDAO) it.next() ) );
    }
    return composites;
    
  } // protected static Collection getDTO(c)

  // @since   1.2.0
  protected static CompositeDTO getDTO(HibernateCompositeDAO dao) {
    return new CompositeDTO()
      .setCreateTime( dao.getCreateTime() )
      .setCreatorUuid( dao.getCreatorUuid() )
      .setFactorOwnerUuid( dao.getFactorOwnerUuid() )
      .setId( dao.getId() )
      .setLeftFactorUuid( dao.getLeftFactorUuid() )
      .setUuid( dao.getUuid() )
      .setRightFactorUuid( dao.getRightFactorUuid() )
      .setType( dao.getType() )
      ;
  } // protected static CompositeDTO getDTO(dao)


  // PROTECTED INSTANCE METHODS //
  
  // @since   1.2.0
  protected HibernateCompositeDAO getDAO() {
    return new HibernateCompositeDAO()
      .setCreateTime( this.getCreateTime() )
      .setCreatorUuid( this.getCreatorUuid() )
      .setFactorOwnerUuid( this.getFactorOwnerUuid() )
      .setId( this.getId() )
      .setLeftFactorUuid( this.getLeftFactorUuid() )
      .setUuid( this.getUuid() )
      .setRightFactorUuid( this.getRightFactorUuid() )
      .setType( this.getType() )
      ;
  } // protected HibernateCompositeDAO getDAO()


  // GETTERS //

  protected long getCreateTime() {
    return this.createTime;
  }
  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected String getFactorOwnerUuid() {
    return this.factorOwnerUUID;
  }
  protected String getId() {
    return this.id;
  }
  protected String getLeftFactorUuid() {
    return this.leftFactorUUID;
  }
  protected String getRightFactorUuid() {
    return this.rightFactorUUID;
  }
  protected String getType() {
    return this.type;
  }
  protected String getUuid() {
    return this.uuid;
  }


  // SETTERS //

  protected CompositeDTO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }
  protected CompositeDTO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }
  protected CompositeDTO setFactorOwnerUuid(String factorOwnerUUID) {
    this.factorOwnerUUID = factorOwnerUUID;
    return this;
  }
  protected CompositeDTO setId(String id) {
    this.id = id;
    return this;
  }
  protected CompositeDTO setLeftFactorUuid(String leftFactorUUID) {
    this.leftFactorUUID = leftFactorUUID;
    return this;
  }
  protected CompositeDTO setRightFactorUuid(String rightFactorUUID) {
    this.rightFactorUUID = rightFactorUUID;
    return this;
  }
  protected CompositeDTO setType(String type) {
    this.type = type;
    return this;
  }
  protected CompositeDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

} // class CompositeDTO extends BaseGrouperDTO

