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
 * $Id: TestGroupsAdd.java,v 1.2 2004-11-13 05:26:48 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;


public class TestGroups extends TestCase {

  public TestGroups(String name) {
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
  

  // Fetch a non-existent group.
  public void testGroupExistFalse() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    GrouperGroup g = GrouperGroup.load(s, "stem.0", "descriptor.0");
    // Confirm that group doesn't exist
    Assert.assertFalse( g.exists() );
    // We're done
    s.stop();
  }

  // Create a group
  public void testCreateGroup() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Create the group
    String          stem  = "stem.0";
    String          desc  = "desc.0";
    GrouperGroup grp      = GrouperGroup.create(s, stem, desc);
    Assert.assertNotNull(grp);
    String klass          = "edu.internet2.middleware.grouper.GrouperGroup";
    Assert.assertTrue( klass.equals( grp.getClass().getName() ) );
    Assert.assertTrue( grp.exists() );
    Assert.assertNotNull( grp.type() );
    Assert.assertNotNull( grp.attribute("stem") );
    Assert.assertTrue( grp.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( grp.attribute("descriptor") );
    Assert.assertTrue( grp.attribute("descriptor").value().equals(desc) );
    // We're done
    s.stop();
  }

  // Fetch a valid group
  public void testFetchValidGroup() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch the group
    String          stem  = "stem.0";
    String          desc  = "desc.0";
    GrouperGroup grp      = GrouperGroup.load(s, stem, desc);
    Assert.assertNotNull(grp);
    String klass          = "edu.internet2.middleware.grouper.GrouperGroup";
    Assert.assertTrue( klass.equals( grp.getClass().getName() ) );
    Assert.assertTrue( grp.exists() );
    Assert.assertNotNull( grp.type() );
    Assert.assertNotNull( grp.attribute("stem") );
    Assert.assertTrue( grp.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( grp.attribute("descriptor") );
    Assert.assertTrue( grp.attribute("descriptor").value().equals(desc) );
    // We're done
    s.stop();
  }

  // Fetch an invalid group
  public void testFetchInvalidGroup() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch the group
    String          stem  = "stem.not0";
    String          desc  = "desc.not0";
    GrouperGroup grp      = GrouperGroup.load(s, stem, desc);
    // TODO This will fail (or rather, should be changed) if I start
    //      returning null rather than a desolate object
    Assert.assertFalse( grp.exists() );
    // We're done
    s.stop();
  }

  // TODO Assert ADMIN priv (create + fetch)
  // TODO Delete group

}

