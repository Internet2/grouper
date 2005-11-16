/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * Privilege schema specification.
 * <p />
 * @author  blair christensen.
 * @version $Id: Privilege.java,v 1.8 2005-11-16 21:04:25 blair Exp $
 */
public class Privilege implements Serializable {

  // Private Class Constants
  private static final Privilege ADMIN  = new Privilege("admin" );
  private static final Privilege CREATE = new Privilege("create");
  private static final Privilege OPTIN  = new Privilege("optin" );
  private static final Privilege OPTOUT = new Privilege("optout");
  private static final Privilege READ   = new Privilege("read"  );
  private static final Privilege STEM   = new Privilege("stem"  );
  private static final Privilege SYSTEM = new Privilege("system");
  private static final Privilege UPDATE = new Privilege("update");
  private static final Privilege VIEW   = new Privilege("view"  );


  // Private Class Constants
  private static final Map PRIVS = new HashMap();


  // Private Instance Variables
  private String name;


  static {
    PRIVS.put(ADMIN.toString()  , ADMIN );
    PRIVS.put(CREATE.toString() , CREATE);
    PRIVS.put(OPTIN.toString()  , OPTIN );
    PRIVS.put(OPTOUT.toString() , OPTOUT);
    PRIVS.put(READ.toString()   , READ  );
    PRIVS.put(STEM.toString()   , STEM  );
    PRIVS.put(SYSTEM.toString() , SYSTEM);
    PRIVS.put(UPDATE.toString() , UPDATE);
    PRIVS.put(VIEW.toString()   , VIEW  );
  } // static


  // Constructors
  private Privilege(String name) {
    this.name = name;
  } // private Privilege(name)


  // Public Class Methods
  public static Privilege getInstance(String name) {
    return (Privilege) PRIVS.get(name);
  } // public static Privilege getInstance(name)


  // Public Instance Methods
  public String getName() {
    return this.name;
  } // public String getName()

  // Public Instance Methods
  public String toString() {
    return this.getName();
  } // public String toString()


  Object readResolve() {
    return getInstance(name);
  } // Object readResolve()


}

