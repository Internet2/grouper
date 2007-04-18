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
import  edu.internet2.middleware.grouper.internal.dao.StemDAO;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link Stem} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: StemDTO.java,v 1.3 2007-04-18 15:02:11 blair Exp $
 */
public class StemDTO implements GrouperDTO {

  // PRIVATE INSTANCE VARIABLES //
  private String  createSource;
  private long    createTime;
  private String  creatorUUID;
  private String  description;
  private String  displayExtension;
  private String  displayName;
  private String  extension;
  private String  id;
  private String  modifierUUID;
  private String  modifySource;
  private long    modifyTime;
  private String  name;
  private String  parentUUID;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */  
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof StemDTO)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.getUuid(), ( (StemDTO) other ).getUuid() )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public String getCreateSource() {
    return this.createSource;
  }

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
    return GrouperDAOFactory.getFactory().getStem()
      .setCreateSource( this.getCreateSource() )
      .setCreateTime( this.getCreateTime() )
      .setCreatorUuid( this.getCreatorUuid() )
      .setDescription( this.getDescription() )
      .setDisplayExtension( this.getDisplayExtension() )
      .setDisplayName( this.getDisplayName() )
      .setExtension( this.getExtension() )
      .setId( this.getId() )
      .setModifierUuid( this.getModifierUuid() )
      .setModifySource( this.getModifySource() )
      .setModifyTime( this.getModifyTime() )
      .setName( this.getName() )
      .setUuid( this.getUuid() )
      .setParentUuid( this.getParentUuid() )
      ;
  }

  /**
   * @since   1.2.0
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * @since   1.2.0
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * @since   1.2.0
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * @since   1.2.0
   */
  public String getExtension() {
    return this.extension;
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
  public String getModifierUuid() {
    return this.modifierUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getModifySource() {
    return this.modifySource;
  }

  /**
   * @since   1.2.0
   */
  public long getModifyTime() {
    return this.modifyTime;
  }

  /**
   * @since   1.2.0
   */
  public String getName() {
    return this.name;
  }

  /**
   * @since   1.2.0
   */
  public String getParentUuid() {
    return this.parentUUID;
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
  public StemDTO setCreateSource(String createSource) {
    this.createSource = createSource;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setDisplayExtension(String displayExtension) {
    this.displayExtension = displayExtension;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setExtension(String extension) {
    this.extension = extension;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setModifySource(String modifySource) {
    this.modifySource = modifySource;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "createSource",     this.getCreateSource()     )
      .append( "createTime",       this.getCreateTime()       )
      .append( "creatorUuid",      this.getCreatorUuid()      )
      .append( "description",      this.getDescription()      )
      .append( "displayExtension", this.getDisplayExtension() )
      .append( "displayName",      this.getDisplayName()      )
      .append( "extension",        this.getExtension()        )
      .append( "modifierUuid",     this.getModifierUuid()     )
      .append( "modifySource",     this.getModifySource()     )
      .append( "modifyTime",       this.getModifyTime()       )
      .append( "name",             this.getName()             )
      .append( "ownerUuid",        this.getUuid()             )
      .append( "parentUuid",       this.getParentUuid()       )
      .toString();
  } // public String toString()


  // PUBLIC CLASS METHODS //
 
  /**
   * @since   1.2.0
   */
  public static StemDTO getDTO(StemDAO dao) {
    return new StemDTO()
      .setCreateSource( dao.getCreateSource() )
      .setCreateTime( dao.getCreateTime() )
      .setCreatorUuid( dao.getCreatorUuid() )
      .setDescription( dao.getDescription() )
      .setDisplayExtension( dao.getDisplayExtension() )
      .setDisplayName( dao.getDisplayName() )
      .setExtension( dao.getExtension() )
      .setId( dao.getId() )
      .setModifierUuid( dao.getModifierUuid() )
      .setModifySource( dao.getModifySource() )
      .setModifyTime( dao.getModifyTime() )
      .setName( dao.getName() )
      .setUuid( dao.getUuid() )
      .setParentUuid( dao.getParentUuid() )
      ;
  }

} 

