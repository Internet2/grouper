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
 * $Id: TestSessions.java,v 1.1 2004-11-12 20:18:00 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.lang.reflect.*;
import  java.util.*;
import  junit.framework.*;


public class TestSessions extends TestCase {

  public TestSessions(String name) {
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
  

  // Start a session as "member.system"
  public void testSessionStartAsMemberSystem() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    Assert.assertTrue( s.start(subj) );
  }

  // Start and end a session as SubjectID "member.system"
  public void testSessionStartEndAsMemberSystem() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    Assert.assertTrue( s.start(subj) );
    Assert.assertTrue( s.stop() );
  }
  
  // Attempt to end a session that hasn't been started 
  public void testSessionEndWithoutStart() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    Assert.assertFalse( s.stop() );
  }

  // Verify the subject of the current session 
  public void testSessionSubject() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    Assert.assertTrue( s.start(subj) );
    Subject         ret   =  s.subject();
    Assert.assertNotNull( ret );
    Assert.assertTrue( ret.getId().equals( Grouper.config("member.system") ) );
    Assert.assertTrue( s.stop() );
  }

}

