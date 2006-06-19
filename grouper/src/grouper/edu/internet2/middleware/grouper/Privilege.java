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
 * Privilege schema specification.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Privilege.java,v 1.15 2006-06-19 17:00:57 blair Exp $
 */
public class Privilege implements Serializable {

  // PUBLIC CLASS CONSTANTS ///
  public static final long serialVersionUID = 931658631999330719L;


  // PRIVATE CLASS CONSTANTS //
  private static final Set        ACCESS  = new LinkedHashSet();
  private static final Privilege  ADMIN   = new Privilege("admin" );
  private static final Privilege  CREATE  = new Privilege("create");
  private static final Set        NAMING  = new LinkedHashSet();
  private static final Privilege  OPTIN   = new Privilege("optin" );
  private static final Privilege  OPTOUT  = new Privilege("optout");
  private static final Map        PRIVS   = new HashMap();
  private static final Privilege  READ    = new Privilege("read"  );
  private static final Privilege  STEM    = new Privilege("stem"  );
  private static final Privilege  SYSTEM  = new Privilege("system");
  private static final Privilege  UPDATE  = new Privilege("update");
  private static final Privilege  VIEW    = new Privilege("view"  );


  // PRIVATE INSTANCE VARIABLES //
  private String name;


  // STATIC //
  static {
    PRIVS.put(  ADMIN.toString()  , ADMIN   );
    ACCESS.add( ADMIN                       );
    PRIVS.put(  CREATE.toString() , CREATE  );
    NAMING.add( CREATE                      );
    PRIVS.put(  OPTIN.toString()  , OPTIN   );
    ACCESS.add( OPTIN                       );
    PRIVS.put(  OPTOUT.toString() , OPTOUT  );
    ACCESS.add( OPTOUT                      );
    PRIVS.put(  READ.toString()   , READ    );
    ACCESS.add( READ                        );
    PRIVS.put(  STEM.toString()   , STEM    );
    NAMING.add( STEM                        );
    PRIVS.put(  SYSTEM.toString() , SYSTEM  );
    PRIVS.put(  UPDATE.toString() , UPDATE  );
    ACCESS.add( UPDATE                      );
    PRIVS.put(  VIEW.toString()   , VIEW    );
    ACCESS.add( VIEW                        );
  } // static


  // CONSTRUCTORS //
  private Privilege(String name) {
    this.name = name;
  } // private Privilege(name)


  // PUBLIC CLASS METHODS //
  public static Set getAccessPrivs() {
    return ACCESS;
  } // public static Set getAccessPrivs()

  // TODO Should this be public?
  public static Privilege getInstance(String name) {
    return (Privilege) PRIVS.get(name);
  } // public static Privilege getInstance(name)

  public static Set getNamingPrivs() {
    return NAMING;
  } // pubilc static Set getNamingPrivs()

  public static boolean isAccess(Privilege p) {
    if (ACCESS.contains(p)) {
      return true;
    }
    return false;
  } // public static boolean isAccess(p)

  public static boolean isNaming(Privilege p) {
    if (NAMING.contains(p)) {
      return true;
    }
    return false;
  } // public static boolean isNaming(p)


  // PUBLIC INSTANCE METHODS //
  public String getName() {
    return this.name;
  } // public String getName()

  public String toString() {
    return this.getName();
  } // public String toString()


  Object readResolve() {
    return getInstance(name);
  } // Object readResolve()

}

