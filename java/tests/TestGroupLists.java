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
 * $Id: TestGroupLists.java,v 1.2 2004-11-15 18:22:19 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;


public class TestGroupLists extends TestCase {

  private String stem   = "stem.0";
  private String stem1  = "stem.1";
  private String desc   = "desc.0";
  private String desc1  = "desc.1";


  public TestGroupLists(String name) {
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
 
 
  // Add valid list data to a group
  public void testAddValidListDataToGroup() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch a group
    GrouperGroup    grp     = GrouperGroup.load(s, stem, desc);
    Assert.assertNotNull(grp);
    String          klassG  = "edu.internet2.middleware.grouper.GrouperGroup";
    Assert.assertTrue( klassG.equals( grp.getClass().getName() ) );
    Assert.assertTrue( grp.exists() );
    // Fetch a member
    GrouperMember   m       = GrouperMember.lookup("blair", "person");
    Assert.assertNotNull(m);
    String          klassM  = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klassM.equals( m.getClass().getName() ) );
    // Add member to "members" list
    Assert.assertTrue( grp.listAddVal(s, m, "members") );
    // We're done
    s.stop();
  }

  // Add invalid list data to a group
  public void testAddInvalidListDataToGroup() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch a group
    GrouperGroup    grp   = GrouperGroup.load(s, stem, desc);
    Assert.assertTrue( grp.exists() );
    // Fetch a member
    GrouperMember   m     = GrouperMember.lookup("blair", "person");
    Assert.assertNotNull(m);
    String          klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klass.equals( m.getClass().getName() ) );
    // Add member to "notmembers" list
    Assert.assertFalse( grp.listAddVal(s, m, "notmembers") );
    // We're done
    s.stop();
  }

  // TODO Add list data (plural)
  // TODO Add duplicate list data (plural)

  public void testFetchValidListDataForGroup() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch a group
    GrouperGroup    grp     = GrouperGroup.load(s, stem, desc);
    Assert.assertTrue( grp.exists() );
    // Fetch list data of type "admins"
    List            admins  = grp.listVals(s, "admins");
    Assert.assertNotNull(admins);
    Assert.assertTrue(admins.size() == 1);
    // Fetch list data of type "members"
    List members = grp.listVals(s, "members");
    Assert.assertNotNull(members);
    Assert.assertTrue(members.size() == 1);
    // We're done
    s.stop(); 
  }

  // TODO Fetch list data (plural)
  public void testFetchInvalidListDataForGroup() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch a group
    GrouperGroup    grp   = GrouperGroup.load(s, stem, desc);
    Assert.assertTrue( grp.exists() );
    // Fetch list data of type "nonadmins"
    List admins = grp.listVals(s, "nonadmins");
    Assert.assertNotNull(admins);
    Assert.assertTrue(admins.size() == 0);
    // Fetch list data of type "nonmembers"
    List members = grp.listVals(s, "nonmembers");
    Assert.assertNotNull(members);
    Assert.assertTrue(members.size() == 0);
    // We're done
    s.stop(); 
  }


  // Remove valid list data from a group
  public void testRemoveValidListDataFromGroup() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch a group
    GrouperGroup    grp   = GrouperGroup.load(s, stem, desc);
    Assert.assertTrue( grp.exists() );
    // Fetch a member
    GrouperMember   m     = GrouperMember.lookup("blair", "person");
    Assert.assertNotNull(m);
    String          klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klass.equals( m.getClass().getName() ) );
    // Remove a member from the group's "members" list
    Assert.assertTrue( grp.listDelVal(s, m, "members") );
    // We're done
    s.stop();
  }

  // Remove invalid list data from a group
  public void testRemoveInvalidListDataFromGroup() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch a group
    GrouperGroup    grp   = GrouperGroup.load(s, stem, desc);
    Assert.assertTrue( grp.exists() );
    // Fetch a member
    GrouperMember   m     = GrouperMember.lookup("blair", "person");
    Assert.assertNotNull(m);
    String          klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klass.equals( m.getClass().getName() ) );
    // Remove a member from the group's "notmembers" list
    Assert.assertFalse( grp.listDelVal(s, m, "notmembers") );
    // We're done
    s.stop();
  }

  // TODO Remove list data (plural)
  // TODO Remove invalid list data 

  // Add group as immediate member
  public void testAddGroupAsImmediateMember() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch the first group
    GrouperGroup    grp1 = GrouperGroup.load(s, stem, desc);
    Assert.assertNotNull(grp1);
    Assert.assertTrue( grp1.exists() );
    // Create the second group
    GrouperGroup    grp2 = GrouperGroup.create(s, stem1, desc1);
    Assert.assertNotNull(grp2);
    Assert.assertTrue( grp2.exists() );
    // TODO Make `grp2' a member of `grp1'
    // We're done
    s.stop();
  }

  // TODO Delete group with immediate members
  // TODO Delete groups that provide/have effective members

}

