/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * $Id: TestConfigAndSchema.java,v 1.13 2004-12-02 16:09:02 blair Exp $
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
    Assert.assertEquals(15, typeDefs.size());
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
    typeDef = "naming:creators";
    Assert.assertTrue( typeDef.equals( typeDefs.get(13).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(13).getClass().getName() ) );
    typeDef = "naming:stemmers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(14).toString() ) );
    Assert.assertTrue( klass.equals( typeDefs.get(14).getClass().getName() ) );
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

