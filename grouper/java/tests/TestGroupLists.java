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
 * $Id: TestGroupLists.java,v 1.7 2004-11-16 17:59:58 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;


public class TestGroupLists extends TestCase {

  private String stem   = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";
  private String desc   = "desc.0";
  private String desc1  = "desc.1";
  private String desc2  = "desc.2";


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

  // TODO Move to TestMembers?
  // Initialize a valid group as a member object
  public void testCreateMemberFromValidSubjectTypeGroup() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch the group
    GrouperGroup    grp   = GrouperGroup.load(s, stem, desc);
    Assert.assertNotNull(grp);
    String klassGroup     = "edu.internet2.middleware.grouper.GrouperGroup";
    Assert.assertTrue( klassGroup.equals( grp.getClass().getName() ) );
    Assert.assertTrue( grp.exists() );
    String          id    = grp.key();  // TODO ARGH!!!
    String          type  = "group";
    GrouperMember   m     = GrouperMember.lookup(id, type);
    Assert.assertNotNull(m);
    String klassMember     = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klassMember.equals( m.getClass().getName() ) );
    Assert.assertNotNull( m.id() );
    Assert.assertTrue( m.id().equals( id) );
    Assert.assertNotNull( m.typeID() );
    Assert.assertTrue( m.typeID().equals( type ) );
  }

  // TODO Move to TestMembers?
  // Fetch an already existing group-as-member object
  public void testFetchMemberFromValidGroupSubjectTypeGroup() {
    Grouper       G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch the group
    GrouperGroup    grp   = GrouperGroup.load(s, stem, desc);
    Assert.assertNotNull(grp);
    String klassGroup     = "edu.internet2.middleware.grouper.GrouperGroup";
    Assert.assertTrue( klassGroup.equals( grp.getClass().getName() ) );
    Assert.assertTrue( grp.exists() );
    String          id    = grp.key();  // TODO ARGH!!!
    String          type  = "group";
    GrouperMember   m     = GrouperMember.lookup(id, type);
    Assert.assertNotNull(m);
    String klassMember     = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klassMember.equals( m.getClass().getName() ) );
    Assert.assertNotNull( m.id() );
    Assert.assertTrue( m.id().equals( id) );
    Assert.assertNotNull( m.typeID() );
    Assert.assertTrue( m.typeID().equals( type ) );
  }

  // TODO Invalid group 
  // Add group as immediate member
  public void testAddGroupAsImmediateMember() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch the first group
    GrouperGroup    grp1  = GrouperGroup.load(s, stem, desc);
    Assert.assertNotNull(grp1);
    Assert.assertTrue( grp1.exists() );
    // Create the second group
    GrouperGroup    grp2  = GrouperGroup.create(s, stem1, desc1);
    Assert.assertNotNull(grp2);
    Assert.assertTrue( grp2.exists() );
    // Fetch member
    String          id    = grp2.key();  // TODO ARGH!!!
    String          type  = "group";
    GrouperMember   m     = GrouperMember.lookup(id, type);
    Assert.assertNotNull(m);
    String klassMember    = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klassMember.equals( m.getClass().getName() ) );
    Assert.assertNotNull( m.id() );
    Assert.assertTrue( m.id().equals( id) );
    Assert.assertNotNull( m.typeID() );
    Assert.assertTrue( m.typeID().equals( type ) );
    // Add member to "members" list
    Assert.assertTrue( grp1.listAddVal(s, m, "members") );
    // We're done
    s.stop();
  }

  // Add group as immediate member
  public void testAddEffectiveMember() {
    Grouper         G     = new Grouper();
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch the first group
    GrouperGroup    grp1  = GrouperGroup.load(s, stem, desc);
    Assert.assertNotNull(grp1);
    Assert.assertTrue( grp1.exists() );
    // Create the second group
    GrouperGroup    grp2  = GrouperGroup.create(s, stem2, desc2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue( grp2.exists() );
    // Fetch person-as-member
    String          id1   = "notblair";       
    String          type1 = "person";
    GrouperMember   m1    = GrouperMember.lookup(id1, type1);
    Assert.assertNotNull(m1);
    String klassMember1    = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klassMember1.equals( m1.getClass().getName() ) );
    Assert.assertNotNull( m1.id() );
    Assert.assertTrue( m1.id().equals( id1 ) );
    Assert.assertNotNull( m1.typeID() );
    Assert.assertTrue( m1.typeID().equals( type1 ) );
    // Add person-as-member to second group
    Assert.assertTrue( grp2.listAddVal(s, m1, "members") );
    // Fetch group-as-member
    String          id2   = grp2.key();  // TODO ARGH!!!
    String          type2 = "group";
    GrouperMember   m2    = GrouperMember.lookup(id2, type2);
    Assert.assertNotNull(m2);
    String klassMember2    = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klassMember2.equals( m2.getClass().getName() ) );
    Assert.assertNotNull( m2.id() );
    Assert.assertTrue( m2.id().equals( id2) );
    Assert.assertNotNull( m2.typeID() );
    Assert.assertTrue( m2.typeID().equals( type2 ) );
    // Add group-as-member to the first group
    Assert.assertTrue( grp1.listAddVal(s, m2, "members") );
    // We're done
    s.stop();
  }

  public void testFetchValidEffectiveAndImmediateMembers() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch a group
    GrouperGroup    grp     = GrouperGroup.load(s, stem, desc);
    Assert.assertTrue( grp.exists() );
    // Fetch list data of type "members"
    List members = grp.listVals(s, "members");
    Assert.assertNotNull(members);
    // TODO Assert.assertTrue(members.size() == 2);
    // We're done
    s.stop(); 
  }

  // TODO Test setting|fetching of effective memberships
  // TODO Delete group with immediate members
  // TODO Delete groups that provide/have effective members
  // TODO Test for recursive adds?

}

