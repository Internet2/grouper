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
 * MembershipType schema specification.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipType.java,v 1.1 2006-06-16 17:30:01 blair Exp $
 * @since   1.0
 */
public class MembershipType implements Serializable {

  // PROTECTED CLASS CONSTANTS //
  protected static final MembershipType C = new MembershipType("composite");
  protected static final MembershipType E = new MembershipType("effective");
  protected static final MembershipType I = new MembershipType("immediate");


  // PRIVATE CLASS CONSTANTS //
  private static final Map TYPES = new HashMap();


  // PRIVATE INSTANCE VARIABLES //
  private String name;


  // STATIC //
  static {
    TYPES.put(  C.toString()  , C );
    TYPES.put(  E.toString()  , E );
    TYPES.put(  I.toString()  , I );
  } // static


  // CONSTRUCTORS //
  private MembershipType(String name) {
    this.name = name;
  } // private MembershipType(name)


  // PUBLIC INSTANCE METHODS //
  /** 
   * @since 1.0
   */
  public static MembershipType getInstance(String name) {
    return (MembershipType) TYPES.get(name);
  } // public static MembershipType getInstance(name)

  /**
   * @since 1.0
   */
  public String toString() {
    return this.name;
  } // public String toString()


  Object readResolve() {
    return getInstance(name);
  } // Object readResolve()

}

