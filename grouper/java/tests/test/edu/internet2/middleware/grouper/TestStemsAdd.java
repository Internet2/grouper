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
 * $Id: TestStemsAdd.java,v 1.3 2004-11-25 03:04:47 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;


public class TestNamespaces extends TestCase {

  private String stem0  = null;
  private String stem1  = null;
  private String stem2  = null;
  private String stem00 = "stem.0";
  private String extn0  = "stem.0";
  private String extn1  = "stem.1";
  private String extn2  = "stem.2";
  private String extn00 = "stem.0.0";
  
  private String klass  = "edu.internet2.middleware.grouper.GrouperGroup";
  private String type   = "naming";


  public TestNamespaces(String name) {
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
  

  // Fetch a non-existent namespaces
  public void testGroupsExistFalse() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Confirm that namespaces don't exist
    GrouperGroup    ns0   = GrouperGroup.load(s, stem0, extn0, type);
    Assert.assertNotNull(ns0);
    Assert.assertFalse( ns0.exists() );
    GrouperGroup    ns1   = GrouperGroup.load(s, stem1, extn1, type);
    Assert.assertNotNull(ns1);
    Assert.assertFalse( ns1.exists() );
    GrouperGroup    ns2   = GrouperGroup.load(s, stem2, extn2, type);
    Assert.assertNotNull(ns2);
    Assert.assertFalse( ns2.exists() );
    GrouperGroup    ns00  = GrouperGroup.load(s, stem00, extn00, type);
    Assert.assertNotNull(ns00);
    Assert.assertFalse( ns00.exists() );
    // We're done
    s.stop();
  }

  // Create namespaces
  public void testCreateGroups() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Create the namespaces
    // ns0
    GrouperGroup ns0 = GrouperGroup.create(s, stem0, extn0, type);
    Assert.assertNotNull(ns0);
    Assert.assertTrue( klass.equals( ns0.getClass().getName() ) );
    Assert.assertTrue( ns0.exists() );
    Assert.assertNotNull( ns0.type() );
    Assert.assertTrue( ns0.type().equals(type) ); 
    // FIXME Shouldn't this be null?
    Assert.assertNotNull( ns0.attribute("stem") );
    // FIXME *shrug*
    // Assert.assertTrue( ns0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( ns0.attribute("extension") );
    Assert.assertTrue( ns0.attribute("extension").value().equals(extn0) );

    // ns1
    GrouperGroup ns1 = GrouperGroup.create(s, stem1, extn1, type);
    Assert.assertNotNull(ns1);
    Assert.assertTrue( klass.equals( ns1.getClass().getName() ) );
    Assert.assertTrue( ns1.exists() );
    Assert.assertNotNull( ns1.type() );
    Assert.assertTrue( ns1.type().equals(type) ); 
    // FIXME Shouldn't this be null?
    Assert.assertNotNull( ns1.attribute("stem") );
    // FIXME *shrug*
    // Assert.assertTrue( ns1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( ns1.attribute("extension") );
    Assert.assertTrue( ns1.attribute("extension").value().equals(extn1) );

    // ns2
    GrouperGroup ns2 = GrouperGroup.create(s, stem2, extn2, type);
    Assert.assertNotNull(ns2);
    Assert.assertTrue( klass.equals( ns2.getClass().getName() ) );
    Assert.assertTrue( ns2.exists() );
    Assert.assertNotNull( ns2.type() );
    Assert.assertTrue( ns2.type().equals(type) ); 
    // FIXME Shouldn't this be null?
    Assert.assertNotNull( ns2.attribute("stem") );
    // FIXME *shrug*
    // Assert.assertTrue( ns2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( ns2.attribute("extension") );
    Assert.assertTrue( ns2.attribute("extension").value().equals(extn2) );

    // ns00
    GrouperGroup ns00 = GrouperGroup.create(s, stem00, extn00, type);
    Assert.assertNotNull(ns00);
    Assert.assertTrue( klass.equals( ns00.getClass().getName() ) );
    Assert.assertTrue( ns00.exists() );
    Assert.assertNotNull( ns00.type() );
    Assert.assertTrue( ns00.type().equals(type) ); 
    Assert.assertNotNull( ns00.attribute("stem") );
    Assert.assertTrue( ns00.attribute("stem").value().equals(stem00) );
    Assert.assertNotNull( ns00.attribute("extension") );
    Assert.assertTrue( ns00.attribute("extension").value().equals(extn00) );

    // We're done
    s.stop();
  }

  // Fetch valid namespaces
  public void testFetchValidNamespaces() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch the namespaces
    // ns0
    GrouperGroup ns0 = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(ns0);
    Assert.assertTrue( klass.equals( ns0.getClass().getName() ) );
    // FIXME Assert.assertTrue( ns0.exists() );
    // FIXME Assert.assertNotNull( ns0.type() );
    // FIXME Assert.assertTrue( ns0.type().equals(type) ); 
    // FIXME Shouldn't this be null?
    // Assert.assertNotNull( ns0.attribute("stem") );
    // FIXME *shrug*
    // Assert.assertTrue( ns0.attribute("stem").value().equals(stem0) );
    // FIXME Assert.assertNotNull( ns0.attribute("extension") );
    // FIXME Assert.assertTrue( ns0.attribute("extension").value().equals(extn0) );

    // ns1
    GrouperGroup ns1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(ns1);
    Assert.assertTrue( klass.equals( ns1.getClass().getName() ) );
    // FIXME Assert.assertTrue( ns1.exists() );
    // FIXME Assert.assertNotNull( ns1.type() );
    // FIXME Assert.assertTrue( ns1.type().equals(type) ); 
    // FIXME Shouldn't this be null?
    // Assert.assertNotNull( ns1.attribute("stem") );
    // FIXME *shrug*
    // Assert.assertTrue( ns1.attribute("stem").value().equals(stem1) );
    // FIXME Assert.assertNotNull( ns1.attribute("extension") );
    // FIXME Assert.assertTrue( ns1.attribute("extension").value().equals(extn1) );

    // ns2
    GrouperGroup ns2 = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(ns2);
    Assert.assertTrue( klass.equals( ns2.getClass().getName() ) );
    // FIXME Assert.assertTrue( ns2.exists() );
    // FIXME Assert.assertNotNull( ns2.type() );
    // FIXME Assert.assertTrue( ns2.type().equals(type) ); 
    // FIXME Shouldn't this be null?
    // Assert.assertNotNull( ns2.attribute("stem") );
    // FIXME *shrug*
    // Assert.assertTrue( ns2.attribute("stem").value().equals(stem2) );
    // FIXME Assert.assertNotNull( ns2.attribute("extension") );
    // FIXME Assert.assertTrue( ns2.attribute("extension").value().equals(extn2) );

    // ns00
    GrouperGroup ns00 = GrouperGroup.load(s, stem00, extn00);
    Assert.assertNotNull(ns00);
    Assert.assertTrue( klass.equals( ns00.getClass().getName() ) );
    // FIXME Assert.assertTrue( ns00.exists() );
    // FIXME Assert.assertNotNull( ns00.type() );
    // FIXME Assert.assertTrue( ns00.type().equals(type) ); 
    // FIXME Assert.assertNotNull( ns00.attribute("stem") );
    // FIXME *shrug*
    // Assert.assertTrue( ns00.attribute("stem").value().equals(stem00) );
    // FIXME Assert.assertNotNull( ns00.attribute("extension") );
    // FIXME Assert.assertTrue( ns00.attribute("extension").value().equals(extn00) );

    // We're done
    s.stop();
  }

  // TODO Assert ADMIN priv (create + fetch)
  // TODO Delete group

}

