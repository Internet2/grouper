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
 * $Id: TestGroupLists.java,v 1.11 2004-11-19 04:47:00 blair Exp $
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
  private String desc0  = "desc.0";
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

  public void testAddPeopleAsMembers() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
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
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, desc1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "admins"
    List            admin0  = grp0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = grp1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = grp2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    // Fetch g0 "members"
    List            mem0    = grp0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 1);
    List            mem0c   = grp0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 1);
    // Fetch g1 "members"
    List            mem1    = grp1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = grp1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    // Fetch g2 "members"
    List            mem2    = grp2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = grp2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testInvalidFetchListData0() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, desc1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
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
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
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
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, desc1);
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
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, desc1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "admins"
    List            admin0  = grp0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = grp1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = grp2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    // Fetch g0 "members"
    List            mem0    = grp0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 2);
    List            mem0c   = grp0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 2);
    // Fetch g1 "members"
    List            mem1    = grp1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = grp1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    // Fetch g2 "members"
    List            mem2    = grp2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = grp2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testAddGroupAsMemberOfGroupAsMemberOfGroup() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, desc1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g2 as m0
    GrouperMember   m0      = GrouperMember.lookup( grp2.key(), "group");
    Assert.assertNotNull(m0);
    // Add m0 to g0 "members"
    Assert.assertTrue( grp1.listAddVal(s, m0, "members") );
    // We're done
    s.stop();
  }

  public void testFetchListData2() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, desc1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "admins"
    List            admin0  = grp0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = grp1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = grp2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    // Fetch g0 "members"
    List            mem0    = grp0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 2);
    List            mem0c   = grp0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 2);
    // Fetch g1 "members"
    List            mem1    = grp1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 1);
    List            mem1c   = grp1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 1);
    // Fetch g2 "members"
    List            mem2    = grp2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = grp2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testAddGroupWithMembersAsMember() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
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
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, desc1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "admins"
    List            admin0  = grp0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = grp1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = grp2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    // Fetch g0 "members"
    List            mem0    = grp0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 3);
    List            mem0c   = grp0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 3);
    // Fetch g1 "members"
    List            mem1    = grp1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 1);
    List            mem1c   = grp1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 1);
    // Fetch g2 "members"
    List            mem2    = grp2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = grp2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testRemoveMembers() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
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
    // Remove m1 from g2 "members"
    Assert.assertTrue( grp2.listDelVal(s, m1, "members") );
    // We're done
    s.stop();
  }

  public void testFetchListData4() {
    Grouper         G       = new Grouper();
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch Group 0
    GrouperGroup    grp0    = GrouperGroup.load(s, stem0, desc0);
    Assert.assertNotNull(grp0);
    Assert.assertTrue(grp0.exists());
    // Fetch Group 1
    GrouperGroup    grp1    = GrouperGroup.load(s, stem1, desc1);
    Assert.assertNotNull(grp1);
    Assert.assertTrue(grp1.exists());
    // Fetch Group 2
    GrouperGroup    grp2    = GrouperGroup.load(s, stem2, desc2);
    Assert.assertNotNull(grp2);
    Assert.assertTrue(grp2.exists());
    // Fetch g0 "admins"
    List            admin0  = grp0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = grp1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = grp2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    // Fetch g0 "members"
    List            mem0    = grp0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 1);
    List            mem0c   = grp0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 1);
    // Fetch g1 "members"
    List            mem1    = grp1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = grp1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    // Fetch g2 "members"
    List            mem2    = grp2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 0);
    List            mem2c   = grp2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 0);

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

}

