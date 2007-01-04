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
import  java.io.Serializable;
import  java.util.*;

/** 
 * Field Type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldType.java,v 1.10 2007-01-04 17:17:45 blair Exp $    
 */
public class FieldType implements Serializable {

  // PUBLIC CLASS CONSTANTS //
  public static final FieldType ACCESS            = new FieldType("access");
  public static final FieldType ATTRIBUTE         = new FieldType("attribute");
  public static final FieldType LIST              = new FieldType("list");
  public static final FieldType NAMING            = new FieldType("naming");
  public static final long      serialVersionUID  = -2133978355688724546L;


  // PRIVATE CLASS CONSTANTS //
  private static final Map      TYPES     = new HashMap();


  // PRIVATE INSTANCE VARIABLES //
  private String type;


  // STATIC //
  static {
    TYPES.put(ACCESS.toString(),    ACCESS);
    TYPES.put(ATTRIBUTE.toString(), ATTRIBUTE);
    TYPES.put(LIST.toString(),      LIST);
    TYPES.put(NAMING.toString(),    NAMING);
  } // static


  // CONSTRUCTORS //
  private FieldType(String type) {
    this.type = type;
  } // private FieldType(type)


  // PUBLIC CLASS METHODS //
  public static FieldType getInstance(String type) {
    return (FieldType) TYPES.get(type);
  } // public static FieldType getInstance(type)


  // PUBLIC INSTANCE METHODS //
  public String toString() {
    return this.type;
  } // public String toString()


  Object readResolve() {
    return getInstance(type);
  } // Object readResolve()

}

