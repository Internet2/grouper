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

import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.logging.*;


/**
 * Perform low-level operations on the Groups Registry.
 * <p>
 * <strong>WARNING:</strong> Do <strong>not</strong> run the methods
 * expose by this class against your Grouper installation unless you
 * know what you are doing.  It <strong>will</strong> delete data.
 * </p>
 * @author  blair christensen.
 * @version $Id: RegistryReset.java,v 1.15 2006-03-10 20:36:54 blair Exp $
 */
public class RegistryReset {

  /*
   * TODO
   * * Move subject adding to a different class?
   */

  // Private Class Constants
  private static final Log    LOG         = LogFactory.getLog(RegistryReset.class);
  private static final String SUBJ_TYPE   = "person"; 


  // Private Instance Variables
  private Session     hs;
  private Transaction tx;


  // Constructors
  private RegistryReset() {
    super();
  } // private RegistryReset()


  // Public Class methods
 
  /**
   * Add JDBC test subjects to the Groups Registry.
   */ 
  public static void addTestSubjects() {
    RegistryReset rr = new RegistryReset();
    try {
      rr._setUp();
      rr._addSubjects();
    }
    catch (Exception e) {
      rr._abort(e.getMessage());
    }
    finally {
      rr._tearDown();
    } 
  } // public static void addTestSubjects()

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
      rr._setUp();
      rr._emptyTables();
    }
    catch (Exception e) {
      rr._abort(e.getMessage());
    }
    finally {
      rr._tearDown();
    } 
    CacheMgr.resetAllCaches();
  } // public static void reset()

  /**
   * Attempt to reset the Groups Registry to a pristine state and then
   * add JDBC test subjects.
   */
  public static void resetRegistryAndAddTestSubjects() {
    RegistryReset rr = new RegistryReset();
    try {
      rr._setUp();
      rr._emptyTables();
      rr._addSubjects();
    }
    catch (Exception e) {
      rr._abort(e.getMessage());
    }
    finally {
      rr._tearDown();
    } 
    CacheMgr.resetAllCaches();
  } // public static void resetRegistryAndAddTestSubjects()


  // Private Instance Methods
  private void _addSubjects() 
    throws  HibernateException
  {
    for (int i=0; i<100; i++) {
      String  id    = "test.subject." + i;
      String  name  = "my name is " + id;
      this.hs.save(new HibernateSubject(id, SUBJ_TYPE, name));
    }
  } // private void _addSubjects()

  private void _abort(String err) {
    LOG.fatal(err);
    throw new RuntimeException(err);
  } // private void _abort(err)

  private void _emptyTables() 
    throws  HibernateException
  {
    this.hs.delete("from TxQueue");    
    this.hs.delete("from Membership");
    this.hs.delete("from GrouperSession");
    this.hs.delete("from Group");
    List l = this.hs.find("from Stem as ns where ns.stem_name like '" + Stem.ROOT_INT + "'");
    if (l.size() == 1) {
      Stem    root  = (Stem) l.get(0);
      String  uuid  = root.getUuid();
      root.setModifier_id(  null);
      root.setModify_source(null);
      root.setModify_time(  0   );
      this.hs.saveOrUpdate(root);
      this.hs.delete("from Owner as o where o.owner_uuid != '" + uuid + "'");
    }
    else {
      this.hs.delete("from Owner");
    }
    this.hs.delete("from Factor");
    this.hs.delete("from Member as m where m.subject_id != 'GrouperSystem'");
    this.hs.delete(
      "from GroupType as t where (t.name != 'base' and t.name != 'naming')"
    );
    this.hs.delete("from HibernateSubject");
  } // private void _emptyTables()

  private void _setUp() 
    throws  HibernateException
  {
    this.hs   = HibernateHelper.getSession();
    this.tx   = this.hs.beginTransaction();
  } // private void _setUp()

  private void _tearDown() {
    try {
      if (this.hs != null) {
        if (this.tx != null) {
          this.tx.commit();
        }
        this.hs.close();
      }
    }
    catch (HibernateException eH) {
      String err = "unable to tearDown: " + eH.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // private void _tearDown()

}

