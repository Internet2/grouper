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
 * $Id: TestMembers.java,v 1.3 2004-11-20 16:10:24 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;


public class TestMembers extends TestCase {

  public TestMembers(String name) {
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
  

  // Initialize a valid subject as a member object
  public void testCreateMemberFromValidSubject() {
    String        id    = "blair";
    String        type  = "person";
    GrouperMember m     = GrouperMember.lookup(id, type);
    Assert.assertNotNull(m);
    String klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klass.equals( m.getClass().getName() ) );
    Assert.assertNotNull( m.id() );
    Assert.assertTrue( m.id().equals( id) );
    Assert.assertNotNull( m.typeID() );
    Assert.assertTrue( m.typeID().equals( type ) );
  }

  // Fetch an already existing member object
  public void testFetchMemberFromValidSubject() {
    String        id    = "blair";
    String        type  = "person";
    GrouperMember m     = GrouperMember.lookup(id, type);
    Assert.assertNotNull(m);
    String klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klass.equals( m.getClass().getName() ) );
    Assert.assertNotNull( m.id() );
    Assert.assertTrue( m.id().equals( id) );
    Assert.assertNotNull( m.typeID() );
    Assert.assertTrue( m.typeID().equals( type ) );
  }

  // Initialize an invalid subject as a member object
  public void testCreateMemberFromInvalidSubject() {
    String        id    = "invalid id";
    String        type  = "person";
    GrouperMember m     = GrouperMember.lookup(id, type);
    Assert.assertNull(m);
  }

  // TODO Valid member, invalid subject

}

