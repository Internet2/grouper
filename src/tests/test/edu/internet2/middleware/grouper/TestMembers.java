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
 * $Id: TestMembers.java,v 1.1 2004-11-12 21:54:41 blair Exp $
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
    Grouper       G = new Grouper();
    GrouperMember m = GrouperMember.lookup("blair", "person");
    Assert.assertNotNull(m);
  }

  // Initialize an invalid subject as a member object
  // Fetch an already existing member object

/*
  // Set+Get Member id and type
  public void testGrouperMemberSetGetIdAndType() {
    // FIXME I should probably test after fetching a *real* member, no?
    GrouperMember member = new GrouperMember("GrouperSystem", "person");
 
    Class  klass    = member.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMember";

    Assert.assertNotNull(member);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );

    Assert.assertTrue( member.id().equals( "GrouperSystem" ) );
    Assert.assertTrue( member.type().equals( "person" ) );
  }

  // TODO Get Valid Member 
  // public void testGrouperMemberGetValidMember() { }

  // TODO Get Invalid Member
  // public void testGrouperMemberGetInvalidMember() { }
*/

}

