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
 * $Id: TestGroupsAdd.java,v 1.6 2004-11-22 18:32:05 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;


public class TestGroups extends TestCase {

  private String stem0  = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";
  private String desc0  = "desc.0";
  private String desc1  = "desc.1";
  private String desc2  = "desc.2";
  
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
    GrouperGroup    grp0  = GrouperGroup.load(s, stem0, desc0);
    Assert.assertFalse( grp0.exists() );
    GrouperGroup    grp1  = GrouperGroup.load(s, stem1, desc1);
    Assert.assertFalse( grp1.exists() );
    GrouperGroup    grp2  = GrouperGroup.load(s, stem1, desc2);
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
    GrouperGroup    grp0  = GrouperGroup.create(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue( klass.equals( grp0.getClass().getName() ) );
    Assert.assertTrue( grp0.exists() );
    Assert.assertNotNull( grp0.type() );
    Assert.assertNotNull( grp0.attribute("stem") );
    Assert.assertTrue( grp0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( grp0.attribute("descriptor") );
    Assert.assertTrue( grp0.attribute("descriptor").value().equals(desc0) );
    // Group 1
    GrouperGroup    grp1  = GrouperGroup.create(s, stem1, desc1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue( klass.equals( grp1.getClass().getName() ) );
    Assert.assertTrue( grp1.exists() );
    Assert.assertNotNull( grp1.type() );
    Assert.assertNotNull( grp1.attribute("stem") );
    Assert.assertTrue( grp1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( grp1.attribute("descriptor") );
    Assert.assertTrue( grp1.attribute("descriptor").value().equals(desc1) );
    // Group 2
    GrouperGroup    grp2  = GrouperGroup.create(s, stem2, desc2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue( klass.equals( grp2.getClass().getName() ) );
    Assert.assertTrue( grp2.exists() );
    Assert.assertNotNull( grp2.type() );
    Assert.assertNotNull( grp2.attribute("stem") );
    Assert.assertTrue( grp2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( grp2.attribute("descriptor") );
    Assert.assertTrue( grp2.attribute("descriptor").value().equals(desc2) );
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
    GrouperGroup    grp0  = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue( klass.equals( grp0.getClass().getName() ) );
    Assert.assertTrue( grp0.exists() );
    Assert.assertNotNull( grp0.type() );
    Assert.assertNotNull( grp0.attribute("stem") );
    Assert.assertTrue( grp0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( grp0.attribute("descriptor") );
    Assert.assertTrue( grp0.attribute("descriptor").value().equals(desc0) );
    // Group 1
    GrouperGroup    grp1  = GrouperGroup.load(s, stem1, desc1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue( klass.equals( grp1.getClass().getName() ) );
    Assert.assertTrue( grp1.exists() );
    Assert.assertNotNull( grp1.type() );
    Assert.assertNotNull( grp1.attribute("stem") );
    Assert.assertTrue( grp1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( grp1.attribute("descriptor") );
    Assert.assertTrue( grp1.attribute("descriptor").value().equals(desc1) );
    // Group 2
    GrouperGroup    grp2  = GrouperGroup.load(s, stem2, desc2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue( klass.equals( grp2.getClass().getName() ) );
    Assert.assertTrue( grp2.exists() );
    Assert.assertNotNull( grp2.type() );
    Assert.assertNotNull( grp2.attribute("stem") );
    Assert.assertTrue( grp2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( grp2.attribute("descriptor") );
    Assert.assertTrue( grp1.attribute("descriptor").value().equals(desc1) );
    // We're done
    s.stop();
  }

  // TODO Assert ADMIN priv (create + fetch)
  // TODO Delete group

}

