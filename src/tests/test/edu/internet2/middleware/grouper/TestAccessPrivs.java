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
 * $Id: TestAccessPrivs.java,v 1.8 2004-11-23 22:16:43 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;


public class TestAccessPrivs extends TestCase {

  private String desc0  = "desc.0";
  private String desc1  = "desc.1";
  private String desc2  = "desc.2";
  private String klass  = "edu.internet2.middleware.grouper.GrouperGroup";
  private String m0id   = "blair";
  private String m0type = "person";
  private String m1id   = "notblair";
  private String m1type = "person";
  private String stem0  = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";

  public TestAccessPrivs(String name) {
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
    // Fetch the members
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup(m0id, m0type);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup(m1id, m1type);
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
    // Fetch Group 0
    GrouperGroup  grp0 = GrouperGroup.load(s, stem0, desc0);
    // Fetch Member 0
    GrouperMember m0   = GrouperMember.lookup(m0id, m0type);

    // Assert what privs m0 has on grp0
    List privs0 = Grouper.access().has(s, grp0, m0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 0 );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "READ") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "VIEW") );

    // Take a broader view and see where m0 has each of the privs
    List privs0a  = Grouper.access().has(s, m0, "ADMIN");
    List privs0oi = Grouper.access().has(s, m0, "OPTIN");
    List privs0oo = Grouper.access().has(s, m0, "OPTOUT");
    List privs0r  = Grouper.access().has(s, m0, "READ");
    List privs0u  = Grouper.access().has(s, m0, "UPDATE");
    List privs0v  = Grouper.access().has(s, m0, "VIEW");
    Assert.assertNotNull(privs0a);
    Assert.assertTrue( privs0a.size() == 0 );
    Assert.assertNotNull(privs0oi);
    Assert.assertTrue( privs0oi.size() == 0 );
    Assert.assertNotNull(privs0oo);
    Assert.assertTrue( privs0oo.size() == 0 );
    Assert.assertNotNull(privs0r);
    Assert.assertTrue( privs0r.size() == 0 );
    Assert.assertNotNull(privs0u);
    Assert.assertTrue( privs0u.size() == 0 );
    Assert.assertNotNull(privs0v);
    Assert.assertTrue( privs0v.size() == 0 );

    // Grant m0 all privs on grp0
    Assert.assertTrue( Grouper.access().grant(s, grp0, m0, "ADMIN") );
    Assert.assertTrue( Grouper.access().grant(s, grp0, m0, "OPTIN") );
    Assert.assertTrue( Grouper.access().grant(s, grp0, m0, "OPTOUT") );
    Assert.assertTrue( Grouper.access().grant(s, grp0, m0, "READ") );
    Assert.assertTrue( Grouper.access().grant(s, grp0, m0, "UPDATE") );
    Assert.assertTrue( Grouper.access().grant(s, grp0, m0, "VIEW") );

    // Assert what privs m0 has on grp0
    List privs1 = Grouper.access().has(s, grp0, m0);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 6 );
    Assert.assertTrue( Grouper.access().has(s, grp0, m0, "ADMIN") );
    Assert.assertTrue( Grouper.access().has(s, grp0, m0, "OPTIN") );
    Assert.assertTrue( Grouper.access().has(s, grp0, m0, "OPTOUT") );
    Assert.assertTrue( Grouper.access().has(s, grp0, m0, "READ") );
    Assert.assertTrue( Grouper.access().has(s, grp0, m0, "UPDATE") );
    Assert.assertTrue( Grouper.access().has(s, grp0, m0, "VIEW") );

    // Take a broader view and see where m0 has each of the privs
    List privs1a  = Grouper.access().has(s, m0, "ADMIN");
    List privs1oi = Grouper.access().has(s, m0, "OPTIN");
    List privs1oo = Grouper.access().has(s, m0, "OPTOUT");
    List privs1r  = Grouper.access().has(s, m0, "READ");
    List privs1u  = Grouper.access().has(s, m0, "UPDATE");
    List privs1v  = Grouper.access().has(s, m0, "VIEW");
    Assert.assertNotNull(privs1a);
    Assert.assertTrue( privs1a.size() == 1 );
    Assert.assertNotNull(privs1oi);
    Assert.assertTrue( privs1oi.size() == 1 );
    Assert.assertNotNull(privs1oo);
    Assert.assertTrue( privs1oo.size() == 1 );
    Assert.assertNotNull(privs1r);
    Assert.assertTrue( privs1r.size() == 1 );
    Assert.assertNotNull(privs1u);
    Assert.assertTrue( privs1u.size() == 1 );
    Assert.assertNotNull(privs1v);
    Assert.assertTrue( privs1v.size() == 1 );

    // Revoke all privs m0 has on grp0
    Assert.assertTrue( Grouper.access().revoke(s, grp0, m0, "ADMIN") );
    Assert.assertTrue( Grouper.access().revoke(s, grp0, m0, "OPTIN") );
    Assert.assertTrue( Grouper.access().revoke(s, grp0, m0, "OPTOUT") );
    Assert.assertTrue( Grouper.access().revoke(s, grp0, m0, "READ") );
    Assert.assertTrue( Grouper.access().revoke(s, grp0, m0, "UPDATE") );
    Assert.assertTrue( Grouper.access().revoke(s, grp0, m0, "VIEW") );

    // Assert what privs m0 has on grp0
    List privs2 = Grouper.access().has(s, grp0, m0);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 0 );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "READ") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, grp0, m0, "VIEW") );

    // Take a broader view and see where m0 has each of the privs
    List privs2a  = Grouper.access().has(s, m0, "ADMIN");
    List privs2oi = Grouper.access().has(s, m0, "OPTIN");
    List privs2oo = Grouper.access().has(s, m0, "OPTOUT");
    List privs2r  = Grouper.access().has(s, m0, "READ");
    List privs2u  = Grouper.access().has(s, m0, "UPDATE");
    List privs2v  = Grouper.access().has(s, m0, "VIEW");
    Assert.assertNotNull(privs2a);
    Assert.assertTrue( privs2a.size() == 0 );
    Assert.assertNotNull(privs2oi);
    Assert.assertTrue( privs2oi.size() == 0 );
    Assert.assertNotNull(privs2oo);
    Assert.assertTrue( privs2oo.size() == 0 );
    Assert.assertNotNull(privs2r);
    Assert.assertTrue( privs2r.size() == 0 );
    Assert.assertNotNull(privs2u);
    Assert.assertTrue( privs2u.size() == 0 );
    Assert.assertNotNull(privs2v);
    Assert.assertTrue( privs2v.size() == 0 );

    // We're done
    s.stop();
  }

  public void testHasPrivsCurrentSubject() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch the groups
    // Group 0
    GrouperGroup    grp0  = GrouperGroup.load(s, stem0, desc0);
    // Group 1
    GrouperGroup    grp1  = GrouperGroup.load(s, stem1, desc1);
    // Group 2
    GrouperGroup    grp2  = GrouperGroup.load(s, stem2, desc2);
    // Fetch the members
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup(m0id, m0type);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup(m1id, m1type);

    // What privs does the current subject have on grp0?
    List privs0 = Grouper.access().has(s, grp0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 1 );
    Assert.assertTrue( Grouper.access().has(s, grp0, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, grp0, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, grp0, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, grp0, "READ") );
    Assert.assertFalse( Grouper.access().has(s, grp0, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, grp0, "VIEW") );
    
    // What privs does the current subject have on grp1?
    List privs1 = Grouper.access().has(s, grp1);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 1 );
    Assert.assertTrue( Grouper.access().has(s, grp1, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, grp1, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, grp1, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, grp1, "READ") );
    Assert.assertFalse( Grouper.access().has(s, grp1, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, grp1, "VIEW") );
    
    // What privs does the current subject have on grp2?
    List privs2 = Grouper.access().has(s, grp2);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 1 );
    Assert.assertTrue( Grouper.access().has(s, grp2, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, grp2, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, grp2, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, grp2, "READ") );
    Assert.assertFalse( Grouper.access().has(s, grp2, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, grp2, "VIEW") );
   
    // Take a broader view and see where the current subject has each
    // of the privs
    List privs3a  = Grouper.access().has(s, "ADMIN");
    List privs3oi = Grouper.access().has(s, "OPTIN");
    List privs3oo = Grouper.access().has(s, "OPTOUT");
    List privs3r  = Grouper.access().has(s, "READ");
    List privs3u  = Grouper.access().has(s, "UPDATE");
    List privs3v  = Grouper.access().has(s, "VIEW");
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

    // TODO All of the above
    // TODO Do GrouperSystem for below as well?
    // TODO Grouper.access().has(s, grp0, m0);
    // TODO Grouper.access().has(s, m0, "ADMIN");
    // TODO boolean Grouper.access().has(s, grp0, m0, "ADMIN");

    // TODO Grant
    // TODO Revoke

}

