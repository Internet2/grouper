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


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  org.apache.commons.lang.builder.*;


/** 
 * An instance of a granted access privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AccessPrivilege.java,v 1.13 2006-06-15 04:45:58 blair Exp $
 */
public class AccessPrivilege {

  // Public Class Constants
  public static final Privilege ADMIN   = Privilege.getInstance("admin");
  public static final Privilege OPTIN   = Privilege.getInstance("optin");
  public static final Privilege OPTOUT  = Privilege.getInstance("optout");
  public static final Privilege READ    = Privilege.getInstance("read");
  public static final Privilege SYSTEM  = Privilege.getInstance("system");
  public static final Privilege UPDATE  = Privilege.getInstance("update");
  public static final Privilege VIEW    = Privilege.getInstance("view");


  // Private Instance Variables
  private Group   group;
  private boolean isRevokable;
  private String  klass;
  private String  name;
  private Subject owner;
  private Subject subj;


  // Constructors
  public AccessPrivilege(
    Group   group , Subject subj,   Subject owner, 
    Privilege priv, String  klass,  boolean isRevokable
  ) 
  {
    this.group        = group;
    this.isRevokable  = isRevokable;
    this.klass        = klass;
    this.name         = priv.toString();
    this.owner        = owner;
    this.subj         = subj;
  } // public AccessPrivilege(object, subj, owner, priv, klass, isRevokable)


  // Public Instance Methods

  /**
   * Get {@link Group} that the privilege was granted on.
   * <p/>
   * @return  {@link Group}
   */
  public Group getGroup() {
    return this.group;
  } // public Group getGroup()

  /**
   * Get name of implementation class for this privilege type.
   * @return  Class name of implementing class.
   */
  public String getImplementationName() {
    return this.klass;
  } // public String getImplementationName()

  /**
   * Get name of privilege.
   * @return  Name of privilege.
   */
  public String getName() {
    return this.name;
  } // public String getName()

  /**
   * Get subject which was granted privilege on this object.
   * @return  {@link Subject} that was granted privilege.
   */
  public Subject getOwner() {
    return this.owner;
  } // public Subject getOwner()

  /**
   * Get subject which has this privilege.
   * @return  {@link Subject} that has this privilege.
   */
  public Subject getSubject() {
    return this.subj;
  } // public Subject getSubject()

  /**
   * Returns true if privilege can be revoked.
   * @return  Boolean true if privilege can be revoked.
   */
  public boolean isRevokable() {
    return this.isRevokable;
  } // public boolean isRevokable()

  public String toString() {
    return new ToStringBuilder(this)
           .append("name"           , this.getName()                )
           .append("implementation" , this.getImplementationName()  )
           .append("revokable"      , this.isRevokable()            ) 
           .append("group"          , this.getGroup()               )
           .append("subject"        , this.getSubject()             )
           .append("owner"          , this.getOwner()               )
           .toString(); 
  } // public String toString()

}

