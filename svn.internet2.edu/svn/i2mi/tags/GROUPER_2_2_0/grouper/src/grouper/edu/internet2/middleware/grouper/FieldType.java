/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Field Type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldType.java,v 1.15 2009-09-21 06:14:27 mchyzer Exp $    
 */
public enum FieldType implements Serializable {

  /** */
  ACCESS("access"),
  
  /** */
  ATTRIBUTE_DEF("attributeDef"),
  
  /** */
  LIST("list"),

  /** */
  NAMING("naming");
  
  /**
   * 
   * @param type
   * @param exceptionOnNull
   * @return field type
   */
  public static FieldType valueOfIgnoreCase(String type, boolean exceptionOnNull) {

    FieldType fieldType = null;
    
    try {
      fieldType = GrouperUtil.enumValueOfIgnoreCase(FieldType.class, 
          type, false);
    } catch (Exception e) {
      //ignore this
    }
    
    if (fieldType != null) {
      return fieldType;
    }
    
    for (FieldType localFieldType : FieldType.values()) {
      if (StringUtils.equalsIgnoreCase(type, localFieldType.getType())) {
        return localFieldType;
      }
    }
      
    if (exceptionOnNull) {
      throw new RuntimeException("Cant find type: " + type);
    }
    
    return null;
    
  }
  
  /**
   * 
   * @param type
   * @return the type
   */
  public static FieldType getInstance(String type) {
    for (FieldType fieldType : FieldType.values()) {
      if (StringUtils.equalsIgnoreCase(type, fieldType.getType())) {
        return fieldType;
      }
    }
    throw new RuntimeException("Cant find field type: " + type);
  }

  /**
   * 
   * @param theType
   */
  private FieldType(String theType) {
    this.type = theType;
  }
  
  /** */
  private String type;


  /**
   * 
   * @see java.lang.Enum#toString()
   */
  public String toString() {
    return this.type;
  } // public String toString()

  /**
   * 
   * @return type
   */
  public String getType() {
    return type;
  }

}

