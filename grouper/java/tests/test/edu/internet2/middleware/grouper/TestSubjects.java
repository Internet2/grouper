/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the Academic
 * Free License version 2.1.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.opensource.org/licenses/afl-2.1.php>
 */

/*
 * $Id: TestSubjects.java,v 1.3 2004-11-12 16:38:29 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.lang.reflect.*;
import  java.util.*;
import  junit.framework.*;


public class TestSubjects extends TestCase {

  public TestSubjects(String name) {
    super(name);
  }

  protected void setUp () {
    // Nothing -- Yet
  }

  protected void tearDown () {
    // Nothing -- Yet
  }


  /*
   * TESTS
   */
  

  public void testGrouperSubjectClassLookupFailure() {
    Grouper G     = new Grouper();
    String id     = "invalid id";
    String type   = "person";
    Subject subj  = GrouperSubject.lookup(id, type);
    Assert.assertNull(subj);
  }

  public void testGrouperSubjectClassLookupMemberSystem() {
    Grouper G     = new Grouper();
    String id     = Grouper.config("member.system");
    String type   = "person";
    Subject subj  = GrouperSubject.lookup(id, type);
    //Assert.assertNotNull(subj);
    String klass  = "edu.internet2.middleware.grouper.GrouperSubjImpl";
    //Assert.assertTrue( klass.equals( subj.getClass().getName() ) );
    //Assert.assertTrue( id.equals( m.id() ) );
    //Assert.assertTrue( typeID.equals( m.typeID() ) );
  }

/*
  public void testGrouperSubjectClassLookup() {
    Grouper G = new Grouper();
    String id     = "blair";
    String typeID = "person";
    GrouperMember m = GrouperSubject.lookup(id, typeID);
    String klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertNotNull(m);
    Assert.assertTrue( klass.equals( m.getClass().getName() ) );
    Assert.assertTrue( id.equals( m.id() ) );
    Assert.assertTrue( typeID.equals( m.typeID() ) );
  }
*/


}

