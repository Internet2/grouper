/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
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
 * $Id: TestGroupsAdd.java,v 1.9 2004-11-25 03:04:47 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;


public class TestGroups extends TestCase {

  private String stem0  = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";
  private String extn0  = "extn.0";
  private String extn1  = "extn.1";
  private String extn2  = "extn.2";
  
  private String klass  = "edu.internet2.middleware.grouper.GrouperGroup";

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
  public void testGroupsExistFalse() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Confirm that groups don't exist
    GrouperGroup    grp0  = GrouperGroup.load(s, stem0, extn0);
    Assert.assertFalse( grp0.exists() );
    GrouperGroup    grp1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertFalse( grp1.exists() );
    GrouperGroup    grp2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertFalse( grp2.exists() );
    // We're done
    s.stop();
  }

  // Create a group
  public void testCreateGroups() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Create the groups
    // Group 0
    GrouperGroup    grp0  = GrouperGroup.create(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue( klass.equals( grp0.getClass().getName() ) );
    Assert.assertTrue( grp0.exists() );
    Assert.assertNotNull( grp0.type() );
    Assert.assertNotNull( grp0.attribute("stem") );
    Assert.assertTrue( grp0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( grp0.attribute("extension") );
    Assert.assertTrue( grp0.attribute("extension").value().equals(extn0) );
    // Group 1
    GrouperGroup    grp1  = GrouperGroup.create(s, stem1, extn1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue( klass.equals( grp1.getClass().getName() ) );
    Assert.assertTrue( grp1.exists() );
    Assert.assertNotNull( grp1.type() );
    Assert.assertNotNull( grp1.attribute("stem") );
    Assert.assertTrue( grp1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( grp1.attribute("extension") );
    Assert.assertTrue( grp1.attribute("extension").value().equals(extn1) );
    // Group 2
    GrouperGroup    grp2  = GrouperGroup.create(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue( klass.equals( grp2.getClass().getName() ) );
    Assert.assertTrue( grp2.exists() );
    Assert.assertNotNull( grp2.type() );
    Assert.assertNotNull( grp2.attribute("stem") );
    Assert.assertTrue( grp2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( grp2.attribute("extension") );
    Assert.assertTrue( grp2.attribute("extension").value().equals(extn2) );
    // We're done
    s.stop();
  }

  // Fetch a valid group
  public void testFetchValidGroups() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch the groups
    // Group 0
    GrouperGroup    grp0  = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue( klass.equals( grp0.getClass().getName() ) );
    Assert.assertTrue( grp0.exists() );
    Assert.assertNotNull( grp0.type() );
    Assert.assertNotNull( grp0.attribute("stem") );
    Assert.assertTrue( grp0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( grp0.attribute("extension") );
    Assert.assertTrue( grp0.attribute("extension").value().equals(extn0) );
    // Group 1
    GrouperGroup    grp1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue( klass.equals( grp1.getClass().getName() ) );
    Assert.assertTrue( grp1.exists() );
    Assert.assertNotNull( grp1.type() );
    Assert.assertNotNull( grp1.attribute("stem") );
    Assert.assertTrue( grp1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( grp1.attribute("extension") );
    Assert.assertTrue( grp1.attribute("extension").value().equals(extn1) );
    // Group 2
    GrouperGroup    grp2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue( klass.equals( grp2.getClass().getName() ) );
    Assert.assertTrue( grp2.exists() );
    Assert.assertNotNull( grp2.type() );
    Assert.assertNotNull( grp2.attribute("stem") );
    Assert.assertTrue( grp2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( grp2.attribute("extension") );
    Assert.assertTrue( grp1.attribute("extension").value().equals(extn1) );
    // We're done
    s.stop();
  }

  // TODO Assert ADMIN priv (create + fetch)
  // TODO Delete group

}

