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
import  java.util.*;
import  net.sf.hibernate.*;


/**
 * Perform low-level operations on the Groups Registry.
 * <p>
 * <strong>WARNING:</strong> Do <strong>not</strong> run the methods
 * expose by this class against your Grouper installation unless you
 * know what you are doing.  It <strong>will</strong> delete data.
 * </p>
 * @author  blair christensen.
 * @version $Id: RegistryReset.java,v 1.32 2006-09-06 15:30:40 blair Exp $
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
  protected static void addTestSubjects() { 
    RegistryReset rr = new RegistryReset();
    try {
      rr._addSubjects();
    }
    catch (Exception e) {
      e.printStackTrace();
      rr._abort(e.getMessage());
    }
  } // protected static void addTestSubjects()

  protected static void resetRegistryAndAddTestSubjects() { 
    RegistryReset rr = new RegistryReset();
    try {
      rr._emptyTables();
      rr._addSubjects();
    }
    catch (Exception e) {
      e.printStackTrace();
      rr._abort(e.getMessage());
    }
  } // protected static void resetRegistryAndAddTestSubjects()


  // PRIVATE INSTANCE METHODS //
  private void _addSubjects()   
    throws  HibernateException
  {
    for (int i=0; i<10; i++) {
      String  id    = "test.subject." + i;
      String  name  = "my name is " + id;
      HibernateSubject.add(id, SUBJ_TYPE, name);
    }
    CacheMgr.resetAllCaches();
  } // private void _addSubjects()

  private void _abort(String msg) 
    throws  GrouperRuntimeException
  {
    ErrorLog.error(RegistryReset.class, msg);
    throw new GrouperRuntimeException(msg);
  } // private void _abort(msg)

  private void _emptyTables() 
    throws  HibernateException
  {
    Session     hs  = HibernateHelper.getSession();
    Transaction tx  = hs.beginTransaction();

    hs.delete("from Membership");
    hs.delete("from GrouperSession");

    hs.delete("from Composite");
    hs.delete("from Group");
    List l = hs.find("from Stem as ns where ns.stem_name like '" + Stem.ROOT_INT + "'");
    if (l.size() == 1) {
      Stem    root  = (Stem) l.get(0);
      String  uuid  = root.getUuid();
      root.setModifier_id(  null);
      root.setModify_source(null);
      root.setModify_time(  0   );
      hs.saveOrUpdate(root);
      hs.delete("from Owner as o where o.uuid != '" + uuid + "'");
    }
    else {
      hs.delete("from Owner");
    }

    hs.delete("from Member as m where m.subject_id != 'GrouperSystem'");
    hs.delete(
      "from GroupType as t where (  "
      + "     t.name != 'base'      "
      + "and  t.name != 'naming'    "
      + ")"
    );
    // TODO Once properly mapped I can delete the explicit attr delete
    hs.delete("from HibernateSubjectAttribute");
    hs.delete("from HibernateSubject");

    tx.commit();
    hs.close();
    // TODO Now update the cached types + fields
    GroupTypeFinder.updateKnownTypes();
    FieldFinder.updateKnownFields();
    CacheMgr.resetAllCaches();
  } // private void _emptyTables()

}

