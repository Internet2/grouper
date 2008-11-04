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

package edu.internet2.middleware.grouper.misc;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;

/** 
 * Composite Type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CompositeType.java,v 1.3 2008-11-04 07:17:56 mchyzer Exp $    
 * @since   1.0
 */
public class CompositeType implements Serializable {

  // PUBLIC CLASS CONSTANTS //

  /**
   * Complement Membership.
   */
  public static final CompositeType COMPLEMENT        = new CompositeType("complement");
  /**
   * Intersection Membership.
   */
  public static final CompositeType INTERSECTION      = new CompositeType("intersection");
  /**
   * Union Memberhsip.
   */
  public static final CompositeType UNION             = new CompositeType("union");
  public static final long          serialVersionUID  = 8723086294472152215L;


  // PRIVATE CLASS CONSTANTS //
  private static final Map<String, CompositeType> TYPES = new HashMap<String, CompositeType>();


  // PRIVATE INSTANCE VARIABLES //
  private String type;

  /**
   * get name of composite type, e.g. complement, union, intersection
   * @return name
   */
  public String getName() {
    return this.type;
  }

  // STATIC //
  static {
    TYPES.put(  COMPLEMENT.toString()   , COMPLEMENT    );
    TYPES.put(  INTERSECTION.toString() , INTERSECTION  );
    TYPES.put(  UNION.toString()        , UNION         );
  } // static


  // CONSTRUCTORS //
  // @since 1.0
  private CompositeType(String type) {
    this.type = type;
  } // private CompositeType(type)


  // PUBLIC CLASS METHODS //

  /**
   * @since 1.0
   */
  public static CompositeType getInstance(String type) {
    return (CompositeType) TYPES.get(type);
  } // public static CompositeType getInstance(type)
  
  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof CompositeType)) {
      return false;
    }
    return StringUtils.equals(this.type, ((CompositeType)other).type);
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (this.type == null) {
      return 1;
    }
    return this.type.hashCode();
  }


  /**
   * @since 1.0
   */
  public String toString() {
    return this.type;
  } // public String toString()


  /**
   * @since 1.0
   */
  Object readResolve() {
    return getInstance(type);
  } // Object readResolve()

}

