/* 
 * Copyright (C) 2004 TODO
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
 * $Id: GrouperTest.java,v 1.29 2004-09-08 23:21:32 blair Exp $
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

  /*
   * Class: GrouperGroup
   */

  /* Instantiate a Grouper instance */
  public void testGrouperInstantiate() {
    // Establish a new Grouper instance
    G = new Grouper();

    Class  klass    = G.getClass();
    String expKlass = "edu.internet2.middleware.grouper.Grouper";

    Assert.assertNotNull(G);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
    G.destroy();
  }

  /* Initialize Grouper environment */
  public void testGrouperInitialize() {
    G = new Grouper();

    try {
      G.init();
    } catch(Exception e) {
      Assert.fail("Exception thrown when initializing Grouper");
    }
    G.destroy();
  }

  /* Get a runtime configuration setting */
  public void testGetRuntimeConfigSetting() {
    G = new Grouper();
    G.init();
    
    String expVal = "GrouperSystem";
    Assert.assertTrue( expVal.equals( G.config("member.system") ) );
    G.destroy();
  }


  /*
   * Class: GrouperSession
   */

  /* Instantiate a Grouper session */
  public void testSessionInstantiate() {
    // Establish a new Grouper instance
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();

    Class klass     = s.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperSession";

    Assert.assertNotNull(s);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
    G.destroy();
  }

  /* Start a session as SubjectID "member.system", 1 argument method */
  public void testSessionStartAsMemberSystemOneArgMethod() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    try {
      s.start( G, G.config("member.system") );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    G.destroy();
  }
  
  /* Start a session as SubjectID "member.system", 2 argument method */
  public void testSessionStartAsMemberSystemTwoArgMethod() {
    G = new Grouper();
    G.init();
    GrouperSession s= new GrouperSession();
    try {
      s.start( G, G.config("member.system"), true );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    G.destroy();
  }
  
  /* Start and end a session as SubjectID "member.system", 1 argument method */
  public void testSessionStartEndAsMemberSystemOneArgMethod() {
    G = new Grouper();
    G.init();
    GrouperSession s= new GrouperSession();
    try {
      s.start( G, G.config("member.system") );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    try {
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending session");
    }
    G.destroy();
  }
  
  /* Start and end a session as SubjectID "member.system", 2 argument method */
  public void testSessionStartEndAsMemberSystemTwoArgMethod() {
    G = new Grouper();
    G.init();
    GrouperSession s= new GrouperSession();
    try {
      s.start( G, G.config("member.system"), true );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    try {
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending session");
    }
    G.destroy();
  }

  /* Attempt to end a session that hasn't been started */
  public void testSessionEndWithoutStart() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    try {
      // XXX This may fail if we start throwing exceptions. 
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending unstarted session");
    }
    G.destroy();
  }


  /*
   * Class: GrouperField
   */

  /* Instantiate a GrouperField instance */
  public void testGrouperFieldInstantiate() {
    GrouperField field = new GrouperField();

    Class  klass    = field.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperField";

    Assert.assertNotNull(field);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }


  /*
   * Class: GrouperFields
   */

  /* Instantiate a GrouperFields instance */
  public void testGrouperFieldsInstantiate() {
    GrouperFields fields = new GrouperFields();

    Class  klass    = fields.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperFields";

    Assert.assertNotNull(fields);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Get cached GrouperFields */
  public void testGetGrouperFields() {
    G = new Grouper();
    G.init();
    GrouperSession  s       = new GrouperSession();
    List            fields  = G.groupFields();
    Assert.assertNotNull(fields);
    Assert.assertEquals(10, fields.size());

    String field;
    field = "admins:ADMIN:ADMIN:TRUE";
    Assert.assertTrue( field.equals( fields.get(0).toString() ) );
    field = "description:READ:ADMIN:FALSE";
    Assert.assertTrue( field.equals( fields.get(1).toString() ) );
    field = "descriptor:READ:ADMIN:FALSE";
    Assert.assertTrue( field.equals( fields.get(2).toString() ) );
    field = "members:READ:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(3).toString() ) );
    field = "optins:READ:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(4).toString() ) );
    field = "optouts:READ:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(5).toString() ) );
    field = "readers:UPDATE:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(6).toString() ) );
    field = "stem:READ:ADMIN:FALSE";
    Assert.assertTrue( field.equals( fields.get(7).toString() ) );
    field = "updaters:UPDATE:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(8).toString() ) );
    field = "viewers:UPDATE:UPDATE:TRUE";
    Assert.assertTrue( field.equals( fields.get(9).toString() ) );

    G.destroy();
  }


  /*
   * Class: GrouperType
   */
 
  /* Instantiate a GrouperType instance */
  public void testGrouperTypeInstantiate() {
    GrouperType type = new GrouperType();

    Class  klass    = type.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperType";

    Assert.assertNotNull(type);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }


  /*
   * Class: GrouperTypes
   */

  /* Instantiate a GrouperTypes instance */
  public void testGrouperTypesInstantiate() {
    GrouperTypes types = new GrouperTypes();

    Class  klass    = types.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperTypes";

    Assert.assertNotNull(types);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Get cached GrouperTypes */
  public void testGetGrouperTypes() {
    G = new Grouper();
    G.init();
    GrouperSession s  = new GrouperSession();
    List types        = G.groupTypes();
    Assert.assertNotNull(types);
    Assert.assertEquals(1, types.size());

    String type = "1";
    Assert.assertTrue( type.equals( types.get(0).toString() ) );

    G.destroy();
  }


  /*
   * Class: GrouperTypeDef
   */
 
  /* Instantiate a GrouperTypeDef instance */
  public void testGrouperTypeDefInstantiate() {
    GrouperTypeDef typeDef = new GrouperTypeDef();

    Class  klass    = typeDef.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperTypeDef";

    Assert.assertNotNull(typeDef);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }


  /*
   * Class: GrouperTypeDefs
   */

  /* Instantiate a GrouperTypeDefs instance */
  public void testGrouperTypeDefsInstantiate() {
    GrouperTypeDefs typeDefs = new GrouperTypeDefs();

    Class  klass    = typeDefs.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperTypeDefs";

    Assert.assertNotNull(typeDefs);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Get cached GrouperTypeDefs */
  public void testGetGrouperTypeDefs() {
    G = new Grouper();
    G.init();
    GrouperSession  s         = new GrouperSession();
    List            typeDefs  = G.groupTypeDefs();
    Assert.assertNotNull(typeDefs);
    Assert.assertEquals(10, typeDefs.size());

    String typeDef;
    typeDef = "1:stem";
    Assert.assertTrue( typeDef.equals( typeDefs.get(0).toString() ) );
    typeDef = "1:descriptor";
    Assert.assertTrue( typeDef.equals( typeDefs.get(1).toString() ) );
    typeDef = "1:description";
    Assert.assertTrue( typeDef.equals( typeDefs.get(2).toString() ) );
    typeDef = "1:members";
    Assert.assertTrue( typeDef.equals( typeDefs.get(3).toString() ) );
    typeDef = "1:viewers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(4).toString() ) );
    typeDef = "1:readers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(5).toString() ) );
    typeDef = "1:updaters";
    Assert.assertTrue( typeDef.equals( typeDefs.get(6).toString() ) );
    typeDef = "1:admins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(7).toString() ) );
    typeDef = "1:optins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(8).toString() ) );
    typeDef = "1:optouts";
    Assert.assertTrue( typeDef.equals( typeDefs.get(9).toString() ) );


    G.destroy();
  }
 
  /*
   * Class: GrouperGroup
   */

  /* Instantiate a GrouperGroup instance */
  public void testGrouperGroupInstantiate() {
    GrouperGroup g = new GrouperGroup();

    Class  klass    = g.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperGroup";

    Assert.assertNotNull(g);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /*
   * Class: GrouperSchema
   */

  /* Instantiate a GrouperSchema instance */
  public void testGrouperSchemaInstantiate() {
    GrouperSchema schema = new GrouperSchema();

    Class  klass    = schema.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperSchema";

    Assert.assertNotNull(schema);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /*
   * Class: GrouperAttribute
   */

  /* Instantiate a GrouperAttribute instance */
  public void testGrouperAttributeInstantiate() {
    GrouperAttribute attr = new GrouperAttribute();

    Class  klass    = attr.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperAttribute";

    Assert.assertNotNull(attr);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /*
   * Class: GrouperMembership
   */

  /* Instantiate a GrouperMembership instance */
  public void testGrouperMembershipInstantiate() {
    GrouperMembership list = new GrouperMembership();

    Class  klass    = list.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMembership";

    Assert.assertNotNull(list);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /*
   * Class: GrouperMember
   */

  /* Instantiate a GrouperMembership instance */
  public void testGrouperMemberInstantiate() {
    GrouperMember member = new GrouperMember();

    Class  klass    = member.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMember";

    Assert.assertNotNull(member);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /*
   * Class: GrouperMembers
   */

  /* Instantiate a GrouperMembership instance */
  public void testGrouperMembersInstantiate() {
    GrouperMembers members = new GrouperMembers();

    Class  klass    = members.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMembers";

    Assert.assertNotNull(members);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }
  /*
   * Class: GrouperMemberType
   */

  /* Instantiate a GrouperMembership instance */
  public void testGrouperMemberTypeInstantiate() {
    GrouperMemberType memberType = new GrouperMemberType();

    Class  klass    = memberType.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperMemberType";

    Assert.assertNotNull(memberType);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /*
   * Does Group exist?  No.
   */
  public void testGroupExistFalse() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    s.start( G, G.config("member.system"), true );
    GrouperGroup grp = new GrouperGroup();
    // Attach a session
    grp.session(s);

    // Identify the group
    grp.attribute("stem", "stem.0");
    grp.attribute("descriptor", "descriptor.0");

    // Confirm that group doesn't exist
    Assert.assertFalse( grp.exist() );

    // We're done
    s.end();
    G.destroy();
  }

  /*
   * Create a group
   */
  public void testCreateGroup() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    s.start( G, G.config("member.system"), true );
    GrouperGroup grp = new GrouperGroup();
    // Attach a session
    grp.session(s);

    // Identify the group
    grp.attribute("stem", "stem.1");
    grp.attribute("descriptor", "descriptor.1");
    // Describe the group
    grp.attribute("description", "group.1");

    // Create it
    grp.create();

    // Confirm that the group exists
    Assert.assertTrue( grp.exist() );
    
    // We're done
    s.end();
    G.destroy();
  }

  /*
   * Fetch a group
   */
  public void testFetchGroup() {
    G = new Grouper();
    G.init();
    GrouperSession s = new GrouperSession();
    s.start( G, G.config("member.system"), true );
    GrouperGroup grp = new GrouperGroup();
    // Attach a session
    grp.session(s);

    // Identify the group
    grp.attribute("stem", "stem.1");
    grp.attribute("descriptor", "descriptor.1");

    // Confirm the class type of the group
    Class  klass    = grp.getClass();
    String expKlass = "edu.internet2.middleware.grouper.GrouperGroup";

    Assert.assertNotNull(grp);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  
    // Confirm that we can fetch an attribute and that it 
    // matches what we expect. 
    GrouperAttribute description  = grp.attribute("description");
    String expDescription         = "group.1";

    Assert.assertNotNull(description);
    Assert.assertTrue( expDescription.equals(description.value()) );

    // We're done
    s.end();
    G.destroy();
  }
}

