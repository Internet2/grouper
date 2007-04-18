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
import  edu.internet2.middleware.grouper.internal.dao.CompositeDAO;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link Composite} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CompositeDTO.java,v 1.3 2007-04-18 15:02:11 blair Exp $
 */
public class CompositeDTO implements GrouperDTO {

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
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   1.2.0
   */
  public GrouperDAO getDAO() {
    return GrouperDAOFactory.getFactory().getComposite()
      .setCreateTime( this.getCreateTime() )
      .setCreatorUuid( this.getCreatorUuid() )
      .setFactorOwnerUuid( this.getFactorOwnerUuid() )
      .setId( this.getId() )
      .setLeftFactorUuid( this.getLeftFactorUuid() )
      .setUuid( this.getUuid() )
      .setRightFactorUuid( this.getRightFactorUuid() )
      .setType( this.getType() )
      ;
  } 

  /**
   * @since   1.2.0
   */
  public String getFactorOwnerUuid() {
    return this.factorOwnerUUID;
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
  public String getLeftFactorUuid() {
    return this.leftFactorUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getRightFactorUuid() {
    return this.rightFactorUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getType() {
    return this.type;
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
      .append( this.getUuid() )
      .toHashCode();
  } // public int hashCode()


  /**
   * @since   1.2.0
   */
  public CompositeDTO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public CompositeDTO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public CompositeDTO setFactorOwnerUuid(String factorOwnerUUID) {
    this.factorOwnerUUID = factorOwnerUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public CompositeDTO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public CompositeDTO setLeftFactorUuid(String leftFactorUUID) {
    this.leftFactorUUID = leftFactorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public CompositeDTO setRightFactorUuid(String rightFactorUUID) {
    this.rightFactorUUID = rightFactorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public CompositeDTO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public CompositeDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

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
  }


  // PUBLIC CLASS METHODS //

  // @since   1.2.0
  public static CompositeDTO getDTO(CompositeDAO dao) {
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
  } 

} 

