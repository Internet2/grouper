/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Schema specification for a Group attribute or list.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Field.java,v 1.26 2008-06-25 05:46:05 mchyzer Exp $    
 */
public class Field extends GrouperAPI implements Serializable {


  private GroupType cachedGroupType   = null;
  // PRIVATE INSTANCE VARIABLES //
  private String    groupTypeUUID;
  private String    id;
  private boolean   isNullable;
  private String    name;
  private String    readPrivilege;
  private String    type;
  private String    uuid;
  private String    writePrivilege;
  public  static final  long      serialVersionUID  = 2072790175332537149L;


  // PUBLIC INSTANCE METHODS //

  /**
   */
  public GroupType getGroupType() 
    throws  IllegalStateException
  {
    if ( this.cachedGroupType == null ) {
      try {
        GroupType type = GrouperDAOFactory.getFactory().getGroupType().findByUuid( this.getGroupTypeUuid() ) ;
        this.cachedGroupType = type;
      }
      catch (SchemaException eS) {
        throw new IllegalStateException( "unable to fetch GroupType: " + eS.getMessage() );
      }
    }
    return this.cachedGroupType;
  } 

  /**
   */
  public FieldType getType() {
    return FieldType.getInstance( this.getTypeString() );
  } // public FieldType getType()

  /**
   */
  public Privilege getReadPriv() {
    return Privilege.getInstance( this.getReadPrivilege() ); 
  } // public Privilege getReadPriv()

  /**
   */
  public boolean getRequired() {
    return !this.getIsNullable();
  } // public boolean isRequired()

  /**
   */
  public Privilege getWritePriv() {
    return Privilege.getInstance( this.getWritePrivilege() );
  } // public Privilege getWritePriv()

  /**
   * @since   1.2.0
   */
  public boolean equals(Object other) {
    if (this == other) { 
      return true;
    }
    if ( !(other instanceof Field) ) {
      return false;
    }
    Field that = (Field) other;
    return new EqualsBuilder()
      .append( this.getUuid(), that.getUuid() )
     .isEquals();
  } // public boolean equals(other)

  /**
   * @since   1.2.0
   */
  public String getGroupTypeUuid() {
    return this.groupTypeUUID;
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
  public boolean getIsNullable() {
    return this.isNullable;
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
  public String getReadPrivilege() {
    return this.readPrivilege;
  }

  /**
   * @since   1.2.0
   */
  public String getTypeString() {
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
  public String getWritePrivilege() {
    return this.writePrivilege;
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
  public void setGroupTypeUuid(String groupTypeUUID) {
    this.groupTypeUUID = groupTypeUUID;
  }

  /**
   * @since   1.2.0
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @since   1.2.0
   */
  public void setIsNullable(boolean isNullable) {
    this.isNullable = isNullable;
  }

  /**
   * @since   1.2.0
   */
  public void setName(String name) {
    this.name = name;
  
  }

  /**
   * @since   1.2.0
   */
  public void setReadPrivilege(Privilege readPrivilege) {
    this.setReadPrivilege( readPrivilege.getName() );
  
  }

  /**
   * @since   1.2.0
   */
  public void setReadPrivilege(String readPrivilege) {
    this.readPrivilege = readPrivilege;
  
  }

  /**
   * @since   1.2.0
   */
  public void setType(FieldType type) {
    this.setTypeString( type.toString() );
  
  }

  /**
   * @since   1.2.0
   */
  public void setTypeString(String type) {
    this.type = type;
  
  }

  /**
   * @since   1.2.0
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  
  }

  /**
   * @since   1.2.0
   */
  public void setWritePrivilege(Privilege writePrivilege) {
    this.setWritePrivilege( writePrivilege.getName() );
  
  }

  /**
   * @since   1.2.0
   */
  public void setWritePrivilege(String writePrivilege) {
    this.writePrivilege = writePrivilege;
  
  }

  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "groupTypeUuid",  this.getGroupTypeUuid()  )
      .append( "isNullable",     this.getIsNullable()     )
      .append( "name",           this.getName()           )
      .append( "readPrivilege",  this.getReadPrivilege()  )
      .append( "type",           this.getType()           )
      .append( "uuid",           this.getUuid()           )
      .append( "writePrivilege", this.getWritePrivilege() )
      .toString();
  } // public String toString()

} // public class Field extends GrouperAPI implements Serializable

