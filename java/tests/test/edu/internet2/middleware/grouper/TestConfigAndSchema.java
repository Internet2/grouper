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
 * $Id: TestConfigAndSchema.java,v 1.11 2004-11-25 03:04:47 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
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
    String exp = "GrouperSystem";
    Assert.assertTrue( exp.equals( Grouper.config("member.system") ) );
  }

  // Get cached GrouperFields 
  public void testGetGrouperFields() {
    List fields = Grouper.groupFields();
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
    field = "extension:READ:ADMIN:FALSE";
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
    List types = Grouper.groupTypes();
    Assert.assertNotNull(types);
    Assert.assertEquals(2, types.size());
    String klass  = "edu.internet2.middleware.grouper.GrouperType";
    String type0 = "base";
    Assert.assertTrue( type0.equals( types.get(0).toString() ) );
    Assert.assertTrue( klass.equals( types.get(0).getClass().getName() ) );
    String type1 = "naming";
    Assert.assertTrue( type1.equals( types.get(1).toString() ) );
    Assert.assertTrue( klass.equals( types.get(1).getClass().getName() ) );
  }

  // Get cached GrouperTypeDefs 
  public void testGetGrouperTypeDefs() {
    List typeDefs  = Grouper.groupTypeDefs();
    Assert.assertNotNull(typeDefs);
    Assert.assertEquals(22, typeDefs.size());
    String klass = "edu.internet2.middleware.grouper.GrouperTypeDef";
    String typeDef;
    typeDef = "base:extension";
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
    typeDef = "naming:extension";
    Assert.assertTrue( typeDef.equals( typeDefs.get(10).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(10).getClass().getName() ) );
    typeDef = "naming:stem";
    Assert.assertTrue( typeDef.equals( typeDefs.get(11).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(11).getClass().getName() ) );
    typeDef = "naming:description";
    Assert.assertTrue( typeDef.equals( typeDefs.get(12).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(12).getClass().getName() ) );
    typeDef = "naming:members";
    Assert.assertTrue( typeDef.equals( typeDefs.get(13).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(13).getClass().getName() ) );
    typeDef = "naming:viewers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(14).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(14).getClass().getName() ) );
    typeDef = "naming:readers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(15).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(15).getClass().getName() ) );
    typeDef = "naming:updaters";
    Assert.assertTrue( typeDef.equals( typeDefs.get(16).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(16).getClass().getName() ) );
    typeDef = "naming:admins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(17).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(17).getClass().getName() ) );
    typeDef = "naming:optins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(18).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(18).getClass().getName() ) );
    typeDef = "naming:optouts";
    Assert.assertTrue( typeDef.equals( typeDefs.get(19).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(19).getClass().getName() ) );
    typeDef = "naming:creators";
    Assert.assertTrue( typeDef.equals( typeDefs.get(20).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(20).getClass().getName() ) );
    typeDef = "naming:stemmers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(21).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(21).getClass().getName() ) );
  }

  // Get cached SubjectTypes 
  public void testGetSubjectTypes() {
    List    types           = Grouper.subjectTypes();
    Assert.assertNotNull(types);
    Assert.assertEquals(2, types.size());

    SubjectType stGroup         = Grouper.subjectType("group");
    Assert.assertNotNull(stGroup);
    String klassGroup           = "edu.internet2.middleware.grouper.SubjectTypeImpl";
    Assert.assertTrue( klassGroup.equals( stGroup.getClass().getName()) );
    Assert.assertNotNull( stGroup.getId() );
    Assert.assertTrue( stGroup.getId().equals( "group" ) );
    Assert.assertNotNull( stGroup.getName() );
    Assert.assertTrue( stGroup.getName().equals( "Group" ) );
    SubjectTypeAdapter staGroup = stGroup.getAdapter();
    Assert.assertNotNull( staGroup );
    String klassGroupAdapter    = "edu.internet2.middleware.grouper.SubjectTypeAdapterGroupImpl";
    Assert.assertTrue( klassGroupAdapter.equals( staGroup.getClass().getName() ) );
    Assert.assertNotNull( staGroup.getClass().getName() );
    
    SubjectType stPerson        = Grouper.subjectType("person");
    Assert.assertNotNull(stPerson);
    String klassPerson          = "edu.internet2.middleware.grouper.SubjectTypeImpl";
    Assert.assertTrue( klassPerson.equals( stPerson.getClass().getName()) );
    Assert.assertNotNull( stPerson.getId() );
    Assert.assertTrue( stPerson.getId().equals( "person" ) );
    Assert.assertNotNull( stPerson.getName() );
    Assert.assertTrue( stPerson.getName().equals( "Person" ) );
    SubjectTypeAdapter staPerson = stPerson.getAdapter();
    Assert.assertNotNull( staPerson );
    String klassPersonAdapter    = "edu.internet2.middleware.grouper.SubjectTypeAdapterPersonImpl";
    Assert.assertTrue( klassPersonAdapter.equals( staPerson.getClass().getName() ) );
    Assert.assertNotNull( staPerson.getClass().getName() );
  }

  // TODO Test boolean assertion|validity methods

}

