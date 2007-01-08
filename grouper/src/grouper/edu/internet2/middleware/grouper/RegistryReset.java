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

package edu.internet2.middleware.grouper;

/**
 * Perform low-level operations on the Groups Registry.
 * <p>
 * <strong>WARNING:</strong> Do <strong>not</strong> run the methods
 * expose by this class against your Grouper installation unless you
 * know what you are doing.  It <strong>will</strong> delete data.
 * </p>
 * @author  blair christensen.
 * @version $Id: RegistryReset.java,v 1.41 2007-01-08 18:04:07 blair Exp $
 */
public class RegistryReset {

  // PRIVATE CLASS CONSTANTS //
  private static final String SUBJ_TYPE = "person"; 


  // CONSTRUCTORS //
  private RegistryReset() {
    super();
  } // private RegistryReset()



  // PUBLIC CLASS METHODS //
 
  /**
   * Reset the Groups Registry.
   * <p>
   * <strong>WARNING:</strong> This is a destructive act and will
   * delete all groups, stems, members, memberships and subjects from
   * your Groups Registry.  Do <strong>not</strong> run this unless
   * that is what you want.
   * </p>
   * <pre class="eg">
   * % java edu.internet2.middleware.grouper.RegistryReset
   * </pre>
   */
  public static void main(String[] args) {
    RegistryReset.reset();
    System.exit(0);
  } // public static void main(args)

  /**
   * Attempt to reset the Groups Registry to a pristine state.
   */
  public static void reset() {
    RegistryReset rr = new RegistryReset();
    try {
      rr._emptyTables();
    }
    catch (Exception e) {
      e.printStackTrace();
      rr._abort(e.getMessage());
    }
  } // public static void reset()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void internal_addTestSubjects() { 
    RegistryReset rr = new RegistryReset();
    try {
      rr._addSubjects();
    }
    catch (Exception e) {
      e.printStackTrace();
      rr._abort(e.getMessage());
    }
  } // protected static void internal_addTestSubjects()

  // @since   1.2.0
  protected static void internal_resetRegistryAndAddTestSubjects() { 
    RegistryReset rr = new RegistryReset();
    try {
      rr._emptyTables();
      rr._addSubjects();
    }
    catch (Exception e) {
      e.printStackTrace();
      rr._abort(e.getMessage());
    }
  } // protected static void internal_resetRegistryAndAddTestSubjects()


  // PRIVATE INSTANCE METHODS //
  private void _addSubjects()   
    throws  GrouperException
  {
    for (int i=0; i<10; i++) {
      String  id    = "test.subject." + i;
      String  name  = "my name is " + id;
      HibernateSubject.internal_add(id, SUBJ_TYPE, name);
    }
  } // private void _addSubjects()

  private void _abort(String msg) 
    throws  GrouperRuntimeException
  {
    ErrorLog.error(RegistryReset.class, msg);
    throw new GrouperRuntimeException(msg);
  } // private void _abort(msg)

  private void _emptyTables() 
    throws  GrouperException
  {
    HibernateRegistryDAO.resetRegistry();
    // TODO 20061018 Now update the cached types + fields
    GroupTypeFinder.internal_updateKnownTypes();
    FieldFinder.internal_updateKnownFields();
    SubjectFinder.internal_flushCache();
  } // private void _emptyTables()

} // public class RegistryReset

