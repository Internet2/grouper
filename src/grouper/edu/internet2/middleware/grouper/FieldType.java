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

/** 
 * Field Type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldType.java,v 1.14 2009-02-09 21:36:44 mchyzer Exp $    
 */
public enum FieldType implements Serializable {

  /** */
  ACCESS("access"),
  
  /** */
  ATTRIBUTE("attribute"),
  
  /** */
  LIST("list"),

  /** */
  NAMING("naming");
  
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

