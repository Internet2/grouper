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


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;


/** 
 * An instance of a granted access privilege.
 * <p />
 * @author  blair christensen.
 * @version $Id: AccessPrivilege.java,v 1.10 2005-11-17 18:36:37 blair Exp $
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
  private boolean isRevokable;
  private String  klass;
  private String  name;
  private Object  object;
  private Subject owner;
  private Subject subj;


  // Constructors
  public AccessPrivilege(
    Object  object, Subject subj,   Subject owner, 
    Privilege priv, String  klass,  boolean isRevokable
  ) 
  {
    this.isRevokable  = isRevokable;
    this.klass        = klass;
    this.name         = priv.toString();
    this.object       = object;
    this.owner        = owner;
    this.subj         = subj;
  } // public AccessPrivilege(object, subj, owner, priv, klass, isRevokable)


  // Public Instance Methods

  /**
   * Get name of implementation class for this privilege type.
   * @return  Class name of implementing class.
   */
  public String getImplementationName() {
    throw new RuntimeException("not implemented");
  } // public String getImplementationName()

  /**
   * Returns true if privilege can be revoked.
   * @return  Boolean true if privilege can be revoked.
   */
  public boolean isRevokable() {
    throw new RuntimeException("not implemented");
  } // public boolean isRevokable()

  /**
   * Get name of privilege.
   * @return  Name of privilege.
   */
  public String getName() {
    throw new RuntimeException("not implemented");
  } // public String getName()

  /**
   * Get {@link Group} that the privilege was granted on.
   * <p/>
   * @return  {@link Group}
   */
  public Object getObject() {
    throw new RuntimeException("not implemented");
  } // public Object getObject()

  /**
   * Get subject which was granted privilege on this object.
   * @return  {@link Subject} that was granted privilege.
   */
  public Subject getOwner() 
    throws SubjectNotFoundException
  {
    throw new RuntimeException("not implemented");
  } // public Subject getOwner()

  /**
   * Get subject which has this privilege.
   * @return  {@link Subject} that has this privilege.
   */
  public Subject getSubject() {
    throw new RuntimeException("not implemented");
  } // public Subject getSubject()

}

