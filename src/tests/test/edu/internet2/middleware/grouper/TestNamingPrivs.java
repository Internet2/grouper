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
 * $Id: TestNamingPrivs.java,v 1.3 2004-11-28 17:09:45 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;


public class TestNamingPrivs extends TestCase {

  // TODO Test on naming groups
/*
  private String stem0  = null;
  private String stem1  = null;
  private String stem2  = null;
  private String stem00 = "stem.0";
  private String extn0  = "stem.0";
  private String extn1  = "stem.1";
  private String extn2  = "stem.2";
  private String extn00 = "stem.0.0";
*/
  private String stem0  = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";
  private String extn0  = "extn.0";
  private String extn1  = "extn.1";
  private String extn2  = "extn.2";

  private String m0id   = "blair";
  private String m1id   = "notblair";
  private String m0type = "person";
  private String m1type = "person";

  private String klass  = "edu.internet2.middleware.grouper.GrouperGroup";
  
  
public TestNamingPrivs(String name) {
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
  

  // Test requirements for other *real* tests
  public void testRequirements() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch the groups
    // g0
    GrouperGroup g0 = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue( klass.equals( g0.getClass().getName() ) );
    Assert.assertTrue( g0.exists() );
    Assert.assertNotNull( g0.type() );
    Assert.assertNotNull( g0.attribute("stem") );
    Assert.assertTrue( g0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( g0.attribute("extension") );
    Assert.assertTrue( g0.attribute("extension").value().equals(extn0) );
    // g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue( klass.equals( g1.getClass().getName() ) );
    Assert.assertTrue( g1.exists() );
    Assert.assertNotNull( g1.type() );
    Assert.assertNotNull( g1.attribute("stem") );
    Assert.assertTrue( g1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( g1.attribute("extension") );
    Assert.assertTrue( g1.attribute("extension").value().equals(extn1) );
    // g2
    GrouperGroup g2 = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue( klass.equals( g2.getClass().getName() ) );
    Assert.assertTrue( g2.exists() );
    Assert.assertNotNull( g2.type() );
    Assert.assertNotNull( g2.attribute("stem") );
    Assert.assertTrue( g2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( g2.attribute("extension") );
    Assert.assertTrue( g2.attribute("extension").value().equals(extn2) );
    // Fetch the members
    // Fetch m0
    GrouperMember m0 = GrouperMember.lookup(m0id, m0type);
    Assert.assertNotNull(m0);
    // Fetch m1
    GrouperMember m1 = GrouperMember.lookup(m1id, m1type);
    Assert.assertNotNull(m1);
    // We're done
    s.stop();
  }

  public void testHasGrantHasRevokeHas() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch g0
    GrouperGroup g0 = GrouperGroup.load(s, stem0, extn0);
    // Fetch m0
    GrouperMember m0 = GrouperMember.lookup(m0id, m0type);

    // Assert what privs m0 has on g0
    List privs0 = Grouper.naming().has(s, g0, m0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, g0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, g0, m0, "STEM") );

    // Take a broader view and see where m0 has each of the privs
    List privs0c  = Grouper.naming().has(s, m0, "CREATE");
    List privs0s  = Grouper.naming().has(s, m0, "STEM");
    Assert.assertNotNull(privs0c);
    Assert.assertTrue( privs0c.size() == 0 );
    Assert.assertNotNull(privs0s);
    Assert.assertTrue( privs0s.size() == 0 );

    // Take a broader view and see who has each of the privs
    List whoHas0c  = Grouper.naming().whoHas(s, g0, "CREATE");
    List whoHas0s  = Grouper.naming().whoHas(s, g0, "STEM");
    Assert.assertNotNull(whoHas0c);
    Assert.assertTrue( whoHas0c.size() == 0 );
    Assert.assertNotNull(whoHas0s);
    Assert.assertTrue( whoHas0s.size() == 0 );

    // Grant m0 all privs on g0
    // We can't grant naming privs on !naming groups
    Assert.assertFalse( Grouper.naming().grant(s, g0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().grant(s, g0, m0, "STEM") );

    // Assert what privs m0 has on g0
    List privs1 = Grouper.naming().has(s, g0, m0);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, g0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, g0, m0, "STEM") );

    // Take a broader view and see where m0 has each of the privs
    List privs1c  = Grouper.naming().has(s, m0, "CREATE");
    List privs1s  = Grouper.naming().has(s, m0, "STEM");
    Assert.assertNotNull(privs1c);
    Assert.assertTrue( privs1c.size() == 0 );
    Assert.assertNotNull(privs1s);
    Assert.assertTrue( privs1s.size() == 0 );

    // Take a broader view and see who has each of the privs
    List whoHas1c  = Grouper.naming().whoHas(s, g0, "CREATE");
    List whoHas1s  = Grouper.naming().whoHas(s, g0, "STEM");
    Assert.assertNotNull(whoHas1c);
    Assert.assertTrue( whoHas1c.size() == 0 );
    Assert.assertNotNull(whoHas1s);
    Assert.assertTrue( whoHas1s.size() == 0 );

    // Revoke all privs m0 has on g0
    Assert.assertFalse( Grouper.naming().revoke(s, g0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().revoke(s, g0, m0, "STEM") );

    // Assert what privs m0 has on g0
    List privs2 = Grouper.naming().has(s, g0, m0);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, g0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, g0, m0, "STEM") );

    // Take a broader view and see where m0 has each of the privs
    List privs2c  = Grouper.naming().has(s, m0, "CREATE");
    List privs2s  = Grouper.naming().has(s, m0, "STEM");
    Assert.assertNotNull(privs2c);
    Assert.assertTrue( privs2c.size() == 0 );
    Assert.assertNotNull(privs2s);
    Assert.assertTrue( privs2s.size() == 0 );

    // Take a broader view and see who has each of the privs
    List whoHas2c  = Grouper.naming().whoHas(s, g0, "CREATE");
    List whoHas2s  = Grouper.naming().whoHas(s, g0, "STEM");
    Assert.assertNotNull(whoHas2c);
    Assert.assertTrue( whoHas2c.size() == 0 );
    Assert.assertNotNull(whoHas2s);
    Assert.assertTrue( whoHas2s.size() == 0 );

    Assert.assertTrue( privs1s.size() == 0 );

    // We're done
    s.stop();
  }

/*
  public void testHasPrivsCurrentSubject() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.load(s, stem0, extn0);
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    // Fetch the members
    // Fetch m0
    GrouperMember   m0      = GrouperMember.lookup(m0id, m0type);
    // Fetch m1
    GrouperMember   m1      = GrouperMember.lookup(m1id, m1type);

    // What privs does the current subject have on g0?
    List privs0 = Grouper.naming().has(s, g0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, g0, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, g0, "STEM") );
    Assert.assertFalse( Grouper.naming().has(s, g0, "OPTOUT") );
    Assert.assertFalse( Grouper.naming().has(s, g0, "READ") );
    Assert.assertFalse( Grouper.naming().has(s, g0, "UPDATE") );
    Assert.assertFalse( Grouper.naming().has(s, g0, "VIEW") );
    
    // What privs does the current subject have on g1?
    List privs1 = Grouper.naming().has(s, g1);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, g1, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, g1, "STEM") );
    Assert.assertFalse( Grouper.naming().has(s, g1, "OPTOUT") );
    Assert.assertFalse( Grouper.naming().has(s, g1, "READ") );
    Assert.assertFalse( Grouper.naming().has(s, g1, "UPDATE") );
    Assert.assertFalse( Grouper.naming().has(s, g1, "VIEW") );
    
    // What privs does the current subject have on g2?
    List privs2 = Grouper.naming().has(s, g2);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, g2, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, g2, "STEM") );
    Assert.assertFalse( Grouper.naming().has(s, g2, "OPTOUT") );
    Assert.assertFalse( Grouper.naming().has(s, g2, "READ") );
    Assert.assertFalse( Grouper.naming().has(s, g2, "UPDATE") );
    Assert.assertFalse( Grouper.naming().has(s, g2, "VIEW") );
   
    // Take a broader view and see where the current subject has each
    // of the privs
    List privs3a  = Grouper.naming().has(s, "CREATE");
    List privs3oi = Grouper.naming().has(s, "STEM");
    List privs3oo = Grouper.naming().has(s, "OPTOUT");
    List privs3r  = Grouper.naming().has(s, "READ");
    List privs3u  = Grouper.naming().has(s, "UPDATE");
    List privs3v  = Grouper.naming().has(s, "VIEW");
    Assert.assertNotNull(privs3a);
    Assert.assertTrue( privs3a.size() == 7 );
    Assert.assertNotNull(privs3oi);
    Assert.assertTrue( privs3oi.size() == 0 );
    Assert.assertNotNull(privs3oo);
    Assert.assertTrue( privs3oo.size() == 0 );
    Assert.assertNotNull(privs3r);
    Assert.assertTrue( privs3r.size() == 0 );
    Assert.assertNotNull(privs3u);
    Assert.assertTrue( privs3u.size() == 0 );
    Assert.assertNotNull(privs3v);
    Assert.assertTrue( privs3v.size() == 0 );

    // We're done
    s.stop();
  }
*/

    // TODO All of the above
    // TODO Do GrouperSystem for below as well?
    // TODO Grouper.naming().has(s, g0, m0);
    // TODO Grouper.naming().has(s, m0, "CREATE");
    // TODO boolean Grouper.naming().has(s, g0, m0, "CREATE");

    // TODO Grant
    // TODO Revoke

}

