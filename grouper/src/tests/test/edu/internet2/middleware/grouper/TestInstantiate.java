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
 * $Id: TestInstantiate.java,v 1.10 2004-11-23 19:28:47 blair Exp $
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
  

  // Instantiate a GrouperAccessImpl instance 
  public void testGrouperAccessImplInstantiate() {
    GrouperAccess obj = new GrouperAccessImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperAccessImpl";
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

  // Instantiate a GrouperList instance 
  public void testGrouperListInstantiate() {
    GrouperList obj = new GrouperList();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperList";
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

  // Instantiate a GrouperNamingImpl instance 
  public void testGrouperNamingImplInstantiate() {
    GrouperNaming obj = new GrouperNamingImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperNamingImpl";
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

  // Instantiate a GrouperVia instance 
  public void testGrouperViaInstantiate() {
    GrouperVia obj = new GrouperVia();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.GrouperVia";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectImpl instance 
  public void testGrouperSubjectImplInstantiate() {
    SubjectImpl obj = new SubjectImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectTypeAdapterGroupImpl instance 
  public void testSubjectTypeAdapterGroupImplInstantiate() {
    SubjectTypeAdapterGroupImpl obj = new SubjectTypeAdapterGroupImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectTypeAdapterGroupImpl";
    Assert.assertTrue( klass.equals( obj.getClass().getName() ) );
  }

  // Instantiate a SubjectTypeAdapterPersonImpl instance 
  public void testSubjectTypeAdapterPersonImplInstantiate() {
    SubjectTypeAdapterPersonImpl obj = new SubjectTypeAdapterPersonImpl();
    Assert.assertNotNull(obj);
    String klass = "edu.internet2.middleware.grouper.SubjectTypeAdapterPersonImpl";
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

