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

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefAddException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class AttributeDefTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefTest("testHibernateSecurity"));
  }
  
  /**
   * 
   */
  public AttributeDefTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeDefTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** top stem */
  private Stem top;

  /** some group */
  private Group group;
  
  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
    this.group = this.top.addChildGroup("group", "group");
  }

  /**
   * make sure security is there
   */
  public void testHibernateSecurity() {
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    try {
    
      this.top.addChildAttributeDef("test", AttributeDefType.attr);
      fail("This shouldnt be allowed");
    } catch (AttributeDefAddException e) {
      //acceptable
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    //####################################
    //grant that subject stem on that stem
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    this.top.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    this.top.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_UPDATE);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    try {
    
      this.top.addChildAttributeDef("test", AttributeDefType.attr);
      fail("This shouldnt be allowed");
    } catch (AttributeDefAddException e) {
      //acceptable
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    //####################################
    //grant that subject create on that stem
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    this.top.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);

    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    //this should work
    this.top.addChildAttributeDef("test", AttributeDefType.attr);
    
    //####################################
    //grant that subject stem on that stem
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    this.top.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);

    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ1 );
    
    //this should work
    this.top.addChildAttributeDef("test1", AttributeDefType.attr);
    
    
  }
  
  /**
   * make sure security is there
   */
  public void testHibernateSecurityAdmin() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    this.group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);

    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    try {
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    try {
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, true);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ1);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().hasAttrDefAttrRead(SubjectTestHelper.SUBJ1);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    try {
      attributeDef.getPrivilegeDelegate().hasAttrDefAttrUpdate(SubjectTestHelper.SUBJ1);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (RevokePrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (RevokePrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    try {
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, true);
      fail("This shouldnt be allowed");
    } catch (RevokePrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    //####################################
    //grant that subject update on that attributeDef, not enough
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, true);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    try {
    
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    try {
      
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    try {
      
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, true);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    try {
      attributeDef.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ1);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().hasAttrDefAttrRead(SubjectTestHelper.SUBJ1);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    try {
      attributeDef.getPrivilegeDelegate().hasAttrDefAttrUpdate(SubjectTestHelper.SUBJ1);
      fail("This shouldnt be allowed");
    } catch (GrantPrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (RevokePrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    try {
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
      fail("This shouldnt be allowed");
    } catch (RevokePrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }
    
    try {
      attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, true);
      fail("This shouldnt be allowed");
    } catch (RevokePrivilegeException e) {
      //ok
    } catch (InsufficientPrivilegeException e) {
      //good
    }

    //####################################
    //grant that subject update on that attributeDef, not enough
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, true);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, true);
    
    attributeDef.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ1);
    attributeDef.getPrivilegeDelegate().hasAttrDefAttrRead(SubjectTestHelper.SUBJ1);
    attributeDef.getPrivilegeDelegate().hasAttrDefAttrUpdate(SubjectTestHelper.SUBJ1);

    assertTrue(attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, true));
    assertTrue(attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true));
    assertTrue(attributeDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, true));
  }
  
  /**
   * 
   */
  public void testHibernateSecurity2() {
    
    //dont default any
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");

    int grouperGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    int grouperMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_memberships");
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    
    int newGrouperGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    int newGrouperMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_memberships");
    
    assertEquals("Should make 6 group sets for permissions", grouperGroupSetCount + 8, newGrouperGroupSetCount);
    assertEquals("Should make a memberships for owner", grouperMembershipCount + 1, newGrouperMembershipCount);

    //#############################################
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "true");
    
    grouperGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    grouperMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_memberships");
    
    attributeDef = this.top.addChildAttributeDef("test2", AttributeDefType.attr);
    
    newGrouperGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_group_set");
    newGrouperMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_memberships");
    
    assertEquals("Should make 6 group sets for permissions", grouperGroupSetCount + 8, newGrouperGroupSetCount);
    assertEquals("Should make a memberships for owner, and 2 for grouperAll", grouperMembershipCount + 3, newGrouperMembershipCount);
    
    //################################################
    // make sure user can admin to same an attribute
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );

    attributeDef.setDescription("whatever");
    try {
      attributeDef.store();
      fail("Shouldnt succeed");
    } catch (Exception e) {
      //good
    }
    
    //make the user an admin
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );

    attributeDef.setDescription("whatever");
    //should succeed
    attributeDef.store();
    
    
    //#################################################
    //make sure if in group, then still allowed
    //TODO check group

  }
  
  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    assertNotNull(attributeDef.getId());

    //lets retrieve by id
    AttributeDef attributeDef2 = AttributeDefFinder.findById(attributeDef.getId(), true);

    assertEquals(attributeDef.getId(), attributeDef2.getId());
    
    //lets retrieve by name
    attributeDef2 = AttributeDefFinder.findByName("top:test", true);
    
    assertEquals("top:test", attributeDef2.getName());
    assertEquals(attributeDef.getId(), attributeDef2.getId());

    //try to add another
    try {
      attributeDef2 = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    } catch (AttributeDefAddException adae) {
      assertTrue(adae.getMessage(), adae.getMessage().contains("attribute def already exists"));
    }

    attributeDef2 = this.top.addChildAttributeDef("test2", AttributeDefType.attr);
    
    
  }

  /**
   * make sure security is there
   */
  public void testHibernateGroup() {
    
//    this.grouperSession.stop();
//    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    
    assertNotNull(attributeDef.getCreatorId());
    assertNotNull(attributeDef.getCreatedOn());
    
    
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
  
    assertFalse(this.group.getAttributeDelegate().hasAttributeByName("top:testName"));
    
    assertTrue(this.group.getAttributeDelegate().assignAttribute(attributeDefName).isChanged());
  
    assertTrue(this.group.getAttributeDelegate().hasAttributeByName("top:testName"));

    assertFalse(this.group.getAttributeDelegate().assignAttribute(attributeDefName).isChanged());
    
  }

  /**
   * 
   */
  public void testHibernateSecurityWheel() {
    
    Stem etc = new StemSave(this.grouperSession).assignStemNameToEdit("etc").assignName("etc").save();
    Group wheel        = etc.addChildGroup("wheel","wheel");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    //################################################
    // make sure user can admin to same an attribute
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
  
    attributeDef.setDescription("whatever");
    try {
      attributeDef.store();
      fail("Shouldnt succeed");
    } catch (Exception e) {
      //good
    }
    
    //make the user an admin
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    
    wheel.addMember(SubjectTestHelper.SUBJ0);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
  
    attributeDef.setDescription("whatever");
    //should succeed
    attributeDef.store();
    
    
  }

  /**
   * make an example group for testing
   * @return an example group
   */
  public static AttributeDef exampleAttributeDef() {
    AttributeDef attributeDef = new AttributeDef();
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.setAssignToAttributeDefAssn(true);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.setAssignToEffMembershipAssn(true);
    attributeDef.setAssignToGroup(true);
    attributeDef.setAssignToGroupAssn(true);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.setAssignToImmMembershipAssn(true);
    attributeDef.setAssignToMember(true);
    attributeDef.setAssignToMemberAssn(true);
    attributeDef.setAssignToStem(true);
    attributeDef.setAssignToStemAssn(true);
    attributeDef.setAttributeDefPublic(true);
    attributeDef.setAttributeDefType(AttributeDefType.attr);
    attributeDef.setContextId("contextId");
    attributeDef.setCreatedOnDb(4L);
    attributeDef.setCreatorId("creatorId");
    attributeDef.setDescription("description");
    attributeDef.setExtensionDb("extension");
    attributeDef.setHibernateVersionNumber(5L);
    attributeDef.setId("id");
    attributeDef.setIdIndex(12345L);
    attributeDef.setLastUpdatedDb(3L);
    attributeDef.setMultiAssignable(true);
    attributeDef.setMultiValued(true);
    attributeDef.setNameDb("name");
    attributeDef.setStemId("stemId");
    attributeDef.setValueType(AttributeDefValueType.floating);
    return attributeDef;
  }
  
  /**
   * make an example attributeDef for testing
   * @return an example attributeDef
   */
  public static AttributeDef exampleAttributeDefDb() {
    return exampleAttributeDefDb("test", "testAttributeDef");
  }

  /**
   * make an example attributeDef for testing
   * @param stemName 
   * @param extension 
   * @return an example attributeDef
   */
  public static AttributeDef exampleAttributeDefDb(String stemName, String extension) {

    String name = stemName + ":" + extension;
    
    AttributeDef attributeDef = AttributeDefFinder.findByName(name, false);

    if (attributeDef == null) {
      Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit(stemName).assignName(stemName).assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();
      attributeDef = stem.addChildAttributeDef(extension, AttributeDefType.attr);
    }
    return attributeDef;
  }


  /**
   * make an example attribute def for testing
   * @return an example attribute def
   */
  public static AttributeDef exampleRetrieveAttributeDefDb() {
    AttributeDef attributeDef = AttributeDefFinder.findByName("test:testAttributeDef", true);
    return attributeDef;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AttributeDef attributeDefOriginal = exampleAttributeDefDb("test", "testAttributeDefInsert");
    
    //do this because last membership update isnt there, only in db
    attributeDefOriginal =  AttributeDefFinder.findByName("test:testAttributeDefInsert", true);
    AttributeDef attributeDefCopy = AttributeDefFinder.findByName("test:testAttributeDefInsert", true);
    AttributeDef attributeDefCopy2 = AttributeDefFinder.findByName("test:testAttributeDefInsert", true);
    attributeDefCopy.delete();
    
    //lets insert the original
    attributeDefCopy2.xmlSaveBusinessProperties(null);
    attributeDefCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeDefCopy = AttributeDefFinder.findByName("test:testAttributeDefInsert", true);
    
    assertFalse(attributeDefCopy == attributeDefOriginal);
    assertFalse(attributeDefCopy.xmlDifferentBusinessProperties(attributeDefOriginal));
    assertFalse(attributeDefCopy.xmlDifferentUpdateProperties(attributeDefOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = null;
    AttributeDef exampleAttributeDef = null;

    
    //TEST UPDATE PROPERTIES
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();
      
      attributeDef.setContextId("abc");
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertTrue(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setContextId(exampleAttributeDef.getContextId());
      attributeDef.xmlSaveUpdateProperties();

      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
      
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setCreatedOnDb(99L);
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertTrue(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setCreatedOnDb(exampleAttributeDef.getCreatedOnDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setCreatorId("abc");
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertTrue(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setCreatorId(exampleAttributeDef.getCreatorId());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setHibernateVersionNumber(99L);
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertTrue(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setHibernateVersionNumber(exampleAttributeDef.getHibernateVersionNumber());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToAttributeDef(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToAttributeDef(exampleAttributeDef.isAssignToAttributeDef());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToAttributeDefAssn(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToAttributeDefAssn(exampleAttributeDef.isAssignToAttributeDefAssn());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToEffMembership(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToEffMembership(exampleAttributeDef.isAssignToEffMembership());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToEffMembershipAssn(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToEffMembershipAssn(exampleAttributeDef.isAssignToEffMembershipAssn());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToGroup(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToGroup(exampleAttributeDef.isAssignToGroup());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToGroupAssn(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToGroupAssn(exampleAttributeDef.isAssignToGroupAssn());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToImmMembership(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToImmMembership(exampleAttributeDef.isAssignToImmMembership());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToImmMembershipAssn(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToImmMembershipAssn(exampleAttributeDef.isAssignToImmMembershipAssn());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToMember(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToMember(exampleAttributeDef.isAssignToMember());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToMemberAssn(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToMemberAssn(exampleAttributeDef.isAssignToMemberAssn());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToStem(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToStem(exampleAttributeDef.isAssignToStem());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAssignToStemAssn(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAssignToStemAssn(exampleAttributeDef.isAssignToStemAssn());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAttributeDefPublic(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAttributeDefPublic(exampleAttributeDef.isAttributeDefPublic());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setAttributeDefType(AttributeDefType.perm);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setAttributeDefType(exampleAttributeDef.getAttributeDefType());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setDescription("abc");
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setDescription(exampleAttributeDef.getDescription());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setExtensionDb("abc");
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setExtensionDb(exampleAttributeDef.getExtensionDb());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setId("abc");
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setId(exampleAttributeDef.getId());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setMultiAssignable(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setMultiAssignable(exampleAttributeDef.isMultiAssignable());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setMultiValued(true);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setMultiValued(exampleAttributeDef.isMultiValued());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
    
    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setNameDb("abc");
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setNameDb(exampleAttributeDef.getName());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }

    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setStemId("abc");
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setStemId(exampleAttributeDef.getStemId());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }

    {
      attributeDef = exampleAttributeDefDb();
      exampleAttributeDef = exampleRetrieveAttributeDefDb();

      attributeDef.setValueType(AttributeDefValueType.floating);
      
      assertTrue(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));

      attributeDef.setValueType(exampleAttributeDef.getValueType());
      attributeDef.xmlSaveBusinessProperties(exampleRetrieveAttributeDefDb());
      attributeDef.xmlSaveUpdateProperties();
      
      attributeDef = exampleRetrieveAttributeDefDb();
      
      assertFalse(attributeDef.xmlDifferentBusinessProperties(exampleAttributeDef));
      assertFalse(attributeDef.xmlDifferentUpdateProperties(exampleAttributeDef));
    
    }
  }

  /**
   * testFindByIdsSecure
   */
  public void testFindByIdsSecure() {
    
    AttributeDef attributeDefAb = new AttributeDefSave(this.grouperSession).assignName("a:b").assignCreateParentStemsIfNotExist(true).save();
    attributeDefAb.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    AttributeDef attributeDefAc = new AttributeDefSave(this.grouperSession).assignName("a:c").assignCreateParentStemsIfNotExist(true).save();
    attributeDefAc.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, false);
    AttributeDef attributeDefAd = new AttributeDefSave(this.grouperSession).assignName("a:d").assignCreateParentStemsIfNotExist(true).save();
    attributeDefAd.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDefAd.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    AttributeDef attributeDefAe = new AttributeDefSave(this.grouperSession).assignName("a:e").assignCreateParentStemsIfNotExist(true).save();
    attributeDefAe.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    AttributeDef attributeDefAf = new AttributeDefSave(this.grouperSession).assignName("a:f").assignCreateParentStemsIfNotExist(true).save();
    attributeDefAf.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);

    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ0 );
    
    Set<AttributeDef> attributeDefs = GrouperDAOFactory.getFactory().getAttributeDef().findByIdsSecure(
        GrouperUtil.toSet(attributeDefAb.getId(), attributeDefAc.getId(), attributeDefAd.getId(), attributeDefAe.getId(), attributeDefAf.getId()), null);
    
    assertEquals(4, GrouperUtil.length(attributeDefs));
    assertTrue(attributeDefs.contains(attributeDefAb));
    assertTrue(attributeDefs.contains(attributeDefAd));
    assertTrue(attributeDefs.contains(attributeDefAe));
    assertTrue(attributeDefs.contains(attributeDefAf));
    
    //####################################
    //see about subject 1
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start( SubjectTestHelper.SUBJ1);
    
    attributeDefs = GrouperDAOFactory.getFactory().getAttributeDef().findByIdsSecure(
        GrouperUtil.toSet(attributeDefAb.getId(), attributeDefAc.getId(), attributeDefAd.getId(), attributeDefAe.getId(), attributeDefAf.getId()), null);
    
    assertEquals(2, GrouperUtil.length(attributeDefs));
    assertTrue(attributeDefs.contains(attributeDefAc));
    assertTrue(attributeDefs.contains(attributeDefAd));
    

    
  }


  /**
   * Test to verify GRP-880
   */
  public void testDeleteWithPrivileges() {
    AttributeDef attributeDef = top.addChildAttributeDef("test", AttributeDefType.attr);
    Group group1 = top.addChildGroup("group1", "group1");
    Group group2 = top.addChildGroup("group2", "group2");
    
    group1.addMember(group2.toSubject());
    group1.addMember(SubjectTestHelper.SUBJ0);
    group2.addMember(SubjectTestHelper.SUBJ1);
    group1.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);

    attributeDef.getPrivilegeDelegate().grantPriv(group1.toSubject(), AttributeDefPrivilege.ATTR_ADMIN, true);
    attributeDef.getPrivilegeDelegate().grantPriv(group1.toSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_READ, true);
    attributeDef.getPrivilegeDelegate().grantPriv(group1.toSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, true);

    attributeDef.delete();
    
    assertTrue(group1.hasMember(group2.toSubject()));
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(group1.hasAdmin(SubjectTestHelper.SUBJ2));
  }

  
}
