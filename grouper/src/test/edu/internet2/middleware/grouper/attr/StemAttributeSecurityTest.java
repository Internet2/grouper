/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * @author mchyzer
 *
 */
public class StemAttributeSecurityTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new StemAttributeSecurityTest("testSecuritySubj5"));
  }

  /** grouper sesion */
  private GrouperSession grouperSession;
  /** edu stem */
  private Stem edu;
  /** test stem */
  private Stem stem;
  /** root stem */
  private Stem root;
  /** top stem */
  private Stem top;
  /** */
  private Stem etc;
  /** */
  private Group wheel;
  /** */
  private AttributeDef attributeDef1;
  /** */
  private AttributeDef attributeDef2;
  /** */
  private AttributeDefName attributeDefName1_1;
  /** */
  private AttributeDefName attributeDefName1_2;
  /** */
  private AttributeDefName attributeDefName2_1;
  /** */
  private AttributeDefName attributeDefName2_2;

  /**
   * 
   */
  public StemAttributeSecurityTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public StemAttributeSecurityTest(String name) {
    super(name);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    super.setUp();
    this.grouperSession     = SessionHelper.getRootSession();
    this.root  = StemHelper.findRootStem(grouperSession);
    this.edu   = StemHelper.addChildStem(root, "edu", "education");
    this.stem = StemHelper.addChildStem(this.edu, "stem", "the stem");
    this.top = this.root.addChildStem("top", "top display name");

    @SuppressWarnings("unused")
    Subject subject = SubjectTestHelper.SUBJ0;

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.create.grant.all.create", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.create.grant.all.stem", "false");

    this.etc = new StemSave(this.grouperSession).assignStemNameToEdit("etc").assignName("etc").save();
    this.wheel = etc.addChildGroup("wheel","wheel");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    this.attributeDef1 = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    this.attributeDef1.setAssignToStem(true);
    this.attributeDef1.store();
    this.attributeDef2 = this.top.addChildAttributeDef("test2", AttributeDefType.attr);
    this.attributeDef2.setAssignToStem(true);
    this.attributeDef2.store();
  
    this.attributeDefName1_1 = this.top.addChildAttributeDefName(attributeDef1, "testName1_1", "test name1_1");
    this.attributeDefName1_2 = this.top.addChildAttributeDefName(attributeDef1, "testName1_2", "test name1_2");
    this.attributeDefName2_1 = this.top.addChildAttributeDefName(attributeDef2, "testName2_1", "test name2_1");
    this.attributeDefName2_2 = this.top.addChildAttributeDefName(attributeDef2, "testName2_2", "test name2_2");
  
    //subj0 cant stemAttrRead/stemAttrUpdate in stem or update the attribute (def1)
    //subj1 can stemAttrRead/stemAttrUpdate in stem but nothing on attribute (def1)
    //subj2 cant stemAttrRead/stemAttrUpdate in stem, can update attribute (def1)
    //subj3 is wheel group
    //subj4 can stemAttrRead/stemAttrUpdate in stem and admin attribute (def1)
    //subj5 can stem in stem and admin attribute (def1)
    //subj6 can stemAttrRead/stemAttrUpdate in stem and update attribute (not read) (def1)
    //subj7 can stemAttrRead/stemAttrUpdate in stem and update/read attribute (def1)
    //subj8 can create in stem and admin attribute (def1)
    //subj9 can stemAttrRead in stem and admin attribute (def1)

    this.stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_READ);
    this.stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ATTR_UPDATE);
    
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_UPDATE, true);
    
    this.wheel.addMember(SubjectTestHelper.SUBJ3);

    this.stem.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.STEM_ATTR_READ);
    this.stem.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.STEM_ATTR_UPDATE);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    this.stem.grantPriv(SubjectTestHelper.SUBJ5, NamingPrivilege.STEM);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    this.stem.grantPriv(SubjectTestHelper.SUBJ6, NamingPrivilege.STEM_ATTR_READ);
    this.stem.grantPriv(SubjectTestHelper.SUBJ6, NamingPrivilege.STEM_ATTR_UPDATE);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_UPDATE, true);

    this.stem.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM_ATTR_READ);
    this.stem.grantPriv(SubjectTestHelper.SUBJ7, NamingPrivilege.STEM_ATTR_UPDATE);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, true);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_READ, true);

    this.stem.grantPriv(SubjectTestHelper.SUBJ8, NamingPrivilege.CREATE);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    this.stem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM_ATTR_READ);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_ADMIN, true);
  }

  /**
   * @throws Exception 
   */
  public void testGrouperSystem() throws Exception {

    //###################
    // try grouper system
    
    //assign these
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2).isChanged());
    
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(2, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(2, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());
    
    assertEquals(3, this.stem.getAttributeDelegate().retrieveAssignments().size());

    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());

    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());
    
  }
  
  /**
   * subj0 cant stemAttrRead/stemAttrUpdate in stem or update the attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj0() throws Exception {
  
    //###################
    // assign an attribute

    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );

    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }

    //############################################
    // Try to read an attribute
    
    
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    //##############################
    // remove attributes
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
  }

  /**
   * @throws Exception 
   */
  public void testSecuritySubjOrig() throws Exception {
  
  
    
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
  
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ1 );
  
    //subj1 cant even update/admin the attribute, but can admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    //subj1 cant even update/admin the attribute, but can admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ2 );
  
    //subj2 can admin the attribute, but cant admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    //subj2 cant admin the attribute, and cant admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ3 );
  
    //subj3 is a wheel member
    this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
  
    //subj3 is a wheel member
    this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ4 );
  
    //subj4 can admin group and admin attribute (def1)
    this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
  
    //subj4 can admin group and admin attribute (def1)
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ5 );
  
    //subj5 can stem the stem and admin attribute (def1)
    this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
  
    //subj5 can stem the stem and admin attribute (def1)
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ6 );
  
    //subj6 can admin group and update attribute (not read) (def1)
    this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
  
    //subj6 can admin group and update attribute (not read) (def1)
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ7 );
  
    //subj7 can admin group and update/read attribute (def1)
    this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
  
    //subj7 can admin group and update/read attribute (def1)
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ8 );
  
    //subj8 can create the stem and admin attribute (def1)
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    //subj8 can create the stem and admin attribute (def1)
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
  
    //subj is root, can do anything
    this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
  
    //subj is root, can do anything
    this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
  
  
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    
    //############################################
    // Try to read an attribute
    
    
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(2, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(2, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());
    
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());
  
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());  
  
  
    
  }

  /**
   * subj1 can stemAttrRead/stemAttrUpdate in stem but nothing on attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj1() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ1 );
  
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    //############################################
    // Try to read an attribute
    
    
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    //##############################
    // remove attributes
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
  }

  /**
   * subj2 cant stemAttrRead/stemAttrUpdate in stem, can update attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj2() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ2 );
  
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    //############################################
    // Try to read an attribute
    
    
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    //##############################
    // remove attributes
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
  }

  /**
   * subj3 is wheel group
   * @throws Exception 
   */
  public void testSecuritySubj3() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ3 );
  
    //assign these
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2).isChanged());
    
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(2, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(2, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());
    
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());

    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());
    
    
  }

  /**
   * subj4 can stemAttrRead/stemAttrUpdate in stem and admin attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj4() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ4 );
  
    //assign these
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }

    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
  
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    
    
  }

  /**
   * subj5 can stem in stem and admin attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj5() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ5 );

    //assign these
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());

    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }

    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    AttributeAssignResult attributeAssignResult = this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);

    assertTrue(attributeAssignResult.isChanged());
    
    attributeAssignResult = this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2);

    assertFalse(attributeAssignResult.isChanged());

    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    
  }

  /**
   * subj6 can stemAttrRead/stemAttrUpdate in stem and update attribute (not read) (def1)
   * @throws Exception 
   */
  public void testSecuritySubj6() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ6 );
  
    
    
    //assign these
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());

    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
  }

  /**
   * subj7 can stemAttrRead/stemAttrUpdate in stem and update/read attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj7() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ7 );
  
    //assign these
    assertTrue(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    assertFalse(this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    
    assertTrue(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    assertEquals(1, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    assertTrue(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    assertFalse(this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
  
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    
    
  }
  
  /**
   * subj8 can create in stem and admin attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj8() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ8 );


    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }

    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    //############################################
    // Try to read an attribute
    
    
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().hasAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    //##############################
    // remove attributes
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    
  }
  
  /**
   * subj9 can stemAttrRead in stem and admin attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj9() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ9 );

    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    try {
      this.stem.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }

    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.stem.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.stem.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.stem.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    
  }
}
