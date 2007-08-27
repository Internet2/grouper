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
import  edu.internet2.middleware.grouper.internal.dto.FieldDTO;
import  java.io.Serializable;


/** 
 * Schema specification for a Group attribute or list.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Field.java,v 1.25 2007-08-27 17:49:26 blair Exp $    
 */
public class Field extends GrouperAPI implements Serializable {


  private               GroupType cachedGroupType   = null;
  public  static final  long      serialVersionUID  = 2072790175332537149L;


  // PUBLIC INSTANCE METHODS //

  /**
   */
  public boolean equals(Object other) {
    if (this == other) { 
      return true;
    }
    if (!(other instanceof Field)) {
      return false;
    }
    return this.getDTO().equals( ( (Field) other ).getDTO() );
  } // public boolean equals(other)

  /**
   */
  public GroupType getGroupType() 
    throws  IllegalStateException
  {
    if ( this.cachedGroupType == null ) {
      try {
        GroupType type = new GroupType();
        type.setDTO( GrouperDAOFactory.getFactory().getGroupType().findByUuid( this._getDTO().getGroupTypeUuid() ) );
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
    return FieldType.getInstance( this._getDTO().getType() );
  } // public FieldType getType()

  /**
   */
  public String getName() {
    return this._getDTO().getName();
  } // public String getName()

  /**
   */
  public Privilege getReadPriv() {
    return Privilege.getInstance( this._getDTO().getReadPrivilege() ); 
  } // public Privilege getReadPriv()

  /**
   */
  public boolean getRequired() {
    return !this._getDTO().getIsNullable();
  } // public boolean isRequired()

  /**
   */
  public Privilege getWritePriv() {
    return Privilege.getInstance( this._getDTO().getWritePrivilege() );
  } // public Privilege getWritePriv()

  /**
   */
  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  /**
   */
  public String toString() {
    return this.getDTO().toString();
  } // public String toString()


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private FieldDTO _getDTO() {
    return (FieldDTO) super.getDTO();
  }

} // public class Field extends GrouperAPI implements Serializable

