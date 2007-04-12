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
 * @version $Id: StemDTO.java,v 1.6 2007-04-12 15:40:41 blair Exp $
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
    return new EqualsBuilder()
      .append( this.getUuid(), ( (StemDTO) other ).getUuid() )
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
  protected static Collection getDTO(Collection c) {
    Set       stems = new LinkedHashSet();
    Iterator  it    = c.iterator();
    while ( it.hasNext() ) {
      stems.add( getDTO( (HibernateStemDAO) it.next() ) );
    }
    return stems;
  } // protected static Collection getDTO(c)

  // @since   1.2.0
  protected static StemDTO getDTO(HibernateStemDAO dao) {
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
  } // protected static StemDTO getDTO(dao)


  // PROTECTED INSTANCE METHODS //
  
  // @since   1.2.0
  protected GrouperDAO getDAO() {
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

  protected StemDTO setCreateSource(String createSource) {
    this.createSource = createSource;
    return this;
  }
  protected StemDTO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }
  protected StemDTO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }
  protected StemDTO setDescription(String description) {
    this.description = description;
    return this;
  }
  protected StemDTO setDisplayExtension(String displayExtension) {
    this.displayExtension = displayExtension;
    return this;
  }
  protected StemDTO setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }
  protected StemDTO setExtension(String extension) {
    this.extension = extension;
    return this;
  }
  protected StemDTO setId(String id) {
    this.id = id;
    return this;
  }
  protected StemDTO setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
    return this;
  }
  protected StemDTO setModifySource(String modifySource) {
    this.modifySource = modifySource;
    return this;
  }
  protected StemDTO setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
    return this;
  }
  protected StemDTO setName(String name) {
    this.name = name;
    return this;
  }
  protected StemDTO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }
  protected StemDTO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

} // class StemDTO extends BaseGrouperDTO

