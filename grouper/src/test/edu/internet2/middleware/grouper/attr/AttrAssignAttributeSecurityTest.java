/**
 * Copyright 2012 Internet2
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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * @author mchyzer
 *
 */
public class AttrAssignAttributeSecurityTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new AttrAssignAttributeSecurityTest("testGrouperSystem"));
    TestRunner.run(AttrAssignAttributeSecurityTest.class);
  }

  /** grouper sesion */
  private GrouperSession grouperSession;
  /** edu stem */
  private Stem edu;
  /** test attribute assign */
  private AttributeAssign attrAssignToAssignTo;
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
  public AttrAssignAttributeSecurityTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttrAssignAttributeSecurityTest(String name) {
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
    Group group = StemHelper.addChildGroup(this.edu, "group", "the group");
    this.top = this.root.addChildStem("top", "top display name");
    AttributeDef metaAttributeDef = this.top.addChildAttributeDef("testGroup", AttributeDefType.attr);
    metaAttributeDef.setAssignToGroup(true);
    metaAttributeDef.store();
    AttributeDefName metaAttributeDefName = this.top.addChildAttributeDefName(
        metaAttributeDef, "testGroupName", "test group name");
    
    group.getAttributeDelegate().assignAttribute(metaAttributeDefName);
    
    this.attrAssignToAssignTo = group.getAttributeDelegate()
      .retrieveAssignments(metaAttributeDefName).iterator().next();
    
    @SuppressWarnings("unused")
    Subject subject = SubjectTestHelper.SUBJ0;
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    this.etc = new StemSave(this.grouperSession).assignStemNameToEdit("etc").assignName("etc").save();
    this.wheel = etc.addChildGroup("wheel","wheel");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    
    this.attributeDef1 = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    
    this.attributeDef1.setAssignToGroupAssn(true);
    this.attributeDef1.store();
    
    this.attributeDef2 = this.top.addChildAttributeDef("test2", AttributeDefType.attr);
  
    this.attributeDef2.setAssignToGroupAssn(true);
    this.attributeDef2.store();

    this.attributeDefName1_1 = this.top.addChildAttributeDefName(attributeDef1, "testName1_1", "test name1_1");
    this.attributeDefName1_2 = this.top.addChildAttributeDefName(attributeDef1, "testName1_2", "test name1_2");
    this.attributeDefName2_1 = this.top.addChildAttributeDefName(attributeDef2, "testName2_1", "test name2_1");
    this.attributeDefName2_2 = this.top.addChildAttributeDefName(attributeDef2, "testName2_2", "test name2_2");
  
    //subj0 cant update/read attributeToAssignTo or update the attribute (def1), subject has admin on group
    //subj1 can update/read attributeToAssignTo but nothing on attribute (def1), subject has admin on group
    //subj2 cant update/read attributeToAssignTo, can update attribute (def1), subject has admin on group
    //subj3 is wheel group
    //subj4 can update/read attributeToAssignTo and admin attribute (def1), subject has admin on group
    //subj5 can read attributeToAssignTo and admin attribute (def1), subject has admin on group
    //subj6 can update/read attributeToAssignTo and update attribute (not read) (def1), subject has admin on group
    //subj7 can update/read attributeToAssignTo and update/read attribute (def1), subject has admin on group
    //subj8 can admin attributeToAssignTo and admin the attribute (def1) but nothing on group
    //subj9 can admin attributeToAssignTo and admin the attribute (def1) and groupAttrRead on group
    
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.ADMIN);
    group.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.ADMIN);
    group.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    group.grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    group.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.GROUP_ATTR_READ);

    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, true);
    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
    
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_UPDATE, true);
    
    this.wheel.addMember(SubjectTestHelper.SUBJ3);

    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_UPDATE, true);
    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, true);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, true);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_UPDATE, true);
    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, true);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_UPDATE, true);

    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, true);
    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_READ, true);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, true);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_READ, true);

    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, true);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    metaAttributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_ADMIN, true);
    this.attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_ADMIN, true);

  }

  /**
   * @throws Exception 
   */
  public void testGrouperSystem() throws Exception {

    //###################
    // try grouper system
    
    //assign these
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_2).isChanged());
    
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(2, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(2, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());
    
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());

    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());
    
  }
  
  /**
   * subj0 cant admin group or update the attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj0() throws Exception {
  
    //###################
    // assign an attribute

    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );

    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }

    //############################################
    // Try to read an attribute
    
    
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    //##############################
    // remove attributes
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
  }

  /**
   * subj1 can admin group but nothing on attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj1() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ1 );
  
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    //############################################
    // Try to read an attribute
    
    
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    //##############################
    // remove attributes
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
  }

  /**
   * subj2 cant admin group, can update attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj2() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ2 );
  
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException ipe) {
      //good
    }
    //subj0 cant even update/admin the attribute, or admin the group
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    //############################################
    // Try to read an attribute
    
    
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    //##############################
    // remove attributes
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
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
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_2).isChanged());
    
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(2, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(2, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());
    
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1).isChanged());
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2).isChanged());

    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_2));
    
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size());
    
    
  }

  /**
   * subj4 can admin group and admin attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj4() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ4 );
  
    //assign these
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }

    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
  
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    
    
  }

  /**
   * subj5 can read group and admin attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj5() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ5 );

    //assign these
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }

    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    
    
  }

  /**
   * subj6 can admin group and update attribute (not read) (def1)
   * @throws Exception 
   */
  public void testSecuritySubj6() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ6 );
  
    
    
    //assign these
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());

    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
  }

  /**
   * subj7 can admin group and update/read attribute (def1)
   * @throws Exception 
   */
  public void testSecuritySubj7() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ7 );
  
    //assign these
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1).isChanged());
    
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    assertEquals(1, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    assertTrue(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2).isChanged());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1).isChanged());
  
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    
    
  }
  

  /**
   * subj8 can admin attributeToAssignTo, can admin attribute (def1), but nothing on group
   * @throws Exception 
   */
  public void testSecuritySubj8() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ8 );
  
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    //############################################
    // Try to read an attribute
    
    
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size();
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2).size();
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    //##############################
    // remove attributes
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
  }
  

  /**
   * subj9 can admin attributeToAssignTo and admin attribute (def1), but only groupAttrRead on group
   * @throws Exception 
   */
  public void testSecuritySubj9() throws Exception {
  
    //###################
    // assign an attribute
  
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ9 );

    //assign these
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().assignAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }

    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
  
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_1);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName1_2);
      fail("Not allowed");
    } catch (InsufficientPrivilegeException ipe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_1);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
    try {
      this.attrAssignToAssignTo.getAttributeDelegate().removeAttribute(attributeDefName2_2);
      fail("Not allowed");
    } catch (AttributeDefNotFoundException adnfe) {
      //good
    }
  
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_1));
    assertFalse(this.attrAssignToAssignTo.getAttributeDelegate().hasAttribute(attributeDefName1_2));
    
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAttributes(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDef1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_1).size());
    assertEquals(0, this.attrAssignToAssignTo.getAttributeDelegate().retrieveAssignments(attributeDefName1_2).size());
    
    
  }
}
