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
 * {@link Stem} DTO class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: StemDTO.java,v 1.2 2007-02-14 17:06:28 blair Exp $
 */
class StemDTO extends BaseGrouperDTO {

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
    StemDTO that = (StemDTO) other;
    // TODO 20070201 should i use more than this?
    return new EqualsBuilder()
      .append( this.getUuid(), that.getUuid() )
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


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // TODO 20070125 this doesn't fit with everything else
  protected static Collection getDTO(Collection c) {
    Set       stems = new LinkedHashSet();
    Iterator  it    = c.iterator();
    while ( it.hasNext() ) {
      stems.add( getDTO( (HibernateStemDAO) it.next() ) );
    }
    return stems;
  } // protected static Collection getDTO(c)

  // @since   1.2.0
  // TODO 20070125 this doesn't fit with everything else
  protected static StemDTO getDTO(HibernateStemDAO dao) {
    StemDTO dto = new StemDTO();
    dto.setCreateSource( dao.getCreateSource() );
    dto.setCreateTime( dao.getCreateTime() );
    dto.setCreatorUuid( dao.getCreatorUuid() );
    dto.setDescription( dao.getDescription() );
    dto.setDisplayExtension( dao.getDisplayExtension() );
    dto.setDisplayName( dao.getDisplayName() );
    dto.setExtension( dao.getExtension() );
    dto.setId( dao.getId() );
    dto.setModifierUuid( dao.getModifierUuid() );
    dto.setModifySource( dao.getModifySource() );
    dto.setModifyTime( dao.getModifyTime() );
    dto.setName( dao.getName() );
    dto.setUuid( dao.getUuid() );
    dto.setParentUuid( dao.getParentUuid() );
    return dto;
  } // protected static StemDTO getDTO(dao)


  // PROTECTED INSTANCE METHODS //
  
  // @since   1.2.0
  // TODO 20070125 is this the direction i want to take?
  protected HibernateStemDAO getDAO() {
    HibernateStemDAO dao = new HibernateStemDAO();
    dao.setCreateSource( this.getCreateSource() );
    dao.setCreateTime( this.getCreateTime() );
    dao.setCreatorUuid( this.getCreatorUuid() );
    dao.setDescription( this.getDescription() );
    dao.setDisplayExtension( this.getDisplayExtension() );
    dao.setDisplayName( this.getDisplayName() );
    dao.setExtension( this.getExtension() );
    dao.setId( this.getId() );
    dao.setModifierUuid( this.getModifierUuid() );
    dao.setModifySource( this.getModifySource() );
    dao.setModifyTime( this.getModifyTime() );
    dao.setName( this.getName() );
    dao.setUuid( this.getUuid() );
    dao.setParentUuid( this.getParentUuid() );
    return dao;
  } // protected HibernateStemDAO getDAO()


  // GETTERS //

  protected String getCreateSource() {
    return this.createSource;
  }
  protected long getCreateTime() {
    return this.createTime;
  }
  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected String getDescription() {
    return this.description;
  }
  protected String getDisplayExtension() {
    return this.displayExtension;
  }
  protected String getDisplayName() {
    return this.displayName;
  }
  protected String getExtension() {
    return this.extension;
  }
  protected String getId() {
    return this.id;
  }
  protected String getModifierUuid() {
    return this.modifierUUID;
  }
  protected String getModifySource() {
    return this.modifySource;
  }
  protected long getModifyTime() {
    return this.modifyTime;
  }
  protected String getName() {
    return this.name;
  }
  protected String getParentUuid() {
    return this.parentUUID;
  }
  protected String getUuid() {
    return this.uuid;
  }


  // SETTERS //

  protected void setCreateSource(String createSource) {
    this.createSource = createSource;
  }
  protected void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  protected void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }
  protected void setDescription(String description) {
    this.description = description;
  }
  protected void setDisplayExtension(String displayExtension) {
    this.displayExtension = displayExtension;
  }
  protected void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  protected void setExtension(String extension) {
    this.extension = extension;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
  }
  protected void setModifySource(String modifySource) {
    this.modifySource = modifySource;
  }
  protected void setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
  }
  protected void setName(String name) {
    this.name = name;
  }
  protected void setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
  }
  protected void setUuid(String uuid) {
    this.uuid = uuid;
  }

} // class StemDTO extends BaseGrouperDTO

