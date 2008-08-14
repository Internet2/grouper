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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;

/**
 * Basic Hibernate <code>Attribute</code> DTO interface.
 * @author  blair christensen.
 * @version $Id: Attribute.java,v 1.19 2008-08-14 06:35:47 mchyzer Exp $
 * @since   @HEAD@
 */
@GrouperIgnoreDbVersion @GrouperIgnoreClone
public class Attribute extends GrouperAPI implements Hib3GrouperVersioned {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";

  /** constant for field name for: groupUUID */
  public static final String FIELD_GROUP_UUID = "groupUUID";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: value */
  public static final String FIELD_VALUE = "value";

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  // PRIVATE INSTANCE VARIABLES //
  
  /** id of the field which is the attribute name */
  private String  fieldId;

  private String  groupUUID;
  private String  id;
  private String  value;

  /**
   * 
   */
  public static final String TABLE_GROUPER_ATTRIBUTES = "grouper_attributes";

  /** column field_id col in db */
  public static final String COLUMN_FIELD_ID = "field_id";

  /** column field_name col in db */
  public static final String COLUMN_FIELD_NAME = "field_name";

  /** column old_field_name col in db */
  public static final String COLUMN_OLD_FIELD_NAME = "old_field_name";



  // PUBLIC INSTANCE METHODS //

  /**
   * @since   @HEAD@
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Attribute)) {
      return false;
    }
    Attribute that = (Attribute) other;
    return new EqualsBuilder()
      .append( this.getAttrName(),  that.getAttrName()  )
      .append( this.getGroupUuid(), that.getGroupUuid() )
      .append( this.getValue(),     that.getValue()     )
      .isEquals();
  } // public boolean equals(other)
  
  /**
   * @since   @HEAD@
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getAttrName()  )
      .append( this.getGroupUuid() )
      .append( this.getValue()     )
      .toHashCode();
  } // public int hashCode()

  /**
   * @since   @HEAD@
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "attrName",  this.getAttrName()  )
      .append( "groupUuid", this.getGroupUuid() )
      .append( "id",        this.getId()        )
      .append( "value",     this.getValue()     )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //

  // @since   @HEAD@
  public String getAttrName() {
    if (StringUtils.isBlank(this.fieldId)) {
      return null;
    }
    Field field = FieldFinder.findById(this.fieldId);
    if (!field.isAttributeName()) {
      throw new RuntimeException("Field is not an attribute name, id: " + this.fieldId
          + ", instead it is: " + field.getTypeString());
    }
    return field.getName();
  }
  // @since   @HEAD@
  public String getGroupUuid() {
    return this.groupUUID;
  }
  // @since   @HEAD@
  public String getId() {
    return this.id;
  }
  // @since   @HEAD@
  public String getValue() {
    return this.value;
  }

  // @since   @HEAD@
  public void setGroupUuid(String groupUUID) {
    this.groupUUID = groupUUID;
  }
  // @since   @HEAD@
  public void setId(String id) {
    this.id = id;
  }
  // @since   @HEAD@
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public Attribute clone() {
    throw new RuntimeException("Clone not supported");
  }

  /**
   * id of the field which is the attribute name
   * @return the field id
   */
  public String getFieldId() {
    return fieldId;
  }

  /**
   * id of the field which is the attribute name
   * @param fieldId1
   */
  public void setFieldId(String fieldId1) {
    this.fieldId = fieldId1;
  }

} 

