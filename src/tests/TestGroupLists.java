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
 * $Id: TestGroupLists.java,v 1.16 2004-11-25 03:04:47 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;


public class TestGroupLists extends TestCase {

  private String stem0  = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";
  private String extn0  = "extn.0";
  private String extn1  = "extn.1";
  private String extn2  = "extn.2";


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


  public void testAddPeopleAsMembers() {
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup("blair", "person");
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup("notblair", "person");
    Assert.assertNotNull(m1);
    // Add m0 to g0 "members"
    Assert.assertTrue( grp0.listAddVal(s, m0, "members") );
    // Add m1 to g2 "members"
    Assert.assertTrue( grp2.listAddVal(s, m1, "members") );
    // We're done
    s.stop();
  }

  public void testFetchListData0() {
    // g0 (m0)  ()
    // g1 ()    ()
    // g2 (m1)  ()
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "admins"
    List            admin0  = grp0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    List            admin0e = grp0.listEffVals(s, "admins");
    Assert.assertNotNull(admin0e);
    Assert.assertTrue(admin0e.size() == 0);
    List            admin0i = grp0.listImmVals(s, "admins");
    Assert.assertNotNull(admin0i);
    Assert.assertTrue(admin0i.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = grp1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    List            admin1e = grp1.listEffVals(s, "admins");
    Assert.assertNotNull(admin1e);
    Assert.assertTrue(admin1e.size() == 0);
    List            admin1i = grp1.listImmVals(s, "admins");
    Assert.assertNotNull(admin1i);
    Assert.assertTrue(admin1i.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = grp2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    List            admin2e = grp2.listEffVals(s, "admins");
    Assert.assertNotNull(admin2e);
    Assert.assertTrue(admin2e.size() == 0);
    List            admin2i = grp2.listImmVals(s, "admins");
    Assert.assertNotNull(admin2i);
    Assert.assertTrue(admin2i.size() == 1);
    // Fetch g0 "members"
    List            mem0    = grp0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 1);
    List            mem0c   = grp0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 1);
    List            mem0e   = grp0.listEffVals(s, "members"); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 0);
    List            mem0i   = grp0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 1);
    // Fetch g1 "members"
    List            mem1    = grp1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = grp1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = grp1.listEffVals(s, "members"); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = grp1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 "members"
    List            mem2    = grp2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = grp2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = grp2.listEffVals(s, "members"); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = grp2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testInvalidFetchListData0() {
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "invalid admins"
    List            admin0  = grp0.listVals(s, "invalid admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 0);
    // Fetch g1 "invalid admins"
    List            admin1  = grp1.listVals(s, "invalid admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 0);
    // Fetch g2 "invalid admins"
    List            admin2  = grp2.listVals(s, "invalid admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 0);
    // Fetch g0 "invalid members"
    List            mem0    = grp0.listVals(s, "invalid members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 0);
    // Fetch g1 "invalid members"
    List            mem1    = grp1.listVals(s, "invalid members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    // Fetch g2 "invalid members"
    List            mem2    = grp2.listVals(s, "invalid members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 0);

    // We're done
    s.stop(); 
  }

  public void testAddPeopleAsInvalidMembers() {
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup("blair", "person");
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup("notblair", "person");
    Assert.assertNotNull(m1);
    // Add m0 to g0 "members"
    Assert.assertFalse( grp0.listAddVal(s, m0, "invalid members") );
    // Add m1 to g2 "members"
    Assert.assertFalse( grp2.listAddVal(s, m1, "invalid members") );
    // We're done
    s.stop();
  }

  public void testAddGroupWithoutMembersAsMember() {
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch g1 as m0
    GrouperMember   m0      = GrouperMember.lookup( grp1.key(), "group");
    Assert.assertNotNull(m0);
    // Add m0 to g0 "members"
    Assert.assertTrue( grp0.listAddVal(s, m0, "members") );
    // We're done
    s.stop();
  } 

  public void testFetchListData1() {
    // g0 (m0, g1)  ()
    // g1 ()        ()
    // g2 (m1)      ()
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "admins"
    List            admin0  = grp0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    List            admin0e = grp0.listEffVals(s, "admins");
    Assert.assertNotNull(admin0e);
    Assert.assertTrue(admin0e.size() == 0);
    List            admin0i = grp0.listImmVals(s, "admins");
    Assert.assertNotNull(admin0i);
    Assert.assertTrue(admin0i.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = grp1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    List            admin1e = grp1.listEffVals(s, "admins");
    Assert.assertNotNull(admin1e);
    Assert.assertTrue(admin1e.size() == 0);
    List            admin1i = grp1.listImmVals(s, "admins");
    Assert.assertNotNull(admin1i);
    Assert.assertTrue(admin1i.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = grp2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    List            admin2e = grp2.listEffVals(s, "admins");
    Assert.assertNotNull(admin2e);
    Assert.assertTrue(admin2e.size() == 0);
    List            admin2i = grp2.listImmVals(s, "admins");
    Assert.assertNotNull(admin2i);
    Assert.assertTrue(admin2i.size() == 1);
    // Fetch g0 "members"
    List            mem0    = grp0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 2);
    List            mem0c   = grp0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 2);
    List            mem0e   = grp0.listEffVals(s, "members"); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 0);
    List            mem0i   = grp0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 2);
    // Fetch g1 "members"
    List            mem1    = grp1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = grp1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = grp1.listEffVals(s, "members"); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = grp1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 "members"
    List            mem2    = grp2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = grp2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = grp2.listEffVals(s, "members"); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = grp2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testAddGroupWithMembersAsMember() {
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g2 as m0
    GrouperMember   m0      = GrouperMember.lookup( grp2.key(), "group");
    Assert.assertNotNull(m0);
    // Add m0 to g0 "members"
    Assert.assertTrue( grp0.listAddVal(s, m0, "members") );
    // We're done
    s.stop();
  }

  public void testFetchListData3() {
    //
    // g0 (m0, g1, g2)  (m1)
    // g1 ()            ()
    // g2 (m1)          ()
    //
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "admins"
    List            admin0  = grp0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    List            admin0e = grp0.listEffVals(s, "admins");
    Assert.assertNotNull(admin0e);
    Assert.assertTrue(admin0e.size() == 0);
    List            admin0i = grp0.listImmVals(s, "admins");
    Assert.assertNotNull(admin0i);
    Assert.assertTrue(admin0i.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = grp1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    List            admin1e = grp1.listEffVals(s, "admins");
    Assert.assertNotNull(admin1e);
    Assert.assertTrue(admin1e.size() == 0);
    List            admin1i = grp1.listImmVals(s, "admins");
    Assert.assertNotNull(admin1i);
    Assert.assertTrue(admin1i.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = grp2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    List            admin2e = grp2.listEffVals(s, "admins");
    Assert.assertNotNull(admin2e);
    Assert.assertTrue(admin2e.size() == 0);
    List            admin2i = grp2.listImmVals(s, "admins");
    Assert.assertNotNull(admin2i);
    Assert.assertTrue(admin2i.size() == 1);
    // Fetch g0 "members"
    List            mem0    = grp0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 4);
    List            mem0c   = grp0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 4);
    List            mem0e   = grp0.listEffVals(s, "members"); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 1); 
    List            mem0i   = grp0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 3); 
    // Fetch g1 "members"
    List            mem1    = grp1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = grp1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = grp1.listEffVals(s, "members"); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = grp1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 "members"
    List            mem2    = grp2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = grp2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = grp2.listEffVals(s, "members"); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = grp2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testRemoveMember0() {
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup("blair", "person");
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup("notblair", "person");
    Assert.assertNotNull(m1);
    // Remove m0 from g0 "members"
    Assert.assertTrue( grp0.listDelVal(s, m0, "members") );
    // We're done
    s.stop();
  }

  // TODO Remove g2 from g0 "members"
  // TODO Remove m1 from g2 "members"

  public void testFetchListData4() {
    //
    // g0 (g1, g2)  (m1)
    // g1 ()        ()
    // g2 (m1)      ()
    //
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "admins"
    List            admin0  = grp0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    List            admin0e = grp0.listEffVals(s, "admins");
    Assert.assertNotNull(admin0e);
    Assert.assertTrue(admin0e.size() == 0);
    List            admin0i = grp0.listImmVals(s, "admins");
    Assert.assertNotNull(admin0i);
    Assert.assertTrue(admin0i.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = grp1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    List            admin1e = grp1.listEffVals(s, "admins");
    Assert.assertNotNull(admin1e);
    Assert.assertTrue(admin1e.size() == 0);
    List            admin1i = grp1.listImmVals(s, "admins");
    Assert.assertNotNull(admin1i);
    Assert.assertTrue(admin1i.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = grp2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    List            admin2e = grp2.listEffVals(s, "admins");
    Assert.assertNotNull(admin2e);
    Assert.assertTrue(admin2e.size() == 0);
    List            admin2i = grp2.listImmVals(s, "admins");
    Assert.assertNotNull(admin2i);
    Assert.assertTrue(admin2i.size() == 1);
    // Fetch g0 "members"
    List            mem0    = grp0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 3);
    List            mem0c   = grp0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 3);
    List            mem0e   = grp0.listEffVals(s, "members"); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 1); 
    List            mem0i   = grp0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 2); 
    // Fetch g1 "members"
    List            mem1    = grp1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = grp1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = grp1.listEffVals(s, "members"); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = grp1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 "members"
    List            mem2    = grp2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = grp2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = grp2.listEffVals(s, "members"); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = grp2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

/*
  // TODO Effective Membership
  // TODO Fetch immediate members
  // TODO Fetch effective members
  // TODO Remove a group with immediate members
  // TODO Remove a group with effective members
  // TODO Remove a group that creates effective members in another
  //      group
  // TODO Bulk add 
  // TODO Bulk delete
  // TODO Add to an invalid group
  // TODO Recursive adds|dels
*/

/*
  public void testCensored() {
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);

    // g0
    GrouperGroup    g0      = GrouperGroup.load(s, stem0, extn0);
    // g1
    GrouperGroup    g1      = GrouperGroup.load(s, stem1, extn1);
    // g2
    GrouperGroup    g2      = GrouperGroup.load(s, stem2, extn2);

    // m0
    GrouperMember   m0      = GrouperMember.lookup("blair", "person");
    // m1
    GrouperMember   m1      = GrouperMember.lookup( g1.key(), "group");
    // m2
    GrouperMember   m2      = GrouperMember.lookup( g2.key(), "group");
    // m3
    GrouperMember   m3      = GrouperMember.lookup("notblair", "person");

    // Add m0 to g0
    // g0 (m0) ()
    Assert.assertTrue( g0.listAddVal(s, m0) );
    List g0dot0   = g0.listVals(s);
    Assert.assertTrue( g0dot0.size() == 1 );
    List g0dot0e  = g0.listEffVals(s);
    Assert.assertTrue( g0dot0e.size() == 0 );
    List g0dot0i  = g0.listImmVals(s);
    Assert.assertTrue( g0dot0i.size() == 1 );
    List g1dot0   = g1.listVals(s);
    Assert.assertTrue( g1dot0.size() == 0 );
    List g1dot0e  = g1.listEffVals(s);
    Assert.assertTrue( g1dot0e.size() == 0 );
    List g1dot0i  = g1.listImmVals(s);
    Assert.assertTrue( g1dot0i.size() == 0 );
    List g2dot0   = g2.listVals(s);
    Assert.assertTrue( g2dot0.size() == 0 );
    List g2dot0e  = g2.listEffVals(s);
    Assert.assertTrue( g2dot0e.size() == 0 );
    List g2dot0i  = g2.listImmVals(s);
    Assert.assertTrue( g2dot0i.size() == 0 );
    // Add m1 to g0
    // g0 (m0, g1) ()
    Assert.assertTrue( g0.listAddVal(s, m1) );
    List g0dot1   = g0.listVals(s);
    Assert.assertTrue( g0dot1.size() == 2 );
    List g0dot1e  = g0.listEffVals(s);
    Assert.assertTrue( g0dot1e.size() == 0 );
    List g0dot1i  = g0.listImmVals(s);
    Assert.assertTrue( g0dot1i.size() == 2 );
    List g1dot1   = g1.listVals(s);
    Assert.assertTrue( g1dot1.size() == 0 );
    List g1dot1e  = g1.listEffVals(s);
    Assert.assertTrue( g1dot1e.size() == 0 );
    List g1dot1i  = g1.listImmVals(s);
    Assert.assertTrue( g1dot1i.size() == 0 );
    List g2dot1   = g2.listVals(s);
    Assert.assertTrue( g2dot1.size() == 0 );
    List g2dot1e  = g2.listEffVals(s);
    Assert.assertTrue( g2dot1e.size() == 0 );
    List g2dot1i  = g2.listImmVals(s);
    Assert.assertTrue( g2dot1i.size() == 0 );
    // Add m2 to g1
    // g0 (m0, g1) (g2)
    // g1 (g2) ()
    Assert.assertTrue( g1.listAddVal(s, m2) );
    List g0dot2   = g0.listVals(s);
    Assert.assertTrue( g0dot2.size() == 3 );  
    List g0dot2e  = g0.listEffVals(s);
    Assert.assertTrue( g0dot2e.size() == 1 ); 
    List g0dot2i  = g0.listImmVals(s);
    Assert.assertTrue( g0dot2i.size() == 2 ); 
    List g1dot2   = g1.listVals(s);
    Assert.assertTrue( g1dot2.size() == 1 );
    List g1dot2e  = g1.listEffVals(s);
    Assert.assertTrue( g1dot2e.size() == 0 );
    List g1dot2i  = g1.listImmVals(s);
    Assert.assertTrue( g1dot2i.size() == 1 );
    List g2dot2   = g2.listVals(s);
    Assert.assertTrue( g2dot2.size() == 0 );
    List g2dot2e  = g2.listEffVals(s);
    Assert.assertTrue( g2dot2e.size() == 0 );
    List g2dot2i  = g2.listImmVals(s);
    Assert.assertTrue( g2dot2i.size() == 0 );
    // Add m3 to g2
    // g0 (m0, g1) (g2, m3)
    // g1 (g2) (m3)
    // g2 (m3) ()
    Assert.assertTrue( g2.listAddVal(s, m3) );
    List g0dot3   = g0.listVals(s);
    Assert.assertTrue( g0dot3.size() == 4 );  
    List g0dot3e  = g0.listEffVals(s);
    Assert.assertTrue( g0dot3e.size() == 2 );  
    List g0dot3i  = g0.listImmVals(s);
    Assert.assertTrue( g0dot3i.size() == 2 );  
    List g1dot3   = g1.listVals(s);
    Assert.assertTrue( g1dot3.size() == 2 );  
    List g1dot3e  = g1.listEffVals(s);
    Assert.assertTrue( g1dot3e.size() == 1 );  
    List g1dot3i  = g1.listImmVals(s);
    Assert.assertTrue( g1dot3i.size() == 1 );  
    List g2dot3   = g2.listVals(s);
    Assert.assertTrue( g2dot3.size() == 1 );  
    List g2dot3e  = g2.listEffVals(s);
    Assert.assertTrue( g2dot3e.size() == 0 );  
    List g2dot3i  = g2.listImmVals(s);
    Assert.assertTrue( g2dot3i.size() == 1 );  

  }
*/

}

