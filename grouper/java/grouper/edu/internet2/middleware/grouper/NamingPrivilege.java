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
 * Implementation of the {@link Privilege} interface for naming
 * privileges.
 * <p />
 * @author  blair christensen.
 * @version $Id: NamingPrivilege.java,v 1.8 2005-11-15 20:14:42 blair Exp $
 */
public class NamingPrivilege extends BasePrivilege {

  // Public Class Constants
  public static final Privilege CREATE = new NamingPrivilege(
    "create", "creators"   
  );
  public static final Privilege STEM   = new NamingPrivilege(
    "stem"  , "stemmers"  
  );


  // Private Instance Variables
  private String list;
  private String name;


  // Constructors
  private NamingPrivilege(String name, String list) {
    this.list = list;
    this.name = name;
  } // private NamingPrivilege(name)


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
   * Get object {@link Stem} that the privilege was
   * granted on.
   * <p/>
   * @return  {@link Stem} object.
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

  public String toString() {
    return this.name;
  } // public String toString()


  // Protected Instance Methods
  // TODO Bah
  protected String getList() {
    return this.list;
  } // protected String getList()

}

