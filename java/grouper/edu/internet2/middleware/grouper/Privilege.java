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


import  java.util.*;


/** 
 * Privilege schema specification.
 * <p />
 * @author  blair christensen.
 * @version $Id: Privilege.java,v 1.7 2005-11-16 16:59:11 blair Exp $
 */
public class Privilege {

  // Private Class Constants
  private static final Privilege ADMIN   = new Privilege("admin",  "admins"  );
  private static final Privilege CREATE  = new Privilege("create", "creators");
  private static final Privilege OPTIN   = new Privilege("optin",  "optins"  );
  private static final Privilege OPTOUT  = new Privilege("optout", "optouts" );
  private static final Privilege READ    = new Privilege("read",   "readers" );
  private static final Privilege STEM    = new Privilege("stem",   "stemmers");
  private static final Privilege UPDATE  = new Privilege("update", "updaters");
  private static final Privilege VIEW    = new Privilege("view",   "viewers" );


  // Private Class Constants
  private static final Map PRIVS = new HashMap();


  // Private Instance Variables
  private String list;
  private String name;


  static {
    PRIVS.put(ADMIN.toString(),   ADMIN );
    PRIVS.put(OPTIN.toString(),   OPTIN );
    PRIVS.put(OPTOUT.toString(),  OPTOUT);
    PRIVS.put(READ.toString(),    READ  );
    PRIVS.put(UPDATE.toString(),  UPDATE);
    PRIVS.put(VIEW.toString(),    VIEW  );
    PRIVS.put(CREATE.toString(),  CREATE);
    PRIVS.put(STEM.toString(),    STEM  );
  } // static


  // Constructors
  private Privilege(String name, String list) {
    this.list = list;
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


  // Protected Instance Methods
  protected String getList() {
    return this.list;
  } // protected String getList()

}

