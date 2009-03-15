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

package edu.internet2.middleware.grouper.privs;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.exception.SchemaException;


/** 
 * Privilege schema specification.  Access the constants for Groups from AccessPrivilege
 * and Stems from NamingPrivilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Privilege.java,v 1.8 2009-03-15 08:18:11 mchyzer Exp $
 */
public class Privilege implements Serializable {

  /** constant */
  public static final long serialVersionUID = 931658631999330719L;

  /**
   * generate hash code
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /**
   * string equals
   * @return if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Privilege other = (Privilege) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  
  
  //get references to these from other classes, e.g. AccessPrivilege.READ, or NamingPrivilege
  // PRIVATE CLASS CONSTANTS //
  private static final Set<Privilege>         ACCESS  = new LinkedHashSet<Privilege>();
  private static final Privilege              ADMIN   = new Privilege("admin" );
  private static final Privilege              CREATE  = new Privilege("create");
  private static final Set<Privilege>         NAMING  = new LinkedHashSet<Privilege>();
  private static final Privilege              OPTIN   = new Privilege("optin" );
  private static final Privilege              OPTOUT  = new Privilege("optout");
  private static final Map<String,Privilege>  PRIVS   = new HashMap<String,Privilege>();
  private static final Privilege              READ    = new Privilege("read"  );
  private static final Privilege              STEM    = new Privilege("stem"  );
  private static final Privilege              SYSTEM  = new Privilege("system");
  private static final Privilege              UPDATE  = new Privilege("update");
  private static final Privilege              VIEW    = new Privilege("view"  );


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


  /**
   * return the list name
   * @return the list name
   */
  public String getListName() {
    if (isAccess(this)) {
      return AccessPrivilege.privToList(this);
    }
    if (isNaming(this)) {
      return NamingPrivilege.privToList(this);
    }
    throw new RuntimeException("Invalid list: " + this);
  }
  
  /**
   * return the list name
   * @return the list name
   */
  public Field getField() throws SchemaException {
    String listName = this.getListName();
    if (!StringUtils.isBlank(listName)) {
      return FieldFinder.find(listName, true);
    }
    throw new SchemaException("invalid privilege: " + this);
  }
  
  // CONSTRUCTORS //
  private Privilege(String name) {
    this.name = name;
  } // private Privilege(name)


  // PUBLIC CLASS METHODS //
  public static Set getAccessPrivs() {
    return ACCESS;
  } // public static Set getAccessPrivs()

  public static Privilege getInstance(String name) {
    return (Privilege) PRIVS.get(name);
  } // public static Privilege getInstance(name)

  public static Set getNamingPrivs() {
    return NAMING;
  }

  public static boolean isAccess(Privilege p) {
    if (ACCESS.contains(p)) {
      return true;
    }
    return false;
  }

  public static boolean isNaming(Privilege p) {
    if (NAMING.contains(p)) {
      return true;
    }
    return false;
  }


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

