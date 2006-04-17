/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
 * Composite Type.
 * <p />
 * @author  blair christensen.
 * @version $Id: CompositeType.java,v 1.1.2.1 2006-04-17 18:19:54 blair Exp $    
 */
public class CompositeType implements Serializable {

  // Public Class Constants //
  public static final CompositeType COMPLEMENT    = new CompositeType("complement");
  public static final CompositeType INTERSECTION  = new CompositeType("intersection");
  public static final CompositeType UNION         = new CompositeType("union");

  // Private Class Constants //
  private static final Map TYPES = new HashMap();

  // Private Instance Variables //
  private String type;


  static {
    TYPES.put(  COMPLEMENT.toString()   , COMPLEMENT    );
    TYPES.put(  INTERSECTION.toString() , INTERSECTION  );
    TYPES.put(  UNION.toString()        , UNION         );
  } // static


  // Constructors //
  private CompositeType(String type) {
    this.type = type;
  } // private CompositeType(type)


  // Public Class Methods //
  public static CompositeType getInstance(String type) {
    return (CompositeType) TYPES.get(type);
  } // public static CompositeType getInstance(type)


  // Public Instance Methods //
  public String toString() {
    return this.type;
  } // public String toString()


  Object readResolve() {
    return getInstance(type);
  } // Object readResolve()

}

