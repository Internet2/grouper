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
 * $Id: TestInstantiate.java,v 1.5 2004-11-12 20:21:59 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;


public class TestInstantiate extends TestCase {

  public TestInstantiate(String name) {
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
  

  // Instantiate a Grouper instance 
  public void testGrouperInstantiate() {
    Grouper obj = new Grouper();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.Grouper";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperAttribute instance 
  public void testGrouperAttributeInstantiate() {
    GrouperAttribute obj = new GrouperAttribute();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperAttribute";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperField instance 
  public void testGrouperFieldInstantiate() {
    GrouperField obj = new GrouperField();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperField";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperGroup instance 
  public void testGrouperGroupInstantiate() {
    GrouperGroup obj = new GrouperGroup();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperGroup";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperMember instance 
  public void testGrouperMemberInstantiate() {
    GrouperMember obj = new GrouperMember();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
    GrouperMember member = new GrouperMember();
  }

  // Instantiate a GrouperMembership instance 
  public void testGrouperMembershipInstantiate() {
    GrouperMembership obj = new GrouperMembership();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperMembership";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSchema instance 
  public void testGrouperSchemaInstantiate() {
    GrouperSchema obj = new GrouperSchema();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperSchema";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSession instance
  public void testGrouperSessionInstantiate() {
    GrouperSession obj = new GrouperSession();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperSession";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSubject instance 
  public void testGrouperSubjectInstantiate() {
    GrouperSubject obj = new GrouperSubject();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperSubject";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSubjectAttribute instance 
  public void testGrouperSubjectAttributeInstantiate() {
    GrouperSubjectAttribute obj = new GrouperSubjectAttribute();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperSubjectAttribute";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperType instance 
  public void testGrouperTypeInstantiate() {
    GrouperType obj = new GrouperType();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperType";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperTypeDef instance 
  public void testGrouperTypeDefInstantiate() {
    GrouperTypeDef obj = new GrouperTypeDef();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperTypeDef";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectImpl instance 
  public void testGrouperSubjectImplInstantiate() {
    SubjectImpl obj = new SubjectImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a GrouperSubjectTypeAdapterImpl instance 
  public void testSubjectTypeAdapterImplInstantiate() {
    SubjectTypeAdapterImpl obj = new SubjectTypeAdapterImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectTypeAdapterImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectTypeImpl instance 
  public void testSubjectTypeImplInstantiate() {
    SubjectTypeImpl obj = new SubjectTypeImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectTypeImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

}

