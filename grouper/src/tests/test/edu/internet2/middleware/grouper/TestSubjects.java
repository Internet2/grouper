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
 * $Id: TestSubjects.java,v 1.5 2004-11-12 20:21:59 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
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
  

  public void testSubjectInterfaceLookupFailureInvalidID() {
    Grouper G     = new Grouper();
    String id     = "invalid id";
    String type   = "person";
    Subject subj  = GrouperSubject.lookup(id, type);
    Assert.assertNull(subj);
  }

  public void testSubjectInterfaceLookupFailureInvalidType() {
    Grouper G     = new Grouper();
    String id     = Grouper.config("member.system");
    String type   = "notaperson";
    Subject subj  = GrouperSubject.lookup(id, type);
    Assert.assertNull(subj);
  }

  public void testSubjectInterfaceLookupMemberSystem() {
    Grouper G     = new Grouper();
    String id     = Grouper.config("member.system");
    String type   = "person";
    Subject subj  = GrouperSubject.lookup(id, type);
    Assert.assertNotNull(subj);
    String klass  = "edu.internet2.middleware.grouper.SubjectImpl";
    Assert.assertTrue( klass.equals( subj.getClass().getName() ) );
    Assert.assertTrue( id.equals( subj.getId() ) );
    String name   = "Person";
    Assert.assertNotNull( subj.getSubjectType() );
    Assert.assertTrue( name.equals( subj.getSubjectType().getName() ) );
    Assert.assertTrue( type.equals( subj.getSubjectType().getId() ) );
  }

  public void testSubjectInterfaceLookup() {
    Grouper G     = new Grouper();
    String id     = "blair";
    String type   = "person";
    Subject subj  = GrouperSubject.lookup(id, type);
    Assert.assertNotNull(subj);
    String klass  = "edu.internet2.middleware.grouper.SubjectImpl";
    Assert.assertTrue( klass.equals( subj.getClass().getName() ) );
    Assert.assertTrue( id.equals( subj.getId() ) );
    String name   = "Person";
    Assert.assertNotNull( subj.getSubjectType() );
    Assert.assertTrue( name.equals( subj.getSubjectType().getName() ) );
    Assert.assertTrue( type.equals( subj.getSubjectType().getId() ) );
  }

}

