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
 * $Id: TestConfigAndSchema.java,v 1.4 2004-11-12 04:25:41 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.lang.reflect.*;
import  java.util.*;
import  junit.framework.*;


public class TestConfigAndSchema extends TestCase {

  public TestConfigAndSchema(String name) {
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
  

  // Get a runtime configuration setting 
  public void testGetRuntimeConfigSetting() {
    Grouper G = new Grouper();
    String exp = "GrouperSystem";
    Assert.assertTrue( exp.equals( Grouper.config("member.system") ) );
  }

  // Get cached GrouperFields 
  public void testGetGrouperFields() {
    Grouper G = new Grouper();
    List fields = G.groupFields();
    Assert.assertNotNull(fields);
    Assert.assertEquals(10, fields.size());
    String klass = "edu.internet2.middleware.grouper.GrouperField";
    String field;
    field = "admins:ADMIN:ADMIN:TRUE";
    Assert.assertTrue( field.equals( fields.get(0).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(0).getClass().getName() ) );
    field = "description:READ:ADMIN:FALSE";
    Assert.assertTrue( field.equals( fields.get(1).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(1).getClass().getName() ) );
    field = "descriptor:READ:ADMIN:FALSE";
    Assert.assertTrue( field.equals( fields.get(2).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(2).getClass().getName() ) );
    field = "members:READ:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(3).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(3).getClass().getName() ) );
    field = "optins:READ:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(4).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(5).getClass().getName() ) );
    field = "optouts:READ:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(5).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(6).getClass().getName() ) );
    field = "readers:UPDATE:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(6).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(7).getClass().getName() ) );
    field = "stem:READ:ADMIN:FALSE";
    Assert.assertTrue( field.equals( fields.get(7).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(4).getClass().getName() ) );
    field = "updaters:UPDATE:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(8).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(8).getClass().getName() ) );
    field = "viewers:UPDATE:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(9).toString() ) );
    Assert.assertTrue( klass.equals( fields.get(9).getClass().getName() ) );
  }

  // Get cached GrouperTypes 
  public void testGetGrouperTypes() {
    Grouper G = new Grouper();
    List types = G.groupTypes();
    Assert.assertNotNull(types);
    Assert.assertEquals(1, types.size());
    String type = "base";
    Assert.assertTrue( type.equals( types.get(0).toString() ) );
    String klass  = "edu.internet2.middleware.grouper.GrouperType";
    Assert.assertTrue( klass.equals( types.get(0).getClass().getName() ) );
  }

  // Get cached GrouperTypeDefs 
  public void testGetGrouperTypeDefs() {
    Grouper G = new Grouper();
    List typeDefs  = G.groupTypeDefs();
    Assert.assertNotNull(typeDefs);
    Assert.assertEquals(10, typeDefs.size());
    String klass = "edu.internet2.middleware.grouper.GrouperTypeDef";
    String typeDef;
    typeDef = "base:descriptor";
    Assert.assertTrue( typeDef.equals( typeDefs.get(0).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(0).getClass().getName() ) );
    typeDef = "base:stem";
    Assert.assertTrue( typeDef.equals( typeDefs.get(1).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(1).getClass().getName() ) );
    typeDef = "base:description";
    Assert.assertTrue( typeDef.equals( typeDefs.get(2).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(2).getClass().getName() ) );
    typeDef = "base:members";
    Assert.assertTrue( typeDef.equals( typeDefs.get(3).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(3).getClass().getName() ) );
    typeDef = "base:viewers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(4).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(4).getClass().getName() ) );
    typeDef = "base:readers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(5).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(5).getClass().getName() ) );
    typeDef = "base:updaters";
    Assert.assertTrue( typeDef.equals( typeDefs.get(6).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(6).getClass().getName() ) );
    typeDef = "base:admins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(7).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(7).getClass().getName() ) );
    typeDef = "base:optins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(8).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(8).getClass().getName() ) );
    typeDef = "base:optouts";
    Assert.assertTrue( typeDef.equals( typeDefs.get(9).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(9).getClass().getName() ) );
  }

  // Get cached SubjectTypes 
  public void testGetSubjectTypes() {
    Grouper G = new Grouper();
    List types = G.subjectTypes();
    Assert.assertNotNull(types);
    Assert.assertEquals(1, types.size());
    SubjectType st = (SubjectType) types.get(0);
    // XXX String adapterClass = "edu.internet2.middleware.grouper.GrouperSubjectTypeAdapterImpl";
    // XXX Assert.assertTrue( adapterClass.equals( st.adapterClass().getClass().getName() ) );
    String name         = "Person";
    Assert.assertTrue( name.equals( st.getName() ) );
    String typeID       = "person";
    Assert.assertTrue( typeID.equals( st.getId() ) );
  }

  // TODO Test boolean assertion|validity methods

}

