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
 * $Id: GrouperTest.java,v 1.51 2004-09-20 00:21:19 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.io.BufferedReader;
import  java.io.File;
import  java.io.FileReader;
import  java.io.IOException;
import  java.lang.reflect.*;
import  java.sql.*;
import  java.util.*;
import  junit.framework.*;

public class GrouperTest extends TestCase {

  public Grouper G;

  public GrouperTest(String name) {
    super(name);
  }

  protected void setUp () {
    // Nothing -- Yet
  }

  protected void tearDown () {
    // Nothing -- Yet
  }


  //
  // Class: Grouper
  //

  // Instantiate a Grouper instance 
  public void testGrouperInstantiate() {
    // Establish a new Grouper instance
    G = new Grouper();

    Class  klass    = G.getClass();
    String expKlass = "edu.internet2.middleware.grouper.Grouper";

    Assert.assertNotNull(G);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
    G.destroy();
  }

  // Initialize Grouper environment
  public void testGrouperInitialize() {
    G = new Grouper();

    try {
      G.init();
    } catch(Exception e) {
      Assert.fail("Exception thrown when initializing Grouper");
    }
    G.destroy();
  }

  // Get a runtime configuration setting 
  public void testGetRuntimeConfigSetting() {
    G = new Grouper();
    G.init();
    
    String expVal = "GrouperSystem";
    Assert.assertTrue( expVal.equals( G.config("member.system") ) );
    G.destroy();
  }


  //
  // Class: GrouperField
  //
   

  // Instantiate a GrouperField instance 
  public void testGrouperFieldInstantiate() {
    GrouperField field = new GrouperField();

    Class  klass    = field.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperField";

    Assert.assertNotNull(field);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  // Get cached GrouperFields 
  public void testGetGrouperFields() {
    G = new Grouper();
    G.init();
    List fields = G.groupFields();
    Assert.assertNotNull(fields);
    Assert.assertEquals(10, fields.size());

    String expKlass = "edu.internet2.middleware.grouper.GrouperField";
    String field;
    field = "admins:ADMIN:ADMIN:TRUE";
    Assert.assertTrue( field.equals( fields.get(0).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(0).getClass().getName() ) );
    field = "description:READ:ADMIN:FALSE";
    Assert.assertTrue( field.equals( fields.get(1).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(1).getClass().getName() ) );
    field = "descriptor:READ:ADMIN:FALSE";
    Assert.assertTrue( field.equals( fields.get(2).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(2).getClass().getName() ) );
    field = "members:READ:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(3).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(3).getClass().getName() ) );
    field = "optins:READ:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(4).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(4).getClass().getName() ) );
    field = "optouts:READ:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(5).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(5).getClass().getName() ) );
    field = "readers:UPDATE:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(6).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(6).getClass().getName() ) );
    field = "stem:READ:ADMIN:FALSE";
    Assert.assertTrue( field.equals( fields.get(7).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(7).getClass().getName() ) );
    field = "updaters:UPDATE:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(8).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(8).getClass().getName() ) );
    field = "viewers:UPDATE:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(9).toString() ) );
    Assert.assertTrue( expKlass.equals( fields.get(9).getClass().getName() ) );

    G.destroy();
  }

  //
  // Class: GrouperType
  //
 
  // Instantiate a GrouperType instance 
  public void testGrouperTypeInstantiate() {
    GrouperType type = new GrouperType();

    Class  klass    = type.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperType";

    Assert.assertNotNull(type);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  // Get cached GrouperTypes 
  public void testGetGrouperTypes() {
    G = new Grouper();
    G.init();
    List types = G.groupTypes();
    Assert.assertNotNull(types);
    Assert.assertEquals(1, types.size());

    String expKlass = "edu.internet2.middleware.grouper.GrouperType";
    String type = "base";
    Assert.assertTrue( type.equals( types.get(0).toString() ) );
    Assert.assertTrue( expKlass.equals( types.get(0).getClass().getName() ) );

    G.destroy();
  }

  //
  // Class: GrouperTypeDef
  // 
 
  // Instantiate a GrouperTypeDef instance 
  public void testGrouperTypeDefInstantiate() {
    GrouperTypeDef typeDef = new GrouperTypeDef();

    Class  klass    = typeDef.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperTypeDef";

    Assert.assertNotNull(typeDef);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  // Get cached GrouperTypeDefs 
  public void testGetGrouperTypeDefs() {
    G = new Grouper();
    G.init();
    List typeDefs  = G.groupTypeDefs();
    Assert.assertNotNull(typeDefs);
    Assert.assertEquals(10, typeDefs.size());

    String expKlass = "edu.internet2.middleware.grouper.GrouperTypeDef";
    String typeDef;
    typeDef = "base:stem";
    Assert.assertTrue( typeDef.equals( typeDefs.get(0).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(0).getClass().getName() ) );
    typeDef = "base:descriptor";
    Assert.assertTrue( typeDef.equals( typeDefs.get(1).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(1).getClass().getName() ) );
    typeDef = "base:description";
    Assert.assertTrue( typeDef.equals( typeDefs.get(2).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(2).getClass().getName() ) );
    typeDef = "base:members";
    Assert.assertTrue( typeDef.equals( typeDefs.get(3).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(3).getClass().getName() ) );
    typeDef = "base:viewers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(4).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(4).getClass().getName() ) );
    typeDef = "base:readers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(5).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(5).getClass().getName() ) );
    typeDef = "base:updaters";
    Assert.assertTrue( typeDef.equals( typeDefs.get(6).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(6).getClass().getName() ) );
    typeDef = "base:admins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(7).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(7).getClass().getName() ) );
    typeDef = "base:optins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(8).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(8).getClass().getName() ) );
    typeDef = "base:optouts";
    Assert.assertTrue( typeDef.equals( typeDefs.get(9).toString() ) );
    Assert.assertTrue( expKlass.equals( typeDefs.get(9).getClass().getName() ) );

    G.destroy();
  }
 
  //
  // Class: GrouperMember
  //
   
  // Instantiate a GrouperMembership instance 
  public void testGrouperMemberInstantiate() {
    GrouperMember member = new GrouperMember();

    Class  klass    = member.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMember";

    Assert.assertNotNull(member);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  //
  // Class: GrouperSubject
  //
   
  public void testGrouperSubjectInstantiate() {
    GrouperSubject subject = new GrouperSubject();
    String klass = "edu.internet2.middleware.grouper.GrouperSubject";
    Assert.assertNotNull(subject);
    Assert.assertTrue( klass.equals( subject.getClass().getName() ) );
  }

  public void testGrouperSubjectClassLookupFailure() {
    Grouper G = new Grouper();
    G.init();
    String id   = "invalid id";
    String type = "person";
    GrouperMember m = GrouperSubject.lookup(id, type);
    Assert.assertNull(m);
  }

  public void testGrouperSubjectClassLookupMemberSystem() {
    Grouper G = new Grouper();
    G.init();
    String id   = G.config("member.system");
    String type = "person";
    GrouperMember m = GrouperSubject.lookup(id, type);
    String klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertNotNull(m);
    Assert.assertTrue( klass.equals( m.getClass().getName() ) );
    Assert.assertTrue( id.equals( m.memberID() ) );
    Assert.assertTrue( type.equals( m.memberType() ) );
    G.destroy();
  }

  public void testGrouperSubjectClassLookup() {
    Grouper G = new Grouper();
    G.init();
    String id   = "blair";
    String type = "person";
    GrouperMember m = GrouperSubject.lookup(id, type);
    String klass = "edu.internet2.middleware.grouper.GrouperMember";
    Assert.assertNotNull(m);
    Assert.assertTrue( klass.equals( m.getClass().getName() ) );
    Assert.assertTrue( id.equals( m.memberID() ) );
    Assert.assertTrue( type.equals( m.memberType() ) );
    G.destroy();
  }

  //
  // Class: GrouperSession
  //

  // Instantiate a Grouper session 
  public void testSessionInstantiate() {
    // Establish a new Grouper instance
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    String klass = "edu.internet2.middleware.grouper.GrouperSession";
    Assert.assertNotNull(s);
    Assert.assertTrue( klass.equals( s.getClass().getName() ) );
    G.destroy();
  }

  // Start a session as SubjectID "member.system"
  public void testSessionStartAsMemberSystem() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    GrouperMember subject = GrouperSubject.lookup( G.config("member.system"), "person" );
    try {
      s.start(G, subject);
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    G.destroy();
  }
  
  // Start and end a session as SubjectID "member.system"
  public void testSessionStartEndAsMemberSystem() {
    G = new Grouper();
    G.init();
    GrouperSession s= new GrouperSession();
    GrouperMember subject = GrouperSubject.lookup( G.config("member.system"), "person" );
    try {
      s.start(G, subject);
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    try {
      s.stop();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending session");
    }
    G.destroy();
  }
  
  // Attempt to end a session that hasn't been started 
  public void testSessionEndWithoutStart() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    try {
      // XXX This may fail if we start throwing exceptions. 
      s.stop();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending unstarted session");
    }
    G.destroy();
  }

  // Verify the subject of the current session 
  public void testSessionSubject() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    GrouperMember subject = GrouperSubject.lookup( G.config("member.system"), "person" );
    s.start(G, subject);

    GrouperMember m = s.subject();

    Class klass     = m.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMember";

    Assert.assertNotNull(m);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );

    G.destroy();
  }

  //
  // Class: GrouperSchema
  //
   
  // Instantiate a GrouperSchema instance 
  public void testGrouperSchemaInstantiate() {
    GrouperSchema schema = new GrouperSchema();

    Class  klass    = schema.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperSchema";

    Assert.assertNotNull(schema);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  //
  // Class: GrouperAttribute
  //

  // Instantiate a GrouperAttribute instance 
  public void testGrouperAttributeInstantiate() {
    GrouperAttribute attr = new GrouperAttribute();

    Class  klass    = attr.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperAttribute";

    Assert.assertNotNull(attr);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  //
  // Class: GrouperMembership
  //
   

  // Instantiate a GrouperMembership instance 
  public void testGrouperMembershipInstantiate() {
    GrouperMembership list = new GrouperMembership();

    Class  klass    = list.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMembership";

    Assert.assertNotNull(list);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  //
  // Class: GrouperMembers
  //
   

  // Instantiate a GrouperMembership instance 
  public void testGrouperMembersInstantiate() {
    GrouperMembers members = new GrouperMembers();

    Class  klass    = members.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMembers";

    Assert.assertNotNull(members);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  //
  // Class: GrouperMemberType
  // 

  // Instantiate a GrouperMembership instance 
  public void testGrouperMemberTypeInstantiate() {
    GrouperMemberType memberType = new GrouperMemberType();

    Class  klass    = memberType.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMemberType";

    Assert.assertNotNull(memberType);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  //
  // Class: GrouperGroup
  //
   
  // Instantiate a GrouperGroup instance 
  public void testGrouperGroupInstantiate() {
    GrouperGroup g = new GrouperGroup();

    Class  klass    = g.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperGroup";

    Assert.assertNotNull(g);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  // Does Group exist?  No.
  public void testGroupExistFalse() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    GrouperMember subject = GrouperSubject.lookup( G.config("member.system"), "person" );
    s.start(G, subject);

    GrouperGroup g = GrouperGroup.load(s, "stem.0", "desc.0");

    // Confirm that group doesn't exist
    Assert.assertFalse( g.exist() );

    // We're done
    s.stop();
    G.destroy();
  }

  // Create a group
  public void testCreateGroup() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    GrouperMember subject = GrouperSubject.lookup( G.config("member.system"), "person" );
    s.start(G, subject);

    // Create the group
    // GrouperGroup grp = GrouperGroup.create(s, "stem.1", "desc.1");

    // Confirm that the group exists
    // Assert.assertTrue( grp.exist() );
    
    GrouperGroup grp = new GrouperGroup();
    // Attach a session
    grp.session(s);

    // Identify the group
    grp.attribute("stem", "stem.1");
    grp.attribute("descriptor", "descriptor.1");
    // Describe the group
    grp.attribute("description", "group.1");

    // Create it
    grp.create(s);

    // Confirm that the group exists
    Assert.assertTrue( grp.exist() );

    // We're done
    s.stop();
    G.destroy();
  }

  // Fetch a group
  public void testFetchGroup() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    GrouperMember subject = GrouperSubject.lookup( G.config("member.system"), "person" );
    s.start(G, subject);
    GrouperGroup grp = new GrouperGroup();
    // Attach a session
    grp.session(s);

    // Identify the group
    grp.attribute("stem", "stem.1");
    grp.attribute("descriptor", "descriptor.1");

    // Confirm the class type of the group
    String klass = "edu.internet2.middleware.grouper.GrouperGroup";

    Assert.assertNotNull(grp);
    Assert.assertTrue( klass.equals( grp.getClass().getName() ) );
  
    // Confirm that we can fetch an attribute and that it 
    // matches what we expect. 
    GrouperAttribute description  = grp.attribute("description");
    String expDescription         = "group.1";

    //Assert.assertNotNull(description);
    //Assert.assertTrue( expDescription.equals(description.value()) );

    // We're done
    s.stop();
    G.destroy();
  }

}

