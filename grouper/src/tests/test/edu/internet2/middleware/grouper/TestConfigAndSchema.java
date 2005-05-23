/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
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
 * $Id: TestConfigAndSchema.java,v 1.29 2005-05-23 20:41:02 blair Exp $
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
    DB db = new DB();
    db.emptyTables();
    db.stop();
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
    Assert.assertNotNull("fields !null", fields);
    Assert.assertEquals("15 fields returned", 15, fields.size());
    GrouperField gf;
    Collections.sort(fields);

    int idx = 0;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("admins !null", gf);
    Assert.assertTrue(
                      "admins is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "admins groupField",
                      gf.field().equals("admins")
                     );
    Assert.assertTrue(
                      "admins readPriv",
                      gf.readPriv().equals("ADMIN")
                     );
    Assert.assertTrue(
                      "admins writePriv", 
                      gf.writePriv().equals("ADMIN")
                     );
    Assert.assertTrue("admins isList == true", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("creators !null", gf);
    Assert.assertTrue(
                      "creators is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "creators groupField " + gf.field(),
                      gf.field().equals("creators")
                     );
    Assert.assertTrue(
                      "creators readPriv",
                      gf.readPriv().equals("STEM")
                     );
    Assert.assertTrue(
                      "creators writePriv", 
                      gf.writePriv().equals("STEM")
                     );
    Assert.assertTrue("creators isList == true", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("description !null", gf);
    Assert.assertTrue(
                      "description is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "description groupField",
                      gf.field().equals("description")
                     );
    Assert.assertTrue(
                      "description readPriv",
                      gf.readPriv().equals("READ")
                     );
    Assert.assertTrue(
                      "description writePriv", 
                      gf.writePriv().equals("ADMIN")
                     );
    Assert.assertFalse("description isList == false", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("displayExtension !null", gf);
    Assert.assertTrue(
                      "displayExtension is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "displayExtension groupField",
                      gf.field().equals("displayExtension")
                     );
    Assert.assertTrue(
                      "displayExtension readPriv",
                      gf.readPriv().equals("VIEW")
                     );
    Assert.assertTrue(
                      "displayExtension writePriv", 
                      gf.writePriv().equals("ADMIN")
                     );
    Assert.assertFalse("displayExtension isList == false", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("field !null", gf);
    Assert.assertTrue(
                      "displayName is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "displayName groupField",
                      gf.field().equals("displayName")
                     );
    Assert.assertTrue(
                      "displayName readPriv",
                      gf.readPriv().equals("VIEW")
                     );
    Assert.assertTrue(
                      "displayName writePriv",
                      gf.writePriv().equals("SYSTEM")
                     );
    Assert.assertFalse("displayName isList == false", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("extension !null", gf);
    Assert.assertTrue(
                      "extension is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "extension groupField",
                      gf.field().equals("extension")
                     );
    Assert.assertTrue(
                      "extension readPriv",
                      gf.readPriv().equals("VIEW")
                     );
    Assert.assertTrue(
                      "extension writePriv", 
                      gf.writePriv().equals("ADMIN")
                     );
    Assert.assertFalse("extension isList == false", gf.isList());
    
    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("members !null", gf);
    Assert.assertTrue(
                      "members is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "members groupField",
                      gf.field().equals("members")
                     );
    Assert.assertTrue(
                      "members readPriv",
                      gf.readPriv().equals("READ")
                     );
    Assert.assertTrue(
                      "members writePriv", 
                      gf.writePriv().equals("UPDATE")
                     );
    Assert.assertTrue("members isList == true", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertTrue(
                      "name is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "name groupField",
                      gf.field().equals("name")
                     );
    Assert.assertTrue(
                      "name readPriv",
                      gf.readPriv().equals("VIEW")
                     );
    Assert.assertTrue(
                      "name writePriv",
                      gf.writePriv().equals("SYSTEM")
                     );
    Assert.assertFalse("name isList == false", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("optins !null", gf);
    Assert.assertTrue(
                      "optins is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "optins groupField",
                      gf.field().equals("optins")
                     );
    Assert.assertTrue(
                      "optins readPriv",
                      gf.readPriv().equals("UPDATE")
                     );
    Assert.assertTrue(
                      "optins writePriv", 
                      gf.writePriv().equals("UPDATE")
                     );
    Assert.assertTrue("optins isList == true", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("optouts !null", gf);
    Assert.assertTrue(
                      "optouts is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "optouts groupField",
                      gf.field().equals("optouts")
                     );
    Assert.assertTrue(
                      "optouts readPriv",
                      gf.readPriv().equals("UPDATE")
                     );
    Assert.assertTrue(
                      "optouts writePriv", 
                      gf.writePriv().equals("UPDATE")
                     );
    Assert.assertTrue("optouts isList == true", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("readers !null", gf);
    Assert.assertTrue(
                      "readers is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "readers groupField",
                      gf.field().equals("readers")
                     );
    Assert.assertTrue(
                      "readers readPriv",
                      gf.readPriv().equals("ADMIN")
                     );
    Assert.assertTrue(
                      "readers writePriv", 
                      gf.writePriv().equals("ADMIN")
                     );
    Assert.assertTrue("readers isList == true", gf.isList());


    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("stem !null", gf);
    Assert.assertTrue(
                      "stem is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "stem groupField",
                      gf.field().equals("stem")
                     );
    Assert.assertTrue(
                      "stem readPriv",
                      gf.readPriv().equals("VIEW")
                     );
    Assert.assertTrue(
                      "stem writePriv", 
                      gf.writePriv().equals("ADMIN")
                     );
    Assert.assertFalse("stem isList == false", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("stemmers !null", gf);
    Assert.assertTrue(
                      "stemmers is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "stemmers groupField",
                      gf.field().equals("stemmers")
                     );
    Assert.assertTrue(
                      "stemmers readPriv",
                      gf.readPriv().equals("STEM")
                     );
    Assert.assertTrue(
                      "stemmers writePriv", 
                      gf.writePriv().equals("STEM")
                     );
    Assert.assertTrue("stemmers isList == true", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("updaters !null", gf);
    Assert.assertTrue(
                      "updaters is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "updaters groupField",
                      gf.field().equals("updaters")
                     );
    Assert.assertTrue(
                      "updaters readPriv",
                      gf.readPriv().equals("ADMIN")
                     );
    Assert.assertTrue(
                      "updaters writePriv", 
                      gf.writePriv().equals("ADMIN")
                     );
    Assert.assertTrue("updaters isList == true", gf.isList());

    idx += 1;
    gf = (GrouperField) fields.get(idx);
    Assert.assertNotNull("viewers !null", gf);
    Assert.assertTrue(
                      "viewers is GrouperField", 
                      Constants.KLASS_GF.equals(gf.getClass().getName())
                     );
    Assert.assertTrue(
                      "viewers groupField",
                      gf.field().equals("viewers")
                     );
    Assert.assertTrue(
                      "viewers readPriv",
                      gf.readPriv().equals("ADMIN")
                     );
    Assert.assertTrue(
                      "viewers writePriv", 
                      gf.writePriv().equals("ADMIN")
                     );
    Assert.assertTrue("viewers isList == true", gf.isList());

  }

  // Get cached GrouperTypes 
  public void testGetGrouperTypes() {
    List types = Grouper.groupTypes();
    Assert.assertNotNull(types);
    Assert.assertEquals(2, types.size());
    String type0 = Grouper.DEF_GROUP_TYPE;
    Assert.assertTrue( type0.equals( types.get(0).toString() ) );
    Assert.assertTrue( Constants.KLASS_GT.equals( types.get(0).getClass().getName() ) );
    String type1 = Grouper.NS_TYPE;
    Assert.assertTrue( type1.equals( types.get(1).toString() ) );
    Assert.assertTrue( Constants.KLASS_GT.equals( types.get(1).getClass().getName() ) );
  }

  // Get cached GrouperTypeDefs 
  public void testGetGrouperTypeDefs() {
    List typeDefs  = Grouper.groupTypeDefs();
    Assert.assertNotNull(typeDefs);
    Assert.assertEquals(21, typeDefs.size());
    String typeDef;
    int idx = 0;
    typeDef = "base:extension";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:stem";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:name";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:description";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:displayExtension";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:displayName";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:members";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:viewers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:readers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:updaters";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:admins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:optins";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "base:optouts";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "naming:displayExtension";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "naming:displayName";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "naming:extension";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "naming:stem";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "naming:name";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "naming:description";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "naming:creators";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
    idx += 1;
    typeDef = "naming:stemmers";
    Assert.assertTrue( typeDef.equals( typeDefs.get(idx).toString() ) );
    Assert.assertTrue( Constants.KLASS_GTD.equals( typeDefs.get(idx).getClass().getName() ) );
  }

  // Get cached SubjectTypes 
  public void testGetSubjectTypes() {
    Set types = SubjectFactory.types();
    Assert.assertEquals("types == 2", 2, types.size());
    Assert.assertTrue(
      "has type 'group'", SubjectFactory.hasType("group")
    );
    Assert.assertTrue(
      "has type 'person'", SubjectFactory.hasType("person")
    );
  }

  // TODO Test boolean assertion|validity methods

}

